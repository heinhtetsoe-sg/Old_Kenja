// kanji=漢字
/*
 * $Id: DaoCourseInfo.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/24 21:35:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.CourseInfo;
import jp.co.alp.kenja.common.domain.Grade;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * db2 describe table COURSE_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り NULL
 * ------------------------------ --------- ------------------ -------- ----- ------
 * COURSECD                       SYSIBM    VARCHAR                   1     0 いいえ
 * COURSENAME                     SYSIBM    VARCHAR                  60     0 はい
 * COURSEABBV                     SYSIBM    VARCHAR                  60     0 はい
 * COURSEENG                      SYSIBM    VARCHAR                  10     0 はい
 * S_PERIODCD                     SYSIBM    VARCHAR                   1     0 はい
 * E_PERIODCD                     SYSIBM    VARCHAR                   1     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 * 
 *   8 レコードが選択されました。
 * 
 */
/*
 * db2 describe table MAJOR_MST
 *                                タイプ・
 * 列名   スキーマ    タイプ名    長さ  位取り NULL
 * ------------------------------   ---------   ------------------  --------    -----   ------
 * COURSECD                       SYSIBM    VARCHAR                   1     0 いいえ
 * MAJORCD                        SYSIBM    VARCHAR                   3     0 いいえ
 * MAJORNAME                      SYSIBM    VARCHAR                  60     0 はい
 * MAJORABBV                      SYSIBM    VARCHAR                   6     0 はい
 * MAJORENG                       SYSIBM    VARCHAR                  20     0 はい
 * MAJORBANKCD                    SYSIBM    VARCHAR                   2     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 * 
 * 8    レコードが選択されました。
 */ 
/*
 * db2 describe table coursecode_mst
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り NULL
 * ------------------------------ --------- ------------------ -------- ----- ------
 * COURSECODE                     SYSIBM    VARCHAR                   4     0 いいえ
 * COURSECODENAME                 SYSIBM    VARCHAR                  60     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 * 
 *   4 レコードが選択されました。
 * /

/**
 * コース情報を読み込む。
 * @author maesiro
 * @version $Id: DaoCourseInfo.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoCourseInfo extends AbstractDaoLoader<CourseInfo> {
    /** 課程マスタ/テーブル名 */
    public static final String TABLE_NAME = "COURSE_MST";
    /** 学科データ/テーブル名 */
    public static final String TABLE_NAME2 = "MAJOR_MST";
    /** コースマスタ /テーブル名 */
    public static final String TABLE_NAME3 = "COURSECODE_MST";

    private static final Log log = LogFactory.getLog(DaoCourseInfo.class);
    private static final DaoCourseInfo INSTANCE = new DaoCourseInfo();

    /*
     * コンストラクタ。
     */
    private DaoCourseInfo() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<CourseInfo> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {

        final CourseInfo courseInfo = CourseInfo.create(
                _cm.getCategory(),
                MapUtils.getString(map, "courseCd"),
                MapUtils.getString(map, "majorCd"),
                MapUtils.getString(map, "courseCode"),
                Grade.create(_cm.getCategory(), MapUtils.getString(map, "grade"))
        );
        courseInfo.setCourseName(MapUtils.getString(map, "courseName"));
        courseInfo.setMajorName(MapUtils.getString(map, "majorName"));
        courseInfo.setCourseCodeName(MapUtils.getString(map, "coursecodeName"));

        return courseInfo;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final StringBuffer sql = new StringBuffer();
        sql.append("select distinct ");
        sql.append("    t1.GRADE as grade,");               // 学年
        sql.append("    t2.COURSECD as courseCd,");         // 課程コード
        sql.append("    t2.COURSENAME as courseName,");     // 課程名
        sql.append("    t3.MAJORCD as majorCd,");           // 学科コード
        sql.append("    t3.MAJORNAME as majorName,");       // 学科名
        sql.append("    t4.COURSECODE as courseCode,");     // コースコード
        sql.append("    t4.COURSECODENAME as coursecodeName");     // コース名
        sql.append("  from ").append(DaoStudent.TABLE_NAME2).append(" t1");
        sql.append("    inner join ").append(TABLE_NAME).append(" t2");
        sql.append("      on t1.COURSECD = t2.COURSECD");
        sql.append("    inner join ").append(TABLE_NAME2).append(" t3");
        sql.append("      on t1.COURSECD = t3.COURSECD");
        sql.append("     and t1.MAJORCD = t3.MAJORCD");
        sql.append("    inner join ").append(TABLE_NAME3).append(" t4");
        sql.append("      on t1.COURSECODE = t4.COURSECODE");
        sql.append("  where t1.YEAR = ?");
        sql.append("  and   t1.SEMESTER = ?");
        sql.append("  order by");
        sql.append("    t1.GRADE,");
        sql.append("    t2.COURSECD,");
        sql.append("    t3.MAJORCD,");
        sql.append("    t4.COURSECODE");
        return sql.toString();
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
} // DaoCourseInfo

// eof
