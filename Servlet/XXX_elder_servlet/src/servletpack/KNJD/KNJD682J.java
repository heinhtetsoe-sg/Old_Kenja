/*
 * $Id: bafba781daba51689576e0097f311308db806c3e $
 *
 * 作成日: 2016/07/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 文京学園 中学 クラス別朝間結果一覧表
 */
public class KNJD682J {

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        new KNJD682().svf_out(request, response);
    }
}

// eof

