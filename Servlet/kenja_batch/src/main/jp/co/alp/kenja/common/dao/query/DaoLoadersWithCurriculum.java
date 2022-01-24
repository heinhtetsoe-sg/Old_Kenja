/*
 * $Id: DaoLoadersWithCurriculum.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2012/07/31
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import jp.co.alp.kenja.common.dao.query.nocurriculum.DaoChairNoCurriculum;
import jp.co.alp.kenja.common.dao.query.nocurriculum.DaoSubClassNoCurriculum;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 教育課程コードを考慮したローダー
 * @version $Id: DaoLoadersWithCurriculum.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public final class DaoLoadersWithCurriculum {
    
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
        AbstractDaoLoader<Chair> getDaoChairInstance();
        /**
         * 科目読み込みインスタンスを得る。
         * @return 科目読み込みインスタンス
         */
        AbstractDaoLoader<SubClass> getDaoSubClassInstance();
    }

    /**
     * 教育課程ありのテーブルを読み込むローダー
     */
    private class CurriculumLoaders implements Loaders {

        public AbstractDaoLoader<Chair> getDaoChairInstance() {
            return DaoChair.getInstance();
        }

        public AbstractDaoLoader<SubClass> getDaoSubClassInstance() {
            return DaoSubClass.getInstance();
        }
    }
    
    /**
     * 教育課程なしのテーブルを読み込むローダー
     */
    private class NoCurriculumLoaders implements Loaders {

        public AbstractDaoLoader<Chair> getDaoChairInstance() {
            return DaoChairNoCurriculum.getInstance();
        }

        public AbstractDaoLoader<SubClass> getDaoSubClassInstance() {
            return DaoSubClassNoCurriculum.getInstance();
        }
    }
}
