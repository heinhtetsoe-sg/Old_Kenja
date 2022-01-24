// kanji=漢字
/*
 * $Id: 6018adf46e40c56f2616863452130633f2e69d76 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 *
 *  2005/07/21 yamashiro 
 */

public abstract class KNJGCertificate{

    private static final Log log = LogFactory.getLog(KNJGCertificate.class);
    public KNJDefineCode definecode;        //各学校における定数等設定


    /**
     *  PrepareStatement close
     */
    public abstract void pre_stat_f();


    /**
     *  クラス内で使用する定数設定
     */
    public void setClasscode( DB2UDB db2, String year )
    {
        try {
            definecode = new KNJDefineCode();
            definecode.defineCode( db2, year );         //各学校における定数等設定
log.debug("semesdiv="+definecode.semesdiv + "   absent_cov="+definecode.absent_cov + "   absent_cov_late="+definecode.absent_cov_late);
        } catch( Exception ex ){
            log.warn("semesterdiv-get error!",ex);
        }
    }

}
