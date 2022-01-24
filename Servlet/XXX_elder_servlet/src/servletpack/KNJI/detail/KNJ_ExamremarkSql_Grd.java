// kanji=漢字
/*
 * $Id: 71d2ff08e37309ae17bc15c6f7df8218a48d3195 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI.detail;

import servletpack.KNJE.detail.KNJ_ExamremarkSql;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 *  調査書等所見データSQL作成(卒業生用)
 *
 *  2004/08/19 yamashiro・調査書所見データのフィールド名変更に伴い修正。
 *  2005/07/22 yamashiro・在校生用と卒業生用のSQLを共通化するため作成
 */

public class KNJ_ExamremarkSql_Grd extends KNJ_ExamremarkSql
{
    private static final Log log = LogFactory.getLog(KNJ_ExamremarkSql_Grd.class);

    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/22 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "GRD_HEXAM_ENTREMARK_HDAT";
        tname2 = "GRD_HEXAM_ENTREMARK_DAT";
        tname3 = "GRD_HEXAM_EMPREMARK_DAT";
    }
}
