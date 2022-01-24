/*
 * $Id: acd80ee7bd78f949e0d4839ccd0457614bf5f04d $
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
 *  学校教育システム 賢者 [特別支援学校]  指導計画（通知票）印刷
 */
public class KNJD419 {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        new KNJD421().svf_out(request, response);
    }
}

// eof

