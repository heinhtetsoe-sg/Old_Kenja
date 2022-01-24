/*
 * $Id: d3ee2b6fa5c3a6e22ed04204510c1a0bee8098a4 $
 *
 * 作成日: 2015/08/05
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
 * 文京学園 評定平均分布表
 */
public class KNJD678 {

    private static final Log log = LogFactory.getLog(KNJD678.class);

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        KNJD666 d666 = new KNJD666();
        d666.svf_out(request, response);
    }
}

// eof

