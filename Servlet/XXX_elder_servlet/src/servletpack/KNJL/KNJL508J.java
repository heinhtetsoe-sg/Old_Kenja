package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL508J {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String outPattern = request.getParameter("OUTPUT");
        if ("1".equals(outPattern)) {
            //入学者一覧表(入試基礎資料付き)
            new KNJL508J_1().svf_out(request, response);
        } else if ("2".equals(outPattern)) {
            //入学者一覧表
            new KNJL508J_2().svf_out(request, response);
        } else if ("3".equals(outPattern)) {
            //入学者タックシール
            new KNJL508J_3().svf_out(request, response);
        } else if ("4".equals(outPattern)) {
            //出身校タックシール
            new KNJL508J_4().svf_out(request, response);
        } else if ("5".equals(outPattern)) {
            //受付簿
            new KNJL508J_5().svf_out(request, response);
        }
    }
}//クラスの括り
