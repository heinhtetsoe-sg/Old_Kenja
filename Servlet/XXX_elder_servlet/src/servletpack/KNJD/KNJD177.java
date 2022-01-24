// kanji=漢字
/*
 *
 * 作成日: 2008/05/07 13:50:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfo;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 通知表(熊本)。
 * @author m-yama
 */
public class KNJD177 {

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
    private static final String PASSSCORE = "PASS_SCORE";
    private static final String ASSESSLEVEL = "ASSESSLEVEL";
    private static final String ASSESSHIGH1 = "ASSESSHIGH1";

    private static final Log log = LogFactory.getLog(KNJD177.class);

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

        } finally {
            if (null != _param) {
                DbUtils.closeQuietly(_param._psAttendSemes);
                DbUtils.closeQuietly(_param._psAttendSubclass);
            }
            db2.commit();
            db2.close();
        }

    }// doGetの括り

    private void printMain(final HttpServletResponse response, final DB2UDB db2) throws Exception {
        final KNJD177FormAbstract form = _param.getFormClass(response);
        try {
            boolean hasData = false;
            final List studentList = getStudentInfo(db2);
            for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
                final Student student = (Student) iter.next();
                form.setHead(_param.getFormName());
                form.printStudent(student);
                if (_param._formName.equals("KNJD177E")) {
                    form.printHreport("SPECIALACTREMARK", student._specialactremarkSem1, 20, 10);
                    if (null != _param._semester && Integer.parseInt(_param._semester) > 1) {
                        form.printHreport("COMMUNICATION", student._specialactremarkSem2, 20, 10);
                    }
                } else {
                    form.printHreport("SPECIALACTREMARK", student._specialactremark, 20, 10);
                    if (_param._formName.equals("KNJD177D")) {
                        form.printHreport("TOTALSTUDYTIME", student._totalstudytime, 20, 10);
                        form.printHreport("COMMUNICATION", student._communication, 70, 3);
                    } else {
                        form.printHreport("COMMUNICATION", student._communication, 20, 10);
                    }
                }
                if (_param._remarkOnly) {
                    form._svf.VrEndRecord();
                } else {
                    if ("9".equals(_param._paramSemester)) printGetCredit(db2, form, student, "", "GET_CREDIT");
                    if ((_param.isPatarnC() || _param.isPatarnE()) && !_param._shimebiPrint) {
                        printAttendPatarnC(db2, form, student);
                    } else {
                        printAttendInfo(db2, form, student);
                    }
                    printScoreInfo(db2, form, student);
                }
                form._svf.VrEndPage();
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
            final KNJD177FormAbstract form,
            final Student student,
            final String yearDiv,
            final String field
    ) throws SQLException {
        final String sql = getReportCardInfo.getGetCreditNotContainAttendSubclassSql(_param._year,
                                                             SEMEALL,
                                                             student._schregno,
                                                             SUBJECT_D,
                                                             SUBJECT_T,
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

    private void printAttendInfo(final DB2UDB db2, final KNJD177FormAbstract form, final Student student) throws ParseException, SQLException {
        if (null == _param._psAttendSemes) {
            final String sql = getAttendSql();
            _param._psAttendSemes = db2.prepareStatement(sql);
            log.debug("getAttendSql = "+sql);
        }
        _param._psAttendSemes.setString(1, student._schregno);

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
            rs = _param._psAttendSemes.executeQuery();
            while (rs.next()) {
                if (SEMEALL.equals(rs.getString("SEMESTER"))) continue;
                final int mourning = rs.getInt("SUSPEND") + rs.getInt("MOURNING") + rs.getInt("VIRUS") + rs.getInt("KOUDOME");
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
            if (_param.isGakunenMatu() && SEMEALL.equals(_param._paramSemester)) {
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

    private String getAttendSql() throws ParseException {
        _param._attendParamMap.put("schregno", "?");
        final String rtnSql = AttendAccumulate.getAttendSemesSql(
                                                _param._year,
                                                _param._semester,
                                                null,
                                                _param._date,
                                                _param._attendParamMap
                                                );
        return rtnSql;
    }

    private void printAttendPatarnC(final DB2UDB db2, final KNJD177FormAbstract form, final Student student) throws ParseException, SQLException {
        log.debug(getAttendPatarnCSql(student));
        db2.query(getAttendPatarnCSql(student));
        ResultSet rs = null;
        try {
            int line = 1;
            rs = db2.getResultSet();
            while (rs.next()) {
                final int mourning = rs.getInt("SUSPEND") + rs.getInt("MOURNING");

                int lesson = rs.getInt("LESSON");
                int sick = rs.getInt("SICK");
                if ("1".equals(_param._knjSchoolMst._semOffDays)) {
                    lesson += rs.getInt("OFFDAYS");
                    sick += rs.getInt("OFFDAYS");
                }

                final int present = lesson - mourning;
                final int attend = present - sick;
                form.printAttend(line,
                                 rs.getString("NAME"),
                                 String.valueOf(lesson),
                                 String.valueOf(mourning),
                                 String.valueOf(present),
                                 String.valueOf(attend),
                                 String.valueOf(sick),
                                 rs.getString("LATE"),
                                 rs.getString("EARLY")
                        );
                line++;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private String getAttendPatarnCSql(final Student student) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_D011 AS (");
        stb.append("     SELECT");
        stb.append("         NAMECD2 as CODE,");
        stb.append("         NAME1 as SEMESTER,");
        stb.append("         NAME2 as MONTH,");
        stb.append("         NAME3 as NAME,");
        stb.append("         CASE WHEN NAME2 BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1))");
        stb.append("              ELSE YEAR END || NAME2 AS YEAR_MONTH");
        stb.append("     FROM");
        stb.append("         V_NAME_MST");
        stb.append("     WHERE");
        stb.append("         YEAR='" + _param._year + "' AND");
        stb.append("         NAMECD1='D011' AND");
        stb.append("         NAME1 <= '" + _param._semester + "'");
        stb.append(" )");

        stb.append(" ,T_SEMES AS (");
        stb.append("     SELECT");
        stb.append("         SCHREGNO, SEMESTER, MONTH,");
        stb.append("         LESSON, OFFDAYS, ABROAD, MOURNING, SUSPEND, ABSENT,");
        stb.append("         SICK, NOTICE, NONOTICE, LATE, EARLY,");
        stb.append("         CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ");
        stb.append("              ELSE YEAR END || MONTH AS YEAR_MONTH");
        stb.append("     FROM");
        stb.append("         ATTEND_SEMES_DAT");
        stb.append("     WHERE");
        stb.append("         YEAR = '" + _param._year + "' AND");
        stb.append("         SEMESTER <= '" + _param._semester + "' AND");
        stb.append("         SCHREGNO = '" + student._schregno + "'");
        stb.append(" )");

        stb.append(" SELECT");
        stb.append("     W1.SCHREGNO,");
        stb.append("     D1.CODE,");
        stb.append("     D1.NAME,");
        stb.append("     SUM( VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0)) AS LESSON,");
        stb.append("     SUM(OFFDAYS) AS OFFDAYS,");
        stb.append("     SUM(MOURNING) AS MOURNING,");
        stb.append("     SUM(SUSPEND) AS SUSPEND,");
        stb.append("     SUM(ABSENT) AS ABSENT,");
        stb.append("     SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ) AS SICK,");
        stb.append("     SUM(LATE) AS LATE,");
        stb.append("     SUM(EARLY) AS EARLY");
        stb.append(" FROM");
        stb.append("     T_SEMES W1");
        stb.append("     INNER JOIN T_D011 D1 ON W1.SEMESTER <= D1.SEMESTER");
        stb.append("                         AND W1.YEAR_MONTH <= D1.YEAR_MONTH");
        stb.append("                         AND D1.YEAR_MONTH <= '" + _param._yearMonth + "'");
        stb.append(" GROUP BY");
        stb.append("     W1.SCHREGNO,");
        stb.append("     D1.CODE,");
        stb.append("     D1.NAME");
        stb.append(" ORDER BY");
        stb.append("     W1.SCHREGNO,");
        stb.append("     D1.CODE");

        return stb.toString();
    }

    private void printScoreInfo(final DB2UDB db2, final KNJD177FormAbstract form, final Student student) throws SQLException, ParseException {
        final Map subclassMap = getSubclassMap(db2, student);
        final List testList = getTestItemList(db2, student);
        final Map scoreMap = getScoreMap(db2, subclassMap, student);
        form.printScore(subclassMap, testList, scoreMap, db2);
    }

    /**
     * @return rtnMap : kye = 科目コード, value = Score
     */
    private Map getScoreMap(final DB2UDB db2, final Map subclassMap, final Student student) throws SQLException, ParseException {
        Map rtnMap = new HashMap();
        final String rankField = _param.getRankField(false);
        try {
            setRecordRankDat(db2, student, rtnMap, rankField);
            if (_param.isPatarnC() || _param.isPatarnE()) {
                setRecordMockDat(db2, student, rtnMap, rankField, _param._hasMock);
                if (_param._hasMock) {
                    setRecordMockRankDat(db2, student, rtnMap, rankField);
                    setRecordMockAverageDat(db2, student, rtnMap);
                }
            }

            setRecordAverageDat(db2, student, rtnMap);

            setRecordAverageClassCnt(db2, student, rtnMap);

            setRecordScoreDat(db2, student, rtnMap);

            setRecordRankTotal(db2, student, rtnMap);

            if (_param.isPatarnB() && "2".equals(_param._checkKettenDiv)) {
                setPerfectRecordPassScore(db2, student, rtnMap);
            }

            if (_param.isPatarnB() && !"2".equals(_param._checkKettenDiv) && "1".equals(_param._useAssessSubclassMst)) {
                setAssessSubclassMst(db2, student, rtnMap);
            }

            if (_param.isPatarnE()) {
                setAssessLevelMst(db2, student, rtnMap);
                if (_param._hasMock) {
                    setMockAssessLevelMst(db2, student, rtnMap);
                }
            }

            setAttendSubclass(db2, student, rtnMap);
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
                if (SEMEALL.equals(rsRank.getString("SEMESTER")) && _9900.equals(testKindItem)) {
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

    private void setRecordMockDat(final DB2UDB db2, final Student student, Map rtnMap, final String rankField, final boolean hasMock) throws SQLException {
        ResultSet rsRank = null;
        try {
            final String rankMockSql = getReportCardInfo.getRecordMockSql(_param._year, SSEMESTER, _param._semester, student._schregno, student._grade, null, null, hasMock, _param._useCurriculumcd);
            log.debug(rankMockSql);
            db2.query(rankMockSql);
            rsRank = db2.getResultSet();
            while (rsRank.next()) {
                final String testCd = rsRank.getString("TEST_CD");
                String semester = rsRank.getString("SEMESTER");
                Score score = new Score(semester);
                if (rtnMap.containsKey(getSubclasscd(rsRank))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsRank));
                }

                score.setScore(semester, testCd, rsRank.getString("SCORE"), SCORE);
                score.setScore(semester, testCd, rsRank.getString(rankField + "DEVIATION"), DEVIATION);
                score.setScore(semester, testCd, rsRank.getString(rankField + "RANK"), RANK);
                score.setScore(semester, testCd, rsRank.getString("SEQ"), SEQ);

                if (!rtnMap.containsKey(getSubclasscd(rsRank))) {
                    rtnMap.put(getSubclasscd(rsRank), score);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsRank);
        }
    }

    private void setRecordMockRankDat(final DB2UDB db2, final Student student, Map rtnMap, final String rankField) throws SQLException {
        ResultSet rs = null;
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String sql = getReportCardInfo.getRecordMockRankSql(_param._year, _param._ctrlSeme, student._schregno, _param._groupDiv,
                    student._grade, student._hrClass, course, _param.getSubclassCd(), _param._useCurriculumcd);
            log.debug(sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String testCd = rs.getString("TEST_CD");
                String semester = rs.getString("SEMESTER");
                Score score = new Score(semester);
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                final String subclasscd = SUBCLASSALL3.equals(mockSubclassCd) || SUBCLASSALL5.equals(mockSubclassCd) || SUBCLASSALL.equals(mockSubclassCd) ? mockSubclassCd : getSubclasscd(rs);
                if (rtnMap.containsKey(subclasscd)) {
                    score = (Score) rtnMap.get(subclasscd);
                }

                score.setScore(semester, testCd, rs.getString("SCORE"), SCORE);
                score.setScore(semester, testCd, rs.getString("AVG"), AVG);
                score.setScore(semester, testCd, rs.getString(rankField + "RANK"), RANK);
                score.setScore(semester, testCd, rs.getString("CNT"), CNT);
                score.setScore(semester, testCd, rs.getString("CLASS_RANK"), CLASSRANK);
                score.setScore(semester, testCd, rs.getString("CLASS_COUNT"), CLASSCNT);
                score.setScore(semester, testCd, rs.getString("SEQ"), SEQ);

                if (!rtnMap.containsKey(subclasscd)) {
                    rtnMap.put(subclasscd, score);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private void setRecordMockAverageDat(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rs = null;
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String sql = getReportCardInfo.getRecordMockAverageSql(_param._year, student._schregno, null, null, _param._groupDiv,
                    student._grade, student._hrClass, course, _param._useCurriculumcd);
            log.debug(sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String testCd = rs.getString("TEST_CD");
                final String semester = rs.getString("SEMESTER");
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");

                if (mockSubclassCd.equals(SUBCLASSALL3) || mockSubclassCd.equals(SUBCLASSALL5) || mockSubclassCd.equals(SUBCLASSALL)) {
                    continue;
                }

                Score score = new Score(semester);
                if (rtnMap.containsKey(getSubclasscd(rs))) {
                    score = (Score) rtnMap.get(getSubclasscd(rs));
                }

                score.setScore(semester, testCd, rs.getString("AVG_KANSAN"), AVG);

                if (!rtnMap.containsKey(getSubclasscd(rs))) {
                    rtnMap.put(getSubclasscd(rs), score);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
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
                if (SEMEALL.equals(rsAvg.getString("SEMESTER")) && _9900.equals(testKindItem)) {
                    continue;
                }
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
                    if (SEMEALL.equals(rsAvg.getString("SEMESTER")) && _9900.equals(testKindItem)) {
                        continue;
                    }
                    Score score = new Score(rsAvg.getString("SEMESTER"));
                    if (rtnMap.containsKey(getSubclasscd(rsAvg))) {
                        score = (Score) rtnMap.get(getSubclasscd(rsAvg));
                    }

                    score.setScore(rsAvg.getString("SEMESTER"), testKindItem, rsAvg.getString("COUNT"), CLASSCNT);

                    if (!rtnMap.containsKey(getSubclasscd(rsAvg))) {
                        rtnMap.put(getSubclasscd(rsAvg), score);
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
                    Score score = new Score(rsAvg.getString("SEMESTER"));
                    if (rtnMap.containsKey(getSubclasscd(rsAvg))) {
                        score = (Score) rtnMap.get(getSubclasscd(rsAvg));
                    }

                    score.setScore(rsAvg.getString("SEMESTER"), testKindItem, rsAvg.getString("COUNT"), CLASSCNT);

                    if (!rtnMap.containsKey(getSubclasscd(rsAvg))) {
                        rtnMap.put(getSubclasscd(rsAvg), score);
                    }
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAvg);
        }
    }

    private void setAssessSubclassMst(final DB2UDB db2, final Student student, final Map rtnMap) throws SQLException {
        ResultSet assessSubclassRs = null;
        try {
            final String assessSubclassSql = getReportCardInfo.getAssessSubclassMstSql(_param._year, _param._ctrlSeme, student._schregno);
            db2.query(assessSubclassSql);
            assessSubclassRs = db2.getResultSet();
            while (assessSubclassRs.next()) {
                final String subclassCd = getSubclasscd(assessSubclassRs);
                final String assessLevel = assessSubclassRs.getString("ASSESSLEVEL");
                if (rtnMap.containsKey(subclassCd) && "1".equals(assessLevel)) {
                    final String assessHigh1 = String.valueOf(assessSubclassRs.getInt("ASSESSHIGH"));
                    Score score = (Score) rtnMap.get(subclassCd);
                    score.setScore("1", _9900, assessHigh1, ASSESSHIGH1);
                    score.setScore("2", _9900, assessHigh1, ASSESSHIGH1);
                    score.setScore("3", _9900, assessHigh1, ASSESSHIGH1);
                    score.setScore("9", _9900, assessHigh1, ASSESSHIGH1);
                }
            }
        } finally {
            db2.commit();
        }
    }

    private void setAssessLevelMst(final DB2UDB db2, final Student student, final Map rtnMap) throws SQLException {
        ResultSet assessLevelRs = null;
        try {
            final String assessLevelSql = getReportCardInfo.getAssessLevelMstSql(_param._year, _param._semester, student._schregno, null, null, _param._groupDiv, null, _param._useCurriculumcd);
            db2.query(assessLevelSql);
            assessLevelRs = db2.getResultSet();
            while (assessLevelRs.next()) {
                final String subclassCd = getSubclasscd(assessLevelRs);
                final String testKindItem = assessLevelRs.getString("TESTKINDCD") + assessLevelRs.getString("TESTITEMCD");
                final String semester = assessLevelRs.getString("SEMESTER");
                if (!rtnMap.containsKey(subclassCd)) {
                    rtnMap.put(subclassCd, new Score(semester));
                }
                Score score = (Score) rtnMap.get(subclassCd);
                score.setScore(semester, testKindItem, assessLevelRs.getString("ASSESSLEVEL"), ASSESSLEVEL);
            }
        } finally {
            db2.commit();
        }
    }

    private void setMockAssessLevelMst(final DB2UDB db2, final Student student, final Map rtnMap) throws SQLException {
        ResultSet rs = null;
        try {
            final String sql = getReportCardInfo.getMockAssessLevelMstSql(_param._year, _param._ctrlSeme, student._schregno, null, null, _param._groupDiv, null);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String testCd = rs.getString("TEST_CD");
                final String semester = rs.getString("SEMESTER");
                final String subclassCd = rs.getString("SUBCLASSCD");

                if (!rtnMap.containsKey(subclassCd)) {
                    rtnMap.put(subclassCd, new Score(semester));
                }
                Score score = (Score) rtnMap.get(subclassCd);
                score.setScore(semester, testCd, rs.getString("ASSESSLEVEL"), ASSESSLEVEL);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private String getRecordRankTestAppointSql(
            final String schregno
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        if (_param._hasRecordProvFlgDat) {
            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.PROV_FLG = '1' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + SEMEALL + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _9900 + "' ");
        if (_param._hasRecordProvFlgDat) {
            stb.append("     AND T2.YEAR IS NULL ");
        }

        return stb.toString();
    }

    private void setRecordScoreDat(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rsScore = null;
        // 学年評定
        try {
            final String rankSql = getRecordRankTestAppointSql(student._schregno);
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
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsScore);
        }

        // 履修単位数・修得単位数
        try {
            final String scoreSql = getReportCardInfo.getRecordScoreSql(_param._year, SEMEALL, student._schregno, null, null, "00", _param._useCurriculumcd);
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

        // 学年成績
        try {
            final String rankvSql = getReportCardInfo.getRecordRankVSql(_param._year, SEMEALL, SEMEALL, student._schregno, student._grade, null, null);
            log.debug(rankvSql);
            db2.query(rankvSql);
            rsScore = db2.getResultSet();
            while (rsScore.next()) {
                final String testKindItem = rsScore.getString("TESTKINDCD") + rsScore.getString("TESTITEMCD");
                final Score score;
                if (rtnMap.containsKey(getSubclasscd(rsScore))) {
                    score = (Score) rtnMap.get(getSubclasscd(rsScore));
                } else {
                    score = new Score(rsScore.getString("SEMESTER"));
                    rtnMap.put(getSubclasscd(rsScore), score);
                }

                score.setScore(rsScore.getString("SEMESTER"), testKindItem, rsScore.getString("SCORE"), SCORE);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsScore);
        }
    }

    private void setPerfectRecordPassScore(final DB2UDB db2, final Student student, Map rtnMap) throws SQLException {
        ResultSet rsScore = null;
        try {
            final String perfectRecordSql = getReportCardInfo.getPerfectRecordSql(_param._year, _param._ctrlSeme, student._schregno, _param._useCurriculumcd);
            log.debug(perfectRecordSql);
            db2.query(perfectRecordSql);
            rsScore = db2.getResultSet();
            while (rsScore.next()) {
                final String semester = rsScore.getString("SEMESTER");
                final String testKindItem = rsScore.getString("TESTKINDCD") + rsScore.getString("TESTITEMCD");
                final String subclassCd = getSubclasscd(rsScore);
                if (!rtnMap.containsKey(subclassCd)) {
                    rtnMap.put(subclassCd, new Score(semester));
                }
                final Score score = (Score) rtnMap.get(subclassCd);
                score.setScore(semester, testKindItem, rsScore.getString("PASS_SCORE"), PASSSCORE);
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
                if (SEMEALL.equals(rsRankTotal.getString("SEMESTER")) && _9900.equals(testKindItem)) {
                    continue;
                }
                final String semester = rsRankTotal.getString("SEMESTER");
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
                score.setScore(semester, testKindItem, rsRankTotal.getString(_param.getRankField()), RANK);
                score.setScore(semester, testKindItem, rsRankTotal.getString(_param.getClassRankField()), CLASSRANK);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsRankTotal);
        }
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
                score.setScore(semester, testKindItem, rsRankTotal.getString(_param.getRankField()), RANK);
                score.setScore(semester, testKindItem, rsRankTotal.getString(_param.getClassRankField()), CLASSRANK);
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
            if (null == _param._psAttendSubclass) {
                _param._attendParamMap.put("schregno", "?");
                final String attendSql = AttendAccumulate.getAttendSubclassAbsenceSql(
                                                        _param._year,
                                                        _param._semester,
                                                        null,
                                                        _param._date,
                                                        _param._attendParamMap);

                log.debug(attendSql);
                _param._psAttendSubclass = db2.prepareStatement(attendSql);
            }

            _param._psAttendSubclass.setString(1, student._schregno);
            rsAttend = _param._psAttendSubclass.executeQuery();
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

        public String toString() {
            return "Score(" + _rankMap + ")";
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
        String _passScore;
        String _assessLevel;
        String _dankaiHigh1;

        public TestScore(String score, String scoreDiv) {
            setScore(score, scoreDiv);
        }

        private void setScore(final String score, final String scoreDiv) {
            if (scoreDiv.equals(SCORE)) {
                _score = score;
            } else if (scoreDiv.equals(AVG)) {
                _avg = score;
                if (null != _avg) {
                    _bdAvg = new BigDecimal(_avg);
                }
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
            } else if (scoreDiv.equals(PASSSCORE)) {
                _passScore = score;
            } else if (scoreDiv.equals(ASSESSLEVEL)) {
                _assessLevel = score;
            } else if (scoreDiv.equals(ASSESSHIGH1)) {
                _dankaiHigh1 = score;
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
        final List subclassOrder = new ArrayList();
        rtnMap.put(_param._keySubclassOrder, subclassOrder);
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
                    subclassOrder.add(getSubclasscd(rsSubclass));
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
        stb.append("     T1.SUBCLASSCD ");
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
        stb.append("     T1.SUBCLASSCD ");
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
        stb.append("     AND NOT (T1.VALUE IS NULL AND T1.COMP_CREDIT IS NULL AND T1.GET_CREDIT IS NULL) ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" ), SUBCLASS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("     COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("     COMBINED_CURRICULUM_CD AS CURRICULUM_CD ");
        } else {
            stb.append("     substr(T1.COMBINED_SUBCLASSCD, 1, 2) AS CLASSCD ");
        }
        stb.append(" FROM ");
        stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1, ");
        stb.append("     REC_RANK T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.ATTEND_CLASSCD = T2.CLASSCD AND ");
            stb.append("     T1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND AND ");
            stb.append("     T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD AND ");
        }
        stb.append("     T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.COMBINED_CLASSCD, ");
            stb.append("     T1.COMBINED_SCHOOL_KIND, ");
            stb.append("     T1.COMBINED_CURRICULUM_CD, ");
        }
        stb.append("     T1.COMBINED_SUBCLASSCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.SUBCLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD AS CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD AS CURRICULUM_CD ");
        } else {
            stb.append("     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD ");
        }
        stb.append(" FROM ");
        stb.append("     REC_RANK T1 ");
        if (_param.isPatarnC() || _param._isNoPrintMoto) {
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
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.CLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     ,SCHOOL_KIND ");
            stb.append("     ,CURRICULUM_CD ");
        }
        stb.append(" FROM ");
        stb.append("     CREDIT_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR        = '" + _param._year + "' AND ");
        stb.append("     T1.COURSECD    = '" + student._courseCd + "' AND ");
        stb.append("     T1.MAJORCD     = '" + student._majorCd + "' AND ");
        stb.append("     T1.GRADE       = '" + student._grade + "' AND ");
        stb.append("     T1.COURSECODE  = '" + student._courseCode + "' AND ");
        stb.append("     T1.COMP_UNCONDITION_FLG = '1' ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CLASSCD ");
        if ((_param.isPatarnC() || _param.isPatarnE()) && _param._hasMock) {
            stb.append(" ), REC_MOC AS ( ");
            stb.append(" SELECT ");
            stb.append("     MOCKCD ");
            stb.append(" FROM ");
            stb.append("     RECORD_MOCK_ORDER_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND GRADE = '" + student._grade + "' ");
            stb.append("     AND TEST_DIV = '2' ");
            stb.append(" ORDER BY ");
            stb.append("     SEQ ");
            stb.append("  ");
            stb.append(" ), REC_MOC_ALL AS ( ");
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       L1.SCHOOL_KIND, ");
                stb.append("       L1.CURRICULUM_CD, ");
            }
            //stb.append("     T1.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     VALUE(L1.SUBCLASSCD, T1.MOCK_SUBCLASS_CD) AS SUBCLASSCD, ");
            stb.append("     VALUE(L2.SHOWORDER3, 999) AS SUBCLASSORDER3, ");
            stb.append("     VALUE(L2.SUBCLASSORDERNAME2, L2.SUBCLASSNAME, L1.SUBCLASS_NAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     MOCK_DAT T1 ");
            stb.append("     INNER JOIN REC_MOC T2 ON T2.MOCKCD = T1.MOCKCD ");
            stb.append("     LEFT JOIN MOCK_SUBCLASS_MST L1 ON T1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD AND L1.SCHOOL_KIND = L2.SCHOOL_KIND AND L1.CURRICULUM_CD = L2.CURRICULUM_CD AND L1.SUBCLASSCD = L2.SUBCLASSCD ");
            } else {
                stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L1.SUBCLASSCD = L2.SUBCLASSCD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("     AND (T1.SCORE IS NOT NULL OR T1.SCORE_DI IS NOT NULL) ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     VALUE(L1.SHOWORDER3, 999) AS SUBCLASSORDER3, ");
        stb.append("     VALUE(L1.SUBCLASSORDERNAME2, L1.SUBCLASSNAME) AS SUBCLASSNAME, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     VALUE(L2.SHOWORDER3, 999) AS CLASSORDER3, ");
        stb.append("     VALUE(L2.CLASSORDERNAME2, L2.CLASSNAME) AS CLASSNAME ");
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
        if (_param.isPatarnC() || _param.isPatarnE()) {
            if (_param._hasMock) {
                stb.append(" UNION ");
                stb.append(" SELECT ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("       T1.SCHOOL_KIND, ");
                    stb.append("       T1.CURRICULUM_CD, ");
                }
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SUBCLASSORDER3, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
                stb.append("     VALUE(L2.SHOWORDER3, 999) AS CLASSORDER3, ");
                stb.append("     VALUE(L2.CLASSORDERNAME2, L2.CLASSNAME) AS CLASSNAME ");
                stb.append(" FROM ");
                stb.append("     REC_MOC_ALL T1 ");
                stb.append("     LEFT JOIN CLASS_MST L2 ON substr(T1.SUBCLASSCD, 1, 2) = L2.CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("     AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append(" GROUP BY ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("       T1.SCHOOL_KIND, ");
                    stb.append("       T1.CURRICULUM_CD, ");
                }
                stb.append("     SUBCLASSCD, ");
                stb.append("     SUBCLASSORDER3, ");
                stb.append("     SUBCLASSNAME, ");
                stb.append("     CLASSCD, ");
                stb.append("     VALUE(L2.SHOWORDER3, 999), ");
                stb.append("     VALUE(L2.CLASSORDERNAME2, L2.CLASSNAME) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     CLASSORDER3, ");
            stb.append("     CLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSORDER3, ");
            stb.append("     SUBCLASSCD ");
        } else {
            stb.append(" GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.CLASSCD, ");
                stb.append("       T1.SCHOOL_KIND, ");
                stb.append("       T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     VALUE(L1.SHOWORDER3, 999), ");
            stb.append("     VALUE(L1.SUBCLASSORDERNAME2, L1.SUBCLASSNAME), ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     VALUE(L2.SHOWORDER3, 999), ");
            stb.append("     VALUE(L2.CLASSORDERNAME2, L2.CLASSNAME) ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(L2.SHOWORDER3, 999), ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     VALUE(L1.SHOWORDER3, 999), ");
            stb.append("     T1.SUBCLASSCD ");
        }
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
        if (_param.isPatarnC() || _param.isPatarnE()) {
            stb.append(" WITH REC_MOC AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.TEST_DIV, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     CASE WHEN T1.TEST_DIV = '1' ");
            stb.append("          THEN L2.TESTITEMNAME ");
            stb.append("          ELSE L1.MOCKNAME2 ");
            stb.append("     END AS TESTITEMNAME, ");
            stb.append("     T1.MOCKCD, ");
            stb.append("     T1.SEQ, ");
            stb.append("     L2.COUNTFLG ");
            stb.append(" FROM ");
            stb.append("     RECORD_MOCK_ORDER_DAT T1 ");
            stb.append("     LEFT JOIN MOCK_MST L1 ON T1.MOCKCD = L1.MOCKCD ");
            stb.append("     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L2 ON T1.YEAR = L2.YEAR ");
            stb.append("          AND T1.SEMESTER = L2.SEMESTER ");
            stb.append("          AND T1.TESTKINDCD || T1.TESTITEMCD = L2.TESTKINDCD || L2.TESTITEMCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.GRADE = '" + student._grade + "' ");
            if ("1".equals(_param._knjd177cUseSeme2Form)) {
                stb.append("     AND NOT (T1.TEST_DIV = '1' AND T1.SEMESTER = '3' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00') ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.TEST_DIV, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.TESTITEMNAME, ");
            stb.append("     T1.MOCKCD, ");
            stb.append("     T1.SEQ, ");
            stb.append("     T1.COUNTFLG ");
            stb.append(" FROM ");
            stb.append("     REC_MOC T1 ");
            stb.append(" UNION ");
        }
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
        if ("1".equals(_param._knjd177cUseSeme2Form)) {
            stb.append("     AND NOT (T1.SEMESTER = '3' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00') ");
        }
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
        final String sql = getStudentInfoSql(_param.getAddrField(), _param.getAddrTable(), _param.getAddrTableOn());
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
                                              rs.getString("SPECIALACTREMARK_SEM1"),
                                              rs.getString("SPECIALACTREMARK_SEM2"),
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
        final String _specialactremarkSem1;
        final String _specialactremarkSem2;
        final String _communication;
        final String _totalstudytime;
        final String _hrStaffName;
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
                final String specialactremarkSem1,
                final String specialactremarkSem2,
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
            _specialactremarkSem1 = specialactremarkSem1;
            _specialactremarkSem2 = specialactremarkSem2;
            _communication = communication;
            _totalstudytime = totalstudytime;
            _hrStaffName = hrStaffName;
        }
        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }
    private String getStudentInfoSql(final String fieldName, final String tableName, final String tableOn) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VSCH.SCHREGNO, ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO, ");
        stb.append("     VALUE(VSCH.HR_NAME, '') AS HR_NAME, ");
        stb.append("     VALUE(VSCH.HR_NAMEABBV, '') AS HR_NAMEABBV, ");
        stb.append("     VSCH.NAME, ");
        stb.append("     VSCH.COURSECD, ");
        stb.append("     VALUE(L1.COURSENAME, '') AS COURSENAME, ");
        stb.append("     VSCH.MAJORCD, ");
        stb.append("     VALUE(L1.MAJORNAME, '') AS MAJORNAME, ");
        stb.append("     VSCH.COURSECODE, ");
        stb.append("     VALUE(L2.COURSECODENAME, '') AS COURSECODENAME, ");
        stb.append("     L3." + fieldName + "_NAME AS GNAME, ");
        stb.append("     L3." + fieldName + "_ZIPCD AS GZIP, ");
        stb.append("     L3." + fieldName + "_ADDR1 AS GADDR1, ");
        stb.append("     L3." + fieldName + "_ADDR2 AS GADDR2, ");
        stb.append("     L4.SPECIALACTREMARK, ");
        stb.append("     L6.SPECIALACTREMARK AS SPECIALACTREMARK_SEM1, ");
        stb.append("     L7.SPECIALACTREMARK AS SPECIALACTREMARK_SEM2, ");
        stb.append("     L8.COMMUNICATION, ");
        stb.append("     L4.TOTALSTUDYTIME, ");
        stb.append("     (SELECT W1.STAFFNAME FROM STAFF_MST W1 WHERE W1.STAFFCD=L5.TR_CD1) AS STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO VSCH ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
        stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
        stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
        stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
        stb.append("     LEFT JOIN " + tableName + " L3 ON VSCH.SCHREGNO = L3.SCHREGNO " + tableOn);
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L4 ON VSCH.YEAR = L4.YEAR ");
        stb.append("          AND L4.SEMESTER = '9' ");
        stb.append("          AND VSCH.SCHREGNO = L4.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L5 ON VSCH.YEAR = L5.YEAR ");
        stb.append("          AND VSCH.SEMESTER = L5.SEMESTER ");
        stb.append("          AND VSCH.GRADE = L5.GRADE ");
        stb.append("          AND VSCH.HR_CLASS = L5.HR_CLASS ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L6 ON VSCH.YEAR = L6.YEAR ");
        stb.append("          AND L6.SEMESTER = '1' ");
        stb.append("          AND VSCH.SCHREGNO = L6.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L7 ON VSCH.YEAR = L7.YEAR ");
        stb.append("          AND L7.SEMESTER = '2' ");
        stb.append("          AND VSCH.SCHREGNO = L7.SCHREGNO ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT L8 ON VSCH.YEAR = L8.YEAR ");
        if (SEMEALL.equals(_param._paramSemester)) {
            stb.append("          AND L8.SEMESTER = '" + _param._defineSchoolCode.semesdiv + "' ");
        } else {
            stb.append("          AND L8.SEMESTER = '" + _param._paramSemester + "' ");
        }
        stb.append("          AND VSCH.SCHREGNO = L8.SCHREGNO ");
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
        log.fatal("$Revision: 64106 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    static class Param {
        final DB2UDB _db2;
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
        final String _rankDiv; //順位の基準点 1:総合点 2:平均点 3:偏差値
        final String _rankPrint;
        final String _formName;
        final String _inState;
        final boolean _printKatei;
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
        String _dankaiItem = ""; //評定段階区分の文言
        int _dankaiHigh1;
        final String _keySubclassOrder = "SUBCLASS_ORDER";
        final String _dankaiYomikae;
        final String _checkKettenDiv;
        final String _kyoukaSu;
        final boolean _hasMock;
        KNJDefineSchool _defineSchoolCode;       //各学校における定数等設定
        KNJSchoolMst _knjSchoolMst;
        boolean _remarkOnly;
        final String _knjd177cUseSeme2Form; // '1'の場合、2学期用フォームを使用し、3学期評価を表示しない
        boolean _shimebiPrint;

        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;

        /** 科目別評定マスタを使用するか */
        final String _useAssessSubclassMst;

        final Map _attendParamMap = new HashMap();
        PreparedStatement _psAttendSemes;
        PreparedStatement _psAttendSubclass;

        final boolean _hasRecordProvFlgDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _db2 = db2;
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
                _rankDiv = request.getParameter("RANK_DIV");
                _formName = request.getParameter("FORMNAME");
                _rankPrint = request.getParameter("RANK_PRINT");
                _printKatei = null == request.getParameter("PRINT_KATEI") ? false : true;
                _isRankPrintAll = null == request.getParameter("RANK_PRINT_ALL") ? true : false;
                _isPrintKekka = null == request.getParameter("KEKKA_PRINT") ? true : false;
                _checkKettenDiv = request.getParameter("checkKettenDiv");
                _kyoukaSu = request.getParameter("KYOUKA_SU");
                _useCurriculumcd = request.getParameter("useCurriculumcd");
                _useClassDetailDat = request.getParameter("useClassDetailDat");
                _useAssessSubclassMst = request.getParameter("useAssessSubclassMst");
                _remarkOnly = "1".equals(request.getParameter("remarkOnly"));
                _knjd177cUseSeme2Form = request.getParameter("knjd177cUseSeme2Form");
                _shimebiPrint = "1".equals(request.getParameter("SHIMEBI_PRINT"));

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
                while (rsSeme.next()) {
                    _semesterMap.put(rsSeme.getString("SEMESTER"), rsSeme.getString("SEMESTERNAME"));
                    seme = rsSeme.getString("SEMESTER").equals(SEMEALL) ? seme : rsSeme.getString("SEMESTER");

                    // 順位の印字制御
                    final String print = request.getParameter("RANK_PRINT_SEM" + rsSeme.getString("SEMESTER"));
                    _rankPrintSemMap.put(rsSeme.getString("SEMESTER"), print);
                }
                _semester = request.getParameter("SEMESTER").equals(seme) ? SEMEALL : request.getParameter("SEMESTER");
                _paramSemester = request.getParameter("SEMESTER");
                _d026 = setNameMstV026(db2);

                _defineSchoolCode = new KNJDefineSchool();
                _defineSchoolCode.defineCode(db2, _year);         //各学校における定数等設定

                try {
                    _knjSchoolMst = new KNJSchoolMst(db2, _year);
                } catch (SQLException e) {
                    log.warn("学校マスタ取得でエラー", e);
                }

                _dankaiHigh1 = 0;//初期化
                _dankaiItem = setAssessMst(db2);//評定段階区分の文言
                _dankaiYomikae = request.getParameter("DANKAI_YOMIKAE"); // 段階読替
                _hasMock = hasMock(db2, _year, _hrClass);
            } finally {
                DbUtils.closeQuietly(rsZ010);
                DbUtils.closeQuietly(rsZ012);
                DbUtils.closeQuietly(rsCertif);
                DbUtils.closeQuietly(rsSeme);
                db2.commit();
            }

            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _hasRecordProvFlgDat = setTableColumnCheck(db2, "RECORD_PROV_FLG_DAT", null);
        }

        private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT 1 FROM ");
            if (StringUtils.isBlank(colname)) {
                stb.append("SYSCAT.TABLES");
            } else {
                stb.append("SYSCAT.COLUMNS");
            }
            stb.append(" WHERE TABNAME = '" + tabname + "' ");
            if (!StringUtils.isBlank(colname)) {
                stb.append(" AND COLNAME = '" + colname + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean hasTableColumn = false;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    hasTableColumn = true;
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
            return hasTableColumn;
        }

        private boolean hasMock(final DB2UDB db2, final String year, final String hrClass) throws SQLException {
            boolean rtn = false;
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT MOCKCD ");
            sql.append(" FROM RECORD_MOCK_ORDER_DAT ");
            sql.append(" WHERE YEAR = '" + year + "' AND GRADE = '" + hrClass.substring(0, 2) + "' AND TEST_DIV = '2' ");
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rtn = true;
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            return rtn;
        }

        public String getSubclassCd() {
            if (null == _kyoukaSu) {
                return null;
            } else {
                if (_kyoukaSu.equals("3")) {
                    return SUBCLASSALL3;
                }
                if (_kyoukaSu.equals("5")) {
                    return SUBCLASSALL5;
                }
                if (_kyoukaSu.equals("9")) {
                    return SUBCLASSALL;
                }
            }
            return null;
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
                return KNJ_EditDate.h_format_JP(_db2, date);
            }
        }

        public String changePrintYear() {
            if (_isSeireki) {
                return _year + "年度";
            } else {
                return KNJ_EditDate.gengou(_db2, Integer.parseInt(_year)) + "年度";
            }
        }

        private String getAddrField() {
            return _addrDiv.equals("3") ? "SEND" : _addrDiv.equals("1") ? "GUARD" : "GUARANTOR";
        }

        private String getAddrTable() {
            return _addrDiv.equals("3") ? "SCHREG_SEND_ADDRESS_DAT" : "GUARDIAN_DAT";
        }

        private String getAddrTableOn() {
            return _addrDiv.equals("3") ? " AND L3.DIV = '1' " : "";
        }

        private KNJD177FormAbstract getFormClass(final HttpServletResponse response) throws Exception {
            if (_formName.equals("KNJD177A")) {
                return new KNJD177A(response, this);
            } else if (_formName.equals("KNJD177B")) {
                return new KNJD177B(response, this);
            } else if (_formName.equals("KNJD177D")) {
                return new KNJD177D(response, this);
            } else if (_formName.equals("KNJD177E")) {
                return new KNJD177E(response, this);
            } else {
                return new KNJD177C(response, this);
            }
        }

        public String getFormName() {
            if (_formName.equals("KNJD177A")) {
                return _printKatei ? "KNJD177A.frm" : "KNJD177A_2.frm";
            } else if (_formName.equals("KNJD177B")) {
                return _printKatei ? "KNJD177B.frm" : "KNJD177B_2.frm";
            } else if (_formName.equals("KNJD177D")) {
                return "KNJD177D.frm";
            } else if (_formName.equals("KNJD177E")) {
                return "KNJD177E.frm";
            } else {
                if ("1".equals(_knjd177cUseSeme2Form) || _defineSchoolCode.semesdiv <= 2) {
                    return _printKatei ? "KNJD177C_2.frm" : "KNJD177C_4.frm";
                } else {
                    return _printKatei ? "KNJD177C.frm" : "KNJD177C_3.frm";
                }
            }
        }

        private boolean isRankSou() {
            return "1".equals(_rankDiv);
        }

        private boolean isRankAvg() {
            return "2".equals(_rankDiv);
        }

        private boolean isRankDev() {
            return "3".equals(_rankDiv);
        }

        public String getClassRankField() {
            return isRankSou() ? "CLASS_RANK" : isRankAvg() ? "CLASS_AVG_RANK" : "CLASS_DEVIATION_RANK";
        }

        public String getRankField() {
            if (_groupDiv.equals("1")) {
                return "1".equals(_rankDiv) ? "GRADE_RANK" : "2".equals(_rankDiv) ? "GRADE_AVG_RANK" : "GRADE_DEVIATION_RANK";
            } else if (_groupDiv.equals("2")) {
                return isRankSou() ? "CLASS_RANK" : isRankAvg() ? "CLASS_AVG_RANK" : "CLASS_DEVIATION_RANK";
            } else {
                return "1".equals(_rankDiv) ? "COURSE_RANK" : "2".equals(_rankDiv) ? "COURSE_AVG_RANK" : "COURSE_DEVIATION_RANK";
            }
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
        boolean isPatarnE() {
            return _formName.equals("KNJD177E");
        }

        boolean isPatarnC() {
            return _formName.equals("KNJD177C");
        }

        boolean isPatarnB() {
            return _formName.equals("KNJD177B");
        }

        boolean isGakunenMatu() {
            return _semester.equals(SEMEALL) ? true : false;
        }
    }

    static class CertifSchool {
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
 // KNJD177

// eof
