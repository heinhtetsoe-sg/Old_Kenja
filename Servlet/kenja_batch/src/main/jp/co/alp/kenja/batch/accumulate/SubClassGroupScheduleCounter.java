/*
 * $Id: SubClassGroupScheduleCounter.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/07/31
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 特別科目グループ毎の講座カウンタ。
 * @author maesiro
 * @version $Id: SubClassGroupScheduleCounter.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class SubClassGroupScheduleCounter {

    private static final Log log = LogFactory.getLog(SubClassGroupScheduleCounter.class);

    private final SubClassScheduleCounter _subclassScheduleCounter;
    private final Map<String, SubclassAbsenceHighSpecial> _subclassAbsenceHighSpecial;

    /**
     * コンストラクタ
     * @param subClassScheduleCounter 科目毎の講座カウンタ
     */
    public SubClassGroupScheduleCounter(final SubClassScheduleCounter subClassScheduleCounter) {
        _subclassScheduleCounter = subClassScheduleCounter;
        _subclassAbsenceHighSpecial = new HashMap<String, SubclassAbsenceHighSpecial>();
    }

    /**
     * 指定科目グループコードの欠課上限値を取得する。
     * @param specialGroupCd 科目グループコード
     * @return 指定科目グループコードの欠課上限値
     */
    public SubclassAbsenceHighSpecial getSubclassAbsenceHighSpecial(final String specialGroupCd) {
        if (_subclassAbsenceHighSpecial.get(specialGroupCd) == null) {
            _subclassAbsenceHighSpecial.put(specialGroupCd, new SubclassAbsenceHighSpecial(specialGroupCd));
        }
        return _subclassAbsenceHighSpecial.get(specialGroupCd);
    }

    /**
     * 科目を得る。
     * @return 科目
     */
    public Collection<String> getSubClassGroupCodes() {
        return _subclassAbsenceHighSpecial.keySet();
    }

    /**
     * データが空か
     * @return データが空ならtrue、そうでなければfalse
     */
    public boolean isEmpty() {
        return _subclassAbsenceHighSpecial.size() == 0;
    }

    /**
     * 科目毎の講座カウンタを得る。
     * @return 科目毎の講座カウンタ
     */
    public SubClassScheduleCounter getSubClassScheduleCounter() {
        return _subclassScheduleCounter;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" 生徒=[" + _subclassScheduleCounter.getStudent() + "]");
        for (final String specialGroupCd : getSubClassGroupCodes()) {
            stb.append("[" + specialGroupCd + " " + getSubclassAbsenceHighSpecial(specialGroupCd).getLessonMinutes() + " (min)]");
        }
        return stb.toString();
    }
}
