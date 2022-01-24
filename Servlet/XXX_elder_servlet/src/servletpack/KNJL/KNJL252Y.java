/*
 * $Id: 701409d571e8052b81819ba9596b1aba9f94f603 $
 *
 * 作成日: 2015/10/16
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
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJL252Y {

    private static final Log log = LogFactory.getLog(KNJL252Y.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        setHead(svf);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int gyo = 1;
            int printNo = 1;
            while (rs.next()) {
                if (gyo > 20) {
                    svf.VrEndPage();
                    setHead(svf);
                    gyo = 1;
                }
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sexName = rs.getString("SEX_NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String interviewValue2 = rs.getString("INTERVIEW_VALUE2");
                final String interviewRemark2 = rs.getString("INTERVIEW_REMARK2");

                final int nameKeta     = keta(name);
                final int nameKanaKeta = keta(nameKana);
                final String nameField     = "NAME" + (24 < nameKeta ? "1_3" : (20 < nameKeta ? "1_2" : "1"));
                final String nameKanaField = "KANA" + (24 < nameKanaKeta ? "1_2" : "1_1");

                svf.VrsOutn("NO", gyo, String.valueOf(printNo));
                svf.VrsOutn("EXAM_NO", gyo, examNo);
                svf.VrsOutn(nameField, gyo, name);
                svf.VrsOutn(nameKanaField, gyo, nameKana);
                svf.VrsOutn("SEX", gyo, sexName);
                svf.VrsOutn("BIRTHDAY", gyo, birthday.replace('-', '.'));
                svf.VrsOutn("VAL", gyo, interviewValue2);
                svf.VrsOutn("INTERVIEW", gyo, interviewRemark2);
                gyo++;
                printNo++;
                _hasData = true;
            }
            if (gyo > 1) {
                svf.VrEndPage();
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
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

    private void setHead(final Vrw32alp svf) {
        svf.VrSetForm("KNJL252Y.frm", 1);
        svf.VrsOut("TITLE", _param._year + "年度　" + _param._title + "入学試験　集団あそび評価記入用紙");
        svf.VrsOut("SUBTITLE", "(" + _param._subTitle + ")");
        svf.VrsOut("DATE", _param._dateString);
        svf.VrsOut("GROUP_NAME", _param._examhallName);

    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_INTERVIEWE AS (  ");
        stb.append("     SELECT S1.EXAMNO  ");
        stb.append("       ,S1.INTERVIEW_VALUE2  ");
        stb.append("       ,S1.INTERVIEW_REMARK2  ");
        stb.append("       FROM ENTEXAM_INTERVIEW_DAT S1  ");
        stb.append("      WHERE S1.ENTEXAMYEAR    = '" + _param._year + "'  ");
        stb.append("        AND S1.APPLICANTDIV   = '" + _param._applicantDiv + "'  ");
        stb.append("        AND S1.TESTDIV        = '" + _param._testDiv + "'  ");
        stb.append(" )  ");
        stb.append(" SELECT T1.RECEPTNO  ");
        stb.append("       ,T1.EXAMNO  ");
        stb.append("       ,T2.NAME ");
        stb.append("       ,T2.NAME_KANA ");
        stb.append("       ,N1.NAME2 AS SEX_NAME ");
        stb.append("       ,T2.BIRTHDAY ");
        stb.append("       ,N2.NAME1 AS INTERVIEW_VALUE2  ");
        stb.append("       ,S1.INTERVIEW_REMARK2  ");
        stb.append("   FROM ENTEXAM_RECEPT_DAT T1  ");
        stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2  ");
        stb.append("                ON T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR  ");
        stb.append("               AND T2.EXAMNO         = T1.EXAMNO  ");
        stb.append("               AND T2.APPLICANTDIV   = T1.APPLICANTDIV  ");
        stb.append("        LEFT JOIN NAME_MST N1 ");
        stb.append("               ON N1.NAMECD1 = 'Z002' ");
        stb.append("              AND N1.NAMECD2 = T2.SEX ");
        stb.append("        INNER JOIN ENTEXAM_HALL_LIST_YDAT H1  ");
        stb.append("                ON H1.ENTEXAMYEAR    = T1.ENTEXAMYEAR  ");
        stb.append("               AND H1.APPLICANTDIV   = T1.APPLICANTDIV  ");
        stb.append("               AND H1.TESTDIV        = T1.TESTDIV  ");
        stb.append("               AND H1.EXAM_TYPE      = T1.EXAM_TYPE  ");
        stb.append("               AND H1.EXAMHALLCD     = '" + _param._examhallCd + "'  ");
        stb.append("               AND H1.RECEPTNO       = T1.RECEPTNO  ");
        stb.append("        LEFT JOIN T_INTERVIEWE S1 ON S1.EXAMNO = T1.RECEPTNO  ");
        stb.append("        LEFT JOIN NAME_MST N2 ");
        stb.append("               ON N2.NAMECD1 = 'L030' ");
        stb.append("              AND N2.NAMECD2 = S1.INTERVIEW_VALUE2 ");
        stb.append("  WHERE T1.ENTEXAMYEAR    = '" + _param._year + "'  ");
        stb.append("    AND T1.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        stb.append("    AND T1.TESTDIV        = '" + _param._testDiv + "'  ");
        stb.append("    AND T1.EXAM_TYPE      = '1'  ");
        stb.append("  ORDER BY T1.RECEPTNO ");
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
        private final String _examhallCd;
        private final String _examhallName;
        private final String _title;
        private final String _dateString;
        private final String _subTitle;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _examhallCd = request.getParameter("EXAMHALLCD");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            _examhallName = getHallName(db2);
            final Calendar cal = Calendar.getInstance();
            final String sysDate = String.valueOf(cal.get(Calendar.YEAR)) + "-" + String.valueOf(cal.get(Calendar.MONTH)) + "-" + String.valueOf(cal.get(Calendar.DATE));
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(sysDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
        }

        private String getSchoolName(final DB2UDB db2) {
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

        private String getTestDivName(final DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "L004";
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

        private String getHallName(final DB2UDB db2) {
            String hallName = "";
            final String hallNameSql = getHallNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hallNameSql);
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("HALL_NAME")) {
                  hallName = rs.getString("HALL_NAME");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return hallName;
        }

        private String getHallNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EXAMHALLCD || ':' || T1.EXAMHALL_NAME AS HALL_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_HALL_YDAT T1 ");
            stb.append(" WHERE ");
            stb.append("         T1.ENTEXAMYEAR  = '" + _year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV      = '" + _testDiv + "' ");
            stb.append("     AND T1.EXAM_TYPE    = '1' ");
            stb.append("     AND T1.EXAMHALLCD    = '" + _examhallCd + "' ");
            return stb.toString();
        }

    }
}

// eof

