// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJE070G {
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJE070().svf_out(request, response);
    }
}
