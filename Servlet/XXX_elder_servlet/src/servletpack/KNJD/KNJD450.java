/*
 * $Id: 5683c982ee4d53814d62b16425556d5c49f967b2 $
 *
 * 作成日: 2014/10/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  学校教育システム 賢者 [特別支援学校] 
 */
public class KNJD450 {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        new KNJD451().svf_out(request, response);
    }
}

// eof

