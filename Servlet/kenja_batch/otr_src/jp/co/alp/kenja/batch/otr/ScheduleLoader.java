// kanji=漢字
/*
 * $Id: ScheduleLoader.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/12 16:13:05 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.otr.domain.Chair;
import jp.co.alp.kenja.batch.otr.domain.Facility;
import jp.co.alp.kenja.batch.otr.domain.HomeRoom;
import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Period;
import jp.co.alp.kenja.batch.otr.domain.Schedule;
import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 時間割ローダー
 * @version $Id: ScheduleLoader.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class ScheduleLoader {

    /*pkg*/static final Log log = LogFactory.getLog(ScheduleLoader.class);
    /** 時間割データ */
    private Map _schedules = new TreeMap();
    /** 講座マスタ */
    private Map _chairs;
    /** DB */
    private final DB2UDB _db;
    /** パラメータ */
    private final Param _param;

    /**
     * コンストラクタ
     * @param db DB
     * @param param パラメーター
     * @throws SQLException SQL例外
     */
    public ScheduleLoader(final DB2UDB db, final Param param) throws SQLException {
        _db = db;
        _param = param;
        loadMasterData(_db);
        loadSchedules();
        debugDisplaySchedule();
        _db.commit();
    }

    /**
     * DBのデータ(HRクラス、講座、講座受講クラス、講座担当職員データ)をロードする
     * @param db DB
     * @throws SQLException SQL例外
     */
    private void loadMasterData(final DB2UDB db) throws SQLException {
        try {
            final String semester = _param.getSemester();
            final Map facilities = Facility.load(db);

            final Map homeRooms = HomeRoom.load(db, _param.getYear(), semester);
            log.debug("時間割登録: 組み名称の情報: schreg_regd_hdat の総数=" + homeRooms.size());
            for (final Iterator it = homeRooms.keySet().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) homeRooms.get(it.next());
                log.debug("  HomeRoom " + hr.getCode() + " 学生数 = " + hr.getStudentCount());
            }

            _chairs = Chair.load(_db, _param.getYear(), semester);
            log.debug("時間割登録: 講座マスタの数=" + _chairs.size());
            Chair.loadStaffs(_chairs, db, _param.getYear(), semester);
            Chair.loadStudents(_chairs, db, _param.getYear(), semester, _param.getTargetDate().toString());
            Chair.loadFacilities(_chairs, db, _param.getYear(), semester, facilities);

        } catch (final SQLException e) {
            log.fatal("時間割登録のマスタ系取得でエラー");
            throw e;
        } finally {
            _db.commit();
        }
    }

    private void loadSchedules() throws SQLException {
        final Map facilities = Facility.load(_db);
        final String sql = scheduleSql();
        log.debug("********* sql = " + sql);
        _db.query(sql);
        final ResultSet rs = _db.getResultSet();
        while (rs.next()) {
            final KenjaDateImpl date = KenjaDateImpl.getInstance(rs.getDate("executedate"));
            if (_schedules.get(date) == null) {
                _schedules.put(date, new TreeMap());
            }
            final Map periodMap = (Map) _schedules.get(date);
            final Period period = _param.getPeriod(rs.getString("periodcd"));

            // 校時コードが校時タイムテーブルに無いならこの時間割を読み込まない
            if (period == null) {
                continue;
            }

            if (periodMap.get(period) == null) {
                periodMap.put(period, new ArrayList());
            }
            final List schedules = (List) periodMap.get(period);
            final String chairCd = rs.getString("chaircd");
            final String executed = rs.getString("executed");
            final boolean b = "1".equals(executed);
            final Schedule schedule = new Schedule(date, period, chairCd, b);
            final Chair chair = (Chair) _chairs.get(chairCd);
            if (null == chair) {
                log.warn("講座の存在しない時間割は無視: " + schedule);
                continue;
            }
            schedule.setChair(chair);
            final String faccd = rs.getString("faccd");
            final Facility facility = faccd != null ? (Facility) facilities.get(faccd) : null;
            if (facility != null) {
                schedule.addFacility(facility);
            }
            schedules.add(schedule);
            
            // 連続授業の設定
            for (final Iterator it = getNeighboorPeriodScheduleList(date, period).iterator(); it.hasNext();) {
                final Schedule anotherSchedule = (Schedule) it.next();
                if (!anotherSchedule.getChair().equals(chair)) {
                    continue;
                }
                schedule.setContinuedSchedule(anotherSchedule);
                log.debug("  連続授業 [" + schedule + "] <==> [" + anotherSchedule + "]");
            }
        }
        log.info("時間割読み込み終了。時間割の日数=" + _schedules.size());
    }

    /**
     * 時間割を取得するsqlを得る
     * 出欠が未実施のものを対象とする
     * @return 時間割を取得するsql
     */
    private String scheduleSql() {
        Period targetPeriod = _param.getTargetPeriod();
        String periodcd = (targetPeriod == null) ? "0" : targetPeriod.getCode();
        final String sql = "SELECT"
            + "  t1.executedate,"
            + "  t1.periodcd,"
            + "  t1.chaircd,"
            + "  t1.executed,"
            + "  t2.faccd"
            + " FROM"
            + "  sch_chr_dat t1"
            + " LEFT JOIN sch_fac_dat t2 ON "
            + "  t1.year = t2.year AND"
            + "  t1.semester = t2.semester AND"
            + "  t1.executedate = t2.executedate AND"
            + "  t1.periodcd = t2.periodcd AND"
            + "  t1.chaircd = t2.chaircd "
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.semester='" + _param.getSemester() + "' AND"
            + "  ((t1.executedate='" + _param.getTargetDate() + "' AND t1.periodcd <= '" + periodcd + "' ) OR "
            + "   (t1.executedate BETWEEN '" + _param.getOldestDate() + "' AND '" + _param.getTargetDate().add(-1) + "')) AND"
            + "  t1.executed = '0'"
            + " ORDER BY t1.executedate, t1.periodcd, t1.chaircd"
            ;
        return sql;
    }

    private void debugDisplaySchedule() {

        for (final Iterator it = _schedules.keySet().iterator(); it.hasNext();) {
            final KenjaDateImpl date = (KenjaDateImpl) it.next();
            log.debug("■日付 = " + date);

            final Map periodMap = (Map) _schedules.get(date);
            for (final Iterator it2 = periodMap.keySet().iterator(); it2.hasNext();) {
                final Period period = (Period) it2.next();
                log.debug("  ▲校時 = " + period);

                final List scheduleList = (List) periodMap.get(period);
                for (final Iterator it3 = scheduleList.iterator(); it3.hasNext();) {
                    final Schedule schedule = (Schedule) it3.next();
                    final Chair chair = schedule.getChair();
                    if (chair.getStudents().size() != 0) {
                        log.debug("      " + chair);
                    }
                }
            }
        }
    }

    /**
     * 年月と校時の時間割に入退室データをセットする。
     * @param date 年月
     * @param period 校時
     * @param data 入退室データ
     * @param param パラメータ
     * @return データがセットされたらtrue、そうでなければfalseを返す
     */
    public boolean addAttendance(final KenjaDateImpl date, final Period period, final RoomEnterExitData data, final Param param) {
        final List schedules = getSchedules(date, period);
        if (schedules.isEmpty()) {
            log.debug("次の月日校時の時間割は指定の範囲外です。" + date + " 、 " + period + "(" + data + ")");
            return false;
        }

        for (final Iterator it = schedules.iterator(); it.hasNext();) {
            final Schedule schedule = (Schedule) it.next();
            final String schregno = data.getSchregno();

            if (data.isStaff() && schedule.getChair().hasTeacher(schregno)) {
                // 先生のデータなら時間割に登録する
                schedule.setTeacherData(data);
                log.debug("  スタッフのデータ 時間割=[" + schedule + "] 、 先生の打刻データ = [" + data + "]");
                return true;
            } else if (schedule.hasStudent(schregno)) {

                // 生徒のデータが時間割の講座に登録されていれば、出欠データとして登録する
                final Attend2Dat attend = new Attend2Dat(
                    data,
                    schedule,
                    param.getYear()
                );
                schedule.addAttendance(attend);
                return true;
            }
        }
        return false;
    }


    /**
     * 指定された月日校時の時間割リストを返す。
     * @param date 月日
     * @param period 校時
     * @return 指定された月日校時の時間割リスト
     */
    private List getSchedules(final KenjaDateImpl date, final Period period) {
        final Map periodMap = (Map) _schedules.get(date);
        if (null == period || null == periodMap || null == periodMap.get(period)) {
            return java.util.Collections.EMPTY_LIST;
        }
        return (List) periodMap.get(period);
    }
    /**
     * 指定された月日の、前の校時と次の校時の時間割リストを返す。
     * @param date 月日
     * @param period 校時
     * @return 指定された月日の、前の校時と次の校時の時間割リスト
     */
    private List getNeighboorPeriodScheduleList(final KenjaDateImpl date, final Period period) {
        final List schedules = new ArrayList();
        schedules.addAll(getSchedules(date, period.getPrevious()));
        schedules.addAll(getSchedules(date, period.getNext()));
        return schedules;
    }

    /**
     * 勤怠ファイルに無いデータを含めた全出欠データを返す。
     * @param param パラメータ
     * @param param roomEnterExitDataList OTR出力入退室データリスト
     * @return 全出欠データ
     */
    public List getAttendanceList(final Param param, final Map datePeriodMap) {
        final List attendanceList = new LinkedList();

        for (final Iterator it = _schedules.keySet().iterator(); it.hasNext();) {
            final KenjaDateImpl date = (KenjaDateImpl) it.next();

            final Map dataPeriodMap = (Map) datePeriodMap.get(date);
            final Map periodMap = (Map) _schedules.get(date);
            for (final Iterator it2 = periodMap.keySet().iterator(); it2.hasNext();) {
                final Period period = (Period) it2.next();
                
                final Map roomEnterExitDataMap = (dataPeriodMap==null) ? null : (Map) dataPeriodMap.get(period);

                final List schedules = (List) periodMap.get(period);
                for (final Iterator it3 = schedules.iterator(); it3.hasNext();) {
                    final Schedule sch = (Schedule) it3.next();
                    sch.setAllAttendance(param, roomEnterExitDataMap);
                    attendanceList.addAll(sch.getAttendanceList());
                }
            }
        }
        return attendanceList;
    }
} // MkJikanwariTouroku

// eof
