// kanji=漢字
/*
 * $Id: 79f3e1392017458207f9a86c63d34ab4e9518a37 $
 *
 * 作成日: 2005/08/27 11:25:40 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 *
 *  学校教育システム 賢者 [入試管理] 合否人数
 *
 *  2005/08/27 Build
 *  2005/11/10 yamashiro 「大阪府」は出身学校コードの3・4桁目を見て( '55'以下 )カウント
 *                       受験者数の下側に全合格者数を表示
 *                       行がダブって出力される不具合を修正
 *                       判定区分A・Bの上側に出力する専願・併願の数値に判定区分Cを含むように（含まれていなかったので）修正
 *                       ０をブランクとしていたので、０表記に修正
 *  2005/11/11 yamashiro 志望コースコード変更に因る変更 <= 難〜易へ昇順
 *  2005/11/15 yamashiro 志望コースコード変更に因る変更 <= 05/11/11から更に変更 各コース内では難〜易へ降順
 *                       Ａ・Ｂ判定を志願者事前相談データの判定ではなく、基礎データの正規合格フラグをみる
 *  2005/12/30 yamasihro 
 *      ○合格者の抽出において「推薦を含む・含まない」の選択を追加 => judgement = '4' が推薦のコード --NO001
 *      ○合格者の抽出において  --NO002
 *          ○追加、繰上げ合格 (JUDGEMENT = ('5','6')) を含まない
 *          ○クラブ推薦 (APPLICANTDIV == '3')を含まない、但し正規合格 (REGULARSUCCESS_FLG IN('1','2','3')) のクラブ推薦者は含む
 *          ○付属推薦　(APPLICANTDIV == '1') は含まない
 *      ○スライド合格は、第一志望コースでまとめる --NO003
 *          ○特進コース合格（特）　　：特進第一合格者の人数
 *                          （理->特）：理数第一希望者で理数不合格者だが特進第二合格者の人数（理->特）＋（理->特->進）
 *                          （国->特）：国際第一希望者で国際不合格者だが特進第二合格者の人数（国->特）＋（国->特->進）
 *      ○計の前で改ページしないようにする --NO004
 *  2006/01/06 yamashiro NO003の不具合を修正 --NO005
 *  2006/01/06 yamashiro 
 *      ○合格者の抽出条件を以下のとおりとする  --NO006
 *          ○追加、繰上げ合格 (JUDGEMENT = ('5','6')) を含む
 *          ○クラブ推薦 (JUDGEMENT == '4')を含まない、但し正規合格 (REGULARSUCCESS_FLG IN('1','2','3')) のクラブ推薦者は含む
 *  2006/01/07 yamashiro 
 *      ○合格者の抽出条件を以下のとおりとする  --NO007
 *          ○追加(JUDGEMENT = ('5'))は含まない、繰上げ合格 (JUDGEMENT = ('6')) は含む
 *  2006/01/08 yamashiro 
 *      ○繰上合格者の人数を表記する  --NO008
 *      ○クラブ推薦の人数を表記する  --NO009
 *      ○不合格の人数を（受験人数−合格人数−推薦人数(クラブ推薦正規合格者ではない) ）とする  --NO009
 *  2006/01/10 yamashiro 
 *      ○正規合格フラグはNULLを'0'と見做す --NO010
 *  2006/01/11 yamashiro
 *      ○集計は’繰上合格者は除く’とする  --NO011
 *        帳票のサブタイトルは’（繰上合格者xx名除く）’と表記する  --NO011
 *  2006/01/22 yamashiro
 *      ○クラブ推薦項目の’推薦’はカットする  --NO012
 *      ○不合格者数の面接は、推薦不合格者数を表記する  --NO012
 *      ○不合格の人数を（受験人数−合格人数＋(クラブ推薦人数−クラブ推薦正規合格者−面接不合格者) ）とする ???  --NO012
 *        不合格の人数を（受験人数−(合格人数＋(クラブ推薦人数−クラブ推薦正規合格者−面接不合格者)) ）が正しいのでは？
 *  2006/01/24 yamashiro
 *      ○「クラブ推薦者」の項目は、クラブ推薦合格者を出力する  --NO013
 *      ○「正規合格」の項目は「(正規合格)」と表記する  --NO014
 *      ○追加合格は反映しない => 不合格にカウントする  --NO015
 *      ○繰上合格は反映しない => 合格コースのひとつ前の志望コースでカウントする  --NO015
 *      ○「得点」は一般の人で不合格者 => 追加合格者も含む  --NO017
 *        「面接」はクラブ推薦者の中で、正規合格者ではなく、クラブ推薦合格にならなかった者  --NO017
 *  2006/01/25 yamashiro
 *      ○各集計は以下の仕様とする  --NO018
 *        ○不合格者＝受験者ー合格者
 *        ○合格者  ＝(各コース合格者合計)＋(クラブ推薦者−クラブ推薦正規合格者)  <= これまでクラブ推薦正規合格者以外を除外していた
 *        ○不合格者＝不合格者(得点)＋不合格者(面接)
 *        ○クラブ推薦者        ：出願区分が３で判定が４以下の者
 *        ○クラブ推薦正規合格者：クラブ推薦者でシミュレーションで(基準値以上)合格した者
 *        ○不合格者(得点)      ：出願区分が一般で判定が７の者
 *        ○不合格者(面接)      ：出願区分がクラブ推薦で判定が７の者 <= 01/24の修正で、クラブ推薦正規合格者以外をここに含めていた
 *      ○中高一貫コースは出力しない  --NO019
 *  2006/01/31 yamashiro
 *      ○半角の’A/B'を全角へ変更 --NO020
 *  2006/02/09 yamshiro
 *      ○学校所在地は、LOCATIONCDをみる --NO021
 *
 *
 */
public class KNJL350K
{
    private static final Log log = LogFactory.getLog(KNJL350K.class);

    private KNJEditString knjobj = new KNJEditString();
    private Map desiredivmap;   //志望区分表示用Map
    private int pagecount;
    private int linenum;   //出力行数 --NO004
    private int arrculubsuisen[] = {0,0,0,0,0,0,0,0,0};  //NO009


    /**
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        Map paramap = new HashMap();        //HttpServletRequestからの引数
        boolean nonedata = false;           //該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

    // ＤＢ接続
        db2 = sd.setDb( request );
        if( sd.openDb( db2 ) ){
            log.error("db open error");
            return;
        }

    // パラメータの取得
        getParam( db2, request, paramap );

    // print svf設定
        sd.setSvfInit( request, response, svf );

    // 印刷処理
        nonedata = printSvf( db2, svf, paramap );

    // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb( db2 );
    }


    /** 
     *  get parameter doGet()パラメータ受け取り 
     *
     *  引数の説明
     *      "YEAR",       年度                 
     *      "TESTDIV",    試験区分             1.前期  2.後期(2005/07/28現在 NAME_MST 'L003')
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap )
    {
        try {
            paramap.put( "YEAR",       request.getParameter("YEAR")      );  //年度
            paramap.put( "TESTDIV",    request.getParameter("TESTDIV")   );  //試験区分
            if( request.getParameter("OUTPUT") != null )
                paramap.put( "RECOMMENDATION",    request.getParameter("OUTPUT")   );  //推薦 1:含まない 2:含む --NO001
            else
                paramap.put( "RECOMMENDATION",    new String("0")   );  //推薦 1:含まない 2:含む --NO001
            paramap.put( "SPECIAL_REASON_DIV", request.getParameter("SPECIAL_REASON_DIV")); // 特別理由
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        } finally {
log.debug("paramap="+paramap);
        }
    }

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, Map paramap) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                svf.VrsOut("VIRUS", name2);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
        }
    }

    /**
     *  印刷処理
     *
     */
    private boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        PreparedStatement arrps[] = new PreparedStatement[3];

        try {
            setSvfformFormat( db2, svf, paramap );                       //SVF-FORM設定

            //繰上合格人数印刷 NO008
            printsvfSuccessKuriage( db2, svf, paramap, arrps[1] );

            //受験人数印刷
            arrps[0] = db2.prepareStatement( prestatTotal( paramap ) );
            if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;

            //全合格人数印刷 05/11/10
            arrps[0] = db2.prepareStatement( prestatSuccessTotal( paramap ) );
            if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;

            //合格人数印刷
            printsvfSuccessMain( db2, svf, paramap, arrps );

            //クラブ推薦人数印刷 NO009
            arrps[0] = db2.prepareStatement( prestatCulubSuisen( paramap ) );
            if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;

            //不合格人数印刷
            arrps[0] = db2.prepareStatement( prestatFailed( paramap ) );
            if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;

        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                for( int i = 0 ; i < arrps.length ; i++ )if( arrps[i] != null )arrps[i].close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return nonedata;
    }


    /**
     *  印刷処理 受験人数印刷および不合格人数印刷
     *
     */
    private boolean printsvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        ResultSet rs = null;

        try {
            rs = arrps[0].executeQuery();
            while( rs.next() ){
                printsvfBlankOut( svf, rs );  //--NO004
                svf.VrsOut( "ITEM",    printsvfOutDetailTitle( rs.getInt("LDIV") ) );
                printsvfOutDetail( svf, rs );  //NO012 不合格者集計仕様変更のため共通とする

                svf.VrEndRecord();
                nonedata = true;
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                if( rs != null )rs.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return nonedata;
    }


    /**
     *  帳票明細行印刷
     *
     */
    private void printsvfOutDetail( Vrw32alp svf, ResultSet rs )
    {
        try {
            //05/11/10 0も表記
            svf.VrsOut( "TOTAL1",  ( rs.getString("TOTAL_NUM")        != null )? rs.getString("TOTAL_NUM")       : "0" );
            svf.VrsOut( "BOY1",    ( rs.getString("MEN_NUM")          != null )? rs.getString("MEN_NUM")         : "0" );
            svf.VrsOut( "GIRL1",   ( rs.getString("FEMAIL_NUM")       != null )? rs.getString("FEMAIL_NUM")      : "0" );
            svf.VrsOut( "TOTAL2",  ( rs.getString("LOCAL_NUM")        != null )? rs.getString("LOCAL_NUM")       : "0" );
            svf.VrsOut( "BOY2",    ( rs.getString("LOCAL_MEN_NUM")    != null )? rs.getString("LOCAL_MEN_NUM")   : "0" );
            svf.VrsOut( "GIRL2",   ( rs.getString("LOCAL_FEMAIL_NUM") != null )? rs.getString("LOCAL_FEMAIL_NUM"): "0" );
            svf.VrsOut( "TOTAL3",  ( rs.getString("OTHER_NUM")        != null )? rs.getString("OTHER_NUM")       : "0" );
            svf.VrsOut( "BOY3",    ( rs.getString("OTHER_MEN_NUM")    != null )? rs.getString("OTHER_MEN_NUM")   : "0" );
            svf.VrsOut( "GIRL3",   ( rs.getString("OTHER_FEMAIL_NUM") != null )? rs.getString("OTHER_FEMAIL_NUM"): "0" );

            if( rs.getInt("LDIV") == 12 )setCulubNotSuccessNum( rs );  //NO009

        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  帳票明細行印刷 不合格人数
     *  2006/01/08 Build NO009
     */
    private void printsvfOutDetailFail( Vrw32alp svf, ResultSet rs )
    {
        int i = 0;
        try {
            arrculubsuisen[ i   ] += ( rs.getString("TOTAL_NUM")        != null )? Integer.parseInt( rs.getString("TOTAL_NUM") )       : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("MEN_NUM")          != null )? Integer.parseInt( rs.getString("MEN_NUM") )         : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("FEMAIL_NUM")       != null )? Integer.parseInt( rs.getString("FEMAIL_NUM") )      : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("LOCAL_NUM")        != null )? Integer.parseInt( rs.getString("LOCAL_NUM") )       : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("LOCAL_MEN_NUM")    != null )? Integer.parseInt( rs.getString("LOCAL_MEN_NUM") )   : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("LOCAL_FEMAIL_NUM") != null )? Integer.parseInt( rs.getString("LOCAL_FEMAIL_NUM") ): 0;
            arrculubsuisen[ ++i ] += ( rs.getString("OTHER_NUM")        != null )? Integer.parseInt( rs.getString("OTHER_NUM") )       : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("OTHER_MEN_NUM")    != null )? Integer.parseInt( rs.getString("OTHER_MEN_NUM") )   : 0;
            arrculubsuisen[ ++i ] += ( rs.getString("OTHER_FEMAIL_NUM") != null )? Integer.parseInt( rs.getString("OTHER_FEMAIL_NUM") ): 0;
        } catch( Exception ex ){
            log.error("arrculubsuisen add error! ",ex);
        }

        try {
            svf.VrsOut( "TOTAL1",  String.valueOf( arrculubsuisen[ 0 ] ) );
            svf.VrsOut( "BOY1",    String.valueOf( arrculubsuisen[ 1 ] ) );
            svf.VrsOut( "GIRL1",   String.valueOf( arrculubsuisen[ 2 ] ) );
            svf.VrsOut( "TOTAL2",  String.valueOf( arrculubsuisen[ 3 ] ) );
            svf.VrsOut( "BOY2",    String.valueOf( arrculubsuisen[ 4 ] ) );
            svf.VrsOut( "GIRL2",   String.valueOf( arrculubsuisen[ 5 ] ) );
            svf.VrsOut( "TOTAL3",  String.valueOf( arrculubsuisen[ 6 ] ) );
            svf.VrsOut( "BOY3",    String.valueOf( arrculubsuisen[ 7 ] ) );
            svf.VrsOut( "GIRL3",   String.valueOf( arrculubsuisen[ 8 ] ) );
        } catch( Exception ex ){
            log.error("svfout error! ",ex);
        }
    }


    /**
     *  行タイトル設定  受験人数印刷および不合格人数印刷
     *
     */
    private String printsvfOutDetailTitle( int ldiv )
    {
        String str = null;
        try {
            if( ldiv == 1 )     str = "受  験";
            else if( ldiv == 2 )str = "      専  願";
            else if( ldiv == 3 )str = "      併  願";
            else if( ldiv == 4 )str = "不合格";
            else if( ldiv == 5 )str = "     ( 得点 )";
            else if( ldiv == 6 )str = "     ( 面接 )";
            else if( ldiv == 7 )str = "合　格";
            else if( ldiv == 10 )str = "クラブ推薦者";    //NO009  NO012Modify
            else if( ldiv == 11 )str = "  （正規合格）";  //NO009  NO012Modify  NO013Modify
            else if( ldiv == 12 )str = "      推薦";    //NO009
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return str;
    }


    /**
     *  帳票ヘッダー等印刷
     *
     */
    private void printsvfOutHead( DB2UDB db2, Vrw32alp svf, Map paramap )
    {

        try {
            svf.VrsOut( "NENDO",     nao_package.KenjaProperties.gengou( Integer.parseInt( (String)paramap.get("YEAR") ) ) + "年度" );
            svf.VrsOut( "SCHOOLDIV", "高校" );
            setInfluenceName(db2, svf, paramap);
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            StringBuffer stb = new StringBuffer();
            stb.append( nao_package.KenjaProperties.gengou(Integer.parseInt( sdf.format(date) )) );
            sdf = new SimpleDateFormat("年M月d日H時m分");  //05/11/10
            stb.append( sdf.format(date) );
            svf.VrsOut( "DATE",      stb.toString() );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  SVF-FORM設定  
     *
     */
    private void setSvfformFormat( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        try {
            svf.VrSetForm( "KNJL350.frm", 4 );
            printsvfOutHead( db2, svf, paramap );            //SVF-FORM印刷
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

    }


    /**
     *  印刷処理 合格
     *
     */
    private void printsvfSuccessMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        try {
            String arrcourse[][] = setArrayCourse( db2, paramap );
            arrps[0] = db2.prepareStatement( prestatSuccess( paramap ) );       //合格 スライド無し
            arrps[1] = db2.prepareStatement( prestatSuccessSlide( paramap ) );  //合格 スライド有り
            arrps[2] = db2.prepareStatement( prestatSlideCourse( paramap ) );   //スライド項目名
            for( int i = 0 ; i < arrcourse.length && arrcourse[i][0] != null ; i++ ){
                if( arrcourse[i][2] == null )
                    printsvfSuccessSub1( db2, svf, paramap, arrps[0], arrcourse[i][0], arrcourse[i][1] ); //スライド無し
                else
                    printsvfSuccessSub2( db2, svf, paramap, arrps, arrcourse, i ); //スライド有り
            }
        } catch( Exception ex ) {
            log.error("error! ",ex);
        }
    }


    /**
     *  印刷処理 合格 スライド無し
     *
     */
    private void printsvfSuccessSub1( DB2UDB db2, Vrw32alp svf, Map paramap, 
                                      PreparedStatement ps,
                                      String mark,
                                      String name
                                    )
    {
        int pp = 0;
        ResultSet rs = null;
        try {
            pp = 0;
            ps.setString( ++pp,  mark );    //受験コース記号
            ps.setString( ++pp,  mark );    //受験コース記号  NO015
            rs = ps.executeQuery();
            while( rs.next() ){
                printsvfBlankOut( svf, rs );  //--NO004
                svf.VrsOut( "ITEM",    printsvfOutDetailTitleS( rs, name ) );
                printsvfOutDetail( svf, rs );
                svf.VrEndRecord();
            }
        } catch( Exception ex ) {
            log.error("error! ",ex);
        } finally{
            try {
                if( rs != null )rs.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
    }


    /**
     *  印刷処理 合格 スライド有り
     *
     */
    private void printsvfSuccessSub2( DB2UDB db2, Vrw32alp svf, Map paramap, 
                                      PreparedStatement arrps[],
                                      String arrcourse[][], int i
                                    )
    {
        int pp = 0;
        ResultSet arrrs[] = new ResultSet[2];
        try {
            pp = 0;
            arrps[1].setString( ++pp,  arrcourse[i][0] );    //受験コース記号
            arrps[1].setString( ++pp,  arrcourse[i][0] );    //受験コース記号
            arrps[1].setString( ++pp,  arrcourse[i][0] );    //受験コース記号   05/11/11 NO005
            arrps[1].setString( ++pp,  arrcourse[i][0] );    //受験コース記号   NO015
            arrrs[0] = arrps[1].executeQuery();
log.debug("arrcourse[i][0]="+arrcourse[i][0]);
            while( arrrs[0].next() ){
                printsvfBlankOut( svf, arrrs[0] );  //--NO004
                svf.VrsOut( "ITEM",    printsvfOutDetailTitleSL( arrrs, arrps, arrcourse, i ) );
                printsvfOutDetail( svf, arrrs[0] );
                svf.VrEndRecord();
            }
        } catch( Exception ex ) {
            log.error("error! ",ex);
        } finally{
            try {
                for( int j = 0 ; i < arrrs.length ; j++ )if( arrrs[j] != null )arrrs[j].close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
    }


    /**
     *  行タイトル設定  合格人数印刷 スライド無し
     *
     */
    private String printsvfOutDetailTitleS( ResultSet rs, String coursename )
    {
        String str = null;
        try {
            if( rs.getInt("SHDIV") == 0  &&  rs.getInt("JUDGEMENT") == 9 )
                str = coursename + "合格";
            else if( rs.getInt("SHDIV") == 1  &&  rs.getInt("JUDGEMENT") == 0 )
                str = "      専  願";
            else if( rs.getInt("SHDIV") == 2  &&  rs.getInt("JUDGEMENT") == 0 )
                str = "      併  願";
            else if( rs.getInt("JUDGEMENT") == 1 )
                str = "          Ａ";
            else if( rs.getInt("JUDGEMENT") == 2 )
                str = "          Ｂ";
            else
                str = "          計";
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return str;
    }


    /**
     *  行タイトル設定  合格人数印刷 スライド有り
     *
     */
    private String printsvfOutDetailTitleSL( ResultSet arrrs[], 
                                             PreparedStatement arrps[], 
                                             String arrcourse[][], int i
                                           )
    {
        String str = null;
        try {
            if( arrrs[0].getInt("DESIREDIV") == 0 ){
                if( arrrs[0].getInt("SHDIV") == 0 )
                    str = arrcourse[i][1] + "合格";
                else if( arrrs[0].getInt("SHDIV") == 1 )
                    str = "  専  願";
                else if( arrrs[0].getInt("SHDIV") == 2 )
                    str = "  併  願";
            } else if( arrrs[0].getInt("SHDIV") == 0 ){
                str = getTitleSlideCourse( arrrs, arrps, arrcourse, i );
            } else{
                if( arrrs[0].getInt("JUDGEMENT") == 0 ){
                    str = getTitleSlideCourse( arrrs, arrps, arrcourse, i );
                }
                else if( arrrs[0].getInt("JUDGEMENT") == 1 )
                    str = "          Ａ";  //NO020
                else if( arrrs[0].getInt("JUDGEMENT") == 2 )
                    str = "          Ｂ";  //NO020
                else
                    str = "          計";
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return str;
    }


    /**
     *  行タイトル設定  合格人数印刷 スライド有り コース編集
     *
     */
    private String getTitleSlideCourse( ResultSet arrrs[], 
                                        PreparedStatement arrps[],
                                        String arrcourse[][], int i
                                      )
    {
        int pp = 0;
        StringBuffer stb = new StringBuffer();
        try {
            pp = 0;
            arrps[2].setString( ++pp,  arrrs[0].getString("DESIREDIV") );    //志望区分
            arrrs[1] = arrps[2].executeQuery();
            int j = 0;
            stb.append("   (");
            while( arrrs[1].next() ){
                if( 0 < j++ )stb.append("->");  //06/01/08
                stb.append( arrrs[1].getString("EXAMCOURSE_ABBV") );
            }
            stb.append(")");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  印刷処理 空白行出力
     *  2005/12/30 Build NO004
     */
    private void printsvfBlankOut( Vrw32alp svf, ResultSet rs )
    {
        try {
            int d = Integer.parseInt( rs.getString("LDIV") );  //レコード種別フラグ
            if( ( 1 <= d  &&  d <= 3 ) || ( 7 <= d  &&  d <= 8 ) ){
                //受験者数、全体合格者数の場合
                if( 50 <= linenum )linenum = 0;
                linenum++; 
                return;
            } else{
                int i = 0;
                if( d == 9 )
                    //コース別スライド有り合格者数の場合
                    i = ( rs.getString("CODE2").equals("Z") )? 5: ( rs.getString("JUDGEMENT").equals("0") )? 4: 1;
                else if( d == 4 )
                    //不合格者数の場合
                    i = 3;
                else if( d == 10 )         //NO009
                    //クラブ推薦の場合
                    i = 3;                 //NO009
                else if( d == 11 )         //NO009
                    //クラブ推薦の場合
                    i = 2;                 //NO009
                else
                    //上記以外
                    i = 1;

                if(  50 < ( i + linenum ) ){
                    for( int j = linenum;  j < 50;  j++ )svf.VrEndRecord();
                    linenum = 0;
                }
                linenum++; 
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  印刷処理 繰上合格者人数印刷
     *  2006/01/08 Build NO008
     */
    private void printsvfSuccessKuriage( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement ps )
    {
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement( prestatSuccessKuriage( paramap ) );
            rs = ps.executeQuery();
            int cnt = 0;
            if( rs.next()  &&  rs.getString("TOTAL_NUM") != null )cnt = Integer.parseInt( rs.getString("TOTAL_NUM") );
            svf.VrsOut( "SUBTITLE",    "         (繰上合格者" + cnt + "名除く)" );  //NO010
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                db2.commit();
                if( rs != null )rs.close();
                if( ps != null )ps.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
    }


    /**
     *  クラブ推薦正規合格以外の人数をセット
     *  2006/01/08 Build NO009
     */
    private void setCulubNotSuccessNum( ResultSet rs )
    {
        try {
            int i = 0;
            arrculubsuisen[ i++ ] = ( rs.getString("TOTAL_NUM")        != null )? Integer.parseInt( rs.getString("TOTAL_NUM") )       : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("MEN_NUM")          != null )? Integer.parseInt( rs.getString("MEN_NUM") )         : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("FEMAIL_NUM")       != null )? Integer.parseInt( rs.getString("FEMAIL_NUM") )      : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("LOCAL_NUM")        != null )? Integer.parseInt( rs.getString("LOCAL_NUM") )       : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("LOCAL_MEN_NUM")    != null )? Integer.parseInt( rs.getString("LOCAL_MEN_NUM") )   : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("LOCAL_FEMAIL_NUM") != null )? Integer.parseInt( rs.getString("LOCAL_FEMAIL_NUM") ): 0;
            arrculubsuisen[ i++ ] = ( rs.getString("OTHER_NUM")        != null )? Integer.parseInt( rs.getString("OTHER_NUM") )       : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("OTHER_MEN_NUM")    != null )? Integer.parseInt( rs.getString("OTHER_MEN_NUM") )   : 0;
            arrculubsuisen[ i++ ] = ( rs.getString("OTHER_FEMAIL_NUM") != null )? Integer.parseInt( rs.getString("OTHER_FEMAIL_NUM") ): 0;
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  priparedstatement作成  共通列表
     */
    private String prestatColumn()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(       ",COUNT(*) AS TOTAL_NUM ");
            stb.append(       ",SUM( CASE WHEN SEX = '1' THEN 1 ELSE 0 END ) AS MEN_NUM ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' THEN 1 ELSE 0 END ) AS FEMAIL_NUM ");
            //NO021 Modify 再び大阪府出身者の抽出方法の変更
            stb.append(       ",SUM( CASE WHEN VALUE(LOCATIONCD,'0') BETWEEN '01' AND '35' THEN 1 ELSE 0 END ) AS LOCAL_NUM ");
            stb.append(       ",SUM( CASE WHEN VALUE(LOCATIONCD,'0') BETWEEN '01' AND '35' AND SEX = '1' THEN 1 ELSE 0 END ) AS LOCAL_MEN_NUM ");
            stb.append(       ",SUM( CASE WHEN VALUE(LOCATIONCD,'0') BETWEEN '01' AND '35' AND SEX = '2' THEN 1 ELSE 0 END ) AS LOCAL_FEMAIL_NUM ");
            stb.append(       ",SUM( CASE WHEN VALUE(LOCATIONCD,'0') NOT BETWEEN '01' AND '35' THEN 1 ELSE 0 END ) AS OTHER_NUM ");
            stb.append(       ",SUM( CASE WHEN VALUE(LOCATIONCD,'0') NOT BETWEEN '01' AND '35' AND SEX = '1' THEN 1 ELSE 0 END ) AS OTHER_MEN_NUM ");
            stb.append(       ",SUM( CASE WHEN VALUE(LOCATIONCD,'0') NOT BETWEEN '01' AND '35' AND SEX = '2' THEN 1 ELSE 0 END ) AS OTHER_FEMAIL_NUM ");
        } catch( Exception ex ) {
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  受験人数
     */
    private String prestatTotal( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //全体
            stb.append("SELECT  1 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '7') OR JUDGEMENT = '9') ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            //専願
            stb.append("UNION ALL ");
            stb.append("SELECT  2 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '7') OR JUDGEMENT = '9') ");
            stb.append(    "AND SHDIV = '1' ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            //併願
            stb.append("UNION ALL ");
            stb.append("SELECT  3 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '7') OR JUDGEMENT = '9') ");
            stb.append(    "AND SHDIV = '2' ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");

            stb.append("ORDER BY LDIV ");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  全合格人数
     *  2005/11/10 Build
     */
    private String prestatSuccessTotal( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //全体
            stb.append("SELECT  1 AS PDIV, 7 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(    "AND NOT JUDGEMENT IN('5') ");  // --NO015
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append("AND JUDGEMENT <> '4' ");  // --NO001
            //専願
            stb.append("UNION ALL ");
            stb.append("SELECT  2 AS PDIV, 2 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            stb.append(    "AND SHDIV = '1' ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(    "AND NOT JUDGEMENT IN('5') ");  // --NO015
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append("AND JUDGEMENT <> '4' ");  // --NO001
            //併願
            stb.append("UNION ALL ");
            stb.append("SELECT  3 AS PDIV, 3 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            stb.append(    "AND SHDIV = '2' ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(    "AND NOT JUDGEMENT IN('5') ");  // --NO015
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append("AND JUDGEMENT <> '4' ");  // --NO001

            stb.append("ORDER BY PDIV ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatSuccessTotal ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  不合格人数
     */
    private String prestatFailed( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //全体の受験者人数 NO012 Build
            stb.append("WITH ");
            stb.append("EXAM AS(");
            stb.append(   "SELECT  'A' AS DIV ");
            stb.append(    prestatColumn() );
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '7') OR JUDGEMENT = '9') ");
            stb.append(       "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(") ");
            //全体の合格者人数 NO012 Build
            stb.append(",SUCCESS AS(");
            stb.append(   "SELECT  'A' AS DIV ");
            stb.append(    prestatColumn() );
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            stb.append(       "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(       "AND NOT JUDGEMENT IN('5') ");  // --NO011 NO017
            stb.append(") ");
            //全体 NO012 Modify
            stb.append("SELECT  4 AS LDIV ");
            stb.append(       ",VALUE(T1.TOTAL_NUM,0)        - VALUE(T2.TOTAL_NUM,0)         AS TOTAL_NUM ");
            stb.append(       ",VALUE(T1.MEN_NUM,0)          - VALUE(T2.MEN_NUM,0)           AS MEN_NUM ");
            stb.append(       ",VALUE(T1.FEMAIL_NUM,0)       - VALUE(T2.FEMAIL_NUM,0)        AS FEMAIL_NUM ");
            stb.append(       ",VALUE(T1.LOCAL_NUM,0)        - VALUE(T2.LOCAL_NUM,0)         AS LOCAL_NUM ");
            stb.append(       ",VALUE(T1.LOCAL_MEN_NUM,0)    - VALUE(T2.LOCAL_MEN_NUM,0)     AS LOCAL_MEN_NUM ");
            stb.append(       ",VALUE(T1.LOCAL_FEMAIL_NUM,0) - VALUE(T2.LOCAL_FEMAIL_NUM,0)  AS LOCAL_FEMAIL_NUM ");
            stb.append(       ",VALUE(T1.OTHER_NUM,0)        - VALUE(T2.OTHER_NUM,0)         AS OTHER_NUM ");
            stb.append(       ",VALUE(T1.OTHER_MEN_NUM,0)    - VALUE(T2.OTHER_MEN_NUM,0)     AS OTHER_MEN_NUM ");
            stb.append(       ",VALUE(T1.OTHER_FEMAIL_NUM,0) - VALUE(T2.OTHER_FEMAIL_NUM,0)  AS OTHER_FEMAIL_NUM ");
            stb.append("FROM EXAM T1 ");
            stb.append("LEFT JOIN SUCCESS T2 ON T2.DIV = T1.DIV ");

            //得点
            //NO017Modify
            stb.append("UNION ALL ");
            stb.append("SELECT  5 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND JUDGEMENT IN ('7','5') ");  //--NO017
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(    "AND NOT APPLICANTDIV = '3' ");  //NO012
            //面接
            // NO012 Modify NO017Modify
            stb.append("UNION ALL ");
            stb.append("SELECT  6 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND JUDGEMENT IN('7','5') ");  //NO018
            stb.append(    "AND APPLICANTDIV = '3' ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append("ORDER BY LDIV ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("ps="+stb.toString());
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  合格人数（スライド無し）
     */
    private String prestatSuccess( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");

            //繰上合格者の表 NO015 Build
            stb.append("BASE_B AS(");
            stb.append(   "SELECT  T1.EXAMNO,T1.DESIREDIV, T3.WISHNO, T1.SEX, T1.LOCATIONCD, T1.SHDIV, T1.FS_CD ");
            stb.append(          ",CASE VALUE(T1.REGULARSUCCESS_FLG,'0') WHEN '2' THEN '1' WHEN '3' THEN '2' ELSE '0' END  AS JUDGEMENT ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN ENTEXAM_WISHDIV_MST T3 ON ");
            stb.append(                   "T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(               "AND T3.DESIREDIV = T1.DESIREDIV ");
            stb.append(               "AND T3.COURSECD = T1.SUC_COURSECD AND T3.MAJORCD = T1.SUC_MAJORCD AND T3.EXAMCOURSECD = T1.SUC_COURSECODE ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND NOT (T1.EXAMNO BETWEEN '5000' AND '5999') ");
            stb.append(       "AND T1.JUDGEMENT IN('6') ");
            stb.append(") ");

            stb.append(", BASE_A AS(");
            stb.append(   "SELECT  T1.EXAMNO, T1.SEX, T1.LOCATIONCD, T1.SHDIV, T1.FS_CD ");   //05/11/10 FS_CDを追加
            stb.append(          ",CASE VALUE(T1.REGULARSUCCESS_FLG,'0') WHEN '2' THEN '1' WHEN '3' THEN '2' ELSE '0' END  AS JUDGEMENT ");  //05/11/15
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN ENTEXAM_COURSE_MST T2 ON ");
            stb.append(                   "T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(               "AND T1.SUC_COURSECD = T2.COURSECD ");
            stb.append(               "AND T1.SUC_MAJORCD = T2.MAJORCD ");
            stb.append(               "AND T1.SUC_COURSECODE = T2.EXAMCOURSECD ");
            stb.append(               "AND T2.EXAMCOURSE_MARK = ? ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND ((T1.JUDGEMENT > '0' AND T1.JUDGEMENT <= '6') OR T1.JUDGEMENT = '9') ");
            stb.append(       "AND NOT (T1.EXAMNO BETWEEN '5000' AND '5999') ");
            stb.append(       "AND NOT ( T1.JUDGEMENT = '4' AND NOT VALUE(T1.REGULARSUCCESS_FLG,'0') IN('1','2','3') ) ");   // --NO006  NO010
            stb.append(       "AND NOT T1.JUDGEMENT IN('5','6') ");  // --NO011
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append(   "AND T1.JUDGEMENT <> '4' ");  // --NO001

            //繰上合格者を元のコースで追加 NO015 Build
            stb.append(   "UNION ");
            stb.append(   "SELECT  T1.EXAMNO, T1.SEX, T1.LOCATIONCD, T1.SHDIV, T1.FS_CD, T1.JUDGEMENT ");
            stb.append(   "FROM BASE_B T1 ");
            stb.append(   "INNER JOIN ENTEXAM_WISHDIV_MST T3 ON ");
            stb.append(              "T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(          "AND T3.DESIREDIV = T1.DESIREDIV ");
            stb.append(          "AND CHAR( INT(T1.WISHNO) + 1 ) = T3.WISHNO ");
            stb.append(   "INNER JOIN ENTEXAM_COURSE_MST T2 ON ");
            stb.append(              "T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(          "AND T2.COURSECD = T3.COURSECD AND T2.MAJORCD = T3.MAJORCD AND T2.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append(          "AND T2.EXAMCOURSE_MARK = ? ");

            stb.append(") ");
            
            stb.append(",SHDIV_JUDGEMENT_DIV (SHDIV,JUDGEMENT) AS (");
                            //コース計,専願計,専願&事前相談無計,専願&事前相談A計,専願&事前相談B計,専願計,併願&事前相談無計,併願&事前相談A計,併願&事前相談B計,併願計
            stb.append(    "VALUES('0','9'),('1','0'),('1','1'),('1','2'),('1','9'),('2','0'),('2','1'),('2','2'),('2','9')");
            stb.append(") ");
            
            stb.append(",CALC AS(");
            stb.append(   "SELECT  VALUE(SHDIV,'0')AS SHDIV ");
            stb.append(          ",VALUE(JUDGEMENT,'9') AS JUDGEMENT ");
            stb.append( prestatColumn() );
            stb.append(   "FROM    BASE_A ");
            stb.append(   "GROUP BY GROUPING SETS((),SHDIV,(SHDIV, JUDGEMENT)) ");
            stb.append(   "ORDER BY SHDIV,JUDGEMENT ");
            stb.append(") ");
            
            stb.append("SELECT  9 AS LDIV ");
            stb.append(       ",'0' AS CODE2 ");  //--NO004
            stb.append(       ",T2.SHDIV ");
            stb.append(       ",T2.JUDGEMENT ");
            stb.append(       ",TOTAL_NUM ");
            stb.append(       ",MEN_NUM ");
            stb.append(       ",FEMAIL_NUM ");
            stb.append(       ",LOCAL_NUM ");
            stb.append(       ",LOCAL_MEN_NUM ");
            stb.append(       ",LOCAL_FEMAIL_NUM ");
            stb.append(       ",OTHER_NUM ");
            stb.append(       ",OTHER_MEN_NUM ");
            stb.append(       ",OTHER_FEMAIL_NUM ");
            stb.append("FROM    SHDIV_JUDGEMENT_DIV T2 ");
            stb.append("LEFT JOIN CALC T1 ON T1.SHDIV = T2.SHDIV AND T1.JUDGEMENT = T2.JUDGEMENT ");
            stb.append("ORDER BY T2.SHDIV,T2.JUDGEMENT");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("ps="+stb.toString());
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  受験コース
     */
    private String prestatCourse( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            stb.append("COURSE AS(");
            stb.append(   "SELECT  T1.DESIREDIV, T1.WISHNO,T1.COURSECD,T1.MAJORCD,T1.EXAMCOURSECD ");
            stb.append(          ",EXAMCOURSE_MARK ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(      "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(      "AND T2.COURSECD = T1.COURSECD ");
            stb.append(      "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(      "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append(") ");

            stb.append("SELECT  T1.EXAMCOURSE_NAME, T1.EXAMCOURSE_MARK, ");
            stb.append(        "T2.EXAMCOURSE_MARK AS SLIDE, T2.WISHNO_MAX ");
            stb.append("FROM    ENTEXAM_COURSE_MST T1 ");
            stb.append("LEFT JOIN(");
            stb.append(   "SELECT  EXAMCOURSE_MARK, MAX(WISHNO) AS WISHNO_MAX ");
            stb.append(   "FROM    COURSE T1 ");
            stb.append(   "WHERE   '1' < T1.WISHNO ");
            stb.append(   "GROUP BY EXAMCOURSE_MARK ");
            stb.append(")T2 ON T2.EXAMCOURSE_MARK = T1.EXAMCOURSE_MARK ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(    "AND NOT EXISTS(SELECT  'X' FROM COURSE T2 ");        //NO019 中高一貫コースを除外の条件追加
            stb.append(                   "WHERE   T2.COURSECD = T1.COURSECD ");
            stb.append(                       "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(                       "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append(                       "AND T2.DESIREDIV = '00') ");
            stb.append("ORDER BY EXAMCOURSECD DESC");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  文字配列作成  受験コース
     *
     */
    private String [][] setArrayCourse( DB2UDB db2, Map paramap )
    {
        ResultSet rs = null;
        String ret[][] = new String[5][4];

        try {
            db2.query( prestatCourse( paramap ) );
            rs = db2.getResultSet();
            for( int i = 0 ; rs.next() && i < ret.length ; i++ ){
                ret[i][0] = rs.getString("EXAMCOURSE_MARK");
                ret[i][1] = rs.getString("EXAMCOURSE_NAME");
                ret[i][2] = rs.getString("SLIDE");                  //NOT NULL => スライド有り
                ret[i][3] = rs.getString("WISHNO_MAX");             //NOT NULL => 志望連番のMAX
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                if( rs != null )rs.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return ret;
    }


    /**
     *  priparedstatement作成  合格人数（スライド有り）
     */
    private String prestatSuccessSlide( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            //志望コースの表
            stb.append("COURSE_A AS("); //NO005
            stb.append(   "SELECT  T1.DESIREDIV, T1.WISHNO,T1.COURSECD||T1.MAJORCD||T1.EXAMCOURSECD AS CODE ");
            stb.append(          ",EXAMCOURSE_MARK ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T2.COURSECD = T1.COURSECD ");
            stb.append(       "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(       "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD");
            stb.append(") ");

            //COURSE_A表より、該当コースより下位のコースを除外した表 NO005
            stb.append(",COURSE AS(");
            stb.append(   "SELECT  T1.* ");
            stb.append(   "FROM    COURSE_A T1 ");
            stb.append(   "WHERE   EXISTS(SELECT  'X' FROM COURSE_A T2 ");
            stb.append(                  "WHERE   T2.EXAMCOURSE_MARK = ? ");
            stb.append(                      "AND T1.DESIREDIV=T2.DESIREDIV ");
            stb.append(                      "AND T1.WISHNO <= T2.WISHNO) ");
            stb.append(") ");

            //繰上合格者の表 NO015 Build
            stb.append(",BASE_B AS(");
            stb.append(   "SELECT  T1.EXAMNO,T1.DESIREDIV, T3.WISHNO, T1.SEX, T1.LOCATIONCD, T1.SHDIV, T1.FS_CD ");
            stb.append(          ",CASE VALUE(T1.REGULARSUCCESS_FLG,'0') WHEN '2' THEN '1' WHEN '3' THEN '2' ELSE '0' END  AS JUDGEMENT ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN ENTEXAM_WISHDIV_MST T3 ON ");
            stb.append(                   "T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(               "AND T3.DESIREDIV = T1.DESIREDIV ");
            stb.append(               "AND T3.COURSECD = T1.SUC_COURSECD AND T3.MAJORCD = T1.SUC_MAJORCD AND T3.EXAMCOURSECD = T1.SUC_COURSECODE ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND NOT (T1.EXAMNO BETWEEN '5000' AND '5999') ");
            stb.append(       "AND T1.JUDGEMENT IN('6') ");
            stb.append(") ");

            //該当コースでの合格者の表
            stb.append(", BASE_A AS(");
            stb.append(   "SELECT  T1.EXAMNO, T1.SEX, T1.LOCATIONCD, T1.SHDIV, T1.DESIREDIV, T1.SUC_COURSECODE, T1.FS_CD ");   //05/11/10 FS_CDを追加
            stb.append(          ",CASE VALUE(T1.REGULARSUCCESS_FLG,'0') WHEN '2' THEN '1' WHEN '3' THEN '2' ELSE '0' END  AS JUDGEMENT ");  //05/11/15
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN ENTEXAM_COURSE_MST T2 ON ");
            stb.append(                   "T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(               "AND T1.SUC_COURSECD = T2.COURSECD ");
            stb.append(               "AND T1.SUC_MAJORCD = T2.MAJORCD ");
            stb.append(               "AND T1.SUC_COURSECODE = T2.EXAMCOURSECD ");
            stb.append(               "AND T2.EXAMCOURSE_MARK = ? ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND ((T1.JUDGEMENT > '0' AND T1.JUDGEMENT <= '6') OR T1.JUDGEMENT = '9') ");
            stb.append(       "AND NOT T1.EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(       "AND NOT ( T1.JUDGEMENT = '4' AND NOT VALUE(T1.REGULARSUCCESS_FLG,'0') IN('1','2','3') ) ");   // --NO006  NO010
            stb.append(       "AND NOT T1.JUDGEMENT IN('5','6') ");  // --NO011
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append(   "AND JUDGEMENT <> '4' ");  // --NO001

            //繰上合格者を元のコースで追加 NO015 Build
            stb.append(   "UNION ");
            stb.append(   "SELECT  T1.EXAMNO, T1.SEX, T1.LOCATIONCD, T1.SHDIV, T1.DESIREDIV, T3.EXAMCOURSECD AS SUC_COURSECODE, T1.FS_CD, T1.JUDGEMENT ");
            stb.append(   "FROM BASE_B T1 ");
            stb.append(   "INNER JOIN ENTEXAM_WISHDIV_MST T3 ON ");
            stb.append(              "T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(          "AND T3.DESIREDIV = T1.DESIREDIV ");
            stb.append(          "AND CHAR( INT(T1.WISHNO) + 1 ) = T3.WISHNO ");
            stb.append(   "INNER JOIN ENTEXAM_COURSE_MST T2 ON ");
            stb.append(              "T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(          "AND T2.COURSECD = T3.COURSECD AND T2.MAJORCD = T3.MAJORCD AND T2.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append(          "AND T2.EXAMCOURSE_MARK = ? ");

            stb.append(") ");
            
            //帳票レイアウトに合わせた分類用の表
            stb.append(",SHDIV_JUDGEMENT_DIV (SHDIV,JUDGEMENT) AS (");
                            //コース計,専願計,専願&事前相談無計,専願&事前相談A計,専願&事前相談B計,専願計,併願&事前相談無計,併願&事前相談A計,併願&事前相談B計,併願計
            stb.append(    "VALUES('0','9'),('1','0'),('1','1'),('1','2'),('1','9'),('2','0'),('2','1'),('2','2'),('2','9')");
            stb.append(") ");

            //帳票レイアウトに合わせた分類用の表
            stb.append(",COURSE_DIV (DESIREDIV,CODE2,SHDIV,JUDGEMENT) AS(");
            stb.append(    "SELECT  DISTINCT DESIREDIV,CODE2,SHDIV,JUDGEMENT ");  //05/11/10
            stb.append(    "FROM   (SELECT  DESIREDIV, MAX(CODE2) AS CODE2, COUNT(*) AS COUNT ");
            stb.append(            "FROM   (SELECT  T1.DESIREDIV,WISHNO,EXAMCOURSE_MARK,T1.CODE, T2.CODE AS CODE2 ");
            stb.append(                    "FROM COURSE T1 ");
            stb.append(                    "INNER JOIN (");
            stb.append(                        "SELECT  DESIREDIV ");
            stb.append(                               ",CASE WHEN COUNT(*) = 1 THEN 'X' ELSE MAX(CODE) END AS CODE ");
            stb.append(                        "FROM COURSE ");
            stb.append(                        "GROUP BY DESIREDIV ");
            stb.append(                    ") T2 ON T1.DESIREDIV = T2.DESIREDIV ");
            stb.append(            "WHERE   EXISTS(");
            stb.append(                        "SELECT  'X' ");
            stb.append(                        "FROM    COURSE T2 ");
            stb.append(                        "WHERE   T1.DESIREDIV = T2.DESIREDIV ");
            stb.append(                            "AND T2.EXAMCOURSE_MARK = ? ");
            stb.append(                    ") ");
            stb.append(           ")T1 ");
            stb.append(    "GROUP BY DESIREDIV ");
            stb.append(    ")T1 ");
            stb.append(   ",SHDIV_JUDGEMENT_DIV T2 ");
            stb.append(    "UNION ");
                            //コース計,専願計,併願計
            stb.append(    "VALUES('0','Z','0','9'),('0','Z','1','9'),('0','Z','2','9') ");
            stb.append(") ");
            
            //集計の表
            stb.append(",CALC AS(");
            stb.append(   "SELECT  VALUE(DESIREDIV,'00')AS DESIREDIV ");
            stb.append(          ",VALUE(SHDIV,'0')AS SHDIV ");
            stb.append(          ",VALUE(JUDGEMENT,'9') AS JUDGEMENT ");
            stb.append( prestatColumn() );
            stb.append(   "FROM    BASE_A ");
            stb.append(   "GROUP BY GROUPING SETS((),DESIREDIV,SHDIV,(DESIREDIV, SHDIV),(DESIREDIV, SHDIV, JUDGEMENT)) ");   //05/11/10
            stb.append(") ");
            
            //志望コース別明細 --NO003
            stb.append(",CALC_2 AS(");
            stb.append(   "SELECT  9 AS LDIV, T2.DESIREDIV, T2.CODE2 ");
            stb.append(          ",T2.SHDIV ");
            stb.append(          ",T2.JUDGEMENT ");
            stb.append(          ",TOTAL_NUM ");
            stb.append(          ",MEN_NUM ");
            stb.append(          ",FEMAIL_NUM ");
            stb.append(          ",LOCAL_NUM ");
            stb.append(          ",LOCAL_MEN_NUM ");
            stb.append(          ",LOCAL_FEMAIL_NUM ");
            stb.append(          ",OTHER_NUM ");
            stb.append(          ",OTHER_MEN_NUM ");
            stb.append(          ",OTHER_FEMAIL_NUM ");
            stb.append(   "FROM    COURSE_DIV T2 ");
            stb.append(   "LEFT JOIN CALC T1 ON T1.SHDIV = T2.SHDIV AND T1.JUDGEMENT = T2.JUDGEMENT AND T1.DESIREDIV = T2.DESIREDIV ");
            stb.append(") ");

            //第１志望コース別合格者集計（複数コース志望） --NO003
            stb.append("SELECT  T2.LDIV ");
            stb.append(       ",MAX(T2.DESIREDIV) AS DESIREDIV ");
            stb.append(       ",T2.CODE2 ");
            stb.append(       ",T2.SHDIV ");
            stb.append(       ",T2.JUDGEMENT ");
            stb.append(       ",SUM(TOTAL_NUM) AS TOTAL_NUM ");
            stb.append(       ",SUM(MEN_NUM) AS MEN_NUM ");
            stb.append(       ",SUM(FEMAIL_NUM) AS FEMAIL_NUM ");
            stb.append(       ",SUM(LOCAL_NUM) AS LOCAL_NUM ");
            stb.append(       ",SUM(LOCAL_MEN_NUM) AS LOCAL_MEN_NUM ");
            stb.append(       ",SUM(LOCAL_FEMAIL_NUM) AS LOCAL_FEMAIL_NUM ");
            stb.append(       ",SUM(OTHER_NUM) AS OTHER_NUM ");
            stb.append(       ",SUM(OTHER_MEN_NUM) AS OTHER_MEN_NUM ");
            stb.append(       ",SUM(OTHER_FEMAIL_NUM) AS OTHER_FEMAIL_NUM ");
            stb.append("FROM    CALC_2 T2 ");
            stb.append("GROUP BY LDIV,SHDIV,CODE2,JUDGEMENT ");

            stb.append("ORDER BY SHDIV,CODE2 DESC,DESIREDIV DESC,JUDGEMENT");

        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatSuccessSlide ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  合格人数 スライド有り コース名
     */
    private String prestatSlideCourse( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(   "SELECT  EXAMCOURSE_ABBV, EXAMCOURSE_MARK ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(      "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(      "AND T2.COURSECD = T1.COURSECD ");
            stb.append(      "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(      "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append(      "AND DESIREDIV = ? ");
            stb.append(   "ORDER BY WISHNO");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  繰上合格人数
     *  2006/01/08 Build  NO008
     */
    private String prestatSuccessKuriage( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //全体
            stb.append("SELECT  COUNT(*) AS TOTAL_NUM ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND JUDGEMENT = '6' ");
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatSuccessTotal ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  クラブ推薦人数
     *  2006/01/08 Build  NO009
     *  2006/01/24 Modiyf NO013
     */
    private String prestatCulubSuisen( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //クラブ推薦人数
            stb.append("SELECT  10 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(    "AND APPLICANTDIV = '3' ");     // --NO012
            stb.append(    "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '4') OR JUDGEMENT = '9') ");       // --NO013
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append("AND JUDGEMENT <> '4' ");  // --NO001
            //クラブ推薦で正規合格人数
            stb.append("UNION ");
            stb.append("SELECT  11 AS LDIV ");
            stb.append( prestatColumn() );
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(       "AND ( ((JUDGEMENT > '0' AND JUDGEMENT <= '4') OR JUDGEMENT = '9') AND VALUE(REGULARSUCCESS_FLG,'0') IN('1','2','3') ) ");   // --NO006  NO010  NO012
            stb.append(       "AND APPLICANTDIV = '3' ");   // --NO012
            if( ( (String)paramap.get("RECOMMENDATION") ).equals("1") )
                stb.append("AND JUDGEMENT <> '4' ");  // --NO001

            stb.append(   "ORDER BY LDIV ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatSuccessTotal ps = " + stb.toString() );
        }
        return stb.toString();
    }


}
