// kanji=漢字
/*
 * $Id: 7bd53573277780fa841dfaa60893b8ed32990c6a $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ４７０＞  不合格者一覧表
 *
 *  2005/09/14 m-yama 作成日
 **/

public class KNJM470 {

    private static final Log log = LogFactory.getLog(KNJM470.class);

    /**
     *  KNJM470最初のクラス
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        try {
            log.fatal("$Revision: 56595 $");
            KNJM470sc_rep obj = new KNJM470sc_rep();
            obj.svf_out( request, response );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }
}//クラスの括り
