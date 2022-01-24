// kanji=漢字
/*
 * $Id: 05252cc0c4fc38abcd520f4ebf3a9f5dc57432c2 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: 05252cc0c4fc38abcd520f4ebf3a9f5dc57432c2 $
 */
public class KNJD615M {
    private static final Log log = LogFactory.getLog(KNJD615M.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    private static final String OUTPUT_KJUN2 = "2";
    private static final String OUTPUT_KJUN3 = "3";
    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";
    private static final String OUTPUT_RANK4 = "4";

//    private static final String SIDOU_INPUT_INF_MARK = "1";
//    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_02 = "02";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";

//    private static final String ATTRIBUTE_KETTEN = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_KETTEN = "Palette=9"; // 赤文字
    private static final String ATTRIBUTE_KETTEN_NORMAL = "Pallete=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,70,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER_NORMAL = "Paint=(0,0,0),Bold=0";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";

    private static String attributeBackground(final int line) {
        if (line % 2 == 0) {
            return "Paint=(0,100,2)";
        }
        return "Paint=(8,70,2)";
    }

    private static final String csv = "csv";

    protected boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        Param param = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            param = createParam(request, db2);

            if (csv.equals(param._cmd)) {
//                final List outputLines = new ArrayList();
//                setOutputCsvLines(db2, param, outputLines);
//                CsvUtils.outputLines(log, response, param._title + ".csv", outputLines);
            } else {
                svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                response.setContentType("application/pdf");

                printMain(db2, param, svf);
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != param) {
                for (final Iterator it = param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }

                if (csv.equals(param._cmd)) {
                } else {
                    if (!_hasData) {
                        svf.VrSetForm("MES001.frm", 0);
                        svf.VrsOut("note" , "note");
                        svf.VrEndPage();
                    }
                    int ret = svf.VrQuit();
                    if (ret == 0) {
                        log.info("===> VrQuit():" + ret);
                    }
                }
            }
        }
    }

//    public void setOutputCsvLines(
//            final DB2UDB db2,
//            final Param param,
//            final List outputList
//    ) throws Exception {
//
//        for (int h = 0; h < param._classSelected.length; h++) { //印刷対象HR組
//            final List courses = Course.createCourses(db2, param, param._classSelected[h]);
//            log.debug("コース数=" + courses.size());
//
//            final HRInfo hrInfo = new HRInfo(param._classSelected[h]);  //HR組
//            log.info(" print hr " + hrInfo._hrclassCd);
//            hrInfo._courses = courses;
//
//            hrInfo.load(db2, param, null);
//
//            // 印刷処理
//            final Form form = new Form();
//            form.outputCsv(outputList, param, hrInfo);
//        }
//    }

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void printMain(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf
    ) throws Exception {

        _hasData = false;
        for (int h = 0; h < param._classSelected.length; h++) { //印刷対象HR組

            final List courses = Course.createCourses(db2, param, param._classSelected[h]);
            log.debug("コース数=" + courses.size());

            final HRInfo hrInfo = new HRInfo(param._classSelected[h]);  //HR組
            hrInfo._courses = courses;

//            if (_param._outputCourse) {
//                for (final Iterator it = courses.iterator(); it.hasNext();) {
//                    final Course course = (Course) it.next();
//                    hrInfo.load(db2, _param, course._coursecd);
//
//                    // 印刷処理
//                    if (printMain(svf, hrInfo)) {
//                        hasData = true;
//                    }
//                }
//            } else {
                hrInfo.load(db2, param, null);
                // 印刷処理
                final Form form = new Form();
                if (form.print(svf, param, hrInfo)) {
                    _hasData = true;
                }
//            }
        }
    }

    private static class Course {
        final String _grade;
        final String _hrclass;
        final String _coursecd;
        final String _name;

        public Course(
                final String grade,
                final String hrclass,
                final String coursecd,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _coursecd = coursecd;
            _name = name;
        }

        private static List createCourses(final DB2UDB db2, final Param param, final String gradeHrclass) {
            final List rtn = new ArrayList();
            final String sql = sqlCourses(param, gradeHrclass);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("COURSECD");
                    final String name = rs.getString("COURSECODENAME");

                    final Course course = new Course(
                            grade,
                            hrclass,
                            coursecd,
                            name
                    );

                    rtn.add(course);
                }
            } catch (final Exception ex) {
                log.error("コースのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlCourses(final Param param, final String hrclass) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD || W1.MAJORCD || W1.COURSECODE as COURSECD, ");
            stb.append("     L1.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT W1 ");
            stb.append("     LEFT JOIN COURSECODE_MST L1 ON L1.COURSECODE=W1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + param._year + "' AND ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" W1.SEMESTER = '" + param._semester + "' AND ");
            } else {
                stb.append(" W1.SEMESTER = '" + param._semeFlg + "' AND ");
            }
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE, ");
            stb.append("     L1.COURSECODENAME ");

            return stb.toString();
        }

        public String toString() {
            return _coursecd + ":" + _name;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private static class HRInfo implements Comparable {
        final String _hrclassCd;
        private List _courses;
        private String _staffName;
        private String _hrName;
        private List _students = Collections.EMPTY_LIST;
        private Set _courseSet = new HashSet();
        private Set _majorSet = new HashSet();
        private final Map _subclasses = new TreeMap();
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrMLesson;  // 学級の授業日数
        private String _avgHrTotal;   // 総合点の学級平均
        private String _avgHrAverage; // 平均点の学級平均
        private int _avgHrCount; // 総合点の学級の母集団の数
        private String _avgGradeAverage; // 平均点の学級平均
        private String _avgGradeTotal; // 総合点の学年平均
        private String _avgCourseAverage;
        private String _avgCourseTotal; // 総合点のコース平均
        private String _avgMajorAverage;
        private String _avgMajorTotal; // 総合点の学科平均
        private String _maxHrTotal;   // 総合点の最高点
        private String _minHrTotal;   // 総合点の最低点
        private String _failHrTotal;  // 欠点の数

        public HRInfo(final String hrclassCd) {
            _hrclassCd = hrclassCd;
        }

        public void load(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            loadHRClassStaff(db2, param);
            loadStudents(db2, param, courseCd);
            loadStudentsInfo(db2, param);
            loadHrclassAverage(db2, param, courseCd);
            loadRank(db2, param);
            loadScoreDetail(db2, param);
            createRanking(param);
            setSubclassAverage(param);
            setHrTotal();  // 学級平均等の算出
            setHrTotalMaxMin();
            setHrTotalFail();
            setSubclassGradeAverage(db2, param);
//            if (param._hasJudgementItem) {
//                loadPreviousCredits(db2, param);  // 前年度までの修得単位数取得
//                loadPreviousMirisyu(db2, param);  // 前年度までの未履修（必須科目）数
//                loadQualifiedCredits(db2, param);  // 今年度の資格認定単位数
//            }
        }

        private void loadHRClassStaff(
                final DB2UDB db2,
                final Param param
        ) {
            final KNJ_Get_Info.ReturnVal returnval = param._getinfo.Hrclass_Staff(
                    db2,
                    param._year,
                    param._semester,
                    _hrclassCd,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private void loadStudents(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _students = new LinkedList();
            try {
                final String sql = sqlHrclassStdList(param, _hrclassCd, courseCd);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
                    gnum = rs.getInt("ATTENDNO");
                    final Student student = new Student(rs.getString("SCHREGNO"), this, gnum);
                    _students.add(student);
                    _courseSet.add(rs.getString("COURSE"));
                    _majorSet.add(rs.getString("MAJOR"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * SQL HR組の学籍番号を取得するSQL
         */
        private String sqlHrclassStdList(final Param param, final String hrClass, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO, COURSECD || MAJORCD AS MAJOR, COURSECD || MAJORCD || COURSECODE AS COURSE ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("    AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append("    AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
//            if (param._outputCourse) {
//                stb.append("    AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
//            }
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdNameInfo(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    try {
                        final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] { student._schregno }));

                        TransInfo transInfo = null;
                        final String d1 = KnjDbUtils.getString(row, "KBN_DATE1");
                        final String d2 = KnjDbUtils.getString(row, "KBN_DATE2");
                        if (null != d1) {
                            final String n1 = KnjDbUtils.getString(row, "KBN_NAME1");
                            transInfo = new TransInfo(d1, KNJ_EditDate.h_format_JP(db2, d1), n1);
                        } else if (null != d2) {
                            final String n2 = KnjDbUtils.getString(row, "KBN_NAME2");
                            transInfo = new TransInfo(d2, KNJ_EditDate.h_format_JP(db2, d2), n2);
                        }
                        if (null == transInfo) {
                            transInfo = new TransInfo(null, null, null);
                        }
                        student._attendNo = KnjDbUtils.getString(row, "ATTENDNO");
                        student._name = KnjDbUtils.getString(row, "NAME");
                        student._transInfo = transInfo;

                    } catch (Exception e) {
                        log.error("Exception", e);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * SQL 任意の生徒の学籍情報を取得するSQL
         */
        private String sqlStdNameInfo(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
            stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
            stb.append("             ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
            stb.append("        W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append("        (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = W1.YEAR AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN V_SEMESTER_GRADE_MST    W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = '" + param._semester + "' AND W2.GRADE = W1.GRADE ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                              AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
            stb.append("                                OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                                  AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append("    AND W1.SCHREGNO = ? ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }

            return stb.toString();
        }

        private Student getStudent(String code) {
            if (code == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (code.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }

        private void loadHrclassAverage(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String course = SQLUtils.whereIn(true, toArray(_courseSet));
                final String major = SQLUtils.whereIn(true, toArray(_majorSet));
                final String sql = sqlHrclassAverage(param, _hrclassCd, course, major, courseCd);
                log.debug(" avg sql = " + sql);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String avgTotal = rs.getString("AVG_HR_TOTAL");
                    final String avgAveage = rs.getString("AVG_HR_AVERAGE");
                    if ("HR".equals(rs.getString("FLG"))) {
                        _avgHrTotal = avgTotal;
                        _avgHrAverage = avgAveage;
                        _avgHrCount = rs.getInt("COUNT");
                    } else if ("GRADE".equals(rs.getString("FLG"))) {
                        _avgGradeTotal = avgTotal;
                        _avgGradeAverage = avgAveage;
                    } else if ("COURSE".equals(rs.getString("FLG"))) {
                        _avgCourseTotal = avgTotal;
                        _avgCourseAverage = avgAveage;
                    } else if ("MAJOR".equals(rs.getString("FLG"))) {
                        _avgMajorTotal = avgTotal;
                        _avgMajorAverage = avgAveage;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String[] toArray(final Set set) {
            final List list = new ArrayList(set);
            final String[] arr = new String[set.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String) list.get(i);
            }
            return arr;
        }

        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private String sqlHrclassAverage(final Param param, final String hrClass, final String course, final String major, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("            ,CASE WHEN W1.GRADE||W1.HR_CLASS = '" + hrClass + "' THEN '1' ELSE '0' END AS IS_HR ");
            stb.append("            ,CASE WHEN W1.COURSECD||W1.MAJORCD||W1.COURSECODE IN " + course + " THEN '1' ELSE '0' END AS IS_COURSE ");
            stb.append("            ,CASE WHEN W1.COURSECD||W1.MAJORCD IN " + major + " THEN '1' ELSE '0' END AS IS_MAJOR ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
//            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
//            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
//            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
//            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
//            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = '" + hrClass.substring(0, 2) + "' ");
//            stb.append("         AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
//            if (param._outputCourse) {
//                stb.append("         AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
//            }
            stb.append(") ");

            stb.append("SELECT 'HR' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_HR = '1') ");
            stb.append("UNION ALL ");
            stb.append("SELECT 'GRADE' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("UNION ALL ");
            stb.append("SELECT 'COURSE' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_COURSE = '1') ");
            stb.append("UNION ALL ");
            stb.append("SELECT 'MAJOR' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_MAJOR = '1') ");

            return stb.toString();
        }

        private void loadRank(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdTotalRank(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._classRank = rs.getInt("CLASS_RANK");
                            student._rank = rs.getInt("TOTAL_RANK");
                            student._scoreSum = rs.getString("TOTAL_SCORE");
                            student._scoreAvg = rs.getString("TOTAL_AVG");
                        }
                    } catch (Exception e) {
                        log.error("Exception", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * SQL 任意の生徒の順位を取得するSQL
         */
        private String sqlStdTotalRank(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("             , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
//            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
//            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
//            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
//            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
//            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  W3.SCHREGNO ");
            if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                stb.append("   ,CLASS_AVG_RANK AS CLASS_RANK");
            } else if (OUTPUT_KJUN3.equals(param._outputKijun)) {
                stb.append("   ,CLASS_DEVIATION_RANK AS CLASS_RANK");
            } else {
                stb.append("   ,CLASS_RANK ");
            }
            stb.append("       ," + param._rankFieldName + "  AS TOTAL_RANK ");
            stb.append("       ,W3.SCORE AS TOTAL_SCORE ");
            stb.append("       ,DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            return stb.toString();
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String psKey = "SCORE_DETAIL";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlStdSubclassDetail(param);
                    log.debug(" subclass detail sql = " + sql);

                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, _hrclassCd.substring(0, 2));
                ps.setString(2, _hrclassCd.substring(2));

                rs = ps.executeQuery();

                while (rs.next()) {
                    if (param._enablePringFlg && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String classCd = subclassCd == null ? "" : subclassCd.substring(1, 3);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        final ScoreDetail scoreDetail = new ScoreDetail(
                                getSubClass(rs, _subclasses),
                                rs.getString("SCORE"),
                                rs.getString("SCORE_DI"),
                                rs.getString("ASSESS_LEVEL"),
                                rs.getString("KARI_HYOUTEI"),
                                (Integer) rs.getObject("REPLACEMOTO"),
                                (String) rs.getObject("PRINT_FLG"),
//                                rs.getString("SLUMP"),
//                                rs.getString("SLUMP_MARK"),
//                                rs.getString("SLUMP_SCORE"),
                                (Integer) rs.getObject("COMP_CREDIT"),
                                (Integer) rs.getObject("GET_CREDIT"),
                                (Integer) rs.getObject("CREDITS")
                        );
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }

            try {
                final String psKey = "ATTENDSUBCLASS";
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            param._sDate,
                            param._date,
                            param._attendParamMap
                            );
                    //log.debug(" attend subclass sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);

                log.info("load attendSubclass");
                long attendSubclassStart = System.currentTimeMillis();
                long attendSubclassAcc = 0;
                for (final Iterator sit = _students.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();

                    ps.setString(1, student._schregno);
                    long attendSubclassAccStart = System.currentTimeMillis();
                    rs = ps.executeQuery();
                    attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }

                        ScoreDetail scoreDetail = null;
                        for (final Iterator it = student._scoreDetails.keySet().iterator(); it.hasNext();) {
                            final String subclasscd = (String) it.next();
                            if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                                scoreDetail = (ScoreDetail) student._scoreDetails.get(subclasscd);
                                break;
                            }
                        }
                        if (null == scoreDetail) {
                            SubClass subClass = null;
                            for (final Iterator it = _subclasses.keySet().iterator(); it.hasNext();) {
                                final String subclasscd = (String) it.next();
                                if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                                    subClass = (SubClass) _subclasses.get(subclasscd);
                                    scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null);
                                    student._scoreDetails.put(subclasscd, scoreDetail);
                                    break;
                                }
                            }
                            if (null == scoreDetail) {
                                // log.fatal(" no detail " + student._schregno + ", " + rs.getString("SUBCLASSCD"));
                                continue;
                            }
                        }

                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                        final Number specialAbsentMinutes = (Number) rs.getObject("SPECIAL_SICK_MINUTES3");
                        if (specialGroupCd != null && specialAbsentMinutes != null) {
                            if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
                                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
                            }
                            int minute = ((Integer) student._spGroupAbsentMinutes.get(specialGroupCd)).intValue();
                            student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
                        }


                        if (0 != rs.getInt("MLESSON") && scoreDetail._subClass._jisu < rs.getInt("MLESSON")) {
                            scoreDetail._subClass._jisu = rs.getInt("MLESSON");
                        }
                        if (NumberUtils.isNumber(rs.getString("MLESSON"))) {
                            scoreDetail._jisu = rs.getBigDecimal("MLESSON").intValue();
                        }
                        scoreDetail._absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                        if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                            scoreDetail._absent = Double.valueOf(rs.getString("REPLACED_SICK"));
                        } else {
                            scoreDetail._absent = Double.valueOf(rs.getString("SICK2"));
                        }
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);
                        if (scoreDetail._isOver) {
                            log.info(" kekka over:" + scoreDetail._absent + " > " + scoreDetail._absenceHigh + " / " + student._schregno + ":" + student._name);
                        }
                    }

                    DbUtils.closeQuietly(rs);
                }
                long attendSubclassEnd = System.currentTimeMillis();
                log.info(" load attendSubclass elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("             , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
//            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
//            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
//            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
//            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
//            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
//            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = ? AND W1.HR_CLASS = ? ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
                stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

            stb.append(",CHAIR_STF AS(");
            stb.append("     SELECT W1.CHAIRCD, W1.SEMESTER, MIN(STAFFCD) AS STAFFCD ");
            stb.append("     FROM   CHAIR_A W1 ");
            stb.append("     LEFT JOIN CHAIR_STF_DAT W3 ON W3.YEAR = '" + param._year + "' ");
            stb.append("         AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W3.CHAIRCD = W1.CHAIRCD ");
            stb.append("         AND W3.CHARGEDIV = 1 ");
            stb.append("     GROUP BY W1.CHAIRCD, W1.SEMESTER ");
            stb.append("     )");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append("    SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("            SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append("    FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.COURSECD = T2.COURSECD ");
            stb.append("        AND T1.MAJORCD = T2.MAJORCD ");
            stb.append("        AND T1.COURSECODE = T2.COURSECODE ");
            stb.append("        AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                    T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.CREDITS");
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + param._year + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                          AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                 COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("             COMBINED_SUBCLASSCD");
            stb.append(") ");

            stb.append("   , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
            }
            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE ");
            stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END AS ASSESS_LEVEL ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("           W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("           ,W3.COMP_CREDIT ");
            stb.append("           ,W3.GET_CREDIT ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");

            //仮評定の表
            stb.append(",RECORD_KARI_HYOUTEI AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE AS KARI_HYOUTEI ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd.substring(0, 4) + SCORE_DIV_09 + "' ");
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END ");
            stb.append("        END AS SLUMP_SCORE ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  W2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("             W2.SUBCLASSCD, ");
            stb.append("             MIN(W22.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W2");
            stb.append("     LEFT JOIN CHAIR_STF W22 ON W22.SEMESTER = W2.SEMESTER ");
            stb.append("         AND W22.CHAIRCD = W2.CHAIRCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("              W2.SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  value(T7.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T3.SCORE AS SCORE ");
            stb.append("        ,W4.VALUE_DI AS SCORE_DI ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T35.KARI_HYOUTEI ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSABBV ");
            stb.append("        ,T7.SUBCLASSNAME ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_SCORE ");

            stb.append("    , W23.STAFFNAME ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI T35 ON T35.SUBCLASSCD = T1.SUBCLASSCD AND T35.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T8.CLASSCD || '-' || T8.SCHOOL_KIND = ");
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(" T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
            }
            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append("     LEFT JOIN STAFF_MST W23 ON W23.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            SDET.CLASSCD || '-' || SDET.SCHOOL_KIND || '-' || SDET.CURRICULUM_CD || '-' || ");
            }
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append("    LEFT JOIN RECORD_SCORE_DAT W4 ON W4.YEAR = '" + param._year + "' AND W4.SEMESTER = '" + param._semester + "' AND W4.TESTKINDCD || W4.TESTITEMCD || W4.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("                                 AND W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || W4.SUBCLASSCD = T1.SUBCLASSCD AND W4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubClass(
                final ResultSet rs,
                final Map subclasses
        ) {
            String subclasscode = null;
            int credit = 0;
            try {
                subclasscode = rs.getString("SUBCLASSCD");
                if (rs.getString("CREDITS") != null) { credit = rs.getInt("CREDITS"); }
            } catch (Exception e) {
                 log.error("Exception", e);
            }
            //科目クラスのインスタンスを更新して返す
            SubClass subclass;
            if (!subclasses.containsKey(subclasscode)) {
                //科目クラスのインスタンスを作成して返す
                String classabbv = null;
                String subclassabbv = null;
                String subclassname = null;
                String staffname = null;
                boolean electdiv = false;
                try {
                    classabbv = rs.getString("CLASSNAME");
                    subclassabbv = rs.getString("SUBCLASSABBV");
                    subclassname = rs.getString("SUBCLASSNAME");
                    staffname = rs.getString("STAFFNAME");
                    if ("1".equals(rs.getString("ELECTDIV"))) {
                        electdiv = true;
                    }
                } catch (Exception e) {
                     log.error("Exception", e);
                }
                subclass = new SubClass(subclasscode, classabbv, subclassabbv, subclassname, electdiv, credit, staffname);
                subclasses.put(subclasscode, subclass);
            } else {
                subclass = (SubClass) subclasses.get(subclasscode);
                int[] maxMin = setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = maxMin[0];
                subclass._mincredit = maxMin[1];
//                if (0 != credit) {
//                    if (subclass._maxcredit < credit) subclass._maxcredit = credit;
//                    if (credit < subclass._mincredit || 0 == subclass._mincredit) subclass._mincredit = credit;
//                }
            }
            return subclass;
        }

//        // 前年度までの修得単位数計
//        private void loadPreviousCredits(
//                final DB2UDB db2,
//                final Param param
//        ) {
//            PreparedStatement ps = null;
//
//            try {
//                final StringBuffer stb = new StringBuffer();
//
//                stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
//                stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
//                stb.append(" WHERE  T1.SCHREGNO = ?");
//                stb.append("    AND T1.YEAR < '" + param._year + "'");
//                stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
//                stb.append("      OR T1.SCHOOLCD != '0')");
//
//                final String sql = stb.toString();
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    int i = 1;
//                    ps.setString(i++, student._schregno);
//
//                    ResultSet rs = null;
//                    try {
//                        rs = ps.executeQuery();
//                        if (rs.next()) {
//                            student._previousCredits = rs.getInt("CREDIT");
//                        }
//                    } catch (Exception e) {
//                        log.error("Exception", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }

//        // 前年度までの未履修（必須科目）数
//        private void loadPreviousMirisyu(
//                final DB2UDB db2,
//                final Param param
//        ) {
//            PreparedStatement ps = null;
//
//            try {
//                final StringBuffer stb = new StringBuffer();
//
//                stb.append(" SELECT COUNT(*) AS COUNT");
//                stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
//                stb.append(" INNER JOIN SUBCLASS_MST T2 ON ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
//                }
//                stb.append("            T1.SUBCLASSCD = ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
//                }
//                stb.append("            T2.SUBCLASSCD");
//                stb.append(" WHERE  T1.SCHREGNO = ?");
//                stb.append("    AND T1.YEAR < '" + param._year + "'");
//                stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
//                stb.append("      OR T1.SCHOOLCD != '0')");
//                stb.append("    AND VALUE(T2.ELECTDIV,'0') <> '1'");
//                stb.append("    AND VALUE(T1.COMP_CREDIT,0) = 0");
//
//                final String sql = stb.toString();
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    int i = 1;
//                    ps.setString(i++, student._schregno);
//
//                    ResultSet rs = null;
//                    try {
//                        rs = ps.executeQuery();
//                        if (rs.next()) {
//                            student._previousMirisyu = rs.getInt("COUNT");
//                        }
//                    } catch (Exception e) {
//                        log.error("Exception", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }

//        // 今年度の資格認定単位数
//        private void loadQualifiedCredits(
//                final DB2UDB db2,
//                final Param param
//        ) {
//            PreparedStatement ps = null;
//
//            try {
//                final StringBuffer stb = new StringBuffer();
//
//                stb.append(" SELECT SUM(T1.CREDITS) AS CREDITS");
//                stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
//                stb.append(" WHERE  T1.SCHREGNO = ?");
//                stb.append("    AND T1.YEAR < '" + param._year + "'");
//
//                final String sql = stb.toString();
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    int i = 1;
//                    ps.setString(i++, student._schregno);
//
//                    ResultSet rs = null;
//                    try {
//                        rs = ps.executeQuery();
//                        if (rs.next()) {
//                            student._qualifiedCredits = rs.getInt("CREDITS");
//                        }
//                    } catch (Exception e) {
//                        log.error("Exception", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }

        /**
         * 欠点の算出
         */
        private void setHrTotalFail() {
            int countFail = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null != student._total) {
                    if (0 < student._total._countFail) {
                        countFail += student._total._countFail;
                    }
                }
            }
            if (0 < countFail) {
                _failHrTotal = String.valueOf(countFail);
            }
        }

        /**
         * 最高点・最低点の算出
         */
        private void setHrTotalMaxMin() {
            int totalMax = 0;
            int totalMin = Integer.MAX_VALUE;
            int countT = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null == student._scoreSum) {
                    continue;
                }
                countT++;
                final int totalInt = Integer.parseInt(student._scoreSum);
                //最高点
                totalMax = Math.max(totalMax, totalInt);
                //最低点
                totalMin = Math.min(totalMin, totalInt);
//              log.debug("total="+total+", totalMax="+totalMax+", totalMin="+totalMin);
            }
            if (0 < countT) {
                _maxHrTotal = String.valueOf(totalMax);
                _minHrTotal = String.valueOf(totalMin);
            }
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         */
        private void setSubclassAverage(final Param param) {
            final Map map = new HashMap();

            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                for (final Iterator itD = student._scoreDetails.values().iterator(); itD.hasNext();) {
                    final ScoreDetail detail = (ScoreDetail) itD.next();
//                    final ScoreValue val = detail.getPatternAsses();
                    final String scorevalue = detail._score;
                    if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                        continue;
                    }
                    if (null == map.get(detail._subClass)) {
                        map.put(detail._subClass, new int[5]);
                    }
                    final int[] arr = (int[]) map.get(detail._subClass);
                    if (null != scorevalue && StringUtils.isNumeric(scorevalue)) {
                        final int v = Integer.parseInt(scorevalue);
                        arr[0] += v;
                        arr[1]++;
                        //最高点
                        if (arr[2] < v) {
                            arr[2] = v;
                        }
                        //最低点
                        if (arr[3] > v || arr[1] == 1) {
                            arr[3] = v;
                        }
                    }
                    //欠点（赤点）
                    if (ScoreDetail.isFailCount(param, detail)) {
                        arr[4]++;
                    }
                }
            }

            for (final Iterator it = _subclasses.values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                if (map.containsKey(subclass)) {
                    final int[] val = (int[]) map.get(subclass);
                    if (0 != val[1]) {
                        final double d = Math.round(val[0] * 10.0 / val[1]) / 10.0;
                        subclass._scoreaverage = DEC_FMT1.format(d);
                        subclass._scoretotal = String.valueOf(val[0]);
                        subclass._scoreCount = String.valueOf(val[1]);
                        subclass._scoreMax = String.valueOf(val[2]);
                        subclass._scoreMin = String.valueOf(val[3]);
                        if (0 != val[4]) {
                            subclass._scoreFailCnt = String.valueOf(val[4]);
                        }
                    }
                }
            }
        }

        /**
         * 学級平均の算出
         */
        private void setHrTotal() {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
//            int mlesson = 0;
//            int present = 0;
//            int absent = 0;
//            int[] arrc = {0,0};  // 履修単位
//            int[] arrj = {0,0};  // 授業日数
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final Total totalObj = student._total;
                if (null != totalObj) {
                    if (0 < totalObj._count) {
                        totalT += totalObj._total;
                        countT++;
                    }
//                    if (null != totalObj._avgBigDecimal) {
//                        totalA += totalObj._avgBigDecimal.doubleValue();
//                        countA++;
//                    }
//                    if (0< totalObj._avgcount) {
//                        totalA += totalObj._avgtotal;
//                        countA += totalObj._avgcount;
                    if (0< totalObj._count) {
                        totalA += totalObj._total;
                        countA += totalObj._count;
                    }
                }
//                final AttendInfo attend = student._attendInfo;
//                if (null != attend) {
//                    mlesson += attend._mLesson;
//                    present += attend._present;
//                    absent += attend._absent;
//                    arrj = setMaxMin(arrj[0], arrj[1], attend._mLesson);
//                }
//                arrc = setMaxMin(arrc[0], arrc[1], student._compCredit);
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
//            if (0 < arrc[0]) {
//                _HrCompCredits = arrc[0] + "単位";
//            }
//            if (0 < arrj[0]) {
//                _HrMLesson = arrj[0] + "日";
//            }
        }

        /**
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    VALUE(T2.ELECTDIV, '0') AS ELECTDIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T1.SUBCLASSCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testKindCd + "'");
            stb.append("    AND T1.AVG_DIV = '" + param._avgDiv + "' ");
            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '" + param.SUBCLASSCD999999 + "' ");
            if ("2".equals(param._avgDiv)) {
                stb.append("    AND T1.HR_CLASS = '" + _hrclassCd.substring(2) + "' ");
                stb.append("    ORDER BY HR_CLASS ");

            } else if ("3".equals(param._avgDiv)) {
                final String[] coursecds = new String[_courses.size()];
                for (int i = 0; i < _courses.size(); i++) {
                    final Course course = (Course) _courses.get(i);
                    coursecds[i] = course._coursecd;
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN " + SQLUtils.whereIn(true, coursecds) + " ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            } else if ("4".equals(param._avgDiv)) {
                final String[] majorcds = new String[_courses.size()];
                for (int i = 0; i < _courses.size(); i++) {
                    final Course course = (Course) _courses.get(i);
                    majorcds[i] = course._coursecd.substring(0, course._coursecd.length() - 4);
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD IN " + SQLUtils.whereIn(true, majorcds) + " ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD ");
            }

            final String sql = stb.toString();
            log.debug(" gradeAverage sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();

                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String electDiv = rs.getString("ELECTDIV");

                    final SubClass subclass = (SubClass) _subclasses.get(electDiv + subclassCd);
                    final BigDecimal subclassGradeAvg = rs.getBigDecimal("AVG");
                    if (subclass == null || subclassGradeAvg == null) {
                        //log.debug("subclass => " + subclass + " , gradeAvg => " + subclassGradeAvg);
                        continue;
                    }
                    //log.debug("subclass => " + subclass._subclassabbv + " , gradeAvg => " + subclassGradeAvg);
                    subclass._scoresubaverage = Form.sishaGonyu(subclassGradeAvg);
                }
            } catch (Exception e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 順位の算出
         */
        private List createRanking(final Param param) {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(param, student);
            }
            return list;
        }

        private static int[] setMaxMin(
                int maxInt,
                int minInt,
                int tergetInt
        ) {
            if (0 < tergetInt) {
                if (maxInt < tergetInt){ maxInt = tergetInt; }
                if (0 == minInt) {
                    minInt = tergetInt;
                } else {
                    if (minInt > tergetInt){ minInt = tergetInt; }
                }
            }
            return new int[]{maxInt, minInt};
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _hrclassCd.compareTo(that._hrclassCd);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class TransInfo {
        final String _date;
        final String _dateStr;
        final String _name;

        public TransInfo(
                final String date,
                final String dateStr,
                final String name
        ) {
            _date = date;
            _dateStr = dateStr;
            _name = name;
        }

        public String toString() {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _date) {
                sb.append(_dateStr);
            }
            if (null != _name) {
                sb.append(_name);
            }
            return sb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private static class Student implements Comparable {
        final int _gnum;  // 行番号
        final String _schregno;  // 学籍番号
        final HRInfo _hrInfo;
        private String _attendNo;
        private String _name;
        private TransInfo _transInfo;
        private String _scoreSum;
        private String _scoreAvg;
        private int _classRank;
        private int _rank;
        private final Map _scoreDetails = new TreeMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者
        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠課時分
//        private int _specialAbsent; // 特活欠課時数
//        private String _attendSemesRemarkDatRemark1;

        Student(final String code, final HRInfo hrInfo, final int gnum) {
            _gnum = gnum;
            _schregno = code;
            _hrInfo = hrInfo;
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Student)) return -1;
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 != rtn) return rtn;
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

//        /**
//         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
//         */
//        public boolean isGradeGood(final Param param) {
//            if (null == _total._avgBigDecimal) {
//                return false;
//            }
//            if (param._assess.floatValue() <= _total._avgBigDecimal.doubleValue()) {
//                return true;
//            }
//            return false;
//        }

//        /**
//         * @return 成績不振者（評定１が1つでもある）は true を戻します。
//         */
//        public boolean isGradePoor() {
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                final ScoreValue scorevalue = detail._score;
//                if (null == scorevalue || !StringUtils.isNumeric(scorevalue._strScore)) {
//                    continue;
//                }
//                if (scorevalue.getScoreAsInt() == 1) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        /**
//         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
//         */
//        public boolean isAttendPerfect() {
//            if (! _attendInfo.isAttendPerfect()) { return false; }
//
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                if (null != detail._absent && 0 < detail._absent.doubleValue()) {
//                    return false;
//                }
//            }
//            return true;
//        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        public boolean isKekkaOver(final Param param) {
            return null != getKekkaOverKamokuCount(param);
        }

        public String getKekkaOverKamokuCount(final Param param) {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                }
                if (detail._isOver) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenKamokuCount(final Param param) {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                }
                if (ScoreDetail.isFailCount(param, detail)) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenTanni(final Param param) {
            int credit = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                }
                if (ScoreDetail.isFailCount(param, detail) && null != detail._credits) {
                    credit += detail._credits.intValue();
                }
            }
            return credit == 0 ? null : String.valueOf(credit);
        }

        public String getRemark(final Param param) {
            String remark = "";
//            if (param._outputBiko && null != _attendSemesRemarkDatRemark1) {
//                remark += _attendSemesRemarkDatRemark1 + " ";
//            }
            remark += _transInfo.toString();  // 備考
            return remark;
        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : _attendNo;
        }

        public int kekkaAll(final Param param) {
            float total = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (null != detail._absent) {
                    final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                    if (0 != value) {
                        total += detail._absent.floatValue();
                    }
                }
            }
            return (int) total;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass {
        final String _classabbv;
        final String _classcode;
        final String _subclasscode;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _electdiv; // 選択科目
        final String _staffname;  // 科目担当者名
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private int _jisu;  // 授業時数
        private String _scoreaverage;  // 学級平均
        private String _scoresubaverage;  // 学年平均
        private String _scoretotal;  // 学級合計
        private String _scoreCount;  // 学級人数
        private String _scoreMax;  // 最高点
        private String _scoreMin;  // 最低点
        private String _scoreFailCnt;  // 欠点者数


        SubClass(
                final String subclasscode,
                final String classabbv,
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final int credit,
                final String staffname
        ) {
            _classabbv = classabbv;
            // (subclasscodeは頭1桁+科目コードなので教科コードは2文字目から2桁)
            _classcode = subclasscode.substring(1, 3);
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _staffname = staffname;
        }

//        public String toString() {
//            return _subclasscode + ":" + _abbv;
//        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscode + " , " +_subclassabbv + " , " +_electdiv + " , " +_maxcredit + " , " +_mincredit + " , " +_jisu +"]";
        }

        public String keySubclasscd() {
            return _subclasscode.substring(1);
        }

        public String getPrintCredit() {
            if (0 == _maxcredit) {
                return "";
            }
            return _maxcredit == _mincredit ? String.valueOf(_maxcredit) : String.valueOf(_mincredit) + " " + Param.FROM_TO_MARK + " " + String.valueOf(_maxcredit);
        }

        public String getJisu() {
            if (_jisu <= 0) {
                return "";
            }
            return String.valueOf(_jisu);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
        final SubClass _subClass;
        Double _absent;
        Integer _jisu;
        final String _score;
        final String _scoreDi;
        final String _assessLevel;
        final String _karihyotei;
        final Integer _replacemoto;
        final String _print_flg;
        final Integer _compCredit;
        final Integer _getCredit;
        BigDecimal _absenceHigh;
        final Integer _credits;
        boolean _isOver;
//        final String _chaircd;
//        final String _slump;
//        final String _slumpMark;
//        final String _slumpScore;

        ScoreDetail(
                final SubClass subClass,
                final String score,
                final String scoreDi,
                final String assessLevel,
                final String karihyotei,
                final Integer replacemoto,
                final String print_flg,
//                final String slump,
//                final String slumpMark,
//                final String slumpScore,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits
//                final String chaircd
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
            _assessLevel = assessLevel;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _print_flg = print_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
//            _chaircd = chaircd;
//            _slump = slump;
//            _slumpScore = slumpScore;
//            _slumpMark = slumpMark;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public Integer getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public Integer getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            if (null != _replacemoto && _replacemoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

//        private int getFailValue(final Param param) {
//            if (param.isPerfectRecord() && null != _passScore) {
//                return Integer.parseInt(_passScore);
//            } else if (param.isKetten() && !StringUtils.isBlank(param._ketten)) {
//                return Integer.parseInt(param._ketten);
//            }
//            return -1;
//        }

//        public static Boolean hoge(final Param param, final ScoreDetail detail) {
//            if (null != param._testItem._sidouinput) {
//                if (SIDOU_INPUT_INF_MARK.equals(param._testItem._sidouinputinf)) { // 記号
//                    if (null != param._d054Namecd2Max && null != detail._slumpMark) {
//                        if (param._d054Namecd2Max.equals(detail._slumpMark)) {
//                            return Boolean.TRUE;
//                        }
//                        return Boolean.FALSE;
//                    }
//                } else if (SIDOU_INPUT_INF_SCORE.equals(param._testItem._sidouinputinf)) { // 得点
//                    if (null != detail._slumpScore) {
//                        if ("1".equals(detail._slumpScore)) {
//                            return Boolean.TRUE;
//                        }
//                        return Boolean.FALSE;
//                    }
//                }
//            }
//            return null;
//        }

        private static boolean is5dankai(final Param param) {
            return param._testKindCd != null && param._testKindCd.endsWith("09");
        }

        private static boolean isFail(final Param param, final ScoreDetail detail) {
//            if (!is5dankai(param) && param._useSlumpSdivDatSlump) {
//                Boolean slump = hoge(param, detail);
//                if (null != slump) {
//                    return slump.booleanValue();
//                }
//            }
            if (is5dankai(param)) {
                return "1".equals(detail._score);
            }
            return "1".equals(detail._assessLevel);
        }

        private static boolean isFailCount(final Param param, final ScoreDetail detail) {
//            if (!is5dankai(param) && param._useSlumpSdivDatSlump) {
//                Boolean slump = hoge(param, detail);
//                if (null != slump) {
//                    return slump.booleanValue();
//                }
//            }
//            if (is5dankai(param)) {
//                return (!param._kettenshaSuuNiKessaShaWoFukumenai && "*".equals(detail._scoreDi)) || "1".equals(detail._score);
//            }
//            return (!param._kettenshaSuuNiKessaShaWoFukumenai && "*".equals(detail._scoreDi)) || "1".equals(detail._assessLevel);
            return isFail(param, detail);
        }

        public String toString() {
            return (_subClass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isOver);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private static class Total implements Comparable {
        final int _total;  // 総合点
        final int _count;  // 件数（成績）
        final BigDecimal _avgBigDecimal;  // 平均点
        final int _countFail;  //欠点科目数

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
         * @param student
         */
        Total(final Param param, final Student student) {

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            int countFail = 0;

            for (final Iterator it = student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                }

                final String scoreValue = detail._score;
                if (isAddTotal(detail._replacemoto, param)) {
                    if (null != scoreValue && StringUtils.isNumeric(scoreValue)) {
                        total += Integer.parseInt(scoreValue);
                        count++;
                    }
                    if (ScoreDetail.isFailCount(param, detail)) {
                        countFail++;
                    }
                }

                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }

                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                }
            }

//            int specialAbsent = 0;
//            for (final Iterator it = student._spGroupAbsentMinutes.values().iterator(); it.hasNext();) {
//                final Integer groupAbsentMinutes = (Integer) it.next();
//                specialAbsent += getSpecialAttendExe(param, groupAbsentMinutes.intValue());
//            }

            _total = total;
            _count = count;
            if (0 < count) {
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            } else {
                _avgBigDecimal = null;
            }
            if (0 < compCredit) {
                student._compCredit = compCredit;
            }
            if (0 < getCredit) {
                student._getCredit = getCredit;
            }
//            student._specialAbsent = specialAbsent;
            _countFail = countFail;
        }

//        /**
//         * 欠課時分を欠課時数に換算した値を得る
//         * @param kekka 欠課時分
//         * @return 欠課時分を欠課時数に換算した値
//         */
//        private int getSpecialAttendExe(final Param param, final int kekka) {
//            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
//            final BigDecimal bigKekka = new BigDecimal(kekka);
//            final BigDecimal bigJitu = new BigDecimal(jituJifun);
//            final BigDecimal bigD = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN);
//            final String retSt = bigD.toString();
//            final int retIndex = retSt.indexOf(".");
//            int seisu = 0;
//            if (retIndex > 0) {
//                seisu = Integer.parseInt(retSt.substring(0, retIndex));
//                final int hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
//                seisu = hasu < 5 ? seisu : seisu + 1;
//            } else {
//                seisu = Integer.parseInt(retSt);
//            }
//            return seisu;
//        }

        /**
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final Integer replacemoto,
                final Param param
        ) {
//            if (null == scoreValue || !scoreValue.hasIntValue()) { return false; }
            if (param._isGakunenMatu && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Total)) return -1;
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Total)) return false;
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

    static class Form {

        public boolean print(final Vrw32alp svf, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
            final List studentListList = getStudentListList(hrInfo._students, param._formMaxLine);
            for (final Iterator it = studentListList.iterator(); it.hasNext();) {
                final List studentList = (List) it.next();
                if (Form1.print(svf, param, hrInfo, studentList)) {
                    hasData = true;
                }
            }
            return hasData;
        }

//        public boolean outputCsv(final List outputList, final Param param, final HRInfo hrInfo) {
//            boolean hasData = false;
//            if (Form1.outputCsv(outputList, param, hrInfo, hrInfo._students)) {
//                hasData = true;
//            }
//            return hasData;
//        }

        private static String sishaGonyu(final BigDecimal bd) {
            return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static List newLine(final List listList) {
            final List line = line();
            listList.add(line);
            return line;
        }

        private static List line() {
            return line(0);
        }

        private static List line(final int size) {
            final List line = new ArrayList();
            for (int i = 0; i < size; i++) {
                line.add(null);
            }
            return line;
        }

        private static int currentColumn(final List lineList) {
            int max = 0;
            for (final Iterator it = lineList.iterator(); it.hasNext();) {
                final List line = (List) it.next();
                max = Math.max(max, line.size());
            }
            return max;
        }

        private static List setSameSize(final List list, final int max) {
            for (int i = list.size(); i < max; i++) {
                list.add(null);
            }
            return list;
        }

        private static List getStudentListList(final List students, final int count) {
            final List rtn = new ArrayList();
            List current = null;
            int page = 0;
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final int cpage = student._gnum / count + (student._gnum % count != 0 ? 1 : 0);
                if (null == current || page < cpage) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(student);
                page = cpage;
            }
            return rtn;
        }

        private static List getSubClassListList(final Param param, final Collection subclasses, final int count) {
            final List rtn = new ArrayList();
            List current = null;
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                final boolean notOutputColumn = "90".equals(subClass._classcode) && param._notOutputSougou;
                if (notOutputColumn) {
                    continue;
                }
                if (null == current || current.size() >= count) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(subClass);
            }
            return rtn;
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

        private static String zeroToNull(final int num) {
            return num == 0 ? null : String.valueOf(num);
        }

        private static String nullToBlank(final Object o) {
            return null == o ? null : o.toString();
        }

        /**
         *
         * @param svf
         * @param field
         * @param line
         * @param data
         */
        private static void svfsetString1(final Vrw32alp svf, final String field, final int line, final String pf, final int col, final String data) {
            svf.VrsOutn(field, line, data);
        }

        private static int gnumToLine(final Param param, final int gnum) {
            return param._formMaxLine == gnum ? param._formMaxLine : gnum % param._formMaxLine;
        }

        static class Form1 {

            private static boolean print(
                    final Vrw32alp svf,
                    final Param param,
                    final HRInfo hrInfo,
                    final List stulist
            ) {
                boolean hasData = false;
                svf.VrSetForm(param._formname, 4);

                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = (SubClass) it.next();
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd())._isSaki) {
                        it.remove();
                    }
                }

                final List subclassListList = getSubClassListList(param, printSubclassList, param._formMaxColumn);

                for (int p = 0, pages = subclassListList.size(); p < pages; p++) {
                    final List subclassList = (List) subclassListList.get(p);

                    svf.VrSetForm(param._formname, 4);
                    printHeader(svf, param, hrInfo);
                    for (final Iterator it = stulist.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        printStudentName(svf, param, student);

                        if (p == pages - 1) {
                            printStudentTotal(svf, param, student);
                            printHrInfo(svf, param, hrInfo);
                        }
                    }

                    for (int i = 0, size = subclassList.size(); i < size; i++) {

                        final SubClass subclass = (SubClass) subclassList.get(i);
                        log.debug("p=" + p + ", i=" + i + ", subclasscd=" + subclass._subclasscode + " " + subclass._subclassabbv);
                        printSubclasses(svf, param, i + 1, subclass);

                        for (final Iterator it = stulist.iterator(); it.hasNext();) {
                            final Student student = (Student) it.next();
                            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                                final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                                printDetail(svf, param, detail, gnumToLine(param, student._gnum), i + 1);
                            } else {
                                svf.VrAttribute("SCORE" + String.valueOf(student._gnum), attributeBackground(student._gnum));
                                svf.VrAttribute("kekka2_" + String.valueOf(student._gnum), attributeBackground(student._gnum));
                            }
                        }

                        //学級平均・合計
                        printSubclassStat(svf, param, i + 1, subclass);

                        svf.VrEndRecord();
                        hasData = true;
                    }

                    for (int col = subclassList.size(); col < param._formMaxColumn; col++) {
                        //教科名
                        svf.VrsOut("course1", String.valueOf(col));
                        svf.VrAttribute("course1", "X=10000");
                        for (int j = 0; j < stulist.size(); j++) {
                            final Student student = (Student) stulist.get(j);
                            svf.VrAttribute("SCORE" + String.valueOf(student._gnum), attributeBackground(student._gnum));
                            svf.VrAttribute("kekka2_" + String.valueOf(student._gnum), attributeBackground(student._gnum));
                        }
                        svf.VrEndRecord();
                    }
                }
                return hasData;
            }

//            public static boolean outputCsv(final List outputList, final Param param, final HRInfo hrInfo, final List stulist) {
//
//                boolean hasData = false;
////                final Vrw32alp svf = null;
//
//                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());
//                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
//                    final SubClass subclass = (SubClass) it.next();
//                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd())._isSaki) {
//                        it.remove();
//                    }
//                }
//
//                param._formMaxLine = printSubclassList.size();
//                param._formMaxColumn = stulist.size();
//
//                //printHeader(svf, param, hrInfo);
//                final List headerLineList = new ArrayList();
//                final List header1Line = newLine(headerLineList);
//                final String title = KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度　" + param._title;
//                header1Line.addAll(Arrays.asList(new String[] {"", title}));
//
//                final List header2Line = newLine(headerLineList);
//                header2Line.addAll(Arrays.asList(new String[] {hrInfo._hrName, "", "出欠集計範囲：" + param.getTermKekka(), "", "", "", "", "", "担任：" + StringUtils.defaultString(hrInfo._staffName)}));
//
//                final List blockStudentName = new ArrayList();
//                List nameLine0 = newLine(blockStudentName);
//                List nameLine1 = newLine(blockStudentName);
//
//                nameLine0.add("教科");
//                nameLine0.add("科目");
//                nameLine0.add("単位数");
//                if (null != fieldClassTeacher(param, "")) {
//                    nameLine0.add("教科担任名");
//                }
//                nameLine0.add("授業時数");
//                nameLine0.add(param._item1Name + "・" + param._item2Name);
//                nameLine0.add("");
//                final int headerSize = nameLine0.size();
//                setSameSize(nameLine1, headerSize);
//
//                for (final Iterator its = stulist.iterator(); its.hasNext();) {
//
//                    final Student student = (Student) its.next();
//
//                    //printStudentName(svf, param, student);
//
//                    nameLine0.add(student.getPrintAttendno());
//                    nameLine1.add(student._name);
//                }
//
//                nameLine0.add("合計");
//                nameLine0.add("学級平均");
//                nameLine0.add(StringUtils.defaultString(param._rankName) + "平均");
//                nameLine0.add(!param._useKetten ? "" : "欠点者数");
//                nameLine0.add("最高点");
//                nameLine0.add("最低点");
//
//                final List blockSubclassList = new ArrayList();
//
//                String classabbvbefore = null;
//
//                for (int coli = 0, size = printSubclassList.size(); coli < size; coli++) {
//
//                    final List line1 = newLine(blockSubclassList);
//                    List line2 = null;
//
//                    final SubClass subclass = (SubClass) printSubclassList.get(coli);
//                    //final int col = coli + 1;
//
//                    //printSubclasses1(svf, param, col, subclass);
//
//                    final boolean diff = !(null == subclass._classabbv && null == classabbvbefore || null != subclass._classabbv && subclass._classabbv.equals(classabbvbefore));
//                    line1.add(diff ? subclass._classabbv : "");
//                    line1.add(subclass._subclassname);
//                    line1.add(subclass.getPrintCredit());
//                    if (null != fieldClassTeacher(param, subclass._staffname)) {
//                        line1.add(subclass._staffname);
//                    }
//                    line1.add(subclass.getJisu());
//                    line2 = setSameSize(newLine(blockSubclassList), line1.size());
//
//                    line1.add(param._item1Name);
//                    line1.add("");
//                    line2.add(param._item2Name);
//                    line2.add("");
//
//                    for (final Iterator its = stulist.iterator(); its.hasNext();) {
//
//                        final Student student = (Student) its.next();
//                        //final int line = gnumToLine(param, student._gnum);
//
//                        // 欠課
//                        List scoreLine = null;
//                        List absenceLine = null;
//                        scoreLine = line1;
//                        absenceLine = line2;
//                        if (student._scoreDetails.containsKey(subclass._subclasscode)) {
//                            final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
//                            //printDetail(svf, param, detail, line, col);
//
//                            if (null != scoreLine) {
//                                final String aster;
//                                if ("990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
//                                    aster = "*";
//                                } else {
//                                    aster = "";
//                                }
//                                final String printScore;
//                                if (null == detail._score) {
//                                    printScore = StringUtils.defaultString(detail._scoreDi);
//                                } else {
//                                    printScore = StringUtils.defaultString(detail._score);
//                                }
//                                scoreLine.add(aster + printScore);
//                            }
//
//                            if (null != absenceLine) {
//                                if (null != detail._absent) {
//                                    final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
//                                    if (0 != value) {
//                                        absenceLine.add(param.getAbsentFmt().format(detail._absent.floatValue()));
//                                    } else {
//                                        absenceLine.add("");
//                                    }
//                                } else {
//                                    absenceLine.add("");
//                                }
//                            }
//                        } else {
//                            if (null != scoreLine) {
//                                scoreLine.add("");
//                            }
//                            if (null != absenceLine) {
//                                absenceLine.add("");
//                            }
//                        }
//                    }
//
//                    //学級平均・合計
//                    //printSubclassStat(svf, param, col, subclass);
//                    if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
//                        line1.add(StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
//                    } else {
//                        line1.add("");
//                    }
//                    line1.add(subclass._scoreaverage);
//                    line1.add(subclass._scoresubaverage);
//                    line1.add(param._useKetten ? subclass._scoreFailCnt : "");
//                    line1.add(subclass._scoreMax);
//                    line1.add(subclass._scoreMin);
//
//                    hasData = true;
//                    classabbvbefore = subclass._classabbv;
//                }
//
//                int totalColumns = 1;
//                totalColumns = 13 + 1;
//                final List[] columnsStudentTotalHeader = new List[totalColumns];
//                for (int i = 0; i < columnsStudentTotalHeader.length; i++) {
//                    columnsStudentTotalHeader[i] = new ArrayList();
//
//                    for (int j = 0; j < headerSize - 1; j++) {
//                        columnsStudentTotalHeader[i].add(null);
//                    }
//                }
//                int j = 0;
//                columnsStudentTotalHeader[j++].add(param._item4Name);
//                columnsStudentTotalHeader[j++].add(param._item5Name);
//                columnsStudentTotalHeader[j++].add("学級順位");
//                columnsStudentTotalHeader[j++].add(param._rankName + "順位");
//                columnsStudentTotalHeader[j++].add(!param._useKetten ? "" : "欠点科目数");
//
//                columnsStudentTotalHeader[j++].add("出停");
//                columnsStudentTotalHeader[j++].add("忌引");
//                columnsStudentTotalHeader[j++].add("留学中の授業日数");
//                columnsStudentTotalHeader[j++].add("出席すべき日数");
//                columnsStudentTotalHeader[j++].add("出席日数");
//                columnsStudentTotalHeader[j++].add("欠席日数");
//                columnsStudentTotalHeader[j++].add("遅刻回数");
//                columnsStudentTotalHeader[j++].add("早退回数");
//                columnsStudentTotalHeader[j++].add("備考");
//                final List[] columnsStudentTotal = new List[totalColumns];
//                for (int i = 0; i < columnsStudentTotal.length; i++) {
//                    columnsStudentTotal[i] = new ArrayList();
//                }
//                for (final Iterator its = stulist.iterator(); its.hasNext();) {
//
//                    final Student student = (Student) its.next();
//
//                    //printStudentTotal(svf, param, student);
//
//                    int i = 0;
//                    final AttendInfo attendInfo = student._attendInfo;
//                    final String scoreAvg = !param._testKindCd.startsWith("99") ? "" : student._scoreAvg;
//                    final String classRank = 0 >= student._classRank ? "" : String.valueOf(student._classRank);
//                    final String rank = 0 >= student._rank ? "" : String.valueOf(student._rank);
//                    final String countFail = !param._useKetten || 0 >= student._total._countFail ? "" : String.valueOf(student._total._countFail);
//                    columnsStudentTotal[i++].add(student._scoreSum);
//                    columnsStudentTotal[i++].add(scoreAvg);
//                    columnsStudentTotal[i++].add(classRank);
//                    columnsStudentTotal[i++].add(rank);
//                    columnsStudentTotal[i++].add(countFail);
//
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._suspend));
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._mourning));
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._transDays));
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._mLesson));
//                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._present));
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._absent));
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._late));
//                    columnsStudentTotal[i++].add(zeroToNull(attendInfo._early));
//
//                    //printRemark(svf, param, student);
//                    columnsStudentTotal[i++].add(student.getRemark(param));
//
//                }
//
//                final List columnsStudentTotalAll = new ArrayList();
//                setColumnList(columnsStudentTotalAll, totalColumns);
//                joinColumnListArray(columnsStudentTotalAll, columnsStudentTotalHeader);
//                joinColumnListArray(columnsStudentTotalAll, columnsStudentTotal);
//
//                //printHrInfo(svf, param, hrInfo);
//                List[] columnStudentTotalFooterList = new List[totalColumns];
//                for (int i = 0; i < columnStudentTotalFooterList.length; i++) {
//                    columnStudentTotalFooterList[i] = new ArrayList();
//                }
//
//                //学級合計
//                String gavg = null;
//                String gtotal = null;
//                if (OUTPUT_RANK1.equals(param._outputRank)) {
//                    gavg = hrInfo._avgHrAverage;
//                    gtotal = hrInfo._avgHrTotal;
//                } else if (OUTPUT_RANK2.equals(param._outputRank)) {
//                    gavg = hrInfo._avgGradeAverage;
//                    gtotal = hrInfo._avgGradeTotal;
//                } else if (OUTPUT_RANK3.equals(param._outputRank)) {
//                    gavg = hrInfo._avgCourseAverage;
//                    gtotal = hrInfo._avgCourseTotal;
//                } else if (OUTPUT_RANK4.equals(param._outputRank)) {
//                    gavg = hrInfo._avgMajorAverage;
//                    gtotal = hrInfo._avgMajorTotal;
//                }
//
//                setSameSize(columnStudentTotalFooterList[0], 7);
//                columnStudentTotalFooterList[0].set(1, null == hrInfo._avgHrTotal ? "" : hrInfo._avgHrTotal);
//                columnStudentTotalFooterList[0].set(2, null != gtotal ? "" : gtotal);
//                columnStudentTotalFooterList[0].set(3, param._isSapporo || null == hrInfo._failHrTotal ? "" : hrInfo._failHrTotal);
//                columnStudentTotalFooterList[0].set(4, null == hrInfo._maxHrTotal ? "" : hrInfo._maxHrTotal);
//                columnStudentTotalFooterList[0].set(5, null == hrInfo._minHrTotal ? "" : hrInfo._minHrTotal);
//                setSameSize(columnStudentTotalFooterList[1], 3);
//                columnStudentTotalFooterList[1].set(1, !param._testKindCd.startsWith("99") || null == hrInfo._avgHrAverage ? "" : hrInfo._avgHrAverage);
//                columnStudentTotalFooterList[1].set(2, !param._testKindCd.startsWith("99") || null == gavg ? "" : gavg);
//
//                setSameSize(columnStudentTotalFooterList[5], 8);
//                columnStudentTotalFooterList[5].set(7, "出席率 " + (null == hrInfo._perHrPresent ? "" : sishaGonyu(hrInfo._perHrPresent) + "%"));
//                setSameSize(columnStudentTotalFooterList[6], 8);
//                columnStudentTotalFooterList[6].set(7, "欠席率 " + (null == hrInfo._perHrAbsent ? "" : sishaGonyu(hrInfo._perHrAbsent) + "%"));
//                joinColumnListArray(columnsStudentTotalAll, columnStudentTotalFooterList);
//
//
//                final List columnList = new ArrayList();
//                columnList.addAll(blockStudentName);
//                columnList.addAll(blockSubclassList);
//                columnList.addAll(columnsStudentTotalAll);
//
//                outputList.addAll(headerLineList);
//                outputList.addAll(columnListToLines(columnList));
//                newLine(outputList); // ブランク
//                newLine(outputList); // ブランク
//
//                return hasData;
//            }
//
//            private static List columnListToLines(final List columnList) {
//                final List lines = new ArrayList();
//                int maxLine = 0;
//                for (int ci = 0; ci < columnList.size(); ci++) {
//                    final List column = (List) columnList.get(ci);
//                    maxLine = Math.max(maxLine, column.size());
//                }
//                for (int li = 0; li < maxLine; li++) {
//                    lines.add(line(columnList.size()));
//                }
//                for (int ci = 0; ci < columnList.size(); ci++) {
//                    final List column = (List) columnList.get(ci);
//                    for (int li = 0; li < column.size(); li++) {
//                        ((List) lines.get(li)).set(ci, column.get(li));
//                    }
//                }
//                return lines;
//            }
//
//            private static List setColumnList(final List columnsList, final int column) {
//                for (int i = columnsList.size(); i < column; i++) {
//                    columnsList.add(new ArrayList());
//                }
//                return columnsList;
//            }
//
//            private static List joinColumnListArray(final List columnsList, final List[] columnStudentFooterList) {
//                for (int i = 0; i < columnStudentFooterList.length; i++) {
//                    ((List) columnsList.get(i)).addAll(columnStudentFooterList[i]);
//                }
//                return columnsList;
//            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param svf
             * @param hrInfo
             */
            private static void printHeader(
                    final Vrw32alp svf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                svf.VrsOut("ymd1", param._now); // 作成日
                svf.VrsOut("DATE", param._termKekka);  // 欠課の集計範囲

                svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrInfo._staffName) > 30 ? "2" : ""), hrInfo._staffName);  //担任名
//                if (!PATTERN1.equals(param._outputPattern) && !PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
//                    svf.VrsOut("COURSE", hrInfo._hrName);  //組名称
//                }
                svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
//                if (param._hasCompCredit) {
//                    svf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
//                }
//                if (!PATTERN1.equals(param._outputPattern) && !PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
//                    svf.VrsOut("lesson20", hrInfo._HrMLesson);  // 授業日数
//                }
                svf.VrsOut("ITEM7", param._rankName + "順位");
                svf.VrsOut("ITEM4", param._item4Name);
                svf.VrsOut("ITEM5", param._item5Name);
                svf.VrsOut("ITEM6", param._item1Name + "・" + param._item2Name);
                svf.VrsOut("ITEM8", param._rankName);
                if (param._useKetten) {
                    svf.VrsOut("ITEM9", "欠点科目数");
                    svf.VrsOut("ITEM10", "欠点者数");
                }

                // 一覧表枠外の文言
                svf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKAOVER);
                svf.VrsOut("NOTE1",  " " );
                svf.VrsOut("NOTE2",  "：欠課時数超過者" );
                if (param._useKetten) {
//                    svf.VrAttribute("NOTE3",  ATTRIBUTE_KETTEN);
//                    svf.VrsOut("NOTE3",  " " );
                    svf.VrAttribute("NOTE4",  ATTRIBUTE_KETTEN);
                    svf.VrsOut("NOTE4",  "欠点" );
                }

//              if (_param instanceof KNJD065_GRADE) {
//                  svf.setString("TITLE", _param._semesterName + "成績一覧表"); //成績名称
//                  svf.setString("MARK1_2",  _param._assess.toString());
//              }

                svf.VrsOut("TITLE", param._nendo + "　" + param._title);
//                if (!PATTERN1.equals(param._outputPattern) && !PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
//                    if (param._printPrgid) {
//                        svf.VrsOut("PRGID", param._prgId);
//                        svf.VrsOut("MARK", "/");
//                    }
//                }

                for (int i = 1; i <= 8; i++) {
                    final String name1 = (String) param._d055Name1Map.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    final String field = "JOB" + String.valueOf(i) + (getMS932ByteLength(name1) > 8  ? "_2" : "_1");
                    svf.VrsOut(field, name1);
                }
            }

            /**
             * 学級データの印字処理(学級平均)
             * @param svf
             */
            private static void printHrInfo(
                    final Vrw32alp svf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                final int col = -1;
                //学級合計
                String gavg = null;
                String gtotal = null;
                if (OUTPUT_RANK1.equals(param._outputRank)) {
                    gavg = hrInfo._avgHrAverage;
                    gtotal = hrInfo._avgHrTotal;
                } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                    gavg = hrInfo._avgGradeAverage;
                    gtotal = hrInfo._avgGradeTotal;
                } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                    gavg = hrInfo._avgCourseAverage;
                    gtotal = hrInfo._avgCourseTotal;
                } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                    gavg = hrInfo._avgMajorAverage;
                    gtotal = hrInfo._avgMajorTotal;
                }

//                if (null != hrInfo._avgHrTotal) {
//                    svfsetString1(svf, "TOTAL1S", 1, "", col, hrInfo._avgHrTotal); // 学級合計
//                }
                //学級合計の母集団の生徒数
                if (0 < hrInfo._avgHrCount) {
//                    svfsetString1(svf, "TOTAL1S", 2, "", col, String.valueOf(hrInfo._avgHrCount)); // 学級合計の母集団の生徒数
                    svfsetString1(svf, "TOTAL1S", 2, "", col, String.valueOf(hrInfo._avgHrTotal));
                }
                if (null != gtotal) {
                    svfsetString1(svf, "TOTAL1S", 3, "", col, gtotal); // 学年合計
                }
                // 学級平均
                if (null != hrInfo._avgHrAverage) {
                    svfsetString1(svf, "AVERAGE1S", 2, "", col, hrInfo._avgHrAverage); // 学級平均
                }
                if (null != gavg) {
                    svfsetString1(svf, "AVERAGE1S", 3, "", col, gavg); // 指定母集団平均
                }
                //欠点者数
                if (null != hrInfo._failHrTotal) {
                    svfsetString1(svf, "TOTAL1S", 4, "", col, hrInfo._failHrTotal); // 欠点者数
                }
                //最高点
                if (null != hrInfo._maxHrTotal) {
                    svfsetString1(svf, "TOTAL1S", 5, "", col, hrInfo._maxHrTotal); // 最高点
                }
                //最低点
                if (null != hrInfo._minHrTotal) {
                    svfsetString1(svf, "TOTAL1S", 6, "", col, hrInfo._minHrTotal); // 最低点
                }
            }

            /**
             * 生徒別総合点・平均点・順位を印刷します。
             * @param svf
             */
            private static void printStudentTotal(
                    final Vrw32alp svf,
                    final Param param,
                    final Student student
            ) {
                final int line = gnumToLine(param, student._gnum);
                if (null != student._scoreSum) {
                    svf.VrsOutn(param._fieldSeitoSogoten, line, student._scoreSum);  //総合点
                    svf.VrsOutn(param._fieldSeitoHeikinten, line, student._scoreAvg);  //平均点
                }
                //順位（学級）
                if (1 <= student._classRank) {
                    svf.VrsOutn(param._fieldSeitoClassRank, line, String.valueOf(student._classRank));
                }
                //順位（学年orコース）
                if (1 <= student._rank) {
                    svf.VrsOutn(param._fieldSeitoSiteiRank, line, String.valueOf(student._rank));
                }
                //欠点科目数
                if (param._useKetten) {
                    if (0 < student._total._countFail) {
                        svf.VrsOutn(param._fieldSeitoKettenKamoku, line, String.valueOf(student._total._countFail));
                    }
                }
                svf.VrsOutn("SICK", line, zeroToNull(student.kekkaAll(param)));
                final String[] fields = {param._fieldSeitoSogoten, param._fieldSeitoHeikinten, param._fieldSeitoClassRank, param._fieldSeitoSiteiRank, param._fieldSeitoKettenKamoku, "SICK"};
                for (int i = 0; i < fields.length; i++) {
                    svf.VrAttributen(fields[i], line, attributeBackground(line));
                }
//                if (param._hasCompCredit) {
//                    svf.VrsOutn("COMP_CREDIT", line, zeroToNull(student._compCredit)); //今年度履修単位数
//                    svf.VrsOutn("GET_CREDIT", line, zeroToNull(student._getCredit)); //今年度修得単位数
//                }
//                if (param._hasJudgementItem) { // 前年度までの単位数を印字
//                    // 各単位数、未履修科目数を印字
//                    svf.VrsOutn("A_CREDIT", line, zeroToNull(student._qualifiedCredits)); // 今年度認定単位数
//                    svf.VrsOutn("PRE_C_CREDIT", line, zeroToNull(student._previousCredits)); // 前年度までの修得単位数
//                    final int t = student._getCredit + student._qualifiedCredits + student._previousCredits; // 修得単位数計
//                    if (t != 0) {
//                        final String at = (param._gradCredits != 0 && param._gradCredits <= t) ? "@" : "";
//                        svf.VrsOutn("TOTAL_C_CREDIT", line, at + String.valueOf(t));
//                    }
//                    svf.VrsOutn("PRE_N_CREDIT", line, zeroToNull(student._previousMirisyu)); // 前年度までの未履修科目数
//                }
//                if (0 < student._specialAbsent) {
//                    svf.VrsOutn("SP_KEKKA", line, zeroToNull(student._specialAbsent));
//                }
            }

            /**
             * 生徒の氏名・備考を印字
             * @param svf
             * @param hrInfo
             * @param stulist：List hrInfo._studentsのsublist
             */
            private static void printStudentName(
                    final Vrw32alp svf,
                    final Param param,
                    final Student student
            ) {
                final int line = gnumToLine(param, student._gnum);
                svf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                // 学籍番号表記
                String nameNo = "1";
                if ("1".equals(param._use_SchregNo_hyoji)) {
                    svf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                    nameNo = "2" + (getMS932ByteLength(student._name) > 16 ? "_2" : "_1"); // 学籍番号表示用の氏名フィールド
                }
                svf.VrsOutn("NAME" + nameNo, line, student._name);    // 氏名
                svf.VrAttributen("NAME" + nameNo, line, attributeBackground(student._gnum));
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param svf
             * @param subclass
             * @param line：科目の列番号
             * @param stulist：List hrInfo._studentsのsublist
             * @return
             */
            private static void printSubclasses(
                    final Vrw32alp svf,
                    final Param param,
                    final int col,
                    final SubClass subclass
            ) {
                //教科名
                svf.VrsOut("course1", subclass._classabbv);
                //科目名
                final String subclassfield = "subject1";
                if (subclass._electdiv) { svf.VrAttribute(subclassfield, ATTRIBUTE_ELECTDIV); }
                svf.VrsOut(subclassfield, subclass._subclassabbv);
                if (subclass._electdiv) { svf.VrAttribute(subclassfield, ATTRIBUTE_KETTEN_NORMAL); }
                //単位数
                svf.VrsOut("credit1", subclass.getPrintCredit());
                svf.VrsOut("CLASS_TEACHER", subclass._staffname);
                //授業時数
                svf.VrsOut("lesson1", subclass.getJisu());
                //項目名
                svf.VrsOut("ITEM1", param._item1Name);
                svf.VrsOut("ITEM2", param._item2Name);
            }

            public static void printSubclassStat(final Vrw32alp svf, final Param param, final int col, final SubClass subclass) {
                svf.VrsOut("AVE_CLASS", subclass._scoreaverage);
                svf.VrsOut("AVE_SUBCLASS", subclass._scoresubaverage);
                if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                    svf.VrsOut("TOTAL_SUBCLASS", StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
                }
                svf.VrsOut("MAX_SCORE", subclass._scoreMax);
                svf.VrsOut("MIN_SCORE", subclass._scoreMin);
                if (param._useKetten) {
                    svf.VrsOut("FAIL_STD", subclass._scoreFailCnt);
                }
            }

            private static String addAttribute(final String attr1, final String attr2) {
                if (StringUtils.isEmpty(attr1) && StringUtils.isEmpty(attr2)) {
                    return "";
                }
                if (StringUtils.isEmpty(attr2)) {
                    return attr1;
                }
                if (StringUtils.isEmpty(attr1)) {
                    return attr2;
                }
                return attr1 + "," + attr2;
            }

            /**
             * 生徒別科目別素点・評定・欠課時数等を印刷します。
             * @param svf
             * @param line 生徒の行番
             */
            private static void printDetail(
                    final Vrw32alp svf,
                    final Param param,
                    final ScoreDetail detail,
                    final int line,
                    final int col
            ) {
                final String scoreField = "SCORE";
                final String attrBackground = attributeBackground(line);
                String attrKetten = "";
                if (param._useKetten && ScoreDetail.isFail(param, detail)) { attrKetten = ATTRIBUTE_KETTEN; }
                svf.VrAttribute(scoreField + String.valueOf(line) + "", addAttribute(attrKetten, attrBackground));
                final String aster;
//                if ("990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
//                    aster = "*";
//                } else {
                    aster = "";
//                }
                final String printScore;
                if (null == detail._score) {
                    printScore = StringUtils.defaultString(detail._scoreDi);
                } else {
                    printScore = StringUtils.defaultString(detail._score);
                }
                svf.VrsOut(scoreField + String.valueOf(line) + "", aster + printScore);
                if (param._useKetten && ScoreDetail.isFail(param, detail)) { attrKetten = ATTRIBUTE_KETTEN_NORMAL; }
                svf.VrAttribute(scoreField + String.valueOf(line) + "", addAttribute(attrKetten, attrBackground));

                // 欠課
                boolean setKekkaAttr = false;
                final String field;
                if (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) {
                    field = "kekka2_";
                } else {
                    field = "kekka";
                }
                if (null != detail._absent) {
                    final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                    if (0 != value) {
                        String attr = attrBackground;
                        if (detail._isOver) { attr = ATTRIBUTE_KEKKAOVER; }
                        svf.VrAttribute(field + String.valueOf(line), attr);
                        svf.VrsOut(field + String.valueOf(line), param.getAbsentFmt().format(detail._absent.floatValue()));
//                        attr = attrBackground;
//                        svf.VrAttribute(field + String.valueOf(line), attr);
                        setKekkaAttr = true;
                    }
                }
                if (!setKekkaAttr) {
                    svf.VrAttribute(field + String.valueOf(line), attrBackground);
                }
            }
        }
    }

    private static class TestItem {

        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String toString() {
            return "TestItem(" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class SubclassMst {
        final String _subclasscode;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String subclasscode, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final boolean isSaki, final boolean isMoto) {
            _subclasscode = subclasscode;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
//        String str = "なし";
//        if (param.isRecordSlump())     str = "RECORD_SLUMP_DAT";
//        if (param.isPerfectRecord())   str = "PERFECT_RECORD_DAT";
//        if (param.isKetten())          str = "指示画面の欠点";
//        log.debug("欠点参照：" + str);
        return param;
    }

    protected static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;

        final String _nendo;
        final String _now;
        final String _termKekka;
        /** 学年 */
        final String _grade;
        final String[] _classSelected;
        /** 出欠集計日付 */
        final String _date;
        final String _sDate;
        final String _testKindCd;

        /** 総合順位出力 1:学級 2:学年 3:コース 4:学科 */
        final String _outputRank;
        /** 順位の基準点 1:総合点 2:平均点 */
        final String _outputKijun;
        /** フォーム 1:科目固定型 2:科目変動型 3:成績と出欠の記録 4:欠課時数と出欠の記録 5:仮評定と出欠の記録 */
//        final String _outputPattern;
//        /** 欠点 */
//        final String _ketten;
        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
        final String _checkKettenDiv;
        /** 起動元のプログラムＩＤ */
        final String _prgId;
        /** 成績優良者評定平均の基準値===>KNJD615：未使用 */
        final Float _assess;

        private String _cmd;

        /** フォーム（1:４５名、2:５０名）*/
//        final String _formSelect;
        int _formMaxLine;

        /** 科目数　（1:15科目、2:20科目) */
//        final String _subclassMax;
        int _formMaxColumn;
        final String _formname;

//        /** 同一クラスでのコース毎に改頁あり */
//        final boolean _outputCourse;
        /** "総合的な学習の時間"を表示しない */
        final boolean _notOutputSougou;
//        /** "総合的な学習の時間"を表示しない */
//        final boolean _useSlumpSdivDatSlump;
        /** 欠点者数に欠査者を含めない */
        final boolean _kettenshaSuuNiKessaShaWoFukumenai;
        /** 備考欄出力（出欠備考を出力） */
        final boolean _outputBiko;
        /** 1:全て/2:学期から/3:年間まとめ */
        final String _bikoKind;
        final String _schoolName;

        private String FORM_FILE;

        private String _yearDateS;
        private String _semesterName;
        private String _semesterDateS;
        private TestItem _testItem;

        private static final String FROM_TO_MARK = "\uFF5E";

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

//        final String _knjd615vPrintNullRemark;

        private KNJSchoolMst _knjSchoolMst;

        private KNJ_Get_Info _getinfo;

        private int _gradCredits;  // 卒業認定単位数

        final boolean _useKetten;

        private String _rankName;
        private String _rankFieldName;
        private String _avgDiv;
//        private String _d054Namecd2Max;
//        private String _sidouHyoji;
        private Map _d055Name1Map;
        private Map _subclassMst;
        private boolean _isPrintSakiKamoku;

        private final String SUBCLASSCD999999;

//      String _fieldChaircd;
        private String _title;
        private String _mark;
        private String _item1Name;  // 明細項目名
        private String _item2Name;  // 明細項目名
        private String _item4Name;  // 総合点欄項目名
        private String _item5Name;  // 平均点欄項目名
        private String _form2Item4Name;  // 平均点欄項目名
        private String _form2Item5Name;  // 平均点欄項目名
//        private boolean _creditDrop;
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
//        private boolean _hasCompCredit; // 履修単位数/修得単位数なし
//        private boolean _hasJudgementItem; // 判定会議資料用の項目あり
        private boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
        private boolean _isPrintDetailTani; // 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
        private int _failValue; // 欠点 100段階：30未満 5段階： 2未満
//      String _fieldname;
//      String _fieldname2;
        private boolean _printPrgid;
        final Map _psMap = new HashMap();

        protected final String _fieldSeitoSogoten;
        protected final String _fieldSeitoHeikinten;
        protected final String _fieldSeitoClassRank;
        protected final String _fieldSeitoSiteiRank;
        protected final String _fieldSeitoKettenKamoku;
        protected final String _fieldSeitoShutokutanisu;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _sDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _outputRank = request.getParameter("OUTPUT_RANK");
//            _creditDrop = (request.getParameter("OUTPUT4") != null);
//            _outputCourse = (request.getParameter("OUTPUT5") != null);
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT5"));
//            _useSlumpSdivDatSlump = "1".equals(request.getParameter("OUTPUT6"));
            _kettenshaSuuNiKessaShaWoFukumenai = "1".equals(request.getParameter("OUTPUT7"));
            _outputBiko = "1".equals(request.getParameter("OUTPUT_BIKO"));
            _bikoKind = request.getParameter("BIKO_KIND");
//            _outputPattern = _isDemo ? PATTERN1 : StringUtils.isBlank(request.getParameter("OUTPUT_PATERN")) ? PATTERN1 : request.getParameter("OUTPUT_PATERN");
            _assess = (request.getParameter("ASSESS") != null) ? new Float(request.getParameter("ASSESS1")) : new Float(4.3);
//            _formSelect = request.getParameter("FORM_SELECT");
//            _subclassMax = request.getParameter("SUBCLASS_MAX");
//            _cmd = request.getParameter("cmd");
            SUBCLASSCD999999 = "999999";
            _formname = "KNJD615M.frm";
            _formMaxLine = 45;
            _formMaxColumn = 29;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _now = getNow(db2);
            _termKekka = getTermKekka(db2);

            _outputKijun = request.getParameter("OUTPUT_KIJUN");
//            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _prgId = request.getParameter("PRGID");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
//            _knjd615vPrintNullRemark = request.getParameter("knjd615vPrintNullRemark");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _getinfo = new KNJ_Get_Info();
            _schoolName = getSchoolName(db2);
            log.info(" schoolName = " + _schoolName);
            _useKetten = true;

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

//            setD054Namecd2Max(db2);
            setD055Name1(db2);
            _definecode = createDefineCode(db2);
            _gradCredits = getGradCredits(db2);
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = StringUtils.defaultString(returnval.val1);  //学期名称
            // 学期期間FROM
            if (null == returnval.val2) {
                _semesterDateS = _year + "-04-01";
            } else {
                _semesterDateS = returnval.val2;
            }
            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, SEMEALL);
            _yearDateS = returnval1.val2;
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
            final String scoreDiv = _testKindCd.substring(4);
            if (SCORE_DIV_01.equals(scoreDiv) || SCORE_DIV_02.equals(scoreDiv)) {
                _isGakunenMatu = false;
//                _hasCompCredit = false;
//                _hasJudgementItem = false;
                _enablePringFlg = false;
                _isPrintDetailTani = false;
                _failValue = 30;
                _printPrgid = true;

                if (SCORE_DIV_02.equals(scoreDiv)) {
                    _item1Name = "平常点";
                } else {
                    _item1Name = "素点";
                }
                _item2Name = "欠課";
                _item4Name = "総合点";
                _item5Name = "平均点";
                _form2Item4Name = _item4Name;
                _form2Item5Name = _item5Name;
//                _creditDrop = false;
                _title = _semesterName + " " + _testItem._testitemname + " 成績一覧表";

            } else if (SCORE_DIV_08.equals(scoreDiv)) {
//                _hasJudgementItem = false;
                _failValue = 30;
                _printPrgid = true;

                _enablePringFlg = false;
                _isPrintDetailTani = true;
                _item1Name = "評価";
                _item2Name = "欠課";
                _item4Name = "評価総合点";
                _item5Name = "評価平均点";
                _form2Item4Name = "評価合計";
                _form2Item5Name = "評価平均";
                _isGakunenMatu = false;
//                _hasCompCredit = false;
                _title = _semesterName + " " + _testItem._testitemname + " 成績一覧表";

            } else if (SCORE_DIV_09.equals(scoreDiv)) {
//                _hasJudgementItem = false;
                _failValue = 30;
                _printPrgid = true;

                _item1Name = "評定";
                _item2Name = "欠課";
                _item4Name = "評定総合点";
                _item5Name = "評定平均点";
                _form2Item4Name = "評定合計";
                _form2Item5Name = "評定平均";
                _isGakunenMatu = true;
//                _hasCompCredit = true;
                _enablePringFlg = false;
                _isPrintDetailTani = true;
                _title = _testItem._testitemname + "  成績一覧表（評定）";
            }

            if (OUTPUT_RANK1.equals(_outputRank)) {
                _rankName = "学級";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "CLASS_AVG_RANK";
                } else {
                    _rankFieldName = "CLASS_RANK";
                }
                _avgDiv = "2";
            } else if (OUTPUT_RANK2.equals(_outputRank)) {
                _rankName = "学年";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "GRADE_AVG_RANK";
                } else {
                    _rankFieldName = "GRADE_RANK";
                }
                _avgDiv = "1";
            } else if (OUTPUT_RANK3.equals(_outputRank)) {
                _rankName = "コース";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "COURSE_AVG_RANK";
                } else {
                    _rankFieldName = "COURSE_RANK";
                }
                _avgDiv = "3";
            } else if (OUTPUT_RANK4.equals(_outputRank)) {
                _rankName = "学科";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "MAJOR_AVG_RANK";
                } else {
                    _rankFieldName = "MAJOR_RANK";
                }
                _avgDiv = "4";
            }
            log.debug("順位名称=" + _rankName);
            setSubclassMst(db2);
            setPrintSakiKamoku(db2);
            _fieldSeitoSogoten = "TOTAL1";
            _fieldSeitoHeikinten = "AVERAGE1";
            _fieldSeitoClassRank = "CLASS_RANK1";
            _fieldSeitoSiteiRank = "RANK1";
            _fieldSeitoKettenKamoku = "FAIL1";
            _fieldSeitoShutokutanisu = "GET_CREDIT";
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        /** 欠課の集計範囲 */
        public String getTermKekka(final DB2UDB db2) {
            return KNJ_EditDate.h_format_JP(db2, _sDate) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(db2, _date);
        }

        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                absentFmt = new DecimalFormat("0");
                break;
            default:
                absentFmt = new DecimalFormat("0.0");
            }
            return absentFmt;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        private TestItem getTestItem(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) {
            TestItem testitem = new TestItem();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME, SIDOU_INPUT, SIDOU_INPUT_INF "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV "
                                 +  "WHERE YEAR = '" + year + "' "
                                 +    "AND SEMESTER = '" + semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._sidouinput = rs.getString("SIDOU_INPUT");
                    testitem._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitem;
        }

        private String getAttendRemarkMonth(final DB2UDB db2, final String date) {
            String rtn = "99";
            if (null == date) {
                return rtn;
            }
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            int month = cal.get(Calendar.MONTH) + 1;
            if (month < 4) {
                month += 12;
            }
            return String.valueOf(month);
        }

//        private void setD054Namecd2Max(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
//                stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _d054Namecd2Max = rs.getString("NAMECD2");
//                    _sidouHyoji = rs.getString("NAME1");
//                }
//            } catch (Exception ex) {
//                log.debug("getZ010 exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

        private void setD055Name1(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d055Name1Map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1 FROM V_NAME_MST ");
                stb.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D055' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _d055Name1Map.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception ex) {
                log.debug("getD055 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (Exception ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private String getSchoolName(final DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolName = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        // 卒業認定単位数の取得
        private int getGradCredits(
                final DB2UDB db2
        ) {
            int gradcredits = 0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    gradcredits = rs.getInt("GRAD_CREDITS");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradcredits;
        }


        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;

            // 名称マスタ「D021」「01」から取得する
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    String str = rs.getString("NAMESPARE3");
                    if ("Y".equals(str)) _isPrintSakiKamoku = false;
                }
            } catch (Exception e) {
                log.error("合併先科目を印刷するかフラグの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }

            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null, false, false);
            }
            return (SubclassMst) _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMst = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON ";
                if ("1".equals(_useCurriculumcd)) {
                    sql += " T2.CLASSCD = T1.CLASSCD ";
                    sql += " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                } else {
                    sql += " T2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), isSaki, isMoto);
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

}
