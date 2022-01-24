// kanji=漢字
/*
 * $Id: SemesterGrade.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2016/01/18 18:03:10 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 学期(学年ごと)。
 * @author maesiro
 * @version $Id: SemesterGrade.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class SemesterGrade extends MyEnum<String, SemesterGrade> {
    /** log */
    private static final Log log = LogFactory.getLog(SemesterGrade.class);
    private static final Class<SemesterGrade> MYCLASS = SemesterGrade.class;

    private final Semester _semester;
    private final Grade _grade;
    private final KenjaDateImpl _sDate;
    private final KenjaDateImpl _eDate;
    private final String _str;

    /*
     * コンストラクタ。
     */
    private SemesterGrade(
            final Category category,
            final Semester semester,
            final Grade grade,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        super(category, semester.getCode() + "-" + grade.getCode());

        _semester = semester;
        _grade = grade;
        _sDate = sDate;
        _eDate = eDate;
        _str = _semester.getCode() + ":" + _grade + "[" + _sDate + "," + _eDate + "]";
    }

    /**
     * 学期を得る。
     * @return 学期
     */
    public Semester getSemester() {
        checkAlive();
        return _semester;
    }

    /**
     * 学年を得る。
     * @return 学年
     */
    public Grade getGrade() {
        checkAlive();
        return _grade;
    }

    /**
     * 学期(学年ごと)開始日付を得る。
     * @return 学期(学年ごと)開始日付
     */
    public KenjaDateImpl getSDate() {
        checkAlive();
        return _sDate;
    }

    /**
     * 学期(学年ごと)終了日付を得る。
     * @return 学期(学年ごと)終了日付
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
        if (null == getSDate() && null == getEDate()) {
            return false;
        }
        if (null != getSDate() && getSDate().compareTo(date) > 0) {
            // sDate > date なら、範囲外
            return false;
        }
        if (null != getEDate() && date.compareTo(getEDate()) > 0) {
            // date > eDate なら、範囲外
            return false;
        }
        return true;
    }

    /**
     * 学期(学年ごと)
     * @param category カテゴリー
     * @param code 学期コード
     * @param gradeCode 学年
     * @param sDate 学期開始日付
     * @param eDate 学期終了日付
     * @return 学期
     */
    public static SemesterGrade create(
            final Category category,
            final int code,
            final String gradeCode,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (0 > code)          { throw new IllegalArgumentException("引数が不正(code)"); }
        if (null == gradeCode)  { throw new IllegalArgumentException("引数が不正(gradeCode)"); }

        final Semester semester = Semester.getInstance(category, code);
        if (null == semester)  { throw new IllegalArgumentException("引数が不正(code)"); }
        final Grade grade = Grade.create(category, gradeCode);
        final SemesterGrade found = getInstance(category, code, grade);
        if (null != found) {
            return found;
        }
        return new SemesterGrade(category, semester, grade, sDate, eDate);
    }

    /**
     * 学期コードから、学期を得る。
     * @param category カテゴリー
     * @param code 学期コード
     * @param grade 学年
     * @return 学期
     */
    public static SemesterGrade getInstance(
            final Category category,
            final int code,
            final Grade grade
    ) {
        return getEnum(category, MYCLASS, String.valueOf(code) + "-" + grade.getCode());
    }

    /**
     * 学期コードから、学期のリストを得る。
     * @param category カテゴリー
     * @param code 学期コード
     * @return 学期のリスト
     */
    public static List<SemesterGrade> getEnumList(
            final Category category,
            final int code
    ) {
        final List<SemesterGrade> enumList = getEnumList(category);
        final List<SemesterGrade> rtn = new ArrayList<SemesterGrade>();
        for (final SemesterGrade semesterGrade : enumList) {
            if (semesterGrade.getSemester().getCode() == code) {
                rtn.add(semesterGrade);
            }
        }
        return rtn;
    }

    /**
     * 学期の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Semester&gt;</code>
     */
    public static List<SemesterGrade> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 学期の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;学期コード, Semester&gt;</code>
     */
    public static Map<String, SemesterGrade> getEnumMap(final Category category) {
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
