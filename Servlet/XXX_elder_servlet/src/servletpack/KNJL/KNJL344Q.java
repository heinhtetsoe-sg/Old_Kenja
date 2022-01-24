/*
 * $Id: a96f9bdbf6a114683fed9f5871d264e6d3f8d396 $
 *
 * 作成日: 2017/10/26
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

public class KNJL344Q {

    private static final Log log = LogFactory.getLog(KNJL344Q.class);

    private boolean _hasData;
    private final String PRINT_PASS = "1";
    private final String PRINT_KEKKA = "2";
    private final String PRINT_NTUCHI = "3";
    private final String PRINT_NKYOKA = "4";

    private final String JUDGE_PASS = "1";
    private final String JUDGE_UNPASS = "2";
    private final String JUDGE_PASS_HOKETU = "3";

    private final String SUISEN = "1";
    private final String SENBATSU = "2";
    private final String NAIBU = "9";

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
            if (PRINT_KEKKA.equals(_param._taisyou)) {
                if ("P".equals(_param._schoolkind)) {
                    printUnPassP(db2, svf);
                } else {
                    printUnPass(db2, svf);
                }
            }
            if (PRINT_NTUCHI.equals(_param._taisyou)) {
                printNyuugakuTuchi(db2, svf);
            }
            if (PRINT_NKYOKA.equals(_param._taisyou)) {
                if ("P".equals(_param._schoolkind)) {
                    printNyuugakuKyokaP(db2, svf);
                } else {
                    printNyuugakuKyoka(db2, svf);
                }
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
        svf.VrSetForm("KNJL344Q_1.frm", 1);

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

                svf.VrsOut("EXAM_NO", examno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
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
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
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

    private void printUnPassP(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL344Q_2_3.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlUnPass("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String gName = rs.getString("GNAME");

                svf.VrsOut("NAME1", name + "　様");
                svf.VrsOut("NAME2", gName + "　様");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("GREETING", _param._certifSchool._remark9); //季節の挨拶
                schoolInfoPrint(svf, false); //校章なし
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
                final String gName = rs.getString("GNAME");

                if (SUISEN.equals(_param._testDiv)) {
                    svf.VrSetForm("KNJL344Q_2_1.frm", 1);
                } else {
                    svf.VrSetForm("KNJL344Q_2_2.frm", 1);
                }
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME1", name + "　様");
                svf.VrsOut("NAME2", gName + "　様");
                svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_S(_param._senbatsuTestDate, "M月d日"));
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
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

    private String sqlUnPass(final String selectDiv) {
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
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append("     AND VBASE.JUDGEMENT IN ('" + JUDGE_UNPASS + "', '" + JUDGE_PASS_HOKETU + "') ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void printNyuugakuTuchi(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL344Q_3.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlNyuugakuTuchi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");

                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 22 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 16 ? "2" : "1";
                svf.VrsOut("FINSCHOOL_NAME", finschoolName);
                svf.VrsOut("NAME" + nameField, name);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
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

    private String sqlNyuugakuTuchi(final String selectDiv) {
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
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append("     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void printNyuugakuKyokaP(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL344Q_4_2.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlNyuugakuKyoka("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String address1 = StringUtils.defaultString(rs.getString("ADDRESS1"));
                final String address2 = StringUtils.defaultString(rs.getString("ADDRESS2"));

                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                final String addrPlus = address1 + address2;
                if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 60) {
                    String addrField = "1";
                    if (KNJ_EditEdit.getMS932ByteLength(address1) > 50 || KNJ_EditEdit.getMS932ByteLength(address2) > 50) {
                        addrField = "4";
                    } else if (KNJ_EditEdit.getMS932ByteLength(address1) > 40 || KNJ_EditEdit.getMS932ByteLength(address2) > 40) {
                        addrField = "3";
                    } else if (KNJ_EditEdit.getMS932ByteLength(address1) > 30 || KNJ_EditEdit.getMS932ByteLength(address2) > 30) {
                        addrField = "2";
                    }
                    svf.VrsOut("ADDR1_" + addrField, address1);
                    svf.VrsOut("ADDR2_" + addrField, address2);
                } else {
                    String addrField = "1";
                    if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 50) {
                        addrField = "4";
                    } else if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 40) {
                        addrField = "3";
                    } else if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 30) {
                        addrField = "2";
                    }
                    svf.VrsOut("ADDR2_" + addrField, addrPlus);
                }
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("ENT_MONTH2", KNJ_EditDate.h_format_JP_M(db2, _param._entexamyear + "/04/01"));
                schoolInfoPrint(svf, false); //校章なし

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

    private void printNyuugakuKyoka(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL344Q_4.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sqlNyuugakuKyoka("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String address1 = StringUtils.defaultString(rs.getString("ADDRESS1"));
                final String address2 = StringUtils.defaultString(rs.getString("ADDRESS2"));

                svf.VrsOut("FINSCHOOL_NAME", finschoolName);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name);
                final String addrPlus = address1 + address2;
                if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 60) {
                    String addrField = "1";
                    if (KNJ_EditEdit.getMS932ByteLength(address1) > 50 || KNJ_EditEdit.getMS932ByteLength(address2) > 50) {
                        addrField = "4";
                    } else if (KNJ_EditEdit.getMS932ByteLength(address1) > 40 || KNJ_EditEdit.getMS932ByteLength(address2) > 40) {
                        addrField = "3";
                    } else if (KNJ_EditEdit.getMS932ByteLength(address1) > 30 || KNJ_EditEdit.getMS932ByteLength(address2) > 30) {
                        addrField = "2";
                    }
                    svf.VrsOut("ADDR1_" + addrField, address1);
                    svf.VrsOut("ADDR2_" + addrField, address2);
                } else {
                    String addrField = "1";
                    if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 50) {
                        addrField = "4";
                    } else if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 40) {
                        addrField = "3";
                    } else if (KNJ_EditEdit.getMS932ByteLength(addrPlus) > 30) {
                        addrField = "2";
                    }
                    svf.VrsOut("ADDR2_" + addrField, addrPlus);
                }
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                if (NAIBU.equals(_param._testDiv)) {
                    svf.VrsOut("ENT_MONTH1", KNJ_EditDate.h_format_JP_M(db2, _param._entexamyear + "/04/01"));
                    schoolInfoPrint(svf, true);
                } else {
                    svf.VrsOut("ENT_MONTH2", KNJ_EditDate.h_format_JP_M(db2, _param._entexamyear + "/04/01"));
                    schoolInfoPrint(svf, true);
                }

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
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
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
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (SITEI.equals(_param._output)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._examno + "' ");
        }
        if (!"1".equals(_param._jizenNkyoka)) {
            stb.append("     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private void schoolInfoPrint(final Vrw32alp svf, final boolean isLogoPrint) {
        if (isLogoPrint && null != _param._schoollogoFilePath) {
            svf.VrsOut("SCHOOL_LOGO", _param._schoollogoFilePath);
        }
        if (null != _param._staffStampFilePath) {
            svf.VrsOut("STAFFSTAMP", _param._staffStampFilePath);
        }
        if (null != _param._certifSchool) {
        	svf.VrsOut("SCHOOL_NAME1", _param._certifSchool._remark6 + _param._certifSchool._remark7);
        	svf.VrsOut("SCHOOL_NAME2", _param._certifSchool._schoolName);
        	svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);
        }
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark6;
        final String _remark7;
        final String _remark9;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark6,
                final String remark7,
                final String remark9
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark6 = remark6;
            _remark7 = remark7;
            _remark9 = remark9;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77360 $");
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
        final String _jizenNkyoka;
        final String _output;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _examno;
        final String _documentroot;
        final String _imagepath;
        final String _schoolkind;
        final String _schoollogoFilePath;
        final String _staffStampFilePath;
        final CertifSchool _certifSchool;
        final String _senbatsuTestDate;
        final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _noticeDate = request.getParameter("NOTICEDATE");
            _taisyou = request.getParameter("TAISYOU");
            _jizen = request.getParameter("JIZEN");
            _jizenNkyoka = request.getParameter("JIZEN_NKYOKA");
            _output = request.getParameter("OUTPUT");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _examno = request.getParameter("EXAMNO");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _schoollogoFilePath = getImageFilePath("SCHOOLBADGE2.jpg"); //校章
            if ("P".equals(_schoolkind)) {
                _staffStampFilePath = getImageFilePath("PRINCIPALSTAMP_P.bmp");//校長印
                _certifSchool = getCertifSchool(db2, "145");
                _senbatsuTestDate = StringUtils.defaultString(getNameMst(db2, "NAMESPARE1", "LP24", SENBATSU));
                _testDivName = StringUtils.defaultString(getNameMst(db2, "ABBV1", "LP24", _testDiv));
            } else {
                _staffStampFilePath = getImageFilePath("PRINCIPALSTAMP_J.bmp");//校長印
                _certifSchool = getCertifSchool(db2, "105");
                _senbatsuTestDate = StringUtils.defaultString(getNameMst(db2, "NAMESPARE1", "L024", SENBATSU));
                _testDivName = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L024", _testDiv));
            }
        }

        private CertifSchool getCertifSchool(final DB2UDB db2, final String certifKindcd) {
            CertifSchool certifSchool = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindcd + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark9 = rs.getString("REMARK9");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, remark6, remark7, remark9);
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
