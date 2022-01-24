// kanji=漢字
/*
 * $Id: SubClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/06/07 18:11:54 - JST
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
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.Clearable;
import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 科目。
 * @author tamura
 * @version $Id: SubClass.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class SubClass extends MyEnum<String, SubClass> implements DomainItem, Clearable {
    /** 空の科目 */
    public static final SubClass NULL = new SubClass(Category.NULL, true, "", "", "", "", "", "") {
        public String toString() { return ""; }
    };

    /** log */
    private static final Log log = LogFactory.getLog(SubClass.class);
    private static final Class<SubClass> MYCLASS = SubClass.class;

    private final String _classCd;
    private final String _schoolKind;
    private final String _curriculumCd;
    private final String _subClassCd;
    private final String _name;
    private final String _abbr;
    private final String _str;

    private final Set<Chair> _chairs = new TreeSet<Chair>();
    private final Set<Chair> _unmodChairs = Collections.unmodifiableSet(_chairs);

    private final Map<Student, StudentAbsenceHigh> _studentAbsenceHigh = new TreeMap<Student, StudentAbsenceHigh>();

    /*
     * コンストラクタ。
     */
    /*pkg*/SubClass(
            final Category category,
            final boolean keepAlive,
            final String classCd,
            final String schoolKind,
            final String curriculumCd,
            final String subClassCd,
            final String name,
            final String abbr
    ) {
        super(category, keepAlive, code(classCd, schoolKind, curriculumCd, subClassCd));

        _classCd = classCd;
        _schoolKind = schoolKind;
        _curriculumCd = curriculumCd;
        _subClassCd = subClassCd;
        _abbr = abbr;
        _name = name;
        _str = code(classCd, schoolKind, curriculumCd, subClassCd) + ":" + _abbr;
    }

    /*
     * コンストラクタ。
     */
    private SubClass(
            final Category category,
            final String classCd,
            final String schoolKind,
            final String curriculumCd,
            final String subClassCd,
            final String name,
            final String abbr
    ) {
        this(category, false, classCd, schoolKind, curriculumCd, subClassCd, name, abbr);
    }

    /**
     * コードを得る。
     * @return コード
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
     * 名称を得る。
     * @return 名称
     */
    public Object getName() {
        checkAlive();
        return _name;
    }

    /**
     * 略称を得る。
     * @return 略称
     */
    public String getAbbr() {
        checkAlive();
        return _abbr;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _str;
    }

    /**
     * 教科コードを得る。
     * @return 教科コード
     */
    public String getClassCd() {
        checkAlive();
        return _classCd;
    }

    /**
     * 学校種別を得る。
     * @return 学校種別
     */
    public String getSchoolKind() {
        checkAlive();
        return _schoolKind;
    }

    /**
     * 教育課程コードを得る。
     * @return 教育課程コード
     */
    public String getCurriculumCd() {
        checkAlive();
        return _curriculumCd;
    }

    /**
     * 科目コードを得る。
     * @return 科目コード
     */
    public String getSubClassCd() {
        checkAlive();
        return _subClassCd;
    }

    /**
     * この科目に属する講座を追加する。
     * @param chair 講座
     */
    public void addChair(final Chair chair) {
        _chairs.add(chair);
    }

    /**
     * この科目に属する講座の集合を得る。
     * @return 講座の集合
     */
    public Collection<Chair> getChairs() {
        return _unmodChairs;
    }

    /**
     * 生徒のこの科目の欠課上限値を追加する。
     * @param student 生徒のインスタンス
     * @param absenceHigh 欠課上限値
     */
    public synchronized void addAbsenceHigh(
            final Student student,
            final StudentAbsenceHigh absenceHigh
    ) {
        checkAlive();
        _studentAbsenceHigh.put(student, absenceHigh);
    }


    /**
     * 生徒のこの科目の欠課上限値を得る。
     * @param student 生徒
     * @return 欠課上限値
     */
    public synchronized StudentAbsenceHigh getStudentAbsenceHigh(final Student student) {
        checkAlive();
        return (StudentAbsenceHigh) _studentAbsenceHigh.get(student);
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        checkAlive();
        _chairs.clear();
    }

    // info
    //------------------------------------------------------------------------

    /**
     * 科目infoを得る。
     * @return 科目info
     */
    public String getInfoSubClass() {
        return toString(); // getCode() + ":" + getAbbr();
    }

    //------------------------------------------------------------------------

    /**
     * なければ、科目のインスタンスを作成する。
     * すでに同じコードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param classCd 教科コード
     * @param schoolKind 学校種別
     * @param curriculumCd 教育課程コード
     * @param subClassCd 科目コード
     * @param name 科目名称
     * @param abbr 科目略称
     * @return 科目のインスタンス
     */
    public static SubClass create(
            final Category category,
            final String classCd,
            final String schoolKind,
            final String curriculumCd,
            final String subClassCd,
            final String name,
            final String abbr
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }

        final SubClass found = getInstance(category, classCd, schoolKind, curriculumCd, subClassCd);
        if (null != found) {
            return found;
        }

        if (null == abbr) { throw new IllegalArgumentException("引数が不正(abbr)"); }

        return new SubClass(category, classCd, schoolKind, curriculumCd, subClassCd, name, abbr);
    }

    /**
     * コードから、科目を得る。
     * @param category カテゴリー
     * @param classCd 教科コード
     * @param schoolKind 学校種別
     * @param curriculumCd 教育課程コード
     * @param subClassCd 科目コード
     * @return 科目
     */
    public static SubClass getInstance(
            final Category category,
            final String classCd,
            final String schoolKind,
            final String curriculumCd,
            final String subClassCd
    ) {
        final String code = code(classCd, schoolKind, curriculumCd, subClassCd);
        if (code.equals(NULL.getCode())) {
            return NULL;
        }
        return (SubClass) getEnum(category, MYCLASS, code);
    }

    /**
     * 科目の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;SubClass&gt;</code>
     */
    public static List<SubClass> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 科目の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;科目のキー, SubClass&gt;</code>
     */
    public static Map<String, SubClass> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 科目の数を得る。
     * @param category カテゴリー
     * @return 科目の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 科目の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

    /**
     * 
     * @param classCd 教科コード
     * @param schoolKind 学校種別
     * @param curriculumCd 教育課程コード
     * @param subClassCd 科目コード
     * @return コード
     */
    public static String code(final String classCd, final String schoolKind, final String curriculumCd, final String subClassCd) {
        if (null == classCd && null == schoolKind && null == curriculumCd) {
            return subClassCd;
        }
        return StringUtils.defaultString(classCd) + "-"
            + StringUtils.defaultString(schoolKind) + "-"
            + StringUtils.defaultString(curriculumCd) + "-"
            + StringUtils.defaultString(subClassCd);
    }
} // SubClass

// eof
