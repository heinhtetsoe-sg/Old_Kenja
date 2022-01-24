// kanji=漢字
/*
 * $Id: 532739a12b50b3d0f58cd17f46072c4266619bea $
 *
 * 作成日: 2005/07/28 11:25:40 - JST
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
 *  学校教育システム 賢者 [入試管理] 成績一覧表
 *
 *  2005/07/28 Build
 *  2005/08/10 Modify yamashiro 受験番号順出力において付属出身者出力有無を追加
 *                              付属出身者の判断を受験番号3000番台以上とする
 *  2005/08/16 Modify yamashiro 付属出身者の判断を元にもどす（08/10の仕様は破棄）
 *                              全一覧表において付属出身者と付属出身者以外の選択を追加（08/10の仕様は破棄）
 *  2005/09/05 Modify yamashiro 表１において、’付属’欄を追加
 *                              表１・３において、後期試験のアラカルト順位は志望区分に「ＴＨ」がある人で「Ｉ」のみの人は対象外
 *                              表１において、１教科でも抜けがあった場合、４教科順位は対象外
 *                              表４において、コース毎の出力対象者は各コースが第一志望の場合のみとする
 *  2005/11/08 Modify m-yama    前期成績順でも、コース別に出力可にする。 --NO001
 *  2005/12/21 Modify yamashiro 
 *      ○後期アラカルト型(志望者全員)においてＩのみの受験者の場合、アラカルト席次は付けず(ブランクのまま)国算理の合計点を基に該当行に表記する。
 *      　同点の場合、アラカルト合計点を先に表記する。
 *      ○後期アラカルト型(コースごと)において、順位はコース毎ではなくアラカルト型全員の順位を表記する。
 *        即ち、３科目アラカルト型成績順をコース別に分類したもの。
 *      ○集計対象科目は満点マスターのＡ配点集計フラグ(Ｉ)またはＢ配点集計フラグ(Ｈ・Ｔ)が'1'のものとする。
 *  2005/12/26 Modify m-yama    前期コース別の出力時にサブタイトルにコース名を出力 --NO002
 *  2005/12/28 Modify yamashiro
 *      ○前期成績順（コースごと)において、順位はコース毎ではなく全員の順位を表記する。 --NO003
 *      ○判定対象コースごとに、１科目でも未受験科目があれば'＊'を表記する。 --NO004
 *          前期は４科目、後期は?コースは国・算・理の３科目、?以外のコースは国・算・アラカルトの３科目を満たしていない場合は未受験科目有りとする
 *            => 満点マスターのＡ配点集計フラグ(Ｉ)またはＢ配点集計フラグ(Ｈ・Ｔ)が'0'、若しくはアラカルトフラグが'1'の科目数が４または３未満を未受験とする
 *  2006/01/17 Modify yamashiro
 *      ○事前判定の不具合を修正 --NO005
 * @author yamashiro
 * @version $Id: 532739a12b50b3d0f58cd17f46072c4266619bea $
 */
public class KNJL323K
{
    private static final Log log = LogFactory.getLog(KNJL323K.class);

    private KNJEditString knjobj = new KNJEditString();
    private Map desiredivmap;                               //志望区分表示用Map
    private KNJL323K.outDetailCommonClass outobj;            //各出力様式のクラス
    private int pagecount;


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
     *      "SORT",       出力順               1.受験番号順  2.成績順
     *      "OUTTYPE",    後期用出力型         1.理科型  2.アラカルト型
     *      "OUTRIKA",    理科出力種別         1.医薬進学  2.その他
     *      "OUTARAKALT", アラカルト出力種別   1.志願者全部  2.コース別
     *      "OUTPUT",     名前・受験No.出力有  
     *      "JHFLG",      中学/高校フラグ      1.中学  2.高校
     *      "OUTPUT2"     出力対象             1.付属出身以外  1.付属出身
     *
     *
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap )
    {
        try {
            paramap.put( "YEAR",       request.getParameter("YEAR")      );  //年度
            paramap.put( "TESTDIV",    request.getParameter("TESTDIV")   );  //試験区分
            paramap.put( "SORT",       request.getParameter("SORT")      );  //出力順

            if( request.getParameter("OUTTYPE")    != null )
                paramap.put( "OUTTYPE",    request.getParameter("OUTTYPE")   );  //後期用出力型
            else
                paramap.put( "OUTTYPE",    new String("0")   );                  //後期用出力型

            if( request.getParameter("OUTRIKA")    != null )
                paramap.put( "OUTRIKA",    request.getParameter("OUTRIKA")   );  //理科出力種別
            else
                paramap.put( "OUTRIKA",    new String("0")   );                  //理科出力種別

            if( request.getParameter("OUTARAKALT") != null )
                paramap.put( "OUTARAKALT", request.getParameter("OUTARAKALT"));  //アラカルト出力種別
            else
                paramap.put( "OUTARAKALT", new String("0")   );                  //アラカルト出力種別

            if( request.getParameter("OUTPUT")     != null )
                paramap.put( "OUTPUT",     request.getParameter("OUTPUT")    );  //名前・受験No.出力有

            paramap.put( "JHFLG",      request.getParameter("JHFLG")     );      //中学/高校フラグ
            paramap.put( "OUTARAKALTCOURSE",      new String("1")     );         //アラカルト出力種別コース種別 => プログラムで指定

            if( request.getParameter("OUTPUT2")     != null )
                paramap.put( "OUTPUT2",     request.getParameter("OUTPUT2")    );   //付属出身者出力有
            else
                paramap.put( "OUTPUT2",     new String("1")    );                   //付属出身者出力有
            //NO001
            if( request.getParameter("OUTPUT3")     != null )
                paramap.put( "OUTPUT3",     request.getParameter("OUTPUT3")    );   //コース別出力

            if( request.getParameter("SPECIAL_REASON_DIV")     != null )
                paramap.put( "SPECIAL_REASON_DIV",     request.getParameter("SPECIAL_REASON_DIV")    );   //特別理由

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
        PreparedStatement arrps[] = new PreparedStatement[2];

        try {
            desiredivmap = setMapDesirediv( db2, paramap );         //志望区分表示用Map作成
            setSvfformFormat(db2, svf, paramap );                       //SVF-FORM設定

            //＃１〜＃４の医薬コースまでの印刷処理を行う
            setPreparedstatement( db2, paramap, arrps );            //PreparedStatement作成
            if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;              //SVF-FORM印刷

            //＃４帳票のコース別の場合、続けて印刷処理を行う
            if( ( (String)paramap.get("OUTARAKALT") ).equals("2") || ( (String)paramap.get("OUTPUT3") ) != null)    //NO001
                if( printSvfCourse( db2, svf, paramap, arrps ) )nonedata = true;        //SVF-FORM印刷
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
     *  印刷処理  ＃４帳票のコース別
     *
     */
    private boolean printSvfCourse( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ

        try {
            for( int i = 2 ; i <= 3 ; i++ ){
                paramap.remove("OUTARAKALTCOURSE");
                paramap.put( "OUTARAKALTCOURSE",      new String( String.valueOf( i ) )     );         //アラカルト出力種別コース種別 => プログラムで指定
                arrps[1] = db2.prepareStatement( prestatScore( paramap ) );          //受験生別得点
                pagecount = 0;
                if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;       //SVF-FORM印刷
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /**
     *  印刷処理
     *
     */
    private boolean printsvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        ResultSet arrrs[] = new ResultSet[2];
        int cnt = 0;            //出力件数カウント

        try {
            setHeadItem( db2, paramap, arrrs[0] );              //帳票見出し項目設定
            arrrs[1] = arrps[1].executeQuery();                 //受験生別得点
            while( arrrs[1].next() ){
                if( 100 == cnt ){
                    svf.VrEndPage();
                    nonedata = true;
                    cnt = 0;
                }
                if( 0 == cnt )outobj.printsvfOutHead( svf, paramap );           //SVF-FORM印刷
                outobj.printsvfOutDetail( db2, svf, paramap, arrrs, arrps[0], cnt );        //SVF-FORM印刷
                cnt++;
            }
            if( 0 == cnt )outobj.printsvfOutHead( svf, paramap );                   //SVF-FORM印刷
            printsvfOutTotal( svf, paramap );                //SVF-FORM印刷
            svf.VrEndPage();
            nonedata = true;
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                for( int i = 0 ; i < arrrs.length ; i++ )if( arrrs[i] != null )arrrs[i].close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return nonedata;
    }


    /**
     *  帳票見出し項目設定
     *      paramap
     *  
     */
    private void setHeadItem( DB2UDB db2, Map paramap, ResultSet rs )
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
            if( ! paramap.containsKey("SCHOOLDIVNAME") ){
                if( ( (String)paramap.get("JHFLG") ).equals("1") )
                    paramap.put( "SCHOOLDIVNAME",  "中学校" );                      //中学/高校
                else
                    paramap.put( "SCHOOLDIVNAME",  "高校" );                        //中学/高校
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

        //受験者数取得および総ページ数
        try {
            if( paramap.containsKey( "A_NUMBER" ) ){
                paramap.remove( "A_NUMBER" );
                paramap.remove( "M_NUMBER" );
                paramap.remove( "F_NUMBER" );
                paramap.remove( "TOTALPAGE" );
            }

            db2.query( prestatCountExamnee( paramap ) );
            rs = db2.getResultSet();
            if( rs.next() ){
                if( rs.getString("A_NUMBER") != null ){
                    paramap.put( "A_NUMBER",  rs.getString("A_NUMBER") );       //受験者数
                    paramap.put( "M_NUMBER",  rs.getString("M_NUMBER") );       //男子受験者数
                    paramap.put( "F_NUMBER",  rs.getString("F_NUMBER") );       //女子受験者数
                    int intx = Integer.parseInt( rs.getString("A_NUMBER") );
                    paramap.put( "TOTALPAGE", String.valueOf( ( 0 < intx % 100 )? intx / 100 + 1 : intx / 100 )  );     //総ページ数
                }
            }
            if( ! paramap.containsKey( "A_NUMBER") ){
                paramap.put( "A_NUMBER",  new String("0") );        //受験者数
                paramap.put( "M_NUMBER",  new String("0") );        //男子受験者数
                paramap.put( "F_NUMBER",  new String("0") );        //女子受験者数
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

        try {
            if( paramap.containsKey( "NOW_DATE" ) )paramap.remove( "NOW_DATE" );

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

        //コース名 NO002
        try {
            str = "SELECT EXAMCOURSE_NAME FROM ENTEXAM_COURSE_MST WHERE ENTEXAMYEAR = '"+(String)paramap.get("YEAR")+"' ORDER BY EXAMCOURSECD ";
            db2.query( str );
            rs = db2.getResultSet();
            int cnt = 1;
            while( rs.next() ){
                paramap.put( "COURSENAME"+String.valueOf(cnt),  rs.getString("EXAMCOURSE_NAME") );
                cnt++;
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }



    }


    /**
     *  帳票ヘッダー等印刷
     *
     */
    private void printsvfOutHead( Vrw32alp svf, Map paramap )
    {

        try {
            svf.VrsOut( "NENDO",     (String)paramap.get("NENDO") );
            svf.VrsOut( "SCHOOLDIV", (String)paramap.get("SCHOOLDIVNAME") );
            svf.VrsOut( "TESTDIV",   (String)paramap.get("TESTDIVNAME") );
            svf.VrsOut( "DATE",      (String)paramap.get("NOW_DATE") );
            svf.VrsOut( "TOTAL_PAGE",(String)paramap.get("TOTALPAGE") );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  帳票合計印刷
     *
     */
    private void printsvfOutTotal( Vrw32alp svf, Map paramap )
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append("男 ").append( (String)paramap.get("M_NUMBER") ).append("名");
            stb.append("  女 ").append( (String)paramap.get("F_NUMBER") ).append("名");
            stb.append("  合計 ").append( (String)paramap.get("A_NUMBER") ).append("名");
            svf.VrsOut( "TOTAL_MEMBER", stb.toString() );
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

            if( ( (String)paramap.get("SORT") ).equals("1") ){                          // <<出力順による分岐>>
                svf.VrSetForm( "KNJL323_1.frm", 1 );      //受験番号順
                outobj = new outDetailSub1Class();
            } else if( ( (String)paramap.get("OUTTYPE") ).equals("1") ){                // <<後期用出力型による分岐>>
                if( paramap.containsKey("OUTPUT") ){
                    svf.VrSetForm( "KNJL323_4.frm", 1 );  //成績順/後期/理科型/名前受験No.出力有
                    outobj = new outDetailSub3Class();
                }else{
                    svf.VrSetForm( "KNJL323_5.frm", 1 );  //成績順/後期/理科型/名前受験No.出力無
                    outobj = new outDetailSub3Class();
                }
            } else{

                if( paramap.containsKey("OUTPUT") ){
                    svf.VrSetForm( "KNJL323_4.frm", 1 );  //成績順/後期/アラカルト型/名前受験No.出力有
                    outobj = new outDetailSub4Class();
                }else{
                    svf.VrSetForm( "KNJL323_5.frm", 1 );  //成績順/後期/アラカルト型/名前受験No.出力無
                    outobj = new outDetailSub4Class();

                }

            }
            setInfluenceName(db2, svf, paramap);

        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
log.debug("paramap="+paramap);
        }

    }


    /**
     *  PreparedStatement作成
     *
     */
    private void setPreparedstatement( DB2UDB db2, Map paramap, PreparedStatement arrps[] )
    {
        try {
            arrps[0] = db2.prepareStatement( prestatConsultation( paramap ) );  //事前判定
            arrps[1] = db2.prepareStatement( prestatScore( paramap ) );         //受験生別得点
        } catch( Exception ex ) {
            log.error("error! ",ex);
        }
    }


    /**
     *  事前判定設定
     *
     */
    private String setConsultation( DB2UDB db2, PreparedStatement ps, ResultSet arrrs[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            int pp = 0;
            stb.append("---");
            ps.setString( ++pp,  arrrs[1].getString("EXAMNO") );
            ps.setString( ++pp,  arrrs[1].getString("EXAMNO") );
            arrrs[0] = ps.executeQuery();
            while ( arrrs[0].next() ){
                if( 0 < Integer.parseInt( arrrs[0].getString("WISHNO") )   &&
                    Integer.parseInt( arrrs[0].getString("WISHNO") ) <= 3  &&
                    arrrs[0].getString("JUDGEMENT") != null )
                    stb.replace( Integer.parseInt( arrrs[0].getString("WISHNO") ) - 1, Integer.parseInt( arrrs[0].getString("WISHNO") ), arrrs[0].getString("JUDGEMENT") );  //NO005
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
            try {
    log.debug("examno="+arrrs[1].getString("EXAMNO"));
    log.debug("ps="+ps.toString());
            } catch( Exception ex2 ){
                log.error("error! ",ex2);
            }
        }
        return stb.toString();
    }


    /**
     *  MAP作成  志望区分表示用Map作成
     *
     */
    private Map setMapDesirediv( DB2UDB db2, Map paramap )
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map retmap = new HashMap();

        try {
            Map divmap = new HashMap();
            divmap.put( "I", new Integer(1) );
            divmap.put( "T", new Integer(2) );
            divmap.put( "H", new Integer(3) );
            String desirediv = null;
            StringBuffer stb = new StringBuffer();
            ps = db2.prepareStatement( prestatDesirediv( paramap ) );    //志望区分設定
            rs = ps.executeQuery();
log.debug("map="+divmap);
            while( rs.next() ){
                if( ! rs.getString("DESIREDIV").equals( desirediv ) ){
                    if( desirediv != null ){
                        retmap.put( desirediv, stb.toString() );
                        stb.delete(0, stb.length());
                    }
                    desirediv = rs.getString("DESIREDIV");
                }
                if( divmap.containsKey( rs.getString("EXAMCOURSE_MARK") ) ){
                    for( int i = 0 ; stb.length() < ((Integer)divmap.get( rs.getString("EXAMCOURSE_MARK") )).intValue() - 1 && i < 3 ; i++ )stb.append(" ");
                    stb.append( rs.getString("EXAMCOURSE_MARK") );
                }
            }
            if( desirediv != null  &&  0 < stb.length() )retmap.put( desirediv, stb.toString() );
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                if( rs != null )rs.close();
                if( ps != null )ps.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
log.debug("retmap="+retmap);

        return retmap;
    }


    /**
     *  priparedstatement作成  志望区分
     */
    private String prestatDesirediv( Map paramap )
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append("SELECT  T1.DESIREDIV, T2.EXAMCOURSE_MARK ");
            stb.append("FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(    "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(    "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(    "AND T2.COURSECD = T1.COURSECD ");
            stb.append(    "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(    "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append("ORDER BY T1.DESIREDIV, T1.WISHNO ");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  事前判定
     *  2005/08/16 Modify 事前判定の取得仕様を変更  SQLはm.yamashiro君作成
     */
    private String prestatConsultation( Map paramap )
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append("SELECT  INT(T1.EXAMCOURSECD)/1000 AS WISHNO ");
            stb.append(       ",(SELECT NAME1 FROM NAME_MST T4 WHERE T4.NAMECD1 = 'L002' AND T4.NAMECD2 = T4.JUDGEMENT) AS JUDGEMENT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T3 ");
            stb.append("LEFT    JOIN ENTEXAM_APPLICANTCONS_DAT T4 ");
            stb.append(              "ON T4.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(             "AND T4.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(             "AND T4.EXAMNO = T3.EXAMNO ");
            stb.append(             "AND T4.SHDIV = T3.SHDIV ");
            stb.append(             "AND T4.EXAMNO = ? ");  //NO005
            stb.append("LEFT    JOIN ENTEXAM_COURSE_MST T1 ");
            stb.append(              "ON T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(             "AND T4.COURSECD = T1.COURSECD ");
            stb.append(             "AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(             "AND T4.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append("WHERE  T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("           AND T3.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            }
            stb.append(   "AND T3.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(   "AND T3.EXAMNO = ? ");
            stb.append(   "AND T4.EXAMNO IS NOT NULL ");
            stb.append("ORDER BY T1.EXAMCOURSECD ");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        return stb.toString();
    }


    /**
     *  priparedstatement作成  受験生別得点
     *  2005/02/21 Modify
     */
    private String prestatScore( Map paramap )
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append("WITH ");

            //任意のコースを含む志望区分を抽出　＊＊内容をメソッドprestatCountExamneeと同様とする＊＊
            stb.append("COURSE_DESIREDIV AS(");
            stb.append(       "SELECT  T1.DESIREDIV ");
            stb.append(       "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            // -> ＃４の帳票でコース別出力の場合は、任意のコースを含んだ志望区分とし、以外は医薬進学コースとする
            if( ( (String)paramap.get("OUTARAKALTCOURSE") ).equals("2")  )
                stb.append(       "AND T2.EXAMCOURSE_MARK = 'T' ");
            else if( ( (String)paramap.get("OUTARAKALTCOURSE") ).equals("3")  )
                stb.append(       "AND T2.EXAMCOURSE_MARK = 'H' ");
            else
                stb.append(       "AND T2.EXAMCOURSE_MARK = 'I' ");
            //05/09/05Modify コース毎の出力対象者は、各コースが第一志望の場合のみとする
            stb.append(           "AND T1.WISHNO = '1' ");   //05/09/05
            stb.append(")");

            //対象受験生を抽出
            //05/12/21 Build  元のBASE_A表において志望コースの条件を除いた表
            stb.append(",BASE_B AS( ");
            stb.append(   "SELECT  T3.EXAMNO, T3.NAME, T3.SEX, T3.DESIREDIV, T3.NATPUBPRIDIV ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T3 ");
            stb.append(   "WHERE   T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("           AND T3.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            }
            stb.append(       "AND T3.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            // -> ＃３の帳票で医薬進学希望者のみの場合または前期のコース別の場合は対象者を限定する
            // -> ＃２の帳票(前期４科目合計成績順)でコース別の場合対象者を限定していたが、これを削除する --NO003
            if( ( ( (String)paramap.get("OUTTYPE") ).equals("1")  &&  ( (String)paramap.get("OUTRIKA") ).equals("1") )
            )
                stb.append(   "AND EXISTS(SELECT 'X' FROM COURSE_DESIREDIV T4 WHERE T4.DESIREDIV = T3.DESIREDIV ) ");
            // -> 付属出身者の出力の有無 受験番号3000は付属出身 => 受験番号順のみ 05/08/10Modify
            // -> 全一覧表において付属出身者と付属出身者以外の選択を追加  05/08/16Modify
            if( ((String)paramap.get("OUTPUT2") ).equals("1") )
                stb.append(   "AND ( INT(T3.EXAMNO) < 3000 OR 4000 <= INT(T3.EXAMNO) ) ");
            else
                stb.append(   "AND ( 3000 <= INT(T3.EXAMNO) AND INT(T3.EXAMNO) <= 3999  ) ");
            stb.append(")");

            //対象受験生を抽出　＊＊内容をメソッドprestatCountExamneeと同様とする＊＊
            //05/12/21 Modify　BASE_B表に志望コースの条件を付けた表
            stb.append(",BASE_A AS( ");
            stb.append(   "SELECT  T3.EXAMNO, T3.NAME, T3.SEX, T3.DESIREDIV, T3.NATPUBPRIDIV ");
            stb.append(   "FROM    BASE_B T3 ");
            // -> ＃４の帳票でコース別の場合対象者を限定する
            // -> ＃２の帳票(前期４科目合計成績順)でコース別の場合対象者を限定する --NO003
            if( ( (String)paramap.get("OUTARAKALT") ).equals("2")  ||  paramap.containsKey("OUTPUT3") )  //05/21/28 Modify
                stb.append("WHERE EXISTS(SELECT 'X' FROM COURSE_DESIREDIV T4 WHERE T4.DESIREDIV = T3.DESIREDIV ) ");
            stb.append(")");

            //志望区分によるコースおよびアラカルト科目の抽出
            stb.append(",ALACALT_SUBCLASS AS(");
            stb.append(   "SELECT  T1.DESIREDIV, T2.TESTSUBCLASSCD, T3.EXAMCOURSE_MARK, T2.ADOPTIONDIV ");
            stb.append(          ",T2.A_TOTAL_FLG, T2.B_TOTAL_FLG ");   //05/12/21
            stb.append(   "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_PERFECT_MST T2, ENTEXAM_COURSE_MST t3 ");
            stb.append(   "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(       "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T2.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(       "AND T2.COURSECD = T1.COURSECD ");
            stb.append(       "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(       "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append(       "AND T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "AND T3.COURSECD = T2.COURSECD ");
            stb.append(       "AND T3.MAJORCD = T2.MAJORCD ");
            stb.append(       "AND T3.EXAMCOURSECD = T2.EXAMCOURSECD ");
            stb.append(")");

            //科目別得点の表
            stb.append(",SUBCLASS_SCORE AS(");
                            //科目別得点および志望区分によるコースの有無
                              //05/12/21 Modify ＩコースはＡ配点、以外はＢ配点
                              //                集計フラグの条件を追加
            stb.append(       "SELECT  T2.EXAMNO, TESTSUBCLASSCD, A_SCORE AS SCORE ");
            stb.append(              ",CASE WHEN (SELECT EXAMCOURSE_MARK FROM ALACALT_SUBCLASS T3 ");
            stb.append(                          "WHERE  T3.DESIREDIV = T2.DESIREDIV AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(                             "AND EXAMCOURSE_MARK='I' AND ADOPTIONDIV = '0' AND A_TOTAL_FLG = '1' ) IS NOT NULL THEN 'I' ELSE NULL END AS ISELECT ");
            stb.append(              ",CASE WHEN (SELECT EXAMCOURSE_MARK FROM ALACALT_SUBCLASS T3 ");
            stb.append(                          "WHERE  T3.DESIREDIV = T2.DESIREDIV AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(                             "AND EXAMCOURSE_MARK='T' AND ADOPTIONDIV = '0' AND B_TOTAL_FLG = '1' ) IS NOT NULL THEN 'T' ELSE NULL END AS TSELECT ");
            stb.append(              ",CASE WHEN (SELECT EXAMCOURSE_MARK FROM ALACALT_SUBCLASS T3 ");
            stb.append(                         "WHERE  T3.DESIREDIV = T2.DESIREDIV AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(                             "AND EXAMCOURSE_MARK='H' AND ADOPTIONDIV = '0' AND B_TOTAL_FLG = '1' ) IS NOT NULL THEN 'H' ELSE NULL END AS HSELECT ");
            stb.append(       "FROM    BASE_B T2, ENTEXAM_SCORE_DAT T1 ");  //05/12/21 Modify BASE_A => BASE_B
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND T1.A_SCORE IS NOT NULL ");
            stb.append(           "AND T2.EXAMNO = T1.EXAMNO ");

                            //Ｉコースのアラカルト科目得点  05/12/21 Build
            stb.append(       "UNION ");
            stb.append(       "SELECT  T2.EXAMNO, 'A' AS TESTSUBCLASSCD, MAX(A_SCORE) AS SCORE ");
            stb.append(              ",EXAMCOURSE_MARK AS ISELECT ");
            stb.append(              ",CASE WHEN T2.EXAMNO IS NOT NULL THEN NULL ELSE '' END AS TSELECT ");
            stb.append(              ",CASE WHEN T2.EXAMNO IS NOT NULL THEN NULL ELSE '' END AS HSELECT ");
            stb.append(       "FROM    BASE_B T2, ENTEXAM_SCORE_DAT T1, ALACALT_SUBCLASS T3 ");  //05/12/21 Modify BASE_A => BASE_B
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND T3.DESIREDIV = T2.DESIREDIV ");
            stb.append(           "AND T3.EXAMCOURSE_MARK = 'I' AND ADOPTIONDIV = '1' ");
            stb.append(           "AND T3.A_TOTAL_FLG = '1' ");
            stb.append(           "AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(           "AND T1.A_SCORE IS NOT NULL ");
            stb.append(           "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(       "GROUP BY T2.EXAMNO, EXAMCOURSE_MARK ");

                            //Ｔコースのアラカルト科目得点
            stb.append(       "UNION ");
            stb.append(       "SELECT  T2.EXAMNO, 'A' AS TESTSUBCLASSCD, MAX(B_SCORE) AS SCORE ");  //05/12/21 A_SCORE => B_SCORE
            stb.append(              ",CASE WHEN T2.EXAMNO IS NOT NULL THEN NULL ELSE '' END AS ISELECT ");
            stb.append(              ",EXAMCOURSE_MARK AS TSELECT ");
            stb.append(              ",CASE WHEN T2.EXAMNO IS NOT NULL THEN NULL ELSE '' END AS HSELECT ");
            stb.append(       "FROM    BASE_B T2, ENTEXAM_SCORE_DAT T1, ALACALT_SUBCLASS T3 ");  //05/12/21 Modify BASE_A => BASE_B
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND T3.DESIREDIV = T2.DESIREDIV ");
            stb.append(           "AND T3.EXAMCOURSE_MARK = 'T' AND ADOPTIONDIV = '1' ");
            stb.append(           "AND T3.B_TOTAL_FLG = '1' ");  //05/12/21
            stb.append(           "AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(           "AND T1.B_SCORE IS NOT NULL ");  //05/12/21 A_SCORE => B_SCORE
            stb.append(           "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(       "GROUP BY T2.EXAMNO, EXAMCOURSE_MARK ");

                            //Ｈコースのアラカルト科目得点
            stb.append(       "UNION ");
            stb.append(       "SELECT  T2.EXAMNO, 'A' AS TESTSUBCLASSCD, MAX(B_SCORE) AS SCORE ");  //05/12/21 A_SCORE => B_SCORE
            stb.append(              ",CASE WHEN T2.EXAMNO IS NOT NULL THEN NULL ELSE '' END AS ISELECT ");
            stb.append(              ",CASE WHEN T2.EXAMNO IS NOT NULL THEN NULL ELSE '' END AS TSELECT ");
            stb.append(              ",EXAMCOURSE_MARK AS HSELECT ");
            stb.append(       "FROM    BASE_B T2, ENTEXAM_SCORE_DAT T1, ALACALT_SUBCLASS T3 ");  //05/12/21 Modify BASE_A => BASE_B
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND T3.DESIREDIV = T2.DESIREDIV ");
            stb.append(           "AND T3.EXAMCOURSE_MARK = 'H' AND ADOPTIONDIV = '1' ");
            stb.append(           "AND T3.B_TOTAL_FLG = '1' ");  //05/12/21
            stb.append(           "AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(           "AND T1.B_SCORE IS NOT NULL ");  //05/12/21 A_SCORE => B_SCORE
            stb.append(           "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(       "GROUP BY T2.EXAMNO, EXAMCOURSE_MARK ");
            stb.append(")");

            //コース別受験科目数のカウント  NO004 Build
            stb.append(",SUBCLASS_CNT AS(");
            stb.append(       "SELECT  EXAMNO ");
            stb.append(              ",SUM(CASE WHEN TESTSUBCLASSCD <> 'A' THEN 1 ELSE NULL END)AS ALLCNT ");
            stb.append(              ",SUM(CASE WHEN ISELECT IS NOT NULL THEN 1 ELSE NULL END)AS ICNT ");
            stb.append(              ",SUM(CASE WHEN TSELECT IS NOT NULL THEN 1 ELSE NULL END)AS TCNT ");
            stb.append(              ",SUM(CASE WHEN HSELECT IS NOT NULL THEN 1 ELSE NULL END)AS HCNT ");
            stb.append(       "FROM    SUBCLASS_SCORE T2 ");
            stb.append(       "GROUP BY EXAMNO ");
            stb.append(") ");

            //合計点の表
            stb.append(",SUM_SCORE AS(");
            stb.append(       "SELECT  EXAMNO ");
            stb.append(              ",SUM( CASE WHEN TESTSUBCLASSCD <> 'A'          THEN SCORE ELSE NULL END ) AS SCORE ");
            stb.append(              ",SUM( CASE WHEN TESTSUBCLASSCD IN('1','2','4') THEN SCORE ELSE NULL END ) AS SCORE_SCIENCE ");
            stb.append(              ",SUM( CASE WHEN ISELECT IS NOT NULL THEN SCORE ELSE NULL END ) AS SCOREI ");
            stb.append(              ",SUM( CASE WHEN TSELECT IS NOT NULL THEN SCORE ELSE NULL END ) AS SCORET ");
            stb.append(              ",SUM( CASE WHEN HSELECT IS NOT NULL THEN SCORE ELSE NULL END ) AS SCOREH ");
            stb.append(       "FROM    SUBCLASS_SCORE ");
            stb.append(       "GROUP BY EXAMNO ");
            stb.append(")");
            
            //全教科席次
            //05/09/05Modify １教科でも抜けがあった場合、４教科順位は対象外
            stb.append(",ALL_RANK AS(");
            stb.append(       "SELECT  EXAMNO, ");
            stb.append(               "RANK() OVER(ORDER BY SCORE DESC) AS RANK ");
            stb.append(       "FROM    SUM_SCORE ");
            stb.append(       "WHERE   SCORE IS NOT NULL ");
            if( ( (String)paramap.get("SORT") ).equals("1") ){
                stb.append(       "AND EXAMNO NOT IN(");
                stb.append(             "SELECT  EXAMNO FROM ENTEXAM_SCORE_DAT ");
                stb.append(             "WHERE   ENTEXAMYEAR='" + (String)paramap.get("YEAR") + "' ");
                stb.append(                 "AND TESTDIV='" + (String)paramap.get("TESTDIV") + "' ");
                stb.append(             "GROUP BY EXAMNO ");
                stb.append(             "HAVING COUNT(*) < 4 ) ");
            }
            stb.append(")");
            
            //理科型席次
            stb.append(",SCIENCE_RANK AS(");
            stb.append(       "SELECT  EXAMNO, ");
            stb.append(               "RANK() OVER(ORDER BY SCORE_SCIENCE DESC) AS RANK ");
            stb.append(       "FROM    SUM_SCORE T1 ");
            stb.append(       "WHERE   SCORE_SCIENCE IS NOT NULL ");

            // -> ＃１の帳票で、理科型は医薬コースのみ順位を表記
            if( ( (String)paramap.get("SORT") ).equals("1") ){
                stb.append(       "AND EXISTS( SELECT  'X'   FROM BASE_A T2, COURSE_DESIREDIV T3 ");
                stb.append(                   "WHERE   T2.DESIREDIV = T3.DESIREDIV AND T2.EXAMNO = T1.EXAMNO ) ");
            }
            stb.append(")");

            
            //アラカルト型席次
            //05/09/05 Modify 後期試験のアラカルト順位は志望区分に「ＴＨ」がある人で「Ｉ」のみの人は対象外
            stb.append(",ALACART_RANK AS(");
            stb.append(       "SELECT  EXAMNO ");
            stb.append(              ",RANK() OVER(ORDER BY CASE WHEN SCORET IS NOT NULL THEN SCORET WHEN SCOREH IS NOT NULL THEN SCOREH ELSE NULL END DESC) AS RANK ");  //05/09/05
            stb.append(       "FROM    SUM_SCORE ");
            stb.append(       "WHERE   SCORET IS NOT NULL OR SCOREH IS NOT NULL  ");  //05/09/05
            stb.append(")");

            //05/12/21 Modify
            stb.append(",ALACART_RANK2 AS(");
            stb.append(       "SELECT  EXAMNO ");
            stb.append(              ",RANK() OVER(ORDER BY CASE WHEN SCORET IS NOT NULL THEN SCORET WHEN SCOREH IS NOT NULL THEN SCOREH ELSE SCOREI END DESC) AS RANK ");
            stb.append(       "FROM    SUM_SCORE ");
            stb.append(       "WHERE   SCOREI IS NOT NULL OR SCORET IS NOT NULL OR SCOREH IS NOT NULL  ");  //05/12/21
            stb.append(")");

            
            //得点表
            stb.append(",SCORE_RECORD AS(");
            stb.append(       "SELECT  T1.EXAMNO ");
            stb.append(              ",SUM(CASE WHEN T2.TESTSUBCLASSCD = '1' THEN T2.SCORE ELSE NULL END)AS SCORE_KOKUGO ");
            stb.append(              ",SUM(CASE WHEN T2.TESTSUBCLASSCD = '2' THEN T2.SCORE ELSE NULL END)AS SCORE_SANSU ");
            stb.append(              ",SUM(CASE WHEN T2.TESTSUBCLASSCD = '3' THEN T2.SCORE ELSE NULL END)AS SCORE_SHAKAI ");
            stb.append(              ",SUM(CASE WHEN T2.TESTSUBCLASSCD = '4' THEN T2.SCORE ELSE NULL END)AS SCORE_RIKA ");
            stb.append(              ",MAX(T3.SCORE)  AS SCORE_ALL ");
            stb.append(              ",MAX(T3.SCORE_SCIENCE) AS SCORE_SCIENCE ");
            stb.append(              ",MAX(T3.SCOREI)AS SCORE_ALACART_I ");
            stb.append(              ",MAX(T3.SCORET)AS SCORE_ALACART_T ");
            stb.append(              ",MAX(T3.SCOREH)AS SCORE_ALACART_H ");
            stb.append(              ",MAX(T4.RANK) AS RANK_ALL ");
            stb.append(              ",MAX(T5.RANK) AS RANK_SCIENCE ");
            stb.append(              ",MAX(T6.RANK) AS RANK_ALACART ");
            stb.append(              ",MAX(T7.RANK) AS RANK2_ALACART ");  //05/12/21
            stb.append(       "FROM   BASE_A T1 ");
            stb.append(       "LEFT JOIN SUBCLASS_SCORE T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append(       "LEFT JOIN SUM_SCORE      T3 ON T3.EXAMNO = T1.EXAMNO ");
            stb.append(       "LEFT JOIN ALL_RANK       T4 ON T4.EXAMNO = T1.EXAMNO ");
            stb.append(       "LEFT JOIN SCIENCE_RANK   T5 ON T5.EXAMNO = T1.EXAMNO ");
            stb.append(       "LEFT JOIN ALACART_RANK   T6 ON T6.EXAMNO = T1.EXAMNO ");
            stb.append(       "LEFT JOIN ALACART_RANK2  T7 ON T7.EXAMNO = T1.EXAMNO ");  //05/12/21
            stb.append(       "GROUP BY T1.EXAMNO ");
            stb.append(")");

            //アラカルト対象科目が社会の受験者の表
            stb.append(",ALACALTSCORE_SUBCLASS AS(");
            stb.append(       "SELECT  T2.EXAMNO ");
            stb.append(       "FROM    BASE_A T2, ENTEXAM_SCORE_DAT T1 ");
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND EXISTS( SELECT  'X'   FROM ALACALT_SUBCLASS T3 ");
            stb.append(                       "WHERE   T3.DESIREDIV = T2.DESIREDIV AND T3.TESTSUBCLASSCD = T1.TESTSUBCLASSCD AND ADOPTIONDIV = '1')");
            stb.append(           "AND T1.A_SCORE IS NOT NULL ");
            stb.append(           "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(      "GROUP BY T2.EXAMNO ");
            stb.append(      "HAVING MAX( CASE WHEN TESTSUBCLASSCD = '3' THEN A_SCORE ELSE NULL END) IS NOT NULL AND ");
            stb.append(             "VALUE(MAX( CASE WHEN TESTSUBCLASSCD <> '3' THEN A_SCORE ELSE NULL END),0) ");
            stb.append(               "< VALUE(MAX( CASE WHEN TESTSUBCLASSCD ='3' THEN A_SCORE ELSE NULL END),0) ");
            stb.append(")");

            //メイン表
            stb.append("SELECT  T1.EXAMNO, T1.NAME, T1.SEX, T1.DESIREDIV, T1.NATPUBPRIDIV ");
            stb.append(       ",SCORE_KOKUGO ");
            stb.append(       ",SCORE_SHAKAI ");
            stb.append(       ",SCORE_SANSU ");
            stb.append(       ",SCORE_RIKA ");
            stb.append(       ",SCORE_ALL ");
            stb.append(       ",SCORE_SCIENCE ");
            stb.append(       ",SCORE_ALACART_I ");
            stb.append(       ",SCORE_ALACART_T ");
            stb.append(       ",SCORE_ALACART_H ");
            stb.append(       ",RANK_ALL ");
            stb.append(       ",RANK_SCIENCE ");
            stb.append(       ",RANK_ALACART ");
            stb.append(       ",T3.EXAMNO AS A_SUBCLSSCD ");
            stb.append(       ",CASE WHEN ALLCNT IS NOT NULL AND ALLCNT < 4 THEN '*' ELSE NULL END AS ALLCNT ");  //NO004 Add
            stb.append(       ",CASE WHEN ICNT IS NOT NULL AND ICNT < 3 THEN '*' ELSE NULL END AS ICNT ");        //NO004 Add
            stb.append(       ",CASE WHEN TCNT IS NOT NULL AND TCNT < 3 THEN '*' ELSE NULL END AS TCNT ");        //NO004 Add
            stb.append(       ",CASE WHEN HCNT IS NOT NULL AND HCNT < 3 THEN '*' ELSE NULL END AS HCNT ");        //NO004 Add
            stb.append("FROM   BASE_A T1 ");
            stb.append("LEFT JOIN SCORE_RECORD T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append("LEFT JOIN ALACALTSCORE_SUBCLASS T3 ON T3.EXAMNO = T1.EXAMNO ");
            stb.append("LEFT JOIN SUBCLASS_CNT T4 ON T4.EXAMNO = T1.EXAMNO ");  //NO004 Add
            if( ( (String)paramap.get("SORT") ).equals("1") )
                stb.append("ORDER BY T1.EXAMNO");
            else if( ( (String)paramap.get("OUTTYPE") ).equals("1") )
                stb.append("ORDER BY RANK_SCIENCE, T1.EXAMNO");
            else if( ( (String)paramap.get("OUTTYPE") ).equals("2") )
                stb.append("ORDER BY RANK2_ALACART, VALUE(RANK_ALACART,10000) , T1.EXAMNO");  //05/12/21 Modify
            else
                stb.append("ORDER BY RANK_ALL, T1.EXAMNO");

        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("ps="+stb.toString());
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  受験人数
     *  ＊＊ 内容をメソッドprestatScoreの共通表COURSE_DESIREDIV,BASE_Aと同様とする
     */
    private String prestatCountExamnee( Map paramap )
    {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append("WITH ");

            //任意のコースを含む志望区分を抽出
            stb.append("COURSE_DESIREDIV AS(");
            stb.append(       "SELECT  T1.DESIREDIV ");
            stb.append(       "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T1.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            // -> ＃４の帳票でコース別出力の場合は、任意のコースを含んだ志望区分とし、以外は医薬進学コースとする
            if( ( (String)paramap.get("OUTARAKALTCOURSE") ).equals("2")  )
                stb.append(       "AND T2.EXAMCOURSE_MARK = 'T' ");
            else if( ( (String)paramap.get("OUTARAKALTCOURSE") ).equals("3")  )
                stb.append(       "AND T2.EXAMCOURSE_MARK = 'H' ");
            else
                stb.append(       "AND T2.EXAMCOURSE_MARK = 'I' ");
            stb.append(           "AND T1.WISHNO = '1' ");   //05/09/05
            stb.append(")");

            stb.append(   "SELECT  SUM(1) AS A_NUMBER ");
            stb.append(          ",SUM( CASE WHEN SEX = '1' THEN 1 ELSE 0 END ) AS M_NUMBER ");
            stb.append(          ",SUM( CASE WHEN SEX = '2' THEN 1 ELSE 0 END ) AS F_NUMBER ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T3 ");
            stb.append(   "WHERE   T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("           AND T3.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            }
            stb.append(       "AND T3.TESTDIV = '" + (String)paramap.get("TESTDIV") + "' ");
            // -> ＃３の帳票で医薬進学希望者のみの場合 || ＃４の帳票でコース別の場合、対象者を限定する
            if( ( ( (String)paramap.get("OUTTYPE") ).equals("1")  &&  ( (String)paramap.get("OUTRIKA") ).equals("1") )  ||
                ( ( (String)paramap.get("OUTARAKALT") ).equals("2") || ( (String)paramap.get("OUTPUT3") ) != null) )    //NO001
                stb.append(   "AND EXISTS(SELECT 'X' FROM COURSE_DESIREDIV T4 WHERE T4.DESIREDIV = T3.DESIREDIV ) ");
            // -> 付属出身者の出力の有無 受験番号3000は付属出身 05/08/10Modify
            // -> 全一覧表において付属出身者と付属出身者以外の選択を追加  05/08/16Modify
            if( ((String)paramap.get("OUTPUT2") ).equals("1") )
                stb.append(   "AND ( INT(T3.EXAMNO) < 3000 OR 4000 <= INT(T3.EXAMNO) ) ");
            else
                stb.append(   "AND ( 3000 <= INT(T3.EXAMNO) AND INT(T3.EXAMNO) <= 3999  ) ");

        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }



/**
 *  帳票別で異なる内容のクラス  標準
 */
private class outDetailCommonClass
{
    /**
     *  明細行印刷  共通項目
     */
    void printsvfOutDetail( DB2UDB db2, Vrw32alp svf, Map paramap, ResultSet arrrs[], PreparedStatement ps, int cnt )
    {
        int i = ( cnt < 50 )? 1 : 2;
        int j = ( 0 < (cnt + 1) % 50  )? (cnt + 1) % 50 : 50;
        try {
            svf.VrsOutn( "DESIREDIV"     + i,  j,  (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV"))) );
            svf.VrsOutn( "ORG_JUDGEMENT" + i,  j,  setConsultation( db2, ps, arrrs ) );

            svf.VrsOutn( "EXAMNO" + i,  j,  arrrs[1].getString("EXAMNO") );

            if( arrrs[1].getString("SEX") != null  &&  arrrs[1].getString("SEX").equals("2") )
                svf.VrsOutn( "SEX"  + i,  j,  "*" );

            if( 20 < knjobj.retStringByteValue( arrrs[1].getString("NAME"), 21 ) )
                svf.VrsOutn( "NAME" + i + "_2",  j,  arrrs[1].getString("NAME") );
            else
                svf.VrsOutn( "NAME" + i + "_1",  j,  arrrs[1].getString("NAME") );

            printsvfOutDetail_A( svf, paramap, arrrs, i, j );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  明細行印刷
     */
    void printsvfOutDetail_A( Vrw32alp svf, Map paramap, ResultSet arrrs[], int i, int j )
    {
        try {
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷  共通項目
     */
    void printsvfOutHead( Vrw32alp svf, Map paramap )
    {

        try {
            svf.VrsOut( "NENDO",     (String)paramap.get("NENDO") );
            svf.VrsOut( "SCHOOLDIV", (String)paramap.get("SCHOOLDIVNAME") );
            svf.VrsOut( "TESTDIV",   (String)paramap.get("TESTDIVNAME") );
            svf.VrsOut( "DATE",      (String)paramap.get("NOW_DATE") );
            svf.VrsOut( "PAGE",      String.valueOf( ++pagecount ) );
            svf.VrsOut( "TOTAL_PAGE",(String)paramap.get("TOTALPAGE") );

            printsvfOutHead_A( svf, paramap );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷
     */
    void printsvfOutHead_A( Vrw32alp svf, Map paramap )
    {
        try {
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}


/**
 *  帳票別で異なる内容のクラス  ?１（受験番号順）
 */
private class outDetailSub1Class extends outDetailCommonClass
{
    /**
     *  帳票明細行印刷
     */
    void printsvfOutDetail_A( Vrw32alp svf, Map paramap, ResultSet arrrs[], int i, int j )
    {
        try {
            if( arrrs[1].getString("NATPUBPRIDIV") != null  &&  arrrs[1].getString("NATPUBPRIDIV").equals("9") )  //05/08/16Revive
                svf.VrsOutn( "ATTACH"      + i,         j,  "*" );  //付属 05/09/01Build

            svf.VrsOutn( "POINT" + i + "_1",  j,  arrrs[1].getString("SCORE_KOKUGO") );
            svf.VrsOutn( "POINT" + i + "_2",  j,  arrrs[1].getString("SCORE_SANSU") );
            svf.VrsOutn( "POINT" + i + "_3",  j,  arrrs[1].getString("SCORE_SHAKAI") );
            svf.VrsOutn( "POINT" + i + "_4",  j,  arrrs[1].getString("SCORE_RIKA") );

            svf.VrsOutn( "4TOTAL" + i,  j,  arrrs[1].getString("SCORE_ALL") );
            svf.VrsOutn( "4ORDER" + i,  j,  arrrs[1].getString("RANK_ALL") );

            svf.VrsOutn( "RTOTAL" + i,  j,  arrrs[1].getString("SCORE_SCIENCE") );
            svf.VrsOutn( "RORDER" + i,  j,  arrrs[1].getString("RANK_SCIENCE") );
            
            if( arrrs[1].getString("SCORE_SCIENCE") != null    ||
                    arrrs[1].getString("SCORE_ALACART_T") != null  ||
                    arrrs[1].getString("SCORE_ALACART_H") != null    ){       //05/09/05条件文追加
                int intI = ( arrrs[1].getString("SCORE_SCIENCE")   != null )? Integer.parseInt( arrrs[1].getString("SCORE_SCIENCE")   ) : 0;
                int intT = ( arrrs[1].getString("SCORE_ALACART_T") != null )? Integer.parseInt( arrrs[1].getString("SCORE_ALACART_T") ) : 0;
                int intH = ( arrrs[1].getString("SCORE_ALACART_H") != null )? Integer.parseInt( arrrs[1].getString("SCORE_ALACART_H") ) : 0;
                svf.VrsOutn( "ATOTAL" + i,  j,  String.valueOf( ( ( intT <= intI )? ( intH <= intI )? intI : intH : ( intH <= intT )? intT : intH ) ) );
            }
            svf.VrsOutn( "AORDER" + i,  j,  arrrs[1].getString("RANK_ALACART") );
            
            svf.VrsOutn( "MARK" + i + "_2",  j,  arrrs[1].getString("ICNT")   );  //未受験有り --NO004
            if( arrrs[1].getString("TCNT") != null ) {
                svf.VrsOutn( "MARK" + i + "_3",  j,  arrrs[1].getString("TCNT") );  //未受験有り --NO004
            } else if( arrrs[1].getString("HCNT") != null ) {
                svf.VrsOutn( "MARK" + i + "_3",  j,  arrrs[1].getString("HCNT") );  //未受験有り --NO004
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷
     */
    void printsvfOutHead_A( Vrw32alp svf, Map paramap )
    {
        //NO002
        try {
            svf.VrsOut( "SUBTITLE", "（受験番号順）" );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}


/**
 *  帳票別で異なる内容のクラス  ?２（全体成績順）
 */
private class outDetailSub2Class extends outDetailCommonClass
{
    /**
     *  帳票明細行印刷
     */
    void printsvfOutDetail_A( Vrw32alp svf, Map paramap, ResultSet arrrs[], int i, int j )
    {
        try {
            if( arrrs[1].getString("NATPUBPRIDIV") != null  &&  arrrs[1].getString("NATPUBPRIDIV").equals("9") )  //05/08/16Revive
            //05/08/16Delete if( arrrs[1].getString("EXAMNO") != null  &&  3000 <= Integer.parseInt( arrrs[1].getString("EXAMNO") ) )
                svf.VrsOutn( "ATTACH"  + i,         j,  "*" );  //付属

            svf.VrsOutn( "TOTAL_ORDER" + i,         j,  arrrs[1].getString("RANK_ALL")  );  //全体順位

            String str = (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV")));
            if( -1 < str.indexOf('I') )
            {
                svf.VrsOutn( "POINT" + i + "_1",  j,  arrrs[1].getString("SCORE_ALL") );  //医薬進学得点
                svf.VrsOutn( "MARK"  + i + "_1",  j,  arrrs[1].getString("ALLCNT")      );  //未受験科目有り --NO004
            }
            if( -1 < str.indexOf('T') )
            {
                svf.VrsOutn( "POINT" + i + "_2",  j,  arrrs[1].getString("SCORE_ALL") );  //英数特進得点
                svf.VrsOutn( "MARK"  + i + "_2",  j,  arrrs[1].getString("ALLCNT")      );  //未受験科目有り --NO004
            }
            if( -1 < str.indexOf('H') )
            {
                svf.VrsOutn( "POINT" + i + "_3",  j,  arrrs[1].getString("SCORE_ALL") );  //英数標準得点
                svf.VrsOutn( "MARK"  + i + "_3",  j,  arrrs[1].getString("ALLCNT")      );  //未受験科目有り --NO004
            }
            if( arrrs[1].getString("SCORE_ALL") == null )
                svf.VrsOutn( "TOTAL_ORDER" + i,  j,  "欠"  );  //全体順位
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷
     */
    void printsvfOutHead_A( Vrw32alp svf, Map paramap )
    {
        //NO002
        try {
            if (null != paramap.get("OUTPUT3")){
                svf.VrsOut( "SUBTITLE", "（４科目合計成績順 "+paramap.get("COURSENAME"+(String)paramap.get("OUTARAKALTCOURSE"))+"）" );
            }else {
                svf.VrsOut( "SUBTITLE", "（４科目合計成績順）" );
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}


/**
 *  帳票別で異なる内容のクラス  ?３（理科型成績順）
 */
private class outDetailSub3Class extends outDetailCommonClass
{
    /**
     *  帳票明細行印刷
     */
    void printsvfOutDetail_A( Vrw32alp svf, Map paramap, ResultSet arrrs[], int i, int j )
    {
        try {
            if( arrrs[1].getString("NATPUBPRIDIV") != null  &&  arrrs[1].getString("NATPUBPRIDIV").equals("9") )  //05/08/16Revive
                svf.VrsOutn( "ATTACH"  + i,   j,  "*" );  //付属

            if( arrrs[1].getString("A_SUBCLSSCD") != null )
                svf.VrsOutn( "ALACARTE" + i,  j,  "*" );  //アラカルト（社）

            svf.VrsOutn( "ORDER" + i,   j,  arrrs[1].getString("RANK_SCIENCE")  );  //理科型順位

            String str = (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV")));
            if( -1 < str.indexOf('I') ){
                svf.VrsOutn( "POINT" + i + "_1",  j,  arrrs[1].getString("SCORE_SCIENCE")   );  //医薬進学得点
                svf.VrsOutn( "MARK"  + i + "_1",  j,  arrrs[1].getString("ICNT")            );  //未受験科目有り --NO004
            }
            if( -1 < str.indexOf('T') ){
                svf.VrsOutn( "POINT" + i + "_2",  j,  arrrs[1].getString("SCORE_ALACART_T") );  //英数特進得点
                svf.VrsOutn( "MARK"  + i + "_2",  j,  arrrs[1].getString("TCNT")            );  //未受験科目有り --NO004
            }
            if( -1 < str.indexOf('H') ){
                svf.VrsOutn( "POINT" + i + "_3",  j,  arrrs[1].getString("SCORE_ALACART_H") );  //英数標準得点
                svf.VrsOutn( "MARK"  + i + "_3",  j,  arrrs[1].getString("HCNT")            );  //未受験科目有り --NO004
            }

            if( arrrs[1].getString("SCORE_ALL") == null )
                svf.VrsOutn( "ORDER" + i,         j,  "欠"  );  //理科型順位
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷
     */
    void printsvfOutHead_A( Vrw32alp svf, Map paramap )
    {
        try {
            if( ( (String)paramap.get("OUTRIKA") ).equals("1") ){
                svf.VrsOut( "TEST_TYPE",  ( paramap.containsKey("OUTPUT") )? "理科型" : "Ｒ" );
                svf.VrsOut( "EXAMCOURSE", " 医薬進学志願者" );
            }else{
                svf.VrsOut( "TEST_TYPE",  ( paramap.containsKey("OUTPUT") )? "理科型" : "Ｒ" );
                svf.VrsOut( "EXAMCOURSE", "" );
            }

            svf.VrsOut( "ORDER_ITEM1", ( paramap.containsKey("OUTPUT") )? "理科" : "Ｒ" );
            svf.VrsOut( "ORDER_ITEM2", ( paramap.containsKey("OUTPUT") )? "理科" : "Ｒ" );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}


/**
 *  帳票別で異なる内容のクラス  ?４（アラカルト型成績順）
 */
private class outDetailSub4Class extends outDetailCommonClass
{
    /**
     *  帳票明細行印刷
     */
    void printsvfOutDetail_A( Vrw32alp svf, Map paramap, ResultSet arrrs[], int i, int j )
    {
        try {
            if( arrrs[1].getString("NATPUBPRIDIV") != null  &&  arrrs[1].getString("NATPUBPRIDIV").equals("9") )  //05/08/16Revive
                svf.VrsOutn( "ATTACH"      + i,         j,  "*" );  //付属

            if( arrrs[1].getString("A_SUBCLSSCD") != null )
                svf.VrsOutn( "ALACARTE" + i,  j,  "*" );  //アラカルト（社）

            svf.VrsOutn( "ORDER" + i,  j,  arrrs[1].getString("RANK_ALACART")  );  //アラカルト型順位

            String str = (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV")));
            if( -1 < str.indexOf('I') )
            {
                svf.VrsOutn( "POINT" + i + "_1",  j,  arrrs[1].getString("SCORE_SCIENCE")   );  //医薬進学得点
                svf.VrsOutn( "MARK"  + i + "_1",  j,  arrrs[1].getString("ICNT")            );  //未受験科目有り --NO004
            }
            if( -1 < str.indexOf('T') )
            {
                svf.VrsOutn( "POINT" + i + "_2",  j,  arrrs[1].getString("SCORE_ALACART_T") );  //英数特進得点
                svf.VrsOutn( "MARK"  + i + "_2",  j,  arrrs[1].getString("TCNT")            );  //未受験科目有り --NO004
            }
            if( -1 < str.indexOf('H') )
            {
                svf.VrsOutn( "POINT" + i + "_3",  j,  arrrs[1].getString("SCORE_ALACART_H") );  //英数標準得点
                svf.VrsOutn( "MARK"  + i + "_3",  j,  arrrs[1].getString("HCNT")            );  //未受験科目有り --NO004
            }

            if( arrrs[1].getString("SCORE_ALL") == null )
                svf.VrsOutn( "ORDER" + i,         j,  "欠"  );  //アラカルト型順位
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷
     */
    void printsvfOutHead_A( Vrw32alp svf, Map paramap )
    {
        try {
            if( ( (String)paramap.get("OUTARAKALT") ).equals("1") ){
                svf.VrsOut( "TEST_TYPE",  ( paramap.containsKey("OUTPUT") )? "アラカルト型" : "Ａ" );
                svf.VrsOut( "EXAMCOURSE", "" );
            }else{
                svf.VrsOut( "TEST_TYPE",  ( paramap.containsKey("OUTPUT") )? "アラカルト型" : "Ａ" );
                svf.VrsOut( "EXAMCOURSE", ( ( ( (String)paramap.get("OUTARAKALTCOURSE") ).equals("3") )? " 英数標準志願者" : 
                                                  ( ( (String)paramap.get("OUTARAKALTCOURSE") ).equals("2") )? " 英数特進志願者" : " 医薬進学志願者" ) );
            }

            svf.VrsOut( "ORDER_ITEM1", ( paramap.containsKey("OUTPUT") )? "Ａ" : "Ａ" );
            svf.VrsOut( "ORDER_ITEM2", ( paramap.containsKey("OUTPUT") )? "Ａ" : "Ａ" );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}


}
