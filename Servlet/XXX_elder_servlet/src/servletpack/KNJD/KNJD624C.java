/*
 * $Id: 69dde71e467c62d9c2aad2f6ba7a089fb78bae13 $
 *
 * 作成日: 2011/06/13
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [成績管理]  得点分布表
 * 
 * @version $Id: 69dde71e467c62d9c2aad2f6ba7a089fb78bae13 $
 */
public class KNJD624C {

    private static final Log log = LogFactory.getLog(KNJD624C.class);

    private static final String SUBCLASSCD_ALL = "999999";
    private static final String SUBCLASSCD_ALL_NAME = "平均点（全科目）";
    private static final int SCORE_MAX = 100;
    private static final int SCORE_MIN = 0;

    private boolean _hasData;

    Param _param;

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
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String frm = "KNJD624C.frm";
        final Map hrclassMap = getHrclassMap(db2);
        final Map subclassMap = getSubclassMap(db2);
        final int MAX_LINE = 101;
        final int COLUMN = 12;
        for (Iterator itSub = subclassMap.keySet().iterator(); itSub.hasNext();) {
            final String subclassCd = (String) itSub.next();
            final Subclass subclass = (Subclass) subclassMap.get(subclassCd);
            svf.VrSetForm(frm, 1);
            
            final List hrclassCdList = new ArrayList(hrclassMap.keySet());
            final CountTable gakunen = new CountTable(null);
            for (int ci = 0, i = 0; i < hrclassCdList.size(); ci++, i++) {
                final String hrClass = (String) hrclassCdList.get(i);
                if (COLUMN <= ci) {
                    svf.VrEndPage();
                    svf.VrSetForm(frm, 1);
                    ci -= COLUMN;
                }
                printHeader(svf, subclass);
                final String classi = String.valueOf(i + 1);
                svf.VrsOut("HR_NAME" + classi, (String) hrclassMap.get(hrClass));
                CountTable ct = null;
                for (Iterator itCt = subclass._countTableList.iterator(); itCt.hasNext(); ) {
                    CountTable ct1 = (CountTable) itCt.next();
                    if (ct1._hrClass.equals(hrClass)) {
                        ct = ct1;
                        break;
                    }
                }
                if (null == ct) {
                    continue;
                }
                // 各クラスごと
                printCountTable(svf, ct, "CLASS" + classi, MAX_LINE);
                gakunen.addCount(ct);
            }
            // 学年
            printCountTable(svf, gakunen, "GRADE", MAX_LINE);
            // 累計
            int ruiseki = 0;
            for (int l = 1, score = SCORE_MAX; score >= SCORE_MIN; l++, score--) {
                final Count c = gakunen.getCount(score);
                if (null != c) {
                    ruiseki += c._count;
                }
                final String value = 0 == ruiseki ? "" : String.valueOf(ruiseki);
                svf.VrsOutn("TOTAL", l, value);
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printCountTable(final Vrw32alp svf, CountTable ct, final String field, final int MAX_LINE) {
        for (int l = 1, score = SCORE_MAX; score >= SCORE_MIN; l++, score--) {
            final Count c = ct.getCount(score);
            final String value = null == c || c._count == 0 ? "" : String.valueOf(c._count); 
            svf.VrsOutn(field, l, value);
        }
        // 合計・平均
        final int totalCount = ct.getTotalCount();
        if (0 != totalCount) {
            final int totalScore = ct.getTotalScore();
            final BigDecimal bdTotalScore = new BigDecimal(String.valueOf(totalScore));
            final BigDecimal bdTotalCount = new BigDecimal(String.valueOf(totalCount));
            final String avg = bdTotalScore.divide(bdTotalCount, 1, BigDecimal.ROUND_HALF_UP).toString();
            svf.VrsOutn(field, MAX_LINE + 1, String.valueOf(totalScore));
            svf.VrsOutn(field, MAX_LINE + 2, avg);
        }
    }

    private void printHeader(final Vrw32alp svf, final Subclass subclass) {
        svf.VrsOut("SUBJECT", subclass._subclassName);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
        svf.VrsOut("MOCK_NAME", _param._mockName);
        svf.VrsOut("GRADE_NAME", _param._gradeName);
        
        for (int l = 1, score = SCORE_MAX; score >= SCORE_MIN; l++, score--) {
            svf.VrsOutn("POINT", l, String.valueOf(score));
        }
    }
    
    private Map getHrclassMap(final DB2UDB db2) {
        final Map hrclassMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getHrclassSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                hrclassMap.put(rs.getString("HR_CLASS"), rs.getString("HR_CLASS_NAME1"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return hrclassMap;
    }
    
    private Map getSubclassMap(final DB2UDB db2) {
        TreeMap subclassMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getScoreSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                if (null == rs.getString("SUBCLASSCD") || null == rs.getString("HR_CLASS") || !NumberUtils.isNumber(rs.getString("SCORE"))) {
                    continue;
                }
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String hrClass = rs.getString("HR_CLASS");
                if (!subclassMap.containsKey(subclassCd)) {
                    subclassMap.put(subclassCd, new Subclass(subclassCd, rs.getString("SUBCLASSNAME")));
                }
                final Subclass subclass = (Subclass) subclassMap.get(subclassCd);
                CountTable ct = null;
                for (Iterator it = subclass._countTableList.iterator(); it.hasNext(); ) {
                    final CountTable ct1 = (CountTable) it.next();
                    if (hrClass != null && hrClass.equals(ct1._hrClass)) {
                        ct = ct1;
                        break;
                    }
                }
                if (null == ct) {
                    ct = new CountTable(hrClass);
                    subclass._countTableList.add(ct);
                }
                ct.addCount(rs.getInt("SCORE"), 1);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return subclassMap;
    }
    
    private static class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final List _countTableList = new ArrayList();
        Subclass(final String subclassCd, final String subclassName) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }
    }
    
    /**
     * 点数の列
     */
    private static class CountTable {
        final String _hrClass;
        final Map _counts; // 点数をキーとするCountのマップ
        CountTable(final String hrClass) {
            _hrClass = hrClass;
            _counts = new TreeMap();
            for (int i = SCORE_MIN; i <= SCORE_MAX; i++) {
                final int score = i;
                _counts.put(new Integer(score), new Count(score));
            }
        }
        public void addCount(final int score, int c) {
            if (isInvalidScore(score)) {
                return;
            }
            Count count = (Count) _counts.get(new Integer(score));
            count._count += c;
        }
        public void addCount(final CountTable ct) {
            if (null != ct) {
                for (int score = SCORE_MIN; score <= SCORE_MAX; score++) {
                    Count c = ct.getCount(score);
                    if (null != c) {
                        addCount(score, c._count);
                    }
                }
            }
        }
        /**
         * 無効な点数か
         * @param score 点数
         * @return 無効な点数ならtrue、そうでなければfalse
         */
        private boolean isInvalidScore(int score) {
            return score < SCORE_MIN || SCORE_MAX < score || !_counts.containsKey(new Integer(score));
        }
        public Count getCount(int score) {
            if (isInvalidScore(score)) {
                return null;
            }
            return (Count) _counts.get(new Integer(score));
        }
        public int getTotalScore() {
            int total = 0;
            for (final Iterator it = _counts.values().iterator(); it.hasNext(); ) {
                final Count c = (Count) it.next();
                total += c._score * c._count;
            }
            return total;
        }
        /**
         * 列の合計人数を得る
         * @return 列の合計人数
         */
        public int getTotalCount() {
            int total = 0;
            for (final Iterator it = _counts.values().iterator(); it.hasNext(); ) {
                final Count c = (Count) it.next();
                total += c._count;
            }
            return total;
        }
    }
    
    /**
     * 点数ごとの人数
     */
    private static class Count implements Comparable {
        final int _score;
        int _count;
        Count(int score) {
            _score = score;
            _count = 0;
        }
        public int compareTo(final Object obj) {
            final Count o = (Count) obj;
            return new Integer(_count).compareTo(new Integer(o._count));
        }
    }
    
    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T3.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
        stb.append("     T4.SUBCLASS_NAME AS SUBCLASSNAME, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.SCORE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_RANK_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.MOCKCD = '" + _param._mockCd + "' ");
        stb.append("         AND T3.MOCKDIV = '1' ");
        stb.append("         AND T3.SCORE IS NOT NULL ");
        stb.append("     LEFT JOIN MOCK_SUBCLASS_MST T4 ON T4.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T3.MOCK_SUBCLASS_CD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND T3.MOCK_SUBCLASS_CD <> '" + SUBCLASSCD_ALL + "' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '" + SUBCLASSCD_ALL + "' AS SUBCLASSCD, ");
        stb.append("     '" + SUBCLASSCD_ALL_NAME + "' AS SUBCLASSNAME, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     INT(ROUND(T3.AVG, 0)) AS SCORE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_RANK_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.MOCKCD = '" + _param._mockCd + "' ");
        stb.append("         AND T3.MOCKDIV = '1' ");
        stb.append("         AND T3.AVG IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T3.MOCK_SUBCLASS_CD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND T3.MOCK_SUBCLASS_CD = '" + SUBCLASSCD_ALL + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     HR_CLASS ");
        return stb.toString();
    }
    
    private String getHrclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.HR_CLASS ");
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
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _grade;
        private final String _mockCd;
        private final String _mockName;
        private final String _semesterName;
        private final String _gradeName;
        private final String[] _categorySelected;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _mockCd = request.getParameter("MOCKCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _mockName = getMockName(db2);
            _semesterName = getSemesterName(db2);
            _gradeName = getGradeName(db2);
        }
        
        private String getMockName(DB2UDB db2) {
            String testName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     VALUE(MOCKNAME1, '') AS MOCKNAME ");
                stb.append(" FROM ");
                stb.append("     MOCK_MST ");
                stb.append(" WHERE ");
                stb.append("     MOCKCD = '" + _mockCd + "' ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    testName = rs.getString("MOCKNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testName;
        }
        
        private String getSemesterName(DB2UDB db2) {
            String semesterName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     VALUE(SEMESTERNAME, '') AS SEMESTERNAME ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _year + "' ");
                stb.append("     AND SEMESTER = '" + _semester + "' ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterName;
        }
        
        private String getGradeName(DB2UDB db2) {
            String gradeName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     VALUE(GRADE_NAME1, '') AS GRADE_NAME1 ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _year + "' ");
                stb.append("     AND GRADE = '" + _grade + "' ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    gradeName = rs.getString("GRADE_NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeName;
        }
    }
}

// eof
