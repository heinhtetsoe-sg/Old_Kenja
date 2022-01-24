// kanji=漢字
/*
 * $Id: KnjVirtualAccountDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * VIRTUAL_ACCOUNT_DATを作る。
 * @author takaesu
 * @version $Id: KnjVirtualAccountDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjVirtualAccountDat extends AbstractKnj implements IKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjVirtualAccountDat.class);
    private static final String KNJTABLE = "VIRTUAL_ACCOUNT_DAT";

    public KnjVirtualAccountDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "仮想口座管理データ"; }

    void migrate() throws SQLException {
        final StringBuffer sql = getSql();
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql.toString(), _handler);            
        } catch (SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        _runner.listToKnj(result, KNJTABLE, this);
    }

    private StringBuffer getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.KOZA_NO, ");
        stb.append("     SUBSTR(T1.TOROKU_DATE,1,10) AS VIRTUAL_SDATE, ");
        stb.append("     T1.SHIGANSHA_RENBAN, ");
        stb.append("     T2.VIRTUAL_BANK_CD ");
        stb.append(" FROM ");
        stb.append("     FURIKOMI_KOZA T1 ");
        stb.append("     LEFT JOIN ");
        stb.append("     VIRTUAL_BANK_MST T2 ");
        stb.append("     ON ");
        stb.append("     T1.BANK_CODE = T2.BANK_CD ");
        stb.append("     AND ");
        stb.append("     T1.BRANCH_CODE = T2.BRANCH_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.KOZA_NO IS NOT NULL ");
        stb.append("     AND T1.TOROKU_DATE IS NOT NULL ");
        stb.append("     AND T2.VIRTUAL_BANK_CD IS NOT NULL ");

        log.debug(stb);

        return stb;
    }

    /*
     * [db2inst1@withus script]$ db2 describe table virtual_account_dat

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        VIRTUAL_BANK_CD                SYSIBM    VARCHAR                   3     0 いいえ
        VIRTUAL_ACCOUNT_DIV            SYSIBM    VARCHAR                   1     0 いいえ
        VIRTUAL_ACCOUNT_NO             SYSIBM    VARCHAR                   7     0 いいえ
        VIRTUAL_SDATE                  SYSIBM    DATE                      4     0 いいえ
        VIRTUAL_EDATE                  SYSIBM    DATE                      4     0 はい
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい

        8 レコードが選択されました。
     */


    public Object[] mapToArray(Map map) {
        final String siganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String applicantNo = siganshaRenban == null ? null : _param.getApplicantNo(siganshaRenban);
        final Object[] rtn = {
                map.get("VIRTUAL_BANK_CD"),
                "1",
                map.get("KOZA_NO"),
                map.get("VIRTUAL_SDATE"),
                "9999-12-31",
                applicantNo,
                Param.REGISTERCD,
        };

        return rtn;
    }

    private boolean getGraduation(String applicantNo) {
        final StringBuffer gra = new StringBuffer();
        gra.append(" SELECT ");
        gra.append("     count(*) AS CNT ");
        gra.append(" FROM ");
        gra.append("     SCHREG_BASE_MST ");
        gra.append(" WHERE ");
        gra.append("     applicantno = '" + applicantNo + "' AND ");
        gra.append("     grd_div is null ");

        try {
            _db2.query(gra.toString());
            ResultSet rs = _db2.getResultSet();
            if (rs.next()) {
                return rs.getInt("CNT") > 0 ? true : false;
            } else {
                log.debug("ありえません。");
                return false;
            }
        } catch (SQLException e) {
            log.debug(gra.toString());
        }
        return false;
    }
}
// eof

