// kanji=漢字
/*
 * $Id: KNJD184I.java 76137 2020-08-21 08:36:51Z ishimine $
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD184I {

    private static final Log log = LogFactory.getLog(KNJD184I.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";

    private static final String COMMUNICATION = "COMMUNICATION";
    private static final String TOTALSTUDYTIME = "TOTALSTUDYTIME";
    private static final String REMARK1 = "REMARK1";
    private static final String ATTENDREC_REMARK = "ATTENDREC_REMARK";

    private static final String STATUS = "STATUS";
    private static final String VIEWFLG = "VIEWFLG";
    private static final String ASSESS_SHOW1 = "ASSESS_SHOW1";
    private static final String SCORE = "SCORE";

    private static final String SLASH_YEAR = "2020";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                try {
                    db2.commit();
                    db2.close();
                } catch (Exception ex) {
                    log.error("db close error!", ex);
                }
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        final List<Student> studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }

        load(param, db2, studentList);

        Form form = new Form();

        for (final Student student : studentList) {
            log.info(" schregno = " + student._schregno);

            form.printHyoshi(db2, svf, student, param);
            form.printMain(svf, student, param);
        }

        _hasData = true;
    }

    private void load(final Param param, final DB2UDB db2, final List studentList) {
        Attendance.load(db2, param, studentList);
        ViewClass.setViewClassList(db2, param, studentList);
        BehaviorSemesDat.setBehaviorSemesDatMap(db2, param, studentList);
        Student.setHReportRemarkDat(db2, param, studentList);
    }

    private static int getMS932ByteLength(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private static String hankakuToZenkaku(final String str) {
        if (null == str) {
            return null;
        }
        final String[] nums = new String[]{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            final String s = String.valueOf(str.charAt(i));
            if (NumberUtils.isDigits(s)) {
                final int j = Integer.parseInt(s);
                stb.append(nums[j]);
            } else {
                stb.append(s);
            }
        }
        return stb.toString();
    }

    private static String trim(final String s) {
        if (null == s) {
            return s;
        }
        int st = 0, ed = s.length();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == ' ' || ch == '　') {
                st = i + 1;
            } else {
                break;
            }
        }
        for (int i = s.length() - 1; i >= 0; i--) {
            final char ch = s.charAt(i);
            if (ch == ' ' || ch == '　') {
                ed = i;
            } else {
                break;
            }
        }
        if (st < ed) {
            return s.substring(st, ed);
        }
        return s;
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

    private static String zeroBlank(final int v) {
        if (v == 0) {
            return "";
        }
        return String.valueOf(v);
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static void svfVrsOutnKurikaeshi(final Vrw32alp svf, final String field, final List data) {
        if (null == field || null == data) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            svf.VrsOutn(field, i + 1, (String) data.get(i));
        }
    }

    private static void svfVrsOutKurikaeshi(final Vrw32alp svf, final String[] field, final List data) {
        if (null == field || null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field.length, data.size()); i++) {
            svf.VrsOut(field[i], (String) data.get(i));
        }
    }

    private static List<String> getTokenList(final String strx, final KNJPropertiesShokenSize size) {
        final List<String> tokenList = getTokenList(strx, size.getKeta());
        if (tokenList.size() > size._gyo) {
            return tokenList.subList(0, size._gyo);
        }
        return tokenList;
    }

    private static List<String> getTokenList(final String strx, final int keta) {
        return KNJ_EditKinsoku.getTokenList(strx, keta);
    }

    private static List<String> getCenterizedCharList(final String name, final int count) {
        final StringBuffer stb = new StringBuffer();
        if (null != name) {
            final int spc1 = (count - name.length()) / 2;
            stb.append(StringUtils.repeat(" ", spc1));
            stb.append(name);
            stb.append(StringUtils.repeat(" ", count - stb.length()));
        }
        final List<String> list = new ArrayList();
        for (int i = 0; i < stb.length(); i++) {
            list.add(String.valueOf(stb.charAt(i)));
        }
        return list;
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        String _name;
        final String _gradeCd;
        final String _gradeName1;
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
        final String _entyear;
        String _birthday;
        final Map _behaviorSemesDatMap = new TreeMap();
        final Map _semesterHreportremarkDat = new HashMap();
        final Map _semesterHreportremarkDetailDat = new HashMap();

        final List<ViewClass> _viewClassList = new ArrayList();
        private boolean _hasHyokaFuka;

        Student(final String schregno, final String gradeCd, final String gradeName1, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
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
        }

        private String getHrName(final Param param) {
            try {
                final String grade = "第" + String.valueOf(Integer.parseInt(param._gradeCd)) + "学年";
                final String hrclass = StringUtils.defaultString(_hrClassName1) + "組";
                return hankakuToZenkaku(grade + hrclass);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        private String getAttendNo(final Param param) {
            try {
                String num = String.valueOf(Integer.parseInt(_attendno));
                if (num.length() > 1) {
                    return num;
                }
                return hankakuToZenkaku(num);
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
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,BASE.REAL_NAME ");
            stb.append("            ,NAMESD.SCHREGNO AS NAME_SETUP ");
            stb.append("            ,REGDH.HR_NAME ");
            stb.append("            ,STFM.STAFFNAME ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,REGDG.GRADE_CD ");
            stb.append("            ,REGDG.GRADE_NAME1 ");
            stb.append("            ,REGDH.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN TRANSF.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("            ,BASE.BIRTHDAY ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT TRANSF ON TRANSF.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND TRANSF.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN TRANSF.TRANSFER_SDATE AND TRANSF.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT NAMESD ON NAMESD.SCHREGNO = REGD.SCHREGNO AND NAMESD.DIV = '03' ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD ");
            stb.append("                  AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            if ("1".equals(param._disp)) {
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
                stb.append("         AND REGD.SCHREGNO IN " + whereIn(true, param._categorySelected));
            }
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE ");
            stb.append("       , REGD.HR_CLASS ");
            stb.append("       , REGD.ATTENDNO ");
            final String sql = stb.toString();
            if (param._isOutputDebug) {
                log.info(" student sql = " + sql);
            }

            final List students = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
                final String staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "GRADE_CD"), KnjDbUtils.getString(row, "GRADE_NAME1"), KnjDbUtils.getString(row, "HR_NAME"), staffname, attendno, KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "COURSECD"), KnjDbUtils.getString(row, "MAJORCD"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "MAJORNAME"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "ENT_YEAR"));
                student._name = null != KnjDbUtils.getString(row, "NAME_SETUP") ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME");
                student._birthday = KnjDbUtils.getString(row, "BIRTHDAY");
                students.add(student);
            }
            return students;
        }

        /**
         * 文字列の配列を、SQL文where節のin句で使える文字列に変換する。
         * 例:<br/>
         * <pre>
         * whereIn(*, null)                         = null
         * whereIn(*, [])                           = null
         * whereIn(false, [null])                   = "(null)"
         * whereIn(true, [null])                    = null
         * whereIn(*, ["can't"])                    = "('can''t')"
         * whereIn(*, ["abc", "don't"])             = "('abc', 'don''t')"
         * whereIn(false, ["abc", null, "xyz"])     = "('abc', null, 'xyz')"
         * whereIn(true, ["abc", null, "xyz"])      = "('abc', 'xyz')"
         * </pre>
         * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
         * @param array 文字列の配列
         * @return 変換後の文字列
         */
        public static String whereIn(final boolean skipNull, final String[] array) {
            if (null == array || 0 == array.length) {
                return null;
            }

            final StringBuffer sb = new StringBuffer();
            int n = 0;
            for (int i = 0; i < array.length; i++) {
                if (null == array[i] && skipNull) {
                    continue;
                }

                if (0 == n) { sb.append("("); }
                if (0 != n) { sb.append(", "); }

                if (null == array[i]) {
                    sb.append(String.valueOf(array[i])); // "null"
                } else {
                    sb.append('\'');
                    sb.append(StringEscapeUtils.escapeSql(array[i]));
                    sb.append('\'');
                }
                //--
                n++;
            }

            if (0 == n) {
                return null;
            }

            sb.append(")");
            return sb.toString();
        }

        public static void setHReportRemarkDat(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.* ");
                stb.append(" FROM ");
                stb.append("     HREPORTREMARK_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SCHREGNO = ? ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._semesterHreportremarkDat.clear();
                    final Map keyMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}), "SEMESTER");
                    student._semesterHreportremarkDat.putAll(keyMap);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER || DIV || CODE AS KEY, T1.* ");
                stb.append(" FROM ");
                stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND (T1.SEMESTER <= '" + param._semester + "' AND DIV = '01' AND CODE = '01' OR T1.SEMESTER = '" + SEMEALL + "' AND DIV = '02' AND CODE = '01' OR T1.SEMESTER = '" + SEMEALL + "' AND DIV = '01' AND CODE = '01') ");
                stb.append("     AND T1.SCHREGNO = ? ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._semesterHreportremarkDetailDat.clear();
                    student._semesterHreportremarkDetailDat.putAll(KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}), "KEY"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }
    }

    private static class BehaviorSemesDat {
        final String _code;
        final String _codename;
        final String _viewname;
        final Map _semesterRecordMap = new HashMap();

        public BehaviorSemesDat(
            final String code,
            final String codename,
            final String viewname
        ) {
            _code = code;
            _codename = codename;
            _viewname = viewname;
        }

        public static void setBehaviorSemesDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.CODE, ");
                stb.append("     T1.CODENAME, ");
                stb.append("     T1.VIEWNAME, ");
                stb.append("     L1.SCHREGNO, ");
                stb.append("     L1.SEMESTER, ");
                stb.append("     L1.RECORD, ");
                stb.append("     L2.NAME1 ");
                stb.append(" FROM BEHAVIOR_SEMES_MST T1 ");
                stb.append(" LEFT JOIN BEHAVIOR_SEMES_DAT L1 ON ");
                stb.append("    L1.YEAR = T1.YEAR ");
                stb.append("    AND L1.SEMESTER <= '" + param._semester + "' ");
                stb.append("    AND L1.SCHREGNO = ? ");
                stb.append("    AND L1.CODE = T1.CODE ");
                stb.append(" LEFT JOIN NAME_MST L2 ON ");
                stb.append("    L2.NAMECD1 = 'D036' ");
                stb.append("    AND L2.NAMECD2 = L1.RECORD ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
                stb.append("    AND T1.GRADE = '" + param._grade + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CODE ");
                stb.append("   , L1.SEMESTER ");

                final String sql = stb.toString();
                if (param._isOutputDebug) {
                    log.info(" behavior sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._behaviorSemesDatMap.clear();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();
                        final String code = KnjDbUtils.getString(row, "CODE");
                        if (null == student._behaviorSemesDatMap.get(code)) {
                            final String codename = KnjDbUtils.getString(row, "CODENAME");
                            final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                            final BehaviorSemesDat bsd = new BehaviorSemesDat(code, codename, viewname);
                            student._behaviorSemesDatMap.put(code, bsd);
                        }
                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
                        if (null != semester) {
                            final String record = KnjDbUtils.getString(row, "RECORD");
//        					final String name1 = KnjDbUtils.getString(row, "NAME1");
                            final BehaviorSemesDat bsd = (BehaviorSemesDat) student._behaviorSemesDatMap.get(code);
                            bsd._semesterRecordMap.put(semester, record);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        public String toString() {
            return "BehaviorSemesDat(" + _code + ", " + _viewname + ", " + _semesterRecordMap + ")";
        }
    }

    private static class Attendance {
        final String _semester;
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
        Attendance(
                final String semester,
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _semester = semester;
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList
        ) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                if (SLASH_YEAR.equals(param._year)) {
                    // 2020年度は時間割を集計対象外とする。
                    // * そもそも時間割は作成していないが
                    // 2020年度は学期マスタ3学期制（中高併用）でも2学期で運用する。従来2学期の8月分、9月分を1学期の累積データとして作成する。
                    param._attendParamMap.put("notAcuumulateSchedule", "1");
                }

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        param._sdate,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final Attendance attendance = new Attendance(
                                KnjDbUtils.getString(row, "SEMESTER"),
                                KnjDbUtils.getInt(row, "LESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),
                                KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),
                                KnjDbUtils.getInt(row, "SICK", zero).intValue(),
                                KnjDbUtils.getInt(row, "SICK_ONLY", zero).intValue(),
                                KnjDbUtils.getInt(row, "NOTICE_ONLY", zero).intValue(),
                                KnjDbUtils.getInt(row, "NONOTICE_ONLY", zero).intValue(),
                                KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                                KnjDbUtils.getInt(row, "LATE", zero).intValue(),
                                KnjDbUtils.getInt(row, "EARLY", zero).intValue(),
                                KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue()
                        );
                        student._attendMap.put(KnjDbUtils.getString(row, "SEMESTER"), attendance);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final String _classname;
        final String _subclassname;
        final List _viewList;

        final Map _viewcdSemesterStatDatMap = new HashMap();

        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv,
                final String classname,
                final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _electDiv = electDiv;
            _classname = classname;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public boolean hasViewCd(final String viewcd) {
            for (final Iterator it = _viewList.iterator(); it.hasNext();) {
                final String[] arr = (String[]) it.next();
                if (arr[0].equals(viewcd)) {
                    return true;
                }
            }
            return false;
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public String toString() {
            return "ViewClass(" + _subclasscd + ", " + _classname + ")";
        }

        public static void setViewClassList(final DB2UDB db2, final Param param, final List<Student> studentList) {
            final String sql = getViewClassSql(param);
            if (param._isOutputDebug) {
                log.info(" view class sql = " + sql);
            }

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Student student : studentList) {

                    student._viewClassList.clear();
                    student._hasHyokaFuka = false;

                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno, student._schregno})) {

                        final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String viewcd = KnjDbUtils.getString(row, "VIEWCD");

                        ViewClass viewClass = null;
                        for (final ViewClass viewClass0 : student._viewClassList) {
                            if (viewClass0._subclasscd.equals(subclasscd)) {
                                viewClass = viewClass0;
                                break;
                            }
                        }

                        if (null == viewClass) {
                            final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");
                            final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                            final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");

                            viewClass = new ViewClass(classcd, subclasscd, electDiv, classname, subclassname);
                            student._viewClassList.add(viewClass);
                        }

                        if (!viewClass.hasViewCd(viewcd)) {
                            final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                            viewClass.addView(viewcd, viewname);
                        }

                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
                        if (null == semester) {
                            continue;
                        }
                        final Map stat = getMappedMap(getMappedMap(viewClass._viewcdSemesterStatDatMap, viewcd), semester);
                        stat.put(STATUS, KnjDbUtils.getString(row, STATUS));
                        stat.put(VIEWFLG, KnjDbUtils.getString(row, VIEWFLG));
                        String assess = KnjDbUtils.getString(row, ASSESS_SHOW1);
                        if (null == assess) {
                            if (Integer.parseInt(semester) <= Integer.parseInt(param._semester) && "1".equals(KnjDbUtils.getString(row, VIEWFLG)) || "9".equals(param._semester) || SLASH_YEAR.equals(param._year) && "2".compareTo(param._semester) <= 0) {
                                assess = "※";
                                student._hasHyokaFuka = true;
                            }
                        }
                        stat.put(ASSESS_SHOW1, assess);
                        String score = KnjDbUtils.getString(row, SCORE);
                        if (null == score) {
                            if ("9".equals(param._semester) || SLASH_YEAR.equals(param._year) && "2".compareTo(param._semester) <= 0) {
                                score = "※";
                                student._hasHyokaFuka = true;
                            }
                        }
                        stat.put(SCORE, score);
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static String getViewClassSql(final Param param) {
//            final int gradeCdInt = Integer.parseInt(param._gradeCd);

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH JVIEW_SEME AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , SEME.SEMESTER ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = '" + param._year + "' ");
            stb.append("     WHERE ");
            stb.append("         T1.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , CLM.CLASSCD ");
            stb.append("   , VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SCLM.SUBCLASSORDERNAME2, SCLM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , VALUE(SCLM.ELECTDIV, '0') AS ELECTDIV ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , T1.VIEWNAME ");
            stb.append("   , L1.SEMESTER ");
            stb.append("   , REC.SCHREGNO ");
            stb.append("   , REC.STATUS ");
            stb.append("   , INP.VIEWFLG ");
            stb.append("   , PAT.PATTERN_CD ");
            stb.append("   , PDAT.ASSESS_SHOW1 ");
            stb.append("   , SDIV.SCORE ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST SCLM ON SCLM.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND SCLM.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND SCLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND SCLM.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN JVIEW_SEME L1 ON L1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L1.VIEWCD = T1.VIEWCD ");
            stb.append("         AND L1.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_INPUTSEQ_DAT INP ON INP.YEAR = T2.YEAR ");
            stb.append("         AND INP.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND INP.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND INP.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND INP.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("         AND INP.VIEWCD = T2.VIEWCD ");
            stb.append("         AND INP.SEMESTER = L1.SEMESTER ");
            stb.append("         AND INP.GRADE = T2.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT REC ON REC.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND REC.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND REC.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND REC.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND REC.VIEWCD = T2.VIEWCD ");
            stb.append("         AND REC.YEAR = T2.YEAR ");
            stb.append("         AND REC.SEMESTER = L1.SEMESTER ");
            stb.append("         AND REC.SCHREGNO = ? ");
            stb.append("     LEFT JOIN JVIEWSTAT_SUBCLASS_PATTERN_DAT PAT ON PAT.YEAR = T2.YEAR ");
            stb.append("         AND PAT.GRADE = T2.GRADE ");
            stb.append("         AND PAT.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND PAT.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND PAT.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND PAT.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("         AND PAT.VIEWCD = T2.VIEWCD ");
            stb.append("     LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT PDAT ON PDAT.YEAR = PAT.YEAR ");
            stb.append("         AND PDAT.SCHOOL_KIND = PAT.SCHOOL_KIND ");
            stb.append("         AND PDAT.PATTERN_CD = PAT.PATTERN_CD ");
            stb.append("         AND PDAT.ASSESSMARK = REC.STATUS ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SDIV ");
            stb.append("            ON SDIV.YEAR          = T2.YEAR ");
            stb.append("           AND SDIV.SEMESTER      = '9' ");
            stb.append("           AND SDIV.TESTKINDCD    = '99' ");
            stb.append("           AND SDIV.TESTITEMCD    = '00' ");
            stb.append("           AND SDIV.SCORE_DIV     = '09' ");
            stb.append("           AND SDIV.CLASSCD       = T2.CLASSCD ");
            stb.append("           AND SDIV.SCHOOL_KIND   = T2.SCHOOL_KIND ");
            stb.append("           AND SDIV.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("           AND SDIV.SUBCLASSCD    = T2.SUBCLASSCD ");
            stb.append("           AND SDIV.SCHREGNO      = ? ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param.SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(SCLM.ELECTDIV, '0'), ");
            stb.append("     VALUE(CLM.SHOWORDER3, -1), ");
            stb.append("     CLM.CLASSCD, ");
            stb.append("     VALUE(SCLM.SHOWORDER3, -1), ");
            stb.append("     SCLM.CLASSCD, ");
            stb.append("     SCLM.SCHOOL_KIND, ");
            stb.append("     SCLM.CURRICULUM_CD, ");
            stb.append("     SCLM.SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     L1.SEMESTER ");
            return stb.toString();
        }
    }

    private static class Form {

        final int FORM_DIV1 = 1;
        final int FORM_DIV2 = 2;
        final int FORM_DIV3 = 3;

        boolean hasdata = false;

        private void printHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {
            final boolean isLastSemester = Integer.parseInt(param._semester) == param._definecode.semesdiv || SLASH_YEAR.equals(param._year) && "2".equals(param._semester);

            String form = null;
            boolean printLeft = false;
            final int gradeCdInt = Integer.parseInt(student._gradeCd);
            if (gradeCdInt == 1 || gradeCdInt == 2) {
                if (isLastSemester) {
                    form = "KNJD184I_1_1_2.frm";
                    printLeft = true;
                } else {
                    form = "KNJD184I_1_1_1.frm";
                }
            } else {
                if (isLastSemester) {
                    if (gradeCdInt == 6) {
                        form = "KNJD184I_1_2_3.frm";
                    } else {
                        form = "KNJD184I_1_2_2.frm";
                        printLeft = true;
                    }
                } else {
                    form = "KNJD184I_1_2_1.frm";
                }
            }
            svf.VrSetForm(form, 1);

            if (printLeft) {
                final int keta = KNJ_EditEdit.getMS932ByteLength(param._cerifSchoolPrincipalName);
                svf.VrsOut("PRINCIPAL_NAME2_" + (keta <= 12 ? "1" : keta <= 16 ? "2" : "3"), param._cerifSchoolPrincipalName); // 校長名
                svf.VrsOut("SCHOOL_NAME2", param._cerifSchoolRemark3); // 学校名
                svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(param._gradeCd))); // 学年
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._descDate)); // 日付
                if (null != param._schoolstampFilePath) {
                    svf.VrsOut("SCHOOL_STAMP", param._schoolstampFilePath);
                }
            }

            final int keta1 = KNJ_EditEdit.getMS932ByteLength(trim(param._cerifSchoolPrincipalName));
            svf.VrsOut("PRINCIPAL_NAME1_" + (keta1 <= 12 ? "1" : keta1 <= 16 ? "2" : "3"), trim(param._cerifSchoolPrincipalName)); // 校長名

            final int keta2 = KNJ_EditEdit.getMS932ByteLength(student._staffName);
            svf.VrsOut("TEACHER_NAME" + (keta2 <= 12 ? "1" : keta2 <= 16 ? "2" : "3"), student._staffName); // 担任名

            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度"); // 年度
            if (null != param._schoollogoFilePath) {
                svf.VrsOut("SCHOOL_LOGO", param._schoollogoFilePath); //
            }
            if (null != param._schoolgoalFilePath) {
                svf.VrsOut("SCHOOL_GOAL", param._schoolgoalFilePath);
            }
            svf.VrsOut("SCHOOL_NAME", trim(param._cerifSchoolSchoolName)); // 学校名

            svf.VrsOut("NO", student.getAttendNo(param)); // 出席番号
            svf.VrsOut("HR_NAME", student.getHrName(param)); // 年組
            svf.VrsOut("NAME1", student._name); // 児童名

            svf.VrEndPage();
            hasdata = true;
        }

        private void printMain(final Vrw32alp svf, final Student student, final Param param) {
            int maxView = 0;
            //int maxComm = 0;
            boolean printTotalStudyTime = false;
            boolean printClub = false;
            int formDiv = 0;
            int moralKeta;
            final int gradeCdInt = Integer.parseInt(student._gradeCd);

            String form = null;
            if (gradeCdInt == 1 || gradeCdInt == 2) { // 1年、2年
                if (SLASH_YEAR.equals(param._year)) {
                    form = "KNJD184I_2_1_2020.frm";
                } else {
                    form = "KNJD184I_2_1.frm";
                }
                maxView = 19;
                //maxComm = 9;
                formDiv = FORM_DIV1;
                moralKeta = 60;
            } else if (gradeCdInt == 3 || gradeCdInt == 4) { // 3年、4年
                if (SLASH_YEAR.equals(param._year)) {
                    form = "KNJD184I_2_2_2020.frm";
                } else {
                    form = "KNJD184I_2_2.frm";
                }
                printTotalStudyTime = true;
                maxView = 12;
                //maxComm = 7;
                formDiv = FORM_DIV2;
                moralKeta = 60;
            } else if (gradeCdInt == 5 || gradeCdInt == 6) { // 5年、6年
                if (SLASH_YEAR.equals(param._year)) {
                    form = "KNJD184I_2_3_2020.frm";
                } else {
                    form = "KNJD184I_2_3.frm";
                }
                printTotalStudyTime = true;
                maxView = 10;
                //maxComm = 7;
                printClub = true;
                formDiv = FORM_DIV3;
                moralKeta = 60;
            } else {
                log.warn(" no form : " + gradeCdInt);
                return;
            }
            log.warn(" form = " + form);
            svf.VrSetForm(form, 4);

            final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
            svf.VrsOut("NAME1_" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 氏名
            svf.VrsOut("NAME2_" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 氏名

            for (int semei = 1; semei <= Integer.parseInt(param._semester); semei++) {
                final String semester = String.valueOf(semei);

                if (SLASH_YEAR.equals(param._year) && "3".equals(semester)) {
                    continue;
                }

                final Map hreportremarkDat = getMappedMap(student._semesterHreportremarkDat, semester);
                final Map htrainremarkDetailDatSeme = getMappedMap(student._semesterHreportremarkDetailDat, semester + "0101");

                if (printTotalStudyTime) {
                    svf.VrsOut("TOTAL_ACT_TITLE" + semester, KnjDbUtils.getString(htrainremarkDetailDatSeme, "REMARK1")); // 総学タイトル
                    final String totalStudyTime = KnjDbUtils.getString(hreportremarkDat, TOTALSTUDYTIME); // 総合的な学習 HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P
                    svfVrsOutnKurikaeshi(svf, "TOTAL_ACT" + semester, getTokenList(totalStudyTime, 60));
                }

                svf.VrsOut("MORAL_TITLE" + semester, KnjDbUtils.getString(htrainremarkDetailDatSeme, "REMARK2")); // 道徳タイトル

                svfVrsOutnKurikaeshi(svf, "MORAL" + semester, getTokenList(KnjDbUtils.getString(hreportremarkDat, REMARK1), moralKeta)); // 道徳 HREPORTREMARK_DAT_REMARK1_SIZE_P

            }

            //特記事項・担任からの通信
            setComm(svf, param, student, "COMM1", "COMM2");

            if (printClub) {
                final Map htrainremarkDetailDat9 = getMappedMap(student._semesterHreportremarkDetailDat, "90201");
                svf.VrsOut("CLUB1", KnjDbUtils.getString(htrainremarkDetailDat9, "REMARK1")); // 部活動
                svf.VrsOut("CLUB2", KnjDbUtils.getString(htrainremarkDetailDat9, "REMARK2")); // 部活動
                svf.VrsOut("CLUB3", KnjDbUtils.getString(htrainremarkDetailDat9, "REMARK3")); // 部活動
            }

            final List behaviorSemesList = new ArrayList(student._behaviorSemesDatMap.values());
            //log.info(" schregno  " + student._schregno + ", behaviorSemesDatMap = " + student._behaviorSemesDatMap);
            for (int j = 0; j < Math.min(behaviorSemesList.size(), maxView); j++) {
                final BehaviorSemesDat bsd = (BehaviorSemesDat) behaviorSemesList.get(j);
                final int line = j + 1;
//                final int namelen = KNJ_EditEdit.getMS932ByteLength(bsd._viewname);
//                String field = null;
//                if (formDiv == FORM_DIV1) {
//                    field = "BEHA_VIEW_NAME1_" + (namelen <= 34 ? "1" : namelen <= 38 ? "2" : namelen <= 40 ? "3" : namelen <= 42 ? "4" : "5");
//                } else if (formDiv == FORM_DIV2) {
//                    field = "BEHA_VIEW_NAME1_" + (namelen <= 42 ? "1" : namelen <= 46 ? "2" : "3");
//                } else if (formDiv == FORM_DIV3) {
//                    field = "BEHA_VIEW_NAME1_" + (namelen <= 16 ? "1" : "2");
//                }
//        		svf.VrsOutn(field, line, bsd._viewname); // 行動の記録観点
                svf.VrsOutn("BEHA_VIEW_NAME", line, bsd._viewname); // 行動の記録観点

//        	    for (int semei = 1; semei <= Integer.parseInt(param._semester); semei++) {
//        			final String semester = String.valueOf(semei);
//        			final String name1 = (String) bsd._semesterRecordMap.get(semester);
//        			String actField = null;
//        			if ("3".equals(name1)) {
//        				actField = "ACT" + semester + "_1";
//        			} else if ("2".equals(name1)) {
//        				actField = "ACT" + semester + "_2";
//        			} else if ("1".equals(name1)) {
//        				actField = "ACT" + semester + "_3";
//        			}
//        			if (null == actField) {
//        				continue;
//        			}
//    	            svf.VrsOutn(actField, line, "〇"); // 行動の記録
//        	    }
                final String name1 = (String) bsd._semesterRecordMap.get(param._semester);
                String actField = null;
                if ("3".equals(name1)) {
                    actField = "ACT1";
                } else if ("2".equals(name1)) {
                    actField = "ACT2";
                } else if ("1".equals(name1)) {
                    actField = "ACT3";
                }
                if (null == actField) {
                    continue;
                }
                svf.VrsOutn(actField, line, "〇"); // 行動の記録
            }
            printAttendance(svf, student, param);

            printRecord(svf, formDiv, student, param);
        }

        //特記事項・担任からの通信を出力
        private static void setComm(final Vrw32alp svf, Param param, Student student, String field, String field2) {
            final int gradeCdInt = Integer.parseInt(student._gradeCd);
            int prtlen = 0;
            if (gradeCdInt == 1 || gradeCdInt == 2) { // 1年、2年
                prtlen = 50;
            } else if (gradeCdInt == 3 || gradeCdInt == 4) { // 3年、4年
                prtlen = 50;
            } else if (gradeCdInt == 5 || gradeCdInt == 6) { // 5年、6年
                prtlen = 50;
            } else {
                log.warn(" no form : " + gradeCdInt);
                return;
            }
            int line = 1;
            final Map hreportremarkDat = getMappedMap(student._semesterHreportremarkDat, "9");
            final Map htrainremarkDetailDatSeme = getMappedMap(student._semesterHreportremarkDetailDat, "90101");

            // 特記事項タイトル
            final List title = getTokenList(KnjDbUtils.getString(htrainremarkDetailDatSeme, "REMARK3"), prtlen);
            for (int i = 0; i < title.size(); i++) {
                svf.VrsOutn(field, line, (String) title.get(i));
                line++;
            }

            // 特記事項
            line = 1;
            final List data = getTokenList(KnjDbUtils.getString(hreportremarkDat, COMMUNICATION), prtlen);
            if (null == field2 || null == data) {
                return;
            }
            for (int i = 0; i < data.size(); i++) {
                svf.VrsOutn(field2, line, (String) data.get(i));
                line++;
            }
        }

        private void printAttendance(final Vrw32alp svf, final Student student, final Param param) {
            for (final Iterator it = student._attendMap.values().iterator(); it.hasNext();) {
                final Attendance attend = (Attendance) it.next();
                if (!NumberUtils.isDigits(attend._semester)) {
                    continue;
                }
                if ("9".equals(attend._semester)) {
                    svf.VrsOut("LESSON2", String.valueOf(attend._lesson)); // 授業日数
                    svf.VrsOut("MOURNING2", String.valueOf(attend._suspend + attend._mourning)); // 忌引出停
                    svf.VrsOut("MUST2", String.valueOf(attend._mLesson)); // 出席しなければならない日数
                    svf.VrsOut("NOTICE2", String.valueOf(attend._sick)); // 欠席日数
                    svf.VrsOut("PRESENT2", String.valueOf(attend._present)); // 出席日数
                    svf.VrsOut("LATE2", String.valueOf(attend._late)); // 遅刻
                    svf.VrsOut("EARLY2", String.valueOf(attend._early)); // 早退

                    //svfVrsOutnKurikaeshi(svf, "ATTEND_REC" + attend._semester, getTokenList(remark, KNJPropertiesShokenSize.getShokenSize(null, 32, 3))); // 出欠備考
                } else if (SLASH_YEAR.equals(param._year) && "3".equals(attend._semester)) {
                    // フォームの斜線表示
                } else {
                    final int line = Integer.parseInt(attend._semester);

                    svf.VrsOutn("LESSON1", line, String.valueOf(attend._lesson)); // 授業日数
                    svf.VrsOutn("MOURNING1", line, String.valueOf(attend._suspend + attend._mourning)); // 忌引出停
                    svf.VrsOutn("MUST1", line, String.valueOf(attend._mLesson)); // 出席しなければならない日数
                    svf.VrsOutn("NOTICE1", line, String.valueOf(attend._sick)); // 欠席日数
                    svf.VrsOutn("PRESENT1", line, String.valueOf(attend._present)); // 出席日数
                    svf.VrsOutn("LATE1", line, String.valueOf(attend._late)); // 遅刻
                    svf.VrsOutn("EARLY1", line, String.valueOf(attend._early)); // 早退

                    final String remark = KnjDbUtils.getString(getMappedMap(student._semesterHreportremarkDat, attend._semester), ATTENDREC_REMARK);
                    svfVrsOutnKurikaeshi(svf, "ATTEND_REC" + attend._semester, getTokenList(remark, KNJPropertiesShokenSize.getShokenSize(null, 32, 3))); // 出欠備考
                }
            }
        }

        private void printRecord(final Vrw32alp svf, final int formDiv, final Student student, final Param param) {
            final int maxRecord;
            if (formDiv == FORM_DIV1) {
                maxRecord = 25;
            } else if (formDiv == FORM_DIV2) {
                maxRecord = 34;
            } else { // if (formDiv == FORM_DIV3) {
                maxRecord = 36;
            }

            if (param._isOutputDebug) {
                log.info(" ViewClass size " + student._viewClassList.size());
            }
            if (student._hasHyokaFuka) {
                svf.VrsOut("TEXT1", "※は成績資料不足により評価できません");
            }

            int count = 0;
            printView:
            for (int i = 0; i < student._viewClassList.size(); i++) {
                final ViewClass vc = student._viewClassList.get(i);

                final List<String> classnameCharList = getCenterizedCharList(vc._classname, vc.getViewSize());
                if (param._isOutputDebug) {
                    log.info(" " + vc + ", classnameCharList = " + classnameCharList);
                }

                for (int vi = 0; vi < vc.getViewSize(); vi++) {
                    final String viewcd = vc.getViewCd(vi);
                    final String viewname = vc.getViewName(vi);
                    String recorddiv = null;
                    final int div;
                    final boolean isLastView = vi == vc.getViewSize() - 1; // 最後の観点
                    if (formDiv == FORM_DIV1) {
                        recorddiv = "1";
                        if (isLastView) {
                            recorddiv += "_UL";
                        }
                        String out = "";
                        if (vi < classnameCharList.size()) {
                            out = (String) classnameCharList.get(vi);
                        }
                        if (StringUtils.isBlank(out)) {
                            // ブランクを出力してもレコードが印字扱いしない現象の対応
                            svf.VrsOut("CLASS_NAME" + recorddiv, "DUMMY"); // 教科名
                            svf.VrAttribute("CLASS_NAME" + recorddiv, "X=10000"); // 教科名
                        } else {
                            svf.VrsOut("CLASS_NAME" + recorddiv, out); // 教科名
                        }
                        div = 22;
                    } else { // if (formDiv == FORM_DIV2 || formDiv == FORM_DIV3) {
                        if (isLastView) {
                            recorddiv = "2";
                            if (vc.getViewSize() == 1) {
                                svf.VrsOut("CLASS_NAME3", vc._classname); // 教科名
                            } else {
                                String out = "";
                                if (vi < classnameCharList.size()) {
                                    out = classnameCharList.get(vi);
                                }
                                if (StringUtils.isBlank(out)) {
                                    // ブランクを出力してもレコードが印字扱いしない現象の対応
                                    svf.VrsOut("CLASS_NAME2", "DUMMY"); // 教科名
                                    svf.VrAttribute("CLASS_NAME2", "X=10000"); // 教科名
                                } else {
                                    svf.VrsOut("CLASS_NAME2", out); // 教科名
                                }
                            }
                        } else {
                            recorddiv = "1";
                            if (vi < classnameCharList.size()) {
                                svf.VrsOut("CLASS_NAME1", classnameCharList.get(vi)); // 教科名
                            }
                        }
                        div = 18;
                    }
                    if (param._isOutputDebug) {
                        log.info(" view " + vi + " recorddiv " + recorddiv);
                    }
                    String[] field = null;
                    List<String> val = null;
                    if (KNJ_EditEdit.getMS932ByteLength(viewname) <= div) {
                        field = new String[] {"STUDY_VIEW_NAME" + recorddiv + "_1"};
                        val = Arrays.asList(new String[] {viewname});
                    } else {
                        field = new String[] {"STUDY_VIEW_NAME" + recorddiv + "_2"};
                        val = Arrays.asList(new String[] {viewname});
                    }
                    for (int j = 0; j < Math.min(field.length, val.size()); j++) {
                        svf.VrsOut(field[j], (String) val.get(j)); // 学習の記録観点
                    }
                    for (int si = 1; si <= 4; si++) {
                        final String semester = String.valueOf(si == 4 ? 9 : si);

                        final Map stat = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), semester);

                        final String viewFlg = si == 4 ? "1" : KnjDbUtils.getString(getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), semester), VIEWFLG);
                        final boolean isSlash = !"1".equals(viewFlg) || SLASH_YEAR.equals(param._year) && Arrays.asList("3").contains(semester); // 学期斜線
                        if (isSlash) {
                            final String slashField = "STUDY_VIEW_SLASH" + recorddiv;
                            svf.VrsOutn(slashField, si, param._slashImagePath); // 学習の記録観点
                            if (param._isOutputDebug) {
                                log.info("観点斜線 " + vc._subclasscd + "(" + vi + ") " + viewcd + " " + semester);
                            }
                        } else {
                            final String assess = KnjDbUtils.getString(stat, ASSESS_SHOW1);
                            final String field1 = "STUDY_VIEW" + recorddiv;
                            String attr = null;
                            if (null != assess && assess.endsWith("゜")) {
                                final int xplus = (1051 - 873) / 6; // 1桁右
                                attr = "X=" + String.valueOf(873 + xplus);
                                if (param._isOutputDebug) {
                                    log.info(" xplus = " + xplus + ", attribute " + attr + " subclasscd = " + vc._subclasscd + ", " + viewcd);
                                }
                            }
                            if (null != attr) {
                                svf.VrAttributen(field1, si, attr);
                            }
                            svf.VrsOutn(field1, si, assess); // 学習の記録観点
                            if (vi == vc.getViewSize() / 2) {
                                final String score = KnjDbUtils.getString(stat, SCORE);
                                svf.VrsOut("STUDY_VIEW" + recorddiv + "_9", score); // 学習の記録観点 評定
                            }
                        }
                    }
                    svf.VrEndRecord();
                    count += 1;
                    if (count > maxRecord) {
                        break printView;
                    }
                }
            }
            if (count == 0) {
                for (int i = 0; i < maxRecord; i++) {
                    svf.VrsOut("CLASS_NAME1", "DUMMY"); // 教科名
                    svf.VrAttribute("CLASS_NAME1", "X=10000"); // 教科名
                    svf.VrEndRecord();
                }
            }
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 76137 $ $Date: 2020-08-21 17:36:51 +0900 (金, 21 8 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;

        final String _grade;
        final String _gradeCd;
        final String _gradeHrclass;
        final String _disp;
        final String[] _categorySelected;
        final String _sdate;
        final String _date;
        final String _descDate;
        final String _documentroot;
        final String _imagepath;
        final String _schoollogoFilePath;
        final String _schoolgoalFilePath;
        final String _schoolstampFilePath;
        final String _slashImagePath;
        final Map _semesternameMap;

        final String _useSchool_KindField;
        final String _knjdBehaviorsd_UseText_P;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
//        final String HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P;
        final String HREPORTREMARK_DAT_COMMUNICATION_SIZE_P;
        final String HREPORTREMARK_DAT_REMARK1_SIZE_P;
        final String HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P;

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private String _cerifSchoolSchoolName;
        private String _cerifSchoolJobName;
        private String _cerifSchoolPrincipalName;
        private String _cerifSchoolRemark2;
        private String _cerifSchoolRemark3;
        private boolean _isNoPrintMoto;
        final Map _attendParamMap;
        int formdiv;
        final boolean _isOutputDebug;

        final List _d026List = new ArrayList();


        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = _semester;
            _disp = request.getParameter("DISP");
            if ("1".equals(_disp)) {
                _gradeHrclass = null;
                _grade = request.getParameter("GRADE");
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
                _grade = _gradeHrclass.substring(0, 2);
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
                for (int i = 0; i < _categorySelected.length; i++) {
                    _categorySelected[i] = StringUtils.split(_categorySelected[i], "-")[0];
                }
            }
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _descDate = null == request.getParameter("PRINT_DATE") ? null : KNJ_EditDate.H_Format_Haifun(request.getParameter("PRINT_DATE"));
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");

            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _knjdBehaviorsd_UseText_P = request.getParameter("knjdBehaviorsd_UseText_P");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            SCHOOLKIND = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));
//            HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P = request.getParameter("HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P");
            HREPORTREMARK_DAT_COMMUNICATION_SIZE_P = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P");
            HREPORTREMARK_DAT_REMARK1_SIZE_P = request.getParameter("HREPORTREMARK_DAT_REMARK1_SIZE_P");
            HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P = request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P");

            setCertifSchoolDat(db2);

            _definecode = createDefineCode(db2);
            loadNameMstD026(db2);
//            setCreditMst(db2);
//            setRecordMockOrderSdivDat(db2);
            loadNameMstD016(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            if (!"1".equals(_disp)) {
                _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            }
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _schoollogoFilePath = getImageFilePath("SCHOOLLOGO.jpg");
            _schoolgoalFilePath = getImageFilePath("SCHOOLGOAL.jpg");
            _schoolstampFilePath = getImageFilePath("SCHOOLSTAMP_P.bmp");
            _slashImagePath = getImageFilePath("slash.jpg");

            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD184I' AND NAME = 'outputDebug' ")));
            _gradeCd = getSchregRegdGdat(db2, "GRADE_CD");
            _semesternameMap = getSemesternameMap(db2);
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

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        " + field + " ");
            stb.append("FROM    SCHREG_REGD_GDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + _year + "' ");
            stb.append(    "AND T1.GRADE = '" + _grade + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

        private void loadNameMstD016(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final String namespare1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            _isNoPrintMoto = "Y".equals(namespare1);
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK3, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '117' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));
            _cerifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _cerifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _cerifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _cerifSchoolRemark2 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任");
            _cerifSchoolRemark3 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK3"));
        }

        private Map getSemesternameMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql.toString()), "SEMESTER", "SEMESTERNAME");
        }

        private void loadNameMstD026(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String field;
            if ("1".equals(_semester)) {
                field = "ABBV1";
            } else if ("2".equals(_semester)) {
                field = "ABBV2";
            } else {
                field = "ABBV3";
            }
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1' OR NAMESPARE1 = '1' ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "NAME1"));
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            log.warn("画像ファイル:" + path + " exists? " + file.exists());
            if (!file.exists()) {
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

    }
}
