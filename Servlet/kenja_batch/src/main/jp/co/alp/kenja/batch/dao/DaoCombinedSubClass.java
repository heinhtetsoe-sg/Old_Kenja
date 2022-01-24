// kanji=漢字
/*
 * $Id: DaoCombinedSubClass.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2004/07/05 14:23:26 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.Map;

import jp.co.alp.kenja.batch.domain.CombinedSubClassManager;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.dao.query.DaoSubClass;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.SubClass;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * db2 describe table SUBCLASS_REPLACE_COMBINED_DAT
 *
 *                                 Data type                     Column
 * Column name                     schema    Data type name      Length     Scale Nulls
 * ------------------------------- --------- ------------------- ---------- ----- ------
 * REPLACECD                       SYSIBM    VARCHAR                      1     0 いいえ
 * YEAR                            SYSIBM    VARCHAR                      4     0 いいえ
 * COMBINED_CLASSCD                SYSIBM    VARCHAR                      2     0 いいえ
 * COMBINED_SCHOOL_KIND            SYSIBM    VARCHAR                      2     0 いいえ
 * COMBINED_CURRICULUM_CD          SYSIBM    VARCHAR                      2     0 いいえ
 * COMBINED_SUBCLASSCD             SYSIBM    VARCHAR                      6     0 いいえ
 * ATTEND_CLASSCD                  SYSIBM    VARCHAR                      2     0 いいえ
 * ATTEND_SCHOOL_KIND              SYSIBM    VARCHAR                      2     0 いいえ
 * ATTEND_CURRICULUM_CD            SYSIBM    VARCHAR                      2     0 いいえ
 * ATTEND_SUBCLASSCD               SYSIBM    VARCHAR                      6     0 いいえ
 * CALCULATE_CREDIT_FLG            SYSIBM    VARCHAR                      1     0 はい
 * STUDYREC_CREATE_FLG             SYSIBM    VARCHAR                      1     0 はい
 * PRINT_FLG1                      SYSIBM    VARCHAR                      1     0 はい
 * PRINT_FLG2                      SYSIBM    VARCHAR                      1     0 はい
 * PRINT_FLG3                      SYSIBM    VARCHAR                      1     0 はい
 * REGISTERCD                      SYSIBM    VARCHAR                      8     0 はい
 * UPDATED                         SYSIBM    TIMESTAMP                   10     0 はい
 *
 *   17 レコードが選択されました。
 */

/**
 * 合併科目を読み込む。
 * @author maesiro
 * @version $Id: DaoCombinedSubClass.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoCombinedSubClass extends AbstractDaoLoader<SubClass> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SUBCLASS_REPLACE_COMBINED_DAT";
    /** テーブル名2 */
    public static final String TABLE_NAME2 = DaoSubClass.TABLE_NAME;

    /** log */
    private static final Log log = LogFactory.getLog(DaoCombinedSubClass.class);
    private static final DaoCombinedSubClass INSTANCE = new DaoCombinedSubClass();

    /*
     * コンストラクタ。
     */
    private DaoCombinedSubClass() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<SubClass> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        Object rtn = null;
    check:
        {
            final SubClass combinedSubClass = SubClass.getInstance(
                    _cm.getCategory(),
                    MapUtils.getString(map, "combinedClassCd"),
                    MapUtils.getString(map, "combinedSchoolKind"),
                    MapUtils.getString(map, "combinedCurriculumCd"),
                    MapUtils.getString(map, "combinedSubClassCd")
            );
            if (null == combinedSubClass) {
                rtn = "不明な合併先科目情報(combinedClassCd,combinedSchoolKind,combinedCurriculumCd,combinedSubClassCd)";
                break check;
            }

            final SubClass attendSubClass = SubClass.getInstance(
                    _cm.getCategory(),
                    MapUtils.getString(map, "attendClassCd"),
                    MapUtils.getString(map, "attendSchoolKind"),
                    MapUtils.getString(map, "attendCurriculumCd"),
                    MapUtils.getString(map, "attendSubClassCd")
            );
            if (null == attendSubClass) {
                rtn = "不明な合併元科目情報(attendClassCd,attendSchoolKind,attendCurriculumCd,attendSubClassCd)";
                break check;
            }
            CombinedSubClassManager.getInstance().add(combinedSubClass, attendSubClass);
            rtn = null;
        } // check:
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    t1.COMBINED_CLASSCD as combinedClassCd,"
                + "    t1.COMBINED_SCHOOL_KIND as combinedSchoolKind,"
                + "    t1.COMBINED_CURRICULUM_CD as combinedCurriculumCd,"
                + "    t1.COMBINED_SUBCLASSCD as combinedSubClassCd,"
                + "    t1.ATTEND_CLASSCD as attendClassCd, "
                + "    t1.ATTEND_SCHOOL_KIND as attendSchoolKind, "
                + "    t1.ATTEND_CURRICULUM_CD as attendCurriculumCd, "
                + "    t1.ATTEND_SUBCLASSCD as attendSubClassCd "
                + "  from " + TABLE_NAME + " t1 "
                + "  inner join " + TABLE_NAME2 + " t2 on t2.year = t1.year "
                + "      and t1.ATTEND_CLASSCD = t2.CLASSCD "
                + "      and t1.ATTEND_SCHOOL_KIND = t2.SCHOOL_KIND "
                + "      and t1.ATTEND_CURRICULUM_CD = t2.CURRICULUM_CD "
                + "      and t1.ATTEND_SUBCLASSCD = t2.SUBCLASSCD "
                + "  inner join " + TABLE_NAME2 + " t3 on t3.year = t1.year "
                + "      and t1.COMBINED_CLASSCD = t3.CLASSCD "
                + "      and t1.COMBINED_SCHOOL_KIND = t3.SCHOOL_KIND "
                + "      and t1.COMBINED_CURRICULUM_CD = t3.CURRICULUM_CD "
                + "      and t1.COMBINED_SUBCLASSCD = t3.SUBCLASSCD "
                + "  where"
                + "    t1.YEAR = ?"
                + "  order by"
                + "    t1.COMBINED_CLASSCD, t1.COMBINED_SCHOOL_KIND, t1.COMBINED_CURRICULUM_CD, t1.COMBINED_SUBCLASSCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoCombinedSubClass

// eof
