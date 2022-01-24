// kanji=漢字
/*
 * $Id: DaoAttendSubclassDatNoCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 14:59:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.nocurriculum;

import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AttendSubclassDat;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.dao.DaoAttendSubclassDat;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 科目別累積データを取得する。
 * @version $Id: DaoAttendSubclassDatNoCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoAttendSubclassDatNoCurriculum extends DaoAttendSubclassDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_SUBCLASS_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoAttendSubclassDatNoCurriculum.class);
    private static DaoAttendSubclassDatNoCurriculum instance_;

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    private DaoAttendSubclassDatNoCurriculum(
            final AccumulateOptions options
    ) {
        super(log, options);
    }

    /**
     * インスタンスを得る。
     * @param options オプション
     * @return インスタンス
     */
    public static DaoAttendSubclassDat getInstance(final AccumulateOptions options) {
        if (instance_ == null) {
            instance_ = new DaoAttendSubclassDatNoCurriculum(options);
        }
        return instance_;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final Semester semester = Semester.getInstance(_cm.getCategory(), MapUtils.getIntValue(map, "semester"));
        if (null == semester) {
            return "不明な学期(semester)";
        }

        final Student student = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "schregno"));
        if (null == student) {
            return "不明な生徒(student)";
        }

        final SubClass subClass = SubClass.getInstance(
                _cm.getCategory(),
                null, null, null,
                MapUtils.getString(map, "subClassCd")
        );
        if (null == subClass) {
            return "不明な科目情報(subClassCd)";
        }

        final int year = MapUtils.getIntValue(map, "year");
        final int month = MapUtils.getIntValue(map, "month");
        final int appointedDay = MapUtils.getIntValue(map, "appointedDay");

        final int lesson = MapUtils.getIntValue(map, "lesson", 0);
        final int abroad = MapUtils.getIntValue(map, "abroad", 0);
        final int offdays = MapUtils.getIntValue(map, "offdays", 0);
        final int suspend = MapUtils.getIntValue(map, "suspend", 0);
        final int mourning = MapUtils.getIntValue(map, "mourning", 0);

        final KenjaDateImpl calcEdate = KenjaDateImpl.getInstance(year, month, appointedDay);
        final KenjaDateImpl calcSdate = _options.getBaseDay().getTerm(calcEdate).getSDate();
        final Term calculatedTerm = new Term(calcSdate, calcEdate);

        final AttendSubclassDat asd = new AttendSubclassDat(subClass, student, calculatedTerm, lesson, abroad, offdays, suspend, mourning);
        _attendSubclassDatList.add(asd);
        return asd;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    SEMESTER as semester,"
                + "    SCHREGNO as schregno,"
                + "    SUBCLASSCD as subClassCd,"
                + "    YEAR as year,"
                + "    MONTH as month,"
                + "    APPOINTED_DAY as appointedDay,"
                + "    LESSON as lesson,"
                + "    ABROAD as abroad,"
                + "    OFFDAYS as offdays,"
                + "    SUSPEND as suspend,"
                + "    MOURNING as mourning"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ? "
                + "    AND MONTH in " + _monthSqlWhereIn
                + "  order by"
                + "    SEMESTER, SCHREGNO, SUBCLASSCD ";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }

} // DaoAttendSubclassDat

// eof
