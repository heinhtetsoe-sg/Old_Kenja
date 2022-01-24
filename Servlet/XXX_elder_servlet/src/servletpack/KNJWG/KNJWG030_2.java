// kanji=漢字
/*
 * $Id: a549f7a61768b0ded7106ac5baca8f9a44affc6c $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJI.detail.KNJ_PersonalinfoSql;
import servletpack.KNJI.detail.KNJ_StudyrecSql_Grd;
//import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/*
 *  学校教育システム 賢者 [事務管理]  単位習得証明書
 *
 *  2005/07/22 yamashiro 出力項目を追加（生年月日・課程・学科・入学・卒業・出欠）
 *  2005/07/25 yamashiro KNJ_StudyrecSqlをKNJ_StudyrecSql_Grdへ変更
 *  2005/11/18 yamashiro 学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

public class KNJWG030_2 extends KNJWG030_1{

    private static final Log log = LogFactory.getLog(KNJWG030_2.class);

    public KNJWG030_2(DB2UDB db2,Vrw32alp svf) throws SQLException{
        super(db2,svf);
    }


    /**
     *  PrepareStatement作成
     */
    public void pre_stat(String hyotei)
    {
log.debug("GRADUATION DATA");
        try {
        //  学習記録データ
            KNJ_StudyrecSql_Grd obj_StudyrecSql = new KNJ_StudyrecSql_Grd("hyde", "hyde", 1, false, _isHosei);              //05/07/25Modify
            ps1 = db2.prepareStatement(obj_StudyrecSql.pre_sql());
        //  個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            //ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("0000001000"));
            ps6 = db2.prepareStatement( obj_Personalinfo.sql_info_reg("1111001000") );    //05/07/22Modify
        //  学校データ
            //KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("10100");  //05/11/18 Modify
            //05/11/18 Delete KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("12000");
            //ps7 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
            servletpack.KNJWG.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJWG.detail.KNJ_SchoolinfoSql("10000");
            ps7 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
        } catch( Exception e ){
            log.error("pre_stat error! " + e );
        }
    }

}
