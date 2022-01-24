// kanji=漢字
/*
 * $Id: Staff.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/06/07 11:26:38 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 職員。
 * @author tamura
 * @version $Id: Staff.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class Staff extends MyEnum<String, Staff> implements DomainItem, Clearable {
    /** log */
    private static final Log log = LogFactory.getLog(Staff.class);
    private static final Class<Staff> MYCLASS = Staff.class;

    /*pkg*/final HashMap<ChargeDiv, TreeSet<Chair>> _chairs = new HashMap<ChargeDiv, TreeSet<Chair>>() {
        public Set<Chair> get(final ChargeDiv key) {
            final Set<Chair> rtn = super.get(key);
            if (null == rtn) {
                return Collections.emptySet();
            }
            return rtn;
        }
    };
    
    private Set<Chair> chairsValues() {
        final Set<Chair> rtn = new TreeSet<Chair>();
        for (final Collection<Chair> values : _chairs.values()) {
            rtn.addAll(values);
        }
        return rtn;
    }

//    /*pkg*/final MultiHashMap _chairs = new MultiHashMap() {
//        protected Collection createCollection(final Collection coll) {
//            if (coll == null) {
//                return new TreeSet();
//            } else {
//                return new TreeSet(coll);
//            }
//        }
//    };

    private final String _showCode;
    private final String _name;
    private final String _showName;
    private final String _str;

    /*
     * コンストラクタ。
     */
    private Staff(
            final Category category,
            final String code,
            final String showCode,
            final String name,
            final String showName
    ) {
        super(category, code);

        _showCode = showCode;
        _name = name;
        _showName = showName;
        _str = code + ":" + _showName;
    }

    /**
     * 職員コードを得る。
     * @return 職員コード
     * {@inheritDoc}
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
     * 職員コード表示用を得る。
     * @param postString あとにつづく文字列
     * @return 職員コード表示用
     * {@inheritDoc}
     */
    public String getShowCode(final String postString) {
        if (StringUtils.isEmpty(_showCode)) {
            return "";
        }
        return _showCode + StringUtils.defaultString(postString);
    }

    /**
     * 職員氏名を得る。
     * @return 職員氏名
     * {@inheritDoc}
     */
    public String getName() {
        checkAlive();
        return _name;
    }

    /**
     * 職員氏名表示用を得る。
     * @return 職員氏名表示用
     */
    public String getShowName() {
        checkAlive();
        return _showName;
    }

    /**
     * 職員同士を比較する。
     * @param staff 他の職員
     * @return 比較結果
     */
    public int compareTo(final Staff staff) {
        checkAlive();
        if (this == staff) {
            return 0;
        }
        return getKey().compareTo(staff.getKey());
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _showName;
    }

    /**
     * この職員が正担当する講座の<code>Set</code>を得る。
     * @param hr <code>true</code>なら群に属さない講座のみ。<code>false</code>なら群に属する講座のみ。
     * @return <code>Set&lt;Chair&gt;</code>
     */
    public Set<Chair> getChairs(final boolean hr) {
        checkAlive();
        final Set<Chair> rtn = new TreeSet<Chair>(getChairs(ChargeDiv.REGULAR));
        for (final Iterator<Chair> it = rtn.iterator(); it.hasNext();) {
            final Chair chair = it.next();
            if (hr == (GroupClass.ZERO != chair.getGroup())) {
                it.remove();
            }
        }
        return rtn;
    }

    /**
     * この職員が担当する講座の<code>Set</code>を得る。
     * @param chargeDiv 担任区分
     * @return <code>Set&lt;Chair&gt;</code>
     * @see Chair#getStaffs Chair#getStaffs
     */
    public Set<Chair> getChairs(final ChargeDiv chargeDiv) {
        checkAlive();
        final Set<Chair> set = _chairs.get(chargeDiv);
        if (null == set) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(set);
    }

    /**
     * いずれかの講座の正担当であるかを判定する。
     * @return いずれかの講座の正担当であるなら<code>true</code>を返す
     */
    public boolean isRelgular() {
        checkAlive();
        final Set<Chair> set = _chairs.get(ChargeDiv.REGULAR);
        return !(null == set || set.isEmpty());
    }

    /**
     * この職員が担当する講座の<code>Set</code>を得る。
     * @return <code>Set&lt;Chair&gt;</code>
     */
    public Set<Chair> getChairs() {
        checkAlive();
        return Collections.unmodifiableSet(new TreeSet<Chair>(chairsValues()));
    }

    /**
     * @see jp.co.alp.kenja.common.lang.Clearable#clear()
     */
    public void clear() {
        checkAlive();
        _chairs.clear();
    }

    /**
     * なければ、職員のインスタンスを作成する。
     * すでに同じ職員コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 職員コード
     * @param name 職員氏名
     * @param showName 職員氏名表示用
     * @return 職員のインスタンス
     */
    public static Staff create(
            final Category category,
            final String code,
            final String showCode,
            final String name,
            final String showName
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == code)       { throw new IllegalArgumentException("引数が不正"); }

        final Staff found = getInstance(category, code);
        if (null != found) {
            return found;
        }
        if (null == name || null == showName) {
            throw new IllegalArgumentException("引数が不正");
        }
        return new Staff(category, code, showCode, name, showName);
    }

    /**
     * 職員コードから、職員を得る。
     * @param category カテゴリー
     * @param code 職員コード
     * @return 職員
     */
    public static Staff getInstance(
            final Category category,
            final String code
    ) {
        return (Staff) getEnum(category, MYCLASS, code);
    }

    /**
     * 職員の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Staff&gt;</code>
     */
    public static List<Staff> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 職員の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;職員コード, Staff&gt;</code>
     */
    public static Map<String, Staff> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 職員の数を得る。
     * @param category カテゴリー
     * @return 職員の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 職員の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

    /**
     * 職員のSetを文字列に変換する。
     * @param staffs 職員のSet
     * @param left 左の文字列//"["
     * @param middle 中の文字列//",_"
     * @param right 右の文字列//"]"
     * @return 文字列
     */
    public static String toString(
            final Set<Staff> staffs,
            final String left,
            final String middle,
            final String right
    ) {
        return toString(staffs, left, middle, right, Integer.MAX_VALUE,"", "", "", "", "", "", "", middle, "");
    }

    /**
     * 職員のSetを文字列に変換する。
     * @param staffs 職員のSet
     * @param left 左の文字列//"["
     * @param middle 中の文字列//",_"
     * @param right 右の文字列//"]"
     * @param maxLine 最大行数
     * @param left2 ブロック左の文字列//"["
     * @param middle2 ブロック中の文字列//",_"
     * @param right2 ブロック右の文字列//"]"
     * @param left3 行左の文字列//"["
     * @param middle3 行中の文字列//",_"
     * @param right3 行右の文字列//"]"
     * @param left4 行左の文字列//"["
     * @param middle4 行中の文字列//",_"
     * @param right4 行右の文字列//"]"
     * @return 文字列
     */
    public static String toString(
            final Set<Staff> staffs,
            final String left,
            final String middle,
            final String right,
            final int maxLine,
            final String left2,
            final String middle2,
            final String right2,
            final String left3,
            final String middle3,
            final String right3,
            final String left4,
            final String middle4,
            final String right4
    ) {
        if (null == staffs) {
            return null;
        }

        final String sLeft = StringUtils.defaultString(left, "[");
        final String sMiddle = StringUtils.defaultString(middle, ", ");
        final String sRight = StringUtils.defaultString(right, "]");

        if (0 == staffs.size()) {
            return sLeft + sRight; // "[]"
        }

        final StringBuffer buf = new StringBuffer(staffs.size() * 32);
        buf.append(sLeft);

        boolean hasNext;
        final List<Object> staffNames = new ArrayList<Object>();
        final Iterator<Staff> i = staffs.iterator();
        hasNext = i.hasNext();
        while (hasNext) {
            final Object o = i.next();
            if (o instanceof Staff) {
                final Staff staff = (Staff) o;
                staffNames.add(o == staffs ? "(this Collection)" : staff.getShowCode(" ") + staff.getShowName());
            }
            hasNext = i.hasNext();
        }

        List<Object> lines;
        if (staffNames.size() >= maxLine) {
            lines = new ArrayList<Object>();
            lines.add(left2);
            final int size = staffNames.size();
            final int maxColumn =  size / maxLine + (size % maxLine == 0 ? 0 : 1);
            int column = 0;
            StringBuffer line = null;
            for (final Object name : staffNames) {
                String blank = middle4;
                if (null == line || column >= maxColumn) {
                    if (column >= maxColumn) {
                        line.append(right3);
                        line.append(middle3);
                    }
                    line = new StringBuffer();
                    line.append(left3);
                    lines.add(line);
                    blank = "";
                    column = 0;
                }
                line.append(blank).append(left4).append(name).append(right4);
                column += 1;
            }
            line.append(right3);
            lines.add(right2);
        } else {
            lines = staffNames;
        }
        final Iterator<Object> i2 = lines.iterator();
        hasNext = i2.hasNext();
        while (hasNext) {
            final Object n = i2.next();
            buf.append(n);
            hasNext = i2.hasNext();
            if (hasNext) {
                buf.append(sMiddle); // ",_"
            }
        }

        buf.append(sRight); // "]"
        return buf.toString();
    }
} // Staff

// eof
