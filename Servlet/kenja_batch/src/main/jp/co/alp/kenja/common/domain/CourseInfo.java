// kanji=漢字
/*
 * $Id: CourseInfo.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/21 18:58:46 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;

import jp.co.alp.kenja.common.lang.enums.MyEnum;

import org.apache.commons.lang.StringUtils;

/**
 * コース情報。
 * @author tamura
 * @version $Id: CourseInfo.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class CourseInfo extends MyEnum<String, CourseInfo> {
    private static final Class<CourseInfo> MYCLASS = CourseInfo.class;

    private final String _courseCd;
    private final String _majorCd;
    private final String _courseCode;
    private final Grade _grade;
    private String _courseName;
    private String _majorName;
    private String _coursecodeName;

    /*
     * コンストラクタ。
     */
    private CourseInfo(
            final Category category,
            final String courseCd,
            final String majorCd,
            final String courseCode,
            final Grade grade
    ) {
        super(category, code(courseCd, majorCd, courseCode, grade));
        _courseCd = courseCd;
        _majorCd = majorCd;
        _courseCode = courseCode;
        _grade = grade;
    }

    /**
     * 課程コードを得る。
     * @return 課程コード
     */
    public String getCourseCd() { return _courseCd; }

    /**
     * 学科コードを得る。
     * @return 学科コード
     */
    public String getMajorCd() { return _majorCd; }

    /**
     * コースコードを得る。
     * @return コースコード
     */
    public String getCourseCode() { return _courseCode; }

    /**
     * 課程名を得る。
     * @return 課程名
     */
    public String getCourseName() { return _courseName; }

    /**
     * 学科名を得る。
     * @return 学科名
     */
    public String getMajorName() { return _majorName; }

    /**
     * コース名を得る。
     * @return コース名
     */
    public String getCourseCodeName() { return _coursecodeName; }

    /**
     * 課程名をセットする。
     */
    public void setCourseName(final String courseName) { _courseName = courseName; }

    /**
     * 学科名をセットする。
     */
    public void setMajorName(final String majorName) { _majorName = majorName; }

    /**
     * コース名をセットする。
     */
    public void setCourseCodeName(final String coursecodeName) { _coursecodeName = coursecodeName; }

    /**
     * 学年を得る。
     * @return 学年
     */
    public Grade getGrade() { return _grade; }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return StringUtils.defaultString(_majorName) + StringUtils.defaultString(_coursecodeName);
    }

    /*
     */
    private static String code(
            final String courseCd,
            final String majorCd,
            final String courseCode,
            final Grade grade
    ) {
        return StringUtils.defaultString(grade.getCode(), "-") + ":"
                + StringUtils.defaultString(courseCd, "-") + ":"
                + StringUtils.defaultString(majorCd, "-") + ":"
                + StringUtils.defaultString(courseCode, "-");
    }

    /**
     * なければ、インスタンスを作成する。
     * すでにインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param courseCd 課程コード
     * @param majorCd 学科コード
     * @param courseCode コースコード
     * @param grade 学年
     * @return インスタンス
     */
    public static CourseInfo create(
            final Category category,
            final String courseCd,
            final String majorCd,
            final String courseCode,
            final Grade grade
    ) {
        final CourseInfo found = getInstance(category, courseCd, majorCd, courseCode, grade);
        if (null != found) {
            return found;
        }
        return new CourseInfo(category, courseCd, majorCd, courseCode, grade);
    }

    /**
     * インスタンスを得る。
     * @param category カテゴリー
     * @param courseCd 課程コード
     * @param majorCd 学科コード
     * @param courseCode コースコード
     * @param grade 学年
     * @return インスタンス
     */
    public static CourseInfo getInstance(
            final Category category,
            final String courseCd,
            final String majorCd,
            final String courseCode,
            final Grade grade
    ) {
        if (null == grade) {
            return null;
        }
        return getEnum(category, MYCLASS, code(courseCd, majorCd, courseCode, grade));
    }

    /**
     * コースの列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;CourseInfo&gt;</code>
     */
    public static List<CourseInfo> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }
} // CourseInfo

// eof
