// kanji=漢字
/*
 * $Id: cfd94b6b58e983ae0a80feb180cf540e161ee077 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJI.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/*
 *  学校教育システム 賢者 [事務管理] 卒業生データ読み込み
 *
 *  2005/11/18 yamashiro 学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

public class KNJG010_2 extends KNJG010_1{
    
    private static Log log = LogFactory.getLog(KNJG010_2.class);

    public KNJG010_2(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        super(db2,svf,definecode);
        log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    /**PrepareStatement作成**/
    public void pre_stat(String ptype)
    {
        try {
        //  個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("1111001111"));
        //  学校データ
            KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("12100");  //05/11/18 Modify
            //05/11/18 Delete KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("12000");
            ps7 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
        } catch( Exception e ){
            log.error("[KNJG010_2]pre_stat error!", e);
        }
    }//pre_statの括り


}//KNJG010_2クラスの括り
