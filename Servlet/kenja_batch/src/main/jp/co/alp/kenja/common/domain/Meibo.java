/*
 * $Id: Meibo.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2011/06/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jp.co.alp.kenja.common.lang.enums.MyEnum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 講座名簿。
 * @author maesiro
 * @version $Id: Meibo.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class Meibo extends MyEnum<String, Meibo> {

    private static final Log log = LogFactory.getLog(Meibo.class);
    private static final Class<Meibo> MYCLASS = Meibo.class;

    private final Chair _chair;

    /** 名簿の生徒コード */
    private final Set<Student> _students = new TreeSet<Student>();
    private final Set<Student> _studentsUnmodVal = Collections.unmodifiableSet(_students);

    /*
     * コンストラクタ。
     */
    private Meibo(
            final Category category,
            final Chair chair
    ) {
        super(category, chair.getCode());
        _chair = chair;
    }

    /**
     * 講座コードを得る。
     * @return 講座コード
     */
    public String getCode() {
        final String key = getKey();
        if (key instanceof String) {
            return (String) key;
        }

        log.error("key'" + key + "'がStringではない");
        throw new IllegalStateException("key'" + key + "'がStringではない");
    }

    /**
     * 生徒のホームルームのコレクションを得る。
     * @return 生徒のホームルームのコレクション
     */
    public Collection<HomeRoom> getStudentHomeRooms() {
        final Set<HomeRoom> homeRooms = new TreeSet<HomeRoom>();
        for (final Student student : getStudents()) {
            if (null == student || null == student.getHomeRoom()) {
                continue;
            }
            homeRooms.add(student.getHomeRoom());
        }
        return homeRooms;
    }

    /**
     * 生徒のコードを追加する。
     * @param student 生徒
     */
    public synchronized void addStudent(
            final Student student
    ) {
        checkAlive();
        _students.add(student);
    }

    /**
     * 生徒のコードを空にする。
     */
    public synchronized void removeAllStudents() {
        checkAlive();
        _students.clear();
    }

    /**
     * 生徒のコードのコレクションを得る。
     * @return 生徒のコードのコレクション
     */
    public synchronized Collection<Student> getStudents() {
        checkAlive();
        return _studentsUnmodVal;
    }

    /**
     * なければ、名簿のインスタンスを作成する。
     * すでに同じ講座コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param chair 講座
     * @return 名簿
     */
    public static Meibo create(
            final Category category,
            final Chair chair
    ) {
        if (null == category)  { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == chair)     { throw new IllegalArgumentException("引数が不正(chair)"); }

        final Meibo found = getInstance(category, chair);
        if (null != found) {
            return found;
        }
        return new Meibo(category, chair);
    }

    /**
     * 講座から、名簿を得る。
     * @param category カテゴリー
     * @param chair 講座
     * @return 名簿
     */
    public static Meibo getInstance(
            final Category category,
            final Chair chair
    ) {
        return getEnum(category, MYCLASS, chair.getCode());
    }

    /**
     * 名簿の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;名簿&gt;</code>
     */
    public static List<Meibo> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 名簿の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;講座コード, 名簿&gt;</code>
     */
    public static Map<String, Meibo> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 名簿の数を得る。
     * @param category カテゴリー
     * @return 名簿の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 名簿の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

} // Meibo

//eof
