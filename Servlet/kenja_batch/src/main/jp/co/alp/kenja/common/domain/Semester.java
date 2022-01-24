// kanji=漢字
/*
 * $Id: Semester.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/06/04 21:03:10 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 学期。
 * @author tamura
 * @version $Id: Semester.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class Semester extends MyEnum<Integer, Semester> {
    /** log */
    private static final Log log = LogFactory.getLog(Semester.class);
    private static final Class<Semester> MYCLASS = Semester.class;

    private static final int THROUGH_THE_YEAR = 9;

    private final String _name;
    private final KenjaDateImpl _sDate;
    private final KenjaDateImpl _eDate;
    private final String _str;

    /*
     * コンストラクタ。
     */
    private Semester(
            final Category category,
            final int code,
            final String name,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        super(category, new Integer(code));

        _name = name;
        _sDate = sDate;
        _eDate = eDate;
        _str = code + ":" + _name + "[" + _sDate + "," + _eDate + "]";
    }

    /**
     * 学期コードを得る。
     * @return 学期コード
     */
    public int getCode() {
        checkAlive();
        final Comparable<Integer> key = getKey();
        if (key instanceof Integer) {
            return ((Integer) key).intValue();
        }

        log.error("key'" + key + "'がIntegerではない");
        throw new IllegalStateException("key'" + key + "'がIntegerではない");
    }

    /**
     * 学期コードを得る。
     * @return 学期コード
     */
    public String getCodeAsString() {
        return String.valueOf(getCode());
    }

    /**
     * 学期名を得る。
     * @return 学期名
     */
    public String getName() {
        checkAlive();
        return _name;
    }

    /**
     * 学期開始日付を得る。
     * @return 学期開始日付
     */
    public KenjaDateImpl getSDate() {
        checkAlive();
        return _sDate;
    }

    /**
     * 学期終了日付を得る。
     * @return 学期終了日付
     */
    public KenjaDateImpl getEDate() {
        checkAlive();
        return _eDate;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _str;
    }

    /**
     * 引数の日付が、この学期の範囲内か検査する。
     * @param date 検査する日付
     * @return 範囲内なら<code>true</code>
     */
    public boolean isValidDate(final KenjaDateImpl date) {
        if (getSDate().compareTo(date) > 0) {
            // sDate > date なら、範囲外
            return false;
        }
        if (date.compareTo(getEDate()) > 0) {
            // date > eDate なら、範囲外
            return false;
        }
        return true;
    }

    /**
     * 「年間」を表す学期を得る。
     * @param category カテゴリ
     * @return 「年間」を表す学期
     */
    public static Semester throughTheYear(final Category category) {
        return getInstance(category, THROUGH_THE_YEAR);
    }

    /**
     * 学期
     * @param category カテゴリー
     * @param code 学期コード
     * @param name 学期名
     * @param sDate 学期開始日付
     * @param eDate 学期終了日付
     * @return 学期
     */
    public static Semester create(
            final Category category,
            final int code,
            final String name,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (0 >= code)          { throw new IllegalArgumentException("引数が不正(code)"); }

        final Semester found = getInstance(category, code);
        if (null != found) {
            return found;
        }

        if (null == name)  { throw new IllegalArgumentException("引数が不正(name)"); }
        if (null == sDate) { throw new IllegalArgumentException("引数が不正(sDate)"); }
        if (null == eDate) { throw new IllegalArgumentException("引数が不正(eDate)"); }

        return new Semester(category, code, name, sDate, eDate);
    }

    /**
     * 学期コードから、学期を得る。
     * @param category カテゴリー
     * @param code 学期コード
     * @return 学期
     */
    public static Semester getInstance(
            final Category category,
            final int code
    ) {
        return getEnum(category, MYCLASS, new Integer(code));
    }

    /**
     * 学期の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Semester&gt;</code>
     */
    public static List<Semester> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 学期の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;学期コード, Semester&gt;</code>
     */
    public static Map<Integer, Semester> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 学期の数を得る。
     * @param category カテゴリー
     * @return 学期の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 学期の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // Semester

// eof
