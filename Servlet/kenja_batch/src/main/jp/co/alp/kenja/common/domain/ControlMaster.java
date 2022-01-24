// kanji=漢字
/*
 * $Id: ControlMaster.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/21 18:58:46 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * コントロール・マスタ。
 * @author tamura
 * @version $Id: ControlMaster.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class ControlMaster {
    /** 科目名 */
    public static final String DISPLAY_SUBCLASS = "1";
    /** 講座名 */
    public static final String DISPLAY_CHAIR    = "2";

    private final MyEnum.Category          _category;
    /** 現在処理年度 */
    private final int               _currentYear;
    private final String            _currentYearStr;

    /** 現在処理学期 */
    private final int               _currentSemester;

    /** 現在処理日付 */
    private KenjaDateImpl           _currentDate;

    /** 出欠制御日付 */
    private final KenjaDateImpl     _attendCtrlDate;

    /** 科目名か講座名かの区分 */
    private final String            _subClassOrChair;

    /** 出欠期間 */
    private final String            _attendTerm;

    /**
     * コンストラクタ。
     * @param category カテゴリー
     * @param currentYear 現在処理年度
     * @param currentSemester 現在処理学期
     * @param currentDate 現在処理日付
     * @param attendCtrlDate 出欠制御日付
     * @param subClassOrChair 科目名か講座名かの区分
     * @param attendTerm 出欠期間
     */
    public ControlMaster(
            final MyEnum.Category category,
            final int currentYear,
            final int currentSemester,
            final KenjaDateImpl currentDate,
            final KenjaDateImpl attendCtrlDate,
            final String subClassOrChair,
            final String attendTerm
    ) {
        if (0 >= currentYear || 0 >= currentSemester) {
            throw new IllegalArgumentException("引数が不正");
        }
        if (null == subClassOrChair) {
            throw new IllegalArgumentException("引数が不正");
        }
//        if ((!subClassOrChair.equals(DISPLAY_SUBCLASS)) && (!subClassOrChair.equals(DISPLAY_CHAIR))) {
//            throw new IllegalArgumentException("引数が不正");
//        }

        _category = category;
        _currentYear = currentYear;
        _currentYearStr = String.valueOf(_currentYear);
        _currentSemester = currentSemester;
        _currentDate = currentDate;
        _attendCtrlDate = (KenjaDateImpl) ObjectUtils.defaultIfNull(attendCtrlDate, _currentDate);
        if (subClassOrChair.equals(DISPLAY_SUBCLASS) || subClassOrChair.equals(DISPLAY_CHAIR)) {
            _subClassOrChair = subClassOrChair;
        } else {
            _subClassOrChair = DISPLAY_SUBCLASS;
        }
        _attendTerm = attendTerm;
    }

    /**
     * コンストラクタ。
     * @param category カテゴリー
     * @param currentYear 現在処理年度
     * @param currentSemester 現在処理学期
     * @param currentDate 現在処理日付
     * @param subClassOrChair 科目名か講座名かの区分
     * @param attendTerm 出欠期間
     */
    public ControlMaster(
            final MyEnum.Category category,
            final int currentYear,
            final int currentSemester,
            final KenjaDateImpl currentDate,
            final String subClassOrChair,
            final String attendTerm
    ) {
        this(category, currentYear, currentSemester, currentDate, null, subClassOrChair, attendTerm);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof ControlMaster)) { return false; }

        final ControlMaster that = (ControlMaster) obj;

    equals:
        {
            final EqualsBuilder eb = new EqualsBuilder();
            eb.append(_currentYear, that._currentYear);
            eb.append(_currentSemester, that._currentSemester);
            eb.append(_currentDate, that._currentDate);
            eb.append(_attendCtrlDate, that._attendCtrlDate);
            return eb.isEquals();
        }
    }

    /**
     * ハッシュコードを返します。
     * {@inheritDoc}
     */
    public int hashCode() { return toString().hashCode(); }

    /**
     * 文字列に変換する。
     * {@inheritDoc}
     */
    public String toString() {
        final Semester sem = Semester.getInstance(_category, _currentSemester);
        final StringBuffer sb = new StringBuffer(32);
        sb.append('[');
        sb.append(_currentYear).append("年度,");
        if (null == sem) {
            sb.append("学期コード=").append(_currentSemester).append(',');
        } else {
            sb.append(sem).append(',');
        }
        sb.append(_currentDate).append(',');
        sb.append("出欠制御日付=").append(_attendCtrlDate);
        sb.append(']');
        return sb.toString();
    }

    /**
     * カテゴリーを得る。
     * @return カテゴリー
     */
    public MyEnum.Category getCategory() { return _category; }

    /**
     * 現在処理年度を得る。
     * @return 現在処理年度
     */
    public int getCurrentYear() { return _currentYear; }

    /**
     * 現在処理年度を得る。
     * @return 現在処理年度
     */
    public String getCurrentYearAsString() { return _currentYearStr; }

    /**
     * 現在処理学期を得る。
     * @return 現在処理学期
     */
    public Semester getCurrentSemester() { return Semester.getInstance(_category, _currentSemester); }

    /**
     * 現在処理学期(学年ごと)を得る。
     * @return 現在処理学期(学年ごと)のリスト
     */
    public List<SemesterGrade> getCurrentSemesterGradeList() { return SemesterGrade.getEnumList(_category, _currentSemester); }

    /**
     * 学年ごとを考慮した現在処理学期の開始日付を得る。
     * @return 学年ごとを考慮した現在処理学期の開始日付
     */
    public KenjaDateImpl getCurrentSemesterSdate(final Grade grade) {
        final SemesterGrade semesterGrade = SemesterGrade.getInstance(_category, _currentSemester, grade);
        if (null != semesterGrade && null != semesterGrade.getSDate()) {
            return semesterGrade.getSDate();
        }
        return getCurrentSemester().getSDate();
    }

    /**
     * 学年ごとを考慮した現在処理学期の終了日付を得る。
     * @return 学年ごとを考慮した現在処理学期の終了日付
     */
    public KenjaDateImpl getCurrentSemesterEdate(final Grade grade) {
        final SemesterGrade semesterGrade = SemesterGrade.getInstance(_category, _currentSemester, grade);
        if (null != semesterGrade && null != semesterGrade.getEDate()) {
            return semesterGrade.getEDate();
        }
        return getCurrentSemester().getEDate();
    }

    /**
     * 現在処理日付を得る。
     * @return 現在処理日付
     */
    public KenjaDateImpl getCurrentDate() { return _currentDate; }

    /**
     * 出欠制御日付を得る。
     * @return 出欠制御日付
     */
    public KenjaDateImpl getAttendCtrlDate() { return _attendCtrlDate; }

    /**
     * 科目を表示するか（講座を表示するか）判定する。
     * @return 科目を表示するなら<code>true</code>を返す
     */
    public boolean isUsingSubClass() {
        return DISPLAY_SUBCLASS.equals(_subClassOrChair);
    }

    /**
     * 入力期間の最大日付を得る。
     * @return 入力期間の最大日付。入力期間の設定がなければnull
     */
    public KenjaDateImpl getAttendTermDates() {
        if (!NumberUtils.isNumber(_attendTerm)) {
            return null;
        }
        final int term = Double.valueOf(_attendTerm).intValue();
        final KenjaDateImpl attendTermlimitDate = _currentDate.add(term);
        return attendTermlimitDate;
    }

    /**
     * 出欠日付が入力期間を超えているか。
     * @param 出欠日付
     * @return 出欠日付が入力期間を超えているなら<code>true</code>、それ以外は<code>false</code>
     */
    public boolean isAttendTermLimitDateOver(final KenjaDate attendDate) {
        final KenjaDate attendTermLimitDate = getAttendTermDates();
        if (null == attendTermLimitDate) {
            return false;
        }
        return attendTermLimitDate.compareTo(attendDate) < 0;
    }

    /**
     * 現在処理日付が未設定の場合に限り、現在処理日付を設定する。
     * @param date 日付
     */
    public void setCurrentDate(final KenjaDateImpl date) {
        if (null == _currentDate) {
            _currentDate = date;
        }
    }

    // info
    //------------------------------------------------------------------------

    /**
     * 講座infoまたは科目infoを得る。
     * @param chair 講座
     * @return 講座infoまたは科目info
     */
    public String getInfoNameChair(final Chair chair) {
        if (isUsingSubClass()) {
            return chair.getSubClass().getAbbr();
        } else {
            return chair.getName();
        }
    }
} // ControlMaster

// eof
