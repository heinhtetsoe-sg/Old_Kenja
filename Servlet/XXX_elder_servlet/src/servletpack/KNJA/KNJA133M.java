/*
 * $Id: f351beb737464eaacf3121fb9cfd03c0c2fa26df $
 *
 * 作成日: 2011/02/04
 * 作成者: maesiro
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
*
*  学校教育システム 賢者 [学籍管理]  生徒指導要録  通信制高校用
*
*/
public class KNJA133M {
    private static final Log log = LogFactory.getLog(KNJA133M.class);

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        new KNJA130C.KNJA133M().svf_out(request, response);
    }
}

// eof
