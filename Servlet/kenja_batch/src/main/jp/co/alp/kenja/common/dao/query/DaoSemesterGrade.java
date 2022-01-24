// kanji=漢字
/*
 * $Id: DaoSemesterGrade.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2016/01/18 17:15:04 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.SemesterGrade;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

/*
 * describe table SEMESTER_GRADE_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * GRADE                          SYSIBM    VARCHAR                   2     0 はい
 * SEMESTERNAME                   SYSIBM    VARCHAR                  30     0 はい
 * SDATE                          SYSIBM    DATE                      4     0 はい
 * EDATE                          SYSIBM    DATE                      4     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     8 レコードが選択されました。
 */

/**
 * 学期(学年ごと)を読み込む。
 * @author maesiro
 * @version $Id: DaoSemesterGrade.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoSemesterGrade extends AbstractDaoLoader<SemesterGrade> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SEMESTER_GRADE_MST";

    /** log */
    private static final Log log = LogFactory.getLog(DaoSemesterGrade.class);
    private static final AbstractDaoLoader<SemesterGrade> INSTANCE = new DaoSemesterGrade();

    /*
     * コンストラクタ。
     */
    private DaoSemesterGrade() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<SemesterGrade> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        return SemesterGrade.create(
                _cm.getCategory(),
                MapUtils.getIntValue(map, "code", -1),
                MapUtils.getString(map, "grade"),
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
                + "    GRADE as grade,"
                + "    SDATE as sdate,"
                + "    EDATE as edate"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
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
} // DaoSemesterGrade

// eof
