// kanji=漢字
/*
 * $Id: Term.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/09/22 16:08:41 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.domain;

import java.util.Iterator;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;


//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 日付の期間。
 * @author takaesu
 * @version $Id: Term.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class Term implements Iterable<KenjaDateImpl> {
    private final KenjaDateImpl _sDate;
    private final KenjaDateImpl _eDate;

    /**
     * コンストラクタ。
     * @param date1 日付
     * @param date2 日付
     */
    public Term(final KenjaDateImpl date1, final KenjaDateImpl date2) {
        if (null == date1 && null == date2) {
            throw new IllegalArgumentException("引数が 両方null");
        }

        if (null == date1) {
            _sDate = date2;
            _eDate = KenjaDateImpl.getInstance(9999, 12, 31);
        } else if (null == date2) {
            _sDate = date1;
            _eDate = KenjaDateImpl.getInstance(9999, 12, 31);
        } else {
            _eDate = date1.max(date2);
            _sDate = DateUtils.min(date1, date2);
        }
    }

    /**
     * 開始日を得る。
     * @return 開始日
     */
    public KenjaDateImpl getSDate() { return _sDate; }

    /**
     * 終了日を得る。
     * @return 終了日
     */
    public KenjaDateImpl getEDate() { return _eDate; }

    /**
     * イテレータを得る。
     * @return イテレータ
     */
    public Iterator<KenjaDateImpl> iterator() {
        return new MyIterator(this);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "[" + _sDate + " - " + _eDate + "]";
    }

    // ==================================

    /**
     * イテレータ。
     */
    private class MyIterator implements Iterator<KenjaDateImpl> {
        private final KenjaDateImpl _sDate;
        private final KenjaDateImpl _eDate;
        private KenjaDateImpl _idx;

        public MyIterator(final Term term) {
            _sDate = term.getSDate();
            _eDate = term.getEDate();
            _idx = _sDate;
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return _idx.compareTo(_eDate) <= 0;
        }

        /**
         * {@inheritDoc}
         */
        public KenjaDateImpl next() {
            final KenjaDateImpl rtn = _idx;
            _idx = rtn.nextDate();
            return rtn;
        }
    }
} // Term

// eof
