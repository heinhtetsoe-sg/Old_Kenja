// kanji=漢字
/*
 * $Id: HomeRoom.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/06/07 16:05:14 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * ホームルーム（学年と組）。
 * @author tamura
 * @version $Id: HomeRoom.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class HomeRoom extends MyEnum<String, HomeRoom> implements Clearable {
    private static final Class<HomeRoom> MYCLASS = HomeRoom.class;

    private final Grade _grade;
    private final String _room;
    private final String _name;
    private final String _abbr;
    private final Set<Student> _students = new TreeSet<Student>();
    private final Set<Student> _unmodStudents = Collections.unmodifiableSet(_students);
    private final Set<Staff> _staffs = new TreeSet<Staff>();
    private final Set<Staff> _unmodStaffs = Collections.unmodifiableSet(_staffs);

    /*
     * コンストラクタ。
     */
    private HomeRoom(
            final Category category,
            final String key,
            final Grade grade,
            final String room,
            final String name,
            final String nameAbbr
    ) {
        super(category, key);

        _grade = grade;
        _room = room;
        _name = name;
        _abbr = nameAbbr;

        _grade.addHomeRoom(this);
    }

    /**
     * 学年を得る。
     * @return 学年
     * {@inheritDoc}
     */
    public Grade getGrade() {
        checkAlive();
        return _grade;
    }

    /**
     * 組を得る。
     * @return 組(1...)
     * {@inheritDoc}
     */
    public String getRoom() {
        checkAlive();
        return _room;
    }

    /**
     * ホームルーム名称を得る。
     * @return ホームルーム名称
     */
    public String getName() {
        checkAlive();
        return _name;
    }

    /**
     * ホームルーム略称を得る。
     * @return ホームルーム略称
     * {@inheritDoc}
     */
    public String getAbbr() {
        checkAlive();
        return _abbr;
    }

    /**
     * このホームルームの全ての生徒の<code>Collection</code>を得る。
     * @return <code>Collection&lt;Student&gt;</code>
     * {@inheritDoc}
     */
    public Collection<Student> getStudents() {
        checkAlive();
        return _unmodStudents;
    }

    /**
     * このホームルームの生徒を追加する。
     * @param student 生徒
     */
    public void addStudent(final Student student) {
        checkAlive();
        _students.add(student);
    }

    /**
     * このホームルームを担当する職員の<code>Collection</code>を得る。
     * @return <code>Collection&lt;Staff&gt;</code>
     * {@inheritDoc}
     */
    public Collection<Staff> getStaffs() {
        checkAlive();
        return _unmodStaffs;
    }

    /**
     * このホームルームを担当する職員を追加する。
     * @param staff 職員
     */
    public void addStaff(final Staff staff) {
        checkAlive();
        _staffs.add(staff);
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        checkAlive();
        _students.clear();
        _staffs.clear();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _abbr;
    }

    /**
     * ホームルームのSetの文字列を得る。
     * @param homeRooms ホームルームのSet。<code>Set&lt;HomeRoom&gt;</code>
     * @param whenEmpty 空の場合の文字列
     * @return 文字列
     */
    public static String toString(
            final Set<HomeRoom> homeRooms,
            final String whenEmpty
    ) {
        if (homeRooms.isEmpty()) {
            return whenEmpty;
        }

        HomeRoom first = null;
        for (final Iterator<HomeRoom> it = homeRooms.iterator(); it.hasNext();) {
            first = it.next();
            break;
        }

        if (first instanceof HomeRoom) {
            // 対象学級の最初の学級の文字列を返す
            return first.toString();
        }

        // null==firstの場合もここ。ありえないハズだが念のため
        return "エラー" + whenEmpty;
    }

    /*
     * 学年、組を文字列にする。
     * 例：makeKey("1", "A1"); // --> "1%A1"
     * @param grade 学年
     * @param room 組
     * @return 文字列
     */
    private static String makeKey(
            final Grade grade,
            final String room
    ) {
        return grade.getCode() + "%" + room;
    }

    /**
     * なければ、ホームルームのインスタンスを作成する。
     * すでに同じ学年・組のインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param grade 学年
     * @param room 組
     * @param name ホームルーム名称
     * @param nameAbbr ホームルーム略称
     * @return ホームルーム
     */
    public static HomeRoom create(
            final Category category,
            final Grade grade,
            final String room,
            final String name,
            final String nameAbbr
    ) {
        if (null == category) { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == grade)    { throw new IllegalArgumentException("引数が不正(grade)"); }
        if (null == room)     { throw new IllegalArgumentException("引数が不正(room)"); }

        final HomeRoom found = getInstance(category, grade, room);
        if (null != found) {
            return found;
        }

        if (null == name)     { throw new IllegalArgumentException("引数が不正(name)"); }
        if (null == nameAbbr) { throw new IllegalArgumentException("引数が不正(nameAbbr)"); }

        return new HomeRoom(category, makeKey(grade, room), grade, room, name, nameAbbr);
    }

    /**
     * なければ、ホームルームのインスタンスを作成する。
     * すでに同じ学年・組のインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param gradeCode 学年コード
     * @param room 組
     * @param name ホームルーム名称
     * @param nameAbbr ホームルーム略称
     * @return ホームルーム
     */
    public static HomeRoom create(
            final Category category,
            final String gradeCode,
            final String room,
            final String name,
            final String nameAbbr
    ) {
        if (null == category)  { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == gradeCode) { throw new IllegalArgumentException("引数が不正(gradeCode)"); }

        return create(category, Grade.create(category, gradeCode), room, name, nameAbbr);
    }

    /**
     * 学年、組から、ホームルームを得る。
     * @param category カテゴリー
     * @param grade 学年
     * @param room 組
     * @return ホームルーム
     */
    public static HomeRoom getInstance(
            final Category category,
            final Grade grade,
            final String room
    ) {
        if (null == grade) {
            return null;
        }
        return getEnum(category, MYCLASS, makeKey(grade, room));
    }

    /**
     * 学年、組から、ホームルームを得る。
     * @param category カテゴリー
     * @param gradeCode 学年コード
     * @param room 組
     * @return ホームルーム
     */
    public static HomeRoom getInstance(
            final Category category,
            final String gradeCode,
            final String room
    ) {
        final Grade grade = Grade.getInstance(category, gradeCode);
        if (null == grade) {
            return null;
        }
        return getInstance(category, grade, room);
    }

    /**
     * ホームルームの列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;HomeRoom&gt;</code>
     */
    public static List<HomeRoom> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * ホームルームの列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;学年、組, HomeRoom&gt;</code>
     */
    public static Map<String, HomeRoom> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * ホームルームの数を得る。
     * @param category カテゴリー
     * @return ホームルームの数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * ホームルームの列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // HomeRoom

// eof
