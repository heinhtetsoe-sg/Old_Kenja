/*
 * $Id: 4b87a7a75ed5e9a30bf88d47ad1eccd78813466f $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１４Ｒ＞  面接Cランク成績一覧
 **/
public class KNJL314R {

    private static final Log log = LogFactory.getLog(KNJL314R.class);

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        // 成績一覧で処理
        new KNJL313R().svf_out(request, response);
    }
}

// eof
