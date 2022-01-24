// kanji=漢字
/*
 * $Id: InJikanwariHenkou.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/06/27 14:53:13 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import java.sql.Date;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.nbi.groupware.domain.Schedule;
import jp.co.alp.kenja.batch.opencsv.CSVReader;

/**
 * 時間割変更CSV。
 * @author takaesu
 * @version $Id: InJikanwariHenkou.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class InJikanwariHenkou extends In {
    /*pkg*/static final Log log = LogFactory.getLog(InJikanwariHenkou.class);

    private final static String _FILE = "group0601.csv";

    /* CSVフォーマット
     *  1: 学校識別コード
     *  2: 時間割変更種別(1=授業変更, 2=曜日の交換, 3=曜日の変更, 4=校時の一括交換, 5=校時の一括変更, 6=校時の一括削除, 7=授業の交換)
     *  3: 繰り上げ繰り下げコード(時間割変更種別=校時の一括削除(6)の場合有効。0=処理なし(削除のみ), 1=繰り上げ, 2=繰り下げ)
     *  4: 学校コード
     *  --- 変更前
     *  5: 日付//TAKAESU:YYYYMMDD
     *  6: 曜日
     *  7: 校時
     *  8: 組名称
     *  9: 群コード
     * 10: 科目コード
     * 11: 職員コード
     *  --- 変更後
     * 12: 日付
     * 13: 曜日
     * 14: 校時
     * 15: 組名称
     * 16: 群コード
     * 17: 科目コード
     * 18: 職員コード
     */
    //TAKAESU: 『授業の変更交換コード(0=交換なし, 1=同日交換, 2=他日交換)』って何?FAXでもらった資料に書いてある

    public InJikanwariHenkou(DB2UDB db, Param param, String title) {
        super(db, param, title);
    }

    void doIt() throws IOException, SQLException {
        final String fullPathFile = _param.getFullPath(_FILE);
        final Reader inputStreamReader = new InputStreamReader(new FileInputStream(fullPathFile), Param.encode);
        final CSVReader reader = new CSVReader(new BufferedReader(inputStreamReader));
        try {
            final List list = reader.readAll();
            log.info("読み込み件数=" + list.size());

            // DBに入れる
            toDb(list);
        } catch (final IOException e) {
            log.fatal("CSVが読込めない:" + _FILE, e);
            throw e;
        }
    }

    private void toDb(final List list) throws SQLException {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();
            if (invalid(line)) {
                continue;
            }
            log.debug(line);
            
        }
    }

    private boolean invalid(final String[] line) {
        if ("6".equals(line[1])) {
            if (line[7].length() != 1) {
                return true;
            }
            if ("012".indexOf(line[7]) == -1) {
                return true;
            }
        }
        return false;
    }

    private void deleteTables() throws SQLException {
        final String sql = "DELETE FROM sch_chr_dat WHERE executedate=? AND periodcd=? AND chaircd=?";
        try {
            _qr.update(_db.conn, sql);
        } catch (final SQLException e) {
            log.fatal("時間割削除でエラー");
            throw e;
        }
    }

    private void insertTables() throws SQLException {
        final String sql = "INSERT INTO sch_chr_dat VALUES (?,?,?,?,?,?,?,?,?,current timestamp)";
    }

    private class MySchedule extends Schedule {
        /** データ区分. 0=基本時間割からセット, 1=通常時間割でセット, 2=テスト時間割でセット */
        private String _dataDiv;

        /** 出欠確認者コード. */
        private final String _attestor;

        public MySchedule(final Date date, final String periodCd, final String chairCd, final boolean executed, String attestor) {
            super(date, periodCd, chairCd, executed);
            _attestor = attestor;
        }
    }
} // InJikanwariHenkou

// eof
