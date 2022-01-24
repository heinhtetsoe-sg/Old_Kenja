/*
 * $Id: 5bd06217d2fa5826f9e0de17e5c873cf28395b20 $
 *
 * 作成日: 2016/12/13
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL302G {

    private static final Log log = LogFactory.getLog(KNJL302G.class);

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
        svf.VrSetForm("KNJL302G.frm", 1);

        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　志願者名簿");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 50;
            int lineCnt = 1;
            String aftCd = "";
            String befCd = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String sex = rs.getString("SEX");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String cmc = rs.getString("CMC");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String shDiv = rs.getString("SHDIV");
                final String shName = rs.getString("SH_NAME");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                if ("1".equals(_param._kaipage)) {
                    aftCd = cmc;
                } else {
                    aftCd = shDiv;
                }
                if (lineCnt > maxLine || (!"".equals(befCd) && !befCd.equals(aftCd))) {
                    svf.VrEndPage();
                    svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　志願者名簿");
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
                    lineCnt = 1;
                }
                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                final String kanaField = getMS932Bytecount(nameKana) > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, lineCnt, nameKana);
                svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(birthday));
                svf.VrsOutn("SEX", lineCnt, sex);
                svf.VrsOutn("ZIP_NO", lineCnt, zipcd);
                final String addrField = getMS932Bytecount(address1 + address2) > 80 ? "2" : "1";
                svf.VrsOutn("ADDR" + addrField, lineCnt, address1 + address2);
                svf.VrsOutn("TEL_NO", lineCnt, telno);
                svf.VrsOutn("HOPE1", lineCnt, course1);
                svf.VrsOutn("HOPE2", lineCnt, course2);
                svf.VrsOutn("SDIV", lineCnt, shName);
                if ("1".equals(_param._kaipage)) {
                    svf.VrsOut("SUBTITLE", "（" + course1 + "）");
                } else {
                    svf.VrsOut("SUBTITLE", "（" + shName + "）");
                }
                svf.VrsOutn("FINSCHHO_CD", lineCnt, fsCd);
                final String schoolField = getMS932Bytecount(finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, finschoolName);

                befCd = aftCd;
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
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     VALUE(ADDR.ADDRESS1, '') AS ADDRESS1, ");
        stb.append("     VALUE(ADDR.ADDRESS2, '') AS ADDRESS2, ");
        stb.append("     ADDR.TELNO, ");
        stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     VBASE.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON VBASE.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
        stb.append("          AND VBASE.EXAMNO = ADDR.EXAMNO ");
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
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._kaipage)) {
            stb.append("     VBASE.DAI1_COURSECD, ");
            stb.append("     VBASE.DAI1_MAJORCD, ");
            stb.append("     VBASE.DAI1_COURSECODE, ");
        } else {
            stb.append("     VBASE.SHDIV, ");
        }
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57254 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _kaipage;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _kaipage = request.getParameter("KAIPAGE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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

