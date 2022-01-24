/*
 * $Id: ac1f166ac421c8a7fd035c63079a94f3bc5ba974 $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 収納額計算書
 */
public class KNJTE062 {

    private static final Log log = LogFactory.getLog(KNJTE062.class);

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
    
    private void VrsOut(final String[] field1, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOut(field1[i], data[i]);
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        /// 現年度分
        final PrintYear gennendo = new PrintYear(_param._year);
        setGennendoChoteizumi(db2, gennendo); // 調定済
        setGennendoShunyuzumi(db2, gennendo); // 収入済
        setGennendoFuno(db2, gennendo); // 不納欠損用

        final List kannendoList = new ArrayList();
        final int bottomYear = Integer.parseInt(_param._year) - 10;
        setKanendoChoteizumi(db2, kannendoList, bottomYear); // 調定済
        setKanendoShunyuzumi(db2, kannendoList, bottomYear); // 収入済
        setKanendoFunouKesson(db2, kannendoList, bottomYear); // 不納欠損

        svf.VrSetForm("KNJTE062.frm", 4);

        svf.VrsOut("NENDO", _param._shugakuDate.gengou(_param._year) + "年度"); // 年度
        svf.VrsOut("YM", _param._shugakuDate.formatNentuki(_param._ym)); // 年月
        svf.VrsOut("PRINTDAY", _param._shugakuDate.formatDate(_param._sakuseiDate)); // 作成日

        printGennendo(svf, gennendo);
        
        /// 過年度分
        PrintYear kara = new PrintYear("空");
        PrintYear kanendoTotal = new PrintYear("過年度計"); 
        for (int i = 0; i < 10; i++) {
            final String year = String.valueOf(Integer.parseInt(_param._year) - 10 + i) + (i == 0 ? "BEFORE" : "");
            PrintYear kanendo = getPrintYear(year, kannendoList);
            if (null == kanendo) {
                kanendo = kara;
            }
            printKannendo(svf, kanendo, year);
            svf.VrEndRecord();
            kanendoTotal = kanendoTotal.add(kanendo);
        }
        printKannendo(svf, kanendoTotal, "過年度計");
        svf.VrEndRecord();
        svf.VrsOut("SPACE", "空行"); // 空欄行表示用
        svf.VrEndRecord();
        printKannendo(svf, gennendo.add(kanendoTotal), "総合計");
        svf.VrEndRecord();
        _hasData = true;
    }

    private void printGennendo(final Vrw32alp svf, final PrintYear gennendo) {
        // 現年度: 調定累計
        // 前月まで
        svf.VrsOutn("BEFORE_TOTAL1", 1, gennendo._choteiZumiZen._gk); // 4
        svf.VrsOutn("BEFORE_TOTAL1", 2, gennendo._choteiZumiZen._kensu); // 5
        // 当月
        svf.VrsOutn("THIS_TOTAL1", 1, gennendo._choteiZumiTou._gk); // 6
        svf.VrsOutn("THIS_TOTAL1", 2, gennendo._choteiZumiTou._kensu); // 7
        // 累計
        gennendo._chotei = gennendo._choteiZumiZen.add(gennendo._choteiZumiTou);
        svf.VrsOutn("TOTAL1", 1, gennendo._chotei._gk); // 8
        svf.VrsOutn("TOTAL1", 2, gennendo._chotei._kensu); // 9
        // 現年度: 収入累計
        // 前月まで
        svf.VrsOutn("BEFORE_TOTAL2", 1, gennendo._shunyuZumiZen._gk); // 10
        svf.VrsOutn("BEFORE_TOTAL2", 2, gennendo._shunyuZumiZen._kokkoGk); // 11
        svf.VrsOutn("BEFORE_TOTAL2", 3, gennendo._shunyuZumiZen._tanpiGk); // 12
        svf.VrsOutn("BEFORE_TOTAL2", 4, gennendo._shunyuZumiZen._koufuGk); // 114
        svf.VrsOutn("BEFORE_TOTAL2", 5, gennendo._shunyuZumiZen._kensu); // 13
        // 当月
        svf.VrsOutn("THIS_TOTAL2", 1, gennendo._shunyuZumiTou._gk); // 14
        svf.VrsOutn("THIS_TOTAL2", 2, gennendo._shunyuZumiTou._kokkoGk); // 15
        svf.VrsOutn("THIS_TOTAL2", 3, gennendo._shunyuZumiTou._tanpiGk); // 16
        svf.VrsOutn("THIS_TOTAL2", 4, gennendo._shunyuZumiTou._koufuGk); // 115
        svf.VrsOutn("THIS_TOTAL2", 5, gennendo._shunyuZumiTou._kensu); // 17
        // 累計
        final Data shunyuRuikei = gennendo._shunyuZumiZen.add(gennendo._shunyuZumiTou);
        svf.VrsOutn("TOTAL2", 1, shunyuRuikei._gk); // 18 = 10 + 14
        svf.VrsOutn("TOTAL2", 2, shunyuRuikei._kokkoGk); // 19 = 11 + 15
        svf.VrsOutn("TOTAL2", 3, shunyuRuikei._tanpiGk); // 20 = 12 + 16
        svf.VrsOutn("TOTAL2", 4, shunyuRuikei._koufuGk); // 116 = 114 + 115
        svf.VrsOutn("TOTAL2", 5, shunyuRuikei._kensu); // 21 = 13 + 17

        // 現年度: 不能欠損用
        // 前月まで
        svf.VrsOutn("BEFORE_TOTAL2F", 1, gennendo._funoZen._gk); // 10
        svf.VrsOutn("BEFORE_TOTAL2F", 2, gennendo._funoZen._kokkoGk); // 11
        svf.VrsOutn("BEFORE_TOTAL2F", 3, gennendo._funoZen._tanpiGk); // 12
        svf.VrsOutn("BEFORE_TOTAL2F", 4, gennendo._funoZen._koufuGk); // 114
        svf.VrsOutn("BEFORE_TOTAL2F", 5, gennendo._funoZen._kensu); // 13
        // 当月
        svf.VrsOutn("THIS_TOTAL2F", 1, gennendo._funoTou._gk); // 14
        svf.VrsOutn("THIS_TOTAL2F", 2, gennendo._funoTou._kokkoGk); // 15
        svf.VrsOutn("THIS_TOTAL2F", 3, gennendo._funoTou._tanpiGk); // 16
        svf.VrsOutn("THIS_TOTAL2F", 4, gennendo._funoTou._koufuGk); // 115
        svf.VrsOutn("THIS_TOTAL2F", 5, gennendo._funoTou._kensu); // 17
        // 累計
        final Data funoRuikei = gennendo._funoZen.add(gennendo._funoTou);
        svf.VrsOutn("TOTAL2F", 1, funoRuikei._gk); // 18 = 10 + 14
        svf.VrsOutn("TOTAL2F", 2, funoRuikei._kokkoGk); // 19 = 11 + 15
        svf.VrsOutn("TOTAL2F", 3, funoRuikei._tanpiGk); // 20 = 12 + 16
        svf.VrsOutn("TOTAL2F", 4, funoRuikei._koufuGk); // 116 = 114 + 115
        svf.VrsOutn("TOTAL2F", 5, funoRuikei._kensu); // 21 = 13 + 17
        
        // 現年度: 未収入 = 調定累計 - 収入累計
        svf.VrsOutn("TOTAL3", 1, gennendo._chotei.subtract(shunyuRuikei)._gk); // 未収入累計額　38 = 8 - 18
        svf.VrsOutn("TOTAL3", 2, gennendo._chotei.subtract(shunyuRuikei)._kensu); // 未収入累計件数 39 = 9 - 21
    }
    
    private void printKannendo(final Vrw32alp svf, final PrintYear kanendo, final String div) {

        if ("過年度計".equals(div)) {
            svf.VrsOut("PAST_NENDO1", "過年度計");
        } else if ("総合計".equals(div)) {
            svf.VrsOut("PAST_NENDO3", "現年度");
            svf.VrsOut("PAST_NENDO1", "＋");
            svf.VrsOut("PAST_NENDO2", "過年度計");
        } else if (-1 != div.indexOf("BEFORE")) {
            svf.VrsOut("PAST_NENDO1", _param._shugakuDate.formatNen(div.substring(0, div.indexOf("BEFORE"))) + "度"); // 40
            svf.VrsOut("PAST_NENDO2", "　　　以前"); // 40
        } else {
            svf.VrsOut("PAST_NENDO1", _param._shugakuDate.formatNen(div) + "度"); // 59
        }
        
        // 過年度: 調定累計
        svf.VrsOut("PAST_COUNT1", kanendo._chotei._kensu); // 41
        svf.VrsOut("PAST_MONEY1", kanendo._chotei._gk); // 42
        // 過年度: 収入前月
        svf.VrsOut("PAST_COUNT2", kanendo._shunyuZumiZen._kensu); // 43
        svf.VrsOut("PAST_MONEY2", kanendo._shunyuZumiZen._gk); // 44
        svf.VrsOut("KOKKO1", kanendo._shunyuZumiZen._kokkoGk); // 45
        svf.VrsOut("TANPI1", kanendo._shunyuZumiZen._tanpiGk); // 46
        svf.VrsOut("KOUFU1", kanendo._shunyuZumiZen._koufuGk); // 117
        // 過年度: 収入当月
//        svf.VrsOut("", kanendo._shunyuZumiTou._kensu); 
        svf.VrsOut("PAST_MONEY3", kanendo._shunyuZumiTou._gk); // 47
        svf.VrsOut("KOKKO2", kanendo._shunyuZumiTou._kokkoGk); // 48
        svf.VrsOut("TANPI2", kanendo._shunyuZumiTou._tanpiGk); // 49
        svf.VrsOut("KOUFU2", kanendo._shunyuZumiTou._koufuGk); // 118
        // 過年度: 収入累計
        final Data shunyuRuikei = kanendo._shunyuZumiZen.add(kanendo._shunyuZumiTou);
//      svf.VrsOut("", shunyuRuikei._kensu); 
        svf.VrsOut("PAST_MONEY4", shunyuRuikei._gk); // 50
        svf.VrsOut("KOKKO3", shunyuRuikei._kokkoGk); // 51
        svf.VrsOut("TANPI3", shunyuRuikei._tanpiGk); // 52
        svf.VrsOut("KOUFU3", shunyuRuikei._koufuGk); // 119
        // 過年度: 不能欠損
        svf.VrsOut("PAST_COUNT3", kanendo._funouKessonShobun._kensu); // 53
        svf.VrsOut("PAST_MONEY5", kanendo._funouKessonShobun._gk); // 54
        svf.VrsOut("KOKKO4", kanendo._funouKessonShobun._kokkoGk); // 55
        svf.VrsOut("TANPI4", kanendo._funouKessonShobun._tanpiGk); // 56
        svf.VrsOut("KOUFU4", kanendo._funouKessonShobun._koufuGk); // 120
        // 過年度: 未収入 = 調定累計 - 収入累計
        svf.VrsOut("PAST_COUNT4", kanendo._chotei.subtract(shunyuRuikei).subtract(kanendo._funouKessonShobun)._kensu); // 未収入累計件数 57
        svf.VrsOut("PAST_MONEY6", kanendo._chotei.subtract(shunyuRuikei).subtract(kanendo._funouKessonShobun)._gk); // 未収入累計額　58
    }

    private void setGennendoChoteizumi(final DB2UDB db2, final PrintYear thisYear) {
        StringBuffer sql1;
        for (int i = 0; i < 2; i++) {
            sql1 = new StringBuffer();
            Data data = null;
            if (i == 0) {
                data = thisYear._choteiZumiZen;
            } else if (i == 1) {
                data = thisYear._choteiZumiTou;
            }

            sql1.append(" SELECT ");
            sql1.append("     COUNT(*) AS CHOTEI_COUNT, ");
            sql1.append("     SUM(HENKAN_GK) AS CHOTEI_GK ");
            sql1.append(" FROM ");
            sql1.append("     V_CHOTEI_NOUFU_ADD ");
            sql1.append(" WHERE ");
            sql1.append("     CHOTEI_NEND = '" + _param._year + "' ");
//          sql1.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
//          sql1.append("     AND GENKA_KBN = 0 AND ");
//          sql1.append("     AND FUNO_FLG = '0' ");
            sql1.append("     AND TORIKESI_FLG = '0' ");
            if (i == 0) {
                sql1.append("     AND CHOTEI_YM < '" + _param._ym + "' ");
            } else if (i == 1) {
                sql1.append("     AND CHOTEI_YM = '" + _param._ym + "' ");
            }
            sql1.append(" HAVING COUNT(*) > 0 ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug(" sql1 = " + sql1.toString());
                ps = db2.prepareStatement(sql1.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    data._gk = rs.getString("CHOTEI_GK");
                    data._kensu = rs.getString("CHOTEI_COUNT");
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
        }
    }

    private void setGennendoShunyuzumi(final DB2UDB db2, final PrintYear thisYear) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer sql2;
        for (int i = 0; i < 2; i++) {
            Data data = null;
            if (i == 0) {
                data = thisYear._shunyuZumiZen;
            } else if (i == 1) {
                data = thisYear._shunyuZumiTou;
            }
            
            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("       SUM(SHUNO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(SHUNO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(SHUNO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(SHUNO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append("     , COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND = '" + _param._year + "' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND BUNKATU_FLG = '0' ");
            sql2.append("     AND MINOU_GK = 0 ");
            sql2.append(" HAVING COUNT(*) > 0 ");

            try {
                log.debug(" sql2 = " + sql2);
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    data._gk = rs.getString("CHOTEI_GK");
                    data._kokkoGk = rs.getString("KOKKO_GK");
                    data._tanpiGk = rs.getString("TANPI_GK");
                    data._koufuGk = rs.getString("KOUFU_GK");
                    data._kensu = rs.getString("CHOTEI_COUNT");
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            
            // 件数だけ足す
            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("     COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND = '" + _param._year + "' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND BUNKATU_FLG = '1' "); // 件数だけ足すデータの条件
            sql2.append("     AND MINOU_GK = 0 ");
            sql2.append(" HAVING COUNT(*) > 0 ");

            try {
                log.debug(" sql2 kensu dake = " + sql2);
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    data._kensu = Data.add(data._kensu, rs.getString("CHOTEI_COUNT"));
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            
            // 件数以外を足す
            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("       SUM(HAKKO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(HAKKO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(HAKKO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(HAKKO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU "); // 件数以外を足す場合のビュー
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND = '" + _param._year + "' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND SHUNO_FLG = '1' "); // 件数以外を足す条件
            sql2.append("     AND BUNKATU_KAISU > 0 "); // 件数以外を足す条件
            sql2.append(" HAVING COUNT(*) > 0 ");
            try {
                log.debug(" sql2 kensu igai = " + sql2);
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    data._gk = Data.add(data._gk, rs.getString("CHOTEI_GK"));
                    data._kokkoGk = Data.add(data._kokkoGk, rs.getString("KOKKO_GK"));
                    data._tanpiGk = Data.add(data._tanpiGk, rs.getString("TANPI_GK"));
                    data._koufuGk = Data.add(data._koufuGk, rs.getString("KOUFU_GK"));
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
        }
    }
    
    private void setGennendoFuno(final DB2UDB db2, final PrintYear thisYear) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer sql2;
        for (int i = 0; i < 2; i++) {
            Data data = null;
            if (i == 0) {
                data = thisYear._funoZen;
            } else if (i == 1) {
                data = thisYear._funoTou;
            }

            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("       SUM(FUNO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(FUNO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(FUNO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(FUNO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append("     , COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '2' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND = '" + _param._year + "' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            if (i == 0) {
                sql2.append("     AND FUNO_KETEI_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND FUNO_KETEI_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
//            sql2.append("     AND BUNKATU_FLG = '0' ");
            sql2.append("     AND MINOU_GK = 0 ");
            sql2.append(" HAVING COUNT(*) > 0 ");
            try {
                log.debug(" sql2 = " + sql2);
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    data._gk = rs.getString("CHOTEI_GK");
                    data._kokkoGk = rs.getString("KOKKO_GK");
                    data._tanpiGk = rs.getString("TANPI_GK");
                    data._koufuGk = rs.getString("KOUFU_GK");
                    data._kensu = rs.getString("CHOTEI_COUNT");
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            
//            // 件数だけ足す
//            sql2 = new StringBuffer();
//            sql2.append(" SELECT ");
//            sql2.append("     COUNT(*) AS CHOTEI_COUNT ");
//            sql2.append(" FROM ");
//            sql2.append("     V_CHOTEI_NOUFU_ADD ");
//            sql2.append(" WHERE ");
//            sql2.append("     FUNO_FLG = '2' ");
//            sql2.append("     AND TORIKESI_FLG = '0' ");
//            sql2.append("     AND CHOTEI_NEND = '" + _param._year + "' ");
//            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
//            if (i == 0) {
//                sql2.append("     AND FUNO_KETEI_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
//            } else if (i == 1) {
//                sql2.append("     AND FUNO_KETEI_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
//            }
//            sql2.append("     AND BUNKATU_FLG = '1' "); // 件数だけ足すデータの条件
//            sql2.append("     AND MINOU_GK = 0 ");
//            sql2.append(" HAVING COUNT(*) > 0 ");
//
//            try {
//                log.debug(" sql2 kensu dake = " + sql2);
//                ps = db2.prepareStatement(sql2.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    data._kensu = Data.add(data._kensu, rs.getString("CHOTEI_COUNT"));
//                }
//            } catch (Exception ex) {
//                 log.fatal("exception!", ex);
//            } finally {
//                 DbUtils.closeQuietly(null, ps, rs);
//                 db2.commit();
//            }

//            // 件数以外を足す
//            sql2 = new StringBuffer();
//            sql2.append(" SELECT ");
//            sql2.append("       SUM(FUNO_TOTAL_GK) AS CHOTEI_GK ");
//            sql2.append("     , SUM(FUNO_KOKKO_GK) AS KOKKO_GK ");
//            sql2.append("     , SUM(FUNO_TANPI_GK) AS TANPI_GK ");
//            sql2.append("     , SUM(FUNO_KOUFU_GK) AS KOUFU_GK ");
//            sql2.append(" FROM ");
//            sql2.append("     V_CHOTEI_NOUFU "); // 件数以外を足す場合のビュー
//            sql2.append(" WHERE ");
//            sql2.append("     FUNO_FLG = '2' ");
//            sql2.append("     AND TORIKESI_FLG = '0' ");
//            sql2.append("     AND CHOTEI_NEND = '" + _param._year + "' ");
//            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
//            if (i == 0) {
//                sql2.append("     AND FUNO_KETEI_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
//            } else if (i == 1) {
//                sql2.append("     AND FUNO_KETEI_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
//            }
//            sql2.append("     AND SHUNO_FLG = '0' "); // 件数以外を足す条件
//            sql2.append("     AND BUNKATU_KAISU > 0 "); // 件数以外を足す条件
//            sql2.append(" HAVING COUNT(*) > 0 ");
//
//            try {
//                log.debug(" sql2 kensu igai = " + sql2);
//                ps = db2.prepareStatement(sql2.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    data._gk = Data.add(data._gk, rs.getString("CHOTEI_GK"));
//                    data._kokkoGk = Data.add(data._kokkoGk, rs.getString("KOKKO_GK"));
//                    data._tanpiGk = Data.add(data._tanpiGk, rs.getString("TANPI_GK"));
//                    data._koufuGk = Data.add(data._koufuGk, rs.getString("KOUFU_GK"));
//                }
//            } catch (Exception ex) {
//                 log.fatal("exception!", ex);
//            } finally {
//                 DbUtils.closeQuietly(null, ps, rs);
//                 db2.commit();
//            }
        }
        thisYear._funouKessonShobun = thisYear._funoZen.add(thisYear._funoTou);
    }
    
    private void setKanendoChoteizumi(final DB2UDB db2, final List rtn, final int bottomYear) {
        StringBuffer sql1;
        sql1 = new StringBuffer();
        sql1.append(" SELECT ");
        sql1.append("     '" + String.valueOf(bottomYear) + "BEFORE' AS YEAR, ");
        sql1.append("     COUNT(*) AS CHOTEI_COUNT, ");
        sql1.append("     SUM(DECIMAL(HENKAN_GK, 11, 0)) AS CHOTEI_GK ");
        sql1.append(" FROM ");
        sql1.append("     V_CHOTEI_NOUFU_ADD ");
        sql1.append(" WHERE ");
        sql1.append("     TORIKESI_FLG = '0' ");
//        sql1.append("     AND FUNO_FLG = '0' ");
        sql1.append("     AND CHOTEI_NEND <= '" + bottomYear + "' ");
        sql1.append("     AND KAIKEI_NEND >= '" + _param._year + "' ");
        sql1.append(" HAVING COUNT(*) > 0 ");
        sql1.append(" UNION ALL ");
        sql1.append(" SELECT ");
        sql1.append("     CHOTEI_NEND AS YEAR, ");
        sql1.append("     COUNT(*) AS CHOTEI_COUNT, ");
        sql1.append("     SUM(DECIMAL(HENKAN_GK, 11, 0)) AS CHOTEI_GK ");
        sql1.append(" FROM ");
        sql1.append("     V_CHOTEI_NOUFU_ADD ");
        sql1.append(" WHERE ");
        sql1.append("     TORIKESI_FLG = '0' ");
//        sql1.append("     AND FUNO_FLG = '0' ");
        sql1.append("     AND CHOTEI_NEND > '" + bottomYear + "' ");
        sql1.append("     AND KAIKEI_NEND >= '" + _param._year + "' ");
        sql1.append(" GROUP BY ");
        sql1.append("     CHOTEI_NEND ");
        sql1.append(" HAVING COUNT(*) > 0 ");
        sql1.append(" ORDER BY ");
        sql1.append("     YEAR ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql1 = " + sql1.toString());
            ps = db2.prepareStatement(sql1.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                PrintYear py = getPrintYear(rs.getString("YEAR"), rtn);
                if (null == py) {
                    py = new PrintYear(rs.getString("YEAR"));
                    rtn.add(py);
                }
                py._chotei._gk = rs.getString("CHOTEI_GK");
                py._chotei._kensu = rs.getString("CHOTEI_COUNT");
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        
        // 金額をひく
        sql1 = new StringBuffer();
        sql1.append(" SELECT ");
        sql1.append("     '" + String.valueOf(bottomYear) + "BEFORE' AS YEAR, ");
        sql1.append("     SUM(HAKKO_TOTAL_GK) AS CHOTEI_GK ");
        sql1.append(" FROM ");
        sql1.append("     V_CHOTEI_NOUFU ");
        sql1.append(" WHERE ");
        sql1.append("     TORIKESI_FLG = '0' ");
        sql1.append("     AND CHOTEI_NEND <= '" + bottomYear + "' ");
        sql1.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
        sql1.append("     AND KEISAN_DATE < '" + _param._year + "-04-01' ");
        sql1.append("     AND SHUNO_FLG = '1' ");
        sql1.append("     AND BUNKATU_KAISU > 0 ");
        sql1.append(" HAVING COUNT(*) > 0 ");
        sql1.append(" UNION ALL ");
        sql1.append(" SELECT ");
        sql1.append("     CHOTEI_NEND AS YEAR, ");
        sql1.append("     SUM(HAKKO_TOTAL_GK) AS CHOTEI_GK ");
        sql1.append(" FROM ");
        sql1.append("     V_CHOTEI_NOUFU ");
        sql1.append(" WHERE ");
        sql1.append("     TORIKESI_FLG = '0' ");
        sql1.append("     AND CHOTEI_NEND > '" + bottomYear + "' ");
        sql1.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
        sql1.append("     AND KEISAN_DATE < '" + _param._year + "-04-01' ");
        sql1.append("     AND SHUNO_FLG = '1' ");
        sql1.append("     AND BUNKATU_KAISU > 0 ");
        sql1.append(" GROUP BY ");
        sql1.append("     CHOTEI_NEND ");
        sql1.append(" HAVING COUNT(*) > 0 ");
        sql1.append(" ORDER BY ");
        sql1.append("     YEAR ");

        try {
            log.debug(" sql1 kensu igai = " + sql1);
            ps = db2.prepareStatement(sql1.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                PrintYear py = getPrintYear(rs.getString("YEAR"), rtn);
                if (null == py) {
                    continue;
                }
                // 金額をひく
                //log.info("過年度調定済 " + rs.getString("YEAR") + " : " + py._chotei._gk + " - " + rs.getString("CHOTEI_GK") + " = " + Data.subtract(py._chotei._gk, rs.getString("CHOTEI_GK")));
                py._chotei._gk = Data.subtract(py._chotei._gk, rs.getString("CHOTEI_GK"));
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
    }

    private void setKanendoShunyuzumi(final DB2UDB db2, final List rtn, final int bottomYear) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer sql2;
        for (int i = 0; i < 2; i++) {
            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("       '" + String.valueOf(bottomYear) + "BEFORE' AS YEAR ");
            sql2.append("     , SUM(SHUNO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(SHUNO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(SHUNO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(SHUNO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append("     , COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND <= '" + bottomYear + "' ");
            sql2.append("     AND MINOU_GK = 0 ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND BUNKATU_FLG = '0' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            sql2.append(" HAVING COUNT(*) > 0 ");
            sql2.append(" UNION ALL ");
            sql2.append(" SELECT ");
            sql2.append("       CHOTEI_NEND AS YEAR ");
            sql2.append("     , SUM(SHUNO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(SHUNO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(SHUNO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(SHUNO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append("     , COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND > '" + bottomYear + "' ");
            sql2.append("     AND MINOU_GK = 0 ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND BUNKATU_FLG = '0' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            sql2.append(" GROUP BY ");
            sql2.append("     CHOTEI_NEND ");
            sql2.append(" HAVING COUNT(*) > 0 ");
            sql2.append(" ORDER BY ");
            sql2.append("     YEAR ");
            
            try {
                log.debug(" sql2 = " + sql2.toString());
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    PrintYear py = getPrintYear(rs.getString("YEAR"), rtn);
                    if (null == py) {
                        py = new PrintYear(rs.getString("YEAR"));
                        rtn.add(py);
                    }
                    Data data = null;
                    if (i == 0) {
                        data = py._shunyuZumiZen;
                    } else if (i == 1) {
                        data = py._shunyuZumiTou;
                    }
                    data._gk = rs.getString("CHOTEI_GK");
                    data._kokkoGk = rs.getString("KOKKO_GK");
                    data._tanpiGk = rs.getString("TANPI_GK");
                    data._koufuGk = rs.getString("KOUFU_GK");
                    data._kensu = rs.getString("CHOTEI_COUNT");
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            
            // 件数だけ足す
            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("       '" + bottomYear + " + BEFORE' AS YEAR ");
            sql2.append("     , COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND <= '" + bottomYear + "' ");
            sql2.append("     AND MINOU_GK = 0 ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND BUNKATU_FLG = '1' "); // 件数だけ足すデータの条件
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            sql2.append(" HAVING COUNT(*) > 0 ");
            sql2.append(" UNION ALL ");
            sql2.append(" SELECT ");
            sql2.append("       CHOTEI_NEND AS YEAR ");
            sql2.append("     , COUNT(*) AS CHOTEI_COUNT ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU_ADD ");
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND > '" + bottomYear + "' ");
            sql2.append("     AND MINOU_GK = 0 ");

            if (i == 0) {
                sql2.append("     AND KEISAN_DATE <= '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND BUNKATU_FLG = '1' "); // 件数だけ足すデータの条件
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            sql2.append(" GROUP BY ");
            sql2.append("     CHOTEI_NEND ");
            sql2.append(" HAVING COUNT(*) > 0 ");
            sql2.append(" ORDER BY ");
            sql2.append("     YEAR ");

            try {
                log.debug(" sql2 kensu dake = " + sql2);
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    PrintYear py = getPrintYear(rs.getString("YEAR"), rtn);
                    if (null == py) {
                        py = new PrintYear(rs.getString("YEAR"));
                        rtn.add(py);
                    }
                    Data data = null;
                    if (i == 0) {
                        data = py._shunyuZumiZen;
                    } else if (i == 1) {
                        data = py._shunyuZumiTou;
                    }
                    data._kensu = Data.add(data._kensu, rs.getString("CHOTEI_COUNT"));
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            
            // 件数以外を足す
            sql2 = new StringBuffer();
            sql2.append(" SELECT ");
            sql2.append("       '" + bottomYear + "BEFORE' AS YEAR ");
            sql2.append("     , SUM(HAKKO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(HAKKO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(HAKKO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(HAKKO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU "); // 件数以外を足す場合のビュー
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND <= '" + bottomYear + "' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._year + "-04-01' AND '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND SHUNO_FLG = '1' "); // 件数以外を足す条件
            sql2.append("     AND BUNKATU_KAISU > 0 "); // 件数以外を足す条件
            sql2.append(" HAVING COUNT(*) > 0 ");
            sql2.append(" UNION ALL ");
            sql2.append(" SELECT ");
            sql2.append("       CHOTEI_NEND AS YEAR ");
            sql2.append("     , SUM(HAKKO_TOTAL_GK) AS CHOTEI_GK ");
            sql2.append("     , SUM(HAKKO_KOKKO_GK) AS KOKKO_GK ");
            sql2.append("     , SUM(HAKKO_TANPI_GK) AS TANPI_GK ");
            sql2.append("     , SUM(HAKKO_KOUFU_GK) AS KOUFU_GK ");
            sql2.append(" FROM ");
            sql2.append("     V_CHOTEI_NOUFU "); // 件数以外を足す場合のビュー
            sql2.append(" WHERE ");
            sql2.append("     FUNO_FLG = '0' ");
            sql2.append("     AND TORIKESI_FLG = '0' ");
            sql2.append("     AND CHOTEI_NEND > '" + bottomYear + "' ");
            sql2.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
            if (i == 0) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._year + "-04-01' AND '" + _param._choteiYmLastMonthLastDayYmd + "' ");
            } else if (i == 1) {
                sql2.append("     AND KEISAN_DATE BETWEEN '" + _param._choteiYmThisMonthFirstDayYmd + "' AND '" + _param._choteiYmThisMonthLastDayYmd + "' ");
            }
            sql2.append("     AND SHUNO_FLG = '1' "); // 件数以外を足す条件
            sql2.append("     AND BUNKATU_KAISU > 0 "); // 件数以外を足す条件
            sql2.append(" GROUP BY ");
            sql2.append("     CHOTEI_NEND ");
            sql2.append(" HAVING COUNT(*) > 0 ");
            sql2.append(" ORDER BY ");
            sql2.append("     YEAR ");
            
            try {
                log.debug(" sql2 kensu igai = " + sql2);
                ps = db2.prepareStatement(sql2.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    PrintYear py = getPrintYear(rs.getString("YEAR"), rtn);
                    if (null == py) {
                        py = new PrintYear(rs.getString("YEAR"));
                        rtn.add(py);
                    }
                    Data data = null;
                    if (i == 0) {
                        data = py._shunyuZumiZen;
                    } else if (i == 1) {
                        data = py._shunyuZumiTou;
                    }
                    data._gk = Data.add(data._gk, rs.getString("CHOTEI_GK"));
                    data._kokkoGk = Data.add(data._kokkoGk, rs.getString("KOKKO_GK"));
                    data._tanpiGk = Data.add(data._tanpiGk, rs.getString("TANPI_GK"));
                    data._koufuGk = Data.add(data._koufuGk, rs.getString("KOUFU_GK"));
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
        }
    }

    private void setKanendoFunouKesson(final DB2UDB db2, final List rtn, final int bottomYear) {
        StringBuffer sql3;
        sql3 = new StringBuffer();
        sql3.append(" SELECT ");
        sql3.append("     '" + bottomYear + "BEFORE' AS YEAR, ");
        sql3.append("     COUNT(*) AS CHOTEI_COUNT, ");
        sql3.append("     SUM(FUNO_TOTAL_GK) AS CHOTEI_GK, ");
        sql3.append("     SUM(FUNO_KOKKO_GK) AS KOKKO_GK, ");
        sql3.append("     SUM(FUNO_TANPI_GK) AS TANPI_GK, ");
        sql3.append("     SUM(FUNO_KOUFU_GK) AS KOUFU_GK ");
        sql3.append(" FROM ");
        sql3.append("     V_CHOTEI_NOUFU ");
        sql3.append(" WHERE ");
        sql3.append("     TORIKESI_FLG = '0' ");
        sql3.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
        sql3.append("     AND FUNO_FLG = '2' ");
        sql3.append("     AND FUNO_KETEI_DATE <= '" + _param._choteiYmThisMonthLastDayYmd + "' ");
        sql3.append("     AND BUNKATU_KAISU = 0 ");
        sql3.append("     AND CHOTEI_NEND <= '" + bottomYear + "' ");
        sql3.append(" HAVING COUNT(*) > 0 ");
        sql3.append(" UNION ALL ");
        sql3.append(" SELECT ");
        sql3.append("     CHOTEI_NEND AS YEAR, ");
        sql3.append("     COUNT(*) AS CHOTEI_COUNT, ");
        sql3.append("     SUM(FUNO_TOTAL_GK) AS CHOTEI_GK, ");
        sql3.append("     SUM(FUNO_KOKKO_GK) AS KOKKO_GK, ");
        sql3.append("     SUM(FUNO_TANPI_GK) AS TANPI_GK, ");
        sql3.append("     SUM(FUNO_KOUFU_GK) AS KOUFU_GK ");
        sql3.append(" FROM ");
        sql3.append("     V_CHOTEI_NOUFU ");
        sql3.append(" WHERE ");
        sql3.append("     TORIKESI_FLG = '0' ");
        sql3.append("     AND KAIKEI_NEND = '" + _param._year + "' ");
        sql3.append("     AND FUNO_FLG = '2' ");
        sql3.append("     AND FUNO_KETEI_DATE <= '" + _param._choteiYmThisMonthLastDayYmd + "' ");
        sql3.append("     AND BUNKATU_KAISU = 0 ");
        sql3.append("     AND CHOTEI_NEND > '" + bottomYear + "' ");
        sql3.append(" GROUP BY ");
        sql3.append("     CHOTEI_NEND ");
        sql3.append(" HAVING COUNT(*) > 0 ");
        sql3.append(" ORDER BY ");
        sql3.append("     YEAR ");
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql3 = " + sql3.toString());
            ps = db2.prepareStatement(sql3.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                PrintYear py = getPrintYear(rs.getString("YEAR"), rtn);
                if (null == py) {
                    py = new PrintYear(rs.getString("YEAR"));
                    rtn.add(py);
                }
                py._funouKessonShobun._gk = rs.getString("CHOTEI_GK");
                py._funouKessonShobun._kokkoGk = rs.getString("KOKKO_GK");
                py._funouKessonShobun._tanpiGk = rs.getString("TANPI_GK");
                py._funouKessonShobun._koufuGk = rs.getString("KOUFU_GK");
                py._funouKessonShobun._kensu = rs.getString("CHOTEI_COUNT");
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
    }
    
    private PrintYear getPrintYear(final String year, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final PrintYear py = (PrintYear) it.next();
            if (year.equals(py._year)) {
                return py;
            }
        }
        return null;
    }
    
    private static class Data {
        static String defVal = "0";
        String _kensu = defVal;
        String _gk = defVal;
        String _kokkoGk = defVal;
        String _tanpiGk = defVal;
        String _koufuGk = defVal;
        Data add(final Data d) {
            Data rtn = new Data();
            rtn._kensu = add(_kensu, d._kensu);
            rtn._gk = add(_gk, d._gk);
            rtn._kokkoGk = add(_kokkoGk, d._kokkoGk);
            rtn._tanpiGk = add(_tanpiGk, d._tanpiGk);
            rtn._koufuGk = add(_koufuGk, d._koufuGk);
            return rtn;
        }
        Data subtract(final Data d) {
            Data rtn = new Data();
            rtn._kensu = subtract(_kensu, d._kensu);
            rtn._gk = subtract(_gk, d._gk);
            rtn._kokkoGk = subtract(_kokkoGk, d._kokkoGk);
            rtn._tanpiGk = subtract(_tanpiGk, d._tanpiGk);
            rtn._koufuGk = subtract(_koufuGk, d._koufuGk);
            return rtn;
        }
        private static String add(final String v1, final String v2) {
            if (null == v1) return v2;
            if (null == v2) return v1;
            final BigDecimal bdv1 = new BigDecimal(v1);
            final BigDecimal bdv2 = new BigDecimal(v2);
            return bdv1.add(bdv2).toString();
        }
        private static String subtract(final String v1, final String v2) {
            if (null == v1) return v2;
            if (null == v2) return v1;
            final BigDecimal bdv1 = new BigDecimal(v1);
            final BigDecimal bdv2 = new BigDecimal(v2);
            return bdv1.subtract(bdv2).toString();
        }
    }
    
    private static class PrintYear {
        final String _year;
        Data _choteiZumiZen = new Data();
        Data _choteiZumiTou = new Data();
        Data _chotei = new Data();
        Data _shunyuZumiZen = new Data();
        Data _shunyuZumiTou = new Data();
        Data _funoZen = new Data();
        Data _funoTou = new Data();
        Data _funouKessonShobun = new Data();
        PrintYear(final String year) {
            _year = year;
        }
        PrintYear add(final PrintYear py) {
            final PrintYear rtn = new PrintYear(_year);
            rtn._choteiZumiZen = _choteiZumiZen.add(py._choteiZumiZen);
            rtn._choteiZumiTou = _choteiZumiTou.add(py._choteiZumiTou);
            rtn._chotei = _chotei.add(py._chotei);
            rtn._shunyuZumiZen = _shunyuZumiZen.add(py._shunyuZumiZen);
            rtn._shunyuZumiTou = _shunyuZumiTou.add(py._shunyuZumiTou);
            rtn._funouKessonShobun = _funouKessonShobun.add(py._funouKessonShobun);
            return rtn;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67241 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }
    
    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ym;
        private String _choteiYmLastMonthLastDayYmd;
        private String _choteiYmThisMonthFirstDayYmd;
        private String _choteiYmThisMonthLastDayYmd;
        final String _sakuseiDate;
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _ym = _shugakuDate.d5toYmStr(request.getParameter("YM"));
            _sakuseiDate = _shugakuDate.d7toDateStr(request.getParameter("SAKUSEI_DATE"));
            setYmd();
        }
        
        private void setYmd() {
            final DecimalFormat df = new DecimalFormat("00");
            final int year = Integer.parseInt(_ym.substring(0, 4));
            final int month = Integer.parseInt(_ym.substring(5, 7));
            final Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1 + 1); // 来月
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DATE, -1); // 今月の最後の日
            _choteiYmThisMonthFirstDayYmd = cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(1);

            final Calendar calSakusei = Calendar.getInstance();
            calSakusei.set(Calendar.YEAR, Integer.parseInt(_sakuseiDate.substring(0, 4)));
            calSakusei.set(Calendar.MONTH, Integer.parseInt(_sakuseiDate.substring(5, 7)) - 1);
            calSakusei.set(Calendar.DATE, Integer.parseInt(_sakuseiDate.substring(8, 10)));
            if (cal.get(Calendar.YEAR) == calSakusei.get(Calendar.YEAR)
                    && cal.get(Calendar.MONTH) == calSakusei.get(Calendar.MONTH)
                    && cal.get(Calendar.DAY_OF_MONTH) > calSakusei.get(Calendar.DAY_OF_MONTH)) {
                _choteiYmThisMonthLastDayYmd = cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(calSakusei.get(Calendar.DAY_OF_MONTH));
            } else {
                _choteiYmThisMonthLastDayYmd = cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(cal.get(Calendar.DAY_OF_MONTH));
            }
            cal.set(Calendar.MONTH, month - 1); // 今月
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DATE, -1); // 先月の最後の日
            _choteiYmLastMonthLastDayYmd = cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(cal.get(Calendar.DAY_OF_MONTH));
        }
    }
}

// eof

