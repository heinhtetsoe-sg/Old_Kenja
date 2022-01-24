/*
 * $Id: 17dd976ae255978ec1873e0eff12f00c1cfb0ca1 $
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
 * 定期考査欠点者一覧
 */
public class KNJM835W {

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

