// kanji=漢字
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2009/03/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Period;
import jp.co.alp.kenja.batch.otr.domain.Semester;
import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * パラメータ。
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {

    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** SimpleDateFormat */
    static SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");
    /** プロパティーファイル名 */
    private static final String PROPSNAME = "OtrRead.properties";

    private DB2UDB _db;

    private final String _dbUrl;

    /** 起動した時刻 */
    private final int hour;
    /** 起動した時刻(分) */
    private final int minute;
    /** 起動した日付 */
    private final Date today;

    /** 時間割データを読み込む最も古い日付 */
    private KenjaDateImpl _oldestDate;
    /** 時間割データを読み込む日付 */
    private KenjaDateImpl _date;
    /** 時間割データを読み込む最大校時 */
    private Period _period;

    /** 更新日 */
    private String _update;

    /** 勤怠ファイル */
    private final File _kintaiFile;

    /** 学期 */
    private Semester _semester;

    /** 校時データ */
    private Map _periods;

    /** 校時タイムテーブルデータ */
    private Map _periodTimeTables;
    
    /** 同一データの削除フラグ */
    private boolean _deletesOldDataInDB;

    /** 勤怠が"出席"のデータの出力フラグ */
    private boolean _outputSeatedToDB;

    /**
     * コンストラクタ。
     * @param args 引数
     * @exception ParseException パース例外
     * @exception IOException 入出力例外
     */
    public Param(final String[] args) throws IOException, ParseException {
        if (args.length < 1) {
            log.error("Usage: java Main <//localhost:50000/dbname> ");
            throw new IllegalArgumentException("引数の数が違う");
        }

        // DBのURLを読み込む
        _dbUrl = args[0];

        // 校時タイムテーブルを読み込む
        _periodTimeTables = PeriodTimeTable.load();

        // デバッグ用
        if (args.length >= 4 && "-debug".equals(args[1])) {
            log.debug("debug");
            today = java.sql.Date.valueOf(args[2]);
            final String[] hourMinute = StringUtils.split(args[3], ':');
            hour = Integer.parseInt(hourMinute[0]);
            minute = Integer.parseInt(hourMinute[1]);
        } else {
            today = new Date();
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }
        // OTRが出力した勤怠ファイルをセットする
        final Properties properties = new Properties();
        final File propertyFile = new File(PROPSNAME);
        log.debug("propertyFile path = " + propertyFile.getAbsolutePath());
        properties.load(new FileInputStream(propertyFile));
        final String kintaiFilePath = properties.getProperty("KintaiFilePath");
        log.debug("勤怠ファイル path = " + kintaiFilePath);

        _kintaiFile = new File(kintaiFilePath);
        if (!_kintaiFile.isFile()) {
            throw new IllegalArgumentException("'" + _kintaiFile + "'はファイルではありません");
        }
        
        String deletesOldDataInDB = properties.getProperty("DeleteOldDataInDB");
        _deletesOldDataInDB = deletesOldDataInDB != null && "true".equals(deletesOldDataInDB.toLowerCase());
        
        String outputSeatedToDB = properties.getProperty("OutputSeatedToDB");
        _outputSeatedToDB = outputSeatedToDB != null && "true".equals(outputSeatedToDB.toLowerCase());
    }


    /**
     * DBのURLを得る。
     * @return DBURL
     */
    public String getDbUrl() {
        return _dbUrl;
    }

    /**
     * 勤怠ファイルを得る。
     * @return 勤怠ファイル
     */
    public File getKintaiFile() {
        return _kintaiFile;
    }

    /**
     * 古いデータを削除するか
     * @return 古いデータを削除するならtrue、そうでなければfalse
     */
    public boolean deletesOldDataInDB() {
        return _deletesOldDataInDB;
    }

    /**
     * 勤怠が"出席"のデータの出力フラグ
     * @return 勤怠が"出席"のデータを出力するならtrue、そうでなければfalse
     */
    public boolean outputsSeatedToDB() {
        return _outputSeatedToDB;
    }

    /**
     * 必要な情報をロード
     * @param db DB
     * @throws IOException IO例外
     * @throws SQLException SQL例外
     */
    public void load(final DB2UDB db) throws IOException, SQLException {
        _periods = Period.load(db);

        final BatchTime time = BatchTime.create(hour, minute);
        // プログラム起動時の校時判定については範囲の開始時間を考慮しない。(生徒の校時判定とは区別する)
        final PeriodTimeTable tt = getPeriodTimeTable(time, false);
        if (tt == null) {
            throw new IllegalArgumentException("時間の指定が間違っています。" + time);
        }
        _period = getPeriod(tt).getPrevious();
        _update = sdf_.format(today);
        log.debug("対象とする年月日:" + _update + " 校時:" + (_period == null ? "0" : _period.getCode()));
        _date = KenjaDateImpl.getInstance(today);

        _semester = Semester.load(db, _update);
    }

    /**
     * (学期マスタの)年度を得る。
     * @return 年度
     */
    public String getYear() {
        return _semester.getYear();
    }

    /**
     * (学期マスタの)学期を得る。
     * @return 学期
     */
    public String getSemester() {
        return _semester.getSemesterString();
    }

    /**
     * (学期マスタの)学期の開始日を得る。
     * @return 学期の開始日
     */
    public String getSemesterSdate() {
        return _semester.getSDate().toString();
    }

    /**
     * (学期マスタの)学期の終了日を得る。
     * @return 学期の終了日
     */
    public String getSemesterEdate() {
        return _semester.getEDate().toString();
    }

    /**
     * 時間割を読み込む日付を得る。
     * @return 日付
     */
    public KenjaDateImpl getTargetDate() {
        return KenjaDateImpl.getInstance(java.sql.Date.valueOf(_update));
    }
    
    /**
     * 時間割を読み込む最も古い日付を得る
     * @return 時間割を読み込む最も古い日付
     */
    public KenjaDateImpl getOldestDate() {
        return _oldestDate;
    }

    /**
     * 時間割を読み込む最も古い日付をセットする
     */
    public void setOldestDate(final KenjaDateImpl oldestDate) {
        _oldestDate = oldestDate;
    }

    /**
     * 時間割を読み込む最大の校時を得る。
     * @return 校時
     */
    public Period getTargetPeriod() {
        return _period;
    }

    /**
     * 校時のタイムテーブルを得る。
     * @param period 校時
     * @return 校時のタイムテーブル
     */
    public PeriodTimeTable getPeriodTimeTable(final Period period) {
        return getPeriodTimeTable(period.getCode());
    }


    /**
     * 校時コードの校時タイムテーブルを得る。
     * @param cd 校時コード
     * @return 校時コードのタイムテーブル
     */
    public PeriodTimeTable getPeriodTimeTable(final String cd) {
        return (PeriodTimeTable) _periodTimeTables.get(cd);
    }

    /**
     * 指定された時刻の校時タイムテーブルを得る。
     * 有効な時間の範囲は各授業の開始時刻10分前から終了時刻まで。
     * もし指定される時刻が校時内に当てはまらない場合、nullを返す。
     * (例: 4校時=(11:35開始,12:25終了)
     *      打刻=11:35では校時は4校時、11:24と12:26では校時はnull)
     * 最終校時以降の打刻はnullを返す
     * @param time 指定される時刻
     * @return 校時タイムテーブル
     */
    private PeriodTimeTable getPeriodTimeTable(final BatchTime time, final boolean doBeforeCheck) {
        final String[] cds = new String[_periodTimeTables.size()];
        int j = 0;
        for (final Iterator it = _periodTimeTables.keySet().iterator(); it.hasNext(); j++) {
            final String pcd = (String) it.next();
            cds[j] = pcd;
        }
        Arrays.sort(cds);
        for (int i = 0; i < cds.length; i++) {
            final PeriodTimeTable tt = getPeriodTimeTable(cds[i]);
            final BatchTime validBegin = tt.getBeginTime().add(0, -10); // 校時の開始10分前オフセット
            
            final boolean beforeCheck = doBeforeCheck ? validBegin.isBefore(time, true) : true;
            if (beforeCheck && time.isBefore(tt.getEndTime(), true)) {
                return tt;
            }
        }
        return (doBeforeCheck) ? null : PeriodTimeTable.ONE_DAY;
    }

    /**
     * 指定された時刻の校時タイムテーブルを得る。(校時の開始時間もチェックする)
     * @param time 指定される時刻
     * @return 校時タイムテーブル
     */
    public PeriodTimeTable getPeriodTimeTable(final BatchTime time) {
        return getPeriodTimeTable(time, true);
    }
    /**
     * 校時タイムテーブルの校時を得る。
     * @param tt 校時タイムテーブル
     * @return 校時
     */
    public Period getPeriod(final PeriodTimeTable tt) {
        return (tt == null) ? null : getPeriod(tt.getPeriodCd());
    }

    /**
     * 校時コードの校時を得る。
     * @param cd 校時コード
     * @return 校時
     */
    public Period getPeriod(final String cd) {
        if (Period.LATEST_PERIOD_CODE.equals(cd)) return Period.LATEST_PERIOD;
        if (getPeriodTimeTable(cd)==null) return null;
        return (Period) _periods.get(cd);
    }
} // Param

// eof
