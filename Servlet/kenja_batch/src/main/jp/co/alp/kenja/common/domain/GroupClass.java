// kanji=漢字
/*
 * $Id: GroupClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/06/07 20:16:01 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 選択科目。
 * @author tamura
 * @version $Id: GroupClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class GroupClass extends MyEnum<String, GroupClass> implements ChairsHolder, Clearable {
    /** 非選択科目を表す */
    public static final GroupClass ZERO = new GroupClass(Category.NULL, true, "0000", "", "", "") {
        public String toString() {
            return "";
        }
        public Class<GroupClass> getEnumClass() {
            return GroupClass.class;
        }
        /*pkg*/void addChair(final Chair c) { /*nothing*/ }
        public Collection<Chair> getChairs() { return Collections.emptySet(); }
        public void addHomeRoom(final HomeRoom h) { /*nothing*/ }
        public Set<HomeRoom> getHomeRooms() { return Collections.emptySet(); }
    };

    /** log */
    private static final Log log = LogFactory.getLog(GroupClass.class);
    private static final Class<GroupClass> MYCLASS = GroupClass.class;

    private static final List<GroupClass> LIST_ZERO = Collections.singletonList(ZERO);

    /** この群に属する講座のSet。<code>Set&lt;Chair&gt;</code> */
    private final Set<Chair> _chairs = new TreeSet<Chair>();
    private final Set<Chair> _unmodChairs = Collections.unmodifiableSet(_chairs);

    /** この群の対象年組のSet。<code>Set&lt;HomeRoom&gt;</code> */
    private final Set<HomeRoom> _homerooms = new TreeSet<HomeRoom>();
    private final Set<HomeRoom> _unmodHomerooms = Collections.unmodifiableSet(_homerooms);

    private final String _name;
    private final String _abbr;
    private final String _remark;
    private final String _str;

    /*
     * コンストラクタ。
     */
    /*pkg*/GroupClass(
            final Category category,
            final boolean keepAlive,
            final String code,
            final String name,
            final String abbr,
            final String remark
    ) {
        super(category, keepAlive, code);

        _name = name;
        _abbr = abbr;
        _remark = remark;
        _str = code + ":" + _abbr;
    }

    /*
     * コンストラクタ。
     */
    private GroupClass(
            final Category category,
            final String code,
            final String name,
            final String abbr,
            final String remark
    ) {
        this(category, false, code, name, abbr, remark);
    }

    /**
     * 群コードを得る。
     * @return 群コード
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
     * 群名称を得る。
     * @return 群名称
     */
    public String getName() {
        checkAlive();
        return _name;
    }

    /**
     * 群略称を得る。
     * @return 群略称
     */
    public String getAbbr() {
        checkAlive();
        return _abbr;
    }

    /**
     * 備考を得る。
     * @return 備考
     */
    public String getRemark() {
        checkAlive();
        return _remark;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _str + "[" + _chairs.size() + "]";
    }

    /*
     * この群に属する講座を追加する。
     * @param chair 講座
     */
    /*pkg*/void addChair(final Chair chair) {
        checkAlive();
        _chairs.add(chair);
    }

    /**
     * この群に属するすべての講座の<code>Collection</code>を得る。
     * @return <code>Collection&lt;Chair&gt;</code>
     */
    public Collection<Chair> getChairs() {
        checkAlive();
        return _unmodChairs;
    }

    /**
     * この群の対象年組を追加する。
     * @param homeRoom 対象年組
     */
    public void addHomeRoom(final HomeRoom homeRoom) {
        checkAlive();
        _homerooms.add(homeRoom);

        for (final Chair chair : _chairs) {
            homeRoom.getGrade().addChair(chair);
            // 群に属する各講座の正副職員に、対象年組を設定する
            chair.addHomeRoomStaff0(homeRoom);
        }
    }

    /**
     * この群のすべての対象年組の<code>Set</code>を得る。
     * @return <code>Set&lt;HomeRoom&gt;</code>
     */
    public Set<HomeRoom> getHomeRooms() {
        checkAlive();
        return _unmodHomerooms;
    }

    /**
     * @see jp.co.alp.kenja.common.lang.Clearable#clear()
     */
    public void clear() {
        checkAlive();
        _chairs.clear();
        _homerooms.clear();
    }

    // info
    //------------------------------------------------------------------------

    /**
     * 群(選択科目)infoを得る。
     * @return 群(選択科目)info
     */
    public String getInfoGroup() {
        if (ZERO.equals(this)) {
            return "";
        }
        return getCode() + ":" + getName();
    }

    //------------------------------------------------------------------------

    /**
     * なければ、選択科目のインスタンスを作成する。
     * すでに同じ群コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 群コード
     * @param name 群名称
     * @param abbr 群略称
     * @param remark 備考
     * @return 選択科目のインスタンス
     */
    public static GroupClass create(
            final Category category,
            final String code,
            final String name,
            final String abbr,
            final String remark
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }

        final GroupClass found = getInstance(category, code);
        if (null != found) {
            return found;
        }

        if (null == abbr)       { throw new IllegalArgumentException("引数が不正(abbr)"); }

        final String lName = (null != name) ? name : abbr;
        final String lRemark = (null != remark) ? remark : "";

        return new GroupClass(category, code, lName, abbr, lRemark);
    }

    /**
     * 群コードから、選択科目を得る。
     * @param category カテゴリー
     * @param code 群コード
     * @return 選択科目
     */
    public static GroupClass getInstance(
            final Category category,
            final String code
    ) {
        if (ZERO.getCode().equals(code)) {
            return ZERO;
        }
        return getEnum(category, MYCLASS, code);
    }

    /**
     * 選択科目の列挙のListを得る。
     * {@link GroupClass#ZERO}を含む。
     * @param category カテゴリー
     * @return <code>List&lt;GroupClass&gt;</code>
     */
    public static List<GroupClass> getEnumList(final Category category) {
        final List<GroupClass> rtn = getEnumList(category, MYCLASS);
        if (rtn.isEmpty()) {
            return LIST_ZERO;
        }
        return rtn;
    }

    /**
     * 選択科目の列挙のMapを得る。
     * {@link GroupClass#ZERO}を含む。
     * @param category カテゴリー
     * @return <code>Map&lt;群コード, GroupClass&gt;</code>
     */
    public static Map<String, GroupClass> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 選択科目の数を得る。
     * @param category カテゴリー
     * @return 選択科目の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 選択科目がないか判定する。
     * @param category カテゴリー
     * @return なければ<code>true</code>を返す
     */
    public static boolean isEmpty(final Category category) {
        final List<GroupClass> list = getEnumList(category);
        for (final GroupClass group : list) {
            if (!group.equals(GroupClass.ZERO)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 選択科目の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // GroupClass

// eof
