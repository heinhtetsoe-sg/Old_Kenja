// kanji=漢字
/*
 * 作成日: 2021/03/23
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.AbstractHashedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

public class KNJD626K {
    private static final Log log = LogFactory.getLog(KNJD626K.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_02 = "02";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";

    private static final String ATTRIBUTE_KETTEN = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";
    private static final String ATTRIBUTE_CENTERING = "Hensyu=3"; // 中央寄せ

    private static final String csv = "csv";

    private static final String ALL9 = "999999";
    protected boolean _hasData;

    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = createParam(request, db2);

            if (csv.equals(_param._cmd)) {
                final List<List<String>> outputLines = new ArrayList<List<String>>();
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                setOutputCsvLines(db2, outputLines);
                CsvUtils.outputLines(log, response, _param._title + ".csv", outputLines, csvParam);
            } else {
                svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                final IPdf ipdf = new SvfPdf(svf);

                response.setContentType("application/pdf");

                printMain(db2, ipdf);
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != _param) {
                _param.close();

                if (csv.equals(_param._cmd)) {
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

    private void setOutputCsvLines(final DB2UDB db2, final List<List<String>> outputList) {

        for (final String classSelected : _param._classSelected) { //印刷対象HR組

            final StudentGroup studentGroup = new StudentGroup(classSelected, false);
            log.info(" classSelected = " + classSelected);

            final List<Course> courses = createCourses(db2, classSelected);
            log.debug("コース数=" + courses.size());
            studentGroup._courses = courses;

            studentGroup.load(db2, null);

            // 印刷処理
            final Form form = new Form();
            form.outputCsv(db2, outputList, studentGroup);
        }
    }

    private void printMain(final DB2UDB db2, final IPdf ipdf) {
        final List<ReportInfo> bkupReportInfo = new ArrayList<ReportInfo>();
        _hasData = false;
        for (final String classSelected : _param._classSelected) { //印刷対象HR組
            final long start = System.currentTimeMillis();

            final StudentGroup studentGroup = new StudentGroup(classSelected, false);
            log.info(" classSelected = " + classSelected);

            final List<Course> courses = createCourses(db2, studentGroup._cd);
            log.debug("コース数=" + courses.size());
            studentGroup._courses = courses;

            studentGroup.load(db2, null);

            // 印刷処理
            final Form form = new Form();
            form.print(db2, ipdf, studentGroup);
            if (form._hasData) {
                _hasData = true;
            }
            // クラス毎の情報を保持する。
            studentGroup.bkupInfo(bkupReportInfo);
            final long end = System.currentTimeMillis();
            _param._elapsed.put(studentGroup._cd, new BigDecimal(end - start).divide(new BigDecimal(1000), 1, BigDecimal.ROUND_HALF_UP) + " [s]");

        }
    }

    private Map<String, List<SubClass>> createSubclasscdList(final List<ReportInfo> bkupReportInfo) {
        Map<String, List<SubClass>> retlist = new LinkedMap();
        for (final ReportInfo repoinfo : bkupReportInfo) {
            for (final SubClass sc : new ArrayList<SubClass>(repoinfo._reportlist._subclasses.values())) {
                if (!retlist.containsKey(sc._subclasscd)) {
                    retlist.put(sc._subclasscd, new ArrayList<SubClass>());
                }
                retlist.get(sc._subclasscd).add(sc);
            }
        }
        return retlist;
    }

    /**
     *
     * @param subclassCnt
     * @param subclassOrder
     * @param abbvName
     * @param abbvLen
     * @param abbvStrCnt
     * @return
     */
    private static ClassAbbvFieldSet setClassAbbv(final int subclassCnt, final int subclassOrder, final String abbvName, final int abbvLen, final int abbvStrCnt) {
        String fieldNum  = "";
        String setString = "";
        ClassAbbvFieldSet retData = new ClassAbbvFieldSet(fieldNum, setString);
        try {
            if (0 != subclassCnt && 0 != subclassOrder && null != abbvName && 0 != abbvLen && 0 != abbvStrCnt) {
                if (1 == subclassCnt) {
                    fieldNum  = (abbvLen > 6) ? "3": (abbvLen > 4) ? "2": "1";
                    setString = abbvName;
                } else if (2 == subclassCnt) {
                    if (abbvStrCnt > 4) {
                        fieldNum = "2";
                        setString = (1 == subclassOrder) ? abbvName.substring(0, 3): abbvName.substring(3) + "　";
                    } else {
                        fieldNum = "1";
                        if (3 == abbvStrCnt || 4 == abbvStrCnt) {
                            setString = (1 == subclassOrder) ? abbvName.substring(0, 2): abbvName.substring(2) + "　";
                        } else if (2 == abbvStrCnt || 1 == abbvStrCnt) {
                            setString = (1 == subclassOrder) ? "　"+ abbvName.substring(0, 1): abbvName.substring(1) + "　";
                        }
                    }
                }
                if (subclassCnt > 2) {
                    fieldNum = "1";
                    final boolean oddNumber = subclassCnt % 2 == 1;
                    final int herfNo        = subclassCnt / 2;
                    final int middleNo      = herfNo + 1;
                    final int noTextNo      = herfNo + 2;
                    final int evenStartNo   = herfNo - 1;

                    //科目数が奇数の時
                    if (oddNumber) {
                        if (5 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo < subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? abbvName.substring(0, 2): (middleNo == subclassOrder) ? abbvName.substring(2, 4): abbvName.substring(4) + "　";
                            }
                        } else if (3 == abbvStrCnt || 4 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo < subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? "　"+ abbvName.substring(0, 1): (middleNo == subclassOrder) ? abbvName.substring(1, 3): abbvName.substring(3) + "　";
                            }
                        } else if (2 == abbvStrCnt || 1 == abbvStrCnt) {
                            if (subclassOrder != middleNo) {
                                setString = "";
                            } else {
                                setString = abbvName.substring(0);
                            }
                        }

                    // 科目数が偶数の時
                    } else {
                        if (5 == abbvStrCnt) {
                            if (subclassOrder < evenStartNo || noTextNo <= subclassOrder) {
                                setString = "";
                            } else {
                                setString = (evenStartNo == subclassOrder) ? "　"+ abbvName.substring(0, 1): (herfNo == subclassOrder) ? abbvName.substring(1, 3): abbvName.substring(3);
                            }
                        } else if (3 == abbvStrCnt || 4 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo <= subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? abbvName.substring(0, 2): abbvName.substring(2) + "　";
                            }
                        } else if (2 == abbvStrCnt || 1 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo <= subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? "　"+ abbvName.substring(0, 1): abbvName.substring(1) + "　";
                            }
                        }
                    }
                }
            }
            retData = new ClassAbbvFieldSet(fieldNum, setString);
        } catch (final Exception ex) {
            log.error("classAbbvSetError!!", ex);
        }

        return retData;
    }

    private static String stringSum(final List<String> nums) {
        String n = null;
        for (final String num : nums) {
            n = addnum(n, num);
        }
        return n;
    }

    private static String sum(final List<Integer> nums) {
        if (nums.isEmpty()) {
            return "";
        }
        BigDecimal sum = new BigDecimal(0);
        for (final Integer num : nums) {
            sum = sum.add(new BigDecimal(num));
        }
        return sum.toString();
    }

    private static String avg(final List<Integer> nums) {
        final String sum = sum(nums);
        if (!NumberUtils.isNumber(sum)) {
            return sum;
        }
        return new BigDecimal(sum).divide(new BigDecimal(nums.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String max(final List<Integer> nums) {
        if (nums.isEmpty()) {
            return "";
        }
        Collections.sort(nums);
        return nums.get(nums.size() - 1).toString();
    }

    private static String min(final List<Integer> nums) {
        if (nums.isEmpty()) {
            return "";
        }
        Collections.sort(nums);
        return nums.get(0).toString();
    }

    private static Integer toInt(final String numstr, final Integer def) {
        if (NumberUtils.isNumber(numstr)) {
            return (int) Double.parseDouble(numstr);
        }
        return def;
    }

    private static BigDecimal toBigDecimal(final String numstr, final BigDecimal def) {
        if (NumberUtils.isNumber(numstr)) {
            return new BigDecimal(numstr);
        }
        return def;
    }

    public static <A, B> Set<B> getMappedTreeSet(final Map<A, Set<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeSet<B>());
        }
        return map.get(key1);
    }

    public static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    public static <A, B, C> Map<B, C> getMappedHashMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static String nullToBlank(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String addnum(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishaGonyu2(final String val, final int keta) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(keta, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String prepend(final String a, final String b) {
        if (StringUtils.isEmpty(b)) {
            return "";
        }
        return a + b;
    }

    private static String append(final String a, final String b) {
        if (StringUtils.isEmpty(a)) {
            return "";
        }
        return a + b;
    }

    private static String kakko(final String s) {
        return prepend("（", append(s, "）"));
    }

    private static String mkString(final Collection<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String comma0 = "";
        for (final String s : list) {
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    // StringUtils.defaultString
    private static String defstr(final Object str1, final String ... str2) {
        if (null != str1) {
            return str1.toString();
        }
        if (null != str2) {
            for (final String s : str2) {
                if (null != s) {
                    return s;
                }
            }
        }
        return "";
    }

    private class Course {
        final String _grade;
        final String _hrclass;
        final String _code;
        final String _name;

        public Course(
                final String grade,
                final String hrclass,
                final String code,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _code = code;
            _name = name;
        }

        public String toString() {
            return _code + ":" + _name;
        }
    }

    private List<Course> createCourses(final DB2UDB db2, final String gradeHrclass) {
        final List<Course> rtn = new ArrayList<Course>();

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
        stb.append("     W1.YEAR = '" + _param._year + "' AND ");
        if (!SEMEALL.equals(_param._semester)) {
            stb.append(" W1.SEMESTER = '" + _param._semester + "' AND ");
        } else {
            stb.append(" W1.SEMESTER = '" + _param._semeFlg + "' AND ");
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

        for (final Map row : KnjDbUtils.query(db2, sql)) {
            final String grade = KnjDbUtils.getString(row, "GRADE");
            final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
            final String coursecd = KnjDbUtils.getString(row, "COURSECD");
            final String name = KnjDbUtils.getString(row, "COURSECODENAME");

            final Course course = new Course(grade, hrclass, coursecd, name);

            rtn.add(course);
        }
        return rtn;
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private class StudentGroup implements Comparable<StudentGroup> {
        static final String AVG_FLG_HR = "HR";
        static final String AVG_FLG_GRADE = "GRADE";
        static final String AVG_FLG_COURSE = "COURSE";
        static final String AVG_FLG_MAJOR = "MAJOR";

        final String _cd;
        final boolean _isCourse;
        String _courseName;
        private List<Course> _courses;
        private boolean _isPrintHrCoursePage;
        private String _staffname;
        private String _hrName;
        private String _gradeName1;
        private String _majorname;
        private String _coursecodename;
        private String _avgKansanScore;
        private String _avgTotalKansanScore;

        private List<Student> _students = Collections.emptyList();

        private final Map<String, SubClass> _subclasses = new LinkedHashMap<String, SubClass>();
        private Map<String, String> _recordRankSdivAverageMap;
        private List<String> _attendHrRemark = Collections.EMPTY_LIST;

        public StudentGroup(final String cd, final boolean isCourse) {
            _cd = cd;
            _isCourse = isCourse;
        }

        public void load(final DB2UDB db2, final String course) {
            final Map<String, String> row = Hrclass_Staff(db2);
            _staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
            _hrName = StringUtils.defaultString(KnjDbUtils.getString(row, "HR_NAME"));

            final Map<String, String> kansanRow = hrClassKansanScore(db2);
            _avgKansanScore = StringUtils.defaultString(KnjDbUtils.getString(kansanRow, "AVG_KANSAN_SCORE"), "0");
            _avgTotalKansanScore = StringUtils.defaultString(KnjDbUtils.getString(kansanRow, "AVG_TOTAL_KANSAN_SCORE"), "0");

            if (_param._isOutputDebug) {
                log.info(" load student.");
            }
            _students = loadStudents(db2, course);

            final Map<String, Student> studentMap = new HashMap();
            for (final Student student : _students) {
                studentMap.put(student._schregno, student);
            }
            if (_param._isOutputDebug) {
                log.info(" load rank.");
            }
            loadRank(db2, studentMap);

            setStudentsInfo(db2, _students);
            loadAttend(db2, _students);
            if (_param._isOutputDebug) {
                log.info(" load attendRemark.");
            }
            loadAttendRemark(db2, studentMap);
            if (_param._isOutputDebug) {
                log.info(" load hrclassAverage.");
            }
            _recordRankSdivAverageMap = getRecordRankSdivAverageMap(db2, _students);

            loadScoreDetail(db2, studentMap);
            if (_param._isOutputDebug) {
                log.info(" set subclass average.");
            }
            setSubclassAverage(_students);
            setSubclassGradeAverage(db2);
        }

        public String getName() {
            String rtn = "";
            rtn = _hrName + (_isCourse ? "(" + defstr(_courseName) + ")" : "");
            return rtn;
        }

        public Map<String, String> Hrclass_Staff(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("   REGDH.HR_NAME,");
            sql.append("   REGDH.HR_NAMEABBV,");
            sql.append("   STFM.STAFFNAME ");
            sql.append(" FROM ");
            sql.append("   SCHREG_REGD_HDAT REGDH ");
            sql.append("   LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = REGDH.TR_CD1 ");
            sql.append(" WHERE ");
            sql.append("   REGDH.YEAR = '" + _param._year + "' ");
            sql.append("   AND REGDH.GRADE || REGDH.HR_CLASS = '" + _cd + "' ");
            if (!SEMEALL.equals(_param._semester)) {
                sql.append("   AND REGDH.SEMESTER = '" + _param._semester + "' ");
            } else {
                sql.append("   AND REGDH.SEMESTER = '" + _param._semeFlg + "' ");
            }

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
        }

        public Map<String, String> hrClassKansanScore(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("   REGDH.GRADE, ");
            sql.append("   REGDH.HR_CLASS, ");
            sql.append("   AVG(KANSAN.KANSAN_SCORE) AS AVG_KANSAN_SCORE, ");
            sql.append("   AVG(KANSAN_T.KANSAN_SCORE) AS AVG_TOTAL_KANSAN_SCORE ");
            sql.append(" FROM ");
            sql.append("   SCHREG_REGD_HDAT REGDH ");
            sql.append("   INNER JOIN SCHREG_REGD_DAT REGD ");
            sql.append("           ON REGD.YEAR     = REGDH.YEAR ");
            sql.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
            sql.append("          AND REGD.GRADE    = REGDH.GRADE ");
            sql.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            sql.append("   LEFT JOIN RECORD_SCORE_KANSAN_DAT KANSAN ");
            sql.append("           ON KANSAN.YEAR     = REGD.YEAR ");
            sql.append("          AND KANSAN.GRADE    = REGD.GRADE ");
            sql.append("          AND KANSAN.SEMESTER = REGD.SEMESTER ");
            sql.append("          AND KANSAN.SCHREGNO = REGD.SCHREGNO ");
            sql.append("   LEFT JOIN RECORD_SCORE_KANSAN_TOTAL_DAT KANSAN_T ");
            sql.append("           ON KANSAN_T.YEAR     = REGD.YEAR ");
            sql.append("          AND KANSAN_T.GRADE    = REGD.GRADE ");
            sql.append("          AND KANSAN_T.SCHREGNO = REGD.SCHREGNO ");
            sql.append(" WHERE ");
            sql.append("   REGDH.YEAR = '" + _param._year + "' ");
            sql.append("   AND REGDH.GRADE || REGDH.HR_CLASS = '" + _cd + "' ");
            if (!SEMEALL.equals(_param._semester)) {
                sql.append("   AND REGDH.SEMESTER = '" + _param._semester + "' ");
            } else {
                sql.append("   AND REGDH.SEMESTER = '" + _param._semeFlg + "' ");
            }
            sql.append(" GROUP BY ");
            sql.append("   REGDH.GRADE, ");
            sql.append("   REGDH.HR_CLASS ");

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
        }

        private void loadAttendRemark(final DB2UDB db2, final Map<String, Student> studentMap) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     SD.SCHREGNO, ");
            sql.append("     RMK.SEMESTER, ");
            sql.append("     RMK.MONTH, ");
            sql.append("     RMK.REMARK1 AS REMARK ");
            sql.append(" FROM ");
            sql.append("     SCHREG_REGD_DAT SD ");
            sql.append(" INNER JOIN ATTEND_SEMES_REMARK_DAT RMK ON SD.YEAR = RMK.YEAR ");
            sql.append("      AND SD.SCHREGNO = RMK.SCHREGNO ");
            sql.append("      AND RMK.SEMESTER = '" + _param._semester + "' ");
            sql.append("      AND INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END <= " + _param.getAttendRemarkMonth(db2, _param._date) + " ");
            sql.append(" WHERE ");
            sql.append("     SD.YEAR = '" + _param._year + "' ");
            sql.append("     AND SD.SEMESTER = '" + (SEMEALL.equals(_param._semester) ? _param._semeFlg : _param._semester) + "' ");
            sql.append("     AND SD.GRADE = '" + _param._grade + "' ");
            sql.append(" ORDER BY SD.SCHREGNO, RMK.SEMESTER, INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END  ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                final Student student = getStudent(studentMap, KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                student._attendSemesRemarkDatRemark1 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK"));
            }

            final Set<String> argSet = new HashSet<String>();
            for (final Student student : studentMap.values()) {
                final String a = "('" + _param._grade + "', '" + student._hrClass + "')";
                argSet.add(a);
            }
            final String argGradeHrclass = mkString(argSet, ", ");
            if (argGradeHrclass.length() > 0) {
                _attendHrRemark = new ArrayList<String>();
                if ("1".equals(_param._useAttendSemesHrRemark) && _param._hasATTEND_SEMES_REMARK_HR_DAT) {
                    final StringBuffer hrsql = new StringBuffer();
                    hrsql.append(" SELECT ");
                    hrsql.append("     RMK.SEMESTER, ");
                    hrsql.append("     RMK.MONTH, ");
                    hrsql.append("     RMK.REMARK1 AS REMARK ");
                    hrsql.append(" FROM ");
                    hrsql.append("     ATTEND_SEMES_REMARK_HR_DAT RMK ");
                    hrsql.append(" WHERE ");
                    hrsql.append("     RMK.YEAR = '" + _param._year + "' ");
                    hrsql.append("     AND RMK.SEMESTER = '" + _param._semester + "' ");
                    hrsql.append("     AND INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END <= " + _param.getAttendRemarkMonth(db2, _param._date) + " ");
                    hrsql.append("     AND (RMK.GRADE, RMK.HR_CLASS) IN (VALUES " + argGradeHrclass.toString() + ") ");
                    hrsql.append(" ORDER BY RMK.SEMESTER, INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END  ");

                    if (_param._isOutputDebugQuery) {
                        log.info(" hrsql = " + hrsql);
                    }
                    for (final Map row : KnjDbUtils.query(db2, hrsql.toString())) {
                        final String remark = KnjDbUtils.getString(row, "REMARK");
                        if (null != remark) {
                            _attendHrRemark.add(remark);
                        }
                    }
                }

                if (_param._hasATTEND_BATCH_INPUT_HDAT) {
                    final StringBuffer hrsql = new StringBuffer();
                    hrsql.append(" SELECT ");
                    hrsql.append("     T1.SEQNO ");
                    hrsql.append("   , T1.FROM_DATE ");
                    hrsql.append("   , T1.TO_DATE ");
                    hrsql.append("   , T1.DI_REMARK ");
                    hrsql.append(" FROM ATTEND_BATCH_INPUT_HDAT T1 ");
                    hrsql.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ");
                    hrsql.append("      AND SEME.SEMESTER <> '9' ");
                    hrsql.append("      AND T1.FROM_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
                    hrsql.append("      AND SEME.SEMESTER = '" + _param._semester + "' ");
                    hrsql.append(" LEFT JOIN ATTEND_BATCH_INPUT_HR_DAT L1 ON ");
                    hrsql.append("      L1.YEAR = T1.YEAR ");
                    hrsql.append("    AND L1.SEQNO = T1.SEQNO ");
                    hrsql.append("    AND (L1.GRADE, L1.HR_CLASS) IN (VALUES " + argGradeHrclass.toString() + ") ");
                    hrsql.append("  WHERE ");
                    hrsql.append("      T1.YEAR = '" + _param._year + "' ");
                    hrsql.append("      AND ( T1.INPUT_TYPE = '1' OR T1.INPUT_TYPE = '3' AND L1.YEAR IS NOT NULL) ");
                    hrsql.append("  ORDER BY ");
                    hrsql.append("      T1.FROM_DATE, T1.SEQNO ");

                    if (_param._isOutputDebugQuery) {
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
                                _attendHrRemark.add(KNJ_EditDate.h_format_JP_MD(fromDate) + " " + remark);
                            } else {
                                _attendHrRemark.add(KNJ_EditDate.h_format_JP_MD(fromDate) + "～" + KNJ_EditDate.h_format_JP_MD(toDate) + " " + remark);
                            }
                        } else {
                            _attendHrRemark.add(remark);
                        }
                    }
                }
            }
        }

        private List<Student> loadStudents(final DB2UDB db2, final String course) {
            final List<Student> students = new LinkedList();
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("    REGD.SCHREGNO, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO, ");
            stb.append("    CASE WHEN W3.SEX = '1' THEN '〇' ELSE '' END AS SEX, ");
            stb.append("    A053.ABBV1 AS FINSCHOOL, ");
            stb.append("    REGD.COURSECD || REGD.MAJORCD AS MAJOR, ");
            stb.append("    REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("    CCM.COURSECODENAME ");
            stb.append("     , REFUS.SCHREGNO AS REFUSAL ");
            stb.append("      , KANSAN.KANSAN_SCORE ");
            stb.append("      , KANSAN_T.KANSAN_SCORE AS TOTAL_KANSAN_SCORE ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_DE ON REGD.SCHREGNO = BASE_DE.SCHREGNO ");
            stb.append("         AND BASE_DE.BASE_SEQ = '014' ");
            stb.append("    LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND W3.SEX = Z002.NAMECD2 ");
            stb.append("    LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("    LEFT JOIN SCHREG_SCHOOL_REFUSAL_DAT REFUS ON REFUS.YEAR = REGD.YEAR ");
            stb.append("          AND REFUS.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_BASE_DETAIL_MST BD016 ");
            stb.append("           ON BD016.SCHREGNO = REGD.SCHREGNO ");
            stb.append("          AND BD016.BASE_SEQ = '016' ");
            stb.append("    LEFT JOIN NAME_MST A053 ");
            stb.append("           ON A053.NAMECD2 = BD016.BASE_REMARK1 ");
            stb.append("          AND A053.NAMECD1 = 'A053' ");
            stb.append("     LEFT JOIN RECORD_SCORE_KANSAN_DAT KANSAN ");
            stb.append("            ON KANSAN.YEAR     = REGD.YEAR ");
            stb.append("           AND KANSAN.GRADE    = REGD.GRADE ");
            stb.append("           AND KANSAN.SEMESTER = REGD.SEMESTER ");
            stb.append("           AND KANSAN.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_SCORE_KANSAN_TOTAL_DAT KANSAN_T ");
            stb.append("            ON KANSAN_T.YEAR     = REGD.YEAR ");
            stb.append("           AND KANSAN_T.GRADE    = REGD.GRADE ");
            stb.append("           AND KANSAN_T.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("    REGD.YEAR = '" + _param._year + "' ");
            if (!SEMEALL.equals(_param._semester)) {
                stb.append("    AND REGD.SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append("    AND REGD.SEMESTER = '" + _param._semeFlg + "' ");
            }
            stb.append("    AND REGD.GRADE = '" + _cd.substring(0, 2) + "' AND REGD.HR_CLASS = '" + _cd.substring(2) + "' ");
            if (null != course) {
                stb.append("    AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE = '" + course + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO");

            final String sql = stb.toString();
            log.debug("sql = "+ sql);
            int gnum = 0;
            final Integer zero = new Integer(0);
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                gnum = KnjDbUtils.getInt(row, "ATTENDNO", zero).intValue();

                final String schregno = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHREGNO"));
                final String sex = StringUtils.defaultString(KnjDbUtils.getString(row, "SEX"));
                final String finschool = StringUtils.defaultString(KnjDbUtils.getString(row, "FINSCHOOL"));
                final String coursecodename = StringUtils.defaultString(KnjDbUtils.getString(row, "COURSECODENAME"));
                String schoolRefusal = StringUtils.defaultString(KnjDbUtils.getString(row, "REFUSAL"));
                final String hrClass = StringUtils.defaultString(KnjDbUtils.getString(row, "HR_CLASS"));
                final String regdCourse = StringUtils.defaultString(KnjDbUtils.getString(row, "COURSE"));
                final String major = StringUtils.defaultString(KnjDbUtils.getString(row, "MAJOR"));
                final String kansanScore = StringUtils.defaultString(KnjDbUtils.getString(row, "KANSAN_SCORE"), "0");
                final String totalKansanScore = StringUtils.defaultString(KnjDbUtils.getString(row, "TOTAL_KANSAN_SCORE"), "0");
                final Student student = new Student(schregno, hrClass, regdCourse, major, sex, finschool, coursecodename, this, schoolRefusal, kansanScore, totalKansanScore);

                student._gnum = gnum;
                students.add(student);
            }
            return students;
        }

        private void setStudentsInfo(final DB2UDB db2, final List<Student> students) {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  REGD.SCHREGNO, REGD.ATTENDNO, BASE.NAME, BASE.NAME_ENG, REGDH.HR_NAME ");
            stb.append("      , ENTGRD.ENT_DIV ");
            stb.append("      , ENTGRD.GRD_DIV ");
            stb.append("      , ENTGRD.ENT_DATE ");
            stb.append("      , ENTGRD.GRD_DATE ");
            stb.append("      , TRF.TRANSFERCD ");
            stb.append("      , TRF.TRANSFER_SDATE ");
            stb.append("      , TRF.TRANSFER_EDATE ");
            stb.append("FROM   SCHREG_REGD_DAT REGD ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("INNER  JOIN V_SEMESTER_GRADE_MST    W2 ON W2.YEAR = REGD.YEAR AND W2.SEMESTER = '" + _param._semester + "' AND W2.GRADE = REGD.GRADE ");
            stb.append("INNER  JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
            stb.append("LEFT   JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                              AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("                              AND ((ENTGRD.GRD_DIV IN ('2','3','6','7') AND ENTGRD.GRD_DATE <= W2.EDATE) ");
            stb.append("                                OR (ENTGRD.ENT_DIV IN ('4','5','7') AND ENTGRD.ENT_DATE >= W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT TRF ON TRF.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                                  AND (TRF.TRANSFERCD IN ('1','2') ");
            stb.append("                                  AND ( W2.EDATE BETWEEN TRF.TRANSFER_SDATE AND TRF.TRANSFER_EDATE ");
            stb.append("                                        OR TRF.TRANSFER_SDATE BETWEEN W2.SDATE AND W2.EDATE ");
            stb.append("                                        OR TRF.TRANSFER_EDATE BETWEEN W2.SDATE AND W2.EDATE) ) ");
            stb.append("WHERE   REGD.YEAR = '" + _param._year + "' ");
            stb.append("    AND REGD.SCHREGNO = ? ");
            if (!SEMEALL.equals(_param._semester)) {
                stb.append("AND REGD.SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append("AND REGD.SEMESTER = '" + _param._semeFlg + "' ");
            }

            final String psKey = "STUDENT_BASE_KEY";
            try {
                if (!_param._psMap.containsKey(psKey)) {
                    _param._psMap.put(psKey, db2.prepareStatement(stb.toString()));
                }
            } catch (Exception e) {
                log.error("Exception", e);
                return;
            }

            final Map<String, String> a002Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A002' "), "NAMECD2", "NAME1");
            final Map<String, String> a003Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A003' "), "NAMECD2", "NAME1");
            final Map<String, String> a004Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A004' "), "NAMECD2", "NAME1");

            for (final Student student : students) {

                final Map rs = KnjDbUtils.firstRow(KnjDbUtils.query(db2, _param.getPs(psKey), new Object[] {student._schregno}));
                if (!rs.isEmpty()) {
                    String transInfo = "";

                    final String entDate = KnjDbUtils.getString(rs, "ENT_DATE");
                    final String grdDate = KnjDbUtils.getString(rs, "GRD_DATE");
                    final String transferSdate = KnjDbUtils.getString(rs, "TRANSFER_SDATE");
                    if (null != grdDate) {
                        final String name1 = a003Name1Map.get(KnjDbUtils.getString(rs, "GRD_DIV"));
                        if (null != name1) {
                            transInfo = KNJ_EditDate.h_format_JP(db2, grdDate) + name1;
                        }
                    } else if (null != entDate) {
                        final String name1 = a002Name1Map.get(KnjDbUtils.getString(rs, "ENT_DIV"));
                        if (null != name1) {
                            transInfo = KNJ_EditDate.h_format_JP(db2, entDate) + name1;
                        }

                    } else if (null != transferSdate) {
                        final String a004Name1 = a004Name1Map.get(KnjDbUtils.getString(rs, "TRANSFERCD"));
                        final String transferEdate = KnjDbUtils.getString(rs, "TRANSFER_EDATE");

                        if (null != transferSdate && null != a004Name1) {
                            final StringBuffer sb = new StringBuffer();
                            sb.append(KNJ_EditDate.h_format_JP(db2, transferSdate));
                            if (null != transferEdate) {
                                sb.append("～");
                                sb.append(KNJ_EditDate.h_format_JP(db2, transferEdate));
                            }
                            sb.append(a004Name1);
                            transInfo = sb.toString();
                        }
                    }
                    student._attendno = KnjDbUtils.getString(rs, "ATTENDNO");
                    student._name = KnjDbUtils.getString(rs, "NAME");
                    student._nameEng = KnjDbUtils.getString(rs, "NAME_ENG");
                    student._transInfo = transInfo;
                }
            }
        }

        private Student getStudent(final Map<String, Student> studentMap, String code) {
            if (code == null) {
                return null;
            }
            return studentMap.get(code);
        }

        private void loadAttend(final DB2UDB db2, final List<Student> students) {

            final String psKey = "ATTENDSEMES";
            if (null == _param.getPs(psKey)) {
                final Map attendParamMap;
                if (_param._schChrDatCount == 0) {
                    attendParamMap = new HashMap(_param._attendParamMap); // コピーする
                    attendParamMap.put("hrClass", "?");
                } else {
                    attendParamMap = _param._attendParamMap;
                    attendParamMap.put("schregno", "?");
                }

                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._year,
                        _param._semester,
                        _param._sdate,
                        _param._date,
                        attendParamMap
                );
                log.debug(" sql = " + sql);

                try {
                    _param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("exception!", e);
                    return;
                }
            }

            if (_param._isOutputDebug) {
                log.info("load attend.");
            }

            final long attendStartTime = System.currentTimeMillis();
            long attendAccTime = 0;

            if (_param._schChrDatCount == 0) {
                final Set<String> hrClasses = new HashSet<String>();
                final Map<String, Student> studentMap = new HashMap<String, Student>();
                for (final Student student : students) {
                    if (null == student._hrClass) {
                        continue;
                    }
                    hrClasses.add(student._hrClass);
                    studentMap.put(student._schregno, student);
                }

                for (final String hrClass : hrClasses) {

                    final long indStart = System.currentTimeMillis();
                    final List<Map<String, String>> rowList = KnjDbUtils.query(db2, _param.getPs(psKey), new Object[] {hrClass});
                    final long indEnd = System.currentTimeMillis();
                    attendAccTime += indEnd - indStart;

                    for (final Map row : rowList) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student) {
                            continue;
                        }

                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }

                        student._attendInfo = createAttendInfo(row);
                    }
                }

                final long attendEndTime = System.currentTimeMillis();
                if (_param._isOutputDebug) {
                    log.info("load attend (hr) elapsed time = " + (attendEndTime - attendStartTime) + "[ms] ( query time = " + attendAccTime + "[ms] / student count = " + students.size() + ")");
                }

            } else {
                for (final Student student : students) {

                    final long indStart = System.currentTimeMillis();
                    final List<Map<String, String>> rowList = KnjDbUtils.query(db2, _param.getPs(psKey), new Object[] {student._schregno});
                    final long indEnd = System.currentTimeMillis();
                    attendAccTime += indEnd - indStart;

                    for (final Map row : rowList) {
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }

                        student._attendInfo = createAttendInfo(row);
                    }
                }

                final long attendEndTime = System.currentTimeMillis();
                if (_param._isOutputDebug) {
                    log.info("load attend elapsed time = " + (attendEndTime - attendStartTime) + "[ms] ( query time = " + attendAccTime + "[ms] / student count = " + students.size() + ")");
                }
            }

        }

        private AttendInfo createAttendInfo(final Map row) {
            final Integer zero = new Integer(0);
            final AttendInfo attendInfo = new AttendInfo(
                    KnjDbUtils.getInt(row, "LESSON", zero).intValue(),
                    KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),
                    KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),
                    KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),
                    KnjDbUtils.getInt(row, "SICK", zero).intValue(),
                    KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                    KnjDbUtils.getInt(row, "LATE", zero).intValue(),
                    KnjDbUtils.getInt(row, "EARLY", zero).intValue(),
                    KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue()
            );
            return attendInfo;
        }

        private String getRecordRankSdivAverage(final String groupFlg, final String subclasscd, final String field) {
            return _recordRankSdivAverageMap.get(avgKey(groupFlg, subclasscd,  field));
        }

        private String avgKey(final String groupFlg, final String subclasscd, final String field) {
            return groupFlg + "|" + subclasscd + "|" + field;
        }

        private Map<String, String> getRecordRankSdivAverageMap(final DB2UDB db2, final List<Student> students) {
            final Map<String, String> averageMap = new HashMap<String, String>();
            final Set<String> courseSet = new HashSet();
            final Set<String> majorSet = new HashSet();
            for (final Student student : students) {
                if (defstr(student._course).length() == 8) {
                    courseSet.add("('" + student._course.substring(0, 1) + "', '" + student._course.substring(1, 4) + "', '" + student._course.substring(4) + "')");
                }
                if (defstr(student._major).length() == 4) {
                    majorSet.add("('" + student._major.substring(0, 1) + "', '" + student._major.substring(1) + "')");
                }
            }
            if (courseSet.isEmpty() || majorSet.isEmpty()) {
                log.warn("warning:PARAMETER(couse or major) is NULL .");
                return averageMap;
            }

            final StringBuffer stb = new StringBuffer();
            final String testkindcd = _param._testKindCd.substring(0, 2);
            final String testitemcd = _param._testKindCd.substring(2, 4);
            final String scoreDiv = _param._testKindCd.substring(4);

            stb.append("WITH ");
            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,CASE WHEN W1.GRADE||W1.HR_CLASS = '" + _cd + "' THEN '1' ELSE '0' END AS IS_HR ");
            stb.append("            ,CASE WHEN (W1.COURSECD, W1.MAJORCD, W1.COURSECODE) IN (VALUES " + mkString(courseSet, ",") + ") THEN '1' ELSE '0' END AS IS_COURSE ");
            stb.append("            ,CASE WHEN (W1.COURSECD, W1.MAJORCD) IN (VALUES " + mkString(majorSet, ",") + ") THEN '1' ELSE '0' END AS IS_MAJOR ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = W1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");

            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND NOT (ENTGRD.GRD_DIV IN ('2','3','6','7') AND ENTGRD.GRD_DATE IS NOT NULL AND ENTGRD.GRD_DATE < W2.SDATE) "); // 学期開始日前に在籍していない生徒は対象外
            }
            stb.append("         AND W1.GRADE = '" + _param._grade + "' ");
            stb.append(") ");

            for (final String flg : Arrays.asList(StudentGroup.AVG_FLG_HR, StudentGroup.AVG_FLG_GRADE, StudentGroup.AVG_FLG_COURSE, StudentGroup.AVG_FLG_MAJOR)) {
                final StringBuffer stb2 = new StringBuffer();
                stb2.append(stb);
                stb2.append("SELECT ");
                stb2.append("        W3.SUBCLASSCD ");
                stb2.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
                stb2.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
                stb2.append("       ,COUNT(W3.SCORE) AS COUNT ");
                stb2.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
                stb2.append(" WHERE  W3.YEAR = '" + _param._year + "' ");
                stb2.append("   AND  W3.SEMESTER = '" + _param._semester + "' ");
                stb2.append("   AND  W3.TESTKINDCD = '" + testkindcd + "' AND W3.TESTITEMCD = '" + testitemcd + "' AND W3.SCORE_DIV = '" + scoreDiv + "' ");
                stb2.append("   AND  W3.SUBCLASSCD IN ('333333', '555555', '" + _param.SUBCLASSCD999999 + "') ");
                stb2.append("   AND  W3.SCHREGNO IN ( SELECT W1.SCHREGNO FROM SCHNO_A W1 ");
                if (StudentGroup.AVG_FLG_HR.equals(flg)) {
                    stb2.append(" WHERE W1.IS_HR = '1' ");
                } else if (StudentGroup.AVG_FLG_GRADE.equals(flg)) {
                } else if (StudentGroup.AVG_FLG_COURSE.equals(flg)) {
                    stb2.append(" WHERE W1.IS_COURSE = '1' ");
                } else if (StudentGroup.AVG_FLG_MAJOR.equals(flg)) {
                    stb2.append(" WHERE W1.IS_MAJOR = '1' ");
                }
                stb2.append(" ) ");
                stb2.append(" GROUP BY W3.SUBCLASSCD ");
                final String sql = stb2.toString();
                if (_param._isOutputDebugQuery) {
                    log.info(" avg " + flg + " sql = " + sql);
                }

                for (final Map row : KnjDbUtils.query(db2, sql)) {

                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                    averageMap.put(avgKey(flg, subclasscd, "TOTAL"), KnjDbUtils.getString(row, "AVG_HR_TOTAL"));
                    averageMap.put(avgKey(flg, subclasscd, "AVG"), KnjDbUtils.getString(row, "AVG_HR_AVERAGE"));
                    averageMap.put(avgKey(flg, subclasscd, "COUNT"), KnjDbUtils.getString(row, "COUNT"));
                }
            }
            return averageMap;
        }

        private String[] toArray(final Set<String> set) {
            final List<String> list = new ArrayList(set);
            final String[] arr = new String[set.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }

        private void loadRank(final DB2UDB db2, final Map<String, Student> studentMap) {
            final String testkindcd = _param._testKindCd.substring(0, 2);
            final String testitemcd = _param._testKindCd.substring(2, 4);
            final String scoreDiv = _param._testKindCd.substring(4);

            String _rankFieldName = "CLASS_RANK";
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO ");
            stb.append("          ,  W1.GRADE ");
            stb.append("          ,  W1.HR_CLASS ");
            stb.append("          ,  W1.COURSECD ");
            stb.append("          ,  W1.MAJORCD ");
            stb.append("          ,  W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = '" + _cd.substring(0, 2) + "' ");
            stb.append("         AND W1.HR_CLASS = '" + _cd.substring(2) + "' ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  RANK.SCHREGNO ");
            stb.append("       ,RANK.SUBCLASSCD ");
            stb.append("       ,RANK.CLASS_RANK ");
            stb.append("       ," + _rankFieldName + "  AS TOTAL_RANK ");
            stb.append("       ,RANK.SCORE ");
            stb.append("       ,DECIMAL(ROUND(FLOAT(RANK.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
            stb.append("  FROM RECORD_RANK_SDIV_DAT RANK ");
            stb.append("  INNER JOIN SCHNO_A T1 ");
            stb.append("      ON RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("        RANK.YEAR = '" + _param._year + "' ");
            stb.append("   AND  RANK.SEMESTER = '" + _param._semester + "' ");
            stb.append("   AND  RANK.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("   AND  RANK.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("   AND  RANK.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("   AND  RANK.SUBCLASSCD IN ('" + _param.SUBCLASSCD999999 + "') ");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = getStudent(studentMap, schregno);
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (_param.SUBCLASSCD999999.equals(subclasscd)) {
                    student._scoreSum999999 = KnjDbUtils.getString(row, "SCORE");
                    student._scoreAvg999999 = KnjDbUtils.getString(row, "TOTAL_AVG");
                    student._classRank = KnjDbUtils.getInt(row, "CLASS_RANK", new Integer(0)).intValue();
                    student._rank = KnjDbUtils.getInt(row, "TOTAL_RANK", new Integer(0)).intValue();
                }
            }
        }

        private void loadScoreDetail(final DB2UDB db2, final Map<String, Student> studentMap) {
            if (_param._isOutputDebug) {
                log.info(" load scoreDetail.");
            }
            final Integer zero = new Integer(0);

            final String psKeyScoreDetail = "SCORE_DETAIL";
            try {
                if (null == _param.getPs(psKeyScoreDetail)) {
                    final String sql = sqlStdSubclassDetail();
                    if (_param._isOutputDebugQuery) {
                        log.info(" subclass detail sql = " + sql);
                    } else {
                        log.debug(" subclass detail sql = " + sql);
                    }

                    _param._psMap.put(psKeyScoreDetail, db2.prepareStatement(sql));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }

            Object[] arg = new Object[] {_cd.substring(0, 2), _cd.substring(2)};

            final long scoreSubclassStart = System.currentTimeMillis();

            final long scoreStartTime = System.currentTimeMillis();
            final List<Map<String, String>> rowListScore = KnjDbUtils.query(db2, _param.getPs(psKeyScoreDetail), arg);
            final long scoreEndTime = System.currentTimeMillis();
            for (final Map row : rowListScore) {
                final Student student = getStudent(studentMap, KnjDbUtils.getString(row, "SCHREGNO"));
                if (student == null) {
                    continue;
                }

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String classCd = subclasscd == null ? "" : subclasscd.substring(0, 2);
                if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                    final SubClass subclass = getSubclass(row, _subclasses);
                    final ScoreDetail scoreDetail = new ScoreDetail(
                            subclass,
                            student,
                            KnjDbUtils.getString(row, "CHAIRCD"),
                            KnjDbUtils.getString(row, "SCORE"),
                            StringUtils.defaultString(KnjDbUtils.getString(row, "RECORD_SCORE_DAT_SCORE"), "0"),
                            KnjDbUtils.getString(row, "VALUE_DI"),
                            KnjDbUtils.getString(row, "SUPP_SCORE"),
                            KnjDbUtils.getString(row, "ASSESS_LEVEL"),
                            KnjDbUtils.getString(row, "KARI_HYOUTEI"),
                            KnjDbUtils.getString(row, "PASS_SCORE"),
                            KnjDbUtils.getString(row, "PROV_FLG"),
                            KnjDbUtils.getInt(row, "REPLACEMOTO", zero),
                            KnjDbUtils.getString(row, "PRINT_FLG"),
                            KnjDbUtils.getString(row, "SLUMP"),
                            KnjDbUtils.getString(row, "SLUMP_MARK"),
                            KnjDbUtils.getString(row, "SLUMP_SCORE"),
                            KnjDbUtils.getInt(row, "COMP_CREDIT", zero),
                            KnjDbUtils.getInt(row, "GET_CREDIT", zero),
                            KnjDbUtils.getInt(row, "CREDITS", zero),
                            KnjDbUtils.getString(row, "CLASS_DEVIATION"),
                            KnjDbUtils.getString(row, "GRADE_DEVIATION"),
                            KnjDbUtils.getString(row, "COURSE_DEVIATION"),
                            KnjDbUtils.getString(row, "MAJOR_DEVIATION")
                            );
                    student._scoreDetails.put(scoreDetail._subclass._subclasscd, scoreDetail);
                }
            }
            final long scoreSubclassEnd = System.currentTimeMillis();
            if (_param._isOutputDebug) {
                log.info(" load scoreDetail elapsed time = " + (scoreSubclassEnd - scoreSubclassStart) + "[ms] ( query time = " + (scoreEndTime - scoreStartTime) + "[ms] / count = " + rowListScore.size() + ")");
            }

            final String psKey = "ATTENDSUBCLASS";
            if (null == _param.getPs(psKey)) {
                final Map attendParamMap;
                if (_param._schChrDatCount == 0) {
                    // 処理が重くないので年組指定
                    attendParamMap = new HashMap(_param._attendParamMap); // 念のためコピーする
                    attendParamMap.put("hrClass", "?");
                } else {
                    attendParamMap = _param._attendParamMap;
                    attendParamMap.put("schregno", "?");
                }

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        _param._year,
                        _param._attendSemes,
                        _param._attendSdate,
                        _param._attendEdate,
                        attendParamMap
                        );
                //log.debug(" attend subclass sql = " + sql);
                try {
                    _param._psMap.put(psKey, db2.prepareStatement(sql));
                } catch (Exception e) {
                    log.error("exception!", e);
                    return;
                }
            }

            log.info("load attendSubclass");
            long attendSubclassStart = System.currentTimeMillis();
            long attendSubclassAcc = 0;

            if (_param._schChrDatCount == 0) {
                final Set<String> hrClasses = new HashSet<String>();
                for (final Student student : _students) {
                    if (null == student._hrClass) {
                        continue;
                    }
                    hrClasses.add(student._hrClass);
                }

                for (final String hrClass : hrClasses) {
                    long attendSubclassAccStart = System.currentTimeMillis();
                    final List<Map<String, String>> rowList = KnjDbUtils.query(db2, _param.getPs(psKey), new Object[] {hrClass});
                    attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                    for (final Map<String, String> row : rowList) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student) {
                            continue;
                        }
                        setSubclassAttendInfo(row, student);
                    }
                }
                long attendSubclassEnd = System.currentTimeMillis();
                log.info("load attendSubclass (hr) elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");
            } else {
                for (final Student student : _students) {
                    long attendSubclassAccStart = System.currentTimeMillis();
                    final List<Map<String, String>> rowList = KnjDbUtils.query(db2, _param.getPs(psKey), new Object[] {student._schregno});
                    attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                    for (final Map<String, String> row : rowList) {
                        setSubclassAttendInfo(row, student);
                    }
                }
                long attendSubclassEnd = System.currentTimeMillis();
                log.info("load attendSubclass elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");
            }

        }

        private void setSubclassAttendInfo(final Map<String, String> row, final Student student) {
            final Integer zero = new Integer(0);
            final String semester = KnjDbUtils.getString(row, "SEMESTER");

            if (!SEMEALL.equals(semester)) {
                return;
            }

            ScoreDetail scoreDetail = student._scoreDetails.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
            if (null == scoreDetail) {
                final SubClass subClass = _subclasses.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
                if (null != subClass) {
                    scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                    student._scoreDetails.put(KnjDbUtils.getString(row, "SUBCLASSCD"), scoreDetail);
                }
                if (null == scoreDetail) {
                    // log.fatal(" no detail " + student._schregno + ", " + KnjDbUtils.getString(row, "SUBCLASSCD"));
                    return;
                }
            }

            final String specialGroupCd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
            final Integer specialAbsentMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES3", null);
            if (specialGroupCd != null && specialAbsentMinutes != null) {
                if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
                    student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
                }
                int minute = student._spGroupAbsentMinutes.get(specialGroupCd).intValue();
                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
            }


            final String jisuField;
            if ("1".equals(_param._knjd626kJugyoJisuLesson)) {
                // 授業時数
                jisuField = "LESSON";
            } else {
                // 出席すべき時数
                jisuField = "MLESSON";
            }
            final Integer integerJisu = KnjDbUtils.getInt(row, jisuField, zero) - KnjDbUtils.getInt(row, "ABSENT", zero) - KnjDbUtils.getInt(row, "MOURNING", zero);
            if (0 != integerJisu.intValue()) {
                scoreDetail._subclass._jisuSet.add(integerJisu);
            }
            scoreDetail._jisu = KnjDbUtils.getInt(row, "LESSON", zero) - KnjDbUtils.getInt(row, "ABSENT", zero) - KnjDbUtils.getInt(row, "MOURNING", zero);
            scoreDetail._absenceHigh = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH", null);
            if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                scoreDetail._absent = Double.valueOf(KnjDbUtils.getString(row, "REPLACED_SICK"));
            } else {
                scoreDetail._absent = Double.valueOf(KnjDbUtils.getString(row, "SICK2"));
            }
            scoreDetail._isKekkaOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);

        }

        private boolean isNarakenKekkaOver(final Student student, final ScoreDetail scoreDetail) {
            if (null == scoreDetail._absent) {
                return false;
            }
            int credits = 0;
            int week = 0;
            int jisu = 0;
            _param.logOnce(" 上限値算出 分子 = " + _param._knjSchoolMst._syutokuBunsi + ", 分母 = " + _param._knjSchoolMst._syutokuBunbo);
            if (_param._knjSchoolMst.isHoutei()) {
                if (null == scoreDetail._credits) {
                    log.warn("単位無し：" + student._schregno + ", " + scoreDetail._subclass._subclasscd + ", kekka = " + scoreDetail._absent);
                } else {
                    final String sWeek;
                    if (SEMEALL.equals(_param._semester) || _param._isLastSemester) {
                        sWeek = KnjDbUtils.getString(_param._vSchoolMst, "JITU_SYUSU");
                    } else {
                        sWeek = KnjDbUtils.getString(_param._vSchoolMst, "HOUTEI_SYUSU_SEMESTER" + _param._semester);
                    }
                    if (!NumberUtils.isDigits(sWeek)) {
                        log.warn("週数無し： semester = " + _param._semester);
                    } else {
                        credits = scoreDetail._credits.intValue();
                        week = Integer.parseInt(sWeek);
                        jisu = credits * week;
                        _param.logOnce(" subclass " + scoreDetail._subclass._subclasscd + ", 単位数 " + credits + ", 週数 " + week + ", 時数 " + jisu);
                    }
                }
            } else { // _param._knjSchoolMst.isJitsu()
                jisu = scoreDetail._jisu;
                _param.logOnce(" subclass " + scoreDetail._subclass._subclasscd + ", 時数 " + jisu);
            }
            boolean rtn = false;
            if (jisu != 0) {
                int jougenti = 0;
                final boolean isFutoukouTaiou = (SEMEALL.equals(_param._semester) || _param._isLastSemester) && null != student._schoolRefusal;
                if (isFutoukouTaiou) {
                    jougenti = jisu / 2; // 端数切捨て
                } else {
                    //jougenti = jisu / 3; // 端数切捨て
                    if (NumberUtils.isNumber(_param._knjSchoolMst._syutokuBunsi) && NumberUtils.isNumber(_param._knjSchoolMst._syutokuBunbo) && new BigDecimal(_param._knjSchoolMst._syutokuBunbo).doubleValue() != 0.0) {
                        jougenti = new BigDecimal(jisu).multiply(new BigDecimal(_param._knjSchoolMst._syutokuBunsi)).divide(new BigDecimal(_param._knjSchoolMst._syutokuBunbo), 0, BigDecimal.ROUND_DOWN).intValue(); // 端数切捨て
                    } else {
                        _param.logOnce(" 上限値算出エラー 分子 = " + _param._knjSchoolMst._syutokuBunsi + ", 分母 = " + _param._knjSchoolMst._syutokuBunbo);
                    }
                }
                if (jougenti > 0) {
                    rtn = jougenti < scoreDetail._absent.doubleValue();
                    if (rtn) {
                        if (_param._isOutputDebug) {
                            log.info(" student " + student._attendno +  ":" + student._schregno + ", subclass " + scoreDetail._subclass._subclasscd + ", 時数 " + jisu + ", 上限 " + jougenti + ", 欠席 " + scoreDetail._absent + (isFutoukouTaiou ? " (不登校傾向対応)" : ""));
                        }
                    }
                }
            }
            return rtn;
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail() {
            final StringBuffer stb = new StringBuffer();
            final String testkindcd = _param._testKindCd.substring(0, 2);
            final String testitemcd = _param._testKindCd.substring(2, 4);
            final String scoreDiv = _param._testKindCd.substring(4);
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = ? AND W1.HR_CLASS = ? ");

            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("            W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR ");
            stb.append("         AND SEME.SEMESTER = W1.SEMESTER ");
            if ("1".equals(_param._printSubclassLastChairStd)) {
                stb.append("         AND SEME.EDATE = W1.APPENDDATE ");
            }
            stb.append("     INNER JOIN SCHNO_A W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("     WHERE  W1.YEAR = '" + _param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     )");

            stb.append(",T_CHAIR_STF AS(");
            stb.append("     SELECT W1.CHAIRCD, W1.SEMESTER, MIN(W3.STAFFCD) AS STAFFCD ");
            stb.append("     FROM   CHAIR_A W1 ");
            stb.append("     INNER JOIN CHAIR_STF_DAT W3 ON W3.YEAR = '" + _param._year + "' ");
            stb.append("         AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W3.CHAIRCD = W1.CHAIRCD ");
            stb.append("         AND W3.CHARGEDIV = 1 ");
            stb.append("     GROUP BY W1.CHAIRCD, W1.SEMESTER ");
            stb.append("     )");

            stb.append(",CREDITS_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, ");
            stb.append("            CRED.CLASSCD, CRED.SCHOOL_KIND, CRED.CURRICULUM_CD, CRED.SUBCLASSCD, CRED.CREDITS ");
            stb.append("    FROM    SCHNO_A T1 ");
            stb.append("    INNER JOIN (SELECT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD_ONLY ");
            stb.append("                FROM CHAIR_A ");
            stb.append("                GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD_ONLY ");
            stb.append("               ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    INNER JOIN CREDIT_MST CRED ON ");
            stb.append("            CRED.YEAR = '" + _param._year + "' ");
            stb.append("        AND CRED.GRADE = T1.GRADE ");
            stb.append("        AND CRED.COURSECD = T1.COURSECD ");
            stb.append("        AND CRED.MAJORCD = T1.MAJORCD ");
            stb.append("        AND CRED.COURSECODE = T1.COURSECODE ");
            stb.append("        AND CRED.CLASSCD = T2.CLASSCD ");
            stb.append("        AND CRED.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("        AND CRED.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("        AND CRED.SUBCLASSCD = T2.SUBCLASSCD_ONLY ");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("       , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            stb.append("       , T1.CREDITS");
            stb.append("    FROM CREDITS_A T1");
            stb.append("    WHERE (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) ");
            stb.append("          NOT IN (SELECT T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD ");
            stb.append("                  FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                  WHERE  T2.YEAR = '" + _param._year + "' ");
            stb.append("                     AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                  ) ");
            stb.append("    UNION ");
            stb.append("    SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("       , T2.COMBINED_CLASSCD AS CLASSCD, T2.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append("       , SUM(T1.CREDITS) AS CREDITS ");
            stb.append("    FROM CREDITS_A T1 ");
            stb.append("    INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON ");
            stb.append("            T2.YEAR = '" + _param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("        AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    GROUP BY T1.SCHREGNO, ");
            stb.append("            T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD ");
            stb.append(") ");
            stb.append(" , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("   FROM RELATIVEASSESS_MST ");
            stb.append("   WHERE GRADE = '" + _param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append(" ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD ");
            stb.append("     , W3.SCORE ");
            stb.append("     , W3.CLASS_DEVIATION ");
            stb.append("     , W3.GRADE_DEVIATION ");
            stb.append("     , W3.COURSE_DEVIATION ");
            stb.append("     , W3.MAJOR_DEVIATION ");
            stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + _param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("             AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("             AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("             AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("          ) ELSE ");
            if ("1".equals(_param._useAssessCourseMst)) {
                stb.append("          VALUE((SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_COURSE_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND L3.COURSECD = SCH.COURSECD  ");
                stb.append("             AND L3.MAJORCD = SCH.MAJORCD  ");
                stb.append("             AND L3.COURSECODE = SCH.COURSECODE  ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("           ),  ");
                stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          )) ");
            } else {
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          ) ");
            }
            stb.append("         END AS ASSESS_LEVEL ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A SCH ON W3.SCHREGNO = SCH.SCHREGNO ");
            stb.append("       AND SCH.LEAVE = 0 ");
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("            AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    WHERE   W3.YEAR = '" + _param._year + "' ");
            stb.append("            AND W3.SEMESTER = '" + _param._semester + "' ");
            stb.append("            AND W3.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("            AND W3.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("            AND W3.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     ) ");

            //仮評定の表
            stb.append(",RECORD_KARI_HYOUTEI AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("           ,W3.CLASSCD ");
            stb.append("           ,W3.SCHOOL_KIND ");
            stb.append("           ,W3.CURRICULUM_CD ");
            stb.append("           ,W3.SUBCLASSCD AS SUBCLASSCD_ONLY ");
            stb.append("           ,W3.SCORE AS KARI_HYOUTEI ");
            stb.append("           ,T2.PROV_FLG ");
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
            stb.append("    WHERE   W3.YEAR = '" + _param._year + "' ");
            stb.append("        AND W3.SEMESTER = '" + _param._semester + "' ");
            stb.append("        AND W3.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("        AND W3.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("        AND W3.SCORE_DIV = '" + SCORE_DIV_09 + "' ");
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + _param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("             AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("             AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("             AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("          ) ELSE ");

            if ("1".equals(_param._useAssessCourseMst)) {
                stb.append("          VALUE((SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_COURSE_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND L3.COURSECD = SCH.COURSECD  ");
                stb.append("             AND L3.MAJORCD = SCH.MAJORCD  ");
                stb.append("             AND L3.COURSECODE = SCH.COURSECODE  ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("           ),  ");
                stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          )) ");
            } else {
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          ) ");
            }
            stb.append("         END ");
            stb.append("        END AS SLUMP_SCORE ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("            AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = W3.SCHREGNO ");
            stb.append("       AND SCH.LEAVE = 0 ");
            stb.append("    WHERE   W3.YEAR = '" + _param._year + "' ");
            stb.append("            AND W3.SEMESTER = '" + _param._semester + "' ");
            stb.append("            AND W3.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("            AND W3.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("            AND W3.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     ) ");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(T1.CHAIRCD) AS CHAIRCD, ");
            stb.append("             MIN(T2.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A T1");
            stb.append("     LEFT JOIN T_CHAIR_STF T2 ON T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            if (!SEMEALL.equals(_param._semester)) {
                stb.append(" WHERE   T1.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("     GROUP BY T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY ");
            stb.append(" ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("           COMBINED_CLASSCD AS CLASSCD, COMBINED_SCHOOL_KIND AS SCHOOL_KIND, COMBINED_CURRICULUM_CD AS CURRICULUM_CD, COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("           COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("           ATTEND_CLASSCD AS CLASSCD, ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("           ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT ");
            stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY AS SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T1.CHAIRCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,RSD.SCORE AS RECORD_SCORE_DAT_SCORE ");
            stb.append("        ,T3.CURRICULUM_CD ");
            stb.append("        ,T3.CLASS_DEVIATION ");
            stb.append("        ,T3.GRADE_DEVIATION ");
            stb.append("        ,T3.COURSE_DEVIATION ");
            stb.append("        ,T3.MAJOR_DEVIATION ");
            stb.append("        ,RSD.VALUE_DI ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,SUPP.SCORE AS SUPP_SCORE ");
            stb.append("        ,RSD.COMP_CREDIT ");
            stb.append("        ,RSD.GET_CREDIT ");
            stb.append("        ,KARIHYO.KARI_HYOUTEI ");
            stb.append("        ,KARIHYO.PROV_FLG ");
            stb.append("        ,CREM.CREDITS ");
            if (_param._isPrintPerfect) {
                stb.append("        ,VALUE(PERF.PERFECT, 100) AS PERFECT ");
            } else {
                stb.append("        ,CASE WHEN PERF.DIV IS NULL THEN 100 ELSE PERF.PERFECT END AS PERFECT ");
            }
            stb.append("        ,PERF.PASS_SCORE ");
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

            stb.append("    , STFM.STAFFNAME ");
            stb.append("    , RAD.STDDEV ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND T3.SCHREGNO = T1.SCHREGNO");

            stb.append(" LEFT JOIN RECORD_SCORE_DAT RSD ON RSD.YEAR = '" + _param._year + "' ");
            stb.append("        AND RSD.SEMESTER = '" + _param._semester + "' ");
            stb.append("        AND RSD.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("        AND RSD.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("        AND RSD.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("        AND RSD.CLASSCD = T1.CLASSCD AND RSD.SCHOOL_KIND = T1.SCHOOL_KIND AND RSD.CURRICULUM_CD = T1.CURRICULUM_CD AND RSD.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND RSD.SCHREGNO = T1.SCHREGNO ");

            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI KARIHYO ON KARIHYO.CLASSCD = T1.CLASSCD AND KARIHYO.SCHOOL_KIND = T1.SCHOOL_KIND AND KARIHYO.CURRICULUM_CD = T1.CURRICULUM_CD AND KARIHYO.SUBCLASSCD_ONLY = T1.SUBCLASSCD_ONLY AND KARIHYO.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append(" LEFT JOIN COMBINED_SUBCLASS T9 ON T9.CLASSCD = T1.CLASSCD AND T9.SCHOOL_KIND = T1.SCHOOL_KIND AND T9.CURRICULUM_CD = T1.CURRICULUM_CD AND T9.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            //合併元科目の表
            stb.append(" LEFT JOIN ATTEND_SUBCLASS T10 ON T10.CLASSCD = T1.CLASSCD AND T10.SCHOOL_KIND = T1.SCHOOL_KIND AND T10.CURRICULUM_CD = T1.CURRICULUM_CD AND T10.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");

            stb.append(" LEFT JOIN CREDITS_B CREM ON CREM.CLASSCD = T1.CLASSCD AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD AND CREM.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND CREM.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T1.CLASSCD AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD AND SUBM.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN CLASS_MST CM ON CM.CLASSCD = T1.CLASSCD AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.CLASSCD = T1.CLASSCD AND K1.SCHOOL_KIND = T1.SCHOOL_KIND AND K1.CURRICULUM_CD = T1.CURRICULUM_CD AND K1.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + _param._year + "' AND SDET.CLASSCD = T1.CLASSCD AND SDET.SCHOOL_KIND = T1.SCHOOL_KIND AND SDET.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = '" + _param._year + "' AND PERF.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND PERF.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("     AND PERF.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("     AND PERF.CLASSCD = T1.CLASSCD AND PERF.SCHOOL_KIND = T1.SCHOOL_KIND AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD AND PERF.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append("     AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE SCH.GRADE END ");
            stb.append("     AND (PERF.DIV IN ('01','02')  AND PERF.COURSECD = '0' AND PERF.MAJORCD = '000' AND PERF.COURSECODE = '0000' ");
            stb.append("       OR PERF.DIV NOT IN ('01','02')  AND PERF.COURSECD = SCH.COURSECD AND PERF.MAJORCD = SCH.MAJORCD AND PERF.COURSECODE = SCH.COURSECODE ");
            stb.append("         ) ");
            stb.append(" LEFT JOIN SUPP_EXA_SDIV_DAT SUPP ON SUPP.YEAR = '" + _param._year + "' ");
            stb.append("             AND SUPP.SEMESTER = '" + _param._semester + "' ");
            stb.append("             AND SUPP.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("             AND SUPP.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("             AND SUPP.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("             AND SUPP.CLASSCD = T1.CLASSCD AND SUPP.SCHOOL_KIND = T1.SCHOOL_KIND AND SUPP.CURRICULUM_CD = T1.CURRICULUM_CD AND SUPP.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append("             AND SUPP.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND SUPP.SCORE_FLG = '2' ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAD ");
            stb.append("        ON RAD.YEAR          = '"+ _param._year +"' ");
            stb.append("       AND RAD.SEMESTER      = '"+ _param._semester +"' ");
            stb.append("       AND RAD.TESTKINDCD    = '"+ testkindcd +"' ");
            stb.append("       AND RAD.TESTITEMCD    = '"+ testitemcd +"' ");
            stb.append("       AND RAD.SCORE_DIV     = '"+ scoreDiv +"' ");
            stb.append("       AND RAD.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND RAD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND RAD.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("       AND RAD.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
            stb.append("       AND RAD.AVG_DIV       = '2' ");
            stb.append("       AND RAD.GRADE         = SCH.GRADE ");
            stb.append("       AND RAD.HR_CLASS      = SCH.HR_CLASS ");
            stb.append("       AND RAD.COURSECD || RAD.MAJORCD || RAD.COURSECODE = '00000000' ");
            stb.append(" ORDER BY ");
            if(_param._output1) {
                stb.append("   SUBM.SHOWORDER3, ");
            }
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY, ");
            stb.append("   T1.SCHREGNO ");
            return stb.toString();
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassStaff() {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = ? AND W1.HR_CLASS = ? ");

            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("            W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR ");
            stb.append("         AND SEME.SEMESTER = W1.SEMESTER ");
            if ("1".equals(_param._printSubclassLastChairStd)) {
                stb.append("         AND SEME.EDATE = W1.APPENDDATE ");
            }
            stb.append("     WHERE  W1.YEAR = '" + _param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("        AND EXISTS( ");
            stb.append("            SELECT 'X' ");
            stb.append("            FROM SCHNO_A W3 ");
            stb.append("                 LEFT JOIN SCHREG_REGD_GDAT WG ON WG.YEAR = W3.YEAR AND WG.GRADE = W3.GRADE ");
            stb.append("            WHERE W3.SCHREGNO = W1.SCHREGNO");
            stb.append("              AND WG.SCHOOL_KIND = W2.SCHOOL_KIND ");
            stb.append("        )");
            stb.append("     )");

            stb.append(",CHAIR_STF AS(");
            stb.append("     SELECT W1.CHAIRCD, W1.SEMESTER, MIN(STAFFCD) AS STAFFCD ");
            stb.append("     FROM   CHAIR_A W1 ");
            stb.append("     LEFT JOIN CHAIR_STF_DAT W3 ON W3.YEAR = '" + _param._year + "' ");
            stb.append("         AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W3.CHAIRCD = W1.CHAIRCD ");
            stb.append("         AND W3.CHARGEDIV = 1 ");
            stb.append("     GROUP BY W1.CHAIRCD, W1.SEMESTER ");
            stb.append("     )");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(W22.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W2");
            stb.append("     LEFT JOIN CHAIR_STF W22 ON W22.SEMESTER = W2.SEMESTER ");
            stb.append("         AND W22.CHAIRCD = W2.CHAIRCD ");
            if (!SEMEALL.equals(_param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY ");
            stb.append(" ) ");

            stb.append(" ,CHAIR_A3 AS ( ");
            stb.append("     SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(W33.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W3");
            stb.append("     LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W3.SCHREGNO AND WA2.SUBCLASSCD_ONLY = W3.SUBCLASSCD_ONLY ");
            stb.append("         AND WA2.CLASSCD = W3.CLASSCD AND WA2.SCHOOL_KIND = W3.SCHOOL_KIND AND WA2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("     LEFT JOIN CHAIR_STF W33 ON W33.SEMESTER = W3.SEMESTER ");
            stb.append("         AND W33.CHAIRCD = W3.CHAIRCD ");
            stb.append(" WHERE WA2.STAFFCD IS NOT NULL AND W33.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(_param._semester)) {
                stb.append("    AND W3.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("     GROUP BY W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD_ONLY");
            stb.append(" ) ");
            stb.append(" ,CHAIR_A4 AS ( ");
            stb.append("     SELECT  W4.SCHREGNO, ");
            stb.append("             W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, W4.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(W44.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W4");
            stb.append("       LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W4.SCHREGNO ");
            stb.append("           AND WA2.CLASSCD = W4.CLASSCD AND WA2.SCHOOL_KIND = W4.SCHOOL_KIND AND WA2.CURRICULUM_CD = W4.CURRICULUM_CD AND WA2.SUBCLASSCD_ONLY = W4.SUBCLASSCD_ONLY ");
            stb.append("       LEFT JOIN CHAIR_A3 WA3 ON WA3.SCHREGNO = W4.SCHREGNO ");
            stb.append("           AND WA3.CLASSCD = W4.CLASSCD AND WA3.SCHOOL_KIND = W4.SCHOOL_KIND AND WA3.CURRICULUM_CD = W4.CURRICULUM_CD AND WA3.SUBCLASSCD_ONLY = W4.SUBCLASSCD_ONLY ");
            stb.append("       LEFT JOIN CHAIR_STF W44 ON W44.SEMESTER = W4.SEMESTER ");
            stb.append("           AND W44.CHAIRCD = W4.CHAIRCD ");
            stb.append("     WHERE WA2.STAFFCD IS NOT NULL AND W44.STAFFCD <> WA3.STAFFCD AND W44.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(_param._semester)) {
                stb.append(" AND   W4.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("     GROUP BY W4.SCHREGNO, W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, W4.SUBCLASSCD_ONLY ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY AS SUBCLASSCD ");
            stb.append("    , STFM2.STAFFNAME AS STAFF2 ");
            stb.append("    , STFM3.STAFFNAME AS STAFF3 ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + _param._year + "' AND SDET.CLASSCD = T1.CLASSCD AND SDET.SCHOOL_KIND = T1.SCHOOL_KIND AND SDET.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append(" LEFT JOIN CHAIR_A3 CA3 ON CA3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND CA3.CLASSCD = T1.CLASSCD AND CA3.SCHOOL_KIND = T1.SCHOOL_KIND AND CA3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND CA3.SUBCLASSCD_ONLY = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = CA3.STAFFCD ");
            stb.append(" LEFT JOIN CHAIR_A4 CA4 ON CA4.SCHREGNO = T1.SCHREGNO AND CA4.CLASSCD = T1.CLASSCD AND CA4.SCHOOL_KIND = T1.SCHOOL_KIND AND CA4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND CA4.SUBCLASSCD_ONLY = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN STAFF_MST STFM3 ON STFM3.STAFFCD = CA4.STAFFCD ");
            stb.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY ");

            return stb.toString();
        }

        private String sqlStdSubclassScoreDeviation() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     SDIV.SCHREGNO, ");
            stb.append("     SDIV.CLASSCD ||'-' || SDIV.SCHOOL_KIND || '-'|| SDIV.CURRICULUM_CD || '-'|| SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SDIV.CURRICULUM_CD, ");
            stb.append("     SDIV.SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = SDIV.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = SDIV.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = SDIV.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("         SDIV.YEAR     = '" + _param._year + "' ");
            stb.append("     AND SDIV.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '990009' ");
            stb.append("     AND SCHREGNO IN (SELECT ");
            stb.append("                          SCHREGNO ");
            stb.append("                      FROM ");
            stb.append("                          SCHREG_REGD_DAT ");
            stb.append("                      WHERE ");
            stb.append("                             YEAR     = '" + _param._year + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("                     AND SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("                     AND SEMESTER = '" + _param._semester + "' ");
            }
            stb.append("                         AND GRADE    = ? ");
            stb.append("                         AND HR_CLASS = ? ");
            stb.append("                      ) ");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubclass(final Map row, final Map<String, SubClass> subclasses) {
            final Integer zero = new Integer(0);
            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            //科目クラスのインスタンスを更新して返す
            if (!subclasses.containsKey(subclasscd)) {
                //科目クラスのインスタンスを作成して返す
                final String classabbv = KnjDbUtils.getString(row, "CLASSNAME");
                final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String staffname = KnjDbUtils.getString(row, "STAFFNAME");
                final boolean electdiv = "1".equals(KnjDbUtils.getString(row, "ELECTDIV"));
                final List<String> otherstafflist = new ArrayList<String>();
                final String standardDeviation = KnjDbUtils.getString(row, "STDDEV");
                subclasses.put(subclasscd, new SubClass(subclasscd, classabbv, subclassabbv, subclassname, electdiv, staffname, otherstafflist, standardDeviation));
            }
            final SubClass subclass = subclasses.get(subclasscd);
            if (KnjDbUtils.getString(row, "CREDITS") != null) {
                final int credit = KnjDbUtils.getInt(row, "CREDITS", zero).intValue();
                if (0 < credit) {
                    subclass._creditSet.add(credit);
                }
            }
            if (KnjDbUtils.getString(row, "PERFECT") != null) {
                final int perfect = KnjDbUtils.getInt(row, "PERFECT", zero).intValue();
                if (0 < perfect) {
                    subclass._perfectSet.add(perfect);
                }
            }
            return subclass;
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         */
        public void setSubclassAverage(final List<Student> students) {
            final Map<SubClass, List<ScoreDetail>> subClassScoreDetailList = new HashMap<SubClass, List<ScoreDetail>>();

            for (final Student student : students) {
                for (final ScoreDetail detail : student.getCountScoreDetails()) {
                    final boolean useD001 = _param._d065Name1List.contains(detail._subclass.keySubclasscd());
                    if (useD001) {
                        continue;
                    }
                    if (detail._isNarakenKekkaOver) {
                        continue;
                    }
                    final String scorevalue = detail._score;
                    if (StringUtils.isNumeric(scorevalue) || detail.isFailCount()) {
                        getMappedList(subClassScoreDetailList, detail._subclass).add(detail);
                    }
                }
            }

            for (final SubClass subClass : subClassScoreDetailList.keySet()) {
                final List<ScoreDetail> detailList = subClassScoreDetailList.get(subClass);

                final List<ScoreDetail> failList = new ArrayList<ScoreDetail>();
                final List<Integer> scoreList = new ArrayList<Integer>();
                for (final ScoreDetail detail : detailList) {
                    final String scorevalue = detail._score;
                    if (detail.isFailCount()) {
                        failList.add(detail);
                    }
                    if (StringUtils.isNumeric(scorevalue)) {
                        scoreList.add(Integer.parseInt(scorevalue));
                    }
                }
                if (scoreList.size() > 0) {
                    subClass._scoretotal = sum(scoreList);
                    subClass._scoreCount = String.valueOf(scoreList.size());
                    subClass._scoreHrAverage = avg(scoreList);
                    subClass._scoreMax = max(scoreList);
                    subClass._scoreMin = min(scoreList);
                }
                if (failList.size() > 0) {
                    subClass._scoreFailCnt = String.valueOf(failList.size());
                }
            }
        }

        // 今学期換算点
        private List<Integer> kansanScores() {
            final List<Integer> scores = new ArrayList<Integer>();
            for (final Student student : _students) {
                final String s = student._kansanScore;
                if (NumberUtils.isDigits(s)) {
                    scores.add(Integer.parseInt(s));
                }
            }
            return scores;
        }

        // 通算換算点
        private List<Integer> totalKansanScores() {
            final List<Integer> scores = new ArrayList<Integer>();
            for (final Student student : _students) {
                final String s = student._totalKansanScore;
                if (NumberUtils.isDigits(s)) {
                    scores.add(Integer.parseInt(s));
                }
            }
            return scores;
        }

        // 最高点
        public String scoresMax(final List<Integer> scores) {
            return max(scores);
        }

        // 最低点
        public String scoresMin(final List<Integer> scores) {
            return min(scores);
        }

        /**
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(final DB2UDB db2) {
            final String sql = getRecordAverageSdivDatSql(_param._avgDiv);
            if (null != sql) {
                if (_param._isOutputDebugQuery) {
                    log.info(" gradeAverage sql = " + sql);
                } else {
                    log.debug(" gradeAverage sql = " + sql);
                }
                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final SubClass subclass = _subclasses.get(subclassCd);
                    final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                    if (subclass == null || avg == null) {
                        continue;
                    }
                    if (_param._isOutputDebugQuery) {
                        log.info("subclass " + subclass._subclasscd + ":" + subclass._subclassabbv + " , gradeAvg => " + avg);
                    }
                    subclass._recordAverageSdivDatAvg = sishaGonyu(avg);
                }
            }

            // コース毎改頁なら「クラス平均点」はページの生徒から算出した値ではなくRECORD_AVERAGE_SDIV_DATから表示する
            if (_isPrintHrCoursePage) {
                final String sql2 = getRecordAverageSdivDatSql("2");
                if (null != sql2) {
                    if (_param._isOutputDebugQuery) {
                        log.info(" hrAverage sql = " + sql2);
                    } else {
                        log.debug(" hrAverage sql = " + sql2);
                    }
                    for (final Map row : KnjDbUtils.query(db2, sql2)) {
                        final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final SubClass subclass = _subclasses.get(subclassCd);
                        final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                        if (subclass == null || avg == null) {
                            continue;
                        }
                        if (_param._isOutputDebugQuery) {
                            log.info("subclass " + subclass._subclasscd + ":" + subclass._subclassabbv + " , hrAvg => " + avg + " (calc = " + subclass._scoreHrAverage + ")");
                        }
                        subclass._scoreHrAverage = sishaGonyu(avg);
                    }
                }
            }
        }

        private String getRecordAverageSdivDatSql(final String avgDiv) {
            final String testkindcd = _param._testKindCd.substring(0, 2);
            final String testitemcd = _param._testKindCd.substring(2, 4);
            final String scoreDiv = _param._testKindCd.substring(4);

            final Collection<String> cds = new TreeSet<String>();
            final StringBuffer stb = new StringBuffer();
            // _param._avgDivのRECORD_AVERAGE_SDIV_DAT
            stb.append("SELECT ");
            stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD = '" + testkindcd + "'");
            stb.append("    AND T1.TESTITEMCD = '" + testitemcd + "'");
            stb.append("    AND T1.SCORE_DIV = '" + scoreDiv + "'");
            stb.append("    AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("    AND T1.GRADE = '" + _param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '" + _param.SUBCLASSCD999999 + "' ");
            if ("2".equals(avgDiv)) {
                for (final Student student : _students) {
                    if (null != student._hrClass) {
                        cds.add(student._hrClass);
                    }
                }
                if (cds.size() == 0) {
                    log.warn("warning:PARAMETER(hrclasses) is NULL .");
                    return null;
                }
                stb.append("    AND T1.HR_CLASS IN ('" + mkString(cds, "','")  + "') ");
                stb.append("    ORDER BY HR_CLASS ");

            } else if ("3".equals(avgDiv)) {
                for (final Student student : _students) {
                    if (null != student._course) {
                        cds.add(student._course);
                    }
                }
                if (cds.size() == 0) {
                    log.warn("warning:PARAMETER(courses) is NULL .");
                    return null;
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ('" + mkString(cds, "','") + "') ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            } else if ("4".equals(avgDiv)) {
                for (final Student student : _students) {
                    if (null != student._course && student._course.length() >= 4) {
                        cds.add(student._course.substring(0, 4));
                    }
                }
                if (cds.size() == 0) {
                    log.warn("warning:PARAMETER(majorcds) is NULL .");
                    return null;
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD IN ('" + mkString(cds, "','") + "') ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD ");
            }
            return stb.toString();
        }

        public int compareTo(final StudentGroup that) {
            return _cd.compareTo(that._cd);
        }

        public String toString() {
            return "StudentGroup(" + _cd + ")";
        }
        public void bkupInfo(final List<ReportInfo> bkupReportInfo) {
            String groupFlg = StudentGroup.AVG_FLG_HR;
            final String hrclass = _cd;
            ReportDetailInfo adddetailwk = new ReportDetailInfo(getRecordRankSdivAverage(groupFlg, _param.SUBCLASSCD999999, "AVG"), getRecordRankSdivAverage(groupFlg, _param.SUBCLASSCD999999, "TOTAL"), _subclasses);
            ReportInfo addwk = new ReportInfo(hrclass, _hrName, _courses, adddetailwk);
            bkupReportInfo.add(addwk);
        }
    }

    private static class ReportInfo {
        final String _hrclass;
        final String _hrName;
        final List<Course> _courses;
        final ReportDetailInfo _reportlist;
        ReportInfo(final String hrclass, final String hrName, final List<Course> courses, final ReportDetailInfo reportlist) {
            _hrclass = hrclass;
            _hrName = hrName;
            _courses = courses;
            _reportlist = reportlist;
        }
    }

    private static class ReportDetailInfo{
        final Map<String, SubClass> _subclasses;
        final String _gavg;
        final String _gtotal;
        ReportDetailInfo(final String gavg, final String gtotal, final Map<String, SubClass> subclasses) {
            _gavg = gavg;
            _gtotal = gtotal;
            _subclasses = new TreeMap<String, SubClass>(subclasses);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private class Student implements Comparable<Student> {
        int _gnum;  // 行番号
        final String _schregno;  // 学籍番号
        final String _hrClass;
        final String _course;
        final String _major;
        final StudentGroup _studentGroup;
        final String _sex;
        final String _finschool;
        final String _coursecodename;
        final String _schoolRefusal;
        private String _attendno;
        private String _name;
        private String _nameEng;
        private String _transInfo;
        private AttendInfo _attendInfo = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0);
        private Map<String, String> _subclasscdChairGroupCdMap = new HashMap<String, String>();
        private String _scoreSum999999;
        private String _scoreAvg999999;
        private int _classRank;
        private int _rank;
        private final Map<String, ScoreDetail> _scoreDetails = new TreeMap<String, ScoreDetail>();
        private Map<String, Integer> _spGroupAbsentMinutes = new HashMap<String, Integer>(); // 特活グループコードごとの欠課時分
        private String _attendSemesRemarkDatRemark1;
        int _defaultNum; //不合格
        private String _kansanScore;
        private String _totalKansanScore;

        Student(
                final String schregno,
                final String hrClass,
                final String course,
                final String major,
                final String sex,
                final String finschool,
                final String coursecodename,
                final StudentGroup studentGroup,
                final String schoolRefusal,
                final String kansanScore,
                final String totalKansanScore
        ) {
            _schregno = schregno;
            _hrClass = hrClass;
            _course = course;
            _major = major;
            _sex = sex;
            _finschool = finschool;
            _coursecodename = coursecodename;
            _studentGroup = studentGroup;
            _schoolRefusal = schoolRefusal;
            _defaultNum = 0;
            _kansanScore = kansanScore;
            _totalKansanScore = totalKansanScore;
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Student that) {
            int rtn;
            rtn = _studentGroup.compareTo(that._studentGroup);
            if (0 != rtn) return rtn;
            rtn = _hrClass.compareTo(that._hrClass);
            if (0 != rtn) return rtn;
            rtn = _attendno.compareTo(that._attendno);
            return rtn;
        }

        public String toString() {
            return _attendno + ":" + _name;
        }

        public List<ScoreDetail> getCountScoreDetails() {
            final List<ScoreDetail> rtn = new ArrayList<ScoreDetail>(_scoreDetails.values());
            for (final Iterator<ScoreDetail> it = rtn.iterator(); it.hasNext();) {
                final ScoreDetail detail = it.next();
                if (!_param._isPrintSakiKamoku && null != detail._subclass && _param.getSubclassMst(detail._subclass.keySubclasscd()).isSaki(Collections.singleton(_course), _param._combinedCourseListMap)) {
                    it.remove();
                } else if (_param._isNoPrintMoto && null != detail._subclass && _param.getSubclassMst(detail._subclass.keySubclasscd()).isMoto(Collections.singleton(_course), _param._attendCourseListMap)) {
                    it.remove();
                }
            }
            return rtn;
        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : _attendno;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class AttendInfo {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;

        AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
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

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private int getSpecialAttendExe(final int kekka) {
            final int jituJifun = (_param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(_param._knjSchoolMst._jituJifunSpecial);
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
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private class SubClass implements Comparable<SubClass> {
        final String _classabbv;
        final String _classcd;
        final String _subclasscd;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _electdiv; // 選択科目
        final String _staffname;  // 科目担当者名
        final String _standardDeviation; // 標準偏差
        final TreeSet<Integer> _creditSet = new TreeSet<Integer>();  // 単位
        final TreeSet<Integer> _jisuSet = new TreeSet<Integer>();  // 授業時数
        private String _scoreHrAverage;  // 学級平均
        private String _recordAverageSdivDatAvg;  // 学年orコース平均
        String _scoretotal = "";  // 学級合計
        String _scoreCount = "";  // 学級人数
        String _scoreMax = "";  // 最高点
        String _scoreMin = "";  // 最低点
        String _scoreFailCnt = "";  // 欠点者数
        final TreeSet<Integer> _perfectSet = new TreeSet<Integer>();  // 満点
        final List<String> _otherstafflist; //他の先生(MAX2人)
        final Map<String, Map<String, String>> _chairInfos = new TreeMap<String, Map<String, String>>(); // 講座ごとの設定
        final Map<String, String> _chairGroupAvgs = new TreeMap<String, String>(); // <講座グループコード, 平均点>
        int _defaultNum; //不合格

        SubClass(
                final String subclasscd,
                final String classabbv,
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final String staffname,
                final List<String> otherstafflist,
                final String standardDeviation
        ) {
            _classabbv = classabbv;
            _classcd = subclasscd.substring(0, 2);
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _staffname = staffname;
            _otherstafflist = otherstafflist;
            _standardDeviation = standardDeviation;
            _defaultNum = 0;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscd.equals(that._subclasscd);
        }

        @Override
        public int compareTo(final SubClass subclass) {
            return keySubclasscd().compareTo(subclass.keySubclasscd());
        }

        public int hashCode() {
            return ("Subclass(" + _subclasscd + ")").hashCode() * 17;
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscd + " , " +_subclassabbv + " , " +_electdiv + " , " +_creditSet + " , " +_jisuSet +"]";
        }

        public String keySubclasscd() {
            return _subclasscd.substring(0);
        }

        public String getPrintCredit() {
            final StringBuffer stb = new StringBuffer();
            if (0 < _creditSet.size()) {
                if (_creditSet.size() == 1) {
                    stb.append(_creditSet.last());
                } else {
                    if (_param._isPrintPerfect) {
                        stb.append(String.valueOf(_creditSet.first()) + Param.FROM_TO_MARK + String.valueOf(_creditSet.last()));
                    } else {
                        stb.append(String.valueOf(_creditSet.first()) + " " + Param.FROM_TO_MARK + " " + String.valueOf(_creditSet.last()));
                    }
                }
            }
            if (_param._isPrintPerfect) {
                if (0 < _perfectSet.size()) {
                    if (_perfectSet.size() == 1) {
                        stb.append("(").append(_perfectSet.last()).append(")");
                    } else {
                        stb.append("(*)");
                    }
                }
            }
            return stb.toString();
        }

        public String getJisu() {
            if (_jisuSet.isEmpty()) {
                return "";
            }
            return _jisuSet.last().toString(); // MAX
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private class ScoreDetail {
        final SubClass _subclass;
        final Student _student;
        final String _chaircd;
        Double _absent;
        Integer _jisu;
        final String _score;
        final String _recordScoreDatScore;
        final String _valueDi;
        final String _suppScore;
        final String _assessLevel;
        final String _karihyotei;
        final String _passScore;
        final String _provFlg;
        final Integer _replacemoto;
        final String _print_flg;
        final Integer _compCredit;
        final Integer _getCredit;
        BigDecimal _absenceHigh;
        final Integer _credits;
        boolean _isKekkaOver;
        boolean _isNarakenKekkaOver;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;
        final String _classDeviation;
        final String _gradeDeviation;
        final String _courseDeviation;
        final String _majorDeviation;

        ScoreDetail(
                final SubClass subclass,
                final Student student,
                final String chaircd,
                final String score,
                final String recordScoreDatScore,
                final String valueDi,
                final String suppScore,
                final String assessLevel,
                final String karihyotei,
                final String passScore,
                final String provFlg,
                final Integer replacemoto,
                final String print_flg,
                final String slump,
                final String slumpMark,
                final String slumpScore,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits,
                final String classDeviation,
                final String gradeDeviation,
                final String courseDeviation,
                final String majorDeviation
        ) {
            _subclass = subclass;
            _student = student;
            _chaircd = chaircd;
            _score = score;
            _recordScoreDatScore = recordScoreDatScore;
            _valueDi = valueDi;
            _suppScore = suppScore;
            _assessLevel = assessLevel;
            _karihyotei = karihyotei;
            _passScore = passScore;
            _provFlg = provFlg;
            _replacemoto = replacemoto;
            _print_flg = print_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpMark = slumpMark;
            _classDeviation = classDeviation;
            _gradeDeviation = gradeDeviation;
            _courseDeviation = courseDeviation;
            _majorDeviation = majorDeviation;
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

        public Boolean hoge(final ScoreDetail detail) {
            if (null != _param._testItem._sidouinput) {
                if (SIDOU_INPUT_INF_MARK.equals(_param._testItem._sidouinputinf)) { // 記号
                    if (null != _param._d054Namecd2Max && null != detail._slumpMark) {
                        if (_param._d054Namecd2Max.equals(detail._slumpMark)) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                } else if (SIDOU_INPUT_INF_SCORE.equals(_param._testItem._sidouinputinf)) { // 得点
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

        private boolean is5dankai() {
            return _param._testKindCd != null && _param._testKindCd.endsWith("09") || _param._is5dankai;
        }

        private boolean isFail(final ScoreDetail detail) {
            final boolean useD001 = _param._d065Name1List.contains(detail._subclass.keySubclasscd());
            if (useD001) {
                return false;
            }
            Boolean rtn = null;
            if (null == rtn) {
                if (is5dankai()) {
                    rtn = new Boolean("*".equals(detail._valueDi) || "1".equals(detail._score));
                } else {
                    final boolean setPassScore = NumberUtils.isDigits(detail._passScore);
                    rtn = new Boolean("*".equals(detail._valueDi) || !setPassScore && "1".equals(detail._assessLevel) || setPassScore && NumberUtils.isDigits(detail._score) && Integer.parseInt(detail._score) < Integer.parseInt(detail._passScore));
                }
            }
            return rtn.booleanValue();
        }

        /**
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(final Integer replacemoto) {
            if (_param._isGakunenMatu && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }

        private boolean isFailCount() {
            final boolean useD001 = _param._d065Name1List.contains(_subclass.keySubclasscd());
            if (useD001) {
                return false;
            }
            if (is5dankai()) {
                return ("1".equals(_score));
            }
            final boolean setPassScore = NumberUtils.isDigits(_passScore);
            return (!setPassScore && "1".equals(_assessLevel) || setPassScore && NumberUtils.isDigits(_score) && Integer.parseInt(_score) < Integer.parseInt(_passScore));
        }

        public String toString() {
            return (_subclass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isKekkaOver);
        }
    }

    static class ClassAbbvFieldSet {
        final String _fieldNumer;
        final String _setAbbv;
        public ClassAbbvFieldSet(
                final String fieldNumer,
                final String setAbbv
        ) {
            _fieldNumer = fieldNumer;
            _setAbbv = setAbbv;
        }
    }

    class Form {

        boolean _hasData = false;

        public void print(final DB2UDB db2, final IPdf ipdf, final StudentGroup studentGroup) {
            final Form1 form = new Form1();
            for (final List<Student> studentList : getStudentListList(getPrintStudentAll(studentGroup), _param._formMaxLine)) {
                form.print(db2, ipdf, studentGroup, studentList);
            }
            if (form._hasData) {
                _hasData = true;
            }
        }

        public boolean outputCsv(final DB2UDB db2, final List<List<String>> outputList, final StudentGroup studentGroup) {
            boolean hasData = false;
            final Form1 form = new Form1();
            final List<Student> studentsAll = getPrintStudentAll(studentGroup);
            if (form.outputCsv(db2, outputList, studentGroup, studentsAll)) {
                hasData = true;
            }
            return hasData;
        }

        private List<Student> getPrintStudentAll(final StudentGroup studentGroup) {
            final List<Student> studentsAll = new ArrayList(studentGroup._students);
            return studentsAll;
        }

        private String getKekkaString(final Double absent) {
            return null == absent ? "" : 0.0 == absent.doubleValue() ? "" : _param.getAbsentFmt().format(absent.floatValue());
        }

        private List<String> newLine(final List<List<String>> listList) {
            final List<String> line = line();
            listList.add(line);
            return line;
        }

        private List<String> line() {
            return line(0);
        }

        private List<String> line(final int size) {
            final List<String> line = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                line.add(null);
            }
            return line;
        }

        private int currentColumn(final List<List> lineList) {
            int max = 0;
            for (final List line : lineList) {
                max = Math.max(max, line.size());
            }
            return max;
        }

        private <T> List<T> setSameSize(final List<T> list, final int max) {
            for (int i = list.size(); i < max; i++) {
                list.add(null);
            }
            return list;
        }

        private List<List<Student>> getStudentListList(final List<Student> students, final int count) {
            final List<List<Student>> rtn = new ArrayList<List<Student>>();
            List<Student> current = null;
            int page = 0;
            for (final Student student : students) {
                final int cpage = student._gnum / count + (student._gnum % count != 0 ? 1 : 0);
                if (null == current || page < cpage) {
                    current = new ArrayList<Student>();
                    rtn.add(current);
                }
                current.add(student);
                page = cpage;
            }
            return rtn;
        }

        private List<List<SubClass>> getSubClassListList(final Collection<SubClass> subclasses, final int count) {
            final List<List<SubClass>> rtn = new ArrayList<List<SubClass>>();
            List<SubClass> current = null;
            for (final SubClass subClass : subclasses) {

                if (null == current || current.size() >= count) {
                    current = new ArrayList<SubClass>();
                    rtn.add(current);
                }
                current.add(subClass);
            }
            return rtn;
        }

        /**
         *
         * @param ipdf
         * @param field
         * @param line
         * @param data
         */
        private void svfsetString1(final IPdf ipdf, final String field, final int line, final String pf, final int col, final String data) {
            ipdf.VrsOutn(field, line, data);
        }

        private void printAttendInfo(final IPdf ipdf, final AttendInfo attendInfo, final int line) {
            final String fieldLesson;
            final String fieldMLesson;
            final String fieldSusMour;
            final String fieldSick;
            final String fieldPresent;
            final String fieldLate;
            final String fieldEarly;
            final String fieldAbroad;
            fieldLesson = "LESSON";
            fieldSusMour = "SUSPEND1";
            fieldAbroad = "ABROAD1";
            fieldMLesson = "PRESENT1";
            fieldPresent = "ATTEND1";
            fieldSick = "ABSENCE1";
            fieldLate = "TOTAL_LATE1";
            fieldEarly = "LEAVE1";

            ipdf.VrsOutn(fieldLesson,  line, String.valueOf(attendInfo._lesson));      // 授業日数
            ipdf.VrsOutn(fieldSusMour, line, String.valueOf(attendInfo._suspend + attendInfo._mourning));      // 出席停止
            ipdf.VrsOutn(fieldAbroad,  line, String.valueOf(attendInfo._transDays));        // 留学
            ipdf.VrsOutn(fieldMLesson, line, String.valueOf(attendInfo._mLesson));      // 出席すべき日数
            ipdf.VrsOutn(fieldPresent, line, attendInfo._lesson == 0 ? "0" : String.valueOf(attendInfo._present));      // 出席日数 (0は表示する)
            ipdf.VrsOutn(fieldSick,    line, String.valueOf(attendInfo._sick));       // 欠席日数
            ipdf.VrsOutn(fieldLate,    line, String.valueOf(attendInfo._late));      // 遅刻回数
            ipdf.VrsOutn(fieldEarly,   line, String.valueOf(attendInfo._early));        // 早退回数
        }

        private boolean isAlpPdf(final IPdf ipdf) {
            return "1".equals(ipdf.getParameter("AlpPdf"));
        }

        private void setForm(final IPdf ipdf, final String formname, final int n) {
            _param._currentform = formname;
            log.info(" form = " + _param._currentform);
            ipdf.VrSetForm(_param._currentform, n);

            if (ipdf instanceof SvfPdf) {
                final SvfPdf svfPdf = (SvfPdf) ipdf;
                modifyForm(svfPdf.getVrw32alp(), n);
            }
        }

        private void modifyForm(final Vrw32alp svf, final int n) {

            _param._svfformModifyKeys = getModifyKeys();
            String key = mkString(_param._svfformModifyKeys, "|");
            if (key.length() != 0) {
                key = _param._currentform + key;
            }
            if (!StringUtils.isBlank(key) && !_param._createdFiles.containsKey(key)) {
                final SvfForm svfForm = new SvfForm(new File(svf.getPath(_param._currentform)));
                if (svfForm.readFile()) {
                    modifyForm(_param._svfformModifyKeys, svfForm);
                    try {
                        File newFormFile = svfForm.writeTempFile();
                        if (null != newFormFile && newFormFile.exists()) {
                            _param._createdFiles.put(key, newFormFile);
                        }
                    } catch (Exception e) {
                        log.info("exception!", e);
                    }
                }
            }
            if (_param._createdFiles.containsKey(key)) {
                final File newFormFile = _param._createdFiles.get(key);
                _param._currentform = newFormFile.getName();
                svf.VrSetForm(_param._currentform, n);
            }

            if (null != _param._currentform && null == _param._formFieldInfoMap.get(_param._currentform)) {
                _param._formFieldInfoMap.put(_param._currentform, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
                //debugFormInfo();
            }
        }

        static final String KEY_REMOVE_FIELD_STAFFNAME = "REMOVE_FIELD_STAFFNAME";
        static final String KEY_MIGITSUME_TANNINMEI = "KEY_MIGITSUME_TANNINMEI";
        static final String KEY_REMOVE_TEXT_TANNINMEI = "REMOVE_TEXT_TANNINMEI";
        static final String KEY_REMOVE_TEXT_INN = "REMOVE_TEXT_INN";
        static final String KEY_REMOVE_TEXT_MARU = "REMOVE_TEXT_MARU";
        static final String KEY_ADD_SUBTITLE = "ADD_SUBTITLE";
        static final String KEY_REMOVE_HR_HEIKIN = "REMOVE_HR_HEIKIN";
        static final String KEY_REMOVE_PARAM_RANK = "REMOVE_PARAM_RANK";
        static final String KEY_SCORE_FIELD_7KETA = "SCORE_FIELD_7KETA";
        static final String KEY_SUBJECT1_6KETA = "SUBJECT1_6KETA";
        private List<String> getModifyKeys() {
            List<String> keys = new ArrayList<String>();
            keys.add(KEY_ADD_SUBTITLE);
            keys.add(KEY_REMOVE_HR_HEIKIN);
            return keys;
        }

        private void modifyForm(List<String> keys, final SvfForm svfForm) {
            if (keys.contains(KEY_REMOVE_FIELD_STAFFNAME)) {
                final List<String> fieldnames = new ArrayList<String>(svfForm.getFieldNameMap().keySet());
                Collections.sort(fieldnames);
                for (final String fieldname : fieldnames) {
                    if (0 == fieldname.indexOf("HR_TEACHER")) {
                        log.info(" remove field :" + fieldname);
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        svfForm.removeField(field);
                    }
                }
            }
            if (keys.contains(KEY_REMOVE_TEXT_TANNINMEI)) {
                for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("担任：")) {
                    log.info(" remove koteiMoji :" + koteiMoji);
                    svfForm.removeKoteiMoji(koteiMoji);
                }
            }
            if (keys.contains(KEY_REMOVE_TEXT_MARU)) {
                for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("○")) {
                    log.info(" remove koteiMoji :" + koteiMoji);
                    svfForm.removeKoteiMoji(koteiMoji);
                }
            }
            if (keys.contains(KEY_REMOVE_TEXT_INN)) {
                for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("印")) {
                    log.info(" remove koteiMoji :" + koteiMoji);
                    svfForm.removeKoteiMoji(koteiMoji);
                }
            }
            if (keys.contains(KEY_MIGITSUME_TANNINMEI)) {
                SvfForm.Field HR_TEACHER = null;
                for (final String fieldname : Arrays.asList("HR_TEACHER", "HR_TEACHER2")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    svfForm.removeField(field);
                    if ("HR_TEACHER".equals(fieldname)) {
                        HR_TEACHER = field;
                    }
                }
                if (null != HR_TEACHER) {
                    svfForm.addField(HR_TEACHER.setX(5226).setFieldLength(45).setHenshuShiki("\"\"担任:\"\" + HR_TEACHER").setPrintMethod(SvfForm.Field.PrintMethod.MIGITSUME));
                }
            }
            if (keys.contains(KEY_ADD_SUBTITLE)) {
                final boolean hasSubtitle = svfForm.getFieldNameMap().containsKey("SUBTITLE");
                if (!hasSubtitle) {
                    final SvfForm.Field title = svfForm.getField("TITLE");
                    if (null == title) {
                        log.warn(" no field TITLE");
                    } else {
                        svfForm.addField(title.copyTo("SUBTITLE").setY(title._position._y + 120).setHenshuShiki(""));
                    }
                }
            }
            if (keys.contains(KEY_REMOVE_HR_HEIKIN)) {
                for (final String text : Arrays.asList("学級平均", "組平均", "学級順位")) {
                    for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(text)) {
                        log.info(" remove koteiMoji :" + koteiMoji);
                        svfForm.removeKoteiMoji(koteiMoji);
                    }
                }
                for (final String fieldname : Arrays.asList("AVE_CLASS", "CLASS_RANK1")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                    }
                }
            }
            if (keys.contains(KEY_REMOVE_PARAM_RANK)) {
                for (final String fieldname : Arrays.asList("ITEM", "ITEM8", "AVE_SUBCLASS", "ITEM7", "RANK1")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                    }
                }
            }
            if (keys.contains(KEY_SCORE_FIELD_7KETA)) {
                for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                    if (field._fieldname.matches("SCORE[0-9]+_2")) {
                        svfForm.removeField(field);
                        svfForm.addField(field.setFieldLength(7).setCharPoint10(field._charPoint10 - 10).addY(5));
                    }
                }
            }
            if (keys.contains(KEY_SUBJECT1_6KETA)) {
                final SvfForm.Field field = svfForm.getField("SUBJECT1");
                if (null != field) {
                    svfForm.removeField(field);
                    svfForm.addField(field.addX(-50).setFieldLength(8).setEndX(field._endX + 50));
                }
            }
        }

        public int getFieldKeta(final IPdf ipdf, final String field) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return -1;
            }
            final Map<String, SvfField> fieldMap = getMappedHashMap(_param._formFieldInfoMap, _param._currentform);
            int keta = -1;
            try {
                final SvfField f = (SvfField) fieldMap.get(field);
                if (null != f) {
                    keta = f._fieldLength;
                }
            } catch (Throwable t) {
                log.info("not found SvfField.class");
            }
            final String logVal = " form " + _param._currentform + " " + field + " keta = " + keta;
            if (_param._isOutputDebug && !_param._formFieldInfoLog.contains(logVal)) {
                log.warn(logVal);
                _param._formFieldInfoLog.add(logVal);
            }
            return keta;
        }

        public SvfField getSvfField(final IPdf ipdf, final String field) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return null;
            }
            final Map<String, SvfField> fieldMap = getMappedHashMap(_param._formFieldInfoMap, _param._currentform);
            final SvfField f = fieldMap.get(field);
            return f;
        }

        protected String getFieldForData(final IPdf ipdf, final String[] fields, final String data) {
            final int keta = KNJ_EditEdit.getMS932ByteLength(data);
            String fieldFound = null;
            int lastFieldLength = -1;
            searchField:
            for (int i = 0; i < fields.length; i++) {
                final String fieldname = fields[i];
                final SvfField svfField = getSvfField(ipdf, fieldname);
                if (null == svfField) {
                    _param.logOnce("no field : " + fieldname);
                    continue searchField;
                }
                fieldFound = fieldname;
                lastFieldLength = svfField._fieldLength;
                if (keta <= svfField._fieldLength) {
                    return fieldname;
                }
            }
            if (-1 != lastFieldLength && lastFieldLength < keta) {
                log.info(" 桁不足 : " + fieldFound + " ( " + lastFieldLength + ") , data = " + data + "(" + keta + ")");
            }
            return fieldFound;
        }

        private Tuple<String, Tuple<String, List<String>>> getPrintScoreAndMarkAndAttribute(final ScoreDetail detail) {
            String printScore;
            List<String> attributes = new ArrayList<String>();
            final boolean useD001 = _param._d065Name1List.contains(detail._subclass.keySubclasscd());
            if (null == detail._score) {
                if ("*".equals(detail._valueDi)) {
                    if (StringUtils.isBlank(detail._suppScore)) {
                        printScore = "(欠)"; // 欠席
                        attributes.add(ATTRIBUTE_CENTERING);
                    } else {
                        printScore = "(" + detail._suppScore + ")"; // 欠席見込点
                    }
                } else if ("**".equals(detail._valueDi)) {
                    if (StringUtils.isBlank(detail._suppScore)) {
                        printScore = "(公欠)"; // 公欠
                        attributes.add(ATTRIBUTE_CENTERING);
                    } else {
                        printScore = "(" + detail._suppScore + ")"; // 公欠見込点
                    }
                } else {
                    printScore = detail._valueDi;
                }

            } else {
                if (useD001) {
                    printScore = _param._d001Name1Map.get(detail._score);
                } else {
                    printScore = detail._score;
                }
            }
            if (detail._isNarakenKekkaOver) {
                if (null == printScore) {
                    printScore = detail._recordScoreDatScore;
                }
                printScore = "(" + (StringUtils.isEmpty(printScore) ? "  " : printScore) + ")";
            }

            String mark = null;
            if (null == mark) {
                if (!useD001 && "990009".equals(_param._testKindCd) && "1".equals(detail._score)) {
                    mark = "*";
                }
            }

            return Tuple.of(printScore, Tuple.of(defstr(mark, ""), attributes));
        }

        class Form1 {
            boolean _hasData;
            private AbstractHashedMap subclassList;

            Form1() {
            }

            private void print(final DB2UDB db2, final IPdf ipdf, final StudentGroup studentGroup, final List<Student> stulist) {
                setForm(ipdf, _param._formname, 4);

                final List<SubClass> printSubclassList = getPrintSubclassList(studentGroup, stulist);

                final List<List<SubClass>> subclassListList;
                subclassListList = getSubClassListList(printSubclassList, _param._formMaxColumn);

                for (int pi = 0, pages = subclassListList.size(); pi < pages; pi++) {
                    final List<SubClass> subclassList = subclassListList.get(pi);
                    final boolean isLastPage = pi == pages - 1;

                    log.info(" subclassList page = " + String.valueOf(pi + 1) + " / " + subclassListList.size());

                    final int maxCol;
                    setForm(ipdf, _param._formname, 4);
                    maxCol = _param._formMaxColumn;
                    ipdf.addRecordField(_param._recordField);
                    printHeader(ipdf, studentGroup);
                    if (isLastPage) {
                        printGroupInfo(db2, ipdf, studentGroup);
                    }

                    for (int sti = 0; sti < stulist.size(); sti++) {
                        final Student student = stulist.get(sti);
                        final int line = gnumToLine(student._gnum, _param._formMaxLine);
                        printStudentName(ipdf, line, student);

                        if (isLastPage) {
                            printStudentTotal(db2, ipdf, line, student, subclassList);
                        }
                        printRemark(ipdf, line, student);
                    }
                    final List<String> studentCourses = getStudentCourses(stulist);
                    //教科毎の科目数をカウント
                    final Map<String, String> classSubclassMap = new LinkedHashMap<String, String>();
                    int subClassCnt = 1;
                    String befClassCd = "";
                    for (final SubClass subclass : subclassList) {

                        if (!"".equals(befClassCd) && !befClassCd.equals(subclass._classcd)) {
                            subClassCnt = 1;
                        }

                        classSubclassMap.put(subclass._classcd, String.valueOf(subClassCnt));
                        subClassCnt++;
                        befClassCd = subclass._classcd;
                    }
                    befClassCd = "";
                    int subClassOrder = 1;
                    int totalJisu = 0;
                    int totalCredit = 0;
                    for (int subi = 0, size = subclassList.size(); subi < size; subi++) {

                        final SubClass subclass = subclassList.get(subi);
                        if (_param._isOutputDebug) {
                            log.info("p=" + pi + ", i=" + subi + ", subclasscd=" + subclass._subclasscd + " " + subclass._subclassabbv);
                        }
                        final int abbvLen = getMS932ByteLength(subclass._classabbv);
                        final int abbvStrCnt = defstr(subclass._classabbv).length();
                        if (!"".equals(befClassCd) && !befClassCd.equals(subclass._classcd)) {
                            subClassOrder = 1;
                        }
                        final ClassAbbvFieldSet abbvFieldSet = setClassAbbv(Integer.parseInt(classSubclassMap.get(subclass._classcd)), subClassOrder, subclass._classabbv, abbvLen, abbvStrCnt);
                        printSubclassHeader(ipdf, subi + 1, subclass, false, abbvFieldSet, studentCourses);
                        if (NumberUtils.isDigits(subclass.getJisu())) totalJisu += Integer.parseInt(subclass.getJisu());
                        if (0 < subclass._creditSet.size()) {
                            totalCredit += subclass._creditSet.last(); // MAX
                        }

                        for (final Student student : stulist) {
                            final int line = gnumToLine(student._gnum, _param._formMaxLine);
                            final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);
                            if (null != detail) {
                                printScoreKekka(ipdf, student._schregno, detail, line, subi + 1, subclass, student);
                            }
                        }

                        //学級平均・合計
                        printSubclassStat(ipdf, subi + 1, subclass);

                        ipdf.VrEndRecord();
                        subClassOrder++;
                        befClassCd = subclass._classcd;
                        _hasData = true;
                    }
                    ipdf.VrsOut("TOTAL_CREDIT", String.valueOf(totalCredit));
                    final String present = defstr(getShukkoTeisu(db2, _param._year, _param._semester, _param._grade, "0", "000"));
                    ipdf.VrsOut("PRESENT1", present);
                    ipdf.VrsOut("SCHOOL_TIME", String.valueOf(totalJisu));

                    for (int i = subclassList.size(); i < maxCol; i++) {
                        //教科名
                        setRecordString(ipdf, "credit1", i + 1, "DUMMY");
                        ipdf.VrAttribute("credit1", "X=10000");
                        ipdf.VrEndRecord();
                        _hasData = true;
                    }
                }
            }

            private int gnumToLine(final int gnum, final int maxLine) {
                return 0 != gnum && gnum % maxLine == 0 ? maxLine : gnum % maxLine;
            }

            private boolean checkForm(final IPdf ipdf, final String formname) {
                if (null != formname && ipdf instanceof SvfPdf) {
                    final SvfPdf svfPdf = (SvfPdf) ipdf;
                    final String path = svfPdf.getVrw32alp().getPath(formname);
                    boolean exists = false;
                    if (null != path) {
                        exists = new File(path).exists();
                    }
                    if (!exists) {
                        log.warn("no form file : " + path);
                    }
                    return exists;
                }
                return false;
            }

            private List<SubClass> getPrintSubclassList(final StudentGroup studentGroup, final List<Student> stulist) {
                final List<SubClass> printSubclassList = new ArrayList<SubClass>(studentGroup._subclasses.values());
                final List<String> courses = getStudentCourses(stulist);
                for (final Iterator<SubClass> it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = it.next();
                    if (!_param._isPrintSakiKamoku && _param.getSubclassMst(subclass.keySubclasscd()).isSaki(courses, _param._combinedCourseListMap)) {
                        if (_param._isOutputDebug) {
                            log.info(" skip saki " + subclass.keySubclasscd());
                        }
                        it.remove();
                        continue;
                    }
                    if (_param._isNoPrintMoto && _param.getSubclassMst(subclass.keySubclasscd()).isMoto(courses, _param._attendCourseListMap)) {
                        if (_param._isOutputDebug) {
                            log.info(" skip moto " + subclass.keySubclasscd());
                        }
                        it.remove();
                        continue;
                    }
                    if (_param._d079Name1List.contains(subclass.keySubclasscd())) {
                        if (_param._isOutputDebug) {
                            log.info(" skip d079 " + subclass.keySubclasscd());
                        }
                        it.remove();
                        continue;
                    }
                }
                return printSubclassList;
            }

            private List<String> getStudentCourses(final List<Student> studentList) {
                final List<String> courses = new ArrayList<String>();
                for (final Student student : studentList) {
                    if (null != student._course) {
                        courses.add(student._course);
                    }
                }
                return courses;
            }

            public boolean outputCsv(final DB2UDB db2, final List<List<String>> outputList, final StudentGroup studentGroup, final List<Student> stulist) {

                boolean hasData = false;

                final List<SubClass> printSubclassList = getPrintSubclassList(studentGroup, stulist);

                _param._formMaxLine = printSubclassList.size();
                _param._formMaxColumn = stulist.size();

                final List<List<String>> headerLineList = new ArrayList<List<String>>();
                final List<String> header1Line = newLine(headerLineList);
                header1Line.addAll(Arrays.asList("", _param._title));
                final String staffname = "担任：" + defstr(studentGroup._staffname);

                final List<String> header2Line = newLine(headerLineList);
                header2Line.addAll(Arrays.asList("HR　"+studentGroup.getName(), "", "出欠集計範囲：" + _param._attendTerm, "", "", "", "", "", staffname));

                final List<List<String>> blockStudentName = new ArrayList<List<String>>();
                List<String> nameLine0 = newLine(blockStudentName);
                List<String> nameLine1 = newLine(blockStudentName);
                List<String> nameLineSex = newLine(blockStudentName);
                List<String> nameLineFinschool = newLine(blockStudentName);

                //見出し
                nameLine0.add("教科");
                nameLine0.add("科目");
                nameLine0.add("単位数");
                nameLine0.add(_param._item1Name + "・" + _param._item2Name);
                final int headerSize = nameLine0.size();
                setSameSize(nameLine1, headerSize);
                setSameSize(nameLineSex, headerSize);
                setSameSize(nameLineFinschool, headerSize);

                nameLine0.add("No.");
                nameLine1.add("氏名");
                nameLineSex.add("性別");
                nameLineFinschool.add("出身");

                //生徒情報
                for (final Student student : stulist) {
                    nameLine0.add(student.getPrintAttendno()); //番号
                    nameLine1.add(student._name); //氏名
                    nameLineSex.add(student._sex); //性別
                    nameLineFinschool.add(student._finschool); //出身
                }

                //合計欄
                nameLine0.add("クラス平均");
                nameLine0.add("不合格者数");
                nameLine0.add("最高点");
                nameLine0.add("最低点");

                final List<List<String>> blockSubclassList = new ArrayList<List<String>>();

                String classabbvbefore = null;

                //明細
                for (int coli = 0, size = printSubclassList.size(); coli < size; coli++) {

                    final List<String> line1 = newLine(blockSubclassList);
                    List<String> line2 = null;

                    final SubClass subclass = printSubclassList.get(coli);

                    final boolean diff = !(null == subclass._classabbv && null == classabbvbefore || null != subclass._classabbv && subclass._classabbv.equals(classabbvbefore));
                    line1.add(diff ? subclass._classabbv : ""); //教科
                    line1.add(subclass._subclassname); //科目
                    line1.add(subclass.getPrintCredit()); //単位数
                    line1.add("");
                    line2 = setSameSize(newLine(blockSubclassList), line1.size());

                    line1.add(_param._item1Name); //評価
                    line2.add(_param._item2Name); //欠課

                    //評価・欠課
                    for (final Student student : stulist) {
                        List<String> scoreLine = null;
                        List<String> absenceLine = null;
                        scoreLine = line1; //評価
                        absenceLine = line2; //欠課
                        if (student._scoreDetails.containsKey(subclass._subclasscd)) {
                            final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);

                            if (null != scoreLine) {
                                final Tuple<String, Tuple<String, List<String>>> printScoreAndMarkAndAttribute = getPrintScoreAndMarkAndAttribute(detail);
                                final String printScore = printScoreAndMarkAndAttribute._first;
                                final String mark = printScoreAndMarkAndAttribute._second._first;
                                if(Integer.parseInt(detail._recordScoreDatScore) < _param._defaultBorder) {
                                    student._defaultNum += 1;
                                    subclass._defaultNum += 1;
                                }

                                scoreLine.add(mark + defstr(printScore));
                            }

                            final boolean isOutputJisu = _param._output2 && null != detail._jisu && !subclass.getJisu().equals(detail._jisu.toString());
                            if (null != absenceLine) {
                                final String val;
                                if (isOutputJisu) {
                                    val = (null == detail._absent ? "0" : String.valueOf(detail._absent.intValue())) + "/" + detail._jisu.toString();
                                } else {
                                    val = getKekkaString(detail._absent);
                                }
                                absenceLine.add(val);
                            }
                        } else {
                            if (null != scoreLine) {
                                scoreLine.add("");
                            }
                            if (null != absenceLine) {
                                absenceLine.add("");
                            }
                        }
                    }


                    //明細 合計欄
                    final boolean useD001 = _param._d065Name1List.contains(subclass.keySubclasscd());
                    if (!useD001) {
                        line1.add(subclass._scoreHrAverage); //クラス平均
                        line1.add(String.valueOf(subclass._defaultNum)); //不合格者数
                        line1.add(subclass._scoreMax); //最高点
                        line1.add(subclass._scoreMin); //最低点
                    }

                    hasData = true;
                    classabbvbefore = subclass._classabbv;
                }

                //末尾の設定
                final int scoreColumns = 4; //平均点～通算換算点
                final int attendColumns = 8; //授業日数～早退回数
                final int remarkColumns = 1; //備考
                final int totalColumns = scoreColumns + attendColumns + remarkColumns;
                final List[] columnsStudentTotalHeader = new List[totalColumns];
                for (int i = 0; i < columnsStudentTotalHeader.length; i++) {
                    columnsStudentTotalHeader[i] = new ArrayList();
                }

                //末尾ヘッダ（平均点～備考）
                final String[] totalHeader1 = {_param._item4Name, _param._item5Name, _param._item7Name, _param._item9Name, "出欠の記録", "", "", "", "", "", "", "", "備考"};
                final String[] totalHeader2 = {"", "", "", "", "授業日数", "出停忌引日数", "留学中授業日数", "要出席日数", "欠席日数", "出席日数", "遅刻回数", "早退回数", ""};
                for (int i = 0; i < totalHeader1.length; i++) {
                    columnsStudentTotalHeader[i].add(totalHeader1[i]);
                }
                for (int i = 0; i < totalHeader2.length; i++) {
                    columnsStudentTotalHeader[i].add(totalHeader2[i]);
                }

                //末尾明細（平均点～備考）
                final List[] columnsStudentTotal = new List[totalColumns];
                for (int i = 0; i < columnsStudentTotal.length; i++) {
                    columnsStudentTotal[i] = new ArrayList();
                    for (int k = 0; k < headerSize-1; k++) {
                        columnsStudentTotal[i].add(null);
                    }
                }
                for (final Student student : stulist) {
                    int i = 0;
                    final AttendInfo attendInfo = student._attendInfo;

                    columnsStudentTotal[i++].add(student._scoreAvg999999); //平均点
                    columnsStudentTotal[i++].add(String.valueOf(student._defaultNum)); //不合格科目数
                    columnsStudentTotal[i++].add(student._kansanScore); //今学期換算点
                    columnsStudentTotal[i++].add(student._totalKansanScore); //通算換算点
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._lesson));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._suspend + attendInfo._mourning));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._transDays));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._mLesson));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._sick));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._present));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._late));
                    columnsStudentTotal[i++].add(String.valueOf(attendInfo._early));
                    columnsStudentTotal[i++].add(student._attendSemesRemarkDatRemark1); //備考
                }


                //明細 合計欄 平均点～通算換算点
                final List columnsStudentTotalAll = new ArrayList();
                setColumnList(columnsStudentTotalAll, totalColumns);
                joinColumnListArray(columnsStudentTotalAll, columnsStudentTotalHeader);
                joinColumnListArray(columnsStudentTotalAll, columnsStudentTotal);

                List[] columnStudentTotalFooterList = new List[totalColumns];
                for (int i = 0; i < columnStudentTotalFooterList.length; i++) {
                    columnStudentTotalFooterList[i] = new ArrayList();
                }

                //平均点
                final int maxLine = 5;
                int col = 0;
                final String avgHrAverage = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _param.SUBCLASSCD999999, "AVG");
                setSameSize(columnStudentTotalFooterList[col], maxLine);
                columnStudentTotalFooterList[col].set(1, StringUtils.defaultString(avgHrAverage)); // クラス平均
                columnStudentTotalFooterList[col].set(2, "");
                columnStudentTotalFooterList[col].set(3, "");
                columnStudentTotalFooterList[col].set(4, "");

                //不合格科目数
                col = 1;
                setSameSize(columnStudentTotalFooterList[col], maxLine);
                columnStudentTotalFooterList[col].set(1, "");
                columnStudentTotalFooterList[col].set(2, "");
                columnStudentTotalFooterList[col].set(3, "");
                columnStudentTotalFooterList[col].set(4, "");

                //今学期換算点
                col = 2;
                final List<Integer> kansanScores = studentGroup.kansanScores();
                setSameSize(columnStudentTotalFooterList[col], maxLine);
                columnStudentTotalFooterList[col].set(1, studentGroup._avgKansanScore); // クラス平均
                columnStudentTotalFooterList[col].set(2, "");
                columnStudentTotalFooterList[col].set(3, studentGroup.scoresMax(kansanScores)); // 最高点
                columnStudentTotalFooterList[col].set(4, studentGroup.scoresMin(kansanScores)); // 最低点

                //通算換算点
                col = 3;
                final List<Integer> totalKansanScores = studentGroup.totalKansanScores();
                setSameSize(columnStudentTotalFooterList[col], maxLine);
                columnStudentTotalFooterList[col].set(1, studentGroup._avgTotalKansanScore); // クラス平均
                columnStudentTotalFooterList[col].set(2, "");
                columnStudentTotalFooterList[col].set(3, studentGroup.scoresMax(totalKansanScores)); // 最高点
                columnStudentTotalFooterList[col].set(4, studentGroup.scoresMin(totalKansanScores)); // 最低点

                joinColumnListArray(columnsStudentTotalAll, columnStudentTotalFooterList);


                //出力
                final List<List<String>> columnList = new ArrayList<List<String>>();
                columnList.addAll(blockStudentName); //生徒情報
                columnList.addAll(blockSubclassList); //明細
                columnList.addAll(columnsStudentTotalAll); //明細 合計欄

                outputList.addAll(headerLineList);
                outputList.addAll(columnListToLines(columnList));
                newLine(outputList); // ブランク
                newLine(outputList); // ブランク

                return hasData;
            }

            private List<List<String>> columnListToLines(final List<List<String>> columnList) {
                final List<List<String>> lines = new ArrayList<List<String>>();
                int maxLine = 0;
                for (final List<String> column : columnList) {
                    maxLine = Math.max(maxLine, column.size());
                }
                for (int li = 0; li < maxLine; li++) {
                    lines.add(line(columnList.size()));
                }
                for (int ci = 0; ci < columnList.size(); ci++) {
                    final List<String> column = columnList.get(ci);
                    for (int li = 0; li < column.size(); li++) {
                        lines.get(li).set(ci, column.get(li));
                    }
                }
                return lines;
            }

            private List<List> setColumnList(final List<List> columnsList, final int column) {
                for (int i = columnsList.size(); i < column; i++) {
                    columnsList.add(new ArrayList<List>());
                }
                return columnsList;
            }

            private List<List> joinColumnListArray(final List<List> columnsList, final List[] columnStudentFooterList) {
                for (int i = 0; i < columnStudentFooterList.length; i++) {
                    columnsList.get(i).addAll(columnStudentFooterList[i]);
                }
                return columnsList;
            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param ipdf
             * @param studentGroup
             */
            private void printHeader(final IPdf ipdf, final StudentGroup studentGroup) {
                //ページ見出し
                ipdf.VrsOut("ymd1", _param._now); // 処理日
                ipdf.VrsOut("TITLE", _param._title); // タイトル
                ipdf.VrsOut("HR_NAME", "HR　" + studentGroup.getName()); // クラス
                ipdf.VrsOut("DATE", _param._attendTerm); // 集計日付範囲
                ipdf.VrsOut(getFieldForData(ipdf, new String[] {"HR_TEACHER", "HR_TEACHER2"}, studentGroup._staffname), studentGroup._staffname); // 担任

                //各項目名
                ipdf.VrsOut("NAME_TITLE", "氏名");
                ipdf.VrsOut("ITEM4", _param._item4Name);
                ipdf.VrsOut("ITEM5", _param._item5Name);
                ipdf.VrsOut("ITEM7", _param._item7Name);
                ipdf.VrsOut("ITEM9", _param._item9Name);
            }

            /**
             * 学級データの印字処理(学級平均) 右下
             * @param ipdf
             */
            private void printGroupInfo(final DB2UDB db2, final IPdf ipdf, final StudentGroup studentGroup) {
                final int col = -1;
                // 平均点 クラス平均
                final String avgHrAverage = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _param.SUBCLASSCD999999, "AVG");
                svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 1, "", col, StringUtils.defaultString(avgHrAverage));

                //今学期換算点 クラス平均・最高点・最低点
                final List<Integer> kansanScores = studentGroup.kansanScores();
                svfsetString1(ipdf, "RANK1", _param._formMaxLine + 1, "", col, studentGroup._avgKansanScore); // クラス平均
                svfsetString1(ipdf, "RANK1", _param._formMaxLine + 3, "", col, studentGroup.scoresMax(kansanScores)); // 最高点
                svfsetString1(ipdf, "RANK1", _param._formMaxLine + 4, "", col, studentGroup.scoresMin(kansanScores)); // 最低点

                //通算換算点 クラス平均・最高点・最低点
                final List<Integer> totalKansanScores = studentGroup.totalKansanScores();
                svfsetString1(ipdf, "FAIL1", _param._formMaxLine + 1, "", col, studentGroup._avgTotalKansanScore); // クラス平均
                svfsetString1(ipdf, "FAIL1", _param._formMaxLine + 3, "", col, studentGroup.scoresMax(totalKansanScores)); // 最高点
                svfsetString1(ipdf, "FAIL1", _param._formMaxLine + 4, "", col, studentGroup.scoresMin(totalKansanScores)); // 最低点
            }

            /**
             * 生徒別総合点・平均点・順位を印刷します。
             * @param ipdf
             */
            private void printStudentTotal(final DB2UDB db2, final IPdf ipdf, final int line, final Student student, final List<SubClass> subclassList) {
                for (int subi = 0, size = subclassList.size(); subi < size; subi++) {
                    final SubClass subclass = subclassList.get(subi);
                    final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);
                    if(detail != null && detail._recordScoreDatScore != null) {
                        if(Integer.parseInt(detail._recordScoreDatScore) < _param._defaultBorder) {
                            student._defaultNum += 1;
                            subclass._defaultNum += 1;
                        }
                    }
                }

                ipdf.VrsOutn("TOTAL1", line, StringUtils.defaultString(student._scoreAvg999999, "0")); //平均点
                ipdf.VrsOutn("AVERAGE1", line, String.valueOf(student._defaultNum)); //不合格科目数
                ipdf.VrsOutn("RANK1", line, student._kansanScore); //今学期換算点
                ipdf.VrsOutn("FAIL1", line, student._totalKansanScore); //通算換算点

//
//                //順位（学級）
//                if (1 <= student._classRank) {
//                    ipdf.VrsOutn("CLASS_RANK1", line, String.valueOf(student._classRank));
//                }
//                //順位（学年orコース）
//                if (1 <= student._rank) {
//                    ipdf.VrsOutn("RANK1", line, String.valueOf(student._rank));
//                }
//                //偏差値
//                final String gradeDeviation = defstr(getRankSdiv(db2, "GRADE_DEVIATION", student._schregno));
//                ipdf.VrsOutn("DEVI1", line, String.valueOf(gradeDeviation));
//                //評定平均
//                final String val1 = defstr(getRankSdiv(db2, "SCORE", student._schregno), "0");
//                final String val2 = defstr(getAssessMark(db2, val1));
//                ipdf.VrsOutn("VALUE1", line, val1);
//                ipdf.VrsOutn("VALUE2", line, val2);

                printAttendInfo(ipdf, student._attendInfo, line);
            }

            /**
             * 生徒の氏名・備考を印字
             * @param ipdf
             */
            private void printStudentName(final IPdf ipdf, final int line, final Student student) {
                ipdf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                // 氏名
                final int ketaName = getMS932ByteLength(student._name);
                ipdf.VrsOutn("NAME" + (ketaName <= 20 ? "" : ketaName <= 30 ? "_2" : "_3"), line, student._name);
                ipdf.VrsOutn("SEX", line, student._sex);
                ipdf.VrsOutn("FINSCHOOL", line, student._finschool);
            }

            private void printRemark(final IPdf ipdf, final int line, final Student student) {
                final String remark = student._attendSemesRemarkDatRemark1;
                final int keta = getMS932ByteLength(remark);
                if (ipdf instanceof SvfPdf) {
                    //final SvfPdf svfPdf = (SvfPdf) ipdf;
                    String[] fields = null;
                    fields = new String[] {"REMARK", "REMARK1_2", "REMARK1_3", "REMARK2_1"};
                    if (null != fields) {
                        ipdf.VrsOutn(getFieldForData(ipdf, fields, remark), line, remark);  // 備考
                    }
                } else {
                    ipdf.VrsOutn("REMARK" + (keta <= 20 ? "1" : keta <= 30 ? "1_2" : keta <= 40 ? "1_3" : "2_1"), line, remark);  // 備考
                }
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param ipdf
             * @param subclass
             * @param line：科目の列番号
             * @return
             */
            private void printSubclassHeader(
                    final IPdf ipdf,
                    final int col,
                    final SubClass subclass,
                    final boolean pat3isKariHyotei,
                    final ClassAbbvFieldSet abbvFieldSet,
                    final List<String> studentCourses
            ) {
                //教科名
                setRecordString(ipdf, "course1", col, subclass._classabbv);
                //科目名
                final String subclassfield = "subject1";
                if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_ELECTDIV); }
                String abbv = subclass._subclassabbv;
                setRecordString(ipdf, subclassfield, col, abbv);
                if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_NORMAL); }

                //単位数
                final String creditStr = subclass.getPrintCredit();
                final int creditStrLen = getMS932ByteLength(creditStr);
                final int creditFieldKeta = getFieldKeta(ipdf, "credit1");
                final String creditField = (creditFieldKeta < creditStrLen && creditFieldKeta < getFieldKeta(ipdf, "credit1_2")) ? "credit1_2" :"credit1";
                setRecordString(ipdf, creditField, col, creditStr);
                //授業時数
                setRecordString(ipdf, "lesson1", col, subclass.getJisu());
                //項目名
                final int mojisu = getMS932ByteLength(_param._item1Name);
                final int field1Keta = getFieldKeta(ipdf, "ITEM1");
                final String fieldname;
                if (field1Keta < mojisu && field1Keta < getFieldKeta(ipdf, "ITEM1_2")) {
                    fieldname = "ITEM1_2";
                } else {
                    fieldname = "ITEM1";
                }
                setRecordString(ipdf, fieldname, col, _param._item1Name);
                setRecordString(ipdf, "ITEM2", col, _param._item2Name);
            }

            /**
             * パターン3（仮評定付き）で科目が仮評定を表示するならtrue、そうでなければfalse
             * @param param
             * @param subclass 科目
             * @param studentList 生徒のリスト
             * @return パターン3（仮評定付き）で科目が仮評定を表示するならtrue、そうでなければfalse
             */
            private boolean pat3SubclassIsKariHyotei(final SubClass subclass, final List<Student> studentList) {
                if (!"9".equals(_param._semester)) {
                    return true; // 学年末以外は仮評定を表示
                }
                final boolean isD008 = _param._d008Namecd2List.contains(subclass._classcd) || "90".equals(subclass._classcd);
                int valueCount = 0;
                int provFlgCount = 0;
                for (final Student student : studentList) {
                    final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);
                    if (null != detail) {
                        if (null != detail._karihyotei) {
                            valueCount += 1;
                        }
                        if ("1".equals(detail._provFlg)) {
                            provFlgCount += 1;
                        }
                    }
                }
                // 評定がないもしくは仮評定フラグが1なら仮評定を表示する。そうでなければ本評定を印字する。
                final boolean isKari = valueCount == 0 && !isD008 || provFlgCount > 0;
                if (_param._isOutputDebug) {
                    log.info(" kari " + subclass.keySubclasscd() + " = " + isKari + "　( prov/value = " + provFlgCount + "/" + valueCount + ")");
                }
                return isKari;
            }

            public void printSubclassStat(final IPdf ipdf, final int col, final SubClass subclass) {
                final boolean useD001 = _param._d065Name1List.contains(subclass.keySubclasscd());
                if (useD001) {
                    return;
                }

                setRecordString(ipdf, "AVE_CLASS", col, subclass._scoreHrAverage); //クラス平均
                setRecordString(ipdf, "DEFAULT_NUM", col, String.valueOf(subclass._defaultNum)); //不合格者数
                setRecordString(ipdf, "MAX_SCORE", col, subclass._scoreMax); //最高点
                setRecordString(ipdf, "MIN_SCORE", col, subclass._scoreMin); //最低点
            }

            /**
             * 生徒別科目別素点・評定・欠課時数等を印刷します。
             * @param ipdf
             * @param line 生徒の行番
             */
            private void printScoreKekka(
                    final IPdf ipdf,
                    final String schregno,
                    final ScoreDetail detail,
                    final int line,
                    final int col,
                    final SubClass subclass,
                    final Student student
            ) {
                final String sline = String.valueOf(line);
                final List<String> attributes = new ArrayList<String>();
                String printScore = "";
                final Tuple<String, Tuple<String, List<String>>> printScoreAndMarkAndAttribute = getPrintScoreAndMarkAndAttribute(detail);
                printScore = printScoreAndMarkAndAttribute._first;
                final String mark = printScoreAndMarkAndAttribute._second._first;
                attributes.addAll(printScoreAndMarkAndAttribute._second._second);
                if (detail._isNarakenKekkaOver) {
                    log.info(" " + sline + " : " + schregno + ", score " + printScore);
                }
                printScore = mark + defstr(printScore);

                final String scoreField = getFieldForData(ipdf, new String[] {"SCORE" + sline,  "SCORE" + sline + "_2"}, printScore);
                final String attribute = mkString(attributes, ",");
                if (!StringUtils.isEmpty(attribute)) {
                    ipdf.VrAttribute(scoreField, attribute);
                }
                setRecordString(ipdf, scoreField, col, printScore);

                // 欠課
                final boolean isOutputJisu = _param._output2 && null != detail._jisu && !subclass.getJisu().equals(detail._jisu.toString());
                if (null != detail._absent || isOutputJisu) {
                    final String val;
                    if (isOutputJisu) {
                        val = (null == detail._absent ? "0" : String.valueOf(detail._absent.intValue())) + "/" + detail._jisu.toString();
                    } else {
                        val = getKekkaString(detail._absent);
                    }
                    final boolean isLongField = _param._definecode.absent_cov == 3 || _param._definecode.absent_cov == 4;
                    final String field;

                    if (isOutputJisu) {
                        field = getFieldForData(ipdf, new String[] {"kekka2_" + sline, "kekka3_" + sline}, val);
                    } else if (isLongField) {
                        field = "kekka2_" + sline;
                    } else {
                        field = "kekka" + sline;
                    }

                    if (detail._isKekkaOver) { ipdf.VrAttribute(field, ATTRIBUTE_KEKKAOVER); }
                    setRecordString(ipdf, field, col, val);
                    if (detail._isKekkaOver) { ipdf.VrAttribute(field, ATTRIBUTE_NORMAL); }
                }
            }

            public int setRecordString(IPdf ipdf, String field, int gyo, int retsu, String data) {
                return ipdf.setRecordString(field, gyo, data);
            }

            public int setRecordString(IPdf ipdf, String field, int gyo, String data) {
                return ipdf.setRecordString(field, gyo, data);
            }

            //年組、学年ごとに集計した値を取得
            private Map getScoreData(final DB2UDB db2, final String subclassCd, final String grade, final String gradeHrclass) {
                final StringBuffer stb = new StringBuffer();
                final String testkindcd = _param._testKindCd.substring(0, 2);
                final String testitemcd = _param._testKindCd.substring(2, 4);
                final String scoreDiv = _param._testKindCd.substring(4);
                stb.append("WITH ");

                //対象生徒の表 クラスの生徒
                stb.append(" SCHNO_A AS(");
                stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
                stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
                stb.append("            , 0 AS LEAVE ");
                stb.append("     FROM    SCHREG_REGD_DAT W1 ");
                stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

                stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
                if (SEMEALL.equals(_param._semester)) {
                    stb.append("     AND W1.SEMESTER = '" + _param._semeFlg + "' ");
                } else {
                    stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                    stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM  SCHREG_BASE_MST S1");
                    stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
                }
                stb.append("         AND W1.GRADE    = '"+ grade +"' ");
                if(!"".equals(gradeHrclass)) {
                    stb.append("         AND W1.HR_CLASS = '"+ gradeHrclass.substring(2) +"' ");
                }
                stb.append(") ");
                //対象講座の表
                stb.append(",CHAIR_A AS(");
                stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
                stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
                stb.append("            W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
                stb.append("     FROM   CHAIR_STD_DAT W1 ");
                stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
                stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
                stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
                stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR ");
                stb.append("         AND SEME.SEMESTER = W1.SEMESTER ");
                if ("1".equals(_param._printSubclassLastChairStd)) {
                    stb.append("         AND SEME.EDATE = W1.APPENDDATE ");
                }
                stb.append("     WHERE  W1.YEAR = '" + _param._year + "' ");
                stb.append("        AND W1.SEMESTER <= '" + _param._semester + "' ");
                stb.append("        AND EXISTS( ");
                stb.append("            SELECT 'X' ");
                stb.append("            FROM SCHNO_A W3 ");
                stb.append("                 LEFT JOIN SCHREG_REGD_GDAT WG ON WG.YEAR = W3.YEAR AND WG.GRADE = W3.GRADE ");
                stb.append("            WHERE W3.SCHREGNO = W1.SCHREGNO");
                stb.append("              AND WG.SCHOOL_KIND = W2.SCHOOL_KIND ");
                stb.append("        )");
                stb.append("        AND W2.SUBCLASSCD = '"+ subclassCd +"' ");
                stb.append("     )");
                stb.append(" , REL_COUNT AS (");
                stb.append("   SELECT SUBCLASSCD");
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
                stb.append("     , COUNT(*) AS COUNT ");
                stb.append("   FROM RELATIVEASSESS_MST ");
                stb.append("   WHERE GRADE = '" + _param._grade + "' AND ASSESSCD = '3' ");
                stb.append("   GROUP BY SUBCLASSCD");
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
                stb.append(" ) ");
                //成績データの表（通常科目）
                stb.append(",RECORD_REC AS(");
                stb.append("    SELECT  W3.SCHREGNO, ");
                stb.append("            W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD ");
                stb.append("     , W3.SCORE ");
                stb.append("     , W3.CLASS_DEVIATION ");
                stb.append("     , W3.GRADE_DEVIATION ");
                stb.append("     , W3.COURSE_DEVIATION ");
                stb.append("     , W3.MAJOR_DEVIATION ");
                stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM RELATIVEASSESS_MST L3 ");
                stb.append("           WHERE L3.GRADE = '" + _param._grade + "' AND L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append("             AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("             AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("             AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
                stb.append("          ) ELSE ");
                if ("1".equals(_param._useAssessCourseMst)) {
                    stb.append("          VALUE((SELECT MAX(L3.ASSESSLEVEL) ");
                    stb.append("           FROM ASSESS_COURSE_MST L3 ");
                    stb.append("           WHERE L3.ASSESSCD = '3' ");
                    stb.append("             AND L3.COURSECD = SCH.COURSECD  ");
                    stb.append("             AND L3.MAJORCD = SCH.MAJORCD  ");
                    stb.append("             AND L3.COURSECODE = SCH.COURSECODE  ");
                    stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                    stb.append("           ),  ");
                    stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
                    stb.append("           FROM ASSESS_MST L3 ");
                    stb.append("           WHERE L3.ASSESSCD = '3' ");
                    stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                    stb.append("          )) ");
                } else {
                    stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                    stb.append("           FROM ASSESS_MST L3 ");
                    stb.append("           WHERE L3.ASSESSCD = '3' ");
                    stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                    stb.append("          ) ");
                }
                stb.append("         END AS ASSESS_LEVEL ");
                stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
                stb.append("            INNER JOIN SCHNO_A SCH ON W3.SCHREGNO = SCH.SCHREGNO ");
                stb.append("                   AND SCH.LEAVE = 0 ");
                stb.append("            LEFT JOIN REL_COUNT T2 ");
                stb.append("                   ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append("                  AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("                  AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("                  AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
                stb.append("    WHERE   W3.YEAR = '" + _param._year + "' ");
                stb.append("            AND W3.SEMESTER = '" + _param._semester + "' ");
                stb.append("            AND W3.TESTKINDCD = '" + testkindcd + "' ");
                stb.append("            AND W3.TESTITEMCD = '" + testitemcd + "' ");
                stb.append("            AND W3.SCORE_DIV = '" + scoreDiv + "' ");
                stb.append("            AND W3.SUBCLASSCD = '"+ subclassCd +"' ");
                stb.append("     ) ");
                stb.append(" ,CHAIR_A2 AS ( ");
                stb.append("     SELECT  W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY ");
                stb.append("     FROM    CHAIR_A W2");
                if (!SEMEALL.equals(_param._semester)) {
                    stb.append(" WHERE   W2.SEMESTER = '" + _param._semester + "'");
                }
                stb.append("     GROUP BY W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY ");
                stb.append(" ) ");
                stb.append(",RECORD_GROUP AS( ");
                stb.append("     SELECT ");
                stb.append("              '"+_param._year+"' AS YEAR, ");
                stb.append("              '"+_param._semester+"' AS SEMESTER, ");
                stb.append("              L2.TESTKINDCD, L2.TESTITEMCD, L2.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY AS SUBCLASSCD ");
                stb.append("            , AVG(L2.SCORE) AS AVG ");
                stb.append("            , MAX(L2.SCORE) AS MAX_SCORE ");
                stb.append("            , MIN(L2.SCORE) AS MIN_SCORE ");
                if (_param._testKindCd != null && _param._testKindCd.endsWith("09") || _param._is5dankai) {
                    stb.append("            , COUNT(CASE WHEN L2.SCORE = '1' THEN 1 ELSE 0 END) AS COUNT ");
                } else {
                    stb.append("            , COUNT(CASE WHEN PERF.PASS_SCORE > 0 ");
                    stb.append("                         THEN CASE WHEN T3.ASSESS_LEVEL = '1'      THEN 1 ELSE 0 END ");
                    stb.append("                         ELSE CASE WHEN L2.SCORE < PERF.PASS_SCORE THEN 1 ELSE 0 END ");
                    stb.append("                    END) AS COUNT ");
                }
                stb.append("     FROM CHAIR_A2 T1 ");
                stb.append("          INNER JOIN SCHNO_A SCH ");
                stb.append("                  ON SCH.SCHREGNO = T1.SCHREGNO ");
                stb.append("                 AND SCH.LEAVE   = 0 ");
                stb.append("          LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
                stb.append("                 ON L2.YEAR          = '" + _param._year + "' ");
                stb.append("                AND L2.SEMESTER      = '" + _param._semester+ "' ");
                stb.append("                AND L2.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND L2.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND L2.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("                AND L2.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND L2.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                AND L2.SCHREGNO      = T1.SCHREGNO ");
                stb.append("          LEFT JOIN RECORD_SCORE_DAT RSD ");
                stb.append("                 ON RSD.YEAR          = '" + _param._year + "' ");
                stb.append("                AND RSD.SEMESTER      = '" + _param._semester + "' ");
                stb.append("                AND RSD.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND RSD.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND RSD.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("                AND RSD.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND RSD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND RSD.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND RSD.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                AND RSD.SCHREGNO      = T1.SCHREGNO ");
                stb.append("          LEFT JOIN PERFECT_RECORD_DAT PERF ");
                stb.append("                 ON PERF.YEAR          = '" + _param._year + "' ");
                stb.append("                AND PERF.SEMESTER      = '" + _param._semester + "' ");
                stb.append("                AND PERF.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND PERF.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND PERF.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND PERF.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND PERF.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE SCH.GRADE END ");
                stb.append("                AND (     PERF.DIV        IN ('01','02') ");
                stb.append("                      AND PERF.COURSECD   = '0' ");
                stb.append("                      AND PERF.MAJORCD    = '000' ");
                stb.append("                      AND PERF.COURSECODE = '0000' ");
                stb.append("                      OR ");
                stb.append("                          PERF.DIV NOT    IN ('01','02')  ");
                stb.append("                      AND PERF.COURSECD   = SCH.COURSECD ");
                stb.append("                      AND PERF.MAJORCD    = SCH.MAJORCD ");
                stb.append("                      AND PERF.COURSECODE = SCH.COURSECODE ");
                stb.append("                      AND PERF.COURSECD   = SCH.COURSECD ");
                stb.append("                      AND PERF.MAJORCD    = SCH.MAJORCD ");
                stb.append("                      AND PERF.COURSECODE = SCH.COURSECODE ");
                stb.append("                    ) ");
                stb.append("          LEFT JOIN RECORD_REC T3 ");
                stb.append("                   ON T3.CLASSCD       = T1.CLASSCD ");
                stb.append("                  AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                  AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                  AND T3.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                  AND T3.SCHREGNO      = T1.SCHREGNO");
                stb.append("     GROUP BY ");
                stb.append("              L2.TESTKINDCD, L2.TESTITEMCD, L2.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY ");
                stb.append(" ) ");
                //メイン表
                stb.append(" SELECT  T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY AS SUBCLASSCD");
                stb.append("        ,RGRP.AVG ");
                stb.append("        ,RGRP.COUNT ");
                stb.append("        ,RGRP.MAX_SCORE ");
                stb.append("        ,RGRP.MIN_SCORE ");
                stb.append("        ,RAD.STDDEV ");
                //対象生徒・講座の表
                stb.append(" FROM CHAIR_A2 T1 ");
                stb.append(" INNER JOIN RECORD_GROUP RGRP ");
                stb.append("                 ON RGRP.YEAR          = '" + _param._year + "' ");
                stb.append("                AND RGRP.SEMESTER      = '" + _param._semester + "' ");
                stb.append("                AND RGRP.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND RGRP.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND RGRP.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("                AND RGRP.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND RGRP.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND RGRP.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND RGRP.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ");
                stb.append("        ON SDET.YEAR          = '" + _param._year + "' ");
                stb.append("       AND SDET.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND SDET.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND SDET.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("       AND SDET.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("       AND SDET.SUBCLASS_SEQ  = '012' ");
                stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAD ");
                stb.append("        ON RAD.YEAR          = '" + _param._year + "' ");
                stb.append("       AND RAD.SEMESTER      = '" + _param._semester + "' ");
                stb.append("       AND RAD.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("       AND RAD.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("       AND RAD.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("       AND RAD.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND RAD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND RAD.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("       AND RAD.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                if(!"".equals(gradeHrclass)) {
                    stb.append("       AND RAD.AVG_DIV       = '2' ");
                    stb.append("       AND RAD.GRADE         = '"+ grade +"' ");
                    stb.append("       AND RAD.HR_CLASS      = '"+ gradeHrclass.substring(2) +"' ");
                } else {
                    stb.append("       AND RAD.AVG_DIV       = '1' ");
                    stb.append("       AND RAD.GRADE         = '"+ grade +"' ");
                    stb.append("       AND RAD.HR_CLASS      = '000' ");
                }
                stb.append("       AND RAD.COURSECD || RAD.MAJORCD || RAD.COURSECODE = '00000000' ");
                stb.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY ");
                final String sql =  stb.toString();

                PreparedStatement ps = null;
                ResultSet rs = null;
                Map resultMap = new HashMap() ;

                try {
                    log.debug(sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        resultMap.put("AVG", defstr(rs.getString("AVG"),"0"));
                        resultMap.put("COUNT", defstr(rs.getString("COUNT"),"0"));
                        resultMap.put("MAX_SCORE", defstr(rs.getString("MAX_SCORE"),"0"));
                        resultMap.put("MIN_SCORE", defstr(rs.getString("MIN_SCORE"),"0"));
                        resultMap.put("STDDEV", defstr(rs.getString("STDDEV"),"0"));
                    }
                } catch (SQLException ex) {
                    log.debug("Exception:", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }

                return resultMap;
            }

            private String getShukkoTeisu(final DB2UDB db2, final String year, final String semester, final String grade, final String coursecd, final String majorcd) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT SUM(LESSON) AS LESSON ");
                stb.append("   FROM ATTEND_LESSON_MST ");
                stb.append("  WHERE ");
                stb.append("        YEAR     = '"+ year +"' ");
                stb.append("    AND SEMESTER = '"+ semester +"' ");
                stb.append("    AND GRADE    = '"+ grade +"' ");
                stb.append("    AND COURSECD = '"+ coursecd +"' ");
                stb.append("    AND MAJORCD  = '"+ majorcd +"' ");
                stb.append(" GROUP BY YEAR,SEMESTER,GRADE,COURSECD,MAJORCD ");
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            }

            private String getRankSdiv(final DB2UDB db2, final String field, final String schregno) {
                final String testkindcd = _param._testKindCd.substring(0, 2);
                final String testitemcd = _param._testKindCd.substring(2, 4);
                final String scoreDiv = _param._testKindCd.substring(4);
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT "+field+" ");
                stb.append("   FROM RECORD_RANK_SDIV_DAT ");
                stb.append("  WHERE  ");
                stb.append("        YEAR       = '"+ _param._year +"' ");
                stb.append("    AND SEMESTER   = '"+ _param._semester +"' ");
                stb.append("    AND TESTKINDCD = '"+ testkindcd +"' ");
                stb.append("    AND TESTITEMCD = '"+ testitemcd +"' ");
                stb.append("    AND SCORE_DIV  = '"+ scoreDiv +"' ");
                stb.append("    AND SUBCLASSCD = '"+ ALL9 +"' ");
                stb.append("    AND SCHREGNO   = '"+ schregno +"' ");
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            }

            private String getAssessMark(final DB2UDB db2, final String score) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ASSESSMARK  ");
                stb.append("   FROM ASSESS_MST");
                stb.append("  WHERE ASSESSCD = '4'");
                stb.append("    AND "+ score +" BETWEEN ASSESSLOW AND ASSESSHIGH");
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
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

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        public SubclassMst(final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
        }
        public int compareTo(final SubclassMst m) {
            return _subclasscd.compareTo(m._subclasscd);
        }

        /**
         *
         * @param param
         * @param courses コースのリスト
         * @return コースのリストの1つでも合併元コースならTrue
         */
        public boolean isSaki(final Collection<String> courses, final Map<SubclassMst, Set<String>> combinedCourseListMap) {
            final Collection<String> replaceCourse = getMappedTreeSet(combinedCourseListMap, this);
            return replaceCourse.contains("ALL") || hasIntersection(replaceCourse, courses);
        }

        /**
         *
         * @param param
         * @param courses コースのリスト
         * @return コースのリストの1つでも合併先コースならTrue
         */
        public boolean isMoto(final Collection<String> courses, final Map<SubclassMst, Set<String>> attendCourseListMap) {
            final Collection<String> replaceCourse = getMappedTreeSet(attendCourseListMap, this);
            return replaceCourse.contains("ALL") || hasIntersection(replaceCourse, courses);
        }

        public boolean hasIntersection(final Collection<String> courses1, final Collection<String> courses2) {
            for (final String c1 : courses1) {
                if (courses2.contains(c1)) {
                    return true;
                }
            }
            return false;
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 76403 $ $Date: 2020-09-03 20:18:11 +0900 (木, 03 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    protected static class Tuple<K, V> implements Comparable<Tuple<K, V>> {
        final K _first;
        final V _second;
        private Tuple(final K first, final V second) {
            _first = first;
            _second = second;
        }

        public static <K, V> Tuple<K, V> of(final K first, final V second) {
            return new Tuple<K, V>(first, second);
        }

        public int compareTo(final Tuple<K, V> to) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == to._first && !(to._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(to._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == to._second && !(to._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(to._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    protected static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;
        final boolean _isLastSemester;

        /** 学年 */
        final String _grade;
        final String[] _classSelected;

        /** 指示画面チェックボックス */
        final boolean _output1; //必履修区分順に出力する
        final boolean _output2; //授業時数を分数表記する

        /** 出欠集計日付 */
        final String _sdate;
        final String _date;
        final String _testKindCd;
        final String _scoreDiv;
        final String _schoolKind;
        final String _useSchool_KindField;
        final String SCHOOLCD;
        final String SCHOOLKIND;

        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
        final String _checkKettenDiv;
        /** 起動元のプログラムＩＤ */
        final String _prgId;

        final String _useAssessCourseMst;
        final String _cmd;

        /** フォーム生徒行数 */
        int _formMaxLine;

        /** 科目数 */
        int _formMaxColumn;
        final String _formname;
        private String[] _recordField;

        /** 生徒の時数を印字する */
        final boolean _isOutputCoursePage;
        final String _printSubclassLastChairStd;

        final String _documentroot;
        final String _attendTerm;
        final String _useAttendSemesHrRemark;
        final String _useSubclassWeightingCourseDat;
        final String _now;
        final boolean _hasATTEND_BATCH_INPUT_HDAT;
        final boolean _hasATTEND_SEMES_REMARK_HR_DAT;

        private String _yearDateS;
        private String _semesterName;
        private TestItem _testItem;

        private static final String FROM_TO_MARK = "\uFF5E";

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        private KNJSchoolMst _knjSchoolMst;

        final String _nendo;
        final boolean _is5dankai;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugKetten;
        final boolean _isOutputDebugFormCreate;

        private String _avgDiv;
        final String _d054Namecd2Max;
        final List<String> _d065Name1List;
        final Map<String, String> _d001Name1Map;
        final List<String> _d079Name1List;
        final List<String> _d008Namecd2List;
        private Map<String, SubclassMst> _subclassMst;
        private Map<SubclassMst, Set<String>> _combinedCourseListMap;
        private Map<SubclassMst, Set<String>> _attendCourseListMap;
        private boolean _isPrintSakiKamoku;
        private boolean _isNoPrintMoto;

        private final String SUBCLASSCD999999;

        final String _title;
        private String _item1Name;  // 明細項目名
        final String _item2Name;  // 明細項目名
        final String _item4Name;
        final String _item5Name;
        final String _item7Name;
        final String _item9Name;
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
        final boolean _isPrintPerfect;
        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        private String _currentform;
        private Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap();
        private List<String> _svfformModifyKeys = Collections.emptyList();
        private List<String> _formFieldInfoLog = new ArrayList<String>();

        final String _logindate;

        final Map<String, String> _vSchoolMst;

        private Set<String> _logOnce = new HashSet<String>();
        private Map<String, File> _createdFiles = new HashMap<String, File>();
        private final Map<String, String> _elapsed = new TreeMap<String, String>();

        /**
         * 1なら「授業時数」欄にはMLESSON(出席すべき授業時数）ではなくLESSON(授業時数)を表示
         */
        final String _knjd626kJugyoJisuLesson;

        /**
         * 指定年度の時間割数
         */
        final int _schChrDatCount;

        final int _defaultBorder; //不合格の基準点

        String _attendSemes;
        String _attendSdate;
        String _attendEdate;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            boolean hasSDate = false;
            for (final Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
                final String parameterName = e.nextElement();
                if ("SDATE".equals(parameterName)) {
                    hasSDate = true;
                    break;
                }
            }
            _sdate = hasSDate ? KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE")) : KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '1' "));

            _output1 = "1".equals(request.getParameter("OUTPUT1"));
            _output2 = "1".equals(request.getParameter("OUTPUT2"));

            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugKetten = ArrayUtils.contains(outputDebug, "ketten");
            _isOutputDebugFormCreate = ArrayUtils.contains(outputDebug, "formCreate");
            log.info(" isOutputDebug = " + ArrayUtils.toString(outputDebug));

            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _scoreDiv = _testKindCd.substring(4);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _cmd = request.getParameter("cmd");
            _prgId = request.getParameter("PRGID");


            SUBCLASSCD999999 = "999999";
            _recordField = new String[] {};

            _formname = "KNJD626K.frm";
            _formMaxLine = 50;
            _formMaxColumn = 20;

            _hasATTEND_SEMES_REMARK_HR_DAT = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_SEMES_REMARK_HR_DAT", null);
            _hasATTEND_BATCH_INPUT_HDAT = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_BATCH_INPUT_HDAT", null);
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD") ? "1" : "";
            _useAttendSemesHrRemark = request.getParameter("useAttendSemesHrRemark");
            _useSubclassWeightingCourseDat = request.getParameter("useSubclassWeightingCourseDat");
            _logindate = defstr(request.getParameter("LOGIN_DATE"));
            _knjd626kJugyoJisuLesson = request.getParameter("knjd626kJugyoJisuLesson");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            try {
                final Map<String, Object> paramMap = new HashMap<String, Object>();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                    paramMap.put("SCHOOL_KIND", _schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _isLastSemester = null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester);

            final StringBuffer vSchoolMstSql = new StringBuffer();
            vSchoolMstSql.append(" SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" + _year + "' ");
            if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                vSchoolMstSql.append(" AND SCHOOL_KIND = '" + _schoolKind + "'  ");
            }
            _vSchoolMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, vSchoolMstSql.toString()));

            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            final boolean useOutputCoursePage = "1".equals(request.getParameter("USE_OUTPUT_COURSE_PAGE"));
            _isOutputCoursePage = !useOutputCoursePage || useOutputCoursePage && "1".equals(request.getParameter("OUTPUT_COURSE_PAGE"));
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");

            _is5dankai = _testKindCd.endsWith("990008");

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            final Map d054Max = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(d054Max, "NAMECD2");
            _d065Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' "), "NAME1");
            _d001Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D001' "), "NAMECD2", "NAME1");
            final String d008NameCd1 = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + d008NameCd1 + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd2List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + (d008Namecd2Cnt > 0 ? d008NameCd1 : "D008") + "' "), "NAMECD2");
            final String d079field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            _d079Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D079' AND " + d079field + " = '1'"), "NAME1");
            if (_isOutputDebug) {
                log.info(" D079 = " + _d079Name1List);
            }
            _definecode = createDefineCode(db2);
            //  学期名称、範囲の取得
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));  //学期名称
            // 年度の開始日
            _yearDateS = _sdate;
            if (null == _yearDateS) {
                _yearDateS = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + SEMEALL + "' "));
            }
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
            _item4Name = "平均点";
            _item5Name = "不合格科目数";
            _item7Name = "今学期換算点";
            _item9Name = "通算換算点";
            _attendTerm = getAttendTermKekka(db2, _yearDateS);
            _now = getNow(db2);

            _isGakunenMatu = SCORE_DIV_09.equals(_scoreDiv);
            _isPrintPerfect = SCORE_DIV_01.equals(_scoreDiv);
            if (SCORE_DIV_01.equals(_scoreDiv) || SCORE_DIV_02.equals(_scoreDiv)) {
                _item1Name = "素点";
            } else if (SCORE_DIV_08.equals(_scoreDiv)) {
                _item1Name = "評価";
            } else if (SCORE_DIV_09.equals(_scoreDiv)) {
                _item1Name = "評定";
            }
            _item2Name = "欠課";

            _title = _nendo + "　" + _semesterName + "　" + _testItem._testitemname + "　成績一覧表";
            _avgDiv = "2";
            setSubclassMst(db2);
            setPrintSakiKamoku(db2);
            loadNameMstD016(db2);
            _schChrDatCount = toInt(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM SCH_CHR_DAT WHERE YEAR = '" + _year + "' ")), 0);

            if (SCORE_DIV_08.equals(_scoreDiv)) {
                _defaultBorder = 3;
            } else {
                _defaultBorder = 1;
            }

            getAttendSemesterInfo(db2);
        }

        private void close() {
            if (_isOutputDebug) {
                for (final Map.Entry<String, String> e : _elapsed.entrySet()) {
                    log.info(" elapsed " + e.getKey() + " = " + e.getValue());
                }
            }
            for (final Iterator<PreparedStatement> it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = it.next();
                DbUtils.closeQuietly(ps);
                it.remove();
            }
            for (final File file : _createdFiles.values()) {
                log.info(" file " + file.getAbsolutePath() + (_isOutputDebugFormCreate ?  "" : " deleted? " + file.delete()));
            }
        }

        private void logOnce(final String s) {
            if (!_logOnce.contains(s)) {
                log.info(s);
                _logOnce.add(s);
            }
        }

        private PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD626K' AND NAME = '" + propName + "' "));
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            stb.append(KNJ_EditDate.getAutoFormatDate(db2, new SimpleDateFormat("yyyy-MM-dd").format(date)));
            final SimpleDateFormat sdf = new SimpleDateFormat("H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        /** 欠課の集計範囲 */
        public String getAttendTermKekka(final DB2UDB db2, final String startDate) {
            return KNJ_EditDate.getAutoFormatDate(db2, startDate) + FROM_TO_MARK + KNJ_EditDate.getAutoFormatDate(db2, _date);
        }

        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 3:
            case 4:
                absentFmt = new DecimalFormat("0.0");
                break;
            default:
                absentFmt = new DecimalFormat("0");
            }
            return absentFmt;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        private TestItem getTestItem(final DB2UDB db2, final String year, final String semester, final String testcd) {
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
                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' "));
                if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) {
                    _isNoPrintMoto = true;
                }
                log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
            }
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

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null);
            }
            return _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(final DB2UDB db2) {
            _subclassMst = new HashMap<String, SubclassMst>();
            String sql = "";
            sql += " SELECT ";
            sql += "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            sql += "    , T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME ";
            sql += " FROM SUBCLASS_MST T1 ";
            sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            sql += " WHERE ";
            sql += "     T1.SCHOOL_KIND = '" + _schoolKind + "' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"));
                _subclassMst.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
            }

            _combinedCourseListMap = new TreeMap<SubclassMst, Set<String>>();
            _attendCourseListMap = new TreeMap<SubclassMst, Set<String>>();
            if ("1".equals(_useSubclassWeightingCourseDat)) {
                String sql2 = "";
                sql2 += " SELECT ";
                sql2 += "     COURSECD || MAJORCD || COURSECODE AS COURSE ";
                sql2 += "   , COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
                sql2 += "   , ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql2 += " FROM SUBCLASS_WEIGHTING_COURSE_DAT ";
                sql2 += " WHERE YEAR = '" + _year + "' ";
                sql2 += "   AND FLG = '" + ("99".equals(_testKindCd.substring(0, 2)) ? "2" : "1") + "' ";
                sql2 += "   AND GRADE = '" + _grade + "' ";
                for (final Map row : KnjDbUtils.query(db2, sql2)) {
                    final SubclassMst combined = _subclassMst.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = _subclassMst.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null == combined || null == attend) {
                        continue;
                    }
                    getMappedTreeSet(_combinedCourseListMap, combined).add(KnjDbUtils.getString(row, "COURSE"));
                    getMappedTreeSet(_attendCourseListMap, attend).add(KnjDbUtils.getString(row, "COURSE"));
                }

            } else {
                String sql2 = "";
                sql2 += " SELECT ";
                sql2 += "     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
                sql2 += "   , ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql2 += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                for (final Map row : KnjDbUtils.query(db2, sql2)) {
                    final SubclassMst combined = _subclassMst.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = _subclassMst.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null == combined || null == attend) {
                        continue;
                    }
                    getMappedTreeSet(_combinedCourseListMap, combined).add("ALL");
                    getMappedTreeSet(_attendCourseListMap, attend).add("ALL");
                }
            }
            if (_isOutputDebug) {
                log.info(" combinedCourseListMap = " + _combinedCourseListMap);
                log.info(" attendCourseListMap = " + _attendCourseListMap);
            }
        }

        private void  getAttendSemesterInfo(DB2UDB db2) {
            _attendSemes = _semester;
            _attendSdate = _sdate;
            _attendEdate = _date;
            final String gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '"+ _year +"' AND GRADE = '"+ _grade +"' "));
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   SEMESTER, ");
                stb.append("   SDATE ");
                stb.append(" FROM ");
                stb.append("   SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("       YEAR  = '"+ _year +"' ");
                if("1".equals(_semester)) {
                     //「全学年の1学期」の場合、単学期の集計
                    stb.append("   AND SEMESTER = '"+ _semester +"' ");
                } else if("2".equals(_semester)) {
                    if("03".equals(gradeCd)) {
                        //「3年の2学期」の場合、累積の集計
                        stb.append("   AND SEMESTER = '"+ SEMEALL +"' ");
                    } else {
                        //「1・2年の2学期」の場合、単学期の集計
                        stb.append("   AND SEMESTER = '"+ _semester +"' ");
                    }
                } else {
                    //「全学年の3学期」の場合、累積の集計
                    stb.append("   AND SEMESTER = '"+ SEMEALL +"' ");
                }
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _attendSemes = StringUtils.defaultString(rs.getString("SEMESTER"));
                    _attendSdate = StringUtils.defaultString(rs.getString("SDATE"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

}
