// kanji=漢字
/*
 * $Id: DaoExamItem.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/07/21 15:02:14 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.CountFlagUtils;
import jp.co.alp.kenja.common.domain.ExamItem;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/*
 * describe table TESTKIND_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * TESTKINDCD                     SYSIBM    VARCHAR                   2     0 いいえ
 * TESTKINDNAME                   SYSIBM    VARCHAR                  15     0 はい
 * TESTITEMADDCD                  SYSIBM    VARCHAR                   1     0 はい
 * REPORTOUTPUTCD                 SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     6 レコードが選択されました。
 */

/*
 * describe table TESTITEM_MST_COUNTFLG_NEW
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * TESTKINDCD                     SYSIBM    VARCHAR                   2     0 いいえ
 * TESTITEMCD                     SYSIBM    VARCHAR                   2     0 いいえ
 * TESTITEMNAME                   SYSIBM    VARCHAR                  30     0 はい
 * COUNTFLG                       SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     8 レコードが選択されました。
 */

/**
 * 考査種別マスタおよび考査項目マスタを読み込む。
 * @author tamura
 * @version $Id: DaoExamItem.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoExamItem extends AbstractDaoLoader<ExamItem> {
    /** テーブル名 */
    public static final String TABLE_NAME_KIND = "TESTKIND_MST";
    public static final String TABLE_NAME_COUNTFLG_NEW_SDIV = "TESTITEM_MST_COUNTFLG_NEW_SDIV";

    /*pkg*/static final Log log = LogFactory.getLog(DaoExamItem.class);
    private static final AbstractDaoLoader<ExamItem> INSTANCE = new DaoExamItem();

    private static boolean useCountflgNew_;
    private static boolean useCountflgNewSdiv_;

    /*
     * コンストラクタ。
     */
    private DaoExamItem() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @param prop プロパティ
     * @return インスタンス
     */
    public static AbstractDaoLoader<ExamItem> getInstance(final Properties prop) {
        final String property = prop.getProperty("testitem_mst_countflg_new", "false");
        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(property)) {
            useCountflgNewSdiv_ = true;
        } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(property)) {
            useCountflgNew_ = true;
        } else {
            useCountflgNew_ = BooleanUtils.toBoolean(property);
            log.debug("「集計フラグ用テスト項目マスタ」テーブルの新旧どちらを使うかフラグ=" + useCountflgNew_);
        }

        return INSTANCE;
    }

    /**
     * 「集計フラグ用テスト項目マスタ」テーブルの[新]を使うか判定する。
     * @return <code>true</code>なら[新]
     */
    public static boolean isUseCountflgNew() {
        return useCountflgNew_;
    }

    /**
     * 「集計フラグ用テスト項目マスタ」テーブル名を取得する。
     * @return テーブル名
     */
    public static String getItemTableName() {
        final String tableNameItem;
        if (useCountflgNewSdiv_) {
            tableNameItem = TABLE_NAME_COUNTFLG_NEW_SDIV;
        } else if (useCountflgNew_) {
            tableNameItem = "TESTITEM_MST_COUNTFLG_NEW";            
        } else {
            tableNameItem = "TESTITEM_MST_COUNTFLG";            
        }
        return tableNameItem;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final MyEnum.Category cat = _cm.getCategory();
        final int semesterCode = MapUtils.getIntValue(map, "semester");
        final String kindCode = MapUtils.getString(map, "kindCode");
        final String kindName = MapUtils.getString(map, "kindName", "(null)");
        final String itemCode = MapUtils.getString(map, "itemCode");
        final String itemName = MapUtils.getString(map, "itemName", "(null)");
        final boolean countFlag = CountFlagUtils.booleanValue(MapUtils.getString(map, "countFlag"));
        final ExamItem.Kind kind = ExamItem.Kind.create(cat, kindCode, kindName);
        final String scoreDiv = MapUtils.getString(map,  "scoreDiv");
        final ExamItem ei = ExamItem.create(cat, semesterCode, kind, itemCode, scoreDiv, itemName, countFlag);

        final String itemMsg = ", itemCode=" + itemCode + ", itemName=" + itemName + ", scoreDiv = " + scoreDiv + ", itemKey = " + ei.getKey();
        log.debug("ExamItem=" + ei + ", kind=" + kind + ", kindCode=" + kindCode + ", kindName=" + kindName + itemMsg);
        return ei;
    }

    /**
     * SQL文。
     * itemがゼロ件なkindは取得しない。
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final String tableNameItem = getItemTableName();

        final String sql;
        sql = "select distinct"
                + "    " + getSemesterColumn() + " as semester,"
                + "    kind.TESTKINDCD   as kindCode,"
                + "    kind.TESTKINDNAME as kindName,"
                + "    item.TESTITEMCD   as itemCode,"
                + "    item.TESTITEMNAME as itemName,"
                + "    item.COUNTFLG     as countFlag"
                + (TABLE_NAME_COUNTFLG_NEW_SDIV.equals(tableNameItem) ? ", item.SCORE_DIV as scoreDiv" : "")
                + "  from"
                + "    " + TABLE_NAME_KIND + " as kind,"
                + "    " + tableNameItem + " as item"
                + "  where item.YEAR = ?"
                + getCondition()
                + "    and kind.TESTKINDCD = item.TESTKINDCD"
                + "  order by"
                + "    kindCode,"
                + "    itemCode"
                + (TABLE_NAME_COUNTFLG_NEW_SDIV.equals(tableNameItem) ? ", scoreDiv" : "")
            ;
        log.debug("sql=" + sql);
        return sql;
    }

    private String getSemesterColumn() {
        return useCountflgNew_ || useCountflgNewSdiv_ ? "int(item.SEMESTER)" : _cm.getCurrentSemester().getCodeAsString();
    }

    private String getCondition() {
        final String condition;
        if (useCountflgNewSdiv_) {
            condition = "    and kind.TESTKINDCD <> '99' ";
        } else if (useCountflgNew_) {
            condition = "    and kind.TESTKINDCD <> '99' ";
        } else {
            condition = "";
        }
        return condition;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoExamItem

// eof
