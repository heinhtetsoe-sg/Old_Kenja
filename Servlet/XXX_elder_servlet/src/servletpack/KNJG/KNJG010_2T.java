// kanji=漢字
/*
 * $Id: 25694370a845e15239fb2b49c1835d882c44fc60 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.util.HashMap;
import java.util.Map;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;

/*
 *
 *  学校教育システム 賢者 [事務管理]
 *
 *                ＜ＫＮＪＧ０１０＿２＞  卒業生データ読み込み
 *
 *  2005/10/22 m-yama /KNJZ/KNJ_Schoolinfoを/KNJG/KNJ_SchoolinfoSqlへ変更 NO001
 */

public class KNJG010_2T extends KNJG010_1T{
    private static final Log log = LogFactory.getLog(KNJG010_2T.class);

    public KNJG010_2T(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        super(db2,svf,definecode);
        log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $ ");
    }

    public void pre_stat(final String ptype) {
        pre_stat(ptype, new HashMap());
    }

    /**
     * PrepareStatement作成
     */
    public void pre_stat(final String ptype, final Map paramMap) {
        paramMap.put("PRINT_GRD", "1");
        super.pre_stat(ptype, paramMap);
    }

}
