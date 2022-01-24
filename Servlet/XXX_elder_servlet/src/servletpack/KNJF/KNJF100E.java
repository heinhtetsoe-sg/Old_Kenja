// kanji=漢字
/*
 * $Id: 8350061eb823189c9d30b6f72ef786423ac61e0b $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJF;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJF100E {
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJF100BC().svf_out(request, response);
    }
}
