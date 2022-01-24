// kanji=漢字
/*
 * $Id: 7316632a49e0e495a196e1971c13d9fac79337a8 $
 *
 * 作成日: 2006/01/04 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３５６＞  平均点・最高・最低点一覧（受験・合格者）
 *    ○一般は、受験番号3000番台以外の者
 *    ○附属は、受験番号3000番台の者
 *  　　　○受験者とは？
 *  　　　　　○受験者は、志願者基礎データの合否判定フラグが(7)以下の者
 *　　　　　　○コースは、志願者基礎データの志望区分の第１志望コースを参照
 *　　　　○合格者とは？
 *　　　　　　○一般は、志願者基礎データの合否判定フラグが(4)以下且つ正規合格フラグが(1)の者。
 *　　　　　　○附属は、志願者基礎データの合否判定フラグが(4)以下の者。
 *　　　　　　○コースは、志願者基礎データの合格コースを参照
 *　　　　○得点は、志願者得点データの得点（前期：Ａ得点、後期：医薬はＡ得点、特進・標準はＢ得点）をみる
 *　　　　○各教科・（）内人数は、全員含める
 *　　　　○合Ｓ・合Ａ・合４は、１科目でも未受験の者は含めない
 *　　　　　　○合Ｓは、国＋算＋理
 *　　　　　　○合Ａは、国＋算＋社or理(得点の高い方)
 *　　　　　　○合４は、国＋算＋社＋理
 *
 *  2006/01/04 nakamoto 作成日
 *  2006/01/05 nakamoto NO001 サブタイトルを追加
 *  2006/01/06 nakamoto NO002 （）内の人数は、全部入れる
 *  2006/01/06 nakamoto NO003 合格者の条件を変更
 *  2006/01/06 nakamoto NO004 合格者の条件を再度変更
 *  2006/01/11 nakamoto NO005 合格者の条件を再度再度変更
 *  2006/12/13 m-yama   NO006 性別がNullの場合のNullPointerExceptionエラーの対応
 */

public class KNJL356K {


    private static final Log log = LogFactory.getLog(KNJL356K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[6];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                    //次年度
            param[1] = request.getParameter("TESTDIV");                 //入試区分 1:前期,2:後期
            param[2] = request.getParameter("OUTPUT2");                 //出力対象 1:受験者平均点一覧
                                                                                // 2:合格者平均点一覧
                                                                                // 3:受験者最高点・最低点一覧
                                                                                // 4:合格者最高点・最低点一覧
            param[3] = request.getParameter("JHFLG");                   //中学高校フラグ
            param[4] = request.getParameter("FZKFLG"+param[2]);         //1一般・2附属フラグ
            param[5] = request.getParameter("SPECIAL_REASON_DIV");      //特別理由
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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[5] + "' ");
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
        svf.VrSetForm("KNJL356_"+param[2]+".frm", 1);//1〜4

    //  次年度
        try {
            svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" );
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  試験区分
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='L003' AND NAMECD2='"+param[1]+"' ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                svf.VrsOut("TESTDIV"  , rs.getString("NAME1") );
            }
            db2.commit();
        } catch( Exception e ){
            log.warn("testname get error!",e);
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

    //  サブタイトル---NO001
        if (param[4] != null && param[4].equals("2")) {
            svf.VrsOut("SUBTITLE" , "（附属）" );
        } else {
            svf.VrsOut("SUBTITLE" , "（一般）" );
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //明細データ
            if( printSubclassScore(db2,svf,param,1) ){//各教科:1受験者
                if (param[2].equals("2") || param[2].equals("4")) 
                    printSubclassScore(db2,svf,param,2);//各教科:2合格者
                for (int ia = 3; ia < 6; ia++) {
                    printSubclassScore(db2,svf,param,ia);//各教科:3標準・4特進・5医薬
                }
                setInfluenceName(db2, svf, param);

                printTotalScore(db2,svf,param,1);//各合計:1受験者
                if (param[2].equals("2") || param[2].equals("4")) 
                    printTotalScore(db2,svf,param,2);//各合計:2合格者
                for (int ia = 3; ia < 6; ia++) {
                    printTotalScore(db2,svf,param,ia);//各合計:3標準・4特進・5医薬
                }
                svf.VrEndPage();
                nonedata = true;
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
                len = rs.getString("TESTSUB");//1,2,3,4
                if (rs.getInt("SEX") == 0) continue;    //NO006
                gyo = rs.getInt("SEX");//1,2,3

                //平均点
                if (param[2].equals("1") || param[2].equals("2")) {
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


    /**明細データ印刷処理(各人数・合Ｓ・合Ａ・合４)*/
    private void printTotalScore(DB2UDB db2,Vrw32alp svf,String param[],int table_no)
    {
        try {
            db2.query(getSqlTotalScore(param,table_no));
            ResultSet rs = db2.getResultSet();

            String tbl = String.valueOf(table_no);//1,2,3,4,5
            String len = "0";
            int gyo = 0;
            while( rs.next() ){
                len = rs.getString("KEINO");//1,2,3,4
                if (rs.getInt("SEX") == 0) continue;    //NO006
                gyo = rs.getInt("SEX");//1,2,3

                //人数
                if (len.equals("4")) {//NO002
                    if (gyo == 1) svf.VrsOut("BOY"+tbl    , rs.getString("NIN") );
                    if (gyo == 2) svf.VrsOut("GIRL"+tbl   , rs.getString("NIN") );
                    if (gyo == 3) svf.VrsOut("TOTAL"+tbl  , rs.getString("NIN") );
                }

                //平均点
                if (param[2].equals("1") || param[2].equals("2")) {
                    svf.VrsOutn("TOTAL_AVERAGE"+tbl+"_"+len    ,gyo    , rs.getString("HEI") );

                //最高点・最低点
                } else {
                    svf.VrsOutn("TOTAL_SCORE"+tbl+"_"+len   ,gyo*2      , rs.getString("MIN_SCORE") );
                    svf.VrsOutn("TOTAL_SCORE"+tbl+"_"+len   ,gyo*2-1    , rs.getString("MAX_SCORE") );
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
            stb.append("SELECT VALUE(SEX, '0') AS SEX,TESTSUB, ");  //NO006
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 3) stb.append("       AND MARK = 'H' ");//標準
        if (table_no == 4) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 5) stb.append("       AND MARK = 'I' ");//医薬
            stb.append("GROUP BY SEX,TESTSUB ");
            stb.append("UNION ");
            stb.append("SELECT '3' AS SEX,TESTSUB, ");
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 3) stb.append("       AND MARK = 'H' ");//標準
        if (table_no == 4) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 5) stb.append("       AND MARK = 'I' ");//医薬
            stb.append("GROUP BY TESTSUB ");
            stb.append("ORDER BY SEX,TESTSUB ");
        } catch( Exception e ){
            log.warn("getSqlSubclassScore error!",e);
        }
        return stb.toString();

    }//getSqlSubclassScore()の括り


    /**
     *  (表２)各人数・合Ｓ・合Ａ・合４を取得
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

            //合Ｓ・合Ａ・合４---１科目でも未受験は、含めない
            stb.append(",EXAM_MAIN_KEI AS ( ");
            stb.append( getSqlExamMainKei(param,table_no) );
            stb.append("    ) ");

            //メイン表
            stb.append("SELECT VALUE(SEX, '0') AS SEX,KEINO, ");    //NO006
            stb.append("       COUNT(*) AS NIN, ");
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN_KEI ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 3) stb.append("       AND MARK = 'H' ");//標準
        if (table_no == 4) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 5) stb.append("       AND MARK = 'I' ");//医薬
            stb.append("GROUP BY SEX,KEINO ");
            stb.append("UNION ");
            stb.append("SELECT '3' AS SEX,KEINO, ");
            stb.append("       COUNT(*) AS NIN, ");
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI, ");
            stb.append("       MAX(SCORE) AS MAX_SCORE, ");
            stb.append("       MIN(SCORE) AS MIN_SCORE ");
            stb.append("FROM   EXAM_MAIN_KEI ");
            stb.append("WHERE  SCORE IS NOT NULL ");
        if (table_no == 3) stb.append("       AND MARK = 'H' ");//標準
        if (table_no == 4) stb.append("       AND MARK = 'T' ");//特進
        if (table_no == 5) stb.append("       AND MARK = 'I' ");//医薬
            stb.append("GROUP BY KEINO ");
            stb.append("ORDER BY KEINO,SEX ");
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
            stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV,SEX,JUDGEMENT,REGULARSUCCESS_FLG AS SUCCESS_FLG, ");
            stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[5])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[5] +"' AND ");
            }
            stb.append("           TESTDIV = '"+param[1]+"' ");
            //附属
            if (param[4] != null && param[4].equals("2")) {
                stb.append("       AND EXAMNO BETWEEN '3000' AND '3999' ");
            //一般
            } else {
                stb.append("       AND (EXAMNO < '3000' OR '3999' < EXAMNO) ");
            }
            //受験者
            if (param[2].equals("1") || param[2].equals("3") || table_no == 1) {
                stb.append("       AND ((JUDGEMENT > '0' AND JUDGEMENT <= '7') OR JUDGEMENT = '9') ");
            //合格者---NO004
            } else {
                //附属
                if (param[4] != null && param[4].equals("2")) {
                    stb.append("   AND ((JUDGEMENT > '0' AND JUDGEMENT <= '4') OR JUDGEMENT = '9') ");//NO005
                //一般
                } else {
                    stb.append("   AND ((JUDGEMENT > '0' AND JUDGEMENT <= '4') OR JUDGEMENT = '9') ");//NO005
                    stb.append("   AND REGULARSUCCESS_FLG = '1' ");
                }
            }
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ---第１志望コース
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.DESIREDIV,W2.EXAMCOURSE_MARK AS MARK, ");
            stb.append("           W2.COURSECD||W2.MAJORCD||W2.EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1,ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W1.WISHNO = '1' AND ");
            stb.append("           W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("           W2.COURSECD = W1.COURSECD AND ");
            stb.append("           W2.MAJORCD = W1.MAJORCD AND ");
            stb.append("           W2.EXAMCOURSECD = W1.EXAMCOURSECD ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD AS TESTSUB,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           TESTDIV = '"+param[1]+"' AND ");
            stb.append("          (A_SCORE IS NOT NULL OR B_SCORE IS NOT NULL) ");
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

            stb.append("    SELECT T1.EXAMNO,T1.SEX,T2.TESTSUB, ");
            //受験者
            if (param[2].equals("1") || param[2].equals("3") || table_no == 1) {
                stb.append("       CASE WHEN T1.TESTDIV = '2' AND T3.MARK IN('T','H') ");
                stb.append("            THEN T2.B_SCORE ELSE T2.A_SCORE END AS SCORE, ");
                stb.append("       T3.COURSE,T3.MARK ");
            //合格者---NO004
            } else {
                stb.append("       CASE WHEN T1.TESTDIV = '2' AND T4.MARK IN('T','H') ");
                stb.append("            THEN T2.B_SCORE ELSE T2.A_SCORE END AS SCORE, ");
                stb.append("       T1.SUC_COURSE AS COURSE,T4.MARK ");
            }
            stb.append("    FROM   EXAM_SCORE T2,EXAM_WISH T3,EXAM_BASE T1 ");
            stb.append("           LEFT JOIN EXAM_COURSE T4 ON T4.COURSE = T1.SUC_COURSE ");
            stb.append("    WHERE  T1.EXAMNO = T2.EXAMNO AND ");
            stb.append("           T1.DESIREDIV = T3.DESIREDIV ");

        } catch( Exception e ){
            log.warn("getSqlExamMain error!",e);
        }
        return stb.toString();

    }//getSqlExamMain()の括り


    /**
     *  合Ｓ・合Ａ・合４を取得---１科目でも未受験は、含めない
     *
     */
    private String getSqlExamMainKei(String param[],int table_no)
    {
        StringBuffer stb = new StringBuffer();
        try {
                            //合Ｓ
            stb.append("    SELECT SEX,EXAMNO,MARK,'1' AS KEINO, ");
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_MAIN ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("           AND TESTSUB IN('1','2','4') ");
            stb.append("    GROUP BY SEX,EXAMNO,MARK ");
            stb.append("    HAVING COUNT(*) = 3 ");
                            //合Ａ
            stb.append("    UNION ");
            stb.append("    SELECT SEX,EXAMNO,MARK,'2' AS KEINO, ");
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   ( ");
            stb.append("            SELECT SEX,EXAMNO,MARK,TESTSUB, ");
            stb.append("                   SCORE ");
            stb.append("            FROM   EXAM_MAIN ");
            stb.append("            WHERE  SCORE IS NOT NULL ");
            stb.append("                   AND TESTSUB IN('1','2') ");
            stb.append("            UNION ");
            stb.append("            SELECT SEX,EXAMNO,MARK,'0' AS TESTSUB, ");
            stb.append("                   MAX(SCORE) AS SCORE ");
            stb.append("            FROM   EXAM_MAIN ");
            stb.append("            WHERE  SCORE IS NOT NULL ");
            stb.append("                   AND TESTSUB IN('3','4') ");
            stb.append("            GROUP BY SEX,EXAMNO,MARK ");
            stb.append("            ) T1 ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("    GROUP BY SEX,EXAMNO,MARK ");
            stb.append("    HAVING COUNT(*) = 3 ");
                            //合４
            stb.append("    UNION ");
            stb.append("    SELECT SEX,EXAMNO,MARK,'3' AS KEINO, ");
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_MAIN ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("           AND TESTSUB IN('1','2','3','4') ");
            stb.append("    GROUP BY SEX,EXAMNO,MARK ");
            stb.append("    HAVING COUNT(*) = 4 ");
                            //人数---NO002
            stb.append("    UNION ");
            stb.append("    SELECT SEX,EXAMNO,MARK,'4' AS KEINO, ");
            stb.append("           SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_MAIN ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("    GROUP BY SEX,EXAMNO,MARK ");
        } catch( Exception e ){
            log.warn("getSqlExamMainKei error!",e);
        }
        return stb.toString();

    }//getSqlExamMainKei()の括り



}//クラスの括り
