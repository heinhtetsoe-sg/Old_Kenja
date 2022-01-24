// kanji=漢字
/*
 * $Id: 66c8316cf35cd5afa3de2e2d442f87da4db99bd2 $
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
import java.util.LinkedList;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.Vrw32alpWrap;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */

public class KNJD181 {

    private static final String SLUMP_CD = "1";
    private static final String SUBCLASS_ALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD181.class);

    /** 修得単位数 */
    private int scredits;

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

        log.fatal(" $Revision: 75745 $ $Date: 2020-07-30 18:49:11 +0900 (木, 30 7 2020) $");
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);

        final KNJD181Base knjd181;
        if (Param.PATTERN_A.equals(param._pattern)) {
            knjd181 = new KNJD181A(db2, svf, param);
        } else if (Param.PATTERN_B.equals(param._pattern)) {
            knjd181 = new KNJD181B(db2, svf, param);
        } else if (Param.PATTERN_C.equals(param._pattern)) {
            knjd181 = new KNJD181C(db2, svf, param);
        } else if (Param.PATTERN_D.equals(param._pattern)) {
            knjd181 = new KNJD181D(db2, svf, param);
        } else {
            knjd181 = null;
        }

        try {
            final List students = createStudents(db2, param);

            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                knjd181.print(student);

                hasData = true;
            }

            if (!hasData) {
                log.warn("データがありません");
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
            if (null != knjd181) {
                for (final Iterator it = knjd181._formInfoMap.values().iterator(); it.hasNext();) {
                    final File formPath = (File) it.next();
                    if (null != formPath && formPath.exists()) {
                        final boolean deleted = formPath.delete();
                        log.info(" file " + formPath.getPath() + " deleted? " + deleted);
                    }
                }
            }
        }

        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }

    private static String nullToAlt(final String str, final String alt) {
        return null == str ? alt : str;
    }

    /**
     * 表示するデータをセットした生徒のリストを得る
     * @param db2
     * @param param
     * @return
     */
    private List createStudents(final DB2UDB db2, final Param param) {

        final List students = Student.getStudents(db2, param);

        if (students.size() == 0) {
            log.warn("対象の生徒がいません");
            return students;
        }

        setAttendData(db2, students, param);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            setScoreValue(db2, student, param);
            if ("2".equals(param._gakunenmatuRan)) {
                setScoreValue99901(db2, student, param);
            }
            setSpecialSubclassAttendData(student, param);
            AbsenceHigh.setAbsenceHigh(db2, student, param);
            setRecDetail(db2, student, param);
            Address.setAddress(db2, student, param);
        }
        setPreviousScredits(db2, students, param);
        HReportRemark.setHReportRemark(db2, students, param);
        setRecordDocumentKindDatFootnote(db2, students, param);
        RecordTotalStudyTimeDat.setTotalStudy(db2, students, param);
        // setSogotekinaGakusyunoJikan(db2, students, param);

        return students;
    }

    /**
     * 前年度までの修得単位数合計を得る
     * @param db2
     * @param students
     * @param param
     */
    private void setPreviousScredits(final DB2UDB db2, final List students, final Param param) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        boolean isGakunensei = param._knjSchoolMst == null || "0".equals(param._knjSchoolMst._schoolDiv);

        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS ( ");
        stb.append(    "SELECT SCHREGNO ");
        stb.append(    "FROM   SCHREG_REGD_DAT T1 ");

        stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append(            "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
        stb.append(            "AND T1.GRADE || T1.HR_CLASS = '" + param._grade_hr_class + "' ");
        stb.append("    ) ");
        stb.append("SELECT SCHREGNO, SUM(VALUE(GET_CREDIT,0) + VALUE(ADD_CREDIT,0)) AS SCREDITS ");
        stb.append("FROM   SCHREG_STUDYREC_DAT T1 ");
        stb.append("WHERE  YEAR < '"+ param._year +"' AND ");
        stb.append("       SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        if (isGakunensei) {
            stb.append("       AND YEAR NOT IN (SELECT T2.YEAR ");
            stb.append("       FROM SCHREG_REGD_DAT T2  ");
            stb.append("       WHERE T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND (GRADE, YEAR) NOT IN ");
            stb.append("          (SELECT GRADE, MAX(YEAR) ");
            stb.append("           FROM SCHREG_REGD_DAT ");
            stb.append("           WHERE SCHREGNO = T2.SCHREGNO ");
            stb.append("           GROUP BY GRADE)) ");
        }
        stb.append("       GROUP BY SCHREGNO ");

        final String previousScreditsSql = stb.toString();
        try {
            // log.debug(" previous scredits sql = " + previousScreditsSql);
            ps = db2.prepareStatement(previousScreditsSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = getStudent(rs.getString("SCHREGNO"), students);
                if (student == null) {
                    continue;
                }
                student._previousScredits = rs.getInt("SCREDITS");
            }

        } catch (SQLException e) {
            log.error("sql exception! :" + previousScreditsSql, e);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     * フッタの内容をセットする
     * @param db2
     * @param students
     * @param param
     */
    private void setRecordDocumentKindDatFootnote(final DB2UDB db2, final List students, final Param param) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.FOOTNOTE ");
        stb.append(" FROM ");
        stb.append("     RECORD_DOCUMENT_KIND_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year +"' ");
        stb.append("     AND T1.SEMESTER = '" + (Param.SEMEALL.equals(param._semester) ? param.getMaxSemester() : param._semester)  + "' ");
        stb.append("     AND T1.TESTKINDCD = '99' ");
        stb.append("     AND T1.TESTITEMCD = '00' ");
        stb.append("     AND T1.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' ");
        stb.append("     AND T1.HR_CLASS = '000' ");
        stb.append("     AND T1.COURSECD = '0' ");
        stb.append("     AND T1.MAJORCD = '000' ");
        stb.append("     AND T1.COURSECODE = '0000' ");
        stb.append("     AND T1.SUBCLASSCD = '000000' ");
        stb.append("     AND T1.KIND_DIV = '2' ");

        final String sql = stb.toString();
        String footnote = "";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                footnote = rs.getString("FOOTNOTE");
            }
        } catch (SQLException e) {
            log.error("sql exception! :" + sql, e);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student= (Student) it.next();
            student._recordDocumentKindDatFootnote = footnote;
        }
    }

    /**
     * 学籍番号の生徒を得る
     * @param schregno 学籍番号
     * @param students 生徒のリスト
     * @return 学籍番号の生徒
     */
    private Student getStudent(final String schregno, final List students) {
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    /**
     * 科目ごとの出欠から特別活動の出欠をセットする。
     */
    private void setSpecialSubclassAttendData(final Student student, final Param param) {

        final String[] semesters;
        if (Param.PATTERN_C.equals(param._pattern)) {
            semesters = new String[]{"1", "2", Param.SEMEALL};
        } else if (Param.PATTERN_D.equals(param._pattern)) {
            semesters = new String[]{"1", Param.SEMEALL};
        } else {
            semesters = new String[]{"1", "2", "3", Param.SEMEALL};
        }

        for (int i = 0; i < semesters.length; i++) {
            final String semester = semesters[i];

            int spShrKoma = 0;
            int spLhrKoma = 0;
            int spAssKoma = 0;
            final Map spGroupLessons = new HashMap();
            final Map spGroupKekka = new HashMap();
            final Map specialSubclassAttendanceMap = student.getSpecialSubclassAttendanceMap(semester);

            for (final Iterator it = specialSubclassAttendanceMap.keySet().iterator(); it.hasNext();) {
                final String specialGroupCd = (String) it.next();
                final SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);

                final BigDecimal spGroupLessonJisu = getSpecialAttendExe(ssa.spLessonMinutesTotal(), param);
                final BigDecimal spGroupAbsenceJisu = getSpecialAttendExe(ssa.spAbsenceMinutesTotal(), param);

                spGroupLessons.put(specialGroupCd, new Integer(spGroupLessonJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
                spGroupKekka.put(specialGroupCd, new Integer(spGroupAbsenceJisu.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
                if (Attendance.GROUP_SHR.equals(specialGroupCd)) {
                    spShrKoma += ssa.spAbsenceKomaTotal();
                } else if (Attendance.GROUP_LHR.equals(specialGroupCd)) {
                    spLhrKoma += ssa.spAbsenceKomaTotal();
                } else if (Attendance.GROUP_ASS.equals(specialGroupCd)) {
                    spAssKoma += ssa.spAbsenceKomaTotal();
                }
            }

            //log.debug(" student = " + student._schregno + " , semester = " + semester + " : " + " , " + spGroupLessons + " , " +  spGroupKekka);

            final Attendance att = student.getAttendance(semester);
            att._spGroupLessons = spGroupLessons;
            att._spGroupKekka = spGroupKekka;
            att._spShrKoma = spShrKoma;
            att._spLhrKoma = spLhrKoma;
            att._spAssKoma = spAssKoma;
        }
    }

    /**
     * 欠課時分を欠課時数に換算した値を得る
     * @param kekka 欠課時分
     * @return 欠課時分を欠課時数に換算した値
     */
    private BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
        final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
        final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
        int hasu = 0;
        final String retSt = bigD.toString();
        final int retIndex = retSt.indexOf(".");
        if (retIndex > 0) {
            hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
        }
        final BigDecimal rtn;
        if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
            rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
        } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
            rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
        } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
            rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
        } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
            rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
        } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
            rtn = bigD;
        } else {
            rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
        }
        return rtn;
    }

    /**
     * 成績・序列・欠点をセットする
     * @param db2
     * @param student
     * @param param
     */
    private void setScoreValue(final DB2UDB db2, final Student student, final Param param) {

        final String prestatementRecordScore = sqlRecordScore(param, student._schregno);
        // log.debug("setScoreValue sql = " + prestatementRecordScore);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(prestatementRecordScore);
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

                    student.findSubclass(subclassCd).setScoreValue(testKindCd, tableDiv, sv);

                    if (Param._99900.equals(testKindCd)) {
                        if (org.apache.commons.lang.math.NumberUtils.isDigits(value)
                                && Integer.valueOf(value).compareTo(param._kettenHyotei) <= 0
                                && Param.REC.equals(tableDiv)) {
                            final Subclass subclass = student.getSubclass(subclassCd);
                            subclass.setSlump(testKindCd, SLUMP_CD);
                        }
                    } else {
                        if (param._useKetten) {
                            if (org.apache.commons.lang.math.NumberUtils.isDigits(value)
                                    && Integer.valueOf(value).compareTo(param._ketten) <= 0
                                    && Param.REC.equals(tableDiv)) {
                                final Subclass subclass = student.getSubclass(subclassCd);
                                subclass.setSlump(testKindCd, SLUMP_CD);
                            }
                        } else {
                            final Subclass subclass = student.getSubclass(subclassCd);
                            subclass.setSlump(testKindCd, rs.getString("SLUMP"));
                        }
                    }
                    // log.debug(" " + student + " , subclassCd = " + subclassCd + " , semTestKindCd = " + testKindCd + " , tableDiv = " + tableDiv + " "+ sv);
                }
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     * 成績・序列・欠点をセットする
     * @param db2
     * @param student
     * @param param
     */
    private void setScoreValue99901(final DB2UDB db2, final Student student, final Param param) {

        final String prestatementRecordScore = sqlRecordScore99901(param, student._schregno);
        log.debug("setScoreValue99901 sql = " + prestatementRecordScore);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(prestatementRecordScore);
            rs = ps.executeQuery();
            while(rs.next()) {
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
                    student.findSubclass(subclassCd).setScoreValue(testKindCd, tableDiv, sv);

                    if (param._useKetten) {
                        if (org.apache.commons.lang.math.NumberUtils.isDigits(value)
                                && Integer.valueOf(value).compareTo(param._ketten) <= 0
                                && Param.DIV99901.equals(tableDiv)) {
                            final Subclass subclass = student.getSubclass(subclassCd);
                            subclass.setSlump(testKindCd, SLUMP_CD);
                        }
                    } else {
                        final Subclass subclass = student.getSubclass(subclassCd);
                        subclass.setSlump(testKindCd, rs.getString("SLUMP"));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     * 各生徒に１日ごと、科目ごとの出欠データをセットする
     * @param db2
     * @param students
     * @param param
     */
    private void setAttendData(final DB2UDB db2, final List students, final Param param) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String[] targetSemesters;
        if (Param.PATTERN_C.equals(param._pattern)) {
            targetSemesters = new String[]{"1", "2", Param.SEMEALL};
        } else if (Param.PATTERN_D.equals(param._pattern)) {
            targetSemesters = new String[]{"1", Param.SEMEALL};
        } else {
            targetSemesters = new String[]{"1", "2", "3", Param.SEMEALL};
        }
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
            final String sdate = Param.SEMEALL.equals(semester) ? param._sdate : semesS._sdate;
            final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;
            try {
                String sql;
                sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        semester,
                        sdate,
                        edate,
                        param._attendParamMap
                );

                //log.debug(" attend semes sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
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
                        semester,
                        sdate,
                        edate,
                        param._attendParamMap
                );

                // log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"), students);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
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

                    final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                    if (specialGroupCd != null) {
                        final int spLessonMinutes = rs.getInt("SPECIAL_LESSON_MINUTES");

                        int spAbsenceMinutes = 0;
                        if (param._subClassC005.containsKey(subclassCd)) {
                            final String is = (String) param._subClassC005.get(subclassCd);
                            if ("1".equals(is)) {
                                spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES3");
                            } else if ("2".equals(is)) {
                                spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES2");
                            }
                        } else {
                            spAbsenceMinutes = rs.getInt("SPECIAL_SICK_MINUTES1");
                        }

                        SpecialSubclassAttendance ssa = student.getSpecialSubclassAttendance(specialGroupCd, semester);
                        ssa.add(subclassCd, lesson.intValue(), sick.intValue(), spLessonMinutes, spAbsenceMinutes);
//                        log.debug("   schregno = " + student._schregno + " , specialGroupCd = " + specialGroupCd + " , special subclass attendance = " + ssa);
                    }

                    student.setSubclassAttendance(subclassCd, semester, sa);
//                    log.debug("   schregno = " + student._schregno + " , subclcasscd = " + subclassCd + " , subclass attendance = " + sa);
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

    private String sqlRecordScore(final Param param, final String schregno) {

        final String[] targets = param.getTargetTestKindCds();
        int rank = 0;
        int rankV = 0;

        for (int i = 0; i < targets.length; i++) {
            if (param.isValueFromRecordRankVDat(targets[i])) {
                rankV += 1;
            } else {
                rank += 1;
                if (param.isGakunenmatu(targets[i])) {
                    rankV += 1;
                }
            }
        }
        final String[] fromRank = new String[rank];
        final String[] fromRankV = new String[rankV];
        String unionAll = "";

        if (rank != 0) {
            for (int c = 0, i = 0; i < targets.length; i++) {
                if (!param.isValueFromRecordRankVDat(targets[i])) {
                    fromRank[c] = targets[i];
                    c += 1;
                }
            }
        }

        if (rankV != 0) {
            for (int c = 0, i = 0; i < targets.length; i++) {
                if (param.isValueFromRecordRankVDat(targets[i]) || param.isGakunenmatu(targets[i])) {
                    fromRankV[c] = targets[i];
                    c += 1;
                }
            }
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RECORD_VALUE AS ( ");
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
            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("        AND T2.SCHREGNO = '" + schregno + "'");

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
            stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
            stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, fromRank));
            unionAll = "   UNION ALL";
        }
        if (rankV != 0) {
            stb.append(unionAll);
            stb.append("   SELECT  '" + Param.REC_V + "' AS TABLE_DIV, T1.YEAR, T1.SCHREGNO, ");
            stb.append("           T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            stb.append("           T1.SUBCLASSCD, T1.SEMESTER, ");
            stb.append("           T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
            stb.append("           T1.SCORE, T1.AVG, ");
            stb.append("           T1.COURSE_RANK, T1.COURSE_AVG_RANK, T3.COUNT AS COURSE_COUNT, ");
            stb.append("           T1.CLASS_RANK, T1.CLASS_AVG_RANK, T4.COUNT AS CLASS_COUNT, ");
            stb.append("           T6.SLUMP ");
            stb.append("   FROM    RECORD_RANK_V_DAT T1");

            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("        AND T2.SCHREGNO = '" + schregno + "'");
            stb.append("   LEFT JOIN RECORD_AVERAGE_V_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD ");
            stb.append("        AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T3.GRADE = T2.GRADE ");
            stb.append("        AND T3.AVG_DIV = '3' ");
            stb.append("        AND T3.HR_CLASS = '000' ");
            stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_V_DAT T4 ON T4.YEAR = T1.YEAR ");
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
            stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
            stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, fromRankV));
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
        stb.append("   WHERE T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
        stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
        stb.append("            OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
        stb.append("            OR T1.SUBCLASSCD = '" + SUBCLASS_ALL + "')");
        return stb.toString();
    }

    private String sqlRecordScore99901(final Param param, final String schregno) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RECORD_VALUE AS ( ");
        stb.append("   SELECT  '" + Param.DIV99901 + "' AS TABLE_DIV, T1.YEAR, T1.SCHREGNO, ");
        stb.append("           T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
        stb.append("           T1.SUBCLASSCD, T1.SEMESTER, ");
        stb.append("           T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
        stb.append("           T1.SCORE, T1.AVG, ");
        stb.append("           T1.COURSE_RANK, T1.COURSE_AVG_RANK, T3.COUNT AS COURSE_COUNT, ");
        stb.append("           T1.CLASS_RANK, T1.CLASS_AVG_RANK, T4.COUNT AS CLASS_COUNT, ");
        stb.append("           T6.SLUMP ");
        stb.append("   FROM    RECORD_RANK_DAT T1");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
        stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
        stb.append("        AND T2.SCHREGNO = '" + schregno + "'");

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
        stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
        stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + "('" + Param._99901 + "') ");
        stb.append(" ) ");
        stb.append("   SELECT  T1.TABLE_DIV ");
        stb.append("           , CASE WHEN T1.SUBCLASSCD = '" + SUBCLASS_ALL + "' THEN T1.SUBCLASSCD ");
        stb.append("              ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD END ");
        stb.append("             AS SUBCLASSCD ");
        stb.append("           ,T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS TESTKINDCD ");
        stb.append("           ,T1.SCORE, T1.AVG, T1.COURSE_RANK, T1.COURSE_AVG_RANK, T1.COURSE_COUNT, T1.CLASS_RANK, T1.CLASS_AVG_RANK, T1.CLASS_COUNT ");
        stb.append("           ,T1.VALUE, T1.SLUMP ");
        stb.append("   FROM    RECORD_VALUE T1");
        stb.append("   WHERE T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
        stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
        stb.append("            OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
        stb.append("            OR T1.SUBCLASSCD = '" + SUBCLASS_ALL + "')");
        return stb.toString();
    }

    private String sqlSubclass (final Param param) {

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
        stb.append("               AND T3.GRADE || T3.HR_CLASS = '" + param._grade_hr_class + "'");
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
        stb.append("       AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "')");
        stb.append("   GROUP BY ");
        stb.append("   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
        stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        stb.append("             T2.SUBCLASSCD");
        stb.append(" )");

        stb.append(", SUBCLASSNUM AS(");
        stb.append("   SELECT  SUM(CASE WHEN ");
        stb.append(" S1.CLASSCD ");
        stb.append("            = '" + KNJDefineSchool.subject_T + "' OR T1.NAMECD2 IS NOT NULL THEN 1 ELSE NULL END) AS NUM90");
        stb.append("         , SUM(CASE WHEN ");
        stb.append(" S1.CLASSCD ");
        stb.append("           != '" + KNJDefineSchool.subject_T + "' AND T1.NAMECD2 IS NULL THEN 1 ELSE NULL END) AS NUMTOTAL");
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
        stb.append("       , CASE WHEN '90' = T2.CLASSCD THEN 3 ");
        stb.append("              WHEN N1.NAMECD2 IS NOT NULL THEN 2 ");
        stb.append("              ELSE 1 END AS ORDER0");
        stb.append("       , CASE WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN T9.COMBINED_SUBCLASSCD ELSE T2.SUBCLASSCD END AS ORDER1");
        stb.append("       , CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN 1 WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 2 ELSE 0 END AS ORDER2");
        stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
        stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
        stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
        stb.append("       , REC_SCORE.COMP_CREDIT ");
        if ("1".equals(param._zouka)) {
            stb.append("       , CASE WHEN QUAL.CREDITS IS NOT NULL THEN QUAL.CREDITS + VALUE(REC_SCORE.GET_CREDIT, 0) ELSE REC_SCORE.GET_CREDIT END AS GET_CREDIT ");
        } else {
            stb.append("       , REC_SCORE.GET_CREDIT AS GET_CREDIT ");
        }
        stb.append("       , REC_SCORE.ADD_CREDIT ");
        stb.append(" FROM    CHAIR_A T2");
        stb.append(" LEFT JOIN SUBCLASS_MST T4 ON ");
        stb.append("    T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
        stb.append("   T4.SUBCLASSCD = T2.SUBCLASSCD");
        stb.append(" LEFT JOIN CLASS_MST T7 ON ");
        stb.append("   T7.CLASSCD || '-' || T7.SCHOOL_KIND = ");
        stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
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
        stb.append("    QUAL.CLASSCD || '-' || QUAL.SCHOOL_KIND || '-' || QUAL.CURRICULUM_CD || '-' || ");
        stb.append("       QUAL.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" ORDER BY ORDER0, ORDER1, ORDER2");
        return stb.toString();
    }

    private void setRecDetail (
            final DB2UDB db2,
            final Student student,
            final Param param
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        student._subclassInfos.clear();
        try {
            final String prestatementSubclass = sqlSubclass(param);
            log.fatal(" subclass sql = " + prestatementSubclass);
            ps = db2.prepareStatement(prestatementSubclass);

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

                final Subclass subclass = student.getSubclass(subclassCd);

                final SubclassInfo info = new SubclassInfo(classabbv, subclassname, credits, compCredit, getCredit, subclassCd,
                        attendSubclasscd, combinedSubclasscd, namespare1, subclass, replaceflg, calculateCreditFlg, num90, num90Other);
                student._subclassInfos.add(info);
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        int totalCredit = 0;
        for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
            final SubclassInfo info = (SubclassInfo) it.next();
            if (null == info._getCredit
                    || (1 == info._replaceflg && student.hasSubclassInfo(info._combinedSubclasscd))
                    || (2 == info._replaceflg && (student.hasSubclassInfo(info._attendSubclasscd) || student.hasSubclassInfo(info._combinedSubclasscd)))) {
                // 加算しない。
            } else {
                totalCredit += Integer.parseInt(info._getCredit);
            }
        }
        student._totalCredit = totalCredit;
    }

    static class Student {

        public String _attendNo;
        public String _grade;
        public String _gradeCd;
        public String _courseCd;
        public String _courseName;
        public String _majorCd;
        public String _majorName;
        public String _courseCode;
        public String _courseCodeName;
        public String _hrName;
        public String _hrNameAbbv;
        public String _name;
        final String _schregno;
        final List _subclassInfos = new ArrayList();
        int _totalCredit = 0;
        final Map _subclassScoreMap = new HashMap();
        final Map _attendMap = new TreeMap();
        final Map _scoreRankMap = new HashMap();
        final Map _subclassAbsenceHigh = new HashMap();
        final Map _spSubclassAbsenceHigh = new HashMap();
        final Map _specialGroupAttendanceMap = new HashMap();
        final Map _recordTotalStudieTimeDat = new HashMap();
        final Map _hReportRemarks = new HashMap();
        String _recordDocumentKindDatFootnote;
        int _previousScredits;
        public Address _address;

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

        public void putSlump(final String subclassCd, final String testKindcd, final String slump) {
            findSubclass(subclassCd).setSlump(testKindcd, slump);
        }

        /**
         * 指定テスト種別の科目は欠点か
         * @param subclassCd 科目コード
         * @param testKindCd 指定テスト種別
         * @return
         */
        public boolean isSlump(final String subclassCd, final String testKindCd) {
            return findSubclass(subclassCd).isSlump(testKindCd);
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
         * 特別活動グループのグループごとの学期ごとの出欠をセットする
         * @param specialGroupCd グループコード
         * @param semester 学期
         * @return
         */
        public SpecialSubclassAttendance getSpecialSubclassAttendance(final String specialGroupCd, final String semester) {
            Map map = getSpecialSubclassAttendanceMap(semester);
            if (!map.containsKey(specialGroupCd)) {
                map.put(specialGroupCd, new SpecialSubclassAttendance(specialGroupCd));
            }
            return (SpecialSubclassAttendance) map.get(specialGroupCd);
        }

        public Map getSpecialSubclassAttendanceMap(final String semester) {
            if (!_specialGroupAttendanceMap.containsKey(semester)) {
                _specialGroupAttendanceMap.put(semester, new HashMap());
            }
            return (Map) _specialGroupAttendanceMap.get(semester);
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
         * 指定特別活動グループの欠課数上限値を得る
         * @param subclassCd 特別活動グループ
         * @return
         */
        public AbsenceHigh getSpecialAbsenceHigh(final String specialSubclassGroupCd) {
            return (AbsenceHigh) _spSubclassAbsenceHigh.get(specialSubclassGroupCd);
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

        private boolean isTankyu(final Param param) {
            boolean isTankyu = false;
            final int tankyuStartYear = 2019;
            final int year = Integer.parseInt(param._year);
            final int gradeCdInt = NumberUtils.isDigits(_gradeCd) ? Integer.parseInt(_gradeCd) : 99;
            if (year == tankyuStartYear     && gradeCdInt <= 1
             || year == tankyuStartYear + 1 && gradeCdInt <= 2
             || year == tankyuStartYear + 2 && gradeCdInt <= 3
             || year >= tankyuStartYear + 3
             ) {
                isTankyu = true;
            }
            return isTankyu;
        }

        public String toString() {
            return _schregno + ":" + _name;
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
                    student._gradeCd = rs1.getString("GRADE_CD");
                    student._courseCd = rs1.getString("COURSECD");
                    student._majorCd = rs1.getString("MAJORCD");
                    student._courseCode = rs1.getString("COURSECODE");

                    student._courseName = rs1.getString("COURSENAME");
                    student._majorName = rs1.getString("MAJORNAME");
                    student._courseCodeName = nullToAlt(rs1.getString("COURSECODENAME"), "");
                    student._hrName = nullToAlt(rs1.getString("HR_NAME"), "");
                    student._hrNameAbbv = nullToAlt(rs1.getString("HR_NAMEABBV"), "");
                    student._attendNo = null == rs1.getString("ATTENDNO") || !NumberUtils.isDigits(rs1.getString("ATTENDNO"))? "" : Integer.parseInt(rs1.getString("ATTENDNO")) + "番";
                    student._name = "1".equals(rs1.getString("USE_REAL_NAME")) ? nullToAlt(rs1.getString("REAL_NAME"), "") : nullToAlt(rs1.getString("NAME"), "");

                    //log.debug("対象の生徒" + student);
                }
            } catch( Exception ex ) {
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs1);
                db2.commit();
            }
            return students;
        }

        private static String sqlRegdData(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T3.GRADE_CD ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
            stb.append(    " INNER JOIN V_SEMESTER_GRADE_MST T2 ON T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(    " INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR ");
            stb.append(        "AND T1.GRADE = T3.GRADE ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectSchregno) + " ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");

            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T2.HR_NAMEABBV, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append(        "T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T1.GRADE_CD ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '03' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 宛先住所データ
     */
    static class Address {
        final String _addressee;
        final String _address1;
        final String _address2;
        final String _zipcd;

        public Address(final String addressee, final String address1, final String address2, final String zipcd) {
            _addressee = addressee;
            _address1 = address1;
            _address2 = address2;
            _zipcd = zipcd;
        }

        /**
         * 宛先の住所をセットする
         * @param db2
         * @param student
         * @param param
         */
        public static void setAddress(final DB2UDB db2, final Student student, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();

                if ("1".equals(param._addressSelect)) {
                    stb.append(" SELECT T0.SCHREGNO, ");
                    stb.append("        CASE WHEN T5.SCHREGNO IS NOT NULL THEN T0.REAL_NAME ELSE T0.NAME END AS ADDRESSEE, ");
                    stb.append("        T4.ADDR1, T4.ADDR2, T4.ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) T3 ON ");
                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                    stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                    stb.append(" LEFT JOIN SCHREG_NAME_SETUP_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO AND T5.DIV = '03' ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = '" + student._schregno + "' ");
                } else if ("2".equals(param._addressSelect)) {
                    stb.append(" SELECT T0.SCHREGNO, T2.GUARD_NAME AS ADDRESSEE, T5.GUARD_NAME AS ADDRESSEE2, T4.GUARD_ADDR1 AS ADDR1, T4.GUARD_ADDR2 AS ADDR2, T4.GUARD_ZIPCD AS ZIPCD, ");
                    stb.append("        T2.GUARD_NAME, T2.GUARD_REAL_NAME, CASE WHEN T7_2.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_GUARD_REAL_NAME ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT WHERE '" + param._ctrlDate + "' BETWEEN ISSUEDATE AND EXPIREDATE GROUP BY SCHREGNO) T3 ON ");
                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                    stb.append(" LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                    stb.append(" LEFT JOIN GUARDIAN_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._ctrlDate + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
                    stb.append(" LEFT JOIN GUARDIAN_NAME_SETUP_DAT T7_2 ON T7_2.SCHREGNO = T0.SCHREGNO AND T7_2.DIV = '03' ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = '" + student._schregno + "' ");
                } else {
                    stb.append(" SELECT T0.SCHREGNO, T2.SEND_NAME AS ADDRESSEE, T2.SEND_NAME AS ADDRESSEE2, T2.SEND_ADDR1 AS ADDR1, T2.SEND_ADDR2 AS ADDR2, T2.SEND_ZIPCD AS ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO AND T2.DIV = '1' ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = '" + student._schregno + "' ");
                }

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String addressee;
                    if ("2".equals(param._addressSelect)) {
                        if ("1".equals(rs.getString("USE_GUARD_REAL_NAME")) && null != rs.getString("GUARD_REAL_NAME")) {
                            addressee = nullToAlt(rs.getString("GUARD_REAL_NAME"), "");
                        } else {
                            addressee = StringUtils.defaultString(rs.getString("ADDRESSEE2"), rs.getString("ADDRESSEE"));
                        }
                    } else {
                        addressee = rs.getString("ADDRESSEE");
                    }
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String zipcd = rs.getString("ZIPCD");
                    student._address = new Address(addressee, addr1, addr2, zipcd);
                }

            } catch (SQLException e) {
                log.error("SQLException!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    /**
     * 1日出欠データ
     */
    static class Attendance {

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

        Map _spGroupLessons = new HashMap();
        Map _spGroupKekka = new HashMap();
        int _spShrKoma;
        int _spAssKoma;
        int _spLhrKoma;

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

        public BigDecimal getSpGroupKekka(final String groupCd) {
            final Integer kekka = (Integer) _spGroupKekka.get(groupCd);
            return kekka == null ? new BigDecimal(0) : new BigDecimal(kekka.intValue());
        }

        public int getSpGroupKomaKekka(final String groupCd) {
            if (Attendance.GROUP_SHR.equals(groupCd)) {
                return _spShrKoma;
            } else if (Attendance.GROUP_LHR.equals(groupCd)) {
                return _spLhrKoma;
            } else if (Attendance.GROUP_ASS.equals(groupCd)) {
                return _spAssKoma;
            }
            return 0;
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
    static class SubclassAttendance {
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
     * 特別活動グループの出欠
     */
    static class SpecialSubclassAttendance {
        final Map _spLessonMinutes;
        final Map _spAbsenceMinutes;
        final Map _spLessonKoma;
        final Map _spAbsenceKoma;

        final String _spGroupCd;

        public SpecialSubclassAttendance(final String spGroupCd) {
            _spGroupCd = spGroupCd;
            _spLessonMinutes = new HashMap();
            _spAbsenceMinutes = new HashMap();
            _spLessonKoma = new HashMap();
            _spAbsenceKoma = new HashMap();
        }

        public void add(final String subclasscd, final int splessonKoma, final int spAbsenceKoma, final int splessonMinutes, final int spAbsenceMinutes) {
            add(_spLessonKoma, subclasscd, splessonKoma);
            add(_spAbsenceKoma, subclasscd, spAbsenceKoma);
            add(_spLessonMinutes, subclasscd, splessonMinutes);
            add(_spAbsenceMinutes, subclasscd, spAbsenceMinutes);
        }

        public int spLessonKomaTotal() {
            return mapValueTotal(_spLessonKoma);
        }

        public int spAbsenceKomaTotal() {
            return mapValueTotal(_spAbsenceKoma);
        }

        public int spLessonMinutesTotal() {
            return mapValueTotal(_spLessonMinutes);
        }

        public int spAbsenceMinutesTotal() {
            return mapValueTotal(_spAbsenceMinutes);
        }

        private int mapValueTotal(final Map map) {
            int total = 0;
            for (final Iterator its = map.values().iterator(); its.hasNext();) {
                final Integer intnum = (Integer) its.next();
                total += intnum.intValue();
            }
            return total;
        }

        private void add(final Map subclasscdIntMap, final String subclasscd, final int intnum) {
            if (!subclasscdIntMap.containsKey(subclasscd)) {
                subclasscdIntMap.put(subclasscd, new Integer(0));
            }
            final Integer intn = (Integer) subclasscdIntMap.get(subclasscd);
            subclasscdIntMap.put(subclasscd, new Integer(intn.intValue() + intnum));
        }

        public String getMinutesString() {
            return " spGroupCd = " + _spGroupCd + " , spLessonMinutes = " + _spLessonMinutes + " , spAbsenceMinutes = " + _spAbsenceMinutes;
        }

        public String toString() {
            return getMinutesString();
        }
    }

    /**
     * 生徒の科目ごとの成績・出欠データ
     */
    static class Subclass {
        final String _schregno;
        final String _subclassCd;
        final String _classCd;

        final Map _testKindTableMap = new HashMap();

        final Map _attendSubclassMap = new HashMap();

        final Map _slumpMap = new HashMap();

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

        public void setSlump(final String testKind, final String slump) {
            _slumpMap.put(testKind, slump);
        }

        public boolean isSlump(final String testKind) {
            return SLUMP_CD.equals(_slumpMap.get(testKind));
        }

        public String toString() {
            return _schregno + " : " + _subclassCd + ":" + _testKindTableMap;
        }
    }

    /**
     * 欠課数上限値
     */
    static class AbsenceHigh {
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

        // ------

        /**
         * 生徒に欠課数上限値をセットする
         * @param db2
         * @param student 生徒
         * @param param
         */
        public static void setAbsenceHigh(final DB2UDB db2, final Student student, final Param param) {
            String absenceHighSql = "";
            String spAbsenceHighSql = "";
            if (param._knjSchoolMst.isHoutei()) {
                absenceHighSql = sqlHouteiJisu(student, null, param, false);
                spAbsenceHighSql = sqlHouteiJisu(student, null, param, true);
            } else {
                absenceHighSql = sqlJituJisuSql(student, null, param, false);
                spAbsenceHighSql = sqlJituJisuSql(student, null, param, true);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(absenceHighSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String absenceHigh = nullToAlt(rs.getString("ABSENCE_HIGH"), "0");
                    final String getAbsenceHigh = nullToAlt(rs.getString("GET_ABSENCE_HIGH"), "0");

                    student._subclassAbsenceHigh.put(rs.getString("SUBCLASSCD"), new AbsenceHigh(absenceHigh, getAbsenceHigh));
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                ps = db2.prepareStatement(spAbsenceHighSql);
                rs = ps.executeQuery();
                student._spSubclassAbsenceHigh.clear();
                while (rs.next()) {
                    final String compAbsenceHigh = nullToAlt(rs.getString("ABSENCE_HIGH"), "0");
                    final String getAbsenceHigh = nullToAlt(rs.getString("GET_ABSENCE_HIGH"), "0");

                    student._spSubclassAbsenceHigh.put(rs.getString("SPECIAL_GROUP_CD"), new AbsenceHigh(compAbsenceHigh, getAbsenceHigh));
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlHouteiJisu(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_RISHU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_SHUTOKU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.GRADE = T1.GRADE AND ");
            stb.append("       T2.COURSECD = T1.COURSECD AND ");
            stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
            stb.append("       T2.YEAR = T1.YEAR AND ");
            stb.append("       T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T2.SCHREGNO = '" + student._schregno + "' ");
            return stb.toString();
        }

        private static String sqlJituJisuSql(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
            final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("        AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("        AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
            if (!isGroup) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                stb.append("       T3.SUBCLASSCD = ");
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                stb.append("       T1.SUBCLASSCD ");
            } else {
                stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
            }
            stb.append("       AND T3.COURSECD = T2.COURSECD ");
            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       AND T3.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.DIV = '2' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
            return stb.toString();
        }
    }

    static class ScoreValue {
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
    static class ScoreRank {
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
    static class Semester {
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

    static class RecordTotalStudyTimeDat {
        public final String _totalStudyTime;
        public final String _totalStudyAct;

        public RecordTotalStudyTimeDat(final String totalStudyTime, final String totalStudyAct) {
            _totalStudyTime = totalStudyTime;
            _totalStudyAct = totalStudyAct;
        }

        // -----

        /**
         * 総合的な学習の時間の所見をセットする
         * @param db2
         * @param students
         * @param param
         */
        public static void setTotalStudy(final DB2UDB db2, final List students, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("      SEMESTER ");
            stb.append("     ,CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     ,TOTALSTUDYTIME ");
            stb.append("     ,TOTALSTUDYACT");
            stb.append(" FROM ");
            stb.append("     RECORD_TOTALSTUDYTIME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND CLASSCD <> '88' ");
            stb.append(" ORDER BY ");
            stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");

            final String sql = stb.toString();
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    final Map recordTotalStudys = new HashMap();

                    student._recordTotalStudieTimeDat.clear();

                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        String totalStudyTime = rs.getString("TOTALSTUDYTIME");
                        String totalStudyAct = rs.getString("TOTALSTUDYACT");

                        if (null != recordTotalStudys.get(semester)) {
                            final RecordTotalStudyTimeDat bef = (RecordTotalStudyTimeDat) recordTotalStudys.get(semester);
                            totalStudyTime = addLine(bef._totalStudyTime, totalStudyTime);
                            totalStudyAct = addLine(bef._totalStudyAct, totalStudyAct);
                        }

                        final RecordTotalStudyTimeDat totalStudy = new RecordTotalStudyTimeDat(totalStudyTime, totalStudyAct);
                        recordTotalStudys.put(semester, totalStudy);
                    }
                    student._recordTotalStudieTimeDat.putAll(recordTotalStudys);
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

        private static String addLine(final String a, final String b) {
            if (StringUtils.isBlank(a)) {
                return b;
            }
            if (StringUtils.isBlank(b)) {
                return a;
            }
            return a + "\n" + b;
        }
    }

    /**
     * 通知書所見
     */
    static class HReportRemark {
        public final String _semester;
        public final String _specialActRemark;
        public final String _totalStudyTime;
        public final String _communication;
        public final String _remark3;

        public HReportRemark(final String semester,
                final String specialActRemark,
                final String totalStudyTime,
                final String communication,
                final String remark3) {
            _semester = semester;
            _specialActRemark = specialActRemark;
            _totalStudyTime = totalStudyTime;
            _communication = communication;
            _remark3 = remark3;
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
            stb.append("     SEMESTER ");
            stb.append("     ,SPECIALACTREMARK ");
            stb.append("     ,TOTALSTUDYTIME ");
            stb.append("     ,COMMUNICATION");
            stb.append("     ,REMARK3");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER <> '" + Param.SEMEALL + "' ");
            stb.append("     AND SCHREGNO = ? ");

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
                        final String remark3 = rs.getString("REMARK3");

                        final HReportRemark hreportremark = new HReportRemark(semester, specialActRemark, totalStudyTime, communication, remark3);
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

    static class SubclassInfo {
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
                final String num90Other) {
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
        return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static class KNJD181Base {

        protected DB2UDB _db2;
        protected Param _param;
        protected Vrw32alpWrap _svf;

        protected Map _amikakeMap;

        protected String _fieldScoreRank;

        protected String _fieldKamokuKetsuji;
        // protected String _fieldKamokuKekka;
        protected String _fieldKamokuLateEarly;
        protected String _fieldKamokuKouketsu;
        protected String _fieldKamokuMourning;
        protected String _fieldKamokuSuspend;

        protected Map _formInfoMap = new HashMap();

        protected String _sogakuBefore;
        protected String _sogakuAfter;
        protected String _formBefore;

        public KNJD181Base(DB2UDB db2, Vrw32alpWrap svf, Param param) {
            this._db2 = db2;
            this._svf = svf;
            this._param = param;
        }

        public void setForm(final String formName, final boolean tankyu) {
            String setForm = formName;
            if (tankyu) {
                if (!_formInfoMap.containsKey(formName) && null != _sogakuBefore && null != _sogakuAfter) {
                    final String formFilePath = _svf.getPath(formName);
                    File newFormFile = null;
                    if (null != formFilePath) {
                        try {
                            final File formFile = new File(formFilePath);
                            SvfForm svfForm = new SvfForm(formFile);
                            boolean modified = false;
                            if (svfForm.readFile()) {
                                List koteiMojiList = svfForm.getKoteiMojiListWithText(_sogakuBefore);
                                for (final Iterator it = koteiMojiList.iterator(); it.hasNext();) {
                                    KoteiMoji km = (KoteiMoji) it.next();
                                    svfForm.removeKoteiMoji(km);
                                    svfForm.addKoteiMoji(km.replaceMojiWith(_sogakuAfter));
                                    modified = true;
                                }
                                if (!modified) {
                                    log.info(" " + formName + " not modified : [" + _sogakuBefore + "] not found.");
                                } else {
                                    newFormFile = svfForm.writeTempFile();
                                }
                            }
                        } catch (Exception e) {
                            log.error("exception!", e);
                        }
                        _formInfoMap.put(formName, newFormFile);
                    }
                }
                final File formFile = (File) _formInfoMap.get(formName);
                if (null != formFile && formFile.exists()) {
                    setForm = formFile.getAbsolutePath();
                }
            }
            _svf.VrSetForm(setForm, 4);
            if (null == _formBefore || !_formBefore.equals(setForm)) {
                log.info(" setForm " + setForm);
            }
            _formBefore = setForm;
        }

        public void print(final Student student) {

        }

        protected void printComment(Vrw32alpWrap svf) {
            if (!_param._isNotPrintNote) {
                if (_param._isPrintAbsenceHighNote) {
                    String comment = _param._useAbsenceWarn ? "注意" : "超過";
                    svf.VrAttribute("MARK102",  "Paint=(1,40,1),Bold=1");
                    svf.VrsOut("MARK102",  " " );
                    svf.VrsOut("NOTE2_2",  "　：未履修" + comment + ",特活進級" + comment );

                    svf.VrAttribute("MARK103",  "Paint=(1,70,1),Bold=1");
                    svf.VrsOut("MARK103",  " " );
                    svf.VrsOut("NOTE2_3",  "　：未修得" + comment );
                }
                svf.VrAttribute("MARK104",  "Paint=(1,50,1),Bold=1");
                svf.VrsOut("MARK104",  " " );
                svf.VrsOut("NOTE2_4",  "　：不振");
            }
        }

        protected void setAmikake(Vrw32alpWrap svf) {
            if (_param._isPrintAbsenceHighNote) {
                if (_amikakeMap == null) {
                    return;
                }
                for (Iterator it = _amikakeMap.keySet().iterator(); it.hasNext();) {
                    String field = (String) it.next();
                    String attribute = (String) _amikakeMap.get(field);

                    final int commaIndex = field.indexOf(",");
                    if (commaIndex == -1) {
                        svf.VrAttribute(field, attribute);
                    } else {
                        String field1 = field.substring(0, commaIndex);
                        int idx = Integer.parseInt(field.substring(commaIndex + 1));
                        svf.VrAttributen(field1, idx, attribute);
                    }
                }
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

        protected void setAbsenceFieldAttribute(
                final Vrw32alpWrap svf,
                final AbsenceHigh absenceHigh,
                final String absent1,
                final String fieldKekka,
                final String fieldLateEarly
        ) {
            if (_param._isPrintAbsenceHighNote) {
                final String paint40 = "Paint=(1,40,1),Bold=1";
                final String paint70 = "Paint=(1,70,1),Bold=1";

                if (absenceHigh == null) {
                    if (absent1 != null && NumberUtils.isNumber(absent1) && Double.parseDouble(absent1) != 0.0) {
                        svf.VrAttribute(fieldKekka, paint40);
                        svf.VrAttribute(fieldLateEarly, paint40);
                        if (_amikakeMap != null) {
                            _amikakeMap.put(fieldKekka, paint40);
                            _amikakeMap.put(fieldLateEarly, paint40);
                        }
                    }
                } else if (absenceHigh.isRishuOver(absent1)) {
                    svf.VrAttribute(fieldKekka, paint40);
                    svf.VrAttribute(fieldLateEarly, paint40);
                    if (_amikakeMap != null) {
                        _amikakeMap.put(fieldKekka, paint40);
                        _amikakeMap.put(fieldLateEarly, paint40);
                    }
                } else if (absenceHigh.isShutokuOver(absent1)) {
                    svf.VrAttribute(fieldKekka, paint70);
                    svf.VrAttribute(fieldLateEarly, paint70);
                    if (_amikakeMap != null) {
                        _amikakeMap.put(fieldKekka, paint70);
                        _amikakeMap.put(fieldLateEarly, paint70);
                    }
                }
            }
        }

        protected void setSpecialAbsenceFieldAttribute(
                final Vrw32alpWrap svf,
                final AbsenceHigh absenceHigh,
                final String absent1,
                final String fieldKekka
        ) {
            if (_param._isPrintAbsenceHighNote) {
                final String paint40 = "Paint=(1,40,1),Bold=1";
                if (absenceHigh == null) {
                    if (absent1 != null && NumberUtils.isNumber(absent1) && Double.parseDouble(absent1) != 0.0) {
                        svf.VrAttribute(fieldKekka, paint40);
                        if (_amikakeMap != null) {
                            _amikakeMap.put(fieldKekka, paint40);
                        }
                    }
                } else if (absenceHigh.isRishuOver(absent1)) {
                    svf.VrAttribute(fieldKekka, paint40);
                    if (_amikakeMap != null) {
                        _amikakeMap.put(fieldKekka, paint40);
                    }
                }
            }
        }

        protected void setSpecialAbsenceFieldAttributen(
                final Vrw32alpWrap svf,
                final AbsenceHigh absenceHigh,
                final String absent1,
                final String fieldKekka,
                final int n
        ) {
            if (_param._isPrintAbsenceHighNote) {
                final String paint40 = "Paint=(1,40,1),Bold=1";
                if (absenceHigh == null) {
                    if (absent1 != null && NumberUtils.isNumber(absent1) && Double.parseDouble(absent1) != 0.0) {
                        svf.VrAttributen(fieldKekka, n, paint40);
                        if (_amikakeMap != null) {
                            _amikakeMap.put(fieldKekka + "," + n, paint40);
                        }
                    }
                } else if (absenceHigh.isRishuOver(absent1)) {
                    svf.VrAttributen(fieldKekka, n, paint40);
                    if (_amikakeMap != null) {
                        _amikakeMap.put(fieldKekka + "," + n, paint40);
                    }
                }
            }
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

        protected SubclassAttendance getSubclassAttendance90(final Student student, final String semester) {
            // 欠時数、欠課数： 科目コード頭2桁が90、合併先科目 + 合併設定なしの科目
            // 欠課数上限値：科目コード頭2桁が90、合併科目があるなら先科目の科目コードのMIN、ないなら合併設定なしの科目の科目コードのMIN)
            final Map subclassInfoMap = getSubclassInfoMap(student._subclassInfos);
            String minSubclassCd = null;
            int sick = 0;
            int rawSick = 0;
            int lateEarly = 0;
            int absent = 0;
            int mourning = 0;
            int suspend = 0;
            int koudome = 0;
            int virus = 0;
            boolean hasGappeiSubclasscd = false;
            for (final Iterator it = student._subclassScoreMap.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                if (null == subclassCd || !is90(subclassCd)) {
                    continue;
                }

                final SubclassInfo si = (SubclassInfo) subclassInfoMap.get(subclassCd);
                if (null != si) {
                    if (si._replaceflg == SubclassInfo.GAPPEI_SAKI && (!hasGappeiSubclasscd || null == minSubclassCd || si._subclassCd.compareTo(minSubclassCd) < 0)) {
                        minSubclassCd = si._subclassCd;
                        log.debug(" 合併先 minSubclassCd = " + minSubclassCd);
                        hasGappeiSubclasscd = true;
                    }
                }
                if (!hasGappeiSubclasscd && (null == minSubclassCd || subclassCd.compareTo(minSubclassCd) < 0)) {
                    minSubclassCd = subclassCd;
                    log.debug(" 合併設定無し minSubclassCd = " + minSubclassCd);
                }
                final SubclassAttendance sa = student.getSubclassAttendance(subclassCd, semester);
                if (null == sa) {
                    continue;
                }
                if (!hasGappeiSubclasscd && (null != si && si._replaceflg == SubclassInfo.GAPPEI_NASI)) { // 合併設定無し
                    sick      += null == sa._sick ? 0 : sa._sick.intValue();
                    rawSick   += null == sa._rawSick ? 0 : sa._rawSick.intValue();
                    lateEarly += sa._lateearly;
                    absent    += sa._absent;
                    mourning  += sa._mourning;
                    suspend   += sa._suspend;
                    koudome   += sa._koudome;
                    virus     += sa._virus;
                    // log.fatal(" NASI : subclasscd = " + subclassCd + ", sick = " + sick + ", rawSick = " + rawSick + ", lateearly = " + lateEarly + ", absent = " + absent + ", mourning = " + mourning + ", suspend = " + suspend + ", koudome = " + koudome + ", virus = " + virus);
                } else if (hasGappeiSubclasscd && subclassCd.equals(minSubclassCd) && (null != si && SubclassInfo.GAPPEI_SAKI == si._replaceflg)) { // 合併先科目
                    sick      += null == sa._replacedSick ? 0 : sa._replacedSick.intValue();
                    rawSick   += null == sa._rawReplacedSick ? 0 : sa._rawReplacedSick.intValue();
                    lateEarly += sa._lateearly;
                    absent    += sa._absent;
                    mourning  += sa._mourning;
                    suspend   += sa._suspend;
                    koudome   += sa._koudome;
                    virus     += sa._virus;
                    // log.fatal(" SAKI : subclasscd = " + subclassCd + ", sick = " + sick + ", rawSick = " + rawSick + ", lateearly = " + lateEarly + ", absent = " + absent + ", mourning = " + mourning + ", suspend = " + suspend + ", koudome = " + koudome + ", virus = " + virus);
                } else if (null != si && SubclassInfo.GAPPEI_MOTO == si._replaceflg) { // 合併元科目
                }
            }
            final BigDecimal _null = new BigDecimal(0);
            final SubclassAttendance sa = new SubclassAttendance(_null, new BigDecimal(rawSick), new BigDecimal(sick), new BigDecimal(absent), new BigDecimal(suspend), new BigDecimal(koudome), new BigDecimal(virus), new BigDecimal(mourning), new BigDecimal(lateEarly), _null, _null, _null);
            sa._subclassCd = minSubclassCd;
            return sa;
        }

        protected boolean isGakunenHyouka(final String semTestKindCd) {
            return Param._99900.equals(semTestKindCd) && "2".equals(_param._gakunenmatuRan);
        }

        protected void printScoreRank(final Vrw32alpWrap svf, final Student student) {
            if (_fieldScoreRank != null) {
                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int k = 0; k < semTestKindCds.length; k++) {
                    final ScoreRank sr;
                    if (isGakunenHyouka(semTestKindCds[k])) {
                        sr = student.getScoreRank(Param._99901, Param.DIV99901);
                    } else {
                        sr = student.getScoreRank(semTestKindCds[k]);
                    }
                    if (sr != null && NumberUtils.isNumber(sr._avgScore)) {
                        // log.debug(" score rank = " + sr);
                        String avgScore = new BigDecimal(sr._avgScore).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        svf.VrsOut(_fieldScoreRank + getFieldColumnTestKind(semTestKindCds[k]), avgScore);
                    }
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

            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(subclass._subclassCd), sick, _fieldKamokuKetsuji, _fieldKamokuLateEarly);

            String rawSick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getRawReplacedSick() : sa.getRawSick();


            svf.VrsOut(_fieldKamokuKetsuji, rawSick == null ? "" : String.valueOf(rawSick));
            // svf.VrsOut(_fieldKamokuKekka, sick == null ? "" : String.valueOf(sick));
            svf.VrsOut(_fieldKamokuLateEarly, sa.getLateEarly());
            svf.VrsOut(_fieldKamokuKouketsu, sa.getKoketsu());
            svf.VrsOut(_fieldKamokuMourning, sa.getMourning());
            svf.VrsOut(_fieldKamokuSuspend, sa.getSuspend());
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
            final String[] testKindCds = _param.getTargetTestKindCds();
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
    }

    private static class KNJD181A extends KNJD181Base {

        private static final int MAX_RECORD = 17;

        private static final String FORM1  = "KNJD181A_1";   // 住所左 総合的な学習の時間有
        private static final String FORM2  = "KNJD181A_2";   // 住所右 総合的な学習の時間有
        private static final String FORM3  = "KNJD181A_3";   // 住所左 総合的な学習の時間無
        private static final String FORM4  = "KNJD181A_4";   // 住所右 総合的な学習の時間無
        private static final String FORM12 = "KNJD181A_1_2"; // 住所左 総合的な学習の時間有 SHR枠有
        private static final String FORM22 = "KNJD181A_2_2"; // 住所右 総合的な学習の時間有 SHR枠有
        private static final String FORM32 = "KNJD181A_3_2"; // 住所左 総合的な学習の時間無 SHR枠有
        private static final String FORM42 = "KNJD181A_4_2"; // 住所右 総合的な学習の時間無 SHR枠有

        private static final String FORM13 = "KNJD181A_1_3"; // 住所左 総合的な学習の時間広
        private static final String FORM14 = "KNJD181A_1_4"; // 住所左 総合的な学習の時間広 SHR枠有
        private static final String FORM23 = "KNJD181A_2_3"; // 住所右 総合的な学習の時間広
        private static final String FORM24 = "KNJD181A_2_4"; // 住所右 総合的な学習の時間広 SHR枠有

        public KNJD181A(DB2UDB db2, Vrw32alpWrap svf, KNJD181.Param param) {
            super(db2, svf, param);

            _fieldScoreRank = "GRADE_AVE";
            _fieldKamokuKetsuji = "ABSENCE";
            _fieldKamokuLateEarly = "EARLY1";
            _fieldKamokuKouketsu = "PUB_KEKKA1";
            _fieldKamokuMourning = "MOURNING1";
            _fieldKamokuSuspend = "SUSPEND1";

            _sogakuBefore = "総　合　的　な　学　習　の　時　間";
            _sogakuAfter  = "総　合　的　な　探　究　の　時　間";
        }

        public void print(final Student student) {

            log.debug(" student = " + student + "(" + student._attendNo + ")");

            _amikakeMap = new HashMap();

            final String formName = getFormName(student);
            log.debug(" FORM = " + formName);

            setForm(formName, student.isTankyu(_param));

            printHeader(_svf, student);

            printAddress(_svf, student);

            printTotalStudy(_svf, student);

            printTsushinran(_svf, student);

            printBiko(_svf, student);

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

        private String getFormName(final Student student) {
            String formName;
            if (null != _param._kobetuPrintShrOrLateearly) {
                if ("1".equals(_param._formSelect)) {
                    formName = _param._isNoSogo ? FORM42 : "1".equals(_param._tutisyoTotalstudyWideForm) ? FORM24 : FORM22;
                } else {
                    formName = _param._isNoSogo ? FORM32 : "1".equals(_param._tutisyoTotalstudyWideForm) ? FORM14 : FORM12;
                }
            } else {
                if ("1".equals(_param._formSelect)) {
                    formName = _param._isNoSogo ? FORM4 : "1".equals(_param._tutisyoTotalstudyWideForm) ? FORM23 : FORM2;
                } else {
                    formName = _param._isNoSogo ? FORM3 : "1".equals(_param._tutisyoTotalstudyWideForm) ? FORM13 : FORM1;
                }
            }
            if (_param._isNotPrintCommunication) {
                formName += "_NC";
            }
            formName += ".frm";
            return formName;
        }

        private void printBiko(final Vrw32alpWrap svf, final Student student) {
            final String[] communication = get_token(student._recordDocumentKindDatFootnote, 100, 3);
            for (int i = 0; i < communication.length; i++) {
                svf.VrsOut("COMMUNICATION" + (i + 1), communication[i]);
            }
        }

        /**
         * 通信欄を印字
         * @param svf
         * @param student
         */
        private void printTsushinran(final Vrw32alpWrap svf, final Student student) {
            HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(_param.isGakunenmatsu() ? _param.getMaxSemester() : _param._semester);
            if (null != reportRemark) {
                final String[] corre = get_token(reportRemark._communication, 60, 5);
                for (int i = 0; i < corre.length; i++) {
                    svf.VrsOut("CORRE" + (i + 1), corre[i]);
                }
            }
        }

        /**
         * ヘッダ印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alpWrap svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);

            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }

            svf.VrsOut("PRESIDENT", _param._principalName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0));
            }
            if (_param._subStaffNames != null && _param._subStaffNames.size() > 0 && !"1".equals(_param._KNJD181_HideSubTr)) {
                svf.VrsOut("TEACHER2", (String) _param._subStaffNames.get(0));
            }

            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("HR_NAME",  student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);
        }

        /**
         * 総合的な学習の時間・出欠席を印字
         * @param svf
         * @param student
         */
        private void printTotalStudy(final Vrw32alpWrap svf, final Student student) {
            RecordTotalStudyTimeDat recordTotalStudy = (RecordTotalStudyTimeDat) student._recordTotalStudieTimeDat.get(Param.SEMEALL);
            if (null != recordTotalStudy) {
                final int contentLines = "1".equals(_param._tutisyoTotalstudyWideForm) ? 3 : 2;
                String[] spContent = get_token(recordTotalStudy._totalStudyAct, 50, contentLines);
                for (int i = 0; i < spContent.length; i++) {
                    svf.VrsOut("SP_CONENT" + (i + 1), spContent[i]);
                }
                final int evaLines = "1".equals(_param._tutisyoTotalstudyWideForm) ? 5 : 3;
                String[] spEva = get_token(recordTotalStudy._totalStudyTime, 50, evaLines);
                for (int i = 0; i < spEva.length; i++) {
                    svf.VrsOut("SP_EVA" + (i + 1), spEva[i]);
                }
            }
            final SubclassAttendance sa = getSubclassAttendance90(student, Param.SEMEALL);
            final String minSubclassCd = sa._subclassCd;
            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(minSubclassCd), sa.getSick() == null || "".equals(sa.getSick()) ? "0" : sa.getSick(), "SP_ABSENCE", "SP_EARLY");
            svf.VrsOut("SP_ABSENCE", sa.getRawSick() == null || "".equals(sa.getRawSick()) ? "0" : sa.getRawSick());
            svf.VrsOut("SP_EARLY", sa.getLateEarly() == null || "".equals(sa.getLateEarly()) ? "0" : sa.getLateEarly());
            svf.VrsOut("SP_PUB_ABSENCE", sa.getKoketsu() == null || "".equals(sa.getKoketsu()) ? "0" : sa.getKoketsu());
            svf.VrsOut("SP_MOURNING", sa.getMourning() == null || "".equals(sa.getMourning()) ? "0" : sa.getMourning());
            svf.VrsOut("SP_SUSPEND", sa.getSuspend() == null || "".equals(sa.getSuspend()) ? "0" : sa.getSuspend());
        }

        /**
         * 宛先を印字
         * @param svf
         * @param student
         */
        private void printAddress(final Vrw32alpWrap svf, final Student student) {
            if (student._address != null) {
                boolean use2 = false;
                boolean use3 = false;
                boolean useNameBig = false;
                final String addr1 = student._address._address1;
                final String addr2 = student._address._address2;
                final String addressee = student._address._addressee == null ? "" : student._address._addressee + "  様";
                try {
                    int check1 = addr1 == null ? 0 : addr1.getBytes("MS932").length;
                    int check2 = addr2 == null ? 0 : addr2.getBytes("MS932").length;
                    use3 = "1".equals(_param._useAddrField2) && (check1 > 50 || check2 > 50);
                    use2 = check1 > 40 || check2 > 40;
                    int check3 = addressee == null ? 0 : addressee.getBytes("MS932").length;
                    useNameBig = check3 > 24;
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
                if (!_param._isNotPrintAddressZipcd) {
                    svf.VrsOut(use3 ? "ADDR1_3" : use2 ? "ADDR1_2" : "ADDR1", addr1);     //住所
                    svf.VrsOut(use3 ? "ADDR2_3" : use2 ? "ADDR2_2" : "ADDR2", addr2);     //住所
                    svf.VrsOut("ZIPCD", student._address._zipcd);
                }
                svf.VrsOut(useNameBig ? "ADDRESSEE2" : "ADDRESSEE", addressee);
            }
            svf.VrsOut("HR_ATTNO_NAME", student._hrNameAbbv + "　" + student._attendNo + (_param._isNotPrintStudentNameWhenAddresseeIs2 ? "" : "　" + student._name));
        }

        private int printRecDetail (
                final Vrw32alpWrap svf,
                final Student student
        ) {
            boolean hasData = false;
            int i = 0;

            boolean bsubclass90 = false;

            printAttendance(svf, student);
            for (Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                printAttendance(svf, student);
                printScoreRank(svf, student);

                if (_param.isGakunenmatsu()) {
                    _svf.VrsOut("GETCREDIT", String.valueOf(student._totalCredit));
                }

                SubclassInfo info = (SubclassInfo) it.next();

                if (info.isNotPrintSubclassList(_param)) { continue; }

                if (hasData) {
                    svf.VrEndRecord();
                    i++;
                }
                printComment(svf);

                if ((is90(info._subclassCd) || info._num90Other != null) && ! bsubclass90) {
                    if (!_param._isNoBlank90) {
                        i += blankRecord(svf, info._num90, i, MAX_RECORD, "CLASS");
                        printComment(svf);
                    }
                   bsubclass90 = true;
                }

                hasData= printSubclassInfo(svf, info, student, (i + 1));
            }
            return i;
        }

        private void printAttendance(final Vrw32alpWrap svf, final Student student) {
            final String nameField1 = "REC_LHR_ABSENCE_NAME";
            final String nameField2 = "REC_ASSEMBLY_ABSENCE_NAME";
            final String nameField3 = "ABSENCE_NAME";
            final String valField1 = "REC_LHR_ABSENCE";
            final String valField2 = "REC_ASSEMBLY_ABSENCE";
            final String valField3 = "PERIOD_SHR_ABSENCE";

            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                svf.VrsOut(nameField1, "LHRの欠席時数");
                svf.VrsOut(nameField2, "学校行事の欠席時数");
                if ("2".equals(_param._kobetuPrintShrOrLateearly)) {
                    svf.VrsOut(nameField3, "遅刻・早退数");
                } else {
                    svf.VrsOut(nameField3, "SHRの欠席時数");
                }
            } else {
                svf.VrsOut(nameField1, "特別活動欠課時数");
                if ("1".equals(_param._gassanPrintLateearly)) {
                    svf.VrsOut(nameField2, "遅刻・早退数");
                }
            }

            Attendance sum = student.getAttendance(Param.SEMEALL);

            svf.VrsOut("REC_LESSON" , String.valueOf(sum._lesson));
            svf.VrsOut("REC_MOURNING" , String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
            svf.VrsOut("REC_PRESENT", String.valueOf(sum._mlesson));
            svf.VrsOut("REC_ABSENCE", String.valueOf(sum._absence));
            svf.VrsOut("REC_ATTEND" , String.valueOf(sum._attend));

            String[] spGroupCd = new String[]{Attendance.GROUP_LHR, Attendance.GROUP_SHR, Attendance.GROUP_ASS};
            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                String[] field = new String[]{valField1, valField3, valField2};
                for (int i = 0; i < spGroupCd.length; i++) {

                    final BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                    svf.VrsOut(field[i], String.valueOf(spGroupKomaKekka));

                    if (_param._isPrintAbsenceHighNote) {
                        final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd[i]);
                        setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekka.setScale(0, BigDecimal.ROUND_HALF_UP)), field[i]);
                    }
                }
                if ("2".equals(_param._kobetuPrintShrOrLateearly)) { // 遅刻・早退を表示
                    svf.VrsOut(valField3, String.valueOf(sum._late + sum._leave));
                }
            } else if (Param.SPECIAL_ACT_GASSAN.equals(_param._printSpecialAct)) {
                BigDecimal spGroupKekkaTotal = new BigDecimal(0);
                int spGroupKomaKekkaTotal = 0;
                for (int i = 0; i < spGroupCd.length; i++) {
                    final BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                    spGroupKekkaTotal = spGroupKekkaTotal.add(spGroupKekka);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                    spGroupKomaKekkaTotal += spGroupKomaKekka;
                }
                svf.VrsOut(valField1, String.valueOf(spGroupKomaKekkaTotal));
                if (_param._isPrintAbsenceHighNote) {
                    final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(Attendance.GROUP_ALL);
                    setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekkaTotal.setScale(0, BigDecimal.ROUND_HALF_UP)), valField1);
                }
                if ("1".equals(_param._gassanPrintLateearly)) { // 遅刻・早退を表示
                    svf.VrsOut(valField2, String.valueOf(sum._late + sum._leave));
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
            svf.VrsOut("SUBCLASS" + (si._subclassname != null && si._subclassname.length() > 13 ? "2" : "1"),  si._subclassname);

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOut("CREDIT", si._credits);
            }

            Subclass subclass = student.getSubclass(si._subclassCd);
            if (subclass == null) {
                return true;
            }

            if (SubclassInfo.GAPPEI_NASI == si._replaceflg  || SubclassInfo.GAPPEI_MOTO == si._replaceflg  || SubclassInfo.GAPPEI_SAKI == si._replaceflg) {
                // log.debug(" 科目= " + si._subclassname + " ( " + si._subclassCd + " )");

                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String fieldGrading = "GRADE" + getFieldColumnTestKind(semTestKindCd);
                    final ScoreValue sv;
                    final boolean isSlump;
                    if (isGakunenHyouka(semTestKindCd)) {
                        sv = subclass.getScoreValue(Param._99901, Param.DIV99901);
                        isSlump = student.isSlump(subclass._subclassCd, Param._99901);
                    } else {
                        sv = subclass.getScoreValue(semTestKindCd);
                        isSlump = student.isSlump(subclass._subclassCd, semTestKindCd);
                    }
                    if (!isSubclassCdSogotekinaGakushunoJikan(subclass) && isSlump) {
                        svf.VrAttribute(fieldGrading,  "Paint=(1,50,1),Bold=1");
                    }
                    if (sv != null) {
                        // log.debug(" semTestKindCd = " + semTestKindCd + " "+ sv);

                        if (sv._value != null) {
                            if (isCharacter(sv._value)) {
                                svf.VrAttribute(fieldGrading, "Hensyu=3");
                            }
                            if (!(isSubclassCdSogotekinaGakushunoJikan(subclass))) {
                                svf.VrsOut(fieldGrading, sv._value);
                            }
                            //log.debug("   field = " + fieldGrading + " , " + sv._value);
                        }
                    }

                }
                printSubclassAttendance(svf, si, student, si._replaceflg, subclass, subclass.getAttendance(Param.SEMEALL));
            }
            return true;
        }

    }

    private static class KNJD181B extends KNJD181Base {

        private static final int MAX_RECORD = 17;

        public KNJD181B(DB2UDB db2, Vrw32alpWrap svf, Param param) {
            super(db2, svf, param);

            _fieldScoreRank = "AVE_RATE";

            _fieldKamokuKetsuji = "ABSENCE";
            _fieldKamokuLateEarly = "LATE";
            _fieldKamokuKouketsu = "PUB_ABSENCE";
            _fieldKamokuMourning = "MOURNING";
            _fieldKamokuSuspend = "SUSPEND";

            _sogakuBefore = "総合的な学習の時間";
            _sogakuAfter  = "総合的な探究の時間";
        }

        public void print(final Student student) {

            printHyoshi(_svf, student);

            final String formName = getFormName();
            log.debug(" form = " + formName);
            setForm(formName, student.isTankyu(_param));

            printHeader(_svf, student);

            printTotalStudy(_svf, student);

            printTushinran(_svf, student);

            printBiko(_svf, student);

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

        /**
         * フォーム名を得る
         * @return
         */
        private String getFormName() {

            final String FORM2 = "KNJD181B_2"; // 総合学習あり SHR枠あり 校長枠あり
            final String FORM4 = "KNJD181B_4"; // 総合学習なし SHR枠あり 校長枠あり
            final String FORM22 = "KNJD181B_2_2"; // 総合学習あり SHR枠あり 校長枠なし
            final String FORM42 = "KNJD181B_4_2"; // 総合学習なし SHR枠あり 校長枠なし

            final String FORM5 = "KNJD181B_5"; // 総合学習あり SHR枠なし 校長枠なし
            final String FORM6 = "KNJD181B_6"; // 総合学習なし SHR枠なし 校長枠なし
            final String FORM52 = "KNJD181B_5_2"; // 総合学習あり SHR枠なし 校長枠あり
            final String FORM62 = "KNJD181B_6_2"; // 総合学習なし SHR枠なし 校長枠あり

            String formName;
            if (null != _param._kobetuPrintShrOrLateearly) {
                if (_param._isPrintPrincipalMark) {
                    formName = _param._isNoSogo ? FORM4 : FORM2;
                } else {
                    formName = _param._isNoSogo ? FORM42 : FORM22;
                }
            } else {
                if (!_param._isPrintPrincipalMark) {
                    formName = _param._isNoSogo ? FORM6 : FORM5;
                } else {
                    formName = _param._isNoSogo ? FORM62 : FORM52;
                }
            }
            if (_param._isNotPrintCommunication) {
                formName += "_NC";
            }
            formName += ".frm";
            return formName;
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
            final String FORM1 = "KNJD181B_1.frm";
            svf.VrSetForm(FORM1, 1);

            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("SEMESTER", _param.getSemesterName(_param._semester));
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("STAFFNAME1", _param._principalName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0));
            }
            if (_param._subStaffNames != null && _param._subStaffNames.size() > 0 && !"1".equals(_param._KNJD181_HideSubTr)) {
                svf.VrsOut("TEACHER2", (String) _param._subStaffNames.get(0));
            }
            svf.VrsOut("COURSE",   student._courseName);
            svf.VrsOut("MAJOR",  student._majorName);
            svf.VrsOut("HR_NAME", student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            if (getMS932ByteLength(student._name) > 14) {
                svf.VrsOut("NAME2",    student._name);
            } else {
                svf.VrsOut("NAME",     student._name);
            }

            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }
            svf.VrEndPage();
        }

        /**
         * ヘッダを印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alpWrap svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);

            svf.VrsOut("PRESIDENT", _param._principalName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0));
            }
            if (_param._subStaffNames != null && _param._subStaffNames.size() > 0 && !"1".equals(_param._KNJD181_HideSubTr)) {
                svf.VrsOut("TEACHER2", (String) _param._subStaffNames.get(0));
            }

            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("COURSE",   student._courseCodeName);
            svf.VrsOut("HR_NAME",  student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);
        }

        /**
         * 通信欄を印字
         * @param svf
         * @param student
         */
        private void printTushinran(final Vrw32alpWrap svf, final Student student) {
            final String[] hreportRemarkSemester = new String[]{"1", "2", "3"};
            String[] semesters = new String[3];
            int j = 0;
            for (int i = 0; i < hreportRemarkSemester.length; i++) {
                if (_param._semester.compareTo(hreportRemarkSemester[i]) >= 0) {
                    semesters[j] = hreportRemarkSemester[i];
                    j++;
                }
            }
            for (int k = 0; k < semesters.length; k++) {
                if (semesters[k] == null) { continue; }

                HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(semesters[k]);
                if (null != reportRemark) {
                    String[] totalStudy = get_token(reportRemark._communication, 60, 5);
                    for (int i = 0; i < totalStudy.length; i++) {
                        svf.VrsOutn("TOTAL_STUDY" + (i + 1), Integer.parseInt(semesters[k]), totalStudy[i]);
                    }
                }
            }
        }

        /**
         * 備考を印字
         * @param svf
         * @param student
         */
        private void printBiko(final Vrw32alpWrap svf, final Student student) {
            final String[] communication = get_token(student._recordDocumentKindDatFootnote, 50, 6);
            for (int i = 0; i < communication.length; i++) {
                svf.VrsOut("COMMUNICATION" + (i + 1), communication[i]);
            }
        }

        /**
         * 総合的な学習の時間・出欠席を印字
         * @param svf
         * @param student
         */
        private void printTotalStudy(final Vrw32alpWrap svf, final Student student) {
            RecordTotalStudyTimeDat recordTotalStudy = (RecordTotalStudyTimeDat) student._recordTotalStudieTimeDat.get(Param.SEMEALL);
            if (null != recordTotalStudy) {
                final String[] spContent = get_token(recordTotalStudy._totalStudyAct, 50, 2);
                for (int i = 0; i < spContent.length; i++) {
                    svf.VrsOut("SP_CONENT" + (i + 1), spContent[i]);
                }
                final String[] spEva = get_token(recordTotalStudy._totalStudyTime, 50, 3);
                for (int i = 0; i < spEva.length; i++) {
                    svf.VrsOut("SP_EVA" + (i + 1), spEva[i]);
                }
            }

            SubclassAttendance sa = getSubclassAttendance90(student, Param.SEMEALL);
            final String minSubclassCd = sa._subclassCd;
            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(minSubclassCd), sa.getSick() == null || "".equals(sa.getSick()) ? "0" : sa.getSick(), "SP_ABSENCE", "SP_LATE");
            svf.VrsOut("SP_ABSENCE", sa.getRawSick() == null || "".equals(sa.getRawSick()) ? "0" : sa.getRawSick());
            svf.VrsOut("SP_LATE", sa.getLateEarly() == null || "".equals(sa.getLateEarly()) ? "0" : sa.getLateEarly());
            svf.VrsOut("SP_PUB_ABSENCE", sa.getKoketsu() == null || "".equals(sa.getKoketsu()) ? "0" : sa.getKoketsu());
            svf.VrsOut("SP_MOURNING", sa.getMourning() == null || "".equals(sa.getMourning()) ? "0" : sa.getMourning());
            svf.VrsOut("SP_SUSPEND", sa.getSuspend() == null || "".equals(sa.getSuspend()) ? "0" : sa.getSuspend());
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
            printComment(svf);
            for (Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
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
                printComment(svf);

                if ((is90(info._subclassCd) || info._num90Other != null) && ! bsubclass90) {
                    if (!_param._isNoBlank90) {
                        i += blankRecord(svf, info._num90, i, MAX_RECORD, "CLASS");
                        printComment(svf);
                    }
                   bsubclass90 = true;
                }

                hasData = printSubclassInfo(svf, info, student, (i + 1));
            }
            return i;
        }

        private void printAttendData(final Vrw32alpWrap svf, final Student student) {
            final String nameField1 = "REC_LHR_ABSENCE_NAME";
            final String nameField2 = "REC_ASSEMBLY_ABSENCE_NAME";
            final String nameField3 = "ABSENCE_NAME";
            final String valField1 = "PERIOD_LHR_ABSENCE";
            final String valField2 = "PERIOD_ASSEMBLY_ABSENCE";
            final String valField3 = "PERIOD_SHR_ABSENCE";

            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                svf.VrsOut(nameField1, "LHRの欠席時数");
                svf.VrsOut(nameField2, "学校行事の欠席時数");
                if ("2".equals(_param._kobetuPrintShrOrLateearly)) {
                    svf.VrsOut(nameField3, "遅刻・早退数");
                } else {
                    svf.VrsOut(nameField3, "SHRの欠席時数");
                }
            } else {
                svf.VrsOut(nameField1, "特別活動欠課時数");
                if ("1".equals(_param._gassanPrintLateearly)) {
                    svf.VrsOut(nameField2, "遅刻・早退数");
                }
            }

            String[] targetTestKindCds = _param.getTargetSemester();
            for (int j = 0; j < targetTestKindCds.length; j++) {
                String semester = targetTestKindCds[j];

                final Attendance sum = student.getAttendance(semester);
                final int k = getFieldColumnSemester(semester);
                svf.VrsOutn("PERIOD_LESSON" , k, String.valueOf(sum._lesson));
                svf.VrsOutn("PERIOD_SUSPEND", k, String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
                svf.VrsOutn("PERIOD_PRESENT", k, String.valueOf(sum._mlesson));
                svf.VrsOutn("PERIOD_ABSENCE", k, String.valueOf(sum._absence));
                svf.VrsOutn("PERIOD_ATTEND" , k, String.valueOf(sum._attend));

                String[] spGroupCd = new String[]{Attendance.GROUP_LHR, Attendance.GROUP_SHR, Attendance.GROUP_ASS};
                if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                    String[] field = new String[]{valField1, valField3, valField2};
                    for (int i = 0; i < spGroupCd.length; i++) {

                        final BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                        int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                        svf.VrsOutn(field[i], k, String.valueOf(spGroupKomaKekka));

                        if (_param._isPrintAbsenceHighNote) {
                            final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd[i]);
                            setSpecialAbsenceFieldAttributen(svf, ah, String.valueOf(spGroupKekka.setScale(0, BigDecimal.ROUND_HALF_UP)), field[i], k);
                        }
                    }
                    if ("2".equals(_param._kobetuPrintShrOrLateearly)) { // 遅刻・早退を表示
                        svf.VrsOutn(valField3, k, String.valueOf(sum._late + sum._leave));
                    }
                } else if (Param.SPECIAL_ACT_GASSAN.equals(_param._printSpecialAct)) {
                    BigDecimal spGroupKekkaTotal = new BigDecimal(0);
                    int spGroupKomaKekkaTotal = 0;
                    for (int i = 0; i < spGroupCd.length; i++) {
                        final BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                        spGroupKekkaTotal = spGroupKekkaTotal.add(spGroupKekka);
                        int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                        spGroupKomaKekkaTotal += spGroupKomaKekka;
                    }
                    svf.VrsOutn(valField1, k, String.valueOf(spGroupKomaKekkaTotal));
                    if (_param._isPrintAbsenceHighNote) {
                        final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(Attendance.GROUP_ALL);
                        setSpecialAbsenceFieldAttributen(svf, ah, String.valueOf(spGroupKekkaTotal.setScale(0, BigDecimal.ROUND_HALF_UP)), valField1, k);
                    }
                    if ("1".equals(_param._gassanPrintLateearly)) { // 遅刻・早退を表示
                        svf.VrsOutn(valField2, k, String.valueOf(sum._late + sum._leave));
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
            svf.VrsOut("SUBCLASS",  si._subclassname);

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOut("CREDIT", si._credits);
            }

            Subclass subclass = student.getSubclass(si._subclassCd);
            if (subclass == null) {
                return true;
            }

            if (SubclassInfo.GAPPEI_NASI == si._replaceflg  || SubclassInfo.GAPPEI_MOTO == si._replaceflg  || SubclassInfo.GAPPEI_SAKI == si._replaceflg) {

                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String fieldGrading = "RATE" + getFieldColumnTestKind(semTestKindCd);
                    final ScoreValue sv;
                    final boolean isSlump;
                    if (isGakunenHyouka(semTestKindCd)) {
                        sv = subclass.getScoreValue(Param._99901, Param.DIV99901);
                        isSlump = student.isSlump(subclass._subclassCd, Param._99901);
                    } else {
                        sv = subclass.getScoreValue(semTestKindCd);
                        isSlump = student.isSlump(subclass._subclassCd, semTestKindCd);
                    }
                    if (!isSubclassCdSogotekinaGakushunoJikan(subclass) && isSlump) {
                        svf.VrAttribute(fieldGrading,  "Paint=(1,50,1),Bold=1");
                    }
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

                }
                printSubclassAttendance(svf, si, student, si._replaceflg, subclass, subclass.getAttendance(Param.SEMEALL));
            }

            return true;
        }
    }

    private static class KNJD181C extends KNJD181Base {

        private static final int MAX_RECORD = 17;

        private static final String FORM = "KNJD181C";

        /** 総合的な学習の時間欄無 */
        private static final String FORM_NOSOGO = "KNJD181C_3";

        public KNJD181C(DB2UDB db2, Vrw32alpWrap svf, KNJD181.Param param) {
            super(db2, svf, param);

            _sogakuBefore = "総　合　的　な　学　習　の　時　間";
            _sogakuAfter  = "総　合　的　な　探　究　の　時　間";
        }

        public void print(final Student student) {

            log.debug(" student = " + student + "(" + student._attendNo + ")");

            _amikakeMap = new HashMap();

            final String formName = getFormName();
            log.debug(" FORM = " + formName);

            setForm(formName, student.isTankyu(_param));

            printHeader(_svf, student);

            printAddress(_svf, student);

            printTotalStudy(_svf, student);

            printTushinran(_svf, student);

            printBiko(_svf, student);

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

        private String getFormName() {
            String formName;
            if (_param._isNoSogo) {
                formName = FORM_NOSOGO;
            } else {
                formName = FORM;
            }
            if (_param._isNotPrintCommunication) {
                formName += "_NC";
            }
            formName += ".frm";
            return formName;
        }

        /**
         * 通信欄を印字
         * @param svf
         * @param student
         */
        private void printTushinran(final Vrw32alpWrap svf, final Student student) {
            final HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(_param.isGakunenmatsu() ? _param.getMaxSemester() : _param._semester);
            if (null != reportRemark) {
                final String[] communication1 = get_token(reportRemark._communication, 60, 5);
                for (int i = 0; i < communication1.length; i++) {
                    svf.VrsOut("COMMUNICATION1" + (i + 1), communication1[i]);
                }
            }
        }

        /**
         * 備考を印字
         * @param svf
         * @param student
         */
        private void printBiko(final Vrw32alpWrap svf, final Student student) {
            final String[] communication = get_token(student._recordDocumentKindDatFootnote, 100, 3);
            for (int i = 0; i < communication.length; i++) {
                svf.VrsOut("COMMUNICATION" + (i + 1), communication[i]);
            }
        }

        /**
         * ヘッダを印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alpWrap svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);

            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }
            svf.VrsOut("PRESIDENT", _param._principalName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0));
            }
            if (_param._subStaffNames != null && _param._subStaffNames.size() > 0 && !"1".equals(_param._KNJD181_HideSubTr)) {
                svf.VrsOut("TEACHER2", (String) _param._subStaffNames.get(0));
            }
            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("COURSE",   student._courseCodeName);
            svf.VrsOut("HR_NAME",  student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);

        }

        /**
         * 住所を印字
         * @param svf
         * @param student
         */
        private void printAddress(final Vrw32alpWrap svf, final Student student) {
            if (student._address != null) {
                boolean use2 = false;
                boolean use3 = false;
                boolean useNameBig = false;
                final String addr1 = student._address._address1;
                final String addr2 = student._address._address2;
                final String addressee = student._address._addressee == null ? "" : student._address._addressee + "  様";
                try {
                    int check1 = addr1 == null ? 0 : addr1.getBytes("MS932").length;
                    int check2 = addr2 == null ? 0 : addr2.getBytes("MS932").length;
                    use3 = "1".equals(_param._useAddrField2) && (check1 > 50 || check2 > 50);
                    use2 = check1 > 40 || check2 > 40;
                    int check3 = addressee == null ? 0 : addressee.getBytes("MS932").length;
                    useNameBig = check3 > 24;
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
                if (!_param._isNotPrintAddressZipcd) {
                    svf.VrsOut(use3 ? "ADDR1_3" : use2 ? "ADDR1_2" : "ADDR1", addr1);     //住所
                    svf.VrsOut(use3 ? "ADDR2_3" : use2 ? "ADDR2_2" : "ADDR2", addr2);     //住所
                    svf.VrsOut("ZIPCD", student._address._zipcd);
                }
                svf.VrsOut(useNameBig ? "ADDRESSEE2" : "ADDRESSEE", addressee);
            }
            svf.VrsOut("HR_ATTNO_NAME", student._hrNameAbbv + "　" + student._attendNo + (_param._isNotPrintStudentNameWhenAddresseeIs2 ? "" : "　" + student._name));
        }

        /**
         * 総合的な学習の時間・出欠席を印字
         * @param svf
         * @param student
         */
        private void printTotalStudy(final Vrw32alpWrap svf, final Student student) {
            final RecordTotalStudyTimeDat recordTotalStudy = (RecordTotalStudyTimeDat) student._recordTotalStudieTimeDat.get(Param.SEMEALL);
            if (null != recordTotalStudy) {
                String[] spContent = get_token(recordTotalStudy._totalStudyAct, 50, 2);
                for (int i = 0; i < spContent.length; i++) {
                    svf.VrsOut("SP_CONENT" + (i + 1), spContent[i]);
                }
                String[] spEva = get_token(recordTotalStudy._totalStudyTime, 50, 3);
                for (int i = 0; i < spEva.length; i++) {
                    svf.VrsOut("SP_EVA" + (i + 1), spEva[i]);
                }
            }
            final SubclassAttendance sa = getSubclassAttendance90(student, Param.SEMEALL);
            final String minSubclassCd = sa._subclassCd;
            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(minSubclassCd), sa.getSick() == null || "".equals(sa.getSick()) ? "0" : sa.getSick(), "SP_ABSENCE", "SP_EARLY");
            svf.VrsOut("SP_ABSENCE", sa.getRawSick() == null || "".equals(sa.getRawSick()) ? "0" : sa.getRawSick());
            svf.VrsOut("SP_EARLY", sa.getLateEarly() == null || "".equals(sa.getLateEarly()) ? "0" : sa.getLateEarly());
            svf.VrsOut("SP_PUB_ABSENCE", sa.getKoketsu() == null || "".equals(sa.getKoketsu()) ? "0" : sa.getKoketsu());
            svf.VrsOut("SP_MOURNING", sa.getMourning() == null || "".equals(sa.getMourning()) ? "0" : sa.getMourning());
            svf.VrsOut("SP_SUSPEND", sa.getSuspend() == null || "".equals(sa.getSuspend()) ? "0" : sa.getSuspend());
        }

        private int printRecDetail (
                final Vrw32alpWrap svf,
                final Student student
        ) {
            boolean hasData = false;
            int i = 0;

            boolean bsubclass90 = false;

            printAttendData(svf, student);
            printComment(svf);
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();

                printAttendData(svf, student);

                if (_param.isGakunenmatsu()) {
                    _svf.VrsOut("GETCREDIT", String.valueOf(student._totalCredit));
                }

                if (info.isNotPrintSubclassList(_param)) { continue; }

                if (hasData) {
                    svf.VrEndRecord();
                    i++;
                }
                printComment(svf);

                if ((is90(info._subclassCd) || info._num90Other != null) && ! bsubclass90) {
                    if (!_param._isNoBlank90) {
                        i += blankRecord(svf, info._num90, i, MAX_RECORD, "CLASS");
                        printComment(svf);
                    }
                   bsubclass90 = true;
                }
                hasData= printSubclassInfo(svf, info, student, (i + 1));
            }
            return i;
        }

        private void printAttendData(final Vrw32alpWrap svf, final Student student) {
            final String nameField1 = "REC_LHR_ABSENCE_NAME";
            final String nameField2 = "REC_ASSEMBLY_ABSENCE_NAME";
            final String valField1 = "REC_LHR_ABSENCE";
            final String valField2 = "REC_ASSEMBLY_ABSENCE";
            final Attendance sum = student.getAttendance(Param.SEMEALL);

            svf.VrsOut("REC_LESSON" , String.valueOf(sum._lesson));
            svf.VrsOut("REC_MOURNING" , String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
            svf.VrsOut("REC_PRESENT", String.valueOf(sum._mlesson));
            svf.VrsOut("REC_ABSENCE", String.valueOf(sum._absence));
            svf.VrsOut("REC_ATTEND" , String.valueOf(sum._attend));

            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                svf.VrsOut(nameField1, "LHRの欠席時数");
                svf.VrsOut(nameField2, "学校行事の欠席時数");
            } else {
                svf.VrsOut(nameField1, "特別活動欠課時数");
            }

            final String[] spGroupCd = new String[]{Attendance.GROUP_LHR, Attendance.GROUP_ASS};
            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                final String[] field = new String[]{valField1, valField2};
                for (int i = 0; i < spGroupCd.length; i++) {

                    BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                    svf.VrsOut(field[i], String.valueOf(spGroupKomaKekka));

                    if (_param._isPrintAbsenceHighNote) {
                        final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd[i]);
                        setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekka.setScale(0, BigDecimal.ROUND_HALF_UP)), field[i]);
                    }
                }
            } else if (Param.SPECIAL_ACT_GASSAN.equals(_param._printSpecialAct)) {
                BigDecimal spGroupKekkaTotal = new BigDecimal(0);
                int spGroupKomaKekkaTotal = 0;
                for (int i = 0; i < spGroupCd.length; i++) {
                    BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                    spGroupKekkaTotal = spGroupKekkaTotal.add(spGroupKekka);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                    spGroupKomaKekkaTotal += spGroupKomaKekka;
                }
                svf.VrsOut(valField1, String.valueOf(spGroupKomaKekkaTotal));
                if (_param._isPrintAbsenceHighNote) {
                    final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(Attendance.GROUP_ALL);
                    setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekkaTotal.setScale(0, BigDecimal.ROUND_HALF_UP)), valField1);
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
            svf.VrsOut("SUBCLASS" + (si._subclassname != null && si._subclassname.length() > 13 ? "2" : "1"),  si._subclassname);

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOut("CREDIT", si._credits);
            }
            if (Param.SEMEALL.equals(_param._semester)) {
                svf.VrsOut("GET_CREDIT", si._getCredit);
            }

            final Subclass subclass = student.getSubclass(si._subclassCd);
            if (subclass == null) {
                log.info(" subclass null : subclasscd = " + si._subclassCd + " / " + si._getCredit);
                return true;
            }

            if (SubclassInfo.GAPPEI_NASI == si._replaceflg  || SubclassInfo.GAPPEI_MOTO == si._replaceflg  || SubclassInfo.GAPPEI_SAKI == si._replaceflg) {

                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String j = String.valueOf(getFieldColumnTestKind(semTestKindCd));
                    final String fieldGrading = "RATE" + j;
                    final ScoreValue sv;
                    final boolean isSlump;
                    if (isGakunenHyouka(semTestKindCd)) {
                        sv = subclass.getScoreValue(Param._99901, Param.DIV99901);
                        isSlump = student.isSlump(subclass._subclassCd, Param._99901);
                    } else {
                        sv = subclass.getScoreValue(semTestKindCd);
                        isSlump = student.isSlump(subclass._subclassCd, semTestKindCd);
                    }
                    if (!isSubclassCdSogotekinaGakushunoJikan(subclass) && isSlump) {
                        svf.VrAttribute(fieldGrading,  "Paint=(1,50,1),Bold=1");
                    }
                    if (sv != null && sv._value != null) {
                        log.debug(" semTestKindCd = " + semTestKindCd + " "+ sv);
                        if (isCharacter(sv._value)) {
                            svf.VrAttribute(fieldGrading, "Hensyu=3");
                        }
                        if (!isSubclassCdSogotekinaGakushunoJikan(subclass)) {
                            svf.VrsOut(fieldGrading, sv._value);
                        }
                    }
                    if (!"3".equals(semTestKindCd.substring(0, 1))) {
                        printSubclassAttendance(svf, si, student, si._replaceflg, subclass, j, subclass.getAttendance(semTestKindCd.substring(0, 1)));
                    }
                }
                printSubclassAttendance(svf, si, student, si._replaceflg, subclass, "3", subclass.getAttendance(Param.SEMEALL));
            }
            return true;
        }

        /**
         * @param si
         * @param student
         * @param replaceflg
         * @param subclass
         * @param sa
         */
        private void printSubclassAttendance(final Vrw32alpWrap svf,
                final SubclassInfo si,
                final Student student,
                final int replaceflg,
                Subclass subclass,
                final String j,
                final SubclassAttendance sa) {

            if (sa == null) {
                return;
            }
            String sick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getReplacedSick() : sa.getSick();

//            if (SubclassInfo.GAPPEI_SAKI == replaceflg && SubclassInfo.GAPPEI_TANNI_KASAN.equals(si._calculateCreditFlg) && !_param._isNoPrintMoto) {
//                sick = "0";
//            }
            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(subclass._subclassCd), sick, "ABSENCE" + j, "LATE_EARLY" + j);

            String rawSick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getRawReplacedSick() : sa.getRawSick();

//            if (SubclassInfo.GAPPEI_SAKI == replaceflg && SubclassInfo.GAPPEI_TANNI_KASAN.equals(si._calculateCreditFlg) && !_param._isNoPrintMoto) {
//                rawSick = "0";
//            }

            svf.VrsOut("ABSENCE" + j, rawSick == null ? "" : String.valueOf(rawSick)); // "欠課"欄に欠時数を表示（A・Bパターンは"欠時"、"欠課"各欄に欠時数、欠課数を表示）
            svf.VrsOut("LATE_EARLY" + j, sa.getLateEarly());
            svf.VrsOut("PUB_KEKKA" + j, sa.getKoketsu());
            svf.VrsOut("MOURNING_SUSPEND" + j, sa.getMourningSuspend());
        }
    }

    private static class KNJD181D extends KNJD181Base {

        private static final int MAX_RECORD = 17;

        private static final String FORM = "KNJD181D";

        /** 総合的な学習の時間欄無 */
        private static final String FORM_NOSOGO = "KNJD181D_3";

        public KNJD181D(DB2UDB db2, Vrw32alpWrap svf, KNJD181.Param param) {
            super(db2, svf, param);

            _sogakuBefore = "総　合　的　な　学　習　の　時　間";
            _sogakuAfter  = "総　合　的　な　探　究　の　時　間";
        }

        public void print(final Student student) {

            log.debug(" student = " + student + "(" + student._attendNo + ")");

            _amikakeMap = new HashMap();

            final String formName = getFormName();
            log.debug(" FORM = " + formName);

            setForm(formName, student.isTankyu(_param));

            printHeader(_svf, student);

            printAddress(_svf, student);

            printTotalStudy(_svf, student);

            printTushinran(_svf, student);

            printBiko(_svf, student);

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

        private String getFormName() {
            String formName;
            if (_param._isNoSogo) {
                formName = FORM_NOSOGO;
            } else {
                formName = FORM;
            }
            if (_param._isNotPrintCommunication) {
                formName += "_NC";
            }
            formName += ".frm";
            return formName;
        }

        /**
         * 通信欄を印字
         * @param svf
         * @param student
         */
        private void printTushinran(final Vrw32alpWrap svf, final Student student) {
            final HReportRemark reportRemark  = (HReportRemark) student._hReportRemarks.get(_param.isGakunenmatsu() ? _param.getMaxSemester() : _param._semester);
            if (null != reportRemark) {
                final String[] communication1 = get_token(reportRemark._communication, 60, 5);
                for (int i = 0; i < communication1.length; i++) {
                    svf.VrsOut("COMMUNICATION1" + (i + 1), communication1[i]);
                }
            }
        }

        /**
         * 備考を印字
         * @param svf
         * @param student
         */
        private void printBiko(final Vrw32alpWrap svf, final Student student) {
            final String[] communication = get_token(student._recordDocumentKindDatFootnote, 100, 3);
            for (int i = 0; i < communication.length; i++) {
                svf.VrsOut("COMMUNICATION" + (i + 1), communication[i]);
            }
        }

        /**
         * ヘッダを印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alpWrap svf, final Student student) {
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
            svf.VrsOut("JOB_NAME1", _param._jobName);
            svf.VrsOut("JOB_NAME2", _param._hrJobName);

            if (null != _param._logoFile && _param._logoFile.exists()) {
                svf.VrsOut("SCHOOL_LOGO", _param._logoFile.toString());
            }
            svf.VrsOut("PRESIDENT", _param._principalName);
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                svf.VrsOut("TEACHER", (String) _param._staffNames.get(0));
            }

            if (_param._subStaffNames != null && _param._subStaffNames.size() > 0 && !"1".equals(_param._KNJD181_HideSubTr)) {
                svf.VrsOut("TEACHER2", (String) _param._subStaffNames.get(0));
            }

            svf.VrsOut("SUBJECT",  student._majorName);
            svf.VrsOut("COURSE",   student._courseCodeName);
            svf.VrsOut("HR_NAME",  student._hrName);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME",     student._name);
        }

        /**
         * 住所を印字
         * @param svf
         * @param student
         */
        private void printAddress(final Vrw32alpWrap svf, final Student student) {
            if (student._address != null) {
                boolean use2 = false;
                boolean use3 = false;
                boolean useNameBig = false;
                final String addr1 = student._address._address1;
                final String addr2 = student._address._address2;
                final String addressee = student._address._addressee == null ? "" : student._address._addressee + "  様";
                try {
                    int check1 = addr1 == null ? 0 : addr1.getBytes("MS932").length;
                    int check2 = addr2 == null ? 0 : addr2.getBytes("MS932").length;
                    use3 = "1".equals(_param._useAddrField2) && (check1 > 50 || check2 > 50);
                    use2 = check1 > 40 || check2 > 40;
                    int check3 = addressee == null ? 0 : addressee.getBytes("MS932").length;
                    useNameBig = check3 > 24;
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
                if (!_param._isNotPrintAddressZipcd) {
                    svf.VrsOut(use3 ? "ADDR1_3" : use2 ? "ADDR1_2" : "ADDR1", addr1);     //住所
                    svf.VrsOut(use3 ? "ADDR2_3" : use2 ? "ADDR2_2" : "ADDR2", addr2);     //住所
                    svf.VrsOut("ZIPCD", student._address._zipcd);
                }
                svf.VrsOut(useNameBig ? "ADDRESSEE2" : "ADDRESSEE", addressee);
            }
            svf.VrsOut("HR_ATTNO_NAME", student._hrNameAbbv + "　" + student._attendNo + (_param._isNotPrintStudentNameWhenAddresseeIs2 ? "" : "　" + student._name));
        }

        /**
         * 総合的な学習の時間・出欠席を印字
         * @param svf
         * @param student
         */
        private void printTotalStudy(final Vrw32alpWrap svf, final Student student) {
            final RecordTotalStudyTimeDat recordTotalStudy = (RecordTotalStudyTimeDat) student._recordTotalStudieTimeDat.get(Param.SEMEALL);
            if (null != recordTotalStudy) {
                String[] spContent = get_token(recordTotalStudy._totalStudyAct, 50, 2);
                for (int i = 0; i < spContent.length; i++) {
                    svf.VrsOut("SP_CONENT" + (i + 1), spContent[i]);
                }
                String[] spEva = get_token(recordTotalStudy._totalStudyTime, 50, 3);
                for (int i = 0; i < spEva.length; i++) {
                    svf.VrsOut("SP_EVA" + (i + 1), spEva[i]);
                }
            }
            final SubclassAttendance sa = getSubclassAttendance90(student, Param.SEMEALL);
            final String minSubclassCd = sa._subclassCd;
            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(minSubclassCd), sa.getSick() == null || "".equals(sa.getSick()) ? "0" : sa.getSick(), "SP_ABSENCE", "SP_EARLY");
            svf.VrsOut("SP_ABSENCE", sa.getRawSick() == null || "".equals(sa.getRawSick()) ? "0" : sa.getRawSick());
            svf.VrsOut("SP_EARLY", sa.getLateEarly() == null || "".equals(sa.getLateEarly()) ? "0" : sa.getLateEarly());
            svf.VrsOut("SP_PUB_ABSENCE", sa.getKoketsu() == null || "".equals(sa.getKoketsu()) ? "0" : sa.getKoketsu());
            svf.VrsOut("SP_MOURNING", sa.getMourning() == null || "".equals(sa.getMourning()) ? "0" : sa.getMourning());
            svf.VrsOut("SP_SUSPEND", sa.getSuspend() == null || "".equals(sa.getSuspend()) ? "0" : sa.getSuspend());
        }

        private int printRecDetail (
                final Vrw32alpWrap svf,
                final Student student
        ) {
            boolean hasData = false;
            int i = 0;

            boolean bsubclass90 = false;

            printAttendData(svf, student);
            printComment(svf);
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();

                printAttendData(svf, student);

                if (_param.isGakunenmatsu() || "2".equals(_param._semester)) {
                    _svf.VrsOut("GETCREDIT", String.valueOf(student._totalCredit));
                }

                if (info.isNotPrintSubclassList(_param)) { continue; }

                if (hasData) {
                    svf.VrEndRecord();
                    i++;
                }
                printComment(svf);

                if ((is90(info._subclassCd) || info._num90Other != null) && ! bsubclass90) {
                    if (!_param._isNoBlank90) {
                        i += blankRecord(svf, info._num90, i, MAX_RECORD, "CLASS");
                        printComment(svf);
                    }
                   bsubclass90 = true;
                }
                hasData= printSubclassInfo(svf, info, student, (i + 1));
            }
            return i;
        }

        private void printAttendData(final Vrw32alpWrap svf, final Student student) {
            final String nameField1 = "REC_LHR_ABSENCE_NAME";
            final String nameField2 = "REC_ASSEMBLY_ABSENCE_NAME";
            final String nameField3 = "ABSENCE_NAME";
            final String valField1 = "REC_LHR_ABSENCE";
            final String valField2 = "REC_ASSEMBLY_ABSENCE";
            final String valField3 = "PERIOD_SHR_ABSENCE";
            final Attendance sum = student.getAttendance(Param.SEMEALL);

            svf.VrsOut("REC_LESSON" , String.valueOf(sum._lesson));
            svf.VrsOut("REC_MOURNING" , String.valueOf(sum._mourning + sum._suspend + sum._koudome + sum._virus));
            svf.VrsOut("REC_PRESENT", String.valueOf(sum._mlesson));
            svf.VrsOut("REC_ABSENCE", String.valueOf(sum._absence));
            svf.VrsOut("REC_ATTEND" , String.valueOf(sum._attend));

            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                svf.VrsOut(nameField1, "LHRの欠席時数");
                svf.VrsOut(nameField2, "学校行事の欠席時数");
                if ("2".equals(_param._kobetuPrintShrOrLateearly)) {
                    svf.VrsOut(nameField3, "遅刻・早退数");
                } else if ("1".equals(_param._kobetuPrintShrOrLateearly)) {
                    svf.VrsOut(nameField3, "SHRの欠席時数");
                }
            } else {
                svf.VrsOut(nameField1, "特別活動欠課時数");
                if ("1".equals(_param._gassanPrintLateearly)) {
                    svf.VrsOut(nameField2, "遅刻・早退数");
                }
            }

            final String[] spGroupCd = new String[]{Attendance.GROUP_LHR, Attendance.GROUP_ASS};
            if (Param.SPECIAL_ACT_KOBETSU.equals(_param._printSpecialAct)) {
                final String[] field = new String[]{valField1, valField2};
                for (int i = 0; i < spGroupCd.length; i++) {

                    BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                    svf.VrsOut(field[i], String.valueOf(spGroupKomaKekka));

                    if (_param._isPrintAbsenceHighNote) {
                        final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd[i]);
                        setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekka.setScale(0, BigDecimal.ROUND_HALF_UP)), field[i]);
                    }
                }
                if ("1".equals(_param._kobetuPrintShrOrLateearly)) { // SHRを表示
                    final String spGroupCd3 = Attendance.GROUP_SHR;
                    final String field3 = valField3;
                    BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd3);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd3);
                    svf.VrsOut(field3, String.valueOf(spGroupKomaKekka));
                    if (_param._isPrintAbsenceHighNote) {
                        final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(spGroupCd3);
                        setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekka.setScale(0, BigDecimal.ROUND_HALF_UP)), field3);
                    }
                } else if ("2".equals(_param._kobetuPrintShrOrLateearly)) { // 遅刻・早退を表示
                    svf.VrsOut(valField3, String.valueOf(sum._late + sum._leave));
                }
            } else if (Param.SPECIAL_ACT_GASSAN.equals(_param._printSpecialAct)) {
                BigDecimal spGroupKekkaTotal = new BigDecimal(0);
                int spGroupKomaKekkaTotal = 0;
                for (int i = 0; i < spGroupCd.length; i++) {
                    BigDecimal spGroupKekka = sum.getSpGroupKekka(spGroupCd[i]);
                    spGroupKekkaTotal = spGroupKekkaTotal.add(spGroupKekka);
                    int spGroupKomaKekka = sum.getSpGroupKomaKekka(spGroupCd[i]);
                    spGroupKomaKekkaTotal += spGroupKomaKekka;
                }
                svf.VrsOut(valField1, String.valueOf(spGroupKomaKekkaTotal));
                if (_param._isPrintAbsenceHighNote) {
                    final AbsenceHigh ah = (AbsenceHigh) student._spSubclassAbsenceHigh.get(Attendance.GROUP_ALL);
                    setSpecialAbsenceFieldAttribute(svf, ah, String.valueOf(spGroupKekkaTotal.setScale(0, BigDecimal.ROUND_HALF_UP)), valField1);
                }
                if ("1".equals(_param._gassanPrintLateearly)) { // 遅刻・早退を表示
                    svf.VrsOut(valField2, String.valueOf(sum._late + sum._leave));
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
            svf.VrsOut("SUBCLASS" + (si._subclassname != null && si._subclassname.length() > 13 ? "2" : "1"),  si._subclassname);

            if (si.isPrintCreditMstCredit()) {
                svf.VrsOut("CREDIT", si._credits);
            }
            if (Param.SEMEALL.equals(_param._semester) || "2".equals(_param._semester)) {
                svf.VrsOut("GET_CREDIT", si._getCredit);
            }

            final Subclass subclass = student.getSubclass(si._subclassCd);
            if (subclass == null) {
                return true;
            }

            if (SubclassInfo.GAPPEI_NASI == si._replaceflg  || SubclassInfo.GAPPEI_MOTO == si._replaceflg  || SubclassInfo.GAPPEI_SAKI == si._replaceflg) {

                final String[] semTestKindCds = _param.getTargetTestKindCds();
                for (int i = 0; i < semTestKindCds.length; i++) {

                    final String semTestKindCd = semTestKindCds[i];
                    final String fieldGrading = getFieldColumnTestKind(semTestKindCd) == 1 ? "RATE1" : "RATE3";
                    final ScoreValue sv;
                    final boolean isSlump;
                    if (isGakunenHyouka(semTestKindCd)) {
                        sv = subclass.getScoreValue(Param._99901, Param.DIV99901);
                        isSlump = student.isSlump(subclass._subclassCd, Param._99901);
                    } else {
                        sv = subclass.getScoreValue(semTestKindCd);
                        isSlump = student.isSlump(subclass._subclassCd, semTestKindCd);
                    }
                    if (!isSubclassCdSogotekinaGakushunoJikan(subclass) && isSlump) {
                        svf.VrAttribute(fieldGrading,  "Paint=(1,50,1),Bold=1");
                    }
                    if (sv != null && sv._value != null) {
                        if (isCharacter(sv._value)) {
                            svf.VrAttribute(fieldGrading, "Hensyu=3");
                        }
                        if (!isSubclassCdSogotekinaGakushunoJikan(subclass)) {
                            svf.VrsOut(fieldGrading, sv._value);
                        }
                    }
                    if (!semTestKindCd.substring(0, 1).equals("1")) {
                        printSubclassAttendance(svf, si, student, si._replaceflg, subclass, "3", subclass.getAttendance(Param.SEMEALL));
                    }
                }
                printSubclassAttendance(svf, si, student, si._replaceflg, subclass, "1", subclass.getAttendance("1"));
            }
            return true;
        }

        /**
         * @param si
         * @param student
         * @param replaceflg
         * @param subclass
         * @param sa
         */
        private void printSubclassAttendance(final Vrw32alpWrap svf,
                final SubclassInfo si,
                final Student student,
                final int replaceflg,
                Subclass subclass,
                final String j,
                final SubclassAttendance sa) {

            if (sa == null) {
                return;
            }
            String sick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getReplacedSick() : sa.getSick();

//            if (SubclassInfo.GAPPEI_SAKI == replaceflg && SubclassInfo.GAPPEI_TANNI_KASAN.equals(si._calculateCreditFlg) && !_param._isNoPrintMoto) {
//                sick = "0";
//            }
            setAbsenceFieldAttribute(svf, student.getAbsenceHigh(subclass._subclassCd), sick, "ABSENCE" + j, "LATE_EARLY" + j);

            String rawSick = (SubclassInfo.GAPPEI_SAKI == replaceflg) ? sa.getRawReplacedSick() : sa.getRawSick();

//            if (SubclassInfo.GAPPEI_SAKI == replaceflg && SubclassInfo.GAPPEI_TANNI_KASAN.equals(si._calculateCreditFlg) && !_param._isNoPrintMoto) {
//                rawSick = "0";
//            }

            svf.VrsOut("ABSENCE" + j, rawSick == null ? "" : String.valueOf(rawSick)); // "欠課"欄に欠時数を表示（A・Bパターンは"欠時"、"欠課"各欄に欠時数、欠課数を表示）
            svf.VrsOut("LATE_EARLY" + j, sa.getLateEarly());
            svf.VrsOut("PUB_KEKKA" + j, sa.getKoketsu());
            svf.VrsOut("MOURNING_SUSPEND" + j, sa.getMourningSuspend());
        }
    }


    /**
     * パラメータクラス
     */
    private static class Param {

        static final String SEMEALL = "9";

        static final String REC = "REC  ";

        static final String REC_V = "REC_V";

        static final String _39900 = "39900";

        static final String _99900 = "99900";

        static final String _99901 = "99901";

        static final String DIV99901 = "DIV99901";

        // 科目コード:総合的な学習の時間
        static final String subclassCdSogotekinaGakushunoJikan = "900400";

        static final String PATTERN_A = "A";
        static final String PATTERN_B = "B";
        static final String PATTERN_C = "C";
        static final String PATTERN_D = "D";

        static final String SPECIAL_ACT_KOBETSU = "1";
        static final String SPECIAL_ACT_GASSAN = "2";

        final String _year;
        final String _semester;
        final String _semeFlg;
        final String _ctrlSemester;
        final String _grade_hr_class;
        final String _schoolKind;
        final String _pattern;

        private String _sdate;
        private final String _edate;
        private final String _ctrlDate;

        private final String[] _dispSemester;

        private final String[] _testKindCds;

        private final String[] _selectSchregno;

        private final boolean _useKetten;

        private final Integer _ketten;

        private final Integer _kettenHyotei;

        private boolean _isNoBlank90;

        String _schoolName;

        String _principalName;

        String _jobName;

        String _hrJobName;

        String _schoolAddress;

        String _schoolTelNo;

        List _staffNames;
        List _subStaffNames;

        KNJSchoolMst _knjSchoolMst;

        private KNJDefineSchool _definecode;

        private boolean _isSeireki;

        private TreeMap _semesterMap;

        final boolean _useAbsenceWarn;

        final boolean _isNotPrintNote;

        final String _kijunten;

        /** D016 */
        boolean _isNoPrintMoto;

        /** D026 */
        private final List _d026List = new ArrayList();

        /** D016 */
        boolean _isMirishuu;

        /** C005 */
        private final Map _subClassC005 = new HashMap();

        String _hitsuRishuNote;

        final String _formSelect;

        /** 住所・郵便番号を表示しない */
        final boolean _isNotPrintAddressZipcd;

        final String _addressSelect;

        final boolean _isNoSogo;

        final boolean _isPrintAbsenceHighNote;

        final boolean _isNotPrintStudentNameWhenAddresseeIs2;

        /** 個別：SHRの欠席時数 or 1日遅刻・早退*/
        final String _kobetuPrintShrOrLateearly;

        /** 合算：1日遅刻・早退*/
        final String _gassanPrintLateearly;

        /** Bパターンのとき校長印欄のあるフォームを選択するか */
        final boolean _isPrintPrincipalMark;

        final String _documentRoot;

        private String _imagePath;

        private String _extension;

        public File _logoFile;

        /** 副担任を表示しない **/
        final String _KNJD181_HideSubTr;
        
        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;

        private String _attendEndDateSemester;

        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;

        /** 学年末欄 1:学年評定 2:学年評価 3:３学期評価 */
        final String _gakunenmatuRan;

        /** 総合的な学習の時間の所見のフィールドが広いフォーム使用フラグ （Aパターンのみ）*/
        final String _tutisyoTotalstudyWideForm;

        /** 1:個別、2:合算 */
        final String _printSpecialAct;

        /** 教育課程コードを使用するか */
        final String _useClassDetailDat;
        final String _useAddrField2;

        /** 増加単位を反映する */
        final String _zouka;

        final Map _attendParamMap;

        final boolean _isNotPrintCommunication;

        final String _nendo;

        final String _d008Namecd1;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            String ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlDate = ctrlDate.substring(0,4) + "-" + ctrlDate.substring(5,7) + "-" + ctrlDate.substring(8);

            _edate = request.getParameter("DATE").replace('/', '-');

            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            String p = request.getParameter("TYOUHYOU_PATTERN");
            _pattern = "1".equals(p) ? PATTERN_A : "2".equals(p) ? PATTERN_B : "3".equals(p) ? PATTERN_C : "4".equals(p) ? PATTERN_D : null;

            _selectSchregno = request.getParameterValues("CATEGORY_SELECTED");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAddrField2 = request.getParameter("useAddrField2");
            _zouka = request.getParameter("ZOUKA");
            _isNotPrintCommunication = "1".equals(request.getParameter("NO_COMM"));
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";

            boolean haskettenParam = false;
            for (final Enumeration enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String paramname = (String) enums.nextElement();
                if ("KETTEN".equals(paramname)) {
                    haskettenParam = true;
                    break;
                }
            }
            _useKetten = haskettenParam;

            _ketten = !NumberUtils.isNumber(request.getParameter("KETTEN")) ? new Integer(-1) : Integer.valueOf(request.getParameter("KETTEN"));

            _kettenHyotei = request.getParameter("KETTEN_HYOTEI") == null ? new Integer(-1) : Integer.valueOf(request.getParameter("KETTEN_HYOTEI"));

            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));

            _isNotPrintNote = "1".equals(request.getParameter("HANREI_SYUTURYOKU_NASI"));

            _kijunten = request.getParameter("OUTPUT_KIJUN");

            _formSelect = request.getParameter("FORM_SELECT");

            _isNotPrintAddressZipcd = "1".equals(request.getParameter("JUSYO_PRINT"));

            _addressSelect = request.getParameter("OKURIJOU_JUSYO");

            _isPrintAbsenceHighNote = "1".equals(request.getParameter("TYUI_TYOUKA_CHECK"));

            _isNotPrintStudentNameWhenAddresseeIs2 = !"1".equals(_addressSelect) ? "1".equals(request.getParameter("NO_PRINT_STUDENT_NAME")) || "1".equals(request.getParameter("NO_PRINT_STUDENT_NAME2")) : true;

            _isPrintPrincipalMark = "1".equals(request.getParameter("KOUTYOU"));

            _printSpecialAct = request.getParameter("SPECIALACT");

            _kobetuPrintShrOrLateearly = SPECIAL_ACT_KOBETSU.equals(_printSpecialAct) && "1".equals(request.getParameter("SYUSEKI")) ? request.getParameter("SHR_LATE_EARLY") : null;

            _gassanPrintLateearly = SPECIAL_ACT_GASSAN.equals(_printSpecialAct) ? request.getParameter("LATE_EARLY") : null;

            _isNoSogo = "1".equals(request.getParameter("SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI"));

            _documentRoot = request.getParameter("DOCUMENTROOT");

            _dispSemester = PATTERN_D.equals(_pattern) ? new String[]{"1", SEMEALL} : new String[]{"1", "2", SEMEALL};
            if (Param.PATTERN_B.equals(_pattern)) {
                _semeFlg = "3";
            } else {
                _semeFlg = "2";
            }

            _gakunenmatuRan = request.getParameter("GAKUNENMATU_RAN");

            _isNoBlank90 = "1".equals(request.getParameter("NO_BLANK_90"));

            final String gakunenmatuCd = "3".equals(_gakunenmatuRan) ? _39900 : _99900;
            if (Param.PATTERN_C.equals(_pattern)) {
                _testKindCds = new String[]{"19900", "29900", gakunenmatuCd};
            } else if (Param.PATTERN_D.equals(_pattern)) {
                _testKindCds = new String[]{"19900", gakunenmatuCd};
            } else {
                _testKindCds = new String[]{"19900", "29900", gakunenmatuCd};
            }

            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");

            _KNJD181_HideSubTr = request.getParameter("KNJD181_HideSubTr");

            
            
            _tutisyoTotalstudyWideForm = request.getParameter("tutisyoTotalstudyWideForm");

            load(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("grade", _grade_hr_class.substring(0, 2));
            _attendParamMap.put("hrClass", _grade_hr_class.substring(2, 5));

            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
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
            try {
                return String.valueOf(_definecode.semesdiv);
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
                } else if (PATTERN_D.equals(_pattern) && "2".equals(_semester) && SEMEALL.equals(dseme)) {
                    list.add(dseme);
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
                } else if (PATTERN_D.equals(_pattern) && "2".equals(_semester) && _99900.equals(testkind)) {
                    list.add(testkind);
                }
            }

            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _hrJobName = rs.getString("REMARK2");
                    _schoolAddress = rs.getString("REMARK4");
                    _schoolTelNo = rs.getString("REMARK5");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public void load(final DB2UDB db2) {
            try {
                loadNameMstD016(db2);
                loadNameMstD026(db2);
                loadNameMstZ012(db2);
                loadNameMstC005(db2);
                loadNameMstC042(db2);
            } catch (SQLException e) {
                log.error("名称マスタ読み込みエラー", e);
            }

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }

            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);
            if (log.isDebugEnabled()) {
                log.debug("schoolmark=" + _definecode.schoolmark + " *** semesdiv=" + _definecode.semesdiv + " " +
                        "*** absent_cov=" + _definecode.absent_cov + " *** absent_cov_late=" + _definecode.absent_cov_late);
            }

            loadSemester(db2);
            loadControlMst(db2);
            loadAttendEdateSemester(db2);

            final File file = new File(_documentRoot + "/" + _imagePath + "/" + "SCHOOLLOGO." + _extension);
            _logoFile = file.exists() ? file : null;

            _staffNames = getStaffNames(db2, isGakunenmatsu() ? _ctrlSemester : _semester);
            _subStaffNames = getSubStaffNames(db2, isGakunenmatsu() ? _ctrlSemester : _semester);

            setCertifSchoolDat(db2);
            setHitsuRishuNote(db2);
        }

        private void loadSemester(final DB2UDB db2) {
            _semesterMap = new TreeMap();

            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    final String cd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    final Semester semester = new Semester(cd, name, sdate, edate);
                    _semesterMap.put(cd, semester);

                    if (first) {
                        _sdate = sdate;
                        first = false;
                    }
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
            _isMirishuu = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAMESPARE1");
                final String name2 = rs.getString("NAMESPARE2");
                if ("Y".equals(name)) _isNoPrintMoto = true;
                if ("Y".equals(name2)) _isMirishuu = true;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.debug("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
            log.debug("(名称マスタD016):未履修 = " + _isMirishuu);
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

        private void loadNameMstC005(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String is = rs.getString("NAMESPARE1");
                log.debug("(名称マスタC005):科目コード" + subclassCd);
                _subClassC005.put(subclassCd, is);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC042(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                _absenceWarnIsUnitCount = "1".equals(rs.getString("NAMESPARE1"));
                log.debug("(名称マスタ C042) =" + _absenceWarnIsUnitCount);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private String loadAttendEdateSemester(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND T1.GRADE = '" + _grade_hr_class.substring(0, 2) + "' ");
            stb.append("     AND (('" + _edate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR ('" + _edate + "' BETWEEN T1.EDATE AND VALUE(T2.SDATE, '9999-12-30'))) ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _attendEndDateSemester = rs.getString("SEMESTER");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return _attendEndDateSemester;
        }

        /**
         * @param db2
         * @param _param
         * @return
         */
        private void setHitsuRishuNote(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rsName = null;

            final StringBuffer note = new StringBuffer();
            try {
                final String sql = "SELECT VALUE(NAMESPARE1,'') || ':' || VALUE(NAME1,'') AS NOTE "
                    + "FROM V_NAME_MST "
                    + "WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z011' "
                    + "ORDER BY NAMECD1";

                ps = db2.prepareStatement(sql);
                rsName = ps.executeQuery();

                String comma = "";
                while (rsName.next()) {
                    note.append(comma).append(rsName.getString("NOTE"));
                    comma = "、";
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rsName);
                db2.commit();
            }
            if (note.length() != 0) {
                note.insert(0, "※必履修区分…");
            }
            _hitsuRishuNote = note.toString();
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

        public List getStaffNames(final DB2UDB db2, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List list = new LinkedList();
            try{
                StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        CASE WHEN L11.STAFFCD IS NOT NULL THEN VALUE(L1.STAFFNAME_REAL, L1.STAFFNAME) ELSE L1.STAFFNAME END AS TR_NAME1 ");
                stb.append("       ,CASE WHEN L21.STAFFCD IS NOT NULL THEN VALUE(L2.STAFFNAME_REAL, L2.STAFFNAME) ELSE L2.STAFFNAME END AS TR_NAME2 ");
                stb.append("       ,CASE WHEN L31.STAFFCD IS NOT NULL THEN VALUE(L3.STAFFNAME_REAL, L3.STAFFNAME) ELSE L3.STAFFNAME END AS TR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L11 ON L11.YEAR = T1.YEAR AND L11.STAFFCD = L1.STAFFCD AND L11.DIV = '03' ");
                stb.append("LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.TR_CD2 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L21 ON L21.YEAR = T1.YEAR AND L21.STAFFCD = L2.STAFFCD AND L21.DIV = '03' ");
                stb.append("LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T1.TR_CD3 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L31 ON L31.YEAR = T1.YEAR AND L31.STAFFCD = L3.STAFFCD AND L31.DIV = '03' ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
                stb.append("    AND T1.SEMESTER = '" + semester + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if( rs.getString("TR_NAME1") != null )list.add( rs.getString("TR_NAME1") );
                    if( rs.getString("TR_NAME2") != null )list.add( rs.getString("TR_NAME2") );
                    if( rs.getString("TR_NAME3") != null )list.add( rs.getString("TR_NAME3") );
                }
            } catch (Exception ex) {
                log.error("List Staff_name() Staff_name error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        public List getSubStaffNames(final DB2UDB db2, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List list = new LinkedList();
            try{
                StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        CASE WHEN L11.STAFFCD IS NOT NULL THEN VALUE(L1.STAFFNAME_REAL, L1.STAFFNAME) ELSE L1.STAFFNAME END AS SUBTR_NAME1 ");
                stb.append("       ,CASE WHEN L21.STAFFCD IS NOT NULL THEN VALUE(L2.STAFFNAME_REAL, L2.STAFFNAME) ELSE L2.STAFFNAME END AS SUBTR_NAME2 ");
                stb.append("       ,CASE WHEN L31.STAFFCD IS NOT NULL THEN VALUE(L3.STAFFNAME_REAL, L3.STAFFNAME) ELSE L3.STAFFNAME END AS SUBTR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.SUBTR_CD1 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L11 ON L11.YEAR = T1.YEAR AND L11.STAFFCD = L1.STAFFCD AND L11.DIV = '03' ");
                stb.append("LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.SUBTR_CD2 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L21 ON L21.YEAR = T1.YEAR AND L21.STAFFCD = L2.STAFFCD AND L21.DIV = '03' ");
                stb.append("LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T1.SUBTR_CD3 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L31 ON L31.YEAR = T1.YEAR AND L31.STAFFCD = L3.STAFFCD AND L31.DIV = '03' ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
                stb.append("    AND T1.SEMESTER = '" + semester + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if( rs.getString("SUBTR_NAME1") != null )list.add( rs.getString("SUBTR_NAME1") );
                    if( rs.getString("SUBTR_NAME2") != null )list.add( rs.getString("SUBTR_NAME2") );
                    if( rs.getString("SUBTR_NAME3") != null )list.add( rs.getString("SUBTR_NAME3") );
                }
            } catch (Exception ex) {
                log.error("List Staff_name() Staff_name error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        /**
         * 成績データをRECORD_RANK_VDATから読み込むか
         * @param semTestKindCd
         * @return
         */
        public boolean isValueFromRecordRankVDat(final String semTestKindCd) {
            return !"99".equals(semTestKindCd.substring(1,3));
        }
    }
}
