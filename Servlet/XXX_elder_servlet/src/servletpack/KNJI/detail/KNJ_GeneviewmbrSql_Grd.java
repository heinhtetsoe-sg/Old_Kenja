// kanji=漢字
/*
 * $Id: bd00d0babfe4e6ffa56209976ad6babc69a5f53f $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI.detail;

import servletpack.KNJE.detail.KNJ_GeneviewmbrSql;
import servletpack.KNJZ.detail.KNJDefineSchool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 *  [進路情報・調査書]成績概評人数データSQL作成(卒業生用)
 *
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため作成
 */

public class KNJ_GeneviewmbrSql_Grd extends KNJ_GeneviewmbrSql
{
    private static final Log log = LogFactory.getLog(KNJ_GeneviewmbrSql_Grd.class);

    public KNJ_GeneviewmbrSql_Grd(KNJDefineSchool definecode) {
        super(definecode);
        log.debug(" KNJI.detail.KNJ_GeneviewmbrSql_Grd $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "GRD_REGD_DAT";
    }
}
