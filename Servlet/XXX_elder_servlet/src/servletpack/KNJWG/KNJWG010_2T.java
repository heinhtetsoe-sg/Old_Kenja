// kanji=漢字
/*
 * $Id: 1ca5e0cdad14093aea53fd05ae50cb65df097009 $
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

import servletpack.KNJWG.detail.KNJ_SchoolinfoSql;
import servletpack.KNJI.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJDefineSchool;

/*
 *
 *  学校教育システム 賢者 [事務管理]
 *
 *                ＜ＫＮＪＧ０１０＿２＞  卒業生データ読み込み
 *
 *  2005/10/22 m-yama /KNJZ/KNJ_Schoolinfoを/KNJWG/KNJ_SchoolinfoSqlへ変更 NO001
 */

public class KNJWG010_2T extends KNJWG010_1T{
    private static final Log log = LogFactory.getLog(KNJWG010_2T.class);

    public KNJWG010_2T(){
        super();
    }

    public KNJWG010_2T(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        super(db2,svf,definecode);
    }

    /**
     * PrepareStatement作成
     */
    public void pre_stat(final String ptype) {
        try {
            // 個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("11110011"));
            // 学校データ
            String str = ("HIRO".equals(_definecode.schoolmark))? "12100": "12000";
            KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql(str);
            ps7 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

}
