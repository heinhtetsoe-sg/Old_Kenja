// kanji=漢字
/*
 * $Id: DaoAttendSubclassSpecialDatNoCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/18 14:59:56 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.nocurriculum;

import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AttendSubclassSpecialDat;
import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.SubClass;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 特別活動科目グループデータを取得する。
 * @version $Id: DaoAttendSubclassSpecialDatNoCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoAttendSubclassSpecialDatNoCurriculum extends AbstractDaoLoader<AttendSubclassSpecialDat> {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_SUBCLASS_SPECIAL_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoAttendSubclassSpecialDatNoCurriculum.class);
    private static DaoAttendSubclassSpecialDatNoCurriculum instance_;

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    private DaoAttendSubclassSpecialDatNoCurriculum(
    ) {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static DaoAttendSubclassSpecialDatNoCurriculum getInstance() {
        if (instance_ == null) {
            instance_ = new DaoAttendSubclassSpecialDatNoCurriculum();
        }
        return instance_;
    }



    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {

        final String specialGroupCd = MapUtils.getString(map, "specialGroupCd");

        final SubClass subClass = SubClass.getInstance(
                _cm.getCategory(),
                null, null, null,
                MapUtils.getString(map, "subClassCd")
        );
        if (null == subClass) {
            return "不明な科目情報(subClassCd)";
        }

        final Integer minutes = MapUtils.getInteger(map, "minutes", Integer.valueOf("0"));

        final AttendSubclassSpecialDat asd = AttendSubclassSpecialDat.getAttendSubclassSpecialDat(specialGroupCd);
        asd.put(subClass, minutes);
        return asd;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    SPECIAL_GROUP_CD as specialGroupCd,"
                + "    SUBCLASSCD as subClassCd,"
                + "    MINUTES as minutes"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ? "
                + "  order by"
                + "    SPECIAL_GROUP_CD, SUBCLASSCD ";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }

} // DaoAttendSubclassSpecialDat

// eof
