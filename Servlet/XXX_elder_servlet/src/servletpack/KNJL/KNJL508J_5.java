/*
 * $Id: 1510c3d3b8e701cb17ceb5996d1be3a9c1e5d5af $
 *
 * 作成日: 2015/09/08
 * 作成者: nakamoto
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３００Ｎ＞  座席ラベル
 **/
public class KNJL508J_5 {

    private static final Log log = LogFactory.getLog(KNJL508J_5.class);
    private static final int MAXLINECNT = 25;

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

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        // *** ここから入学者データの抽出。L508_1でも同じ処理をするため、変更は注意。 ***
        stb.append(" WITH SRCHPASSDATA_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.APPLICANTDIV, ");
        stb.append("  T1.TESTDIV, ");
        stb.append("  T1.EXAM_TYPE, ");
        stb.append("  T1.RECEPTNO, ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  T1.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN NAME_MST T3 ");
        stb.append("    ON T3.NAMECD2 = T1.JUDGEDIV ");
        stb.append("   AND T3.NAMECD1 = 'L013' ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("  AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        if (!"".equals(_param._testdiv)) {
            stb.append("     AND T1.TESTDIV = '"+_param._testdiv+"' ");
        }
        stb.append("  AND T3.NAMESPARE1 = '1' ");
        stb.append("  AND T1.ADJOURNMENTDIV = '1' ");
        stb.append("  AND T1.PROCEDUREDIV1 = '1' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("  T1.ENTEXAMYEAR, ");
        stb.append("  T1.APPLICANTDIV, ");
        stb.append("  T1.TESTDIV, ");
        stb.append("  T1.EXAM_TYPE, ");
        stb.append("  T1.RECEPTNO, ");
        stb.append("  T1.EXAMNO, ");
        stb.append("  T3.NAME, ");
        stb.append("  T4.FINSCHOOL_NAME, ");
        stb.append("  CASE WHEN T1.JUDGEDIV = '4' THEN '' ");
        stb.append("       WHEN T1.EXAMNO IS NOT NULL THEN '〇' ");
        stb.append("       ELSE '' END AS JUKEN, ");
        stb.append("  CASE WHEN T1.JUDGEDIV = '1' THEN '合'  ");
        stb.append("       WHEN T1.JUDGEDIV = '2' THEN '×' ");
        stb.append("       ELSE '' END AS PASS_UNPASS ");
        stb.append(" FROM ");
        stb.append("  SRCHPASSDATA_TBL T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ");
        stb.append("     ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN FINSCHOOL_MST T4 ");
        stb.append("    ON T4.FINSCHOOLCD = T3.FS_CD ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("  AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPTNO ");
        return stb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL508J_5.frm", 1);
        final List printList = getList(db2);
        final int maxRow = MAXLINECNT;
        int row = 1;
        int pagecnt = 1;

        setTitle(db2, svf, pagecnt, printList);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData putwk = (PrintData) iterator.next();

            if (row > maxRow) {
                svf.VrEndPage();
                pagecnt++;
                setTitle(db2, svf, pagecnt, printList);//ヘッダ
                row = 1;
            }

            svf.VrsOutn("EXAM_NO", row, putwk._receptNo); // 受験番号
            final int nlen = KNJ_EditEdit.getMS932ByteLength(putwk._name);
            final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" :"1";
            svf.VrsOutn("NAME"+nfield, row, putwk._name); // 氏名
            svf.VrsOutn("FINSCHOOL_NAME", row, putwk._finSchoolName);   // 出身学校名
            svf.VrsOutn("EXAM", row, putwk._juken); // 受験
            svf.VrsOutn("JUDGE", row, putwk._passunpass); // 合否

            row++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final int pagecnt, final List printList) {
        //ヘッダ、タイトル
    	String setYear = KNJ_EditDate.h_format_Seireki_N(_param._entexamyear + "-04-01");
        String setTestDivName = "".equals(_param._testdivName) ? "" : " " + _param._testdivName;
        svf.VrsOut("TITLE", setYear + "度 " + _param._applicantdivName + setTestDivName + " 受付簿");
        //ページ情報
        svf.VrsOut("PAGE", String.valueOf(pagecnt));
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String receptNo = rs.getString("RECEPTNO");
            	final String name = rs.getString("NAME");
            	final String juken = rs.getString("JUKEN");
            	final String passunpass = rs.getString("PASS_UNPASS");
            	final String finSchoolName = rs.getString("FINSCHOOL_NAME");

                final PrintData printData = new PrintData(receptNo, name, juken, passunpass, finSchoolName);
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

    private class PrintData {
        final String _receptNo;
        final String _name;
        final String _juken;
        final String _passunpass;
        final String _finSchoolName;
        public PrintData (final String receptNo, final String name, final String juken, final String passunpass, final String finSchoolName)
        {
            _receptNo = receptNo;
            _name = name;
            _juken = juken;
            _passunpass = passunpass;
            _finSchoolName = finSchoolName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70876 $");
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
        final Map _testdivMap;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");

            _applicantdivName = getNameMst(db2, "L003", _applicantdiv);
            _testdivName = getTestdivNameMst(db2);
            _testdivMap = getSortTestDiv(db2);
            _loginDate = request.getParameter("LOGIN_DATE");
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
            stb.append("     ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append(" AND APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append(" AND TESTDIV = '" + _testdiv + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("TESTDIV_NAME"));
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

