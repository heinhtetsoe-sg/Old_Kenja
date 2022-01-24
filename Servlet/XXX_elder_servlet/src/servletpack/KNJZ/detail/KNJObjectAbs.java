// kanji=漢字
/*
 * $Id: 5251a062d94491583f3760794097a1eaacd40abd $
 *
 * 作成日: 2005/04/26
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
*
*  編集クラスのスーパークラス
*    2005/04/26 yamashiro
*
*/

public abstract class KNJObjectAbs{

    private static final Log log = LogFactory.getLog(KNJObjectAbs.class);

    /**
     *
     *  文字列の長さをバイト数で返す処理
     *
     */
    public abstract int retStringByteValue( String str, int i );


    /**
     *
     *  文字列を改行マークおよび文字数で区切って返す処理
     *
     */
    public abstract ArrayList retDividString( String targetstr, int f_len, int f_cnt );


    /**
     *
     *  文字列を改行マークおよび文字数で区切って返す処理
     *
     */
    public abstract ArrayList retDividConectString( String targetstr, int f_len, int f_cnt );


}
