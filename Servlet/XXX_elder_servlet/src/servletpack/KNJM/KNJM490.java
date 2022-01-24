package servletpack.KNJM;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [通信制]
 *
 *					＜ＫＮＪＭ４９０＞  個人別学習状況一覧
 *
 *	2005/06/28 m-yama 作成日
 **/

public class KNJM490 {

    private static final Log log = LogFactory.getLog(KNJM490.class);

	/**
	 *  KNJM490最初のクラス
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		try {
		    String output = request.getParameter("OUTPUT2");
		    if (output.equals("1")){
				KNJM490sc_rep obj = new KNJM490sc_rep();
				obj.svf_out( request, response );
			}else if (output.equals("2")){
				KNJM490school obj = new KNJM490school();
log.debug("1111111");
				obj.svf_out( request, response );
			}else {
				KNJM490report obj = new KNJM490report();
				obj.svf_out( request, response );
			}
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
	}
}//クラスの括り
