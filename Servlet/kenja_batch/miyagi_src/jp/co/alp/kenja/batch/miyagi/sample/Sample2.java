// kanji=漢字
/*
 * $Id: Sample2.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/29 10:37:24 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.miyagi.sample;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.miyagi.db.PostgreSQL;

public class Sample2 {

    public static void main(final String[] args) {

        Database db2 = new DB2UDB("//jaguar:50000/miyadb01", "db2inst1", "db2inst1", DB2UDB.TYPE2);
        Database pg = new PostgreSQL("//192.168.50.89/renkei", "renkei", "renkei");

        try {
            db2.open();
            pg.open("renkei", "renkei");
            selectKnj(db2);
            // db2.query("SELECT year, semester FROM semester_mst WHERE '2008-01-01' BETWEEN sdate AND edate");
            // db2.commit();

            select(pg);
            // update(pg);

            pg.close();
            db2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void update(final Database pg) throws SQLException {
        int count;

        // count =
        // pg.executeUpdate("INSERT INTO k_school_info (id, year, schoolcd, school_name, school_name_kana, school_type, course_cd, attendance_flag, kenja_url, updated) VALUES (nextval('k_school_info_id_seq'), 2014, '4999999', 'hoge1', 'hoge1', 4, 1, 0, 'http://kyomu1.dc2.myswan.ne.jp/shibata-nourin/', '2015-04-01 00:00:00')");
        // System.out.println(count + "件追加。");
        count = pg
                .executeUpdate("INSERT INTO k_school_info (year, schoolcd, school_name, school_name_kana, school_type, course_cd, attendance_flag, kenja_url, updated) VALUES (2014, '4999999', 'hoge1', 'hoge1', 4, 1, 0, 'http://kyomu1.dc2.myswan.ne.jp/shibata-nourin/', '2015-04-01 00:00:00')");
        System.out.println(count + "件追加。");

        count = pg.executeUpdate("UPDATE k_school_info SET school_name='hoge2' WHERE year=2014 and schoolcd='4999999' and course_cd=1");
        System.out.println(count + "件更新。");

        count = pg.executeUpdate("DELETE FROM k_school_info WHERE year=2014 and schoolcd='4999999' and course_cd=1");
        System.out.println(count + "件削除。");

        pg.commit();
    }

    private static void select(final Database pg) throws SQLException {
        pg.query("SELECT max(id) AS MAX_ID FROM k_staff_info");
        ResultSet rs = pg.getResultSet();
        while (rs.next()) {
            String max_id = rs.getString("MAX_ID");
            System.out.println("year=" + max_id);
        }
        pg.commit();
        DbUtils.closeQuietly(null, null, rs);
    }

    private static void selectKnj(final Database db2) throws SQLException {
        db2.query("SELECT year, semester FROM semester_mst WHERE semester != '9' AND '2012-06-03' BETWEEN sdate AND edate");
        ResultSet rs = db2.getResultSet();
        while (rs.next()) {
            String year = rs.getString("year");
            String semester = rs.getString("semester");
            System.out.println("year=" + year + ", semester=" + semester);
        }
        db2.commit();
        DbUtils.closeQuietly(null, null, rs);
    }
}
// eof
