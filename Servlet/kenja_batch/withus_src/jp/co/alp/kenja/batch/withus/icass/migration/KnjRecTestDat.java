// kanji=漢字
/*
 * $Id: KnjRecTestDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.icass.migration.table_icass.SeitoRishuKadaiJisseki;

/**
 * REC_TEST_DATを作る。
 * @author takaesu
 * @version $Id: KnjRecTestDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecTestDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecTestDat.class);

    public static final String ICASS_TABLE = "SEITO_RISHU_KADAI_JISSEKI";

    public KnjRecTestDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "テスト実績"; }

    void migrate() throws SQLException {
        int totalCount = 0;
        final String insertSql = "INSERT INTO rec_test_dat VALUES(?,?,?,?,?,?,?,?,?,current timestamp)";

        final String sql;
        sql = " SELECT "
            + "     T1.* "
            + " FROM "
            + "     SEITO_RISHU_KADAI_JISSEKI T1 LEFT JOIN SEITO L1 ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN "
            + " WHERE "
            + "     rishu_kadai_shubetsu_code='2' AND"
            + "     L1.SEITO_NO IS NOT NULL AND"
            + "     L1.SEITO_NO != ''"; // 1=レポート, 2=テスト
        log.debug("sql=" + sql);

        _db2.query(sql);
        ResultSet rs = _db2.getResultSet();
        while (rs.next()) {
            final SeitoRishuKadaiJisseki srkj = new SeitoRishuKadaiJisseki(_param, rs);

            final Object[] array = srkj.toRecTestDat();
            final int insertCount = _runner.update(_db2.conn, insertSql, array);
            if (1 != insertCount) {
                throw new IllegalStateException("INSERT件数が1件以外!:" + insertCount);
            }
            totalCount += insertCount;
        }
        _db2.commit();
        DbUtils.closeQuietly(rs);
    }
}
// eof
