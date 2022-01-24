// kanji=漢字
/*
 * $Id: UsualSchedule.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/06/16 21:19:18 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 通常時間割。
 * @author tamura
 * @version $Id: UsualSchedule.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class UsualSchedule extends Schedule {
    /** 日付/校時/年組/科目/講座でソートする */
//    public static final Comparator<UsualSchedule> SORTER = new MyComparator();
    /*pkg*/static final Log log = LogFactory.getLog(UsualSchedule.class);

    private final RollCalledDiv _rollCalledDiv;

    private final DataDiv _dataDiv;
    
    private ExamItem _examItem;

    /**
     * コンストラクタ。
     * @param dirty 時間割の状態
     * @param date 日付
     * @param period 校時
     * @param chair 講座
     * @param rollCalledDiv 出欠実施区分
     * @param dataDiv データ区分
     * @param doInitCountFlags 年組毎の集計フラグを講座の集計フラグで初期化するか否か
     * @param lessonMode 授業形態
     */
    public UsualSchedule(
            final KenjaDateImpl date,
            final Period period,
            final Chair chair,
            final RollCalledDiv rollCalledDiv,
            final DataDiv dataDiv
    ) {
        super(date, period, chair, DataDiv.EXAM != dataDiv, false);
        _rollCalledDiv = rollCalledDiv;
        _dataDiv = dataDiv;
    }

    private boolean isExam() {
        return DataDiv.EXAM == getDataDiv();
    }

    /**
     * SQL-Dateを得る。
     * @return SQLDateのインスタンス
     */
    public java.sql.Date getSQLDate() {
        return ((KenjaDateImpl) getDate()).getSQLDate();
    }

    /**
     * 指定した年組の集計フラグを得る。
     * @param homeRoom 年組
     * @return 集計フラグ
     */
    public boolean getCountFlag(final HomeRoom homeRoom) {
        if (null == homeRoom) {
            throw new NullPointerException("homeRoomがnull");
        }
        if (isExam()) {
            return getExamItem().getCountFlag();
        }

        return super.getCountFlag(homeRoom);
    }

    /**
     * 出欠（点呼）実施区分を得る。
     * @return 出欠（点呼）実施区分。
     */
    public RollCalledDiv getRollCalledDiv() { return _rollCalledDiv; }

    /**
     * データ区分を得る。
     * @return データ区分
     */
    public DataDiv getDataDiv() { return _dataDiv; }

    /**
     * 考査項目を設定する。
     * @param examItem 考査項目
     * @throws IllegalStateException データ区分が考査以外の場合
     */
    protected final void setExamItem0(final ExamItem examItem) {
        if (isExam()) {
            _examItem = examItem;
        } else {
            throw new IllegalStateException("考査以外では設定できない");
        }
    }

    /**
     * 考査項目を設定する。
     * @param examItem 考査項目
     */
    public void setExamItem(final ExamItem examItem) {
        setExamItem0(examItem);
    }

    /**
     * 考査項目を得る。
     * @return 考査項目
     */
    public ExamItem getExamItem() {
        return _examItem;
    }

    //========================================================================

    /**
     * 出欠（点呼）実施区分。
     * テーブル名.カラム名=SCH_CHR_DAT.EXECUTED。
     */
    public static final class RollCalledDiv extends ValuedEnum {
        /** 出欠確認未実施(0:未実施) */
        public static final RollCalledDiv NOTYET = new RollCalledDiv(
                "未実施",
                "出欠未",
                0);

        /** 出欠確認実施済み(1:実施) */
        public static final RollCalledDiv FINISHED = new RollCalledDiv(
                "実施済み",
                "出欠済",
                1);

        /**
         * 出欠確認未実施(一部済み)。
         * 未実施と済みの混合。
         * getInstance(int)では、扱わない。
         * @since T105
         */
        public static final RollCalledDiv MIXED = new RollCalledDiv(
                "未実施(一部済み)",
                "出欠未(一部済み)",
                99);

        private final String _label;

        /*
         * コンストラクタ。
         */
        private RollCalledDiv(
                final String name,
                final String label,
                final int value
        ) {
            super(name, value);
            _label = label;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() { return getName(); }

        /**
         * コードを文字列で得る。
         * @return コード
         */
        public String getCode() {
            return String.valueOf(getValue());
        }

        /**
         * ラベル文字列を得る。
         * @return ラベル文字列
         */
        public String getLabel() {
            return _label;
        }

        /**
         * 値から、出欠（点呼）実施区分のインスタンスを得る。
         * ただし{@link #MIXED}は、扱わない。
         * @param value 値。0または1
         * @return 出欠（点呼）実施区分のインスタンス
         */
        public static RollCalledDiv getInstance(final int value) {
            final RollCalledDiv rtn = (RollCalledDiv) EnumUtils.getEnum(RollCalledDiv.class, value);
            if (MIXED.equals(rtn)) {
                // MIXED(混在)は、このメソッド(getInstance(int))では扱わない
                return null;
            }
            return rtn;
        }
    } // RollCalledDiv

    //========================================================================

    /**
     * データ区分（基本時間割の反映のまま/通常時間割で編集した/定期考査）。
     */
    public static final class DataDiv extends ValuedEnum {
        /** 基本時間割のまま(0:変更なし) */
        public static final DataDiv BASIC = new DataDiv("基本時間割から反映", 0);
        /** 通常時間割で編集(1:変更あり) */
        public static final DataDiv USUAL = new DataDiv("通常時間割で編集", 1);
        /** 通常時間割で編集(2:定期考査) */
        public static final DataDiv EXAM = new DataDiv("定期考査", 2);

        /*
         * コンストラクタ。
         */
        private DataDiv(
                final String name,
                final int value
        ) {
            super(name, value);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() { return getName(); }

        /**
         * コードを文字列で得る。
         * @return コード
         */
        public String getCode() {
            return String.valueOf(getValue());
        }

        /**
         * 値から、データ区分のインスタンスを得る。
         * @param value 値
         * @return データ区分のインスタンス
         */
        public static DataDiv getInstance(final int value) {
            return (DataDiv) EnumUtils.getEnum(DataDiv.class, value);
        }
    } // DataDiv

} // UsualSchedule

// eof
