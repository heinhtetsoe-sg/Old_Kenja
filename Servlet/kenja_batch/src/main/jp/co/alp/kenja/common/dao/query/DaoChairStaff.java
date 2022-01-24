// kanji=漢字
/*
 * $Id: DaoChairStaff.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/07 17:35:15 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChargeDiv;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Staff;

/*
 * describe table CHAIR_STF_DAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 いいえ
 * STAFFCD                        SYSIBM    VARCHAR                   8     0 いいえ
 * CHARGEDIV                      SYSIBM    SMALLINT                  2     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     7 レコードが選択されました。
 */

/**
 * 講座担当職員を読み込む。
 * @author tamura
 * @version $Id: DaoChairStaff.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoChairStaff extends AbstractDaoLoader<Staff> {
    /** テーブル名 */
    public static final String TABLE_NAME = "CHAIR_STF_DAT";

    /** log */
    private static final Log log = LogFactory.getLog(DaoChairStaff.class);
    private static final AbstractDaoLoader<Staff> INSTANCE = new DaoChairStaff();

    /*
     * コンストラクタ。
     */
    private DaoChairStaff() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Staff> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final Chair chair = Chair.getInstance(_cm.getCategory(), MapUtils.getString(map, "chair"));
        if (null == chair) {
            return "不明な講座コード(chair)";
        }
        final Staff staff = Staff.getInstance(_cm.getCategory(), MapUtils.getString(map, "staff"));
        if (null == staff) {
            return "不明な職員コード(staff)";
        }
        final ChargeDiv charge = ChargeDiv.getInstance(MapUtils.getIntValue(map, "chargeDiv", -1));
        if (null == charge) {
            return "不明な担任区分(chargeDiv)";
        }

        chair.addStaff(staff, charge);

        return chair;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    CHAIRCD as chair,"
                + "    STAFFCD as staff,"
                + "    int(CHARGEDIV) as chargeDiv"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  and"
                + "    SEMESTER = ?";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
            cm.getCurrentSemester().getCodeAsString(),
        };
    }
} // DaoChairStaff

// eof
