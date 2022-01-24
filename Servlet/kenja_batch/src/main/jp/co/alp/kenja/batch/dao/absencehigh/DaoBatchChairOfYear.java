// kanji=漢字
/*
 * $Id: DaoBatchChairOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.absencehigh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
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
 * @version $Id: DaoBatchChairOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class DaoBatchChairOfYear extends AbstractDaoLoader<Chair> {
    /** テーブル名 */
    public static final String TABLE_NAME = DaoChair.TABLE_NAME;

    /** log */
    private static final Log log = LogFactory.getLog(DaoBatchChairOfYear.class);
    private static final DaoBatchChairOfYear INSTANCE = new DaoBatchChairOfYear();
    /** 学期ごとの講座コードリストを保持する */
    protected final Map<Semester, List<String>> _semesterChairCds = new HashMap<Semester, List<String>>();

    /*
     * コンストラクタ。
     */
    private DaoBatchChairOfYear() {
        this(log);
    }

    /**
     * コンストラクタ。
     * @param log1 出力用のlog
     */
    protected DaoBatchChairOfYear(final Log log1) {
        super(log1);
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
                MapUtils.getString(map, "classCd"),
                MapUtils.getString(map, "schoolKind"),
                MapUtils.getString(map, "curriculumCd"),
                MapUtils.getString(map, "subClassCd")
        );
        if (null == subClass) {
            return "不明な科目情報(classCd,schoolKind,curriculumCd,subClassCd)";
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////// =>2
    /**
     * 学期の講座コードリストを得る
     * @param semester 学期
     * @return 学期の講座コードリスト
     */
    public List<String> getChairList(final Semester semester) {
        if (semester == null || !_semesterChairCds.containsKey(semester)) {
            return Collections.emptyList();
        }
        return _semesterChairCds.get(semester);
    }

    /**
     * 講座が学期に開講されるか
     * @param semester 学期
     * @param chair 講座
     * @return 講座が学期に開講されるか
     */
    public boolean contains(final Semester semester, final Chair chair) {
        return getChairList(semester).contains(chair.getCode());
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////// <=2


    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    CHAIRCD as code,"
                + "    SEMESTER as semester,"
                + "    GROUPCD as group,"
                + "    CLASSCD as classCd,"
                + "    SCHOOL_KIND as schoolKind,"
                + "    CURRICULUM_CD as curriculumCd,"
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
