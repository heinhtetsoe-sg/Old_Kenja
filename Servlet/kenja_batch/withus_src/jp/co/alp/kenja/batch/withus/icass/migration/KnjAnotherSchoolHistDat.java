// kanji=漢字
/*
 * $Id: KnjAnotherSchoolHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ANOTHER_SCHOOL_HIST_DAT を作る。
 * @author takaesu
 * @version $Id: KnjAnotherSchoolHistDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjAnotherSchoolHistDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjAnotherSchoolHistDat.class);

    public KnjAnotherSchoolHistDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "前籍校履歴データ"; }

    void migrate() throws SQLException {
        final String sql;
        sql = " SELECT "
            + " T1.*, "
            + "     CASE L1.DN_DIV "
            + "          WHEN '0' THEN '1' "
            + "          WHEN '1' THEN '2' "
            + "          WHEN '2' THEN '3' "
            + "     END AS DN_DIV, "
            + "     L1.SCHOOL_CD, "
            + "     L2.GAKKA_NAME "
            + " FROM "
            + "     SEITO_TAKO_ZAISEKI_RIREKI T1 "
            + "     LEFT JOIN KOKO_YOMIKAE L1 ON T1.KOKO_KANRI_NO = L1.KOKO_KANRI_NO "
            + "     LEFT JOIN KOKO_MASTER L2 ON T1.KOKO_KANRI_NO = L2.KOKO_KANRI_NO "
            ;
        log.debug("sql=" + sql);
        
        // SQL実行
        final List result;
        try {
            result = _runner.mapListQuery(sql);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "another_school_hist_dat", this);
    }

    /**[db2inst1@withus script]$ db2 describe table ANOTHER_SCHOOL_HIST_DAT
        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        APPLICANTNO                    SYSIBM    VARCHAR                   7     0 いいえ
        SEQ                            SYSIBM    SMALLINT                  2     0 いいえ
        STUDENT_DIV                    SYSIBM    VARCHAR                   1     0 はい
        FORMER_REG_SCHOOLCD            SYSIBM    VARCHAR                  11     0 はい
        MAJOR_NAME                     SYSIBM    VARCHAR                 120     0 はい
        REGD_S_DATE                    SYSIBM    DATE                      4     0 はい
        REGD_E_DATE                    SYSIBM    DATE                      4     0 はい
        PERIOD_MONTH_CNT               SYSIBM    VARCHAR                   2     0 はい
        ABSENCE_CNT                    SYSIBM    VARCHAR                   2     0 はい
        MONTH_CNT                      SYSIBM    VARCHAR                   2     0 はい
        ENT_FORM                       SYSIBM    VARCHAR                   1     0 はい
        REASON                         SYSIBM    VARCHAR                 150     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
     */
    public Object[] mapToArray(final Map map) {
// TAKAESU: SEITO_TAKO_KYUGAKU_RIREKIを使うなら以下のロジック生きる
//        final String kyugakuKikanF = (String) map.get("KYUGAKU_KIKAN_F");
//        final String kyugakuKikanT = (String) map.get("KYUGAKU_KIKAN_T");
//        final Integer absenceCnt = get月数(kyugakuKikanF, kyugakuKikanT);
        String rslt = null;
        if (map.get("ZAISEKI_KIKAN_F") != null && map.get("ZAISEKI_KIKAN_T") != null) {
            final String zaiseki_kikan_f = map.get("ZAISEKI_KIKAN_F").toString();
            final String zaiseki_kikan_t = map.get("ZAISEKI_KIKAN_T").toString();
            final int f_year  = Integer.parseInt(zaiseki_kikan_f.substring(0, 4));
            final int t_year  = Integer.parseInt(zaiseki_kikan_t.substring(0, 4));
            final int f_month = Integer.parseInt(zaiseki_kikan_f.substring(5, 7));
            final int t_month = Integer.parseInt(zaiseki_kikan_t.substring(5, 7));
            final int year  = t_year - f_year;
            final int month = t_month - f_month;
            rslt  = String.valueOf(year * 12 + month + 1); //←期間月数
            if (rslt.length() > 2) {
                rslt  = "99";
            }
        }
        final String shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
        final String applicantNo = _param.getApplicantNo(shiganshaRenban);
        final Object[] rtn = {
                applicantNo,
                map.get("ZAISEKI_RIREKI_RENBAN"),
                map.get("DN_DIV"),// 3.前籍校学生区分
                map.get("SCHOOL_CD"),
                map.get("GAKKA_NAME"),
                map.get("ZAISEKI_KIKAN_F"),
                map.get("ZAISEKI_KIKAN_T"),
                rslt,
                null,// 9.休学月数
                rslt,
                map.get("TAKO_NYUGAKU_KEITAI_CODE"),
                null,// 12. 事由
                Param.REGISTERCD,
        };
        return rtn;
    }
}
// eof

