// kanji=漢字
/*
 * $Id: 24b26131bd93cb048e844c0d217403d4e17dbb48 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 成績一覧表 を印刷します。
 * @author nakamoto
 * @version $Id: 24b26131bd93cb048e844c0d217403d4e17dbb48 $
 */
public class KNJD616V {
    private static final Log log = LogFactory.getLog(KNJD616V.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_02 = "02";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";

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

            final IPdf ipdf = new SvfPdf(svf);

            response.setContentType("application/pdf");

            printMain(db2, param, ipdf);

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
            final IPdf ipdf
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
            if (form.print(db2, ipdf, param, hrInfo)) {
                _hasData = true;
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

        private static List createCourses(final DB2UDB db2, final Param param, final String gradeHrclass) {
            final List rtn = new ArrayList();

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
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + gradeHrclass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE, ");
            stb.append("     L1.COURSECODENAME ");

            final String sql = stb.toString();

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String name = KnjDbUtils.getString(row, "COURSECODENAME");

                final Course course = new Course(
                        grade,
                        hrclass,
                        coursecd,
                        name
                );

                rtn.add(course);
            }
            return rtn;
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
        private Map _studentMap = Collections.EMPTY_MAP;
        private Set _courseSet = new HashSet();
        private Set _majorSet = new HashSet();
        private final Map _subclasses = new TreeMap();
//        private List _ranking;
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
//            loadAttend(db2, param);
            loadScoreDetail(db2, param);
//            _ranking = createRanking(param);
//            log.debug("RANK:" + _ranking);
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
            _students = new LinkedList();
            _studentMap = new HashMap();
            try {

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
                stb.append("    AND W1.GRADE||W1.HR_CLASS = '" + _hrclassCd + "' ");
                stb.append("ORDER BY W1.ATTENDNO");

                final String sql = stb.toString();

                int gnum = 0;
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
//                    if (_param._noKetuban) {
//                        gnum++;
//                    } else {
                        gnum = KnjDbUtils.getInt(row, "ATTENDNO", new Integer(-1)).intValue();
//                    }
                    final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), this, gnum);
                    _students.add(student);
                    _studentMap.put(student._schregno, student);
                    _courseSet.add(KnjDbUtils.getString(row, "COURSE"));
                    _majorSet.add(KnjDbUtils.getString(row, "MAJOR"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
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
                stb.append("LEFT   JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
                stb.append("LEFT   JOIN SCHREG_ENT_GRD_HIST_DAT W4 ON W4.SCHREGNO = W1.SCHREGNO ");
                stb.append("                              AND W4.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
                stb.append("                              AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE <= W2.EDATE) ");
                stb.append("                                OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE >= W2.SDATE)) ");
                stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
                stb.append("                                  AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
                stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
                stb.append("    AND W1.SCHREGNO = ? ");
                if (!SEMEALL.equals(param._semester)) {
                    stb.append("AND W1.SEMESTER = '" + param._semester + "' ");
                } else {
                    stb.append("AND W1.SEMESTER = '" + param._semeFlg + "' ");
                }

                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    try {
                        for (final Iterator qit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); qit.hasNext();) {
                        	final Map row = (Map) qit.next();
                            TransInfo transInfo = null;
                            final String d1 = KnjDbUtils.getString(row, "KBN_DATE1");
                            final String d2 = KnjDbUtils.getString(row, "KBN_DATE2");
                            if (null != d1) {
                                final String n1 = KnjDbUtils.getString(row, "KBN_NAME1");
                                transInfo = new TransInfo(d1, n1);
                            } else if (null != d2) {
                                final String n2 = KnjDbUtils.getString(row, "KBN_NAME2");
                                transInfo = new TransInfo(d2, n2);
                            }
                            if (null == transInfo) {
                                transInfo = new TransInfo(null, null);
                            }
                            student._attendNo = KnjDbUtils.getString(row, "ATTENDNO");
                            student._name = KnjDbUtils.getString(row, "NAME");
                            student._transInfo = transInfo;
                        }
                    } catch (Exception e) {
                        log.error("Exception", e);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        private Student getStudent(String code) {
            if (code == null) {
                return null;
            }
            return (Student) _studentMap.get(code);
        }

//        private void loadAttend(
//                final DB2UDB db2,
//                final Param param
//        ) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String psKey = "ATTENDSEMES";
//                if (null == param._psMap.get(psKey)) {
//                    param._attendParamMap.put("schregno", "?");
//                    final String sql = AttendAccumulate.getAttendSemesSql(
//                            param._year,
//                            param._semester,
//                            param._sdate,
//                            param._date,
//                            param._attendParamMap
//                    );
//                    log.debug(" sql = " + sql);
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
//                        final AttendInfo attendInfo = new AttendInfo(
//                                rs.getInt("LESSON"),
//                                rs.getInt("MLESSON"),
//                                rs.getInt("SUSPEND"),
//                                rs.getInt("MOURNING"),
//                                rs.getInt("SICK"),
//                                rs.getInt("PRESENT"),
//                                rs.getInt("LATE"),
//                                rs.getInt("EARLY"),
//                                rs.getInt("TRANSFER_DATE")
//                        );
//                        student._attendInfo = attendInfo;
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

        private String[] toArray(final Set set) {
            final List list = new ArrayList(set);
            final String[] arr = new String[set.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String) list.get(i);
            }
            return arr;
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param param
        ) {
            try {
                final String psKey = "SCORE_DETAIL";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlStdSubclassDetail(param);
                    log.debug(" subclass detail sql = " + sql);

                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                PreparedStatement ps = (PreparedStatement) param._psMap.get(psKey);

                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {_hrclassCd.substring(0,2), _hrclassCd.substring(2)}).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    if (param._enablePringFlg && "1".equals(KnjDbUtils.getString(row, "PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String classCd = subclassCd == null ? "" : subclassCd.substring(1, 3);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        final String slumpMark = KnjDbUtils.getString(row, "SLUMP_MARK");
                        final String slumpScore = KnjDbUtils.getString(row, "SLUMP_SCORE");
                        final String slumpRemark = KnjDbUtils.getString(row, "SLUMP_REMARK");
                        if (null == slumpMark && null == slumpScore && null == slumpRemark) {
                        	continue;
                        }
						final ScoreDetail scoreDetail = new ScoreDetail(
                                getSubClass(row, _subclasses),
                                KnjDbUtils.getString(row, "SCORE"),
                                KnjDbUtils.getString(row, "SCORE_DI"),
                                KnjDbUtils.getString(row, "ASSESS_LEVEL"),
                                KnjDbUtils.getString(row, "KARI_HYOUTEI"),
                                KnjDbUtils.getString(row, "PROV_FLG"),
                                KnjDbUtils.getInt(row, "REPLACEMOTO", new Integer(0)),
                                KnjDbUtils.getString(row, "PRINT_FLG"),
                                KnjDbUtils.getString(row, "SLUMP"),
                                slumpMark,
                                slumpScore,
                                slumpRemark,
                                KnjDbUtils.getInt(row, "COMP_CREDIT", new Integer(0)),
                                KnjDbUtils.getInt(row, "GET_CREDIT", new Integer(0)),
                                KnjDbUtils.getInt(row, "CREDITS", new Integer(0))
                        );
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
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
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
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
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE AS KARI_HYOUTEI ");
            stb.append("     , T2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT T2 ON T2.YEAR = W3.YEAR ");
            stb.append("            AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("            AND T2.SCHREGNO = W3.SCHREGNO ");
            stb.append("            AND T2.PROV_FLG = '1' ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd.substring(0, 4) + "' AND ");
            stb.append("            W3.SCORE_DIV = '" + SCORE_DIV_09 + "' ");
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
            stb.append("        W3.SCORE END AS SLUMP_SCORE, ");
            stb.append("        W3.REMARK AS SLUMP_REMARK ");
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

            stb.append(" ,CHAIR_A3 AS ( ");
            stb.append("     SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, ");
            }
            stb.append("             W3.SUBCLASSCD, ");
            stb.append("             MIN(W33.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W3");
            stb.append("     LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W3.SCHREGNO AND WA2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND WA2.CLASSCD = W3.CLASSCD AND WA2.SCHOOL_KIND = W3.SCHOOL_KIND AND WA2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CHAIR_STF W33 ON W33.SEMESTER = W3.SEMESTER ");
            stb.append("         AND W33.CHAIRCD = W3.CHAIRCD ");
            stb.append(" WHERE WA2.STAFFCD IS NOT NULL AND W33.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND W3.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W3.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, ");
            }
            stb.append("              W3.SUBCLASSCD");
            stb.append(" ) ");
            stb.append(" ,CHAIR_A4 AS ( ");
            stb.append("     SELECT  W4.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ");
            }
            stb.append("             W4.SUBCLASSCD, ");
            stb.append("             MIN(W44.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W4");
            stb.append("       LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W4.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND WA2.CLASSCD = W4.CLASSCD AND WA2.SCHOOL_KIND = W4.SCHOOL_KIND AND WA2.CURRICULUM_CD = W4.CURRICULUM_CD ");
            }
            stb.append("         AND WA2.SUBCLASSCD = W4.SUBCLASSCD ");
            stb.append("       LEFT JOIN CHAIR_A3 WA3 ON WA3.SCHREGNO = W4.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND WA3.CLASSCD = W4.CLASSCD AND WA3.SCHOOL_KIND = W4.SCHOOL_KIND AND WA3.CURRICULUM_CD = W4.CURRICULUM_CD ");
            }
            stb.append("         AND WA3.SUBCLASSCD = W4.SUBCLASSCD ");
            stb.append("       LEFT JOIN CHAIR_STF W44 ON W44.SEMESTER = W4.SEMESTER ");
            stb.append("         AND W44.CHAIRCD = W4.CHAIRCD ");
            stb.append("     WHERE WA2.STAFFCD IS NOT NULL AND W44.STAFFCD <> WA3.STAFFCD AND W44.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" AND   W4.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W4.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ");
            }
            stb.append("              W4.SUBCLASSCD");
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
            stb.append(" SELECT  value(SUBM.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T3.SCORE AS SCORE ");
            stb.append("        ,W4.VALUE_DI AS SCORE_DI ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,KARIHYO.KARI_HYOUTEI ");
            stb.append("        ,KARIHYO.PROV_FLG ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,case when PERF.DIV IS NULL then 100 else PERF.PERFECT end as PERFECT ");
            stb.append("        ,SUBM.SUBCLASSABBV ");
            stb.append("        ,SUBM.SUBCLASSNAME ");
            stb.append("        ,CM.CLASSABBV AS CLASSNAME ");
            stb.append("        ,SUBM.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_SCORE ");
            stb.append("        ,K1.SLUMP_REMARK ");

            stb.append("    , STFM.STAFFNAME ");
            stb.append("    , STFM2.STAFFNAME AS STAFF2 ");
            stb.append("    , STFM3.STAFFNAME AS STAFF3 ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI KARIHYO ON KARIHYO.SUBCLASSCD = T1.SUBCLASSCD AND KARIHYO.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append(" LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append(" LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST CM ON CM.CLASSCD || '-' || CM.SCHOOL_KIND = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND SDET.CLASSCD || '-' || SDET.SCHOOL_KIND || '-' || SDET.CURRICULUM_CD || '-' || ");
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT W4 ON W4.YEAR = '" + param._year + "' AND W4.SEMESTER = '" + param._semester + "' AND W4.TESTKINDCD || W4.TESTITEMCD || W4.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("     AND W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || W4.SUBCLASSCD = T1.SUBCLASSCD AND W4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = '" + param._year + "' AND PERF.SEMESTER = '" + param._semester + "' AND PERF.TESTKINDCD || PERF.TESTITEMCD = '" + param._testKindCd.substring(0, 4) + "' ");
            stb.append("     AND PERF.CLASSCD || '-' || PERF.SCHOOL_KIND || '-' || PERF.CURRICULUM_CD || '-' || PERF.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE SCH.GRADE END ");
            stb.append("     AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE SCH.COURSECD || SCH.MAJORCD || SCH.COURSECODE END ");
            stb.append(" LEFT JOIN CHAIR_A3 CA3 ON CA3.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND CA3.CLASSCD = T1.CLASSCD AND CA3.SCHOOL_KIND = T1.SCHOOL_KIND AND CA3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     AND CA3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = CA3.STAFFCD ");
            stb.append(" LEFT JOIN CHAIR_A4 CA4 ON CA4.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND CA4.CLASSCD = T1.CLASSCD AND CA4.SCHOOL_KIND = T1.SCHOOL_KIND AND CA4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     AND CA4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN STAFF_MST STFM3 ON STFM3.STAFFCD = CA4.STAFFCD ");
            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubClass(
                final Map row,
                final Map subclasses
        ) {
            String subclasscode = null;
            int credit = 0;
            int perfect = 0;
            try {
                subclasscode = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (KnjDbUtils.getString(row, "CREDITS") != null) { credit = KnjDbUtils.getInt(row, "CREDITS", new Integer(1)).intValue(); }
                if (KnjDbUtils.getString(row, "PERFECT") != null) { perfect = KnjDbUtils.getInt(row, "PERFECT", new Integer(1)).intValue(); }
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
                List otherstafflist = new ArrayList();
                boolean electdiv = false;
                try {
                    classabbv = KnjDbUtils.getString(row, "CLASSNAME");
                    subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                    subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                    staffname = KnjDbUtils.getString(row, "STAFFNAME");
                    if ("1".equals(KnjDbUtils.getString(row, "ELECTDIV"))) {
                        electdiv = true;
                    }
                    if (!"".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "STAFF2")))) {
                        otherstafflist.add(KnjDbUtils.getString(row, "STAFF2"));
                    }
                    if (!"".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "STAFF3")))) {
                        otherstafflist.add(KnjDbUtils.getString(row, "STAFF3"));
                    }
                } catch (Exception e) {
                     log.error("Exception", e);
                }
                subclass = new SubClass(subclasscode, classabbv, subclassabbv, subclassname, electdiv, credit,  perfect, staffname, otherstafflist);
                subclasses.put(subclasscode, subclass);
            } else {
                subclass = (SubClass) subclasses.get(subclasscode);
                int[] maxMin = setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = maxMin[0];
                subclass._mincredit = maxMin[1];
//                int[] maxMinP = setMaxMin(subclass._maxperfect, subclass._minperfect, perfect);
//                subclass._maxperfect = maxMinP[0];
//                subclass._minperfect = maxMinP[1];
                if (0 != credit) {
                    if (subclass._maxcredit < credit) subclass._maxcredit = credit;
                    if (credit < subclass._mincredit || 0 == subclass._mincredit) subclass._mincredit = credit;
                }
            }
            return subclass;
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

//        private int rank(final Student student) {
//            final Total total = student._total;
//            if (0 >= total._count) {
//                return -1;
//            }
//            return 1 + _ranking.indexOf(total);
//        }

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

        public String toString(final DB2UDB db2) {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _date) {
                sb.append(KNJ_EditDate.h_format_JP(db2, _date));
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
        private final Map _scoreDetails = new TreeMap();
//        private int _compCredit;  // 今年度の履修単位数
//        private int _getCredit;  // 今年度の修得単位数
//        private int _qualifiedCredits;  // 今年度の認定単位数
//        private int _previousCredits;  // 前年度までの修得単位数
//        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
//        private boolean _isGradePoor;  // 成績不振者
//        private boolean _isAttendPerfect;  // 皆勤者
//        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者

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
//         * @return 欠課超過が1科目でもあるなら true を戻します。
//         */
//        public boolean isKekkaOver(final Param param) {
//            return null != getKekkaOverKamokuCount(param);
//        }

//        public String getKekkaOverKamokuCount(final Param param) {
//            int count = 0;
//            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
//                final ScoreDetail detail = (ScoreDetail) itD.next();
//                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
//                    continue;
//                } else if (param._isNoPrintMoto && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
//                    continue;
//                }
//                if (detail._isOver) {
//                    count += 1;
//                }
//            }
//            return count == 0 ? null : String.valueOf(count);
//        }

        public String getRemark(final DB2UDB db2, final Param param, final List printSubclassList) {
            String remark = "";
            String comma = "";
            for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
            	final SubClass subclass = (SubClass) it.next();
                final ScoreDetail detail = (ScoreDetail) _scoreDetails.get(subclass._subclasscode);
                if (null != detail && null != detail._slumpRemark) {
                	remark += comma + StringUtils.defaultString(subclass._subclassname) + " " + StringUtils.defaultString(detail._slumpRemark);
                	comma = "、";
                }
            }
            final String transInfo = _transInfo.toString(db2);
            if (!StringUtils.isBlank(transInfo)) {
            	remark += comma + transInfo;  // 備考
            }
            return remark;
        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : _attendNo;
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
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        final String _staffname;  // 科目担当者名
        private String _scoresubaverage;  // 学年平均
        private List _otherstafflist; //他の先生(MAX2人)


        SubClass(
                final String subclasscode,
                final String classabbv,
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final int credit,
                final int perfect,
                final String staffname,
                final List otherstafflist
        ) {
            _classabbv = classabbv;
            _classcode = subclasscode.substring(1, 3);
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _staffname = staffname;
            _otherstafflist = otherstafflist;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }
        
        public String getPrintCredit(final Param param) {
        	final StringBuffer stb = new StringBuffer();
            if (0 != _maxcredit) {
            	if (_maxcredit == _mincredit) {
            		stb.append(_maxcredit);
            	} else {
            		stb.append(String.valueOf(_mincredit) + " " + Param.FROM_TO_MARK + " " + String.valueOf(_maxcredit));
            	}
            }
            return stb.toString();
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public String keySubclasscd() {
            return _subclasscode.substring(1);
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
        final String _provFlg;
        final Integer _replacemoto;
        final String _print_flg;
        final Integer _compCredit;
        final Integer _getCredit;
//        BigDecimal _absenceHigh;
        final Integer _credits;
//        boolean _isOver;
//        final String _chaircd;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;
        final String _slumpRemark;

        ScoreDetail(
                final SubClass subClass,
                final String score,
                final String scoreDi,
                final String assessLevel,
                final String karihyotei,
                final String provFlg,
                final Integer replacemoto,
                final String print_flg,
                final String slump,
                final String slumpMark,
                final String slumpScore,
                final String slumpRemark,
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
            _provFlg = provFlg;
            _print_flg = print_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
//            _chaircd = chaircd;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpMark = slumpMark;
            _slumpRemark = slumpRemark;
        }

//        /**
//         * 欠課時数超過ならTrueを戻します。
//         * @param absent 欠課時数
//         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
//         * @return
//         */
//        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
//            if (null == absent || null == absenceHigh) {
//                return false;
//            }
//            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
//                return false;
//            }
//            if (absenceHigh.doubleValue() < absent.doubleValue()) {
//                return true;
//            }
//            return false;
//        }

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

//        public String toString() {
//            return (_subClass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isOver);
//        }
    }

    static class Form {

        public boolean print(final DB2UDB db2, final IPdf ipdf, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
            final List studentsAll = new ArrayList(hrInfo._students);

            final List studentListList = getStudentListList(studentsAll, param._formMaxLine);
            for (final Iterator it = studentListList.iterator(); it.hasNext();) {
                final List studentList = (List) it.next();
                if (Form1.print(db2, ipdf, param, hrInfo, studentList)) {
                    hasData = true;
                }
            }
            return hasData;
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
                if (null == current || current.size() >= count) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(subClass);
            }
            return rtn;
        }

        public static List getMappedList(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList());
            }
            return (List) map.get(key1);
        }

        public static Map getMappedHashMap(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap());
            }
            return (Map) map.get(key1);
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

//        /**
//         *
//         * @param ipdf
//         * @param field
//         * @param line
//         * @param data
//         */
//        private static void svfsetString1(final IPdf ipdf, final String field, final int line, final String pf, final int col, final String data) {
//            ipdf.VrsOutn(field, line, data);
//        }

//        private static int gnumToLine(final Param param, final int gnum) {
//            return param._formMaxLine == gnum ? param._formMaxLine : gnum % param._formMaxLine;
//        }

        private static boolean isAlpPdf(final IPdf ipdf) {
            return "1".equals(ipdf.getParameter("AlpPdf"));
        }

        private static void setForm(final IPdf ipdf, final String formname, final int n, final Param param) {
            log.info(" form = " + formname);
            ipdf.VrSetForm(formname, n);

            param._currentform = formname;
            if (ipdf instanceof SvfPdf) {
                if (null != param._currentform && null == param._formFieldInfoMap.get(param._currentform)) {
                    param._formFieldInfoMap.put(param._currentform, SvfField.getSvfFormFieldInfoMapGroupByName(((SvfPdf) ipdf).getVrw32alp()));
                    //debugFormInfo(param);
                }
            }
        }

        public static boolean hasField(final IPdf ipdf, final String field, final Param param) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return true;
            }
            final Map fieldMap = getMappedHashMap(param._formFieldInfoMap, param._currentform);
            final boolean hasField = null != fieldMap.get(field);
            if (param._isOutputDebug) {
                if (!hasField) {
                    log.warn(" form " + param._currentform + " has no field :" + field);
                }
            }
            return hasField;
        }

        public static int getFieldKeta(final IPdf ipdf, final String field, final Param param) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return -1;
            }
            final Map fieldMap = getMappedHashMap(param._formFieldInfoMap, param._currentform);
            int keta = -1;
        	final SvfField f = (SvfField) fieldMap.get(field);
        	if (null != f) {
        		keta = f._fieldLength;
        	}
            final String logVal = " form " + param._currentform + " " + field + " keta = " + keta;
            if (param._isOutputDebug && !getMappedList(param._formFieldInfoMap, ".log").contains(logVal)) {
            	log.warn(logVal);
            	getMappedList(param._formFieldInfoMap, ".log").add(logVal);
            }
            return keta;
        }

        static class Form1 {

            private static boolean print(
            		final DB2UDB db2,
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo,
                    final List stulist
            ) {
                boolean hasData = false;
                setForm(ipdf, param._formname, 4, param);

                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = (SubClass) it.next();
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd())._isSaki) {
                        it.remove();
                    } else if (param._isNoPrintMoto && param.getSubclassMst(subclass.keySubclasscd())._isMoto) {
                        it.remove();
                    }
                }

                final List subclassListList = getSubClassListList(param, printSubclassList, param._formMaxColumn);

                for (int pi = 0, pages = subclassListList.size(); pi < pages; pi++) {
                    final List subclassList = (List) subclassListList.get(pi);

                    log.info(" subclassList page = " + String.valueOf(pi + 1) + " / " + subclassListList.size());

                    setForm(ipdf, param._formname, 4, param);
                    printHeader(db2, ipdf, param, hrInfo);
                    for (int sti = 0; sti < stulist.size(); sti++) {
                        final Student student = (Student) stulist.get(sti);
                        final int line = sti + 1; //gnumToLine(param, student._gnum);
                        printStudentName(ipdf, param, line, student);

                        printRemark(db2, ipdf, param, line, student, printSubclassList);
                    }

                    for (int subi = 0, size = subclassList.size(); subi < size; subi++) {

                        final SubClass subclass = (SubClass) subclassList.get(subi);
                        log.debug("p=" + pi + ", i=" + subi + ", subclasscd=" + subclass._subclasscode + " " + subclass._subclassabbv);
                        printSubclass(ipdf, param, subi + 1, subclass);

                        for (int sti = 0; sti < stulist.size(); sti++) {
                            final Student student = (Student) stulist.get(sti);
                            final int line = sti + 1; // gnumToLine(param, student._gnum);
                            final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                            if (null != detail) {
                            	printDetail(ipdf, param, detail, line, subi + 1, subclass);
                            }
                        }

                        ipdf.VrEndRecord();
                        hasData = true;
                    }
                    for (int i = subclassList.size(); i < param._formMaxColumn; i++) {
                        //教科名
                        ipdf.setRecordString("credit1", i + 1, "DUMMY");
                        ipdf.VrAttribute("credit1", "X=10000");
                        ipdf.VrEndRecord();
                        hasData = true;
                    }
                }
                return hasData;
            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param ipdf
             * @param hrInfo
             */
            private static void printHeader(
            		final DB2UDB db2,
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                ipdf.VrsOut("ymd1", param.getNow(db2)); // 作成日
                ipdf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrInfo._staffName) > 30 ? "2" : ""), hrInfo._staffName);  //担任名
                ipdf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称

                ipdf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度　" + param._title);

                for (int i = 1; i <= 8; i++) {
                    final String name1 = (String) param._d055Name1Map.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    final String field = "JOB" + String.valueOf(i) + (getMS932ByteLength(name1) > 8  ? "_2" : "_1");
                    ipdf.VrsOut(field, name1);
                }
            }

            /**
             * 生徒の氏名・備考を印字
             * @param ipdf
             * @param hrInfo
             * @param stulist：List hrInfo._studentsのsublist
             */
            private static void printStudentName(
                    final IPdf ipdf,
                    final Param param,
                    final int line,
                    final Student student
            ) {
                ipdf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                // 学籍番号表記
                String nameNo = "1";
                ipdf.VrsOutn("NAME" + nameNo, line, student._name);    // 氏名
            }

            private static void printRemark(final DB2UDB db2, final IPdf ipdf, final Param param, final int line, final Student student, final List printSubclassList) {
                final String remark = student.getRemark(db2, param, printSubclassList);
                ipdf.VrsOutn("REMARK1" + (getMS932ByteLength(remark) <= 62 ? "" : "_2"), line, remark);  // 備考
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param ipdf
             * @param subclass
             * @param line：科目の列番号
             * @param stulist：List hrInfo._studentsのsublist
             * @return
             */
            private static void printSubclass(
                    final IPdf ipdf,
                    final Param param,
                    final int col,
                    final SubClass subclass
            ) {
                //教科名
                ipdf.setRecordString("course1", col, subclass._classabbv);
                //科目名
                final String[] subclassfields = null != subclass._subclassname && subclass._subclassname.length() <= 7 ? new String[] {"SUBCLASS"} : new String[] {"SUBCLASS_1", "SUBCLASS_2"};
                if (null != subclass._subclassname && subclass._subclassname.length() <= 7) {
                    ipdf.setRecordString(subclassfields[0], col, subclass._subclassname);
                } else {
                    final String[] token = new String[2];
                    token[0] = subclass._subclassname.substring(0, Math.min(subclass._subclassname.length(), 8));
                    token[1] = (subclass._subclassname.length () <= 8) ? "" : subclass._subclassname.substring(8, Math.min(subclass._subclassname.length(), 8 * 2));
                    if (null != token) {
                        for (int i = 0; i < token.length && i < subclassfields.length; i++) {
                            ipdf.setRecordString(subclassfields[i], col, token[i]);
                        }
                    }
                }
                //単位数
                final String creditStr = subclass.getPrintCredit(param);
                final int creditStrLen = getMS932ByteLength(creditStr);
                final int creditFieldKeta = getFieldKeta(ipdf, "credit1", param);
                final String creditField = (creditFieldKeta < creditStrLen && creditFieldKeta < getFieldKeta(ipdf, "credit1_2", param)) ? "credit1_2" :"credit1";
				Form1.setRecordString(ipdf, creditField, col, creditStr);
                Form1.setRecordString(ipdf, fieldClassTeacher(param, subclass._staffname), col, subclass._staffname);
            }

            public static String fieldClassTeacher(final Param param, final String staffname) {
                String fieldClassTeacher = null;
                if (getMS932ByteLength(staffname) > 4) {
                    fieldClassTeacher  = "CLASS_TEACHER2";
                } else {
                    fieldClassTeacher  = "CLASS_TEACHER";
                }
                return fieldClassTeacher;
            }

            /**
             * 生徒別科目別素点・評定・欠課時数等を印刷します。
             * @param ipdf
             * @param line 生徒の行番
             */
            private static void printDetail(
                    final IPdf ipdf,
                    final Param param,
                    final ScoreDetail detail,
                    final int line,
                    final int col,
                    final SubClass subclass
            ) {
                final String scoreField = "SCORE";
                final String aster;
                if ("990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
                    aster = "*";
                } else {
                    aster = "";
                }
//                final String printScore;
//                if (null == detail._score) {
//                    printScore = StringUtils.defaultString(detail._scoreDi);
//                } else {
//                    printScore = StringUtils.defaultString(detail._score);
//                }
                String printScore = "";
                if (SIDOU_INPUT_INF_MARK.equals(param._testItem._sidouinputinf)) { // 記号
                	printScore = StringUtils.defaultString((String) param._d054Name1Map.get(detail._slumpMark));
                } else if (SIDOU_INPUT_INF_SCORE.equals(param._testItem._sidouinputinf)) { // 得点
                	printScore = StringUtils.defaultString(detail._slumpScore);
                }
                ipdf.setRecordString(scoreField + String.valueOf(line) + "", col, aster + printScore);
            }
            
            public static int setRecordString(IPdf ipdf, String field, int gyo, int retsu, String data) {
                return ipdf.setRecordString(field, gyo, data);
            }

            public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
                return ipdf.setRecordString(field, gyo, data);
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

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 60687 $ $Date: 2018-06-15 22:15:41 +0900 (金, 15 6 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    protected static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;

        /** 学年 */
        final String _grade;
        final String[] _classSelected;
        final String _testKindCd;
        final String _scoreDiv;

//        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
//        final String _checkKettenDiv;
        /** 起動元のプログラムＩＤ */
        final String _prgId;

        final String _cmd;

        /** フォーム生徒行数 */
        int _formMaxLine;

        KNJ_Get_Info _getinfo = new KNJ_Get_Info();

        /** 科目数 */
        int _formMaxColumn;
        final String _formname;

//        /** 備考欄出力（出欠備考を出力） */
//        final boolean _outputBiko;
        final String _schoolName;
//        final String _documentroot;

        private String _semesterName;
        private TestItem _testItem;

        private static final String FROM_TO_MARK = "\uFF5E";

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd = "1";

        final String _knjd615vPrintNullRemark;

        private KNJSchoolMst _knjSchoolMst;

        final boolean _isOutputDebug;

        final String _d054Namecd2Max;
        final String _sidouHyoji;
        final Map _d054Name1Map;
        final Map _d055Name1Map;
        private Map _subclassMst;
        private boolean _isPrintSakiKamoku;
        private boolean _isNoPrintMoto;

        private String _title;
//        private String _item1Name;  // 明細項目名
//        final String _item2Name;  // 明細項目名
//        final String _item4Name;  // 総合点欄項目名
//        final String _item5Name;  // 平均点欄項目名
//        final String _form2Item4Name;  // 平均点欄項目名
//        final String _form2Item5Name;  // 平均点欄項目名
        private boolean _creditDrop;
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
        final boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
        final Map _psMap = new HashMap();
        final Map _d053Name1Map;
        private String _currentform;
        private Map _formFieldInfoMap = new HashMap();

        final String _logindate;
        final String _gdatgradename;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _creditDrop = (request.getParameter("OUTPUT4") != null);
//            _documentroot = request.getParameter("DOCUMENTROOT");
//            _outputBiko = "1".equals(request.getParameter("OUTPUT_BIKO"));
            _cmd = request.getParameter("cmd");
            _formname = "KNJD616V.frm";
            _formMaxLine = 45;
            _formMaxColumn = 46;
//            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _prgId = request.getParameter("PRGID");
//            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _knjd615vPrintNullRemark = request.getParameter("knjd615vPrintNullRemark");
            _d053Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D053' "), "NAMECD2", "NAME1");
            _logindate = StringUtils.defaultString(request.getParameter("LOGIN_DATE"));

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            log.info(" schoolName = " + _schoolName);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            log.info(" isOutputDebug = " + _isOutputDebug);

        	final Map d054Max = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(d054Max, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(d054Max, "NAME1");
            _d054Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D054' "), "NAMECD2", "NAME1");

            _d055Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D055' "), "NAMECD2", "NAME1");
            _definecode = createDefineCode(db2);
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = StringUtils.defaultString(returnval.val1);  //学期名称
//            // 学期期間FROM
//            if (null == returnval.val2) {
//                _semesterDateS = _year + "-04-01";
//            } else {
//                _semesterDateS = returnval.val2;
//            }
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
//            _item4Name = "合計点";
//            _item5Name = "平均点";
//            _form2Item4Name = _item4Name;
//            _form2Item5Name = _item5Name;

//            final String ineiWakuPath = _documentroot + "/image/KNJD615_keninwaku2.jpg";
//            final File ineiWakuFile = new File(ineiWakuPath);
//            log.info(" ineiWakuFile exists? " + ineiWakuFile.exists());
//            if (!ineiWakuFile.exists()) {
//                _ineiWakuPath = null;
//            } else {
//                _ineiWakuPath = ineiWakuPath;
//            }

//            _item2Name = "欠課";
            _enablePringFlg = false;
            _scoreDiv = _testKindCd.substring(4);
            _isGakunenMatu = SCORE_DIV_09.equals(_scoreDiv);
            if (SCORE_DIV_01.equals(_scoreDiv) || SCORE_DIV_02.equals(_scoreDiv)) {
//            	_item1Name = StringUtils.defaultString((String) _d053Name1Map.get(_scoreDiv), SCORE_DIV_02.equals(_scoreDiv) ? "平常点" : "素点");
                _creditDrop = false;
                _title = _semesterName + " " + _testItem._testitemname + " 追指導成績一覧表";
            } else if (SCORE_DIV_08.equals(_scoreDiv)) {
//                _item1Name = "評価";
                _title = _semesterName + " " + _testItem._testitemname + " 追指導成績一覧表";
            } else if (SCORE_DIV_09.equals(_scoreDiv)) {
//                _item1Name = "評定";
                _title = _testItem._testitemname + "  追指導成績一覧表（評定）";
            }

            setSubclassMst(db2);
            setPrintSakiKamoku(db2);
            loadNameMstD016(db2);
            _gdatgradename = loadGdatGName(db2, _year, _grade);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD616V' AND NAME = '" + propName + "' "));
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
            final String sql = "SELECT TESTITEMNAME, SIDOU_INPUT, SIDOU_INPUT_INF "
                    +   "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV "
                    +  "WHERE YEAR = '" + year + "' "
                    +    "AND SEMESTER = '" + semester + "' "
                    +    "AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            testitem._testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
            testitem._sidouinput = KnjDbUtils.getString(row, "SIDOU_INPUT");
            testitem._sidouinputinf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
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

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            if (SEMEALL.equals(_semester)) {
            	_isNoPrintMoto = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ")));
                log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
            }
        }

        private String loadGdatGName(final DB2UDB db2, final String year, final String grade) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " select GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' "));
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 名称マスタ「D021」「01」から取得する
            _isPrintSakiKamoku = !"Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' ")));

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
            _subclassMst = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
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
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final boolean isSaki = "1".equals(KnjDbUtils.getString(row, "IS_SAKI"));
                    final boolean isMoto = "1".equals(KnjDbUtils.getString(row, "IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"), isSaki, isMoto);
                    _subclassMst.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
    }

}
