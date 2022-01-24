package jp.co.alp.kenja.batch.otr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 入退室データ
 * @author maesiro
 * @version $Id: RoomEnterExitData.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class RoomEnterExitData implements Comparable {

    private static Log log = LogFactory.getLog(RoomEnterExitData.class);

    private static final String VALID_CARD_DIV = "1";

    /** データ区分 */
    private final String _dataDiv;
    /** 打刻年月日 */
    private final KenjaDateImpl _date;
    /** 時刻 */
    private final BatchTime _time;
    /** カード種別 */
    private final String _cardDiv;
    /** 開閉区分 */
    private final String _openCloseDiv;
    /** 入退室区分 */
    private final String _enterExitDiv;
    /** 個人コード(在籍番号) */
    private final String _schregno;
    /** ゲートNo. */
    private final String _gateNo;

    /** 先生か */
    private final boolean _isTeacher;

    /**
     * コンストラクタ
     * @param schregno 学籍番号
     * @param date 月日
     * @param time 時間
     * @param dataDiv データ区分
     * @param cardDiv カード区分
     * @param openCloseDiv 開閉区分
     * @param enterExitDiv 入退室区分
     * @param gateNo ゲート番号
     * @param isTeacher 先生か
     */
    public RoomEnterExitData(
            final String schregno,
            final KenjaDateImpl date,
            final BatchTime time,
            final String dataDiv,
            final String cardDiv,
            final String openCloseDiv,
            final String enterExitDiv,
            final String gateNo,
            final boolean isTeacher) {
        _schregno = schregno;
        _date = date;
        _time = time;
        _dataDiv = dataDiv;
        _cardDiv = cardDiv;
        _openCloseDiv = openCloseDiv;
        _enterExitDiv = enterExitDiv;
        _gateNo = gateNo;

        _isTeacher = isTeacher;
    }

    /**
     * 月日を返す
     * @return 月日
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * 先生か
     * @return 先生ならtrue, そうでなければfalse
     */
    public boolean isStaff() {
        return _isTeacher;
    }

    /**
     * 在籍番号を返す
     * @return 在籍番号
     */
    public String getSchregno() {
        return _schregno;
    }

    /**
     * 時間を返す
     * @return 時間
     */
    public BatchTime getTime() {
        return _time;
    }

    /**
     * ゲートNo.を返す
     * @return ゲートNo.
     */
    public String getGateno() {
        return _gateNo;
    }

    /**
     * 指定された位置のトークンを得る
     *
     * ----- データフォーマット -----
     * dataDiv データ区分 2byte
     * year 年            4byte
     * month 月           2byte
     * date 日            2byte
     * hour 時間          2byte
     * minute 時間        2byte
     * cardDiv カード区分 1byte
     * aux 予備           1byte
     * openCloseDiv 開閉区分   1byte
     * enterExitDiv 入退室区分 1byte
     * schregno 学籍番号 10byte
     * gateNo ゲート番号  4byte
     *                 計32byte
     *                 
     * @param nth トークンの位置
     * @param dataLine 1行のデータ
     * @return トークン
     */
    private static String getToken(final int nth, final String dataLine) {
        final int[] sizes = new int[]{2, 4, 2, 2, 2, 2, 1, 1, 1, 1, 10, 4};
        final int[] beginIndex = new int[sizes.length];

        int ti = 0;
        for (int i = 0; i < sizes.length; i++) {
            beginIndex[i] = ti;
            ti += sizes[i];
        }
        return dataLine.substring(beginIndex[nth], beginIndex[nth] + sizes[nth]);
    }

    /**
     * 入退室データ作成
     * @param enterExitString 勤怠ファイルのデータの１行
     * @return 入退室データ
     */
    public static RoomEnterExitData create(final String enterExitString) {
        int p = 0;
        try {
            final String dataDiv = getToken(p++, enterExitString);
            final int year = Integer.parseInt(getToken(p++, enterExitString));
            final int month = Integer.parseInt(getToken(p++, enterExitString));
            final int date = Integer.parseInt(getToken(p++, enterExitString));
            final int hour = Integer.parseInt(getToken(p++, enterExitString));
            final int minute = Integer.parseInt(getToken(p++, enterExitString));
            final String cardDiv = getToken(p++, enterExitString);
            if (!VALID_CARD_DIV.equals(cardDiv)) {
                throw new IllegalArgumentException("cardDiv = " + cardDiv);
            }
            p++; // 予備データ
            final String openCloseDiv = getToken(p++, enterExitString);
            final String enterExitDiv = getToken(p++, enterExitString);
            final String studentCodeField = getToken(p++, enterExitString);
            final String gateNo = getToken(p++, enterExitString);

            final KenjaDateImpl kenjaDate = KenjaDateImpl.getInstance(year, month, date);
            final BatchTime time = BatchTime.create(hour, minute);
            final boolean isTeacher = studentCodeField.charAt(3) == '2'; 

            RoomEnterExitData data =  new RoomEnterExitData(
                    "20" + studentCodeField.substring(4),
                    kenjaDate, 
                    time,
                    dataDiv,
                    cardDiv,
                    openCloseDiv,
                    enterExitDiv, 
                    gateNo,
                    isTeacher // 在籍番号フィールドの2桁目が'2'なら先生
            ); 
                        
            return data;
        } catch (final IllegalArgumentException e) {
            log.error("無効なデータ " + enterExitString + " , " + p + " th field " + e.getMessage());
        }
        return null;
    }

    /**
     * 勤怠ファイルを読み込み勤怠データのリストを返す
     * @param file 勤怠ファイル
     * @return 勤怠データのリスト
     * @throws IOException IO例外
     */
    public static List load(final File file) throws IOException {
        final List dataList = new LinkedList();
        final BufferedReader br = new BufferedReader(new FileReader(file));
        int dataCount = 0;
        log.debug("OTRデータファイル読み込み開始。");
        for (String line = "";; dataCount++) {
            line = br.readLine();
            if (line == null) {
                break;
            }
            final RoomEnterExitData data = RoomEnterExitData.create(line);
            if (null == data) {
                continue;
            }
            log.debug(data);
            dataList.add(data);
        }
        log.debug("OTRデータファイル読み込み終了。 データ数 = " + dataCount + ", 有効データ数 = " + dataList.size());
        br.close();

        return dataList;
    }

    /**
     * 月日で比較する。月日が同一なら時刻で比較する。
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (o instanceof RoomEnterExitData) {
            return 0;
        }
        final RoomEnterExitData other = (RoomEnterExitData) o;
        int rtn = _date.compareTo(other._date);
        if (rtn == 0) {
            rtn = _time.compareTo(other._time);
        }
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return _schregno.hashCode() + _date.hashCode() + _time.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuffer stb = new StringBuffer();
        stb.append("在籍番号=").append(_schregno);
        stb.append(", 日付=").append(_date.toString());
        stb.append(", 時間=").append(_time.toString());
        stb.append(", ゲートNo.=").append(_gateNo);
        stb.append(isStaff() ? "(先生)" : "");
        return stb.toString();
    }
}
