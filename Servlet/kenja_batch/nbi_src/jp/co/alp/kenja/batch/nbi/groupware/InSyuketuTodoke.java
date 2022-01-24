// kanji=漢字
/*
 * $Id: InSyuketuTodoke.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/06/27 14:54:42 - JST
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.opencsv.CSVReader;

/*
 * ソースの最後に賢者側DBのテーブル定義コメントあり。
 */

/**
 * 出欠届けCSV。
 * @author takaesu
 * @version $Id: InSyuketuTodoke.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class InSyuketuTodoke extends In {
    /*pkg*/static final Log log = LogFactory.getLog(InSyuketuTodoke.class);

    private String _file;

    public InSyuketuTodoke(final DB2UDB db, final Param param, final String title) {
        super(db, param, title);

        _file = getFileName();
    }

    public void doIt() throws IOException, SQLException {
        final Reader inputStreamReader = new InputStreamReader(new FileInputStream(_file), Param.encode);
        final CSVReader reader = new CSVReader(new BufferedReader(inputStreamReader));
        try {
            final List list = reader.readAll();
            log.info("読み込み件数=" + list.size());

            // DBに入れる
            toDb(list);
        } catch (final IOException e) {
            log.fatal("CSVが読込めない:" + _file, e);
            throw e;
        }
    }

    private void toDb(final List list) throws SQLException {
        final ScalarHandler scalarHandler = new ScalarHandler();

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();
            if (invalid(line)) {
                continue;
            }
            final Petition petition = createPetition(line);

            int seq = 1;
            try {
                final String seqSql = getSeqSql();
                final Integer maxSeq = (Integer) _qr.query(_db.conn, seqSql, scalarHandler);
                if (null != maxSeq) {
                    seq = maxSeq.intValue() + 1;
                }
            } catch (final SQLException e) {
                log.error("受付番号の最大値取得でエラー:" + petition, e);
                throw e;
            }

            insertTables(petition, seq);
            log.debug("受付番号(SeqNo)=" + seq + " で2つのテーブル(attend_petition_hdat, attend_petition_dat)にINSERTした。" + petition);
        }
        _db.commit();
    }

    private void insertTables(final Petition petition, int seq) throws SQLException {
        final String sql0 = "INSERT INTO attend_petition_hdat VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";
        final Object[] params0 = petition.toArray0(seq);
        try {
            _qr.update(_db.conn, sql0, params0);
        } catch (final SQLException e) {
            log.fatal("出欠届けヘッダーデータのINSERTでエラー!:" + sql0, e);
            throw e;
        }

        final String sql1 = "INSERT INTO attend_petition_dat VALUES (?,?,?,?,?,?,?,?,?,current timestamp)";
        final Object[] params1 = petition.toArray1(seq);
        try {
            _qr.update(_db.conn, sql1, params1);
        } catch (final SQLException e) {
            log.fatal("出欠届けデータのINSERTでエラー!:" + sql1, e);
            throw e;
        }
    }

    private String getSeqSql() {
        final String sql;
        sql = "SELECT"
            + "  max(seqno) as maxseq"
            + " FROM"
            + "  attend_petition_hdat"
            + " WHERE"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private boolean invalid(final String[] line) {
        if (StringUtils.isEmpty(line[7])) {//TODO: 出欠席コードが「1:欠席」の時は1日全体が欠席なので、校時は入れていないとの事。'08.7.10米田氏のメールより
            return true;
        }
        return false;
    }

    private Petition createPetition(final String[] line) {
        /*
         * 0: 学校識別コード
         * 1: 出欠席年月日
         * 2: 学籍番号
         * 3: 出席番号
         * 4: 組名称
         * 5: 出欠席コード
         * 6: 理由コード
         * 7: 校時
         */
        final String date = line[1];
        final String schregno = line[2];
        final String attendCd = line[5];
        final String reasonCd = line[6];
        final String periodCd = line[7];

        final Petition petition = new Petition(date, periodCd, schregno, attendCd, reasonCd);
        return petition;
    }

    private String getFileName() {
        final String fileName;
        if (null == _param._debugFileName) {
            final String ymd = new SimpleDateFormat("yyyyMMdd").format(new Date());
            fileName = "group" + ymd + ".csv";
        } else {
            fileName = _param._debugFileName;
        }
        return _param.getFullPath(fileName);
    }

    private class Petition {
        private final String _date;
        private final String _periodCd;
        private final String _schregno;
        /** 出欠席コード. 1=欠席, 2=遅刻, 3=早退 */
        private final String _attendCd;
        /** 理由コード. 1=公欠, 2=忌引, 3=病欠 */
        private final String _reasonCd;

        public Petition(
                final String date,
                final String periodCd,
                final String schregno,
                final String attendCd,
                final String reasonCd
        ) {
            final String yyyy = date.substring(0, 4);
            final String mm = date.substring(4, 6);
            final String dd = date.substring(6);
            _date = yyyy + "-" + mm + "-" + dd;
            _periodCd = periodCd;
            _schregno = schregno;
            _attendCd = attendCd;
            _reasonCd = reasonCd;
        }

        private Object[] toArray0(final int seq) {
            final Integer seqNo = new Integer(seq);
            final Object[] rtn = {
                    _param._year,
                    seqNo,
                    _schregno,
                    "2",    // 連絡元区分。0=保護者、1=生徒、2=その他
                    null,   // 連絡元
                    "0",    // 返電の要・不要。0=連絡不要、1=連絡必要
                    _param.getDate() + " 00:00:00",   // 受付日時
                    GroupWareServerString,
                    _date,
                    _periodCd,
                    _date,
                    _periodCd,
                    getDiCd(),
                    null,
                    GroupWareServerString,
            };
            return rtn;
        }

        private Object[] toArray1(final int seq) {
            final Integer seqNo = new Integer(seq);
            final Object[] rtn = {
                    _param._year,
                    seqNo,
                    _schregno,
                    _date,
                    _periodCd,
                    getDiCd(),
                    null,
                    "0",    // 実施区分。0=出欠未、1=出欠済み
                    GroupWareServerString,
            };
            return rtn;
        }

        /**
         * 賢者の勤怠コードを得る。
         * @return 賢者の勤怠コード
         */
        private String getDiCd() {
            // TODO: 適切に変換せよ
            return _reasonCd;
        }

        public String toString() {
            return _date + " " + _periodCd + "/" + _schregno;
        }
    }
} // InSyuketuTodoke

/*
    puma /tmp% db2 describe table attend_petition_hdat
                                   タイプ・
    列名                           スキーマ  タイプ名           長さ    位取り NULL
    ------------------------------ --------- ------------------ -------- ----- ------
    YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
    SEQNO                          SYSIBM    INTEGER                   4     0 いいえ
    SCHREGNO                       SYSIBM    VARCHAR                   8     0 はい
    CONTACTERDIV                   SYSIBM    VARCHAR                   1     0 はい
    CONTACTER                      SYSIBM    VARCHAR                  90     0 はい
    CALLBACK                       SYSIBM    VARCHAR                   1     0 はい
    FIRSTDATE                      SYSIBM    TIMESTAMP                10     0 はい
    FIRSTREGISTER                  SYSIBM    VARCHAR                   8     0 はい
    FROMDATE                       SYSIBM    DATE                      4     0 はい
    FROMPERIOD                     SYSIBM    VARCHAR                   1     0 はい
    TODATE                         SYSIBM    DATE                      4     0 はい
    TOPERIOD                       SYSIBM    VARCHAR                   1     0 はい
    DI_CD                          SYSIBM    VARCHAR                   2     0 はい
    DI_REMARK                      SYSIBM    VARCHAR                  30     0 はい
    REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
    UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
    
      16 レコードが選択されました。
    
    puma /tmp% db2 describe table attend_petition_dat
    
                                   タイプ・
    列名                           スキーマ  タイプ名           長さ    位取り NULL
    ------------------------------ --------- ------------------ -------- ----- ------
    YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
    SEQNO                          SYSIBM    INTEGER                   4     0 いいえ
    SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
    ATTENDDATE                     SYSIBM    DATE                      4     0 いいえ
    PERIODCD                       SYSIBM    VARCHAR                   1     0 いいえ
    DI_CD                          SYSIBM    VARCHAR                   2     0 はい
    DI_REMARK                      SYSIBM    VARCHAR                  30     0 はい
    EXECUTED                       SYSIBM    VARCHAR                   1     0 はい
    REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
    UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
    
      10 レコードが選択されました。
    
    puma /tmp%
 */
// eof
