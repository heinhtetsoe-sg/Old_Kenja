/*
 * $Id: ccf69bc39c70f617fad411081592cd4b5f6ffcbb $
 *
 * 作成日: 2017/04/13
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

public class KNJL333Q {

    private static final Log log = LogFactory.getLog(KNJL333Q.class);

    private boolean _hasData;

    private final String PRINT_ALL = "1";
    private final String PRINT_PASS = "2";
    private final String PRINT_NAIPASS = "3";
    private final String PRINT_GAIPASS = "4";
    private final String PRINT_UNPASS = "5";
    private final String PRINT_KESSEKI = "6";
    private final String PRINT_SCHOLAR = "7";
    private final String PRINT_UNSCHOLAR = "8";
    private final String PRINT_SCHOLAR_HOPE = "9";
    private final String PRINT_NYUURYOU = "10";
    private final String PRINT_SCHOOL = "11";
    private final String PRINT_HASSOU_SCHOOL = "12";
    private final String PRINT_HASSOU_PASS = "13";

    private final String SUISEN = "2";
    private final String IPPAN = "3";

    private final String COURSE_FUTSU = "11";
    private final String COURSE_SPORT = "12";

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

            if ("1".equals(_param._all)) {
                printSagyouList(db2, svf, PRINT_ALL);
            }
            if ("1".equals(_param._gokaku)) {
                printSagyouList(db2, svf, PRINT_PASS);
            }
            if ("1".equals(_param._naiGokaku)) {
                printSagyouList(db2, svf, PRINT_NAIPASS);
            }
            if ("1".equals(_param._gaiGokaku)) {
                printSagyouList(db2, svf, PRINT_GAIPASS);
            }
            if ("1".equals(_param._fugokaku)) {
                printSagyouList(db2, svf, PRINT_UNPASS);
            }
            if ("1".equals(_param._kesseki)) {
                printSagyouList(db2, svf, PRINT_KESSEKI);
            }
            if ("1".equals(_param._sksaiyo)) {
                printSagyouList(db2, svf, PRINT_SCHOLAR);
            }
            if ("1".equals(_param._skfusaiyo)) {
                printSagyouList(db2, svf, PRINT_UNSCHOLAR);
            }
            if ("1".equals(_param._skkibo)) {
                printSagyouList(db2, svf, PRINT_SCHOLAR_HOPE);
            }
            if ("1".equals(_param._nyuryo)) {
                printSagyouList(db2, svf, PRINT_NYUURYOU);
            }
            if ("1".equals(_param._chugakuate)) {
                printSchool(db2, svf, PRINT_SCHOOL);
            }
            if ("1".equals(_param._order)) {
                printHassouSchool(db2, svf, PRINT_HASSOU_SCHOOL);
            }
            if ("2".equals(_param._order)) {
                printHassouPass(db2, svf, PRINT_HASSOU_PASS);
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

    private void printSagyouList(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {
        svf.VrSetForm("KNJL333Q_1.frm", 4);

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            final String sqlCnt = sqlSagyouList(printDiv, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 40;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlSagyouList(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrsOut("TITLE", _param._entexamyear + "年度　入試発送作業用リスト");
            svf.VrsOut("SUBTITLE", _param._testdivName + "《" + getSubTitle(printDiv) + "》");
            svf.VrsOut("APPLY", String.valueOf(totalCnt) + "件");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String judgementName1 = rs.getString("JUDGEMENT_NAME1");
                final String scholarKibou = rs.getString("SCHOLAR_KIBOU");
                final String scholarSaiyou = rs.getString("SCHOLAR_SAIYOU");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");

                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("NAME", name);
                svf.VrsOut("FINSCHOOL_NAME", finschoolName);
                svf.VrsOut("JUDGE", judgementName1);
                svf.VrsOut("SCHOLAR1", scholarKibou);
                svf.VrsOut("SCHOLAR2", scholarSaiyou + " " + scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);

                svf.VrEndRecord();
                lineCnt++;
                lineNo++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getSubTitle(final String printDiv) {
        if (PRINT_ALL.equals(printDiv)) {
            return "全員";
        }
        if (PRINT_PASS.equals(printDiv)) {
            return "合格";
        }
        if (PRINT_NAIPASS.equals(printDiv)) {
            return "県内合格";
        }
        if (PRINT_GAIPASS.equals(printDiv)) {
            return "県外合格";
        }
        if (PRINT_UNPASS.equals(printDiv)) {
            return "不合格";
        }
        if (PRINT_KESSEKI.equals(printDiv)) {
            return "欠席";
        }
        if (PRINT_SCHOLAR.equals(printDiv)) {
            return "スカラー採用";
        }
        if (PRINT_UNSCHOLAR.equals(printDiv)) {
            return "スカラー不採用";
        }
        if (PRINT_SCHOLAR_HOPE.equals(printDiv)) {
            return "スカラー希望者";
        }
        if (PRINT_NYUURYOU.equals(printDiv)) {
            return "入寮希望者";
        }
        if (PRINT_SCHOOL.equals(printDiv)) {
            return "中学校宛通知を送る中学校一覧";
        }

        return null;
    }

    private String sqlSagyouList(final String printDiv, final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     B1.EXAMNO, ");
            stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
            stb.append("     B1.NAME, ");
            stb.append("     F1.FINSCHOOL_NAME, ");
            stb.append("     N2.NAME1 AS JUDGEMENT_NAME1, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ");
            stb.append("          ELSE '無' ");
            stb.append("     END AS SCHOLAR_KIBOU, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU = '1' THEN '採用' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU IS NULL THEN '不採用' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCHOLAR_SAIYOU, ");
            stb.append("     VALUE(B1.SCHOLAR_TOUKYU_SENGAN, '') AS SCHOLAR_TOUKYU, ");
            stb.append("     CASE WHEN B1.DORMITORY_FLG = '1' THEN 'レ' END AS DORMITORY_KIBOU ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L013' AND N2.NAMECD2 = B1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");
        if (PRINT_PASS.equals(printDiv)) {
            stb.append("     AND B1.JUDGEMENT = '1' ");
        }
        if (PRINT_NAIPASS.equals(printDiv)) {
            stb.append("     AND B1.JUDGEMENT = '1' ");
            stb.append("     AND F1.FINSCHOOL_PREF_CD = '19' ");
        }
        if (PRINT_GAIPASS.equals(printDiv)) {
            stb.append("     AND B1.JUDGEMENT = '1' ");
            stb.append("     AND F1.FINSCHOOL_PREF_CD <> '19' ");
        }
        if (PRINT_UNPASS.equals(printDiv)) {
            stb.append("     AND B1.JUDGEMENT = '2' ");
        }
        if (PRINT_KESSEKI.equals(printDiv)) {
            stb.append("     AND B1.JUDGEMENT = '4' ");
        }
        if (PRINT_SCHOLAR.equals(printDiv)) {
            stb.append("     AND B1.SCHOLAR_KIBOU IS NOT NULL ");
            stb.append("     AND B1.SCHOLAR_SAIYOU = '1' ");
        }
        if (PRINT_UNSCHOLAR.equals(printDiv)) {
            stb.append("     AND B1.SCHOLAR_KIBOU IS NOT NULL ");
            stb.append("     AND B1.SCHOLAR_SAIYOU IS NULL ");
        }
        if (PRINT_SCHOLAR_HOPE.equals(printDiv)) {
            stb.append("     AND B1.SCHOLAR_KIBOU IS NOT NULL ");
        }
        if (PRINT_NYUURYOU.equals(printDiv)) {
            stb.append("     AND B1.DORMITORY_FLG = '1' ");
        }
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     B1.EXAMNO ");
        }

        return stb.toString();
    }

    private void printSchool(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {
        svf.VrSetForm("KNJL333Q_2.frm", 4);

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            final String sqlCnt = sqlSchool(printDiv, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 40;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlSchool(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrsOut("TITLE", _param._entexamyear + "年度　入試発送作業用リスト");
            svf.VrsOut("SUBTITLE", _param._testdivName + "《" + getSubTitle(printDiv) + "》");
            svf.VrsOut("APPLY", String.valueOf(totalCnt) + "件");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String koutyouCnt = rs.getString("KOUTYOU_CNT");
                final String jikoCnt = rs.getString("JIKO_CNT");
                final String ippanCnt = rs.getString("IPPAN_CNT");

                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("FINSCHOO_NO", fsCd);
                svf.VrsOut("FINSCHOOL_NAME", finschoolName);
                if (SUISEN.equals(_param._testdiv)) {
                    svf.VrsOut("HOPE_TITLE1", ((NameMst) _param._l004Map.get("3"))._name1 + "志願者数");
                    svf.VrsOut("HOPE_TITLE2", ((NameMst) _param._l004Map.get("4"))._name1 + "志願者数");
                    svf.VrsOut("HOPE1", koutyouCnt);
                    svf.VrsOut("HOPE2", jikoCnt);
                } else {
                    svf.VrsOut("HOPE_TITLE1", ((NameMst) _param._l004Map.get("5"))._name1 + "志願者数");
                    svf.VrsOut("HOPE1", ippanCnt);
                }

                svf.VrEndRecord();
                lineCnt++;
                lineNo++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlSchool(final String printDiv, final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        if ("CNT".equals(selectDiv)) {
            stb.append("     WITH MAIN_T AS ( ");
        }
        stb.append(" SELECT ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '3' THEN 1 ELSE 0 END) AS KOUTYOU_CNT, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '4' THEN 1 ELSE 0 END) AS JIKO_CNT, ");
        stb.append("     SUM(CASE WHEN B1.TESTDIV = '5' THEN 1 ELSE 0 END) AS IPPAN_CNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     B1.FS_CD ");
        if ("CNT".equals(selectDiv)) {
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("     MAIN_T ");
        }

        return stb.toString();
    }

    private void printHassouSchool(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        //表紙印刷
        printHassouSchoolHyoushi(db2, svf, printDiv);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL333Q_3_2.frm", 4);

            final String sql = sqlHassouSchool(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineNo = 1;
            String befSchool = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String judgementName1 = rs.getString("JUDGEMENT_NAME1");
                final String scholarKibou = rs.getString("SCHOLAR_KIBOU");
                final String scholarSaiyou = rs.getString("SCHOLAR_SAIYOU");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String examcourseAbbv = rs.getString("EXAMCOURSE_ABBV");
                final String prefName = rs.getString("PREF_NAME");

                if (!befSchool.equals(fsCd)) {
                    svf.VrsOut("FINSCHOOL_NAME", finschoolName);
                    svf.VrEndRecord();
                    lineNo = 1;
                }
                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("COURSE_NAME" + (getMS932Bytecount(examcourseAbbv) > 10 ? "2" : "1"), examcourseAbbv);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sexAbbv1);
                svf.VrsOut("JUDGE", judgementName1);
                svf.VrsOut("SCHOLAR1", scholarKibou);
                svf.VrsOut("SCHOLAR2", scholarSaiyou + " " + scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);
                svf.VrsOut("PREF_NAME", prefName);

                svf.VrEndRecord();
                lineNo++;
                befSchool = fsCd;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printHassouPass(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        //表紙印刷
        printHassouSchoolHyoushi(db2, svf, printDiv);

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            svf.VrSetForm("KNJL333Q_3_3.frm", 4);
            final String sqlCnt = sqlHassouSchool(printDiv, "CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 40;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlHassouSchool(printDiv, "");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineNo = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String examno = rs.getString("EXAMNO");
                final String testdivAbbv1 = rs.getString("TESTDIV_ABBV1");
                final String name = rs.getString("NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String judgementName1 = rs.getString("JUDGEMENT_NAME1");
                final String scholarKibou = rs.getString("SCHOLAR_KIBOU");
                final String scholarSaiyou = rs.getString("SCHOLAR_SAIYOU");
                final String scholarToukyu = rs.getString("SCHOLAR_TOUKYU");
                final String dormitoryKibou = rs.getString("DORMITORY_KIBOU");
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String examcourseAbbv = rs.getString("EXAMCOURSE_ABBV");
                final String prefName = rs.getString("PREF_NAME");

                svf.VrsOut("FINSCHOOL_NAME", finschoolName);
                svf.VrsOut("NO", String.valueOf(lineNo));
                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV", testdivAbbv1);
                svf.VrsOut("COURSE_NAME" + (getMS932Bytecount(examcourseAbbv) > 10 ? "2" : "1"), examcourseAbbv);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sexAbbv1);
                svf.VrsOut("JUDGE", judgementName1);
                svf.VrsOut("SCHOLAR1", scholarKibou);
                svf.VrsOut("SCHOLAR2", scholarSaiyou + " " + scholarToukyu);
                svf.VrsOut("DOMITORY", dormitoryKibou);
                svf.VrsOut("PREF_NAME", prefName);

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

    private String sqlHassouSchool(final String printDiv, final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     B1.EXAMNO, ");
            stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
            stb.append("     B1.NAME, ");
            stb.append("     B1.FS_CD, ");
            stb.append("     F1.FINSCHOOL_NAME, ");
            stb.append("     N2.NAME1 AS JUDGEMENT_NAME1, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ");
            stb.append("          ELSE '無' ");
            stb.append("     END AS SCHOLAR_KIBOU, ");
            stb.append("     CASE WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU = '1' THEN '採用' ");
            stb.append("          WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU IS NULL THEN '不採用' ");
            stb.append("          ELSE '' ");
            stb.append("     END AS SCHOLAR_SAIYOU, ");
            stb.append("     VALUE(B1.SCHOLAR_TOUKYU_SENGAN, '') AS SCHOLAR_TOUKYU, ");
            stb.append("     CASE WHEN B1.DORMITORY_FLG = '1' THEN '希望' ELSE '-' END AS DORMITORY_KIBOU, ");
            stb.append("     N3.ABBV1 AS SEX_ABBV1, ");
            stb.append("     C1.EXAMCOURSE_ABBV, ");
            stb.append("     P1.PREF_NAME ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     LEFT JOIN PREF_MST P1 ON P1.PREF_CD = F1.FINSCHOOL_PREF_CD ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L013' AND N2.NAMECD2 = B1.JUDGEMENT ");
        stb.append("     LEFT JOIN V_NAME_MST N3 ON N3.YEAR = B1.ENTEXAMYEAR AND N3.NAMECD1 = 'Z002' AND N3.NAMECD2 = B1.SEX ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            if (PRINT_HASSOU_SCHOOL.equals(printDiv)) {
                stb.append("     B1.FS_CD, ");
            } else {
                stb.append("     B1.JUDGEMENT, ");
            }
            stb.append("     B1.EXAMNO ");
        }
        return stb.toString();
    }

    private void printHassouSchoolHyoushi(final DB2UDB db2, final Vrw32alp svf, final String printDiv) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm("KNJL333Q_3_1.frm", 1);
            svf.VrsOut("NENDO", _param._entexamyear + "年度");
            svf.VrsOut("TITLE", "入学試験　書類発送用");
            svf.VrsOut("SUBTITLE", PRINT_HASSOU_SCHOOL.equals(printDiv) ? "中学校順" : "合否結果順");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日") + "版");

            final String sql = sqlHassouSchoolHyoushi("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            svf.VrsOut("COURSE_NAME1", getStringKeyEndsWith(_param._courseMap, COURSE_FUTSU));
            svf.VrsOut("COURSE_NAME2", getStringKeyEndsWith(_param._courseMap, COURSE_SPORT));
            svf.VrsOut("COURSE_NUM1", rs.getString("PASS_FUTSU"));
            svf.VrsOut("COURSE_NUM2", rs.getString("PASS_SPORT"));
            svf.VrsOut("TOTAL", String.valueOf(rs.getInt("PASS_FUTSU") + rs.getInt("PASS_SPORT")));
            svf.VrsOut("PLACE_NUM1", rs.getString("PASS_NAI"));
            svf.VrsOut("PLACE_NUM2", rs.getString("PASS_GAI"));
            svf.VrsOut("PLACE_NUM3", rs.getString("PASS_KAIGAI"));
            svf.VrsOut("ETC_NUM1", rs.getString("PASS_HOKETSU"));
            svf.VrsOut("ETC_NUM2", rs.getString("UNPASS"));
            svf.VrsOut("ETC_NUM3", rs.getString("KESSEKI"));
            svf.VrsOut("ETC_NUM4", rs.getString("PASS_JITAI"));
            svf.VrsOut("SCHOOL_NAME", _param._schoolName + "　普通科");

            svf.VrEndPage();
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlHassouSchoolHyoushi(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FUTSU + "' THEN 1 ELSE 0 END) AS PASS_FUTSU, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SPORT + "' THEN 1 ELSE 0 END) AS PASS_SPORT, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' THEN 1 ELSE 0 END) AS PASS, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' AND F1.FINSCHOOL_PREF_CD = '19' THEN 1 ELSE 0 END) AS PASS_NAI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' AND F1.FINSCHOOL_PREF_CD <> '19' AND F1.FINSCHOOL_PREF_CD <= '47' THEN 1 ELSE 0 END) AS PASS_GAI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' AND F1.FINSCHOOL_PREF_CD > '47' THEN 1 ELSE 0 END) AS PASS_KAIGAI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS PASS_HOKETSU, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '2' THEN 1 ELSE 0 END) AS UNPASS, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '4' THEN 1 ELSE 0 END) AS KESSEKI, ");
        stb.append("     SUM(CASE WHEN B1.JUDGEMENT = '1' AND (B1.PROCEDUREDIV = '2' OR B1.ENTDIV = '2') THEN 1 ELSE 0 END) AS PASS_JITAI ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testdiv + "' ");

        return stb.toString();
    }

    private String getStringKeyEndsWith(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (m.containsKey(field)) {
            return getString(m, field);
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
        return (String) m.get(key);
    }

    private String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private int getMS932Bytecount(String str) {
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58338 $");
        KNJServletUtils.debugParam(request, log);
        return param;
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

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testdiv;
        final String _testdivName;
        final String _taisyo;
        final String _all;
        final String _gokaku;
        final String _naiGokaku;
        final String _gaiGokaku;
        final String _fugokaku;
        final String _kesseki;
        final String _sksaiyo;
        final String _skfusaiyo;
        final String _skkibo;
        final String _nyuryo;
        final String _chugakuate;
        final String _order;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _printLogStaffcd;
        final Map _l004Map;
        final Map _courseMap;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _taisyo = request.getParameter("TAISYO");
            _all = request.getParameter("ALL");
            _gokaku = request.getParameter("GOKAKU");
            _naiGokaku = request.getParameter("NAIGOKAKU");
            _gaiGokaku = request.getParameter("GAIGOKAKU");
            _fugokaku = request.getParameter("FUGOKAKU");
            _kesseki = request.getParameter("KESSEKI");
            _sksaiyo = request.getParameter("SKSAIYO");
            _skfusaiyo = request.getParameter("SKFUSAIYO");
            _skkibo = request.getParameter("SKKIBO");
            _nyuryo = request.getParameter("NYURYO");
            _chugakuate = request.getParameter("CHUGAKUATE");
            _order = request.getParameter("ORDER");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _testdivName = getTestDivName(db2);
            _l004Map = getL004Map(db2);
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

        private Map getL004Map(final DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'L004' ");

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
            stb.append("     C1.EXAMCOURSECD, ");
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
                    retMap.put(rs.getString("EXAMCOURSECD"), rs.getString("EXAMCOURSE_ABBV"));
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

