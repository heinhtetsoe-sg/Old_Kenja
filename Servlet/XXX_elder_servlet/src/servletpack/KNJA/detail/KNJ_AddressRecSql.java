// kanji=漢字
/*
 * $Id: da7645a24c3bb46f7b4de48d866896dab8beec54 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA.detail;

/**
 *
 *    ＜ＫＮＪ＿ＡｄｄｒｅｓｓｒｅｃＳｑｌ＞ 生徒住所履歴取得のＳＱＬ
 *
 *  パラメーターの設定(setXXX)
 *      1.学籍番号 2.年度 3.学籍番号 4.年度
 */

public class KNJ_AddressRecSql{

    public String sql_state(){

        String sql = new String();
        try{
            sql = "SELECT "
                    + "T1.ISSUEDATE,"
                    + "T1.ADDR1,"
                    + "T1.ADDR2,"
                    + "T1.ADDR_FLG,"
                    + "T1.ZIPCD,"
                    + "T2.COUNT,"
                    + "T1.SCHREGNO "
                + "FROM "
                    + "SCHREG_ADDRESS_DAT T1 "
                    + "LEFT JOIN("
                        + "SELECT "
                            + "SCHREGNO,"
                            + "COUNT(SCHREGNO) AS COUNT "
                        + "FROM "
                            + "SCHREG_ADDRESS_DAT T1 "
                        + "WHERE "
                                + "SCHREGNO =? "
                            + "AND FISCALYEAR(ISSUEDATE) <=? "
                        + "GROUP BY "
                            + "SCHREGNO "
                    + ")T2 ON T2.SCHREGNO = T1.SCHREGNO "
                
                + "WHERE "
                        + "T1.SCHREGNO =? "
                    + "AND FISCALYEAR(ISSUEDATE) <=? "
                + "ORDER BY "
                    + "ISSUEDATE DESC";
    
            } catch( Exception ex ){
                System.out.println("[KNJ_AddressRecSql]sql_state error!");
                System.out.println( ex );
            }
    
            return sql;


    }//public String sql_stateの括り


}//クラスの括り
