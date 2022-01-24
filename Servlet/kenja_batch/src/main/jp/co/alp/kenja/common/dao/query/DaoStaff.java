// kanji=漢字
/*
 * $Id: DaoStaff.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/07/06 17:39:15 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Staff;
import jp.co.alp.kenja.common.util.KenjaParameters;

/*
 * describe table V_STAFF_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * STAFFCD                        SYSIBM    VARCHAR                   8     0 いいえ
 * STAFFNAME                      SYSIBM    VARCHAR                  60     0 はい
 * STAFFNAME_SHOW                 SYSIBM    VARCHAR                  15     0 はい
 * STAFFNAME_KANA                 SYSIBM    VARCHAR                 120     0 はい
 * STAFFNAME_ENG                  SYSIBM    VARCHAR                  60     0 はい
 * JOBCD                          SYSIBM    VARCHAR                   4     0 はい
 * SECTIONCD                      SYSIBM    VARCHAR                   4     0 はい
 * DUTYSHARECD                    SYSIBM    VARCHAR                   4     0 はい
 * CHARGECLASSCD                  SYSIBM    VARCHAR                   1     0 はい
 * STAFFSEX                       SYSIBM    VARCHAR                   1     0 はい
 * STAFFBIRTHDAY                  SYSIBM    DATE                      4     0 はい
 * STAFFZIPCD                     SYSIBM    VARCHAR                   8     0 はい
 * STAFFADDR1                     SYSIBM    VARCHAR                  75     0 はい
 * STAFFADDR2                     SYSIBM    VARCHAR                  75     0 はい
 * STAFFTELNO                     SYSIBM    VARCHAR                  14     0 はい
 * STAFFFAXNO                     SYSIBM    VARCHAR                  14     0 はい
 * STAFFE_MAIL                    SYSIBM    VARCHAR                  25     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     19 レコードが選択されました。
 */

/**
 * 職員を読み込む。
 * @author tamura
 * @version $Id: DaoStaff.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public final class DaoStaff extends AbstractDaoLoader<Staff> {
    /** テーブル名 */
    public static final String TABLE_NAME = "V_STAFF_MST";

    /** log */
    private static final Log log = LogFactory.getLog(DaoStaff.class);
    private static final DaoStaff INSTANCE = new DaoStaff();

    private boolean _isShowStaffcd;

    /*
     * コンストラクタ。
     */
    private DaoStaff() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Staff> getInstance(final KenjaParameters param) {
        INSTANCE._isShowStaffcd = param.isShowStaffcd();
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final String code = MapUtils.getString(map, "code");
        final String showCode;
        if (_isShowStaffcd) {
            showCode = code;
        } else {
            showCode = "";
        }

        return Staff.create(
                _cm.getCategory(),
                code,
                showCode,
                MapUtils.getString(map, "name"),
                MapUtils.getString(map, "showName")
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    STAFFCD as code,"
                + "    STAFFNAME as name,"
                + "    STAFFNAME_SHOW as showName"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  order by"
                + "    SECTIONCD,"
                + "    STAFFCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoStaff

// eof
