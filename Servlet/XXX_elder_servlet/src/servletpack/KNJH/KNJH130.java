// kanji=漢字
/*
 * $Id: 2120de37a68a9ee9733d9f54c67e8493a47f4d8d $
 *
 * 作成日: 2005/04/13
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [生徒指導情報システム]
 *
 *					＜ＫＮＪＨ１３０＞  保護者/学費負担者一覧Main
 *
 *  2005/04/13 m-yama 新規作成
 *  2005/11/28 m-yama 出力帳票追加に伴う修正
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH130 {

    private static final Log log = LogFactory.getLog(KNJH130.class);

    /**
     *  KNJH130最初のクラス
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
            {
        try {
            final String outputAddr = request.getParameter("OUTPUTADDR");
            final String outputB = request.getParameter("OUTPUTB");
            if (outputB.equals("1")){
                KNJH130KenItiran obj = new KNJH130KenItiran();
                log.debug("KNJH130KenItiran");
                obj.svf_out( request, response );
            }else if(outputB.equals("2")){
                if ("1".equals(outputAddr)) {
                    KNJH130KenMeiboAddr obj = new KNJH130KenMeiboAddr();
                    log.debug("KNJH130KenMeibo");
                    obj.svf_out(request, response);
                } else {
                    KNJH130KenMeibo obj = new KNJH130KenMeibo();
                    log.debug("KNJH130KenMeibo");
                    obj.svf_out(request, response);
                }
            }else {
                KNJH130TiikiItiran obj = new KNJH130TiikiItiran();
                log.debug("KNJH130TiikiItiran");
                obj.svf_out( request, response );
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
            }
}//クラスの括り
