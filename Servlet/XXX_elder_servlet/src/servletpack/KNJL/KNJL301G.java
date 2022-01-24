/*
 * $Id: 1876eecbce183049979e2dc4f5a932059e836b47 $
 *
 * 作成日: 2016/12/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJL301G {

    private static final Log log = LogFactory.getLog(KNJL301G.class);

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
        svf.VrSetForm("KNJL301G.frm", 1);

        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　志願者データチェックリスト");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 30;
            int lineCnt = 1;
            String aftCd = "";
            String befCd = "";
            while (rs.next()) {
                final String examno             = null != rs.getString("EXAMNO") ? rs.getString("EXAMNO") : "";
                final String name               = null != rs.getString("NAME") ? rs.getString("NAME") : "";
                final String nameKana           = null != rs.getString("NAME_KANA") ? rs.getString("NAME_KANA") : "";
                final String birthday           = null != rs.getString("BIRTHDAY") ? rs.getString("BIRTHDAY") : "";
                final String sex                = null != rs.getString("SEX") ? rs.getString("SEX") : "";
                final String cmc                = null != rs.getString("CMC") ? rs.getString("CMC") : "";
                final String course1            = null != rs.getString("COURSE1") ? rs.getString("COURSE1") : "";
                final String course2            = null != rs.getString("COURSE2") ? rs.getString("COURSE2") : "";
                final String shDiv              = null != rs.getString("SHDIV") ? rs.getString("SHDIV") : "";
                final String shName             = null != rs.getString("SH_NAME") ? rs.getString("SH_NAME") : "";
                final String gradName           = null != rs.getString("GRAD_NAME") ? rs.getString("GRAD_NAME") : "";
                final String fsCd               = null != rs.getString("FS_CD") ? rs.getString("FS_CD") : "";
                final String finschoolName      = null != rs.getString("FINSCHOOL_NAME") ? rs.getString("FINSCHOOL_NAME") : "";
                final String confidentialRpt01  = null != rs.getString("CONFIDENTIAL_RPT01") ? rs.getString("CONFIDENTIAL_RPT01") : "";
                final String confidentialRpt02  = null != rs.getString("CONFIDENTIAL_RPT02") ? rs.getString("CONFIDENTIAL_RPT02") : "";
                final String confidentialRpt03  = null != rs.getString("CONFIDENTIAL_RPT03") ? rs.getString("CONFIDENTIAL_RPT03") : "";
                final String confidentialRpt04  = null != rs.getString("CONFIDENTIAL_RPT04") ? rs.getString("CONFIDENTIAL_RPT04") : "";
                final String confidentialRpt05  = null != rs.getString("CONFIDENTIAL_RPT05") ? rs.getString("CONFIDENTIAL_RPT05") : "";
                final String confidentialRpt06  = null != rs.getString("CONFIDENTIAL_RPT06") ? rs.getString("CONFIDENTIAL_RPT06") : "";
                final String confidentialRpt07  = null != rs.getString("CONFIDENTIAL_RPT07") ? rs.getString("CONFIDENTIAL_RPT07") : "";
                final String confidentialRpt08  = null != rs.getString("CONFIDENTIAL_RPT08") ? rs.getString("CONFIDENTIAL_RPT08") : "";
                final String confidentialRpt09  = null != rs.getString("CONFIDENTIAL_RPT09") ? rs.getString("CONFIDENTIAL_RPT09") : "";
                final String totalAll           = null != rs.getString("TOTAL_ALL") ? rs.getString("TOTAL_ALL") : "";
                final String averageAll         = null != rs.getString("AVERAGE_ALL") ? rs.getString("AVERAGE_ALL") : "";
                final String kasantenAll        = null != rs.getString("KASANTEN_ALL") ? rs.getString("KASANTEN_ALL") : "";
                final String absenceDays        = null != rs.getString("ABSENCE_DAYS") ? rs.getString("ABSENCE_DAYS") : "";
                final String absenceDays2       = null != rs.getString("ABSENCE_DAYS2") ? rs.getString("ABSENCE_DAYS2") : "";
                final String absenceDays3       = null != rs.getString("ABSENCE_DAYS3") ? rs.getString("ABSENCE_DAYS3") : "";
                final String absenceRemark      = null != rs.getString("ABSENCE_REMARK") ? rs.getString("ABSENCE_REMARK") : "";
                final String absenceRemark2     = null != rs.getString("ABSENCE_REMARK2") ? rs.getString("ABSENCE_REMARK2") : "";
                final String absenceRemark3     = null != rs.getString("ABSENCE_REMARK3") ? rs.getString("ABSENCE_REMARK3") : "";
                final String jituryokuKokugo    = null != rs.getString("JITURYOKU_KOKUGO") ? rs.getString("JITURYOKU_KOKUGO") : "";
                final String jituryokuEigo      = null != rs.getString("JITURYOKU_EIGO") ? rs.getString("JITURYOKU_EIGO") : "";
                final String jituryokuSuugaku   = null != rs.getString("JITURYOKU_SUUGAKU") ? rs.getString("JITURYOKU_SUUGAKU") : "";
                final String jituryokuTotal     = null != rs.getString("JITURYOKU_TOTAL") ? rs.getString("JITURYOKU_TOTAL") : "";
                final String mogiHensati        = null != rs.getString("MOGI_HENSATI") ? rs.getString("MOGI_HENSATI") : "";
                final String sinzokuName        = null != rs.getString("SINZOKU_NAME") ? rs.getString("SINZOKU_NAME") : "";
                final String sinzokuKyusei      = null != rs.getString("SINZOKU_KYUSEI") ? rs.getString("SINZOKU_KYUSEI") : "";
                final String sinzokuZokugara    = null != rs.getString("SINZOKU_ZOKUGARA") ? rs.getString("SINZOKU_ZOKUGARA") : "";
                final String sinzokuGakkoumei   = null != rs.getString("SINZOKU_GAKKOUMEI") ? rs.getString("SINZOKU_GAKKOUMEI") : "";
                final String sinzokuGakka       = null != rs.getString("SINZOKU_GAKKA") ? rs.getString("SINZOKU_GAKKA") : "";
                final String sinzokuZaiGrdYear  = null != rs.getString("SINZOKU_ZAI_GRD_YEAR") ? rs.getString("SINZOKU_ZAI_GRD_YEAR") : "";
                final String sinzokuZaiGrd      = null != rs.getString("SINZOKU_ZAI_GRD") ? rs.getString("SINZOKU_ZAI_GRD") : "";
                final String sinzokuFutagoName  = null != rs.getString("SINZOKU_FUTAGO_NAME") ? rs.getString("SINZOKU_FUTAGO_NAME") : "";
                final String futagoZokugara     = null != rs.getString("FUTAGO_ZOKUGARA") ? rs.getString("FUTAGO_ZOKUGARA") : "";
                final String clubName           = null != rs.getString("CLUB_NAME") ? rs.getString("CLUB_NAME") : "";
                final String clubRank           = null != rs.getString("CLUB_RANK") ? rs.getString("CLUB_RANK") : "";
                final String jizenSoudan        = null != rs.getString("JIZEN_SOUDAN") ? rs.getString("JIZEN_SOUDAN") : "";
                final String jizenSoudanText    = null != rs.getString("JIZEN_SOUDAN_TEXT") ? rs.getString("JIZEN_SOUDAN_TEXT") : "";
                if ("1".equals(_param._kaipage)) {
                    aftCd = cmc;
                } else {
                    aftCd = shDiv;
                }
                if (lineCnt > maxLine || (!"".equals(befCd) && !befCd.equals(aftCd))) {
                    svf.VrEndPage();
                    svf.VrsOut("TITLE", nendo + "　" + _param._applicantdivName + "　" + _param._testdivAbbv1 + "　志願者データチェックリスト");
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
                    lineCnt = 1;
                }
                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                final String kanaField = getMS932Bytecount(nameKana) > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, lineCnt, nameKana);
                svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(birthday));
                svf.VrsOutn("SEX", lineCnt, sex);
                svf.VrsOutn("HOPE1", lineCnt, course1);
                svf.VrsOutn("HOPE2", lineCnt, course2);
                svf.VrsOutn("SDIV", lineCnt, shName);
                if ("1".equals(_param._kaipage)) {
                    svf.VrsOut("SUBTITLE", "（" + course1 + "）");
                } else {
                    svf.VrsOut("SUBTITLE", "（" + shName + "）");
                }
                svf.VrsOutn("FINSCHHO_CD", lineCnt, fsCd);
                final String schoolField = getMS932Bytecount(finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, finschoolName);
                svf.VrsOutn("GRAD_DIV", lineCnt, gradName);

                svf.VrsOutn("CLASS_DIV1", lineCnt, confidentialRpt01);
                svf.VrsOutn("CLASS_DIV2", lineCnt, confidentialRpt02);
                svf.VrsOutn("CLASS_DIV3", lineCnt, confidentialRpt03);
                svf.VrsOutn("CLASS_DIV4", lineCnt, confidentialRpt04);
                svf.VrsOutn("CLASS_DIV5", lineCnt, confidentialRpt05);
                svf.VrsOutn("CLASS_DIV6", lineCnt, confidentialRpt06);
                svf.VrsOutn("CLASS_DIV7", lineCnt, confidentialRpt07);
                svf.VrsOutn("CLASS_DIV8", lineCnt, confidentialRpt08);
                svf.VrsOutn("CLASS_DIV9", lineCnt, confidentialRpt09);
                svf.VrsOutn("CLASS_DIV_TOTAL", lineCnt, totalAll);
                svf.VrsOutn("CLASS_DIV_AVE", lineCnt, averageAll);
                svf.VrsOutn("CLASS_DIV_VAL", lineCnt, kasantenAll);
                svf.VrsOutn("ABSENCE1", lineCnt, absenceDays);
                svf.VrsOutn("ABSENCE2", lineCnt, absenceDays2);
                svf.VrsOutn("ABSENCE3", lineCnt, absenceDays3);
                svf.VrsOutn("ABSENCE_REMARK1", lineCnt, absenceRemark);
                svf.VrsOutn("ABSENCE_REMARK2", lineCnt, absenceRemark2);
                svf.VrsOutn("ABSENCE_REMARK3", lineCnt, absenceRemark3);
                svf.VrsOutn("MOCK_CLASS_SCORE1", lineCnt, jituryokuKokugo);
                svf.VrsOutn("MOCK_CLASS_SCORE2", lineCnt, jituryokuEigo);
                svf.VrsOutn("MOCK_CLASS_SCORE3", lineCnt, jituryokuSuugaku);
                svf.VrsOutn("MOCK_CLASS_SCORE_TOTAL", lineCnt, jituryokuTotal);
                svf.VrsOutn("MOCK_DEV1", lineCnt, mogiHensati);
                final String setSinzokuName = sinzokuName + (!"".equals(sinzokuKyusei) ? "(" + sinzokuKyusei + ")" : "");
                final String rNameField = getMS932Bytecount(setSinzokuName) > 30 ? "2" : "1";
                svf.VrsOutn("RELA_NAME" + rNameField, lineCnt, setSinzokuName);
                svf.VrsOutn("RELA_DIV", lineCnt, sinzokuZokugara);
                final String sinzokuGakkoumeiField = getMS932Bytecount(sinzokuGakkoumei) > 20 ? "2" : "1";
                svf.VrsOutn("RELA_FINSCHOOL_NAME" + sinzokuGakkoumeiField, lineCnt, sinzokuGakkoumei);
                svf.VrsOutn("RELA_COURSE_NAM1", lineCnt, sinzokuGakka);
                svf.VrsOutn("RELA_YEAR", lineCnt, sinzokuZaiGrdYear);
                svf.VrsOutn("RELA_GRAD_DIV", lineCnt, sinzokuZaiGrd);
                final String twinNameField = getMS932Bytecount(sinzokuFutagoName) > 30 ? "2" : "1";
                svf.VrsOutn("TWIN_NAME" + twinNameField, lineCnt, sinzokuFutagoName);
                svf.VrsOutn("TWIN_RELATION", lineCnt, futagoZokugara);
                svf.VrsOutn("CLUB_NAME", lineCnt, clubName);
                svf.VrsOutn("CLUB_RECOMMEND_RANK", lineCnt, clubRank);
                svf.VrsOutn("CONSUL", lineCnt, jizenSoudan);
                svf.VrsOutn("CONSUL_ETC", lineCnt, jizenSoudanText);

                befCd = aftCd;
                lineCnt++;
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
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS CMC, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     VBASE.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     L016.NAME1 AS GRAD_NAME, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT01, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT02, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT03, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT04, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT05, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT06, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT07, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT08, ");
        stb.append("     CONFRPT.CONFIDENTIAL_RPT09, ");
        stb.append("     CONFRPT.TOTAL_ALL, ");
        stb.append("     CONFRPT.AVERAGE_ALL, ");
        stb.append("     CONFRPT.KASANTEN_ALL, ");
        stb.append("     CONFRPT.ABSENCE_DAYS, ");
        stb.append("     CONFRPT.ABSENCE_DAYS2, ");
        stb.append("     CONFRPT.ABSENCE_DAYS3, ");
        stb.append("     CONFRPT.ABSENCE_REMARK, ");
        stb.append("     CONFRPT.ABSENCE_REMARK2, ");
        stb.append("     CONFRPT.ABSENCE_REMARK3, ");
        stb.append("     VBASE.JITURYOKU_KOKUGO, ");
        stb.append("     VBASE.JITURYOKU_EIGO, ");
        stb.append("     VBASE.JITURYOKU_SUUGAKU, ");
        stb.append("     VBASE.JITURYOKU_TOTAL, ");
        stb.append("     VBASE.MOGI_HENSATI, ");
        stb.append("     VBASE.SINZOKU_NAME, ");
        stb.append("     VBASE.SINZOKU_KYUSEI, ");
        stb.append("     H201.NAME1 AS SINZOKU_ZOKUGARA, ");
        stb.append("     VBASE.SINZOKU_GAKKOUMEI, ");
        stb.append("     VBASE.SINZOKU_GAKKA, ");
        stb.append("     VBASE.SINZOKU_ZAI_GRD_YEAR, ");
        stb.append("     CASE WHEN VBASE.SINZOKU_ZAI_GRD_FLG = '1' THEN '在学生' ELSE '卒業生' END AS SINZOKU_ZAI_GRD, ");
        stb.append("     VBASE.SINZOKU_FUTAGO_NAME, ");
        stb.append("     H201_2.NAME1 AS FUTAGO_ZOKUGARA, ");
        stb.append("     L037.NAME1 AS CLUB_NAME, ");
        stb.append("     L025.NAME1 AS CLUB_RANK, ");
        stb.append("     L032.NAME1 AS JIZEN_SOUDAN, ");
        stb.append("     VBASE.JIZEN_SOUDAN_TEXT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L016 ON L016.NAMECD1 = 'L016' ");
        stb.append("          AND VBASE.FS_GRDDIV = L016.NAMECD2 ");
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
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON VBASE.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR ");
        stb.append("          AND VBASE.EXAMNO = CONFRPT.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._kaipage)) {
            stb.append("     VBASE.DAI1_COURSECD, ");
            stb.append("     VBASE.DAI1_MAJORCD, ");
            stb.append("     VBASE.DAI1_COURSECODE, ");
        } else {
            stb.append("     VBASE.SHDIV, ");
        }
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57253 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _kaipage;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _kaipage = request.getParameter("KAIPAGE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
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

