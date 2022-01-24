// kanji=漢字
/*
 * $Id: DaoMeibo.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2004/11/28 16:21:50 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.enums.MyEnum.Category;

/*
 * describe table CHAIR_STD_DAT
 *
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * CHAIRCD                        SYSIBM    VARCHAR                   7     0 いいえ
 * SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
 * APPDATE                        SYSIBM    DATE                      4     0 いいえ
 * APPENDDATE                     SYSIBM    DATE                      4     0 はい
 * ROW                            SYSIBM    VARCHAR                   2     0 はい
 * COLUMN                         SYSIBM    VARCHAR                   2     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     10 レコードが選択されました。
 */

/**
 * 講座の名簿を得る。
 * @author tamura
 * @version $Id: DaoMeibo.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class DaoMeibo {
    private static final Log log = LogFactory.getLog(DaoMeibo.class);
    private final DbConnection _dbcon;
    private final ControlMaster _cm;

    private Connection _con;
    private KenjaPS _ps;

    /**
     * コンストラクタ。
     * @param dbcon DB接続情報
     * @param cm コントロールマスタ
     */
    public DaoMeibo(
            final DbConnection dbcon,
            final ControlMaster cm
    ) {
        _dbcon = dbcon;
        _cm = cm;
    }

    /**
     * DBに接続し、psを作成する。
     * @throws SQLException SQL例外
     */
    public void open() throws SQLException {
        _con = _dbcon.getROConnection();
    }

    /**
     * DB接続を閉じる。
     */
    public void close() {
        DbUtils.closeQuietly(_ps);
        DbUtils.commitAndCloseQuietly(_con);
        _ps = null;
        _con = null;
    }

    /*
     */
    private static void whereCondition(final StringBuffer sql, final Collection<Chair> chairs) {
        sql.append("  and CHAIRCD in ( ");

        String comma = "";
        for (final Chair chair : chairs) {

            sql.append(comma);
            sql.append("'").append(StringEscapeUtils.escapeSql(chair.getCode())).append("'");
            // --
            comma = ",";
        }

        sql.append(")");
    }

    /*
     */
    private String getSql(final Set<Chair> chairs) {
        final int num = (null == chairs) ? 0 : chairs.size();

        final StringBuffer sql = new StringBuffer(128 + (num * 16));
        sql.append("select");
        sql.append("    CHAIRCD,");
        sql.append("    SCHREGNO");
        sql.append("  from  CHAIR_STD_DAT");
        sql.append("  where YEAR = ?");
        sql.append("    and SEMESTER = ?");
        sql.append("    and ? between APPDATE and APPENDDATE");

        if (0 < num) {
            whereCondition(sql, chairs);
        }

        sql.append("  group by CHAIRCD, SCHREGNO");

        return sql.toString();
    }

    /*
     */
    private static <T> Map<Chair, List<T>> createMap(final Set<Chair> keys) {
        final Map<Chair, List<T>> rtn = new HashMap<Chair, List<T>>();
        for (final Chair chair : keys) {
            rtn.put(chair, new LinkedList<T>());
        }
        return rtn;
    }

    /**
     * 講座の名簿（生徒のList)を得る。
     * @param date 名簿の日付
     * @param chairs 講座のSet
     * @return 講座毎の生徒のList。Map&lt;key=Chair,value=List&lt;Student&gt;&gt;
     * @throws SQLException SQL例外
     */
    public Map<Chair, List<Student>> getMeiboMapList(
            final KenjaDateImpl date,
            final Set<Chair> chairs
    ) throws SQLException {
        return getMeiboMapList(date, chairs, true);
    }

    /**
     * 講座の名簿（生徒のList)を得る。
     * @param date 名簿の日付
     * @param chairs 講座のSet
     * @param doActiveCheck 「在籍」チェック
     * @return 講座毎の生徒のList。Map&lt;key=Chair,value=List&lt;Student&gt;&gt;
     * @throws SQLException SQL例外
     */
    public Map<Chair, List<Student>> getMeiboMapList(
            final KenjaDateImpl date,
            final Set<Chair> chairs,
            final boolean doActiveCheck
    ) throws SQLException {
        final long start = System.currentTimeMillis();
        final Category category = _cm.getCategory();
        final Map<Chair, List<Student>> rtn = createMap(chairs);

        _ps = new KenjaPS(_con.prepareStatement(getSql(chairs)));
        _ps.clearParameters();

        final Set<Student> students = new HashSet<Student>();
        ResultSet rs = null;
        try {
            _ps.setString(1, _cm.getCurrentYearAsString());
            _ps.setString(2, _cm.getCurrentSemester().getCodeAsString());
            _ps.setDate(3, date);

            rs = _ps.executeQuery();
            while (rs.next()) {
                final Chair chair = Chair.getInstance(category, rs.getString(1));
                if (null == chair || !chairs.contains(chair)) {
                    continue;
                }

                List<Student> list = rtn.get(chair);
                if (null == list) {
                    list = new LinkedList<Student>();
                    rtn.put(chair, list);
                }

                final Student student = Student.getInstance(category, rs.getString(2));
                if (null == student) {
                    continue;
                }

                // チェックする場合、指定した日付に「在籍」しているか？
                if (doActiveCheck && !student.isActive(date)) {
                    continue;
                }
                list.add(student);
                students.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }

        final long elapsed = System.currentTimeMillis() - start;
        log.fatal(StringUtils.leftPad(String.valueOf(elapsed), 4) + "ミリ秒, 名簿取得:講座数=" + chairs.size() + ", 生徒数=" + students.size());

        return rtn;
    }

} // DaoMeibo

// eof
