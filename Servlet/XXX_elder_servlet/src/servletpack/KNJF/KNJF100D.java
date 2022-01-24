// kanji=漢字
/*
 * $Id: bc396c343d1d3d58c8865dc8f888435ff0554d48 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJF;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJF100D {
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJF100BC().svf_out(request, response);
    }
}
