// kanji=漢字
/*
 * $Id: 8f015dfc26c7764472298865bbb209b7dab40709 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA.detail;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;

/**
 *
 *    ＜ＫＮＪ＿ＧｒａｄｅＲｅｃＳｑｌ＞    学籍履歴等取得
 *
 *  2004/03/16 yamashiro・メソッドmax_gradeにおいて、最高学年が取得されない不具合を修正
 *  2004/03/19 yamashiro・メソッドmax_gradeにおいて、生徒個々の最高学年が取得されない不具合を修正
 *  2006/03/18 yamashiro・校長および担任のコードを追加（指導要録の印鑑対応）--NO001
 */

public class KNJ_GradeRecSql{


/*-----------------------------------------------------------------*
 *  学籍等履歴取得ＳＱＬを返す
 *
 *  priparestatement パラメーターの設定(setXXX)
 *      1.学籍番号 2.学籍番号 3.年度 4.年度
 *-----------------------------------------------------------------*/
    public String sql_state(String useSchregRegdHdat){

        String sql = null;
        try{
        sql = "SELECT "
                + "T1.YEAR,"
                + "T1.GRADE,"
                + "T1.HR_CLASS,"
                + "T1.ATTENDNO,"
            //  + "T7.ANNUAL,"
                + "T1.ANNUAL,"
                + "T3.HR_NAME,";
                if ("1".equals(useSchregRegdHdat)) {
                   sql += "T3.HR_CLASS_NAME1,";
                }
        sql +=    "T2.SCHOOLDIV,"
                + "T4.STAFFNAME,"
                + "S2.STAFFNAME as STAFFNAME2,"
                + "CASE WHEN T6.PRINCIPAL_NAME IS NOT NULL "
                + "     THEN T6.PRINCIPAL_NAME "
                + "     ELSE T7.STAFFNAME "
                + "END AS PRINCIPALNAME "
                + ",T4.STAFFCD "  //NO001
                + ",T5.STAFFCD AS PRINCIPALSTAFFCD "  //NO001
            + "FROM "
                + "("
                    + "SELECT "
                        + "'0' AS SCHOOLDIV,"
                        + "SCHREGNO,"
                        + "YEAR,"
                        + "SEMESTER,"
                        + "GRADE,"
                        + "HR_CLASS,"
                        + "ANNUAL,"
                        + "ATTENDNO "
                    + "FROM "
                        + "V_REGDYEAR_GRADE_DAT "
                    + "WHERE "
                        + "SCHREGNO=? "
                    + "UNION SELECT "
                        + "'1' AS SCHOOLDIV,"
                        + "SCHREGNO,"
                        + "YEAR,"
                        + "SEMESTER,"
                        + "GRADE,"
                        + "HR_CLASS,"
                        + "ANNUAL,"
                        + "ATTENDNO "
                    + "FROM "
                        + "V_REGDYEAR_UNIT_DAT "
                    + "WHERE "
                        + "SCHREGNO=? "
                + ")T1 "
            //  + "INNER JOIN SCHREG_REGD_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO "
            //                                  + "AND T7.YEAR = T1.YEAR "
            //                                  + "AND T7.SEMESTER = T1.SEMESTER "
                + "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR AND T2.SCHOOLDIV=T1.SCHOOLDIV "
                + "INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR "
                                                    + "AND T3.SEMESTER=T1.SEMESTER "
                                                    + "AND T3.GRADE=T1.GRADE "
                                                    + "AND T3.HR_CLASS=T1.HR_CLASS "
                + "LEFT JOIN STAFF_MST T4 ON T4.STAFFCD=T3.TR_CD1 "
                + "LEFT JOIN STAFF_MST S2 ON S2.STAFFCD=T3.TR_CD2 "
                + "LEFT JOIN("
                    + "SELECT "
                        + "YEAR,"
                        + "MAX(STAFFCD) AS STAFFCD "
                    + "FROM "
                        + "V_STAFF_MST "
                    + "WHERE "
                            + "YEAR<=? "
                        + "AND JOBCD='0001' "
                    + "GROUP BY "
                        + "YEAR "
                + ")T5 ON T5.YEAR=T2.YEAR "
                + "LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T5.STAFFCD "
                + "LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR "
                + "     AND T6.CERTIF_KINDCD = '108' "
            + "WHERE "
                + "T1.YEAR<=? "
            + "ORDER BY "
                + "T1.GRADE,T1.YEAR";

            } catch( Exception ex ){
                System.out.println("[KNJ_GradeRecSql]sql_state error!");
                System.out.println( ex );
            }
    
            return sql;


    }//public String sql_stateの括り

/*-----------------------------------------------------------------*
 *  学籍在籍データを検索し、最高年次を返す 2004/03/19修正
 *-----------------------------------------------------------------*/
    public static int max_grade(DB2UDB db2,String year,String schregno){

        int grade = 0;
        try{
            String sql = new String();
            sql = "SELECT "
                    + "MAX(CASE WHEN W1.ANNUAL IS NULL THEN W2.ANNUAL ELSE W1.ANNUAL END) "
                + "FROM(SELECT SCHREGNO,ANNUAL FROM SCHREG_REGD_DAT W1 WHERE W1.SCHREGNO='"+schregno+"' AND W1.YEAR<='"+year+"')W1 "
                    + "FULL JOIN(SELECT SCHREGNO,ANNUAL FROM GRD_REGD_DAT W1 WHERE W1.SCHREGNO='"+schregno+"' AND W1.YEAR<='"+year+"')W2 "
                        + "ON W2.SCHREGNO=W1.SCHREGNO";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if( rs.next() ){
                grade = Integer.parseInt(rs.getString(1));
            }
            rs.close();
        } catch( Exception ex ){
            System.out.println("[KNJ_GradeRecSql]max_grade error!");
            System.out.println( ex );
            return 0;
        }
        return grade;
    }



/*-----------------------------------------------------------------*
 *  卒業学籍基礎データを検索し、存在すればTRUEを以外はFALSEを返す
 *-----------------------------------------------------------------*/
    public static boolean GrdDiv(DB2UDB db2,String schregno){

        boolean grddiv = false;     //卒業生:true
        String sql = new String();
        sql = "SELECT "
                + "SCHREGNO "
            + "FROM "
                + "GRD_BASE_MST "
            + "WHERE "
                + "SCHREGNO = '" + schregno + "'";
        try {
            System.out.println("[KNJZ_GradeRecSql]GrdDiv sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if( rs.next() ) grddiv = true;
            rs.close();
            System.out.println("[KNJ_GradeRecSql]GrdDiv read ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_GradeRecSql]GrdDiv read error!");
            System.out.println( ex );
        }

        return grddiv;

    }//public static boolean GrdDiv()の括り



}//クラスの括り
