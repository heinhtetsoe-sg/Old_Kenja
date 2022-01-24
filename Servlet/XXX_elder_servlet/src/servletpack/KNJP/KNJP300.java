/**
 *
 * $Id: 67e24bd6f26da91c5329fc80363693f6046c301e $
 * 学校教育システム 賢者 [学籍管理]  校納金振込み用紙/タックシール印刷
 *
 * 2005/06/01  作成 m-yama
 *
 */

package servletpack.KNJP;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJP300 {

    private static final Log log = LogFactory.getLog(KNJP300.class);

    /**
     *  KNJP300最初のクラス
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        try {
            String output = request.getParameter("OUTPUT2");
            if (!output.equals("3")){
                KNJP300KouNou obj = new KNJP300KouNou();
                obj.svf_out( request, response );
            }else {
                KNJP300Tack obj = new KNJP300Tack();
                obj.svf_out( request, response );
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }
}//クラスの括り
