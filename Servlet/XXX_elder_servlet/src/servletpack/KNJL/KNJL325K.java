// kanji=漢字
/*
 * $Id: 7d6d108aa8654faf6daacfc0d9ea42d1af1f52ea $
 *
 * 作成日: 2005/08/10 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３２５＞  入学試験得点分布表
 *
 *  2005/08/10 nakamoto 作成日
 *  2005/08/16 nakamoto 受験者番号'3000'番台は、対象外とするよう修正
 *  2005/08/23 nakamoto 合計・平均は、全教科得点がある受験者を対象とするよう修正(４教科計：４教科、その他：３教科)
 *  2006/10/24 m-yam    附属推薦者出力処理追加。
 * @author nakamoto
 * @version $Id: 7d6d108aa8654faf6daacfc0d9ea42d1af1f52ea $
 */
public class KNJL325K {


    private static final Log log = LogFactory.getLog(KNJL325K.class);

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
            if (param[1].equals("3")) {
                param[2] = param[1];
                param[1] = "1";
            }

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

        svf.VrSetForm("KNJL325_2.frm", 1);
        setInfluenceName(db2, svf, param);

    //  次年度
        try {
            svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" );
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        try {
            svf.VrsOut("SCHOOLDIV"   , (param[5].equals("1")) ? "中学校" : "高等学校" );
        } catch( Exception e ){
            log.warn("SchoolName get error!",e);
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

    //  サブタイトル
        try {
            if (null != param[2]) {
                svf.VrsOut("SUBTITLE"   ,  "(附属推薦者)");
            }
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
            //明細データ
            if( printSubclassScore(db2,svf,param) ){//表２
                printSubclassAverage(db2,svf,param);//表１：各教科
                printTotalAverage(db2,svf,param,1); //表１：４教科
                printTotalScore(db2,svf,param,1);   //表３：４教科
                //後期
                for (int ia = 2; ia < 5; ia++) {
                    printTotalAverage(db2,svf,param,ia);   //表１：社会型・理科型・アラカルト型
                    printTotalScore(db2,svf,param,ia);     //表３：社会型・理科型・アラカルト型
                }
                svf.VrEndPage();
                nonedata = true;
            }
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**明細データ印刷処理(表１：各教科)*/
    private void printSubclassAverage(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
log.debug("SubclassAverage start!");
            db2.query(getSqlSubclassAverage(param));
            ResultSet rs = db2.getResultSet();
log.debug("SubclassAverage end!");

            while( rs.next() ){
                //得点分布
                svf.VrsOut("CLASS1_"+rs.getString("TESTSUBCLASSCD")  , rs.getString("KEI") );//合計
                svf.VrsOut("CLASS2_"+rs.getString("TESTSUBCLASSCD")  , rs.getString("HEI") );//平均
                svf.VrsOut("CLASS3_"+rs.getString("TESTSUBCLASSCD")  , rs.getString("NIN") );//人数
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSubclassAverage read error!",ex);
        }

    }//printSubclassAverage()の括り


    /**明細データ印刷処理(表１：４教科・社会型・理科型・アラカルト型)*/
    private void printTotalAverage(DB2UDB db2,Vrw32alp svf,String param[], int ala_flg)
    {
        try {
log.debug("TotalAverage start"+ala_flg);
            //教科コード
            param[6] = (ala_flg == 1) ? "('1','2','3','4')" : 
                       (ala_flg == 2) ? "('1','2','3')" : 
                       (ala_flg == 3) ? "('1','2','4')" : "('1','2')" ;
            db2.query(getSqlTotalAverage(param, ala_flg));
            ResultSet rs = db2.getResultSet();
log.debug("TotalAverage end"+ala_flg);

            while( rs.next() ){
                //合計列
                svf.VrsOut("TOTAL1_"+String.valueOf(ala_flg)  , rs.getString("KEI_G") );  //合計行
                svf.VrsOut("TOTAL2_"+String.valueOf(ala_flg)  , rs.getString("HEI_G") );  //平均行
                svf.VrsOut("TOTAL3_"+String.valueOf(ala_flg)  , rs.getString("NIN") );    //人数行
                //平均列
                svf.VrsOut("AVERAGE1_"+String.valueOf(ala_flg), rs.getString("KEI_H") );  //合計行
                svf.VrsOut("AVERAGE2_"+String.valueOf(ala_flg), rs.getString("HEI_H") );  //平均行
                svf.VrsOut("AVERAGE3_"+String.valueOf(ala_flg), rs.getString("NIN") );    //人数行
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printTotalAverage read error!",ex);
        }

    }//printTotalAverage()の括り


    /**明細データ印刷処理(表２：各教科)*/
    private boolean printSubclassScore(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
log.debug("SubclassScore start!");
            db2.query(getSqlSubclassScore(param));
            ResultSet rs = db2.getResultSet();
log.debug("SubclassScore end!");

            String len = "0";
            int gyo_s = 0;
            while( rs.next() ){
                //教科
                len = rs.getString("TESTSUBCLASSCD");
                gyo_s = ( len.equals("3") || len.equals("4") ) ? 9 : 1 ;
                //得点分布
                for (int gyo = gyo_s; gyo < 27; gyo++) 
                    svf.VrsOutn("MEMBER1_"+len    ,gyo    , rs.getString("CNT"+String.valueOf(gyo)) );

                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSubclassScore read error!",ex);
        }
        return nonedata;

    }//printSubclassScore()の括り


    /**明細データ印刷処理(表３：４教科・社会型・理科型・アラカルト型)*/
    private void printTotalScore(DB2UDB db2,Vrw32alp svf,String param[], int ala_flg)
    {
        int gyo_s = 0;
        try {
            //教科コード
            param[6] = (ala_flg == 1) ? "('1','2','3','4')" : 
                       (ala_flg == 2) ? "('1','2','3')" : 
                       (ala_flg == 3) ? "('1','2','4')" : "('1','2')" ;
            db2.query(getSqlTotalScore(param, ala_flg));
            ResultSet rs = db2.getResultSet();

            int cnt = 0;
            int rui = 0;
            int gyo2 = 0;
            String len = "0";
            gyo_s = ( 1 < ala_flg ) ? 16 : 1 ;
            while( rs.next() ){
                //得点分布
                for (int gyo = gyo_s; gyo < 81; gyo++) {
                    cnt = rs.getInt("CNT"+String.valueOf(gyo));//人数
                    rui = rui + cnt;//累計
                    gyo2 = (gyo < 41) ? gyo : gyo-40 ;
                    len = (gyo < 41) ? "_1" : "_2" ;

                    svf.VrsOutn("MEMBER"+String.valueOf(ala_flg+1)+len       , gyo2 , String.valueOf(cnt) );
                    svf.VrsOutn("TOTAL_MEMBER"+String.valueOf(ala_flg+1)+len , gyo2 , String.valueOf(rui) );
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printTotalScore read error!",ex);
        }

    }//printTotalScore()の括り


    /**
     *  (表１)各教科の合計・平均・人数を取得
     *
     */
    private String getSqlSubclassAverage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者得点データ
            stb.append("WITH EXAM_SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO, T1.TESTSUBCLASSCD, T1.A_SCORE AS SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT T1 ");
            stb.append("    WHERE  T1.ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           T1.TESTDIV='"+param[1]+"' ");
            if (null == param[2]) {
                stb.append("           AND (T1.EXAMNO < '3000' OR '4000' <= T1.EXAMNO) ");//2005.08.16
            } else {
                stb.append("           AND T1.EXAMNO BETWEEN '3000' AND '4000' ");     //2006.10.24
            }
            stb.append("           AND EXISTS( ");
            stb.append("                SELECT ");
            stb.append("                    'x' ");
            stb.append("                FROM ");
            stb.append("                    ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("                WHERE ");
            stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
            stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
            stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
            if (!"9".equals(param[9])) {
                stb.append("                    AND E1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("           ) ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT TESTSUBCLASSCD, ");
            stb.append("       COUNT(*) AS NIN, ");
            stb.append("       SUM(SCORE) AS KEI, ");
            stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI ");
            stb.append("FROM   EXAM_SCORE ");
            stb.append("WHERE  SCORE IS NOT NULL ");
            stb.append("GROUP BY TESTSUBCLASSCD ");
            stb.append("ORDER BY TESTSUBCLASSCD ");
        } catch( Exception e ){
            log.warn("getSqlSubclassAverage error!",e);
        }
        return stb.toString();

    }//getSqlSubclassAverage()の括り


    /**
     *  (表１)４教科・社会型・理科型・アラカルト型の合計・平均・人数を取得
     *
     */
    private String getSqlTotalAverage(String param[], int ala_flg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者得点データ
            stb.append("WITH EXAM_SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO, T1.TESTSUBCLASSCD, T1.A_SCORE AS SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT T1 ");
            stb.append("    WHERE  T1.ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           T1.TESTDIV='"+param[1]+"' AND ");
            stb.append("           T1.TESTSUBCLASSCD IN "+param[6]+" ");
            if (null == param[2]) {
                stb.append("           AND (T1.EXAMNO < '3000' OR '4000' <= T1.EXAMNO) ");//2005.08.16
            } else {
                stb.append("           AND T1.EXAMNO BETWEEN '3000' AND '4000' ");     //2005.08.16
            }
            stb.append("           AND EXISTS( ");
            stb.append("                SELECT ");
            stb.append("                    'x' ");
            stb.append("                FROM ");
            stb.append("                    ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("                WHERE ");
            stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
            stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
            stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
            if (!"9".equals(param[9])) {
                stb.append("                    AND E1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("           ) ");
            //  アラカルト型
            if (ala_flg == 4) {
                stb.append("UNION ");
                stb.append("SELECT T1.EXAMNO, '0' AS TESTSUBCLASSCD, MAX(T1.A_SCORE) AS SCORE ");
                stb.append("FROM   ENTEXAM_SCORE_DAT T1 ");
                stb.append("WHERE  T1.ENTEXAMYEAR='"+param[0]+"' AND ");
                stb.append("       T1.TESTDIV='"+param[1]+"' AND ");
                stb.append("       T1.TESTSUBCLASSCD IN ('3','4') ");
                if (null == param[2]) {
                    stb.append("           AND (T1.EXAMNO < '3000' OR '4000' <= T1.EXAMNO) ");//2005.08.16
                } else {
                    stb.append("           AND T1.EXAMNO BETWEEN '3000' AND '4000' ");     //2005.08.16
                }
                stb.append("           AND EXISTS( ");
                stb.append("                SELECT ");
                stb.append("                    'x' ");
                stb.append("                FROM ");
                stb.append("                    ENTEXAM_APPLICANTBASE_DAT E1 ");
                stb.append("                WHERE ");
                stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
                stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
                stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
                if (!"9".equals(param[9])) {
                    stb.append("                    AND E1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
                }
                stb.append("           ) ");
                stb.append("GROUP BY T1.EXAMNO ");
            }
            stb.append("    ) ");
            //得点がNULLの受験者---2005.08.23
            String testsubcnt = (ala_flg == 1) ? "4" : "3" ;
            stb.append(",EXAM_SCORE_NULL AS ( ");
            stb.append("    SELECT EXAMNO ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    HAVING COUNT(*) < "+testsubcnt+" ");
            stb.append("    ) ");
            //教科毎の人数・合計
            stb.append(",EXAM_SCORE_TESTSUB AS ( ");
            stb.append("    SELECT TESTSUBCLASSCD, ");
            stb.append("           COUNT(*) AS NIN, ");
            stb.append("           SUM(SCORE) AS KEI ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  SCORE IS NOT NULL AND ");
            stb.append("           EXAMNO NOT IN (SELECT EXAMNO FROM EXAM_SCORE_NULL) ");
            stb.append("    GROUP BY TESTSUBCLASSCD ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT NIN, ");
            stb.append("       SUM(KEI) AS KEI_G, ");
            stb.append("       DECIMAL(ROUND(FLOAT(SUM(KEI))/COUNT(*)*10,0)/10,9,1) AS KEI_H, ");
            stb.append("       DECIMAL(ROUND(FLOAT(SUM(KEI))/NIN*10,0)/10,5,1) AS HEI_G, ");
            stb.append("       DECIMAL(ROUND(FLOAT(SUM(KEI))/(NIN*COUNT(*))*10,0)/10,5,1) AS HEI_H ");
            stb.append("FROM   EXAM_SCORE_TESTSUB ");
            stb.append("GROUP BY NIN ");
        } catch( Exception e ){
            log.warn("getSqlTotalAverage error!",e);
        }
        return stb.toString();

    }//getSqlTotalAverage()の括り


    /**
     *  (表２)各教科の得点分布を取得
     *
     */
    private String getSqlSubclassScore(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者得点データ
            stb.append("WITH EXAM_SCORE AS ( ");
            stb.append("    SELECT T1.TESTDIV, T1.EXAMNO, T1.TESTSUBCLASSCD, T1.A_SCORE AS SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT T1 ");
            stb.append("    WHERE  T1.ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           T1.TESTDIV='"+param[1]+"' AND ");
            stb.append("           T1.A_SCORE IS NOT NULL ");
            if (null == param[2]) {
                stb.append("           AND (T1.EXAMNO < '3000' OR '4000' <= T1.EXAMNO) ");//2005.08.16
            } else {
                stb.append("           AND T1.EXAMNO BETWEEN '3000' AND '4000' ");     //2005.08.16
            }
            stb.append("           AND EXISTS( ");
            stb.append("                SELECT ");
            stb.append("                    'x' ");
            stb.append("                FROM ");
            stb.append("                    ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("                WHERE ");
            stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
            stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
            stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
            if (!"9".equals(param[9])) {
                stb.append("                    AND E1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("           ) ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT TESTSUBCLASSCD, ");
            stb.append("       SUM(CASE WHEN SCORE = 120 THEN 1 ELSE 0 END) AS CNT1, ");
            stb.append("       SUM(CASE WHEN 115 <= SCORE AND SCORE <= 119 THEN 1 ELSE 0 END) AS CNT2, ");
            stb.append("       SUM(CASE WHEN 110 <= SCORE AND SCORE <= 114 THEN 1 ELSE 0 END) AS CNT3, ");
            stb.append("       SUM(CASE WHEN 105 <= SCORE AND SCORE <= 109 THEN 1 ELSE 0 END) AS CNT4, ");
            stb.append("       SUM(CASE WHEN 100 <= SCORE AND SCORE <= 104 THEN 1 ELSE 0 END) AS CNT5, ");
            stb.append("       SUM(CASE WHEN  95 <= SCORE AND SCORE <=  99 THEN 1 ELSE 0 END) AS CNT6, ");
            stb.append("       SUM(CASE WHEN  90 <= SCORE AND SCORE <=  94 THEN 1 ELSE 0 END) AS CNT7, ");
            stb.append("       SUM(CASE WHEN  85 <= SCORE AND SCORE <=  89 THEN 1 ELSE 0 END) AS CNT8, ");
            stb.append("       SUM(CASE WHEN  80 <= SCORE AND SCORE <=  84 THEN 1 ELSE 0 END) AS CNT9, ");
            stb.append("       SUM(CASE WHEN  75 <= SCORE AND SCORE <=  79 THEN 1 ELSE 0 END) AS CNT10, ");
            stb.append("       SUM(CASE WHEN  70 <= SCORE AND SCORE <=  74 THEN 1 ELSE 0 END) AS CNT11, ");
            stb.append("       SUM(CASE WHEN  65 <= SCORE AND SCORE <=  69 THEN 1 ELSE 0 END) AS CNT12, ");
            stb.append("       SUM(CASE WHEN  60 <= SCORE AND SCORE <=  64 THEN 1 ELSE 0 END) AS CNT13, ");
            stb.append("       SUM(CASE WHEN  55 <= SCORE AND SCORE <=  59 THEN 1 ELSE 0 END) AS CNT14, ");
            stb.append("       SUM(CASE WHEN  50 <= SCORE AND SCORE <=  54 THEN 1 ELSE 0 END) AS CNT15, ");
            stb.append("       SUM(CASE WHEN  45 <= SCORE AND SCORE <=  49 THEN 1 ELSE 0 END) AS CNT16, ");
            stb.append("       SUM(CASE WHEN  40 <= SCORE AND SCORE <=  44 THEN 1 ELSE 0 END) AS CNT17, ");
            stb.append("       SUM(CASE WHEN  35 <= SCORE AND SCORE <=  39 THEN 1 ELSE 0 END) AS CNT18, ");
            stb.append("       SUM(CASE WHEN  30 <= SCORE AND SCORE <=  34 THEN 1 ELSE 0 END) AS CNT19, ");
            stb.append("       SUM(CASE WHEN  25 <= SCORE AND SCORE <=  29 THEN 1 ELSE 0 END) AS CNT20, ");
            stb.append("       SUM(CASE WHEN  20 <= SCORE AND SCORE <=  24 THEN 1 ELSE 0 END) AS CNT21, ");
            stb.append("       SUM(CASE WHEN  15 <= SCORE AND SCORE <=  19 THEN 1 ELSE 0 END) AS CNT22, ");
            stb.append("       SUM(CASE WHEN  10 <= SCORE AND SCORE <=  14 THEN 1 ELSE 0 END) AS CNT23, ");
            stb.append("       SUM(CASE WHEN   5 <= SCORE AND SCORE <=   9 THEN 1 ELSE 0 END) AS CNT24, ");
            stb.append("       SUM(CASE WHEN   1 <= SCORE AND SCORE <=   4 THEN 1 ELSE 0 END) AS CNT25, ");
            stb.append("       SUM(CASE WHEN SCORE = 0 THEN 1 ELSE 0 END) AS CNT26 ");
            stb.append("FROM   EXAM_SCORE ");
            stb.append("GROUP BY TESTSUBCLASSCD ");
        } catch( Exception e ){
            log.warn("getSqlSubclassScore error!",e);
        }
        return stb.toString();

    }//getSqlSubclassScore()の括り


    /**
     *  (表３)総得点の得点分布を取得
     *
     */
    private String getSqlTotalScore(String param[], int ala_flg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者得点データ
            stb.append("WITH EXAM_SCORE AS ( ");
            stb.append("    SELECT T1.EXAMNO, T1.TESTSUBCLASSCD, T1.A_SCORE AS SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT T1 ");
            stb.append("    WHERE  T1.ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           T1.TESTDIV='"+param[1]+"' AND ");
            stb.append("           T1.TESTSUBCLASSCD IN "+param[6]+" ");
            if (null == param[2]) {
                stb.append("           AND (T1.EXAMNO < '3000' OR '4000' <= T1.EXAMNO) ");//2005.08.16
            } else {
                stb.append("           AND T1.EXAMNO BETWEEN '3000' AND '4000' ");     //2005.08.16
            }
            stb.append("           AND EXISTS( ");
            stb.append("                SELECT ");
            stb.append("                    'x' ");
            stb.append("                FROM ");
            stb.append("                    ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("                WHERE ");
            stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
            stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
            stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
            if (!"9".equals(param[9])) {
                stb.append("                    AND E1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("           ) ");
            //  アラカルト型
            if (ala_flg == 4) {
                stb.append("UNION ");
                stb.append("SELECT T1.EXAMNO, '0' AS TESTSUBCLASSCD, MAX(T1.A_SCORE) AS SCORE ");
                stb.append("FROM   ENTEXAM_SCORE_DAT T1 ");
                stb.append("WHERE  T1.ENTEXAMYEAR='"+param[0]+"' AND ");
                stb.append("       T1.TESTDIV='"+param[1]+"' AND ");
                stb.append("       T1.TESTSUBCLASSCD IN ('3','4') ");
                if (null == param[2]) {
                    stb.append("           AND (T1.EXAMNO < '3000' OR '4000' <= T1.EXAMNO) ");//2005.08.16
                } else {
                    stb.append("           AND T1.EXAMNO BETWEEN '3000' AND '4000' ");     //2005.08.16
                }
                stb.append("           AND EXISTS( ");
                stb.append("                SELECT ");
                stb.append("                    'x' ");
                stb.append("                FROM ");
                stb.append("                    ENTEXAM_APPLICANTBASE_DAT E1 ");
                stb.append("                WHERE ");
                stb.append("                    T1.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
                stb.append("                    AND T1.TESTDIV = E1.TESTDIV ");
                stb.append("                    AND T1.EXAMNO = E1.EXAMNO ");
                if (!"9".equals(param[9])) {
                    stb.append("                    AND E1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
                }
                stb.append("           ) ");
                stb.append("GROUP BY T1.EXAMNO ");
            }
            stb.append("    ) ");
            //得点がNULLの受験者---2005.08.23
            String testsubcnt = (ala_flg == 1) ? "4" : "3" ;
            stb.append(",EXAM_SCORE_NULL AS ( ");
            stb.append("    SELECT EXAMNO ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  SCORE IS NOT NULL ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    HAVING COUNT(*) < "+testsubcnt+" ");
            stb.append("    ) ");
            //各受験者の合計得点
            stb.append(",EXAM_SCORE_SUM AS ( ");
            stb.append("    SELECT EXAMNO, SUM(SCORE) AS SCORE ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    WHERE  SCORE IS NOT NULL AND ");
            stb.append("           EXAMNO NOT IN (SELECT EXAMNO FROM EXAM_SCORE_NULL) ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT SUM(CASE WHEN 395 <= SCORE AND SCORE <= 400 THEN 1 ELSE 0 END) AS CNT1, ");
            stb.append("       SUM(CASE WHEN 390 <= SCORE AND SCORE <= 394 THEN 1 ELSE 0 END) AS CNT2, ");
            stb.append("       SUM(CASE WHEN 385 <= SCORE AND SCORE <= 389 THEN 1 ELSE 0 END) AS CNT3, ");
            stb.append("       SUM(CASE WHEN 380 <= SCORE AND SCORE <= 384 THEN 1 ELSE 0 END) AS CNT4, ");
            stb.append("       SUM(CASE WHEN 375 <= SCORE AND SCORE <= 379 THEN 1 ELSE 0 END) AS CNT5, ");
            stb.append("       SUM(CASE WHEN 370 <= SCORE AND SCORE <= 374 THEN 1 ELSE 0 END) AS CNT6, ");
            stb.append("       SUM(CASE WHEN 365 <= SCORE AND SCORE <= 369 THEN 1 ELSE 0 END) AS CNT7, ");
            stb.append("       SUM(CASE WHEN 360 <= SCORE AND SCORE <= 364 THEN 1 ELSE 0 END) AS CNT8, ");
            stb.append("       SUM(CASE WHEN 355 <= SCORE AND SCORE <= 359 THEN 1 ELSE 0 END) AS CNT9, ");
            stb.append("       SUM(CASE WHEN 350 <= SCORE AND SCORE <= 354 THEN 1 ELSE 0 END) AS CNT10, ");
            stb.append("       SUM(CASE WHEN 345 <= SCORE AND SCORE <= 349 THEN 1 ELSE 0 END) AS CNT11, ");
            stb.append("       SUM(CASE WHEN 340 <= SCORE AND SCORE <= 344 THEN 1 ELSE 0 END) AS CNT12, ");
            stb.append("       SUM(CASE WHEN 335 <= SCORE AND SCORE <= 339 THEN 1 ELSE 0 END) AS CNT13, ");
            stb.append("       SUM(CASE WHEN 330 <= SCORE AND SCORE <= 334 THEN 1 ELSE 0 END) AS CNT14, ");
            stb.append("       SUM(CASE WHEN 325 <= SCORE AND SCORE <= 329 THEN 1 ELSE 0 END) AS CNT15, ");
            stb.append("       SUM(CASE WHEN 320 <= SCORE AND SCORE <= 324 THEN 1 ELSE 0 END) AS CNT16, ");
            stb.append("       SUM(CASE WHEN 315 <= SCORE AND SCORE <= 319 THEN 1 ELSE 0 END) AS CNT17, ");
            stb.append("       SUM(CASE WHEN 310 <= SCORE AND SCORE <= 314 THEN 1 ELSE 0 END) AS CNT18, ");
            stb.append("       SUM(CASE WHEN 305 <= SCORE AND SCORE <= 309 THEN 1 ELSE 0 END) AS CNT19, ");
            stb.append("       SUM(CASE WHEN 300 <= SCORE AND SCORE <= 304 THEN 1 ELSE 0 END) AS CNT20, ");
            stb.append("       SUM(CASE WHEN 295 <= SCORE AND SCORE <= 299 THEN 1 ELSE 0 END) AS CNT21, ");
            stb.append("       SUM(CASE WHEN 290 <= SCORE AND SCORE <= 294 THEN 1 ELSE 0 END) AS CNT22, ");
            stb.append("       SUM(CASE WHEN 285 <= SCORE AND SCORE <= 289 THEN 1 ELSE 0 END) AS CNT23, ");
            stb.append("       SUM(CASE WHEN 280 <= SCORE AND SCORE <= 284 THEN 1 ELSE 0 END) AS CNT24, ");
            stb.append("       SUM(CASE WHEN 275 <= SCORE AND SCORE <= 279 THEN 1 ELSE 0 END) AS CNT25, ");
            stb.append("       SUM(CASE WHEN 270 <= SCORE AND SCORE <= 274 THEN 1 ELSE 0 END) AS CNT26, ");
            stb.append("       SUM(CASE WHEN 265 <= SCORE AND SCORE <= 269 THEN 1 ELSE 0 END) AS CNT27, ");
            stb.append("       SUM(CASE WHEN 260 <= SCORE AND SCORE <= 264 THEN 1 ELSE 0 END) AS CNT28, ");
            stb.append("       SUM(CASE WHEN 255 <= SCORE AND SCORE <= 259 THEN 1 ELSE 0 END) AS CNT29, ");
            stb.append("       SUM(CASE WHEN 250 <= SCORE AND SCORE <= 254 THEN 1 ELSE 0 END) AS CNT30, ");
            stb.append("       SUM(CASE WHEN 245 <= SCORE AND SCORE <= 249 THEN 1 ELSE 0 END) AS CNT31, ");
            stb.append("       SUM(CASE WHEN 240 <= SCORE AND SCORE <= 244 THEN 1 ELSE 0 END) AS CNT32, ");
            stb.append("       SUM(CASE WHEN 235 <= SCORE AND SCORE <= 239 THEN 1 ELSE 0 END) AS CNT33, ");
            stb.append("       SUM(CASE WHEN 230 <= SCORE AND SCORE <= 234 THEN 1 ELSE 0 END) AS CNT34, ");
            stb.append("       SUM(CASE WHEN 225 <= SCORE AND SCORE <= 229 THEN 1 ELSE 0 END) AS CNT35, ");
            stb.append("       SUM(CASE WHEN 220 <= SCORE AND SCORE <= 224 THEN 1 ELSE 0 END) AS CNT36, ");
            stb.append("       SUM(CASE WHEN 215 <= SCORE AND SCORE <= 219 THEN 1 ELSE 0 END) AS CNT37, ");
            stb.append("       SUM(CASE WHEN 210 <= SCORE AND SCORE <= 214 THEN 1 ELSE 0 END) AS CNT38, ");
            stb.append("       SUM(CASE WHEN 205 <= SCORE AND SCORE <= 209 THEN 1 ELSE 0 END) AS CNT39, ");
            stb.append("       SUM(CASE WHEN 200 <= SCORE AND SCORE <= 204 THEN 1 ELSE 0 END) AS CNT40, ");

            stb.append("       SUM(CASE WHEN 195 <= SCORE AND SCORE <= 199 THEN 1 ELSE 0 END) AS CNT41, ");
            stb.append("       SUM(CASE WHEN 190 <= SCORE AND SCORE <= 194 THEN 1 ELSE 0 END) AS CNT42, ");
            stb.append("       SUM(CASE WHEN 185 <= SCORE AND SCORE <= 189 THEN 1 ELSE 0 END) AS CNT43, ");
            stb.append("       SUM(CASE WHEN 180 <= SCORE AND SCORE <= 184 THEN 1 ELSE 0 END) AS CNT44, ");
            stb.append("       SUM(CASE WHEN 175 <= SCORE AND SCORE <= 179 THEN 1 ELSE 0 END) AS CNT45, ");
            stb.append("       SUM(CASE WHEN 170 <= SCORE AND SCORE <= 174 THEN 1 ELSE 0 END) AS CNT46, ");
            stb.append("       SUM(CASE WHEN 165 <= SCORE AND SCORE <= 169 THEN 1 ELSE 0 END) AS CNT47, ");
            stb.append("       SUM(CASE WHEN 160 <= SCORE AND SCORE <= 164 THEN 1 ELSE 0 END) AS CNT48, ");
            stb.append("       SUM(CASE WHEN 155 <= SCORE AND SCORE <= 159 THEN 1 ELSE 0 END) AS CNT49, ");
            stb.append("       SUM(CASE WHEN 150 <= SCORE AND SCORE <= 154 THEN 1 ELSE 0 END) AS CNT50, ");
            stb.append("       SUM(CASE WHEN 145 <= SCORE AND SCORE <= 149 THEN 1 ELSE 0 END) AS CNT51, ");
            stb.append("       SUM(CASE WHEN 140 <= SCORE AND SCORE <= 144 THEN 1 ELSE 0 END) AS CNT52, ");
            stb.append("       SUM(CASE WHEN 135 <= SCORE AND SCORE <= 139 THEN 1 ELSE 0 END) AS CNT53, ");
            stb.append("       SUM(CASE WHEN 130 <= SCORE AND SCORE <= 134 THEN 1 ELSE 0 END) AS CNT54, ");
            stb.append("       SUM(CASE WHEN 125 <= SCORE AND SCORE <= 129 THEN 1 ELSE 0 END) AS CNT55, ");
            stb.append("       SUM(CASE WHEN 120 <= SCORE AND SCORE <= 124 THEN 1 ELSE 0 END) AS CNT56, ");
            stb.append("       SUM(CASE WHEN 115 <= SCORE AND SCORE <= 119 THEN 1 ELSE 0 END) AS CNT57, ");
            stb.append("       SUM(CASE WHEN 110 <= SCORE AND SCORE <= 114 THEN 1 ELSE 0 END) AS CNT58, ");
            stb.append("       SUM(CASE WHEN 105 <= SCORE AND SCORE <= 109 THEN 1 ELSE 0 END) AS CNT59, ");
            stb.append("       SUM(CASE WHEN 100 <= SCORE AND SCORE <= 104 THEN 1 ELSE 0 END) AS CNT60, ");
            stb.append("       SUM(CASE WHEN  95 <= SCORE AND SCORE <=  99 THEN 1 ELSE 0 END) AS CNT61, ");
            stb.append("       SUM(CASE WHEN  90 <= SCORE AND SCORE <=  94 THEN 1 ELSE 0 END) AS CNT62, ");
            stb.append("       SUM(CASE WHEN  85 <= SCORE AND SCORE <=  89 THEN 1 ELSE 0 END) AS CNT63, ");
            stb.append("       SUM(CASE WHEN  80 <= SCORE AND SCORE <=  84 THEN 1 ELSE 0 END) AS CNT64, ");
            stb.append("       SUM(CASE WHEN  75 <= SCORE AND SCORE <=  79 THEN 1 ELSE 0 END) AS CNT65, ");
            stb.append("       SUM(CASE WHEN  70 <= SCORE AND SCORE <=  74 THEN 1 ELSE 0 END) AS CNT66, ");
            stb.append("       SUM(CASE WHEN  65 <= SCORE AND SCORE <=  69 THEN 1 ELSE 0 END) AS CNT67, ");
            stb.append("       SUM(CASE WHEN  60 <= SCORE AND SCORE <=  64 THEN 1 ELSE 0 END) AS CNT68, ");
            stb.append("       SUM(CASE WHEN  55 <= SCORE AND SCORE <=  59 THEN 1 ELSE 0 END) AS CNT69, ");
            stb.append("       SUM(CASE WHEN  50 <= SCORE AND SCORE <=  54 THEN 1 ELSE 0 END) AS CNT70, ");
            stb.append("       SUM(CASE WHEN  45 <= SCORE AND SCORE <=  49 THEN 1 ELSE 0 END) AS CNT71, ");
            stb.append("       SUM(CASE WHEN  40 <= SCORE AND SCORE <=  44 THEN 1 ELSE 0 END) AS CNT72, ");
            stb.append("       SUM(CASE WHEN  35 <= SCORE AND SCORE <=  39 THEN 1 ELSE 0 END) AS CNT73, ");
            stb.append("       SUM(CASE WHEN  30 <= SCORE AND SCORE <=  34 THEN 1 ELSE 0 END) AS CNT74, ");
            stb.append("       SUM(CASE WHEN  25 <= SCORE AND SCORE <=  29 THEN 1 ELSE 0 END) AS CNT75, ");
            stb.append("       SUM(CASE WHEN  20 <= SCORE AND SCORE <=  24 THEN 1 ELSE 0 END) AS CNT76, ");
            stb.append("       SUM(CASE WHEN  15 <= SCORE AND SCORE <=  19 THEN 1 ELSE 0 END) AS CNT77, ");
            stb.append("       SUM(CASE WHEN  10 <= SCORE AND SCORE <=  14 THEN 1 ELSE 0 END) AS CNT78, ");
            stb.append("       SUM(CASE WHEN   5 <= SCORE AND SCORE <=   9 THEN 1 ELSE 0 END) AS CNT79, ");
            stb.append("       SUM(CASE WHEN   0 <= SCORE AND SCORE <=   4 THEN 1 ELSE 0 END) AS CNT80 ");
            stb.append("FROM   EXAM_SCORE_SUM ");
        } catch( Exception e ){
            log.warn("getSqlTotalScore error!",e);
        }
        return stb.toString();

    }//getSqlTotalScore()の括り



}//クラスの括り
