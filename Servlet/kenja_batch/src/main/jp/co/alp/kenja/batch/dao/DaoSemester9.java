// kanji=漢字
/*
 * $Id: DaoSemester9.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.lang.enums.MyEnum.Category;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学期(総合)を読み込む。
 * @author maesiro
 * @version $Id: DaoSemester9.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class DaoSemester9 extends AbstractDaoLoader<Semester> {
    /** テーブル名 */
    public static final String TABLE_NAME = DaoDateSemester.TABLE_NAME;
    /** 総合学期 */
    public static final int SEMESTER_9 = 9;

    /** log */
    private static final Log log = LogFactory.getLog(DaoSemester9.class);
    private static final DaoSemester9 INSTANCE = new DaoSemester9();

    /*
     * コンストラクタ。
     */
    private DaoSemester9() {
        super(log);
    }

    /**
     * 日付の学期を得る。
     * @param category カテゴリー
     * @param date 日付
     * @return 学期
     */
    public Semester getSemester(final Category category, final KenjaDateImpl date) {
        final List<Semester> list = Semester.getEnumList(category);
        for (final Semester sem : list) {
            if (sem.getCode() == SEMESTER_9) {
                continue;
            }
            if (sem.isValidDate(date)) {
                return sem;
            }
        }
        throw new IllegalArgumentException("学期不明=" + date);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static DaoSemester9 getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return Semester.create(
                _cm.getCategory(),
                MapUtils.getIntValue(map, "code", -1),
                MapUtils.getString(map, "name"),
                KenjaMapUtils.getKenjaDateImpl(map, "sdate"),
                KenjaMapUtils.getKenjaDateImpl(map, "edate")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    int(SEMESTER) as code,"
                + "    SEMESTERNAME as name,"
                + "    SDATE as sdate,"
                + "    EDATE as edate"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "    and SEMESTER = '" + SEMESTER_9 + "'"
                + "  order by"
                + "    SEMESTER";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoSemester9

// eof
