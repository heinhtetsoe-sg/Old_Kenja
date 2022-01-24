// kanji=漢字
/*
 * $Id: c219a054144a3062d190232a92df53ada871e20b $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI.detail;

import servletpack.KNJE.detail.KNJ_MedexamSql;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 *  [進路情報・調査書]健康診断データSQL作成(卒業生用)
 *
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため作成
 */

public class KNJ_MedexamSql_Grd extends KNJ_MedexamSql
{
    private static final Log log = LogFactory.getLog(KNJ_MedexamSql_Grd.class);



    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "GRD_MEDEXAM_DET_DAT";
        tname2 = "GRD_MEDEXAM_HDAT";
    }
}
