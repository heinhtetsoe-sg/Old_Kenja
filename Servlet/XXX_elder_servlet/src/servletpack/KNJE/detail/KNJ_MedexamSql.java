// kanji=漢字
/*
 * $Id: 355288e47b584f019923158c1c00764c98b0d189 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  [進路情報・調査書]健康診断データSQL作成
 *
 *  2004/03/16 yamashiro・結核コードでname_mstを検索する際のコードを'F100'に変更
 *
 */

public class KNJ_MedexamSql{

    private static final Log log = LogFactory.getLog(KNJ_MedexamSql.class);
    public String tname1 = null;    //05/07/25 MEDEXAM_DET_DAT
    public String tname2 = null;    //05/07/25 MEDEXAM_HDAT

    public KNJ_MedexamSql() {
        log.debug("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }
    
    /**
     *  健康診断データのSQL
     */
    public String pre_sql(){

        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/25Build
        String sql = null;
    
        try{
            sql = "SELECT "
                + "T2.DATE,"
                + "T1.HEIGHT,"
                + "T1.WEIGHT,"
                + "T1.R_BAREVISION,"
                + "T1.L_BAREVISION,"
                + "T1.R_VISION,"
                + "T1.L_VISION,"
                + "T1.R_BAREVISION_MARK,"
                + "T1.L_BAREVISION_MARK,"
                + "T1.R_VISION_MARK,"
                + "T1.L_VISION_MARK,"
                + "(SELECT MAX(NAMESPARE2) FROM NAME_MST WHERE NAMECD1 = 'F011') AS BAREVISION_LINE_FLG,"
                + "(SELECT (CASE WHEN NAME2 IS NULL THEN NAME1 ELSE NAME2 END) FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = R_EAR) AS R_EAR,"
                + "(SELECT (CASE WHEN NAME2 IS NULL THEN NAME1 ELSE NAME2 END) FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = L_EAR) AS L_EAR,"
                + "(SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = R_EAR) AS R_EAR_CLEAR_FLG,"
                + "(SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = L_EAR) AS L_EAR_CLEAR_FLG,"
                + "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F100' AND NAMECD2 = TB_REMARKCD) AS RB_REMARK "
            + "FROM "
                +  tname1 + " T1 "                                                //05/07/25Modify
                + "INNER JOIN " + tname2 + " T2 ON T1.SCHREGNO = T2.SCHREGNO "    //05/07/25Modify
                                            + "AND T1.YEAR = T2.YEAR "
            + "WHERE  "
                    + "T1.SCHREGNO =? "
                + "AND T1.YEAR <=? "
            + "ORDER BY "
                + "T1.YEAR DESC";

        } catch( Exception ex ){
            log.error("pre_sql error! " + ex );
        }
    
        return sql;

    }

    /**
     *  健康診断データのSQL
     */
    public String preSqlMark(){

        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/25Build
        String sql = null;
    
        try{
            sql = "SELECT "
                + "T2.DATE,"
                + "T1.HEIGHT,"
                + "T1.WEIGHT,"
                + "T1.R_BAREVISION,"
                + "T1.R_BAREVISION_MARK,"
                + "T1.L_BAREVISION,"
                + "T1.L_BAREVISION_MARK,"
                + "T1.R_VISION,"
                + "T1.R_VISION_MARK,"
                + "T1.L_VISION,"
                + "T1.L_VISION_MARK,"
                + "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = R_EAR) AS R_EAR,"
                + "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = L_EAR) AS L_EAR,"
                + "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F100' AND NAMECD2 = TB_REMARKCD) AS RB_REMARK "
            + "FROM "
                +  tname1 + " T1 "                                                //05/07/25Modify
                + "INNER JOIN " + tname2 + " T2 ON T1.SCHREGNO = T2.SCHREGNO "    //05/07/25Modify
                                            + "AND T1.YEAR = T2.YEAR "
            + "WHERE  "
                    + "T1.SCHREGNO =? "
                + "AND T1.YEAR <=? "
            + "ORDER BY "
                + "T1.YEAR DESC";

        } catch( Exception ex ){
            log.error("pre_sql error! " + ex );
        }
    
        return sql;

    }


    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "MEDEXAM_DET_DAT";
        tname2 = "MEDEXAM_HDAT";
    }

}
