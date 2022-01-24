// kanji=漢字
/*
 * $Id: DaoInitLoaderLessonCountBatch.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.loader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.dao.DaoAccumulateAttendanceNoCount;
import jp.co.alp.kenja.batch.dao.DaoBatchSchoolMaster;
import jp.co.alp.kenja.batch.dao.DaoBatchWithCurriculum;
import jp.co.alp.kenja.batch.dao.DaoEntDate;
import jp.co.alp.kenja.batch.dao.DaoSemester9;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchChairStudentOfYear;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchHomeRoomOfYear;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchStudentOfYear;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchUsualScheduleOfYear;
import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.query.DaoChairStaff;
import jp.co.alp.kenja.common.dao.query.DaoGroupClass;
import jp.co.alp.kenja.common.dao.query.DaoInitLoader;
import jp.co.alp.kenja.common.dao.query.DaoPeriod;
import jp.co.alp.kenja.common.dao.query.DaoSemester;
import jp.co.alp.kenja.common.dao.query.DaoStaff;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.lang.enums.MyEnum.Category;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.dbutils.DbUtils;

/**
 * 欠課数上限テーブルバッチのDB初期読み込み。
 * @version $Id: DaoInitLoaderLessonCountBatch.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class DaoInitLoaderLessonCountBatch implements DaoInitLoader {
    private static DaoInitLoaderLessonCountBatch instance_;

    private AccumulateOptions _options;

    /**
     * コンストラクタ。
     * @param options 出欠累積データバッチ処理オプション
     */
    public DaoInitLoaderLessonCountBatch(final AccumulateOptions options) {
        super();
        _options = options;
    }

    /**
     * インスタンスを得る。
     * @param options 出欠累積データバッチ処理オプション
     * @return インスタンス
     */
    public static DaoInitLoaderLessonCountBatch getInstance(final AccumulateOptions options) {
        instance_ = new DaoInitLoaderLessonCountBatch(options);
        return instance_;
    }

    /**
     * {@inheritDoc}
     */
    public void load(
            final Category category,
            final ControlMaster cm,
            final DbConnection dbcon,
            final Properties prop,
            final KenjaParameters params
    ) throws SQLException {
        Connection conn = null;
        try {
            conn = dbcon.getROConnection();
            final DaoBatchWithCurriculum.Loaders loaders = new DaoBatchWithCurriculum().getLoaders(params);
            DaoBatchSchoolMaster.getInstance(_options).load(conn, cm);

            DaoSemester.getInstance().load(conn, cm);
            DaoSemester9.getInstance().load(conn, cm);
            DaoPeriod.getInstance().load(conn, cm);
            loaders.getDaoSubClassInstance().load(conn, cm);
            loaders.getDaoCombinedSubClassInstance().load(conn, cm);
            DaoGroupClass.getInstance().load(conn, cm);
            DaoStaff.getInstance(params).load(conn, cm);
            DaoChairStaff.getInstance().load(conn, cm);
            DaoBatchHomeRoomOfYear.getInstance().load(conn, cm);
            DaoBatchStudentOfYear.getInstance().load(conn, cm);
            DaoEntDate.getInstance().load(conn, cm);
            loaders.getDaoBatchChairOfYearInstance().load(conn, cm);
            DaoBatchChairStudentOfYear.getInstance().load(conn, cm);
            loaders.getDaoAttendSubClassDatInstance(_options).load(conn, cm);
            loaders.getDaoAttendSubClassSpecialDatInstance().load(conn, cm);

            final KenjaDateImpl scheduleStartDate = _options.getDate().nextDate();
            DaoBatchUsualScheduleOfYear.getInstance(scheduleStartDate).load(conn, cm);
            DaoAccumulateAttendanceNoCount.getInstance(scheduleStartDate).load(conn, cm);

        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
} // DaoInitLoaderLessonCountBatch

// eof
