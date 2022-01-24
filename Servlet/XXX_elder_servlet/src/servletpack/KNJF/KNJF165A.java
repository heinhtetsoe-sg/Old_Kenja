/*
 * $Id: 131de9cfb190ea729574b1ef1c621984c0047400 $
 *
 * 作成日: 2016/12/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJF165A {

    private static final Log log = LogFactory.getLog(KNJF165A.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(svf, db2);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    public void printMain(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        Map schregMap = new HashMap();
        final List printData = getPrintData(db2, schregMap);
        svf.VrSetForm("KNJF165A.frm", 4);
        int idx = 1;

        //ヘッダ
        //タイトル
        svf.VrsOut("TITLE", _param._nendo + "　学校管理下災害一覧表");
        //期間
        final String period = printDate(db2, _param._sDate) + "～" + printDate(db2, _param._eDate);
        svf.VrsOut("PERIOD", period);
        //作成日
        svf.VrsOut("DATE", printDate(db2, _param._ctrlDate));


        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData) it.next();
            //明細
            //管理番号
            svf.VrsOut("NO", String.valueOf(idx));
            //学年
            svf.VrsOut("GRADE", data._grade);
            //組
            svf.VrsOut("HR_NAME", data._hr_class);
            //番号
            svf.VrsOut("ATTENDNO", data._attendno);
            //氏名
            svf.VrsOut("NAME", data._name);
            //保護者氏名
            svf.VrsOut("GUARD_NAME", data._guard_name);
            //災害発生日
            String dStr = printDate(db2, data._visit_date);
            final String hweek = KNJ_EditDate.h_format_W(data._visit_date);
            final String disasterDate = ("".equals(hweek)) ?  dStr : dStr + "(" + hweek + ")";
            svf.VrsOut("DISASTER_DATE", disasterDate);
            //時刻
            final String disasterTime = data._visit_hour + "時" + data._visit_minute + "分";
            svf.VrsOut("DISASTER_TIME", disasterTime);
            //場所
            svf.VrsOut("DISASTER_PLACE", data._disaster_place);
            //場合
            svf.VrsOut("DISASTER_CASE", data._disaster_case);
            //病院名
            svf.VrsOut("HOSPITAL_NAME", data._hospital);
            //転帰
            svf.VrsOut("OUTCOME", data._outcome);
            //申請日
            svf.VrsOut("APPLICANT_DATE", printDate(db2, data._remark3));
            //支給日
            svf.VrsOut("PAYMENT_DATE", printDate(db2, data._remark4));
            //医療点数
            svf.VrsOut("MEDICAL_POINT", data._remark5);
            //支給額
            svf.VrsOut("MEDICAL_MONEY", data._remark6);
            //受信日
            svf.VrsOut("RECIEVE_DATE", data._remark7);
            //備考
            svf.VrsOut("REMARK", data._remark8);

            idx++;
            _hasData = true;
            svf.VrEndRecord();
        }
        if (_hasData) {
            svf.VrEndPage();
        }
        return;
    }

    //日付の編集
    public String printDate(final DB2UDB db2, final String date) {
        String dStr = date.replace('-', '/');
        if (!"".equals(dStr) && dStr.length() >= 10) {
            if(_param._isSeireki) {
                SimpleDateFormat sdf_d = new SimpleDateFormat();
                sdf_d.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                sdf_d.applyPattern("yyyy年MM月dd日");
                dStr = sdf_d.format(java.sql.Date.valueOf(date));
            } else {
                SimpleDateFormat sdf_d = new SimpleDateFormat();
                sdf_d.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                sdf_d.applyPattern("MM月dd日");
                dStr = sdf_d.format(java.sql.Date.valueOf(date));
                final String[] gengou = KNJ_EditDate.tate_format4(db2, date);
                dStr = gengou[0] + gengou[1] + "年" + dStr;
            }
        }
        return dStr;
    }

    private List getPrintData(final DB2UDB db2, final Map schregMap) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getPrintDataSql();
        log.debug(sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String type_name = StringUtils.defaultString(rs.getString("TYPE_NAME"));
            	final String grade = StringUtils.defaultString(rs.getString("GRADE"));
            	final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
            	final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
            	final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
            	final String name = StringUtils.defaultString(rs.getString("NAME"));
            	final String guard_name = StringUtils.defaultString(rs.getString("GUARD_NAME"));
            	final String visit_date = StringUtils.defaultString(rs.getString("VISIT_DATE"));
            	final String visit_hour = StringUtils.defaultString(rs.getString("VISIT_HOUR"));
            	final String visit_minute = StringUtils.defaultString(rs.getString("VISIT_MINUTE"));
            	final String disaster_place = StringUtils.defaultString(rs.getString("DISASTER_PLACE"));
            	final String disaster_case = StringUtils.defaultString(rs.getString("DISASTER_CASE"));
            	final String hospital = StringUtils.defaultString(rs.getString("HOSPITAL"));
            	final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
            	final String outcome = StringUtils.defaultString(rs.getString("OUTCOME"));
            	final String remark3 = StringUtils.defaultString(rs.getString("REMARK3"));
            	final String remark4 = StringUtils.defaultString(rs.getString("REMARK4"));
            	final String remark5 = StringUtils.defaultString(rs.getString("REMARK5"));
            	final String remark6 = StringUtils.defaultString(rs.getString("REMARK6"));
            	final String remark7 = StringUtils.defaultString(rs.getString("REMARK7"));
            	final String remark8 = StringUtils.defaultString(rs.getString("REMARK8"));
                final PrintData printData = new PrintData(type_name, grade, hr_class, attendno, schregno, name, guard_name, visit_date, visit_hour, visit_minute, disaster_place, disaster_case, hospital, remark2, outcome, remark3, remark4, remark5, remark6, remark7, remark8);
                rtnList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getPrintDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.TYPE, ");
        stb.append("   CASE WHEN T1.TYPE = '1' THEN '内科' ");
        stb.append("                           ELSE '外科' ");
        stb.append("   END AS TYPE_NAME, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T4.NAME, ");
        stb.append("   T5.GUARD_NAME, ");
        stb.append("   T0.VISIT_DATE, ");
        stb.append("   T0.VISIT_HOUR, ");
        stb.append("   T0.VISIT_MINUTE, ");
        stb.append("   F206.NAME1 AS DISASTER_PLACE, ");
        stb.append("   F224.NAME1 AS DISASTER_CASE, ");
        //stb.append("   T0.HOSPITAL, ");
        stb.append("   '' AS HOSPITAL, ");
        stb.append("   T1.REMARK2, ");
        stb.append("   F225.NAME1 AS OUTCOME, ");
        stb.append("   T1.REMARK3, ");
        stb.append("   T1.REMARK4, ");
        stb.append("   T1.REMARK5, ");
        stb.append("   T1.REMARK6, ");
        stb.append("   T1.REMARK7, ");
        stb.append("   T1.REMARK8 ");
        stb.append(" FROM ");
        stb.append("   NURSEOFF_VISITREC_DETAIL_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("           ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND T2.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("          AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("          ON T3.YEAR     = T2.YEAR ");
        stb.append("         AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("         AND T3.GRADE    = T2.GRADE ");
        stb.append("         AND T3.HR_CLASS = T2.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T4 ");
        stb.append("          ON T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("   LEFT JOIN GUARDIAN_DAT T5 ");
        stb.append("          ON T5.SCHREGNO = T2.SCHREGNO ");
        stb.append("   INNER JOIN NURSEOFF_VISITREC_DAT T0 ");
        stb.append("           ON T0.SCHREGNO     = T1.SCHREGNO ");
        stb.append("          AND T0.VISIT_DATE   = T1.VISIT_DATE ");
        stb.append("          AND T0.VISIT_HOUR   = T1.VISIT_HOUR ");
        stb.append("          AND T0.VISIT_MINUTE = T1.VISIT_MINUTE ");
        stb.append("          AND T0.TYPE         = T1.TYPE ");
        stb.append("   LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT SEQ01 ");
        stb.append("          ON SEQ01.SCHREGNO     = T1.SCHREGNO ");
        stb.append("         AND SEQ01.VISIT_DATE   = T1.VISIT_DATE ");
        stb.append("         AND SEQ01.VISIT_HOUR   = T1.VISIT_HOUR ");
        stb.append("         AND SEQ01.VISIT_MINUTE = T1.VISIT_MINUTE ");
        stb.append("         AND SEQ01.TYPE         = T1.TYPE ");
        stb.append("         AND SEQ01.SEQ          = '01' ");
        stb.append("   LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT SEQ03 ");
        stb.append("          ON SEQ03.SCHREGNO     = T1.SCHREGNO ");
        stb.append("         AND SEQ03.VISIT_DATE   = T1.VISIT_DATE ");
        stb.append("         AND SEQ03.VISIT_HOUR   = T1.VISIT_HOUR ");
        stb.append("         AND SEQ03.VISIT_MINUTE = T1.VISIT_MINUTE ");
        stb.append("         AND SEQ03.TYPE         = T1.TYPE ");
        stb.append("         AND SEQ03.SEQ          = '03' ");
        stb.append("   LEFT JOIN NAME_MST F206 ");
        stb.append("          ON F206.NAMECD2 = SEQ03.REMARK1 ");
        stb.append("         AND F206.NAMECD1 = 'F206' ");
        stb.append("   LEFT JOIN NAME_MST F224 ");
        stb.append("          ON F224.NAMECD2 = SEQ01.REMARK4 ");
        stb.append("         AND F224.NAMECD1 = 'F224' ");
        stb.append("   LEFT JOIN NAME_MST F225 ");
        stb.append("          ON F225.NAMECD2 = T1.REMARK2 ");
        stb.append("         AND F225.NAMECD1 = 'F225' ");
        stb.append(" WHERE ");
        stb.append("   T1.VISIT_DATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
        stb.append("   AND T1.TYPE IN ('1','2') ");
        stb.append("   AND T1.SEQ     = '97' ");
        stb.append("   AND T1.REMARK1 = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.TYPE ");

        return stb.toString();
    }

    private class PrintData {
    	final String _type_name;
    	final String _grade;
    	final String _hr_class;
    	final String _attendno;
    	final String _schregno;
    	final String _name;
    	final String _guard_name;
    	final String _visit_date;
    	final String _visit_hour;
    	final String _visit_minute;
    	final String _disaster_place;
    	final String _disaster_case;
    	final String _hospital;
    	final String _remark2;
    	final String _outcome;
    	final String _remark3;
    	final String _remark4;
    	final String _remark5;
    	final String _remark6;
    	final String _remark7;
    	final String _remark8;

        PrintData(final String type_name,
        		final String grade,
        		final String hr_class,
        		final String attendno,
        		final String schregno,
        		final String name,
        		final String guard_name,
        		final String visit_date,
        		final String visit_hour,
        		final String visit_minute,
        		final String disaster_place,
        		final String disaster_case,
        		final String hospital,
        		final String remark2,
        		final String outcome,
        		final String remark3,
        		final String remark4,
        		final String remark5,
        		final String remark6,
        		final String remark7,
        		final String remark8

        ) {
        	_type_name = type_name;
        	_grade = grade;
        	_hr_class = hr_class;
        	_attendno = attendno;
        	_schregno = schregno;
        	_name = name;
        	_guard_name = guard_name;
        	_visit_date = visit_date;
        	_visit_hour = visit_hour;
        	_visit_minute = visit_minute;
        	_disaster_place = disaster_place;
        	_disaster_case = disaster_case;
        	_hospital = hospital;
        	_remark2 = remark2;
        	_outcome = outcome;
        	_remark3 = remark3;
        	_remark4 = remark4;
        	_remark5 = remark5;
        	_remark6 = remark6;
        	_remark7 = remark7;
        	_remark8 = remark8;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if (null == _attendno) return "  番";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69380 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _sDate;
        final String _eDate;
        private boolean _isSeireki;
        private final String _nendo;
        final String _semesterName;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _sDate = request.getParameter("SDATE").replace('/', '-');
            _eDate = request.getParameter("EDATE").replace('/', '-');

            setSeirekiFlg(db2);
            _nendo = changePrintYear(db2, _ctrlYear, _ctrlDate);

            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "' "));

        }
        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintYear(final DB2UDB db2, final String year, final String date) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                final String[] gengou = KNJ_EditDate.tate_format4(db2, date);
                return gengou[0] + gengou[1] + "年度";
            }
        }
    }
}

// eof

