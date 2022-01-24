// kanji=漢字
/*
 * $Id: Schedule.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/24 14:43:39 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.otr.Attend2Dat;
import jp.co.alp.kenja.batch.otr.BatchTime;
import jp.co.alp.kenja.batch.otr.Param;
import jp.co.alp.kenja.batch.otr.PeriodTimeTable;
import jp.co.alp.kenja.batch.otr.RoomEnterExitData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 時間割。
 * @version $Id: Schedule.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Schedule {
    
    private static final Log log = LogFactory.getLog(Schedule.class);
    
    /** 実施日付 */
    private final KenjaDateImpl _date;
    /** 校時 */
    private final Period _period;
    /** 講座コード */
    private final String _chairCd;
    /** 実施フラグ */
    private final boolean _executed;
    /** 施設 */
    private final List _facilities;
    /** 講座 */
    private Chair _chair;
    /** 先生の打刻データ (複数の場合、より早い時刻のデータ) */
    private RoomEnterExitData _teacherData;
    /** この講座の学生の出欠データのリスト */
    private List _attendanceList;

    /** 連続授業の時間割 */
    private Schedule _continuedSchedule;

    /**
     * 時間割
     * @param date 日
     * @param period 校時
     * @param chairCd 講座コード
     * @param executed 実行されたか
     */
    public Schedule(
            final KenjaDateImpl date, 
            final Period period, 
            final String chairCd, 
            final boolean executed) {
        _date = date;
        _period = period;
        _chairCd = chairCd;
        _executed = executed;
        _facilities = new ArrayList();
        _attendanceList = new LinkedList();
    }

    /**
     * 日を得る
     * @return 日
     */
    public KenjaDateImpl getDate() { return _date; }

    /**
     * 校時を得る
     * @return 校時
     */
    public Period getPeriod() { return _period; }

    /**
     * 実行されたか
     * @return 実行されたならtrue、そうでなければfalse
     */
    public boolean isExecuted() { return _executed; }

    /**
     * 施設を追加する
     */
    public void addFacility(final Facility fac) {
        _facilities.add(fac);
    }

    /**
     * 施設を得る
     * @return 施設
     */
    private List getFacilities() {
        // 施設が登録されていなければ、講座の施設を取得する
        if (_facilities.size() == 0) return getChair().getFacilities();
        return _facilities;
    }

    /**
     * 施設がゲートNo. を含んでいるか
     * @param data 
     * @return 施設がゲートNo. を含んでいるならtrue、そうでなければfalse
     */
    public boolean facilitiesContainGateNo(final String gateNo) {
        for (final Iterator it = getFacilities().iterator(); it.hasNext();) {
            final Facility fac = (Facility) it.next();
            if (fac.contain(gateNo)) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * 講座を得る
     * @return 講座
     */
    public Chair getChair() { return _chair; }

    /**
     * 講座をセットする
     * @param chair 講座
     */
    public void setChair(final Chair chair) { _chair = chair; }


    /**
     * 記録時刻の勤怠コードを得る
     * @param time 記録時刻
     * @param param パラメータ
     * @return 記録時刻の勤怠コード
     */
    public Kintai getKintai(final BatchTime time, final Param param) {
        
        PeriodTimeTable ptt = null;
        BatchTime beginTime = null;
        BatchTime teacherTime = null;
        Kintai kintai = null;
        int processId=0;
        try {
            processId=0;
            ptt = param.getPeriodTimeTable(_period);
            processId = 1;
            beginTime = ptt.getBeginTime();
            processId = 2;
            teacherTime = (getTeacherData() == null) ? null : getTeacherData().getTime();
            processId = 3;
            kintai = Kintai.getKintai(time, beginTime, teacherTime);
            processId = 4;
        } catch (Exception ex) {
            log.debug(" !!! 勤怠の取得過程でエラーが発生しました。("+processId+") 時間割=[" + this + "] , 校時タイムテーブル=" + ptt + " , 校時開始時間=" + beginTime +" , 記録時刻=" + time +" !!!", ex);
            kintai = null;
        }

        return kintai;
    }

    /**
     * 先生の在籍コードを得る
     * @return 先生の在籍コード
     */
    public String getStaffCd() {
        return (_teacherData == null) ? "00999999" : _teacherData.getSchregno();
    }

    /**
     * 講座を受講している年組の中に在籍番号があるか
     * @param schregno 在籍番号
     * @return 講座を受講している年組の中に在籍番号があればtrue、そうでなければfalse
     */
    public boolean hasStudent(final String schregno) {
        return getStudentList().contains(schregno);
    }

    /**
     * 連続授業の時間割をセットする
     * @param schedule 時間割
     */
    public void setContinuedSchedule(final Schedule schedule) {
        _continuedSchedule = schedule;
        schedule._continuedSchedule = this;
    }

    /**
     * 連続授業の時間割を得る
     * @return 連続授業の時間割
     */
    public Schedule getContinuedSchedule() {
        return _continuedSchedule;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _date + ", 校時コード=[" + _period.getCode() + "], 講座コード=[" + _chairCd + "]";
    }

    /**
     * 先生の入退室データを得る
     * @return 先生の入退室データ
     */
    public RoomEnterExitData getTeacherData() {
        return _teacherData;
    }

    /**
     * 先生の入退室データをセットする
     * もし、他の先生が既に打刻していたら
     * 時間の早い場合のみセットする
     * @param data 先生の入退室データ
     */
    public void setTeacherData(final RoomEnterExitData data) {
        if (_teacherData == null || data.getTime().isBefore(_teacherData.getTime(), false)) {
            _teacherData = data;
        }
    }

    /**
     * 講座を受講する全学生のリストを得る
     * @return 講座を受講する全学生のリスト
     */
    private List getStudentList() {
        return _chair.getStudents();
    }


    /**
     * 仮出欠データを追加する
     * @param attendance 仮出欠データ
     */
    public void addAttendance(final Attend2Dat attendance) {
        _attendanceList.add(attendance);
    }

    /**
     * 出欠リストから指定の在籍番号のデータを得る
     * @param schregno 在籍番号
     * @return 在籍番号のデータ
     */
    public Attend2Dat getAttendance(final String schregno) {
        for (final Iterator it = _attendanceList.iterator(); it.hasNext();) {
            final Attend2Dat attend = (Attend2Dat) it.next();
            if (attend.getSchregno().equals(schregno)) {
                return attend;
            }
        }
        return null;
    }

    /**
     * 勤怠ファイルに無い出欠データも設定する。
     * @param param パラメータ
     * @param roomEnterExitDataMap OTR出力入退室データの学籍番号とデータのマップ
     */
    public void setAllAttendance(final Param param, final Map roomEnterExitDataMap) {
        final List studentList = getStudentList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final String schregno = (String) it.next();

            if (null != getAttendance(schregno)) {
                continue;
            }
            if (roomEnterExitDataMap != null) {
                boolean checked = false;
                for (final Iterator it2 = roomEnterExitDataMap.keySet().iterator(); it2.hasNext();) {
                    String dataSchregno = (String) it2.next();
                    if (dataSchregno.equals(schregno))  checked = true;
                }
                if (checked) continue;
            }
            // 出欠リストにない、もしくはチェックされていないデータのみ追加する。

            final Attend2Dat attendance = new Attend2Dat(
                    schregno,
                    getDate(),
                    null, // 時刻は無し = 勤怠はデフォルトで 事故欠(無)(カード入力無)
                    this,
                    param.getYear());

            addAttendance(attendance);
            
        }
    }

    /**
     * 出欠データリストを返す
     * @return 出欠データリスト
     */
    public List getAttendanceList() {
        return _attendanceList;
    }
} // Schedule

// eof
