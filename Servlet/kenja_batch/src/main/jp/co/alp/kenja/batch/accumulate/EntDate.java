/*
 * $Id: EntDate.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/03/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import jp.co.alp.kenja.common.domain.KenjaDate;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 生徒の入学日付。
 * @author maesiro
 * @version $Id: EntDate.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class EntDate extends MyEnum<String, EntDate> {

    private static final Class<EntDate> MYCLASS = EntDate.class;
    private final KenjaDate _entDate;
    private final String _str;

    /*
     */
    private EntDate(final Category category,
            final String code,
            final KenjaDate entDate) {
        super(category, code);
        _entDate = entDate;
        _str = "入学日付 " + entDate.toString() + " （学籍番号" + code + "）";
    }

    /**
     * 入学日付を得る。
     * @return 入学日付
     */
    public KenjaDate getDate() {
        return _entDate;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _str;
    }

    /**
     * 学籍番号から入学日付のインスタンスを得る。
     * @param category カテゴリー
     * @param code 学籍番号
     * @return 入学日付
     */
    public static EntDate getInstance(
            final Category category,
            final String code
    ) {
        return getEnum(category, MYCLASS, code);
    }

    /**
     * なければ、生徒のインスタンスを作成する。
     * すでに同じ学籍番号のインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 学籍番号
     * @param entDate 入学日付
     * @return 入学日付のインスタンス
     */
    public static EntDate create(
            final Category category,
            final String code,
            final KenjaDateImpl entDate
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }

        final EntDate found = getInstance(category, code);
        if (null != found) {
            return found;
        }

        return new EntDate(category, code, entDate);
    }
}
