/*
 * $Id: b944b7f91693672ed746b2a5278bf247414ce64c $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０９Ｙ＞  面接用紙
 **/
public class KNJL309Y {
    
    private static final Log log = LogFactory.getLog(KNJL309Y.class);
    
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
    
    private int getMaxPage(final List hallDetails, final int maxLine) {
        int page = 0;
        for (Iterator it = hallDetails.iterator(); it.hasNext(); ) {
            final HallDetail hall = (HallDetail) it.next();
            final int p = hall._applicants.size() / maxLine;
            page += (hall._applicants.size() % maxLine == 0 ? p : p + 1);
        }
        return page;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List hallDetails = getHallDetails(db2);
        final int maxLine = 20;
        final int maxPage = getMaxPage(hallDetails, maxLine);

        int page = 0;
        for (final Iterator itH = hallDetails.iterator(); itH.hasNext();) {
            final HallDetail hallDetail = (HallDetail) itH.next();
            svf.VrSetForm("1".equals(_param._applicantDiv) ? "KNJL309Y_1.frm" : "KNJL309Y_2.frm", 4);
            page += 1;
            int line = 0;
            
            for (final Iterator it = hallDetail._applicants.iterator(); it.hasNext();) {
                line += 1;
                if (maxLine < line) {
                    line -= maxLine;
                    page += 1;
                }
                
                final Applicant applicant = (Applicant) it.next();

                svf.VrsOut("NENDO", _param._entexamYear + "年度");
                svf.VrsOut("TITLE", _param._title);
                final String subTitle2 = ("1".equals(_param._shiftDesireFlg)) ? "：特別判定" : "";
                svf.VrsOut("SUBTITLE", _param._subTitle + subTitle2);
                svf.VrsOut("DATE", _param._dateString);
                svf.VrsOut("ORAL_HALL", hallDetail._examHallName + "(" + hallDetail._examHallDetailNo + ")");
                svf.VrsOut("PAGE1", String.valueOf(page));
                svf.VrsOut("PAGE2", String.valueOf(maxPage));
                
                svf.VrsOut("EXAMNO", applicant._examno);
                svf.VrsOut("EXAMCOURSE_NAME1", applicant._examCourseName1);
                if ("2".equals(_param._applicantDiv)) {
                    svf.VrsOut("FINSCHOOL_ABBV", applicant._finschoolNameAbbv);
                    svf.VrsOut("AVERAGE_ALL", applicant._averageAll);
                    svf.VrsOut("SH", applicant._shdivname);
                    svf.VrsOut("SH_SCHOOL", applicant._shFinschoolname);
                } else {
                    svf.VrsOut("FINSCHOOL_ABBV", applicant._finschoolName);
                    if (!"1".equals(_param._form)) {
                        svf.VrsOut("INTERVIEW_REMARK1", applicant._interviewRemark);
                    }
                    svf.VrsOut("GRD_ABBV", applicant._grddivName);
                }
                final String[] remark1 = KNJ_EditEdit.get_token(applicant._remark1, 40, 4);
                if (null != remark1) {
                    for (int i = 0; i < remark1.length; i++) {
                        svf.VrsOut("REMARK1_" + (i + 1), remark1[i]);
                    }
                }
                
                svf.VrsOut("DORMITORY", "1".equals(applicant._dormitoryFlg) ? "有" : "");
                svf.VrsOut("ABSENCE_DAYS", applicant._absenceDays);
                svf.VrsOut("SEX", applicant._sexname);
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }
    
    private HallDetail getHall(final List halls, final String examHallCd, final String examHallDetailCd) {
        if (null != examHallCd && null != examHallDetailCd) {
            for (final Iterator it = halls.iterator(); it.hasNext();) {
                final HallDetail hall = (HallDetail) it.next();
                if (examHallCd.equals(hall._examHallCd) && examHallDetailCd.equals(hall._examHallDetailNo)) {
                    return hall;
                }
            }
        }
        return null;
    }
    
    private List getHallDetails(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List halldetails= new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                HallDetail hallDetail = getHall(halldetails, rs.getString("EXAMHALLCD"), rs.getString("DETAIL_NO"));
                if (null == hallDetail) {
                    if (null == rs.getString("EXAMHALLCD")) {
                        continue;
                    }
                    hallDetail = new HallDetail(rs.getString("EXAMHALLCD"), rs.getString("DETAIL_NO"), rs.getString("EXAMHALL_NAME"));
                    halldetails.add(hallDetail);
                }
                
                final String examno = rs.getString("EXAMNO");
                final String examCourseName1 = rs.getString("EXAMCOURSE_NAME1");
                final String recomKind = rs.getString("RECOM_KIND");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String absenceDays = formatKeta(rs.getString("ABSENCE_DAYS")) + "," + formatKeta(rs.getString("ABSENCE_DAYS2")) + "," + formatKeta(rs.getString("ABSENCE_DAYS3"));
                final String averageAll = NumberUtils.isNumber(rs.getString("AVERAGE_ALL")) ? String.valueOf(new BigDecimal(rs.getString("AVERAGE_ALL")).setScale(0)) : null;
                final String grddivName = rs.getString("GRDDIVNAME");
                final String shdivname = rs.getString("SHDIVNAME");
                final String shFinschoolname = rs.getString("SH_FINSCHOOLNAME");
                final String sexname = rs.getString("SEXNAME");
                final String dormitoryFlg = rs.getString("DORMITORY_FLG");
                final String interviewRemark = rs.getString("INTERVIEW_REMARK");
                final String remark1 = rs.getString("REMARK1");
                
                final Applicant applicant = new Applicant(examno, examCourseName1, recomKind, finschoolName, finschoolNameAbbv, absenceDays, averageAll, grddivName,
                        shdivname, shFinschoolname, sexname, dormitoryFlg, interviewRemark, remark1);
                hallDetail._applicants.add(applicant);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return halldetails;
    }
    
    private String formatKeta(final String absenceDays) {
        if (!NumberUtils.isDigits(absenceDays)) {
            return "   ";
        }
        final int absenceDaysInt = Integer.parseInt(absenceDays);
        return (absenceDaysInt < 10 ? "  " : (absenceDaysInt < 100 ? " " : "")) + String.valueOf(absenceDaysInt); 
    }
    
    private static class Applicant {
        final String _examno;
        final String _examCourseName1;
        final String _recomKind;
        final String _finschoolName;
        final String _finschoolNameAbbv;
        final String _absenceDays;
        final String _averageAll;
        final String _grddivName;
        final String _shdivname;
        final String _shFinschoolname;
        final String _sexname;
        final String _dormitoryFlg;
        final String _interviewRemark;
        final String _remark1;
        Applicant(
                final String examno,
                final String examCourseName1,
                final String recomKind,
                final String finschoolName,
                final String finschoolNameAbbv,
                final String absenceDays,
                final String averageAll,
                final String grddivName,
                final String shdivname,
                final String shFinschoolname,
                final String sexname,
                final String dormitoryFlg,
                final String interviewRemark,
                final String remark1
                ) {
             _examno = examno;
             _examCourseName1 = examCourseName1;
             _recomKind = recomKind;
             _finschoolName = finschoolName;
             _finschoolNameAbbv = finschoolNameAbbv;
             _absenceDays = absenceDays;
             _averageAll = averageAll;
             _grddivName = grddivName;
             _shdivname = shdivname;
             _shFinschoolname = shFinschoolname;
             _sexname = sexname;
             _dormitoryFlg = dormitoryFlg;
             _interviewRemark = interviewRemark;
             _remark1 = remark1;
        }
    }
    
    private static class HallDetail {
        final String _examHallCd;
        final String _examHallDetailNo;
        final String _examHallName;
        final List _applicants = new ArrayList();
        
        public HallDetail(String examHallCd, String examHallDetailNo, String examHallName) {
            _examHallCd = examHallCd;
            _examHallDetailNo = examHallDetailNo;
            _examHallName = examHallName;
        }
    }
    
    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HALL_EXAMNO AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAM_TYPE, ");
        stb.append("     T1.EXAMHALLCD, ");
        stb.append("     T2.DETAIL_NO, ");
        stb.append("     T1.EXAMHALL_NAME, ");
        stb.append("     T3.RECEPTNO, ");
        stb.append("     T3.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_HALL_DETAIL_YDAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("          AND T2.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("          AND T2.EXAMHALLCD = T1.EXAMHALLCD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("          AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("          AND T3.TESTDIV = T2.TESTDIV ");
        stb.append("          AND T3.EXAM_TYPE = T2.EXAM_TYPE ");
        stb.append("          AND T3.RECEPTNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "'     AND ");
        stb.append("     T1.APPLICANTDIV = '" + _param._applicantDiv + "'     AND ");
        stb.append("     T1.TESTDIV = '" + _param._testDiv + "'     AND ");
        stb.append("     T1.EXAM_TYPE = '1'     AND ");
        if ("2".equals(_param._hallDiv)) {
            stb.append("     T1.EXAMHALLCD || T2.DETAIL_NO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        } else {
            stb.append("     T1.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T3.RECEPTNO ");
        stb.append(" ) SELECT DISTINCT ");
        stb.append("     T1.EXAMHALLCD, ");
        stb.append("     T1.DETAIL_NO, ");
        stb.append("     T1.EXAMHALL_NAME, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T4.COURSECD, ");
        stb.append("     T4.MAJORCD, ");
        stb.append("     T4.EXAMCOURSECD, ");
        stb.append("     T9.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1, ");
        stb.append("     T0.EXAMNO, ");
        stb.append("     T0.RECOM_KIND, ");
        stb.append("     T5.FINSCHOOL_NAME, ");
        stb.append("     T5.FINSCHOOL_NAME_ABBV, ");
        stb.append("     T6.ABSENCE_DAYS, ");
        stb.append("     T6.ABSENCE_DAYS2, ");
        stb.append("     T6.ABSENCE_DAYS3, ");
        stb.append("     T6.AVERAGE_ALL, ");
        stb.append("     NML016.NAME1 AS GRDDIVNAME, ");
        stb.append("     NML006.NAME1 AS SHDIVNAME, ");
        stb.append("     T7.FINSCHOOL_NAME AS SH_FINSCHOOLNAME, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     T3.DORMITORY_FLG, ");
        stb.append("     T8.INTERVIEW_REMARK, ");
        stb.append("     T3.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTDESIRE_DAT T0 ");
        stb.append("     INNER JOIN HALL_EXAMNO T1 ON T1.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("          AND T1.TESTDIV = T0.TESTDIV ");
        stb.append("          AND T1.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND T3.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("          AND T3.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND T4.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("          AND T4.TESTDIV = T0.TESTDIV ");
        stb.append("          AND T4.DESIREDIV = T0.DESIREDIV ");
        stb.append("          AND T4.WISHNO = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T5 ON T5.FINSCHOOLCD = T3.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T6 ON T6.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND T6.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T7 ON T7.FINSCHOOLCD = T3.SH_SCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT T8 ON T8.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND T8.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("          AND T8.TESTDIV = T0.TESTDIV ");
        stb.append("          AND T8.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T9 ON T9.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND T9.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("          AND T9.TESTDIV = T0.TESTDIV ");
        stb.append("          AND T9.COURSECD = T4.COURSECD ");
        stb.append("          AND T9.MAJORCD = T4.MAJORCD ");
        stb.append("          AND T9.EXAMCOURSECD = T4.EXAMCOURSECD ");
        stb.append("     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' AND NML006.NAMECD2 = T0.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NML016 ON NML016.NAMECD1 = 'L016' AND NML016.NAMECD2 = T3.FS_GRDDIV ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T3.SEX ");
        if ("1".equals(_param._shiftDesireFlg)) {
            stb.append(" WHERE ");
            stb.append("     T3.SHIFT_DESIRE_FLG = '1' "); //特別判定希望
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.RECEPTNO ");
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
        final String _hallDiv;
        final String[] _categorySelected;
        final String _form;
        final String _dateString;
        final String _shiftDesireFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getNameMst(db2, "NAME1"); // TestDivName
            _hallDiv = request.getParameter("HALL_DIV");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _form = request.getParameter("FORM");
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            final String executeDate = getNameMst(db2, "NAMESPARE1");
            _dateString = (null == executeDate || "".equals(executeDate)) ? "" : sdf.format(Date.valueOf(executeDate.replace('/', '-')));
            _shiftDesireFlg = request.getParameter("SHIFT_DESIRE_FLG");
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
        
        private String getNameMst(DB2UDB db2, final String field) {
            String val = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  val = rs.getString(field); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return val;
        }
    }
}

// eof
