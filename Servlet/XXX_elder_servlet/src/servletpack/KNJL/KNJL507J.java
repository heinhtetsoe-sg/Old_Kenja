package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL507J {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String outPattern = request.getParameter("OUTPUT");
        if ("1".equals(outPattern)) {
        	//合格者一覧表(入試基礎資料付き)
            new KNJL507J_1().svf_out(request, response);
        } else if ("2".equals(outPattern)) {
            //合格者一覧表
            new KNJL507J_2().svf_out(request, response);
        } else if ("3".equals(outPattern)) {
            //合格者一覧表(掲示用)
            new KNJL507J_3().svf_out(request, response);
        } else if ("6".equals(outPattern)) {
            //入試集計表
            new KNJL507J_6().svf_out(request, response);
        }
    }
}//クラスの括り
