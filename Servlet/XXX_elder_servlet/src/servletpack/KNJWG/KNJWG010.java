// kanji=漢字
/*
 * $Id: d58603822f2f9834a14f81ba3f6ce388798a17be $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJWE.KNJWE070_1;
import servletpack.KNJWE.KNJWE070_2;
import servletpack.KNJE.KNJE080J_1;
import servletpack.KNJE.KNJE080J_2;
import servletpack.KNJWE.KNJWE080_1;
import servletpack.KNJE.KNJE080_2;
import servletpack.KNJI.KNJI060_1;
import servletpack.KNJI.KNJI060_2;
import servletpack.KNJI.KNJI070_1;
import servletpack.KNJI.KNJI070_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
http://tokio/serv_ktest/KNJD?DBNAME=KINJDB&PRGID=KNJWG010&category_name=20051241,012,2005,3,01,20052281,,1,2006/01/17,,,&CTRL_YEAR=2005
 *
 *  学校教育システム 賢者 [事務管理] 証明書交付
 *
 *     001  卒業証明書
 *     002  卒業証明書（英）
 *     003  卒業見込証明書
 *     004  在学証明書
 *     005  在学証明書(英)
 *     006  学業成績証明書
 *     007  学業成績証明(英)
 *     008  調査書(進学)
 *     009  調査書(就職)
 *     011  単位修得証明書
 *     012  在学証明書(中学)
 *     006  学業成績証明書(中学)    NO002
 *     007  学業成績証明(英)(中学)  NO002
 *
 *  2004/04/27 yamashiro・学習成績概評の学年人数の出力条件を追加 -> 指示画面より受取
 *  2005/04/14 yamashiro Modify : 中学用在学証明書を追加
 *  ** 広島版 2005/05/31 yamashiro・ＯＳ区分を追加
 *  **                            ・学業成績および調査書の出力仕様を変更
 *  2005/07/01 yamashiro・ＯＳ区分を追加
 *                      ・学業成績および調査書の出力仕様を変更
 *  2005/07/22 yamashiro・改定
 *  2005/11/18 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度
 *  2005/12/08 yamashiro・学籍処理日を引数として追加（「卒業日が学籍処理日の後なら卒業見込とする」仕様に伴い）
 *                        但し、現時点では指示画面が未対応のため、処理日を使用する
 *  2006/03/20 yamashiro・就職用調査書を全国高等学校統一用紙の改定に対応 --NO001
 *                        但し、４学年用のみ。６学年用は用紙が未定
 *  2006/03/23 yamashiro・中学成績証明書（和・英）を追加 --NO002
 *  2006/03/29 yamashiro・--NO002により高校成績証明書が出力されない不具合を修正 --NO003
 */

public class KNJWG010
{
    private static final Log log = LogFactory.getLog(KNJWG010.class);
    String plist[];
    final private KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定 06/03/23

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *  2005/07/22 Modify
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        Vrw32alpWrap svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        boolean nonedata = false;           //該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
        Map paramap = new HashMap();        //HttpServletRequestからの引数 05/11/18

        // ＤＢ接続
        db2 = sd.setDb( request );
        if( sd.openDb( db2 ) ){
            log.error("db open error");
            return;
        }

        // パラメータの取得
        getParam( db2, request, paramap );  //05/11/18Modify

        // print svf設定
        sd.setSvfInit( request, response, svf );

        // 印刷処理
        nonedata = printSvf( db2, svf, paramap );  //05/11/18Modify

        // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb( db2 );
    }


    /*
     *  get parameter doGet()パラメータ受け取り 
     *
     *          plist[0] = "181010,1,,2002,3,,,,,2003-04-08,1001";
     *
     *          category_name   181010,     1学籍番号
     *                          004,        2証明書種別
     *                          2002,       3年度
     *                          3,          4学期
     *                          3,          5学年
     *                          100001,     6記載責任者
     *                          ,           7評定の読み替え
     *                          1,          8漢字出力
     *                          2003/02/20, 9処理日付／証明日付／記載日
     *                          ,           10証明書番号
     *
     *  2005/07/22 Build
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap ) //05/11/18Modify
    {
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }

        try {
            plist = request.getParameterValues("category_name");        //証明書情報

            //ＯＳ区分 05/07/01 1:XP 2:WINDOWS2000
            if( request.getParameter("OS") != null )
                paramap.put( "OS", request.getParameter("OS") );  //ＯＳ区分 05/11/18 HashMapへ変更
            else
                paramap.put( "OS", new String("1") );             //ＯＳ区分 05/11/18 HashMapへ変更

            paramap.put( "CTRL_YEAR", request.getParameter("CTRL_YEAR") );  //今年度  05/11/18
            if( request.getParameter("MIRISYU") != null )paramap.put( "MIRISYU", request.getParameter("MIRISYU") );  // 未履修科目を出力する:1 しない:2
            if( request.getParameter("RISYU") != null )paramap.put( "RISYU", request.getParameter("RISYU") );  // 未履修科目を出力する:1 しない:2
            if (null != request.getParameter("FORM6") && 0 < request.getParameter("FORM6").length()) {
                paramap.put( "FORM6", request.getParameter("FORM6") );  // ６年生用フォーム選択 する:on
            }
            paramap.put( "useCurriculumcd", "1");
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        }
    }


    /*
     *  印刷処理
     *  2005/07/22 Build
     */
    private boolean printSvf( DB2UDB db2, Vrw32alpWrap svf, Map paramap )     //05/11/18Modify
    {
        boolean nonedata = false;           //該当データなしフラグ
        Map hobj = new HashMap();
        int beforcertif = 0;              //05/07/23 証明書種別の保管
        _definecode.defineCode(db2, (String)paramap.get("CTRL_YEAR"));  //各学校における定数等設定

        try {
            for( int i = 0 ; i < plist.length ; i++ ){
                log.debug("plist[i] = " + plist[i]);
                //指示画面より受け取ったパラメーターを分解
                final String param[] = new String[12];
                final StringTokenizer stkx = new StringTokenizer( plist[i], ",", true );
                for( int j = 0 ; j < param.length ; j++ ) param[j] = null;  // 05/01/18
                int ib = 0;
                while (stkx.hasMoreTokens()) {
                    final String strx = stkx.nextToken();
                    if(strx.equals(",")){
                        ib++;
                        continue;
                    }
                    if(ib>11)   break;                      //04/04/27変更
                    if( ib>7 && strx.equals("on") )ib=10;   //テスト印刷時は証明書番号がない！ 04/04/27
                    param[ib] = strx;
                }
                if( param[8]!=null ){                       //param[8]が年度でない場合は今年度をいれておく！ 04/04/28
                    if( param[8].length()>3 )   param[11] = KNJWG010_1.b_year(param[8]);//過卒生対応年度取得->掲載日より年度を算出
                    //else                      param[11] = param[2];
                    else                        param[11] = (String)paramap.get("CTRL_YEAR");   //05/11/18Modify
                } else                          param[11] = (String)paramap.get("CTRL_YEAR");   //05/11/18Modify
                if( param[6] == null )param[6]="off";                           //評定1/2読替のフラグ
                if (!paramap.containsKey("HYOTEI")) {
                    if (param[6] != null && param[6].equals("on")) {
                        paramap.put("HYOTEI", "on");  // 評定の読み替え offの場合はparamapに追加しない。
                    }
                }

                if( Integer.parseInt(param[1]) != beforcertif ){
                    if( beforcertif != 0 )
                        releaseCertif( Integer.parseInt(param[1]), beforcertif, hobj );
                    beforcertif = Integer.parseInt(param[1]);       //05/07/23 証明書種別の保管
                }
                if( printSvfCertify( db2, svf, param, hobj, paramap ) )nonedata = true;  //各証明書の印刷処理  05/11/18Modify
            }
            printSvfCertifyClose( hobj );                                       //各証明書の終了処理
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /*
     *  各証明書の印刷処理
     *  2005/07/22 Build
     *  2005/11/18 Modify HttpServletRequestsの引数格納Map paramapを挿入
     */
    private boolean printSvfCertify( DB2UDB db2, Vrw32alpWrap svf, String param[], Map hobj, Map paramap )  //05/11/18Modify
    {
        boolean nonedata = false;
        boolean grddiv = false;     //卒・在判定
        int pdiv = 0;               //証明書種別

        try {
            grddiv = servletpack.KNJA.detail.KNJ_GradeRecSql.GrdDiv(db2,param[0]);  //卒・在判定
            pdiv = Integer.parseInt(param[1]);                                      //証明書種別
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        //学籍処理日をMapへセット。現時点で処理日を使用するので暫定的ココに置く。05/12/08
        try {
            if( ! paramap.containsKey("CTRL_DATE") )paramap.put( "CTRL_DATE", param[8] );  //学籍処理日
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        if( _definecode.schoolmark.equals("TOK")) {
//            param[9] = getCertificateNum(param);  // 証明書番号セット param[9]を上書き
        }
        paramap.put("CERTIFKIND", param[1]);  // 証明書種別
        
        try {
            if ((1 <= pdiv && pdiv <= 5) || (12 <= pdiv && pdiv <= 16) || (19 <= pdiv && pdiv <= 22)) {  // 卒業証明書等
                if (!_definecode.schoolmark.equals("KIN") && !_definecode.schoolmark.equals("KINJUNIOR")) {
                    if (pknjwg010T (db2,svf,param,hobj,grddiv,paramap)) nonedata = true;   // 卒業証明書等 東京都用
                } else {
                    if (pknjwg010(db2,svf,param,hobj,grddiv,paramap)) nonedata = true;   // 卒業証明書等
                }
            } else if (pdiv == 6) {
                if( _definecode.schoolmark.equals("KINJUNIOR")) {
                    if (pknje080J_1( db2, svf, param, hobj, grddiv, paramap)) nonedata = true;   //中学成績証明書(和) --NO002
                }else {
                    if (pknje080( db2, svf, param, hobj, grddiv, paramap)) nonedata = true;   //成績証明書
                }
            } else if (pdiv == 7) {
                if (_definecode.schoolmark.equals("KINJUNIOR")) {
                    if (pknje080J_2( db2, svf, param, hobj, grddiv, paramap)) nonedata = true;   //中学成績証明書(和) --NO002
                } else{
                    if (pknje080( db2, svf, param, hobj, grddiv, paramap)) nonedata = true;   //成績証明書
                }
            } else if (pdiv == 8 || pdiv == 9) {
                if (pknjwe070( db2, svf, param, hobj, grddiv, paramap)) nonedata = true;   //調査書
            } else if (pdiv == 11 || pdiv == 18) {
                if (pknjwg030( db2, svf, param, hobj, grddiv, paramap)) nonedata = true;   //単位修得証明書
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
        return nonedata;
    }


    /*
     *  各証明書の解放 => SQL0954Cのエラー回避のため
     *  2005/07/23 Build
     */
    private void releaseCertif( int certif, int beforcertif, Map hobj )
    {
        try {
            //成績証明書和文と英文は共存しないようにする
            if( certif == 6 ){
                if( hobj.containsKey("KNJI070_2") ){
                    ( (KNJWE080_1)hobj.get("KNJI070_2") ).pre_stat_f(); //成績証明書(英語)
                    hobj.remove("KNJI070_2");
                }
                if( hobj.containsKey("KNJE080_2") ){
                    ( (KNJWE080_1)hobj.get("KNJE080_2") ).pre_stat_f(); //成績証明書(英語)
                    hobj.remove("KNJE080_2");
                }
            } else if( certif == 7 ){
                if( hobj.containsKey("KNJI070_1") ){
                    ( (KNJWE080_1)hobj.get("KNJI070_1") ).pre_stat_f(); //成績証明書
                    hobj.remove("KNJI070_1");
                }
                if( hobj.containsKey("KNJWE080_1") ){
                    ( (KNJWE080_1)hobj.get("KNJWE080_1") ).pre_stat_f(); //成績証明書
                    hobj.remove("KNJWE080_1");
                }
            //調査書の進学用と就職用は共存しないようにする
            } else if( certif == 8 ){
                if( hobj.containsKey("KNJI060_2") ){
                    ( (KNJWE070_1)hobj.get("KNJI060_2") ).pre_stat_f(); //調査書(就職)
                    hobj.remove("KNJI060_2");
                }
                if( hobj.containsKey("KNJWE070_2") ){
                    ( (KNJWE070_1)hobj.get("KNJWE070_2") ).pre_stat_f(); //調査書(就職)
                    hobj.remove("KNJWE070_2");
                }
            } else if( certif == 9 ){
                if( hobj.containsKey("KNJI060_1") ){
                    ( (KNJWE070_1)hobj.get("KNJI060_1") ).pre_stat_f(); //調査書(進学)
                    hobj.remove("KNJI060_1");
                }
                if( hobj.containsKey("KNJWE070_1") ){
                    ( (KNJWE070_1)hobj.get("KNJWE070_1") ).pre_stat_f(); //調査書(進学)
                    hobj.remove("KNJWE070_1");
                }
            }
        } catch( Exception ex ) {
            log.error("error! ",ex );
        }
    }


    /*
     *  各証明書の印刷終了処理
     *  2005/07/22 Build
     */
    private void printSvfCertifyClose( Map hobj )
    {
        try {
            if( hobj.containsKey("KNJWG010_1") ) ( (KNJWG010_1)hobj.get("KNJWG010_1") ).pre_stat_f(); //卒業証明書等
            if( hobj.containsKey("KNJWG010_2") ) ( (KNJWG010_1)hobj.get("KNJWG010_2") ).pre_stat_f(); //卒業証明書等
            if( hobj.containsKey("KNJWG010_1T") ) ( (KNJWG010_1T)hobj.get("KNJWG010_1T") ).pre_stat_f(); //卒業証明書等
            if( hobj.containsKey("KNJWG010_2T") ) ( (KNJWG010_1T)hobj.get("KNJWG010_2T") ).pre_stat_f(); //卒業証明書等
            if( hobj.containsKey("KNJI070_1") ) ( (KNJWE080_1)hobj.get("KNJI070_1") ).pre_stat_f(); //成績証明書
            if( hobj.containsKey("KNJWE080_1") ) ( (KNJWE080_1)hobj.get("KNJWE080_1") ).pre_stat_f(); //成績証明書
            if( hobj.containsKey("KNJI070_2") ) ( (KNJWE080_1)hobj.get("KNJI070_2") ).pre_stat_f(); //成績証明書(英語)
            if( hobj.containsKey("KNJE080_2") ) ( (KNJWE080_1)hobj.get("KNJE080_2") ).pre_stat_f(); //成績証明書(英語)
            if( hobj.containsKey("KNJI060_1") ) ( (KNJWE070_1)hobj.get("KNJI060_1") ).pre_stat_f(); //調査書(進学)
            if( hobj.containsKey("KNJWE070_1") ) ( (KNJWE070_1)hobj.get("KNJWE070_1") ).pre_stat_f(); //調査書(進学)
            if( hobj.containsKey("KNJI060_2") ) ( (KNJWE070_1)hobj.get("KNJI060_2") ).pre_stat_f(); //調査書(就職)
            if( hobj.containsKey("KNJWE070_2") ) ( (KNJWE070_1)hobj.get("KNJWE070_2") ).pre_stat_f(); //調査書(就職)
            if( hobj.containsKey("KNJWG030_2") ) ( (KNJWG030_1)hobj.get("KNJWG030_2") ).pre_stat_f(); //単位修得証明書
            if( hobj.containsKey("KNJWG030_1") ) ( (KNJWG030_1)hobj.get("KNJWG030_1") ).pre_stat_f(); //単位修得証明書
            if( hobj.containsKey("KNJE080J_1") ) ( (KNJE080J_1)hobj.get("KNJE080J_1") ).pre_stat_f(); //中学成績証明書 --NO002
            if( hobj.containsKey("KNJE080J_2") ) ( (KNJE080J_2)hobj.get("KNJE080J_2") ).pre_stat_f(); //中学成績証明書 --NO002
        } catch( Exception ex ) {
            log.error("error! ",ex );
        }
    }


    /*
     *  卒業・卒業見込・在学証明書
     *  2005/07/22 Modify
     *  2005/08/31 Modify 中学在学証明書を変更 SVF-FORMはKNJWG010_1において設定
     */
    private boolean pknjwg010( DB2UDB db2, Vrw32alpWrap svf, String param[], Map hobj, boolean grddiv, Map paramap )  //05/11/18Modify
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean nonedata = false;
        KNJWG010_1 pobj = null;

        if (!grddiv) {
            if (!hobj.containsKey("KNJWG010_1")) {
                hobj.put("KNJWG010_1", new KNJWG010_1(db2, svf,_definecode));     //在校生用オブジェクト作成
                ((KNJWG010_1) hobj.get("KNJWG010_1") ).pre_stat(null);  //preparestatement作成
            }
            pobj = (KNJWG010_1) hobj.get("KNJWG010_1");
        } else {
            if (!hobj.containsKey("KNJWG010_2")) {
                hobj.put("KNJWG010_2", new KNJWG010_2(db2, svf,_definecode));     //卒業生用オブジェクト作成
                ((KNJWG010_1) hobj.get("KNJWG010_2") ).pre_stat(null);  //preparestatement作成
            }
            pobj = (KNJWG010_1) hobj.get("KNJWG010_2");
        }

        int paper = Integer.parseInt(param[1]);
        if( paper==1 ){
            //卒業証明書
            pobj.certif1_out(param[0],param[2],param[11],param[3],param[8],param[9]);
        } else if( paper==2 ){
            //卒業証明書（英語）
            pobj.certif2_out(param[0],param[2],param[11],param[3],param[8],param[9]);
        } else if( paper==3 ){
            //卒業見込証明書
            pobj.certif3_out(param[0],param[2],param[11],param[3],param[8],param[9]);
        } else if( paper==4 ){
            //在学証明書(日本語)
            pobj.certif4_out(param[0],param[2],param[11],param[3],param[8],param[9]);
        } else if( paper==5 ){
            //在学証明書(英語)
            pobj.certif5_out(param[0],param[2],param[11],param[3],param[8],param[9]);
        } else if( paper==12 ){
            //在学証明書(中学用) 05/04/14Build
            pobj.certif4_out(param[0],param[2],param[11],param[3],param[8],param[9]); //05/08/31Modify
        }
        if(pobj.nonedata == true)nonedata = true;

        return nonedata;
    }

    /*
     *      卒業証明書（和）     Form-ID:KNJWG010_1   証明書種別:001
     *      卒業証明書（英）     Form-ID:KNJWG010_2   証明書種別:002
     *      卒業見込証明書（和） Form-ID:KNJWG010_3   証明書種別:003
     *      卒業見込証明書（英） Form-ID:KNJWG010_6   証明書種別:012
     *      在学証明書（和）     Form-ID:KNJWG010_4   証明書種別:004
     *      在学証明書（英）     Form-ID:KNJWG010_5   証明書種別:005
     *      在籍証明書（和）     Form-ID:KNJWG010_7   証明書種別:013
     *      在籍証明書（英）     Form-ID:KNJWG010_8   証明書種別:014
     *      修了証明書（和）     Form-ID:KNJWG010_9   証明書種別:015
     *      修了証明書（英）     Form-ID:KNJWG010_10  証明書種別:016
     */
    private boolean pknjwg010T (
            DB2UDB db2,
            Vrw32alpWrap svf,
            String param[],
            Map hobj,
            boolean grddiv,
            Map paramap
    ) {
        KNJWG010_1T pobj = null;
        if(!grddiv) {
            if(!hobj.containsKey("KNJWG010_1T")) {
                hobj.put("KNJWG010_1T", new KNJWG010_1T(db2, svf,_definecode));     //在校生用オブジェクト作成
                ((KNJWG010_1T) hobj.get("KNJWG010_1T")).pre_stat(null);  //preparestatement作成
            }
            pobj = (KNJWG010_1T) hobj.get("KNJWG010_1T");
        } else {
            if(!hobj.containsKey("KNJWG010_2T")) {
                hobj.put("KNJWG010_2T", new KNJWG010_2T(db2, svf,_definecode));     //卒業生用オブジェクト作成
                ((KNJWG010_1T) hobj.get("KNJWG010_2T") ).pre_stat( null );  //preparestatement作成
            }
            pobj = (KNJWG010_1T) hobj.get("KNJWG010_2T");
        }

        pobj.printSvfMain( param );
        if (pobj.nonedata) return true;
        else return false;
    }


    /*
     *  学業成績証明書
     *  2005/07/22 Modify
     */
    private boolean pknje080( DB2UDB db2, Vrw32alpWrap svf, String param[], Map hobj, boolean grddiv, Map paramap )  //05/11/18Modify
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean nonedata = false;
        KNJWE080_1 pobj = null;

        try {
            if (Integer.parseInt(param[1]) == 6) {
                //成績証明書
                if (! grddiv) {
                    if (!hobj.containsKey("KNJWE080_1")) {
                        hobj.put("KNJWE080_1", new KNJWE080_1(db2,svf,_definecode));     //在校生用オブジェクト作成
                        ((KNJWE080_1) hobj.get("KNJWE080_1")).pre_stat(param[0], param[2], null);  //preparestatement作成
                    }
                    pobj = (KNJWE080_1) hobj.get("KNJWE080_1");
                } else {
                    if (!hobj.containsKey("KNJI070_1")) {
                        hobj.put("KNJI070_1", new KNJI070_1(db2,svf,_definecode));     //卒業生用オブジェクト作成
                        ((KNJWE080_1) hobj.get("KNJI070_1")).pre_stat(null);  //preparestatement作成
                    }
                    pobj = (KNJWE080_1) hobj.get("KNJI070_1");
                }
            } else {
                //成績証明書(英語)
                if (! grddiv) {
                    if (!hobj.containsKey("KNJE080_2")) {
                        hobj.put("KNJE080_2", new KNJE080_2(db2,svf,_definecode));     //在校生用オブジェクト作成
                        ((KNJWE080_1) hobj.get("KNJE080_2") ).pre_stat(null);  //preparestatement作成
                    }
                    pobj = (KNJWE080_1) hobj.get("KNJE080_2");
                } else{
                    if (!hobj.containsKey("KNJI070_2")) {
                        hobj.put("KNJI070_2", new KNJI070_2(db2,svf,_definecode));     //卒業生用オブジェクト作成
                        ((KNJWE080_1) hobj.get("KNJI070_2")).pre_stat(null);  //preparestatement作成
                    }
                    pobj = (KNJWE080_1) hobj.get("KNJI070_2");
                }
            }
        } catch( Exception ex ) {
            log.error("error! ", ex );
        }

        try {
            int paper = Integer.parseInt(param[1]);
            int maxgrade = servletpack.KNJA.detail.KNJ_GradeRecSql.max_grade(db2,param[2],param[0]); //最高学年取得
            if( paper==6 ){
                //成績証明書
                if( maxgrade>4 ) ret = svf.VrSetForm("KNJWE080_1.frm", 4);       //４年生対応FORM
                else             ret = svf.VrSetForm("KNJWE080_1.frm", 4);       //６年生対応FORM
            } else{
                //成績証明書(英語)
                if( maxgrade>4 ) ret = svf.VrSetForm("KNJE080_2.frm", 4);       //４年生対応FORM
                else             ret = svf.VrSetForm("KNJE080_2.frm", 4);       //６年生対応FORM
            }
            pobj.head_out(param[2],param[8],param[5],maxgrade,paramap);         //学校名、校長名のセット 05/11/18Modify
            pobj.address_out(param[0],param[2],param[3],param[7],param[9]); //氏名、住所等出力
            pobj.attend_out(param[0],param[2]);                             //出欠の出力
            if( paper==6 )
                pobj.exam_out(param[0],param[2]);                           //所見の出力
            pobj.study_out(param[0],param[2]);                              //学習の記録出力-->VrEndRecord()はここで！
            if( pobj.nonedata == true ) nonedata = true;
        } catch( Exception ex ) {
            log.error("error! ",ex );
        }
        return nonedata;
    }


    /*
     *  調査書
     *  2005/07/22 Modify
     */
    private boolean pknjwe070(
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final String[] param,
            final Map hobj,
            final boolean grddiv,
            final Map paramap
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean nonedata = false;
        KNJWE070_1 pobj = null;

        try {
            if (Integer.parseInt(param[1]) == 8) {
                //調査書(進学用)
                if( ! grddiv ){
                    if (!hobj.containsKey("KNJWE070_1")) {
                        hobj.put("KNJWE070_1", new KNJWE070_1(db2,svf,_definecode));         //在校生用オブジェクト作成
                        ((KNJWE070_1) hobj.get("KNJWE070_1") ).pre_stat( param[6]);  //preparestatement作成
                    }
                    pobj = (KNJWE070_1) hobj.get("KNJWE070_1");
                } else{
                    if (!hobj.containsKey("KNJI060_1")) {
                        hobj.put("KNJI060_1", new KNJI060_1(db2,svf,_definecode, ""));         //卒業生用オブジェクト作成
                        ((KNJWE070_1) hobj.get("KNJI060_1")).pre_stat( param[6]);  //preparestatement作成
                    }
                    pobj = (KNJWE070_1) hobj.get("KNJI060_1");
                }
            } else {
                //調査書(就職用)
                if (! grddiv) {
                    if (!hobj.containsKey("KNJWE070_2")) {
                        hobj.put("KNJWE070_2", new KNJWE070_2(db2,svf,_definecode));         //在校生用オブジェクト作成
                        ((KNJWE070_1) hobj.get("KNJWE070_2")).pre_stat( param[6]);  //preparestatement作成
                    }
                    pobj = (KNJWE070_1) hobj.get("KNJWE070_2");
                } else {
                    if (!hobj.containsKey("KNJI060_2")) {
                        hobj.put("KNJI060_2", new KNJI060_2(db2,svf,_definecode, ""));         //卒業生用オブジェクト作成
                        ((KNJWE070_1) hobj.get("KNJI060_2")).pre_stat(param[6]);  //preparestatement作成
                    }
                    pobj = (KNJWE070_1) hobj.get("KNJI060_2");
                }
            }
        } catch( Exception ex ) {
            log.error("error! ", ex );
        }

        try {
            int paper = Integer.parseInt(param[1]);
            pobj.svf_int(param[0],param[2],paramap);                                         //ＳＶＦ−ＦＯＲＭフィールド初期化
            pobj.head_out( param[2], param[8], param[5], paramap );   //学校名、校長名のセット 05/11/18Modify
            pobj.address_out(param[0],param[2],param[3],param[7],param[9], paramap );   //氏名、住所等出力 05/12/08Modify
            if( paper==8 )
                pobj.geneviewmbr_out(param[0],param[2],param[3],param[10]); //成績段階別人数の出力 04/04/27変更
            pobj.attend_out(param[0],param[2]);  // 出欠の出力
            pobj.medexam_out(param[0], param[2]); // 身体状況の出力
            pobj.exam_out( param[0], param[2], paramap);  //所見の出力  05/07/01,05/11/18Modify
            pobj.study_out(param[0],param[2],paramap);  // 学習の記録出力-->VrEndRecord()はここで！ 05/07/01Modify maxgrade追加
            if( pobj.nonedata == true ) nonedata = true;
        } catch( Exception ex ) {
            log.error("error! ",ex );
        }
        return nonedata;
    }


    /*
     *  単位修得証明書
     *  2005/07/22 Modify
     */
    private boolean pknjwg030(
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final String[] param,
            final Map hobj,
            final boolean grddiv,
            final Map paramap )
    {
        boolean nonedata = false;
        KNJWG030_1 pobj = null;

        final String schregNo = param[0];
        final String certifkind = param[1];
        final String year = param[2];
        final String semes = param[3];
        final String date = param[8];
        final String number = param[9];
        try {
            if (!grddiv) {
                if (!hobj.containsKey("KNJWG030_1")) {
                    hobj.put("KNJWG030_1", new KNJWG030_1(db2, svf));     //在校生用オブジェクト作成
                    ((KNJWG030_1) hobj.get("KNJWG030_1")).pre_stat();  //preparestatement作成
                }
                pobj = (KNJWG030_1)hobj.get("KNJWG030_1");
            } else {
                if (!hobj.containsKey("KNJWG030_2")) {
                    hobj.put( "KNJWG030_2", new KNJWG030_2( db2, svf ) );     //卒業生用オブジェクト作成
                    ((KNJWG030_1)hobj.get("KNJWG030_2")).pre_stat();  //preparestatement作成
                }
                pobj = (KNJWG030_1)hobj.get("KNJWG030_2");
            }
        } catch(Exception ex) {
            log.error("error! ", ex);
        }

        try {
            if (param[1].equals("011")) {
                svf.VrSetForm("KNJWG030.frm", 4);
                svf.VrsOut("TITLE", "成績単位修得証明書");
            } else {
                svf.VrSetForm("KNJWG031.frm", 4);
                svf.VrsOut("TITLE", "成績単位修得見込証明書");
            }

            //学校名、校長名のセット
            pobj.head_out(year, date, paramap, certifkind, number);

            //氏名、住所等出力
            pobj.studentInfoPrint(schregNo, year, semes, certifkind);

            //学習の記録出力-->VrEndRecord()はここで！
            pobj.study_out(schregNo, year, certifkind, paramap);

            if (pobj.nonedata) {
                nonedata = true;
            }
        } catch(Exception ex) {
            log.error("error! ", ex);
        }
        return nonedata;
    }


    /*
     *  中学成績証明書(和) --NO002
     *
     */
    private boolean pknje080J_1( DB2UDB db2, Vrw32alpWrap svf, String param[], Map hobj, boolean grddiv, Map paramap )  //05/11/18Modify
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean nonedata = false;
        KNJE080J_1 pobj = null;

        try {
            //成績証明書(和)のクラスインスタンスがない場合は作成する
            if( !hobj.containsKey("KNJE080J_1") ){
                hobj.put( "KNJE080J_1", new KNJE080J_1( db2, svf, paramap) );  //在校生用
                ( (KNJE080J_1)hobj.get("KNJE080J_1") ).pre_stat( null );  //preparestatement作成
            }
            pobj = (KNJE080J_1)hobj.get("KNJE080J_1");

            int maxgrade = servletpack.KNJA.detail.KNJ_GradeRecSql.max_grade( db2, param[2], param[0] );  //最高学年取得

            ret = svf.VrSetForm("KNJE080_3.frm", 1);  //成績証明書(和)

            pobj.head_out( param[2], param[8], param[5], maxgrade, paramap );  //学校名、校長名のセット
            pobj.address_out( param[0], param[2], param[3], param[7], param[9] );  //氏名、住所等出力
            pobj.attend_out( param[0], param[2] );  //出欠の出力
            pobj.study_out( param[0], param[2] );  //学習の記録出力-->VrEndRecord()はここで！

            if( pobj.nonedata == true ) nonedata = true;

        } catch( Exception ex ) {
            log.error("error! ",ex );
        }
        return nonedata;
    }


    /*
     *  中学成績証明書(英) --NO002
     *
     */
    private boolean pknje080J_2( DB2UDB db2, Vrw32alpWrap svf, String param[], Map hobj, boolean grddiv, Map paramap )  //05/11/18Modify
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean nonedata = false;
        KNJE080J_2 pobj = null;

        try {
            //成績証明書(英)のクラスインスタンスがない場合は作成する
            if( !hobj.containsKey("KNJE080J_2") ){
                hobj.put( "KNJE080J_2", new KNJE080J_2( db2, svf, paramap) );  //在校生用
                ( (KNJE080J_2)hobj.get("KNJE080J_2") ).pre_stat( null );  //preparestatement作成
            }
            pobj = (KNJE080J_2)hobj.get("KNJE080J_2");

            int maxgrade = servletpack.KNJA.detail.KNJ_GradeRecSql.max_grade( db2, param[2], param[0] );  //最高学年取得

            ret = svf.VrSetForm("KNJE080_4.frm", 1);  //成績証明書(英)

            pobj.head_out( param[2], param[8], param[5], maxgrade, paramap );  //学校名、校長名のセット
            pobj.address_out( param[0], param[2], param[3], param[7], param[9] );  //氏名、住所等出力
            pobj.attend_out( param[0], param[2] );  //出欠の出力
            pobj.study_out( param[0], param[2] );  //学習の記録出力-->VrEndRecord()はここで！

            if( pobj.nonedata == true ) nonedata = true;

        } catch( Exception ex ) {
            log.error("error! ",ex );
        }
        return nonedata;
    }

    /**
     * @param param : getParameterValues の証明日付がNULLでなければ証明書日付より割出した年度、以外は getParameterValues の年度。
     *                 東京都では、年度param[11]の今年度をセットし直す。（ログイン画面の年度は使用しない。）
     * @return : 証明書番号にセットする和暦年を返す。
     *            （元は和暦年＋ SCHOOL_MST SCHOOLNAME2 学校名略称を返していた。）
     */
    public String getCertificateNum(String param[])
    {
        // 東京都では、年度param[2]から年度を取り出してる。
        // param[8]が年度でない場合は今年度をいれておく！ 04/04/28
        if(param[8] != null) {   
            if(param[8].length() > 3) {
                param[11] = KNJWG010_1.b_year( param[8] );  // 過卒生対応年度取得->掲載日より年度を算出
            } else {
                param[11] = param[2];
            }
        } else {
            param[11] = param[2];
        }

        return nao_package.KenjaProperties.gengou (Integer.parseInt(param[11])).substring (2, 4);
    }
    
}
