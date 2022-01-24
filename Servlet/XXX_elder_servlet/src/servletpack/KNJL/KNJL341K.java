// kanji=漢字
/*
 * $Id: 2a4d47bfbc0e316df2d5d9e335d2526092823f40 $
 *
 * 作成日: 2005/08/08 11:25:40 - JST
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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３４１＞  入試合否判定原簿集計表
 *
 *  2005/08/08 nakamoto 作成日
 *
 *  2005/11/08 nakamoto NO001 出力対象：「一般受験者」と「附属出身者」に分けて印刷
 *  2006/01/08 nakamoto NO002 タイトルの下のサブタイトルを追加。一般は「（一般）」、附属は「（附属）」と表記
 *  2007/06/29 m-yama   NO003 フォーム変更に伴う修正
 *  2007/07/03 m-yama   NO004 フォーム変更に伴う修正
 */
public class KNJL341K {


    private static final Log log = LogFactory.getLog(KNJL341K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[10];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 1:前期,2:後期
            param[2] = request.getParameter("OUTPUT2");                     //出力対象 1:一般受験者,2:附属出身者---NO001

            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[9] = request.getParameter("SPECIAL_REASON_DIV");          //特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[9] + "' ");
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

        svf.VrSetForm("KNJL341.frm", 4);

    //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");

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
            String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            String arr_ctrl_date[] = new String[3];
            int number = 0;
            while( rs.next() ){
                arr_ctrl_date[number] = rs.getString(1);
                number++;
            }
            db2.commit();
            param[8] = KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分"+"　現在";
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    //  サブタイトル---NO002
        if (param[2].equals("1")) //一般受験者
            svf.VrsOut("SUBTITLE"         , "（一般）" );
        if (param[2].equals("2")) //附属出身者
            svf.VrsOut("SUBTITLE"         , "（附属）" );

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //明細データ
            if( printMeisai(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();

            int kei_s[][] = {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}};//小計用
            int kei_t[][] = {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}};//合計用
            int kei_c[][] = {{0,0,0},{0,0,0},{0,0,0}};//合格者のコース別小計用
            int kei_ct[][] = {{0,0,0},{0,0,0},{0,0,0}};//合格者のコース別合計用
            String kei_cname[] = new String[3];//コース名小計用
            String kei_ctname[] = new String[3];//コース名合計用
            String sort_flg = "0";//

            int lineCnt = 1;    //NO004

            while( rs.next() ){
                //小計のセット
                if( (rs.getString("SORT")).equals("4") || (rs.getString("SORT")).equals("6") ) {
                    printTotal(svf,param,kei_s,1);//合格者数以外
                    for (int ic = 0; ic < 3; ic++) {
                        if (kei_cname[ic] == null) continue;
                        printTotal2(svf,param,kei_c,kei_cname,ic,1, lineCnt);//合格者数
                        svf.VrEndRecord();
                        lineCnt++;
                        //クリアおよび累計（合格者数）
                        kei_ctname[ic] = kei_cname[ic];
                        kei_cname[ic] = null;
                        for (int ib = 0; ib < 3; ib++) {
                            kei_ct[ic][ib] = kei_ct[ic][ib] + kei_c[ic][ib];
                            kei_c[ic][ib] = 0;
                        }
                    }
                    //クリアおよび累計（合格者数以外）
                    for (int ia = 0; ia < 5; ia++) {
                        for (int ib = 0; ib < 3; ib++) {
                            kei_t[ia][ib] = kei_t[ia][ib] + kei_s[ia][ib];
                            kei_s[ia][ib] = 0;
                        }
                    }
                }
                //見出し
                printHeader(db2, svf,param,rs);
                //明細データ
                printScore(svf,param,rs,kei_s,kei_c,kei_cname,sort_flg, lineCnt);
                //小計の計算（合格者数以外）
                if( !sort_flg.equals(rs.getString("SORT")) ) setTotal(svf,param,rs,kei_s);

                sort_flg = rs.getString("SORT");
                svf.VrEndRecord();
                lineCnt++;
                nonedata = true;
            }
            //合計
            if (nonedata) {
                //累計（合格者数以外）
                for (int ia = 0; ia < 5; ia++) 
                    for (int ib = 0; ib < 3; ib++) 
                        kei_t[ia][ib] = kei_t[ia][ib] + kei_s[ia][ib];
                //累計（合格者数）
                for (int ic = 0; ic < 3; ic++) 
                    for (int ib = 0; ib < 3; ib++) 
                        kei_ct[ic][ib] = kei_ct[ic][ib] + kei_c[ic][ib];
                printTotal(svf,param,kei_t,2);//合格者数以外
                for (int ic = 0; ic < 3; ic++) {
                    if (kei_ctname[ic] == null) continue;
                    printTotal2(svf,param,kei_ct,kei_ctname,ic,1, lineCnt);//合格者数
                    svf.VrEndRecord();
                    lineCnt++;
                }
                printTotal3(svf,param,kei_t);//合格者数の総合計
                svf.VrEndRecord();
                lineCnt++;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("DATE"         , param[8] );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int kei_s[][],int kei_c[][],String kei_cname[],String sort_flg, int lineCnt)
    {
        try {
            if ( !sort_flg.equals(rs.getString("SORT")) ) {
                svf.VrsOut("DESIREDIV", rs.getString("ABBV") );//志望区分
            }
            svf.VrsOut("EXAMCOURSE", rs.getString("EXAMCOURSE_NAME") );//コース

            if (lineCnt == 9 || lineCnt == 14 || lineCnt == 15 || lineCnt == 18) {
                svf.VrsOut("FLG1", "2");    //NO004
                svf.VrsOut("FLG2", "2");    //NO004
            } else if (lineCnt == 1 || lineCnt == 3 || lineCnt == 6 || lineCnt == 10 || lineCnt == 12) {
                svf.VrsOut("FLG1", "1");    //NO004
                svf.VrsOut("FLG2", "1");    //NO004
            } else {
                svf.VrsOut("FLG1", "");    //NO004
            }

            for (int ia = 1; ia < 4; ia++) {
                if( !sort_flg.equals(rs.getString("SORT")) ){
                    svf.VrsOut("APPLICANT"+String.valueOf(ia) , rs.getString("A_KEI"+String.valueOf(ia)) );//志願者数
                    svf.VrsOut("EXAMINEE"+String.valueOf(ia)  , rs.getString("B_KEI"+String.valueOf(ia)) );//受験者数
                    svf.VrsOut("ABSENTEE"+String.valueOf(ia)  , rs.getString("C_KEI"+String.valueOf(ia)) );//欠席者数
                    svf.VrsOut("FAIL"+String.valueOf(ia)      , rs.getString("D_KEI"+String.valueOf(ia)) );//不合格者数
                }
                svf.VrsOut("SUCCESS"+String.valueOf(ia)   , rs.getString("E_KEI"+String.valueOf(ia)) );//合格者数
                //小計の計算（合格者数）
                kei_s[4][ia-1] = kei_s[4][ia-1] + rs.getInt("E_KEI"+String.valueOf(ia));//総合計用
                kei_c[rs.getInt("MARK")][ia-1] = kei_c[rs.getInt("MARK")][ia-1] + rs.getInt("E_KEI"+String.valueOf(ia));
                kei_cname[rs.getInt("MARK")] = rs.getString("EXAMCOURSE_NAME");
            }
            for (int ib = 1; ib < 14; ib++) {
                svf.VrsOut("MASK"+String.valueOf(ib) , rs.getString("SORT") );
            }
        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


    /**小計の計算*/
    private void setTotal(Vrw32alp svf,String param[],ResultSet rs,int kei_s[][])
    {
        try {
            for (int ia = 1; ia < 4; ia++) {
                kei_s[0][ia-1] = kei_s[0][ia-1] + rs.getInt("A_KEI"+String.valueOf(ia));
                kei_s[1][ia-1] = kei_s[1][ia-1] + rs.getInt("B_KEI"+String.valueOf(ia));
                kei_s[2][ia-1] = kei_s[2][ia-1] + rs.getInt("C_KEI"+String.valueOf(ia));
                kei_s[3][ia-1] = kei_s[3][ia-1] + rs.getInt("D_KEI"+String.valueOf(ia));
            }
        } catch( Exception ex ) {
            log.warn("setTotal read error!",ex);
        }

    }//setTotal()の括り


    /**小計・合計をセット（合格者数以外）*/
    private void printTotal(Vrw32alp svf,String param[],int kei_s[][],int flg)
    {
        try {
            svf.VrsOut("DESIREDIV"   , (flg == 1) ? "小計" : "合計" );
            for (int ia = 1; ia < 4; ia++) {
                svf.VrsOut("APPLICANT"+String.valueOf(ia) , String.valueOf(kei_s[0][ia-1]) );
                svf.VrsOut("EXAMINEE"+String.valueOf(ia)  , String.valueOf(kei_s[1][ia-1]) );
                svf.VrsOut("ABSENTEE"+String.valueOf(ia)  , String.valueOf(kei_s[2][ia-1]) );
                svf.VrsOut("FAIL"+String.valueOf(ia)      , String.valueOf(kei_s[3][ia-1]) );
            }
        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }//printTotal()の括り


    /**小計・合計をセット（合格者数）*/
    private void printTotal2(Vrw32alp svf,String param[],int kei_c[][],String kei_cname[],int ic,int flg, int lineCnt)
    {
        try {
            svf.VrsOut("EXAMCOURSE"  , (flg == 1) ? kei_cname[ic] : "合計" );
            if (lineCnt == 9 || lineCnt == 14 || lineCnt == 15 || lineCnt == 18) {
                svf.VrsOut("FLG1", "2");    //NO004
                svf.VrsOut("FLG2", "2");    //NO004
            } else if (lineCnt == 1 || lineCnt == 3 || lineCnt == 6 || lineCnt == 10 || lineCnt == 12) {
                svf.VrsOut("FLG1", "1");    //NO004
                svf.VrsOut("FLG2", "1");    //NO004
            } else {
                svf.VrsOut("FLG1", "");    //NO004
                svf.VrsOut("FLG2", "1");    //NO004
            }
            for (int ia = 1; ia < 4; ia++) {
                svf.VrsOut("SUCCESS"+String.valueOf(ia)   , String.valueOf(kei_c[ic][ia-1]) );
            }
            for (int ib = 1; ib < 14; ib++) 
                svf.VrsOut("MASK"+String.valueOf(ib) , "0" );
        } catch( Exception ex ) {
            log.warn("printTotal2 read error!",ex);
        }

    }//printTotal2()の括り


    /**合格者数の総合計をセット*/
    private void printTotal3(Vrw32alp svf,String param[],int kei_t[][])
    {
        try {
            svf.VrsOut("TOTAL_EXAMCOURSE"  , "合計" );
            svf.VrsOut("FLG3", "2");    //NO004
            for (int ia = 1; ia < 4; ia++) 
                svf.VrsOut("TOTAL_SUCCESS"+String.valueOf(ia)   , String.valueOf(kei_t[4][ia-1]) );
        } catch( Exception ex ) {
            log.warn("printTotal3 read error!",ex);
        }

    }//printTotal3()の括り


    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,SEX,DESIREDIV,JUDGEMENT, ");
            stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            //NO001
            if (param[2].equals("1")) //一般受験者
                stb.append("       AND (EXAMNO < '3000' OR '4000' <= EXAMNO) ");
            if (param[2].equals("2")) //附属出身者
                stb.append("       AND '3000' <= EXAMNO AND EXAMNO < '4000' ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ1
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.TESTDIV,W1.DESIREDIV,W1.WISHNO, ");
            stb.append("           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ");
            stb.append("           EXAMCOURSE_NAME,EXAMCOURSE_ABBV,EXAMCOURSE_MARK ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ2
            stb.append(",EXAM_WISH2 AS ( ");
            stb.append("    SELECT TESTDIV,DESIREDIV, ");
            stb.append("           MAX(CASE WHEN WISHNO = '1' THEN EXAMCOURSE_ABBV ELSE NULL END) AS ABBV1, ");
            stb.append("           MAX(CASE WHEN WISHNO = '2' THEN EXAMCOURSE_ABBV ELSE NULL END) AS ABBV2, ");
            stb.append("           MAX(CASE WHEN WISHNO = '3' THEN EXAMCOURSE_ABBV ELSE NULL END) AS ABBV3, ");
            stb.append("           MAX(CASE WHEN WISHNO = '1' THEN EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ");
            stb.append("           MAX(CASE WHEN WISHNO = '2' THEN EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ");
            stb.append("           MAX(CASE WHEN WISHNO = '3' THEN EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ");
            stb.append("    FROM   EXAM_WISH ");
            stb.append("    GROUP BY TESTDIV,DESIREDIV ");
            stb.append("    ) ");
            //志望者・受験者・欠席者・不合格者
            stb.append(",EACH_TOTAL AS ( ");
            stb.append("    SELECT TESTDIV,DESIREDIV, ");
            stb.append("           SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SEX1_A, ");
            stb.append("           SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SEX2_A, ");
            stb.append("           SUM(CASE WHEN SEX = '1' OR SEX = '2' THEN 1 ELSE 0 END) AS KEI_A, ");
            stb.append("           SUM(CASE WHEN SEX = '1' AND VALUE(INT(JUDGEMENT),88) <> 8 THEN 1 ELSE 0 END) AS SEX1_B, ");
            stb.append("           SUM(CASE WHEN SEX = '2' AND VALUE(INT(JUDGEMENT),88) <> 8 THEN 1 ELSE 0 END) AS SEX2_B, ");
            stb.append("           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND VALUE(INT(JUDGEMENT),88) <> 8 THEN 1 ELSE 0 END) AS KEI_B, ");
            stb.append("           SUM(CASE WHEN SEX = '1' AND INT(JUDGEMENT) = 8 THEN 1 ELSE 0 END) AS SEX1_C, ");
            stb.append("           SUM(CASE WHEN SEX = '2' AND INT(JUDGEMENT) = 8 THEN 1 ELSE 0 END) AS SEX2_C, ");
            stb.append("           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND INT(JUDGEMENT) = 8 THEN 1 ELSE 0 END) AS KEI_C, ");
            stb.append("           SUM(CASE WHEN SEX = '1' AND INT(JUDGEMENT) = 7 THEN 1 ELSE 0 END) AS SEX1_D, ");
            stb.append("           SUM(CASE WHEN SEX = '2' AND INT(JUDGEMENT) = 7 THEN 1 ELSE 0 END) AS SEX2_D, ");
            stb.append("           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND INT(JUDGEMENT) = 7 THEN 1 ELSE 0 END) AS KEI_D ");
            stb.append("    FROM   EXAM_BASE ");
            stb.append("    GROUP BY TESTDIV,DESIREDIV ");
            stb.append("    ) ");
            //合格者
            stb.append(",EACH_TOTAL2 AS ( ");
            stb.append("    SELECT TESTDIV,DESIREDIV,SUC_COURSE, ");
            stb.append("           SUM(CASE WHEN SEX = '1' AND (INT(JUDGEMENT) > 0 AND INT(JUDGEMENT) <= 6) OR INT(JUDGEMENT) = 9 THEN 1 ELSE 0 END) AS SEX1_E, ");
            stb.append("           SUM(CASE WHEN SEX = '2' AND (INT(JUDGEMENT) > 0 AND INT(JUDGEMENT) <= 6) OR INT(JUDGEMENT) = 9 THEN 1 ELSE 0 END) AS SEX2_E, ");
            stb.append("           SUM(CASE WHEN (SEX = '1' OR SEX = '2') AND (INT(JUDGEMENT) > 0 AND INT(JUDGEMENT) <= 6) OR INT(JUDGEMENT) = 9 THEN 1 ELSE 0 END) AS KEI_E ");
            stb.append("    FROM   EXAM_BASE ");
            stb.append("    GROUP BY TESTDIV,DESIREDIV,SUC_COURSE ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME,T1.DESIREDIV, ");
            stb.append("       CASE WHEN MARK1 = 'I' AND MARK2 = 'T' AND MARK3 = 'H' THEN '3' ");
            stb.append("            WHEN MARK1 = 'I' AND MARK2 = 'T' THEN '2' ");
            stb.append("            WHEN MARK1 = 'T' AND MARK2 = 'H' THEN '5' ");
            stb.append("            WHEN MARK1 = 'I' THEN '1' ");
            stb.append("            WHEN MARK1 = 'T' THEN '4' ");
            stb.append("            WHEN MARK1 = 'H' THEN '6' ");
            stb.append("            ELSE '99' END AS SORT, ");
            stb.append("       VALUE(W1.ABBV1,'')||VALUE(W1.ABBV2,'')||VALUE(W1.ABBV3,'') AS ABBV, ");
            stb.append("       VALUE(SEX1_A,0) AS A_KEI1,VALUE(SEX2_A,0) AS A_KEI2,VALUE(KEI_A,0) AS A_KEI3, ");
            stb.append("       VALUE(SEX1_B,0) AS B_KEI1,VALUE(SEX2_B,0) AS B_KEI2,VALUE(KEI_B,0) AS B_KEI3, ");
            stb.append("       VALUE(SEX1_C,0) AS C_KEI1,VALUE(SEX2_C,0) AS C_KEI2,VALUE(KEI_C,0) AS C_KEI3, ");
            stb.append("       VALUE(SEX1_D,0) AS D_KEI1,VALUE(SEX2_D,0) AS D_KEI2,VALUE(KEI_D,0) AS D_KEI3, ");
            stb.append("       EXAMCOURSE_NAME,EXAMCOURSE_MARK, ");
            stb.append("       CASE WHEN EXAMCOURSE_MARK = 'I' THEN '0' ");
            stb.append("            WHEN EXAMCOURSE_MARK = 'T' THEN '1' ");
            stb.append("            WHEN EXAMCOURSE_MARK = 'H' THEN '2' ");
            stb.append("            ELSE '0' END AS MARK, ");
            stb.append("       VALUE(SEX1_E,0) AS E_KEI1,VALUE(SEX2_E,0) AS E_KEI2,VALUE(KEI_E,0) AS E_KEI3 ");
            stb.append("FROM   EXAM_WISH T1 ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("       LEFT JOIN EXAM_WISH2 W1 ON W1.TESTDIV=T1.TESTDIV AND W1.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN EACH_TOTAL T2 ON T2.TESTDIV=T1.TESTDIV AND T2.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN EACH_TOTAL2 T3 ON T3.TESTDIV=T1.TESTDIV AND T3.DESIREDIV=T1.DESIREDIV AND T3.SUC_COURSE=T1.COURSE ");
            stb.append("ORDER BY T1.TESTDIV,SORT ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り



}//クラスの括り
