// kanji=漢字
/*
 * $Id$
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.util.HashMap;
import java.util.Map;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE080_1;
import servletpack.KNJZ.detail.KNJDefineSchool;

/*
 *  学校教育システム 賢者 [卒業生管理] 学業成績証明書（卒業生用・和文）
 */

public class KNJI070_1 extends KNJE080_1 {

    private static final Log log = LogFactory.getLog(KNJI070_1.class);

    public KNJI070_1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        super(db2, svf, definecode);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei) {
        pre_stat(hyotei, new HashMap());
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei, final Map paramMap) {
        paramMap.put("PRINT_GRD", "1");
        super.pre_stat(hyotei, paramMap);
    }
}
