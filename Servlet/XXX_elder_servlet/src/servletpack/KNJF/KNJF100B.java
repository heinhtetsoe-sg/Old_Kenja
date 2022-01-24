// kanji=漢字
/*
 * $Id: f84624d633d8b73eab8fcb5df2fe8d19f7bfb28c $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJF;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJF100B {
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        new KNJF100BC().svf_out(request, response);
    }
}
