// kanji=漢字
/*
 * $Id: 4ebebc94dd1b4b7016c0f3f323bddf020a750a63 $
 */
package servletpack.KNJD;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186N {
    private static final Log log = LogFactory.getLog(KNJD186N.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String ITEM_DIV1_KOUSATOKUTEN_TSUCHIHYO = "1";
    private static final String ITEM_DIV2_SOTEN_TSUCHIHYO = "2";

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
        final List studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }
        load(param, db2, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);
            param._form.init(param.getRecordMockOrderSdivDat(student._grade, student._coursecd, student._majorcd));
            param._form.print(db2, ipdf, student);
        }
        _hasData = true;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static void loginfo(final Param param, final Object o) {
        if (param._isOutputDebug) {
            log.info(o);
        }
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List studentList0
    ) {
        Student.loadPreviousCredits(db2, param, studentList0);  // 前年度までの修得単位数取得

        final Form form = param._form;

        final Map courseStudentsMap = new HashMap();
        for (final Iterator it = studentList0.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
            getMappedList(courseStudentsMap, key).add(student);
        }

        for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final List studentList = (List) courseStudentsMap.get(key);
            final String[] split = StringUtils.split(key, "-");

            final List recordMockOrderSdivDatList = param.getRecordMockOrderSdivDat(split[0], split[1], split[2]);

            form.init(recordMockOrderSdivDatList);

            for (int i = 0; i < form._attendRanges.length; i++) {
                final DateRange range = form._attendRanges[i];
                Attendance.load(db2, param, studentList, range);
            }

            final List schoolkindNotJStudentList = new ArrayList();
            for (int i = 0; i < studentList.size(); i++) {
            	final Student student = (Student) studentList.get(i);
            	if (!"J".equals(student._schoolKind)) {
            		schoolkindNotJStudentList.add(student);
            	}
            }
            if (schoolkindNotJStudentList.size() > 0) {
            	for (int i = 0; i < form._attendSubclassRanges.length; i++) {
            		final DateRange range = form._attendSubclassRanges[i];
            		SubclassAttendance.load(db2, param, schoolkindNotJStudentList, range);
            	}
            }

            String testcdor = "";
            final StringBuffer stbtestcd = new StringBuffer();
            stbtestcd.append(" AND (");
            final List testcds = new ArrayList(form._testcds);
            if (SEMEALL.equals(param._semester)) {
                testcds.add(TESTCD_GAKUNEN_HYOTEI);
            }
            for (int i = 0; i < testcds.size(); i++) {
                final String testcd = (String) testcds.get(i);
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

            Address.setAddress(db2, studentList, param);
            Student.setHreportremarkCommunication(param, db2, studentList);
        }
    }

    private static Student getStudent(final List studentList, final String code) {
        if (code == null) {
            return null;
        }
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
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

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _schoolKind;
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
        final Map _attendRemarkMap;
        final Map _subclassMap;
        final String _entyear;
        private Address _address;
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private String _communication;

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String schoolKind, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _schoolKind = schoolKind;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
            _attendMap = new TreeMap();
            _attendRemarkMap = new TreeMap();
            _subclassMap = new TreeMap();
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
            return (SubClass) _subclassMap.get(subclasscd);
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
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  W1.SCHREGNO");
            stb.append("            ,W1.SEMESTER ");
            stb.append("            ,W7.NAME ");
            stb.append("            ,W6.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,W1.ATTENDNO ");
            stb.append("            ,W1.GRADE ");
            stb.append("            ,GDAT.SCHOOL_KIND ");
            stb.append("            ,W1.COURSECD ");
            stb.append("            ,W1.MAJORCD ");
            stb.append("            ,W1.COURSECD || W1.MAJORCD || W1.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W6.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(W7.ENT_DATE) AS ENT_YEAR ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST W7 ON W7.SCHREGNO = W1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
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

            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final String schoolKind = rs.getString("SCHOOL_KIND");
					students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), schoolKind, rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR")));
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
                final List studentList
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            final String sql = stb.toString();

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
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
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
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
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
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
            final List list = new ArrayList();
            boolean hasNotNullSubclassScore = false;
            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
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

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT COMMUNICATION ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("   AND SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
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
        public static void setAddress(final DB2UDB db2, final List studentList, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" SELECT T0.SCHREGNO, T2.GUARD_NAME AS ADDRESSEE, T5.GUARD_NAME AS ADDRESSEE2, T4.GUARD_ADDR1 AS ADDR1, T4.GUARD_ADDR2 AS ADDR2, T4.GUARD_ZIPCD AS ZIPCD ");
                stb.append(" FROM SCHREG_BASE_MST T0 ");
                stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T0.SCHREGNO ");
                stb.append(" LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT WHERE '" + param._date + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') GROUP BY SCHREGNO) T3 ON ");
                stb.append("     T3.SCHREGNO = T0.SCHREGNO  ");
                stb.append(" LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.ISSUEDATE = T3.ISSUEDATE ");
                stb.append(" LEFT JOIN GUARDIAN_HIST_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO AND '" + param._date + "' BETWEEN T5.ISSUEDATE AND T5.EXPIREDATE ");
                stb.append(" WHERE ");
                stb.append("     T0.SCHREGNO = ? ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    if (rs.next()) {
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

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _abroad;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _choreiketsu;
        final int _taireiketsu;
        final int _kekka;
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
                final int early,
                final int choreiketsu,
                final int taireiketsu,
                final int kekka
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
            _choreiketsu = choreiketsu;
            _taireiketsu = taireiketsu;
            _kekka = kekka;
        }

        public Attendance add(final Attendance att) {
        	return new Attendance(
        			_lesson + att._lesson,
        			_mLesson + att._mLesson,
        			_suspend + att._suspend,
        			_mourning + att._mourning,
        			_abroad + att._abroad,
        			_absent + att._absent,
        			_present + att._present,
        			_late + att._late,
        			_early + att._early,
        			_choreiketsu + att._choreiketsu,
        			_taireiketsu + att._taireiketsu,
        			_kekka + att._kekka
        			);
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
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
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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
                                rs.getInt("EARLY"),
                                rs.getInt("REIHAI_KEKKA"), // SEQ001
                                rs.getInt("M_KEKKA_JISU"),  // SEQ002
                                rs.getInt("REIHAI_TIKOKU") // SEQ003
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

        private static void loadRemark(final DB2UDB db2, final Param param, final String attendSemesInState, final List studentList, final DateRange dateRange) {
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
                stb.append(" ORDER BY T1.MONTH, T1.SEMESTER ");

                //log.debug(" dateRange = " + dateRange + " /  remark sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    String comma = "";
                    final StringBuffer remark = new StringBuffer();
                    while (rs.next()) {
                        remark.append(comma).append(rs.getString("REMARK1"));
                        comma = "、";
                    }
                    if (remark.length() != 0) {
                        student._attendRemarkMap.put(dateRange._key, remark.toString());
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
    private static class SubClass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        final Map _attendMap;

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
            return (Score) _scoreMap.get(testcd);
        }

        public SubclassAttendance getAttendance(final String key) {
            if (null == key) {
                return null;
            }
            return (SubclassAttendance) _attendMap.get(key);
        }

        public int compareTo(final Object o) {
            final SubClass subclass = (SubClass) o;
            return _mst.compareTo(subclass._mst);
        }

        public String toString() {
            return "SubClass(" + _mst.toString() + ", score = " + _scoreMap + ")";
        }
    }

    private static BigDecimal getKekka(final SubclassAttendance att, final DateRange dr, final Param param) {
		BigDecimal kekka = SubclassAttendance.add(att._sick1, SubclassAttendance.divide(SubclassAttendance.add(att._late, att._early), new BigDecimal(2)));
		if (param._isOutputDebug) {
			log.info(" " + dr + " : sick1 = " + att._sick1 + ", late = " + att._late + ", early = " + att._early + " => kekka = " + kekka);
		}
		return kekka;
	}

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _sick1;
        final BigDecimal _late;
        final BigDecimal _early;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal sick1, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _sick1 = sick1;
            _late = late;
            _early = early;
        }

        private static BigDecimal add(final BigDecimal a, final BigDecimal b) {
        	return null == a ? b : null == b ? a : a.add(b);
        }

        private static BigDecimal divide(final BigDecimal a, final BigDecimal b) {
        	return null == a || null == b ? null : a.divide(b, 5, BigDecimal.ROUND_HALF_UP);
        }

        public SubclassAttendance add(final SubclassAttendance att) {
        	return new SubclassAttendance(add(_lesson, att._lesson), add(_sick1, att._sick1), add(_late, att._late), add(_early, att._early));
        }

        public String toString() {
            return "SubclassAttendance(" + (_sick1 == null ? null : sishaGonyu(_sick1.toString())) + "/" + _lesson + ")";
        }

        private static void createSemeAllAttendance(final Student student) {
            for (final Iterator sbit = student._subclassMap.keySet().iterator(); sbit.hasNext();) {
            	final String subclasscd = (String) sbit.next();
                final SubClass subClass = (SubClass) student._subclassMap.get(subclasscd);

                SubclassAttendance total = new SubclassAttendance(null, null, null, null);
                for (final Iterator attit = subClass._attendMap.keySet().iterator(); attit.hasNext();) {
                	final String key = (String) attit.next();
                    final SubclassAttendance att = (SubclassAttendance) subClass._attendMap.get(key);
                    total = total.add(att);
                }
                subClass._attendMap.put(SEMEALL, total);
            }
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange0) {
            log.info(" subclass attendance dateRange = " + dateRange0);
            if (null == dateRange0) {
            	return;
            }
        	if (SEMEALL.equals(dateRange0._key)) {
                return;
        	}
        	final List dateRanges;
        	if (!dateRange0._childrenDateRangeList.isEmpty()) {
        		dateRanges = dateRange0._childrenDateRangeList;
        	} else {
        		dateRanges = Collections.singletonList(dateRange0);
        	}

        	for (final Iterator it = dateRanges.iterator(); it.hasNext();) {
        		final DateRange dateRange = (DateRange) it.next();
        		if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
        			continue;
        		}
        		final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
        		PreparedStatement ps = null;
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

        			final BigDecimal zero = new BigDecimal(0);
        			for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
        				final Student student = (Student) stit.next();

        				final List rowList = KnjDbUtils.query(db2, ps, new Object[] {student._schregno});

        				for (final Iterator rit = rowList.iterator(); rit.hasNext();) {
        					final Map rs = (Map) rit.next();
        					if (!SEMEALL.equals(KnjDbUtils.getString(rs, "SEMESTER"))) {
        						continue;
        					}
        					final String subclasscd = KnjDbUtils.getString(rs, "SUBCLASSCD");

        					final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
        					if (null == mst) {
        						continue;
        					}
        					final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
        					if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
        						final BigDecimal lesson = KnjDbUtils.getBigDecimal(rs, "MLESSON", zero);
        						final BigDecimal late = KnjDbUtils.getBigDecimal(rs, "LATE", zero);
        						final BigDecimal early = KnjDbUtils.getBigDecimal(rs, "EARLY", zero);
        						final BigDecimal sick1 = KnjDbUtils.getBigDecimal(rs, "SICK1", zero);
        						final BigDecimal rawReplacedSick = KnjDbUtils.getBigDecimal(rs, "RAW_REPLACED_SICK", zero);

        						final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, mst._isSaki ? rawReplacedSick : sick1, late, early);

        						if (null == student._subclassMap.get(subclasscd)) {
        							final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
        							student._subclassMap.put(subclasscd, subClass);
        						}
        						final SubClass subClass = student.getSubClass(subclasscd);
        						subClass._attendMap.put(dateRange._key, subclassAttendance);
        					}
        				}
        			}

        		} catch (Exception e) {
        			log.fatal("exception!", e);
        		} finally {
        			DbUtils.closeQuietly(ps);
        			db2.commit();
        		}
        	}
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
                final String provFlg
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
            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1) {
                return false;
            }
            return true;
        }

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
        	if (null != testItem._sidouinput) {; // if (null != testItem._sidouinput && !"1".equals(param._kettenKamokuNoSubtract)) {
                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
                    if (null != _slumpMark) {
                        if (null != param._d054Namecd2Max && param._d054Namecd2Max.equals(_slumpMark)) {
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
            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
                return "1".equals(_score);
            }
            return "1".equals(_assessLevel);
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final StringBuffer stbtestcd
        ) {
            try {
                final String sql = sqlScore(param, stbtestcd);

                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map rs = (Map) it.next();
                    final Student student = getStudent(studentList, KnjDbUtils.getString(rs, "SCHREGNO"));
                    final String testcd = KnjDbUtils.getString(rs, "TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Rank gradeRank = new Rank(KnjDbUtils.getString(rs, "GRADE_RANK"), KnjDbUtils.getString(rs, "GRADE_AVG_RANK"), KnjDbUtils.getString(rs, "GRADE_COUNT"), KnjDbUtils.getString(rs, "GRADE_AVG"), KnjDbUtils.getString(rs, "GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(KnjDbUtils.getString(rs, "CLASS_RANK"), KnjDbUtils.getString(rs, "CLASS_AVG_RANK"), KnjDbUtils.getString(rs, "HR_COUNT"), KnjDbUtils.getString(rs, "HR_AVG"), KnjDbUtils.getString(rs, "HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(KnjDbUtils.getString(rs, "COURSE_RANK"), KnjDbUtils.getString(rs, "COURSE_AVG_RANK"), KnjDbUtils.getString(rs, "COURSE_COUNT"), KnjDbUtils.getString(rs, "COURSE_AVG"), KnjDbUtils.getString(rs, "COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(KnjDbUtils.getString(rs, "MAJOR_RANK"), KnjDbUtils.getString(rs, "MAJOR_AVG_RANK"), KnjDbUtils.getString(rs, "MAJOR_COUNT"), KnjDbUtils.getString(rs, "MAJOR_AVG"), KnjDbUtils.getString(rs, "MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                            testcd,
                            KnjDbUtils.getString(rs, "SCORE"),
                            KnjDbUtils.getString(rs, "ASSESS_LEVEL"),
                            KnjDbUtils.getString(rs, "AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // KnjDbUtils.getString(rs, "KARI_HYOUTEI"),
                            KnjDbUtils.getString(rs, "REPLACEMOTO"),
                            KnjDbUtils.getString(rs, "SLUMP"),
                            KnjDbUtils.getString(rs, "SLUMP_MARK"),
                            KnjDbUtils.getString(rs, "SLUMP_MARK_NAME1"),
                            KnjDbUtils.getString(rs, "SLUMP_SCORE"),
                            KnjDbUtils.getString(rs, "SLUMP_SCORE_KANSAN"),
                            KnjDbUtils.getString(rs, "COMP_CREDIT"),
                            KnjDbUtils.getString(rs, "GET_CREDIT"),
                            KnjDbUtils.getString(rs, "PROV_FLG")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(KnjDbUtils.getString(rs, "SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = KnjDbUtils.getString(rs, "SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final SubClass subClass = new SubClass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subClass);
                    }
                    if (null == testcd) {
                        continue;
                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final SubClass subClass = student.getSubClass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
                }
            } catch (Exception e) {
                log.error("Exception", e);
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

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SCORE ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
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
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.PROV_FLG ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_MARK_NAME1 ");
            stb.append("        ,K1.SLUMP_SCORE ");
            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");

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
        private List _childrenDateRangeList = new ArrayList();
        public DateRange(final String key, final String semester, final String name, final String sdate, final String edate) {
            _key = key;
            _semester = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
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

    private static class SubclassMst implements Comparable {
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
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
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

    private static class Form {

        List _testcds;
        List _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendSubclassRanges;
//        int _formTestCount; // フォームに表示する考査の回数
//        boolean _use5testForm; // 5回考査用フォームを使用するか
//        TestItem _notPrintTestItem;
//        DateRange _notPrintDateRange;
        private Param _param;
        String _currentForm;
        Map _fieldInfoMap = Collections.EMPTY_MAP;

        protected Param param() {
            return _param;
        }

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
//                log.info(" _formTestCount = " + _formTestCount);
//                log.info(" _notPrintTestItem = " + _notPrintTestItem);
//                log.info(" _notPrintDateRange = " + _notPrintDateRange);
            }
        }

        private static int getMS932ByteLength(final String s) {
        	return KNJ_EditEdit.getMS932ByteLength(s);
        }

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static BigDecimal kirisute(final BigDecimal bd) {
        	if (null == bd) {
        		return bd;
        	}
        	return bd.setScale(0, BigDecimal.ROUND_DOWN); // 端数切捨て
        }

        protected Attendance[] getAttendances(final Student student) {
            final Attendance[] attendances = new Attendance[_attendRanges.length];
            Attendance sum = null;
            for (int i = 0; i < _attendRanges.length; i++) {
                final DateRange dateRange = (DateRange) _attendRanges[i];
                if (null != dateRange) {
                    if (SEMEALL.equals(dateRange._key)) {
                        attendances[i] = sum;
                    } else {
                    	attendances[i] = (Attendance) student._attendMap.get(dateRange._key);
                    	if (null != attendances[i]) {
                    		if (null == sum) {
                    			sum = new Attendance(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                    		}
                    		sum = sum.add(attendances[i]);
                    	}
                    }
                    if (null != attendances[i]) {
                        attendances[i]._dateRange = dateRange;
                    }
                }
            }
            return attendances;
        }

        protected String[] getAttendanceRemarks(final Student student) {
            final String[] remarks = new String[_attendRanges.length];
            for (int i = 0; i < _attendRanges.length; i++) {
                final DateRange dateRange = (DateRange) _attendRanges[i];
                if (null != dateRange) {
                    if (SEMEALL.equals(dateRange._key)) {
                        continue;
                    }
                    remarks[i] = (String) student._attendRemarkMap.get(dateRange._key);
                }
            }
            return remarks;
        }

        protected List getTestItems(
                final Param param,
                final List testcds
        ) {
            final List testitems = new ArrayList();
            for (int i = 0; i < testcds.size(); i++) {
                testitems.add(null);
            }
            for (int j = 0; j < testcds.size(); j++) {
                testitems.set(j, (TestItem) param._testitemMap.get(testcds.get(j)));
            }
            final List notFoundTestcds = new ArrayList();
            for (int i = 0; i < testcds.size(); i++) {
                final String testcd = (String) testcds.get(i);
                if (null == testitems.get(i)) {
                    notFoundTestcds.add(testcd);
                }
            }
            if (!notFoundTestcds.isEmpty()) {
                log.warn("TESTITEM_MST_COUNTFLG_NEW_SDIVがない: " + notFoundTestcds + " / 実際のマスタのコード:" + param._testitemMap.keySet());
            }
            return testitems;
        }

        protected DateRange[] getSemesterDetails(
                final Param param,
                final int max
        ) {
            final DateRange[] semesterDetails = new DateRange[max];
            for (int i = 0; i < Math.min(max, param._semesterDetailList.size()); i++) {
                semesterDetails[i] = (DateRange) param._semesterDetailList.get(i);
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
        public static List getTokenList(final String source, final int bytePerLine) {
            if (source == null || source.length() == 0) {
                return Collections.EMPTY_LIST;
            }
            final List tokenList = new ArrayList();        //分割後の文字列の配列
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

        protected List setTestcd3(final List testcdList, final String[] array, int min) {
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
            while (testcds.size() < min) {
                testcds.add(null);
            }
            testcds.add(TESTCD_GAKUNEN_HYOKA);
            testcds.add(TESTCD_GAKUNEN_HYOTEI);
            return testcds;
        }

        // 学年評価、学年評定を除いた表記する考査の数
        protected Collection getTestItemSet(final List testItemList) {
            final Map map = new TreeMap();
            for (final Iterator it = testItemList.iterator(); it.hasNext();) {
                final TestItem ti = (TestItem) it.next();
                if (null == ti) { //if (null == ti || "1".equals(param()._notPrintLastExam) && _notPrintTestItem == ti) {
                    continue;
                }
                map.put(ti._testcd, ti);
            }
            map.remove(TESTCD_GAKUNEN_HYOKA);
            map.remove(TESTCD_GAKUNEN_HYOTEI);
            return map.values();
        }

        public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
//            if (Param._isDemo) {
//                return ipdf.VrsOutn(field, gyo, data);
//            }
            return ipdf.setRecordString(field, gyo, data);
        }

        protected Rank getGroupDivRank(final Score score) {
            return "4".equals(param()._groupDiv) ? score._majorRank : "3".equals(param()._groupDiv) ? score._courseRank : score._gradeRank;
        }

        protected String getGroupDivName() {
            return "4".equals(param()._groupDiv) ? "学科順位" : "3".equals(param()._groupDiv) ? "コース順位" : "学年順位";
        }

        protected List getPrintSubclassList(final Student student, final int max) {
            List printSubclassList = new ArrayList();
            for (final Iterator it = student._subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final SubClass subClass = student.getSubClass(subclasscd);
                if (SUBCLASSCD999999.equals(subclasscd)) {
                    continue;
                }
                if (null == subClass || param()._isNoPrintMoto && subClass._mst._isMoto) {
                    continue;
                }
                printSubclassList.add(subClass);
            }
            if (printSubclassList.size() > max) {
            	log.warn(" printSubclassList size = " + printSubclassList.size() + ", max = " + max + ", rest = " + printSubclassList.subList(max, printSubclassList.size()));
            	printSubclassList = printSubclassList.subList(0, max);
            }
            Collections.sort(printSubclassList);
            return printSubclassList;
        }

        protected TestItem testItem(final int i) {
            return (TestItem) _testItems.get(i);
        }

        public void setForm(final IPdf ipdf, final String form) {
            log.info(" form = " + form);
            int rtn = ipdf.VrSetForm(form, 4);
            if (rtn < 0) {
                throw new IllegalArgumentException("フォーム設定エラー:" + rtn);
            }
            if (null == _currentForm || !_currentForm.equals(form)) {
            	_currentForm = form;
            	if (ipdf instanceof SvfPdf) {
            		SvfPdf svfpdf = (SvfPdf) ipdf;
            		_fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(svfpdf.getVrw32alp());
            	}
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


        int _attendSemeAllPos;

        void init(final List testcdList) {
            final List _attendTestcds1 = setTestcd3(testcdList, new String[] {"1010101", "1020101", "2010101", "2020101", "3020101"}, 5);
            final List _attendTestItems1 = getTestItems(param(), _attendTestcds1);
            if (ITEM_DIV1_KOUSATOKUTEN_TSUCHIHYO.equals(param()._itemDiv)) {
                _testcds = _attendTestcds1;
                _testItems = _attendTestItems1;

                _attendRanges = new DateRange[_testItems.size() + 1];
                _attendSubclassRanges = new DateRange[_testItems.size()];
                for (int i = 0, attendRangeIdx = 0; i < _testItems.size() - 1; i++) {
                    final TestItem item = testItem(i);
                    if (null != item) {
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
                	_attendRanges[_attendRanges.length - 1] = new DateRange(seme9._semester, seme9._semester, "", seme9._dateRange._sdate, seme9._dateRange._edate);
                }

            } else if (ITEM_DIV2_SOTEN_TSUCHIHYO.equals(param()._itemDiv)) {
                _testcds = setTestcd3(testcdList, new String[] {"1990008", "2990008", "3990008"}, 3);
                _testItems = getTestItems(param(), _testcds);

                _attendRanges = new DateRange[param()._semesterMap.size()];
                _attendSubclassRanges = new DateRange[param()._semesterMap.size()];
                final int last = param()._semesterMap.size() - 1;
                int attendRangeIdx = 0;
                for (final Iterator it = param()._semesterMap.values().iterator(); it.hasNext();) {
                	final Semester seme = (Semester) it.next();
                    if (null != seme) {
                        final DateRange dr = new DateRange(seme._semester, seme._semester, seme._semestername, seme._dateRange._sdate, seme._dateRange._edate);
                        if (!SEMEALL.equals(seme._semester)) {
                        	// 学期内のテストの出欠の合計値
                        	final List childDateRange = new ArrayList();
                        	for (final Iterator tit = _attendTestItems1.iterator(); tit.hasNext();) {
                        		final TestItem attendTestItem = (TestItem) tit.next();
                        		if (null != attendTestItem) {
                        			if (seme._semester.equals(attendTestItem._semester)) {
                        				if (null != attendTestItem._dateRange) {
                        					childDateRange.add(attendTestItem._dateRange);
                        				}
                        			}
                        		}
                        	}
                        	dr._childrenDateRangeList = childDateRange;
                        }
                        _attendRanges[attendRangeIdx] = dr;
                        _attendSubclassRanges[attendRangeIdx] = dr;
                        attendRangeIdx += 1;
                    }
                }
                if (null != param().getSemester(SEMEALL)) {
                	final Semester seme9 = param().getSemester(SEMEALL);
                	_attendRanges[last] = new DateRange(seme9._semester, seme9._semester, "", seme9._dateRange._sdate, seme9._dateRange._edate);
                	_attendSubclassRanges[last] = _attendRanges[last];
                }
            }
            initDebug();
        }

        void print(final DB2UDB db2, final IPdf ipdf, final Student student) {
            String _form = null;
            loginfo(param(), " testItems = " + _testItems);
            if (ITEM_DIV1_KOUSATOKUTEN_TSUCHIHYO.equals(param()._itemDiv)) {
            	// 考査得点通知票
            	_attendSemeAllPos = 6;
            	if ("J".equals(student._schoolKind)) {
            		_form = "KNJD186N_J2.frm";
            	} else {
            		_form = "KNJD186N2.frm";
            	}
            } else if (ITEM_DIV2_SOTEN_TSUCHIHYO.equals(param()._itemDiv)) {
            	// 素点通知票
            	_attendSemeAllPos = 4;
            	if ("J".equals(student._schoolKind)) {
            		_form = "KNJD186N_J.frm";
            	} else {
            		_form = "KNJD186N.frm";
            	}
            }
            setForm(ipdf, _form);

            printCHeader(db2, ipdf, student);
//            printCAddress(ipdf, student);
            //出力内容が 1:テスト点 の場合、出席欄を非表示とする
            if ("1".equals(_param._itemDiv)) {
                whitespace(ipdf, "BLANK");
            } else {
                //出席欄の印字
                printCAttendance(ipdf, student);
            }
            printCCommunication(ipdf, student);
            printCScore(ipdf, student);
        }

//        void printCAddress(final IPdf ipdf, final Student student) {
//            final Address address = student._address;
//            if (null == address) {
//                return;
//            }
////            ipdf.setString("ZIPCD", address._zipcd); // 郵便番号
//            if (!StringUtils.isBlank(address._zipcd)) {
//                ipdf.VrsOut("ZIPCD", "〒" + address._zipcd); // 郵便番号
//            }
//            final boolean useAddress2 = getMS932ByteLength(address._address1) > 40 || getMS932ByteLength(address._address2) > 40;
//            ipdf.VrsOut(useAddress2 ? "ADDR1_2" : "ADDR1", address._address1); // 住所
//            ipdf.VrsOut(useAddress2 ? "ADDR2_2" : "ADDR2", address._address2); // 住所
//            if (null != address._addressee) {
//                ipdf.VrsOut(getMS932ByteLength(address._addressee) > 20 ? "ADDRESSEE2" : "ADDRESSEE", address._addressee + "　様"); // 受取人2
//            }
//        }

        void printCAttendance(final IPdf ipdf, final Student student) {
            final Attendance[] attendances = getAttendances(student);
            for (int i = 0; i < attendances.length; i++) {
                final Attendance att = attendances[i];
                if (null == att || att._lesson == 0) {
                    continue;
                }
                final int line = SEMEALL.equals(att._dateRange._key) ? _attendSemeAllPos : i + 1;
                ipdf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引等日数
                ipdf.VrsOutn("PRESENT", line, String.valueOf(att._mLesson)); // 出席すべき日数
//                ipdf.VrsOutn("ABROAD", line, String.valueOf(att._abroad)); // 留学日数
                ipdf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
            	ipdf.VrsOutn("ABSENCE", line, String.valueOf(att._kekka)); // 欠課回数
            	ipdf.VrsOutn("SICK", line, String.valueOf(att._absent)); // 欠席日数
                ipdf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                ipdf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                ipdf.VrsOutn("CHOREI", line, String.valueOf(att._choreiketsu)); // 朝礼欠回数
                ipdf.VrsOutn("TAIREI", line, String.valueOf(att._taireiketsu)); // 退礼欠回数
            }

        }

        // 空白の画像を表示して欄を非表示
        private void whitespace(final IPdf ipdf, final String field) {
            if (null != _param._whitespaceImagePath) {
            	ipdf.VrsOut(field, _param._whitespaceImagePath);
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
//            if (null != student.getHrAttendNo(param())) {
//                ipdf.VrsOut("HR_ATTNO_NAME", "(" + student.getHrAttendNo(param()) + ")"); // 年組番・氏名
//            }
//            ipdf.setString("MAJORNAME", student._majorname); // 学科名
//            ipdf.setString("HR_NAME", student._hrName); // クラス名
//            ipdf.setString("ATTENDNO", student._attendno); // 出席番号
//            ipdf.setString("NAME", student._name); // 氏名
            final String smajorname = StringUtils.defaultString(student._majorname);
            final String shrname = StringUtils.defaultString(student._hrName);
            final String sattendno = StringUtils.defaultString(student._attendno);
            final String sname = StringUtils.defaultString(student._name);
            ipdf.VrsOut("SCH_INFO", smajorname + "  " + shrname + sattendno + "番　　" + sname);
            ipdf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param()._year)) + "年度"); // 年度
            ipdf.VrsOut("TITLE", StringUtils.defaultString(param()._title, ITEM_DIV1_KOUSATOKUTEN_TSUCHIHYO.equals(param()._itemDiv) ? "考査得点通知表" : "素点通知表")); // 年度
            if ("1".equals(param()._printRank)) {
                ipdf.VrsOut("RANK_NAME1", "学級順位"); // 項目
                ipdf.VrsOut("RANK_NAME", getGroupDivName()); // 項目
            }

            if (SEMEALL.equals(param()._semester)) {
            	final boolean addPrevious = false; // "1".equals(param()._addPastCredit);
                ipdf.VrsOut("GET_CREDIT", student.getTotalGetCredit(param(), addPrevious)); // 修得単位数
            }

            for (int i = 1; i <= 9; i++) {
                final String si = String.valueOf(i);
                ipdf.VrsOut("KEKKA_NAME" + si + "_2", "欠課時数");
            }

            final SubClass subclass999999 = (SubClass) student.getSubClass(SUBCLASSCD999999);
            for (int ti = 0; ti < _testItems.size(); ti++) {
                if (null != testItem(ti)) {
                    final boolean isGakunenHyotei = TESTCD_GAKUNEN_HYOTEI.equals(testItem(ti)._testcd);
                    final boolean isGakunenHyoka = TESTCD_GAKUNEN_HYOKA.equals(testItem(ti)._testcd);
                    boolean printGakunenHyokaHyoteiSumAvg = true;
                    final String line = isGakunenHyotei ? "9" : String.valueOf(ti + 1);
                    final String line2 = isGakunenHyoka ? "9" : line;
                    final TestItem testItem = testItem(ti);
                    if (isGakunenHyoka) {
                    	final String testTitle = StringUtils.defaultString(testItem._scoreDivName, "学年評価");
                        ipdf.VrsOut("HYOKA_" + TESTCD_GAKUNEN_HYOKA + (testTitle.length() > 3 ? "_2" : ""), testTitle);
                    } else {
                    	final String testTitle = StringUtils.defaultString(testItem._scoreDivName, "評価");
                        ipdf.VrsOut("HYOKA" + line + (testTitle.length() > 3 ? "_2" : ""), testTitle);
                    }

                    String testname = "";
//                    if (null == testItem._dateRange || !SEMEALL.equals(testItem._dateRange._semester)) {
//                        testname += StringUtils.defaultString(param().getSemestername(testItem._semester));
//                    }
                    testname += StringUtils.defaultString(testItem._testitemname);
                    ipdf.VrsOut("TESTNAME" + line + "_2", testname); // テスト名
                    if (!isGakunenHyotei && testItem._printScore) {
                        ipdf.VrsOut("KETTEN_CNT" + line2, student.getKettenSubclassCount(param(), testItem)); // 欠点科目数
                    }
                    if (null != subclass999999._scoreMap.get(testItem(ti)._testcd)) {
                        final Score score = (Score) subclass999999._scoreMap.get(testItem(ti)._testcd);
                        final Rank rank = getGroupDivRank(score);
                        if (isGakunenHyotei) {
                            if (!student.hasKari(param()) || student.hasKari(param()) && "1".equals(param()._tutisyoPrintKariHyotei)) {
                                if (printGakunenHyokaHyoteiSumAvg) {
                                    ipdf.VrsOut("AVE_VALUE", sishaGonyu(score._avg)); // 平均
                                    ipdf.VrsOut("TOTAL_VALUE", score._score); // 合計
                                }
                            }
                        } else {
                                if (printGakunenHyokaHyoteiSumAvg) {
                                    ipdf.VrsOut("AVERAGE" + line2, sishaGonyu(score._avg)); // 平均
                                    ipdf.VrsOut("TOTAL" + line2, score._score); // 合計
                                }
                                if ("1".equals(param()._printRank)) {
                                	ipdf.VrsOut("HR_CLASS_RANK" + line2, score._hrRank.getRank(param())); // クラス順位
                                	ipdf.VrsOut("HR_CLASS_SLASH" + line2, "／");
                                	ipdf.VrsOut("HR_CLASS_CNT" + line2, score._hrRank._count); // クラス人数
                                	ipdf.VrsOut("GRADE_RANK" + line2, rank.getRank(param())); // 学年順位 or 学科順位
                                	ipdf.VrsOut("GRADE_SLASH" + line2, "／");
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
                    attendTestname = param().getSemestername(SEMEALL);
                    attendTestnamePos = _attendSemeAllPos;
                } else {
                    attendTestname = "";
                    if (ITEM_DIV2_SOTEN_TSUCHIHYO.equals(param()._itemDiv)) {
                    	if (null == dr || !SEMEALL.equals(dr._semester)) {
                    		attendTestname += StringUtils.defaultString(param().getSemestername(dr._semester));
                    	}
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
            ipdf.VrsOut("LATE_TITLE", "遅刻回数");
            ipdf.VrsOut("EARLY_TITLE", "早退回数");
        }

        void printCScore(final IPdf ipdf, final Student student) {
        	final String suffix = "J".equals(student._schoolKind) ? "__2" : "";
        	final int maxLine = "J".equals(student._schoolKind) ? 23 : 20;
            int count = 0;
            String oldclasscd = null;
            final List printSubclassList = getPrintSubclassList(student, maxLine);
            
//        	for (int ti = 0; ti < _attendSubclassRanges.length; ti++) {
//        		final DateRange dr = _attendSubclassRanges[ti];
//        		if (null == dr || !SEMEALL.equals(dr._semester) && 0 > param()._semester.compareTo(dr._semester)) {
//        			continue;
//        		}
//        		
//        		BigDecimal totalKekka = BigDecimal.valueOf(0);
//            	for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
//                    final SubClass subClass = (SubClass) it.next();
//                    if (subClass._mst._isSaki) {
//                    	continue;
//                    }
//                    BigDecimal kekka = null;
//                    if (SEMEALL.equals(dr._semester)) {
//                    	// 各試験の合計を換算
//                    	SubclassAttendance total = new SubclassAttendance(null, null, null, null);
//                    	for (final Iterator attit = subClass._attendMap.values().iterator(); attit.hasNext();) {
//                    		final SubclassAttendance att = (SubclassAttendance) attit.next();
//                    		if (null != att) {
//                    			total = total.add(att);
//                    		}
//                    	}
//                    	kekka = kirisute(getKekka(total, dr, param()));
//                    } else if (!dr._childrenDateRangeList.isEmpty()) {
//                    	// 学期内の試験の各換算結果の合計
//                    	for (final Iterator tit = dr._childrenDateRangeList.iterator(); tit.hasNext();) {
//                    		final DateRange tdr = (DateRange) tit.next();
//                    		final SubclassAttendance sa = subClass.getAttendance(tdr._key);
//                    		if (null != sa) {
//                    			final BigDecimal testKekka = kirisute(getKekka(sa, tdr, param()));
//                    			if (null == kekka) {
//                    				kekka = new BigDecimal(0);
//                    			}
//                    			kekka = kekka.add(testKekka);
//                    		}
//                    	}
//                    	
//                    } else if (null != dr._testitem) {
//                    	final SubclassAttendance sa = subClass.getAttendance(dr._key);
//                    	if (null != sa) {
//                    		kekka = kirisute(getKekka(sa, dr, param()));
//                    	}
//                    } else {
//                    	log.error(" illegal state dateRange = " + dr);
//                    	continue;
//                    }
//                    if (null != kekka) {
//                    	totalKekka = totalKekka.add(kekka);
//                    }
//            	}
//            	final int line = SEMEALL.equals(dr._semester) ? _attendSemeAllPos : ti + 1;
//            	ipdf.VrsOutn("ABSENCE", line, getAbsentStr(param(), totalKekka, false)); // 遅刻回数
//        	}

        	for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                final int ci = count + 1;
                final SubClass subClass = (SubClass) it.next();
                loginfo(param(), " subclass = " + subClass);
                final String subclasscd = subClass._mst._subclasscd;
                Form.setRecordString(ipdf, "GRP" + suffix, ci, subclasscd.substring(0, 2));
                if (null == oldclasscd || !oldclasscd.equals(subclasscd.substring(0, 2))) {
                    Form.setRecordString(ipdf, "CLASSNAME" + (getMS932ByteLength(subClass._mst._classname) > 8 ? "_2" : "") + suffix, ci, subClass._mst._classname); // 教科名
                    oldclasscd = subclasscd.substring(0, 2);
                }
                if (getMS932ByteLength(subClass._mst._subclassname) > 24) {
                    if (getMS932ByteLength(subClass._mst._subclassname) > 28) {
                        final List tokenList = getTokenList(subClass._mst._subclassname, 28);
                        for (int i = 0; i < tokenList.size(); i++) {
                            Form.setRecordString(ipdf, "SUBCLASSNAME2_" + String.valueOf(i + 1) + suffix, ci, (String) tokenList.get(i)); // 科目名
                        }
                    } else {
                        Form.setRecordString(ipdf, "SUBCLASSNAME2_1" + suffix, ci, subClass._mst._subclassname); // 科目名
                    }
                } else {
                    Form.setRecordString(ipdf, "SUBCLASSNAME" + suffix, ci, subClass._mst._subclassname); // 科目名
                }
                Form.setRecordString(ipdf, "CREDIT" + suffix, ci, param().getCredits(subclasscd, student._course)); // 単位数
                for (int ti = 0; ti < _testItems.size(); ti++) {
                    final TestItem item = testItem(ti);
                    if (null == item || 0 > param()._semester.compareTo(item._semester)) {
                        continue;
                    }
//                    if ("1".equals(param()._notPrintGappeiMotoGakunenHyokaHyotei) && SEMEALL.equals(item._semester) && subClass._mst._isMoto) {
//                        continue;
//                    }
                    final Score score = subClass.getScore(item._testcd);
                    final String line = String.valueOf(ti + 1);
                    if (null != score) {
                        if (TESTCD_GAKUNEN_HYOTEI.equals(item._testcd)) {
                            if ((!"1".equals(score._provFlg) || "1".equals(score._provFlg) && "1".equals(param()._tutisyoPrintKariHyotei))) {
                                final String field1 = "VALUE";
                                Form.setRecordString(ipdf, field1 + suffix, ci, score._score); // 評定
                            }
                        } else {
                            final String field1;
                            if (TESTCD_GAKUNEN_HYOKA.equals(item._testcd)) {
                                field1 = "SCORE9";
                            } else {
                                if (false) { // if (_notPrintTestItem == item) { // 成績表記なし
                                    field1 = "";
                                } else {
                                    field1 = "SCORE" + line;
                                }
                            }
                            Form.setRecordString(ipdf, field1 + suffix, ci, score._score); // 評価
                        }
                    }
                }
                if (!"J".equals(student._schoolKind)) {
                	for (int ti = 0; ti < _attendSubclassRanges.length; ti++) {
                		final DateRange dr = _attendSubclassRanges[ti];
                		if (null == dr || !SEMEALL.equals(dr._semester) && 0 > param()._semester.compareTo(dr._semester)) {
                			continue;
                		}
                		final String pos;
                		if (SEMEALL.equals(dr._semester)) {
                			pos = SEMEALL;
                		} else {
                			pos = String.valueOf(ti + 1);
                		}
                		BigDecimal kekka = null;
                		if (SEMEALL.equals(dr._semester)) {
                			// 各試験の合計を換算
                			SubclassAttendance total = new SubclassAttendance(null, null, null, null);
                			for (final Iterator attit = subClass._attendMap.values().iterator(); attit.hasNext();) {
                				final SubclassAttendance att = (SubclassAttendance) attit.next();
                				if (null != att) {
                					total = total.add(att);
                				}
                			}
            				kekka = kirisute(getKekka(total, dr, param()));
                		} else if (!dr._childrenDateRangeList.isEmpty()) {
                			// 学期内の試験の各換算結果の合計
                			for (final Iterator tit = dr._childrenDateRangeList.iterator(); tit.hasNext();) {
                				final DateRange tdr = (DateRange) tit.next();
                    			final SubclassAttendance sa = subClass.getAttendance(tdr._key);
                    			if (null != sa) {
                    				final BigDecimal testKekka = kirisute(getKekka(sa, tdr, param()));
                					if (null == kekka) {
                						kekka = new BigDecimal(0);
                					}
                					kekka = kekka.add(testKekka);
                    			}
                			}

                		} else if (null != dr._testitem) {
                			final SubclassAttendance sa = subClass.getAttendance(dr._key);
                			if (null != sa) {
                				kekka = kirisute(getKekka(sa, dr, param()));
                			}
                		} else {
                			log.error(" illegal state dateRange = " + dr);
                			continue;
                		}
                		if (null != kekka) {
                			Form.setRecordString(ipdf, "KEKKA" + pos +"_2" + suffix, ci, getAbsentStr(param(), kekka, false)); // 欠課時数
                		}
                	}
                }
                ipdf.VrEndRecord();
                count++;
            }
            if (count == 0 || count % maxLine != 0) {
            	for (int i = 0; i < maxLine - (count % maxLine); i++) {
            		Form.setRecordString(ipdf, "GRP" + suffix, i, String.valueOf(i)); // 科目名
            		ipdf.VrEndRecord();
            	}
            }
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 71984 $ $Date: 2020-01-27 15:08:50 +0900 (月, 27 1 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;

        final String _itemDiv; // 1:テスト点 2:素点

        final String _groupDiv; // 総合順位出力 1:学年 4:学科 3:コース
        final String _rankDiv; // 順位の基準点 1:総合点 2:平均点
        final String _tutisyoPrintKariHyotei; // 仮評定を表示する
        final String _printRank; // 順位表記
        final String _schoolKind;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final Form _form;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMst;
        private Map _creditMst;
        private Map _recordMockOrderSdivDatMap;

        private KNJSchoolMst _knjSchoolMst;

        private String _avgDiv;
        final String _d054Namecd2Max;
        final String _sidouHyoji;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
        private String _title;
        final String _h508Name1;
        private boolean _isNoPrintMoto;
        final Map _testitemMap;
        final List _semesterDetailList;
        final boolean _isOutputDebug;
        final Map _attendParamMap;

        final String _documentRoot;
        private String _imagePath;
        private String _extension;
        final String _whitespaceImagePath;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _schoolKind = getSchoolKind(db2);
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _groupDiv = request.getParameter("GROUP_DIV");
            _rankDiv = request.getParameter("RANK_DIV");
            _tutisyoPrintKariHyotei = request.getParameter("tutisyoPrintKariHyotei");
            _printRank = request.getParameter("PRINT_RANK");
            _itemDiv = request.getParameter("ITEM_DIV");

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _form = new Form();
            _form._param = this;

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(row, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(row, "NAME1");
            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);         //各学校における定数等設定
//            _gradCredits = getGradCredits(db2);
            setSubclassMst(db2);
            setCreditMst(db2);
            setRecordMockOrderSdivDat(db2);
            _h508Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'H508' AND NAMECD2 = '02' "));
            loadNameMstD016(db2);
            _testitemMap = getTestItemMap(db2);
            _semesterDetailList = getSemesterDetailList(db2);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _documentRoot           = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            _whitespaceImagePath = getImageFilePath("whitespace.png");
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186N' AND NAME = '" + propName + "' "));
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
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
            return (Semester) _semesterMap.get(semester);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            final Map map = new TreeMap();
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

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map rs = (Map) it.next();

                map.put(KnjDbUtils.getString(rs, "SEMESTER"), new Semester(KnjDbUtils.getString(rs, "SEMESTER"), KnjDbUtils.getString(rs, "SEMESTERNAME"), KnjDbUtils.getString(rs, "SDATE"), KnjDbUtils.getString(rs, "EDATE")));
            }
            return map;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' ");
            if ("J".equals(_schoolKind)) {
            	sql.append("   AND CERTIF_KINDCD = '103' ");
            } else {
            	sql.append("   AND CERTIF_KINDCD = '104' ");
            }
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _schoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _jobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _principalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _hrJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任");
            _title = KnjDbUtils.getString(row, "REMARK6");
        }

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
            return (String) _creditMst.get(subclasscd + ":" + course);
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMst = new HashMap();
            try {
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
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _creditMst.put(rs.getString("SUBCLASSCD") + ":" + rs.getString("COURSE"), rs.getString("CREDITS"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public List getRecordMockOrderSdivDat(final String grade, final String coursecd, final String majorcd) {
            log.info(" grade = " + grade + ", coursecd = " + coursecd + ", majorcd = " + majorcd);
            final String[] keys = {grade + "-" + coursecd + "-" + majorcd, "00" + "-" + coursecd + "-" + majorcd, grade + "-" + "0" + "-" + "000", "00" + "-" + "0" + "-" + "000"};
            for (int i = 0; i < keys.length; i++) {
                final List rtn = (List) _recordMockOrderSdivDatMap.get(keys[i]);
                if (null != rtn) {
                    log.info(" set key = " + keys[i]);
                    return rtn;
                }
            }
            log.info(" set key = " + ArrayUtils.toString(keys) + " but nothing");
            return Collections.EMPTY_LIST;
        }

        private void setRecordMockOrderSdivDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _recordMockOrderSdivDatMap = new HashMap();
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
                    if (null == _recordMockOrderSdivDatMap.get(key)) {
                        _recordMockOrderSdivDatMap.put(key, new ArrayList());
                    }
                    ((List) _recordMockOrderSdivDatMap.get(key)).add(rs.getString("TESTCD"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        protected Map getTestItemMap(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map testitemMap = new HashMap();
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
                                 +  " ,CASE WHEN T1.SEMESTER = '9' THEN NMD053.NAME3 ELSE NMD053.NAME1 END AS SCORE_DIV_NAME "
                                 +  "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 "
                                 +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
                                 +  " AND T2.SEMESTER = T1.SEMESTER "
                                 +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                                 +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND NOT (T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.SCORE_DIV = '09') "
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

        protected List getSemesterDetailList(
                final DB2UDB db2
        ) {
            final List semesterDetailList = new ArrayList();
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

        public String getImageFilePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name;
            final boolean exists = new File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

    }
}
