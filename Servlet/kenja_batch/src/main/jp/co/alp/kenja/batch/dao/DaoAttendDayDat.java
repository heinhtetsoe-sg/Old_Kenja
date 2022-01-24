// kanji=漢字
/*
 * $Id: DaoAttendDayDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 14:59:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AccumulateAttendDayDat;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.common.dao.SQLUtils;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Kintai;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日々出欠を取得する。
 * @version $Id: DaoAttendDayDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoAttendDayDat extends AbstractDaoLoader<AccumulateAttendDayDat> {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_DAY_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoAttendDayDat.class);

    private final Header _header;
    private final KintaiManager _kintaiManager;
    private final String[] _schoolKind;
    private final Map<Student, AccumulateAttendDayDat> _schregnoAttendDayDat;
    private final String[] _updateRegisterCd;

    /**
     * コンストラクタ。
     * @param term 範囲
     * @param kintaiManager 勤怠マネージャ
     * @param schregnoAttendDayDat 格納するマップコレクション
     */
    public DaoAttendDayDat(final Header header, final KintaiManager kintaiManager, final Map<Student, AccumulateAttendDayDat> schregnoAttendDayDat, final String[] schoolKind, final String[] updateRegisterCd) {
        super(log);
        _header = header;
        _kintaiManager = kintaiManager;
        _schregnoAttendDayDat = schregnoAttendDayDat;
        _schoolKind = schoolKind;
        _updateRegisterCd = updateRegisterCd;
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

            if (null == _schregnoAttendDayDat.get(student)) {
                _schregnoAttendDayDat.put(student, new AccumulateAttendDayDat(student));
            }
            final AccumulateAttendDayDat aadd = _schregnoAttendDayDat.get(student);
            aadd.addDayDat(date, _kintaiManager, kintai);
            rtn = aadd;

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
                + "    DI_CD as kintai "
                + "  from " + TABLE_NAME
                + "  where"
                + "    ATTENDDATE between ? and ? "
                + (ArrayUtils.isEmpty(_schoolKind) ? "" : "  and SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT I1 INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE WHERE I1.YEAR = ? AND I2.SCHOOL_KIND IN " + SQLUtils.whereIn(true, _schoolKind) + ") ")
                + (ArrayUtils.isEmpty(_updateRegisterCd) ? "" : " and REGISTERCD NOT IN " + SQLUtils.whereIn(true, _updateRegisterCd))
                + "  order by"
                + "    SCHREGNO ";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        final List<Object> args = new ArrayList<Object>();
        args.add(_header.getTerm().getSDate().getSQLDate());
        args.add(_header.getTerm().getEDate().getSQLDate());
        if (!ArrayUtils.isEmpty(_schoolKind)) {
            args.add(_header.getNendo());
        }
        return args.toArray(new Object[args.size()]);
    }

} // DaoAttendDayDat

// eof
