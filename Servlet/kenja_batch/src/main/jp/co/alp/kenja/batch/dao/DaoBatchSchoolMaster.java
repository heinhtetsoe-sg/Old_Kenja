// kanji=漢字
/*
 * $Id: DaoBatchSchoolMaster.java 75778 2020-07-31 15:15:18Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.NameMaster;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaTableStatus;

/**
 * 学校マスタ(欠課フラグ)を読み込む。
 * @version $Id: DaoBatchSchoolMaster.java 75778 2020-07-31 15:15:18Z maeshiro $
 */
public final class DaoBatchSchoolMaster extends AbstractDaoLoader<BatchSchoolMaster> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_SCHOOL_MST";

    private static final Log log = LogFactory.getLog(DaoBatchSchoolMaster.class);
    private static AbstractDaoLoader<BatchSchoolMaster> INSTANCE;

    private final AccumulateOptions _options;

    private DaoBatchSchoolMaster(final AccumulateOptions options) {
        super(log);
        _options = options;
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public synchronized static AbstractDaoLoader<BatchSchoolMaster> getInstance(final AccumulateOptions options) {
        if (null == INSTANCE) {
            INSTANCE = new DaoBatchSchoolMaster(options);
        }
        return INSTANCE;
    }

    /**
     * @throws SQLException 例外
     */
    protected void preLoad() throws SQLException {
        DaoNameMaster.getInstance().load(_conn, _cm);
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final BatchSchoolMaster schoolMaster = new BatchSchoolMaster(
                MapUtils.getIntValue(map, "semOffdays", 0),
                MapUtils.getIntValue(map, "subOffdays", 0),
                MapUtils.getIntValue(map, "subAbsent", 0),
                MapUtils.getIntValue(map, "subMourning", 0),
                MapUtils.getIntValue(map, "subSuspend", 0),
                MapUtils.getIntValue(map, "subVirus", 0),
                MapUtils.getIntValue(map, "jugyoJisuFlg", 1),
                MapUtils.getIntValue(map, "rishuBunsi", 1),
                MapUtils.getIntValue(map, "rishuBunbo", 3),
                MapUtils.getIntValue(map, "shutokuBunsi", 1),
                MapUtils.getIntValue(map, "shutokuBunbo", 5),
                MapUtils.getIntValue(map, "rishuBunsiSpecial", 1),
                MapUtils.getIntValue(map, "rishuBunboSpecial", 3),
                MapUtils.getIntValue(map, "shutokuBunsiSpecial", 1),
                MapUtils.getIntValue(map, "shutokuBunboSpecial", 5),
                MapUtils.getInteger(map, "jituJifun"),
                MapUtils.getIntValue(map, "jituJifunSpecial", 1),
                MapUtils.getIntValue(map, "jituSyusu", 1),
                MapUtils.getIntValue(map, "jougentiSansyutuHou", 0),
                MapUtils.getIntValue(map, "syukessekiSansyutuHou", 0),
                NameMaster.getInstance()
        );

        BatchSchoolMaster.setBatchSchoolMaster(schoolMaster);

        return schoolMaster;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    SEM_OFFDAYS as semOffdays,"
                + "    SUB_OFFDAYS as subOffdays,"
                + "    SUB_ABSENT as subAbsent,"
                + "    SUB_MOURNING as subMourning,"
                + "    SUB_SUSPEND as subSuspend,"
                + "    SUB_VIRUS as subVirus,"
                + "    JUGYOU_JISU_FLG as jugyoJisuFlg,"
                + "    RISYU_BUNSI as rishuBunsi,"
                + "    RISYU_BUNBO as rishuBunbo,"
                + "    SYUTOKU_BUNSI as shutokuBunsi,"
                + "    SYUTOKU_BUNBO as shutokuBunbo,"
                + "    RISYU_BUNSI_SPECIAL as rishuBunsiSpecial,"
                + "    RISYU_BUNBO_SPECIAL as rishuBunboSpecial,"
                + "    SYUTOKU_BUNSI_SPECIAL as shutokuBunsiSpecial,"
                + "    SYUTOKU_BUNBO_SPECIAL as shutokuBunboSpecial,"
                + "    JITU_JIFUN as jituJifun,"
                + "    JITU_JIFUN_SPECIAL as jituJifunSpecial,"
                + "    JITU_SYUSU as jituSyusu,"
                + "    JOUGENTI_SANSYUTU_HOU as jougentiSansyutuHou,"
                + "    SYUKESSEKI_HANTEI_HOU as syukessekiSansyutuHou"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + (KenjaTableStatus.getInstance().isValidSchoolMasterSchoolKind() && null != _options.getSchoolKind()[0] ? " and SCHOOL_KIND = '" + _options.getSchoolKind()[0] + "' " : "")
            ;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoBatchSchoolMaster

// eof
