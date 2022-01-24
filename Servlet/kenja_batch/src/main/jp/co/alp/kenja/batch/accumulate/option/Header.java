// kanji=漢字
/*
 * $Id: Header.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2007/01/22 14:35:39 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate.option;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 累積テーブルの1レコードに相当する。
 * また、そのレコードの集計範囲を持つ。
 * @author takaesu
 * @version $Id: Header.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class Header {
    /*pkg*/static final Log log = LogFactory.getLog(Header.class);

    private final Term _term;
    private final Integer _nendo;
    private final KenjaParameters _params;
    private final String _monthAsString;
    private final String _dayAsString;

    /**
     * コンストラクタ。
     * @param term 期間
     * @param nendo 年度
     * @param staffcd 登録者コード
     */
    public Header(
            final Term term,
            final int nendo,
            final KenjaParameters parameters
    ) {
        _term = term;
        _nendo = new Integer(nendo);
        _params = parameters;

        final int month = new Integer(term.getSDate().getMonth()).intValue();
        _monthAsString = (month <= 9) ? "0" + String.valueOf(month) : String.valueOf(month);
        final int day = new Integer(term.getEDate().getDay()).intValue();
        _dayAsString = (day <= 9) ? "0" + String.valueOf(day) : String.valueOf(day);
    }

    /**
     * 期間を得る。
     * @return 期間
     */
    public Term getTerm() {
        return _term;
    }
    /**
     * 年度を得る。
     * @return 年度
     */
    public Integer getNendo() {
        return _nendo;
    }
    /**
     * 登録者コードを得る。
     * @return 登録者コード
     */
    public String getStaffcd() {
        return _params.getStaffCd();
    }

    /**
     * 月の文字列を得る。<br>
     * 月が1桁の場合、頭に"0"を付加する。
     * @return 月の文字列
     */
    public String getMonthAsString() {
        return _monthAsString;
    }

    /**
     * 月の文字列を得る。<br>
     * 月が1桁の場合、頭に"0"を付加する。
     * @return 月の文字列
     */
    public String getDayAsString() {
        return _dayAsString;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _term + ", " + _nendo;
    }
} // Header

// eof
