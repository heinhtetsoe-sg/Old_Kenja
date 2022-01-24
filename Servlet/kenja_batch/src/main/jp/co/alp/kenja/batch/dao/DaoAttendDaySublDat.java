// kanji=漢字
/*
 * $Id: DaoAttendDaySublDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 14:59:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AccumulateAttendDayDat;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日々出欠大分類データを取得する。
 * @version $Id: DaoAttendDaySublDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoAttendDaySublDat extends AbstractDaoLoader<AccumulateAttendDayDat> {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_DAY_SUBL_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoAttendDaySublDat.class);

    private final Term _term;
    private final Map<Student, AccumulateAttendDayDat> _schregnoAttendDayDat;

    /**
     * コンストラクタ。
     * @param term 範囲
     * @param schregnoAttendDayDat 格納するマップコレクション
     */
    public DaoAttendDaySublDat(final Term term, final Map<Student, AccumulateAttendDayDat> schregnoAttendDayDat) {
        super(log);
        _term = term;
        _schregnoAttendDayDat = schregnoAttendDayDat;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        Object rtn = null;
    check:
        {
            final Student student = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "schregno"));
            if (null == student) {
                rtn = "不明な生徒(student)";
                break check;
            }

            final KenjaDateImpl date = KenjaMapUtils.getKenjaDateImpl(map, "date");
            if (null == date) {
                rtn = "不明な日付(date)";
                break check;
            }

            final Kintai kintai = Kintai.getInstance(_cm.getCategory(), MapUtils.getString(map, "kintai"));
            if (null == kintai) {
                rtn = "不明な勤怠コード(kintai)";
                break check;
            }

            final String sublCd = MapUtils.getString(map, "sublCd");

            if (null == _schregnoAttendDayDat.get(student)) {
                _schregnoAttendDayDat.put(student, new AccumulateAttendDayDat(student));
            }
            final AccumulateAttendDayDat aadd = _schregnoAttendDayDat.get(student);
            aadd.addDayDatSubl(date, kintai, sublCd);

        } // check:
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    SCHREGNO as schregno,"
                + "    ATTENDDATE as date,"
                + "    DI_CD as kintai, "
                + "    SUBL_CD as sublCd "
                + "  from " + TABLE_NAME
                + "  where"
                + "    ATTENDDATE between ? and ? "
                + "  order by"
                + "    SCHREGNO ";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            _term.getSDate().getSQLDate(),
            _term.getEDate().getSQLDate(),
        };
    }

} // DaoAttendDayDat

// eof
