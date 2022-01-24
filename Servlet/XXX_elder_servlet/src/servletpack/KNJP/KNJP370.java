// kanji=漢字
/*
 * $Id: bc51dba58d8dcf5caba19faaa4fcdebb24d24dec $
 *
 * 作成日: 2005/06/21 11:40:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [近大]
 *
 *                  ＜ＫＮＪＰ３７０＞  振込依頼書・辺戻通知書
 *
 *  2005/06/21 m-yama 作成日
 *  @version $Id: bc51dba58d8dcf5caba19faaa4fcdebb24d24dec $
 **/

public class KNJP370 {

    private static final Log log = LogFactory.getLog(KNJP370.class);

    /**
     * KNJP.classから最初に呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        try {
            final String output = request.getParameter("OUTPUT");
            if (output.equals("1")) {
                final KNJP370Transfer obj = new KNJP370Transfer();
                obj.svf_out(request, response);
            } else {
                final KNJP370Repai obj = new KNJP370Repai();
                obj.svf_out(request, response);
            }
        } finally {
            ;
        }
    }
} //クラスの括り
