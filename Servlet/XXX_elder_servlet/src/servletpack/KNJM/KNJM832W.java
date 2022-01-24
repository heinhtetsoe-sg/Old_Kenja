/*
 * $Id: 3cef65aa0bd70cfcbec30b54d13139efe58275e1 $
 *
 * 作成日: 2012/12/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 単位認定一覧表 入力確認シート (KNJM832W)
 */
public class KNJM832W {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
    	
    	new KNJM831W().svf_out(request, response);
    }
}

// eof

