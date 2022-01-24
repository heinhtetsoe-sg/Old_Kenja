/*
 * $Id: 611ee337bd75689b5205b7b26c7a28f71547232f $
 *
 * 作成日: 2016/12/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJA340 {

    private static final Log log = LogFactory.getLog(KNJA340.class);

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
        svf.VrSetForm("KNJA340.frm", 1);
        String befcol = "";
        int idx = 1;
        int maxline = 40;

        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData) it.next();
            if (!befcol.equals(data._colcode1) && _hasData) {
                svf.VrEndPage();
                idx = 1;
            } else if (idx > maxline) {
                svf.VrEndPage();
                idx = 1;
            }
            if (idx == 1) {
                //ヘッダ
                svf.VrsOut("TITLE", "生徒自由項目の出力(" + data._col1 + ")");
            }
            //明細
            //項目1
            svf.VrsOutn("ITEM1", idx, data._col1);
            //項目2
            svf.VrsOutn("ITEM2", idx, data._col2);
            //備考
            svf.VrsOutn("REMARK", idx, data._remark);
            //学年
            svf.VrsOutn("GRADE", idx, data._grade_name1);
            //クラス
            svf.VrsOutn("HR_NAME", idx, data._hr_name);
            //番号
            svf.VrsOutn("NO", idx, data.getAttendNo());
            //氏名
            String nameField = KNJ_EditEdit.getMS932ByteLength(data._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(data._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, idx, data._name);
            //性別
            svf.VrsOutn("SEX", idx, data._sex);
            //郵便番号
            svf.VrsOutn("ZIPNO", idx, data._zipcd);
            //住所
            String addr = data._addr1;
            if(!"".equals(data._addr2)) addr = addr + data._addr2;
            String addrField = KNJ_EditEdit.getMS932ByteLength(addr) > 50 ? "2" : "1";
            svf.VrsOutn("ADDR" + addrField, idx, addr);
            //電話番号
            svf.VrsOutn("TELNO", idx, data._telno);

            _hasData = true;
            befcol = data._colcode1;
            idx++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
        return;
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
                final String colcode1 = StringUtils.defaultString(rs.getString("COLCODE1"));
                final String col1 = StringUtils.defaultString(rs.getString("COL1"));
                final String colcode2 = StringUtils.defaultString(rs.getString("COLCODE2"));
                final String col2 = StringUtils.defaultString(rs.getString("COL2"));
                final String remark = StringUtils.defaultString(rs.getString("remark"));
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String grade_name1 = StringUtils.defaultString(rs.getString("GRADE_NAME1"));
                final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                final String hr_name = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String sex = StringUtils.defaultString(rs.getString("SEX"));
                final String zipcd = StringUtils.defaultString(rs.getString("ZIPCD"));
                final String addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                final String addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                final String telno = StringUtils.defaultString(rs.getString("TELNO"));
                final PrintData printData = new PrintData(colcode1, col1, colcode2, col2, remark, grade, grade_name1, hr_class, hr_name, attendno, schregno, name, sex, zipcd, addr1, addr2, telno);
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
        stb.append(" WITH BASE AS( ");
        stb.append("   SELECT ");
        stb.append("     T1.CODE, ");
        stb.append("     T1.NAME AS CODE_NAME, ");
        stb.append("     T2.SEQ, ");
        stb.append("     T2.NAME AS SEQ_NAME ");
        stb.append("   FROM ");
        stb.append("     BASE_REMARK_MST T1 ");
        stb.append("     LEFT JOIN BASE_REMARK_DETAIL_MST T2 ");
        stb.append("            ON T2.CODE = T1.CODE ");
        stb.append(" ), BASE_REMARK AS ( ");
        stb.append(" SELECT ");
        stb.append("   T2.YEAR, ");
        stb.append("   T2.SCHREGNO, ");
        stb.append("   T1.CODE, ");
        stb.append("   T1.CODE_NAME, ");
        stb.append("   T1.SEQ, ");
        stb.append("   T1.SEQ_NAME, ");
        stb.append("   T2.REMARK ");
        stb.append(" FROM ");
        stb.append("   BASE T1 ");
        stb.append("   LEFT JOIN SCHREG_BASE_REMARK_DAT T2 ");
        stb.append("          ON T2.CODE     = T1.CODE ");
        stb.append("         AND T2.SEQ      = T1.SEQ  ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        // 項目1
        stb.append("   BASE.CODE AS COLCODE1, ");
        stb.append("   BASE.CODE_NAME AS COL1, ");
        // 項目2
        stb.append("   BASE.SEQ AS COLCODE2, ");
        stb.append("   BASE.SEQ_NAME AS COL2, ");
        // 備考
        stb.append("   BASE.REMARK, ");
        // 生徒情報
        stb.append("   T1.GRADE, ");
        stb.append("   T4.GRADE_NAME1, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T3.HR_NAME, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.NAME, ");
        stb.append("   Z002.ABBV1 AS SEX, ");
        stb.append("   T6.ZIPCD, ");
        stb.append("   T6.ADDR1, ");
        stb.append("   T6.ADDR2, ");
        stb.append("   T6.TELNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN BASE_REMARK BASE ");
        stb.append("           ON BASE.YEAR     = T1.YEAR ");
        stb.append("          AND BASE.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("           ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("           ON T3.YEAR     = T1.YEAR ");
        stb.append("          AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("          AND T3.GRADE    = T1.GRADE ");
        stb.append("          AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("   INNER JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("           ON T4.YEAR     = T1.YEAR ");
        stb.append("          AND T4.GRADE    = T1.GRADE ");
        stb.append("   LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) T5 ");
        stb.append("          ON T5.SCHREGNO  = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_ADDRESS_DAT T6 ");
        stb.append("          ON T6.SCHREGNO  = T5.SCHREGNO ");
        stb.append("         AND T6.ISSUEDATE = T5.ISSUEDATE ");
        stb.append("   LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = T2.SEX ");
        stb.append(" WHERE ");
        stb.append("       T1.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelectedIn) + " ");
        stb.append(" ORDER BY ");
        stb.append("   COLCODE1, ");
        stb.append("   COLCODE2 DESC, ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _colcode1;
        final String _col1;
        final String _colcode2;
        final String _col2;
        final String _remark;
        final String _grade;
        final String _grade_name1;
        final String _hr_class;
        final String _hr_name;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _sex;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;

        PrintData(final String colcode1,
                final String col1,
                final String colcode2,
                final String col2,
                final String remark,
                final String grade,
                final String grade_name1,
                final String hr_class,
                final String hr_name,
                final String attendno,
                final String schregno,
                final String name,
                final String sex,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno
        ) {
            _colcode1 = colcode1;
            _col1 = col1;
            _colcode2 = colcode2;
            _col2 = col2;
            _remark = remark;
            _grade = grade;
            _grade_name1 = grade_name1;
            _hr_class = hr_class;
            _hr_name = hr_name;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
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
        log.fatal("$Revision: 69345 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String[] _categorySelectedIn;
        private boolean _isSeireki;
        private final String _nendo;
        final String _schoolCd;
        final String _schoolKind;
        final String _grade;
        final String _semesterName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _categorySelectedIn = request.getParameterValues("CATEGORY_SELECTED");

            setSeirekiFlg(db2);
            _nendo = changePrintYear(db2, _ctrlYear, _ctrlDate);

            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _grade = request.getParameter("GRADE");

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

