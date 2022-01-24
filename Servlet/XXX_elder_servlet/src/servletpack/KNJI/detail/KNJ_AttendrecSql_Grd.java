// kanji=漢字
/*
 * $Id: 62bf3ca2982b381b8e0add21ef702029bef3d8b1 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI.detail;

import servletpack.KNJE.detail.KNJ_AttendrecSql;
import servletpack.KNJZ.detail.KNJDefineCode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 *  [進路情報・調査書]出欠記録データSQL作成(卒業生用)
 *
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため作成
 */

public class KNJ_AttendrecSql_Grd extends KNJ_AttendrecSql
{
    private static final Log log = LogFactory.getLog(KNJ_AttendrecSql_Grd.class);

    public KNJ_AttendrecSql_Grd() {
        this(new KNJDefineCode());
    }

    public KNJ_AttendrecSql_Grd(final KNJDefineCode definecode) {
        super(definecode, true);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "GRD_ATTENDREC_DAT";
    }
}
