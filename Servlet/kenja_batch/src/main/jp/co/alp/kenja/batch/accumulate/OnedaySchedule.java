// kanji=漢字
/*
 * $Id: OnedaySchedule.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 11:22:16 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;

/**
 * 一日の出欠。
 * 出欠から、時間割や勤怠を得る事ができる。
 * @author takaesu
 * @version $Id: OnedaySchedule.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class OnedaySchedule {
    /*pkg*/static final Log log = LogFactory.getLog(OnedaySchedule.class);

    protected final Student _student;
    protected final KenjaDateImpl _date;
    protected final List<Kintai> _kintaiList;

    /**
     * コンストラクタ。
     * @param coll 出欠の<code>Collection</code>
     */
    public OnedaySchedule(final Student student, final KenjaDateImpl date, final Collection<Attendance> coll) {
        _student = student;
        _date = date;
        _kintaiList = new ArrayList<Kintai>();
        for (final Attendance attendance : coll) {
            _kintaiList.add(attendance.getKintai());
        }
    }

    public Student getStudent() {
        return _student;
    }

    /**
     * 先頭の勤怠を得る。
     * @return 先頭の勤怠
     */
    public Kintai getFirstKintai() {
        final Iterator<Kintai> it = _kintaiList.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    /**
     * 最後の勤怠を得る。
     * @return 最後の勤怠
     */
    public Kintai getLastKintai() {
        final ListIterator<Kintai> lit = _kintaiList.listIterator(_kintaiList.size());
        if (lit.hasPrevious()) {
            return lit.previous();
        }
        return null;
    }

    /**
     * すべてその勤怠か？
     * @param kintai 勤怠
     * @return すべてその勤怠ならtrue
     */
    public boolean isAll(final Kintai kintai) {
        if (null == kintai) {
            return false;
        }
        return isAll(_kintaiList, Collections.singletonList(kintai.getAltKintai()));
    }

    /**
     * すべて勤怠リストに含まれるか？
     * @param kintaiList 勤怠リスト
     * @return すべて勤怠リストに含まれるならtrue
     */
    public boolean isAll(final List<Kintai> kintaiList) {
        return isAll(_kintaiList, kintaiList);
    }

    private static boolean isAll(final List<Kintai> targetList, final List<Kintai> list) {
        if (targetList.isEmpty() || list.isEmpty()) {
            return false;
        }
        for (final Kintai kintai : targetList) {
            if (null == kintai || !list.contains(kintai.getAltKintai())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 勤怠のリストに含まれる最初の勤怠を得る。
     * @param kintaiList 勤怠のリスト
     * @return 勤怠のリストに含まれる最初の勤怠
     */
    public Kintai getFirstKintaiIn(final List<Kintai> kintaiList) {
        for (final Kintai kintai : _kintaiList) {
            if (kintaiList.contains(kintai)) {
                return kintai;
            }
        }
        return null;
    }

    /**
     * 勤怠リストを得る。
     * @return 勤怠リスト
     */
    public List<Kintai> getAltKintaiList() {
        final List<Kintai> rtn = new ArrayList<Kintai>();
        for (final Kintai kintai : _kintaiList) {
            final Kintai alt = (null == kintai ? kintai :  kintai.getAltKintai());
            rtn.add(alt);
        }
        return rtn;
    }

    /**
     * 「欠課」か？
     * @param kintai 勤怠
     * @return 欠課ならtrue
     */
    private boolean isKekka(final Kintai kintai) {
        if (null == kintai) {
            return false;
        }
        final int code = kintai.getAltCode().intValue();

        switch (code) {
            case KintaiManager.CODE_SICK:
            case KintaiManager.CODE_NOTICE:
            case KintaiManager.CODE_NONOTICE:
            case KintaiManager.CODE_NURSEOFF:
                return true;
            default:
                return false;
        }
    }

    /**
     * その勤怠を含んでいるか？
     * @param kintai 勤怠
     * @return 含んでいればtrue
     */
    public boolean contains(final Kintai kintai) {
        if (null == kintai) {
            return false;
        }
        return getAltKintaiList().contains(kintai.getAltKintai());
    }

    /**
     * 単コマ遅刻回数を得る。
     * @return 単コマ遅刻回数
     */
    public List<Kintai> getLateDetail() {
        List<Kintai> lateDetail = new ArrayList<Kintai>();
        for (final Kintai kintai : _kintaiList) {
            if (KintaiManager.isLate(kintai)
                    || KintaiManager.isLateNonotice(kintai)
                    || KintaiManager.isLate2(kintai)
                    || KintaiManager.isLate3(kintai)) {
                for (int i = 0, c = AccumulateKintaiInfo.getInstance().getCount(kintai); i < c; i++) {
                    lateDetail.add(kintai);
                }
            }
        }
        return lateDetail;
    }

    /**
     * 欠課回数を得る。
     * @return 欠課回数
     */
    public List<Kintai> getKekka() {

        boolean wasKekka = false;
        List<Kintai> kekka = new ArrayList<Kintai>();

        for (final Kintai kintai : _kintaiList) {
            final boolean isKekka = isKekka(kintai);
            if (!wasKekka && isKekka) {
                kekka.add(kintai);
            }
            wasKekka = isKekka;
        }
        return kekka;
    }

    /**
     * 欠課時数を得る。
     * @return 欠課回数
     */
    public List<Kintai> getKekkaJisu() {
        List<Kintai> kekka = new ArrayList<Kintai>();

        for (final Kintai kintai : _kintaiList) {
            if (isKekka(kintai)) {
                kekka.add(kintai);
            }
        }
        return kekka;
    }

    /**
     * 勤怠のリストを得る。
     * @return 勤怠のリスト
     */
    public List<Kintai> kintaiList() {
        return Collections.unmodifiableList(_kintaiList);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _kintaiList.toString();
    }
} // OnedaySchedule

// eof
