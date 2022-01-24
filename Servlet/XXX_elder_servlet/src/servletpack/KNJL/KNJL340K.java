// kanji=漢字
/*
 * $Id: 7e7110d3adc8bd89bd58c520e6e3c2bacb709b7e $
 *
 * 作成日: 2005/07/29 11:25:40 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

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

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３４０＞  入試合否判定原簿
 *
 *  2005/08/02 nakamoto 作成日
 *  2005/08/23 nakamoto 出力対象：「一般受験者」と「附属出身者」に分けて印刷
 *  2005/09/01 nakamoto 高校用にも対応郵便番号マスタ
 *  2005/09/04 nakamoto 出力対象：「スポーツ推薦者」を追加
 *                      「一般受験者」の場合、備考欄に「スポーツ推薦者」と表記
 *                      サブタイトルを追加
 *  2005/09/13 nakamoto 郵便番号マスタの複数データに対応
 *
 *  2005/10/27 nakamoto 市区町村欄（所在地コードを表記する。）
 *  2005/11/07 m-yama   NO001 専併区分の表示を'専'、'併'に変更
 *  2005/11/07 m-yama   NO002 中高一貫者は、成績データを出力しない
 *  2005/11/14 nakamoto NO003 中学後期の英数は、Ｂ配点フラグをみる
 *                      NO004 中学の判定結果は、'否'ではなく不合格コース('I','T','H')を表記
 *  2005/11/18 nakamoto NO005 出身学校の市区町村は、出身学校の所在地コードをみて、「L007」「名称１」を表記
 *  2005/12/20 m-yama   NO006 NO004をコメント化('否'に戻す)
 *  2005/12/21 m-yama   NO007 順位を最終判定コースの得点でつける
 *  2005/12/29 nakamoto NO008 高校の否欄は、中高一貫者の場合、印字しない。
 *  2005/12/30 nakamoto NO009 (高校)コースコード記号の変更による修正 Q→T R→P
 *  2006/01/10 nakamoto NO010 (中学)合計・平均・順位の計算を変更
 *                      　　　○志望区分('I','IT','ITH')の者で、合計・平均・順位を計算---?
 *                      　　　○志望区分('I')以外の者で、合計・平均・順位を計算----------?
 *                      　　　　　○最終判定コースが('I')の者は、?から合計・平均・順位を表記
 *                      　　　　　○最終判定コースが('T','H')の者は、?から合計・平均・順位を表記
 *  2006/01/11 m-yama   NO011 判定結果の合欄にデータがある場合、否欄は出力しない。
 *  2006/01/12 o-naka   NO012 「附属出身者」の場合、判定結果の否欄は、'否'、'欠'の表記を以下の条件の通りとする
 *                      　　　○附属出身者で１人でも得点データがある場合---テストありと判断
 *                      　　　　　○個人得点（有）Base判定（不合格）--->否欄（否）
 *                      　　　　　○個人得点（無）Base判定（不合格）--->否欄（欠）
 *                      　　　○附属出身者で１人も得点データがない場合---テストなしと判断
 *                      　　　　　○Base判定（不合格）--->否欄（否）
 *  2006/01/15 o-naka   NO013 サブタイトル「スポーツ推薦者」→「クラブ推薦者」と変更
 *  2006/01/24 o-naka   NO014 中高一貫者は、出身学校欄は出力しない
 *  2008/01/11 m-yama   NO015 出力対象：「K2希望者」を追加
 *  2008/02/10 o-naka   NO016 出力対象：「K2希望者」にて、クラブ推薦者は備考欄に「クラブ推薦」と出力。
 *  2010/01/13 o-naka   NO017 「判定区分が8：未受験」の場合に”欠”表示する
 * @author nakamoto
 * @version $Id: 7e7110d3adc8bd89bd58c520e6e3c2bacb709b7e $
 */
public class KNJL340K {


    private static final Log log = LogFactory.getLog(KNJL340K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];//NO012

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 1:前期,2:後期
            param[2] = request.getParameter("OUTPUT2");                     //出力対象 1:一般受験者, 2:附属出身者, 3:スポーツ推薦者, 4:K2希望者 NO015

            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[10] = request.getParameter("SPECIAL_REASON_DIV");         //特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        if( printMain(db2,svf,param) ) nonedata = true;

log.debug("nonedata="+nonedata);

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り


    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[10] + "' ");
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
    
    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        if (param[5].equals("1")) svf.VrSetForm("KNJL340_1.frm", 1);
        if (param[5].equals("2")) svf.VrSetForm("KNJL340_2.frm", 1);//2005.09.01

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        if (param[5].equals("1")) param[6] = "中学校";
        if (param[5].equals("2")) param[6] = "高等学校";

    //  作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[8] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

//NO012↓
        //附属出身者
        if (param[2].equals("2")) {
    //  附属で１人でも得点データがある場合（テストあり：試験区分毎）
        try {
            db2.query(statementFuzokuScore(param));
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                param[9] = rs.getString("CNT_F");
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getFuzokuScore read error!",ex);
        }
        }
//NO012↑

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            int total_page[] = new int[2];//2
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**試験区分毎の総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
    {
        try {
log.debug("TotalPage start!");
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();
log.debug("TotalPage end!");

            int cnt = 0;
            while( rs.next() ){
                total_page[cnt] = rs.getInt("COUNT");
                cnt++;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

    }//getTotalPage()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
    {
        boolean nonedata = false;
        try {
log.debug("Meisai start!");
            if (param[5].equals("1")) db2.query(statementMeisaiJ(param));
            if (param[5].equals("2")) db2.query(statementMeisaiH(param));//2005.09.01
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int examno_limit = (param[5].equals("1")) ? 49 : 24 ;//2005.09.01

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            int page_arr = 0;   //総ページ数配列No
            String testdiv = "d";
            while( rs.next() ){
                //１ページ印刷
                if (examno_limit < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ) {
                    //合計印刷
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2, svf,param,rs,page_cnt,total_page,page_arr);
                //明細データ---2005.09.01
                if (param[5].equals("1")) printScoreJ(svf,param,rs,gyo);
                if (param[5].equals("2")) printScoreH(svf,param,rs,gyo);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = rs.getString("TESTDIV");
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                svf.VrEndPage();
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int page_cnt,int total_page[],int page_arr)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (param[5].equals("1")) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );//2005.09.01
            svf.VrsOut("DATE"         , param[8] );
            //2005.09.04
            if (param[5].equals("2")) {
                if (param[2].equals("1")) svf.VrsOut("SUBTITLE"   , "（一般受験者）" );
                if (param[2].equals("2")) svf.VrsOut("SUBTITLE"   , "（中高一貫者）" );
                if (param[2].equals("3")) svf.VrsOut("SUBTITLE"   , "（クラブ推薦者）" );//NO013
                if (param[2].equals("4")) svf.VrsOut("SUBTITLE"   , "（K2希望者）" );
            }

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , String.valueOf(total_page[page_arr]) );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**(中学)明細データをセット*/
    private void printScoreJ(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len = "0";
        String len2 = "0";
        String mark1 = " ";
        String mark2 = " ";
        String mark3 = " ";
        try {
            len = (gyo < 25) ? "1" : "2";
            gyo = (gyo < 25) ? gyo+1 : gyo+1-25;
            len2 = (10 < (rs.getString("NAME")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("EXAMNO"+len     ,gyo      , rs.getString("EXAMNO") );
            svf.VrsOutn("KANA"+len       ,gyo      , rs.getString("NAME_KANA") );
            svf.VrsOutn("NAME"+len+len2  ,gyo      , rs.getString("NAME") );
            svf.VrsOutn("SEX"+len        ,gyo      , rs.getString("SEX_NAME") );

            svf.VrsOutn("JUDGEMENT"+len+"_1"   ,gyo      , rs.getString("JUDGE1") );
            //NO011
            if (null == rs.getString("JUDGE1") || rs.getString("JUDGE1").equals("")){
                svf.VrsOutn("JUDGEMENT"+len+"_2"   ,gyo      , rs.getString("JUDGE2") );
            }
            svf.VrsOutn("POINT"+len+"_1" ,gyo      , rs.getString("SCORE1") );
            svf.VrsOutn("POINT"+len+"_2" ,gyo      , rs.getString("SCORE2") );
            svf.VrsOutn("POINT"+len+"_3" ,gyo      , rs.getString("SCORE3") );
            svf.VrsOutn("POINT"+len+"_4" ,gyo      , rs.getString("SCORE4") );
            svf.VrsOutn("TOTAL"+len      ,gyo      , rs.getString("SCORE_SUM") );
            svf.VrsOutn("AVERAGE"+len    ,gyo      , rs.getString("SCORE_AVG") );
            svf.VrsOutn("ORDER"+len      ,gyo      , rs.getString("SCORE_RNK") );

            if( (rs.getString("MARK1")).equals("I") ) mark1 = rs.getString("MARK1");
            if( (rs.getString("MARK2")).equals("I") ) mark1 = rs.getString("MARK2");
            if( (rs.getString("MARK3")).equals("I") ) mark1 = rs.getString("MARK3");
            if( (rs.getString("MARK1")).equals("T") ) mark2 = rs.getString("MARK1");
            if( (rs.getString("MARK2")).equals("T") ) mark2 = rs.getString("MARK2");
            if( (rs.getString("MARK3")).equals("T") ) mark2 = rs.getString("MARK3");
            if( (rs.getString("MARK1")).equals("H") ) mark3 = rs.getString("MARK1");
            if( (rs.getString("MARK2")).equals("H") ) mark3 = rs.getString("MARK2");
            if( (rs.getString("MARK3")).equals("H") ) mark3 = rs.getString("MARK3");

            svf.VrsOutn("DESIREDIV"+len  ,gyo      , mark1 + mark2 + mark3 );
            svf.VrsOutn("ATTACH"+len     ,gyo      , rs.getString("NATPUB_NAME") );
        } catch( Exception ex ) {
            log.warn("printScoreJ read error!",ex);
        }

    }//printScoreJ()の括り


    /**(高校)明細データをセット---2005.09.01*/
    private void printScoreH(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len2 = "0";
        String len3 = "0";
        try {
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            len3 = (10 < (rs.getString("FS_NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"      ,gyo+1  , rs.getString("EXAMNO") );     //受験番号
            svf.VrsOutn("KANA"        ,gyo+1  , rs.getString("NAME_KANA") );  //ふりがな
            svf.VrsOutn("NAME"+len2   ,gyo+1  , rs.getString("NAME") );       //氏名
            svf.VrsOutn("SEX"         ,gyo+1  , rs.getString("SEX_NAME") );   //性別
//NO001
            if (rs.getString("SHDIV") != null && rs.getString("SHDIV").equals("1")){
                svf.VrsOutn("SHDIV"       ,gyo+1  , "専" ); //専
            }else {
                svf.VrsOutn("SHDIV"       ,gyo+1  , "併" ); //併
            }
            svf.VrsOutn("DESIREDIV1_1",gyo+1  , rs.getString("ABBV1_1") );    //第１志望(理)
            svf.VrsOutn("DESIREDIV1_2",gyo+1  , rs.getString("ABBV1_2") );    //第１志望(国)
            svf.VrsOutn("DESIREDIV1_3",gyo+1  , rs.getString("ABBV1_3") );    //第１志望(特)
            svf.VrsOutn("DESIREDIV1_4",gyo+1  , rs.getString("ABBV1_4") );    //第１志望(進)
            svf.VrsOutn("DESIREDIV2"  ,gyo+1  , rs.getString("ABBV2") );      //第２志望
            svf.VrsOutn("DESIREDIV3"  ,gyo+1  , rs.getString("ABBV3") );      //第３志望
            svf.VrsOutn("JUDGEMENT1"  ,gyo+1  , rs.getString("JUDGE1") );     //判定結果(合)
            //NO011
            if (null == rs.getString("JUDGE1") || rs.getString("JUDGE1").equals("")){
                svf.VrsOutn("JUDGEMENT2"  ,gyo+1  , rs.getString("JUDGE2") );     //判定結果(否)
            }
            //NO002
            if (!param[2].equals("2")){ //中高一貫者以外
                //NO011
                if (null == rs.getString("JUDGE1") || rs.getString("JUDGE1").equals("")){
                    svf.VrsOutn("JUDGEMENT2"  ,gyo+1  , rs.getString("JUDGE2") );     //判定結果(否)---NO008
                }
                svf.VrsOutn("TOTAL1"      ,gyo+1  , rs.getString("TOTAL1") );     //成績Ａ(合計)
                svf.VrsOutn("POINT1_1"    ,gyo+1  , rs.getString("SCORE1_1") );   //成績Ａ(国語)
                svf.VrsOutn("POINT1_2"    ,gyo+1  , rs.getString("SCORE1_2") );   //成績Ａ(社会)
                svf.VrsOutn("POINT1_3"    ,gyo+1  , rs.getString("SCORE1_3") );   //成績Ａ(数学)
                svf.VrsOutn("POINT1_4"    ,gyo+1  , rs.getString("SCORE1_4") );   //成績Ａ(理科)
                svf.VrsOutn("POINT1_5"    ,gyo+1  , rs.getString("SCORE1_5") );   //成績Ａ(英語)
                svf.VrsOutn("TOTAL2"      ,gyo+1  , rs.getString("TOTAL2") );     //成績Ｂ(合計)
                svf.VrsOutn("POINT2_3"    ,gyo+1  , rs.getString("SCORE2_3") );   //成績Ｂ(数学)
                svf.VrsOutn("POINT2_5"    ,gyo+1  , rs.getString("SCORE2_5") );   //成績Ｂ(英語)
                //NO014
                svf.VrsOutn("LOCATION"    ,gyo+1  , rs.getString("LOC_NAME") );   //市区町村---NO014
                svf.VrsOutn("FINSCHOOL"+len3  ,gyo+1  , rs.getString("FS_NAME") );//出身中学校-NO014
            }
            if (param[2].equals("1") || param[2].equals("4")) //一般受験者, 4:K2希望者 NO016
                svf.VrsOutn("REMARK"  ,gyo+1  , rs.getString("APPLICANT") );  //備考---2005.09.04
        } catch( Exception ex ) {
            log.warn("printScoreH read error!",ex);
        }

    }//printScoreH()の括り


    /**
     *  市町村をセット
     *
     * ※最後の文字で判断---2005.09.01
     * 市：大阪市             ⇒ 大阪市
     * 区：大阪市中央区       ⇒ 大阪市
     * 町：泉南郡岬町         ⇒ 泉南郡
     * 村：南河内郡千早赤阪村 ⇒ 南河内郡
     * その他：XXX            ⇒ XXX
     */
    private String setCityName(ResultSet rs)
    {
        String ret_val = "";
        try {
            if (rs.getString("ZIP_CITY") != null) {
                String city_nam = rs.getString("ZIP_CITY");
                String city_flg = rs.getString("ZIP_CITY_FLG");

                if (city_flg.equals("市")) {
                    ret_val = city_nam;

                } else if (city_flg.equals("区")) {
                    ret_val = (-1 < city_nam.indexOf("市")) ? city_nam.substring(0,city_nam.indexOf("市"))+"市" : city_nam;

                } else if (city_flg.equals("町") || city_flg.equals("村")) {
                    ret_val = (-1 < city_nam.indexOf("郡")) ? city_nam.substring(0,city_nam.indexOf("郡"))+"郡" : city_nam;

                } else {
                    ret_val = city_nam;
                }
            }
        } catch( Exception ex ) {
            log.warn("setCityName read error!",ex);
        }
        return ret_val;

    }//setCityName()の括り


    /**合計をセット*/
    private void printTotal(Vrw32alp svf,String param[],int sex1_cnt,int sex2_cnt,int sex_cnt)
    {
        try {
            svf.VrsOut("TOTAL_MEMBER" , "男" + String.valueOf(sex1_cnt) + "名、" + 
                                              "女" + String.valueOf(sex2_cnt) + "名、" + 
                                              "合計" + String.valueOf(sex_cnt) + "名" );
        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }//printTotal()の括り


    /**
     *  明細データを抽出(中学)
     *
     */
    private String statementMeisaiJ(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,DESIREDIV,NATPUBPRIDIV, ");
            stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[10])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[10] + "' AND ");
            }
            stb.append("           TESTDIV = '"+param[1]+"' ");
            //2005.08.23
            if (param[2].equals("1")) //一般受験者
                stb.append("       AND (EXAMNO < '3000' OR '4000' <= EXAMNO) ");
            if (param[2].equals("2")) //附属出身者
                stb.append("       AND '3000' <= EXAMNO AND EXAMNO < '4000' ");
            if (param[2].equals("4")) //NO015
                stb.append("       AND SCALASHIPDIV = '02' ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           TESTDIV = '"+param[1]+"' AND ");
            stb.append("           A_SCORE IS NOT NULL ");
            stb.append("    ) ");
            //志望区分マスタ：志望連番MAX値
            stb.append(",WISHDIV AS ( ");
            stb.append("    SELECT DESIREDIV,WISHNO,COURSECD||MAJORCD||EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           TESTDIV = '"+param[1]+"' AND ");
            stb.append("           (DESIREDIV,WISHNO) IN ( ");
            stb.append("                SELECT DESIREDIV,MAX(WISHNO) AS MAX_WISHNO ");
            stb.append("                FROM   ENTEXAM_WISHDIV_MST ");
            stb.append("                WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("                       TESTDIV = '"+param[1]+"' ");
            stb.append("                GROUP BY DESIREDIV ");
            stb.append("                ) ");
            stb.append("    ) ");
            //最終判定コース
            stb.append(",LAST_COURSE AS ( ");
            stb.append("    SELECT T1.EXAMNO, ");
            stb.append("           CASE WHEN T1.JUDGEMENT = '7' THEN T4.COURSE ");//否
            stb.append("                WHEN T1.SUC_COURSE IS NOT NULL THEN T1.SUC_COURSE ");//合
            stb.append("                ELSE NULL END AS COURSE ");
            stb.append("    FROM   EXAM_BASE T1 ");
            stb.append("           LEFT JOIN WISHDIV T4 ON T4.DESIREDIV=T1.DESIREDIV ");
            stb.append("    ) ");
            //成績：前期（素点）
            stb.append(",SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO, ");
            stb.append("           SUM(T2.A_SCORE) AS SCORE_KEI, ");//NO012
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '1' THEN T2.A_SCORE ELSE NULL END) AS SCORE1, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '2' THEN T2.A_SCORE ELSE NULL END) AS SCORE2, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '3' THEN T2.A_SCORE ELSE NULL END) AS SCORE3, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '4' THEN T2.A_SCORE ELSE NULL END) AS SCORE4 ");
            stb.append("    FROM   EXAM_BASE T1, EXAM_SCORE T2 ");
            stb.append("    WHERE  T2.EXAMNO=T1.EXAMNO ");
            stb.append("    GROUP BY T1.EXAMNO ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.DESIREDIV, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    GROUP BY W1.DESIREDIV ");
            stb.append("    ) ");

            //成績：後期
            stb.append(",EXAM_SCORE_I AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE AS SCORE ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  TESTSUBCLASSCD IN('1','2','4') ");
            stb.append("    ) ");
            stb.append(",EXAM_SCORE_TH AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE AS SCORE ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  TESTSUBCLASSCD IN('1','2') ");
            stb.append("    UNION ");
            stb.append("    SELECT EXAMNO,'0' AS TESTSUBCLASSCD,MAX(A_SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  TESTSUBCLASSCD IN('3','4') ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    ) ");
            //成績：後期(合計・平均・順位)(I)
            stb.append(",SCORE_I AS ( ");
            stb.append("    SELECT T2.EXAMNO ");
            stb.append("          ,SUM(SCORE) AS KEI ");
            stb.append("          ,DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI ");
            stb.append("          ,RANK() OVER (ORDER BY SUM(SCORE) DESC) AS RNK ");
            stb.append("    FROM   EXAM_BASE T2, EXAM_WISH W2,EXAM_SCORE_I S2 ");
            stb.append("    WHERE  S2.EXAMNO=T2.EXAMNO AND ");
            stb.append("           W2.DESIREDIV=T2.DESIREDIV AND ");
            stb.append("          (W2.MARK1 IN('I') OR W2.MARK2 IN('I') OR W2.MARK3 IN('I')) ");
            stb.append("    GROUP BY T2.EXAMNO ");
            stb.append("    ) ");
            //成績：後期(合計・平均・順位)(TH)
            stb.append(",SCORE_TH AS ( ");
            stb.append("    SELECT T2.EXAMNO ");
            stb.append("          ,SUM(SCORE) AS KEI ");
            stb.append("          ,DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI ");
            stb.append("          ,RANK() OVER (ORDER BY SUM(SCORE) DESC) AS RNK ");
            stb.append("    FROM   EXAM_BASE T2, EXAM_WISH W2,EXAM_SCORE_TH S2 ");
            stb.append("    WHERE  S2.EXAMNO=T2.EXAMNO AND ");
            stb.append("           W2.DESIREDIV=T2.DESIREDIV AND ");
            stb.append("          (W2.MARK1 IN('T','H') OR W2.MARK2 IN('T','H') OR W2.MARK3 IN('T','H')) ");
            stb.append("    GROUP BY T2.EXAMNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       T1.EXAMNO,T1.NAME,T1.NAME_KANA, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T1.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ");
            stb.append("       T4.EXAMCOURSE_ABBV AS JUDGE1, ");
//NO012↓
        //?附属でテストありの者の場合
        if (param[2].equals("2") && param[9] != null) {
            stb.append("       CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' AND T2.SCORE_KEI IS NOT NULL THEN '否' ");
            stb.append("            WHEN T1.JUDGEMENT = '7' AND T2.SCORE_KEI IS NULL THEN '欠' ");
            stb.append("            WHEN T1.JUDGEMENT = '8' THEN '欠' "); //NO017
            stb.append("            ELSE NULL END AS JUDGE2, ");//判定結果：否
        //?以外の者の場合
        } else {
            stb.append("       CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' THEN '否' ");
            stb.append("            WHEN T2.SCORE_KEI IS NULL THEN '欠' ");
            stb.append("            WHEN T1.JUDGEMENT = '8' THEN '欠' "); //NO017
            stb.append("            ELSE NULL END AS JUDGE2, ");//判定結果：否
        }
//NO012↑
            stb.append("       T2.SCORE1,T2.SCORE2,T2.SCORE3,T2.SCORE4, ");
            stb.append("       CASE WHEN T8.EXAMCOURSE_MARK = 'I' THEN T5.KEI ELSE T6.KEI END AS SCORE_SUM, ");
            stb.append("       CASE WHEN T8.EXAMCOURSE_MARK = 'I' THEN T5.HEI ELSE T6.HEI END AS SCORE_AVG, ");
            stb.append("       CASE WHEN T8.EXAMCOURSE_MARK = 'I' THEN T5.RNK ELSE T6.RNK END AS SCORE_RNK, ");
            stb.append("       T1.DESIREDIV,VALUE(T3.MARK1,'') AS MARK1,VALUE(T3.MARK2,'') AS MARK2,VALUE(T3.MARK3,'') AS MARK3, ");
            stb.append("       T1.NATPUBPRIDIV, ");
            stb.append("       CASE WHEN T1.NATPUBPRIDIV = '9' THEN '*' ELSE NULL END AS NATPUB_NAME ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN EXAM_WISH T3 ON T3.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD = T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("       LEFT JOIN SCORE_I T5 ON T5.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN SCORE_TH T6 ON T6.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN LAST_COURSE T7 ON T7.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T8 ON T8.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T8.COURSECD||T8.MAJORCD||T8.EXAMCOURSECD = T7.COURSE ");
            stb.append("ORDER BY T1.TESTDIV,T1.EXAMNO ");
//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisaiJ error!",e);
        }
        return stb.toString();

    }//statementMeisaiJ()の括り


    /**
     *  明細データを抽出(高校)---2005.09.01
     *
     */
    private String statementMeisaiH(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,DESIREDIV,A_TOTAL,B_TOTAL,SHDIV,FS_CD,APPLICANTDIV, ");//---2005.09.04
            stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE,LOCATIONCD ");//2005.10.27
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[10])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[10] + "' AND ");
            }
            if (param[2].equals("1")) //一般受験者
                stb.append("       (EXAMNO < '5000' OR '6000' <= EXAMNO) ");
            if (param[2].equals("2")) //附属出身者
                stb.append("       '5000' <= EXAMNO AND EXAMNO < '6000' ");
            if (param[2].equals("3")) //スポーツ推薦者---2005.09.04
                stb.append("       APPLICANTDIV = '3' ");
            if (param[2].equals("4")) //NO015
                stb.append("       SCALASHIPDIV = '02' ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    ) ");
            //成績：（素点）
            stb.append(",SCORE AS ( ");
            stb.append("    SELECT EXAMNO, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN A_SCORE ELSE NULL END) AS SCORE1_1, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN A_SCORE ELSE NULL END) AS SCORE1_2, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN A_SCORE ELSE NULL END) AS SCORE1_3, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN A_SCORE ELSE NULL END) AS SCORE1_4, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN A_SCORE ELSE NULL END) AS SCORE1_5, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN B_SCORE ELSE NULL END) AS SCORE2_1, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN B_SCORE ELSE NULL END) AS SCORE2_2, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN B_SCORE ELSE NULL END) AS SCORE2_3, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN B_SCORE ELSE NULL END) AS SCORE2_4, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN B_SCORE ELSE NULL END) AS SCORE2_5 ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.DESIREDIV, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_ABBV ELSE NULL END) AS ABBV1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_ABBV ELSE NULL END) AS ABBV2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_ABBV ELSE NULL END) AS ABBV3, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    GROUP BY W1.DESIREDIV ");
            stb.append("    ) ");
            //郵便番号マスタ---2005.09.13
            stb.append(",ZIPCD AS ( ");
            stb.append("    SELECT NEW_ZIPCD,MAX(ZIPNO) AS ZIPNO_MAX ");
            stb.append("    FROM   ZIPCD_MST ");
            stb.append("    GROUP BY NEW_ZIPCD ");
            stb.append("    ) ");
            stb.append(",ZIPCD2 AS ( ");
            stb.append("    SELECT NEW_ZIPCD,CITY ");
            stb.append("    FROM   ZIPCD_MST ");
            stb.append("    WHERE  ZIPNO IN (SELECT ZIPNO_MAX FROM ZIPCD) ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV, ");
            stb.append("       T1.EXAMNO,T1.NAME,T1.NAME_KANA, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX,N1.ABBV1 AS SEX_NAME, ");
            stb.append("       T1.DESIREDIV, ");
            stb.append("       CASE WHEN T3.MARK1 = 'S' THEN T3.ABBV1 ELSE NULL END AS ABBV1_1, ");
            stb.append("       CASE WHEN T3.MARK1 = 'K' THEN T3.ABBV1 ELSE NULL END AS ABBV1_2, ");
            stb.append("       CASE WHEN T3.MARK1 = 'T' THEN T3.ABBV1 ELSE NULL END AS ABBV1_3, ");//NO009
            stb.append("       CASE WHEN T3.MARK1 = 'P' THEN T3.ABBV1 ELSE NULL END AS ABBV1_4, ");//NO009
            stb.append("       T3.ABBV2,T3.ABBV3, ");
            stb.append("       T1.SHDIV,N2.ABBV1 AS SHDIV_NAME, ");
            stb.append("       CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9' THEN T4.EXAMCOURSE_ABBV ELSE NULL END AS JUDGE1, ");
//NO012↓
        //?附属でテストありの者の場合
        if (param[2].equals("2") && param[9] != null) {
            stb.append("       CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9' THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' AND (T1.A_TOTAL IS NOT NULL OR T1.B_TOTAL IS NOT NULL) THEN '否' ");
            stb.append("            WHEN T1.JUDGEMENT = '7' AND T1.A_TOTAL IS NULL AND T1.B_TOTAL IS NULL THEN '欠' ");
            stb.append("            ELSE NULL END AS JUDGE2, ");//判定結果：否
        //?以外の者の場合
        } else {
            stb.append("       CASE WHEN (T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9' THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' THEN '否' ");
            stb.append("            WHEN T1.A_TOTAL IS NULL AND T1.B_TOTAL IS NULL THEN '欠' ");
            stb.append("            ELSE NULL END AS JUDGE2, ");//判定結果：否
        }
//NO012↑
            stb.append("       T1.A_TOTAL AS TOTAL1,T1.B_TOTAL AS TOTAL2, ");
            stb.append("       T2.SCORE1_1,T2.SCORE1_2,T2.SCORE1_3,T2.SCORE1_4,T2.SCORE1_5, ");
            stb.append("       CASE WHEN T3.MARK1 = 'S' THEN T2.SCORE2_3 ELSE NULL END AS SCORE2_3, ");
            stb.append("       CASE WHEN T3.MARK1 = 'K' THEN T2.SCORE2_5 ELSE NULL END AS SCORE2_5, ");
            stb.append("       T1.LOCATIONCD, N4.NAME1 AS LOC_NAME, ");//市区町村---NO005
            stb.append("       T1.FS_CD,VALUE(T5.FINSCHOOL_NAME,'') AS FS_NAME ");
            stb.append("       ,CASE WHEN T1.APPLICANTDIV = '3' THEN N3.NAME1 ELSE NULL END AS APPLICANT ");//---2005.09.04
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN EXAM_WISH T3 ON T3.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD = T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T1.SEX ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L006' AND N2.NAMECD2=T1.SHDIV ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L005' AND N3.NAMECD2=T1.APPLICANTDIV ");//---2005.09.04
            stb.append("       LEFT JOIN NAME_MST N4 ON N4.NAMECD1='L007' AND N4.NAMECD2=T1.LOCATIONCD ");//NO005
            stb.append("       LEFT JOIN FINSCHOOL_MST T5 ON T5.FINSCHOOLCD=T1.FS_CD ");
            stb.append("ORDER BY T1.TESTDIV,T1.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisaiH error!",e);
        }
        return stb.toString();

    }//statementMeisaiH()の括り


    /**
     *  試験区分毎の総ページ数を取得(共通)---2005.09.01
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[10])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[10] + "' AND ");
            }
            //中学
            if (param[5].equals("1")) {
                stb.append("       TESTDIV = '"+param[1]+"' AND ");
                if (param[2].equals("1")) //一般受験者
                    stb.append("   (EXAMNO < '3000' OR '4000' <= EXAMNO) ");
                if (param[2].equals("2")) //附属出身者
                    stb.append("   '3000' <= EXAMNO AND EXAMNO < '4000' ");
            }
            //高校
            if (param[5].equals("2")) {
                if (param[2].equals("1")) //一般受験者
                    stb.append("   (EXAMNO < '5000' OR '6000' <= EXAMNO) ");
                if (param[2].equals("2")) //附属出身者
                    stb.append("   '5000' <= EXAMNO AND EXAMNO < '6000' ");
                if (param[2].equals("3")) //スポーツ推薦者---2005.09.04
                    stb.append("   APPLICANTDIV = '3' ");
                if (param[2].equals("4")) //NO015
                    stb.append("   SCALASHIPDIV = '02' ");
            }
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV, ");
            if (param[5].equals("1")) 
                stb.append("   CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            if (param[5].equals("2")) 
                stb.append("   CASE WHEN 0 < MOD(COUNT(*),25) THEN COUNT(*)/25 + 1 ELSE COUNT(*)/25 END AS COUNT ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("GROUP BY T1.TESTDIV ");
            stb.append("ORDER BY T1.TESTDIV ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り


    /**
     *  附属で１人でも得点データがある場合（テストあり：試験区分毎）---NO012
     *
     */
    private String statementFuzokuScore(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TESTDIV,COUNT(*) AS CNT_F ");
            stb.append("FROM   ENTEXAM_SCORE_DAT ");
            stb.append("WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            //中学
            if (param[5].equals("1")) {
                stb.append("   TESTDIV = '"+param[1]+"' AND ");
                stb.append("   EXAMNO BETWEEN '3000' AND '3999' AND ");
            }
            //高校
            if (param[5].equals("2")) {
                stb.append("   EXAMNO BETWEEN '5000' AND '5999' AND ");
            }
            stb.append("       A_SCORE IS NOT NULL ");
            stb.append("GROUP BY TESTDIV ");
        } catch( Exception e ){
            log.warn("statementFuzokuScore error!",e);
        }
        return stb.toString();

    }//statementFuzokuScore()の括り



}//クラスの括り
