/*
 * $Id: e05818b98ec27b69c36d1d6c72b8072c3e52b964 $
 *
 * 作成日: 2013/07/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJD128L {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        new KNJD620V().svf_out(request, response);
    }
}

// eof

