// kanji=漢字
/*
 * $Id: 7e9d95b22db80307dcd09b92ba39443b82a1ff78 $
 *
 * 作成日: 2007/06/07
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 7e9d95b22db80307dcd09b92ba39443b82a1ff78 $
 */
public class KNJH131 {

    private static final Log log = LogFactory.getLog(KNJH131.class);

	/**
	 *  KNJH131最初のクラス
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		try {
		    String output = request.getParameter("OUTPUTB");
		    if (output.equals("1")){
				KNJH131KenItiran obj = new KNJH131KenItiran();
log.debug("KNJH131KenItiran");
				obj.svf_out( request, response );
			}else if(output.equals("2")){
				KNJH131KenMeibo obj = new KNJH131KenMeibo();
log.debug("KNJH131KenMeibo");
				obj.svf_out( request, response );
			}else {
				KNJH131TiikiItiran obj = new KNJH131TiikiItiran();
log.debug("KNJH131TiikiItiran");
				obj.svf_out( request, response );
			}
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
	}
}//クラスの括り
