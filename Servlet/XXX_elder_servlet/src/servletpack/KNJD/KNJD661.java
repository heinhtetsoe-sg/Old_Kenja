/*
 * $Id: 88ccb34a7fb3b3ae4ed2094c24f17c24fdf41acc $
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
 * 中京 漢字テスト追試対象者一覧
 *
 */
public class KNJD661 {
    
    private static final Log log = LogFactory.getLog(KNJD661.class);
    
    private static final String MAN = "1";
    private static final String WOMAN = "2";
    private static final String SEMEALL = "9";
    private static final int LINE_PER_PAGE = 20;
    
    private static final int AVG_JUDGE_NULL = 0;
    private static final int AVG_JUDGE_PASSED = 1;
    private static final int AVG_JUDGE_NOT_PASSEED = 2;

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
        PreparedStatement ps = null;
        ResultSet rs = null;
        List grades = new ArrayList();
        try {
            final String sqlGrade = " SELECT DISTINCT GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._year  + "' ORDER BY GRADE ";
            ps = db2.prepareStatement(sqlGrade);
            rs = ps.executeQuery();
            while (rs.next()) {
                grades.add(rs.getString("GRADE"));
            }
            
        } catch (SQLException e) {
            log.error("Exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        final Map gradeCourseListMap = new HashMap();
        for (final Iterator it = grades.iterator(); it.hasNext();) {
            final String grade = (String) it.next();
            try {
                final String sqlGradeCourse = sqlGradeCourse(grade);
                log.debug(" sqlGradeCourse = " + sqlGradeCourse);
                ps = db2.prepareStatement(sqlGradeCourse);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("HR_CLASS", rs.getString("HR_CLASS"));
                    m.put("COURSE", rs.getString("COURSE"));
                    if (null == gradeCourseListMap.get(grade)) {
                        gradeCourseListMap.put(grade, new ArrayList());
                    }
                    ((List) gradeCourseListMap.get(grade)).add(m);
                }
                
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        for (final Iterator it = grades.iterator(); it.hasNext();) {
            final String grade = (String) it.next();
            final List gradeCourseList = (List) gradeCourseListMap.get(grade);
            if (null == gradeCourseList) {
                continue;
            }
            for (final Iterator gcit = gradeCourseList.iterator(); gcit.hasNext();) {
                final Map m = (Map) gcit.next();
                final String hrClass = (String) m.get("HR_CLASS");
                final String course = (String) m.get("COURSE");
                printStudent(db2, svf, grade, hrClass, course);
            }
        }
    }

    private void printHeader(DB2UDB db2, Vrw32alp svf, String grade, String hrClass, String course) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        
        int j = 0;
        for (final Iterator itm = _param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
            j += 1;
            String mockcd = (String) itm.next();
            KanjiMockTest k = (KanjiMockTest) _param._kanjiMockTestMap.get(mockcd);
            final String name = k._mockName2;
            final int idx = (k._isRetest) ? 6 : j;
            svf.VrsOut("SUBCLASS" + idx + "_2", name);
            if (k._avgUsed) {
                svf.VrsOut("KOME" + idx, "*");
            }
        }
        svf.VrsOut("SUBCLASS5_2", "平均");
        
        final String gradeName = _param._gradeNames.get(grade) == null ? "" : (String) _param._gradeNames.get(grade);
        final String hrName = _param._hrNames.get(grade + hrClass) == null ? "" : (String) _param._hrNames.get(grade + hrClass);
        final String courseName = _param._courseNames.get(course) == null ? "" : (String) _param._courseNames.get(course);
        svf.VrsOut("SELECT_NAME", gradeName + "　" + hrName + "　" + courseName);
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
     * 指定された学年、コースの生徒を印刷する
     * @param db2
     * @param svf
     * @param grade
     * @param course
     */
    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final String grade, final String hrClass, final String course) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        List studentsAll = new ArrayList();
        try {
            final String sqlSchreg = sqlSchreg(grade, hrClass, course, false);
            log.debug(" sqlSchreg =" + sqlSchreg);
            ps = db2.prepareStatement(sqlSchreg);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                Student s = getStudent(studentsAll, schregno);
                if (s == null) {
                    final String name = rs.getString("NAME");
                    final String hrname = rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String sex = rs.getString("SEX");
                    final String sexname = rs.getString("SEXNAME");
                    s = new Student(schregno, name, hrname, attendno, sex, sexname);
                    studentsAll.add(s);
                }
                
                s._scores.put(rs.getString("MOCKCD"), new TestScore(rs.getString("MOCKCD"), rs.getString("SCORE"), rs.getString("IS_PASSED")));
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        Double passScoreALL9 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try {
            final String sqlPassScoreALL9 = sqlPassScoreALL9(grade, course);
            log.debug(" sqlPassScoreALL9 =" + sqlPassScoreALL9);
            ps2 = db2.prepareStatement(sqlPassScoreALL9);
            rs2 = ps2.executeQuery();
            while (rs2.next()) {
                passScoreALL9 = (Double) rs2.getObject("PASS_SCORE");
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps2, rs2);
            db2.commit();
        }
        if ("499999999".equals(_param._mockCd)) {
            // 平均点が不合格以外の生徒を除く
            for (final Iterator it = studentsAll.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final int testCount = student.getTestCount(_param);
                if (0 == testCount) {
                    it.remove();
                    continue;
                }
                final BigDecimal avgBigDecimal = student.getAvg(_param);
                final int judgePassScoreALL9 = judgePassScoreALL9(passScoreALL9, avgBigDecimal);
                if (AVG_JUDGE_NOT_PASSEED != judgePassScoreALL9) {
                    it.remove();
                }
            }
        }

        int man = 0;
        int woman = 0;
        int total = 0;
        
        final List pageList = getPageList(studentsAll, LINE_PER_PAGE);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List students = (List) pageList.get(pi);

            svf.VrSetForm("KNJD661.frm", 4);
            printHeader(db2, svf, grade, hrClass, course);

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                svf.VrsOut("HR_NAME", student._hrname);
                svf.VrsOut("ATTENDNO", student._attendno);
                svf.VrsOut("NAME", student._name);
                svf.VrsOut("SEX", student._sexname);
                
                int i = 0;
                for (final Iterator itm = _param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
                    final String mockCd = (String) itm.next();
                    final KanjiMockTest kanjiMockTest = (KanjiMockTest) _param._kanjiMockTestMap.get(mockCd);
                    if (null == kanjiMockTest || 6 <= i) {
                        continue;
                    }
                    i += 1;
                    final int idx = kanjiMockTest._isRetest ? 6 : i;
                    
                    final TestScore testScore = (TestScore) student._scores.get(mockCd);
                    if (null == testScore) {
                        continue;
                    }
                    if (null != testScore._score) {
                        svf.VrsOut("SCORE" + idx, testScore._score);
                    }
                    final String sPass = null == testScore._isPassed ? "" : "1".equals(testScore._isPassed) ? "合格" : "不合格";
                    svf.VrsOut("PASSNAME" + idx, sPass);
                }
                
                final int testCount = student.getTestCount(_param);
                if (0 != testCount) {
                    final BigDecimal avgBigDecimal = student.getAvg(_param);
                    svf.VrsOut("SCORE5", avgBigDecimal.toString());
                    svf.VrsOut("PASSNAME5", getJudgename(judgePassScoreALL9(passScoreALL9, avgBigDecimal)));
                }
                if (MAN.equals(student._sex)) {
                    man += 1;
                } else if (WOMAN.equals(student._sex)){
                    woman += 1;
                }
                total += 1;
                
                if (pi == pageList.size() - 1) {
                    svf.VrsOut("TOTAL_NUMBER", "男" + man + "名　女" + woman + "名　計" + total + "名");
                } else {
                    svf.VrsOut("TOTAL_NUMBER", "");
                }
                
                svf.VrEndRecord();
                
                _hasData = true;
            }
            for (int i = students.size(); i < LINE_PER_PAGE; i++) {
                svf.VrsOut("ATTENDNO", "");
                svf.VrsOut("NAME", "DUMMY");
                svf.VrAttribute("NAME", "X=10000");
                svf.VrEndRecord();
            }
        }
    }
    
    private static List getPageList(final List list, final int cnt) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= cnt) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    /**
     * 合否を判定します。
     * @param passScore 合格点
     * @param avg 平均
     * @return
     */
    private int judgePassScoreALL9(final Double passScore, final BigDecimal avg) {
        if (null == passScore || null == avg) {
            return AVG_JUDGE_NULL;
        }
        if (passScore.floatValue() <= avg.floatValue()) {
            return AVG_JUDGE_PASSED;
        }
        return AVG_JUDGE_NOT_PASSEED;
    }
    
    /**
     * 合否の判定名称
     * @param judge 合否判定
     * @return
     */
    private String getJudgename(int judge) {
        if (AVG_JUDGE_PASSED == judge) {
            return "合格";
        } else if (AVG_JUDGE_NOT_PASSEED == judge) {
            return "不合格";
        }
        return "";
    }
    
    /**
     * 平均欄の合否を判定する合格点を取得
     * MOCKCD = '499999999'
     * @return
     */
    private String sqlPassScoreALL9(String grade, String course) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T4.MOCKCD, ");
        stb.append("     T4.MOCK_SUBCLASS_CD, ");
        stb.append("     FLOAT(T4.PASS_SCORE) AS PASS_SCORE ");
        stb.append(" FROM ");
        stb.append("     MOCK_PERFECT_COURSE_DAT T4 ");
        stb.append(" WHERE ");
        stb.append("         T4.YEAR = '" + _param._year + "' ");
        stb.append("     AND T4.COURSE_DIV = '0' ");
        stb.append("     AND T4.MOCKCD = '499999999' ");
        stb.append("     AND T4.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '" + grade + "' END ");
        stb.append("     AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = ");
        stb.append("         CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ELSE '" + course + "' END ");
        return stb.toString();
    }
    
    /**
     * 印刷する学年コースを得る
     * @return
     */
    private String sqlGradeCourse(String grade) {
        final StringBuffer stb = new StringBuffer();
        stb.append(sqlSchreg(grade, null, null, true));
        stb.append(" SELECT DISTINCT GRADE, HR_CLASS, COURSE ");
        stb.append(" FROM T_GC ");
        stb.append(" WHERE IS_PASSED = 0 ");
        if (!"499999999".equals(_param._mockCd)) {
            stb.append("     AND MOCKCD = '" + _param._mockCd + "' ");
        }
        stb.append(" ORDER BY GRADE, HR_CLASS, COURSE ");
        return stb.toString();
    }

    private String sqlSchreg(String grade, String hrClass, String course, boolean isWith) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TA AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, T3.MOCK_SUBCLASS_CD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN MOCK_MST T2 ON T2.MOCKCD LIKE '4%' ");
        stb.append("     INNER JOIN MOCK_DAT T3 ON T3.MOCKCD = T2.MOCKCD ");
        stb.append("         AND T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN MOCK_PERFECT_COURSE_DAT T4 ON T1.YEAR = T4.YEAR ");
        stb.append("         AND T4.COURSE_DIV = '0' ");
        stb.append("         AND T4.MOCKCD = T3.MOCKCD ");
        stb.append("         AND T4.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD ");
        stb.append("         AND T4.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T1.GRADE END  ");
        stb.append("         AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = ");
        stb.append("           CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if (SEMEALL.equals(_param._semester)) {
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemes + "' ");
        } else {
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("     AND T1.GRADE = '" + grade + "' ");
        if (null != hrClass) {
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        }
        if (null != course) {
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + course + "' ");
        }
        if (!"499999999".equals(_param._mockCd)) {
            stb.append("     AND T2.MOCKCD = '" + _param._mockCd + "' ");
            stb.append("     AND T4.PASS_SCORE > T3.SCORE ");
        }
        stb.append(" ) ");
        if (isWith) {
            stb.append(" , T_GC AS ( ");
        }
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T6.NAME, ");
        stb.append("     T6.SEX, ");
        stb.append("     T7.NAME2 AS SEXNAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T2.MOCKCD, ");
        stb.append("     T3.MOCK_SUBCLASS_CD, ");
        stb.append("     T3.SCORE, ");
        stb.append("     T4.PERFECT, ");
        stb.append("     CASE WHEN T3.SCORE IS NULL OR T4.PASS_SCORE IS NULL THEN NULL ");
        stb.append("          WHEN T4.PASS_SCORE <= T3.SCORE THEN 1 ELSE 0 END AS IS_PASSED ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN MOCK_MST T2 ON T2.MOCKCD LIKE '4%' ");
        stb.append("     INNER JOIN MOCK_DAT T3 ON T3.MOCKCD = T2.MOCKCD ");
        stb.append("         AND T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN MOCK_PERFECT_COURSE_DAT T4 ON T1.YEAR = T4.YEAR ");
        stb.append("         AND T4.COURSE_DIV = '0' ");
        stb.append("         AND T4.MOCKCD = T2.MOCKCD ");
        stb.append("         AND T4.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD ");
        stb.append("         AND T4.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T1.GRADE END  ");
        stb.append("         AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = ");
        stb.append("           CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T5.GRADE = T1.GRADE ");
        stb.append("         AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._chkTentai)) {
            stb.append("     AND (T6.GRD_DIV IS NULL OR T6.GRD_DIV = '4') ");
        }
        stb.append("     LEFT JOIN NAME_MST T7 ON T7.NAMECD1 = 'Z002' ");
        stb.append("         AND T7.NAMECD2 = T6.SEX ");
        stb.append(" WHERE ");
        stb.append("     (T1.SCHREGNO, T3.MOCK_SUBCLASS_CD) IN (SELECT SCHREGNO, T3.MOCK_SUBCLASS_CD FROM TA) ");
        stb.append("     AND T1.YEAR = '" + _param._year +"' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester +"' ");
        stb.append("     AND T1.GRADE = '" + grade +"' ");
        if (null != hrClass) {
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        }
        if (null != course) {
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + course + "' ");
        }
        stb.append("     AND T2.MOCKCD <= '" + _param._mockCd + "' ");
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        if (isWith) {
            stb.append(" ) ");
        }
        
        return stb.toString();
    }
    
    private static class Student {
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
            _hrname = hrname;
            _attendno = (null == attendno) ? "" : Integer.valueOf(attendno).toString() + "番";
            _sex = sex;
            _sexname = sexName;
        }
        
        public int getTestCount(final Param param) {
            int testCount = 0;
            for (final Iterator itm = param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
                final String mockCd = (String) itm.next();
                final KanjiMockTest kanjiMockTest = (KanjiMockTest) param._kanjiMockTestMap.get(mockCd);
                if (null == kanjiMockTest) {
                    continue;
                }
                final TestScore testScore = (TestScore) _scores.get(mockCd);
                if (null == testScore) {
                    continue;
                }
                if (null != testScore._score) {
                    if (kanjiMockTest._avgUsed) {
                        testCount += 1;
                    }
                }
            }
            return testCount;
        }
        
        public int getTotalScore(final Param param) {
            int totalScore = 0;
            for (final Iterator itm = param._kanjiMockTestMap.keySet().iterator(); itm.hasNext();) {
                final String mockCd = (String) itm.next();
                final KanjiMockTest kanjiMockTest = (KanjiMockTest) param._kanjiMockTestMap.get(mockCd);
                if (null == kanjiMockTest) {
                    continue;
                }
                final TestScore testScore = (TestScore) _scores.get(mockCd);
                if (null == testScore) {
                    continue;
                }
                if (null != testScore._score) {
                    if (kanjiMockTest._avgUsed) {
                        totalScore += Integer.parseInt(testScore._score);
                    }
                }
            }
            return totalScore;
        }
        
        public BigDecimal getAvg(final Param param) {
            final int testCount = getTestCount(param);
            if (0 == testCount) {
                return new BigDecimal(-1);
            }
            final int totalScore = getTotalScore(param);
            return new BigDecimal(totalScore).divide(new BigDecimal(testCount), 0, BigDecimal.ROUND_FLOOR);
        }
    }
    
    private class TestScore {
        final String _mockcd;
        final String _score;
        final String _isPassed;
        TestScore(final String mockcd, final String score, final String isPassed) {
            _mockcd = mockcd;
            _score = score;
            _isPassed = isPassed;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    /**
     * 漢字模試テスト
     */
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
        private final String _ctrlSemes;
        private final String _ctrlDate;
        private final String _mockCd;
        final String _chkTentai;
        private final Map _kanjiMockTestMap = new TreeMap();
        private final Map _gradeNames = new HashMap();
        private final Map _hrNames = new HashMap();
        private final Map _courseNames = new HashMap();
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemes = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _mockCd = request.getParameter("MOCKCD");
            _chkTentai = request.getParameter("CHK_TENTAI");
            setKanjiMockTestMap(db2);
            setGradeNames(db2);
            setHrNames(db2);
            setCourseNames(db2);
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
                    KanjiMockTest kanjiMockTest = new KanjiMockTest(mockCd, mockName1, mockName2, mockName3, avgUsed, isRetest);
                    _kanjiMockTestMap.put(mockCd, kanjiMockTest);
                }
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }
        
        private void setGradeNames(final DB2UDB db2) throws SQLException {
            _gradeNames.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.GRADE, ");
                sql.append("     T1.SCHOOL_KIND, ");
                sql.append("     T1.GRADE_CD, ");
                sql.append("     T1.GRADE_NAME1 ");
                sql.append(" FROM ");
                sql.append("     SCHREG_REGD_GDAT T1 ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + _year + "' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    
                    _gradeNames.put(grade, gradeName);
                }
            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }

        private void setHrNames(final DB2UDB db2) throws SQLException {
            _hrNames.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.GRADE, ");
                sql.append("     T1.HR_CLASS, ");
                sql.append("     T1.HR_NAME ");
                sql.append(" FROM ");
                sql.append("     SCHREG_REGD_HDAT T1 ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + _year + "' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    
                    _hrNames.put(grade + hrClass, hrName);
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

