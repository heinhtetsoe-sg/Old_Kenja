// kanji=漢字
/*
 * $Id: 3a779fc8eab48d7044c0061580b76d802ea96693 $
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
 *  調査所用所見データSQL作成
 *
 *  2004/08/19 yamashiro・調査書所見データのフィールド名変更に伴い修正。
 *  2005/07/19 yamashiro・HEXAM_ENTREMARK_HDATが存在しない場合を考慮して修正
 *  2005/07/22 yamashiro・在校生用と卒業生用のSQLを共通化するため使用テーブル名を変数に変更
 *
 */

public class KNJ_ExamremarkSql
{
    private static final Log log = LogFactory.getLog(KNJ_ExamremarkSql.class);

    public String tname1 = null;    //05/07/22 HEXAM_ENTREMARK_HDAT
    public String tname2 = null;    //05/07/22 HEXAM_ENTREMARK_DAT
    public String tname3 = null;    //05/07/22 HEXAM_EMPREMARK_DAT
    public String tname4 = null;    //05/07/22 HEXAM_EMPREMARK_HDAT


    /**
     *  調査書進学用所見データのSQL
     *  2005/07/19 Modify 「HEXAM_ENTREMARK_HDATは必須ではない」と判明したことに対応
     */
    public String pre_sql_ent(final String useSyojikou3)
    {
        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/22Build
        String sql = null;
        try{
            sql = "SELECT "
                // 05/07/19 + "W1.SCHREGNO,"
                + "VALUE(W1.SCHREGNO, W2.SCHREGNO) AS SCHREGNO, "   //05/07/19Modify
                + "W1.COMMENTEX_A_CD,"
                + "W1.DISEASE,"
                + "W1.DOC_REMARK,"
                + "W1.TR_REMARK,"
                + "W1.TOTALSTUDYACT,"   // 04/08/18Modify
                + "W1.TOTALSTUDYVAL,"
                + "W1.REMARK,"
                + "W2.ANNUAL,"
                + "W2.YEAR,"
                + "W2.ATTENDREC_REMARK,"
                + "W2.SPECIALACTREC,";
                if (useSyojikou3.equals("1")) {
                    sql = sql + "W2.TRAIN_REF1, "
                    + "W2.TRAIN_REF2, "
                    + "W2.TRAIN_REF3, ";
                }
      sql = sql + "W2.TRAIN_REF, "
                + "W2.TOTALSTUDYACT AS DAT_TOTALSTUDYACT, "
                + "W2.TOTALSTUDYVAL AS DAT_TOTALSTUDYVAL "
                + "FROM "
                /* **************
                + "HEXAM_ENTREMARK_HDAT W1 "
                + "LEFT JOIN HEXAM_ENTREMARK_DAT W2 ON W2.SCHREGNO = W1.SCHREGNO "
                                                    + "AND W2.YEAR <=? "
            + "WHERE "
                + "W1.SCHREGNO =? "
                /* *** 05/07/19Modify *** */
                //+ "(SELECT * FROM HEXAM_ENTREMARK_HDAT WHERE SCHREGNO = ? )W1 "
                + "(SELECT * FROM " + tname1 + " WHERE SCHREGNO = ? )W1 "
                + "FULL OUTER JOIN "
                //+ "(SELECT * FROM HEXAM_ENTREMARK_DAT WHERE SCHREGNO = ? AND YEAR <= ? )W2 ON W1.SCHREGNO = W2.SCHREGNO "
                + "(SELECT * FROM " + tname2 + " WHERE SCHREGNO = ? AND YEAR <= ? )W2 ON W1.SCHREGNO = W2.SCHREGNO "

            + "ORDER BY W2.ANNUAL, W2.YEAR ";

        } catch( Exception ex ){
            log.error("[KNJ_ExamremarkSql]pre_sql_ent error!" + ex );
        }
        return sql;
    }


    /**
     *  調査書進学用所見データTITLEのSQL
     *  2005/07/19 Modify 「HEXAM_ENTREMARK_HDATは必須ではない」と判明したことに対応
     */
    public String preSqlEntTitle()
    {
        if (tname1 == null) setFieldName();
        String sql = null;
        try{
            sql = "SELECT "
                + "W2.ANNUAL,"
                + "W2.YEAR "
                + "FROM "
                + "(SELECT * FROM " + tname1 + " WHERE SCHREGNO = ? ) W1 "
                + "FULL OUTER JOIN "
                + "(SELECT * FROM " + tname2 + " WHERE SCHREGNO = ? AND YEAR <= ? )W2 ON W1.SCHREGNO = W2.SCHREGNO "
                + "GROUP BY W2.ANNUAL, W2.YEAR "
                + "ORDER BY W2.ANNUAL ";

        } catch( Exception ex ){
            log.error("[KNJ_ExamremarkSql]pre_sql_ent error!" + ex );
        }
        return sql;
    }

    /**
     *  調査書就職用所見データのSQL
     */
    public String pre_sql_emp(){

        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/22Build
        String sql = null;
        try{
            sql = "SELECT "
                + "W1.SCHREGNO,"
                + "JOBHUNT_REC,"
                + "JOBHUNT_RECOMMEND,"
                + "JOBHUNT_ABSENCE,"
                + "JOBHUNT_HEALTHREMARK "
            + "FROM "
                //+ "HEXAM_EMPREMARK_DAT W1 "
                + tname3 + " W1 "
            + "WHERE "
                + "W1.SCHREGNO =? ";

        } catch( Exception ex ){
            log.error("[KNJ_ExamremarkSql]pre_sql_emp error!" + ex );
        }
        return sql;
    }


    /**
     *  調査書就職用所見データのSQL
     */
    public String pre_sql_empDatNew(){

        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/22Build
        String sql = null;
        try{
            sql = "SELECT "
                + "W1.YEAR,"
                + "W1.SCHREGNO,"
                + "JOBHUNT_REC,"
                + "JOBHUNT_ABSENCE "
            + "FROM "
                + tname3 + " W1 "
            + "WHERE "
                + "W1.SCHREGNO =? ";

        } catch( Exception ex ){
            log.error("[KNJ_ExamremarkSql]pre_sql_emp error!" + ex );
        }
        return sql;
    }

    /**
     *  調査書就職用所見データのSQL
     */
    public String pre_sql_empHdatNew(){

        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/22Build
        String sql = null;
        try{
            sql = "SELECT "
                + "W1.SCHREGNO,"
                + "JOBHUNT_REC,"
                + "JOBHUNT_RECOMMEND,"
                + "JOBHUNT_ABSENCE,"
                + "JOBHUNT_HEALTHREMARK "
            + "FROM "
                + tname4 + " W1 "
            + "WHERE "
                + "W1.SCHREGNO =? ";

        } catch( Exception ex ){
            log.error("[KNJ_ExamremarkSql]pre_sql_emp error!" + ex );
        }
        return sql;
    }

    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/22 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "HEXAM_ENTREMARK_HDAT";
        tname2 = "HEXAM_ENTREMARK_DAT";
        tname3 = "HEXAM_EMPREMARK_DAT";
        tname4 = "HEXAM_EMPREMARK_HDAT";
    }
}
