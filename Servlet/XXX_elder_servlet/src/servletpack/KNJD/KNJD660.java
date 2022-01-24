/*
 * $Id: 67235058b60470ce423e3a038c0f2b809cd2c7ad $
 *
 * 作成日: 2010/05/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

/**
 * 中京 漢字テスト成績一覧
 *
 */
public class KNJD660 {
    
    private static final Log log = LogFactory.getLog(KNJD660.class);
    
    private static final String SEMEALL = "9";
    private static final int LINE_PER_PAGE = 50;
    private static final String TOTAL_SCORE = "TOTAL_SCORE";
    
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
            
            boolean hasData = printMain(db2, svf);
            
            if (!hasData) {
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
    
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) {
        boolean hasData = false;
        svf.VrSetForm("KNJD660.frm", 4);
        printHeader(db2, svf);
        for (int i = 0; i < _param._categorySelected.length; i++) {
            hasData = printStudent(db2, svf, _param._categorySelected[i]) || hasData;
        }
        return hasData;
    }
    
    private void printHeader(DB2UDB db2, Vrw32alp svf) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        
        int j = 0;
        for (final Iterator itm = _param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
            j += 1;
            String mockcd = (String) itm.next();
            KanjiMockTest k = (KanjiMockTest) _param._kanjiMockTestMap.get(mockcd);
            final String name = k._mockName2;
            final int idx = (k._isRetest) ? 7 : j;
            svf.VrsOut("SUBCLASS" + idx + "_2", name);
            if (k._avgUsed) {
                svf.VrsOut("KOME" + idx, "*");
            }
        }
        svf.VrsOut("SUBCLASS5_2", "総合");
        svf.VrsOut("SUBCLASS6_2", "平均");
    }

    /**
     * 生徒を得る
     * @param students
     * @param schregno
     * @return
     */
    private Student getStudent(List students, String schregno) {
        for (Iterator it = students.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            if (schregno != null && schregno.equals(student._schregno)) {
                return student;
            }
        }
        return null;
    }

    /**
     * categorySelectedの生徒を印刷する
     * @param db2
     * @param svf
     * @param categorySelected
     * @return
     */
    private boolean printStudent(final DB2UDB db2, final Vrw32alp svf, final String categorySelected) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasData = false;
        List students = new ArrayList();
        try {
            final String sqlSchregSelected = sqlSchregSelected(categorySelected);
            // log.debug(" sqlSchregSelected =" + sqlSchregSelected);
            ps = db2.prepareStatement(sqlSchregSelected);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                Student s = getStudent(students, schregno);
                if (s == null) {
                    final String name = rs.getString("NAME");
                    final String hrname = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String sex = rs.getString("SEX");
                    final String sexname = rs.getString("SEXNAME");
                    s = new Student(schregno, name, hrname, attendno, sex, sexname);
                    students.add(s);
                }
                if (null != rs.getString("SCORE")) {
                    s._scores.put(rs.getString("MOCKCD"), rs.getString("SCORE"));
                    hasData = true;
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (!hasData) {
            return hasData;
        }
        
        svf.VrsOut("SELECT_NAME", _param.getSelectName(categorySelected));
        int no = 0;
        ScoreAverage scoreAverage = new ScoreAverage();
        for (Iterator it = students.iterator(); it.hasNext();) {
            no += 1;
            Student student = (Student) it.next();
            svf.VrsOut("NUMBER", String.valueOf(no));
            svf.VrsOut("HR_NAME", student._hrname);
            svf.VrsOut("ATTENDNO", student._attendno);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("SEX", student._sexname);
            
            int i = 0;
            int privateTotalScore = 0;
            int testCount = 0;
            for (final Iterator itm = _param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
                final String mockCd = (String) itm.next();
                final KanjiMockTest kanjiMockTest = (KanjiMockTest) _param._kanjiMockTestMap.get(mockCd);
                if (6 <= i) {
                    continue;
                }
                i += 1;
                final int idx = kanjiMockTest._isRetest ? 7 : i;
                
                final String score = (String) student._scores.get(mockCd);
                if (null != score) {
                    svf.VrsOut("SCORE" + idx, score);
                    scoreAverage.add(mockCd, score, null);
                    if (kanjiMockTest._avgUsed) {
                        privateTotalScore += Integer.parseInt(score);
                        testCount += 1;
                    }
                }
            }
            if (0 != testCount) {
                final String avg = new BigDecimal(privateTotalScore).divide(new BigDecimal(testCount), 1, BigDecimal.ROUND_HALF_UP).toString();
                svf.VrsOut("SCORE5", String.valueOf(privateTotalScore));
                scoreAverage.add(TOTAL_SCORE, String.valueOf(privateTotalScore), String.valueOf(testCount));
                svf.VrsOut("SCORE6", avg);
            }
            svf.VrEndRecord();
            hasData = true;

            if (no % LINE_PER_PAGE == 0) {
                svf.VrsOut("AVE_SCORE1", "　\t　\t　\t");
                svf.VrEndRecord();
            }
        }
        if (no % LINE_PER_PAGE != 0) {
            for (int i = no % LINE_PER_PAGE; i < LINE_PER_PAGE; i++) {
                svf.VrsOut("ATTENDNO", "");
                svf.VrsOut("NAME", "　\t　\t　\t");
                svf.VrEndRecord();
            }
        }
        int i = 0;
        for (final Iterator itm = _param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
            final String mockCd = (String) itm.next();
            final KanjiMockTest kanjiMockTest = (KanjiMockTest) _param._kanjiMockTestMap.get(mockCd);
            if (6 <= i) {
                continue;
            }
            i += 1;
            final String avg = scoreAverage.getAverage(mockCd);
            if (kanjiMockTest._isRetest) {
                svf.VrsOut("AVE_SCORE7", avg);
            } else {
                svf.VrsOut("AVE_SCORE" + i, avg);
            }
        }
        svf.VrsOut("AVE_SCORE5", scoreAverage.getAverage(TOTAL_SCORE));
        svf.VrsOut("AVE_SCORE6", scoreAverage.getTotalAverage());
        svf.VrEndRecord();
        return hasData;
    }
    
    private class ScoreAverage {
        final Map _mockScores = new HashMap();
        public ScoreAverage() {
            _mockScores.clear();
        }
        
        /**
         * テストのリストを得る
         * @param mockCd
         * @return
         */
        private List getScoreList(String mockCd) {
            if (!_mockScores.containsKey(mockCd)) {
                _mockScores.put(mockCd, new ArrayList());
            }
            return (List) _mockScores.get(mockCd);
        }

        /**
         * テストに得点を追加する
         * @param mockCd
         * @return
         */
        public void add(String mockCd, String score, String count) {
            if (null != score) {
                if (TOTAL_SCORE.equals(mockCd)) {
                    getScoreList(mockCd).add(new String[]{score, count});
                } else {
                    getScoreList(mockCd).add(score);
                }
            }
        }

        /**
         * テストの得点の合計点を得る
         * @param mockCd
         * @return
         */
        public int getTotal(String mockCd) {
            int total = 0;
            List scores = getScoreList(mockCd);
            if (TOTAL_SCORE.equals(mockCd)) {
                for (Iterator it = scores.iterator(); it.hasNext();) {
                    String[] scoreCount = (String[]) it.next();
                    total += Integer.parseInt(scoreCount[0]);
                }
            } else {
                for (Iterator it = scores.iterator(); it.hasNext();) {
                    String score = (String) it.next();
                    total += Integer.parseInt(score);
                }
            }
            return total;
        }
        
        /**
         * テストの平均点を得る
         * @param mockCd
         * @return
         */
        public String getAverage(String mockCd) {
            int size = getScoreList(mockCd).size();
            if (size == 0) {
                return "";
            }
            int total = getTotal(mockCd);
            return new BigDecimal(total).divide(new BigDecimal(size), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
        

        /**
         * 各生徒テストの平均点を得る
         * @param mockCd
         * @return
         */
        public String getTotalAverage() {
            final List scores = getScoreList(TOTAL_SCORE);
            if (scores.size() == 0) {
                return "";
            }
            int count = 0;
            BigDecimal totalAverage = new BigDecimal(0);
            for (Iterator it = scores.iterator(); it.hasNext();) {
                String[] scoreCount = (String[]) it.next();
                if (null == scoreCount || null == scoreCount[1] || "0".equals(scoreCount[1])) {
                    continue;
                }
                BigDecimal avg = new BigDecimal(scoreCount[0]).divide(new BigDecimal(scoreCount[1]), BigDecimal.ROUND_HALF_UP);
                totalAverage = totalAverage.add(avg);
                count += 1;
            }
            return (count == 0) ? "" : totalAverage.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
    }
    
    private String sqlSchregSelected(String categorySelected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T6.NAME, ");
        stb.append("     T6.SEX, ");
        stb.append("     T7.NAME2 AS SEXNAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T2.MOCKCD, ");
        stb.append("     T3.MOCK_SUBCLASS_CD, ");
        stb.append("     T3.SCORE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_MST T2 ON T2.MOCKCD LIKE '4%' ");
        stb.append("     LEFT JOIN MOCK_DAT T3 ON T3.MOCKCD = T2.MOCKCD ");
        stb.append("         AND T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T5.GRADE = T1.GRADE ");
        stb.append("         AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST T7 ON T7.NAMECD1 = 'Z002' ");
        stb.append("         AND T7.NAMECD2 = T6.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        if ("2".equals(_param._selectDiv)) {
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + categorySelected + "' ");
        } else {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + categorySelected + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }
    
    private class Student {
        final String _schregno;
        final String _name;
        final String _hrname;
        final String _attendno;
        final String _sex;
        final String _sexname;
        final Map _scores = new HashMap();
        public Student(
                final String schregno,
                final String name,
                final String hrname,
                final String attendno,
                final String sex,
                final String sexName) {
            _schregno = schregno;
            _name = name;
            _hrname = (null == hrname) ? "" : hrname;
            _attendno = (null == attendno) ? "" : Integer.valueOf(attendno).toString();
            _sex = sex;
            _sexname = sexName;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private static class KanjiMockTest {
        final String _mockCd;
        final String _mockName1;
        final String _mockName2;
        final String _mockName3;
        final boolean _avgUsed; // 平均点の算出に使用するか
        final boolean _isRetest; // 追試か
        public KanjiMockTest(String mockCd, String mockName1, String mockName2, String mockName3, boolean avgUsed, boolean isRetest) {
            _mockCd = mockCd;
            _mockName1 = mockName1;
            _mockName2 = mockName2;
            _mockName3 = mockName3;
            _avgUsed = avgUsed;
            _isRetest = isRetest;
        }
        public String toString() {
            return "[" + _mockCd + ":" + _mockName1 + "(" + _mockName2 + " , " + _mockName3 + ") 平均に使用する?=" + _avgUsed + " , 追試?=" + _isRetest + "]";
        }
    }
    
    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _selectDiv;
        private final String _grade;
        private final String[] _categorySelected;
        private final Map _kanjiMockTestMap = new TreeMap();
        private final Map _hrNames = new TreeMap();
        private final Map _courseNames = new TreeMap();
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _selectDiv = request.getParameter("SELECT_DIV");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            setKanjiMockTestMap(db2);
            setHrNames(db2);
            setCourseNames(db2);
        }
        
        public String getSelectName(String categorySelected) {
            if ("2".equals(_selectDiv)) {
                return (String) _courseNames.get(categorySelected);
            }
            return (String) _hrNames.get(categorySelected);
        }

        private void setKanjiMockTestMap(final DB2UDB db2) throws SQLException {
            _kanjiMockTestMap.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.MOCKCD, ");
                sql.append("     T1.MOCKNAME1, ");
                sql.append("     T1.MOCKNAME2, ");
                sql.append("     T1.MOCKNAME3, ");
                sql.append("     T2.NAMESPARE1 AS AVG_USED, ");
                sql.append("     T2.NAMESPARE2 AS IS_RETEST ");
                sql.append(" FROM ");
                sql.append("     MOCK_MST T1 ");
                sql.append("     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'D025' ");
                sql.append("         AND T2.NAME1 = T1.MOCKCD ");
                sql.append(" WHERE ");
                sql.append("     T1.MOCKCD LIKE '4%' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String mockCd = rs.getString("MOCKCD");
                    final String mockName1 = rs.getString("MOCKNAME1");
                    final String mockName2 = rs.getString("MOCKNAME2");
                    final String mockName3 = rs.getString("MOCKNAME3");
                    final boolean avgUsed = "1".equals(rs.getString("AVG_USED"));
                    final boolean isRetest = "1".equals(rs.getString("IS_RETEST"));
                    _kanjiMockTestMap.put(mockCd, new KanjiMockTest(mockCd, mockName1, mockName2, mockName3, avgUsed, isRetest));
                }
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }
        
        
        private void setHrNames(final DB2UDB db2) {
            _hrNames.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
                sql.append("     T1.HR_NAME ");
                sql.append(" FROM ");
                sql.append("     SCHREG_REGD_HDAT T1 ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + _year  +"' ");
                sql.append("     AND T1.SEMESTER = '" + _semester +"' ");
                sql.append("     AND T1.GRADE = '" + _grade +"' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String hrclass = rs.getString("GRADE_HR_CLASS");
                    final String name = rs.getString("HR_NAME");
                    _hrNames.put(hrclass, name);
                }
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }
        
        private void setCourseNames(final DB2UDB db2) {
            _courseNames.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT DISTINCT ");
                sql.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECODE, ");
                sql.append("     T2.COURSENAME, ");
                sql.append("     T3.MAJORNAME, ");
                sql.append("     T4.COURSECODENAME ");
                sql.append(" FROM ");
                sql.append("     SCHREG_REGD_DAT T1 ");
                sql.append("     LEFT JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
                sql.append("     LEFT JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD ");
                sql.append("     LEFT JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + _year  +"' ");
                sql.append("     AND T1.SEMESTER = '" + _semester +"' ");
                sql.append("     AND T1.GRADE = '" + _grade +"' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String course = rs.getString("COURSECODE");
                    final String coursename = rs.getString("COURSENAME") == null ? "" : rs.getString("COURSENAME") + "課程";
                    final String majorname = rs.getString("MAJORNAME") == null ? "" : rs.getString("MAJORNAME") + "学科";
                    final String coursecodename = rs.getString("COURSECODENAME") == null ? "" : rs.getString("COURSECODENAME") + "コース";
                    _courseNames.put(course, coursename + majorname + "　" + coursecodename);
                }
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }
    }
}

// eof
