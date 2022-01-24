// kanji=����
/*
 * $Id: CsvSample1.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/01/28 15:12:27 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CSV��DB2 �̑��݂��������B
 * @author takaesu
 * @version $Id: CsvSample1.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class CsvSample1 {
    /*pkg*/static final Log log = LogFactory.getLog(CsvSample1.class);

    final File _csv = new File("csv_test.csv"); // CSV�f�[�^�t�@�C��
    List _list = new ArrayList();

    public static void main(final String[] args) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        final CsvSample1 main = new CsvSample1(args);

        log.fatal("CSV�t�@�C����DB�ɓ����B");
        main.csvToDb();

        log.fatal("DB�̓��e��CSV�t�@�C�����B");
        main.dbToCsv();
    }

    public CsvSample1(final String[] args) {
        final String currentDir = new File( "." ).getAbsoluteFile().getParent();
        log.fatal("�J�����g�f�B���N�g��=" + currentDir);
    }

    public void dbToCsv() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        final List list = new ArrayList();

        // DB�����荞��
        final DB2UDB db2 = dbOpen();
        db2.query(selectSql());
        final ResultSet rs = db2.getResultSet();
        while(rs.next()) {
            final String type = rs.getString("SCHOOLING_TYPE");
            final String name = rs.getString("NAME");
            System.err.println("-->" + type + ", " + name);

            final String[] buf = new String[2];
            buf[0] = type;
            buf[1] = name;
            list.add(buf);
        }
        db2.commit();
        DbUtils.closeQuietly(null, null, rs);

        // CSV�t�@�C���ɏ���
//        final Writer fileWriter = new FileWriter("output.csv");
        final Writer fileWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream("output.csv"), "SJIS"));  // Linux��œ���������z��

        final CSVWriter writer = new CSVWriter(fileWriter, ',', '\0');
        writer.writeAll(list);
        writer.close();
    }

    public void csvToDb() throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // CSV����荞��
//        final CSVReader reader = new CSVReader(new FileReader(_csv));
        final BufferedReader hoge = new BufferedReader(new InputStreamReader(new FileInputStream(_csv), "SJIS"));   // Linux��œ���������z��
        final CSVReader reader = new CSVReader(hoge);
        _list = reader.readAll();

        // DB�ɓ����
        final DB2UDB db2 = dbOpen();
        db2.query("select * from SCHOOLING_TYPE_MST");  // naopackage �Ƀo�O�������

        db2.executeUpdate(deleteSql());

        int keyIndex = 0;
        for (final Iterator it = _list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();

            System.err.println(">>" + Arrays.asList(line));

            final String key = "0" + keyIndex;
            db2.executeUpdate(insertSql(key, line[0]));
            keyIndex++;
        }

        db2.commit();
        db2.close();

        // CSV��BACKUP�t�H���_�Ɉړ�
    }

    private DB2UDB dbOpen() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        final DB2UDB db2 = new DB2UDB("//withus:50000/WITESTDB", "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db2.open();
        return db2;
    }

    private String insertSql(final String key, final String value) {
        final String sql;
        sql = "insert into SCHOOLING_TYPE_MST"
            + " values("
            + "'" + key + "',"
            + "'99',"
            + "'" + value + "',"
            + "'takaesu',"
            + "current timestamp"
            + ")"
            ;
        return sql;
    }

    private String deleteSql() {
        return "delete from SCHOOLING_TYPE_MST where SCHOOLING_DIV='99'";
    }

    private String selectSql() {
        return "select * from SCHOOLING_TYPE_MST";
    }
} // CsvSample1

// eof
