/*
 * $Id: d3ea321db3f770e1b00eb94a1e98ebba61f44361 $
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL327G {

    private static final Log log = LogFactory.getLog(KNJL327G.class);

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
        svf.VrSetForm("KNJL327G.frm", 1);

        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        setTitle(svf, nendo);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int maxLine = 16;
            int lineCnt = 1;
            String befCd = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String judge = rs.getString("JUDGE");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String fsCd = rs.getString("FS_CD");
                final String fsZip = rs.getString("FINSCHOOL_ZIPCD");
                final String fsAddr1 = rs.getString("FINSCHOOL_ADDR1");
                final String fsAddr2 = rs.getString("FINSCHOOL_ADDR2");
                final String finschoolName = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME")) + "中学校長　様";
                if (lineCnt > maxLine || (!"".equals(befCd) && !befCd.equals(fsCd))) {
                    svf.VrEndPage();
                    setTitle(svf, nendo);
                    lineCnt = 1;
                    if ((!"".equals(befCd) && !befCd.equals(fsCd))) {
                        svf.VrSetForm("KNJL327G.frm", 1);
                        maxLine = 16;
                    } else {
                        svf.VrSetForm("KNJL327G_2.frm", 1);
                        maxLine = 40;
                    }
                }
                svf.VrsOut("ZIP_NO", fsZip);
                String addrField = "1";
                if (getMS932Bytecount(fsAddr1) > 50 || getMS932Bytecount(fsAddr2) > 50) {
                    addrField = "4";
                }
                if (getMS932Bytecount(fsAddr1) > 42 || getMS932Bytecount(fsAddr2) > 42) {
                    addrField = "3";
                }
                if (getMS932Bytecount(fsAddr1) > 34 || getMS932Bytecount(fsAddr2) > 34) {
                    addrField = "2";
                }
                svf.VrsOut("ADDR1_" + addrField, fsAddr1);
                svf.VrsOut("ADDR2_" + addrField, fsAddr2);
                final String fsNameField = getMS932Bytecount(finschoolName) > 42 ? "3" : getMS932Bytecount(finschoolName) > 32 ? "2" : "1";
                svf.VrsOut("JHSCHOOL_NAME" + fsNameField, finschoolName);

                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._printDate));
                svf.VrsOut("SCHOOL_NAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME"));
                svf.VrsOut("JOB_NAME", (String) _param._certifSchoolMap.get("JOB_NAME"));
                svf.VrsOut("STAFF_NAME", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME"));

                svf.VrsOutn("JUDGE", lineCnt, judge);
                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                svf.VrsOutn("SEX", lineCnt, sex);

                svf.VrsOutn("FINSCHHO_CD", lineCnt, fsCd);
                final String schoolField = getMS932Bytecount(finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, finschoolName);
                svf.VrsOutn("COURSE_NAME1", lineCnt, course1);
                svf.VrsOutn("COURSE_NAME2", lineCnt, course2);

                if (null != _param._schoolStampPath) {
                    svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
                }

                lineCnt++;
                befCd = fsCd;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf, final String nendo) {
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
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
        stb.append("     L013.NAME1 AS JUDGE, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ZIPCD, ");
        stb.append("     VALUE(FINSCHOOL.FINSCHOOL_ADDR1, '') AS FINSCHOOL_ADDR1, ");
        stb.append("     VALUE(FINSCHOOL.FINSCHOOL_ADDR2, '') AS FINSCHOOL_ADDR2, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND VBASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.SUC_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.SUC_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.SUC_COURSECODE = COURSE2.EXAMCOURSECD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if ("1".equals(_param._specialReasonDiv)) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV = '" + _param._specialReasonDiv + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58596 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _specialReasonDiv;
        private final String _printDate;
        private final String _loginDate;
        private final Map _certifSchoolMap;
        private final String _documentRoot;
        private String _imagePath;
        private String _extension;
        private String _schoolStampPath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _printDate = request.getParameter("PRINT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _certifSchoolMap = getCertifScholl(db2);
            _testDiv = request.getParameter("TESTDIV");
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");

            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP.bmp");
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
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '112' ");
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

