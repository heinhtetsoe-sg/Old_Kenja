// kanji=漢字
/*
 * $Id: KnjSalesPlanDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/10/14 15:46:35 - JST
 * 作成者: m-yama
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
 * SALES_PLAN_DATを更新する。
 * @author m-yama
 * @version $Id: KnjSalesPlanDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSalesPlanDat2 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSalesPlanDat2.class);

    public KnjSalesPlanDat2() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "売上計画データ"; }

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
            for (final Iterator itDetail = claimList.iterator(); itDetail.hasNext();) {
                final Sales sales = (Sales) itDetail.next();
//                log.debug(sales);

                final int payMoney = sales._money;
                if (syoriMoney >= payMoney) {
                    syoriMoney -= payMoney;
                    sales._paymentDate = date[moneyCnt];
                } else {
                    if (syoriMoney > 0) {
                        syoriMoney = 0;
                        sales._paymentDate = date[moneyCnt];
                    }

                    moneyCnt++;
                    if (moneyCnt == cnt) {
                        if (sales._keepDiv.equals("1")) {
                            updateClaim(sales);
                        }
                        break;
                    }

                    syoriMoney += money[moneyCnt];
                    if (syoriMoney >= payMoney) {
                        syoriMoney -= payMoney;
                        sales._paymentDate = date[moneyCnt];
                    }
                }
                if (sales._keepDiv.equals("1")) {
                    updateClaim(sales);
                }
            }
        }
    }

    private void updateClaim(final Sales sales) {
          final StringBuffer stb = new StringBuffer();
          stb.append(" UPDATE ");
          stb.append("     SALES_PLAN_DAT ");
          stb.append(" SET ");
          stb.append("     KEEPING_DATE = '" + sales._paymentDate + "' ");
          stb.append(" WHERE ");
          stb.append("     YEAR = '" + sales._year + "' ");
          stb.append("     AND APPLICANTNO = '" + sales._applicantNo + "' ");
          stb.append("     AND PLAN_YEAR = '" + sales._planYear + "' ");
          stb.append("     AND PLAN_MONTH = '" + sales._planMonth + "' ");
          stb.append("     AND SLIP_NO = '" + sales._slipNo + "' ");
          stb.append("     AND SEQ = '" + sales._seq + "' ");

          try {
              _db2.stmt.executeUpdate(stb.toString());
              _db2.commit();
          } catch (SQLException e) {
              log.error("SQLException", e);
          }
    }

    private Map loadSales() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     PLAN_YEAR, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     SLIP_NO, ");
        stb.append("     SEQ, ");
        stb.append("     CASE WHEN KEEPING_DATE IS NOT NULL ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '2' ");
        stb.append("     END AS KEEP_DIV, ");
        stb.append("     CASE WHEN KEEPING_DATE IS NOT NULL ");
        stb.append("          THEN KEEPING_MONEY ");
        stb.append("          ELSE SUMMING_UP_MONEY ");
        stb.append("     END AS MONEY ");
        stb.append(" FROM ");
        stb.append("     SALES_PLAN_DAT ");
        stb.append(" WHERE ");
        stb.append("     SUMMING_UP_DATE IS NOT NULL ");
        stb.append("     OR KEEPING_DATE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     YEAR, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     PLAN_YEAR, ");
        stb.append("     PLAN_MONTH ");

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

            final Sales sales = new Sales((String) map.get("YEAR"),
                                          applicantNo,
                                          (String) map.get("PLAN_YEAR"),
                                          (String) map.get("PLAN_MONTH"),
                                          (String) map.get("SLIP_NO"),
                                          (String) map.get("SEQ"),
                                          (String) map.get("KEEP_DIV"),
                                          ((Number) map.get("MONEY")).intValue()
                                         );

            retList.add(sales);
            retMap.put(applicantNo, new ArrayList(retList));
        }
        return retMap;
    }

    private class Sales {
        final String _year;
        final String _applicantNo;
        final String _planYear;
        final String _planMonth;
        final String _slipNo;
        final String _seq;
        final String _keepDiv;
        final int _money;
        String _paymentDate;
 
        public Sales(
                final String year,
                final String applicantNo,
                final String planYear,
                final String planMonth,
                final String slipNo,
                final String seq,
                final String keepDiv,
                final int money
        ) {
            _year = year;
            _applicantNo = applicantNo;
            _planYear = planYear;
            _planMonth = planMonth;
            _slipNo = slipNo;
            _seq = seq;
            _keepDiv = keepDiv;
            _money = money;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return " 伝票：" + _slipNo
                  + " SEQ：" + _seq
                  + " 志願：" + _applicantNo
                  + " 計画：" + _planYear + _planMonth
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

