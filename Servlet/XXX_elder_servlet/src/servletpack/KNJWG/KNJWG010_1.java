// kanji=漢字
/*
 * $Id: dd43e65017b52051b359dd173e86414d834ca8d8 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 *
 *          在学証明書(日本語・英語）
 *          卒業見込証明書
 *          卒業証明書(日本語・英語）
 *
 *  2005/04/06 yamashiro 在学証明書(高校用)
 *  2005/04/12 yamashiro 在学証明書(高校用)
 *  2005/04/14 yamashiro 中学用在学証明書を追加
 *  2005/05/12 yamashiro 在学証明書(中学用)
 *  2005/07/12 yamashiro 卒業見込証明書において、氏名は文字数により文字の大きさを変えて出力する
 *  2005/07/21 yamashiro 標準版と近大付属版の異なる出力様式に対応 => 在学証明書(高校用)/卒業見込証明書/卒業証明書
 *  2005/08/04 yamashiro 近大付属版卒業見込証明書の出力仕様変更
 *  2005/08/31 yamashiro 近大付属版卒業証明書・卒業見込証明書・在学証明書の校長名出力仕様変更
 *                       近大付属中学用卒業証明書・卒業見込証明書・在学証明書を追加
 *  2005/10/05 yamashiro 卒業証明書において、学校名を現在年度で取得
 *  2005/11/18〜11/22 yamashiro 「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *                       学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

 /*
  *  2005/07/21 Modify KNJGCertificateの継承を追加 => インスタンスKNJDefineCodeの読み込みおよびインスタンス変数definecodeの設定
  */

public class KNJWG010_1 {

    private static final Log log = LogFactory.getLog(KNJWG010_1.class);
    public Vrw32alp svf = new Vrw32alp();       //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    public DB2UDB db2;                          //Databaseクラスを継承したクラス
    public boolean nonedata;
    protected PreparedStatement ps6,ps7;
    private KNJEditString knjobj = new KNJEditString();     //各学校における定数等設定 05/07/12 Build
    private KNJWG010_1.certifCommon outobj;                  //各出力様式のクラス 05/07/21 Build 
    final KNJDefineSchool _definecode;  // 各学校における定数等設定

    public KNJWG010_1(){
        _definecode = new KNJDefineSchool();  // 各学校における定数等設定

    }

    public KNJWG010_1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        this.db2 = db2;
        this.svf = svf;
        nonedata = false;
        _definecode = definecode;
    }

    /**
     *  PrepareStatement作成
     *  2005/07/21 Modify 'オブジェクトがNULLの場合作成する'条件を追加
     */
    public void pre_stat(String ptype)
    {
        try {
            //個人データ
            if( ps6 == null ){
                KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
                ps6 = db2.prepareStatement( obj_Personalinfo.sql_info_reg("11110011") );
            }
            //学校データ
            if (ps7 == null) {
                if (_definecode.schoolmark.equals("KIN") || _definecode.schoolmark.equals("KINJUNIOR")) {
                    servletpack.KNJZ.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
                    obj_SchoolinfoSql = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("12100");
                    ps7 = db2.prepareStatement( obj_SchoolinfoSql.pre_sql() );
                } else {
                    servletpack.KNJWG.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
                    obj_SchoolinfoSql = new servletpack.KNJWG.detail.KNJ_SchoolinfoSql("12100");
                    ps7 = db2.prepareStatement( obj_SchoolinfoSql.pre_sql() );
                }
            }
        } catch( Exception e ){
            log.error("pre_stat error! " + e );
        }
        if (log.isDebugEnabled()) log.debug("USE KNJWG010_1");
    }


    /**
     *  PrepareStatement close
     */
    public void pre_stat_f()
    {
        try {
            if( ps6 != null ) ps6.close();
            if( ps7 != null ) ps7.close();
        } catch( Exception e ){
            log.error("pre_stat_f error! " + e );
        }
    }


    /**
     *  過卒生対応年度取得
     */
    public static String b_year( String pdate )
    {
        String b_year = null;

        try {
            if( pdate != null ){
                b_year = pdate.substring( 0, 4 );
                String b_month = pdate.substring( 5, 7 );
                if( b_month.equals("01")  ||  b_month.equals("02")  ||  b_month.equals("03") )
                    b_year = String.valueOf( Integer.parseInt( b_year ) - 1 );
            }
        } catch( Exception e ){
            log.error("b_year error! " + e );
        }
        if( b_year == null ) b_year = new String();
        return b_year;
    }


    /** 
     *  在学証明書(英語) 
     */
    public void certif5_out(String schregno,String year,String year2,String semester,String date,String number)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int pp = 0;

        try {
            ps7.setString( ++pp, year2);  //05/11/18 Mdify
            // 05/11/18 Delete ps7.setString(1,year);
            ps7.setString( ++pp, year2);
            ps7.setString( ++pp, "00000000");
            ps7.setString( ++pp, year);  //05/11/18 Build
//log.debug("ps7="+ps7.toString());
            ResultSet rs = ps7.executeQuery();
            setCertifStyle( year, 5 );      //様式別出力クラスの読み込み
            outobj.setCertifForm( 5 );

            if( rs.next() ){
                ret = svf.VrsOut("DATE", ( date != null )? KNJ_EditDate.h_format_US(date): "" ); //証明日付 05/11/18 Modify
                //05/11/18Delete ret = svf.VrsOut("DATE"                , KNJ_EditDate.h_format_US(date));      //証明日付
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME_ENG"));     //学校名称
                ret = svf.VrsOut("STAFFNAME", rs.getString("PRINCIPAL_NAME_ENG"));  //代表名
                String address = rs.getString("SCHOOLADDR1_ENG");
                if( rs.getString("SCHOOLADDR2_ENG")!=null ) address += rs.getString("SCHOOLADDR2_ENG");
                ret = svf.VrsOut("SCHOOL_ADDRESS1", address);                               //学校住所
                ret = svf.VrsOut("PHONE", rs.getString("SCHOOLTELNO"));         //学校電話番号
            }
            if( rs != null )rs.close();
            db2.commit();

            ps6.setString(1,schregno);
            ps6.setString(2,year);
            ps6.setString(3,semester);
            ps6.setString(4,schregno);
            ps6.setString(5,year);
//log.debug("ps6="+ps6.toString());
            rs = ps6.executeQuery();

            if( rs.next() ){
                ret = svf.VrsOut("NUMBER"   , number);                                              //証明書番号
                ret = svf.VrsOut("GRADE"    , String.valueOf(rs.getInt("ANNUAL")));                 //年次
                ret = svf.VrsOut("NAME"     , rs.getString("NAME_ENG"));                            //氏名
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_US(rs.getString("BIRTHDAY")));  //生年月日
                ret = svf.VrsOut("SEX"      , rs.getString("SEX_ENG"));                             //性別
                ret = svf.VrEndPage();
                nonedata = true;
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("certif5_out error! " + ex);
        }

    }  //certif5_outの括り


    /** 
     *  在学証明書(日本語) 
     *  2005/04/06 Modify レイアウト変更に因る
     *  2005/04/12 Modify 氏名出力仕様変更に因る
     *  2005/07/21 Modify 標準版と近大付属版の異なる出力様式に対応
     */
    public void certif4_out(String schregno,
                            String year,
                            String year2,
                            String semester,
                            String date,
                            String number)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int pp = 0;
        ResultSet rs = null;
        String schooldivname = null;
        setCertifStyle( year, 4 );      //様式別出力クラスの読み込み
        outobj.setCertifForm( 4 );

        try {
            ps7.setString( ++pp, year2);  //05/11/18 Mdify
            // 05/11/18 Delete ps7.setString(1,year);
            ps7.setString( ++pp, year2);
            ps7.setString( ++pp, "00000000");
            ps7.setString( ++pp, year);  //05/11/18 Build
            rs = ps7.executeQuery();

            if( rs.next() ){
                outobj.outRegistSchoolinfo( rs, date );
                if( rs.getString("T4SCHOOLDIV").equals("0") )schooldivname = "学年";  //05/11/18Modify
                else                                         schooldivname = "年次";
            }
            if( schooldivname == null )schooldivname = "";    //05/04/14
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }

        try {
            pp = 0;
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year     );
            ps6.setString( ++pp, semester );
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year );
            rs = ps6.executeQuery();

            if( rs.next() ){
                outobj.outRegistStudentinfo( rs, number, schooldivname );
                ret = svf.VrEndPage();
                nonedata = true;
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }
    }


    /** 
     *  卒業見込証明書 
     *  2005/07/12 Modify 氏名を文字数により文字の大きさを変えて出力する処理を追加
     *                    nullデータの処理を追加
     *  2005/07/21 Modify 標準版と近大付属版の異なる出力様式に対応
     *  2005/08/04 Modify 課程・学科名を出力 => 卒業証明書と同様とする
     *  2005/08/31 Modify 近大付属版の校長名等の出力様式を変更 卒業証明書と同様とする
     **/
    public void certif3_out( String schregno,
                             String year,
                             String year2,
                             String semester,
                             String date,
                             String number)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int pp = 0;
        ResultSet rs = null;
        setCertifStyle( year, 3 );      //様式別出力クラスの読み込み
        outobj.setCertifForm( 3 );

        try {
            ps7.setString( ++pp, year2);  //05/11/18 Mdify
            // 05/11/18 Delete ps7.setString(1,year);
            ps7.setString( ++pp, year2);
            ps7.setString( ++pp, "00000000");
            ps7.setString( ++pp, year);  //05/11/18 Build
            rs = ps7.executeQuery();
            if( rs.next() )outobj.outGraduateSchoolinfo( rs, date );   //05/08/31Build
            /* *** 05/08/31Delete
            if( rs.next() ){
                if( date != null ) ret = svf.VrsOut( "CERTIFDATE",  KNJ_EditDate.h_format_JP(date) );                               //証明日付
                if( rs.getString("SCHOOLNAME1") != null ) ret = svf.VrsOut( "SCHOOLNAME", rs.getString("SCHOOLNAME1") );    //学校名称

                String pname = null;
                if( rs.getString("PRINCIPAL_JOBNAME") != null ) pname = rs.getString("PRINCIPAL_JOBNAME");
                if( rs.getString("PRINCIPAL_NAME")    != null ) pname = pname + "  " + rs.getString("PRINCIPAL_NAME");
                if( pname != null ) ret = svf.VrsOut( "STAFFNAME", pname );             //職名・校長名
            }
            ***** */
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }

        try {
            pp = 0;
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year     );
            ps6.setString( ++pp, semester );
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year );
            rs = ps6.executeQuery();

            if( rs.next() ){
                outobj.outGraduateExpectationStudentinfo( rs, number );  //05/08/04
/*
                if( number != null ) ret = svf.VrsOut( "NUMBER",  number );     //証明書番号
                if( rs.getString("ADDR") != null ) ret = svf.VrsOut( "ADDRESS1",   rs.getString("ADDR") );  //住所
                outobj.outStudentname( rs );    //生徒名
                if( rs.getString("BIRTHDAY")   != null ) ret = svf.VrsOut( "BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //生年月日
                if( rs.getString("GRADU_DATE") != null ) ret = svf.VrsOut( "GRADUATION", KNJ_EditDate.h_format_JP_M( rs.getString("GRADU_DATE") ) );  //卒業見込日
*/
                ret = svf.VrEndPage();
                nonedata = true;
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }
    }


    /** 
     *  卒業証明書（英語） 
     */
    public void certif2_out(String schregno,String year,String year2,String semester,String date,String number)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int pp = 0;

        try {
            ps7.setString( ++pp, year2);  //05/11/18 Mdify
            // 05/11/18 Delete ps7.setString(1,year);
            ps7.setString( ++pp, year2);
            ps7.setString( ++pp, "00000000");
            ps7.setString( ++pp, year);  //05/11/18 Build
            ResultSet rs = ps7.executeQuery();
            setCertifStyle( year, 1 );      //様式別出力クラスの読み込み
            outobj.setCertifForm( 2 );

            if( rs.next() ){
                ret = svf.VrsOut("DATE", ( date != null )? KNJ_EditDate.h_format_US(date): "" );    //証明日付 05/11/18 Modify
                //05/11/18Delete ret = svf.VrsOut("DATE"            , KNJ_EditDate.h_format_US(date));                  //証明日付
                ret = svf.VrsOut("SCHOOLNAME1"  , rs.getString("SCHOOLNAME_ENG"));                  //学校名称
                ret = svf.VrsOut("SCHOOLNAME2"  , rs.getString("SCHOOLNAME_ENG"));                  //学校名称
                ret = svf.VrsOut("STAFFNAME"    , rs.getString("PRINCIPAL_NAME_ENG"));              //代表名
            }
            if( rs != null )rs.close();
            db2.commit();

            ps6.setString(1,schregno);
            ps6.setString(2,year);
            ps6.setString(3,semester);
            ps6.setString(4,schregno);
            ps6.setString(5,year);
            rs = ps6.executeQuery();

            if( rs.next() ){
                ret = svf.VrsOut("NUMBER"   , number);                                              //証明書番号
                ret = svf.VrsOut("NAME"     , rs.getString("NAME_ENG"));                            //氏名
                ret = svf.VrsOut("SEX"      , rs.getString("SEX_ENG"));                             //性別
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_US(rs.getString("BIRTHDAY")));      //生年月日
                ret = svf.VrsOut("YEAR_S"   , KNJ_EditDate.h_format_US_M(rs.getString("ENT_DATE")));    //入学日
                ret = svf.VrsOut("YEAR_F"   , KNJ_EditDate.h_format_US_M(rs.getString("GRADU_DATE")));  //卒業日
                ret = svf.VrsOut("YEAR_G"   , KNJ_EditDate.h_format_US(rs.getString("GRADU_DATE")));    //卒業日
                ret = svf.VrEndPage();
                nonedata = true;
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("certif2_out error! " + ex);
        }

    }//certif2_outの括り


    /** 
     *  卒業証明書 
     *  2005/01/17 近大付属高校用のフォームレイアウトとする
     *  2005/01/19 学科課程の表示に’課程’を入れる
     *  2005/01/27 FormにおいてFieldの変更に伴い修士
     *  2005/07/21 Modify 標準版と近大付属版の異なる出力様式に対応
     **/
    public void certif1_out(String schregno,
                            String year,
                            String year2,
                            String semester,
                            String date,
                            String number)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int pp = 0;
        ResultSet rs = null;
        setCertifStyle( year, 1 );      //様式別出力クラスの読み込み
        outobj.setCertifForm( 1 );

        try {
            //ps7.setString( ++pp, year  );
            ps7.setString( ++pp, year2  );  //05/10/05Modify 学校名を現在年度で取得
            ps7.setString( ++pp, year2 );
            ps7.setString( ++pp, "00000000" );
            ps7.setString( ++pp, year);  //05/11/18 Build
            rs = ps7.executeQuery();
            if( rs.next() )outobj.outGraduateSchoolinfo( rs, date );
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }

        try {
            pp = 0;
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year     );
            ps6.setString( ++pp, semester );
            ps6.setString( ++pp, schregno );
            ps6.setString( ++pp, year );
            rs = ps6.executeQuery();
            if( rs.next() ){
                outobj.outGraduateStudentinfo( rs, number );
                ret = svf.VrEndPage();
                nonedata = true;
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }
    }


    /** 
     *  在学証明書(中学用) 
     *  2005/04/14 yamashiro
     *  2005/05/12 yamashiro 学年の前スペースを削除
     *
     */
    public void certif12_out(String schregno,String year,String year2,String semester,String date,String number)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int pp = 0;

        try {
            ps7.setString( ++pp, year2);  //05/11/18 Mdify
            // 05/11/18 Delete ps7.setString(1,year);
            ps7.setString( ++pp, year2);
            ps7.setString( ++pp, "00000000");
            ps7.setString( ++pp, year);  //05/11/18 Build
//log.debug("ps7="+ps7.toString());
            ResultSet rs = ps7.executeQuery();

            String schooldivname = null;
            if( rs.next() ){
                printsvfDate( date, "DATE" );  //05/11/18 処理日の出力
                //05/11/18Delete ret = svf.VrsOut("DATE"            , KNJ_EditDate.h_format_JP(date));      //証明日付
                ret = svf.VrsOut("SCHOOLNAME"   , rs.getString("SCHOOLNAME1"));         //学校名称
                String pname = null;
                if( rs.getString("PRINCIPAL_JOBNAME") != null )
                            pname = rs.getString("PRINCIPAL_JOBNAME");
                if( rs.getString("PRINCIPAL_NAME") != null )
                            pname = pname + "  " + rs.getString("PRINCIPAL_NAME");
                ret = svf.VrsOut("STAFFNAME"  , pname);                                 //職名・校長名
                if( rs.getString("T4SCHOOLDIV").equals("0") )schooldivname = "学年";   //05/11/18 Modify
                else                                         schooldivname = "年次";
            }
            if( schooldivname == null )schooldivname = "";    //05/04/14
            if( rs != null )rs.close();
            db2.commit();

            ps6.setString(1,schregno);
            ps6.setString(2,year);
            ps6.setString(3,semester);
            ps6.setString(4,schregno);
            ps6.setInt(5,Integer.parseInt(year));
//log.debug("ps6="+ps6.toString());
            rs = ps6.executeQuery();

            if( rs.next() ){
                ret = svf.VrsOut( "NUMBER",  number);                                                     //証明書番号
                ret = svf.VrsOut( "GRADE",   "第"+String.valueOf(rs.getInt("ANNUAL"))+schooldivname );               //05/04/14Modify 05/05/12Modify
                ret = svf.VrsOut( "COURSE",  ( rs.getString("COURSENAME") != null )? rs.getString("COURSENAME") : "" );//05/04/14Modify
                ret = svf.VrsOut( "SUBJECT", ( rs.getString("MAJORNAME")  != null )? rs.getString("MAJORNAME")  : "" );//05/04/14Modify

                if( rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length() )      //05/04/12Modify
                    ret = svf.VrsOut( "NAME2", rs.getString("NAME") );                          //氏名
                else
                    ret = svf.VrsOut( "NAME1", rs.getString("NAME") );                          //氏名

                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));   //生年月日
                ret = svf.VrEndPage();
                nonedata = true;
            }
            if( rs != null )rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.debug("certif12 error! " + ex );
        }
    }


    /** 
     *  学校設定クラスおよび様式別出力クラスの読み込み
     *  2005/07/21 Build
     *  2005/08/31 Modify 引数にcertifnumを追加
     */
    private void setCertifStyle( String year, 
                                 int certifnum 
                               )
    {
        if (outobj == null) {
            if (_definecode.schoolmark.substring( 0, 1 ).equals("K")) {
                outobj = new KNJWG010_1.certifKin();
            } else {
                outobj = new KNJWG010_1.certifCommon();
            }
            log.debug("schoolmark=" + _definecode.schoolmark);
        }
    }


    /** 
     *  処理日の出力
     *  2005/11/18 Build
     */
    private void printsvfDate( String date, String fname )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( date != null ){
                ret = svf.VrsOut( fname, KNJ_EditDate.h_format_JP( date ) ); //証明日付
            } else{
                /* *** 05/11/22 Delete
                ret = svf.VrAttribute( fname, "Hensyu=0" );    //05/11/18
                ret = svf.VrsOut( fname, "　　　年　 月　 日" ); //05/11/18
                *** */
                ret = svf.VrAttribute( fname, "Hensyu=1" );    //05/11/22
                ret = svf.VrsOut( fname, "年　 月　 日" ); //05/11/22
            }
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }
    }


    /** 
     *  [近大付属用様式]フォーム設定 高校用・中学用
     *  2005/08/31 Build
     *  引数　1:卒業証明書 3:卒業見込証明書 4:在学証明書
     *
    private void setCertifForm( int i )
    {
        int ret = 0;
        try {
            if( definecode.schoolmark.equals("KINJUNIOR") )ret = svf.VrSetForm("KNJWG010_" + i + "J.frm", 1);
        } catch( Exception ex ) {
            log.error("error! " + ex);
        }
    }*/




    //--- 内部クラス -------------------------------------------------------
    /*
     *  標準版用様式のクラス
     */
    private class certifCommon {
        /*
         * [標準版用様式]卒業証明書の学校項目を出力
         */
        void outGraduateSchoolinfo(
                final ResultSet rs,
                final String date
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            printsvfDate(date, "DATE");  // 処理日の出力
            try {
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名称
                String pname = null;
                if (rs.getString("PRINCIPAL_JOBNAME") != null) { pname = rs.getString("PRINCIPAL_JOBNAME"); }
                if (rs.getString("PRINCIPAL_NAME") != null) { pname = pname + "  " + rs.getString("PRINCIPAL_NAME"); }
                ret = svf.VrsOut("STAFFNAME", pname);  // 職名・校長名
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         * [標準版用様式]卒業証明書の生徒項目を出力
         */
        void outGraduateStudentinfo(
                final ResultSet rs,
                final String number
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            ret = svf.VrsOut("NUMBER", number);  // 証明書番号
            try {
                ret = svf.VrsOut("NAME"         , rs.getString("NAME"));  // 氏名
                ret = svf.VrsOut("BIRTHDAY"     , KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));  // 生年月日
                ret = svf.VrsOut("GRADUATION"   , KNJ_EditDate.h_format_JP_M(rs.getString("GRADU_DATE")));  // 卒業年月日
                ret = svf.VrsOut("COURSE"       , rs.getString("COURSENAME"));  // 課程
                ret = svf.VrsOut("SUBJECT"      , rs.getString("MAJORNAME"));  // 学科
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         * [標準版用様式]在学証明書の学校項目を出力
         */
        void outRegistSchoolinfo(
                final ResultSet rs,
                final String date
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            printsvfDate( date, "DATE" );  // 処理日の出力
            try {
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名称
                String pname = new String();
                if (rs.getString("PRINCIPAL_JOBNAME") != null) { pname = rs.getString("PRINCIPAL_JOBNAME"); }
                if (rs.getString("PRINCIPAL_NAME") != null) { pname = pname + "  " + rs.getString("PRINCIPAL_NAME"); }
                ret = svf.VrsOut("STAFFNAME", pname);  // 職名・校長名
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         * [標準版用様式]在学証明書の生徒項目を出力
         */
        void outRegistStudentinfo(
                final ResultSet rs,
                final String number,
                final String schooldivname
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            ret = svf.VrsOut("NUMBER", number);  // 証明書番号
            try {
                StringBuffer strb = new StringBuffer("  第" + String.valueOf(rs.getInt("ANNUAL")) + schooldivname);
                if (rs.getString("COURSENAME") != null) { strb.insert(0,rs.getString("COURSENAME")); }
                if (rs.getString("MAJORNAME") != null) { strb.insert(0,rs.getString("MAJORNAME")); }
                ret = svf.VrsOut("GRADE"    , strb.toString());  // 課程学科学年
                ret = svf.VrsOut("NAME"     , rs.getString("NAME"));  // 氏名
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));  // 生年月日
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         * [標準版用様式]卒業見込み証明書の生徒項目を出力
         */
        void outGraduateExpectationStudentinfo(
                final ResultSet rs,
                final String number
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            try {
                outobj.outStudentname(rs);  // 生徒名
                if (number != null ) ret = svf.VrsOut("NUMBER",  number);  // 証明書番号
                if (rs.getString("ADDR") != null) { ret = svf.VrsOut("ADDRESS1", rs.getString("ADDR")); }  // 住所
                if (rs.getString("BIRTHDAY") != null) { ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY"))); }  // 生年月日
                if (rs.getString("GRADU_DATE") != null) { ret = svf.VrsOut("GRADUATION", KNJ_EditDate.h_format_JP_M(rs.getString("GRADU_DATE"))); }  // 卒業見込日
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         * [標準版用様式]生徒名を出力
         */
        void outStudentname(final ResultSet rs) throws SQLException {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if (rs.getString("NAME") != null) { ret = svf.VrsOut("NAME", rs.getString("NAME")); }
        }

        /*
         * [標準版用様式]フォーム設定
         *  引数　1:卒業証明書 3:卒業見込証明書 4:在学証明書
         */
        void setCertifForm(final int i) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            svf.VrSetForm("KNJWG010_" + i + ".frm", 1);
        }

    }


    //--- 内部クラス -------------------------------------------------------
    /*
     *  近大付属用様式のクラス
     */
    private class certifKin extends certifCommon {
        /*
         *  [近大付属用様式] 卒業証明書の学校項目を出力
         *  2005/08/31 Modify 校長名を'X X(姓)   X X(名)'と編集して出力
         */
        void outGraduateSchoolinfo(
                final ResultSet rs,
                final String date
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            try {
                printsvfDate( date, "CERTIFDATE" );  //05/11/18 処理日の出力
                if( rs.getString("PRINCIPAL_NAME") != null )
                    ret = svf.VrsOut( "STAFFNAME", editName( rs.getString("PRINCIPAL_NAME") ) );    //校長名
            } catch( Exception ex ) {
                log.error("error! " + ex);
            }
        }

        /*
         *  [近大付属用様式] 卒業証明書の生徒項目を出力
         */
        void outGraduateStudentinfo(
                final ResultSet rs,
                final String number
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if (number != null) ret = svf.VrsOut("CERTIFNO", number);  // 証明書番号
            try {
                outStudentname(rs);  // 生徒名
                if (rs.getString("BIRTHDAY") != null ) ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));  // 生年月日
                if (rs.getString("GRADU_DATE") != null ) ret = svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_M(rs.getString("GRADU_DATE")));  // 卒業年月日
                if (rs.getString("COURSENAME") != null ) ret = svf.VrsOut("COURSE", rs.getString("COURSENAME"));
                if (rs.getString("MAJORNAME") != null ) ret = svf.VrsOut("MAJOR", rs.getString("MAJORNAME"));
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         *  [近大付属用様式] 在学証明書の学校項目を出力
         *  2005/08/31 Modify 校長名を'X X(姓)   X X(名)'と編集して出力
         */
        void outRegistSchoolinfo(
                final ResultSet rs,
                final String date
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            printsvfDate(date, "DATE");  // 処理日の出力
            try {
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名称
                if (rs.getString("PRINCIPAL_NAME") != null) { ret = svf.VrsOut("STAFFNAME", editName( rs.getString("PRINCIPAL_NAME"))); }  // 校長名
            } catch (SQLException e) {
                 log.error("SQLException", e);
            } catch (Exception e) {
                 log.error("Exception", e);
            }
        }

        /*
         *  [近大付属用様式] 在学証明書の生徒項目を出力
         */
        void outRegistStudentinfo(
                final ResultSet rs,
                final String number,
                final String schooldivname
        ) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            ret = svf.VrsOut("NUMBER", number);  // 証明書番号
            try {
                ret = svf.VrsOut("GRADE", "  第" + String.valueOf(rs.getInt("ANNUAL")) + schooldivname);
                ret = svf.VrsOut("COURSE", (rs.getString("COURSENAME") != null)? rs.getString("COURSENAME"): "");
                ret = svf.VrsOut("SUBJECT", (rs.getString("MAJORNAME") != null)? rs.getString("MAJORNAME"): "");
                outStudentname(rs);   //生徒名
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));  // 生年月日
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }

        /*
         *  [近大付属用様式] 卒業見込み証明書の生徒項目を出力
         */
        void outGraduateExpectationStudentinfo(
                final ResultSet rs,
                final String number
        ) {
            outGraduateStudentinfo(rs, number);  // 卒業証明書の生徒項目
        }

        /*
         *  [近大付属用様式] 生徒名を出力
         */
        void outStudentname(final ResultSet rs) throws SQLException {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if (rs.getString("NAME") != null) {
                if (24 < knjobj.retStringByteValue( rs.getString("NAME"), 25)) {
                    ret = svf.VrsOut("NAME2", rs.getString("NAME"));
                } else {
                    ret = svf.VrsOut("NAME1", rs.getString("NAME"));
                }
            }
        }

        /*
         *  [近大付属用様式] 校長名を編集
         */
        String editName(final String name) throws Exception {
            StringBuffer stb = new StringBuffer();
            char chr[] = name.toCharArray();
            boolean boo = false;
            for (int i = 0 ; i < chr.length ; i++) {
                if (chr[i] == (' ')  ||  chr[i] == ('　')) {
                    if (! boo  &&  0 < i ) {
                        stb.append(" ");
                        boo = true;
                    }
                    continue;
                }
                if (0 < i) stb.append("  ");
                stb.append(chr[i]);
            }
            return stb.toString();
        }

        /*
         *  [近大付属用様式]フォーム設定 高校用・中学用
         *  引数　1:卒業証明書 3:卒業見込証明書 4:在学証明書
         */
        void setCertifForm(final int i) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if (_definecode.schoolmark.equals("KINJUNIOR")) {
                ret = svf.VrSetForm("KNJWG010_" + i + "J.frm", 1);
            } else {
                ret = svf.VrSetForm("KNJWG010_" + i + ".frm", 1);
            }
            log.debug("schoolmark="+_definecode.schoolmark);
        }
    }
}
