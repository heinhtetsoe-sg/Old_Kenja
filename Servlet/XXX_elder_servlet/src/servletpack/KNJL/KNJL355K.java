// kanji=漢字
/*
 * $Id: 4f2daf6c5a5bd5310c06ebca0f11b04144c88fa2 $
 *
 * 作成日: 2005/12/30 11:25:40 - JST
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

import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 *  学校教育システム 賢者 [入試管理] 中学 府県別集計
 *    ○学校所在地コードによる府県別集計
 *        ○受験者は、志願者基礎データの合否判定フラグが(7)以下の者
 *        ○欠席者は、志願者基礎データの合否判定フラグが(8)の者
 *        ○志願者は、志願者基礎データの全て
 *    ○現住所コードによる府県別集計
 *　　　　○合格者は、志願者基礎データの合否判定フラグが(1,2,3,5,6)且つ正規合格フラグが(1)の者
 *    ○受験番号3000番台は集計から除外する
 *
 *  2005/12/30 yamashiro Build
 *  2006/01/05 yamashiro 受験番号3000番台は含む                            --NO001
 *                       ( )は付属小学校出身者数（志願者・受験者・欠席者） --NO001
 *  2006/01/06 yamashiro 
 *    ○合格者の条件は基礎データの合否判定<='6'とする  --NO002
 *      =>「志願者基礎データの合否判定フラグ(4)の者」を条件に追加し、「正規合格フラグが(1)の者」を条件から外す
 *  2006/01/07 yamashiro
 *    ○試験区分(印刷指示画面)に「付属推薦」を追加  --NO003
 *        ?「前期」・「後期」指定の場合は、附属推薦（合否区分=4）は除く
 *        ?「附属推薦」指定の場合は、附属推薦（合否区分=4）のみ出力する
 *        ? (??における)「附属推薦」の条件は、受験番号3000番台へ変更
 *          => 「附属推薦」=「付属」で、且つ受験番号3000番台はすべて合否区分=4となる  ( <- 宮城さんより )
 *        ? (○における)「付属推薦」は試験区分(印刷指示画面)には含めず、「一般・付属推薦」の選択とする
 *    ○学校所在地区別一覧の（　）の数字は附属出身者（国立区分natpubpridiv＝９）を出力する  --NO004
 *    ○学校所在地区別・現住所所在地区別とも、５種類の帳票(受験者・欠席者・志願者・合格者・入学者)を出力する  --NO005
 *　　○合格者の条件に「正規合格フラグが(1)の者」を再掲
 *  2006/01/08 yamashiro
 *    ○出力順を 志願者 -> 受験者 -> 欠席者 -> 合格者 -> 入学者 とする  --NO006
 *　　○合格者の条件に「正規合格フラグが(1)の者」を再再削除
 *    ○一般は「（一般）」、附属は「（附属）」と表記
 *  2006/01/10 yamashiro
 *    ○入学者は基礎データの「入学区分=2の者」を集計
 *  2006/01/11 yamashiro
 *    ○正しい地区コードへ変更  --NO007
 *  2006/01/14 yamashiro
 *    ○地域別集計一覧表を追加  --NO008
 *  2006/01/17 yamashiro
 *    ○地域別集計一覧表に現住所在地別を追加  --NO009
 *      => 印刷指示画面のラヂオボタンを変更
 */
public class KNJL355K
{
    private static final Log log = LogFactory.getLog(KNJL355K.class);


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
     *      "OUTPUT",     
     *
     *
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap )
    {
        try {
            paramap.put( "YEAR",       request.getParameter("YEAR")   );  //年度
            paramap.put( "TESTDIV",    request.getParameter("TESTDIV"));  //試験区分
            paramap.put( "OUTPUT",     request.getParameter("OUTPUT") );  //1:府県別集計 2:地域別集計

            if( ( (String)paramap.get("OUTPUT") ).equals("1") ){          //NO009によりif〜を追加
                paramap.put( "OUTPUTSUB",     request.getParameter("OUTPUT3") );  //1:学校所在地別 2:現住所在地別
            } else{
                paramap.put( "OUTPUTSUB",     request.getParameter("OUTPUT4") );  //1:学校所在地別 2:現住所在地別
            }

            //NO003 推薦の場合のみMapを作成
            if( request.getParameter("OUTPUT2") != null  &&  ( request.getParameter("OUTPUT2") ).equals("2") )
                paramap.put( "FUZOKUSUISEN",    request.getParameter("OUTPUT2") ); //1:一般 2:推薦
            paramap.put( "SPECIAL_REASON_DIV", request.getParameter("SPECIAL_REASON_DIV"));

        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        } finally {
log.debug("paramap="+paramap);
        }
    }


    /**
     *  印刷処理
     *  2006/01/07 Modify NO005
     *  2006/01/14 Modify NO008
     */
    private boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        AreaStatistics_Base areaobj = null;

        try {
            setHeadItem( db2, paramap );

            if( ( (String)paramap.get("OUTPUT") ).equals("2") ){                       // NO008 =>   NO009
                areaobj = new AreaStatistics_3();  //KNJL355_3.frm(地域別集計一覧)
                areaobj.setHeadItem( paramap );
                areaobj.svfprintSub( db2, svf, paramap );
            } else{                                                                    // <= NO008
                areaobj = new AreaStatistics_1();  //KNJL355_1.frm(受験者・欠席者・志願者)
                areaobj.setHeadItem( paramap );    //NO005
                areaobj.svfprintSub( db2, svf, paramap );

                areaobj = new AreaStatistics_2();  //KNJL355_2.frm(合格者・入学者)
                areaobj.setHeadItem( paramap );    //NO005
                areaobj.svfprintSub( db2, svf, paramap );
            }
            nonedata = true;  //印刷処理
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /**
     *  帳票見出し項目設定
     */
    private void setHeadItem( DB2UDB db2, Map paramap )
    {
        String str = null;
        ResultSet rs = null;
        try {
            if( ! paramap.containsKey("NENDO") ){
                str = nao_package.KenjaProperties.gengou( Integer.parseInt( (String)paramap.get("YEAR") ) ) + "年度";
                paramap.put( "NENDO",  str );                                   //年度
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        //試験区分
        try {
            if( ! paramap.containsKey("TESTDIVNAME") ){
                str = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + (String)paramap.get("TESTDIV") + "'";
                db2.query( str );
                rs = db2.getResultSet();
                if( rs.next() )paramap.put( "TESTDIVNAME",  rs.getString("NAME1") );        //試験名称
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            StringBuffer stb = new StringBuffer();
            stb.append( nao_package.KenjaProperties.gengou(Integer.parseInt( sdf.format(date) )) );
            sdf = new SimpleDateFormat("年M月d日");
            stb.append( sdf.format(date) );
            paramap.put( "NOW_DATE",  stb.toString() );     //作成日
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }



/**
 *  印刷処理の内部クラス 基本
 */
private abstract class AreaStatistics_Base
{
    PreparedStatement ps;
    ResultSet rs = null;
    String addfield = null;  //学校所在地コード OR 現住地コード NO005

    abstract void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap );


    /**
     *  初期設定
     *  2006/01/07 Build NO005
     */
    void setHeadItem( Map paramap )
    {
        try {
            //学校所在地コード OR 現住地コード NO005
            addfield = ( ( (String)paramap.get("OUTPUTSUB") ).equals("2") )? "ADDRESSCD": "LOCATIONCD";  //NO008 Modify NO009Modify
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  帳票ヘッダー等印刷
     */
    void printsvfOutHead( DB2UDB db2, Vrw32alp svf, Map paramap, int i )
    {
        try {
            svf.VrsOut( "NENDO",     (String)paramap.get("NENDO") );
            svf.VrsOut( "TESTDIV",   (String)paramap.get("TESTDIVNAME") );
            svf.VrsOut( "DATE",      (String)paramap.get("NOW_DATE") );
            svf.VrsOut( "SUBTITLE",  ( addfield.equals("LOCATIONCD") )? "学校所在地": "現住所在地" );  //NO005
            svf.VrsOut( "SUBTITLE2", ( paramap.containsKey("FUZOKUSUISEN") )? "（附属）": "（一般）" );  //06/01/08
            setInfluenceName(db2, svf, paramap);
            printsvfOutHead2( svf, i );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

    }

    abstract void printsvfOutHead2( Vrw32alp svf, int i );

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
}


/**
 *  府県別集計表＿１(第１志望のみ)
 */
private class AreaStatistics_1 extends AreaStatistics_Base
{
    /**
     *  府県別集計表＿１(第１志望のみ)　帳票ヘッダー等印刷
     */
    void printsvfOutHead2( Vrw32alp svf, int i )
    {
        try {
            svf.VrsOut( "TITLEDIV",  ( ( i == 1 )? "受験者": ( i ==2 )? "欠席者": ( i == 3 )? "志願者":"合格者" ) );

            for( int j = 1; j <= 4; j++ )for( int k = 1; k <= 7; k++ )for( int m = 1; m <= 3; m++ ){
                svf.VrsOutn( "COUNT" + j + "_" + k, m, "0" );
                svf.VrsOutn( "ADVANCE" + j + "_" + k, m, "0" );  //--NO001
            }
            for( int j = 1; j <= 4; j++ ){
                for( int m = 1; m <= 3; m++ ){
                    svf.VrsOutn( "TOTAL" + j, m, "0" );
                    svf.VrsOutn( "TOTAL_ADVANCE" + j, m, "0" );  //--NO001
                }
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  府県別集計表＿１(第１志望のみ)　印刷処理
     */
    void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm( "KNJL355_1.frm", 1 );
            svfprintSubDetail( db2, svf, paramap, 3 );  //志願者 NO006
            svfprintSubDetail( db2, svf, paramap, 1 );  //受験者 NO006
            svfprintSubDetail( db2, svf, paramap, 2 );  //欠席者 NO006
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        } finally{
            try {
                if( rs != null )rs.close();
                if( ps != null )ps.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
    }


    /**
     *  府県別集計表＿１(第１志望のみ)　印刷処理
     *  2006/01/08 Build NO006
     */
    private void svfprintSubDetail( DB2UDB db2, Vrw32alp svf, Map paramap, int i )
    {
        try {
            printsvfOutHead( db2, svf, paramap, i );
            ps = db2.prepareStatement( preStatSub( paramap, i ) );
            rs = ps.executeQuery();
            svfprintDetail( svf, rs, paramap );
            svf.VrEndPage();
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿１(第１志望のみ)　各集計種別印刷
     */
    private void svfprintDetail( Vrw32alp svf, ResultSet rs, Map paramap )
    {

        try {
            int pdiv = 0;  //コース種別
            int sex = 0;   //性別
            while( rs.next() ){
                if( ! rs.getString("SEX").equals("1")  &&  ! rs.getString("SEX").equals("2")  &&  ! rs.getString("SEX").equals("3") )continue;
                if( pdiv != Integer.parseInt( rs.getString("PDIV") )  ||  sex != Integer.parseInt( rs.getString("SEX") ) ){
                    pdiv = Integer.parseInt( rs.getString("PDIV") );
                    sex = Integer.parseInt( rs.getString("SEX") );
                }
                if( Integer.parseInt( rs.getString("LOCATIONCD") ) < 8 ){
                    svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  sex,  rs.getString("CNT") );
                    svf.VrsOutn( "ADVANCE" + pdiv + "_" + rs.getString("LOCATIONCD"),  sex,  rs.getString("FCNT") );  //--NO001
                } else{
                    svf.VrsOutn( "TOTAL" + pdiv,  sex,  rs.getString("CNT") );
                    svf.VrsOutn( "TOTAL_ADVANCE" + pdiv,  sex,  rs.getString("FCNT") );  //--NO001
                }
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿１(第１志望のみ)　ＳＱＬ作成
     */
    private String preStatSub( Map paramap, int div )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            //志望コース(第１志望)の表
            stb.append("COURSE AS( ");
            stb.append(   "SELECT  T1.DESIREDIV, T2.EXAMCOURSE_MARK  ");
            stb.append(          ",T2.COURSECD,T2.MAJORCD,T2.EXAMCOURSECD  ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2  ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "'  ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD  ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD  ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD  ");
            stb.append(           "AND T1.WISHNO = '1'  ");
            stb.append(   "ORDER BY T1.DESIREDIV, T1.WISHNO  ");
            stb.append(")  ");

            //対象者の表
            stb.append(",BASE_A AS(");
            stb.append(   "SELECT  EXAMNO,T1.DESIREDIV,T1.APPLICANTDIV,T1.SEX,T1.JUDGEMENT ");
            //NO007
            stb.append(          ",CASE WHEN VALUE(" + addfield + ",'0') BETWEEN '01' AND '39' THEN '1' "); //大阪
            stb.append(                "WHEN VALUE(" + addfield + ",'0') BETWEEN '40' AND '48' THEN '2' "); //奈良
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '50' THEN '3' "); //兵庫
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '60' THEN '4' "); //滋賀
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '70' THEN '5' "); //京都
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '80' THEN '6' "); //和歌山
            stb.append(                "ELSE '7' END AS LOCATIONCD "); //その他
            stb.append(          ",T2.EXAMCOURSE_MARK ");
            stb.append(          ",CASE WHEN VALUE(NATPUBPRIDIV,'0') = '9' THEN 1 ELSE 0 END AS FUZOKU ");  //--NO004
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN  COURSE T2 ON T2.DESIREDIV = T1.DESIREDIV ");
            stb.append(   "WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("       AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            if( paramap.containsKey("FUZOKUSUISEN") )                        //NO003
                stb.append(   "AND EXAMNO BETWEEN '3000' AND '3999' ");      //NO003
            else
                stb.append(   "AND NOT EXAMNO BETWEEN '3000' AND '3999' ");  //NO003

            if( div == 1 )
                stb.append(   "AND ((JUDGEMENT > '0' AND JUDGEMENT <= '7') OR JUDGEMENT = '9') ");  //受験者数
            else if( div == 2 )
                stb.append(   "AND JUDGEMENT = '8' ");   //欠席者数
            stb.append(") ");

            //メイン表
                    //総数
            stb.append("SELECT  1 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN FUZOKU = 1 THEN 1 ELSE 0 END) AS FCNT ");  //--NO001
            stb.append("FROM    BASE_A T1 ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");
                    //医薬進学コース
            stb.append("UNION ");
            stb.append("SELECT  2 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN FUZOKU = 1 THEN 1 ELSE 0 END) AS FCNT ");  //--NO001
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   EXAMCOURSE_MARK = 'I' ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");
                    //英数特進コース
            stb.append("UNION ");
            stb.append("SELECT  3 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN FUZOKU = 1 THEN 1 ELSE 0 END) AS FCNT ");  //--NO001
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   EXAMCOURSE_MARK = 'T' ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");
                    //英数標準コース
            stb.append("UNION ");
            stb.append("SELECT  4 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN FUZOKU = 1 THEN 1 ELSE 0 END) AS FCNT ");  //--NO001
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   EXAMCOURSE_MARK = 'H' ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");

            stb.append("ORDER BY PDIV,SEX,LOCATIONCD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  府県別集計表＿２(全志望)
 */
private class AreaStatistics_2 extends AreaStatistics_Base
{
    /**
     *  府県別集計表＿２(全志望)　帳票ヘッダー等印刷
     */
    void printsvfOutHead2( Vrw32alp svf, int i )
    {
        try {
            svf.VrsOut( "TITLENAME",  ( ( i == 5 )? "合格": "入学" ) );  //NO005


            for( int k = 1; k <= 7; k++ )for( int m = 1; m <= 15; m++ )svf.VrsOutn( "COUNT1" + "_" + k, m, "0" );
            for( int k = 1; k <= 7; k++ )for( int m = 1; m <= 3;  m++ )svf.VrsOutn( "COUNT2" + "_" + k, m, "0" );
            for( int k = 1; k <= 7; k++ )for( int m = 1; m <= 9;  m++ )svf.VrsOutn( "COUNT3" + "_" + k, m, "0" );
            for( int k = 1; k <= 7; k++ )for( int m = 1; m <= 12; m++ )svf.VrsOutn( "COUNT4" + "_" + k, m, "0" );

            for( int m = 1; m <= 15; m++ )svf.VrsOutn( "TOTAL1", m, "0" );
            for( int m = 1; m <= 3;  m++ )svf.VrsOutn( "TOTAL2", m, "0" );
            for( int m = 1; m <= 9;  m++ )svf.VrsOutn( "TOTAL3", m, "0" );
            for( int m = 1; m <= 12; m++ )svf.VrsOutn( "TOTAL4", m, "0" );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  府県別集計表＿２(全志望)　印刷処理
     */
    void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm( "KNJL355_2.frm", 1 );
            for( int i = 5; i <= 6; i++ ){              //NO005
                printsvfOutHead( db2, svf, paramap, i );
                ps = db2.prepareStatement( preStatSub( paramap, i ) );
                rs = ps.executeQuery();
                svfprintDetail( svf, rs, paramap );
                svf.VrEndPage();
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        } finally{
            try {
                if( rs != null )rs.close();
                if( ps != null )ps.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
    }


    /**
     *  府県別集計表＿２(全志望)　各集計種別印刷
     */
    private void svfprintDetail( Vrw32alp svf, ResultSet rs, Map paramap )
    {

        try {
            int pdiv = 0;  //コース種別
            int sex = 0;   //性別
            while( rs.next() ){
                if( ! rs.getString("SEX").equals("1")  &&  ! rs.getString("SEX").equals("2")  &&  ! rs.getString("SEX").equals("3") )continue;
                if( pdiv != Integer.parseInt( rs.getString("PDIV") )  ||  sex != Integer.parseInt( rs.getString("SEX") ) ){
                    pdiv = Integer.parseInt( rs.getString("PDIV") );
                    sex = Integer.parseInt( rs.getString("SEX") );
                }
                if( pdiv == 1 )svfprintDetail1( svf, rs, pdiv, ( sex - 1 ) * 5 );  //総数
                if( pdiv == 2 )svfprintDetail2( svf, rs, pdiv, ( sex - 1 ) * 1 );  //医薬進学コース
                if( pdiv == 3 )svfprintDetail3( svf, rs, pdiv, ( sex - 1 ) * 3 );  //英数特進コース
                if( pdiv == 4 )svfprintDetail4( svf, rs, pdiv, ( sex - 1 ) * 4 );  //英数標準コース
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿２(全志望)　合格者総数明細印刷
     */
    private void svfprintDetail1( Vrw32alp svf, ResultSet rs, int pdiv, int i )
    {

        try {
            if( Integer.parseInt( rs.getString("LOCATIONCD") ) < 8 ){
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 1,  rs.getString("CNT")  ); //合計
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 2,  rs.getString("CNT1") ); //第１志望合格
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 3,  rs.getString("CNT2") ); //第２志望合格 I => T
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 4,  rs.getString("CNT3") ); //第３志望合格 I => H
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 5,  rs.getString("CNT4") ); //第２志望合格 T => H
            } else{
                svf.VrsOutn( "TOTAL" + pdiv,  i + 1,  rs.getString("CNT")  ); //合計
                svf.VrsOutn( "TOTAL" + pdiv,  i + 2,  rs.getString("CNT1") ); //第１志望合格
                svf.VrsOutn( "TOTAL" + pdiv,  i + 3,  rs.getString("CNT2") ); //第２志望合格 I => T
                svf.VrsOutn( "TOTAL" + pdiv,  i + 4,  rs.getString("CNT3") ); //第３志望合格 I => H
                svf.VrsOutn( "TOTAL" + pdiv,  i + 5,  rs.getString("CNT4") ); //第２志望合格 T => H
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿２(全志望)　医薬進学コース合格者明細印刷
     */
    private void svfprintDetail2( Vrw32alp svf, ResultSet rs, int pdiv, int i )
    {

        try {
            if( Integer.parseInt( rs.getString("LOCATIONCD") ) < 8 ){
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 1,  rs.getString("CNT")  ); //合計
            } else{
                svf.VrsOutn( "TOTAL" + pdiv,  i + 1,  rs.getString("CNT")  ); //合計
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿２(全志望)　英数特進コース合格者明細印刷
     */
    private void svfprintDetail3( Vrw32alp svf, ResultSet rs, int pdiv, int i )
    {

        try {
            if( Integer.parseInt( rs.getString("LOCATIONCD") ) < 8 ){
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 1,  rs.getString("CNT")  ); //合計
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 2,  rs.getString("CNT1") ); //第１志望合格
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 3,  rs.getString("CNT2") ); //第２志望合格 I => T
            } else{
                svf.VrsOutn( "TOTAL" + pdiv,  i + 1,  rs.getString("CNT")  ); //合計
                svf.VrsOutn( "TOTAL" + pdiv,  i + 2,  rs.getString("CNT1") ); //第１志望合格
                svf.VrsOutn( "TOTAL" + pdiv,  i + 3,  rs.getString("CNT2") ); //第１志望合格 I => T
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿２(全志望)　英数標準コース合格者明細印刷
     */
    private void svfprintDetail4( Vrw32alp svf, ResultSet rs, int pdiv, int i )
    {

        try {
            if( Integer.parseInt( rs.getString("LOCATIONCD") ) < 8 ){
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 1,  rs.getString("CNT")  ); //合計
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 2,  rs.getString("CNT1") ); //第１志望合格
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 3,  rs.getString("CNT3") ); //第３志望合格 I => H
                svf.VrsOutn( "COUNT" + pdiv + "_" + rs.getString("LOCATIONCD"),  i + 4,  rs.getString("CNT4") ); //第２志望合格 T => H
            } else{
                svf.VrsOutn( "TOTAL" + pdiv,  i + 1,  rs.getString("CNT")  ); //合計
                svf.VrsOutn( "TOTAL" + pdiv,  i + 2,  rs.getString("CNT1") ); //第１志望合格
                svf.VrsOutn( "TOTAL" + pdiv,  i + 3,  rs.getString("CNT3") ); //第３志望合格 I => H
                svf.VrsOutn( "TOTAL" + pdiv,  i + 4,  rs.getString("CNT4") ); //第２志望合格 T => H
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿２(全志望)　ＳＱＬ作成
     */
    private String preStatSub( Map paramap, int div )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            //志望コース(第１志望)の表
            stb.append("COURSE AS( ");
            stb.append(   "SELECT  T1.DESIREDIV, T2.EXAMCOURSE_MARK  ");
            stb.append(          ",T2.COURSECD,T2.MAJORCD,T2.EXAMCOURSECD  ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2  ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "'  ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD  ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD  ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD  ");
            stb.append(           "AND T1.WISHNO = '1'  ");
            stb.append(   "ORDER BY T1.DESIREDIV, T1.WISHNO  ");
            stb.append(")  ");

            //合格者の表
            stb.append(",BASE_A AS(");
            stb.append(   "SELECT  EXAMNO,T1.DESIREDIV,T1.APPLICANTDIV,T1.SEX,T1.JUDGEMENT ");
            stb.append(          ",T4.EXAMCOURSE_MARK AS SUCCESS ");
            stb.append(          ",T2.EXAMCOURSE_MARK AS WISH ");
            //NO007
            stb.append(          ",CASE WHEN VALUE(" + addfield + ",'0') BETWEEN '01' AND '39' THEN '1' "); //大阪
            stb.append(                "WHEN VALUE(" + addfield + ",'0') BETWEEN '40' AND '48' THEN '2' "); //奈良
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '50' THEN '3' "); //兵庫
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '60' THEN '4' "); //滋賀
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '70' THEN '5' "); //京都
            stb.append(                "WHEN VALUE(" + addfield + ",'0') = '80' THEN '6' "); //和歌山
            stb.append(                "ELSE '7' END AS LOCATIONCD "); //その他
            stb.append(          ",T2.EXAMCOURSE_MARK ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(                                   "AND T4.COURSECD = T1.SUC_COURSECD AND T4.MAJORCD = T1.SUC_MAJORCD AND T4.EXAMCOURSECD = T1.SUC_COURSECODE ");
            stb.append(   "INNER JOIN COURSE T2 ON T2.DESIREDIV = T1.DESIREDIV ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("       AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            if( paramap.containsKey("FUZOKUSUISEN") )                           //NO003
                stb.append(   "AND T1.EXAMNO BETWEEN '3000' AND '3999' ");      //NO003
            else
                stb.append(   "AND NOT T1.EXAMNO BETWEEN '3000' AND '3999' ");  //NO003

            if( div == 5 )                                                       //NO005
                stb.append(   "AND T1.JUDGEMENT IN('1','2','3','4','5','6','9') ");  //NO002
            else
                stb.append(   "AND T1.ENTDIV IN('2') ");                         //NO005  06/01/10Modify
            stb.append(") ");

            //メイン表
                    //総数
            stb.append("SELECT  1 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN WISH = SUCCESS THEN 1 ELSE 0 END) AS CNT1 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'T' THEN 1 ELSE 0 END) AS CNT2 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT3 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'T' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT4 ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");
                    //医薬進学コース
            stb.append("UNION ");
            stb.append("SELECT  2 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN WISH = SUCCESS THEN 1 ELSE 0 END) AS CNT1 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'T' THEN 1 ELSE 0 END) AS CNT2 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT3 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'T' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT4 ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   SUCCESS = 'I' ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");
                    //英数特進コース
            stb.append("UNION ");
            stb.append("SELECT  3 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN WISH = SUCCESS THEN 1 ELSE 0 END) AS CNT1 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'T' THEN 1 ELSE 0 END) AS CNT2 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT3 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'T' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT4 ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   SUCCESS = 'T' ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");
                    //英数標準コース
            stb.append("UNION ");
            stb.append("SELECT  4 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'8') AS LOCATIONCD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append(       ",SUM(CASE WHEN WISH = SUCCESS THEN 1 ELSE 0 END) AS CNT1 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'T' THEN 1 ELSE 0 END) AS CNT2 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'I' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT3 ");
            stb.append(       ",SUM(CASE WHEN WISH = 'T' AND SUCCESS = 'H' THEN 1 ELSE 0 END) AS CNT4 ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   SUCCESS = 'H' ");
            stb.append("GROUP BY GROUPING SETS((),(SEX),(LOCATIONCD),(SEX,LOCATIONCD)) ");

            stb.append("ORDER BY PDIV,SEX,LOCATIONCD ");

        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  府県別集計表＿３
 *  2006/01/14 Build NO008
 */
private class AreaStatistics_3 extends AreaStatistics_Base
{
    /**
     *  府県別集計表＿３　帳票ヘッダー等印刷
     */
    void printsvfOutHead2( Vrw32alp svf, int i )
    {
        try {
            for( int j = 1; j <= 4; j++ )for( int k = 1; k <= 5; k++ )for( int m = 1; m <= 3; m++ ){
                svf.VrsOutn( "INSIDE" + j + "_" + k, m, "0" );
                svf.VrsOutn( "OUTSIDE" + j + "_" + k, m, "0" );
            }
            for( int j = 1; j <= 4; j++ ){
                for( int m = 1; m <= 3; m++ ){
                    svf.VrsOutn( "INSIDE_TOTAL" + j, m, "0" );
                    svf.VrsOutn( "OUTSIDE_TOTAL" + j, m, "0" );
                }
            }
            for( int j = 1; j <= 4; j++ ){
                for( int m = 1; m <= 3; m++ ){
                    svf.VrsOutn( "TOTAL" + j, m, "0" );
                }
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  府県別集計表＿３　印刷処理
     */
    void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            svf.VrSetForm( "KNJL355_3.frm", 1 );
            printsvfOutHead( db2, svf, paramap, 0 );
            ps = db2.prepareStatement( preStatSub( paramap ) );
            rs = ps.executeQuery();
            svfprintDetail( svf, rs, paramap );
            svf.VrEndPage();
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        } finally{
            try {
                if( rs != null )rs.close();
                if( ps != null )ps.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
    }


    /**
     *  府県別集計表＿３　各集計種別印刷
     */
    private void svfprintDetail( Vrw32alp svf, ResultSet rs, Map paramap )
    {

        try {
            int i = 0;
            int j = 0;
            while( rs.next() ){
                i = rs.getInt("PDIV");
                if( ! rs.getString("SEX").equals("1")  &&  ! rs.getString("SEX").equals("2")  &&  ! rs.getString("SEX").equals("3") )continue;
                j = Integer.parseInt( rs.getString("SEX") );

                if( Integer.parseInt( rs.getString("LOCATIONCD") ) == 1 ){
                    if( Integer.parseInt( rs.getString("NATPUB") ) < 6 )
                        svf.VrsOutn( "INSIDE" + i + "_" + rs.getString("NATPUB"),  j,  rs.getString("CNT") );
                    else
                        svf.VrsOutn( "INSIDE_TOTAL" + i,  j,  rs.getString("CNT") );
                } else if( Integer.parseInt( rs.getString("LOCATIONCD") ) == 2 ){
                    if( Integer.parseInt( rs.getString("NATPUB") ) < 6 )
                        svf.VrsOutn( "OUTSIDE" + i + "_" + rs.getString("NATPUB"),  j,  rs.getString("CNT") );
                    else
                        svf.VrsOutn( "OUTSIDE_TOTAL" + i,  j,  rs.getString("CNT") );
                } else
                    svf.VrsOutn( "TOTAL" + i,  j,  rs.getString("CNT") );
            }
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
    }


    /**
     *  府県別集計表＿３　ＳＱＬ作成
     */
    private String preStatSub( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            //志望コース(第１志望)の表
            stb.append("COURSE AS( ");
            stb.append(   "SELECT  T1.DESIREDIV, T2.EXAMCOURSE_MARK  ");
            stb.append(          ",T2.COURSECD,T2.MAJORCD,T2.EXAMCOURSECD  ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2  ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "'  ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD  ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD  ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD  ");
            stb.append(           "AND T1.WISHNO = '1'  ");
            stb.append(   "ORDER BY T1.DESIREDIV, T1.WISHNO  ");
            stb.append(")  ");

            //対象者の表
            stb.append(",BASE_A AS(");
            stb.append(   "SELECT  EXAMNO,T1.DESIREDIV,T1.APPLICANTDIV,T1.SEX,T1.JUDGEMENT,T1.ENTDIV ");
            stb.append(          ",CASE WHEN VALUE(" + addfield + ",'0') BETWEEN '01' AND '39' THEN '1' ");  //大阪
            stb.append(                "ELSE '2' END AS LOCATIONCD ");  //その他
            stb.append(          ",CASE VALUE(NATPUBPRIDIV,'0') WHEN '9' THEN '1' ");
            stb.append(                                        "WHEN '3' THEN '2' ");
            stb.append(                                        "WHEN '2' THEN '3' ");
            stb.append(                                        "WHEN '1' THEN '4' ");
            stb.append(                                        "ELSE '5' END AS NATPUB ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(   "INNER JOIN  COURSE T2 ON T2.DESIREDIV = T1.DESIREDIV ");
            stb.append(   "WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("       AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(       "AND TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            if( paramap.containsKey("FUZOKUSUISEN") )
                stb.append(   "AND EXAMNO BETWEEN '3000' AND '3999' ");
            else
                stb.append(   "AND NOT EXAMNO BETWEEN '3000' AND '3999' ");
            stb.append(") ");

            //メイン表
            //（志願者数）
            stb.append("SELECT  1 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'3') AS LOCATIONCD ");
            stb.append(       ",VALUE(NATPUB,'6') AS NATPUB ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("GROUP BY GROUPING SETS( (SEX,LOCATIONCD,NATPUB),(SEX,LOCATIONCD),(LOCATIONCD,NATPUB),(LOCATIONCD),(SEX),() ) ");

            //（受験者数）
            stb.append("UNION ");
            stb.append("SELECT  2 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'3') AS LOCATIONCD ");
            stb.append(       ",VALUE(NATPUB,'6') AS NATPUB ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   ((T1.JUDGEMENT > '0' AND T1.JUDGEMENT <= '7') OR T1.JUDGEMENT = '9') ");  //受験者数
            stb.append("GROUP BY GROUPING SETS( (SEX,LOCATIONCD,NATPUB),(SEX,LOCATIONCD),(LOCATIONCD,NATPUB),(LOCATIONCD),(SEX),() ) ");

            //（合格者数）
            stb.append("UNION ");
            stb.append("SELECT  3 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'3') AS LOCATIONCD ");
            stb.append(       ",VALUE(NATPUB,'6') AS NATPUB ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   T1.JUDGEMENT IN('1','2','3','4','5','6','9') ");  //合格者数
            stb.append("GROUP BY GROUPING SETS( (SEX,LOCATIONCD,NATPUB),(SEX,LOCATIONCD),(LOCATIONCD,NATPUB),(LOCATIONCD),(SEX),() ) ");

            //（入学者数）
            stb.append("UNION ");
            stb.append("SELECT  4 AS PDIV ");
            stb.append(       ",VALUE(SEX,'3') AS SEX ");
            stb.append(       ",VALUE(LOCATIONCD,'3') AS LOCATIONCD ");
            stb.append(       ",VALUE(NATPUB,'6') AS NATPUB ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    BASE_A T1 ");
            stb.append("WHERE   T1.ENTDIV IN('2') ");  //入学者数
            stb.append("GROUP BY GROUPING SETS( (SEX,LOCATIONCD,NATPUB),(SEX,LOCATIONCD),(LOCATIONCD,NATPUB),(LOCATIONCD),(SEX),() ) ");

            stb.append("ORDER BY PDIV,SEX,LOCATIONCD,NATPUB ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


}
