/*
 * $Id: b2e7013c5396651822e2a54920e87e7d3c692779 $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 *                  ＜ＫＮＪＬ３２８Ｙ＞  再受験確認表
 **/
public class KNJL328Y {

    private static final Log log = LogFactory.getLog(KNJL328Y.class);

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

    private static int getMS932ByteCount(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return count;
    }

    private int getMaxPage(final List applicants, final int MAX_LINE) {
        final int p = applicants.size() / MAX_LINE;
        return (applicants.size() % MAX_LINE == 0 ? p : p + 1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        svf.VrSetForm(("2".equals(_param._output)) ? "KNJL328Y_2.frm" : "KNJL328Y.frm", 1);

        final int MAX_LINE = ("2".equals(_param._output)) ? 25 : 10;

        final List finschoolList = getFinschools(db2);

        try {
            for (final Iterator it = finschoolList.iterator(); it.hasNext();) {
                final Finschool finschool = (Finschool) it.next();

                final int maxPage = getMaxPage(finschool._applicants, MAX_LINE);
                int page = 1;
                int line = 1;
                for (final Iterator itApp = finschool._applicants.iterator(); itApp.hasNext(); ) {
                    final Applicant applicant = (Applicant) itApp.next();

                    final String finschoolDistname = null == finschool._finschoolDistcdName ? "" : finschool._finschoolDistcdName + "立";
                    final String finschoolName = null == finschool._finschoolName ? "" : finschool._finschoolName;
                    if (finschoolName.contains("中等教育学校")) {
                        svf.VrsOut("FINSCHOOL", finschoolDistname + finschoolName + "長　様");
                    } else {
                        svf.VrsOut("FINSCHOOL", finschoolDistname + finschoolName + " 中学校長　様");
                    }
                    svf.VrsOut("NENDO", _param._entexamYear + "年度");
                    svf.VrsOut("DATE", _param._dateString);
                    svf.VrsOut("LIMITDAY", _param._limitDate);
                    svf.VrsOut("TITLE", ("2".equals(_param._output)) ? "入学試験再受験確認表" : "入学試験再受験確認表");
                    svf.VrsOut("SUBTITLE", ("2".equals(_param._output)) ? "一般入試特進チャレンジ確認表" : "一般入試再受験意思の確認");
                    svf.VrsOut("PAGE1",    String.valueOf(page));
                    svf.VrsOut("PAGE2",    String.valueOf(maxPage));

                    svf.VrsOut("SCHOOLNAME", _param._schoolName);
                    svf.VrsOutn("EXAMNO", line, applicant._examno);
                    final int nameLen = getMS932ByteCount(applicant._name);
                    svf.VrsOutn("NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1"), line, applicant._name);
                    svf.VrsOutn("RE_EXAM_HOPE", line, "希望する・希望しない");
                    svf.VrsOutn("EXAMCOURSE_NAME",  line, "普通科・英語科");
                    svf.VrsOutn("SH",  line, "専願・併願");

                    _hasData = true;
                    line += 1;
                    if (MAX_LINE < line) {
                        svf.VrEndPage();
                        page += 1;
                        line = 1;
                    }
                }
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.debug("Exception:", ex);
        }
    }

    private List getFinschools(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List finschoolList = new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String finschoolCd = null == rs.getString("FINSCHOOLCD") ? "" : rs.getString("FINSCHOOLCD");
                Finschool finschool = null;
                for (final Iterator it = finschoolList.iterator(); it.hasNext();) {
                    final Finschool fs = (Finschool) it.next();
                    if (fs._finschoolcd.equals(finschoolCd)) {
                        finschool = fs;
                    }
                }
                if (null == finschool) {
                    final String finschoolDistcdName = rs.getString("FINSCHOOL_DISTNAME");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    finschool = new Finschool(finschoolCd, finschoolDistcdName, finschoolName);
                    finschoolList.add(finschool);
                }

                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");

                final Applicant applicant = new Applicant(examno, name);
                finschool._applicants.add(applicant);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return finschoolList;
    }

    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     T2.FINSCHOOLCD, ");
        stb.append("     T2.FINSCHOOL_DISTCD, ");
        stb.append("     NML001.NAME1 AS FINSCHOOL_DISTNAME, ");
        stb.append("     T2.FINSCHOOL_NAME, ");
        stb.append("     T0.EXAMNO, ");
        stb.append("     T1.NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T0 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ON T1.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("         AND T1.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("         AND T1.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD  ");
        stb.append("     INNER JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T0.JUDGEDIV ");
        stb.append("     LEFT  JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = T2.FINSCHOOL_DISTCD ");
        stb.append("     LEFT  JOIN NAME_MST NML017 ON NML017.NAMECD1 = 'L017' AND NML017.NAMECD2 = T0.HONORDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND C1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND C1.TESTDIV = T1.TESTDIV ");
        stb.append("         AND C1.COURSECD = T1.SUC_COURSECD ");
        stb.append("         AND C1.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("         AND C1.EXAMCOURSECD = T1.SUC_COURSECODE ");
        stb.append(" WHERE ");
        // 特進チャレンジ確認表
        if ("2".equals(_param._output)) {
            stb.append("     T1.GENERAL_FLG = '1' ");
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
            stb.append("     AND C1.EXAMCOURSE_MARK = 'S' ");
//            stb.append("     AND (T0.JUDGEDIV = '1' AND T1.DESIREDIV IN ('2','4') OR T0.JUDGEDIV IN ('3','5')) "); // 高校：学特入試のスライド合格(3)・特別判定合格(5)を含む進学コース(2,4)合格者(1)
        } else {
            stb.append("     T1.GENERAL_FLG = '1' ");
            stb.append("     AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
            stb.append("     AND (VALUE(T0.JUDGEDIV, '') <> '4' OR VALUE(NML017.NAMESPARE1, '') = '1') "); // 欠席理由がインフルエンザ以外の欠席者は除く
        }
        stb.append("     AND T0.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T0.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T0.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T2.FINSCHOOLCD, ");
        stb.append("     T0.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71659 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Finschool {
        final String _finschoolcd;
        final String _finschoolName;
        final String _finschoolDistcdName;
        final List _applicants = new ArrayList();
        public Finschool(String finschoolcd, String finschoolDistcdName, String finschoolName) {
            _finschoolcd = finschoolcd;
            _finschoolName = finschoolName;
            _finschoolDistcdName = finschoolDistcdName;
        }
    }

    private static class Applicant {
        final String _name;
        final String _examno;
        public Applicant(
                final String examno,
                final String name) {
            _name = name;
            _examno = examno;
        }
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _output; //1:再受験確認表 2:特進チャレンジ確認表
        final String _limitDate;          // 締め切り日付
        final String _dateString;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _output = request.getParameter("OUTPUT");
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            _limitDate = null == request.getParameter("DATE") ? "" : sdf.format(Date.valueOf(request.getParameter("DATE").replace('/', '-')));
            _dateString = sdf.format(Date.valueOf(request.getParameter("LOGIN_DATE")));
            _schoolName = getSchoolName(db2);
        }

        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamYear + "' AND CERTIF_KINDCD = '106' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    schoolName = rs.getString("SCHOOL_NAME");
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
    }
}

// eof
