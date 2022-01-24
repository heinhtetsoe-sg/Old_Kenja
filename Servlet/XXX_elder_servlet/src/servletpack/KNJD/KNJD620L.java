/*
 * $Id: be16cd4768a13301121ecc67dfe8d7c4451df05c $
 *
 * 作成日: 2013/07/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJD620L {

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

