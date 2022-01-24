/*
 * $Id: DaoBatchWithCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2012/08/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.sql.Connection;

import jp.co.alp.kenja.batch.accumulate.AttendSubclassSpecialDat;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchChairOfYear;
import jp.co.alp.kenja.batch.dao.nocurriculum.DaoAttendSubclassDatNoCurriculum;
import jp.co.alp.kenja.batch.dao.nocurriculum.DaoAttendSubclassSpecialDatNoCurriculum;
import jp.co.alp.kenja.batch.dao.nocurriculum.DaoBatchChairOfYearNoCurriculum;
import jp.co.alp.kenja.batch.dao.nocurriculum.DaoCombinedSubClassNoCurriculum;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAbsenceHighDat;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSubclassDat;
import jp.co.alp.kenja.batch.dao.update.nocurriculum.DaoUpdateAbsenceHighDatNoCurriculum;
import jp.co.alp.kenja.batch.dao.update.nocurriculum.DaoUpdateAttendSubclassDatNoCurriculum;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.dao.query.DaoSubClass;
import jp.co.alp.kenja.common.dao.query.nocurriculum.DaoSubClassNoCurriculum;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 教育課程コードを考慮したローダー
 * @version $Id: DaoBatchWithCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoBatchWithCurriculum {
    
    /**
     * 教育課程コードを考慮した更新クラスを得る
     * @param param パラメータ
     * @return 教育課程コードを考慮した更新クラス 
     */
    public Updaters getUpdaters(final KenjaParameters param) {
        if (param.useCurriculumcd()) {
            return new CurriculumUpdaters();
        } else {
            return new NoCurriculumUpders();
        }
    }
    
    /**
     * 科目の教育課程コードの有無に対応する共通インターフェース
     */
    public interface Updaters {
        /**
         * 科目出欠累積データ更新インスタンスを得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @param options オプション
         * @return 科目出欠累積データ更新インスタンス
         */
        DaoUpdateAttendSubclassDat getDaoUpdateAttendSubclassDatInstance(
                final Connection conn, final ControlMaster cm, final AccumulateOptions options);
        /**
         * 欠課数上限値データ更新インスタンスを得る。
         * @param conn コネクション
         * @param cm コントロール・マスタ
         * @param options オプション
         * @param schoolMaster バッチ用学校マスタ
         * @return 欠課数上限値データ更新インスタンス
         */
        DaoUpdateAbsenceHighDat getDaoUpdateAbsenceHighDatInstance(
                final Connection conn, final ControlMaster cm, final AccumulateOptions options, final BatchSchoolMaster schoolMaster);
    }

    /**
     * 教育課程ありのテーブルを更新する
     */
    private class CurriculumUpdaters implements Updaters {
        public DaoUpdateAttendSubclassDat getDaoUpdateAttendSubclassDatInstance(
                final Connection conn, final ControlMaster cm, final AccumulateOptions options) {
            return new DaoUpdateAttendSubclassDat(conn, cm, options);
        }

        public DaoUpdateAbsenceHighDat getDaoUpdateAbsenceHighDatInstance(
                final Connection conn, final ControlMaster cm, final AccumulateOptions options, final BatchSchoolMaster schoolMaster) {
            return new DaoUpdateAbsenceHighDat(conn, cm, options, schoolMaster);
        }
    }
    
    /**
     * 教育課程なしのテーブルを更新する
     */
    private class NoCurriculumUpders implements Updaters {
        public DaoUpdateAttendSubclassDat getDaoUpdateAttendSubclassDatInstance(
                final Connection conn, final ControlMaster cm, final AccumulateOptions options) {
            return new DaoUpdateAttendSubclassDatNoCurriculum(conn, cm, options);
        }

        public DaoUpdateAbsenceHighDat getDaoUpdateAbsenceHighDatInstance(
                final Connection conn, final ControlMaster cm, final AccumulateOptions options, final BatchSchoolMaster schoolMaster) {
            return new DaoUpdateAbsenceHighDatNoCurriculum(conn, cm, options, schoolMaster);
        }
    }
    
    /**
     * 教育課程コードを考慮したローダーを得る
     * @param param パラメータ
     * @return 教育課程コードを考慮したローダー 
     */
    public Loaders getLoaders(final KenjaParameters param) {
        if (param.useCurriculumcd()) {
            return new CurriculumLoaders();
        } else {
            return new NoCurriculumLoaders();
        }
    }
    
    /**
     * 科目の教育課程コードの有無に対応する共通インターフェース
     */
    public interface Loaders {
        /**
         * 講座読み込みインスタンスを得る。
         * @return 講座読み込みインスタンス
         */
        DaoBatchChairOfYear getDaoBatchChairOfYearInstance();
        /**
         * 科目読み込みインスタンスを得る。
         * @return 科目読み込みインスタンス
         */
        AbstractDaoLoader<SubClass> getDaoSubClassInstance();
        /**
         * 科目別累積データ読み込みインスタンスを得る。
         * @param options オプション
         * @return 科目別累積データ読み込みインスタンス
         */
        DaoAttendSubclassDat getDaoAttendSubClassDatInstance(final AccumulateOptions options);
        /**
         * 特別活動科目グループデータ読み込みインスタンスを得る。
         * @return 特別活動科目グループデータ読み込みインスタンス
         */
        AbstractDaoLoader<AttendSubclassSpecialDat> getDaoAttendSubClassSpecialDatInstance();
        /**
         * 合併科目読み込みインスタンスを得る。
         * @return 合併科目読み込みインスタンス
         */
        AbstractDaoLoader<SubClass> getDaoCombinedSubClassInstance();
    }

    /**
     * 教育課程ありのテーブルを読み込むローダー
     */
    private class CurriculumLoaders implements Loaders {

        public AbstractDaoLoader<SubClass> getDaoSubClassInstance() {
            return DaoSubClass.getInstance();
        }

        public DaoBatchChairOfYear getDaoBatchChairOfYearInstance() {
            return DaoBatchChairOfYear.getInstance();
        }

        public DaoAttendSubclassDat getDaoAttendSubClassDatInstance(final AccumulateOptions options) {
            return DaoAttendSubclassDat.getInstance(options);
        }

        public AbstractDaoLoader<AttendSubclassSpecialDat> getDaoAttendSubClassSpecialDatInstance() {
            return DaoAttendSubclassSpecialDat.getInstance();
        }

        public AbstractDaoLoader<SubClass> getDaoCombinedSubClassInstance() {
            return DaoCombinedSubClass.getInstance();
        }
    }
    
    /**
     * 教育課程なしのテーブルを読み込むローダー
     */
    private class NoCurriculumLoaders implements Loaders {

        public AbstractDaoLoader<SubClass> getDaoSubClassInstance() {
            return DaoSubClassNoCurriculum.getInstance();
        }

        public DaoBatchChairOfYear getDaoBatchChairOfYearInstance() {
            return DaoBatchChairOfYearNoCurriculum.getInstance();
        }

        public DaoAttendSubclassDat getDaoAttendSubClassDatInstance(final AccumulateOptions options) {
            return DaoAttendSubclassDatNoCurriculum.getInstance(options);
        }

        public AbstractDaoLoader<AttendSubclassSpecialDat> getDaoAttendSubClassSpecialDatInstance() {
            return DaoAttendSubclassSpecialDatNoCurriculum.getInstance();
        }

        public AbstractDaoLoader<SubClass> getDaoCombinedSubClassInstance() {
            return DaoCombinedSubClassNoCurriculum.getInstance();
        }
    }
}
