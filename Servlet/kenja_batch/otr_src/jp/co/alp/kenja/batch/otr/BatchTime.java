package jp.co.alp.kenja.batch.otr;

import java.text.DecimalFormat;

/**
 * 時刻
 * @version $Id1.0v$
 */
public final class BatchTime implements Comparable {

    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("00");
    private final Integer _hour;
    private final Integer _minute;

    private BatchTime(final Integer hour, final Integer minute) {
        _hour = hour;
        _minute = minute;
    }

    /**
     * 時刻インスタンスを作成する。
     * @param hour ~時
     * @param minute ~分
     * @return 時刻インスタンス
     * @throws IllegalArgumentException (0 <= hour < 24) かつ (0 <= minute < 60) でないとき
     */
    public static BatchTime create(final int hour, final int minute) throws IllegalArgumentException {
        if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60) {
            throw new IllegalArgumentException();
        }
        return new BatchTime(new Integer(hour), new Integer(minute));
    }

    /**
     * 時刻に引数の時間を足した時刻を返す
     * @param plusHour プラス~時間
     * @param plusMinute プラス~分
     * @return 時刻に引数の時間を足した時刻
     */
    public BatchTime add(final int plusHour, final int plusMinute) {
        int hour = _hour.intValue() + plusHour;
        int minute = _minute.intValue() + plusMinute;

        if (minute < 0) {
            hour -= 1;
            minute += 60;
        } else if (minute >= 60) {
            hour += 1;
            minute -= 60;
        }
        return create(hour, minute);
    }

    /**
     * 指定時刻より前の時間か
     * @param batchTime 指定時刻
     * @param contains 指定時刻を含むならtrue、それ以外ならfalse
     * @return 指定時刻より前の時間ならtrue、それ以外ならfalse
     */
    public boolean isBefore(final BatchTime batchTime, final boolean contains) {
        final int cmp = compareTo(batchTime);
        return (contains && cmp == 0) || cmp < 0;
    }

    /**
     * 指定時刻より後の時間か
     * @param batchTime 指定時刻
     * @param contains 指定時刻を含むならtrue、それ以外ならfalse
     * @return 指定時刻より後の時間ならtrue、それ以外ならfalse
     */
    public boolean isAfter(final BatchTime batchTime, final boolean contains) {
        final int cmp = compareTo(batchTime);
        return (contains && cmp == 0) || cmp > 0;
    }

    /*
     * 時刻の比較
     * hourで比較する。hourが同一ならminuteで比較する。
     *
     * TODO: 次の日の00:00と前の日の23:59では後者が大きいことになる(=あとになる)ので
     *       前提として日付と一緒に使用する
     * TODO: 内部でCalendar使うとか
     */
    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (!(o instanceof BatchTime)) {
            return 0;
        }
        final BatchTime otherTime = (BatchTime) o;

        int rtn = _hour.compareTo(otherTime._hour);
        if (rtn == 0) {
            rtn = _minute.compareTo(otherTime._minute);
        }
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return TIME_FORMAT.format(_hour) + ":" + TIME_FORMAT.format(_minute);
    }
}
