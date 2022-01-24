/*
 * $Id: 93df5b479fe59128103e653aab8194f398fe9dd3 $
 *
 * 作成日: 2017/10/30
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJL507J_3 {

    private static final Log log = LogFactory.getLog(KNJL507J_3.class);

    private boolean _hasData;

    private Param _param;

    private String bithdayField;

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
        svf.VrSetForm("KNJL507J_3.frm", 1);
        final List printList = getList(db2);
        final int maxCnt = 29;
        final int maxCol = 3;
        int printLine = 1;
        int printCol = 1;

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (printLine > maxCnt && printCol >= maxCol) {
                svf.VrEndPage();
                svf.VrSetForm("KNJL507J_3.frm", 1);
                printCol = 1;
                printLine = 1;
            } else if (printLine > maxCnt) {
                printLine = 1;
                printCol++;
            }

            if(printLine % 6 == 0) {
                //5行ごとに空行を設定
                svf.VrsOutn("EXAM_NO" + printCol, printLine, "");
                printLine = printLine + 1;
            }

            final PrintData printData = (PrintData) iterator.next();
            //受験番号
            svf.VrsOutn("EXAM_NO" + String.valueOf(printCol), printLine, printData._receptNo);
            printLine = printLine + 1;

            _hasData = true;
        }

        svf.VrEndPage();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");

                final PrintData printData = new PrintData(receptNo);
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("  T1.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN NAME_MST T3_L013 ");
        stb.append("    ON T3_L013.NAMECD1 = 'L013' ");
        stb.append("   AND T3_L013.NAMECD2 = T1.JUDGEDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("     AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        if (!"".equals(_param._testdiv)) {
            stb.append("     AND T1.TESTDIV = '"+_param._testdiv+"' ");
        }
        stb.append("     AND T3_L013.NAMESPARE1 = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPTNO ");
        return stb.toString();
    }

    private class PrintData {
        final String _receptNo;

        public PrintData(
                final String receptNo
        ) {
            _receptNo = receptNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67882 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _applicantdivName;
        final String _testdiv;
        final String _testdivName;
        final String _loginDate;
        final Map _testSubclassMap;
        final Map _testdivMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");

            _applicantdivName = getNameMst(db2, "L003", _applicantdiv);
            _testdivName = getTestdivNameMst(db2);
            _loginDate = request.getParameter("LOGIN_DATE");

            _testSubclassMap = getSortTestSubclassMap(db2);
            _testdivMap = getSortTestDiv(db2);
        }

        private Map getSortTestDiv(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTDIV_NAME, ");
            stb.append("     TESTDIV_ABBV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTDIV_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '"+_entexamyear+"' ");
            stb.append("     AND APPLICANTDIV = '"+_applicantdiv+"' ");
            stb.append(" ORDER BY ");
            stb.append("     TEST_DATE, ");
            stb.append("     TESTDIV ");
            stb.append(" FETCH FIRST 7 ROWS ONLY");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("TESTDIV"), rs.getString("TESTDIV_ABBV"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retMap;
        }

        private Map getSortTestSubclassMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST AS ( ");
            stb.append("     SELECT ");
            stb.append("       SUBCLASSCD, ");
            stb.append("       SUM(CASE WHEN JUDGE_SUMMARY = '1' THEN 1 ELSE 0 END) AS CNTFLG ");
            stb.append("     FROM ");
            stb.append("       ENTEXAM_EXAMTYPE_SUBCLASS_MST ");
            stb.append("     WHERE ");
            stb.append("       ENTEXAMYEAR = '"+_entexamyear+"' ");
            stb.append("       AND APPLICANTDIV = '"+_applicantdiv+"' ");
            stb.append("     GROUP BY ");
            stb.append("       SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T2_L009.NAME1, ");
            stb.append("   CASE WHEN T1.CNTFLG > 0 THEN 1 ELSE 0 END AS GRPFLG ");
            stb.append(" FROM ");
            stb.append("   GROUPING_MERGE_EXAMTYPE_SUBCLASS_MST T1 ");
            stb.append("   LEFT JOIN NAME_MST T2_L009 ");
            stb.append("     ON T2_L009.NAMECD1 = 'L009' ");
            stb.append("    AND T2_L009.NAMECD2 = T1.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("   GRPFLG DESC, ");
            stb.append("   T1.SUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("SUBCLASSCD"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retMap;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();

            stb.append(" SELECT DISTINCT ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR    = '" + _entexamyear + "' AND ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
                stb.append(" AND NAMECD2 = '" + namecd2 + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

        private String getTestdivNameMst(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();

            stb.append(" SELECT DISTINCT ");
            stb.append("     TESTDIV_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTDIV_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR    = '" + _entexamyear + "' AND ");
            stb.append("     APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append(" AND TESTDIV = '" + _testdiv + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("TESTDIV_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }
    }
}

// eof

