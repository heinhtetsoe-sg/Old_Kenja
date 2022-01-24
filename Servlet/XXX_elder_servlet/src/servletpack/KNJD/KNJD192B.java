// kanji=漢字
/*
 * $Id: 7712f5655068ec8b502ecdf42f4ed80e728bbddb $
 *
 * 作成日: 2008/05/19 15:38:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfo;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 7712f5655068ec8b502ecdf42f4ed80e728bbddb $
 */
public class KNJD192B {

    private static final Log log = LogFactory.getLog(KNJD192B.class);

    Param _param;

    private static final String SCORE = "SCORE";
    private static final String AVG = "AVG";
    private static final String RANK = "RANK";
    private static final String RANK2 = "RANK2";

    private static final String SUBCLASS_SIMO4KETA0 = "0000";
    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASSALL = "999999";

    private static final int SUBCLASS_MAX_CNT = 15;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(response, db2, svf);

        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    /**
     * @param response
     * @param db2
     */
    private boolean printMain(final HttpServletResponse response, final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        final List studentList = getStudentList(db2);
        final List printDataList = getPrintData(db2, studentList);
        setHead(svf);
        int cnt = 0;
        String befGradeClass = "";
        for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (hasData && !befGradeClass.equals(student._grade + student._hrClass)) {
                cnt = pageChange(svf);
            }

            //次に印字する生徒が複数ページにまたがる場合、印字する前に改ページする
            if (student._testSubclass.size() > SUBCLASS_MAX_CNT) {
                int fukusuuCnt = cnt + 1 + student._testSubclass.size() / SUBCLASS_MAX_CNT;
                log.debug(""+fukusuuCnt);
                if (fukusuuCnt > 5) {
                    cnt = pageChange(svf);
                }
            }

            cnt++;
            cnt = setPrintOut(svf, student, cnt);

            if (cnt == 5) {
                cnt = pageChange(svf);
            }

            befGradeClass = student._grade + student._hrClass;
            hasData = true;
        }
        if (cnt > 0) {
            svf.VrEndPage();
        }
        return hasData;
    }

    private int pageChange(final Vrw32alp svf) {
        svf.VrEndPage();
        return 0;
    }

    private int setPrintOut(final Vrw32alp svf, final Student student, int fieldNo) {
        log.debug(student);
        svf.VrsOutn("NENDO", fieldNo, _param.changePrintYear());
        svf.VrsOutn("SEMESTER", fieldNo, _param._semesterName);
        svf.VrsOutn("TESTNAME", fieldNo, _param._testName);
        svf.VrsOutn("CLASS_AVG_NAME", fieldNo, _param.getRankName2("AVG"));
        svf.VrsOutn("CLASS_RANK_NAME", fieldNo, _param.getRankName2("RANK"));
        svf.VrsOutn("AVG_NAME", fieldNo, _param.getRankName1());
        svf.VrsOutn("RANK_NAME", fieldNo, _param.getRankName1());
        svf.VrsOutn("HR_NAME", fieldNo, student._hrName + "(" + student._attendNo + ")");
        svf.VrsOutn("NAME", fieldNo, student._name);

        int fieldCnt = 0;
        int kettenSubclassCount = 0;
        for (final Iterator itSubclass = student._testSubclass.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

            //科目列がオーバーした場合、次の行に印字する
            if (fieldCnt == SUBCLASS_MAX_CNT) {
                fieldCnt = 0;
                fieldNo++;
            }

            fieldCnt++;
            svf.VrsOutn("SUBCLASS" + fieldCnt, fieldNo, testScore._name);
            svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
            if (!"欠".equals(testScore._score)) {
                int score = Integer.parseInt(testScore._score);
                if (testScore.isKetten(score)) {
                    svf.VrAttributen("SCORE" + fieldCnt, fieldNo, "Paint=(1,70,1),Bold=1");
                    kettenSubclassCount += 1;
                }
            }
            
            if ("1".equals(_param._groupDiv)) {
                svf.VrsOutn("CLASS_AVERAGE" + fieldCnt, fieldNo, testScore._avgHr);
                svf.VrsOutn("AVERAGE" + fieldCnt, fieldNo, testScore._avgGrade);
            } else if ("2".equals(_param._groupDiv)) {
                svf.VrsOutn("CLASS_AVERAGE" + fieldCnt, fieldNo, testScore._avgHr);
                svf.VrsOutn("AVERAGE" + fieldCnt, fieldNo, testScore._avgCourse);
            } else if ("3".equals(_param._groupDiv)) {
                svf.VrsOutn("CLASS_AVERAGE" + fieldCnt, fieldNo, testScore._avgCourse);
                svf.VrsOutn("AVERAGE" + fieldCnt, fieldNo, testScore._avgGrade);
            }
            if (_param.isAssessLevelMst()) {
                String assessLevelGrade  = testScore._assessLevelGrade;
                String assessLevelCourse = testScore._assessLevelCourse;
                if ("1".equals(_param._groupDiv)) {
                    svf.VrsOutn("RANK" + fieldCnt, fieldNo, "1".equals(_param._assessLevelRep) && ("1".equals(assessLevelGrade) || "2".equals(assessLevelGrade)) ? "3" : assessLevelGrade);
                } else if ("2".equals(_param._groupDiv)) {
                    svf.VrsOutn("RANK" + fieldCnt, fieldNo, "1".equals(_param._assessLevelRep) && ("1".equals(assessLevelCourse) || "2".equals(assessLevelCourse)) ? "3" : assessLevelCourse);
                } else if ("3".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK" + fieldCnt, fieldNo, "1".equals(_param._assessLevelRep) && ("1".equals(assessLevelCourse) || "2".equals(assessLevelCourse)) ? "3" : assessLevelCourse);
                    svf.VrsOutn("RANK" + fieldCnt, fieldNo, "1".equals(_param._assessLevelRep) && ("1".equals(assessLevelGrade) || "2".equals(assessLevelGrade)) ? "3" : assessLevelGrade);
                }
            } else {
                if ("1".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK" + fieldCnt, fieldNo, testScore._rankHr);
                    svf.VrsOutn("RANK" + fieldCnt, fieldNo, testScore._rankGrade);
                } else if ("2".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK" + fieldCnt, fieldNo, testScore._rankHr);
                    svf.VrsOutn("RANK" + fieldCnt, fieldNo, testScore._rankCourse);
                } else if ("3".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK" + fieldCnt, fieldNo, testScore._rankCourse);
                    svf.VrsOutn("RANK" + fieldCnt, fieldNo, testScore._rankGrade);
                }
            }
            log.debug(testScore);
        }

        //科目列がオーバーした場合、次の行に印字する
        if (student._testSubclass.size() > SUBCLASS_MAX_CNT) {
            int subCnt = student._testSubclass.size();
            log.debug(""+subCnt);
            svf.VrsOutn("NENDO", fieldNo, _param.changePrintYear());
            svf.VrsOutn("SEMESTER", fieldNo, _param._semesterName);
            svf.VrsOutn("TESTNAME", fieldNo, _param._testName);
            svf.VrsOutn("CLASS_AVG_NAME", fieldNo, _param.getRankName2("AVG"));
            svf.VrsOutn("CLASS_RANK_NAME", fieldNo, _param.getRankName2("RANK"));
            svf.VrsOutn("AVG_NAME", fieldNo, _param.getRankName1());
            svf.VrsOutn("RANK_NAME", fieldNo, _param.getRankName1());
            svf.VrsOutn("HR_NAME", fieldNo, student._hrName + "(" + student._attendNo + ")");
            svf.VrsOutn("NAME", fieldNo, student._name);
        }

        if ("1".equals(_param._groupDiv)) {
            svf.VrsOutn("CLASS_AVERAGE", fieldNo, student._testAll._avgHr);
            svf.VrsOutn("TOTAL_AVERAGE", fieldNo, student._testAll._avgGrade);
        } else if ("2".equals(_param._groupDiv)) {
            svf.VrsOutn("CLASS_AVERAGE", fieldNo, student._testAll._avgHr);
            svf.VrsOutn("TOTAL_AVERAGE", fieldNo, student._testAll._avgCourse);
        } else if ("3".equals(_param._groupDiv)) {
            svf.VrsOutn("CLASS_AVERAGE", fieldNo, student._testAll._avgCourse);
            svf.VrsOutn("TOTAL_AVERAGE", fieldNo, student._testAll._avgGrade);
        }

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", fieldNo, student._testAll._score);
            if (_param.isAssessLevelMst()) {
            } else {
                final TestScore testScore = student._testAll;
                if ("1".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK", fieldNo, testScore._rankHr);
                    svf.VrsOutn("TOTAL_RANK", fieldNo, testScore._rankGrade);
                } else if ("2".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK", fieldNo, testScore._rankHr);
                    svf.VrsOutn("TOTAL_RANK", fieldNo, testScore._rankCourse);
                } else if ("3".equals(_param._groupDiv)) {
                    svf.VrsOutn("CLASS_RANK", fieldNo, testScore._rankCourse);
                    svf.VrsOutn("TOTAL_RANK", fieldNo, testScore._rankGrade);
                }
            }
            svf.VrsOutn("FAIL", fieldNo, String.valueOf(kettenSubclassCount));
            log.debug("欠点科目数 = " + kettenSubclassCount);
            svf.VrsOutn("AVERAGEL_SCORE", fieldNo, student._averageScore);
            log.debug("個人平均点 = " + student._averageScore);
        }
        return fieldNo;
    }

    private void setHead(final Vrw32alp svf) {
        svf.VrSetForm(_param.isAssessLevelMst() ? "KNJD192B.frm" : "KNJD192B_2.frm", 1);
    }

    private List getPrintData(final DB2UDB db2, final List studentList) throws SQLException {
        List printDataList = new ArrayList();
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            setMapTestSubclassAvg(db2, student);
            setScore(db2, student);
            setRank(db2, student);
            setAvg(db2, student);
            if (!_param.isKetten()) {
                setKetten(db2, student);
            }
            if (_param.isAssessLevelMst()) {
                setAssessLevelMst(db2, student);
            }
            printDataList.add(student);
        }
        return printDataList;
    }
    
    private String getSubclasscd(final ResultSet rs) throws SQLException {
        final String subclassCd = rs.getString("SUBCLASSCD");
        if (SUBCLASSALL.equals(subclassCd) || SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd)) {
            return subclassCd;
        }
        if ("1".equals(_param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclassCd;
        }
        return subclassCd;
    }

    private void setMapTestSubclassAvg(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rankRs = null;
        // 学級の平均
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, course, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, course, _param._kindItem);
            }
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                final String avg = rankRs.getString("AVG");
                student._testSubclassAvg.put(subclassCd, avg);
            }
        } finally {
            DbUtils.closeQuietly(rankRs);
            db2.commit();
        }
    }

    private void setScore(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet scoreRs = null;
        try {
            final String scoreSql = getReportCardInfo.getRecordScoreTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._scoreDiv, _param._kindItem, _param._useCurriculumcd);
            db2.query(scoreSql);
            scoreRs = db2.getResultSet();
            while (scoreRs.next()) {
                final String subclassCd = getSubclasscd(scoreRs);
                if (student._testSubclassAvg.containsKey(subclassCd)) {
                    final TestScore testScore = new TestScore("欠");
                    testScore._name = scoreRs.getString("SUBCLASSABBV");
                    student._testSubclass.put(subclassCd, testScore);
                }
            }
        } finally {
            db2.commit();
        }
    }

    private void setRank(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rankRs = null;
        try {
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordRankVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordRankTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem);
            }
            log.debug(rankSql);
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            boolean hasScore = false;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                if (student._testSubclass.containsKey(subclassCd)) {
                    hasScore = true;
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._score = rankRs.getString("SCORE");
                    testScore._rankGrade = rankRs.getString(_param.getGradeRankField());
                    testScore._rankCourse = rankRs.getString(_param.getCourseRankField());
                    testScore._rankHr = rankRs.getString(_param.getClassRankField());
                }
                if (subclassCd.equals(SUBCLASSALL)) {
                    student._testAll._score = rankRs.getString("SCORE");
                    student._testAll._rankGrade = rankRs.getString(_param.getGradeRankField());
                    student._testAll._rankCourse = rankRs.getString(_param.getCourseRankField());
                    student._testAll._rankHr = rankRs.getString(_param.getClassRankField());
                    if (null != rankRs.getString("AVG")) {
                        student._averageScore = new BigDecimal(rankRs.getDouble("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
            }
            student._hasScore = hasScore;
            
        } finally {
            db2.commit();
        }
    }

    private void setAvg(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rankRs = null;
        // 学年の平均
        final String course = student._courseCd + student._majorCd + student._courseCode;
        try {
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "1", student._grade, student._hrClass, course, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "1", student._grade, student._hrClass, course, _param._kindItem);
            }
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            BigDecimal totalAvg = new BigDecimal(0);
            int totalCnt = 0;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    if (rankRs.getString("AVG") != null) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        if (!_param._isChiben) {
                            totalAvg = totalAvg.add(avg);
                            totalCnt++;
                        }
                        testScore._avgGrade = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
                if (_param._isChiben) {
                    final String subclassSimo4Keta0 = subclassCd.substring(2);
                    if (!SUBCLASS_SIMO4KETA0.equals(subclassSimo4Keta0) && 
                        !SUBCLASS3.equals(subclassCd) && !SUBCLASS5.equals(subclassCd) && !SUBCLASSALL.equals(subclassCd)) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        totalAvg = totalAvg.add(avg);
                        totalCnt++;
                    }
                }
            }
            if (totalCnt > 0) {
                BigDecimal avg = new BigDecimal(totalAvg.doubleValue() / totalCnt);
                student._testAll._avgGrade = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
        } finally {
            db2.commit();
        }
        
        // コースの平均
        try {
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "3", student._grade, student._hrClass, course, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "3", student._grade, student._hrClass, course, _param._kindItem);
            }
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            BigDecimal totalAvg = new BigDecimal(0);
            int totalCnt = 0;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    if (rankRs.getString("AVG") != null) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        if (!_param._isChiben) {
                            totalAvg = totalAvg.add(avg);
                            totalCnt++;
                        }
                        testScore._avgCourse = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
                if (_param._isChiben) {
                    final String subclassSimo4Keta0 = subclassCd.substring(2);
                    if (!SUBCLASS_SIMO4KETA0.equals(subclassSimo4Keta0) && 
                        !SUBCLASS3.equals(subclassCd) && !SUBCLASS5.equals(subclassCd) && !SUBCLASSALL.equals(subclassCd)) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        totalAvg = totalAvg.add(avg);
                        totalCnt++;
                    }
                }
            }
            if (totalCnt > 0) {
                BigDecimal avg = new BigDecimal(totalAvg.doubleValue() / totalCnt);
                student._testAll._avgCourse = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
        } finally {
            db2.commit();
        }

        // 学級の平均
        try {
            final String rankSql;
            if (_param.isVdat()) {
                rankSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, null, _param._kindItem);
            } else {
                rankSql = getReportCardInfo.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, null, _param._kindItem);
            }
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            BigDecimal totalAvg = new BigDecimal(0);
            int totalCnt = 0;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    if (rankRs.getString("AVG") != null) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        if (!_param._isChiben) {
                            totalAvg = totalAvg.add(avg);
                            totalCnt++;
                        }
                        testScore._avgHr = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
                if (_param._isChiben) {
                    final String subclassSimo4Keta0 = subclassCd.substring(2);
                    if (!SUBCLASS_SIMO4KETA0.equals(subclassSimo4Keta0) && 
                        !SUBCLASS3.equals(subclassCd) && !SUBCLASS5.equals(subclassCd) && !SUBCLASSALL.equals(subclassCd)) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        totalAvg = totalAvg.add(avg);
                        totalCnt++;
                    }
                }
            }
            if (totalCnt > 0) {
                BigDecimal avg = new BigDecimal(totalAvg.doubleValue() / totalCnt);
                student._testAll._avgHr = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
        } finally {
            db2.commit();
        }
    }

    private void setAssessLevelMst(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet assessLevelRs = null;
        // 学年の評定段階値
        try {
            final String assessLevelSql = getReportCardInfo.getAssessLevelMstSql(_param._year, _param._semester, student._schregno, null, null, "1", _param._kindItem, _param._useCurriculumcd);
            db2.query(assessLevelSql);
            assessLevelRs = db2.getResultSet();
            while (assessLevelRs.next()) {
                final String subclassCd = getSubclasscd(assessLevelRs);
                if (!_param._semester.equals(assessLevelRs.getString("SEMESTER"))) {
                    continue;
                }
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._assessLevelGrade = assessLevelRs.getString("ASSESSLEVEL");
                }
            }
        } finally {
            db2.commit();
        }

        // コースの評定段階値
        try {
            final String assessLevelSql = getReportCardInfo.getAssessLevelMstSql(_param._year, _param._semester, student._schregno, null, null, "3", _param._kindItem, _param._useCurriculumcd);
            db2.query(assessLevelSql);
            assessLevelRs = db2.getResultSet();
            while (assessLevelRs.next()) {
                final String subclassCd = getSubclasscd(assessLevelRs);
                if (!_param._semester.equals(assessLevelRs.getString("SEMESTER"))) {
                    continue;
                }
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._assessLevelCourse = assessLevelRs.getString("ASSESSLEVEL");
                }
            }
        } finally {
            db2.commit();
        }
    }

    private void setKetten(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rs = null;
        // 欠点対象
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String sql = getKettenSql(student._schregno, student._grade, course);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String subclassCd = getSubclasscd(rs);
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._slump = rs.getString("SLUMP");
                    testScore._passScore = rs.getString("PASS_SCORE");
                }
            }
        } finally {
            db2.commit();
        }
    }

    private String getKettenSql(final String schregno, final String grade, final String course) {
        final StringBuffer stb = new StringBuffer();
        if (_param.isRecordSlump()) {
            //成績不振科目データの表
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD, ");
            stb.append("     SLUMP, ");
            stb.append("     cast(null as smallint) as PASS_SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_SLUMP_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' AND ");
            stb.append("     SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     TESTKINDCD || TESTITEMCD = '" + _param.getRecordSlumpTestcd() + "' AND ");
            stb.append("     SCHREGNO = '" + schregno + "' ");
        }
        if (_param.isPerfectRecord()) {
            //満点マスタの表
            stb.append(" WITH PERFECT_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
            stb.append("     CLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD, ");
            stb.append("     MIN(DIV) AS DIV ");
            stb.append(" FROM ");
            stb.append("     PERFECT_RECORD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._kindItem + "' ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     TESTKINDCD || TESTITEMCD, ");
            stb.append("     CLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD ");
            stb.append(" ) ");

            stb.append(" SELECT DISTINCT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     cast(null as varchar(1)) as SLUMP, ");
            stb.append("     T1.PASS_SCORE ");
            stb.append(" FROM ");
            stb.append("     PERFECT_RECORD_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             PERFECT_T E1 ");
            stb.append("         WHERE ");
            stb.append("             E1.YEAR = T1.YEAR ");
            stb.append("             AND E1.SEMESTER = T1.SEMESTER ");
            stb.append("             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ");
            stb.append("             AND E1.CLASSCD = T1.CLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("             AND E1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND E1.DIV = T1.DIV ");
            stb.append("     ) ");
            stb.append("     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '" + grade + "' END ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') THEN '00000000' ELSE '" + course + "' END ");
        }
        return stb.toString();
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List rtnStudent = new ArrayList();
        ResultSet rs = null;
        try {
            final String sql = getStudentInfoSql();
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                Student student = new Student(rs.getString("SCHREGNO"),
                                              rs.getString("GRADE"),
                                              rs.getString("HR_CLASS"),
                                              rs.getString("ATTENDNO"),
                                              rs.getString("HR_NAME"),
                                              rs.getString("HR_NAMEABBV"),
                                              "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"),
                                              rs.getString("COURSECD"),
                                              rs.getString("COURSENAME"),
                                              rs.getString("MAJORCD"),
                                              rs.getString("MAJORNAME"),
                                              rs.getString("COURSECODE"),
                                              rs.getString("COURSECODENAME"));
                rtnStudent.add(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnStudent;
    }


    class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final Map _testSubclass;
        final Map _testSubclassAvg;
        TestScore _testAll;
        boolean _hasScore;
        /** 個人の平均点 */
        String _averageScore;
        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _testSubclass = new TreeMap();
            _testAll = new TestScore("");
            _testSubclassAvg = new TreeMap();
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }

    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VSCH.SCHREGNO, ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO, ");
        stb.append("     VSCH.HR_NAME, ");
        stb.append("     VSCH.HR_NAMEABBV, ");
        stb.append("     VSCH.NAME, ");
        stb.append("     BASE.REAL_NAME, ");
        stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
        stb.append("     VSCH.COURSECD, ");
        stb.append("     L1.COURSENAME, ");
        stb.append("     VSCH.MAJORCD, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     VSCH.COURSECODE, ");
        stb.append("     L2.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO VSCH ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
        stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
        stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
        stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     VSCH.YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._semester)) {
            stb.append("     AND VSCH.SEMESTER = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("     AND VSCH.SEMESTER = '" + _param._semester + "' ");
        }
        if (_param.isClass()) {
            stb.append("     AND VSCH.GRADE = '" + _param._grade + "' ");
            stb.append("     AND VSCH.HR_CLASS IN " + _param._selectInstate + " ");
        } else {
            stb.append("     AND VSCH.GRADE = '" + _param._grade + "' ");
            stb.append("     AND VSCH.HR_CLASS = '" + _param._hrClass + "' ");
            stb.append("     AND VSCH.SCHREGNO IN " + _param._selectInstate + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO ");

        return stb.toString();
    }
 
    class TestScore {
        String _name;
        String _score;
        String _avgGrade;
        String _avgCourse;
        String _avgHr;
        String _rankGrade;
        String _rankCourse;
        String _rankHr;
        String _slump;
        String _passScore;
        String _assessLevelGrade;
        String _assessLevelCourse;

        public TestScore(String score) {
            _score = score;
        }

        private int getFailValue() {
            if (_param.isPerfectRecord() && null != _passScore) {
                return Integer.parseInt(_passScore);
            } else if (_param.isKetten() && null != _param._ketten && !"".equals(_param._ketten)) {
                return Integer.parseInt(_param._ketten);
            }
            return -1;
        }
        
        private boolean isKetten(int score) {
            if (_param.isRecordSlump()) {
                return "1".equals(_slump);
            } else {
                return score < getFailValue();
            }
//            return score < _param.getFailValue();
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _avgGrade
                    + " 席次：" + _rankGrade;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    private class Param {
        final String _year;
        final String _categoryIsClass;
        final String _kindItem;
        final String _scoreDiv;
        final String _testName;
        final String _semester;
        final String _ctrlSeme;
        final String _semesterName;
        final String _grade;
        final String _hrClass;
        final String _groupDiv; // 1:学級・学年、2:学級・コース、3:コース・学年
        final String _rankDiv;
        final String[] _selectData;
        final String _selectInstate;
        final String _ketten;
        final String _checkKettenDiv; //欠点プロパティ 1,2,設定無し(1,2以外)
        final String _useCurriculumcd; //教育課程コード
        final String _outputAssessLevel;
        final String _assessLevelRep;
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        final boolean _isChiben;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _kindItem = request.getParameter("TESTCD");
            _scoreDiv = _kindItem.equals("9900") ? "00" : "01";
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _groupDiv = request.getParameter("GROUP_DIV");
            _rankDiv = request.getParameter("RANK_DIV");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _selectInstate = getInstate(_selectData);
            _outputAssessLevel = request.getParameter("ASSESS_LEVEL");
            _assessLevelRep = request.getParameter("ASSESS_LEVEL_REP");

            _z010 = setNameMst(db2, "Z010", "00");
            _z012 = setNameMst(db2, "Z012", "01");
            _isSeireki = _z012.equals("2") ? true : false;
            _isChiben = "CHIBEN".equals(_z010);
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND TESTKINDCD || TESTITEMCD = '" + _kindItem + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        /**
         * @param selectData
         * @return
         */
        private String getInstate(final String[] selectData) {
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("('");
            for (int i = 0; i < selectData.length; i++) {
                stb.append(sep + selectData[i]);
                sep = "','";
            }
            stb.append("')");

            return stb.toString();
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            db2.query(getNameMst(_year, namecd1, namecd2));
            ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private boolean isClass() {
            return _categoryIsClass.equals("1");
        }

        private String getGradeRankField() {
            return "1".equals(_rankDiv) ? "GRADE_RANK" : "2".equals(_rankDiv) ? "GRADE_AVG_RANK" : "GRADE_DEVIATION_RANK";
        }

        private String getCourseRankField() {
            return "1".equals(_rankDiv) ? "COURSE_RANK" : "2".equals(_rankDiv) ? "COURSE_AVG_RANK" : "COURSE_DEVIATION_RANK";
        }

        private String getClassRankField() {
            return "1".equals(_rankDiv) ? "CLASS_RANK" : "2".equals(_rankDiv) ? "CLASS_AVG_RANK" : "CLASS_DEVIATION_RANK";
        }

        private String getRankName2(final String div) {
            if (isAssessLevelMst() && "RANK".equals(div)) {
                if ("1".equals(_groupDiv)) {
                    return null;
                } else if ("2".equals(_groupDiv)) {
                    return null;
                } else if ("3".equals(_groupDiv)) {
                    return "コース";
                }
            } else {
                if ("1".equals(_groupDiv)) {
                    return "学級";
                } else if ("2".equals(_groupDiv)) {
                    return "学級";
                } else if ("3".equals(_groupDiv)) {
                    return "コース";
                }
            }
            return null;
        }

        private String getRankName1() {
            if ("1".equals(_groupDiv)) {
                return "学年";
            } else if ("2".equals(_groupDiv)) {
                return "コース";
            } else if ("3".equals(_groupDiv)) {
                return "学年";
            }
            return null;
        }

        private String changePrintYear() {
            if (_isSeireki) {
                return _param._year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
            }
        }

        /** RECORD_SLUMP_DATを参照するテストコード */
        public String getRecordSlumpTestcd() {
            if (isKyoto()) {
                if ( "1".equals(_semester) && "9901".equals(_kindItem)) return "0201";
                if ( "2".equals(_semester) && "9901".equals(_kindItem)) return "0102";
                if (!"9".equals(_semester) && "9900".equals(_kindItem)) return "0201";
            }
            return _kindItem;
        }
        
        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return "1".equals(_checkKettenDiv) && !"9".equals(_semester);
        }

        /** 欠点対象：満点マスタ(PERFECT_RECORD_DAT)の合格点(PASS_SCORE)を参照して判断するか */
        private boolean isPerfectRecord() {
            return "2".equals(_checkKettenDiv);
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        private boolean isKetten() {
            return !isRecordSlump() && !isPerfectRecord();
        }
        
        private boolean isKyoto() {
            return "kyoto".equals(_z010);
        }
        
        public boolean isAssessLevelMst() {
            return "1".equals(_outputAssessLevel);
        }
        
        private boolean isVdat() {
            return "9".equals(_semester) && "9900".equals(_kindItem);
        }
    }
}
 // KNJD192

// eof
