/*
 * $Id: 68d652c38840479351691a8ddadf68b033a63f0b $
 *
 * 作成日: 2012/12/11
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 定期考査補充指導状況
 */
public class KNJM836W {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

    	new KNJM835().svf_out(request, response);
    }
    
}

// eof

