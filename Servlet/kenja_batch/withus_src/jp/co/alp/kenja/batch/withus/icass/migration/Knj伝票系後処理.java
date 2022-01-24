// kanji=漢字
/*
 * $Id: Knj莨晉･ｨ邉ｻ蠕悟�ｦ逅�.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 伝票系の後処理を行う。
 * @author takaesu
 * @version $Id: Knj莨晉･ｨ邉ｻ蠕悟�ｦ逅�.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Knj伝票系後処理 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(Knj伝票系後処理.class);

    private static DecimalFormat _monthFormat = new DecimalFormat("00");

    public Knj伝票系後処理() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "伝票系の後処理"; }

    void migrate() throws SQLException {
        updateSales1();
        updateSales2();
    }

    /**
     * 計上済みの売上計画の計画年度と年月を、計上日に変更する。
     */
    private void updateSales1() throws SQLException {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYO_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR AS MOTO_YEAR, ");
        stb.append("     PLAN_YEAR AS MOTO_PLAN_YEAR, ");
        stb.append("     PLAN_MONTH AS MOTO_PLAN_MONTH, ");
        stb.append("     CASE WHEN MONTH(SUMMING_UP_DATE) < 4 ");
        stb.append("          THEN CAST(YEAR(SUMMING_UP_DATE) - 1 AS CHAR(4)) ");
        stb.append("          ELSE CAST(YEAR(SUMMING_UP_DATE) AS CHAR(4)) ");
        stb.append("     END AS YEAR, ");
        stb.append("     APPLICANTNO, ");
        stb.append("     substr(CAST(SUMMING_UP_DATE AS CHAR(10)), 1, 4) AS PLAN_YEAR, ");
        stb.append("     substr(CAST(SUMMING_UP_DATE AS CHAR(10)), 6, 2) AS PLAN_MONTH, ");
        stb.append("     SLIP_NO, ");
        stb.append("     SEQ, ");
        stb.append("     TOTAL_CLAIM_MONEY, ");
        stb.append("     PRICE, ");
        stb.append("     TAX, ");
        stb.append("     SUMMING_UP_MONEY ");
        stb.append(" FROM ");
        stb.append("     SALES_PLAN_DAT ");
        stb.append(" WHERE ");
        stb.append("     SUMMING_UP_DATE IS NOT NULL ");
        stb.append("     AND ( ");
        stb.append("         CASE WHEN INT(PLAN_MONTH) < 4 ");
        stb.append("              THEN INT(PLAN_YEAR) - 1 ");
        stb.append("              ELSE INT(PLAN_YEAR) ");
        stb.append("         END ");
        stb.append("         > ");
        stb.append("         CASE WHEN MONTH(SUMMING_UP_DATE) < 4 ");
        stb.append("              THEN YEAR(SUMMING_UP_DATE) - 1 ");
        stb.append("              ELSE YEAR(SUMMING_UP_DATE) ");
        stb.append("         END ");
        stb.append("         OR CASE WHEN INT(PLAN_MONTH) < 4 ");
        stb.append("              THEN INT(PLAN_MONTH) + 12 ");
        stb.append("              ELSE INT(PLAN_MONTH) ");
        stb.append("         END ");
        stb.append("         > ");
        stb.append("         CASE WHEN MONTH(SUMMING_UP_DATE) < 4 ");
        stb.append("              THEN MONTH(SUMMING_UP_DATE) + 12 ");
        stb.append("              ELSE MONTH(SUMMING_UP_DATE) ");
        stb.append("         END ");
        stb.append("         ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     'UPD' AS INSUPD_DIV, ");
        stb.append("     T1.MOTO_YEAR, ");
        stb.append("     T1.MOTO_PLAN_YEAR, ");
        stb.append("     T1.MOTO_PLAN_MONTH, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.PLAN_YEAR, ");
        stb.append("     T1.PLAN_MONTH, ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.TOTAL_CLAIM_MONEY, ");
        stb.append("     T1.PRICE, ");
        stb.append("     T1.TAX, ");
        stb.append("     T1.SUMMING_UP_MONEY ");
        stb.append(" FROM ");
        stb.append("     TAISYO_DATA T1 ");
        stb.append(" WHERE ");
        stb.append("     EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             SALES_PLAN_DAT T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.YEAR = T2.YEAR ");
        stb.append("             AND T1.APPLICANTNO = T2.APPLICANTNO ");
        stb.append("             AND T1.PLAN_YEAR = T2.PLAN_YEAR ");
        stb.append("             AND T1.PLAN_MONTH = T2.PLAN_MONTH ");
        stb.append("             AND T1.SLIP_NO = T2.SLIP_NO ");
        stb.append("             AND T1.SEQ = T2.SEQ ");
        stb.append("     ) ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     'INS' AS INSUPD_DIV, ");
        stb.append("     T1.MOTO_YEAR, ");
        stb.append("     T1.MOTO_PLAN_YEAR, ");
        stb.append("     T1.MOTO_PLAN_MONTH, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.PLAN_YEAR, ");
        stb.append("     T1.PLAN_MONTH, ");
        stb.append("     T1.SLIP_NO, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.TOTAL_CLAIM_MONEY, ");
        stb.append("     T1.PRICE, ");
        stb.append("     T1.TAX, ");
        stb.append("     T1.SUMMING_UP_MONEY ");
        stb.append(" FROM ");
        stb.append("     TAISYO_DATA T1 ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             SALES_PLAN_DAT T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.YEAR = T2.YEAR ");
        stb.append("             AND T1.APPLICANTNO = T2.APPLICANTNO ");
        stb.append("             AND T1.PLAN_YEAR = T2.PLAN_YEAR ");
        stb.append("             AND T1.PLAN_MONTH = T2.PLAN_MONTH ");
        stb.append("             AND T1.SLIP_NO = T2.SLIP_NO ");
        stb.append("             AND T1.SEQ = T2.SEQ ");
        stb.append("     ) ");

        log.debug(stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String insUpdDiv = (String) map.get("INSUPD_DIV");
            String sql = "";
            if (insUpdDiv.equals("INS")) {
                sql = getInsSql(map);
            } else {
                sql = getUpdSql(map);
            }
            try {
                _db2.stmt.executeUpdate(sql);
                _db2.commit();
            } catch (SQLException e) {
                log.error("SQLException = " + sql, e);
            }

            if (insUpdDiv.equals("UPD")) {
                sql = getDelSql(map);
            }
            try {
                _db2.stmt.executeUpdate(sql);
                _db2.commit();
            } catch (SQLException e) {
                log.error("SQLException = " + sql, e);
            }
        }

    }

    private String getInsSql(final Map map) {

        final StringBuffer stb = new StringBuffer();

        stb.append(" UPDATE SALES_PLAN_DAT ");
        stb.append("     SET (YEAR, ");
        stb.append("          PLAN_YEAR, ");
        stb.append("          PLAN_MONTH) = ");
        stb.append("     ('" + map.get("YEAR") + "', ");
        stb.append("      '" + map.get("PLAN_YEAR") + "', ");
        stb.append("      '" + map.get("PLAN_MONTH") + "') ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + map.get("MOTO_YEAR") + "' ");
        stb.append("     AND APPLICANTNO = '" + map.get("APPLICANTNO") + "' ");
        stb.append("     AND PLAN_YEAR = '" + map.get("MOTO_PLAN_YEAR") + "' ");
        stb.append("     AND PLAN_MONTH = '" + map.get("MOTO_PLAN_MONTH") + "' ");
        stb.append("     AND SLIP_NO = '" + map.get("SLIP_NO") + "' ");
        stb.append("     AND SEQ = '" + map.get("SEQ") + "' ");

        return stb.toString();
    }

    private String getUpdSql(final Map map) {

        final StringBuffer stb = new StringBuffer();

        stb.append(" UPDATE SALES_PLAN_DAT ");
        stb.append("     SET (TOTAL_CLAIM_MONEY, ");
        stb.append("          PRICE, ");
        stb.append("          TAX, ");
        stb.append("          SUMMING_UP_MONEY) = ");
        stb.append("     (TOTAL_CLAIM_MONEY + " + map.get("TOTAL_CLAIM_MONEY") + ", ");
        stb.append("      PRICE + " + map.get("PRICE") + ", ");
        stb.append("      TAX + " + map.get("TAX") + ", ");
        stb.append("      SUMMING_UP_MONEY + " + map.get("SUMMING_UP_MONEY") + ") ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + map.get("YEAR") + "' ");
        stb.append("     AND APPLICANTNO = '" + map.get("APPLICANTNO") + "' ");
        stb.append("     AND PLAN_YEAR = '" + map.get("PLAN_YEAR") + "' ");
        stb.append("     AND PLAN_MONTH = '" + map.get("PLAN_MONTH") + "' ");
        stb.append("     AND SLIP_NO = '" + map.get("SLIP_NO") + "' ");
        stb.append("     AND SEQ = '" + map.get("SEQ") + "' ");

        return stb.toString();
    }

    private String getDelSql(final Map map) {

        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM SALES_PLAN_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + map.get("MOTO_YEAR") + "' ");
        stb.append("     AND APPLICANTNO = '" + map.get("APPLICANTNO") + "' ");
        stb.append("     AND PLAN_YEAR = '" + map.get("MOTO_PLAN_YEAR") + "' ");
        stb.append("     AND PLAN_MONTH = '" + map.get("MOTO_PLAN_MONTH") + "' ");
        stb.append("     AND SLIP_NO = '" + map.get("SLIP_NO") + "' ");
        stb.append("     AND SEQ = '" + map.get("SEQ") + "' ");

        return stb.toString();
    }

    /**
     * ICASSの予定月＜計上日データの計画変更
     */
    private void updateSales2() throws SQLException {

        final String exeMonth = "SELECT MAX(SALES_YEAR_MONTH) AS YEAR_MONTH FROM SALES_TIGHTENS_HIST_DAT WHERE TEMP_TIGHTENS_FLAG = '2'";

        ResultSet rs = null;
        String planYearMonth = "";
        try {
            _db2.query(exeMonth);
            rs = _db2.getResultSet();
            while (rs.next()) {
                planYearMonth = rs.getString("YEAR_MONTH").substring(0, 4) + "-" + rs.getString("YEAR_MONTH").substring(4) + "-01";
            }
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(rs);
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ICASS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     L1.APPLICANTNO, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE, ");
        stb.append("     CASE WHEN MONTH(DATE(T1.URIAGE_NENTSUKI)) < 4 ");
        stb.append("          THEN YEAR(DATE(T1.URIAGE_NENTSUKI)) - 1 ");
        stb.append("          ELSE YEAR(DATE(T1.URIAGE_NENTSUKI)) ");
        stb.append("     END AS YEAR, ");
        stb.append("     YEAR(DATE(T1.URIAGE_NENTSUKI)) AS PLAN_YEAR, ");
        stb.append("     MONTH(DATE(T1.URIAGE_NENTSUKI)) AS PLAN_MONTH, ");
        stb.append("     YEAR(DATE(T1.KEIJO_NENTSUKI)) AS CHANGE_PLAN_YEAR, ");
        stb.append("     MONTH(DATE(T1.KEIJO_NENTSUKI)) AS CHANGE_PLAN_MONTH, ");
        stb.append("     SUM(INT(T1.JUTO_KINGAKU)) AS JUTO, ");
        stb.append("     SUM(INT(T1.URIAGE_KINGAKU_ZEINUKI)) AS ZEINUKI, ");
        stb.append("     SUM(INT(T1.URIAGE_KINGAKU_ZEIKIN)) AS ZEIKIN ");
        stb.append(" FROM ");
        stb.append("     SEITO_URIAGE T1 ");
        stb.append("     LEFT JOIN APPLICANT_BASE_MST L1 ON T1.SHIGANSHA_RENBAN = L1.REMARK ");
        stb.append(" WHERE ");
        stb.append("     DATE(T1.URIAGE_NENTSUKI) >= '2008-04-01' ");
        stb.append("     AND T1.KEIJO_NENTSUKI IS NOT NULL ");
        stb.append("     AND T1.KEIJO_NENTSUKI <> '' ");
        stb.append("     AND DATE(T1.KEIJO_NENTSUKI) <= '" + planYearMonth + "' ");
        stb.append("     AND DATE(T1.URIAGE_NENTSUKI) < DATE(KEIJO_NENTSUKI) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     L1.APPLICANTNO, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE, ");
        stb.append("     YEAR(DATE(T1.URIAGE_NENTSUKI)), ");
        stb.append("     MONTH(DATE(T1.URIAGE_NENTSUKI)), ");
        stb.append("     YEAR(DATE(T1.KEIJO_NENTSUKI)), ");
        stb.append("     MONTH(DATE(T1.KEIJO_NENTSUKI)) ");
        stb.append(" HAVING ");
        stb.append("     SUM(INT(T1.JUTO_KINGAKU)) <> 0 ");
        stb.append(" ORDER BY ");
        stb.append("     INT(T1.SHIGANSHA_RENBAN) ");
        stb.append(" ), HAS_CLAIM AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE, ");
        stb.append("     SUM(CASE WHEN L1.APPLICANTNO IS NOT NULL ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS HASCLAIM ");
        stb.append(" FROM ");
        stb.append("     ICASS_T T1 ");
        stb.append("     LEFT JOIN SALES_PLAN_DAT L1 ON T1.YEAR = INT(L1.YEAR) ");
        stb.append("          AND T1.APPLICANTNO = L1.APPLICANTNO ");
        stb.append("          AND T1.GAKUHI_HIMOKU_CODE = substr(L1.COMMODITY_CD, 1, 2) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE ");
        stb.append(" HAVING ");
        stb.append("     SUM(CASE WHEN L1.APPLICANTNO IS NOT NULL ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) > 0 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     ABS(T1.JUTO) AS ABS_JUTO, ");
        stb.append("     CASE WHEN T1.JUTO < 0 ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '2' ");
        stb.append("     END AS MINUS_JUTO, ");
        stb.append("     CASE WHEN L1.APPLICANTNO IS NOT NULL ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '2' ");
        stb.append("     END AS MOTO_FLG, ");
        stb.append("     L1.YEAR AS MOTO_YEAR, ");
        stb.append("     L1.APPLICANTNO AS MOTO_APPLICANTNO, ");
        stb.append("     L1.PLAN_YEAR AS MOTO_PLAN_YEAR, ");
        stb.append("     L1.PLAN_MONTH AS MOTO_PLAN_MONTH, ");
        stb.append("     L1.SLIP_NO AS MOTO_SLIP_NO, ");
        stb.append("     L1.SEQ AS MOTO_SEQ, ");
        stb.append("     L1.COMMODITY_CD AS MOTO_COMMODITY_CD, ");
        stb.append("     L1.SUMMING_UP_MONEY AS MOTO_SUMMING_UP_MONEY, ");
        stb.append("     CASE WHEN L2.APPLICANTNO IS NOT NULL ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '2' ");
        stb.append("     END AS SAKI_FLG, ");
        stb.append("     L2.YEAR AS SAKI_YEAR, ");
        stb.append("     L2.APPLICANTNO AS SAKI_APPLICANTNO, ");
        stb.append("     L2.PLAN_YEAR AS SAKI_PLAN_YEAR, ");
        stb.append("     L2.PLAN_MONTH AS SAKI_PLAN_MONTH, ");
        stb.append("     L2.SLIP_NO AS SAKI_SLIP_NO, ");
        stb.append("     L2.SEQ AS SAKI_SEQ, ");
        stb.append("     L2.COMMODITY_CD AS SAKI_COMMODITY_CD, ");
        stb.append("     L2.SUMMING_UP_MONEY AS SAKI_SUMMING_UP_MONEY ");
        stb.append(" FROM ");
        stb.append("     ICASS_T T1 ");
        stb.append("     LEFT JOIN SALES_PLAN_DAT L1 ON T1.YEAR = INT(L1.YEAR) ");
        stb.append("          AND T1.APPLICANTNO = L1.APPLICANTNO ");
        stb.append("          AND T1.PLAN_YEAR = INT(L1.PLAN_YEAR) ");
        stb.append("          AND T1.PLAN_MONTH = INT(L1.PLAN_MONTH) ");
        stb.append("          AND T1.GAKUHI_HIMOKU_CODE = substr(L1.COMMODITY_CD, 1, 2) ");
        stb.append("     LEFT JOIN SALES_PLAN_DAT L2 ON T1.YEAR = INT(L2.YEAR) ");
        stb.append("          AND T1.APPLICANTNO = L2.APPLICANTNO ");
        stb.append("          AND T1.CHANGE_PLAN_YEAR = INT(L2.PLAN_YEAR) ");
        stb.append("          AND T1.CHANGE_PLAN_MONTH = INT(L2.PLAN_MONTH) ");
        stb.append("          AND T1.GAKUHI_HIMOKU_CODE = substr(L2.COMMODITY_CD, 1, 2), ");
        stb.append("     HAS_CLAIM T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.APPLICANTNO = T2.APPLICANTNO ");
        stb.append("     AND T1.APPLICANTNO = T2.APPLICANTNO ");
        stb.append("     AND T1.GAKUHI_HIMOKU_CODE = T2.GAKUHI_HIMOKU_CODE ");

        log.debug(stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final boolean hasSaki = map.get("SAKI_FLG").equals("1") ? true : false;
            final boolean hasMoto = map.get("MOTO_FLG").equals("1") ? true : false;
            final boolean minus = map.get("MINUS_JUTO").equals("1") ? true : false;
            if (hasSaki) {
                sakiUpd(map, minus);
            } else if (!minus) {
                sakiIns(map, minus);
            }
            if (hasMoto) {
                motoUpd(map, minus);
            }
            if (minus) {
                
            }
        }

    }

    private void sakiUpd(final Map map, final boolean minus) throws SQLException {
        final String keisanKigou = minus ? "-" : "+";
        final StringBuffer stb = new StringBuffer();
        stb.append(" UPDATE ");
        stb.append("     SALES_PLAN_DAT ");
        stb.append(" SET ");
        stb.append("     TOTAL_CLAIM_MONEY = TOTAL_CLAIM_MONEY " + keisanKigou + " " + map.get("ABS_JUTO") + ", ");
        stb.append("     SUMMING_UP_MONEY = SUMMING_UP_MONEY " + keisanKigou + " " + map.get("ABS_JUTO") + ", ");
        stb.append("     PRICE = PRICE " + keisanKigou + " " + map.get("ZEINUKI") + ", ");
        stb.append("     TAX = TAX " + keisanKigou + " " + map.get("ZEIKIN") + " ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + map.get("SAKI_YEAR") + "' ");
        stb.append("     AND APPLICANTNO = '" + map.get("SAKI_APPLICANTNO") + "' ");
        stb.append("     AND PLAN_YEAR = '" + map.get("SAKI_PLAN_YEAR") + "' ");
        stb.append("     AND PLAN_MONTH = '" + map.get("SAKI_PLAN_MONTH") + "' ");
        stb.append("     AND SLIP_NO = '" + map.get("SAKI_SLIP_NO") + "' ");
        stb.append("     AND SEQ = '" + map.get("SAKI_SEQ") + "' ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
            _db2.commit();
        } catch (SQLException e) {
            log.error("SQLException = " + stb, e);
        }
    }

    private void sakiIns(final Map map, final boolean minus) throws SQLException {
        final int intChangeYear = ((Number) map.get("CHANGE_PLAN_YEAR")).intValue();
        final int intChangeMonth = ((Number) map.get("CHANGE_PLAN_MONTH")).intValue();
        final int intYear = ((Number) map.get("YEAR")).intValue();
        final String month = _monthFormat.format(intChangeMonth);
        final int setJuto = ((Number) map.get("ABS_JUTO")).intValue();
        final int setPrice = ((Number) map.get("ZEINUKI")).intValue();
        final int setTax = ((Number) map.get("ZEIKIN")).intValue();
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO SALES_PLAN_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(String.valueOf(intYear)) + ", ");
        stb.append(" " + getInsertVal((String) map.get("APPLICANTNO")) + ", ");
        stb.append(" " + getInsertVal(String.valueOf(intChangeYear)) + ", ");
        stb.append(" " + getInsertVal(month) + ", ");
        stb.append(" " + getInsertVal((String) map.get("MOTO_SLIP_NO")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("MOTO_SEQ")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("MOTO_COMMODITY_CD")) + ", ");
        stb.append(" " + getInsertVal(new Integer(setJuto)) + ", ");
        stb.append(" " + getInsertVal(new Integer(setPrice)) + ", ");
        stb.append(" " + getInsertVal(new Integer(setTax)) + ", ");
        stb.append(" null, ");
        stb.append(" null, ");
        stb.append(" " + getInsertVal(new Integer(setJuto)) + ", ");
        stb.append(" " + getInsertVal(map.get("YEAR") + "-" + month + "-01") + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
            _db2.commit();
        } catch (SQLException e) {
            log.error("SQLException = " + stb, e);
        }
    }

    private void motoUpd(final Map map, final boolean minus) throws SQLException {
        final String keisanKigou = minus ? "+" : "-";
        final StringBuffer stb = new StringBuffer();
        stb.append(" UPDATE ");
        stb.append("     SALES_PLAN_DAT ");
        stb.append(" SET ");
        stb.append("     TOTAL_CLAIM_MONEY = TOTAL_CLAIM_MONEY " + keisanKigou + " " + map.get("ABS_JUTO") + ", ");
        stb.append("     SUMMING_UP_MONEY = SUMMING_UP_MONEY " + keisanKigou + " " + map.get("ABS_JUTO") + ", ");
        stb.append("     PRICE = PRICE " + keisanKigou + " " + map.get("ZEINUKI") + ", ");
        stb.append("     TAX = TAX " + keisanKigou + " " + map.get("ZEIKIN") + " ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + map.get("MOTO_YEAR") + "' ");
        stb.append("     AND APPLICANTNO = '" + map.get("MOTO_APPLICANTNO") + "' ");
        stb.append("     AND PLAN_YEAR = '" + map.get("MOTO_PLAN_YEAR") + "' ");
        stb.append("     AND PLAN_MONTH = '" + map.get("MOTO_PLAN_MONTH") + "' ");
        stb.append("     AND SLIP_NO = '" + map.get("MOTO_SLIP_NO") + "' ");
        stb.append("     AND SEQ = '" + map.get("MOTO_SEQ") + "' ");

        try {
            _db2.stmt.executeUpdate(stb.toString());
            _db2.commit();
        } catch (SQLException e) {
            log.error("SQLException = " + stb, e);
        }
    }
}
// eof

