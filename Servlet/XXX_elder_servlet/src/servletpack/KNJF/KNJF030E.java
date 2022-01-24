// kanji=漢字
/*
 * $Id: de6780a3e719c2f4da1d896056cd9f3ac622913f $
 *
 * 作成日: 2009/10/06 11:39:11 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: de6780a3e719c2f4da1d896056cd9f3ac622913f $
 */
public class KNJF030E {


    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        new KNJF030C().svf_out(request, response);
    }
}

// eof
