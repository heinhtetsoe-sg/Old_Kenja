// kanji=漢字
/*
 * $Id: 012da141d0e1f0b3345f5d881ad74f1592620b2b $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.File;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJA.detail.KNJ_AddressRecSql;
import servletpack.KNJA.detail.KNJ_GradeRecSql;
import servletpack.KNJA.detail.KNJ_TransferRecSql;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditSvf;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中等教育学校用（千代田区立九段）
 *                                                    様式1（学生に関する記録）
 *
 *  2005/12/27 Build yamashiro
 *  2006/04/24 yamashiro・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加  --NO003
 *                            => 無い場合は従来通りHR_CLASSを出力
 *                      ・日付および年度の出力仕様を変更 --NO004
 *                      ・生徒名および保護者名の出力仕様変更 --NO006
 *                      ・学籍異動履歴出力仕様変更 --NO007
 *  2006/05/02 yamashiro・入学前学歴の学校名出力仕様変更 --NO008
 */

public class KNJA131FORM1 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131FORM1.class);
    private StringBuffer stb;
    private ResultSet rs;
    private Map hmap;       //NO003
    private KNJ_EditSvf editsvfobj;  //NO008


    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            setSvfForm( svf, paramap);
            printSvfDefault( svf, paramap );        //デフォルト印刷  --NO004
            printSvfDetail_1( db2, svf, paramap );  //学校情報
            printSvfDetail_2( db2, svf, paramap );  //個人情報
            printSvfDetail_3( db2, svf, paramap );  //住所履歴情報
            printSvfDetail_4( db2, svf, paramap );  //異動履歴情報
            printSvfDetail_5( db2, svf, paramap );  //年次・ホームルーム・整理番号
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
            ret = svf.VrSetForm("KNJA131_1.frm", 1);
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  学校情報
     */
    void printSvfDetail_1( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( editstringobj == null )editstringobj = new KNJEditString();

            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).setString( ++p, (String)paramap.get("YEAR") );   //年度
            ( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).setString( ++p, (String)paramap.get("YEAR") );   //年度
            rs = ( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).executeQuery();

            if( rs.next() ){
                int n1 = 0;
                n1 = editstringobj.retStringByteValue( rs.getString("SCHOOLNAME1"), 60 );
                if( 50 < n1 )ret = svf.VrsOut( "SCHOOLNAME3",  rs.getString("SCHOOLNAME1") );
                else if( 40 < n1 )ret = svf.VrsOut( "SCHOOLNAME2",  rs.getString("SCHOOLNAME1") );
                else if(  0 < n1 )ret = svf.VrsOut( "SCHOOLNAME1",  rs.getString("SCHOOLNAME1") );

                if( 0 < n1  &&  ! paramap.containsKey("SCHOOLNAME") )paramap.put("SCHOOLNAME",  rs.getString("SCHOOLNAME1") );

                int n2 = 0;
                n1 = editstringobj.retStringByteValue( rs.getString("SCHOOLADDR1"), 50 );
                n2 = editstringobj.retStringByteValue( rs.getString("SCHOOLADDR2"), 50 );
                if( 40 < n1 )ret = svf.VrsOut( "SCHOOLADDRESS1_2",  rs.getString("SCHOOLADDR1") );
                else if(  0 < n1 )ret = svf.VrsOut( "SCHOOLADDRESS1_1",  rs.getString("SCHOOLADDR1") );
                if( 40 < n2  ||  40 < n1 )ret = svf.VrsOut( "SCHOOLADDRESS2_2",  rs.getString("SCHOOLADDR2") );
                else if(  0 < n2 )ret = svf.VrsOut( "SCHOOLADDRESS2_1",  rs.getString("SCHOOLADDR2") );
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_1 error!", ex );
            log.debug( "SQL = " + ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  個人情報
     */
    void printSvfDetail_2( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("GAKKI") );  //学期
            ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            rs = ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).executeQuery();

            if ( rs.next() ){
//                if( rs.getString("COURSENAME") != null )ret = svf.VrsOut( "COURSE",        rs.getString("COURSENAME") );
                if( rs.getString("MAJORNAME")  != null )ret = svf.VrsOut( "MAJOR",         rs.getString("MAJORNAME")  );
                if( rs.getString("NAME_KANA")  != null )ret = svf.VrsOut( "KANA",          rs.getString("NAME_KANA")  );
                if( rs.getString("GUARD_KANA") != null )ret = svf.VrsOut( "GUARDIANKANA",  rs.getString("GUARD_KANA") );
                if( paramap.containsKey("KANJI_OUT") ){
                    //NO006 if( rs.getString("NAME") != null       )ret=svf.VrsOut( "NAME",          rs.getString("NAME")       );
                    //NO006 if( rs.getString("GUARD_NAME") != null )ret=svf.VrsOut( "GUARDIANNAME",  rs.getString("GUARD_NAME") );
                    //NO006 --->
                    int n = editstringobj.retStringByteValue( rs.getString("NAME"), 26 );
                    if( 0 < n  &&  n <= 24 )ret = svf.VrsOut( "NAME1", rs.getString("NAME") );
                    else if( 0 < n )ret = svf.VrsOut( "NAME2", rs.getString("NAME") );
                    n = editstringobj.retStringByteValue( rs.getString("GUARD_NAME"), 26 );
                    if( 0 < n  &&  n <= 24 )ret = svf.VrsOut( "GUARDIANNAME1", rs.getString("GUARD_NAME") );
                    else if( 0 < n )ret = svf.VrsOut( "GUARDIANNAME2", rs.getString("GUARD_NAME") );
                    //<--- NO006
                }

                if( rs.getString("NAME") != null  &&  ! paramap.containsKey("SCHNAME") )paramap.put("SCHNAME",  rs.getString("NAME") );

                if (rs.getString("BIRTHDAY") != null) ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY"))) + "生");
                if( rs.getString("SEX") != null )ret = svf.VrsOut( "SEX",  rs.getString("SEX") );

                int n1 = 0;
                /* *** NO008
                n1 = editstringobj.retStringByteValue( rs.getString("J_NAME"), 50 );
                if( 40 < n1 )ret = svf.VrsOut( "FINSCHOOL2",  rs.getString("J_NAME") );
                else         ret = svf.VrsOut( "FINSCHOOL1",  rs.getString("J_NAME") );
                *** */
                if( editsvfobj == null )editsvfobj = new KNJ_EditSvf( svf );
                editsvfobj.printSvfFinSchool( rs.getString("J_NAME"), "小学校卒業" );  //入学前学歴の学校名編集  --NO008
                //<---NO008

                //NO004 if( rs.getString("FINISH_DATE") != null )ret = svf.VrsOut( "FINISHDATE",  KNJ_EditDate.h_format_JP_M( rs.getString("FINISH_DATE") ) );
                if( rs.getString("FINISH_DATE") != null )ret = svf.VrsOut( "FINISHDATE",  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP_M( rs.getString("FINISH_DATE") ), (String)paramap.get("YEAR") ) );  //NO004

                if( ( ( rs.getString("GUARD_ADDR1") != null )? rs.getString("GUARD_ADDR1"): "" ).equals( ( ( rs.getString("ADDR1") != null )? rs.getString("ADDR1"): "" ) ) 
                 && ( ( rs.getString("GUARD_ADDR2") != null )? rs.getString("GUARD_ADDR2"): "" ).equals( ( ( rs.getString("ADDR2") != null )? rs.getString("ADDR2"): "" ) ) )  //06/03/23
                    ret=svf.VrsOut( "GUARDIANADD1_1_1",  "生徒の欄に同じ" );
                else{
                    int n2 = 0;
                    n1 = editstringobj.retStringByteValue( rs.getString("GUARD_ADDR1"), 50 );
                    n2 = editstringobj.retStringByteValue( rs.getString("GUARD_ADDR2"), 50 );
                    if( 40 < n1 )ret = svf.VrsOut( "GUARDIANADD1_1_2",  rs.getString("GUARD_ADDR1") );
                    else if(  0 < n1 )ret = svf.VrsOut( "GUARDIANADD1_1_1",  rs.getString("GUARD_ADDR1") );
                    if( 40 < n2  ||  40 < n1 )ret = svf.VrsOut( "GUARDIANADD1_2_2",  rs.getString("GUARD_ADDR2") );
                    else if(  0 < n2 )ret = svf.VrsOut( "GUARDIANADD1_2_1",  rs.getString("GUARD_ADDR2") );
                }
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_2 error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  個人情報 住所履歴情報  履歴を降順に読み込み、最大３件まで出力
     */
    void printSvfDetail_3( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_ADDRESS_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );   //学籍番号
            ( ( PreparedStatement )paramap.get("PS_ADDRESS_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );   //年度
            ( ( PreparedStatement )paramap.get("PS_ADDRESS_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );   //学籍番号
            ( ( PreparedStatement )paramap.get("PS_ADDRESS_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );   //年度
            rs = ( ( PreparedStatement )paramap.get("PS_ADDRESS_RECORD") ).executeQuery();
            int i = 0;              //出力件数
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            int n1 = 0;
            int n2 = 0;
            while ( rs.next() ){
                if( i == 0 ){
                    i = rs.getInt("COUNT");
                    if( 3 < i )i = 3;
                }
                n1 = editstringobj.retStringByteValue( rs.getString("ADDR1"), 50 );
                n2 = editstringobj.retStringByteValue( rs.getString("ADDR2"), 50 );
                if( 40 < n1 )ret = svf.VrsOut( "ADDRESS" + i + "_1_2",  rs.getString("ADDR1") );
                else if(  0 < n1 )ret = svf.VrsOut( "ADDRESS" + i + "_1_1",  rs.getString("ADDR1") );
                if( 40 < n2  ||  40 < n1 )ret = svf.VrsOut( "ADDRESS" + i + "_2_2",  rs.getString("ADDR2") );
                else if(  0 < n2 )ret = svf.VrsOut( "ADDRESS" + i + "_2_1",  rs.getString("ADDR2") );

                if( i == 1 )break;
                i--;
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_3 error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  個人情報 異動履歴情報
     */
    void printSvfDetail_4( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            rs = ( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).executeQuery();

            ret = svf.VrsOut( "LINE1",  "＝＝＝＝＝＝＝＝" );
            ret = svf.VrsOut( "LINE2",  "＝＝＝＝＝＝＝＝" );
            int cd2 = 0;
            int i = 0;
            int j = 0;    //休学・留学回数
            while( rs.next() ){
                cd2 = Integer.parseInt( rs.getString("NAMECD2") );
                if( rs.getString("NAMECD1").equals("A002") ){
                    if (cd2 == 4) {  // 転入学
                        if( rs.getString("SDATE")  != null )ret = svf.VrsOut( "MOVEDATE",     KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) ); //NO004
                        if( rs.getString("GRADE")  != null )ret = svf.VrsOut( "MOVEGRADE",    String.valueOf( Integer.parseInt( rs.getString("GRADE") ) ) );
                        i = 0;
                        if( rs.getString("REASON") != null )ret = svf.VrsOut( "MOVENOTE" + ( ++i ),  rs.getString("REASON") );
                        if( rs.getString("PLACE")  != null )ret = svf.VrsOut( "MOVENOTE" + ( ++i ),  rs.getString("PLACE")  );
                        if( rs.getString("ADDR")   != null )ret = svf.VrsOut( "MOVENOTE" + ( ++i ),  rs.getString("ADDR")   );
                    } else {
                        if( rs.getString("SDATE") != null  )ret = svf.VrsOut( "ENTERDATE",    KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) ); //NO004
                        if( rs.getString("REASON") != null )ret = svf.VrsOut( "ENTERNOTE1",    rs.getString("REASON") );
                        if( cd2 != 5 ){
                            if( rs.getString("GRADE") != null  )ret = svf.VrsOut( "ENTERGRADE1",  String.valueOf( Integer.parseInt( rs.getString("GRADE") ) ) );
                            ret = svf.VrsOut( "LINE1",  "                " );
                        } else{
                            if( rs.getString("GRADE") != null  )ret = svf.VrsOut( "ENTERGRADE2",  String.valueOf( Integer.parseInt( rs.getString("GRADE") ) ) );
                        ret = svf.VrsOut( "LINE2",  "                " );
                        }
                    }
                }
                if( rs.getString("NAMECD1").equals("A004") ){
                    if( cd2 == 1  ||  cd2 == 2){     //1:留学  2:休学
                        if( j > 2)  continue;
                        j++;
                        if( rs.getString("EDATE") != null )
                            //NO004 ret = svf.VrsOut( "ABROADDATE" + j,  KNJ_EditDate.h_format_JP( rs.getString("SDATE") ) + "\uFF5E" + KNJ_EditDate.h_format_JP( rs.getString("EDATE") ) );
                            ret = svf.VrsOut( "ABROADDATE" + j,  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) + "\uFF5E" + KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("EDATE") ), (String)paramap.get("YEAR") ) );  //NO004
                        else
                            //NO004 ret = svf.VrsOut( "ABROADDATE" + j,  KNJ_EditDate.h_format_JP( rs.getString("SDATE") ) );
                            ret = svf.VrsOut( "ABROADDATE" + j,  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) );  //NO004
                        if( rs.getString("REASON") != null )ret = svf.VrsOut( "ABROADNOTE" + j + "_1",  rs.getString("REASON") );
                        if( rs.getString("PLACE")  != null )ret = svf.VrsOut( "ABROADNOTE" + j + "_2",  rs.getString("PLACE")  );
                    }
                }
                if( rs.getString("NAMECD1").equals("A003") ){
                    if( cd2 == 2  ||  cd2 == 3 ){   //2:退学  3:転学
                        if( rs.getString("SDATE") != null ){
                            //NO004 if( cd2 == 3 )ret = svf.VrsOut( "EXPULDATE1",  KNJ_EditDate.h_format_JP( rs.getString("SDATE") ) );
                            //NO004 else          ret = svf.VrsOut( "EXPULDATE2",  KNJ_EditDate.h_format_JP( rs.getString("SDATE") ) );
                            //NO007 if( cd2 == 3 )ret = svf.VrsOut( "EXPULDATE1",  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) ); //NO004
                            //NO007 else          ret = svf.VrsOut( "EXPULDATE2",  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) ); //NO004
                            ret = svf.VrsOut( "EXPULDATE1",  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) ); //NO004  NO007
                        }
                        //ret = svf.VrsOut( "tengaku_GRADE"     ,String.valueOf(rs.getInt("GRADE")));
                        if( rs.getString("REASON") != null )ret = svf.VrsOut( "EXPULNOTE1",  rs.getString("REASON") );
                        if( rs.getString("PLACE")  != null )ret = svf.VrsOut( "EXPULNOTE2",  rs.getString("PLACE")  );  //NO007
                        if( rs.getString("ADDR")   != null )ret = svf.VrsOut( "EXPULNOTE3",  rs.getString("ADDR")   );  //NO007
                        //ret = svf.VrsOut( "KUBUN"             ,"転学");
                    }
                    if( cd2 == 1 ){                                         //卒業
                        //NO004 if( rs.getString("SDATE") != null )ret = svf.VrsOut( "GRADDATE",  KNJ_EditDate.h_format_JP( rs.getString("SDATE") ) );
                        if( rs.getString("SDATE") != null )ret = svf.VrsOut( "GRADDATE",  KNJ_EditDate.setDateFormat( KNJ_EditDate.h_format_JP( rs.getString("SDATE") ), (String)paramap.get("YEAR") ) ); //NO004
                        //if( rs.getString("PLACE") != null )ret = svf.VrsOut( "FIELD1",  rs.getString("PLACE") );  //卒業台帳番号
                    }
                }
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_4 error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  個人情報  学籍等履歴情報
     */
    void printSvfDetail_5( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            getDocumentroot( db2, paramap );  //NO001
//log.debug("paramap="+paramap);
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            rs = ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).executeQuery();
            if( hmap == null )hmap = KNJ_Get_Info.getMapForHrclassName( db2 );  //NO003 表示用組

            int i = 0;
//            String str = null;  //NO001
            while( rs.next() ){
                i = Integer.parseInt( rs.getString("GRADE") );
                ret = svf.VrsOutn( "HR_CLASS",  i,  KNJ_EditEdit.Ret_Num_Str( rs.getString("HR_CLASS"), hmap ) );   //組 04/08/18Modify  NO003 Modify
                //NO003 ret = svf.VrsOutn( "HR_CLASS",  i,  KNJ_EditEdit.Ret_Num_Str( rs.getString("HR_CLASS") ) ); //組 04/08/18Modify
                ret = svf.VrsOutn( "ATTENDNO",  i,  String.valueOf( Integer.parseInt( rs.getString("ATTENDNO") ) ) );    //出席番号
                ret = svf.VrsOutn( "NENDO",       +i,  KNJ_EditDate.setNendoFormat(nao_package.KenjaProperties.gengou( rs.getInt("YEAR") ) + "年度", null) );
                ret = svf.VrsOutn( "STAFFNAME1",  +i,  rs.getString("PRINCIPALNAME") );
                //担任名を2名まで印刷可能にする。　担任1＋’△’＋担任2で表示。　20文字と30文字。
                String staffName = "";
                if (rs.getString("STAFFNAME") != null)  staffName = rs.getString("STAFFNAME");
                if (rs.getString("STAFFNAME2") != null) staffName = staffName + "　　" + rs.getString("STAFFNAME2");
                if (staffName.length() <= 20) {
                    ret = svf.VrsOutn( "STAFFNAME2_1",  i,  staffName );
                } else {
                    ret = svf.VrsOutn( "STAFFNAME2_2",  i,  staffName );
                }
////paramap.put("DOCUMENTROOT", "/usr/local/development/src");
//                str = getImageFile( paramap, rs.getString("PRINCIPALSTAFFCD") );  //NO001
//                if( str != null )ret = svf.VrsOutn( "STAMP1",  +i,  str );  //校長印　NO001
//                str = getImageFile( paramap, rs.getString("STAFFCD") );  //NO001
//                if( str != null )ret = svf.VrsOutn( "STAMP2",  +i,  str );  //担任印 NO001
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_5 error!", ex );
        }
    }


    /**
     *  PrepareStatementオブジェクト作成
     */
    public void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            if( paramap.containsKey("PS_SCHOOL_INFO") )return;

            //学校データ
            if( ! paramap.containsKey("PS_SCHOOL_INFO") ){
                KNJ_SchoolinfoSql obj = new KNJ_SchoolinfoSql("10000");
                paramap.put("PS_SCHOOL_INFO",  db2.prepareStatement( obj.pre_sql() ) );
                obj = null;
            }
            //個人学籍データ
            if( ! paramap.containsKey("PS_SCHREG_INFO") ){
                KNJ_PersonalinfoSql obj = new KNJ_PersonalinfoSql();
                paramap.put("PS_SCHREG_INFO",  db2.prepareStatement( obj.sql_info_reg("1111111000") ) );
                obj = null;
            }
            //生徒住所履歴
            if( ! paramap.containsKey("PS_ADDRESS_RECORD") ){
                KNJ_AddressRecSql obj = new KNJ_AddressRecSql();
                paramap.put("PS_ADDRESS_RECORD",  db2.prepareStatement( obj.sql_state() ) );
                obj = null;
            }
            //生徒異動履歴
            if( ! paramap.containsKey("PS_TRANSFER_RECORD") ){
                KNJ_TransferRecSql obj = new KNJ_TransferRecSql();
                paramap.put("PS_TRANSFER_RECORD",  db2.prepareStatement( obj.sql_state() ) );
                obj = null;
            }
            //学籍等履歴
            if( ! paramap.containsKey("PS_REGD_RECORD") ){
                String useSchregRegdHdat = (paramap.get("useSchregRegdHdat") == null ? null : (String) paramap.get("useSchregRegdHdat")); 
                paramap.put("PS_REGD_RECORD",  db2.prepareStatement( new KNJ_GradeRecSql().sql_state(useSchregRegdHdat) ) );
            }
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
            if( ! paramap.containsKey("PS_SCHOOL_INFO")     )( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO")     ).close();
            if( ! paramap.containsKey("PS_SCHREG_INFO")     )( ( PreparedStatement )paramap.get("PS_SCHREG_INFO")     ).close();
            if( ! paramap.containsKey("PS_ADDRESS_RECORD")  )( ( PreparedStatement )paramap.get("PS_ADDRESS_RECORD")  ).close();
            if( ! paramap.containsKey("PS_TRANSFER_RECORD") )( ( PreparedStatement )paramap.get("PS_TRANSFER_RECORD") ).close();
            if( ! paramap.containsKey("PS_REGD_RECORD")     )( ( PreparedStatement )paramap.get("PS_REGD_RECORD")     ).close();
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }


    /**
     *  写真データ格納フォルダの取得  --NO001
     */
    private void getDocumentroot( DB2UDB db2, Map paramap )
    {
        try {
            KNJ_Control imagepath_extension = new KNJ_Control();            //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            if( returnval.val4 != null )paramap.put( "IMAGE1",  returnval.val4 );  //写真データ格納フォルダ
            if( returnval.val5 != null )paramap.put( "IMAGE2",  returnval.val5 );  //写真データの拡張子
        } catch( Exception ex ){
            log.debug( "getDocumentroot error!", ex );
        }
    }


    /**
     *  写真データファイルの取得  --NO001
     */
    private String getImageFile( Map paramap, String filename )
    {
        String ret = null;
        try {
            String str = null;
            File file1 = null;
//log.debug("image1="+(String)paramap.get("IMAGE1"));
            if( paramap.containsKey("DOCUMENTROOT")  &&  paramap.containsKey("IMAGE1")  &&  paramap.containsKey("IMAGE1") ){
                str = (String)paramap.get("DOCUMENTROOT") + "/" + (String)paramap.get("IMAGE1") + "/" + filename + "." + (String)paramap.get("IMAGE2");
//log.debug("str="+str);
                file1 = new File( str );   //写真データ存在チェック用
                if( file1.exists() )ret = str;
            }
        } catch( Exception ex ){
            log.debug( "getDocumentroot error!", ex );
        }
        return ret;
    }


    /**
     *  SVF-FORM 印刷処理 初期印刷  --NO004
     */
    void printSvfDefault( Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(null) + "生");
            ret = svf.VrsOut( "ENTERDATE",   KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) );
            ret = svf.VrsOut( "MOVEDATE",    KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) );
            ret = svf.VrsOut( "EXPULDATE1",  KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) );
            ret = svf.VrsOut( "EXPULDATE2",  KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) );
            ret = svf.VrsOut( "GRADDATE",    KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) );
            ret = svf.VrsOut( "ABROADDATE1", KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) + "\uFF5E" + KNJ_EditDate.setDateFormat( null, (String)paramap.get("YEAR") ) );
            for( int i = 0; i < 6; i++ )ret = svf.VrsOutn( "NENDO",  i + 1,  KNJ_EditDate.setNendoFormat( null, (String)paramap.get("YEAR") ) );
        } catch( Exception ex ){
            log.debug( "printSvfDefault error!", ex );
        }
    }


}
