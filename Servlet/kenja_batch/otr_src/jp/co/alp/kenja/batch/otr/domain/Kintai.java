// kanji=漢字
/*
 * $Id: Kintai.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2006/12/30 11:50:02 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import jp.co.alp.kenja.batch.otr.BatchTime;

/**
 * 勤怠コードクラス
 * @version $Id: Kintai.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Kintai {

    /** 勤怠コード: 出席 */
    private static final Kintai SEATED = new Kintai(new Integer(0), "出席");

    /** 勤怠コード: 事故欠(無) */
    private static final Kintai INPUT_NONOTICE = new Kintai(new Integer(106), "事故欠(無)");

    /** 勤怠コード: 事故欠(無) (カード入力無) */
    private static final Kintai NONOTICE = new Kintai(new Integer(6), "事故欠(無) (カード入力無)");

    /** 勤怠コード: 遅刻 */
    private static final Kintai LATE = new Kintai(new Integer(15), "遅刻");

    /** 勤怠コード: 早退 */
    private static final Kintai EARLY = new Kintai(new Integer(16), "早退");

    private final Integer _code;
    private final String _remark;

    /**
     * コンストラクタ
     * @param code 勤怠コード
     * @param remark コメント文字列
     */
    private Kintai(final Integer code, final String remark) {
        _code = code;
        _remark = remark;
    }

    /**
     * デフォルト勤怠(事故欠(無))を得る
     * @return デフォルト勤怠
     */
    public static Kintai getDefault() {
        return NONOTICE;
    }

    /**
     * 勤怠出席を得る
     * @return 勤怠出席
     */
    public static Kintai getSeated() {
        return SEATED;
    }

    /**
     * 勤怠コードを得る
     * @return 勤怠コード
     */
    public Integer getCode() {
        return _code;
    }

    /**
     * DBに出力する勤怠コードを得る
     * @return DBに出力する勤怠コード
     */
    public Integer getResultCode() {
        return (this == INPUT_NONOTICE) ? NONOTICE.getCode() : getCode();
    }

    /**
     * 出席か
     * @return 出席ならtrue、そうでなければfalse
     */
    public boolean isSeated() {
        return this.equals(SEATED);
    }

    /**
     * 事故欠(無)か
     * @return 事故欠(無)ならtrue、そうでなければfalse
     */
    public boolean isNonotice() {
        return this.equals(NONOTICE);
    }

    /**
     * 勤怠を判定する
     * @param rec 生徒の打刻時間
     * @param begin 校時の開始時間
     * @param teacherRec 講座の先生の打刻時間
     * @return 勤怠
     */
    public static Kintai getKintai(
            final BatchTime rec,
            final BatchTime begin,
            final BatchTime teacherRec) {
        if (rec == null) {
            return NONOTICE;
        }
        final BatchTime lateLine = begin.add(0, 15); // 遅刻/欠席判定時間は校時の開始時間から15分後

        if (rec.isBefore(begin, true)) {
            // 授業の開始時間より前に打刻した
            return SEATED;
        } else if (teacherRec == null) {
            // 先生が打刻していない
            return rec.isBefore(lateLine, true) ? LATE : INPUT_NONOTICE;
        } else {
            // 先生が遅刻/欠席判定時間より前に打刻し、かつ生徒が先生の打刻時間より前に打刻した
            if (teacherRec.isBefore(lateLine, true) && rec.isBefore(teacherRec, true)) {
                return SEATED;
            } else {
                // 先生が遅刻/欠席時間より前に打刻し、かつ生徒が先生の打刻時間より後に打刻した
                // または先生が遅刻/欠席時間より後に打刻した
                return rec.isBefore(lateLine, true) ? LATE : INPUT_NONOTICE;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String rescode = (_code.equals(getResultCode()) ? "" : "(" + getResultCode() +  ")" );
        return "勤怠コード=[" + _code + rescode + "] 名称=[" + _remark + "]";
    }
} // KintaiManager

// eof
