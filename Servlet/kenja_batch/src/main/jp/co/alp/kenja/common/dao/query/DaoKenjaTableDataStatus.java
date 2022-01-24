// kanji=漢字
/*
 * $Id: DaoKenjaTableDataStatus.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/06 19:57:51 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaTableStatus;

/**
 * 賢者のデータ情報を読み込む。
 * @author maesiro
 * @version $Id: DaoKenjaTableDataStatus.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoKenjaTableDataStatus extends AbstractDaoLoader<KenjaTableStatus> {
    /** テーブル名 */
    public static final String ATTEND_DI_CD_DAT = DaoKintai.ATTEND_DI_CD_DAT;

    /** log */
    private static final Log log = LogFactory.getLog(DaoKenjaTableDataStatus.class);
    private static final AbstractDaoLoader<KenjaTableStatus> INSTANCE = new DaoKenjaTableDataStatus();

    /*
     * コンストラクタ。
     */
    private DaoKenjaTableDataStatus() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<KenjaTableStatus> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final String tabname = MapUtils.getString(map, "tabname");
        final int count = MapUtils.getIntValue(map, "count", 0);
        if (ATTEND_DI_CD_DAT.equals(tabname)) {
            KenjaTableStatus.getInstance().setUseAttendDiCdDat(count > 0);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        // ユーザ定義関数 varchar(4) FISCALYEAR(date) を使ってます。
        return "select"
                + "    '" + ATTEND_DI_CD_DAT + "' as tabname,"
                + "    COUNT(*) as count"
                + "  from " + ATTEND_DI_CD_DAT
                + "  where"
                + "    YEAR = ? "
                ;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString()
        };
    }
} // DaoKenjaTableStatus

// eof
