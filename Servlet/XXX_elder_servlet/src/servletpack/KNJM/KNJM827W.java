/*
 * $Id: 68cee57072dcd558c724f9411dd0ced435185a24 $
 *
 * 作成日: 2012/12/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * レポート提出状況一覧
 */
public class KNJM827W {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
    	new KNJM827().svf_out(request, response);
    }
    
}

// eof

