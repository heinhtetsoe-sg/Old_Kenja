// kanji=漢字
/*
 * $Id: Kintai.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/02/11 11:24:32 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.enums.MyValuedEnum;

/**
 * 勤怠。
 * @author takaesu
 * @version $Id: Kintai.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class Kintai extends MyValuedEnum<Integer, String> {
    /** 「出席」のコードは固定 */
    private static final int SEATED_CODE = 0;
    /** 「一日勤怠」以外の校時に表示 */
    private static final int NOT_IN_SPECIAL = -1;
    /** log */
    private static final Log log = LogFactory.getLog(Kintai.class);
    private static final Class<Kintai> MYCLASS = Kintai.class;

    private final String _mark;
    private final Integer _altCode;
    private final Integer _betsuCode;

    /*
     * コンストラクタ。
     */
    private Kintai(
            final Category category,
            final Integer code,
            final String name,
            final String mark,
            final Integer altCode,
            final Integer betsuCode
    ) {
        super(category, code, name);
        _mark = mark;
        _altCode = altCode;
        _betsuCode = betsuCode;
    }

    /**
     * 勤怠コードを得る。
     * @return 勤怠コード
     */
    public int getCode() {
        return getKey().intValue();
    }

    /**
     * 代替(だいたい)コードを得る。
     * @return 代替コード
     */
    public Integer getAltCode() {
        checkAlive();
        if (hasBetsuCode()) {
            return _betsuCode;
        }
        if (hasAltCode()) {
            return _altCode;
        }
        return Integer.valueOf(getCode());
    }

    /**
     * 代替コードの勤怠インスタンスを得る。
     * @return 代替コードの勤怠
     */
    public Kintai getAltKintai() {
        if (hasBetsuCode()) {
            return getInstance(getCategory(), _betsuCode.intValue());
        }
        if (hasAltCode()) {
            return getInstance(getCategory(), _altCode.intValue());
        }
        return this;
    }
    
    /**
     * 勤怠名称を得る。
     * @return 勤怠名称
     */
    public String getName() {
        checkAlive();
        return getValue();
    }

    /**
     * 勤怠マークを得る。
     * @return 勤怠マーク
     */
    public String getMark() {
        checkAlive();
        return _mark;
    }

    /**
     * 「出席」か否か判定する。
     * @return 「出席」なら<code>true</code>を返す
     */
    public boolean isSeated() {
        checkAlive();
        return SEATED_CODE == getAltCode().intValue();
    }

    /**
     * 「特別校時は入力しない」か否か判定する。
     * @return 「特別校時は入力しない」なら<code>true</code>を返す
     */
    public boolean isNotInSpecial() {
        return null != _altCode && _altCode.intValue() == NOT_IN_SPECIAL;
    }

    /**
     * 入力可能か
     * @param isSpecial 特別校時か
     * @return 入力可能なら<code>true</code>を返す
     */
    public boolean isEnabled(final boolean isSpecial) {
        if (isSpecial) {
            return !isNotInSpecial();
        } else {
            return !hasAltCode();
        }
    }

    /**
     * 代替コードを持っているか判定する。
     * １日ＸＸか否かの判定に使える。
     * @return 代替コードを持っているなら<code>true</code>を返す
     */
    public boolean hasAltCode() {
        return null != _altCode && !isNotInSpecial() || hasBetsuCode();
    }
    
    public boolean hasBetsuCode() {
        return null != _betsuCode;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        if (!hasAltCode() || getKey().equals(_altCode)) {
            return getCode() + ":" + getName();
        } else {
            return getCode() + ":" + (null != _betsuCode ? _betsuCode : _altCode) + ":" + getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Kintai that) {
        checkAlive();
        return getKey().compareTo(that.getKey());
    }

    // =====================================================================

    /**
     * なければ、勤怠のインスタンスを作成する。
     * すでに同じ勤怠コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 勤怠コード
     * @param name 勤怠名称
     * @param mark 勤怠マーク
     * @param altCode 代替(だいたい)コード
     * @return 勤怠のインスタンス
     */
    public static Kintai create(
            final Category category,
            final String code,
            final String name,
            final String mark,
            final String altCode
    ) {
        return create(category, code, name, mark, altCode, null);
    }

    /**
     * なければ、勤怠のインスタンスを作成する。
     * すでに同じ勤怠コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 勤怠コード
     * @param name 勤怠名称
     * @param mark 勤怠マーク
     * @param altCode 代替(だいたい)コード
     * @param isInputtable 権限による入力可フラグ
     * @return 勤怠のインスタンス
     */
    public static Kintai create(
            final Category category,
            final String code,
            final String name,
            final String mark,
            final String altCode,
            final String betsuCode
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        final int codeint = Integer.parseInt(code);
        final Kintai found = getInstance(category, code);
        if (null != found) {
            return found;
        }

        if (null == name) { throw new IllegalArgumentException("引数が不正(name)"); }
        if (null == mark) { throw new IllegalArgumentException("引数が不正(mark)"); }

        final Integer alt;
        if (StringUtils.isEmpty(altCode)) {
            alt = null;
        } else {
            try {
                alt = new Integer(altCode);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("引数が不正(altCode)");
            }
        }

        Integer betsu = null;
        if (!StringUtils.isEmpty(betsuCode)) {
            try {
                betsu = new Integer(betsuCode);
                if (betsu.intValue() == codeint) {
                    betsu = null;
                } else if (null != alt && betsu.intValue() == alt.intValue()) {
                    betsu = null;
                }
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("引数が不正(altCode)");
            }
        }
        return new Kintai(category, new Integer(codeint), name, mark, alt, betsu);
    }

    /**
     * 勤怠コード(int)から、勤怠を得る。
     * @param category カテゴリー
     * @param code 勤怠コード
     * @return 勤怠
     */
    public static Kintai getInstance(
            final Category category,
            final int code
    ) {
        return getEnum(category, MYCLASS, new Integer(code));
    }

    /**
     * 勤怠コード(String)から、勤怠を得る。
     * @param category カテゴリー
     * @param code 勤怠コード
     * @return 勤怠
     */
    public static Kintai getInstance(
            final Category category,
            final String code
    ) {
        final int codeint = Integer.parseInt(code);
        return getInstance(category, codeint);
    }

    /**
     * 「出席」のインスタンスを得る。
     * @param category カテゴリー
     * @return 「出席」のインスタンス
     */
    public static Kintai getSeated(final Category category) {
        return getInstance(category, SEATED_CODE);
    }

    /**
     * 勤怠の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Kintai&gt;</code>
     */
    public static List<Kintai> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 勤怠の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;勤怠コード, Kintai&gt;</code>
     */
    public static Map<Integer, Kintai> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 勤怠の数を得る。
     * @param category カテゴリー
     * @return 勤怠の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 勤怠の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // Kintai

// eof
