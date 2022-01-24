// kanji=漢字
/*
 * $Id: 226a63946d55cae9b2e21b591d4439fd9e6011d2 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 成績一覧表を印刷します。
 * @version $Id: 226a63946d55cae9b2e21b591d4439fd9e6011d2 $
 */
public class KNJD615P {
    
    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        new KNJD615V().svf_out(request, response);
    }
}
