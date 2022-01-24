// kanji=漢字
/*
 * $Id: KnjPaymentMoneyHistDetailsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.Date;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PAYMENT_MONEY_HIST_DETAILS_DATを作る。
 * @author takaesu
 * @version $Id: KnjPaymentMoneyHistDetailsDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjPaymentMoneyHistDetailsDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjPaymentMoneyHistDetailsDat.class);

    private static final String KNJTABLE = "PAYMENT_MONEY_HIST_DETAILS_DAT";

    public KnjPaymentMoneyHistDetailsDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "入金詳細データ"; }

    void migrate() throws SQLException {
        final Map claimMap = loadSales();
        final Map paymentMap = loadPayment();

        log.debug("売上計画件数=" + claimMap.size());
        log.debug("入金件数=" + paymentMap.size());

        updateSalesPlan(claimMap, paymentMap);
    }

    /**
     * 
     */
    private void updateSalesPlan(final Map claimMap, final Map paymentMap) {

        for (final Iterator itClaim = claimMap.keySet().iterator(); itClaim.hasNext();) {
            final String applicantNo = (String) itClaim.next();
            final List claimList = (List) claimMap.get(applicantNo);

            final List paymentMoney = (List) paymentMap.get(applicantNo);

            if (null == paymentMoney) {
                continue;
            }
            final int[] money = new int[paymentMoney.size()];

            final String[] date = new String[paymentMoney.size()];
            int cnt = 0;
            for (final Iterator iter = paymentMoney.iterator(); iter.hasNext();) {
                final Payment payment = (Payment) iter.next();
                money[cnt] = payment._paymentMoney;
                date[cnt] = payment._paymentDate;
                cnt++;
//                log.debug(payment);
            }

            int moneyCnt = 0;
            int syoriMoney = money[moneyCnt];
            String befDate = "";
            int seq = 0;
            for (final Iterator itDetail = claimList.iterator(); itDetail.hasNext();) {
                final Sales sales = (Sales) itDetail.next();
//                log.debug(sales);

                if (syoriMoney >= sales._zanMoney) {
                    syoriMoney -= sales._zanMoney;
                    sales._zanMoney = 0;
                    sales._paymentDate = date[moneyCnt];
                } else {
                    if (syoriMoney > 0) {
                        sales._payMoney = syoriMoney;
                        sales._paymentDate = date[moneyCnt];
                        seq = befDate.equals(sales._paymentDate) ? seq + 1 : 1;
                        befDate = sales._paymentDate;
                        insertPaymentDetails(sales, seq);
                        sales._zanMoney -= syoriMoney;
                        syoriMoney = 0;
                    }

                    moneyCnt++;
                    if (moneyCnt == cnt) {
                        break;
                    }

                    syoriMoney += money[moneyCnt];
                    if (syoriMoney >= sales._zanMoney) {
                        sales._payMoney = sales._zanMoney;
                        syoriMoney -= sales._zanMoney;
                        sales._zanMoney = 0;
                        sales._paymentDate = date[moneyCnt];
                    } else {
                        sales._payMoney = syoriMoney;
                        syoriMoney = 0;
                        sales._paymentDate = date[moneyCnt];
                        seq = befDate.equals(sales._paymentDate) ? seq + 1 : 1;
                        befDate = sales._paymentDate;
                        insertPaymentDetails(sales, seq);
                        sales._zanMoney -= syoriMoney;
                    }
                }
                seq = befDate.equals(sales._paymentDate) ? seq + 1 : 1;
                befDate = sales._paymentDate;
                insertPaymentDetails(sales, seq);
            }
        }
    }

    private void insertPaymentDetails(final Sales sales, final int seq) {
          final StringBuffer stb = new StringBuffer();

          stb.append(" INSERT INTO " + KNJTABLE + " ");
          stb.append(" VALUES( ");
          stb.append(" " + getInsertVal(sales._applicantNo) + ", ");
          stb.append(" " + getInsertVal(sales._paymentDate) + ", ");
          stb.append(" " + getInsertVal(String.valueOf(seq)) + ", ");
          stb.append(" " + getInsertVal(sales._slipNo) + ", ");
          stb.append(" " + getInsertVal(sales._commodityCd) + ", ");
          stb.append(" " + getInsertVal(new Integer(sales._payMoney)) + ", ");
          stb.append(" " + getInsertChangeVal(sales._keepingDiv) + ", ");
          stb.append(" '" + Param.REGISTERCD + "', ");
          stb.append(" current timestamp ");
          stb.append(" ) ");

          try {
              _db2.stmt.executeUpdate(stb.toString());
              _db2.commit();
          } catch (SQLException e) {
              log.error("SQLException " + stb, e);
          }
    }

    private Map loadSales() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     1 AS ORDER_CD, ");
        stb.append("     T1.S_YEAR_MONTH AS YEAR_MONTH, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.COMMODITY_CD, ");
        stb.append("     L1.DIVIDING_MULTIPLICATION_DIV AS KEEPING_DIV, ");
        stb.append("     PAYMENT_MONEY AS MONEY ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DETAILS_DAT T1 ");
        stb.append("     LEFT JOIN COMMODITY_MST L1 ON T1.COMMODITY_CD = L1.COMMODITY_CD ");
        stb.append(" WHERE ");
        stb.append("     VALUE(PAYMENT_MONEY, 0) > 0 ");
        stb.append(" UNION   ");
        stb.append(" SELECT ");
        stb.append("     2 AS ORDER_CD, ");
        stb.append("     T1.PLAN_YEAR || T1.PLAN_MONTH AS YEAR_MONTH, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.COMMODITY_CD, ");
        stb.append("     L1.DIVIDING_MULTIPLICATION_DIV AS KEEPING_DIV, ");
        stb.append("     KEEPING_MONEY AS MONEY ");
        stb.append(" FROM ");
        stb.append("     SALES_PLAN_DAT T1 ");
        stb.append("     LEFT JOIN COMMODITY_MST L1 ON T1.COMMODITY_CD = L1.COMMODITY_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.KEEPING_DATE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     APPLICANTNO, ");
        stb.append("     ORDER_CD, ");
        stb.append("     YEAR_MONTH ");

        log.debug("sql=" + stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("伝票明細データ取込みでエラー", e);
            throw e;
        }

        Map retMap = new TreeMap();
        final List retList = new ArrayList();
        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String applicantNo = (String) map.get("APPLICANTNO");

            if (!retMap.containsKey(applicantNo)) {
                retList.clear();
            }

            final Sales sales = new Sales(applicantNo,
                                          (String) map.get("SLIP_NO"),
                                          (String) map.get("SEQ"),
                                          (String) map.get("COMMODITY_CD"),
                                          (String) map.get("KEEPING_DIV"),
                                          ((Number) map.get("MONEY")).intValue()
                                         );

            retList.add(sales);
            retMap.put(applicantNo, new ArrayList(retList));
        }
        return retMap;
    }

    private class Sales {
        final String _applicantNo;
        final String _slipNo;
        final String _seq;
        final String _commodityCd;
        final String _keepingDiv;
        final int _money;
        int _zanMoney;
        int _payMoney;
        String _paymentDate;
 
        public Sales(
                final String applicantNo,
                final String slipNo,
                final String seq,
                final String commodityCd,
                final String keepingDiv,
                final int money
        ) {
            _applicantNo = applicantNo;
            _slipNo = slipNo;
            _seq = seq;
            _commodityCd = commodityCd;
            _keepingDiv = keepingDiv;
            _money = money;
            _zanMoney = money;
            _payMoney = money;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return " 伝票：" + _slipNo
                  + " SEQ：" + _seq
                  + " 志願：" + _applicantNo
                  + " 日付：" + _paymentDate;
        }
    }

    private Map loadPayment() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     APPLICANTNO, ");
        stb.append("     PAYMENT_DATE, ");
        stb.append("     PAYMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     PAYMENT_MONEY_HIST_DAT ");
        stb.append(" ORDER BY ");
        stb.append("     APPLICANTNO, ");
        stb.append("     DATE(PAYMENT_DATE) ");

        log.debug("sql=" + stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("入金データ取込みでエラー", e);
            throw e;
        }

        Map retMap = new TreeMap();
        final List retList = new ArrayList();
        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String applicantNo = (String) map.get("APPLICANTNO");

            if (!retMap.containsKey(applicantNo)) {
                retList.clear();
            }

            final Date paymentDate = (Date) map.get("PAYMENT_DATE");
            final Payment payment = new Payment(applicantNo,
                                                ((Number) map.get("PAYMENT_MONEY")).intValue(),
                                                paymentDate.toString()
                                               );

            retList.add(payment);
            retMap.put(applicantNo, new ArrayList(retList));
        }
        return retMap;
    }

    private class Payment {
        final String _applicantNo;
        final int _paymentMoney;
        String _paymentDate;
 
        public Payment(
                final String applicantNo,
                final int paymentMoney,
                final String paymentDate
        ) {
            _applicantNo = applicantNo;
            _paymentMoney = paymentMoney;
            _paymentDate = paymentDate;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return " 志願：" + _applicantNo
                  + " 金額：" + _paymentMoney
                  + " 日付：" + _paymentDate;
        }
    }
}
// eof

