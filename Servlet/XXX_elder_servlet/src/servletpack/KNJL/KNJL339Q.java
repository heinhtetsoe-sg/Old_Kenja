/*
 * $Id: a729a42851a68716a9c43284fba6ba2902b12335 $
 *
 * 作成日: 2017/04/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL339Q {

    private static final Log log = LogFactory.getLog(KNJL339Q.class);

    private boolean _hasData;

    private final String SUISEN = "2";
    private final String IPPAN = "3";

    private final String L004KOUTYOU = "3";
    private final String L004JIKO = "4";
    private final String L004IPPAN = "5";
    private final String L004SUNTYU = "9";

    private final String L045KAIGAI = "1";
    private final String L045SUISEN = "2";
    private final String L045IPPAN = "3";
    private final String L045SUNTYU = "9";

    private final String L013GOUKAKU = "1";
    private final String L013FUGOUKAKU = "2";
    private final String L013HOKETSU = "3";
    private final String L013KESSEKI = "4";

    private final String L011NASHI = "2";

    private final String L012JITAI = "2";

    private final String PRINT_KOUTYOU = "1";
    private final String PRINT_JIKO = "2";
    private final String PRINT_IPPAN = "3";
    private final String PRINT_FUTSU = "4";
    private final String PRINT_SPORT = "5";
    private final String PRINT_SUNTYU = "6";
    private final String PRINT_IPPAN_SPORT = "7";
    private final String PRINT_IPPAN_FUTSU = "8";

    private final String COURSE_FUTSU = "11";
    private final String COURSE_SPORT = "12";

    private final String KOKUGO = "1";
    private final String SUUGAKU = "2";
    private final String RIKA = "3";
    private final String EIGO = "5";
    private final String SHOURON = "6";

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

            if ("1".equals(_param._kocho)) {
                //表紙印刷
                printSuisenHyoushi(db2, svf, PRINT_KOUTYOU);
                printSuisen(db2, svf, PRINT_KOUTYOU, COURSE_FUTSU);
                printSuisen(db2, svf, PRINT_KOUTYOU, COURSE_SPORT);
            }
            if ("1".equals(_param._jiko)) {
                //表紙印刷
                printSuisenHyoushi(db2, svf, PRINT_JIKO);
                printSuisen(db2, svf, PRINT_JIKO, COURSE_FUTSU);
                printSuisen(db2, svf, PRINT_JIKO, COURSE_SPORT);
            }
            if ("1".equals(_param._ippan)) {
                //表紙印刷
                if ("1".equals(_param._order)) {
                    printIppanHyoushi(db2, svf, PRINT_IPPAN);
                } else {
                    printIppanExamHyoushi(db2, svf, PRINT_IPPAN);
                }
                print339_2_2Form(db2, svf, PRINT_IPPAN, "");
            }
            if ("1".equals(_param._normal)) {
                //表紙印刷
                printFutsuHyoushi(db2, svf, PRINT_FUTSU, COURSE_FUTSU);
                print339_4_2Form(db2, svf, PRINT_FUTSU, COURSE_FUTSU);
            }
            if ("1".equals(_param._sport)) {
                //表紙印刷
                printFutsuHyoushi(db2, svf, PRINT_SPORT, COURSE_SPORT);
                print339_5_2Form(db2, svf, PRINT_SPORT, COURSE_SPORT);
            }
            if ("1".equals(_param._sunchu)) {
                //表紙印刷
                printSuntyuHyoushi(db2, svf, PRINT_SUNTYU);
                print339_2_2Form(db2, svf, PRINT_SUNTYU, "");
            }
            if ("1".equals(_param._sgsport)) {
                //表紙印刷
                printIppanSportHyoushi(db2, svf, PRINT_IPPAN_SPORT);
                print339_5_2Form(db2, svf, PRINT_IPPAN_SPORT, COURSE_SPORT);
            }
            if ("1".equals(_param._sgnormal)) {
                //表紙印刷
                printIppanFutsuHyoushi(db2, svf, PRINT_IPPAN_FUTSU);
                print339_4_2Form(db2, svf, PRINT_IPPAN_FUTSU, COURSE_FUTSU);
            }
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

    private CourseMst getCourseMstKeyEndsWith(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (m.containsKey(field)) {
            return (CourseMst) m.get(field);
        }
        final List keyList = new ArrayList(m.keySet());
        Collections.sort(keyList);
        String key = null;
        for (final Iterator keyIt = keyList.iterator(); keyIt.hasNext();) {
            final String k = (String) keyIt.next();
            if (null == k && null == field || null != k && k.endsWith(field)) { // フィールドをソートして最初に「field like '%{k}'」条件が合致した値
                key = k;
                break;
            }
        }
        return (CourseMst) m.get(key);
    }

    private void printSuisen(final DB2UDB db2, final Vrw32alp svf, final String printDiv, final String course) {

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            if (PRINT_KOUTYOU.equals(printDiv)) {
                svf.VrSetForm("KNJL339Q_1_2.frm", 4);
            } else {
                svf.VrSetForm("KNJL339Q_1_3.frm", 4);
            }
            final String sqlCnt = sqlSuisen(printDiv, course, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 20;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlSuisen(printDiv, course, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final String getKey = PRINT_KOUTYOU.equals(printDiv) ? L004KOUTYOU : L004JIKO;
            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("TITLE", _param._entexamyear + "年度　" + ((NameMst) _param._l004Map.get(getKey))._abbv1 + "入学試験" + _param._orderName + "順一覧(" + getCourseMstKeyEndsWith(_param._courseMap, course)._examcourseName + ")");
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                svf.VrsOut("CLASS_NAME1", ((NameMst) _param._l009Map.get(KOKUGO))._abbv1);
                svf.VrsOut("CLASS_NAME2", ((NameMst) _param._l009Map.get(SUUGAKU))._abbv1);
                svf.VrsOut("CLASS_NAME3", ((NameMst) _param._l009Map.get(RIKA))._abbv1);
                svf.VrsOut("CLASS_NAME4", ((NameMst) _param._l009Map.get(EIGO))._abbv1);
                svf.VrsOut("CLASS_NAME5", ((NameMst) _param._l009Map.get(SHOURON))._abbv1);
                svf.VrsOut("CLASS_NAME6", "合計");
                svf.VrsOut("CLASS_NAME7", "面接");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

                final String judgementName1 = rs.getString("JUDGEMENT_NAME1");
                final String rank = rs.getString("RANK");
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String prefName = rs.getString("PREF_NAME");
                final String kokugo = rs.getString("KOKUGO");
                final String suugaku = rs.getString("SUUGAKU");
                final String rika = rs.getString("RIKA");
                final String eigo = rs.getString("EIGO");
                final String shouron = rs.getString("SHOURON");
                final String total = rs.getString("TOTAL");
                final String interview = rs.getString("INTERVIEW");
                final String publicScore1 = rs.getString("PUBLIC_SCORE1");
                final String publicScore2 = rs.getString("PUBLIC_SCORE2");
                final String publicValue = rs.getString("PUBLIC_VALUE");
                final String confAvg2_5 = rs.getString("CONF_AVG2_5");
                final String confAvg2_9 = rs.getString("CONF_AVG2_9");
                final String confAvg3_5 = rs.getString("CONF_AVG3_5");
                final String confAvg3_9 = rs.getString("CONF_AVG3_9");
                final String confAbsence1 = rs.getString("CONF_ABSENCE1");
                final String confAbsence2 = rs.getString("CONF_ABSENCE2");
                final String confAbsence3 = rs.getString("CONF_ABSENCE3");
                final String confTokki = rs.getString("CONF_TOKKI");
                final String satTotal = rs.getString("SAT_TOTAL");
                final String satJudge = rs.getString("SAT_JUDGE");
                final String satNo = rs.getString("SAT_NO");
                final String mockAugDev = rs.getString("MOCK_AUG_DEV");
                final String mockSepDev = rs.getString("MOCK_SEP_DEV");
                final String scholarKibou = rs.getString("SCHOLAR_KIBOU");
                final String scholarSaiyou = rs.getString("SCHOLAR_SAIYOU");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");
                final String aoKatudou1 = rs.getString("AO_KATUDOU1");
                final String aoKatudou2 = rs.getString("AO_KATUDOU2");
                final String aoTokki = rs.getString("AO_TOKKI");

                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("JUDGE", judgementName1);
                svf.VrsOut("RANK", rank);
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sexAbbv1);
                svf.VrsOut("JHSCHOOL_NAME", finschoolName);
                svf.VrsOut("PREF_NAME", prefName);
                svf.VrsOut("SCORE1", kokugo);
                svf.VrsOut("SCORE2", suugaku);
                svf.VrsOut("SCORE3", rika);
                svf.VrsOut("SCORE4", eigo);
                svf.VrsOut("SCORE5", shouron);
                svf.VrsOut("SCORE6", total);
                svf.VrsOut("SCORE7", interview);
                svf.VrsOut("VISIT1", publicScore1);
                svf.VrsOut("VISIT2", publicScore2);
                svf.VrsOut("VISIT3", publicValue);
                svf.VrsOut("VISIT4", "");
                svf.VrsOut("REPORT1", confAvg2_5);
                svf.VrsOut("REPORT2", confAvg2_9);
                svf.VrsOut("REPORT3", confAvg3_5);
                svf.VrsOut("REPORT4", confAvg3_9);
                svf.VrsOut("NOTICE1", confAbsence1);
                svf.VrsOut("NOTICE2", confAbsence2);
                svf.VrsOut("NOTICE3", confAbsence3);

                if (PRINT_KOUTYOU.equals(printDiv)) {
                    final String[] tokki = KNJ_EditEdit.get_token(confTokki, 40, 4);
                    printBunkatsu(svf, tokki, "NOTE1_");
                } else {
                    svf.VrsOut("INTERVIEW1", "");
                    svf.VrsOut("INTERVIEW2", interview);
                    final String[] katudou1 = KNJ_EditEdit.get_token(aoKatudou1, 34, 3);
                    printBunkatsu(svf, katudou1, "VISIT1_");
                    final String[] tokki = KNJ_EditEdit.get_token(aoTokki, 34, 3);
                    printBunkatsu(svf, tokki, "NOTE1_");
                    final String[] katudou2 = KNJ_EditEdit.get_token(aoKatudou2, 34, 3);
                    printBunkatsu(svf, katudou2, "COMMENT1_");
                }

                svf.VrsOut("SAT1", satTotal);
                svf.VrsOut("SAT2", satJudge);
                svf.VrsOut("SAT3", satNo);
                svf.VrsOut("MOCK1", mockAugDev);
                svf.VrsOut("MOCK2", mockSepDev);

                svf.VrsOut("SCHOLAR1", scholarKibou);
                svf.VrsOut("SCHOLAR2", scholarSaiyou + " " + scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);

                svf.VrEndRecord();
                lineNo++;
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlSuisen(final String printDiv, final String course, final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         R1.TESTDIV, ");
        stb.append("         R1.EXAMNO, ");
        stb.append("         R1.TOTAL_RANK4 AS RANK, ");
        stb.append("         R1.TOTAL4 AS TOTAL, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '" + KOKUGO + "' THEN S1.SCORE END) AS KOKUGO, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '" + SUUGAKU + "' THEN S1.SCORE END) AS SUUGAKU, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '" + RIKA + "' THEN S1.SCORE END) AS RIKA, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '" + EIGO + "' THEN S1.SCORE END) AS EIGO, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '" + SHOURON + "' THEN S1.SCORE END) AS SHOURON ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECEPT_DAT R1 ");
        stb.append("         LEFT JOIN ENTEXAM_SCORE_DAT S1 ON S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("                 AND S1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("                 AND S1.TESTDIV = R1.TESTDIV ");
        stb.append("                 AND S1.RECEPTNO = R1.RECEPTNO ");
        stb.append("     WHERE ");
        stb.append("             R1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND R1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     GROUP BY ");
        stb.append("         R1.TESTDIV, ");
        stb.append("         R1.EXAMNO, ");
        stb.append("         R1.TOTAL_RANK4, ");
        stb.append("         R1.TOTAL4 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     B1.DAI1_COURSECODE, ");
            stb.append("     N2.NAME1 AS JUDGEMENT_NAME1, ");
            stb.append("     S1.RANK, ");
            stb.append("     B1.EXAMNO, ");
            stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
            stb.append("     B1.NAME, ");
            stb.append("     N3.ABBV1 AS SEX_ABBV1, ");
            stb.append("     F1.FINSCHOOL_NAME, ");
            stb.append("     PREF.PREF_NAME, ");
            stb.append("     S1.KOKUGO, ");
            stb.append("     S1.SUUGAKU, ");
            stb.append("     S1.RIKA, ");
            stb.append("     S1.EIGO, ");
            stb.append("     S1.SHOURON, ");
            stb.append("     S1.TOTAL, ");
            stb.append("     N5.NAME1 AS INTERVIEW, ");
            stb.append("     PUB.SCORE1 AS PUBLIC_SCORE1, ");
            stb.append("     PUB.SCORE2 AS PUBLIC_SCORE2, ");
            stb.append("     PUB.VALUE AS PUBLIC_VALUE, ");
            stb.append("     CONF_D.REMARK11 AS CONF_AVG2_5, ");
            stb.append("     CONF_D.REMARK12 AS CONF_AVG2_9, ");
            stb.append("     CONF.AVERAGE5 AS CONF_AVG3_5, ");
            stb.append("     CONF.AVERAGE_ALL AS CONF_AVG3_9, ");
            stb.append("     CONF.ABSENCE_DAYS AS CONF_ABSENCE1, ");
            stb.append("     CONF.ABSENCE_DAYS2 AS CONF_ABSENCE2, ");
            stb.append("     CONF.ABSENCE_DAYS3 AS CONF_ABSENCE3, ");
            stb.append("     CONF.REMARK1 AS CONF_TOKKI, ");
            stb.append("     SAT_E.SCORE_TOTAL AS SAT_TOTAL, ");
            stb.append("     N4.NAME1 AS SAT_JUDGE, ");
            stb.append("     SAT_E.SAT_NO, ");
            stb.append("     SAT_A.MOCK_AUG_DEV, ");
            stb.append("     SAT_A.MOCK_SEP_DEV, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ");
            stb.append("          ELSE '無' ");
            stb.append("     END AS SCHOLAR_KIBOU, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU = '1' THEN '採用' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU IS NULL THEN '不採用' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCHOLAR_SAIYOU, ");
            stb.append("     VALUE(B1.SCHOLAR_TOUKYU_SENGAN, '') AS SCHOLAR_TOUKYU, ");
            stb.append("     CASE WHEN B1.DORMITORY_FLG = '1' THEN 'レ' END AS DORMITORY_KIBOU, ");
            stb.append("     CONF_SEQ003.REMARK1 AS AO_KATUDOU1, ");
            stb.append("     CONF_SEQ003.REMARK2 AS AO_KATUDOU2, ");
            stb.append("     CONF_SEQ003.REMARK9 AS AO_TOKKI, ");
            stb.append("     C1.EXAMCOURSE_ABBV ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     LEFT JOIN PREF_MST PREF ON PREF.PREF_CD = F1.FINSCHOOL_PREF_CD ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L013' AND N2.NAMECD2 = B1.JUDGEMENT ");
        stb.append("     LEFT JOIN V_NAME_MST N3 ON N3.YEAR = B1.ENTEXAMYEAR AND N3.NAMECD1 = 'Z002' AND N3.NAMECD2 = B1.SEX ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append("     LEFT JOIN ENTEXAM_PUBLIC_TEST_DAT PUB ON PUB.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND PUB.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND PUB.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND CONF.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND CONF.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_D ON CONF_D.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND CONF_D.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND CONF_D.EXAMNO = B1.EXAMNO ");
        stb.append("             AND CONF_D.SEQ = '002' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_SEQ003 ON CONF_SEQ003.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND CONF_SEQ003.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND CONF_SEQ003.EXAMNO = B1.EXAMNO ");
        stb.append("             AND CONF_SEQ003.SEQ = '003' ");
        stb.append("     LEFT JOIN SAT_EXAM_DAT SAT_E ON SAT_E.YEAR = '" + _param._loginYear + "' ");
        stb.append("             AND SAT_E.SAT_NO = B1.JIZEN_BANGOU ");
        stb.append("     LEFT JOIN V_NAME_MST N4 ON N4.YEAR = SAT_E.YEAR AND N4.NAMECD1 = 'L200' AND N4.NAMECD2 = SAT_E.JUDGE_SAT ");
        stb.append("     LEFT JOIN SAT_APP_FORM_MST SAT_A ON SAT_A.YEAR = '" + _param._loginYear + "' ");
        stb.append("             AND SAT_A.SAT_NO = B1.JIZEN_BANGOU ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND INTV.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND INTV.TESTDIV = B1.TESTDIV ");
        stb.append("             AND INTV.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST N5 ON N5.YEAR = B1.ENTEXAMYEAR AND N5.NAMECD1 = 'L027' AND N5.NAMECD2 = INTV.INTERVIEW_VALUE ");
        stb.append("     LEFT JOIN T_SCORE S1 ON S1.EXAMNO = B1.EXAMNO AND S1.TESTDIV = B1.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (PRINT_KOUTYOU.equals(printDiv)) {
            stb.append("     AND B1.TESTDIV = '" + L004KOUTYOU + "' ");
        }
        if (PRINT_JIKO.equals(printDiv)) {
            stb.append("     AND B1.TESTDIV = '" + L004JIKO + "' ");
        }
        stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + course + "' ");
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     B1.DAI1_COURSECODE, ");
            if ("1".equals(_param._order)) {
                stb.append("     VALUE(S1.TOTAL,-1) DESC, ");
                stb.append("     B1.EXAMNO ");
            } else {
                stb.append("     B1.EXAMNO ");
            }
        }
        return stb.toString();
    }

    private void printSuisenHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL339Q_1_1.frm", 1);
            final String getKey = PRINT_KOUTYOU.equals(printDiv) ? L004KOUTYOU : L004JIKO;

            svf.VrsOut("TITLE", ((NameMst) _param._l004Map.get(getKey))._abbv1 + "入学試験合否台帳");
            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(getKey))._spare1, "yyyy年MM月dd日");
            final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(getKey))._spare1);
            svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");
            svf.VrsOut("SUBTITLE1", getCourseMstKeyEndsWith(_param._courseMap, COURSE_FUTSU)._examcourseName + _param._orderName + "順一覧");
            svf.VrsOut("SUBTITLE2", getCourseMstKeyEndsWith(_param._courseMap, COURSE_SPORT)._examcourseName + _param._orderName + "順一覧");

            final String sql = sqlSuisenHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrsOut("COURSE_NAME1", getCourseMstKeyEndsWith(_param._courseMap, COURSE_FUTSU)._examcourseAbbv);
            svf.VrsOut("COURSE_NAME2", getCourseMstKeyEndsWith(_param._courseMap, COURSE_SPORT)._examcourseAbbv);
            while (rs.next()) {
                final int setFieldCnt = rs.getString("DAI1_COURSECODE").endsWith(COURSE_FUTSU) ? 1 : 2;
                final String setFieldName = L004KOUTYOU.equals(rs.getString("TESTDIV")) ? "1" : "5";
                if (!"99".equals(rs.getString("TESTDIV"))) {
                    svf.VrsOutn("HOPE_NUM" + setFieldName, setFieldCnt, rs.getString("SIGAN"));
                    svf.VrsOutn("EXAM_NUM" + setFieldName, setFieldCnt, rs.getString("JUKEN"));
                    svf.VrsOutn("NOTICE_NUM" + setFieldName, setFieldCnt, rs.getString("KESSEKI"));
                } else {
                    svf.VrsOutn("TOTAL_HOPE_NUM", setFieldCnt, rs.getString("SIGAN"));
                    svf.VrsOutn("TOTAL_EXAM_NUM", setFieldCnt, rs.getString("JUKEN"));
                    svf.VrsOutn("TOTAL_NOTICE_NUM", setFieldCnt, rs.getString("KESSEKI"));
                }
            }
            svf.VrsOut("SCHOOL_NAME", _param._schoolName + "　普通科");

            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlSuisenHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     B1.DAI1_COURSECODE, ");
        stb.append("     C1.EXAMCOURSE_NAME, ");
        stb.append("     B1.TESTDIV, ");
        stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
        stb.append("     COUNT(B1.EXAMNO) AS SIGAN, ");
        stb.append("     COUNT(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' THEN 1 END) AS JUKEN, ");
        stb.append("     COUNT(CASE WHEN B1.JUDGEMENT =  '" + L013KESSEKI + "' THEN 1 END) AS KESSEKI ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     B1.TESTDIV, ");
        stb.append("     N1.ABBV1, ");
        stb.append("     B1.DAI1_COURSECODE, ");
        stb.append("     C1.EXAMCOURSE_NAME ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     DAI1_COURSECODE, ");
        stb.append("     EXAMCOURSE_NAME, ");
        stb.append("     TESTDIV, ");
        stb.append("     TESTDIV_ABBV1, ");
        stb.append("     SIGAN, ");
        stb.append("     JUKEN, ");
        stb.append("     KESSEKI ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     DAI1_COURSECODE, ");
        stb.append("     EXAMCOURSE_NAME, ");
        stb.append("     '99' AS TESTDIV, ");
        stb.append("     '合計' AS TESTDIV_ABBV1, ");
        stb.append("     SUM(SIGAN) AS SIGAN, ");
        stb.append("     SUM(JUKEN) AS JUKEN, ");
        stb.append("     SUM(KESSEKI) AS KESSEKI ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     DAI1_COURSECODE, ");
        stb.append("     EXAMCOURSE_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     DAI1_COURSECODE, ");
        stb.append("     TESTDIV ");

        return stb.toString();
    }

    private void printIppanHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL339Q_2_1.frm", 1);

            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            svf.VrsOut("TITLE", "入学試験" + _param._orderName + "順一覧");
            final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(L004IPPAN))._spare1, "yyyy年MM月dd日");
            final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(L004IPPAN))._spare1);
            svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");

            final String sql = sqlIppanHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            svf.VrsOutn("ITEM1", 1, "一般入学試験");
            svf.VrsOutn("ITEM2", 1, "志願者数普通");
            svf.VrsOutn("ITEM3", 1, "志願者数スポーツ");
            svf.VrsOutn("ITEM6", 1, "志願者数合計");
            svf.VrsOutn("ITEM_NUM1", 1, rs.getString("SIGAN"));
            svf.VrsOutn("ITEM_NUM2", 1, rs.getString("FUTSU_SIGAN"));
            svf.VrsOutn("ITEM_NUM3", 1, rs.getString("SPORT_SIGAN"));
            svf.VrsOutn("ITEM_NUM6", 1, rs.getString("SIGAN"));

            svf.VrsOutn("ITEM1", 2, "受検者数普通");
            svf.VrsOutn("ITEM2", 2, "受検者数スポーツ");
            svf.VrsOutn("ITEM3", 2, "欠席者数普通");
            svf.VrsOutn("ITEM4", 2, "欠席者数スポーツ");
            svf.VrsOutn("ITEM5", 2, "辞退者");
            svf.VrsOutn("ITEM6", 2, "受検者数合計");
            svf.VrsOutn("ITEM_NUM1", 2, rs.getString("FUTSU_JUKEN"));
            svf.VrsOutn("ITEM_NUM2", 2, rs.getString("SPORT_JUKEN"));
            svf.VrsOutn("ITEM_NUM3", 2, rs.getString("FUTSU_KESSEKI"));
            svf.VrsOutn("ITEM_NUM4", 2, rs.getString("SPORT_KESSEKI"));
            svf.VrsOutn("ITEM_NUM5", 2, rs.getString("JITAI"));
            svf.VrsOutn("ITEM_NUM6", 2, rs.getString("JUKEN"));

            svf.VrsOutn("ITEM1", 3, "普通合格者");
            svf.VrsOutn("ITEM2", 3, "普通不合格者");
            svf.VrsOutn("ITEM3", 3, "スポーツ合格者");
            svf.VrsOutn("ITEM4", 3, "スポーツ不合格者");
            svf.VrsOutn("ITEM_NUM1", 3, rs.getString("FUTSU_GOUKAKU"));
            svf.VrsOutn("ITEM_NUM2", 3, rs.getString("FUTSU_FUGOUKAKU"));
            svf.VrsOutn("ITEM_NUM3", 3, rs.getString("SPORT_GOUKAKU"));
            svf.VrsOutn("ITEM_NUM4", 3, rs.getString("SPORT_FUGOUKAKU"));

            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlIppanHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_SIGAN, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS JUKEN, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_JUKEN, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_JUKEN, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.PROCEDUREDIV = '" + L011NASHI + "' OR B1.ENTDIV = '" + L012JITAI + "' THEN 1 ELSE 0 END) AS JITAI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT =  '" + L013GOUKAKU + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_GOUKAKU, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT =  '" + L013FUGOUKAKU + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_FUGOUKAKU, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT =  '" + L013GOUKAKU + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_GOUKAKU, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT =  '" + L013FUGOUKAKU + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_FUGOUKAKU ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");
        return stb.toString();
    }

    private void printIppanExamHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL339Q_3.frm", 1);

            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            svf.VrsOut("TITLE", "入学試験" + _param._orderName + "順一覧");
            svf.VrsOut("CLASS_NAME1", getCourseMstKeyEndsWith(_param._courseMap, COURSE_FUTSU)._examcourseName);
            final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(L004IPPAN))._spare1, "yyyy年MM月dd日");
            final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(L004IPPAN))._spare1);
            svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");

            final String sql = sqlIppanHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            svf.VrsOutn("ITEM1", 1, "一般入学試験");
            svf.VrsOutn("ITEM2", 1, "志願者数普通");
            svf.VrsOutn("ITEM3", 1, "志願者数スポーツ");
            svf.VrsOutn("ITEM6", 1, "志願者数合計");
            svf.VrsOutn("ITEM_NUM1", 1, rs.getString("SIGAN"));
            svf.VrsOutn("ITEM_NUM2", 1, rs.getString("FUTSU_SIGAN"));
            svf.VrsOutn("ITEM_NUM3", 1, rs.getString("SPORT_SIGAN"));
            svf.VrsOutn("ITEM_NUM6", 1, rs.getString("SIGAN"));

            svf.VrsOutn("ITEM1", 2, "受検者数普通");
            svf.VrsOutn("ITEM2", 2, "受検者数スポーツ");
            svf.VrsOutn("ITEM3", 2, "欠席者数普通");
            svf.VrsOutn("ITEM4", 2, "欠席者数スポーツ");
            svf.VrsOutn("ITEM6", 2, "受検者数合計");
            svf.VrsOutn("ITEM_NUM1", 2, rs.getString("FUTSU_JUKEN"));
            svf.VrsOutn("ITEM_NUM2", 2, rs.getString("SPORT_JUKEN"));
            svf.VrsOutn("ITEM_NUM3", 2, rs.getString("FUTSU_KESSEKI"));
            svf.VrsOutn("ITEM_NUM4", 2, rs.getString("SPORT_KESSEKI"));
            svf.VrsOutn("ITEM_NUM6", 2, rs.getString("JUKEN"));

            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlIppanExamHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_SIGAN, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS JUKEN, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_JUKEN, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.JUDGEMENT,'0') <> '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_JUKEN, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '" + L013KESSEKI + "' AND B1.TESTDIV = '" + L004IPPAN + "' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_KESSEKI ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");

        return stb.toString();
    }

    private void printFutsuHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv, final String course) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL339Q_4_1.frm", 1);

            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            svf.VrsOut("TITLE", "基準試験" + _param._orderName + "順一覧");
            svf.VrsOut("CLASS_NAME1", getCourseMstKeyEndsWith(_param._courseMap, course)._examcourseName);
            final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(L004IPPAN))._spare1, "yyyy年MM月dd日");
            final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(L004IPPAN))._spare1);
            svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");

            final String sql = sqlFutsuHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            svf.VrsOutn("CLASS_NAME2", 1, getCourseMstKeyEndsWith(_param._courseMap, COURSE_FUTSU)._examcourseName);
            svf.VrsOutn("ITEM1", 1, "校長推薦入試");
            svf.VrsOutn("ITEM2", 1, "自己推薦入試");
            svf.VrsOutn("ITEM3", 1, "駿台中学生");
            svf.VrsOutn("ITEM5", 1, "合計");
            svf.VrsOutn("ITEM6", 1, "試験欠席者");
            svf.VrsOutn("ITEM_NUM1", 1, rs.getString("FUTSU_KOUTYOU_SIGAN"));
            svf.VrsOutn("ITEM_NUM2", 1, rs.getString("FUTSU_JIKO_SIGAN"));
            svf.VrsOutn("ITEM_NUM3", 1, rs.getString("FUTSU_SUNTYU_SIGAN"));
            svf.VrsOutn("ITEM_NUM5", 1, rs.getString("FUTSU_SIGAN"));
            svf.VrsOutn("ITEM_NUM6", 1, rs.getString("FUTSU_KESSEKI"));

            svf.VrsOutn("CLASS_NAME2", 2, getCourseMstKeyEndsWith(_param._courseMap, COURSE_SPORT)._examcourseName);
            svf.VrsOutn("ITEM1", 2, "校長推薦入試");
            svf.VrsOutn("ITEM2", 2, "自己推薦入試");
            svf.VrsOutn("ITEM5", 2, "合計");
            svf.VrsOutn("ITEM6", 2, "試験欠席者");
            svf.VrsOutn("ITEM_NUM1", 2, rs.getString("SPORT_KOUTYOU_SIGAN"));
            svf.VrsOutn("ITEM_NUM2", 2, rs.getString("SPORT_JIKO_SIGAN"));
            svf.VrsOutn("ITEM_NUM5", 2, rs.getString("SPORT_SIGAN"));
            svf.VrsOutn("ITEM_NUM6", 2, rs.getString("SPORT_KESSEKI"));

            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlFutsuHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' AND B1.TESTDIV = '" + L004KOUTYOU + "' THEN 1 ELSE 0 END) AS FUTSU_KOUTYOU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' AND B1.TESTDIV = '" + L004JIKO + "' THEN 1 ELSE 0 END) AS FUTSU_JIKO_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' AND B1.TESTDIV = '" + L004SUNTYU + "' THEN 1 ELSE 0 END) AS FUTSU_SUNTYU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS FUTSU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' AND B1.JUDGEMENT =  '" + L013KESSEKI + "' THEN 1 ELSE 0 END) AS FUTSU_KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' AND B1.TESTDIV = '" + L004KOUTYOU + "' THEN 1 ELSE 0 END) AS SPORT_KOUTYOU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' AND B1.TESTDIV = '" + L004JIKO + "' THEN 1 ELSE 0 END) AS SPORT_JIKO_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS SPORT_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' AND B1.JUDGEMENT =  '" + L013KESSEKI + "' THEN 1 ELSE 0 END) AS SPORT_KESSEKI ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND ( ");
        stb.append("         B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
        stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
        stb.append("     ) ");

        return stb.toString();
    }

    private void printSuntyuHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        svf.VrSetForm("KNJL339Q_5_1.frm", 1);
        svf.VrsOut("NENDO", _param._entexamyear + "年度");
        svf.VrsOut("TITLE", "基準試験" + _param._orderName + "順一覧");
        svf.VrsOut("CLASS_NAME", ((NameMst) _param._l004Map.get(L004SUNTYU))._name1);
        final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(L004SUNTYU))._spare1, "yyyy年MM月dd日");
        final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(L004SUNTYU))._spare1);
        svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");

        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrEndPage();
    }

    private void printIppanSportHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL339Q_6.frm", 1);

            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            svf.VrsOut("TITLE", "基準・入試" + _param._orderName + "順一覧");
            svf.VrsOut("CLASS_NAME1", getCourseMstKeyEndsWith(_param._courseMap, COURSE_SPORT)._examcourseName);
            svf.VrsOut("CLASS_NAME2", getCourseMstKeyEndsWith(_param._courseMap, COURSE_SPORT)._examcourseName);
            final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(L004IPPAN))._spare1, "yyyy年MM月dd日");
            final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(L004IPPAN))._spare1);
            svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");

            final String sql = sqlIppanSportHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            svf.VrsOut("ITEM1", "一般入試");
            svf.VrsOut("ITEM2", "校長推薦入試");
            svf.VrsOut("ITEM3", "自己推薦入試");
            svf.VrsOut("ITEM4", "合計");
            svf.VrsOut("ITEM5", "試験欠席者");
            svf.VrsOut("ITEM_NUM1", rs.getString("IPPAN_SIGAN"));
            svf.VrsOut("ITEM_NUM2", rs.getString("KOUTYOU_SIGAN"));
            svf.VrsOut("ITEM_NUM3", rs.getString("JIKO_SIGAN"));
            svf.VrsOut("ITEM_NUM4", rs.getString("SIGAN"));
            svf.VrsOut("ITEM_NUM5", rs.getString("KESSEKI"));

            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlIppanSportHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS IPPAN_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004KOUTYOU + "' THEN 1 ELSE 0 END) AS KOUTYOU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004JIKO + "' THEN 1 ELSE 0 END) AS JIKO_SIGAN, ");
        stb.append("     COUNT(B1.EXAMNO) AS SIGAN, ");
        stb.append("     COUNT(CASE WHEN B1.JUDGEMENT =  '" + L013KESSEKI + "' THEN 1 END) AS KESSEKI ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND ( ");
        stb.append("         B1.TESTDIV0 = '" + L045IPPAN + "' ");
        stb.append("      OR B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
        stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
        stb.append("     ) ");
        stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' ");

        return stb.toString();
    }

    private void printIppanFutsuHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL339Q_7.frm", 1);

            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            svf.VrsOut("TITLE", "基準・入試" + _param._orderName + "順一覧");
            svf.VrsOut("CLASS_NAME1", getCourseMstKeyEndsWith(_param._courseMap, COURSE_FUTSU)._examcourseName);
            svf.VrsOut("CLASS_NAME2", getCourseMstKeyEndsWith(_param._courseMap, COURSE_FUTSU)._examcourseName);
            final String examDate = KNJ_EditDate.h_format_S(((NameMst) _param._l004Map.get(L004IPPAN))._spare1, "yyyy年MM月dd日");
            final String examWeek = KNJ_EditDate.h_format_W(((NameMst) _param._l004Map.get(L004IPPAN))._spare1);
            svf.VrsOut("EXAM_DATE", examDate + "（" + examWeek + "）実施");

            final String sql = sqlIppanFutsuHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            svf.VrsOut("ITEM1", "一般入試");
            svf.VrsOut("ITEM2", "校長推薦入試");
            svf.VrsOut("ITEM3", "自己推薦入試");
            svf.VrsOut("ITEM4", "駿台中学生");
            svf.VrsOut("ITEM5", "合計");
            svf.VrsOut("ITEM6", "試験欠席者");
            svf.VrsOut("ITEM_NUM1", rs.getString("IPPAN_SIGAN"));
            svf.VrsOut("ITEM_NUM2", rs.getString("KOUTYOU_SIGAN"));
            svf.VrsOut("ITEM_NUM3", rs.getString("JIKO_SIGAN"));
            svf.VrsOut("ITEM_NUM4", rs.getString("SUNTYU_SIGAN"));
            svf.VrsOut("ITEM_NUM5", rs.getString("SIGAN"));
            svf.VrsOut("ITEM_NUM6", rs.getString("KESSEKI"));

            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlIppanFutsuHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004IPPAN + "' THEN 1 ELSE 0 END) AS IPPAN_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004KOUTYOU + "' THEN 1 ELSE 0 END) AS KOUTYOU_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004JIKO + "' THEN 1 ELSE 0 END) AS JIKO_SIGAN, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '" + L004SUNTYU + "' THEN 1 ELSE 0 END) AS SUNTYU_SIGAN, ");
        stb.append("     COUNT(B1.EXAMNO) AS SIGAN, ");
        stb.append("     COUNT(CASE WHEN B1.JUDGEMENT =  '" + L013KESSEKI + "' THEN 1 END) AS KESSEKI ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND ( ");
        stb.append("         B1.TESTDIV0 = '" + L045IPPAN + "' ");
        stb.append("      OR B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
        stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
        stb.append("     ) ");
        stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' ");

        return stb.toString();
    }

    private void print339_2_2Form(final DB2UDB db2, final Vrw32alp svf, final String printDiv, final String course) {

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            svf.VrSetForm("KNJL339Q_2_2.frm", 4);

            final String sqlCnt = sqlIppan(printDiv, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 20;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlIppan(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                final String setTestName = PRINT_IPPAN.equals(printDiv) ? "入学" : "基準";
                final String setCourseName = "(" + getCourseMstKeyEndsWith(_param._courseMap, course)._examcourseName + ")";
                svf.VrsOut("TITLE", _param._entexamyear + "年度　" + setTestName + "試験　" + _param._orderName + "順一覧" + setCourseName);
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                svf.VrsOut("CLASS_NAME1", ((NameMst) _param._l009Map.get(EIGO))._abbv1);
                svf.VrsOut("CLASS_NAME2", ((NameMst) _param._l009Map.get(SUUGAKU))._abbv1);
                svf.VrsOut("CLASS_NAME3", ((NameMst) _param._l009Map.get(KOKUGO))._abbv1);
                svf.VrsOut("CLASS_NAME4", "");
                svf.VrsOut("CLASS_NAME5", "");
                svf.VrsOut("CLASS_NAME6", "合計");
                svf.VrsOut("CLASS_NAME7", "面接");
                svf.VrsOut("CLASS_NAME8", "偏差");
                svf.VrsOut("CLASS_NAME9", "確約");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

                final String judgementName1 = rs.getString("JUDGEMENT_NAME1");
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String prefName = rs.getString("PREF_NAME");
                final String eigoKijun = rs.getString("EIGO_KIJUN");
                final String suugakuKijun = rs.getString("SUUGAKU_KIJUN");
                final String kokugoKijun = rs.getString("KOKUGO_KIJUN");
                final String totalKijun = rs.getString("TOTAL_KIJUN");
                final String deviationKijun = rs.getString("DEVIATION_KIJUN");
                final String interview = rs.getString("INTERVIEW");
                final String kakuyakuFlg = rs.getString("KAKUYAKU_FLG");
                final String publicValue = rs.getString("PUBLIC_VALUE");
                final String publicScore1 = rs.getString("PUBLIC_SCORE1");
                final String publicScore2 = rs.getString("PUBLIC_SCORE2");
                final String satNo = rs.getString("SAT_NO");
                final String satTotal = rs.getString("SAT_TOTAL");
                final String satJudge = rs.getString("SAT_JUDGE");
                final String mockAugDev = rs.getString("MOCK_AUG_DEV");
                final String mockSepDev = rs.getString("MOCK_SEP_DEV");
                final String confAvg3_5 = rs.getString("CONF_AVG3_5");
                final String confAvg3_9 = rs.getString("CONF_AVG3_9");
                final String confAbsence1 = rs.getString("CONF_ABSENCE1");
                final String confAbsence2 = rs.getString("CONF_ABSENCE2");
                final String confAbsence3 = rs.getString("CONF_ABSENCE3");
                final String confTokki = rs.getString("CONF_TOKKI");
                final String scholarKibou = rs.getString("SCHOLAR_KIBOU");
                final String scholarSaiyou = rs.getString("SCHOLAR_SAIYOU");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");
                final String shSchoolName1 = rs.getString("SH_SCHOOL_NAME1");
                final String shSchoolName2 = rs.getString("SH_SCHOOL_NAME2");
                final String shSchoolName3 = rs.getString("SH_SCHOOL_NAME3");
                final String shSchoolName4 = rs.getString("SH_SCHOOL_NAME4");

                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("JUDGE", judgementName1);
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("CLASS", "");
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sexAbbv1);
                svf.VrsOut("PAST", "");
                svf.VrsOut("JHSCHOOL_NAME", finschoolName);
                svf.VrsOut("PREF_NAME", prefName);
                svf.VrsOut("SCORE1", eigoKijun);
                svf.VrsOut("SCORE2", suugakuKijun);
                svf.VrsOut("SCORE3", kokugoKijun);
                svf.VrsOut("SCORE4", "");
                svf.VrsOut("SCORE5", "");
                svf.VrsOut("SCORE6", totalKijun);
                svf.VrsOut("SCORE7", interview);
                svf.VrsOut("SCORE8", deviationKijun);
                svf.VrsOut("SCORE9", kakuyakuFlg);
                svf.VrsOut("VISIT3", publicValue);
                svf.VrsOut("VISIT1", publicScore1);
                svf.VrsOut("VISIT2", publicScore2);
                svf.VrsOut("VISIT4", "");
                svf.VrsOut("SAT3", satNo);
                svf.VrsOut("SAT1", satTotal);
                svf.VrsOut("SAT2", satJudge);
                svf.VrsOut("MOCK1", mockAugDev);
                svf.VrsOut("MOCK2", mockSepDev);
                svf.VrsOut("REPORT1", confAvg3_5);
                svf.VrsOut("REPORT2", confAvg3_9);
                svf.VrsOut("NOTICE1", confAbsence1);
                svf.VrsOut("NOTICE2", confAbsence2);
                svf.VrsOut("NOTICE3", confAbsence3);
                final String[] tokki = KNJ_EditEdit.get_token(confTokki, 40, 4);
                printBunkatsu(svf, tokki, "NOTE1_");
                svf.VrsOut("HOPE1", shSchoolName1);
                svf.VrsOut("HOPE2", shSchoolName2);
                svf.VrsOut("HOPE3", shSchoolName3);
                svf.VrsOut("HOPE4", shSchoolName4);
                svf.VrsOut("SCHOLAR1", scholarKibou);
                svf.VrsOut("SCHOLAR2", scholarSaiyou + " " + scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);

                svf.VrEndRecord();
                lineNo++;
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void print339_4_2Form(final DB2UDB db2, final Vrw32alp svf, final String printDiv, final String course) {

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            svf.VrSetForm("KNJL339Q_4_2.frm", 4);

            final String sqlCnt = sqlIppan(printDiv, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 20;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlIppan(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                final String setTestName = PRINT_FUTSU.equals(printDiv) ? "基準試験" : "基準・入試";
                final String setCourseName = "(" + getCourseMstKeyEndsWith(_param._courseMap, course)._examcourseName + ")";
                svf.VrsOut("TITLE", _param._entexamyear + "年度　" +  setTestName + "　" + _param._orderName + "順一覧" + setCourseName);
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                svf.VrsOut("STANDARD_NAME1", ((NameMst) _param._l009Map.get(EIGO))._abbv1);
                svf.VrsOut("STANDARD_NAME2", ((NameMst) _param._l009Map.get(SUUGAKU))._abbv1);
                svf.VrsOut("STANDARD_NAME3", ((NameMst) _param._l009Map.get(KOKUGO))._abbv1);
                svf.VrsOut("STANDARD_NAME4", "合計");
                svf.VrsOut("STANDARD_NAME5", "偏差");

                svf.VrsOut("CLASS_NAME1", ((NameMst) _param._l009Map.get(EIGO))._abbv1);
                svf.VrsOut("CLASS_NAME2", ((NameMst) _param._l009Map.get(SUUGAKU))._abbv1);
                svf.VrsOut("CLASS_NAME3", ((NameMst) _param._l009Map.get(KOKUGO))._abbv1);
                svf.VrsOut("CLASS_NAME4", ((NameMst) _param._l009Map.get(RIKA))._abbv1);
                svf.VrsOut("CLASS_NAME5", ((NameMst) _param._l009Map.get(SHOURON))._abbv1);
                svf.VrsOut("CLASS_NAME6", "計");
                svf.VrsOut("CLASS_NAME7", "順");
                svf.VrsOut("CLASS_NAME8", "面");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

                final String ranKijun = rs.getString("RANK_KIJUN");
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String prefName = rs.getString("PREF_NAME");
                final String eigoKijun = rs.getString("EIGO_KIJUN");
                final String suugakuKijun = rs.getString("SUUGAKU_KIJUN");
                final String kokugoKijun = rs.getString("KOKUGO_KIJUN");
                final String totalKijun = rs.getString("TOTAL_KIJUN");
                final String deviationKijun = rs.getString("DEVIATION_KIJUN");
                final String eigo = rs.getString("EIGO");
                final String suugaku = rs.getString("SUUGAKU");
                final String kokugo = rs.getString("KOKUGO");
                final String rika = rs.getString("RIKA");
                final String shouron = rs.getString("SHOURON");
                final String total = rs.getString("TOTAL");
                final String rank = rs.getString("RANK");
                final String interview = rs.getString("INTERVIEW");
                final String publicValue = rs.getString("PUBLIC_VALUE");
                final String publicScore1 = rs.getString("PUBLIC_SCORE1");
                final String publicScore2 = rs.getString("PUBLIC_SCORE2");
                final String satNo = rs.getString("SAT_NO");
                final String satTotal = rs.getString("SAT_TOTAL");
                final String satJudge = rs.getString("SAT_JUDGE");
                final String mockAugDev = rs.getString("MOCK_AUG_DEV");
                final String mockSepDev = rs.getString("MOCK_SEP_DEV");
                final String confAvg3_5 = rs.getString("CONF_AVG3_5");
                final String confAvg3_9 = rs.getString("CONF_AVG3_9");
                final String confAbsence1 = rs.getString("CONF_ABSENCE1");
                final String confAbsence2 = rs.getString("CONF_ABSENCE2");
                final String confAbsence3 = rs.getString("CONF_ABSENCE3");
                final String confTokki = rs.getString("CONF_TOKKI");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");

                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("RANK", ranKijun);
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sexAbbv1);
                svf.VrsOut("JHSCHOOL_NAME", finschoolName);
                svf.VrsOut("PREF_NAME", prefName);
                svf.VrsOut("STANDARD_SCORE1", eigoKijun);
                svf.VrsOut("STANDARD_SCORE2", suugakuKijun);
                svf.VrsOut("STANDARD_SCORE3", kokugoKijun);
                svf.VrsOut("STANDARD_SCORE4", totalKijun);
                svf.VrsOut("STANDARD_SCORE5", deviationKijun);
                svf.VrsOut("SCORE1", eigo);
                svf.VrsOut("SCORE2", suugaku);
                svf.VrsOut("SCORE3", kokugo);
                svf.VrsOut("SCORE4", rika);
                svf.VrsOut("SCORE5", shouron);
                svf.VrsOut("SCORE6", total);
                svf.VrsOut("SCORE7", rank);
                svf.VrsOut("SCORE8", interview);
                svf.VrsOut("VISIT3", publicValue);
                svf.VrsOut("VISIT1", publicScore1);
                svf.VrsOut("VISIT2", publicScore2);
                svf.VrsOut("VISIT4", "");
                svf.VrsOut("SAT3", satNo);
                svf.VrsOut("SAT1", satTotal);
                svf.VrsOut("SAT2", satJudge);
                svf.VrsOut("MOCK1", mockAugDev);
                svf.VrsOut("MOCK2", mockSepDev);
                svf.VrsOut("REPORT1", confAvg3_5);
                svf.VrsOut("REPORT2", confAvg3_9);
                svf.VrsOut("NOTICE1", confAbsence1);
                svf.VrsOut("NOTICE2", confAbsence2);
                svf.VrsOut("NOTICE3", confAbsence3);
                final String[] tokki = KNJ_EditEdit.get_token(confTokki, 40, 4);
                printBunkatsu(svf, tokki, "NOTE1_");
                svf.VrsOut("SCHOLAR2", scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);

                svf.VrEndRecord();
                lineNo++;
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void print339_5_2Form(final DB2UDB db2, final Vrw32alp svf, final String printDiv, final String course) {

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            svf.VrSetForm("KNJL339Q_5_2.frm", 4);

            final String sqlCnt = sqlIppan(printDiv, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 20;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlIppan(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                final String setTestName = PRINT_SPORT.equals(printDiv) ? "基準試験" : "基準・入試";
                final String setCourseName = "(" + getCourseMstKeyEndsWith(_param._courseMap, course)._examcourseName + ")";
                svf.VrsOut("TITLE", _param._entexamyear + "年度　" + setTestName + "　" + _param._orderName + "順一覧" + setCourseName);
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                svf.VrsOut("STANDARD_NAME1", ((NameMst) _param._l009Map.get(EIGO))._abbv1);
                svf.VrsOut("STANDARD_NAME2", ((NameMst) _param._l009Map.get(SUUGAKU))._abbv1);
                svf.VrsOut("STANDARD_NAME3", ((NameMst) _param._l009Map.get(KOKUGO))._abbv1);
                svf.VrsOut("STANDARD_NAME4", "合計");
                svf.VrsOut("STANDARD_NAME5", "偏差");

                svf.VrsOut("CLASS_NAME1", ((NameMst) _param._l009Map.get(EIGO))._abbv1);
                svf.VrsOut("CLASS_NAME2", ((NameMst) _param._l009Map.get(SUUGAKU))._abbv1);
                svf.VrsOut("CLASS_NAME3", ((NameMst) _param._l009Map.get(KOKUGO))._abbv1);
                svf.VrsOut("CLASS_NAME4", ((NameMst) _param._l009Map.get(RIKA))._abbv1);
                svf.VrsOut("CLASS_NAME5", ((NameMst) _param._l009Map.get(SHOURON))._abbv1);
                svf.VrsOut("CLASS_NAME6", "計");
                svf.VrsOut("CLASS_NAME7", "順");
                svf.VrsOut("CLASS_NAME8", "面");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

                final String judgementName1 = rs.getString("JUDGEMENT_NAME1");
                final String ranKijun = rs.getString("RANK_KIJUN");
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String prefName = rs.getString("PREF_NAME");
                final String eigoKijun = rs.getString("EIGO_KIJUN");
                final String suugakuKijun = rs.getString("SUUGAKU_KIJUN");
                final String kokugoKijun = rs.getString("KOKUGO_KIJUN");
                final String totalKijun = rs.getString("TOTAL_KIJUN");
                final String deviationKijun = rs.getString("DEVIATION_KIJUN");
                final String eigo = rs.getString("EIGO");
                final String suugaku = rs.getString("SUUGAKU");
                final String kokugo = rs.getString("KOKUGO");
                final String rika = rs.getString("RIKA");
                final String shouron = rs.getString("SHOURON");
                final String total = rs.getString("TOTAL");
                final String rank = rs.getString("RANK");
                final String interview = rs.getString("INTERVIEW");
                final String publicValue = rs.getString("PUBLIC_VALUE");
                final String publicScore1 = rs.getString("PUBLIC_SCORE1");
                final String publicScore2 = rs.getString("PUBLIC_SCORE2");
                final String satNo = rs.getString("SAT_NO");
                final String satTotal = rs.getString("SAT_TOTAL");
                final String satJudge = rs.getString("SAT_JUDGE");
                final String clubName = rs.getString("CLUB_NAME");
                final String confAvg3_5 = rs.getString("CONF_AVG3_5");
                final String confAvg3_9 = rs.getString("CONF_AVG3_9");
                final String confAbsence1 = rs.getString("CONF_ABSENCE1");
                final String confAbsence2 = rs.getString("CONF_ABSENCE2");
                final String confAbsence3 = rs.getString("CONF_ABSENCE3");
                final String confTokki = rs.getString("CONF_TOKKI");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");

                if (PRINT_SPORT.equals(printDiv)) {
                    svf.VrsOut("NO", String.valueOf(lineNo));
                } else {
                    svf.VrsOut("NO2", String.valueOf(lineNo));
                }
                svf.VrsOut("JUDGE", judgementName1);
                svf.VrsOut("RANK", ranKijun);
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sexAbbv1);
                svf.VrsOut("JHSCHOOL_NAME", finschoolName);
                svf.VrsOut("PREF_NAME", prefName);
                svf.VrsOut("STANDARD_SCORE1", eigoKijun);
                svf.VrsOut("STANDARD_SCORE2", suugakuKijun);
                svf.VrsOut("STANDARD_SCORE3", kokugoKijun);
                svf.VrsOut("STANDARD_SCORE4", totalKijun);
                svf.VrsOut("STANDARD_SCORE5", deviationKijun);
                svf.VrsOut("SCORE1", eigo);
                svf.VrsOut("SCORE2", suugaku);
                svf.VrsOut("SCORE3", kokugo);
                svf.VrsOut("SCORE4", rika);
                svf.VrsOut("SCORE5", shouron);
                svf.VrsOut("SCORE6", total);
                svf.VrsOut("SCORE7", rank);
                svf.VrsOut("SCORE8", interview);
                svf.VrsOut("VISIT3", publicValue);
                svf.VrsOut("VISIT1", publicScore1);
                svf.VrsOut("VISIT2", publicScore2);
                svf.VrsOut("VISIT4", "");
                svf.VrsOut("SAT3", satNo);
                svf.VrsOut("SAT1", satTotal);
                svf.VrsOut("SAT2", satJudge);
                svf.VrsOut("CLUB", clubName);
                svf.VrsOut("CLUB2", "");
                svf.VrsOut("REPORT1", confAvg3_5);
                svf.VrsOut("REPORT2", confAvg3_9);
                svf.VrsOut("NOTICE1", confAbsence1);
                svf.VrsOut("NOTICE2", confAbsence2);
                svf.VrsOut("NOTICE3", confAbsence3);

                final String[] tokki = KNJ_EditEdit.get_token(confTokki, 40, 4);
                printBunkatsu(svf, tokki, "NOTE1_");
                svf.VrsOut("SCHOLAR2", scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);

                svf.VrEndRecord();
                lineNo++;
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlIppan(final String printDiv, final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         R1.TESTDIV, ");
        stb.append("         R1.EXAMNO, ");
        stb.append("         R1.TOTAL_RANK4 AS RANK, ");
        stb.append("         R1.TOTAL4 AS TOTAL, ");
        stb.append("         R1.JUDGE_DEVIATION AS DEVIATION, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '1' THEN S1.SCORE END) AS KOKUGO, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '2' THEN S1.SCORE END) AS SUUGAKU, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '3' THEN S1.SCORE END) AS RIKA, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '5' THEN S1.SCORE END) AS EIGO, ");
        stb.append("         SUM(CASE WHEN S1.TESTSUBCLASSCD = '6' THEN S1.SCORE END) AS SHOURON ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECEPT_DAT R1 ");
        stb.append("         LEFT JOIN ENTEXAM_SCORE_DAT S1 ON S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("                 AND S1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("                 AND S1.TESTDIV = R1.TESTDIV ");
        stb.append("                 AND S1.RECEPTNO = R1.RECEPTNO ");
        stb.append("     WHERE ");
        stb.append("             R1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND R1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     GROUP BY ");
        stb.append("         R1.TESTDIV, ");
        stb.append("         R1.EXAMNO, ");
        stb.append("         R1.TOTAL_RANK4, ");
        stb.append("         R1.TOTAL4, ");
        stb.append("         R1.JUDGE_DEVIATION ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     B1.DAI1_COURSECODE, ");
            stb.append("     N2.NAME1 AS JUDGEMENT_NAME1, ");
            stb.append("     S2.RANK AS RANK_KIJUN, ");
            stb.append("     B1.EXAMNO, ");
            stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
            stb.append("     B1.NAME, ");
            stb.append("     N3.ABBV1 AS SEX_ABBV1, ");
            stb.append("     F1.FINSCHOOL_NAME, ");
            stb.append("     PREF.PREF_NAME, ");
            stb.append("     S2.EIGO AS EIGO_KIJUN, ");
            stb.append("     S2.SUUGAKU AS SUUGAKU_KIJUN, ");
            stb.append("     S2.KOKUGO AS KOKUGO_KIJUN, ");
            stb.append("     S2.TOTAL AS TOTAL_KIJUN, ");
            stb.append("     S2.DEVIATION AS DEVIATION_KIJUN, ");
            stb.append("     S1.EIGO, ");
            stb.append("     S1.SUUGAKU, ");
            stb.append("     S1.KOKUGO, ");
            stb.append("     S1.RIKA, ");
            stb.append("     S1.SHOURON, ");
            stb.append("     S1.TOTAL, ");
            stb.append("     S1.RANK, ");
            stb.append("     N5.NAME1 AS INTERVIEW, ");
            stb.append("     CASE WHEN PUB.KAKUYAKU_FLG = '1' THEN 'y' END AS KAKUYAKU_FLG, ");
            stb.append("     PUB.VALUE AS PUBLIC_VALUE, ");
            stb.append("     PUB.SCORE1 AS PUBLIC_SCORE1, ");
            stb.append("     PUB.SCORE2 AS PUBLIC_SCORE2, ");
            stb.append("     SAT_E.SAT_NO, ");
            stb.append("     SAT_E.SCORE_TOTAL AS SAT_TOTAL, ");
            stb.append("     N4.NAME1 AS SAT_JUDGE, ");
            stb.append("     SAT_A.MOCK_AUG_DEV, ");
            stb.append("     SAT_A.MOCK_SEP_DEV, ");
            stb.append("     N7.NAME1 AS CLUB_NAME, ");
            stb.append("     CONF_D.REMARK11 AS CONF_AVG2_5, ");
            stb.append("     CONF_D.REMARK12 AS CONF_AVG2_9, ");
            stb.append("     CONF.AVERAGE5 AS CONF_AVG3_5, ");
            stb.append("     CONF.AVERAGE_ALL AS CONF_AVG3_9, ");
            stb.append("     CONF.ABSENCE_DAYS AS CONF_ABSENCE1, ");
            stb.append("     CONF.ABSENCE_DAYS2 AS CONF_ABSENCE2, ");
            stb.append("     CONF.ABSENCE_DAYS3 AS CONF_ABSENCE3, ");
            stb.append("     CONF.REMARK1 AS CONF_TOKKI, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ");
            stb.append("          ELSE '無' ");
            stb.append("     END AS SCHOLAR_KIBOU, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU = '1' THEN '採用' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU IS NULL THEN '不採用' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCHOLAR_SAIYOU, ");
            stb.append("     VALUE(B1.SCHOLAR_TOUKYU_SENGAN, '') AS SCHOLAR_TOUKYU, ");
            stb.append("     CASE WHEN B1.DORMITORY_FLG = '1' THEN 'レ' END AS DORMITORY_KIBOU, ");
            stb.append("     CONF_SEQ003.REMARK1 AS AO_KATUDOU1, ");
            stb.append("     CONF_SEQ003.REMARK2 AS AO_KATUDOU2, ");
            stb.append("     CONF_SEQ003.REMARK9 AS AO_TOKKI, ");
            stb.append("     C1.EXAMCOURSE_ABBV, ");
            stb.append("     CASE WHEN B1.FS_GRDDIV = '2' THEN N6.NAME2 END AS ROUNIN, ");
            stb.append("     SH1.FINSCHOOL_NAME AS SH_SCHOOL_NAME1, ");
            stb.append("     SH2.FINSCHOOL_NAME AS SH_SCHOOL_NAME2, ");
            stb.append("     SH3.FINSCHOOL_NAME AS SH_SCHOOL_NAME3, ");
            stb.append("     SH4.FINSCHOOL_NAME AS SH_SCHOOL_NAME4 ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH1 ON SH1.FINSCHOOLCD = B1.SH_SCHOOLCD1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH2 ON SH2.FINSCHOOLCD = B1.SH_SCHOOLCD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH3 ON SH3.FINSCHOOLCD = B1.SH_SCHOOLCD3 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH4 ON SH4.FINSCHOOLCD = B1.SH_SCHOOLCD4 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     LEFT JOIN PREF_MST PREF ON PREF.PREF_CD = F1.FINSCHOOL_PREF_CD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append("     LEFT JOIN ENTEXAM_PUBLIC_TEST_DAT PUB ON PUB.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND PUB.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND PUB.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND CONF.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND CONF.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_D ON CONF_D.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND CONF_D.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND CONF_D.EXAMNO = B1.EXAMNO ");
        stb.append("             AND CONF_D.SEQ = '002' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_SEQ003 ON CONF_SEQ003.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND CONF_SEQ003.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND CONF_SEQ003.EXAMNO = B1.EXAMNO ");
        stb.append("             AND CONF_SEQ003.SEQ = '003' ");
        stb.append("     LEFT JOIN SAT_EXAM_DAT SAT_E ON SAT_E.YEAR = '" + _param._loginYear + "' ");
        stb.append("             AND SAT_E.SAT_NO = B1.JIZEN_BANGOU ");
        stb.append("     LEFT JOIN SAT_APP_FORM_MST SAT_A ON SAT_A.YEAR = '" + _param._loginYear + "' ");
        stb.append("             AND SAT_A.SAT_NO = B1.JIZEN_BANGOU ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND INTV.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND INTV.TESTDIV = B1.TESTDIV ");
        stb.append("             AND INTV.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN T_SCORE S1 ON S1.EXAMNO = B1.EXAMNO AND S1.TESTDIV = B1.TESTDIV AND S1.TESTDIV <> '5' ");
        stb.append("     LEFT JOIN T_SCORE S2 ON S2.EXAMNO = B1.EXAMNO AND S2.TESTDIV = '5' ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L013' AND N2.NAMECD2 = B1.JUDGEMENT ");
        stb.append("     LEFT JOIN V_NAME_MST N3 ON N3.YEAR = B1.ENTEXAMYEAR AND N3.NAMECD1 = 'Z002' AND N3.NAMECD2 = B1.SEX ");
        stb.append("     LEFT JOIN V_NAME_MST N4 ON N4.YEAR = SAT_E.YEAR AND N4.NAMECD1 = 'L200' AND N4.NAMECD2 = SAT_E.JUDGE_SAT ");
        stb.append("     LEFT JOIN V_NAME_MST N5 ON N5.YEAR = B1.ENTEXAMYEAR AND N5.NAMECD1 = 'L027' AND N5.NAMECD2 = INTV.INTERVIEW_VALUE ");
        stb.append("     LEFT JOIN V_NAME_MST N6 ON N6.YEAR = B1.ENTEXAMYEAR AND N6.NAMECD1 = 'L016' AND N6.NAMECD2 = B1.FS_GRDDIV ");
        stb.append("     LEFT JOIN V_NAME_MST N7 ON N7.YEAR = B1.ENTEXAMYEAR AND N7.NAMECD1 = 'L037' AND N7.NAMECD2 = B1.CLUB_CD ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (PRINT_IPPAN.equals(printDiv)) {
            stb.append("     AND B1.TESTDIV0 = '" + L045IPPAN + "' ");
        }
        if (PRINT_FUTSU.equals(printDiv)) {
            stb.append("     AND ( ");
            stb.append("         B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
            stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
            stb.append("     ) ");
            stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' ");
        }
        if (PRINT_SPORT.equals(printDiv)) {
            stb.append("     AND ( ");
            stb.append("         B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
            stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
            stb.append("     ) ");
            stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' ");
        }
        if (PRINT_SUNTYU.equals(printDiv)) {
            stb.append("     AND B1.TESTDIV0 = '" + L045SUNTYU + "' ");
        }
        if (PRINT_IPPAN_SPORT.equals(printDiv)) {
            stb.append("     AND ( ");
            stb.append("         B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
            stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
            stb.append("      OR B1.TESTDIV0 = '" + L045IPPAN + "' ");
            stb.append("     ) ");
            stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' ");
        }
        if (PRINT_IPPAN_FUTSU.equals(printDiv)) {
            stb.append("     AND ( ");
            stb.append("         B1.TESTDIV0 = '" + L045SUISEN + "' AND B1.GENERAL_FLG = '1' ");
            stb.append("      OR B1.TESTDIV0 = '" + L045SUNTYU + "' ");
            stb.append("      OR B1.TESTDIV0 = '" + L045IPPAN + "' ");
            stb.append("     ) ");
            stb.append("     AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' ");
        }
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            if ("1".equals(_param._order)) {
                stb.append("     VALUE(S2.TOTAL,-1) DESC, ");
                stb.append("     B1.EXAMNO ");
            } else {
                stb.append("     B1.EXAMNO ");
            }
        }
        return stb.toString();
    }

    private void printBunkatsu(final Vrw32alp svf, final String[] printData, final String fieldName) {
        if (null == printData) {
            return;
        }
        int setCnt = 1;
        for (int i = 0; i < printData.length; i++) {
            final String setStr = printData[i];
            svf.VrsOut(fieldName + setCnt, setStr);
            setCnt++;
        }
    }

    private class NameMst {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _name3;
        final String _abbv1;
        final String _abbv2;
        final String _abbv3;
        final String _spare1;
        final String _spare2;
        final String _spare3;
        public NameMst(
                final String namecd2,
                final String name1,
                final String name2,
                final String name3,
                final String abbv1,
                final String abbv2,
                final String abbv3,
                final String spare1,
                final String spare2,
                final String spare3
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _name3 = name3;
            _abbv1 = abbv1;
            _abbv2 = abbv2;
            _abbv3 = abbv3;
            _spare1 = spare1;
            _spare2 = spare2;
            _spare3 = spare3;
        }
    }

    private class CourseMst {
        final String _courseCd;
        final String _majorCd;
        final String _examCourseCd;
        final String _examcourseName;
        final String _examcourseAbbv;
        public CourseMst(
                final String courseCd,
                final String majorCd,
                final String examCourseCd,
                final String examcourseName,
                final String examcourseAbbv
        ) {
            _courseCd = courseCd;
            _majorCd = majorCd;
            _examCourseCd = examCourseCd;
            _examcourseName = examcourseName;
            _examcourseAbbv = examcourseAbbv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58524 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testdiv;
        final String _testdivName;
        final String _kocho;
        final String _jiko;
        final String _ippan;
        final String _normal;
        final String _sport;
        final String _sunchu;
        final String _sgsport;
        final String _sgnormal;
        final String _order;
        final String _orderName;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final Map _l004Map;
        final Map _l009Map;
        final Map _courseMap;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _kocho = request.getParameter("KOCHO");
            _jiko = request.getParameter("JIKO");
            _ippan = request.getParameter("IPPAN");
            _normal = request.getParameter("NORMAL");
            _sport = request.getParameter("SPORT");
            _sunchu = request.getParameter("SUNCHU");
            _sgsport = request.getParameter("SGSPORT");
            _sgnormal = request.getParameter("SGNORMAL");
            _order = request.getParameter("ORDER");
            _orderName = "1".equals(_order) ? "高得点" : "受験番号";
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testdivName = getTestDivName(db2);
            _l004Map = getNameMstMap(db2, "L004");
            _l009Map = getNameMstMap(db2, "L009");
            _courseMap = geCourseMap(db2);
            _schoolName = getSchoolName(db2);
        }

        private String getTestDivName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ABBV1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'L045' ");
            stb.append("     AND NAMECD2 = '" + _testdiv + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                rs.next();
                retStr = rs.getString("ABBV1");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private Map getNameMstMap(final DB2UDB db2, final String namecd2) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd2 + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final NameMst nameMst = new NameMst(rs.getString("NAMECD2"), rs.getString("NAME1"), rs.getString("NAME2"), rs.getString("NAME3"), rs.getString("ABBV1"), rs.getString("ABBV2"), rs.getString("ABBV3"), rs.getString("NAMESPARE1"), rs.getString("NAMESPARE2"), rs.getString("NAMESPARE3"));
                    retMap.put(rs.getString("NAMECD2"), nameMst);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map geCourseMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     C1.COURSECD, ");
            stb.append("     C1.MAJORCD, ");
            stb.append("     C1.EXAMCOURSECD, ");
            stb.append("     C1.EXAMCOURSE_NAME, ");
            stb.append("     C1.EXAMCOURSE_ABBV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST C1 ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = C1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = C1.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("     C1.ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND C1.APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("     AND N1.ABBV3 = '" + _testdiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     C1.EXAMCOURSECD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final CourseMst courseMst = new CourseMst(rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("EXAMCOURSECD"), rs.getString("EXAMCOURSE_NAME"), rs.getString("EXAMCOURSE_ABBV"));
                    retMap.put(rs.getString("EXAMCOURSECD"), courseMst);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getSchoolName(DB2UDB db2) throws SQLException {
            String retStr = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     C1.SCHOOLNAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST C1 ");
            stb.append("     INNER JOIN V_NAME_MST N1 ON N1.NAMECD1 = 'L003' ");
            stb.append("           AND N1.NAMECD2 = '" + _applicantdiv + "' ");
            stb.append("           AND C1.SCHOOL_KIND = N1.NAMESPARE3 ");
            stb.append(" WHERE ");
            stb.append("     C1.YEAR = '" + _loginYear + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                rs.next();
                retStr = rs.getString("SCHOOLNAME1");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof

