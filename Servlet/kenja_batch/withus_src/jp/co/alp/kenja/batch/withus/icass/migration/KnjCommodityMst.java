// kanji=漢字
/*
 * $Id: KnjCommodityMst.java 56574 2017-10-22 11:21:06Z maeshiro $
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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * COMMODITY_MSTを作る。
 * @author takaesu
 * @version $Id: KnjCommodityMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjCommodityMst extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjCommodityMst.class);

    public static final String ICASS_TABLE = "GAKUHI_MASTER";
    public static final String ICASS_TABLE2 = "GAKUHI_HIMOKU_MASTER";

    public KnjCommodityMst() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "商品マスタ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "と" + ICASS_TABLE2 + "データ件数=" + list.size());

        try {
            saveKnj(list);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("更新処理中にエラー! rollback した。");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final CommodityMst commodityMst = new CommodityMst(map);
            rtn.add(commodityMst);
        }
        final CommodityMst commodityMst = new CommodityMst();
        rtn.add(commodityMst);
        return rtn;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GAKUHI_HIMOKU_CODE, ");
        stb.append("     T1.KINGAKU, ");
        stb.append("     T1.COMMODITY_CD, ");
        stb.append("     L1.GAKUHI_HIMOKU_NAME, ");
        stb.append("     L1.GAKUHI_HIMOKU_R_NAME ");
        stb.append(" FROM ");
        stb.append("     GAKUHI_MASTER_TMP T1 ");
        stb.append("     LEFT JOIN GAKUHI_HIMOKU_MASTER L1 ON T1.GAKUHI_HIMOKU_CODE = L1.GAKUHI_HIMOKU_CODE ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class CommodityMst {
        final String _commodityCd;
        final String _sYearMonth;
        final String _eYearMonth;
        final String _commodityName;
        final String _commodityAbbv;
        final String _itemCd;
        final String _calculationSubCd;
        final String _assistanceSubCd;
        final Integer _includingTaxPrice;
        final Integer _price;
        final Integer _tax;
        final String _taxFlg;
        final String _taxPercent;
        final String _salesMonth;
        final String _dividingMultiplicationDiv;
        final String _fractionDiv;
        final String _tuitionDiv;
        final String _salesDiv;

        public CommodityMst(final Map map) {
            _commodityCd = (String) map.get("COMMODITY_CD");
            _sYearMonth = "";    // TODO:未定
            _eYearMonth = "";    // TODO:未定
            _commodityName = (String) map.get("GAKUHI_HIMOKU_NAME");
            _commodityAbbv = (String) map.get("GAKUHI_HIMOKU_R_NAME");
            _itemCd = "";    // TODO:未定
            _calculationSubCd = "";    // TODO:未定
            _assistanceSubCd = "";    // TODO:未定
            final int kingaku = ((Number) map.get("KINGAKU")).intValue();
            _includingTaxPrice = new Integer(kingaku); // 本体価格は単価に同じ
            _price = new Integer(kingaku);
            _tax = null;    // TODO:手入力
            _taxFlg = "";    // TODO:手入力
            _taxPercent = "";    // TODO:手入力
            _salesMonth = "";    // TODO:未定
            final String himokuCd = (String) map.get("GAKUHI_HIMOKU_CODE");
            _dividingMultiplicationDiv = himokuCd.equals("02") || himokuCd.equals("11") || himokuCd.equals("12") ? "1" : "2";
            _fractionDiv = himokuCd.equals("02") || himokuCd.equals("11") || himokuCd.equals("12") ? "2" : "1";
            _tuitionDiv = "1";    // TODO:固定
            _salesDiv = himokuCd.equals("01") ? "1" : "";
        }

        public CommodityMst() {
            _commodityCd = "90001";
            _sYearMonth = "";    // TODO:未定
            _eYearMonth = "";    // TODO:未定
            _commodityName = "授業料免除";
            _commodityAbbv = "免除";
            _itemCd = "";    // TODO:未定
            _calculationSubCd = "";    // TODO:未定
            _assistanceSubCd = "";    // TODO:未定
            _includingTaxPrice = new Integer(0); // 本体価格は単価に同じ
            _price = new Integer(0);
            _tax = null;    // TODO:手入力
            _taxFlg = "";    // TODO:手入力
            _taxPercent = "";    // TODO:手入力
            _salesMonth = "";    // TODO:未定
            _dividingMultiplicationDiv = "1";
            _fractionDiv = "2";
            _tuitionDiv = "1"; // TODO:固定
            _salesDiv = "";    // TODO:手入力
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table COMMODITY_MST

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 いいえ
        S_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 はい
        E_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 はい
        COMMODITY_NAME                 SYSIBM    VARCHAR                 150     0 いいえ
        COMMODITY_ABBV                 SYSIBM    VARCHAR                  60     0 いいえ
        ITEM_CD                        SYSIBM    VARCHAR                  10     0 はい
        CALCULATION_SUB_CD             SYSIBM    VARCHAR                  10     0 はい
        ASSISTANCE_SUB_CD              SYSIBM    VARCHAR                  10     0 はい
        INCLUDING_TAX_PRICE            SYSIBM    INTEGER                   4     0 はい
        PRICE                          SYSIBM    INTEGER                   4     0 はい
        TAX                            SYSIBM    INTEGER                   4     0 はい
        TAX_FLG                        SYSIBM    VARCHAR                   1     0 はい
        TAX_PERCENT                    SYSIBM    DECIMAL                   4     1 はい
        SALES_MONTH                    SYSIBM    VARCHAR                   2     0 はい
        DIVIDING_MULTIPLICATION_DIV    SYSIBM    VARCHAR                   1     0 はい
        FRACTION_DIV                   SYSIBM    VARCHAR                   1     0 はい
        TUITION_DIV                    SYSIBM    VARCHAR                   1     0 はい
        SALES_DIV                      SYSIBM    VARCHAR                   1     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          20 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final CommodityMst commodityMst = (CommodityMst) it.next();
            final String insSql = getInsertSql(commodityMst);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final CommodityMst commodityMst) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO COMMODITY_MST ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(commodityMst._commodityCd) + ", ");
        stb.append(" " + getInsertVal(commodityMst._sYearMonth) + ", ");
        stb.append(" " + getInsertVal(commodityMst._eYearMonth) + ", ");
        stb.append(" " + getInsertVal(commodityMst._commodityName) + ", ");
        stb.append(" " + getInsertVal(commodityMst._commodityAbbv) + ", ");
        stb.append(" " + getInsertVal(commodityMst._itemCd) + ", ");
        stb.append(" " + getInsertVal(commodityMst._calculationSubCd) + ", ");
        stb.append(" " + getInsertVal(commodityMst._assistanceSubCd) + ", ");
        stb.append(" " + getInsertVal(commodityMst._includingTaxPrice) + ", ");
        stb.append(" " + getInsertVal(commodityMst._price) + ", ");
        stb.append(" " + getInsertVal(commodityMst._tax) + ", ");
        stb.append(" " + getInsertVal(commodityMst._taxFlg) + ", ");
        stb.append(" " + getInsertVal(commodityMst._taxPercent) + ", ");
        stb.append(" " + getInsertVal(commodityMst._salesMonth) + ", ");
        stb.append(" " + getInsertVal(commodityMst._dividingMultiplicationDiv) + ", ");
        stb.append(" " + getInsertVal(commodityMst._fractionDiv) + ", ");
        stb.append(" " + getInsertVal(commodityMst._tuitionDiv) + ", ");
        stb.append(" " + getInsertVal(commodityMst._salesDiv) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

