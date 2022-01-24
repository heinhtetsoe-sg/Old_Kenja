// kanji=漢字
/*
 * $Id: 74a266dd9935b00e465c7fb78a882f55769bf554 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJF;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJF100C {
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJF100BC().svf_out(request, response);
    }
}
