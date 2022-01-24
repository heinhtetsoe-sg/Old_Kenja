// kanji=漢字
/*
 * $Id: DaoNameMaster.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2010/10/28 13:00:00 - JST
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.NameMaster;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;

/**
 * 名称マスタを読み込む。
 * @version $Id: DaoNameMaster.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public final class DaoNameMaster extends AbstractDaoLoader<NameMaster> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_NAME_MST";

    private static final Log log = LogFactory.getLog(DaoNameMaster.class);
    private static final AbstractDaoLoader<NameMaster> INSTANCE = new DaoNameMaster();

    private DaoNameMaster() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<NameMaster> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {

        final String namecd1 = MapUtils.getString(map, "NAMECD1");
        final String namecd2 = MapUtils.getString(map, "NAMECD2");

        final Map<String, String> stringMap = new HashMap<String, String>();
        for (final Map.Entry<String, Object> e : map.entrySet()) {
            stringMap.put(e.getKey(), null == e.getValue() ? null : e.getValue().toString());
        }
        NameMaster.getInstance().add(namecd1, namecd2, stringMap);

        return NameMaster.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    NAMECD1 as NAMECD1, "
                + "    NAMECD2 as NAMECD2, "
                + "    NAME1 as NAME1, "
                + "    NAME2 as NAME2, "
                + "    NAME3 as NAME3, "
                + "    ABBV1 as ABBV1, "
                + "    ABBV2 as ABBV2, "
                + "    ABBV3 as ABBV3, "
                + "    NAMESPARE1 as NAMESPARE1, "
                + "    NAMESPARE2 as NAMESPARE2, "
                + "    NAMESPARE3 as NAMESPARE3 "
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ? "
                + "    and ( NAMECD1 like 'Z%' "
                + "       or NAMECD1 = 'C040' "
                + "        ) "
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
} // DaoNameMasterZ

// eof
