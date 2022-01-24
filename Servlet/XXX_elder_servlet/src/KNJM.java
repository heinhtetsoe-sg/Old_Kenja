//package

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [通信制]
 *
 *						＜ＫＮＪＭ＞  
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM extends KNJServlet {
	private static final Log log = LogFactory.getLog(KNJM.class);

	public KNJM() {
		super("KNJM0000", log);
	}

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void doGet(
			final String prgid,
			final HttpServletRequest request,
			final HttpServletResponse response
	) throws ServletException, IOException {
		log.warn("unknown prgid:" + prgid);
	}
}//クラスの括り
