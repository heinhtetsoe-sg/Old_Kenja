//package

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                      ＜ＫＮＪＡ＞  学籍管理ＴＯＰ
 *
 * 2004/05/07 nakamoto ＨＲ別名票・講座別名票（湧心館）を追加
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJX extends KNJServlet {
    private static final Log log = LogFactory.getLog(KNJX.class);

    public KNJX() {
        super("KNJX0000", log);
    }

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void doGet(
            final String prgid,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.warn("unknown prgid:" + prgid);
    }
}//クラスの括り

