// kanji=漢字
/*
 * $Id: adb37d77cdcc24f83fe5fd83883afda203650fa6 $
 *
 * 作成日: 2011/05/08 0:27:56 - JST
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfoTottori;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: adb37d77cdcc24f83fe5fd83883afda203650fa6 $
 */
public class KNJD154K_JUNIOR extends KNJD154K_Abstract {

    private static final Log log = LogFactory.getLog("KNJD154K_JUNIOR.class");

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

            PreparedStatement psMock = null;
            ResultSet rsMock = null;
            final String mockSql = getMockData(student);
            try {
                psMock = _db2.prepareStatement(mockSql);
                rsMock = psMock.executeQuery();
                while (rsMock.next()) {
                    final String semester = "";
                    final String kindCd = rsMock.getString("MOCKCD");
                    final String itemCd = "";
                    final String name = rsMock.getString("MOCKNAME1");
                    final String countFlg = "";
                    final String semeDetail = "";
                    final TestItem testItem = new TestItem(semester, kindCd, itemCd, name, countFlg, semeDetail);
                    student._mockList.add(testItem);
                }
            } finally {
                DbUtils.closeQuietly(null, psMock, rsMock);
                _db2.commit();
            }

            for (final Iterator iterator = student._mockList.iterator(); iterator.hasNext();) {
                final TestItem testItem = (TestItem) iterator.next();
                final Map setDefMap = new TreeMap();
                student._mockMap.put(testItem._key, setDefMap);
                TestScore setDefTestScore = new TestScore("");
                student._mockAllMap.put(testItem._key, setDefTestScore);
                setMockRank(student, testItem);
            }
            printDataList.add(student);
        }
        return printDataList;
    }

    private void setHead() {
        _svf.VrSetForm("KNJD154K_1.frm", 1);
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

        final Map classAllMap = new TreeMap();
        for (final Iterator itSub = _param._testList.iterator(); itSub.hasNext();) {
            final TestItem testItem = (TestItem) itSub.next();
            _svf.VrsOutn("EXAM_NAME", fieldNo, testItem._name);
            final Map testSub = (Map) student._testMap.get(testItem._key);
            int fieldCnt = 1;
            if (null != testSub) {
                for (final Iterator itSubclass = testSub.keySet().iterator(); itSubclass.hasNext();) {
                    final String subclassCd = (String) itSubclass.next();
                    final TestScore testScore = (TestScore) testSub.get(subclassCd);
                    String checkSubclassCd = subclassCd;
                    if ("1".equals(_param._useCurriculumcd)) {
                        checkSubclassCd = subclassCd.substring(4);
                    }
                    if (SUBCLASS5.equals(checkSubclassCd)) {
                        _svf.VrsOutn("CLASS_TOTAL", fieldNo, testScore._score);
                        _svf.VrsOutn("CLASS_RANK", fieldNo, testScore._gradeRank);
                        _svf.VrsOutn("CLASS_RANK_TOTAL", fieldNo, testScore._gradeCnt);
                    } else if (!SUBCLASS3.equals(checkSubclassCd)) {
                        final String subName = null != testScore._name ? testScore._name : "";
                        _svf.VrsOutn("CLASS" + fieldCnt, fieldNo, subName);
                        _svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
                        _svf.VrsOutn("AVE_GRADE" + fieldCnt, fieldNo, testScore._gradeAvg);
                        _svf.VrsOutn("RANK_GRADE" + fieldCnt, fieldNo, testScore._gradeRank);
                        fieldCnt++;
                        final AllClassData allClassData = classAllMap.containsKey(subclassCd) ? (AllClassData) classAllMap.get(subclassCd) : new AllClassData();
                        allClassData.setData(testScore._score);
                        classAllMap.put(subclassCd, allClassData);
                    }
                }
            }
            final TestScore allTestScore = (TestScore) student._testAllMap.get(testItem._key);
            _svf.VrsOutn("ALL_CLASS_TOTAL", fieldNo, allTestScore._score);
            _svf.VrsOutn("MAJOR_RANK", fieldNo, allTestScore._gradeRank);
            _svf.VrsOutn("MAJOR_RANK_TOTAL", fieldNo, allTestScore._gradeCnt);

            fieldNo++;
        }

        fieldNo = 1;
        for (final Iterator itAll = classAllMap.keySet().iterator(); itAll.hasNext();) {
            final String subclassCd = (String) itAll.next();
            final AllClassData allClassData = (AllClassData) classAllMap.get(subclassCd);
            _svf.VrsOut("TOTAL_CLASS" + fieldNo, String.valueOf(allClassData._goukei));
            _svf.VrsOut("AVE_CLASS" + fieldNo, allClassData.getAvg());
            fieldNo++;
        }

        fieldNo = 1;
        for (final Iterator iterator = student._mockList.iterator(); iterator.hasNext();) {
            final TestItem testItem = (TestItem) iterator.next();
            _svf.VrsOutn("MOCK_EXAM_NAME", fieldNo, testItem._name);
            final Map mockSub = (Map) student._mockMap.get(testItem._key);
            int fieldCnt = 1;
            if (null != mockSub) {
                for (final Iterator itSubclass = mockSub.keySet().iterator(); itSubclass.hasNext();) {
                    final String subclassCd = (String) itSubclass.next();
                    final TestScore testScore = (TestScore) mockSub.get(subclassCd);
                    String checkSubclassCd = subclassCd;
                    if ("1".equals(_param._useCurriculumcd)) {
                        checkSubclassCd = subclassCd.substring(4);
                    }
                    if (SUBCLASS5.equals(checkSubclassCd)) {
                        _svf.VrsOutn("MOCK_CLASS_TOTAL", fieldNo, testScore._score);
                        _svf.VrsOutn("MOCK_CLASS_RANK", fieldNo, testScore._gradeRank);
                        _svf.VrsOutn("MOCK_CLASS_RANK_TOTAL", fieldNo, testScore._gradeCnt);
                    } else if (!SUBCLASS3.equals(checkSubclassCd)) {
                        final String subName = null != testScore._name ? testScore._name : "";
                        _svf.VrsOutn("MOCK_CLASS" + fieldCnt, fieldNo, subName);
                        _svf.VrsOutn("MOCK_AVE_GRADE" + fieldCnt, fieldNo, testScore._gradeAvg);
                        _svf.VrsOutn("MOCK_RANK_GRADE" + fieldCnt, fieldNo, testScore._gradeRank);
                        fieldCnt++;
                    }
                }
            }
            final TestScore allTestScore = (TestScore) student._mockAllMap.get(testItem._key);
            _svf.VrsOutn("MOCK_ALL_CLASS_TOTAL", fieldNo, allTestScore._score);
            _svf.VrsOutn("MOCK_MAJOR_RANK", fieldNo, allTestScore._gradeRank);
            _svf.VrsOutn("MOCK_MAJOR_RANK_TOTAL", fieldNo, allTestScore._gradeCnt);

            fieldNo++;
        }
        _svf.VrEndPage();
    }

    private Map setChairStd(final Student student, final TestItem paraTestItem) throws SQLException {
        final Map retMap = new TreeMap();
        ResultSet scoreRs = null;
        PreparedStatement psScore = null;
        try {
            for (final Iterator iterator = _param._testList.iterator(); iterator.hasNext();) {
                final TestItem testItem = (TestItem) iterator.next();
                final String scoreSql = getRecordScoreTestAppointSql(_param._ctrlYear, testItem._semester, student._schregno, testItem);
                psScore = _db2.prepareStatement(scoreSql);
                scoreRs = psScore.executeQuery();
                while (scoreRs.next()) {
                    String subclassCd = scoreRs.getString("SUBCLASSCD");
                    if ("1".equals(_param._useCurriculumcd)) {
                        subclassCd = scoreRs.getString("CLASSCD") + scoreRs.getString("SCHOOL_KIND") + scoreRs.getString("CURRICULUM_CD") + scoreRs.getString("SUBCLASSCD");
                    }
                    final String strKetu = getKetu(student._schregno, subclassCd, testItem, paraTestItem);
                    final TestScore testScore = new TestScore(strKetu);
                    testScore._name = scoreRs.getString("SUBCLASSABBV");
                    retMap.put(subclassCd, testScore);
                }
                DbUtils.closeQuietly(null, psScore, scoreRs);
            }

            final TestScore all5TestScore = new TestScore("");
            retMap.put(SUBCLASS5, all5TestScore);

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

    private String getKetu(final String schregNo, final String subclassCd, final TestItem testItem, final TestItem paraTestItem) throws SQLException {
        String ketu = "";
        if (testItem._key.equals(paraTestItem._key)) {
            final String sql = getKetuSql(schregNo, subclassCd, testItem);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ketu = rs.getString("KETU");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }
        }
        return ketu;
    }
    private String getKetuSql(
            final String schregno,
            final String subclassCd,
            final TestItem testItem
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     '欠' AS KETU ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + testItem._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testItem._kindCd + testItem._itemCd + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '" + subclassCd + "' ");
        } else {
            stb.append("     AND T1.SUBCLASSCD = '" + subclassCd + "' ");
        }
        stb.append("     AND T1.SCORE IS NULL ");
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

    private String getMockData(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.MOCKCD, ");
        stb.append("     L1.MOCKNAME1 ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_MST L1 ON T1.MOCKCD = L1.MOCKCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("     AND T1.MOCKDIV = '2' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.MOCKCD ");

        return stb.toString();
    }

    private void setMockRank(final Student student, final TestItem testItem) throws SQLException {
        ResultSet rankRs = null;
        try {
            final String rankSql = getMockRank(student, testItem);
            _db2.query(rankSql);
            rankRs = _db2.getResultSet();
            final Map testDataMap = (Map) student._mockMap.get(testItem._key);
            final TestScore allTestScore = (TestScore) student._mockAllMap.get(testItem._key);
            while (rankRs.next()) {
                final String mockSubclassCd = rankRs.getString("MOCK_SUBCLASS_CD");
                TestScore testScore = null;
                if (null == testDataMap  || !testDataMap.containsKey(mockSubclassCd)) {
                    testScore = new TestScore("");
                    testDataMap.put(mockSubclassCd, testScore);
                }
                testScore = (TestScore) testDataMap.get(mockSubclassCd);
                if (!mockSubclassCd.equals(SUBCLASSALL)) {
                    testScore._name = rankRs.getString("SUBCLASS_NAME");
                    testScore._score = rankRs.getString("SCORE");
                    testScore._gradeRank = rankRs.getString("GRADE_RANK");
                    testScore._gradeCnt = rankRs.getString("G_COUNT");
                    testScore._gradeAvg = new BigDecimal(rankRs.getDouble("G_AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    testScore._hrRank = rankRs.getString("CLASS_RANK");
                    testScore._hrCnt = rankRs.getString("H_COUNT");
                    testScore._hrAvg = new BigDecimal(rankRs.getDouble("H_AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    testScore._courseRank = rankRs.getString("COURSE_RANK");
                    testScore._courseCnt = rankRs.getString("C_COUNT");
                    testScore._courseAvg = new BigDecimal(rankRs.getDouble("C_AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                } else  {
                    allTestScore._score = rankRs.getString("SCORE");
                    allTestScore._gradeRank = rankRs.getString("GRADE_RANK");
                    allTestScore._gradeCnt = rankRs.getString("G_COUNT");
                    allTestScore._gradeAvg = new BigDecimal(rankRs.getDouble("G_AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    allTestScore._hrRank = rankRs.getString("CLASS_RANK");
                    allTestScore._hrCnt = rankRs.getString("H_COUNT");
                    allTestScore._hrAvg = new BigDecimal(rankRs.getDouble("H_AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    allTestScore._courseRank = rankRs.getString("COURSE_RANK");
                    allTestScore._courseCnt = rankRs.getString("C_COUNT");
                    allTestScore._courseAvg = new BigDecimal(rankRs.getDouble("C_AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
        } finally {
            _db2.commit();
        }
    }

    private String getMockRank(final Student student, final TestItem testItem) {
        final String[] grade = StringUtils.split(_param._gradeHrClass, '-');
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.MOCKCD, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     L1.SUBCLASS_NAME, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.GRADE_RANK, ");
        stb.append("     T1.GRADE_DEVIATION, ");
        stb.append("     T1.CLASS_RANK, ");
        stb.append("     T1.CLASS_DEVIATION, ");
        stb.append("     T1.COURSE_RANK, ");
        stb.append("     T1.COURSE_DEVIATION, ");
        stb.append("     GR.AVG AS G_AVG, ");
        stb.append("     GR.AVG_KANSAN AS G_AVG_KANSAN, ");
        stb.append("     GR.COUNT AS G_COUNT, ");
        stb.append("     HR.AVG AS H_AVG, ");
        stb.append("     HR.AVG_KANSAN AS H_AVG_KANSAN, ");
        stb.append("     HR.COUNT AS H_COUNT, ");
        stb.append("     CR.AVG AS C_AVG, ");
        stb.append("     CR.AVG_KANSAN AS C_AVG_KANSAN, ");
        stb.append("     CR.COUNT AS C_COUNT ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_SUBCLASS_MST L1 ON T1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ");
        stb.append("     LEFT JOIN MOCK_AVERAGE_DAT GR ON T1.YEAR = GR.YEAR ");
        stb.append("          AND T1.MOCKCD = GR.MOCKCD ");
        stb.append("          AND T1.MOCK_SUBCLASS_CD = GR.MOCK_SUBCLASS_CD ");
        stb.append("          AND GR.AVG_DIV = '1' ");
        stb.append("          AND GR.GRADE = '" + grade[0] + "' ");
        stb.append("     LEFT JOIN MOCK_AVERAGE_DAT HR ON T1.YEAR = HR.YEAR ");
        stb.append("          AND T1.MOCKCD = HR.MOCKCD ");
        stb.append("          AND T1.MOCK_SUBCLASS_CD = HR.MOCK_SUBCLASS_CD ");
        stb.append("          AND HR.AVG_DIV = '2' ");
        stb.append("          AND HR.GRADE = '" + grade[0] + "' ");
        stb.append("     LEFT JOIN MOCK_AVERAGE_DAT CR ON T1.YEAR = CR.YEAR ");
        stb.append("          AND T1.MOCKCD = CR.MOCKCD ");
        stb.append("          AND T1.MOCK_SUBCLASS_CD = CR.MOCK_SUBCLASS_CD ");
        stb.append("          AND CR.AVG_DIV = '3' ");
        stb.append("          AND CR.GRADE = '" + grade[0] + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.MOCKCD = '" + testItem._key + "' ");
        stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("     AND T1.MOCKDIV = '2' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.MOCKCD, ");
        stb.append("     T1.MOCK_SUBCLASS_CD ");

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

    private class AllClassData {
        int _goukei = 0;
        int _cnt = 0;
 
        private void setData(final String score) {
            int goukei = 0;
            int cnt = 0;
            if (!"欠".equals(score) && !"".equals(score)) {
                goukei = Integer.parseInt(score);
                cnt = 1;
            }
            _goukei += goukei;
            _cnt += cnt;
        }

        private String getAvg() {
            if (_cnt > 0) {
                return new BigDecimal(_goukei).divide(new BigDecimal(_cnt), 1, BigDecimal.ROUND_HALF_UP).toString();
            } else {
                return "";
            }
        }
    }
}

// eof
