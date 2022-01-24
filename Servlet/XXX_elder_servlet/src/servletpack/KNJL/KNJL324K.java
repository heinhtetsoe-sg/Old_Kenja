// kanji=漢字
/*
 * $Id: 25619ec4f5b21a60af78c644b72470f8f74af6d9 $
 *
 * 作成日: 2005/09/02 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３２４＞  入学試験得点分布表(高校)
 *
 *  2005/09/02 nakamoto 作成日
 *  2005/09/09 nakamoto スポーツ推薦者は、対象外--->対象外にしていない 2005.11.18調査
 *  2005/09/13 nakamoto No.1の得点分布の人数が違う不具合を修正
 *
 *  2005/10/28 nakamoto NO001：KNJL324_1の最下位の人数は、その点数以下の人数全てを表記する。（例：190点は、194点以下の人数）
 *                           ：(質問・回答待ち)最下位については、Ｎｏ．１のみですか？
 *                      NO002：標準偏差値を表記する。
 *                      NO003：指示画面からの「合格者を除く除かないのパラメータ」について対応
 *  2005/11/18 nakamoto NO004：志願・欠席・受験の人数は、第１志望を集計するよう修正
 *                      NO005：No.2,No.3の「全体」の人数が違っている不具合を修正
 *
 *  2005/11/22 m-yama   NO006：スポーツ推薦を集計するよう修正
 *  2005/12/30 nakamoto NO007 コースコード記号の変更による修正 Q→T R→P
 *  2006/01/30 nakamoto NO008 No.2,No.3の志願・欠席・受験に「進学」も表記。表示順を理数→国際→特進→進学に変更
 *                      NO009 最高・最低・平均の表について、条件を追加。
 *                            ○「専願(1)・併願(2)」の条件を追加（No.2,3,4,5）
 *                            ○「第１志望を集計」の条件を追加（No.1,2,3,4,5）
 *  2006/01/31 nakamoto NO010 最高・最低・平均の表の「平均の平均点」について、
 *                            ○基礎データからではなく得点データから抽出---KNJL359と同じ値になるように修正
 *  2006/02/07 nakamoto NO011 NO001の(質問・回答待ち)は、No.2〜No.5も同じように対応
 *                      NO012 前年度平均を出力するよう修正
 */
public class KNJL324K {


    private static final Log log = LogFactory.getLog(KNJL324K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[8];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("OUTPUT");                      //出力帳票 1:理数科・国際科,2:特進(専),3:特進(併),4:進学(専),5:進学(併)
            param[2] = request.getParameter("BORDER");                      //ボーダー
            if (param[1].equals("2") || param[1].equals("4")) param[3] = "1";//専願
            if (param[1].equals("3") || param[1].equals("5")) param[3] = "2";//併願

            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[6] = request.getParameter("PASSFLG");                     //合格者フラグ 1:除く,2:除かない NO003
            param[7] = request.getParameter("SPECIAL_REASON_DIV");          //特別理由
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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[7] + "' ");
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

    //  フォーム
        if (param[1].equals("1")) svf.VrSetForm("KNJL324_1.frm", 1);//理数科・国際科
        if (param[1].equals("2")) svf.VrSetForm("KNJL324_2.frm", 1);//特進(専)
        if (param[1].equals("3")) svf.VrSetForm("KNJL324_2.frm", 1);//特進(併)
        if (param[1].equals("4")) svf.VrSetForm("KNJL324_3.frm", 1);//進学(専)
        if (param[1].equals("5")) svf.VrSetForm("KNJL324_3.frm", 1);//進学(併)
        setInfluenceName(db2, svf, param);
    //  専併
        if (param[3] != null) {
            if (param[3].equals("1")) svf.VrsOut("SHDIV"  , "専願" );
            if (param[3].equals("2")) svf.VrsOut("SHDIV"  , "併願" );
        }

    //  次年度
        try {
            svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" );
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校・タイトルNo
        try {
            svf.VrsOut("SCHOOLDIV"    , (param[5].equals("1")) ? "中学校" : "高等学校" );
            svf.VrsOut("TITLE"        , param[1] );
        } catch( Exception e ){
            log.warn("SchoolName get error!",e);
        }

    //  作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE" , KNJ_EditDate.h_format_JP(returnval.val3) );
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //表共通：ボーダー
            int score[] = new int[47];
            int gyo_limit = printBorder(db2,svf,param,score);
            //表共通：明細
            if (printScoreTable(db2,svf,param,score,gyo_limit)) nonedata = true;
            //出力
            if (nonedata) {
                //表２３：志願・欠席・受験
                if (!param[1].equals("1")) printSiganTable23(db2,svf,param);
                //表共通
                printPerfectTable1(db2,svf,param);  //満点
                printAverageTable(db2,svf,param);   //最高点・最低点・平均点・標準偏差
                printPreAverageTable(db2,svf,param);//前年度平均 NO012

                svf.VrEndPage();//出力
            }
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**(表共通)ボーダーより得点範囲をセット*/
    private int printBorder(DB2UDB db2,Vrw32alp svf,String param[],int score[])
    {
        int gyo_limit = 2;
        try {
            for (int ia = 0; ia < 47; ia++) {
                if ((Integer.parseInt(param[2]) - (ia * 5)) < 0) break;
                score[ia] = Integer.parseInt(param[2]) - (ia * 5);
                svf.VrsOutn("SCORE"     ,ia + 1    , String.valueOf(score[ia]) );
                svf.VrsOutn("SCORE1"    ,ia + 1    , String.valueOf(score[ia]) );
                svf.VrsOutn("SCORE2"    ,ia + 1    , String.valueOf(score[ia]) );
                gyo_limit = ia + 1;
            }
        } catch( Exception ex ) {
            log.warn("printBorder read error!",ex);
        }
        return gyo_limit;

    }//printBorder()の括り


    /**(表１)理数科・国際科の満点をセット*/
    private void printPerfectTable1(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query(getSqlPerfectTable1(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                svf.VrsOut("EXAM_COURSE"+rs.getString("MARK_NO")  , rs.getString("PERFECT") );//満点
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printPerfectTable1 read error!",ex);
        }

    }//printPerfectTable1()の括り


    /**(表２３)志願・欠席・受験をセット*/
    private void printSiganTable23(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query(getSqlSiganTable23(param));
            ResultSet rs = db2.getResultSet();

            int hyo = 1;//NO008
            while( rs.next() ){
                //行---NO007---NO008
                int gyo = ((rs.getString("MARK")).equals("S")) ? hyo : 
                          ((rs.getString("MARK")).equals("K")) ? hyo+1 : 
                          ((rs.getString("MARK")).equals("T")) ? hyo+2 : 
                          ((rs.getString("MARK")).equals("P")) ? hyo+3 : 
                          ((rs.getString("MARK")).equals("KEI")) ? hyo+4 : 0 ;
                //志願
                svf.VrsOutn("APPLI_BOY"   ,gyo    , rs.getString("SIGAN1") );
                svf.VrsOutn("APPLI_GIRL"  ,gyo    , rs.getString("SIGAN2") );
                svf.VrsOutn("APPLI_TOTAL" ,gyo    , rs.getString("SIGAN3") );
                //欠席
                svf.VrsOutn("ABSENT_BOY"  ,gyo    , rs.getString("KESEK1") );
                svf.VrsOutn("ABSENT_GIRL" ,gyo    , rs.getString("KESEK2") );
                svf.VrsOutn("ABSENT_TOTAL",gyo    , rs.getString("KESEK3") );
                //受験
                svf.VrsOutn("EXAM_BOY"    ,gyo    , rs.getString("JUKEN1") );
                svf.VrsOutn("EXAM_GIRL"   ,gyo    , rs.getString("JUKEN2") );
                svf.VrsOutn("EXAM_TOTAL"  ,gyo    , rs.getString("JUKEN3") );
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSiganTable23 read error!",ex);
        }

    }//printSiganTable23()の括り


    /**(表共通)最高点・最低点・平均点・標準偏差をセット*/
    private void printAverageTable(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query(getSqlAverageTable(param));
            ResultSet rs = db2.getResultSet();

            String mark_sub = "";
            while( rs.next() ){
                //平均
                if ((rs.getString("TESTSUBCLASSCD")).equals("9")) {
                    mark_sub = rs.getString("MARK");
                    svf.VrsOutn("AVERAGE"+mark_sub ,3 , rs.getString("HEIKIN") );
                //各試験科目
                } else {
                    mark_sub = rs.getString("MARK") + "_" + rs.getString("TESTSUBCLASSCD");
                    svf.VrsOutn("SUBCLASS"+mark_sub ,1 , rs.getString("MAXSCO") );
                    svf.VrsOutn("SUBCLASS"+mark_sub ,2 , rs.getString("MINSCO") );
                    svf.VrsOutn("SUBCLASS"+mark_sub ,3 , rs.getString("HEIKIN") );

                    svf.VrsOutn("SUBCLASS"+mark_sub ,5 , rs.getString("HENSA") );//NO002
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printAverageTable read error!",ex);
        }

    }//printAverageTable()の括り


    /**(表共通)前年度平均 NO012をセット*/
    private void printPreAverageTable(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            param[0] = String.valueOf(Integer.parseInt(param[0]) - 1);
            db2.query(getSqlAverageTable(param));
            ResultSet rs = db2.getResultSet();

            String mark_sub = "";
            while( rs.next() ){
                //平均
                if ((rs.getString("TESTSUBCLASSCD")).equals("9")) {
                    mark_sub = rs.getString("MARK");
                    svf.VrsOutn("AVERAGE"+mark_sub ,4 , rs.getString("HEIKIN") );
                //各試験科目
                } else {
                    mark_sub = rs.getString("MARK") + "_" + rs.getString("TESTSUBCLASSCD");
                    svf.VrsOutn("SUBCLASS"+mark_sub ,4 , rs.getString("HEIKIN") );
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printPreAverageTable read error!",ex);
        }

    }//printPreAverageTable()の括り


    /**(表共通)明細データ印刷処理*/
    private boolean printScoreTable(DB2UDB db2,Vrw32alp svf,String param[],int score[],int gyo_limit)
    {
        boolean nonedata = false;
        try {
            db2.query(getSqlScoreTable(param));
            ResultSet rs = db2.getResultSet();

            int score_cnt[][] = new int[7][gyo_limit];//{計,男子,女子,なし,Ａ,Ｂ,累計}
            String mark_no = "d";
            String shdiv = "d";
            String len = "0";
            while( rs.next() ){
                //コースまたは専併のブレイク時、
                if (!mark_no.equals(rs.getString("MARK_NO")) || !shdiv.equals(rs.getString("SHDIV"))) {
log.debug("mark_no="+rs.getString("MARK_NO")+" shdiv="+rs.getString("SHDIV"));
                    //累計
                    if (!mark_no.equals("d") && !shdiv.equals("d")) 
                        setRuikei(svf,param,gyo_limit,score_cnt,mark_no,len);
                    //初期化
                    for (int gyo = 0; gyo < gyo_limit; gyo++) 
                        for (int ic = 0; ic < 7; ic++) score_cnt[ic][gyo] = 0;
                }
                //保管
                mark_no = rs.getString("MARK_NO");
                shdiv = rs.getString("SHDIV");
                len = (!param[1].equals("1")) ? "" : (shdiv.equals("2")) ? "_1" : "_2" ;
                //明細
                setMeisai1(svf,param,rs,gyo_limit,score,score_cnt,mark_no,len);

                nonedata = true;
            }
            //累計
            if (nonedata) setRuikei(svf,param,gyo_limit,score_cnt,mark_no,len);
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printScoreTable read error!",ex);
        }
        return nonedata;

    }//printScoreTable()の括り


    /**明細をセット*/
    private void setMeisai1(Vrw32alp svf, String param[], ResultSet rs, int gyo_limit,
                            int score[], int score_cnt[][], String mark_no, String len )
    {
        try {
            for (int gyo = 0; gyo < gyo_limit; gyo++) {
/*****コメントをはずした---NO001*****/
                //最終行
                if (gyo == (gyo_limit-1)) {
                    if (rs.getInt("SCORE_G") < score[gyo-1]) {
                        setMeisai2(svf,param,rs,gyo,score_cnt,mark_no,len);
                        break;
                    }
                }
/**********/
                //その他の行
                if (score[gyo] <= rs.getInt("SCORE_G")) {
                    setMeisai2(svf,param,rs,gyo,score_cnt,mark_no,len);
                    break;
                }
            }
        } catch( Exception ex ) {
            log.warn("setMeisai1 read error!",ex);
        }

    }//setMeisai1()の括り


    /**明細をセット*/
    private void setMeisai2(Vrw32alp svf, String param[], ResultSet rs,
                            int gyo, int score_cnt[][], String mark_no, String len)
    {
        try {
            String score_fd[] = {"","BOY","GIRL"};
            int sex = rs.getInt("SEX");
            //男子,女子,計
            if (sex == 1 || sex == 2) {
                score_cnt[0][gyo] += 1;
                score_cnt[sex][gyo] += 1;
                svf.VrsOutn("TOTAL"+mark_no+len ,gyo + 1 , String.valueOf(score_cnt[0][gyo]) );
                svf.VrsOutn(score_fd[sex]+mark_no+len ,gyo + 1 , String.valueOf(score_cnt[sex][gyo]) );
                //第Xなし
                if (param[1].equals("1") || param[1].equals("2") || param[1].equals("3")) {//理数・国際・特進
                    if (rs.getString("NON") != null) {
                        score_cnt[3][gyo] += 1;
                        svf.VrsOutn("NON"+mark_no+len ,gyo + 1 , String.valueOf(score_cnt[3][gyo]) );
                    }
                }
                //Ａ
                if ((rs.getString("JUDGEMENT")).equals("A")) {
                    score_cnt[4][gyo] += 1;
                    svf.VrsOutn("JUDGE_A"+mark_no+len ,gyo + 1 , String.valueOf(score_cnt[4][gyo]) );
                }
                //Ｂ
                if ((rs.getString("JUDGEMENT")).equals("B")) {
                    score_cnt[5][gyo] += 1;
                    svf.VrsOutn("JUDGE_B"+mark_no+len ,gyo + 1 , String.valueOf(score_cnt[5][gyo]) );
                }
            }
        } catch( Exception ex ) {
            log.warn("setMeisai2 read error!",ex);
        }

    }//setMeisai2()の括り


    /**累計をセット*/
    private void setRuikei(Vrw32alp svf,String param[],int gyo_limit,int score_cnt[][],String mark_no,String len)
    {
        try {
            int judge_a = 0;
            int judge_b = 0;
            for (int gyo = 0; gyo < gyo_limit; gyo++) {
                //累計
                if (gyo == 0) score_cnt[6][gyo] = score_cnt[0][gyo];
                else          score_cnt[6][gyo] = score_cnt[6][gyo-1] + score_cnt[0][gyo];
                if (0 < score_cnt[6][gyo])
                    svf.VrsOutn("CUMULATIVE"+mark_no+len ,gyo + 1 , String.valueOf(score_cnt[6][gyo]) );
                //ＡＢ
                judge_a += score_cnt[4][gyo];
                judge_b += score_cnt[5][gyo];
            }
            //ＡＢ
            if (0 < judge_a) svf.VrsOutn("JUDGE_A"+mark_no+len ,48 , String.valueOf(judge_a) );
            if (0 < judge_b) svf.VrsOutn("JUDGE_B"+mark_no+len ,48 , String.valueOf(judge_b) );
        } catch( Exception ex ) {
            log.warn("setRuikei read error!",ex);
        }

    }//setRuikei()の括り


    /**
     *  (表１)理数科・国際科の満点を取得
     */
    private String getSqlPerfectTable1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //満点マスタ
            stb.append("WITH EXAM_PERFECT AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            if (param[1].equals("1")) stb.append("       SUM(B_PERFECT) AS PERFECT ");
            else                      stb.append("       SUM(A_PERFECT) AS PERFECT ");
            stb.append("    FROM   ENTEXAM_PERFECT_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    GROUP BY COURSECD||MAJORCD||EXAMCOURSECD ");
            stb.append("    ) ");
            //受験コースマスタ
            stb.append(",EXAM_COURSE AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("           EXAMCOURSE_NAME AS NAME, ");
            stb.append("           EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (param[1].equals("1")) 
                stb.append("       EXAMCOURSE_MARK IN ('S','K') ");
            if (param[1].equals("2") || param[1].equals("3")) 
                stb.append("       EXAMCOURSE_MARK IN ('T') ");//NO007
            if (param[1].equals("4") || param[1].equals("5")) 
                stb.append("       EXAMCOURSE_MARK IN ('P') ");//NO007
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.COURSE,T1.PERFECT, ");//満点
            stb.append("       CASE WHEN T2.MARK = 'S' THEN '1' ");//理数科
            stb.append("            WHEN T2.MARK = 'K' THEN '2' ");//国際科
            stb.append("            ELSE '' END AS MARK_NO, ");//特進・進学
            stb.append("       T2.ABBV,T2.MARK ");
            stb.append("FROM   EXAM_PERFECT T1, EXAM_COURSE T2 ");
            stb.append("WHERE  T1.COURSE = T2.COURSE ");
            stb.append("ORDER BY MARK_NO ");
        } catch( Exception e ){
            log.warn("getSqlPerfectTable1 error!",e);
        }
        return stb.toString();

    }//getSqlPerfectTable1()の括り


    /**
     *  (表共通)得点分布を取得
     */
    private String getSqlScoreTable(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,SHDIV,DESIREDIV,VALUE(SEX,'0') AS SEX ");
            if (param[3] != null) {//理数・国際以外
                stb.append("       ,A_TOTAL AS SCORE_G ");
            } else {//理数・国際
                stb.append("       ,B_TOTAL AS SCORE_G ");
            }
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            if (!"9".equals(param[7])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[7] + "' AND ");
            }
            if (param[3] != null) {//理数・国際以外
                stb.append("       SHDIV = '"+param[3]+"' AND ");
                stb.append("       A_TOTAL IS NOT NULL AND ");
            } else {//理数・国際
                stb.append("       B_TOTAL IS NOT NULL AND ");
            }
            stb.append("           (EXAMNO < '5000' OR '6000' <= EXAMNO) AND ");
            if (param[6].equals("1")){
                stb.append("       VALUE(JUDGEMENT,'88') NOT IN ('1','2','3','5','6','9') AND ");    //NO006
                stb.append("       VALUE(REGULARSUCCESS_FLG,'0') NOT IN ('1','2','3') AND ");   //NO006
            }
            stb.append("           VALUE(JUDGEMENT,'88') NOT IN ('8','0') ");
            stb.append("    ) ");

            //受験コースマスタ・志望区分マスタ
            stb.append(",EXAM_WISHDIV AS ( ");
            stb.append("    SELECT DESIREDIV,WISHNO, ");
            stb.append("           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ");
            stb.append("           W1.EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           W1.EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST W1, ENTEXAM_WISHDIV_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");

            //第２３なし・志望区分マスタ
            stb.append(",WISHDIV_MAX AS ( ");
            stb.append("    SELECT DESIREDIV,MAX(WISHNO) AS WISHNO_MAX ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    GROUP BY DESIREDIV ");
            stb.append("    ) ");
            if (param[1].equals("1")) //理数・国際
                stb.append(",WISHDIV_P AS (SELECT * FROM EXAM_WISHDIV WHERE MARK IN ('S','K')) ");
            if (param[1].equals("2") || param[1].equals("3")) //特進
                stb.append(",WISHDIV_P AS (SELECT * FROM EXAM_WISHDIV WHERE MARK = 'T') ");//NO007
            if (param[1].equals("4") || param[1].equals("5")) //進学
                stb.append(",WISHDIV_P AS (SELECT * FROM EXAM_WISHDIV WHERE MARK = 'P') ");//NO007
            stb.append(",WISHDIV_S AS (SELECT * FROM EXAM_WISHDIV WHERE MARK = 'S') ");
            stb.append(",WISHDIV_K AS (SELECT * FROM EXAM_WISHDIV WHERE MARK = 'K') ");
            stb.append(",WISHDIV_Q AS (SELECT * FROM EXAM_WISHDIV WHERE MARK = 'T') ");//NO007
            stb.append(",WISHDIV_PSK AS ( ");
            stb.append("    SELECT P.DESIREDIV,M.WISHNO_MAX, ");
            stb.append("           CASE WHEN S.DESIREDIV IS NOT NULL THEN S.COURSE ");
            stb.append("                WHEN K.DESIREDIV IS NOT NULL THEN K.COURSE ");
            stb.append("                WHEN Q.DESIREDIV IS NOT NULL THEN Q.COURSE ");
            stb.append("                ELSE P.COURSE END AS COURSE, ");
            stb.append("           CASE WHEN S.DESIREDIV IS NOT NULL THEN S.MARK ");
            stb.append("                WHEN K.DESIREDIV IS NOT NULL THEN K.MARK ");
            stb.append("                WHEN Q.DESIREDIV IS NOT NULL THEN Q.MARK ");
            stb.append("                ELSE P.MARK END AS MARK ");
            stb.append("    FROM   WISHDIV_P P ");
            stb.append("           LEFT JOIN WISHDIV_S S ON S.DESIREDIV = P.DESIREDIV ");
            stb.append("           LEFT JOIN WISHDIV_K K ON K.DESIREDIV = P.DESIREDIV ");
            stb.append("           LEFT JOIN WISHDIV_Q Q ON Q.DESIREDIV = P.DESIREDIV ");
            stb.append("           LEFT JOIN WISHDIV_MAX M ON M.DESIREDIV = P.DESIREDIV ");
            stb.append("    ) ");

            //ＡＢ・志願者事前相談データ
            stb.append(",EXAM_CONS AS ( ");
            stb.append("    SELECT EXAMNO,SHDIV,COURSECD||MAJORCD||EXAMCOURSECD AS COURSE,JUDGEMENT ");
            stb.append("    FROM   ENTEXAM_APPLICANTCONS_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (param[3] != null) //理数・国際以外
                stb.append("       SHDIV = '"+param[3]+"' AND ");
            stb.append("           COURSECD||MAJORCD||EXAMCOURSECD IN (SELECT DISTINCT COURSE FROM WISHDIV_P) ");//2005.09.05
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.DESIREDIV,T1.SHDIV, ");
            stb.append("       T3.COURSE,T3.MARK, ");
            stb.append("       CASE WHEN T3.MARK = 'S' THEN '1' ");
            stb.append("            WHEN T3.MARK = 'K' THEN '2' ");
            stb.append("            WHEN T3.MARK = 'T' THEN '3' ");//NO007
            stb.append("            WHEN T3.MARK = 'P' THEN '4' END AS MARK_NO, ");//NO007
            stb.append("       T3.WISHNO_MAX, ");
            if (param[1].equals("1")) {//理数・国際
                stb.append("   CASE WHEN T3.MARK = 'S' AND T3.WISHNO_MAX < '2' THEN 'non' ");//第２なし
                stb.append("        WHEN T3.MARK = 'K' AND T3.WISHNO_MAX < '2' THEN 'non' ");//第２なし
                stb.append("        ELSE NULL END AS NON, ");
            }
            if (param[1].equals("2") || param[1].equals("3")) {//特進
                stb.append("   CASE WHEN T3.MARK = 'S' AND T3.WISHNO_MAX < '3' THEN 'non' ");//第３なし
                stb.append("        WHEN T3.MARK = 'K' AND T3.WISHNO_MAX < '3' THEN 'non' ");//第３なし
                stb.append("        WHEN T3.MARK = 'T' AND T3.WISHNO_MAX < '2' THEN 'non' ");//第２なし//NO007
                stb.append("        ELSE NULL END AS NON, ");
            }
            stb.append("       CASE WHEN T5.JUDGEMENT = '1' THEN 'A' ");
            stb.append("            WHEN T5.JUDGEMENT = '2' THEN 'B' ELSE 'Z' END AS JUDGEMENT, ");//ＡＢ
            stb.append("       T1.EXAMNO,T1.SEX, ");
            stb.append("       T1.SCORE_G ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN WISHDIV_PSK T3 ON T3.DESIREDIV = T1.DESIREDIV ");
            if (param[3] != null) {//理数・国際以外
                stb.append("   LEFT JOIN EXAM_CONS T5 ON T5.EXAMNO = T1.EXAMNO AND T5.SHDIV = T1.SHDIV ");//2005.09.05
            } else {//理数・国際
                stb.append("   LEFT JOIN EXAM_CONS T5 ON T5.EXAMNO = T1.EXAMNO AND T5.SHDIV = T1.SHDIV AND T5.COURSE = T3.COURSE ");//2005.09.13
            }
            stb.append("WHERE  T3.COURSE IS NOT NULL ");
            stb.append("ORDER BY MARK_NO,T1.SHDIV DESC,T1.SCORE_G DESC ");
        } catch( Exception e ){
            log.warn("getSqlScoreTable error!",e);
        }
        return stb.toString();

    }//getSqlScoreTable()の括り


    /**
     *  (表２３)志願・欠席・受験を取得
     */
    private String getSqlSiganTable23(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,DESIREDIV,SEX, ");
            stb.append("           CASE WHEN JUDGEMENT = '8' THEN '2' ELSE '3' END AS JUDGE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            if (!"9".equals(param[7])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[7] + "' AND ");
            }
            stb.append("           (EXAMNO < '5000' OR '6000' <= EXAMNO) ");
            stb.append("    ) ");
            //受験コースマスタ・志望区分マスタ
            stb.append(",EXAM_WISHDIV AS ( ");
            stb.append("    SELECT DESIREDIV,WISHNO, ");
            stb.append("           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ");
            stb.append("           W1.EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           W1.EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST W1, ENTEXAM_WISHDIV_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W2.WISHNO = '1' AND ");//NO004
            stb.append("       W1.EXAMCOURSE_MARK IN('S','K','T','P') AND ");   //No.2,No.3,No.4,No.5 //NO008
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");
            //欠席・受験
            stb.append(",EXAM_CNT AS ( ");
            stb.append("    SELECT T2.COURSE,T2.MARK,T1.JUDGE,T1.SEX,COUNT(*) AS CNT ");
            stb.append("    FROM   EXAM_BASE T1 ");
            stb.append("           LEFT JOIN EXAM_WISHDIV T2 ON T2.DESIREDIV = T1.DESIREDIV ");
            stb.append("    WHERE  T2.COURSE IS NOT NULL ");
            stb.append("    GROUP BY T2.COURSE,T2.MARK,T1.JUDGE,T1.SEX ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT MARK ");
            stb.append("       ,SUM(CASE WHEN SEX = '1' THEN CNT ELSE 0 END) AS SIGAN1 ");
            stb.append("       ,SUM(CASE WHEN SEX = '2' THEN CNT ELSE 0 END) AS SIGAN2 ");
            stb.append("       ,SUM(CASE WHEN SEX = '1' OR SEX = '2' THEN CNT ELSE 0 END) AS SIGAN3 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '2' AND SEX = '1' THEN CNT ELSE 0 END) AS KESEK1 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '2' AND SEX = '2' THEN CNT ELSE 0 END) AS KESEK2 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '2' AND (SEX = '1' OR SEX = '2') THEN CNT ELSE 0 END) AS KESEK3 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '3' AND SEX = '1' THEN CNT ELSE 0 END) AS JUKEN1 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '3' AND SEX = '2' THEN CNT ELSE 0 END) AS JUKEN2 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '3' AND (SEX = '1' OR SEX = '2') THEN CNT ELSE 0 END) AS JUKEN3 ");
            stb.append("FROM   EXAM_CNT ");
            stb.append("GROUP BY MARK ");
            stb.append("UNION ");
            stb.append("SELECT 'KEI' AS MARK ");
            stb.append("       ,SUM(CASE WHEN SEX = '1' THEN CNT ELSE 0 END) AS SIGAN1 ");
            stb.append("       ,SUM(CASE WHEN SEX = '2' THEN CNT ELSE 0 END) AS SIGAN2 ");
            stb.append("       ,SUM(CASE WHEN SEX = '1' OR SEX = '2' THEN CNT ELSE 0 END) AS SIGAN3 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '2' AND SEX = '1' THEN CNT ELSE 0 END) AS KESEK1 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '2' AND SEX = '2' THEN CNT ELSE 0 END) AS KESEK2 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '2' AND (SEX = '1' OR SEX = '2') THEN CNT ELSE 0 END) AS KESEK3 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '3' AND SEX = '1' THEN CNT ELSE 0 END) AS JUKEN1 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '3' AND SEX = '2' THEN CNT ELSE 0 END) AS JUKEN2 ");
            stb.append("       ,SUM(CASE WHEN JUDGE = '3' AND (SEX = '1' OR SEX = '2') THEN CNT ELSE 0 END) AS JUKEN3 ");
            stb.append("FROM   EXAM_CNT ");
        } catch( Exception e ){
            log.warn("getSqlSiganTable23 error!",e);
        }
        return stb.toString();

    }//getSqlSiganTable23()の括り


    /**
     *  (表共通)最高点・最低点・平均点・標準偏差を取得
     */
    private String getSqlAverageTable(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,DESIREDIV, ");
            if (param[1].equals("1")) {//理数・国際
                stb.append("       B_TOTAL AS TOTAL ");
            } else {//理数・国際以外
                stb.append("       A_TOTAL AS TOTAL ");
            }
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            if (!"9".equals(param[7])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[7] + "' AND ");
            }
            stb.append("           (EXAMNO < '5000' OR '6000' <= EXAMNO) AND ");
            stb.append("           VALUE(JUDGEMENT,'88') NOT IN ('8','0') ");
            if (param[1].equals("2") || param[1].equals("4")) //専願
                stb.append("       AND SHDIV = '1' ");//NO009
            if (param[1].equals("3") || param[1].equals("5")) //併願
                stb.append("       AND SHDIV = '2' ");//NO009
            stb.append("    ) ");

            //受験コースマスタ・志望区分マスタ
            stb.append(",EXAM_WISHDIV AS ( ");
            stb.append("    SELECT DESIREDIV,WISHNO, ");
            stb.append("           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ");
            stb.append("           W1.EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           W1.EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST W1, ENTEXAM_WISHDIV_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W2.WISHNO = '1' AND ");//NO009
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");

            //EXAM_BASEとEXAM_WISHDIVをDESIREDIVでリンク
            stb.append(",EXAM_BASE2 AS ( ");
            stb.append("    SELECT T1.EXAMNO,T1.TOTAL,T2.COURSE,T2.MARK ");
            stb.append("    FROM   EXAM_BASE T1 ");
            stb.append("           LEFT JOIN EXAM_WISHDIV T2 ON T2.DESIREDIV = T1.DESIREDIV ");
            stb.append("    WHERE  T2.COURSE IS NOT NULL ");
            stb.append("    ) ");

            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD, ");
            if (param[1].equals("1")) {//理数・国際
                stb.append("       B_SCORE AS SCORE ");
            } else {//理数・国際以外
                stb.append("       A_SCORE AS SCORE ");
            }
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            if (param[1].equals("1")) {//理数・国際
                stb.append("       B_SCORE IS NOT NULL ");
            } else {//理数・国際以外
                stb.append("       A_SCORE IS NOT NULL ");
            }
            stb.append("    ) ");

            //志願者得点データ２---NO010
            stb.append(",EXAM_SCORE2 AS ( ");
            stb.append("    SELECT EXAMNO, ");
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    HAVING COUNT(*) = 5 ");
            stb.append("    ) ");

            //最高点・最低点・平均点・標準偏差
            stb.append(",EXAM_CALC AS ( ");
            stb.append("    SELECT T2.MARK,TESTSUBCLASSCD, ");
            stb.append("           MAX(SCORE) AS MAXSCO, ");
            stb.append("           MIN(SCORE) AS MINSCO, ");
            stb.append("           DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEIKIN ");
            stb.append("           ,DECIMAL(ROUND(STDDEV(SCORE)*10,0)/10,5,1) AS HENSA ");//NO002
            stb.append("    FROM   EXAM_SCORE T1 ");
            stb.append("           LEFT JOIN EXAM_BASE2 T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append("    GROUP BY T2.MARK,TESTSUBCLASSCD ");
            stb.append("    UNION ");
            stb.append("    SELECT T2.MARK,'9' AS TESTSUBCLASSCD, ");
            stb.append("           0 AS MAXSCO, ");
            stb.append("           0 AS MINSCO, ");
            stb.append("           DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEIKIN ");
            stb.append("           ,0 AS HENSA ");//NO002
            stb.append("    FROM   EXAM_SCORE2 T1 ");
            stb.append("           LEFT JOIN EXAM_BASE2 T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append("    GROUP BY T2.MARK ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT CASE WHEN MARK = 'K' THEN '2' ELSE '1' END AS MARK, ");
            stb.append("       TESTSUBCLASSCD,MAXSCO,MINSCO,HEIKIN,HENSA ");//NO002
            stb.append("FROM   EXAM_CALC ");
            if (param[1].equals("1")) //理数・国際
                stb.append("WHERE  MARK IN ('S','K') ");
            if (param[1].equals("2") || param[1].equals("3")) //特進
                stb.append("WHERE  MARK IN ('T') ");//NO007
            if (param[1].equals("4") || param[1].equals("5")) //進学
                stb.append("WHERE  MARK IN ('P') ");//NO007
            stb.append("ORDER BY MARK,TESTSUBCLASSCD ");
        } catch( Exception e ){
            log.warn("getSqlAverageTable error!",e);
        }
        return stb.toString();

    }//getSqlAverageTable()の括り



}//クラスの括り
