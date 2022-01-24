// kanji=漢字
/*
 * $Id: Period.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/06/03 16:19:24 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.enums.MyValuedEnum;

/**
 * 校時。
 * @author tamura
 * @version $Id: Period.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class Period extends MyValuedEnum<Integer, String> {
    /** log */
    private static final Log log = LogFactory.getLog(Period.class);
    private static final Class<Period> MYCLASS = Period.class;

    private static final int RADIX = 36;
    private static final int LIMIT_MIN = 0;
    private static final int LIMIT_MAX = 35;

    private final String _shortName;
    private final String _special; // 一日編集可能校時なら非null。
    private final Integer _minutes;

    /*
     * コンストラクタ。
     */
    private Period(
            final Category category,
            final Integer code,
            final String name,
            final String shortName,
            final String special,
            final Integer minutes
    ) {
        super(category, code, name);

        _shortName = shortName;
        _special = special;
        _minutes = minutes;
    }


    /**
     * 校時コードを得る。
     * @return 校時コード
     */
    public int getCode() {
        final Integer key = getKey();
        if (key instanceof Integer) {
            return ((Integer) key).intValue();
        }

        log.error("key'" + key + "'がIntegerではない");
        throw new IllegalStateException("key'" + key + "'がIntegerではない");
    }

    /**
     * 校時コードを得る。
     * @return 校時コード ("0","1","2"..."9","A","B"..."Z")
     */
    public String getCodeAsString() {
        final int rtn = getCode();
        return Integer.toString(rtn, RADIX).toUpperCase();
    }

    /**
     * 校時名称を得る。
     * @return 校時名称
     */
    public String getName() {
        checkAlive();
        return (String) getValue();
    }

    /**
     * 校時略称を得る。
     * @return 校時略称
     */
    public String getShortName() {
        checkAlive();
        return _shortName;
    }

    /**
     * 一日編集可能校時か否か判定する。
     * @return 一日編集可能校時なら<code>true</code>を返す
     */
    public boolean isSpecial() {
        return null != _special;
    }

    /**
     * 一日編集可能校時の値を得る。
     * @return 一日編集可能校時の値
     */
    public String getSpecial() {
        return _special;
    }

    /**
     * 校時の実施時間（分）を得る。
     * @return 校時の実施時間（分）
     */
    public Integer getPeriodMinutes() {
        return _minutes;
    }

    /**
     * 次の校時を得る。
     * @return 次の校時
     */
    public Period next() {
        checkAlive();
        final int next = getIndex() + 1;
        if (next >= size(getCategory())) {
            return null;
        }
        return getInstanceByIndex(getCategory(), next);
    }

    /**
     * 前の校時を得る。
     * @return 前の校時
     */
    public Period previous() {
        final int previous = getIndex() - 1;
        if (previous < 0) {
            return null;
        }
        return getInstanceByIndex(getCategory(), previous);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Period that) {
        return getKey().compareTo(that.getKey());
    }

    /**
     * なければ、校時のインスタンスを作成する。
     * すでに同じ校時コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 校時コード("0","1","2"..."9","A","B"..."Z")
     * @param name 校時名称
     * @param shortName 校時略称
     * @param special 一日編集可能校時
     * @return 校時のインスタンス
     */
    public static Period create(
            final Category category,
            final String code,
            final String name,
            final String shortName,
            final String special,
            final Integer minutes
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        final int codeint = Integer.parseInt(code, RADIX);
        if (codeint < LIMIT_MIN || LIMIT_MAX < codeint) { throw new IllegalArgumentException("引数が不正(code)"); }

        final Period found = getInstance(category, codeint);
        if (null != found) {
            return found;
        }

        if (null == name)      { throw new IllegalArgumentException("引数が不正(name)"); }
        if (null == shortName) { throw new IllegalArgumentException("引数が不正(shortName)"); }

        return new Period(category, new Integer(codeint), name, shortName, special, minutes);
    }

    /**
     * 校時コード(int)から、校時を得る。
     * @param category カテゴリー
     * @param code 校時コード
     * @return 校時
     */
    public static Period getInstance(
            final Category category,
            final int code
    ) {
        return (Period) getEnum(category, MYCLASS, new Integer(code));
    }

    /**
     * 校時コード(String)から、校時を得る。
     * @param category カテゴリー
     * @param code 校時コード
     * @return 校時
     */
    public static Period getInstance(
            final Category category,
            final String code
    ) {
        final int codeint = Integer.parseInt(code, RADIX);
        return (Period) getEnum(category, MYCLASS, new Integer(codeint));
    }

    /**
     * 表示順から、校時を得る。
     * @param category カテゴリー
     * @param index 表示順
     * @return 校時
     */
    public static Period getInstanceByIndex(
            final Category category,
            final int index
    ) {
        return (Period) getEnumList(category).get(index);
    }

    /**
     * 校時名称から、校時を得る。
     * @param category カテゴリー
     * @param name 校時名称
     * @return 校時
     */
    public static Period getEnum(
            final Category category,
            final String name
    ) {
        return (Period) getEnumByValue(category, MYCLASS, name);
    }

    /**
     * 校時の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Period&gt;</code>
     */
    public static List<Period> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 校時の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;校時コード, Period&gt;</code>
     */
    public static Map<Integer, Period> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 校時の数を得る。
     * @param category カテゴリー
     * @return 校時の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 校時の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

} // Period

// eof
