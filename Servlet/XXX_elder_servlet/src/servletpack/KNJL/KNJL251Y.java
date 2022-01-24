/*
 * $Id: 9b5573bdeb26e7ac58ac5954405bc215584623a1 $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ２５１Ｙ＞  各種通知書
 **/
public class KNJL251Y {
    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        new KNJL409Y().svf_out(request, response);
    }
}

// eof
