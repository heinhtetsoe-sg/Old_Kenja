/*
 * $Id: 1e54d331ff292e9d756a04c8043e36a5d556ed71 $
 *
 * 作成日: 2016/12/20
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

public class KNJL326G {

    private static final Log log = LogFactory.getLog(KNJL326G.class);

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
        svf.VrSetForm("KNJL326G.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String printDateStr = KNJ_EditDate.h_format_JP(db2, _param._printDate);
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String finschoolName = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME")) + StringUtils.defaultString(rs.getString("FINSCHOOL_TYPE_NAME"));
                final String majorName = ("1".equals(_param._printDiv) && !"2".equals(_param._passDiv)) ? rs.getString("SUC_MAJORNAME") : rs.getString("MAJORNAME1");
                final String courseName = ("1".equals(_param._printDiv) && !"2".equals(_param._passDiv)) ? rs.getString("SUC_COURSE") : rs.getString("COURSE1");

                svf.VrsOut("DATE", printDateStr);
                svf.VrsOut("SCHOOL_NAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME"));
                svf.VrsOut("JOB_NAME", (String) _param._certifSchoolMap.get("JOB_NAME"));
                svf.VrsOut("STAFF_NAME", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME"));

                if (null != _param._schoolStampPath) {
                    svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
                }
                svf.VrsOut("EXAM_NO", examno);
                final String seqName = getMS932Bytecount(name) > 50 ? "_3" : getMS932Bytecount(name) > 40 ? "_2" : "";
                final String seqFsName = getMS932Bytecount(finschoolName) > 50 ? "_3" : getMS932Bytecount(finschoolName) > 40 ? "_2" : "";
                svf.VrsOut("NAME" + seqName, name);
                svf.VrsOut("FINSCHOOL_NAME" + seqFsName, finschoolName);
                svf.VrsOut("TEXT1", getText(majorName, courseName));

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

    private String getText(final String majorName, final String courseName) {
        if ("1".equals(_param._printDiv)) {
            return "上記の者は入学選考の結果、" + majorName + "(" + courseName + ")に合格したことを通知します";
        } else {
        	if ("1".equals(_param._isKasiwaFlg)) {
                return "上記の者は入学選考の結果、残念ながら不合格に決定したことを通知します";
            } else {
                return "上記の者は入学選考の結果、第１、第２志望とも残念ながら不合格に決定したことを\r\n通知します";
            }
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     MAJOR.MAJORNAME AS SUC_MAJORNAME, ");
        stb.append("     SUC_COURSE.EXAMCOURSE_NAME AS SUC_COURSE, ");
        stb.append("     MAJOR1.MAJORNAME AS MAJORNAME1, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN FINSCHOOL.FINSCHOOL_TYPE = '3' THEN L019.NAME1 END AS FINSCHOOL_TYPE_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L019 ON L019.NAMECD1 = 'L019' AND L019.NAMECD2 = FINSCHOOL.FINSCHOOL_TYPE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        if ("1".equals(_param._printDiv) && !"2".equals(_param._passDiv)) {
            stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        }
        if ("2".equals(_param._printDiv) && !"2".equals(_param._unpassDiv)) {
            stb.append("           AND RECEPT.JUDGEDIV = '2' ");
        }
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON VBASE.SUC_COURSECD = MAJOR.COURSECD ");
        stb.append("          AND VBASE.SUC_MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST SUC_COURSE ON VBASE.ENTEXAMYEAR = SUC_COURSE.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = SUC_COURSE.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = SUC_COURSE.TESTDIV ");
        stb.append("          AND VBASE.SUC_COURSECD = SUC_COURSE.COURSECD ");
        stb.append("          AND VBASE.SUC_MAJORCD = SUC_COURSE.MAJORCD ");
        stb.append("          AND VBASE.SUC_COURSECODE = SUC_COURSE.EXAMCOURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR1 ON VBASE.DAI1_COURSECD = MAJOR1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = MAJOR1.MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.DAI2_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.DAI2_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.DAI2_COURSECODE = COURSE2.EXAMCOURSECD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        if ("1".equals(_param._printDiv) && "3".equals(_param._passDiv) && !"".equals(_param._passExamno) && null != _param._passExamno) {
            stb.append("           AND VBASE.EXAMNO >= '" + _param._passExamno + "' ");
        }
        if ("1".equals(_param._printDiv) && "3".equals(_param._passDiv) && !"".equals(_param._passExamnoTo) && null != _param._passExamnoTo) {
            stb.append("           AND VBASE.EXAMNO <= '" + _param._passExamnoTo + "' ");
        }
        if ("2".equals(_param._printDiv) && "3".equals(_param._unpassDiv) && !"".equals(_param._unpassExamno) && null != _param._unpassExamno) {
            stb.append("           AND VBASE.EXAMNO >= '" + _param._unpassExamno + "' ");
        }
        if ("2".equals(_param._printDiv) && "3".equals(_param._unpassDiv) && !"".equals(_param._unpassExamnoTo) && null != _param._unpassExamnoTo) {
            stb.append("           AND VBASE.EXAMNO <= '" + _param._unpassExamnoTo + "' ");
        }
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72535 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _printDate;
        private final String _printDiv;
        private final String _passDiv;
        private final String _passExamno;
        private final String _passExamnoTo;
        private final String _unpassDiv;
        private final String _unpassExamno;
        private final String _unpassExamnoTo;
        private final Map _certifSchoolMap;

        private final String _documentRoot;
        private String _imagePath;
        private String _extension;
        private String _schoolStampPath;
        private String _isKasiwaFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _printDate = request.getParameter("PRINT_DATE");
            _printDiv = request.getParameter("PRINT_DIV");
            _passDiv = request.getParameter("PASS_DIV");
            _passExamno = request.getParameter("PASS_EXAMNO");
            _passExamnoTo = request.getParameter("PASS_EXAMNO_TO");
            _unpassDiv = request.getParameter("UNPASS_DIV");
            _unpassExamno = request.getParameter("UNPASS_EXAMNO");
            _unpassExamnoTo = request.getParameter("UNPASS_EXAMNO_TO");
            _certifSchoolMap = getCertifScholl(db2);

            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP.bmp");
            _isKasiwaFlg = request.getParameter("KNJL326G_DISPTYPE_HIGASHIOSAKA_KASIWA");
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

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

    }
}

// eof

