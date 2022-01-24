// kanji=漢字
/*
 * $Id: f053f8a88694560026e9f32e62d702c1174fd417 $
 *
 * 作成日: 2005/08/05 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３４４＞  1:合格者名簿,4:手続者名簿,7:入学者名簿,8:合格者名簿２
 *
 *  2005/08/05 nakamoto 作成日
 *  2005/08/24 nakamoto 7:入学者名簿を追加
 *  2005/09/02 nakamoto 高校のみページ数を印字
 *  2005/09/04 nakamoto 手続者名簿：コース、専願・併願別出力。入学者名簿：専願・併願別出力。
 *  2005/09/08 nakamoto フォームＩＤ変更
 *
 *  2005/10/28 nakamoto NO001：60行5列 --> 50行4列 に変更
 *                      NO002：フォームＩＤ変更
 *  2005/11/09 nakamoto NO003：バグ修正
 *  2005/11/09 nakamoto NO004：仕様変更
 *  2005/12/23 m-yama   NO005：合計欄出力
 *  2006/01/08 nakamoto NO006：合格者名簿・手続者名簿 備考欄出力「する」「しない」選択可にするよう修正
 *  2006/01/10 nakamoto NO007：フォームＩＤおよびタイトル変更
 *  2006/01/17 nakamoto NO008：(高校)備考欄の合格コース記号をすべて出力---'T'しか出力されない不具合の対応
 *  2006/01/18 nakamoto NO009：(高校)合格者名簿１,手続者名簿,入学者名簿にて、ソートの優先順を指定可にする。指定されたもので改ページする。---指定できるもの（1:男女別,2:専願併願別,3:コース別）
 *  2006/01/19 nakamoto NO010：(高校)コースは、コースコードを降順にソート。つまり、理・国・特・進の順。
 *  2006/02/07 nakamoto NO011：(高校)中高一貫者は除外するよう修正---各種名簿共通条件
 *  2006/10/25 m-yama   NO012：新規帳票追加OUTPUT'10:不合格者名簿'と'11:欠席者名簿'
 *  2006/10/25 m-yama   NO013：(高校)中高一貫者は合格者名簿1/2には含めるよう修正。
 *  2006/11/09 m-yama   NO014：備考１の出力は、中学のみに変更。
 */
public class KNJL344K {


    private static final Log log = LogFactory.getLog(KNJL344K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[16];//NO006 NO009

    //  パラメータの取得
        String classcd[] = request.getParameterValues("L_COURSE");          //1:男女別,2:専／併別,3:コース別 NO009
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 1:前期,2:後期

            param[4] = request.getParameter("OUTPUT");                      //帳票フラグ 1:合格者名簿,4:手続者名簿,7:入学者名簿,8:合格者名簿２
            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[9] = request.getParameter("CHECK"+param[4]);              //備考欄出力 1:する,null:しない NO006

            param[10] = request.getParameter("SORT");                       //選択ソート 1:受験番号順,2:かな氏名順 NO009
            param[11] = request.getParameter("selectdata");                 //選択ソート not null:あり,null:なし NO009
            param[15] = request.getParameter("SPECIAL_REASON_DIV");         //特別理由
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

        getHeaderData(db2,svf,param,classcd);                   //ヘッダーデータ抽出メソッド

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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[15] + "' ");
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
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[],String classcd[]){


    //  フォーム---2005.09.08
        if (param[4].equals("1")) svf.VrSetForm("KNJL344_1.frm", 4);//NO002 KNJL344 --> KNJL344_1
        if (param[4].equals("4")) svf.VrSetForm("KNJL344_1.frm", 4);
        if (param[4].equals("7")) svf.VrSetForm("KNJL344_1.frm", 4);//NO007
        if (param[4].equals("8")) svf.VrSetForm("KNJL344_3.frm", 4);//NO007
        if (param[4].equals("10")) svf.VrSetForm("KNJL344_1.frm", 4);   //NO0012
        if (param[4].equals("11")) svf.VrSetForm("KNJL344_1.frm", 4);   //NO0012

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        if (param[5].equals("1")) param[6] = "中学校";
        if (param[5].equals("2")) param[6] = "高等学校";

    //  タイトル---2005.08.24---NO007
        param[2] = (param[4].equals("8")) ? "合格者名簿２" : 
                   (param[4].equals("1")) ? "合格者名簿１" : 
                   (param[4].equals("4")) ? "手続者名簿" :
                   (param[4].equals("7")) ? "入学者名簿" :
                   (param[4].equals("10")) ? "不合格者名簿" :
                   (param[4].equals("11")) ? "欠席者名簿" : "" ;

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

    //  選択ソート---NO009
        //高校且つ合格者名簿２以外
        if (param[5].equals("2") && !param[4].equals("8")) {
            StringBuffer kaipage  = new StringBuffer();
            StringBuffer subtitle = new StringBuffer();
            StringBuffer orderby  = new StringBuffer();
            //1:男女別・2:専願併願別・3:コース別あり
            if (param[11] != null && !param[11].equals("")) {
                subtitle.append("'（' || ");
                for( int ia=0 ; ia<classcd.length ; ia++ ){
                    if (ia > 0) {
                        kaipage.append(" || ");
                        subtitle.append(" || '・' || ");
                    }
                    if (classcd[ia].equals("1")) {
                        kaipage.append("T1.SEX");
                        subtitle.append("N3.NAME1");
                        orderby.append("T1.SEX, ");
                    }
                    if (classcd[ia].equals("2")) {
                        kaipage.append("T1.SHDIV");
                        subtitle.append("N2.NAME1");
                        orderby.append("T1.SHDIV, ");
                    }
                    if (classcd[ia].equals("3")) {
                        kaipage.append("T1.SUC_COURSE");
                        subtitle.append("T4.EXAMCOURSE_NAME");
                        orderby.append("T1.SUC_COURSE DESC, ");//NO010
                    }
                }
                subtitle.append(" || '）'");
            } else {
                kaipage.append("'1'");
                subtitle.append("''");
            }
            //1:受験番号順,2:かな氏名順
            if (null == param[10] || param[10].equals("1")) {
                orderby.append("T1.EXAMNO ");
            } else {
                //NO013
                orderby.append("T1.EXAMDIV,T1.NAME_KANA ");
            }
            param[12] = kaipage.toString();
            param[13] = subtitle.toString();
            param[14] = orderby.toString();
        }

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
log.debug("Meisai start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int gyo = 0;
            int page_cnt = 1;   //ページ数
            int page_flg = 1;   //ページ数---NO009
            String kaipage = "d";   //改ページフラグ---2005.09.04
            String examdiv = "d";   //NO013

            int show_gyo = 50;  //行 NO001
            int show_ret = 4;   //列 NO001
            int gyo_ret = (show_gyo * show_ret) - 1;   //行×列−1 NO001
            while( rs.next() ){
                //１ページ印刷---2005.09.04
                if (gyo_ret < gyo || 
                    (!kaipage.equals("d") && !kaipage.equals(rs.getString("KAIPEGEFLG"))) ||
                    (!examdiv.equals("d") && !examdiv.equals(rs.getString("EXAMDIV")))) {
                    //合計印刷---2005.09.04---2005.09.08(バグ修正)
                    printTotal(db2,svf,param,page_cnt,page_flg,kaipage);//NO009
                    printScoreReset(svf,param,show_gyo,show_ret);//NO001
                    page_cnt++;
                    page_flg++;
                    gyo = 0;
                    if ( !kaipage.equals("d") && !kaipage.equals(rs.getString("KAIPEGEFLG")) ) page_cnt = 1;//NO009
                }
                //見出し
                printHeader(db2,svf,param,rs);
                //明細データ
                printScore(svf,param,rs,gyo,show_gyo);//NO001

                kaipage = rs.getString("KAIPEGEFLG");
                examdiv = rs.getString("EXAMDIV");
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                printTotal(db2,svf,param,page_cnt,page_flg,kaipage);//NO009
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("TITLEDIV"     , param[2] );//2005.08.24Modify
            svf.VrsOut("DATE"         , param[8] );

            if (param[5].equals("2")) svf.VrsOut("SUBTITLE"   , rs.getString("SUBTITLE") );//2005.09.04
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int gyo,int show_gyo)
    {
        String len = "0";
        String len2 = "0";
        try {
            len = (gyo < show_gyo) ? "1" : 
                  (gyo < show_gyo*2) ? "2" : 
                  (gyo < show_gyo*3) ? "3" : 
                  (gyo < show_gyo*4) ? "4" : "5" ;
            gyo = (gyo < show_gyo) ? gyo+1 : 
                  (gyo < show_gyo*2) ? gyo+1-show_gyo : 
                  (gyo < show_gyo*3) ? gyo+1-show_gyo*2 : 
                  (gyo < show_gyo*4) ? gyo+1-show_gyo*3 : gyo+1-show_gyo*4 ;
            len2 = (10 < (rs.getString("NAME")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("EXAMNO"+len     ,gyo      , rs.getString("EXAMNO") );
            svf.VrsOutn("NAME"+len+len2  ,gyo      , rs.getString("NAME") );
            svf.VrsOutn("SEX"+len        ,gyo      , rs.getString("SEX_NAME") );
            svf.VrsOutn("ATTACH"+len          ,gyo , (param[9] != null && param[5].equals("1")) ? rs.getString("BIKOU1") : "" );
            svf.VrsOutn("EXAMCOURSE_MARK"+len ,gyo , (param[9] != null) ? rs.getString("BIKOU2") : "" );
        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


    /**合計をセット*/
    private void printTotal(DB2UDB db2,Vrw32alp svf,String param[],int page_cnt,int page_flg,String kaipage)
    {
        try {
            PreparedStatement ps1 = db2.prepareStatement(statementTotalCount(param));
            //NO009
            if (param[5].equals("2") && !param[4].equals("8") && param[11] != null && !param[11].equals("")) 
                ps1.setString(1,kaipage);
            //NO009
            ResultSet rs = ps1.executeQuery();
            svf.VrsOut("PAGE" ,   String.valueOf(page_cnt) );//ページ数および改ページ用---2005.09.02
            //NO009---KNJL344_1のみ
            if (param[5].equals("2") && !param[4].equals("8") && param[11] != null && !param[11].equals("")) 
                svf.VrsOut("CHANGE_PAGE" ,   String.valueOf(page_flg) );//改ページ用
            else 
                svf.VrsOut("CHANGE_PAGE" ,   String.valueOf(page_cnt) );//改ページ用
            //NO009

            while( rs.next() ){
                svf.VrsOut("EXAMCOURSE"   ,   rs.getString("EXAMCOURSE_NAME") );
                svf.VrsOut("MEMBER1"      ,   rs.getString("SEX1") );
                svf.VrsOut("MEMBER2"      ,   rs.getString("SEX2") );
                svf.VrsOut("TOTAL_MEMBER" ,   rs.getString("KEI") );

                svf.VrEndRecord();
            }
            rs.close();
            ps1.close();
        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }//printTotal()の括り


    /**明細データをクリア*/
    private void printScoreReset(Vrw32alp svf,String param[],int show_gyo,int show_ret)
    {
        try {
            for (int len = 1; len < (show_ret + 1); len++) {
                for (int gyo = 1; gyo < (show_gyo + 1); gyo++) {
                    svf.VrsOutn("EXAMNO"+String.valueOf(len)          ,gyo      , "" );
                    svf.VrsOutn("NAME"+String.valueOf(len)+"_1"       ,gyo      , "" );
                    svf.VrsOutn("NAME"+String.valueOf(len)+"_2"       ,gyo      , "" );
                    svf.VrsOutn("SEX"+String.valueOf(len)             ,gyo      , "" );
                    svf.VrsOutn("ATTACH"+String.valueOf(len)          ,gyo      , "" );
                    svf.VrsOutn("EXAMCOURSE_MARK"+String.valueOf(len) ,gyo      , "" );
                }
            }

        } catch( Exception ex ) {
            log.warn("printScoreReset read error!",ex);
        }

    }//printScoreReset()の括り


    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            //NO012
            stb.append("WITH EXAM_BASE AS ( ");
            if (param[4].equals("10") || param[4].equals("11")) {
                stb.append("    SELECT T1.TESTDIV,T1.EXAMNO,T1.NAME,T1.SEX,T1.JUDGEMENT,T1.NATPUBPRIDIV,T1.SHDIV,T1.NAME_KANA, ");//NO009
                stb.append("           '0' AS EXAMDIV, ");
                stb.append("           L1.COURSECD||L1.MAJORCD||L1.EXAMCOURSECD AS SUC_COURSE ");
                stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT T1 ");
                stb.append("           LEFT JOIN (SELECT ");
                stb.append("                          T1.* ");
                stb.append("                      FROM ");
                stb.append("                          ENTEXAM_WISHDIV_MST T1, ");
                stb.append("                          (SELECT ");
                stb.append("                               ENTEXAMYEAR, ");
                stb.append("                               TESTDIV, ");
                stb.append("                               DESIREDIV, ");
                stb.append("                               MAX(WISHNO) AS WISHNO ");
                stb.append("                           FROM ");
                stb.append("                               ENTEXAM_WISHDIV_MST ");
                stb.append("                           WHERE ");
                stb.append("                               ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("                               AND TESTDIV = '"+param[1]+"' ");
                stb.append("                           GROUP BY ");
                stb.append("                               ENTEXAMYEAR, ");
                stb.append("                               TESTDIV, ");
                stb.append("                               DESIREDIV) T2 ");
                stb.append("                      WHERE ");
                stb.append("                          T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
                stb.append("                          AND T1.TESTDIV = T2.TESTDIV ");
                stb.append("                          AND T1.DESIREDIV = T2.DESIREDIV ");
                stb.append("                          AND T1.WISHNO = T2.WISHNO) L1 ");
                stb.append("                ON T1.DESIREDIV = L1.DESIREDIV ");
                stb.append("    WHERE  T1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[15])) {
                    stb.append("           AND T1.SPECIAL_REASON_DIV = '" + param[15] + "' ");
                }
                stb.append("           AND T1.TESTDIV = '"+param[1]+"' ");
                if (param[4].equals("10")) {
                    stb.append("       AND T1.JUDGEMENT = '7' "); //不合格者 NO012
                }
                if (param[4].equals("11")) {
                    stb.append("       AND T1.JUDGEMENT = '8' "); //欠席者 NO012
                }
                stb.append("           AND T1.APPLICANTDIV NOT IN('2') ");//NO011
            } else {
                stb.append("    SELECT TESTDIV,EXAMNO,NAME,SEX,JUDGEMENT,NATPUBPRIDIV,SHDIV,NAME_KANA, ");//NO009
                //NO013
                if (param[5].equals("2") && (param[4].equals("1") || param[4].equals("8"))) {
                    stb.append("           CASE WHEN EXAMNO BETWEEN '5000' AND '5999' THEN '1' ELSE '0' END AS EXAMDIV, ");
                } else {
                    stb.append("           '0' AS EXAMDIV, ");
                }
                stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
                stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
                stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[15])) {
                    stb.append("           AND SPECIAL_REASON_DIV = '" + param[15] + "' ");
                }
                stb.append("           AND TESTDIV = '"+param[1]+"' ");
//              NO004-----↓-----
                stb.append("       AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");//合格者
                if (param[4].equals("4")) {
                    stb.append("       AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");//手続者
                }
                if (param[4].equals("7")) {
                    stb.append("       AND PROCEDUREDIV = '2' AND ENTDIV = '2' ");//入学者---2005.08.24
                }
//              NO004-----↑-----
                //NO013
                if (!param[4].equals("1") && !param[4].equals("8")) {
                    stb.append("           AND APPLICANTDIV NOT IN('2') ");//NO011
                }
            }
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       T1.EXAMNO,T1.NAME, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T1.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ");
            stb.append("       T1.NATPUBPRIDIV,T1.EXAMDIV, ");   //NO013
            stb.append("       CASE WHEN T1.NATPUBPRIDIV = '9' THEN 'F' ELSE NULL END AS BIKOU1, ");
            //中学
            if (param[5].equals("1") && !param[4].equals("10")) {
                stb.append("   CASE WHEN T4.EXAMCOURSE_MARK = 'I' OR T4.EXAMCOURSE_MARK = 'T' ");
                stb.append("        THEN T4.EXAMCOURSE_MARK ELSE NULL END AS BIKOU2 ");
            } else {
            //高校---NO008 不合格者---NO012
                stb.append("   T4.EXAMCOURSE_MARK AS BIKOU2 ");
            }
            //2005.09.04
            if (param[5].equals("1")) {
                stb.append("   ,'1' AS KAIPEGEFLG ");
            } else {
                //NO009
                if (param[4].equals("8")) {
                    stb.append("   ,'1' AS KAIPEGEFLG ");
                    stb.append("   ,'' AS SUBTITLE ");
                } else {
                    stb.append("   ,"+param[12]+" AS KAIPEGEFLG ");
                    stb.append("   ,"+param[13]+" AS SUBTITLE ");
                }
            }
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD=T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L006' AND N2.NAMECD2=T1.SHDIV ");//2005.09.04
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='Z002' AND N3.NAMECD2=T1.SEX ");//NO009
            //2005.09.04
            if (param[5].equals("1")) {
                stb.append("ORDER BY T1.TESTDIV,T1.EXAMNO ");
            } else {
                //NO009
                if (param[4].equals("8")) {
                    stb.append("ORDER BY T1.TESTDIV,T1.EXAMNO ");
                } else {
                    stb.append("ORDER BY T1.TESTDIV,"+param[14]+" ");
                }
            }
//log.debug(stb.toString());
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  コース別男女計を取得
     *
     */
    private String statementTotalCount(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            //NO012
            if (param[4].equals("10") || param[4].equals("11")) {
                stb.append("    SELECT T1.TESTDIV,T1.EXAMNO,T1.NAME,T1.SEX,T1.JUDGEMENT,T1.NATPUBPRIDIV,T1.SHDIV, ");//NO009
                stb.append("           '0' AS EXAMDIV, ");
                stb.append("           L1.COURSECD||L1.MAJORCD||L1.EXAMCOURSECD AS SUC_COURSE ");
                stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT T1 ");
                stb.append("           LEFT JOIN (SELECT ");
                stb.append("                          T1.* ");
                stb.append("                      FROM ");
                stb.append("                          ENTEXAM_WISHDIV_MST T1, ");
                stb.append("                          (SELECT ");
                stb.append("                               ENTEXAMYEAR, ");
                stb.append("                               TESTDIV, ");
                stb.append("                               DESIREDIV, ");
                stb.append("                               MAX(WISHNO) AS WISHNO ");
                stb.append("                           FROM ");
                stb.append("                               ENTEXAM_WISHDIV_MST ");
                stb.append("                           WHERE ");
                stb.append("                               ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("                               AND TESTDIV = '"+param[1]+"' ");
                stb.append("                           GROUP BY ");
                stb.append("                               ENTEXAMYEAR, ");
                stb.append("                               TESTDIV, ");
                stb.append("                               DESIREDIV) T2 ");
                stb.append("                      WHERE ");
                stb.append("                          T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
                stb.append("                          AND T1.TESTDIV = T2.TESTDIV ");
                stb.append("                          AND T1.DESIREDIV = T2.DESIREDIV ");
                stb.append("                          AND T1.WISHNO = T2.WISHNO) L1 ");
                stb.append("                ON T1.DESIREDIV = L1.DESIREDIV ");
                stb.append("    WHERE  T1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[15])) {
                    stb.append("           AND T1.SPECIAL_REASON_DIV = '" + param[15] + "' ");
                }
                stb.append("           AND T1.TESTDIV = '"+param[1]+"' ");
                if (param[4].equals("10")) {
                    stb.append("       AND T1.JUDGEMENT = '7' "); //不合格者 NO012
                }
                if (param[4].equals("11")) {
                    stb.append("       AND T1.JUDGEMENT = '8' "); //欠席者 NO012
                }
                stb.append("           AND T1.APPLICANTDIV NOT IN('2') ");//NO011
            } else {
                stb.append("    SELECT TESTDIV,EXAMNO,NAME,SEX,JUDGEMENT,NATPUBPRIDIV,SHDIV, ");//NO009
                //NO013
                if (param[5].equals("2") && (param[4].equals("1") || param[4].equals("8"))) {
                    stb.append("           CASE WHEN EXAMNO BETWEEN '5000' AND '5999' THEN '1' ELSE '0' END AS EXAMDIV, ");
                } else {
                    stb.append("           '0' AS EXAMDIV, ");
                }
                stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
                stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
                stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[15])) {
                    stb.append("           AND SPECIAL_REASON_DIV = '" + param[15] + "' ");
                }
                stb.append("           AND TESTDIV = '"+param[1]+"' ");
    //NO004-----↓-----
                stb.append("       AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");//合格者
                if (param[4].equals("4")) {
                    stb.append("       AND PROCEDUREDIV = '2' AND ENTDIV <= '2' ");//手続者
                }
                if (param[4].equals("7")) {
                    stb.append("       AND PROCEDUREDIV = '2' AND ENTDIV = '2' ");//入学者---2005.08.24
                }
    //NO004-----↑-----
                //NO013
                if (!param[4].equals("1") && !param[4].equals("8")) {
                    stb.append("           AND APPLICANTDIV NOT IN('2') ");//NO011
                }
            }
            stb.append("    ) ");
            //NO012
            stb.append(",EXAM_COURSE2 (COURSE, EXAMCOURSE_NAME, EXAMCOURSE_MARK) AS ( ");
            stb.append("    VALUES('00000001', '中高一貫コース', 'J') ");
            stb.append("    ) ");
            //受験コースマスタ
            stb.append(",EXAM_COURSE AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("           EXAMCOURSE_NAME,EXAMCOURSE_MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            //NO011
            if (param[5].equals("2")) {
                stb.append("   AND EXAMCOURSE_MARK IN('S','K','T','P') ");
            }
            if (param[5].equals("2") && (param[4].equals("1") || param[4].equals("8"))) {
                stb.append("    UNION ");
                stb.append("    SELECT * FROM EXAM_COURSE2 ");
            }
            stb.append("    ) ");

            //コース別男女計
            stb.append(",GOUKEI AS ( ");
            stb.append("    SELECT CASE WHEN EXAMDIV = '0' THEN SUC_COURSE ELSE '00000001' END AS SUC_COURSE, ");
            stb.append("           EXAMDIV, ");
            stb.append("           SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SEX1, ");
            stb.append("           SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SEX2, ");
            stb.append("           SUM(CASE WHEN SEX = '1' OR SEX = '2' THEN 1 ELSE 0 END) AS KEI ");
            stb.append("    FROM   EXAM_BASE T1 ");//NO009
            stb.append("    WHERE  SUC_COURSE IS NOT NULL ");//2005.09.08
            //NO009
            if (param[5].equals("2") && !param[4].equals("8") && param[11] != null && !param[11].equals("")) {
                stb.append("       AND "+param[12]+" = ? ");
            }
            //NO009
            stb.append("    GROUP BY SUC_COURSE,EXAMDIV ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.COURSE,T1.EXAMCOURSE_NAME, ");
            stb.append("       T2.SEX1,T2.SEX2,T2.KEI ");
            stb.append("FROM   EXAM_COURSE T1 ");
            stb.append("       LEFT JOIN GOUKEI T2 ON T2.SUC_COURSE=T1.COURSE ");
            //中学
            if (param[5].equals("1")) {
                stb.append("UNION ");
                stb.append("SELECT '99999999' AS COURSE,'合計' AS EXAMCOURSE_NAME, ");
                stb.append("       SUM(T2.SEX1) AS SEX1,SUM(T2.SEX2) AS SEX2,SUM(T2.KEI) AS KEI ");
                stb.append("FROM   GOUKEI T2 ");
                stb.append("ORDER BY COURSE ");
            //高校---NO010
            } else {
                stb.append("UNION ");
                stb.append("SELECT '00000000' AS COURSE,'合計' AS EXAMCOURSE_NAME, ");
                stb.append("       SUM(T2.SEX1) AS SEX1,SUM(T2.SEX2) AS SEX2,SUM(T2.KEI) AS KEI ");
                stb.append("FROM   GOUKEI T2 ");
                stb.append("ORDER BY COURSE DESC ");
            }
        } catch( Exception e ){
            log.warn("statementTotalCount error!",e);
        }
//log.debug(stb.toString());
        return stb.toString();

    }//statementTotalCount()の括り



}//クラスの括り
