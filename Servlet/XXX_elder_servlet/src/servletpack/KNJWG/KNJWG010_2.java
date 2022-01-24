// kanji=漢字
/*
 * $Id: f2541ffee9d58216b9625c6abd7044ea2ea5537b $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJI.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/*
 *  学校教育システム 賢者 [事務管理] 卒業生データ読み込み
 *
 *  2005/11/18 yamashiro 学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

public class KNJWG010_2 extends KNJWG010_1{
    int ret;                            //ＳＶＦ応答値


    public KNJWG010_2(){
        super();

    }


    public KNJWG010_2(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        super(db2,svf,definecode);
    }



    /**PrepareStatement作成**/
    public void pre_stat(String ptype)
    {
        try {
        //  個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("11110011"));
        //  学校データ
            KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("12100");  //05/11/18 Modify
            //05/11/18 Delete KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("12000");
            ps7 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
        } catch( Exception e ){
            System.out.println("[KNJWG010_2]pre_stat error!");
            System.out.println( e );
        }
    }//pre_statの括り


}//KNJWG010_2クラスの括り
