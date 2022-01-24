// kanji=漢字
/*
 * $Id: DaoSemester.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 11:15:04 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

/*
 * describe table SEMESTER_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * SEMESTERNAME                   SYSIBM    VARCHAR                  15     0 はい
 * SDATE                          SYSIBM    DATE                      4     0 はい
 * EDATE                          SYSIBM    DATE                      4     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     7 レコードが選択されました。
 */

/**
 * 学期を読み込む。
 * @author tamura
 * @version $Id: DaoSemester.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoSemester extends AbstractDaoLoader<Semester> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SEMESTER_MST";

    /** log */
    private static final Log log = LogFactory.getLog(DaoSemester.class);
    private static final AbstractDaoLoader<Semester> INSTANCE = new DaoSemester();

    /*
     * コンストラクタ。
     */
    private DaoSemester() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Semester> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    protected void postLoad() throws SQLException {
        if (hasTable(_conn, DaoSemesterGrade.TABLE_NAME)) {
            DaoSemesterGrade.getInstance().load(_conn, _cm);
        }
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
} // DaoSemester

// eof
