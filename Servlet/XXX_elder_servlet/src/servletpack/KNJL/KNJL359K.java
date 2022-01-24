// kanji=漢字
/*
 * $Id: 566fc612e284b1a6c5c013d30086caa8fa21c07f $
 *
 * 作成日: 2006/01/19 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３５９＞  平均点・最高・最低点一覧（受験・合格者）（高校）
 *    ○一般は、受験番号5000番台以外の者
 *    ○附属は、受験番号5000番台の者
 *  　　　○受験者とは？
 *  　　　　　○受験者は、志願者基礎データの合否判定フラグが(7)以下の者---nullも含める
 *　　　　　　○コースは、志願者基礎データの志望区分の第１志望コースを参照
 *　　　　○合格者とは？
 *　　　　　　○一般は、志願者基礎データの合否判定フラグが(4)以下且つ正規合格フラグが(1,2,3)の者。
 *　　　　　　○附属は、志願者基礎データの合否判定フラグが(4)以下の者。
 *　　　　　　○コースは、志願者基礎データの合格コースを参照
 *　　　　○得点は、志願者得点データの得点（進学コース・特進コースはＡ得点、理数科・国際コースはＢ得点）をみる
 *　　　　　但し、５００点満点の表だけは、志願者得点データの得点（Ａ得点）をみる
 *　　　　○各教科・（）内人数は、全員含める
 *　　　　○平均または計は、１科目でも未受験の者は含めない
 *　　　　　　○平均は、(国＋社＋算＋理＋英)の平均
 *　　　　　　○計は、(国＋社＋算＋理＋英)の最高点・最低点
 *    ○一般に「全体・専願・併願」を追加
 *
 *  2006/01/19 nakamoto 作成日
 *  2006/01/24 nakamoto NO001 各５００点満点の表を一番上に追加
 *  2006/01/25 nakamoto NO002 処理速度向上のためＳＱＬ修正
 *  2006/01/31 nakamoto NO003 一般に「全体・専願・併願」を追加。この対応により、パラメータ名変更。
 *  2006/01/31 nakamoto NO004 処理速度向上のためＳＱＬ修正 NO003の対応
 *  2006/02/10 nakamoto NO005 中高一貫出力時、NullPointerExceptionエラーの対応---但し、今現在、中高一貫は、コースが普のため出力できない(仕様が未確定である)
 *  2006/02/11 nakamoto NO006 受験者の条件を変更
 *  2006/12/13 m-yama   NO007 性別がNullの場合のNullPointerExceptionエラーの対応
 */

public class KNJL359K {


    private static final Log log = LogFactory.getLog(KNJL359K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[8];//NO003

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                    //次年度
            param[1] = request.getParameter("TESTDIV");                 //入試区分 1:前期,2:後期
//          param[2] = request.getParameter("OUTPUT2");                 //出力対象 1:受験者平均点一覧
//                                                                                // 2:合格者平均点一覧
//                                                                                // 3:受験者最高点・最低点一覧
//                                                                                // 4:合格者最高点・最低点一覧
            param[3] = request.getParameter("JHFLG");                   //中学高校フラグ
//          param[4] = request.getParameter("FZKFLG"+param[2]);         //1一般・2附属フラグ

            //NO003
            param[5] = request.getParameter("OUTPUT1");                 //出力帳票 1:平均点,2:最高点・最低点
            param[2] = request.getParameter("OUTPUT2");                 //対象１ 1:受験者,2:合格者
            param[4] = request.getParameter("OUTPUT3");                 //対象２ 1:一般,2:中高一貫
            param[6] = request.getParameter("SHDIV");                   //専／併 9:全体,1:専願,2:併願
            param[7] = request.getParameter("SPECIAL_REASON_DIV");      //特別理由
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
        if (param[5].equals("1")) svf.VrSetForm("KNJL359_1.frm", 1);//平均点
        if (param[5].equals("2")) svf.VrSetForm("KNJL359_2.frm", 1);//最高点・最低点

    //  次年度
        try {
            svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" );
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  タイトル
        if (param[2].equals("1")) svf.VrsOut("TITLE"    ,  "受験者" );
        if (param[2].equals("2")) svf.VrsOut("TITLE"    ,  "合格者" );

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

    //  サブタイトル
        if (param[4] != null && param[4].equals("2")) {
            svf.VrsOut("SUBTITLE" , "(中高一貫)" );
        } else {
            if (param[6].equals("1")) svf.VrsOut("SUBTITLE" , "（一般：専願）" );
            if (param[6].equals("2")) svf.VrsOut("SUBTITLE" , "（一般：併願）" );
            if (param[6].equals("9")) svf.VrsOut("SUBTITLE" , "（一般：全体）" );
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            setInfluenceName(db2, svf, param);

            //明細データ---1理数科(S)・2国際(K)・3特進(T)・4進学(P)・5全体(500点満点)
            for (int ia = 1; ia <= 5; ia++) {
                //各教科
                if( printSubclassScore(db2,svf,param,ia) ) nonedata = true;//出力フラグ
            }
            if( nonedata ){
                //各人数・平均・計
                for (int ia = 1; ia <= 5; ia++) {
                    printTotalScore(db2,svf,param,ia);
                }

                svf.VrEndPage();
            }
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**明細データ印刷処理(各教科)*/
    private boolean printSubclassScore(DB2UDB db2,Vrw32alp svf,String param[],int table_no)
    {
        boolean nonedata = false;
        try {
            db2.query(getSqlSubclassScore(param,table_no));
            ResultSet rs = db2.getResultSet();

            String tbl = String.valueOf(table_no);//1,2,3,4,5
            String len = "0";
            int gyo = 0;
            while( rs.next() ){
                if (rs.getInt("SEX") == 0) continue;    //NO007
                if (param[4].equals("1") && !param[6].equals(rs.getString("SHDIV"))) continue;//NO005

                len = rs.getString("TESTSUB");//1,2,3,4,5
                gyo = rs.getInt("SEX");//1,2,3

                //平均点
                if (param[5].equals("1")) {
                    svf.VrsOutn("AVERAGE"+tbl+"_"+len    ,gyo    , rs.getString("HEI") );

                //最高点・最低点
                } else {
                    svf.VrsOutn("SCORE"+tbl+"_"+len   ,gyo*2      , rs.getString("MIN_SCORE") );
                    svf.VrsOutn("SCORE"+tbl+"_"+len   ,gyo*2-1    , rs.getString("MAX_SCORE") );
                }

                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSubclassScore read error!",ex);
        }
        return nonedata;

    }//printSubclassScore()の括り


    /**明細データ印刷処理(各人数・平均・計)*/
    private void printTotalScore(DB2UDB db2,Vrw32alp svf,String param[],int table_no)
    {
        try {
            db2.query(getSqlTotalScore(param,table_no));
            ResultSet rs = db2.getResultSet();

            String tbl = String.valueOf(table_no);//1,2,3,4,5
            String len = "0";
            int gyo = 0;
            while( rs.next() ){
                if (rs.getInt("SEX") == 0) continue;    //NO007
                if (param[4].equals("1") && !param[6].equals(rs.getString("SHDIV"))) continue;//NO005

                len = rs.getString("KEINO");//1,4
                gyo = rs.getInt("SEX");//1,2,3

                //人数
                if (len.equals("4")) {
                    if (gyo == 1) svf.VrsOut("BOY"+tbl    , rs.getString("NIN") );
                    if (gyo == 2) svf.VrsOut("GIRL"+tbl   , rs.getString("NIN") );
                    if (gyo == 3) svf.VrsOut("TOTAL"+tbl  , rs.getString("NIN") );
                } else {
                    //平均点
                    if (param[5].equals("1")) {
                        svf.VrsOutn("TOTAL_AVERAGE"+tbl    ,gyo    , rs.getString("HEI") );
                    //最高点・最低点
                    } else {
                        svf.VrsOutn("TOTAL_SCORE"+tbl   ,gyo*2      , rs.getString("MIN_SCORE") );
                        svf.VrsOutn("TOTAL_SCORE"+tbl   ,gyo*2-1    , rs.getString("MAX_SCORE") );
                    }
                }

            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printTotalScore read error!",ex);
        }

    }//printTotalScore()の括り


    /**
     *  各教科の平均点・最高・最低点を取得
     *
     */
    private String getSqlSubclassScore(String param[],int table_no)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //各共通表
            stb.append( getSqlExamCommon(param,table_no) );

            //各抽出データメイン表
            stb.append(",EXAM_MAIN AS ( ");
            stb.append( getSqlExamMain(param,table_no) );
            stb.append("    ) ");

            //メイン表
            stb.append("SELECT SHDIV,VALUE(SEX, '0') AS SEX,TESTSUB, ");//NO004 NO007
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 1) stb.append("       AND MARK = 'S' ");//理数科
        if (table_no == 2) stb.append("       AND MARK = 'K' ");//国際
        if (table_no == 3) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 4) stb.append("       AND MARK = 'P' ");//進学
        if (table_no == 5) stb.append("       AND MARK IN('S','K','T','P') ");//全体 NO001
            stb.append("GROUP BY SHDIV,SEX,TESTSUB ");//NO004
            stb.append("UNION ");
            stb.append("SELECT SHDIV,'3' AS SEX,TESTSUB, ");//NO004
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 1) stb.append("       AND MARK = 'S' ");//理数科
        if (table_no == 2) stb.append("       AND MARK = 'K' ");//国際
        if (table_no == 3) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 4) stb.append("       AND MARK = 'P' ");//進学
        if (table_no == 5) stb.append("       AND MARK IN('S','K','T','P') ");//全体 NO001
            stb.append("GROUP BY SHDIV,TESTSUB ");//NO004
            stb.append("ORDER BY SHDIV,SEX,TESTSUB ");//NO004
        } catch( Exception e ){
            log.warn("getSqlSubclassScore error!",e);
        }
        return stb.toString();

    }//getSqlSubclassScore()の括り


    /**
     *  各人数・平均・計を取得
     *
     */
    private String getSqlTotalScore(String param[],int table_no)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //各共通表
            stb.append( getSqlExamCommon(param,table_no) );

            //各抽出データメイン表
            stb.append(",EXAM_MAIN AS ( ");
            stb.append( getSqlExamMain(param,table_no) );
            stb.append("    ) ");

            //人数および平均・計---１科目でも未受験は、含めない
            stb.append(",EXAM_MAIN_KEI AS ( ");
            stb.append( getSqlExamMainKei(param,table_no) );
            stb.append("    ) ");

            //メイン表
            stb.append("SELECT SHDIV,VALUE(SEX, '0') AS SEX,KEINO, ");//NO004 NO007
            stb.append("       COUNT(*) AS NIN, ");
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN_KEI ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 1) stb.append("       AND MARK = 'S' ");//理数科
        if (table_no == 2) stb.append("       AND MARK = 'K' ");//国際
        if (table_no == 3) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 4) stb.append("       AND MARK = 'P' ");//進学
        if (table_no == 5) stb.append("       AND MARK IN('S','K','T','P') ");//全体 NO001
            stb.append("GROUP BY SHDIV,SEX,KEINO ");//NO004
            stb.append("UNION ");
            stb.append("SELECT SHDIV,'3' AS SEX,KEINO, ");//NO004
            stb.append("       COUNT(*) AS NIN, ");
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN_KEI ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 1) stb.append("       AND MARK = 'S' ");//理数科
        if (table_no == 2) stb.append("       AND MARK = 'K' ");//国際
        if (table_no == 3) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 4) stb.append("       AND MARK = 'P' ");//進学
        if (table_no == 5) stb.append("       AND MARK IN('S','K','T','P') ");//全体 NO001
            stb.append("GROUP BY SHDIV,KEINO ");//NO004
            stb.append("ORDER BY SHDIV,KEINO,SEX ");//NO004
        } catch( Exception e ){
            log.warn("getSqlTotalScore error!",e);
        }
        return stb.toString();

    }//getSqlTotalScore()の括り


    /**
     *  各共通表を取得
     *
     */
    private String getSqlExamCommon(String param[],int table_no)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ---受験者全員
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,DESIREDIV,SEX,JUDGEMENT,REGULARSUCCESS_FLG AS SUCCESS_FLG, ");
//NO005-->
            if (param[4].equals("1") && !param[6].equals("9")) {
                stb.append("       VALUE(SHDIV,'') AS SHDIV, ");//NO004
            } else {
                stb.append("       '9' AS SHDIV, ");//NO004
            }
//NO005<--
            stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            //附属
            if (param[4] != null && param[4].equals("2")) {
                stb.append("       AND EXAMNO BETWEEN '5000' AND '5999' ");
            //一般
            } else {
                stb.append("       AND (EXAMNO < '5000' OR '5999' < EXAMNO) ");
            }
            //受験者
            if (param[2].equals("1")) {
                stb.append("       AND ((VALUE(JUDGEMENT,'88') > '0' AND VALUE(JUDGEMENT,'88') <= '7') OR VALUE(JUDGEMENT,'88') = '9') ");//NO006
            //合格者
            } else {
                //附属
                if (param[4] != null && param[4].equals("2")) {
                    stb.append("   AND ((JUDGEMENT > '0' AND JUDGEMENT <= '4') OR JUDGEMENT = '9') ");
                //一般
                } else {
                    stb.append("   AND ((JUDGEMENT > '0' AND JUDGEMENT <= '4') OR JUDGEMENT = '9') ");
                    stb.append("   AND REGULARSUCCESS_FLG <= '3' ");
                }
            }
            if (!"9".equals(param[7])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[7] + "' ");
            }
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ---第１志望コース
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.DESIREDIV,W2.EXAMCOURSE_MARK AS MARK, ");
            stb.append("           W2.COURSECD||W2.MAJORCD||W2.EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1,ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.WISHNO = '1' AND ");
            stb.append("           W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("           W2.COURSECD = W1.COURSECD AND ");
            stb.append("           W2.MAJORCD = W1.MAJORCD AND ");
            stb.append("           W2.EXAMCOURSECD = W1.EXAMCOURSECD ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO,TESTSUBCLASSCD AS TESTSUB,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT T1");
            stb.append("    INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("           AND T1.EXAMNO = T2.EXAMNO ");
            stb.append("    WHERE  T1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("          (A_SCORE IS NOT NULL OR B_SCORE IS NOT NULL) ");
            if (!"9".equals(param[7])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[7] + "' ");
            }
            stb.append("    ) ");
            //受験コースマスタ---合格コース
            stb.append(",EXAM_COURSE AS ( ");
            stb.append("    SELECT EXAMCOURSE_MARK AS MARK, ");
            stb.append("           COURSECD||MAJORCD||EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_COURSE_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    ) ");
        } catch( Exception e ){
            log.warn("getSqlExamCommon error!",e);
        }
        return stb.toString();

    }//getSqlExamCommon()の括り


    /**
     *  各抽出データメイン表を取得
     *
     */
    private String getSqlExamMain(String param[],int table_no)
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("    SELECT T1.EXAMNO,T1.SEX,T2.TESTSUB, T1.SHDIV,");//NO004
            //受験者
            if (param[2].equals("1")) {
                //５００点満点の表 NO001
                if (table_no == 5) {
                    stb.append("   CASE WHEN T3.MARK IN('S','K','T','P') THEN T2.A_SCORE ");
                    stb.append("        ELSE T2.A_SCORE END AS SCORE, ");//NO002
                } else {
                    stb.append("   CASE WHEN T3.MARK IN('S','K') THEN T2.B_SCORE ");//理数科・国際コース
                    stb.append("        ELSE T2.A_SCORE END AS SCORE, ");
                }
                stb.append("       T3.COURSE,T3.MARK ");//第１志望コース
            //合格者
            } else {
                //５００点満点の表 NO001
                if (table_no == 5) {
                    stb.append("   CASE WHEN T4.MARK IN('S','K','T','P') THEN T2.A_SCORE ");
                    stb.append("        ELSE T2.A_SCORE END AS SCORE, ");//NO002
                } else {
                    stb.append("   CASE WHEN T4.MARK IN('S','K') THEN T2.B_SCORE ");//理数科・国際コース
                    stb.append("        ELSE T2.A_SCORE END AS SCORE, ");
                }
                stb.append("       T1.SUC_COURSE AS COURSE,T4.MARK ");//合格コース
            }
            stb.append("    FROM   EXAM_BASE T1 ");
            stb.append("           LEFT JOIN EXAM_SCORE T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append("           LEFT JOIN EXAM_WISH T3 ON T3.DESIREDIV = T1.DESIREDIV ");
            stb.append("           LEFT JOIN EXAM_COURSE T4 ON T4.COURSE = T1.SUC_COURSE ");

        } catch( Exception e ){
            log.warn("getSqlExamMain error!",e);
        }
        return stb.toString();

    }//getSqlExamMain()の括り


    /**
     *  人数および平均・計を取得---１科目でも未受験は、含めない
     *
     */
    private String getSqlExamMainKei(String param[],int table_no)
    {
        StringBuffer stb = new StringBuffer();
        try {
                            //平均・計
            stb.append("    SELECT SHDIV,SEX,EXAMNO,MARK,'1' AS KEINO, ");//NO004
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_MAIN ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("           AND TESTSUB IN('1','2','3','4','5') ");
            stb.append("    GROUP BY SHDIV,SEX,EXAMNO,MARK ");//NO004
            stb.append("    HAVING COUNT(*) = 5 ");
                            //人数
            stb.append("    UNION ");
            stb.append("    SELECT SHDIV,SEX,EXAMNO,MARK,'4' AS KEINO, ");//NO004
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_MAIN ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("    GROUP BY SHDIV,SEX,EXAMNO,MARK ");//NO004
        } catch( Exception e ){
            log.warn("getSqlExamMainKei error!",e);
        }
        return stb.toString();

    }//getSqlExamMainKei()の括り



}//クラスの括り
