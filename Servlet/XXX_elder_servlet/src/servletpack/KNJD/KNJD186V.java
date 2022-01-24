// kanji=漢字
/*
 * $Id: KNJD186V.java 75537 2020-07-20 10:07:40Z maeshiro $
 */
package servletpack.KNJD;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186V {
    private static final Log log = LogFactory.getLog(KNJD186V.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    static final String PATTERN_A = "1";
    static final String PATTERN_B = "2";
    static final String PATTERN_C = "3";
    static final String PATTERN_D = "4";
    static final String PATTERN_E = "5";

    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String ATTRIBUTE_TUISHIDO = "Paint=(1,90,2),Bold=1";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            final IPdf ipdf = new SvfPdf(svf);

            response.setContentType("application/pdf");

            outputPdf(ipdf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    public void outputPdf(
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, ipdf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    protected void printMain(
            final DB2UDB db2,
            final IPdf ipdf,
            final Param param
    ) {
        final List<Student> studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }
        load(param, db2, studentList);

        for (final Student student : studentList) {
            log.info(" schregno = " + student._schregno);
            param._form.init(param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));
            param._form.print(db2, ipdf, student);
        }
        _hasData = true;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static void loginfo(final Param param, final Object o) {
        if (param._isOutputDebug) {
            log.info(o);
        }
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List<Student> studentList0
    ) {
        Student.loadPreviousCredits(db2, param, studentList0);  // 前年度までの修得単位数取得

        final Form form = param._form;

        final Map<String, List<Student>> courseStudentsMap = new HashMap<String, List<Student>>();
        for (final Student student : studentList0) {
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            getMappedList(courseStudentsMap, key).add(student);
        }

        for (final String key : courseStudentsMap.keySet()) {
            final List<Student> studentList = courseStudentsMap.get(key);
            final String[] split = StringUtils.split(key, "-");

            final List recordMockOrderSdivDatList = param.getRecordMockOrderSdivDat(split[0], split[1], split[2]);

            form.init(recordMockOrderSdivDatList);

            for (final DateRange range : form._attendRanges) {
                Attendance.load(db2, param, studentList, range);
            }
            for (final DateRange range : form._attendSubclassRanges) {
                SubclassAttendance.load(db2, param, studentList, range);
            }

            String testcdor = "";
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            final List<String> testcds = new ArrayList<String>(form._testcds);
            if (SEMEALL.equals(param._semester)) {
                testcds.add(TESTCD_GAKUNEN_HYOTEI);
            }
            for (final String testcd : testcds) {
                if (null == testcd) {
                    continue;
                }
                final String seme = testcd.substring(0, 1);
                final String kind = testcd.substring(1, 3);
                final String item = testcd.substring(3, 5);
                final String sdiv = testcd.substring(5);
                if (seme.compareTo(param._semester) <= 0) {
                    stbtestcd.append(testcdor);
                    stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                    testcdor = " OR ";
                }
            }
            stbtestcd.append(") ");
            Score.load(db2, param, studentList, stbtestcd);

            if (PATTERN_A.equals(param._patarnDiv)) {
                ProficiencyScore.load(db2, param, studentList);
            }
            if (PATTERN_C.equals(param._patarnDiv) || PATTERN_E.equals(param._patarnDiv)) {
                Address.setAddress(db2, studentList, param);
            }
            if (PATTERN_E.equals(param._patarnDiv)) {
                Student.setTotalStudy(db2, param, studentList);
            }
            Student.setHreportremarkCommunication(param, db2, studentList);
        }
    }

    private static Student getStudent(final List<Student> studentList, final String code) {
        if (code == null) {
            return null;
        }
        for (final Student student : studentList) {
            if (code.equals(student._schregno)) {
                return student;
            }
        }
        return null;
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String mkString(final List<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        if (null == list) {
            return stb.toString();
        }
        String comma0 = "";
        for (final String s : list) {
            if (null == s || s.length() == 0) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
        final Map<String, String> _attendRemarkMap;
        final Map<String, SubClass> _subclassMap;
        final List<ProficiencyScore> _proficiencySubclassScoreList;
        final String _entyear;
        final String _curriculumYear;
        private Address _address;
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private String _communication;

        private Map _specialGroupKekkaMinutes;
        private String _totalStudyKatsudo;
        private String _totalStudyHyoka;
        private String _careerPlanKatsudo;
        private String _careerPlanHyoka;
        private List<String> _attendHrRemark = new ArrayList<String>();

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear, final String curriculumYear) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
            _curriculumYear = curriculumYear;
            _attendMap = new TreeMap();
            _attendRemarkMap = new TreeMap<String, String>();
            _subclassMap = new TreeMap<String, SubClass>();
            _proficiencySubclassScoreList = new ArrayList<ProficiencyScore>();
            _specialGroupKekkaMinutes = new HashMap();
        }

        SubClass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubClass(new SubclassMst(classcd, subclasscd, null, null, null, null, 9999, 9999, false, false));
            }
            return _subclassMap.get(subclasscd);
        }

        public String getHrAttendNo(final Param param) {
            try {
                final String grade = String.valueOf(Integer.parseInt(param._gradeHrclass.substring(0, 2)));
                final String hrclass = String.valueOf(Integer.parseInt(param._gradeHrclass.substring(2)));
                final String attendno = String.valueOf(Integer.parseInt(_attendno));
                return grade + "-" + hrclass + "-" + attendno + " " + StringUtils.defaultString(_name);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        /**
         * 生徒を取得
         */
        private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  W1.SCHREGNO");
            stb.append("            ,W1.SEMESTER ");
            stb.append("            ,W7.NAME ");
            stb.append("            ,W6.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,EGHIST.CURRICULUM_YEAR ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
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
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = W1.SCHREGNO AND EGHIST.SCHOOL_KIND = 'H' ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND W1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         W1.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List<Student> students = new ArrayList<Student>();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR"), rs.getString("CURRICULUM_YEAR")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

        private String getSogoSubclassname(final Param param) {
            final int tankyuStartYear = 2019;
            int y = -1;
            if (NumberUtils.isDigits(_curriculumYear)) {
                y = Integer.parseInt(_curriculumYear);
            } else if (NumberUtils.isDigits(_entyear) && NumberUtils.isDigits(param._gradeCd)) {
                y = Integer.parseInt(_entyear) - Integer.parseInt(param._gradeCd) + 1;
            }
            final String subclassname = y >= tankyuStartYear ? "総合的な探究の時間" : "総合的な学習の時間";
            if (param._isOutputDebug) {
                log.info(" sogo = " + subclassname + ", y = " + y + ", curriculumYear = " + _curriculumYear + ", entYear = " + _entyear + ", gradeCd = " + param._gradeCd);
            }
            return subclassname;
        }

        // 前年度までの修得単位数計
        private static void loadPreviousCredits(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" LEFT JOIN ( ");
            stb.append("         SELECT I1.SCHREGNO, MAX(I1.YEAR) AS YEAR ");
            stb.append("         FROM SCHREG_REGD_DAT I1 ");
            stb.append("         WHERE I1.YEAR <= '" + param._year + "' ");
            stb.append("           AND I1.SCHREGNO = ? ");
            stb.append("         GROUP BY I1.SCHREGNO, I1.GRADE ");
            stb.append("     ) NOT_RYUNEN ON NOT_RYUNEN.SCHREGNO = T1.SCHREGNO");
            stb.append("                 AND NOT_RYUNEN.YEAR = T1.YEAR ");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + param._year + "'");
            stb.append("    AND NOT_RYUNEN.YEAR IS NOT NULL ");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            final String sql = stb.toString();

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    int i = 1;
                    ps.setString(i++, student._schregno);
                    ps.setString(i++, student._schregno);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._previousCredits = rs.getInt("CREDIT");
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

        // 仮評定があるか
        public boolean hasKari(final Param param) {
            for (final String subclasscd : _subclassMap.keySet()) {
                if (SUBCLASSCD999999.equals(subclasscd) || subclasscd.endsWith("333333") || subclasscd.endsWith("555555") || subclasscd.endsWith("99999B")) {
                    continue;
                }
                final SubClass subClass = getSubClass(subclasscd);
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                if (null != score && NumberUtils.isDigits(score._score) && "1".equals(score._provFlg)) {
                    return true;
                }
            }
            return false;
        }

        public String getTotalGetCredit(final Param param, final boolean addPrevious) {
            int totalGetCredit = 0;
            int totalGetCreditKari = 0;
            final Map getCreditMap = new TreeMap();
            final Map getCreditKariMap = new TreeMap();
            for (final String subclasscd : _subclassMap.keySet()) {
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final SubClass subClass = getSubClass(subclasscd);
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    log.info(" // skip credit : " + (null == subClass ? "" : subClass._mst + " : " + subClass._mst._isMoto));
                    continue;
                }
                final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                final String getCredit = null == score ? null : score.getGetCredit(param);
                if (NumberUtils.isDigits(getCredit)) {
                    final int iCredit = Integer.parseInt(getCredit);
                    if ("1".equals(score._provFlg)) {
                        totalGetCreditKari += iCredit;
                        getCreditKariMap.put(subClass._mst, getCredit);
                    } else {
                        totalGetCredit += iCredit;
                        getCreditMap.put(subClass._mst, getCredit);
                    }
                }
            }
            if (param._isOutputDebug) {
                log.info(" total get credit      = " + getCreditMap);
                log.info(" total get credit kari = " + getCreditKariMap);
            }
            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
                if (addPrevious && _previousCredits > 0) {
                    return String.valueOf(_previousCredits);
                }
                return "(" + String.valueOf(totalGetCreditKari) + ")";
            }
            if (addPrevious) {
                totalGetCredit += _previousCredits;
            }
            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
        }

        public String getKettenSubclassCount(final Param param, final TestItem testItem) {
            final List<SubClass> list = new ArrayList<SubClass>();
            boolean hasNotNullSubclassScore = false;
            for (final String subclasscd : _subclassMap.keySet()) {
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final SubClass subClass = getSubClass(subclasscd);
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                final Score score = subClass.getScore(testItem._testcd);
                if (null != score) {
                    if (score.isKetten(param, testItem)) {
                        list.add(subClass);
                    }
                    if (null != score._score) {
                        hasNotNullSubclassScore = true;
                    }
                }
            }
            if (!hasNotNullSubclassScore) {
                return null;
            }
            if (!list.isEmpty()) {
                log.info(" ketten " + testItem._testcd + " subclass list = " + list);
            }
            return String.valueOf(list.size());
        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List<Student> studentList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT COMMUNICATION ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("   AND SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentList) {
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._communication = rs.getString("COMMUNICATION");
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 総合的な学習の時間の所見をセットする
         * @param db2
         * @param param
         * @param studentList
         */
        public static void setTotalStudy(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SUBCLASSNAME ");
                stb.append(" FROM ");
                stb.append("     SUBCLASS_MST T1 ");
                stb.append("     INNER JOIN V_NAME_MST T2 ON T2.YEAR = '" + param._year + "' ");
                stb.append("         AND T2.NAMECD1 = '" + param._d008Namecd1 + "' ");
                stb.append("         AND T2.NAMECD2 = T1.CLASSCD  ");
                stb.append("         AND T2.NAMESPARE2 = 'CAREER_PLAN' ");
                stb.append(" ORDER BY ");
                stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");

                final String sql = stb.toString();
                //log.debug(" total study sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (null == param._careerPlanSubclassname) {
                        param._careerPlanSubclassname = rs.getString("SUBCLASSNAME");
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }


            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SEMESTER ");
                stb.append("     ,T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("     ,T1.TOTALSTUDYTIME ");
                stb.append("     ,T1.TOTALSTUDYACT");
                stb.append("     ,T2.NAMESPARE2 ");
                stb.append(" FROM ");
                stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
                stb.append("     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '" + param._year + "' ");
                stb.append("         AND T2.NAMECD1 = '" + param._d008Namecd1 + "' ");
                stb.append("         AND T2.NAMECD2 = T1.CLASSCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SEMESTER = '" + SEMEALL + "' ");
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");

                final String sql = stb.toString();

                //log.debug(" total study sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String totalStudyAct = rs.getString("TOTALSTUDYACT");
                        final String totalStudyTime = rs.getString("TOTALSTUDYTIME");

                        if ("CAREER_PLAN".equals(rs.getString("NAMESPARE2"))) {
                            student._careerPlanKatsudo = addLine(student._careerPlanKatsudo, totalStudyAct);
                            student._careerPlanHyoka = addLine(student._careerPlanHyoka, totalStudyTime);
                        } else {
                            student._totalStudyKatsudo = addLine(student._totalStudyKatsudo, totalStudyAct);
                            student._totalStudyHyoka = addLine(student._totalStudyHyoka, totalStudyTime);
                        }
                    }

                    //log.debug(" _careerPlanSubclassname " + student._careerPlanSubclassname + " / " + student._totalStudySubclassname);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String addLine(final String source, final String data) {
            if (StringUtils.isBlank(source)) {
                return data;
            }
            if (StringUtils.isBlank(data)) {
                return source;
            }
            return source + "\n" + data;
        }
    }

    /**
     * 宛先住所データ
     */
    private static class Address {
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
        public static void setAddress(final DB2UDB db2, final List<Student> studentList, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();

//                if ("1".equals(param._addressSelect)) {
//                    stb.append(" SELECT T0.SCHREGNO, ");
//                    stb.append("        CASE WHEN T5.SCHREGNO IS NOT NULL THEN T0.REAL_NAME ELSE T0.NAME END AS ADDRESSEE, ");
//                    stb.append("        T4.ADDR1, T4.ADDR2, T4.ZIPCD ");
//                    stb.append(" FROM SCHREG_BASE_MST T0 ");
//                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) T3 ON ");
//                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
//                    stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
//                    stb.append(" LEFT JOIN SCHREG_NAME_SETUP_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO AND T5.DIV = '03' ");
//                    stb.append(" WHERE ");
//                    stb.append("     T0.SCHREGNO = ? ");
//                } else if ("2".equals(param._addressSelect)) {
                    stb.append(" SELECT T0.SCHREGNO, T2.GUARD_NAME AS ADDRESSEE, T5.GUARD_NAME AS ADDRESSEE2, T4.GUARD_ADDR1 AS ADDR1, T4.GUARD_ADDR2 AS ADDR2, T4.GUARD_ZIPCD AS ZIPCD ");
                    stb.append(" FROM SCHREG_BASE_MST T0 ");
                    stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                    stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT WHERE '" + param._date + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') GROUP BY SCHREGNO) T3 ON ");
                    stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                    stb.append(" LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                    stb.append(" LEFT JOIN GUARDIAN_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._date + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
                    stb.append(" WHERE ");
                    stb.append("     T0.SCHREGNO = ? ");
//                } else {
//                    stb.append(" SELECT T0.SCHREGNO, T2.SEND_NAME AS ADDRESSEE, T2.SEND_NAME AS ADDRESSEE2, T2.SEND_ADDR1 AS ADDR1, T2.SEND_ADDR2 AS ADDR2, T2.SEND_ZIPCD AS ZIPCD ");
//                    stb.append(" FROM SCHREG_BASE_MST T0 ");
//                    stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO AND T2.DIV = '1' ");
//                    stb.append(" WHERE ");
//                    stb.append("     T0.SCHREGNO = ? ");
//                }

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        // final String addressee = "2".equals(param._addressSelect) && null != rs.getString("ADDRESSEE2") ? rs.getString("ADDRESSEE2") : rs.getString("ADDRESSEE");
                        final String addressee = true && null != rs.getString("ADDRESSEE2") ? rs.getString("ADDRESSEE2") : rs.getString("ADDRESSEE");
                        final String addr1 = rs.getString("ADDR1");
                        final String addr2 = rs.getString("ADDR2");
                        final String zipcd = rs.getString("ZIPCD");
                        student._address = new Address(addressee, addr1, addr2, zipcd);
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class Attendance {
        private static final String GROUP_LHR = "001";
        private static final String GROUP_EVENT = "002";
        private static final String GROUP_COMMITTEE = "003";

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _abroad;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        BigDecimal _lhrKekka = new BigDecimal("0");
        BigDecimal _gyojiKekka = new BigDecimal("0");
        BigDecimal _iinkaiKekka = new BigDecimal("0");
        DateRange _dateRange;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int abroad,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }

        Attendance add(final Attendance att) {
            return new Attendance(
                    _lesson + att._lesson,
                    _mLesson + att._mLesson,
                    _suspend + att._suspend,
                    _mourning + att._mourning,
                    _abroad + att._abroad,
                    _absent + att._absent,
                    _present + att._present,
                    _late + att._late,
                    _early + att._early
                    );
        }

        public String toString() {
            return "Att(les=" + _lesson + ",mles=" + _mLesson + ",susp" + _suspend + ",mourn=" + _mourning  + ",abroad=" + _abroad + ",absent=" + _absent + ",prese=" + _present + ",late=" + _late + ",early=" + _early + ")";
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("TRANSFER_DATE"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY")
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, param._year, dateRange._sdate, edate);
            loadRemark(db2, param, (String) hasuuMap.get("attendSemesInState"), studentList, dateRange);
        }

        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List<Student> studentList, final DateRange dateRange) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.MONTH, T1.SEMESTER, T1.SCHREGNO, T1.REMARK1 ");
                stb.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   T1.COPYCD = '0' ");
                stb.append("   AND T1.YEAR = '" + param._year + "' ");
                stb.append("   AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState + " ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append("   AND T1.REMARK1 IS NOT NULL ");
                stb.append(" ORDER BY T1.SEMESTER, INT(T1.MONTH) + CASE WHEN INT(T1.MONTH) < 4 THEN 12 ELSE 0 END ");

                //log.debug(" dateRange = " + dateRange + " /  remark sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    final List<String> remark = new ArrayList<String>();
                    while (rs.next()) {
                        if (null != rs.getString("REMARK1")) {
                            remark.add(rs.getString("REMARK1"));
                        }
                    }
                    if (remark.size() != 0) {
                        student._attendRemarkMap.put(dateRange._key, mkString(remark, "、"));
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /**
     * 科目
     */
    private static class SubClass implements Comparable<SubClass> {
        final SubclassMst _mst;
        final Map<String, Score> _scoreMap;
        final Map<String, SubclassAttendance> _attendMap;

        SubClass(
                final SubclassMst mst
        ) {
            _mst = mst;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
        }

        public Score getScore(final String testcd) {
            if (null == testcd) {
                return null;
            }
            return _scoreMap.get(testcd);
        }

        public SubclassAttendance getAttendance(final String key) {
            if (null == key) {
                return null;
            }
            return _attendMap.get(key);
        }

        public int compareTo(final SubClass subclass) {
            return _mst.compareTo(subclass._mst);
        }

        public String toString() {
            return "SubClass(" + _mst.toString() + ", score = " + _scoreMap + ", attend = " + _attendMap + ")";
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal sick) {
            _lesson = lesson;
            _sick = sick;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                //loginfo(param, " attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    final Map specialGroupKekkaMinutes = new HashMap();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = param._subclassMst.get(subclasscd);
                        if (null == mst) {
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            // final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            // final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? replacedSick : sick);

                            if (null == student._subclassMap.get(subclasscd)) {
                                final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                                student._subclassMap.put(subclasscd, subClass);
                            }
                            final SubClass subClass = student.getSubClass(subclasscd);
                            subClass._attendMap.put(dateRange._key, subclassAttendance);
                        }

                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                        if (null != specialGroupCd) {
                            // 特別活動科目の処理 (授業分数と結果数の加算)
                            final String subclassCd = rs.getString("SUBCLASSCD");
                            final String kekkaMinutes = rs.getString("SPECIAL_SICK_MINUTES1");

                            getMappedMap(specialGroupKekkaMinutes, specialGroupCd).put(subclassCd, kekkaMinutes);
                        }
                    }

                    for (final Iterator spit = specialGroupKekkaMinutes.entrySet().iterator(); spit.hasNext();) {
                        final Map.Entry e = (Map.Entry) spit.next();
                        final String specialGroupCd = (String) e.getKey();
                        final Map subclassKekkaMinutesMap = (Map) e.getValue();

                        int totalMinutes = 0;
                        for (final Iterator subit = subclassKekkaMinutesMap.entrySet().iterator(); subit.hasNext();) {
                            final Map.Entry subMinutes = (Map.Entry) subit.next();
                            final String minutes = (String) subMinutes.getValue();
                            if (NumberUtils.isDigits(minutes)) {
                                totalMinutes += Integer.parseInt(minutes);
                            }
                        }

                        final BigDecimal spGroupKekkaJisu = getSpecialAttendExe(totalMinutes, param);

                        if (null == student._attendMap.get(dateRange._key)) {
                            student._attendMap.put(dateRange._key, new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0));
                        }
                        final Attendance attendance = (Attendance) student._attendMap.get(dateRange._key);

                        if (param._isOutputDebug) {
                            log.info(" set " + specialGroupCd + " kekkaJisu = " + spGroupKekkaJisu);
                        }
                        if (Attendance.GROUP_LHR.equals(specialGroupCd)) {
                            attendance._lhrKekka = spGroupKekkaJisu;
                        } else if (Attendance.GROUP_EVENT.equals(specialGroupCd)) {
                            attendance._gyojiKekka = spGroupKekkaJisu;
                        } else if (Attendance.GROUP_COMMITTEE.equals(specialGroupCd)) {
                            attendance._iinkaiKekka = spGroupKekkaJisu;
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
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
    }

    /**
     * 成績
     */
    private static class Score {
        final String _testcd;
        final String _score;
        final String _assessLevel;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;
        final String _karihyotei;
        final String _replacemoto;
        final String _compCredit;
        final String _getCredit;
        final String _slump;
        final String _slumpMark;
        final String _slumpMarkName1;
        final String _slumpScore;
        final String _slumpScoreKansan;
        final String _provFlg;
        final String _passScore;

        Score(
                final String testcd,
                final String score,
                final String assessLevel,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank,
                final String karihyotei,
                final String replacemoto,
                final String slump,
                final String slumpMark,
                final String slumpMarkName1,
                final String slumpScore,
                final String slumpScoreKansan,
                final String compCredit,
                final String getCredit,
                final String provFlg,
                final String passScore
        ) {
            _testcd = testcd;
            _score = score;
            _assessLevel = assessLevel;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpScoreKansan = slumpScoreKansan;
            _slumpMark = slumpMark;
            _slumpMarkName1 = slumpMarkName1;
            _provFlg = provFlg;
            _passScore = passScore;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public String getCompCredit(final Param param) {
            return enableCredit(param) ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public String getGetCredit(final Param param) {
            return enableCredit(param) ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit(final Param param) {
            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1 && !"1".equals(param._knjd186vAddAttendSubclassGetCredit)) {
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

        private String getPrintSlump(final TestItem testItem) {
            if (null != testItem._sidouinput) {
                String rtn = null;
                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
                    rtn = _slumpMarkName1;
                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点
                    rtn = _slumpScore;
                }
                return rtn;
            }
            return null;
        }

        private boolean isKetten(final Param param, final TestItem testItem) {
            if (null != testItem._sidouinput && !"1".equals(param._kettenKamokuNoSubtract)) {
                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
                    if (null != _slumpMark) {
                        if (param._d054KettenNamecd2List.contains(_slumpMark)) {
                            return true;
                        }
                        return false;
                    }
                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点
                    if (null != _slumpScoreKansan) {
                        return "1".equals(_slumpScoreKansan);
                    }
                }
            }

            final boolean setPassScore = NumberUtils.isDigits(_passScore);
            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
                return !setPassScore && "1".equals(_score) || setPassScore && NumberUtils.isDigits(_score) && Integer.parseInt(_score) < Integer.parseInt(_passScore);
            }
            return !setPassScore && "1".equals(_assessLevel) || setPassScore && NumberUtils.isDigits(_score) && Integer.parseInt(_score) < Integer.parseInt(_passScore);
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList,
                final StringBuffer stbtestcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param, stbtestcd);
                if (param._isOutputDebug) {
                    log.info(" subclass query start. sql = " + sql);
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (param._isOutputDebug) {
                    log.info(" subclass query end.");
                }

                final boolean isNotPrintRank = "1".equals(param._noPrintRank);
                while (rs.next()) {
                    final Student student = getStudent(studentList, rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Rank gradeRank = new Rank(isNotPrintRank ? null : rs.getString("GRADE_RANK"), isNotPrintRank ? null : rs.getString("GRADE_AVG_RANK"), isNotPrintRank ? null : rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(isNotPrintRank ? null : rs.getString("CLASS_RANK"), isNotPrintRank ? null : rs.getString("CLASS_AVG_RANK"), isNotPrintRank ? null : rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(isNotPrintRank ? null : rs.getString("COURSE_RANK"), isNotPrintRank ? null : rs.getString("COURSE_AVG_RANK"), isNotPrintRank ? null : rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(isNotPrintRank ? null : rs.getString("MAJOR_RANK"), isNotPrintRank ? null : rs.getString("MAJOR_AVG_RANK"), isNotPrintRank ? null : rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                            testcd,
                            rs.getString("SCORE"),
                            rs.getString("ASSESS_LEVEL"),
                            rs.getString("AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // rs.getString("KARI_HYOUTEI"),
                            rs.getString("REPLACEMOTO"),
                            rs.getString("SLUMP"),
                            rs.getString("SLUMP_MARK"),
                            rs.getString("SLUMP_MARK_NAME1"),
                            rs.getString("SLUMP_SCORE"),
                            rs.getString("SLUMP_SCORE_KANSAN"),
                            rs.getString("COMP_CREDIT"),
                            rs.getString("GET_CREDIT"),
                            rs.getString("PROV_FLG"),
                            rs.getString("PASS_SCORE")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subClass);
                    }
                    if (null == testcd) {
                        if (null != rs.getString("GET_CREDIT") && null != rs.getString("ZOUKA")) {
                            final SubClass subClass = student.getSubClass(subclasscd);
                            subClass._scoreMap.put(TESTCD_GAKUNEN_HYOTEI, score);
                        }
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final SubClass subClass = student.getSubClass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlScore(final Param param, final StringBuffer stbtestcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, ");
            stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

            stb.append("   , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM RELATIVEASSESS_MST L3 ");
            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("         AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("      ) ELSE ");
            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("       FROM ASSESS_MST L3 ");
            stb.append("       WHERE L3.ASSESSCD = '3' ");
            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("      ) ");
            stb.append("     END AS ASSESS_LEVEL ");
            stb.append("    ,W3.AVG ");
            stb.append("    ,W3.GRADE_RANK ");
            stb.append("    ,W3.GRADE_AVG_RANK ");
            stb.append("    ,W3.CLASS_RANK ");
            stb.append("    ,W3.CLASS_AVG_RANK ");
            stb.append("    ,W3.COURSE_RANK ");
            stb.append("    ,W3.COURSE_AVG_RANK ");
            stb.append("    ,W3.MAJOR_RANK ");
            stb.append("    ,W3.MAJOR_AVG_RANK ");
            stb.append("    ,T_AVG1.AVG AS GRADE_AVG ");
            stb.append("    ,T_AVG1.COUNT AS GRADE_COUNT ");
            stb.append("    ,T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
            stb.append("    ,T_AVG2.AVG AS HR_AVG ");
            stb.append("    ,T_AVG2.COUNT AS HR_COUNT ");
            stb.append("    ,T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
            stb.append("    ,T_AVG3.AVG AS COURSE_AVG ");
            stb.append("    ,T_AVG3.COUNT AS COURSE_COUNT ");
            stb.append("    ,T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
            stb.append("    ,T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("    ,T_AVG4.COUNT AS MAJOR_COUNT ");
            stb.append("    ,T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG1.AVG_DIV = '1' ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("     AND (W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("       OR W3.SEMESTER || W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.COMP_CREDIT ");
            stb.append("    ,W3.GET_CREDIT ");
            stb.append("    ,W2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            if (!"1".equals(param._tutisyoPrintKariHyotei)) {
//                stb.append("            AND (SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV <> '" + TESTCD_GAKUNEN_HYOTEI + "' ");
//                stb.append("              OR SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + TESTCD_GAKUNEN_HYOTEI + "' AND VALUE(W2.PROV_FLG, '') <> '1') ");
//            }
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN T4.NAME1 END AS SLUMP_MARK_NAME1 ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN W3.SCORE END AS SLUMP_SCORE ");
            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
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
            stb.append("    END AS SLUMP_SCORE_KANSAN ");
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
            stb.append("        LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D054' AND T4.NAMECD2 = W3.MARK ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
            stb.append("            AND (W1.SIDOU_INPUT_INF = '1' AND W3.MARK IS NOT NULL ");
            stb.append("              OR W1.SIDOU_INPUT_INF = '2' AND W3.SCORE IS NOT NULL ");
            stb.append("                ) ");
            stb.append("     ) ");
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(", QUALIFIED AS(");
            stb.append("   SELECT ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("       SUM(T1.CREDITS) AS CREDITS ");
            stb.append("   FROM ");
            stb.append("       SCHREG_QUALIFIED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' ");
            stb.append("       AND T1.CREDITS IS NOT NULL ");
            stb.append("       AND EXISTS (SELECT 'X' FROM SCHNO_A WHERE SCHREGNO = T1.SCHREGNO) ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append(" )");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
            if (SEMEALL.equals(param._semester) && "1".equals(param._zouka)) {
                stb.append("    UNION ");
                stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM QUALIFIED ");
            }
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SLUMP ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,T3.GRADE_RANK ");
            stb.append("        ,T3.GRADE_AVG_RANK ");
            stb.append("        ,T3.CLASS_RANK ");
            stb.append("        ,T3.CLASS_AVG_RANK ");
            stb.append("        ,T3.COURSE_RANK ");
            stb.append("        ,T3.COURSE_AVG_RANK ");
            stb.append("        ,T3.MAJOR_RANK ");
            stb.append("        ,T3.MAJOR_AVG_RANK ");
            stb.append("        ,T3.GRADE_AVG ");
            stb.append("        ,T3.GRADE_COUNT ");
            stb.append("        ,T3.GRADE_HIGHSCORE ");
            stb.append("        ,T3.HR_AVG ");
            stb.append("        ,T3.HR_COUNT ");
            stb.append("        ,T3.HR_HIGHSCORE ");
            stb.append("        ,T3.COURSE_AVG ");
            stb.append("        ,T3.COURSE_COUNT ");
            stb.append("        ,T3.COURSE_HIGHSCORE ");
            stb.append("        ,T3.MAJOR_AVG ");
            stb.append("        ,T3.MAJOR_COUNT ");
            stb.append("        ,T3.MAJOR_HIGHSCORE ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,TQ.SUBCLASSCD AS ZOUKA ");
            if ("1".equals(param._zouka)) {
                stb.append("        ,CASE WHEN T33.GET_CREDIT IS NOT NULL OR TQ.CREDITS IS NOT NULL THEN VALUE(T33.GET_CREDIT, 0) + VALUE(TQ.CREDITS, 0) END AS GET_CREDIT ");
            } else {
                stb.append("        ,T33.GET_CREDIT ");
            }
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_MARK_NAME1 ");
            stb.append("        ,K1.SLUMP_SCORE ");
            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");
            stb.append("        ,PERF.PASS_SCORE ");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");
            //資格取得
            stb.append("  LEFT JOIN QUALIFIED TQ ON TQ.SCHREGNO = T1.SCHREGNO AND TQ.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = '" + param._year + "' AND PERF.SEMESTER = T2.SEMESTER AND PERF.TESTKINDCD = T2.TESTKINDCD AND PERF.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("     AND PERF.CLASSCD || '-' || PERF.SCHOOL_KIND || '-' || PERF.CURRICULUM_CD || '-' || PERF.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND (PERF.DIV =  '01' AND PERF.GRADE = '00' ");
            stb.append("       OR PERF.DIV <> '01' AND PERF.GRADE = SCH.GRADE ");
            stb.append("         ) ");
            stb.append("     AND (PERF.DIV     IN ('01','02') AND PERF.COURSECD = '0'          AND PERF.MAJORCD = '000'       AND PERF.COURSECODE = '0000' ");
            stb.append("       OR PERF.DIV NOT IN ('01','02') AND PERF.COURSECD = SCH.COURSECD AND PERF.MAJORCD = SCH.MAJORCD AND PERF.COURSECODE = SCH.COURSECODE ");
            stb.append("         ) ");

            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }

        public String toString() {
            final String slump = StringUtils.defaultString(_slumpScore, StringUtils.defaultString(_slumpScoreKansan, _slumpMark));
            return "(score = " + _score + "" + (null == slump ? "" : " (slump" + slump + ")") + (TESTCD_GAKUNEN_HYOTEI.equals(_testcd) ? (" [getCredit = " + _getCredit + ", compCredit = " + _compCredit + ", provFlg = " + _provFlg + "]") : "") +")";
        }
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
        public String getRank(final Param param) {
            return "2".equals(param._rankDiv) ? _avgRank : _rank;
        }
    }

    private static class ProficiencyScore {
        final String _schregno;
        final String _semtestcd;
        final String _proficiencyname1;
        final String _proficiencySubclassCd;
        final String _subclassName;
        final String _score;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;

        public ProficiencyScore(
                final String schregno,
                final String semtestcd,
                final String proficiencyname1,
                final String proficiencySubclassCd,
                final String subclassName,
                final String score,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank) {
            _schregno = schregno;
            _semtestcd = semtestcd;
            _proficiencyname1 = proficiencyname1;
            _proficiencySubclassCd = proficiencySubclassCd;
            _subclassName = subclassName;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List<Student> studentList
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param);
                if (param._isOutputDebug) {
                    log.info(" proficiency sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

                final boolean isNotPrintRank = "1".equals(param._noPrintRank);
                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        final String semtestcd = rs.getString("SEMTESTCD");
                        final String proficiencyname1 = rs.getString("PROFICIENCYNAME1");
                        final String proficiencySubclassCd = rs.getString("PROFICIENCY_SUBCLASS_CD");
                        final String subclassName = rs.getString("SUBCLASS_NAME");
                        final String score = rs.getString("SCORE");
                        final String avg = rs.getString("AVG");
                        final Rank gradeRank = new Rank(isNotPrintRank ? null : rs.getString("GRADE_RANK"), null, isNotPrintRank ? null : rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), null);
                        final Rank hrRank = new Rank(isNotPrintRank ? null : rs.getString("HR_RANK"), null, isNotPrintRank ? null : rs.getString("HR_COUNT"), rs.getString("HR_AVG"), null);
                        final Rank courseRank = new Rank(isNotPrintRank ? null : rs.getString("COURSE_RANK"), null, isNotPrintRank ? null : rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), null);
                        final Rank majorRank =  new Rank(isNotPrintRank ? null : rs.getString("MAJOR_RANK"), null, isNotPrintRank ? null : rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), null);
                        final ProficiencyScore pscore = new ProficiencyScore(schregno, semtestcd, proficiencyname1, proficiencySubclassCd, subclassName, score, avg, gradeRank, hrRank, courseRank, majorRank);
                        student._proficiencySubclassScoreList.add(pscore);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlScore(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR || T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS SEMTESTCD, ");
            stb.append("     T2.PROFICIENCYNAME1, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD, T1.YEAR, T1.SEMESTER, T1.PROFICIENCYDIV, T1.PROFICIENCYCD, ");
            stb.append("     T9.SUBCLASS_NAME, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.RANK AS GRADE_RANK, ");
            stb.append("     T4.RANK AS HR_RANK, ");
            stb.append("     T5.RANK AS COURSE_RANK, ");
            stb.append("     T6.RANK AS MAJOR_RANK, ");
            stb.append("     T11.COUNT AS GRADE_COUNT, ");
            stb.append("     T14.COUNT AS HR_COUNT, ");
            stb.append("     T15.COUNT AS COURSE_COUNT, ");
            stb.append("     T16.COUNT AS MAJOR_COUNT, ");
            stb.append("     T11.AVG AS GRADE_AVG, ");
            stb.append("     T14.AVG AS HR_AVG, ");
            stb.append("     T15.AVG AS COURSE_AVG, ");
            stb.append("     T16.AVG AS MAJOR_AVG ");
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append(" INNER JOIN PROFICIENCY_MST T2 ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append(" LEFT JOIN PROFICIENCY_SUBCLASS_MST T9 ON T9.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T4 ON  T4.SCHREGNO = T1.SCHREGNO AND T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T4.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T4.PROFICIENCYCD = T1.PROFICIENCYCD AND T4.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T4.RANK_DATA_DIV = T1.RANK_DATA_DIV ");
            stb.append("     AND T4.RANK_DIV = '02' "); // HR
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T5 ON  T5.SCHREGNO = T1.SCHREGNO AND T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T5.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T5.PROFICIENCYCD = T1.PROFICIENCYCD AND T5.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T5.RANK_DATA_DIV = T1.RANK_DATA_DIV ");
            stb.append("     AND T5.RANK_DIV = '03' "); // コース
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T6 ON  T6.SCHREGNO = T1.SCHREGNO AND T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T6.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T6.PROFICIENCYCD = T1.PROFICIENCYCD AND T6.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T6.RANK_DATA_DIV = T1.RANK_DATA_DIV ");
            stb.append("     AND T6.RANK_DIV = '04' "); // 学科
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T11 ON T11.YEAR = T1.YEAR AND T11.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T11.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T11.PROFICIENCYCD = T1.PROFICIENCYCD AND T11.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T11.DATA_DIV = '1' AND T11.GRADE = T3.GRADE ");
            stb.append("     AND T11.AVG_DIV = '01' "); // 学年
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T14 ON T14.YEAR = T1.YEAR AND T14.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T14.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T14.PROFICIENCYCD = T1.PROFICIENCYCD AND T14.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T14.DATA_DIV = '1' AND T14.GRADE = T3.GRADE ");
            stb.append("     AND T14.AVG_DIV = '02' AND T14.HR_CLASS = T3.HR_CLASS "); // HR
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T15 ON T15.YEAR = T1.YEAR AND T15.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T15.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T15.PROFICIENCYCD = T1.PROFICIENCYCD AND T15.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T15.DATA_DIV = '1' AND T15.GRADE = T3.GRADE ");
            stb.append("     AND T15.AVG_DIV = '03' AND T15.COURSECD = T3.COURSECD AND T15.MAJORCD = T3.MAJORCD AND T15.COURSECODE = T3.COURSECODE "); // コース
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T16 ON T16.YEAR = T1.YEAR AND T16.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T16.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T16.PROFICIENCYCD = T1.PROFICIENCYCD AND T16.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND T16.DATA_DIV = '1' AND T16.GRADE = T3.GRADE ");
            stb.append("     AND T16.AVG_DIV = '04' AND T16.COURSECD = T3.COURSECD AND T16.MAJORCD = T3.MAJORCD AND T16.COURSECODE = '0000' "); // 学科
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append("     AND (T9.SUBCLASS_NAME IS NOT NULL OR T1.PROFICIENCY_SUBCLASS_CD = '" + SUBCLASSCD999999 + "') ");
            if ("2".equals(param._rankDiv)) {
                stb.append("     AND T1.RANK_DATA_DIV = '02' "); // 平均点
            } else {
                stb.append("     AND T1.RANK_DATA_DIV = '01' "); // 総合点
            }
            stb.append("     AND T1.RANK_DIV = '01' "); // 学年順位
            return stb.toString();
        }
    }

    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _semester;
        public String _scoreDivName;
        public String _semesterDetail;
        public DateRange _dateRange;
        public boolean _printScore;
        public String semester() {
            return _testcd.substring(0, 1);
        }
        public String scorediv() {
            return _testcd.substring(_testcd.length() - 2);
        }
        public boolean isKarihyotei() {
            return !SEMEALL.equals(semester()) && "09".equals(scorediv());
        }
        public String toString() {
            return "TestItem(" + _testcd + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semester, _semestername, sdate, edate);
        }
        public String toString() {
            return "Semester(" + _semester + ", " + _semestername + ")";
        }
    }

    private static class DateRange {
        final String _key;
        final String _semester;
        final String _name;
        final String _sdate;
        final String _edate;
        TestItem _testitem;
        public DateRange(final String key, final String semester, final String name, final String sdate, final String edate) {
            _key = key;
            _semester = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public boolean isNull() {
            return null == _sdate || null == _edate;
        }
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (!(o instanceof DateRange)) {
                return false;
            }
            final DateRange dr = (DateRange) o;
            return _key.equals(dr._key) && StringUtils.defaultString(_name).equals(StringUtils.defaultString(dr._name)) && rangeEquals(dr);
        }
        public boolean rangeEquals(final DateRange dr) {
            return StringUtils.defaultString(_sdate).equals(StringUtils.defaultString(dr._sdate)) && StringUtils.defaultString(_edate).equals(StringUtils.defaultString(dr._edate));
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _name + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final int classShoworder3,
                final int subclassShoworder3,
                final boolean isSaki, final boolean isMoto) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = new Integer(classShoworder3);
            _subclassShoworder3 = new Integer(subclassShoworder3);
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
        public int compareTo(final SubclassMst mst) {
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    private abstract static class Form {

        List _testcds;
        List<TestItem> _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendSubclassRanges;
        int _formTestCount; // フォームに表示する考査の回数
        boolean _use5testForm; // 5回考査用フォームを使用するか
        TestItem _notPrintTestItem;
        DateRange _notPrintDateRange;
        private Param _param;
        Map _fieldInfoMap = Collections.EMPTY_MAP;

        protected Param param() {
            return _param;
        }

        abstract void init(final List<String> testcdList);
        protected void initDebug() {
            if (_param._isOutputDebug) {
                for (int i = 0; i < _testcds.size(); i++) {
                    log.info(" testcds[" + i + "] = " + _testcds.get(i) + " : " + _testItems.get(i));
                }
                for (int i = 0; i < _attendRanges.length; i++) {
                    log.info(" attendRanges[" + i + "] = " + _attendRanges[i]);
                }
                for (int i = 0; i < _attendSubclassRanges.length; i++) {
                    log.info(" attendSubclassRanges[" + i + "] = " + _attendSubclassRanges[i]);
                }
                log.info(" _formTestCount = " + _formTestCount);
                log.info(" _notPrintTestItem = " + _notPrintTestItem);
                log.info(" _notPrintDateRange = " + _notPrintDateRange);
            }
        }

        abstract void print(final DB2UDB db2, final IPdf ipdf, final Student student);

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

        protected Attendance[] getAttendances(final Student student) {
//        	Attendance sum = null;
            final Attendance[] attendances = new Attendance[_attendRanges.length];
            for (int i = 0; i < _attendRanges.length; i++) {
                final DateRange dateRange = _attendRanges[i];
                if (null != dateRange) {
                    attendances[i] = (Attendance) student._attendMap.get(dateRange._key);
                    if (SEMEALL.equals(dateRange._key)) {
//                    	if (null != sum) {
//                    		attendances[i] = sum;
//                    	}
                        if (null != attendances[i]) {
                            attendances[i]._dateRange = dateRange;
                        }
                        continue;
                    }
                    if (null != attendances[i]) {
                        attendances[i]._dateRange = dateRange;
//                        if (null == sum) {
//                        	sum = new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0);
//                        }
//                        sum = sum.add(attendances[i]);
                    }
                }
            }
            if (_param._isOutputDebug) {
                for (int i = 0; i < attendances.length; i++) {
                    log.info(" att[" + i + "]  = " + attendances[i]);
                }
            }
            return attendances;
        }

//        protected String[] getAttendanceRemarks(final Student student) {
//            final String[] remarks = new String[_attendRanges.length];
//            for (int i = 0; i < _attendRanges.length; i++) {
//                final DateRange dateRange = (DateRange) _attendRanges[i];
//                if (null != dateRange) {
//                    if (SEMEALL.equals(dateRange._key)) {
//                        continue;
//                    }
//                    remarks[i] = student._attendRemarkMap.get(dateRange._key);
//                }
//            }
//            return remarks;
//        }

        protected List<String> getAttendRemarks(final Student student, final Attendance[] attendances, final Attendance att) {

            final List<String> remarks = new ArrayList<String>();

            if (SEMEALL.equals(att._dateRange._key)) {
                if (SEMEALL.equals(param()._semester)) {
                    for (int i2 = 0; i2 < attendances.length; i2++) {
                        final Attendance att2 = attendances[i2];
                        if (null != att2 && null != att2._dateRange._key) {
                            final List<String> hrAttendRemark = getHrAttendRemark(att2._dateRange._key);
                            String attendRemark = null;
                            if (!SEMEALL.equals(att2._dateRange._key)) {
                                attendRemark = student._attendRemarkMap.get(att2._dateRange._key);
                            }
                            if (param()._isOutputDebug) {
                                log.info(" attend remarks (" + att._dateRange._key + ") (" + att2._dateRange._key + ") hrAttendRemark = " + hrAttendRemark);
                                log.info(" attend remarks (" + att._dateRange._key + ") (" + att2._dateRange._key + ") attendRemark = " + attendRemark);
                            }
                            remarks.addAll(hrAttendRemark);
                            remarks.add(attendRemark);
                        }
                    }
                }
            } else {
                final List<String> hrAttendRemark = getHrAttendRemark(att._dateRange._key);
                final String attendRemark = student._attendRemarkMap.get(att._dateRange._key);
                if (param()._isOutputDebug) {
                    log.info(" attend remarks (" + att._dateRange._key + ") hrAttendRemark = " + hrAttendRemark);
                    log.info(" attend remarks (" + att._dateRange._key + ") attendRemark = " + attendRemark);
                }
                remarks.addAll(hrAttendRemark);
                remarks.add(attendRemark);
            }
            return remarks;
        }

        protected List<String> getHrAttendRemark(final String dateRangeKey) {
            final List<String> rtn = new ArrayList<String>();
            if (null == dateRangeKey) {
                return rtn;
            }
            String attendRemarkHrKey = null;
            if (SEMEALL.equals(dateRangeKey)) {
                attendRemarkHrKey = SEMEALL;
            } else {
                final TestItem testItem = param()._testitemMap.get(dateRangeKey);
                if (null != testItem) {
                    attendRemarkHrKey = testItem._semesterDetail;
                }
            }
            final List<String> remarkHr = param()._attendRemarkHrMap.get(attendRemarkHrKey);
            if (null != remarkHr) {
                rtn.addAll(remarkHr);
            }
            return rtn;
        }

        protected List<TestItem> getTestItems(final Param param, final List<String> testcds) {
            final List<TestItem> testitems = new ArrayList<TestItem>();
            for (int i = 0; i < testcds.size(); i++) {
                testitems.add(null);
            }
            for (int j = 0; j < testcds.size(); j++) {
                testitems.set(j, param._testitemMap.get(testcds.get(j)));
            }
            final List<String> notFoundTestcds = new ArrayList<String>();
            for (int i = 0; i < testcds.size(); i++) {
                final String testcd = testcds.get(i);
                if (null == testitems.get(i)) {
                    notFoundTestcds.add(testcd);
                }
            }
            if (!notFoundTestcds.isEmpty()) {
                log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + notFoundTestcds + " / 実際のマスタのコード:" + param._testitemMap.keySet());
            }
            return testitems;
        }

        protected DateRange[] getSemesterDetails(final Param param, final int max) {
            final DateRange[] semesterDetails = new DateRange[max];
            for (int i = 0; i < Math.min(max, param._semesterDetailList.size()); i++) {
                semesterDetails[i] = param._semesterDetailList.get(i);
            }
            return semesterDetails;
        }

        public static String[] get_token(final String strx0, final int f_len, final int f_cnt) {

            if (strx0 == null || strx0.length() == 0) {
                return new String[] {};
            }
            final String strx = StringUtils.replace(StringUtils.replace(strx0, "\r\n", "\n"), "\r", "\n");
            final String[] stoken = new String[f_cnt];        //分割後の文字列の配列
            int slen = 0;                               //文字列のバイト数カウント
            int s_sta = 0;                              //文字列の開始位置
            int ib = 0;
            for (int s_cur = 0; s_cur < strx.length() && ib < f_cnt; s_cur++) {
                //改行マークチェック
                if (strx.charAt(s_cur) == '\n') {
                    stoken[ib] = strx.substring(s_sta, s_cur);
                    ib++;
                    slen = 0;
                    s_sta = s_cur + 1;
                } else{
                    //文字数チェック
                    int blen = 0;
                    try{
                        blen = (strx.substring(s_cur,s_cur+1)).getBytes("MS932").length;
                    } catch (Exception e) {
                        log.fatal("get_token exception", e);
                    }
                    slen += blen;
                    if (slen > f_len) {
                        stoken[ib] = strx.substring(s_sta, s_cur);
                        ib++;
                        slen = blen;
                        s_sta = s_cur;
                    }
                }
            }
            if (slen > 0 && ib < f_cnt) {
                stoken[ib] = strx.substring(s_sta);
            }
            return stoken;

        }

        /**
         * @param source 元文字列
         * @param bytePerLine 1行あたりのバイト数
         * @return bytePerLineのバイト数ごとの文字列リスト
         */
        public static List<String> getTokenList(final String source, final int bytePerLine) {
            if (source == null || source.length() == 0) {
                return Collections.emptyList();
            }
            final List<String> tokenList = new ArrayList<String>();        //分割後の文字列の配列
            int startIndex = 0;                         //文字列の分割開始位置
            int byteLengthInLine = 0;                   //文字列の分割開始位置からのバイト数カウント
            for (int idx = 0; idx < source.length(); idx += 1) {
                //改行マークチェック
                if (source.charAt(idx) == '\r') {
                    continue;
                }
                if (source.charAt(idx) == '\n') {
                    tokenList.add(source.substring(startIndex, idx));
                    byteLengthInLine = 0;
                    startIndex = idx + 1;
                } else {
                    final int sbytelen = getMS932ByteLength(source.substring(idx, idx + 1));
                    byteLengthInLine += sbytelen;
                    if (byteLengthInLine > bytePerLine) {
                        tokenList.add(source.substring(startIndex, idx));
                        byteLengthInLine = sbytelen;
                        startIndex = idx;
                    }
                }
            }
            if (byteLengthInLine > 0) {
                tokenList.add(source.substring(startIndex));
            }
            return tokenList;
        }

        protected static List<String> setTestcd(final List<String> testcdList, final int max, final String[] array) {
            final List<String> testcds;
            log.info(" db testcdList = " + testcdList);
            if (testcdList.isEmpty()) {
                testcds = new ArrayList(Arrays.asList(array));
            } else {
                testcds = new ArrayList();
                for (int i = 0; i < Math.min(testcdList.size(), max); i++) {
                    for (int j = testcds.size(); j <= i; j++) {
                        testcds.add(null);
                    }
                    log.info(" testcds size = " + testcds.size() + " / testcdList = " + testcdList + ", size = " + testcdList.size());
                    testcds.set(i, testcdList.get(i));
                }
                for (int i = max; i < array.length; i++) {
                    for (int j = testcds.size(); j <= i; j++) {
                        testcds.add(null);
                    }
                    testcds.set(i, array[i]);
                }
            }
            return testcds;
        }

        protected List<String> setTestcd3(final List<String> testcdList, final String[] array, int min, final boolean has3Form) {
            List testcds;
            log.info(" db testcdList = " + testcdList);
            if (testcdList.isEmpty()) {
                testcds = new ArrayList(Arrays.asList(array));
            } else {
                testcds = new ArrayList(testcdList);
            }
            testcds.remove(TESTCD_GAKUNEN_HYOKA);
            testcds.remove(TESTCD_GAKUNEN_HYOTEI);
            if (testcds.size() > 5) {
                testcds = testcds.subList(0, 5);
            }
            if ("1".equals(param()._notPrintLastExam)) {
//                log.info(" 最終考査を表記しない:" + testcds.get(testcds.size() - 1));
                if (has3Form) {
                    min = min - 1;
                }
            }
            while (testcds.size() < min) {
                testcds.add(null);
            }
            testcds.add(TESTCD_GAKUNEN_HYOKA);
            testcds.add(TESTCD_GAKUNEN_HYOTEI);
            return testcds;
        }

        // 学年評価、学年評定を除いた表記する考査の数
        protected Collection<TestItem> getTestItemSet(final List<TestItem> testItemList) {
            final Map<String, TestItem> map = new TreeMap<String, TestItem>();
            for (final TestItem ti : testItemList) {
                if (null == ti || "1".equals(param()._notPrintLastExam) && _notPrintTestItem == ti) {
                    continue;
                }
                map.put(ti._testcd, ti);
            }
            if (this instanceof FormD) {
            } else {
                map.remove(TESTCD_GAKUNEN_HYOKA);
            }
            map.remove(TESTCD_GAKUNEN_HYOTEI);
            return map.values();
        }

//        /**
//         * ただし「とりあえず設定できる分」以上にDBで設定していればそちらを使用
//         * @param testcdList DBから取得したテスト種別のリスト
//         * @param max とりあえず設定できる分
//         * @param array 未設定の場合に使用するデフォルトテスト種別
//         */
//        protected static List getTestcd2(final List testcdList, final int max, final int max2, final String[] array) {
//            final List testcds;
//            log.info(" db testcdList2 = " + testcdList);
//            if (testcdList.isEmpty()) {
//                testcds = new ArrayList(Arrays.asList(array));
//            } else {
//                testcds = new ArrayList();
//                for (int i = 0; i < array.length; i++) {
//                    testcds.add(null);
//                }
//                for (int i = 0; i < Math.min(testcdList.size(), testcds.size()); i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, (String) testcdList.get(i));
//                }
//                for (int i = Math.max(testcdList.size(), max); i < array.length; i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, array[i]); // 設定より
//                }
//                for (int i = max2; i < array.length; i++) {
//                    for (int j = testcds.size(); j <= i; j++) {
//                        testcds.add(null);
//                    }
//                    testcds.set(i, array[i]);
//                }
//            }
//            return testcds;
//        }

        public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
//            if (Param._isDemo) {
//                return ipdf.VrsOutn(field, gyo, data);
//            }
            return ipdf.setRecordString(field, gyo, data);
        }

        protected Rank getGroupDivRank(final Score score) {
            return "4".equals(param()._groupDiv) ? score._majorRank : "3".equals(param()._groupDiv) ? score._courseRank : score._gradeRank;
        }

        protected Rank getGroupDivRank(final ProficiencyScore pscore) {
            return "4".equals(param()._groupDiv) ? pscore._majorRank : "3".equals(param()._groupDiv) ? pscore._courseRank : pscore._gradeRank;
        }

        protected String getGroupDivName() {
            return "4".equals(param()._groupDiv) ? "学科順位" : "3".equals(param()._groupDiv) ? "コース順位" : "学年順位";
        }

        protected List<SubClass> getPrintSubclassList(final Student student, final int max) {
            final List<SubClass> printSubclassList = new ArrayList<SubClass>();
            for (final String subclasscd : student._subclassMap.keySet()) {
                final SubClass subClass = student.getSubClass(subclasscd);
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                if (null == subClass || param()._isNoPrintMoto && subClass._mst._isMoto || !param()._isPrintSakiKamoku && subClass._mst._isSaki || param()._d026List.contains(subClass._mst._subclasscd)) {
                    continue;
                }
                printSubclassList.add(subClass);
                if (printSubclassList.size() >= max) {
                    break;
                }
            }
            Collections.sort(printSubclassList);
            return printSubclassList;
        }

        protected void debugSlump(final Student student, final SubClass subclass, final TestItem testItem) {
            final Score score = subclass.getScore(testItem._testcd);
            final String scoreString = null == score ? null : score._score;
            final String slumpScore = null == score ? null : score.getPrintSlump(testItem);
            if (null != slumpScore && "1".equals(param()._printTuishidoAmikakeKakko)) {
                log.info(" slumpScore " + slumpScore + " (score = " + scoreString + ") schregno = " + student._schregno + ", subclasscd = " + subclass._mst._subclasscd + ":" + subclass._mst._subclassname + ", testitem = " + testItem._testcd);
            }
        }

        protected TestItem testItem(final int i) {
            return _testItems.get(i);
        }

        protected void setNotPrintTestItem() {
            final TreeMap<String, List<TestItem>> semesterTestItemListMap = new TreeMap<String, List<TestItem>>();
            for (int i = 0; i < _testItems.size(); i++) {
                if (null != testItem(i) && !SEMEALL.equals(testItem(i)._semester)) {
                    getMappedList(semesterTestItemListMap, testItem(i)._semester).add(testItem(i));
                }
            }
            if (!semesterTestItemListMap.isEmpty() && ("1".equals(param()._notPrintLastExam) || "1".equals(param()._notPrintLastExamScore))) {
                final TreeSet<String> semesterSet = new TreeSet(semesterTestItemListMap.keySet());
                final String maxSemester = semesterSet.last();
                final List<TestItem> maxSemesterTestItemList = getMappedList(semesterTestItemListMap, maxSemester);
                final TestItem item = maxSemesterTestItemList.get(maxSemesterTestItemList.size() - 1);
                if ("1".equals(param()._notPrintLastExam)) {
                    _notPrintTestItem = item;
                    _notPrintDateRange = item._dateRange;
                } else if ("1".equals(param()._notPrintLastExamScore)) {
                    _notPrintTestItem = item;
                }
                log.info(" not print testitem = " + _notPrintTestItem);
                log.info(" not print daterange = " + _notPrintDateRange);
            }
            final Collection<TestItem> testItemSet = getTestItemSet(_testItems);
            loginfo(param(), " testItemSet = " + testItemSet);
            _formTestCount = testItemSet.size();
            _use5testForm = _formTestCount >= 5;
        }

        public void setForm(final IPdf ipdf, final String form) {
            log.info(" form = " + form);
            int rtn = ipdf.VrSetForm(form, 4);
            if (rtn < 0) {
                throw new IllegalArgumentException("フォーム設定エラー:" + rtn);
            }
            if (ipdf instanceof SvfPdf) {
                SvfPdf svfpdf = (SvfPdf) ipdf;
                _fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(svfpdf.getVrw32alp());
            }
        }

        public boolean isKarihyoteiPrintAttendance(final TestItem testitem, final List testItems) {
            if (null == testitem) {
                return false;
            }
            if (!testitem.isKarihyotei()) {
                return true;
            }
            // 以下、仮評定
            // 学期詳細がなければ印字しない
            log.info(" karihyotei " + testitem + " ( semester_detail = " + testitem._semesterDetail + ")");
            if (null == testitem._semesterDetail) {
                return false;
            }
            for (int i = 0; i < testItems.size(); i++) {
                final TestItem ti = (TestItem) testItems.get(i);
                if (null == ti || ti == testitem || null == ti._semesterDetail) {
                    continue;
                }
                if (ti._semesterDetail.equals(testitem._semesterDetail)) {
                    log.warn("他の試験が同じ学期詳細をもつので出欠欄に表示しない: " + testitem);
                    return false;
                }
            }
            return true;
        }

        protected void setWhitespace(final IPdf ipdf) {
            if ("1".equals(param()._noPrintHogosha)) {
                ipdf.VrImageOut("IMG_HOGOSHA", param()._whitespaceImagePath);
            }
            if ("1".equals(param()._noPrintCommunication)) {
                ipdf.VrImageOut("IMG_COM", param()._whitespaceImagePath);
            }
        }

        // パターンＡ
        public static class FormA extends Form {
            void init(final List testcdList) {
                _testcds = setTestcd3(testcdList, new String[] {"1010108", "1990008", "2010108"}, 4, false);
                _testItems = getTestItems(param(), _testcds);
                setNotPrintTestItem();
                final int count;
//                if ("2".equals(param()._hogosha)) {
//                    count = _formTestCount + 1;
//                    _attendRanges = new DateRange[count];
//                    _attendSubclassRanges = new DateRange[count];
////                    final DateRange[] semesterDetails = getSemesterDetails(param(), count);
////                    final DateRange[] semsterDetails = new DateRange[_testItems.size() + 1];
//                    for (int i = 0, attendRangeIdx = 0; i < count - 1; i++) {
////                        if (null != _notPrintDateRange && _notPrintDateRange.rangeEquals(semesterDetails[i])) {
////                            // 表示なし
////                            continue;
////                        }
////                        _attendRanges[i] = semesterDetails[i];
//                        final TestItem testItem = testItem(i);
//                        if (null == testItem || testItem._dateRange.isNull() || null != _notPrintDateRange && _notPrintDateRange.rangeEquals(testItem._dateRange)) {
//                            // 表示なし
//                            continue;
//                        }
//                        if (!testItem.isKarihyotei() || testItem.isKarihyotei() && isKarihyoteiPrintAttendance(testItem, _testItems)) {
//                            _attendRanges[attendRangeIdx] = testItem._dateRange;
//                            _attendRanges[attendRangeIdx]._testitem = testItem;
//                            //_attendRanges[attendRangeIdx] = _testItems.length > i && null ==  testItem(i) ? null : new DateRange(testItem(i)._testcd, testItem(i)._dateRange._sdate, testItem(i)._dateRange._edate);
//                            attendRangeIdx += 1;
//                        }
//                        _attendSubclassRanges[i] = testItem._dateRange;
//                    }
//                    if (null != param().getSemester(SEMEALL)) {
//                        final Semester seme9 = param().getSemester(SEMEALL);
//                        final int i = count - 1;
//                        _attendRanges[i] = new DateRange(SEMEALL, SEMEALL, "合計", seme9._dateRange._sdate, seme9._dateRange._edate);
//                        _attendSubclassRanges[i] = _attendRanges[i];
//                    }
//                } else {
                    count = 1;
                    _attendRanges = new DateRange[count];
                    _attendSubclassRanges = new DateRange[count];
                    if (null != param().getSemester(SEMEALL)) {
                        final Semester seme9 = param().getSemester(SEMEALL);
                        final int i = count - 1;
                        _attendRanges[i] = new DateRange(param()._semester, param()._semester, "", seme9._dateRange._sdate, seme9._dateRange._edate);
                        _attendSubclassRanges[i] = new DateRange(SEMEALL, param()._semester, "", seme9._dateRange._sdate, seme9._dateRange._edate);
                    }
//                }
                if (null != _notPrintTestItem) {
                    for (int i = 0; i < _testItems.size(); i++) {
                        if (testItem(i) == _notPrintTestItem) {
                            _testItems.set(i, null);
                        }
                    }
                }
                initDebug();
            }

            void print(final DB2UDB db2, final IPdf ipdf, final Student student) {
                final String form;
//                final boolean isP9 = getProficiencySubclassCount(student) > 5;
//                if ("2".equals(param()._hogosha)) {
//                    if (isP9) {
//                        form = _use5testForm ? "KNJD186V_A_2_T5_P9.frm" : "KNJD186V_A_2_P9.frm";
//                    } else {
//                        form = _use5testForm ? "KNJD186V_A_2_T5.frm" : "KNJD186V_A_2.frm";
//                    }
//                } else {
//                    if (isP9) {
//                        form = _use5testForm ? "KNJD186V_A_T5_P9.frm" : "KNJD186V_A_P9.frm";
//                    } else {
//                        form = _use5testForm ? "KNJD186V_A_T5.frm" : "KNJD186V_A.frm";
//                    }
//                }
                        form = _use5testForm ? "KNJD186V_A_T5.frm" : "KNJD186V_A.frm";
                setForm(ipdf, form);
                printAHeader(db2, ipdf, student);
                printAProficiency(ipdf, student);
                printAAttendance(ipdf, student);
                printACommunication(ipdf, student);
                printAScore(ipdf, student);
            }

            void printAAttendance(final IPdf ipdf, final Student student) {
                final Attendance[] attendances = getAttendances(student);
//                final String[] remarks = getAttendanceRemarks(student);
                for (int i = 0; i < attendances.length; i++) {
                    final Attendance att = attendances[i];
                    if (null == att) {
                        continue;
                    }
//                    final String remark = remarks[i];
//                    final int pos = SEMEALL.equals(att._dateRange._key) ? (_use5testForm ? 6 : 5) : i + 1;
                    if (null != att && att._lesson != 0) {
//                        if ("2".equals(param()._hogosha)) {
//                            ipdf.VrsOutn("LESSON", pos, String.valueOf(att._lesson)); // 授業日数
//                            ipdf.VrsOutn("SUSPEND", pos, String.valueOf(att._suspend + att._mourning)); // 出停・忌引
//                            ipdf.VrsOutn("PRESENT", pos, String.valueOf(att._mLesson)); // 出席すべき日数
//                            ipdf.VrsOutn("ABROAD", pos, String.valueOf(att._abroad)); // 留学日数
//                            ipdf.VrsOutn("SICK", pos, String.valueOf(att._absent)); // 欠席日数
//                            ipdf.VrsOutn("ATTEND", pos, String.valueOf(att._present)); // 出席日数
//                            if ("1".equals(param()._chikokuSoutaiNasi)) {
//                            } else {
//                                ipdf.VrsOutn("LATE", pos, String.valueOf(att._late)); // 遅刻回数
//                                ipdf.VrsOutn("EARLY", pos, String.valueOf(att._early)); // 早退回数
//                            }
//                        } else {
                            ipdf.VrsOut("LESSON", String.valueOf(att._lesson)); // 授業日数
                            ipdf.VrsOut("SUSPEND", String.valueOf(att._suspend + att._mourning)); // 出停・忌引
                            ipdf.VrsOut("PRESENT", String.valueOf(att._mLesson)); // 出席すべき日数
                            ipdf.VrsOut("ABROAD", String.valueOf(att._abroad)); // 留学日数
                            ipdf.VrsOut("SICK", String.valueOf(att._absent)); // 欠席日数
                            ipdf.VrsOut("ATTEND", String.valueOf(att._present)); // 出席日数
                            if ("1".equals(param()._chikokuSoutaiNasi)) {
                            } else {
                                ipdf.VrsOut("LATE", String.valueOf(att._late)); // 遅刻回数
                                ipdf.VrsOut("EARLY", String.valueOf(att._early)); // 早退回数
                            }
//                        }
                    }

//                    if ("2".equals(param()._hogosha)) {
//                        final int remarklen = getMS932ByteLength(remark);
//                        if (remarklen > 46) {
//                            final String[] token = get_token(remark, 46, 2);
//                            if (null != token) {
//                                for (int j = 0; j < token.length; j++) {
//                                    ipdf.VrsOutn("REMARK3_" + String.valueOf(j + 1), pos, token[j]); // 授業日数
//                                }
//                            }
//                        } else if (remarklen > 30) {
//                            ipdf.VrsOutn("REMARK2", pos, remark); // 授業日数
//                        } else {
//                            ipdf.VrsOutn("REMARK", pos, remark); // 授業日数
//                        }
//                    }
                }
            }

            void printACommunication(final IPdf ipdf, final Student student) {
                final String[] token = get_token(student._communication, 100, 3);
                for (int i = 0; i < token.length; i++) {
                    ipdf.VrsOutn("COMMUNICATION", (i + 1), token[i]);
                }
            }

            void printAScore(final IPdf ipdf, final Student student) {

                int count = 0;
                final List<SubClass> printSubclassList = getPrintSubclassList(student, 20);
                for (final SubClass subClass : printSubclassList) {
                    final int ci = count + 1;
                    loginfo(param(), " subclass = " + subClass);
                    final String subclasscd = subClass._mst._subclasscd;
                    Form.setRecordString(ipdf, "CLASSNAME", ci, subClass._mst._classabbv); // 教科名略称
                    if (null != subClass._mst._subclassname && subClass._mst._subclassname.length() > 12) {
                        Form.setRecordString(ipdf, "SUBCLASSNAME2_1", ci, subClass._mst._subclassname.substring(0, 12)); // 科目名
                        Form.setRecordString(ipdf, "SUBCLASSNAME2_2", ci, subClass._mst._subclassname.substring(12)); // 科目名
                    } else {
                        Form.setRecordString(ipdf, "SUBCLASSNAME", ci, subClass._mst._subclassname); // 科目名
                    }
                    Form.setRecordString(ipdf, "CREDIT", ci, param().getCredits(subclasscd, student._course)); // 単位数
                    for (int ti = 0; ti < _testItems.size(); ti++) {
                        final TestItem item = testItem(ti);
                        if (null == item || TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                            continue;
                        }
                        if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && "9".equals(item._semester) && subClass._mst._isMoto) {
                            continue;
                        }
                        debugSlump(student, subClass, item);
                        final Score score = subClass.getScore(item._testcd);
                        if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd)) {
                            if (null != score) {
//                                final String scoreTui = score.getPrintSlump(item);
                                Form.setRecordString(ipdf, "SCORE" + "9_990008", ci, score._score); // 評価
                            }
                            final Score scoreHyotei = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                            if (null != scoreHyotei) {
                                if (!"1".equals(scoreHyotei._provFlg) || "1".equals(scoreHyotei._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei)) {
                                    Form.setRecordString(ipdf, "SCORE9_990009", ci, scoreHyotei._score); // 評定
                                }
                                Form.setRecordString(ipdf, "MAX_VALUE9_990008", ci, scoreHyotei.getGetCredit(param())); // 修得単位
                            }
                        } else {
                            final String[] fld;
                            if (_use5testForm) {
                                fld = new String[] {"1_010108", "1_990008", "2_010108", "2_990008", "3_020108"};
                            } else {
                                fld = new String[] {"1_010108", "1_990008", "2_010108", "2_990008"};
                            }
                            if (ti >= fld.length) {
                                continue;
                            }
                            final String f = fld[ti];
                            if (null != _notPrintTestItem && _notPrintTestItem.equals(item)) {
                                // 表示無し
                            } else if (null != score) {
//                                final String scoreTui = score.getPrintSlump(item);
                                if (StringUtils.defaultString(item._testcd).length() > 2 && !"09".equals(item.scorediv())) {
                                    Form.setRecordString(ipdf, "AVERAGE" + f, ci, sishaGonyu(getGroupDivRank(score)._avg)); // 平均
                                    Form.setRecordString(ipdf, "MAX_VALUE" + f, ci, "2".equals(param()._maxOrSidou) ? score.getPrintSlump(item) : getGroupDivRank(score)._highscore); // 最高
                                }
                                Form.setRecordString(ipdf, "SCORE" + f, ci, score._score); // 評価
                            }
                        }
                    }
//                    final String key = "2".equals(param()._hogosha) ? SEMEALL : param()._semester;
                    final String key = SEMEALL;
                    final SubclassAttendance sa = subClass.getAttendance(key);
                    // log.debug(" sa = " + sa);
                    if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && subClass._mst._isMoto) {
                        // 表示無し
                    } else if (null != sa) {
                        Form.setRecordString(ipdf, "KEKKA", ci, getAbsentStr(param(), sa._sick, true)); // 欠課時数
                    }
                    ipdf.VrEndRecord();
                    count += 1;
                }
                if (count == 0) {
                    for (int i = count; i <= 1; i++) {
                        final int ci = i + 1;
                        ipdf.setRecordString("CLASSNAME", ci, i % 2 == 0 ? "" : "　"); // 教科名
                        ipdf.VrEndRecord();
                    }
                }
//                for (int i = count; i <= 17; i++) {
//                    final int ci = i + 1;
//                    ipdf.setRecordString("CLASSNAME", ci, i % 2 == 0 ? "" : "　"); // 教科名
//                    ipdf.setRecordString("SUBCLASSNAME", ci, "fuga" + String.valueOf(ci)); // 科目名
//                    ipdf.endRecord();
//                }
            }

            int getProficiencySubclassCount(final Student student) {
                final Set<String> subclasses = new TreeSet<String>();
                for (final ProficiencyScore pscore : student._proficiencySubclassScoreList) {
                    subclasses.add(pscore._proficiencySubclassCd);
                }
                subclasses.remove(SUBCLASSCD999999);
                return subclasses.size();
            }

            void printAProficiency(final IPdf ipdf, final Student student) {
                final Map<String, String> testnames = new TreeMap<String, String>();
                final Map<String, String> subclassnames = new TreeMap<String, String>();
                final Set<String> subclasses = new TreeSet<String>();
                final Map<String, ProficiencyScore> scoreMap = new HashMap<String, ProficiencyScore>();
                for (final ProficiencyScore pscore : student._proficiencySubclassScoreList) {
                    testnames.put(pscore._semtestcd, pscore._proficiencyname1);
                    subclasses.add(pscore._proficiencySubclassCd);
                    scoreMap.put(pscore._semtestcd + ":" + pscore._proficiencySubclassCd, pscore);
                    subclassnames.put(pscore._proficiencySubclassCd, pscore._subclassName);
                }
                if (param()._isOutputDebug) {
                    log.info(" proficiency subclass = " + subclassnames);
                }

                int ti = 0;
                for (final String testcd : testnames.keySet()) {
                    final String testname = testnames.get(testcd);
                    final String line = String.valueOf(ti + 1);
                    ipdf.VrsOut("PROF_TESTNAME" + line, testname); // テスト名（実力テスト）

                    final List<String> subclassList = new ArrayList<String>(subclasses);
                    for (int subi = 0; subi < subclassList.size(); subi++) {
                        final String subclassCd = subclassList.get(subi);

                        final int sline = subi + 1;
                        ipdf.VrsOutn("PROF_SUBCLASS", sline, subclassnames.get(subclassCd)); // 科目（実力テスト）
                        final ProficiencyScore pscore = scoreMap.get(testcd + ":" + subclassCd);
                        if (null == pscore) {
                            continue;
                        }
                        if (SUBCLASSCD999999.equals(subclassCd)) {
                            ipdf.VrsOut("PROF_TOTALSCORE" + line, pscore._score); // 総点（実力テスト）
                            ipdf.VrsOut("PROF_AVERAGE" + line, sishaGonyu(pscore._avg)); // 人数（実力テスト）
                            Rank rank = getGroupDivRank(pscore);
                            ipdf.VrsOut("PROF_RANK" + line, rank._rank); // 順位（実力テスト）
                            ipdf.VrsOut("PROF_CNT" + line, rank._count); // 人数（実力テスト）
                        } else {
                            if (null == subclassnames.get(subclassCd)) {
                                continue;
                            }
                            ipdf.VrsOutn("PROF_SCORE" + line, sline, pscore._score); // 得点（実力テスト）
                        }
                    }
                    ti++;
                }
            }

            void printAHeader(final DB2UDB db2, final IPdf ipdf, final Student student) {
                ipdf.VrsOut("MAJORNAME", student._majorname); // 学科名
                ipdf.VrsOut("HR_NAME", student._hrName); // クラス名
                ipdf.VrsOut("NAME", student._name); // 氏名
                ipdf.VrsOut("NENDO", param()._nendo + "　" + StringUtils.defaultString(param()._title, "通知票")); // 年度
                ipdf.VrsOut("ATTENDNO", student._attendno); // 出席番号
//                final int maxLine2 = 4;
//                for (int j = 0; j < maxLine2; j++) {
//                    final int line = j + 1;
//                    ipdf.setStringn("COMMUNICATION", line, null); // 通信欄
//                }
                ipdf.VrsOut("STAFFNAME1", param()._jobName + "　" + param()._principalName); // 校長
                ipdf.VrsOut("STAFFNAME2", param()._hrJobName + "　" +  student._staffName); // 担任
                ipdf.VrsOut("SCHOOLNAME", param()._schoolName); // 学校名
                if ("1".equals(param()._chikokuSoutaiNasi)) {
                } else {
                    ipdf.VrsOut("LATE_NAME", "遅刻回数");
                    ipdf.VrsOut("EARLY_NAME", "早退回数");
                }
                ipdf.VrsOut("SEMESTER", param().getSemestername(param()._semester)); // 学期
                final String ssemester = StringUtils.defaultString(param().getSemestername(param()._semester));
                final String smajorname = StringUtils.defaultString(student._majorname);
                final String shrname = StringUtils.defaultString(student._hrName);
                final String sattendno = StringUtils.defaultString(student._attendno);
                final String sname = StringUtils.defaultString(student._name);
                ipdf.VrsOut("SCH_INFO1", smajorname + "　　" + shrname + sattendno + "番　" + sname);
                ipdf.VrsOut("SCH_INFO2", "・保護者から（" + param()._nendo + ssemester + StringUtils.defaultString(param()._title, "通知票") + "　" + shrname + sattendno + "番　" + sname + "）");
                final String[] fields;
                if (_use5testForm) {
                    fields = new String[] {"1_010108", "1_990008", "2_010108", "2_990008", "3_020108"};
                } else {
                    fields = new String[] {"1_010108", "1_990008", "2_010108", "2_990008"};
                }
                final SubClass subClass999999 = student.getSubClass(SUBCLASSCD999999);
                for (int i = 0; i < _testItems.size(); i++) {
                    final TestItem testitem = testItem(i);
                    if (null == testitem) {
                        continue;
                    }
                    final String f;
                    if (TESTCD_GAKUNEN_HYOKA.equals(testitem._testcd)) {
                        f = "9_990008";
                    } else if (TESTCD_GAKUNEN_HYOTEI.equals(testitem._testcd)) {
                        f = "9_990009";
                    } else {
                        if (i >= fields.length) {
                            continue;
                        }
                        f = fields[i];
                    }
                    String testname = "";
                    if (!"1".equals(param()._noPrintSemesternameInTestname) && !SEMEALL.equals(testitem._semester)) {
                        testname += StringUtils.defaultString(param().getSemestername(testitem._semester));
                    }
                    testname += StringUtils.defaultString(testitem._testitemname);
                    if (testname.length() <= 4) {
                        ipdf.VrsOut("TESTNAME" + f, testname); // テスト名
                    } else {
                        ipdf.VrsOut("TESTNAME" + f + "_2", testname.substring(0, Math.min(testname.length(), 6))); // テスト名
                    }
                    if (testitem._printScore) {
                        ipdf.VrsOut("KETTEN_CNT" + f, student.getKettenSubclassCount(param(), testitem)); // 欠点科目数
                    }
                    ipdf.VrsOut("TESTNAME" + f + "_HYOKA", StringUtils.defaultString(testitem._scoreDivName, "評価"));
                    if (TESTCD_GAKUNEN_HYOKA.equals(testitem._testcd)) {
                        ipdf.VrsOut("TESTNAME" + f + "_AVE", "評定");
                        ipdf.VrsOut("TESTNAME" + f + "_MAX2", "修得単位数");
                    } else {
                        ipdf.VrsOut("TESTNAME" + f + "_AVE", "平均");
                        ipdf.VrsOut("TESTNAME" + f + "_MAX", "2".equals(param()._maxOrSidou) ? "追指導" : "最高点");
                    }
                    if (null != subClass999999 && null != subClass999999.getScore(testitem._testcd)) {
                        final Score score = subClass999999.getScore(testitem._testcd);
                        if (TESTCD_GAKUNEN_HYOTEI.equals(testitem._testcd) && ("1".equals(score._provFlg) && !"1".equals(param()._tutisyoPrintKariHyotei))) {
                        } else {
                            ipdf.VrsOut("TOTAL_SCORE" + f, score._score); // 合計
                            ipdf.VrsOut("SCORE_AVERAGE" + f, sishaGonyu(score._avg)); // 平均
                            ipdf.VrsOut("HR_CLASS_RANK" + f, score._hrRank.getRank(param())); // 組順位
                            ipdf.VrsOut("HR_CLASS_RANK" + f + "_2", score._hrRank._count); // 組順位 母集団
                            ipdf.VrsOut("GRADE_RANK" + f, getGroupDivRank(score).getRank(param())); // 学年順位or学科順位
                            ipdf.VrsOut("GRADE_RANK" + f + "_2", getGroupDivRank(score)._count); // 学年順位or学科順位 母集団
                        }
                    }
                }
//                if ("2".equals(param()._hogosha)) {
//                    for (int i = 0; i < _attendRanges.length; i++) {
//                        if (null == _attendRanges[i]) {
//                            continue;
//                        }
//                        final String f;
//                        if (SEMEALL.equals(_attendRanges[i]._key)) {
//                            f = "9_990008";
//                        } else {
//                            if (i >= fields.length) {
//                                continue;
//                            }
//                            f = fields[i];
//                        }
//                        String name = "";
//                        if (SEMEALL.equals(_attendRanges[i]._semester)) {
//                        	name = "合計";
//                        } else {
//                        	if (!"1".equals(param()._noPrintSemesternameInTestname)) {
//                        		name += StringUtils.defaultString(param().getSemestername(_attendRanges[i]._semester));
//                        	}
//                        	name += StringUtils.defaultString(_attendRanges[i]._name);
//                        }
//						ipdf.VrsOut("TESTNAME" + f + (getMS932ByteLength(name) >= 8 ? "_4" : "_3"), name); // テスト名
//                    }
//                }

                ipdf.VrsOut("ITEMNAME", getGroupDivName()); // 項目
                // ipdf.setString("TOTAL_KEKKA", null); // 合計
                ipdf.VrsOut("MOCK_NAME", "・" + StringUtils.defaultString(param()._h508Name1, "実力テスト"));
                if (SEMEALL.equals(param()._semester)) {
                    ipdf.VrsOut("GET_CREDIT", student.getTotalGetCredit(param(), "1".equals(param()._addPastCredit)));
                }
                setWhitespace(ipdf);
            }
        }

        // パターンＢ
        public static class FormB extends Form {
            boolean _isOnedayAttendancePrintTest;
            void init(final List<String> testcdList) {
                if (SEMEALL.equals(param()._semester)) {
                    _testcds = setTestcd(testcdList, 1, new String[] { TESTCD_GAKUNEN_HYOTEI});
                } else {
                    _testcds = setTestcd(testcdList, 1, new String[] { param()._semester + "990008", TESTCD_GAKUNEN_HYOTEI});
                }
                _testItems = getTestItems(param(), _testcds);

                if (testcdList.isEmpty()) {
                    _isOnedayAttendancePrintTest = false;
                    final String[] semes = new String[] {"1", "2", SEMEALL};
                    _attendRanges = new DateRange[semes.length];
                    for (int i = 0; i < semes.length; i++) {
                        if (null != param().getSemester(semes[i])) {
                            final Semester seme = param().getSemester(semes[i]);
                            _attendRanges[i] = new DateRange(semes[i], semes[i], seme._semestername, seme._dateRange._sdate, seme._dateRange._edate);
                        }
                    }
                } else {
                    _isOnedayAttendancePrintTest = true;

                    final List attendTestcds = setTestcd(testcdList, 5, new String[] { null, null, null, null, TESTCD_GAKUNEN_HYOTEI});
                    final List<TestItem> attendTestItems = getTestItems(param(), attendTestcds);

                    _attendRanges = new DateRange[attendTestItems.size() + 1];
                    for (int i = 0; i < attendTestItems.size(); i++) {
                        final TestItem attendTestItem = attendTestItems.get(i);
                        if (null != attendTestItem && !attendTestItem._dateRange.isNull()) {
                            _attendRanges[i] = attendTestItem._dateRange;
                        }
                    }
                    final Semester seme = param().getSemester(SEMEALL);
                    if (null != seme) {
                        _attendRanges[_attendRanges.length - 1] = seme._dateRange;
                    }
                }
                _attendSubclassRanges = new DateRange[1];
                if (null != param().getSemester(param()._semester)) {
                    final Semester seme = param().getSemester(param()._semester);
                    _attendSubclassRanges[0] = new DateRange(param()._semester, param()._semester, seme._semestername, seme._dateRange._sdate, seme._dateRange._edate);
                }
                initDebug();
            }

            void print(final DB2UDB db2, final IPdf ipdf, final Student student) {
                final String form = "KNJD186V_B.frm";
                setForm(ipdf, form);

                printBHeader(db2, ipdf, student);
                printBAttendance(ipdf, student);
                printBCommunication(ipdf, student);
                printBScore(ipdf, student);
            }

            void printBHeader(final DB2UDB db2, final IPdf ipdf, final Student student) {
                ipdf.VrsOut("ATTENDNO", student._attendno); // 出席番号
                ipdf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
                ipdf.VrsOut("SCHOOLNAME", param()._schoolName); // 学校名
                ipdf.VrsOut("STAFFNAME1", param()._jobName + "　" + param()._principalName); // 校長
                ipdf.VrsOut("STAFFNAME2", param()._hrJobName + "　" +  student._staffName); // 担任
                ipdf.VrsOut("NAME", student._name); // 氏名
                ipdf.VrsOut("HR_NAME", student._hrName); // クラス名
                ipdf.VrsOut("HR_CLASS_NO", StringUtils.defaultString(student._hrName) + StringUtils.defaultString(student._attendno) + "番");
                if ("1".equals(param()._chikokuSoutaiNasi)) {
                } else {
                    ipdf.VrsOut("LATE_NAME", "遅刻回数");
                    ipdf.VrsOut("EARLY_NAME", "早退回数");
                }

                if (NumberUtils.isDigits(student._entyear)) {
                    final String nendon = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, student._entyear + "-04-01"))[1];
                    ipdf.VrsOut("ENT_YEAR", nendon); // 入学年度
                }
                ipdf.VrsOut("NENDO", param()._nendo + "　" + StringUtils.defaultString(param()._title, "通知表")); // 年度
                if (SEMEALL.equals(param()._semester)) {
                    ipdf.VrsOut("NENDO_CREDIT", student.getTotalGetCredit(param(), false)); // 今年度の修得単位数
                    ipdf.VrsOut("GRAD_CREDIT", student.getTotalGetCredit(param(), true)); // 卒業単位数の合計
                }

                if (_isOnedayAttendancePrintTest) {
                    final int max = 5;
                    for (int i = 0; i < _attendRanges.length; i++) {
                        DateRange dr = _attendRanges[i];
                        if (null != dr) {
                            final int line = SEMEALL.equals(dr._semester) ? max : i + 1;
                            String name = "";
                            if (SEMEALL.equals(_attendRanges[i]._semester)) {
                                name = "合計";
                            } else {
                                if (!"1".equals(param()._noPrintSemesternameInTestname)) {
                                    name += StringUtils.defaultString(param().getSemestername(_attendRanges[i]._semester));
                                }
                                name += StringUtils.defaultString(dr._name);
                            }
                            ipdf.VrsOutn("SEMESTER", line, ""); // 学期
                            ipdf.VrsOutn("SEMESTER_2" , line, ""); // 学期
                            ipdf.VrsOutn("SEMESTER" + (getMS932ByteLength(name) > 8 ? "_2" : "") , line, name); // 学期
                        }
                    }
                } else {
                    final String[] semes = new String[] {"1", "2", SEMEALL};
                    for (int i = 0; i < semes.length; i++) {
                        final int line = i + 1;
                        final String name;
                        if (SEMEALL.equals(semes[i])) {
                            name = "合計";
                        } else {
                            name = param().getSemestername(semes[i]);
                        }
                        ipdf.VrsOutn("SEMESTER", line, name); // 学期
                    }
                }
                setWhitespace(ipdf);

                final String ssemester = StringUtils.defaultString(param().getSemestername(param()._semester));
                final String shrname = StringUtils.defaultString(student._hrName);
                final String sattendno = StringUtils.defaultString(student._attendno);
                final String sname = StringUtils.defaultString(student._name);
                ipdf.VrsOut("SCH_INFO2", "・保護者から（" + param()._nendo + ssemester + StringUtils.defaultString(param()._title, "通知票") + "　" + shrname + sattendno + "番　" + sname + "）");
            }

            void printBAttendance(final IPdf ipdf, final Student student) {
                final int max = 5;
                final Attendance[] attendances = getAttendances(student);
                for (int i = 0; i < attendances.length; i++) {
                    final Attendance att = attendances[i];
                    if (null == att || att._lesson == 0) {
                        continue;
                    }
                    final int line;
                    if (_isOnedayAttendancePrintTest && null != att._dateRange && SEMEALL.equals(att._dateRange._semester)) {
                        line = max;
                    } else {
                        line = i + 1;
                    }
                    ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引
                    ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                    ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席しなければならない日数
                    ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                    ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    if ("1".equals(param()._chikokuSoutaiNasi)) {
                    } else {
                        ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                        ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                    }

                    if (!"1".equals(param()._noPrintAttendremark)) {
                        final String remark = mkString(getAttendRemarks(student, attendances, att), " ");
                        final int keta = getMS932ByteLength(remark);
                        final String field;
                        if (keta <= 30) {
                            field = "ATTENDREMARK";
                        } else if (keta <= 40) {
                            field = "ATTENDREMARK2";
                        } else if (keta <= 50) {
                            field = "ATTENDREMARK3";
                        } else {
                            field = "ATTENDREMARK4_1";
                        }
                        ipdf.VrsOutn(field, line, remark); //出欠の記録の備考
                    }
                }
            }

            void printBCommunication(final IPdf ipdf, final Student student) {
                final String[] token = get_token(student._communication, 100, 3);
                for (int i = 0; i < token.length; i++) {
                    ipdf.VrsOutn("COMMUNICATION", (i + 1), token[i]);
                }
            }

            void printBScore(final IPdf ipdf, final Student student) {
                int count = 0;
                String oldclasscd = null;
                final List<SubClass> printSubclassList = getPrintSubclassList(student, 20);
                for (final SubClass subClass : printSubclassList) {
                    final int ci = count + 1;
                    loginfo(param(), " subclass = " + subClass);
                    final String subclasscd = subClass._mst._subclasscd;
                    Form.setRecordString(ipdf, "GRP", ci, subclasscd.substring(0, 2));
                    if (null == oldclasscd || !oldclasscd.equals(subclasscd.substring(0, 2))) {
                        Form.setRecordString(ipdf, "CLASSNAME" + (getMS932ByteLength(subClass._mst._classname) > 10 ? "_2" : ""), ci, subClass._mst._classname); // 教科名
                        oldclasscd = subclasscd.substring(0, 2);
                    }
                    if (getMS932ByteLength(subClass._mst._subclassname) > 16) {
                        if (getMS932ByteLength(subClass._mst._subclassname) > 24) {
                            final List<String> tokenList = getTokenList(subClass._mst._subclassname, 24);
                            for (int i = 0; i < tokenList.size(); i++) {
                                Form.setRecordString(ipdf, "SUBCLASSNAME2_" + String.valueOf(i + 1), ci, tokenList.get(i)); // 科目名
                            }
                        } else {
                            Form.setRecordString(ipdf, "SUBCLASSNAME1", ci, subClass._mst._subclassname); // 科目名
                        }
                    } else {
                        Form.setRecordString(ipdf, "SUBCLASSNAME", ci, subClass._mst._subclassname); // 科目名
                    }
                    Form.setRecordString(ipdf, "CREDIT", ci, param().getCredits(subclasscd, student._course)); // 単位数
                    if (null != testItem(0)) {
                        if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && subClass._mst._isMoto) {
                        } else {
                            final TestItem item = testItem(0);
                            debugSlump(student, subClass, item);
                            final Score score = subClass.getScore(item._testcd);
                            if (null != score) {
                                final String scoreTui = score.getPrintSlump(item);
                                if ("1".equals(param()._printTuishidoAmikakeKakko) && null != scoreTui) {
                                    ipdf.VrAttribute("SCORE_TUI", ATTRIBUTE_TUISHIDO);
                                    Form.setRecordString(ipdf, "SCORE_TUI", ci, StringUtils.defaultString(score._score) + "(" + scoreTui + ")"); // 評定
                                } else {
                                    Form.setRecordString(ipdf, "SCORE", ci, score._score); // 評定
                                }
                            }
                            final SubclassAttendance sa = subClass.getAttendance(param()._semester);
                            if (null != sa && null != sa._sick) {
                                Form.setRecordString(ipdf, "KEKKA", ci, getAbsentStr(param(), sa._sick, true)); // 欠課時数
                            }
                        }
                    }
                    if (SEMEALL.equals(param()._semester)) {
                        final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                        if (null != score && (!"1".equals(score._provFlg) || "1".equals(score._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei))) {
                            Form.setRecordString(ipdf, "STANDARD_CREDIT", ci, score.getGetCredit(param())); // 標準単位数
                        }
                    }
                    //ipdf.setString("REMARK", null); // 備考
                    ipdf.VrEndRecord();
                    count++;
                }
                if (count == 0) {
                    for (int i = count; i <= 1; i++) {
                        final int ci = i + 1;
                        Form.setRecordString(ipdf, "SUBCLASSNAME", ci, "　"); // 教科名
                        ipdf.VrEndRecord();
                    }
                }
            }
        }

        // パターンＣ
        public static class FormC extends Form {

            int _attendSemeAllPos;

            void init(final List<String> testcdList) {
                _testcds = setTestcd3(testcdList, new String[] {"1010108", "1020108", "2010108", "2020108"}, 4, true);
                _testItems = getTestItems(param(), _testcds);

                _attendRanges = new DateRange[_testItems.size()];
                _attendSubclassRanges = new DateRange[_testItems.size()];
                final int last = _testItems.size() - 1;
                for (int i = 0, attendRangeIdx = 0; i < last; i++) {
                    final TestItem item = testItem(i);
                    if (null != item && !item._dateRange.isNull()) {
                        final DateRange dr = new DateRange(item._testcd, item._semester, item._testitemname, item._dateRange._sdate, item._dateRange._edate);
                        if (!item.isKarihyotei() || item.isKarihyotei() && isKarihyoteiPrintAttendance(item, _testItems)) {
                            _attendRanges[attendRangeIdx] = dr;
                            _attendRanges[attendRangeIdx]._testitem = item;
                            attendRangeIdx += 1;
                        }
                        _attendSubclassRanges[i] = dr;
                    }
                }
                if (null != param().getSemester(SEMEALL)) {
                    final Semester seme9 = param().getSemester(SEMEALL);
                    _attendRanges[last] = new DateRange(seme9._semester, seme9._semester, "", seme9._dateRange._sdate, seme9._dateRange._edate);
                    _attendSubclassRanges[last] = _attendRanges[last];
                }
                setNotPrintTestItem();
                initDebug();
            }

            void print(final DB2UDB db2, final IPdf ipdf, final Student student) {
                final String _form;
                loginfo(param(), " testItems = " + _testItems);
//                if (_formTestCount <= 3) {
//                    _attendSemeAllPos = 4;
//                    _form = "KNJD186V_C2.frm"; // 考査3列
//                } else if (_use5testForm) {
                    _attendSemeAllPos = 5;
                    _form = "KNJD186V_C.frm"; // 考査5列 <- "KNJD186V_C3.frm"フォーム名変更
//                } else {
//                    _attendSemeAllPos = 5;
//                    _form = "KNJD186V_C.frm"; // 考査4列
//                }
                setForm(ipdf, _form);

                printCHeader(db2, ipdf, student);
                printCAddress(ipdf, student);
                printCAttendance(ipdf, student);
                printCCommunication(ipdf, student);
                printCScore(ipdf, student);
            }

            void printCAddress(final IPdf ipdf, final Student student) {
                final Address address = student._address;
                if (null == address) {
                    return;
                }
//                ipdf.setString("ZIPCD", address._zipcd); // 郵便番号
                if (!StringUtils.isBlank(address._zipcd)) {
                    ipdf.VrsOut("ZIPCD", "〒" + address._zipcd); // 郵便番号
                }
                final boolean useAddress2 = getMS932ByteLength(address._address1) > 40 || getMS932ByteLength(address._address2) > 40;
                ipdf.VrsOut(useAddress2 ? "ADDR1_2" : "ADDR1", address._address1); // 住所
                ipdf.VrsOut(useAddress2 ? "ADDR2_2" : "ADDR2", address._address2); // 住所
                if (null != address._addressee) {
                    ipdf.VrsOut(getMS932ByteLength(address._addressee) > 20 ? "ADDRESSEE2" : "ADDRESSEE", address._addressee + "　様"); // 受取人2
                }
            }

            void printCAttendance(final IPdf ipdf, final Student student) {
                final Attendance[] attendances = getAttendances(student);
                for (int i = 0; i < attendances.length; i++) {
                    final Attendance att = attendances[i];
                    if (null == att || att._lesson == 0) {
                        continue;
                    }
                    if (_notPrintDateRange == att._dateRange) {
                        continue;
                    }
                    final int line = SEMEALL.equals(att._dateRange._key) ? _attendSemeAllPos : i + 1;
                    ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引等日数
                    ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
                    ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                    ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                    if ("1".equals(param()._chikokuSoutaiNasi)) {
                    } else {
                        ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                        ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                    }
                }
            }


            void printCCommunication(final IPdf ipdf, final Student student) {
                final String[] token = get_token(student._communication, 50, 6);
                for (int i = 0; i < token.length; i++) {
                    ipdf.VrsOutn("COMMUNICATION", (i + 1), token[i]);
                }
            }

            void printCHeader(final DB2UDB db2, final IPdf ipdf, final Student student) {
                ipdf.VrsOut("SCHOOLNAME", param()._schoolName); // 学校名
                ipdf.VrsOut("STAFFNAME1", param()._jobName + "　" + param()._principalName); // 校長
                ipdf.VrsOut("STAFFNAME2", param()._hrJobName + "　" +  student._staffName); // 担任
                if (null != student.getHrAttendNo(param())) {
                    ipdf.VrsOut("HR_ATTNO_NAME", "(" + student.getHrAttendNo(param()) + ")"); // 年組番・氏名
                }
//                ipdf.setString("MAJORNAME", student._majorname); // 学科名
//                ipdf.setString("HR_NAME", student._hrName); // クラス名
//                ipdf.setString("ATTENDNO", student._attendno); // 出席番号
//                ipdf.setString("NAME", student._name); // 氏名
                final String smajorname = StringUtils.defaultString(student._majorname);
                final String shrname = StringUtils.defaultString(student._hrName);
                final String sattendno = StringUtils.defaultString(student._attendno);
                final String sname = StringUtils.defaultString(student._name);
                ipdf.VrsOut("SCH_INFO", smajorname + "  " + shrname + sattendno + "番　　" + sname);
                ipdf.VrsOut("NENDO", param()._nendo); // 年度
                ipdf.VrsOut("TITLE", StringUtils.defaultString(param()._title, "通 知 表")); // 年度
                ipdf.VrsOut("RANK_NAME", getGroupDivName()); // 項目

                final String ssemester = StringUtils.defaultString(param().getSemestername(param()._semester));
                ipdf.VrsOut("SCH_INFO2", "・保護者から（" + param()._nendo + ssemester + StringUtils.defaultString(param()._title, "通知票") + "　" + shrname + sattendno + "番　" + sname + "）");


                if (SEMEALL.equals(param()._semester)) {
                    ipdf.VrsOut("GET_CREDIT", student.getTotalGetCredit(param(), "1".equals(param()._addPastCredit))); // 修得単位数
                }

//                final int maxLine2 = 8;
//                for (int j = 0; j < maxLine2; j++) {
//                    final int line = j + 1;
//                    ipdf.setStringn("COMMUNICATION", line, null); // 通信欄
//                }
                for (int i = 1; i <= 9; i++) {
                    final String si = String.valueOf(i);
                    if ("1".equals(param()._notPrintSubeki)) {
                        ipdf.VrsOut("KEKKA_NAME" + si + "_2", "欠課時数");
                    } else {
                        ipdf.VrsOut("KEKKA_NAME" + si + "_1", "欠課時数");
                        ipdf.VrsOut("KEKKA_NAME" + si + "_2", "／出席す");
                        ipdf.VrsOut("KEKKA_NAME" + si + "_3", "べき時数");
                    }
                }

                final SubClass subclass999999 = student.getSubClass(SUBCLASSCD999999);
                for (int ti = 0; ti < _testItems.size(); ti++) {
                    if (null != testItem(ti)) {
                        final boolean isGakunenHyotei = TESTCD_GAKUNEN_HYOTEI.equals(testItem(ti)._testcd);
                        final boolean isGakunenHyoka = TESTCD_GAKUNEN_HYOKA.equals(testItem(ti)._testcd);
                        boolean printGakunenHyokaHyoteiSumAvg = true;
                        if ((isGakunenHyoka || isGakunenHyotei) && "1".equals(param()._noPrintGakunenHyokaHyoteiSumAvg)) {
                            printGakunenHyokaHyoteiSumAvg = false;
                        }
                        final String line = isGakunenHyotei ? "9" : String.valueOf(ti + 1);
                        final String line2 = isGakunenHyoka ? "9" : line;
                        final TestItem testItem = testItem(ti);
                        ipdf.VrsOut("HYOKA" + line, StringUtils.defaultString(testItem._scoreDivName, "評価"));

                        String testname = "";
                        if (!"1".equals(param()._noPrintSemesternameInTestname) && (null == testItem._dateRange || !SEMEALL.equals(testItem._dateRange._semester))) {
                            testname += StringUtils.defaultString(param().getSemestername(testItem._semester));
                        }
                        testname += StringUtils.defaultString(testItem._testitemname);
                        if ("1".equals(param()._noPrintSemesternameInTestname)) {
                            ipdf.VrsOut("TESTNAME" + line, testname); // テスト名
                        } else {
                            ipdf.VrsOut("TESTNAME" + line + "_2", testname); // テスト名
                        }
                        if (!isGakunenHyotei && testItem._printScore) {
                            ipdf.VrsOut("KETTEN_CNT" + line2, student.getKettenSubclassCount(param(), testItem)); // 欠点科目数
                        }
                        if (null != subclass999999._scoreMap.get(testItem(ti)._testcd)) {
                            final Score score = subclass999999._scoreMap.get(testItem(ti)._testcd);
                            final Rank rank = getGroupDivRank(score);
                            if (isGakunenHyotei) {
                                if (!student.hasKari(param()) || student.hasKari(param()) && "1".equals(param()._tutisyoPrintKariHyotei)) {
                                    if (printGakunenHyokaHyoteiSumAvg) {
                                        ipdf.VrsOut("AVE_VALUE", sishaGonyu(score._avg)); // 平均
                                        ipdf.VrsOut("TOTAL_VALUE", score._score); // 合計
                                    }
                                }
                            } else {
                                if (_notPrintTestItem == testItem(ti)) {
                                    // 成績表記なし
                                } else {
                                    if (printGakunenHyokaHyoteiSumAvg) {
                                        ipdf.VrsOut("AVERAGE" + line2, sishaGonyu(score._avg)); // 平均
                                        ipdf.VrsOut("TOTAL" + line2, score._score); // 合計
                                    }
                                    ipdf.VrsOut("HR_CLASS_RANK" + line2, score._hrRank.getRank(param())); // クラス順位
                                    ipdf.VrsOut("HR_CLASS_CNT" + line2, score._hrRank._count); // クラス人数
                                    ipdf.VrsOut("GRADE_RANK" + line2, rank.getRank(param())); // 学年順位 or 学科順位
                                    ipdf.VrsOut("GRADE_CNT" + line2, rank._count); // 学年人数 or 学科人数
                                }
                            }
                        }
                    }
                }
                for (int ari = 0; ari < _attendRanges.length; ari++) {
                    final DateRange dr = _attendRanges[ari];
                    if (null == dr) {
                        continue;
                    }
                    String attendTestname = "";
                    int attendTestnamePos;
                    if (SEMEALL.equals(dr._semester)) {
                        attendTestname = "合計";
                        attendTestnamePos = _attendSemeAllPos;
                    } else {
                        attendTestname = "";
                        if (!"1".equals(param()._noPrintSemesternameInTestname) && (null == dr || !SEMEALL.equals(dr._semester))) {
                            attendTestname += StringUtils.defaultString(param().getSemestername(dr._semester));
                        }
                        if (null != dr._testitem) {
                            attendTestname += StringUtils.defaultString(dr._testitem._testitemname);
                        }
                        attendTestnamePos = ari + 1;
                    }
                    final int keta2 = getMS932ByteLength(attendTestname);
                    ipdf.VrsOutn("ATTEND_TESTNAME", attendTestnamePos, "");
                    ipdf.VrsOutn("ATTEND_TESTNAME_2", attendTestnamePos, "");
                    ipdf.VrsOutn("ATTEND_TESTNAME_3", attendTestnamePos, "");
                    ipdf.VrsOutn("ATTEND_TESTNAME" + (keta2 > 8 ? "_3" : keta2 > 6 ? "_2" : ""), attendTestnamePos, attendTestname); // テスト名（出欠の記録）
                }
                if ("1".equals(param()._chikokuSoutaiNasi)) {
                } else {
                    ipdf.VrsOut("LATE_TITLE", "遅刻回数");
                    ipdf.VrsOut("EARLY_TITLE", "早退回数");
                }
                setWhitespace(ipdf);
            }

            void printCScore(final IPdf ipdf, final Student student) {
                int count = 0;
                String oldclasscd = null;
                final List<SubClass> printSubclassList = getPrintSubclassList(student, 20);
                for (final SubClass subClass : printSubclassList) {
                    final int ci = count + 1;
                    loginfo(param(), " subclass = " + subClass);
                    final String subclasscd = subClass._mst._subclasscd;
                    Form.setRecordString(ipdf, "GRP", ci, subclasscd.substring(0, 2));
                    if (null == oldclasscd || !oldclasscd.equals(subclasscd.substring(0, 2))) {
                        Form.setRecordString(ipdf, "CLASSNAME" + (getMS932ByteLength(subClass._mst._classname) > 8 ? "_2" : ""), ci, subClass._mst._classname); // 教科名
                        oldclasscd = subclasscd.substring(0, 2);
                    }
                    if (getMS932ByteLength(subClass._mst._subclassname) > 24) {
                        if (getMS932ByteLength(subClass._mst._subclassname) > 28) {
                            final List<String> tokenList = getTokenList(subClass._mst._subclassname, 28);
                            for (int i = 0; i < tokenList.size(); i++) {
                                Form.setRecordString(ipdf, "SUBCLASSNAME2_" + String.valueOf(i + 1), ci, tokenList.get(i)); // 科目名
                            }
                        } else {
                            Form.setRecordString(ipdf, "SUBCLASSNAME2_1", ci, subClass._mst._subclassname); // 科目名
                        }
                    } else {
                        Form.setRecordString(ipdf, "SUBCLASSNAME", ci, subClass._mst._subclassname); // 科目名
                    }
                    Form.setRecordString(ipdf, "CREDIT", ci, param().getCredits(subclasscd, student._course)); // 単位数
                    for (int ti = 0; ti < _testItems.size(); ti++) {
                        final TestItem item = testItem(ti);
                        if (null == item || 0 > param()._semester.compareTo(item._semester)) {
                            continue;
                        }
                        if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && SEMEALL.equals(item._semester) && subClass._mst._isMoto) {
                            continue;
                        }
                        debugSlump(student, subClass, item);
                        final Score score = subClass.getScore(item._testcd);
                        final String line = String.valueOf(ti + 1);
                        if (null != score) {
                            final String scoreTui = score.getPrintSlump(item);
                            if (TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                                if ((!"1".equals(score._provFlg) || "1".equals(score._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei))) {
                                    final String field1 = "VALUE";
                                    if ("1".equals(param()._printTuishidoAmikakeKakko) && null != scoreTui) {
                                        ipdf.VrAttribute(field1 + "_TUI", ATTRIBUTE_TUISHIDO);
                                        Form.setRecordString(ipdf, field1 + "_TUI", ci, score._score); // 評定
                                        ipdf.VrAttribute(field1 + "_TUI2", ATTRIBUTE_TUISHIDO);
                                        Form.setRecordString(ipdf, field1 + "_TUI2", ci, "(" + scoreTui + ")"); // 評定
                                    } else {
                                        Form.setRecordString(ipdf, field1, ci, score._score); // 評定
                                    }
                                }
                            } else {
                                final String field1;
                                if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd)) {
                                    field1 = "SCORE9";
                                } else {
                                    if (_notPrintTestItem == item) { // 成績表記なし
                                        field1 = "";
                                    } else {
                                        field1 = "SCORE" + line;
                                    }
                                }
                                if ("1".equals(param()._printTuishidoAmikakeKakko) && null != scoreTui) {
                                    ipdf.VrAttribute(field1 + "_TUI", ATTRIBUTE_TUISHIDO);
                                    Form.setRecordString(ipdf, field1 + "_TUI", ci, score._score); // 評定
                                    ipdf.VrAttribute(field1 + "_TUI2", ATTRIBUTE_TUISHIDO);
                                    Form.setRecordString(ipdf, field1 + "_TUI2", ci, "(" + scoreTui + ")"); // 評定
                                } else {
                                    boolean isPrint = true;
                                    if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd) && "1".equals(param()._knjd186vNoPrintGakunenHyokaIfHyoteiNull)) {
                                        final Score gakunenHyotei = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                                        if (null == gakunenHyotei || null == gakunenHyotei._score || "1".equals(gakunenHyotei._provFlg)) {
                                            // 学年評定がないので学年評価を印字しない
                                            isPrint = false;
                                        }
                                        log.info(" subClass = " + subClass._mst + " : gakunenHyotei = " + gakunenHyotei + " => " + isPrint);
                                    }
                                    if (isPrint) {
                                        Form.setRecordString(ipdf, field1, ci, score._score); // 評価
                                    }
                                }
                            }
                        }
                        if (_notPrintDateRange != item._dateRange) {
                            final String attendKey;
                            final String pos;
                            if (SEMEALL.equals(item._testcd.substring(0, 1))) {
                                attendKey = SEMEALL;
                                pos = SEMEALL;
                            } else {
                                attendKey = item._testcd;
                                pos = String.valueOf(line);
                            }
                            final SubclassAttendance sa = subClass.getAttendance(attendKey);
                            if (null != sa) {
                                if ("1".equals(param()._notPrintSubeki)) {
                                    Form.setRecordString(ipdf, "KEKKA" + pos +"_2", ci, getAbsentStr(param(), sa._sick, false)); // 欠課時数
                                } else {
                                    final String slesson = null == sa._lesson ? null : String.valueOf(sa._lesson.intValue());
                                    if (null != slesson) {
                                        Form.setRecordString(ipdf, "SLASH" + pos, ci, "/"); // スラッシュ
                                    }
                                    Form.setRecordString(ipdf, "KEKKA" + pos, ci, getAbsentStr(param(), sa._sick, false)); // 欠課時数
                                    Form.setRecordString(ipdf, "LESSON" + pos, ci, slesson); // 授業日数
                                }
                            }
                        }
                    }
                    ipdf.VrEndRecord();
                    count++;
                }
            }
        }

        // パターンＤ
        public static class FormD extends Form {
            void init(final List<String> testcdList) {
//                _testcds = getTestcd2(testcdList, 3, 4, new String[] {"1010108", "1020108", "2010108", TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
                _testcds = setTestcd3(testcdList, new String[] {"1010108", "1020108", "2010108"}, 4, false);
                _testItems = getTestItems(param(), _testcds);

                _attendRanges = new DateRange[_testItems.size()];
                setNotPrintTestItem();
                for (int i = 0, attendRangeIdx = 0; i < _testItems.size() - 1; i++) {
                    final TestItem item = testItem(i);
                    if (null != item) {
                        if (null != _notPrintDateRange && _notPrintDateRange.equals(item._dateRange) || SEMEALL.equals(item._semester) || item._dateRange.isNull()) {
                        } else if (!item.isKarihyotei() || item.isKarihyotei() && isKarihyoteiPrintAttendance(item, _testItems)) {
                            _attendRanges[attendRangeIdx] = new DateRange(item._testcd, item._semester, item._testitemname, item._dateRange._sdate, item._dateRange._edate);
                            _attendRanges[attendRangeIdx]._testitem = item;
                            attendRangeIdx += 1;
                        }
                        if (null != _notPrintTestItem && _notPrintTestItem.equals(item)) {
                            _testItems.set(i,  null); // はらぼこぉ
                        }
                    }
                }
                _attendSubclassRanges = new DateRange[1];
                if (null != param().getSemester(SEMEALL)) {
                    final Semester seme9 = param().getSemester(SEMEALL);

                    final DateRange range9 = new DateRange(seme9._semester, seme9._semester, "1".equals(param()._notPrintLastExamScore) ? "合計" : "", seme9._dateRange._sdate, seme9._dateRange._edate);
                    _attendRanges[_testItems.size() - 1] = range9;
                    _attendSubclassRanges[0] = range9;
                }
                initDebug();
            }

            void print(final DB2UDB db2, final IPdf ipdf, final Student student) {

                List<SubClass> printSubclassList = getPrintSubclassList(student, 999);

                final String form = "KNJD186V_D.frm";
                final int maxSubclass = 25;
                printSubclassList = printSubclassList.subList(0, Math.min(printSubclassList.size(), maxSubclass));
                ipdf.setParameter(SvfPdf.setFormArgN, "4");
                setForm(ipdf, form);
                printDHeader(db2, ipdf, student);
                printDAttendance(ipdf, student);
                printDCommunication(ipdf, student);
                printDScore(ipdf, student, printSubclassList);
            }

            void printDAttendance(final IPdf ipdf, final Student student) {
//                final int max = _use5testForm ? 5 : 4;
                final int max = 5;
                final Attendance[] attendances = getAttendances(student);
                // 仕様を統一するため不要
//                for (int i = 0; i < attendances.length; i++) {
//                    final Attendance att = attendances[i];
//                    if (null == att || att._lesson == 0) {
//                        continue;
//                    }
//                    loginfo(param(), " print attend index :" + i + ", max = " + max + " / range = " + att._dateRange);
//                }
                int line = 0;
                for (int i = 0; i < attendances.length; i++) {
                    final Attendance att = attendances[i];
                    if (null == att || att._lesson == 0) {
                        continue;
                    }
//                    line += 1;
//                    if (i == attendances.length - 1) {
//                        if (null != att._dateRange && SEMEALL.equals(att._dateRange._key) && line > max) {
//                            // 9学期が枠を超えていたら表示しない
//                            loginfo(param(), " print attend index over:" + i + ", line = " + line + ", max = " + max + " / length = " + attendances.length);
//                            continue;
//                        } else {
//                            line = max;
//                        }
//                    }
                    // 仕様を統一する
                    if (SEMEALL.equals(att._dateRange._key)) {
                        line = max;
                    } else {
                        line = i + 1;
                    }
                    // log.info(" line = " + line + ", " + att._dateRange + ", max = " + max + ", " + attendances.length);
                    ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引
                    ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                    ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席しなければならない日数
                    ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                    ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    if ("1".equals(param()._chikokuSoutaiNasi)) {
                    } else {
                        ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻
                        ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退
                    }

                    if (!"1".equals(param()._noPrintAttendremark)) {
                        final String remark = mkString(getAttendRemarks(student, attendances, att), " ");
                        final int keta = getMS932ByteLength(remark);
                        final String field;
                        if (keta <= 40) {
                            field = "ATTEND_REMARK";
                        } else if (keta <= 60) {
                            field = "ATTEND_REMARK_2";
                        } else {
                            field = "ATTEND_REMARK_3_1";
                        }
                        ipdf.VrsOutn(field, line, remark); //出欠の記録の備考
                    }
                }
            }

            void printDScore(final IPdf ipdf, final Student student, final List<SubClass> printSubclassList) {
                int count = 0;
                for (final SubClass subClass : printSubclassList) {
                    final int line = count + 1;
                    loginfo(param(), " subclass = " + subClass);
                    Form.setRecordString(ipdf, "CLASSNAME2", line, subClass._mst._classabbv); // 教科名略称
                    if (null != subClass._mst._subclassname && subClass._mst._subclassname.length() > 12) {
                        Form.setRecordString(ipdf, "SUBCLASSNAME2", line, subClass._mst._subclassname.substring(0, 12)); // 科目名
                        Form.setRecordString(ipdf, "SUBCLASSNAME2_2", line, subClass._mst._subclassname.substring(12)); // 科目名
                    } else {
                        Form.setRecordString(ipdf, "SUBCLASSNAME1", line, subClass._mst._subclassname); // 科目名
                    }
                    Form.setRecordString(ipdf, "CREDIT", line, param().getCredits(subClass._mst._subclasscd, student._course)); // 単位数
                    for (int ti = 0; ti < _testItems.size(); ti++) {
                        final TestItem item = testItem(ti);
                        if (null == item) {
                            continue;
                        }
                        if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && "9".equals(item._semester) && subClass._mst._isMoto) {
                            continue;
                        }
                        final String testline = String.valueOf(ti + 1);
                        if (SUBCLASSCD999999.equals(subClass._mst._subclasscd)) {
                            continue;
                        } else {
                            debugSlump(student, subClass, item);
                            final Score score = subClass.getScore(item._testcd);
                            if (null != score) {
                                final boolean isPrintScore;
                                final String field1;
                                final String scoreTui = score.getPrintSlump(item);
                                if (TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                                    isPrintScore = (!"1".equals(score._provFlg) || "1".equals(score._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei));
                                    field1 = "VALUE";
                                    if (isPrintScore) {
                                        Form.setRecordString(ipdf, "STANDARD_CREDIT", line, score.getGetCredit(param())); // 修得単位
                                    }
                                } else {
                                    isPrintScore = true;
                                    field1 = "SCORE" + testline;
                                }
                                if (isPrintScore) {
                                    if ("1".equals(param()._printTuishidoAmikakeKakko) && null != scoreTui) {
                                        ipdf.VrAttribute(field1 + "_TUI", ATTRIBUTE_TUISHIDO);
                                        Form.setRecordString(ipdf, field1 + "_TUI", line, StringUtils.defaultString(score._score) + "(" + scoreTui + ")"); // 評定
                                    } else {
                                        Form.setRecordString(ipdf, field1, line, score._score); // 得点
                                    }
                                }
                            }
                        }
                    }
                    if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && subClass._mst._isMoto) {
                    } else {
                        final SubclassAttendance sa = subClass.getAttendance(SEMEALL);
                        if (null != sa) {
                            Form.setRecordString(ipdf, "KEKKA", line, getAbsentStr(param(), sa._sick, false)); // 欠課時数
                            Form.setRecordString(ipdf, "KEKKA_LESSON", line, null == sa._lesson || 0.0 == sa._lesson.doubleValue() ? "" : String.valueOf(sa._lesson.intValue())); // 授業日数
                        }
                    }
                    ipdf.VrEndRecord();
                    count++;
                }
                if (count == 0) {
                    for (int i = count; i <= 1; i++) {
                        final int ci = i + 1;
                        Form.setRecordString(ipdf, "SUBCLASSNAME1", ci, "　"); // 教科名
                        ipdf.VrEndRecord();
                    }
                }
            }

            void printDHeader(final DB2UDB db2, final IPdf ipdf, final Student student) {
//                ipdf.setString("HR_NAME", student._hrName); // クラス名
//                ipdf.setString("ATTENDNO", student._attendno); // 出席番号
//                ipdf.setString("NAME", student._name); // 生徒氏名
                final String shrname = StringUtils.defaultString(student._hrName);
                final String sattendno = StringUtils.defaultString(student._attendno);
                final String sname = StringUtils.defaultString(student._name);
                ipdf.VrsOut("NENDO", param()._nendo + "　" + StringUtils.defaultString(param()._title, "成績通知票")); // 年度
                ipdf.VrsOut("GET_CREDIT", student.getTotalGetCredit(param(), "1".equals(param()._addPastCredit))); // 修得単位数の合計
                ipdf.VrsOut("SCH_INFO1", shrname + sattendno + "番　　" + sname); // 学校名
                ipdf.VrsOut("SCH_INFO2", shrname + sattendno + "番　　" + sname); // 学校名
                ipdf.VrsOut("SCHOOLNAME", param()._schoolName); // 学校名
                ipdf.VrsOut("STAFFNAME1", param()._jobName + "　" + param()._principalName); // 校長
                ipdf.VrsOut("STAFFNAME2", param()._hrJobName + "　" +  student._staffName + "　印"); // 担任
                ipdf.VrsOut("ITEMNAME", getGroupDivName()); // 項目
                if ("1".equals(param()._chikokuSoutaiNasi)) {
                } else {
                    ipdf.VrsOut("LATE_NAME", "遅刻回数");
                    ipdf.VrsOut("EARLY_NAME", "早退回数");
                }

                final SubClass subclass999999 = student._subclassMap.get(SUBCLASSCD999999);
                for (int i = 0; i < _testItems.size(); i++) {
                    final String line = String.valueOf(i + 1);
                    final TestItem testItem = testItem(i);
                    if (null != testItem) {
                        final String semestername = "1".equals(param()._noPrintSemesternameInTestname) || SEMEALL.equals(testItem._semester) ? "" : StringUtils.defaultString(param().getSemestername(testItem._semester));
                        ipdf.VrsOut("TESTNAME" + line,  semestername + StringUtils.defaultString(testItem._testitemname)); // テスト名
                        ipdf.VrsOut("KETTEN_CNT" + line, student.getKettenSubclassCount(param(), testItem)); // 欠点科目数

                        if (null != subclass999999 && null != subclass999999.getScore(testItem._testcd)) {

                            final Score score = subclass999999.getScore(testItem._testcd);
                            if (TESTCD_GAKUNEN_HYOTEI.equals(testItem._testcd)) {
                                if (!student.hasKari(param())) {
                                    ipdf.VrsOut("TOTAL_VALUE", score._score); // 総点
                                    ipdf.VrsOut("VALUE_AVERAGE", sishaGonyu(score._avg)); // 平均
                                }
                            } else {
                                ipdf.VrsOut("TOTAL_SCORE" + line, score._score); // 総点
                                ipdf.VrsOut("SCORE_AVERAGE" + line, sishaGonyu(score._avg)); // 平均

                                ipdf.VrsOut("HR_CLASS_RANK" + line, score._hrRank.getRank(param())); // 学級順位
                                ipdf.VrsOut("HR_CLASS_CNT" + line, score._hrRank._count); // 学級人数
                                ipdf.VrsOut("GRADE_RANK" + line, getGroupDivRank(score).getRank(param())); // 学年順位
                                ipdf.VrsOut("GRADE_CNT" + line, getGroupDivRank(score)._count); // 学年人数
                            }
                        }
                    }
                }
                for (int i = 0; i < _attendRanges.length; i++) {
                    final DateRange range = _attendRanges[i];
                    if (null != range) {
                        final int pos;
                        String attendTestname = "";
                        if (SEMEALL.equals(range._key)) {
                            attendTestname = "合計";
                            pos = 5;
                        } else {
                            if ("1".equals(param()._noPrintSemesternameInTestname) || SEMEALL.equals(range._semester)) {
                            } else {
                                attendTestname += StringUtils.defaultString(param().getSemestername(range._semester));
                            }
                            String testname = range._name;
                            testname = (testname == null || -1 == testname.indexOf("評価") ? StringUtils.defaultString(testname) : testname.substring(0, testname.indexOf("評価")));
                            attendTestname += StringUtils.defaultString(testname);
                            pos = i + 1;
                        }
                        ipdf.VrsOutn("ATTEND_TESTNAME", pos, attendTestname); // テスト名（出欠の記録）
                    }
                }
                setWhitespace(ipdf);
            }


            void printDCommunication(final IPdf ipdf, final Student student) {
                final String[] token = get_token(student._communication, 50, 6);
                for (int i = 0; i < token.length; i++) {
                    ipdf.VrsOutn("COMMUNICATION", (i + 1), token[i]);
                }
            }
        }

        // パターンＥ
        public static class FormE extends Form {
            void init(final List<String> testcdList) {
                _testcds = setTestcd3(testcdList, new String[] {"1010101", "1990008", "2010101", "2990008"}, 4, false);
                _testItems = getTestItems(param(), _testcds);
                setNotPrintTestItem();
                int notNullCount = 0;
                for (int i = 0; i < _testItems.size(); i++) {
                    if (null != testItem(i)) {
                        notNullCount += 1;
                    }
                }
                notNullCount = Math.min(notNullCount, _formTestCount + 1);

                _attendRanges = new DateRange[notNullCount];
                _attendSubclassRanges = new DateRange[notNullCount];
                int ari = 0;
                for (int i = 0; i < notNullCount - 1; i++) {
                    final TestItem item = testItem(i);
                    if (null != item) {
                        if (null != _notPrintDateRange && _notPrintDateRange.equals(item._dateRange) || item._dateRange.isNull()) {
                            // 表示なし
                            continue;
                        }
                        if (!item.isKarihyotei() || item.isKarihyotei() && isKarihyoteiPrintAttendance(item, _testItems)) {
                            _attendRanges[ari] = new DateRange(item._testcd, item._semester, item._testitemname, item._dateRange._sdate, item._dateRange._edate);
                            _attendRanges[ari]._testitem = item;
                            if (!"1".equals(param()._noAttendSubclassSp)) {
                                _attendSubclassRanges[ari] = _attendRanges[ari];
                            }
                            ari += 1;
                        }
                    }
                }
                    final Semester seme9 = param().getSemester(SEMEALL);
                if (null != seme9) {
                    _attendRanges[notNullCount - 1] = new DateRange(seme9._semester, seme9._semester, "1".equals(param()._notPrintLastExamScore) ? "合計" : seme9._semestername, seme9._dateRange._sdate, seme9._dateRange._edate);
                    _attendSubclassRanges[notNullCount - 1] = _attendRanges[notNullCount - 1];
                }
                initDebug();
            }

            void print(final DB2UDB db2, final IPdf ipdf, final Student student) {
                final String _form;
//                if ("1".equals(param()._printCareerplan)) {
//                    _form = _use5testForm ? "KNJD186V_E_2_T5.frm" : "KNJD186V_E_2.frm";
//                } else {
//                    _form = _use5testForm ? "KNJD186V_E_1_T5.frm" : "KNJD186V_E_1.frm";
//                }
                if ("1".equals(param()._printCareerplan)) {
                    _form = "KNJD186V_E_2.frm";
                } else {
                    _form = "KNJD186V_E_1.frm";
                }

                setForm(ipdf, _form);
                printEHeader(db2, ipdf, student);
                printEAddress(ipdf, student);
                printEAttendance(ipdf, student);
                printECommunication(ipdf, student);
                printEScore(ipdf, student);
            }

            void printEAddress(final IPdf ipdf, final Student student) {
                final Address address = student._address;
                if (null == address) {
                    return;
                }
//                ipdf.setString("ZIPCD", address._zipcd); // 郵便番号
                if (!StringUtils.isBlank(address._zipcd)) {
                    ipdf.VrsOut("ZIPCD", "〒" + address._zipcd); // 郵便番号
                }
                final boolean useAddress2 = getMS932ByteLength(address._address1) > 40 || getMS932ByteLength(address._address2) > 40;
                ipdf.VrsOut(useAddress2 ? "ADDR1_2" : "ADDR1", address._address1); // 住所
                ipdf.VrsOut(useAddress2 ? "ADDR2_2" : "ADDR2", address._address2); // 住所
                if (null != address._addressee) {
                    ipdf.VrsOut(getMS932ByteLength(address._addressee) > 20 ? "ADDRESSEE2" : "ADDRESSEE", address._addressee + "　様"); // 受取人2
                }
            }

            void printEAttendance(final IPdf ipdf, final Student student) {
                final int maxLine = /*_use5testForm ? 6 : 5;*/ 5;
                final Attendance[] attendances = getAttendances(student);
                for (int i = 0; i < attendances.length; i++) {
                    final Attendance att = attendances[i];
                    if (null == att || att._lesson == 0) {
                        continue;
                    }
                    final int line = SEMEALL.equals(att._dateRange._key) ? maxLine : i + 1;
                    ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                    ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引等日数
                    ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                    ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
                    ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
                    ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                    if ("1".equals(param()._chikokuSoutaiNasi)) {
                    } else {
                        ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                        ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                    }
                    if ("1".equals(param()._noAttendSubclassSp)) {
                    } else {
                        ipdf.VrsOutn("LHR_ABSENT", line, att._lhrKekka.toString()); // LHR欠課時数
                        ipdf.VrsOutn("EVENT_ABSENT", line, att._gyojiKekka.add(att._iinkaiKekka).toString()); // 行事+委員会欠課時数
                    }
                }
            }


            void printECommunication(final IPdf ipdf, final Student student) {
                final String[] token = get_token(student._communication, 50, 6);
                for (int i = 0; i < token.length; i++) {
                    ipdf.VrsOutn("COMMUNICATION", (i + 1), token[i]);
                }
                ipdf.VrsOut("SUBCLASSNAME3", student.getSogoSubclassname(param()));
                final String[] totalStudyKatsudo = get_token(student._totalStudyKatsudo, 50, 2);
                for (int i = 0; i < totalStudyKatsudo.length; i++) {
                    ipdf.VrsOut("TOTAL_STUDY1_" + (i + 1), totalStudyKatsudo[i]);
                }
                final String[] totalStudyHyoka = get_token(student._totalStudyHyoka, 50, 3);
                for (int i = 0; i < totalStudyHyoka.length; i++) {
                    ipdf.VrsOut("TOTAL_STUDY2_" + (i + 1), totalStudyHyoka[i]);
                }
                ipdf.VrsOut("SUBCLASSNAME4", StringUtils.defaultString(param()._careerPlanSubclassname, "キャリアプラン"));
                final String[] careerPlanKatsudo = get_token(student._careerPlanKatsudo, 50, 2);
                for (int i = 0; i < careerPlanKatsudo.length; i++) {
                    ipdf.VrsOut("CARRIER1_" + (i + 1), careerPlanKatsudo[i]);
                }
                final String[] careerPlanHyoka = get_token(student._careerPlanHyoka, 50, 3);
                for (int i = 0; i < careerPlanHyoka.length; i++) {
                    ipdf.VrsOut("CARRIER2_" + (i + 1), careerPlanHyoka[i]);
                }
            }

            void printEHeader(final DB2UDB db2, final IPdf ipdf, final Student student) {
                ipdf.VrsOut("SCHOOLNAME", param()._schoolName); // 学校名
                ipdf.VrsOut("STAFFNAME1", param()._jobName + "　" + param()._principalName); // 校長
                ipdf.VrsOut("STAFFNAME2", param()._hrJobName + "　" +  student._staffName); // 担任
                if (null != student.getHrAttendNo(param())) {
                    ipdf.VrsOut("HR_ATTNO_NAME", "(" + student.getHrAttendNo(param()) + ")"); // 年組番・氏名
                }
//                ipdf.setString("MAJORNAME", student._majorname); // 学科名
//                ipdf.setString("HR_NAME", student._hrName); // クラス名
//                ipdf.setString("ATTENDNO", student._attendno); // 出席番号
//                ipdf.setString("NAME", student._name); // 氏名
                final String smajorname = StringUtils.defaultString(student._majorname);
                final String shrname = StringUtils.defaultString(student._hrName);
                final String sattendno = StringUtils.defaultString(student._attendno);
                final String sname = StringUtils.defaultString(student._name);
                ipdf.VrsOut("SCH_INFO", smajorname + "  " + shrname + sattendno + "番　　" + sname);
                final String ssemester = StringUtils.defaultString(param().getSemestername(param()._semester));
                ipdf.VrsOut("SCH_INFO2", "・保護者から（" + param()._nendo + ssemester + StringUtils.defaultString(removeSpace(param()._title), "通知表") + "　" + shrname + sattendno + "番　" + sname + "）");

                ipdf.VrsOut("NENDO", param()._nendo); // 年度
                ipdf.VrsOut("TITLE", StringUtils.defaultString(param()._title, "通 知 表")); // 年度
                ipdf.VrsOut("RANK_NAME", getGroupDivName()); // 項目

                if (SEMEALL.equals(param()._semester)) {
                    ipdf.VrsOut("GET_CREDIT", student.getTotalGetCredit(param(), "1".equals(param()._addPastCredit))); // 修得単位数
                }

                for (int ti = 0; ti < _attendRanges.length; ti++) {
                    final DateRange dr = _attendRanges[ti];
                    if (null != dr) {
                        final String field;
                        String name = "";
                        if (SEMEALL.equals(dr._semester)) {
                            name = "合計";
                        } else {
                            if (!"1".equals(param()._noPrintSemesternameInTestname)) {
                                name += StringUtils.defaultString(param().getSemestername(dr._semester));
                            }
                            name += StringUtils.defaultString(dr._name);
                        }
                        if (getMS932ByteLength(name) > 10) {
                            field = "ATTEND_TESTNAME_4";
                        } else {
                            if (getMS932ByteLength(name) > 8) {
                                field = "ATTEND_TESTNAME_3";
                            } else {
                                if (getMS932ByteLength(name) > 6) {
                                    field = "ATTEND_TESTNAME_2";
                                } else {
                                    field = "ATTEND_TESTNAME";
                                }
                            }
                        }
                        final int pos = SEMEALL.equals(dr._semester) ? /*(_use5testForm ? 6 : 5)*/ 5 : ti + 1;
                        ipdf.VrsOutn(field, pos, name); // テスト名（出欠の記録）
                    }
                }

                if ("1".equals(param()._chikokuSoutaiNasi)) {
                } else {
                    ipdf.VrsOut("LATE_TITLE", "遅刻回数");
                    ipdf.VrsOut("EARLY_TITLE", "早退回数");
                }
                if ("1".equals(param()._noAttendSubclassSp)) {
                } else {
                    ipdf.VrsOut("LHR_TITLE1", "ロングホームルーム");
                    ipdf.VrsOut("LHR_TITLE2", "欠課時数");
                    ipdf.VrsOut("EVENT_TITLE1", "生徒会活動・学校行事");
                    ipdf.VrsOut("EVENT_TITLE2", "欠課時数");
                }
                setWhitespace(ipdf);
            }

            private String removeSpace(final String s) {
                if (null == s) {
                    return s;
                }
                final StringBuffer stb = new StringBuffer();
                for (final char ch : s.toCharArray()) {
                    if (!Character.isWhitespace(ch)) {
                        stb.append(ch);
                    }
                }
                return stb.toString();
            }

            void printEScore(final IPdf ipdf, final Student student) {
                final SubClass subclass999999 = student.getSubClass(SUBCLASSCD999999);

                final List<SubClass> subclassList = getPrintSubclassList(student, 20);

                for (int j = 0; j < subclassList.size(); j++) {
                    final SubClass subClass = subclassList.get(j);
                    loginfo(param(), " subclass = " + subClass);
                    final int ci = j + 1;
                    ipdf.VrsOutn("GRP", ci, subClass._mst._subclasscd.substring(0, 2));
                    ipdf.VrsOutn("CLASSNAME" + (getMS932ByteLength(subClass._mst._classname) > 8 ? "_2" : ""), ci, subClass._mst._classname); // 教科名
                    if (getMS932ByteLength(subClass._mst._subclassname) > 24) {
                        if (getMS932ByteLength(subClass._mst._subclassname) > 37) {
                            final List<String> tokenList = getTokenList(subClass._mst._subclassname, 37);
                            for (int i = 0; i < tokenList.size(); i++) {
                                ipdf.VrsOutn("SUBCLASSNAME2_" + String.valueOf(i + 1), ci, tokenList.get(i)); // 科目名
                            }
                        } else {
                            ipdf.VrsOutn("SUBCLASSNAME2_1", ci, subClass._mst._subclassname); // 科目名
                        }
                    } else {
                        ipdf.VrsOutn("SUBCLASSNAME", ci, subClass._mst._subclassname); // 科目名
                    }
                }

                final Map<String, List<TestItem>> semesterTestItemListMap = new TreeMap<String, List<TestItem>>();
                int idx9 = 1;
                for (final TestItem testItem : _testItems) {
                    if (null == testItem) {
                        continue;
                    }
                    getMappedList(semesterTestItemListMap, testItem._semester).add(testItem);
                }
                final Set<String> testSemesterSet = new HashSet<String>(semesterTestItemListMap.keySet());
                testSemesterSet.remove(SEMEALL);
                testSemesterSet.remove(null);
                final TreeSet<String> testSemesterTreeSet = new TreeSet<String>(testSemesterSet);
                String lastSemester = null;
                if (!testSemesterTreeSet.isEmpty()) {
                    lastSemester = testSemesterTreeSet.last();
                }

                log.info(" lastSemester = " + lastSemester);

                final List<Map.Entry<String, List<TestItem>>> semesterTestItemEntrySetList = new ArrayList(semesterTestItemListMap.entrySet());
                for (int sti = 0; sti < semesterTestItemEntrySetList.size(); sti++) {
                    final int bi = sti + 1;
                    final Map.Entry<String, List<TestItem>> e = semesterTestItemEntrySetList.get(sti);
                    final String semester = e.getKey();
                    List<TestItem> semesterTestItemList = e.getValue();
                    final boolean isLastTestSemester = null != lastSemester && lastSemester.equals(semester);
                    if (isLastTestSemester && "1".equals(param()._notPrintLastExam)) {
                        if (semesterTestItemList.size() > 0) {
                            semesterTestItemList = semesterTestItemList.subList(0, semesterTestItemList.size() - 1);
                        }
                    }

                    if (SEMEALL.equals(semester)) {
                        for (int i = 0; i < subclassList.size(); i++) {
                            final SubClass subClass = subclassList.get(i);
                            final int ci = i + 1;
                            if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && subClass._mst._isMoto) {
                                continue;
                            }
                            final SubclassAttendance sa = subClass.getAttendance(SEMEALL);
                            if (null != sa && null != sa._sick) {
                                ipdf.VrsOutn("KEKKA", ci, getAbsentStr(param(), sa._sick, true)); // 欠課時数
                            }
                        }
                    }

                    for (int ti = 0; ti < semesterTestItemList.size(); ti++) {
                        final TestItem testItem = semesterTestItemList.get(ti);
                        if (null != _notPrintTestItem && _notPrintTestItem.equals(testItem)) {
                            continue;
                        }
                        if (SEMEALL.equals(semester)) {
                            final String tis = "9_" + String.valueOf(idx9);

                            ipdf.VrsOut("SEMESTERNAME9", StringUtils.defaultString(param().getSemestername(SEMEALL))); // テスト名

                            print9Gakki(ipdf, student, subclass999999, subclassList, testItem, tis);
                            idx9 += 1;
                        } else {
                            final String fieldDiv;
                            if (semesterTestItemList.size() == 1) {
//                            if (semesterTestItemList.size() <= 2) {
                                fieldDiv = "3";
                            } else if (semesterTestItemList.size() > 2) {
                                fieldDiv = "1";
                            } else {
                                fieldDiv = "2";
                            }

                            final String field = "SEMESTERNAME" + fieldDiv;
//                            log.info(" " + field + " = " + _fieldInfoMap.get(field));

                            Form.setRecordString(ipdf, field, bi, param().getSemestername(semester)); // テスト名

                            final boolean isLastTestItem = ti == semesterTestItemList.size() - 1;
                            final boolean isPrintScore = !(isLastTestSemester && isLastTestItem && "1".equals(param()._notPrintLastExamScore)); // 最終学期最終考査以外は表示する
                            final String tis = fieldDiv + "_" + String.valueOf(ti + 1);
                            if (null != testItem) {
                                final String testitemnameField;
                                if (getMS932ByteLength(testItem._testitemname) > 8) {
                                    testitemnameField = "TESTNAME" + tis + "_3";
                                } else if (getMS932ByteLength(testItem._testitemname) > 6) {
                                    testitemnameField = "TESTNAME" + tis + "_2";
                                } else {
                                    testitemnameField = "TESTNAME" + tis + "";
                                }
                                Form.setRecordString(ipdf, testitemnameField, bi, testItem._testitemname); // テスト名
                                if (testItem._printScore) {
                                    Form.setRecordString(ipdf, "KETTEN_CNT" + tis, bi, student.getKettenSubclassCount(param(), testItem)); // 欠点科目数
                                }
                                if (null != subclass999999._scoreMap.get(testItem._testcd) && isPrintScore) {
                                    final Score score = (Score) subclass999999._scoreMap.get(testItem._testcd);
                                    Form.setRecordString(ipdf, "AVERAGE" + tis, bi, sishaGonyu(score._avg)); // 平均
                                    Form.setRecordString(ipdf, "TOTAL" + tis, bi, score._score); // 合計
                                    Form.setRecordString(ipdf, "HR_CLASS_RANK" + tis, bi, score._hrRank.getRank(param())); // クラス順位
                                    Form.setRecordString(ipdf, "HR_CLASS_CNT" + tis, bi, score._hrRank._count); // クラス人数
                                    final Rank rank = getGroupDivRank(score);
                                    Form.setRecordString(ipdf, "GRADE_RANK" + tis, bi, rank.getRank(param())); // 学年順位 or 学科順位
                                    Form.setRecordString(ipdf, "GRADE_CNT" + tis, bi, rank._count); // 学年人数 or 学科人数
                                }
                            }
                            for (int i = 0; i < subclassList.size(); i++) {
                                final SubClass subClass = subclassList.get(i);
                                final int line = i + 1;

                                debugSlump(student, subClass, testItem);
                                final Score score = subClass.getScore(testItem._testcd);
                                if (null != score && isPrintScore) {
                                    final String scoreTui = score.getPrintSlump(testItem);

                                    final String field1 = "SCORE" + tis;
                                    if ("1".equals(param()._printTuishidoAmikakeKakko) && null != scoreTui) {
                                        ipdf.VrAttributen(field1 + "TUI", line, ATTRIBUTE_TUISHIDO);
                                        ipdf.VrsOutn(field1 + "TUI", line, score._score + "(" + scoreTui + ")"); // 評定
                                    } else {
                                        ipdf.VrsOutn(field1, line, score._score); // 評価
                                    }
                                }
                            }
                        }
                    }
                    ipdf.VrEndRecord();
                }
            }
            private void print9Gakki(final IPdf ipdf, final Student student, final SubClass subclass999999, final List<SubClass> subclassList, final TestItem testItem, final String tis) {
                if (null != subclass999999._scoreMap.get(testItem._testcd)) {
                    final Score score = (Score) subclass999999._scoreMap.get(testItem._testcd);
                    if (TESTCD_GAKUNEN_HYOTEI.equals(testItem._testcd)) {
                        if (!student.hasKari(param()) || student.hasKari(param()) && "1".equals(param()._tutisyoPrintKariHyotei)) {
                            ipdf.VrsOut("AVE_VALUE", sishaGonyu(score._avg)); // 平均
                            ipdf.VrsOut("TOTAL_VALUE", score._score); // 合計

//                                    final String tisHyotei = "9";
//                                    ipdf.VrsOut("HR_CLASS_RANK" + tisHyotei, score._hrRank.getRank(param())); // クラス順位
//                                    ipdf.VrsOut("HR_CLASS_CNT" + tisHyotei, score._hrRank._count); // クラス人数
//                                    final Rank rank = "4".equals(param()._groupDiv) ? score._majorRank : score._gradeRank;
//                                    ipdf.VrsOut("GRADE_RANK" + tisHyotei, rank.getRank(param())); // 学年順位 or 学科順位
//                                    ipdf.VrsOut("GRADE_CNT" + tisHyotei, rank._count); // 学年人数 or 学科人数
                        }
                    } else {
                        if (testItem._printScore) {
                            ipdf.VrsOut("KETTEN_CNT" + tis, student.getKettenSubclassCount(param(), testItem)); // 欠点科目数
                        }
                        ipdf.VrsOut("AVERAGE" + tis, sishaGonyu(score._avg)); // 平均
                        ipdf.VrsOut("TOTAL" + tis, score._score); // 合計
                        ipdf.VrsOut("HR_CLASS_RANK" + tis, score._hrRank.getRank(param())); // クラス順位
                        ipdf.VrsOut("HR_CLASS_CNT" + tis, score._hrRank._count); // クラス人数
                        final Rank rank = getGroupDivRank(score);
                        ipdf.VrsOut("GRADE_RANK" + tis, rank.getRank(param())); // 学年順位 or 学科順位
                        ipdf.VrsOut("GRADE_CNT" + tis, rank._count); // 学年人数 or 学科人数
                    }
                }

                ipdf.VrsOut("TESTNAME" + tis + (getMS932ByteLength(testItem._testitemname) > 6 ? "_2" : ""), testItem._testitemname); // テスト名
                for (int i = 0; i < subclassList.size(); i++) {
                    final SubClass subClass = subclassList.get(i);
                    final int line = i + 1;
                    if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && "9".equals(testItem._semester) && subClass._mst._isMoto) {
                        continue;
                    }
                    debugSlump(student, subClass, testItem);
                    final Score score = subClass.getScore(testItem._testcd);
                    if (null != score) {
                        final String scoreTui = score.getPrintSlump(testItem);
                        final boolean isPrintScore;
                        if (TESTCD_GAKUNEN_HYOTEI.equals(testItem._testcd)) {
                            isPrintScore = (!"1".equals(score._provFlg) || "1".equals(score._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei));
                            if (isPrintScore) {
                                ipdf.VrsOutn("CREDIT1_1", line, score.getCompCredit(param()));
                                ipdf.VrsOutn("CREDIT1_2", line, score.getGetCredit(param()));
                            }
                        } else {
                            isPrintScore = true;
                        }
                        if (isPrintScore) {
                            final String field1 = "SCORE" + tis;
                            if ("1".equals(param()._printTuishidoAmikakeKakko) && null != scoreTui) {
                                ipdf.VrAttributen(field1 + "TUI", line, ATTRIBUTE_TUISHIDO);
                                ipdf.VrsOutn(field1 + "TUI", line, score._score + "(" + scoreTui + ")"); // 評定
                            } else {
                                ipdf.VrsOutn(field1, line, score._score); // 評価
                            }
                        }
                    }
                }
            }
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75537 $ $Date: 2021-02-16 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;

        final String _grade;
        final String _gradeCd;
        final String _gradeHrclass;
        final String _schoolKind;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        final String _nendo;

        final String _groupDiv; // 総合順位出力 1:学年 4:学科 3:コース
        final String _rankDiv; // 順位の基準点 1:総合点 2:平均点
        final String _patarnDiv; // 1:A 2:B 3:C 4:D 5:E
        final String _maxOrSidou; // 最高点・追指導 (Aパターンのみ)
//        final String _hogosha; // 保護者からのコメント欄 ・出欠の記録（考査ごと）(Aパターンのみ)
        final String _chikokuSoutaiNasi; // 遅刻・早退回数 表示なし
        final String _notPrintSubeki; // 出席すべき時数表示なし(Cパターンのみ)
        final String _notPrintGappeiMotoGakunenHyokaHyotei; // 合併元科目の学年評価・学年評定・出欠時数
        final String _printCareerplan; // キャリアプランを表示する（Eパターンのみ）
        final String _tutisyoPrintKariHyotei; // 仮評定を表示する
        final String _notPrintLastExam; // 最終考査表記なし
        final String _notPrintLastExamScore; // 最終考査表記なし (成績のみ表記なし)
        final String _noAttendSubclassSp; // 遅刻・早退回数 表示なし
        final String _noPrintSemesternameInTestname; // 考査名に学期名表示なし
        final String _printTuishidoAmikakeKakko; // 追指導点を網掛けしてカッコつき表示 (Aパターン以外)
        final String _addPastCredit; // 前年度までの修得単位数を加算する
        final String _knjd186vAddAttendSubclassGetCredit; // 合併元科目の単位数を加算する
        final String _noPrintRank; // 順位表記なし
        final String _knjd186vNoPrintGakunenHyokaIfHyoteiNull; // 評定がない学年評価を印字しない
        final String _noPrintGakunenHyokaHyoteiSumAvg; // 学年評価評定の合計点平均点を印字しない
        final String _kettenKamokuNoSubtract; // 欠点科目数表記 追指導合格した科目数を減算しない
        final String _zouka; // 増加単位を加算する
        final String _noPrintHogosha; // 保護者欄を印字しない(Dパターンのみ)
        final String _noPrintCommunication; // 通信欄を表示しない
        final String _noPrintAttendremark;
        final String _documentroot;
        final String _imagepath;
        final boolean _hasATTEND_BATCH_INPUT_HDAT;
        final boolean _hasATTEND_SEMES_REMARK_HR_DAT;
        final String _useAttendSemesHrRemark;
        private Map<String, List<String>> _attendRemarkHrMap = Collections.emptyMap();

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final Form _form;

        /** 端数計算共通メソッド引数 */
        private Map<String, Semester> _semesterMap;
        private Map<String, SubclassMst> _subclassMst;
        private Map<String, String> _creditMst;
        private Map<String, List<String>> _recordMockOrderSdivDatMap;

        private KNJSchoolMst _knjSchoolMst;

//        private int _gradCredits;  // 卒業認定単位数

        private String _avgDiv;
        private List<String> _d054KettenNamecd2List;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private String _title;
        final String _whitespaceImagePath;
        final String _h508Name1;
        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;
        private List<String> _d026List = new ArrayList<String>();
        private String _careerPlanSubclassname;
        final Map<String, TestItem> _testitemMap;
        final List<DateRange> _semesterDetailList;
        final boolean _isOutputDebug;
        final Map _attendParamMap;
        final String _d008Namecd1;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _groupDiv = request.getParameter("GROUP_DIV");
            _rankDiv = request.getParameter("RANK_DIV");
            _patarnDiv = request.getParameter("PATARN_DIV");
            _maxOrSidou = request.getParameter("MAX_OR_SIDOU");
//            _hogosha = request.getParameter("HOGOSHA");
            _chikokuSoutaiNasi = request.getParameter("CHIKOKU_SOUTAI_NASI");
            _notPrintSubeki = request.getParameter("NOT_PRINT_SUBEKI");
            _notPrintGappeiMotoGakunenHyokaHyotei = request.getParameter("NOT_PRINT_GAKUNEN_HYOKA_HYOTEI");
            _printCareerplan = request.getParameter("PRINT_CAREERPLAN");
            _tutisyoPrintKariHyotei = request.getParameter("tutisyoPrintKariHyotei");
            _notPrintLastExam = request.getParameter("NOT_PRINT_LASTEXAM");
            _notPrintLastExamScore = request.getParameter("NOT_PRINT_LASTEXAM_SCORE");
            _noAttendSubclassSp = request.getParameter("NO_ATTEND_SUBCLASS_SP");
            _noPrintSemesternameInTestname = request.getParameter("NO_PRINT_SEMENAME_IN_TESTNAME");
            _printTuishidoAmikakeKakko = request.getParameter("PRINT_TUISHIDOU");
            _knjd186vAddAttendSubclassGetCredit = request.getParameter("knjd186vAddAttendSubclassGetCredit");
            _addPastCredit = request.getParameter("ADD_PAST_CREDIT");
            _noPrintRank = request.getParameter("NO_PRINT_RANK");
            _knjd186vNoPrintGakunenHyokaIfHyoteiNull = request.getParameter("knjd186vNoPrintGakunenHyokaIfHyoteiNull");
            _noPrintGakunenHyokaHyoteiSumAvg = request.getParameter("NO_PRINT_GAKUNENHYOKA_HYOTEI_SUM_AVG");
            _kettenKamokuNoSubtract = request.getParameter("KETTEN_KAMOKU_NO_SUBTRACT");
            _zouka = request.getParameter("ZOUKA");
            _noPrintHogosha = request.getParameter("NO_PRINT_HOGOSHA");
            _noPrintCommunication = request.getParameter("NO_PRINT_COMMUNICATION");
            _noPrintAttendremark = request.getParameter("NO_PRINT_ATTENDREMARK");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _hasATTEND_SEMES_REMARK_HR_DAT = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_SEMES_REMARK_HR_DAT", null);
            _hasATTEND_BATCH_INPUT_HDAT = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_BATCH_INPUT_HDAT", null);
            _useAttendSemesHrRemark = request.getParameter("useAttendSemesHrRemark");

            _gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            if (PATTERN_A.equals(_patarnDiv)) {
                _form = new Form.FormA();
            } else if (PATTERN_B.equals(_patarnDiv)) {
                _form = new Form.FormB();
            } else if (PATTERN_C.equals(_patarnDiv)) {
                _form = new Form.FormC();
            } else if (PATTERN_D.equals(_patarnDiv)) {
                _form = new Form.FormD();
            } else if (PATTERN_E.equals(_patarnDiv)) {
                _form = new Form.FormE();
            } else {
                _form = null;
            }
            _form._param = this;

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            setD054Namecd2Max(db2);
            _definecode = createDefineCode(db2);
//            _gradCredits = getGradCredits(db2);
            setSubclassMst(db2);
            setCreditMst(db2);
            setRecordMockOrderSdivDat(db2);
            _h508Name1 = getH508Name1(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            loadNameMstD026(db2);
            _testitemMap = getTestItemMap(db2);
            _semesterDetailList = getSemesterDetailList(db2);
            _whitespaceImagePath = getImageFilePath("whitespace.png");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            loadAttendRemarkHrMap(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186V' AND NAME = '" + propName + "' "));
        }

        private String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            return definecode;
        }

        private void setD054Namecd2Max(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
            stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 <> (SELECT MIN(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
            _d054KettenNamecd2List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "NAMECD2");
        }

        /**
         * 名称マスタ NAMECD1='H508' NAMECD2='02'読込
         */
        private String getH508Name1(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H508' AND NAMECD2 = '02' "));
        }


        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
//            if ("1".equals(_useClassDetailDat)) {
//                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
//                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
//                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
//            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
//            }

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private String getSemestername(final String semester) {
            final Semester s = getSemester(semester);
            if (null == s) {
                log.warn(" no semester : " + s);
                return null;
            }
            return s._semestername;
        }

        private Semester getSemester(final String semester) {
            return _semesterMap.get(semester);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map<String, Semester> loadSemester(final DB2UDB db2, final String year, final String grade) {
            final Map<String, Semester> map = new HashMap<String, Semester>();
            final String sql = "select"
                    + "   SEMESTER,"
                    + "   SEMESTERNAME,"
                    + "   SDATE,"
                    + "   EDATE"
                    + " from"
                    + "   V_SEMESTER_GRADE_MST"
                    + " where"
                    + "   YEAR='" + year + "'"
                    + "   AND GRADE='" + grade + "'"
                    + " order by SEMESTER"
                ;

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                map.put(KnjDbUtils.getString(row, "SEMESTER"), new Semester(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SEMESTERNAME"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE")));
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _jobName = KnjDbUtils.getString(row, "JOB_NAME");
            _principalName = KnjDbUtils.getString(row, "PRINCIPAL_NAME");
            _hrJobName = KnjDbUtils.getString(row, "REMARK2");
            _title = KnjDbUtils.getString(row, "REMARK6");

            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
        }

//        // 卒業認定単位数の取得
//        private int getGradCredits(
//                final DB2UDB db2
//        ) {
//            int gradcredits = 0;
//            final String gradecreditsStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _year + "'"));
//            if (NumberUtils.isDigits(gradecreditsStr)) {
//                gradcredits = Integer.parseInt(gradecreditsStr);
//            }
//            return gradcredits;
//        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null, 9999, 9999, false, false);
            }
            return _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMst = new HashMap<String, SubclassMst>();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " VALUE(T2.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int classShoworder3 = rs.getInt("CLASS_SHOWORDER3");
                    final int subclassShoworder3 = rs.getInt("SUBCLASS_SHOWORDER3");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), classShoworder3, subclassShoworder3, isSaki, isMoto);
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getCredits(final String subclasscd, final String course) {
            return _creditMst.get(subclasscd + ":" + course);
        }

        private void setCreditMst(final DB2UDB db2) {
            _creditMst = new HashMap<String, String>();
            String sql = "";
            sql += " SELECT ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
            sql += " T1.SUBCLASSCD AS SUBCLASSCD,  ";
            sql += " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE,  ";
            sql += " T1.CREDITS  ";
            sql += " FROM CREDIT_MST T1 ";
            sql += " WHERE T1.YEAR = '" + _year + "' ";
            sql += "   AND T1.GRADE = '" + _grade + "' ";
            sql += "   AND T1.CREDITS IS NOT NULL";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                _creditMst.put(KnjDbUtils.getString(row, "SUBCLASSCD") + ":" + KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "CREDITS"));
            }
        }

        public List<String> getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
            final String[] keys = {grade + "-" + coursecd + "-" + majorcd, "00" + "-" + coursecd + "-" + majorcd, grade + "-" + "0" + "-" + "000", "00" + "-" + "0" + "-" + "000"};
            for (int i = 0; i < keys.length; i++) {
                final List<String> rtn = _recordMockOrderSdivDatMap.get(keys[i]);
                if (null != rtn) {
                    log.info(" set key = " + keys[i]);
                    return rtn;
                }
            }
            log.info(" set key = " + ArrayUtils.toString(keys) + " but nothing");
            return Collections.emptyList();
        }

        private void setRecordMockOrderSdivDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _recordMockOrderSdivDatMap = new HashMap<String, List<String>>();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
                sql += " T1.SEQ,  ";
                sql += " T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD ";
                sql += " FROM RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
                sql += " WHERE T1.YEAR = '" + _year + "' ";
                sql += "   AND T1.TEST_DIV = '1' ";
                sql += " ORDER BY ";
                sql += " T1.GRADE, T1.COURSECD, T1.MAJORCD, ";
                sql += " T1.SEQ  ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key = rs.getString("GRADE") + "-" + rs.getString("COURSECD") + "-" + rs.getString("MAJORCD");
                    getMappedList(_recordMockOrderSdivDatMap, key).add(rs.getString("TESTCD"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        protected Map<String, TestItem> getTestItemMap(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map<String, TestItem> testitemMap = new HashMap<String, TestItem>();
            try {
                final String sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD "
                                 +  " ,TESTITEMNAME "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
                                 + "  ,T1.SEMESTER_DETAIL "
                                 + "  ,T2.SDATE "
                                 + "  ,T2.EDATE "
                                 +  " ,CASE WHEN T1.SEMESTER <= '" + _semester + "' THEN 1 ELSE 0 END AS PRINT "
                                 +  " ,NMD053.NAME1 AS SCORE_DIV_NAME "
                                 +  "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 "
                                 +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
                                 +  " AND T2.SEMESTER = T1.SEMESTER "
                                 +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                                 +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' "
                                 +  "WHERE T1.YEAR = '" + _year + "' "
                                 +  " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("TESTCD");
                    final TestItem testitem = new TestItem();
                    testitem._testcd = testcd;
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._sidouinput = rs.getString("SIDOU_INPUT");
                    testitem._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                    testitem._semester = rs.getString("SEMESTER");
                    testitem._semesterDetail = rs.getString("SEMESTER_DETAIL");
                    testitem._dateRange = new DateRange(testitem._testcd, testitem._semester, testitem._testitemname, rs.getString("SDATE"), rs.getString("EDATE"));
                    testitem._printScore = "1".equals(rs.getString("PRINT"));
                    testitem._scoreDivName = rs.getString("SCORE_DIV_NAME");
                    testitemMap.put(testcd, testitem);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemMap;
        }

        protected List<DateRange> getSemesterDetailList(
                final DB2UDB db2
        ) {
            final List<DateRange> semesterDetailList = new ArrayList<DateRange>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME, T1.SEMESTER_DETAIL "
                                 + "  ,T1.SDATE "
                                 + "  ,T1.EDATE "
                                 + " FROM SEMESTER_DETAIL_MST T1 "
                                 + " WHERE T1.YEAR = '" + _year + "' "
                                 + " ORDER BY T1.SEMESTER_DETAIL ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    semesterDetailList.add(new DateRange(rs.getString("SEMESTER_DETAIL"), rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterDetailList;
        }

        private void loadAttendRemarkHrMap(final DB2UDB db2) {
            _attendRemarkHrMap = new HashMap();
            if ("1".equals(_useAttendSemesHrRemark) && _hasATTEND_SEMES_REMARK_HR_DAT) {
                final StringBuffer hrsql = new StringBuffer();
                hrsql.append(" SELECT ");
                hrsql.append("     RMK.SEMESTER, ");
                hrsql.append("     SEMED.SEMESTER_DETAIL, ");
                hrsql.append("     RMK.MONTH, ");
                hrsql.append("     RMK.REMARK1 AS REMARK ");
                hrsql.append(" FROM ");
                hrsql.append("     ATTEND_SEMES_REMARK_HR_DAT RMK ");
                hrsql.append("     INNER JOIN (SELECT YEAR, SEMESTER, MONTH, MAX(DATE(YEAR || '-' || MONTH || '-' || TRIM(CAST(APPOINTED_DAY AS VARCHAR(2))))) AS APPOINTED_DATE ");
                hrsql.append("                 FROM ATTEND_SEMES_DAT ");
                hrsql.append("                 GROUP BY YEAR, SEMESTER, MONTH ");
                hrsql.append("                 ) T2 ON T2.YEAR = RMK.YEAR AND T2.SEMESTER = RMK.SEMESTER AND T2.MONTH = RMK.MONTH ");
                hrsql.append("     INNER JOIN SEMESTER_DETAIL_MST SEMED ON SEMED.YEAR = RMK.YEAR AND SEMED.SEMESTER = RMK.SEMESTER AND T2.APPOINTED_DATE BETWEEN SEMED.SDATE AND SEMED.EDATE ");
                hrsql.append(" WHERE ");
                hrsql.append("     RMK.YEAR = '" + _year + "' ");
                hrsql.append("     AND RMK.GRADE = '" + _grade + "' ");
                hrsql.append("     AND RMK.HR_CLASS = '" + _gradeHrclass.substring(2) + "' ");
                hrsql.append(" ORDER BY RMK.SEMESTER, SEMED.SEMESTER_DETAIL, INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END  ");

                if (_isOutputDebug) {
                    log.info(" hrsql = " + hrsql);
                }
                for (final Map row : KnjDbUtils.query(db2, hrsql.toString())) {
                    final String remark = KnjDbUtils.getString(row, "REMARK");
                    final String semesterDetail = KnjDbUtils.getString(row, "SEMESTER_DETAIL");
                    if (null != remark && null != semesterDetail) {
                        getMappedList(_attendRemarkHrMap, semesterDetail).add(remark.replace("\r\n", " "));
                    }
                }
            }

            if (_hasATTEND_BATCH_INPUT_HDAT) {
                final StringBuffer hrsql = new StringBuffer();
                hrsql.append(" SELECT ");
                hrsql.append("     T1.SEQNO ");
                hrsql.append("   , T1.FROM_DATE ");
                hrsql.append("   , T1.TO_DATE ");
                hrsql.append("   , T1.DI_REMARK ");
                hrsql.append(" FROM ATTEND_BATCH_INPUT_HDAT T1 ");
                hrsql.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR AND SEME.SEMESTER <> '9' AND T1.FROM_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
                hrsql.append(" LEFT JOIN ATTEND_BATCH_INPUT_HR_DAT L1 ON ");
                hrsql.append("      L1.YEAR = T1.YEAR ");
                hrsql.append("    AND L1.SEQNO = T1.SEQNO ");
                hrsql.append("    AND L1.GRADE = '" + _grade + "' ");
                hrsql.append("    AND L1.HR_CLASS = '" + _gradeHrclass.substring(2) + "' ");
                hrsql.append("  WHERE ");
                hrsql.append("      T1.YEAR = '" + _year + "' ");
                hrsql.append("      AND ( T1.INPUT_TYPE = '1' OR T1.INPUT_TYPE = '3' AND L1.YEAR IS NOT NULL) ");
                hrsql.append("  ORDER BY ");
                hrsql.append("      T1.FROM_DATE, T1.SEQNO ");

                if (_isOutputDebug) {
                    log.info(" hrsql 2 = " + hrsql);
                }
                for (final Map row : KnjDbUtils.query(db2, hrsql.toString())) {
                    final String remark = KnjDbUtils.getString(row, "DI_REMARK");
                    if (null == remark) {
                        continue;
                    }
                    final String fromDate = KnjDbUtils.getString(row, "FROM_DATE");
                    final String toDate = KnjDbUtils.getString(row, "TO_DATE");
                    if (null != fromDate && null != toDate) {
                        if (fromDate.equals(toDate)) {
                            getMappedList(_attendRemarkHrMap, SEMEALL).add(KNJ_EditDate.h_format_JP_MD(fromDate) + " " + remark);
                        } else {
                            getMappedList(_attendRemarkHrMap, SEMEALL).add(KNJ_EditDate.h_format_JP_MD(fromDate) + "～" + KNJ_EditDate.h_format_JP_MD(toDate) + " " + remark);
                        }
                    } else {
                        getMappedList(_attendRemarkHrMap, SEMEALL).add(remark);
                    }
                }
            }
        }
    }
}
