package servletpack.KNJL;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL327F {

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        new KNJL326F().svf_out(request, response);
    }
}//クラスの括り
