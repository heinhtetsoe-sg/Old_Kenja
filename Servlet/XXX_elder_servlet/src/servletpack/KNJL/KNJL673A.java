/*
 * $Id: ccc75220b03e2b8d967ece22d74d1d9a32bfbd32 $
 *
 * 作成日: 2020/06/15
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL673A{

    private static final Log log = LogFactory.getLog(KNJL673A.class);

    private boolean _hasData;

    private Param _param;

    private final String EXAMCOURSECD_ALL = "9999";
    private final String TESTDIV_NOBE = "5";
    private final String TESTDIV_JITSU = "6";
    private final String HOPE_COURSE1 = "1";
    private final String HOPE_COURSE2 = "2";
    private final String HOPE_COURSE_ALL = "3";

    private final String DIV_TOKUSHOU = "10";
    private final String DIV_TOKUSHOU1 = "11";
    private final String DIV_TOKUSHOU2 = "12";
    private final String DIV_TOKUSHOU3 = "13";
    private final String DIV_KIKOKU = "21";
    private final String DIV_RYOSEI = "31";

    private final String SENBATU_C1 = "03";
    private final String SENBATU_C2 = "04";
    private final String SENBATU_C3 = "05";

    private final String TESTSUBCLASSCD3 = "T3";
    private final String TESTSUBCLASSCD5 = "T5";

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
        print1(db2, svf); //ページ(1/4)

        print2(db2, svf); //ページ(2/4)

        print3(db2, svf); //ページ(3/4)

        print4(db2, svf); //ページ(4/4)
    }

    private void print1(final DB2UDB db2, final Vrw32alp svf) {
        final Map printDataMap = getPrintData1Map(db2);

        svf.VrSetForm("KNJL673A_1.frm", 1);
        svf.VrsOut("TITLE", "麗澤高等学校　" + _param._nendo + "　入学試験概況"); //タイトル
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            final String examcourseName = (String) _param._examcourseMap.get(examcourseCd);
            final String courseNo = (!EXAMCOURSECD_ALL.equals(examcourseCd)) ? String.valueOf(++cNo) : "3";
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", examcourseName); //コース名
            //◆ループ(列6つ)各回(4つ)・延人数・実人数
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                final TestdivMst testdivMst = (TestdivMst) _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                svf.VrsOutn("DATE" + courseNo, line, KNJ_EditDate.h_format_JP_MD(testdivMst._testdivDate)); //試験日
                for (Iterator its = _param._sexMap.keySet().iterator(); its.hasNext();) {
                    final String sex = (String) its.next();
                    final String sexName = (String) _param._sexMap.get(sex);
                    svf.VrsOutn("SEX_NAME" + courseNo + "_" + sex, line, sexName); //性別
                }
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    final String key = examcourseCd + testdiv + hopeCourseDiv;
                    final PrintData1 printData = (PrintData1) printDataMap.get(key);
                    if (printData == null) continue;
                    if (HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望
                        //志願者数
                        svf.VrsOutn("HOPE" + courseNo + "_1", line, printData._hope1); //男子
                        svf.VrsOutn("HOPE" + courseNo + "_2", line, printData._hope2); //女子
                        svf.VrsOutn("HOPE" + courseNo + "_3", line, printData._hope3); //合計
                        //受験者数
                        svf.VrsOutn("EXAM" + courseNo + "_1", line, printData._exam1); //男子
                        svf.VrsOutn("EXAM" + courseNo + "_2", line, printData._exam2); //女子
                        svf.VrsOutn("EXAM" + courseNo + "_3", line, printData._exam3); //合計
                        //合格者数
                        svf.VrsOutn("PASS" + courseNo + "_1", line, printData._pass1); //男子
                        svf.VrsOutn("PASS" + courseNo + "_2", line, printData._pass2); //女子
                        svf.VrsOutn("PASS" + courseNo + "_3", line, printData._pass3); //合計
                        //入学者数
                        svf.VrsOutn("ENT" + courseNo + "_1", line, printData._ent1); //男子
                        svf.VrsOutn("ENT" + courseNo + "_2", line, printData._ent2); //女子
                        svf.VrsOutn("ENT" + courseNo + "_3", line, printData._ent3); //合計
                        //募集人員
                        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) {
                            svf.VrsOutn("RECRUIT" + courseNo + "_3", line, printData._recruit); //合計
                        }
                        //志願者倍率
                        if (TESTDIV_NOBE.equals(testdiv) || TESTDIV_JITSU.equals(testdiv)) {
                            svf.VrsOutn("HOPE_RATIO" + courseNo + "_3", line, getRate(printData._hope3, printData._recruit)); //合計
                        }
                        //実質倍率
                        if ((TESTDIV_NOBE.equals(testdiv) || TESTDIV_JITSU.equals(testdiv)) && !EXAMCOURSECD_ALL.equals(examcourseCd)) {
                            svf.VrsOutn("PASS_RATIO" + courseNo + "_1", line, getRate(printData._exam1, printData._pass1)); //男子
                            svf.VrsOutn("PASS_RATIO" + courseNo + "_2", line, getRate(printData._exam2, printData._pass2)); //女子
                        }
                        svf.VrsOutn("PASS_RATIO" + courseNo + "_3", line, getRate(printData._exam3, printData._pass3)); //合計
                        //入学手続率
                        svf.VrsOutn("ENT_RATIO" + courseNo + "_1", line, getRate(printData._ent1, printData._pass1)); //男子
                        svf.VrsOutn("ENT_RATIO" + courseNo + "_2", line, getRate(printData._ent2, printData._pass2)); //女子
                        svf.VrsOutn("ENT_RATIO" + courseNo + "_3", line, getRate(printData._ent3, printData._pass3)); //合計
                    } else if (HOPE_COURSE1.equals(hopeCourseDiv) && !EXAMCOURSECD_ALL.equals(examcourseCd)) { //第1志望
                        int hcNo = 1;
                        //志願者数(第1志望)
                        svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._hope1); //男子
                        svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._hope2); //女子
                        svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._hope3); //合計
                        //受験者数(第1志望)
                        svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._exam1); //男子
                        svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._exam2); //女子
                        svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._exam3); //合計
                        //合格者数(第1志望)
                        svf.VrsOutn("PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._pass1); //男子
                        svf.VrsOutn("PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._pass2); //女子
                        svf.VrsOutn("PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._pass3); //合計
                        //入学者数(第1志望)
                        svf.VrsOutn("ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._ent1); //男子
                        svf.VrsOutn("ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._ent2); //女子
                        svf.VrsOutn("ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._ent3); //合計
                    } else if (HOPE_COURSE2.equals(hopeCourseDiv) && !EXAMCOURSECD_ALL.equals(examcourseCd)) { //第2志望
                        int hcNo = 2;
                        //志願者数(第2志望)
                        svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._hope1); //男子
                        svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._hope2); //女子
                        svf.VrsOutn("HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._hope3); //合計
                        //受験者数(第2志望)
                        svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._exam1); //男子
                        svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._exam2); //女子
                        svf.VrsOutn("EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._exam3); //合計
                        //合格者数(第2志望)
                        svf.VrsOutn("PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._pass1); //男子
                        svf.VrsOutn("PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._pass2); //女子
                        svf.VrsOutn("PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._pass3); //合計
                        //入学者数(第2志望)
                        svf.VrsOutn("ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._ent1); //男子
                        svf.VrsOutn("ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._ent2); //女子
                        svf.VrsOutn("ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._ent3); //合計
                    }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private String getRate(final String bunsi, final String bunbo) {
        if (bunsi == null || bunbo == null) return null;
        if (NumberUtils.isNumber(bunbo) && Integer.parseInt(bunbo) == 0) return "0.00";
        return new BigDecimal(bunsi).divide(new BigDecimal(bunbo), 2, BigDecimal.ROUND_HALF_UP).toString(); //小数点第3位を四捨五入し、小数点第2位まで表示
    }

    private void print2(final DB2UDB db2, final Vrw32alp svf) {
        final Map printDataMap = getPrintData2Map(db2);

        svf.VrSetForm("KNJL673A_2.frm", 1);
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            final String examcourseName = (String) _param._examcourseMap.get(examcourseCd);
            final String courseNo = (!EXAMCOURSECD_ALL.equals(examcourseCd)) ? String.valueOf(++cNo) : "3";
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", examcourseName); //コース名
            //◆ループ(列6つ)各回(4つ)・延人数・実人数
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                final TestdivMst testdivMst = (TestdivMst) _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                for (Iterator its = _param._sexMap.keySet().iterator(); its.hasNext();) {
                    final String sex = (String) its.next();
                    final String sexName = (String) _param._sexMap.get(sex);
                    svf.VrsOutn("SEX_NAME" + courseNo + "_" + sex, line, sexName); //性別
                }
                for (Iterator itDiv = _param._divList.iterator(); itDiv.hasNext();) {
                    final String div = (String) itDiv.next();
                    final String divField = DIV_RYOSEI.equals(div) ? "DOM_" : DIV_KIKOKU.equals(div) ? "RET_" : "SC_";
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    final String key = examcourseCd + testdiv + hopeCourseDiv + div;
                    final PrintData2 printData = (PrintData2) printDataMap.get(key);
                    if (printData == null) continue;
                    if ("SC_".equals(divField) && HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望
                        final String tokushouNo = DIV_TOKUSHOU1.equals(div) ? "_1" : DIV_TOKUSHOU2.equals(div) ? "_2" : DIV_TOKUSHOU3.equals(div) ? "_3" : "";
                        //特奨採用者数(全,1,2,3種)
                        svf.VrsOutn(divField + "ADPTION" + courseNo + tokushouNo + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(divField + "ADPTION" + courseNo + tokushouNo + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(divField + "ADPTION" + courseNo + tokushouNo + "_3", line, printData._pass3); //合計
                        //特奨入学者数(全,1,2,3種)
                        svf.VrsOutn(divField + "ENT" + courseNo + tokushouNo + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(divField + "ENT" + courseNo + tokushouNo + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(divField + "ENT" + courseNo + tokushouNo + "_3", line, printData._ent3); //合計
                    } else if (!"SC_".equals(divField) && HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望
                        //志願者数
                        svf.VrsOutn(divField + "HOPE" + courseNo + "_1", line, printData._hope1); //男子
                        svf.VrsOutn(divField + "HOPE" + courseNo + "_2", line, printData._hope2); //女子
                        svf.VrsOutn(divField + "HOPE" + courseNo + "_3", line, printData._hope3); //合計
                        //受験者数
                        svf.VrsOutn(divField + "EXAM" + courseNo + "_1", line, printData._exam1); //男子
                        svf.VrsOutn(divField + "EXAM" + courseNo + "_2", line, printData._exam2); //女子
                        svf.VrsOutn(divField + "EXAM" + courseNo + "_3", line, printData._exam3); //合計
                        //合格者数
                        svf.VrsOutn(divField + "PASS" + courseNo + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(divField + "PASS" + courseNo + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(divField + "PASS" + courseNo + "_3", line, printData._pass3); //合計
                        //入学者数
                        svf.VrsOutn(divField + "ENT" + courseNo + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(divField + "ENT" + courseNo + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(divField + "ENT" + courseNo + "_3", line, printData._ent3); //合計
                    } else if (!"SC_".equals(divField) && HOPE_COURSE1.equals(hopeCourseDiv) && !EXAMCOURSECD_ALL.equals(examcourseCd)) { //第1志望
                        int hcNo = 1;
                        //志願者数(第1志望)
                        svf.VrsOutn(divField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._hope1); //男子
                        svf.VrsOutn(divField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._hope2); //女子
                        svf.VrsOutn(divField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._hope3); //合計
                        //受験者数(第1志望)
                        svf.VrsOutn(divField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._exam1); //男子
                        svf.VrsOutn(divField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._exam2); //女子
                        svf.VrsOutn(divField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._exam3); //合計
                        //合格者数(第1志望)
                        svf.VrsOutn(divField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(divField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(divField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._pass3); //合計
                        //入学者数(第1志望)
                        svf.VrsOutn(divField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(divField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(divField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._ent3); //合計
                    } else if (!"SC_".equals(divField) && HOPE_COURSE2.equals(hopeCourseDiv) && !EXAMCOURSECD_ALL.equals(examcourseCd)) { //第2志望
                        int hcNo = 2;
                        //志願者数(第2志望)
                        svf.VrsOutn(divField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._hope1); //男子
                        svf.VrsOutn(divField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._hope2); //女子
                        svf.VrsOutn(divField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._hope3); //合計
                        //受験者数(第2志望)
                        svf.VrsOutn(divField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._exam1); //男子
                        svf.VrsOutn(divField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._exam2); //女子
                        svf.VrsOutn(divField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._exam3); //合計
                        //合格者数(第2志望)
                        svf.VrsOutn(divField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(divField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(divField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._pass3); //合計
                        //入学者数(第2志望)
                        svf.VrsOutn(divField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(divField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(divField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._ent3); //合計
                    }
                }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void print3(final DB2UDB db2, final Vrw32alp svf) {
        final Map printDataMap = getPrintData3Map(db2);

        svf.VrSetForm("KNJL673A_3.frm", 1);
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            final String examcourseName = (String) _param._examcourseMap.get(examcourseCd);
            final String courseNo = (!EXAMCOURSECD_ALL.equals(examcourseCd)) ? String.valueOf(++cNo) : "3";
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", examcourseName); //コース名
            //◆ループ(列6つ)各回(4つ)・延人数・実人数
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                final TestdivMst testdivMst = (TestdivMst) _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                for (Iterator its = _param._sexMap.keySet().iterator(); its.hasNext();) {
                    final String sex = (String) its.next();
                    final String sexName = (String) _param._sexMap.get(sex);
                    svf.VrsOutn("SEX_NAME" + courseNo + "_" + sex, line, sexName); //性別
                }
                for (Iterator itSen = _param._senbatuList.iterator(); itSen.hasNext();) {
                    final String senbatu = (String) itSen.next();
                    final String senbatuField = SENBATU_C1.equals(senbatu) ? "DOM_" : SENBATU_C2.equals(senbatu) ? "CLUB_" : SENBATU_C3.equals(senbatu) ? "SC_" : "";
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    final String key = examcourseCd + testdiv + hopeCourseDiv + senbatu;
                    final PrintData3 printData = (PrintData3) printDataMap.get(key);
                    if (printData == null) continue;
                    if (HOPE_COURSE_ALL.equals(hopeCourseDiv)) { //第1志望 + 第2志望
                        //志願者数
                        svf.VrsOutn(senbatuField + "HOPE" + courseNo + "_1", line, printData._hope1); //男子
                        svf.VrsOutn(senbatuField + "HOPE" + courseNo + "_2", line, printData._hope2); //女子
                        svf.VrsOutn(senbatuField + "HOPE" + courseNo + "_3", line, printData._hope3); //合計
                        //受験者数
                        svf.VrsOutn(senbatuField + "EXAM" + courseNo + "_1", line, printData._exam1); //男子
                        svf.VrsOutn(senbatuField + "EXAM" + courseNo + "_2", line, printData._exam2); //女子
                        svf.VrsOutn(senbatuField + "EXAM" + courseNo + "_3", line, printData._exam3); //合計
                        //合格者数
                        svf.VrsOutn(senbatuField + "PASS" + courseNo + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(senbatuField + "PASS" + courseNo + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(senbatuField + "PASS" + courseNo + "_3", line, printData._pass3); //合計
                        //入学者数
                        svf.VrsOutn(senbatuField + "ENT" + courseNo + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(senbatuField + "ENT" + courseNo + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(senbatuField + "ENT" + courseNo + "_3", line, printData._ent3); //合計
                    } else if (HOPE_COURSE1.equals(hopeCourseDiv) && !EXAMCOURSECD_ALL.equals(examcourseCd)) { //第1志望
                        int hcNo = 1;
                        //志願者数(第1志望)
                        svf.VrsOutn(senbatuField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._hope1); //男子
                        svf.VrsOutn(senbatuField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._hope2); //女子
                        svf.VrsOutn(senbatuField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._hope3); //合計
                        //受験者数(第1志望)
                        svf.VrsOutn(senbatuField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._exam1); //男子
                        svf.VrsOutn(senbatuField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._exam2); //女子
                        svf.VrsOutn(senbatuField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._exam3); //合計
                        //合格者数(第1志望)
                        svf.VrsOutn(senbatuField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(senbatuField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(senbatuField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._pass3); //合計
                        //入学者数(第1志望)
                        svf.VrsOutn(senbatuField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(senbatuField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(senbatuField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._ent3); //合計
                    } else if (HOPE_COURSE2.equals(hopeCourseDiv) && !EXAMCOURSECD_ALL.equals(examcourseCd)) { //第2志望
                        int hcNo = 2;
                        //志願者数(第2志望)
                        svf.VrsOutn(senbatuField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._hope1); //男子
                        svf.VrsOutn(senbatuField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._hope2); //女子
                        svf.VrsOutn(senbatuField + "HOPE_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._hope3); //合計
                        //受験者数(第2志望)
                        svf.VrsOutn(senbatuField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._exam1); //男子
                        svf.VrsOutn(senbatuField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._exam2); //女子
                        svf.VrsOutn(senbatuField + "EXAM_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._exam3); //合計
                        //合格者数(第2志望)
                        svf.VrsOutn(senbatuField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._pass1); //男子
                        svf.VrsOutn(senbatuField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._pass2); //女子
                        svf.VrsOutn(senbatuField + "PASS_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._pass3); //合計
                        //入学者数(第2志望)
                        svf.VrsOutn(senbatuField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_1", line, printData._ent1); //男子
                        svf.VrsOutn(senbatuField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_2", line, printData._ent2); //女子
                        svf.VrsOutn(senbatuField + "ENT_COURSE" + courseNo + "_" + String.valueOf(hcNo) + "_3", line, printData._ent3); //合計
                    }
                }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void print4(final DB2UDB db2, final Vrw32alp svf) {
        final Map printDataMap = getPrintData4Map(db2);

        svf.VrSetForm("KNJL673A_4.frm", 1);
        svf.VrsOut("TITLE", "麗澤高等学校　" + _param._nendo + "　入学試験概況"); //タイトル
        //◆ループ(表3つ)各コース(2つ)・全体合計
        int cNo = 0;
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            final String examcourseName = (String) _param._examcourseMap.get(examcourseCd);
            final String courseNo = (!EXAMCOURSECD_ALL.equals(examcourseCd)) ? String.valueOf(++cNo) : "3";
            svf.VrsOut("COURSE_NAME" + courseNo + "_1", examcourseName); //コース名
            //◆ループ(列4つ)各回(4つ)
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                if (TESTDIV_NOBE.equals(testdiv) || TESTDIV_JITSU.equals(testdiv)) {
                    continue;
                }
                final TestdivMst testdivMst = (TestdivMst) _param._testdivMstMap.get(testdiv);
                final int line = testdivMst._line;
                svf.VrsOutn("COUNT" + courseNo, line, testdivMst._testdivAbbv); //試験名
                svf.VrsOutn("DATE" + courseNo, line, KNJ_EditDate.h_format_JP_MD(testdivMst._testdivDate)); //試験日

                int subNo = 0;
                for (Iterator its = _param._testsubclassMap.keySet().iterator(); its.hasNext();) {
                    final String testsubclasscd = (String) its.next();
                    final String testsubclassName = (String) _param._testsubclassMap.get(testsubclasscd);
                    final String subclassNo = TESTSUBCLASSCD3.equals(testsubclasscd) ? "7" : TESTSUBCLASSCD5.equals(testsubclasscd) ? "8" : String.valueOf(++subNo);
                    final String nameField = KNJ_EditEdit.getMS932ByteLength(testsubclassName) > 6 ? "2" : "1";

                    final String key = examcourseCd + testdiv + testsubclasscd;
                    final PrintData4 printData = (PrintData4) printDataMap.get(key);
                    if (printData == null) continue;

                    svf.VrsOutn("CLASS_NAME" + courseNo + "_" + subclassNo + "_" + nameField, line, testsubclassName); //試験科目名
                    svf.VrsOutn("EXAM" + courseNo + "_" + subclassNo, line, printData._examCnt); //受験者数
                    svf.VrsOutn("PERFECT" + courseNo + "_" + subclassNo, line, printData._perfect); //満点
                    svf.VrsOutn("AVERAGE" + courseNo + "_1_" + subclassNo, line, printData._scoreAvgSex1); //男子平均点
                    svf.VrsOutn("AVERAGE" + courseNo + "_2_" + subclassNo, line, printData._scoreAvgSex2); //女子平均点
                    svf.VrsOutn("AVERAGE" + courseNo + "_3_" + subclassNo, line, printData._scoreAvg); //全体平均点
                    svf.VrsOutn("DEVI" + courseNo + "_" + subclassNo, line, printData._scoreStddev); //全体標準偏差
                    svf.VrsOutn("MAX" + courseNo + "_" + subclassNo, line, printData._scoreMax); //最高点
                    svf.VrsOutn("MIN" + courseNo + "_" + subclassNo, line, printData._scoreMin); //最低点
                    svf.VrsOutn("PASS_AVERAGE" + courseNo + "_" + subclassNo, line, printData._passAvg); //合格者平均点
                    svf.VrsOutn("SC_MIN" + courseNo + "_1_" + subclassNo, line, printData._tokushouMin1); //特奨1種最低点
                    svf.VrsOutn("SC_MIN" + courseNo + "_2_" + subclassNo, line, printData._tokushouMin2); //特奨2種最低点
                    svf.VrsOutn("SC_MIN" + courseNo + "_3_" + subclassNo, line, printData._tokushouMin3); //特奨3種最低点
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private Map getPrintData1Map(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        final String sql = getPrintData1Sql(examcourseCd, testdiv, hopeCourseDiv);
                        log.debug(" sql =" + sql);
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String recruit = rs.getString("RECRUIT");
                            final String hope1 = rs.getString("HOPE1");
                            final String hope2 = rs.getString("HOPE2");
                            final String hope3 = rs.getString("HOPE3");
                            final String exam1 = rs.getString("EXAM1");
                            final String exam2 = rs.getString("EXAM2");
                            final String exam3 = rs.getString("EXAM3");
                            final String pass1 = rs.getString("PASS1");
                            final String pass2 = rs.getString("PASS2");
                            final String pass3 = rs.getString("PASS3");
                            final String ent1 = rs.getString("ENT1");
                            final String ent2 = rs.getString("ENT2");
                            final String ent3 = rs.getString("ENT3");

                            final PrintData1 printData1 = new PrintData1(examcourseCd, testdiv, hopeCourseDiv, recruit, hope1, hope2, hope3, exam1, exam2, exam3, pass1, pass2, pass3, ent1, ent2, ent3);
                            retMap.put(examcourseCd + testdiv + hopeCourseDiv, printData1);
                        }
                    } catch (SQLException ex) {
                        log.debug("Exception:", ex);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                }
            }
        }
        for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
            final String testdiv = (String) itt.next();
            for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                final String hopeCourseDiv = (String) ith.next();

                PrintData1 courseTotal = new PrintData1(EXAMCOURSECD_ALL, testdiv, hopeCourseDiv);
                
                for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
                	final String examcourseCd = (String) itc.next();
                    if (EXAMCOURSECD_ALL.equals(examcourseCd)) continue;
                    final PrintData1 printData1 = (PrintData1) retMap.get(examcourseCd + testdiv + hopeCourseDiv);
                    if (null != printData1) {
                    	courseTotal._recruit = plus(courseTotal._recruit, printData1._recruit);
                    	courseTotal._hope1 = plus(courseTotal._hope1, printData1._hope1);
						courseTotal._hope2 = plus(courseTotal._hope2, printData1._hope2);
						courseTotal._hope3 = plus(courseTotal._hope3, printData1._hope3);
						courseTotal._exam1 = plus(courseTotal._exam1, printData1._exam1);
						courseTotal._exam2 = plus(courseTotal._exam2, printData1._exam2);
						courseTotal._exam3 = plus(courseTotal._exam3, printData1._exam3);
						courseTotal._pass1 = plus(courseTotal._pass1, printData1._pass1);
						courseTotal._pass2 = plus(courseTotal._pass2, printData1._pass2);
						courseTotal._pass3 = plus(courseTotal._pass3, printData1._pass3);
                    }
                }
                final PrintData1 printDataAll = (PrintData1) retMap.get(EXAMCOURSECD_ALL + testdiv + hopeCourseDiv);
                if (null != printDataAll) {
                	courseTotal._ent1 = printDataAll._ent1;
                	courseTotal._ent2 = printDataAll._ent2;
                	courseTotal._ent3 = printDataAll._ent3;
                }
                retMap.put(EXAMCOURSECD_ALL + testdiv + hopeCourseDiv, courseTotal);
            }
        }

        return retMap;
    }

    private String plus(final String i1, final String i2) {
        if (!NumberUtils.isDigits(i1)) return i2;
        if (!NumberUtils.isDigits(i2)) return i1;
        return String.valueOf((!NumberUtils.isDigits(i1) ? 0 : Integer.parseInt(i1)) + (!NumberUtils.isDigits(i2) ? 0 : Integer.parseInt(i2)));
    }

    private Map getPrintData2Map(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                for (Iterator itDiv = _param._divList.iterator(); itDiv.hasNext();) {
                    final String div = (String) itDiv.next();
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        final String sql = getPrintData2Sql(examcourseCd, testdiv, hopeCourseDiv, div);
                        log.debug(" sql =" + sql);
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String hope1 = rs.getString("HOPE1");
                            final String hope2 = rs.getString("HOPE2");
                            final String hope3 = rs.getString("HOPE3");
                            final String exam1 = rs.getString("EXAM1");
                            final String exam2 = rs.getString("EXAM2");
                            final String exam3 = rs.getString("EXAM3");
                            final String pass1 = rs.getString("PASS1");
                            final String pass2 = rs.getString("PASS2");
                            final String pass3 = rs.getString("PASS3");
                            final String ent1 = rs.getString("ENT1");
                            final String ent2 = rs.getString("ENT2");
                            final String ent3 = rs.getString("ENT3");

                            final PrintData2 printData2 = new PrintData2(examcourseCd, testdiv, hopeCourseDiv, div, hope1, hope2, hope3, exam1, exam2, exam3, pass1, pass2, pass3, ent1, ent2, ent3);
                            retMap.put(examcourseCd + testdiv + hopeCourseDiv + div, printData2);
                        }
                    } catch (SQLException ex) {
                        log.debug("Exception:", ex);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                }
                }
            }
        }
        for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
        	final String testdiv = (String) itt.next();
        	for (Iterator itDiv = _param._divList.iterator(); itDiv.hasNext();) {
        		final String div = (String) itDiv.next();
        		for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
        			final String hopeCourseDiv = (String) ith.next();

        			PrintData2 courseTotal = new PrintData2(EXAMCOURSECD_ALL, testdiv, hopeCourseDiv, div);
                    
                    for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
                    	final String examcourseCd = (String) itc.next();
                        if (EXAMCOURSECD_ALL.equals(examcourseCd)) continue;
                        final PrintData2 printData2 = (PrintData2) retMap.get(examcourseCd + testdiv + hopeCourseDiv + div);
                        if (null != printData2) {
                        	courseTotal._hope1 = plus(courseTotal._hope1, printData2._hope1);
    						courseTotal._hope2 = plus(courseTotal._hope2, printData2._hope2);
    						courseTotal._hope3 = plus(courseTotal._hope3, printData2._hope3);
    						courseTotal._exam1 = plus(courseTotal._exam1, printData2._exam1);
    						courseTotal._exam2 = plus(courseTotal._exam2, printData2._exam2);
    						courseTotal._exam3 = plus(courseTotal._exam3, printData2._exam3);
    						courseTotal._pass1 = plus(courseTotal._pass1, printData2._pass1);
    						courseTotal._pass2 = plus(courseTotal._pass2, printData2._pass2);
    						courseTotal._pass3 = plus(courseTotal._pass3, printData2._pass3);
                        }
                    }
                    final PrintData2 printDataAll = (PrintData2) retMap.get(EXAMCOURSECD_ALL + testdiv + hopeCourseDiv + div);
                    if (null != printDataAll) {
                    	courseTotal._ent1 = printDataAll._ent1;
                    	courseTotal._ent2 = printDataAll._ent2;
                    	courseTotal._ent3 = printDataAll._ent3;
                    }
                    retMap.put(EXAMCOURSECD_ALL + testdiv + hopeCourseDiv + div, courseTotal);
                }
            }
        }
        return retMap;
    }

    private Map getPrintData3Map(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                for (Iterator itSen = _param._senbatuList.iterator(); itSen.hasNext();) {
                    final String senbatu = (String) itSen.next();
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        final String sql = getPrintData3Sql(examcourseCd, testdiv, hopeCourseDiv, senbatu);
                        log.debug(" sql =" + sql);
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String hope1 = rs.getString("HOPE1");
                            final String hope2 = rs.getString("HOPE2");
                            final String hope3 = rs.getString("HOPE3");
                            final String exam1 = rs.getString("EXAM1");
                            final String exam2 = rs.getString("EXAM2");
                            final String exam3 = rs.getString("EXAM3");
                            final String pass1 = rs.getString("PASS1");
                            final String pass2 = rs.getString("PASS2");
                            final String pass3 = rs.getString("PASS3");
                            final String ent1 = rs.getString("ENT1");
                            final String ent2 = rs.getString("ENT2");
                            final String ent3 = rs.getString("ENT3");

                            final PrintData3 printData3 = new PrintData3(examcourseCd, testdiv, hopeCourseDiv, senbatu, hope1, hope2, hope3, exam1, exam2, exam3, pass1, pass2, pass3, ent1, ent2, ent3);
                            retMap.put(examcourseCd + testdiv + hopeCourseDiv + senbatu, printData3);
                        }
                    } catch (SQLException ex) {
                        log.debug("Exception:", ex);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                }
                }
            }
        }
        for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
        	final String testdiv = (String) itt.next();
        	for (Iterator itSen = _param._senbatuList.iterator(); itSen.hasNext();) {
        		final String senbatu = (String) itSen.next();
                for (Iterator ith = _param._hopeCourseDivList.iterator(); ith.hasNext();) {
                    final String hopeCourseDiv = (String) ith.next();
                    
                    PrintData3 courseTotal = new PrintData3(EXAMCOURSECD_ALL, testdiv, hopeCourseDiv, senbatu);
                    
                    for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
                    	final String examcourseCd = (String) itc.next();
                    	if (EXAMCOURSECD_ALL.equals(examcourseCd)) continue;
                    	final PrintData3 printData3 = (PrintData3) retMap.get(examcourseCd + testdiv + hopeCourseDiv + senbatu);
                    	if (null != printData3) {
                    		courseTotal._hope1 = plus(courseTotal._hope1, printData3._hope1);
                    		courseTotal._hope2 = plus(courseTotal._hope2, printData3._hope2);
                    		courseTotal._hope3 = plus(courseTotal._hope3, printData3._hope3);
                    		courseTotal._exam1 = plus(courseTotal._exam1, printData3._exam1);
                    		courseTotal._exam2 = plus(courseTotal._exam2, printData3._exam2);
                    		courseTotal._exam3 = plus(courseTotal._exam3, printData3._exam3);
                    		courseTotal._pass1 = plus(courseTotal._pass1, printData3._pass1);
                    		courseTotal._pass2 = plus(courseTotal._pass2, printData3._pass2);
                    		courseTotal._pass3 = plus(courseTotal._pass3, printData3._pass3);
                    	}
                    }
                    final PrintData3 printDataAll = (PrintData3) retMap.get(EXAMCOURSECD_ALL + testdiv + hopeCourseDiv + senbatu);
                    if (null != printDataAll) {
                    	courseTotal._ent1 = printDataAll._ent1;
                    	courseTotal._ent2 = printDataAll._ent2;
                    	courseTotal._ent3 = printDataAll._ent3;
                    }
                    retMap.put(EXAMCOURSECD_ALL + testdiv + hopeCourseDiv + senbatu, courseTotal);
                }
            }
        }
        return retMap;
    }

    private Map getPrintData4Map(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        for (Iterator itc = _param._examcourseMap.keySet().iterator(); itc.hasNext();) {
            final String examcourseCd = (String) itc.next();
            for (Iterator itt = _param._testdivMstMap.keySet().iterator(); itt.hasNext();) {
                final String testdiv = (String) itt.next();
                if (TESTDIV_NOBE.equals(testdiv) || TESTDIV_JITSU.equals(testdiv)) {
                    continue;
                }
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = getPrintData4Sql(examcourseCd, testdiv);
                    log.debug(" sql =" + sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                        final String examCnt = rs.getString("EXAM_CNT");
                        final String perfect = rs.getString("PERFECT");
                        final String scoreAvgSex1 = rs.getString("SCORE_AVG_SEX1");
                        final String scoreAvgSex2 = rs.getString("SCORE_AVG_SEX2");
                        final String scoreAvg = rs.getString("SCORE_AVG");
                        final String scoreStddev = rs.getString("SCORE_STDDEV");
                        final String scoreMax = rs.getString("SCORE_MAX");
                        final String scoreMin = rs.getString("SCORE_MIN");
                        final String passAvg = rs.getString("PASS_AVG");
                        final String tokushouMin1 = rs.getString("TOKUSHOU_MIN1");
                        final String tokushouMin2 = rs.getString("TOKUSHOU_MIN2");
                        final String tokushouMin3 = rs.getString("TOKUSHOU_MIN3");

                        final PrintData4 printData4 = new PrintData4(examcourseCd, testdiv, testsubclasscd, examCnt, perfect, scoreAvgSex1, scoreAvgSex2, scoreAvg, scoreStddev, scoreMax, scoreMin, passAvg, tokushouMin1, tokushouMin2, tokushouMin3);
                        retMap.put(examcourseCd + testdiv + testsubclasscd, printData4);
                    }
                } catch (SQLException ex) {
                    log.debug("Exception:", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }
        return retMap;
    }

    private String getPrintData1Sql(final String examcourseCd, final String testdiv, final String hopeCourseDiv) {
        final StringBuffer stb = new StringBuffer();
        //メイン表
        stb.append(" SELECT ");
        stb.append("     MAX(C1.CAPACITY) AS RECRUIT, "); //募集人員
        if (TESTDIV_JITSU.equals(testdiv)) { //実人数
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '1' THEN R1.EXAMNO END) AS HOPE1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '2' THEN R1.EXAMNO END) AS HOPE2, ");
            stb.append("     COUNT(DISTINCT R1.EXAMNO) AS HOPE3, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            }
        } else { //各回・延人数
            stb.append("     COUNT(CASE WHEN B1.SEX = '1' THEN RD_007.RECEPTNO END) AS HOPE1, ");
            stb.append("     COUNT(CASE WHEN B1.SEX = '2' THEN RD_007.RECEPTNO END) AS HOPE2, ");
            stb.append("     COUNT(RD_007.RECEPTNO) AS HOPE3, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS EXAM1, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS EXAM2, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' THEN RD_007.RECEPTNO END) AS EXAM3, ");
            if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            }
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             RD7.TESTDIV, ");
        stb.append("             RD7.RECEPTNO ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_RECEPT_DETAIL_DAT RD7 ");
        stb.append("         WHERE ");
        stb.append("             RD7.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND RD7.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND RD7.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { // 各コースの表
            if (HOPE_COURSE1.equals(hopeCourseDiv)) { // 第1志望
                stb.append("     AND (RD7.REMARK1 = '" + examcourseCd + "') ");
            } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { // 第2志望
                stb.append("     AND (RD7.REMARK2 = '" + examcourseCd + "' AND VALUE(RD7.REMARK7, '') != '1') ");
            } else {
                stb.append("     AND (RD7.REMARK1 = '" + examcourseCd + "' OR RD7.REMARK2 = '" + examcourseCd + "' AND VALUE(RD7.REMARK7, '') != '1') ");
            }
        }
        stb.append("     ) RD7 ON RD7.RECEPTNO = R1.RECEPTNO AND RD7.TESTDIV = R1.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             S1.TESTDIV, ");
        stb.append("             S1.RECEPTNO, ");
        stb.append("             '1' AS EXAM_FLG ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_SCORE_DAT S1 ");
        stb.append("         WHERE ");
        stb.append("             S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND S1.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("             AND S1.SCORE2 IS NOT NULL ");
        stb.append("         GROUP BY ");
        stb.append("             S1.TESTDIV, ");
        stb.append("             S1.RECEPTNO ");
        stb.append("     ) S1 ON S1.RECEPTNO = RD7.RECEPTNO AND S1.TESTDIV = RD7.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             BD_007.EXAMNO, ");
        stb.append("             '1' AS ENT_FLG, ");
        stb.append("             BD_007.REMARK4 AS TESTDIV ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_007 ");
        stb.append("         WHERE ");
        stb.append("             BD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND BD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND BD_007.SEQ = '007' ");
        stb.append("             AND BD_007.REMARK1 || BD_007.REMARK2 || BD_007.REMARK3 IN ( ");
        stb.append("                 SELECT ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE AS ENTER_COURSE ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_COURSE_MST C1 ");
        stb.append("                 WHERE ");
        stb.append("                     C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                     AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("                     AND C1.TESTDIV = '" + testdiv + "' ");
        }
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("                     AND C1.EXAMCOURSECD = '" + examcourseCd + "' ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE ");
        stb.append("             ) ");
        stb.append("     ) BD_007 ON BD_007.EXAMNO = B1.EXAMNO AND BD_007.TESTDIV = RD_007.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             C1.ENTEXAMYEAR, C1.APPLICANTDIV, ");
        stb.append("             SUM(C1.CAPACITY) AS CAPACITY ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_COURSE_MST C1 ");
        stb.append("         WHERE ");
        stb.append("             C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND C1.TESTDIV = '" + testdiv + "' ");
        }
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("             AND C1.EXAMCOURSECD = '" + examcourseCd + "' ");
        }
        stb.append("         GROUP BY ");
        stb.append("             C1.ENTEXAMYEAR, C1.APPLICANTDIV ");
        stb.append("     ) C1 ON C1.ENTEXAMYEAR = R1.ENTEXAMYEAR AND C1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("     AND RD_007.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "') ");
            } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                stb.append("     AND (RD_007.REMARK2 = '" + examcourseCd + "') ");
            } else {
                stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "' OR RD_007.REMARK2 = '" + examcourseCd + "') ");
            }
        }
        return stb.toString();
    }

    private String getPrintData2Sql(final String examcourseCd, final String testdiv, final String hopeCourseDiv, final String div) {
        final StringBuffer stb = new StringBuffer();
        //メイン表
        stb.append(" SELECT ");
        if (TESTDIV_JITSU.equals(testdiv)) { //実人数
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '1' THEN R1.EXAMNO END) AS HOPE1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '2' THEN R1.EXAMNO END) AS HOPE2, ");
            stb.append("     COUNT(DISTINCT R1.EXAMNO) AS HOPE3, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            }
        } else { //各回・延人数
            stb.append("     COUNT(CASE WHEN B1.SEX = '1' THEN RD_007.RECEPTNO END) AS HOPE1, ");
            stb.append("     COUNT(CASE WHEN B1.SEX = '2' THEN RD_007.RECEPTNO END) AS HOPE2, ");
            stb.append("     COUNT(RD_007.RECEPTNO) AS HOPE3, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS EXAM1, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS EXAM2, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' THEN RD_007.RECEPTNO END) AS EXAM3, ");
            if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            }
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             RD7.TESTDIV, ");
        stb.append("             RD7.RECEPTNO ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_RECEPT_DETAIL_DAT RD7 ");
        stb.append("         WHERE ");
        stb.append("             RD7.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND RD7.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND RD7.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { // 各コースの表
            if (HOPE_COURSE1.equals(hopeCourseDiv)) { // 第1志望
                stb.append("     AND (RD7.REMARK1 = '" + examcourseCd + "') ");
            } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { // 第2志望
                stb.append("     AND (RD7.REMARK2 = '" + examcourseCd + "' AND VALUE(RD7.REMARK7, '') != '1') ");
            } else {
                stb.append("     AND (RD7.REMARK1 = '" + examcourseCd + "' OR RD7.REMARK2 = '" + examcourseCd + "' AND VALUE(RD7.REMARK7, '') != '1') ");
            }
        }
        stb.append("     ) RD7 ON RD7.RECEPTNO = R1.RECEPTNO AND RD7.TESTDIV = R1.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             S1.TESTDIV, ");
        stb.append("             S1.RECEPTNO, ");
        stb.append("             '1' AS EXAM_FLG ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_SCORE_DAT S1 ");
        stb.append("         WHERE ");
        stb.append("             S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND S1.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("             AND S1.SCORE2 IS NOT NULL ");
        stb.append("         GROUP BY ");
        stb.append("             S1.TESTDIV, ");
        stb.append("             S1.RECEPTNO ");
        stb.append("     ) S1 ON S1.RECEPTNO = RD7.RECEPTNO AND S1.TESTDIV = RD7.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             BD_007.EXAMNO, ");
        stb.append("             '1' AS ENT_FLG, ");
        stb.append("             BD_007.REMARK4 AS TESTDIV ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_007 ");
        stb.append("         WHERE ");
        stb.append("             BD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND BD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND BD_007.SEQ = '007' ");
        stb.append("             AND BD_007.REMARK1 || BD_007.REMARK2 || BD_007.REMARK3 IN ( ");
        stb.append("                 SELECT ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE AS ENTER_COURSE ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_COURSE_MST C1 ");
        stb.append("                 WHERE ");
        stb.append("                     C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                     AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("                     AND C1.TESTDIV = '" + testdiv + "' ");
        }
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("                     AND C1.EXAMCOURSECD = '" + examcourseCd + "' ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE ");
        stb.append("             ) ");
        stb.append("     ) BD_007 ON BD_007.EXAMNO = B1.EXAMNO AND BD_007.TESTDIV = RD_007.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_005 ");
        stb.append("          ON BD_005.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND BD_005.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND BD_005.EXAMNO = R1.EXAMNO ");
        stb.append("         AND BD_005.SEQ = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_015 ");
        stb.append("          ON RD_015.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND RD_015.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND RD_015.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND RD_015.EXAM_TYPE = RD_007.EXAM_TYPE  ");
        stb.append("         AND RD_015.RECEPTNO = RD_007.RECEPTNO  ");
        stb.append("         AND RD_015.SEQ = '015' ");
        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("     AND RD_007.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "') ");
            } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                stb.append("     AND (RD_007.REMARK2 = '" + examcourseCd + "') ");
            } else {
                stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "' OR RD_007.REMARK2 = '" + examcourseCd + "') ");
            }
        }
        if (DIV_TOKUSHOU.equals(div)) { //特奨(全)
            stb.append("     AND RD_015.REMARK1 IS NOT NULL ");
        } else if (DIV_TOKUSHOU1.equals(div) || DIV_TOKUSHOU2.equals(div) || DIV_TOKUSHOU3.equals(div)) { //特奨(1,2,3種)
            final String tokushouCd = DIV_TOKUSHOU1.equals(div) ? "001" : DIV_TOKUSHOU2.equals(div) ? "002" : DIV_TOKUSHOU3.equals(div) ? "003" : "";
            stb.append("     AND RD_015.REMARK1 = '" + tokushouCd + "' ");
        } else if (DIV_KIKOKU.equals(div)) {
            stb.append("     AND BD_005.REMARK2 = '1' "); //帰国子女
        } else if (DIV_RYOSEI.equals(div)) {
            stb.append("     AND RD_007.REMARK5 = '1' "); //寮生
        }
        return stb.toString();
    }

    private String getPrintData3Sql(final String examcourseCd, final String testdiv, final String hopeCourseDiv, final String senbatu) {
        final StringBuffer stb = new StringBuffer();
        //メイン表
        stb.append(" SELECT ");
        if (TESTDIV_JITSU.equals(testdiv)) { //実人数
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '1' THEN R1.EXAMNO END) AS HOPE1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN B1.SEX = '2' THEN R1.EXAMNO END) AS HOPE2, ");
            stb.append("     COUNT(DISTINCT R1.EXAMNO) AS HOPE3, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS EXAM1, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS EXAM2, ");
            stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' THEN R1.EXAMNO END) AS EXAM3, ");
            if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN R1.EXAMNO END) AS PASS1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN R1.EXAMNO END) AS PASS2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN R1.EXAMNO END) AS PASS3, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN R1.EXAMNO END) AS ENT1, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN R1.EXAMNO END) AS ENT2, ");
                    stb.append("     COUNT(DISTINCT CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN R1.EXAMNO END) AS ENT3 ");
                }
            }
        } else { //各回・延人数
            stb.append("     COUNT(CASE WHEN B1.SEX = '1' THEN RD_007.RECEPTNO END) AS HOPE1, ");
            stb.append("     COUNT(CASE WHEN B1.SEX = '2' THEN RD_007.RECEPTNO END) AS HOPE2, ");
            stb.append("     COUNT(RD_007.RECEPTNO) AS HOPE3, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS EXAM1, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS EXAM2, ");
            stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' THEN RD_007.RECEPTNO END) AS EXAM3, ");
            if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            } else { //全体合計の表
                if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                    //フォームの印字欄はないが、SQLは残しておく
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                } else {
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS PASS1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS PASS2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN RD_007.RECEPTNO END) AS PASS3, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '1' THEN RD_007.RECEPTNO END) AS ENT1, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' AND B1.SEX = '2' THEN RD_007.RECEPTNO END) AS ENT2, ");
                    stb.append("     COUNT(CASE WHEN S1.EXAM_FLG = '1' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') AND BD_007.ENT_FLG = '1' THEN RD_007.RECEPTNO END) AS ENT3 ");
                }
            }
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             RD7.TESTDIV, ");
        stb.append("             RD7.RECEPTNO ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_RECEPT_DETAIL_DAT RD7 ");
        stb.append("         WHERE ");
        stb.append("             RD7.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND RD7.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND RD7.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { // 各コースの表
            if (HOPE_COURSE1.equals(hopeCourseDiv)) { // 第1志望
                stb.append("     AND (RD7.REMARK1 = '" + examcourseCd + "') ");
            } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { // 第2志望
                stb.append("     AND (RD7.REMARK2 = '" + examcourseCd + "' AND VALUE(RD7.REMARK7, '') != '1') ");
            } else {
                stb.append("     AND (RD7.REMARK1 = '" + examcourseCd + "' OR RD7.REMARK2 = '" + examcourseCd + "' AND VALUE(RD7.REMARK7, '') != '1') ");
            }
        }
        stb.append("     ) RD7 ON RD7.RECEPTNO = R1.RECEPTNO AND RD7.TESTDIV = R1.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             S1.TESTDIV, ");
        stb.append("             S1.RECEPTNO, ");
        stb.append("             '1' AS EXAM_FLG ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_SCORE_DAT S1 ");
        stb.append("         WHERE ");
        stb.append("             S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("             AND S1.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("             AND S1.SCORE2 IS NOT NULL ");
        stb.append("         GROUP BY ");
        stb.append("             S1.TESTDIV, ");
        stb.append("             S1.RECEPTNO ");
        stb.append("     ) S1 ON S1.RECEPTNO = RD7.RECEPTNO AND S1.TESTDIV = RD7.TESTDIV ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             BD_007.EXAMNO, ");
        stb.append("             '1' AS ENT_FLG, ");
        stb.append("             BD_007.REMARK4 AS TESTDIV ");
        stb.append("         FROM ");
        stb.append("             ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_007 ");
        stb.append("         WHERE ");
        stb.append("             BD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND BD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND BD_007.SEQ = '007' ");
        stb.append("             AND BD_007.REMARK1 || BD_007.REMARK2 || BD_007.REMARK3 IN ( ");
        stb.append("                 SELECT ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE AS ENTER_COURSE ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_COURSE_MST C1 ");
        stb.append("                 WHERE ");
        stb.append("                     C1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                     AND C1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("                     AND C1.TESTDIV = '" + testdiv + "' ");
        }
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("                     AND C1.EXAMCOURSECD = '" + examcourseCd + "' ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     C1.ENTER_COURSECD || C1.ENTER_MAJORCD || C1.ENTER_COURSECODE ");
        stb.append("             ) ");
        stb.append("     ) BD_007 ON BD_007.EXAMNO = B1.EXAMNO AND BD_007.TESTDIV = RD_007.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!TESTDIV_NOBE.equals(testdiv) && !TESTDIV_JITSU.equals(testdiv)) { //各回
            stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        }
        stb.append("     AND RD_007.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            if (HOPE_COURSE1.equals(hopeCourseDiv)) { //第1志望
                stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "') ");
            } else if (HOPE_COURSE2.equals(hopeCourseDiv)) { //第2志望
                stb.append("     AND (RD_007.REMARK2 = '" + examcourseCd + "') ");
            } else {
                stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "' OR RD_007.REMARK2 = '" + examcourseCd + "') ");
            }
        }
        stb.append("     AND RD_007.REMARK6 = '" + senbatu + "' "); //選抜方式
        return stb.toString();
    }

    private String getPrintData4Sql(final String examcourseCd, final String testdiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_PERFECT AS ( ");
        stb.append("     SELECT ");
        stb.append("         P1.TESTSUBCLASSCD, ");
        stb.append("         MAX(P1.PERFECT) AS PERFECT ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_PERFECT_MST P1 ");
        stb.append("     WHERE ");
        stb.append("         P1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND P1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND P1.TESTDIV = '" + testdiv + "' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("     AND P1.EXAMCOURSECD = '" + examcourseCd + "' ");
        }
        stb.append("         AND P1.TESTSUBCLASSCD != 'A' ");
        stb.append("     GROUP BY ");
        stb.append("         P1.TESTSUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" , T_SCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         S1.RECEPTNO, ");
        stb.append("         S1.TESTSUBCLASSCD, ");
        stb.append("         S1.SCORE2 ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_SCORE_DAT S1 ");
        stb.append("     WHERE ");
        stb.append("         S1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND S1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND S1.TESTDIV = '" + testdiv + "' ");
        stb.append("         AND S1.TESTSUBCLASSCD != 'A' ");
        stb.append("         AND S1.SCORE2 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" , T_PERFECT2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         P1.TESTSUBCLASSCD, ");
        stb.append("         P1.PERFECT ");
        stb.append("     FROM ");
        stb.append("         T_PERFECT P1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'T3' AS TESTSUBCLASSCD, ");
        stb.append("         SUM(P1.PERFECT) AS PERFECT ");
        stb.append("     FROM ");
        stb.append("         T_PERFECT P1 ");
        stb.append("     WHERE ");
        stb.append("         P1.TESTSUBCLASSCD NOT IN ('3','4') ");
        stb.append("     HAVING ");
        stb.append("         COUNT(P1.TESTSUBCLASSCD) >= 3 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'T5' AS TESTSUBCLASSCD, ");
        stb.append("         SUM(P1.PERFECT) AS PERFECT ");
        stb.append("     FROM ");
        stb.append("         T_PERFECT P1 ");
        stb.append("     HAVING ");
        stb.append("         COUNT(P1.TESTSUBCLASSCD) >= 5 ");
        stb.append(" ) ");
        stb.append(" , T_SCORE2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         S1.RECEPTNO, ");
        stb.append("         S1.TESTSUBCLASSCD, ");
        stb.append("         S1.SCORE2 ");
        stb.append("     FROM ");
        stb.append("         T_SCORE S1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         S1.RECEPTNO, ");
        stb.append("         'T3' AS TESTSUBCLASSCD, ");
        stb.append("         SUM(S1.SCORE2) AS SCORE2 ");
        stb.append("     FROM ");
        stb.append("         T_SCORE S1 ");
        stb.append("     WHERE ");
        stb.append("         S1.TESTSUBCLASSCD NOT IN ('3','4') ");
        stb.append("     GROUP BY ");
        stb.append("         S1.RECEPTNO ");
        stb.append("     HAVING ");
        stb.append("         COUNT(S1.TESTSUBCLASSCD) >= 3 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         S1.RECEPTNO, ");
        stb.append("         'T5' AS TESTSUBCLASSCD, ");
        stb.append("         SUM(S1.SCORE2) AS SCORE2 ");
        stb.append("     FROM ");
        stb.append("         T_SCORE S1 ");
        stb.append("     GROUP BY ");
        stb.append("         S1.RECEPTNO ");
        stb.append("     HAVING ");
        stb.append("         COUNT(S1.TESTSUBCLASSCD) >= 5 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     S1.TESTSUBCLASSCD, ");
        stb.append("     COUNT(RD_007.RECEPTNO) AS EXAM_CNT, ");
        stb.append("     MAX(P1.PERFECT) AS PERFECT, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN B1.SEX = '1' THEN S1.SCORE2 END)),1),4,1) AS SCORE_AVG_SEX1, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN B1.SEX = '2' THEN S1.SCORE2 END)),1),4,1) AS SCORE_AVG_SEX2, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(S1.SCORE2)),1),4,1) AS SCORE_AVG, ");
        stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(S1.SCORE2)),1),4,1) AS SCORE_STDDEV, ");
        stb.append("     MAX(S1.SCORE2) AS SCORE_MAX, ");
        stb.append("     MIN(S1.SCORE2) AS SCORE_MIN, ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN S1.SCORE2 END)),1),4,1) AS PASS_AVG, ");
            stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '001' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN S1.SCORE2 END) AS TOKUSHOU_MIN1, ");
            stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '002' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN S1.SCORE2 END) AS TOKUSHOU_MIN2, ");
            stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '003' AND (RD_007.REMARK1 = '" + examcourseCd + "' AND RD_007.REMARK7 = '1' OR RD_007.REMARK2 = '" + examcourseCd + "' AND RD_007.REMARK8 = '1') THEN S1.SCORE2 END) AS TOKUSHOU_MIN3 ");
        } else { //全体合計の表
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(CASE WHEN S1.TESTSUBCLASSCD IN ('3','4','T5') AND (RD_007.REMARK7 = '1') OR S1.TESTSUBCLASSCD NOT IN ('3','4','T5') AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN S1.SCORE2 END)),1),4,1) AS PASS_AVG, ");
            stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '001' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN S1.SCORE2 END) AS TOKUSHOU_MIN1, ");
            stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '002' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN S1.SCORE2 END) AS TOKUSHOU_MIN2, ");
            stb.append("     MIN(CASE WHEN RD_015.TOKUSHOU_CD = '003' AND (RD_007.REMARK7 = '1' OR RD_007.REMARK8 = '1') THEN S1.SCORE2 END) AS TOKUSHOU_MIN3 ");
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD_007 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("          ON R1.ENTEXAMYEAR = RD_007.ENTEXAMYEAR ");
        stb.append("         AND R1.APPLICANTDIV = RD_007.APPLICANTDIV ");
        stb.append("         AND R1.TESTDIV = RD_007.TESTDIV ");
        stb.append("         AND R1.EXAM_TYPE = RD_007.EXAM_TYPE ");
        stb.append("         AND R1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     INNER JOIN T_SCORE2 S1 ON S1.RECEPTNO = RD_007.RECEPTNO ");
        stb.append("     INNER JOIN T_PERFECT2 P1 ON P1.TESTSUBCLASSCD = S1.TESTSUBCLASSCD ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             RD_015.RECEPTNO, ");
        stb.append("             RD_015.REMARK1 AS TOKUSHOU_CD "); //特奨(1,2,3種)
        stb.append("         FROM ");
        stb.append("             ENTEXAM_RECEPT_DETAIL_DAT RD_015 ");
        stb.append("         WHERE ");
        stb.append("             RD_015.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("             AND RD_015.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("             AND RD_015.TESTDIV = '" + testdiv + "' ");
        stb.append("             AND RD_015.EXAM_TYPE = '1' ");
        stb.append("             AND RD_015.SEQ = '015' ");
        stb.append("             AND RD_015.REMARK1 IS NOT NULL ");
        stb.append("     ) RD_015 ON RD_015.RECEPTNO = RD_007.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     RD_007.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RD_007.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RD_007.TESTDIV = '" + testdiv + "' ");
        stb.append("     AND RD_007.SEQ = '007' ");
        if (!EXAMCOURSECD_ALL.equals(examcourseCd)) { //各コースの表
            stb.append("     AND (RD_007.REMARK1 = '" + examcourseCd + "' OR RD_007.REMARK2 = '" + examcourseCd + "' AND VALUE(RD_007.REMARK7, '') != '1') ");
        }
        stb.append(" GROUP BY ");
        stb.append("     S1.TESTSUBCLASSCD ");
        return stb.toString();
    }

    private class PrintData1 {
        final String _examcourseCd;
        final String _testdiv;
        final String _hopeCourseDiv;
        String _recruit;
        String _hope1;
        String _hope2;
        String _hope3;
        String _exam1;
        String _exam2;
        String _exam3;
        String _pass1;
        String _pass2;
        String _pass3;
        String _ent1;
        String _ent2;
        String _ent3;

        public PrintData1(final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv
        		) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
        }

		public PrintData1(
                final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String recruit,
                final String hope1,
                final String hope2,
                final String hope3,
                final String exam1,
                final String exam2,
                final String exam3,
                final String pass1,
                final String pass2,
                final String pass3,
                final String ent1,
                final String ent2,
                final String ent3
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _recruit = recruit;
            _hope1 = hope1;
            _hope2 = hope2;
            _hope3 = hope3;
            _exam1 = exam1;
            _exam2 = exam2;
            _exam3 = exam3;
            _pass1 = pass1;
            _pass2 = pass2;
            _pass3 = pass3;
            _ent1 = ent1;
            _ent2 = ent2;
            _ent3 = ent3;
        }
    }

    private class PrintData2 {
        final String _examcourseCd;
        final String _testdiv;
        final String _hopeCourseDiv;
        final String _div;
        String _hope1;
        String _hope2;
        String _hope3;
        String _exam1;
        String _exam2;
        String _exam3;
        String _pass1;
        String _pass2;
        String _pass3;
        String _ent1;
        String _ent2;
        String _ent3;

        public PrintData2(final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String div
        		) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _div = div;
        }

        public PrintData2(
                final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String div,
                final String hope1,
                final String hope2,
                final String hope3,
                final String exam1,
                final String exam2,
                final String exam3,
                final String pass1,
                final String pass2,
                final String pass3,
                final String ent1,
                final String ent2,
                final String ent3
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _div = div;
            _hope1 = hope1;
            _hope2 = hope2;
            _hope3 = hope3;
            _exam1 = exam1;
            _exam2 = exam2;
            _exam3 = exam3;
            _pass1 = pass1;
            _pass2 = pass2;
            _pass3 = pass3;
            _ent1 = ent1;
            _ent2 = ent2;
            _ent3 = ent3;
        }
    }

    private class PrintData3 {
        final String _examcourseCd;
        final String _testdiv;
        final String _hopeCourseDiv;
        final String _senbatu;
        String _hope1;
        String _hope2;
        String _hope3;
        String _exam1;
        String _exam2;
        String _exam3;
        String _pass1;
        String _pass2;
        String _pass3;
        String _ent1;
        String _ent2;
        String _ent3;

        public PrintData3(final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String senbatu
        		) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _senbatu = senbatu;
        }

        public PrintData3(
                final String examcourseCd,
                final String testdiv,
                final String hopeCourseDiv,
                final String senbatu,
                final String hope1,
                final String hope2,
                final String hope3,
                final String exam1,
                final String exam2,
                final String exam3,
                final String pass1,
                final String pass2,
                final String pass3,
                final String ent1,
                final String ent2,
                final String ent3
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _hopeCourseDiv = hopeCourseDiv;
            _senbatu = senbatu;
            _hope1 = hope1;
            _hope2 = hope2;
            _hope3 = hope3;
            _exam1 = exam1;
            _exam2 = exam2;
            _exam3 = exam3;
            _pass1 = pass1;
            _pass2 = pass2;
            _pass3 = pass3;
            _ent1 = ent1;
            _ent2 = ent2;
            _ent3 = ent3;
        }
    }

    private class PrintData4 {
        final String _examcourseCd;
        final String _testdiv;
        final String _testsubclasscd;
        final String _examCnt;
        final String _perfect;
        final String _scoreAvgSex1;
        final String _scoreAvgSex2;
        final String _scoreAvg;
        final String _scoreStddev;
        final String _scoreMax;
        final String _scoreMin;
        final String _passAvg;
        final String _tokushouMin1;
        final String _tokushouMin2;
        final String _tokushouMin3;

        public PrintData4(
                final String examcourseCd,
                final String testdiv,
                final String testsubclasscd,
                final String examCnt,
                final String perfect,
                final String scoreAvgSex1,
                final String scoreAvgSex2,
                final String scoreAvg,
                final String scoreStddev,
                final String scoreMax,
                final String scoreMin,
                final String passAvg,
                final String tokushouMin1,
                final String tokushouMin2,
                final String tokushouMin3
        ) {
            _examcourseCd = examcourseCd;
            _testdiv = testdiv;
            _testsubclasscd = testsubclasscd;
            _examCnt = examCnt;
            _perfect = perfect;
            _scoreAvgSex1 = scoreAvgSex1;
            _scoreAvgSex2 = scoreAvgSex2;
            _scoreAvg = scoreAvg;
            _scoreStddev = scoreStddev;
            _scoreMax = scoreMax;
            _scoreMin = scoreMin;
            _passAvg = passAvg;
            _tokushouMin1 = tokushouMin1;
            _tokushouMin2 = tokushouMin2;
            _tokushouMin3 = tokushouMin3;
        }
    }

    private class TestdivMst {
        final String _testdiv;
        final String _testdivAbbv;
        final String _testdivDate;
        final int _line;

        public TestdivMst(
                final String testdiv,
                final String testdivAbbv,
                final String testdivDate,
                final int line
        ) {
            _testdiv = testdiv;
            _testdivAbbv = testdivAbbv;
            _testdivDate = testdivDate;
            _line = line;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77378 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _loginDate;
        private final String _loginYear;
        final String _applicantdivName;
        final String _nendo;
        private final Map _examcourseMap;
        private final Map _testdivMstMap;
        private final Map _sexMap;
        private final Map _testsubclassMap;
        final List _hopeCourseDivList = new ArrayList();
        final List _divList = new ArrayList();
        final List _senbatuList = new ArrayList();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_entexamyear)) + "年度";
            _examcourseMap = getExamcourseMap(db2);
            _testdivMstMap = getTestdivMstMap(db2);
            _sexMap = getSexMap(db2);
            _testsubclassMap = getTestsubclassMap(db2);
            _hopeCourseDivList.add(HOPE_COURSE_ALL);
            _hopeCourseDivList.add(HOPE_COURSE1);
            _hopeCourseDivList.add(HOPE_COURSE2);
            _divList.add(DIV_TOKUSHOU);
            _divList.add(DIV_TOKUSHOU1);
            _divList.add(DIV_TOKUSHOU2);
            _divList.add(DIV_TOKUSHOU3);
            _divList.add(DIV_KIKOKU);
            _divList.add(DIV_RYOSEI);
            _senbatuList.add(SENBATU_C1);
            _senbatuList.add(SENBATU_C2);
            _senbatuList.add(SENBATU_C3);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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

        private Map getExamcourseMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getExamcourseSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examcourseCd = rs.getString("EXAMCOURSECD");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");

                    retMap.put(examcourseCd, examcourseName);
                }
                retMap.put(EXAMCOURSECD_ALL, "全体合計");
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getExamcourseSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     EXAMCOURSECD, ");
            stb.append("     EXAMCOURSE_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     EXAMCOURSECD ");
            return stb.toString();
        }

        private Map getTestdivMstMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getTestdivMstSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int line = 0;
                while (rs.next()) {
                    final String testdiv = rs.getString("TESTDIV");
                    final String testdivAbbv = rs.getString("TESTDIV_ABBV");
                    final String testdivDate = rs.getString("TESTDIV_DATE");
                    line++;
                    if (line >= 5) continue;

                    final TestdivMst testdivMst = new TestdivMst(testdiv, testdivAbbv, testdivDate, line);
                    retMap.put(testdiv, testdivMst);
                }
                retMap.put(TESTDIV_NOBE, new TestdivMst(TESTDIV_NOBE, "延人数", null, 5));
                retMap.put(TESTDIV_JITSU, new TestdivMst(TESTDIV_JITSU, "実人数", null, 6));
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getTestdivMstSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS TESTDIV, ");
            stb.append("     ABBV1 AS TESTDIV_ABBV, ");
            stb.append("     NAMESPARE1 AS TESTDIV_DATE ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _entexamyear + "' ");
            stb.append("     AND NAMECD1 = 'L004' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private Map getSexMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSexSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");

                    retMap.put(sex, sexName);
                }
                retMap.put("3", "合計");
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getSexSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS SEX, ");
            stb.append("     NAME1 AS SEX_NAME ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'Z002' ");
            stb.append(" ORDER BY ");
            stb.append("     SEX ");
            return stb.toString();
        }

        private Map getTestsubclassMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getTestsubclassSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String testsubclassName = rs.getString("TESTSUBCLASS_NAME");

                    retMap.put(testsubclasscd, testsubclassName);
                }
                retMap.put(TESTSUBCLASSCD3, "3教科");
                retMap.put(TESTSUBCLASSCD5, "5教科");
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getTestsubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS TESTSUBCLASSCD, ");
            stb.append("     NAME1 AS TESTSUBCLASS_NAME ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _entexamyear + "' ");
            stb.append("     AND NAMECD1 = 'L009' ");
            stb.append("     AND NAMECD2 != 'A' "); //面接除く
            stb.append("     AND NAME1 IS NOT NULL "); //高校科目
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

    }
}

// eof

