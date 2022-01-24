/*
 * $Id$
 *
 * 作成日: 2016/11/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL357F {

    private static final Log log = LogFactory.getLog(KNJL357F.class);

    private static final String SCHOOL_J = "1";
    private static final String SCHOOL_H = "2";
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
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String form;
                if (SCHOOL_J.equals(_param._applicantdiv)) {
                    form = "KNJL357F_J.frm";
                } else {
                    if ("6".equals(testdiv)) {
                        form = "KNJL357F_H_2.frm";
                    } else {
                        form = "KNJL357F_H.frm";
                    }
                }
                svf.VrSetForm(form, 1);

                if (SCHOOL_J.equals(_param._applicantdiv)) {
                    svf.VrsOut("NENDO", _param._entexamyear + "年度　教科書・副教材等　引換券");
                    svf.VrsOut("EXAM_NO", rs.getString("RECEPTNO"));
                    svf.VrsOut("NAME", rs.getString("NAME"));
                } else {
                    if ("6".equals(testdiv)) {
                        svf.VrsOut("NENDO", _param._entexamyear + "年度");
                        final String name = StringUtils.defaultString(rs.getString("EXAMCOURSE_NAME"), "　　") + "　　" + StringUtils.defaultString(rs.getString("RECEPTNO"), "　") + "　　" + StringUtils.defaultString(rs.getString("NAME")) + "　さん";
                        final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 56 ? "2": "";
                        svf.VrsOut("NAME" + nameField, name);
                    } else {
                        svf.VrsOut("NENDO", _param._entexamyear + "年度　　　　　学用品等　　　　　引換券");
                        final String nameField = KNJ_EditEdit.getMS932ByteLength(rs.getString("EXAMCOURSE_NAME")) > 18 ? "2": "";
                        svf.VrsOut("COURSE_NAME" + nameField, rs.getString("EXAMCOURSE_NAME"));
                        svf.VrsOut("EXAM_NO", rs.getString("RECEPTNO"));
                        svf.VrsOut("NAME", rs.getString("NAME"));
                    }
                }
                svf.VrsOut("SCHOOL_NAME", _param._schoolName);
                if (null != _param._schoolStampPath) {
                    svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
                }
                _hasData = true;
                svf.VrEndPage();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     BASE.ENTEXAMYEAR, ");
        stb.append("     BASE.APPLICANTDIV, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.SUC_COURSECD, ");
        stb.append("     BASE.SUC_MAJORCD, ");
        stb.append("     BASE.SUC_COURSECODE, ");
        stb.append("     RECEPT.RECEPTNO AS SRC_RECEPTNO, ");
        stb.append("     V_BASE.TESTDIV ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        if ("2".equals(_param._printDiv)) {
            stb.append("     AND BASE.ENTDIV = '1' ");
        }
        stb.append("     INNER JOIN V_ENTEXAM_RECEPT_DAT RECEPT ON V_BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1'  ");
        stb.append("           AND V_BASE.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        if ("1".equals(_param._printDiv)) {
            stb.append("           AND RECEPT.PROCEDUREDIV1 = '1' ");
        }
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            stb.append("           AND VALUE(BASE.GENERAL_FLG, '0') != CASE WHEN RECEPT.TESTDIV = '5' THEN '1' ELSE '9' END ");
        } else {
            stb.append("           AND VALUE(BASE.GENERAL_FLG, '0') != CASE WHEN RECEPT.TESTDIV = '3' THEN '1' ELSE '9' END ");
        }
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (!"ALL".equals(_param._testdiv)) {
            stb.append("     AND V_BASE.TESTDIV = '" + _param._testdiv + "' ");
        }
        if (!"5".equals(_param._testdiv)) {
            stb.append(" ), RECEPT_MIN AS ( ");
            stb.append(" SELECT ");
            stb.append("     BASE_T.EXAMNO, ");
            stb.append("     MIN(RECEPT.RECEPTNO) AS RECEPTNO ");
            stb.append(" FROM ");
            stb.append("     BASE_T, ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE, ");
            stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
            stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     BASE_T.ENTEXAMYEAR = V_BASE.ENTEXAMYEAR ");
            stb.append("     AND BASE_T.APPLICANTDIV = V_BASE.APPLICANTDIV ");
            stb.append("     AND BASE_T.EXAMNO = V_BASE.EXAMNO ");
            stb.append("     AND BASE_T.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("     AND BASE_T.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("     AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
            stb.append("     AND BASE_T.EXAMNO = RECEPT.EXAMNO ");
            stb.append("     AND L013.NAMESPARE1 = '1' ");
            if ("2".equals(_param._passDiv)) {
                stb.append("     AND RECEPT.RECEPTNO = '" + _param._receptNo + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     BASE_T.EXAMNO ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE_T.EXAMNO, ");
        if (!"5".equals(_param._testdiv)) {
            stb.append("     RECEPT_MIN.RECEPTNO, ");
        } else {
            stb.append("     BASE_T.SRC_RECEPTNO AS RECEPTNO, ");
        }
        stb.append("     BASE_T.NAME, ");
        stb.append("     BASE_T.NAME_KANA, ");
        stb.append("     BASE_T.SUC_COURSECD, ");
        stb.append("     BASE_T.SUC_MAJORCD, ");
        stb.append("     BASE_T.SUC_COURSECODE, ");
        stb.append("     COURSE.EXAMCOURSE_NAME, ");
        stb.append("     BASE_T.TESTDIV ");
        stb.append(" FROM ");
        stb.append("     BASE_T ");
        if (!"5".equals(_param._testdiv)) {
            stb.append("     INNER JOIN RECEPT_MIN ON BASE_T.EXAMNO = RECEPT_MIN.EXAMNO ");
        }
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE_T.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND BASE_T.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND COURSE.TESTDIV = '1' ");
        stb.append("          AND BASE_T.SUC_COURSECD = COURSE.COURSECD ");
        stb.append("          AND BASE_T.SUC_MAJORCD = COURSE.MAJORCD ");
        stb.append("          AND BASE_T.SUC_COURSECODE = COURSE.EXAMCOURSECD ");
        stb.append(" ORDER BY ");
        stb.append("     BASE_T.TESTDIV, ");
        if (!"5".equals(_param._testdiv)) {
            stb.append("     RECEPT_MIN.RECEPTNO ");
        } else {
            stb.append("     BASE_T.SRC_RECEPTNO ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65463 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _testdiv;
        private final String _printDiv;
        private final String _passDiv;
        private final String _receptNo;
        private final String _entexamyear;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _printLogStaffcd;

        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";
        private String _schoolNamePath;
        private String _schoolStampPath;
        private String _schoolLogoPath;
        private String _imagePath;
        private String _documentRoot;
        private String _certifSchoolDatRemark1 = "";
        private String _certifSchoolDatRemark2 = "";
        private String _certifSchoolDatRemark4 = "";
        private String _certifSchoolDatRemark5 = "";
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _testdiv            = request.getParameter("TESTDIV");
            _printDiv           = request.getParameter("PRINTDIV");
            _passDiv            = request.getParameter("PASSDIV");
            _receptNo           = request.getParameter("RECEPTNO");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _loginYear          = request.getParameter("LOGIN_YEAR");
            _loginSemester      = request.getParameter("LOGIN_SEMESTER");
            _loginDate          = request.getParameter("LOGIN_DATE");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");

            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            final String jorh = "1".equals(_applicantdiv) ? "J" : "H";
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_" + jorh + ".bmp");
            _schoolLogoPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLLOGO_" + jorh + ".jpg");
            _schoolNamePath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLNAME_" + jorh + ".jpg");
            setCertifSchoolDat(db2);
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return null;
            }
            log.info("exists:" + path);
            return path;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if (SCHOOL_J.equals(_applicantdiv)) {
                certifKindCd = "105";
            } else {
                certifKindCd = "106";
            }

            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
                _schoolName = StringUtils.replace(StringUtils.replace(_schoolName, "　", ""), " ", "");
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }
}

// eof

