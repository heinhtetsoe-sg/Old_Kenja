//package

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import jp.co.alp.kenja.servlet.hiro.knjb.KNJB0045B;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [時間割管理]
 *
 *						＜ＫＮＪＢ＞  時間割管理ＴＯＰ
 *
 *	2004/02/17 nakamoto  KNJB140/KNJB150を追加
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJB extends KNJServlet {
	private static final Log log = LogFactory.getLog(KNJB.class);

	public KNJB() {
		super("KNJB0000", log);
	}

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void doGet(
			final String prgid,
			final HttpServletRequest request,
			final HttpServletResponse response
	) throws ServletException, IOException {
        // ★ コンパイルエラーを回避する為、エラー個所をコメント化した。
        //   jp.co.alp.kenja.servlet.hiro.knjb.KNJB0045B を使っているから。
        //   ソース整理・移行が完了し、 XXX_elder_servlet プロジェクトが整理できるまでの措置。
        /*
		if( prgid.equals("KNJB0045B")){				//時間割チェックリスト（バッチ処理）
       		KNJB0045B batch = new KNJB0045B();
       		batch.doBatch(this, request, response);
		} else {
			log.warn("unknown prgid:" + prgid);
		}
        */
	}
}//クラスの括り
