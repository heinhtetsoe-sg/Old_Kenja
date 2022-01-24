/*
 * $Id: 0ba412d0ad886892e3aa05540ce9f2c08ac001f9 $
 *
 * 作成日: 2016/11/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJL051F {

    private static final Log log = LogFactory.getLog(KNJL051F.class);

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
        final String form = SCHOOL_J.equals(_param._applicantdiv) ? "KNJL051F_2.frm" : "KNJL051F.frm";
        svf.VrSetForm(form, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            setTitle(db2, svf);
            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > 40) {
                    svf.VrEndPage();
                    setTitle(db2, svf);
                    lineCnt = 1;
                }
                svf.VrsOutn("COURSE", lineCnt, rs.getString("EXAMCOURSE_ABBV"));
                svf.VrsOutn("EXAM_NO1", lineCnt, rs.getString("RECEPTNO"));
                svf.VrsOutn("NAME", lineCnt, rs.getString("NAME"));
                svf.VrsOutn("SCHOOL_NAME", lineCnt, rs.getString("FINSCHOOL_NAME"));
                if (!"1".equals(_param._tempPrint)) {
                    svf.VrsOutn("INTERVIEW_VALUE1", lineCnt, rs.getString("INTERVIEW_VALUE"));
                    svf.VrsOutn("INTERVIEW_REMARK1", lineCnt, rs.getString("INTERVIEW_REMARK"));
                }
                lineCnt++;
                _hasData = true;
            }
            if (lineCnt > 1) {
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
        stb.append(" SELECT ");
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            stb.append("     '" + _param._examTypeName + "' AS EXAMCOURSE_ABBV, ");//受験型
        } else {
            stb.append("     COURSE.EXAMCOURSE_ABBV, ");
        }
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     INTERVIEW.INTERVIEW_VALUE, ");
        stb.append("     INTERVIEW.INTERVIEW_REMARK ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        if ("1".equals(_param._specialReasonDiv)) {
            stb.append("                                    AND BASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BASE_011 ON BASE.ENTEXAMYEAR = BASE_011.ENTEXAMYEAR ");
            stb.append("          AND BASE.EXAMNO = BASE_011.EXAMNO ");
            stb.append("          AND BASE_011.SEQ = '011' ");
            stb.append("          AND BASE_011.REMARK" + _param._testdiv + " = '" + _param._examType + "' ");
        } else {
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RECEPT_003 ON RECEPT.ENTEXAMYEAR = RECEPT_003.ENTEXAMYEAR ");
            stb.append("           AND RECEPT.APPLICANTDIV = RECEPT_003.APPLICANTDIV ");
            stb.append("           AND RECEPT.TESTDIV = RECEPT_003.TESTDIV ");
            stb.append("           AND RECEPT.EXAM_TYPE = RECEPT_003.EXAM_TYPE ");
            stb.append("           AND RECEPT.RECEPTNO = RECEPT_003.RECEPTNO ");
            stb.append("           AND RECEPT_003.SEQ = '003' ");
            stb.append("           AND RECEPT_003.REMARK1 = '" + _param._testdiv0 + "' ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_001 ON BASE.ENTEXAMYEAR = BASE_001.ENTEXAMYEAR ");
            stb.append("          AND BASE.EXAMNO = BASE_001.EXAMNO ");
            stb.append("          AND BASE_001.SEQ = '001' ");
            stb.append("          AND BASE_001.REMARK8 || '-' || BASE_001.REMARK9 || '-' || BASE_001.REMARK10 = '" + _param._examcourse + "' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON RECEPT.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = COURSE.APPLICANTDIV ");
            stb.append("          AND BASE_001.REMARK8 = COURSE.COURSECD ");
            stb.append("          AND BASE_001.REMARK9 = COURSE.MAJORCD ");
            stb.append("          AND BASE_001.REMARK10 = COURSE.EXAMCOURSECD ");
        }
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ON RECEPT.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = INTERVIEW.APPLICANTDIV ");
        stb.append("          AND RECEPT.TESTDIV = INTERVIEW.TESTDIV ");
        stb.append("          AND RECEPT.EXAMNO = INTERVIEW.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        if (!"1".equals(_param._specialReasonDiv)) {
            stb.append("     AND VALUE(RECEPT.JUDGEDIV, '') <> '4' ");
        }
        if ("3".equals(_param._testdiv)) {
            stb.append("     AND VALUE(BASE.GENERAL_FLG, '') <> '1' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + "入試　" + _param._testdivName1 + "　面接結果表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            svf.VrsOut("SUBTITLE", "（" + _param._examTypeName + "）");
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65622 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _testdiv;
        private final String _testdiv0;
        private final String _specialReasonDiv;
        private final String _tempPrint;
        private final String _entexamyear;
        private final String _ctrlDate;
        private final String _prgid;
        private final String _printLogStaffcd;
        private final String _printLogRemoteAddr;
        final String _examcourse;
        final String _examcoursename;
        final String _examType;
        final String _examTypeName;

        private final String _applicantdivName;
        private final String _testdivName1;
        private final String _testdivAbbv1;
        private final String _testdivAbbv2;
        private final String _testdivAbbv3;
        private final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _testdiv            = request.getParameter("TESTDIV");
            _testdiv0           = request.getParameter("TESTDIV0");
            _specialReasonDiv   = request.getParameter("SPECIAL_REASON_DIV");
            _examcourse         = request.getParameter("EXAMCOURSE");
            _examType           = request.getParameter("EXAM_TYPE");
            _tempPrint          = request.getParameter("TEMP_PRINT");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _prgid              = request.getParameter("PRGID");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            final String testName = SCHOOL_J.equals(_applicantdiv) ? "L024" : "L004";
            _testdivName1 = StringUtils.defaultString(getNameMst(db2, "NAME1", testName, _testdiv));
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", testName, _testdiv));
            _testdivAbbv2 = StringUtils.defaultString(getNameMst(db2, "ABBV2", testName, _testdiv));
            _testdivAbbv3 = StringUtils.defaultString(getNameMst(db2, "ABBV3", testName, _testdiv));
            _dateStr = getDateStr(db2, _ctrlDate);
            _examcoursename = getExamcoursename(db2);
            _examTypeName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L005", _examType));
        }

        private String getExamcoursename(DB2UDB db2) {
//            if ("1".equals(_examcourse)) {
//                return "理数キャリア";
//            } else if ("2".equals(_examcourse)) {
//                return "国際教養";
//            } else if ("3".equals(_examcourse)) {
//                return "スポーツ科学";
//            }
//            return "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR  = '" + _entexamyear + "' AND ");
            stb.append("     APPLICANTDIV = '" + _applicantdiv + "' AND ");
            stb.append("     TESTDIV      = '1' ");
            stb.append("     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '" + _examcourse + "'");

            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("LABEL");
                }
            } catch (Exception e) {
                log.error("getSchoolName Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
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

    }
}

// eof

