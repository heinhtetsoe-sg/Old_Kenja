// kanji=漢字
/*
 * $Id: e914804449c8e2a0efa1fb5a7c8113b3a36fcdde $
 *
 * 作成日: 
 * 作成者:
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import nao_package.db.*;
import java.sql.ResultSet;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *         ＜ＫＮＪ＿Ｓｃｈｏｏｌｉｎｆｏ＿２＞     学校情報の取得 その２
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJ_Schoolinfo_2{


//  学校区分を取得するメソッド
    public ReturnVal Schooldiv(DB2UDB db2,String year){

        String schooldiv = new String();            //学校区分

        try{
            String sql = new String();
            sql = "SELECT "
                    + "SCHOOLDIV "
                + "FROM "
                    + "SCHOOL_MST "
                + "WHERE "
                    + "YEAR = '" + year + "'";

            System.out.println("[KNJ_Schoolinfo_2]Schooldiv sql=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            if( rs.next() ){
                schooldiv = rs.getString("SCHOOLDIV");
            }

            rs.close();
            System.out.println("[KNJ_Schoolinfo_2]Schooldiv sql ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Schoolinfo_2]Schooldiv read error!");
            System.out.println( ex );
        }

        return (new ReturnVal(schooldiv,null));
    }


//  卒業期を取得するメソッド
    public ReturnVal Grd_Period(DB2UDB db2,String year){

        String schooldiv = new String();            //学校区分

        try{
            String sql = new String();
            sql = "SELECT "
                    + "SCHOOLDIV "
                + "FROM "
                    + "SCHOOL_MST "
                + "WHERE "
                    + "YEAR = '" + year + "'";

            System.out.println("[KNJ_Schoolinfo_2]Schooldiv sql=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            if( rs.next() ){
                schooldiv = rs.getString("SCHOOLDIV");
            }

            rs.close();
            System.out.println("[KNJ_Schoolinfo_2]Grd_Period sql ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Schoolinfo_2]Grd_Period read error!");
            System.out.println( ex );
        }

        return (new ReturnVal(schooldiv,null));
    }


//  return値を返す内部クラス
    public static class ReturnVal{

        public final String val1,val2;

        public ReturnVal(String val1,String val2){
            this.val1 = val1;
            this.val2 = val2;
        }
    }



}//クラスの括り
