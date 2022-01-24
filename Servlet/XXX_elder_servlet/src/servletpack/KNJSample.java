// kanji=漢字
/*
 * $Id: KNJSample.java 56595 2017-10-22 14:25:19Z maeshiro $
 *
 * 作成日: 2007/05/23 13:36:33 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 賢者帳票サンプル。
 * @author takaesu
 * @version $Id: KNJSample.java 56595 2017-10-22 14:25:19Z maeshiro $
 */
public class KNJSample {
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        out.println("<H1>hello servlet</H1>");
        out.close();
    }
} // KNJSample

// eof
