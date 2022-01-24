// kanji=漢字
/*
 * $Id: DaoChairNoCurriculum.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 13:56:19 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query.nocurriculum;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.dao.query.DaoChair;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.SubClass;

/*
 * describe table CHAIR_DAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 いいえ
 * GROUPCD                        SYSIBM    VARCHAR                   4     0 はい
 * SUBCLASSCD                     SYSIBM    VARCHAR                   6     0 はい
 * CHAIRNAME                      SYSIBM    VARCHAR                  30     0 はい
 * TAKESEMES                      SYSIBM    VARCHAR                   1     0 はい
 * LESSONCNT                      SYSIBM    SMALLINT                  2     0 はい
 * FRAMECNT                       SYSIBM    SMALLINT                  2     0 はい
 * COUNTFLG                       SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     12 レコードが選択されました。
 */

/**
 * 講座を読み込む。
 * @author tamura
 * @version $Id: DaoChairNoCurriculum.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoChairNoCurriculum extends AbstractDaoLoader<Chair> {
    /** テーブル名 */
    public static final String TABLE_NAME = "CHAIR_DAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoChair.class);
    private static final AbstractDaoLoader<Chair> INSTANCE = new DaoChairNoCurriculum();

    /*
     * コンストラクタ。
     */
    private DaoChairNoCurriculum() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Chair> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final GroupClass group = GroupClass.getInstance(_cm.getCategory(), MapUtils.getString(map, "group"));
        if (null == group) {
            return "不明な選択科目コード(group)";
        }
        final SubClass subClass = SubClass.getInstance(_cm.getCategory(), null, null, null, MapUtils.getString(map, "subClass"));
        if (null == subClass) {
            return "不明な科目コード(subClass)";
        }
        final boolean countFlag = StringUtils.equals(MapUtils.getString(map, "countFlag"), "1");
        return Chair.create(
                _cm.getCategory(),
                MapUtils.getString(map, "code"),
                group,
                subClass,
                MapUtils.getString(map, "name"),
                MapUtils.getInteger(map, "lessonCount"),
                MapUtils.getInteger(map, "frameCount"),
                countFlag
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    CHAIRCD as code,"
                + "    GROUPCD as group,"
                + "    SUBCLASSCD as subClass,"
                + "    CHAIRNAME as name,"
                + "    LESSONCNT as lessonCount,"
                + "    FRAMECNT as frameCount,"
                + "    COUNTFLG as countFlag"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  and"
                + "    SEMESTER = ?"
                + "  order by"
                + "    CHAIRCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
            cm.getCurrentSemester().getCodeAsString(),
        };
    }
} // DaoChair

// eof
