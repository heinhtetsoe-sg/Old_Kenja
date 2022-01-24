// kanji=漢字
/*
 * $Id: AccumulateAttendDayDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 11:21:26 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 出欠日々保持クラス。
 * @author takaesu
 * @version $Id: AccumulateAttendDayDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AccumulateAttendDayDat implements Comparable<AccumulateAttendDayDat> {
    /*pkg*/static final Log log = LogFactory.getLog(AccumulateAttendDayDat.class);

    private final Student _student;
    private final Map<Kintai, Set<KenjaDateImpl>> _dayDat = new HashMap<Kintai, Set<KenjaDateImpl>>();
    private final Map<Kintai, Map<String, Collection<KenjaDateImpl>>> _dayDatSubl = new HashMap<Kintai, Map<String, Collection<KenjaDateImpl>>>();
    private final Map<Kintai, Map<String, Map<String, Collection<KenjaDateImpl>>>> _dayDatSubm = new HashMap<Kintai, Map<String, Map<String, Collection<KenjaDateImpl>>>>();

    /**
     * コンストラクタ。
     * @param student 生徒
     */
    public AccumulateAttendDayDat(
            final Student student
    ) {
        _student = student;
    }

    /**
     * 日々出欠を追加する。
     * @param date 日付
     * @param kintaiManager 勤怠マネージャ
     * @param kintai 勤怠
     */
    public void addDayDat(final KenjaDateImpl date, final KintaiManager kintaiManager, final Kintai kintai) {
        if (null == _dayDat.get(kintai)) {
            _dayDat.put(kintai, new HashSet<KenjaDateImpl>());
        }
        _dayDat.get(kintai).add(date);
    }

    /**
     * 大分類データを追加する。
     * @param date 日付
     * @param kintai 勤怠
     * @param sublCd 大分類コード
     */
    public void addDayDatSubl(final KenjaDateImpl date, final Kintai kintai, final String sublCd) {
        if (null == _dayDatSubl.get(kintai)) {
            _dayDatSubl.put(kintai, new HashMap<String, Collection<KenjaDateImpl>>());
        }
        final Map<String, Collection<KenjaDateImpl>> dateSublMap = _dayDatSubl.get(kintai);
        if (null == dateSublMap.get(sublCd)) {
            dateSublMap.put(sublCd, new HashSet<KenjaDateImpl>());
        }
        dateSublMap.get(sublCd).add(date);
    }

    /**
     * 中分類データを追加する。
     * @param date 日付
     * @param kintai 勤怠
     * @param sublCd 大分類コード
     * @param submCd 中分類コード
     */
    public void addDayDatSubm(final KenjaDateImpl date, final Kintai kintai, final String sublCd, final String submCd) {
        if (null == _dayDatSubm.get(kintai)) {
            _dayDatSubm.put(kintai, new HashMap<String, Map<String, Collection<KenjaDateImpl>>>());
        }
        final Map<String, Map<String, Collection<KenjaDateImpl>>> dateSublMap = _dayDatSubm.get(kintai);
        if (null == dateSublMap.get(sublCd)) {
            dateSublMap.put(sublCd, new HashMap<String, Collection<KenjaDateImpl>>());
        }
        final Map<String, Collection<KenjaDateImpl>> dateSubmMap = dateSublMap.get(sublCd);
        if (null == dateSubmMap.get(submCd)) {
            dateSubmMap.put(submCd, new HashSet<KenjaDateImpl>());
        }
        dateSubmMap.get(submCd).add(date);
    }

    /**
     * 日々出欠データのマップを得る。
     * @return 日々出欠データのマップ
     */
    public Map<Kintai, Set<KenjaDateImpl>> getDayDatMap() {
        return Collections.unmodifiableMap(_dayDat);
    }

    /**
     * 大分類データのマップを得る。
     * @return 大分類データのマップ
     */
    public Map<Kintai, Map<String, Collection<KenjaDateImpl>>> getDayDatSublMap() {
        return Collections.unmodifiableMap(_dayDatSubl);
    }

    /**
     * 中分類データのマップを得る。
     * @return 中分類データのマップ
     */
    public Map<Kintai, Map<String, Map<String, Collection<KenjaDateImpl>>>> getDayDatSubmMap() {
        return Collections.unmodifiableMap(_dayDatSubm);
    }

    /**
     * 生徒を得る。
     * @return 生徒
     */
    public Student getStudent() {
        return _student;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final AccumulateAttendDayDat that) {

        int rtn = 0;

        rtn = getStudent().compareTo(that.getStudent());
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "[" + _dayDat + ":" + _dayDatSubl + ":" + _dayDatSubm + "]";
    }
} // AccumulateAttendance

// eof
