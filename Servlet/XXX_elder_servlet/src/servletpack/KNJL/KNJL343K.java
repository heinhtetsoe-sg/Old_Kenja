// kanji=漢字
/*
 * $Id: 914d7e328eaa768e4b9b59e7b4a0c5f79948d4ab $
 *
 * 作成日: 2005/08/04 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３４３＞  入試選考結果一覧
 *
 *  2005/08/04 nakamoto 作成日
 *  2005/11/07 NO001 m-yama 通知承諾処理追加
 *  2005/12/20 NO002 m-yama 通知承諾がnullは2で出力
 *  2005/12/31 NO003 m-yama 通知承諾をデータをそのまま出力
 */

public class KNJL343K {


    private static final Log log = LogFactory.getLog(KNJL343K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[12];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 1:前期,2:後期
            param[2] = request.getParameter("DATE");                        //印刷日付
            //塾コードリスト
            String classcd[] = request.getParameterValues("DATA_SELECTED");
            param[3] = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) param[3] = param[3] + ",";
                param[3] = param[3] + "'" + classcd[ia] + "'";
            }
            param[3] = param[3] + ")";

            param[4] = request.getParameter("OUTPUT");                      //通知承諾可のみ
            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
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
    
    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        svf.VrSetForm("KNJL343.frm", 1);

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        try {
            getSchoolName(db2,svf,param);
        } catch( Exception e ){
            log.warn("SchoolName get error!",e);
        }

    //  基準点
        try {
            getBoderScore(db2,svf,param);
        } catch( Exception e ){
            log.warn("BoderScore get error!",e);
        }

    //  作成日
        try {
            param[8] = KNJ_EditDate.h_format_JP(param[2]);
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            getPriSchoolCnt(db2,svf,param);
            int total_page[] = new int[Integer.parseInt(param[10])];//塾
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**塾マスタの塾数を取得*/
    private void getPriSchoolCnt(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query(statementPriSchoolCnt(param));
            ResultSet rs = db2.getResultSet();

            param[10] = "100";
            while( rs.next() ){
                param[10] = rs.getString("PRI_CNT");
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getPriSchoolCnt read error!",ex);
        }

    }//getPriSchoolCnt()の括り


    /**学校*/
    private void getSchoolName(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query("SELECT YEAR,SCHOOLNAME1 FROM SCHOOL_MST ORDER BY YEAR");
            ResultSet rs = db2.getResultSet();

            param[6] = "";
            while( rs.next() ){
                param[6] = rs.getString("SCHOOLNAME1");
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getSchoolName read error!",ex);
        }

    }//getSchoolName()の括り


    /**基準点*/
    private void getBoderScore(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query(statementBoderScore(param));
            ResultSet rs = db2.getResultSet();

            String score = "";
            param[9] = "";
            while( rs.next() ){
                score = (rs.getString("SCORE") != null) ? rs.getString("SCORE") : "XXX";
                param[9] = param[9] + rs.getString("NAME") + score + "点　";
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getBoderScore read error!",ex);
        }

    }//getBoderScore()の括り


    /**塾コード毎の総ページ数*/
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
            String ps_cd = "d";
            while( rs.next() ){
                //１ページ印刷
                if (49 < gyo || 
                    (!ps_cd.equals("d") && !ps_cd.equals(rs.getString("PS_CD"))) ) {
                    //合計印刷
                    if ((!ps_cd.equals("d") && !ps_cd.equals(rs.getString("PS_CD"))) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!ps_cd.equals("d") && !ps_cd.equals(rs.getString("PS_CD"))) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,total_page,page_arr);
                //明細データ
                printScore(svf,param,rs,gyo);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                ps_cd = rs.getString("PS_CD");
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
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs,int page_cnt,int total_page[],int page_arr)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLNAME"   , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("PS_CD"        , rs.getString("PS_CD") );
            svf.VrsOut("PS_SCHOOLNAME", rs.getString("PRISCHOOL_NAME") );
            svf.VrsOut("STANDARD_POINT", param[9] );
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , String.valueOf(total_page[page_arr]) );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len2 = "0";
        try {
            gyo = gyo+1;
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"      ,gyo      , rs.getString("EXAMNO") );
            svf.VrsOutn("SEX"         ,gyo      , rs.getString("SEX_NAME") );
            svf.VrsOutn("NAME"+len2   ,gyo      , rs.getString("NAME") );

            svf.VrsOutn("JUDGEMENT"   ,gyo      , rs.getString("JUDGE2") );
            svf.VrsOutn("EXAMCOURSE"  ,gyo      , rs.getString("JUDGE1") );
            //NO001
            svf.VrsOutn("NOTICE"      ,gyo      , rs.getString("APPROVAL_FLG") );

            if( (rs.getString("JUDGEMENT")).equals("7") ) {
                svf.VrsOutn("POINT1"      ,gyo      , rs.getString("SCORE1") );
                svf.VrsOutn("POINT2"      ,gyo      , rs.getString("SCORE2") );
                svf.VrsOutn("POINT3"      ,gyo      , rs.getString("SCORE3") );
                svf.VrsOutn("POINT4"      ,gyo      , rs.getString("SCORE4") );
                svf.VrsOutn("TOTAL"       ,gyo      , rs.getString("SCORE_SUM") );
            }
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
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,DESIREDIV,NATPUBPRIDIV,PS_CD, ");
            stb.append("           APPROVAL_FLG, ");    //NO003
            stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[11])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[11] + "' AND ");
            }
            stb.append("           TESTDIV = '"+param[1]+"' AND ");
            stb.append("           PS_CD IN "+param[3]+" ");
            //NO001
            if (param[4] != null) {
                stb.append("           AND APPROVAL_FLG = '1' ");
            }
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
            //満点マスタ：採用区分
            stb.append(",PERFECT AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("           TESTSUBCLASSCD,ADOPTIONDIV ");
            stb.append("    FROM   ENTEXAM_PERFECT_MST ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           TESTDIV='"+param[1]+"' AND ");
            stb.append("           VALUE(A_TOTAL_FLG,'1') = '1' ");//A配点集計フラグ（0:集計に含めない／1:集計に含める）
            stb.append("    ) ");
            //成績：後期（合計・平均・順位の計算用）
            stb.append(",BASE_SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO,T1.COURSE,T2.TESTSUBCLASSCD,T2.A_SCORE ");
            stb.append("    FROM   LAST_COURSE T1 ");
            stb.append("           LEFT JOIN EXAM_SCORE T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append("    ) ");
            stb.append(",BASE_SCORE2 AS ( ");
            stb.append("    SELECT T1.EXAMNO,T1.COURSE,T1.TESTSUBCLASSCD,T1.A_SCORE,T2.ADOPTIONDIV ");
            stb.append("    FROM   BASE_SCORE T1 ");
            stb.append("           LEFT JOIN PERFECT T2 ON T2.COURSE=T1.COURSE AND T2.TESTSUBCLASSCD=T1.TESTSUBCLASSCD ");
            stb.append("    ) ");
            stb.append(",BASE_SCORE3 AS ( ");
            stb.append("    SELECT EXAMNO,COURSE,TESTSUBCLASSCD,A_SCORE ");
            stb.append("    FROM   BASE_SCORE2 ");
            stb.append("    WHERE  ADOPTIONDIV='0' ");
            stb.append("    UNION ");
            stb.append("    SELECT EXAMNO,COURSE,'0' AS TESTSUBCLASSCD,MAX(A_SCORE) AS A_SCORE ");
            stb.append("    FROM   BASE_SCORE2 ");
            stb.append("    WHERE  ADOPTIONDIV='1' ");//アラカルト
            stb.append("    GROUP BY EXAMNO,COURSE ");
            stb.append("    ) ");
            //成績：後期（合計・平均・順位）
            stb.append(",SCORE_S AS ( ");
            stb.append("    SELECT EXAMNO,COURSE, ");
            stb.append("           SUM(A_SCORE) AS SCORE_SUM, ");
            stb.append("           DECIMAL(ROUND(AVG(FLOAT(A_SCORE))*10,0)/10,5,1) AS SCORE_AVG, ");
            stb.append("           RANK() OVER (PARTITION BY COURSE ORDER BY SUM(A_SCORE) DESC) AS SCORE_RNK ");
            stb.append("    FROM   BASE_SCORE3 ");
            stb.append("    GROUP BY EXAMNO,COURSE ");
            stb.append("    ) ");
            //成績：前期（素点）
            stb.append(",SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '1' THEN T2.A_SCORE ELSE NULL END) AS SCORE1, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '2' THEN T2.A_SCORE ELSE NULL END) AS SCORE2, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '3' THEN T2.A_SCORE ELSE NULL END) AS SCORE3, ");
            stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '4' THEN T2.A_SCORE ELSE NULL END) AS SCORE4 ");
            stb.append("    FROM   EXAM_BASE T1, EXAM_SCORE T2 ");
            stb.append("    WHERE  T2.EXAMNO=T1.EXAMNO ");
            stb.append("    GROUP BY T1.EXAMNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       T1.PS_CD,T6.PRISCHOOL_NAME, ");
            stb.append("       T1.EXAMNO,T1.NAME,T1.NAME_KANA, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T1.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ");
            stb.append("       VALUE(T1.JUDGEMENT,'') AS JUDGEMENT, ");
            stb.append("       T1.APPROVAL_FLG, "); //NO002
            stb.append("       T4.EXAMCOURSE_ABBV AS JUDGE1, ");
            stb.append("       CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN '合' ");
            stb.append("            WHEN T1.JUDGEMENT = '7' THEN '否' ");
            stb.append("            WHEN T2.SCORE1 IS NULL AND T2.SCORE2 IS NULL AND ");
            stb.append("                 T2.SCORE3 IS NULL AND T2.SCORE4 IS NULL THEN '欠' ");
            stb.append("            ELSE NULL END AS JUDGE2, ");
            stb.append("       T2.SCORE1,T2.SCORE2,T2.SCORE3,T2.SCORE4,T5.SCORE_SUM ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD = T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("       LEFT JOIN SCORE_S T5 ON T5.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN PRISCHOOL_MST T6 ON T6.PRISCHOOLCD=T1.PS_CD ");
            stb.append("ORDER BY T1.TESTDIV,T1.PS_CD,T1.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  塾コード毎の総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,DESIREDIV,NATPUBPRIDIV,PS_CD, ");
            stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[11])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[11] + "' AND ");
            }
            stb.append("           TESTDIV = '"+param[1]+"' AND ");
            stb.append("           PS_CD IN "+param[3]+" ");
            //NO001
            if (param[4] != null)
                stb.append("           AND APPROVAL_FLG = '1' ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.PS_CD, ");
            stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("GROUP BY T1.PS_CD ");
            stb.append("ORDER BY T1.PS_CD ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り


    /**
     *  コース毎の基準点を取得
     *
     */
    private String statementBoderScore(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //受験コースマスタ
            stb.append("WITH EXAM_COURSE AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("           EXAMCOURSE_NAME ");
            stb.append("    FROM   ENTEXAM_COURSE_MST ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            stb.append("    ) ");
            //合格点マスタ
            stb.append(",EXAM_PASS AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("           BORDER_SCORE ");
            stb.append("    FROM   ENTEXAM_PASSINGMARK_MST ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND TESTDIV='"+param[1]+"' ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.COURSE,VALUE(EXAMCOURSE_NAME,'') AS NAME,BORDER_SCORE AS SCORE ");
            stb.append("FROM   EXAM_COURSE T1 ");
            stb.append("       LEFT JOIN EXAM_PASS T2 ON T2.COURSE=T1.COURSE ");
            stb.append("ORDER BY T1.COURSE DESC ");
        } catch( Exception e ){
            log.warn("statementBoderScore error!",e);
        }
        return stb.toString();

    }//statementBoderScore()の括り


    /**
     *  塾マスタの塾数を取得
     *
     */
    private String statementPriSchoolCnt(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT YEAR, COUNT(*) AS PRI_CNT ");
            stb.append("FROM   V_PRISCHOOL_MST ");
            stb.append("WHERE  YEAR='"+param[0]+"' ");
            stb.append("GROUP BY YEAR ");
            stb.append("HAVING 0 < COUNT(*) ");
        } catch( Exception e ){
            log.warn("statementPriSchoolCnt error!",e);
        }
        return stb.toString();

    }//statementPriSchoolCnt()の括り



}//クラスの括り
