// kanji=漢字
/*
 * $Id: 218a068163e944d35e894fd4f90d9fec90eb311a $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;

/**
 * 学校情報の取得
 *
 *  使い方の例 KNJE070)
 *      KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(year1,year2,staffcd,"12");
 *      KNJ_Schoolinfo.ReturnVal returnval = schoolinfo.get_info(db2);
 *      ret=svf.VrsOut("school_name"    ,returnval.SCHOOL_NAME2);
 */

public class KNJ_Schoolinfo{

    String year;        //当年度
    String year2;       //該当年度(過卒者に対応)
    String serchcd;     //担当者検索におけるJOBCD OR STAFFCD
    String t_switch;    //検索区分
                        //１番目:校長名及び職務名取得   0->検索無 1->検索有
                        //２番目:担当者名及び職務名取得 0->検索無 1->JOBCDで検索 2->STAFFCDで検索



    public KNJ_Schoolinfo(String year){                                                     //s
        this.t_switch = "10000";
        this.year = year;
        this.year2 = year;
    }

    public KNJ_Schoolinfo(String year,String serchcd,String t_switch){                      //s,s,s
        this.t_switch = t_switch + "00000";
        this.year = year;
        this.year2 = year;
        this.serchcd = serchcd;
    }

    public KNJ_Schoolinfo(String year,String year2,String serchcd,String t_switch){         //s,s,s,s
        this.t_switch = t_switch + "00000";
        this.year = year;
        this.year2 = year2;
        this.serchcd = serchcd;
    }



//  学校情報を取得するメソッド
    public ReturnVal get_info(DB2UDB db2){

        PreparedStatement ps1;
        String SCHOOL_NAME1 = new String();
        String SCHOOL_NAME2 = new String();
        String SCHOOL_NAME3 = new String();
        String SCHOOL_ZIPCD = new String();
        String SCHOOL_ADDRESS1 = new String();
        String SCHOOL_ADDRESS2 = new String();
        String PRINCIPAL_NAME = new String();
        String PRINCIPAL_JOBNAME = new String();
        String STAFF2_NAME = new String();
        String STAFF2_JOBNAME = new String();
        String FOUNDEDYEAR = new String();
        String SEMESTERDIV = new String();


        try {
            KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql(this.t_switch);
            ps1 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
//System.out.println("[KNJ_Schoolinfo]get_info ps1="+String.valueOf(ps1));

            ps1.setString(1,this.year);
            ps1.setString(2,this.year2);
            if( this.t_switch.substring(1,2).equals("1") ){ //JOBCDで担当者を検索
                ps1.setString(3,this.year2);
                ps1.setString(4,this.serchcd);
            }
            if( this.t_switch.substring(1,2).equals("2") ){ //STAFFCDで担当者を検索
                ps1.setString(3,this.serchcd);
            }
            ResultSet rs = ps1.executeQuery();
//System.out.println("[KNJ_Schoolinfo]get_info ps1 ok!");

            if( rs.next() ){
                SCHOOL_NAME1        = rs.getString("SCHOOLNAME1");
                SCHOOL_NAME2        = rs.getString("SCHOOLNAME2");
                SCHOOL_NAME3        = rs.getString("SCHOOLNAME3");
                SCHOOL_ZIPCD        = rs.getString("SCHOOLZIPCD");
                SCHOOL_ADDRESS1     = rs.getString("SCHOOLADDR1");
                SCHOOL_ADDRESS2     = rs.getString("SCHOOLADDR2");
                FOUNDEDYEAR         = rs.getString("FOUNDEDYEAR");                  //創立年度
                SEMESTERDIV         = rs.getString("SEMESTERDIV");                  //学期制

                if( !t_switch.substring(0,1).equals("0") ){
                    PRINCIPAL_NAME      = rs.getString("PRINCIPAL_NAME");
                    PRINCIPAL_JOBNAME   = rs.getString("PRINCIPAL_JOBNAME");
                }

                if( !t_switch.substring(1,2).equals("0") ){
                    STAFF2_NAME         = rs.getString("STAFF2_NAME");
                    STAFF2_JOBNAME      = rs.getString("STAFF2_JOBNAME");
                }
            }
            ps1.close();
            //db2.commit();
            System.out.println("[KNJ_Schoolinfo]get_info ok!");
        } catch( Exception e ){
            System.out.println("[KNJ_Schoolinfo]get_info error!!");
            System.out.println( e );
        }

        if( SCHOOL_ADDRESS1==null )     SCHOOL_ADDRESS1="";
        if( SCHOOL_ADDRESS2==null )     SCHOOL_ADDRESS2="";
        if( PRINCIPAL_NAME==null )      PRINCIPAL_NAME="";
        if( PRINCIPAL_JOBNAME==null )   PRINCIPAL_JOBNAME="";
        if( STAFF2_NAME==null )         STAFF2_NAME="";
        if( STAFF2_JOBNAME==null )      STAFF2_JOBNAME="";

        return (new ReturnVal(SCHOOL_NAME1,SCHOOL_NAME2,SCHOOL_NAME3,SCHOOL_ZIPCD,SCHOOL_ADDRESS1,
                                SCHOOL_ADDRESS2,PRINCIPAL_NAME,PRINCIPAL_JOBNAME,STAFF2_NAME,STAFF2_JOBNAME,
                                FOUNDEDYEAR,SEMESTERDIV));

    }//get_infoの括り



//  return値を返す内部クラス
    public static class ReturnVal{

        public final String SCHOOL_NAME1,SCHOOL_NAME2,SCHOOL_NAME3,SCHOOL_ZIPCD,SCHOOL_ADDRESS1,
                                SCHOOL_ADDRESS2,PRINCIPAL_NAME,PRINCIPAL_JOBNAME,STAFF2_NAME,STAFF2_JOBNAME,
                                FOUNDEDYEAR,SEMESTERDIV;

        public ReturnVal(String val1,String val2,String val3,String val4,String val5,
                            String val6 ,String val7,String val8,String val9,String val10,
                            String val11 ,String val12){
            SCHOOL_NAME1        = val1;
            SCHOOL_NAME2        = val2;
            SCHOOL_NAME3        = val3;
            SCHOOL_ZIPCD        = val4;
            SCHOOL_ADDRESS1     = val5;
            SCHOOL_ADDRESS2     = val6;
            PRINCIPAL_NAME      = val7;
            PRINCIPAL_JOBNAME   = val8;
            STAFF2_NAME         = val9;
            STAFF2_JOBNAME      = val10;
            FOUNDEDYEAR         = val11;
            SEMESTERDIV         = val12;
        }
    }



}//クラスの括り
