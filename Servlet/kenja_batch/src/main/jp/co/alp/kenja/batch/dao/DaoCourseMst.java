// kanji=漢字
/*
 * $Id: DaoCourseMst.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/15 15:23:01 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Period;

import jp.co.alp.kenja.batch.domain.CourseMst;

/*
 * describe table COURSE_MST
 *                                タイプ・
 *
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * COURSECD                       SYSIBM    VARCHAR                   1     0 いいえ
 * COURSENAME                     SYSIBM    VARCHAR                  12     0 はい
 * COURSEABBV                     SYSIBM    VARCHAR                   6     0 はい
 * COURSEENG                      SYSIBM    VARCHAR                  10     0 はい
 * S_PERIODCD                     SYSIBM    VARCHAR                   1     0 はい
 * E_PERIODCD                     SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     8 レコードが選択されました。
 */

/**
 * 課程を課程マスタから読み込む。
 * @author takaesu
 * @version $Id: DaoCourseMst.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoCourseMst extends AbstractDaoLoader<CourseMst> {
    /** テーブル名 */
    public static final String TABLE_NAME = "COURSE_MST";

    /*pkg*/static final Log log = LogFactory.getLog(DaoCourseMst.class);
    private static final AbstractDaoLoader<CourseMst> INSTANCE = new DaoCourseMst();

    /*
     * コンストラクタ。
     */
    private DaoCourseMst() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<CourseMst> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    COURSECD as code,"
                + "    COURSENAME as name,"
                + "    COURSEABBV as abbv,"
                + "    S_PERIODCD as s_period,"
                + "    E_PERIODCD as e_period"
                + "  from " + TABLE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final CourseMst courseMst = CourseMst.create(
                _cm.getCategory(),
                MapUtils.getString(map, "code"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "shortName"),
                Period.getInstance(_cm.getCategory(), MapUtils.getString(map, "s_period")),
                Period.getInstance(_cm.getCategory(), MapUtils.getString(map, "e_period"))
        );

        return courseMst;
    }
} // DaoCourseMst

// eof
