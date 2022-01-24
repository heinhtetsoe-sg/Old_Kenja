// kanji=漢字
/*
 * $Id: f362e073a4e79f66cf904ca62e75ce34e6b7847b $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中学校用（千代田区立九段中等教育学校版を基に作成）
 *
 *  2006/03/18 Build yamashiro
 */

public class KNJA131J
{
    private static final Log log = LogFactory.getLog(KNJA131J.class);
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

    /**
     *
     *  KNJA.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        Map paramap = new HashMap();        //HttpServletRequestの引数
        boolean nonedata = false;

    // パラメータの取得
        getParam( request, paramap );
    // print svf設定
        sd.setSvfInit( request, response, svf);
    // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error! ");
            return;
        }
    // 印刷処理
        nonedata = printSvf( request, db2, svf, paramap );
    // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb(db2);
    }


    /** 
     *  get parameter doGet()パラメータ受け取り 
     */
    private void getParam( HttpServletRequest request, Map paramap )
    {
        try {
            paramap.put( "YEAR",   request.getParameter("YEAR")  );  //年度
            paramap.put( "GAKKI",  request.getParameter("GAKKI") );  //学期
            paramap.put( "GRADE_HR_CLASS", request.getParameter("GRADE_HR_CLASS"));  //学年・組
            if( request.getParameter("simei") != null )paramap.put( "KANJI_OUT",  request.getParameter("simei") );  //漢字名出力
            if( request.getParameter("inei")  != null )paramap.put( "INNEI_OUT",  request.getParameter("inei")  );  //陰影出力
            paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));
            
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        }
    }


    /**
     *  印刷処理
     */
    private boolean printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        KNJDefineCode definecode = new KNJDefineCode();  // 各学校における定数等設定
        definecode.setSchoolCode(db2,(String)paramap.get("YEAR"));
        boolean nonedata = false;
        List knjobj = new ArrayList(4); //帳票作成クラスを格納
        String schno[] = request.getParameterValues("category_selected");   //学籍番号
        try {
            if( request.getParameter("seito")    != null )knjobj.add( new KNJA131JFORM1() ); //様式１（学籍に関する記録）
            if( request.getParameter("gakushu1") != null )knjobj.add( new KNJA131JFORM3() ); //様式２（指導に関する記録）前期課程
            if( request.getParameter("katsudo")  != null )knjobj.add( new KNJA131JFORM5() ); //様式３
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        try {
            for( int i = 0; i < schno.length; i++ ){
                for( int j = 0; j < knjobj.size(); j++ ){
                    ( ( KNJA131BASE )knjobj.get(j) ).prepareSqlState( db2, paramap, definecode );  //PrepareStatementオブジェクト作成
                    ( ( KNJA131BASE )knjobj.get(j) ).setSvfForm( svf, paramap );  //SVF-FORM設定
                    removeParamap( paramap );
                    paramap.put( "SCHNO",  new String( schno[i] ) );
                    if( ( ( KNJA131BASE )knjobj.get(j) ).printSvf( db2, svf, paramap ) )nonedata = true;
                    if( i == schno.length - 1 )( ( KNJA131BASE )knjobj.get(j) ).closePrepareState( db2, paramap );
                }
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /**
     *  引数用マップより不要マップを削除
     */
    private void removeParamap( Map paramap )
    {
        try {
            if( paramap.containsKey("SCHNO") )paramap.remove("SCHNO");
            if( paramap.containsKey("SCHNAME") )paramap.remove("SCHNAME");
            /*
            for( int i = 1; i <= 6; i++ ){
                if( paramap.containsKey("HRCLASS_" + i ) )paramap.remove("HRCLASS_" + i );
                if( paramap.containsKey("ATTENDNO_" + i ) )paramap.remove("ATTENDNO_" + i );
            }
            */
            /*
            Set m = paramap.keySet( );
            String s = null;
            for (Iterator i = m.iterator( ); i.hasNext( );){
                s = (String)i.next();
                if( s.equals("SCHNO") || s.equals("SCHNAME") )i.remove();
            }
            */
        } catch( Exception ex ){
            log.error( "error! ", ex );
        }
    }


}
