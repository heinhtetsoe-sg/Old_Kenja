/*
 * $Id: 0a6c4a9b8f4d914d56eb4f37ff28384cba722fad $
 *
 * 作成日: 2016/11/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL342F {

    private static final Log log = LogFactory.getLog(KNJL342F.class);

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

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String toukeiPref = rs.getString("TOUKEI_PREF");
                final String toukeiCity = rs.getString("TOUKEI_CITY");
                final String testdiv = rs.getString("TESTDIV");
                final String dataDiv = rs.getString("DATA_DIV");
                final String finschoolDistcd = rs.getString("FINSCHOOL_DISTCD");
                final String cnt = rs.getString("CNT");
                final String testdivAvg = rs.getString("TESTDIV_AVG");

                final PrintData printData = new PrintData(toukeiPref, toukeiCity, testdiv, dataDiv, finschoolDistcd, cnt, testdivAvg);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL342F.frm", 4);
        setTitle(svf);
        final List list = getList(db2);
        String befPref = "";
        String befDist = "";
        for (int line = 0; line < list.size(); line++) {
            final PrintData printData = (PrintData) list.get(line);
            if (!"".equals(befPref) && !befPref.equals(printData._toukeiPref)
                ||
                !"".equals(befDist) && !befDist.equals(printData._finschoolDistcd)
            ) {
                svf.VrEndRecord();
            }
            if ("1".equals(printData._dataDiv)) {
                svf.VrsOut("NUM" + printData._testdiv, printData._cnt);
                svf.VrsOut("CITY_NAME1", printData._toukeiCity);
                svf.VrsOut("PER" + printData._testdiv, printData._testdivAvg);
            } else {
                svf.VrsOut("TOTAL_NUM" + printData._testdiv, printData._cnt);
                svf.VrsOut("CITY_NAME2", printData._toukeiCity);
                svf.VrsOut("TOTAL_PER" + printData._testdiv, printData._testdivAvg);
            }
            befPref = printData._toukeiPref;
            befDist = printData._finschoolDistcd;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndRecord();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        final String titleData = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + "入試　入学者出身校所在地別統計表";
        svf.VrsOut("TITLE", titleData);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TOTAL_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     V_BASE.ENTEXAMYEAR, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.JUDGEMENT = '1' ");
        stb.append("           AND BASE.ENTDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     V_BASE.ENTEXAMYEAR ");
        stb.append(" ), TOTAL_TESTDIV AS ( ");
        stb.append(" SELECT ");
        stb.append("     V_BASE.TESTDIV, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.JUDGEMENT = '1' ");
        stb.append("           AND BASE.ENTDIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     V_BASE.TESTDIV ");
        stb.append(" ), TOTAL_DIST AS ( ");
        stb.append(" SELECT ");
        stb.append("     V_BASE.ENTEXAMYEAR, ");
        stb.append("     '99' AS TESTDIV, ");
        stb.append("     '1' AS DATA_DIV, ");
        stb.append("     VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA') AS FINSCHOOL_DISTCD, ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS TOUKEI_PREF, ");
        stb.append("     VALUE(L001.NAME1, '登録なし') AS TOUKEI_CITY, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.JUDGEMENT = '1' ");
        stb.append("           AND BASE.ENTDIV = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     V_BASE.ENTEXAMYEAR, ");
        stb.append("     VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2), ");
        stb.append("     VALUE(L001.NAME1, '登録なし') ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     V_BASE.ENTEXAMYEAR, ");
        stb.append("     '99' AS TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS FINSCHOOL_DISTCD, ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS TOUKEI_PREF, ");
        stb.append("     VALUE(PREF.PREF_NAME, '登録なし') AS TOUKEI_CITY, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.JUDGEMENT = '1' ");
        stb.append("           AND BASE.ENTDIV = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN PREF_MST PREF ON SUBSTR(FSCHOOL.FINSCHOOL_DISTCD, 1, 2) = PREF.PREF_CD ");
        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEPT ON V_BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND V_BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("          AND V_BASE.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     V_BASE.ENTEXAMYEAR, ");
        stb.append("     V_BASE.TESTDIV, ");
        stb.append("     '2', ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2), ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2), ");
        stb.append("     VALUE(PREF.PREF_NAME, '登録なし') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS TOUKEI_PREF, ");
        stb.append("     VALUE(L001.NAME1, '登録なし') AS TOUKEI_CITY, ");
        stb.append("     V_BASE.TESTDIV, ");
        stb.append("     '1' AS DATA_DIV, ");
        stb.append("     VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA') AS FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS CNT, ");
        stb.append("     ROUND(COUNT(*) * 1.0 / TOTAL_TESTDIV.CNT, 4) * 100 AS TESTDIV_AVG ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.JUDGEMENT = '1' ");
        stb.append("           AND BASE.ENTDIV = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append("     LEFT JOIN TOTAL_TESTDIV ON V_BASE.TESTDIV = TOTAL_TESTDIV.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     V_BASE.TESTDIV, ");
        stb.append("     '1', ");
        stb.append("     VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2), ");
        stb.append("     VALUE(L001.NAME1, '登録なし'), ");
        stb.append("     TOTAL_TESTDIV.CNT ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS TOUKEI_PREF, ");
        stb.append("     VALUE(PREF.PREF_NAME, '登録なし') AS TOUKEI_CITY, ");
        stb.append("     V_BASE.TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS CNT, ");
        stb.append("     ROUND(COUNT(*) * 1.0 / TOTAL_TESTDIV.CNT, 4) * 100 AS TESTDIV_AVG ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.JUDGEMENT = '1' ");
        stb.append("           AND BASE.ENTDIV = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN PREF_MST PREF ON SUBSTR(FSCHOOL.FINSCHOOL_DISTCD, 1, 2) = PREF.PREF_CD ");
        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEPT ON V_BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND V_BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("          AND V_BASE.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("     LEFT JOIN TOTAL_TESTDIV ON V_BASE.TESTDIV = TOTAL_TESTDIV.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     V_BASE.TESTDIV, ");
        stb.append("     '2', ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2), ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2), ");
        stb.append("     VALUE(PREF.PREF_NAME, '登録なし'), ");
        stb.append("     TOTAL_TESTDIV.CNT ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     TOTAL_DIST.TOUKEI_PREF, ");
        stb.append("     TOTAL_DIST.TOUKEI_CITY, ");
        stb.append("     TOTAL_DIST.TESTDIV, ");
        stb.append("     TOTAL_DIST.DATA_DIV, ");
        stb.append("     TOTAL_DIST.FINSCHOOL_DISTCD AS FINSCHOOL_DISTCD, ");
        stb.append("     TOTAL_DIST.CNT, ");
        stb.append("     ROUND(TOTAL_DIST.CNT * 1.0 / TOTAL_T.CNT, 4) * 100 AS TESTDIV_AVG ");
        stb.append(" FROM ");
        stb.append("     TOTAL_DIST ");
        stb.append("     LEFT JOIN TOTAL_T ON TOTAL_DIST.ENTEXAMYEAR = TOTAL_T.ENTEXAMYEAR ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'ZZ' AS TOUKEI_PREF, ");
        stb.append("     '総合計' AS TOUKEI_CITY, ");
        stb.append("     TOTAL_TESTDIV.TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     'ZZZZ' AS FINSCHOOL_DISTCD, ");
        stb.append("     TOTAL_TESTDIV.CNT, ");
        stb.append("     ROUND(TOTAL_TESTDIV.CNT * 1.0 / TOTAL_TESTDIV.CNT, 4) * 100 AS TESTDIV_AVG ");
        stb.append(" FROM ");
        stb.append("     TOTAL_TESTDIV ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'ZZ' AS TOUKEI_PREF, ");
        stb.append("     '総合計' AS TOUKEI_CITY, ");
        stb.append("     '99' AS TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     'ZZZZ' AS FINSCHOOL_DISTCD, ");
        stb.append("     TOTAL_TESTDIV.CNT, ");
        stb.append("     ROUND(TOTAL_TESTDIV.CNT * 1.0 / TOTAL_TESTDIV.CNT, 4) * 100 AS TESTDIV_AVG ");
        stb.append(" FROM ");
        stb.append("     TOTAL_TESTDIV ");
        stb.append(" ORDER BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     DATA_DIV, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     TESTDIV ");

        return stb.toString();
    }

    private class PrintData {
        final String _toukeiPref;
        final String _toukeiCity;
        final String _testdiv;
        final String _dataDiv;
        final String _finschoolDistcd;
        final String _cnt;
        final String _testdivAvg;

        public PrintData(
                final String toukeiPref,
                final String toukeiCity,
                final String testdiv,
                final String dataDiv,
                final String finschoolDistcd,
                final String cnt,
                final String testdivAvg
        ) {
            _toukeiPref         = toukeiPref;
            _toukeiCity         = toukeiCity;
            _testdiv            = testdiv;
            _dataDiv            = dataDiv;
            _finschoolDistcd    = finschoolDistcd;
            _cnt                = cnt;
            BigDecimal bd = new BigDecimal(testdivAvg);
            BigDecimal bd2 = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
            _testdivAvg         = bd2.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63593 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _entexamyear;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _printLogStaffcd;

        private final String _applicantdivName;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _loginYear          = request.getParameter("LOGIN_YEAR");
            _loginSemester      = request.getParameter("LOGIN_SEMESTER");
            _loginDate          = request.getParameter("LOGIN_DATE");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
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

