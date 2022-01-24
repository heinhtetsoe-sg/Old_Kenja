// kanji=漢字
/*
 * $Id: DaoGroupClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 14:34:09 - JST
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
import jp.co.alp.kenja.common.domain.GroupClass;

/*
 * describe table V_ELECTCLASS_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * GROUPCD                        SYSIBM    VARCHAR                   4     0 いいえ
 * GROUPNAME                      SYSIBM    VARCHAR                   9     0 はい
 * GROUPABBV                      SYSIBM    VARCHAR                   6     0 はい
 * REMARK                         SYSIBM    VARCHAR                  90     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     6 レコードが選択されました。
 */

/**
 * 選択科目を読み込む。
 * @author tamura
 * @version $Id: DaoGroupClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoGroupClass extends AbstractDaoLoader<GroupClass> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_ELECTCLASS_MST";

    /** log */
    private static final Log log = LogFactory.getLog(DaoGroupClass.class);
    private static final AbstractDaoLoader<GroupClass> INSTANCE = new DaoGroupClass();

    /*
     * コンストラクタ。
     */
    private DaoGroupClass() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<GroupClass> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return GroupClass.create(
                _cm.getCategory(),
                MapUtils.getString(map, "code"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "abbr"),
                MapUtils.getString(map, "remark")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    GROUPCD as code,"
                + "    coalesce(GROUPNAME, GROUPABBV) as name,"
                + "    GROUPABBV as abbr,"
                + "    coalesce(REMARK, '') as remark"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  order by"
                + "    GROUPCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoGroupClass

// eof
