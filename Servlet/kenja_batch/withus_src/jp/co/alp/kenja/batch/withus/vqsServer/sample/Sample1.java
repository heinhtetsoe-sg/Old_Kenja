// kanji=漢字
/*
 * $Id: Sample1.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/29 10:37:24 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer.sample;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;

import jp.co.alp.kenja.batch.withus.vqsServer.db.PostgreSQL;

public class Sample1 {

    public static void main(final String[] args) {

        Database db2 = new DB2UDB("//withus:50000/witestdb", "db2inst1", "db2inst1", DB2UDB.TYPE2);
        Database pg = new PostgreSQL("//192.168.50.140/hoge", "takaesu", "");

        try {
            db2.open();
            db2.query("SELECT year, semester FROM semester_mst WHERE '2008-01-01' BETWEEN sdate AND edate");
            db2.commit();

            pg.open();
            select(pg);
            update(pg);

            pg.close();
            db2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void update(Database pg) throws SQLException {
        int count;

        count = pg.executeUpdate("INSERT INTO hoge1 VALUES ('200812', 'jiro', '0120-123-456', 98)");
        System.out.println(count + "件追加。");

        count = pg.executeUpdate("UPDATE hoge1 SET phone='xxxx' WHERE yyyymm='200812' AND customer='jiro'");
        System.out.println(count + "件更新。");

        count = pg.executeUpdate("DELETE FROM hoge1 WHERE yyyymm='200812' AND customer='jiro'");
        System.out.println(count + "件削除。");
    }

    private static void select(Database pg) throws SQLException {
        pg.query("SELECT * FROM hoge1");
        ResultSet rs = pg.getResultSet();
        while (rs.next()) {
            String yyyymm = rs.getString("yyyymm");
            String customer = rs.getString("customer");
            System.out.println("yyyymm=" + yyyymm + ", customer=" + customer);
        }
        pg.commit();
        DbUtils.closeQuietly(null, null, rs);
    }
}
// eof
