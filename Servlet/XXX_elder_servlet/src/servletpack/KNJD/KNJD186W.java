// kanji=漢字
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186W {
    private static final Log log = LogFactory.getLog(KNJD186W.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private static final String PATTERN_A = "1";
    private static final String PATTERN_B = "2";
    private static final String PATTERN_C = "3";

    private static final int COLUMN_ITEM_TOTAL = 1;
    private static final int COLUMN_ITEM_HR_AVG = 2;
    private static final int COLUMN_ITEM_HR_RANK = 3;
    private static final int COLUMN_ITEM_COURSE_AVG = 4;
    private static final int COLUMN_ITEM_COURSE_RANK = 5;
    private static final int COLUMN_ITEM_GRADE_RANK = 6;

    private static final int LINE_ITEM_TANNI = 1;
    private static final int LINE_ITEM_SCORE = 2;
    private static final int LINE_ITEM_KEKKA = 3;

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

        load(param, db2, studentList);

        for (final Student student : studentList) {
            log.info(" schregno = " + student._schregno);
            param._form.init(param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));
            param._form.print(ipdf, db2, student);
        }
        _hasData = true;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static String getString(final int val) {
        return String.valueOf(val);
    }

    private static void VrsOutnKurikaeshi(final IPdf ipdf, final String field, final String[] token) {
        for (int j = 0; j < token.length; j++) {
            ipdf.VrsOutn(field, j + 1, token[j]);
        }
    }

    private static String distinctConcat(final List<String> stringList, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String c = "";
        for (final String clubname : stringList) {
            stb.append(c).append(clubname);
            c = comma;
        }
        return stb.toString();
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List<Student> studentList
    ) {
        Student.loadPreviousCredits(db2, param, studentList);  // 前年度までの修得単位数取得

        final Form form = param._form;

        final Map<String, List<Student>> courseStudentsMap = new HashMap();
        for (final Student student : studentList) {
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            getMappedList(courseStudentsMap, key).add(student);
        }

        for (final String key : courseStudentsMap.keySet()) {
            final List<Student> courseGroupStudentList = courseStudentsMap.get(key);
            final String[] split = StringUtils.split(key, "-");

            final List<String> recordMockOrderSdivDatList = param.getRecordMockOrderSdivDat(split[0], split[1], split[2]);

            form.init(recordMockOrderSdivDatList);

            String testcdor = "";
            final List testcdList = new ArrayList();
            for (int i = 0; i < form._testcds.length; i++) {
                final String testcd = form._testcds[i];
                if (null == testcd) {
                    continue;
                }
                testcdList.add(testcd);
            }
            if (("9".equals(param._semester) || PATTERN_C.equals(param._patternDiv) && param._semester.equals(param._knjSchoolMst._semesterDiv)) && !testcdList.contains(TESTCD_GAKUNEN_HYOTEI)) {
                testcdList.add(TESTCD_GAKUNEN_HYOTEI);
            }
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            for (int i = 0; i < testcdList.size(); i++) {
                final String testcd = (String) testcdList.get(i);
                final String seme = testcd.substring(0, 1);
                final String kind = testcd.substring(1, 3);
                final String item = testcd.substring(3, 5);
                final String sdiv = testcd.substring(5);
                if (seme.compareTo(param._semester) <= 0 || "9".equals(seme) && PATTERN_C.equals(param._patternDiv) && param._semester.equals(param._knjSchoolMst._semesterDiv) || TESTCD_GAKUNEN_HYOTEI.equals(testcd)) {
                    stbtestcd.append(testcdor);
                    stbtestcd.append(" W3.SEMESTER = '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ");
                    testcdor = " OR ";
                    //log.debug(" use testcd " + testcd);
                }
            }
            stbtestcd.append(") ");
            Score.load(db2, param, courseGroupStudentList, stbtestcd);

            for (final DateRange range : form._attendRanges.values()) {
                Attendance.load(db2, param, courseGroupStudentList, range);
            }
            for (final DateRange range : form._attendSubclassRanges.values()) {
                SubclassAttendance.load(db2, param, courseGroupStudentList, range);
            }
        }

        Student.setTotalStudy(db2, param, studentList);
        Student.setRemark(db2, param, studentList);
        Student.setFootnote(db2, param, studentList);
        Student.setClub(db2, param, studentList);
        Student.setCommittee(db2, param, studentList);
        Student.setQualifiedList(db2, param, studentList);
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 生徒
     */
    private static class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _grade;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _attendno;
        String _hrClassName1;
        String _curriculumYear;
        final Map<String, Attendance> _attendMap = new TreeMap();
        final Map _attendRemarkMap = new TreeMap();
        final Map<String, Subclass> _subclassMap = new TreeMap();
        final Map<String, List<String>> _semesClubListMap = new TreeMap();
        final Map<String, List<String>> _semesCommitteeListMap = new TreeMap();
        List<Map<String, String>> _qualifiedList = Collections.EMPTY_LIST;
        String _entyear;
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数

        private Map<String, String> _semesterTotalStudyactMap = new HashMap();
        private Map<String, String> _semesterTotalStudytimeMap = new HashMap();

        private String _club;
        private String _committee;
        private String _footnote;
        private String _hreportremarkDetailDatRemark1;
        private String _hreportremarkDat9Remark1;
        private String _remark;


        Subclass getSubclass(final String subclasscd) {
            return _subclassMap.get(subclasscd);
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
            stb.append("            ,W10.STAFFNAME AS STAFFNAME2 ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("            ,ENTGRD.CURRICULUM_YEAR ");
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
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = W6.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = W1.COURSECD ");
            stb.append("                  AND W9.MAJORCD = W1.MAJORCD ");
            stb.append("     LEFT JOIN STAFF_MST W10 ON W10.STAFFCD = W6.TR_CD2 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = W1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
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

            final List<Student> students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = new Student();
                    student._schregno = rs.getString("SCHREGNO");
                    student._name = rs.getString("NAME");
                    student._hrname = rs.getString("HR_NAME");
                    student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                    student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                    student._grade = rs.getString("GRADE");
                    student._coursecd = rs.getString("COURSECD");
                    student._majorcd = rs.getString("MAJORCD");
                    student._course = rs.getString("COURSE");
                    student._majorname = rs.getString("MAJORNAME");
                    student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    student._entyear = rs.getString("ENT_YEAR");
                    student._curriculumYear = rs.getString("CURRICULUM_YEAR");
                    students.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
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
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            if (!"1".equals(param._knjSchoolMst._schoolDiv)) {
                stb.append("    AND T1.ANNUAL < '" + param._grade + "'");
                stb.append("    AND T1.YEAR IN (SELECT MAX(YEAR) AS YEAR FROM SCHREG_REGD_DAT WHERE YEAR <= '" + param._year + "' AND SCHREGNO = ? GROUP BY GRADE) ");
            }
            final String sql = stb.toString();

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    int i = 1;
                    ps.setString(i++, student._schregno);
                    if (!"1".equals(param._knjSchoolMst._schoolDiv)) {
                        ps.setString(i++, student._schregno);
                    }

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

        public String getTotalGetCredit(final Param param) {
            int totalGetCredit = 0;
            int totalGetCreditKari = 0;
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                final Subclass subClass = getSubclass(subclasscd);
                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                if (SEMEALL.equals(param._semester) || subClass.isZenkiKamoku()) {
                    final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
                    if (null != score && NumberUtils.isDigits(score.getGetCredit())) {
                        final int iCredit = Integer.parseInt(score.getGetCredit());
                        if ("1".equals(score._provFlg)) {
                            totalGetCreditKari += iCredit;
                            if (param._isOutputDebug) {
                                log.info(" " + subClass._mst.toString() + " = (" + iCredit + ")");
                            }
                        } else {
                            totalGetCredit += iCredit;
                            if (param._isOutputDebug) {
                                log.info(" " + subClass._mst.toString() + " =  " + iCredit + "");
                            }
                        }
                    }
                }
            }
            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
                return "(" + String.valueOf(totalGetCreditKari) + ")";
            }
            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
        }

        public static void setRemark(final DB2UDB db2, final Param param, final List<Student> studentList) {
            final StringBuffer sql1 = new StringBuffer();
            sql1.append(" SELECT SEMESTER, REMARK1, REMARK2 ");
            sql1.append(" FROM HREPORTREMARK_DETAIL_DAT ");
            sql1.append(" WHERE YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                sql1.append("   AND SEMESTER = '" + SEMEALL + "' ");
            } else {
                sql1.append("   AND SEMESTER <= '" + param._semester + "' ");
            }
            sql1.append("   AND DIV = '06' ");
            sql1.append("   AND SCHREGNO = ? ");
            sql1.append(" ORDER BY SEMESTER, CODE ");

            PreparedStatement ps1 = null;
            try {
                ps1 = db2.prepareStatement(sql1.toString());

                for (final Student student : studentList) {

                    StringBuffer stb = new StringBuffer();

                    ps1.setString(1, student._schregno);
                    ResultSet rs = ps1.executeQuery();
                    while (rs.next()) {
                        if (!"1".equals(rs.getString("REMARK2"))) {
                            continue;
                        }
                        if (StringUtils.isBlank(rs.getString("REMARK1"))) {
                            continue;
                        }
                        if (0 != stb.length()) {
                            stb.append("\n"); // 改行
                        }
                        stb.append(rs.getString("REMARK1"));
                    }
                    DbUtils.closeQuietly(rs);

                    student._hreportremarkDetailDatRemark1 = stb.toString();
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps1);
                db2.commit();
            }


            final StringBuffer sql2 = new StringBuffer();
            sql2.append(" SELECT REMARK1 ");
            sql2.append(" FROM HREPORTREMARK_DAT ");
            sql2.append(" WHERE YEAR = '" + param._year + "' ");
            sql2.append("   AND SEMESTER = '" + SEMEALL + "' ");
            sql2.append("   AND SCHREGNO = ? ");

            PreparedStatement ps2 = null;
            try {
                ps2 = db2.prepareStatement(sql2.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps2.setString(1, student._schregno);
                    ResultSet rs = ps2.executeQuery();
                    while (rs.next()) {
                        student._hreportremarkDat9Remark1 = rs.getString("REMARK1");
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps2);
                db2.commit();
            }

            for (final Student student : studentList) {
                final String a = student._hreportremarkDetailDatRemark1;
                final String b = student._hreportremarkDat9Remark1;
                if (!StringUtils.isEmpty(a) && !StringUtils.isEmpty(b)) {
                    student._remark = StringUtils.defaultString(a) + "\n" + StringUtils.defaultString(b);
                } else {
                    student._remark = StringUtils.defaultString(a) + StringUtils.defaultString(b);
                }
            }
        }

        private static Map rsToMap(final ResultSet rs) throws SQLException {
            final Map m = new HashMap();
            final ResultSetMetaData meta = rs.getMetaData();
            for (int col = 1; col <= meta.getColumnCount(); col++) {
                final String key = meta.getColumnName(col);
                final String val = rs.getString(col);
                m.put(key, val);
            }
            return m;
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
                stb.append("     T1.SEMESTER ");
                stb.append("     ,T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("     ,T1.TOTALSTUDYTIME ");
                stb.append("     ,T1.TOTALSTUDYACT");
                stb.append(" FROM ");
                stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");

                final String sql = stb.toString();

                log.debug(" total study sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    student._semesterTotalStudyactMap = new HashMap();
                    student._semesterTotalStudytimeMap = new HashMap();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String totalStudyact = StringUtils.defaultString(student._semesterTotalStudyactMap.get(semester));
                        student._semesterTotalStudyactMap.put(semester, addLine(totalStudyact, rs.getString("TOTALSTUDYACT")));
                        final String totalStudytime = StringUtils.defaultString(student._semesterTotalStudytimeMap.get(semester));
                        student._semesterTotalStudytimeMap.put(semester, addLine(totalStudytime, rs.getString("TOTALSTUDYTIME")));
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 定型コメントをセット
         * @param studentList
         */
        public static void setFootnote(final DB2UDB db2, final Param param, final List<Student> studentList) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.FOOTNOTE ");
            stb.append(" FROM ");
            stb.append("     RECORD_DOCUMENT_KIND_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + ("9".equals(param._semester) ? param._knjSchoolMst._semesterDiv : param._semester) + "' ");
            stb.append("     AND T1.TESTKINDCD = '99' ");
            stb.append("     AND T1.TESTITEMCD = '00' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.HR_CLASS = '000' ");
            stb.append("     AND T1.COURSECD = '0' ");
            stb.append("     AND T1.MAJORCD = '000' ");
            stb.append("     AND T1.COURSECODE = '0000' ");
            stb.append("     AND T1.CLASSCD = '00' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     AND T1.CURRICULUM_CD = '00' ");
            stb.append("     AND T1.SUBCLASSCD = '000000' ");
            stb.append("     AND T1.KIND_DIV = '2' ");

            final String sql = stb.toString();
            //log.debug(" footnote sql = " + sql);

            final String footnote = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));

            for (final Student student : studentList) {

                student._footnote = footnote;
            }
        }

        public static void setClub(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     TSEM.SEMESTER, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME, ");
            stb.append("     NMJ001.NAME1 AS EXECUTIVENAME, ");
            stb.append("     CASE WHEN T1.EDATE <= TSEM.EDATE THEN 1 END AS TAIBU_FLG, ");
            stb.append("     CASE WHEN T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
            stb.append("                       T1.SDATE <= TSEM.SDATE AND TSEM.EDATE <=  VALUE(T1.EDATE, TSEM.EDATE) THEN 1 END AS FLG ");
            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
            stb.append("     AND TSEM.SEMESTER <> '9' ");
            stb.append("     AND TSEM.SEMESTER <= '" + param.getRegdSemester() + "' ");
            stb.append(" LEFT JOIN NAME_MST NMJ001 ");
            stb.append("     ON NMJ001.NAMECD1 = 'J001' ");
            stb.append("     AND NMJ001.NAMECD2 = T1.EXECUTIVECD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        String clubname = rs.getString("CLUBNAME");
                        final String executivename = rs.getString("EXECUTIVENAME");
                        final String flg = rs.getString("FLG");
                        final String taibuFlg = rs.getString("TAIBU_FLG");

                        if ("1".equals(taibuFlg) || !"1".equals(flg) || StringUtils.isBlank(clubname)) {
                            continue;
                        }
                        if (!StringUtils.isBlank(executivename)) {
                            clubname += "(" + executivename + ")";
                        }
                        if (!getMappedList(student._semesClubListMap, "9").contains(clubname)) {
                            getMappedList(student._semesClubListMap, "9").add(clubname);
                        }
                    }
                    DbUtils.closeQuietly(rs);

                    final StringBuffer stbClub = new StringBuffer();
                    String lf = "";
                    for (final String semester : student._semesClubListMap.keySet()) {
                        stbClub.append(lf).append(distinctConcat(getMappedList(student._semesClubListMap, semester), "、"));
                        lf = "\n";
                    }
                    student._club = stbClub.toString();
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static void setCommittee(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     VALUE(NMJ004.NAME1, '') AS SEMESTERNAME, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD, ");
            stb.append("     T1.CHARGENAME, ");
            stb.append("     NMJ002.NAME1 AS EXECUTIVENAME, ");
            stb.append("     T2.COMMITTEENAME ");
            stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb.append(" LEFT JOIN NAME_MST NMJ004 ON NMJ004.NAMECD1 = 'J004' ");
            stb.append("     AND NMJ004.NAMECD2 = T1.SEMESTER ");
            stb.append(" LEFT JOIN NAME_MST NMJ002 ");
            stb.append("     ON NMJ002.NAMECD1 = 'J002' ");
            stb.append("     AND NMJ002.NAMECD2 = T1.EXECUTIVECD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND (T1.SEMESTER = '9' OR T1.SEMESTER BETWEEN '1' AND '" + param.getRegdSemester() + "') ");
            stb.append("     AND T1.COMMITTEE_FLG IN ('1', '2') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    final Map<String, String> semesternameMap = new HashMap();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final String executivename = rs.getString("EXECUTIVENAME");
                        String name = null;
                            name = rs.getString("COMMITTEENAME");
                        if (StringUtils.isBlank(name)) {
                            continue;
                        }
                        if (!StringUtils.isBlank(executivename)) {
                            name += "(" + executivename + ")";
                        }
                        semesternameMap.put(semester, rs.getString("SEMESTERNAME"));
                        getMappedList(student._semesCommitteeListMap, semester).add(name);
                    }
                    DbUtils.closeQuietly(rs);

                    final StringBuffer stbCommittee = new StringBuffer();
                    String lf = "";
                    for (final String semester : student._semesCommitteeListMap.keySet()) {
                        final String semestername = semesternameMap.get(semester);
                        stbCommittee.append(lf).append(semestername).append("：").append(distinctConcat(getMappedList(student._semesCommitteeListMap, semester), "、"));
                        lf = "\n";
                    }
                    student._committee = stbCommittee.toString();
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
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

        private static void setQualifiedList(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH DATA_MAX8 AS ( ");
                stb.append(" SELECT ");
                stb.append("    T2.QUALIFIED_NAME ");
                stb.append("    , NMH312.NAME1 AS RANK_NAME ");
                stb.append("    , T1.REGDDATE ");
                stb.append("    , T1.SEQ ");
                stb.append(" FROM SCHREG_QUALIFIED_HOBBY_DAT T1 ");
                stb.append(" INNER JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
                stb.append(" LEFT JOIN NAME_MST NMH312 ON NMH312.NAMECD1 = 'H312' ");
                stb.append("     AND NMH312.NAMECD2 = T1.RANK ");
                stb.append(" WHERE ");
                stb.append(" T1.YEAR = '" + param._year + "' ");
                stb.append(" AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("   T1.REGDDATE DESC ");
                stb.append("   , T1.SEQ ");
                stb.append(" FETCH FIRST 8 ROWS ONLY ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("    QUALIFIED_NAME, ");
                stb.append("    RANK_NAME, ");
                stb.append("    REGDDATE, ");
                stb.append("    SEQ ");
                stb.append(" FROM ");
                stb.append("    DATA_MAX8 ");
                stb.append(" ORDER BY ");
                stb.append("    REGDDATE, ");
                stb.append("    SEQ ");

                ps = db2.prepareStatement(stb.toString());

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);

                    student._qualifiedList = new ArrayList();

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        student._qualifiedList.add(Student.rsToMap(rs));
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getRegdInfo(final Param param) {
            final String a;
            if ("1".equals(param._hrnameAttendnoOrSchregno)) {
                a = StringUtils.defaultString(_hrname) + " " + StringUtils.defaultString(_attendno);
            } else {
                a = _schregno;
            }
            return StringUtils.defaultString(_majorname) + " " + a;
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
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
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
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY"),
                                rs.getInt("TRANSFER_DATE")
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
        }
    }

    /**
     * 科目
     */
    private static class Subclass implements Comparable<Subclass> {
        final String _chaircd;
        final Map _semesterChaircdMap = new HashMap();
        final Map _semesterTakeSemesMap = new HashMap();
        final String _chairname;
        final SubclassMst _mst;
        final Map<String, Score> _scoreMap;
        final Map<String, SubclassAttendance> _attendMap;
        final String _credits;
        final String _minTakesemes;

        Subclass(
                final String chaircd,
                final String chaircdSeme1,
                final String chaircdSeme2,
                final String chaircdSeme3,
                final String chairname,
                final SubclassMst mst,
                final String credits,
                final String minTakesemes,
                final String takeSemesSeme1,
                final String takeSemesSeme2,
                final String takeSemesSeme3
        ) {
            _chaircd = chaircd;
            _semesterChaircdMap.put("1", chaircdSeme1);
            _semesterChaircdMap.put("2", chaircdSeme2);
            _semesterChaircdMap.put("3", chaircdSeme3);
            _semesterTakeSemesMap.put("1", takeSemesSeme1);
            _semesterTakeSemesMap.put("2", takeSemesSeme2);
            _semesterTakeSemesMap.put("3", takeSemesSeme3);
            _chairname = chairname;
            _mst = mst;
            _credits = credits;
            _scoreMap = new TreeMap();
            _attendMap = new TreeMap();
            _minTakesemes = minTakesemes;
        }

        public boolean isZenkiKamoku() {
            return "1".equals(_minTakesemes);
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

        public int compareTo(final Subclass o) {
            int rtn;
            if (null == _mst) {
                return 1;
            }
            if (null == o._mst) {
                return -1;
            }
            rtn = _mst.compareTo(o._mst);
            return rtn;
        }

        public String toString() {
            return "Subclass { chaircd: " + _chaircd + ", chairname: " + _chairname + ", mst = " + _mst + "} ";
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick) {
            _lesson = lesson;
            _attend = attend;
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

                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (null == student._subclassMap.get(subclasscd)) {
                                continue;
                            }

                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2);

                            final Subclass subClass = student.getSubclass(subclasscd);
                            subClass._attendMap.put(dateRange._key, subclassAttendance);
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
        final String _score;
        final String _assessmark;
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
        final String _slumpScore;
        final String _slumpScoreKansan;
        final String _provFlg;

        Score(
                final String score,
                final String assessmark,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank,
                final String karihyotei,
                final String replacemoto,
                final String slump,
                final String slumpMark,
                final String slumpScore,
                final String slumpScoreKansan,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
            _score = score;
            _assessmark = assessmark;
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
            _provFlg = provFlg;
        }

        public String toString() {
            return "Score(score=" + _score + ", getCredit=" + _getCredit + ")";
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public String getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public String getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1) {
                return false;
            }
            return true;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final StringBuffer stbtestcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param, stbtestcd);
                if (param._isOutputDebug) {
                    log.info(" record sql = " + sql);
                }
                log.info(" subclass query start. ");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" subclass query end.");

                while (rs.next()) {
                    final Student student = Student.getStudent(studentList, rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                            rs.getString("SCORE"),
                            rs.getString("ASSESSMARK"),
                            rs.getString("AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // rs.getString("KARI_HYOUTEI"),
                            rs.getString("REPLACEMOTO"),
                            null, // rs.getString("SLUMP"),
                            null, // rs.getString("SLUMP_MARK"),
                            null, // rs.getString("SLUMP_SCORE"),
                            null, // rs.getString("SLUMP_SCORE_KANSAN"),
                            rs.getString("COMP_CREDIT"),
                            rs.getString("GET_CREDIT"),
                            rs.getString("PROV_FLG")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        SubclassMst subclassMst = param.getSubclassMst(subclasscd);
                        if (null == subclassMst) {
                            subclassMst = new SubclassMst("", "", subclasscd, "", "", "", "", new Integer(999999), new Integer(999999), false, false, "");
                        }
                        final Subclass subClass = new Subclass(rs.getString("CHAIRCD"), rs.getString("CHAIRCD1"), rs.getString("CHAIRCD2"), rs.getString("CHAIRCD3"), rs.getString("CHAIRNAME"), subclassMst, rs.getString("CREDITS"), rs.getString("MIN_TAKESEMES"), rs.getString("TAKESEMES1"), rs.getString("TAKESEMES2"), rs.getString("TAKESEMES3"));
                        student._subclassMap.put(subclasscd, subClass);
                    }
                    if (null == testcd) {
                        if (null != rs.getString("GET_CREDIT") && null != rs.getString("ZOUKA")) {
                            final Subclass subClass = student.getSubclass(subclasscd);
                            subClass._scoreMap.put(TESTCD_GAKUNEN_HYOTEI, score);
                        }
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final Subclass subClass = student.getSubclass(subclasscd);
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
            stb.append(",CHAIRS AS(");
            stb.append("     SELECT DISTINCT W1.SCHREGNO, W1.SEMESTER, ");
            stb.append("        W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append("        W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        W2.CHAIRCD, W2.CHAIRNAME, W2.TAKESEMES ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     INNER JOIN SCHNO_A T1 ON T1.SCHREGNO = W1.SCHREGNO ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND W2.CLASSCD <= '90' ");
            stb.append("     )");

            stb.append(",CHAIR_A_SEME AS(");
            stb.append("     SELECT ");
            stb.append("       W1.SCHREGNO, ");
            stb.append("       W1.SUBCLASSCD, W2.SEMESTER, W2.CHAIRCD, W3.TAKESEMES ");
            stb.append("     FROM   (SELECT DISTINCT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM CHAIRS) W1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, SEMESTER, SUBCLASSCD, MAX(CHAIRCD) AS CHAIRCD ");
            stb.append("                 FROM CHAIRS ");
            stb.append("                 GROUP BY SCHREGNO, SEMESTER, SUBCLASSCD ");
            stb.append("                ) W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("     INNER JOIN CHAIRS W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("         AND W3.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("         AND W3.CHAIRCD = W2.CHAIRCD ");
            stb.append("         AND W3.SEMESTER = W2.SEMESTER ");
            stb.append("     INNER JOIN SCHNO_A W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("     )");

            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT DISTINCT ");
            stb.append("       W1.SCHREGNO, ");
            stb.append("       W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, W1.SUBCLASSCD, W2.CHAIRCD, W3.CHAIRNAME, TCRE.CREDITS, W2.MIN_TAKESEMES ");
            stb.append("     FROM   (SELECT DISTINCT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM CHAIRS) W1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, SUBCLASSCD, MAX(CHAIRCD) AS CHAIRCD, MIN(TAKESEMES) AS MIN_TAKESEMES ");
            stb.append("                 FROM CHAIRS ");
            stb.append("                 GROUP BY SCHREGNO, SUBCLASSCD ");
            stb.append("                ) W2 ON W2.SCHREGNO = W1.SCHREGNO AND W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("     INNER JOIN CHAIRS W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("         AND W3.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("         AND W3.CHAIRCD = W2.CHAIRCD ");
            stb.append("     INNER JOIN SCHNO_A W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST TCRE ON W4.YEAR = TCRE.YEAR ");
            stb.append("        AND W4.COURSECD = TCRE.COURSECD ");
            stb.append("        AND W4.GRADE = TCRE.GRADE ");
            stb.append("        AND W4.MAJORCD = TCRE.MAJORCD ");
            stb.append("        AND W4.COURSECODE = TCRE.COURSECODE ");
            stb.append("        AND W1.SUBCLASSCD = TCRE.CLASSCD || '-' || TCRE.SCHOOL_KIND || '-' || TCRE.CURRICULUM_CD || '-' || TCRE.SUBCLASSCD ");
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
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,CH1.CHAIRCD ");
            stb.append("    ,CH1.CHAIRNAME ");
            stb.append("    ,CH1.CREDITS ");
            stb.append("    ,CH1.MIN_TAKESEMES ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("      (SELECT L3.ASSESSMARK FROM RELATIVEASSESS_MST L3 ");
            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' AND L3.ASSESSLEVEL = W3.SCORE ");
            stb.append("           AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("           AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("           AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("           AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("      ) ELSE ");
            stb.append("      (SELECT L4.ASSESSMARK FROM ASSESS_MST L4 WHERE L4.ASSESSCD = '3' AND L4.ASSESSLEVEL = W3.SCORE) ");
            stb.append("     END AS ASSESSMARK ");
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
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
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
            stb.append("        AND CH1.SUBCLASSCD = W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("     AND (CH1.SUBCLASSCD IS NOT NULL OR W3.SUBCLASSCD = '999999') ");
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,CH1.CHAIRCD ");
            stb.append("    ,CH1.CHAIRNAME ");
            stb.append("    ,CH1.CREDITS ");
            stb.append("    ,CH1.MIN_TAKESEMES ");
            stb.append("    ,W3.COMP_CREDIT ");
            stb.append("    ,W3.GET_CREDIT ");
            stb.append("    ,W2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A T1 ON T1.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

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

            stb.append(", QUALIFIED AS(");
            stb.append("   SELECT ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("       VALUE(T2.SUBCLASSORDERNAME2, T2.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("       SUM(T1.CREDITS) AS CREDITS ");
            stb.append("   FROM ");
            stb.append("       SCHREG_QUALIFIED_DAT T1 ");
            stb.append("       LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' ");
            stb.append("       AND T1.CREDITS IS NOT NULL ");
            stb.append("       AND EXISTS (SELECT 'X' FROM SCHNO_A WHERE SCHREGNO = T1.SCHREGNO) ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD, ");
            stb.append("       VALUE(T2.SUBCLASSORDERNAME2, T2.SUBCLASSNAME) ");
            stb.append(" )");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, MIN_TAKESEMES FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, MIN_TAKESEMES FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, CHAIRCD, CHAIRNAME, CREDITS, MIN_TAKESEMES FROM RECORD_SCORE ");
            if (SEMEALL.equals(param._semester) && "1".equals(param._zouka)) {
                stb.append("    UNION ");
                stb.append("    SELECT SCHREGNO, SUBCLASSCD, CAST(NULL AS VARCHAR(1)), SUBCLASSNAME AS CHAIRNAME, CREDITS, CAST(NULL AS VARCHAR(1)) FROM QUALIFIED ");
            }
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T1.CHAIRCD ");
            stb.append("        ,T1.MIN_TAKESEMES ");
            stb.append("        ,CHSEME1.CHAIRCD AS CHAIRCD1 ");
            stb.append("        ,CHSEME2.CHAIRCD AS CHAIRCD2 ");
            stb.append("        ,CHSEME3.CHAIRCD AS CHAIRCD3 ");
            stb.append("        ,CHSEME1.TAKESEMES AS TAKESEMES1 ");
            stb.append("        ,CHSEME2.TAKESEMES AS TAKESEMES2 ");
            stb.append("        ,CHSEME3.TAKESEMES AS TAKESEMES3 ");
            stb.append("        ,T1.CHAIRNAME ");
            stb.append("        ,T1.CREDITS ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.ASSESSMARK ");
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

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
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

            stb.append("  LEFT JOIN CHAIR_A_SEME CHSEME1 ON CHSEME1.SCHREGNO = T1.SCHREGNO AND CHSEME1.SEMESTER = '1' AND CHSEME1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN CHAIR_A_SEME CHSEME2 ON CHSEME2.SCHREGNO = T1.SCHREGNO AND CHSEME2.SEMESTER = '2' AND CHSEME2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  LEFT JOIN CHAIR_A_SEME CHSEME3 ON CHSEME3.SCHREGNO = T1.SCHREGNO AND CHSEME3.SEMESTER = '3' AND CHSEME3.SUBCLASSCD = T1.SUBCLASSCD ");

            //成績不振科目データの表
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
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

    private static class TestItem {
        public String _semester;
        public String _testkindcd;
        public String _testitemcd;
        public String _scoreDiv;
        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _scoreDivName;
        public DateRange _dateRange;
        public boolean _printScore;
        public String testcd() {
            return _semester + _testkindcd + _testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + testcd() + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final SubclassMst o) {
            int rtn;
            rtn = _classShoworder3.compareTo(o._classShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classcd.compareTo(o._classcd);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassShoworder3.compareTo(o._subclassShoworder3);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclasscd.compareTo(o._subclasscd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private static class Form {

        String[] _testcds;
        TestItem[] _testItems;
        Map<String, DateRange> _attendRanges;
        Map<String, DateRange> _attendSubclassRanges;
        private Param _param;

        protected void initDebug() {
            for (int i = 0; i < _testcds.length; i++) {
                if (null != _testcds[i]) {
                    log.info(" testcds[" + i + "] = " + _testcds[i] + " : " + _testItems[i]);
                }
            }
            for (final String key : _attendRanges.keySet()) {
                log.info(" attendRanges[" + key + "] = " + _attendRanges.get(key));
            }
            for (final String key : _attendSubclassRanges.keySet()) {
                log.info(" attendSubclassRanges[" + key + "] = " + _attendSubclassRanges.get(key));
            }
        }

        private static int getMS932ByteLength(final String s) {
            return KNJ_EditEdit.getMS932ByteLength(s);
        }

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = "3".equals(param._knjSchoolMst._absentCov) || "4".equals(param._knjSchoolMst._absentCov) ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

        protected Map getAttendances(final Student student) {
            final Map attendances = new HashMap();
            for (final String key : _attendRanges.keySet()) {
                final DateRange dateRange = _attendRanges.get(key);
                if (null != dateRange) {
                    attendances.put(dateRange._key, student._attendMap.get(dateRange._key));
                }
            }
            return attendances;
        }

        protected Map getAttendanceRemarks(final Student student) {
            final Map remarks = new HashMap();
            for (final String key : _attendRanges.keySet()) {
                final DateRange dateRange = _attendRanges.get(key);
                if (null != dateRange) {
                    remarks.put(dateRange._key, student._attendRemarkMap.get(dateRange._key));
                }
            }
            return remarks;
        }

        protected TestItem[] getTestItems(
                final Param param,
                final String[] testcds
        ) {
            final TestItem[] testitems = new TestItem[testcds.length];
            try {
                for (final TestItem testitem : param._allTestItemList) {
                    int i = -1;
                    for (int j = 0; j < testcds.length; j++) {
                        if (null != testcds[j] && testcds[j].equals(testitem.testcd())) {
                            i = j;
                        }
                    }
                    if (-1 == i) {
                        continue;
                    }
                    testitems[i] = testitem;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            for (int i = 0; i < testcds.length; i++) {
                final String testcd = testcds[i];
                if (null == testitems[i] && null != testcd) {
                    log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + testcd);
                }
            }
            return testitems;
        }

        protected DateRange[] getSemesterDetails(
                final Param param,
                final int max
        ) {
            final DateRange[] semesterDetails = new DateRange[max];
            for (int j = 0, i = 0; i < param._allSemesterDetailList.size(); i++) {
                final DateRange sd = param._allSemesterDetailList.get(i);
                semesterDetails[j++] = sd;
                if (j >= max) {
                    break;
                }
            }
            return semesterDetails;
        }

        protected Map getTestItemMap() {
            final Map map = new HashMap();
            for (int i = 0; i < _testItems.length; i++) {
                if (null != _testItems[i]) {
                    map.put(_testItems[i].testcd(), _testItems[i]);
                }
            }
            return map;
        }

        public static String[] get_token(final String strx0, final int f_len) {
            final List<String> token = KNJ_EditKinsoku.getTokenList(strx0, f_len);
            if (token.size() == 0) {
                return new String[] {};
            }
            final String[] array = new String[token.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = (String) token.get(i);
            }
            return array;
        }

        public static String[] get_token(final String strx0, final int f_len, final int f_cnt) {
            final List<String> token = KNJ_EditKinsoku.getTokenList(strx0, f_len, f_cnt);
            if (token.size() == 0) {
                return new String[] {};
            }
            final String[] array = new String[token.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = (String) token.get(i);
            }
            return array;
        }

        protected void setTestcd(final List<String> testcdList, final int max, final String[] array) {
            log.info(" db testcdList = " + testcdList);
            if (testcdList.isEmpty()) {
                _testcds = array;
            } else {
                _testcds = new String[array.length];
                for (int i = 0; i < Math.min(testcdList.size(), max); i++) {
                    _testcds[i] = testcdList.get(i);
                }
                for (int i = max; i < array.length; i++) {
                    _testcds[i] = array[i];
                }
            }
        }

        public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
            return ipdf.setRecordString(field, gyo, data);
        }

        void init(final List<String> testcdList) {
            if (PATTERN_C.equals(_param._patternDiv)) {
                final int maxTest = 6;
                final String[] deftestcd = new String[maxTest * 2 + 1];
                deftestcd[deftestcd.length - 1] = TESTCD_GAKUNEN_HYOTEI;
                setTestcd(testcdList, maxTest * 2, deftestcd);
            } else if (_param.maxSemesterIs3()) {
                setTestcd(testcdList, 4, new String[] {"1990008", "2990008", null, TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
            } else {
                setTestcd(testcdList, 2, new String[] {"1990008", TESTCD_GAKUNEN_HYOKA, TESTCD_GAKUNEN_HYOTEI});
            }
            _testItems = getTestItems(_param, _testcds);
            _attendRanges = new HashMap();
            for (final String semester : _param._semesterMap.keySet()) {
                final Semester oSemester = _param._semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            _attendSubclassRanges = new HashMap();
            if (PATTERN_C.equals(_param._patternDiv)) {
                for (final String semester : _param._semesterMap.keySet()) {
                    final Semester oSemester = _param._semesterMap.get(semester);
                    _attendSubclassRanges.put(semester, oSemester._dateRange);
                }
            } else {
                for (int i = 0; i < _testItems.length; i++) {
                    final TestItem testitem = _testItems[i];
                    if (null == testitem) {
                        continue;
                    }
                    DateRange range = testitem._dateRange;
                    if (null != range && "99".equals(testitem._testkindcd)) {
                        final Semester semester = _param.getSemester(testitem._semester);
                        if (null != semester && null != semester._dateRange) {
                            range = semester._dateRange;
                        }
                    }
                    if (null != range) {
                        _attendSubclassRanges.put(testitem.testcd(), range);
                    }
                }
                for (final Map.Entry<String, Semester> e : _param._semesterMap.entrySet()) {
                    if (!SEMEALL.equals(e.getKey()) && null == _attendSubclassRanges.get(e.getKey())) {
                        _attendSubclassRanges.put(e.getKey(), _param.getSemester(e.getKey())._dateRange);
                    }
                }
                if (null != _param.getSemester(SEMEALL) && null != _param.getSemester(SEMEALL)._dateRange) {
                    _attendSubclassRanges.put(SEMEALL, _param.getSemester(SEMEALL)._dateRange);
                }
            }
            if (_param._isOutputDebug) {
                initDebug();
            }
        }

        void print(final IPdf ipdf, final DB2UDB db2, final Student student) {
            if (PATTERN_C.equals(_param._patternDiv)) {
                printC(ipdf, student);
            } else {
                printAB(ipdf, db2, student);
            }
        }

        private Object getItemPrintValue(final ColumnItem columnItem, final int lineItemKind, final LineItem lineItem, final String paramKekkaKey, final Subclass subclass) {
            if (null == subclass) {
                return null;
            }
            Object rtn = null;
            switch (lineItemKind) {
            case LINE_ITEM_TANNI:
                if (null == columnItem) {
                    rtn = subclass._credits;
                }
                break;
            case LINE_ITEM_SCORE:
                final Score s = subclass.getScore(lineItem._testItem.testcd());
                if (null == s) {
                    return null;
                }
                if (null == columnItem) {
                    if (TESTCD_GAKUNEN_HYOTEI.equals(lineItem._testItem.testcd())) {
                        rtn = StringUtils.defaultString(s._assessmark, s._score);
                    } else {
                        rtn = s._score;
                    }
                } else {
                    switch (columnItem._kind) {
                    case COLUMN_ITEM_TOTAL:
                        rtn = s._score;
                        break;
                    case COLUMN_ITEM_HR_AVG:
                        rtn = sishaGonyu(s._hrRank._avg);
                        break;
                    case COLUMN_ITEM_HR_RANK:
                        rtn = s._hrRank.getRank(_param);
                        break;
                    case COLUMN_ITEM_COURSE_AVG:
                        rtn = sishaGonyu(s._courseRank._avg);
                        break;
                    case COLUMN_ITEM_COURSE_RANK:
                        rtn = s._courseRank.getRank(_param);
                        break;
                    case COLUMN_ITEM_GRADE_RANK:
                        rtn = s._gradeRank.getRank(_param);
                        break;
                    }
                }
                if (_param._isOutputDebug) {
                    log.info(" score lineItem = " + lineItem + ", " + (null == lineItem || null == lineItem._testItem ? "" : lineItem._testItem.toString()) + " => " + rtn);
                }
                break;
            case LINE_ITEM_KEKKA:
                String kekkaKey = paramKekkaKey;
                if (_param._isOutputDebug) {
                    log.info(" lineItem = " + lineItem + ", " + (null == lineItem || null == lineItem._testItem ? "" : lineItem._testItem.toString()));
                }
                if (null == kekkaKey && null != lineItem) {
                    if (null != lineItem._testItem) {
                        if (lineItem._testItem.testcd().substring(1).startsWith("99")) {
                            kekkaKey = lineItem._testItem._semester;
                        } else {
                            kekkaKey = lineItem._testItem._dateRange._key;
                        }
                    } else {
                        kekkaKey = lineItem._semester;
                    }
                }
                if (_param._isOutputDebug) {
                    log.info(" kekkaKey = " + kekkaKey + " / " + subclass);
                }
                if (null == kekkaKey) {
                    return null;
                }
                final SubclassAttendance sa = subclass._attendMap.get(kekkaKey);
                if (null == sa) {
                    if (_param._isOutputDebug) {
                        log.info(" null at " + kekkaKey + " / " + subclass._attendMap);
                    }
                    return null;
                }
                rtn = kekkaString(_param, sa);
                break;
            }
            return rtn;
        }

        private List<LineItem> createLineItemList(final int max) {
            final List<LineItem> list = new ArrayList<LineItem>();
            list.add(new LineItem(LINE_ITEM_TANNI, null, null));
            TestItem last = null;
            final Set<String> kekkaSemester = new HashSet();
            kekkaSemester.add(SEMEALL);
            for (int i = 0; i < _testItems.length; i++) {
                if (null == _testItems[i]) {
                    continue;
                }
                if (null != last && null != last._semester && !last._semester.equals(_testItems[i]._semester)) {
                    for (final String cdSemester : _param._semesterMap.keySet()) {
                        if (last._semester.compareTo(cdSemester) <= 0 && cdSemester.compareTo(_testItems[i]._semester) < 0 && !kekkaSemester.contains(cdSemester)) {
                            list.add(new LineItem(LINE_ITEM_KEKKA, cdSemester, null));
                            kekkaSemester.add(cdSemester);
                        }
                    }
                }
                list.add(new LineItem(LINE_ITEM_SCORE, _testItems[i]._semester, _testItems[i]));
                last = _testItems[i];
            }
            if (null != last && null != last._semester) {
                for (final String cdSemester : _param._semesterMap.keySet()) {
                    if (last._semester.compareTo(cdSemester) <= 0 && !kekkaSemester.contains(cdSemester)) {
                        list.add(new LineItem(LINE_ITEM_KEKKA, cdSemester, null));
                        kekkaSemester.add(cdSemester);
                    }
                }
            }
            return list;
        }

        private Map<String, List<TestItem>> getSemesterTestitemListMap(final TestItem[] testitemList) {
            final Map<String, List<TestItem>> rtn = new HashMap();
            for (int i = 0; i < testitemList.length; i++) {
                if (null == testitemList[i]) {
                    continue;
                }
                getMappedList(rtn, testitemList[i]._semester).add(testitemList[i]);
            }
            return rtn;
        }

        // 空白の画像を表示して欄を非表示
        private void whitespace(final IPdf ipdf, final String field) {
            if (null != _param._whitespaceImagePath) {
                ipdf.VrImageOut(field, _param._whitespaceImagePath);
            }
        }

        private void printAB(final IPdf ipdf, final DB2UDB db2, final Student student) {
            final String form;
            if (PATTERN_A.equals(_param._patternDiv)) {
                // Aパターン
                if (_param.maxSemesterIs3()) {
                    form = "KNJD186W_1_2.frm";
                } else {
                    form = "KNJD186W_1_1.frm";
                }
            } else if (PATTERN_B.equals(_param._patternDiv)) {
                // Bパターン
                if (_param.maxSemesterIs3()) {
                    form = "KNJD186W_2_2.frm";
                } else {
                    form = "KNJD186W_2_1.frm";
                }
            } else {
                return;
            }
            log.info(" form = " + form);

            ipdf.VrSetForm(form, 4);
            ipdf.VrsOut("TITLE", _param._title); // タイトル
            ipdf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student.getRegdInfo(_param))); // 学校名
            ipdf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 30 ? "2" : "1"), student._name); // 名前

            ipdf.VrsOut("SOGO_SUBCLASSNAME", _param.getSogoSubclassname(student._curriculumYear));
            final String subtotal = student.getTotalGetCredit(_param);
            ipdf.VrsOut("SUBTOTAL_GET_CREDIT", subtotal); // 合計修得単位数
            if (SEMEALL.equals(_param._semester) || NumberUtils.isDigits(subtotal)) {
                if (NumberUtils.isDigits(subtotal) || student._previousCredits > 0) {
                    final String total = String.valueOf((NumberUtils.isDigits(subtotal) ? Integer.parseInt(subtotal) : 0) + student._previousCredits);
                    ipdf.VrsOut("TOTAL_GET_CREDIT", total); // 累計修得単位数
                }
            }
            ipdf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 校長名
            String teacher_all = student._staffname + ((student._staffname2 == null || "".equals(student._staffname2)) ? "" : "、" + student._staffname2);
            ipdf.VrsOut("TEACHER_NAME", teacher_all); // 担任名

            for (int j = 0; j < Integer.parseInt(_param._knjSchoolMst._semesterDiv) + 1; j++) {
                final int semesi = j + 1;
                if (_param.maxSemesterIs3() && 3 == j || !_param.maxSemesterIs3() && 2 == j) {
                    ipdf.VrsOutn("SEMESTER_NAME", semesi, "合計"); // 学期名称
                } else {
                    final Semester semester = _param.getSemester(String.valueOf(semesi));
                    if (null != semester) {
                        ipdf.VrsOutn("SEMESTER_NAME", semesi, semester._semestername); // 学期名称
                    }
                }
            }

            if ("1".equals(_param._isPrintspDisp)) {
                VrsOutnKurikaeshi(ipdf, "CLUB", get_token(student._club, 36, 4)); // 部活動
                VrsOutnKurikaeshi(ipdf, "COMMITTEE", get_token(student._committee, 36, 4)); // 委員会
            } else {
                whitespace(ipdf, "IMG_SP");
            }
            if ("1".equals(_param._isPrintRemark)) {
                VrsOutnKurikaeshi(ipdf, "REMARK", get_token(student._remark, 30, 15)); // 備考
            } else {
                whitespace(ipdf, "IMG_REMARK");
            }
            if ("1".equals(_param._isPrintTeikeiComment)) {
                VrsOutnKurikaeshi(ipdf, "COMMENT", get_token(student._footnote, 150, 3)); // コメント
            }

            if (PATTERN_A.equals(_param._patternDiv)) {
                // Aパターン
                if ("1".equals(_param._isPrintSogoKentei)) {
                    int moji = getParamSizeNum(_param._RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H, 0);
                    int gyo = getParamSizeNum(_param._RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H, 1);
                    if (-1 == moji || -1 == gyo) {
                        moji = 30;
                    }
                    gyo = 7;
                    final List totalstudyLineList = new ArrayList();
                    final String totalStudyact = student._semesterTotalStudyactMap.get(SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester);
                    if (null != totalStudyact) {
                        totalstudyLineList.add("＜学習内容＞");
                        totalstudyLineList.addAll(KNJ_EditKinsoku.getTokenList(totalStudyact, moji * 2));
                    }
                    final String totalStudytime = student._semesterTotalStudytimeMap.get(SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester);
                    if (null != totalStudytime) {
                        totalstudyLineList.add("＜評価＞");
                        totalstudyLineList.addAll(KNJ_EditKinsoku.getTokenList(totalStudytime, moji * 2));
                    }
                    final StringBuffer totalStudy = new StringBuffer();
                    for (final Iterator it = totalstudyLineList.iterator(); it.hasNext();) {
                        if (totalStudy.length() != 0) {
                            totalStudy.append("\n");
                        }
                        totalStudy.append(it.next());
                    }
                    VrsOutnKurikaeshi(ipdf, moji > 30 ? "TOTAL_ACT2" : "TOTAL_ACT", get_token(totalStudy.toString(), moji * 2, gyo)); // 総学
                } else {
                    whitespace(ipdf, "IMG_SOGOKENTEI");
                }
            } else if (PATTERN_B.equals(_param._patternDiv)) {
                // Bパターン
                if ("1".equals(_param._isPrintSogoKentei)) {
                    for (int j = 0; j < Math.min(8, student._qualifiedList.size()); j++) {
                        final Map qualified = student._qualifiedList.get(j);
                        final int line = j + 1;
                        ipdf.VrsOutn("APPROVAL_DATE", line, KNJ_EditDate.getAutoFormatDate(db2, KnjDbUtils.getString(qualified, "REGDDATE"))); // 検定年月日
                        ipdf.VrsOutn("APPROVAL_NAME", line, KnjDbUtils.getString(qualified, "QUALIFIED_NAME")); // 検定名称
                        ipdf.VrsOutn("APPROVAL_PASS", line, KnjDbUtils.getString(qualified, "RANK_NAME")); // 検定合格
                    }
                } else {
                    whitespace(ipdf, "IMG_SOGOKENTEI");
                }
            }

            final int itemLineMax;
            if (_param.maxSemesterIs3()) {
                itemLineMax = 8;
            } else {
                itemLineMax = 6;
            }
            final List<LineItem> lineItems = createLineItemList(itemLineMax);
            if (_param._isOutputDebug) {
                for (int i = 0; i < lineItems.size(); i++) {
                    log.info(" line item [" + i + "] = " + lineItems.get(i));
                }
            }

            for (int i = 0; i < _param._columnItemList.size(); i++) {
                final ColumnItem item = _param._columnItemList.get(i);
                ipdf.VrsOut("ITEM2_" + String.valueOf(i + 1), item._itemName); // 項目名
            }
            for (int j = 0; j < itemLineMax; j++) {
                if (j >= lineItems.size()) {
                    continue;
                }
                final LineItem lineItem = lineItems.get(j);
                final int line = j + 1;
                for (int i = 0; i < _param._columnItemList.size(); i++) {
                    final ColumnItem columnItem = _param._columnItemList.get(i);
                    ipdf.VrsOutn("SCORE2_" + String.valueOf(i + 1), line, (String) getItemPrintValue(columnItem, lineItem._kind, lineItem, null, student.getSubclass(SUBCLASSCD999999))); // 点数等
                }
            }

            for (int j = 0; j < itemLineMax; j++) {
                if (j >= lineItems.size()) {
                    continue;
                }
                final LineItem lineItem = lineItems.get(j);
                ipdf.VrsOutn("ITEM_NAME1", j + 1, lineItem.getLineName(_param)); // 得点/評価/欠課名
            }

            // 出欠記録
            for (final String semester : _param._semesterMap.keySet()) {
                final int line;
                if (SEMEALL.equals(semester)) {
                    line = _param._semesterMap.keySet().size();
                } else {
                    line = Integer.parseInt(semester);
                    if (line > Integer.parseInt(_param._semester)) {
                        continue;
                    }
                }
                final Attendance att = student._attendMap.get(semester);
                if (null != att) {
                    ipdf.VrsOutn("LESSON", line, getString(att._lesson)); // 授業日数
                    ipdf.VrsOutn("MOURNING", line, getString(att._suspend + att._mourning)); // 忌引出停日数
                    ipdf.VrsOutn("ABROAD", line, getString(att._abroad)); // 留学日数
                    ipdf.VrsOutn("MUST", line, getString(att._mLesson)); // 出席しなければならない日数
                    ipdf.VrsOutn("ABSENT", line, getString(att._absent)); // 欠席日数
                    ipdf.VrsOutn("ATTEND", line, getString(att._present)); // 欠席日数
                    ipdf.VrsOutn("LATE", line, getString(att._late)); // 遅刻
                    ipdf.VrsOutn("EARLY", line, getString(att._early)); // 早退
                }
            }

            final List<Subclass> subclassList = new ArrayList(student._subclassMap.values());
            for (final Iterator<Subclass> it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = it.next();
                if (_param._d026List.contains(subclass._mst._subclasscd)) {
                    if (_param._isOutputDebug) {
                        log.info(" not print subclass = " + subclass._mst._subclasscd);
                    }
                    it.remove();
                }
            }
            Collections.sort(subclassList);
            int line = 0;
            for (int i = 0; i < subclassList.size(); i++) {
                final Subclass subclass = subclassList.get(i);
                if (subclass == student.getSubclass(SUBCLASSCD999999)) {
                    continue;
                }
                if (_param._isOutputDebug) {
                    log.info(" subclass = " + subclass._mst._subclasscd + " / " + subclass._mst._subclassShoworder3 + " / " + subclass._mst._subclassname);
                }
                final String classname = "90".equals(subclass._mst._classcd) ? "　　　　　　　　　　A" : subclass._mst._classname;
                if (getMS932ByteLength(classname) <= 8) {
                    ipdf.VrsOut("CLASS_NAME1", classname); // 教科名
                    ipdf.VrsOut("CLASS_NAME2_1", subclass._mst._classcd);
                    ipdf.VrsOut("CLASS_NAME2_2", subclass._mst._classcd);
                    ipdf.VrAttribute("CLASS_NAME2_1", "X=10000");
                    ipdf.VrAttribute("CLASS_NAME2_2", "X=10000");
                } else {
                    final String[] token = KNJ_EditEdit.get_token(classname, 10, 2);
                    for (int j = 0; j < token.length; j++) {

                        ipdf.VrsOut("CLASS_NAME2_" + String.valueOf(j + 1), token[j]); // 教科名
                    }
                    ipdf.VrsOut("CLASS_NAME1", subclass._mst._classcd);
                    ipdf.VrAttribute("CLASS_NAME1", "X=10000");
                }
                String subclassname = null;
                if ("2".equals(_param._subclassnameOrChairname)) {
                    // 講座名
                    subclassname = subclass._chairname;
                } else if ("3".equals(_param._subclassnameOrChairname)) {
                    // 講座コード下3桁 + 講座名
                    final String keta3;
                    if (null == subclass._chaircd) {
                        keta3 = "   ";
                    } else {
                        final int len = subclass._chaircd.length();
                        final String spaces = StringUtils.repeat(" ", 3 - Math.min(3, len));
                        final int startIdx = Math.max(len - 3, 0);
                        keta3 = (subclass._chaircd.substring(startIdx, startIdx + Math.min(3, len))) + spaces;
                    }
                    subclassname = keta3 + " " + StringUtils.defaultString(subclass._chairname);
                } else if ("1".equals(_param._subclassnameOrChairname)) {
                    // 科目名
                    subclassname = subclass._mst._subclassname;
                }
                if (null != subclassname) {
                    if (subclassname.length() > 15) {
                        subclassname = subclassname.substring(0, 15);
                    }
                    if (subclassname.length() <= 10) {
                        ipdf.VrsOut("SUBCLASS_NAME1", subclassname); // 科目名
                    } else if (subclassname.length() <= 13) {
                        ipdf.VrsOut("SUBCLASS_NAME2", subclassname); // 科目名
                    } else {
                        ipdf.VrsOut("SUBCLASS_NAME3", subclassname); // 科目名
                    }
                }
                for (int j = 0; j < itemLineMax; j++) {
                    if (j >= lineItems.size()) {
                        continue;
                    }
                    final LineItem lineItem = lineItems.get(j);
                    if (_param._isOutputDebug) {
                        log.info(" print record " + lineItem);
                    }
                    final Object o = getItemPrintValue(null, lineItem._kind, lineItem, null, subclass);
                    if (o instanceof String) {
                        final String field = KNJ_EditEdit.getMS932ByteLength(o.toString()) > 3 ? "SCORE2" : "SCORE1";
                        ipdf.VrsOutn(field, j + 1, o.toString()); // 点数等
                    } else if (o instanceof Map) {
                        for (final Iterator it = ((Map) o).entrySet().iterator(); it.hasNext();) {
                            final Map.Entry e = (Map.Entry) it.next();
                            final String field = (String) e.getKey();
                            final String value = (String) e.getValue();
                            ipdf.VrsOutn("SCORE1" + field, j + 1, value); // 点数等
                        }
                    }
                }
                if (SEMEALL.equals(_param._semester) || subclass.isZenkiKamoku()) {
                    final Score score = subclass._scoreMap.get(TESTCD_GAKUNEN_HYOTEI);
                    if (null != score) {
                        ipdf.VrsOut("COMP_CREDIT", score._compCredit); // 履修単位数
                        ipdf.VrsOut("GET_CREDIT", score._getCredit); // 修得単位数
                    }
                    final Object o = getItemPrintValue(null, LINE_ITEM_KEKKA, null, SEMEALL, subclass);
                    if (o instanceof String) {
                        if (null != o) {
                            final String field = KNJ_EditEdit.getMS932ByteLength(o.toString()) > 3 ? "KEKKA2" : "KEKKA";
                            ipdf.VrsOut(field, o.toString()); // 欠課数
                        }
                    } else if (o instanceof Map) {
                        for (final Iterator it = ((Map) o).entrySet().iterator(); it.hasNext();) {
                            final Map.Entry e = (Map.Entry) it.next();
                            final String field = (String) e.getKey();
                            final String value = (String) e.getValue();
                            ipdf.VrsOut("KEKKA" + field, value);  // 欠課数
                        }
                    }
                }
                ipdf.VrEndRecord();
                line += 1;
            }
            int max = 20;
            if (0 == line || line % max != 0) {
                for (int i = line % max; i < max; i++) {
                    final String classname = "1";
                    ipdf.VrsOut("CLASS_NAME1", classname); // 教科名
                    ipdf.VrAttribute("CLASS_NAME1", "X=10000");
                    ipdf.VrEndRecord();
                }
            }
        }

        private void printC(final IPdf ipdf, final Student student) {
            final String form;
            form = "KNJD186W_3.frm";
            ipdf.VrSetForm(form, 4);
            log.info(" form = " + form);

            ipdf.VrsOut("TITLE", _param._title); // タイトル
            ipdf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(student.getRegdInfo(_param))); // 学校名
            ipdf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 30 ? "2" : "1"), student._name); // 名前

            for (int j = 0; j < Integer.parseInt(_param._knjSchoolMst._semesterDiv) + 1; j++) {
                final int semesi = j + 1;
                final String name;
                if (j == Integer.parseInt(_param._knjSchoolMst._semesterDiv)) {
                    name = "合計";
                } else {
                    name = _param.getSemester(String.valueOf(j + 1))._semestername;
                }
                ipdf.VrsOut("SEMESTER_NAME1_" + String.valueOf(semesi), name); // 学期名称
                ipdf.VrsOutn("SEMESTER_NAME3", semesi, name); // 学期名称
            }
            for (int j = 0; j < Integer.parseInt(_param._knjSchoolMst._semesterDiv) + 1; j++) {
                final int semesi = j + 1;
                final String name;
                if (j == Integer.parseInt(_param._knjSchoolMst._semesterDiv)) {
                    name = "通算";
                } else if (j == Integer.parseInt(_param._knjSchoolMst._semesterDiv) - 1) {
                    name = "年間合計";
                } else {
                    name = StringUtils.defaultString(_param.getSemester(String.valueOf(j + 1))._semestername) + "合計";
                }
                ipdf.VrsOutn("SEMESTER_NAME4", semesi, name); // 学期名称
            }

            ipdf.VrsOut("CHAIR_NAME1", "1".equals(_param._subclassnameOrChairname) ? "科目名" : "講座名"); // 講座名タイトル
            ipdf.VrsOut("CHAIR_NAME2", "1".equals(_param._subclassnameOrChairname) ? "科目名" : "講座名"); // 講座名タイトル

            ipdf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 校長名
            String teacher_all = student._staffname + ((student._staffname2 == null || "".equals(student._staffname2)) ? "" : "、" + student._staffname2);
            ipdf.VrsOut("TEACHER_NAME", teacher_all); // 担任名

            // 出欠記録
            for (final String semester : _param._semesterMap.keySet()) {
                final int line = SEMEALL.equals(semester) ? _param._semesterMap.keySet().size() : Integer.parseInt(semester);
                final Attendance att = student._attendMap.get(semester);
                if (null != att) {
                    ipdf.VrsOutn("LESSON", line, getString(att._lesson)); // 授業日数
                    ipdf.VrsOutn("MOURNING", line, getString(att._suspend + att._mourning)); // 忌引出停日数
                    ipdf.VrsOutn("ABROAD", line, getString(att._abroad)); // 留学日数
                    ipdf.VrsOutn("MUST", line, getString(att._mLesson)); // 出席しなければならない日数
                    ipdf.VrsOutn("ABSENT", line, getString(att._absent)); // 欠席日数
                    ipdf.VrsOutn("ATTEND", line, getString(att._present)); // 欠席日数
                    ipdf.VrsOutn("LATE", line, getString(att._late)); // 遅刻
                    ipdf.VrsOutn("EARLY", line, getString(att._early)); // 早退
                }
            }

            if ("1".equals(_param._isPrintRemark)) {
                VrsOutnKurikaeshi(ipdf, "REMARK", get_token(student._remark, 30, 15)); // 備考
            } else {
                whitespace(ipdf, "IMG_REMARK");
            }
            if ("1".equals(_param._isPrintTeikeiComment)) {
                VrsOutnKurikaeshi(ipdf, "COMMENT", get_token(student._footnote, 150, 3)); // コメント
            }

            final int kekkasuPosition = 8;
            for (int seme = 1; seme <= 2; seme++) {
                final int maxColumn = kekkasuPosition - 2;
                final List<TestItem> semeTestItemList = getMappedList(getSemesterTestitemListMap(_testItems), String.valueOf(seme));
                log.debug(" seme = " + seme + " / list = " + semeTestItemList);
                for (int j = 0; j < Math.min(semeTestItemList.size(), maxColumn); j++) {
                    final int line = j + 1;
                    final TestItem testItem = semeTestItemList.get(j);
                    ipdf.VrsOutn("ITEM_NAME" + String.valueOf(seme), line, testItem._testitemname); // テスト名
                }
                ipdf.VrsOutn("ITEM_NAME" + String.valueOf(seme), kekkasuPosition - 1, "学年評定"); // テスト名
                ipdf.VrsOutn("ITEM_NAME" + String.valueOf(seme), kekkasuPosition, "欠課数"); // テスト名
            }

            final List<Subclass> subclassList = new ArrayList(student._subclassMap.values());
            for (final Iterator<Subclass> it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = it.next();
                if (_param._d026List.contains(subclass._mst._subclasscd)) {
                    if (_param._isOutputDebug) {
                        log.info(" not print subclass = " + subclass._mst._subclasscd);
                    }
                    it.remove();
                }
            }
            Collections.sort(subclassList);
            final Map semesterSpecialDivTanniMap = getSemesterSpecialDivTanniMap(subclassList);
            for (int j = 0; j < Integer.parseInt(_param._knjSchoolMst._semesterDiv) + 1; j++) {
                final String semester;
                if (Integer.parseInt(_param._knjSchoolMst._semesterDiv) == j) {
                    // 合計
                    semester = "9";
                } else {
                    semester = String.valueOf(j + 1);
                }
                for (int specialDivi = 0; specialDivi <= 1; specialDivi++) {
                    final String specialDiv = String.valueOf(specialDivi);

                    final List getCreditList = getMappedList(getMappedMap(getMappedMap(semesterSpecialDivTanniMap, semester), specialDiv), "GET_CREDIT");
                    final List compCreditList = getMappedList(getMappedMap(getMappedMap(semesterSpecialDivTanniMap, semester), specialDiv), "COMP_CREDIT");

                    if (specialDivi == 0) {
                        ipdf.VrsOutn("GET_CREDIT3_1", j + 1, sum(getCreditList)); // 修得単位数 普通科目
                        ipdf.VrsOutn("COMP_CREDIT3_2", j + 1, sum(compCreditList)); // 履修単位数 普通科目
                    } else if (specialDivi == 1) {
                        ipdf.VrsOutn("GET_CREDIT2_1", j + 1, sum(getCreditList)); // 修得単位数 専門科目
                        ipdf.VrsOutn("COMP_CREDIT2_2", j + 1, sum(compCreditList)); // 履修単位数 専門科目
                    }
                }
            }

            for (int seme = 1; seme <= 2; seme++) { // 前期:上段 後期：下段
                final int maxSubclass = 13;

                final List<Subclass> semeSubclassList = new ArrayList();
                for (final Iterator<Subclass> prit = subclassList.iterator(); prit.hasNext();) {
                    final Subclass subclass = prit.next();
                    //log.debug(" subclass = " + subclass._mst._subclasscd + ", " + subclass._semesterChaircdMap);
                    if (null != subclass._semesterChaircdMap.get(String.valueOf(seme))) { // とりあえず指定学期に講座がある科目
                        if (("0".equals(subclass._minTakesemes) || String.valueOf(seme).equals(subclass._semesterTakeSemesMap.get(String.valueOf(seme))))) { // 履修学期
                            semeSubclassList.add(subclass);
                        }
                    }
                }

                int printline = 0;
                String oldclassname = "";
                for (int i = 0; i < Math.min(semeSubclassList.size(), maxSubclass); i++) {
                    final Subclass subclass = semeSubclassList.get(i);

                    if (null != subclass._mst) {
                        if (null != oldclassname && !oldclassname.equals(subclass._mst._classname)) {
                            ipdf.VrsOut("CLASS_NAME", subclass._mst._classname); // 教科名
                            oldclassname = subclass._mst._classname;
                        }
                        ipdf.VrsOut("SUBCLASS_NAME1", subclass._mst._subclassname); // 科目名
                    }
                    if (_param._isOutputDebug) {
                        log.info(" subclass " + subclass._mst._subclasscd + " (chair " + subclass._chaircd + ") zenki? " + subclass.isZenkiKamoku() + " hyotei = " + subclass.getScore(TESTCD_GAKUNEN_HYOTEI));
                    }


                    if ("0".equals(subclass._minTakesemes)) {
                        ipdf.VrsOut("SEMESTER_NAME2", "通年"); // 履修学期名称
                    } else {
                        Semester semester = _param._semesterMap.get(subclass._semesterTakeSemesMap.get(String.valueOf(seme)));
                        if (null != semester) {
                            ipdf.VrsOut("SEMESTER_NAME2", semester._semestername); // 履修学期名称
                        }
                    }
                    ipdf.VrsOut("CREDIT", subclass._credits); // 単位数

                    boolean isPrintHyoteiTanni = seme == 1 && subclass.isZenkiKamoku() || seme == 2 && !subclass.isZenkiKamoku();
                    final List<TestItem> semeTestItemList = getMappedList(getSemesterTestitemListMap(_testItems), String.valueOf(seme));
                    for (int j = 0; j < kekkasuPosition; j++) {
                        if (j == kekkasuPosition - 2) {
                            if (isPrintHyoteiTanni) {
                                final int line = j + 1;
                                final Score score = subclass._scoreMap.get(TESTCD_GAKUNEN_HYOTEI);
                                if (null != score) {
                                    ipdf.VrsOutn("SCORE1", line, StringUtils.defaultString(score._score, score._assessmark)); // 点数等
                                }
                            }
                            continue;
                        } else if (j == kekkasuPosition - 1) {
                            final SubclassAttendance sa = subclass._attendMap.get(String.valueOf(seme));
                            final Object o = kekkaString(_param, sa);
                            if (null != o) {
                                if (o instanceof String) {
                                    ipdf.VrsOut("KEKKA13", o.toString()); // 欠課数
                                } else if (o instanceof Map) {
                                    ipdf.VrsOut("KEKKA11", KnjDbUtils.getString((Map) o, "_BUNSI")); // 欠課数
                                    ipdf.VrsOut("KEKKA_SLASH2", "/"); // 欠課数
                                    ipdf.VrsOut("KEKKA12", KnjDbUtils.getString((Map) o, "_BUNBO")); // 欠課数
                                }
                            }
                            continue;
                        }
                        if (j >= semeTestItemList.size()) {
                            continue;
                        }
                        final int line = j + 1;
                        final TestItem testitem = semeTestItemList.get(j);
                        final Score score = subclass._scoreMap.get(testitem.testcd());
                        if (null != score) {
                            ipdf.VrsOutn("SCORE1", line, score._score); // 点数等
                        }
                    }

                    if (isPrintHyoteiTanni) {
                        final Score scoreGakunenmatsu = subclass._scoreMap.get(TESTCD_GAKUNEN_HYOTEI);
                        if (null != scoreGakunenmatsu) {
                            ipdf.VrsOut("COMP_CREDIT1", scoreGakunenmatsu._compCredit); // 履修単位数
                            ipdf.VrsOut("GET_CREDIT1", scoreGakunenmatsu._getCredit); // 修得単位数
                        }
                    }
                    final SubclassAttendance sa = subclass._attendMap.get(String.valueOf(seme == 2 ? 9 : seme));
                    final Object o = kekkaString(_param, sa);
                    if (null != o) {
                        if (o instanceof String) {
                            ipdf.VrsOut("KEKKA3", o.toString()); // 欠課数
                        } else if (o instanceof Map) {
                            ipdf.VrsOut("KEKKA1", KnjDbUtils.getString((Map) o, "_BUNSI")); // 欠課数
                            ipdf.VrsOut("KEKKA_SLASH", "/"); // 欠課数
                            ipdf.VrsOut("KEKKA2", KnjDbUtils.getString((Map) o, "_BUNBO")); // 欠課数
                        }
                    }
                    ipdf.VrEndRecord();
                    printline += 1;
                }
                for (;printline < maxSubclass; printline++) {
                    ipdf.VrEndRecord();
                }
            }
        }

        private String sum(final List creditList) {
            int sum = 0;
            boolean hasCredit = false;
            for (final Iterator it = creditList.iterator(); it.hasNext();) {
                final String credit = (String) it.next();
                if (!NumberUtils.isDigits(credit)) {
                    continue;
                }
                sum += Integer.parseInt(credit);
                hasCredit = true;
            }
            if (!hasCredit) {
                return null;
            }
            return String.valueOf(sum);
        }

        private Map getSemesterSpecialDivTanniMap(final List subclassList) {
            final Map rtn = new HashMap();
            for (int seme = 1; seme <= 2; seme++) { // 前期:上段 後期：下段

                final List semeSubclassList = new ArrayList();
                for (final Iterator prit = subclassList.iterator(); prit.hasNext();) {
                    final Subclass subclass = (Subclass) prit.next();
                    if (null != subclass._semesterChaircdMap.get(String.valueOf(seme))) { // とりあえず指定学期に講座がある科目
                        if (("0".equals(subclass._minTakesemes) || String.valueOf(seme).equals(subclass._semesterTakeSemesMap.get(String.valueOf(seme))))) { // 履修学期
                            semeSubclassList.add(subclass);
                        }
                    }
                }

                for (int i = 0; i < semeSubclassList.size(); i++) {
                    final Subclass subclass = (Subclass) semeSubclassList.get(i);
                    if (null == subclass._mst) {
                        continue;
                    }
                    if (subclass._mst._isMoto && "2".equals(subclass._mst._calculateCreditFlg)) {
                        // 加算タイプ
                        continue;
                    }

                    boolean isPrintTanni = seme == 1 && subclass.isZenkiKamoku() || seme == 2 && !subclass.isZenkiKamoku();
                    if (isPrintTanni) {
                        final Score scoreGakunenmatsu = subclass._scoreMap.get(TESTCD_GAKUNEN_HYOTEI);
                        if (null != scoreGakunenmatsu) {
                            final Map semeSpecialDivMap = getMappedMap(getMappedMap(rtn, String.valueOf(seme)), subclass._mst._specialDiv); // 指定学期
                            getMappedList(semeSpecialDivMap, "COMP_CREDIT").add(scoreGakunenmatsu._compCredit);
                            getMappedList(semeSpecialDivMap, "GET_CREDIT").add(scoreGakunenmatsu._getCredit);
                            final Map seme9SpecialDivMap = getMappedMap(getMappedMap(rtn, "9"), subclass._mst._specialDiv); // 年間
                            getMappedList(seme9SpecialDivMap, "COMP_CREDIT").add(scoreGakunenmatsu._compCredit);
                            getMappedList(seme9SpecialDivMap, "GET_CREDIT").add(scoreGakunenmatsu._getCredit);
                        }
                    }
                }
            }
            return rtn;
        }

        private static Object kekkaString(final Param param, final SubclassAttendance sa) {
            if (null == sa) {
                return null;
            }
            Object rtn = null;
            if ("1".equals(param._kekkaDisp)) {
                if (null != sa._sick) {
                    final DecimalFormat df = null != param._knjSchoolMst && ("3".equals(param._knjSchoolMst._absentCov) || "4".equals(param._knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");
                    rtn = df.format(sa._sick);
                }
            } else if ("2".equals(param._kekkaDisp)) {
                if (null != sa._sick || null != sa._lesson) {
                    final Map m = new HashMap();
                    String bunsi = null;
                    if (null != sa._sick) {
                        final DecimalFormat df = null != param._knjSchoolMst && ("3".equals(param._knjSchoolMst._absentCov) || "4".equals(param._knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");
                        bunsi = df.format(sa._sick);
                    }
                    m.put("_BUNSI", bunsi);
                    m.put("_SLASH", "/");
                    m.put("_BUNBO", null == sa._lesson ? null : sa._lesson.setScale(0, BigDecimal.ROUND_DOWN).toString());
                    rtn = m;
                }
            } else if ("3".equals(param._kekkaDisp)) {
                if (null != sa._lesson || null != sa._attend) {
                    String bunsi = null;
                    if (null != sa._sick) {
                        if (sa._attend.setScale(0, BigDecimal.ROUND_DOWN).equals(sa._attend)) {
                            bunsi = sa._attend.setScale(0, BigDecimal.ROUND_DOWN).toString();
                        } else {
                            bunsi = sa._attend.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        }
                    }
                    final Map m = new HashMap();
                    m.put("_BUNSI", bunsi);
                    m.put("_SLASH", "/");
                    m.put("_BUNBO", null == sa._lesson ? null : sa._lesson.setScale(0, BigDecimal.ROUND_DOWN).toString());
                    rtn = m;
                }
            }
            return rtn;
        }
    }

    private static class ColumnItem {
        final int _kind;
        final String _itemName;
        ColumnItem(final int kind, final String itemName) {
            _kind = kind;
            _itemName = itemName;
        }
        public String toString() {
            return "Column(" + _itemName + ")";
        }
    }

    private static class LineItem {
        final int _kind;
        final String _semester;
        final TestItem _testItem;
        LineItem(final int kind, final String semester, final TestItem testItem) {
            _kind = kind;
            _semester = semester;
            _testItem = testItem;
        }
        public String getLineName(final Param param) {
            switch (_kind) {
            case LINE_ITEM_TANNI:
                return "単位数";
            case LINE_ITEM_SCORE:
                return _testItem._testitemname;
            case LINE_ITEM_KEKKA:
                return StringUtils.defaultString((param._semesterMap.get(_semester))._semestername) + "欠課数";
            }
            return null;
        }
        public String toString() {
            switch (_kind) {
            case LINE_ITEM_TANNI:
                return "単位数";
            case LINE_ITEM_SCORE:
                return _testItem._testitemname;
            case LINE_ITEM_KEKKA:
                return StringUtils.defaultString(_semester) + "欠課数";
            }
            return null;
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _loginDate;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;

        final String _rankDiv; // 1:総合点 2:平均点
        final String _patternDiv; // 1:Aパターン 2:B 3:C
        final String _subclassnameOrChairname; // 1:科目名 2:講座名 3:講座番号（下3桁）付講座名
        final String _hrnameAttendnoOrSchregno; // 1:年組番号 2:学籍番号
        final String _kekkaDisp; // 1:欠課数 2:欠課数／時間数 3:出席数／時数
        final String _isPrintclassRank;
        final String _isPrintcourseRank;
        final String _isPrintgradeRank;
        final String _isPrintspDisp;
        final String _isPrintSogoKentei;
        final String _isPrintRemark;
        final String _isPrintTeikeiComment;
        final String _isPrintmirisyuDisp;
        final String _zouka; // 増加単位 1:加算する 2:加算しない
        final String _RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H;
        final String _documentroot;
        final String _imagepath;
        final String _gradeCd;
        final boolean _isOutputDebug;

        final String _schoolKind;

        final String _tutisyoPrintKariHyotei; // 仮評定を表示する

        final String _title;
        private final Form _form;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;
        final String _whitespaceImagePath;

        private Map<String, Semester> _semesterMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private Map _creditMst;
        private Map<String, List<String>> _recordMockOrderSdivDatMap;
        private List<TestItem> _allTestItemList;
        private List<DateRange> _allSemesterDetailList;

        private KNJSchoolMst _knjSchoolMst;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;
        private boolean _isNoPrintMoto;
        private List<String> _d026List = Collections.EMPTY_LIST;
        private List<ColumnItem> _columnItemList;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("LOGIN_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));

            _tutisyoPrintKariHyotei = null; // request.getParameter("tutisyoPrintKariHyotei");

            _patternDiv = request.getParameter("SEQ001");
            _subclassnameOrChairname = request.getParameter("SEQ002");
            _hrnameAttendnoOrSchregno = request.getParameter("SEQ003");
            _kekkaDisp = request.getParameter("SEQ004");
            _rankDiv = request.getParameter("SEQ005");
            _isPrintclassRank = request.getParameter("SEQ0061");
            _isPrintcourseRank = request.getParameter("SEQ0062");
            _isPrintgradeRank = request.getParameter("SEQ0063");
            _isPrintspDisp = request.getParameter("SEQ007");
            _isPrintSogoKentei = request.getParameter("SEQ008");
            _isPrintRemark = request.getParameter("SEQ009");
            _isPrintTeikeiComment = request.getParameter("SEQ010");
            _isPrintmirisyuDisp = request.getParameter("SEQ011");
            _zouka = request.getParameter("SEQ021");
            _RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H = request.getParameter("RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H");
            _documentroot = request.getParameter("DOCUMENTROOT");

            final Map gdat = getGdat(db2);
            _gradeCd = KnjDbUtils.getString(gdat, "GRADE_CD");
            _schoolKind = KnjDbUtils.getString(gdat, "SCHOOL_KIND");

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _form = new Form();
            _form._param = this;

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            setSubclassMst(db2);
            setRecordMockOrderSdivDat(db2);
            loadNameMstD016(db2);
            loadNameMstD026(db2);
            _allTestItemList = getAllTestItems(db2);
            _allSemesterDetailList = getSemesterDetails(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _title = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度　" + (StringUtils.defaultString(getSemester(_semester)._semestername)) + "　通知票";
            _columnItemList = new ArrayList();
            _columnItemList.add(new ColumnItem(COLUMN_ITEM_TOTAL, "総点"));
            if ("1".equals(_isPrintclassRank)) {
                _columnItemList.add(new ColumnItem(COLUMN_ITEM_HR_AVG, "学級平均"));
                _columnItemList.add(new ColumnItem(COLUMN_ITEM_HR_RANK, "学級順位"));
            }
            if ("1".equals(_isPrintcourseRank)) {
                _columnItemList.add(new ColumnItem(COLUMN_ITEM_COURSE_AVG, "コース平均"));
                _columnItemList.add(new ColumnItem(COLUMN_ITEM_COURSE_RANK, "コース順位"));
            }
            if ("1".equals(_isPrintgradeRank)) {
                _columnItemList.add(new ColumnItem(COLUMN_ITEM_GRADE_RANK, "学年順位"));
            }

            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));

            _whitespaceImagePath = getImageFilePath("whitespace.png");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186W' AND NAME = '" + propName + "' "));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        /**
         * 名称マスタ NAMECD1='H508' NAMECD2='02'読込
         */
        private String getH508Name1(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H508' AND NAMECD2 = '02' "));
        }


        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            _isNoPrintMoto = "Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
            final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
            sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List = new ArrayList();
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
            log.info("非表示科目:" + _d026List);
        }

        private Semester getSemester(final String semester) {
            return _semesterMap.get(semester);
        }

        private boolean maxSemesterIs3() {
            return "3".equals(_knjSchoolMst._semesterDiv);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            final Map<String, Semester> map = new TreeMap();
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

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                map.put(KnjDbUtils.getString(row, "SEMESTER"), new Semester(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SEMESTERNAME"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE")));
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolHrJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任");
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                return null;
            }
            return _subclassMstMap.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public List<String> getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
            final String[] keys = { grade + "-" + coursecd + "-" + majorcd
                                   , "00"  + "-" + coursecd + "-" + majorcd
                                   , grade + "-" + "0"      + "-" + "000"
                                   , "00"  + "-" + "0"      + "-" + "000"
                                   };
            for (int i = 0; i < keys.length; i++) {
                final List rtn = _recordMockOrderSdivDatMap.get(keys[i]);
                if (null != rtn) return rtn;
            }
            return Collections.EMPTY_LIST;
        }

        private void setRecordMockOrderSdivDat(final DB2UDB db2) {
            _recordMockOrderSdivDatMap = new HashMap();
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
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String key = KnjDbUtils.getString(row, "GRADE") + "-" + KnjDbUtils.getString(row, "COURSECD") + "-" + KnjDbUtils.getString(row, "MAJORCD");
                getMappedList(_recordMockOrderSdivDatMap, key).add(KnjDbUtils.getString(row, "TESTCD"));
            }
        }

        private List<TestItem> getAllTestItems(
                final DB2UDB db2
        ) {
            final List<TestItem> testitemList = new ArrayList<TestItem>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T1.SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV "
                                 +  " ,TESTITEMNAME "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
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
                    final TestItem testitem = new TestItem();
                    testitem._testkindcd = rs.getString("TESTKINDCD");
                    testitem._testitemcd = rs.getString("TESTITEMCD");
                    testitem._scoreDiv = rs.getString("SCORE_DIV");
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._sidouinput = rs.getString("SIDOU_INPUT");
                    testitem._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                    testitem._semester = rs.getString("SEMESTER");
                    testitem._dateRange = new DateRange(testitem.testcd(), testitem._testitemname, rs.getString("SDATE"), rs.getString("EDATE"));
                    testitem._printScore = "1".equals(rs.getString("PRINT"));
                    testitem._scoreDivName = rs.getString("SCORE_DIV_NAME");
                    testitemList.add(testitem);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemList;
        }

        private List<DateRange> getSemesterDetails(
                final DB2UDB db2
        ) {
            final List<DateRange> list = new ArrayList<DateRange>();
            final String sql = "SELECT T1.SEMESTER, T1.SEMESTERNAME, T1.SEMESTER_DETAIL "
                    + "  ,T1.SDATE "
                    + "  ,T1.EDATE "
                    + " FROM SEMESTER_DETAIL_MST T1 "
                    + " WHERE T1.YEAR = '" + _year + "' "
                    + " ORDER BY T1.SEMESTER_DETAIL ";

            log.debug(" sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                list.add(new DateRange(KnjDbUtils.getString(row, "SEMESTER_DETAIL"), KnjDbUtils.getString(row, "SEMESTERNAME"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE")));
            }
            return list;
        }

        private Map getGdat(final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        * ");
            stb.append("FROM    SCHREG_REGD_GDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + _year + "' ");
            stb.append(    "AND T1.GRADE = '" + _grade + "' ");

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
        }

        public String getRegdSemester() {
            return (SEMEALL.equals(_semester)) ? _ctrlSeme : _semester;
        }


        public String getSogoSubclassname(final String curriculumYear) {
            final int tankyuStartYear = 2019;
            boolean isTankyu = false;
            if (NumberUtils.isDigits(curriculumYear)) {
                isTankyu = tankyuStartYear <= Integer.parseInt(curriculumYear);
            } else {
                final int year = Integer.parseInt(_year);
                final int gradeCdInt = NumberUtils.isDigits(_gradeCd) ? Integer.parseInt(_gradeCd) : 99;
                if (year == tankyuStartYear     && gradeCdInt <= 1
                        || year == tankyuStartYear + 1 && gradeCdInt <= 2
                        || year == tankyuStartYear + 2 && gradeCdInt <= 3
                        || year >= tankyuStartYear + 3
                        ) {
                    isTankyu = true;
                }
            }
            if (_isOutputDebug) {
                log.info(" 探究? " + isTankyu);
            }
            return isTankyu ? "総合的な探究の時間" : "総合的な学習の時間";
        }
    }
}
