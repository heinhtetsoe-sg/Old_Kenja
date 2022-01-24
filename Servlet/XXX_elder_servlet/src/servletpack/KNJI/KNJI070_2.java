// kanji=漢字
/*
 * $Id: 7bbdabd7c30d607d5cb00f8777b7e61ef56d2328 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE080_2;
import servletpack.KNJZ.detail.KNJDefineSchool;

/*
 *  学校教育システム 賢者 [卒業生管理] 学業成績証明書（卒業生用・英文）
 */

public class KNJI070_2 extends KNJE080_2 {

    private static final Log log = LogFactory.getLog(KNJI070_2.class);

    public KNJI070_2(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        super(db2, svf, definecode);
        log.fatal("$Revision: 61720 $ $Date: 2018-08-09 09:51:34 +0900 (木, 09 8 2018) $"); // CVSキーワードの取り扱いに注意
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei, final Map paramMap) {
        paramMap.put("PRINT_GRD", "1");
        super.pre_stat(hyotei, paramMap);
    }
}
