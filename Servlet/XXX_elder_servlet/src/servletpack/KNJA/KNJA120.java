// kanji=漢字
/*
 * $Id: 0f257bbaca46fd139cdf11303d1d8a60ae1a9c66 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA120 {
    private static final Log log = LogFactory.getLog(KNJA120.class);

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        new KNJA120A().svf_out(request, response);
    }
}
