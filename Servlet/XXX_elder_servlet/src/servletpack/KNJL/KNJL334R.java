/*
 * $Id: c737d85870600ee9dc9aa53e249b2d999c2861aa $
 *
 * 作成日: 2020/01/31
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３３４Ｒ＞  コース別平均点一覧
 **/
public class KNJL334R {

    private static final Log log = LogFactory.getLog(KNJL334R.class);

    private boolean _hasData;

    private static String coursecd1 = "0001";
    private static String coursecd2 = "0002";
    private static String coursecd3 = "0003";
    private static String coursecd4 = "0004";
    private static String coursecdIsNull = "IS NULL";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL334R.frm";
        svf.VrSetForm(form, 1);
        svf.VrsOut("DATE", _param._dateStr); // 作成日時
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivName + "　コース別平均点一覧"); // タイトル

        // 教科
        int gyo = 1;
        for (final Iterator it = _param._nameMstL009List.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String testsubclasscd = (String) map.get("NAMECD2");
            final String testsubclassname = (String) map.get("NAME1");
            svf.VrsOutn("CLASS_NAME", gyo, testsubclassname); // 教科名
            printnCount(svf, "AVERAGE", gyo, CourseAvg.load(db2, _param, "SCORE", testsubclasscd));
            gyo++;
        }
        // 内申点
        printCount(svf, "DIV", CourseAvg.load(db2, _param, "NAISIN", null));
        // 合計
        printCount(svf, "TOTAL", CourseAvg.load(db2, _param, "TOTAL", null));

        svf.VrEndPage();
        _hasData = true;
    }

    private void printnCount(final Vrw32alp svf, final String pfx, final int gyo, final CourseAvg c) {
        if (null == c) {
            return;
        }
        svf.VrsOutn(pfx + "1", gyo, c._courseavg1); // GS
        svf.VrsOutn(pfx + "2", gyo, c._courseavg2); // GA
        svf.VrsOutn(pfx + "3", gyo, c._courseavg3); // GB
        svf.VrsOutn(pfx + "4", gyo, c._courseavg4); // GC
        svf.VrsOutn(pfx + "5", gyo, c._courseavgAll); // 全体
    }

    private void printCount(final Vrw32alp svf, final String pfx, final CourseAvg c) {
        if (null == c) {
            return;
        }
        svf.VrsOut(pfx + "1", c._courseavg1); // GS
        svf.VrsOut(pfx + "2", c._courseavg2); // GA
        svf.VrsOut(pfx + "3", c._courseavg3); // GB
        svf.VrsOut(pfx + "4", c._courseavg4); // GC
        svf.VrsOut(pfx + "5", c._courseavgAll); // 全体
    }

    private static class CourseAvg {
        final String _courseavg1;
        final String _courseavg2;
        final String _courseavg3;
        final String _courseavg4;
        final String _courseavgAll;

        CourseAvg(
            final String courseavg1,
            final String courseavg2,
            final String courseavg3,
            final String courseavg4,
            final String courseavgAll
        ) {
            _courseavg1 = courseavg1;
            _courseavg2 = courseavg2;
            _courseavg3 = courseavg3;
            _courseavg4 = courseavg4;
            _courseavgAll = courseavgAll;
        }

        private static String zeroToBlank(final String s) {
            return (NumberUtils.isDigits(s) && Integer.parseInt(s) == 0) ? null : s;
        }

        public static CourseAvg load(final DB2UDB db2, final Param param, final String avgFlg, final String testsubclasscd) {
            CourseAvg cAvg = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, avgFlg, testsubclasscd);
                log.debug(" " + avgFlg + ", " + testsubclasscd + " sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String courseavg1 = zeroToBlank(rs.getString("COURSEAVG_1"));
                    final String courseavg2 = zeroToBlank(rs.getString("COURSEAVG_2"));
                    final String courseavg3 = zeroToBlank(rs.getString("COURSEAVG_3"));
                    final String courseavg4 = zeroToBlank(rs.getString("COURSEAVG_4"));
                    final String courseavgAll = zeroToBlank(rs.getString("COURSEAVG_ALL"));
                    cAvg = new CourseAvg(courseavg1, courseavg2, courseavg3, courseavg4, courseavgAll);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return cAvg;
        }

        public static String sql(final Param param, final String avgFlg, final String testsubclasscd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH APPLICANT AS ( ");
            stb.append("     SELECT ");
            stb.append("         BASE.EXAMNO, ");
            stb.append("         WISH.EXAMCOURSECD AS EXAMCOURSECD1, ");
            if ("NAISIN".equals(avgFlg)) {
                stb.append("         CONF.KASANTEN_ALL AS SCORE ");
            } else if ("TOTAL".equals(avgFlg)) {
                stb.append("         RCPT.TOTAL4 AS SCORE ");
            } else if ("SCORE".equals(avgFlg)) {
                stb.append("         SCRE.SCORE AS SCORE ");
            }
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST WISH ON WISH.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND WISH.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND WISH.TESTDIV      = BASE.TESTDIV ");
            stb.append("             AND WISH.DESIREDIV    = BASE.DESIREDIV ");
            stb.append("             AND WISH.WISHNO       = '1' ");
            stb.append("         LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            if ("NAISIN".equals(avgFlg)) {
                stb.append("         LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
                stb.append("             AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
                stb.append("             AND CONF.EXAMNO       = BASE.EXAMNO ");
            } else if ("TOTAL".equals(avgFlg)) {
                stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
                stb.append("             AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ");
                stb.append("             AND RCPT.TESTDIV      = BASE.TESTDIV ");
                stb.append("             AND RCPT.EXAMNO       = BASE.EXAMNO ");
            } else if ("SCORE".equals(avgFlg)) {
                stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
                stb.append("             AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ");
                stb.append("             AND RCPT.TESTDIV      = BASE.TESTDIV ");
                stb.append("             AND RCPT.EXAMNO       = BASE.EXAMNO ");
                stb.append("         LEFT JOIN ENTEXAM_SCORE_DAT SCRE ON SCRE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
                stb.append("             AND SCRE.APPLICANTDIV = RCPT.APPLICANTDIV ");
                stb.append("             AND SCRE.TESTDIV      = RCPT.TESTDIV ");
                stb.append("             AND SCRE.EXAM_TYPE    = RCPT.EXAM_TYPE ");
                stb.append("             AND SCRE.RECEPTNO     = RCPT.RECEPTNO ");
                stb.append("             AND SCRE.TESTSUBCLASSCD = '" + testsubclasscd + "' ");
            }
            stb.append("     WHERE ");
            stb.append("             BASE.ENTEXAMYEAR  = '" + param._entexamyear + "' ");
            stb.append("         AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND BASE.TESTDIV      = '" + param._testdiv + "' ");
            if ("1".equals(param._passFlg)) {
                stb.append("         AND NML013.NAMESPARE1 = '1' ");
            }
            stb.append(" )  ");
            stb.append(" SELECT ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T1.EXAMCOURSECD1 = '" + coursecd1 + "' THEN T1.SCORE END))*10,0)/10,5,1) AS COURSEAVG_1, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T1.EXAMCOURSECD1 = '" + coursecd2 + "' THEN T1.SCORE END))*10,0)/10,5,1) AS COURSEAVG_2, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T1.EXAMCOURSECD1 = '" + coursecd3 + "' THEN T1.SCORE END))*10,0)/10,5,1) AS COURSEAVG_3, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T1.EXAMCOURSECD1 = '" + coursecd4 + "' THEN T1.SCORE END))*10,0)/10,5,1) AS COURSEAVG_4, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN T1.EXAMCOURSECD1 IN  ('" + coursecd1 + "', '" + coursecd2 + "', '" + coursecd3 + "', '" + coursecd4 + "') THEN T1.SCORE END))*10,0)/10,5,1) AS COURSEAVG_ALL ");
            stb.append(" FROM ");
            stb.append("     APPLICANT T1 ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72127 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _passFlg; // 1:合格者のみ
        final String _date;

        final String _dateStr;
        final String _testdivName;
        final List _nameMstL009List;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _passFlg = request.getParameter("PASS_FLG");
            _date = request.getParameter("CTRL_DATE");

            _dateStr = getDateStr(db2, _date);
            _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            _nameMstL009List = getNameMstL009List(db2);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(db2, date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
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

        private List getNameMstL009List(final DB2UDB db2) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT NAMECD2, NAME1 FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L009' ");
                if ("1".equals(_testdiv)) {
                    sql.append(" AND NAMESPARE2 = '1' ");
                } else {
                    sql.append(" AND NAMESPARE3 = '1' ");
                }
                sql.append(" ORDER BY NAMECD2 ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("NAMECD2", rs.getString("NAMECD2"));
                    m.put("NAME1", rs.getString("NAME1"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

