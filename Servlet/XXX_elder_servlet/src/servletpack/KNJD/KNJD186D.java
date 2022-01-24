// kanji=漢字
/*
 * $Id$
 */
package servletpack.KNJD;

import java.io.File;
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
import java.util.ListIterator;
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
public class KNJD186D {

    private static final Log log = LogFactory.getLog(KNJD186D.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";

    private static final String ENG_LANG_CD = "900100";
    private static final String FOREIGN_LANG_CD = "210100";

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

    private void load(final Param param, final DB2UDB db2, final List<Student> studentList) {
        Attendance.load(db2, param, studentList);
        HreportBehaviorLmDat.setHreportBehaviorLmDatList(db2, param, studentList);
        Student.setHReportRemarkDat(db2, param, studentList);
        Student.setRecordTotalstudytimeDat(db2, param, studentList);
        View.setViewRecordList(db2, param, studentList);
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

    private static int calcDivIntCeil(final int bunsi, final int bunbo) {
        return (new BigDecimal((bunsi * 1.0) / (bunbo * 1.0))).setScale(0, BigDecimal.ROUND_CEILING).intValue();
    }
    private static int getPrintClassnameLen(final String str) {
        int retVal = 0;
        if (str == null || str.length() == 0) {
            retVal = 0;
        } else {
            retVal = str.length() > 3 ? calcDivIntCeil(str.length(), 2) : str.length();
        }
        return retVal;
    }

    private static String getPrintClassname(final String str, final int width) {
        String rtn;
        rtn = addYohaku(str, width);

        // rtn = kintouwari(str, width);
        // if (rtn.length() != 0 && rtn.charAt(0) != ' ') {
        //     rtn = centering(str, width);
        // }
        return rtn;
    }

    /**
     * 表示文字数幅分後ろにスペースを挿入した文字列
     * @param str 元文字列
     * @param keta 表示文字数幅
     * @return 後ろにスペースを挿入した文字列
     */
    private static String addYohaku(final String str, final int width) {
        if (null == str || str.length() == 0) {
            return StringUtils.repeat("　", width);
        }
        //基本的には、3行の出力になっていて、6文字以上の出力(1行2文字まで出力)は無い想定。
        final StringBuffer stb = new StringBuffer();
        stb.append(str);
        if (str.length() > 3) {
            stb.append(StringUtils.repeat("　", (width - calcDivIntCeil(str.length(), 2))));
        } else if ((str.length() <= 3)) {
            stb.append(StringUtils.repeat("　", (width - str.length())));
        }  //3と6以上は除外。余白調整不要。
        return stb.toString();
    }

    /**
     * 表示文字数幅分均等にスペースを挿入した文字列
     * @param str 元文字列
     * @param keta 表示文字数幅
     * @return 均等にスペースを挿入した文字列
     */
    private static String kintouwari(final String str, final int width) {
        if (null == str) {
            return StringUtils.repeat("　", width);
        }
        final String sps = StringUtils.repeat("　", (width - str.length()) / (str.length() + 1));
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            stb.append(sps).append(str.charAt(i));
        }
        stb.append(StringUtils.repeat("　", width - stb.length()));
        return stb.toString();
    }

    /**
     * 表示文字数幅分にセンタリングした文字列
     * @param str 元文字列
     * @param keta 表示文字数幅
     * @return センタリングした文字列
     */
    private static String centering(final String str, final int width) {
        if (null == str) {
            return StringUtils.repeat("　", width);
        }
        final String sps = StringUtils.repeat("　", (width - str.length()) / 2);
        final StringBuffer stb = new StringBuffer();
        stb.append(sps);
        for (int i = 0; i < str.length(); i++) {
            stb.append(str.charAt(i));
        }
        stb.append(StringUtils.repeat("　", width - stb.length()));
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

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return map.get(key1);
    }

    private static String zeroBlank(final int v) {
        if (v == 0) {
            return "";
        }
        return String.valueOf(v);
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

    private static void svfVrsOutnKurikaeshi(final Vrw32alp svf, final String[] field, final int gyo, final List<String> data) {
        if (null == field || null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field.length, data.size()); i++) {
            svf.VrsOutn(field[i], gyo, data.get(i));
        }
    }

    private static void svfVrsOutKurikaeshi(final Vrw32alp svf, final String[] field, final List<String> data) {
        if (null == field || null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field.length, data.size()); i++) {
            svf.VrsOut(field[i], data.get(i));
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
        List _hrepoBehavLmDatList = Collections.EMPTY_LIST;
        Map<String, String> _hreportremarkDatCommunication = new HashMap();
        Map<String, String> _hreportremarkDatTotalstudytime = new HashMap();
        Map<String, String> _hreportremarkDatRemark1 = new HashMap();
        Map<String, String> _hreportremarkDatAttendrecRemark = new HashMap();
        Map<String, String> _recordTotalstudytimeDatTotalstudytime = new HashMap();
        List _viewList = Collections.EMPTY_LIST;

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

        private String getHrAttendNo(final Param param) {
            try {
                final String grade = "第" + String.valueOf(Integer.parseInt(param._gradeCd)) + "学年";
                final String hrclass = StringUtils.defaultString(_hrClassName1) + "組";
                final String attendno = String.valueOf(Integer.parseInt(_attendno)) + "番";
                return hankakuToZenkaku(grade + " " + hrclass + " " + attendno);
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
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT TRANSF ON TRANSF.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND TRANSF.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END BETWEEN TRANSF.TRANSFER_SDATE AND TRANSF.TRANSFER_EDATE ");
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
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + whereIn(true, param._categorySelected));
            stb.append("     ORDER BY ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.info(" student sql = " + sql);

            final List<Student> students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("GRADE_CD"), rs.getString("GRADE_NAME1"), rs.getString("HR_NAME"), staffname, attendno, rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"), rs.getString("ENT_YEAR"));
                    student._name = null != rs.getString("NAME_SETUP") ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    student._birthday = rs.getString("BIRTHDAY");
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
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkDatSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        student._hreportremarkDatCommunication.put(semester, rs.getString("COMMUNICATION"));
                        student._hreportremarkDatTotalstudytime.put(semester, rs.getString("TOTALSTUDYTIME"));
                        student._hreportremarkDatRemark1.put(semester, rs.getString("REMARK1"));
                        student._hreportremarkDatAttendrecRemark.put(semester, rs.getString("ATTENDREC_REMARK"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHReportRemarkDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

        public static void setRecordTotalstudytimeDat(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRecordTotalstudytimeDatSql(param);
                ps = db2.prepareStatement(sql);
                // log.debug("recordTotalstudytimeDat SQL: " + sql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    String setTotalStudytime = "";
                    String sep = "";
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (!StringUtils.isBlank(rs.getString("TOTALSTUDYTIME"))) {
                            final String semester = rs.getString("SEMESTER");
                            setTotalStudytime += sep + rs.getString("TOTALSTUDYTIME");
                            student._recordTotalstudytimeDatTotalstudytime.put(semester, setTotalStudytime);
                            sep = "、";
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getRecordTotalstudytimeDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_TOTALSTUDYTIME_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR        = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER    = '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO    = ? ");
            stb.append("     AND (   T1.CLASSCD IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = '" + param._d008Namecd1 + "') ");
            stb.append("          OR T1.SUBCLASSCD LIKE '90%') ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");

            return stb.toString();
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
         * 観点コードの観点のリストを得る
         * @param semester 学期
         * @param subclasscd 科目コード
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public View getView(final String semester, final String subclasscd, final String viewcd) {
            if (null == semester || null == subclasscd || null == viewcd) {
                return null;
            }
            for (final Iterator it = _viewList.iterator(); it.hasNext();) {
                final View view = (View) it.next();
                if (view._semester.equals(semester) && view._subclasscd.equals(subclasscd) && viewcd.equals(view._viewcd)) {
                    return view;
                }
            }
            return null;
        }

        /**
         * 観点コードの観点のリストを得る
         * @param subclasscd 科目コード
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String subclasscd, final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (Iterator it = _viewList.iterator(); it.hasNext();) {
                    final View view = (View) it.next();
                    if (view._subclasscd.equals(subclasscd) && viewcd.equals(view._viewcd)) {
                        rtn.add(view);
                    }
                }
            }
            return rtn;
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
            final String edate = param._edate;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final Attendance attendance = new Attendance(
                                rs.getString("SEMESTER"),
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("SICK"),
                                rs.getInt("SICK_ONLY"),
                                rs.getInt("NOTICE_ONLY"),
                                rs.getInt("NONOTICE_ONLY"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY"),
                                rs.getInt("TRANSFER_DATE")
                        );
                        student._attendMap.put(rs.getString("SEMESTER"), attendance);
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

    private static class Form {

        boolean hasdata = false;

        private final int FORM_DIV1 = 1;
        private final int FORM_DIV2 = 2;
        private final int FORM_DIV3 = 3;
        private int _formDiv;

        private void printHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student, final Param param) {
            final String form;
            final boolean isEnd = Integer.parseInt(param._semester) == param._definecode.semesdiv;
            if (isEnd) {
                form = "KNJD186D_1_2.frm";
            } else {
                form = "KNJD186D_1_1.frm";
            }
            svf.VrSetForm(form, 1);
            if (isEnd) {
                if (null != param._schoolstampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", param._schoolstampFilePath); //
                }
            }
            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度"); // 年度
            if (null != param._schoollogoFilePath) {
                svf.VrsOut("SCHOOL_LOGO", param._schoollogoFilePath); //
            }
            svf.VrsOut("SCHOOL_NAME", trim(param._cerifSchoolSchoolName)); // 学校名
            svf.VrsOut("HR_NAME", student.getHrAttendNo(param)); // 年組番号
            if (isEnd) {
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthday)); // 生年月日
                svf.VrsOut("END_GRADE", String.valueOf(Integer.parseInt(param._gradeCd))); // 修了学年
                svf.VrsOut("END_DATE", KNJ_EditDate.h_format_JP(db2, param._descDate)); // 修了日
                svf.VrsOut("END_SCHOOL_NAME", param._cerifSchoolRemark3); // 修了学校名
                svf.VrsOut("JOB_NAME", param._cerifSchoolJobName); // 校長名称
                svf.VrsOut("END_PRINCIPAL_NAME", param._cerifSchoolPrincipalName); // 修了校長名称

                final int keta = getMS932ByteLength(student._name);
                svf.VrsOut("END_NAME" + (keta <= 20 ? "1" : keta <= 30 ? "2" : "3"), student._name);
            }

            svf.VrsOut("SEMESTER2", param._semesternameMap.get(param._semester)); // 学期
            final int keta1 = getMS932ByteLength(student._name);
            svf.VrsOut("NAME" + (keta1 <= 20 ? "1" : keta1 <= 30 ? "2" : "3"), student._name);
            final int keta2 = getMS932ByteLength(trim(param._cerifSchoolPrincipalName));
            svf.VrsOut("PRINCIPAL_NAME" + (keta2 <= 20 ? "1" : keta2 <= 30 ? "2" : "3"), trim(param._cerifSchoolPrincipalName));
            final int keta3 = getMS932ByteLength(student._staffName);
            svf.VrsOut("TEACHER_NAME" + (keta3 <= 20 ? "1" : keta3 <= 30 ? "2" : "3"), student._staffName);

            svf.VrsOutn("SEMESTER1", 1, "１"); // 学期
            svf.VrsOutn("SEMESTER1", 2, "２"); // 学期
            svf.VrsOutn("SEMESTER1", 3, "３"); // 学期

            for (final Iterator it = student._attendMap.values().iterator(); it.hasNext();) {
                final Attendance attend = (Attendance) it.next();
                if (!NumberUtils.isDigits(attend._semester)) {
                    continue;
                }
                final int line = "9".equals(attend._semester) ? 4 : Integer.parseInt(attend._semester);
                svf.VrsOutn("LESSON", line, String.valueOf(attend._lesson)); // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(attend._suspend));   // 出停
                svf.VrsOutn("MOURNING", line, String.valueOf(attend._mourning)); // 忌引
                svf.VrsOutn("MUST", line, String.valueOf(attend._mLesson)); // 出席しなければならない日数
                svf.VrsOutn("SICK", line, String.valueOf(attend._sickOnly + attend._nonoticeOnly)); // 病気
                svf.VrsOutn("NOTICE", line, String.valueOf(attend._noticeOnly)); // 事故欠
                svf.VrsOutn("APPOINT", line, String.valueOf(attend._present)); // 出席日数
                svf.VrsOutn("LATE", line, String.valueOf(attend._late)); // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(attend._early)); // 早退

                final String remark = student._hreportremarkDatAttendrecRemark.get(attend._semester);
//                svfVrsOutnKurikaeshi(svf, new String[] {"REMARK1", "REMARK2"}, line, getTokenList(remark, KNJPropertiesShokenSize.getShokenSize(param.HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P, 9, 1)));
                svfVrsOutnKurikaeshi(svf, new String[] {"REMARK1", "REMARK2"}, line, getTokenList(remark, KNJPropertiesShokenSize.getShokenSize(null, 9, 2)));
            }
            svf.VrEndPage();
            hasdata = true;
        }

        private void printMain(final Vrw32alp svf, final Student student, final Param param) {
            final String form;
            final boolean isEnd = Integer.parseInt(param._semester) == param._definecode.semesdiv;

            if ("06".equals(param._gradeCd) || "05".equals(param._gradeCd)) {
                if (isEnd) {
                    form = "KNJD186D_2_3_2.frm";
                } else {
                    form = "KNJD186D_2_3.frm";
                }
                _formDiv = FORM_DIV3;
            } else if ("03".equals(param._gradeCd)) {
                if (isEnd) {
                    form = "KNJD186D_2_2_2.frm";
                } else {
                    form = "KNJD186D_2_2.frm";
                }
                _formDiv = FORM_DIV2;
            } else if ("04".equals(param._gradeCd)) {
                if (isEnd) {
                    form = "KNJD186D_2_2_4.frm";
                } else {
                    form = "KNJD186D_2_2_3.frm";
                }
                _formDiv = FORM_DIV2;
            } else {
                if (isEnd) {
                    form = "KNJD186D_2_1_2.frm";
                } else {
                    form = "KNJD186D_2_1.frm";
                }
                _formDiv = FORM_DIV1;
            }
            log.info(" form = " + form);

            svf.VrSetForm(form, 4);

            printHeader(svf, student, param);

            printTsushinran(svf, student, param);

            printTotalAct(svf, student, param);

            printMoral(svf, student, param);

            printLifeView(svf, student, param);

            printView(svf, student, param);

            hasdata = true;
        }

        private void printHeader(final Vrw32alp svf, final Student student, final Param param) {
            final int ketaName = getMS932ByteLength(student._name);
            svf.VrsOut(ketaName <= 20 ? "NAME1" : ketaName <= 30 ? "NAME2" : "NAME3", student._name);

            svf.VrsOut("GRADE", "(" + (NumberUtils.isDigits(param._gradeCd) ? String.valueOf(Integer.parseInt(param._gradeCd)) : StringUtils.defaultString(param._gradeCd)) + "年)"); // 学年

            for (int si = 1; si <= (_formDiv == FORM_DIV3 ? 7 : 5); si++) {
                final String ssi = String.valueOf(si);
                svf.VrsOut("SEMESTER" + ssi, param._semesternameMap.get(param._semester)); // 学期
            }
        }

        // 通信欄
        private void printTsushinran(final Vrw32alp svf, final Student student, final Param param) {
            final List<String> comm = getTokenList(student._hreportremarkDatCommunication.get(param._semester), KNJPropertiesShokenSize.getShokenSize(param.HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, 45, 5));
            for (int si = 0; si < comm.size(); si++) {
                final String ssi = String.valueOf(si + 1);
                svf.VrsOut("COMM" + ssi, comm.get(si)); // 通信欄
            }
        }

        // 道徳
        private void printMoral(final Vrw32alp svf, final Student student, final Param param) {
            if ("06".equals(param._gradeCd) || "05".equals(param._gradeCd)) {
                svf.VrsOut("TOTAL_ACT_ITEM3", "道徳");
            } else {
                svf.VrsOut("TOTAL_ACT_ITEM2", "道徳");
            }
            final List<String> moral = getTokenList(student._recordTotalstudytimeDatTotalstudytime.get(param._semester), KNJPropertiesShokenSize.getShokenSize(param.RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_P, 48, 2));
            for (int si = 0; si < moral.size(); si++) {
                final String ssi = String.valueOf(si + 1);
                svf.VrsOut("MORAL" + ssi, moral.get(si)); // 道徳
            }
        }

        // 総合的な学習の時間
        private void printTotalAct(final Vrw32alp svf, final Student student, final Param param) {
            if (_formDiv == FORM_DIV1) {

                final String title1;
                title1 = "［総合的な学習の時間（つくし村など）］";
                svf.VrsOut("TOTAL_ACT_TITLE1", title1); // 総学タイトル

                final List<String> totalStudyTime = getTokenList(student._hreportremarkDatTotalstudytime.get(param._semester), KNJPropertiesShokenSize.getShokenSize(param.HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P, 45, 4));
                svfVrsOutKurikaeshi(svf, new String[] {"TOTAL_ACT1_1", "TOTAL_ACT1_2", "TOTAL_ACT1_3", "TOTAL_ACT1_4", },  totalStudyTime); // 総学内容

                final List<String> remark1 = getTokenList(student._hreportremarkDatRemark1.get(param._semester), KNJPropertiesShokenSize.getShokenSize(param.HREPORTREMARK_DAT_REMARK1_SIZE_P, 45, 4));
                svfVrsOutKurikaeshi(svf, new String[] {"TOTAL_ACT2_1", "TOTAL_ACT2_2", "TOTAL_ACT2_3", "TOTAL_ACT2_4", },  remark1); // 総学取り組み
            }

            List<ViewClass> sogoList = new ArrayList();
            ViewClass gaikokugo = null;
            for (final ViewClass vc : param._viewClassList) {
                if (isSogo(param, vc)) {
                    sogoList.add(vc);
                }
                if (isGaikokugo(param, vc)) {
                    gaikokugo = vc;
                }
            }

            // 外国語活動
            if (_formDiv == FORM_DIV2 && null != gaikokugo) {
                svf.VrsOut("TOTAL_ACT_TITLE1", "［外国語活動］"); // 外国語タイトル
                svf.VrsOut("CLASS_NAME3", gaikokugo._subclassname); // 外国語

                for (int i = 0; i < gaikokugo.getViewSize(); i++) {
                    final int line = i + 1;

                    final List<String> viewnameList = getTokenList(gaikokugo.getViewName(i), 20);

                    if (viewnameList.size() == 1) {
                        svf.VrsOutn("VIEW3_1", line, viewnameList.get(0)); // 外国語観点
                    } else {
                        for (int ti = 0; ti < viewnameList.size(); ti++) {
                            svf.VrsOutn("VIEW3_" + (2 + ti), line, viewnameList.get(ti)); // 外国語観点
                        }
                    }

                    List<String> meateTokenList = getTokenList(gaikokugo.getMeate(i), 66);
                    if (meateTokenList.size() == 1) {
                        svf.VrsOutn("TARGET3_1", line, meateTokenList.get(0)); // 外国語めあて
                    } else {
                        String[] field = {"2", "3"};
                        if (meateTokenList.size() > field.length) {
                            meateTokenList = getTokenList(gaikokugo.getMeate(i), 78);
                            field = new String[] {"4", "5"};
                        }
                        for (int k = 0; k < Math.min(meateTokenList.size(), field.length); k++) {
                            svf.VrsOutn("TARGET3_" + field[k], line, meateTokenList.get(k)); // 外国語めあて
                        }
                    }

                    final View view = student.getView(param._semester, gaikokugo._subclasscd, gaikokugo.getViewCd(i));
                    if (null != view) {
                        svf.VrsOutn("VALUE3", line, view._statusName1); // 外国語評価
                    }
                }
            }

            if (_formDiv == FORM_DIV1 || _formDiv == FORM_DIV2) {
                String title2 = "";
                if ("03".equals(param._gradeCd)) {
                    title2 = "［総合的な学習の時間（英語・囲碁・将棋）］";
                } else if ("04".equals(param._gradeCd)) {
                    title2 = "［総合的な学習の時間（英語）］";
                } else if ("01".equals(param._gradeCd)) {
                    title2 = "［総合的な学習の時間（英語・将棋）］";
                } else if ("02".equals(param._gradeCd)) {
                    title2 = "［総合的な学習の時間（英語・囲碁）］";
                }
                svf.VrsOut("TOTAL_ACT_TITLE2", title2); // 総学タイトル

                for (int k = 0; k < sogoList.size(); k++) {
                    final int kline = k + 1;
                    final ViewClass vc = sogoList.get(k);

                    log.info(" sogo " + k + " " + vc._subclasscd + " : " + vc._subclassname);

                    if ("04".equals(param._gradeCd)) {
                        svf.VrsOut("TOTAL_ACT_ITEM", vc._subclassname); // 総学項目
                    }else {
                        svf.VrsOutn("TOTAL_ACT_ITEM", kline, vc._subclassname); // 総学項目
                    }
                    for (int i = 0; i < vc.getViewSize(); i++) {
                        final String ssi = String.valueOf(i + 1);

                        final List<String> viewnameList = getTokenList(vc.getViewName(i), 20);

                        if (viewnameList.size() == 1) {
                            if ("04".equals(param._gradeCd)) {
                                svf.VrsOut("TOTAL_ACT_VIEW" + ssi + "_1", viewnameList.get(0)); // 総学観点
                            }else {
                                svf.VrsOutn("TOTAL_ACT_VIEW" + ssi + "_1", kline, viewnameList.get(0)); // 総学観点
                            }
                        } else {
                            for (int ti = 0; ti < viewnameList.size(); ti++) {
                                if ("04".equals(param._gradeCd)) {
                                    svf.VrsOut("TOTAL_ACT_VIEW" + ssi + "_" + (2 + ti), viewnameList.get(ti)); // 総学観点
                                }else {
                                    svf.VrsOutn("TOTAL_ACT_VIEW" + ssi + "_" + (2 + ti), kline, viewnameList.get(ti)); // 総学観点
                                }
                            }
                        }

                        if ("04".equals(param._gradeCd)) {
                            svf.VrsOut("TOTAL_ACT_TARGET" + ssi, vc.getMeate(i)); // 総学めあて
                        }else {
                            svf.VrsOutn("TOTAL_ACT_TARGET" + ssi, kline, vc.getMeate(i)); // 総学めあて
                        }
                        final View view = student.getView(param._semester, vc._subclasscd, vc.getViewCd(i));
                        if (null != view) {
                            if ("04".equals(param._gradeCd)) {
                                svf.VrsOut("TOTAL_ACT_VALUE" + ssi, view._statusName1); // 総学評価
                            }else {
                                svf.VrsOutn("TOTAL_ACT_VALUE" + ssi, kline, view._statusName1); // 総学評価
                            }
                        }
                    }
                    if ("04".equals(param._gradeCd)) {
                        break;
                    }
                }

            } else if (_formDiv == FORM_DIV3) {

                //sogoList = reverse(sogoList);
                svf.VrsOut("TOTAL_ACT_TITLE2", "［総合的な学習の時間］"); // 総学タイトル

                for (int k = 0; k < sogoList.size(); k++) {
                    final ViewClass vc = sogoList.get(k);

                    log.info(" sogo " + k + " " + vc._subclasscd + " : " + vc._subclassname);

                    final String kline = String.valueOf(k + 1);
                    svf.VrsOut("TOTAL_ACT_ITEM" + kline, vc._subclassname); // 総学項目

                    for (int i = 0; i < vc.getViewSize(); i++) {
                        final int line = i + 1;
                        final List<String> viewnameList = getTokenList(vc.getViewName(i), 16);
                        if (viewnameList.size() == 1) {
                            // 1がまんなか
                            svf.VrsOutn("TOTAL_ACT_VIEW" + kline + "_1", line, viewnameList.get(0)); // 総学観点
                        } else {
                            // 1がまんなか
                            final String[] fieldN = {"2", "1", "3"};
                            for (int vi = 0; vi < Math.min(viewnameList.size(),  fieldN.length); vi++) {
                                svf.VrsOutn("TOTAL_ACT_VIEW" + kline + "_" + fieldN[vi], line, viewnameList.get(vi)); // 総学観点
                            }
                        }

                        List<String> meateList = getTokenList(vc.getMeate(i), 32);
                        final String field;
                        if (meateList.size() <= 5) {
                            // 32桁5行
                            field = "TOTAL_ACT_TARGET" + kline + "_1_";
                        } else {
                            // 40桁5行
                            meateList = getTokenList(vc.getMeate(i), 40);
                            field = "TOTAL_ACT_TARGET" + kline + "_2_";
                        }
                        for (int mi = 0; mi < meateList.size(); mi++) {
                            svf.VrsOutn(field + String.valueOf(mi + 1), line, meateList.get(mi)); // 総学めあて
                        }

                        final View view = student.getView(param._semester, vc._subclasscd, vc.getViewCd(i));
                        if (null != view) {
                            svf.VrsOutn("TOTAL_ACT_VALUE" + kline, line, view._statusName1); // 総学評価
                        }
                    }
                }
            }
        }

        private <T> List<T> reverse(final List<T> sogoList) {
            final List<T> rtn = new ArrayList();
            for (final ListIterator<T> it = sogoList.listIterator(sogoList.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        // 生活のようす
        private void printLifeView(final Vrw32alp svf, final Student student, final Param param) {
            final int keta = _formDiv == FORM_DIV1 ? 42 : 48;
            final int maxLine = 6;

            int n = 0;
            for (int j = 0; j < param._behavSemesMstList.size(); j++) {

                final BehaviorSemesLMst lmst = param._behavSemesMstList.get(j);

                for (int k = 0; k < lmst._mMstList.size(); k++) {

                    final BehaviorSemesLMst.BehaviorSemesMMst mmst = lmst._mMstList.get(k);
                    n++;

                    final int line = n % maxLine == 0 ? maxLine : n % maxLine;
                    final String col = String.valueOf(n / maxLine + (n % maxLine == 0 ? 0 : 1));
//                    log.info("  (lcd, mcd, line, col) = (" + lmst._lCd + ", " + mmst._mCd + ", " + line + ", " + col + ")");
                    final List<String> behavName;
                    final String mname = StringUtils.defaultString(mmst._mName);
                    final String lname = StringUtils.isEmpty(lmst._lName) ? "" : "(" + lmst._lName + ")";
                    if (getMS932ByteLength(mname + lname) <= keta) {
                        behavName = Arrays.asList(new String[] {mname + lname});
                    } else if (getMS932ByteLength(mname) <= keta && getMS932ByteLength(lname) <= keta) {
                        behavName = Arrays.asList(new String[] {mname, lname});
                    } else {
                        behavName = getTokenList(mname + lname, keta);
                    }
//                    log.info(" behavName = " + behavName + " / " + behavName.size());
                    if (behavName.size() == 1) {
                        svf.VrsOutn("LIFE_VIEW" + col + "_1", line, behavName.get(0)); // 生活のようす観点
                    } else {
                        final String[] field = {"2", "3"};
                        for (int i = 0; i < Math.min(behavName.size(), field.length); i++) {
                            svf.VrsOutn("LIFE_VIEW" + col + "_" + field[i], line, behavName.get(i)); // 生活のようす観点
                        }
                    }

                    final HreportBehaviorLmDat lmDat = HreportBehaviorLmDat.getHreportBehaviorLmDat(param._semester, lmst._lCd, mmst._mCd, student._hrepoBehavLmDatList);
                    if (null != lmDat) {
                        if ("1".equals(param._knjdBehaviorsd_UseText_P)) {
                            svf.VrsOutn("LIFE_VALUE" + col, line, lmDat._nmd036Namespare1); // 生活のようす評価
                        } else {
                            if ("1".equals(lmDat._record)) {
                                svf.VrsOutn("LIFE_VALUE" + col, line, "○"); // 生活のようす評価
                            }
                        }
                    }
                }
            }
        }

        private boolean isSogo(Param param, ViewClass vc) {
            boolean isSogo = false;
            if ("01".equals(param._gradeCd)) {
                if (vc._subclasscd.endsWith(ENG_LANG_CD) || vc._subclasscd.endsWith("900300")) { // 英語・将棋
                    isSogo = true;
                }
            } else if ("02".equals(param._gradeCd)) {
                if (vc._subclasscd.endsWith(ENG_LANG_CD) || vc._subclasscd.endsWith("900400")) { // 英語・囲碁
                    isSogo = true;
                }
            } else if ("03".equals(param._gradeCd)) {
                if (vc._subclasscd.endsWith(ENG_LANG_CD) || vc._subclasscd.endsWith("900200")) { // 英語・囲碁・将棋
                    isSogo = true;
                }
            } else if ("04".equals(param._gradeCd)) {
                if (vc._subclasscd.endsWith(ENG_LANG_CD)) { // 英語
                    isSogo = true;
                }
            } else if ("05".equals(param._gradeCd) || "06".equals(param._gradeCd)) {
                if (vc._subclasscd.endsWith(ENG_LANG_CD) || vc._subclasscd.endsWith(FOREIGN_LANG_CD)) { // 英語 外国語
                    isSogo = true;
                }
            }
            log.info(" vc " + vc._classcd + " : " + vc._subclasscd +" : isSogo = " + isSogo);
            return isSogo;
        }

        private boolean isGaikokugo(Param param, ViewClass vc) {
            boolean isGaikokugo = false;
            if ("05".equals(param._gradeCd) || "06".equals(param._gradeCd)) {
                if (vc._subclasscd.endsWith(FOREIGN_LANG_CD)) { // 外国語
                    isGaikokugo = true;
                }
            }
            log.info(" vc " + vc._classcd + " : " + vc._subclasscd +" : isSogo = " + isGaikokugo);
            return isGaikokugo;
        }

        private boolean isKanten(Param param, ViewClass vc) {
            boolean isKanten = true;
            if (isSogo(param, vc) || vc._classcd.startsWith("9")) {
                isKanten = false;
            }
            return isKanten;
        }

        // 観点
        private void printView(final Vrw32alp svf, final Student student, final Param param) {
            final int maxLine1;
            if (_formDiv == FORM_DIV1) {
                maxLine1 = 18;
            } else if (_formDiv == FORM_DIV2) {
                maxLine1 = 24;
            } else {
                maxLine1 = 13;
            }

            log.info(" viewClassList size = " + param._viewClassList.size());

            int line = 0;
            for (int vci = 0; vci < param._viewClassList.size(); vci++) {
                final ViewClass vc = param._viewClassList.get(vci);
                if (!isKanten(param, vc)) {
                    continue;
                }

                final int mxViewSize = vc.getViewSize() > getPrintClassnameLen(vc._subclassname) ? vc.getViewSize() : getPrintClassnameLen(vc._subclassname);
                final int mxLen = Math.max(vc.getViewSize(), StringUtils.defaultString(vc._subclassname).length());
                final String classname = getPrintClassname(vc._subclassname, mxLen);
                log.info(" classname classcd = " + vc._classcd + " / length = " + classname.length() + " / kanten size = " + vc.getViewSize() + " / [" + classname + "]");
                final int formDiv3recordCount = 8;
                for (int ci = 0; ci < mxViewSize; ci++) {

                    final String div;
                    if (_formDiv == FORM_DIV3) {
                        if (vci >= formDiv3recordCount) {
                            div = ci == 0 ? "3" : "4"; // 小さい枠
                        } else {
                            div = ci == 0 ? "1" : "2";
                        }
                    } else {
                        div = ci == 0 ? "1" : "2";
                    }
                    final String ch;
                    final String cnDiv;
                    final String ch2;
                    if (vc._subclassname.length() > 3) {
                        cnDiv = "_2";
                        //行数(ci)はMAX3行、入る文字はMAX6文字。
                        ch = (classname.length() <= ci || ci >= 3) ? "" : ci * 2 < classname.length() ? String.valueOf(classname.charAt(ci*2)) : "";
                        ch2 = (classname.length() <= ci || ci >= 3) ? "" : (ci * 2 + 1) < classname.length() ? String.valueOf(classname.charAt(ci*2 + 1)) : "";
                    } else {
                        cnDiv = "";
                        ch = classname.length() <= ci ? "" : String.valueOf(classname.charAt(ci));
                        ch2 = "";
                    }

                    if (StringUtils.isBlank(ch)) {
                        svf.VrsOut("CLASS_NAME" + div, "DUMMY"); // 教科名
                        svf.VrAttribute("CLASS_NAME" + div, "X=10000");
                    } else {
                        if (classname.length() > 3) {
                            svf.VrsOut("CLASS_NAME" + div + cnDiv, ch+ch2); // 教科名
                        } else {
                            svf.VrsOut("CLASS_NAME" + div + cnDiv, ch); // 教科名
                        }
                    }

                    // final String ch = String.valueOf(classname.charAt(ci));
                    // if (StringUtils.isBlank(ch)) {
                    //     svf.VrsOut("CLASS_NAME" + div, "DUMMY"); // 教科名
                    //     svf.VrAttribute("CLASS_NAME" + div, "X=10000");
                    // } else {
                    //     svf.VrsOut("CLASS_NAME" + div, ch); // 教科名
                    // }

                    if (ci < vc.getViewSize()) {
                        int viewKeta;
                        String[] viewField;
                        if (_formDiv == FORM_DIV3) {
                            viewKeta = 16;
                            if (getTokenList(vc.getViewName(ci), viewKeta).size() == 1) {
                                viewField = new String[] {"VIEW" + div + "_1"};
                            } else {
                                viewField = new String[] {"VIEW" + div + "_2", "VIEW" + div + "_1", "VIEW" + div + "_3"};
                            }
                        } else {
                            viewKeta = 26;
                            if (getTokenList(vc.getViewName(ci), viewKeta).size() == 1) {
                                viewField = new String[] {"VIEW" + div + "_1"};
                            } else {
                                viewField = new String[] {"VIEW" + div + "2", "VIEW" + div + "3"};
                            }
                        }
                        final List<String> viewnameTokenList = getTokenList(vc.getViewName(ci), viewKeta);
                        log.info(" viewnameTokenList = " + viewnameTokenList + " / " + vc.getViewCd(ci));
                        for (int i = 0; i < Math.min(viewnameTokenList.size(), viewField.length); i++) {
                            svf.VrsOut(viewField[i], viewnameTokenList.get(i)); // 観点
                        }

                        int keta;
                        String[] field;
                        final String meate = vc.getMeate(ci);
                        if (_formDiv == FORM_DIV3) {
                            keta = 32;
                            if (vci >= formDiv3recordCount) {
//                                if (getTokenList(meate, keta).size() <= 3) {
//                                    field = new String[] {"TARGET" + div + "_1_2", "TARGET" + div + "_1_1", "TARGET" + div + "_1_3"};
//                                } else {
                                    keta = 40;
                                    if (getTokenList(meate, keta).size() <= 3) {
                                        field = new String[] {"TARGET" + div + "_2_2", "TARGET" + div + "_2_1", "TARGET" + div + "_2_3"};
                                    } else {
                                        field = new String[] {"TARGET" + div + "_3_2", "TARGET" + div + "_3_1", "TARGET" + div + "_3_3", "TARGET" + div + "_3_4"};
                                    }
//                                }
                            } else {
//                                if (getTokenList(meate, keta).size() <= 5) {
//                                    field = new String[] {"TARGET" + div + "_1_2", "TARGET" + div + "_1_1", "TARGET" + div + "_1_3", "TARGET" + div + "_1_4", "TARGET" + div + "_1_5"};
//                                } else {
                                    keta = 40;
                                    if (getTokenList(meate, keta).size() <= 5) {
                                        field = new String[] {"TARGET" + div + "_2_2", "TARGET" + div + "_2_1", "TARGET" + div + "_2_3", "TARGET" + div + "_2_4", "TARGET" + div + "_2_5"};
                                    } else {
                                        field = new String[] {"TARGET" + div + "_3_2", "TARGET" + div + "_3_1", "TARGET" + div + "_3_3", "TARGET" + div + "_3_4", "TARGET" + div + "_3_5", "TARGET" + div + "_3_6"};
                                    }
//                                }
                            }
                        } else {
                            keta = 66;
                            if (getTokenList(meate, keta).size() == 1) {
                                field = new String[] {"TARGET" + div + "_1"};
                            } else {
                                field = new String[] {"TARGET" + div + "_2", "TARGET" + div + "_3"};
                                if (getTokenList(meate, keta).size() > field.length) {
                                    if (_formDiv == FORM_DIV2) {
                                        keta = 70;
                                    }
                                    field = new String[] {"TARGET" + div + "_4", "TARGET" + div + "_5", "TARGET" + div + "_6"};
                                }
                            }
                        }
                        final List<String> meateTokenList = getTokenList(meate, keta);
                        for (int i = 0; i < Math.min(meateTokenList.size(), field.length); i++) {
                            svf.VrsOut(field[i], meateTokenList.get(i)); // 観点
                        }

                        View view = student.getView(param._semester, vc._subclasscd, vc.getViewCd(ci));
                        if (null != view) {
                            svf.VrsOut("VALUE" + div, view._statusName1); // 評価
                        }
                    }
                    svf.VrEndRecord();
                    line += 1;
                }
            }

            for (int i = line; i < maxLine1; i++) {
                svf.VrsOut("CLASS_NAME1", String.valueOf(i)); // 教科名
                svf.VrAttribute("CLASS_NAME1", "X=10000");

                svf.VrEndRecord();
            }
        }
    }

    /**
     * 学校生活のようす（マスタ）
     */
    private static class BehaviorSemesLMst {
        final String _lCd;
        final String _lName;
        final List<BehaviorSemesMMst> _mMstList = new ArrayList();

        BehaviorSemesLMst(
            final String lCd,
            final String lName
        ) {
            _lCd = lCd;
            _lName = lName;
        }

        private static BehaviorSemesLMst getBehaviorSemesLMst(final List<BehaviorSemesLMst> list, final String lCd) {
            if (null == lCd) {
                return null;
            }
            for (final BehaviorSemesLMst lMst : list) {
                if (lCd.equals(lMst._lCd)) {
                    return lMst;
                }
            }
            return null;
        }

        private static class BehaviorSemesMMst {
            final String _lCd;
            final String _mCd;
            final String _mName;
            public BehaviorSemesMMst(final String lCd, final String mCd, final String mName) {
                _lCd = lCd;
                _mCd = mCd;
                _mName = mName;
            }
        }

        public static List<BehaviorSemesLMst> getBehaviorSemesLMstList(final DB2UDB db2, final Param param, final String grade) {
            final List<BehaviorSemesLMst> list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, grade);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd = rs.getString("L_CD");

                    if (null == getBehaviorSemesLMst(list, lCd)) {
                        final String lName = rs.getString("L_NAME");
                        final BehaviorSemesLMst behaviorsemeslmst = new BehaviorSemesLMst(lCd, lName);
                        list.add(behaviorsemeslmst);
                    }

                    final String mCd = rs.getString("M_CD");
                    final String mName = rs.getString("M_NAME");
                    getBehaviorSemesLMst(list, lCd)._mMstList.add(new BehaviorSemesMMst(lCd, mCd, mName));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.L_CD, ");
            stb.append("     T1.L_NAME, ");
            stb.append("     T2.M_CD, ");
            stb.append("     T2.M_NAME ");
            stb.append(" FROM HREPORT_BEHAVIOR_L_MST T1 ");
            stb.append(" LEFT JOIN HREPORT_BEHAVIOR_M_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND T2.L_CD = T1.L_CD ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SCHOOL_KIND = 'P' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.L_CD, ");
            stb.append("     T2.M_CD ");
            return stb.toString();
        }
    }

    /**
     * 学校生活のようす
     */
    private static class HreportBehaviorLmDat {

        final String _semester;
        final String _lCd;
        final String _mCd;
        final String _record;
        final String _nmd036Namespare1;

        public HreportBehaviorLmDat(
                final String semester,
                final String lCd,
                final String mCd,
                final String record,
                final String nmd036Namespare1) {
            _semester = semester;
            _lCd = lCd;
            _mCd = mCd;
            _record = record;
            _nmd036Namespare1 = nmd036Namespare1;
        }

        public static HreportBehaviorLmDat getHreportBehaviorLmDat(final String semester, final String lCd, final String mCd, final List lmList) {
            if (null == semester || null == lCd || null == mCd) {
                return null;
            }
            for (final Iterator it = lmList.iterator(); it.hasNext();) {
                final HreportBehaviorLmDat lmDat = (HreportBehaviorLmDat) it.next();
                if (semester.equals(lmDat._semester) && lCd.equals(lmDat._lCd) && mCd.equals(lmDat._mCd)) {
                    return lmDat;
                }
            }
            return null;
        }

        public static void setHreportBehaviorLmDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHreportBehaviorLmDatSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hrepoBehavLmDatList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String lCd = rs.getString("L_CD");
                        final String mCd = rs.getString("M_CD");
                        final String record = rs.getString("RECORD");
                        final String nmd036Namespare1 = rs.getString("NAMESPARE1");

                        final HreportBehaviorLmDat behaviorSemesDat = new HreportBehaviorLmDat(semester, lCd, mCd, record, nmd036Namespare1);

                        student._hrepoBehavLmDatList.add(behaviorSemesDat);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHreportBehaviorLmDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("   , NMD036.NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     HREPORT_BEHAVIOR_LM_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST NMD036 ON NMD036.NAMECD1 = 'D036' ");
            stb.append("         AND NMD036.NAME1 = T1.RECORD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final String _subclassname;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv,
                final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _electDiv = electDiv;
            _subclassname = subclassname;
            _viewList = new ArrayList();
            _valuationList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname, final String meate) {
            _viewList.add(new String[]{viewcd, viewname, meate});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public String getMeate(final int i) {
            return ((String[]) _viewList.get(i))[2];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                log.info(" view class sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final String remark1 = rs.getString("REMARK1");

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }

                    if (null == viewClass) {
                        final String electDiv = rs.getString("ELECTDIV");
                        final String subclassname = rs.getString("SUBCLASSNAME");

                        viewClass = new ViewClass(classcd, subclasscd, electDiv, subclassname);
                        list.add(viewClass);
                    }

                    viewClass.addView(viewcd, viewname, remark1);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME, ");
            stb.append("     DET.REMARK1 ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     LEFT JOIN JVIEWNAME_GRADE_SEMES_DETAIL_MST DET ON DET.YEAR = T2.YEAR ");
            stb.append("         AND DET.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND DET.GRADE = T1.GRADE ");
            stb.append("         AND DET.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND DET.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND DET.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND DET.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND DET.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param.SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T4.SHOWORDER3, -1), ");
            stb.append("     T4.CLASSCD, ");
            stb.append("     T4.SCHOOL_KIND, ");
            stb.append("     T4.CURRICULUM_CD, ");
            stb.append("     T4.SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class View {

        String _semester;
        String _viewcd;
        String _status;
        String _statusName1;
        String _viewname;
        String _subclasscd;

        public static void setViewRecordList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewSql(param);
                log.info(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._viewList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final View view = new View();
                        view._semester = rs.getString("SEMESTER");
                        view._viewcd = rs.getString("VIEWCD");
                        view._status = rs.getString("STATUS");
                        view._statusName1 = rs.getString("STATUS_NAME1");
                        view._viewname = rs.getString("VIEWNAME");
                        view._subclasscd = rs.getString("SUBCLASSCD");

                        student._viewList.add(view);
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getViewSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.NAME1 AS STATUS_NAME1 ");
            stb.append("     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T3.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = ? ");
            stb.append("     LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D029' ");
            stb.append("         AND T4.ABBV1 = T3.STATUS ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param.SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75846 $ $Date: 2020-08-05 11:25:19 +0900 (水, 05 8 2020) $"); // CVSキーワードの取り扱いに注意
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
        final String[] _categorySelected;
        final String _sdate;
        final String _edate;
        final String _descDate;
        final String _documentroot;
        final String _imagepath;
        final String _schoollogoFilePath;
        final String _schoolstampFilePath;
        final Map<String, String> _semesternameMap;

        final String _use_school_detail_gcm_dat;
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
        final String RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_P;

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

//        private Map _creditMst;

        private String _cerifSchoolSchoolName;
        private String _cerifSchoolJobName;
        private String _cerifSchoolPrincipalName;
        private String _cerifSchoolRemark2;
        private String _cerifSchoolRemark3;
        private boolean _isNoPrintMoto;
        final Map _attendParamMap;
        List<BehaviorSemesLMst> _behavSemesMstList;
        final List<ViewClass> _viewClassList;
        final String _d008Namecd1;

        final List _d026List = new ArrayList();


        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = _semester;
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _edate = KNJ_EditDate.H_Format_Haifun(request.getParameter("EDATE"));
            _descDate = null == request.getParameter("DESC_DATE") ? null : KNJ_EditDate.H_Format_Haifun(request.getParameter("DESC_DATE"));
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");

            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
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
            RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_P = request.getParameter("RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_P");

            setCertifSchoolDat(db2);

            _definecode = createDefineCode(db2);
            loadNameMstD026(db2);
//            setCreditMst(db2);
//            setRecordMockOrderSdivDat(db2);
            loadNameMstD016(db2);
            _behavSemesMstList = BehaviorSemesLMst.getBehaviorSemesLMstList(db2, this, _grade);
            if (_behavSemesMstList.isEmpty()) {
                _behavSemesMstList = BehaviorSemesLMst.getBehaviorSemesLMstList(db2, this, "00");
            }
            _viewClassList = ViewClass.getViewClassList(db2, this);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");

            _schoollogoFilePath = getImageFilePath("SCHOOLLOGO.jpg");
            _schoolstampFilePath = getImageFilePath("SCHOOLSTAMP_P.bmp");

            _gradeCd = getGradeCd(db2);
            _semesternameMap = getSemesternameMap(db2);

            final String tmpD008Cd = "D" + SCHOOLKIND + "08";
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

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        " + field + " ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
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

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK3, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '117' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _cerifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _cerifSchoolJobName = rs.getString("JOB_NAME");
                    _cerifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _cerifSchoolRemark2 = rs.getString("REMARK2");
                    _cerifSchoolRemark3 = rs.getString("REMARK3");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _cerifSchoolSchoolName = StringUtils.defaultString(_cerifSchoolSchoolName);
            _cerifSchoolJobName = StringUtils.defaultString(_cerifSchoolJobName, "校長");
            _cerifSchoolPrincipalName = StringUtils.defaultString(_cerifSchoolPrincipalName);
            _cerifSchoolRemark2 = StringUtils.defaultString(_cerifSchoolRemark2, "担任");
            _cerifSchoolRemark3 = StringUtils.defaultString(_cerifSchoolRemark3);
        }

        private Map getSemesternameMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeCd(final DB2UDB db2) {
            String rtn = null;
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("GRADE_CD");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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

            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
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
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

    }
}
