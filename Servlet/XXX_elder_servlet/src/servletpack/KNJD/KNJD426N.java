package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [奈良特支]
 *
 **/

public class KNJD426N {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        String outPattern = request.getParameter("OUTPUT_PTRN");
        if ("1".equals(outPattern)) {
            //知的用
            new KNJD426N_1().svf_out(request, response);
        } else if ("2".equals(outPattern)) {
            //準ずる教育
            new KNJD426N_2().svf_out(request, response);
        } else if ("3".equals(outPattern)) {
            //自立活動中心用
            new KNJD426N_3().svf_out(request, response);
        }
    }
}//クラスの括り
