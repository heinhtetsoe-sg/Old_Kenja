// kanji=漢字
/*
 * $Id: 447358a4df20967be03c67608d63e15cbbf999e9 $
 *
 */
package servletpack.KNJH;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 *  学校教育システム 賢者 [事務管理] 預かり保育名簿（事務用）
 */

public class KNJH189 {

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        
        new KNJH188().svf_out(request, response);
    }
}
