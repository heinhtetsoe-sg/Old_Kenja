package servletpack.KNJL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL505J {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String outPattern = request.getParameter("OUTPUT");
        if ("1".equals(outPattern)) {
            //入試基礎資料
            new KNJL505J_1().svf_out(request, response);
        } else if ("2".equals(outPattern)) {
            //受験者名簿
            new KNJL505J_2().svf_out(request, response);
        } else if ("3".equals(outPattern)) {
            //出欠者リスト
            new KNJL505J_3().svf_out(request, response);
        } else if ("4".equals(outPattern)) {
            //机上タックシール
            new KNJL505J_4().svf_out(request, response);
        }
    }
}//クラスの括り
