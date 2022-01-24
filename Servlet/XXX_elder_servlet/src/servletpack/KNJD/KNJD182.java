// kanji=漢字
/*
 * $Id: 72580d9a478592493eaecbb7eda82af38cf425c9 $
 *
 * 作成日: 2008/05/07 13:50:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfo;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 通知表(中京)。
 * @author m-yama
 * @version $Id: 72580d9a478592493eaecbb7eda82af38cf425c9 $
 */
public class KNJD182 {

    /**
     * 学年末
     */
    private static final String SEMEALL = "9";
    private static final String _9900 = "9900";

    private static final String SUBCLASSALL3 = "333333";
    private static final String SUBCLASSALL5 = "555555";
    private static final String SUBCLASSALL = "999999";

    private static final String SCORE = "SCORE";
    private static final String AVG = "AVG";
    private static final String VALUE = "VALUE";
    private static final String DEVIATION = "DEVIATION";
    private static final String RANK = "RANK";
    private static final String CLASSRANK = "CLASSRANK";
    private static final String CNT = "CNT";
    private static final String CLASSCNT = "CLASSCNT";
    private static final String SEQ = "SEQ";

    private static final Log log = LogFactory.getLog(KNJD182.class);

    Param _param;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final String SUBJECT_D = "01";  //教科コード
    private static final String SUBJECT_U = "89";  //教科コード
    private static final String SUBJECT_T = "90";  //総合的な学習の時間
    private static final String SSEMESTER = "1";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        try {

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            printMain(response, db2);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            db2.commit();
            db2.close();
        }

    }// doGetの括り

    private void printMain(final HttpServletResponse response, final DB2UDB db2) throws Exception {
        final KNJD182Form form = new KNJD182Form(response, _param);
        try {
            boolean hasData = false;
            final List studentList = getStudentInfo(db2);
            for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                form.setHead(_param.getFormName()); 
                form.printStudent(student);
                form.printHreport("SPECIALACTREMARK", student._specialactremark, 20, 10);
                form.printHreport("COMMUNICATION", student._communication, 20, 10);
                if (_param.isGakunenMatu()) printGetCredit(db2, form, student, "", "GET_CREDIT");
                printAttendInfo(db2, form, student);
                printScoreInfo(db2, form, student);
                hasData = true;
            }
            if (!hasData) {
                form.noneData();
            }
        } finally {
            form._svf.VrQuit();
        }
    }

    private void printGetCredit(
            final DB2UDB db2,
            final KNJD182Form form,
            final Student student,
            final String yearDiv,
            final String field
    ) throws SQLException {
        final String sql = getReportCardInfo.getGetCreditSql(_param._year,
                                                             SEMEALL,
                                                             student._schregno,
                                                             SUBJECT_D,
                                                             SUBJECT_T,
                                                             true,
                                                             _param._d026,
                                                             yearDiv,
                                                             _param._useCurriculumcd,
                                                             _param._useClassDetailDat);
        db2.query(sql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            while (rs.next()) {
                final String getCredit = rs.getString("GET_CREDIT");
                form.printGetCredit(field, getCredit);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private void printAttendInfo(final DB2UDB db2, final KNJD182Form form, final Student student) throws ParseException, SQLException {
        log.debug("getAttendSql = "+getAttendSql(student));
        db2.query(getAttendSql(student));
        ResultSet rs = null;
        try {
            int line = 1;
            int tLesson = 0;
            int tMourning = 0;
            int tPresent = 0;
            int tAttend = 0;
            int tAbsence = 0;
            int tLate = 0;
            int tEarly = 0;
            rs = db2.getResultSet();
            while (rs.next()) {
                if (SEMEALL.equals(rs.getString("SEMESTER"))) continue;
                final int mourning = rs.getInt("SUSPEND") + rs.getInt("MOURNING") + rs.getInt("VIRUS")  + rs.getInt("KOUDOME");
                final int present = rs.getInt("LESSON") - mourning;
                final int attend = present - rs.getInt("SICK");
                form.printAttend(line,
                                 (String) _param._semesterMap.get(rs.getString("SEMESTER")),
                                 rs.getString("LESSON"),
                                 String.valueOf(mourning),
                                 String.valueOf(present),
                                 String.valueOf(attend),
                                 rs.getString("SICK"),
                                 rs.getString("LATE"),
                                 rs.getString("EARLY")
                        );
                tLesson = tLesson + rs.getInt("LESSON");
                tMourning = tMourning + mourning;
                tPresent = tPresent + present;
                tAttend = tAttend + attend;
                tAbsence = tAbsence + rs.getInt("SICK");
                tLate = tLate + rs.getInt("LATE");
                tEarly = tEarly + rs.getInt("EARLY");
                line++;
            }
            if (_param.isGakunenMatu()) {
                form.printAttend(line,
                        "学年",
                        String.valueOf(tLesson),
                        String.valueOf(tMourning),
                        String.valueOf(tPresent),
                        String.valueOf(tAttend),
                        String.valueOf(tAbsence),
                        String.valueOf(tLate),
                        String.valueOf(tEarly)
               );
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private String getAttendSql(final Student student) throws ParseException {
        _param._attendParamMap.put("schregno", student._schregno);
        final String rtnSql = AttendAccumulate.getAttendSemesSql(
                                                _param._year,
                                                _param._semester,
                                                _param._semesterSdate,
                                                _param._date,
                                                _param._attendParamMap);
        return rtnSql;
    }

    private void printScoreInfo(final DB2UDB db2, final KNJD182Form form, final Student student) throws SQLException, ParseException {
        final Map subclassMap = getSubclassMap(db2, student);
        final List testList = getTestItemList(db2, student);
        final Map scoreMap = getScoreMap(db2, subclassMap, student);
        form.printKanjiMockScore(scoreMap, db2, student);
        form.printScore(subclassMap, testList, scoreMap, db2);
        form._svf.VrEndPage();
    }

    /**
     * @return rtnMap : kye = 科目コード, value = Score
     */
    private Map getScoreMap(final DB2UDB db2, final Map subclassMap, final Student student) throws SQLException, ParseException {
        Map rtnMap = new HashMap();
        final String rankField = _param.getRankField(false);
        try {
            setRecordRankDat(db2, student, rtnMap, rankField);

            setRecordAverageDat(db2, student, rtnMap);

            setRecordAverageClassCnt(db2, student, rtnMap);

            setRecordScoreDat(db2, student, rtnMap);

            setRecordRankTotal(db2, student, rtnMap);

            setAttendSubclass(db2, student, rtnMap);
            
            setKanjiPassScoreALL9(db2, student);
            
            setKanjiMockTestScore(db2, student, rtnMap);
        } finally {
            db2.commit();
        }
        return rtnMap;
    }
    
    private String getSubclasscd(final ResultSet rs) throws SQLException {
        final String subclassCd = rs.getString("SUBCLASSCD");
        if (SUBCLASSALL.equals(subclassCd) || SUBCLASSALL3.equals(subclassCd) || SUBCLASSALL5.equals(subclassCd)) {
            return subclassCd;
        }
        if ("1".equals(_param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclassCd;
        }
        return subclassCd;
    }

    private void setRecordRankDat(final DB2UDB db2, final Student student, Map rtnMap, final String rankField) throws SQLException {
        ResultSet rsRank = null;
        try {
            final String rankSql = getReportCardInfo.getRecordRankSql(_param._year, SSEMESTER, _param._semester, student._schregno, student._grade, null, null);
            log.debug(rankSql);
            db2.query(rankSql);
            rsRank = db2.getResultSet();
            while (rsRank.next()) {
                final String testKindItem = rsRank.getString("TESTKINDCD") + rsRank.getString("TESTITEMCD");
                final String semester = rsRank.getString("SEMESTER");
                if (SEMEALL.equals(semester) && _9900.equals(testKindItem)) {
                    continue;
                }
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsRank))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsRank));
                } else {
                    score = new Score(semester);
                    rtnMap.put(getSubclasscd(rsRank), score);
                }

                score.setScore(semester, testKindItem, rsRank.getString("SCORE"), SCORE);
                score.setScore(semester, testKindItem, rsRank.getString(rankField + "DEVIATION"), DEVIATION);
                score.setScore(semester, testKindItem, rsRank.getString(_param.getRankField(false) + "RANK"), RANK);
                score.setScore(semester, testKindItem, rsRank.getString("SEQ"), SEQ);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsRank);
        }

        // 学年成績
        try {
            final String rankSql = getReportCardInfo.getRecordRankVSql(_param._year, SEMEALL, SEMEALL, student._schregno, student._grade, null, null);
            log.debug(rankSql);
            db2.query(rankSql);
            rsRank = db2.getResultSet();
            while (rsRank.next()) {
                final String testKindItem = rsRank.getString("TESTKINDCD") + rsRank.getString("TESTITEMCD");
                if (!_9900.equals(testKindItem)) {
                    continue;
                }
                final String semester = rsRank.getString("SEMESTER");
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsRank))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsRank));
                } else {
                    score = new Score(semester);
                    rtnMap.put(getSubclasscd(rsRank), score);
                }

                score.setScore(semester, testKindItem, rsRank.getString("SCORE"), SCORE);
                score.setScore(semester, testKindItem, rsRank.getString(rankField + "DEVIATION"), DEVIATION);
                score.setScore(semester, testKindItem, rsRank.getString(_param.getRankField(false) + "RANK"), RANK);
                score.setScore(semester, testKindItem, rsRank.getString("SEQ"), SEQ);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsRank);
        }
    }

    private void setRecordAverageDat(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rsAvg = null;
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String avgSql = getReportCardInfo.getRecordAverageSql(_param._year, SSEMESTER, _param._semester, student._schregno, null, null, _param._groupDiv, student._grade,
                    student._hrClass, course);
            log.debug(avgSql);
            db2.query(avgSql);
            rsAvg = db2.getResultSet();
            while (rsAvg.next()) {
                final String testKindItem = rsAvg.getString("TESTKINDCD") + rsAvg.getString("TESTITEMCD");
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsAvg))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsAvg));
                } else {
                    score = new Score(rsAvg.getString("SEMESTER"));
                    rtnMap.put(getSubclasscd(rsAvg), score);
                }

                if (SEMEALL.equals(rsAvg.getString("SEMESTER")) && _9900.equals(testKindItem)) {
                    if (getSubclasscd(rsAvg).equals(SUBCLASSALL)) {
                        score._gradCnt = rsAvg.getString("COUNT");
                    }
                } else {
                    final float avg = rsAvg.getFloat("AVG") * 10 / 10;
                    if (getSubclasscd(rsAvg).equals(SUBCLASSALL)) {
                        score.setScore(rsAvg.getString("SEMESTER"), testKindItem, rsAvg.getString("COUNT"), CNT);
                    } else {
                        score.setScore(rsAvg.getString("SEMESTER"), testKindItem, String.valueOf(avg), AVG);
                    }
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAvg);
        }
        // 学年成績の平均点データ
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String avgSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, SEMEALL, student._schregno, null, null, _param._groupDiv, student._grade,
                    student._hrClass, course, _9900);
            log.debug(avgSql);
            db2.query(avgSql);
            rsAvg = db2.getResultSet();
            while (rsAvg.next()) {
                final String testKindItem = rsAvg.getString("TESTKINDCD") + rsAvg.getString("TESTITEMCD");
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsAvg))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsAvg));
                } else {
                    score = new Score(rsAvg.getString("SEMESTER"));
                    rtnMap.put(getSubclasscd(rsAvg), score);
                }

                final float avg = rsAvg.getFloat("AVG") * 10 / 10;
                if (getSubclasscd(rsAvg).equals(SUBCLASSALL)) {
                    score.setScore(rsAvg.getString("SEMESTER"), testKindItem, rsAvg.getString("COUNT"), CNT);
                } else {
                    score.setScore(rsAvg.getString("SEMESTER"), testKindItem, String.valueOf(avg), AVG);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAvg);
        }
    }

    private void setRecordAverageClassCnt(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rsAvg = null;
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String avgSql = getReportCardInfo.getRecordAverageSql(_param._year, SSEMESTER, _param._semester, student._schregno, null, null, "2", student._grade,
                    student._hrClass, course);
            log.debug(avgSql);
            db2.query(avgSql);
            rsAvg = db2.getResultSet();
            while (rsAvg.next()) {
                if (getSubclasscd(rsAvg).equals(SUBCLASSALL)) {
                    final String testKindItem = rsAvg.getString("TESTKINDCD") + rsAvg.getString("TESTITEMCD");
                    final Score score;
                    if (rtnMap.containsKey(getSubclasscd(rsAvg))) {
                        score = (Score) rtnMap.get(getSubclasscd(rsAvg));
                    } else {
                        score = new Score(rsAvg.getString("SEMESTER"));
                        rtnMap.put(getSubclasscd(rsAvg), score);
                    }
                    
                    if (SEMEALL.equals(rsAvg.getString("SEMESTER")) && _9900.equals(testKindItem)) {
                        score._gradClassCnt = rsAvg.getString("COUNT");
                    } else {
                        score.setScore(rsAvg.getString("SEMESTER"), testKindItem, rsAvg.getString("COUNT"), CLASSCNT);
                    }
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAvg);
        }
        // 学年成績の平均点データ
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String avgSql = getReportCardInfo.getRecordAverageVTestAppointSql(_param._year, SEMEALL, student._schregno, null, null, "2", student._grade,
                    student._hrClass, course, _9900);
            log.debug(avgSql);
            db2.query(avgSql);
            rsAvg = db2.getResultSet();
            while (rsAvg.next()) {
                if (getSubclasscd(rsAvg).equals(SUBCLASSALL)) {
                    final String testKindItem = rsAvg.getString("TESTKINDCD") + rsAvg.getString("TESTITEMCD");
                    final Score score;
                    if (rtnMap.containsKey(getSubclasscd(rsAvg))) {
                        score = (Score) rtnMap.get(getSubclasscd(rsAvg));
                    } else {
                        score = new Score(rsAvg.getString("SEMESTER"));
                        rtnMap.put(getSubclasscd(rsAvg), score);
                    }
                    
                    score.setScore(rsAvg.getString("SEMESTER"), testKindItem, rsAvg.getString("COUNT"), CLASSCNT);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAvg);
        }
    }

    private void setRecordScoreDat(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rsScore = null;
        // 学年評定
        try {
            final String rankSql = getReportCardInfo.getRecordRankTestAppointSql(_param._year, SEMEALL, student._schregno, null, null, _9900);
            log.debug(rankSql);
            db2.query(rankSql);
            rsScore = db2.getResultSet();
            while (rsScore.next()) {
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsScore))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsScore));
                } else {
                    score = new Score(rsScore.getString("SEMESTER"));
                    rtnMap.put(getSubclasscd(rsScore), score);
                }

                score._value = rsScore.getString("SCORE");
                if (SUBCLASSALL.equals(getSubclasscd(rsScore))) {
                    final float avg = rsScore.getFloat("AVG") * 10 / 10;
                    score._gradAvg = String.valueOf(avg);
                    score._gradRank = rsScore.getString(_param.getRankField(true) + "RANK");
                    score._gradClassRank = rsScore.getString("CLASS_AVG_RANK");
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsScore);
        }
        
        // 履修単位数・修得単位数
        try {
            final String scoreSql = getReportCardInfo.getRecordScoreSql(_param._year, SEMEALL, student._schregno, null, null, "00", _param._useCurriculumcd
                    );
            log.debug(scoreSql);
            db2.query(scoreSql);
            rsScore = db2.getResultSet();
            while (rsScore.next()) {
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsScore))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsScore));
                } else {
                    score = new Score(rsScore.getString("SEMESTER"));
                    rtnMap.put(getSubclasscd(rsScore), score);
                }

                score._compCredit = rsScore.getString("COMP_CREDIT");
                score._getCredit = rsScore.getString("GET_CREDIT");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsScore);
        }
    }

    private void setRecordRankTotal(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rsRankTotal = null;
        try {
            final String rankTotalSql = getReportCardInfo.getRecordRankTotalSql(_param._year, SSEMESTER, _param._semester, student._schregno);
            log.debug(rankTotalSql);
            db2.query(rankTotalSql);
            rsRankTotal = db2.getResultSet();
            while (rsRankTotal.next()) {
                final String testKindItem = rsRankTotal.getString("TESTKINDCD") + rsRankTotal.getString("TESTITEMCD");
                final String semester = rsRankTotal.getString("SEMESTER");
                if (SEMEALL.equals(semester) && _9900.equals(testKindItem)) {
                    continue;
                }
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsRankTotal))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsRankTotal));
                } else {
                    score = new Score(semester);
                    rtnMap.put(getSubclasscd(rsRankTotal), score);
                }

                score.setScore(semester, testKindItem, rsRankTotal.getString("SCORE"), SCORE);
                final float avg = rsRankTotal.getFloat("AVG") * 10 / 10;
                score.setScore(semester, testKindItem, String.valueOf(avg), AVG);
                score.setScore(semester, testKindItem, rsRankTotal.getString(_param.getRankField(true) + "RANK"), RANK);
                score.setScore(semester, testKindItem, rsRankTotal.getString("CLASS_AVG_RANK"), CLASSRANK);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsRankTotal);
        }

        // 学年成績
        try {
            final String rankTotalSql = getReportCardInfo.getRecordRankVTotalSql(_param._year, SEMEALL, SEMEALL, student._schregno);
            log.debug(rankTotalSql);
            db2.query(rankTotalSql);
            rsRankTotal = db2.getResultSet();
            while (rsRankTotal.next()) {
                final String testKindItem = rsRankTotal.getString("TESTKINDCD") + rsRankTotal.getString("TESTITEMCD");
                final String semester = rsRankTotal.getString("SEMESTER");
                if (!(SEMEALL.equals(semester) && _9900.equals(testKindItem))) {
                    continue;
                }
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsRankTotal))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsRankTotal));
                } else {
                    score = new Score(semester);
                    rtnMap.put(getSubclasscd(rsRankTotal), score);
                }

                score.setScore(semester, testKindItem, rsRankTotal.getString("SCORE"), SCORE);
                final float avg = rsRankTotal.getFloat("AVG") * 10 / 10;
                score.setScore(semester, testKindItem, String.valueOf(avg), AVG);
                score.setScore(semester, testKindItem, rsRankTotal.getString(_param.getRankField(true) + "RANK"), RANK);
                score.setScore(semester, testKindItem, rsRankTotal.getString("CLASS_AVG_RANK"), CLASSRANK);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsRankTotal);
        }
    }

    private void setAttendSubclass(
            final DB2UDB db2,
            final Student student,
            Map rtnMap
    ) throws SQLException, ParseException {
        ResultSet rsAttend = null;
        try {
            _param._attendParamMap.put("schregno", student._schregno);
            final String attendSql = AttendAccumulate.getAttendSubclassAbsenceSql(
                                                    _param._year,
                                                    _param._semester,
                                                    _param._semesterSdate,
                                                    _param._date,
                                                    _param._attendParamMap);
            log.debug(attendSql);
            db2.query(attendSql);
            rsAttend = db2.getResultSet();
            while (rsAttend.next()) {
                Score score = new Score(rsAttend.getString("SEMESTER"));
                final String subclasscd = rsAttend.getString("SUBCLASSCD");
                if (rtnMap.containsKey(subclasscd)) {
                    score = (Score) rtnMap.get(subclasscd);
                }

                score._attend = rsAttend.getString("ABSENT_SEM");

                if (!rtnMap.containsKey(subclasscd)) {
                    rtnMap.put(subclasscd, score);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAttend);
        }
    }
    

    private void setKanjiPassScoreALL9(final DB2UDB db2, final Student student) throws SQLException, ParseException {
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String sqlPassScoreALL9 = sqlPassScoreALL9(student._grade, course);
            log.debug(" sqlPassScoreALL9 =" + sqlPassScoreALL9);
            ps2 = db2.prepareStatement(sqlPassScoreALL9);
            rs2 = ps2.executeQuery();
            while (rs2.next()) {
                student._passScoreALL9 = (Double) rs2.getObject("PASS_SCORE");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps2, rs2);
            db2.commit();
        }
    }

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

    private void setKanjiMockTestScore(
            final DB2UDB db2,
            final Student student,
            Map rtnMap
    ) throws SQLException, ParseException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.MOCKCD, ");
        stb.append("     T3.MOCK_SUBCLASS_CD, ");
        stb.append("     T3.SCORE, ");
        stb.append("     T4.PERFECT, ");
        stb.append("     T4.PASS_SCORE, ");
        stb.append("     CASE WHEN T3.SCORE IS NULL OR T4.PASS_SCORE IS NULL THEN NULL ");
        stb.append("          WHEN T4.PASS_SCORE <= T3.SCORE THEN 1 ELSE 0 END AS IS_PASSED ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_MST T2 ON T2.MOCKCD LIKE '4%' ");
        stb.append("     LEFT JOIN MOCK_DAT T3 ON T3.MOCKCD = T2.MOCKCD ");
        stb.append("         AND T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN MOCK_PERFECT_COURSE_DAT T4 ON T1.YEAR = T4.YEAR ");
        stb.append("         AND T4.COURSE_DIV = '0' ");
        stb.append("         AND T4.MOCKCD = T2.MOCKCD ");
        stb.append("         AND T4.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD ");
        stb.append("         AND T4.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T1.GRADE END  ");
        stb.append("         AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = ");
        stb.append("           CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T2.MOCKCD ");

        PreparedStatement psMock = null;
        ResultSet rsMock = null;
        KanjiMockScore kanjiMockScore = new KanjiMockScore(student._schregno); 
        rtnMap.put("KANJI_MOCK_SCORE", kanjiMockScore);
        try {
            psMock = db2.prepareStatement(stb.toString());
            rsMock = psMock.executeQuery();
            while (rsMock.next()) {
                final String mockCd = rsMock.getString("MOCKCD");
                final String score = rsMock.getString("SCORE");
                final String passScore = rsMock.getString("PASS_SCORE");
                final Boolean isPassed = rsMock.getString("IS_PASSED") == null ? null : new Boolean("1".equals(rsMock.getString("IS_PASSED")));
                
                kanjiMockScore.add(mockCd, score, passScore, isPassed);
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psMock, rsMock);
        }
    }

    class KanjiMockTest {
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
    
    class KanjiMockScore {
        final String _schregno;
        final Map _scoreMap = new TreeMap();
        final Map _passScoreMap = new TreeMap();
        final Map _isPassedMap = new TreeMap();
        public KanjiMockScore(String schregno) {
            _schregno = schregno;
        }
        
        public void add(String mockCd, String score, String passScore, Boolean isPassed) {
            if (null != mockCd) {
                _scoreMap.put(mockCd, score);
                _passScoreMap.put(mockCd, passScore);
                _isPassedMap.put(mockCd, isPassed);
            }
        }
    }

    /**
     * _rankMap : key = 学期, value = Map : key = テストコード, value = TestScore
     */
    class Score {
        /** key = テストコード, value = TestScore */
        Map _rankMap;
        /** 評定 */
        String _value;
        /** 欠課時数 */
        String _attend;
        /** 単位 */
        String _compCredit;
        String _getCredit;
        /** 平均・席次・クラス席次 */
        String _gradAvg;
        String _gradRank;
        String _gradClassRank;
        String _gradCnt;
        String _gradClassCnt;

        public Score(final String semester) {
            if (null == _rankMap) {
                _rankMap = new HashMap();
                _rankMap.put(semester, null);
            }
        }

        private void setScore(
                final String key,
                final String kindItem,
                final String score,
                final String scoreDiv
        ) {
            if (null == _rankMap.get(key)) {
                _rankMap.put(key, new HashMap());
            }

            final Map setMap = (Map) _rankMap.get(key);
            TestScore testScore = null;

            if (setMap.containsKey(kindItem)) {
                testScore = (TestScore) setMap.get(kindItem);
                testScore.setScore(score, scoreDiv);
            } else {
                testScore = new TestScore(score, scoreDiv);
                ((Map) _rankMap.get(key)).put(kindItem, testScore);
            }
        }
    }

    class TestScore {
        String _score;
        String _avg;
        String _value;
        String _deviation;
        String _rank;
        String _classRank;
        String _cnt;
        String _classCnt;
        String _seq;
        BigDecimal _bdAvg;

        public TestScore(String score, String scoreDiv) {
            setScore(score, scoreDiv);
        }

        private void setScore(final String score, final String scoreDiv) {
            if (scoreDiv.equals(SCORE)) {
                _score = score;
            } else if (scoreDiv.equals(AVG)) {
                _avg = score;
                _bdAvg = new BigDecimal(_avg);
            } else if (scoreDiv.equals(VALUE)) {
                _value = score;
            } else if (scoreDiv.equals(DEVIATION)) {
                _deviation = score;
            } else if (scoreDiv.equals(RANK)) {
                _rank = score;
            } else if (scoreDiv.equals(CLASSRANK)) {
                _classRank = score;
            } else if (scoreDiv.equals(CNT)) {
                _cnt = score;
            } else if (scoreDiv.equals(CLASSCNT)) {
                _classCnt = score;
            } else if (scoreDiv.equals(SEQ)) {
                _seq = score;
            }
        }

        public String toString() {
            return "得点：" + _score
                    + " 平均：" + _avg
                    + " 評価：" + _value
                    + " 偏差：" + _deviation;
        }
    }

    private Map getSubclassMap(final DB2UDB db2, final Student student) throws SQLException {
        final String subclassSql = getSubclassSql(student);
        log.debug(subclassSql);
        db2.query(subclassSql);
        ResultSet rsSubclass = null;
        final Map rtnMap = new TreeMap();
        try {
            rsSubclass = db2.getResultSet();
            while (rsSubclass.next()) {
                Subclass subclass = new Subclass(rsSubclass.getString("CLASSCD"),
                                                 rsSubclass.getString("CLASSNAME"),
                                                 getSubclasscd(rsSubclass),
                                                 rsSubclass.getString("SUBCLASSNAME")
                                                 );
                final String keySubclasscd;
                if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                    keySubclasscd = rsSubclass.getString("CLASSCD") + "-" + rsSubclass.getString("SCHOOL_KIND") + "-" + rsSubclass.getString("CURRICULUM_CD") + "-" + rsSubclass.getString("SUBCLASSCD");
                } else {
                    keySubclasscd = rsSubclass.getString("SUBCLASSCD");
                }
                if (!ArrayUtils.contains(_param._d026, keySubclasscd)) {
                    rtnMap.put(getSubclasscd(rsSubclass), subclass);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsSubclass);
        }
        return rtnMap;
    }

    class Subclass {
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        public Subclass(
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName
        ) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }
    }

    private String getSubclassSql(final Student student) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH REC_RANK AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER BETWEEN '" + SSEMESTER + "' AND '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND (T1.CLASSCD BETWEEN '" + SUBJECT_D + "' AND '" + SUBJECT_T + "') ");
        } else {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + SUBJECT_D + "' AND '" + SUBJECT_T + "') ");
        }
        stb.append("     AND T1.SUBCLASSCD not in ('" + SUBCLASSALL3 + "','" + SUBCLASSALL5 + "') ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + SEMEALL  + "' ");
        stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND (T1.CLASSCD BETWEEN '" + SUBJECT_D + "' AND '" + SUBJECT_T + "') ");
        } else {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + SUBJECT_D + "' AND '" + SUBJECT_T + "') ");
        }
        stb.append("     AND T1.SUBCLASSCD not in ('" + SUBCLASSALL3 + "','" + SUBCLASSALL5 + "') ");
        stb.append("     AND T1.VALUE IS NOT NULL ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" ), SUBCLASS_T AS ( ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        } else {
            stb.append("     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
        }
        stb.append("     T1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     REC_RANK T1 ");
        if (_param._isNoPrintMoto) {
            stb.append(" WHERE ");
            stb.append("     NOT EXISTS(SELECT ");
            stb.append("                     'X' ");
            stb.append("                 FROM ");
            stb.append("                     SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                 WHERE ");
            stb.append("                     T2.YEAR = '" + _param._year + "' AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T2.ATTEND_CLASSCD = T1.CLASSCD AND ");
                stb.append("     T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                stb.append("     T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append("                     T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("                 ) ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN L1.SHOWORDER3 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER3 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS SUBCLASSORDER3, ");
        stb.append("     CASE WHEN L1.SUBCLASSORDERNAME2 IS NOT NULL ");
        stb.append("          THEN L1.SUBCLASSORDERNAME2 ");
        stb.append("          ELSE L1.SUBCLASSNAME ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     CASE WHEN L2.SHOWORDER3 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER3 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER3, ");
        stb.append("     CASE WHEN L2.CLASSORDERNAME2 IS NOT NULL ");
        stb.append("          THEN L2.CLASSORDERNAME2 ");
        stb.append("          ELSE L2.CLASSNAME ");
        stb.append("     END AS CLASSNAME ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_T T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN CLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        }
            stb.append(" GROUP BY ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     CASE WHEN L1.SHOWORDER3 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER3 ");
            stb.append("          ELSE 999 ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L1.SUBCLASSORDERNAME2 IS NOT NULL ");
            stb.append("          THEN L1.SUBCLASSORDERNAME2 ");
            stb.append("          ELSE L1.SUBCLASSNAME ");
            stb.append("     END, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     CASE WHEN L2.SHOWORDER3 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER3 ");
            stb.append("          ELSE 999 ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L2.CLASSORDERNAME2 IS NOT NULL ");
            stb.append("          THEN L2.CLASSORDERNAME2 ");
            stb.append("          ELSE L2.CLASSNAME ");
            stb.append("     END ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND ");
                stb.append("       ,T1.CURRICULUM_CD ");
            }
            stb.append(" ORDER BY ");
            stb.append("     CASE WHEN L2.SHOWORDER3 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER3 ");
            stb.append("          ELSE 999 ");
            stb.append("     END, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     CASE WHEN L1.SHOWORDER3 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER3 ");
            stb.append("          ELSE 999 ");
            stb.append("     END, ");
            stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    private List getTestItemList(final DB2UDB db2, final Student student) throws SQLException {
        final String testItemSql = getTestItemSql(student);
        log.debug(testItemSql);
        db2.query(testItemSql);
        ResultSet rsTestItem = null;
        final List rtnList = new ArrayList();
        try {
            rsTestItem = db2.getResultSet();
            while (rsTestItem.next()) {
                TestItem testItem = new TestItem(rsTestItem.getString("TEST_DIV"),
                                                 rsTestItem.getString("SEMESTER"),
                                                 rsTestItem.getString("TESTKINDCD"),
                                                 rsTestItem.getString("TESTITEMCD"),
                                                 rsTestItem.getString("TESTITEMNAME"),
                                                 rsTestItem.getString("MOCKCD"),
                                                 rsTestItem.getString("COUNTFLG")
                                                 );
                rtnList.add(testItem);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsTestItem);
        }
        return rtnList;
    }

    private String getTestItemSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     '9' AS TEST_DIV, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.TESTKINDCD, ");
        stb.append("     T1.TESTITEMCD, ");
        stb.append("     T1.TESTITEMNAME, ");
        stb.append("     '' AS MOCKCD, ");
        stb.append("     999 AS SEQ, ");
        stb.append("     T1.COUNTFLG ");
        stb.append(" FROM ");
        stb.append("     TESTITEM_MST_COUNTFLG_NEW T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER BETWEEN '" + SSEMESTER + "' AND '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SEQ, ");
        stb.append("     SEMESTER, ");
        stb.append("     TESTKINDCD, ");
        stb.append("     TESTITEMCD ");

        return stb.toString();
    }

    class TestItem {
        final String _testDiv;
        final String _semester;
        final String _testKindCd;
        final String _testItemCd;
        final String _testItemName;
        final String _mockCd;
        final String _countFlg;
        public TestItem(
                final String testDiv,
                final String semester,
                final String testKindCd,
                final String testItemCd,
                final String testItemName,
                final String mockCd,
                final String countFlg
        ) {
            _testDiv = testDiv;
            _semester = semester;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _testItemName = testItemName;
            _mockCd = mockCd;
            _countFlg = countFlg;
        }
    }

    private List getStudentInfo(final DB2UDB db2) throws SQLException  {
        final List rtnStudent = new ArrayList();
        final String sql = getStudentInfoSql(_param.getAddrField());
        ResultSet rs = null;
        try {
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                Student student = new Student(rs.getString("SCHREGNO"),
                                              rs.getString("GRADE"),
                                              rs.getString("HR_CLASS"),
                                              rs.getString("ATTENDNO"),
                                              rs.getString("HR_NAME"),
                                              rs.getString("HR_NAMEABBV"),
                                              rs.getString("NAME"),
                                              rs.getString("COURSECD"),
                                              rs.getString("COURSENAME"),
                                              rs.getString("MAJORCD"),
                                              rs.getString("MAJORNAME"),
                                              rs.getString("COURSECODE"),
                                              rs.getString("COURSECODENAME"),
                                              rs.getString("GNAME"),
                                              rs.getString("GZIP"),
                                              rs.getString("GADDR1"),
                                              rs.getString("GADDR2"),
                                              rs.getString("SPECIALACTREMARK"),
                                              rs.getString("COMMUNICATION"),
                                              rs.getString("TOTALSTUDYTIME"),
                                              rs.getString("STAFFNAME"));
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
        final String _gName;
        final String _gZip;
        final String _gAddr1;
        final String _gAddr2;
        final String _specialactremark;
        final String _communication;
        final String _totalstudytime;
        final String _hrStaffName;
        Double _passScoreALL9 = null;
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
                final String courseCodeName,
                final String gName,
                final String gZip,
                final String gAddr1,
                final String gAddr2,
                final String specialactremark,
                final String communication,
                final String totalstudytime,
                final String hrStaffName
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
            _gName = gName;
            _gZip = gZip;
            _gAddr1 = gAddr1;
            _gAddr2 = gAddr2;
            _specialactremark = specialactremark;
            _communication = communication;
            _totalstudytime = totalstudytime;
            _hrStaffName = hrStaffName;
        }
        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }
    private String getStudentInfoSql(final String fieldName) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VSCH.SCHREGNO, ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO, ");
        stb.append("     VSCH.HR_NAME, ");
        stb.append("     VSCH.HR_NAMEABBV, ");
        stb.append("     VSCH.NAME, ");
        stb.append("     VSCH.COURSECD, ");
        stb.append("     L1.COURSENAME, ");
        stb.append("     VSCH.MAJORCD, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     VSCH.COURSECODE, ");
        stb.append("     L2.COURSECODENAME, ");
        stb.append("     L3." + fieldName + "_NAME AS GNAME, ");
        stb.append("     L3." + fieldName + "_ZIPCD AS GZIP, ");
        stb.append("     L3." + fieldName + "_ADDR1 AS GADDR1, ");
        stb.append("     L3." + fieldName + "_ADDR2 AS GADDR2, ");
        stb.append("     L4.SPECIALACTREMARK, ");
        stb.append("     L4.COMMUNICATION, ");
        stb.append("     L4.TOTALSTUDYTIME, ");
        stb.append("     (SELECT W1.STAFFNAME FROM STAFF_MST W1 WHERE W1.STAFFCD=L5.TR_CD1) AS STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO VSCH ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
        stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
        stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
        stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L4 ON VSCH.YEAR = L4.YEAR ");
        stb.append("          AND L4.SEMESTER = '9' ");
        stb.append("          AND VSCH.SCHREGNO = L4.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L5 ON VSCH.YEAR = L5.YEAR ");
        stb.append("          AND VSCH.SEMESTER = L5.SEMESTER ");
        stb.append("          AND VSCH.GRADE = L5.GRADE ");
        stb.append("          AND VSCH.HR_CLASS = L5.HR_CLASS ");
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     VSCH.YEAR = '" + _param._year + "' ");
        stb.append("     AND VSCH.SEMESTER = '" + _param._ctrlSeme + "' ");
        stb.append("     AND VSCH.SCHREGNO IN " + _param._inState + " ");
        stb.append(" ORDER BY ");
        stb.append("     VSCH.ATTENDNO ");

        return stb.toString();
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

    /** パラメータクラス */
    class Param {
        final String _year;
        final String _semester;
        final String _paramSemester;
        final String _ctrlSeme;
        final String _date;
        final String _yearMonth;
        final String _hrClass;
        final String _addrPrint;
        final String _addrDiv;
        final String _groupDiv;
        final String _rankPrint;
        final String _formName;
        final String _inState;
        // final boolean _printKatei;
        final boolean _isRankPrintAll;
        final boolean _isPrintKekka;
        final Map _rankPrintMap = new TreeMap();
        final Map _rankPrintSemMap = new TreeMap();
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        String _d016 = "";
        final boolean _isNoPrintMoto;
        String[] _d026 = null;
        CertifSchool _certifSchool;
        final Map _semesterMap = new TreeMap();
        final String _semesterSdate;
        String _dankaiItem = ""; //評定段階区分の文言
        int _dankaiHigh1;
        final Map _kanjiMockTestMap = new TreeMap();
        final Map _attendParamMap;
        
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            ResultSet rsZ010 = null;
            ResultSet rsZ012 = null;
            ResultSet rsCertif = null;
            ResultSet rsSeme = null;

            try {
                _year = request.getParameter("YEAR");
                _ctrlSeme = request.getParameter("SEMESTER").equals(SEMEALL) ? request.getParameter("CTRL_SEME") : request.getParameter("SEMESTER");
                _date = request.getParameter("DATE");
                _yearMonth = _date.substring(0, 4) + _date.substring(5, 7);
                _hrClass = request.getParameter("GRADE_HR_CLASS");
                _addrPrint = request.getParameter("ADDR_PRINT");
                _addrDiv = request.getParameter("ADDR_DIV");
                _groupDiv = request.getParameter("GROUP_DIV");
                _formName = request.getParameter("FORMNAME");
                _rankPrint = request.getParameter("RANK_PRINT");
                // _printKatei = null == request.getParameter("PRINT_KATEI") ? false : true;
                _isRankPrintAll = null == request.getParameter("RANK_PRINT_ALL") ? true : false;
                _isPrintKekka = null == request.getParameter("KEKKA_PRINT") ? true : false;
                _useCurriculumcd = request.getParameter("useCurriculumcd");
                _useClassDetailDat = request.getParameter("useClassDetailDat");

                // 順位の印字制御
                for (int i = 1; i <= 8; i++) {
                    final String print = request.getParameter("RANK_PRINT" + i);
                    _rankPrintMap.put(String.valueOf(i), print);
                }
                final String schregNo[] = request.getParameterValues("category_selected");
                String sep = "";
                final StringBuffer stb = new StringBuffer();
                stb.append("(");
                for (int ia = 0; ia<schregNo.length; ia++) {
                    stb.append(sep + "'" + schregNo[ia] + "'");
                    sep = ",";
                }
                stb.append(")");
                _inState = stb.toString();

                _z010 = setNameMst(db2, "Z010", "00", "NAME1");
                _z012 = setNameMst(db2, "Z012", "01", "NAME1");
                _isSeireki = _z012.equals("2") ? true : false;
                _d016 = setNameMst(db2, "D016", null, "NAMESPARE1");
                _isNoPrintMoto = "Y".equals(_d016) ? true : false;

                // 証明書学校データ
                String sqlCertif = "SELECT * FROM CERTIF_SCHOOL_DAT " + "WHERE YEAR='" + _year + "' AND CERTIF_KINDCD = '104'";
                db2.query(sqlCertif);
                rsCertif = db2.getResultSet();
                while (rsCertif.next()) {
                    _certifSchool = new CertifSchool(rsCertif.getString("SYOSYO_NAME"),
                                                     rsCertif.getString("SYOSYO_NAME2"),
                                                     rsCertif.getString("SCHOOL_NAME"),
                                                     rsCertif.getString("JOB_NAME"),
                                                     rsCertif.getString("PRINCIPAL_NAME"),
                                                     rsCertif.getString("REMARK1"),
                                                     rsCertif.getString("REMARK2"),
                                                     rsCertif.getString("REMARK3"),
                                                     rsCertif.getString("REMARK4"),
                                                     rsCertif.getString("REMARK5"),
                                                     rsCertif.getString("REMARK6"),
                                                     rsCertif.getString("REMARK7"),
                                                     rsCertif.getString("REMARK8"),
                                                     rsCertif.getString("REMARK9"),
                                                     rsCertif.getString("REMARK10")
                                                    );
                }
                db2.commit();

                // 学期データ
                String sqlSem = "SELECT * FROM V_SEMESTER_GRADE_MST " + "WHERE YEAR='" + _year + "' AND GRADE = '" + _hrClass.substring(0, 2) + "' ORDER BY SEMESTER";
                db2.query(sqlSem);
                rsSeme = db2.getResultSet();
                String seme = "1";
                String semesterSdate = _year + "-04-01";
                while (rsSeme.next()) {
                    _semesterMap.put(rsSeme.getString("SEMESTER"), rsSeme.getString("SEMESTERNAME"));
                    seme = rsSeme.getString("SEMESTER").equals(SEMEALL) ? seme : rsSeme.getString("SEMESTER");
                    if (rsSeme.getString("SEMESTER").equals(SSEMESTER)) semesterSdate = rsSeme.getString("SDATE");

                    // 順位の印字制御
                    final String print = request.getParameter("RANK_PRINT_SEM" + rsSeme.getString("SEMESTER"));
                    _rankPrintSemMap.put(rsSeme.getString("SEMESTER"), print);
                }
                _semester = request.getParameter("SEMESTER").equals(seme) ? SEMEALL : request.getParameter("SEMESTER");
                _paramSemester = request.getParameter("SEMESTER");
                _semesterSdate = semesterSdate;
                _d026 = setNameMstV026(db2);

                _dankaiHigh1 = 0;//初期化
                _dankaiItem = setAssessMst(db2);//評定段階区分の文言
                setKajiMockTestMap(db2);
            } finally {
                DbUtils.closeQuietly(rsZ010);
                DbUtils.closeQuietly(rsZ012);
                DbUtils.closeQuietly(rsCertif);
                DbUtils.closeQuietly(rsSeme);
                db2.commit();
            }
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }

        private void setKajiMockTestMap(final DB2UDB db2) throws SQLException {
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

        private String setAssessMst(final DB2UDB db2) throws SQLException {
            String rtnSt = "";
            String seq = "";
            db2.query(getAssessMst());
            ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    String lev = rs.getString("LEVEL");
                    String hih = rs.getString("HIGH");
                    String low = rs.getString("LOW");
                    String tmpStr = lev + "・・・" + hih + "\uFF5E" + low;
                    rtnSt = rtnSt + seq + tmpStr;
                    seq = "　";
                    if ("1".equals(lev)) _dankaiHigh1 = Integer.parseInt(hih);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
                log.debug("欠点："+_dankaiHigh1+"点以下");
            }
            return rtnSt;
        }

        private String getAssessMst() {
            final String rtnSql = ""
                                + " SELECT "
                                + "     ASSESSLEVEL as LEVEL, "
                                + "     smallint(ASSESSLOW) as LOW, "
                                + "     smallint(ASSESSHIGH) as HIGH "
                                + " FROM "
                                + "     ASSESS_MST "
                                + " WHERE "
                                + "     ASSESSCD='3' "
                                + " ORDER BY "
                                + "     ASSESSLEVEL DESC ";
            return rtnSql;
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String field) throws SQLException {
            String rtnSt = "";
            db2.query(getNameMst(_year, namecd1, namecd2, null));
            ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnSt = rs.getString(field);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtnSt;
        }

        private String[] setNameMstV026(final DB2UDB db2) throws SQLException {
            final List v026List = new ArrayList();
            final String sql;
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_paramSemester) ? "4" : String.valueOf(Integer.parseInt(_paramSemester)));
                sql = " SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS NAME1 FROM SUBCLASS_DETAIL_DAT WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1' ";
            } else {
                sql = getNameMst(_year, "D026", null, "AND " + (SEMEALL.equals(_paramSemester) ? "NAMESPARE1" : ("ABBV" + _paramSemester)) + " = '1' ");
            }
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    final String s = rs.getString("NAME1");
                    if (null != s) {
                        v026List.add(s);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            final String[] v026 = new String[v026List.size()];
            for (int i = 0; i < v026.length; i++) {
                v026[i] = (String) v026List.get(i);
            }
            return v026;
        }
        
        private String getNameMst(final String year, final String namecd1, final String namecd2, final String where) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + (null != namecd2 ? ("     AND NAMECD2 = '" + namecd2 + "'") : "")
                                + (null != where ? where : "")
                                + " ORDER BY "
                                + "     NAMECD2 ";
            return rtnSql;
        }

        public String changePrintDate(final String date) {
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        public String changePrintYear() {
            if (_isSeireki) {
                return _param._year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
            }
        }

        private String getAddrField() {
            return _addrDiv.equals("1") ? "GUARD" : "GUARANTOR";
        }

        public String getFormName() {
            return "KNJD182.frm";
        }

        public String getRankField(boolean forRecordRankDat) {
            if (forRecordRankDat) {
                if (_groupDiv.equals("1")) {
                    return "GRADE_AVG_";
                } else if (_groupDiv.equals("2")) {
                    return "CLASS_AVG_";
                } else {
                    return "COURSE_AVG_";
                }
            } 
            if (_groupDiv.equals("1")) {
                return "GRADE_";
            } else if (_groupDiv.equals("2")) {
                return "CLASS_";
            } else {
                return "COURSE_";
            }
        }

        public String getItemName() {
            if (_groupDiv.equals("1")) {
                return "学年";
            } else if (_groupDiv.equals("2")) {
                return "クラス";
            } else {
                return "コース";
            }
        }

//        boolean isPatarnC() {
//            return _formName.equals("KNJD182C");
//        }

        boolean isGakunenMatu() {
            return _semester.equals(SEMEALL) ? true : false;
        }
    }

    class CertifSchool {
        final String _syosyoName;
        final String _syosyoName2;
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        final String _remark10;

        CertifSchool(
                final String syosyoName,
                final String syosyoName2,
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10
        ) {
            _syosyoName = syosyoName;
            _syosyoName2 = syosyoName2;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
            _remark8 = remark8;
            _remark9 = remark9;
            _remark10 = remark10;
        }
    }
}
 // KNJD182

// eof
