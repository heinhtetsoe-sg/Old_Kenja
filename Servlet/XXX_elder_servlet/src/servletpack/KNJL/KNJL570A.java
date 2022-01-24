/*
 * $Id$
 *
 * 作成日: 2019/10/01
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL570A {

    private static final Log log = LogFactory.getLog(KNJL570A.class);
    private static final String CONST_SELALL = "99999";

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
        svf.VrSetForm("KNJL570A.frm", 1);
        final Map testSubjectMap = getNameMstIdName(db2);
        final List printList = getList(db2, testSubjectMap);

        int colCnt = 1;
        final int maxCnt = 50;
        setTitle(svf, db2, testSubjectMap);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (colCnt > maxCnt) {
                svf.VrEndPage();
                colCnt = 1;
                setTitle(svf, db2, testSubjectMap);
            }
            //受験番号
            final PrintData printData = (PrintData) iterator.next();
            svf.VrsOutn("EXAM_NO", colCnt, printData._receptNo);
            //氏名
            final int nlen = KNJ_EditEdit.getMS932ByteLength(printData._name);
            final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nfield, colCnt, printData._name);
            //出身中学
            svf.VrsOutn("FINSCHOOL_NAME", colCnt, printData._finschool_Abbv);
            //(内申)5科
            svf.VrsOutn("TOTAL_DIV5", colCnt, printData._total5);
            //(内申)9科
            svf.VrsOutn("TOTAL_DIV9", colCnt, printData._total_All);
            //内申
            List datList = new ArrayList();
            datList.add(StringUtils.defaultString(printData._confidential_Rpt01, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt02, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt03, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt04, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt05, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt06, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt07, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt08, "0"));
            datList.add(StringUtils.defaultString(printData._confidential_Rpt09, "0"));
            svf.VrsOutn("DIV", colCnt, _param.convertListtoOneLine(datList, true));

            //(得点)国語(偏差値)
            svf.VrsOutn("SCORE1", colCnt, printData._score_1);
            svf.VrsOutn("DEVI1", colCnt, printData._score_Dev1);
            //(得点)英語(偏差値)
            svf.VrsOutn("SCORE2", colCnt, printData._score_2);
            svf.VrsOutn("DEVI2", colCnt, printData._score_Dev2);
            //(得点)数学(偏差値)
            svf.VrsOutn("SCORE3", colCnt, printData._score_3);
            svf.VrsOutn("DEVI3", colCnt, printData._score_Dev3);
            //(得点)合計
            svf.VrsOutn("TOTAL_SCORE", colCnt, printData._total4);
            //順位
            svf.VrsOutn("RANK", colCnt, printData._total_Rank4);
            //確約者
            svf.VrsOutn("PRIMISE", colCnt, printData._promisePaper);
            //基準外
            svf.VrsOutn("NON_STANDARD", colCnt, printData._notStandard);
            //希望
            svf.VrsOutn("HOPE", colCnt, printData._hope_Name);
            //判定結果
            svf.VrsOutn("RESULT", colCnt, printData._result);
            //公立併願1
            final int aa1len = KNJ_EditEdit.getMS932ByteLength(printData._heigan_Kouritu1);
            final String aa1field = aa1len > 12 ? "_2" : "";
            svf.VrsOutn("ANOTHER_APPLI1" + aa1field, colCnt, printData._heigan_Kouritu1);
            //公立併願2
            final int aa2len = KNJ_EditEdit.getMS932ByteLength(printData._heigan_Kouritu2);
            final String aa2field = aa2len > 12 ? "_2" : "";
            svf.VrsOutn("ANOTHER_APPLI2" + aa2field, colCnt, printData._heigan_Kouritu2);
            //私立併願1
            final int aa3len = KNJ_EditEdit.getMS932ByteLength(printData._heigan_Siritu1);
            final String aa3field = aa3len > 12 ? "_2" : "";
            svf.VrsOutn("ANOTHER_APPLI3" + aa3field, colCnt, printData._heigan_Siritu1);
            //私立併願2
            final int aa4len = KNJ_EditEdit.getMS932ByteLength(printData._heigan_Siritu2);
            final String aa4field = aa4len > 12 ? "_2" : "";
            svf.VrsOutn("ANOTHER_APPLI4" + aa4field, colCnt, printData._heigan_Siritu1);

            colCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final DB2UDB db2, final Map testSubjectMap) {
        //入試区分
        final String testDivStr = "".equals(_param._testDiv) ? "" : CONST_SELALL.equals(_param._testDiv) ? "全て" : _param._testDivStr;
        //志望区分
        final String hopeDivStr = "".equals(_param._hopeCourseCode) ? "" : CONST_SELALL.equals(_param._hopeCourseCode) ? "全て" : _param._hopeCourseName;
        //ソート順
        final String sortStr = "2".equals(_param._sort) ? "＜合計得点順＞" : "＜受験番号順＞";
        final String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._ObjYear + "/04/01");
        svf.VrsOut("TITLE", setYear+"度 " + _param.getNameMst(db2, "L004", _param._testDiv) + "合否判定会議資料 " + "入試区分:" + testDivStr + " 志望区分:" + hopeDivStr + " " + sortStr);
        svf.VrsOut("CLASS_NAME", _param._subclsStr);
        int cnt = 1;
        int cntMax = 3;//表項目名称出力列数
        for (Iterator ite = testSubjectMap.keySet().iterator();ite.hasNext();) {
            if (cnt > cntMax) continue;
            final String kStr = (String)ite.next();
            final String subjStr = (String)testSubjectMap.get(kStr);
            svf.VrsOut("SUBCLASS_NAME"+Integer.parseInt(kStr), subjStr + "(偏差値)");
            cnt++;
        }
    }

    private List getList(final DB2UDB db2, final Map testSubjectMap) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(testSubjectMap);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String finschool_Abbv = rs.getString("FINSCHOOL_ABBV");
                final String hope_Name = rs.getString("HOPE_NAME");
                final String total5 = rs.getString("TOTAL5");
                final String total_All = rs.getString("TOTAL_ALL");
                final String confidential_Rpt01 = rs.getString("CONFIDENTIAL_RPT01");
                final String confidential_Rpt02 = rs.getString("CONFIDENTIAL_RPT02");
                final String confidential_Rpt03 = rs.getString("CONFIDENTIAL_RPT03");
                final String confidential_Rpt04 = rs.getString("CONFIDENTIAL_RPT04");
                final String confidential_Rpt05 = rs.getString("CONFIDENTIAL_RPT05");
                final String confidential_Rpt06 = rs.getString("CONFIDENTIAL_RPT06");
                final String confidential_Rpt07 = rs.getString("CONFIDENTIAL_RPT07");
                final String confidential_Rpt08 = rs.getString("CONFIDENTIAL_RPT08");
                final String confidential_Rpt09 = rs.getString("CONFIDENTIAL_RPT09");
                final String testSubclsCd1 = rs.getString("TESTSUBCLSCD1");
                final String score_1 = rs.getString("SCORE_1");
                final String score_Dev1 = rs.getString("SCORE_DEV1");
                final String testSubclsCd2 = rs.getString("TESTSUBCLSCD2");
                final String score_2 = rs.getString("SCORE_2");
                final String score_Dev2 = rs.getString("SCORE_DEV2");
                final String testSubclsCd3 = rs.getString("TESTSUBCLSCD3");
                final String score_3 = rs.getString("SCORE_3");
                final String score_Dev3 = rs.getString("SCORE_DEV3");
                final String total4 = rs.getString("TOTAL4");
                final String total_Rank4 = rs.getString("TOTAL_RANK4");
                final String promisePaper = rs.getString("PROMISEPAPER");
                final String notStandard = rs.getString("NOT_STANDARD");
                final String result = rs.getString("RESULT");
                final String heigan_Kouritu1 = rs.getString("HEIGAN_KOURITU1");
                final String heigan_Kouritu2 = rs.getString("HEIGAN_KOURITU2");
                final String heigan_Siritu1 = rs.getString("HEIGAN_SIRITU1");
                final String heigan_Siritu2 = rs.getString("HEIGAN_SIRITU2");

                final PrintData printData = new PrintData(receptNo, name, finschool_Abbv, hope_Name, total5, total_All, confidential_Rpt01, confidential_Rpt02,
                                                           confidential_Rpt03, confidential_Rpt04, confidential_Rpt05, confidential_Rpt06, confidential_Rpt07,
                                                           confidential_Rpt08, confidential_Rpt09, testSubclsCd1, score_1, score_Dev1, testSubclsCd2, score_2, score_Dev2, testSubclsCd3,
                                                           score_3, score_Dev3, total4, total_Rank4, promisePaper, notStandard, result, heigan_Kouritu1, heigan_Kouritu2, heigan_Siritu1, heigan_Siritu2);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private Map getNameMstIdName(final DB2UDB db2) {
        Map retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getNameMstIdNameSql("L009");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                retMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getNameMstIdNameSql(final String nameCd1) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     ESDWK1.TESTSUBCLASSCD AS NAMECD2, ");
        stb.append("     L009.NAME1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT ESDWK1 ");
        stb.append(" LEFT JOIN ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("   ON RCPT.ENTEXAMYEAR = ESDWK1.ENTEXAMYEAR ");
        stb.append("  AND RCPT.APPLICANTDIV = ESDWK1.APPLICANTDIV ");
        stb.append("  AND RCPT.TESTDIV = ESDWK1.TESTDIV ");
        stb.append("  AND RCPT.RECEPTNO = ESDWK1.RECEPTNO ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EABD ");
        stb.append("   ON EABD.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("  AND EABD.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("  AND EABD.TESTDIV = RCPT.TESTDIV ");
        stb.append("  AND EABD.EXAMNO = RCPT.EXAMNO ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ");
        stb.append("   ON BASEDTL01.ENTEXAMYEAR = EABD.ENTEXAMYEAR ");
        stb.append("  AND BASEDTL01.APPLICANTDIV = EABD.APPLICANTDIV ");
        stb.append("  AND BASEDTL01.EXAMNO = EABD.EXAMNO ");
        stb.append("  AND BASEDTL01.SEQ = '001' ");
        stb.append(" LEFT JOIN NAME_MST L009 ");
        stb.append("   ON NAMECD1 = 'L009' ");
        stb.append("  AND NAMECD2 = ESDWK1.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     EABD.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND EABD.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!CONST_SELALL.equals(_param._testDiv)) {
            stb.append("     AND EABD.TESTDIV = '" + _param._testDiv + "' ");
        }
        if (!CONST_SELALL.equals(_param._hopeCourseCode)) {
            stb.append("     AND BASEDTL01.REMARK10 = '" + _param._hopeCourseCode + "' ");
        }
        stb.append(" ORDER BY ESDWK1.TESTSUBCLASSCD ");
        return stb.toString();
    }

    private String getSql(final Map testSubjectMap) {
        Iterator ite = testSubjectMap.keySet().iterator();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CRPT_DETAIL_CHL AS ( ");
        stb.append("     SELECT ");
        stb.append("       CRPT.ENTEXAMYEAR, ");
        stb.append("       CRPT.APPLICANTDIV, ");
        stb.append("       CRPT.EXAMNO, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_01, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_02, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_03, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_04, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_05, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_06, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_07, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_08, ");
        stb.append("       CASE WHEN ECBM.HEALTH_PE_DISREGARD = '1' AND EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09' AND L008.ABBV3 = '1') THEN 2 ELSE (CASE WHEN CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END) END AS CRPT_CHKC_09, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_01, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_02, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_03, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_04, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_05, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_06, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_07, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_08, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09' AND L008.NAMESPARE1 = '1') AND CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK5_09, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '01') AND CRPT.CONFIDENTIAL_RPT01 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_01, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '02') AND CRPT.CONFIDENTIAL_RPT02 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_02, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '03') AND CRPT.CONFIDENTIAL_RPT03 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_03, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '04') AND CRPT.CONFIDENTIAL_RPT04 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_04, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '05') AND CRPT.CONFIDENTIAL_RPT05 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_05, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '06') AND CRPT.CONFIDENTIAL_RPT06 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_06, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '07') AND CRPT.CONFIDENTIAL_RPT07 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_07, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '08') AND CRPT.CONFIDENTIAL_RPT08 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_08, ");
        stb.append("       CASE WHEN EXISTS(SELECT 'X' FROM NAME_MST L008 WHERE L008.NAMECD1 = 'L008' AND L008.NAMECD2 = '09') AND CRPT.CONFIDENTIAL_RPT09 IS NOT NULL THEN 1 ELSE 0 END AS CRPT_CHK9_09 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("       ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND BASE.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND BASE.EXAMNO = RCPT.EXAMNO ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDAT ");
        stb.append("      ON BASEDAT.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("     AND BASEDAT.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("     AND BASEDAT.EXAMNO = RCPT.EXAMNO ");
        stb.append("     AND BASEDAT.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ");
        stb.append("       ON CRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND CRPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND CRPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ");
        stb.append("       ON ECBM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ECBM.TESTDIV = BASE.TESTDIV ");
        stb.append("      AND ECBM.HOPE_COURSECODE = BASEDAT.REMARK10 ");
        stb.append(" WHERE ");
        stb.append("     RCPT.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" ), RCPT_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     ROW_NUMBER() OVER(partition by ESDWK1.RECEPTNO ORDER BY ESDWK1.ENTEXAMYEAR, ESDWK1.APPLICANTDIV, ESDWK1.TESTDIV, ESDWK1.EXAM_TYPE, ESDWK1.RECEPTNO, ESDWK1.TESTSUBCLASSCD) AS rn, ");
        stb.append("     RCPT.ENTEXAMYEAR, ");
        stb.append("     RCPT.APPLICANTDIV, ");
        stb.append("     RCPT.TESTDIV, ");
        stb.append("     RCPT.EXAM_TYPE, ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     RCPT.EXAMNO, ");
        stb.append("     RCPT.TOTAL1, ");
        stb.append("     RCPT.TOTAL_RANK4, ");
        stb.append("     RCPT.TOTAL4, ");
        stb.append("     ESDWK1.TESTSUBCLASSCD, ");
        stb.append("     ESDWK1.ATTEND_FLG, ");
        stb.append("     ESDWK1.SCORE, ");
        stb.append("     ESDWK1.STD_SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT ESDWK1 ");
        stb.append("       ON ESDWK1.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND ESDWK1.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND ESDWK1.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND ESDWK1.EXAM_TYPE = RCPT.EXAM_TYPE ");
        stb.append("      AND ESDWK1.RECEPTNO = RCPT.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     RCPT.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     FM.FINSCHOOL_NAME_ABBV AS FINSCHOOL_ABBV, ");
        stb.append("     HOPEMST_SUC.HOPE_NAME, ");
        stb.append("     CRPT.TOTAL5, ");
        stb.append("     CRPT.TOTAL_ALL, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT01, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT02, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT03, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT04, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT05, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT06, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT07, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT08, ");
        stb.append("     CRPT.CONFIDENTIAL_RPT09, ");
        stb.append("     SCORE01.TESTSUBCLASSCD AS TESTSUBCLSCD1, ");
        stb.append("     SCORE01.SCORE AS SCORE_1, ");
        stb.append("     DECIMAL(ROUND(SCORE01.STD_SCORE*10)/10, 4, 1) AS SCORE_DEV1, ");
        stb.append("     SCORE02.TESTSUBCLASSCD AS TESTSUBCLSCD2, ");
        stb.append("     SCORE02.SCORE AS SCORE_2, ");
        stb.append("     DECIMAL(ROUND(SCORE02.STD_SCORE*10)/10, 4, 1) AS SCORE_DEV2, ");
        stb.append("     SCORE03.TESTSUBCLASSCD AS TESTSUBCLSCD3, ");
        stb.append("     SCORE03.SCORE AS SCORE_3, ");
        stb.append("     DECIMAL(ROUND(SCORE03.STD_SCORE*10)/10, 4, 1) AS SCORE_DEV3, ");
        stb.append("     RCPT.TOTAL4, ");
        stb.append("     RCPT.TOTAL_RANK4, ");
        stb.append("     CASE WHEN BASEDTL04.REMARK8 = '1' THEN '確約' ELSE '' END AS PROMISEPAPER, ");
        stb.append("     CASE WHEN ");
        stb.append("          (CASE WHEN CRPT_CHK.CRPT_CHKC_01 = 2 OR (CRPT_CHK.CRPT_CHKC_01 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT01, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_02 = 2 OR (CRPT_CHK.CRPT_CHKC_02 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT02, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_03 = 2 OR (CRPT_CHK.CRPT_CHKC_03 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT03, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_04 = 2 OR (CRPT_CHK.CRPT_CHKC_04 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT04, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_05 = 2 OR (CRPT_CHK.CRPT_CHKC_05 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT05, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_06 = 2 OR (CRPT_CHK.CRPT_CHKC_06 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT06, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_07 = 2 OR (CRPT_CHK.CRPT_CHKC_07 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT07, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_08 = 2 OR (CRPT_CHK.CRPT_CHKC_08 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT08, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHKC_09 = 2 OR (CRPT_CHK.CRPT_CHKC_09 = 1 AND VALUE(CRPT.CONFIDENTIAL_RPT09, 0) >= VALUE(ECBM.CLASS_SCORE, 0)) THEN 0 ELSE 1 END ");
        stb.append("          ) > 0 THEN '＊' ");
        stb.append("          WHEN ");
        stb.append("          (CASE WHEN CRPT_CHK.CRPT_CHK5_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END  ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_02 = 1 THEN CRPT.CONFIDENTIAL_RPT02 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_03 = 1 THEN CRPT.CONFIDENTIAL_RPT03 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_04 = 1 THEN CRPT.CONFIDENTIAL_RPT04 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_05 = 1 THEN CRPT.CONFIDENTIAL_RPT05 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_06 = 1 THEN CRPT.CONFIDENTIAL_RPT06 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_07 = 1 THEN CRPT.CONFIDENTIAL_RPT07 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_08 = 1 THEN CRPT.CONFIDENTIAL_RPT08 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK5_09 = 1 THEN CRPT.CONFIDENTIAL_RPT09 ELSE 0 END ");
        stb.append("          ) < ECBM.SCORE5 THEN '＊' ");
        stb.append("          WHEN ");
        stb.append("          (CASE WHEN CRPT_CHK.CRPT_CHK9_01 = 1 THEN CRPT.CONFIDENTIAL_RPT01 ELSE 0 END  ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_02 = 1 THEN CRPT.CONFIDENTIAL_RPT02 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_03 = 1 THEN CRPT.CONFIDENTIAL_RPT03 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_04 = 1 THEN CRPT.CONFIDENTIAL_RPT04 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_05 = 1 THEN CRPT.CONFIDENTIAL_RPT05 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_06 = 1 THEN CRPT.CONFIDENTIAL_RPT06 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_07 = 1 THEN CRPT.CONFIDENTIAL_RPT07 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_08 = 1 THEN CRPT.CONFIDENTIAL_RPT08 ELSE 0 END ");
        stb.append("          + CASE WHEN CRPT_CHK.CRPT_CHK9_09 = 1 THEN CRPT.CONFIDENTIAL_RPT09 ELSE 0 END ");
        stb.append("          ) < ECBM.SCORE9 THEN '＊' ");
        stb.append("          ELSE '' ");
        stb.append("          END AS NOT_STANDARD, ");
        stb.append("     L013.NAME1 ");
        stb.append("     || CASE WHEN BASE.JUDGEMENT = '1' AND BASE.SUC_COURSECODE IS NOT NULL ");
        stb.append("                  AND BASEDTL01.REMARK10 <> BASE.SUC_COURSECODE ");
        stb.append("                  THEN '(' || HOPEMST_SUC.NOT_PASS_NAME || ')' ");
        stb.append("             ELSE '' ");
        stb.append("        END AS RESULT, ");
        stb.append("     FMH1.FINSCHOOL_NAME_ABBV AS HEIGAN_KOURITU1, ");
        stb.append("     FMH2.FINSCHOOL_NAME_ABBV AS HEIGAN_KOURITU2, ");
        stb.append("     FMH3.FINSCHOOL_NAME_ABBV AS HEIGAN_SIRITU1, ");
        stb.append("     FMH4.FINSCHOOL_NAME_ABBV AS HEIGAN_SIRITU2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("       ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND BASE.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND BASE.EXAMNO = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ESM ");
        stb.append("       ON ESM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ESM.ENTEXAM_SCHOOLCD = BASE.FS_CD");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("       ON FM.FINSCHOOLCD = ESM.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ");
        stb.append("       ON BASEDTL01.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL01.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND BASEDTL01.EXAMNO = BASE.EXAMNO ");
        stb.append("      AND BASEDTL01.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CRPT ");
        stb.append("       ON CRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND CRPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND CRPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN RCPT_BASE SCORE01 ");
        stb.append("       ON SCORE01.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND SCORE01.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND SCORE01.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND SCORE01.EXAM_TYPE = RCPT.EXAM_TYPE ");
        stb.append("      AND SCORE01.RECEPTNO = RCPT.RECEPTNO ");
        final String itecd1 = ite.hasNext() ? (String)ite.next() : "";
        stb.append("      AND SCORE01.TESTSUBCLASSCD = '" + itecd1  +"' ");
        stb.append("     LEFT JOIN RCPT_BASE SCORE02 ");
        stb.append("       ON SCORE02.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND SCORE02.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND SCORE02.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND SCORE02.EXAM_TYPE = RCPT.EXAM_TYPE ");
        stb.append("      AND SCORE02.RECEPTNO = RCPT.RECEPTNO ");
        final String itecd2 = ite.hasNext() ? (String)ite.next() : "";
        stb.append("      AND SCORE02.TESTSUBCLASSCD = '" + itecd2 + "' ");
        stb.append("     LEFT JOIN RCPT_BASE SCORE03 ");
        stb.append("       ON SCORE03.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND SCORE03.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND SCORE03.TESTDIV = RCPT.TESTDIV ");
        stb.append("      AND SCORE03.EXAM_TYPE = RCPT.EXAM_TYPE ");
        stb.append("      AND SCORE03.RECEPTNO = RCPT.RECEPTNO ");
        final String itecd3 = ite.hasNext() ? (String)ite.next() : "";
        stb.append("      AND SCORE03.TESTSUBCLASSCD = '" + itecd3 + "' ");
        stb.append("     LEFT JOIN NAME_MST L009_1 ");
        stb.append("       ON L009_1.NAMECD1 = 'L009' ");
        stb.append("      AND L009_1.NAMECD2 = SCORE01.TESTSUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST L009_2 ");
        stb.append("       ON L009_2.NAMECD1 = 'L009' ");
        stb.append("      AND L009_2.NAMECD2 = SCORE02.TESTSUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST L009_3 ");
        stb.append("       ON L009_3.NAMECD1 = 'L009' ");
        stb.append("      AND L009_3.NAMECD2 = SCORE03.TESTSUBCLASSCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL04 ");
        stb.append("       ON BASEDTL04.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL04.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND BASEDTL04.EXAMNO = BASE.EXAMNO ");
        stb.append("      AND BASEDTL04.SEQ = '004' ");
        stb.append("     LEFT JOIN ENTEXAM_CONFRPT_BASE_MST ECBM ");
        stb.append("       ON ECBM.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ECBM.TESTDIV = BASE.TESTDIV ");
        stb.append("      AND ECBM.HOPE_COURSECODE = BASEDTL01.REMARK10 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL05 ");
        stb.append("       ON BASEDTL05.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND BASEDTL05.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("      AND BASEDTL05.EXAMNO = BASE.EXAMNO ");
        stb.append("      AND BASEDTL05.SEQ = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_1 ");
        stb.append("       ON ESMB05_1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ESMB05_1.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FMH1 ");
        stb.append("       ON FMH1.FINSCHOOLCD = ESMB05_1.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_2 ");
        stb.append("       ON ESMB05_2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ESMB05_2.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FMH2 ");
        stb.append("       ON FMH2.FINSCHOOLCD = ESMB05_2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_3 ");
        stb.append("       ON ESMB05_3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ESMB05_3.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK3 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FMH3 ");
        stb.append("       ON FMH3.FINSCHOOLCD = ESMB05_3.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ESMB05_4 ");
        stb.append("       ON ESMB05_4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("      AND ESMB05_4.ENTEXAM_SCHOOLCD = BASEDTL05.REMARK4 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FMH4 ");
        stb.append("       ON FMH4.FINSCHOOLCD = ESMB05_4.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_HOPE_COURSE_MST HOPEMST_SUC ");
        stb.append("       ON HOPEMST_SUC.HOPE_COURSECODE = BASEDTL01.REMARK10 ");
        stb.append("     LEFT JOIN NAME_MST L013 ");
        stb.append("       ON L013.NAMECD1 = 'L013' ");
        stb.append("      AND L013.NAMECD2 = BASE.JUDGEMENT ");
        stb.append("     LEFT JOIN CRPT_DETAIL_CHL CRPT_CHK ");
        stb.append("       ON CRPT_CHK.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("      AND CRPT_CHK.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("      AND CRPT_CHK.EXAMNO = RCPT.EXAMNO ");
        stb.append("     ");
        stb.append(" WHERE ");
        stb.append("     RCPT.ENTEXAMYEAR = '" + _param._ObjYear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV = '1' ");
        if (!CONST_SELALL.equals(_param._testDiv)) {
            stb.append("     AND RCPT.TESTDIV = '" + _param._testDiv + "' ");
        }
        if (!CONST_SELALL.equals(_param._hopeCourseCode)) {
            stb.append("     AND BASEDTL01.REMARK10 = '" + _param._hopeCourseCode + "' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sort)) {
            stb.append("     RCPT.TOTAL4 IS NULL ASC, ");
            stb.append("     RCPT.TOTAL4 DESC, ");
        }
        stb.append("     RCPT.RECEPTNO ");

        return stb.toString();
    }


    private class PrintData {
        final String _receptNo;
        final String _name;
        final String _finschool_Abbv;
        final String _hope_Name;
        final String _total5;
        final String _total_All;
        final String _confidential_Rpt01;
        final String _confidential_Rpt02;
        final String _confidential_Rpt03;
        final String _confidential_Rpt04;
        final String _confidential_Rpt05;
        final String _confidential_Rpt06;
        final String _confidential_Rpt07;
        final String _confidential_Rpt08;
        final String _confidential_Rpt09;
        final String _testSubclsCd1;
        final String _score_1;
        final String _score_Dev1;
        final String _testSubclsCd2;
        final String _score_2;
        final String _score_Dev2;
        final String _testSubclsCd3;
        final String _score_3;
        final String _score_Dev3;
        final String _total4;
        final String _total_Rank4;
        final String _promisePaper;
        final String _notStandard;
        final String _result;
        final String _heigan_Kouritu1;
        final String _heigan_Kouritu2;
        final String _heigan_Siritu1;
        final String _heigan_Siritu2;
        public PrintData(
                final String receptNo, final String name, final String finschool_Abbv, final String hope_Name, final String total5,
                final String total_All, final String confidential_Rpt01, final String confidential_Rpt02, final String confidential_Rpt03,
                final String confidential_Rpt04, final String confidential_Rpt05, final String confidential_Rpt06, final String confidential_Rpt07,
                final String confidential_Rpt08, final String confidential_Rpt09, final String testSubclsCd1, final String score_1, final String score_Dev1,final String testSubclsCd2, final String score_2,
                final String score_Dev2, final String testSubclsCd3, final String score_3, final String score_Dev3, final String total4,final String total_Rank4, final String promisePaper, final String notStandard,
                final String result, final String heigan_Kouritu1, final String heigan_Kouritu2, final String heigan_Siritu1, final String heigan_Siritu2
        ) {
            _receptNo = receptNo;
            _name = name;
            _finschool_Abbv = finschool_Abbv;
            _hope_Name = hope_Name;
            _total5 = total5;
            _total_All = total_All;
            _confidential_Rpt01 = confidential_Rpt01;
            _confidential_Rpt02 = confidential_Rpt02;
            _confidential_Rpt03 = confidential_Rpt03;
            _confidential_Rpt04 = confidential_Rpt04;
            _confidential_Rpt05 = confidential_Rpt05;
            _confidential_Rpt06 = confidential_Rpt06;
            _confidential_Rpt07 = confidential_Rpt07;
            _confidential_Rpt08 = confidential_Rpt08;
            _confidential_Rpt09 = confidential_Rpt09;
            _testSubclsCd1 = testSubclsCd1;
            _score_1 = score_1;
            _score_Dev1 = score_Dev1;
            _testSubclsCd2 = testSubclsCd2;
            _score_2 = score_2;
            _score_Dev2 = score_Dev2;
            _testSubclsCd3 = testSubclsCd3;
            _score_3 = score_3;
            _score_Dev3 = score_Dev3;
            _total4 = total4;
            _total_Rank4 = total_Rank4;
            _promisePaper = promisePaper;
            _notStandard = notStandard;
            _result = result;
            _heigan_Kouritu1 = heigan_Kouritu1;
            _heigan_Kouritu2 = heigan_Kouritu2;
            _heigan_Siritu1 = heigan_Siritu1;
            _heigan_Siritu2 = heigan_Siritu2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70851 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;

        private final String _ObjYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _testDivStr;
        private final String _hopeCourseCode;
        private final String _hopeCourseName;

        private final String _sort;
        private final String _subclsStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear       = request.getParameter("LOGIN_YEAR");
            _loginSemester   = request.getParameter("LOGIN_SEMESTER");
            _loginDate       = request.getParameter("LOGIN_DATE");
            _ObjYear         = request.getParameter("ENTEXAMYEAR");
            _applicantDiv    = request.getParameter("APPLICANTDIV");
            _testDiv         = request.getParameter("TESTDIV");
            _testDivStr      = getNameMst(db2, "L004", _testDiv);
            _hopeCourseCode  = StringUtils.defaultString(request.getParameter("DESIREDIV"), "");
            _hopeCourseName  = getHopeCourseName(db2, _hopeCourseCode);
            _sort          = request.getParameter("SORT");
            _subclsStr = convertListtoOneLine(getNameMstList(db2, "L008", "ABBV2"), false);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   NAME1 ");
            stb.append(" FROM ");
            stb.append("   NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   NAMECD1 = '" + nameCd1 + "' ");
            stb.append("   AND NAMECD2 = '" + nameCd2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("   NAMECD1 ");
            final String sql =  stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private List getNameMstList(final DB2UDB db2, final String nameCd1, final String getColName) {
            List retList = new ArrayList();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   " + getColName + " ");
            stb.append(" FROM ");
            stb.append("   NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   NAMECD1 = '" + nameCd1 + "' ");
            stb.append(" ORDER BY ");
            stb.append("   NAMECD1 ");
            final String sql =  stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retList.add(rs.getString(getColName));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String getHopeCourseName(final DB2UDB db2, final String hopeCourseCd) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("   T2.HOPE_NAME ");  // 志望区分名称
            stb.append(" FROM ");
            stb.append("   ENTEXAM_HOPE_COURSE_YDAT T1 ");
            stb.append("   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ");
            stb.append("     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ");
            stb.append(" WHERE ");
            stb.append("   ENTEXAMYEAR = '" + _ObjYear + "' ");
            stb.append("   AND T1.HOPE_COURSECODE = '" + hopeCourseCd + "' ");
            final String sql =  stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("HOPE_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String convertListtoOneLine(final List datList, final boolean valSetFlg) {
            String retStr = "";
            for (int cnt = 0;cnt < datList.size();cnt++) {
                if (cnt == 0) {
                    retStr += "(";
                } else {
                    if (valSetFlg) {
                        retStr += " ";
                    } else {
                        retStr += "|";
                    }
                }
                if (valSetFlg) {
                    retStr += (Integer.parseInt((String)datList.get(cnt)) > 9 ? "" : " ") + (String)datList.get(cnt);
                } else {
                    retStr += (String)datList.get(cnt);
                }
                if (cnt == datList.size()-1) {
                    retStr += ")";
                }
            }
            return retStr;
        }
    }
}

// eof
