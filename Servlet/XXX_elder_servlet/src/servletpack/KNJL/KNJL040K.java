// kanji=漢字
/*
 * $Id: 4c4b7303ed3af8a18a14648db92fc331f02f5bc2 $
 *
 * 作成日: 2006/01/15 11:25:40 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 *  学校教育システム 賢者 [入試管理] 高校入学試験志願者受付累計表
 *
 *  2006/01/15 Build yamashiro
 *  2006/01/22 Modify yamasihro
 *    ○改ページ処理をやらない <= １０日分の表記とするため --NO001
 *    ○（コース・地域別）日計表および累計表においても女子の数をカッコ付きで出力する --NO002
 *    ○府下公立は国公立コード＝５だけとする  --NO003
 *
 *  2006/01/31 NO004 m-yama    出力データがある日付のみ出力する。
 */
public class KNJL040K
{
    private static final Log log = LogFactory.getLog(KNJL040K.class);

    private KNJEditString knjobj = new KNJEditString();
    private Map desiredivmap;                               //志望区分表示用Map


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
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap )
    {
        try {
            paramap.put( "YEAR",       request.getParameter("YEAR")      );  //年度
            paramap.put( "SPECIAL_REASON_DIV", request.getParameter("SPECIAL_REASON_DIV"));
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        } finally {
log.debug("paramap="+paramap);
        }
    }


    /**
     *  印刷処理
     */
    private boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        CourseStatistics_Base areaobj = null;

        try {
            setHeadItem( paramap );

            //コース別
            paramap.put( "SUBTITLE", new String("（コース別）") );
            areaobj = new CourseStatistics();
            areaobj.svfprintSub( db2, svf, paramap );

            //コース・地域別(日計表)
            if( paramap.containsKey("SUBTITLE") )paramap.remove("SUBTITLE");
            paramap.put( "SUBTITLE", new String("（コース・地域別）") );
            paramap.put( "SUBTITLE2", new String("１（日計表）") );
            areaobj = new CourseAreaStatistics_1();
            areaobj.svfprintSub( db2, svf, paramap );

            //コース・地域別(累計表)
            if( paramap.containsKey("SUBTITLE2") )paramap.remove("SUBTITLE2");
            paramap.put( "SUBTITLE2", new String("２（累計表）") );
            areaobj = new CourseAreaStatistics_2();
            areaobj.svfprintSub( db2, svf, paramap );

            nonedata = true;
        } catch( Exception ex ){
            log.error("printSvf error! ",ex);
        }
        return nonedata;
    }


    /**
     *  帳票見出し項目設定
     */
    private void setHeadItem( Map paramap )
    {
        String str = null;
        try {
            if( ! paramap.containsKey("NENDO") ){
                str = nao_package.KenjaProperties.gengou( Integer.parseInt( (String)paramap.get("YEAR") ) ) + "年度";
                paramap.put( "NENDO",  str );                                   //年度
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
            log.error("setHeadItem error! ",ex);
        }
    }



/*************************************************
 *  印刷処理の内部クラス 基本
 *************************************************/
private abstract class CourseStatistics_Base
{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");


    abstract void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap );


    /**
     *  SVF-FORMカッコ()編集
     */
    String setFormatValue( String str )
    {
        String retstr = null;
        try {
            retstr = str;  //NO002 文字位置揃えのため、()はフォームで対応
        } catch( Exception ex ){
            log.error("setFormatValue error! ",ex);
        } finally{
            if( retstr == null )retstr = "(  )";
        }
        return retstr;
    }


    /**
     *  日付編集 月日
     */
    String setFormatDate( Calendar cal )
    {
        String retstr = null;
        try {
            retstr = sdf2.format( cal.getTime() );
        } catch( Exception ex ){
            log.error("setFormatDate error! ",ex);
        } finally{
            if( retstr == null )retstr = "  /  ";
        }
        return retstr;
    }


    /**
     *  日付編集 曜日
     */
    String setFormatWeek( Calendar cal )
    {
        String retstr = null;
        try {
            retstr =  ( ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.SUNDAY    )? "(日)"
                      : ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.MONDAY    )? "(月)"
                      : ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.TUESDAY   )? "(火)"
                      : ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.WEDNESDAY )? "(水)"
                      : ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.THURSDAY  )? "(木)"
                      : ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.FRIDAY    )? "(金)"
                      : ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.SATURDAY )?  "(土)": "(  )" );
        } catch( Exception ex ){
            log.error("setFormatWeek error! ",ex);
        } finally{
            if( retstr == null )retstr = "(  )";
        }
        return retstr;
    }


    /**
     *  基本　帳票ヘッダー等印刷
     */
    void printsvfOutHead( Vrw32alp svf, Map paramap )
    {
        try {
            svf.VrsOut( "NENDO",     (String)paramap.get("NENDO") );
            svf.VrsOut( "DATE",      (String)paramap.get("NOW_DATE") );
            svf.VrsOut( "SUBTITLE",  (String)paramap.get("SUBTITLE") );
            svf.VrsOut( "TABLE",     (String)paramap.get("SUBTITLE2") );
            printsvfOutHead2( svf );
        } catch( Exception ex ){
            log.error("printsvfOutHead error! ",ex);
        }

    }

    abstract void printsvfOutHead2( Vrw32alp svf );


    /**
     *  基本　ＳＱＬ作成
     */
    String preStatSub( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ");
            //志望コース(第１志望)の表
            stb.append("COURSE AS( ");
            stb.append(   "SELECT  T1.DESIREDIV, T2.EXAMCOURSE_MARK  ");
            stb.append(          ",T2.COURSECD,T2.MAJORCD,T2.EXAMCOURSECD  ");
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2  ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = ?  ");
            stb.append(           "AND T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD  ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD  ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD  ");
            stb.append(           "AND T1.WISHNO = '1'  ");
            stb.append(   "ORDER BY T1.DESIREDIV, T1.WISHNO  ");
            stb.append(")  ");

            //対象者の表
            stb.append(",BASE_A AS(");
            stb.append(   "SELECT  RECEPT_DATE, SEX, SHDIV, EXAMCOURSE_MARK ");
            stb.append(          ",CASE WHEN VALUE(NATPUBPRIDIV,'0') IN('5') THEN '1' ELSE '2' END AS NATDIV ");  //NO003
            stb.append(   "FROM    ENTEXAM_NEWSFLASH_DAT T1 ");
            stb.append(   "INNER JOIN COURSE T2 ON T2.DESIREDIV = T1.DESIREDIV ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = ? ");
            stb.append(")  ");

            stb.append( preStatSubSelect( paramap ) );   //メイン表

        } catch( Exception e ){
            log.error( "preStatSub error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }

    abstract String preStatSubSelect( Map paramap );
    
    public void setInfluenceName(DB2UDB db2, Map paramap, Vrw32alp svf) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            String sql = "SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + paramap.get("SPECIAL_REASON_DIV") + "' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                log.debug(" name2 = " + name2);
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


/*************************************************
 *  コース別集計表(日計・累計)
 *************************************************/
private class CourseStatistics extends CourseStatistics_Base
{
    int arraccumuthis[][] = { {0,0},{0,0},{0,0},{0,0},{0,0},{0,0} };  //今年度累積用
    int arraccumulast[][] = { {0,0},{0,0} };                          //昨年度累積用

    /**
     *  コース別集計表(日計・累計)　帳票ヘッダー等印刷
     */
    void printsvfOutHead2( Vrw32alp svf )
    {
        try {
        } catch( Exception ex ){
            log.error("rintsvfOutHead2 error! ",ex);
        }
    }


    /**
     *  コース別集計表(日計・累計)　印刷処理
     */
    void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        PreparedStatement ps1 = null;  //今年度
        PreparedStatement ps2 = null;  //昨年度
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            svf.VrSetForm( "KNJL040_1.frm", 1 );
            setInfluenceName(db2, paramap, svf);
            ps1 = db2.prepareStatement( preStatSub( paramap ) );
            int p = 0;
            ps1.setString( ++p, (String)paramap.get("YEAR") );
            ps1.setString( ++p, (String)paramap.get("YEAR") );
            rs1 = ps1.executeQuery();    //今年度
            ps2 = db2.prepareStatement( preStatSub( paramap ) );
            int y = Integer.parseInt( (String)paramap.get("YEAR") ) - 1;
            p = 0;
            ps2.setString( ++p, String.valueOf( y ) );
            ps2.setString( ++p, String.valueOf( y ) );
            rs2 = ps2.executeQuery();    //昨年度

            svfprintSubDetail2( svf, rs1, rs2,  paramap );  //明細印刷  NO004

        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        } finally{
            try {
                db2.commit();
                if( rs1 != null )rs1.close();
                if( rs2 != null )rs2.close();
                if( ps1 != null )ps1.close();
                if( ps2 != null )ps2.close();
            } catch( SQLException ex ){
                log.error("svfprintSub error! ",ex);
            }
        }
    }


    /**
     *  コース別集計表(日計・累計)　明細印刷
     */
    void svfprintSubDetail( Vrw32alp svf, ResultSet rs1, ResultSet rs2, Map paramap )
    {
        int counter = 0;   //出力日付カウンター
        Calendar cals1 = Calendar.getInstance();  //今年度
        Calendar cale1 = Calendar.getInstance();
        boolean read1 = false;
        Calendar cals2 = Calendar.getInstance();  //昨年度
        Calendar cale2 = Calendar.getInstance();
        boolean read2 = false;

        try {
            for( int i = 0; i < 10; i++ ){              //NO001
                if( ! read1  ){   //今年度の最初のレコードの読込における処理
                    if( rs1.next() )read1 = true;
                    if( read1 )cals1.setTime( sdf.parse( rs1.getString("RECEPT_DATE") ) );
                    if( read1 )cale1.setTime( sdf.parse( rs1.getString("RECEPT_DATE") ) );
                }
                if( ! read2  ){   //昨年度の最初のレコードの読込における処理
                    if( rs2.next() )read2 = true;
                    if( read2 )cals2.setTime( sdf.parse( rs2.getString("RECEPT_DATE") ) );
                    if( read2 )cale2.setTime( sdf.parse( rs2.getString("RECEPT_DATE") ) );
                }
                if( counter == 14 ){   //1ページ当り14日まで出力
                    svf.VrEndPage();
                    counter = 0;
                }
                if( counter == 0 )printsvfOutHead( svf, paramap );   //表題等を出力
                if( read1  ||  read2 )++counter;
                if( read1 ){   //今年度の出力および次レコードの読込
                    if( cals1.equals( cale1 ) ){
                        svfprintDetailOut( svf, rs1, counter, cals1 );
                        if( ! rs1.next() )read1 = false;
                        if( read1 )cale1.setTime( sdf.parse( rs1.getString("RECEPT_DATE") ) );
                    }else {   //値がない日を出力
                        svfprintDetailOutPass( svf, counter, cals1 );
                    }
                    cals1.add( Calendar.DATE, 1 );  //翌日をセット
                }
                if( read2 ){   //去年度の出力および次レコードの読込
                    if( cals2.equals( cale2 ) ){
                        svfprintDetailOutLast( svf, rs2, counter, cals2 );
                        if( ! rs2.next() )read2 = false;
                        if( read2 )cale2.setTime( sdf.parse( rs2.getString("RECEPT_DATE") ) );
                    }else {   //値がない日を出力
                        svfprintDetailOutPassLast( svf, counter, cals2 );
                    }
                    cals2.add( Calendar.DATE, 1 );  //翌日をセット
                }
                if( ! read1  &&  ! read2 )break;   //今年度と昨年度の次レコードが無い場合は処理を終了
            }
            svf.VrEndPage();
        } catch( Exception ex ) {
            log.error( "svfprintSubDetail error!", ex );
        }
    }


    /** NO004
     *  コース別集計表(日計・累計)　明細印刷
     */
    void svfprintSubDetail2( Vrw32alp svf, ResultSet rs1, ResultSet rs2, Map paramap )
    {
        int counter = 1;   //出力日付カウンター
        Calendar cals1 = Calendar.getInstance();  //今年度
        Calendar cals2 = Calendar.getInstance();  //昨年度

        try {
            int i = 0;
            while (rs1.next() && i < 10) {
                if( counter == 1 )printsvfOutHead( svf, paramap );
                cals1.setTime( sdf.parse( rs1.getString("RECEPT_DATE") ) );
                svfprintDetailOut( svf, rs1, counter, cals1 );
                counter++;
                i++;
            }

            counter = 1;
            i = 0;
            while (rs2.next() && i < 10) {
                if( counter == 1 )printsvfOutHead( svf, paramap );
                cals2.setTime( sdf.parse( rs2.getString("RECEPT_DATE") ) );
                svfprintDetailOutLast( svf, rs2, counter, cals2 );
                counter++;
                i++;
            }
            svf.VrEndPage();
        } catch( Exception ex ) {
            log.error( "svfprintSubDetail2 error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　今年度明細出力
     */
    void svfprintDetailOut( Vrw32alp svf, ResultSet rs, int counter, Calendar cals )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE1",  counter,   setFormatDate( cals ) );
            svf.VrsOutn( "WEEK1",         counter,   setFormatWeek( cals ) );
            //日計
            svf.VrsOutn( "COUNT1_1",  counter,  rs.getString("CNT_S") );
            svf.VrsOutn( "COUNT1_2",  counter,  rs.getString("CNT_SUBTOTAL") );
            svf.VrsOutn( "COUNT1_3",  counter,  rs.getString("CNT_K") );
            svf.VrsOutn( "COUNT1_4",  counter,  rs.getString("CNT_T") );
            svf.VrsOutn( "COUNT1_5",  counter,  rs.getString("CNT_P") );
            svf.VrsOutn( "TOTALCOUNT1_1",  counter,  rs.getString("CNT_TOTAL") );
            svf.VrsOutn( "GIRL1_1",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_S") ) );
            svf.VrsOutn( "GIRL1_2",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_SUBTOTAL") ) );
            svf.VrsOutn( "GIRL1_3",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_K") ) );
            svf.VrsOutn( "GIRL1_4",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_T") ) );
            svf.VrsOutn( "GIRL1_5",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_P") ) );
            svf.VrsOutn( "TOTALGIRL1_1",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_TOTAL") ) );

            //累計
            doAccumulation( rs );  //累積処理
            for( int j = 0; j < 5; j++ ){
                svf.VrsOutn( "COUNT2_" + (j+1),   counter,  String.valueOf( arraccumuthis[j][0] ) );
                svf.VrsOutn( "GIRL2_"  + (j+1),   counter,  setFormatValue( String.valueOf( arraccumuthis[j][1] ) ) );
            }
            svf.VrsOutn( "TOTALCOUNT2_1",   counter,  String.valueOf( arraccumuthis[5][0] ) );
            svf.VrsOutn( "TOTALGIRL2_1",    counter,  setFormatValue( String.valueOf( arraccumuthis[5][1] ) ) );
        } catch( Exception ex ) {
            log.error( "svfprintDetailOut error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　昨年度明細出力
     */
    void svfprintDetailOutLast( Vrw32alp svf, ResultSet rs, int counter, Calendar cals )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE2",  counter,   setFormatDate( cals ) );
            svf.VrsOutn( "WEEK2",         counter,   setFormatWeek( cals ) );
            //日計
            svf.VrsOutn( "TOTALCOUNT1_2",  counter,  rs.getString("CNT_TOTAL")    );
            svf.VrsOutn( "TOTALGIRL1_2",   counter,  setFormatValue( rs.getString("CNT_FEMAIL_TOTAL") ) );

            //累計
            doAccumulationLast( rs );  //累積処理
            svf.VrsOutn( "TOTALCOUNT2_2",  counter,  String.valueOf( arraccumulast[0][0] ) );
            svf.VrsOutn( "TOTALGIRL2_2",   counter,  setFormatValue( String.valueOf( arraccumulast[1][0] ) ) );
        } catch( Exception ex ) {
            log.error( "svfprintDetailOutLast error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　今年度累積処理
     */
    private void doAccumulation( ResultSet rs  )
    {
        int i = 0;
        int j = 0;
        try {
            arraccumuthis[   i ][ j ] += Integer.parseInt( rs.getString("CNT_S") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_SUBTOTAL") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_K") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_T") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_P") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_TOTAL") );
            i = 0;
            ++j;
            arraccumuthis[   i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_S") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_SUBTOTAL") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_K") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_T") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_P") );
            arraccumuthis[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_TOTAL") );

        } catch( Exception ex ) {
            log.error( "doAccumulation error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　昨年度累積処理
     */
    private void doAccumulationLast( ResultSet rs  )
    {
        int i = 0;
        int j = 0;
        try {
            arraccumulast[   i ][ j ] += Integer.parseInt( rs.getString("CNT_TOTAL") );
            arraccumulast[ ++i ][ j ] += Integer.parseInt( rs.getString("CNT_FEMAIL_TOTAL") );

        } catch( Exception ex ) {
            log.error( "doAccumulationLast error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　今年度明細出力   => 受付が1件も無い日の印刷処理
     */
    void svfprintDetailOutPass( Vrw32alp svf, int counter, Calendar cals  )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE1",  counter,  setFormatDate( cals ) );
            svf.VrsOutn( "WEEK1",         counter,  setFormatWeek( cals ) );
            //累計
            for( int j = 0; j < 5; j++ ){
                svf.VrsOutn( "COUNT2_" + (j+1),   counter,  String.valueOf( arraccumuthis[j][0] ) );
                svf.VrsOutn( "GIRL2_"  + (j+1),   counter,  setFormatValue( String.valueOf( arraccumuthis[j][1] ) ) );
            }
            svf.VrsOutn( "TOTALCOUNT2_1",   counter,  String.valueOf( arraccumuthis[5][0] ) );
            svf.VrsOutn( "TOTALGIRL2_1",    counter,  setFormatValue( String.valueOf( arraccumuthis[5][1] ) ) );
        } catch( Exception ex ) {
            log.error( "svfprintDetailOutPass error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　昨年度明細出力   => 受付が1件も無い日の印刷処理
     */
    void svfprintDetailOutPassLast( Vrw32alp svf, int counter, Calendar cals  )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE2",   counter,  setFormatDate( cals ) );
            svf.VrsOutn( "WEEK2",          counter,  setFormatWeek( cals ) );
            //累計
            svf.VrsOutn( "TOTALCOUNT2_2",  counter,  String.valueOf( arraccumulast[0][0] ) );
            svf.VrsOutn( "TOTALGIRL2_2",   counter,  setFormatValue( String.valueOf( arraccumulast[1][0] ) ) );
        } catch( Exception ex ) {
            log.error( "svfprintDetailOutPassLast error!", ex );
        }
    }


    /**
     *  コース別集計表(日計・累計)　ＳＱＬ作成
     */
    String preStatSubSelect( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  RECEPT_DATE ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'S' THEN 1 ELSE 0 END ) AS CNT_S ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'K' THEN 1 ELSE 0 END ) AS CNT_K ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'T' THEN 1 ELSE 0 END ) AS CNT_T ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'P' THEN 1 ELSE 0 END ) AS CNT_P ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'S' AND SEX = '2' THEN 1 ELSE 0 END ) AS CNT_FEMAIL_S ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'K' AND SEX = '2' THEN 1 ELSE 0 END ) AS CNT_FEMAIL_K ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'T' AND SEX = '2' THEN 1 ELSE 0 END ) AS CNT_FEMAIL_T ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'P' AND SEX = '2' THEN 1 ELSE 0 END ) AS CNT_FEMAIL_P ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK IN('K','T','P') THEN 1 ELSE 0 END ) AS CNT_SUBTOTAL ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK IN('K','T','P') AND SEX = '2' THEN 1 ELSE 0 END ) AS CNT_FEMAIL_SUBTOTAL ");
            stb.append(       ",COUNT(*) AS CNT_TOTAL ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' THEN 1 ELSE 0 END ) AS CNT_FEMAIL_TOTAL ");
            stb.append("FROM    BASE_A ");
            stb.append("GROUP BY RECEPT_DATE ");
            stb.append("ORDER BY RECEPT_DATE ");
        } catch( Exception e ){
            log.error( "preStatSubSelect error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}



/*************************************************
 *  コース別地域別集計表(日計)
 *************************************************/
private class CourseAreaStatistics_1 extends CourseStatistics_Base
{
    /**
     *  コース別地域別集計表(日計)　帳票ヘッダー等印刷
     */
    void printsvfOutHead2( Vrw32alp svf )
    {
        try {
        } catch( Exception ex ){
            log.error("printsvfOutHead2 error! ",ex);
        }
    }


    /**
     *  コース別地域別集計表(日計)　印刷処理
     */
    void svfprintSub( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

        try {
            svf.VrSetForm( "KNJL040_2.frm", 1 );
            setInfluenceName(db2, paramap, svf);
            ps1 = db2.prepareStatement( preStatSub( paramap ) );
            int p = 0;
            ps1.setString( ++p, (String)paramap.get("YEAR") );
            ps1.setString( ++p, (String)paramap.get("YEAR") );
            rs1 = ps1.executeQuery();
            svfprintSubDetail2( svf, rs1, paramap );    //NO004
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        } finally{
            try {
                db2.commit();
                if( rs1 != null )rs1.close();
                if( ps1 != null )ps1.close();
            } catch( SQLException ex ){
                log.error("svfprintSub error! ",ex);
            }
        }
    }


    /**
     *  コース別地域別集計表(日計)　明細印刷処理
     */
    void svfprintSubDetail( Vrw32alp svf, ResultSet rs, Map paramap )
    {
        Calendar cals = Calendar.getInstance();
        Calendar cale = Calendar.getInstance();
        int counter = 0;
        boolean read = false;

        try {
            for( int i = 0; i < 10; i++ ){                //NO001
                if( ! read  ){
                    if( rs.next() )read = true;
                    if( read )cals.setTime( sdf.parse( rs.getString("RECEPT_DATE") ) );
                    if( read )cale.setTime( sdf.parse( rs.getString("RECEPT_DATE") ) );
                }
                if( counter == 14 ){
                    svf.VrEndPage();
                    counter = 0;
                }
                if( counter == 0 )printsvfOutHead( svf, paramap );
                if( read ){
                    ++counter;
                    if( cals.equals( cale ) ){
                        svfprintDetailOut( svf, rs, counter, cals );
                        if( ! rs.next() )read = false;
                        if( read )cale.setTime( sdf.parse( rs.getString("RECEPT_DATE") ) );
                    }else {
                        svfprintDetailOutPass( svf, counter, cals );
                    }
                    cals.add( Calendar.DATE, 1 );  //翌日をセット
                }
                if( ! read )break;
            }
            svf.VrEndPage();
        } catch( Exception ex ) {
            log.error( "svfprintSubDetail error!", ex );
        }
    }


    /** NO004
     *  コース別地域別集計表(日計)　明細印刷処理
     */
    void svfprintSubDetail2( Vrw32alp svf, ResultSet rs, Map paramap )
    {
        Calendar cals = Calendar.getInstance();
        int counter = 1;

        try {
            int i = 0;
            while (rs.next() && i < 10) {
                if( counter == 1 )printsvfOutHead( svf, paramap );
                cals.setTime( sdf.parse( rs.getString("RECEPT_DATE") ) );
                svfprintDetailOut( svf, rs, counter, cals );
                counter++;
                i++;
            }
            svf.VrEndPage();
        } catch( Exception ex ) {
            log.error( "svfprintSubDetail2 error!", ex );
        }
    }


    /**
     *  コース別地域別集計表(日計)　SVF-FORM明細出力
     */
    void svfprintDetailOut( Vrw32alp svf, ResultSet rs, int counter, Calendar cals )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE",  counter,   setFormatDate( cals ) );
            svf.VrsOutn( "WEEK",         counter,   setFormatWeek( cals ) );
            //日計
            for( int i = 0; i < 8; i++ ){
                svf.VrsOutn( "TOTAL" + (i+1),         counter,  rs.getString("CNT" + (i+1) + "_1") );  //合計
                svf.VrsOutn( "COUNT" + (i+1) + "_1",  counter,  rs.getString("CNT" + (i+1) + "_2") );  //府下公立
                svf.VrsOutn( "COUNT" + (i+1) + "_2",  counter,  rs.getString("CNT" + (i+1) + "_3") );  //その他
                // NO002 ->
                svf.VrsOutn( "TOTALGIRL" + (i+1),     counter,  setFormatValue( rs.getString("FCNT" + (i+1) + "_1") ) );  //合計
                svf.VrsOutn( "GIRL"  + (i+1) + "_1",  counter,  setFormatValue( rs.getString("FCNT" + (i+1) + "_2") ) );  //府下公立
                svf.VrsOutn( "GIRL"  + (i+1) + "_2",  counter,  setFormatValue( rs.getString("FCNT" + (i+1) + "_3") ) );  //その他
                // <- NO002
            }
        } catch( Exception ex ) {
            log.error( "svfprintDetailOut error!", ex );
        }
    }


    /**
     *  コース別地域別集計表(日計)　SVF-FORM明細出力  => 受付が1件も無い日の印刷処理
     */
    void svfprintDetailOutPass( Vrw32alp svf, int counter, Calendar cals  )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE",  counter,  setFormatDate( cals ) );
            svf.VrsOutn( "WEEK",         counter,  setFormatWeek( cals ) );
        } catch( Exception ex ) {
            log.error( "svfprintDetailOutPass error!", ex );
        }
    }


    /**
     *  コース別地域別集計表(日計)　ＳＱＬ作成
     */
    String preStatSubSelect( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  RECEPT_DATE ");
            stb.append(       ",COUNT(*) AS CNT1_1 ");
            stb.append(       ",SUM( CASE WHEN NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT1_2 ");
            stb.append(       ",SUM( CASE WHEN NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT1_3 ");
            stb.append(       ",SUM( CASE WHEN SHDIV = '1' THEN 1 ELSE 0 END ) AS CNT2_1 ");
            stb.append(       ",SUM( CASE WHEN SHDIV = '1' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT2_2 ");
            stb.append(       ",SUM( CASE WHEN SHDIV = '1' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT2_3 ");
            stb.append(       ",SUM( CASE WHEN SHDIV = '2' THEN 1 ELSE 0 END ) AS CNT3_1 ");
            stb.append(       ",SUM( CASE WHEN SHDIV = '2' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT3_2 ");
            stb.append(       ",SUM( CASE WHEN SHDIV = '2' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT3_3 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'S' THEN 1 ELSE 0 END ) AS CNT4_1 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'S' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT4_2 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'S' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT4_3 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK IN('K','T','P') THEN 1 ELSE 0 END ) AS CNT5_1 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK IN('K','T','P') AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT5_2 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK IN('K','T','P') AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT5_3 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'K' THEN 1 ELSE 0 END ) AS CNT6_1 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'K' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT6_2 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'K' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT6_3 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'T' THEN 1 ELSE 0 END ) AS CNT7_1 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'T' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT7_2 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'T' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT7_3 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'P' THEN 1 ELSE 0 END ) AS CNT8_1 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'P' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS CNT8_2 ");
            stb.append(       ",SUM( CASE WHEN EXAMCOURSE_MARK = 'P' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS CNT8_3 ");
            // NO002 ->
            stb.append(       ",SUM( CASE WHEN SEX = '2' THEN 1 ELSE 0 END ) AS FCNT1_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT1_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT1_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND SHDIV = '1' THEN 1 ELSE 0 END ) AS FCNT2_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND SHDIV = '1' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT2_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND SHDIV = '1' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT2_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND SHDIV = '2' THEN 1 ELSE 0 END ) AS FCNT3_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND SHDIV = '2' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT3_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND SHDIV = '2' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT3_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'S' THEN 1 ELSE 0 END ) AS FCNT4_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'S' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT4_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'S' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT4_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK IN('K','T','P') THEN 1 ELSE 0 END ) AS FCNT5_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK IN('K','T','P') AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT5_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK IN('K','T','P') AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT5_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'K' THEN 1 ELSE 0 END ) AS FCNT6_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'K' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT6_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'K' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT6_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'T' THEN 1 ELSE 0 END ) AS FCNT7_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'T' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT7_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'T' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT7_3 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'P' THEN 1 ELSE 0 END ) AS FCNT8_1 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'P' AND NATDIV = '1' THEN 1 ELSE 0 END ) AS FCNT8_2 ");
            stb.append(       ",SUM( CASE WHEN SEX = '2' AND EXAMCOURSE_MARK = 'P' AND NATDIV = '2' THEN 1 ELSE 0 END ) AS FCNT8_3 ");
            // <- NO002 
            stb.append("FROM    BASE_A ");
            stb.append("GROUP BY RECEPT_DATE ");
            stb.append("ORDER BY RECEPT_DATE ");
        } catch( Exception e ){
            log.error( "preStatSubSelect error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}



/*************************************************
 *  コース別地域別集計表(累計)
 *************************************************/
private class CourseAreaStatistics_2 extends CourseAreaStatistics_1
{
    private int arraccumu[][] = { {0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0} };  //累積用
    private int arraccumuf[][] = { {0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0} };  //女子累積用 NO002

    /**
     *  コース別地域別集計表(累計)　SVF-FORM明細出力
     */
    void svfprintDetailOut( Vrw32alp svf, ResultSet rs, int counter, Calendar cals )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE",  counter,   setFormatDate( cals ) );
            svf.VrsOutn( "WEEK",         counter,   setFormatWeek( cals ) );
            //累計
            doAccumulation( rs, arraccumu, arraccumuf );   //NO002 Modify
            for( int i = 0; i < 8; i++ ){
                svf.VrsOutn( "TOTAL" + (i+1),         counter,  String.valueOf( arraccumu[ i ][ 0 ] ) );  //合計
                svf.VrsOutn( "COUNT" + (i+1) + "_1",  counter,  String.valueOf( arraccumu[ i ][ 1 ] ) );  //府下公立
                svf.VrsOutn( "COUNT" + (i+1) + "_2",  counter,  String.valueOf( arraccumu[ i ][ 2 ] ) );  //その他
                // NO002 ->
                svf.VrsOutn( "TOTALGIRL" + (i+1),     counter,  setFormatValue( String.valueOf( arraccumuf[ i ][ 0 ] ) ) );  //合計
                svf.VrsOutn( "GIRL"  + (i+1) + "_1",  counter,  setFormatValue( String.valueOf( arraccumuf[ i ][ 1 ] ) ) );  //府下公立
                svf.VrsOutn( "GIRL"  + (i+1) + "_2",  counter,  setFormatValue( String.valueOf( arraccumuf[ i ][ 2 ] ) ) );  //その他
                // <- NO002
            }
        } catch( Exception ex ) {
            log.error( "svfprintDetailOut error!", ex );
        }
    }


    /**
     *  コース別地域別集計表(日計)　SVF-FORM明細出力  => 受付が1件も無い日の印刷処理
     */
    void svfprintDetailOutPass( Vrw32alp svf, int counter, Calendar cals  )
    {
        try {
            svf.VrsOutn( "RECEPT_DATE",  counter,  setFormatDate( cals ) );
            svf.VrsOutn( "WEEK",         counter,  setFormatWeek( cals ) );

            for( int i = 0; i < 8; i++ ){
                svf.VrsOutn( "TOTAL" + (i+1),         counter,  String.valueOf( arraccumu[ i ][ 0 ] ) );  //合計
                svf.VrsOutn( "COUNT" + (i+1) + "_1",  counter,  String.valueOf( arraccumu[ i ][ 1 ] ) );  //府下公立
                svf.VrsOutn( "COUNT" + (i+1) + "_2",  counter,  String.valueOf( arraccumu[ i ][ 2 ] ) );  //その他
                // NO002 ->
                svf.VrsOutn( "TOTALGIRL" + (i+1),     counter,  setFormatValue( String.valueOf( arraccumuf[ i ][ 0 ] ) ) );  //合計
                svf.VrsOutn( "GIRL"  + (i+1) + "_1",  counter,  setFormatValue( String.valueOf( arraccumuf[ i ][ 1 ] ) ) );  //府下公立
                svf.VrsOutn( "GIRL"  + (i+1) + "_2",  counter,  setFormatValue( String.valueOf( arraccumuf[ i ][ 2 ] ) ) );  //その他
                // <- NO002
            }
        } catch( Exception ex ) {
            log.error( "svfprintDetailOutPass error!", ex );
        }
    }


    /**
     *  コース別集計表(日計)　累積処理
     */
    private void doAccumulation( ResultSet rs, int arraccumu[][], int arraccumuf[][] )  //NO002 Modify
    {
        try {
            for( int i = 0; i < 8; i++ ){
                arraccumu[ i ][ 0 ] += Integer.parseInt( rs.getString("CNT" + (i+1) + "_1") );  //合計
                arraccumu[ i ][ 1 ] += Integer.parseInt( rs.getString("CNT" + (i+1) + "_2") );  //府下公立
                arraccumu[ i ][ 2 ] += Integer.parseInt( rs.getString("CNT" + (i+1) + "_3") );  //その他
                // NO002 ->
                arraccumuf[ i ][ 0 ] += Integer.parseInt( rs.getString("FCNT" + (i+1) + "_1") );  //合計
                arraccumuf[ i ][ 1 ] += Integer.parseInt( rs.getString("FCNT" + (i+1) + "_2") );  //府下公立
                arraccumuf[ i ][ 2 ] += Integer.parseInt( rs.getString("FCNT" + (i+1) + "_3") );  //その他
                // <- NO002
            }
        } catch( Exception ex ) {
            log.error( "doAccumulation error!", ex );
        }
    }
}

}
