/*
 * $Id: AttendSubclassSpecialDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/10/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jp.co.alp.kenja.common.domain.SubClass;

/**
 * 特別科目グループデータ。
 * @author maesiro
 * @version $Id: AttendSubclassSpecialDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class AttendSubclassSpecialDat {

    private static Map<String, AttendSubclassSpecialDat> attendSubclassSpecialData_ = new HashMap<String, AttendSubclassSpecialDat>();

    private final String _specialGroupCd;
    private final Map<SubClass, Integer> _subClasses;

    /**
     * 特別科目グループデータ。
     * @param specialGroupCd 特別科目グループコード
     */
    private AttendSubclassSpecialDat(
            final String specialGroupCd
    ) {
        _specialGroupCd = specialGroupCd;
        _subClasses = new HashMap<SubClass, Integer>();
    }

    /**
     * 特別科目グループデータのマップ。
     * @return 特別科目グループデータのマップ
     */
    public static Map<String, AttendSubclassSpecialDat> getAttendSubclassSpecialData() {
        return Collections.unmodifiableMap(attendSubclassSpecialData_);
    }

    /**
     * 特別活動グループコードに対応する特別科目グループデータを得る。
     * @param specialGroupCd 特別科目グループコード
     * @return 特別科目グループデータ
     */
    public static AttendSubclassSpecialDat getAttendSubclassSpecialDat(final String specialGroupCd) {
        if (!attendSubclassSpecialData_.containsKey(specialGroupCd)) {
            attendSubclassSpecialData_.put(specialGroupCd, new AttendSubclassSpecialDat(specialGroupCd));
        }
        return attendSubclassSpecialData_.get(specialGroupCd);
    }

    /**
     * 科目とその講座の授業分数の設定を追加する
     * @param subClass 科目
     * @param minutes 講座の授業分数
     */
    public void put(final SubClass subClass, final Integer minutes) {
        _subClasses.put(subClass, minutes);
    }

    /**
     * 科目の1時限あたりの時分
     * @param subClass 科目
     * @return 1時限あたりの授業時分
     */
    public int getMinutes(final SubClass subClass) {
        return contains(subClass) ? _subClasses.get(subClass) : 0;
    }

    /**
     * 科目を含んでいるか
     * @param subClass 科目
     * @return 科目を含んでいるか
     */
    public boolean contains(final SubClass subClass) {
        return _subClasses.containsKey(subClass);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "特別科目データ: 特別科目グループコード = " + _specialGroupCd + " , " + _subClasses.toString();
    }

}
