/*
 * $Id: 2cf109a781bb69143a41cecc67ae10810c385afa $
 *
 * 作成日: 2017/10/31
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class KNJL506J_1 {

    private static final Log log = LogFactory.getLog(KNJL506J_1.class);
    private static final int MAXLINECNT = 50;

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
        svf.VrSetForm("KNJL506J_1.frm", 1);
        final Map printMap = getMap(db2);
        final int maxRow = MAXLINECNT;
        int pagecnt = 1;

        //優先する科目順に出力する。
        for (Iterator itr = _param._testSubclassMap.keySet().iterator(); itr.hasNext();) {
        	String kstr = (String)itr.next();
        	List printList = (List)printMap.get(kstr);
        	if (printList == null || printList.size() == 0) {
        		continue;
        	}

        	int row = 1;
	        setTitle(db2, svf, pagecnt, printList, kstr);//ヘッダ
	        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
	            final PrintData putwk = (PrintData) iterator.next();

	            if (row > maxRow) {
	                svf.VrEndPage();
	                pagecnt++;
	                setTitle(db2, svf, pagecnt, printList, kstr);//ヘッダ
	                row = 1;
	            }

	            svf.VrsOutn("EXAM_NO", row, putwk._receptNo); // 受験番号
	            svf.VrsOutn("REF_NO", row, putwk._orderNo);   // 整理番号
	            svf.VrsOutn("NAME", row, putwk._name);        // 氏名
	            svf.VrsOutn("SCORE", row, putwk._score);      // 得点

	            row++;
	            _hasData = true;
	        }
	        if (_hasData) {
	            svf.VrEndPage();
	        }
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final int pagecnt, final List printList, final String subClassCd) {
        //ヘッダ、タイトル
        String setYear = KNJ_EditDate.h_format_Seireki_N(_param._entexamyear + "-04-01");
        svf.VrsOut("TITLE", setYear + "度 チェックリスト");
        //DATE
        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        svf.VrsOut("DATE", sdf.format(cl.getTime()));
        //ページ情報
        svf.VrsOut("PAGE", String.valueOf(pagecnt));  // + "/" + String.valueOf(Math.ceil(printList.size() / MAXLINECNT)));
        final String addWkStr = !"".equals(_param._examtypeName) ? " " + _param._examtypeName : "";
        String subjectName = _param._applicantdivName + " " + _param._testdivName + addWkStr + " " + (String)_param._testSubclassMap.get(subClassCd);
        svf.VrsOut("SUBTITLE", subjectName);
    }

    private Map getMap(final DB2UDB db2) {
        final Map retMap = new LinkedMap(); // 出力データ保持用。key:EXAMTYPE-TESTSUBCLASSCD、data:ArrayList(PrintData)
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String receptNo = rs.getString("RECEPTNO");
            	final String orderNo = rs.getString("ORDERNO");
            	final String name = rs.getString("NAME");
            	final String score = rs.getString("SCORE");

            	final String kstr = rs.getString("SUBCLASSCD");
            	List addlistwk = (List)retMap.get(kstr);
            	if (addlistwk == null) {
            		addlistwk = new ArrayList();
            		retMap.put(kstr, addlistwk);
            	}

                final PrintData printData = new PrintData(receptNo, orderNo, name, score);
                addlistwk.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T13.SUBCLASSCD, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T3_009.REMARK1 AS ORDERNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T12.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3_009 ");
        stb.append("    ON T3_009.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T3_009.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T3_009.TESTDIV = T1.TESTDIV ");
        stb.append("   AND T3_009.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("   AND T3_009.RECEPTNO = T1.RECEPTNO ");
        stb.append("   AND T3_009.SEQ = '009' ");
        stb.append("  LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST T13");
    	stb.append("    ON T13.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
    	stb.append("   AND T13.APPLICANTDIV = T1.APPLICANTDIV ");
    	stb.append("   AND T13.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT T12 ");
        stb.append("    ON T12.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T12.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T12.TESTDIV = T1.TESTDIV ");
        stb.append("   AND T12.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("   AND T12.RECEPTNO = T1.RECEPTNO ");
    	stb.append("   AND T12.ENTEXAMYEAR = T13.ENTEXAMYEAR ");
    	stb.append("   AND T12.APPLICANTDIV = T13.APPLICANTDIV ");
    	stb.append("   AND T12.EXAM_TYPE = T13.EXAM_TYPE ");
    	stb.append("   AND T12.TESTSUBCLASSCD = T13.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("  AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        stb.append("  AND T1.TESTDIV = '"+_param._testdiv+"' ");
        if (!"".equals(_param._examtype)) {
            stb.append("  AND T1.EXAM_TYPE = '"+_param._examtype+"' ");
        }
        stb.append(" ORDER BY ");
        stb.append("   T13.SUBCLASSCD, ");
        if ("1".equals(_param._sort1)) {
            stb.append("     T3_009.REMARK1, ");
        }
        stb.append("     T1.RECEPTNO ");

        return stb.toString();
    }

    private class PrintData {

    	final String _receptNo;
    	final String _orderNo;
    	final String _name;
    	final String _score;
    	public PrintData (final String receptNo, final String orderNo, final String name, final String score)
    	{
    	    _receptNo = receptNo;
    	    _orderNo = orderNo;
    	    _name = name;
    	    _score = score;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67936 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _applicantdivName;
        final String _testdiv;
        final String _testdivName;
        final String _examtype;
        final String _loginDate;
        final String _sort1;
        final Map _testSubclassMap;
        final String _examtypeName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _examtype = StringUtils.defaultString(request.getParameter("EXAM_TYPE"), "");

            _applicantdivName = getNameMst(db2, "L003", _applicantdiv);
            _testdivName = getTestdivNameMst(db2);
            _loginDate = request.getParameter("LOGIN_DATE");
            _sort1 = request.getParameter("SORT1");

            _testSubclassMap = getSortTestSubclassMap(db2);
            _examtypeName = getExamTypeNameMst(db2);
        }

        //集計する科目を優先的に順番を先にするよう、SQLで科目順番を制御。個数を取ってはいるが、個数で優先順位は変えていない。
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

        private String getExamTypeNameMst(final DB2UDB db2) {
            String retStr = "";
            if ("".equals(StringUtils.defaultString(_examtype, "")) ) {
                return retStr;
            }
            StringBuffer stb = new StringBuffer();

            stb.append(" SELECT DISTINCT ");
            stb.append("     EXAMTYPE_NAME_ABBV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_EXAMTYPE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR    = '" + _entexamyear + "' AND ");
            stb.append("     APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append(" AND EXAM_TYPE = '" + _examtype + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("EXAMTYPE_NAME_ABBV");
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
