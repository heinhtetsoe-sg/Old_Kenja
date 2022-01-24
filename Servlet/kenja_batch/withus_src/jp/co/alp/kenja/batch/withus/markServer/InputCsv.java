// kanji=漢字
/*
 * $Id: InputCsv.java 57802 2018-01-05 10:44:05Z yamashiro $
 *
 * 作成日: 2008/03/21 10:46:12 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.QueryRunner;
import jp.co.alp.kenja.batch.opencsv.CSVReader;
import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * 採点結果送信データCSVを取込んで、DBに格納。
 * @author takaesu
 * @version $Id: InputCsv.java 57802 2018-01-05 10:44:05Z yamashiro $
 */
public class InputCsv {
    /*pkg*/static final Log log = LogFactory.getLog(InputCsv.class);

    /** DBレコードをInsertする際の識別文字列 */
    public static final String MarkserverString = "MarkSvr";

    /** DBテーブルの REGISTERCD */
    public static final String StaffCd = "99999999";

    private final Param _param;
    private final DB2UDB _db;

    final QueryRunner _qr = new QueryRunner();  // バグ修正版の QueryRunner。

    public InputCsv(final DB2UDB db, final Param param) {
        _db = db;
        _param = param;
    }

    /**
     * CSVファイルを読込み、DBに格納する。
     */
    public void doIt() throws IOException {
        // CSVを取り込み
        final String file = _param.getFile();
        final Reader inputStreamReader = new InputStreamReader(new FileInputStream(file), Param.encode);
        final CSVReader reader = new CSVReader(new BufferedReader(inputStreamReader));
        try {
            final List list = reader.readAll();
            log.info("読み込み件数=" + list.size());
            log.info("先頭行はスキップします。");

            // DBに入れる
            toDb(list);
        } catch (final IOException e) {
            log.fatal("CSVが読込めない:" + file, e);
            throw e;
        }
    }

    private void toDb(final List list) {
        boolean isFirstLine = true;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();
            if (isFirstLine) {
                isFirstLine = false;
                continue;   // 先頭行はヘッダーなのでスキップ
            }

            final SaitenKekka saitenKekka = createSaitenKekka(line);
            if (null == saitenKekka) {
                continue;
            }
            if (saitenKekka.isReport() && !saitenKekka.validScore()) {
                log.warn("初回得点と追試得点が両方空っぽ or 両方入っている。無視します。:" + saitenKekka);
                continue;
            }

            final String sql;
            final Object[] params;
            if (saitenKekka.isReport()) {
                if (!saitenKekka.isSecond()) {
                    sql = "INSERT INTO rec_report_dat VALUES (?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";
                    params = saitenKekka.toArrayOfInsert();
                } else {
                    sql = updateReportSql(saitenKekka);
                    params = saitenKekka.toArrayOfUpdate();
                }
            } else {
                // テストの場合
                sql = "INSERT INTO rec_test_dat VALUES (?,?,?,?,?,?,?,?,?,current timestamp)";
                params = saitenKekka.toArrayOfInsertOfTest();
            }

            try {
                final int cnt = _qr.update(_db.conn, sql, params);
                if (cnt != 1) {
                    log.warn("更新件数は1件のハズだが違う:" + cnt + ", sql=" + sql);
                }
            } catch (final SQLException e) {
                /* SQL0803N
                 *   INSERT ステートメント、UPDATE ステートメントの 1 つ以上の値、
                 *   または DELETE ステートメントによって行われた外部キーの更新は
                 *   1 次キー、固有制約または固有索引を伴う表ので重複行を作成するため、
                 *   有効ではありません。
                 * sqlcode:  -803(ベンダー固有の例外コード)
                 * sqlstate:  '23505'
                 */
                if (-803 == e.getErrorCode() && e.getSQLState().equals("23505")) {
                    log.warn("重複エラー。既にレコードがある。" + sql);
                } else {
                    log.fatal("SQLコマンド発行でエラー!", e);
                }
            }
        }
        _db.commit();
    }

    private SaitenKekka createSaitenKekka(final String[] line) {
        /*
         * 学校区分,生徒番号,学習拠点,クラス,入学日,
         * 氏名,学籍状態,コース,課程コード,課程,
         * 教育課程適用年度コード,教育課程,年度,教科コード,教科,
         * 科目コード,科目,履修期間コード,履修期間,履修課題種別コード,
         * 履修課題種別,実施番号,課題レベル,提出年月日,初回得点,
         * 追試得点,提出回数
         */
        final String schoolDiv = line[0];
        final String schregno = line[1];
        final String curriculumCd = Curriculum.getCurriculumCd(line[10]);
        final String year = line[12];
        final String classCd = line[13];
        final String subclassCd = line[15];
        final String risyu課題種別cd = line[19];
        final String jissiBangou = line[21];
        final String teisyutuYMD = line[23];
        final String firstScore = line[24];
        final String secondScore = line[25];
        final String staffCd = StaffCd;

        SaitenKekka saitenKekka = null;
        try {
            saitenKekka = new SaitenKekka(
                    schoolDiv,
                    schregno,
                    year,
                    curriculumCd,
                    classCd,
                    subclassCd,
                    risyu課題種別cd,
                    jissiBangou,
                    teisyutuYMD,
                    firstScore,
                    secondScore,
                    staffCd
            );
        } catch (final IllegalArgumentException e) {
            log.fatal("不正なデータ。", e);
        }
        return saitenKekka;
    }

    private String updateReportSql(final SaitenKekka kekka) {
        final String sql;
        sql = "UPDATE rec_report_dat SET"
            + " commited_date2=?,"
            + " commited_score2=?"
            + " WHERE year=?"
            + " AND classcd=?"
            + " AND curriculum_cd=?"
            + " AND subclasscd=?"
            + " AND schregno=?"
            + " AND report_seq=?";
        return sql;
    }

    private class SaitenKekka {
        private final String _schoolDiv;
        private final String _schregno;
        private final String _year;
        private final String _curriculumCd;
        private final String _classCd;
        private final String _subclassCd;
        private final String _risyu課題種別cd;

        /** 実施番号。 テストの時は 90=9月, 91=3月 */
        private final String _jissiBangou;
        private final String _teisyutuYMD;
        private final String _firstScore;
        private final String _secondScore;
        private final String _staffCd;

        private final boolean _isReport;

        public SaitenKekka(
                final String schoolDiv,
                final String schregno,
                final String year,
                final String curriculumCd,
                final String classCd,
                final String subclassCd,
                final String risyu課題種別cd,
                final String jissiBangou,
                final String teisyutuYMD,
                final String firstScore,
                final String secondScore,
                final String staffCd
        ) {
            if (classCd.length() != 2) {
                throw new IllegalArgumentException("教科コードが不正:" + classCd);
            }
            if (subclassCd.length() != 4) {
                throw new IllegalArgumentException("科目コードが不正:" + subclassCd);
            }
            if (teisyutuYMD.length() != 10) {
                throw new IllegalArgumentException("提出年月日が不正:" + teisyutuYMD);
            }

            _schoolDiv = schoolDiv;
            _schregno = schregno;
            _year = year;
            _curriculumCd = curriculumCd;
            _classCd = classCd;
            _subclassCd = classCd + subclassCd; // 教科コード+科目コード
            _risyu課題種別cd = risyu課題種別cd;
            _jissiBangou = jissiBangou;
            _teisyutuYMD = teisyutuYMD.replace('/', '-');
            _firstScore = firstScore;
            _secondScore = secondScore;
            _staffCd = staffCd;

            // validate
            if (classCd.length() != 2) {
                throw new IllegalArgumentException("教科コードが不正:" + classCd);
            }
            if (subclassCd.length() != 4) {
                throw new IllegalArgumentException("科目コードが不正:" + subclassCd);
            }

            if (!"0".equals(_schoolDiv) && !"1".equals(_schoolDiv)) {
                throw new IllegalArgumentException("学校区分が想定外:" + _schoolDiv);
            }
            if (!_param.getYear().equals(_year)) {
                throw new IllegalArgumentException("今年度以外:" + _year);
            }
            if (!"1".equals(_risyu課題種別cd) && !"2".equals(_risyu課題種別cd)) {
                throw new IllegalArgumentException("履修課題識別CDが 1 or 2 ではない:" + _risyu課題種別cd);
            }
            // TAKAESU: 科目マスタにあるかチェック(教科+課程+科目)
            // TAKAESU: 生徒は存在するかチェック
            // TAKAESU: テストの時は、追試得点があれば矛盾
            // TAKAESU: 初回得点、追試得点はいづれかにのみ値があるはず
            //
            _isReport = ("1".equals(_risyu課題種別cd)) ? true : false;
            if (!_isReport) {
                if (!"90".equals(jissiBangou) && !"91".equals(jissiBangou)) {
                    // 90=9月, 91=3月
                    throw new IllegalArgumentException("テストの時は実施番号は 90 or 91 のはずなのに違う:" + _risyu課題種別cd);
                }
            }
        }

        public boolean validScore() {
            if (StringUtils.isEmpty(_firstScore) && StringUtils.isEmpty(_secondScore)) {
                return false;
            }
            if (!StringUtils.isEmpty(_firstScore) && !StringUtils.isEmpty(_secondScore)) {
                return false;
            }
            return true;
        }

        /**
         * 初回得点があればTrue。
         * @deprecated
         * @return 追試のデータならFalse
         */
        public boolean isFirst() {
            return !StringUtils.isEmpty(_firstScore);
        }

        /**
         * 再提出(2回目)得点があれば true。
         * @return 再提出(2回目)得点
         */
        public boolean isSecond() {
            return !StringUtils.isEmpty(_secondScore);
        }

        public String getSecondScoreValue() {
            return getDbValue(_secondScore);
        }

        public String getFirstScoreValue() {
            return getDbValue(_firstScore);
        }

        private String getDbValue(String score) {
            if (StringUtils.isEmpty(score)) {
                return null;
            }
            return score;
        }

        /**
         * 開催回(REC_REPORT_DAT.REPORT_SEQ)を得る。
         * @return 開催回
         */
        public String getReportSeq() {
            return _jissiBangou;
        }

        /**
         * 月のコードを得る。
         * @return 月のコード
         */
        public String getMonth() {
            return "90".equals(_jissiBangou) ? "09" : "03";
        }

        /**
         * レポートのデータか?
         * @return レポートのデータならtrue
         */
        public boolean isReport() {
            return _isReport;
        }

        public String getType() {
            return isReport() ? "レポート" : "テスト";
        }

        public Object[] toArrayOfInsert() {
            final Object[] rtn = {
                    _year,
                    _classCd,
                    _curriculumCd,
                    _subclassCd,
                    _schregno,
                    getReportSeq(),
                    _teisyutuYMD,
                    null,
                    getFirstScoreValue(),
                    getSecondScoreValue(),
                    MarkserverString,
                    _staffCd,
            };
            return rtn;
        }

        public Object[] toArrayOfUpdate() {
            final Object[] rtn = {
                    _teisyutuYMD,
                    _secondScore,
                    _year,
                    _classCd,
                    _curriculumCd,
                    _subclassCd,
                    _schregno,
                    _jissiBangou,
            };
            return rtn;
        }

        public Object[] toArrayOfInsertOfTest() {
            final Object[] rtn = {
                    _year,
                    _classCd,
                    _curriculumCd,
                    _subclassCd,
                    _schregno,
                    getMonth(),
                    _firstScore,    // TODO: テストは常に初回得点か?
                    MarkserverString,
                    _staffCd,
            };
            return rtn;
        }

        public String toString() {
            return "schregno=" + _schregno + ", 科目CD=" + _subclassCd + ", 種類=" + getType();
        }
    }
} // InputCsv

// eof
