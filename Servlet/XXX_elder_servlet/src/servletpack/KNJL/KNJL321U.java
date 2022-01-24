/*
 * $Id: 4e4645f7f134bac9ee31a3f0464332a754bab8f9 $
 *
 * 作成日: 2017/11/02
 * 作成者: yamashiro
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL321U {

    private static final Log log = LogFactory.getLog(KNJL321U.class);

    private static final Object NAIBU = null;

    private boolean _hasData;
    private final String PRINT_PASS = "1";
    private final String PRINT_NKYOKA = "2";
    private final String PRINT_NTUCHI = "3";

    private final String A_GOUKAKU = "1";
    private final String A_SHIGAN = "2";
    private final String A_EXAMNO = "3";

    private final String B_NYUUGAKU = "1";
    private final String B_EXAMNO = "3";

    private final String C_NYUUGAKU = "1";
    private final String C_EXAMNO = "3";

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
            if (PRINT_PASS.equals(_param._output)) {
                printPass(db2, svf);
            }
            if (PRINT_NKYOKA.equals(_param._output)) {
                printNyuugakuKyoka(db2, svf);
            }
            if (PRINT_NTUCHI.equals(_param._output)) {
                printNyuugakuTuchi(db2, svf);
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
        svf.VrSetForm("KNJL321U_1.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlPass("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME") + " 殿";

                svf.VrsOut("EXAM_NO", examno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 50 ? "4" : KNJ_EditEdit.getMS932ByteLength(name) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 26 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                final String[] nendoArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, _param._entexamyear + "/04/01"));
                svf.VrsOut("NENDO", nendoArray[1]);
                final String[] dateArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("YEAR", dateArray[1]);
                svf.VrsOut("MONTH", dateArray[2]);
                svf.VrsOut("DAY", dateArray[3]);
                schoolInfoPrint(svf, true);

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

    private String sqlPass(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     ADDR_D.GNAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR_D ON VBASE.ENTEXAMYEAR = ADDR_D.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = ADDR_D.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = ADDR_D.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (!"Z".equals(_param._testDiv)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        }
        if (A_EXAMNO.equals(_param._outputa)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examnoa + "' ");
        }
        if (!A_SHIGAN.equals(_param._outputa)) {
            stb.append("     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void printNyuugakuKyoka(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL321U_2.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlNyuugakuKyoka("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");

                svf.VrsOut("EXAM_NO", examno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 50 ? "4" : KNJ_EditEdit.getMS932ByteLength(name) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);

                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));

                schoolInfoPrint(svf, false);

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

    private String sqlNyuugakuKyoka(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     ADDR_D.GNAME, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR_D ON VBASE.ENTEXAMYEAR = ADDR_D.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = ADDR_D.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = ADDR_D.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (!"Z".equals(_param._testDiv)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        }
        if (B_EXAMNO.equals(_param._outputb)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examnob + "' ");
        }
        stb.append("     AND VBASE.PROCEDUREDIV = '1' ");
        stb.append("     AND VBASE.ENTDIV = '1' ");
        stb.append("     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.NAME_KANA ");
        return stb.toString();
    }

    private void printNyuugakuTuchi(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL321U_3.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlNyuugakuTuchi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 10;
            String befFsCd = "";
            int lineCnt = 1;
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String zipcd = StringUtils.defaultString(rs.getString("FINSCHOOL_ZIPCD"));
                final String address1 = StringUtils.defaultString(rs.getString("FINSCHOOL_ADDR1"));
                final String address2 = StringUtils.defaultString(rs.getString("FINSCHOOL_ADDR2"));
                if (!"".equals(befFsCd) && !befFsCd.equals(fsCd)) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }

                svf.VrsOut("FINSCHOOL_CD", fsCd);
                svf.VrsOut("FINSCHOOL_NAME", finschoolName + "長　殿");
                svf.VrsOut("ZIP_NO", zipcd);
                svf.VrsOut("FINSCHOOL_ADDR1", address1);
                svf.VrsOut("FINSCHOOL_ADDR2", address2);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));

                svf.VrsOut("CERT_NO", "成城中発第　　号");

                String grdNendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._loginYear));
                String entDate = KNJ_EditDate.h_format_JP(db2, _param._entexamyear + "/04/01");
                svf.VrsOut("TEXT", "　貴校" + grdNendo + "年度卒業下記生徒は、本校にて選考の結果、" + entDate + "付で本校第１学年");

                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                svf.VrsOutn("NAME", lineCnt, name);

                svf.VrsOut("TOTAL_NUM", String.valueOf(lineCnt));
                schoolInfoPrint(svf, false);

                befFsCd = fsCd;
                lineCnt++;
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

    private String sqlNyuugakuTuchi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     ADDR_D.GNAME, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ZIPCD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR1, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR2, ");
        stb.append("     ADDR_D.ADDRESS1, ");
        stb.append("     ADDR_D.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR_D ON VBASE.ENTEXAMYEAR = ADDR_D.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = ADDR_D.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = ADDR_D.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (!"Z".equals(_param._testDiv)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        }
        if (C_EXAMNO.equals(_param._outputc)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examnoc + "' ");
        }
        stb.append("     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') "); //合格
        stb.append("     AND VBASE.PROCEDUREDIV = '1' "); // 手続き終了
        stb.append("     AND VBASE.ENTDIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void schoolInfoPrint(final Vrw32alp svf, final boolean isLogoPrint) {
        if (isLogoPrint && null != _param._schoollogoFilePath) {
            svf.VrsOut("SCHOOL_LOGO", _param._schoollogoFilePath);
        }
        final String addr = StringUtils.defaultString(_param._certifSchool._remark4) + StringUtils.defaultString(_param._certifSchool._remark5);
        svf.VrsOut("SCHOOL_ADDR1", addr);
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);

        if (isLogoPrint) {
            final String teacherNameFied = KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._principalName) > 20 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._principalName) > 12 ? "2" : "1";
            svf.VrsOut("TEACHER_NAME" + teacherNameFied, _param._certifSchool._principalName);
        } else {
            final String teacherNameFied = KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._principalName) > 20 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchool._principalName) > 14 ? "2" : "1";
            svf.VrsOut("TEACHER_NAME" + teacherNameFied, _param._certifSchool._principalName);
        }

        svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);
        svf.VrsOut("TEACHER_NAME", _param._certifSchool._principalName);
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72422 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _testDiv;
        private final String _noticeDate;
        private final String _output;
        private final String _outputa;
        private final String _examnoa;
        private final String _outputb;
        private final String _examnob;
        private final String _outputc;
        private final String _examnoc;
        private final String _entexamyear;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _prgid;
        private final String _documentroot;
        private final String _imagepath;
        final String _schoollogoFilePath;
        final CertifSchool _certifSchool;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _noticeDate = request.getParameter("NOTICEDATE");
            _output = request.getParameter("OUTPUT");
            _outputa = request.getParameter("OUTPUTA");
            _examnoa = request.getParameter("EXAMNOA");
            _outputb = request.getParameter("OUTPUTB");
            _examnob = request.getParameter("EXAMNOB");
            _outputc = request.getParameter("OUTPUTC");
            _examnoc = request.getParameter("EXAMNOC");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoollogoFilePath = getImageFilePath("SCHOOLLOGO.jpg");
            _certifSchool = getCertifSchool(db2);
        }

        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool(null, null, null, null, null, null, null);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '105' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final String remark4 = rs.getString("REMARK4");
                    final String remark5 = rs.getString("REMARK5");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, remark4, remark5, remark6, remark7);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
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
