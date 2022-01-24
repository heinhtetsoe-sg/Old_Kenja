/*
 * $Id: b4a99957621cce9ab566d2603b29f4335e1dc3de $
 *
 * 作成日: 2018/11/14
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

public class KNJL322E {

    private static final Log log = LogFactory.getLog(KNJL322E.class);

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

            printMain(db2, svf);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL322E.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 30;
        int printLine = 1;
        int recordcnt = 1;

        String aftCd = "";
        String befCd = "";
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            aftCd = printData._fsCd;
            if (printLine > maxLine || (!"".equals(befCd) && !befCd.equals(aftCd))) {
                svf.VrEndPage();
                printLine = 1;
                if (!"".equals(befCd) && !befCd.equals(aftCd)) {
                	recordcnt = 1;
                }
            }
            if (printLine == 1) {
                setTitle(db2, svf, printData._fsCd);//ヘッダ
                //中学校
                svf.VrsOut("FINSCHOOL_NAME" , printData._finSchoolNameAbbv);
            }

            //データ
            //NO
            svf.VrsOutn("NO", printLine, String.valueOf(recordcnt));
            //受験番号
            svf.VrsOutn("EXAM_NO" , printLine, printData._examNo);
            //氏名
            svf.VrsOutn("NAME1" , printLine, printData._name);
            //フリガナ
            svf.VrsOutn("KANA" , printLine, printData._nameKana);
            //合否
            svf.VrsOutn("JUDGE" , printLine, printData._judge);
            //合格区分
            svf.VrsOutn("DIV" , printLine, printData._passType);
            //合格コース
            svf.VrsOutn("COURSE", printLine, printData._passCourse);
            //出願専攻
            svf.VrsOutn("MAJOR", printLine, printData._application);

            befCd = aftCd;
            printLine++;
            recordcnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String fsCd) {
        String setYear = KNJ_EditDate.getAutoFormatYearNen(db2, _param._entExamYear + "/04/01");
        final String printDateTime = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" + "　" + _param._testdivName + "　中学校長宛合否通知票");
        List tcwk = (List)_param._totalCnt.get(fsCd);
        final String outputwk1 = "合格者:" + paddingBlank((String)tcwk.get(0), 3);
        final String outputwk2 = "不合格者:" + paddingBlank((String)tcwk.get(1), 3);
        final String outputwk3 = "欠席者:" + paddingBlank((String)tcwk.get(2), 3);
        svf.VrsOut("NUM", outputwk1 + " " + outputwk2 + " " + outputwk3);
    }

    private String paddingBlank(final String basestr, final int maxlen) {
    	String retstr = basestr;
    	retstr = (retstr.length() > maxlen - 1 ? "" : StringUtils.repeat(" ", maxlen - retstr.length())) + retstr;
    	return retstr;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String examNo = rs.getString("EXAMNO");
            	final String name = rs.getString("NAME");
            	final String nameKana = rs.getString("NAME_KANA");
            	final String fsCd = rs.getString("FS_CD");
            	final String finSchoolName = rs.getString("FINSCHOOL_NAME");
            	final String finSchoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
            	final String judgement = rs.getString("JUDGEMENT");
            	final String judge = rs.getString("JUDGE");
            	final String passType = rs.getString("PASSTYPE");
            	final String passCourse = rs.getString("PASSCOURSE");
            	final String application = rs.getString("APPLICATION");

                final PrintData printData = new PrintData(examNo, name, nameKana, fsCd, finSchoolName, finSchoolNameAbbv, judgement, judge, passType, passCourse, application);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  T1.NAME, ");
        stb.append("  T1.NAME_KANA, ");
        stb.append("  T1.FS_CD, ");
        stb.append("  FM1.FINSCHOOL_NAME, ");
        stb.append("  FM1.FINSCHOOL_NAME_ABBV, ");
        stb.append("  T1.JUDGEMENT, ");
        stb.append("  L013.NAME1 AS JUDGE, ");
        stb.append("  L045.NAME1 AS PASSTYPE, ");
        stb.append("  L058.NAME1 AS PASSCOURSE, ");
        stb.append("  CASE WHEN T2_030.REMARK6 = '1' THEN T2_033.REMARK1 WHEN T2_030.REMARK6 = '2' THEN T2_033.REMARK2 ELSE '' END AS APPLICATION ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2_030 ");
        stb.append("    ON T2_030.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2_030.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2_030.EXAMNO = T1.EXAMNO ");
        stb.append("   AND T2_030.SEQ = '030' ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2_033 ");
        stb.append("    ON T2_033.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2_033.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2_033.EXAMNO = T1.EXAMNO ");
        stb.append("   AND T2_033.SEQ = '030' ");
        stb.append("  LEFT JOIN FINSCHOOL_MST FM1 ");
        stb.append("    ON T1.FS_CD = FM1.FINSCHOOLCD ");
        stb.append("  LEFT JOIN NAME_MST L013 ");
        stb.append("    ON L013.NAMECD1 = 'L013' ");
        stb.append("   AND L013.NAMECD2 = T1.JUDGEMENT ");
        stb.append("  LEFT JOIN NAME_MST L045 ");
        stb.append("    ON L045.NAMECD1 = 'L045' ");
        stb.append("   AND L045.NAMECD2 = T2_030.REMARK4 ");
        stb.append("  LEFT JOIN NAME_MST L058 ");
        stb.append("    ON L058.NAMECD1 = 'L058' ");
        stb.append("   AND L058.NAMECD2 = T2_030.REMARK5 ");
        stb.append("  LEFT JOIN NAME_MST L004 ");
        stb.append("    ON L004.NAMECD1 = 'L004' ");
        stb.append("   AND L004.NAMECD2 = T1.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("  AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("1".equals(_param._outputtype)) {
            stb.append(" AND L004.NAMESPARE1 = '1' ");
        }
        stb.append(" ORDER BY ");
        stb.append("  T1.FS_CD,");
        stb.append("  T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
    	final String _examNo;
    	final String _name;
    	final String _nameKana;
    	final String _fsCd;
    	final String _finSchoolName;
    	final String _finSchoolNameAbbv;
    	final String _judgement;
    	final String _judge;
    	final String _passType;
    	final String _passCourse;
    	final String _application;

        public PrintData(
        		final String examNo,
        		final String name,
        		final String nameKana,
        		final String fsCd,
        		final String finSchoolName,
        		final String finSchoolNameAbbv,
        		final String judgement,
        		final String judge,
        		final String passType,
        		final String passCourse,
        		final String application
        ) {
            _examNo = examNo;
        	_name = name;
            _nameKana = nameKana;
            _fsCd = fsCd;
            _finSchoolName = finSchoolName;
            _finSchoolNameAbbv = finSchoolNameAbbv;
            _judgement = judgement;
            _judge = judge;
            _passType = passType;
            _passCourse = passCourse;
            _application = application;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63399 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _outputtype;
        private final String _entExamYear;
        private final String _testdivName;
        private final Map _totalCnt;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _outputtype       = request.getParameter("OUTPUT_TYPE");
            _testdivName = "1".equals(_outputtype) ? "推薦" : "全て";
            _totalCnt = getTotalCnt(db2);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        private Map getTotalCnt(final DB2UDB db2) {
        	Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getTotalCntSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	List addwkList = new ArrayList();
                	addwkList.add(rs.getString("PASS"));
                	addwkList.add(rs.getString("FAIL"));
                	addwkList.add(rs.getString("ABSENT"));
                	retMap.put(rs.getString("FS_CD"), addwkList);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        	return retMap;
        }
        private String getTotalCntSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("  T1.FS_CD, ");
            stb.append("  SUM(CASE WHEN T1.JUDGEMENT = '1' THEN 1 ELSE 0 END) AS PASS, ");
            stb.append("  SUM(CASE WHEN T1.JUDGEMENT = '2' THEN 1 ELSE 0 END) AS FAIL, ");
            stb.append("  SUM(CASE WHEN T1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS ABSENT ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("  LEFT JOIN NAME_MST L004 ");
            stb.append("    ON L004.NAMECD1 = 'L004' ");
            stb.append("   AND L004.NAMECD2 = T1.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("  T1.ENTEXAMYEAR = '" + _entExamYear + "' ");
            stb.append("  AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            if ("1".equals(_outputtype)) {
                stb.append(" AND L004.NAMESPARE1 = '1' ");
            }
            stb.append(" GROUP BY ");
            stb.append("  T1.FS_CD ");

            return stb.toString();
        }
    }
}

// eof
