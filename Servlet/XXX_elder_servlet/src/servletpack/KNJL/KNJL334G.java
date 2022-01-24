/*
 * $Id: 9d9373cdfce0014057192454759b78d69064473b $
 *
 * 作成日: 2016/12/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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

public class KNJL334G {

    private static final Log log = LogFactory.getLog(KNJL334G.class);

    private boolean _hasData;
    private final int MAX_COURSE_CNT = 5;

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
        svf.VrSetForm("KNJL334G.frm", 1);

        setTitle(db2, svf);

        try {
            int lineCnt = 1;
            for (Iterator iterator = _param._courseMap.keySet().iterator(); iterator.hasNext();) {
                if (lineCnt > MAX_COURSE_CNT) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    setTitle(db2, svf);
                }
                final String course = (String) iterator.next();
                final String courseName = (String) _param._courseMap.get(course);

                final String sql = sql(course, false);
                log.debug(" sql =" + sql);
                setPrintData(db2, svf, courseName, sql, false, lineCnt);

                final String totalSql = sqlSougoukei(course, true);
                log.debug(" sql =" + totalSql);
                setPrintData(db2, svf, courseName, totalSql, true, lineCnt);

                lineCnt++;
            }
            final String sql = sql("ALL", false);
            log.debug(" sql =" + sql);
            setPrintData(db2, svf, "　合　計　", sql, false, 6);

            final String totalSql = sqlSougoukei("ALL", true);
            log.debug(" sql =" + totalSql);
            setPrintData(db2, svf, "　合　計　", totalSql, true, 6);
            svf.VrEndPage();

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            db2.commit();
        }
    }

    private void setPrintData(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String courseName,
            final String sql,
            final boolean isGoukei,
            final int lineCnt
    ) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String courseField = getMS932Bytecount(courseName) > 18 ? "2" : "1";
            svf.VrsOutn("COURSE_NAME" + courseField, lineCnt, courseName);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String dataDiv = rs.getString("DATA_DIV");
                final String sex = rs.getString("SEX");
                final String shDiv = isGoukei ? "" : rs.getString("SHDIV");
                final String cnt = rs.getString("CNT");
                final String setField = isGoukei ? "" : "1".equals(shDiv) ? "S_" : "H_";


                svf.VrsOutn(setField + dataDiv + sex, lineCnt, cnt);

                _hasData = true;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　合格者数");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
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

    private String sql(final String course, final boolean isGoukei) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     'GOU' AS DATA_DIV, ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     VBASE.SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = VBASE.SUC_COURSECD || VBASE.SUC_MAJORCD || VBASE.SUC_COURSECODE ");
        stb.append(" GROUP BY ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE, ");
        }
        stb.append("     VBASE.SEX ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'GOU' AS DATA_DIV, ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     '3' AS SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = VBASE.SUC_COURSECD || VBASE.SUC_MAJORCD || VBASE.SUC_COURSECODE ");
        boolean groupByFlg = false;
        if (!isGoukei) {
            stb.append(" GROUP BY ");
            stb.append("     VBASE.SHDIV ");
            groupByFlg = true;
        }
        if (!"ALL".equals(course)) {
            if (!groupByFlg) {
                stb.append(" GROUP BY ");
            } else {
                stb.append("     , ");
            }
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE ");
            groupByFlg = false;
        }

        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'TEN' AS DATA_DIV, ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     VBASE.SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE != VBASE.SUC_COURSECD || VBASE.SUC_MAJORCD || VBASE.SUC_COURSECODE ");
        stb.append(" GROUP BY ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE, ");
        }
        stb.append("     VBASE.SEX ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'TEN' AS DATA_DIV, ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     '3' AS SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE != VBASE.SUC_COURSECD || VBASE.SUC_MAJORCD || VBASE.SUC_COURSECODE ");
        if (!isGoukei) {
            stb.append(" GROUP BY ");
            stb.append("     VBASE.SHDIV ");
            groupByFlg = true;
        }
        if (!"ALL".equals(course)) {
            if (!groupByFlg) {
                stb.append(" GROUP BY ");
            } else {
                stb.append("     , ");
            }
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE ");
            groupByFlg = false;
        }

        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'GOUKEI' AS DATA_DIV, ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     VBASE.SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append(" GROUP BY ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE, ");
        }
        stb.append("     VBASE.SEX ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'GOUKEI' AS DATA_DIV, ");
        if (!isGoukei) {
            stb.append("     VBASE.SHDIV, ");
        }
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     '3' AS SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        if (!isGoukei) {
            stb.append(" GROUP BY ");
            stb.append("     VBASE.SHDIV ");
            groupByFlg = true;
        }
        if (!"ALL".equals(course)) {
            if (!groupByFlg) {
                stb.append(" GROUP BY ");
            } else {
                stb.append("     , ");
            }
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE ");
            groupByFlg = false;
        }

        stb.append(" ORDER BY ");
        if (!isGoukei) {
            stb.append("     SHDIV, ");
        }
        stb.append("     SEX ");

        return stb.toString();
    }

    private String sqlSougoukei(final String course, final boolean isGoukei) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     'SOUGOUKEI' AS DATA_DIV, ");
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     VBASE.SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        stb.append(" GROUP BY ");
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE, ");
        }
        stb.append("     VBASE.SEX ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'SOUGOUKEI' AS DATA_DIV, ");
        if (!"ALL".equals(course)) {
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        }
        stb.append("     '3' AS SEX, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("           AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if (!"ALL".equals(course)) {
            stb.append("     AND VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE = '" + course + "' ");
        }
        if (_param._isSpecialReason) {
            stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
        }
        if (!"ALL".equals(course)) {
            stb.append(" GROUP BY ");
            stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE ");
        }

        stb.append(" ORDER BY ");
        stb.append("     SEX ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65767 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        final boolean _isSpecialReason;
        private final String _formType;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;
        final Map _courseMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _formType = request.getParameter("FORM_TYPE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _isSpecialReason = "1".equals(request.getParameter("SPECIAL_REASON_DIV"));
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
            _courseMap = getCourseMap(db2);
        }

        private Map getCourseMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_COURSE_MST ");
                stb.append(" WHERE ");
                stb.append("     ENTEXAMYEAR = '" + _entexamyear + "' ");
                stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
                stb.append("     AND TESTDIV = '" + _testDiv + "' ");
                stb.append(" ORDER BY ");
                stb.append("     COURSECD, ");
                stb.append("     MAJORCD, ");
                stb.append("     EXAMCOURSECD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setKey = rs.getString("COURSECD") + rs.getString("MAJORCD") + rs.getString("EXAMCOURSECD");
                    retMap.put(setKey, rs.getString("EXAMCOURSE_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
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

