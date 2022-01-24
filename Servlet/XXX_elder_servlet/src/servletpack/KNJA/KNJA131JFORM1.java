// kanji=漢字
/*
 * $Id: 54022455097fe077218913dd0df30ad79956d141 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.Map;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中学校用（千代田区立九段中等教育学校版を基に作成）
 *                                                    様式1（学生に関する記録）
 *
 *  2006/03/18 Build yamashiro
 */

public class KNJA131JFORM1 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131JFORM1.class);
    private StringBuffer stb;
    private ResultSet rs;
    private KNJA131FORM1 knja131obj = new KNJA131FORM1();  //中等教育学校用オブジェクト


    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            knja131obj.printSvfDetail_1( db2, svf, paramap );  //学校情報
            knja131obj.printSvfDetail_2( db2, svf, paramap );  //個人情報
            knja131obj.printSvfDetail_3( db2, svf, paramap );  //住所履歴情報
            knja131obj.printSvfDetail_4( db2, svf, paramap );  //異動履歴情報
            knja131obj.printSvfDetail_5( db2, svf, paramap );  //年次・ホームルーム・整理番号
            ret = svf.VrEndPage();
            nonedata = true;
        } catch( Exception ex ){
            log.debug( "printSvf error!", ex );
        }
        return nonedata;
    }


    /**
     *  SVF-FORM設定
     */
    void setSvfForm( Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetForm("KNJA131_6.frm", 1);
            ret = svf.VrsOut("TITLE",  "中学校指導要録" );   //項目名
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  PrepareStatementオブジェクト作成
     */
    void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            knja131obj.prepareSqlState( db2, paramap, definecode );
        } catch( Exception ex ){
            log.debug( "prepareSqlState error!", ex );
        }
    }


    /**
     *  SQL オブジェクトをクローズ
     */
    void closePrepareState( DB2UDB db2, Map paramap )
    {
        try {
            knja131obj.closePrepareState( db2, paramap );
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }


}
