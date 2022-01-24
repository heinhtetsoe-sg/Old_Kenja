// kanji=漢字
/*
 * $Id: KnjRecReportDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/07/15 14:15:57 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.icass.migration.table_icass.SeitoRishuKadaiJisseki;

/**
 * REC_REPORT_DATを作る。
 * @author takaesu
 * @version $Id: KnjRecReportDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecReportDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecReportDat.class);

    public static final String KNJ_TABLE = "REC_REPORT_DAT";
    public static final String ICASS_TABLE = "SEITO_RISHU_KADAI_JISSEKI";

    public KnjRecReportDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "レポート実績"; }

    public void migrate() throws SQLException {
        for (int year=2005; year<=2008; year++) {
            int recordsPer = 200;
            for (int shigansha_renban=0; shigansha_renban<15000; shigansha_renban+= recordsPer) {
                int begin = shigansha_renban + 1;
                int end = shigansha_renban + recordsPer;
                final List list = loadIcass(begin,end,year);
                log.debug(year+"年度 志願者連番 = "+begin+" ～ "+end);
                log.debug(ICASS_TABLE + "データ件数=" + list.size());

                saveKnj(list);
            }
        }
    }

    private List loadIcass(int shBegin,int shEnd,int year) throws SQLException {
        final List rtn = new ArrayList();

        // SQL文
        final String sql;
        sql = "SELECT T1.* FROM " + ICASS_TABLE + " T1"
            + " WHERE "//TODO: データ量が多いので溢れてしまう。対策を考えろ!通学生だけで良いはず
            + "  T1.rishu_kadai_shubetsu_code = '1'" // 1=レポート, 2=テスト
            + " and int(T1.shigansha_renban) between "+shBegin+" and "+shEnd+" and T1.nendo_code = '"+year+"'"
            + " and exists(SELECT 'x' FROM SEITO T2 WHERE VALUE(T2.SEITO_NO, '') <> '' AND T1.SHIGANSHA_RENBAN = T2.SHIGANSHA_RENBAN)"
            ;
        log.debug("sql=" + sql);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final SeitoRishuKadaiJisseki srkj = new SeitoRishuKadaiJisseki(_param, map);
            rtn.add(srkj);
        }
        return rtn;
    }

    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        final String sql = "INSERT INTO rec_report_dat VALUES(?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SeitoRishuKadaiJisseki srkj = (SeitoRishuKadaiJisseki) it.next();
            try {
                final int insertCount = _runner.update(_db2.conn, sql, srkj.toRecReportDat());
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT件数が1件以外!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                log.error("賢者へのINSERTでエラー", e);
                throw e;
            }
        }
        _db2.commit();
        log.warn("挿入件数=" + totalCount);
    }
} // KnjRecReportDat

// eof
