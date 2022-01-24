// kanji=漢字
/*
 * $Id: 6fd57cb082f0191b63e3d7cda2c4b5ef85045ea7 $
 *
 * 作成日: 2005/03/30 14:27:31 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
 *
 *  学校教育システム 賢者 [成績管理]  中学校成績通知票
 *
 *  2005/03/30 yamashiro 新規作成
 *  2005/05/18 yamashiro 印刷処理はKNJD171K_Impactに移行し、KNJD171K_ImpactとKNJD171K_Laserの起動処理を行う
 *
 */

package servletpack.KNJD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD171K {

    private static final Log log = LogFactory.getLog(KNJD171K.class);


    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response){

        try {
            KNJD171K_Laser obj = new KNJD171K_Laser();
            obj.svf_out( request, response );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}
