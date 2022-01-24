/*
 * $Id: c0c47ec49029d817ce4fa3db5b5f04d04b0b6005 $
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
public class KNJD418 {

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

