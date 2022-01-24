// kanji=漢字
/*
 * $Id: edd79dd79a276c4d91a0cb44ef0001e09e26b91f $
 *
 * 作成日: 2006/01/31 11:25:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３６２＞  合否判定一覧
 *
 *  2006/01/31 m-yama 作成日
 *  2006/02/06 m-yama NO001：演算箇所にVALUE(A,0)を使用するよう修正
 *  2006/02/11 m-yama NO002：「超過数」の計算にクラブ推薦者を含めない
 */
public class KNJL362K {


    private static final Log log = LogFactory.getLog(KNJL362K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                //次年度
            param[1] = request.getParameter("TESTDIV");             //試験区分
            param[2] = request.getParameter("JHFLG");               //中高フラグ
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


        //ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力
        if( printMain(db2,svf,param) ) nonedata = true;

        //該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

        //終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        svf.VrSetForm("KNJL362.frm", 4);

        //  次年度
        try {
            param[3] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

        //  作成日(現在処理日)の取得
        try {
            String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            String arr_ctrl_date[] = new String[3];
            int number = 0;
            while( rs.next() ){
                arr_ctrl_date[number] = rs.getString(1);
                number++;
            }
            rs.close();
            db2.commit();
            param[4] = KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分"+" 現在";
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        //  学校
        if (param[2].equals("1")) param[5] = "中学校";
        if (param[2].equals("2")) param[5] = "高等学校";

        //  試験区分の取得
        try {
            param[6] = "";
            if (!param[1].equals("99")){
                String sql = "SELECT ABBV1,NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '"+param[1]+"'";
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                while( rs.next() ){
                    param[6] = rs.getString("NAME1");
                }
                rs.close();
                db2.commit();
            }
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り

    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();

            int capacity_cnt   = 0;
            int total_cnt1     = 0;
            int capa_cnt1      = 0;
            int total_cnt2     = 0;
            int capa_cnt2      = 0;
            int s_success_cnt  = 0;
            int excess_cnt     = 0;
            //見出し
            printHeader(svf,param,rs);
            while( rs.next() ){
                //明細データ
                printScore(svf,param,rs);

                //合計
                total_cnt1    = total_cnt1 + rs.getInt("TOTALCNT1");
                capa_cnt1     = capa_cnt1 + rs.getInt("CAPA_CNT1");

                total_cnt2    = total_cnt2 + rs.getInt("TOTALCNT2");
                capa_cnt2     = capa_cnt2 + rs.getInt("CAPA_CNT2");

                capacity_cnt  = capacity_cnt  + rs.getInt("CAPACITY1");
                s_success_cnt = s_success_cnt + rs.getInt("S_SUCCESS_CNT");
                excess_cnt    = excess_cnt + rs.getInt("EXCESS");

                nonedata = true;
            }
            //合計印刷
            if (nonedata) {
                printTotal(svf,param,total_cnt1,capa_cnt1,total_cnt2,capa_cnt2,capacity_cnt,s_success_cnt,excess_cnt);
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    /**ヘッダーデータをセット*/
    private void printHeader(Vrw32alp svf,String param[],ResultSet rs)
    {
        try {

            svf.VrsOut("NENDO"        , param[3] );
            svf.VrsOut("SCHOOLDIV"    , param[5] );
            svf.VrsOut("TESTDIV"      , param[6] );
            svf.VrsOut("DATE"         , param[4] );

        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs)
    {
        try {

            //共通
            svf.VrsOut("COURSECODE"       ,rs.getString("COURSECD1") );           //コースコード
            svf.VrsOut("COURSENAME"       ,rs.getString("EXAMCOURSE_NAME1") );    //コース名
            svf.VrsOut("CAPA"             ,rs.getString("CAPACITY1") );           //定員
            svf.VrsOut("CLUB_REC"         ,rs.getString("S_SUCCESS_CNT") );       //クラブ推薦
            svf.VrsOut("OVERCNT"          ,rs.getString("EXCESS") );              //超過数

            //専願
            svf.VrsOut("JUDGE_ITEM1"      ,rs.getString("JUDGE_COL1") );          //判定項目
            svf.VrsOut("SCORE1_1"         ,rs.getString("BORDER_SCORE1") );       //合格点(基本)
            svf.VrsOut("SCORE1_2"         ,rs.getString("A_BORDER_SCORE1") );     //合格点(A)
            svf.VrsOut("SCORE1_3"         ,rs.getString("B_BORDER_SCORE1") );     //合格点(B)

            svf.VrsOut("TCNT1_1"          ,rs.getString("SUCCESS_CNT1") );        //合格者基本(計)
            svf.VrsOut("BCNT1_1"          ,rs.getString("SUCCESS_SEX1_CNT1") );   //合格者基本(男)
            svf.VrsOut("GCNT1_1"          ,rs.getString("SUCCESS_SEX2_CNT1") );   //合格者基本(女)
            svf.VrsOut("TCNT1_2"          ,rs.getString("A_SUCCESS_CNT1") );      //合格者A(計)
            svf.VrsOut("BCNT1_2"          ,rs.getString("A_SUCCESS_SEX1_CNT1") ); //合格者A(男)
            svf.VrsOut("GCNT1_2"          ,rs.getString("A_SUCCESS_SEX2_CNT1") ); //合格者A(女)
            svf.VrsOut("TCNT1_3"          ,rs.getString("B_SUCCESS_CNT1") );      //合格者B(計)
            svf.VrsOut("BCNT1_3"          ,rs.getString("B_SUCCESS_SEX1_CNT1") ); //合格者B(男)
            svf.VrsOut("GCNT1_3"          ,rs.getString("B_SUCCESS_SEX2_CNT1") ); //合格者B(女)
            svf.VrsOut("TOTAL1"           ,rs.getString("TOTALCNT1") );           //合格者計
            if (null != rs.getString("BACK_RATE1")){
                svf.VrsOut("BACKRATE1"        ,rs.getString("BACK_RATE1")+"%" );      //戻率
            }else {
                svf.VrsOut("BACKRATE1"        ,"%" );     //戻率
            }
            svf.VrsOut("CAPA_EXP1"        ,rs.getString("CAPA_CNT1") );           //収容見込

            //併願
            svf.VrsOut("JUDGE_ITEM2"      ,rs.getString("JUDGE_COL2") );          //判定項目
            svf.VrsOut("SCORE2_1"         ,rs.getString("BORDER_SCORE2") );       //合格点(基本)
            svf.VrsOut("SCORE2_2"         ,rs.getString("A_BORDER_SCORE2") );     //合格点(A)
            svf.VrsOut("SCORE2_3"         ,rs.getString("B_BORDER_SCORE2") );     //合格点(B)

            svf.VrsOut("TCNT2_1"          ,rs.getString("SUCCESS_CNT2") );        //合格者基本(計)
            svf.VrsOut("BCNT2_1"          ,rs.getString("SUCCESS_SEX1_CNT2") );   //合格者基本(男)
            svf.VrsOut("GCNT2_1"          ,rs.getString("SUCCESS_SEX2_CNT2") );   //合格者基本(女)
            svf.VrsOut("TCNT2_2"          ,rs.getString("A_SUCCESS_CNT2") );      //合格者A(計)
            svf.VrsOut("BCNT2_2"          ,rs.getString("A_SUCCESS_SEX1_CNT2") ); //合格者A(男)
            svf.VrsOut("GCNT2_2"          ,rs.getString("A_SUCCESS_SEX2_CNT2") ); //合格者A(女)
            svf.VrsOut("TCNT2_3"          ,rs.getString("B_SUCCESS_CNT2") );      //合格者B(計)
            svf.VrsOut("BCNT2_3"          ,rs.getString("B_SUCCESS_SEX1_CNT2") ); //合格者B(男)
            svf.VrsOut("GCNT2_3"          ,rs.getString("B_SUCCESS_SEX2_CNT2") ); //合格者B(女)
            svf.VrsOut("TOTAL2"           ,rs.getString("TOTALCNT2") );           //合格者計
            if (null != rs.getString("BACK_RATE2")){
                svf.VrsOut("BACKRATE2"        ,rs.getString("BACK_RATE2")+"%" );      //戻率
            }else {
                svf.VrsOut("BACKRATE2"        ,"%" );     //戻率
            }
            svf.VrsOut("CAPA_EXP2"        ,rs.getString("CAPA_CNT2") );           //収容見込

            svf.VrEndRecord();

        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り

    /**合計をセット*/
    private void printTotal(Vrw32alp svf,String param[],int total_cnt1,int capa_cnt1,
                            int total_cnt2,int capa_cnt2,int capacity_cnt,int s_success_cnt,int excess_cnt)
    {
        try {

            svf.VrsOut("ITEM"         ,"合計" );
            svf.VrsOut("CAPA"         ,String.valueOf(capacity_cnt) );
            svf.VrsOut("CLUB_REC"     ,String.valueOf(s_success_cnt) );
            svf.VrsOut("TOTAL1"       ,String.valueOf(total_cnt1) );
            svf.VrsOut("CAPA_EXP1"    ,String.valueOf(capa_cnt1) );
            svf.VrsOut("TOTAL2"       ,String.valueOf(total_cnt2) );
            svf.VrsOut("CAPA_EXP2"    ,String.valueOf(capa_cnt2) );
            svf.VrsOut("OVERCNT"      ,String.valueOf(excess_cnt) );

            svf.VrEndRecord();

        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }//printTotal()の括り

    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH SH1 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SHDIV AS SHDIV1, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS COURSECD1, ");
            stb.append("     T2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1, ");
            stb.append("     T2.CAPACITY AS CAPACITY1, ");
            stb.append("     T1.S_SUCCESS_CNT AS S_SUCCESS_CNT1, ");
            stb.append("     CASE WHEN T1.JUDGE_COL = '1' THEN '配点A' WHEN T1.JUDGE_COL = '2' THEN '配点B' ELSE '' END AS JUDGE_COL1, ");
            stb.append("     T1.BORDER_SCORE AS BORDER_SCORE1, ");
            stb.append("     T1.A_BORDER_SCORE AS A_BORDER_SCORE1, ");
            stb.append("     T1.B_BORDER_SCORE AS B_BORDER_SCORE1, ");
            stb.append("     T1.SUCCESS_CNT AS SUCCESS_CNT1, ");
            stb.append("     T1.SUCCESS_SEX1_CNT AS SUCCESS_SEX1_CNT1, ");
            stb.append("     T1.SUCCESS_SEX2_CNT AS SUCCESS_SEX2_CNT1, ");
            stb.append("     T1.A_SUCCESS_CNT AS A_SUCCESS_CNT1, ");
            stb.append("     T1.A_SUCCESS_SEX1_CNT AS A_SUCCESS_SEX1_CNT1, ");
            stb.append("     T1.A_SUCCESS_SEX2_CNT AS A_SUCCESS_SEX2_CNT1, ");
            stb.append("     T1.B_SUCCESS_CNT AS B_SUCCESS_CNT1, ");
            stb.append("     T1.B_SUCCESS_SEX1_CNT AS B_SUCCESS_SEX1_CNT1, ");
            stb.append("     T1.B_SUCCESS_SEX2_CNT AS B_SUCCESS_SEX2_CNT1, ");
            stb.append("     VALUE(T1.SUCCESS_CNT,0) + VALUE(T1.A_SUCCESS_CNT,0) + VALUE(T1.B_SUCCESS_CNT,0) AS TOTALCNT1, ");  //NO001
            stb.append("     T1.BACK_RATE AS BACK_RATE1, ");
            stb.append("     T1.CAPA_CNT AS CAPA_CNT1 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PASSINGMARK_MST T1 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.EXAMCOURSECD = T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("     AND T1.TESTDIV = '"+param[1]+"' ");
            stb.append("     AND SHDIV = '1' ");
            stb.append(" ), SH2 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SHDIV AS SHDIV2, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS COURSECD2, ");
            stb.append("     T2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ");
            stb.append("     T2.CAPACITY AS CAPACITY2, ");
            stb.append("     T1.S_SUCCESS_CNT AS S_SUCCESS_CNT2, ");
            stb.append("     CASE WHEN T1.JUDGE_COL = '1' THEN '配点A' WHEN T1.JUDGE_COL = '2' THEN '配点B' ELSE '' END AS JUDGE_COL2, ");
            stb.append("     T1.BORDER_SCORE AS BORDER_SCORE2, ");
            stb.append("     T1.A_BORDER_SCORE AS A_BORDER_SCORE2, ");
            stb.append("     T1.B_BORDER_SCORE AS B_BORDER_SCORE2, ");
            stb.append("     T1.SUCCESS_CNT AS SUCCESS_CNT2, ");
            stb.append("     T1.SUCCESS_SEX1_CNT AS SUCCESS_SEX1_CNT2, ");
            stb.append("     T1.SUCCESS_SEX2_CNT AS SUCCESS_SEX2_CNT2, ");
            stb.append("     T1.A_SUCCESS_CNT AS A_SUCCESS_CNT2, ");
            stb.append("     T1.A_SUCCESS_SEX1_CNT AS A_SUCCESS_SEX1_CNT2, ");
            stb.append("     T1.A_SUCCESS_SEX2_CNT AS A_SUCCESS_SEX2_CNT2, ");
            stb.append("     T1.B_SUCCESS_CNT AS B_SUCCESS_CNT2, ");
            stb.append("     T1.B_SUCCESS_SEX1_CNT AS B_SUCCESS_SEX1_CNT2, ");
            stb.append("     T1.B_SUCCESS_SEX2_CNT AS B_SUCCESS_SEX2_CNT2, ");
            stb.append("     VALUE(T1.SUCCESS_CNT,0) + VALUE(T1.A_SUCCESS_CNT,0) + VALUE(T1.B_SUCCESS_CNT,0) AS TOTALCNT2, ");  //NO001
            stb.append("     T1.BACK_RATE AS BACK_RATE2, ");
            stb.append("     T1.CAPA_CNT AS CAPA_CNT2 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PASSINGMARK_MST T1 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.EXAMCOURSECD = T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("     AND T1.TESTDIV = '"+param[1]+"' ");
            stb.append("     AND SHDIV = '2' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SHDIV1, ");
            stb.append("     COURSECD1, ");
            stb.append("     EXAMCOURSE_NAME1, ");
            stb.append("     CAPACITY1, ");
            stb.append("     VALUE(S_SUCCESS_CNT1,0) + VALUE(S_SUCCESS_CNT2,0) AS S_SUCCESS_CNT, ");    //NO001
            stb.append("     JUDGE_COL1, ");
            stb.append("     BORDER_SCORE1, ");
            stb.append("     A_BORDER_SCORE1, ");
            stb.append("     B_BORDER_SCORE1, ");
            stb.append("     SUCCESS_CNT1, ");
            stb.append("     SUCCESS_SEX1_CNT1, ");
            stb.append("     SUCCESS_SEX2_CNT1, ");
            stb.append("     A_SUCCESS_CNT1, ");
            stb.append("     A_SUCCESS_SEX1_CNT1, ");
            stb.append("     A_SUCCESS_SEX2_CNT1, ");
            stb.append("     B_SUCCESS_CNT1, ");
            stb.append("     B_SUCCESS_SEX1_CNT1, ");
            stb.append("     B_SUCCESS_SEX2_CNT1, ");
            stb.append("     TOTALCNT1, ");
            stb.append("     BACK_RATE1, ");
            stb.append("     CAPA_CNT1, ");
            stb.append("     SHDIV2, ");
            stb.append("     COURSECD2, ");
            stb.append("     EXAMCOURSE_NAME2, ");
            stb.append("     JUDGE_COL2, ");
            stb.append("     BORDER_SCORE2, ");
            stb.append("     A_BORDER_SCORE2, ");
            stb.append("     B_BORDER_SCORE2, ");
            stb.append("     SUCCESS_CNT2, ");
            stb.append("     SUCCESS_SEX1_CNT2, ");
            stb.append("     SUCCESS_SEX2_CNT2, ");
            stb.append("     A_SUCCESS_CNT2, ");
            stb.append("     A_SUCCESS_SEX1_CNT2, ");
            stb.append("     A_SUCCESS_SEX2_CNT2, ");
            stb.append("     B_SUCCESS_CNT2, ");
            stb.append("     B_SUCCESS_SEX1_CNT2, ");
            stb.append("     B_SUCCESS_SEX2_CNT2, ");
            stb.append("     TOTALCNT2, ");
            stb.append("     BACK_RATE2, ");
            stb.append("     CAPA_CNT2, ");
            stb.append("     VALUE(CAPA_CNT1,0) + VALUE(CAPA_CNT2,0) - VALUE(CAPACITY1,0) AS EXCESS "); //NO002
            stb.append(" FROM ");
            stb.append("     SH1 ");
            stb.append("     LEFT JOIN SH2 ON COURSECD2 = COURSECD1 ");
            stb.append(" ORDER BY ");
            stb.append("     COURSECD1 DESC ");
log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り

}//クラスの括り
