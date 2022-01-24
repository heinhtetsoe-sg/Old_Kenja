package jp.co.alp.kenja.batch.otr;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Kintai;
import jp.co.alp.kenja.batch.otr.domain.Schedule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 仮出欠データ
 * @author maesiro
 * @version $Id: Attend2Dat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Attend2Dat {
    private static final Log log = LogFactory.getLog(Attend2Dat.class);
    
    /** 出欠データタイプ カード入力 */
    private static final int ATTEND_TYPE_INSERTED = 0;
    /** 出欠データタイプ カード入力(無効) */
    private static final int ATTEND_TYPE_INSERTED_INVALID = 1;
    /** 出欠データタイプ カード入力無 */
    private static final int ATTEND_TYPE_NOT_INSERTED = 2;
    
    private final String _schregno;
    private final KenjaDateImpl _date;
    private final BatchTime _time;
    private final Schedule _schedule;
    private final String _year;
    private String _registerCd;
    private Kintai _kintai;
    
    /** 出欠データタイプ */
    private final int _attendType;

    /**
     * コンストラクタ
     * @param data OTR取り込みデータ
     * @param schedule 校時
     * @param year 年度
     */
    public Attend2Dat(
            final RoomEnterExitData data,
            final Schedule schedule,
            final String year) {
        this(data.getSchregno(), data.getDate(), data.getTime(), schedule, Kintai.getDefault(), year,
                isInvalid(data, schedule) ? ATTEND_TYPE_INSERTED_INVALID : ATTEND_TYPE_INSERTED);
    }

    /**
     * コンストラクタ(カード入力無)
     * @param schregno 在籍番号
     * @param date 日付
     * @param time 時間
     * @param schedule 校時
     * @param year 年度
     */
    public Attend2Dat(
            final String schregno,
            final KenjaDateImpl date,
            final BatchTime time,
            final Schedule schedule,
            final String year) {
        this(schregno, date, time, schedule, Kintai.getDefault(), year,
                ATTEND_TYPE_NOT_INSERTED);
    }

    /**
     * コンストラクタ
     * @param schregno 在籍番号
     * @param date 日付
     * @param time 時間
     * @param schedule 校時
     * @param year 年度
     */
    private Attend2Dat(
            final String schregno,
            final KenjaDateImpl date,
            final BatchTime time,
            final Schedule schedule,
            final Kintai kintai,
            final String year,
            final int attendType) {
        _schregno = schregno;
        _date = date;
        _time = time;
        _schedule = schedule;
        _year = year;
        _kintai = kintai;
        _attendType = attendType;
    }

    /**
     * 月日を得る
     * @return 月日
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * 勤怠を得る
     * @return 勤怠
     */
    public Kintai getKintai() {
        return _kintai;
    }

    /**
     * 校時を得る
     * @return 校時
     */
    public Schedule getSchedule() {
        return _schedule;
    }

    /**
     * 登録コードを得る
     * @return 登録コード
     */
    public String getRegisterCd() {
        return _registerCd;
    }

    /**
     * 在籍番号を得る
     * @return 在籍番号
     */
    public String getSchregno() {
        return _schregno;
    }

    /**
     * 年度を得る
     * @return 年度
     */
    public String getYear() {
        return _year;
    }

    /**
     * 勤怠を出席にする
     */
    public void setKintaiSeated() {
        _kintai = Kintai.getSeated();
    }

    /**
     * 時間割から勤怠をセットする
     * @param schedule 時間割
     * @param param パラメータ
     */
    public void setKintai(final Schedule schedule, final Param param) {
        Kintai kintai = schedule.getKintai(_time, param); 

        if (kintai == null) {
            log.debug(" !!! 勤怠が取得できませんでした。["+Kintai.getDefault()+"]をセットします。[" + this + "] !!!");
            kintai = Kintai.getDefault();
        }
        
        _kintai = kintai;
    }

    /**
     * 登録コードをセットする
     * @param cd 登録コード
     */
    public void setRegisterCd(final String cd) {
        _registerCd = cd;
    }

    /**
     * DBにインサートするSQLを返す
     * @return DBにインサートするSQL
     */
    public String getInsertSql() {
        final String sql = 
            "INSERT INTO ATTEND2_DAT (SCHREGNO, ATTENDDATE, PERIODCD, CHAIRCD, DI_CD, YEAR, REGISTERCD, UPDATED) values (" +
            "'" + getSchregno() + "'," + 
            "'" + getDate() + "'," + 
            "'" + _schedule.getPeriod().getCode() + "'," + 
            "'" + _schedule.getChair().getChairCd() + "'," + 
            "'" + _kintai.getResultCode() + "'," + 
            "'" + getYear() + "'," + 
            "'" + getRegisterCd() + "'," + 
            "current timestamp)" ; 
        return sql;
    }

    /**
     * DBから削除するSQLを返す
     * @return DBから削除するSQL
     */
    public String getDeleteSql() {
        final String sql = 
            "DELETE FROM ATTEND2_DAT WHERE " +
            " SCHREGNO = '" + getSchregno() + "' " + 
            " AND ATTENDDATE = '" + getDate() + "' " + 
            " AND PERIODCD = '" + _schedule.getPeriod().getCode() + "' ";
        return sql;
    }

    /**
     * DBにこのデータがすでにあるか検索するSQLを返す
     * @return DBにこのデータがすでにあるか検索するSQL
     */
    public String getSelectSql() {
        final String sql = 
            "SELECT SCHREGNO, ATTENDDATE, PERIODCD, CHAIRCD, DI_CD, UPDATED FROM ATTEND2_DAT WHERE " +
            " SCHREGNO = '" + getSchregno() + "' " + 
            " AND ATTENDDATE = '" + getDate() + "' " + 
            " AND PERIODCD = '" + _schedule.getPeriod().getCode() + "' ";
        return sql;
    }

    /**
     * OTR取り込みデータが無効か
     * @param data OTR取り込みデータ
     * @param schedule 判定された時間割
     * @return OTR取り込みデータが無効ならtrue、そうでなければfalse
     */
    private static boolean isInvalid(final RoomEnterExitData data, final Schedule schedule) {
        
        if(!schedule.facilitiesContainGateNo(data.getGateno())) {
            log.debug("無効なゲートNo.です。学籍番号=" + data.getSchregno() + " ゲートNo.=" + data.getGateno() + "(時間割=" + schedule + ")");
            return true;
        }
        return false;
    }

    /**
     * OTR取り込み元データが無効か
     * @return 無効ならtrue、そうでなければfalse
     */
    public boolean isInvalid() {
        return _attendType == ATTEND_TYPE_INSERTED_INVALID;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getSchregno() + " , "
            + getDate() + " , "
            + _schedule.getPeriod() + " , "
            + _schedule.getChair() + " , "
            + _kintai + " , "
            + getYear();
    }

}
