package jp.co.alp.kenja.batch.otr;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.otr.domain.Period;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 校時のタイムテーブル
 * @version $Id: PeriodTimeTable.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class PeriodTimeTable {

    private static Log log = LogFactory.getLog(PeriodTimeTable.class);
    private static final String PROPSNAME = "PeriodTimeTable.properties";
    public static final PeriodTimeTable ONE_DAY =
        new PeriodTimeTable(Period.LATEST_PERIOD_CODE , BatchTime.create(0,0), BatchTime.create(23,59));

    /** 校時コード */
    private final String _code;
    /** 開始時刻 */
    private final BatchTime _beginTime;
    /** 終了時刻 */
    private final BatchTime _endTime;    

    /**
     * コンストラクタ
     * @param code 校時コード
     * @param beginTime 開始時刻
     * @param endTime 終了時刻
     */
    private PeriodTimeTable(final String code, final BatchTime beginTime, final BatchTime endTime) {
        _code = code;
        _beginTime = beginTime;
        _endTime = endTime;
    }

    /**
     * 校時、開始時刻、終了時刻の文字列から校時タイムテーブルを作成する
     * @param cd 校時コード
     * @param beginStr 開始時刻の文字列
     * @param endStr   終了時刻の文字列
     * @return 校時タイムテーブル
     * @throws IOException
     */
    private static PeriodTimeTable create(final String cd, final String beginStr, final String endStr) throws IOException {
        final BatchTime begin = getBatchTime(beginStr);
        final BatchTime end = getBatchTime(endStr);
        return new PeriodTimeTable(cd, begin, end);
    }

    /**
     * 時刻を返す
     * @param timeStr 時刻フォーマット文字列
     * @return 時刻
     */
    private static BatchTime getBatchTime(final String timeStr) {
        final String[] time = StringUtils.split(timeStr, ":");
        final int hour = Integer.valueOf(time[0].trim()).intValue();
        final int minute = Integer.valueOf(time[1].trim()).intValue();
        return BatchTime.create(hour, minute);
    }

    /**
     * プロパティーから校時タイムテーブルを読み込む
     * @throws IOException ファイル読み込み例外
     * @return 校時タイムテーブルマップ
     */
    public static Map load() throws IOException {

        final Properties props = new Properties();
        props.load(new FileInputStream(PROPSNAME));

        final Map periodTimeTables = new TreeMap();

        for (final Iterator it = props.keySet().iterator(); it.hasNext();) {
            final String cd = (String) it.next();
            final String times = (String) props.get(cd.toString());
            final String[] timesStr = StringUtils.split(times, ",");
            if (timesStr.length != 2) {
                throw new IOException("プロパティーファイルのフォーマットが正しくありません。" + times);
            }
            final PeriodTimeTable tt = PeriodTimeTable.create(cd, timesStr[0], timesStr[1]);
            log.debug(" PeriodTimeTable.properties [" + cd + " , " + tt + "]");
            periodTimeTables.put(cd, tt);
        }
        return periodTimeTables;
    }

    /**
     * 開始時刻を返す
     * @return 開始時刻
     */
    public BatchTime getBeginTime() {
        return _beginTime;
    }

    /**
     * 終了時刻を返す
     * @return 終了時刻
     */
    public BatchTime getEndTime() {
        return _endTime;
    }

    /**
     * 校時コードを返す
     * @return 校時コード
     */
    public String getPeriodCd() {
        return _code;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return " 校時タイムテーブル (校時=" + _code + " 開始時刻=" + _beginTime + " 終了時刻=" + _endTime + ")";
    }
}
