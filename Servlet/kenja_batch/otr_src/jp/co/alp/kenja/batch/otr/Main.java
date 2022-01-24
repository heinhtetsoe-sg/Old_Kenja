// kanji=漢字
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 * 作成日: 2008/03/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Period;
import jp.co.alp.kenja.batch.otr.domain.Schedule;
import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OTR(カードリーダー)読込
 * @author maesiro
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Main {
    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    /**
     * メイン
     * @param args 引数
     * @throws Exception 例外
     */
    public static void main(final String[] args) throws Exception {

        final Param param = new Param(args);

        final DB2UDB db = new DB2UDB(param.getDbUrl(), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db.open();

        param.load(db);

        // 勤怠ファイルを読み込む処理
        final List data = RoomEnterExitData.load(param.getKintaiFile());

        // 月日毎の校時毎の入退室データマップを返す。
        final Map datePeriodMap = getDatePeriodMap(data, param);

        // 時間割読み込み
        final ScheduleLoader schLoader = new ScheduleLoader(db, param);

        // 入退室データを時間割に関連付ける
        setScheduleMap(datePeriodMap, schLoader, param);

        // 勤怠をセットする
        setKintai(datePeriodMap, schLoader, param);

        // DBにデータをインサートする
        insertData(db, datePeriodMap, schLoader, param);

        log.fatal("Done.");
    }

    /**
     * 月日毎の校時毎の入退室データマップを返す。
     * 無効な時間のデータは含めないようにする。
     * 同一校時の同一の人物の入力があった場合、警告を表示して、時間が早いデータで更新する。
     * @param roomEnterExitDataList 入退室データリスト
     * @param param パラメータ
     * @return 月日毎の校時毎の入退室データマップ
     */
    private static Map getDatePeriodMap(final List roomEnterExitDataList, final Param param) {
        final Map rtn = new TreeMap();
        int count = 0;
        for (final Iterator it = roomEnterExitDataList.iterator(); it.hasNext();) {
            final RoomEnterExitData data = (RoomEnterExitData) it.next();
            final KenjaDateImpl date = data.getDate();

            KenjaDateImpl oldestDate = param.getOldestDate();
            if (oldestDate == null || date.compareTo(oldestDate) < 0) {
                param.setOldestDate(date);
            }

            if (rtn.get(date) == null) {
                rtn.put(date, new TreeMap());
            }
            final Map periods = (Map) rtn.get(date);

            final BatchTime time = data.getTime();
            final Period period = param.getPeriod(param.getPeriodTimeTable(time));
            if (period == null) {
                log.error("校時が定義外です。[" + data + "]");
                continue;
            }

            if (periods.get(period) == null) {
                periods.put(period, new TreeMap());
            }
            final Map students = (Map) periods.get(period);

            final RoomEnterExitData inputbefore = (RoomEnterExitData) students.get(data.getSchregno());
            if (inputbefore == null) {
                students.put(data.getSchregno(), data);
            } else {
                log.warn("同一校時の1回以上の入力です。 [" + data + "], 前回の入力[" + inputbefore + "]");
                final BatchTime timeBefore = inputbefore.getTime();
                if (timeBefore.isAfter(time, false)) {
                    log.warn("     [" + data + "] で更新します。");
                    students.put(data.getSchregno(), data);
                }
                count++;
            }
        }
        log.debug("同一校時入力の廃棄データ数 = " + count);

        return rtn;
    }

    /**
     * 入退室データを時間割に関連付ける
     * @param dates 月日毎の校時毎の入退室データマップ
     * @param schLoader 時間割ローダー
     */
    private static void setScheduleMap(final Map dates, final ScheduleLoader schLoader, final Param param) {
        int dataCount = 0;
        int notFound = 0;
        for (final Iterator dateIt = dates.keySet().iterator(); dateIt.hasNext();) {
            final KenjaDateImpl date = (KenjaDateImpl) dateIt.next();
            log.debug(" 日付 = " + date);

            final Map periods = (Map) dates.get(date);
            for (final Iterator periodIt = periods.keySet().iterator(); periodIt.hasNext();) {
                final Period period = (Period) periodIt.next();
                log.debug("   校時 = " + period);

                final Map dataMap = (Map) periods.get(period);
                for (final Iterator dataIt = dataMap.keySet().iterator(); dataIt.hasNext();) {
                    final String schregno = (String) dataIt.next();
                    dataCount++;
                    log.debug("    " + dataMap.get(schregno));

                    final RoomEnterExitData data = (RoomEnterExitData) dataMap.get(schregno);
                    final boolean addSucceed = schLoader.addAttendance(date, period, data, param);
                    if (!addSucceed) {
                        notFound++;
                    }
                }
            }
        }
        log.debug(notFound + "件の講座時間割が見つかりませんでした。");
    }

    /**
     * 勤怠と登録コードをセットする
     * @param schLoader 時間割ローダー
     * @param roomEnterExitDataList OTR出力入退室データリスト
     * @param param パラメータ
     */
    private static void setKintai(final Map datePeriodMap, final ScheduleLoader schLoader, final Param param) {
        for (final Iterator schIt = schLoader.getAttendanceList(param, datePeriodMap).iterator(); schIt.hasNext();) {
            final Attend2Dat attend = (Attend2Dat) schIt.next();

            final Schedule sch = attend.getSchedule();
            attend.setRegisterCd(sch.getStaffCd());
            if(attend.isInvalid()) continue; // 無効なデータは勤怠判定をしない(デフォルトで"事故欠(無) (カード入力無)")

            attend.setKintai(sch, param);
            //log.debug(" 出欠データ = " + attend);

            // 連続授業の扱い
            setSecondScheduleAttendanceSeated(attend.getSchregno(), sch);
        }
    }
    
    /**
     * 指定の時間割に所属する学籍番号の連続授業を探し、
     * 2時間目の時間割の出欠を"出席"とする。
     * @param schregno 学籍番号 
     * @param sch1 時間割
     */
    private static void setSecondScheduleAttendanceSeated(final String schregno, final Schedule sch1) {
        final Schedule sch2 = sch1.getContinuedSchedule();
        if (sch2 == null) {
            return;
        }
        
        Attend2Dat att1 = sch1.getAttendance(schregno);
        Attend2Dat att2 = sch2.getAttendance(schregno);
        if (att1 == null || att2 == null) {
            return;
        }
        
        Attend2Dat secondAttend = null;
        if (sch1.getPeriod().isBefore(sch2.getPeriod()) && !att1.getKintai().isNonotice()) {
            // sch1が1時間目の授業
            // => sch1が事故欠(無)(カード入力無し)でなければsch2は出席とする
            secondAttend = att2;
        } else if (sch1.getPeriod().isAfter(sch2.getPeriod()) && !att2.getKintai().isNonotice()) {
            // sch2が1時間目の授業
            // => sch2が事故欠(無)(カード入力無し)でなければsch1は出席とする
            secondAttend = att1;
        }

        if (secondAttend == null) { 
            return;
        }

        secondAttend.setKintaiSeated();
        secondAttend.setRegisterCd(sch2.getStaffCd());
    }

    /**
     * DBにデータをインサートする
     * @param db2 DB
     */
    private static void insertData(final DB2UDB db, final Map datePeriodMap, final ScheduleLoader schLoader, final Param param) {
        int count = 0;
        log.debug("DBへのデータ出力開始。");
        for (final Iterator schIt = schLoader.getAttendanceList(param, datePeriodMap).iterator(); schIt.hasNext();) {
            final Attend2Dat attend = (Attend2Dat) schIt.next();

            if (!param.outputsSeatedToDB() && attend.getKintai().isSeated()) {
                log.debug("勤怠が'出席'なのでDBに出力しない = " + attend);
                continue;
            }
            log.debug(" DB出力 出欠データ = " + attend);
            try {
                db.query(attend.getSelectSql()); // DBにこのデータがすでにあるか検索する。
                final ResultSet rs = db.getResultSet();
                final boolean found = rs.next();
                
                // DBに同一のデータが存在する場合
                if (found) {
                    String beforeOutputData = "(在籍番号=["+rs.getString("SCHREGNO")+"]、日付=["+rs.getString("ATTENDDATE")+"]、校時コード=["+rs.getString("PERIODCD")+"]、講座コード=["+rs.getString("CHAIRCD")+"]、勤怠コード=["+rs.getString("DI_CD")+"]、更新時間=[" + rs.getString("UPDATED") + "])";
                    log.debug("   すでにDBに同一のデータが存在します。" + beforeOutputData);
                    if (!param.deletesOldDataInDB()) {
                        log.debug("   出力を中止します。");
                        continue;
                    }
                    log.debug("   削除して出力します。");
                    db.executeUpdate(attend.getDeleteSql()); // 同一の元データを削除する
                }

                count += db.executeUpdate(attend.getInsertSql()); // データを出力する
            } catch (SQLException ex) {
                String message = "DB出力エラー。";
                if (ex.getErrorCode() == -803) {
                    message += "::データ重複エラー  重複データ=";
                }
                log.warn(message);
            }
            db.commit();
        }
        log.debug("DBへのデータ出力終了。出力データ数 = " + count);
    }
} // Main

// eof
