// kanji=漢字
/*
 * $Id: 5cf22700b1d32a988f7e746d6b0161ae509df1a1 $
 *
 * 作成日: 2005/04/26
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
*
*  文字列チェック＆編集
*  2005/04/26 yamashiro
*  2005/07/10 yamashiro
*
*/

public class KNJEditString extends KNJObjectAbs {

    private static final Log log = LogFactory.getLog(KNJEditString.class);

    /**
     *
     *  文字列の長さをバイト数で返す処理
     *    2005/05/27
     */
    public int retStringByteValue(final String str, final int i) {
        return getMS932ByteLength(str);
    }

    /**
     *  文字列の長さをバイト数で返す処理
     */
    public static int getMS932ByteLength(final String str) {
        if (str == null) {
            return 0;
        }
        int retval = 0;
        try {
            if (null != str) {
                retval = str.getBytes("MS932").length;            //byte数を取得
            }
        } catch (Exception ex) {
            log.error("retStringByteValue error!", ex);
        }
        return retval;
    }

    /**
     *
     *  文字列を改行マークおよび文字数で区切って返す処理
     *
     */
    public ArrayList retDividString(final String targetstr, final int dividlen, final int dividnum) {
        if (targetstr == null) {
            return null;
        }
        ArrayList retarraylist = new ArrayList(dividnum);           //編集後文字列を格納する配列
        StringBuffer stry = new StringBuffer();                     //SVF出力用
        int len = 0;                                                //１回分の文字列の長さ
        int i = 0;                                                  //分割数カウント
        try {
            for (int j = 0; j < targetstr.length(); j++) {
                final String strz = targetstr.substring(j, j + 1); //1文字を取り出す
                final byte[] SendB = strz.getBytes( "MS932" );
                if (strz.equals("\r") || strz.equals("\n")) {       //改行マークがある場合、強制的に次行へ
                    len = dividlen + 1;
                    continue;
                }
                if ((len + SendB.length) > dividlen) {
                    retarraylist.add(stry.toString());            //１行文字列
                    i++;
                    if (i == dividnum) {
                        break;
                    }
                    len = 0;
                }
                if (len == 0) {
                    stry.delete(0, stry.length());
                }
                stry.append(strz);
                len = len + SendB.length;
            }
            if (0 < len && i < dividnum) {
                retarraylist.add(stry.toString());
            }
        } catch (Exception ex) {
            log.error("retDividString error! ",ex);
        }
        return retarraylist;
    }

    /**
     *
     *  文字列を改行マークで連結して返す処理
     *
     */
    public ArrayList retDividConectString(final String targetstr, final int dividlen, final int dividnum) {
        if (targetstr == null) {
            return null;
        }
        final ArrayList retarraylist = new ArrayList(dividnum);    //編集後文字列を格納する配列
        StringBuffer stry = new StringBuffer();                     //SVF出力用
        int len = 0;                                                //１回分の文字列の長さ
        int i = 0;                                                  //分割数カウント
        try {
            for (int j = 0; j < targetstr.length(); j++) {
                final String strz = targetstr.substring(j, j + 1);  //1文字を取り出す
                if (strz.equals("\r") || strz.equals("\n")) {       //改行マークがある場合、強制的に次行へ
                    continue;
                }
                final byte[] SendB = strz.getBytes( "MS932" );
                if ((len + SendB.length ) > dividlen) {
                    retarraylist.add(stry.toString());            //１行文字列
                    i++;
                    if (i == dividnum) {
                        break;
                    }
                    len = 0;
                }
                if (len == 0) {
                    stry.delete(0, stry.length());
                }
                stry.append(strz);
                len = len + SendB.length;
            }
            if (0 < len && i < dividnum) {
                retarraylist.add(stry.toString());
            }
        } catch (Exception ex) {
            log.error("retDividString error!", ex);
        }
        return retarraylist;
    }

    /**
     *   SVF-FORM 文字列がnullの場合ブランクを返す処理
     *   2005/07/10 Build
     */
    public String retStringNullToBlank(final String str) {
        return StringUtils.defaultString(str);
    }

    /**
     *   SVF-FORM 文字列がnullの場合ブランクを返す処理
     *            引数 nzero==trueの場合はゼロもブランクを返す
     *   2005/07/10 Build
     */
    public String retStringNullToBlank(final String str, final boolean nzero ) {
        String retval = null;
        try {
            if (str == null) {
                retval = "";
            } else if (nzero && str.equals("0")) {
                retval = "";
            } else {
                retval = str;
            }
        } catch (Exception ex) {
            log.error( "printSvfRegdOut error! ", ex);
            retval = "";
        }
        return retval;
    }

}
