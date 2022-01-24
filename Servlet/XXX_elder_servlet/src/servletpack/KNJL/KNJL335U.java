/*
 * $Id: 7cac37f7d5b39faf3963a965e35da2f426fdebbd $
 *
 * 作成日: 2017/11/07
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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

public class KNJL335U {

    private static final Log log = LogFactory.getLog(KNJL335U.class);

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
        svf.VrSetForm("KNJL335U.frm", 1);
        setTitle(svf);
        final int maxLine = 35;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineCnt = 1;
            while (rs.next()) {

                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    setTitle(svf);
                }
                final String name = rs.getString("NAME");
                final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);

                final String kana = rs.getString("NAME_KANA");
                final String kanaField = KNJ_EditEdit.getMS932ByteLength(kana) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(kana) > 20 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, lineCnt, kana);

                final String procedureDiv = StringUtils.defaultString(rs.getString("PROCEDUREDIV"));
                svf.VrsOutn("PROC_F", lineCnt, procedureDiv);

                final String entDiv = StringUtils.defaultString(rs.getString("ENTDIV"));
                svf.VrsOutn("ENT_F", lineCnt, entDiv);

                for (int testCnt = 1; testCnt <= Integer.parseInt(_param._testDiv); testCnt++) {
                    final String examNo = rs.getString("EXAMNO_" + testCnt);
                    final String total4 = rs.getString("TOTAL4_" + testCnt);
                    final String judge = rs.getString("ABBV1_" + testCnt);
                    svf.VrsOutn("EXAM_NO" + testCnt, lineCnt, examNo);
                    svf.VrsOutn("EXAM_F" + testCnt, lineCnt, null != total4 ? "1" : "");
                    svf.VrsOutn("TOTAL_" + testCnt, lineCnt, total4);
                    svf.VrsOutn("PASS_F" + testCnt, lineCnt, judge);
                }

                _hasData = true;
                lineCnt++;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "/04/01") + "度　" + _param._applicantName + "　" + _param._testdivName + "　重複受験者　成績一覧表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RECOM_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.ENTEXAMYEAR, ");
        stb.append("     BASE.RECOM_EXAMNO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     RECOM_T.RECOM_EXAMNO, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE, ");
        stb.append("     RECOM_T ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = RECOM_T.ENTEXAMYEAR ");
        stb.append("     AND BASE.RECOM_EXAMNO = RECOM_T.RECOM_EXAMNO ");
        stb.append("     AND BASE.TESTDIV <= '" + _param._testDiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     RECOM_T.RECOM_EXAMNO ");
        stb.append(" HAVING ");
        stb.append("     COUNT(*) > 1 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.RECOM_EXAMNO, ");
        stb.append("     CASE '" + _param._testDiv + "' ");
        stb.append("          WHEN BASE_1.TESTDIV THEN BASE_1.NAME ");
        stb.append("          WHEN BASE_2.TESTDIV THEN BASE_2.NAME ");
        stb.append("          WHEN BASE_3.TESTDIV THEN BASE_3.NAME ");
        stb.append("          ELSE '' ");
        stb.append("     END AS NAME, ");
        stb.append("     CASE '" + _param._testDiv + "' ");
        stb.append("          WHEN BASE_1.TESTDIV THEN BASE_1.NAME_KANA ");
        stb.append("          WHEN BASE_2.TESTDIV THEN BASE_2.NAME_KANA ");
        stb.append("          WHEN BASE_3.TESTDIV THEN BASE_3.NAME_KANA ");
        stb.append("          ELSE '' ");
        stb.append("     END AS NAME_KANA, ");
        stb.append("     BASE_1.TESTDIV AS TESTDIV_1_1, ");
        stb.append("     BASE_1.EXAMNO AS EXAMNO_1, ");
        stb.append("     RECEPT_1.TOTAL4 AS TOTAL4_1, ");
        stb.append("     L013_1.ABBV1 AS ABBV1_1, ");
        stb.append("     BASE_2.TESTDIV AS TESTDIV_2_2, ");
        stb.append("     BASE_2.EXAMNO AS EXAMNO_2, ");
        stb.append("     RECEPT_2.TOTAL4 AS TOTAL4_2, ");
        stb.append("     L013_2.ABBV1 AS ABBV1_2, ");
        stb.append("     BASE_3.TESTDIV AS TESTDIV_3_3, ");
        stb.append("     BASE_3.EXAMNO AS EXAMNO_3, ");
        stb.append("     RECEPT_3.TOTAL4 AS TOTAL4_3, ");
        stb.append("     L013_3.ABBV1 AS ABBV1_3, ");
        stb.append("     CASE '" + _param._testDiv + "' ");
        stb.append("          WHEN BASE_1.TESTDIV THEN BASE_1.PROCEDUREDIV ");
        stb.append("          WHEN BASE_2.TESTDIV THEN BASE_2.PROCEDUREDIV ");
        stb.append("          WHEN BASE_3.TESTDIV THEN BASE_3.PROCEDUREDIV ");
        stb.append("          ELSE '' ");
        stb.append("     END AS PROCEDUREDIV, ");
        stb.append("     CASE '" + _param._testDiv + "' ");
        stb.append("          WHEN BASE_1.TESTDIV THEN BASE_1.ENTDIV ");
        stb.append("          WHEN BASE_2.TESTDIV THEN BASE_2.ENTDIV ");
        stb.append("          WHEN BASE_3.TESTDIV THEN BASE_3.ENTDIV ");
        stb.append("          ELSE '' ");
        stb.append("     END AS ENTDIV ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE_1 ON BASE_1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("          AND BASE_1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("          AND BASE_1.TESTDIV = '1' ");
        stb.append("          AND MAIN_T.RECOM_EXAMNO = BASE_1.RECOM_EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L013_1 ON L013_1.NAMECD1 = 'L013' ");
        stb.append("          AND BASE_1.JUDGEMENT = L013_1.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT_1 ON BASE_1.ENTEXAMYEAR = RECEPT_1.ENTEXAMYEAR ");
        stb.append("          AND BASE_1.APPLICANTDIV = RECEPT_1.APPLICANTDIV ");
        stb.append("          AND BASE_1.TESTDIV = RECEPT_1.TESTDIV ");
        stb.append("          AND BASE_1.EXAMNO = RECEPT_1.EXAMNO ");
        stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE_2 ON BASE_2.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("          AND BASE_2.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("          AND BASE_2.TESTDIV = '2' ");
        stb.append("          AND MAIN_T.RECOM_EXAMNO = BASE_2.RECOM_EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L013_2 ON L013_2.NAMECD1 = 'L013' ");
        stb.append("          AND BASE_2.JUDGEMENT = L013_2.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT_2 ON BASE_2.ENTEXAMYEAR = RECEPT_2.ENTEXAMYEAR ");
        stb.append("          AND BASE_2.APPLICANTDIV = RECEPT_2.APPLICANTDIV ");
        stb.append("          AND BASE_2.TESTDIV = RECEPT_2.TESTDIV ");
        stb.append("          AND BASE_2.EXAMNO = RECEPT_2.EXAMNO ");
        stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE_3 ON BASE_3.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("          AND BASE_3.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("          AND BASE_3.TESTDIV = '3' ");
        stb.append("          AND MAIN_T.RECOM_EXAMNO = BASE_3.RECOM_EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L013_3 ON L013_3.NAMECD1 = 'L013' ");
        stb.append("          AND BASE_3.JUDGEMENT = L013_3.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT_3 ON BASE_3.ENTEXAMYEAR = RECEPT_3.ENTEXAMYEAR ");
        stb.append("          AND BASE_3.APPLICANTDIV = RECEPT_3.APPLICANTDIV ");
        stb.append("          AND BASE_3.TESTDIV = RECEPT_3.TESTDIV ");
        stb.append("          AND BASE_3.EXAMNO = RECEPT_3.EXAMNO ");
        stb.append(" ORDER BY ");
        stb.append("     CASE '" + _param._testDiv + "' ");
        stb.append("          WHEN BASE_1.TESTDIV THEN VALUE(RECEPT_1.TOTAL4, 0) ");
        stb.append("          WHEN BASE_2.TESTDIV THEN VALUE(RECEPT_2.TOTAL4, 0) ");
        stb.append("          WHEN BASE_3.TESTDIV THEN VALUE(RECEPT_3.TOTAL4, 0) ");
        stb.append("          ELSE 0 ");
        stb.append("     END DESC, ");
        stb.append("     CASE '" + _param._testDiv + "' ");
        stb.append("          WHEN BASE_1.TESTDIV THEN BASE_1.EXAMNO ");
        stb.append("          WHEN BASE_2.TESTDIV THEN BASE_2.EXAMNO ");
        stb.append("          WHEN BASE_3.TESTDIV THEN BASE_3.EXAMNO ");
        stb.append("          ELSE '' ");
        stb.append("     END     ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56983 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantDiv;
        final String _testDiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _applicantName;
        final String _testdivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _applicantName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
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
