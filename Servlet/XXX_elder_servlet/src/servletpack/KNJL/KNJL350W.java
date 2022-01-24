/*
 * $Id: be4539fc56d18faf0ba3fb6b5ab96243c2510270 $
 *
 * 作成日: 2017/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL350W {

    private static final Log log = LogFactory.getLog(KNJL350W.class);

    private boolean _hasData;
    private final String NYUUGAKUTEIIN = "98";
    private final String BOSYUNINZU = "99";
    private final String GOUKEI = "9";

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
        DB2UDB db2IinKai = null;
        try {
	        response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            db2IinKai = new DB2UDB(request.getParameter("DBNAME2"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2IinKai.open();

            _param = createParam(db2, db2IinKai, request);

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

            if (null != db2IinKai) {
                db2IinKai.commit();
                db2IinKai.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL350W.frm", 4);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            setTitle(svf);

            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            String befCourseCd = "";
            for (Iterator itCmc = _param._cmcList.iterator(); itCmc.hasNext();) {
                final CMCdata cmcData = (CMCdata) itCmc.next();
                if (!"".equals(befCourseCd) && befCourseCd.equals(cmcData._courseCd)) {
                    svf.VrEndPage();
                    setTitle(svf);
                }
                if (!GOUKEI.equals(cmcData._courseCd)) {
                    svf.VrsOut("COURSE_NAME", cmcData._courseName);
                }
                final String cmcName = cmcData._cmcName;
                final String setField = KNJ_EditEdit.getMS932ByteLength(cmcName) > 20 ? "2" : "1";
                svf.VrsOut("MAJOR_NAME" + setField, cmcName);
                int numCnt = 1;
                for (Iterator itTest = _param._outputTitleList.iterator(); itTest.hasNext();) {
                    final TitleData titleData = (TitleData) itTest.next();
                    ps.setString(1, cmcData._courseCd);
                    ps.setString(2, cmcData._majorCd);
                    ps.setString(3, cmcData._courseCode);
                    ps.setString(4, titleData._largeDiv);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        svf.VrsOut("NUM" + numCnt, StringUtils.isNotEmpty(rs.getString("COUNT")) ? rs.getString("COUNT") : "");
                        numCnt++;
                    }
                    _hasData = true;
                }
                befCourseCd = cmcData._courseCd;
                svf.VrEndRecord();
            }


            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        String titleField = KNJ_EditEdit.getMS932ByteLength(_param._testName) > 30 ? "60" : KNJ_EditEdit.getMS932ByteLength(_param._testName) > 20 ? "30" : KNJ_EditEdit.getMS932ByteLength(_param._testName) > 10 ? "20" : "10";
        svf.VrAttribute("TITLE" + titleField, "PAINT=(0,0,1)");
        svf.VrsOut("TITLE" + titleField, _param._testName);
        svf.VrsOut("DIV", _param._ruikeiName);
        svf.VrsOut("REPORT_DATE", _param._houkokuData);
        int gyo = 1;
        for (Iterator itTest = _param._outputTitleList.iterator(); itTest.hasNext();) {
            final TitleData titleData = (TitleData) itTest.next();
            final String largeName = titleData._largeName;
            if (NYUUGAKUTEIIN.equals(titleData._largeDiv) || BOSYUNINZU.equals(titleData._largeDiv)) {
                continue;
            }
            final String setField = KNJ_EditEdit.getMS932ByteLength(largeName) > 18 ? "2" : "1";
            svf.VrsOutn("NUM_NAME" + setField, gyo, largeName);
            gyo++;
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     AFT_SINRO_ADDITION_L350W_DAT ");
        stb.append(" WHERE ");
        stb.append("     EDBOARD_SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND RUIKEI_DIV = '" + _param._ruikeiDiv + "' ");
        stb.append("     AND TESTDIV2 = '" + _param._testdiv2 + "' ");
        stb.append("     AND COURSECD = ? ");
        stb.append("     AND MAJORCD = ? ");
        stb.append("     AND COURSECODE = ? ");
        stb.append("     AND LARGE_DIV = ? ");
        stb.append("     AND SEX = '9' ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final DB2UDB db2IinKai, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, db2IinKai, request);
        log.fatal("$Revision: 70535 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class TitleData {
        final String _year;
        final String _applicantdiv;
        final String _testdiv;
        final String _ruikeiDiv;
        final String _testdiv2;
        final String _largeDiv;
        final String _largeName;
        public TitleData(
                final String year,
                final String applicantdiv,
                final String testdiv,
                final String ruikeiDiv,
                final String testdiv2,
                final String largeDiv,
                final String largeName
        ) {
            _year = year;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _ruikeiDiv = ruikeiDiv;
            _testdiv2 = testdiv2;
            _largeDiv = largeDiv;
            _largeName = largeName;
        }
    }

    private class CMCdata {
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _courseCode;
        final String _cmcName;
        public CMCdata(
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String courseCode,
                final String cmcName
        ) {
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _cmcName = cmcName;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _applicantdiv;
        private final String _testdiv;
        private final String _testdiv2;
        private final String _ruikeiDiv;
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _ctrlSemester;
        private final String _schoolcd;
        private final String _prgid;
        private final List _outputTitleList;
        private final String _schoolName;
        private final String _testName;
        private final String _ruikeiName;
        private final String _houkokuData;
        private final List _cmcList;

        Param(final DB2UDB db2, final DB2UDB db2IinKai, final HttpServletRequest request) throws SQLException, ParseException {
            _year = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _testdiv2 = StringUtils.isNotEmpty(request.getParameter("TESTDIV2")) ? request.getParameter("TESTDIV2") : "0";
            _ruikeiDiv = request.getParameter("RUIKEI_DIV");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schoolcd = request.getParameter("SCHOOLCD");
            _prgid = request.getParameter("PRGID");
            _outputTitleList = getTitleList(db2IinKai);
            _schoolName = getSchoolName(db2);
            _testName = getNameMst(db2, "L004", _testdiv, "NAME2");
            _ruikeiName = getNameMst(db2, "L040", _ruikeiDiv, "NAME1");
            _houkokuData = getHoukokuData(db2IinKai);
            _cmcList = getCMCList(db2);
        }

        private List getTitleList(final DB2UDB db2IinKai) throws SQLException {
            final List retList = new ArrayList();
            final String titleSql = getTitleSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2IinKai.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String applicantdiv = rs.getString("APPLICANTDIV");
                    final String testdiv = rs.getString("TESTDIV");
                    final String ruikeiDiv = rs.getString("RUIKEI_DIV");
                    final String testdiv2 = rs.getString("TESTDIV2");
                    final String largeDiv = rs.getString("LARGE_DIV");
                    final String largeName = rs.getString("LARGE_NAME");
                    final TitleData titleData = new TitleData(year, applicantdiv, testdiv, ruikeiDiv, testdiv2, largeDiv, largeName);
                    retList.add(titleData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getTitleSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     AFT_SINRO_ADDITION_L350W_FIELD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("     AND TESTDIV = '" + _testdiv + "' ");
            stb.append("     AND RUIKEI_DIV = '" + _ruikeiDiv + "' ");
            stb.append("     AND TESTDIV2 = '" + _testdiv2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CASE WHEN LARGE_DIV IN ('98', '99') THEN 1 ELSE 2 END, ");
            stb.append("     LARGE_DIV ");

            return stb.toString();
        }

        private String getSchoolName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final String titleSql = getSchoolSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("SCHOOLNAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOLNAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");

            return stb.toString();
        }

        private List getCMCList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String titleSql = getCMCsql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseCd = rs.getString("COURSECD");
                    final String courseName = rs.getString("COURSENAME");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String cmcName = rs.getString("CMC_NAME");
                    final CMCdata cmcData = new CMCdata(courseCd, courseName, majorCd, courseCode, cmcName);
                    retList.add(cmcData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getCMCsql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH TOTAL_T (COURSECD, COURSENAME, MAJORCD, COURSECODE, CMC_NAME) AS ( ");
            stb.append("     VALUES('9', '', '999', '9999', '合計') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     W1.COURSECD , ");
            stb.append("     L2.COURSENAME, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.EXAMCOURSECD AS COURSECODE, ");
            stb.append("     T1.MAJORNAME || W1.EXAMCOURSE_NAME AS CMC_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST W1 ");
            stb.append("     LEFT JOIN V_MAJOR_MST T1 ON W1.ENTEXAMYEAR = T1.YEAR ");
            stb.append("         AND W1.COURSECD = T1.COURSECD ");
            stb.append("         AND W1.MAJORCD = T1.MAJORCD ");
            stb.append("     LEFT JOIN COURSE_MST L2 ON W1.COURSECD = L2.COURSECD ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR = '" + _year + "' ");
            stb.append("     AND W1.APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("     AND W1.TESTDIV = '" + _testdiv + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     COURSECD , ");
            stb.append("     COURSENAME , ");
            stb.append("     MAJORCD, ");
            stb.append("     COURSECODE, ");
            stb.append("     CMC_NAME ");
            stb.append(" FROM ");
            stb.append("     TOTAL_T ");
            stb.append(" ORDER BY ");
            stb.append("     COURSECD , ");
            stb.append("     MAJORCD, ");
            stb.append("     COURSECODE ");

            return stb.toString();
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String getFieldName) throws SQLException {
            String retStr = "";
            final String titleSql = getNameMstSql(namecd1, namecd2, getFieldName);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString(getFieldName);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2, final String getFieldName) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + getFieldName + " ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");

            return stb.toString();
        }

        private String getHoukokuData(final DB2UDB db2IinKai) throws SQLException, ParseException {
            String retStr = "1".equals(_testdiv2) ? "追検査" : "";
            final String titleSql = getHoukokuSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2IinKai.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String executeDate = rs.getString("EXECUTE_DATE");
                    final String label = rs.getString("LABEL");
                    if (StringUtils.isNotEmpty(executeDate)) {
                        final String[] dateArray = StringUtils.split(executeDate, ".");
                        final String[] dateTime = StringUtils.split(dateArray[0], " ");
                        final String houkokuDate = KNJ_EditDate.h_format_JP(dateTime[0]);
                        final String houkokuWeek = " (" + KNJ_EditDate.h_format_W(dateTime[0]) + ") ";
                        DateFormat sdf1 = new SimpleDateFormat("HH時mm分");
                        final String formatStr = sdf1.format(java.sql.Timestamp.valueOf(executeDate));
                        retStr = houkokuDate + houkokuWeek + " " + formatStr + " " + label;
                    } else {
                        retStr = label;
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getHoukokuSql() throws SQLException {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     T1.TESTDIV2, ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     MAX(T1.FIXED_FLG) AS FIXED_FLG, ");
            stb.append("     CASE WHEN T1.TESTDIV2 = '1' THEN '追検査' ELSE '' END AS LABEL ");
            stb.append(" FROM ");
            stb.append("     REPORT_AFT_SINRO_ADDITION_L350W_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L004' ");
            stb.append("          AND L2.NAMECD2 = T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'L040' ");
            stb.append("          AND L3.NAMECD2 = T1.RUIKEI_DIV ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ON T1.REGISTERCD = STAFF.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + _schoolcd + "' ");
            stb.append("     AND T1.YEAR             = '" + _year + "' ");
            stb.append("     AND T1.APPLICANTDIV     = '" + _applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV          = '" + _testdiv + "' ");
            stb.append("     AND T1.RUIKEI_DIV       = '" + _ruikeiDiv + "' ");
            stb.append("     AND T1.TESTDIV2         = '" + _testdiv2 + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     T1.TESTDIV2, ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     CASE WHEN T1.TESTDIV2 = '1' THEN '追検査' ELSE '' END ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXECUTE_DATE DESC, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     T1.TESTDIV2 ");
            stb.append(" FETCH FIRST 1 ROWS ONLY ");

            return stb.toString();
        }

    }
}

// eof

