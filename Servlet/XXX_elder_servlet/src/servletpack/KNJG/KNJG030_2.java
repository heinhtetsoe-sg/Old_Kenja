// kanji=漢字
/*
 * $Id: dcf766c537b183a6057476bcf0cb03bbb88d54e9 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.util.Map;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;

/*
 *  学校教育システム 賢者 [事務管理]  単位習得証明書
 */

public class KNJG030_2 extends KNJG030_1 {

    private static final Log log = LogFactory.getLog(KNJG030_2.class);

    public KNJG030_2(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode) {
        super(db2, svf ,definecode);
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei, final Map map) {
        map.put("PRINT_GRD", "1");
        super.pre_stat(hyotei, map);
    }
}
