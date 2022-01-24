/*
 * $Id: 0dad00ee078ea4aa0ea5928d44ba1f7f9146de33 $
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０５Ｙ＞  封筒ラベル
 **/
public class KNJL305Y {
    
    private static final Log log = LogFactory.getLog(KNJL305Y.class);
    
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
        
        final List halls = getExamHalls(db2);
        final String[] document = KNJ_EditEdit.get_token(getEntexamDocumentDat(db2), 60, 3);
        for (final Iterator itH = halls.iterator(); itH.hasNext();) {
            final Hall hall = (Hall) itH.next();
            
            svf.VrSetForm("KNJL305Y.frm", 4);
            svf.VrsOut("NENDO", _param._entexamYear + "年度");
            svf.VrsOut("TITLE", _param._title);
            svf.VrsOut("SUBTITLE", _param._subTitle);
            svf.VrsOut("HALLNAME", hall._examHallName);
            
            // 上段
            final int maxCourseCount = 8;
            int courseCount1 = 0;
            for (final Iterator it = hall._courses.iterator(); it.hasNext();) {
                final Course course = (Course) it.next();
                if (courseCount1 >= maxCourseCount) {
                    continue;
                }
                courseCount1 = printCourseLineRecord(svf, null, "1", "1", course, courseCount1, maxCourseCount);
            }
            
            svf.VrsOut("HEADER_TOTAL", String.valueOf(getTotalCount(hall)));
            
            // 注意事項
            if (null != document) {
                for (int i = 0; i < document.length; i++) {
                    svf.VrsOut("NOTICE" + String.valueOf(i + 1), document[i]);
                }
            }
            
            int courseCount2 = 0;
            for (final Iterator itHd = hall._detailNos.iterator(); itHd.hasNext();) {
                final HallDetail hallDetail = (HallDetail) itHd.next();
                
                for (final Iterator it = hallDetail._courses.iterator(); it.hasNext();) {
                    final Course course = (Course) it.next();
                    if (courseCount2 >= maxCourseCount) {
                        continue;
                    }
                    courseCount2 = printCourseLineRecord(svf, hallDetail._detailNo, "2", "1", course, courseCount2, maxCourseCount);
                }
            }
            _hasData = true;
        }
    }

    private int getTotalCount(final Hall hall) {
        int total = 0;
        for (final Iterator itHd = hall._detailNos.iterator(); itHd.hasNext();) {
            final HallDetail hallDetail = (HallDetail) itHd.next();
            for (final Iterator it = hallDetail._courses.iterator(); it.hasNext();) {
                final Course course = (Course) it.next();
                total += course._examnos.size();
            }
        }
        return total;
    }
    
    private int printCourseLineRecord(final Vrw32alp svf, String detailNo, String j, String line, final Course course, final int currentLine, final int maxLine) {
        final int divn = 5;
        final List listList = Course.getDividedExamnoListList(course._examnos, divn);
        int l = currentLine;
        for (final Iterator it0 = listList.iterator(); it0.hasNext(); ) {
            final List examnos = (List) it0.next();
            if (l >= maxLine) {
                continue;
            }
            if (null != detailNo) {
                svf.VrsOut("NO", detailNo);
            }
            if (examnos.size() <= divn) {
                for (int i = 0; i < examnos.size(); i++) {
                    svf.VrsOut("EXAMNO" + j + line + "_" + String.valueOf(i + 1), (String) examnos.get(i));
                    svf.VrsOut("COMMA" + j + line + "_" + String.valueOf(i), ",");
                }
            } else {
                svf.VrsOut("EXAMNO" + j + line + "_1", (String) examnos.get(0));
                svf.VrsOut("EXAMNO" + j + line + "_3", "〜");
                svf.VrsOut("EXAMNO" + j + line + "_5", (String) examnos.get(examnos.size() - 1));
            }
            svf.VrsOut("TOTAL" + j + line + "_1", String.valueOf(examnos.size()));
            svf.VrEndRecord();
            l += 1;
        }
        return l;
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
    
    private List getExamHalls(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List halls = new ArrayList();
        try {
            final String sql = getApplicantSql();
//            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                Hall hall = getHall(halls, rs.getString("EXAMHALLCD"));
                if (null == hall) {
                    if (null == rs.getString("EXAMHALLCD")) {
                        continue;
                    }
                    hall = new Hall(rs.getString("EXAMHALLCD"), rs.getString("EXAMHALL_NAME"));
                    halls.add(hall);
                }
                hall.addExamno(rs.getString("DETAIL_NO"), rs.getString("EXAMCOURSECD"), rs.getString("RECOM_KIND"), rs.getString("EXAMNO"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return halls;
    }
    
    private String getEntexamDocumentDat(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        String document = "";
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     DOCUMENT ");
            sql.append(" FROM ");
            sql.append("     ENTEXAM_DOCUMENT_DAT ");
            sql.append(" WHERE ");
            sql.append("     ENTEXAMYEAR = '" + _param._entexamYear + "' AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            
            while (rs.next()) {
                document = rs.getString("DOCUMENT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return document;
    }
    
    private static class Hall {
        private String _examHallCd;
        private String _examHallName;
        private List _detailNos = new ArrayList();
        private List _courses = new ArrayList();
        
        public Hall(String examHallCd, String examHallName) {
            _examHallCd = examHallCd;
            _examHallName = examHallName;
        }
        
        public void addExamno(final String detailNo, final String examCourseCd, final String recomKind, final String examno) {
            final Course course = getCourse(_courses, StringUtils.defaultString(examCourseCd), StringUtils.defaultString(recomKind));
            if (null != course) {
                course._examnos.add(examno);
            }
            final HallDetail hallDetail = getDetail(_detailNos, null == detailNo ? "" : detailNo);
            if (null != hallDetail) {
                final Course detailCourse = getCourse(hallDetail._courses, StringUtils.defaultString(examCourseCd), StringUtils.defaultString(recomKind));
                detailCourse._examnos.add(examno);
            }
        }
        
        private static HallDetail getDetail(final List details, final String detailNo) {
            for (final Iterator it = details.iterator(); it.hasNext();) {
                final HallDetail hallDetail = (HallDetail) it.next();
                if (detailNo.equals(hallDetail._detailNo)) {
                    return hallDetail;
                }
            }
            details.add(new HallDetail(detailNo));
            return getDetail(details, detailNo);
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
    }
    
    private static class HallDetail {
        private String _detailNo;
        private List _courses = new ArrayList();
        public HallDetail(String detailNo) {
            _detailNo = detailNo;
        }
    }
    
    private static class Course {
        private String _examCourseCd;
        private String _recomKind;
        private List _examnos = new ArrayList();
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
        public String toString() {
            return "[Course " + _examCourseCd + "]";
        }
    }

    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HALL_EXAMNO AS (");
        stb.append(" SELECT");
        stb.append("     T1.ENTEXAMYEAR,");
        stb.append("     T1.APPLICANTDIV,");
        stb.append("     T1.TESTDIV,");
        stb.append("     T1.EXAM_TYPE,");
        stb.append("     T1.EXAMHALLCD,");
        stb.append("     T2.DETAIL_NO,");
        stb.append("     T1.EXAMHALL_NAME,");
        stb.append("     T3.RECEPTNO,");
        stb.append("     T3.EXAMNO");
        stb.append(" FROM");
        stb.append("     ENTEXAM_HALL_YDAT T1");
        stb.append("     INNER JOIN ENTEXAM_HALL_DETAIL_YDAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV");
        stb.append("         AND T2.EXAM_TYPE = T1.EXAM_TYPE");
        stb.append("         AND T2.EXAMHALLCD = T1.EXAMHALLCD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("         AND T3.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("         AND T3.TESTDIV = T2.TESTDIV");
        stb.append("         AND T3.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("         AND T3.RECEPTNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO");
        stb.append(" WHERE");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear  +"'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T1.EXAM_TYPE = '1'");
        stb.append("     AND T1.EXAMHALLCD IN ").append(_param._examHallCds);
        stb.append(" ORDER BY");
        stb.append("     T3.RECEPTNO");
        stb.append(" )");
        stb.append(" SELECT DISTINCT");
        stb.append("     T1.EXAMHALLCD,");
        stb.append("     T1.DETAIL_NO,");
        stb.append("     T1.EXAMHALL_NAME,");
        stb.append("     T1.TESTDIV,");
        stb.append("     T4.COURSECD,");
        stb.append("     T4.MAJORCD,");
        stb.append("     T4.EXAMCOURSECD,");
        stb.append("     T1.EXAMNO,");
        stb.append("     T1.RECEPTNO,");
        stb.append("     T5.RECOM_KIND");
        stb.append(" FROM");
        stb.append("     HALL_EXAMNO T1");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T5.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T5.TESTDIV = T1.TESTDIV");
        stb.append("         AND T5.EXAMNO = T3.EXAMNO");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV");
        stb.append("         AND T4.DESIREDIV = T5.DESIREDIV");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append(" ORDER BY");
        stb.append("     T1.RECEPTNO");
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

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            _examHallCds = SQLUtils.whereIn(true, request.getParameterValues("CATEGORY_SELECTED"));
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
