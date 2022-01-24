// kanji=漢字
/*
 * $Id: 82983a11b1222a1d9cca1b8daa0e24f61df474c8 $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD186C {
    private static final Log log = LogFactory.getLog(KNJD186C.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD333333 = "333333";
    private static final String SUBCLASSCD555555 = "555555";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";

    private boolean _hasData;

    static KNJEditString knjobj = new KNJEditString();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

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
            final Vrw32alp svf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

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
            final Vrw32alp svf,
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
//            param._form.init(db2, param.getTestcdList(student._grade, student._coursecd, student._majorcd));
            param._form.print(svf, student);
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

    private static List getRowList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List studentList0
    ) {


//        final Map courseStudentsMap = new HashMap();
//        for (final Iterator it = studentList0.iterator(); it.hasNext();) {
//            final Student student = (Student) it.next();
//            final String key = student._grade + "-" + student._coursecd + "-" + student._majorcd;
//            getMappedList(courseStudentsMap, key).add(student);
//        }
//
//        for (final Iterator it = courseStudentsMap.keySet().iterator(); it.hasNext();) {
//            final String key = (String) it.next();
//            final List studentList = (List) courseStudentsMap.get(key);
//            final String[] split = StringUtils.split(key, "-");
//
//        }

        final List studentList = studentList0;
        final List testcdList = param.getTestcdList();

        param._form.init(db2, testcdList);

        for (int i = 0; i < param._form._attendRanges.length; i++) {
            final DateRange range = param._form._attendRanges[i];
            Attendance.load(db2, param, studentList, range);
        }
        for (int i = 0; i < param._form._attendSubclassRanges.length; i++) {
            final DateRange range = param._form._attendSubclassRanges[i];
            SubclassAttendance.load(db2, param, studentList, range);
        }

        String testcdor = "";
        final StringBuffer stbtestcd = new StringBuffer();
        stbtestcd.append(" AND (");
        for (int i = 0; i < param._form._testcds.length; i++) {
            final String testcd = param._form._testcds[i];
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
        ProficiencyScore.load(db2, param, studentList);
        Behavior.setBehaviorList(db2, studentList, param);
        Student.setHreportremarkCommunication(param, db2, studentList);
        Student.setTotalStudy(db2, param, studentList);
        Student.setMoral(db2, param, studentList);
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
        final String _coursecd;
        final String _majorcd;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
//        final Map _attendRemarkMap;
        final Map _subclassMap;
        final List _proficiencySubclassScoreList;
        final String _entyear;
        final Map _hreportremarkMap = new HashMap();

        private String _totalStudyAct;
        private String _totalStudyTime;

        private String _moral;
        private String _dataInputSize;

        public Map _behaviorMap = Collections.EMPTY_MAP;
        private Map _csKadaiClassScore = new HashMap();

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
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
            _attendMap = new TreeMap();
//            _attendRemarkMap = new TreeMap();
            _subclassMap = new TreeMap();
            _proficiencySubclassScoreList = new ArrayList();
        }

        SubClass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                return new SubClass(new SubclassMst(null, null, null, null, null, false, false, new Integer(999999), new Integer(999999)));
            }
            return (SubClass) _subclassMap.get(subclasscd);
        }

        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
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
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT W6 ON W6.YEAR = W1.YEAR ");
            stb.append("                  AND W6.SEMESTER = W1.SEMESTER ");
            stb.append("                  AND W6.GRADE = W1.GRADE ");
            stb.append("                  AND W6.HR_CLASS = W1.HR_CLASS ");
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

            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    students.add(new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

//        // 仮評定があるか
//        public boolean hasKari(final Param param) {
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd) || subclasscd.endsWith("333333") || subclasscd.endsWith("555555") || subclasscd.endsWith("99999B")) {
//                    continue;
//                }
//                final SubClass subClass = getSubClass(subclasscd);
//                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
//                if (null != score && NumberUtils.isDigits(score._score) && "1".equals(score._provFlg)) {
//                    return true;
//                }
//            }
//            return false;
//        }

//        public String getTotalGetCredit(final Param param) {
//            int totalGetCredit = 0;
//            int totalGetCreditKari = 0;
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd)) {
//                    continue;
//                }
//                final SubClass subClass = getSubClass(subclasscd);
//                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subClass.getScore(TESTCD_GAKUNEN_HYOTEI);
//                if (null != score && NumberUtils.isDigits(score.getGetCredit())) {
//                    final int iCredit = Integer.parseInt(score.getGetCredit());
//                    if ("1".equals(score._provFlg)) {
//                        totalGetCreditKari += iCredit;
//                    } else {
//                        totalGetCredit += iCredit;
//                    }
//                }
//            }
//            if (totalGetCreditKari > 0 && totalGetCredit == 0) {
//                return "(" + String.valueOf(totalGetCreditKari) + ")";
//            }
//            return totalGetCredit > 0 ? String.valueOf(totalGetCredit) : null;
//        }

//        public String getKettenSubclassCount(final Param param, final TestItem testItem) {
//            final List list = new ArrayList();
//            boolean hasNotNullSubclassScore = false;
//            for (final Iterator it = _subclassMap.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                if (SUBCLASSCD999999.equals(subclasscd)) {
//                    continue;
//                }
//                final SubClass subClass = getSubClass(subclasscd);
//                if (null == subClass || param._isNoPrintMoto && subClass._mst._isMoto) {
//                    continue;
//                }
//                final Score score = subClass.getScore(testItem._testcd);
//                if (null != score) {
//                    if (score.isFail(param, testItem)) {
//                        list.add(subClass);
//                    }
//                    if (null != score._score) {
//                        hasNotNullSubclassScore = true;
//                    }
//                }
//            }
//            if (!hasNotNullSubclassScore) {
//                return null;
//            }
//            return String.valueOf(list.size());
//        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SEMESTER, TOTALSTUDYTIME, REMARK1, REMARK2 ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER <= '" + (SEMEALL.equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("   AND SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._hreportremarkMap.put("TOTALSTUDYTIME" + rs.getString("SEMESTER"), rs.getString("TOTALSTUDYTIME"));
                        student._hreportremarkMap.put("REMARK1" + rs.getString("SEMESTER"), rs.getString("REMARK1"));
                        student._hreportremarkMap.put("REMARK2" + rs.getString("SEMESTER"), rs.getString("REMARK2"));
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

        private static Map createMap(final ResultSet rs) throws SQLException {
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
        public static void setTotalStudy(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SEMESTER ");
                stb.append("     ,T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("     ,T1.TOTALSTUDYTIME ");
                stb.append("     ,T1.TOTALSTUDYACT");
                stb.append("     ,T2.NAMESPARE1 ");
                stb.append(" FROM ");
                stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
                stb.append("     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '" + param._year + "' ");
                stb.append("         AND T2.NAMECD1 = '" + param._d008Namecd1 + "' ");
                stb.append("         AND T2.NAMECD2 = T1.CLASSCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SEMESTER = '" + SEMEALL + "' ");
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append("     AND T1.CLASSCD = '97' ");
                stb.append(" ORDER BY ");
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");

                final String sql = stb.toString();

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String totalStudyAct = rs.getString("TOTALSTUDYACT");
                        final String totalStudyTime = rs.getString("TOTALSTUDYTIME");

                        student._totalStudyAct = addLine(student._totalStudyAct, totalStudyAct);
                        student._totalStudyTime = addLine(student._totalStudyTime, totalStudyTime);
                    }
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

        /**
         * 道徳をセットする
         * @param db2
         * @param param
         * @param studentList
         */
        public static void setMoral(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.REMARK2 AS COLUMNNAME ");
                stb.append(" FROM ");
                stb.append("   HREPORTREMARK_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + param._year + "' ");
                stb.append("   AND T1.SEMESTER = '" + param._moralSeme + "' ");
                stb.append("   AND T1.SCHREGNO = ? ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    String delim = "";
                    String colName = "";
                    String moral = "";
                    while (rs.next()) {
                        colName = StringUtils.defaultString(rs.getString("COLUMNNAME"));
                        if(!"".equals(colName)) {
                            moral += delim + colName;
                            delim = "\r\n";
                        }
                    }
                    student._dataInputSize = "";
                    student._moral = moral;
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
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
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
//        BigDecimal _lhrKekka = new BigDecimal("0");
//        BigDecimal _gyojiKekka = new BigDecimal("0");
//        BigDecimal _iinkaiKekka = new BigDecimal("0");
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._sdate) < 0 || dateRange._sdate.compareTo(param._edate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._edate) > 0 ? param._edate : dateRange._edate;
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
//                log.debug(" attend sql = " + sql);
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
        public int compareTo(final Object o2) {
            final SubClass s2 = (SubClass) o2;
            return _mst.compareTo(s2._mst);
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
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._sdate) < 0 || dateRange._sdate.compareTo(param._edate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._edate) > 0 ? param._edate : dateRange._edate;
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

                // log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

//                    final Map specialGroupKekkaMinutes = new HashMap();

                    while (rs.next()) {
                        if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMst.get(subclasscd);
                        if (null == mst) {
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd < Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
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
        final String _slumpScore;
        final String _slumpScoreKansan;
        final String _provFlg;

        Score(
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
                final String slumpScore,
                final String slumpScoreKansan,
                final String compCredit,
                final String getCredit,
                final String provFlg
        ) {
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
            _provFlg = provFlg;
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

//        private int getFailValue(final Param param) {
//            if (param.isPerfectRecord() && null != _passScore) {
//                return Integer.parseInt(_passScore);
//            } else if (param.isKetten() && !StringUtils.isBlank(param._ketten)) {
//                return Integer.parseInt(param._ketten);
//            }
//            return -1;
//        }

        private String getPrintSlump(final TestItem testItem) {
//            if (null != testItem._sidouinput) {
//                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
//                    return _slumpMark;
//                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点
//                    return _slumpScore;
//                }
//            }
            return null;
        }

        private boolean isFail(final Param param, final TestItem testItem) {
//            if (null != testItem._sidouinput) {
//                if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouinputinf)) { // 記号
//                    if (null != _slumpMark) {
//                        if (null != param._d054Namecd2Max && param._d054Namecd2Max.equals(_slumpMark)) {
//                            return true;
//                        }
//                        return false;
//                    }
//                } else if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouinputinf)) { // 得点
//                    if (null != _slumpScoreKansan) {
//                        return "1".equals(_slumpScoreKansan);
//                    }
//                }
//            }
//            if (testItem._testcd != null && testItem._testcd.endsWith("09")) {
//                return "1".equals(_score);
//            }
//            return "1".equals(_assessLevel);
            return false;
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
                log.info(" subclass query start.");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" subclass query end.");

                while (rs.next()) {
                    final Student student = getStudent(studentList, rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student) {
                        continue;
                    }

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_AVG_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = null; // new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("GRADE_AVG_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = null; // new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final Score score = new Score(
                            rs.getString("SCORE"),
                            null, // rs.getString("ASSESS_LEVEL"),
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
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }


            try {
                final String sql = sqlCs(param, stbtestcd);
                log.info(" cs kadai query start.");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.info(" cs kadai query end.");

                while (rs.next()) {
                    final Student student = getStudent(studentList, rs.getString("SCHREGNO"));
                    final String testcd = rs.getString("TESTCD");
                    if (null == student || !"9990009".equals(testcd)) {
                        continue;
                    }

                    student._csKadaiClassScore.put(rs.getString("CLASSCD"), rs.getString("SCORE"));

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

//            stb.append("   , REL_COUNT AS (");
//            stb.append("   SELECT SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("     , COUNT(*) AS COUNT ");
//            stb.append("          FROM RELATIVEASSESS_MST ");
//            stb.append("          WHERE GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND ASSESSCD = '3' ");
//            stb.append("   GROUP BY SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("   ) ");


            stb.append(", T_AVG_AVG AS(");
            stb.append("    SELECT ");
            stb.append("     W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.SUBCLASSCD ");
            stb.append("    ,'1' AS AVG_DIV "); // 学年
            stb.append("    ,T1.GRADE ");
            stb.append("    ,AVG(W3.AVG) AS AVG_AVG ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN (SELECT DISTINCT SCHREGNO, GRADE ");
            stb.append("                FROM SCHREG_REGD_DAT ");
            stb.append("                WHERE YEAR = '" + param._year + "' ");
            stb.append("               ) T1 ON T1.SCHREGNO = W3.SCHREGNO ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("     AND W3.SUBCLASSCD = '999999' ");
            stb.append(stbtestcd.toString());
            stb.append("    GROUP BY ");
            stb.append("     W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.SUBCLASSCD ");
            stb.append("    ,T1.GRADE ");
            stb.append("     ) ");

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
//            stb.append("    ,CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("       FROM RELATIVEASSESS_MST L3 ");
//            stb.append("       WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
//            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("         AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("      ) ELSE ");
//            stb.append("      (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("       FROM ASSESS_MST L3 ");
//            stb.append("       WHERE L3.ASSESSCD = '3' ");
//            stb.append("         AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("      ) ");
//            stb.append("     END AS ASSESS_LEVEL ");
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
            stb.append("    ,T_AVG11.AVG_AVG AS GRADE_AVG_AVG ");
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
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG1.AVG_DIV = '1' ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    LEFT JOIN T_AVG_AVG T_AVG11 ON T_AVG11.SEMESTER = W3.SEMESTER AND T_AVG11.TESTKINDCD = W3.TESTKINDCD AND T_AVG11.TESTITEMCD = W3.TESTITEMCD AND T_AVG11.SCORE_DIV = W3.SCORE_DIV AND T_AVG11.GRADE = '" + param._grade + "' AND T_AVG11.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND T_AVG11.AVG_DIV = '1' ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        AND W2.CLASSCD = W3.CLASSCD ");
            stb.append("        AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("        AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("        AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("        AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append("    LEFT JOIN CHAIR_A CH1 ON W3.SCHREGNO = CH1.SCHREGNO ");
            stb.append("        AND CH1.SUBCLASSCD = ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD ");
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

//            //成績不振科目データの表
//            stb.append(",RECORD_SLUMP AS(");
//            stb.append("    SELECT  W3.SCHREGNO ");
//            stb.append("    ,W3.SEMESTER ");
//            stb.append("    ,W3.TESTKINDCD ");
//            stb.append("    ,W3.TESTITEMCD ");
//            stb.append("    ,W3.SCORE_DIV, ");
//            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
//            stb.append("    W3.SUBCLASSCD AS SUBCLASSCD ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN W3.SCORE END AS SLUMP_SCORE ");
//            stb.append("    ,CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
//            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM RELATIVEASSESS_MST L3 ");
//            stb.append("           WHERE L3.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' AND L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("          ) ELSE ");
//            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
//            stb.append("           FROM ASSESS_MST L3 ");
//            stb.append("           WHERE L3.ASSESSCD = '3' ");
//            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
//            stb.append("          ) ");
//            stb.append("         END ");
//            stb.append("    END AS SLUMP_SCORE_KANSAN ");
//            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
//            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
//            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
//            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
//            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
//            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
//            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
//            stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
//            stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
//            stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
//            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
//            stb.append(stbtestcd.toString());
//            stb.append("            AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = W3.SCHREGNO) ");
//            stb.append("            AND W3.SCORE IS NOT NULL ");
//            stb.append("     ) ");
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
//            stb.append("    UNION ");
//            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_SLUMP ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
//            stb.append("    UNION ");
//            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SLUMP ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
//            stb.append("        ,T3.ASSESS_LEVEL ");
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
            stb.append("        ,T3.GRADE_AVG_AVG ");
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
//            stb.append("        ,K1.SLUMP ");
//            stb.append("        ,K1.SLUMP_MARK ");
//            stb.append("        ,K1.SLUMP_SCORE ");
//            stb.append("        ,K1.SLUMP_SCORE_KANSAN ");

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

//            //成績不振科目データの表
//            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T2.SCHREGNO AND K1.SUBCLASSCD = T2.SUBCLASSCD AND K1.SEMESTER = T2.SEMESTER AND K1.TESTKINDCD = T2.TESTKINDCD AND K1.TESTITEMCD = T2.TESTITEMCD AND K1.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" WHERE ");
            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR SUBSTR(T1.SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }


        private static String sqlCs(final Param param, final StringBuffer stbtestcd) {
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
            stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append("     )");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV, ");
            stb.append(" W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append(stbtestcd.toString());
            stb.append("     ) ");

            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("    SELECT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM CHAIR_A ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM RECORD_REC ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.CLASSCD ");
            stb.append("        ,T1.SCHOOL_KIND ");
            stb.append("        ,T1.CURRICULUM_CD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");

            stb.append(" ORDER BY T1.SCHREGNO, T1.SUBCLASSCD");

            return stb.toString();
        }
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _avgAvg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String avgAvg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _avgAvg = avgAvg;
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
                final List studentList
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param);
//                log.debug(" proficiency sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

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
                        final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), null, rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_AVG_AVG"), null);
                        final Rank hrRank = null; // new Rank(rs.getString("HR_RANK"), null, rs.getString("HR_COUNT"), rs.getString("HR_AVG"), null);
                        final Rank courseRank = null; // new Rank(rs.getString("COURSE_RANK"), null, rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), null);
                        final Rank majorRank =  null; // new Rank(rs.getString("MAJOR_RANK"), null, rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), null);
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

            stb.append(" WITH T_AVG_AVG AS (  ");
            stb.append(" SELECT  ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD, T1.YEAR, T1.SEMESTER, T1.PROFICIENCYDIV, T1.PROFICIENCYCD, T1.RANK_DIV, T3.GRADE, ");
            stb.append("     AVG(T1.AVG) AS AVG_AVG ");
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T3.GRADE = '" + param._grade + "' ");
            stb.append(" WHERE  ");
            stb.append("     T1.YEAR = '" + param._year + "' AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND (T1.PROFICIENCY_SUBCLASS_CD = '" + SUBCLASSCD999999 + "' ");
//            stb.append("       OR T1.PROFICIENCY_SUBCLASS_CD = '" + SUBCLASSCD333333 + "' ");
//            stb.append("       OR T1.PROFICIENCY_SUBCLASS_CD = '" + SUBCLASSCD555555 + "' ");
            stb.append("          ) ");
            if ("2".equals(param._rankDiv)) {
                stb.append("     AND T1.RANK_DATA_DIV = '02' "); // 平均点
            } else {
                stb.append("     AND T1.RANK_DATA_DIV = '01' "); // 総合点
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD, T1.YEAR, T1.SEMESTER, T1.PROFICIENCYDIV, T1.PROFICIENCYCD, T1.RANK_DIV, T3.GRADE ");
            stb.append(" )  ");

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
            stb.append("     T16.AVG AS MAJOR_AVG, ");
            stb.append("     TA1.AVG_AVG AS GRADE_AVG_AVG ");
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
            stb.append(" LEFT JOIN T_AVG_AVG TA1 ON TA1.YEAR = T1.YEAR AND TA1.SEMESTER = T1.SEMESTER ");
            stb.append("     AND TA1.PROFICIENCYDIV = T1.PROFICIENCYDIV AND TA1.PROFICIENCYCD = T1.PROFICIENCYCD AND TA1.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD AND TA1.GRADE = T3.GRADE ");
            stb.append("     AND TA1.RANK_DIV = T1.RANK_DIV "); // 学年
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
            stb.append(" ORDER BY ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD ");
            return stb.toString();
        }
    }

    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _testitemabbv1;
        public String _sidouinputinf;
        public String _sidouinput;
        public String _semester;
        public String _scoreDivName;
        public DateRange _dateRange;
        public boolean _printScore;
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

    private static class SubclassMst implements Comparable {
        final String _subclasscode;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        final Integer _classShoworder;
        final Integer _subclassShoworder;
        public SubclassMst(final String subclasscode, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final boolean isSaki, final boolean isMoto,
                final Integer classShoworder,
                final Integer subclassShoworder) {
            _subclasscode = subclasscode;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _classShoworder = classShoworder;
            _subclassShoworder = subclassShoworder;
        }
        public int compareTo(final Object o2) {
            final SubclassMst s2 = (SubclassMst) o2;
            int rtn;
            rtn = _classShoworder.compareTo(s2._classShoworder);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassShoworder.compareTo(s2._subclassShoworder);
            if (0 != rtn) {
                return rtn;
            }
            if (null == _subclasscode && null == s2._subclasscode) {
                return 0;
            } else if (null == _subclasscode) {
                return 1;
            } else if (null == s2._subclasscode) {
                return -1;
            }
            rtn = _subclasscode.compareTo(s2._subclasscode);
            return rtn;
        }
    }

    private static class Behavior {
        final String _code;
        final String _codename;
        final String _viewname;
        final Map _semesterRecordMap = new HashMap();

        Behavior(
            final String code,
            final String codename,
            final String viewname
        ) {
            _code = code;
            _codename = codename;
            _viewname = viewname;
        }

        public static void setBehaviorList(final DB2UDB db2, final List studentList, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" behav sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._behaviorMap = new TreeMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String code = rs.getString("CODE");
                        if (null == code) {
                            continue;
                        }
                        if (null == student._behaviorMap.get(code)) {
                            final String codename = rs.getString("CODENAME");
                            final String viewname = rs.getString("VIEWNAME");
                            final Behavior behavior = new Behavior(code, codename, viewname);
                            student._behaviorMap.put(code, behavior);
                        }
                        final Behavior b = (Behavior) student._behaviorMap.get(code);
                        final String semester = rs.getString("SEMESTER");
                        final String record = rs.getString("RECORD");
                        b._semesterRecordMap.put(semester, record);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CODE ");
            stb.append("     , T1.CODENAME ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T2.SCHREGNO ");
            stb.append("     , T2.SEMESTER ");
            stb.append("     , T2.RECORD ");
            stb.append(" FROM BEHAVIOR_SEMES_MST T1 ");
            stb.append(" LEFT JOIN BEHAVIOR_SEMES_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.CODE = T1.CODE ");
            stb.append("     AND T2.SCHREGNO = ? ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + param._year + "' ");
            stb.append("   AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE ");
            stb.append("     , T2.SEMESTER ");
            return stb.toString();
        }
    }

    private abstract static class Form {

        String[] _testcds;
        TestItem[] _testItems;
        DateRange[] _attendRanges;
        DateRange[] _attendSubclassRanges;
        private Param _param;

        protected Param param() {
            return _param;
        }

        abstract void init(final DB2UDB db2, final List testcdList);
        protected void initDebug() {
            for (int i = 0; i < _testcds.length; i++) {
                log.info(" testcds[" + i + "] = " + _testcds[i] + " : " + _testItems[i]);
            }
            for (int i = 0; i < _attendRanges.length; i++) {
                log.debug(" attendRanges[" + i + "] = " + _attendRanges[i]);
            }
            for (int i = 0; i < _attendSubclassRanges.length; i++) {
                log.debug(" attendSubclassRanges[" + i + "] = " + _attendSubclassRanges[i]);
            }
        }
        abstract void print(final Vrw32alp svf, final Student student);

        private static int getMS932ByteLength(final String s) {
            if (null != s) {
                try {
                    return s.getBytes("MS932").length;
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return 0;
        }

        private static String getAbsentStr(final Param param, final BigDecimal bd, final boolean notPrintZero) {
            if (null == bd || notPrintZero && bd.doubleValue() == .0) {
                return null;
            }
            final int scale = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4 ? 1 : 0;
            return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }

        protected Attendance[] getAttendances(final Student student) {
            final Attendance[] attendances = new Attendance[_attendRanges.length];
            for (int i = 0; i < _attendRanges.length; i++) {
                final DateRange dateRange = (DateRange) _attendRanges[i];
                if (null != dateRange) {
                    if (SEMEALL.equals(dateRange._key) && !SEMEALL.equals(param()._semester)) {
                        continue;
                    }
                    attendances[i] = (Attendance) student._attendMap.get(dateRange._key);
                }
            }
            return attendances;
        }

        protected Map getTestItemMap() {
            final Map map = new HashMap();
            for (int i = 0; i < _testItems.length; i++) {
                if (null != _testItems[i]) {
                    map.put(_testItems[i]._testcd, _testItems[i]);
                }
            }
            return map;
        }

        protected TestItem[] getTestItems(
                final DB2UDB db2,
                final Param param,
                final String[] testcds
        ) {
            final TestItem[] testitems = new TestItem[testcds.length];
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD "
                                 +  " ,TESTITEMNAME "
                                 +  " ,TESTITEMABBV1 "
                                 + "  ,SIDOU_INPUT "
                                 + "  ,SIDOU_INPUT_INF "
                                 + "  ,T1.SEMESTER "
                                 + "  ,T1.TEST_START_DATE AS SDATE "
                                 + "  ,T1.TEST_END_DATE AS EDATE "
//                                 + "  ,T2.SDATE "
//                                 + "  ,T2.EDATE "
                                 +  " ,CASE WHEN T1.SEMESTER <= '" + param._semester + "' THEN 1 ELSE 0 END AS PRINT "
                                 +  " ,NMD053.NAME1 AS SCORE_DIV_NAME "
                                 +  "FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 "
//                                 +  "LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR "
//                                 +  " AND T2.SEMESTER = T1.SEMESTER "
//                                 +  " AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL "
                                 +  "LEFT JOIN NAME_MST NMD053 ON NMD053.NAMECD1 = 'D053' AND NMD053.NAMECD2 = T1.SCORE_DIV AND T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' "
                                 +  "WHERE T1.YEAR = '" + param._year + "' "
                                 +  "  AND T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV IN " + SQLUtils.whereIn(true, testcds) + " "
                                 +  " ORDER BY T1.SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int i = -1;
                    final String testcd = rs.getString("TESTCD");
                    for (int j = 0; j < testcds.length; j++) {
                        if (null != testcds[j] && testcds[j].equals(testcd)) {
                            i = j;
                        }
                    }
                    if (-1 == i) {
                        continue;
                    }
                    final TestItem testitem = new TestItem();
                    testitem._testcd = testcd;
                    testitem._testitemname = rs.getString("TESTITEMNAME");
                    testitem._testitemabbv1 = rs.getString("TESTITEMABBV1");
                    testitem._sidouinput = rs.getString("SIDOU_INPUT");
                    testitem._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                    testitem._semester = rs.getString("SEMESTER");
                    testitem._dateRange = new DateRange(testitem._testcd, testitem._testitemname, rs.getString("SDATE"), rs.getString("EDATE"));
                    testitem._printScore = "1".equals(rs.getString("PRINT"));
                    testitem._scoreDivName = rs.getString("SCORE_DIV_NAME");
                    testitems[i] = testitem;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (int i = 0; i < testcds.length; i++) {
                final String testcd = testcds[i];
                if (null == testitems[i]) {
                    log.warn("TESTITEM_MST_COUNTFLG_NEW_GCM_SDIVがない: " + testcd);
                }
            }
            return testitems;
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

        static class FormB extends Form {

            void init(final DB2UDB db2, final List testcdList) {
                _testcds = param()._testcds;
                //(testcdList, 6, param()._testcds);
                _testItems = getTestItems(db2, param(), _testcds);
                _attendRanges = new DateRange[] {param().getSemesterRange("1"), param().getSemesterRange("2"), param().getSemesterRange("3"), param().getSemesterRange(SEMEALL)};
                final int count = 6;
                _attendSubclassRanges = new DateRange[count];
                for (int i = 0; i < _testItems.length; i++) {
                    if (null != _testItems[i]) {
                        _attendSubclassRanges[i] = _testItems[i]._dateRange;
                    }
                }
                if (null != param().getSemester(SEMEALL)) {
                    final Semester seme9 = param().getSemester(SEMEALL);
                    _attendSubclassRanges[count - 1] = seme9._dateRange;
                }
                initDebug();
            }

            public void print(final Vrw32alp svf, final Student student) {
                final String form = "KNJD186C.frm";
                svf.VrSetForm(form, 4);

                svf.VrsOut("HR_NAME", StringUtils.defaultString(param()._gradeName) + "年" + StringUtils.defaultString(student._hrClassName1) + "組" + student.getAttendNo()); // 年組番
                svf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 30 ? "2" : "1"), student._name); // 氏名

                printBehavior(svf, student);
                printProficiency(svf, student);
                printAttend(svf, student);
                printCommunication(svf, student);
                printMoral(svf, student);
                printScoreRank(svf, student);
            }

            private void printScoreRank(final Vrw32alp svf, final Student student) {
                final String[] sfx1 = {"1_1", "1_2", "2_1", "2_2", "3_1", "9"};

                final Map subMap = new HashMap(student._subclassMap);
                final int maxSubclass = 9;
                subMap.remove(SUBCLASSCD333333);
                subMap.remove(SUBCLASSCD555555);
                final List subclassList = new ArrayList(subMap.values());
                Collections.sort(subclassList);

                final Map testitemMap = getTestItemMap();
                for (int i = 0; i < param()._testcds.length; i++) {
                    final String sfx = sfx1[i];
                    final TestItem testitem = (TestItem) testitemMap.get(param()._testcds[i]);
                    if (null == testitem) {
                        continue;
                    }
                    svf.VrsOut("TEST_ITEM_NAME" + sfx, testitem._testitemabbv1); // テスト項目名
                    if (!"9".equals(sfx)) {
                        // CS
                        svf.VrsOut("CS_SCORE" + sfx, "****"); // 得点
                    } else {
                        final String csScore = (String) student._csKadaiClassScore.get("90");
                        svf.VrsOut("CS_SCORE" + sfx, csScore); // 得点
                    }
                }

                BigDecimal totalSick = new BigDecimal(0);
                for (int i = 0; i < param()._testcds.length; i++) {
                    final TestItem testitem = (TestItem) testitemMap.get(param()._testcds[i]);
                    if (null == testitem) {
                        continue;
                    }
                    final boolean isPrint = Integer.parseInt(testitem._semester) <= Integer.parseInt(param()._semester) && param()._testcds.length - 1 != i;
                    // 二重をさけるため学年末を除く
                    if (!isPrint) {
                        continue;
                    }

                    int count = 0;
                    for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                        if (count >= maxSubclass) {
                            break;
                        }
                        count += 1;
                        final SubClass subClass = (SubClass) it.next();
                        if (SUBCLASSCD999999.equals(subClass._mst._subclasscode)) {
                            continue;
                        }
                        final String[] split = StringUtils.split(subClass._mst._subclasscode, "-");
                        if (null != split && split.length >= 4 && param()._d026List.contains(split[3])) {
                            continue;
                        }

                        final SubclassAttendance sa = subClass.getAttendance(param()._testcds.length - 1 == i ? SEMEALL : testitem._testcd);
                        // log.debug(" sa = " + sa);
                        if (null != sa && null != sa._sick) {
                            totalSick = totalSick.add(sa._sick);
                        }
                    }
                }
                // CS
                svf.VrsOut("CS_ABSENCE" + "9", getAbsentStr(param(), totalSick, true)); // 欠時

                for (int i = 0; i < param()._testcds.length; i++) {
                    final String sfx = sfx1[i];
                    final TestItem testitem = (TestItem) testitemMap.get(param()._testcds[i]);
                    if (null == testitem) {
                        continue;
                    }
                    final boolean isPrint = Integer.parseInt(testitem._semester) <= Integer.parseInt(param()._semester);
                    if (!isPrint) {
                        continue;
                    }

                    final SubClass subClass999999 = student.getSubClass(SUBCLASSCD999999);
                    if (null != subClass999999) {
                        final Score score999999 = subClass999999.getScore(param()._testcds[i]);
                        if (null != score999999) {
                            // 学年順位
                            if (TESTCD_GAKUNEN_HYOTEI.equals(testitem._testcd)) {
                            	// 評定の順位は表示しない
                            } else {
                                // 合計点
                                svf.VrsOut("TOTAL_SCORE" + sfx, score999999._score); // 得点
                                svf.VrsOut("AVERAGE_SCORE" + sfx, sishaGonyu(score999999._avg)); // 得点
                                final Rank rank = getGroupDivRank(score999999);
                                // 平均
                                svf.VrsOut("TOTAL_AVERAGE" + sfx, sishaGonyu(rank._avg)); // 平均
                                svf.VrsOut("AVERAGE_AVERAGE" + sfx, sishaGonyu(rank._avgAvg)); // 平均
                            	svf.VrsOut("RANK" + sfx, rank.getRank(param())); // 順位
                            }
                        }
                    }

                    BigDecimal sick = new BigDecimal(0);
                    int count = 0;
                    for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                        if (count >= maxSubclass) {
                            break;
                        }
                        count += 1;
                        final SubClass subClass = (SubClass) it.next();
                        if (SUBCLASSCD999999.equals(subClass._mst._subclasscode)) {
                            continue;
                        }
                        final String[] split = StringUtils.split(subClass._mst._subclasscode, "-");
                        if (null != split && split.length >= 4 && param()._d026List.contains(split[3])) {
                            continue;
                        }

                        final SubclassAttendance sa = subClass.getAttendance(param()._testcds.length - 1 == i ? SEMEALL : testitem._testcd);
                        // log.debug(" sa = " + sa);
                        if (null != sa && null != sa._sick) {
                            sick = sick.add(sa._sick);
                        }
                    }

                    // CS
                    svf.VrsOut("CS_ABSENCE" + sfx, getAbsentStr(param(), sick, true)); // 欠時

                    if ("9".equals(sfx)) {
                        // 学年評定の欄は3_1と同じ
                    } else {
                        final String remarksfx = String.valueOf(i + 1);
                        String[] token = null;
                        if ("3_1".equals(sfx)) {
                            token = get_token(student._totalStudyAct, 20, 3);
                        }
                        if (null != token) {
                            for (int ti = 0; ti < token.length; ti++) {
                                svf.VrsOut("REMARK" + remarksfx + "_" + String.valueOf(ti + 1), token[ti]); // 備考
                            }
                        }
                    }
                }

                int count = 0;
                for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                    if (count >= maxSubclass) {
                        break;
                    }
                    count += 1;
                    final SubClass subClass = (SubClass) it.next();
                    if (SUBCLASSCD999999.equals(subClass._mst._subclasscode)) {
                        continue;
                    }
                    final String[] split = StringUtils.split(subClass._mst._subclasscode, "-");
                    if (null != split && split.length >= 4 && param()._d026List.contains(split[3])) {
                        log.info(" not print subclass = " + subClass._mst._subclasscode);
                        continue;
                    }
                    svf.VrsOut("SUBJECT1", subClass._mst._classabbv); // 教科名
                    for (int i = 0; i < param()._testcds.length; i++) {
                        final String sfx = sfx1[i];
                        final TestItem ti = (TestItem) testitemMap.get(param()._testcds[i]);
                        if (null == ti) {
                            continue;
                        }
                        final boolean isPrint = Integer.parseInt(ti._semester) <= Integer.parseInt(param()._semester);
                        if (!isPrint) {
                            continue;
                        }

                        final Score score = subClass.getScore(ti._testcd);
                        String scoreStr = "****";
                        if (null != score) {
                            if (null != score._score) {
                                scoreStr = score._score;
                            }
                            svf.VrsOut("AVERAGE" + sfx, sishaGonyu(getGroupDivRank(score)._avg)); // 平均
                        }
                        svf.VrsOut("SCORE" + sfx, scoreStr); // 得点
                    }

                    for (int i = 0; i < param()._testcds.length; i++) {
                        final String sfx = sfx1[i];
                        final TestItem ti = (TestItem) testitemMap.get(param()._testcds[i]);
                        if (null == ti) {
                            continue;
                        }
                        final boolean isPrint = Integer.parseInt(ti._semester) <= Integer.parseInt(param()._semester) || param()._testcds.length - 1 == i;
                        if (!isPrint) {
                            continue;
                        }

                        final SubclassAttendance sa = subClass.getAttendance(param()._testcds.length - 1 == i ? SEMEALL : ti._testcd);
                        // log.debug(" sa = " + sa);
                        if (null != sa) {
                            svf.VrsOut("ABSENCE" + sfx, getAbsentStr(param(), sa._sick, true)); // 欠時
                        }
                    }

                    final List kantenLine = getKantenLineList(subClass._mst._subclasscode, 14);
                    for (int sfi = 0; sfi < Math.min(11, kantenLine.size()); sfi++) {
                        svf.VrsOut("VIEW1_" + String.valueOf(sfi + 1), (String) kantenLine.get(sfi)); // 評価の観点
                    }
                    svf.VrEndRecord();
                }
            }

            private List getKantenLineList(final String subclasscd, final int keta) {

                final List kantenList = getMappedList(param()._subclasscdKantenListMap, subclasscd);
                final List rtn = new ArrayList();
                for (int i = 0; i < kantenList.size(); i++) {
                    final String kanten = (String) kantenList.get(i);
                    final String[] token = get_token(kanten, keta - 2, 10);
                    if (null == token) {
                        rtn.add("");
                        continue;
                    }
                    for (int j = 0; j < token.length; j++) {
                        if (StringUtils.isBlank(token[j])) {
                            break;
                        }
                        rtn.add((j == 0 ? "・" : "　") + token[j]);
                    }
                }
                return rtn;
            }

            private void printCommunication(final Vrw32alp svf, final Student student) {
                for (int j = 1; j <= 3; j++) {
                    final int line = j;
                    final String semester = String.valueOf(j);
                    final String[] data = {(String) student._hreportremarkMap.get("TOTALSTUDYTIME" + semester)
                                           ,(String) student._hreportremarkMap.get("REMARK1" + semester)
                                           ,(String) student._hreportremarkMap.get("REMARK2" + semester)
                                           };
                    for (int k = 0; k < data.length; k++) {
                        final String[] token = get_token(data[k], 50, 2);
                        for (int i = 0; i < token.length; i++) {
                            svf.VrsOutn("COMM" + String.valueOf(k + 1) + "_" + String.valueOf(i + 1), line, token[i]); // 通信欄
                        }
                    }
                }
            }

            private void printBehavior(final Vrw32alp svf, final Student student) {
                final int maxLine = 10;
                final List codes = new ArrayList(student._behaviorMap.keySet());
                for (int j = 0; j < maxLine; j++) {
                    final int line = j + 1;
                    if(codes.size() <= j) continue;
                    final String code = (String) codes.get(j);

                    final Behavior b = (Behavior) student._behaviorMap.get(code);
                    if (null == b) {
                        continue;
                    }
                    svf.VrsOutn("BEHAVIOR_ITEM" + (getMS932ByteLength(b._codename) > 16 ? "2" : "1"), line, b._codename); // 行動の記録項目

                    if (getMS932ByteLength(b._viewname) > 64) {
                        svf.VrsOutn("BEHAVIOR3_1", line, b._viewname); // 行動の記録項目
                    } else {
                        //svf.VrsOutn("BEHAVIOR" + (getMS932ByteLength(b._viewname) > 50 ? "2" : "1"), line, b._viewname); // 行動の記録項目
                        svf.VrsOutn("BEHAVIOR" + (true ? "2" : "1"), line, b._viewname); // 行動の記録項目
                    }

                    for (final Iterator it = b._semesterRecordMap.keySet().iterator(); it.hasNext();) {
                        final String semester = (String) it.next();
                        final String record = (String) b._semesterRecordMap.get(semester);
                        if (null != record) {
                            svf.VrsOutn("BEHAVIOR_MARK" + semester, line, (String) param()._d036Map.get(record)); // 行動の記録項目
                        }
                    }
                }
            }

            private void printAttend(final Vrw32alp svf, final Student student) {
                final String[] semesters = {"1", "2", "3", "9"};
                for (int j = 0; j < semesters.length; j++) {
                    final int line = j + 1;
                    if ("9".equals(semesters[j]) || Integer.parseInt(semesters[j]) <= Integer.parseInt(param()._semester)) {
                        final Attendance att = (Attendance) student._attendMap.get(semesters[j]);
                        if (null != att) {
                            svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                            svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend + att._mourning)); // 出停・忌引
                            svf.VrsOutn("ABSENT", line, String.valueOf(att._absent)); // 欠席日数
                            svf.VrsOutn("PRESENT", line, String.valueOf(att._present)); // 出席日数
                            svf.VrsOutn("LATE", line, String.valueOf(att._late)); // 遅刻回数
                            svf.VrsOutn("EARLY", line, String.valueOf(att._early)); // 早退回数
                        }
                    }
                }
            }

            private void printProficiency(final Vrw32alp svf, final Student student) {
                final Map testnames = new TreeMap();
                final Map subclassnames = new TreeMap();
                final Set subclasscds = new TreeSet();
                final Map scoreMap = new HashMap();
                for (final Iterator it = student._proficiencySubclassScoreList.iterator(); it.hasNext();) {
                    final ProficiencyScore pscore = (ProficiencyScore) it.next();
                    testnames.put(pscore._semtestcd, pscore._proficiencyname1);
                    scoreMap.put(pscore._semtestcd + ":" + pscore._proficiencySubclassCd, pscore);
                    if (!SUBCLASSCD999999.equals(pscore._proficiencySubclassCd)
//                        && !SUBCLASSCD333333.equals(pscore._proficiencySubclassCd)
//                        && !SUBCLASSCD555555.equals(pscore._proficiencySubclassCd)
                        ) {
                        subclasscds.add(pscore._proficiencySubclassCd);
                        subclassnames.put(pscore._proficiencySubclassCd, pscore._subclassName);
                    }
                }

                final List subclasscdList = new ArrayList(subclasscds);
                final int maxMockSubclass = 5;
                for (int i = 0; i < Math.min(subclasscdList.size(), maxMockSubclass); i++) {
                    final String subclasscd = (String) subclasscdList.get(i);
                    final String col = String.valueOf(i + 1);
                    svf.VrsOut("MOCK_SUBJECT" + col, (String) subclassnames.get(subclasscd)); // 教科名
                }
                svf.VrsOut("MOCK_SUBJECT6", "合計点"); // 教科名
                svf.VrsOut("MOCK_SUBJECT7", "平均点"); // 教科名

                final int maxTest = 5;
                int ti = 0;
                int testCnt = 0;
                for (final Iterator itt = testnames.keySet().iterator(); itt.hasNext();) {
                    final String testcd = (String) itt.next();
                    final String testname = (String) testnames.get(testcd);
                    final int line = ti + 1;
                    testCnt ++;
                    if((testCnt + 3) <= testnames.size()) continue;
                    svf.VrsOutn("MOCK_NAME" + (getMS932ByteLength(testname) > 16 ? "2" : "1"), line, testname); // 実力テスト名称


                    final ProficiencyScore pscore999999 = (ProficiencyScore) scoreMap.get(testcd + ":" + SUBCLASSCD999999);
                    if (null != pscore999999) {

                        svf.VrsOutn("MOCK_SCORE" + "6", line, pscore999999._score); // 実力得点
                        svf.VrsOutn("MOCK_SCORE" + "7", line, sishaGonyu(pscore999999._avg)); // 実力平均
                        Rank rank = pscore999999._gradeRank;
                        if (null != rank) {
                            svf.VrsOutn("MOCK_AVERAGE" + "6", line, sishaGonyu(rank._avg)); // 実力得点
                            svf.VrsOutn("MOCK_AVERAGE" + "7", line, sishaGonyu(rank._avgAvg)); // 実力平均平均
                            svf.VrsOutn("MOCK_RANK", line, rank.getRank(param())); // 実力順位
                        }
                    }

                    for (int i = 0; i < Math.min(subclasscdList.size(), maxMockSubclass); i++) {
                        final String subclasscd = (String) subclasscdList.get(i);
                        final String col = String.valueOf(i + 1);

                        final ProficiencyScore pscore = (ProficiencyScore) scoreMap.get(testcd + ":" + subclasscd);
                        if (null == pscore) {
                            continue;
                        }
                        if (null == subclassnames.get(subclasscd)) {
                            continue;
                        }
                        svf.VrsOutn("MOCK_SCORE" + col, line, pscore._score); // 実力得点
                        Rank rank = pscore._gradeRank;
                        if (null != rank) {
                            svf.VrsOutn("MOCK_AVERAGE" + col, line, sishaGonyu(rank._avg)); // 実力平均
                        }
                    }

                    ti++;
                    if (ti == maxTest) {
                        break;
                    }
                }
            }

            private void printMoral(final Vrw32alp svf, final Student student) {
                boolean ketaFlg = true;
                int keta = 32; //桁数 初期値：32
                int gyo  =  2; //行数 初期値： 2

                if ("".equals(student._dataInputSize)) ketaFlg = false;

                String cutwk[] = StringUtils.split(student._dataInputSize, '*');
                if (cutwk != null && cutwk.length < 2) ketaFlg = false;

                if(ketaFlg == true) {
                    keta = Integer.parseInt(cutwk[0]);
                    gyo  = Integer.parseInt(cutwk[1]);
                }

                //道徳
                VrsOutnRenban(svf, "MORAL", knjobj.retDividString(student._moral, keta * 2, gyo));
            }

            protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
                if (null != list) {
                    for (int i = 0 ; i < list.size(); i++) {
                        svf.VrsOutn(field, i + 1, (String) list.get(i));
                    }
                }
            }

        }

    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74345 $ $Date: 2020-05-15 19:32:44 +0900 (金, 15 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {

        final String[] _testcds = {"1010201", "1020201", "2010201", "2020201", "3020201", "9990009"};

        final String _year;
        final String _semester;
        final String _ctrlSeme;

        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _schoolKind;
        /** 出欠集計日付 */
        final String _sdate;
        final String _edate;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;

        final String _groupDiv; // 総合順位出力 1:学年 4:学科 3:コース
        final String _rankDiv; // 順位の基準点 1:総合点 2:平均点

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private final Form _form;

        /** 端数計算共通メソッド引数 */
        private String _periodInState;
        private Map _semesterMap;
        private Map _subclassMst;
//        private Map _creditMst;
//        private Map _recordMockOrderSdivDatMap;

        private KNJSchoolMst _knjSchoolMst;

//        private int _gradCredits;  // 卒業認定単位数

        private String _avgDiv;
//        private String _d054Namecd2Max;
//        private String _sidouHyoji;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _hrJobName;
//        final String _h508Name1;
        private boolean _isNoPrintMoto;
        final Map _attendParamMap;
        final Map _d036Map;

        final Map _subclasscdKantenListMap;
        final String _gradeName;
        List _d026List = Collections.EMPTY_LIST;
        final String _moralSeme;
        final String _d008Namecd1;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HRCLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _edate = KNJ_EditDate.H_Format_Haifun(request.getParameter("EDATE"));
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _groupDiv = "3"; // request.getParameter("GROUP_DIV");
            _rankDiv = "1"; // request.getParameter("RANK_DIV");

            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _form = new Form.FormB();
            _form._param = this;

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }


            _definecode = createDefineCode(db2);
            setSubclassMst(db2);
            loadNameMstD026(db2, _year);
            loadNameMstD016(db2);
            _d036Map = loadNameMstD036(db2);
            _gradeName = getGradeName(db2, _year, _grade);

            _subclasscdKantenListMap = getSubclasscdKantenListMap(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _moralSeme = getMoralSeme(db2, _year, _semester);

    		_schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";

        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            return definecode;
        }

        private Map loadNameMstD036(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new HashMap();
            final String sql = "SELECT NAME1, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D036' AND NAME1 IS NOT NULL ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAME1"), rs.getString("NAMESPARE1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namespare1 = rs.getString("NAMESPARE1");
                    if ("Y".equals(namespare1)) _isNoPrintMoto = true;
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private Semester getSemester(final String semester) {
            return (Semester) _semesterMap.get(semester);
        }

        private DateRange getSemesterRange(final String semester) {
            Semester s = getSemester(semester);
            if (null == s) {
                return null;
            }
            return s._dateRange;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
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

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ");
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
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
            _hrJobName = StringUtils.defaultString(_hrJobName, "担任");
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null, false, false, new Integer(999999), new Integer(999999));
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
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), isSaki, isMoto, Integer.valueOf(rs.getString("CLASS_SHOWORDER")), Integer.valueOf(rs.getString("SUBCLASS_SHOWORDER")));
                    _subclassMst.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public List getTestcdList() {
            return Arrays.asList(_testcds);
        }

        public Map getSubclasscdKantenListMap(final DB2UDB db2) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   T1.VIEWCD, ");
                stb.append("   T1.VIEWNAME ");
                stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
                stb.append(" INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.GRADE = T1.GRADE ");
                stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     AND T2.VIEWCD = T1.VIEWCD ");
                stb.append(" WHERE ");
                stb.append("     T1.GRADE = '" + _grade + "'  ");
                stb.append("     AND T2.YEAR = '" + _year + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CLASSCD ");
                stb.append("     , T1.SCHOOL_KIND ");
                stb.append("     , T1.CURRICULUM_CD ");
                stb.append("     , T1.SUBCLASSCD ");
                stb.append("     , T1.VIEWCD ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String viewname = rs.getString("VIEWNAME");

                    getMappedList(rtn, subclasscd).add(viewname);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            String rtn = null;
            final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn = getString(row, "GRADE_NAME1");
            }
            return rtn;
        }

        /*
         *  道徳所見取得用学期　「学年末」は「最終学期」を印字する。それ以外は「指定学期」
         */
        private String getMoralSeme(final DB2UDB db2, final String year, final String semester) {
            if (!"9".equals(semester)) return semester;

            String rtn = null;
            final String sql = "SELECT MAX(SEMESTER) AS MAX_SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER <> '" + semester + "'";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn = getString(row, "MAX_SEMESTER");
            }
            return rtn;
        }

        private void loadNameMstD026(final DB2UDB db2, final String year) {

            final StringBuffer sql = new StringBuffer();
            final String field;
            if ("1".equals(_semester)) {
                field = "ABBV1";
            } else if ("2".equals(_semester)) {
                field = "ABBV2";
            } else {
                field = "ABBV3";
            }
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'D026' AND " + field + " = '1' OR NAMESPARE1 = '1' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _d026List.add(rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }
}
