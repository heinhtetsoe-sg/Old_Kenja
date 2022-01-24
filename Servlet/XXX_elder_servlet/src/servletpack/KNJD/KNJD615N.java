// kanji=漢字
/*
 * $Id: 88c32cf1a73a35d0c3735e9bae813790845b6b18 $
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: 88c32cf1a73a35d0c3735e9bae813790845b6b18 $
 */
public class KNJD615N {
    private static final Log log = LogFactory.getLog(KNJD615N.class);

//    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

//    private static final String OUTPUT_KJUN2 = "2";
//    private static final String OUTPUT_KJUN3 = "3";
//    private static final String OUTPUT_RANK1 = "1";
//    private static final String OUTPUT_RANK2 = "2";
//    private static final String OUTPUT_RANK3 = "3";
//    private static final String OUTPUT_RANK4 = "4";
    
//    private static final String SIDOU_INPUT_INF_MARK = "1";
//    private static final String SIDOU_INPUT_INF_SCORE = "2";
    
    private static final String ATTRIBUTE_KETTEN = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";
    private static final String CLASSCD_LHR = "94";

    private Param _param;
    
    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alp svf = null;
        boolean hasData = false;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            _param = createParam(request, db2);
            
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            sd.setSvfInit(request, response, svf);

            final Form form = new Form();
            for (int h = 0; h < _param._classSelected.length; h++) { //印刷対象HR組

                final List courses = Course.createCourses(db2, _param, _param._classSelected[h]);
                log.debug("コース数=" + courses.size());

                final HRInfo hrInfo = new HRInfo(_param._classSelected[h]);  //HR組
                hrInfo._courses = courses;

//                if (_param._outputCourse) {
//                    for (final Iterator it = courses.iterator(); it.hasNext();) {
//                        final Course course = (Course) it.next();
//                        hrInfo.load(db2, _param, course._coursecd);
//
//                        // 印刷処理
//                        if (printMain(svf, hrInfo)) {
//                            hasData = true;
//                        }
//                    }
//                } else {
                    hrInfo.load(db2, _param, null);
                    // 印刷処理
                    if (form.print(svf, _param, hrInfo)) {
                        hasData = true;
                    }
//                }
            }
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
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
        
        private static List createCourses(final DB2UDB db2, final Param param, final String gradeHrclass) throws SQLException {
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

    /*
     *  PrepareStatement作成 --> 科目別平均の表
     */
//    private String sqlSubclassAverage() {
//        final StringBuffer stb = new StringBuffer();
//
//        stb.append(" SELECT  SUBCLASSCD");
//        stb.append("        ," + _knjdobj._fieldChaircd + " AS CHAIRCD");
//        stb.append("        ,ROUND(AVG(FLOAT(" + _knjdobj._fieldname + "))*10,0)/10 AS AVG_SCORE");
//        stb.append(" FROM RECORD_DAT W1");
//        stb.append(" WHERE YEAR = '" + _param._year + "'");
//        stb.append(" GROUP BY SUBCLASSCD");
//        stb.append("," + _knjdobj._fieldChaircd);
//        stb.append(" HAVING " + _knjdobj._fieldChaircd +" IS NOT NULL");
//
//        return stb.toString();
//    }


    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる処理クラス
     */
    private static class KNJD065_COMMON {
//        String _fieldChaircd;
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
        private boolean _hasCompCredit; // 履修単位数/修得単位数なし
        private boolean _hasJudgementItem; // 判定会議資料用の項目あり
//        private boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
        private boolean _isPrintDetailTani; // 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
        private int _failValue; // 欠点 100段階：30未満 5段階： 2未満
//        String _fieldname;
//        String _fieldname2;
        private boolean _printPrgid;

//        ScoreValue getTargetValue(final ScoreDetail d) {
//            return d._score;
//        }
        
        /** {@inheritDoc} */
        public String toString() {
            return getClass().getName();
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
        private final Map _subclasses = new TreeMap();
//        private List _ranking;
//        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
//        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
//        private BigDecimal _perHrPresent;  // 学級の出席率
//        private BigDecimal _perHrAbsent;  // 学級の欠席率
//        private String _HrCompCredits;  // 学級の履修単位数
//        private String _HrMLesson;  // 学級の授業日数
//        private String _avgHrTotal;   // 総合点の学級平均
//        private String _avgHrAverage; // 平均点の学級平均
//        private int _avgHrCount; // 総合点の学級の母集団の数
//        private String _avgGradeAverage; // 平均点の学級平均
//        private String _maxHrTotal;   // 総合点の最高点
//        private String _minHrTotal;   // 総合点の最低点
//        private String _failHrTotal;  // 欠点の数

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
            loadAttend(db2, param);
            loadAttendRemark(db2, param);
//            loadHrclassAverage(db2, param, courseCd);
//            loadRank(db2, param);
            loadScoreDetail(db2, param);
//            _ranking = createRanking(param);
//            log.debug("RANK:" + _ranking);
//            setSubclassAverage(param);
//            setHrTotal();  // 学級平均等の算出
//            setHrTotalMaxMin();        
//            setHrTotalFail();
//            setSubclassGradeAverage(db2, param);
//            if (param._knjdobj._hasJudgementItem) {
//                loadPreviousCredits(db2, param);  // 前年度までの修得単位数取得
//                loadPreviousMirisyu(db2, param);  // 前年度までの未履修（必須科目）数
//                loadQualifiedCredits(db2, param);  // 今年度の資格認定単位数
//            }
        }

        private void loadAttendRemark(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SD.SCHREGNO, ");
                stb.append("     RMK.REMARK1 AS REMARK ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT SD ");
                stb.append(" INNER JOIN ATTEND_SEMES_REMARK_DAT RMK ON SD.YEAR = RMK.YEAR ");
                stb.append("      AND SD.SCHREGNO = RMK.SCHREGNO ");
                stb.append("      AND RMK.SEMESTER || RMK.MONTH BETWEEN '" + param.getSemesterMonth(db2, param._sDate)  + "' AND '" + param.getSemesterMonth(db2, param._date) + "' ");
                stb.append(" WHERE ");
                stb.append("     SD.YEAR = '" + param._year + "' ");
                stb.append("     AND SD.SEMESTER = '" + ("9".equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                stb.append("     AND SD.GRADE || SD.HR_CLASS = '" + _hrclassCd + "' ");
                stb.append("     AND RMK.REMARK1 IS NOT NULL ");
                stb.append(" ORDER BY SD.SCHREGNO, RMK.SEMESTER || RMK.MONTH ");
                
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    if (null == student._attendSemesRemarkDatRemark1) {
                        student._attendSemesRemarkDatRemark1 = "";
                    } else {
                        student._attendSemesRemarkDatRemark1 += " ";
                    }
                    student._attendSemesRemarkDatRemark1 += rs.getString("REMARK");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }            
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
//                    if (_param._noKetuban) {
//                        gnum++;
//                    } else {
                        gnum = rs.getInt("ATTENDNO");
//                    }
                    final Student student = new Student(rs.getString("SCHREGNO"), this, gnum);
                    _students.add(student);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
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

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO ");
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

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            TransInfo transInfo = null;
                            try {
                                final String d1 = rs.getString("KBN_DATE1");
                                final String d2 = rs.getString("KBN_DATE2");
                                if (null != d1) {
                                    final String n1 = rs.getString("KBN_NAME1");
                                    transInfo = new TransInfo(d1, n1);
                                } else if (null != d2) {
                                    final String n2 = rs.getString("KBN_NAME2");
                                    transInfo = new TransInfo(d2, n2);
                                }
                            } catch (final SQLException e) {
                                 log.error("SQLException", e);
                            }
                            if (null == transInfo) {
                                transInfo = new TransInfo(null, null);
                            }
                            student._attendNo = rs.getString("ATTENDNO");
                            student._name = rs.getString("NAME");
                            student._transInfo = transInfo;

                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
        
        /**
         * SQL 任意の生徒の学籍情報を取得するSQL
         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
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
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = '" + param._semester + "' ");
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

        private void loadAttend(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", param._useCurriculumcd);
            paramMap.put("useVirus", param._useVirus);
            paramMap.put("useKoudome", param._useKoudome);
            paramMap.put("DB2UDB", db2);
            try {
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._semesFlg,
                        param._definecode,
                        param._knjSchoolMst,
                        param._year,
                        param.SSEMESTER,
                        param._semester,
                        (String) param._hasuuMap.get("attendSemesInState"),
                        param._periodInState,
                        (String) param._hasuuMap.get("befDayFrom"),
                        (String) param._hasuuMap.get("befDayTo"),
                        (String) param._hasuuMap.get("aftDayFrom"),
                        (String) param._hasuuMap.get("aftDayTo"),
                        param._grade,
                        _hrclassCd.substring(2, 5),
                        null,
                        "SEMESTER",
                        paramMap
                );
                log.debug(" attendSemesInState = " + param._hasuuMap.get("attendSemesInState"));
//                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);

                final ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                
                    student._attendInfo._lesson = rs.getInt("LESSON");
                    student._attendInfo._mLesson = rs.getInt("MLESSON");
                    student._attendInfo._suspend = rs.getInt("SUSPEND");
                    student._attendInfo._mourning = rs.getInt("MOURNING");
                    student._attendInfo._absent = rs.getInt("SICK");
                    student._attendInfo._present = rs.getInt("PRESENT");
                    student._attendInfo._late = rs.getInt("LATE");
                    student._attendInfo._early = rs.getInt("EARLY");
                    student._attendInfo._transDays = rs.getInt("TRANSFER_DATE");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append("    SELECT ");
                sql.append("        W1.SCHREGNO, ");
                if ("1".equals(param._knjSchoolMst._absentCov) || "3".equals(param._knjSchoolMst._absentCov)) {
                    sql.append("        W1.SEMESTER, ");
                }
                sql.append("        SUM(VALUE(REIHAI_KEKKA,0)) AS REIHAI_KEKKA, "); // SEQ='001'
                sql.append("        SUM(VALUE(M_KEKKA_JISU,0)) AS M_KEKKA_JISU, "); // SEQ='002'
                sql.append("        SUM(VALUE(REIHAI_TIKOKU,0)) AS REIHAI_TIKOKU "); // SEQ='003'
                sql.append("    FROM ");
                sql.append("        V_ATTEND_SEMES_DAT W1 ");
                sql.append("    WHERE ");
                sql.append("        W1.YEAR = '" + param._year + "' ");
                sql.append("        AND SEMESTER BETWEEN '" + param.SSEMESTER + "' AND '" + param._semester + "' ");
                sql.append("        AND W1.SEMESTER || W1.MONTH IN " + (String) param._hasuuMap.get("attendSemesInState") + " ");
                sql.append("        AND W1.SCHREGNO = ? ");
                sql.append("    GROUP BY W1.SCHREGNO ");
                if ("1".equals(param._knjSchoolMst._absentCov) || "3".equals(param._knjSchoolMst._absentCov)) {
                    sql.append("        , W1.SEMESTER ");
                }

                log.debug(" kekkajisu sql = " + sql);
                ps = db2.prepareStatement(sql.toString());
                
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._attendInfo._kekkaJisu = new BigDecimal(0);
                    
                    ps.setString(1, student._schregno);
                    final ResultSet rs = ps.executeQuery();
                    
                    final String absentCov = param._knjSchoolMst._absentCov;
                    final String absentCovLate = param._knjSchoolMst._absentCovLate;
                    BigDecimal sumKekkaJisu = new BigDecimal(0);
                    while (rs.next()) {
                        BigDecimal kekkaJisu = rs.getBigDecimal("REIHAI_KEKKA");
                        if (NumberUtils.isDigits(absentCovLate) && Integer.parseInt(absentCovLate) > 0) {
                            final BigDecimal kansan;
                            final BigDecimal lateEarly = rs.getBigDecimal("M_KEKKA_JISU").add(rs.getBigDecimal("REIHAI_TIKOKU"));
                            if ("3".equals(absentCov) || "4".equals(absentCov)) {
                                kansan = lateEarly.divide(new BigDecimal(absentCovLate), 10, BigDecimal.ROUND_HALF_UP); // 小数
                            } else {
                                kansan = lateEarly.divide(new BigDecimal(absentCovLate), 0, BigDecimal.ROUND_FLOOR); // 整数; 余りは切り捨て
                            }
                            kekkaJisu = kekkaJisu.add(kansan);
                        }
                        sumKekkaJisu = sumKekkaJisu.add(kekkaJisu);
                    }
                    student._attendInfo._kekkaJisu = sumKekkaJisu;

                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }
        
//        private void loadHrclassAverage(
//                final DB2UDB db2,
//                final Param param,
//                final String courseCd
//        ) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                final String sql = sqlHrclassAverage(param, _hrclassCd, courseCd);
//                ps = db2.prepareStatement(sql);
//
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    if ("HR".equals(rs.getString("FLG"))) {
//                        _avgHrTotal = rs.getString("AVG_HR_TOTAL");
//                        _avgHrAverage = rs.getString("AVG_HR_AVERAGE");
//                        _avgHrCount = rs.getInt("COUNT");
//                    } else if ("GRADE".equals(rs.getString("FLG"))) {
//                        _avgGradeAverage = rs.getString("AVG_HR_AVERAGE");
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("SQLException", e);
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//        }
//        
//        /**
//         * SQL 総合点・平均点の学級平均を取得するSQL
//         */
//        private String sqlHrclassAverage(final Param param, final String hrClass, final String courseCd) {
//            final StringBuffer stb = new StringBuffer();
//
//            stb.append("WITH ");
//
//            //対象生徒の表 クラスの生徒
//            stb.append(" SCHNO_A AS(");
//            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
//            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
//            stb.append("            ,CASE WHEN W1.GRADE||W1.HR_CLASS = '" + hrClass + "' THEN '1' ELSE '0' END AS IS_HR ");
//            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
//            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
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
//            
//            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
//            if (SEMEALL.equals(param._semester)) {
//                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
//            } else {
//                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
//                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
//                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
//            }
//            stb.append("         AND W1.GRADE = '" + hrClass.substring(0, 2) + "' ");
//            stb.append("         AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
////            if (param._outputCourse) {
////                stb.append("         AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
////            }
//            stb.append(") ");
//
//            stb.append("SELECT 'HR' AS FLG ");
//            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
//            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
//            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
//            stb.append("  FROM  RECORD_RANK_DAT W3 ");
//            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
//            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
//            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' ");
//            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
//            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
//            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_HR = '1') ");
//            stb.append("UNION ALL ");
//            stb.append("SELECT 'GRADE' AS FLG ");
//            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
//            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
//            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
//            stb.append("  FROM  RECORD_RANK_DAT W3 ");
//            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
//            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
//            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' ");
//            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
//            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
//            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
//
//            return stb.toString();
//        }
//
//        private void loadRank(
//                final DB2UDB db2,
//                final Param param
//        ) {
//            PreparedStatement ps = null;
//
//            try {
//                final String sql = sqlStdTotalRank(param);
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//                    ps.setString(1, student._schregno);
//
//                    ResultSet rs = null;
//                    try {
//                        rs = ps.executeQuery();
//                        if (rs.next()) {
//                            student._classRank = rs.getInt("CLASS_RANK");
//                            student._rank = rs.getInt("TOTAL_RANK");
//                            student._scoreSum = rs.getString("TOTAL_SCORE");
//                            student._scoreAvg = rs.getString("TOTAL_AVG");
//                        }
//                    } catch (SQLException e) {
//                        log.error("SQLException", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("SQLException", e);
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }
//        
//        /**
//         * SQL 任意の生徒の順位を取得するSQL
//         */
//        private String sqlStdTotalRank(final Param param) {
//            final StringBuffer stb = new StringBuffer();
//
//            stb.append("WITH ");
//
//            //対象生徒の表 クラスの生徒
//            stb.append(" SCHNO_A AS(");
//            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
//            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
//            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
//            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
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
//            
//            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
//            if (SEMEALL.equals(param._semester)) {
//                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
//            } else {
//                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
//                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
//                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
//            }
//            stb.append("         AND W1.SCHREGNO = ? ");
//            stb.append(") ");
//
//            //メイン表
//            stb.append("SELECT  W3.SCHREGNO ");
//            if (OUTPUT_KJUN2.equals(param._outputKijun)) {
//                stb.append("   ,CLASS_AVG_RANK AS CLASS_RANK");
//            } else if (OUTPUT_KJUN3.equals(param._outputKijun)) {
//                stb.append("   ,CLASS_DEVIATION_RANK AS CLASS_RANK");
//            } else {
//                stb.append("   ,CLASS_RANK ");
//            }
//            stb.append("       ," + param._rankFieldName + "  AS TOTAL_RANK ");
//            stb.append("       ,W3.SCORE AS TOTAL_SCORE ");
//            stb.append("       ,DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
//            stb.append("  FROM  RECORD_RANK_DAT W3 ");
//            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
//            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
//            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' ");
//            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
//            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
//            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
//            // 総合点・平均点を算出。順位は空白。法政以外の処理。
//            // RECORD_RANK_DAT.SUBCLASSCD = '999999' のレコードがない生徒のみ算出。
////            if (!param._isHosei) {
////                stb.append("UNION ");
////                stb.append("SELECT  W3.SCHREGNO ");
////                stb.append("       ,cast(null as smallint) AS CLASS_RANK ");
////                stb.append("       ,cast(null as smallint) AS TOTAL_RANK ");
////                stb.append("       ,SUM(W3.SCORE) AS TOTAL_SCORE ");
////                stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS TOTAL_AVG ");
////                stb.append("  FROM  RECORD_RANK_DAT W3 ");
////                stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
////                stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
////                stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' ");
////                stb.append("   AND  EXISTS(SELECT 'X' FROM SUBCLASS_MST W2 WHERE ");
////                if ("1".equals(param._useCurriculumcd)) {
////                    stb.append("               W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD = ");
////                    stb.append("               W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD ");
////                } else {
////                    stb.append("               W3.SUBCLASSCD = W2.SUBCLASSCD ");
////                }
////                stb.append("               ) ");
////                stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
////                stb.append("   AND  W3.SCHREGNO NOT IN( ");
////                stb.append("        SELECT  R1.SCHREGNO ");
////                stb.append("          FROM  RECORD_RANK_DAT R1 ");
////                stb.append("         WHERE  R1.YEAR = '" + param._year + "' ");
////                stb.append("           AND  R1.SEMESTER = '" + param._semester + "' ");
////                stb.append("           AND  R1.TESTKINDCD || R1.TESTITEMCD = '" + param._testKindCd + "' ");
////                stb.append("           AND  R1.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
////                stb.append("           AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE R1.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
////                stb.append("   ) ");
////                if ("1".equals(param._useCurriculumcd)) {
////                    stb.append("   AND  W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD NOT IN( ");
////                    stb.append("        SELECT  R2.ATTEND_CLASSCD || '-' || R2.ATTEND_SCHOOL_KIND || '-' || R2.ATTEND_CURRICULUM_CD || '-' || R2.ATTEND_SUBCLASSCD ");
////                } else {
////                    stb.append("   AND  W3.SUBCLASSCD NOT IN( ");
////                    stb.append("        SELECT  R2.ATTEND_SUBCLASSCD ");
////                }
////                stb.append("          FROM  SUBCLASS_REPLACE_COMBINED_DAT R2 ");
////                stb.append("         WHERE  R2.YEAR = '" + param._year + "' ");
////                stb.append("           AND  R2.REPLACECD = '1' ");
////                stb.append("   ) ");
////                stb.append("GROUP BY W3.SCHREGNO ");
////            }
//            return stb.toString();
//        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdSubclassDetail(param, _hrclassCd);
                log.debug(" subclass detail sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
//                    if (param._knjdobj._enablePringFlg && "1".equals(rs.getString("PRINT_FLG"))) {
//                        continue;
//                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String classCd = subclassCd == null ? "" : subclassCd.substring(1, 3);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T) || classCd.equals(CLASSCD_LHR)) {
                        final ScoreDetail scoreDetail = new ScoreDetail(
                                getSubClass(rs, _subclasses),
//                                ScoreValue.create(rs.getString("SCORE"),rs.getString("SUBCLASSCD")),
                                rs.getString("ASSESS_LEVEL"),
//                                null,
//                                ScoreValue.create(rs.getString("KARI_HYOUTEI"),rs.getString("SUBCLASSCD")),
                                (Integer) rs.getObject("REPLACEMOTO"),
                                (String) rs.getObject("PRINT_FLG"),
                                null,
//                                rs.getString("SLUMP"),
//                                rs.getString("SLUMP_MARK"),
//                                rs.getString("SLUMP_SCORE"),
                                null,
                                (Integer) rs.getObject("COMP_CREDIT"),
                                (Integer) rs.getObject("GET_CREDIT"),
                                (Integer) rs.getObject("CREDITS")
//                                rs.getString("CHAIRCD")
                        );
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", param._useCurriculumcd);
                paramMap.put("useVirus", param._useVirus);
                paramMap.put("useKoudome", param._useKoudome);
                paramMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW");
                paramMap.put("DB2UDB", db2);
                paramMap.put("absenceDiv", "2");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._semesFlg,
                        param._definecode,
                        param._knjSchoolMst,
                        param._year,
                        param.SSEMESTER,
                        param._semester,
                        (String) param._hasuuMap.get("attendSemesInState"),
                        param._periodInState,
                        (String) param._hasuuMap.get("befDayFrom"),
                        (String) param._hasuuMap.get("befDayTo"),
                        (String) param._hasuuMap.get("aftDayFrom"),
                        (String) param._hasuuMap.get("aftDayTo"),
                        _hrclassCd.substring(0, 2),
                        _hrclassCd.substring(2),
                        null,
                        paramMap
                        );
                //log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
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
                                scoreDetail = new ScoreDetail(subClass, 
                                        //null, 
                                        null, 
                                        //null, null, 
                                        null, null, null,
                                        //null, null, null,
                                        null, null, null, null);
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
                    final Integer specialAbsentMinutes = (Integer) rs.getObject("SPECIAL_SICK_MINUTES3");
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
                    scoreDetail._jisu = (Integer) rs.getObject("MLESSON");
                    scoreDetail._absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                    if ("1".equals(rs.getString("IS_COMBINED_SUBCLASS"))) {
                        scoreDetail._isCombinedSubclass = true;
                        if (null == scoreDetail._replacedSick) {
                            scoreDetail._replacedSick = new Double(0.0);
                        }
                        scoreDetail._replacedSick = new Double(scoreDetail._replacedSick.doubleValue() + Double.parseDouble(rs.getString("REPLACED_SICK")));
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._replacedSick, scoreDetail._absenceHigh);
                    } else {
                        if (null == scoreDetail._absent) {
                            scoreDetail._absent = new Double(0.0);
                        }
                        scoreDetail._absent = new Double(scoreDetail._absent.doubleValue() + Double.parseDouble(rs.getString("SICK2")));
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(final Param param, final String hrclassCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + hrclassCd + "' ");
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
            stb.append("    FROM    RECORD_RANK_DAT W3 ");
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
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' ");
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
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");

//            //仮評定の表
//            stb.append(",RECORD_KARI_HYOUTEI AS(");
//            stb.append("    SELECT  W3.SCHREGNO, ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//            }
//            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
//            stb.append("     , W3.SCORE AS KARI_HYOUTEI ");
//            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
//            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("       AND W1.LEAVE = 0 ");
//            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
//            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
//            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd.substring(0, 4) + SCORE_DIV_09 + "' ");
//            stb.append("     ) ");
//
//            //追試試験データの表
//            stb.append(",SUPP_EXA AS(");
//            stb.append("    SELECT  W3.SCHREGNO, ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//            }
//            stb.append("            W3.SUBCLASSCD AS SUBCLASSCD,SCORE_FLG ");
//            stb.append("           ,CASE WHEN SCORE_PASS IS NOT NULL THEN RTRIM(CHAR(SCORE_PASS)) ");
//            stb.append("                 ELSE NULL END AS SCORE_PASS ");
//            stb.append("    FROM    SUPP_EXA_DAT W3 ");
//            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
//            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
//            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' AND ");
//            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
//            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
//            stb.append("     ) ");
//
//            //成績不振科目データの表
//            stb.append(",RECORD_SLUMP AS(");
//            stb.append("    SELECT  W3.SCHREGNO, ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//            }
//            stb.append("            W3.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP, ");
//            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK, ");
//            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
//            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM RELATIVEASSESS_MST L3 ");
//            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//                stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//                stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            }
//            stb.append("          ) ELSE ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM ASSESS_MST L3 ");
//            stb.append("           WHERE L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("          ) ");
//            stb.append("         END ");
//            stb.append("        END AS SLUMP_SCORE ");
//            stb.append("    FROM    RECORD_SLUMP_DAT W3 ");
//            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW W1 ON W1.YEAR = W3.YEAR ");
//            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
//            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
//            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//                stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//                stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            }
//            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
//            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
//            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' AND ");
//            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
//            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
//            stb.append("     ) ");
//            if (param.isRecordSlump()) {
//                //成績不振科目データの表
//                stb.append(",RECORD_SLUMP AS(");
//                stb.append("    SELECT  W3.SCHREGNO, ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//                }
//                stb.append("            W3.SUBCLASSCD AS SUBCLASSCD, W3.SLUMP ");
//                stb.append("    FROM    RECORD_SLUMP_DAT W3 ");
//                stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
//                stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
//                stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd + "' AND ");
//                stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
//                stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
//                stb.append("     ) ");
//            }
//            if (param.isPerfectRecord()) {
//                //満点マスタの表
//                stb.append(" , PERFECT_T AS ( ");
//                stb.append(" SELECT ");
//                stb.append("     YEAR, ");
//                stb.append("     SEMESTER, ");
//                stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
//                stb.append("     CLASSCD, ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
//                }
//                stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
//                stb.append("     MIN(DIV) AS DIV ");
//                stb.append(" FROM ");
//                stb.append("     PERFECT_RECORD_DAT ");
//                stb.append(" WHERE ");
//                stb.append("     YEAR = '" + param._year + "' ");
//                stb.append("     AND SEMESTER = '" + param._semester + "' ");
//                stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testKindCd + "' ");
//                stb.append(" GROUP BY ");
//                stb.append("     YEAR, ");
//                stb.append("     SEMESTER, ");
//                stb.append("     TESTKINDCD || TESTITEMCD, ");
//                stb.append("     CLASSCD, ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
//                }
//                stb.append("     SUBCLASSCD ");
//                stb.append(" ), PERFECT_MAIN AS ( ");
//                stb.append(" SELECT ");
//                stb.append("     T1.* ");
//                stb.append(" FROM ");
//                stb.append("     PERFECT_RECORD_DAT T1 ");
//                stb.append(" WHERE ");
//                stb.append("     EXISTS( ");
//                stb.append("         SELECT ");
//                stb.append("             'x' ");
//                stb.append("         FROM ");
//                stb.append("             PERFECT_T E1 ");
//                stb.append("         WHERE ");
//                stb.append("             E1.YEAR = T1.YEAR ");
//                stb.append("             AND E1.SEMESTER = T1.SEMESTER ");
//                stb.append("             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ");
//                stb.append("             AND E1.CLASSCD = T1.CLASSCD ");
//                stb.append("             AND E1.SUBCLASSCD = ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
//                }
//                stb.append("                                 T1.SUBCLASSCD ");
//                stb.append("             AND E1.DIV = T1.DIV ");
//                stb.append("     ) ");
//                stb.append(" ), SCH_PERFECT AS ( ");
//                stb.append(" SELECT DISTINCT ");
//                stb.append("     T1.SCHREGNO, ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("            L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
//                }
//                stb.append("     L1.SUBCLASSCD AS SUBCLASSCD, ");
//                stb.append("     L1.PASS_SCORE ");
//                stb.append(" FROM ");
//                stb.append("     SCHNO_A T1 ");
//                stb.append("     LEFT JOIN PERFECT_MAIN L1 ON L1.YEAR = T1.YEAR ");
//                stb.append("          AND L1.GRADE = CASE WHEN L1.DIV = '01' THEN '00' ELSE T1.GRADE END ");
//                stb.append("          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN L1.DIV IN ('01','02') THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ");
//                stb.append(" WHERE ");
//                stb.append("     T1.LEAVE = 0 ");
//                stb.append(" ) ");
//            }
            
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
//            stb.append("        ,T34.SCORE_FLG ");
//            stb.append("        ,CASE WHEN T34.SCORE_FLG IS NOT NULL THEN T34.SCORE_PASS ");
//            stb.append("              ELSE T3.SCORE END AS SCORE ");
            stb.append("        ,T3.SCORE AS SCORE ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
//            stb.append("        ,T35.KARI_HYOUTEI ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSABBV ");
            stb.append("        ,T7.SUBCLASSNAME ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
//            stb.append("        ,K1.SLUMP ");
//            stb.append("        ,K1.SLUMP_MARK ");
//            stb.append("        ,K1.SLUMP_SCORE ");
//            if (param.isRecordSlump()) {
//                stb.append("    ,K1.SLUMP ");
//            } else {
//                stb.append("    ,cast(null as varchar(1)) as SLUMP ");
//            }
//            if (param.isPerfectRecord()) {
//                stb.append("    ,K2.PASS_SCORE ");
//            } else {
//                stb.append("    ,cast(null as smallint) as PASS_SCORE ");
//            }
            stb.append("    , W23.STAFFNAME ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
//            stb.append(" LEFT JOIN SUPP_EXA T34 ON T34.SUBCLASSCD = T1.SUBCLASSCD AND T34.SCHREGNO = T1.SCHREGNO");
//            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI T35 ON T35.SUBCLASSCD = T1.SUBCLASSCD AND T35.SCHREGNO = T1.SCHREGNO");
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
//            //成績不振科目データの表
//            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");
//            if (param.isRecordSlump()) {
//                //成績不振科目データの表
//                stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");
//            }
//            if (param.isPerfectRecord()) {
//                //満点マスタの表
//                stb.append(" LEFT JOIN SCH_PERFECT K2 ON K2.SCHREGNO = T1.SCHREGNO AND K2.SUBCLASSCD = T1.SUBCLASSCD");
//            }
            stb.append("     LEFT JOIN STAFF_MST W23 ON W23.STAFFCD = T1.STAFFCD ");
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
            } catch (SQLException e) {
                 log.error("SQLException", e);
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
                } catch (SQLException e) {
                     log.error("SQLException", e);
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
//                    } catch (SQLException e) {
//                        log.error("SQLException", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("SQLException", e);
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }
//
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
//                    } catch (SQLException e) {
//                        log.error("SQLException", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("SQLException", e);
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }
//
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
//                    } catch (SQLException e) {
//                        log.error("SQLException", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
//            } catch (SQLException e) {
//                log.error("SQLException", e);
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }
//        }
//        
//        /**
//         * 欠点の算出
//         */
//        private void setHrTotalFail() {
//            int countFail = 0;
//            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
//                final Student student = (Student) itS.next();
//                final Total totalObj = student._total;
//                if (null != totalObj) {
//                    if (0 < totalObj._countFail) {
//                        countFail += totalObj._countFail;
//                    }
//                }
//            }
//            if (0 < countFail) {
//                _failHrTotal = String.valueOf(countFail);
//            }
//        }
//
//        /**
//         * 最高点・最低点の算出
//         */
//        private void setHrTotalMaxMin() {
//            int totalMax = 0;
//            int totalMin = Integer.MAX_VALUE;
//            int countT = 0;
//            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
//                final Student student = (Student) itS.next();
//                if (null == student._scoreSum) {
//                    continue;
//                }
//                countT++;
//                final int totalInt = Integer.parseInt(student._scoreSum);
//                //最高点
//                totalMax = Math.max(totalMax, totalInt);
//                //最低点
//                totalMin = Math.min(totalMin, totalInt);
////              log.debug("total="+total+", totalMax="+totalMax+", totalMin="+totalMin);
//            }
//            if (0 < countT) {
//                _maxHrTotal = String.valueOf(totalMax);
//                _minHrTotal = String.valueOf(totalMin);
//            }
//        }
//
//        /**
//         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
//         */
//        private void setSubclassAverage(final Param param) {
//            final Map map = new HashMap();
//
//            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
//                final Student student = (Student) itS.next();
//                for (final Iterator itD = student._scoreDetails.values().iterator(); itD.hasNext();) {
//                    final ScoreDetail detail = (ScoreDetail) itD.next();
////                    final ScoreValue val = detail.getPatternAsses();
//                    final ScoreValue scorevalue = param._knjdobj.getTargetValue(detail);
//                    if (null == map.get(detail._subClass)) {
//                        map.put(detail._subClass, new int[5]);
//                    }
//                    final int[] arr = (int[]) map.get(detail._subClass);
//                    if (null != scorevalue && scorevalue.hasIntValue()) {
//                        arr[0] += scorevalue.getScoreAsInt();
//                        arr[1]++;
//                        //最高点
//                        if (arr[2] < scorevalue.getScoreAsInt()) {
//                            arr[2] = scorevalue.getScoreAsInt();
//                        }
//                        //最低点
//                        if (arr[3] > scorevalue.getScoreAsInt() || arr[1] == 1) {
//                            arr[3] = scorevalue.getScoreAsInt();
//                        }
//                    }
//                    //欠点（赤点）
//                    if (ScoreDetail.isFail(param, detail)) {
//                        arr[4]++;
//                    }
//                }
//            }
//
//            for (final Iterator it = _subclasses.values().iterator(); it.hasNext();) {
//                final SubClass subclass = (SubClass) it.next();
//                if (map.containsKey(subclass)) {
//                    final int[] val = (int[]) map.get(subclass);
//                    if (0 != val[1]) {
//                        double d = round10(val[0], val[1]);
//                        subclass._scoreaverage = DEC_FMT1.format(d);
//                        subclass._scoretotal = String.valueOf(val[0]);
//                        subclass._scoreCount = String.valueOf(val[1]);
//                        subclass._scoreMax = String.valueOf(val[2]);
//                        subclass._scoreMin = String.valueOf(val[3]);
//                        if (0 != val[4]) {
//                            subclass._scoreFailCnt = String.valueOf(val[4]);
//                        }
//                    }
//                }
//            }
//        }
//
//        private static double round10(final int a, final int b) {
//            return Math.round(a * 10.0 / b) / 10.0;
//        }
//        
//        /**
//         * 学級平均の算出
//         */
//        private void setHrTotal() {
//            int totalT = 0;
//            int countT = 0;
//            double totalA = 0;
//            int countA = 0;
//            int mlesson = 0;
//            int present = 0;
//            int absent = 0;
//            int[] arrc = {0,0};  // 履修単位
//            int[] arrj = {0,0};  // 授業日数
//            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
//                final Student student = (Student) itS.next();
//                final Total totalObj = student._total;
//                if (null != totalObj) {
//                    if (0 < totalObj._count) {
//                        totalT += totalObj._total;
//                        countT++;
//                    }
////                    if (null != totalObj._avgBigDecimal) {
////                        totalA += totalObj._avgBigDecimal.doubleValue();
////                        countA++;
////                    }
////                    if (0< totalObj._avgcount) {
////                        totalA += totalObj._avgtotal;
////                        countA += totalObj._avgcount;
//                    if (0< totalObj._count) {
//                        totalA += totalObj._total;
//                        countA += totalObj._count;
//                    }
//                }
//                final AttendInfo attend = student._attendInfo;
//                if (null != attend) {
//                    mlesson += attend._mLesson;
//                    present += attend._present;
//                    absent += attend._absent;
//                    arrj = setMaxMin(arrj[0], arrj[1], attend._mLesson);
//                }
//                arrc = setMaxMin(arrc[0], arrc[1], student._compCredit);
//            }
//            if (0 < countT) {
//                final double avg = (float) totalT / (float) countT;
//                _avgHrTotalScore = new BigDecimal(avg);
//            }                
//            if (0 < countA) {
//                final double avg = (float) totalA / (float) countA;
//                _avgHrAverageScore = new BigDecimal(avg);
//            }
//            if (0 < mlesson) {
//                _perHrPresent = new BigDecimal((float) present / (float) mlesson * 100);
//                _perHrAbsent = new BigDecimal((float) absent / (float) mlesson * 100);
//            }
////            if (0 < arrc[0]) {
////                _HrCompCredits = arrc[0] + "単位";
////            }
////            if (0 < arrj[0]) {
////                _HrMLesson = arrj[0] + "日";
////            }
//        }

//        /**
//         * 科目の学年平均得点
//         * @param db2
//         * @throws SQLException
//         */
//        private void setSubclassGradeAverage(final DB2UDB db2, final Param param) {
//            final StringBuffer stb = new StringBuffer();
//            stb.append("SELECT ");
//            stb.append("    T2.ELECTDIV, ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
//            }
//            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("    T1.AVG ");
//            stb.append("FROM ");
//            stb.append("    RECORD_AVERAGE_DAT T1 ");
//            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
//            }
//            stb.append("        T2.SUBCLASSCD = ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
//            }
//            stb.append("        T1.SUBCLASSCD ");
//            stb.append("WHERE ");
//            stb.append("    T1.YEAR = '" + param._year + "'");
//            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
//            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testKindCd + "'");
//            stb.append("    AND T1.AVG_DIV = '" + param._avgDiv + "' ");
//            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
//            stb.append("    AND T1.SUBCLASSCD <> '" + param.SUBCLASSCD999999 + "' ");
//            if ("2".equals(param._avgDiv)) {
//                stb.append("    AND T1.HR_CLASS = '" + _hrclassCd.substring(2) + "' ");
//                stb.append("    ORDER BY HR_CLASS ");
//                
//            } else if ("3".equals(param._avgDiv)) {
//                final String[] coursecds = new String[_courses.size()];
//                for (int i = 0; i < _courses.size(); i++) {
//                    final Course course = (Course) _courses.get(i);
//                    coursecds[i] = course._coursecd;
//                }
//                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN " + SQLUtils.whereIn(true, coursecds) + " ");
//                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
//            } else if ("4".equals(param._avgDiv)) {
//                final String[] majorcds = new String[_courses.size()];
//                for (int i = 0; i < _courses.size(); i++) {
//                    final Course course = (Course) _courses.get(i);
//                    majorcds[i] = course._coursecd.substring(0, course._coursecd.length() - 4);
//                }
//                stb.append("    AND T1.COURSECD || T1.MAJORCD IN " + SQLUtils.whereIn(true, majorcds) + " ");
//                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD ");
//            }
//
//            final String sql = stb.toString();
//            log.debug(" gradeAverage sql = " + sql);
//            try {
//                PreparedStatement ps = db2.prepareStatement(sql);
//                
//                final ResultSet rs = ps.executeQuery();
//                
//                while (rs.next()) {
//                    final String subclassCd = rs.getString("SUBCLASSCD");
//                    final String electDiv = rs.getString("ELECTDIV");
//                    
//                    final SubClass subclass = (SubClass) _subclasses.get(electDiv + subclassCd);
//                    final BigDecimal subclassGradeAvg = rs.getBigDecimal("AVG");
//                    if (subclass == null || subclassGradeAvg == null) {
//                        //log.debug("subclass => " + subclass + " , gradeAvg => " + subclassGradeAvg);
//                        continue;
//                    }
//                    //log.debug("subclass => " + subclass._subclassabbv + " , gradeAvg => " + subclassGradeAvg);
//                    subclass._scoresubaverage = subclassGradeAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//                }
//            }catch (SQLException e) {
//                log.debug("exception!", e);
//            }
//        }
//        
//        /**
//         * 順位の算出
//         */
//        private List createRanking(final Param param) {
//            final List list = new LinkedList();
//            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
//                final Student student = (Student) itS.next();
//                student._total = new Total(param, student);
//                final Total total = student._total;
//                if (0 < total._count) {
//                    list.add(total);
//                }
//            }
//
//            Collections.sort(list);
//            return list;
//        }
//
//        private int rank(final Student student) {
//            final Total total = student._total;
//            if (0 >= total._count) {
//                return -1;
//            }
//            return 1 + _ranking.indexOf(total);
//        }
        
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
        final String _name;

        public TransInfo(
                final String date,
                final String name
        ) {
            _date = date;
            _name = name;
        }

        public String toString() {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _date) {
                sb.append(KNJ_EditDate.h_format_JP(_date));
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
        final AttendInfo _attendInfo = new AttendInfo();
//        private String _scoreSum;
//        private String _scoreAvg;
//        private int _classRank;
//        private int _rank;
        private final Map _scoreDetails = new TreeMap();
//        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
//        private int _qualifiedCredits;  // 今年度の認定単位数
//        private int _previousCredits;  // 前年度までの修得単位数
//        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者
        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠課時分
        private int _specialAbsent; // 特活欠課時数
        private String _attendSemesRemarkDatRemark1;

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
//
//        /**
//         * @return 成績不振者（評定１が1つでもある）は true を戻します。
//         */
//        public boolean isGradePoor() {
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                final ScoreValue scorevalue = detail._score;
//                if (null == scorevalue || !scorevalue.hasIntValue()) {
//                    continue;
//                }
//                if (scorevalue.getScoreAsInt() == 1) {
//                    return true;
//                }
//            }
//            return false;
//        }

        /**
         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
         */
        public boolean isAttendPerfect() {
            if (! _attendInfo.isAttendPerfect()) { return false; }
            
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final Double field = detail._isCombinedSubclass ? detail._replacedSick : detail._absent;
                if (null != field && 0 < field.doubleValue()) {
                    return false;
                }
            }            
            return true;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        public boolean isKekkaOver() {
            return null != getKekkaOverKamokuCount();
        }

        public String getKekkaOverKamokuCount() {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (detail._isOver) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public BigDecimal getTotalKekkaJisu() {
            BigDecimal rtn = new BigDecimal(0);
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final Double field = detail._isCombinedSubclass ? null : detail._absent;
                if (null != field) {
                    rtn = rtn.add(new BigDecimal(field.doubleValue()));
                }
            }
            return rtn;
        }

//        public String getKettenKamokuCount(final Param param) {
//            int count = 0;
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                if (ScoreDetail.isFail(param, detail)) {
//                    count += 1;
//                }
//            }
//            return count == 0 ? null : String.valueOf(count);
//        }
//
//        public String getKettenTanni(final Param param) {
//            int credit = 0;
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                if (ScoreDetail.isFail(param, detail) && null != detail._credits) {
//                    credit += detail._credits.intValue();
//                }
//            }
//            return credit == 0 ? null : String.valueOf(credit);
//        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class AttendInfo {
        int _lesson;
        int _mLesson;
        int _suspend;
        int _mourning;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transDays;
        BigDecimal _kekkaJisu = new BigDecimal(0);

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        public boolean isAttendPerfect() {
            if (_absent == 0 && _late == 0 && _early == 0) { return true; }
            return false;
        }
        
        public String toString() {
            return "Attendance(" + _lesson + ", " + _mLesson + ", " + _suspend + ", " + _mourning + ", " + _absent + ", " + _present + ", " + _late + ", " + _early + ")"; 
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
//        private String _scoreaverage;  // 学級平均
//        private String _scoresubaverage;  // 学年平均
//        private String _scoretotal;  // 学級合計
//        private String _scoreCount;  // 学級人数
//        private String _scoreMax;  // 最高点
//        private String _scoreMin;  // 最低点
//        private String _scoreFailCnt;  // 欠点者数
        

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
    }

    //--- 内部クラス -------------------------------------------------------
//    /**
//     * <<素点・評定データのクラスです>>。
//     */
//    private static class ScoreValue {
//        final String _strScore;
//
//        ScoreValue(final String strScore) {
//            _strScore = strScore;
//        }
//
//        /**
//         * 生徒別科目別の素点または評定のインスタンスを作成します。
//         * @param strScore 素点または評定
//         * @param classcd 教科コード
//         * @return ScoreValue。'総合的な学習の時間'は'null'を戻します。
//         */
//        private static ScoreValue create(
//                final String strScore,
//                final String classcd
//        ) {
//            if (null == strScore) return null;
//            // if (KNJDefineSchool.subject_T.equals(classcd.substring(1, 3))) return null;
//            return new ScoreValue(strScore);
//        }
//
//        public String getScore() { return _strScore; }
//        public boolean hasIntValue() { return !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore); }
//        public int getScoreAsInt() { return hasIntValue() ? Integer.parseInt(_strScore) : 0; }
//        
//        public String toString() {
//            return _strScore;
//        }
//    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
        final SubClass _subClass;
        boolean _isCombinedSubclass;
        Double _absent;
        Double _replacedSick;
        Integer _jisu;
//        final ScoreValue _score;
        final String _assessLevel;
//        final ScoreValue _karihyotei;
        final Integer _replacemoto;
        final String _print_flg;
        final String _score_flg;
        final Integer _compCredit;
        final Integer _getCredit;
        BigDecimal _absenceHigh;
        final Integer _credits;
        boolean _isOver;
//        final String _chaircd;
//        final String _slump;
//        final String _slumpMark;
//        final String _slumpScore;
        final String _passScore;

        ScoreDetail(
                final SubClass subClass,
//                final ScoreValue score,
                final String assessLevel,
//                final ScoreValue patternAssess,
//                final ScoreValue karihyotei,
                final Integer replacemoto,
                final String print_flg,
                final String score_flg,
//                final String slump,
//                final String slumpMark,
//                final String slumpScore,
                final String passScore,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits
//                final String chaircd
        ) {
            _subClass = subClass;
//            _score = score;
            _assessLevel = assessLevel;
            _replacemoto = replacemoto;
//            _karihyotei = karihyotei;
            _print_flg = print_flg;
            _score_flg = score_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
//            _chaircd = chaircd;
//            _slump = slump;
//            _slumpScore = slumpScore;
//            _slumpMark = slumpMark;
            _passScore = passScore;
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
//
//        private static boolean isFail(final Param param, final ScoreDetail detail) {
//            if (param._testKindCd != null && (param._testKindCd.endsWith("990008") || param._testKindCd.endsWith("09"))) {
//                return "1".equals(detail._score);
//            }
//            return "1".equals(detail._assessLevel);
//        }
//
//        public String toString() {
//            return (_subClass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _score_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isOver);
//        }
    }

//    //--- 内部クラス -------------------------------------------------------
//    /**
//     * <<生徒別総合成績データのクラスです>>。
//     */
//    private static class Total implements Comparable {
//        final int _total;  // 総合点
//        final int _count;  // 件数（成績）
//        final BigDecimal _avgBigDecimal;  // 平均点
//        final int _countFail;  //欠点科目数
//
//        /**
//         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
//         * @param student
//         */
//        Total(final Param param, final Student student) {
//            
//            int total = 0;
//            int count = 0;
//
//            int compCredit = 0;
//            int getCredit = 0;
//
//            int countFail = 0;
//            
//            for (final Iterator it = student._scoreDetails.values().iterator(); it.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) it.next();
//
//                final ScoreValue scoreValue = param._knjdobj.getTargetValue(detail);
//                if (isAddTotal(scoreValue, detail._replacemoto, param._knjdobj)) {
//                    if (null != scoreValue && scoreValue.hasIntValue()) {
//                        total += scoreValue.getScoreAsInt();
//                        count++;                    
//                    }
//                    if (ScoreDetail.isFail(param, detail)) {
//                        countFail++;
//                    }
//                }
//
//                final Integer c = detail.getCompCredit();
//                if (null != c) {
//                    compCredit += c.intValue();
//                }
//
//                final Integer g = detail.getGetCredit();
//                if (null != g) {
//                    getCredit += g.intValue();
//                }
//            }
//            
//            int specialAbsent = 0;
//            for (final Iterator it = student._spGroupAbsentMinutes.values().iterator(); it.hasNext();) {
//                final Integer groupAbsentMinutes = (Integer) it.next();
//                specialAbsent += getSpecialAttendExe(param, groupAbsentMinutes.intValue());
//            }
//
//            _total = total;
//            _count = count;
//            if (0 < count) {
//                final double avg = (float) total / (float) count;
//                _avgBigDecimal = new BigDecimal(avg);
//            } else {
//                _avgBigDecimal = null;
//            }
//            if (0 < compCredit) {
//                student._compCredit = compCredit;
//            }
//            if (0 < getCredit) {
//                student._getCredit = getCredit;
//            }
//            student._specialAbsent = specialAbsent;
//            _countFail = countFail;
//        }
//
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
//        
//        /**
//         * @param scoreValue
//         * @param replacemoto
//         * @param knjdObj
//         * @return 成績総合計に組み入れる場合Trueを戻します。
//         */
//        private boolean isAddTotal(
//                final ScoreValue scoreValue,
//                final Integer replacemoto,
//                final KNJD065_COMMON knjdObj
//        ) {
////            if (null == scoreValue || !scoreValue.hasIntValue()) { return false; }
//            if (knjdObj._isGakunenMatu && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
//            return true;
//        }
//        
//        /**
//         * {@inheritDoc}
//         */
//        public int compareTo(final Object o) {
//            if (!(o instanceof Total)) return -1;
//            final Total that = (Total) o;
//
//            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public boolean equals(final Object o) {
//            if (!(o instanceof Total)) return false;
//            final Total that = (Total) o;
//            return that._avgBigDecimal.equals(this._avgBigDecimal);
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public String toString() {
//            return _avgBigDecimal.toString();
//        }
//    }
    
    private static class Form {
        
        public boolean print(final Vrw32alp svf, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
            final List studentListList = getStudentListList(hrInfo._students, param._formMaxLine);
            for (final Iterator it = studentListList.iterator(); it.hasNext();) {
                final List studentList = (List) it.next();
                if (Form1.print(svf, param, hrInfo, studentList)) {
                    hasData = true;
                }
            }            return hasData;
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
                final boolean notOutputColumn = "90".equals(subClass._classcode) && param._notOutputSougou || CLASSCD_LHR.equals(subClass._classcode) && param._notOutputLhr;
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
        
        private static String bdZeroToNull(final BigDecimal bd, final boolean zeroToNull) {
            return null == bd || bd.doubleValue() == 0.0 && zeroToNull ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        private static String zeroToNull(final int num) {
            return num == 0 ? null : String.valueOf(num);
        }
        
        private static String nullToBlank(final Object o) {
            return null == o ? null : o.toString();
        }

        private static void svfVrAttribute_(final int div, final Vrw32alp svf, final String field, final int line, final String data) {
            svfVrAttribute_(div, svf, field, line, "", data);
        }

        /**
         * 
         * @param div 0:レコード、1:繰り返し
         * @param svf
         * @param field
         * @param line
         * @param data
         */
        private static void svfVrAttribute_(final int div, final Vrw32alp svf, final String field, final int line, final String pf, final String data) {
            if (1 == div) {
                svf.VrAttributen(field, line, data);
            } else {
                svf.VrAttribute(field + String.valueOf(line) + pf, data);
            }
        }
        
        private static void svfVrsOut_(final int div, final Vrw32alp svf, final String field, final int line, final String data) {
            svfVrsOut_(div, svf, field, line, "", data);
        }

        /**
         * 
         * @param div 0:レコード、1:繰り返し
         * @param svf
         * @param field
         * @param line
         * @param data
         */
        private static void svfVrsOut_(final int div, final Vrw32alp svf, final String field, final int line, final String pf, final String data) {
            if (1 == div) {
                svf.VrsOutn(field, line, data);
            } else {
                svf.VrsOut(field + String.valueOf(line) + pf, data);
            }
        }
        
        private static int gnumToLine(final Param param, final int gnum) {
            return param._formMaxLine == gnum ? param._formMaxLine : gnum % param._formMaxLine;
        }
        
        private static void printAttendInfo(final Vrw32alp svf, final Param param, final Student student, final AttendInfo attendInfo, final int line) {
            final int div = 0;
            svfVrsOut_(div, svf, "ITEM1_", line, bdZeroToNull(student.getTotalKekkaJisu(), true));      // 欠課数計
            svfVrsOut_(div, svf, "ITEM2_", line, bdZeroToNull(attendInfo._kekkaJisu, true));      // 担任入力
            svfVrsOut_(div, svf, "ITEM3_", line, bdZeroToNull(student.getTotalKekkaJisu().subtract(attendInfo._kekkaJisu), student.getTotalKekkaJisu().doubleValue() == 0));      // 差異

            svfVrsOut_(div, svf, "APPOINT_DAY", line, zeroToNull(attendInfo._lesson));      // 授業日数
            svfVrsOut_(div, svf, "PRESENT", line, zeroToNull(attendInfo._mLesson));      // 出席すべき日数
            svfVrsOut_(div, svf, "SUSMOUR_DAY", line, zeroToNull(attendInfo._suspend + attendInfo._mourning));      // 出席停止
//            svfVrsOut_(div, svf, null, line, zeroToNull(attendInfo._suspend));      // 出停
//            svfVrsOut_(div, svf, null, line, zeroToNull(attendInfo._mourning));      // 忌引
            svfVrsOut_(div, svf, "ABSENCE", line, zeroToNull(attendInfo._absent));       // 欠席日数
            svfVrsOut_(div, svf, "ATTEND", line, String.valueOf(attendInfo._present));      // 出席日数 (0は表示する)
            svfVrsOut_(div, svf, "LATE", line, zeroToNull(attendInfo._late));      // 遅刻回数
            svfVrsOut_(div, svf, "EARLY", line, zeroToNull(attendInfo._early));        // 早退回数
//            svfVrsOut_(div, svf, null, line, zeroToNull(attendInfo._transDays));        // 留学
        }
        
        private static class Form1 {
            
            private static boolean print(
                    final Vrw32alp svf,
                    final Param param,
                    final HRInfo hrInfo,
                    final List stulist
            ) {
                boolean hasData = false;
                final List subclassListList = getSubClassListList(param, hrInfo._subclasses.values(), param._formMaxColumn);

                for (int p = 0, pages = subclassListList.size(); p < pages; p++) {
                    final List subclassList = (List) subclassListList.get(p);
                    
                    svf.VrSetForm("KNJD615N.frm", 4);  //SVF-FORM設定
                    printHeader(svf, param, hrInfo);
                    printStudentsName(svf, param, stulist);  // 生徒名等を印字

                    if (p == pages - 1) {
                        // 生徒別総合成績および出欠を印字
                        for (final Iterator its = stulist.iterator(); its.hasNext();) {
                            final Student student = (Student) its.next();
                            final int line = gnumToLine(param, student._gnum);
//                            printStudentTotal(svf, param, student, line);
                            printAttendInfo(svf, param, student, student._attendInfo, line);
//                            printOnLastpage(svf, param, student, line);
                        }
                    }

                    for (int i = 0, size = subclassList.size(); i < size; i++) {

                        final SubClass subclass = (SubClass) subclassList.get(i);
                        log.debug("p=" + p + ", i=" + i + ", subclasscd=" + subclass._subclasscode + " " + subclass._subclassabbv);
                        printSubclasses(svf, param, subclass, stulist);
                        svf.VrEndRecord();
                        hasData = true;
                    }
                    for (int i = subclassList.size(); i < param._formMaxColumn; i++) {
                        svf.VrEndRecord();
                    }
                }
                return hasData;
            }

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
                svf.VrsOut("year2", nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");
                svf.VrsOut("ymd1", param.getNow()); // 作成日
                svf.VrsOut("DATE", param.getTermKekka());  // 欠課の集計範囲
                // svf.VrsOut("DATE2", _param.getTermAttend());  // 「出欠の記録」の集計範囲
                svf.VrsOut("DATE2", param.getTermKekka());
                
                svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrInfo._staffName) > 30 ? "2" : ""), hrInfo._staffName);  //担任名
                svf.VrsOut("COURSE", hrInfo._hrName);  //組名称
                svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
//                if (param._knjdobj._hasCompCredit) {
//                    svf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
//                }
//                svf.VrsOut("lesson20", hrInfo._HrMLesson);  // 授業日数
                
//                svf.VrsOut("ITEM7", param._rankName + "順位");
//                svf.VrsOut("ITEM4", param._knjdobj._item4Name);
//                svf.VrsOut("ITEM5", param._knjdobj._item5Name);
//                svf.VrsOut("ITEM6", param._knjdobj._item1Name + "・" + param._knjdobj._item2Name);
//                svf.VrsOut("ITEM8", param._rankName);

                svf.VrsOut("ITEM_NAME1", "欠　課　数　計");
                svf.VrsOut("ITEM_NAME2", "担　任　入　力");
                svf.VrsOut("ITEM_NAME3", "差　　異");

                // 一覧表枠外の文言
                svf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKAOVER);
                svf.VrsOut("NOTE1",  " " );
                svf.VrsOut("NOTE2",  "：欠課時数超過者" );
                svf.VrAttribute("NOTE3",  ATTRIBUTE_KETTEN);
                svf.VrsOut("NOTE3",  " " );
                svf.VrsOut("NOTE4",  "：欠点" );
                
//              if (_param._knjdobj instanceof KNJD065_GRADE) {
//                  svf.VrsOut("TITLE", _param._knjdobj._semesterName + "成績一覧表"); //成績名称
//                  svf.VrsOut("MARK1_2",  _param._assess.toString());
//              }
                
                svf.VrsOut("TITLE", param._knjdobj._title);
                svf.VrsOut("SUBTITLE", "（欠課時数と出欠の記録）");
                if (param._knjdobj._printPrgid) {
                    svf.VrsOut("PRGID", param._prgId);
                    svf.VrsOut("MARK", "/");
                }
                
                for (int i = 1; i <= 8; i++) {
                    final String name1 = (String) param._d055Name1Map.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    final String field = "JOB" + String.valueOf(i) + (getMS932ByteLength(name1) > 8  ? "_2" : "_1");
                    svf.VrsOut(field, name1);
                }
            }
            
//            /**
//             * 最後のページの印字処理（成績総合/出欠の記録）
//             * @param svf
//             */
//            private static void printOnLastpage(
//                    final Vrw32alp svf,
//                    final Param param,
//                    final Student student,
//                    final int line
//            ) {
//                final int div = 0;
//                if (param._knjdobj._hasCompCredit) {
//                    svfVrsOut_(div, svf, "COMP_CREDIT", line, zeroToNull(student._compCredit)); //今年度履修単位数
//                    svfVrsOut_(div, svf, "GET_CREDIT", line, zeroToNull(student._getCredit)); //今年度修得単位数
//                }
//                if (param._knjdobj._hasJudgementItem) { // 前年度までの単位数を印字
//                    // 各単位数、未履修科目数を印字
//                    svfVrsOut_(div, svf, "A_CREDIT", line, zeroToNull(student._qualifiedCredits)); // 今年度認定単位数
//                    svfVrsOut_(div, svf, "PRE_C_CREDIT", line, zeroToNull(student._previousCredits)); // 前年度までの修得単位数
//                    final int t = student._getCredit + student._qualifiedCredits + student._previousCredits; // 修得単位数計
//                    if (t != 0) {
//                        final String at;
//                        if (param._gradCredits != 0 && param._gradCredits <= t) {
//                            at = "@";
//                        } else {
//                            at = "";
//                        }
//                        svfVrsOut_(div, svf, "TOTAL_C_CREDIT", line, at + String.valueOf(t));
//                    }
//                    svfVrsOut_(div, svf, "PRE_N_CREDIT", line, zeroToNull(student._previousMirisyu)); // 前年度までの未履修科目数
//                }
//                if (0 < student._specialAbsent) {
//                    svfVrsOut_(div, svf, "SP_KEKKA", line, zeroToNull(student._specialAbsent));
//                }
//            }
            
//            /**
//             * 生徒別総合点・平均点・順位を印刷します。
//             * @param svf
//             * @param gnum 行番号(印字位置)
//             */
//            private static void printStudentTotal(
//                    final Vrw32alp svf,
//                    final Param param,
//                    final Student student,
//                    final int line
//            ) {
//                final int div = 0;
//                if (null != student._scoreSum) {
//                    svfVrsOut_(div, svf, "TOTAL", line, student._scoreSum);  //総合点
//                    svfVrsOut_(div, svf, "AVERAGE", line, student._scoreAvg);  //平均点
//                }
//                //順位（学級）
//                if (1 <= student._classRank) {
//                    svfVrsOut_(div, svf, "CLASS_RANK", line, String.valueOf(student._classRank));
//                }
//                //順位（学年orコース）
//                if (1 <= student._rank) {
//                    svfVrsOut_(div, svf, "RANK", line, String.valueOf(student._rank));
//                }
//                //欠点科目数
//                if (0 < student._total._countFail) {
//                    svfVrsOut_(div, svf, "FAIL", line, String.valueOf(student._total._countFail));
//                }
//            }
            
            /**
             * 生徒の氏名・備考を印字
             * @param svf
             * @param hrInfo
             * @param stulist：List hrInfo._studentsのsublist
             */
            private static void printStudentsName(
                    final Vrw32alp svf,
                    final Param param,
                    final List stulist
            ) {
                final int div = 0;
                for (final Iterator it = stulist.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    final int line = gnumToLine(param, student._gnum);
                    svf.VrsOutn("NUMBER", line, DEC_FMT2.format(Integer.parseInt(student._attendNo)));  // 出席番号
                    svf.VrsOut("name" + String.valueOf(line), student._name);    // 氏名
                    String remark = "";
                    if (null != student._attendSemesRemarkDatRemark1) {
                        remark += student._attendSemesRemarkDatRemark1 + " ";
                    }
                    remark += student._transInfo.toString();  // 備考

                    final int keta = getMS932ByteLength(remark);
                    String pf = "";
                    if (keta <= 70) {
                    } else {
                        pf = "_2";
                    }
                    svfVrsOut_(div, svf, "REMARK", line, pf, remark);  // 備考
//                    if (param._knjdobj._hasJudgementItem) {
//                        // 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
//                        if (student._isGradePoor) { svf.VrsOutn("CHECK1", student._gnum, "★"); }
//                        if (student._isGradeGood) { svf.VrsOutn("CHECK1", student._gnum, "☆"); } 
//                        if (student._isAttendPerfect) { svf.VrsOutn("CHECK2", student._gnum, "○"); }
//                        if (student._isKekkaOver) { svf.VrsOutn("CHECK2", student._gnum, "●"); } 
//                    }
                }
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
                    final SubClass subclass,
                    final List stulist
            ) {
                //教科名
                svf.VrsOut("course1", subclass._classabbv);
                //科目名
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
                //単位数
                if (0 != subclass._maxcredit) {
                    if (subclass._maxcredit == subclass._mincredit) {
                        svf.VrsOut("credit1",  String.valueOf(subclass._maxcredit));
                    } else  {
                        svf.VrsOut("credit1",  String.valueOf(subclass._mincredit) + " " + Param.FROM_TO_MARK + " " + String.valueOf(subclass._maxcredit));
                    }
                }
                if (getMS932ByteLength(subclass._staffname) > 4) {
                    svf.VrsOut("CLASS_TEACHER2", subclass._staffname);
                } else {
                    svf.VrsOut("CLASS_TEACHER", subclass._staffname);
                }
                //授業時数
                if (0 != subclass._jisu) { 
                    svf.VrsOut("APPOINT_TIMES",  String.valueOf(subclass._jisu));
                }
//                //学級平均・合計
//                svf.VrsOut("AVE_CLASS", subclass._scoreaverage);
//                svf.VrsOut("AVE_SUBCLASS", subclass._scoresubaverage);
//                svf.VrsOut("TOTAL_SUBCLASS", StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
//                svf.VrsOut("MAX_SCORE", subclass._scoreMax);
//                svf.VrsOut("MIN_SCORE", subclass._scoreMin);
//                svf.VrsOut("FAIL_STD", subclass._scoreFailCnt);
                //項目名
                svf.VrsOut("ITEM2", param._knjdobj._item2Name);
                
                for (final Iterator it = stulist.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                        final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                        printDetail(svf, param, detail, gnumToLine(param, student._gnum));
                    }
                }
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
                    final int line
            ) {
                final int div = 0;
                // 欠課
                final Double chkField;
                if (detail._isCombinedSubclass) {
                    chkField = new Double(detail._replacedSick.doubleValue() + (null == detail._absent ? 0.0 : detail._absent.doubleValue()));
                } else {
                    chkField = detail._absent;
                }
                if (null != chkField) {
                    final int value = (int) Math.round(chkField.doubleValue() * 10.0);
                    final String field;
                    String pf = "";
                    field = "SCORE";
                    if (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) {
                        pf = "_2";
                    }
                    if (0 != value) {
                        if (detail._isOver) { svfVrAttribute_(div, svf, field, line, pf, ATTRIBUTE_KEKKAOVER); }
                        svfVrsOut_(div, svf, field, line, pf, param.getAbsentFmt().format(chkField.floatValue()));
                        if (detail._isOver) { svfVrAttribute_(div, svf, field, line, pf, ATTRIBUTE_NORMAL); }
                    }
                }
            }
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
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
        final String[] _classSelected;
        /** 出欠集計日付 */
        final String _date;
        final String _sDate;
        final String _testKindCd;

//        /** 総合順位出力 1:学級 2:学年 3:コース 4:学科 */
//        final String _outputRank;
//        /** 順位の基準点 1:総合点 2:平均点 */
//        final String _outputKijun;
//        /** 欠点 */
//        final String _ketten;
        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
        final String _checkKettenDiv;
        /** 起動元のプログラムＩＤ */
        final String _prgId;
//        /** 成績優良者評定平均の基準値===>KNJD615：未使用 */
//        final Float _assess;

        /** フォーム（1:４５名、2:５０名）*/
//        final String _formSelect;
        final int _formMaxLine;
        
        /** 科目数　（1:15科目、2:20科目) */
//        final String _subclassMax;
        final int _formMaxColumn;
        
//        /** 単位保留 */
//        final boolean _creditDrop;
//        /** 欠番を詰める */
//        final boolean _noKetuban;
//        /** 同一クラスでのコース毎に改頁あり */
//        final boolean _outputCourse;
        /** "総合的な学習の時間"を表示しない */
        final boolean _notOutputSougou;
        /** ＬＨＲを表示しない */
        final boolean _notOutputLhr;
        final String _schoolName;
        
        private String FORM_FILE;

        private String _yearDateS;
//        private String _divideAttendDate;
//        private String _divideAttendMonth;
        private String _semesMonth;
        private String _semesterName;
        private String _semesterDateS;
        private String _testitemname;
        
        private static final String FROM_TO_MARK = "\uFF5E";
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        
        /** 端数計算共通メソッド引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private final String SSEMESTER = "1";
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        
        private KNJD065_COMMON _knjdobj;  //成績別処理のクラス
        private KNJSchoolMst _knjSchoolMst;

        private KNJ_Get_Info _getinfo;
        
//        private int _gradCredits;  // 卒業認定単位数
        
//        final boolean _isKumamoto;
//        final boolean _isKyoto;
//        final boolean _isHosei;

//        private String _rankName;
//        private String _rankFieldName;
//        private String _avgDiv;
//        private String _d054Namecd2Max;
//        private String _sidouHyoji;
        private Map _d055Name1Map;
        
        private final String SUBCLASSCD999999;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _sDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
//            _outputRank = request.getParameter("OUTPUT_RANK");
//            _creditDrop = (request.getParameter("OUTPUT4") != null);
//            _noKetuban = (request.getParameter("OUTPUT5") != null );
//            _outputCourse = (request.getParameter("OUTPUT5") != null);
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT5"));
            _notOutputLhr = "1".equals(request.getParameter("OUTPUT6"));
//            _assess = (request.getParameter("ASSESS") != null) ? new Float(request.getParameter("ASSESS1")) : new Float(4.3);
//            _formSelect = request.getParameter("FORM_SELECT");
//            _subclassMax = request.getParameter("SUBCLASS_MAX");
            SUBCLASSCD999999 = "999999";
            _formMaxLine = 45;
            _formMaxColumn = 25;
//            _outputKijun = request.getParameter("OUTPUT_KIJUN");
//            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _prgId = request.getParameter("PRGID");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _getinfo = new KNJ_Get_Info();
            _schoolName = getSchoolName(db2);
//            _isKumamoto = "kumamoto".equals(_schoolName);
//            _isKyoto = "kyoto".equals(_schoolName);
//            _isHosei = "HOUSEI".equals(_schoolName);
            loadAttendSemesArgument(db2);
//            setD054Namecd2Max(db2);
            setD055Name1(db2);
            _definecode = createDefineCode(db2);
//            _gradCredits = getGradCredits(db2);
            log.fatal(_knjdobj);
//            final KNJDivideAttendDate obj = new KNJDivideAttendDate();
//            obj.getDivideAttendDate(db2, _year, _semester, _date);
//            _divideAttendDate = obj.date;  //最終集計日の翌日
//            _divideAttendMonth = obj.month; //最終集計学期＋月
//            _semesMonth = KNJC053_BASE.retSemesterMonthValue(_divideAttendMonth);
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = returnval.val1;  //学期名称
            // 学期期間FROM
            if (null == returnval.val2) {
                _semesterDateS = _year + "-04-01";
            } else {
                _semesterDateS = returnval.val2;
            }
            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, "9");
            _yearDateS = returnval1.val2;
            // テスト名称
            _testitemname = getTestItem(db2, _year, _semester, _testKindCd);
            _knjdobj = createKnjd065Obj(db2);  //成績別処理クラスを設定するメソッド
            
//            if (OUTPUT_RANK1.equals(_outputRank)) {
//                _rankName = "学級";
//                if (OUTPUT_KJUN2.equals(_outputKijun)) {
//                    _rankFieldName = "CLASS_AVG_RANK";
//                } else {
//                    _rankFieldName = "CLASS_RANK";
//                }
//                _avgDiv = "2";
//            } else if (OUTPUT_RANK2.equals(_outputRank)) {
//                _rankName = "学年";
//                if (OUTPUT_KJUN2.equals(_outputKijun)) {
//                    _rankFieldName = "GRADE_AVG_RANK";
//                } else {
//                    _rankFieldName = "GRADE_RANK";
//                }
//                _avgDiv = "1";
//            } else if (OUTPUT_RANK3.equals(_outputRank)) {
//                _rankName = "コース";
//                if (OUTPUT_KJUN2.equals(_outputKijun)) {
//                    _rankFieldName = "COURSE_AVG_RANK";
//                } else {
//                    _rankFieldName = "COURSE_RANK";
//                }
//                _avgDiv = "3";
//            } else if (OUTPUT_RANK4.equals(_outputRank)) {
//                _rankName = "学科";
//                if (OUTPUT_KJUN2.equals(_outputKijun)) {
//                    _rankFieldName = "MAJOR_AVG_RANK";
//                } else {
//                    _rankFieldName = "MAJOR_RANK";
//                }
//                _avgDiv = "4";
//            }
//            log.debug("順位名称=" + _rankName);
        }
        
        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }
        
        /** 欠課の集計範囲 */
        public String getTermKekka() {
            return KNJ_EditDate.h_format_JP(_sDate) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(_date);
        }
        
//        /** 「出欠の記録」の集計範囲 */
//        public String getTermAttend() {
//            return KNJ_EditDate.h_format_JP(_semesterDateS) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(_date);
//        }

//        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
//        public boolean isRecordSlump() {
//            return "1".equals(_checkKettenDiv) && !"9".equals(_semester);
//        }
//
//        /** 欠点対象：満点マスタ(PERFECT_RECORD_DAT)の合格点(PASS_SCORE)を参照して判断するか */
//        public boolean isPerfectRecord() {
//            return "2".equals(_checkKettenDiv);
//        }
//
//        /** 欠点対象：指示画面の欠点を参照して判断するか */
//        public boolean isKetten() {
//            return !isRecordSlump() && !isPerfectRecord();
//        }
        
        /**
         *  成績別処理クラス設定
         */
        private KNJD065_COMMON createKnjd065Obj(
                final DB2UDB db2
        ) {
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testitemname);
            final KNJD065_COMMON common = new KNJD065_COMMON();
            if ("99900".equals(_semester + _testKindCd)) {
//                common._creditDrop = _creditDrop;
                common._hasJudgementItem = false;
                common._failValue = 30;
                common._printPrgid = true;

                common._item1Name = "評定";
                common._item2Name = "欠課";
                common._item4Name = "評定総合点";
                common._item5Name = "評定平均点";
                common._form2Item4Name = "評定合計";
                common._form2Item5Name = "評定平均";
                common._isGakunenMatu = true;
                common._hasCompCredit = true;
//                common._enablePringFlg = false;
                common._isPrintDetailTani = true;
                common._title = _testitemname + "  成績一覧表（評定）";
                
            } else if ("9900".equals(_testKindCd)) {
//                common._creditDrop = _creditDrop;
                common._hasJudgementItem = false;
                common._failValue = 30;
                common._printPrgid = true;

//                common._enablePringFlg = false;
                common._isPrintDetailTani = true;
                common._item1Name = "評価";
                common._item2Name = "欠課";
                common._item4Name = "評価総合点";
                common._item5Name = "評価平均点";
                common._form2Item4Name = "評価合計";
                common._form2Item5Name = "評価平均";
                common._isGakunenMatu = false;
                common._hasCompCredit = false;
                common._title = _semesterName + " " + _testitemname + " 成績一覧表";
            } else {
                common._isGakunenMatu = false;
                common._hasCompCredit = false;
                common._hasJudgementItem = false;
//                common._enablePringFlg = false;
                common._isPrintDetailTani = false;
                common._failValue = 30;
                common._printPrgid = true;

                common._item1Name = "素点";
                common._item2Name = "欠課";
                common._item4Name = "総合点";
                common._item5Name = "平均点";
                common._form2Item4Name = common._item4Name;
                common._form2Item5Name = common._item5Name;
//                common._creditDrop = false;
                common._title = _semesterName + " " + _testitemname + " 成績一覧表";
                
            }
            return common;
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
        
        private String getTestItem(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) {
            String testitemname = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW "
                                 +  "WHERE YEAR = '" + year + "' "
                                 +    "AND SEMESTER = '" + semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD = '" + testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemname;
        }
        
        private String getSemesterMonth(final DB2UDB db2, final String date) {
            String rtn = "999";
            if (null == date) {
                return rtn;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' AND '" + date + "' BETWEEN SDATE AND EDATE ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SEMESTER");
                }
            } catch (SQLException ex) {
                log.debug("getSemesterMonth exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            final int month = cal.get(Calendar.MONTH) + 1;
            if (NumberUtils.isDigits(rtn) && month <= 3) {
                rtn = String.valueOf(Integer.parseInt(rtn) + 1);
            }
            return rtn + new DecimalFormat("00").format(month);
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
//            } catch (SQLException ex) {
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
            } catch (SQLException ex) {
                log.debug("getD055 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2, final String year) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
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
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private void loadAttendSemesArgument(DB2UDB db2) {
            
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2, _year);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
            
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
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
        
//        // 卒業認定単位数の取得
//        private int getGradCredits(
//                final DB2UDB db2
//        ) {
//            int gradcredits = 0;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = " SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    gradcredits = rs.getInt("GRAD_CREDITS");
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return gradcredits;
//        }
    }
}
