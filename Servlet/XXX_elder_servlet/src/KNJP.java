import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import servletpack.KNJWP.KNJWP300B;

import java.io.*;

public class KNJP extends KNJServlet {
    private static final Log log = LogFactory.getLog(KNJP.class);

    public KNJP() {
        super("KNJP0000", log);
    }

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void doGet(
            final String prgid,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        if ("KNJWP300B".equals(prgid)) {
//            log.fatal(prgid + "のバッチ処理を起動。");
//            final KNJWP300B batch = new KNJWP300B();
//            batch.doBatch(this, request, response);
//            log.fatal(prgid + "のバッチ処理が終了。");
        } else {
            log.warn("unknown prgid:" + prgid);
        }
    }
}
