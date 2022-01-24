/*
 * $Id: e6eb9fff0cd5240d236c2be9d1d7ecdc91c9cd47 $
 *
 * 作成日: 2016/12/19
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL325G {

    private static final Log log = LogFactory.getLog(KNJL325G.class);

    private static final String csv = "csv";

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

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if (csv.equals(_param._cmd)) {
                outputCsv(db2, response);
            } else {
                svf = new Vrw32alp();
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (csv.equals(_param._cmd)) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }

    private void outputCsv(final DB2UDB db2, final HttpServletResponse response) {
        final String filename = getTitle() + getSubTitle() + ".csv";
        CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2));
    }

    private List getCsvOutputLine(final DB2UDB db2) {
        final List lines = new ArrayList();

        final String gradName = "2".equals(_param._zaisotu) ? "卒業" : "在校生";

        //ヘッダー部
        final List headerLines = new ArrayList();
        headerLines.addAll(Arrays.asList(new String[] {"連番", "判定", "受験番号", "氏名", "性別"}));
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            final String subclassName = (String) _param._subclassMap.get(subclassCd);
            headerLines.addAll(Arrays.asList(new String[] {subclassName}));
        }
        headerLines.addAll(Arrays.asList(new String[] {"合計", "コース順位", "総順位", "面接", gradName, "双生児", "クラブ推薦", "事前相談", "専併", "第1志望", "第2志望"}));

        lines.add(headerLines);

        //データ部
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int renBan = 1;
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String judge = rs.getString("JUDGE");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String total4 = rs.getString("TOTAL4");
                final String divRank4 = rs.getString("DIV_RANK4");
                final String totalRank4 = rs.getString("TOTAL_RANK4");
                final String sinzokuZokugara = rs.getString("SINZOKU_ZOKUGARA");
                final String sinzokuGakkoumei = rs.getString("SINZOKU_GAKKOUMEI");
                final String sinzokuName = rs.getString("SINZOKU_NAME");
                final String futagoName = rs.getString("SINZOKU_FUTAGO_NAME");
                final String futagoZokugara = rs.getString("SINZOKU_FUTAGO_ZOKUGARA");
                final String clubCd = rs.getString("CLUB_CD");
                final String clubRank = rs.getString("CLUB_RANK");
                final String jizenSoudanCd = rs.getString("JIZEN_SOUDAN_CD");
                final String jizenSoudanText = rs.getString("JIZEN_SOUDAN_TEXT");
                final String shName = rs.getString("SH_NAME");
                final String interview = rs.getString("INTERVIEW");

                final List line = new ArrayList();

                line.add(String.valueOf(renBan));
                line.add(judge);
                line.add(examno);
                line.add(name);
                line.add(sex);
                for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
                    final String subclassCd = (String) iterator.next();
                    if ("0".equals(rs.getString("ATTEND_FLG" + subclassCd))) {
                        line.add("*");
                    } else if (null != rs.getString("SCORE" + subclassCd)) {
                        line.add(rs.getString("SCORE" + subclassCd));
                    } else {
                        line.add("");
                    }
                }
                line.add(total4);
                line.add(divRank4);
                line.add(totalRank4);
                line.add(interview);

                String setSinzoku = sinzokuZokugara;
                setSinzoku += setSinzoku.length() > 0 ? "・" + sinzokuGakkoumei : sinzokuGakkoumei;
                setSinzoku += setSinzoku.length() > 0 ? "・" + sinzokuName : sinzokuName;
                line.add(setSinzoku);

                String setFutago = futagoName;
                setFutago += setFutago.length() > 0 ? "・" + futagoZokugara : futagoZokugara;
                line.add(setFutago);

                String setClub = clubCd + clubRank;
                line.add(setClub);

                String setJizen = jizenSoudanCd;
                setJizen += setJizen.length() > 0 ? "・" + jizenSoudanText : jizenSoudanText;
                line.add(setJizen);

                line.add(shName);
                line.add(course1);
                line.add(course2);

                lines.add(line);

                renBan++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return lines;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL325G.frm", 1);

        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        setTitle(svf, nendo);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 50;
            int lineCnt = 1;
            int renBan = 1;
            String sExam = "";
            String eExam = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String judge = rs.getString("JUDGE");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String total4 = rs.getString("TOTAL4");
                final String divRank4 = rs.getString("DIV_RANK4");
                final String totalRank4 = rs.getString("TOTAL_RANK4");
                final String sinzokuZokugara = rs.getString("SINZOKU_ZOKUGARA");
                final String sinzokuGakkoumei = rs.getString("SINZOKU_GAKKOUMEI");
                final String sinzokuName = rs.getString("SINZOKU_NAME");
                final String futagoName = rs.getString("SINZOKU_FUTAGO_NAME");
                final String futagoZokugara = rs.getString("SINZOKU_FUTAGO_ZOKUGARA");
                final String clubCd = rs.getString("CLUB_CD");
                final String clubRank = rs.getString("CLUB_RANK");
                final String jizenSoudanCd = rs.getString("JIZEN_SOUDAN_CD");
                final String jizenSoudanText = rs.getString("JIZEN_SOUDAN_TEXT");
                final String shName = rs.getString("SH_NAME");
                final String interview = rs.getString("INTERVIEW");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    setTitle(svf, nendo);
                    lineCnt = 1;
                    sExam = "";
                    eExam = "";
                }
                sExam = "".equals(sExam) ? examno : sExam;
                eExam = examno;
                svf.VrsOut("PRINT_AREA", sExam + "\uFF5E" + eExam);

                svf.VrsOutn("NO", lineCnt, String.valueOf(renBan));
                svf.VrsOutn("JUDGE1", lineCnt, judge);
                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                final String kanaField = getMS932Bytecount(nameKana) > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, lineCnt, nameKana);
                svf.VrsOutn("SEX", lineCnt, sex);
                for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
                    final String subclassCd = (String) iterator.next();
                    if (null != rs.getString("SCORE" + subclassCd)) {
                        svf.VrsOutn("SCORE" + subclassCd, lineCnt, rs.getString("SCORE" + subclassCd));
                    }
                    if ("0".equals(rs.getString("ATTEND_FLG" + subclassCd))) {
                        svf.VrsOutn("SCORE" + subclassCd, lineCnt, "*");
                    }
                }
                svf.VrsOutn("TOTAL_SCORE", lineCnt, total4);
                svf.VrsOutn("RANK1", lineCnt, divRank4);
                svf.VrsOutn("RANK2", lineCnt, totalRank4);
                svf.VrsOutn("INTERVIEW", lineCnt, interview);

                String setSinzoku = sinzokuZokugara;
                setSinzoku += setSinzoku.length() > 0 ? "・" + sinzokuGakkoumei : sinzokuGakkoumei;
                setSinzoku += setSinzoku.length() > 0 ? "・" + sinzokuName : sinzokuName;
                svf.VrsOutn("GRAD", lineCnt, setSinzoku);

                String setFutago = futagoName;
                setFutago += setFutago.length() > 0 ? "・" + futagoZokugara : futagoZokugara;
                svf.VrsOutn("TWINS", lineCnt, setFutago);

                String setClub = clubCd + clubRank;
                svf.VrsOutn("CLUB_RECOMMEND", lineCnt, setClub);

                String setJizen = jizenSoudanCd;
                setJizen += setJizen.length() > 0 ? "・" + jizenSoudanText : jizenSoudanText;
                svf.VrsOutn("CONSULTATION", lineCnt, setJizen);

                svf.VrsOutn("SDIV", lineCnt, shName);
                svf.VrsOutn("FINSCHHO_CD", lineCnt, fsCd);
                final String schoolField = getMS932Bytecount(finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, finschoolName);
                svf.VrsOutn("HOPE1", lineCnt, course1);
                svf.VrsOutn("HOPE2", lineCnt, course2);

                lineCnt++;
                renBan++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf, final String nendo) {
        svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　優遇生一覧表");
        final String gradName = "2".equals(_param._zaisotu) ? "卒業" : "在校生";
        svf.VrsOut("SUBTITLE", "（受験生" + gradName + "関係者）");
        svf.VrsOut("GRAD_NAME", gradName);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            final String subclassName = (String) _param._subclassMap.get(subclassCd);
            svf.VrsOut("SUBCLASS_NAME" + subclassCd, subclassName);
        }
    }

    private String getTitle() {
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        final String title = nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　優遇生一覧表";
        return title;
    }

    private String getSubTitle() {
        final String gradName = "2".equals(_param._zaisotu) ? "卒業" : "在校生";
        final String subtitle = "（受験生" + gradName + "関係者）";
        return subtitle;
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     L013.NAME1 AS JUDGE, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            stb.append("     SCORE" + subclassCd + ".SCORE AS SCORE" + subclassCd + ", ");
            stb.append("     SCORE" + subclassCd + ".ATTEND_FLG AS ATTEND_FLG" + subclassCd + ", ");
        }
        stb.append("     VALUE(RECEPT.TOTAL4, 0) AS TOTAL4, ");
        stb.append("     RECEPT.DIV_RANK4, ");
        stb.append("     RECEPT.TOTAL_RANK4, ");
        stb.append("     VALUE(H201.NAME1, '') AS SINZOKU_ZOKUGARA, ");
        stb.append("     VALUE(VBASE.SINZOKU_GAKKOUMEI, '') AS SINZOKU_GAKKOUMEI, ");
        stb.append("     VALUE(VBASE.SINZOKU_NAME, '') AS SINZOKU_NAME, ");
        stb.append("     VALUE(VBASE.SINZOKU_FUTAGO_NAME, '') AS SINZOKU_FUTAGO_NAME, ");
        stb.append("     VALUE(H201_2.NAME1, '') AS SINZOKU_FUTAGO_ZOKUGARA, ");
        stb.append("     VALUE(L037.NAME1, '') AS CLUB_CD, ");
        stb.append("     VALUE(L025.NAME1, '') AS CLUB_RANK, ");
        stb.append("     VALUE(L032.NAME1, '') AS JIZEN_SOUDAN_CD, ");
        stb.append("     VALUE(VBASE.JIZEN_SOUDAN_TEXT, '')  AS JIZEN_SOUDAN_TEXT, ");
        stb.append("     VALUE(L027.NAME1, '') AS INTERVIEW, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
        stb.append("          AND VBASE.SINZOKU_ZOKUGARA = H201.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST H201_2 ON H201_2.NAMECD1 = 'H201' ");
        stb.append("          AND VBASE.SINZOKU_FUTAGO_ZOKUGARA = H201_2.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L037 ON L037.NAMECD1 = 'L037' ");
        stb.append("          AND VBASE.CLUB_CD = L037.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L025 ON L025.NAMECD1 = 'L025' ");
        stb.append("          AND VBASE.CLUB_RANK = L025.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L032 ON L032.NAMECD1 = 'L032' ");
        stb.append("          AND VBASE.JIZEN_SOUDAN_CD = L032.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ON VBASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND VBASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND VBASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("           AND VBASE.EXAMNO = RECEPT.EXAMNO ");
        for (Iterator iterator = _param._subclassMap.keySet().iterator(); iterator.hasNext();) {
            final String subclassCd = (String) iterator.next();
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE" + subclassCd + " ON RECEPT.ENTEXAMYEAR = SCORE" + subclassCd + ".ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = SCORE" + subclassCd + ".APPLICANTDIV ");
            stb.append("          AND RECEPT.TESTDIV = SCORE" + subclassCd + ".TESTDIV ");
            stb.append("          AND RECEPT.EXAM_TYPE = SCORE" + subclassCd + ".EXAM_TYPE ");
            stb.append("          AND RECEPT.RECEPTNO = SCORE" + subclassCd + ".RECEPTNO ");
            stb.append("          AND SCORE" + subclassCd + ".TESTSUBCLASSCD = '" + subclassCd + "' ");
        }
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ON RECEPT.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = INTERVIEW.APPLICANTDIV ");
        stb.append("          AND RECEPT.TESTDIV = INTERVIEW.TESTDIV ");
        stb.append("          AND RECEPT.EXAMNO = INTERVIEW.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L027 ON L027.NAMECD1 = 'L027' ");
        stb.append("          AND INTERVIEW.INTERVIEW_VALUE = L027.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.DAI2_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.DAI2_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.DAI2_COURSECODE = COURSE2.EXAMCOURSECD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VBASE.SINZOKU_ZAI_GRD_FLG = '" + _param._zaisotu + "' ");
        if ("1".equals(_param._zaisotu) && "1".equals(_param._twins)) {
            stb.append("     AND (VBASE.SINZOKU_FUTAGO_NAME IS NOT NULL OR VBASE.SINZOKU_FUTAGO_ZOKUGARA IS NOT NULL) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57294 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _zaisotu;//1:在校生 2:卒業生
        private final String _twins;
        private final String _loginDate;
        final String _cmd;
        final String _applicantdivName;
        final String _testdivAbbv1;
        final Map _subclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _zaisotu = request.getParameter("ZAISOTU");
            _twins = request.getParameter("TWINS");
            _loginDate = request.getParameter("LOGIN_DATE");
            _cmd = request.getParameter("cmd");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
            _subclassMap = getSubclassList(db2);
        }

        private Map getSubclassList(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L009' ORDER BY NAMECD2 ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
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

