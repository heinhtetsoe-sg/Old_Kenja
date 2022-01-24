package servletpack.KNJG;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servletpack.KNJA.KNJA195A;
import servletpack.KNJE.KNJE070;

public class KNJG081A {

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
    	if ("futo".equals(request.getParameter("OUTPUT"))) {
    		new KNJA195A().svf_out(request, response);
    	} else {
    		new KNJE070().svf_out(request, response);
    	}
    }
}
