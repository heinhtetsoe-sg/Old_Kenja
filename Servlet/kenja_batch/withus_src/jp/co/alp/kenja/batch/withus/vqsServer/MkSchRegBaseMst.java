// kanji=漢字
/*
 * $Id: MkSchRegBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/01 0:20:23 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 生徒情報テーブル。
 * @author takaesu
 * @version $Id: MkSchRegBaseMst.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkSchRegBaseMst extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkSchRegBaseMst.class);

    /** 課程マスタ */
    private Map _courseMst;
    /** 学科マスタ */
    private Map _majorMst;
    /** コースマスタ */
    private Map _courseCodeMst;

    /** 学生区分マスタ */
    private Map _studentDivMst;

    /** 所属マスタ */
    private Map _belongingMst;

    /** 生徒の都道府県コード */
    private Map _studentAddress;

    public MkSchRegBaseMst(final Param param, final Database knj, final Database vqs, final String title) throws SQLException {
        super(param, knj, vqs, title);

        loadMasterData();

        final List list = loadData(knj);
        log.debug("賢者:データ数=" + list.size());

        saveVqs(list, vqs);
    }

    private void saveVqs(final List list, final Database vqs) throws SQLException {
        int count = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] data = (String[]) it.next();
            final String insertSql = createInsertSql();
            try {
                count += _runner.update(vqs.conn, insertSql, data);
            } catch (final SQLException e) {
                final int errorCode = e.getErrorCode();
                final String state = e.getSQLState();
                if (0 == errorCode && UNIQUE_VIOLATION.equals(state)) {
                    vqs.conn.rollback();    // ★

                    final String schregno = data[0];
                    final String updateSql = "UPDATE schreg_base_mst SET updated=current_timestamp WHERE schregno='" + schregno + "'";
                    try {
                        count += _runner.update(vqs.conn, updateSql);
                    } catch (final SQLException e1) {
                        log.error("VQSに更新でエラー:" + insertSql);
                        throw e;
                    }
                    vqs.commit();   // TODO: リファクタせよ! ★で rollback しているせいだ！
                } else {
                    log.error("VQSに挿入でエラー:" + insertSql);
                    throw e;
                }
            }
        }
        vqs.commit();
        log.debug("VQS:データ数=" + count);
    }

    private String createInsertSql() {
        final String sql;

        sql = "INSERT INTO schreg_base_mst VALUES"
            + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)";

        return sql;
    }

    private List loadData(final Database knj) throws SQLException {
        final List rtn = new ArrayList();
        ResultSet rs = null;
        try {
            knj.query(sql());
            rs = knj.getResultSet();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String prefCd = (String) _studentAddress.get(schregno);

                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String studentDiv = rs.getString("student_div");
                final String courseDiv = rs.getString("course_div");
                final String studentDivName = (String) _studentDivMst.get(courseDiv + studentDiv);

                final String belonging = rs.getString("grade");
                final String grdDate = rs.getString("grd_date");

                final String[] fields = {
                        schregno,
                        rs.getString("name"),
                        rs.getString("name_show"),
                        rs.getString("name_kana"),
                        rs.getString("sex"),
                        prefCd, // 都道府県コード
                        coursecd,
                        (String) _courseMst.get(coursecd),  // 課程名
                        majorcd == null ? EMPTY : majorcd.substring(1),
                        (String) _majorMst.get(coursecd + majorcd), // 学科名
                        coursecode == null ? EMPTY : coursecode.substring(2),    // TAKAESU: 4byte?
                        (String) _courseCodeMst.get(coursecode),    // コース名
                        studentDiv == null ? EMPTY : studentDiv.substring(1),    // 学生区分 // TODO: テーブル上は 1バイト?
                        studentDivName, // 学生区分名
                        belonging,  // 所属
                        (String) _belongingMst.get(belonging),  // 所属名
                        rs.getString("annual"), // 年次(postgre側は integer型だがinsert出来た)
                        grdDate == null ? "INFINITY" : grdDate,    // 除籍日付    // TAKAESU: postgreの用語、infinity をここで使ってはいけない
                };
                rtn.add(nullToEmpty(fields));
            }
        } catch (final SQLException e) {
            log.error("生徒情報テーブルでエラー", e);
            throw e;
        } finally {
            knj.commit();
            DbUtils.closeQuietly(null, null, rs);
        }
        return rtn;
    }

    private void loadMasterData() throws SQLException {
        _studentAddress = loadStudentAddress();

        // 課程、学科、コース
        _courseMst = DbToMap(_knj, "coursecd", "courseabbv", "SELECT coursecd, courseabbv FROM course_mst");
        _majorMst = DbToMap(_knj, "cd", "majorabbv", "SELECT coursecd || majorcd as cd, majorabbv FROM major_mst");
        _courseCodeMst = DbToMap(_knj, "coursecode", "coursecodename", "SELECT coursecode, coursecodename FROM coursecode_mst");

        // 学生区分
        _studentDivMst = DbToMap(_knj, "cd", "name", "SELECT course_div || student_div as cd, name FROM studentdiv_mst");

        // 所属
        _belongingMst = DbToMap(_knj, "belonging_div", "schoolname3", "SELECT belonging_div, schoolname3 FROM belonging_mst");
    }

    private Map loadStudentAddress() throws SQLException {
        // TAKAESU: SQL文、もっと簡潔にしたい
        final String sql = "SELECT t2.schregno, t2.pref_cd FROM "
            + "(SELECT schregno, max(issuedate) as issuedate FROM schreg_address_dat GROUP BY schregno) t1 "
            + "INNER JOIN schreg_address_dat t2 ON t1.schregno=t2.schregno AND t1.issuedate=t2.issuedate"
            ;

        return DbToMap(_knj, "schregno", "pref_cd", sql);
    }

    public static Map DbToMap(final Database db, final String key, final String val, final String sql) throws SQLException {
        final Map rtn = new HashMap();

        ResultSet rs = null;
        db.query(sql);
        rs = db.getResultSet();
        while(rs.next()) {
            final String code = rs.getString(key);
            final String name = rs.getString(val);
            rtn.put(code, name);
        }

        return rtn;
    }

    private String sql() {
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.name,"
            + "  t1.name_show,"
            + "  t1.name_kana,"
            + "  t1.sex,"
            + "  t2.coursecd,"
            + "  t2.majorcd,"
            + "  t2.coursecode,"
            + "  t2.course_div,"
            + "  t2.student_div,"
            + "  t2.grade,"
            + "  t2.annual,"
            + "  t1.grd_date"
            + " FROM"
            + "  schreg_base_mst t1,"
            + "  schreg_regd_dat t2,"
            + "  schreg_regd_hdat t3"
            + " WHERE"
            + "  t1.schregno = t2.schregno AND"
            + "  t2.year = t3.year AND"
            + "  t2.semester = t3.semester AND"
            + "  t2.grade = t3.grade AND"
            + "  t2.hr_class = t3.hr_class AND"
            + "  t2.year='" + _param._year + "' AND"
            + "  t2.semester='" + _param._semester + "' AND"
            + "  (t2.student_div='03' OR t2.seat_col='1')"    // student_div="03:のみ生"
            ;
        return sql;
    }
} // MkSchRegBaseMst

// eof
