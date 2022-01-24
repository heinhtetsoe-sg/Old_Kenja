import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// kanji=漢字
/*
 * $Id: hello_servlet.java 56595 2017-10-22 14:25:19Z maeshiro $
 *
 * 作成日: 2006/02/17 11:48:51 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */

/**
 * 賢者の実験用。
 * @author takaesu
 * @version $Id: hello_servlet.java 56595 2017-10-22 14:25:19Z maeshiro $
 */
public class hello_servlet extends HttpServlet {

    /** {@inheritDoc} */
    public void doPost(
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        doIt(req, resp);
    }

    /** {@inheritDoc} */
    public void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        doIt(req, resp);
    }

    private void doIt(
            final HttpServletRequest req,
            final HttpServletResponse resp
    ) throws ServletException, IOException {
        if (null == resp) {
            return;
        }
//        resp.setContentType("text/html");
        resp.setContentType("text/csv; name=\"foo.txt\"");
        resp.setHeader("Content-disposition", "attachment; filename=\"aaa.csv\"");
        
        PrintWriter out = null;
        try {
            out = resp.getWriter();
        } catch (final IOException e) {
             e.printStackTrace();
        }
//        String className = getClass().getName();
        out.println("a1,b1,c1");
        out.println("a2,b2,c2");
        /*
        out.println("<html>");
        out.println("<head><title>hello " + className + "</title><head>");
        out.println("<body>");
        out.println("<h1>hello " + className + "</h1>");
        out.println("<h1>" + new File(".").getAbsolutePath() + "</h1>");
        out.println("<PRE>");
        out.println("java.io.tmpdir = " + System.getProperty("java.io.tmpdir"));
        out.println("</PRE>");
        out.println("</body>");
        out.println("</html>");
        */
        out.close();
    }
} // hello_servlet
// eof
