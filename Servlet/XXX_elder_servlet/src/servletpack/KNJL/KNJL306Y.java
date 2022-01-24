/*
 * $Id: 8e4b6e7ab1e0dcac534e531728e3f50669e86921 $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０６Ｙ＞  問題用紙用封筒ラベル
 **/
public class KNJL306Y {
    
    private static final Log log = LogFactory.getLog(KNJL306Y.class);
    
    private boolean _hasData;
    
    Param _param;
    
    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _param = createParam(db2, request);
            
            _hasData = false;
            
            printMain(db2, svf);
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL306Y.frm", 1);
        
        boolean isUpper = true; // 上段か
        
        final List schedules = getSchedules(db2);
        
        for (final Iterator itSch = schedules.iterator(); itSch.hasNext(); ) {
            final Schedule schedule = (Schedule) itSch.next();
            
            for (final Iterator itH = schedule._halls.iterator(); itH.hasNext();) {
                final Hall hall = (Hall) itH.next();
                
                final int upperlower = isUpper ? 1 : 2;
                
                svf.VrsOutn("NENDO", upperlower, _param._entexamYear + "年度");
                svf.VrsOutn("TITLE", upperlower, _param._title);
                svf.VrsOutn("SUBTITLE", upperlower, _param._subTitle);
                svf.VrsOutn("HALLNAME", upperlower, hall._examHallName);
                svf.VrsOutn("PERIOD", upperlower, schedule._periodCd + "時間目");
                svf.VrsOutn("PERIOD_TIME", upperlower, schedule._sHour + ":" + schedule._sMinutes + " 〜 " + schedule._eHour + ":" + schedule._eMinutes);
                svf.VrsOutn("SUBJECT", upperlower, schedule._testSubclassName);
                
                final String j = "1";
                int line1 = 1;
                int examnosCount = 0;
                for (final Iterator it = hall._courses.iterator(); it.hasNext();) {
                    final Course course = (Course) it.next();
                    line1 = printCourseLine(svf, j, line1, upperlower, course);
                    examnosCount += course._examnos.size();
                }
                
                svf.VrsOutn("PAGE", upperlower, String.valueOf(examnosCount));
                svf.VrsOutn("RESERVE", upperlower, _param._yobi);
                
                isUpper = !isUpper;
                
                if (isUpper) {
                    svf.VrEndPage();
                }
                _hasData = true;
            }
        }
        if (!isUpper) {
            svf.VrEndPage();
        }
    }
    
    private int printCourseLine(final Vrw32alp svf, String j, final int linei, final int upperlower, final Course course) {
        final int divn = 5;
        int li = linei;
        final List listList = Course.getDividedExamnoListList(course._examnos, divn);
        for (final Iterator it0 = listList.iterator(); it0.hasNext(); ) {
            final List examnos = (List) it0.next();
            final String line = String.valueOf(li);
            if (examnos.size() <= divn) {
                for (int i = 0; i < examnos.size(); i++) {
                    svf.VrsOutn("EXAMNO" + j + line + "_" + String.valueOf(i + 1), upperlower, (String) examnos.get(i));
                    svf.VrsOutn("COMMA" + j + line + "_" + String.valueOf(i), upperlower, ",");
                }
            } else {
                svf.VrsOutn("EXAMNO" + j + line + "_1", upperlower, (String) examnos.get(0));
                svf.VrsOutn("EXAMNO" + j + line + "_3", upperlower, "〜");
                svf.VrsOutn("EXAMNO" + j + line + "_5", upperlower, (String) examnos.get(examnos.size() - 1));
            }
            svf.VrsOut("TOTAL" + j + line + "_1", String.valueOf(course._examnos.size()));
            li += 1;
        }
        return li;
    }
    
    private void printCourseLine(final Vrw32alp svf, final String j, final String line, final int upperlower, final Course course) {
        final int size = course._examnos.size();
        if (size <= 5) { // 5人以下
            for (int i = 0; i < size; i++) {
                svf.VrsOutn("EXAMNO" + j + line + "_" + String.valueOf(i + 1), upperlower, (String) course._examnos.get(i));
                svf.VrsOutn("COMMA" + j + line + "_" + String.valueOf(i), upperlower, ",");
            }
        } else {
            svf.VrsOutn("EXAMNO" + j + line + "_1", upperlower, (String) course._examnos.get(0));
            svf.VrsOutn("EXAMNO" + j + line + "_3", upperlower, "〜");
            svf.VrsOutn("EXAMNO" + j + line + "_5", upperlower, (String) course._examnos.get(size - 1));
        }
        svf.VrsOutn("TOTAL" + j + String.valueOf(line) + "_1", upperlower, String.valueOf(size));
    }
    
    private Schedule getSchedule(List schedules, String patternno, String testSubclassCd) {
        if (null != patternno && null != testSubclassCd) {
            for (final Iterator it = schedules.iterator(); it.hasNext();) {
                final Schedule schedule = (Schedule) it.next();
                if (patternno.equals(schedule._patternno) && testSubclassCd.equals(schedule._testSubclassCd)) {
                    return schedule;
                }
            }
        }
        return null;
    }
    
    private Hall getHall(List halls, String examHallCd) {
        if (null != examHallCd) {
            for (final Iterator it = halls.iterator(); it.hasNext();) {
                final Hall hall = (Hall) it.next();
                if (examHallCd.equals(hall._examHallCd)) {
                    return hall;
                }
            }
        }
        return null;
    }
    
    private List getSchedules(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List schedules = new ArrayList();
        try {
            final String sql = getScheduleSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String patternno = rs.getString("PATTERN_NO");
                final String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                Schedule schedule = getSchedule(schedules, patternno, testSubclassCd);
                if (null == schedule) {
                    if (null == patternno && null == testSubclassCd) {
                        continue;
                    }
                    final String testSubclassName = "1".equals(_param._applicantDiv) ? rs.getString("TESTSUBCLASSNAME1") : rs.getString("TESTSUBCLASSNAME2");
                    final String periodCd = rs.getString("PERIODCD");
                    final String sHour = rs.getString("S_HOUR");
                    final String sMinute = rs.getString("S_MINUTE");
                    final String eHour = rs.getString("E_HOUR");
                    final String eMinute = rs.getString("E_MINUTE");
                    schedule = new Schedule(patternno, testSubclassCd, testSubclassName, periodCd, sHour, sMinute, eHour, eMinute);
                    schedules.add(schedule);
                }
                Hall hall = getHall(schedule._halls, rs.getString("EXAMHALLCD"));
                if (null == hall) {
                    if (null == rs.getString("EXAMHALLCD")) {
                        continue;
                    }
                    hall = new Hall(rs.getString("EXAMHALLCD"), rs.getString("EXAMHALL_NAME"));
                    schedule._halls.add(hall);
                }
                hall.addExamno(rs.getString("EXAMCOURSECD"), rs.getString("RECOM_KIND"), rs.getString("EXAMNO"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return schedules;
    }

    private static class Schedule {
        final String _patternno;
        final String _testSubclassCd;
        final String _testSubclassName;
        final String _periodCd;
        final String _sHour;
        final String _sMinutes;
        final String _eHour;
        final String _eMinutes;
        final List _halls = new ArrayList();
        public Schedule(final String patternno, final String testSubclassCd, final String testSubclassName, final String periodCd, final String sHour, final String sMinutes, final String eHour, final String eMinutes) {
            _patternno = patternno;
            _testSubclassCd = testSubclassCd;
            _testSubclassName = testSubclassName;
            _periodCd = periodCd;
            _sHour = sHour;
            _sMinutes = sMinutes;
            _eHour = eHour;
            _eMinutes = eMinutes;
        }
    }

    private static class Hall {
        final String _examHallCd;
        final String _examHallName;
        final List _courses = new ArrayList();
        
        public Hall(String examHallCd, String examHallName) {
            _examHallCd = examHallCd;
            _examHallName = examHallName;
        }
        
        public void addExamno(final String examCourseCd, final String recomKind, final String examno) {
            final Course course = getCourse(_courses, StringUtils.defaultString(examCourseCd), StringUtils.defaultString(recomKind));
            if (null != course) {
                course._examnos.add(examno);
            }
        }
        
        private static Course getCourse(final List courses, final String examCourseCd, final String recomKind) {
            for (final Iterator it = courses.iterator(); it.hasNext();) {
                final Course course = (Course) it.next();
                if (examCourseCd.equals(course._examCourseCd) && recomKind.equals(course._recomKind)) {
                    return course;
                }
            }
            courses.add(new Course(examCourseCd, recomKind));
            return getCourse(courses, examCourseCd, recomKind);
        }
        
        public String toString() {
            return "[" + _examHallCd + " : " + _examHallName + " : " + _courses + "]";
        }
    }
    
    private static class Course {
        final String _examCourseCd;
        final String _recomKind;
        final List _examnos = new ArrayList();
        public Course(String examCoursecd, String recomKind) {
            _examCourseCd = examCoursecd;
            _recomKind = recomKind;
        }
        /**
         * 分割数で分割された受験番号のリストのリストを得る。
         * ただしひとつの受験番号リストが分割数以上連続の場合、非連続になるまでの個数で分割する。
         *  getDividedExamnoListList([1,2,3,4,5,7,8,10,12,13,14], 3) => [[1,2,3,4,5],[7,8,10],[12,13,14]]
         * @param examnos0 受験番号のリスト
         * @param divn 分割数
         * @return 受験番号のリストのリスト
         */
        private static List getDividedExamnoListList(final List examnos0, final int divn) {
            final List listList = new ArrayList();
            List examnos = null;
            for (final Iterator it = examnos0.iterator(); it.hasNext(); ) {
                final String examno = (String) it.next();
                if (examnos == null || !examnosIsSeq(examnos, examno) && divn <= examnos.size()) {
                    examnos = new ArrayList();
                    listList.add(examnos);
                }
                examnos.add(examno);
            }
            return listList;
        }
        /**
         * 受験番号リストと受験番号は連続か
         * @param examnos 受験番号リスト
         * @param examno 受験番号
         * @return 受験番号リストと受験番号は連続か
         */
        public static boolean examnosIsSeq(final List examnos, final String examno) {
            final int seqIndex = examnosSeqIndex(examnos);
            final boolean isSeq = null != examnos && examnos.size() - 1 == seqIndex && isSeq((String) examnos.get(examnos.size() - 1), examno);
            return  isSeq; 
        }
        /**
         * 受験番号リストは連続か
         * @param examnos 受験番号リスト
         * @return 受験番号リストは連続か
         */
        public static int examnosSeqIndex(final List examnos) {
            if (null == examnos || examnos.isEmpty()) {
                return 0;
            }
            String oldExamno = (String) examnos.get(0);
            for (int i = 1; i < examnos.size(); i++) {
                final String examno = (String) examnos.get(i);
                if (!isSeq(oldExamno, examno)) {
                    return i - 1;
                }
                oldExamno = examno;
            }
            return examnos.size() - 1;
        }
        private static boolean isSeq(final String arg1, final String arg2) {
            if (!StringUtils.isNumeric(arg1) || !StringUtils.isNumeric(arg2)) {
                return false;
            }
            return Math.abs(Long.parseLong(arg1) - Long.parseLong(arg2)) <= 1;
        }
    }
    
    private String getScheduleSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HALL_EXAMNO AS (");
        stb.append(" SELECT");
        stb.append("     T1.ENTEXAMYEAR,");
        stb.append("     T1.APPLICANTDIV,");
        stb.append("     T1.TESTDIV,");
        stb.append("     T1.EXAM_TYPE,");
        stb.append("     T1.EXAMHALLCD,");
        stb.append("     T1.EXAMHALL_NAME,");
        stb.append("     T2.RECEPTNO,");
        stb.append("     T2.EXAMNO");
        stb.append(" FROM");
        stb.append("     ENTEXAM_HALL_YDAT T1");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV");
        stb.append("         AND T2.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("         AND T2.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO");
        stb.append(" WHERE");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear  +"'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T1.EXAM_TYPE = '1'");
        stb.append("     AND T1.EXAMHALLCD IN ").append(_param._examHallCds);
        stb.append(" ORDER BY");
        stb.append("     T2.RECEPTNO");
        stb.append(" )");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMHALLCD,");
        stb.append("     T1.EXAMHALL_NAME,");
        stb.append("     T1.TESTDIV,");
        stb.append("     T7.PATTERN_NO,");
        stb.append("     T7.TESTSUBCLASSCD,");
        stb.append("     NM1.NAME1 AS TESTSUBCLASSNAME1,");
        stb.append("     NM1.NAME2 AS TESTSUBCLASSNAME2,");
        stb.append("     T7.PERIODCD,");
        stb.append("     T7.S_HOUR,");
        stb.append("     T7.S_MINUTE,");
        stb.append("     T7.E_HOUR,");
        stb.append("     T7.E_MINUTE,");
        stb.append("     T4.COURSECD,");
        stb.append("     T4.MAJORCD,");
        stb.append("     T4.EXAMCOURSECD,");
        stb.append("     T1.EXAMNO,");
        stb.append("     T1.RECEPTNO,");
        stb.append("     T5.RECOM_KIND");
        stb.append(" FROM");
        stb.append("     HALL_EXAMNO T1");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T5.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T5.TESTDIV = T1.TESTDIV");
        stb.append("         AND T5.EXAMNO = T3.EXAMNO");
        stb.append("     INNER JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV");
        stb.append("         AND T4.DESIREDIV = T5.DESIREDIV");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append("     INNER JOIN ENTEXAM_SCH_PTRN_SUB_DAT T7 ON T7.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T7.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T7.TESTDIV = T1.TESTDIV");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'L009' AND NM1.NAMECD2 = T7.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T7.PATTERN_NO = '" + _param._schPtrn + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T7.PERIODCD, T7.TESTSUBCLASSCD, T1.RECEPTNO ");
        
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final String _examHallCds;
        final String _yobi;
        final String _schPtrn;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            _examHallCds = SQLUtils.whereIn(true, request.getParameterValues("CATEGORY_SELECTED"));
            _yobi = request.getParameter("YOBI");
            _schPtrn = request.getParameter("SCH_PTRN");
        }
        
        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof
