/*
 * $Id: 22d4a7c0102f324ded8f92ef4cc53f1906ccfb16 $
 *
 * 作成日: 2017/04/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.List;



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
//import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJL327Q {

    private static final Log log = LogFactory.getLog(KNJL327Q.class);

    private boolean _hasData;
    private final String PRINT_PASS = "1";
    private final String PRINT_UNPASS = "2";
    private final String PRINT_SCHOLAR = "3";
    private final String PRINT_UNSCHOLAR = "4";

    private final String SCHOLAR_TOKUBETU = "1";
    private final String SCHOLAR_IPPAN = "2";

    private final String JUDGE_PASS = "1";
    private final String JUDGE_UNPASS = "2";

    private final String JIKOSUISEN = "4";

    private final String KAIGAI = "1";
    private final String SUISEN = "2";
    private final String IPPAN = "3";

    private final String SITEI = "2";

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

            if (PRINT_PASS.equals(_param._taisyou)) {
                printPass(db2, svf);
            }
            if (PRINT_UNPASS.equals(_param._taisyou)) {
                printUnPass(db2, svf);
            }
            if (PRINT_SCHOLAR.equals(_param._taisyou)) {
                printScholar(db2, svf);
            }
            if (PRINT_UNSCHOLAR.equals(_param._taisyou)) {
                printUnScholar(db2, svf);
            }
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

    private void printPass(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL327Q_1.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlPass("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String majorName = StringUtils.defaultString(rs.getString("MAJORNAME")) + " " + StringUtils.defaultString(rs.getString("EXAMCOURSE_NAME"));
//                final String text = "本校" + majorName + "に合格したことを通知いたします";
                final String text1 = "本校" + majorName;
                final String text2 = "に合格したことを通知いたします";

                if (getMS932ByteLength(majorName) > 20) {
                    svf.VrsOut("COURSE_NAME2", majorName);
                } else {
                    svf.VrsOut("COURSE_NAME", majorName);
                }
                svf.VrsOut("EXAM_NO", examno);
                final String nameField = getMS932ByteLength(name) > 22 ? "3" : getMS932ByteLength(name) > 16 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
//                final List tokenList = KNJ_EditKinsoku.getTokenList(text, 42);
//                for (int i = 0; i < tokenList.size(); i++) {
//                    svf.VrsOut("FIELD" + String.valueOf(i + 1), (String) tokenList.get(i));
//                }
                svf.VrsOut("FIELD1", text1);
                svf.VrsOut("FIELD2", text2);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolSchoolName);
                svf.VrsOut("STAFF_NAME", _param._cerifSchoolJobName + _param._cerifSchoolPrincipalName);
                if (null != _param._schoollogoStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);
                }
                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlPass(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.TESTDIV, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     EXCOURSE.EXAMCOURSE_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        if ("1".equals(_param._jizen)) {
            stb.append("     LEFT JOIN MAJOR_MST MAJOR ");
            stb.append("           ON VBASE.DAI1_COURSECD = MAJOR.COURSECD ");
            stb.append("          AND VBASE.DAI1_MAJORCD = MAJOR.MAJORCD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST EXCOURSE ");
            stb.append("           ON VBASE.ENTEXAMYEAR = EXCOURSE.ENTEXAMYEAR ");
            stb.append("          AND VBASE.APPLICANTDIV = EXCOURSE.APPLICANTDIV ");
            stb.append("          AND VBASE.TESTDIV = EXCOURSE.TESTDIV ");
            stb.append("          AND VBASE.DAI1_COURSECD = EXCOURSE.COURSECD ");
            stb.append("          AND VBASE.DAI1_MAJORCD = EXCOURSE.MAJORCD ");
            stb.append("          AND VBASE.DAI1_COURSECODE = EXCOURSE.EXAMCOURSECD ");
        } else {
            stb.append("     LEFT JOIN MAJOR_MST MAJOR ");
            stb.append("           ON VBASE.SUC_COURSECD = MAJOR.COURSECD ");
            stb.append("          AND VBASE.SUC_MAJORCD = MAJOR.MAJORCD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST EXCOURSE ");
            stb.append("           ON VBASE.ENTEXAMYEAR = EXCOURSE.ENTEXAMYEAR ");
            stb.append("          AND VBASE.APPLICANTDIV = EXCOURSE.APPLICANTDIV ");
            stb.append("          AND VBASE.TESTDIV = EXCOURSE.TESTDIV ");
            stb.append("          AND VBASE.SUC_COURSECD = EXCOURSE.COURSECD ");
            stb.append("          AND VBASE.SUC_MAJORCD = EXCOURSE.MAJORCD ");
            stb.append("          AND VBASE.SUC_COURSECODE = EXCOURSE.EXAMCOURSECD ");
        }
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV0 = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        if (!"1".equals(_param._jizen)) {
            stb.append("     AND VBASE.JUDGEMENT = '" + JUDGE_PASS + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void printUnPass(final DB2UDB db2, final Vrw32alp svf) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlUnPass("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String testDiv0 = rs.getString("TESTDIV0");
                final String courseName = rs.getString("EXAMCOURSE_NAME");
                final String testDate1 = rs.getString("TESTDATE1");
                final String testDate2 = rs.getString("TESTDATE2");

                if (SUISEN.equals(testDiv0)) {
                    svf.VrSetForm("KNJL327Q_2_1.frm", 1);
                } else {
                    svf.VrSetForm("KNJL327Q_2_2.frm", 1);
                }
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolSchoolName);
                svf.VrsOut("STAFF_NAME", _param._cerifSchoolJobName + _param._cerifSchoolPrincipalName);
                if (null != _param._schoollogoStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);
                }
                svf.VrsOut("COURSE_NAME", courseName);
                svf.VrsOut("JUDGE", "不合格");
                if (SUISEN.equals(testDiv0)) {
                    svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_S(testDate2, "M月d日"));
                } else {
                    svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_S(testDate1, "M月d日"));
                }
                svf.VrsOut("LIMIT_DATE", KNJ_EditDate.h_format_S(_param._teishutsuDate, "M月d日"));
                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlUnPass(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.TESTDIV0, ");
        stb.append("     L004.NAMESPARE1 AS TESTDATE1, ");
        stb.append("     L004_2.NAMESPARE1 AS TESTDATE2, ");
        stb.append("     COURSE.EXAMCOURSE_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ");
        stb.append("          AND VBASE.TESTDIV = L004.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L004_2 ON L004_2.NAMECD1 = 'L004' ");
        stb.append("          AND L004_2.NAMECD2 = '5' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON VBASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE.EXAMCOURSECD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV0 = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append("     AND VBASE.JUDGEMENT = '" + JUDGE_UNPASS + "' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void printScholar(final DB2UDB db2, final Vrw32alp svf) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlScholar("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String testDiv0 = rs.getString("TESTDIV0");
                final String testDate2 = rs.getString("TESTDATE2");
                final String majorname = rs.getString("MAJORNAME");
                final String hope = rs.getString("SCHOLAR_KIBOU");
                final String toukyu1 = rs.getString("TOUKYU1");
                final String toukyu2 = rs.getString("TOUKYU2");

                if (KAIGAI.equals(testDiv0)) {
                    svf.VrSetForm("KNJL327Q_3_1.frm", 1);
                } else if (SUISEN.equals(testDiv0)) {
                    svf.VrSetForm("KNJL327Q_3_2.frm", 1);
                } else {
                    svf.VrSetForm("KNJL327Q_3_3.frm", 1);
                }
                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolSchoolName);
                svf.VrsOut("STAFF_NAME", _param._cerifSchoolJobName + _param._cerifSchoolPrincipalName);
                svf.VrsOut("COURSE_NAME", majorname);
                svf.VrsOut("JUDGE", "不合格");
                if (SUISEN.equals(testDiv0)) {
                    svf.VrsOut("TEST_DATE", KNJ_EditDate.h_format_S(testDate2, "M月d日"));
                } else {
                    svf.VrsOut("TEST_DATE", KNJ_EditDate.h_format_S(testDate2, "M月d日") + "(" + KNJ_EditDate.h_format_W(testDate2) + ")");
                }
                if (null != _param._schoollogoFilePath) {
                    svf.VrsOut("SCHOOL_LOGO", _param._schoollogoFilePath);
                }
                if (null != _param._schoollogoStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);
                }
                svf.VrsOut("HOPE", hope + "奨学生");
                svf.VrsOut("RANK1", toukyu1);
                svf.VrsOut("RANK2", toukyu2);
                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlScholar(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.TESTDIV0, ");
        stb.append("     L004_2.NAMESPARE1 AS TESTDATE2, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     COURSE.EXAMCOURSE_NAME, ");
        stb.append("     CASE WHEN VBASE.SCHOLAR_KIBOU = '" + SCHOLAR_TOKUBETU + "' THEN '特別' ");
        stb.append("          WHEN VBASE.SCHOLAR_KIBOU = '" + SCHOLAR_IPPAN + "' THEN '一般' ");
        stb.append("          ELSE '無' ");
        stb.append("     END AS SCHOLAR_KIBOU, ");
        stb.append("     L025.NAME1 AS TOUKYU1, ");
        stb.append("     L025_2.NAME1 AS TOUKYU2 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST L025 ON L025.NAMECD1 = 'L025' ");
        stb.append("          AND VBASE.SCHOLAR_TOUKYU_SENGAN = L025.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L025_2 ON L025_2.NAMECD1 = 'L025' ");
        stb.append("          AND VBASE.SCHOLAR_TOUKYU_HEIGAN = L025_2.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L004_2 ON L004_2.NAMECD1 = 'L004' ");
        stb.append("          AND L004_2.NAMECD2 = '5' ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON VBASE.DAI1_COURSECD = MAJOR.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON VBASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE.EXAMCOURSECD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV0 = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append("     AND VBASE.JUDGEMENT = '" + JUDGE_PASS + "' ");
        stb.append("     AND VBASE.SCHOLAR_KIBOU IN ('1', '2') ");
        stb.append("     AND VBASE.SCHOLAR_SAIYOU = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void printUnScholar(final DB2UDB db2, final Vrw32alp svf) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlUnScholar("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String majorname = rs.getString("MAJORNAME");

                svf.VrSetForm("KNJL327Q_4.frm", 1);

                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolSchoolName);
                svf.VrsOut("STAFF_NAME", _param._cerifSchoolJobName + _param._cerifSchoolPrincipalName);
                if (null != _param._schoollogoStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);
                }
                svf.VrsOut("COURSE_NAME", majorname);
                svf.VrsOut("JUDGE", "不採用");
                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlUnScholar(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     COURSE.EXAMCOURSE_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON VBASE.DAI1_COURSECD = MAJOR.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON VBASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE.EXAMCOURSECD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV0 = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append("     AND VBASE.JUDGEMENT = '" + JUDGE_PASS + "' ");
        stb.append("     AND VBASE.SCHOLAR_KIBOU IN ('1', '2') ");
        stb.append("     AND VALUE(VBASE.SCHOLAR_SAIYOU, '') <> '1' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70972 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testDiv;
        final String _noticeDate;
        final String _taisyou;
        final String _jizen;
        final String _teishutsuDate;
        final String _output;
        final String _examno;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        private String _cerifSchoolSchoolName;
        private String _cerifSchoolJobName;
        private String _cerifSchoolPrincipalName;
        final String _documentroot;
        final String _imagepath;
        final String _schoollogoFilePath;
        final String _schoollogoStampFilePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _noticeDate = request.getParameter("NOTICEDATE");
            _taisyou = request.getParameter("TAISYOU");
            _jizen = request.getParameter("JIZEN");
            _teishutsuDate = request.getParameter("TEISHUTSUDATE");
            _output = request.getParameter("OUTPUT");
            _examno = request.getParameter("EXAMNO");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoollogoFilePath = getImageFilePath("SCHOOLBADGE.jpg");
            _schoollogoStampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");
            setCertifSchoolDat(db2);
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _cerifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _cerifSchoolJobName = rs.getString("JOB_NAME");
                    _cerifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _cerifSchoolSchoolName = StringUtils.defaultString(_cerifSchoolSchoolName);
            _cerifSchoolJobName = StringUtils.defaultString(_cerifSchoolJobName, "校長");
            _cerifSchoolPrincipalName = StringUtils.defaultString(_cerifSchoolPrincipalName);
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

    }
}

// eof

