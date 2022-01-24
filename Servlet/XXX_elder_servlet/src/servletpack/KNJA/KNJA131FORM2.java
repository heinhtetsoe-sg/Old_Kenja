// kanji=漢字
/*
 * $Id: e24950d14c03407543b40933f364565ba2faf130 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSvfFieldModify;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中等教育学校用（千代田区立九段）
 *                                                    後期課程　各教科・科目の学習の記録
 *
 *  2005/12/27 Build yamashiro
 *  2006/04/13 yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001
 *                      ・データが無い場合もフォームを出力する --NO001
 */

public class KNJA131FORM2 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131FORM2.class);

    private StringBuffer stb;
    private ResultSet rs;
    private KNJSvfFieldModify svfobj;   //フォームのフィールド属性変更


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
            printHeader( db2, svf, paramap );  //年次・ホームルーム・整理番号
            printSvfDetail_1( db2, svf, paramap );
            ret = svf.VrPrint();
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
            ret = svf.VrSetForm("KNJA131_2.frm", 4);
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  学習記録データ
     */
    void printSvfDetail_1( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );   //学籍番号
            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );   //年度
            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setString( ++p, (String)paramap.get("SCHNO")  );   //学籍番号
            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );    //年度
//            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );   //学籍番号
            ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).setInt( ++p, Integer.parseInt ((String) paramap.get ("YEAR")));  // 年度
            rs = ( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD") ).executeQuery();
            
            final String useCurriculumcd = (String) paramap.get("useCurriculumcd");

            int ad_credit = 0;              //加算単位数
            String s_subclasscd = "0";      //科目コード
            String s_classcd = "0";         //教科コード
            String s_schoolKind = "0";      //学校種別
            String s_curriculumCd = "0";    //教育課程コード
            int linex = 0;                  //行数
            List list_classname = new LinkedList();     //教科名のリスト
            List list_subclassname = new LinkedList();  //科目名のリスト
            List list_value = new LinkedList();         //単位数のリスト

            while ( rs.next() ){
                ad_credit += printSvfnotStudy( svf, rs, paramap );  // 総合的な学習の時間・留学
                if( rs.getString("ANNUAL").equals("0") )continue;
                //科目コードのブレイク
                final boolean isDifferentSubclasscd;
                if ("1".equals(useCurriculumcd)) {
                    isDifferentSubclasscd = !(s_classcd.equals( rs.getString("CLASSCD") ) && s_schoolKind.equals(rs.getString("SCHOOL_KIND")) && s_curriculumCd.equals(rs.getString("CURRICULUM_CD")) && s_subclasscd.equals( rs.getString("SUBCLASSCD") ));
                } else {
                    isDifferentSubclasscd = !s_subclasscd.equals( rs.getString("SUBCLASSCD") );
                }
                if( isDifferentSubclasscd ){
                    //教科コードのブレイクで該当教科を出力( prinvSvfOutdetail() )
                    final boolean isDifferentClasscd;
                    if ("1".equals(useCurriculumcd)) {
                        isDifferentClasscd = !(s_classcd.equals( rs.getString("CLASSCD") ) && s_schoolKind.equals(rs.getString("SCHOOL_KIND")) && s_curriculumCd.equals(rs.getString("CURRICULUM_CD")));
                    } else {
                        isDifferentClasscd = !s_classcd.equals( rs.getString("CLASSCD") );
                    }
                    if( isDifferentClasscd ){
                        if( !s_classcd.equals("0") )
                            linex = prinvSvfOutdetail( svf, s_classcd, list_classname, list_subclassname, linex, list_value );
                        if( rs.getString("CLASSNAME") != null ) {
                            for( int i = 0 ; i < ( rs.getString("CLASSNAME") ).length() ; i++ ) {
                                list_classname.add( rs.getString("CLASSNAME").substring(i, i+1) );  //教科名をLISTへセット
                            }
                        }
                        s_classcd = rs.getString("CLASSCD");                                        //教科コードの保存
                        if ("1".equals(useCurriculumcd)) {
                            s_schoolKind = rs.getString("SCHOOL_KIND");
                            s_curriculumCd = rs.getString("CURRICULUM_CD");
                        }
                    }
                    list_subclassname.add( rs.getString("SUBCLASSNAME") );      //科目名をLISTへセット
                    s_subclasscd = rs.getString("SUBCLASSCD");                  //科目コードの保存
                    list_value.add( null );                                     //単位数をLISTへ追加
                }
                if( rs.getString("CREDIT") != null )  //--NO001 not null 条件を追加
                    list_value.set( list_value.size() - 1, new Integer(rs.getInt("CREDIT")) );  //単位数をLISTへセット
            }
            if( 0 < ad_credit ) { //--NO001 '0'は出力しない
                // ret = svf.VrsOut( "TOTAL_CREDIT",  String.valueOf( ad_credit ) );  //総合修得単位数
            }
            if( !s_classcd.equals("0") )
                linex = prinvSvfOutdetail( svf, s_classcd, list_classname, list_subclassname, linex, list_value );
//log.debug("linex="+linex);
            for( int i = linex; i < 84; i++ ){
                    ret = svf.VrsOut( "CLASSCD",  "" );  //教科コード
                    ret = svf.VrEndRecord();  //NO001
            }
        } catch( Exception ex ){
            log.debug( "printSvfDetail_1 error!", ex );
        }
    }


    /**
     *  総合的な学習の時間・留学・小計を出力
     *    return : 単位数を返す
     *    2004/11/29
     */
    int printSvfnotStudy( Vrw32alp svf, ResultSet rs, Map paramap )
    {
        int credits = 0;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( rs.getString("CREDIT") != null  &&  rs.getString("CLASSNAME") != null ){
                if( rs.getString("CLASSNAME").equals("sogo") ){
                    // ret = svf.VrsOut( "TOTAL_ACT",  rs.getString("CREDIT") );   //総合的な学習の時間
                    credits = rs.getInt("CREDIT");                              //加算単位数
                } else if( rs.getString("CLASSNAME").equals("abroad") ){
                    // ret = svf.VrsOut( "ABROAD",  rs.getString("CREDIT") );      //留学
                    credits = rs.getInt("CREDIT");                              //加算単位数
                } else if( rs.getString("CLASSNAME").equals("total") )
                    credits = rs.getInt("CREDIT");                              //加算単位数
            }
//log.debug("classname="+rs.getString("CLASSNAME") + "   credit="+rs.getString("CREDIT")+"   credits="+credits);
        } catch( Exception ex ){
            log.debug( "printSvfnotStudy error!", ex );
        }
        return credits;
    }



    /** 
     *  教科名・科目名・修得単位数を出力  教科ごとの処理
     *    return : linex => 行数
     */
    public int prinvSvfOutdetail( Vrw32alp svf
                                 ,String classcd
                                 ,List list_classname
                                 ,List list_subclassname
                                 ,int linex
                                 ,List list_value
                                )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            //教科名文字数と科目数で多い方を教科の行数にする
            int nameline = ( list_subclassname.size() <= list_classname.size() )?
                                                         list_classname.size() : list_subclassname.size();
            //教科間の科目が続く場合は、空行を出力する [[最終行の扱い次第では代替処理その2を使用]]
            if( list_subclassname.size() == nameline )
                nameline++;

            //教科が次列に跨らないために、空行を出力する
            if( ( linex < 30  &&  30 < linex + nameline ) ||
                ( 30 <= linex  &&  linex < 60  &&  60 < linex + nameline ) ||
                ( 60 <= linex  &&  linex < 84  &&  84 < linex + nameline ) ){
                int k = ( linex < 30 )? 30 : ( linex < 60 )? 60 : 84;
                for( int j = linex ; j < k ; j++ ){
                    ret = svf.VrEndRecord();
                    linex++;
                }
            }
            //列の最終行以外で、教科間の科目が続いている場合は、空行を出力する [[代替処理その2]]
//          if( list_subclassname.size() == nameline )
//              if( linex + nameline != 30  &&  linex + nameline != 60  &&  linex + nameline != 84 )
//                      nameline++;
            //教科の名称、科目名、修得単位数を出力する
            for( int i = 0; i < nameline ; i++ ){
                if( i < list_classname.size() )
                    ret = svf.VrsOut( "CLASS1", (String)list_classname.get(i) );  //教科名
                if( i < list_subclassname.size() ){
                    svfFieldAttribute_SUBCLASS( svf, (String)list_subclassname.get(i), linex + 1 );  //05/12/22 Modify
                    ret = svf.VrsOut( "SUBCLASS", (String)list_subclassname.get(i) );  //科目名
//log.debug("subclass="+(String)list_subclassname.get(i)+"  linex="+linex);
                }
                if( i < list_value.size()  &&  (list_value.get(i)) != null ) {
                    // ret = svf.VrsOut("CREDIT1",  ( (Integer)list_value.get(i) ).toString() );  //修得単位数
                }
                ret = svf.VrsOut( "CLASSCD",  classcd );  //教科コード
                ret = svf.VrEndRecord();
                linex++;
            }
        } catch( Exception ex ){
            log.debug( "prinvSvfOutdetail error! ", ex );
        }

        //教科名、科目名、修得単位数のリストを削除する
        try {
            if( list_classname    != null )list_classname.clear();
            if( list_subclassname != null )list_subclassname.clear();
            if( list_value        != null )list_value.clear();
            if( linex == 101 )linex=0;
        } catch( Exception ex ){
            log.debug( "prinvSvfOutdetail error! ", ex );
        }
        return linex;
    }


    /**
     *  PrepareStatementオブジェクト作成
     */
    public void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            if( paramap.containsKey("PS_CREDITS_RECORD") )return;
            //成績データ
            if( ! paramap.containsKey("PS_CREDITS_RECORD") ){
//                KNJ_StudyrecSql obj = new KNJ_StudyrecSql();
                StudyrecSql obj = new StudyrecSql("hyde","hyde",1,false,definecode,0,1, (String) paramap.get("useCurriculumcd"));
                paramap.put("PS_CREDITS_RECORD",  db2.prepareStatement( obj.pre_sql() ) );
                log.debug(" osj presql = " + obj.pre_sql());

                obj = null;
            }
            prepareSqlStateSub(db2, paramap);
        } catch( Exception ex ){
            log.debug("prepareSqlState", ex );
        }
    }


    /**
     *  PrepareStatement close
     */
    public void closePrepareState( DB2UDB db2, Map paramap )
    {
        try {
            if( ! paramap.containsKey("PS_CREDITS_RECORD")     )( ( PreparedStatement )paramap.get("PS_CREDITS_RECORD")     ).close();
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }


    /**
     * ＳＶＦ−ＦＯＲＭフィールド属性変更(RECORD) => 文字数により文字ピッチ及びＹ軸を変更する
     */
    private void svfFieldAttribute_SUBCLASS( Vrw32alp svf, String name, int ln )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( svfobj == null )svfobj = new KNJSvfFieldModify();
            svfobj.width = 552;     //フィールドの幅(ドット)
            svfobj.height = 120;    //フィールドの高さ(ドット)
            svfobj.ystart = 511;    //開始位置(ドット)
            svfobj.minnum = 20;     //最小設定文字数
            svfobj.maxnum = 40;     //最大設定文字数
            svfobj.setRetvalue( name, ( ln % 30 == 0 )? 30: ln % 30 );

            if( ln <= 30 )ret = svf.VrAttribute("SUBCLASS" , "X="+ ( 276 + 21 ) );       //左列の開始Ｘ軸
            else if( ln <= 60 )ret = svf.VrAttribute("SUBCLASS" , "X="+ ( 1306 + 21 ) ); //中列の開始Ｘ軸
            else ret = svf.VrAttribute("SUBCLASS" , "X="+ ( 2336 + 21 ) );               //右列の開始Ｘ軸

            ret = svf.VrAttribute("SUBCLASS" , "Y="+ svfobj.jiku );             //開始Ｙ軸
            ret = svf.VrAttribute("SUBCLASS" , "Size=" + svfobj.size );         //文字サイズ
            ret = svf.VrsOut("SUBCLASS",  name );
        } catch( Exception e ){
            log.error("svf.VrAttribute error! ", e);
        }
    }


}
