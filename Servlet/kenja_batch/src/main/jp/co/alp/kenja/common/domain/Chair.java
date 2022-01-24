// kanji=漢字
/*
 * $Id: Chair.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/06/08 10:42:30 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 講座。
 * @author tamura
 * @version $Id: Chair.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class Chair extends MyEnum<String, Chair> implements ChairsHolder, DomainItem, Clearable {
    /** 空の講座 */
    public static final Chair NULL = new Chair(Category.NULL, "0000000", GroupClass.ZERO, SubClass.NULL, "", null, null, false) {
        public String toString() { return ""; }
        public void addStaff(final Staff s, final ChargeDiv c) { /*nothing*/ }

        public void addHomeRoom(final HomeRoom h) { /*nothing*/ }
        public Set<HomeRoom> getHomeRooms() { return Collections.emptySet(); }

        public synchronized void addStudent(final KenjaDateImpl date, final Student student) { /*nothing*/ }
    };

    /** log */
    private static final Log log = LogFactory.getLog(Chair.class);
    private static final Class<Chair> MYCLASS = Chair.class;

    /** この講座の担当職員のMap。 */
    private final Map<ChargeDiv, TreeSet<Staff>> _staffs = new HashMap<ChargeDiv, TreeSet<Staff>>();
    private Collection<Staff> staffsValues() {
        final Collection<Staff> staffs = new TreeSet<Staff>();
        for (final TreeSet<Staff> chargeDivStaffs : _staffs.values()) {
            staffs.addAll(chargeDivStaffs);
        }
        return staffs;
    }

    /** この講座の対象年組のSet。<code>Set&lt;HomeRoom&gt;</code> */
    private final TreeSet<HomeRoom> _homerooms = new TreeSet<HomeRoom>();
    private final Set<HomeRoom> _unmodHomerooms = Collections.unmodifiableSet(_homerooms);

    /** この講座の生徒名簿 <code>Map&lt;日付,Set&lt;生徒&gt;&gt;</code> */
    private final SortedMap<KenjaDateImpl, SortedSet<Student>> _studentDateMap = new TreeMap<KenjaDateImpl, SortedSet<Student>>();
    private final SortedMap<KenjaDateImpl, SortedSet<Student>> _studentDateMapUnmodVal = new TreeMap<KenjaDateImpl, SortedSet<Student>>();

    private final GroupClass _group;
    private final SubClass _subClass;
    private final String _name;
    private final boolean _countFlag;
    private final String _str;

    /** この講座だけを含む、要素数が１の<code>Set</code>を得る。 */
    private final Set<Chair> _chairs;

    /*
     * コンストラクタ。
     */
    /*pkg*/Chair(
            final Category category,
            final String code,
            final GroupClass group,
            final SubClass subClass,
            final String name,
            final Integer lessonCount,
            final Integer frameCount,
            final boolean countFlag
    ) {
        super(category, Category.NULL == category, code);

        _group = group;
        _subClass = subClass;
        _name = name;
        _countFlag = countFlag;
        _str = code + ":" + _name;
        _group.addChair(this);
        _subClass.addChair(this);

        _chairs = Collections.singleton(this);
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
     * 群(選択科目)を得る。
     * @return 群(選択科目)
     */
    public GroupClass getGroup() {
        checkAlive();
        return _group;
    }

    /**
     * 科目を得る。
     * @return 科目
     */
    public SubClass getSubClass() {
        checkAlive();
        return _subClass;
    }

    /**
     * 講座名称を得る。
     * @return 講座名称
     */
    public String getName() {
        checkAlive();
        return _name;
    }

    /**
     * 集計フラグを得る。
     * @return 集計フラグ
     */
    public boolean getCountFlag() {
        checkAlive();
        return _countFlag;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _str + "/" + _group.toString();
    }

    /**
     * この講座を担当する職員を設定する。
     * @param staff 職員
     * @param chargeDiv 担任区分
     * @see #getStaffs getStaffs
     * @see Staff#getChairs Staff#getChairs
     */
    public void addStaff(
            final Staff staff,
            final ChargeDiv chargeDiv
    ) {
        checkAlive();
        if (!_staffs.containsKey(chargeDiv)) {
            _staffs.put(chargeDiv, new TreeSet<Staff>());
        }
        _staffs.get(chargeDiv).add(staff);
        if (staff._chairs.containsKey(chargeDiv)) {
            staff._chairs.put(chargeDiv, new TreeSet<Chair>());
        }
        if (!staff._chairs.get(chargeDiv).contains(this)) {
            staff._chairs.get(chargeDiv).add(this);
        }
    }

    /**
     * この講座だけを含む、要素数が１の<code>Collection</code>を得る。
     * {@inheritDoc}
     */
    public Collection<Chair> getChairs() {
        return _chairs;
    }

    /**
     * この講座の対象年組を追加する。
     * @param homeRoom 対象年組
     */
    public void addHomeRoom(final HomeRoom homeRoom) {
        checkAlive();
        _homerooms.add(homeRoom);
        homeRoom.getGrade().addChair(this);

        addHomeRoomStaff0(homeRoom);
    }

    /*
     * 講座の正副職員に、対象年組を設定する。
     * @param homeRoom 対象年組
     */
    /*pkg*/final void addHomeRoomStaff0(final HomeRoom homeRoom) {
        for (final Staff staff : staffsValues()) {
            homeRoom.addStaff(staff);
        }
    }

    /**
     * 群を考慮しながら、この講座のすべての対象年組の<code>Set</code>を得る。
     * @return <code>Set&lt;HomeRoom&gt;</code>
     */
    public Set<HomeRoom> getHomeRooms() {
        checkAlive();
        if (GroupClass.ZERO != getGroup()) {
            return getGroup().getHomeRooms();
        }
        return _unmodHomerooms;
    }

    /**
     * 講座の名簿に生徒を追加する。
     * @param date 日付
     * @param student 生徒のインスタンス
     */
    public synchronized void addStudent(
            final KenjaDateImpl date,
            final Student student
    ) {
        checkAlive();
        SortedSet<Student> set = _studentDateMap.get(date);
        if (null == set) {
            set = new TreeSet<Student>(); // FIXME: Student.SORTER
            _studentDateMap.put(date, set);

            _studentDateMapUnmodVal.put(date, Collections.unmodifiableSortedSet(set));
        }

        set.add(student);
    }

    /**
     * 名簿を空にする。
     */
    public synchronized void removeAllStudents() {
        checkAlive();
        for (final Iterator<SortedSet<Student>> it = _studentDateMap.values().iterator(); it.hasNext();) {
            final SortedSet<Student> students = it.next();
            students.clear();
        }
        _studentDateMap.clear();
        _studentDateMapUnmodVal.clear();
    }

    /**
     * @see jp.co.alp.kenja.common.lang.Clearable#clear()
     */
    public void clear() {
        checkAlive();
        _staffs.clear();
        _homerooms.clear();
        removeAllStudents();
    }

    // info
    //------------------------------------------------------------------------
    /**
     * 科目infoを得る。
     * @return 科目info
     */
    public String getInfoSubClass() {
        return getSubClass().getInfoSubClass();
    }

    /**
     * 群(選択科目)infoを得る。
     * @return 群(選択科目)info
     */
    public String getInfoGroup() {
        return getGroup().getInfoGroup();
    }

    /**
     * 講座infoを得る。
     * @return 講座info
     */
    public String getInfoChair() {
        return getCode() + ":" + getName();
    }

    //------------------------------------------------------------------------

    /*
     * 引数の妥当性を確認する。
     * @param code 講座コード
     * @param group 群(選択科目)
     * @param subClass 科目
     * @param name 講座名称
     * @param lessonCount 週授業回数
     * @param frameCount 連続枠数
     */
    private static void checkArgs(
            final String code,
            final GroupClass group,
            final SubClass subClass,
            final String name,
            final Integer lessonCount,
            final Integer frameCount
    ) {
        if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }
        if (null == group)      { throw new IllegalArgumentException("引数が不正(group)"); }
        if (null == subClass)   { throw new IllegalArgumentException("引数が不正(subClass)"); }
        if (null == name)       { throw new IllegalArgumentException("引数が不正(name)"); }
//        if (0 >= lessonCount)   { throw new IllegalArgumentException("引数が不正(lessonCount)"); }
//        if (0 >= frameCount)    { throw new IllegalArgumentException("引数が不正(frameCount)"); }
    }

    /**
     * なければ、講座のインスタンスを作成する。
     * すでに同じ講座コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 講座コード
     * @param group 群(選択科目)
     * @param subClass 科目
     * @param name 講座名称
     * @param lessonCount 週授業回数
     * @param frameCount 連続枠数
     * @param countFlag 集計フラグ
     * @return 講座のインスタンス
     */
    public static Chair create(
            final Category category,
            final String code,
            final GroupClass group,
            final SubClass subClass,
            final String name,
            final Integer lessonCount,
            final Integer frameCount,
            final boolean countFlag
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }

        final Chair found = getInstance(category, code);
        if (null != found) {
            return found;
        }

        checkArgs(code, group, subClass, name, lessonCount, frameCount);

        return new Chair(category, code, group, subClass, name, lessonCount, frameCount, countFlag);
    }

    /**
     * 講座コードから、講座を得る。
     * @param category カテゴリー
     * @param code 講座コード
     * @return 講座
     */
    public static Chair getInstance(
            final Category category,
            final String code
    ) {
        if (code.equals(NULL.getCode())) {
            return NULL;
        }
        return getEnum(category, MYCLASS, code);
    }

    /**
     * 講座の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Chair&gt;</code>
     */
    public static List<Chair> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 講座の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;講座コード, Chair&gt;</code>
     */
    public static Map<String, Chair> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 講座の数を得る。
     * @param category カテゴリー
     * @return 講座の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 講座の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // Chair

// eof
