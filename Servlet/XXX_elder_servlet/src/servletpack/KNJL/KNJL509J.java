package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL509J {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String outPattern = request.getParameter("OUTPUT");
        if ("1".equals(outPattern)) {
            //合格証
            new KNJL509J_1().svf_out(request, response);
        } else if ("2".equals(outPattern)) {
            //入学許可証
            new KNJL509J_2().svf_out(request, response);
        }
    }
}//クラスの括り
