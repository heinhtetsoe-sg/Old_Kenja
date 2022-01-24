// kanji=漢字
/*
 * $Id: DaoBatchChairOfYearNoCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.nocurriculum;

import java.util.ArrayList;
import java.util.Map;

import jp.co.alp.kenja.batch.dao.absencehigh.DaoBatchChairOfYear;
import jp.co.alp.kenja.common.dao.query.DaoChair;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.GroupClass;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.domain.SubClass;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 年度の講座を読み込む。
 * @version $Id: DaoBatchChairOfYearNoCurriculum.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoBatchChairOfYearNoCurriculum extends DaoBatchChairOfYear {
    /** テーブル名 */
    public static final String TABLE_NAME = DaoChair.TABLE_NAME;

    /** log */
    private static final Log log = LogFactory.getLog(DaoBatchChairOfYearNoCurriculum.class);
    private static final DaoBatchChairOfYearNoCurriculum INSTANCE = new DaoBatchChairOfYearNoCurriculum();

    /*
     * コンストラクタ。
     */
    private DaoBatchChairOfYearNoCurriculum() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static DaoBatchChairOfYear getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final GroupClass group = GroupClass.getInstance(_cm.getCategory(), MapUtils.getString(map, "group"));
        if (null == group) {
            return "不明な選択科目コード(group)";
        }
        final SubClass subClass = SubClass.getInstance(
                _cm.getCategory(),
                null, null, null,
                MapUtils.getString(map, "subClassCd")
        );
        if (null == subClass) {
            return "不明な科目情報(subClassCd)";
        }
        final boolean countFlag = StringUtils.equals(MapUtils.getString(map, "countFlag"), "1");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////// =>1
        final String chairCd = MapUtils.getString(map, "code");
        final Semester semester = Semester.getInstance(_cm.getCategory(), MapUtils.getInteger(map, "semester").intValue());

        if (!_semesterChairCds.containsKey(semester)) {
            _semesterChairCds.put(semester, new ArrayList<String>());
        }
        getChairList(semester).add(chairCd);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////// <=1

        return Chair.create(
                _cm.getCategory(),
                chairCd,
                group,
                subClass,
                MapUtils.getString(map, "name"),
                MapUtils.getInteger(map, "lessonCount"),
                MapUtils.getInteger(map, "frameCount"),
                countFlag
        );
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    CHAIRCD as code,"
                + "    SEMESTER as semester,"
                + "    GROUPCD as group,"
                + "    SUBCLASSCD as subClassCd,"
                + "    CHAIRNAME as name,"
                + "    LESSONCNT as lessonCount,"
                + "    FRAMECNT as frameCount,"
                + "    COUNTFLG as countFlag"
                + "  from " + TABLE_NAME
                + "  where"
                + "    YEAR = ?"
                + "  order by"
                + "    SEMESTER, CHAIRCD";
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            cm.getCurrentYearAsString(),
        };
    }
} // DaoBatchChairOfYear

// eof
