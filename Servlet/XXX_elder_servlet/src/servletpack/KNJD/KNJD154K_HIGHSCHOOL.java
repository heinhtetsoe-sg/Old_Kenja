// kanji=漢字
/*
 * $Id: 989cdd2cfd71f06bd7f0b4e4dab383fb1b1f40d3 $
 *
 * 作成日: 2011/05/02 15:51:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfoTottori;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 989cdd2cfd71f06bd7f0b4e4dab383fb1b1f40d3 $
 */
public class KNJD154K_HIGHSCHOOL extends KNJD154K_Abstract {

    private static final Log log = LogFactory.getLog("KNJD154K_HIGHSCHOOL.class");

    protected void printMain() throws Exception {
        try {
            final List studentList = getStudentList();
            final List printDataList = getPrintData(studentList);
            setHead();
            for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();

                setPrintOut(student);

                _hasData = true;
            }
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }
        } finally {
            _svf.VrQuit();
            _db2.close();
        }
    }

    private List getPrintData(final List studentList) throws SQLException, ParseException {
        List printDataList = new ArrayList();
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            for (final Iterator iterator = _param._testList.iterator(); iterator.hasNext();) {
                final TestItem testItem = (TestItem) iterator.next();
                final Map testDataMap = setChairStd(student, testItem);
                student._testMap.put(testItem._key, testDataMap);
                student._testAllMap.put(testItem._key, new TestScore(""));
                setRank(student, testItem);
                setRankChair(student, testItem);

                //RECORD_AVERAGE_DAT
                final String gradeSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, null, null, AVG_GRADE, student._grade, student._hrClass, null, testItem._kindCd + testItem._itemCd, _param._tableDiv);
                setAvg(student, gradeSql, AVG_GRADE, testItem);

                //RECORD_AVERAGE_DAT
                final String hrSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, null, null, AVG_HR, student._grade, student._hrClass, null, testItem._kindCd + testItem._itemCd, _param._tableDiv);
                setAvg(student, hrSql, AVG_HR, testItem);

                //RECORD_AVERAGE_DAT
                final String course = student._courseCd + student._majorCd + student._courseCode;
                final String courseSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, null, null, AVG_COURSE, student._grade, student._hrClass, course, testItem._kindCd + testItem._itemCd, _param._tableDiv);
                setAvg(student, courseSql, AVG_COURSE, testItem);

                //RECORD_AVERAGE_DAT
                final String major = student._courseCd + student._majorCd;
                final String majorSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, null, null, AVG_MAJOR, student._grade, student._hrClass, major, testItem._kindCd + testItem._itemCd, _param._tableDiv);
                setAvg(student, majorSql, AVG_MAJOR, testItem);

            }
            printDataList.add(student);
        }
        return printDataList;
    }

    private void setHead() {
        _svf.VrSetForm("KNJD154K_2.frm", 1);
    }

    private void setPrintOut(final Student student) {
        log.debug(student);
        _svf.VrsOut("YEAR", _param.changePrintYear());
        _svf.VrsOut("DATE", _param.changePrintDate());
        _svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        _svf.VrsOut("FIELD1", student._majorName + student._courseCodeName);
        _svf.VrsOut("HR_NAME", student._hrName + student._attendNo + "番");
        _svf.VrsOut("NAME", student._name);
        int fieldNo = 1;

        for (final Iterator itSub = _param._testList.iterator(); itSub.hasNext();) {
            final TestItem testItem = (TestItem) itSub.next();
            _svf.VrsOutn("EXAM_NAME", fieldNo, testItem._name);
            final Map testSub = (Map) student._testMap.get(testItem._key);
            int fieldCnt = 1;
            if (null != testSub) {
                for (final Iterator itSubclass = testSub.keySet().iterator(); itSubclass.hasNext();) {
                    final String subclassCd = (String) itSubclass.next();
                    final TestScore testScore = (TestScore) testSub.get(subclassCd);
                    final String subName = null != testScore._name ? testScore._name : "";
                    _svf.VrsOutn("SUBJECT" + fieldCnt, fieldNo, subName);
                    _svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
                    _svf.VrsOutn("AVE_MAJOR" + fieldCnt, fieldNo, testScore._majorAvg);
                    fieldCnt++;
                }
            }
            final TestScore allTestScore = (TestScore) student._testAllMap.get(testItem._key);
            _svf.VrsOutn("AVERAGE", fieldNo, allTestScore._average);
            _svf.VrsOutn("TOTAL", fieldNo, allTestScore._score);
            _svf.VrsOutn("CLASS_RANK", fieldNo, allTestScore._hrRank);
            _svf.VrsOutn("CLASS_RANK_TOTAL", fieldNo, allTestScore._hrCnt);
            _svf.VrsOutn("COURSE_RANK", fieldNo, allTestScore._courseRank);
            _svf.VrsOutn("COURSE_RANK_TOTAL", fieldNo, allTestScore._courseCnt);
            _svf.VrsOutn("MAJOR_RANK", fieldNo, allTestScore._majorRank);
            _svf.VrsOutn("MAJOR_RANK_TOTAL", fieldNo, allTestScore._majorCnt);

            fieldNo++;
        }
        _svf.VrEndPage();
    }

    private Map setChairStd(final Student student, final TestItem testItem) throws SQLException {
        final Map retMap = new TreeMap();
        ResultSet scoreRs = null;
        PreparedStatement psScore = null;
        try {
            final String strKetu = "欠";
            final String scoreSql = getRecordScoreTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, testItem);
            psScore = _db2.prepareStatement(scoreSql);
            scoreRs = psScore.executeQuery();
            while (scoreRs.next()) {
                String subclassCd = scoreRs.getString("SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    subclassCd = scoreRs.getString("CLASSCD") + scoreRs.getString("SCHOOL_KIND") + scoreRs.getString("CURRICULUM_CD") + scoreRs.getString("SUBCLASSCD");
                }
                final TestScore testScore = new TestScore(strKetu);
                testScore._name = scoreRs.getString("SUBCLASSABBV");
                retMap.put(subclassCd, testScore);
            }
            DbUtils.closeQuietly(null, psScore, scoreRs);

            PreparedStatement psChaircd = null;
            final String chaircdSql = getChaircdSql(student);
            psChaircd = _db2.prepareStatement(chaircdSql);

            for (Iterator it = retMap.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                TestScore testScore = (TestScore) retMap.get(subclassCd);
                //講座
                psChaircd.setString(1, subclassCd);
                ResultSet chaircdRs = null;
                chaircdRs = psChaircd.executeQuery();
                while (chaircdRs.next()) {
                    testScore._chaircd = chaircdRs.getString("CHAIRCD");
                }
                DbUtils.closeQuietly(chaircdRs);
            }
            DbUtils.closeQuietly(psChaircd);
        } finally {
            _db2.commit();
        }
        return retMap;
    }

    private String getRecordScoreTestAppointSql(
            final String year,
            final String seme,
            final String schregno,
            final TestItem testItem
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     L1.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("        AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("        AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + seme + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testItem._kindCd + testItem._itemCd + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        return stb.toString();
    }

    private String getChaircdSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     min(T1.CHAIRCD) as CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1, ");
        stb.append("     CHAIR_STD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param.getSeme() + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = ? AND ");
        } else {
            stb.append("     T1.SUBCLASSCD = ? AND ");
        }
        stb.append("     T2.YEAR = T1.YEAR AND ");
        stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
        stb.append("     T2.SCHREGNO = '" + student._schregno + "' ");

        return stb.toString();
    }

    private void setRank(final Student student, final TestItem testItem) throws SQLException {
        ResultSet rankRs = null;
        try {
            final String rankSql = getReportCardInfoTottori.getRecordRankTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, null, null, testItem._kindCd + testItem._itemCd, "1");
//            log.debug(rankSql);
            _db2.query(rankSql);
            rankRs = _db2.getResultSet();
            int subclassCount = 0;
            final Map testDataMap = (TreeMap) student._testMap.get(testItem._key);
            final TestScore allTestScore = (TestScore) student._testAllMap.get(testItem._key);
            while (rankRs.next()) {
                String subclassCd = rankRs.getString("SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    subclassCd = rankRs.getString("CLASSCD") + rankRs.getString("SCHOOL_KIND") + rankRs.getString("CURRICULUM_CD") + rankRs.getString("SUBCLASSCD");
                }
                if (null != testDataMap  && testDataMap.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) testDataMap.get(subclassCd);
                    testScore._score = rankRs.getString("SCORE");
                    testScore._gradeRank = rankRs.getString("GRADE_" + _param.getRankAvgField() + "RANK");
                    testScore._hrRank = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");
                    testScore._courseRank = rankRs.getString("COURSE_" + _param.getRankAvgField() + "RANK");
                    testScore._majorRank = rankRs.getString("MAJOR_" + _param.getRankAvgField() + "RANK");

                    subclassCount += 1;
                }
                String checkSubclassCd = subclassCd;
                if ("1".equals(_param._useCurriculumcd)) {
                    checkSubclassCd = subclassCd.substring(4);
                }
                if (checkSubclassCd.equals(SUBCLASSALL)) {
                    allTestScore._score = rankRs.getString("SCORE");
                    allTestScore._gradeRank = rankRs.getString("GRADE_" + _param.getRankAvgField() + "RANK");
                    allTestScore._hrRank = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");
                    allTestScore._courseRank = rankRs.getString("COURSE_" + _param.getRankAvgField() + "RANK");
                    allTestScore._majorRank = rankRs.getString("MAJOR_" + _param.getRankAvgField() + "RANK");
                    allTestScore._average = new BigDecimal(rankRs.getDouble("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
        } finally {
            _db2.commit();
        }
    }

    private void setRankChair(final Student student, final TestItem testItem) throws SQLException {
        ResultSet rankRs = null;
        try {
            final String rankSql = getReportCardInfoTottori.getRecordRankChairTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, null, null, testItem._kindCd + testItem._itemCd, "1");
            _db2.query(rankSql);
            rankRs = _db2.getResultSet();
            final Map testDataMap = (TreeMap) student._testMap.get(testItem._key);
            while (rankRs.next()) {
                String subclassCd = rankRs.getString("SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    subclassCd = rankRs.getString("CLASSCD") + rankRs.getString("SCHOOL_KIND") + rankRs.getString("CURRICULUM_CD") + rankRs.getString("SUBCLASSCD");
                }
                final String chairCd = rankRs.getString("CHAIRCD");
                if (null != testDataMap && testDataMap.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) testDataMap.get(subclassCd);

                    if (chairCd.equals(testScore._chaircd)) {
                        testScore._chairGradeRank = rankRs.getString("GRADE_" + _param.getRankAvgField() + "RANK");
                        testScore._chairHrRank = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");
                        testScore._chairCourseRank = rankRs.getString("COURSE_" + _param.getRankAvgField() + "RANK");
                        testScore._chairMajorRank = rankRs.getString("MAJOR_" + _param.getRankAvgField() + "RANK");
                    }
                }
            }
        } finally {
            _db2.commit();
        }
    }

    private void setAvg(final Student student, final String sql, final String avgDiv, final TestItem testItem) throws SQLException {
        ResultSet rankRs = null;
        // 学年/コースの平均
        try {
            _db2.query(sql);
            rankRs = _db2.getResultSet();
            int totalScore = 0;
            int totalCount = 0;
            final Map testDataMap = (TreeMap) student._testMap.get(testItem._key);
            final TestScore allTestScore = (TestScore) student._testAllMap.get(testItem._key);
            while (rankRs.next()) {
                String subclassCd = rankRs.getString("SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    subclassCd = rankRs.getString("CLASSCD") + rankRs.getString("SCHOOL_KIND") + rankRs.getString("CURRICULUM_CD") + rankRs.getString("SUBCLASSCD");
                }
                if (null != testDataMap && testDataMap.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) testDataMap.get(subclassCd);
                    if (rankRs.getString("AVG") != null) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        if (AVG_GRADE.equals(avgDiv)) {
                            testScore._gradeCnt = rankRs.getString("COUNT");
                            testScore._gradeAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            testScore._gradeHighScore = rankRs.getString("HIGHSCORE");
                            testScore._gradeLowScore = rankRs.getString("LOWSCORE");
                        } else if (AVG_HR.equals(avgDiv)) {
                            testScore._hrCnt = rankRs.getString("COUNT");
                            testScore._hrAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            testScore._hrHighScore = rankRs.getString("HIGHSCORE");
                            testScore._hrLowScore = rankRs.getString("LOWSCORE");
                        } else if (AVG_COURSE.equals(avgDiv)) {
                            testScore._courseCnt = rankRs.getString("COUNT");
                            testScore._courseAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            testScore._courseHighScore = rankRs.getString("HIGHSCORE");
                            testScore._courseLowScore = rankRs.getString("LOWSCORE");
                        } else if (AVG_MAJOR.equals(avgDiv)) {
                            testScore._majorCnt = rankRs.getString("COUNT");
                            testScore._majorAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            testScore._majorHighScore = rankRs.getString("HIGHSCORE");
                            testScore._majorLowScore = rankRs.getString("LOWSCORE");
                        }
                    }
                }
                String checkSubclassCd = subclassCd;
                if ("1".equals(_param._useCurriculumcd)) {
                    checkSubclassCd = subclassCd.substring(4);
                }
                if (checkSubclassCd.equals(SUBCLASSALL)) {
                    if (AVG_GRADE.equals(avgDiv)) {
                        allTestScore._gradeCnt = rankRs.getString("COUNT");
                    } else if (AVG_HR.equals(avgDiv)) {
                        allTestScore._hrCnt = rankRs.getString("COUNT");
                    } else if (AVG_COURSE.equals(avgDiv)) {
                        allTestScore._courseCnt = rankRs.getString("COUNT");
                    } else if (AVG_MAJOR.equals(avgDiv)) {
                        allTestScore._majorCnt = rankRs.getString("COUNT");
                    }
                }
                if (!checkSubclassCd.equals(SUBCLASSALL) && !checkSubclassCd.equals(SUBCLASS5) && !checkSubclassCd.equals(SUBCLASS3)) {
                    totalScore += rankRs.getInt("SCORE");
                    totalCount += rankRs.getInt("COUNT");
                }
            }
            if (totalCount > 0) {
                BigDecimal avg = new BigDecimal((double)totalScore / totalCount);
                if (AVG_GRADE.equals(avgDiv)) {
                    allTestScore._gradeAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                } else if (AVG_HR.equals(avgDiv)) {
                    allTestScore._hrAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                } else if (AVG_COURSE.equals(avgDiv)) {
                    allTestScore._courseAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                } else if (AVG_MAJOR.equals(avgDiv)) {
                    allTestScore._majorAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
        } finally {
            _db2.commit();
        }

    }

}

// eof
