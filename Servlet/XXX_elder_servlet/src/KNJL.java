//package

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [入試処理]
 *
 *						＜ＫＮＪＬ＞  入試処理ＴＯＰ
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJL extends KNJServlet {
	private static final Log log = LogFactory.getLog(KNJL.class);

	public KNJL() {
		super("KNJL0000", log);
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
