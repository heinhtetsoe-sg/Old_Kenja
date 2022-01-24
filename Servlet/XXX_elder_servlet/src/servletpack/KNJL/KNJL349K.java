// kanji=漢字
/*
 * $Id: 45eccd4355b92ca92205078b50575b8afa4741b2 $
 *
 * 作成日: 2005/08/09 11:25:40 - JST
 * 作成者: m-yama
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
 *  学校教育システム 賢者 [入試]  前・後期重複志願者名簿
 *
 *  2005/08/09  作成  m-yama
 *  2005/08/25  NO001 m-yama    出力帳票/手続・入学出力可否追加に伴う修正/不合格者は、コース印字なし
 *  2005/09/05  NO002 m-yama    手続辞退者の条件を変更（入学区分がNullで手続未の人）
 *  2005/12/20  NO003 m-yama    手続辞退者×表示
 *  2005/12/22  NO004 m-yama    重複出願者(氏名ORかな氏名一致)追加
 *  2005/12/23  NO005 m-yama    合計欄追加
 *  2005/12/26  NO006 m-yama    合計欄追加 349_2.frm
 *                              作成時刻→作成日に変更
 *  2006/01/05  NO007 o-naka    重複合格 合格者のみのリストにするよう修正
 *  2006/01/06  NO008 m-yama    附属を抜く、コースマスタの年度指定を追加。
 *  2006/01/06  NO009 m-yama    重複合格は、フォーム変更また、それに伴う修正。
 *  2006/01/08  NO010 yamashiro 付属出身者(国公私立区分=9)は付属欄に"Ｆ"を出力する
 *  2006/01/16  NO011 m-yama    合格者のみコースを出力。
 *  2006/01/17  NO012 m-yama    SQLでの合否区分の判定にVALUEを使用する。
 *  2006/10/24  NO013 m-yama    OUTPRINTの5以外は、条件に生年月日を追加。
 *  2006/11/10  NO014 m-yama    ヘッダデータの出力箇所を変更した。
 * @author m-yama
 * @version $Id: 45eccd4355b92ca92205078b50575b8afa4741b2 $
 */
public class KNJL349K {

    private static final Log log = LogFactory.getLog(KNJL349K.class);
    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[14];        //NO001
        String sqlsort = "";

    //  パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");           //年度
            param[1]  = request.getParameter("OUTPUT");         //ソート順1:受験番号順、2:氏名＋氏名かな順
            param[2]  = request.getParameter("JHFLG");          //中高判定フラグ1:中学、2:高校
            //NO001↓
            param[10] = request.getParameter("OUTPRINT");       //出力帳票種別1:重複志願、2:重複受験、3:重複合格、4:重複受験者平均点比較表、5:重複志願(氏名ORかな氏名一致)
            param[11] = request.getParameter("OUTPUT3");        //手続・入学欄印字
            param[13] = request.getParameter("SPECIAL_REASON_DIV");         // 特別理由            
            //NO001↑
            if (param[1].equals("1")){
                sqlsort = "t1.EXAMNO";
            }else {
                sqlsort = "t1.NAME_KANA";
            }
        } catch( Exception ex ) {
            log.error("Param read error!");
        }
    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJL349]param["+ia+"]="+param[ia]);

        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param,sqlsort));       //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));               //設定データpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        //SVF出力
        Set_Detail_1(db2,svf,param,ps1,ps2);

        //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        preStatClose(ps1,ps2);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[13] + "' ");
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

    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        //タイトル年度
        try {
            param[8] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

        //  学校
        if (param[2].equals("1")) param[9] = "中学校";
        if (param[2].equals("2")) param[9] = "高等学校";


        //  タイトル
        if (param[10].equals("1")) param[12] = "重複志願者名簿";
        if (param[10].equals("2")) param[12] = "重複受験者名簿";
        if (param[10].equals("3")) param[12] = "重複合格者名簿";
        if (param[10].equals("4")) param[12] = "重複受験者平均点比較表";
        if (param[10].equals("5")) param[12] = "重複志願者名簿";

        //  作成日(現在処理日)の取得 NO006
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[3] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;

        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
        //  試験区分の取得
        try {
            param[6] = "後期";
            param[7] = "前期";
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//Set_Head()の括り

    /**SVF-FORM**/
    private boolean Set_Detail_1(
    DB2UDB db2,
    Vrw32alp svf,
    String param[],
    PreparedStatement ps1,
    PreparedStatement ps2)
    {
        int girlcnt       = 0;      //女子カウンタ
        int passgirlcnt   = 0;      //合格女子カウンタ
        int unpassgirlcnt = 0;      //不合格女子カウンタ
        int unatndgirlcnt = 0;      //欠席女子カウンタ

        int mancnt       = 0;       //男子カウンタ
        int passmancnt   = 0;       //合格男子カウンタ
        int unpassmancnt = 0;       //不合格男子カウンタ
        int unatndmancnt = 0;       //欠席男子カウンタ

        int pagecnt = 1;            //現在ページ
        String allpage = "" ;       //最終ページ
        try {
            //トータルページ取得
            ResultSet rs1 = ps2.executeQuery();
            while( rs1.next() ){
                allpage = rs1.getString("TOTAL_PAGE");
            }
            if (param[10].equals("5")) {
                svf.VrSetForm("KNJL349_2.frm", 1);
            }else if (param[10].equals("3")){   //NO009
                svf.VrSetForm("KNJL349_3.frm", 1);
            }else {
                svf.VrSetForm("KNJL349.frm", 1);
            }
            rs1.close();

            //明細データ
            ResultSet rs = ps1.executeQuery();
            int gyo     = 1;            //行数カウント用
            int number  = 1;            //行番号
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    pagecnt++;
                    svf.VrEndPage();
                }
                //ヘッダ出力 NO014
                printHeader(db2,svf,param,pagecnt,allpage);
                //明細出力 NO002
                if (param[10].equals("5")){
                    printData2(svf,param,rs,gyo,number);
                    //合計用カウンタ更新 NO006
                    if (rs.getString("SEX1").equals("*")){
                        girlcnt++;
                    }else {
                        mancnt++;
                    }
                    if (rs.getString("SEX2").equals("*")){
                        passgirlcnt   = setCnt(rs.getString("JUDG2"),"合",passgirlcnt);
                        unpassgirlcnt = setCnt(rs.getString("JUDG2"),"否",unpassgirlcnt);
                        unatndgirlcnt = setCnt(rs.getString("JUDG2"),"欠",unatndgirlcnt);
                    }else {
                        passmancnt    = setCnt(rs.getString("JUDG2"),"合",passmancnt);
                        unpassmancnt  = setCnt(rs.getString("JUDG2"),"否",unpassmancnt);
                        unatndmancnt  = setCnt(rs.getString("JUDG2"),"欠",unatndmancnt);
                    }
                    gyo = gyo+2;
                    number++;
                }else {
                    printData(svf,param,rs,gyo);
                    //合計用カウンタ更新
                    if (rs.getString("SEX").equals("*")){
                        girlcnt++;
                        //NO009
                        if (param[10].equals("3")){
                            passgirlcnt   = setCnt(rs.getString("JUDG2"),"合",passgirlcnt);
                            unpassgirlcnt = setCnt(rs.getString("JUDG2"),"否",unpassgirlcnt);
                            unatndgirlcnt = setCnt(rs.getString("JUDG2"),"欠",unatndgirlcnt);
                        }else {
                            passgirlcnt   = setCnt(rs.getString("JUDG"),"合",passgirlcnt);
                            unpassgirlcnt = setCnt(rs.getString("JUDG"),"否",unpassgirlcnt);
                            unatndgirlcnt = setCnt(rs.getString("JUDG"),"欠",unatndgirlcnt);
                        }
                    }else {
                        mancnt++;
                        //NO009
                        if (param[10].equals("3")){
                            passmancnt    = setCnt(rs.getString("JUDG2"),"合",passmancnt);
                            unpassmancnt  = setCnt(rs.getString("JUDG2"),"否",unpassmancnt);
                            unatndmancnt  = setCnt(rs.getString("JUDG2"),"欠",unatndmancnt);
                        }else {
                            passmancnt    = setCnt(rs.getString("JUDG"),"合",passmancnt);
                            unpassmancnt  = setCnt(rs.getString("JUDG"),"否",unpassmancnt);
                            unatndmancnt  = setCnt(rs.getString("JUDG"),"欠",unatndmancnt);
                        }
                    }
                    gyo++;
                }
                nonedata = true;
            }
            rs.close();
            if (nonedata){
                String mansp1   = "";
                String mansp2   = "";
                String girlsp1  = "";
                String girlsp2  = "";
                String totalsp1 = "";
                String totalsp2 = "";

                mansp1   = spSet(mancnt);
                girlsp1  = spSet(girlcnt);
                mansp2   = spSet(passmancnt);
                girlsp2  = spSet(passgirlcnt);
                totalsp1 = spSet(girlcnt+mancnt);
                totalsp2 = spSet(passgirlcnt+passmancnt);

                svf.VrsOut("TOTAL_MEMBER1"      , "・重複志願者   男"+mansp1+String.valueOf(mancnt)+"名,女"+girlsp1+String.valueOf(girlcnt)+"名 合計"+totalsp1+String.valueOf(girlcnt+mancnt)+"名,"
                                                          +"・前期合格者 男"+mansp2+String.valueOf(passmancnt)+"名,女"+girlsp2+String.valueOf(passgirlcnt)+"名 合計"+totalsp2+String.valueOf(passgirlcnt+passmancnt)+"名");

                mansp1   = spSet(unpassmancnt);
                girlsp1  = spSet(unpassgirlcnt);
                mansp2   = spSet(unatndmancnt);
                girlsp2  = spSet(unatndgirlcnt);
                totalsp1 = spSet(unpassgirlcnt+unpassmancnt);
                totalsp2 = spSet(unatndgirlcnt+unatndmancnt);

                svf.VrsOut("TOTAL_MEMBER2"      , "・前期不合格者 男"+mansp1+String.valueOf(unpassmancnt)+"名,女"+girlsp1+String.valueOf(unpassgirlcnt)+"名 合計"+totalsp1+String.valueOf(unpassgirlcnt+unpassmancnt)+"名,"
                                                          +"・前期欠席者 男"+mansp2+String.valueOf(unatndmancnt)+"名,女"+girlsp2+String.valueOf(unatndgirlcnt)+"名 合計"+totalsp2+String.valueOf(unatndgirlcnt+unatndmancnt)+"名");
                svf.VrEndPage();
            }

        } catch( Exception ex ) {
            log.error("Set_Detail_1 read error!");
        }
        return nonedata;

    }//Set_Detail_1()の括り

    /**スペースをセット*/
    private String spSet(int spcnt)
    {
        String sp = "";
        try {
            if (spcnt < 10){
                sp = "  ";
            }else if(spcnt < 100) {
                sp = " ";
            }
        } catch( Exception ex ) {
            log.warn("spSetint set error!",ex);
        }

        return sp;

    }//spSet()の括り

    /**カウンタを更新*/
    private int setCnt(String judg,String equalVal,int cnt)
    {
        if (null != judg && judg.equals(equalVal)){
            cnt++;
        }
        return cnt;
    }//printHeader()の括り

    /**ヘッダーデータをセット*/
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],int pagecnt,String allpage)
    {
        try {
            svf.VrsOut("NENDO"      , String.valueOf(param[8]) );
            svf.VrsOut("TITLE"      , String.valueOf(param[12]) );
            svf.VrsOut("TESTDIV1"       , String.valueOf(param[6]) );
            svf.VrsOut("TESTDIV2"       , String.valueOf(param[7]) );
            svf.VrsOut("DATE"           , String.valueOf(param[3]) );
            if (param[10].equals("5")){
                svf.VrsOut("TOTALPAGE"  , allpage );
            }else {
                svf.VrsOut("TOTAL_PAGE" , allpage );
            }
            svf.VrsOut("PAGE"           , String.valueOf(pagecnt) );
            if (param[10].equals("5")){
                svf.VrsOut("SUBTITLE"   , "(漢字氏名orかな氏名 一致)" );
            }
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り

    /**検索一致データをセット*/
    private void printData(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len1 = "0";
        try {
            svf.VrsOutn("EXAMNO1"       ,gyo    ,rs.getString("EXAMNO"));
            svf.VrsOutn("DESIREDIV1"    ,gyo    ,rs.getString("EXAMCOURSE_MARK"));
            if (!param[10].equals("3")){    //NO009
                svf.VrsOutn("ATTACH"        ,gyo    ,rs.getString("FUZOKU"));
            }else {
                //NO011
                if (rs.getString("JUDG1").equals("合")){
                    svf.VrsOutn("EXAMCOURSE1"   ,gyo    ,rs.getString("LASTCOURSE1"));
                }
            }
            svf.VrsOutn("SEX"           ,gyo    ,rs.getString("SEX"));
            if (rs.getString("NAME") != null){
                len1 = (10 < (rs.getString("NAME").length())) ? "2" : "1" ;
                svf.VrsOutn("NAME"+len1 ,gyo    ,rs.getString("NAME"));
            }
            svf.VrsOutn("EXAMNO2"       ,gyo    ,rs.getString("EXAMNO2"));
            svf.VrsOutn("DESIREDIV2"    ,gyo    ,rs.getString("EXAMCOURSE_MARK2"));
            svf.VrsOutn("POINT"     ,gyo    ,rs.getString("A_TOTAL"));
            svf.VrsOutn("ORDER"     ,gyo    ,rs.getString("A_TOTAL_RANK"));
            if (!param[10].equals("3")){    //NO009
                svf.VrsOutn("JUDGEMENT" ,gyo    ,rs.getString("JUDG"));
                //NO011
                if (rs.getString("JUDG").equals("合")){ //NO001
                    svf.VrsOutn("EXAMCOURSE"    ,gyo    ,rs.getString("LASTCOURSE"));
                }
            }else {
                svf.VrsOutn("JUDGEMENT" ,gyo    ,rs.getString("JUDG2"));
                //NO011
                if (rs.getString("JUDG2").equals("合")){    //NO001
                    svf.VrsOutn("EXAMCOURSE2"   ,gyo    ,rs.getString("LASTCOURSE2"));
                }
            }
            if (param[11] != null){ //NO001
                svf.VrsOutn("PROCEDUREDIV",gyo  ,rs.getString("PROCEDUREDIV"));
                svf.VrsOutn("ENTDIV"        ,gyo    ,rs.getString("ENTDIV"));
            }
        } catch( Exception ex ) {
            log.warn("printData read error!",ex);
        }

    }//printData()の括り

    /**検索一致データをセット NO002*/
    private void printData2(Vrw32alp svf,String param[],ResultSet rs,int gyo,int number)
    {
        String len1 = "0";
        try {
            svf.VrsOutn("NUMBER"        ,number ,String.valueOf(number));
            //後期
            svf.VrsOutn("EXAMNO"   ,gyo ,rs.getString("EXAMNO1"));
            svf.VrsOutn("TESTDIV"  ,gyo ,"後期");
            svf.VrsOutn("DESIREDIV",gyo ,rs.getString("EXAMCOURSE_MARK1"));
            svf.VrsOutn("ATTACH"   ,gyo ,rs.getString("FUZOKU1"));
            svf.VrsOutn("SEX"      ,gyo ,rs.getString("SEX1"));
            svf.VrsOutn("BIRTHDAY" ,gyo ,rs.getString("BIRTHDAY1").replace('-', '/'));
            if (rs.getString("NAME1") != null){
                len1 = (10 < (rs.getString("NAME1").length())) ? "2" : "1" ;
                svf.VrsOutn("NAME"+len1 ,gyo    ,rs.getString("NAME1"));
            }
            if (rs.getString("NAME_KANA1") != null){
                len1 = (10 < (rs.getString("NAME_KANA1").length())) ? "2" : "1" ;
                svf.VrsOutn("KANA"+len1 ,gyo    ,rs.getString("NAME_KANA1"));
            }
            svf.VrsOutn("JUDGEMENT" ,gyo    ,rs.getString("JUDG1"));
            //NO011
            if (rs.getString("JUDG1").equals("合")){
                svf.VrsOutn("EXAMCOURSE"    ,gyo    ,rs.getString("LASTCOURSE1"));
            }
            if (param[11] != null){
                svf.VrsOutn("PROCEDUREDIV",gyo  ,rs.getString("PROCEDUREDIV1"));
                svf.VrsOutn("ENTDIV"        ,gyo    ,rs.getString("ENTDIV1"));
            }

            //前期
            svf.VrsOutn("EXAMNO"   ,gyo+1   ,rs.getString("EXAMNO2"));
            svf.VrsOutn("TESTDIV"  ,gyo+1   ,"前期");
            svf.VrsOutn("DESIREDIV",gyo+1   ,rs.getString("EXAMCOURSE_MARK2"));
            svf.VrsOutn("ATTACH"   ,gyo+1   ,rs.getString("FUZOKU2"));
            svf.VrsOutn("SEX"      ,gyo+1   ,rs.getString("SEX2"));
            svf.VrsOutn("BIRTHDAY" ,gyo+1   ,rs.getString("BIRTHDAY2").replace('-', '/'));
            if (rs.getString("NAME2") != null){
                len1 = (10 < (rs.getString("NAME2").length())) ? "2" : "1" ;
                svf.VrsOutn("NAME"+len1 ,gyo+1  ,rs.getString("NAME2"));
            }
            if (rs.getString("NAME_KANA2") != null){
                len1 = (10 < (rs.getString("NAME_KANA2").length())) ? "2" : "1" ;
                svf.VrsOutn("KANA"+len1 ,gyo+1  ,rs.getString("NAME_KANA2"));
            }
            svf.VrsOutn("JUDGEMENT" ,gyo+1  ,rs.getString("JUDG2"));
            //NO011
            if (rs.getString("JUDG2").equals("合")){
                svf.VrsOutn("EXAMCOURSE"    ,gyo+1  ,rs.getString("LASTCOURSE2"));
            }
            if (param[11] != null){ //NO001
                svf.VrsOutn("PROCEDUREDIV",gyo+1    ,rs.getString("PROCEDUREDIV2"));
                svf.VrsOutn("ENTDIV"        ,gyo+1  ,rs.getString("ENTDIV2"));
            }

        } catch( Exception ex ) {
            log.warn("printData2 read error!",ex);
        }

    }//printData2()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[],String sqlsort)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH DESIRETBL1 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV1,TESTDIV AS TESTDIV1, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK1 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD  ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD  ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t1.WISHNO = '1' ");
            stb.append("),DESIRETBL2 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV2,TESTDIV AS TESTDIV2, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK2 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD  ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD  ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t1.WISHNO = '2' ");
            stb.append("),DESIRETBL3 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV3,TESTDIV AS TESTDIV3, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK3 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD  ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD  ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t1.WISHNO = '3' ");
            stb.append("),DESIRETBL4 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV4,TESTDIV AS TESTDIV4, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK4 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD  ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD  ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t1.WISHNO = '4' ");
            stb.append("),DESIRETBLZENKI AS ( ");
            stb.append("SELECT DESIREDIV1 AS DESIREDIV, ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK1 IS NULL THEN '' ELSE EXAMCOURSE_MARK1 END || ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK2 IS NULL THEN '' ELSE EXAMCOURSE_MARK2 END || ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK3 IS NULL THEN '' ELSE EXAMCOURSE_MARK3 END || ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK4 IS NULL THEN '' ELSE EXAMCOURSE_MARK4 END AS EXAMCOURSE_MARK ");
            stb.append("FROM   DESIRETBL1 t1 ");
            stb.append("       LEFT JOIN DESIRETBL2 ON DESIREDIV2 = DESIREDIV1 AND TESTDIV2 = '1' ");
            stb.append("       LEFT JOIN DESIRETBL3 ON DESIREDIV3 = DESIREDIV1 AND TESTDIV3 = '1' ");
            stb.append("       LEFT JOIN DESIRETBL4 ON DESIREDIV4 = DESIREDIV1 AND TESTDIV4 = '1' ");
            stb.append("WHERE  TESTDIV1 = '1' ");
            stb.append("),DESIRETBLKOUKI AS ( ");
            stb.append("SELECT DESIREDIV1 AS DESIREDIV, ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK1 IS NULL THEN '' ELSE EXAMCOURSE_MARK1 END || ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK2 IS NULL THEN '' ELSE EXAMCOURSE_MARK2 END || ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK3 IS NULL THEN '' ELSE EXAMCOURSE_MARK3 END || ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK4 IS NULL THEN '' ELSE EXAMCOURSE_MARK4 END AS EXAMCOURSE_MARK ");
            stb.append("FROM   DESIRETBL1 t1 ");
            stb.append("       LEFT JOIN DESIRETBL2 ON DESIREDIV2 = DESIREDIV1 AND TESTDIV2 = '2' ");
            stb.append("       LEFT JOIN DESIRETBL3 ON DESIREDIV3 = DESIREDIV1 AND TESTDIV3 = '2' ");
            stb.append("       LEFT JOIN DESIRETBL4 ON DESIREDIV4 = DESIREDIV1 AND TESTDIV4 = '2' ");
            stb.append("WHERE  TESTDIV1 = '2' ");
            stb.append("),EXAM_ZENKI AS ( ");
            stb.append("SELECT EXAMNO,NAME,NAME_KANA,t1.DESIREDIV, ");
            stb.append("       t2.EXAMCOURSE_MARK,t1.BIRTHDAY, ");   //NO013
            stb.append("       CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("       CASE WHEN VALUE(NATPUBPRIDIV,'0') = '9' THEN 'F' ELSE '' END AS FUZOKU, ");  //NO010
            stb.append("       CASE WHEN ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT <= '6') OR t1.JUDGEMENT = '9') THEN '合' WHEN t1.JUDGEMENT = '7' THEN '否' WHEN t1.JUDGEMENT = '8' THEN '欠' ELSE '' END AS JUDG, ");
            stb.append("       CASE WHEN ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT <= '6') OR t1.JUDGEMENT = '9') THEN t3.EXAMCOURSE_MARK ELSE CHAR(RIGHT(t2.EXAMCOURSE_MARK,1),1) END AS LASTCOURSE, ");
            stb.append("       CASE WHEN t1.PROCEDUREDIV = '2' THEN '○' WHEN t1.PROCEDUREDIV = '1' AND t1.ENTDIV IS NULL THEN '×' ELSE '' END AS PROCEDUREDIV, ");    //NO002
            stb.append("       CASE WHEN t1.PROCEDUREDIV = '2' AND t1.ENTDIV = '1' THEN '×' WHEN t1.ENTDIV = '2' THEN '○' ELSE '' END AS ENTDIV, ");  //NO003
            stb.append("       t1.A_TOTAL,t1.A_TOTAL_RANK ");
            stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("       LEFT JOIN DESIRETBLZENKI t2 ON t1.DESIREDIV = t2.DESIREDIV ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t3 ON t1.SUC_COURSECD = t3.COURSECD ");
            stb.append("       AND t3.ENTEXAMYEAR = '"+param[0]+"' ");  //NO008
            stb.append("       AND t1.SUC_MAJORCD = t3.MAJORCD ");
            stb.append("       AND t1.SUC_COURSECODE = t3.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR='"+param[0]+"' ");
            if (!"9".equals(param[13])) {
                stb.append("       AND t1.SPECIAL_REASON_DIV = '" + param[13] + "' ");
            }
            stb.append("       AND t1.EXAMNO NOT BETWEEN '3000' AND '3999' ");  //NO008
            if (param[10].equals("2")){ //NO001
                stb.append("       AND t1.TESTDIV='1' ");
                stb.append("       AND VALUE(t1.JUDGEMENT,'88') NOT IN ('8') "); //NO012
            }else {
                stb.append("       AND t1.TESTDIV='1' ");
                if (param[10].equals("3")) stb.append(" AND ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT <= '6') OR t1.JUDGEMENT = '9') ");//NO007
            }
            stb.append("),EXAM_KOUKI AS ( ");
            if (param[10].equals("5") || param[10].equals("3")) {
                stb.append("SELECT EXAMNO,NAME,NAME_KANA,t1.DESIREDIV, ");
                stb.append("       t2.EXAMCOURSE_MARK,t1.BIRTHDAY, ");  //NO013
                stb.append("       CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ");
                stb.append("       CASE WHEN VALUE(NATPUBPRIDIV,'0') = '9' THEN 'F' ELSE '' END AS FUZOKU, ");  //NO010
                stb.append("       CASE WHEN ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT <= '6') OR t1.JUDGEMENT = '9') THEN '合' WHEN t1.JUDGEMENT = '7' THEN '否' WHEN t1.JUDGEMENT = '8' THEN '欠' ELSE '' END AS JUDG, ");
                stb.append("       CASE WHEN ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT <= '6') OR t1.JUDGEMENT = '9') THEN t3.EXAMCOURSE_MARK ELSE CHAR(RIGHT(t2.EXAMCOURSE_MARK,1),1) END AS LASTCOURSE, ");
                stb.append("       CASE WHEN t1.PROCEDUREDIV = '2' THEN '○' WHEN t1.PROCEDUREDIV = '1' AND t1.ENTDIV IS NULL THEN '×' ELSE '' END AS PROCEDUREDIV, ");    //NO002
                stb.append("       CASE WHEN t1.PROCEDUREDIV = '2' AND t1.ENTDIV = '1' THEN '×' WHEN t1.ENTDIV = '2' THEN '○' ELSE '' END AS ENTDIV, ");  //NO003
                stb.append("       t1.A_TOTAL,t1.A_TOTAL_RANK ");
                stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT t1 ");
                stb.append("       LEFT JOIN DESIRETBLKOUKI t2 ON t1.DESIREDIV = t2.DESIREDIV ");
                stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t3 ON t1.SUC_COURSECD = t3.COURSECD ");
                stb.append("       AND t3.ENTEXAMYEAR = '"+param[0]+"' ");  //NO008
                stb.append("       AND t1.SUC_MAJORCD = t3.MAJORCD ");
                stb.append("       AND t1.SUC_COURSECODE = t3.EXAMCOURSECD ");
            }else {
                stb.append("SELECT EXAMNO,NAME,NAME_KANA,t1.DESIREDIV, ");
                stb.append("       EXAMCOURSE_MARK,t1.BIRTHDAY, "); //NO013
                stb.append("       CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ");
                stb.append("       CASE WHEN VALUE(NATPUBPRIDIV,'0') = '9' THEN 'F' ELSE '' END AS FUZOKU ");  //NO010
                stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT t1 ");
                stb.append("       LEFT JOIN DESIRETBLKOUKI t2 ON t1.DESIREDIV = t2.DESIREDIV ");
            }
            stb.append("WHERE  t1.ENTEXAMYEAR='"+param[0]+"' ");
            if (!"9".equals(param[13])) {
                stb.append("       AND t1.SPECIAL_REASON_DIV = '" + param[13] + "' ");
            }
            stb.append("       AND t1.EXAMNO NOT BETWEEN '3000' AND '3999' ");  //NO008
            if (param[10].equals("2")){ //NO001
                stb.append("       AND t1.TESTDIV='2' ");
                stb.append("       AND VALUE(t1.JUDGEMENT,'88') NOT IN ('8') "); //NO012
            }else {
                stb.append("       AND t1.TESTDIV='2' ");
                if (param[10].equals("3")) stb.append(" AND ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT <= '6') OR t1.JUDGEMENT = '9') ");//NO007
            }
            stb.append(") ");
            //NO002
            if (param[10].equals("5")) {
                stb.append("SELECT ");
                stb.append("    t1.EXAMNO AS EXAMNO1,t2.EXAMNO AS EXAMNO2, ");
                stb.append("    t1.EXAMCOURSE_MARK AS EXAMCOURSE_MARK1,t2.EXAMCOURSE_MARK AS EXAMCOURSE_MARK2, ");
                stb.append("    t1.FUZOKU AS FUZOKU1,t2.FUZOKU AS FUZOKU2,t1.SEX AS SEX1,t2.SEX AS SEX2, ");
                stb.append("    t1.NAME AS NAME1,t2.NAME AS NAME2,t1.NAME_KANA AS NAME_KANA1,t2.NAME_KANA AS NAME_KANA2, ");
                stb.append("    t1.EXAMCOURSE_MARK AS EXAMCOURSE_MARK1,t2.EXAMCOURSE_MARK AS EXAMCOURSE_MARK2, ");
                stb.append("    t1.A_TOTAL AS A_TOTAL1,t2.A_TOTAL AS A_TOTAL2, ");
                stb.append("    t1.A_TOTAL_RANK AS A_TOTAL_RANK1,t2.A_TOTAL_RANK AS A_TOTAL_RANK2, ");
                stb.append("    t1.JUDG AS JUDG1,t2.JUDG AS JUDG2,t1.LASTCOURSE AS LASTCOURSE1,t2.LASTCOURSE AS LASTCOURSE2, ");
                stb.append("    t1.PROCEDUREDIV AS PROCEDUREDIV1,t2.PROCEDUREDIV AS PROCEDUREDIV2, ");
                stb.append("    t1.ENTDIV AS ENTDIV1,t2.ENTDIV AS ENTDIV2,t1.BIRTHDAY AS BIRTHDAY1,t2.BIRTHDAY AS BIRTHDAY2 ");
                stb.append("FROM ");
                stb.append("    EXAM_KOUKI t1, ");
                stb.append("    EXAM_ZENKI t2 ");
                stb.append("WHERE  (t1.NAME = t2.NAME AND VALUE(t1.NAME_KANA,'') <> VALUE(t2.NAME_KANA,'') ) ");    //NO012
                stb.append("       OR (VALUE(t1.NAME,'') <> VALUE(t2.NAME,'') AND t1.NAME_KANA = t2.NAME_KANA) ");  //NO012
            }else if (param[10].equals("3")){   //NO009
                stb.append("SELECT ");
                stb.append("    t1.EXAMNO,t1.EXAMCOURSE_MARK,t1.FUZOKU,t1.SEX,t1.NAME, ");
                stb.append("    t2.EXAMNO AS EXAMNO2,t2.EXAMCOURSE_MARK AS EXAMCOURSE_MARK2,t2.A_TOTAL, ");
                stb.append("    t2.A_TOTAL_RANK,t1.JUDG AS JUDG1,t2.JUDG AS JUDG2,t1.LASTCOURSE AS LASTCOURSE1,t2.LASTCOURSE AS LASTCOURSE2, ");
                stb.append("    t2.PROCEDUREDIV,t2.ENTDIV,t1.BIRTHDAY ");
                stb.append("FROM ");
                stb.append("    EXAM_KOUKI t1 ");
                stb.append("    LEFT JOIN EXAM_ZENKI t2 ON t1.NAME = t2.NAME ");
                stb.append("    AND t1.NAME_KANA = t2.NAME_KANA ");
                stb.append("    AND t1.BIRTHDAY = t2.BIRTHDAY ");   //NO013
                stb.append("WHERE  t1.NAME = t2.NAME AND ");
                stb.append("       t1.NAME_KANA = t2.NAME_KANA AND ");
                stb.append("       t1.BIRTHDAY = t2.BIRTHDAY ");    //NO013
            }else {
                stb.append("SELECT ");
                stb.append("    t1.EXAMNO,t1.EXAMCOURSE_MARK,t1.FUZOKU,t1.SEX,t1.NAME, ");
                stb.append("    t2.EXAMNO AS EXAMNO2,t2.EXAMCOURSE_MARK AS EXAMCOURSE_MARK2,A_TOTAL, ");
                stb.append("    A_TOTAL_RANK,JUDG,LASTCOURSE,PROCEDUREDIV,ENTDIV,t1.BIRTHDAY ");
                stb.append("FROM ");
                stb.append("    EXAM_KOUKI t1 ");
                stb.append("    LEFT JOIN EXAM_ZENKI t2 ON t1.NAME = t2.NAME ");
                stb.append("    AND t1.NAME_KANA = t2.NAME_KANA ");
                stb.append("    AND t1.BIRTHDAY = t2.BIRTHDAY ");   //NO013
                stb.append("WHERE  t1.NAME = t2.NAME AND ");
                stb.append("       t1.NAME_KANA = t2.NAME_KANA AND ");
                stb.append("       t1.BIRTHDAY = t2.BIRTHDAY ");    //NO013
            }
            stb.append("ORDER BY "+sqlsort+" ");

//log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH EXAM_ZENKI AS ( ");
            stb.append("SELECT NAME,NAME_KANA,BIRTHDAY ");
            stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT "); //NO013
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            if (!"9".equals(param[13])) {
                stb.append("       AND SPECIAL_REASON_DIV = '" + param[13] + "' ");
            }
            stb.append("       AND EXAMNO NOT BETWEEN '3000' AND '3999' "); //NO008
            if (param[10].equals("2")){ //NO001
                stb.append("       AND TESTDIV='1' ");
                stb.append("       AND VALUE(JUDGEMENT,'88') NOT IN ('8') ");    //NO012
            }else {
                stb.append("       AND TESTDIV='1' ");
                if (param[10].equals("3")) stb.append(" AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");//NO007
            }
            stb.append("),EXAM_KOUKI AS ( ");
            stb.append("SELECT NAME,NAME_KANA,BIRTHDAY ");   //NO013
            stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            if (!"9".equals(param[13])) {
                stb.append("       AND SPECIAL_REASON_DIV = '" + param[13] + "' ");
            }
            stb.append("       AND EXAMNO NOT BETWEEN '3000' AND '3999' "); //NO008
            if (param[10].equals("2")){ //NO001
                stb.append("       AND TESTDIV='2' ");
                stb.append("       AND VALUE(JUDGEMENT,'88') NOT IN ('8') ");    //NO012
            }else {
                stb.append("       AND TESTDIV='2' ");
                if (param[10].equals("3")) stb.append(" AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");//NO007
            }
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS CNT, ");
            stb.append("    CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TOTAL_PAGE ");
            stb.append("FROM ");
            stb.append("    EXAM_KOUKI t1, ");
            stb.append("    EXAM_ZENKI t2 ");
            if (param[10].equals("5")){ //NO002
                stb.append("WHERE  (t1.NAME = t2.NAME AND VALUE(t1.NAME_KANA,'') <> VALUE(t2.NAME_KANA,'') ) ");    //NO012
                stb.append("       OR (VALUE(t1.NAME,'') <> VALUE(t2.NAME,'') AND t1.NAME_KANA = t2.NAME_KANA) ");  //NO012
            }else {
                stb.append("WHERE  t1.NAME = t2.NAME AND ");
                stb.append("       t1.NAME_KANA = t2.NAME_KANA AND ");
                stb.append("       t1.BIRTHDAY = t2.BIRTHDAY ");    //NO013
            }

//log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            ps1.close();
            ps2.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
