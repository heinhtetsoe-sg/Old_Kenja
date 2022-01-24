/*
 * $Id: c0c5934a17461e82f61acfe676f68c797888e112 $
 *
 * 作成日: 2016/11/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

public class KNJL343F {

    private static final Log log = LogFactory.getLog(KNJL343F.class);

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
                final String fsCd = rs.getString("FS_CD");
                final String schoolName = rs.getString("FINSCHOOL_NAME");
                final String finseturitu = rs.getString("FINSETURITU");
                final String toukeiCity = rs.getString("TOUKEI_CITY");
                final String testdiv = rs.getString("TESTDIV");
                final String dataDiv = rs.getString("DATA_DIV");
                final String finschoolDistcd = rs.getString("FINSCHOOL_DISTCD");
                final String siganCnt = rs.getString("SIGAN_CNT");
                final String passCnt = rs.getString("PASS_CNT");
                final String entCnt = rs.getString("ENT_CNT");

                final PrintData printData = new PrintData(toukeiPref, fsCd, schoolName, finseturitu, toukeiCity, testdiv, dataDiv, finschoolDistcd, siganCnt, passCnt, entCnt);
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
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            svf.VrSetForm("KNJL343F_J.frm", 4);
        } else {
            svf.VrSetForm("KNJL343F_H.frm", 4);
        }
        setTitle(svf);
        final List list = getList(db2);
        String befPref = "";
        String befFsCd = "";
        String befDist = "";
        List spltDivList = Arrays.asList(StringUtils.split(_param._testDivInState, ','));
        for (int cnt = 0;cnt < spltDivList.size();cnt++) {
        	spltDivList.set(cnt, ((String)spltDivList.get(cnt)).trim());
        }
        for (int line = 0; line < list.size(); line++) {
            final PrintData printData = (PrintData) list.get(line);
            if (!"".equals(befPref) && !befPref.equals(printData._toukeiPref)
                ||
                !"".equals(befFsCd) && !befFsCd.equals(printData._fsCd)
                ||
                !"".equals(befDist) && !befDist.equals(printData._finschoolDistcd)
            ) {
                svf.VrEndRecord();
            }
            if ("1".equals(printData._dataDiv)) {
                if ("99".equals(printData._testdiv)) {
                    svf.VrsOut("SUBTOTAL_ENT", printData._entCnt);
                } else {
                    svf.VrsOut("CITY_NAME", printData._toukeiCity);
                    svf.VrsOut("SCHOOL_DUV", printData._finseturitu);
                    svf.VrsOut("SCHOOL_NAME", printData._schoolName);
                    if (SCHOOL_J.equals(_param._applicantdiv)) {
	                    if (spltDivList.contains("'" + printData._testdiv + "'")) {
	                    	final int ntdiv = spltDivList.indexOf("'" + printData._testdiv + "'") + 1;
	                        svf.VrsOut("HOPE" + ntdiv, printData._siganCnt);
	                        svf.VrsOut("PASS" + ntdiv, printData._passCnt);
	                        svf.VrsOut("ENT" + ntdiv, printData._entCnt);
	                    }
                    } else {
                        svf.VrsOut("HOPE" + printData._testdiv, printData._siganCnt);
                        svf.VrsOut("PASS" + printData._testdiv, printData._passCnt);
                        svf.VrsOut("ENT" + printData._testdiv, printData._entCnt);
                    }
                }
            } else {
                if ("99".equals(printData._testdiv)) {
                    svf.VrsOut("TOTAL_ENT", printData._entCnt);
                } else {
                    svf.VrsOut("TOTAL_NAME", printData._schoolName);
                    if (SCHOOL_J.equals(_param._applicantdiv)) {
	                    if (spltDivList.contains("'" + printData._testdiv + "'")) {
	                    	final int ntdiv = spltDivList.indexOf("'" + printData._testdiv + "'") + 1;
	                        svf.VrsOut("TOTAL_HOPE" + ntdiv, printData._siganCnt);
	                        svf.VrsOut("TOTAL_PASS" + ntdiv, printData._passCnt);
	                        svf.VrsOut("TOTAL_ENT" + ntdiv, printData._entCnt);
	                    }
                    } else {
                        svf.VrsOut("TOTAL_HOPE" + printData._testdiv, printData._siganCnt);
                        svf.VrsOut("TOTAL_PASS" + printData._testdiv, printData._passCnt);
                        svf.VrsOut("TOTAL_ENT" + printData._testdiv, printData._entCnt);
                    }
                }
            }
            befPref = printData._toukeiPref;
            befFsCd = printData._fsCd;
            befDist = printData._finschoolDistcd;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndRecord();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        final String titleData = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + "入試";
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            svf.VrsOut("TITLE", titleData + "　小学校別入試応募一覧表");
        } else {
            svf.VrsOut("TITLE", titleData + "　中学校別入試応募一覧表");
        }
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TMP AS ( ");
        stb.append(" SELECT ");
        stb.append("     SUBSTR(VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA'), 1, 2) AS TOUKEI_PREF, ");
        stb.append("     VALUE(BASE.FS_CD, 'AAAAAA') AS FS_CD, ");
        stb.append("     VALUE(FSCHOOL.FINSCHOOL_NAME, '登録なし') AS FINSCHOOL_NAME, ");
        stb.append("     VALUE(L015.NAME1, 'なし') AS FINSETURITU, ");
        stb.append("     VALUE(L001.NAME1, '登録なし') AS TOUKEI_CITY, ");
        if ("2".equals(_param._applicantdiv)) {
            stb.append("     CASE WHEN V_BASE.TESTDIV = '5' THEN '4' ELSE V_BASE.TESTDIV END AS TESTDIV, "); // 帰国生A、帰国生Bを帰国生に表示
        } else {
            stb.append("     V_BASE.TESTDIV, ");
        }
        stb.append("     VALUE(FSCHOOL.FINSCHOOL_DISTCD, 'AAAA') AS FINSCHOOL_DISTCD, ");
        stb.append("     VALUE(PREF.PREF_NAME, L001.ABBV1, '登録なし') AS PREF_NAME, ");
        stb.append("     BASE.JUDGEMENT, ");
        stb.append("     BASE.ENTDIV, ");
        stb.append("     RECEPT.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON V_BASE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND V_BASE.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND RECEPT.TESTDIV = V_BASE.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND RECEPT.RECEPTNO = V_BASE.RECEPTNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L015 ON L015.NAMECD1 = 'L015' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DIV = L015.NAMECD2 ");
        stb.append("     LEFT JOIN PREF_MST PREF ON SUBSTR(FSCHOOL.FINSCHOOL_DISTCD, 1, 2) = PREF.PREF_CD ");
        stb.append(" WHERE ");
        stb.append("     V_BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND V_BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND V_BASE.TESTDIV IN (" + _param._testDivInState + ") ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     AND (V_BASE.TESTDIV <> '5' OR V_BASE.TESTDIV = '5' AND VALUE(BASE.GENERAL_FLG, '') <> '1') ");
        } else if ("2".equals(_param._applicantdiv)) {
            stb.append("     AND (V_BASE.TESTDIV <> '3' OR V_BASE.TESTDIV = '3' AND VALUE(BASE.GENERAL_FLG, '') <> '1') ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     FS_CD, ");
        stb.append("     FINSCHOOL_NAME, ");
        stb.append("     FINSETURITU, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     int(TESTDIV) as TESTDIV, ");
        stb.append("     '1' AS DATA_DIV, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     FS_CD, ");
        stb.append("     FINSCHOOL_NAME, ");
        stb.append("     FINSETURITU, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     TESTDIV ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     FS_CD, ");
        stb.append("     FINSCHOOL_NAME, ");
        stb.append("     FINSETURITU, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     99 AS TESTDIV, ");
        stb.append("     '1' AS DATA_DIV, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     FS_CD, ");
        stb.append("     FINSCHOOL_NAME, ");
        stb.append("     FINSETURITU, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     FINSCHOOL_DISTCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     'AAAAAA' AS FS_CD, ");
        stb.append("     '小　計' AS FINSCHOOL_NAME, ");
        stb.append("     '' AS FINSETURITU, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     int(TESTDIV) as TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     TESTDIV ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     'AAAAAA' AS FS_CD, ");
        stb.append("     '小　計' AS FINSCHOOL_NAME, ");
        stb.append("     '' AS FINSETURITU, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     99 AS TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     TOUKEI_CITY, ");
        stb.append("     FINSCHOOL_DISTCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     'BBBBBB' AS FS_CD, ");
        stb.append("     PREF_NAME || '　計' AS FINSCHOOL_NAME, ");
        stb.append("     '' AS FINSETURITU, ");
        stb.append("     PREF_NAME AS TOUKEI_CITY, ");
        stb.append("     int(TESTDIV) as TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     'BBBBBB' AS FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     PREF_NAME, ");
        stb.append("     TESTDIV ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     'BBBBBB' AS FS_CD, ");
        stb.append("     PREF_NAME || '　計' AS FINSCHOOL_NAME, ");
        stb.append("     '' AS FINSETURITU, ");
        stb.append("     PREF_NAME AS TOUKEI_CITY, ");
        stb.append("     99 AS TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     'BBBBBB' AS FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     PREF_NAME ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'CCCCCC' AS TOUKEI_PREF, ");
        stb.append("     'CCCCCC' AS FS_CD, ");
        stb.append("     '総　合　計' AS FINSCHOOL_NAME, ");
        stb.append("     '' AS FINSETURITU, ");
        stb.append("     '' AS TOUKEI_CITY, ");
        stb.append("     int(TESTDIV) as TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     'CCCCCC' AS FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" GROUP BY ");
        stb.append("     TESTDIV ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'CCCCCC' AS TOUKEI_PREF, ");
        stb.append("     'CCCCCC' AS FS_CD, ");
        stb.append("     '総　合　計' AS FINSCHOOL_NAME, ");
        stb.append("     '' AS FINSETURITU, ");
        stb.append("     '' AS TOUKEI_CITY, ");
        stb.append("     99 AS TESTDIV, ");
        stb.append("     '2' AS DATA_DIV, ");
        stb.append("     'CCCCCC' AS FINSCHOOL_DISTCD, ");
        stb.append("     COUNT(*) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN JUDGEDIV = '1' AND ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM TMP ");
        stb.append(" ORDER BY ");
        stb.append("     TOUKEI_PREF, ");
        stb.append("     FINSCHOOL_DISTCD, ");
        stb.append("     DATA_DIV, ");
        stb.append("     FS_CD, ");
        stb.append("     int(TESTDIV) ");

        return stb.toString();
    }

    private class PrintData {
        final String _toukeiPref;
        final String _fsCd;
        final String _schoolName;
        final String _finseturitu;
        final String _toukeiCity;
        final String _testdiv;
        final String _dataDiv;
        final String _finschoolDistcd;
        final String _siganCnt;
        final String _passCnt;
        final String _entCnt;

        public PrintData(
                final String toukeiPref,
                final String fsCd,
                final String schoolName,
                final String finseturitu,
                final String toukeiCity,
                final String testdiv,
                final String dataDiv,
                final String finschoolDistcd,
                final String siganCnt,
                final String passCnt,
                final String entCnt
        ) {
            _toukeiPref         = toukeiPref;
            _fsCd               = fsCd;
            _schoolName         = schoolName;
            _finseturitu        = finseturitu;
            _toukeiCity         = toukeiCity;
            _testdiv            = testdiv;
            _dataDiv            = dataDiv;
            _finschoolDistcd    = finschoolDistcd;
            _siganCnt           = siganCnt;
            _passCnt            = passCnt;
            _entCnt             = entCnt;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70833 $");
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
        private final String _testDivInState;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _loginYear          = request.getParameter("LOGIN_YEAR");
            _loginSemester      = request.getParameter("LOGIN_SEMESTER");
            _loginDate          = request.getParameter("LOGIN_DATE");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testDivInState = SCHOOL_J.equals(_applicantdiv) ? "'1', '16', '2', '3', '5', '17'" : "'1', '2', '3', '4', '5'";
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

