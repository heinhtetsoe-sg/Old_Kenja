// kanji=漢字
/*
 * $Id: ExecuteUnit.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2007/02/08 15:54:38 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate.option;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.NameMaster;
import jp.co.alp.kenja.batch.domain.DateUtils;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;

/**
 * 処理単位。
 * @author takaesu
 * @version $Id: ExecuteUnit.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public final class ExecuteUnit {
    /*pkg*/static final Log log = LogFactory.getLog(ExecuteUnit.class);

    private final boolean _isMonthMode;

    private KenjaDateImpl _lowerDate;   // 最小集計日
    private KenjaDateImpl _upperDate;   // 最大集計日(処理日)

    private BaseDay _baseDay;
    private Semester _semester;

    /**
     * コンストラクタ。
     * @param mode "semester" or "month"
     */
    public ExecuteUnit(final String mode) {
        if (null != mode && "semester".equals(mode.toLowerCase())) {
            _isMonthMode = false;
        } else {
            _isMonthMode = true;
        }
    }

    /**
     * 初期化。
     * @param baseDay 基準日
     * @param date 日付
     * @param semester 学期
     */
    public void init(final BaseDay baseDay, final KenjaDateImpl date, final Semester semester) {
        _baseDay = baseDay;

        _upperDate = date;  // 集計の最終日＝指定日

        final KenjaDateImpl sdate = semester.getSDate();
        final KenjaDateImpl edate = semester.getEDate();
        if (sdate.compareTo(date) > 0) {
            throw new IllegalArgumentException("学期の開始日(" + sdate + ")より過去: " + date);
        }
        if (edate.compareTo(date) < 0) {
            throw new IllegalArgumentException("学期の終了日(" + edate + ")より未来: " + date);
        }
        _semester = semester;

        if (DateUtils.isSameYearMonth(sdate, date) || _isMonthMode) {
            final KenjaDateImpl aDate = _baseDay.getStartDate(date);
            _lowerDate = aDate.max(sdate);
        } else {
            _lowerDate = sdate;
        }
    }
    
    private boolean isYearSemesterMonthDisabledByC040(final Term term, final Semester semester) {
        final Collection<Map<String, String>> c040RecordList = NameMaster.getInstance().get("C040");
        if (c040RecordList.isEmpty()) {
            return false;
        }
        final KenjaDateImpl edate = term.getEDate();
        final DecimalFormat df2 = new DecimalFormat("00");
        final String name1 = String.valueOf(edate.getNendo());
        final String name2 = semester.getCodeAsString();
        final String name3 = df2.format(edate.getMonth());

        for (final Map<String, String> row : c040RecordList) {
            if (name1.equals(NameMaster.getValue(row, "NAME1")) &&
                name2.equals(NameMaster.getValue(row, "NAME2")) &&
                name3.equals(NameMaster.getValue(row, "NAME3"))) {
                log.info("C040 設定により対象外: 期間 = " + term + "、学期 = " + semester);
                return true;
            }
        }
        return false;
    }

    /**
     * 月単位で区切られた期間の<code>List</code>を返す。<br>
     * 月単位の場合は List#size() は１。
     * @return 月単位で区切られた期間の<code>List</code>
     */
    public List<Term> getTermList() {
        final List<Term> termList = new ArrayList<Term>();

        if (_isMonthMode) {
            final Term term = new Term(_lowerDate, _upperDate);
            if (!isYearSemesterMonthDisabledByC040(term, _semester)) {
                termList.add(term);
            }
        } else {
            final List<KenjaDateImpl> dateList = DateUtils.getDateList(_lowerDate, _upperDate); 
            
            final List<Term> wrk = new ArrayList<Term>();
            for (final KenjaDateImpl date : dateList) {
                final Term term0 = _baseDay.getTerm(date);
                final KenjaDateImpl termSdate = (term0.getSDate().getMonth() == _semester.getSDate().getMonth()) ? _semester.getSDate() : term0.getSDate();
                final KenjaDateImpl termEdate = DateUtils.min(_semester.getEDate(), term0.getEDate());
                final Term term = new Term(termSdate, termEdate);
                wrk.add(term);
            }
            
            for (final Term term0 : wrk) {
                final KenjaDateImpl date1 = _lowerDate.max(term0.getSDate());
                final KenjaDateImpl date2 = DateUtils.min(_upperDate, term0.getEDate());
                final Term term = new Term(date1, date2);
                if (0 < term.getSDate().compareTo(_upperDate) || 0 < term.getEDate().compareTo(_upperDate)) {
                    continue;
                }
                if (!isYearSemesterMonthDisabledByC040(term, _semester)) {
                    termList.add(term);
                }
            }
        }

        return termList;
    }

    /**
     * 最小集計日を得る。
     * @return 最小集計日
     */
    public KenjaDateImpl getLowerDate() {
        return _lowerDate;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final String msg = _isMonthMode ? "月単位" : "学期単位";
        return msg + ", " + _lowerDate + " 〜 " + _upperDate;
    }
} // ExecuteUnit

// eof
