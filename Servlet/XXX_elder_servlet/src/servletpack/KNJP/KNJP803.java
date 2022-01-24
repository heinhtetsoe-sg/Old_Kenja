/*
 * $Id: dac7cc59d972c9d7e7df552039457344183ba2fc $
 *
 * 作成日: 2018/03/20
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJP803 {

    private static final Log log = LogFactory.getLog(KNJP803.class);
    private static final String[] _monthArray = {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};

    boolean _hasData;

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

        for (int cnt = 0;cnt < _param._category_selected.length;cnt++) {
            _param._schoolkind = _param.getSchKind(db2, _param._category_selected[cnt]);
            final List list = getList(db2, _param._category_selected[cnt]);
            final String subttlgradestr = (String)_param._csnames.get(cnt);
            boolean titlesetflg = false;
            svf.VrSetForm(_param._formName + ".frm", 4);
            for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
                final OutputDat outputdat = (OutputDat) iterator1.next();
                if (outputdat._groupList.size() > 0) {
                    if (!titlesetflg) {
                        settitle(db2, svf, subttlgradestr);
                        titlesetflg = true;
                    }
                    outsvfGroup(svf, outputdat);
                }
            }
            if (titlesetflg) {
                svf.VrSetForm(_param._formName + ".frm", 4);
            }
        }
    }

    private void settitle(final DB2UDB db2, final Vrw32alp svf, final String subttlgradestr) {
        final List ttlnamelist = getTitleContentName(db2);
        String setYear = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
        svf.VrsOut("TITLE", setYear + "度科目別入金明細表");

        svf.VrsOut("SUBTITLE", subttlgradestr + _param._incomemonth + "月分");
        String ttlwk;
        ttlwk = (String)ttlnamelist.get(0);
        svf.VrsOut("ITEM1", ttlwk);
        svf.VrsOut("ITEM1_COUNT_NAME", "数");
        svf.VrsOut("ITEM1_PRICE_NAME", "金額");
        ttlwk = (String)ttlnamelist.get(1);
        svf.VrsOut("ITEM2", ttlwk);
        svf.VrsOut("ITEM2_COUNT_NAME", "数");
        svf.VrsOut("ITEM2_PRICE_NAME", "金額");
        ttlwk = (String)ttlnamelist.get(2);
        svf.VrsOut("ITEM3", ttlwk);
        svf.VrsOut("ITEM3_COUNT_NAME", "数");
        svf.VrsOut("ITEM3_PRICE_NAME", "金額");
        if ("KNJP803".equals(_param._formName)) {
            svf.VrsOut("ITEM4", "支援金");
            svf.VrsOut("ITEM4_COUNT_NAME", "数");
            svf.VrsOut("ITEM4_PRICE_NAME", "金額");
            svf.VrsOut("ITEM5", "補助金");
            svf.VrsOut("ITEM5_COUNT_NAME", "数");
            svf.VrsOut("ITEM5_PRICE_NAME", "金額");
            svf.VrsOut("ITEM6", "家族減免");
            svf.VrsOut("ITEM6_COUNT_NAME", "数");
            svf.VrsOut("ITEM6_PRICE_NAME", "金額");
            svf.VrsOut("ITEM7", "学力減免");
            svf.VrsOut("ITEM7_COUNT_NAME", "数");
            svf.VrsOut("ITEM7_PRICE_NAME", "金額");
        }
        svf.VrsOut("ITEM10", "未収金");
        svf.VrsOut("ITEM10_COUNT_NAME", "数");
        svf.VrsOut("ITEM10_PRICE_NAME", "金額");
        svf.VrsOut("ITEM11", "前払金");
        svf.VrsOut("ITEM11_COUNT_NAME", "数");
        svf.VrsOut("ITEM11_PRICE_NAME", "金額");
    }

    private void outsvfGroup(final Vrw32alp svf, final OutputDat stwk) {
        //固定結合　　　　：入金科目
        //結合切り替え箇所：月、前月繰越、次月繰越、
        //結合変更キー　　：月
        int grp1cnt = 0;
        int grp2cnt = 0;
        boolean firstsetflg = true;
        svf.VrsOut("GRP1", String.valueOf(grp1cnt));
        svf.VrsOut("GRP2", String.valueOf(grp2cnt));
        svf.VrsOut("GRP3", String.valueOf(grp1cnt));
        svf.VrsOut("GRP4", String.valueOf(grp1cnt));
        svf.VrsOut("GRP5", String.valueOf(grp1cnt));

        OutputSubDat outputsubdat;
        OutputSubDat outputnextdat = null;
        long beforecarryover = 0;
        String beforemonth = "";

        for (Iterator iterator1 = stwk._groupList.iterator(); iterator1.hasNext();) {
            if (firstsetflg) {
                outputsubdat = (OutputSubDat) iterator1.next();
            } else {
                outputsubdat = outputnextdat;
            }

            //MONTH
            if (!beforemonth.equals(outputsubdat._planmonth)) {
                svf.VrsOut("GRP1", String.valueOf(++grp1cnt));
                svf.VrsOut("MONTH", Integer.parseInt(outputsubdat._planmonth) + "月");
            } else {
                svf.VrsOut("GRP1", String.valueOf(grp1cnt));
            }

            //SUBJECT
            if (firstsetflg) {
                svf.VrsOut("SUBJECT", outputsubdat._collect_m_name);
                stwk._totaldat._collect_l_name = outputsubdat._collect_l_name;
                stwk._totaldat._collect_m_name = outputsubdat._collect_m_name;
            } else {
                svf.VrsOut("GRP2", String.valueOf(grp2cnt));
            }
            //PRICE
            svf.VrsOut("PRICE", String.valueOf(outputsubdat._price));
            //CARRY_OVER1 前月繰越金
            svf.VrsOut("CARRY_OVER1", String.valueOf(outputsubdat._carryover1));
            stwk._totaldat._carryover1 += outputsubdat._carryover1;
            //ITEM1_COUNT 振替
            svf.VrsOut("ITEM1_COUNT", String.valueOf(outputsubdat._cnt1));
            stwk._totaldat._cnt1 += outputsubdat._cnt1;
            //ITEM1_PRICE
            svf.VrsOut("ITEM1_PRICE", String.valueOf(outputsubdat._price1));
            stwk._totaldat._price1 += outputsubdat._price1;
            //ITEM2_COUNT 振込
            svf.VrsOut("ITEM2_COUNT", String.valueOf(outputsubdat._cnt2));
            stwk._totaldat._cnt2 += outputsubdat._cnt2;
            //ITEM2_PRICE
            svf.VrsOut("ITEM2_PRICE", String.valueOf(outputsubdat._price2));
            stwk._totaldat._price2 += outputsubdat._price2;
            //ITEM3_COUNT 現金
            svf.VrsOut("ITEM3_COUNT", String.valueOf(outputsubdat._cnt3));
            stwk._totaldat._cnt3 += outputsubdat._cnt3;
            //ITEM3_PRICE
            svf.VrsOut("ITEM3_PRICE", String.valueOf(outputsubdat._price3));
            stwk._totaldat._price3 += outputsubdat._price3;
            if ("1".equals(_param._form)) {
                //ITEM4_COUNT 支援金
                svf.VrsOut("ITEM4_COUNT", String.valueOf(outputsubdat._cnt4));
                stwk._totaldat._cnt4 += outputsubdat._cnt4;
                //ITEM4_PRICE
                svf.VrsOut("ITEM4_PRICE", String.valueOf(outputsubdat._price4));
                stwk._totaldat._price4 += outputsubdat._price4;
                //ITEM5_COUNT 補助金
                svf.VrsOut("ITEM5_COUNT", String.valueOf(outputsubdat._cnt5));
                stwk._totaldat._cnt5 += outputsubdat._cnt5;
                //ITEM5_PRICE
                svf.VrsOut("ITEM5_PRICE", String.valueOf(outputsubdat._price5));
                stwk._totaldat._price5 += outputsubdat._price5;
                //ITEM6_COUNT 家族減免
                svf.VrsOut("ITEM6_COUNT", String.valueOf(outputsubdat._cnt6));
                stwk._totaldat._cnt6 += outputsubdat._cnt6;
                //ITEM6_PRICE
                svf.VrsOut("ITEM6_PRICE", String.valueOf(outputsubdat._price6));
                stwk._totaldat._price6 += outputsubdat._price6;
                //ITEM7_COUNT 学力減免
                svf.VrsOut("ITEM7_COUNT", String.valueOf(outputsubdat._cnt7));
                stwk._totaldat._cnt7 += outputsubdat._cnt7;
                //ITEM7_PRICE
                svf.VrsOut("ITEM7_PRICE", String.valueOf(outputsubdat._price7));
                stwk._totaldat._price7 += outputsubdat._price7;
            }

            //※空き3つは未設定

            //REPAY 返金
            svf.VrsOut("REPAY", String.valueOf(outputsubdat._repay));
            //CARRY_OVER2 次月繰越金
            beforecarryover = outputsubdat._carryover1 + outputsubdat._price1 + outputsubdat._price2 + outputsubdat._price3;
            svf.VrsOut("CARRY_OVER2", String.valueOf(beforecarryover));
            stwk._totaldat._carryover2 += beforecarryover;
            //ITEM10_COUNT 未収金
            svf.VrsOut("ITEM10_COUNT", String.valueOf(outputsubdat._cnt10));
            stwk._totaldat._cnt10 += outputsubdat._cnt10;
            //ITEM10_PRICE
            svf.VrsOut("ITEM10_PRICE", String.valueOf(outputsubdat._price10));
            stwk._totaldat._price10 += outputsubdat._price10;
            //ITEM11_COUNT 前払い金
            svf.VrsOut("ITEM11_COUNT", String.valueOf(outputsubdat._cnt11));
            stwk._totaldat._cnt11 += outputsubdat._cnt11;
            //ITEM11_PRICE
            svf.VrsOut("ITEM11_PRICE", String.valueOf(outputsubdat._price11));
            stwk._totaldat._price11 += outputsubdat._price11;

            firstsetflg = false;
            outputnextdat = (OutputSubDat) iterator1.next();

            //次のデータが結合対象ではない場合は出力しない
            //MUST_MONEY
            svf.VrsOut("MUST_MONEY", String.valueOf(outputsubdat._mustmoney));
            stwk._totaldat._mustmoney += outputsubdat._mustmoney;
            //TOTAL
            if (!beforemonth.equals(outputsubdat._planmonth)) {
                svf.VrsOut("TOTAL", String.valueOf(outputsubdat._total));
                stwk._totaldat._total += outputsubdat._total;
            }
              svf.VrsOut("GRP3", String.valueOf(grp1cnt));
            //NUM
            if (!beforemonth.equals(outputsubdat._planmonth)) {
                svf.VrsOut("NUM", String.valueOf(outputsubdat._num));
                stwk._totaldat._num += outputsubdat._num;
            }
              svf.VrsOut("GRP4", String.valueOf(grp1cnt));
            //REMARK
            svf.VrsOut("REMARK", String.valueOf(outputsubdat._remark));
              svf.VrsOut("GRP5", String.valueOf(grp1cnt));

            beforemonth = outputsubdat._planmonth;
            svf.VrEndRecord();
            //1行空ける。
            //svf.VrEndRecord();
        }

        outputsubdat = stwk._totaldat;

        //TOTAL_MONTH
        svf.VrsOut("TOTAL_MONTH", outputsubdat._planmonth);
        //TOTAL_SUBJECT
        svf.VrsOut("TOTAL_SUBJECT", outputsubdat._collect_m_name + "合計");
        //TOTAL_PRICE
        svf.VrsOut("TOTAL_PRICE", ""); //単価項目のため、集計しない。
        //TOTAL_CARRY_OVER1
        svf.VrsOut("TOTAL_CARRY_OVER1", String.valueOf(outputsubdat._carryover1));
        //TOTAL_ITEM1_COUNT
        svf.VrsOut("TOTAL_ITEM1_COUNT", String.valueOf(outputsubdat._cnt1));
        //TOTAL_ITEM1_PRICE
        svf.VrsOut("TOTAL_ITEM1_PRICE", String.valueOf(outputsubdat._price1));
        //TOTAL_ITEM2_COUNT
        svf.VrsOut("TOTAL_ITEM2_COUNT", String.valueOf(outputsubdat._cnt2));
        //TOTAL_ITEM2_PRICE
        svf.VrsOut("TOTAL_ITEM2_PRICE", String.valueOf(outputsubdat._price2));
        //TOTAL_ITEM3_COUNT
        svf.VrsOut("TOTAL_ITEM3_COUNT", String.valueOf(outputsubdat._cnt3));
        //TOTAL_ITEM3_PRICE
        svf.VrsOut("TOTAL_ITEM3_PRICE", String.valueOf(outputsubdat._price3));
        if ("1".equals(_param._form)) {
            //TOTAL_ITEM4_COUNT
            svf.VrsOut("TOTAL_ITEM4_COUNT", String.valueOf(outputsubdat._cnt4));
            //TOTAL_ITEM4_PRICE
            svf.VrsOut("TOTAL_ITEM4_PRICE", String.valueOf(outputsubdat._price4));
            //TOTAL_ITEM5_COUNT
            svf.VrsOut("TOTAL_ITEM5_COUNT", String.valueOf(outputsubdat._cnt5));
            //TOTAL_ITEM5_PRICE
            svf.VrsOut("TOTAL_ITEM5_PRICE", String.valueOf(outputsubdat._price5));
            //TOTAL_ITEM6_COUNT
            svf.VrsOut("TOTAL_ITEM6_COUNT", String.valueOf(outputsubdat._cnt6));
            //TOTAL_ITEM6_PRICE
            svf.VrsOut("TOTAL_ITEM6_PRICE", String.valueOf(outputsubdat._price6));
            //TOTAL_ITEM7_COUNT
            svf.VrsOut("TOTAL_ITEM7_COUNT", String.valueOf(outputsubdat._cnt7));
            //TOTAL_ITEM7_PRICE
            svf.VrsOut("TOTAL_ITEM7_PRICE", String.valueOf(outputsubdat._price7));
        }

        //※空き3つは未設定

        //TOTAL_REPAY 返金
        svf.VrsOut("TOTAL_REPAY", String.valueOf(outputsubdat._repay));
        //TOTAL_CARRY_OVER2 次月繰越金
        svf.VrsOut("TOTAL_CARRY_OVER2", String.valueOf(outputsubdat._carryover2));
        //TOTAL_ITEM10_COUNT
        svf.VrsOut("TOTAL_ITEM10_COUNT", String.valueOf(outputsubdat._cnt10));
        //TOTAL_ITEM10_PRICE
        svf.VrsOut("TOTAL_ITEM10_PRICE", String.valueOf(outputsubdat._price10));
        //TOTAL_ITEM11_COUNT
        svf.VrsOut("TOTAL_ITEM11_COUNT", String.valueOf(outputsubdat._cnt11));
        //TOTAL_ITEM11_PRICE
        svf.VrsOut("TOTAL_ITEM11_PRICE", String.valueOf(outputsubdat._price11));
        //TOTAL_MUST_MONEY
        svf.VrsOut("TOTAL_MUST_MONEY", String.valueOf(outputsubdat._mustmoney));
        //TOTAL_TOTAL
        svf.VrsOut("TOTAL_TOTAL", String.valueOf(outputsubdat._total));
        //NUM
        svf.VrsOut("TOTAL_NUM", String.valueOf(outputsubdat._num));
        //REMARK
        svf.VrsOut("TOTAL_REMARK", "");

        svf.VrEndRecord();
    }

    private List getList(final DB2UDB db2, final String gradestr) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String keystr = "";
        OutputDat outputdat = new OutputDat();
//        _param._formName = "KNJP803_2";

        try {
            final String sql = OutputDatInfosql(gradestr);
            log.fatal(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String planyear = rs.getString("PLAN_YEAR");
                final String collect_l_name = rs.getString("COLLECT_L_NAME");
                final String collect_m_name = rs.getString("COLLECT_M_NAME");
//                final String kouhiShihi = rs.getString("KOUHI_SHIHI");
//                if ("1".equals(kouhiShihi)) {
//                    _param._formName = "KNJP803";
//                }
                final String planmonth = rs.getString("PLAN_MONTH");
                final long price = rs.getString("PLAN_MONEY") != null ? Integer.parseInt(rs.getString("PLAN_MONEY")) : 0;
                final long carryover1 = rs.getString("CARRYOVER") != null ? Integer.parseInt(rs.getString("CARRYOVER")) : 0;
                final int cnt1 = rs.getString("CNT1") != null ? Integer.parseInt(rs.getString("CNT1")) : 0;
                final long price1 = rs.getString("PRICE1") != null ? Integer.parseInt(rs.getString("PRICE1")) : 0;
                final int cnt2 = rs.getString("CNT2") != null ? Integer.parseInt(rs.getString("CNT2")) : 0;
                final long price2 = rs.getString("PRICE2") != null ? Integer.parseInt(rs.getString("PRICE2")) : 0;
                final int cnt3 = rs.getString("CNT3") != null ? Integer.parseInt(rs.getString("CNT3")) : 0;
                final long price3 = rs.getString("PRICE3") != null ? Integer.parseInt(rs.getString("PRICE3")) : 0;
                final int cnt4 = rs.getString("CNT4") != null ? Integer.parseInt(rs.getString("CNT4")) : 0;
                final long price4 = rs.getString("PRICE4") != null ? Integer.parseInt(rs.getString("PRICE4")) : 0;
                final int cnt5 = rs.getString("CNT5") != null ? Integer.parseInt(rs.getString("CNT5")) : 0;
                final long price5 = rs.getString("PRICE5") != null ? Integer.parseInt(rs.getString("PRICE5")) : 0;
                final int cnt6 = rs.getString("CNT6") != null ? Integer.parseInt(rs.getString("CNT6")) : 0;
                final long price6 = rs.getString("PRICE6") != null ? Integer.parseInt(rs.getString("PRICE6")) : 0;
                final int cnt7 = rs.getString("CNT7") != null ? Integer.parseInt(rs.getString("CNT7")) : 0;
                final long price7 = rs.getString("PRICE7") != null ? Integer.parseInt(rs.getString("PRICE7")) : 0;
                final long carryover2 = 0; //当月繰越(内部で算出)
                final long repay = 0;
                final int cnt10 = rs.getString("CNT10") != null ? Integer.parseInt(rs.getString("CNT10")) : 0;
                final long price10 = rs.getString("PRICE10") != null ? Integer.parseInt(rs.getString("PRICE10")) : 0;
                final int cnt11 = rs.getString("CNT11") != null ? Integer.parseInt(rs.getString("CNT11")) : 0;
                final long price11 = rs.getString("PRICE11") != null ? Integer.parseInt(rs.getString("PRICE11")) : 0;
                final long mustmoney = rs.getString("TOTAL_PLAN_MONEY") != null ? Integer.parseInt(rs.getString("TOTAL_PLAN_MONEY")) : 0;
                final long total = rs.getString("TOTAL_PRICE") != null ? Integer.parseInt(rs.getString("TOTAL_PRICE")) : 0;
                final int num = rs.getString("CNT_NUM") != null ? Integer.parseInt(rs.getString("CNT_NUM")) : 0;
                final String remark = rs.getString("REMARK");
                OutputSubDat outputsubdat = new OutputSubDat(planyear, collect_l_name, collect_m_name, planmonth, price, carryover1,
                        cnt1, price1, cnt2, price2, cnt3, price3, cnt4, price4, cnt5, price5, cnt6, price6, cnt7, price7,
                        carryover2, repay, cnt10, price10, cnt11, price11, mustmoney, total, num, remark);

                //key項目が変わったら、追加
                if (!"".equals(keystr)) {
                    if (!keystr.equals(collect_l_name + "|" +  collect_m_name)) {
                        list.add(outputdat);
                        outputdat = new OutputDat();
                    }
                }
                outputdat._groupList.add(outputsubdat);
                keystr = collect_l_name + "|" +  collect_m_name;
                _hasData = true;
            }
            if (_hasData) {
                list.add(outputdat);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private List getTitleContentName(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getContentNameosql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String nm = rs.getString("NAME1");

                list.add(nm);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    private String getContentNameosql() {
        final StringBuffer stb =  new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   NAME1 ");
        stb.append(" FROM ");
        stb.append("   NAME_MST ");
        stb.append(" WHERE ");
        stb.append("   NAMECD1 = 'P004' ");
        stb.append(" ORDER BY ");
        stb.append("   NAMECD2 ");
        return stb.toString();
    }

    private class OutputDat {
        final List _groupList;
        OutputSubDat _totaldat;

        OutputDat() {
            _groupList = new ArrayList();

            final String planyear = "";
            final String collect_l_name = "";
            final String collect_m_name = "";
            final String planmonth = "";
            final long price = 0;
            final long carryover1 = 0;
            final int cnt1 = 0;
            final long price1 = 0;
            final int cnt2 = 0;
            final long price2 = 0;
            final int cnt3 = 0;
            final long price3 = 0;
            final int cnt4 = 0;
            final long price4 = 0;
            final int cnt5 = 0;
            final long price5 = 0;
            final int cnt6 = 0;
            final long price6 = 0;
            final int cnt7 = 0;
            final long price7 = 0;
            final long repay = 0;
            final long carryover2 = 0;
            final int cnt10 = 0;
            final long price10 = 0;
            final int cnt11 = 0;
            final long price11 = 0;
            final long mustmoney = 0;
            final long total = 0;
            final int num = 0;
            final String remark = "";

            _totaldat = new OutputSubDat(planyear, collect_l_name, collect_m_name, planmonth, price, carryover1,
                    cnt1, price1, cnt2, price2, cnt3, price3, cnt4, price4, cnt5, price5, cnt6, price6, cnt7, price7,
                    carryover2, repay, cnt10, price10, cnt11, price11, mustmoney, total, num, remark);

        }
    }
    private class OutputSubDat {
        String _planyear;
        String _collect_l_name;
        String _collect_m_name;
        String _planmonth;
        long _price;
        long _carryover1;
        int _cnt1;
        long _price1;
        int _cnt2;
        long _price2;
        int _cnt3;
        long _price3;
        int _cnt4;
        long _price4;
        int _cnt5;
        long _price5;
        int _cnt6;
        long _price6;
        int _cnt7;
        long _price7;
        long _carryover2;
        long _repay;
        int _cnt10;
        long _price10;
        int _cnt11;
        long _price11;
        long _mustmoney;
        long _total;
        int _num;
        String _remark;

        public OutputSubDat(
                final String planyear,
                final String collect_l_name,
                final String collect_m_name,
                final String planmonth,
                final long price,
                final long carryover1,
                final int cnt1,
                final long price1,
                final int cnt2,
                final long price2,
                final int cnt3,
                final long price3,
                final int cnt4,
                final long price4,
                final int cnt5,
                final long price5,
                final int cnt6,
                final long price6,
                final int cnt7,
                final long price7,
                final long carryover2,
                final long repay,
                final int cnt10,
                final long price10,
                final int cnt11,
                final long price11,
                final long mustmoney,
                final long total,
                final int num,
                final String remark
        ) {
            _planyear = planyear;
            _collect_l_name = collect_l_name;
            _collect_m_name = collect_m_name;
            _planmonth = planmonth;
            _price = price;
            _carryover1 = carryover1;
            _cnt1 = cnt1;
            _price1 = price1;
            _cnt2 = cnt2;
            _price2 = price2;
            _cnt3 = cnt3;
            _price3 = price3;
            _cnt4 = cnt4;
            _price4 = price4;
            _cnt5 = cnt5;
            _price5 = price5;
            _cnt6 = cnt6;
            _price6 = price6;
            _cnt7 = cnt7;
            _price7 = price7;
            _repay = repay;
            _carryover2 = carryover2;
            _cnt10 = cnt10;
            _price10 = price10;
            _cnt11 = cnt11;
            _price11 = price11;
            _mustmoney = mustmoney;
            _total = total;
            _num = num;
            _remark = remark;
        }
    }

    private String OutputDatInfosql(final String gradestr) {

        String setYear = _param._year;
        if (Integer.parseInt(_param._incomemonth) < 4) {
            setYear = String.valueOf(Integer.parseInt(_param._year) + 1);
        }

        //前年度に4月入金がある為、4月の場合は3月が対象(4月を含めない)
        final String setMonth = Integer.parseInt(_param._incomemonth) == 4 ? "03" : _param._incomemonth;

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Integer.parseInt(setYear), Integer.parseInt(setMonth) - 1, 1);
        int lastDayOfMonth = cal.getActualMaximum(Calendar.DATE);
        String beforepaymentdate = setYear + "-" + setMonth + "-" + lastDayOfMonth;

        final StringBuffer stb =  new StringBuffer();

        stb.append("WITH BEFOREPAYMENTDATA AS (");
        stb.append("   SELECT ");
        stb.append("     CSP_PDAT.SCHOOLCD, ");
        stb.append("     CSP_PDAT.SCHOOL_KIND, ");
        stb.append("     CSP_PDAT.YEAR, ");
        stb.append("     CSP_PDAT.SLIP_NO, ");
        stb.append("     CSP_PDAT.PLAN_YEAR, ");
        stb.append("     CSP_PDAT.PLAN_MONTH, ");
        stb.append("     CSP_PDAT.COLLECT_L_CD, ");
        stb.append("     CSP_PDAT.COLLECT_M_CD, ");
        stb.append("     CSP_PDAT.SCHREGNO, ");
        stb.append("     CSP_PDAT.PLAN_PAID_MONEY, ");
        stb.append("     CSP_PDAT.PLAN_PAID_MONEY_DIV ");
        stb.append("   FROM ");
        stb.append("     COLLECT_SLIP_PLAN_PAID_M_DAT CSP_PDAT ");
        stb.append("     INNER JOIN COLLECT_SLIP_DAT CSD ");
        stb.append("       ON CSP_PDAT.SCHOOLCD = CSD.SCHOOLCD ");
        stb.append("       AND CSP_PDAT.SCHOOL_KIND = CSD.SCHOOL_KIND ");
        stb.append("       AND CSP_PDAT.YEAR = CSD.YEAR ");
        stb.append("       AND CSP_PDAT.SLIP_NO = CSD.SLIP_NO ");
        stb.append("       AND CSP_PDAT.SCHREGNO = CSD.SCHREGNO ");
        stb.append("       AND CSD.YEAR = '" + _param._year + "' ");
        stb.append("       AND CSD.CANCEL_DATE IS NULL ");
        stb.append("   WHERE ");
        stb.append("     CSP_PDAT.YEAR = '" + _param._year + "' ");
        if (Integer.parseInt(_param._incomemonth) == 4) {
            //前年度に4月入金がある為、4月の場合は全月が対象
        } else if (Integer.parseInt(_param._incomemonth) >= 4) {
            stb.append("     AND (CSP_PDAT.PLAN_MONTH > '" + _param._incomemonth + "' OR CSP_PDAT.PLAN_MONTH <= '03') ");
        } else {
            stb.append("     AND (CSP_PDAT.PLAN_MONTH > '" + _param._incomemonth + "' AND CSP_PDAT.PLAN_MONTH <= '03') ");
        }
        stb.append("     AND CSP_PDAT.PLAN_PAID_MONEY_DATE <= '" + beforepaymentdate + "' ");
        stb.append(" ), NOTPAYMENTDATA AS ( ");
        stb.append("   SELECT ");
        stb.append("     CSP_MDAT.SCHOOLCD, ");
        stb.append("     CSP_MDAT.SCHOOL_KIND, ");
        stb.append("     CSP_MDAT.YEAR, ");
        stb.append("     CSP_MDAT.SCHREGNO, ");
        stb.append("     CSP_MDAT.SLIP_NO, ");
        stb.append("     CSP_MDAT.COLLECT_L_CD, ");
        stb.append("     CSP_MDAT.COLLECT_M_CD, ");
        stb.append("     CSP_MDAT.PLAN_YEAR, ");
        stb.append("     CSP_MDAT.PLAN_MONTH, ");
        stb.append("     1 AS NOT_PAY_CNT, ");
        stb.append("     CSP_MDAT.PLAN_MONEY ");
        stb.append("   FROM ");
        stb.append("     COLLECT_SLIP_PLAN_M_DAT CSP_MDAT ");
        stb.append("     INNER JOIN COLLECT_SLIP_DAT CSD ");
        stb.append("       ON CSP_MDAT.SCHOOLCD = CSD.SCHOOLCD ");
        stb.append("       AND CSP_MDAT.SCHOOL_KIND = CSD.SCHOOL_KIND ");
        stb.append("       AND CSP_MDAT.YEAR = CSD.YEAR ");
        stb.append("       AND CSP_MDAT.SLIP_NO = CSD.SLIP_NO ");
        stb.append("       AND CSP_MDAT.SCHREGNO = CSD.SCHREGNO ");
        stb.append("       AND CSD.YEAR = '" + _param._year + "' ");
        stb.append("       AND CSD.CANCEL_DATE IS NULL ");
        stb.append("   WHERE ");
        stb.append("      CSP_MDAT.PLAN_CANCEL_FLG IS NULL ");
        stb.append("      AND CSP_MDAT.YEAR = '" + _param._year + "' ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR || CSP_MDAT.PLAN_MONTH <= '" + setYear + _param._incomemonth + "' ");
        stb.append("      AND VALUE(CSP_MDAT.PAID_YEARMONTH, '999912') > '" + setYear + _param._incomemonth + "' ");
        stb.append(" ), GET_MAXSEMESTER_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   SCHREGNO, ");
        stb.append("   YEAR, ");
        stb.append("   GRADE, ");
        stb.append("   MAX(SEMESTER) AS MAXSEMESTER ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO, ");
        stb.append("   YEAR, ");
        stb.append("   GRADE ");
        stb.append(" ), GET_SUM_DATA AS ( ");
        stb.append("   SELECT ");
        stb.append("     CSP_MDAT.SCHOOLCD, ");
        stb.append("     CSP_MDAT.SCHOOL_KIND, ");
        stb.append("     CSP_MDAT.YEAR, ");
        stb.append("     CSP_MDAT.PLAN_YEAR, ");
        stb.append("     CSP_MDAT.PLAN_MONTH, ");
        stb.append("     CSP_MDAT.COLLECT_L_CD, ");
        stb.append("     CSP_MDAT.COLLECT_M_CD, ");
        stb.append("     CSP_MDAT.PLAN_MONEY, ");
        stb.append("     COUNT(CSP_PAID1.SCHREGNO) AS CNT1, ");
        stb.append("     SUM(CSP_PAID1.PLAN_PAID_MONEY) AS PRICE1, ");
        stb.append("     COUNT(CSP_PAID2.SCHREGNO) AS CNT2, ");
        stb.append("     SUM(CSP_PAID2.PLAN_PAID_MONEY) AS PRICE2, ");
        stb.append("     COUNT(CSP_PAID3.SCHREGNO) AS CNT3, ");
        stb.append("     SUM(CSP_PAID3.PLAN_PAID_MONEY) AS PRICE3, ");
        stb.append("     CASE WHEN CMMST.REDUCTION_DIV = '1' THEN COUNT(RCPD.SCHREGNO) ELSE 0 END AS CNT4, ");
        stb.append("     CASE WHEN CMMST.REDUCTION_DIV = '1' THEN ");
        stb.append("        (CASE WHEN RCPD.ADD_PLAN_CANCEL_FLG IS NULL THEN SUM(RCPD.DECISION_MONEY + COALESCE(RCPD.ADD_DECISION_MONEY, 0)) ELSE SUM(RCPD.DECISION_MONEY) END)");
        stb.append("        ELSE 0 END AS PRICE4, ");
        stb.append("     CASE WHEN CMMST.REDUCTION_DIV IN ('1', '2') THEN COUNT(RPD.SCHREGNO) ELSE 0 END AS CNT5, ");
        stb.append("     CASE WHEN CMMST.REDUCTION_DIV IN ('1', '2') THEN SUM(RPD.DECISION_MONEY) ELSE 0 END AS PRICE5, ");
        stb.append("     CASE WHEN CMMST.GAKUNOKIN_DIV = '1' THEN COUNT(RSPD1.SCHREGNO) ELSE 0 END AS CNT6, ");
        stb.append("     CASE WHEN CMMST.GAKUNOKIN_DIV = '1' THEN SUM(RSPD1.DECISION_MONEY) ELSE 0 END AS PRICE6, ");
        stb.append("     CASE WHEN CMMST.GAKUNOKIN_DIV = '1' THEN COUNT(RSPD2.SCHREGNO) ELSE 0 END AS CNT7, ");
        stb.append("     CASE WHEN CMMST.GAKUNOKIN_DIV = '1' THEN SUM(RSPD2.DECISION_MONEY) ELSE 0 END AS PRICE7, ");
        stb.append("     SUM(NPD.NOT_PAY_CNT) AS CNT10, ");
//        stb.append("     CASE WHEN SUM(NPD.NOT_PAY_CNT) = 0 THEN 0 ELSE SUM(NPD.NOT_PAY_CNT) - COUNT(CSP_PAID1.SCHREGNO) - COUNT(CSP_PAID2.SCHREGNO) - COUNT(CSP_PAID3.SCHREGNO) END AS CNT10, ");
        stb.append("     SUM(NPD.NOT_PAY_CNT * CSP_MDAT.PLAN_MONEY) AS PRICE10, ");
//        stb.append("     CASE WHEN SUM(NPD.NOT_PAY_CNT * CSP_MDAT.PLAN_MONEY) = 0 THEN 0 ELSE (SUM(NPD.NOT_PAY_CNT) - COUNT(CSP_PAID1.SCHREGNO) - COUNT(CSP_PAID2.SCHREGNO) - COUNT(CSP_PAID3.SCHREGNO)) * CSP_MDAT.PLAN_MONEY END AS PRICE10, ");
        stb.append("     COUNT(BPD.SCHREGNO) AS CNT11, ");
        stb.append("     SUM(BPD.PLAN_PAID_MONEY) AS PRICE11, ");
        stb.append("     SUM(CSP_MDAT.PLAN_MONEY) AS TOTAL_PLAN_MONEY, ");
        stb.append("     COUNT(CSP_MDAT.SCHREGNO) AS CNT_NUM, ");
        stb.append("     '' AS REMARK ");
        stb.append(" FROM ");
        stb.append("    COLLECT_SLIP_PLAN_M_DAT CSP_MDAT ");
        stb.append("    INNER JOIN COLLECT_SLIP_DAT CSD ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = CSD.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = CSD.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = CSD.YEAR ");
        stb.append("      AND CSP_MDAT.SLIP_NO = CSD.SLIP_NO ");
        stb.append("      AND CSP_MDAT.SCHREGNO = CSD.SCHREGNO ");
        stb.append("      AND CSD.YEAR = '" + _param._year + "' ");
        stb.append("      AND CSD.CANCEL_DATE IS NULL ");
        stb.append("    LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT CSPLD ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = CSPLD.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = CSPLD.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = CSPLD.YEAR ");
        stb.append("      AND CSP_MDAT.SCHREGNO = CSPLD.SCHREGNO ");
        stb.append("      AND CSP_MDAT.SLIP_NO = CSPLD.SLIP_NO ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = CSPLD.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = CSPLD.PLAN_MONTH ");
        stb.append("    LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT CSP_PAID1 ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = CSP_PAID1.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = CSP_PAID1.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = CSP_PAID1.YEAR ");
        stb.append("      AND CSP_MDAT.SLIP_NO = CSP_PAID1.SLIP_NO ");
        stb.append("      AND CSP_MDAT.COLLECT_L_CD = CSP_PAID1.COLLECT_L_CD ");
        stb.append("      AND CSP_MDAT.COLLECT_M_CD = CSP_PAID1.COLLECT_M_CD ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = CSP_PAID1.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = CSP_PAID1.PLAN_MONTH ");
        stb.append("      AND CSP_MDAT.SCHREGNO = CSP_PAID1.SCHREGNO ");
        stb.append("      AND CSP_PAID1.PLAN_PAID_MONEY_DIV = '1' ");
        stb.append("      AND MONTH(CSP_PAID1.PLAN_PAID_MONEY_DATE) = " + Integer.parseInt(_param._incomemonth) + " ");
        stb.append("    LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT CSP_PAID2 ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = CSP_PAID2.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = CSP_PAID2.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = CSP_PAID2.YEAR ");
        stb.append("      AND CSP_MDAT.SLIP_NO = CSP_PAID2.SLIP_NO ");
        stb.append("      AND CSP_MDAT.COLLECT_L_CD = CSP_PAID2.COLLECT_L_CD ");
        stb.append("      AND CSP_MDAT.COLLECT_M_CD = CSP_PAID2.COLLECT_M_CD ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = CSP_PAID2.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = CSP_PAID2.PLAN_MONTH ");
        stb.append("      AND CSP_MDAT.SCHREGNO = CSP_PAID2.SCHREGNO ");
        stb.append("      AND CSP_PAID2.PLAN_PAID_MONEY_DIV = '2' ");
        stb.append("      AND MONTH(CSP_PAID2.PLAN_PAID_MONEY_DATE) = " + Integer.parseInt(_param._incomemonth) + " ");
        stb.append("    LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT CSP_PAID3 ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = CSP_PAID3.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = CSP_PAID3.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = CSP_PAID3.YEAR ");
        stb.append("      AND CSP_MDAT.SLIP_NO = CSP_PAID3.SLIP_NO ");
        stb.append("      AND CSP_MDAT.COLLECT_L_CD = CSP_PAID3.COLLECT_L_CD ");
        stb.append("      AND CSP_MDAT.COLLECT_M_CD = CSP_PAID3.COLLECT_M_CD ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = CSP_PAID3.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = CSP_PAID3.PLAN_MONTH ");
        stb.append("      AND CSP_MDAT.SCHREGNO = CSP_PAID3.SCHREGNO ");
        stb.append("      AND CSP_PAID3.PLAN_PAID_MONEY_DIV = '3' ");
        stb.append("      AND MONTH(CSP_PAID3.PLAN_PAID_MONEY_DATE) = " + Integer.parseInt(_param._incomemonth) + " ");
        stb.append("    LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT RCPD ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = RCPD.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = RCPD.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = RCPD.YEAR ");
        stb.append("      AND CSP_MDAT.SLIP_NO = RCPD.SLIP_NO ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = RCPD.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = RCPD.PLAN_MONTH ");
        stb.append("      AND CSP_MDAT.SCHREGNO = RCPD.SCHREGNO ");
        stb.append("      AND RCPD.PLAN_MONEY IS NOT NULL");
        stb.append("    LEFT JOIN REDUCTION_PLAN_DAT RPD ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = RPD.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = RPD.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = RPD.YEAR ");
        stb.append("      AND CSP_MDAT.SCHREGNO = RPD.SCHREGNO ");
        stb.append("      AND CSP_MDAT.SLIP_NO = RPD.SLIP_NO ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = RPD.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = RPD.PLAN_MONTH ");
        stb.append("      AND RPD.PLAN_CANCEL_FLG IS NULL ");
        stb.append("    LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT RSPD1 ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = RSPD1.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = RSPD1.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = RSPD1.YEAR ");
        stb.append("      AND CSP_MDAT.SCHREGNO = RSPD1.SCHREGNO ");
        stb.append("      AND CSP_MDAT.SLIP_NO = RSPD1.SLIP_NO ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = RSPD1.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = RSPD1.PLAN_MONTH ");
        stb.append("      AND RSPD1.REDUCTION_TARGET = '2' ");
        stb.append("      AND RSPD1.PLAN_CANCEL_FLG IS NULL ");
        stb.append("    LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT RSPD2 ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = RSPD2.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = RSPD2.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = RSPD2.YEAR ");
        stb.append("      AND CSP_MDAT.SCHREGNO = RSPD2.SCHREGNO ");
        stb.append("      AND CSP_MDAT.SLIP_NO = RSPD2.SLIP_NO ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = RSPD2.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = RSPD2.PLAN_MONTH ");
        stb.append("      AND RSPD2.REDUCTION_TARGET = '1' ");
        stb.append("      AND RSPD2.PLAN_CANCEL_FLG IS NULL ");
        stb.append("    LEFT JOIN COLLECT_M_MST CMMST ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = CMMST.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = CMMST.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = CMMST.YEAR ");
        stb.append("      AND CSP_MDAT.COLLECT_L_CD = CMMST.COLLECT_L_CD ");
        stb.append("      AND CSP_MDAT.COLLECT_M_CD = CMMST.COLLECT_M_CD ");
        stb.append("    LEFT JOIN BEFOREPAYMENTDATA BPD ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = BPD.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = BPD.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = BPD.YEAR ");
        stb.append("      AND CSP_MDAT.SCHREGNO = BPD.SCHREGNO ");
        stb.append("      AND CSP_MDAT.SLIP_NO = BPD.SLIP_NO ");
        stb.append("      AND CSP_MDAT.COLLECT_L_CD = BPD.COLLECT_L_CD ");
        stb.append("      AND CSP_MDAT.COLLECT_M_CD = BPD.COLLECT_M_CD ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = BPD.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = BPD.PLAN_MONTH ");
        stb.append("    LEFT JOIN NOTPAYMENTDATA NPD ");
        stb.append("      ON CSP_MDAT.SCHOOLCD = NPD.SCHOOLCD ");
        stb.append("      AND CSP_MDAT.SCHOOL_KIND = NPD.SCHOOL_KIND ");
        stb.append("      AND CSP_MDAT.YEAR = NPD.YEAR ");
        stb.append("      AND CSP_MDAT.SCHREGNO = NPD.SCHREGNO ");
        stb.append("      AND CSP_MDAT.SLIP_NO = NPD.SLIP_NO ");
        stb.append("      AND CSP_MDAT.COLLECT_L_CD = NPD.COLLECT_L_CD ");
        stb.append("      AND CSP_MDAT.COLLECT_M_CD = NPD.COLLECT_M_CD ");
        stb.append("      AND CSP_MDAT.PLAN_YEAR = NPD.PLAN_YEAR ");
        stb.append("      AND CSP_MDAT.PLAN_MONTH = NPD.PLAN_MONTH ");
        stb.append("    , GET_MAXSEMESTER_DATA GMSD ");
        stb.append(" WHERE ");
        stb.append("     CSP_MDAT.PLAN_CANCEL_FLG IS NULL ");
        stb.append("     AND CSP_MDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND CSP_MDAT.PLAN_CANCEL_FLG IS NULL ");
        stb.append("     AND CSP_MDAT.SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("     AND CSP_MDAT.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("     AND CSP_MDAT.YEAR = GMSD.YEAR ");
        stb.append("     AND CSP_MDAT.SCHREGNO = GMSD.SCHREGNO ");
        stb.append("     AND EXISTS ( ");
        stb.append("         SELECT 1 ");
        stb.append("         FROM ");
        stb.append("           GET_MAXSEMESTER_DATA GMSD ");
        stb.append("         WHERE ");
        stb.append("           CSP_MDAT.YEAR = GMSD.YEAR ");
        stb.append("           AND CSP_MDAT.SCHREGNO = GMSD.SCHREGNO ");
        stb.append("           AND GMSD.GRADE = '" + gradestr + "' ");
        stb.append("         ) ");
        stb.append(" GROUP BY ");
        stb.append("     CSP_MDAT.SCHOOLCD, ");
        stb.append("     CSP_MDAT.SCHOOL_KIND, ");
        stb.append("     CSP_MDAT.YEAR, ");
        stb.append("     CSP_MDAT.PLAN_YEAR, ");
        stb.append("     CSP_MDAT.PLAN_MONTH, ");
        stb.append("     CSP_MDAT.COLLECT_L_CD, ");
        stb.append("     CSP_MDAT.COLLECT_M_CD, ");
        stb.append("     CSP_MDAT.PLAN_MONEY, ");
        stb.append("     RCPD.ADD_PLAN_CANCEL_FLG, ");
        stb.append("     CMMST.GAKUNOKIN_DIV, ");
        stb.append("     CMMST.REDUCTION_DIV ");
        stb.append(" ORDER BY ");
        stb.append("     CSP_MDAT.COLLECT_L_CD, ");
        stb.append("     CSP_MDAT.COLLECT_M_CD, ");
        stb.append("     CSP_MDAT.PLAN_YEAR, ");
        stb.append("     CSP_MDAT.PLAN_MONTH ");
        stb.append(" ), LASTSUMDATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   CSP_PDAT.SCHOOLCD, ");
        stb.append("   CSP_PDAT.SCHOOL_KIND, ");
        stb.append("   CSP_PDAT.YEAR, ");
        stb.append("   CSP_PDAT.PLAN_YEAR, ");
        stb.append("   CSP_PDAT.PLAN_MONTH, ");
        stb.append("   CSP_PDAT.COLLECT_L_CD, ");
        stb.append("   CSP_PDAT.COLLECT_M_CD, ");
        stb.append("   SUM(CSP_PDAT.PLAN_PAID_MONEY) AS CARRYOVER ");
        stb.append(" FROM ");
        stb.append("   COLLECT_SLIP_PLAN_PAID_M_DAT CSP_PDAT ");
        stb.append("   INNER JOIN COLLECT_SLIP_DAT CSD ");
        stb.append("     ON CSP_PDAT.SCHOOLCD = CSD.SCHOOLCD ");
        stb.append("     AND CSP_PDAT.SCHOOL_KIND = CSD.SCHOOL_KIND ");
        stb.append("     AND CSP_PDAT.YEAR = CSD.YEAR ");
        stb.append("     AND CSP_PDAT.SLIP_NO = CSD.SLIP_NO ");
        stb.append("     AND CSP_PDAT.SCHREGNO = CSD.SCHREGNO ");
        stb.append("     AND CSD.YEAR = '" + _param._year+ "' ");
        stb.append("     AND CSD.CANCEL_DATE IS NULL ");
        stb.append(" WHERE ");
        stb.append("   CSP_PDAT.YEAR = '" + _param._year + "' ");
        if (Integer.parseInt(_param._incomemonth) >= 4) {
            stb.append("   AND (MONTH(CSP_PDAT.PLAN_PAID_MONEY_DATE) < " + Integer.parseInt(_param._incomemonth) + " AND MONTH(CSP_PDAT.PLAN_PAID_MONEY_DATE) > 3) ");
        } else {
            stb.append("   AND (MONTH(CSP_PDAT.PLAN_PAID_MONEY_DATE) < " + Integer.parseInt(_param._incomemonth) + " OR MONTH(CSP_PDAT.PLAN_PAID_MONEY_DATE) > 4) ");
        }
        stb.append("   AND CSP_PDAT.SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("   AND CSP_PDAT.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append(" GROUP BY ");
        stb.append("   CSP_PDAT.SCHOOLCD, ");
        stb.append("   CSP_PDAT.SCHOOL_KIND, ");
        stb.append("   CSP_PDAT.YEAR, ");
        stb.append("   CSP_PDAT.PLAN_YEAR, ");
        stb.append("   CSP_PDAT.PLAN_MONTH, ");
        stb.append("   CSP_PDAT.COLLECT_L_CD, ");
        stb.append("   CSP_PDAT.COLLECT_M_CD ");
        stb.append(" ), GET_SUBSUM_DATA AS ( ");
        stb.append("    SELECT ");
        stb.append("     GSD.SCHOOLCD, ");
        stb.append("     GSD.SCHOOL_KIND, ");
        stb.append("     GSD.PLAN_YEAR, ");
        stb.append("     GSD.PLAN_MONTH, ");
        stb.append("     GSD.COLLECT_L_CD, ");
        stb.append("     GSD.COLLECT_M_CD, ");
        stb.append("     SUM(GSD.CNT_NUM) AS CNT_NUM, ");
        stb.append("     SUM(GSD.TOTAL_PLAN_MONEY) AS TOTAL_PRICE ");
        stb.append(" FROM ");
        stb.append("   GET_SUM_DATA GSD ");
        stb.append(" GROUP BY ");
        stb.append("   GSD.SCHOOLCD, ");
        stb.append("   GSD.SCHOOL_KIND, ");
        stb.append("   GSD.PLAN_YEAR, ");
        stb.append("   GSD.PLAN_MONTH, ");
        stb.append("   GSD.COLLECT_L_CD, ");
        stb.append("   GSD.COLLECT_M_CD ");
        stb.append(" )  SELECT ");
        stb.append("     GSD.SCHOOLCD, ");
        stb.append("     GSD.SCHOOL_KIND, ");
        stb.append("     GSD.PLAN_YEAR, ");
        stb.append("     GSD.PLAN_MONTH, ");
        stb.append("     CLMST.COLLECT_L_NAME, ");
        stb.append("     CMMST.COLLECT_M_NAME, ");
        stb.append("     CMMST.KOUHI_SHIHI, ");
        stb.append("     GSD.PLAN_MONEY, ");
        stb.append("     LSD.CARRYOVER, ");
        stb.append("     GSD.CNT1, ");
        stb.append("     GSD.PRICE1, ");
        stb.append("     GSD.CNT2, ");
        stb.append("     GSD.PRICE2, ");
        stb.append("     GSD.CNT3, ");
        stb.append("     GSD.PRICE3, ");
        stb.append("     GSD.CNT4, ");
        stb.append("     GSD.PRICE4, ");
        stb.append("     GSD.CNT5, ");
        stb.append("     GSD.PRICE5, ");
        stb.append("     GSD.CNT6, ");
        stb.append("     GSD.PRICE6, ");
        stb.append("     GSD.CNT7, ");
        stb.append("     GSD.PRICE7, ");
        stb.append("     GSD.CNT10, ");
        stb.append("     GSD.PRICE10, ");
        stb.append("     GSD.CNT11, ");
        stb.append("     GSD.PRICE11, ");
        stb.append("     GSD.TOTAL_PLAN_MONEY, ");
        stb.append("     GSSD.TOTAL_PRICE, ");
        stb.append("     GSSD.CNT_NUM, ");
        stb.append("     GSD.REMARK ");
        stb.append(" FROM ");
        stb.append("   GET_SUM_DATA GSD ");
        stb.append("   LEFT JOIN COLLECT_L_MST CLMST ");
        stb.append("     ON GSD.SCHOOLCD = CLMST.SCHOOLCD ");
        stb.append("     AND GSD.SCHOOL_KIND = CLMST.SCHOOL_KIND ");
        stb.append("     AND GSD.YEAR = CLMST.YEAR ");
        stb.append("     AND GSD.COLLECT_L_CD = CLMST.COLLECT_L_CD ");
        stb.append("   LEFT JOIN COLLECT_M_MST CMMST ");
        stb.append("     ON GSD.SCHOOLCD = CMMST.SCHOOLCD ");
        stb.append("     AND GSD.SCHOOL_KIND = CMMST.SCHOOL_KIND ");
        stb.append("     AND GSD.YEAR = CMMST.YEAR ");
        stb.append("     AND GSD.COLLECT_L_CD = CMMST.COLLECT_L_CD ");
        stb.append("     AND GSD.COLLECT_M_CD = CMMST.COLLECT_M_CD ");
        stb.append("   LEFT JOIN LASTSUMDATA LSD ");
        stb.append("     ON GSD.SCHOOLCD = LSD.SCHOOLCD ");
        stb.append("     AND GSD.SCHOOL_KIND = LSD.SCHOOL_KIND ");
        stb.append("     AND GSD.YEAR = LSD.YEAR ");
        stb.append("     AND GSD.PLAN_YEAR = LSD.PLAN_YEAR ");
        stb.append("     AND GSD.PLAN_MONTH = LSD.PLAN_MONTH ");
        stb.append("     AND GSD.COLLECT_L_CD = LSD.COLLECT_L_CD ");
        stb.append("     AND GSD.COLLECT_M_CD = LSD.COLLECT_M_CD ");
        stb.append("   LEFT JOIN GET_SUBSUM_DATA GSSD ");
        stb.append("     ON GSD.SCHOOLCD = GSSD.SCHOOLCD ");
        stb.append("     AND GSD.SCHOOL_KIND = GSSD.SCHOOL_KIND ");
        stb.append("     AND GSD.PLAN_YEAR = GSSD.PLAN_YEAR ");
        stb.append("     AND GSD.PLAN_MONTH = GSSD.PLAN_MONTH ");
        stb.append("     AND GSD.COLLECT_L_CD = GSSD.COLLECT_L_CD ");
        stb.append("     AND GSD.COLLECT_M_CD = GSSD.COLLECT_M_CD ");
        stb.append(" ORDER BY ");
        stb.append("     GSD.COLLECT_L_CD, ");
        stb.append("     GSD.COLLECT_M_CD, ");
        stb.append("     GSD.PLAN_YEAR, ");
        stb.append("     GSD.PLAN_MONTH ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70120 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _incomemonth;
        final String[] _category_selected;
        final List _csnames;
        final String _incatsel;
        final String _year;
        final String _schoolcd;
        final String _form;
        String _schoolkind;
        String _formName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _incomemonth = request.getParameter("INCOMEMONTH");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");

            _csnames = new ArrayList();
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _category_selected.length; ia++) {
                if (_category_selected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_category_selected[ia]);
                sbx.append("'");
                _csnames.add(getGradeNameMst(db2, _category_selected[ia]));
            }
            sbx.append(")");
            _incatsel = sbx.toString();

            _schoolcd = request.getParameter("SCHOOLCD");
            //_schoolkind は実際の処理(grade単位)で動的に設定。
            _form = request.getParameter("FORM");
            _formName = "1".equals(_form) ? "KNJP803" :  "KNJP803_2";
        }

        private String getSchKind(final DB2UDB db2, final String gradestr) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + gradestr + "' ORDER BY SCHOOL_KIND DESC";
                log.debug("namesql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        private String getGradeNameMst(final DB2UDB db2, final String grade) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' ";
                log.debug("namesql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE_NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

