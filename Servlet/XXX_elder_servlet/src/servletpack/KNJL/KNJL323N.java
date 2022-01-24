/*
 * $Id: a07d722881920b67986a11ef32a0dae62c56f64f $
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
 *                  ＜ＫＮＪＬ３２３Ｎ＞  合格者名簿・奨学生名簿一覧
 **/
public class KNJL323N {

    private static final Log log = LogFactory.getLog(KNJL323N.class);

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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "2".equals(_param._outputdiv) ? "KNJL323N_2.frm" : "KNJL323N_1.frm";
        final String title = "2".equals(_param._outputdiv) ? "奨学生名簿一覧" : "3".equals(_param._outputdiv) ? "不合格者名簿一覧" : "合格者名簿一覧";
        final int maxLine = 30;

        final List allApplicantList = Applicant.getApplicantList(db2, _param);
        
        final List pageList = getPageList(allApplicantList, maxLine, _param);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + title + "（" + StringUtils.defaultString(_param._testdivAbbv1) + "）"); // タイトル
            
            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                
                if ("2".equals(_param._outputdiv) && null != appl._remark8Name) {
                    svf.VrsOut("SUBTITLE", "（" + appl._remark8Name + "）"); // サブタイトル
                }
                
                final int line = j + 1;
                svf.VrsOutn("NO", line, appl._printNo); // 番号
                svf.VrsOutn("SHDIV", line, appl._shdivName); // 専併区分
                svf.VrsOutn("EXAM_NO1", line, appl._examno); // 受験番号
                svf.VrsOutn("NAME", line, appl._name); // 氏名
                svf.VrsOutn("HOPE_COURSE", line, appl._examcourseAbbv); // 志願コース
                svf.VrsOutn("PASS_COURSE", line, appl._sucExamcourseAbbv); // 合格コース
                svf.VrsOutn("SCHOOL_NAME", line, appl._finschoolName); // 中学校名
                svf.VrsOutn("REMARK1", line, appl._remark7Name); // 備考
                svf.VrsOutn("REMARK5", line, appl._remark8Name); // 備考
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static List getPageList(final List list, final int count, final Param param) {
        final List rtn = new ArrayList();
        List current = null;
        String oldRemark8 = null;
        int no = 1;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant o = (Applicant) it.next();
            if (null == current || current.size() >= count || ("2".equals(param._outputdiv) && (null == oldRemark8 || !oldRemark8.equals(o._remark8)))) {
                if ("2".equals(param._outputdiv) && (null == oldRemark8 || !oldRemark8.equals(o._remark8))) {
                    no = 1;
                }
                current = new ArrayList();
                rtn.add(current);
                oldRemark8 = o._remark8;
            }
            current.add(o);
            o._printNo = String.valueOf(no);
            no += 1;
        }
        return rtn;
    }
    
    private static class Applicant {
        String _printNo;
        final String _examno;
        final String _shdiv;
        final String _shdivName;
        final String _name;
        final String _examcourseAbbv;
        final String _sucExamcourseAbbv;
        final String _finschoolName;
        final String _remark7;
        final String _remark7Name;
        final String _remark8;
        final String _remark8Name;

        Applicant(
            final String examno,
            final String shdiv,
            final String shdivName,
            final String name,
            final String examcourseAbbv,
            final String sucExamcourseAbbv,
            final String finschoolName,
            final String remark7,
            final String remark7Name,
            final String remark8,
            final String remark8Name
        ) {
            _examno = examno;
            _shdiv = shdiv;
            _shdivName = shdivName;
            _name = name;
            _examcourseAbbv = examcourseAbbv;
            _sucExamcourseAbbv = sucExamcourseAbbv;
            _finschoolName = finschoolName;
            _remark7 = remark7;
            _remark7Name = remark7Name;
            _remark8 = remark8;
            _remark8Name = remark8Name;
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
                    final String shdiv = rs.getString("SHDIV");
                    final String shdivName = rs.getString("SHDIV_NAME");
                    final String name = rs.getString("NAME");
                    final String examcourseAbbv = rs.getString("EXAMCOURSE_ABBV");
                    final String sucExamcourseAbbv = rs.getString("SUC_EXAMCOURSE_ABBV");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME_ABBV");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark7Name = rs.getString("REMARK7_NAME");
                    final String remark8 = rs.getString("REMARK8");
                    final String remark8Name = rs.getString("REMARK8_NAME");
                    final Applicant applicant = new Applicant(examno, shdiv, shdivName, name, examcourseAbbv, sucExamcourseAbbv, finschoolName, remark7, remark7Name, remark8, remark8Name);
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
            stb.append("     BASE.SHDIV, ");
            stb.append("     NML006.NAME1 AS SHDIV_NAME, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     CRS1.EXAMCOURSE_ABBV, ");
            stb.append("     CRS2.EXAMCOURSE_ABBV AS SUC_EXAMCOURSE_ABBV, ");
            stb.append("     FIN.FINSCHOOL_NAME_ABBV, ");
            stb.append("     BDETAIL9.REMARK7, ");
            stb.append("     NML025_1.NAME1 AS REMARK7_NAME, ");
            stb.append("     BDETAIL9.REMARK8, ");
            stb.append("     NML025_2.NAME1 AS REMARK8_NAME ");
            stb.append("  FROM ENTEXAM_APPLICANTBASE_DAT BASE      ");
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
            stb.append("  LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("      AND CRS2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("      AND CRS2.TESTDIV = BASE.TESTDIV ");
            stb.append("      AND CRS2.COURSECD = BASE.SUC_COURSECD ");
            stb.append("      AND CRS2.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("      AND CRS2.EXAMCOURSECD = BASE.SUC_COURSECODE  ");
            stb.append("  LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' ");
            stb.append("     AND NML006.NAMECD2 = BASE.SHDIV ");
            stb.append("  LEFT JOIN NAME_MST NML025_1 ON NML025_1.NAMECD1 = 'L025' ");
            stb.append("     AND NML025_1.NAMECD2 = BDETAIL9.REMARK7 ");
            stb.append("  LEFT JOIN NAME_MST NML025_2 ON NML025_2.NAMECD1 = 'L025' ");
            stb.append("     AND NML025_2.NAMECD2 = BDETAIL9.REMARK8 ");
            stb.append("  LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" WHERE  ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "'  ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if ("3".equals(param._outputdiv)) {
                stb.append("     AND BASE.JUDGEMENT = '2' ");
            } else {
                stb.append("     AND (BASE.JUDGEMENT = '1' OR BASE.JUDGEMENT = '3') ");
                if ("2".equals(param._outputdiv)) {
                    stb.append("     AND BDETAIL9.REMARK8 IS NOT NULL ");
                }
            }
            stb.append("  ORDER BY ");
            if ("2".equals(param._outputdiv)) {
                stb.append("     BDETAIL9.REMARK8, ");
            }
            stb.append("     BASE.EXAMNO ");
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
        final String _outputdiv; // 1:合格名簿 2:奨学生名簿一覧 3:不合格名簿
        final String _date;
        
        final String _applicantdivAbbv1;
        final String _testdivAbbv1;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _outputdiv = request.getParameter("OUTPUTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);
            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
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

