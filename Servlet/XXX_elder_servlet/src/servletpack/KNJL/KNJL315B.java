/*
 * $Id: 00a240a9691479c1ea8c96b4a83f152057eb8ca3 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１５Ｂ＞  得点分布表
 **/
public class KNJL315B {

    private static final Log log = LogFactory.getLog(KNJL315B.class);

    private static final String TESTSUBCLASSCD_SOGO = "9";
    private static final int KAMOKU3 = 1;
    private static final int KAMOKU5 = 2;
    
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

            final int printCnt = TESTSUBCLASSCD_SOGO.equals(_param._testsubclasscd) ? 2 : 1;
            for (int i = 1; i <= printCnt; i++) {
                printMain(db2, svf, i);
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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf, final int printCnt) {
        
        final String form = "KNJL315B.frm";
        svf.VrSetForm(form, 1);
        final int maxLine = 70;
        
        final SubclassScoreTotal total = SubclassScoreTotal.getTotal(db2, _param, printCnt);
        final Map subclassScoreMap = SubclassScore.load(db2, _param, printCnt);

        log.debug(" total = " + total);
        final String title = TESTSUBCLASSCD_SOGO.equals(_param._testsubclasscd) ? "総合計得点分布表" : "科目別得点分布表";
        svf.VrsOut("TITLE", title); // タイトル
        svf.VrsOut("DATE", _param._dateStr); // 印刷日
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
        svf.VrsOut("KIND", _param._testdivAbbv1); // 入試制度
        // 科目
        if (TESTSUBCLASSCD_SOGO.equals(_param._testsubclasscd)) {
            svf.VrsOut("SUBJECT", printCnt == KAMOKU3 ? "3科目" : "5科目");
        } else {
            svf.VrsOut("SUBJECT", _param._subclassName);
        }

        int subTotal1 = 0;
        int subTotal2 = 0;
        int subTotal9 = 0;
        boolean printUnder30 = false;
        for (int j = 0; j < maxLine; j++) {
            final int iScore = _param._subclassPerfect - j;
            if (iScore < 0) {
                break;
            }
            
            final String scoreStr;
            final SubclassScore subScore;
            if (iScore < 30) {
                if (printUnder30) {
                    break;
                }
                scoreStr = "30点未満";
                int count1 = 0;
                int count2 = 0;
                int countTotal = 0;
                for (int s = iScore; s >= 0; s--) {
                    final SubclassScore ssubScore = (SubclassScore) subclassScoreMap.get(String.valueOf(s));
                    if (null == ssubScore) {
                        continue;
                    }
                    count1 += Integer.parseInt(NumberUtils.isDigits(ssubScore._count1) ? ssubScore._count1 : "0");
                    count2 += Integer.parseInt(NumberUtils.isDigits(ssubScore._count2) ? ssubScore._count2 : "0");
                    countTotal += Integer.parseInt(NumberUtils.isDigits(ssubScore._countTotal) ? ssubScore._countTotal : "0");
                }
                String sCount1 = 0 == count1 ? null : String.valueOf(count1);
                String sCount2 = 0 == count2 ? null : String.valueOf(count2);
                String sCountTotal = 0 == countTotal ? null : String.valueOf(countTotal);
                subScore = new SubclassScore(null, sCount1, sCount2, sCountTotal);
                printUnder30 = true;
            } else if (iScore == _param._subclassPerfect) {
                scoreStr = String.valueOf(iScore) + "点以上";
                int count1 = 0;
                int count2 = 0;
                int countTotal = 0;
                for (int s = _param._subclassPerfect; s <= 100; s++) {
                    final SubclassScore ssubScore = (SubclassScore) subclassScoreMap.get(String.valueOf(s));
                    if (null == ssubScore) {
                        continue;
                    }
                    count1 += Integer.parseInt(NumberUtils.isDigits(ssubScore._count1) ? ssubScore._count1 : "0");
                    count2 += Integer.parseInt(NumberUtils.isDigits(ssubScore._count2) ? ssubScore._count2 : "0");
                    countTotal += Integer.parseInt(NumberUtils.isDigits(ssubScore._countTotal) ? ssubScore._countTotal : "0");
                }
                String sCount1 = 0 == count1 ? null : String.valueOf(count1);
                String sCount2 = 0 == count2 ? null : String.valueOf(count2);
                String sCountTotal = 0 == countTotal ? null : String.valueOf(countTotal);
                subScore = new SubclassScore(null, sCount1, sCount2, sCountTotal);
            } else {
                scoreStr = String.valueOf(iScore) + "点台";
                subScore = (SubclassScore) subclassScoreMap.get(String.valueOf(iScore));
            }
            // log.debug(" score = " + iScore + ", subScore = " + subScore);
            final String k = j < 35 ? "1" : "2";
            final int line = j + 1 + (j < 35 ? 0 : -35);
            svf.VrsOutn("SCORE" + k, line, scoreStr); // 得点
            if (null == subScore) {
                continue;
            }
            
            svf.VrsOutn("BOY" + k, line, subScore._count1); // 男子
            if (null != subScore._count1) {
                subTotal1 += Integer.parseInt(subScore._count1);
                svf.VrsOutn("BOY_SUBTOTAL" + k, line, String.valueOf(subTotal1)); // 男子
            }
            svf.VrsOutn("GIRL" + k, line, subScore._count2); // 女子
            if (null != subScore._count2) {
                subTotal2 += Integer.parseInt(subScore._count2);
                svf.VrsOutn("GIRL_SUBTOTAL" + k, line, String.valueOf(subTotal2)); // 女子
            }
            svf.VrsOutn("SUBTOTAL" + k, line, subScore._countTotal); // 合計
            if (null != subScore._countTotal) {
                svf.VrsOutn("PER_SUBTOTAL" + k, line, percent(String.valueOf(subScore._countTotal), total._count)); // 合計割合
                subTotal9 += Integer.parseInt(subScore._countTotal);
                svf.VrsOutn("TOTAL" + k, line, String.valueOf(subTotal9)); // 合計
                svf.VrsOutn("PER_TOTAL" + k, line, percent(String.valueOf(subTotal9), total._count)); // 合計割合
            }
        }
        svf.VrsOut("ALL_TOTAL", total._count); // 受験者数
        svf.VrsOut("AVERAGE", getSishagonyu(total._avg)); // 平均点
        svf.VrsOut("DEV", getSishagonyu(total._stddev)); // 標準偏差
        svf.VrEndPage();
        _hasData = true;
    }
    
    private String percent(final String count, final String total) {
        if (!NumberUtils.isDigits(count) || !NumberUtils.isDigits(total)) {
            return null;
        }
        final int iCount = Integer.parseInt(count);
        final int iTotal = Integer.parseInt(total);
        if (iTotal <= 0) {
            return null;
        }
        final String s = new BigDecimal(100 * iCount).divide(new BigDecimal(iTotal), 0, BigDecimal.ROUND_FLOOR).toString();
        return s + "%";
    }
    
    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static String getSishagonyu(final String o) {
        if (null == o) {
            return null;
        }
        return new BigDecimal(o).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static class SubclassScore {
        final String _score;
        final String _count1;
        final String _count2;
        final String _countTotal;

        SubclassScore(
            final String score,
            final String count1,
            final String count2,
            final String countTotal
        ) {
            _score = score;
            _count1 = count1;
            _count2 = count2;
            _countTotal = countTotal;
        }

        public static Map load(final DB2UDB db2, final Param param, final int printCnt) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, 0, printCnt);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String score = rs.getString("SCORE");
                    final String count1 = rs.getString("COUNT1");
                    final String count2 = rs.getString("COUNT2");
                    final String countTotal = rs.getString("COUNT_TOTAL");
                    final SubclassScore subclassscore = new SubclassScore(score, count1, count2, countTotal);
                    map.put(score, subclassscore);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
        
        public String toString() {
            return "SubclassScore(score = " + _score + ", count1 = " + _count1 + ", count2 = " + _count2 + ", total = " + _countTotal + ")";
        }
    }
    
    private static class SubclassScoreTotal {
        final String _sum;
        final String _count;
        final String _avg;
        final String _stddev;

        SubclassScoreTotal(
            final String sum,
            final String count,
            final String avg,
            final String stddev
        ) {
            _sum = sum;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        private static SubclassScoreTotal getTotal(final DB2UDB db2, final Param param, final int printCnt) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            SubclassScoreTotal total = null;
            try {
                final String sql = sql(param, 1, printCnt);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sum = rs.getString("SUM");
                    final String count = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String stddev = rs.getString("STDDEV");
                    total = new SubclassScoreTotal(sum, count, avg, stddev);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return total;
        }
        
        public String toString() {
            return "Total(sum = " + _sum + ", count = " + _count + ", avg = " + _avg + ", stddev = " + _stddev + ")";
        }
    }

    private static String sql(final Param param, final int flg, final int printCnt) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCORES AS ( ");
        stb.append("     SELECT ");
        stb.append("         RECEPT.EXAMNO, ");
        stb.append("         BASE.SEX, ");
        if (TESTSUBCLASSCD_SOGO.equals(param._testsubclasscd)) {
            if (printCnt == KAMOKU3) {
                stb.append("         RECEPT.AVARAGE3 AS SCORE ");
            } else {
                stb.append("         RECEPT.AVARAGE1 AS SCORE ");
            }
        } else {
            stb.append("         TSCORE.SCORE ");
        }
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("         INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("             AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("             AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("             AND BASE.EXAMNO = RECEPT.EXAMNO ");
        if (!TESTSUBCLASSCD_SOGO.equals(param._testsubclasscd)) {
            stb.append("         INNER JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("             AND TSCORE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("             AND TSCORE.TESTDIV = RECEPT.TESTDIV ");
            stb.append("             AND TSCORE.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("             AND TSCORE.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("             AND TSCORE.TESTSUBCLASSCD = '" + param._testsubclasscd + "' ");
        }
        stb.append("     WHERE ");
        stb.append("         RECEPT.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("         AND RECEPT.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("         AND RECEPT.TESTDIV = '" + param._testdiv + "' ");
        stb.append("         AND RECEPT.EXAM_TYPE = '1' ");
        if (TESTSUBCLASSCD_SOGO.equals(param._testsubclasscd)) {
            stb.append("         AND RECEPT.ATTEND_ALL_FLG = '1' ");
            if (printCnt == KAMOKU5) {
                stb.append("         AND BASE.TESTDIV1 = '" + printCnt + "' ");
            }
        }
        stb.append(" ) ");
        if (1 == flg) {
            stb.append(" SELECT ");
            stb.append("     SUM(T1.SCORE) AS SUM, ");
            stb.append("     COUNT(*) AS COUNT, ");
            stb.append("     AVG(FLOAT(T1.SCORE)) AS AVG, ");
            stb.append("     STDDEV(FLOAT(T1.SCORE)) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     SCORES T1 ");
        } else {
            stb.append(" , SEX_COUNT AS ( ");
            stb.append("     SELECT ");
            stb.append("         INT(T1.SCORE) AS SCORE, VALUE(T1.SEX, '9') AS SEX, COUNT(*) AS COUNT ");
            stb.append("     FROM ");
            stb.append("         SCORES T1 ");
            stb.append("     GROUP BY ");
            stb.append("         GROUPING SETS ((INT(T1.SCORE), T1.SEX), ");
            stb.append("                    (INT(T1.SCORE))) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCORE,  ");
            stb.append("     L1.COUNT AS COUNT1, ");
            stb.append("     L2.COUNT AS COUNT2, ");
            stb.append("     T1.COUNT AS COUNT_TOTAL ");
            stb.append(" FROM SEX_COUNT T1 ");
            stb.append(" LEFT JOIN SEX_COUNT L1 ON L1.SCORE = T1.SCORE AND L1.SEX = '1' ");
            stb.append(" LEFT JOIN SEX_COUNT L2 ON L2.SCORE = T1.SCORE AND L2.SEX = '2' ");
            stb.append(" WHERE ");
            stb.append("     T1.SEX = '9' ");
        }
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _testsubclasscd;

        final int _subclassPerfect;
        final String _subclassName;
        final String _testdivAbbv1;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _testsubclasscd = request.getParameter("TESTSUBCLASSCD");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);
            
            _subclassPerfect = 95; // getSubclassPerfect(db2);
            _subclassName = getNameMst(db2, "NAME1", "L009", _testsubclasscd);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
        }
        
        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
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
        
//        public int getSubclassPerfect(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            int rtn = 0;
//            try {
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT  ");
//                stb.append("     PERF.PERFECT ");
//                stb.append(" FROM ");
//                stb.append("     ENTEXAM_PERFECT_MST PERF ");
//                stb.append(" WHERE ");
//                stb.append("     PERF.ENTEXAMYEAR = '" + _entexamyear + "' ");
//                stb.append("     AND PERF.APPLICANTDIV = '" + _applicantdiv + "' ");
//                stb.append("     AND PERF.TESTDIV = '" + _testdiv + "' ");
//                stb.append("     AND PERF.TESTSUBCLASSCD = '" + _testsubclasscd + "' ");
//
//                String perfect = null;
//                final String sql = stb.toString();
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    perfect = rs.getString("PERFECT");
//                }
//                rtn = NumberUtils.isDigits(perfect) ? Integer.parseInt(perfect) : 100;
//            } catch (Exception ex) {
//                log.fatal("exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return rtn;
//        }
    }
}

// eof

