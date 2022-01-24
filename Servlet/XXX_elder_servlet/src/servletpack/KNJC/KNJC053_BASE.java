// kanji=漢字
/*
 * $Id: 582a03114f4c9cde2a9b4c501aced1ef25436a08 $
 *
 * 作成日: 2005/03/28
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJC053_BASE {

    private static final Log log = LogFactory.getLog(KNJC053_BASE.class);

    /**
     *   １月〜３月の「学期＋月」の処理 （２学期制における集計処理に対応）
     *   2006/01/27 Build NO004
     */
    public static String retSemesterMonthValue(final String strx)
    {
        String str = null;
        try {
            if (Integer.parseInt(strx.substring(1, strx.length())) < 4) {
                str = String.valueOf(Integer.parseInt(strx.substring(0, 1)) + 1) + "" + strx.substring(1, strx.length());
            } else {
                str = strx;
            }
        } catch (Exception ex) {
            log.error("retSemesterMonthValue!",ex);
        }
        log.debug("retSemesterMonthValue=" + str);
        return str;
    }

}//クラスの括り
