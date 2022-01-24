// kanji=漢字
/*
 * $Id: KnjClaimDetailsDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * CLAIM_DETAILS_DATを更新する。
 * @author m-yama
 * @version $Id: KnjClaimDetailsDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjClaimDetailsDat2 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjClaimDetailsDat2.class);

    public KnjClaimDetailsDat2() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "伝票明細データ"; }

    void migrate() throws SQLException {
        final Map claimMap = loadClaimDetails();
        final Map paymentMap = loadPayment();

        log.debug("伝票件数=" + claimMap.size());
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
                final ClaimDetails claimDetails = (ClaimDetails) itDetail.next();
//                log.debug(claimDetails);

                final int payMoney = claimDetails._paymentMoney;
                if (syoriMoney >= payMoney) {
                    syoriMoney -= payMoney;
                    claimDetails._paymentDate = date[moneyCnt];
                } else {
                    if (syoriMoney > 0) {
                        syoriMoney = 0;
                        claimDetails._paymentDate = date[moneyCnt];
                    }

                    moneyCnt++;
                    if (moneyCnt == cnt) {
                        updateClaim(claimDetails);
                        break;
                    }

                    syoriMoney += money[moneyCnt];
                    if (syoriMoney >= payMoney) {
                        syoriMoney -= payMoney;
                        claimDetails._paymentDate = date[moneyCnt];
                    }
                }
                updateClaim(claimDetails);
            }
        }
    }

    private void updateClaim(final ClaimDetails claimDetails) {
          final StringBuffer stb = new StringBuffer();
          stb.append(" UPDATE ");
          stb.append("     CLAIM_DETAILS_DAT ");
          stb.append(" SET ");
          stb.append("     PAYMENT_DATE = '" + claimDetails._paymentDate + "' ");
          stb.append(" WHERE ");
          stb.append("     SLIP_NO = '" + claimDetails._slipNo + "' ");
          stb.append("     AND SEQ = '" + claimDetails._seq + "' ");
        
          try {
              _db2.stmt.executeUpdate(stb.toString());
              _db2.commit();
          } catch (SQLException e) {
              log.error("SQLException", e);
          }
    }

    private Map loadClaimDetails() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     SEQ, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     PAYMENT_MONEY, ");
        stb.append("     PAYMENT_DATE ");
        stb.append(" FROM ");
        stb.append("     CLAIM_DETAILS_DAT ");
        stb.append(" WHERE ");
        stb.append("     PAYMENT_DATE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     SLIP_NO, ");
        stb.append("     PRIORITY_LEVEL ");

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

            final Date paymentDate = (Date) map.get("PAYMENT_DATE");
            final ClaimDetails claimDetails = new ClaimDetails(
                                                    (String) map.get("SLIP_NO"),
                                                    (String) map.get("SEQ"),
                                                    applicantNo,
                                                    ((Number) map.get("PAYMENT_MONEY")).intValue(),
                                                    paymentDate.toString()
                                              );

            retList.add(claimDetails);
            retMap.put(applicantNo, new ArrayList(retList));
        }
        return retMap;
    }

    private class ClaimDetails {
        final String _slipNo;
        final String _seq;
        final String _applicantNo;
        final int _paymentMoney;
        String _paymentDate;
 
        public ClaimDetails(
                final String slipNo,
                final String seq,
                final String applicantNo,
                final int paymentMoney,
                final String paymentDate
        ) {
            _slipNo = slipNo;
            _seq = seq;
            _applicantNo = applicantNo;
            _paymentMoney = paymentMoney;
            _paymentDate = paymentDate;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return " 伝票：" + _slipNo
                  + " SEQ：" + _seq
                  + " 志願：" + _applicantNo
                  + " 金額：" + _paymentMoney
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

