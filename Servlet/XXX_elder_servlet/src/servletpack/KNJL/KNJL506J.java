package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL506J {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String outPattern = request.getParameter("OUTPUT");
        if ("1".equals(outPattern)) {
            //得点チェックリスト
            new KNJL506J_1().svf_out(request, response);
        } else if ("2".equals(outPattern)) {
            //成績一覧表
            new KNJL506J_2().svf_out(request, response);
        }
    }
}//クラスの括り
