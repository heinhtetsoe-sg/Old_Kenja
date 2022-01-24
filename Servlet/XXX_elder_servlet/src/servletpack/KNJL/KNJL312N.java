/*
 * $Id: 64f97f5703b564bfffa7cfef7c94409ffdee38a8 $
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
 *                  ＜ＫＮＪＬ３１２Ｎ＞  素点入力確認表（科目別）
 **/
public class KNJL312N {

    private static final Log log = LogFactory.getLog(KNJL312N.class);

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
        
        final String form = "KNJL312N.frm";
        final int maxLine = 25;
        final int maxCol = 4;
        final int max= maxLine * maxCol;

        final List allScoreList = ApplicantScore.load(db2, _param);
        
        final List pageList = getPageList(allScoreList, max);
        int boyCount = 0;
        int girlCount = 0;
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List scoreList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", "素点入力確認表"); // タイトル
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
            svf.VrsOut("KIND", _param._applicantdivAbbv1); // 入試制度
            svf.VrsOut("DIV", _param._testdivAbbv1); // 入試制度
            svf.VrsOut("COURSE", _param._examCourseName); // 志望コース
            svf.VrsOut("SUBJECT", _param._subclassName); // 科目
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            
            for (int j = 0; j < scoreList.size(); j++) {
                final ApplicantScore applScore = (ApplicantScore) scoreList.get(j);
                final int line = j + 1;
                final int col = (j < maxLine * 1) ? 1 : (j < maxLine * 2) ? 2 : (j < maxLine * 3) ? 3 : 4;
                final String k = String.valueOf(col);
                final int pline = line - (col - 1) * maxLine;
                svf.VrsOutn("NO" + k, pline, String.valueOf(line)); // NO
                svf.VrsOutn("EXAM_NO" + k, pline, applScore._examno); // 受験番号
                svf.VrsOutn("SCORE" + k, pline, "0".equals(applScore._attendFlg) ? "*" : applScore._score); // 素点
                if ("1".equals(applScore._sex)) {
                    boyCount++;
                } else if ("2".equals(applScore._sex)) {
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
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class ApplicantScore {
        
        final String _examno;
        final String _sex;
        final String _score;
        final String _attendFlg;

        ApplicantScore(
            final String examno,
            final String sex,
            final String score,
            final String attendFlg
        ) {
            _examno = examno;
            _sex = sex;
            _score = score;
            _attendFlg = attendFlg;
        }
        
        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new ApplicantScore(rs.getString("EXAMNO"), rs.getString("SEX"), rs.getString("SCORE"), rs.getString("ATTEND_FLG")));
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
            stb.append("     RCPT.EXAMNO, ");
            stb.append("     BASE.SEX, ");
            stb.append("     TSCORE.SCORE, ");
            stb.append("     TSCORE.ATTEND_FLG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
            stb.append("         AND BASE.TESTDIV = RCPT.TESTDIV ");
            stb.append("         AND RCPT.EXAM_TYPE = '1' ");
            stb.append("         AND BASE.EXAMNO = RCPT.EXAMNO ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("         AND BDETAIL1.EXAMNO = RCPT.EXAMNO ");
            stb.append("         AND BDETAIL1.SEQ = '001' ");
            stb.append("     INNER JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND CRS1.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND CRS1.COURSECD = BDETAIL1.REMARK8 ");
            stb.append("         AND CRS1.MAJORCD = BDETAIL1.REMARK9 ");
            stb.append("         AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10 ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("         AND TSCORE.APPLICANTDIV = RCPT.APPLICANTDIV ");
            stb.append("         AND TSCORE.TESTDIV = RCPT.TESTDIV ");
            stb.append("         AND TSCORE.EXAM_TYPE = RCPT.EXAM_TYPE ");
            stb.append("         AND TSCORE.RECEPTNO = RCPT.RECEPTNO ");
            stb.append("         AND TSCORE.TESTSUBCLASSCD = '" + param._testsubclasscd + "' ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND CRS1.COURSECD || '-' || CRS1.MAJORCD || '-' || CRS1.EXAMCOURSECD = '" + param._examcourse + "' ");
            stb.append(" ORDER BY ");
            stb.append("     RCPT.EXAMNO ");
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
        final String _testsubclasscd;
        final String _date;
        
        final String _subclassName;
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
            _dateStr = getDateStr(_date);
            _testsubclasscd = request.getParameter("TESTSUBCLASSCD");
            _sexName1 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "1"));
            _sexName2 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "2"));
            _subclassName = getNameMst(db2, "NAME1", "L009", _testsubclasscd);
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

