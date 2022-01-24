/*
 * $Id: 89230edf5896b9959b8404cd8072b295cdb4fffd $
 *
 * 作成日: 2014/12/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  学校教育システム 賢者 IB観点相関表印刷(DP)
 */
public class KNJZ068F {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        new KNJZ068EF().svf_out(request, response);
    }
}

// eof

