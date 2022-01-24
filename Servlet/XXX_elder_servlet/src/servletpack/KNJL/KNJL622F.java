/*
 * $Id: a4685cc3051676c25a1d229ba9adbc295363fa9d $
 *
 * 作成日: 2020/01/15
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL622F {

    private static final Log log = LogFactory.getLog(KNJL622F.class);

    private static final String PRINT_FORM_1 = "KNJL622F_1.frm";
    private static final String PRINT_FORM_2 = "KNJL622F_2.frm";
    private static final String PRINT_FORM_3 = "KNJL622F_3.frm";
    private static final String PRINT_FORM_4 = "KNJL622F_4.frm";
    private static final String PRINT_FORM_5 = "KNJL622F_5.frm";
    private static final String PRINT_FORM_6 = "KNJL622F_6.frm";

    private static final int MAX_LINE = 20;

    private static final String BUNRI_1_CD = "1002";
    private static final String BUNRI_2_CD = "1003";
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

            //入試結果
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
        printOut1(db2, svf); //仕様書No22.の資料1
        printOut2(db2, svf); //仕様書No22.の資料2
        printOut3(db2, svf); //仕様書No22.の資料3
        printOut4(db2, svf); //仕様書No22.の資料4,5
        printOut5(db2, svf); //仕様書No.22の資料6～18
        if ("5".equals(_param._testdiv)) {
            printOut6(db2, svf); //仕様書No.22の資料19～22
        }
        printOut7(db2, svf); //仕様書No.22の資料23
    }

    public void printOut1(final DB2UDB db2, final Vrw32alp svf) {
        Map p11RPTMap = new LinkedMap();
        Map p12RPTMap = new LinkedMap();
        Map p13RPTMap = new LinkedMap();
        Map p21RPTMap = new LinkedMap();
        Map p22RPTMap = new LinkedMap();
        Map p23RPTMap = new LinkedMap();
        setP1_RPTMap(db2, svf, p11RPTMap, p12RPTMap, p13RPTMap, p21RPTMap, p22RPTMap, p23RPTMap);
        Map p11TotalMap = new LinkedMap();
        Map p12TotalMap = new LinkedMap();
        Map p13TotalMap = new LinkedMap();
        Map p21TotalMap = new LinkedMap();
        Map p22TotalMap = new LinkedMap();
        Map p23TotalMap = new LinkedMap();
        setP1_TotalMap(db2, svf, p11TotalMap, p12TotalMap, p13TotalMap, p21TotalMap, p22TotalMap, p23TotalMap);
        List stdCntList = new ArrayList();
        setP1_StdCntList(db2, stdCntList);
        if (p11RPTMap.size() > 0 || p12RPTMap.size() > 0 || p13RPTMap.size() > 0 || p21RPTMap.size() > 0 || p22RPTMap.size() > 0 || p23RPTMap.size() > 0) {
            svf.VrSetForm(PRINT_FORM_1, 1);
            //タイトル
            final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
            svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 志願者評定一覧表");
            prtfmtP1(db2, svf, p11RPTMap, p11TotalMap, stdCntList, 1, 1);
            prtfmtP1(db2, svf, p12RPTMap, p12TotalMap, stdCntList, 1, 2);
            prtfmtP1(db2, svf, p13RPTMap, p13TotalMap, stdCntList, 1, 3);
            prtfmtP1(db2, svf, p21RPTMap, p21TotalMap, stdCntList, 2, 1);
            prtfmtP1(db2, svf, p22RPTMap, p22TotalMap, stdCntList, 2, 2);
            prtfmtP1(db2, svf, p23RPTMap, p23TotalMap, stdCntList, 2, 3);
            log.debug("P1_OUT:"+_hasData);
            svf.VrEndPage();
        }
    }
    private void prtfmtP1(final DB2UDB db2, final Vrw32alp svf, final Map outRPTMap, final Map outPastTotalMap, final List stdCntList, final int prtcol, final int prtrow) {

        //各表のタイトル(人数)
        final String ninzu = (String)stdCntList.get((prtcol-1) * 3 + prtrow - 1); //※0ベース指定
        Map shInfMap = (Map)_param._SHMap.get(String.valueOf(String.valueOf(prtcol)));
        svf.VrsOut("EXAM_TYPE" + ((prtcol-1) * 3 + prtrow), getSiganCourseMapValueFromIndex(prtrow) + " " + (String)shInfMap.get("NAME1") + ("".equals(ninzu) ? "" : "("+ ninzu +"人)"));

        List KamokuList = new ArrayList() ;
        KamokuList.add("1"); //国語
        KamokuList.add("2"); //社会
        KamokuList.add("3"); //数学
        KamokuList.add("4"); //理科
        KamokuList.add("5"); //英語 ※SQLで指定しているコード
        int prtRow = 1;
        for (Iterator itr = KamokuList.iterator();itr.hasNext();) {
            final String kmk = (String)itr.next();
            if (outRPTMap.containsKey(kmk)) {
                PrintDataP1RPT prtwk = (PrintDataP1RPT)outRPTMap.get(kmk);
                svf.VrsOutn("RATE"+((prtcol-1) * 3 + prtrow)+"_1", prtRow, prtwk._rpt1);
                svf.VrsOutn("RATE"+((prtcol-1) * 3 + prtrow)+"_2", prtRow, prtwk._rpt2);
                svf.VrsOutn("RATE"+((prtcol-1) * 3 + prtrow)+"_3", prtRow, prtwk._rpt3);
                svf.VrsOutn("RATE"+((prtcol-1) * 3 + prtrow)+"_4", prtRow, prtwk._rpt4);
                svf.VrsOutn("RATE"+((prtcol-1) * 3 + prtrow)+"_5", prtRow, prtwk._rpt5);
                svf.VrsOutn("AVE"+((prtcol-1) * 3 + prtrow), prtRow, prtwk._avgval);
                _hasData = true;
            }
            prtRow++;
        }
        List setyList = new ArrayList();
        setyList.add(String.valueOf(Integer.parseInt(_param._entexamyear)));
        setyList.add(String.valueOf(Integer.parseInt(_param._entexamyear) - 1));
        setyList.add(String.valueOf(Integer.parseInt(_param._entexamyear) - 2));
        setyList.add(String.valueOf(Integer.parseInt(_param._entexamyear) - 3));
        int putAvgCol = 0;
        for (Iterator itr = setyList.iterator();itr.hasNext();) {
            final String yStr = (String)itr.next();
            if (putAvgCol > 0) {
                svf.VrsOut("LAST_YEAR_NAME" + ((prtcol-1) * 3 + prtrow) + "_" + putAvgCol, createDateStrShortFmt(db2, yStr));
            }
            if (outPastTotalMap.containsKey(yStr)) {
                final String outStr = (String)outPastTotalMap.get(yStr);
                if (putAvgCol == 0) {
                    svf.VrsOut("TOTAL_AVE" + ((prtcol-1) * 3 + prtrow), outStr);
                } else {
                    svf.VrsOut("LAST_TOTAL_AVE" + ((prtcol-1) * 3 + prtrow) + "_" + putAvgCol, outStr);
                }
                _hasData = true;
            }
            putAvgCol++;
        }

    }
    private String createDateStrShortFmt(final DB2UDB db2, final String yStr) {
        String retStr = "";
        String hStr = KNJ_EditDate.gengouAlphabetMark(db2, Integer.parseInt(yStr));
        String[] cStr = KNJ_EditDate.tate_format4(db2, yStr+"-05-01");  //念のため、H31->R01と表示されて欲しいため、5/1指定で行う。
        if (cStr.length > 1) {
            if ("元".equals(cStr[1])) {
                retStr = hStr + "01";
            } else {
                retStr = hStr + cStr[1];
            }
        }
        return retStr;
    }

    private String getSiganCourseMapValueFromIndex(int idx) {
        int cnt=0;
        String retStr = "";
        for (Iterator ite = _param._SiganCourseMap.keySet().iterator();ite.hasNext();) {
            final String kStr = (String)ite.next();
            cnt++;
            if (cnt == idx) {
                retStr = (String)_param._SiganCourseMap.get(kStr);
            }
        }
        return retStr;
    }

    private void setP1_RPTMap(final DB2UDB db2, final Vrw32alp svf, final Map p11RPTMap, final Map p12RPTMap, final Map p13RPTMap, final Map p21RPTMap, final Map p22RPTMap, final Map p23RPTMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //専併,コース分類毎にデータを取得
            for (Iterator ite = _param._SHMap.keySet().iterator();ite.hasNext();) {
                final String SHCode = (String)ite.next();
                log.debug("P1_PARAM_SET_SHCODE:"+SHCode);
                int putCnt = 0;
                for (Iterator itr = _param._SiganCourseMap.keySet().iterator();itr.hasNext();) {
                    final String srchCourse = (String)itr.next();
                    log.debug("P1_PARAM_SET_SRCHCOURSE:"+srchCourse);
                    final String sql = getP1RPTQuery(SHCode, srchCourse);
                    log.debug("P1 sql:"+sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        log.debug("   P1_loop_IN");
                        final String kamokucd = rs.getString("KAMOKUCD");
                        final String rpt1 = rs.getString("RPT1");
                        final String rpt2 = rs.getString("RPT2");
                        final String rpt3 = rs.getString("RPT3");
                        final String rpt4 = rs.getString("RPT4");
                        final String rpt5 = rs.getString("RPT5");
                        final String avgval = rs.getString("AVGVAL");
                        PrintDataP1RPT addwk = new PrintDataP1RPT(kamokucd, rpt1, rpt2, rpt3, rpt4, rpt5, avgval);
                        if ("1".equals(SHCode)) { //専願
                            if (putCnt == 0) {  //文理
                                p11RPTMap.put(kamokucd, addwk);
                            } else if (putCnt == 1) {  //ソレイユ・看護医療・子ども教育
                                p12RPTMap.put(kamokucd, addwk);
                            } else if (putCnt == 2) {  //エトワール
                                p13RPTMap.put(kamokucd, addwk);
                            }
                        } else if ("2".equals(SHCode)) { //併願
                            if (putCnt == 0) {  //文理
                                p21RPTMap.put(kamokucd, addwk);
                            } else if (putCnt == 1) {  //ソレイユ・看護医療・子ども教育
                                p22RPTMap.put(kamokucd, addwk);
                            } else if (putCnt == 2) {  //エトワール
                                p23RPTMap.put(kamokucd, addwk);
                            }
                        }
                    }
                    putCnt++;
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }
    private String getP1RPTQuery(final String SHCode, final String srchCourse) {
        StringBuffer stb = new StringBuffer();
        //当年度の内申点の点数毎の人数を算出する。
        stb.append(" WITH NAISIN_DATA AS ( ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '1' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT01 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" WHERE ");
        stb.append("   ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '2' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT02 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" WHERE ");
        stb.append("   ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '3' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT03 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" WHERE ");
        stb.append("   ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '4' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT04 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" WHERE ");
        stb.append("   ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '5' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT09 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" WHERE ");
        stb.append("   ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   D1.KAMOKUCD, ");
        stb.append("   SUM(CASE WHEN D1.CONF_RPT = '1' THEN 1 ELSE 0 END) AS RPT1, ");
        stb.append("   SUM(CASE WHEN D1.CONF_RPT = '2' THEN 1 ELSE 0 END) AS RPT2, ");
        stb.append("   SUM(CASE WHEN D1.CONF_RPT = '3' THEN 1 ELSE 0 END) AS RPT3, ");
        stb.append("   SUM(CASE WHEN D1.CONF_RPT = '4' THEN 1 ELSE 0 END) AS RPT4, ");
        stb.append("   SUM(CASE WHEN D1.CONF_RPT = '5' THEN 1 ELSE 0 END) AS RPT5, ");
        stb.append("   CAST((CAST(AVG(CAST(D1.CONF_RPT AS double)) AS DECIMAL(4,2)) + 0.05) AS DECIMAL(3,1)) AS AVGVAL ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   LEFT JOIN NAISIN_DATA D1 ");
        stb.append("     ON D1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND D1.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("    AND T2.SEQ = '001' ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT T3 ");
        stb.append("     ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T3.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("    AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");  // 高校
        stb.append("    AND T1.TESTDIV = '" + _param._testdiv + "' ");            // 5:1次入試 or 7:1.5次入試
        stb.append("    AND T1.SHDIV = '" + SHCode + "' ");   //1:専願、2:併願
        stb.append("    AND T2.REMARK8 || T2.REMARK9 || T2.REMARK10 IN ( " + srchCourse + " ) ");  // 2002:文理、2003,2004,2006:進学ソレイユ,看護医療,こども教育、2005:進学エトワール
        stb.append("    AND VALUE(T3.JUDGEDIV, '') <> '4' ");
        stb.append("    AND KAMOKUCD IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("   D1.KAMOKUCD ");
        return stb.toString();
    }
    private void setP1_TotalMap(final DB2UDB db2, final Vrw32alp svf, final Map p11TotalMap, final Map p12TotalMap, final Map p13TotalMap, final Map p21TotalMap, final Map p22TotalMap, final Map p23TotalMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //専併,コース分類毎にデータを取得
            for (Iterator ite = _param._SHMap.keySet().iterator();ite.hasNext();) {
                final String SHCode = (String)ite.next();
                int putCnt = 0;
                for (Iterator itr = _param._SiganCourseMap.keySet().iterator();itr.hasNext();) {
                    final String srchCourse = (String)itr.next();
                    final String sql = getP1TotalQuery(SHCode, srchCourse);
                    log.debug("P1 sql:"+sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String entexamyear = rs.getString("ENTEXAMYEAR");
                        final String avgval = rs.getString("AVGVAL");
                        if ("1".equals(SHCode)) { //専願
                            if (putCnt == 0) {  //文理
                                p11TotalMap.put(entexamyear, avgval);
                            } else if (putCnt == 1) {  //ソレイユ・看護医療・子ども教育
                                p12TotalMap.put(entexamyear, avgval);
                            } else if (putCnt == 2) {  //エトワール
                                p13TotalMap.put(entexamyear, avgval);
                            }
                        } else if ("2".equals(SHCode)) { //併願
                            if (putCnt == 0) {  //文理
                                p21TotalMap.put(entexamyear, avgval);
                            } else if (putCnt == 1) {  //ソレイユ・看護医療・子ども教育
                                p22TotalMap.put(entexamyear, avgval);
                            } else if (putCnt == 2) {  //エトワール
                                p23TotalMap.put(entexamyear, avgval);
                            }
                        }
                    }
                    putCnt++;
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }
    private String getP1TotalQuery(final String SHCode, final String srchCourse) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH NAISIN_DATA AS ( ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '1' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT01 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '2' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT02 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '3' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT03 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '4' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT04 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" UNION ");
        stb.append(" select ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   EXAMNO, ");
        stb.append("   '5' AS KAMOKUCD, ");
        stb.append("   CONFIDENTIAL_RPT09 AS CONF_RPT ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("    D1.ENTEXAMYEAR, ");
        stb.append("    CAST((CAST(AVG(CAST(D1.CONF_RPT AS double)) AS DECIMAL(4,2)) + 0.05) AS DECIMAL(3,1)) AS AVGVAL ");
        stb.append(" from ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   LEFT JOIN NAISIN_DATA D1 ");
        stb.append("     ON D1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND D1.EXAMNO = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("    AND T2.SEQ = '001' ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT T3 ");
        stb.append("     ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T3.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("    T1.APPLICANTDIV = '" + _param._applicantdiv + "' "); // 高校
        stb.append("    AND T1.TESTDIV = '" + _param._testdiv + "' ");            // 5:1次入試 or 7:1.5次入試
        stb.append("    AND T1.SHDIV = '" + SHCode + "' "); // 1:専願、2:併願
        stb.append("    AND T2.REMARK8 || T2.REMARK9 || T2.REMARK10 IN (" + srchCourse + ") "); // (2001,2002:文理、2003,2004,2006:進学ソレイユ、看護医療、こども教育、2005:進学エトワール
        stb.append("    AND VALUE(T3.JUDGEDIV, '') <> '4' ");
        stb.append("    AND KAMOKUCD IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("   D1.ENTEXAMYEAR ");
        stb.append(" ORDER BY ");
        stb.append("   D1.ENTEXAMYEAR DESC ");
        return stb.toString();
    }
    private void setP1_StdCntList(final DB2UDB db2, final List stdCntList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //専併,コース分類毎にデータを取得
            for (Iterator ite = _param._SHMap.keySet().iterator();ite.hasNext();) {
                final String SHCode = (String)ite.next();
                for (Iterator itr = _param._SiganCourseMap.keySet().iterator();itr.hasNext();) {
                    final String srchCourse = (String)itr.next();
                    String sql = getStdCntSql(SHCode, srchCourse);
                    log.debug("P1 sql:"+sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    String listSetStr = "";
                    while (rs.next()) {
                        listSetStr = rs.getString("CNT");
                    }
                    stdCntList.add(listSetStr); //SQLで何も取れなくても必ず入るようにする。
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }
    private String getStdCntSql(final String SHCode, final String srchCourse) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   COUNT(T1.EXAMNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ");
        stb.append("     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("    AND T2.SEQ = '001' ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT T3 ");
        stb.append("     ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T3.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("    AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' "); // 高校
        stb.append("    AND T1.TESTDIV = '" + _param._testdiv + "' ");  // 5:1次入試 or 7:1.5次入試
        stb.append("    AND T1.SHDIV = '" + SHCode + "' ");  // 1:専願、2:併願
        stb.append("    AND T2.REMARK8 || T2.REMARK9 || T2.REMARK10 IN (" + srchCourse + ") ");  // 2001,2002:文理、2003,2004,2006:進学ソレイユ、看護医療、こども教育、2005:進学エトワール
        stb.append("    AND VALUE(T3.JUDGEDIV, '') <> '4' ");

        return stb.toString();
    }

    public void printOut2(final DB2UDB db2, final Vrw32alp svf) {
        Map p2NaibuMap;
        Map p2GaibuAllMap = getP2GaibuAllMap(db2);
        Map p2GaibuDtlMap = getP2GaibuDtlMap(db2);
        if ("5".equals(_param._testdiv)) {
            p2NaibuMap = getP2NaibuMap(db2);
        } else {
            //空のままで作成
            p2NaibuMap = new LinkedMap();
        }
          prtfmtP2(db2, svf, p2GaibuAllMap, p2GaibuDtlMap, p2NaibuMap);
        log.debug("P2_OUT:"+_hasData);
    }
    private void prtfmtP2(final DB2UDB db2, final Vrw32alp svf, final Map p2GaibuAllMap, final Map p2GaibuDtlMap, final Map p2NaibuMap) {
        svf.VrSetForm(PRINT_FORM_2, 1);
        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
        svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 教科成績");
        svf.VrsOut("TITLE1", "教科別平均(" + _param._testdivName + "(外部))");
        svf.VrsOut("TITLE2", "教科成績(" + _param._testdivName + "(外部))");

        int prtGACnt = 0;
        //左端のタイトル文字列の年度はきちんと表示されないといけないので、キーとなる年度のリストを作成する。
        final List kGAtbl = new ArrayList();
        for (int cnt = 0;cnt < 3;cnt++) { //2年前まで作成。最後の差は固定で設定。
            final String yStr = String.valueOf(Integer.parseInt(_param._entexamyear) - cnt);
            kGAtbl.add(yStr);
        }

        PrintData2GA nowDat = null;
        PrintData2GA lastDat = null;
        for (Iterator itga = kGAtbl.iterator();itga.hasNext();) {
            final String yStr = (String)itga.next();
            prtGACnt++;
            if (p2GaibuAllMap.containsKey(yStr)) {
                final PrintData2GA prtwk = (PrintData2GA)p2GaibuAllMap.get(yStr);
                if (prtGACnt > 3) { //フォームで用意された出力行数
                    continue;
                }
                if (prtGACnt == 0) {
                    nowDat = prtwk;
                }
                if (prtGACnt == 1) {
                    lastDat = prtwk;
                }
                svf.VrsOutn("NENDO", prtGACnt, yStr + "年度");
                svf.VrsOutn("AVE1_1", prtGACnt, prtwk._avg_Kokugo);
                svf.VrsOutn("AVE1_2", prtGACnt, prtwk._avg_Syakai);
                svf.VrsOutn("AVE1_3", prtGACnt, prtwk._avg_Sugaku);
                svf.VrsOutn("AVE1_4", prtGACnt, prtwk._avg_Rika);
                svf.VrsOutn("AVE1_5", prtGACnt, prtwk._avg_Eigo);
                svf.VrsOutn("AVE1_6", prtGACnt, prtwk._k3_Avg);
                svf.VrsOutn("AVE1_7", prtGACnt, prtwk._k5_Avg);
                _hasData = true;
            }
        }
        if (nowDat != null && lastDat != null) {
            PrintData2GA prtwk = nowDat.minus(lastDat);
            svf.VrsOutn("NENDO", prtGACnt, "差(対前年)");
            svf.VrsOutn("AVE1_1", prtGACnt, prtwk._avg_Kokugo);
            svf.VrsOutn("AVE1_2", prtGACnt, prtwk._avg_Syakai);
            svf.VrsOutn("AVE1_3", prtGACnt, prtwk._avg_Sugaku);
            svf.VrsOutn("AVE1_4", prtGACnt, prtwk._avg_Rika);
            svf.VrsOutn("AVE1_5", prtGACnt, prtwk._avg_Eigo);
            svf.VrsOutn("AVE1_6", prtGACnt, prtwk._k3_Avg);
            svf.VrsOutn("AVE1_7", prtGACnt, prtwk._k5_Avg);
            _hasData = true;
        }

        //左端のタイトル文字列はきちんと表示されないといけないので、キーとなる文理/その他,3/5教科,専併(cname_Id + "_" + testdiv1 + "_" + shdiv)のリストを作成する。
        final List kGDtbl = new ArrayList();
        kGDtbl.add("1_3_0");   //文理3教科全体
        kGDtbl.add("1_3_1");   //文理3教科専願
        kGDtbl.add("1_3_2");   //文理3教科併願
        kGDtbl.add("1_4_0");   //文理5教科全体
        kGDtbl.add("1_4_1");   //文理5教科専願
        kGDtbl.add("1_4_2");   //文理5教科併願
        kGDtbl.add("2_3_0");   //その他3教科全体
        kGDtbl.add("2_3_1");   //その他3教科専願
        kGDtbl.add("2_3_2");   //その他3教科専願
        kGDtbl.add("2_4_0");   //その他5教科全体
        kGDtbl.add("2_4_1");   //その他5教科専願
        kGDtbl.add("2_4_2");   //その他5教科併願

        int prtGDCnt = 0;
        for (Iterator itgd = kGDtbl.iterator();itgd.hasNext();) {
            final String cdStr = (String)itgd.next();
            final String[] cdsplt = StringUtils.split(cdStr, '_');
            prtGDCnt++;
            if (p2GaibuDtlMap.containsKey(cdStr)) {
                final PrintData2GD prtwk = (PrintData2GD)p2GaibuDtlMap.get(cdStr);
                svf.VrsOutn("NUM1", prtGDCnt, prtwk._cnt_Kazu);
                svf.VrsOutn("AVE2_1", prtGDCnt, prtwk._avg_Kokugo); //国語
                if (!"1".equals(cdsplt[1])) { //3教科でないなら出力
                    svf.VrsOutn("AVE2_2", prtGDCnt, prtwk._avg_Syakai); //社会
                    svf.VrsOutn("AVE2_4", prtGDCnt, prtwk._avg_Rika);   //理科
                }
                svf.VrsOutn("AVE2_3", prtGDCnt, prtwk._avg_Sugaku); //数学
                svf.VrsOutn("AVE2_5", prtGDCnt, prtwk._avg_Eigo);   //英語
                svf.VrsOutn("AVE2", prtGDCnt, prtwk._total_Avg);    //3・5教科合計 平均
                svf.VrsOutn("MAX2", prtGDCnt, prtwk._total_Max);    //3・5教科合計 最高
                svf.VrsOutn("MIN2", prtGDCnt, prtwk._total_Min);    //3・5教科合計 最低
                _hasData = true;
            }
        }

        //内部生の表については入試区分に5:一次入試が指定された時のみ出力する。
        if ("5".equals(_param._testdiv)) {
            //全体/文理I/文理IIの出力位置が決まっているので、キーとなるリストを作成する。
            final List kNtbl = new ArrayList();
            kNtbl.add("99");  //全体
            kNtbl.add(BUNRI_1_CD);  //文理I
            kNtbl.add(BUNRI_2_CD);  //文理II

            int prtNCnt = 0;
            for (Iterator itn = kNtbl.iterator();itn.hasNext();) {
                final String cdStr = (String)itn.next();
                if (p2NaibuMap.containsKey(cdStr)) {
                    final PrintData2N prtwk = (PrintData2N)p2NaibuMap.get(cdStr);

                    svf.VrsOutn("NUM2", prtNCnt+1, prtwk._stdcnt);
                    svf.VrsOutn("AVE3_1", (prtNCnt)*3+1, prtwk._score1_Avg);
                    svf.VrsOutn("AVE3_1", (prtNCnt)*3+2, prtwk._score1_Max);
                    svf.VrsOutn("AVE3_1", (prtNCnt)*3+3, prtwk._score1_Min);
                    if (prtNCnt == 1) { //文理Iの時だけ出力
                        svf.VrsOutn("AVE3_2", (prtNCnt)*3+1, prtwk._score2_Avg);
                        svf.VrsOutn("AVE3_2", (prtNCnt)*3+2, prtwk._score2_Max);
                        svf.VrsOutn("AVE3_2", (prtNCnt)*3+3, prtwk._score2_Min);
                        svf.VrsOutn("AVE3_4", (prtNCnt)*3+1, prtwk._score4_Avg);
                        svf.VrsOutn("AVE3_4", (prtNCnt)*3+2, prtwk._score4_Max);
                        svf.VrsOutn("AVE3_4", (prtNCnt)*3+3, prtwk._score4_Min);
                        svf.VrsOutn("AVE3_8", (prtNCnt)*3+1, prtwk._total4_Avg);
                        svf.VrsOutn("AVE3_8", (prtNCnt)*3+2, prtwk._total4_Max);
                        svf.VrsOutn("AVE3_8", (prtNCnt)*3+3, prtwk._total4_Min);
                    }
                    svf.VrsOutn("AVE3_3", (prtNCnt)*3+1, prtwk._score3_Avg);
                    svf.VrsOutn("AVE3_3", (prtNCnt)*3+2, prtwk._score3_Max);
                    svf.VrsOutn("AVE3_3", (prtNCnt)*3+3, prtwk._score3_Min);
                    svf.VrsOutn("AVE3_5", (prtNCnt)*3+1, prtwk._score5_Avg);
                    svf.VrsOutn("AVE3_5", (prtNCnt)*3+2, prtwk._score5_Max);
                    svf.VrsOutn("AVE3_5", (prtNCnt)*3+3, prtwk._score5_Min);
                    svf.VrsOutn("AVE3_6", (prtNCnt)*3+1, prtwk._total3_Avg);
                    svf.VrsOutn("AVE3_6", (prtNCnt)*3+2, prtwk._total3_Max);
                    svf.VrsOutn("AVE3_6", (prtNCnt)*3+3, prtwk._total3_Min);
                    if (prtNCnt > 0) {  //全体以外は出力
                        svf.VrsOutn("AVE3_7", (prtNCnt)*3+1, prtwk._total3_Lastavg);
                        svf.VrsOutn("AVE3_7", (prtNCnt)*3+2, prtwk._total3_Lastmax);
                        svf.VrsOutn("AVE3_7", (prtNCnt)*3+3, prtwk._total3_Lastmin);
                    }
                    _hasData = true;
                }
                prtNCnt++;
            }

        }
        if (p2GaibuAllMap.size() > 0 || p2GaibuDtlMap.size() > 0 || p2NaibuMap.size() > 0) {
            svf.VrEndPage();
        }
    }
    private Map getP2GaibuAllMap(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        final String sql = getP2GaibuAllSql();
        log.debug("P2 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String avg_Kokugo = rs.getString("AVG_KOKUGO");
                final String avg_Syakai = rs.getString("AVG_SYAKAI");
                final String avg_Sugaku = rs.getString("AVG_SUGAKU");
                final String avg_Rika = rs.getString("AVG_RIKA");
                final String avg_Eigo = rs.getString("AVG_EIGO");
                final String k3_Avg = rs.getString("K3_AVG");
                final String k5_Avg = rs.getString("K5_AVG");
                PrintData2GA addwk = new PrintData2GA(entexamyear, avg_Kokugo, avg_Syakai, avg_Sugaku, avg_Rika, avg_Eigo, k3_Avg, k5_Avg);
                retMap.put(entexamyear, addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }
    private final String getP2GaibuAllSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.ENTEXAMYEAR,  ");
        stb.append("   DECIMAL(ROUND(AVG(SC1.SCORE+0.0),1), 5, 1) AS AVG_KOKUGO, ");
        stb.append("   DECIMAL(ROUND(AVG(SC2.SCORE+0.0),1), 5, 1) AS AVG_SYAKAI, ");
        stb.append("   DECIMAL(ROUND(AVG(SC3.SCORE+0.0),1), 5, 1) AS AVG_SUGAKU, ");
        stb.append("   DECIMAL(ROUND(AVG(SC4.SCORE+0.0),1), 5, 1) AS AVG_RIKA, ");
        stb.append("   DECIMAL(ROUND(AVG(SC5.SCORE+0.0),1), 5, 1) AS AVG_EIGO, ");
        stb.append("   DECIMAL(ROUND(AVG(REC.TOTAL3+0.0),1), 5, 1) AS K3_AVG, ");
        stb.append("   DECIMAL(ROUND(AVG(REC.TOTAL4+0.0),1), 5, 1) AS K5_AVG ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV IN ('5', '7') ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC1 ");
        stb.append("     ON SC1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC1.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC1.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC2 ");
        stb.append("     ON SC2.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC2.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC2.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC2.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC2.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC3 ");
        stb.append("     ON SC3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC3.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC3.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC3.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC3.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC4 ");
        stb.append("     ON SC4.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC4.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC4.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC4.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC4.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC4.TESTSUBCLASSCD = '4' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC5 ");
        stb.append("     ON SC5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC5.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC5.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC5.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC5.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC5.TESTSUBCLASSCD = '5' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR >= '" + (Integer.parseInt(_param._entexamyear) - 2) + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" GROUP BY ");
        stb.append("   T1.ENTEXAMYEAR ");
        stb.append(" ORDER BY ");
        stb.append("   T1.ENTEXAMYEAR DESC ");
        return stb.toString();
    }
    private Map getP2GaibuDtlMap(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        final String sql = getP2GaibuDtlSql();
        log.debug("P2 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String cname_Id = rs.getString("CNAME_ID");
                final String cname = rs.getString("CNAME");
                final String testdiv1 = rs.getString("TESTDIV1");
                final String testcnt = rs.getString("TESTCNT");
                final String shdiv = rs.getString("SHDIV");
                final String sh_Name = rs.getString("SH_NAME");
                final String cnt_Kazu = rs.getString("CNT_KAZU");
                final String avg_Kokugo = rs.getString("AVG_KOKUGO");
                final String avg_Syakai = rs.getString("AVG_SYAKAI");
                final String avg_Sugaku = rs.getString("AVG_SUGAKU");
                final String avg_Rika = rs.getString("AVG_RIKA");
                final String avg_Eigo = rs.getString("AVG_EIGO");
                final String total_Avg = rs.getString("TOTAL_AVG");
                final String total_Max = rs.getString("TOTAL_MAX");
                final String total_Min = rs.getString("TOTAL_MIN");
                PrintData2GD addwk = new PrintData2GD(entexamyear, cname_Id, cname, testdiv1, testcnt, shdiv, sh_Name, cnt_Kazu, avg_Kokugo, avg_Syakai, avg_Sugaku, avg_Rika, avg_Eigo, total_Avg, total_Max, total_Min);
                retMap.put(cname_Id + "_" + testdiv1 + "_" + shdiv, addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }
    private final String getP2GaibuDtlSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH GET_ORG_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.ENTEXAMYEAR, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN 1 ELSE 2 END AS CNAME_ID, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN '文理' ELSE 'その他' END AS CNAME, ");
        stb.append("   T1.TESTDIV1, ");
        stb.append("   L005.NAME1 AS TESTCNT, ");
        stb.append("   '0'AS SHDIV, ");
        stb.append("   '' AS SH_NAME, ");
        stb.append("   COUNT(T1.EXAMNO) AS CNT_KAZU, ");
        stb.append("   DECIMAL(ROUND(AVG(SC1.SCORE+0.0),1), 5, 1) AS AVG_KOKUGO, ");
        stb.append("   DECIMAL(ROUND(AVG(SC2.SCORE+0.0),1), 5, 1) AS AVG_SYAKAI, ");
        stb.append("   DECIMAL(ROUND(AVG(SC3.SCORE+0.0),1), 5, 1) AS AVG_SUGAKU, ");
        stb.append("   DECIMAL(ROUND(AVG(SC4.SCORE+0.0),1), 5, 1) AS AVG_RIKA, ");
        stb.append("   DECIMAL(ROUND(AVG(SC5.SCORE+0.0),1), 5, 1) AS AVG_EIGO, ");
        stb.append("   DECIMAL(ROUND(AVG(VALUE(REC.TOTAL4, REC.TOTAL3) + 0.0),1), 5, 1) AS TOTAL_AVG, ");
        stb.append("   MAX(VALUE(REC.TOTAL4,REC.TOTAL3)) AS TOTAL_MAX, ");
        stb.append("   MIN(VALUE(REC.TOTAL4,REC.TOTAL3)) AS TOTAL_MIN ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV IN ('5', '7') ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC1 ");
        stb.append("     ON SC1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC1.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC1.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC2 ");
        stb.append("     ON SC2.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC2.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC2.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC2.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC2.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC3 ");
        stb.append("     ON SC3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC3.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC3.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC3.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC3.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC4 ");
        stb.append("     ON SC4.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC4.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC4.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC4.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC4.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC4.TESTSUBCLASSCD = '4' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC5 ");
        stb.append("     ON SC5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC5.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC5.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC5.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC5.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC5.TESTSUBCLASSCD = '5' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3_001 ");
        stb.append("     ON T3_001.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND T3_001.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND T3_001.EXAMNO         = REC.EXAMNO ");
        stb.append("    AND T3_001.SEQ            = '001' ");
        stb.append("   LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");
        stb.append("     ON ECM1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND ECM1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND ECM1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND ECM1.COURSECD       = T3_001.REMARK8 ");
        stb.append("    AND ECM1.MAJORCD        = T3_001.REMARK9 ");
        stb.append("    AND ECM1.EXAMCOURSECD = T3_001.REMARK10 ");
        stb.append("   LEFT JOIN NAME_MST L005 ");
        stb.append("     ON L005.NAMECD1 = 'L005' ");
        stb.append("    AND L005.NAMECD2 = T1.TESTDIV1 ");
        stb.append("   LEFT JOIN NAME_MST L006 ");
        stb.append("     ON L006.NAMECD1 = 'L006' ");
        stb.append("    AND L006.NAMECD2 = T1.SHDIV ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" GROUP BY ");
        stb.append("   T1.ENTEXAMYEAR, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN 1 ELSE 2 END, ");
        stb.append("   T1.TESTDIV1, ");
        stb.append("   L005.NAME1, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN '文理' ELSE 'その他' END ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("   T1.ENTEXAMYEAR, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN 1 ELSE 2 END AS CNAME_ID, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN '文理' ELSE 'その他' END AS CNAME, ");
        stb.append("   T1.TESTDIV1, ");
        stb.append("   L005.NAME1 AS TESTCNT, ");
        stb.append("   T1.SHDIV, ");
        stb.append("   L006.NAME1 AS SH_NAME, ");
        stb.append("   COUNT(T1.EXAMNO) AS CNT_KAZU, ");
        stb.append("   DECIMAL(ROUND(AVG(SC1.SCORE+0.0),1), 5, 1) AS AVG_KOKUGO, ");
        stb.append("   DECIMAL(ROUND(AVG(SC2.SCORE+0.0),1), 5, 1) AS AVG_SYAKAI, ");
        stb.append("   DECIMAL(ROUND(AVG(SC3.SCORE+0.0),1), 5, 1) AS AVG_SUGAKU, ");
        stb.append("   DECIMAL(ROUND(AVG(SC4.SCORE+0.0),1), 5, 1) AS AVG_RIKA, ");
        stb.append("   DECIMAL(ROUND(AVG(SC5.SCORE+0.0),1), 5, 1) AS AVG_EIGO, ");
        stb.append("   DECIMAL(ROUND(AVG(CASE WHEN T1.TESTDIV1 = '4' THEN REC.TOTAL4 WHEN T1.TESTDIV1 = '3' THEN REC.TOTAL3 ELSE 0.0 END + 0.0),1), 5, 1) AS TOTAL_AVG, ");
        stb.append("   MAX(VALUE(REC.TOTAL4,REC.TOTAL3)) AS TOTAL_MAX, ");
        stb.append("   MIN(VALUE(REC.TOTAL4,REC.TOTAL3)) AS TOTAL_MIN ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV IN ('5', '7') ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC1 ");
        stb.append("     ON SC1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC1.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC1.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC2 ");
        stb.append("     ON SC2.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC2.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC2.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC2.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC2.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC3 ");
        stb.append("     ON SC3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC3.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC3.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC3.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC3.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC4 ");
        stb.append("     ON SC4.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC4.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC4.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC4.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC4.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC4.TESTSUBCLASSCD = '4' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC5 ");
        stb.append("     ON SC5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC5.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC5.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC5.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC5.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC5.TESTSUBCLASSCD = '5' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3_001 ");
        stb.append("     ON T3_001.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND T3_001.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND T3_001.EXAMNO         = REC.EXAMNO ");
        stb.append("    AND T3_001.SEQ            = '001' ");
        stb.append("   LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");
        stb.append("     ON ECM1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND ECM1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND ECM1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND ECM1.COURSECD       = T3_001.REMARK8 ");
        stb.append("    AND ECM1.MAJORCD        = T3_001.REMARK9 ");
        stb.append("    AND ECM1.EXAMCOURSECD = T3_001.REMARK10 ");
        stb.append("   LEFT JOIN NAME_MST L005 ");
        stb.append("     ON L005.NAMECD1 = 'L005' ");
        stb.append("    AND L005.NAMECD2 = T1.TESTDIV1 ");
        stb.append("   LEFT JOIN NAME_MST L006 ");
        stb.append("     ON L006.NAMECD1 = 'L006' ");
        stb.append("    AND L006.NAMECD2 = T1.SHDIV ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" GROUP BY ");
        stb.append("   T1.ENTEXAMYEAR, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN 1 ELSE 2 END, ");
        stb.append("   T1.TESTDIV1, ");
        stb.append("   T1.SHDIV, ");
        stb.append("   L005.NAME1, ");
        stb.append("   L006.NAME1, ");
        stb.append("   CASE WHEN T3_001.REMARK10 IN ('2001', '2002') THEN '文理' ELSE 'その他' END ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   GET_ORG_DATA ");
        stb.append(" ORDER BY ");
        stb.append("   ENTEXAMYEAR DESC, ");
        stb.append("   CNAME_ID, ");
        stb.append("   TESTDIV1 ");
        return stb.toString();
    }
    private Map getP2NaibuMap(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        final String sql = getP2NaibuSql();
        log.debug("P2 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String fscourse_Typecd = rs.getString("FSCOURSE_TYPECD");
                final String fscourse_Typename = rs.getString("FSCOURSE_TYPENAME");
                final String stdcnt = rs.getString("STDCNT");
                final String score1_Avg = sishagonyu(rs.getString("SCORE1_AVG"));
                final String score1_Max = rs.getString("SCORE1_MAX");
                final String score1_Min = rs.getString("SCORE1_MIN");
                final String score2_Avg = sishagonyu(rs.getString("SCORE2_AVG"));
                final String score2_Max = rs.getString("SCORE2_MAX");
                final String score2_Min = rs.getString("SCORE2_MIN");
                final String score3_Avg = sishagonyu(rs.getString("SCORE3_AVG"));
                final String score3_Max = rs.getString("SCORE3_MAX");
                final String score3_Min = rs.getString("SCORE3_MIN");
                final String score4_Avg = sishagonyu(rs.getString("SCORE4_AVG"));
                final String score4_Max = rs.getString("SCORE4_MAX");
                final String score4_Min = rs.getString("SCORE4_MIN");
                final String score5_Avg = sishagonyu(rs.getString("SCORE5_AVG"));
                final String score5_Max = rs.getString("SCORE5_MAX");
                final String score5_Min = rs.getString("SCORE5_MIN");
                final String total3_Avg = sishagonyu(rs.getString("TOTAL3_AVG"));
                final String total3_Max = rs.getString("TOTAL3_MAX");
                final String total3_Min = rs.getString("TOTAL3_MIN");
                final String total4_Avg = sishagonyu(rs.getString("TOTAL4_AVG"));
                final String total4_Max = rs.getString("TOTAL4_MAX");
                final String total4_Min = rs.getString("TOTAL4_MIN");
                final String total3_Lastavg = sishagonyu(rs.getString("TOTAL3_LASTAVG"));
                final String total3_Lastmax = rs.getString("TOTAL3_LASTMAX");
                final String total3_Lastmin = rs.getString("TOTAL3_LASTMIN");
                final PrintData2N addwk = new PrintData2N(fscourse_Typecd, fscourse_Typename, stdcnt, score1_Avg, score1_Max, score1_Min, score2_Avg, score2_Max, score2_Min,
                                                            score3_Avg, score3_Max, score3_Min, score4_Avg, score4_Max, score4_Min, score5_Avg, score5_Max, score5_Min,
                                                            total3_Avg, total3_Max, total3_Min, total4_Avg, total4_Max, total4_Min, total3_Lastavg, total3_Lastmax, total3_Lastmin);
                retMap.put(fscourse_Typecd, addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }
    private static String sishagonyu(final String v) {
        if (!NumberUtils.isNumber(v)) {
            return null;
        }
        return new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    private final String getP2NaibuSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH LASTYEAR_DAT AS ( ");
        //昨年度の集計(全体)
        stb.append(" SELECT ");
        stb.append("   '99' AS FSCOURSE_TYPECD, ");
        stb.append("   AVG(REC.TOTAL3 * 1.0) AS TOTAL3_LASTAVG, ");
        stb.append("   MAX(REC.TOTAL3) AS TOTAL3_LASTMAX, ");
        stb.append("   MIN(REC.TOTAL3) AS TOTAL3_LASTMIN ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV = '6' ");   //内部生のみ
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("     ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("    AND BD041.SEQ = '041' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + (Integer.parseInt(_param._entexamyear) - 1) + "' ");
        stb.append("   AND T1.APPLICANTDIV ='"+_param._applicantdiv+"' ");
        stb.append("   AND BD041.REMARK1 IS NOT NULL ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" UNION ALL ");
        //昨年度の集計(個別)
        stb.append(" SELECT ");
        stb.append("   BD041.REMARK3 AS FSCOURSE_TYPECD, ");
        stb.append("   AVG(REC.TOTAL3 * 1.0) AS TOTAL3_LASTAVG, ");
        stb.append("   MAX(REC.TOTAL3) AS TOTAL3_LASTMAX, ");
        stb.append("   MIN(REC.TOTAL3) AS TOTAL3_LASTMIN ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV = '6' ");   //内部生のみ
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("     ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("    AND BD041.SEQ = '041' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + (Integer.parseInt(_param._entexamyear) - 1) + "' ");
        stb.append("   AND T1.APPLICANTDIV ='"+_param._applicantdiv+"' ");
        stb.append("   AND BD041.REMARK1 IS NOT NULL ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" GROUP BY ");
        stb.append("   BD041.REMARK3 ");
        stb.append(" ), THISYEAR_DAT AS ( ");
        //今年度の集計(全体)
        stb.append(" SELECT ");
        stb.append("   '99' AS FSCOURSE_TYPECD, ");
        stb.append("   '' AS FSCOURSE_TYPENAME, ");
        stb.append("   COUNT(T1.EXAMNO) AS STDCNT, ");
        stb.append("   AVG(SC1.SCORE * 1.0) AS SCORE1_AVG, ");
        stb.append("   MAX(SC1.SCORE) AS SCORE1_MAX, ");
        stb.append("   MIN(SC1.SCORE) AS SCORE1_MIN, ");
        stb.append("   AVG(SC2.SCORE * 1.0) AS SCORE2_AVG, ");
        stb.append("   MAX(SC2.SCORE) AS SCORE2_MAX, ");
        stb.append("   MIN(SC2.SCORE) AS SCORE2_MIN, ");
        stb.append("   AVG(SC3.SCORE * 1.0) AS SCORE3_AVG, ");
        stb.append("   MAX(SC3.SCORE) AS SCORE3_MAX, ");
        stb.append("   MIN(SC3.SCORE) AS SCORE3_MIN, ");
        stb.append("   AVG(SC4.SCORE * 1.0) AS SCORE4_AVG, ");
        stb.append("   MAX(SC4.SCORE) AS SCORE4_MAX, ");
        stb.append("   MIN(SC4.SCORE) AS SCORE4_MIN, ");
        stb.append("   AVG(SC5.SCORE * 1.0) AS SCORE5_AVG, ");
        stb.append("   MAX(SC5.SCORE) AS SCORE5_MAX, ");
        stb.append("   MIN(SC5.SCORE) AS SCORE5_MIN, ");
        stb.append("   AVG(REC.TOTAL3 * 1.0) AS TOTAL3_AVG, ");
        stb.append("   MAX(REC.TOTAL3) AS TOTAL3_MAX, ");
        stb.append("   MIN(REC.TOTAL3) AS TOTAL3_MIN, ");
        stb.append("   AVG(REC.TOTAL4 * 1.0) AS TOTAL4_AVG, ");
        stb.append("   MAX(REC.TOTAL4) AS TOTAL4_MAX, ");
        stb.append("   MIN(REC.TOTAL4) AS TOTAL4_MIN ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV = '6' ");   //内部生のみ
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("     ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("    AND BD041.SEQ = '041' ");
        stb.append("   LEFT JOIN COURSECODE_MST CM ");
        stb.append("     ON CM.COURSECODE = BD041.REMARK3 ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC1 ");
        stb.append("     ON SC1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC1.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC1.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC2 ");
        stb.append("     ON SC2.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC2.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC2.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC2.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC2.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC3 ");
        stb.append("     ON SC3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC3.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC3.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC3.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC3.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC4 ");
        stb.append("     ON SC4.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC4.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC4.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC4.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC4.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC4.TESTSUBCLASSCD = '4' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC5 ");
        stb.append("     ON SC5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC5.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC5.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC5.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC5.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC5.TESTSUBCLASSCD = '5' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV ='" + _param._applicantdiv + "' ");
        stb.append("   AND BD041.REMARK1 IS NOT NULL ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" UNION ALL ");
        //今年度の集計(個別)
        stb.append(" SELECT ");
        stb.append("   BD041.REMARK3 AS FSCOURSE_TYPECD, ");
        stb.append("   CASE WHEN BD041.REMARK3 IN ('" + BUNRI_1_CD + "', '" + BUNRI_2_CD + "') THEN CM.COURSECODENAME ");
        stb.append("        ELSE 'その他' END AS FSCOURSE_TYPENAME, ");
        stb.append("   COUNT(T1.EXAMNO) AS STDCNT, ");
        stb.append("   AVG(SC1.SCORE * 1.0) AS SCORE1_AVG, ");
        stb.append("   MAX(SC1.SCORE) AS SCORE1_MAX, ");
        stb.append("   MIN(SC1.SCORE) AS SCORE1_MIN, ");
        stb.append("   AVG(SC2.SCORE * 1.0) AS SCORE2_AVG, ");
        stb.append("   MAX(SC2.SCORE) AS SCORE2_MAX, ");
        stb.append("   MIN(SC2.SCORE) AS SCORE2_MIN, ");
        stb.append("   AVG(SC3.SCORE * 1.0) AS SCORE3_AVG, ");
        stb.append("   MAX(SC3.SCORE) AS SCORE3_MAX, ");
        stb.append("   MIN(SC3.SCORE) AS SCORE3_MIN, ");
        stb.append("   AVG(SC4.SCORE * 1.0) AS SCORE4_AVG, ");
        stb.append("   MAX(SC4.SCORE) AS SCORE4_MAX, ");
        stb.append("   MIN(SC4.SCORE) AS SCORE4_MIN, ");
        stb.append("   AVG(SC5.SCORE * 1.0) AS SCORE5_AVG, ");
        stb.append("   MAX(SC5.SCORE) AS SCORE5_MAX, ");
        stb.append("   MIN(SC5.SCORE) AS SCORE5_MIN, ");
        stb.append("   AVG(REC.TOTAL3 * 1.0) AS TOTAL3_AVG, ");
        stb.append("   MAX(REC.TOTAL3) AS TOTAL3_MAX, ");
        stb.append("   MIN(REC.TOTAL3) AS TOTAL3_MIN, ");
        stb.append("   AVG(REC.TOTAL4 * 1.0) AS TOTAL4_AVG, ");
        stb.append("   MAX(REC.TOTAL4) AS TOTAL4_MAX, ");
        stb.append("   MIN(REC.TOTAL4) AS TOTAL4_MIN ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND REC.TESTDIV = '6' ");   //内部生のみ
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("     ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("    AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("    AND BD041.SEQ = '041' ");
        stb.append("   LEFT JOIN COURSECODE_MST CM ");
        stb.append("     ON CM.COURSECODE = BD041.REMARK3 ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC1 ");
        stb.append("     ON SC1.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC1.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC1.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC1.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC1.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC1.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC2 ");
        stb.append("     ON SC2.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC2.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC2.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC2.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC2.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC2.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC3 ");
        stb.append("     ON SC3.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC3.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC3.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC3.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC3.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC3.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC4 ");
        stb.append("     ON SC4.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC4.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC4.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC4.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC4.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC4.TESTSUBCLASSCD = '4' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT SC5 ");
        stb.append("     ON SC5.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
        stb.append("    AND SC5.APPLICANTDIV   = REC.APPLICANTDIV ");
        stb.append("    AND SC5.TESTDIV        = REC.TESTDIV ");
        stb.append("    AND SC5.EXAM_TYPE      = REC.EXAM_TYPE ");
        stb.append("    AND SC5.RECEPTNO       = REC.RECEPTNO ");
        stb.append("    AND SC5.TESTSUBCLASSCD = '5' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV ='" + _param._applicantdiv + "' ");
        stb.append("   AND BD041.REMARK1 IS NOT NULL ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" GROUP BY ");
        stb.append("   BD041.REMARK3, ");
        stb.append("   CM.COURSECODENAME ");
        stb.append(" ) ");
        //昨年度分と今年度分を結合
        stb.append(" SELECT ");
        stb.append("   TA.*, ");
        stb.append("   TB.TOTAL3_LASTAVG, ");
        stb.append("   TB.TOTAL3_LASTMAX, ");
        stb.append("   TB.TOTAL3_LASTMIN ");
        stb.append(" FROM ");
        stb.append("   THISYEAR_DAT TA ");
        stb.append("   LEFT JOIN LASTYEAR_DAT TB ");
        stb.append("     ON TB.FSCOURSE_TYPECD = TA.FSCOURSE_TYPECD ");
        return stb.toString();
    }

    public void printOut3(final DB2UDB db2, final Vrw32alp svf) {
        List<Object> P3List = new ArrayList();
        setP3List(db2, svf, P3List);
        svf.VrSetForm(PRINT_FORM_3, 1);
        if (P3List.size() > 0) {
              for (final List<Object> l : getPageList(P3List, 25)) {
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度入試 部活推薦受験者 出願者");
                  prtfmtP3(db2, svf, l);
                  svf.VrEndPage();
                  log.debug("P3_OUT:"+_hasData);
              }
        }
    }
    private void prtfmtP3(final DB2UDB db2, final Vrw32alp svf, final List outList) {
        int putCnt = 1;
        for (Iterator ite = outList.iterator();ite.hasNext();) {
            final PrintDataP3 prtwk =(PrintDataP3)ite.next();
            svf.VrsOutn("NO", putCnt, prtwk._rnum);
            svf.VrsOutn("NEXAMNO", putCnt, prtwk._examno);
            svf.VrsOutn("DIV", putCnt, prtwk._abbv2);
            final int clen = KNJ_EditEdit.getMS932ByteLength(prtwk._clubname);
            final String cfield = clen > 30 ? "3" : (clen > 20 ? "2" : "1");
            svf.VrsOutn("CLUB_NAME"+cfield, putCnt, prtwk._clubname);
            final int nlen = KNJ_EditEdit.getMS932ByteLength(prtwk._name);
            final String nfield = nlen > 30 ? "3" : (nlen > 20 ? "2" : "1");
            svf.VrsOutn("NAME"+nfield, putCnt, prtwk._name);
            final int fslen = KNJ_EditEdit.getMS932ByteLength(prtwk._finschool_Name);
            final String fsfield = fslen > 32 ? "3" : (fslen > 24 ? "2" : "1");
            svf.VrsOutn("FINSCHOOL_NAME"+fsfield, putCnt, prtwk._finschool_Name);
            svf.VrsOutn("RATE1", putCnt, prtwk._crpt1);
            svf.VrsOutn("RATE2", putCnt, prtwk._crpt2);
            svf.VrsOutn("RATE3", putCnt, prtwk._crpt3);
            svf.VrsOutn("RATE4", putCnt, prtwk._crpt4);
            svf.VrsOutn("RATE5", putCnt, prtwk._crpt9);
            svf.VrsOutn("RATE_TOTAL", putCnt, prtwk._conf_Total);
            svf.VrsOutn("MOCK_SCORE1", putCnt, prtwk._jituryoku_Total3);
            svf.VrsOutn("MOCK_SCORE2", putCnt, prtwk._jituryoku_Total4);
            svf.VrsOutn("MOCK_AVE1", putCnt, prtwk._jituryoku_Avg3);
            svf.VrsOutn("MOCK_AVE2", putCnt, prtwk._jituryoku_Avg4);
            if (!"".equals(StringUtils.defaultString(prtwk._test_Total3, ""))) {
                svf.VrsOutn("TOTAL_SCORE1", putCnt, prtwk._test_Total3);
            }
            if (!"".equals(StringUtils.defaultString(prtwk._test_Total4, ""))) {
                svf.VrsOutn("TOTAL_SCORE2", putCnt, prtwk._test_Total4);
            }
            svf.VrsOutn("MATCH_HIST", putCnt, prtwk._remark1);
            putCnt++;
            _hasData = true;
        }
    }
    private void setP3List(final DB2UDB db2, final Vrw32alp svf, List P3List) {
        final String sql = getP3Query();
        log.debug("P3 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String rnum = rs.getString("RNUM");
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String abbv1 = rs.getString("ABBV1");
                final String abbv2 = rs.getString("ABBV2");
                final String clubname = rs.getString("CLUBNAME");
                final String name = rs.getString("NAME");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String crpt1 = rs.getString("CRPT1");
                final String crpt2 = rs.getString("CRPT2");
                final String crpt3 = rs.getString("CRPT3");
                final String crpt4 = rs.getString("CRPT4");
                final String crpt9 = rs.getString("CRPT9");
                final String conf_Total = rs.getString("CONF_TOTAL");
                final String jituryoku_Total3 = rs.getString("JITURYOKU_TOTAL3");
                final String jituryoku_Avg3 = rs.getString("JITURYOKU_AVG3");
                final String jituryoku_Total4 = rs.getString("JITURYOKU_TOTAL4");
                final String jituryoku_Avg4 = rs.getString("JITURYOKU_AVG4");
                final String test_Total3 = rs.getString("TEST_TOTAL3");
                final String test_Total4 = rs.getString("TEST_TOTAL4");
                final String remark1 = rs.getString("REMARK1");
                PrintDataP3 addwk = new PrintDataP3(rnum, examno, receptno, abbv1, abbv2, clubname, name, finschool_Name, crpt1, crpt2, crpt3, crpt4, crpt9, conf_Total, jituryoku_Total3, jituryoku_Avg3, jituryoku_Total4, jituryoku_Avg4, test_Total3, test_Total4, remark1);
                P3List.add(addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }
    private String getP3Query() {
        StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("   ROW_NUMBER() OVER() AS RNUM, ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   REC.RECEPTNO, ");
        stb.append("   L006.ABBV1, ");
        stb.append("   L025.ABBV2, ");
        stb.append("   T7.REMARK1 AS CLUBNAME, ");
        stb.append("   T1.NAME, ");
        stb.append("   FM.FINSCHOOL_NAME, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT01 AS CRPT1, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT02 AS CRPT2, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT03 AS CRPT3, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT04 AS CRPT4, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT09 AS CRPT9, ");
        stb.append("   (CPRT.CONFIDENTIAL_RPT01 + CPRT.CONFIDENTIAL_RPT02 + CPRT.CONFIDENTIAL_RPT03 + CPRT.CONFIDENTIAL_RPT04 + CPRT.CONFIDENTIAL_RPT09) AS CONF_TOTAL, ");
        stb.append("   T4.REMARK1 AS JITURYOKU_TOTAL3, ");
        stb.append("   T4.REMARK2 AS JITURYOKU_AVG3, ");
        stb.append("   T4.REMARK3 AS JITURYOKU_TOTAL4, ");
        stb.append("   T4.REMARK4 AS JITURYOKU_AVG4, ");
        stb.append("   REC.TOTAL3 AS TEST_TOTAL3, ");
        stb.append("   REC.TOTAL4 AS TEST_TOTAL4, ");
        stb.append("   CPRT.REMARK1 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("   LEFT JOIN NAME_MST L006 ");
        stb.append("     ON L006.NAMECD1 = 'L006' ");
        stb.append("    AND L006.NAMECD2 = T1.SHDIV ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ");
        stb.append("     ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T3.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND T3.SEQ = '005' ");
        stb.append("   LEFT JOIN NAME_MST L025 ");
        stb.append("     ON L025.NAMECD1 = 'L025' ");
        stb.append("    AND L025.NAMECD2 = T3.REMARK2 ");
        stb.append("    AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("     ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CPRT ");
        stb.append("     ON CPRT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND CPRT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND CPRT.EXAMNO       = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
        stb.append("     ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T4.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND T4.SEQ = '020' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T7 ");
        stb.append("     ON T7.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T7.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T7.EXAMNO       = REC.EXAMNO ");
        stb.append("    AND T7.SEQ          = '004' ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if ("5".equals(_param._testdiv)) {
            stb.append("   AND T1.TESTDIV IN ('5', '6') ");
        } else {
            stb.append("   AND T1.TESTDIV = '" + _param._testdiv + "' ");
        }
        stb.append("   AND T3.REMARK2 IN ('21', '22', '23', '24', '30') ");
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   REC.RECEPTNO ");
        return stb.toString();
    }
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    public void printOut4(final DB2UDB db2, final Vrw32alp svf) {
        List<Object> P41List = new ArrayList();
        List<Object> P42List = new ArrayList();
        List<Object> P51List = new ArrayList();
        List<Object> P52List = new ArrayList();
        List<Object> P53List = new ArrayList();
        List<Object> P54List = new ArrayList();
        List<Object> P55List = new ArrayList();
        setP4P5List(db2, svf, P41List, P42List, P51List, P52List, P53List, P54List, P55List);
          if (P41List.size() > 0) {
              for (final List<Object> l : getPageList(P41List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 特別活動奨学生 出願者");
                  prtfmtP4P5(db2, svf, l, "P4_1");  //特別活動奨学生
                  svf.VrEndPage();
                  log.debug("P41_OUT:"+_hasData);
              }
          }
          if (P42List.size() > 0) {
              for (final List<Object> l : getPageList(P42List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 学業成績特別奨学生 出願者");
                  prtfmtP4P5(db2, svf, l, "P4_2");  //学業成績特別奨学生
                  svf.VrEndPage();
                  log.debug("P42_OUT:"+_hasData);
              }
          }
          if (P51List.size() > 0) {
              for (final List<Object> l : getPageList(P51List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 身体的に注意を要する生徒・健康チェック");
                  prtfmtP4P5(db2, svf, l, "P5_1");  //身体的に注意を要する生徒・健康チェック
                  svf.VrEndPage();
                  log.debug("P51_OUT:"+_hasData);
              }
          }
          if (P52List.size() > 0) {
              for (final List<Object> l : getPageList(P52List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 基準以下生・入試相談△扱い");
                  prtfmtP4P5(db2, svf, l, "P5_2");  //基準以下生・入試相談△扱い
                  svf.VrEndPage();
                  log.debug("P52_OUT:"+_hasData);
              }
        }
          if (P53List.size() > 0) {
              for (final List<Object> l : getPageList(P53List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 別室受験");
                  prtfmtP4P5(db2, svf, l, "P5_3");  //別室受験
                  svf.VrEndPage();
                  log.debug("P53_OUT:"+_hasData);
              }
        }
          if (P54List.size() > 0) {
              for (final List<Object> l : getPageList(P54List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 入試相談なし");
                  prtfmtP4P5(db2, svf, l, "P5_4");  //入試相談なし
                  svf.VrEndPage();
                  log.debug("P54_OUT:"+_hasData);
              }
        }
          if (P55List.size() > 0) {
              for (final List<Object> l : getPageList(P55List, 25)) {
                  svf.VrSetForm(PRINT_FORM_4, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 未受験・欠席");
                  prtfmtP4P5(db2, svf, l, "P5_5");  //未受験・欠席
                  svf.VrEndPage();
                  log.debug("P55_OUT:"+_hasData);
              }
        }
    }
    private void prtfmtP4P5(final DB2UDB db2, final Vrw32alp svf, final List outList, final String prtPattern) {
        int putCnt = 1;
        for (Iterator ite = outList.iterator();ite.hasNext();) {
            final PrintData45 prtwk =(PrintData45)ite.next();
            svf.VrsOutn("DIV1", putCnt, prtwk._sh_Name);
            svf.VrsOutn("NEXAMNO", putCnt, prtwk._examno);
            final int nlen = KNJ_EditEdit.getMS932ByteLength(prtwk._name);
            final String nfield = nlen > 30 ? "3" : (nlen > 20 ? "2" : "1");
            svf.VrsOutn("NAME"+nfield, putCnt, prtwk._name);
            final int fslen = KNJ_EditEdit.getMS932ByteLength(prtwk._finschool_Name);
            final String fsfield = fslen > 30 ? "3" : (fslen > 20 ? "2" : "1");
            svf.VrsOutn("FINSCHOOL_NAME"+fsfield, putCnt, prtwk._finschool_Name);
            svf.VrsOutn("RATE1", putCnt, prtwk._crpt1);
            svf.VrsOutn("RATE2", putCnt, prtwk._crpt2);
            svf.VrsOutn("RATE3", putCnt, prtwk._crpt3);
            svf.VrsOutn("RATE4", putCnt, prtwk._crpt4);
            svf.VrsOutn("RATE5", putCnt, prtwk._crpt9);
            svf.VrsOutn("RATE_TOTAL", putCnt, prtwk._conf_Total);
            if (!"P5_5".equals(prtPattern)) {
                svf.VrsOutn("SCORE1", putCnt, prtwk._score1);
                svf.VrsOutn("SCORE2", putCnt, prtwk._score2);
                svf.VrsOutn("SCORE3", putCnt, prtwk._score3);
                svf.VrsOutn("SCORE4", putCnt, prtwk._score4);
                svf.VrsOutn("SCORE5", putCnt, prtwk._score5);
                svf.VrsOutn("TOTAL_SCORE1", putCnt, prtwk._total3);
                svf.VrsOutn("TOTAL_SCORE2", putCnt, prtwk._total5);
            }
            if ("P5_4".equals(prtPattern)) {
                svf.VrsOutn("CONSULTATION", putCnt, prtwk._soudan2);
            } else {
                svf.VrsOutn("CONSULTATION", putCnt, prtwk._soudan1);
            }
            svf.VrsOutn("DIV2", putCnt, prtwk._shougaku_Kbn);
            svf.VrsOutn("MOCK_SCORE1", putCnt, prtwk._jituryoku_Total3);
            svf.VrsOutn("MOCK_SCORE2", putCnt, prtwk._jituryoku_Total4);
            svf.VrsOutn("MOCK_AVE1", putCnt, prtwk._jituryoku_Avg3);
            svf.VrsOutn("MOCK_AVE2", putCnt, prtwk._jituryoku_Avg4);
            final int splen = KNJ_EditEdit.getMS932ByteLength(prtwk._spjyouken);
            final String spfield = splen > 24 ? "3" : (splen > 18 ? "2" : "1");
            svf.VrsOutn("SPECIAL"+spfield, putCnt, prtwk._spjyouken);
            final int rlen = KNJ_EditEdit.getMS932ByteLength(prtwk._remark1);
            if (rlen > 30) {
                String[] spltStr = KNJ_EditEdit.get_token(prtwk._remark1, 30, 2);
                svf.VrsOutn("MATCH_HIST2", putCnt, spltStr[0]);
                svf.VrsOutn("MATCH_HIST3", putCnt, spltStr[1]);
            } else {
                svf.VrsOutn("MATCH_HIST1", putCnt, prtwk._remark1);
            }
            putCnt++;
            _hasData = true;
        }
    }
    private void setP4P5List(final DB2UDB db2, final Vrw32alp svf, List P41List,  List P42List, List P51List, List P52List, List P53List, List P54List, List P55List) {
        final String sql = getP4P5Sql();
        log.debug("P4 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String sh_Name = rs.getString("SH_NAME");
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String crpt1 = rs.getString("CRPT1");
                final String crpt2 = rs.getString("CRPT2");
                final String crpt3 = rs.getString("CRPT3");
                final String crpt4 = rs.getString("CRPT4");
                final String crpt9 = rs.getString("CRPT9");
                final String conf_Total = rs.getString("CONF_TOTAL");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String score4 = rs.getString("SCORE4");
                final String score5 = rs.getString("SCORE5");
                final String total3 = rs.getString("TOTAL3");
                final String total5 = rs.getString("TOTAL5");
                final String soudan1 = rs.getString("SOUDAN1");
                final String soudan2 = rs.getString("SOUDAN2");
                final String shougaku_Kbn = rs.getString("SHOUGAKU_KBN");
                final String jituryoku_Total3 = rs.getString("JITURYOKU_TOTAL3");
                final String jituryoku_Avg3 = rs.getString("JITURYOKU_AVG3");
                final String jituryoku_Total4 = rs.getString("JITURYOKU_TOTAL4");
                final String jituryoku_Avg4 = rs.getString("JITURYOKU_AVG4");
                final String spjyouken = rs.getString("SPJYOUKEN");
                final String test_Total3 = rs.getString("TEST_TOTAL3");
                final String test_Total4 = rs.getString("TEST_TOTAL4");
                final String remark1 = rs.getString("REMARK1");
                final PrintData45 addwk = new PrintData45(sh_Name, examno, receptno, name, finschool_Name, crpt1, crpt2, crpt3, crpt4, crpt9, conf_Total, score1, score2, score3, score4, score5, total3, total5, soudan1, soudan2, shougaku_Kbn, jituryoku_Total3, jituryoku_Avg3, jituryoku_Total4, jituryoku_Avg4, spjyouken, test_Total3, test_Total4, remark1);

                if ("P4_1".equals(StringUtils.defaultString(rs.getString("P4_1"), ""))) {
                    P41List.add(addwk);
                }
                if ("P4_2".equals(StringUtils.defaultString(rs.getString("P4_2"), ""))) {
                    P42List.add(addwk);
                }
                if ("P5_1".equals(StringUtils.defaultString(rs.getString("P5_1"), ""))) {
                    P51List.add(addwk);
                }
                if ("P5_2".equals(StringUtils.defaultString(rs.getString("P5_2"), ""))) {
                    P52List.add(addwk);
                }
                if ("P5_3".equals(StringUtils.defaultString(rs.getString("P5_3"), ""))) {
                    P53List.add(addwk);
                }
                if ("P5_4".equals(StringUtils.defaultString(rs.getString("P5_4"), ""))) {
                    P54List.add(addwk);
                }
                if ("P5_5".equals(StringUtils.defaultString(rs.getString("P5_5"), ""))) {
                    P55List.add(addwk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }
    private String getP4P5Sql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        // ここから指定区間内で、帳票の出力条件を情報として取得して関数を戻った所で帳票別にList分けしている(※これらの帳票は全て母集団、出力フォーマットが同じだったから。)。
        stb.append("   CASE WHEN T3.REMARK2 IN ('21', '22', '23', '24') THEN 'P4_1' ELSE '' END AS P4_1, ");  //仕様書No22.の資料4の1番目の帳票
        stb.append("   CASE WHEN T3.REMARK2 IN ('11', '12', '13') THEN 'P4_2' ELSE '' END AS P4_2, ");        //仕様書No22.の資料4の2番目の帳票
        stb.append("   CASE WHEN T3.REMARK6 = '1' THEN 'P5_1' ELSE '' END AS P5_1, ");        //仕様書No22.の資料5の1番目の帳票
        stb.append("   CASE WHEN T7.REMARK9 = '2' THEN 'P5_2' ELSE '' END AS P5_2, ");        //仕様書No22.の資料5の2番目の帳票
        stb.append("   CASE WHEN T3.REMARK7 = '1' THEN 'P5_3' ELSE '' END AS P5_3, ");        //仕様書No22.の資料5の3番目の帳票
        stb.append("   CASE WHEN (VALUE(T7.REMARK10, '') = '' AND VALUE(T7.REMARK9, '') = '') THEN 'P5_4' ELSE '' END AS P5_4, ");        //仕様書No22.の資料5の4番目の帳票
        stb.append("   CASE WHEN T1.SPECIAL_MEASURES = '1' THEN 'P5_5' ELSE '' END AS P5_5, ");        //仕様書No22.の資料5の5番目の帳票
        // ここまで指定区間内で、帳票の出力条件を情報として取得して関数を戻った所で帳票別にList分けしている。
        stb.append("   L006.ABBV1 AS SH_NAME, ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   REC.RECEPTNO, ");
        stb.append("   T1.NAME, ");
        stb.append("   FM.FINSCHOOL_NAME, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT01 AS CRPT1, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT02 AS CRPT2, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT03 AS CRPT3, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT04 AS CRPT4, ");
        stb.append("   CPRT.CONFIDENTIAL_RPT09 AS CRPT9, ");
        stb.append("   (CPRT.CONFIDENTIAL_RPT01 + CPRT.CONFIDENTIAL_RPT02 + CPRT.CONFIDENTIAL_RPT03 + CPRT.CONFIDENTIAL_RPT04 + CPRT.CONFIDENTIAL_RPT09) AS CONF_TOTAL, ");
        stb.append("   T5_01.SCORE AS SCORE1, ");
        stb.append("   T5_02.SCORE AS SCORE2, ");
        stb.append("   T5_03.SCORE AS SCORE3, ");
        stb.append("   T5_04.SCORE AS SCORE4, ");
        stb.append("   T5_05.SCORE AS SCORE5, ");
        stb.append("   T6_02.REMARK1 AS TOTAL3, ");  //見なしを含まない
        stb.append("   T6_01.REMARK1 AS TOTAL5, ");  //見なしを含まない
        stb.append("   VALUE(T7.REMARK10, '') || VALUE(L032.NAME1, '') AS SOUDAN1, ");
        stb.append("   CASE WHEN VALUE(T7.REMARK10, '') = '' AND VALUE(L032.NAME1, '') = '' THEN '事前相談なし' ELSE '' END AS SOUDAN2, ");
        stb.append("   L025.ABBV2 AS SHOUGAKU_KBN, ");
        stb.append("   T4.REMARK1 AS JITURYOKU_TOTAL3, ");
        stb.append("   T4.REMARK2 AS JITURYOKU_AVG3, ");
        stb.append("   T4.REMARK3 AS JITURYOKU_TOTAL4, ");
        stb.append("   T4.REMARK4 AS JITURYOKU_AVG4, ");
        stb.append("   T3.REMARK5 AS SPJYOUKEN, ");
        stb.append("   REC.TOTAL3 AS TEST_TOTAL3, ");
        stb.append("   REC.TOTAL4 AS TEST_TOTAL4, ");
        stb.append("   CPRT.REMARK1 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("     ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("   LEFT JOIN NAME_MST L006 ");
        stb.append("     ON L006.NAMECD1 = 'L006' ");
        stb.append("    AND L006.NAMECD2 = T1.SHDIV ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ");
        stb.append("     ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T3.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND T3.SEQ = '005' ");
        stb.append("   LEFT JOIN NAME_MST L025 ");
        stb.append("     ON L025.NAMECD1 = 'L025' ");
        stb.append("    AND L025.NAMECD2 = T3.REMARK2 ");
        stb.append("    AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("     ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CPRT ");
        stb.append("     ON CPRT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND CPRT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND CPRT.EXAMNO       = T1.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
        stb.append("     ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("    AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("    AND T4.EXAMNO       = T1.EXAMNO ");
        stb.append("    AND T4.SEQ = '020' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT T5_01 ");
        stb.append("     ON T5_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T5_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T5_01.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T5_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T5_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T5_01.TESTSUBCLASSCD = '1' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT T5_02 ");
        stb.append("     ON T5_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T5_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T5_02.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T5_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T5_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T5_02.TESTSUBCLASSCD = '2' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT T5_03 ");
        stb.append("     ON T5_03.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T5_03.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T5_03.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T5_03.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T5_03.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T5_03.TESTSUBCLASSCD = '3' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT T5_04 ");
        stb.append("     ON T5_04.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T5_04.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T5_04.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T5_04.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T5_04.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T5_04.TESTSUBCLASSCD = '4' ");
        stb.append("   LEFT JOIN ENTEXAM_SCORE_DAT T5_05 ");
        stb.append("     ON T5_05.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T5_05.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T5_05.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T5_05.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T5_05.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T5_05.TESTSUBCLASSCD = '5' ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_01 ");
        stb.append("     ON T6_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T6_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T6_01.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T6_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T6_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T6_01.SEQ          = '011' ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_02 ");
        stb.append("     ON T6_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T6_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T6_02.TESTDIV      = REC.TESTDIV ");
        stb.append("    AND T6_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("    AND T6_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("    AND T6_02.SEQ          = '012' ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T7 ");
        stb.append("     ON T7.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("    AND T7.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("    AND T7.EXAMNO       = REC.EXAMNO ");
        stb.append("    AND T7.SEQ          = '004' ");
        stb.append("   LEFT JOIN NAME_MST L032 ");
        stb.append("     ON L032.NAMECD1 = 'L032' ");
        stb.append("    AND L032.NAMECD2 = T7.REMARK9 ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if ("5".equals(_param._testdiv)) {
            stb.append("   AND T1.TESTDIV IN ('5', '6') ");
        } else {
            stb.append("   AND T1.TESTDIV = '" + _param._testdiv + "' ");
        }
        stb.append("   AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   REC.RECEPTNO ");
        return stb.toString();
    }

    public void printOut5(final DB2UDB db2, final Vrw32alp svf) {
        List<Object> P6List = new ArrayList();
        List<Object> P7List = new ArrayList();
        List<Object> P8List = new ArrayList();
        List<Object> P9List = new ArrayList();
        List<Object> P10List = new ArrayList();
        List<Object> P11List = new ArrayList();
        List<Object> P12List = new ArrayList();
        List<Object> P13List = new ArrayList();
        List<Object> P14List = new ArrayList();
        List<Object> P15List = new ArrayList();
        List<Object> P16List = new ArrayList();
        List<Object> P17List = new ArrayList();
        List<Object> P18List = new ArrayList();
        setP6P18List(db2, 0, P6List , P7List , P8List , P9List , P10List, null, null, null, null, null, null, null, null);
        setP6P18List(db2, 1, null , null , null , null , null, P11List, null, P13List, null, P15List, null, P17List, null); // 5教科順
        setP6P18List(db2, 2, null , null , null , null , null, null, P12List, null, P14List, null, P16List, null, P18List); // 3教科順
          if (P6List.size() > 0) {
              for (final List<Object> l : getPageList(P6List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 志願者一覧(入試結果関連)");
                  prtfmtP6P18(db2, svf, l, "P6");
                  svf.VrEndPage();
                  log.debug("P6_OUT:"+_hasData);
              }
          }
          if (P7List.size() > 0) {
              for (final List<Object> l : getPageList(P7List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 志願者一覧(入試結果関連)");
                  prtfmtP6P18(db2, svf, l, "P7");
                  svf.VrEndPage();
                  log.debug("P7_OUT:"+_hasData);
              }
          }
          if (P8List.size() > 0) {
              for (final List<Object> l : getPageList(P8List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 志願者一覧(入試結果関連)");
                  prtfmtP6P18(db2, svf, l, "P8");
                  svf.VrEndPage();
                  log.debug("P8_OUT:"+_hasData);
              }
          }
          if (P9List.size() > 0) {
              for (final List<Object> l : getPageList(P9List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 志願者一覧(入試結果関連)");
                  prtfmtP6P18(db2, svf, l, "P9");
                  svf.VrEndPage();
                  log.debug("P9_OUT:"+_hasData);
              }
          }
          if (P10List.size() > 0) {
              for (final List<Object> l : getPageList(P10List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 志願者一覧(入試結果関連)");
                  prtfmtP6P18(db2, svf, l, "P10");
                  svf.VrEndPage();
                  log.debug("P10_OUT:"+_hasData);
              }
          }
          if (P11List.size() > 0 && shDivChk("1") && testDiv1Chk("4")) {  //指示画面で専願・5教科選択時のみ出力
              for (final List<Object> l : getPageList(P11List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（文理・専願・5教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P11");
                  svf.VrEndPage();
                  log.debug("P11_OUT:"+_hasData);
              }
          }
          if (P12List.size() > 0 && shDivChk("1") && testDiv1Chk("3")) {  //指示画面で専願・3教科選択時のみ出力
              for (final List<Object> l : getPageList(P12List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（文理・専願・3教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P12");
                  svf.VrEndPage();
                  log.debug("P12_OUT:"+_hasData);
              }
          }
          if (P13List.size() > 0 && shDivChk("2") && testDiv1Chk("4")) {  //指示画面で併願・5教科選択時のみ出力
              for (final List<Object> l : getPageList(P13List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（文理・併願・5教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P13");
                  svf.VrEndPage();
                  log.debug("P13_OUT:"+_hasData);
              }
          }
          if (P14List.size() > 0 && shDivChk("2") && testDiv1Chk("3")) {  //指示画面で併願・3教科選択時のみ出力
              for (final List<Object> l : getPageList(P14List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（文理・併願・3教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P14");
                  svf.VrEndPage();
                  log.debug("P14_OUT:"+_hasData);
              }
          }
          if (P15List.size() > 0 && shDivChk("1") && testDiv1Chk("4")) {  //指示画面で専願・5教科選択時のみ出力
              for (final List<Object> l : getPageList(P15List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（その他・専願・5教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P15");
                  svf.VrEndPage();
                  log.debug("P15_OUT:"+_hasData);
              }
          }
          if (P16List.size() > 0 && shDivChk("1") && testDiv1Chk("3")) {  //指示画面で専願・3教科選択時のみ出力
              for (final List<Object> l : getPageList(P16List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（その他・専願・3教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P16");
                  svf.VrEndPage();
                  log.debug("P16_OUT:"+_hasData);
              }
          }
          if (P17List.size() > 0 && shDivChk("2") && testDiv1Chk("4")) {  //指示画面で併願・5教科選択時のみ出力
              for (final List<Object> l : getPageList(P17List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（その他・併願・5教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P17");
                  svf.VrEndPage();
                  log.debug("P17_OUT:"+_hasData);
              }
          }
          if (P18List.size() > 0 && shDivChk("2") && testDiv1Chk("3")) {  //指示画面で併願・3教科選択時のみ出力
              for (final List<Object> l : getPageList(P18List, 25)) {
                  svf.VrSetForm(PRINT_FORM_5, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01");
                  svf.VrsOut("TITLE", nendo+"度 判定資料（その他・併願・3教科；成績順）");
                  prtfmtP6P18(db2, svf, l, "P18");
                  svf.VrEndPage();
                  log.debug("P18_OUT:"+_hasData);
              }
          }
    }
    private boolean shDivChk(final String chkBaseStr) {
        boolean retbl = false;
        if ("".equals(_param._shDiv)) {
            retbl = true;
        } else 	if (chkBaseStr.equals(_param._shDiv)) {
            retbl = true;
        }
        return retbl;

    }
    private boolean testDiv1Chk(final String chkBaseStr) {
        boolean retbl = false;
        if ("".equals(_param._testDiv1)) {
            retbl = true;
        } else 	if (chkBaseStr.equals(_param._testDiv1)) {
            retbl = true;
        }
        return retbl;

    }
    //最後の引数はどの資料出力か、判別するために保持。
    private void prtfmtP6P18(final DB2UDB db2, final Vrw32alp svf, final List outList, final String prtPattern) {
        int putCnt = 1;
        for (Iterator ite = outList.iterator();ite.hasNext();) {
            PrintData618 prtwk = (PrintData618)ite.next();
            svf.VrsOutn("COURSE_NAME1", putCnt, prtwk._examcourse_Abbv1);
            svf.VrsOutn("COURSE_NAME2", putCnt, prtwk._examcourse_Abbv2);
            svf.VrsOutn("DIV1", putCnt, prtwk._sh_Name);
            svf.VrsOutn("EXAM_DIV", putCnt, prtwk._extype);
            svf.VrsOutn("NEXAMNO", putCnt, prtwk._examno);
            final int nlen = KNJ_EditEdit.getMS932ByteLength(prtwk._name);
            final String nfield = nlen > 30 ? "3" : (nlen > 20 ? "2" : "1");
            svf.VrsOutn("NAME"+nfield, putCnt, prtwk._name);
            final int fslen = KNJ_EditEdit.getMS932ByteLength(prtwk._finschool_Name_Abbv);
            final String fsfield = fslen > 30 ? "3" : (fslen > 20 ? "2" : "1");
            svf.VrsOutn("FINSCHOOL_NAME"+fsfield, putCnt, prtwk._finschool_Name_Abbv);
            svf.VrsOutn("TOTAL_SCORE1", putCnt, prtwk._test_Total3);
            svf.VrsOutn("TOTAL_SCORE2", putCnt, prtwk._test_Total4);
            svf.VrsOutn("DEV1", putCnt, prtwk._test_Devi3);
            svf.VrsOutn("DEV2", putCnt, prtwk._test_Devi4);
            svf.VrsOutn("ZONE1", putCnt, _param.getDeviLevStr(prtwk._test_Devi3));
            svf.VrsOutn("ZONE2", putCnt, _param.getDeviLevStr(prtwk._test_Devi4));
            svf.VrsOutn("RANK1", putCnt, prtwk._test_Rank3);
            svf.VrsOutn("RANK2", putCnt, prtwk._test_Rank4);
            svf.VrsOutn("RATE1", putCnt, prtwk._crpt1);
            svf.VrsOutn("RATE2", putCnt, prtwk._crpt2);
            svf.VrsOutn("RATE3", putCnt, prtwk._crpt3);
            svf.VrsOutn("RATE4", putCnt, prtwk._crpt4);
            svf.VrsOutn("RATE5", putCnt, prtwk._crpt9);
            svf.VrsOutn("RATE_TOTAL", putCnt, prtwk._conf_Total);
            svf.VrsOutn("SCORE1", putCnt, prtwk._score1);
            svf.VrsOutn("SCORE2", putCnt, prtwk._score2);
            svf.VrsOutn("SCORE3", putCnt, prtwk._score3);
            svf.VrsOutn("SCORE4", putCnt, prtwk._score4);
            svf.VrsOutn("SCORE5", putCnt, prtwk._score5);
            svf.VrsOutn("SCORE6", putCnt, prtwk._minasi_Tokuten);  //見なし
            svf.VrsOutn("SCORE7", putCnt, prtwk._minasi_Max);      //大きい方
            svf.VrsOutn("CONSULTATION", putCnt, prtwk._soudan2);
            svf.VrsOutn("MOCK_SCORE1", putCnt, prtwk._jituryoku_Total3);
            svf.VrsOutn("MOCK_AVE1", putCnt, prtwk._jituryoku_Avg3);
            svf.VrsOutn("MOCK_SCORE2", putCnt, prtwk._jituryoku_Total4);
            svf.VrsOutn("MOCK_AVE2", putCnt, prtwk._jituryoku_Avg4);
            final int splen = KNJ_EditEdit.getMS932ByteLength(prtwk._spjyouken);
            final String spfield = splen > 24 ? "3" : (splen > 18 ? "2" : "1");
            svf.VrsOutn("SPECIAL"+spfield, putCnt, prtwk._spjyouken);

            putCnt++;
        }
        if (outList.size() > 0) {
            svf.VrEndPage();
            _hasData = true;
        }
    }
    private void setP6P18List(final DB2UDB db2, final int order, final List P6List, final List P7List, final List P8List, final List P9List, final List P10List, final List P11List, final List P12List,
                                final List P13List, final List P14List, final List P15List, final List P16List, final List P17List, final List P18List) {
        final String sql = getP6P18Sql(order);
        log.debug("P6P20 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examcourse_Abbv1 = rs.getString("EXAMCOURSE_ABBV1");
                final String examcourse_Abbv2 = rs.getString("EXAMCOURSE_ABBV2");
                final String sh_Name = rs.getString("SH_NAME");
                final String extype = rs.getString("EXTYPE");
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String test_Total3 = rs.getString("TEST_TOTAL3");
                final String test_Total4 = rs.getString("TEST_TOTAL4");
                final String test_Devi3 = rs.getString("TEST_DEVI3");
                final String test_Devi4 = rs.getString("TEST_DEVI4");
//            	final String test_Xxx1 = rs.getString("TEST_XXX1");  //ゾーンはparamでマップ作成。
//            	final String test_Xxx2 = rs.getString("TEST_XXX2");  //ゾーンはparamでマップ作成。
                final String test_Rank3 = rs.getString("TEST_RANK3");
                final String test_Rank4 = rs.getString("TEST_RANK4");
                final String crpt1 = rs.getString("CRPT1");
                final String crpt2 = rs.getString("CRPT2");
                final String crpt3 = rs.getString("CRPT3");
                final String crpt4 = rs.getString("CRPT4");
                final String crpt9 = rs.getString("CRPT9");
                final String conf_Total = rs.getString("CONF_TOTAL");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String score4 = rs.getString("SCORE4");
                final String score5 = rs.getString("SCORE5");
                final String minasi_Tokuten = rs.getString("MINASI_TOKUTEN");
                final String minasi_Max = rs.getString("MINASI_MAX");
                final String soudan2 = rs.getString("SOUDAN2");
                final String jituryoku_Total3 = rs.getString("JITURYOKU_TOTAL3");
                final String jituryoku_Avg3 = rs.getString("JITURYOKU_AVG3");
                final String jituryoku_Total4 = rs.getString("JITURYOKU_TOTAL4");
                final String jituryoku_Avg4 = rs.getString("JITURYOKU_AVG4");
                final String spjyouken = rs.getString("SPJYOUKEN");
                PrintData618 addwk = new PrintData618(examcourse_Abbv1, examcourse_Abbv2, sh_Name, extype, examno, receptno, name, finschool_Name_Abbv, test_Total3, test_Total4, test_Devi3, test_Devi4,
                                                      test_Rank3, test_Rank4, crpt1, crpt2, crpt3, crpt4, crpt9, conf_Total, score1, score2, score3, score4, score5, minasi_Tokuten, minasi_Max,
                                                      soudan2, jituryoku_Total3, jituryoku_Avg3, jituryoku_Total4, jituryoku_Avg4, spjyouken);
                if ("P6".equals(StringUtils.defaultString(rs.getString("P6"), ""))) {
                    if (null != P6List) {
                        P6List.add(addwk);
                    }
                }
                //P7～P10は、仕様書上でパターンがあると想定されているが、仕様書にそのパターン分けが記載されていなかったので、何も記載しない。
                if ("P11".equals(StringUtils.defaultString(rs.getString("P11"), ""))) {
                    if (null != P11List) {
                        P11List.add(addwk);
                    }
                }
                if ("P12".equals(StringUtils.defaultString(rs.getString("P12"), ""))) {
                    if (null != P12List) {
                        P12List.add(addwk);
                    }
                }
                if ("P13".equals(StringUtils.defaultString(rs.getString("P13"), ""))) {
                    if (null != P13List) {
                        P13List.add(addwk);
                    }
                }
                if ("P14".equals(StringUtils.defaultString(rs.getString("P14"), ""))) {
                    if (null != P14List) {
                        P14List.add(addwk);
                    }
                }
                if ("P15".equals(StringUtils.defaultString(rs.getString("P15"), ""))) {
                    if (null != P15List) {
                        P15List.add(addwk);
                    }
                }
                if ("P16".equals(StringUtils.defaultString(rs.getString("P16"), ""))) {
                    if (null != P16List) {
                        P16List.add(addwk);
                    }
                }
                if ("P17".equals(StringUtils.defaultString(rs.getString("P17"), ""))) {
                    if (null != P17List) {
                        P17List.add(addwk);
                    }
                }
                if ("P18".equals(StringUtils.defaultString(rs.getString("P18"), ""))) {
                    if (null != P18List) {
                        P18List.add(addwk);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private String getP6P18Sql(final int order) {
        //P19P20とP6P18は同じ構造で、母集団が変わるだけ、という前提がある。
        //逆に、ここを変えるのであればgetP6P18Sql、getP19P20Sqlの整合性に注意すること。
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        // ここから指定区間内で、帳票の出力条件を情報として取得して関数を戻った所で帳票別にList分けしている(※これらの帳票は全て母集団、出力フォーマットが同じだったから。)。
        stb.append("     'P6' AS P6, ");
        stb.append("     CASE WHEN (BD_001.REMARK10 IN ('2001', '2002') AND T1.SHDIV = '1' AND T1.TESTDIV1 = '4') THEN 'P11' ELSE '' END AS P11, ");      //資料11
        stb.append("     CASE WHEN (BD_001.REMARK10 IN ('2001', '2002') AND T1.SHDIV = '1' AND T1.TESTDIV1 = '3') THEN 'P12' ELSE '' END AS P12, ");      //資料12
        stb.append("     CASE WHEN (BD_001.REMARK10 IN ('2001', '2002') AND T1.SHDIV = '2' AND T1.TESTDIV1 = '4') THEN 'P13' ELSE '' END AS P13, ");      //資料13
        stb.append("     CASE WHEN (BD_001.REMARK10 IN ('2001', '2002') AND T1.SHDIV = '2' AND T1.TESTDIV1 = '3') THEN 'P14' ELSE '' END AS P14, ");      //資料14
        stb.append("     CASE WHEN (BD_001.REMARK10 NOT IN ('2001', '2002') AND T1.SHDIV = '1' AND T1.TESTDIV1 = '4') THEN 'P15' ELSE '' END AS P15, ");  //資料15
        stb.append("     CASE WHEN (BD_001.REMARK10 NOT IN ('2001', '2002') AND T1.SHDIV = '1' AND T1.TESTDIV1 = '3') THEN 'P16' ELSE '' END AS P16, ");  //資料16
        stb.append("     CASE WHEN (BD_001.REMARK10 NOT IN ('2001', '2002') AND T1.SHDIV = '2' AND T1.TESTDIV1 = '4') THEN 'P17' ELSE '' END AS P17, ");  //資料17
        stb.append("     CASE WHEN (BD_001.REMARK10 NOT IN ('2001', '2002') AND T1.SHDIV = '2' AND T1.TESTDIV1 = '3') THEN 'P18' ELSE '' END AS P18, ");  //資料18
        // ここまで指定区間内で、帳票の出力条件を情報として取得して関数を戻った所で帳票別にList分けしている。
        stb.append("     ECM1.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV1, ");
        stb.append("     ECM2.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV2, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     L005.NAME1 AS EXTYPE, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     REC.RECEPTNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     FM.FINSCHOOL_NAME_ABBV, ");
        stb.append("     REC.TOTAL3 AS TEST_TOTAL3, ");
        stb.append("     REC.TOTAL4 AS TEST_TOTAL4, ");
        stb.append("     REC.AVARAGE3 AS TEST_DEVI3, ");
        stb.append("     REC.AVARAGE4 AS TEST_DEVI4, ");
//    	stb.append("     '' as TEST_XXX1, ");  //ゾーンはparamでマップ作成。
//    	stb.append("     '' as TEST_XXX2, ");  //ゾーンはparamでマップ作成。
        stb.append("     REC.TOTAL_RANK3 AS TEST_RANK3, ");
        stb.append("     REC.TOTAL_RANK4 AS TEST_RANK4, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT01 AS CRPT1, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT02 AS CRPT2, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT03 AS CRPT3, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT04 AS CRPT4, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT09 AS CRPT9, ");
        stb.append("     (CPRT.CONFIDENTIAL_RPT01 + CPRT.CONFIDENTIAL_RPT02 + CPRT.CONFIDENTIAL_RPT03 + CPRT.CONFIDENTIAL_RPT04 + CPRT.CONFIDENTIAL_RPT09) AS CONF_TOTAL, ");
        stb.append("     T5_01.SCORE AS SCORE1, ");
        stb.append("     T5_02.SCORE AS SCORE2, ");
        stb.append("     T5_03.SCORE AS SCORE3, ");
        stb.append("     T5_04.SCORE AS SCORE4, ");
        stb.append("     T5_05.SCORE AS SCORE5, ");
        stb.append("     L055.NAMESPARE2 AS MINASI_TOKUTEN, ");
        stb.append("     CASE WHEN VALUE(L055.NAMESPARE2, 0) > VALUE(T5_05.SCORE, 0) THEN L055.NAMESPARE2 ELSE T5_05.SCORE END as MINASI_MAX, ");
        stb.append("     CASE WHEN VALUE(T7.REMARK10, '') = '' AND VALUE(L032.NAME1, '') = '' THEN '事前相談なし' ELSE VALUE(T7.REMARK10, '') || VALUE(L032.NAME1, '') END AS SOUDAN2, ");
        stb.append("     T4.REMARK1 AS JITURYOKU_TOTAL3, ");
        stb.append("     T4.REMARK2 AS JITURYOKU_AVG3, ");
        stb.append("     T4.REMARK3 AS JITURYOKU_TOTAL4, ");
        stb.append("     T4.REMARK4 AS JITURYOKU_AVG4, ");
        stb.append("     T3.REMARK5 AS SPJYOUKEN ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("       ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L006 ");
        stb.append("       ON L006.NAMECD1 = 'L006' ");
        stb.append("      AND L006.NAMECD2 = T1.SHDIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ");
        stb.append("       ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T3.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T3.SEQ = '005' ");
//    	stb.append("     LEFT JOIN NAME_MST L025 ");
//    	stb.append("       ON L025.NAMECD1 = 'L025' ");
//    	stb.append("      AND L025.NAMECD2 = T3.REMARK2 ");
//    	stb.append("      AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("       ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CPRT ");
        stb.append("       ON CPRT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND CPRT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND CPRT.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
        stb.append("       ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T4.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T4.SEQ = '020' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_01 ");
        stb.append("       ON T5_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_01.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_01.TESTSUBCLASSCD = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_02 ");
        stb.append("       ON T5_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_02.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_02.TESTSUBCLASSCD = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_03 ");
        stb.append("       ON T5_03.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_03.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_03.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_03.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_03.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_03.TESTSUBCLASSCD = '3' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_04 ");
        stb.append("       ON T5_04.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_04.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_04.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_04.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_04.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_04.TESTSUBCLASSCD = '4' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_05 ");
        stb.append("       ON T5_05.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_05.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_05.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_05.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_05.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_05.TESTSUBCLASSCD = '5' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_01 ");
        stb.append("       ON T6_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T6_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T6_01.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T6_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T6_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T6_01.SEQ          = '011' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_02 ");
        stb.append("       ON T6_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T6_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T6_02.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T6_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T6_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T6_02.SEQ          = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T7 ");
        stb.append("       ON T7.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T7.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T7.EXAMNO       = REC.EXAMNO ");
        stb.append("      AND T7.SEQ          = '004' ");
        stb.append("     LEFT JOIN NAME_MST L032 ");
        stb.append("       ON L032.NAMECD1 = 'L032' ");
        stb.append("      AND L032.NAMECD2 = T7.REMARK9 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_001 ");
        stb.append("       ON BD_001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD_001.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD_001.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD_001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");
        stb.append("       ON ECM1.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
        stb.append("      AND ECM1.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND ECM1.TESTDIV = REC.TESTDIV ");
        stb.append("      AND ECM1.COURSECD = BD_001.REMARK8 ");
        stb.append("      AND ECM1.MAJORCD = BD_001.REMARK9 ");
        stb.append("      AND ECM1.EXAMCOURSECD = BD_001.REMARK10 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_017 ");
        stb.append("       ON BD_017.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD_017.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD_017.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD_017.SEQ = '017' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM2 ");
        stb.append("       ON ECM2.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
        stb.append("      AND ECM2.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND ECM2.TESTDIV = REC.TESTDIV ");
        stb.append("      AND ECM2.COURSECD = BD_017.REMARK1 ");
        stb.append("      AND ECM2.MAJORCD = BD_017.REMARK2 ");
        stb.append("      AND ECM2.EXAMCOURSECD = BD_017.REMARK3 ");
        stb.append("     LEFT JOIN NAME_MST L005 ");
        stb.append("       ON L005.NAMECD1 = 'L005' ");
        stb.append("      AND L005.NAMECD2 = T1.TESTDIV1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("       ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD041.SEQ = '041' ");
        stb.append("     LEFT JOIN NAME_MST L055 ");
        stb.append("       ON L055.NAMECD1    = 'L055' ");
        stb.append("      AND L055.NAMECD2    = T3.REMARK1 ");
        stb.append("      AND L055.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" ORDER BY ");
        if (order == 1) {
            // 5教科順
            stb.append("     CASE WHEN REC.TOTAL4 IS NULL THEN -1 ELSE REC.TOTAL4 END DESC, ");
        } else if (order == 2) {
            // 3教科順
            stb.append("     CASE WHEN REC.TOTAL3 IS NULL THEN -1 ELSE REC.TOTAL3 END DESC, ");
        }
        stb.append("     T1.EXAMNO, ");
        stb.append("     REC.RECEPTNO ");
        return stb.toString();
    }

    public void printOut6(final DB2UDB db2, final Vrw32alp svf) {
        List sortTypeList = new ArrayList();
        sortTypeList.add("1"); //受験番号順
        sortTypeList.add("2"); //成績順

        for (Iterator ite = sortTypeList.iterator();ite.hasNext();) {
            final String stype = (String)ite.next();
            List<Object> P19List = new ArrayList();
            List<Object> P20List = new ArrayList();
            setP19P20List(db2, P19List, P20List, stype);
            if (P19List.size() > 0) {
                  for (final List<Object> l : getPageList(P19List, 25)) {
                      svf.VrSetForm(PRINT_FORM_5, 1);
                      final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                      svf.VrsOut("TITLE", nendo+"度 内部認定判定資料（文理Ⅰ；" + ("2".equals(stype) ? "成績順" : "受験番号順") + "）");
                      //資料6～18と同じフォーマット、データ構造なので、資料6～18と出力処理を共有。
                      prtfmtP6P18(db2, svf, l, "P19");
                      svf.VrEndPage();
                      log.debug("P19_OUT:"+_hasData);
                  }
            }
            if (P20List.size() > 0) {
                  for (final List<Object> l : getPageList(P20List, 25)) {
                      svf.VrSetForm(PRINT_FORM_5, 1);
                      final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                      svf.VrsOut("TITLE", nendo+"度 内部認定判定資料（文理Ⅱ；" + ("2".equals(stype) ? "成績順" : "受験番号順") + "）");
                      prtfmtP6P18(db2, svf, l, "P20");
                      svf.VrEndPage();
                      log.debug("P20_OUT:"+_hasData);
                  }
            }
        }
    }
    private void setP19P20List(final DB2UDB db2, final List P19List, final List P20List, final String setsort) {
        final String sql = getP1920Sql(setsort);
        log.debug("P19P20 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examcourse_Abbv1 = rs.getString("EXAMCOURSE_ABBV1");
                final String examcourse_Abbv2 = rs.getString("EXAMCOURSE_ABBV2");
                final String sh_Name = rs.getString("SH_NAME");
                final String extype = rs.getString("EXTYPE");
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String test_Total3 = rs.getString("TEST_TOTAL3");
                final String test_Total4 = rs.getString("TEST_TOTAL4");
                final String test_Devi3 = rs.getString("TEST_DEVI3");
                final String test_Devi4 = rs.getString("TEST_DEVI4");
//            	final String test_Xxx1 = rs.getString("TEST_XXX1");  //ゾーンはparamでマップ作成。
//            	final String test_Xxx2 = rs.getString("TEST_XXX2");  //ゾーンはparamでマップ作成。
                final String test_Rank3 = rs.getString("TEST_RANK3");
                final String test_Rank4 = rs.getString("TEST_RANK4");
                final String crpt1 = rs.getString("CRPT1");
                final String crpt2 = rs.getString("CRPT2");
                final String crpt3 = rs.getString("CRPT3");
                final String crpt4 = rs.getString("CRPT4");
                final String crpt9 = rs.getString("CRPT9");
                final String conf_Total = rs.getString("CONF_TOTAL");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String score4 = rs.getString("SCORE4");
                final String score5 = rs.getString("SCORE5");
                final String minasi_Tokuten = rs.getString("MINASI_TOKUTEN");
                final String minasi_Max = rs.getString("MINASI_MAX");
                final String soudan2 = rs.getString("SOUDAN2");
                final String jituryoku_Total3 = rs.getString("JITURYOKU_TOTAL3");
                final String jituryoku_Avg3 = rs.getString("JITURYOKU_AVG3");
                final String jituryoku_Total4 = rs.getString("JITURYOKU_TOTAL4");
                final String jituryoku_Avg4 = rs.getString("JITURYOKU_AVG4");
                final String spjyouken = rs.getString("SPJYOUKEN");
                //P618とデータ構造は同じ。並び順序が違うだけ。
                PrintData618 addwk = new PrintData618(examcourse_Abbv1, examcourse_Abbv2, sh_Name, extype, examno, receptno, name, finschool_Name_Abbv, test_Total3, test_Total4, test_Devi3, test_Devi4,
                        test_Rank3, test_Rank4, crpt1, crpt2, crpt3, crpt4, crpt9, conf_Total, score1, score2, score3, score4, score5, minasi_Tokuten,
                        minasi_Max, soudan2, jituryoku_Total3, jituryoku_Avg3, jituryoku_Total4, jituryoku_Avg4, spjyouken);
                if ("P19".equals(StringUtils.defaultString(rs.getString("P19"), ""))) {
                    P19List.add(addwk);
                }
                if ("P20".equals(StringUtils.defaultString(rs.getString("P20"), ""))) {
                    P20List.add(addwk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private String getP1920Sql(final String setsort) {
        //P19P20とP6P18は同じ構造で、母集団が変わるだけ、という前提がある。
        //逆に、ここを変えるのであればgetP6P18Sql、getP19P20Sqlの整合性に注意すること。
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        // ここから指定区間内で、帳票の出力条件を情報として取得して関数を戻った所で帳票別にList分けしている(※これらの帳票は全て母集団、出力フォーマットが同じだったから。)。
        stb.append("     CASE WHEN (BD041.REMARK3 = '" + BUNRI_1_CD + "') THEN 'P19' ELSE '' END AS P19, ");  //資料19,21※ソートについては画面指定なので、パターン分け不要。
        stb.append("     CASE WHEN (BD041.REMARK3 = '" + BUNRI_2_CD + "') THEN 'P20' ELSE '' END AS P20, ");  //資料20,22※ソートについては画面指定なので、パターン分け不要。
        // ここまで指定区間内で、帳票の出力条件を情報として取得して関数を戻った所で帳票別にList分けしている。
        stb.append("     ECM1.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV1, ");
        stb.append("     ECM2.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV2, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     L005.NAME1 AS EXTYPE, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     REC.RECEPTNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     FM.FINSCHOOL_NAME_ABBV, ");
        stb.append("     REC.TOTAL3 AS TEST_TOTAL3, ");
        stb.append("     REC.TOTAL4 AS TEST_TOTAL4, ");
        stb.append("     REC.AVARAGE3 AS TEST_DEVI3, ");
        stb.append("     REC.AVARAGE4 AS TEST_DEVI4, ");
//    	stb.append("     '' as TEST_XXX1, ");  //ゾーンはparamでマップ作成。
//    	stb.append("     '' as TEST_XXX2, ");  //ゾーンはparamでマップ作成。
        stb.append("     REC.TOTAL_RANK3 AS TEST_RANK3, ");
        stb.append("     REC.TOTAL_RANK4 AS TEST_RANK4, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT01 AS CRPT1, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT02 AS CRPT2, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT03 AS CRPT3, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT04 AS CRPT4, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT09 AS CRPT9, ");
        stb.append("     (CPRT.CONFIDENTIAL_RPT01 + CPRT.CONFIDENTIAL_RPT02 + CPRT.CONFIDENTIAL_RPT03 + CPRT.CONFIDENTIAL_RPT04 + CPRT.CONFIDENTIAL_RPT09) AS CONF_TOTAL, ");
        stb.append("     T5_01.SCORE AS SCORE1, ");
        stb.append("     T5_02.SCORE AS SCORE2, ");
        stb.append("     T5_03.SCORE AS SCORE3, ");
        stb.append("     T5_04.SCORE AS SCORE4, ");
        stb.append("     T5_05.SCORE AS SCORE5, ");
        stb.append("     L055.NAMESPARE2 AS MINASI_TOKUTEN, ");
        stb.append("     CASE WHEN VALUE(L055.NAMESPARE2, 0) > VALUE(T5_05.SCORE, 0) THEN L055.NAMESPARE2 ELSE T5_05.SCORE END as MINASI_MAX, ");
        stb.append("     CASE WHEN VALUE(T7.REMARK10, '') = '' AND VALUE(L032.NAME1, '') = '' THEN '事前相談なし' ELSE VALUE(T7.REMARK10, '') || VALUE(L032.NAME1, '') END AS SOUDAN2, ");
        stb.append("     T4.REMARK1 AS JITURYOKU_TOTAL3, ");
        stb.append("     T4.REMARK2 AS JITURYOKU_AVG3, ");
        stb.append("     T4.REMARK3 AS JITURYOKU_TOTAL4, ");
        stb.append("     T4.REMARK4 AS JITURYOKU_AVG4, ");
        stb.append("     T3.REMARK5 AS SPJYOUKEN ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("       ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L006 ");
        stb.append("       ON L006.NAMECD1 = 'L006' ");
        stb.append("      AND L006.NAMECD2 = T1.SHDIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ");
        stb.append("       ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T3.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T3.SEQ = '005' ");
//    	stb.append("     LEFT JOIN NAME_MST L025 ");
//    	stb.append("       ON L025.NAMECD1 = 'L025' ");
//    	stb.append("      AND L025.NAMECD2 = T3.REMARK2 ");
//    	stb.append("      AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("       ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CPRT ");
        stb.append("       ON CPRT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND CPRT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND CPRT.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
        stb.append("       ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T4.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T4.SEQ = '020' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_01 ");
        stb.append("       ON T5_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_01.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_01.TESTSUBCLASSCD = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_02 ");
        stb.append("       ON T5_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_02.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_02.TESTSUBCLASSCD = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_03 ");
        stb.append("       ON T5_03.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_03.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_03.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_03.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_03.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_03.TESTSUBCLASSCD = '3' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_04 ");
        stb.append("       ON T5_04.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_04.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_04.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_04.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_04.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_04.TESTSUBCLASSCD = '4' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_05 ");
        stb.append("       ON T5_05.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_05.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_05.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_05.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_05.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_05.TESTSUBCLASSCD = '5' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_01 ");
        stb.append("       ON T6_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T6_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T6_01.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T6_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T6_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T6_01.SEQ          = '011' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_02 ");
        stb.append("       ON T6_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T6_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T6_02.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T6_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T6_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T6_02.SEQ          = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T7 ");
        stb.append("       ON T7.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T7.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T7.EXAMNO       = REC.EXAMNO ");
        stb.append("      AND T7.SEQ          = '004' ");
        stb.append("     LEFT JOIN NAME_MST L032 ");
        stb.append("       ON L032.NAMECD1 = 'L032' ");
        stb.append("      AND L032.NAMECD2 = T7.REMARK9 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_001 ");
        stb.append("       ON BD_001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD_001.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD_001.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD_001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");
        stb.append("       ON ECM1.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
        stb.append("      AND ECM1.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND ECM1.TESTDIV = REC.TESTDIV ");
        stb.append("      AND ECM1.COURSECD = BD_001.REMARK8 ");
        stb.append("      AND ECM1.MAJORCD = BD_001.REMARK9 ");
        stb.append("      AND ECM1.EXAMCOURSECD = BD_001.REMARK10 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_017 ");
        stb.append("       ON BD_017.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD_017.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD_017.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD_017.SEQ = '017' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM2 ");
        stb.append("       ON ECM2.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
        stb.append("      AND ECM2.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND ECM2.TESTDIV = REC.TESTDIV ");
        stb.append("      AND ECM2.COURSECD = BD_017.REMARK1 ");
        stb.append("      AND ECM2.MAJORCD = BD_017.REMARK2 ");
        stb.append("      AND ECM2.EXAMCOURSECD = BD_017.REMARK3 ");
        stb.append("     LEFT JOIN NAME_MST L005 ");
        stb.append("       ON L005.NAMECD1 = 'L005' ");
        stb.append("      AND L005.NAMECD2 = T1.TESTDIV1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("       ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD041.SEQ = '041' ");
        stb.append("     LEFT JOIN NAME_MST L055 ");
        stb.append("       ON L055.NAMECD1    = 'L055' ");
        stb.append("      AND L055.NAMECD2    = T3.REMARK1 ");
        stb.append("      AND L055.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '6'");
        stb.append("     AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" ORDER BY ");
        if ("2".equals(setsort)) {
            stb.append(" CASE WHEN REC.TOTAL3 IS NULL THEN -1 ELSE REC.TOTAL3 END DESC, ");
        }
        stb.append("     T1.EXAMNO, ");
        stb.append("     REC.RECEPTNO ");
        return stb.toString();
    }

    public void printOut7(final DB2UDB db2, final Vrw32alp svf) {
        List<Object>P23List = new ArrayList();
        setP23List(db2, P23List);
          if (P23List.size() > 0) {
              for (final List<Object> l : getPageList(P23List, 25)) {
                  svf.VrSetForm(PRINT_FORM_6, 1);
                  final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
                  svf.VrsOut("TITLE", nendo+"度 " + _param._testdivName + " 学業成績特別奨学生判定資料");
                  prtfmtP23(db2, svf, l, "P23");
                  svf.VrEndPage();
                  log.debug("P23_OUT:"+_hasData);
              }
          }
    }
    private void prtfmtP23(final DB2UDB db2, final Vrw32alp svf, final List outList, final String prtPattern) {
        int putCnt = 1;
        for (Iterator ite = outList.iterator();ite.hasNext();) {
            PrintDataP23 prtwk = (PrintDataP23)ite.next();
            svf.VrsOutn("COURSE_NAME1", putCnt, prtwk._examcourse_Abbv1);
            svf.VrsOutn("COURSE_NAME2", putCnt, prtwk._examcourse_Abbv2);
            svf.VrsOutn("DIV1", putCnt, prtwk._sh_Name);
            svf.VrsOutn("EXAM_DIV", putCnt, prtwk._extype);
            svf.VrsOutn("NEXAMNO", putCnt, prtwk._examno);
            final int nlen = KNJ_EditEdit.getMS932ByteLength(prtwk._name);
            final String nfield = nlen > 30 ? "3" : (nlen > 20 ? "2" : "1");
            svf.VrsOutn("NAME"+nfield, putCnt, prtwk._name);
            final int fslen = KNJ_EditEdit.getMS932ByteLength(prtwk._finschool_Name_Abbv);
            final String fsfield = fslen > 30 ? "3" : (fslen > 20 ? "2" : "1");
            svf.VrsOutn("FINSCHOOL_NAME"+fsfield, putCnt, prtwk._finschool_Name_Abbv);
            svf.VrsOutn("TOTAL_SCORE1", putCnt, prtwk._test_Total3);
            svf.VrsOutn("TOTAL_SCORE2", putCnt, prtwk._test_Total4);
            svf.VrsOutn("TOTAL_PER1", putCnt, prtwk._test_Rate3);
            svf.VrsOutn("TOTAL_PER2", putCnt, prtwk._test_Rate4);
            svf.VrsOutn("DEV1", putCnt, prtwk._test_Devi3);
            svf.VrsOutn("DEV2", putCnt, prtwk._test_Devi4);
            svf.VrsOutn("ZONE1", putCnt, _param.getDeviLevStr(prtwk._test_Devi3));
            svf.VrsOutn("ZONE2", putCnt, _param.getDeviLevStr(prtwk._test_Devi4));
            svf.VrsOutn("RANK1", putCnt, prtwk._test_Rank3);
            svf.VrsOutn("RANK2", putCnt, prtwk._test_Rank4);
            svf.VrsOutn("RATE1", putCnt, prtwk._crpt1);
            svf.VrsOutn("RATE2", putCnt, prtwk._crpt2);
            svf.VrsOutn("RATE3", putCnt, prtwk._crpt3);
            svf.VrsOutn("RATE4", putCnt, prtwk._crpt4);
            svf.VrsOutn("RATE5", putCnt, prtwk._crpt9);
            svf.VrsOutn("RATE_TOTAL", putCnt, prtwk._conf_Total);
            svf.VrsOutn("SCORE1", putCnt, prtwk._score1);
            svf.VrsOutn("SCORE2", putCnt, prtwk._score2);
            svf.VrsOutn("SCORE3", putCnt, prtwk._score3);
            svf.VrsOutn("SCORE4", putCnt, prtwk._score4);
            svf.VrsOutn("SCORE5", putCnt, prtwk._score5);
//    		svf.VrsOutn("SCORE6", putCnt, prtwk._minasi_Tokuten);  //見なし
//    		svf.VrsOutn("SCORE7", putCnt, prtwk._minasi_Max);      //大きい方
            svf.VrsOutn("DIV2", putCnt, prtwk._shogakukbn);
            svf.VrsOutn("CONSULTATION", putCnt, prtwk._soudan2);
            svf.VrsOutn("MOCK_SCORE1", putCnt, prtwk._jituryoku_Total3);
            svf.VrsOutn("MOCK_AVE1", putCnt, prtwk._jituryoku_Avg3);
            svf.VrsOutn("MOCK_SCORE2", putCnt, prtwk._jituryoku_Total4);
            svf.VrsOutn("MOCK_AVE2", putCnt, prtwk._jituryoku_Avg4);
            final int splen = KNJ_EditEdit.getMS932ByteLength(prtwk._spjyouken);
            final String spfield = splen > 24 ? "3" : (splen > 18 ? "2" : "1");
            svf.VrsOutn("SPECIAL"+spfield, putCnt, prtwk._spjyouken);

            putCnt++;
        }
        if (outList.size() > 0) {
            svf.VrEndPage();
            _hasData = true;
        }
    }
    private void setP23List(final DB2UDB db2, final List P23List) {
        final String sql = getP23Sql();
        log.debug("P23 sql:"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examcourse_Abbv1 = rs.getString("EXAMCOURSE_ABBV1");
                final String examcourse_Abbv2 = rs.getString("EXAMCOURSE_ABBV2");
                final String sh_Name = rs.getString("SH_NAME");
                final String extype = rs.getString("EXTYPE");
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String test_Total3 = rs.getString("TEST_TOTAL3");
                final String test_Total4 = rs.getString("TEST_TOTAL4");
                final String test_Devi3 = rs.getString("TEST_DEVI3");
                final String test_Devi4 = rs.getString("TEST_DEVI4");
                final String test_Rate3 = rs.getString("TEST_RATE3");  //ゾーンはparamでマップ作成。
                final String test_Rate4 = rs.getString("TEST_RATE4");  //ゾーンはparamでマップ作成。
                final String test_Rank3 = rs.getString("TEST_RANK3");
                final String test_Rank4 = rs.getString("TEST_RANK4");
                final String crpt1 = rs.getString("CRPT1");
                final String crpt2 = rs.getString("CRPT2");
                final String crpt3 = rs.getString("CRPT3");
                final String crpt4 = rs.getString("CRPT4");
                final String crpt9 = rs.getString("CRPT9");
                final String conf_Total = rs.getString("CONF_TOTAL");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String score4 = rs.getString("SCORE4");
                final String score5 = rs.getString("SCORE5");
                final String minasi_Tokuten = rs.getString("MINASI_TOKUTEN");
                final String minasi_Max = rs.getString("MINASI_MAX");
                final String shogakukbn = rs.getString("SHOGAKUKBN");
                final String soudan2 = rs.getString("SOUDAN2");
                final String jituryoku_Total3 = rs.getString("JITURYOKU_TOTAL3");
                final String jituryoku_Avg3 = rs.getString("JITURYOKU_AVG3");
                final String jituryoku_Total4 = rs.getString("JITURYOKU_TOTAL4");
                final String jituryoku_Avg4 = rs.getString("JITURYOKU_AVG4");
                final String spjyouken = rs.getString("SPJYOUKEN");
                PrintDataP23 addwk = new PrintDataP23(examcourse_Abbv1, examcourse_Abbv2, sh_Name, extype, examno, receptno, name, finschool_Name_Abbv, test_Total3, test_Total4, test_Devi3, test_Devi4,
                                                       test_Rate3, test_Rate4, test_Rank3, test_Rank4, crpt1, crpt2, crpt3, crpt4, crpt9, conf_Total, score1, score2, score3, score4, score5,
                                                       minasi_Tokuten, minasi_Max, shogakukbn, soudan2, jituryoku_Total3, jituryoku_Avg3, jituryoku_Total4, jituryoku_Avg4, spjyouken);
                P23List.add(addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }
    private String getP23Sql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ECM1.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV1, ");
        stb.append("     ECM2.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV2, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     L005.NAME1 AS EXTYPE, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     REC.RECEPTNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     FM.FINSCHOOL_NAME_ABBV, ");
        stb.append("     REC.TOTAL3 AS TEST_TOTAL3, ");
        stb.append("     REC.TOTAL4 AS TEST_TOTAL4, ");
        stb.append("     REC.AVARAGE3 AS TEST_DEVI3, ");
        stb.append("     REC.AVARAGE4 AS TEST_DEVI4, ");
        stb.append("     DECIMAL(ROUND(DECIMAL((REC.TOTAL3/300.0)*100.0, 5, 2), 1), 4, 1) as TEST_RATE3, ");
        stb.append("     DECIMAL(ROUND(DECIMAL((REC.TOTAL4/500.0)*100.0, 5, 2), 1), 4, 1) as TEST_RATE4, ");
        stb.append("     REC.TOTAL_RANK3 AS TEST_RANK3, ");
        stb.append("     REC.TOTAL_RANK4 AS TEST_RANK4, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT01 AS CRPT1, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT02 AS CRPT2, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT03 AS CRPT3, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT04 AS CRPT4, ");
        stb.append("     CPRT.CONFIDENTIAL_RPT09 AS CRPT9, ");
        stb.append("     (CPRT.CONFIDENTIAL_RPT01 + CPRT.CONFIDENTIAL_RPT02 + CPRT.CONFIDENTIAL_RPT03 + CPRT.CONFIDENTIAL_RPT04 + CPRT.CONFIDENTIAL_RPT09) AS CONF_TOTAL, ");
        stb.append("     T5_01.SCORE AS SCORE1, ");
        stb.append("     T5_02.SCORE AS SCORE2, ");
        stb.append("     T5_03.SCORE AS SCORE3, ");
        stb.append("     T5_04.SCORE AS SCORE4, ");
        stb.append("     T5_05.SCORE AS SCORE5, ");
        stb.append("     L055.NAMESPARE2 AS MINASI_TOKUTEN, ");
        stb.append("     CASE WHEN VALUE(L055.NAMESPARE2, 0) > VALUE(T5_05.SCORE, 0) THEN L055.NAMESPARE2 ELSE T5_05.SCORE END as MINASI_MAX, ");
        stb.append("     L025.ABBV2 AS SHOGAKUKBN, ");
        stb.append("     CASE WHEN VALUE(T7.REMARK10, '') = '' AND VALUE(L032.NAME1, '') = '' THEN '事前相談なし' ELSE VALUE(T7.REMARK10, '') || VALUE(L032.NAME1, '') END AS SOUDAN2, ");
        stb.append("     T4.REMARK1 AS JITURYOKU_TOTAL3, ");
        stb.append("     T4.REMARK2 AS JITURYOKU_AVG3, ");
        stb.append("     T4.REMARK3 AS JITURYOKU_TOTAL4, ");
        stb.append("     T4.REMARK4 AS JITURYOKU_AVG4, ");
        stb.append("     T3.REMARK5 AS SPJYOUKEN ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT REC ");
        stb.append("       ON REC.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND REC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND REC.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L006 ");
        stb.append("       ON L006.NAMECD1 = 'L006' ");
        stb.append("      AND L006.NAMECD2 = T1.SHDIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ");
        stb.append("       ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T3.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T3.SEQ = '005' ");
        stb.append("     LEFT JOIN NAME_MST L025 ");
        stb.append("       ON L025.NAMECD1 = 'L025' ");
        stb.append("      AND L025.NAMECD2 = T3.REMARK2 ");
        stb.append("      AND L025.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("       ON FM.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CPRT ");
        stb.append("       ON CPRT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND CPRT.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND CPRT.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
        stb.append("       ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T4.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T4.SEQ = '020' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_01 ");
        stb.append("       ON T5_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_01.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_01.TESTSUBCLASSCD = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_02 ");
        stb.append("       ON T5_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_02.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_02.TESTSUBCLASSCD = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_03 ");
        stb.append("       ON T5_03.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_03.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_03.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_03.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_03.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_03.TESTSUBCLASSCD = '3' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_04 ");
        stb.append("       ON T5_04.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_04.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_04.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_04.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_04.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_04.TESTSUBCLASSCD = '4' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T5_05 ");
        stb.append("       ON T5_05.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T5_05.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T5_05.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T5_05.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T5_05.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T5_05.TESTSUBCLASSCD = '5' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_01 ");
        stb.append("       ON T6_01.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T6_01.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T6_01.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T6_01.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T6_01.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T6_01.SEQ          = '011' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T6_02 ");
        stb.append("       ON T6_02.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T6_02.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T6_02.TESTDIV      = REC.TESTDIV ");
        stb.append("      AND T6_02.EXAM_TYPE    = REC.EXAM_TYPE ");
        stb.append("      AND T6_02.RECEPTNO     = REC.RECEPTNO ");
        stb.append("      AND T6_02.SEQ          = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T7 ");
        stb.append("       ON T7.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
        stb.append("      AND T7.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND T7.EXAMNO       = REC.EXAMNO ");
        stb.append("      AND T7.SEQ          = '004' ");
        stb.append("     LEFT JOIN NAME_MST L032 ");
        stb.append("       ON L032.NAMECD1 = 'L032' ");
        stb.append("      AND L032.NAMECD2 = T7.REMARK9 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_001 ");
        stb.append("       ON BD_001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD_001.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD_001.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD_001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM1 ");
        stb.append("       ON ECM1.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
        stb.append("      AND ECM1.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND ECM1.TESTDIV = REC.TESTDIV ");
        stb.append("      AND ECM1.COURSECD = BD_001.REMARK8 ");
        stb.append("      AND ECM1.MAJORCD = BD_001.REMARK9 ");
        stb.append("      AND ECM1.EXAMCOURSECD = BD_001.REMARK10 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_017 ");
        stb.append("       ON BD_017.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD_017.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD_017.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD_017.SEQ = '017' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ECM2 ");
        stb.append("       ON ECM2.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
        stb.append("      AND ECM2.APPLICANTDIV = REC.APPLICANTDIV ");
        stb.append("      AND ECM2.TESTDIV = REC.TESTDIV ");
        stb.append("      AND ECM2.COURSECD = BD_017.REMARK1 ");
        stb.append("      AND ECM2.MAJORCD = BD_017.REMARK2 ");
        stb.append("      AND ECM2.EXAMCOURSECD = BD_017.REMARK3 ");
        stb.append("     LEFT JOIN NAME_MST L005 ");
        stb.append("       ON L005.NAMECD1 = 'L005' ");
        stb.append("      AND L005.NAMECD2 = T1.TESTDIV1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD041 ");
        stb.append("       ON BD041.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND BD041.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND BD041.EXAMNO = T1.EXAMNO ");
        stb.append("      AND BD041.SEQ = '041' ");
        stb.append("     LEFT JOIN NAME_MST L055 ");
        stb.append("       ON L055.NAMECD1    = 'L055' ");
        stb.append("      AND L055.NAMECD2    = T3.REMARK1 ");
        stb.append("      AND L055.NAMESPARE1 = T1.APPLICANTDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if ("5".equals(_param._testdiv)) {
            stb.append("     AND T1.TESTDIV IN ('5', '6') ");
        } else {
            stb.append("     AND T1.TESTDIV = '" + _param._testdiv + "' ");
        }
        stb.append("     AND VALUE(REC.JUDGEDIV, '') <> '4' ");
        stb.append(" ORDER BY ");
        stb.append("     CASE WHEN REC.TOTAL3 IS NULL THEN -1 ELSE REC.TOTAL3 END DESC, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     REC.RECEPTNO ");

        return stb.toString();
    }

    //入試結果,認定試験結果,総合成績
    private class PrintDataP1RPT {
        final String _kamokucd;
        final String _rpt1;
        final String _rpt2;
        final String _rpt3;
        final String _rpt4;
        final String _rpt5;
        final String _avgval;
        public PrintDataP1RPT (final String kamokucd, final String rpt1, final String rpt2, final String rpt3, final String rpt4, final String rpt5, final String avgval)
        {
            _kamokucd = kamokucd;
            _rpt1 = rpt1;
            _rpt2 = rpt2;
            _rpt3 = rpt3;
            _rpt4 = rpt4;
            _rpt5 = rpt5;
            _avgval = avgval;
        }
    }
    private class PrintData2GA {
        final String _entexamyear;
        final String _avg_Kokugo;
        final String _avg_Syakai;
        final String _avg_Sugaku;
        final String _avg_Rika;
        final String _avg_Eigo;
        final String _k3_Avg;
        final String _k5_Avg;
        private PrintData2GA (final String entexamyear, final String avg_Kokugo, final String avg_Syakai, final String avg_Sugaku, final String avg_Rika, final String avg_Eigo,
                              final String k3_Avg, final String k5_Avg) {
            _entexamyear = entexamyear;
            _avg_Kokugo = avg_Kokugo;
            _avg_Syakai = avg_Syakai;
            _avg_Sugaku = avg_Sugaku;
            _avg_Rika = avg_Rika;
            _avg_Eigo = avg_Eigo;
            _k3_Avg = k3_Avg;
            _k5_Avg = k5_Avg;
        }
        private PrintData2GA minus(final PrintData2GA mwk) {
            final BigDecimal kokgowk = bdSubtract(_avg_Kokugo, mwk._avg_Kokugo);
            final String avg_Kokugo = kokgowk.toString();
            final BigDecimal syakaiwk = bdSubtract(_avg_Syakai, mwk._avg_Syakai);
            final String avg_Syakai = syakaiwk.toString();
            final BigDecimal sugakuwk = bdSubtract(_avg_Sugaku, mwk._avg_Sugaku);
            final String avg_Sugaku = sugakuwk.toString();
            final BigDecimal rikawk = bdSubtract(_avg_Rika, mwk._avg_Rika);
            final String avg_Rika = rikawk.toString();
            final BigDecimal eigowk = bdSubtract(_avg_Eigo, mwk._avg_Eigo);
            final String avg_Eigo = eigowk.toString();

            final BigDecimal k3wk = bdSubtract(_k3_Avg, mwk._k3_Avg);
            final String k3_Avg = k3wk.toString();
            final BigDecimal k5wk = bdSubtract(_k5_Avg, mwk._k5_Avg);
            final String k5_Avg = k5wk.toString();
            final PrintData2GA retwk = new PrintData2GA(_entexamyear, avg_Kokugo, avg_Syakai, avg_Sugaku, avg_Rika, avg_Eigo, k3_Avg, k5_Avg);
            return retwk;
        }
        private BigDecimal bdSubtract(final String str1, final String str2) {
            return new BigDecimal(str1).subtract(new BigDecimal(str2));
        }
    }
    private class PrintData2GD {
        final String _entexamyear;
        final String _cname_Id;
        final String _cname;
        final String _testdiv1;
        final String _testcnt;
        final String _shdiv;
        final String _sh_Name;
        final String _cnt_Kazu;
        final String _avg_Kokugo;
        final String _avg_Syakai;
        final String _avg_Sugaku;
        final String _avg_Rika;
        final String _avg_Eigo;
        final String _total_Avg;
        final String _total_Max;
        final String _total_Min;
        public PrintData2GD (final String entexamyear, final String cname_Id, final String cname, final String testdiv1, final String testcnt, final String shdiv, final String sh_Name,
                              final String cnt_Kazu, final String avg_Kokugo, final String avg_Syakai, final String avg_Sugaku, final String avg_Rika, final String avg_Eigo, final String total_Avg,
                              final String total_Max, final String total_Min){
            _entexamyear = entexamyear;
            _cname_Id = cname_Id;
            _cname = cname;
            _testdiv1 = testdiv1;
            _testcnt = testcnt;
            _shdiv = shdiv;
            _sh_Name = sh_Name;
            _cnt_Kazu = cnt_Kazu;
            _avg_Kokugo = avg_Kokugo;
            _avg_Syakai = avg_Syakai;
            _avg_Sugaku = avg_Sugaku;
            _avg_Rika = avg_Rika;
            _avg_Eigo = avg_Eigo;
            _total_Avg = total_Avg;
            _total_Max = total_Max;
            _total_Min = total_Min;
        }
    }
    private class PrintData2N {
        final String _fscourse_Typecd;
        final String _fscourse_Typename;
        final String _stdcnt;
        final String _score1_Avg;
        final String _score1_Max;
        final String _score1_Min;
        final String _score2_Avg;
        final String _score2_Max;
        final String _score2_Min;
        final String _score3_Avg;
        final String _score3_Max;
        final String _score3_Min;
        final String _score4_Avg;
        final String _score4_Max;
        final String _score4_Min;
        final String _score5_Avg;
        final String _score5_Max;
        final String _score5_Min;
        final String _total3_Avg;
        final String _total3_Max;
        final String _total3_Min;
        final String _total4_Avg;
        final String _total4_Max;
        final String _total4_Min;
        final String _total3_Lastavg;
        final String _total3_Lastmax;
        final String _total3_Lastmin;
        public PrintData2N (final String fscourse_Typecd, final String fscourse_Typename, final String stdcnt, final String score1_Avg, final String score1_Max, final String score1_Min,
                             final String score2_Avg, final String score2_Max, final String score2_Min, final String score3_Avg, final String score3_Max, final String score3_Min,
                             final String score4_Avg, final String score4_Max, final String score4_Min, final String score5_Avg, final String score5_Max, final String score5_Min,
                             final String total3_Avg, final String total3_Max, final String total3_Min, final String total4_Avg, final String total4_Max, final String total4_Min,
                             final String total3_Lastavg, final String total3_Lastmax, final String total3_Lastmin) {
            _fscourse_Typecd = fscourse_Typecd;
            _fscourse_Typename = fscourse_Typename;
            _stdcnt = stdcnt;
            _score1_Avg = score1_Avg;
            _score1_Max = score1_Max;
            _score1_Min = score1_Min;
            _score2_Avg = score2_Avg;
            _score2_Max = score2_Max;
            _score2_Min = score2_Min;
            _score3_Avg = score3_Avg;
            _score3_Max = score3_Max;
            _score3_Min = score3_Min;
            _score4_Avg = score4_Avg;
            _score4_Max = score4_Max;
            _score4_Min = score4_Min;
            _score5_Avg = score5_Avg;
            _score5_Max = score5_Max;
            _score5_Min = score5_Min;
            _total3_Avg = total3_Avg;
            _total3_Max = total3_Max;
            _total3_Min = total3_Min;
            _total4_Avg = total4_Avg;
            _total4_Max = total4_Max;
            _total4_Min = total4_Min;
            _total3_Lastavg = total3_Lastavg;
            _total3_Lastmax = total3_Lastmax;
            _total3_Lastmin = total3_Lastmin;
        }
    }
    private class PrintDataP3 {
        final String _rnum;
        final String _examno;
        final String _receptno;
        final String _abbv1;
        final String _abbv2;
        final String _clubname;
        final String _name;
        final String _finschool_Name;
        final String _crpt1;
        final String _crpt2;
        final String _crpt3;
        final String _crpt4;
        final String _crpt9;
        final String _conf_Total;
        final String _jituryoku_Total3;
        final String _jituryoku_Avg3;
        final String _jituryoku_Total4;
        final String _jituryoku_Avg4;
        final String _test_Total3;
        final String _test_Total4;
        final String _remark1;
        public PrintDataP3 (final String rnum, final String examno, final String receptno, final String abbv1, final String abbv2, final String clubname, final String name, final String finschool_Name,
                             final String crpt1, final String crpt2, final String crpt3, final String crpt4, final String crpt9, final String conf_Total, final String jituryoku_Total3,
                             final String jituryoku_Avg3, final String jituryoku_Total4, final String jituryoku_Avg4, final String test_Total3, final String test_Total4, final String remark1) {
            _rnum = rnum;
            _examno = examno;
            _receptno = receptno;
            _abbv1 = abbv1;
            _abbv2 = abbv2;
            _clubname = clubname;
            _name = name;
            _finschool_Name = finschool_Name;
            _crpt1 = crpt1;
            _crpt2 = crpt2;
            _crpt3 = crpt3;
            _crpt4 = crpt4;
            _crpt9 = crpt9;
            _conf_Total = conf_Total;
            _jituryoku_Total3 = jituryoku_Total3;
            _jituryoku_Avg3 = jituryoku_Avg3;
            _jituryoku_Total4 = jituryoku_Total4;
            _jituryoku_Avg4 = jituryoku_Avg4;
            _test_Total3 = test_Total3;
            _test_Total4 = test_Total4;
            _remark1 = remark1;
        }
    }
    private class PrintData45 {
        final String _sh_Name;
        final String _examno;
        final String _receptno;
        final String _name;
        final String _finschool_Name;
        final String _crpt1;
        final String _crpt2;
        final String _crpt3;
        final String _crpt4;
        final String _crpt9;
        final String _conf_Total;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score4;
        final String _score5;
        final String _total3;
        final String _total5;
        final String _soudan1;
        final String _soudan2;
        final String _shougaku_Kbn;
        final String _jituryoku_Total3;
        final String _jituryoku_Avg3;
        final String _jituryoku_Total4;
        final String _jituryoku_Avg4;
        final String _spjyouken;
        final String _test_Total3;
        final String _test_Total4;
        final String _remark1;
        public PrintData45 (final String sh_Name, final String examno, final String receptno, final String name, final String finschool_Name, final String crpt1,
                             final String crpt2, final String crpt3, final String crpt4, final String crpt9, final String conf_Total, final String score1,
                             final String score2, final String score3, final String score4, final String score5, final String total3, final String total5,
                             final String soudan1, final String soudan2, final String shougaku_Kbn, final String jituryoku_Total3, final String jituryoku_Avg3,
                             final String jituryoku_Total4, final String jituryoku_Avg4, final String spjyouken, final String test_Total3, final String test_Total4, final String remark1) {
            _sh_Name = sh_Name;
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _finschool_Name = finschool_Name;
            _crpt1 = crpt1;
            _crpt2 = crpt2;
            _crpt3 = crpt3;
            _crpt4 = crpt4;
            _crpt9 = crpt9;
            _conf_Total = conf_Total;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _total3 = total3;
            _total5 = total5;
            _soudan1 = soudan1;
            _soudan2 = soudan2;
            _shougaku_Kbn = shougaku_Kbn;
            _jituryoku_Total3 = jituryoku_Total3;
            _jituryoku_Avg3 = jituryoku_Avg3;
            _jituryoku_Total4 = jituryoku_Total4;
            _jituryoku_Avg4 = jituryoku_Avg4;
            _spjyouken = spjyouken;
            _test_Total3 = test_Total3;
            _test_Total4 = test_Total4;
            _remark1 = remark1;
        }
    }
    //P19P20にてP6P18を利用しているが、P6P18とデータ構造は同じ。並び順序が違うだけ。
    //逆に、ここを変えるのであればP6P18Sql、P19P20Sqlの整合性に注意すること。
    private class PrintData618 {
        final String _examcourse_Abbv1;
        final String _examcourse_Abbv2;
        final String _sh_Name;
        final String _extype;
        final String _examno;
        final String _receptno;
        final String _name;
        final String _finschool_Name_Abbv;
        final String _test_Total3;
        final String _test_Total4;
        final String _test_Devi3;
        final String _test_Devi4;
        final String _test_Rank3;
        final String _test_Rank4;
        final String _crpt1;
        final String _crpt2;
        final String _crpt3;
        final String _crpt4;
        final String _crpt9;
        final String _conf_Total;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score4;
        final String _score5;
        final String _minasi_Tokuten;
        final String _minasi_Max;
        final String _soudan2;
        final String _jituryoku_Total3;
        final String _jituryoku_Avg3;
        final String _jituryoku_Total4;
        final String _jituryoku_Avg4;
        final String _spjyouken;
        public PrintData618 (final String examcourse_Abbv1, final String examcourse_Abbv2, final String sh_Name, final String extype, final String examno, final String receptno, final String name,
                              final String finschool_Name_Abbv, final String test_Total3, final String test_Total4, final String test_Avg3, final String test_Avg4,final String test_Rank3, final String test_Rank4,
                              final String crpt1, final String crpt2, final String crpt3, final String crpt4, final String crpt9,final String conf_Total,
                              final String score1, final String score2, final String score3, final String score4, final String score5, final String minasi_Tokuten, final String minasi_Max,
                              final String soudan2, final String jituryoku_Total3, final String jituryoku_Avg3, final String jituryoku_Total4, final String jituryoku_Avg4, final String spjyouken) {
            _examcourse_Abbv1 = examcourse_Abbv1;
            _examcourse_Abbv2 = examcourse_Abbv2;
            _sh_Name = sh_Name;
            _extype = extype;
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _finschool_Name_Abbv = finschool_Name_Abbv;
            _test_Total3 = test_Total3;
            _test_Total4 = test_Total4;
            _test_Devi3 = test_Avg3;
            _test_Devi4 = test_Avg4;
            _test_Rank3 = test_Rank3;
            _test_Rank4 = test_Rank4;
            _crpt1 = crpt1;
            _crpt2 = crpt2;
            _crpt3 = crpt3;
            _crpt4 = crpt4;
            _crpt9 = crpt9;
            _conf_Total = conf_Total;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _minasi_Tokuten = minasi_Tokuten;
            _minasi_Max = minasi_Max;
            _soudan2 = soudan2;
            _jituryoku_Total3 = jituryoku_Total3;
            _jituryoku_Avg3 = jituryoku_Avg3;
            _jituryoku_Total4 = jituryoku_Total4;
            _jituryoku_Avg4 = jituryoku_Avg4;
            _spjyouken = spjyouken;
        }
    }
    private class PrintDataP23 {
        final String _examcourse_Abbv1;
        final String _examcourse_Abbv2;
        final String _sh_Name;
        final String _extype;
        final String _examno;
        final String _receptno;
        final String _name;
        final String _finschool_Name_Abbv;
        final String _test_Total3;
        final String _test_Total4;
        final String _test_Devi3;
        final String _test_Devi4;
        final String _test_Rate3;
        final String _test_Rate4;
        final String _test_Rank3;
        final String _test_Rank4;
        final String _crpt1;
        final String _crpt2;
        final String _crpt3;
        final String _crpt4;
        final String _crpt9;
        final String _conf_Total;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score4;
        final String _score5;
        final String _minasi_Tokuten;
        final String _minasi_Max;
        final String _shogakukbn;
        final String _soudan2;
        final String _jituryoku_Total3;
        final String _jituryoku_Avg3;
        final String _jituryoku_Total4;
        final String _jituryoku_Avg4;
        final String _spjyouken;
        public PrintDataP23 (final String examcourse_Abbv1, final String examcourse_Abbv2, final String sh_Name, final String extype, final String examno, final String receptno, final String name,
                              final String finschool_Name_Abbv, final String test_Total3, final String test_Total4, final String test_Avg3, final String test_Avg4, final String test_Rate3,
                              final String test_Rate4, final String test_Rank3, final String test_Rank4, final String crpt1, final String crpt2, final String crpt3, final String crpt4,
                              final String crpt9, final String conf_Total, final String score1, final String score2, final String score3, final String score4, final String score5,
                              final String minasi_Tokuten, final String minasi_Max, final String shogakukbn, final String soudan2, final String jituryoku_Total3, final String jituryoku_Avg3,
                              final String jituryoku_Total4, final String jituryoku_Avg4, final String spjyouken) {
            _examcourse_Abbv1 = examcourse_Abbv1;
            _examcourse_Abbv2 = examcourse_Abbv2;
            _sh_Name = sh_Name;
            _extype = extype;
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _finschool_Name_Abbv = finschool_Name_Abbv;
            _test_Total3 = test_Total3;
            _test_Total4 = test_Total4;
            _test_Devi3 = test_Avg3;
            _test_Devi4 = test_Avg4;
            _test_Rate3 = test_Rate3;
            _test_Rate4 = test_Rate4;
            _test_Rank3 = test_Rank3;
            _test_Rank4 = test_Rank4;
            _crpt1 = crpt1;
            _crpt2 = crpt2;
            _crpt3 = crpt3;
            _crpt4 = crpt4;
            _crpt9 = crpt9;
            _conf_Total = conf_Total;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _minasi_Tokuten = minasi_Tokuten;
            _minasi_Max = minasi_Max;
            _shogakukbn = shogakukbn;
            _soudan2 = soudan2;
            _jituryoku_Total3 = jituryoku_Total3;
            _jituryoku_Avg3 = jituryoku_Avg3;
            _jituryoku_Total4 = jituryoku_Total4;
            _jituryoku_Avg4 = jituryoku_Avg4;
            _spjyouken = spjyouken;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72328 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

//        final String _examCourse;
        final String _shDiv;
        final String _testDiv1;
//        final String _fs_course;
//        final String _sort;

        final String _dateStr;
        final String _applicantdivName;
        final String _testdivName;
        final List _nameMstL024;
        final String _fixCourseMajor;

        Map _SHMap;
        Map _SiganCourseMap;
        final Map _DeviLevMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');

            _dateStr = getDateStr(db2, _date);
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "1".equals(_applicantdiv) ? "L024" : "L004", _testdiv);
            _nameMstL024 = getNameMstL024(db2);

//            _examCourse = request.getParameter("EXAMCOURSE");
            _shDiv = ""; // request.getParameter("SHDIV");
            _testDiv1 = ""; // request.getParameter("TESTDIV1");
//            _fs_course = request.getParameter("FS_COURSE");
//            _sort = request.getParameter("SORT");

            _fixCourseMajor = "1001";   //教育課程コード(高校、固定値想定)
            _SHMap = getNameMstL006(db2);  //専併区分
            _SiganCourseMap = new LinkedMap(); //資料1用コース分類
            //注意：下記の設定順序を変える場合、setP1_RPTList関数の処理も見直す事！！
            String filterCode = "";
            filterCode = "'" + _fixCourseMajor + "2001', '" + _fixCourseMajor + "2002'"; //文理
            _SiganCourseMap.put(filterCode, getExamCourseMstWithSearchCode(db2, filterCode));
            filterCode = "'" + _fixCourseMajor + "2003', '" + _fixCourseMajor + "2004', '" + _fixCourseMajor + "2006'"; //ソレイユ・看護医療・子ども教育
            _SiganCourseMap.put(filterCode, getExamCourseMstWithSearchCode(db2, filterCode));
            filterCode = "'" + _fixCourseMajor + "2005'";  //エトワール
            _SiganCourseMap.put(filterCode, getExamCourseMstWithSearchCode(db2, filterCode));
            _DeviLevMap = getDeviLevMst(db2);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private String getExamCourseMstWithSearchCode(final DB2UDB db2, final String filterStr) {
            String retStr = "";
            String sep = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT VALUE(COURSECD || MAJORCD || EXAMCOURSECD, '') AS CHKCODE, EXAMCOURSE_ABBV FROM ENTEXAM_COURSE_MST WHERE ENTEXAMYEAR = '" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantdiv + "' AND TESTDIV = '" + _testdiv + "'");
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("CHKCODE") != "" && filterStr.indexOf(rs.getString("CHKCODE")) > 0) {
                        retStr += sep + rs.getString("EXAMCOURSE_ABBV");
                        sep = "・";
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = StringUtils.defaultString(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        public String keta(final int n, final int keta) {
            return StringUtils.repeat(" ", keta - String.valueOf(n).length()) + String.valueOf(n);
        }

        private Map getNameMstL006(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L006' ORDER BY NAMECD2 ASC ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    retMap.put(rs.getString("NAMECD2"), m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private List getNameMstL024(final DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L024' ORDER BY NAMECD2 ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();

                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    list.add(m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        private Map getDeviLevMst(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final String sql = "SELECT DEV_CD, DEV_MARK, DEV_LOW, DEV_HIGH FROM ENTEXAM_DEVIATION_LEVEL_MST WHERE  ENTEXAMYEAR = '" + _entexamyear + "' ORDER BY DEV_HIGH DESC";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String dev_Cd = rs.getString("DEV_CD");
                    final String dev_Mark = rs.getString("DEV_MARK");
                    final String dev_Low = rs.getString("DEV_LOW");
                    final String dev_High = rs.getString("DEV_HIGH");
                    DeviLevMst addwk = new DeviLevMst(dev_Cd, dev_Mark, dev_Low, dev_High);
                    retMap.put(dev_Low, addwk);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
        private String getDeviLevStr(final String Devi) {
            String retStr = "";
            if (!"".equals(StringUtils.defaultString(Devi, ""))) {
                BigDecimal chkVal = new BigDecimal(Devi);
                for (Iterator ite = _DeviLevMap.keySet().iterator();ite.hasNext();) {
                    final String lowStr = (String)ite.next();
                    BigDecimal wkVal = new BigDecimal(lowStr);
                    //降順に並んでいるので、引数値>lowになったタイミングで取る。
                    if (wkVal.compareTo(chkVal) <= 0) {
                        DeviLevMst getwk = (DeviLevMst)_DeviLevMap.get(lowStr);
                        retStr = getwk._dev_Mark;
                        break;  //それ以降回すと置き換わるので、break。
                    }
                }
            }
            return retStr;
        }
        private class DeviLevMst {
            final String _dev_Cd;
            final String _dev_Mark;
            final String _dev_Low;
            final String _dev_High;
            public DeviLevMst (final String dev_Cd, final String dev_Mark, final String dev_Low, final String dev_High) {
                _dev_Cd = dev_Cd;
                _dev_Mark = dev_Mark;
                _dev_Low = dev_Low;
                _dev_High = dev_High;
            }
        }
    }
}

// eof

