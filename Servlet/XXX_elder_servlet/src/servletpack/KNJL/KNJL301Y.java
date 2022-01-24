/*
 * $Id: 9144996e42cf813aa01a244b9a002afbf420f1d7 $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０１Ｙ＞  志願者データチェックリスト
 **/
public class KNJL301Y {
    
    private static final Log log = LogFactory.getLog(KNJL301Y.class);
    
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
    
    /**
     * 文字列をMS932でエンコードしたバイト数を得る
     * @param str 文字列
     * @return バイト数
     */
    private int keta(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
        }
        return count;
    }
    
    /**
     * 改ページするか
     * @param oldExamHallCd
     * @param newExamHallCd
     * @param oldExamCourseCd
     * @param newExamCourseCd
     * @param oldRecomKind
     * @param newRecomKind
     * @return 改ページするか
     */
    private boolean isNewpage(final String oldExamHallCd, final String newExamHallCd, final String oldExamCourseCd, final String newExamCourseCd, final String oldRecomKind, final String newRecomKind) {
        final boolean isDiffExamHall =  !(oldExamHallCd == null && newExamHallCd == null) && !(oldExamHallCd != null && oldExamHallCd.equals(newExamHallCd));
        final boolean isDiffCourse =  !(oldExamCourseCd == null && newExamCourseCd == null) && !(oldExamCourseCd != null && oldExamCourseCd.equals(newExamCourseCd));
        final boolean isDiffRecomKind = !(oldRecomKind == null && newRecomKind == null) && !(oldRecomKind != null && oldRecomKind.equals(newRecomKind));
        return isDiffExamHall || isDiffCourse || isDiffRecomKind;
    }
    
    /**
     * データから最大ページ数を取得
     * @param applicants 表示データ
     * @param linePerPage 1ページあたりに表示するデータ行数
     * @return 最大ページ数
     */
    private int getPageMax(final List applicants, final int linePerPage) {
        int page = 1;
        int line = 1;
        String oldExamHallCd = null;
        String oldExamCoursecd = null;
        String oldRecomKind = null;
        
        for (final Iterator it = applicants.iterator(); it.hasNext();) {
            Applicant applicant = (Applicant) it.next();
            if (line != 1 && isNewpage(oldExamHallCd, applicant._examHallCd, oldExamCoursecd, applicant._examCourseCd, oldRecomKind, applicant._recomKind)) {
                page += 1;
                line = 1;
            }
            oldExamHallCd = applicant._examHallCd;
            oldRecomKind = applicant._recomKind;
            oldExamCoursecd = applicant._examCourseCd;
            line += 1;
            if (linePerPage < line) {
                page += 1;
                line = 1;
            }
        }
        return page;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String formname = "KNJL301Y.frm";
        svf.VrSetForm(formname, 4);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List applicants = getApplicants(db2, ps, rs);
        final int linePerPage = 40;
        int line = 1;
        String oldExamHallCd = null;
        String oldExamCoursecd = null;
        String oldRecomKind = null;

        for (final Iterator it = applicants.iterator(); it.hasNext();) {
            final Applicant applicant = (Applicant) it.next();
            
            if (line != 1 && isNewpage(oldExamHallCd, applicant._examHallCd, oldExamCoursecd, applicant._examCourseCd, oldRecomKind, applicant._recomKind)) {
                svf.VrSetForm(formname, 4);
                line = 1;
            }
            
            svf.VrsOut("NENDO",            _param._year + "年度　");
            svf.VrsOut("DATE",             _param._dateString);
            svf.VrsOut("TITLE",            _param._title);
            svf.VrsOut("SUBTITLE",         _param._subTitle);
            svf.VrsOut("HALLNAME",         applicant._examHallName);
            svf.VrsOut("COURSE",           applicant._examCourseName);
            svf.VrsOut("RECOMMEND",        applicant._recomKindName);
            
            final String nameField     = "NAME" + (30 < keta(applicant._name) ? "3" : (20 < keta(applicant._name) ? "2" : "1"));
            final String nameKanaField = "NAME_KANA" + (50 < keta(applicant._nameKana) ? "3" : (20 < keta(applicant._nameKana) ? "2" : "1"));
            
            svf.VrsOut("SEATNO",           String.valueOf(line));
            svf.VrsOut("EXAMNO",           applicant._examno);
            svf.VrsOut(nameField,          applicant._name);
            svf.VrsOut(nameKanaField,      applicant._nameKana);
            svf.VrsOut("SEX",              applicant._sex);
            svf.VrsOut("EXAMCOURSE_NAME1", applicant._examCourseName);
            svf.VrsOut("EXAMCOURSE_NAME2", applicant._examCourseName2);
            svf.VrsOut("FINSCHOOL_ABBV",   applicant._finschoolAbbv);
            svf.VrsOut("SP_JUDGE",         applicant._shiftDesireFlg);
            svf.VrEndRecord();
            
            _hasData = true;
            oldExamHallCd = applicant._examHallCd;
            oldRecomKind = applicant._recomKind;
            oldExamCoursecd = applicant._examCourseCd;
            line += 1;
            if (linePerPage < line) {
                line = 1;
            }
        }
    }

    private List getApplicants(final DB2UDB db2, PreparedStatement ps, ResultSet rs) {
        final List applicants = new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examHallCd = rs.getString("EXAMHALLCD");
                final String examHallName = rs.getString("EXAMHALL_NAME");
                final String examno = rs.getString("EXAMNO");
                final String examCourseCd = rs.getString("EXAMCOURSECD");
                final String recomKind = rs.getString("RECOM_KIND");
                final String examCourseName = rs.getString("EXAMCOURSE_NAME");
                final String recomKindName = rs.getString("RECOM_KINDNAME");
                final String name     = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String shiftDesireFlg = "1".equals(rs.getString("SHIFT_DESIRE_FLG")) ? "有" : "";
                final String generalFlg = "1".equals(rs.getString("GENERAL_FLG")) ? "有" : "";
                final String sportsFlg = "1".equals(rs.getString("SPORTS_FLG")) ? "有" : "";
                final String dormFlg = "1".equals(rs.getString("DORMITORY_FLG")) ? "有" : "";
                final String shDivName = rs.getString("SH_DIVNAME");
                final String shSchoolName = rs.getString("SH_SCHOOL_NAME");
                final String sremark1 = rs.getString("REMARK1");
                final String sremark2 = rs.getString("REMARK2");
                final String sex = rs.getString("SEX");
                final String examCourseName2 = rs.getString("EXAMCOURSE_NAME2");
                final String finschoolAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                
                final Applicant applicant = new Applicant(examHallCd, examHallName, examno, examCourseCd, recomKind, examCourseName, recomKindName, name, nameKana, shiftDesireFlg, generalFlg, sportsFlg, dormFlg, shDivName, shSchoolName, sremark1, sremark2, sex, examCourseName2, finschoolAbbv);
                applicants.add(applicant);
                
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicants;
    }
    
    private class Applicant {
        final String _examHallCd;
        final String _examHallName;
        final String _examno;
        final String _examCourseCd;
        final String _recomKind;
        final String _examCourseName;
        final String _recomKindName;
        final String _name;
        final String _nameKana;
        final String _shiftDesireFlg;
        final String _generalFlg;
        final String _sportsFlg;
        final String _dormFlg;
        final String _shDivName;
        final String _shSchoolName;
        final String _remark1;
        final String _remark2;
        final String _sex;
        final String _examCourseName2;
        final String _finschoolAbbv;
        public Applicant(
                final String examHallCd,
                final String examHallName,
                final String examno,
                final String examCourseCd,
                final String recomKind,
                final String examCourseName,
                final String recomKindName,
                final String name,
                final String nameKana,
                final String shiftDesireFlg,
                final String generalFlg,
                final String sportsFlg,
                final String dormFlg,
                final String shDivName,
                final String shSchoolName,
                final String remark1,
                final String remark2,
                final String sex, final String examCourseName2, final String finschoolAbbv) {
            _examHallCd = examHallCd;
            _examHallName = examHallName;
            _examno = examno;
            _examCourseCd = examCourseCd;
            _recomKind = recomKind;
            _examCourseName = examCourseName;
            _recomKindName = recomKindName;
            _name = name;
            _nameKana = nameKana;
            _shiftDesireFlg = shiftDesireFlg;
            _generalFlg = generalFlg;
            _sportsFlg = sportsFlg;
            _dormFlg = dormFlg;
            _shDivName = shDivName;
            _shSchoolName = shSchoolName;
            _remark1 = remark1;
            _remark2 = remark2;
            _sex = sex;
            _examCourseName2 = examCourseName2;
            _finschoolAbbv = finschoolAbbv;
        }
        

    }

    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH ENTEXAM_HALL_EXAMNO AS ( ");
        stb.append("  SELECT ");
        stb.append("      T1.TESTDIV, ");
        stb.append("      T1.EXAMHALLCD, ");
        stb.append("      T1.EXAMHALL_NAME, ");
        stb.append("      T3.EXAMNO ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_HALL_YDAT T1 ");
        stb.append("      INNER JOIN ENTEXAM_HALL_DETAIL_YDAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("          AND T2.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("          AND T2.EXAMHALLCD = T1.EXAMHALLCD ");
        stb.append("      INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("          AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("          AND T3.RECEPTNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO ");
        stb.append("  WHERE ");
        stb.append("      T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("      AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("      AND T1.EXAM_TYPE = '1' ");
        stb.append("  ORDER BY ");
        stb.append("      T3.RECEPTNO ");
        stb.append(") ");
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     T4.EXAMCOURSECD, ");
        stb.append("     T6.EXAMCOURSE_NAME, ");
        stb.append("     T5.RECOM_KIND, ");
        stb.append("     NM5.NAME1 AS RECOM_KINDNAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     NM1.NAME2 AS SEX, ");
        stb.append("     T3.FINSCHOOL_NAME_ABBV, ");
        stb.append("     NM3.ABBV1 AS FS_GRDDIVNAME, ");
        stb.append("     (CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T8.EXAMCOURSE_NAME ELSE '' END) AS EXAMCOURSE_NAME2, ");
        stb.append("     T2.TELNO, ");
        stb.append("     T2.ZIPCD, ");
        stb.append("     T2.ADDRESS1, ");
        stb.append("     T2.ADDRESS2, ");
        stb.append("     T2.GNAME, ");
        stb.append("     T2.GKANA, ");
        stb.append("     T2.RELATIONSHIP, ");
        stb.append("     NM2.NAME1 AS RELATIONSHIP_NAME, ");
        stb.append("     T1.SHIFT_DESIRE_FLG, ");
        stb.append("     T1.GENERAL_FLG, ");
        stb.append("     T1.SPORTS_FLG, ");
        stb.append("     T1.DORMITORY_FLG, ");
        stb.append("     T5.SHDIV, ");
        stb.append("     NM4.NAME1 AS SH_DIVNAME, ");
        stb.append("     T9.FINSCHOOL_NAME AS SH_SCHOOL_NAME, ");
        stb.append("     T1.REMARK1, ");
        stb.append("     T1.REMARK2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_HALL_EXAMNO HALL ON HALL.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T3 ON T3.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T5.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T5.TESTDIV = HALL.TESTDIV ");
        stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T5.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T5.TESTDIV ");
        stb.append("         AND T4.DESIREDIV = T5.DESIREDIV ");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T6 ON T6.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
        stb.append("         AND T6.APPLICANTDIV = T4.APPLICANTDIV ");
        stb.append("         AND T6.TESTDIV = T4.TESTDIV ");
        stb.append("         AND T6.COURSECD = T4.COURSECD ");
        stb.append("         AND T6.MAJORCD = T4.MAJORCD ");
        stb.append("         AND T6.EXAMCOURSECD = T4.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T7 ON T7.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
        stb.append("         AND T7.APPLICANTDIV = T5.APPLICANTDIV ");
        stb.append("         AND T7.TESTDIV = T5.TESTDIV ");
        stb.append("         AND T7.DESIREDIV = T5.DESIREDIV ");
        stb.append("         AND T7.WISHNO = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T8 ON T8.ENTEXAMYEAR = T7.ENTEXAMYEAR ");
        stb.append("         AND T8.APPLICANTDIV = T7.APPLICANTDIV ");
        stb.append("         AND T8.TESTDIV = T7.TESTDIV ");
        stb.append("         AND T8.COURSECD = T7.COURSECD ");
        stb.append("         AND T8.MAJORCD = T7.MAJORCD ");
        stb.append("         AND T8.EXAMCOURSECD = T7.EXAMCOURSECD ");
        stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T9 ON T9.FINSCHOOLCD = T1.SH_SCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'Z002' AND NM1.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST NM2 ON NM2.NAMECD1 = 'H201' AND NM2.NAMECD2 = T2.RELATIONSHIP ");
        stb.append("     LEFT JOIN NAME_MST NM3 ON NM3.NAMECD1 = 'L016' AND NM3.NAMECD2 = T1.FS_GRDDIV ");
        stb.append("     LEFT JOIN NAME_MST NM4 ON NM4.NAMECD1 = 'L006' AND NM4.NAMECD2 = T5.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NM5 ON NM5.NAMECD1 = 'L023' AND NM5.NAMECD2 = T5.RECOM_KIND ");
        stb.append("  WHERE ");
        stb.append("      T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     HALL.EXAMHALLCD, T4.EXAMCOURSECD, T5.RECOM_KIND, T1.EXAMNO ");
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
        private final String _year;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _title;
        private final String _dateString;
        private final String _subTitle;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            final Calendar cal = Calendar.getInstance();
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("LOGIN_DATE"))) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
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
