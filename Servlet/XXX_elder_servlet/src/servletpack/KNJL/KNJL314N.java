/*
 * $Id: 72167378290c22e4d152e8c634bd34928cccfdfe $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１４Ｎ＞  面接試験結果表
 **/
public class KNJL314N {

    private static final Log log = LogFactory.getLog(KNJL314N.class);

    private static final String FROM_TO_MARK = "\uFF5E";

    private boolean _hasData;

    private Param _param;

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
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.debug("exception!", e);
            }
        }
        return rtn;
    }
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "KNJL314N.frm";
        final int maxLine = 25;

        final List allScoreList = Applicant.getApplicantList(db2, _param);
        
        final List pageList = getPageList(allScoreList, maxLine);
        int boyCount = 0;
        int girlCount = 0;
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            
            String first = StringUtils.defaultString(((Applicant) applicantList.get(0))._examno);
            String last = StringUtils.defaultString(((Applicant) applicantList.get(applicantList.size() - 1))._examno);

            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", "入学試験面接用紙　No." + first + FROM_TO_MARK + last); // タイトル
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
            svf.VrsOut("KIND", _param._applicantdivAbbv1); // 入試制度
            svf.VrsOut("DIV", _param._testdivAbbv1); // 入試制度
            svf.VrsOut("COURSE", _param._examCourseName); // 志望コース
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            
            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                
                svf.VrsOutn("NO", line, appl._printNo); // コース名
                svf.VrsOutn("EXAM_NO1", line, appl._examno); // 受験番号
                svf.VrsOutn("NAME", line, appl._name); // 氏名
                svf.VrsOutn("SCHOOL_NAME", line, appl._finschoolName); // 中学校名
                svf.VrsOutn("REMARK2", line, appl._remark2); // 備考
                svf.VrsOutn("INTERVIEW_VALUE1", line, appl._interviewValue); // 面接
                final int interviewRemarkKeta = getMS932ByteLength(appl._interviewRemark);
                svf.VrsOutn("INTERVIEW_REMARK1" + (interviewRemarkKeta <= 30 ? "" : interviewRemarkKeta <= 40 ? "_2" : "_3"), line, appl._interviewRemark); // 面接
                svf.VrsOutn("INTERVIEW_VALUE2", line, appl._interviewValue2); // 面接
                final int interviewRemark2Keta = getMS932ByteLength(appl._interviewRemark2);
                svf.VrsOutn("INTERVIEW_REMARK2" + (interviewRemark2Keta <= 30 ? "" : interviewRemark2Keta <= 40 ? "_2" : "_3"), line, appl._interviewRemark2); // 面接
                
                if ("1".equals(appl._sex)) {
                    boyCount++;
                } else if ("2".equals(appl._sex)) {
                    girlCount++;
                }
            }
            if (pi == pageList.size() - 1) {
                svf.VrsOut("TOTAL", _param._sexName1 + boyCount + "名、" + _param._sexName2 + girlCount + "名、合計" + String.valueOf(boyCount + girlCount) + "名"); // 合計
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        int n = 1;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant o = (Applicant) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
            o._printNo = String.valueOf(n);
            n += 1;
        }
        return rtn;
    }
    
    private static class Applicant {
        String _printNo;
        final String _examno;
        final String _name;
        final String _sex;
        final String _finschoolName;
        final String _remark2;
        final String _interviewValue;
        final String _interviewRemark;
        final String _interviewValue2;
        final String _interviewRemark2;

        Applicant(
            final String examno,
            final String name,
            final String sex,
            final String finschoolName,
            final String remark2,
            final String interviewValue,
            final String interviewRemark,
            final String interviewValue2,
            final String interviewRemark2
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
            _finschoolName = finschoolName;
            _remark2 = remark2;
            _interviewValue = interviewValue;
            _interviewRemark = interviewRemark;
            _interviewValue2 = interviewValue2;
            _interviewRemark2 = interviewRemark2;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String remark2 = rs.getString("REMARK2");
                    final String interviewValue = rs.getString("INTERVIEW_VALUE");
                    final String interviewRemark = rs.getString("INTERVIEW_REMARK");
                    final String interviewValue2 = rs.getString("INTERVIEW_VALUE2");
                    final String interviewRemark2 = rs.getString("INTERVIEW_REMARK2");
                    final Applicant applicant = new Applicant(examno, name, sex, finschoolName, remark2, interviewValue, interviewRemark, interviewValue2, interviewRemark2);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     BDETAIL9.REMARK2, ");
            stb.append("     INTV.INTERVIEW_VALUE, ");
            stb.append("     INTV.INTERVIEW_REMARK, ");
            stb.append("     INTV.INTERVIEW_VALUE2, ");
            stb.append("     INTV.INTERVIEW_REMARK2 ");
            stb.append("  FROM ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("  INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
            stb.append("      AND BASE.TESTDIV = RCPT.TESTDIV ");
            stb.append("      AND RCPT.EXAM_TYPE = '1' ");
            stb.append("      AND BASE.EXAMNO = RCPT.EXAMNO ");
            stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("      AND BDETAIL1.EXAMNO = BASE.EXAMNO ");
            stb.append("      AND BDETAIL1.SEQ = '001'  ");
            stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("      AND BDETAIL9.EXAMNO = BASE.EXAMNO ");
            stb.append("      AND BDETAIL9.SEQ = '009'  ");
            stb.append("  LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("      AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("      AND CRS1.TESTDIV = BASE.TESTDIV ");
            stb.append("      AND CRS1.COURSECD = BDETAIL1.REMARK8 ");
            stb.append("      AND CRS1.MAJORCD = BDETAIL1.REMARK9 ");
            stb.append("      AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10  ");
            stb.append("  LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("  LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("     AND INTV.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("     AND INTV.TESTDIV = BASE.TESTDIV ");
            stb.append("     AND INTV.EXAMNO = BASE.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "'  ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND CRS1.COURSECD || '-' || CRS1.MAJORCD || '-' || CRS1.EXAMCOURSECD = '" + param._examcourse + "' ");
            if (null != param._sExamno) {
                stb.append("     AND BASE.EXAMNO >= '" + param._sExamno + "' ");
            }
            if (null != param._eExamno) {
                stb.append("     AND BASE.EXAMNO <= '" + param._eExamno + "' ");
            }
            stb.append("  ORDER BY ");
            stb.append("     BASE.EXAMNO ");
            stb.append("  ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examcourse;
        final String _date;
        final String _sExamno;
        final String _eExamno;
        
        final String _sexName1;
        final String _sexName2;
        final String _applicantdivAbbv1;
        final String _testdivAbbv1;
        final String _examCourseName;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _examcourse = request.getParameter("EXAMCOURSE");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _sExamno = StringUtils.isBlank(request.getParameter("S_EXAMNO")) ? null : request.getParameter("S_EXAMNO");
            _eExamno = StringUtils.isBlank(request.getParameter("E_EXAMNO")) ? null : request.getParameter("E_EXAMNO");
            _dateStr = getDateStr(_date);
            _sexName1 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "1"));
            _sexName2 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "2"));
            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _examCourseName = getCourseName(db2);
        }
        
        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String min = df.format(cal.get(Calendar.MINUTE));
            cal.setTime(Date.valueOf(date));
            final String youbi = StringUtils.defaultString(new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)]);
            return KNJ_EditDate.h_format_JP(date) + "（" + youbi + "） " + hour + ":" + min;
        }

        private String getCourseName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT EXAMCOURSE_NAME ";
                sql += " FROM ENTEXAM_COURSE_MST";
                sql += " WHERE ENTEXAMYEAR = '" + _entexamyear + "' ";
                sql += "   AND APPLICANTDIV = '" + _applicantdiv + "' ";
                sql += "   AND TESTDIV = '" + _testdiv + "' ";
                sql += "   AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '" + _examcourse + "' ";
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("EXAMCOURSE_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

