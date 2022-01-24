/*
 * $Id: b2cfe4404a570f2203554a5a2f733309001a8271 $
 *
 * 作成日: 2014/12/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  学校教育システム 賢者 IB通知表印刷(MYP)
 */
public class KNJD186H {

    private static final Log log = LogFactory.getLog(KNJD186H.class);

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        new KNJD186JK().svf_out(request, response);
    }
}

// eof

