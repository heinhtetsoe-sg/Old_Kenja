// kanji=漢字
/*
 * $Id: 808bc6f6dd7e12d7f4737261072b83488ffae753 $
 *
 * 作成日: 2007/02/13 16:06:18 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import java.awt.GraphicsEnvironment;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 賢者サーブレットのユーティリティクラス。
 * @author takaesu
 * @version $Id: 808bc6f6dd7e12d7f4737261072b83488ffae753 $
 */
public class KNJServletUtils {
    private KNJServletUtils() {
    }

    /**
     * パラメータのキーとバリューをログに出力する。
     * @param request HttpServletRequest
     * @param log ログ出力先
     */
    public static void debugParam(final HttpServletRequest request, final Log log) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final List list = list(request.getParameterNames());
        Collections.sort(list);
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String name = (String) it.next();
            final String[] values = request.getParameterValues(name);
            log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
        }
    }

    /*
     * 1.4 の Collecitons.list() メソッドをパクった。
     */
    private static ArrayList list(Enumeration e) {
        ArrayList l = new ArrayList();
        while (e.hasMoreElements())
            l.add(e.nextElement());
        return l;
    }

    /**
     * グラフが使える環境か調べる。
     * @param log ログ出力先
     * @return グラフが使えるなら True
     */
    public static boolean isEnableGraph(final Log log) {
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        } catch (final NoClassDefFoundError e) {
            // グラフを使用できない
            log.error("グラフを使用できません。: " + e);
            return false;
        } catch (final InternalError e) {
            // グラフを使用できない
            log.error("グラフを使用できません。: " + e);
            return false;
        } catch (final Throwable e) {
            // グラフを使用できない
            log.fatal("想定外の例外が発生: " + e);
            return false;
        }

        // グラフを使用できる
        log.fatal("グラフを使用できる環境です。");
        return true;
    }

    /**
     * Integer型として<code>ResultSet</code>から取得する。
     * @param rs <code>ResultSet</code>
     * @param key フィールド文字列
     * @return ラッパー型のインスタンス。
     * @throws SQLException 例外
     */
    public static Integer getInteger(final ResultSet rs, final String key) throws SQLException {
        final String s = rs.getString(key);
        if (null == s) {
            return null;
        }
        return new Integer(s);
    }

    /**
     * Double型として<code>ResultSet</code>から取得する。
     * @param rs <code>ResultSet</code>
     * @param key フィールド文字列
     * @return ラッパー型のインスタンス。
     * @throws SQLException 例外
     */
    public static Double getDouble(final ResultSet rs, final String key) throws SQLException {
        final String s = rs.getString(key);
        if (null == s) {
            return null;
        }
        return new Double(s);
    }

    /**
     * 任意の桁数で四捨五入する。
     * @param v 値
     * @param scale 小数点第 scale 位
     * @return 任意の桁数で四捨五入した結果。
     */
    public static double roundHalfUp(final double v, final int scale) {
        final BigDecimal bd = new BigDecimal(String.valueOf(v));
        return bd.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * obj が null なら EMPTY("")を返す。
     * @param obj オブジェクト
     * @return 非null なら obj.toString()
     */
    public static String defaultString(final Object obj) {  // TAKAESU: このメソッド、どっかにありそう
        return defaultString(obj, "");
    }

    /**
     * obj が null なら str を返す。
     * @param obj オブジェクト
     * @param str デフォルトの文字列
     * @return 非null なら obj.toString()
     */
    public static String defaultString(final Object obj, final String str) {
        return (null == obj) ? str : obj.toString();
    }

    /**
     * 一時ファイル名を作成する。
     * @param extension 拡張子
     * @return 一時ファイル名
     */
    public static String createTmpFile(final String extension) {
        final String tmpFolder = SystemUtils.JAVA_IO_TMPDIR;
        final int limit = 10;
        String tmpfilename = null;
        for (int i = 0; i < limit; i++) {
            final String prefix = Long.toString(System.currentTimeMillis());
            try {
                tmpfilename = File.createTempFile(prefix, extension, new File(tmpFolder)).getAbsolutePath();
            } catch (Exception e) {
                System.err.println("一時ファイル作成に失敗しました。");
                e.printStackTrace(System.err);
            }
            if (null != tmpfilename) {
                break;
            }
        }
        if (null == tmpfilename) {
            throw new IllegalStateException("一時ファイル作成に" + String.valueOf(limit) + "回失敗しました。");
        }
        return tmpfilename;
    }

    /**
     * 文字列、"yyyy-mm-dd" の dd の部分を 01 に置換する。<br>
     * すなわち、「その年月の一日」を得る。
     * @param date 10バイト以上の文字列
     * @return 置換できない場合、date を返す。
     */
    public static String getFirstDate(final String date) {
        if (null == date) {
            return date;
        }
        if (date.length() < 10) {
            return date;
        }
        return date.substring(0, 8) + "01";
    }

    /**
     * 日付文字列を Calendar に変換する。
     * @param dateStr 日付文字列
     * @return Calendar
     * @throws ParseException 例外
     */
    public static Calendar parseDate(final String dateStr) throws ParseException {
        return parseDate(dateStr, "yyyy-MM-dd");
    }

    private static Calendar parseDate(final String dateStr, final String pattern) throws ParseException {
        // 文字列を Date型に
        final SimpleDateFormat format = new SimpleDateFormat(pattern);
        final Date date = format.parse(dateStr);

        // Date型を Calendar に
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal;
    }

    /**
     * null またはゼロか?
     * @param numObj 数値オブジェクト
     * @return null またはゼロなら true
     */
    public static boolean isEmpty(final Number numObj) {
        if (null == numObj) {
            return true;
        }
        if (numObj.intValue() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 複数行の印字。
     * @param svf SVF
     * @param formField フォームフィールド名
     * @param data  データ
     * @param columnCount   列数
     * @param lineCount 行数
     */
    public static void printDetail(
            final Vrw32alp svf,
            final String formField,
            final String data,
            final int columnCount,
            final int lineCount
    ) {
        if (null == data) {
            return;
        }
        final String[] buf = getToken(data, columnCount, lineCount);
        for (int i = 0; i < buf.length; i++) {
            if (null == buf[i]) {
                continue;
            }
            svf.VrsOutn(formField, i + 1, buf[i]);
        }
    }

    private static String[] getToken(final String strx, final int fLen, final int fCnt) {
        if (strx == null || strx.length() == 0) {
            return null;
        }

        // 分割後の文字列の配列
        String sToken[] = new String[fCnt];

        // 1文字byteカウント用
        byte sByte[] = new byte[3];

        // 文字列のバイト数カウント
        int sLen = 0;

        // 文字列の開始位置
        int sSta = 0;
        int ib = 0;

        for (int sCur = 0; sCur < strx.length() && ib < fCnt; sCur++) {
            if (strx.charAt(sCur) == '\r') {
                continue;
            }

            if (strx.charAt(sCur) == '\n') {
                sToken[ib++] = strx.substring(sSta, sCur);
                sLen = 0;
                sSta = sCur + 1;
            } else {
                final String wrk = strx.substring(sCur, sCur + 1);
                try {
                    sByte = wrk.getBytes("MS932");
                } catch (final UnsupportedEncodingException e) {
                    sByte = wrk.getBytes();
                }
                sLen += sByte.length;
                if (sLen > fLen) {
                    sToken[ib++] = strx.substring(sSta, sCur);
                    sLen = sByte.length;
                    sSta = sCur;
                }
            }
        }
        if (sLen > 0 && ib < fCnt) {
            sToken[ib] = strx.substring(sSta);
        }

        return sToken;
    }

} // KNJServletUtils

// eof
