// kanji=漢字
/*
 * $Id: 834fa04f5902d0b8c5576389791b7ec4a718153a $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJJ;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJJ144F {
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        if("1".equals(request.getParameter("DISP"))) new KNJJ144F_1().svf_out(request, response); // 6.マラソン大会組別記録表
        if("2".equals(request.getParameter("DISP"))) new KNJJ144F_2().svf_out(request, response); // 7.男女別上位者一覧表
        if("3".equals(request.getParameter("DISP"))) new KNJJ144F_3().svf_out(request, response); // 8.学年別順位表

    }
}
