// kanji=漢字
/*
 * $Id: 533fd6cee981135d1085e74028b3f9490051bbf1 $
 *
 * 作成日: 2007/12/18 11:50:26 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */

/**
 *
 *  学校教育システム 賢者 [入試管理] 成績一覧表（高校用）
 *
 *  2005/09/02 Build
 *  NO001 m-yama スポーツ推薦者追加、全コース出力追加、名前・受験番号出力を個別に指定。 2005/11/14
 *  2006/01/31 NO002 o-naka 専願と併願の両方がＯＮの場合は、全体（専願・併願を混在）で出力するように変更
 *  2007/04/16 NO003 m-yama KNJL327よりコールされた場合の処理を追加。
 *  2007/12/17 NO004 m-yama K2希望者を追加。
 *
 *
 */

package servletpack.KNJL;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
//import Param;

public class KNJL326K
{
    private static final Log log = LogFactory.getLog(KNJL326K.class);

    private KNJEditString knjobj = new KNJEditString();
    private Map desiredivmap;                               //志望区分表示用Map
    private Map examcoursemap;                              //受験コースMap
    private KNJL326K.outDetailCommonClass outobj;           //各出力様式のクラス
    private int pagecount;
    private StringBuffer stb = null;


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

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, Map paramap) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
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
     *  get parameter doGet()パラメータ受け取り 
     *
     *  引数の説明
     *      "YEAR",       年度                 
     *      "OUTPUT2"     出力対象             1.一般受験者  2.中高一貫者  3.クラブ推薦者  4.K2希望者
     *      "SORT"        出力順序             1.受験番号順  2.成績順
     *      "OUTTYPE"     コース               1.理数科  2.国際コース  3.特進コース  4.進学コース  5.全コース NO001
     *      "SENGAN"                           on.専願
     *      "HEIGAN"                           on.併願
     *      "OUTPUT"                           on.名前・受験番号出力
     *
     *
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap )
    {
//Param.dispParam( request );
        try {
            paramap.put( "PRGID",      request.getParameter("PRGID")  );    //NO003
            paramap.put( "YEAR",       request.getParameter("YEAR")   );
            paramap.put( "OUTPUT2",    request.getParameter("OUTPUT2"));
            //NO003
            if (((String) paramap.get("PRGID")).equals("KNJL327") && request.getParameter("OUTPUT2").equals("0")) {
                paramap.put( "OUTPUT2",    "1");
            }

            paramap.put( "SORT",       request.getParameter("SORT")   );
            paramap.put( "OUTTYPE",    request.getParameter("OUTTYPE"));

            if( request.getParameter("SENGAN") != null )
                paramap.put( "SENGAN",     request.getParameter("SENGAN") );
            if( request.getParameter("HEIGAN") != null )
                paramap.put( "HEIGAN",     request.getParameter("HEIGAN") );

            //NO001
            if( request.getParameter("OUTPUTNAME") != null )
                paramap.put( "OUTPUTNAME",     request.getParameter("OUTPUTNAME") );
            if( request.getParameter("OUTPUTEXAM") != null )
                paramap.put( "OUTPUTEXAM",     request.getParameter("OUTPUTEXAM") );

            //NO003
            if (((String) paramap.get("PRGID")).equals("KNJL327")) {
                final List paraList = new ArrayList();
                String[] examin = request.getParameterValues("DATA_SELECTED");
                String inSep = "";
                StringBuffer inState  = new StringBuffer();
                inState.append(" IN (");
                for (int exacnt = 0; exacnt < examin.length; exacnt++) {
                    inState.append(inSep + "'" + examin[exacnt] + "'");
                    inSep = ",";
                    paraList.add(examin[exacnt]);
                }
                inState.append(") ");
                paramap.put( "EXAMIN",     inState.toString() );
                paramap.put("EXAM_SORT", paraList);
            }
            
            paramap.put("SPECIAL_REASON_DIV", request.getParameter("SPECIAL_REASON_DIV"));
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        } finally {
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
            examcoursemap = setMapExamcourse( db2, paramap );       //受験コースMap作成
            desiredivmap = setMapDesirediv( db2, paramap );         //志望区分表示用Map作成
            setSvfformFormat( db2, svf, paramap );                       //SVF-FORM設定

            setPreparedstatement( db2, paramap, arrps );            //PreparedStatement作成

//NO002
//            if( paramap.containsKey("SENGAN") ){
            if( paramap.containsKey("SENGAN") && !paramap.containsKey("HEIGAN") ){
                paramap.put( "SHDIV",  new String("1")   );
                if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;  //SVF-FORM印刷
            }

//NO002
//            if( paramap.containsKey("HEIGAN") ){
            if( !paramap.containsKey("SENGAN") && paramap.containsKey("HEIGAN") ){
                if( paramap.containsKey("SHDIV") )paramap.remove("SHDIV");
                paramap.put( "SHDIV",  new String("2")   );
                if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;  //SVF-FORM印刷
            }

//NO002
            if( paramap.containsKey("SENGAN") && paramap.containsKey("HEIGAN") ){
                if (((String) paramap.get("PRGID")).equals("KNJL327")) {
                    if( printsvfMain327( db2, svf, paramap, arrps ) )nonedata = true;  //SVF-FORM印刷
                } else {
                    if( printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;  //SVF-FORM印刷
                }
            }

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
     *  印刷処理
     *
     */
    private boolean printsvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        pagecount = 0;
        ResultSet arrrs[] = new ResultSet[2];
        int cnt = 0;            //出力件数カウント

        try {
            setHeadItem( db2, paramap, arrps[2], arrrs[0] );                //帳票見出し項目設定
            int pp = 0;
            if (Integer.parseInt( (String)paramap.get("OUTTYPE") ) < 5 )    //NO001
                arrps[1].setString( ++pp,  (String)paramap.get("EXAMCOURSEMARK") );
            if( !paramap.containsKey("SENGAN") || !paramap.containsKey("HEIGAN") )  //NO002
                arrps[1].setString( ++pp,  (String)paramap.get("SHDIV") );
            arrrs[1] = arrps[1].executeQuery();                 //受験生別得点
            while( arrrs[1].next() ){
                if( 100 == cnt ){
                    svf.VrEndPage();
                    nonedata = true;
                    cnt = 0;
                }
                if( 0 == cnt )outobj.printsvfOutHead( db2, svf, paramap );           //SVF-FORM印刷
                outobj.printsvfOutDetail( db2, svf, paramap, arrrs, arrps[0], cnt );        //SVF-FORM印刷
                cnt++;
            }
            if( 0 == cnt )outobj.printsvfOutHead( db2, svf, paramap );                   //SVF-FORM印刷
            //printsvfOutTotal( svf, paramap );                //SVF-FORM印刷
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
     *  印刷処理
     *
     */
    private boolean printsvfMain327( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        pagecount = 0;
        ResultSet arrrs[] = new ResultSet[2];
        int cnt = 0;            //出力件数カウント
        final Map outPutData = getOutPutData(db2, paramap, arrps[1]);

        try {
            setHeadItem( db2, paramap, arrps[2], arrrs[0] );                //帳票見出し項目設定
            for (final Iterator iter = ((List) paramap.get("EXAM_SORT")).iterator(); iter.hasNext();) {
                if( 100 == cnt ){
                    svf.VrEndPage();
                    nonedata = true;
                    cnt = 0;
                }
                final String examno = (String) iter.next();
                final OutPutData data = (OutPutData) outPutData.get(examno);
                if( 0 == cnt )outobj.printsvfOutHead( db2, svf, paramap );           //SVF-FORM印刷
                outobj.printsvfOutDetail327(db2, svf, paramap, arrrs, arrps[0], cnt, data);        //SVF-FORM印刷
                cnt++;
            }
            if ( 0 == cnt ) {
                outobj.printsvfOutHead(db2, svf, paramap);                   //SVF-FORM印刷
            }
            //printsvfOutTotal( svf, paramap );                //SVF-FORM印刷
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
     * 327出力データを返す
     */
    private Map getOutPutData(final DB2UDB db2, final Map paramap, final PreparedStatement statement) {
        final Map rtn = new HashMap();
        ResultSet rs = null;
        try {
            int pp = 0;
            if (Integer.parseInt((String) paramap.get("OUTTYPE")) < 5) {
                statement.setString(++pp, (String) paramap.get("EXAMCOURSEMARK"));
            }
            if (!paramap.containsKey("SENGAN") || !paramap.containsKey("HEIGAN")) {
                statement.setString(++pp, (String) paramap.get("SHDIV"));
            }
            rs = statement.executeQuery(); //受験生別得点
            while (rs.next()) {
                OutPutData data = new OutPutData(rs.getString("EXAMNO"),
                        rs.getString("NAME"),
                        rs.getString("SEX"),
                        rs.getString("DESIREDIV"),
                        rs.getString("NATPUBPRIDIV"),
                        rs.getString("SUMSCORE_A"),
                        rs.getString("SUMSCORE_B"),
                        rs.getString("RANK_A"),
                        rs.getString("RANK_B")
                        );
                rtn.put(rs.getString("EXAMNO"), data);
            }
        } catch (final Exception e) {
            log.error("error! ", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtn;
    }

    /**
     * 327出力データクラス
     */
    private class OutPutData
    {
        private final String _examno;
        private final String _name;
        private final String _sex;
        private final String _desirediv;
        private final String _natpub;
        private final String _sumscoreA;
        private final String _sumscoreB;
        private final String _rankA;
        private final String _rankB;

        OutPutData(final String examno,
                final String name,
                final String sex,
                final String desirediv,
                final String natpubpridiv,
                final String sumscore_a,
                final String sumscore_b,
                final String rank_a,
                final String rank_b
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
            _desirediv = desirediv;
            _natpub = natpubpridiv;
            _sumscoreA = sumscore_a;
            _sumscoreB = sumscore_b;
            _rankA = rank_a;
            _rankB = rank_b;
        }
        
        public String toString() {
            return "受験番号 = " + _examno
                  + " 氏名 = " + _name
                  + " 性別 = " + _sex;
        }
    }

    /**
     *  帳票見出し項目設定
     *      paramap
     *  
     */
    private void setHeadItem( DB2UDB db2, Map paramap, PreparedStatement ps, ResultSet rs )
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

        //受験者数取得および総ページ数
        try {
            /* ***
            if( paramap.containsKey( "A_NUMBER" ) ){
                paramap.remove( "A_NUMBER" );
                paramap.remove( "M_NUMBER" );
                paramap.remove( "F_NUMBER" );
                paramap.remove( "TOTALPAGE" );
            }
            ***** */
            ps = db2.prepareStatement( prestatCountExamnee( paramap ) );    //受験生人数
            int pp = 0;
            if (Integer.parseInt( (String)paramap.get("OUTTYPE") ) < 5 )    //NO001
                ps.setString( ++pp,  (String)paramap.get("EXAMCOURSEMARK") );
            if( !paramap.containsKey("SENGAN") || !paramap.containsKey("HEIGAN") )  //NO002
                ps.setString( ++pp,  (String)paramap.get("SHDIV") );
            rs = ps.executeQuery();                 //受験生人数

            if( rs.next() ){
                if( rs.getString("A_NUMBER") != null ){
                    //paramap.put( "A_NUMBER",  rs.getString("A_NUMBER") );     //受験者数
                    //paramap.put( "M_NUMBER",  rs.getString("M_NUMBER") );     //男子受験者数
                    //paramap.put( "F_NUMBER",  rs.getString("F_NUMBER") );     //女子受験者数
                    int intx = Integer.parseInt( rs.getString("A_NUMBER") );
                    paramap.put( "TOTALPAGE", String.valueOf( ( 0 < intx % 100 )? intx / 100 + 1 : intx / 100 )  );     //総ページ数
                }
            }
            /* *****
            if( ! paramap.containsKey( "A_NUMBER") ){
                paramap.put( "A_NUMBER",  new String("0") );        //受験者数
                paramap.put( "M_NUMBER",  new String("0") );        //男子受験者数
                paramap.put( "F_NUMBER",  new String("0") );        //女子受験者数
            }
            ***** */
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
            if( stb == null )stb = new StringBuffer();
            else             stb.delete( 0, stb.length() );
            stb.append( nao_package.KenjaProperties.gengou(Integer.parseInt( sdf.format(date) )) );
            sdf = new SimpleDateFormat("年M月d日");
            stb.append( sdf.format(date) );
            paramap.put( "NOW_DATE",  stb.toString() );     //作成日
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
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );

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
            svf.VrSetForm( "KNJL326.frm", 1 );
            outobj = new outDetailCommonClass();
        } catch( Exception ex ){
            log.error("error! ",ex);
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
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            int pp = 0;
            stb.append("----");
            ps.setString( ++pp,  arrrs[1].getString("EXAMNO") );
            arrrs[0] = ps.executeQuery();
            Integer intei = null;
//log.debug("EXAMNO = " + arrrs[1].getString("EXAMNO") +  "   ps=" + ps.toString() );
            while ( arrrs[0].next() ){
                intei = (Integer)examcoursemap.get( arrrs[0].getString("EXAMCOURSE_MARK") );
                if( intei == null )continue;
                if( 0 < intei.intValue()  &&  intei.intValue() <= 4  &&  arrrs[0].getString("JUDGEMENT") != null ){
                    stb.replace( intei.intValue() - 1, intei.intValue(), arrrs[0].getString("JUDGEMENT") );
                }
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
     *  事前判定設定
     *
     */
    private String setConsultation327( DB2UDB db2, PreparedStatement ps, ResultSet arrrs[], OutPutData data )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            int pp = 0;
            stb.append("----");
            ps.setString( ++pp,  data._examno );
            arrrs[0] = ps.executeQuery();
            Integer intei = null;
//log.debug("EXAMNO = " + arrrs[1].getString("EXAMNO") +  "   ps=" + ps.toString() );
            while ( arrrs[0].next() ){
                intei = (Integer)examcoursemap.get( arrrs[0].getString("EXAMCOURSE_MARK") );
                if( intei == null )continue;
                if( 0 < intei.intValue()  &&  intei.intValue() <= 4  &&  arrrs[0].getString("JUDGEMENT") != null ){
                    stb.replace( intei.intValue() - 1, intei.intValue(), arrrs[0].getString("JUDGEMENT") );
                }
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
            try {
log.debug("examno=" + data._examno);
log.debug("ps=" + ps.toString());
            } catch( Exception ex2 ){
                log.error("error! ",ex2);
            }
        }
        return stb.toString();
    }


    /**
     *  MAP作成  受験コースMap作成
     *
     */
    private Map setMapExamcourse( DB2UDB db2, Map paramap )
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map retmap = new HashMap();

        //受験コースの全種類をマップへ保管
        try {
            ps = db2.prepareStatement( prestatCouseMark( paramap ) );    //受験コース
            rs = ps.executeQuery();
            int i = 0;
            while( rs.next() ){
                retmap.put( rs.getString("EXAMCOURSE_MARK"), new Integer(++i) );
                //出力対象コースの名称等を保管
                if( Integer.parseInt( (String)paramap.get("OUTTYPE") ) == i ){
                    paramap.put( "EXAMCOURSEMARK", rs.getString("EXAMCOURSE_MARK") );
                    paramap.put( "EXAMCOURSENAME", rs.getString("EXAMCOURSE_NAME") );
                }
                //各コースの満点を保管
                if( 2 < i )paramap.put( "PERFECT" + i, rs.getString("A_PERFECT") );
                else       paramap.put( "PERFECT" + i, rs.getString("B_PERFECT") );
            }
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
log.debug("paramap="+paramap);
        return retmap;
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

        //志望区分別受験コース編集 ==> 志望番号の昇順
        try {
            String desirediv = null;
            ps = db2.prepareStatement( prestatDesirediv( paramap ) );    //志望区分設定
            rs = ps.executeQuery();
log.debug("examcoursemap="+examcoursemap);
            if( stb == null )stb = new StringBuffer();
            else             stb.delete( 0, stb.length() );
            while( rs.next() ){
                //志望区分の変わり目で、志望区分表記用MAPに保管する
                if( ! rs.getString("DESIREDIV").equals( desirediv ) ){
                    if( desirediv != null ){
                        retmap.put( desirediv, stb.toString() );
                        stb.delete(0, stb.length());
                    }
                    desirediv = rs.getString("DESIREDIV");
                }
                //コースマークを編集（定位置）
                if( examcoursemap.containsKey( rs.getString("EXAMCOURSE_MARK") ) ){
                    for( int i = 0 ; stb.length() < ((Integer)examcoursemap.get( rs.getString("EXAMCOURSE_MARK") )).intValue() - 1 && i < 3 ; i++ )stb.append(" ");
                    stb.append( rs.getString("EXAMCOURSE_MARK") );
                }
            }
            //志望区分の変わり目で、志望区分表記用MAPに保管する
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
     *  priparedstatement作成  受験コース
     *  ==> 全受験コースのマーク・名称・満点を抽出。並び順は（課程・学科・コースコード）の降順
     *
     */
    private String prestatCouseMark( Map paramap )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            stb.append("SELECT  EXAMCOURSE_MARK, EXAMCOURSE_NAME ");
            stb.append(       ",A_PERFECT, B_PERFECT ");
            stb.append("FROM    ENTEXAM_COURSE_MST T1 ");
            stb.append("LEFT JOIN(");
            stb.append(   "SELECT  COURSECD,MAJORCD,EXAMCOURSECD ");
            stb.append(          ",SUM(A_PERFECT) AS A_PERFECT ");
            stb.append(          ",SUM(B_PERFECT) AS B_PERFECT ");
            stb.append(   "FROM    ENTEXAM_PERFECT_MST ");
            stb.append(   "WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(   "GROUP BY COURSECD,MAJORCD,EXAMCOURSECD ");
            stb.append(") T2 ON T1.COURSECD = T2.COURSECD AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(                                  "AND T1.EXAMCOURSECD = T2.EXAMCOURSECD ");
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append("ORDER BY T1.COURSECD||T1.MAJORCD||T1.EXAMCOURSECD DESC ");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  志望区分
     *  ==> 志望区分別コースを抽出。並び順は（志望区分・志望番号）の昇順
     *
     */
    private String prestatDesirediv( Map paramap )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            stb.append("SELECT  T1.DESIREDIV, T2.EXAMCOURSE_MARK ");
            stb.append("FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
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
     *  priparedstatement作成  受験生別事前判定
     *  ==> 任意の受験生において、各コースに対応した事前判定を抽出。並び順は考慮しない。
     *  2005/08/16 Modify 事前判定の取得仕様を変更  SQLはm.yamashiro君作成
     */
    private String prestatConsultation( Map paramap )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            stb.append("SELECT  T1.EXAMCOURSE_MARK ");
            stb.append(       ",(SELECT NAME1 FROM NAME_MST T4 WHERE T4.NAMECD1 = 'L002' AND T4.NAMECD2 = T4.JUDGEMENT) AS JUDGEMENT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T3 ");
            stb.append("INNER   JOIN ENTEXAM_APPLICANTCONS_DAT T4 ");
            stb.append(              "ON T4.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(             "AND T4.EXAMNO = T3.EXAMNO ");
            stb.append(             "AND T4.SHDIV = T3.SHDIV ");
            stb.append("LEFT    JOIN ENTEXAM_COURSE_MST T1 ");
            stb.append(              "ON T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(             "AND T1.COURSECD = T4.COURSECD ");
            stb.append(             "AND T1.MAJORCD = T4.MAJORCD ");
            stb.append(             "AND T1.EXAMCOURSECD = T4.EXAMCOURSECD ");
            stb.append("WHERE  T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("   AND T3.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            }
            stb.append(   "AND T3.EXAMNO = ? ");
            stb.append("ORDER BY T1.EXAMCOURSECD ");
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  受験生別得点
     *
     */
    private String prestatScore( Map paramap )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
log.debug("paramap="+paramap);
        try {
            stb.append("WITH ");

            //任意のコースを含む志望区分を抽出　＊＊内容をメソッドprestatCountExamneeと同様とする＊＊
            stb.append("COURSE_DESIREDIV AS(");
            stb.append(       "SELECT  T1.DESIREDIV ");
            stb.append(       "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            if (Integer.parseInt( (String)paramap.get("OUTTYPE") ) < 5 )    //NO001
                stb.append(           "AND T2.EXAMCOURSE_MARK = ? ");
            stb.append(           "AND T1.WISHNO = '1' ");
            stb.append(")");

            //対象受験生を抽出　＊＊内容をメソッドprestatCountExamneeと同様とする＊＊
            stb.append(",BASE_A AS( ");
            stb.append(   "SELECT  T3.EXAMNO, T3.NAME, T3.SEX, T3.DESIREDIV, T3.NATPUBPRIDIV ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T3 ");
            stb.append(   "WHERE   T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("   AND T3.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            }
            if( !paramap.containsKey("SENGAN") || !paramap.containsKey("HEIGAN") )  //NO002
                stb.append(       "AND T3.SHDIV = ? ");
            stb.append(       "AND EXISTS(SELECT 'X' FROM COURSE_DESIREDIV T4 WHERE T4.DESIREDIV = T3.DESIREDIV ) ");
            // -> 付属出身者の出力の有無 受験番号5000は付属出身 NO001 スポーツ推薦
            if( ((String)paramap.get("OUTPUT2") ).equals("1") ){
                stb.append(   "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            }else if ( ((String)paramap.get("OUTPUT2") ).equals("3") ){
                stb.append(   "AND APPLICANTDIV = '3' ");
            }else if ( ((String)paramap.get("OUTPUT2") ).equals("4") ){
                //NO004
                stb.append(   "AND SCALASHIPDIV = '02' ");
            }else if ( !((String)paramap.get("OUTPUT2") ).equals("5") ){
                stb.append(   "AND EXAMNO BETWEEN '5000' AND '5999' ");
            }
            //NO003
            if (((String) paramap.get("PRGID")).equals("KNJL327")) {
                stb.append(   "AND EXAMNO " + (String) paramap.get("EXAMIN") + " ");
            }

            stb.append(")");
            
            //合計点の表
            stb.append(",SUM_SCORE AS(");
            stb.append(       "SELECT  T1.EXAMNO ");
            stb.append(              ",SUM(A_SCORE) AS SUMSCORE_A ");
            stb.append(              ",SUM(B_SCORE) AS SUMSCORE_B ");
            stb.append(       "FROM    ENTEXAM_SCORE_DAT T1 ");
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(       "GROUP BY T1.EXAMNO ");
            stb.append(       "HAVING T1.EXAMNO IN(SELECT EXAMNO FROM BASE_A) ");
            stb.append(")");
            
            //Ａ得点席次
            stb.append(",RANK_A_SCORE AS(");
            stb.append(       "SELECT  EXAMNO, ");
            stb.append(               "RANK() OVER(ORDER BY SUMSCORE_A DESC) AS RANK_A ");
            stb.append(       "FROM    SUM_SCORE ");
            stb.append(       "WHERE   SUMSCORE_A IS NOT NULL ");
            stb.append(")");
            
            //Ｂ得点席次
            stb.append(",RANK_B_SCORE AS(");
            stb.append(       "SELECT  EXAMNO, ");
            stb.append(               "RANK() OVER(ORDER BY SUMSCORE_B DESC) AS RANK_B ");
            stb.append(       "FROM    SUM_SCORE ");
            stb.append(       "WHERE   SUMSCORE_B IS NOT NULL ");
            stb.append(")");
            
            //メイン表
            stb.append("SELECT  T1.EXAMNO, T1.NAME, T1.SEX, T1.DESIREDIV, T1.NATPUBPRIDIV ");
            stb.append(       ",SUMSCORE_A ");
            stb.append(       ",SUMSCORE_B ");
            stb.append(       ",RANK_A ");
            stb.append(       ",RANK_B ");
            stb.append("FROM   BASE_A T1 ");
            stb.append("LEFT JOIN SUM_SCORE T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append("LEFT JOIN RANK_A_SCORE T3 ON T3.EXAMNO = T1.EXAMNO ");
            stb.append("LEFT JOIN RANK_B_SCORE T4 ON T4.EXAMNO = T1.EXAMNO ");

            if( ( (String)paramap.get("SORT") ).equals("1") )             //受験番号順で出力
                stb.append("ORDER BY T1.EXAMNO");
            else if( ( (String)paramap.get("OUTTYPE") ).equals("1") )     //コース１はＢ得点順で出力
                stb.append("ORDER BY RANK_B, T1.EXAMNO");
            else if( ( (String)paramap.get("OUTTYPE") ).equals("2") )     //コース２はＢ得点順で出力
                stb.append("ORDER BY RANK_B, T1.EXAMNO");
            else if( !( (String)paramap.get("PRGID") ).equals("KNJL327") )                                                          //コース３・４Ａ得点順で出力
                stb.append("ORDER BY RANK_A, T1.EXAMNO");

        } catch( Exception ex ){
            log.error("error! ",ex);
        }
log.debug(stb+"!!!!!!!!!!!!1");
        return stb.toString();
    }


    /**
     *  priparedstatement作成  受験人数
     *  ＊＊ 内容をメソッドprestatScoreの共通表COURSE_DESIREDIV,BASE_Aと同様とする
     */
    private String prestatCountExamnee( Map paramap )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            stb.append("WITH ");

            //任意のコースを含む志望区分を抽出
            stb.append("COURSE_DESIREDIV AS(");
            stb.append(       "SELECT  T1.DESIREDIV ");
            stb.append(       "FROM    ENTEXAM_WISHDIV_MST T1, ENTEXAM_COURSE_MST T2 ");
            stb.append(       "WHERE   T1.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T2.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            stb.append(           "AND T2.COURSECD = T1.COURSECD ");
            stb.append(           "AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(           "AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            if (Integer.parseInt( (String)paramap.get("OUTTYPE") ) < 5 )    //NO001
                stb.append(           "AND T2.EXAMCOURSE_MARK = ? ");
            stb.append(           "AND T1.WISHNO = '1' ");
            stb.append(")");

            stb.append(   "SELECT  SUM(1) AS A_NUMBER ");
            stb.append(          ",SUM( CASE WHEN SEX = '1' THEN 1 ELSE 0 END ) AS M_NUMBER ");
            stb.append(          ",SUM( CASE WHEN SEX = '2' THEN 1 ELSE 0 END ) AS F_NUMBER ");
            stb.append(   "FROM    ENTEXAM_APPLICANTBASE_DAT T3 ");
            stb.append(   "WHERE   T3.ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "' ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("   AND T3.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            }
            if( !paramap.containsKey("SENGAN") || !paramap.containsKey("HEIGAN") )  //NO002
                stb.append(       "AND T3.SHDIV = ? ");
            stb.append(       "AND EXISTS(SELECT 'X' FROM COURSE_DESIREDIV T4 WHERE T4.DESIREDIV = T3.DESIREDIV ) ");
            // -> 付属出身者の出力の有無 受験番号5000は付属出身 NO001 スポーツ推薦者
            if( ((String)paramap.get("OUTPUT2") ).equals("1") ){
                stb.append(   "AND NOT EXAMNO BETWEEN '5000' AND '5999' ");
            }else if ( ((String)paramap.get("OUTPUT2") ).equals("3") ){
                stb.append(   "AND APPLICANTDIV = '3' ");
            }else if ( ((String)paramap.get("OUTPUT2") ).equals("4") ){
                //NO004
                stb.append(   "AND SCALASHIPDIV = '02' ");
            }else if ( !((String)paramap.get("OUTPUT2") ).equals("5") ){
                stb.append(   "AND EXAMNO BETWEEN '5000' AND '5999' ");
            }
            //NO003
            if (((String) paramap.get("PRGID")).equals("KNJL327")) {
                stb.append(   "AND EXAMNO " + (String) paramap.get("EXAMIN") + " ");
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
log.debug(stb+"!!!!!!!!!!!!1");
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
            //ret = svf.VrsOutn( "DESIREDIV" + i,  j,  (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV"))) );
            svf.VrsOutn( "DESIREDIV" + i,  j,  editString( (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV")))) );
            svf.VrsOutn( "JUDGEMENT" + i,  j,  setConsultation( db2, ps, arrrs ) );

            if( paramap.containsKey("OUTPUTEXAM") ) //NO001
                svf.VrsOutn( "EXAMNO" + i,  j,  arrrs[1].getString("EXAMNO") );

            if( arrrs[1].getString("SEX") != null  &&  arrrs[1].getString("SEX").equals("2") )
                svf.VrsOutn( "SEX"  + i,  j,  "*" );

            if( paramap.containsKey("OUTPUTNAME") ) //NO001
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
     *  明細行印刷  共通項目
     */
    void printsvfOutDetail327(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Map paramap,
            final ResultSet arrrs[],
            final PreparedStatement ps,
            final int cnt,
            final OutPutData data
    ) {
        int i = ( cnt < 50 )? 1 : 2;
        int j = ( 0 < (cnt + 1) % 50  )? (cnt + 1) % 50 : 50;
        try {
            //ret = svf.VrsOutn( "DESIREDIV" + i,  j,  (String)(desiredivmap.get(arrrs[1].getString("DESIREDIV"))) );
            svf.VrsOutn( "DESIREDIV" + i,  j,  editString( (String)(desiredivmap.get(data._desirediv))) );
            svf.VrsOutn( "JUDGEMENT" + i,  j,  setConsultation327( db2, ps, arrrs, data ) );

            if (paramap.containsKey("OUTPUTEXAM")) {
                //NO001
                svf.VrsOutn( "EXAMNO" + i,  j, data._examno);
            }

            if (data._sex != null  &&  data._sex.equals("2")) {
                svf.VrsOutn( "SEX"  + i,  j,  "*" );
            }

            if( paramap.containsKey("OUTPUTNAME") ) //NO001
                if( 20 < knjobj.retStringByteValue(data._name, 21 ) )
                    svf.VrsOutn( "NAME" + i + "_2",  j, data._name);
                else
                    svf.VrsOutn( "NAME" + i + "_1",  j, data._name);

            printsvfOutDetail_A327( svf, paramap, i, j, data );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

    /**
     *  志望区分を表記用に編集  共通項目
     */
    private String editString( String str )
    {
        if( stb == null )stb = new StringBuffer();
        else             stb.delete( 0, stb.length() );
        try {
            for( int i = 0 ; i < str.length() ; i++ )
                if( ! str.substring( i, i+1 ).equals(" ") )stb.append( str.substring( i, i+1 ) );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return stb.toString();
    }


    /**
     *  明細行印刷
     */
    void printsvfOutDetail_A( Vrw32alp svf, Map paramap, ResultSet arrrs[], int i, int j )
    {
        try {
            //出力対象コース１・２は得点Ｂのランキング、以外は得点Ａのランキングを表示する
            svf.VrsOutn( "ORDER" + i,  j,  
                ( 2 < Integer.parseInt(( (String)paramap.get("OUTTYPE") )) )?  arrrs[1].getString("RANK_A") : arrrs[1].getString("RANK_B") );  //全体順位

            //該当する志望区分を表示用Mapより取得し、各コースにおいてブランクでない場合、得点を表示する
            //出力対象コース１・２は得点Ｂの合計、以外は得点Ａの合計を表示する
            String str = (String)( desiredivmap.get( arrrs[1].getString("DESIREDIV") ) );
            for( int k = 1 ; k <= 4 ; k++ )
                if( k <= str.length()  &&  !( str.substring( k - 1, k ).equals(" ") ) )
                    svf.VrsOutn( "POINT" + i + "_" + k,  j,  
                        ( ( 2 < k )? arrrs[1].getString("SUMSCORE_A") : arrrs[1].getString("SUMSCORE_B") ) );  //

        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

    /**
     *  明細行印刷
     */
    void printsvfOutDetail_A327( Vrw32alp svf, Map paramap, int i, int j, OutPutData data )
    {
        try {
            //出力対象コース１・２は得点Ｂのランキング、以外は得点Ａのランキングを表示する
            svf.VrsOutn( "ORDER" + i,  j,  
                ( 2 < Integer.parseInt(( (String)paramap.get("OUTTYPE") )) )?  data._rankA : data._rankB);  //全体順位

            //該当する志望区分を表示用Mapより取得し、各コースにおいてブランクでない場合、得点を表示する
            //出力対象コース１・２は得点Ｂの合計、以外は得点Ａの合計を表示する
            String str = (String)( desiredivmap.get(data._desirediv) );
            for (int k = 1 ; k <= 4 ; k++)
                if( k <= str.length()  &&  !( str.substring( k - 1, k ).equals(" ") ) )
                    svf.VrsOutn( "POINT" + i + "_" + k,  j,  
                        ( ( 2 < k )? data._sumscoreA : data._sumscoreB ) );  //

        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷  共通項目
     */
    void printsvfOutHead( DB2UDB db2, Vrw32alp svf, Map paramap )
    {

        try {
            svf.VrsOut( "NENDO",        (String)paramap.get("NENDO") );
            svf.VrsOut( "SCHOOLDIV",    "高校" );
            svf.VrsOut( "TESTDIV",      "" );
            svf.VrsOut( "EXAMCOURSE",   "" );
            svf.VrsOut( "DATE",         (String)paramap.get("NOW_DATE") );
            svf.VrsOut( "PAGE",         String.valueOf( ++pagecount ) );
            svf.VrsOut( "TOTAL_PAGE",   (String)paramap.get("TOTALPAGE") );

            if (Integer.parseInt( (String)paramap.get("OUTTYPE") ) < 5 ){   //NO001
                svf.VrsOut( "EXAMCOURSE",  "-----" 
                    + (String)paramap.get("EXAMCOURSENAME") 
//                  + ( ( ((String)paramap.get("SHDIV")).equals("1") )? " (専願)" : " (併願)" )
                    + ( ( paramap.containsKey("SENGAN") && paramap.containsKey("HEIGAN") )? " (全体)" : ( ((String)paramap.get("SHDIV")).equals("1") )? " (専願)" : " (併願)" ) //NO002
                    + "-----" );
            }else {
                svf.VrsOut( "EXAMCOURSE",  "-----全コース" 
//                  + ( ( ((String)paramap.get("SHDIV")).equals("1") )? " (専願)" : " (併願)" )
                    + ( ( paramap.containsKey("SENGAN") && paramap.containsKey("HEIGAN") )? " (全体)" : ( ((String)paramap.get("SHDIV")).equals("1") )? " (専願)" : " (併願)" ) //NO002
                    + "-----" );
            }

            //各コースの満点  iは左右欄の番号 jはコースの番号
            for( int i = 1 ; i <= 2 ; i++ )
                for( int j = 1 ; j <= 4 ; j++ )
                    svf.VrsOut( "PERFECT" + i + "_" + j,   "(" + (String)paramap.get("PERFECT" + j) + ")" );
            setInfluenceName(db2, svf, paramap);
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

}

}
