// kanji=漢字
/*
 * $Id: 8f3cca95fb3df98cd1033d9ae351bfb5528858fc $
 *
 * 作成日: 2005/07/27 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３２２Ｋ＞  入学試験得点入力データチェックリスト
 *
 *  2005/07/27 nakamoto 作成日
 *  2005/08/15 m-yama   会場毎改ページチェックボックス追加による修正
 *  2005/08/23 nakamoto 「100」「150」欄は、高校の数学と英語のみで必要
 *  2005/08/28 nakamoto ３列から６列へ変更
 *  2005/08/30 nakamoto 「150」欄は、数学は理数科、英語は国際科以外は表記しない
 * @author nakamoto
 * @version $Id: 8f3cca95fb3df98cd1033d9ae351bfb5528858fc $
 */
public class KNJL322K {


    private static final Log log = LogFactory.getLog(KNJL322K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[12];        //m-yama 050815

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 99:全て
            param[2] = request.getParameter("SUBCLASS");                    //試験科目 99:全て
            //会場または受験番号リスト
            String classcd[] = request.getParameterValues("DATA_SELECTED");
            param[3] = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) param[3] = param[3] + ",";
                param[3] = param[3] + "'" + classcd[ia] + "'";
            }
            param[3] = param[3] + ")";

            param[4] = request.getParameter("OUTPUT");                      //1:会場,2:受験番号
            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[9] = request.getParameter("PCHANGE");                     //改ページフラグ m-yama 050815
            param[10]= String.valueOf(classcd.length);                                      //会場数 m-yama 050815
            param[11] = request.getParameter("SPECIAL_REASON_DIV");         //特別理由
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
        if (param[9] == null){  //m-yama 050815
            if( printMain(db2,svf,param) ) nonedata = true;
        }else {
            if( printMain2(db2,svf,param) ) nonedata = true;
        }

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


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){


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

    }//getHeaderData()の括り


    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[11] + "' ");
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

    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            int total_page[] = new int[10];//2*5
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /*印刷処理メイン2 m-yama 050815*/
    private boolean printMain2(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            int total_page[] = new int[10*Integer.parseInt(param[10])];//2*5*会場数
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai2(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain2 read error!",ex);
        }

        return nonedata;

    }//printMain2()の括り


    /**試験区分・試験科目毎の総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
    {
        try {
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();

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
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            int page_arr = 0;   //総ページ数配列No
            String testdiv = "d";
            String subclassno = "d";
            while( rs.next() ){
                //１ページ印刷
                if (299 < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                    (!subclassno.equals("d") && !subclassno.equals(rs.getString("TESTSUBCLASSCD"))) ) {
                    //合計印刷
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                        (!subclassno.equals("d") && !subclassno.equals(rs.getString("TESTSUBCLASSCD"))) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                        (!subclassno.equals("d") && !subclassno.equals(rs.getString("TESTSUBCLASSCD"))) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,total_page,page_arr,gyo);
                //受験番号・氏名
                printScore(svf,param,rs,gyo);     //受験番号・得点・氏名出力のメソッド
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = rs.getString("TESTDIV");
                subclassno = rs.getString("TESTSUBCLASSCD");
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


    /**明細データ印刷処理*/
    private boolean printMeisai2(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
    {
        boolean nonedata = false;
        try {
            db2.query(statementMeisai2(param));
            ResultSet rs = db2.getResultSet();

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            int page_arr = 0;   //総ページ数配列No
            String testdiv = "d";
            String subclassno = "d";
            String hollno = "d";
            while( rs.next() ){
                //１ページ印刷
                if (299 < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                    (!hollno.equals("d") && !hollno.equals(rs.getString("EXAMHALLNO"))) || 
                    (!subclassno.equals("d") && !subclassno.equals(rs.getString("TESTSUBCLASSCD"))) ) {
                    //合計印刷
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                        (!hollno.equals("d") && !hollno.equals(rs.getString("EXAMHALLNO"))) || 
                        (!subclassno.equals("d") && !subclassno.equals(rs.getString("TESTSUBCLASSCD"))) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                        (!hollno.equals("d") && !hollno.equals(rs.getString("EXAMHALLNO"))) || 
                        (!subclassno.equals("d") && !subclassno.equals(rs.getString("TESTSUBCLASSCD"))) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,total_page,page_arr,gyo);
                //受験番号・氏名
                printScore(svf,param,rs,gyo);     //受験番号・得点・氏名出力のメソッド
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv    = rs.getString("TESTDIV");
                hollno     = rs.getString("EXAMHALLNO");
                subclassno = rs.getString("TESTSUBCLASSCD");
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
            log.warn("printMeisai2 read error!",ex);
        }
        return nonedata;

    }//printMeisai2()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs,int page_cnt,int total_page[],int page_arr,int gyo)
    {
        try {
            //2005.08.23
            if (gyo == 0) {
                //「100」「150」欄は、高校の数学と英語のみで必要
                if (param[5].equals("2") && 
                    ((rs.getString("TESTSUBCLASSCD")).equals("3") || (rs.getString("TESTSUBCLASSCD")).equals("5")))
                {
                    svf.VrSetForm("KNJL322_1.frm", 1);//高校の数学と英語用フォーム
                    setInfluenceName(db2, svf, param);
                } else {
                    svf.VrSetForm("KNJL322_2.frm", 1);//その他用フォーム
                    setInfluenceName(db2, svf, param);
                }
            }

            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("SUBCLASS"     , rs.getString("TESTSUBCLASS_NAME") );
            if (param[9] != null){
                svf.VrsOut("EXAM_PLACE"     , rs.getString("EXAMHALL_NAME") );
            }
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , String.valueOf(total_page[page_arr]) );
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len = "0";
        String len2 = "0";
        try {
            //2005.08.28
            len = (gyo < 50) ? "1" : 
                  (gyo < 100) ? "2" : 
                  (gyo < 150) ? "3" : 
                  (gyo < 200) ? "4" : 
                  (gyo < 250) ? "5" : "6";
            gyo = (gyo < 50) ? gyo+1 : 
                  (gyo < 100) ? gyo+1-50 : 
                  (gyo < 150) ? gyo+1-100 : 
                  (gyo < 200) ? gyo+1-150 : 
                  (gyo < 250) ? gyo+1-200 : gyo+1-250;
            len2 = (10 < (rs.getString("NAME")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("EXAMNO"+len     ,gyo      , rs.getString("EXAMNO") );
            svf.VrsOutn("POINT"+len      ,gyo      , rs.getString("A_SCORE") );
            svf.VrsOutn("100POINT"+len   ,gyo      , rs.getString("A_SCORE") );
            svf.VrsOutn("150POINT"+len   ,gyo      , ((rs.getString("B_SCORE_FLG")).equals("ON")) ? rs.getString("B_SCORE") : "" );//---2005.08.30
            svf.VrsOutn("NAME"+len+len2  ,gyo      , rs.getString("NAME") );
        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


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
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,SEX,DESIREDIV ");//---2005.08.30
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[11])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[11] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (param[4].equals("1")) //会場
                stb.append("       AND EXAMHALLNO IN "+param[3]+" ");
            if (param[4].equals("2")) //受験番号
                stb.append("       AND EXAMNO IN "+param[3]+" ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT TESTDIV,TESTSUBCLASSCD, ");
            stb.append("           EXAMNO,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (!param[2].equals("99")) //試験科目
                stb.append("       AND TESTSUBCLASSCD = '"+param[2]+"' ");
            if (param[4].equals("2")) //受験番号
                stb.append("       AND EXAMNO IN "+param[3]+" ");
            stb.append("    ) ");
            //受験コースマスタ・志望区分マスタ---2005.08.30
            stb.append(",EXAM_WISHDIV AS ( ");
            stb.append("    SELECT DESIREDIV,WISHNO, ");
            stb.append("           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ");
            stb.append("           W1.EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           W1.EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST W1, ENTEXAM_WISHDIV_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.EXAMCOURSE_MARK IN ('S','K') AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       CASE WHEN T2.TESTSUBCLASSCD = '3' AND W1.MARK = 'S' THEN 'ON' ");//---2005.08.30
            stb.append("            WHEN T2.TESTSUBCLASSCD = '5' AND W1.MARK = 'K' THEN 'ON' ");//---2005.08.30
            stb.append("            ELSE 'OFF' END AS B_SCORE_FLG, ");//---2005.08.30
            stb.append("       T2.TESTSUBCLASSCD,N2.NAME1 AS TESTSUBCLASS_NAME, ");
            stb.append("       T2.EXAMNO,T2.A_SCORE,T2.B_SCORE,T1.NAME,VALUE(T1.SEX,'0') AS SEX ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN EXAM_WISHDIV W1 ON W1.DESIREDIV=T1.DESIREDIV, ");//---2005.08.30
            stb.append("       EXAM_SCORE T2 ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L009' AND N2.NAMECD2=T2.TESTSUBCLASSCD ");
            stb.append("WHERE  T1.TESTDIV=T2.TESTDIV AND ");
            stb.append("       T1.EXAMNO=T2.EXAMNO ");
            stb.append("ORDER BY T2.TESTDIV,T2.TESTSUBCLASSCD,T2.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT T1.TESTDIV,T1.EXAMHALLNO,T2.EXAMHALL_NAME,EXAMNO,NAME,SEX,DESIREDIV ");//---2005.08.30
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("           LEFT JOIN ENTEXAM_HALL_DAT T2 ON T1.EXAMHALLNO = T2.EXAMHALLCD ");
            stb.append("           AND T1.TESTDIV = T2.TESTDIV ");
            stb.append("    WHERE  T1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[11])) {
                stb.append("           AND T1.SPECIAL_REASON_DIV = '" + param[11] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND T1.TESTDIV = '"+param[1]+"' ");
            if (param[4].equals("1")) //会場
                stb.append("       AND T1.EXAMHALLNO IN "+param[3]+" ");
            if (param[4].equals("2")) //受験番号
                stb.append("       AND T1.EXAMNO IN "+param[3]+" ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT TESTDIV,TESTSUBCLASSCD, ");
            stb.append("           EXAMNO,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (!param[2].equals("99")) //試験科目
                stb.append("       AND TESTSUBCLASSCD = '"+param[2]+"' ");
            if (param[4].equals("2")) //受験番号
                stb.append("       AND EXAMNO IN "+param[3]+" ");
            stb.append("    ) ");
            //受験コースマスタ・志望区分マスタ---2005.08.30
            stb.append(",EXAM_WISHDIV AS ( ");
            stb.append("    SELECT DESIREDIV,WISHNO, ");
            stb.append("           W1.COURSECD||W1.MAJORCD||W1.EXAMCOURSECD AS COURSE, ");
            stb.append("           W1.EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           W1.EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST W1, ENTEXAM_WISHDIV_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.EXAMCOURSE_MARK IN ('S','K') AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       CASE WHEN T2.TESTSUBCLASSCD = '3' AND W1.MARK = 'S' THEN 'ON' ");//---2005.08.30
            stb.append("            WHEN T2.TESTSUBCLASSCD = '5' AND W1.MARK = 'K' THEN 'ON' ");//---2005.08.30
            stb.append("            ELSE 'OFF' END AS B_SCORE_FLG, ");//---2005.08.30
            stb.append("       T2.TESTSUBCLASSCD,N2.NAME1 AS TESTSUBCLASS_NAME, ");
            stb.append("       T1.EXAMHALLNO,T1.EXAMHALL_NAME, ");
            stb.append("       T2.EXAMNO,T2.A_SCORE,T2.B_SCORE,T1.NAME,VALUE(T1.SEX,'0') AS SEX ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN EXAM_WISHDIV W1 ON W1.DESIREDIV=T1.DESIREDIV, ");//---2005.08.30
            stb.append("       EXAM_SCORE T2 ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L009' AND N2.NAMECD2=T2.TESTSUBCLASSCD ");
            stb.append("WHERE  T1.TESTDIV=T2.TESTDIV AND ");
            stb.append("       T1.EXAMNO=T2.EXAMNO ");
            stb.append("ORDER BY T2.TESTDIV,T2.TESTSUBCLASSCD,T1.EXAMHALLNO,T2.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai2 error!",e);
        }
log.debug(stb);
        return stb.toString();

    }//statementMeisai2()の括り

    /**
     *  試験区分・試験科目毎の総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            if (param[9] == null){
                stb.append("    SELECT TESTDIV,EXAMNO,NAME,SEX ");
            }else {
                stb.append("    SELECT TESTDIV,EXAMHALLNO,EXAMNO,NAME,SEX ");
            }
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[11])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[11] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (param[4].equals("1")) //会場
                stb.append("       AND EXAMHALLNO IN "+param[3]+" ");
            if (param[4].equals("2")) //受験番号
                stb.append("       AND EXAMNO IN "+param[3]+" ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT TESTDIV,TESTSUBCLASSCD, ");
            stb.append("           EXAMNO,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (!param[2].equals("99")) //試験科目
                stb.append("       AND TESTSUBCLASSCD = '"+param[2]+"' ");
            if (param[4].equals("2")) //受験番号
                stb.append("       AND EXAMNO IN "+param[3]+" ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.TESTDIV, ");
            stb.append("       T2.TESTSUBCLASSCD, ");
            if (param[9] != null){
                stb.append("       T1.EXAMHALLNO, ");
            }
            stb.append("       CASE WHEN 0 < MOD(COUNT(*),300) THEN COUNT(*)/300 + 1 ELSE COUNT(*)/300 END AS COUNT ");
            stb.append("FROM   EXAM_BASE T1, EXAM_SCORE T2 ");
            stb.append("WHERE  T1.TESTDIV=T2.TESTDIV AND ");
            stb.append("       T1.EXAMNO=T2.EXAMNO ");
            if (param[9] == null){
                stb.append("GROUP BY T2.TESTDIV,T2.TESTSUBCLASSCD ");
                stb.append("ORDER BY T2.TESTDIV,T2.TESTSUBCLASSCD ");
            }else {
                stb.append("GROUP BY T2.TESTDIV,T2.TESTSUBCLASSCD,T1.EXAMHALLNO ");
                stb.append("ORDER BY T2.TESTDIV,T2.TESTSUBCLASSCD,T1.EXAMHALLNO ");
            }
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
