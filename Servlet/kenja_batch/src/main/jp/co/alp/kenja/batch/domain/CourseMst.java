// kanji=漢字
/*
 * $Id: CourseMst.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/15 15:43:50 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.domain;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Period;
import jp.co.alp.kenja.common.lang.enums.MyValuedEnum;

/**
 * 課程マスタ。
 * @author takaesu
 * @version $Id: CourseMst.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class CourseMst extends MyValuedEnum<String, String> {
    /*pkg*/static final Log log = LogFactory.getLog(CourseMst.class);
    private static final Class<CourseMst> MYCLASS = CourseMst.class;

    private final String _shortName;
    private final Period _sPeriod;
    private final Period _ePeriod;

    private CourseMst(
            final Category category,
            final String code,
            final String name,
            final String shortName,
            final Period s,
            final Period e
    ) {
        super(category, code, name);
        _shortName = shortName;
        _sPeriod = s;
        _ePeriod = e;
    }

    /**
     * 課程コードを得る。
     * @return 課程コード
     */
    public String getCode() {
        checkAlive();
        final String key = getKey();
        if (key instanceof String) {
            return (String) key;
        }

        log.error("key'" + key + "'がStringではない");
        throw new IllegalStateException("key'" + key + "'がStringではない");
    }

    /**
     * 課程名称を得る。
     * @return 課程名称
     */
    public String getName() {
        checkAlive();
        return getValue();
    }

    /**
     * 課程略称を得る。
     * @return 課程略称
     */
    public String getShortName() {
        checkAlive();
        return _shortName;
    }

    /**
     * 開始校時を得る。
     * @return 開始校時
     */
    public Period getSPeriod() {
        checkAlive();
        return _sPeriod;
    }

    /**
     * 終了校時を得る。
     * @return 終了校時
     */
    public Period getEPeriod() {
        checkAlive();
        return _ePeriod;
    }

    /**
     * コアタイム内か判定する。
     * @param period 校時
     * @return コアタイム内なら true
     */
    public boolean isActive(final Period period) {
        return getSPeriod().compareTo(period) <= 0 && 0 <= getEPeriod().compareTo(period);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return getCode() + ":" + getName() + "[開始校時=" + getSPeriod() + ", 終了校時=" + getEPeriod() + "]";
    }

    /**
     * なければ、課程のインスタンスを作成する。
     * すでに同じ課程コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 課程コード
     * @param name 課程名称
     * @param shortName 課程略称
     * @param s 開始校時
     * @param e 終了校時
     * @return 課程のインスタンス
     */
    public static CourseMst create(
            final Category category,
            final String code,
            final String name,
            final String shortName,
            final Period s,
            final Period e
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == name)      { throw new IllegalArgumentException("引数が不正(name)"); }

        return new CourseMst(category, code, name, shortName, s, e);
    }

    /**
     * 課程コード(int)から、課程を得る。
     * @param category カテゴリー
     * @param code 課程コード
     * @return 課程
     */
    public static CourseMst getInstance(
            final Category category,
            final String code
    ) {
        return getEnum(category, MYCLASS, code);
    }

    /**
     * 課程の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Kintai&gt;</code>
     */
    public static List<CourseMst> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 課程の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

} // CourseMst

// eof
