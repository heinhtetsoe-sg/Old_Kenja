// kanji=漢字
/*
 * $Id: 56bf494ab306e2a01e6e76d0d07aca6b9a68cb32 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @version $Id: 56bf494ab306e2a01e6e76d0d07aca6b9a68cb32 $
 */
public class KNJD615R {
    private static final Log log = LogFactory.getLog(KNJD615R.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    static final String PATTERN4 = "4"; // 成績の記録
    static final String PATTERN5 = "5"; // 欠課時数と出欠の記録
    static final String PATTERN6 = "6"; // 両方
    
    private static final String OUTPUT_KJUN2 = "2";
    private static final String OUTPUT_KJUN3 = "3";
    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";
    private static final String OUTPUT_RANK4 = "4";
    private static final String OUTPUT_RANK5 = "5";
    
    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";
    
    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";
    
    private static final String ATTRIBUTE_KETTEN = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";

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

            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            printMain(db2, param, svf);

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

                hrInfo.load(db2, param, null);
                // 印刷処理
                final Form form = new Form();
                if (form.print(db2, svf, param, hrInfo)) {
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
        final String _gradeHrclassCd;
        private List _courses;
        private String _staffName;
        private String _hrName;
        private List _students = Collections.EMPTY_LIST;
        private Set _courseSet = new HashSet();
        private Set _majorSet = new HashSet();
        private final Map _subclasses = new TreeMap();
        private List _ranking;
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private BigDecimal _perHrPresent;  // 学級の出席率
        private BigDecimal _perHrAbsent;  // 学級の欠席率
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrLesson;  // 学級の授業日数
//        private String _HrMLesson;  // 学級の授業日数
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
        private int _countkaikinAll;  // 累計皆勤人数
        private int _countkaikin;  // 皆勤人数

        public HRInfo(final String hrclassCd) {
            _gradeHrclassCd = hrclassCd;
        }

        public void load(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            loadHRClassStaff(db2, param);
            loadStudents(db2, param, courseCd);
            loadStudentsInfo(db2, param);
            loadAttend(db2, param);
            loadHrclassAverage(db2, param, courseCd);
            loadRank(db2, param);
            loadScoreDetail(db2, param);
            _ranking = createRanking(param);
            log.debug("RANK:" + _ranking);
            setSubclassAverage(param);        
            setHrTotal(param);  // 学級平均等の算出
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
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        W1.HR_NAME ");
            stb.append("        , ST1.STAFFNAME AS STAFFNAME1 ");
            stb.append("        , ST2.STAFFNAME AS STAFFNAME2 ");
            stb.append("        , ST3.STAFFNAME AS STAFFNAME3 ");
            stb.append("        , STS1.STAFFNAME AS STAFFNAME4 ");
            stb.append("        , STS2.STAFFNAME AS STAFFNAME5 ");
            stb.append("        , STS3.STAFFNAME AS STAFFNAME6 ");
            stb.append("FROM    SCHREG_REGD_HDAT W1 ");
            stb.append("LEFT JOIN STAFF_MST ST1 ON ST1.STAFFCD = W1.TR_CD1 ");
            stb.append("LEFT JOIN STAFF_MST ST2 ON ST2.STAFFCD = W1.TR_CD2 ");
            stb.append("LEFT JOIN STAFF_MST ST3 ON ST3.STAFFCD = W1.TR_CD3 ");
            stb.append("LEFT JOIN STAFF_MST STS1 ON STS1.STAFFCD = W1.SUBTR_CD1 ");
            stb.append("LEFT JOIN STAFF_MST STS2 ON STS2.STAFFCD = W1.SUBTR_CD2 ");
            stb.append("LEFT JOIN STAFF_MST STS3 ON STS3.STAFFCD = W1.SUBTR_CD3 ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("    AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append("    AND W1.GRADE||W1.HR_CLASS = '" + _gradeHrclassCd + "' ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            String hrName = null;
            StringBuffer staffName = new StringBuffer();
            try {
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                if (rs.next()) {
                    hrName = rs.getString("HR_NAME");
                    for (int i = 1; i <= 6; i++) {
                        final String name = rs.getString("STAFFNAME" + String.valueOf(i));
                        if (StringUtils.isBlank(name)) {
                            continue;
                        }
                        if (staffName.length() != 0) {
                            staffName.append("　");
                        }
                        staffName.append(name);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            _staffName = staffName.toString();
            _hrName = hrName;
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
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  W1.SCHREGNO, ");
                stb.append("        W1.ATTENDNO, ");
                stb.append("        W1.COURSECD || W1.MAJORCD AS MAJOR, ");
                stb.append("        W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE, ");
                stb.append("        W1.COURSECODE, ");
                stb.append("        G1.GROUP_CD ");
                stb.append("FROM    SCHREG_REGD_DAT W1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
                stb.append(" LEFT JOIN COURSE_GROUP_CD_DAT G1 ON G1.YEAR = W1.YEAR AND G1.GRADE = W1.GRADE AND G1.COURSECD = W1.COURSECD AND G1.MAJORCD = W1.MAJORCD AND G1.COURSECODE = W1.COURSECODE ");
                stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
                if (!SEMEALL.equals(param._semester)) {
                    stb.append("    AND W1.SEMESTER = '" + param._semester + "' ");
                } else {
                    stb.append("    AND W1.SEMESTER = '" + param._semeFlg + "' ");
                }
                stb.append("    AND W1.GRADE||W1.HR_CLASS = '" + _gradeHrclassCd + "' ");
//                if (param._outputCourse) {
//                    stb.append("    AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
//                }
                stb.append("ORDER BY W1.ATTENDNO");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
//                    if (_param._noKetuban) {
//                        gnum++;
//                    } else {
                        gnum = rs.getInt("ATTENDNO");
//                    }
                    final Student student = new Student(rs.getString("SCHREGNO"), this, gnum);
                    student._coursecode = rs.getString("COURSECODE");
                    student._groupCd = rs.getString("GROUP_CD");
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
        
        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
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

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
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
                    	
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            
            if (param._isPrintKaikinAll) {
                loadStudentList(db2, param);
            }
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

        private void loadAttend(
                final DB2UDB db2,
                final Param param
        ) {
            setAttend(false, db2, param, param._year, param._semester, param._sdate, param._date);
            
            if (param._isPrintKaikin) {
                
                setAttend(true, db2, param, param._year, "9", null, String.valueOf(Integer.parseInt(param._year) + 1) + "-03-31");
            }
            if (param._isPrintKaikinAll) {
                final Map regdMap = Regd.getRegdMap(_students);
                
                for (final Iterator it = regdMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String key = (String) e.getKey();
                    final String[] split = StringUtils.split(key, "-");
                    setAttend(true, db2, param, split[0], "9", null, String.valueOf(Integer.parseInt(split[0]) + 1) + "-03-31");
//                    setAttendSubclass(db2, param, split[0], "9", String.valueOf(Integer.parseInt(split[0]) + 1) + "-03-31");
                }
            }
        }

        public void setAttend(final boolean isForKaikin, final DB2UDB db2, final Param param, final String year, final String semester, final String sdate, final String date) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.info(" set attend year = " + year + ", semester = " + semester + ", date = " + date);
                final String psKey = "ATTENDSEMES" + year + (isForKaikin ? "_KAIKIN" : "");
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");
                    final String sql = AttendAccumulate.getAttendSemesSql(
                            year,
                            semester,
                            sdate,
                            date,
                            param._attendParamMap
                    );
                    //log.info(" attend semes sql " + year + " = " + sql);
                    
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);
                
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    ps.setString(1, student._schregno);
                    
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        
                        final AttendInfo attendInfo = new AttendInfo(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("SICK"),
                                rs.getInt("SICK_ONLY"),
                                rs.getInt("NOTICE_ONLY"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY"),
                                rs.getInt("VIRUS"),
                                rs.getInt("TRANSFER_DATE"),
                                rs.getString("M_KEKKA_JISU")
                        );
                        if (isForKaikin) {
                            student._attendKaikinInfoMap.put(year, attendInfo);
                        } else {
                            student._attendInfo = attendInfo;
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
        
//        public void setAttendSubclass(final DB2UDB db2, final Param param, final String year, final String semester, final String date) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                log.info(" set attend subclass year = " + year + ", semester = " + semester + ", date = " + date);
//                final String psKey = "ATTENDSUBCLASS" + year;
//                if (null == param._psMap.get(psKey)) {
//                    param._attendParamMap.put("schregno", "?");
//                    final String sql = AttendAccumulate.getAttendSubclassSql(
//                            year,
//                            semester,
//                            null, // 過去の年度の開始日付
//                            date,
//                            param._attendParamMap
//                    );
//                    //log.info(" attend semes sql " + year + " = " + sql);
//                    
//                    param._psMap.put(psKey, db2.prepareStatement(sql));
//                }
//                ps = (PreparedStatement) param._psMap.get(psKey);
//                
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//                    
//                    ps.setString(1, student._schregno);
//                    
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
//                            continue;
//                        }
//                        
//                        String[] split = StringUtils.split(rs.getString("SUBCLASSCD"), "-");
//                        
//                        // 先科目、90を超える科目を含めない
//                        if ("1".equals(rs.getString("IS_COMBINED_SUBCLASS")) || split[0].compareTo("90") > 0) {
//                            continue;
//                        }
//                        
//                        if (null == student._kekkaMap.get(year)) {
//                            student._kekkaMap.put(year, new BigDecimal(0));
//                        }
//                        final BigDecimal kekka = (BigDecimal) student._kekkaMap.get(year);
//                        student._kekkaMap.put(year, kekka.add(rs.getBigDecimal("SICK2")));
//                    }
//                    DbUtils.closeQuietly(rs);
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(rs);
//                db2.commit();
//            }
//        }
        
        private void loadStudentList(final DB2UDB db2, final Param param) {
            ResultSet rs = null;
            
            try {
                // HRの生徒を取得
                final String psKey = "OLD_REGD";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlSchregRegdDat(param);
                    log.info("schreg_regd_dat sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);
                ps.setString(1, _gradeHrclassCd.substring(2));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final Student student = getStudent(schregno);
                    if (null == student) {
                        continue;
                    }
                    if (null == student._regdList) {
                        student._regdList = new ArrayList();
                    }
                    student._regdList.add(new Regd(student, rs.getString("YEAR"), rs.getString("SEMESTER"), rs.getString("GRADE"), rs.getString("GRADE_CD"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("HR_NAMEABBV"), rs.getString("ATTENDNO")));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
        
        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH T_REGD0 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + param._year + "' ");
            stb.append(" ), T_REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     T_REGD0 T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR FROM T_REGD0 GROUP BY SCHREGNO, GRADE) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "') T3 ON T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T5.YEAR, ");
            stb.append("     T5.SEMESTER, ");
            stb.append("     T5.GRADE, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     T5.HR_CLASS, ");
            stb.append("     T5.ATTENDNO, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T3.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN T_REGD T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T5.YEAR AND REGDG.GRADE = T5.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T5.YEAR = T3.YEAR ");
            stb.append("         AND T5.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T5.GRADE = T3.GRADE ");
            stb.append("         AND T5.HR_CLASS = T3.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T5.YEAR DESC ");
            return stb.toString();
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
                final String sql = sqlHrclassAverage(param, _gradeHrclassCd, course, major, courseCd);
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
        private String sqlHrclassAverage(final Param param, final String gradeHrClassCd, final String course, final String major, final String courseCd) {
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
            stb.append("            ,CASE WHEN W1.GRADE||W1.HR_CLASS = '" + gradeHrClassCd + "' THEN '1' ELSE '0' END AS IS_HR ");
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
            stb.append("         AND W1.GRADE = '" + gradeHrClassCd.substring(0, 2) + "' ");
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
            ResultSet rs = null;
            try {
                final String sql = sqlStdTotalRank(param);
                //log.debug(" total rank sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    if (rs.next()) {
                        student._classRank = rs.getInt("CLASS_RANK");
                        student._rank = rs.getInt("TOTAL_RANK");
                        student._scoreSum = rs.getString("TOTAL_SCORE");
                        student._scoreAvg = rs.getString("TOTAL_AVG");
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
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
                ps.setString(1, _gradeHrclassCd.substring(0, 2));
                ps.setString(2, _gradeHrclassCd.substring(2));

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
                                rs.getString("SLUMP"),
                                rs.getString("SLUMP_MARK"),
                                rs.getString("SLUMP_SCORE"),
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
                final String psKey = "ATTENDSUBCLASS" + param._year;
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");
                    
                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            param._sdate,
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
                                    scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null, null, null, null);
                                    student._scoreDetails.put(subclasscd, scoreDetail);
                                    break;
                                }
                            }
                            if (null == scoreDetail) {
                                // log.fatal(" no detail " + student._schregno + ", " + rs.getString("SUBCLASSCD"));
                                continue;
                            }
                        }
                        
//                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
//                        final Integer specialAbsentMinutes = (Integer) rs.getObject("SPECIAL_SICK_MINUTES3");
//                        if (specialGroupCd != null && specialAbsentMinutes != null) {
//                            if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
//                                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
//                            }
//                            int minute = ((Integer) student._spGroupAbsentMinutes.get(specialGroupCd)).intValue();
//                            student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
//                        }


                        final int jisu;
                        if ("1".equals(param._knjd615rJugyoJisuLesson)) {
                        	jisu = rs.getInt("LESSON");
                        } else {
                        	jisu = rs.getInt("MLESSON");
                        }
						if (0 != jisu && scoreDetail._subClass._jisu < jisu) {
                            scoreDetail._subClass._jisu = jisu;
                        }
                        scoreDetail._jisu = (Integer) rs.getObject("MLESSON");
                        scoreDetail._absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                        if ("1".equals(rs.getString("IS_COMBINED_SUBCLASS"))) {
                            scoreDetail._absent = Double.valueOf(rs.getString("REPLACED_SICK"));
                        } else {
                            scoreDetail._absent = Double.valueOf(rs.getString("SICK2"));
                        }
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);
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
            stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append("            W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
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
            stb.append("            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            stb.append("            SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append("    FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.COURSECD = T2.COURSECD ");
            stb.append("        AND T1.MAJORCD = T2.MAJORCD ");
            stb.append("        AND T1.COURSECODE = T2.COURSECODE ");
            stb.append("        AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            stb.append("            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
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
            stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("                              T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, ");
            stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("                 COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            stb.append("            T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("             COMBINED_SUBCLASSCD");
            stb.append(") ");       
            
            stb.append("   , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE ");
            stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
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
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("     ) ");
            
            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
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
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
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
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
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
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
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
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");
            
            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  W2.SCHREGNO, ");
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append("             W2.SUBCLASSCD, ");
            stb.append("             MIN(W22.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W2");
            stb.append("     LEFT JOIN CHAIR_STF W22 ON W22.SEMESTER = W2.SEMESTER ");
            stb.append("         AND W22.CHAIRCD = W2.CHAIRCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,");
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append("              W2.SUBCLASSCD");
            stb.append(" ) ");
            
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  value(T7.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,VALUE(T8.SHOWORDER4, 99) AS SHOWORDER4 ");
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
            stb.append("            T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            stb.append("                              T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            stb.append(" T8.CLASSCD || '-' || T8.SCHOOL_KIND = ");
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append("     LEFT JOIN STAFF_MST W23 ON W23.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND ");
            stb.append("            SDET.CLASSCD || '-' || SDET.SCHOOL_KIND || '-' || SDET.CURRICULUM_CD || '-' || ");
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
                Integer showOrder4 = new Integer(99);
                String classabbv = null;
                String subclassabbv = null;
                String subclassname = null;
                String staffname = null;
                boolean electdiv = false;
                try {
                    if (NumberUtils.isDigits(rs.getString("SHOWORDER4"))) {
                        showOrder4 = Integer.valueOf(rs.getString("SHOWORDER4"));
                    }
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
                subclass = new SubClass(showOrder4, subclasscode, classabbv, subclassabbv, subclassname, electdiv, credit, staffname);
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
//                    //欠点（赤点）
//                    if (ScoreDetail.isFailCount(param, detail)) {
//                        arr[4]++;
//                    }
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
//                        if (0 != val[4]) {
//                            subclass._scoreFailCnt = String.valueOf(val[4]);
//                        }
                    }
                }
            }
        }

        /**
         * 学級平均の算出
         */
        private void setHrTotal(final Param param) {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int mlesson = 0;
            int present = 0;
            int absent = 0;
            int[] arrc = {0,0};  // 履修単位
            int[] arrMLesson = {0,0};  // 出席すべき授業日数
            int[] arrLesson = {0,0};  // 授業日数
            int countKaikinAll = 0;
            int countKaikin = 0;
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
                if (null != student._attendInfo) {
                    mlesson += student._attendInfo._mLesson;
                    present += student._attendInfo._present;
                    absent += student._attendInfo._sick;
                    arrMLesson = setMaxMin(arrMLesson[0], arrMLesson[1], student._attendInfo._mLesson);
                    arrLesson = setMaxMin(arrLesson[0], arrLesson[1], student._attendInfo._lesson);
                }
                arrc = setMaxMin(arrc[0], arrc[1], student._compCredit);
                
                if (param._isPrintKaikin) {
//                    student._kekkaMap.put(param._year, student.getKekkaTotal(param));

                    if (param._isPrintKaikinAll) {
                        //すべて
                        student._isKaikinAll = false;
                        final Set yearSet = new HashSet();
                        yearSet.add(param._year);
                        if (null != student._regdList) {
                            for (final Iterator rit = student._regdList.iterator(); rit.hasNext();) {
                                final Regd regd = (Regd) rit.next();
                                yearSet.add(regd._year);
                                student._isKaikinAll = true; // 初期化
                            }
                            if (student._isKaikinAll) {
                                AttendInfo total = null;
                                for (final Iterator it = yearSet.iterator(); it.hasNext();) {
                                    final String year = (String) it.next();
                                    final AttendInfo att = (AttendInfo) student._attendKaikinInfoMap.get(year);
                                    if (null == att) {
                                        continue;
                                    }
                                    total = att.add(total);
                                }
                                if (isKaikin(student, param, total, "all")) {
                                } else {
                                    student._isKaikinAll = false;
                                }
                            }
                        }
                    }
                    if (!student._isKaikinAll) {
                        if (isKaikin(student, param, (AttendInfo) student._attendKaikinInfoMap.get(param._year), param._year)) {
                            student._iskaikin = true;
                        }
                    }
        			if (param._isOutputDebug) {
        				log.info(" attend " + student._schregno + " = " + student._attendKaikinInfoMap + " / " + " -> isKaikin = " + student._iskaikin + ", isKaikinAll = " + student._isKaikinAll);
        			}
                    if (student._isKaikinAll) {
                        countKaikinAll += 1;
                    }
                    if (!student._isKaikinAll && student._iskaikin) {
                        countKaikin += 1;
                    }
                }
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }                
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
            if (0 < mlesson) {
                _perHrPresent = new BigDecimal((float) present / (float) mlesson * 100);
                _perHrAbsent = new BigDecimal((float) absent / (float) mlesson * 100);
            }
            if (0 < arrc[0]) {
                _HrCompCredits = arrc[0] + "単位";
            }
//            if (0 < arrMLesson[0]) {
//                _HrMLesson = String.valueOf(arrMLesson[0]);
//            }
            if (0 < arrLesson[0]) {
            	_HrLesson = String.valueOf(arrLesson[0]);
            }
            _countkaikinAll = countKaikinAll;
            _countkaikin = countKaikin;
        }

        private static boolean isKaikin(final Student student, final Param param, final AttendInfo att, final String year) {
            if (null == att || att._lesson == 0) {
                return false;
            }
            final double kekka = NumberUtils.isNumber(att._detailSeq001) ? Double.parseDouble(att._detailSeq001) : 0.0;
            final boolean lateEarlyOk = att._late + att._early <= param._kChikokusoutai;
            final boolean lateOk = att._late <= param._kChikoku;
            final boolean earlyOk = att._early <= param._kSoutai;
            final boolean sickOk = att._sick <= param._kKesseki;
            final boolean kekkaOk = kekka <= param._kKekka;
			final boolean rtn = lateEarlyOk
                               && lateOk
                               && earlyOk
                               && sickOk
                               && kekkaOk
                               && att._virus == 0
                               ;
			final StringBuffer stb = new StringBuffer();
			if (param._isOutputDebug) {
				stb.append(" student " + year + " " + student._schregno + "(" + student._attendNo + ") kaikin = " + rtn + "\n");
				if (!lateEarlyOk) {
					stb.append(" lateEarly " + (att._late + att._early) + " <= " + param._kChikokusoutai + "\n");
				}
				if (!lateOk) {
					stb.append(" late " + att._late + " <= " + param._kChikoku + "\n");
				}
				if (!earlyOk) {
					stb.append(" early " + att._early + " <= " + param._kSoutai + "\n");
				}
				if (!sickOk) {
					stb.append(" sick " + att._sick + " <= " + param._kKesseki + "\n");
				}
				if (!kekkaOk) {
					stb.append(" kekka " + kekka + " (" + att._detailSeq001 + ") <= " + param._kKekka);
				}
				log.info(stb);
			}
            return rtn;
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
            stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
            stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = ");
            stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testKindCd + "'");
            stb.append("    AND T1.AVG_DIV = '" + param._avgDiv + "' ");
            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '" + param.SUBCLASSCD999999 + "' ");
            if ("2".equals(param._avgDiv)) {
                stb.append("    AND T1.HR_CLASS = '" + _gradeHrclassCd.substring(2) + "' ");
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
                        //log.debug("subclass => " + subclass + " , gradeAvg => " + subclassGradeAvg + " / " + electDiv + ":" + subclassCd);
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
                final Total total = student._total;
                if (0 < total._count) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }

        private int rank(final Student student) {
            final Total total = student._total;
            if (0 >= total._count) {
                return -1;
            }
            return 1 + _ranking.indexOf(total);
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
            return _gradeHrclassCd.compareTo(that._gradeHrclassCd);
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
        private List _regdList;
        private String _attendNo;
        private String _name;
        private String _coursecode;
        private String _groupCd;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null);
        private Map _attendKaikinInfoMap = new HashMap();
        private String _scoreSum;
        private String _scoreAvg;
        private int _classRank;
        private int _rank;
        private final Map _scoreDetails = new TreeMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private boolean _isKaikinAll;
        private boolean _iskaikin;
//        private Map _kekkaMap = new HashMap();
//        private int _qualifiedCredits;  // 今年度の認定単位数
//        private int _previousCredits;  // 前年度までの修得単位数
//        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
//        private boolean _isGradePoor;  // 成績不振者
//        private boolean _isAttendPerfect;  // 皆勤者
//        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者
//        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠課時分
//        private int _specialAbsent; // 特活欠課時数

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

//        public String getKettenKamokuCount(final Param param) {
//            int count = 0;
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
//                    continue;
//                }
//                if (ScoreDetail.isFailCount(param, detail)) {
//                    count += 1;
//                }
//            }
//            return count == 0 ? null : String.valueOf(count);
//        }

//        public String getKettenTanni(final Param param) {
//            int credit = 0;
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
//                    continue;
//                }
//                if (ScoreDetail.isFailCount(param, detail) && null != detail._credits) {
//                    credit += detail._credits.intValue();
//                }
//            }
//            return credit == 0 ? null : String.valueOf(credit);
//        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : _attendNo;
        }

        public BigDecimal getKekkaTotal(final Param param) {
            BigDecimal rtn = new BigDecimal(0);
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                }
                if (null != detail._absent) {
                    rtn = rtn.add(new BigDecimal(detail._absent.doubleValue()));
                }
            }
            if (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) {
                rtn = rtn.setScale(1, BigDecimal.ROUND_HALF_UP);
            } else {
                rtn = rtn.setScale(0);
            }
            return rtn;
        }
    }
    
    private static class Regd {
        final Student _student;
        final String _year;
        final String _semester;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendNo;

        public Regd(
                final Student student,
                final String year,
                final String semester,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String attendNo) {
            _student = student;
            _year = year;
            _semester = semester;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
        }
        
        private static Map getRegdMap(final List studentList) {
            final Map map = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == student._regdList) {
                    log.warn(" regdList empty : " + student._schregno);
                    continue;
                }
                for (final Iterator git = student._regdList.iterator(); git.hasNext();) {
                    final Regd regd = (Regd) git.next();
                    final String key = regd._year + "-" + regd._semester + "-" + regd._grade + "-" + regd._hrClass;
                    if (null == map.get(key)) {
                        map.put(key, new ArrayList());
                    }
                    ((List) map.get(key)).add(regd);
                }
            }
            return map;
        }
        
        public String toString() {
            return "Regd(year:semester=" + _year + ":" + _semester + ", grade = " + _grade + ")";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class AttendInfo {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _virus;
        final int _transDays;
        final String _detailSeq001;

        AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int present,
                final int late,
                final int early,
                final int virus,
                final int transDays,
                final String detailSeq001
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _virus = virus;
            _transDays = transDays;
            _detailSeq001 = detailSeq001;
        }
        
        public AttendInfo add(final AttendInfo att) {
            if (null == att) {
                return this;
            }
            final BigDecimal detailSeq001 = NumberUtils.isNumber(_detailSeq001) ? new BigDecimal(_detailSeq001) : new BigDecimal(0);
            final BigDecimal attDetailSeq001 = NumberUtils.isNumber(att._detailSeq001) ? new BigDecimal(att._detailSeq001) : new BigDecimal(0);
			final AttendInfo rtn = new AttendInfo(
                    _lesson + att._lesson,
                    _mLesson + att._mLesson,
                    _suspend + att._suspend,
                    _mourning + att._mourning,
                    _sick + att._sick,
                    _sickOnly + att._sickOnly,
                    _noticeOnly + att._noticeOnly,
                    _present + att._present,
                    _late + att._late,
                    _early + att._early,
                    _virus + att._virus,
                    _transDays + att._transDays,
                    NumberUtils.isNumber(_detailSeq001) || NumberUtils.isNumber(att._detailSeq001) ? detailSeq001.add(attDetailSeq001).toString() : null
                    );
            return rtn;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        public boolean isAttendPerfect() {
            if (_sick == 0 && _late == 0 && _early == 0) { return true; }
            return false;
        }
        
        public String toString() {
            return "Attendance(" + _lesson + ", " + _mLesson + ", " + _suspend + ", " + _mourning + ", " + _sick + ", " + _present + ", " + _late + ", " + _early + ")"; 
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass implements Comparable {
        final Integer _showOrder4;
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
                final Integer showOrder4,
                final String subclasscode, 
                final String classabbv, 
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final int credit,
                final String staffname
        ) {
            _showOrder4 = showOrder4;
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
        
        public int compareTo(final Object o) {
            final SubClass sub = (SubClass) o;
            int cmp;
            cmp = _showOrder4.compareTo(sub._showOrder4);
            if (cmp != 0) {
                return cmp;
            }
            cmp = _subclasscode.compareTo(sub._subclasscode);
            return cmp;
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
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;

        ScoreDetail(
                final SubClass subClass,
                final String score,
                final String scoreDi,
                final String assessLevel,
                final String karihyotei,
                final Integer replacemoto,
                final String print_flg,
                final String slump,
                final String slumpMark,
                final String slumpScore,
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
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpMark = slumpMark;
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

        public static Boolean hoge(final Param param, final ScoreDetail detail) {
            if (null != param._testItem._sidouinput) {
                if (SIDOU_INPUT_INF_MARK.equals(param._testItem._sidouinputinf)) { // 記号
                    if (null != param._d054Namecd2Max && null != detail._slumpMark) {
                        if (param._d054Namecd2Max.equals(detail._slumpMark)) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                } else if (SIDOU_INPUT_INF_SCORE.equals(param._testItem._sidouinputinf)) { // 得点
                    if (null != detail._slumpScore) {
                        if ("1".equals(detail._slumpScore)) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                }
            }
            return null;
        }
        
//        private static boolean isGakunenmatsu(final Param param) {
//            return param._testKindCd != null && (SEMEALL.equals(param._semester) && param._testKindCd.endsWith("09"));            
//        }
//
//        private static boolean isFail(final Param param, final ScoreDetail detail) {
//            if (!isGakunenmatsu(param)) {
//                Boolean slump = hoge(param, detail);
//                if (null != slump) {
//                    return slump.booleanValue();
//                }
//            }
//            if (isGakunenmatsu(param)) {
//                return "*".equals(detail._scoreDi) || "1".equals(detail._score);
//            }
//            return "*".equals(detail._scoreDi) || "1".equals(detail._assessLevel);
//        }
//        
//        private static boolean isFailCount(final Param param, final ScoreDetail detail) {
//            if (!isGakunenmatsu(param)) {
//                Boolean slump = hoge(param, detail);
//                if (null != slump) {
//                    return slump.booleanValue();
//                }
//            }
//            if (isGakunenmatsu(param)) {
//                return ("*".equals(detail._scoreDi)) || "1".equals(detail._score);
//            }
//            return ("*".equals(detail._scoreDi)) || "1".equals(detail._assessLevel);
//        }

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
//                    if (ScoreDetail.isFailCount(param, detail)) {
//                        countFail++;
//                    }
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

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private int getSpecialAttendExe(final Param param, final int kekka) {
            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
            final BigDecimal bigKekka = new BigDecimal(kekka);
            final BigDecimal bigJitu = new BigDecimal(jituJifun);
            final BigDecimal bigD = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN);
            final String retSt = bigD.toString();
            final int retIndex = retSt.indexOf(".");
            int seisu = 0;
            if (retIndex > 0) {
                seisu = Integer.parseInt(retSt.substring(0, retIndex));
                final int hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
                seisu = hasu < 5 ? seisu : seisu + 1;
            } else {
                seisu = Integer.parseInt(retSt);
            }
            return seisu;
        }
        
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
    }
    
    static class Form {
        
        public boolean print(final DB2UDB db2, final Vrw32alp svf, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
            if ("2".equals(param._outputPattern)) {
            } else {
                
                final Set divSet = new HashSet();
                for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    if (OUTPUT_RANK3.equals(param._outputRank)) {
                        //下1桁が1 -> 文系、2 -> 理系
                        if (NumberUtils.isDigits(student._coursecode)) {
                            final Integer v = new Integer(Integer.parseInt(student._coursecode) % 10);
                            divSet.add(v);
                        }
                    } else if (OUTPUT_RANK5.equals(param._outputRank)) {
                        //下1桁が1 -> 文系、2 -> 理系
                        if (NumberUtils.isDigits(student._groupCd)) {
                            final Integer v = new Integer(Integer.parseInt(student._groupCd) % 10);
                            divSet.add(v);
                        }
                    }
                }

                if (PATTERN4.equals(param._outputPattern)) {
                    param._formname = "J".equals(param._schoolKind) ? "KNJD615R_1J.frm" : OUTPUT_RANK2.equals(param._outputRank) || divSet.size() == 1 ?  "KNJD615R_1_2.frm" : "KNJD615R_1.frm";
                    param._formMaxLine = 55;
                    param._formMaxColumn = 26;
                } else if (PATTERN5.equals(param._outputPattern)) {
                    param._formname = "KNJD615R_2.frm";
                    param._formMaxLine = 55;
                    param._formMaxColumn = 25;
                } else { // PATTERN6.equals(_outputPattern))
//                  param._formname = OUTPUT_RANK2.equals(param._outputRank) || divSet.size() == 1 ? "KNJD615R_3_2.frm" : "KNJD615R_3.frm";
                    param._formname = "KNJD615R_3_3.frm";
//                    param._formname = OUTPUT_RANK2.equals(param._outputRank) || divSet.size() == 1 ? "KNJD615R_3_2.frm" : "KNJD615R_3.frm";
                    param._formMaxLine = 55;
                    param._formMaxColumn = 26;
                }

                final List studentListList = getStudentListList(hrInfo._students, param._formMaxLine);
                for (final Iterator it = studentListList.iterator(); it.hasNext();) {
                    final List studentList = (List) it.next();
                    if (Form1.print(db2, svf, param, hrInfo, studentList, divSet)) {
                        hasData = true;
                    }
                }
            }
            return hasData;
        }
        
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
                final boolean notOutputColumn = "90".equals(subClass._classcode) && false;
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
        
        private static String zeroToNull(final String s) {
            return !NumberUtils.isNumber(s) || Double.parseDouble(s) == 0.0 ? null : s;
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
        
        private static void printAttendInfo(final Vrw32alp svf, final Param param, final Student student, final AttendInfo attendInfo, final int line) {
            final String fieldML = "PRESENT1";
            final String fieldSU = "SUSPEND1";
            final String fieldVI = "VIRUS1";
            final String fieldMO = "KIBIKI1";
            final String fieldAB1 = "ABSENCE1";
            final String fieldAB2 = "ABSENCE2";
            final String fieldPR = "ATTEND1";
            final String fieldLT = "TOTAL_LATE1";
            final String fieldEA = "LEAVE1";
            final String fieldAR = "ABROAD1";
            final BigDecimal kekkaTotal = student.getKekkaTotal(param);
            svf.VrsOutn("TOTAL_KEKKA", line, nullToBlank(kekkaTotal));        // 欠課の合計
            if ("1".equals(param._semester) && (param._testKindCd.startsWith("01") || param._testKindCd.startsWith("02")) // 1学期中間,期末
             || "2".equals(param._semester) && (param._testKindCd.startsWith("01") || param._testKindCd.startsWith("02")) // 2学期中間,期末
                    ) {
                // 表示しない
            } else {
                svf.VrsOutn(fieldML, line, zeroToNull(attendInfo._mLesson));      // 出席すべき日数
                svf.VrsOutn(fieldAR, line, zeroToNull(attendInfo._transDays));        // 留学
            }
            if ("1".equals(param._semester) && param._testKindCd.startsWith("02") // 1学期期末
             || "2".equals(param._semester) && param._testKindCd.startsWith("02") // 2学期期末
             || param._testKindCd.startsWith("99") // 評価
             || "3".equals(param._semester) // 「学年末」
             || "9".equals(param._semester) // 学年末
                ) {
                svf.VrsOutn(fieldPR, line, String.valueOf(attendInfo._present));      // 出席日数 (0は表示する)
            }
            svf.VrsOutn(fieldSU, line, zeroToNull(attendInfo._suspend));      // 出停
            svf.VrsOutn(fieldVI, line, zeroToNull(attendInfo._virus));      // 停学
            svf.VrsOutn(fieldMO, line, zeroToNull(attendInfo._mourning));      // 忌引
            svf.VrsOutn(fieldAB1, line, zeroToNull(attendInfo._sickOnly));       // 欠席日数
            svf.VrsOutn(fieldAB2, line, zeroToNull(attendInfo._noticeOnly));       // 欠席日数
            svf.VrsOutn(fieldLT, line, zeroToNull(attendInfo._late));      // 遅刻回数
            svf.VrsOutn(fieldEA, line, zeroToNull(attendInfo._early));        // 早退回数
            if (PATTERN6.equals(param._outputPattern)) {
                svf.VrsOutn("TOTAL_KEKKA1", line, zeroToNull(attendInfo._detailSeq001));        // V_ATTEND_SEMES_DAT.M_KEKKA_JISU時数
            } else {
                svf.VrsOutn("KEKKA1", line, zeroToNull(attendInfo._detailSeq001));        // V_ATTEND_SEMES_DAT.M_KEKKA_JISU時数
            }
            
            String remark = "";
            remark += student._transInfo.toString();  // 備考
            if (PATTERN6.equals(param._outputPattern)) {
                if (param._isPrintKaikin) {
                    if (student._isKaikinAll) {
                        svf.VrsOutn("KAIKIN1", line, "◎");
                    } else if (student._iskaikin) {
                        svf.VrsOutn("KAIKIN1", line, "○");
                    }
                }
            } else if (PATTERN4.equals(param._outputPattern)){
                svf.VrsOutn("REMARK1", line, remark);  // 備考
            } else if (PATTERN5.equals(param._outputPattern)) {
                //異常値を表示する
                if (null != kekkaTotal && kekkaTotal.doubleValue() > 0 || NumberUtils.isNumber(attendInfo._detailSeq001) && Double.parseDouble(attendInfo._detailSeq001) > 0.0) {
                    if (!"".equals(remark)) { remark += " "; }
                    final BigDecimal tmpKekkaTotal = null != kekkaTotal ? kekkaTotal : new BigDecimal(0);
                    final BigDecimal bgDetailSeq001 = NumberUtils.isNumber(attendInfo._detailSeq001) ? new BigDecimal(attendInfo._detailSeq001) : new BigDecimal(0);
                    BigDecimal ijouVal = tmpKekkaTotal.subtract(new BigDecimal((attendInfo._sickOnly + attendInfo._noticeOnly) * param._kekka)).subtract(bgDetailSeq001);
                    log.info(" " + tmpKekkaTotal + " - (" + attendInfo._sick + " [" + attendInfo._sickOnly + ", " + attendInfo._noticeOnly + "] * " + param._kekka + ") - " + attendInfo._detailSeq001 + " = " + ijouVal);
                    remark += ijouVal.toString();
                }
                
                if (param._isPrintKaikin) {
                    String mark = null;
                    if (param._isPrintKaikinAll && student._isKaikinAll) {
                        mark = "◎";
                    } else if (student._iskaikin) {
                        mark = "○";
                    }
                    if (null != mark) {
                        if (!"".equals(remark)) { remark += " "; }
                        log.info(mark + ":" + student._regdList + " / " + student._attendKaikinInfoMap);
                        remark += mark;
                    }
                }
                final int keta = getMS932ByteLength(remark);
                svf.VrsOutn("REMARK1" + (keta <= 70 ? "" : "_2"), line, remark);  // 備考
            }
        }
        
        static class Form1 {
            
            private static boolean print(
            		final DB2UDB db2,
                    final Vrw32alp svf,
                    final Param param,
                    final HRInfo hrInfo,
                    final List stulist,
                    final Set divSet
            ) {
                boolean hasData = false;
                log.info(" form = " + param._formname);
                svf.VrSetForm(param._formname, 4);
                
                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());
                Collections.sort(printSubclassList);
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = (SubClass) it.next();
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd())._isSaki) {
                        it.remove();
                    }
                    if ("90".equals(subclass._classcode)) {
                        it.remove();
                    }
                }

                final List subclassListList = getSubClassListList(param, printSubclassList, param._formMaxColumn);

                for (int p = 0, pages = subclassListList.size(); p < pages; p++) {
                    final List subclassList = (List) subclassListList.get(p);
                    
                    svf.VrSetForm(param._formname, 4);
                    printHeader(db2, svf, param, hrInfo);
                    for (final Iterator it = stulist.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        final int line = gnumToLine(param, student._gnum);
                        svf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                        svf.VrsOutn("name1", line, student._name);    // 氏名
                        
                        if (p == pages - 1) {
                            printStudentTotal(svf, param, student, divSet);
                            if (!PATTERN5.equals(param._outputPattern)) {
                                printHrInfo(svf, param, hrInfo);
                            }
                        }
                        
//                      if (param._hasJudgementItem) {
//                          // 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
//                          if (student._isGradePoor) { svf.setStringn("CHECK1", student._gnum, "★"); }
//                          if (student._isGradeGood) { svf.setStringn("CHECK1", student._gnum, "☆"); } 
//                          if (student._isAttendPerfect) { svf.setStringn("CHECK2", student._gnum, "○"); }
//                          if (student._isKekkaOver) { svf.setStringn("CHECK2", student._gnum, "●"); } 
//                      }

                    }

                    for (int i = 0, size = subclassList.size(); i < size; i++) {

                        final SubClass subclass = (SubClass) subclassList.get(i);
                        log.debug("p=" + p + ", i=" + i + ", subclasscd=" + subclass._subclasscode + " " + subclass._subclassabbv);
                        printSubclass(svf, param, i + 1, subclass);
                        
                        for (final Iterator it = stulist.iterator(); it.hasNext();) {
                            final Student student = (Student) it.next();
                            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                                final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                                printDetail(svf, param, detail, gnumToLine(param, student._gnum), i + 1);
                            }
                        }

                        //学級平均・合計
                        printSubclassStat(svf, param, i + 1, subclass);

                        svf.VrEndRecord();
                        hasData = true;
                    }
                    if (PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern)) {
                        for (int i = subclassList.size(); i < param._formMaxColumn; i++) {
                            svf.VrEndRecord();
                        }
                    }
                }
                return hasData;
            }

            private static List columnListToLines(final List columnList) {
                final List lines = new ArrayList();
                int maxLine = 0;
                for (int ci = 0; ci < columnList.size(); ci++) {
                    final List column = (List) columnList.get(ci);
                    maxLine = Math.max(maxLine, column.size());
                }
                for (int li = 0; li < maxLine; li++) {
                    lines.add(line(columnList.size()));
                }
                for (int ci = 0; ci < columnList.size(); ci++) {
                    final List column = (List) columnList.get(ci);
                    for (int li = 0; li < column.size(); li++) {
                        ((List) lines.get(li)).set(ci, column.get(li));
                    }
                }
                return lines;
            }
            
            private static List setColumnList(final List columnsList, final int column) {
                for (int i = columnsList.size(); i < column; i++) {
                    columnsList.add(new ArrayList());
                }
                return columnsList;
            }
            
            private static List joinColumnListArray(final List columnsList, final List[] columnStudentFooterList) {
                for (int i = 0; i < columnStudentFooterList.length; i++) {
                    ((List) columnsList.get(i)).addAll(columnStudentFooterList[i]);
                }
                return columnsList;
            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param svf
             * @param hrInfo
             */
            private static void printHeader(
            		final DB2UDB db2,
                    final Vrw32alp svf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                svf.VrsOut("ymd1", param.getNow(db2)); // 作成日
                svf.VrsOut("DATE", param.getTermKekka(db2));  // 欠課の集計範囲
                if (!PATTERN4.equals(param._outputPattern)
                        && !PATTERN5.equals(param._outputPattern)
                        ) {
                    svf.VrsOut("DATE2", param.getTermKekka(db2));
                }
                if ("1".equals(param._semester) && (param._testKindCd.startsWith("01") || param._testKindCd.startsWith("02")) // 1学期中間,期末
                 || "2".equals(param._semester) && (param._testKindCd.startsWith("01") || param._testKindCd.startsWith("02")) // 2学期中間,期末
                   ) {
                    // 表示しない
                } else {
                    svf.VrsOut("ATTEND_DAY", hrInfo._HrLesson);
                }
                if (param._isPrintKaikin) {
                    svf.VrsOut("PERFECT_ATTEND_NAME1", "１年皆勤(〇)");
                    svf.VrsOut("PERFECT_ATTEND1", String.valueOf(hrInfo._countkaikin));
                    svf.VrsOut("PERFECT_ATTEND1_NIN", "人");
                    if (param._isPrintKaikinAll) {
                        svf.VrsOut("PERFECT_ATTEND_NAME2", "３年皆勤(◎)");
                        svf.VrsOut("PERFECT_ATTEND2", String.valueOf(hrInfo._countkaikinAll));
                        svf.VrsOut("PERFECT_ATTEND2_NIN", "人");
                    }
                    svf.VrsOut("KAIKIN_TITLE", "皆勤");
                }
                
                svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrInfo._staffName) > 30 ? "2" : ""), hrInfo._staffName);  //担任名
                svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
//                if (param._hasCompCredit) {
//                    ipdf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
//                }
                if (!PATTERN5.equals(param._outputPattern)) {
                    if (param._rankName.length() > 5) {
                        svf.VrsOut("ITEM7_2", param._rankName + "順位");
                        svf.VrsOut("ITEM7", "\n");
                    } else {
                        svf.VrsOut("ITEM7_2", "\n");
                        svf.VrsOut("ITEM7", param._rankName + "順位");
                    }
                }
                if (PATTERN4.equals(param._outputPattern)) {
//                    if (isAlpPdf(ipdf)) {
//                        ipdf.VrsOut("ITEM", param._rankName + "平均");
//                    } else {
                        svf.VrsOut("ITEM", param._rankName);
//                    }
                    svf.VrsOut("ITEM4", param._item4Name);
                    svf.VrsOut("ITEM5", param._item5Name);
                } else if (!PATTERN5.equals(param._outputPattern)) {
                    svf.VrsOut("ITEM4", param._item4Name);
                    svf.VrsOut("ITEM5", param._item5Name);
                    if (PATTERN6.equals(param._outputPattern)) {
                        svf.VrsOut("ITEM6", param._item1Name + "・" + param._item2Name);
                    }
                    svf.VrsOut("ITEM8", param._rankName);
                }
                if (PATTERN6.equals(param._outputPattern)) {
//                    if (param._useKetten) {
//                        ipdf.VrsOut("ITEM9", "欠点科目数");
//                        ipdf.VrsOut("ITEM10", "欠点者数");
//                    }
                    svf.VrsOut("ITEM11", "修得単位数");
                    if ("H".equals(param._schoolKind)) {
                    	svf.VrsOut("CREDIT_TITLE", "単 位 数");
                    	svf.VrsOut("TANNI_HORYUSHA_TITLE", "＊：単位保留者");
                    }
                }
                svf.VrsOutn("name1", param._formMaxLine + 1, "学級平均");
                svf.VrsOutn("name1", param._formMaxLine + 2, param._rankName + "平均");

                // 一覧表枠外の文言
                if (!PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)
                        ) {
                    if ("H".equals(param._schoolKind)) {
                    	svf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKAOVER);
                    	svf.VrsOut("NOTE1",  " " );
                    	svf.VrsOut("NOTE2",  "：欠課時数超過者" );
                    }
//                    if (param._useKetten) {
//                        ipdf.VrAttribute("NOTE3",  ATTRIBUTE_KETTEN);
//                        ipdf.VrsOut("NOTE3",  " " );
//                        ipdf.VrsOut("NOTE4",  "：欠点" );
//                    }
                }
                
                svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度　" + param._title);
                if (PATTERN4.equals(param._outputPattern)) {
                    svf.VrsOut("SUBTITLE", "（成績の記録）");
                } else if (PATTERN5.equals(param._outputPattern)) {
                    svf.VrsOut("SUBTITLE", "（欠課時数と出欠の記録）");
                }
                for (int i = 0; i < param._d055Name1List.size(); i++) {
                    final String name1 = (String) param._d055Name1List.get(i);
                    final String pos = String.valueOf(8 - i);
                    final String field = "JOB" + pos + (getMS932ByteLength(name1) > 8  ? "_2" : "_1"); // 昇順に右詰め
                    svf.VrsOut(field, name1);
                    svf.VrsOut("STAMP" + pos, param._inkanWakuPath);
                }
                svf.VrsOut("SUSPEND_NAME", StringUtils.defaultString((String) param._c001Name1Map.get("2")));
                svf.VrsOut("VIRUS_NAME", StringUtils.defaultString((String) param._c001Name1Map.get("19")));
                svf.VrsOut("MOURNING_NAME", StringUtils.defaultString((String) param._c001Name1Map.get("3"), "　"));
                svf.VrsOut("SUSPEND_TEXT", "(タ)");
                svf.VrsOut("VIRUS_TEXT", "(" + StringUtils.defaultString((String) param._c001Abbv1Map.get("19"), "　") + ")");
                svf.VrsOut("KIBIKI_TXT", "(" + StringUtils.defaultString((String) param._c001Abbv1Map.get("3"), "　") + ")");
            }
            
            /**
             * 学級データの印字処理(学級平均)
             * @param ipdf
             */
            private static void printHrInfo(
                    final Vrw32alp ipdf,
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

                svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 1, "", col, hrInfo._avgHrTotal); // 学級平均
                svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 2, "", col, gtotal); // 指定母集団平均
                svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 1, "", col, hrInfo._avgHrAverage); // 学級平均
                svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 2, "", col, gavg); // 指定母集団平均

//                if (null != hrInfo._avgHrTotal) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 2, "", col, hrInfo._avgHrTotal); // 学級合計
//                    } else if (PATTERN4.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 1, "", col, hrInfo._avgHrTotal); // 学級合計
//                    }
//                }
//                if (null != gtotal) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 3, "", col, gtotal); // 学年合計
//                    }
//                }
//                //学級合計の母集団の生徒数
//                if (0 < hrInfo._avgHrCount) {
//                    if (PATTERN4.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 2, "", col, String.valueOf(hrInfo._avgHrCount)); // 学級合計の母集団の生徒数
//                    }
//                }
//                // 学級平均
//                if (null != hrInfo._avgHrAverage) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 2, "", col, hrInfo._avgHrAverage); // 学級平均
//                    } else if (PATTERN4.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 3, "", col, hrInfo._avgHrAverage); // 学級平均
//                    }
//                }
//                if (null != gavg) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 3, "", col, gavg); // 指定母集団平均
//                    } else if (PATTERN4.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 4, "", col, gavg); // 指定母集団平均
//                    }
//                }
//                //欠点者数
//                if (null != hrInfo._failHrTotal) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 4, "", col, hrInfo._failHrTotal); // 欠点者数
//                    } else if (PATTERN4.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 5, "", col, hrInfo._failHrTotal); // 欠点者数
//                    }
//                }
//                //最高点
//                if (null != hrInfo._maxHrTotal) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 5, "", col, hrInfo._maxHrTotal); // 最高点
//                    } else if (PATTERN4.equals(param._outputPattern)) {
//                        // なし
//                    }
//                }
//                //最低点
//                if (null != hrInfo._minHrTotal) {
//                    if (PATTERN6.equals(param._outputPattern)) {
//                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 6, "", col, hrInfo._minHrTotal); // 最低点
//                    } else if (PATTERN4.equals(param._outputPattern)) {
//                        // なし
//                    }
//                }
                //出席率・欠席
                if (null != hrInfo._perHrPresent) {
                    if (!PATTERN4.equals(param._outputPattern)) {
                        ipdf.VrsOut("PER_ATTEND", DEC_FMT1.format(hrInfo._perHrPresent.setScale(1,BigDecimal.ROUND_HALF_UP)));
                    }
                }
                if (null != hrInfo._perHrAbsent) {
                    if (!PATTERN4.equals(param._outputPattern)) {
                        ipdf.VrsOut("PER_ABSENCE", DEC_FMT1.format(hrInfo._perHrAbsent.setScale(1,BigDecimal.ROUND_HALF_UP)));
                    }
                }
            }
            
            /**
             * 生徒別総合点・平均点・順位を印刷します。
             * @param svf
             */
            private static void printStudentTotal(
                    final Vrw32alp svf,
                    final Param param,
                    final Student student,
                    final Set divSet
            ) {
                final int line = gnumToLine(param, student._gnum);
                if (!PATTERN5.equals(param._outputPattern)) {
                    final String _fieldSeitoSogoten;
                    final String _fieldSeitoHeikinten;
                    final String _fieldSeitoClassRank;
//                    final String _fieldSeitoKettenKamoku;
                    _fieldSeitoSogoten = "TOTAL1";
                    _fieldSeitoHeikinten = "AVERAGE1";
                    _fieldSeitoClassRank = "CLASS_RANK1";
//                    _fieldSeitoKettenKamoku = "FAIL1";

                    if (null != student._scoreSum) {
                        svf.VrsOutn(_fieldSeitoSogoten, line, student._scoreSum);  //総合点
                        svf.VrsOutn(_fieldSeitoHeikinten, line, student._scoreAvg);  //平均点
                    }
                    //順位（学級）
                    if (1 <= student._classRank) {
                        svf.VrsOutn(_fieldSeitoClassRank, line, String.valueOf(student._classRank));
                    }
                    //順位（学年orコース）
                    if (1 <= student._rank) {
                        int keta = 0;
                        if (OUTPUT_RANK2.equals(param._outputRank) || divSet.size() == 1) {
                            keta = 1;
                        } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                            //下1桁が1 -> 文系、2 -> 理系
                            keta = NumberUtils.isDigits(student._coursecode) ? Integer.parseInt(student._coursecode) % 10 : -1;
                        } else if (OUTPUT_RANK5.equals(param._outputRank)) {
                            //下1桁が1 -> 文系、2 -> 理系
                            keta = NumberUtils.isDigits(student._groupCd) ? Integer.parseInt(student._groupCd) % 10 : -1; 
                        }
                        svf.VrsOutn("RANK" + String.valueOf(keta), line, String.valueOf(student._rank));
                    }
//                    //欠点科目数
//                    if (param._useKetten) {
//                        if (0 < student._total._countFail) {
//                            ipdf.VrsOutn(_fieldSeitoKettenKamoku, line, String.valueOf(student._total._countFail));
//                        }
//                    }
                }
                printAttendInfo(svf, param, student, student._attendInfo, line);
//                if (param._hasCompCredit) {
//                    ipdf.VrsOutn("COMP_CREDIT", line, zeroToNull(student._compCredit)); //今年度履修単位数
//                    ipdf.VrsOutn("GET_CREDIT", line, zeroToNull(student._getCredit)); //今年度修得単位数
//                }
//                if (param._hasJudgementItem) { // 前年度までの単位数を印字
//                    // 各単位数、未履修科目数を印字
//                    ipdf.VrsOutn("A_CREDIT", line, zeroToNull(student._qualifiedCredits)); // 今年度認定単位数
//                    ipdf.VrsOutn("PRE_C_CREDIT", line, zeroToNull(student._previousCredits)); // 前年度までの修得単位数
//                    final int t = student._getCredit + student._qualifiedCredits + student._previousCredits; // 修得単位数計
//                    if (t != 0) {
//                        final String at = (param._gradCredits != 0 && param._gradCredits <= t) ? "@" : "";
//                        ipdf.VrsOutn("TOTAL_C_CREDIT", line, at + String.valueOf(t));
//                    }
//                    ipdf.VrsOutn("PRE_N_CREDIT", line, zeroToNull(student._previousMirisyu)); // 前年度までの未履修科目数
//                }
//                if (0 < student._specialAbsent) {
//                    ipdf.VrsOutn("SP_KEKKA", line, zeroToNull(student._specialAbsent));
//                }
            }
            
            /**
             * 生徒の氏名・備考を印字
             * @param ipdf
             * @param hrInfo
             * @param stulist：List hrInfo._studentsのsublist
             */
            private static void printStudentName(
                    final Vrw32alp ipdf,
                    final Param param,
                    final Student student
            ) {
                final int line = gnumToLine(param, student._gnum);
                ipdf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                ipdf.VrsOutn("name1", line, student._name);    // 氏名
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param svf
             * @param subclass
             * @param line：科目の列番号
             * @param stulist：List hrInfo._studentsのsublist
             * @return
             */
            private static void printSubclass(
                    final Vrw32alp svf,
                    final Param param,
                    final int col,
                    final SubClass subclass
            ) {
                //教科名
                svf.VrsOut("course1", subclass._classabbv);
                //科目名
                if (PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern)) {
                    final String[] subclassfields = null != subclass._subclassname && subclass._subclassname.length() <= 7 ? new String[] {"SUBCLASS"} : new String[] {"SUBCLASS_1", "SUBCLASS_2"};
                    if (subclass._electdiv) {
                        for (int i = 0; i < subclassfields.length; i++) {
                            svf.VrAttribute(subclassfields[i], ATTRIBUTE_ELECTDIV);
                        }
                    }
                    if (null != subclass._subclassname && subclass._subclassname.length() <= 7) {
                        svf.VrsOut(subclassfields[0], subclass._subclassname);
                    } else {
                        final String[] token = new String[2];
                        token[0] = subclass._subclassname.substring(0, Math.min(subclass._subclassname.length(), 8));
                        token[1] = (subclass._subclassname.length () <= 8) ? "" : subclass._subclassname.substring(8, Math.min(subclass._subclassname.length(), 8 * 2));
                        if (null != token) {
                            for (int i = 0; i < token.length && i < subclassfields.length; i++) {
                                svf.VrsOut(subclassfields[i], token[i]);
                            }
                        }
                    }
                    if (subclass._electdiv) {
                        for (int i = 0; i < subclassfields.length; i++) {
                            svf.VrAttribute(subclassfields[i], ATTRIBUTE_ELECTDIV);
                        }
                    }
                } else {
                    final String subclassfield = "subject1";
                    if (subclass._electdiv) { svf.VrAttribute(subclassfield, ATTRIBUTE_ELECTDIV); }
                    svf.VrsOut(subclassfield, subclass._subclassabbv);
                    if (subclass._electdiv) { svf.VrAttribute(subclassfield, ATTRIBUTE_NORMAL); }
                }
                //単位数
                if (!"J".equals(param._schoolKind)) {
                	svf.VrsOut("credit1", subclass.getPrintCredit());
                }
//                Form1.setRecordString(ipdf, fieldClassTeacher(param, subclass._staffname), subclass._staffname);
                if (PATTERN6.equals(param._outputPattern)) {
                    //授業時数
                    svf.VrsOut("lesson1", subclass.getJisu());
                }
                //項目名
                if (!PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
                    svf.VrsOut("ITEM1", param._item1Name);
                }
                if (!PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
                    svf.VrsOut("ITEM2", param._item2Name);
                }
            }

//            public static String fieldClassTeacher(final Param param, final String staffname) {
//                String fieldClassTeacher = null;
//                if ((PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern)) && getMS932ByteLength(staffname) > 4) {
//                    fieldClassTeacher  = "CLASS_TEACHER2";
//                } else if (!PATTERN6.equals(param._outputPattern)) {
//                    fieldClassTeacher  = "CLASS_TEACHER";
//                }
//                return fieldClassTeacher;
//            }

            public static void printSubclassStat(final Vrw32alp svf, final Param param, final int col, final SubClass subclass) {
                if (PATTERN4.equals(param._outputPattern)) {
//                    Form1.setRecordString(svf, "TOTAL_SUBCLASS", col, subclass._scoretotal);
//                    Form1.setRecordString(svf, "NUM", col, subclass._scoreCount);
//                    Form1.setRecordString(svf, "AVE_CLASS2", col, subclass._scoreaverage);
//                    Form1.setRecordString(svf, "AVE_SUBCLASS2", col, subclass._scoresubaverage);
////                    if (param._useKetten) {
////                        Form1.setRecordString(svf, "FAIL_STD", col, subclass._scoreFailCnt);
////                    }
                } else if (PATTERN5.equals(param._outputPattern)) {
                    svf.VrsOut("APPOINT_TIMES", subclass.getJisu());
                } else if (PATTERN6.equals(param._outputPattern)) {
//                    Form1.setRecordString(svf, "AVE_CLASS", col, subclass._scoreaverage);
//                    Form1.setRecordString(svf, "AVE_SUBCLASS", col, subclass._scoresubaverage);
//                    if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
//                        Form1.setRecordString(svf, "TOTAL_SUBCLASS", col, StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
//                    }
//                    Form1.setRecordString(svf, "MAX_SCORE", col, subclass._scoreMax);
//                    Form1.setRecordString(svf, "MIN_SCORE", col, subclass._scoreMin);
////                    if (param._useKetten) {
////                        Form1.setRecordString(svf, "FAIL_STD", col, subclass._scoreFailCnt);
////                    }
                }
                svf.VrsOutn("SCORE2", param._formMaxLine + 1, subclass._scoreaverage);
                svf.VrsOutn("SCORE2", param._formMaxLine + 2, subclass._scoresubaverage);
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
                if (!PATTERN5.equals(param._outputPattern)) {
//                    if (param._useKetten && ScoreDetail.isFail(param, detail)) { ipdf.VrAttribute(scoreField + String.valueOf(line) + "", ATTRIBUTE_KETTEN); }
                    final String aster;
                    aster = "";
                    final String printScore;
                    if (null == detail._score) {
                        printScore = StringUtils.defaultString(detail._scoreDi);
                    } else {
                        printScore = StringUtils.defaultString(detail._score);
                    }
                    final String scoreField;
                    if (PATTERN6.equals(param._outputPattern) && printScore.length() <= 2) {
                        scoreField = "SCORE1_2";
                    } else {
                        scoreField = "SCORE1";
                    }
                    svf.VrsOutn(scoreField, line, aster + printScore);
//                    if (param._useKetten && ScoreDetail.isFail(param, detail)) { ipdf.VrAttribute(scoreField + String.valueOf(line) + "", ATTRIBUTE_NORMAL); }
                }
                
                // 欠課
                if (!PATTERN4.equals(param._outputPattern) && null != detail._absent) {
                    final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                    final String field;
                    String pf = "";
                    if (PATTERN5.equals(param._outputPattern)) {
                        field = "SCORE";
                        if (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) {
                            pf = "_2";
                        }
                        if (0 != value) {
                            if (detail._isOver) { svf.VrAttribute(field + String.valueOf(line) + pf, ATTRIBUTE_KEKKAOVER); }
                            svf.VrsOut(field + String.valueOf(line) + pf, param.getAbsentFmt().format(detail._absent.floatValue()));
                            if (detail._isOver) { svf.VrAttribute(field + String.valueOf(line) + pf, ATTRIBUTE_NORMAL); }
                        }
                    } else {
                        if (0 != value) {
                            final String sv = param.getAbsentFmt().format(detail._absent.floatValue());
                            if (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) {
                                field = "kekka2_1";
                            } else {
                                if (PATTERN6.equals(param._outputPattern) && sv.length() <= 2) {
                                    field = "kekka3";
                                } else {
                                    field = "kekka1";
                                }
                            }
                            if (detail._isOver) { svf.VrAttributen(field + pf, line, ATTRIBUTE_KEKKAOVER); }
                            svf.VrsOutn(field + pf, line, sv);
                            if (detail._isOver) { svf.VrAttributen(field + pf, line, ATTRIBUTE_NORMAL); }
                        }
                    }
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
        log.fatal("$Revision: 76532 $ $Date: 2020-09-07 18:09:29 +0900 (月, 07 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;

        /** 学年 */
        final String _grade;
        final String _schoolKind;
        final String[] _classSelected;
        /** 出欠集計日付 */
        final String _sdate;
        final String _date;
        final String _testKindCd;
        final int _kekka;

        /** 総合順位出力 1:学級 2:学年 3:コース 4:学科 */
        final String _outputRank;
        /** 順位の基準点 1:総合点 2:平均点 */
        final String _outputKijun;
        /** フォーム 1:科目固定型 2:科目変動型 3:成績と出欠の記録 4:欠課時数と出欠の記録 5:仮評定と出欠の記録 6:両方 */
        final String _outputPattern;
        
        final int _kChikoku;
        final int _kChikokusoutai;
        final int _kSoutai;
        final int _kKekka;
        final int _kKesseki;

        /** フォーム（1:４５名、2:５０名）*/
        int _formMaxLine;
        
        /** 科目数　（1:15科目、2:20科目) */
        int _formMaxColumn;
        String _formname;
        
//        private String _yearDateS;
        private String _semesterName;
//        private String _semesterDateS;
        private TestItem _testItem;
        
        private static final String FROM_TO_MARK = "\uFF5E";
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        
        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;
        
        private KNJSchoolMst _knjSchoolMst;

        private KNJ_Get_Info _getinfo;
        
//        final boolean _useKetten = true;

        private String _rankName;
        private String _rankFieldName;
        private String _avgDiv;
        private String _d054Namecd2Max;
        private String _sidouHyoji;
        private List _d055Name1List;
        private Map _subclassMst;
        private boolean _isPrintSakiKamoku;
        
        private final String SUBCLASSCD999999 = "999999";
        
        final String _documentroot;
        final String _inkanWakuPath;

        private String _title;
        private String _item1Name;  // 明細項目名
        private String _item2Name;  // 明細項目名
        private String _item4Name;  // 総合点欄項目名
        private String _item5Name;  // 平均点欄項目名
        private String _form2Item4Name;  // 平均点欄項目名
        private String _form2Item5Name;  // 平均点欄項目名
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
//        private boolean _hasCompCredit; // 履修単位数/修得単位数なし
        private boolean _hasJudgementItem; // 判定会議資料用の項目あり
        private boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
        private boolean _isPrintDetailTani; // 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
        private int _failValue; // 欠点 100段階：30未満 5段階： 2未満
        private boolean _printPrgid;
        private boolean _isPrintKaikin;
        private boolean _isPrintKaikinAll;
        final String _knjd615rJugyoJisuLesson;
        final Map _psMap = new HashMap();
        private Map _c001Name1Map = new HashMap();
        private Map _c001Abbv1Map = new HashMap();
        private final boolean _isOutputDebug;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _schoolKind = getSchoolKind(db2, _year, _grade);
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _outputRank = request.getParameter("OUTPUT_RANK");
            _outputPattern = StringUtils.isBlank(request.getParameter("OUTPUT_PATERN")) ? PATTERN4 : request.getParameter("OUTPUT_PATERN");
            _kekka = NumberUtils.isDigits(request.getParameter("KEKKA")) ? Integer.parseInt(request.getParameter("KEKKA")) : 0;
            
            _kChikoku = NumberUtils.isDigits(request.getParameter("KCHIKOKU")) ? Integer.parseInt(request.getParameter("KCHIKOKU")) : 999;
            _kChikokusoutai = NumberUtils.isDigits(request.getParameter("KCHIKOKU_SOUTAI")) ? Integer.parseInt(request.getParameter("KCHIKOKU_SOUTAI")) : 999;
            _kSoutai = NumberUtils.isDigits(request.getParameter("KSOUTAI")) ? Integer.parseInt(request.getParameter("KSOUTAI")) : 999;
            _kKekka = NumberUtils.isDigits(request.getParameter("KKEKKA")) ? Integer.parseInt(request.getParameter("KKEKKA")) : 999;
            _kKesseki = NumberUtils.isDigits(request.getParameter("KKESSEKI")) ? Integer.parseInt(request.getParameter("KKESSEKI")) : 999;
            
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _knjd615rJugyoJisuLesson = request.getParameter("knjd615rJugyoJisuLesson");
            _getinfo = new KNJ_Get_Info();
            
            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            setD054Namecd2Max(db2);
            setD055Name1(db2);
            setC001Name1(db2);
            _definecode = createDefineCode(db2);
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = returnval.val1;  //学期名称
//            // 学期期間FROM
//            if (null == returnval.val2) {
//                _semesterDateS = _year + "-04-01";
//            } else {
//                _semesterDateS = returnval.val2;
//            }
//            // 年度の開始日
//            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, SEMEALL);
//            _yearDateS = returnval1.val2;
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            createKnjd065Obj();
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
            } else if (OUTPUT_RANK5.equals(_outputRank)) {
                _rankName = "コースグループ";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "MAJOR_AVG_RANK";
                } else {
                    _rankFieldName = "MAJOR_RANK";
                }
                _avgDiv = "5";
            } else {
                _rankName = "学年";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "GRADE_AVG_RANK";
                } else {
                    _rankFieldName = "GRADE_RANK";
                }
                _avgDiv = "1";
            }
            log.debug("順位名称=" + _rankName);
            setSubclassMst(db2);
            setPrintSakiKamoku(db2);
            setPrintKaikin(db2);
            
            _documentroot = request.getParameter("DOCUMENTROOT");
            File file = new File(_documentroot + "/image/KNJD615_keninwaku2.jpg");
            if (!file.exists()) {
                _inkanWakuPath = null;
                log.warn("file not found:" + file.getPath());
            } else {
                _inkanWakuPath = file.getPath();
            }
        }
        
        private String getSchoolKind(final DB2UDB db2, final String year, final String grade) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' "));
		}

		private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD615R' AND NAME = '" + propName + "' "));
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
            return KNJ_EditDate.h_format_JP(db2, _sdate) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(db2, _date);
        }
        
//        /** 「出欠の記録」の集計範囲 */
//        public String getTermAttend() {
//            return KNJ_EditDate.h_format_JP(_semesterDateS) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(_date);
//        }

        /**
         *  成績別処理クラス設定
         */
        private void createKnjd065Obj(
        ) {
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
            final String scoreDiv = _testKindCd.substring(4);
            if (SCORE_DIV_01.equals(scoreDiv)) {
                _isGakunenMatu = false;
//                _hasCompCredit = false;
                _hasJudgementItem = false;
                _enablePringFlg = false;
                _isPrintDetailTani = false;
                _failValue = 30;
                _printPrgid = true;

                _item1Name = "素点";
                _item2Name = "欠課";
                _item4Name = "総合点";
                _item5Name = "平均点";
                _form2Item4Name = _item4Name;
                _form2Item5Name = _item5Name;
                _title = _semesterName + " " + _testItem._testitemname + " 一覧表";
//                _title = _semesterName + " 学習評価一覧表";
                
            } else if (SCORE_DIV_08.equals(scoreDiv)) {
                _hasJudgementItem = false;
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
                _title = _semesterName + " " + _testItem._testitemname + "一覧表";
//                _title = _semesterName + " 学習評価一覧表";
                
            } else if (SCORE_DIV_09.equals(scoreDiv)) {
                _hasJudgementItem = false;
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
//                _title = _semesterName + " 学習評価一覧表";
            }
            if (PATTERN6.equals(_outputPattern)) {
                _title = "学年評価一覧表";
            }
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
        
        private void setD054Namecd2Max(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
                stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _d054Namecd2Max = rs.getString("NAMECD2");
                    _sidouHyoji = rs.getString("NAME1");
                }
            } catch (Exception ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setD055Name1(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d055Name1List = new ArrayList();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1 FROM V_NAME_MST ");
                stb.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D055' ORDER BY NAMECD2 ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (StringUtils.isBlank(rs.getString("NAME1"))) {
                        continue;
                    }
                    _d055Name1List.add(rs.getString("NAME1"));
                }
            } catch (Exception ex) {
                log.debug("getD055 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setC001Name1(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _c001Name1Map = new HashMap();
            _c001Abbv1Map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1, ABBV1 FROM V_NAME_MST ");
                stb.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C001' ORDER BY NAMECD2 ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (StringUtils.isBlank(rs.getString("ABBV1"))) {
                        continue;
                    }
                    _c001Name1Map.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                    _c001Abbv1Map.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                }
            } catch (Exception ex) {
                log.debug("setC001 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setPrintKaikin(final DB2UDB db2) {
            if (Integer.parseInt(_semester) < 3 || !(PATTERN5.equals(_outputPattern) || PATTERN6.equals(_outputPattern))) {
                return;
            }
            _isPrintKaikin = true;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT MAX(GRADE) AS GRADE ");
                stb.append(" FROM SCHREG_REGD_GDAT ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
                stb.append("   AND SCHOOL_KIND = (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _isPrintKaikinAll = _grade.equals(rs.getString("GRADE")); // 指定学年が学校種別の最大の学年なら、「皆勤」判定出力する
                }
            } catch (Exception ex) {
                log.debug("setPrintKaikin exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info("皆勤を出力するか :" + _isPrintKaikinAll);
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
                sql += " T2.CLASSCD = T1.CLASSCD ";
                sql += " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
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
