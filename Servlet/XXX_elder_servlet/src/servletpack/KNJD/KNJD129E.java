/**
 *
 *    学校教育システム 賢者 [成績管理] 成績伝票 文京
 */

package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KNJD129E {

    /**
     * KNJD.classから最初に呼ばれる処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {

        new KNJD620V().svf_out(request, response);

    }//doGetの括り

}//クラスの括り
