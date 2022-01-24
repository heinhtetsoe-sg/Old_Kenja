// kanji=漢字
/*
 * $Id: f25fc58472a1737382598ef2f4d14fa1a3a20e1c $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJD128V {
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJD620V().svf_out(request, response);
    }
}
