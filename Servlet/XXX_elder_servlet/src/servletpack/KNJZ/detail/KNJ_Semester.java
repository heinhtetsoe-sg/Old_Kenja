// kanji=漢字
/*
 * $Id: 20a59f404dd8b5b710ac856eecce24b4850ae627 $
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
 * 学期の取得
 */

public class KNJ_Semester{


//  ＤＢより該当学期情報を取得するメソッド
    public ReturnVal Semester(DB2UDB db2,String year,String semester){

        String name = new String();     //学期名称
        String sdate = new String();    //学期開始日
        String edate = new String();    //学期終了日

        try{
            String sql = new String();
            sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ";
            //System.out.println("[KNJ_Semester]set_sql sql=" + sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();

            if( rs.next() ){
                name = rs.getString("SEMESTERNAME");
                sdate = rs.getString("SDATE");
                edate = rs.getString("EDATE");
            }

            rs.close();
            System.out.println("[KNJ_Semester]set_sql sql ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Semester]set_sql read error!");
            System.out.println( ex );
        }

        return (new ReturnVal(name,sdate,edate,null));
    }



//  ＤＢより該当年度全学期情報を取得するメソッド
//      全学期を','を区切り文字とした文字列として編集するので、使用する際はStringtoknizerで個々の値を取り出す！
    public ReturnVal Semester_T(DB2UDB db2,String year){

        try{
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM SEMESTER_MST WHERE YEAR = '");
            sql.append(year);
            sql.append("' ORDER BY SEMESTER");
            //System.out.println("[KNJ_Semester]set_sql sql=" + sql);
            db2.query(sql.toString());
            ResultSet rs = db2.getResultSet();

            StringBuffer code = new StringBuffer(8);        //学期コード
            StringBuffer name = new StringBuffer(20);       //学期名称
            StringBuffer sdate = new StringBuffer(44);      //学期開始日
            StringBuffer edate = new StringBuffer(44);      //学期終了日
            boolean first = true;

            while( rs.next() ){
                if( !first ){
                    code.append(",");
                    name.append(",");
                    sdate.append(",");
                    edate.append(",");
                }
                if( rs.getString("SEMESTER")!=null ) code.append(rs.getString("SEMESTER"));
                if( rs.getString("SEMESTERNAME")!=null ) name.append(rs.getString("SEMESTERNAME"));
                if( rs.getString("SDATE")!=null ) sdate.append(rs.getString("SDATE"));
                if( rs.getString("EDATE")!=null ) edate.append(rs.getString("EDATE"));
                first=false;
            }
            rs.close();
            System.out.println("[KNJ_Semester_T]set_sql sql ok!");
            return (new ReturnVal(code.toString(),name.toString(),sdate.toString(),edate.toString()));
        } catch( Exception ex ){
            System.out.println("[KNJ_Semester_T]set_sql read error!");
            System.out.println( ex );
        }

        return (new ReturnVal(null,null,null,null));
    }



//  return値を返す内部クラス
    public static class ReturnVal{

        public final String val1,val2,val3,val4;

        public ReturnVal(String val1,String val2,String val3,String val4){
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
            this.val4 = val4;
        }
    }



}//クラスの括り
