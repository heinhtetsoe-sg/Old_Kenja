// kanji=漢字
/*
 * $Id: DaoBatchStudentOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.absencehigh;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.dao.query.DaoStudent;
import jp.co.alp.kenja.common.dao.query.DaoStudentTransfer;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.CourseInfo;
import jp.co.alp.kenja.common.domain.Gender;
import jp.co.alp.kenja.common.domain.HomeRoom;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 年間の生徒を読み込む。
 * @version $Id: DaoBatchStudentOfYear.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class DaoBatchStudentOfYear extends AbstractDaoLoader<Student> {
    /** T93/学籍基礎マスタ/テーブル名 */
    public static final String TABLE_NAME = DaoStudent.TABLE_NAME;
    /** T96/学籍在籍データ/テーブル名 */
    public static final String TABLE_NAME2 = DaoStudent.TABLE_NAME2;
    /** 学籍在籍詳細 /テーブル名 */
    public static final String TABLE_NAME3 = DaoStudent.TABLE_NAME3;

    private static final Log log = LogFactory.getLog(DaoBatchStudentOfYear.class);
    private static final DaoBatchStudentOfYear INSTANCE = new DaoBatchStudentOfYear();
    private static final Map<String, Map<String, HomeRoom>> STUDENT_HOMEROOMS = new HashMap<String, Map<String, HomeRoom>>();

    private HomeRoom _homeRoom;
    private Collection<String> _codes;


    /*
     * コンストラクタ。
     */
    private DaoBatchStudentOfYear() {
        super(log);
    }

    /*
     * インスタンスを得る。
     * @param codes 生徒の学籍番号のコレクション
     * @param homeRoom 年組
     * @return インスタンス
     */
    private static DaoBatchStudentOfYear getInstance(
            final HomeRoom homeRoom,
            final Collection<String> codes
    ) {
        INSTANCE._homeRoom = homeRoom;
        INSTANCE._codes = codes;
        return INSTANCE;
    }

    /**
     * インスタンスを得る。
     * @param homeRoom 年組
     * @return インスタンス
     */
    public static DaoBatchStudentOfYear getInstance(
            final HomeRoom homeRoom
    ) {
        return getInstance(homeRoom, null);
    }

    /**
     * インスタンスを得る。
     * @param codes 生徒の学籍番号のコレクション
     * @return インスタンス
     */
    public static DaoBatchStudentOfYear getInstance(
            final Collection<String> codes
    ) {
        return getInstance(null, codes);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Student> getInstance() {
        return getInstance(null, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 生徒のホームルームリストに学期のホームルームを追加する
     * @param schregno 生徒の学籍番号
     * @param semester 学期コードの文字列
     * @param homeroom 学期のホームルーム
     */
    public static void addHomeRoom(final String schregno, final String semester, final HomeRoom homeroom) {
        if (!STUDENT_HOMEROOMS.containsKey(schregno)) {
            STUDENT_HOMEROOMS.put(schregno, new HashMap<String, HomeRoom>());
        }
        final Map<String, HomeRoom> studentHomeRooms = STUDENT_HOMEROOMS.get(schregno);
        studentHomeRooms.put(semester, homeroom);
    }

    /**
     * 生徒の学期のホームルームを得る
     * @param semester 学期コードの文字列
     * @param student 生徒
     * @return ホームルーム
     */
    public static HomeRoom getHomeRoom(final String semester, final Student student) {
        if (!STUDENT_HOMEROOMS.containsKey(student.getCode())) {
            return null;
        }
        final Map<String, HomeRoom> studentHomeRooms = STUDENT_HOMEROOMS.get(student.getCode());
        return studentHomeRooms.get(semester);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

        final String semesterCd = MapUtils.getString(map, "semester");
        final HomeRoom homeRoom = DaoBatchHomeRoomOfYear.getHomeRoom(semesterCd, MapUtils.getString(map, "grade"), MapUtils.getString(map, "room"));

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
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // of year
        final Student s = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "code"));
        final String schregno = (s == null) ? MapUtils.getString(map, "code") : s.getCode();

        addHomeRoom(schregno, semesterCd, homeRoom);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return student;
    }

    private static void whereCondition(
            final StringBuffer sql,
            final Collection<String> students
    ) {
        sql.append("  and t2.SCHREGNO in ( ");

        String comma = "";
        for (final String code : students) {
            sql.append(comma);
            sql.append("'").append(StringEscapeUtils.escapeSql(code)).append("'");
            // --
            comma = ",";
        }

        sql.append(")");
    }

    private static void whereCondition(
            final StringBuffer sql,
            final HomeRoom homeRoom
    ) {
        sql.append("  and t2.GRADE   ='").append(StringEscapeUtils.escapeSql(homeRoom.getGrade().getCode())).append("'");
        sql.append("  and t2.HR_CLASS='").append(StringEscapeUtils.escapeSql(homeRoom.getRoom())).append("'");
    }

    private void commonSQL(final StringBuffer sql) {
        sql.append("select");
        sql.append("    t2.SEMESTER as semester,");
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
        sql.append("  where t2.YEAR = ?");
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        final StringBuffer sql;

        if (null != _homeRoom) {
            // 年組指定 ※学籍番号のコレクションは無視する
            sql = new StringBuffer(1024);
            commonSQL(sql);
            whereCondition(sql, _homeRoom);
        } else {
            // 学籍番号のコレクション指定 ※指定なしの場合もある
            final int num = (null == _codes) ? 0 : _codes.size();

            sql = new StringBuffer(1024 + (num * 16));
            commonSQL(sql);
            if (0 < num) {
                whereCondition(sql, _codes);
            }
        }

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
        };
    }
} // DaoBatchStudentOfYear

// eof
