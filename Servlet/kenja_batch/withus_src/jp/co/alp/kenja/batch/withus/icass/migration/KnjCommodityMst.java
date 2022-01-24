// kanji=����
/*
 * $Id: KnjCommodityMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/08/15 15:46:35 - JST
 * �쐬��: takaesu
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
 * COMMODITY_MST�����B
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
    String getTitle() { return "���i�}�X�^"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "��" + ICASS_TABLE2 + "�f�[�^����=" + list.size());

        try {
            saveKnj(list);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("�X�V�������ɃG���[! rollback �����B");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL���s
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: �f�[�^�ʂ������̂ň��Ă��܂��B�΍���l����!
        } catch (final SQLException e) {
            log.error("ICASS�f�[�^�捞�݂ŃG���[", e);
            throw e;
        }

        // ���ʂ̏���
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
            _sYearMonth = "";    // TODO:����
            _eYearMonth = "";    // TODO:����
            _commodityName = (String) map.get("GAKUHI_HIMOKU_NAME");
            _commodityAbbv = (String) map.get("GAKUHI_HIMOKU_R_NAME");
            _itemCd = "";    // TODO:����
            _calculationSubCd = "";    // TODO:����
            _assistanceSubCd = "";    // TODO:����
            final int kingaku = ((Number) map.get("KINGAKU")).intValue();
            _includingTaxPrice = new Integer(kingaku); // �{�̉��i�͒P���ɓ���
            _price = new Integer(kingaku);
            _tax = null;    // TODO:�����
            _taxFlg = "";    // TODO:�����
            _taxPercent = "";    // TODO:�����
            _salesMonth = "";    // TODO:����
            final String himokuCd = (String) map.get("GAKUHI_HIMOKU_CODE");
            _dividingMultiplicationDiv = himokuCd.equals("02") || himokuCd.equals("11") || himokuCd.equals("12") ? "1" : "2";
            _fractionDiv = himokuCd.equals("02") || himokuCd.equals("11") || himokuCd.equals("12") ? "2" : "1";
            _tuitionDiv = "1";    // TODO:�Œ�
            _salesDiv = himokuCd.equals("01") ? "1" : "";
        }

        public CommodityMst() {
            _commodityCd = "90001";
            _sYearMonth = "";    // TODO:����
            _eYearMonth = "";    // TODO:����
            _commodityName = "���Ɨ��Ə�";
            _commodityAbbv = "�Ə�";
            _itemCd = "";    // TODO:����
            _calculationSubCd = "";    // TODO:����
            _assistanceSubCd = "";    // TODO:����
            _includingTaxPrice = new Integer(0); // �{�̉��i�͒P���ɓ���
            _price = new Integer(0);
            _tax = null;    // TODO:�����
            _taxFlg = "";    // TODO:�����
            _taxPercent = "";    // TODO:�����
            _salesMonth = "";    // TODO:����
            _dividingMultiplicationDiv = "1";
            _fractionDiv = "2";
            _tuitionDiv = "1"; // TODO:�Œ�
            _salesDiv = "";    // TODO:�����
        }
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table COMMODITY_MST

        ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        COMMODITY_CD                   SYSIBM    VARCHAR                   5     0 ������
        S_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 �͂�
        E_YEAR_MONTH                   SYSIBM    VARCHAR                   6     0 �͂�
        COMMODITY_NAME                 SYSIBM    VARCHAR                 150     0 ������
        COMMODITY_ABBV                 SYSIBM    VARCHAR                  60     0 ������
        ITEM_CD                        SYSIBM    VARCHAR                  10     0 �͂�
        CALCULATION_SUB_CD             SYSIBM    VARCHAR                  10     0 �͂�
        ASSISTANCE_SUB_CD              SYSIBM    VARCHAR                  10     0 �͂�
        INCLUDING_TAX_PRICE            SYSIBM    INTEGER                   4     0 �͂�
        PRICE                          SYSIBM    INTEGER                   4     0 �͂�
        TAX                            SYSIBM    INTEGER                   4     0 �͂�
        TAX_FLG                        SYSIBM    VARCHAR                   1     0 �͂�
        TAX_PERCENT                    SYSIBM    DECIMAL                   4     1 �͂�
        SALES_MONTH                    SYSIBM    VARCHAR                   2     0 �͂�
        DIVIDING_MULTIPLICATION_DIV    SYSIBM    VARCHAR                   1     0 �͂�
        FRACTION_DIV                   SYSIBM    VARCHAR                   1     0 �͂�
        TUITION_DIV                    SYSIBM    VARCHAR                   1     0 �͂�
        SALES_DIV                      SYSIBM    VARCHAR                   1     0 �͂�
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
        UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
        
          20 ���R�[�h���I������܂����B
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
        log.warn("�}������=" + totalCount);
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

