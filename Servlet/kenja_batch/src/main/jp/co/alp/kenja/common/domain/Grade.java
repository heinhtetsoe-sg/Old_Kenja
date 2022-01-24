// kanji=漢字
/*
 * $Id: Grade.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/10/20 18:13:55 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 学年。
 * @author tamura
 * @version $Id: Grade.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class Grade extends MyEnum<String, Grade> implements IGrade, Clearable {
    private static final Class<Grade> MYCLASS = Grade.class;

    private final String _code;
    private final String _str;
    private final Set<HomeRoom> _homeRooms = new TreeSet<HomeRoom>();
    private final Set<HomeRoom> _unmodHomeRooms = Collections.unmodifiableSet(_homeRooms);
    private final Set<Chair> _chairs = new TreeSet<Chair>();
    private final Set<Chair> _unmodChairs = Collections.unmodifiableSet(_chairs);

    /*
     * コンストラクタ。
     */
    private Grade(
            final Category category,
            final String code
    ) {
        super(category, code);
        _code = code;
        _str = code + "学年";
    }

    /**
     * 学年コードを得る。
     * @return 学年コード
     * {@inheritDoc}
     */
    public String getCode() {
        checkAlive();
        return _code;
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        checkAlive();
        _homeRooms.clear();
        _chairs.clear();
    }

    /**
     * 年組を追加する。
     * @param homeRoom 年組
     */
    public void addHomeRoom(final HomeRoom homeRoom) {
        checkAlive();
        _homeRooms.add(homeRoom);
    }

    /**
     * 年組の<code>Collection</code>を得る。
     * @return 年組の<code>Collection</code>
     * {@inheritDoc}
     */
    public Collection<HomeRoom> getHomeRooms() {
        checkAlive();
        return _unmodHomeRooms;
    }

    /**
     * 講座を追加する。
     * @param chair 講座
     */
    public void addChair(final Chair chair) {
        checkAlive();
        _chairs.add(chair);
    }

    /**
     * 講座の<code>Collection</code>を得る。
     * @return 講座の<code>Collection</code>
     * {@inheritDoc}
     */
    public Collection<Chair> getChairs() {
        checkAlive();
        return _unmodChairs;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _str;
    }

    /**
     * なければ、学年のインスタンスを作成する。
     * すでに同じ学年コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param gradeCode 学年コード
     * @return 学年
     */
    public static Grade create(
            final Category category,
            final String gradeCode
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == gradeCode)      { throw new IllegalArgumentException("引数が不正(gradeCode)"); }

        final Grade found = getInstance(category, gradeCode);
        if (null != found) {
            return found;
        }

        return new Grade(category, gradeCode);
    }

    /**
     * 学年を得る。
     * @param category カテゴリー
     * @param gradeCode 学年コード
     * @return ホームルーム
     */
    public static Grade getInstance(
            final Category category,
            final String gradeCode
    ) {
        return getEnum(category, MYCLASS, gradeCode);
    }

    /**
     * 学年の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Grade&gt;</code>
     */
    public static List<Grade> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 学年の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;学年コード, Grade&gt;</code>
     */
    public static Map<String, Grade> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 学年の数を得る。
     * @param category カテゴリー
     * @return 学年の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 学年の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // Grade

// eof
