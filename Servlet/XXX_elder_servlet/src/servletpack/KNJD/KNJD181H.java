// kanji=漢字
/*
 * $Id: dc599d4bbdcb13bb3e996bdc5c5023ed4439b519 $
 *
 * 作成日: 2009/08/20
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */

public class KNJD181H {

    private static final String SLUMP_CD = "1";
    private static final String SUBCLASS_ALL = "999999";

    private static final String PATTERN_A = "1";
    private static final String PATTERN_B = "2";
    private static final String PATTERN_C = "3";

    private static final String SENKOUKA   = "4903";
    private static final String HOKENRIRYOKA = "4902";

    private static final Log log = LogFactory.getLog(KNJD181H.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alpWrap svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasData = false;

        sd.setSvfInit(request, response, svf);
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        final Param param = createParam(request, db2);

        final Form form = new Form(svf, param);

        try {
            final List students = Student.getStudents(db2, param);

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                form.print(student);

                hasData = true;
            }

            if (!hasData) {
                log.warn("データがありません");
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }

        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }

    private static String nullToAlt(final String str, final String alt) {
        return null == str ? alt : str;
    }

    private static class Student {

        public String _attendNo;
        public String _grade;
        public String _courseCd;
        public String _courseName;
        public String _majorCd;
        public String _majorName;
        public String _courseCode;
        public String _courseCodeName;
        public String _gradeName1;
        public String _hrName;
        public String _hrNameAbbv;
        public String _trName1;

        public String _name;
        final String _schregno;

        final List _subclassInfos = new ArrayList();

        int _totalCredit = 0;
        final Map _subclassScoreMap = new HashMap();
        final Map _attendMap = new TreeMap();
        final Map _scoreRankMap = new HashMap();
        final Map _subclassAbsenceHigh = new HashMap();
        final Map _recordTotalStudieTimeDat = new HashMap();
        final Map _hReportRemarks = new HashMap();

        //String _recordDocumentKindDatFootnote;

        //int _previousScredits;

        /** 総合的な学習の時間の科目が単位マスタに登録されているか */
        // public boolean _hasSogotekinaGakusyunoJikan;

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public void setAttendance(final String semester, final Attendance a) {
            _attendMap.put(semester, a);
        }

        public Attendance getAttendance(final String semester) {
            final Attendance a = (Attendance) _attendMap.get(semester);
            return (a == null) ? new Attendance() : a;
        }

        public void setSubclassAttendance(final String subclassCd, final String semester, final SubclassAttendance sa) {
            findSubclass(subclassCd).setAttendance(semester, sa);
        }

        public SubclassAttendance getSubclassAttendance(final String subclassCd, final String semester) {
            return findSubclass(subclassCd).getAttendance(semester);
        }

        private static Map getMappedMap(final Map map, final String key) {
            if (!map.containsKey(key)) {
                map.put(key, new HashMap());
            }
            return (Map) map.get(key);
        }

        private Map getScoreRankMap(final String testKindcd) {
            return getMappedMap(_scoreRankMap, testKindcd);
        }

        /**
         * テスト種別ごとの序列をセットする
         * @param testKindcd
         * @param tableDiv
         * @param scoreRank
         */
        public void putScoreRank(final String testKindcd, final String tableDiv, final ScoreRank scoreRank) {
            getScoreRankMap(testKindcd).put(tableDiv, scoreRank);
        }

        public ScoreRank getScoreRank(final String testKindcd, final String tableDiv) {
            return (ScoreRank) getScoreRankMap(testKindcd).get(tableDiv);
        }

        /**
         * 指定テスト種別の序列データを得る
         * @param testKindcd 指定テスト種別
         * @return
         */
        public ScoreRank getScoreRank(final String testKindcd) {
            return (ScoreRank) getScoreRankMap(testKindcd).get(Param.REC);
        }

        public void putScoreValue(final String subclassCd, final String testKindcd, final String tableDiv, final ScoreValue scoreValue) {
            findSubclass(subclassCd).setScoreValue(testKindcd, tableDiv, scoreValue);
        }

        public ScoreValue getScoreValue(final String subclassCd, final String testKindCd, final String tableDiv) {
            return findSubclass(subclassCd).getScoreValue(testKindCd, tableDiv);
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        private Subclass findSubclass(final String subclassCd) {
            if (null == getSubclass(subclassCd)) {
                _subclassScoreMap.put(subclassCd, new Subclass(_schregno, subclassCd));
            }
            return getSubclass(subclassCd);
        }

        /**
         *
         * @param subclassCd
         * @return
         */
        public Subclass getSubclass(final String subclassCd) {
            return (Subclass) _subclassScoreMap.get(subclassCd);
        }

        /**
         * 指定科目の欠課数上限値を得る
         * @param subclassCd 科目コード
         * @return
         */
        public AbsenceHigh getAbsenceHigh(final String subclassCd) {
            return (AbsenceHigh) _subclassAbsenceHigh.get(subclassCd);
        }

        /**
         * 指定の科目コードのレコードがあるか
         * @param subclassCd 科目コード
         * @return 指定の科目コードのレコードがあるならtrue
         */
        public boolean hasSubclassInfo(final String subclassCd) {
            if (null == subclassCd) {
                return false;
            }
            for (final Iterator it = _subclassInfos.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();
                if (subclassCd.equals(info._subclassCd)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }

        /**
         * 学籍番号の生徒を得る
         * @param schregno 学籍番号
         * @param students 生徒のリスト
         * @return 学籍番号の生徒
         */
        private static Student getStudent(final String schregno, final List students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        // -----

        public static List getStudents(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs1 = null;
            List students = new ArrayList();
            try {
                final String sqlRegdData = sqlRegdData(param);
                log.debug("sqlRegdData = " + sqlRegdData);
                ps = db2.prepareStatement(sqlRegdData);
                rs1 = ps.executeQuery();

                while( rs1.next() ){

                    Student student = new Student(rs1.getString("SCHREGNO"));
                    students.add(student);

                    student._grade = rs1.getString("GRADE");
                    student._courseCd = rs1.getString("COURSECD");
                    student._majorCd = rs1.getString("MAJORCD");
                    student._courseCode = rs1.getString("COURSECODE");

                    student._courseName = rs1.getString("COURSENAME");
                    student._majorName = rs1.getString("MAJORNAME");
                    student._courseCodeName = nullToAlt(rs1.getString("COURSECODENAME"), "");
                    student._gradeName1 = nullToAlt(rs1.getString("GRADE_NAME1"), "");
                    student._hrName = nullToAlt(rs1.getString("HR_NAME"), "");
                    student._hrNameAbbv = nullToAlt(rs1.getString("HR_NAMEABBV"), "");
                    student._attendNo = null == rs1.getString("ATTENDNO") || !NumberUtils.isDigits(rs1.getString("ATTENDNO"))? "" : Integer.parseInt(rs1.getString("ATTENDNO")) + "番";
                    final String name = nullToAlt(rs1.getString("NAME"), "");
                    final String realName = nullToAlt(rs1.getString("REAL_NAME"), "");
                    student._name = "1".equals(rs1.getString("USE_REAL_NAME")) ? realName : name;
                    student._trName1 = nullToAlt(rs1.getString("TR_NAME1"), "");

                    //log.debug("対象の生徒" + student);
                }
            } catch( Exception ex ) {
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs1);
                db2.commit();
            }

            setAttendData(db2, students, param);
            setScoreValue(db2, students, param);
            setRecDetail(db2, students, param);
            HReportRemark.setHReportRemark(db2, students, param);

            return students;
        }

        /**
         * 成績・序列・欠点をセットする
         * @param db2
         * @param student
         * @param param
         */
        private static void setScoreValue(final DB2UDB db2, final List students, final Param param) {

            final String prestatementRecordScore = sqlRecordScore(param);
            // log.debug("setScoreValue sql = " + prestatementRecordScore);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(prestatementRecordScore);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = rs.getString("SUBCLASSCD");
                        final String testKindCd = rs.getString("TESTKINDCD");
                        final String tableDiv = rs.getString("TABLE_DIV");

                        if (SUBCLASS_ALL.equals(subclassCd)) {
                            final String courseRank = rs.getString("COURSE_RANK");
                            final String courseAvgRank = rs.getString("COURSE_AVG_RANK");
                            final String courseCount = rs.getString("COURSE_COUNT");
                            final String classRank = rs.getString("CLASS_RANK");
                            final String classAvgRank = rs.getString("CLASS_AVG_RANK");
                            final String classCount = rs.getString("CLASS_COUNT");
                            final String score = rs.getString("SCORE");
                            final String avg = rs.getString("AVG");
                            final ScoreRank sr = new ScoreRank(tableDiv, testKindCd, score, avg,
                                    courseRank, courseAvgRank, courseCount, classRank, classAvgRank, classCount);

                            student.putScoreRank(testKindCd, tableDiv, sr);
                            // log.debug(" scoreRank = " + student + " , semTestKindCd = " + testKindCd + " , tableDiv = " + tableDiv + " "+ sr);
                        } else {
                            final String value = rs.getString("VALUE");
                            final ScoreValue sv = new ScoreValue(testKindCd, subclassCd, value);

                            student.putScoreValue(subclassCd, testKindCd, tableDiv, sv);

                        }
                    }

                    DbUtils.closeQuietly(rs);

                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlRecordScore(final Param param) {

            final String[] targets = param.getTargetTestKindCds();
            int rank = 0;

            for (int i = 0; i < targets.length; i++) {
                rank += 1;
            }
            final String[] fromRank = new String[rank];
            if (rank != 0) {
                for (int c = 0, i = 0; i < targets.length; i++) {
                    fromRank[c] = targets[i];
                    c += 1;
                }
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("   SELECT ");
            stb.append("        T1.* ");
            stb.append("   FROM SCHREG_REGD_DAT T1 ");
            stb.append("   WHERE T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("        AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append(" ), RECORD_VALUE AS ( ");
            if (rank != 0) {
                stb.append("   SELECT  '" + Param.REC + "' AS TABLE_DIV, T1.YEAR, T1.SCHREGNO, ");
                stb.append("           T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
                stb.append("           T1.SUBCLASSCD, T1.SEMESTER, ");
                stb.append("           T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
                stb.append("           T1.SCORE, T1.AVG, ");
                stb.append("           T1.COURSE_RANK, T1.COURSE_AVG_RANK, T3.COUNT AS COURSE_COUNT, ");
                stb.append("           T1.CLASS_RANK, T1.CLASS_AVG_RANK, T4.COUNT AS CLASS_COUNT, ");
                stb.append("           T6.SLUMP");
                stb.append("   FROM    RECORD_RANK_DAT T1");
                stb.append("   INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");

                stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T3 ON T3.YEAR = T1.YEAR ");
                stb.append("        AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
                stb.append("        AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("        AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("        AND T3.GRADE = T2.GRADE ");
                stb.append("        AND T3.AVG_DIV = '3' ");
                stb.append("        AND T3.HR_CLASS = '000' ");
                stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
                stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T4 ON T4.YEAR = T1.YEAR ");
                stb.append("        AND T4.SEMESTER || T4.TESTKINDCD || T4.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
                stb.append("        AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("        AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("        AND T4.GRADE = T2.GRADE ");
                stb.append("        AND T4.AVG_DIV = '2' ");
                stb.append("        AND T4.HR_CLASS = T2.HR_CLASS ");
                stb.append("        AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
                //不振科目
                stb.append("   LEFT JOIN RECORD_SLUMP_DAT T6 ON T6.YEAR = T1.YEAR ");
                stb.append("        AND T6.SEMESTER || T6.TESTKINDCD || T6.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
                stb.append("        AND T6.CLASSCD = T1.CLASSCD AND T6.SCHOOL_KIND = T1.SCHOOL_KIND AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("        AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("        AND T6.SCHREGNO = T1.SCHREGNO ");
                stb.append("        AND T6.SLUMP = '1' ");
                stb.append("   WHERE   T1.YEAR = '" + param._year + "' ");
                stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, fromRank));

            }
            stb.append(" ) ");
            stb.append("   SELECT  T1.TABLE_DIV ");
            stb.append("           , CASE WHEN T1.SUBCLASSCD = '" + SUBCLASS_ALL + "' THEN T1.SUBCLASSCD ");
            stb.append("              ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD END ");
            stb.append("             AS SUBCLASSCD ");
            stb.append("           ,T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS TESTKINDCD ");
            stb.append("           ,T1.SCORE, T1.AVG, T1.COURSE_RANK, T1.COURSE_AVG_RANK, T1.COURSE_COUNT, T1.CLASS_RANK, T1.CLASS_AVG_RANK, T1.CLASS_COUNT ");
            stb.append("           ,T1.VALUE, T1.SLUMP ");
            stb.append("   FROM    RECORD_VALUE T1");
            stb.append("   WHERE T1.YEAR = '" + param._year + "' ");
            stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            stb.append("            OR T1.CLASSCD BETWEEN '91' AND '92' ");
            stb.append("            OR T1.SUBCLASSCD = '" + SUBCLASS_ALL + "')");
            return stb.toString();
        }

        private static void setRecDetail (
                final DB2UDB db2,
                final List students,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String prestatementSubclass = sqlSubclass(param);
            // log.debug(" subclass sql = " + prestatementSubclass);
            try {
                ps = db2.prepareStatement(prestatementSubclass);
                for (final Iterator it = students.iterator(); it.hasNext();) {

                    final Student student = (Student) it.next();

                    student._subclassInfos.clear();
                    int pp = 0;
                    ps.setString(++pp, student._schregno);
                    ps.setString(++pp, student._schregno);
                    ps.setString(++pp, student._schregno);
                    ps.setString(++pp, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final String classabbv = rs.getString("CLASSABBV");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String credits = rs.getString("CREDITS");
                        final String compCredit = rs.getString("COMP_CREDIT");
                        final String getCredit = rs.getString("GET_CREDIT");
                        final String subclassCd = rs.getString("SUBCLASSCD");
                        final String attendSubclasscd = rs.getString("ATTEND_SUBCLASSCD");
                        final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");

                        final String namespare1 = rs.getString("NAMESPARE1");

                        final int replaceflg = rs.getInt("REPLACEFLG");
                        final String calculateCreditFlg = nullToAlt(rs.getString("CALCULATE_CREDIT_FLG"), "0");
                        final String num90 = rs.getString("NUM90");
                        final String num90Other = rs.getString("NUM90_OTHER");
                        final String over90 = rs.getString("OVER90");

                        final Subclass subclass = student.getSubclass(subclassCd);

                        final SubclassInfo info = new SubclassInfo(classabbv, subclassname, credits, compCredit, getCredit, subclassCd,
                                attendSubclasscd, combinedSubclasscd, namespare1, subclass, replaceflg, calculateCreditFlg, num90, num90Other, over90);
                        student._subclassInfos.add(info);
                    }

                    int totalCredit = 0;
                    for (final Iterator subit = student._subclassInfos.iterator(); subit.hasNext();) {
                        final SubclassInfo info = (SubclassInfo) subit.next();
                        if (null == info._getCredit
                                || (1 == info._replaceflg && student.hasSubclassInfo(info._combinedSubclasscd))
                                || (2 == info._replaceflg && (student.hasSubclassInfo(info._attendSubclasscd) || student.hasSubclassInfo(info._combinedSubclasscd)))) {
                            // 加算しない。
                        } else {
                            totalCredit += Integer.parseInt(info._getCredit);
                        }
                    }
                    student._totalCredit = totalCredit;
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlSubclass (final Param param) {

            StringBuffer stb = new StringBuffer();
            stb.append(" WITH ");
            stb.append(" SUBCLASS_CREDITS AS(");
            stb.append("   SELECT ");
            stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            stb.append("          SUBCLASSCD AS SUBCLASSCD, CREDITS, L1.NAMESPARE1 ");
            stb.append("   FROM   CREDIT_MST T1");
            stb.append("          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z011' ");
            stb.append("               AND L1.NAMECD2 = T1.REQUIRE_FLG ");
            stb.append("        , (SELECT  T3.GRADE, T3.COURSECD, T3.MAJORCD, T3.COURSECODE");
            stb.append("           FROM    SCHREG_REGD_DAT T3");
            stb.append("           WHERE   T3.SCHREGNO = ?");
            stb.append("               AND T3.YEAR = '" + param._year + "'");
            stb.append("               AND T3.SEMESTER = (SELECT  MAX(SEMESTER)");
            stb.append("                                  FROM    SCHREG_REGD_DAT T4");
            stb.append("                                  WHERE   T4.YEAR = '" + param._year + "'");
            stb.append("                                      AND T4.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("                                      AND T4.SCHREGNO = T3.SCHREGNO)");
            stb.append("          )T2 ");
            stb.append("   WHERE T1.YEAR = '" + param._year + "'");
            stb.append("     AND T1.GRADE = T2.GRADE");
            stb.append("     AND T1.COURSECD = T2.COURSECD");
            stb.append("     AND T1.MAJORCD = T2.MAJORCD");
            stb.append("     AND T1.COURSECODE = T2.COURSECODE");
            stb.append(" ) ");

            stb.append(" ,COMBINED_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("          COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, MIN( ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD) AS ATTEND_SUBCLASSCD");
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("          COMBINED_SUBCLASSCD");
            stb.append(" )");

            stb.append(" ,ATTEND_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, MAX(PRINT_FLG1) AS PRINT_FLG, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, MAX(");
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("          COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD");
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD");
            stb.append(" )");

            stb.append(", CHAIR_A AS(");
            stb.append("   SELECT  ");
            stb.append("   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            stb.append("            T2.SUBCLASSCD AS SUBCLASSCD");
            stb.append("   FROM    CHAIR_STD_DAT T1, CHAIR_DAT T2");
            stb.append("   WHERE   T1.SCHREGNO = ?");
            stb.append("       AND T1.YEAR = '" + param._year + "'");
            stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("       AND T2.YEAR  = '" + param._year + "'");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER");
            stb.append("       AND T2.CHAIRCD = T1.CHAIRCD");
            stb.append("       AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "' OR CLASSCD BETWEEN '91' AND '92')");
            stb.append("   GROUP BY ");
            stb.append("   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            stb.append("             T2.SUBCLASSCD");
            stb.append(" )");

            stb.append(", SUBCLASSNUM AS(");
            stb.append("   SELECT  SUM(CASE WHEN ");
            stb.append(" S1.CLASSCD = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
            stb.append("         , SUM(CASE WHEN ");
            stb.append(" S1.CLASSCD != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
            stb.append("         , SUM(CASE WHEN S1.CLASSCD >= '90' THEN 1 ELSE NULL END) AS OVER90 ");
            stb.append("   FROM    CHAIR_A S1");
            if ("1".equals(param._useClassDetailDat)) {
                stb.append(" LEFT JOIN (SELECT CLASSCD || '-' || SCHOOL_KIND AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND CLASS_SEQ = '003') T1 ON T1.NAMECD2 = ");
                stb.append("   S1.CLASSCD || '-' || S1.SCHOOL_KIND ");
            } else {
                stb.append(" LEFT JOIN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1='" + param._d008Namecd1 + "') T1 ON T1.NAMECD2 = ");
                stb.append(" S1.CLASSCD ");
            }
            stb.append("), QUALIFIED AS(");
            stb.append("   SELECT ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
            stb.append("       T1.SUBCLASSCD, ");
            stb.append("       SUM(T1.CREDITS) AS CREDITS ");
            stb.append("   FROM ");
            stb.append("       SCHREG_QUALIFIED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.CREDITS IS NOT NULL ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.CLASSCD, ");
            stb.append("       T1.SCHOOL_KIND, ");
            stb.append("       T1.CURRICULUM_CD, ");
            stb.append("       T1.SUBCLASSCD ");
            stb.append(" )");

            stb.append(" SELECT  T2.SUBCLASSCD, T7.CLASSABBV, VALUE(T4.SUBCLASSORDERNAME2,T4.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       , T6.CREDITS, T6.NAMESPARE1 ");
            stb.append("       , CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL AND T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 2"); // 元科目かつ先科目
            stb.append("              WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN 9"); // 先科目
            stb.append("              WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 1"); // 元科目
            stb.append("              ELSE 0 END AS REPLACEFLG");
            stb.append("       , T5.ATTEND_SUBCLASSCD ");
            stb.append("       , T9.COMBINED_SUBCLASSCD ");
            stb.append("       , T9.PRINT_FLG");
            stb.append("       , N1.NAMECD2 AS NUM90_OTHER");
            stb.append("       , (SELECT NUM90 FROM SUBCLASSNUM) AS NUM90");
            stb.append("       , (SELECT NUMTOTAL FROM SUBCLASSNUM) AS NUMTOTAL");
            stb.append("       , (SELECT OVER90 FROM SUBCLASSNUM) AS OVER90 ");
            stb.append("       , CASE WHEN '90' < T2.CLASSCD THEN 4 ");
            stb.append("              WHEN '90' = T2.CLASSCD THEN 3 ");
            stb.append("              WHEN N1.NAMECD2 IS NOT NULL THEN 2 ");
            stb.append("              ELSE 1 END AS ORDER0");
            stb.append("       , CASE WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN T9.COMBINED_SUBCLASSCD ELSE T2.SUBCLASSCD END AS ORDER1");
            stb.append("       , CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN 1 WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 2 ELSE 0 END AS ORDER2");
            stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
            stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
            stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
            stb.append("       , REC_SCORE.COMP_CREDIT ");
            if ("1".equals(param._zouka)) {
                stb.append("       , CASE WHEN QUAL.CREDITS IS NOT NULL THEN QUAL.CREDITS + REC_SCORE.GET_CREDIT ELSE REC_SCORE.GET_CREDIT END AS GET_CREDIT ");
            } else {
                stb.append("       , REC_SCORE.GET_CREDIT AS GET_CREDIT ");
            }
            stb.append("       , REC_SCORE.ADD_CREDIT ");
            stb.append(" FROM    CHAIR_A T2");
            stb.append(" LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T7 ON T7.CLASSCD || '-' || T7.SCHOOL_KIND = T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
            stb.append(" LEFT JOIN SUBCLASS_CREDITS T6 ON T6.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5 ON T5.COMBINED_SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN ATTEND_SUBCLASSCD T9 ON T9.ATTEND_SUBCLASSCD = T2.SUBCLASSCD");
            if ("1".equals(param._useClassDetailDat)) {
                stb.append(" LEFT JOIN (SELECT CLASSCD || '-' || SCHOOL_KIND AS NAMECD2 FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '" + param._year + "' AND CLASS_SEQ = '003') N1 ON N1.NAMECD2 = ");
                stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
            } else {
                stb.append(" LEFT JOIN NAME_MST N1 ON N1.NAMECD1='" + param._d008Namecd1 + "' AND N1.NAMECD2 = ");
                stb.append("    T2.CLASSCD ");
            }
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC_SCORE ON REC_SCORE.YEAR = '" + param._year + "' ");
            stb.append("       AND REC_SCORE.SCHREGNO = ? AND ");
            stb.append("    REC_SCORE.CLASSCD || '-' || REC_SCORE.SCHOOL_KIND || '-' || REC_SCORE.CURRICULUM_CD || '-' || ");
            stb.append("       REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD = '" + Param._99900 + "' ");
            stb.append(" LEFT JOIN QUALIFIED QUAL ON QUAL.YEAR = '" + param._year + "' ");
            stb.append("       AND QUAL.SCHREGNO = ? AND ");
            stb.append("    QUAL.CLASSCD || '-' || QUAL.SCHOOL_KIND || '-' || QUAL.CURRICULUM_CD || '-' || QUAL.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" ORDER BY ORDER0, ORDER1, ORDER2");
            return stb.toString();
        }

        /**
         * 各生徒に１日ごと、科目ごとの出欠データをセットする
         * @param db2
         * @param students
         * @param param
         */
        private static void setAttendData(final DB2UDB db2, final List students, final Param param) {

            PreparedStatement ps = null;
            ResultSet rs = null;

            final String[] targetSemesters;
            targetSemesters = new String[]{"1", "2", "3", Param.SEMEALL};
            for (int i = 0; i < targetSemesters.length; i++) {
                try {
                final String semester = targetSemesters[i];
                // log.debug(" semester = " + semester + " , " + param._semesterMap.get(semester));
                final Semester semesS = (Semester) param._semesterMap.get(semester);
                if (null == semesS) {
                    log.debug(" 対象学期がありません。:" + semester);
                    continue;
                } else {
                    log.debug(" 対象学期:" + semester);
                }
                final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;
                if (Param.SEMEALL.equals(semester)) {
                    param._attendParamMap.put("sdate", null);
                } else {
                    param._attendParamMap.put("sdate", semesS._sdate);
                }
                param._attendParamMap.put("schregno", "?");
                try {

                    String sql;
                    sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            semesS._cd,
                            semesS._sdate,
                            edate,
                            param._attendParamMap
                    );

                    //log.debug(" attend semes sql = " + sql);
                    ps = db2.prepareStatement(sql);

                    for (final Iterator stit = students.iterator(); stit.hasNext();) {
                        final Student student = (Student) stit.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            if (!"9".equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final int lesson = rs.getInt("LESSON");
                            final int mourning = rs.getInt("MOURNING");
                            final int suspend = rs.getInt("SUSPEND");
                            final int koudome = rs.getInt("KOUDOME");
                            final int virus = rs.getInt("VIRUS");
                            final int abroad = rs.getInt("TRANSFER_DATE");
                            final int mlesson = rs.getInt("MLESSON");
                            final int absence = rs.getInt("SICK");
                            final int attend = rs.getInt("PRESENT");
                            final int late = rs.getInt("LATE");
                            final int early = rs.getInt("EARLY");

                            final Attendance attendance = new Attendance(lesson, mourning, suspend, koudome, virus, abroad, mlesson, absence, attend, late, early);
                            // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                            student.setAttendance(semester, attendance);
                        }
                        DbUtils.closeQuietly(rs);
                    }

                } catch (SQLException e) {
                    log.error("sql exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }

                String sql = null;
                try {
                    sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            semesS._cd,
                            null,
                            edate,
                            param._attendParamMap
                    );

                    // log.debug(" attend subclass sql = " + sql);
                    ps = db2.prepareStatement(sql);
                    for (final Iterator stit = students.iterator(); stit.hasNext();) {
                        final Student student = (Student) stit.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            if (!"9".equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclassCd = rs.getString("SUBCLASSCD");

                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal absent = rs.getBigDecimal("ABSENT");
                            final BigDecimal suspend = rs.getBigDecimal("SUSPEND");
                            final BigDecimal koudome = rs.getBigDecimal("KOUDOME");
                            final BigDecimal virus = rs.getBigDecimal("VIRUS");
                            final BigDecimal mourning = rs.getBigDecimal("MOURNING");
                            final BigDecimal late = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("LATE") : rs.getBigDecimal("LATE2");
                            final BigDecimal early = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("EARLY") : rs.getBigDecimal("EARLY2");
                            final BigDecimal rawReplacedAbsence = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedAbsence = rs.getBigDecimal("REPLACED_SICK");

                            final SubclassAttendance sa = new SubclassAttendance(lesson, rawSick, sick, absent, suspend, koudome, virus, mourning, late, early, rawReplacedAbsence, replacedAbsence);

                            student.setSubclassAttendance(subclassCd, semester, sa);

                            // 使用するのは学年末のみ。学期ごと欠課数オーバーなし
                            final String absenceHigh = StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "0");
                            final String getAbsenceHigh = StringUtils.defaultString(rs.getString("GET_ABSENCE_HIGH"), "0");
                            student._subclassAbsenceHigh.put(rs.getString("SUBCLASSCD"), new AbsenceHigh(absenceHigh, getAbsenceHigh));

    //                        log.debug("   schregno = " + student._schregno + " , subclcasscd = " + subclassCd + " , subclass attendance = " + sa);
                        }
                    DbUtils.closeQuietly(rs);
                    }
                } catch (SQLException e) {
                    log.debug("sql exception! sql = " + sql, e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }

                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
        }

        private static String sqlRegdData (final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.YEAR, T1.SCHREGNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            if (param._isFi) {
                stb.append(    "    ,T1.GRADE, T1.ATTENDNO, HDAT.HR_NAME, HDAT.HR_NAMEABBV, HDAT.TR_CD1 ");
                stb.append(    "FROM    SCHREG_REGD_FI_DAT T1 ");
                stb.append("        INNER JOIN SCHREG_REGD_FI_HDAT HDAT ON HDAT.YEAR     = T1.YEAR ");
                stb.append("                                           AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("                                           AND HDAT.GRADE || HDAT.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
            } else if (param._isGhr) {
                stb.append(    "    ,T1.GRADE, RGHR.GHR_ATTENDNO AS ATTENDNO, HDAT.GHR_NAME AS HR_NAME, HDAT.GHR_NAMEABBV AS HR_NAMEABBV, HDAT.TR_CD1 ");
                stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
                stb.append("        INNER JOIN SCHREG_REGD_GHR_DAT RGHR ON RGHR.SCHREGNO = T1.SCHREGNO ");
                stb.append("                                           AND RGHR.YEAR     = T1.YEAR ");
                stb.append("                                           AND RGHR.SEMESTER = T1.SEMESTER ");
                stb.append("        INNER JOIN SCHREG_REGD_GHR_HDAT HDAT ON HDAT.YEAR     = T1.YEAR ");
                stb.append("                                            AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("                                            AND HDAT.GHR_CD   = RGHR.GHR_CD ");
            } else if (param._isGakunenKongou) {
                stb.append(    "    ,T1.GRADE, T1.ATTENDNO, HDAT.HR_CLASS_NAME1 AS HR_NAME, HDAT.HR_NAMEABBV, HDAT.STAFFCD AS TR_CD1 ");
                stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
                stb.append("        INNER JOIN V_STAFF_HR_DAT HDAT ON HDAT.YEAR     = T1.YEAR ");
                stb.append("                                      AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("                                      AND HDAT.GRADE || HDAT.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
            } else {
                stb.append(    "    ,T1.GRADE, T1.ATTENDNO, HDAT.HR_NAME, HDAT.HR_NAMEABBV, HDAT.TR_CD1 ");
                stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
                stb.append("        INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = T1.YEAR ");
                stb.append("                                        AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("                                        AND HDAT.GRADE || HDAT.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
            }
            stb.append("        INNER JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ");
            stb.append("                                  AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");

            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.HR_NAME, T1.HR_NAMEABBV, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append(        "T1.GRADE, GDAT.GRADE_NAME1, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("       ,CASE WHEN STUP.STAFFCD IS NOT NULL THEN VALUE(STFF.STAFFNAME_REAL, STFF.STAFFNAME) ELSE STFF.STAFFNAME END AS TR_NAME1 ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = T1.YEAR ");
            stb.append("                                       AND GDAT.GRADE = T1.GRADE ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '03' ");
            stb.append("        LEFT JOIN STAFF_MST STFF ON STFF.STAFFCD = T1.TR_CD1 ");
            stb.append("        LEFT JOIN STAFF_NAME_SETUP_DAT STUP ON STUP.YEAR = T1.YEAR AND STUP.STAFFCD = STFF.STAFFCD AND STUP.DIV = '03' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {

        public static final String GROUP_LHR = "001";
        public static final String GROUP_ASS = "002";
        public static final String GROUP_SHR = "004";
        public static final String GROUP_ALL = "999";

        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 交止 */
        final int _koudome;
        /** 出停伝染病 */
        final int _virus;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 公欠 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _leave;

        public Attendance() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int koudome,
                final int virus,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int leave
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _koudome = koudome;
            _virus = virus;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _leave = leave;
        }

        public String toString() {
            return "[lesson=" + _lesson +
            ",mlesson=" + _mlesson +
            ",mourning=" + _mourning +
            ",suspend=" + _suspend +
            ",abroad=" + _abroad +
            ",absence=" + _absence +
            ",attend=" + _attend +
            ",late=" + _late +
            ",leave=" + _leave;
        }
    }

    /**
     * 科目ごとの出欠データ
     */
    private static class SubclassAttendance {
        final int _lesson;
        /** 換算前の欠席数 */
        final BigDecimal _rawSick;
        /** 換算後の欠課数 */
        final BigDecimal _sick;
        /** 公欠 */
        final int _absent;
        /** 出停 */
        final int _suspend;
        /** 交止 */
        final int _koudome;
        /** 出停伝染病 */
        final int _virus;
        /** 忌引 */
        final int _mourning;
        /** 遅刻早退 */
        final int _lateearly;
        /** 換算前の合併欠席数 */
        final BigDecimal _rawReplacedSick;
        /** 換算後の合併欠課数 */
        final BigDecimal _replacedSick;

        String _subclassCd = null;

        public SubclassAttendance(
                final BigDecimal lesson,
                final BigDecimal rawSick,
                final BigDecimal sick,
                final BigDecimal absent,
                final BigDecimal suspend,
                final BigDecimal koudome,
                final BigDecimal virus,
                final BigDecimal mourning,
                final BigDecimal late,
                final BigDecimal early,
                final BigDecimal rawReplacedSick,
                final BigDecimal replacedSick) {
            _lesson = lesson.intValue();
            _rawSick = rawSick;
            _sick = sick;
            _absent = absent.intValue();
            _suspend = suspend.intValue();
            _koudome = koudome.intValue();
            _virus = virus.intValue();
            _mourning = mourning.intValue();
            _lateearly = late.add(early).intValue();
            _rawReplacedSick = rawReplacedSick;
            _replacedSick = replacedSick;
        }

        public String getRawSick() {
            return (_rawSick == null) ? null : formatBigDecimal(_rawSick);
        }

        public String getSick() {
            return (_sick == null) ? null : formatBigDecimal(_sick);
        }

        public String getRawReplacedSick() {
            return (_rawReplacedSick == null) ? null : formatBigDecimal(_rawReplacedSick);
        }

        public String getReplacedSick() {
            return (_replacedSick == null) ? null : formatBigDecimal(_replacedSick);
        }

        public String getLesson() {
            return String.valueOf(_lesson);
        }

        private String getKekkaString() {
            return "lesson = " + _lesson + " , rawSick = " + _rawSick + ", sick = " + _sick + " , absent = " + _absent + " , susmour = " + _suspend + " , " + _mourning +
            " , lateearly = " + _lateearly + ((_replacedSick.intValue() != 0) ? " , replacedSick = " + _replacedSick : "") + ((_rawReplacedSick.intValue() != 0) ? " , rawReplacedSick = " + _rawReplacedSick : "");
        }

        public String toString() {
            return getKekkaString();
        }

        public String getLateEarly() {
            return formatInt(_lateearly);
        }

        public String getKoketsu() {
            return formatInt(_absent);
        }

        public String getMourning() {
            return formatInt(_mourning);
        }

        public String getSuspend() {
            return formatInt(_suspend + _koudome + _virus);
        }

        public String getMourningSuspend() {
            return formatInt(_mourning + _suspend + _koudome + _virus);
        }

        private String formatInt(int n) {
            return n == 0 ? "" : String.valueOf(n);
        }

        private String formatBigDecimal(BigDecimal n) {
            return n == null ? null : (n.intValue() == 0) ? formatInt(0) : String.valueOf(n);
        }
    }

    /**
     * 生徒の科目ごとの成績・出欠データ
     */
    private static class Subclass {
        final String _schregno;
        final String _subclassCd;
        final String _classCd;

        final Map _testKindTableMap = new HashMap();

        final Map _attendSubclassMap = new HashMap();

        public Subclass(
                final String schregno,
                final String subclassCd
        ) {
            _schregno = schregno;
            _subclassCd = subclassCd;
            _classCd = subclassCd.substring(0, 2);
        }

        private Map findTestKindTableMap(final String testKindCd) {
            if (!_testKindTableMap.containsKey(testKindCd)) {
                _testKindTableMap.put(testKindCd, new HashMap());
            }
            return (Map) _testKindTableMap.get(testKindCd);
        }

        public void setScoreValue(final String testKindCd, final String tableDiv, final ScoreValue sv) {
            findTestKindTableMap(testKindCd).put(tableDiv, sv);
        }

        public ScoreValue getScoreValue(final String testKindCd, final String tableDiv) {
            return (ScoreValue) findTestKindTableMap(testKindCd).get(tableDiv);
        }

        public ScoreValue getScoreValue(final String testKindCd) {
            return (ScoreValue) findTestKindTableMap(testKindCd).get(Param.REC);
        }

        /**
         * 学期ごとの科目出欠をセットする
         * @param semester 学期
         * @param sa 科目出欠
         */
        public void setAttendance(final String semester, final SubclassAttendance sa) {
            _attendSubclassMap.put(semester, sa);
        }

        /**
         * 指定学期の科目出欠を得る
         * @param semester 指定学期
         * @return 指定学期の科目出欠
         */
        public SubclassAttendance getAttendance(final String semester) {
            return (SubclassAttendance) _attendSubclassMap.get(semester);
        }

        public String toString() {
            return _schregno + " : " + _subclassCd + ":" + _testKindTableMap;
        }
    }

    /**
     * 欠課数上限値
     */
    private static class AbsenceHigh {
        /** 履修上限 */
        final String _compAbsenceHigh;
        /** 修得上限 */
        final String _getAbsenceHigh;

        public AbsenceHigh(final String absenceHigh, final String getAbsenceHigh) {
            _compAbsenceHigh = absenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
        }

        /**
         * 履修上限を超えているか
         * @param kekka 欠課数
         * @return
         */
        public boolean isRishuOver(final String kekka) {
            return isOver(kekka, _compAbsenceHigh);
        }

        /**
         * 修得上限を超えているか
         * @param kekka 欠課数
         * @return
         */
        public boolean isShutokuOver(final String kekka) {
            return isOver(kekka, _getAbsenceHigh);
        }

        private static boolean isOver(final String kekka, final String absenceHigh) {
            if (null == kekka || !NumberUtils.isNumber(kekka) || Double.parseDouble(kekka) == 0) {
                return false;
            }
            return absenceHigh == null || Double.parseDouble(absenceHigh) < Double.parseDouble(kekka);
        }

        public String toString() {
            return " 履修上限値" + _compAbsenceHigh + " , 修得上限値" + _getAbsenceHigh;
        }
    }

    private static class ScoreValue {
        final String _testKindcd;
        final String _subclassCd;
        final String _value;

        public ScoreValue(
                final String testKindcd,
                final String subclassCd,
                final String value) {
            _testKindcd = testKindcd;
            _subclassCd = subclassCd;
            _value = value;
        }

        public String toString() {
            return "[value = " + _value + "]";
        }
    }

    /**
     * 序列データ
     */
    private static class ScoreRank {
        final String _tableDiv;
        final String _testKindcd;
        final String _totalScore;
        final String _avgScore;
        final String _courseRank;
        final String _courseAvgRank;
        final String _courseCount;
        final String _classRank;
        final String _classAvgRank;
        final String _classCount;

        public ScoreRank(final String tableDiv,
                final String testKindCd,
                final String totalScore,
                final String avgScore,
                final String courseRank,
                final String courseAvgRank,
                final String courseCount,
                final String classRank,
                final String classAvgRank,
                final String classCount) {
            _tableDiv = tableDiv;
            _testKindcd = testKindCd;
            _totalScore = totalScore;
            _avgScore = avgScore;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseCount = courseCount;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classCount = classCount;
        }

        public String toString() {
            return " ScoreRank " + _testKindcd + " (" + _totalScore + " , " + _avgScore + ") " +
            "[" + _courseRank + " (AVE " + _courseAvgRank +  ") /" + _courseCount + " , " +
            _classRank + " (AVE " + _classAvgRank +  ") / " + _classCount + "] (" + _tableDiv + ")";
        }
    }

    /**
     * 学期
     */
    private static class Semester {
        public final String _cd;
        public final String _name;
        public final String _sdate;
        public final String _edate;
        public Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }

        public String toString() {
            return "(" + _name + " [" + _sdate + "," + _edate + "])";
        }
    }

    /**
     * 通知書所見
     */
    private static class HReportRemark {
        public final String _semester;
        public final String _specialActRemark;
        public final String _totalStudyTime;
        public final String _communication;
        public final String _remark1;
        public final String _remark3;
        public final String _attendrecRemark;
        public final String _club;
        public final String _committee;
        public final String _other;

        public HReportRemark(final String semester,
                final String specialActRemark,
                final String totalStudyTime,
                final String communication,
                final String remark1,
                final String remark3,
                final String attendrecRemark,
                final String club,
                final String committee,
                final String other) {
            _semester = semester;
            _specialActRemark = specialActRemark;
            _totalStudyTime = totalStudyTime;
            _communication = communication;
            _remark1 = remark1;
            _remark3 = remark3;
            _attendrecRemark = attendrecRemark;
            _club      = club;
            _committee = committee;
            _other     = other;
        }

        // -----

        /**
         * 通知書所見をセットする
         * @param db2
         * @param students
         * @param param
         */
        public static void setHReportRemark(final DB2UDB db2, final List students, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     HREP.SEMESTER ");
            stb.append("     ,HREP.SPECIALACTREMARK ");
            stb.append("     ,HREP.TOTALSTUDYTIME ");
            stb.append("     ,HREP.COMMUNICATION");
            stb.append("     ,HREP.REMARK1");
            stb.append("     ,HREP.REMARK3");
            stb.append("     ,HREP.ATTENDREC_REMARK");
            stb.append("     ,COMI.REMARK1 AS COMMITTEE ");
            stb.append("     ,CLUB.REMARK1 AS CLUB ");
            stb.append("     ,OTHE.REMARK1 AS OTHER ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT HREP ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT COMI ON COMI.YEAR     = HREP.YEAR ");
            stb.append("                                            AND COMI.SEMESTER = HREP.SEMESTER ");
            stb.append("                                            AND COMI.SCHREGNO = HREP.SCHREGNO ");
            stb.append("                                            AND COMI.DIV      = '01' ");
            stb.append("                                            AND COMI.CODE     = '01' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT CLUB ON CLUB.YEAR     = HREP.YEAR ");
            stb.append("                                            AND CLUB.SEMESTER = HREP.SEMESTER ");
            stb.append("                                            AND CLUB.SCHREGNO = HREP.SCHREGNO ");
            stb.append("                                            AND CLUB.DIV      = '01' ");
            stb.append("                                            AND CLUB.CODE     = '02' ");
            stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT OTHE ON OTHE.YEAR     = HREP.YEAR ");
            stb.append("                                            AND OTHE.SEMESTER = HREP.SEMESTER ");
            stb.append("                                            AND OTHE.SCHREGNO = HREP.SCHREGNO ");
            stb.append("                                            AND OTHE.DIV      = '01' ");
            stb.append("                                            AND OTHE.CODE     = '03' ");
            stb.append(" WHERE ");
            stb.append("     HREP.YEAR = '" + param._year + "' ");
            stb.append("     AND HREP.SEMESTER <> '" + Param.SEMEALL + "' ");
            stb.append("     AND HREP.SCHREGNO = ? ");

            final String sql = stb.toString();
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    student._hReportRemarks.clear();

                    final Map hreportRemarks = new HashMap();

                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String specialActRemark = rs.getString("SPECIALACTREMARK");
                        final String totalStudyTime = rs.getString("TOTALSTUDYTIME");
                        final String communication = rs.getString("COMMUNICATION");
                        final String remark1 = rs.getString("REMARK1");
                        final String remark3 = rs.getString("REMARK3");
                        final String attendrecRemark = rs.getString("ATTENDREC_REMARK");
                        final String club      = rs.getString("CLUB");
                        final String committee = rs.getString("COMMITTEE");
                        final String other     = rs.getString("OTHER");

                        final HReportRemark hreportremark = new HReportRemark(semester, specialActRemark, totalStudyTime, communication, remark1, remark3, attendrecRemark, club, committee ,other);
                        hreportRemarks.put(semester, hreportremark);
                    }
                    student._hReportRemarks.putAll(hreportRemarks);
                }

            } catch (SQLException e) {
                log.error("sql exception! :" + sql, e);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static class SubclassInfo {
        final static int GAPPEI_NASI = 0;
        final static int GAPPEI_MOTO = 1;
        final static int GAPPEI_MOTO_SAKI = 2;
        final static int GAPPEI_SAKI = 9;
        final static String GAPPEI_TANNI_KOTEI = "1";
        final static String GAPPEI_TANNI_KASAN = "2";

        final String _classabbv;
        final String _subclassname;
        final String _credits;
        final String _compCredit;
        final String _getCredit;
        final String _subclassCd;
        final String _attendSubclasscd;
        final String _combinedSubclasscd;
        final String _namespare1;
        final Subclass _subclass;

        final int _replaceflg; // 0:合併設定なし、1:元科目、2:先科目かつ元科目、9:先科目

        final String _calculateCreditFlg;

        final String _num90;
        final String _num90Other;
        final String _over90;

        public SubclassInfo(
                final String classabbv,
                final String subclassname,
                final String credits,
                final String compCredit,
                final String getCredit,
                final String subclassCd,
                final String attendSubclasscd,
                final String combinedSubclasscd,
                final String namespare1,
                final Subclass subclass,
                final int replaceFlg,
                final String calculateCreditFlg,
                final String num90,
                final String num90Other,
                final String over90) {
            _classabbv = classabbv;
            _subclassname = subclassname;
            _credits = credits;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _subclassCd = subclassCd;
            _attendSubclasscd = attendSubclasscd;
            _combinedSubclasscd = combinedSubclasscd;
            _namespare1 = namespare1;
            _subclass = subclass;

            _replaceflg = replaceFlg;
            _calculateCreditFlg = calculateCreditFlg;
            _num90 = num90;
            _num90Other = num90Other;
            _over90 = over90;
        }

        public boolean isPrintCreditMstCredit() {
            if (_credits == null || GAPPEI_SAKI == _replaceflg && GAPPEI_TANNI_KASAN.equals(_calculateCreditFlg)) {
                return false;
            }
            return true;
        }

        public boolean isNotPrintSubclassList(final Param param) {
            boolean rtn = false;

            if (GAPPEI_MOTO == _replaceflg && param._isNoPrintMoto) { rtn = true; }

            if (param.isD026ContainSubclasscd(_subclassCd)) { rtn = true; }

            return rtn;
        }
    }

    private static int getMS932ByteLength(final String s) {
        int len = 0;
        try {
            if (null != s) {
                len = s.getBytes("MS932").length;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return len;
    }

    private static class Form {

        protected Param _param;
        protected Vrw32alpWrap _svf;

        protected Map _amikakeMap;

        public Form(Vrw32alpWrap svf, Param param) {
            this._svf = svf;
            this._param = param;
        }

        // 空白の画像を表示して欄を非表示
        private void whitespace(final Vrw32alp svf, final String field) {
            if (null != _param._whitespaceImagePath) {
                svf.VrsOut(field, _param._whitespaceImagePath);
            }
        }

        protected int blankRecord (
                final Vrw32alpWrap svf,
                final String num90,
                final int line,
                final int maxLine,
                final String field
        ) {
            int intnum90 = parseInt(num90);
            if (0 == intnum90) return 0;
            int i = line + 1;
            i = (i % maxLine == 0)? maxLine: i % maxLine;
            if (1 == i) return 0;
            svf.VrsOut(field, " ");
            svf.VrEndRecord();
            return 1;
        }

       protected int blankToBottomRecord (
               final Vrw32alpWrap svf,
               final String over90,
               final int line,
               final int maxLine,
               final String field
               ) {
           int intOver90 = parseInt(over90);
           if (0 == intOver90) return 0;
           int i = line + 1;
           if (i + intOver90 >= maxLine) return 0;
           i = (i % maxLine == 0)? maxLine: i % maxLine;
           if (1 == i) return 0;
           int retCnt = 0;
           int setNum = maxLine - i - intOver90;
//           setNum += (0 > setNum) ? maxLine: 0;
           for (int j = 0; j <= setNum; j++) {
               svf.VrsOut(field, " ");
               svf.VrEndRecord();
               retCnt++;
           }
           return retCnt;
       }

        protected void setAbsenceFieldAttribute(
                final Vrw32alpWrap svf,
                final AbsenceHigh absenceHigh,
                final String absent1,
                final String fieldKekka,
                final String fieldLateEarly
        ) {
        }

        private void svfFieldAttribute_CLASS (
                final Vrw32alpWrap svf,
                final String field,
                final String name,
                final int ln
        ) {
            svf.VrsOut(field,  name );
        }

        private void svfFieldAttribute_SUBCLASS (
                final Vrw32alpWrap svf,
                final String field,
                final String name,
                final int ln
        ) {
            svf.VrsOut(field + (name != null && name.length() > 13 ? "2" : "1"),  name );
        }

        private Map getSubclassInfoMap(final List subclassInfoList) {
            final Map map = new HashMap();
            if (null != subclassInfoList) {
                for (Iterator it = subclassInfoList.iterator(); it.hasNext();) {
                    final SubclassInfo si = (SubclassInfo) it.next();
                    map.put(si._subclassCd, si);
                }
            }
            return map;
        }

        private void printScoreRank(final Vrw32alpWrap svf, final Student student) {
            final String[] semTestKindCds = _param.getPrintTestKindCds();
            for (int k = 0; k < semTestKindCds.length; k++) {
                final ScoreRank sr = student.getScoreRank(semTestKindCds[k]);
                if (sr != null && NumberUtils.isNumber(sr._avgScore)) {
                    // log.debug(" score rank = " + sr);
                    String avgScore = new BigDecimal(sr._avgScore).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    svf.VrsOut("AVE_RATE" + getFieldColumnTestKind(semTestKindCds[k]), avgScore);
                }
            }
        }

        /**
         * @param svf
         * @param si
         * @param student
         * @param replaceflg
         * @param subclass
         * @param sa
         */
        protected void printSubclassAttendance(final Vrw32alpWrap svf,
                final SubclassInfo si,
                final Student student,
                final int replaceflg,
                Subclass subclass,
                SubclassAttendance sa) {

            if (sa == null) {
                return;
            }
            String sick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getReplacedSick() : sa.getSick();

            String _fieldKamokuKetsuji = "ABSENCE";
            String _fieldKamokuLateEarly = "LATE";

            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(subclass._subclassCd), sick, _fieldKamokuKetsuji, _fieldKamokuLateEarly);

            String rawSick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getRawReplacedSick() : sa.getRawSick();

            if (PATTERN_C.equals(_param._pattern)) {
                svf.VrsOut(_fieldKamokuKetsuji, rawSick == null || "".equals(rawSick) ? "0" : String.valueOf(rawSick));
            } else {
                svf.VrsOut(_fieldKamokuKetsuji, rawSick == null ? "" : String.valueOf(rawSick));
            }

            svf.VrsOut(_fieldKamokuLateEarly, sa.getLateEarly());
            svf.VrsOut("PUB_ABSENCE", sa.getKoketsu());
            svf.VrsOut("MOURNING", sa.getMourningSuspend());
        }

        protected boolean isSubclassCdSogotekinaGakushunoJikan(final Subclass subclass) {
            if (null == subclass || null == subclass._subclassCd) {
                return false;
            }
            String subclasscd = subclass._subclassCd;
            if (StringUtils.split(subclasscd).length == 4) {
                subclasscd = StringUtils.split(subclasscd)[3];
            }
            return (Param.subclassCdSogotekinaGakushunoJikan.equals(subclasscd));
        }

        protected boolean is90(final String  subclassCd) {
            String classcd;
            if (StringUtils.split(subclassCd).length == 4) {
                classcd = StringUtils.split(subclassCd)[0];
            } else {
                classcd = subclassCd.substring(0, 2);
            }
            return KNJDefineSchool.subject_T.equals(classcd);
        }

        protected int parseInt(final String str) {
            if (null == str || StringUtils.isEmpty(str) || !StringUtils.isNumeric(str)) {
                return 0;
            }
            return Integer.parseInt(str);
        }


        protected boolean isCharacter (final String str) {
            return !StringUtils.isEmpty(str) && !StringUtils.isNumeric(str);
        }

        protected String zeroBlank(int n) {
            return (n == 0) ? "" : String.valueOf(n);
        }

        protected int getFieldColumnSemester(String semester) {
            final String[] semesters = _param.getTargetSemester();
            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equals(semester)) {
                    return (i + 1);
                }
            }
            return 0;
        }

        protected int getFieldColumnTestKind(String semTestKindCd) {
            final String[] testKindCds = _param.getPrintTestKindCds();
            for (int i = 0; i < testKindCds.length; i++) {
                if (testKindCds[i].equals(semTestKindCd)) {
                    return (i + 1);
                }
            }
            return 0;
        }

        protected static String[] get_token(String strx,int f_len,int f_cnt) {
            final String[] token = KNJ_EditEdit.get_token(strx, f_len, f_cnt);
            if (null == token) {
                return new String[]{};
            }
            return token;
        }

        private static final int MAX_RECORD = 17;

        public void print(final Student student) {

            final String FORM1 = PATTERN_A.equals(_param._pattern) ? "KNJD181H_1.frm": "KNJD181H_1_2.frm";
            _svf.VrSetForm(FORM1, 1);
            printHyoshi(_svf, student);
            _svf.VrEndPage();

            String formName = "";
            if (PATTERN_C.equals(_param._pattern)) {
                final String courseMajor = student._courseCd + student._majorCd;
                if (SENKOUKA.equals(courseMajor)) {
                    formName = "KNJD181H_2_3.frm";
                } else if (HOKENRIRYOKA.equals(courseMajor)) {
                    formName = "KNJD181H_2_4.frm";
                } else {
                    formName = "KNJD181H_2_5.frm";
                }
            } else if (PATTERN_B.equals(_param._pattern)) {
                formName = "KNJD181H_2_2.frm";
            } else {
                formName = "KNJD181H_2.frm";
            }
            log.debug(" form = " + formName);
            _svf.VrSetForm(formName, 4);

            printHeader(_svf, student);

            printShoken(student);

            int line = printRecDetail(_svf, student);

            if (line == 0) {
                _svf.VrsOut("CLASS", " ");
            }
            _svf.VrEndRecord();

            for (int l = line % MAX_RECORD; l < MAX_RECORD - 1; l++) {
                _svf.VrsOut("CLASS", " ");
                _svf.VrEndRecord();
            }
            line ++;
        }

        public void printShoken(final Student student) {

            final String[] dispsem = _param.getTargetSemester();
            if (PATTERN_B.equals(_param._pattern) || PATTERN_C.equals(_param._pattern)) {
                final String setSem = Param.SEMEALL.equals(_param._semester) ? _param._maxSemester: _param._semester;
                HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(setSem);
                if (null != reportRemark) {
                    // 自立活動
                    String[] totalStudy = get_token(reportRemark._remark3, 80, 3);
                    for (int i = 0; i < totalStudy.length; i++) {
                        _svf.VrsOut("TOTAL_STUDY" + (i + 1), totalStudy[i]);
                    }
                    // 総合的な学習の時間
                    final String[] spContent = get_token(reportRemark._totalStudyTime, 80, 3);
                    for (int i = 0; i < spContent.length; i++) {
                        _svf.VrsOut("SP_CONENT" + (i + 1), spContent[i]);
                    }
                }
            }

            // 学期毎にセット
            final String ttlStr = (checkChgTimingYear() ? "総合的な探究の時間" : "総合的な学習の時間");
            _svf.VrsOut("SP_NAME", ttlStr);
            for (int k = 0; k < dispsem.length; k++) {
                final String semester = dispsem[k];
                HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(semester);
                if (null != reportRemark) {

                    // 学習内容
                    final String[] spContent = get_token(reportRemark._totalStudyTime, 32, 3);
                    for (int i = 0; i < spContent.length; i++) {
                        _svf.VrsOutn("SP_CONENT" + (i + 1), Integer.parseInt(semester), spContent[i]);
                    }
                    // 評価
                    final String[] spEva = get_token(reportRemark._remark1, 32, 5);
                    for (int i = 0; i < spEva.length; i++) {
                        _svf.VrsOutn("SP_EVA" + (i + 1), Integer.parseInt(semester), spEva[i]);
                    }

                    // 自立活動
                    String[] totalStudy = get_token(reportRemark._remark3, 60, 6);
                    for (int i = 0; i < totalStudy.length; i++) {
                        _svf.VrsOutn("TOTAL_STUDY" + (i + 1), Integer.parseInt(semester), totalStudy[i]);
                    }

                    // 特別活動
                    if (PATTERN_B.equals(_param._pattern) || PATTERN_C.equals(_param._pattern)) {
                        // 委員会
                        String[] committee = get_token(reportRemark._committee, 20, 5);
                        for (int i = 0; i < committee.length; i++) {
                            _svf.VrsOutn("COMM" + (i + 1), Integer.parseInt(semester), committee[i]);
                        }
                        // 部活動
                        String[] club = get_token(reportRemark._club, 20, 5);
                        for (int i = 0; i < club.length; i++) {
                            _svf.VrsOutn("CLUB" + (i + 1), Integer.parseInt(semester), club[i]);
                        }
                        // その他
                        String[] etc = get_token(reportRemark._other, 20, 5);
                        for (int i = 0; i < etc.length; i++) {
                            _svf.VrsOutn("ETC" + (i + 1), Integer.parseInt(semester), etc[i]);
                        }

                        // 総合所見/通信欄
                        String[] comm = get_token(reportRemark._communication, 62, 4);
                        for (int i = 0; i < comm.length; i++) {
                            _svf.VrsOutn("OPINION" + (i + 1), Integer.parseInt(semester), comm[i]);
                        }
                    } else {
                        // 通信欄
                        String[] comm = get_token(reportRemark._communication, 60, 5);
                        for (int i = 0; i < comm.length; i++) {
                            _svf.VrsOutn("COMM" + (i + 1), Integer.parseInt(semester), comm[i]);
                        }
                    }
                }
            }
        }

        /**
         *
         *
         */
        private boolean checkChgTimingYear() {
        //2019年度以降かどうか
            boolean over2019 = 2019 <= Integer.parseInt(_param._year) - Integer.parseInt(_param._gradeCd) + 1;
            return over2019;
        }

        /**
         * 表紙の印字
         * @param svf
         * @param student
         */
        private void printHyoshi (
                final Vrw32alpWrap svf,
                final Student student
        ) {
            svf.VrsOut("NENDO", _param._nendo);
            if (PATTERN_B.equals(_param._pattern)) {
                svf.VrsOut("TITLE", "通 知 票");
            } else if (PATTERN_C.equals(_param._pattern)) {
                svf.VrsOut("TITLE", "通 知 表");
            }
            svf.VrsOut("SEMESTER", _param.getSemesterName(_param._semester));
            svf.VrsOut("SCHOOLNAME", _param._certifScholDatSchoolName);
            svf.VrsOut("JOB_NAME1", _param._certifScholDatJobName);
            svf.VrsOut("STAFFNAME1", _param._certifScholDatPrincipalName);
            svf.VrsOut("JOB_NAME2", _param._certifScholDatHrJobName);
            if (student._trName1 != null) {
                svf.VrsOut("TEACHER", "　" + student._trName1);
            }
            svf.VrsOut("COURSE",   student._courseName);
            svf.VrsOut("MAJOR",  student._majorName);
            svf.VrsOut("COURSE_NAME",  student._courseName + "　" + student._majorName);
            if (PATTERN_C.equals(_param._pattern)) {
                svf.VrsOut("HR_NAME", StringUtils.defaultString(student._gradeName1));
            } else {
                svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName));
            }
            svf.VrsOut("ATTENDNO", student._attendNo);
            if (getMS932ByteLength(student._name) > 14) {
                svf.VrsOut("NAME2",    student._name);
            } else {
                svf.VrsOut("NAME",     student._name);
            }

            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }
            if (Param.SEMEALL.equals(_param._semester)) {
                svf.VrsOut("DATE", _param._printDate);
                if (3 == Integer.parseInt(student._grade)) {
                    svf.VrsOut("NOTE1", "卒業を認定します。");
                } else {
                    svf.VrsOut("NOTE1", "進級を認定します。");
                }
                svf.VrsOut("SCHOOLNAME1_2", _param._certifScholDatRemark3);
                svf.VrsOut("JOB", _param._certifScholDatRemark4);
                svf.VrsOut("STAFFNAME1_3", _param._certifScholDatRemark5);
            }
        }

        /**
         * ヘッダを印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alpWrap svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("SCHOOLNAME", _param._certifScholDatSchoolName);
            svf.VrsOut("JOB_NAME1", _param._certifScholDatJobName);
            svf.VrsOut("JOB_NAME2", _param._certifScholDatHrJobName);

            svf.VrsOut("PRESIDENT", _param._certifScholDatPrincipalName);
            if (student._trName1 != null) {
                svf.VrsOut("TEACHER", student._trName1);
            }

            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("COURSE",   student._courseCodeName);
            if (PATTERN_C.equals(_param._pattern)) {
                svf.VrsOut("HR_NAME",  student._gradeName1);
            } else {
                svf.VrsOut("HR_NAME",  student._hrName);
            }
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);
            if (!"1".equals(_param._trPrint)) {
                whitespace(svf, "IMG");
                log.info("TR_PRINT");
            }
        }

        private int printRecDetail (
                final Vrw32alpWrap svf,
                final Student student
        ) {
            boolean hasData = false;
            int i = 0;

            boolean bsubclass90 = false;

            printAttendData(svf, student);
            printScoreRank(svf, student);
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                SubclassInfo info = (SubclassInfo) it.next();

                printAttendData(svf, student);
                printScoreRank(svf, student);

                if (_param.isGakunenmatsu()) {
                    _svf.VrsOut("GETCREDIT", String.valueOf(student._totalCredit));
                }

                if (info.isNotPrintSubclassList(_param)) { continue; }

                if (hasData) {
                    svf.VrEndRecord();
                    i++;
                }

                if (PATTERN_B.equals(_param._pattern) || PATTERN_C.equals(_param._pattern)) {
                    if (90 <= Integer.parseInt(info._subclassCd.substring(0, 2)) && ! bsubclass90) {
                        i += blankToBottomRecord(svf, info._over90, i, MAX_RECORD, "CLASS");
                        bsubclass90 = true;
                    }
                } else {
                    if ((is90(info._subclassCd) || info._num90Other != null) && ! bsubclass90) {
                        i += blankRecord(svf, info._num90, i, MAX_RECORD, "CLASS");
                       bsubclass90 = true;
                    }
                }

                hasData = printSubclassInfo(svf, info, student, (i + 1));
            }
            return i;
        }

        private void printAttendData(final Vrw32alpWrap svf, final Student student) {
            final String nameField2 = "ABSENCE_NAME";
            final String valField2 = "PERIOD_SHR_ABSENCE";

            svf.VrsOut(nameField2, "遅刻・早退数");

            String[] targetSemester = _param.getTargetSemester();
            for (int j = 0; j < targetSemester.length; j++) {
                String semester = targetSemester[j];

                final Attendance sum = student.getAttendance(semester);
                final int k = getFieldColumnSemester(semester);
                svf.VrsOutn("PERIOD_LESSON" , k, String.valueOf(sum._lesson));
                svf.VrsOutn("PERIOD_SUSPEND", k, String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
                svf.VrsOutn("PERIOD_PRESENT", k, String.valueOf(sum._mlesson));
                svf.VrsOutn("PERIOD_ABSENCE", k, String.valueOf(sum._absence));
                svf.VrsOutn("PERIOD_ATTEND" , k, String.valueOf(sum._attend));
                svf.VrsOutn("PERIOD_LATE"   , k, String.valueOf(sum._late + sum._leave));

                svf.VrsOutn(valField2, k, String.valueOf(sum._late + sum._leave));

                //出欠の備考
                HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(semester);
                if (null != reportRemark) {
                    String[] attRmk = get_token(reportRemark._attendrecRemark, 12, 4);
                    for (int i = 0; i < attRmk.length; i++) {
                        svf.VrsOutn("ATTEND_REMARK" + (i + 1), k, attRmk[i]);
                    }
                }
            }
        }

        private boolean printSubclassInfo (
                final Vrw32alpWrap svf,
                final SubclassInfo si,
                final Student student,
                int line
        ) {

            if (MAX_RECORD < line) {
                line = (line % MAX_RECORD == 0)? MAX_RECORD: line % MAX_RECORD;
            }

            if (getMS932ByteLength(si._classabbv) > 6) {
                svf.VrsOut("CLASS", "DUMM" + si._subclassCd);
                svf.VrAttribute("CLASS", "X=10000");
                svf.VrsOut("CLASS2", si._classabbv);
            } else {
                svf.VrsOut("CLASS", si._classabbv);
                svf.VrsOut("CLASS2", "DUMM" + si._subclassCd);
                svf.VrAttribute("CLASS2", "X=10000");
            }
            if (getMS932ByteLength(si._subclassname) > 40) {
            	svf.VrsOut("SUBCLASS3",  si._subclassname);
            } else if (getMS932ByteLength(si._subclassname) > 30) {
            	svf.VrsOut("SUBCLASS2",  si._subclassname);
            } else {
            	svf.VrsOut("SUBCLASS",  si._subclassname);
            }

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOut("CREDIT", si._credits);
            }

            if (PATTERN_B.equals(_param._pattern) || PATTERN_C.equals(_param._pattern)) {
                if (90 <= Integer.parseInt(si._subclassCd.substring(0, 2))) {
                    final String[] semTestKindCds = _param.getPrintTestKindCds();
                    for (int i = 0; i < semTestKindCds.length; i++) {

                        final String semTestKindCd = semTestKindCds[i];
                        final String fieldGrading = "SLASH" + getFieldColumnTestKind(semTestKindCd);
                        svf.VrsOut(fieldGrading, "／");
                    }
                }
            }
            Subclass subclass = student.getSubclass(si._subclassCd);
            if (subclass == null) {
                return true;
            }

            if (SubclassInfo.GAPPEI_NASI == si._replaceflg  || SubclassInfo.GAPPEI_MOTO == si._replaceflg  || SubclassInfo.GAPPEI_SAKI == si._replaceflg) {

                final String[] semTestKindCds = _param.getPrintTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String fieldGrading = "RATE" + getFieldColumnTestKind(semTestKindCd);
                    final ScoreValue sv = subclass.getScoreValue(semTestKindCd);
                    if (sv != null) {
                        log.debug("  subclassCd = " + si._subclassCd + " semTestKindCd = " + semTestKindCd + " "+ sv);

                        if (sv._value != null) {
                            if (isCharacter(sv._value)) {
                                svf.VrAttribute(fieldGrading, "Hensyu=3");
                            }
                            if (!isSubclassCdSogotekinaGakushunoJikan(subclass)) {
                                svf.VrsOut(fieldGrading, sv._value);
                            }
                        }
                    }
                    if (Param._99900.equals(semTestKindCd)) {
                        boolean isPrint = false;
                        if (null != sv && NumberUtils.isDigits(sv._value)) {
                            if (2 <= Integer.parseInt(sv._value) && Integer.parseInt(sv._value) <= 5) {
                                // 評定が 2 ~ 5
                                svf.VrsOut("REMARK", "認定");
                                isPrint = true;
                            } else if (1 == Integer.parseInt(sv._value)) {
                                // 評定が 1
                                svf.VrsOut("REMARK", "不認定");
                                isPrint = true;
                            }
                        }
                        if (!isPrint) {
                            final SubclassAttendance sa = subclass.getAttendance(Param.SEMEALL);
                            final AbsenceHigh ah = student.getAbsenceHigh(subclass._subclassCd);
                            if (null != sa && null != ah && ah.isRishuOver((SubclassInfo.GAPPEI_SAKI == si._replaceflg) ? sa.getReplacedSick() : sa.getSick())) {
                                svf.VrsOut("REMARK", "-"); // 欠課時数オーバー
                            }
                        }
                    }
                }
                printSubclassAttendance(svf, si, student, si._replaceflg, subclass, subclass.getAttendance(Param.SEMEALL));
            }

            return true;
        }
    }

    private static class SchGDat{
        final String _year;
        final String _grade;
        private SchGDat(final String year, final String grade) {
        	_year = year;
        	_grade = grade;
        }
    }
    private static Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal(" $Revision: 74345 $ $Date: 2020-05-15 19:32:44 +0900 (金, 15 5 2020) $");
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    /**
     * パラメータクラス
     */
    private static class Param {

        static final String SEMEALL = "9";
        static final String REC     = "REC  ";
//        static final String REC_V   = "REC_V";
        static final String _39900  = "39900";
        static final String _99900 = "99900";
        static final String _99901 = "99901";
        final String _testcdGakunenHyokaHyotei;

        // 科目コード:総合的な学習の時間
        static final String subclassCdSogotekinaGakushunoJikan = "900400";

        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _hrClassType;
        final String _gakunenKongou;
        final String _grade_hr_class;
        final String _schoolKind;
        final String _maxSemester;

        final String _edate;
        final String _descDate;
        final String _ctrlDate;

        //帳票パターン
        final String _pattern; // 1:Aパターン 2:Bパターン

        /** 担任印出力 */
        final String _trPrint;
        final String _whitespaceImagePath;

        final String[] _dispSemester;
        final String[] _testKindCds;
        final String[] _printTestKindCds;
        final String[] _categorySelected;
        final boolean _useKetten;
        final Integer _ketten;
        final Integer _kettenHyotei;
        final String _nendo;
        final String _printDate;
        final String _gradeCd;

        final String _certifScholDatSchoolName;
        final String _certifScholDatPrincipalName;
        final String _certifScholDatJobName;
        final String _certifScholDatHrJobName;
        final String _certifScholDatRemark3;
        final String _certifScholDatRemark4;
        final String _certifScholDatRemark5;

        KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool _definecode;
        private boolean _isSeireki;
        private TreeMap _semesterMap;
        final boolean _useAbsenceWarn;
        final String _kijunten;

        boolean _isNoPrintMoto;

        final List _d026List = new ArrayList();

        /** Bパターンのとき校長印欄のあるフォームを選択するか */
        final boolean _isPrintPrincipalMark;

        final String _documentRoot;

        private String _imagePath;
        private String _extension;
        private File _logoFile;

        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;

        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;

        /** 増加単位を反映する */
        final String _zouka;

        final Map _attendParamMap;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;

        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isGakunenKongou = false;
        boolean _isHoutei = false;
        final boolean _isTokubetsuShien;
        final List _schGDatList;
        final String _d008Namecd1;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year               = request.getParameter("CTRL_YEAR");
            _semester           = request.getParameter("SEMESTER");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE").replace('/', '-');
            _edate              = request.getParameter("DATE").replace('/', '-');
            _descDate           = null == request.getParameter("DESC_DATE") ? null : request.getParameter("DESC_DATE").replace('/', '-');
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _grade_hr_class     = request.getParameter("GRADE_HR_CLASS");
            _schoolKind			= request.getParameter("SCHOOL_KIND");
            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd    = request.getParameter("useCurriculumcd");
            _useClassDetailDat  = request.getParameter("useClassDetailDat");
            _useVirus           = request.getParameter("useVirus");
            _useKoudome         = request.getParameter("useKoudome");

            _pattern = request.getParameter("PATTERN");
            _trPrint = request.getParameter("TR_PRINT");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _printDate = KNJ_EditDate.h_format_JP(db2, _descDate);
            _gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' "));

            boolean haskettenParam = false;
            for (final Enumeration enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String paramname = (String) enums.nextElement();
                if ("KETTEN".equals(paramname)) {
                    haskettenParam = true;
                    break;
                }
            }
            _useKetten = haskettenParam;

            _ketten         = !NumberUtils.isNumber(request.getParameter("KETTEN")) ? new Integer(-1) : Integer.valueOf(request.getParameter("KETTEN"));
            _kettenHyotei   = request.getParameter("KETTEN_HYOTEI") == null ? new Integer(-1) : Integer.valueOf(request.getParameter("KETTEN_HYOTEI"));
            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _kijunten       = request.getParameter("OUTPUT_KIJUN");

            //_isPrintAbsenceHighNote = "1".equals(request.getParameter("TYUI_TYOUKA_CHECK"));
            _isPrintPrincipalMark   = "1".equals(request.getParameter("KOUTYOU"));
            _documentRoot           = request.getParameter("DOCUMENTROOT");
            _chikokuHyoujiFlg       = request.getParameter("chikokuHyoujiFlg");

            _dispSemester = new String[]{"1", "2", "3", SEMEALL};

            _zouka = request.getParameter("ZOUKA");

            if ("1".equals(request.getParameter("knjd181hOutputGakunenhyoka"))) {
            	_testcdGakunenHyokaHyotei = _99901;
                _testKindCds = new String[]{"19900", "29900", _39900, _99900, _99901};
                _printTestKindCds = new String[]{"19900", "29900", _39900, _99901};
            } else {
            	_testcdGakunenHyokaHyotei = _99900;
                if (PATTERN_B.equals(_pattern)) {
                    _testKindCds = new String[]{"19900", "29900", _99901, _99900};
                } else {
                    _testKindCds = new String[]{"19900", "29900", _39900, _99900};
                }
                _printTestKindCds = _testKindCds;
            }

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("useCurriculumcd", _useCurriculumcd);
            _attendParamMap.put("useVirus", _useVirus);
            _attendParamMap.put("useKoudome", _useKoudome);

            _certifScholDatSchoolName       = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifScholDatJobName          = getCertifSchoolDat(db2, "JOB_NAME");
            _certifScholDatPrincipalName    = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _certifScholDatHrJobName        = getCertifSchoolDat(db2, "REMARK2");
            _certifScholDatRemark3          = getCertifSchoolDat(db2, "REMARK3");
            _certifScholDatRemark4          = getCertifSchoolDat(db2, "REMARK4");
            _certifScholDatRemark5          = getCertifSchoolDat(db2, "REMARK5");

            _isTokubetsuShien = "1".equals(request.getParameter("useSpecial_Support_Hrclass"));
            if ("2".equals(_hrClassType) && "1".equals(request.getParameter("useFi_Hrclass"))) {
                _isFi = true;
            } else if ("2".equals(_hrClassType) && _isTokubetsuShien) {
                _isGhr = true;
            } else if ("1".equals(_hrClassType) && "1".equals(_gakunenKongou) && _isTokubetsuShien) {
                _isGakunenKongou = true;
            } else {
                _isHoutei = true;
            }
            log.debug(" fi? " + _isFi + ", ghr? " + _isGhr + ", gakunenKongou? " + _isGakunenKongou);

            try {
                loadNameMstD016(db2);
                loadNameMstD026(db2);
                loadNameMstZ012(db2);
            } catch (SQLException e) {
                log.error("名称マスタ読み込みエラー", e);
            }

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }

            _maxSemester = getMaxSemester();

            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);
            if (log.isDebugEnabled()) {
                log.debug("schoolmark=" + _definecode.schoolmark + " *** semesdiv=" + _definecode.semesdiv + " " +
                        "*** absent_cov=" + _definecode.absent_cov + " *** absent_cov_late=" + _definecode.absent_cov_late);
            }

            loadSemester(db2);
            loadControlMst(db2);

            final File file = new File(_documentRoot + "/" + _imagePath + "/" + "SCHOOLLOGO." + _extension);
            _logoFile = file.exists() ? file : null;

            _whitespaceImagePath = getImageFilePath("whitespace.png");
            _schGDatList = loadSchGDatList(db2);

            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        private List loadSchGDatList(final DB2UDB db2) {
        	List retList = new ArrayList();
        	StringBuffer stb = new StringBuffer();
        	stb.append(" WITH GRADE_2018 AS ( ");
        	stb.append("     SELECT ");
        	stb.append("         * ");
        	stb.append("     FROM ");
        	stb.append("         SCHREG_REGD_GDAT ");
        	stb.append("     WHERE ");
        	stb.append("         YEAR = '2018' ");
        	stb.append("         AND SCHOOL_KIND = 'H' ");
        	stb.append("     ORDER BY ");
        	stb.append("         GRADE ");
        	stb.append("         FETCH FIRST 1 ROWS ONLY ");
        	stb.append(" ) ");
        	stb.append(" , GRADE_2019 AS ( ");
        	stb.append("     SELECT ");
        	stb.append("         * ");
        	stb.append("     FROM ");
        	stb.append("         SCHREG_REGD_GDAT ");
        	stb.append("     WHERE ");
        	stb.append("         YEAR = '2019' ");
        	stb.append("         AND SCHOOL_KIND = 'H' ");
        	stb.append("     ORDER BY ");
        	stb.append("         GRADE ");
        	stb.append("         FETCH FIRST 2 ROWS ONLY ");
        	stb.append(" ) ");
        	stb.append(" SELECT * FROM GRADE_2018 ");
        	stb.append(" UNION ");
        	stb.append(" SELECT * FROM GRADE_2019 ");
        	stb.append(" ORDER BY ");
        	stb.append("     YEAR ");
        	stb.append("     , GRADE ");
            final String sql = stb.toString();
            //log.debug("CHKSQL:" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String _d_year = rs.getString("YEAR");
                    final String _d_grade = rs.getString("GRADE");
                    SchGDat addwk = new SchGDat(_d_year, _d_grade);
                    retList.add(addwk);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        public String getImageFilePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name;
            final boolean exists = new File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        public boolean isGakunenmatsu() {
            return Param.SEMEALL.equals(_semester);
        }

        public boolean isGakunenmatu(final String semTestKindCd) {
            return SEMEALL.equals(semTestKindCd.substring(0,1));
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public String getRegdSemester() {
            return isGakunenmatsu() ? _ctrlSemester : _semester;
        }

        public String getMaxSemester() {
            try {
                return _knjSchoolMst._semesterDiv;
            } catch (Exception ex) {
                log.error("getMaxSemester exception!", ex);
            }
            return null;
        }

        public String[] getTargetSemester() {
            final List list = new ArrayList();
            for (int i = 0; i < _dispSemester.length; i++) {
                final String dseme = _dispSemester[i];
                if (_semester.compareTo(dseme) >= 0) {
                    list.add(dseme);
                }
            }

            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }


        public String[] getPrintTestKindCds() {
            final List list = new ArrayList();
            for (int i = 0; i < _printTestKindCds.length; i++) {
                final String testkind = _printTestKindCds[i];
                if (_semester.compareTo(testkind.substring(0, 1)) >= 0) {
                    list.add(testkind);
                }
            }

            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }

        public String[] getTargetTestKindCds() {
            final List list = new ArrayList();
            for (int i = 0; i < _testKindCds.length; i++) {
                final String testkind = _testKindCds[i];
                if (_semester.compareTo(testkind.substring(0, 1)) >= 0) {
                    list.add(testkind);
                }
            }

            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT " + field + " FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            //log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private void loadSemester(final DB2UDB db2) {
            _semesterMap = new TreeMap();

            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd    = rs.getString("SEMESTER");
                    final String name  = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    final Semester semester = new Semester(cd, name, sdate, edate);
                    _semesterMap.put(cd, semester);
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getSemesterName(final String semester) {
            Semester s = (Semester) _semesterMap.get(semester);
            return s == null ? null : s._name;
        }

        private void loadNameMstZ012(final DB2UDB db2) throws SQLException {
            _isSeireki = false;
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z012'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAME1");
                if ("2".equals(name)) _isSeireki = true;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.debug("(名称マスタZ012):西暦フラグ = " + _isSeireki);
        }

        private void loadNameMstD016(final DB2UDB db2) throws SQLException {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAMESPARE1");
                if ("Y".equals(name)) _isNoPrintMoto = true;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.debug("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }


        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public boolean isD026ContainSubclasscd(String subclasscd) {
            if (null == subclasscd) {
                return false;
            }
            if ("1".equals(_useClassDetailDat)) {
            } else if (StringUtils.split(subclasscd, "-").length == 4) {
                subclasscd = StringUtils.split(subclasscd, "-")[3]; // clascd '-' school_kind '-' curriculum_cd '-' subclasscd
            }
            return _d026List.contains(subclasscd);
        }
    }
}
