/*
 * $Id: b99fef0e7b30b7da23b4454c4633986377168ccf $
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
 *  学校教育システム 賢者 IB観点相関表印刷(MYP)
 */
public class KNJZ068E {

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

