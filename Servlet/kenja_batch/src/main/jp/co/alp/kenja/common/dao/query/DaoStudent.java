// kanji=漢字
/*
 * $Id: DaoStudent.java 75778 2020-07-31 15:15:18Z maeshiro $
 *
 * 作成日: 2004/11/24 21:35:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Collection;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.CourseInfo;
import jp.co.alp.kenja.common.domain.Gender;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

/*
 * db2 describe table SCHREG_BASE_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
 * INOUTCD                        SYSIBM    VARCHAR                   1     0 はい
 * NAME                           SYSIBM    VARCHAR                  60     0 はい
 * NAME_SHOW                      SYSIBM    VARCHAR                  30     0 はい
 * NAME_KANA                      SYSIBM    VARCHAR                 120     0 はい
 * NAME_ENG                       SYSIBM    VARCHAR                  40     0 はい
 * BIRTHDAY                       SYSIBM    DATE                      4     0 はい
 * SEX                            SYSIBM    VARCHAR                   1     0 はい
 * BLOODTYPE                      SYSIBM    VARCHAR                   2     0 はい
 * BLOOD_RH                       SYSIBM    VARCHAR                   1     0 はい
 * FINSCHOOLCD                    SYSIBM    VARCHAR                   6     0 はい
 * FINISH_DATE                    SYSIBM    DATE                      4     0 はい
 * ENT_DATE                       SYSIBM    DATE                      4     0 はい
 * ENT_DIV                        SYSIBM    VARCHAR                   1     0 はい
 * GRD_DATE                       SYSIBM    DATE                      4     0 はい
 * GRD_DIV                        SYSIBM    VARCHAR                   1     0 はい
 * GRD_REASON                     SYSIBM    VARCHAR                  75     0 はい
 * GRD_NO                         SYSIBM    VARCHAR                   8     0 はい
 * GRD_TERM                       SYSIBM    VARCHAR                   4     0 はい
 * PERMANENTZIPCD                 SYSIBM    VARCHAR                   8     0 はい
 * PERMANENTADDR1                 SYSIBM    VARCHAR                  75     0 はい
 * PERMANENTADDR2                 SYSIBM    VARCHAR                  75     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     24 レコードが選択されました。
 */

/* db2 describe table SCHREG_REGD_DAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * GRADE                          SYSIBM    VARCHAR                   2     0 はい
 * HR_CLASS                       SYSIBM    VARCHAR                   2     0 はい
 * ATTENDNO                       SYSIBM    VARCHAR                   3     0 はい
 * ANNUAL                         SYSIBM    VARCHAR                   2     0 はい
 * SEAT_ROW                       SYSIBM    VARCHAR                   2     0 はい
 * SEAT_COL                       SYSIBM    VARCHAR                   2     0 はい
 * COURSECD                       SYSIBM    VARCHAR                   1     0 はい
 * MAJORCD                        SYSIBM    VARCHAR                   3     0 はい
 * COURSECODE                     SYSIBM    VARCHAR                   4     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     14 レコードが選択されました。
 */

/**
 * 生徒を読み込む。
 * @author tamura
 * @version $Id: DaoStudent.java 75778 2020-07-31 15:15:18Z maeshiro $
 */
public final class DaoStudent extends AbstractDaoLoader<Student> {
    /** T93/学籍基礎マスタ/テーブル名 */
    public static final String TABLE_NAME = "SCHREG_BASE_MST";
    /** T96/学籍在籍データ/テーブル名 */
    public static final String TABLE_NAME2 = "SCHREG_REGD_DAT";
    /** 学籍在籍詳細 /テーブル名 */
    public static final String TABLE_NAME3 = "SCHREG_REGD_DETAIL";

    public static final String SCHREG_REGD_GDAT = "SCHREG_REGD_GDAT";

    private static final Log log = LogFactory.getLog(DaoStudent.class);
    private static final DaoStudent INSTANCE = new DaoStudent();

    private HomeRoom _homeRoom;
    private Collection<String> _codes;
    private String[] _schoolKinds;

    /*
     * コンストラクタ。
     */
    private DaoStudent() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @param codes 生徒の学籍番号のコレクション
     * @param homeRoom 年組
     * @return インスタンス
     */
    private static DaoStudent getInstance(
            final HomeRoom homeRoom,
            final Collection<String> codes,
            final String[] schoolKinds
    ) {
        INSTANCE._homeRoom = homeRoom;
        INSTANCE._codes = codes;
        INSTANCE._schoolKinds = schoolKinds;
        return INSTANCE;
    }

    /**
     * インスタンスを得る。
     * @param homeRoom 年組
     * @return インスタンス
     */
    public static DaoStudent getInstance(
            final HomeRoom homeRoom
    ) {
        return getInstance(homeRoom, null, null);
    }

    /**
     * インスタンスを得る。
     * @param codes 生徒の学籍番号のコレクション
     * @return インスタンス
     */
    public static DaoStudent getInstance(
            final Collection<String> codes
    ) {
        return getInstance(null, codes, null);
    }

    /**
     * インスタンスを得る。
     * @param codes 生徒の学籍番号のコレクション
     * @return インスタンス
     */
    public static DaoStudent getInstance(
            final String[] schoolKind
    ) {
        return getInstance(null, null, schoolKind);
    }

    /**
     * コース情報を読み込む。
     * {@inheritDoc}
     */
    protected void preLoad() throws SQLException {
        DaoCourseInfo.getInstance().load(_conn, _cm);
    }

    /**
     * 学籍異動データを読み込む。
     * {@inheritDoc}
     */
    protected void postLoad() throws SQLException {
        DaoStudentTransfer.getInstance().load(_conn, _cm);
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final Student.GrdDiv grdDiv = Student.GrdDiv.getInstance(MapUtils.getIntValue(map, "grdDiv"));
        if (null == grdDiv) {
            return "不明な除籍（卒業）区分(grdDiv)";
        }

        final HomeRoom homeRoom = HomeRoom.getInstance(
                _cm.getCategory(),
                MapUtils.getString(map, "grade"),
                MapUtils.getString(map, "room")
        );
        if (null == homeRoom) {
            return "不明なホームルーム(grade,room)";
        }

        final Gender gender = Gender.getInstance(MapUtils.getString(map, "genderCd"));

        final String countflg = MapUtils.getString(map, "countFlg");
        final boolean countable = null == countflg || "1".equals(countflg);

        final Student student = Student.create(
                _cm.getCategory(),
                MapUtils.getString(map, "code"),
                MapUtils.getString(map, "inOutCd"),
                MapUtils.getString(map, "nameShow"),
                MapUtils.getString(map, "nameKana"),
                MapUtils.getString(map, "nameEnglish"),
                gender,
                grdDiv,
                KenjaMapUtils.getKenjaDateImpl(map, "grdDate"),
                homeRoom,
                MapUtils.getString(map, "attendNo"),
                countable
        );

        final CourseInfo courseInfo = CourseInfo.create(
                _cm.getCategory(),
                MapUtils.getString(map, "courseCd"),
                MapUtils.getString(map, "majorCd"),
                MapUtils.getString(map, "courseCode"),
                homeRoom.getGrade()
        );

        if (null != student) {
            student.setCourseInfo(courseInfo);
        }

        return student;
    }

    private static void whereConditionStudents(
            final StringBuffer sql,
            final Collection<String> students
    ) {
        sql.append(" ( t2.SCHREGNO in ( ");

        String comma = "";
        for (final String code : students) {
            sql.append(comma);
            sql.append("'").append(StringEscapeUtils.escapeSql(code)).append("'");
            // --
            comma = ",";
        }

        sql.append("))");
    }

    private static void whereConditionHomeRoom(
            final StringBuffer sql,
            final HomeRoom homeRoom
    ) {
        sql.append("(     t2.GRADE   ='").append(StringEscapeUtils.escapeSql(homeRoom.getGrade().getCode())).append("'");
        sql.append("  and t2.HR_CLASS='").append(StringEscapeUtils.escapeSql(homeRoom.getRoom())).append("'");
        sql.append(")");
    }
    
    private static void whereConditionSchoolKind(
            final StringBuffer sql,
            final String[] schoolKinds
    ) {
        sql.append(" ( t4.SCHOOL_KIND in ").append(SQLUtils.whereIn(true, schoolKinds)).append(" ");
        sql.append(")");
    }

    private void commonSQL(final StringBuffer sql) {
        sql.append("select");
        sql.append("    t1.SCHREGNO as code,");
        sql.append("    t1.INOUTCD as inOutCd,");
        sql.append("    t1.NAME_SHOW as nameShow,");
        sql.append("    t1.NAME_KANA as nameKana,");
        sql.append("    t1.NAME_ENG as nameEnglish,");
        sql.append("    t1.SEX as genderCd,");
        sql.append("    coalesce(int(t1.GRD_DIV), 0) as grdDiv,");
        sql.append("    t1.GRD_DATE as grdDate,");
        sql.append("    t2.COURSECD as courseCd,");         // 課程コード
        sql.append("    t2.MAJORCD as majorCd,");           // 学科コード
        sql.append("    t2.COURSECODE as courseCode,");     // コースコード
        sql.append("    t2.GRADE as grade,");
        sql.append("    t2.HR_CLASS as room,");
        sql.append("    t2.ATTENDNO as attendNo,");
        sql.append("    t3.COUNTFLG as countFlg");
        sql.append("  from ").append(TABLE_NAME).append(" t1");
        sql.append("    inner join ").append(TABLE_NAME2).append(" t2");
        sql.append("      on t1.SCHREGNO = t2.SCHREGNO");
        sql.append("    left join ").append(TABLE_NAME3).append(" t3");
        sql.append("      on t2.SCHREGNO = t3.SCHREGNO");
        sql.append("      and t2.YEAR = t3.YEAR");
        sql.append("      and t2.SEMESTER = t3.SEMESTER");
        sql.append("    left join ").append(SCHREG_REGD_GDAT).append(" t4");
        sql.append("      on t2.YEAR = t4.YEAR");
        sql.append("      and t2.GRADE = t4.GRADE");
        sql.append("  where t2.YEAR = ?");
        sql.append("  and   t2.SEMESTER = ?");
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final StringBuffer whereSQL = new StringBuffer(1024);
        final String or = " or ";
        String andor = " and (";
        if (null != _homeRoom) {
            // 年組指定 ※指定なしの場合もある
            whereSQL.append(andor);
            andor = or;
            whereConditionHomeRoom(whereSQL, _homeRoom);
        }

        if (null != _codes) {
            // 学籍番号のコレクション指定 ※指定なしの場合もある
            final int num = _codes.size();

            if (0 < num) {
                whereSQL.append(andor);
                andor = or;
                whereConditionStudents(whereSQL, _codes);
            }
        }

        if (!ArrayUtils.isEmpty(_schoolKinds) && null != _schoolKinds[0]) {
            // 校種指定 ※指定なしの場合もある
            whereSQL.append(andor);
            andor = or;
            whereConditionSchoolKind(whereSQL, _schoolKinds);
        }
        if (or.equals(andor)) {
            whereSQL.append(" ) ");
        }
        final StringBuffer sql = new StringBuffer(1024 + whereSQL.length());
        commonSQL(sql);
        sql.append(whereSQL);
        sql.append("  order by");
        sql.append("    t2.GRADE,");
        sql.append("    t2.HR_CLASS,");
        sql.append("    t2.ATTENDNO");
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
} // DaoStudent

// eof
