// kanji=漢字
/*
 * $Id: 2cc30eaa6ceecddd2896fd824e75e613c79d5ed2 $
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
 *                                                    各教科・科目の学習の記録（前期課程）
 *
 *  2006/03/18 Build yamashiro
 */

public class KNJA131JFORM3 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131JFORM3.class);

    private StringBuffer stb;
    private ResultSet rs;
    private KNJA131FORM3 knja131obj = new KNJA131FORM3();  //中等教育学校用オブジェクト

    int totalcredit;                //総合修得単位数


    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            setSvfForm( svf, paramap );
            knja131obj.printSvfDetail_1( db2, svf, paramap );  //観点・評定
            knja131obj.printHeader( db2, svf, paramap );  //年次・ホームルーム・整理番号
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
    void setSvfForm( Vrw32alp svf, Map paramp )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetForm("KNJA131_7.frm", 1);
            ret = svf.VrsOut("SUBTITLE",  "各教科の学習の記録" );   //項目名
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
