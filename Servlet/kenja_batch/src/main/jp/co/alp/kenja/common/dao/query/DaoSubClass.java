// kanji=漢字
/*
 * $Id: DaoSubClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 14:23:26 - JST
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
import jp.co.alp.kenja.common.domain.SubClass;

/*
 * describe table V_SUBCLASS_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * CLASSCD                        SYSIBM    VARCHAR                   2     0 いいえ
 * SCHOOL_KIND                    SYSIBM    VARCHAR                   2     0 いいえ
 * CURRICULUM_CD                  SYSIBM    VARCHAR                   2     0 いいえ
 * SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 いいえ
 * SUBCLASSNAME                   SYSIBM    VARCHAR                  60     0 はい
 * SUBCLASSABBV                   SYSIBM    VARCHAR                   9     0 はい
 * SUBCLASSNAME_ENG               SYSIBM    VARCHAR                  40     0 はい
 * SUBCLASSABBV_ENG               SYSIBM    VARCHAR                  20     0 はい
 * SHOWORDER                      SYSIBM    SMALLINT                  2     0 はい
 * ELECTDIV                       SYSIBM    VARCHAR                   1     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     9 レコードが選択されました。
 */

/**
 * 科目を読み込む。
 * @author tamura
 * @version $Id: DaoSubClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoSubClass extends AbstractDaoLoader<SubClass> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_SUBCLASS_MST";

    /** log */
    private static final Log log = LogFactory.getLog(DaoSubClass.class);
    private static final DaoSubClass INSTANCE = new DaoSubClass();

    /*
     * コンストラクタ。
     */
    private DaoSubClass() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<SubClass> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return SubClass.create(
                _cm.getCategory(),
                MapUtils.getString(map, "classCd"),
                MapUtils.getString(map, "schoolKind"),
                MapUtils.getString(map, "curriculumCd"),
                MapUtils.getString(map, "subClassCd"),
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "abbr")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        // order by に、SHOWORDER カラムを指定しない。2005-10-20 by tamura.
        return "select"
                + "    CLASSCD as classCd,"
                + "    SCHOOL_KIND as schoolKind,"
                + "    CURRICULUM_CD as curriculumCd,"
                + "    SUBCLASSCD as subClassCd,"
                + "    SUBCLASSNAME as name,"
                + "    coalesce(SUBCLASSABBV,'null-' || SUBCLASSCD) as abbr"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  order by"
                + "    SUBCLASSCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoSubClass

// eof
