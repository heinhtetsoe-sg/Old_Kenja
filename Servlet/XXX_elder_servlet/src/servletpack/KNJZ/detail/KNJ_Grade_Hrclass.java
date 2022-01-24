// kanji=漢字
/*
 * $Id: ea6c6ad6b5b80205ca2f7564150c380e02aa1278 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;

/**
 * 学年、組の取得
 */

public class KNJ_Grade_Hrclass{


    /** 印刷指示画面より受け取った学年・組の文字列を分解するメソッド **/
    public ReturnVal Grade_Hrclass(String strx){

        String grade = strx.substring(0,2); //学年
        String hrclass = strx.substring(2); //組

        return (new ReturnVal(grade,hrclass,null,null,null));
    }



    /** ＤＢより組名称を取得するメソッド **/
    public ReturnVal hrclass_name(DB2UDB db2,String year,String semester,String grade,String hr_class){

        String name = new String();     //組名称
        String abbv = new String();     //組略称

        try{
            String sql = new String();
            sql = "SELECT "
                    + "HR_NAME,"
                    + "HR_NAMEABBV "
                + "FROM "
                    + "SCHREG_REGD_HDAT W1 "
                + "WHERE "
                        + "YEAR = '" + year + "' "
                    + "AND GRADE || HR_CLASS = '" + grade + hr_class + "' ";
            if( !semester.equals("9") ) sql = sql               //学期指定の場合
                    + "AND SEMESTER = '" + semester + "'";
            else                        sql = sql               //学年指定の場合
                    + "AND SEMESTER = (SELECT "
                                        + "MAX(SEMESTER) "
                                    + "FROM "
                                        + "SCHREG_REGD_HDAT W2 "
                                    + "WHERE "
                                            + "W2.YEAR = W1.YEAR "
                                        + "AND W2.GRADE || W2.HR_CLASS = W1.GRADE || W1.HR_CLASS)";
            //System.out.println("[KNJ_Grade_Hrclass]set_sql sql=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            if( rs.next() ){
                name = rs.getString("HR_NAME");
                abbv = rs.getString("HR_NAMEABBV");
            }

            rs.close();
            System.out.println("[KNJ_Grade_Hrclass]hrclass_name ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Grade_Hrclass]hrclass_name error!");
            System.out.println( ex );
        }

        return (new ReturnVal(name,abbv,null,null,null));
    }



    /** ＤＢより担任名を取得するメソッド **/
    public ReturnVal Staff_name(DB2UDB db2,String year,String semester,String grade,String hr_class){

        String name = new String();         //氏名
        String name_show = new String();    //表示氏名
        String name_kana = new String();    //カナ氏名
        String name_eng = new String();     //英語氏名

        try{
            String sql = new String();
            sql = "SELECT "
                    + "STAFFNAME,"
                    + "STAFFNAME_SHOW,"
                    + "STAFFNAME_KANA,"
                    + "STAFFNAME_ENG "
                + "FROM "
                    + "STAFF_MST W1,"
                    + "SCHREG_REGD_HDAT W2 "
                + "WHERE "
                        + "W2.YEAR='" + year + "' "
                    + "AND W1.STAFFCD=W2.TR_CD1"
                    + "AND GRADE || HR_CLASS = '" + grade + hr_class + "' ";
            if( !semester.equals("9") ) sql = sql               //学期指定の場合
                    + "AND SEMESTER = '" + semester + "'";
            else                        sql = sql               //学年指定の場合
                    + "AND SEMESTER = (SELECT "
                                        + "MAX(SEMESTER) "
                                    + "FROM "
                                        + "SCHREG_REGD_HDAT W3 "
                                    + "WHERE "
                                            + "W2.YEAR = W3.YEAR "
                                        + "AND W2.GRADE || W2.HR_CLASS = W3.GRADE || W3.HR_CLASS)";

            //System.out.println("[KNJ_Staff]set_sql sql=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            if( rs.next() ){
                name = rs.getString("STAFFNAME");
                name_show = rs.getString("STAFFNAME_SHOW");
                name_kana = rs.getString("STAFFNAME_KANA");
                name_eng = rs.getString("STAFFNAME_ENG");
            }

            rs.close();
            System.out.println("[KNJ_Grade_Hrclass]Staff_name ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Grade_Hrclass]Staff_name error!");
            System.out.println( ex );
        }

        return (new ReturnVal(name,name_show,name_kana,name_eng,null));
    }



    /** ＤＢより組名称及び担任名を取得するメソッド **/
    public ReturnVal Hrclass_Staff(DB2UDB db2,String year,String semester,String grade,String hr_class){

        String hrclass_name = new String();     //組名称
        String hrclass_abbv = new String();     //組略称
        String staff_name = new String();       //担任名
        String classweeks = new String();       //授業週数
        String classdays = new String();        //授業日数
        try{
            String sql = new String();
            sql = "SELECT "
                    + "HR_NAME,"
                    + "HR_NAMEABBV,"
                    + "STAFFNAME,"
                    + "CLASSWEEKS,"
                    + "CLASSDAYS "
                + "FROM "
                    + "SCHREG_REGD_HDAT W2 "
                    + "LEFT JOIN STAFF_MST W1 ON W1.STAFFCD=W2.TR_CD1 "
                + "WHERE "
                        + "YEAR = '" + year + "' "
                    + "AND GRADE || HR_CLASS = '" + grade + hr_class + "' ";
            if( !semester.equals("9") ) sql = sql               //学期指定の場合
                    + "AND SEMESTER = '" + semester + "'";
            else                        sql = sql               //学年指定の場合
                    + "AND SEMESTER = (SELECT "
                                        + "MAX(SEMESTER) "
                                    + "FROM "
                                        + "SCHREG_REGD_HDAT W3 "
                                    + "WHERE "
                                            + "W2.YEAR = W3.YEAR "
                                        + "AND W2.GRADE || W2.HR_CLASS = W3.GRADE || W3.HR_CLASS)";

            //System.out.println("[KNJ_Staff]set_sql sql=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            if( rs.next() ){
                hrclass_name = rs.getString("HR_NAME");
                hrclass_abbv = rs.getString("HR_NAMEABBV");
                staff_name = rs.getString("STAFFNAME");
                classweeks = rs.getString("CLASSWEEKS");
                classdays = rs.getString("CLASSDAYS");
            }

            rs.close();
            System.out.println("[KNJ_Grade_Hrclass]Hrclass_Staff ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Grade_Hrclass]Hrclass_Staff error!");
            System.out.println( ex );
        }

        return (new ReturnVal(hrclass_name,hrclass_abbv,staff_name,classweeks,classdays));
    }



    /** 該当生徒の年次別クラス、担任名、校長名を取得するためのStatementを作成するメソッド **/
    public String GradeRecsql(String year,String semester){

        String sql = new String();
        try{
            sql = "SELECT "
                    + "T1.SCHREGNO,"
                    + "T1.ANNUAL,"
                    + "T2.HR_NAME,"
                    + "T1.ATTENDNO,"
                    + "T3.STAFFNAME,"
                    + "T4.PRINCIPAL_NAME "
                + "FROM "
                    + "SCHREG_REGD_DAT T1 "
                    + "INNER JOIN SCHREG_REGD_HDAT T2 ON T1.YEAR=T2.YEAR "
                                                    + "AND T1.SEMESTER=T2.SEMESTER "
                                                    + "AND T1.GRADE=T2.GRADE "
                                                    + "AND T1.HR_CLASS=T2.HR_CLASS "
                    + "LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T2.TR_CD1 "
                    + "LEFT JOIN("
                        + "SELECT "
                            + "YEAR,"
                            + "STAFFNAME AS PRINCIPAL_NAME,"
                            + "JOBNAME AS PRINCIPAL_JOBNAME "
                        + "FROM "
                            + "STAFF_YDAT ST1 "
                            + "INNER JOIN STAFF_MST ST2 ON ST2.JOBCD = '0001' "
                                                    + "AND ST2.STAFFCD = ST1.STAFFCD "
                            + "LEFT JOIN JOB_MST ST3 ON ST3.JOBCD = ST2.JOBCD "
                        + "WHERE "
                            + "ST1.YEAR <= '" + year + "' "
                    + ")T4 ON T4.YEAR = T1.YEAR "
                + "WHERE "
                        + "T1.SCHREGNO =? "
                    + "AND T1.YEAR <= '" + year + "' "
                    + "AND T1.YEAR || T1.SEMESTER IN("
                            + "SELECT MAX(YEAR || SEMESTER)"
                            + "FROM "
                                + "SCHREG_REGD_DAT "
                            + "WHERE "
                                + " SCHREGNO =? "
                            + "GROUP BY "
                                + "ANNUAL)"
                + "ORDER BY "
                    + "T1.ANNUAL";

            System.out.println("[KNJ_Grade_Hrclass]GradeRecsql ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Grade_Hrclass]GradeRecsql error!");
            System.out.println( ex );
        }
        return sql;
    }//GradeRecsqlの括り



    /** <<< return値を返す内部クラス >>> **/
    public static class ReturnVal{

        public final String val1,val2,val3,val4,val5;

        public ReturnVal(String val1,String val2,String val3,String val4,String val5){
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
            this.val4 = val4;
            this.val5 = val5;
        }
    }



}//クラスの括り
