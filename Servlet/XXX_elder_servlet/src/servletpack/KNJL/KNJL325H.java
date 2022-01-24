package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *                    ＜ＫＮＪＬ３２５Ｈ＞  入試統計資料
 *
 *    2007/11/22 RTS 作成日
 **/

public class KNJL325H {

    private static final Log log = LogFactory.getLog(KNJL325H.class);

    DecimalFormat df = new DecimalFormat("0.0");
    String param[];
    //*-------------------------------------------------* 
    // 試験科目データテーブルの対象年度、入試制度に     *
    // 紐つく名称マスタデータ(L009)を格納。             *
    // 構成:キー⇒科目見出しエリア出力順,値=NAMECD2     *
    //*-------------------------------------------------* 
    HashMap hkamoku = new HashMap();

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        param = new String[9];

    //パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");       //年度
            param[1] = request.getParameter("APDIV");      //入試制度
            param[2] = request.getParameter("TESTDV");     //入試区分
            if( request.getParameter("CHECK1") != null ){
                param[7] = request.getParameter("CHECK1"); //前回までの合格者対象者に含める
            } else {
                param[7] = "";
            }
            if( request.getParameter("CHECK2") != null ){
                param[8] = request.getParameter("CHECK2");  //最低点の表示なし
            } else {
                param[8] = "";  //最低点の表示なし
            }
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //svf設定
        int ret = svf.VrInit();                                     //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

    //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //ＳＶＦ作成処理
        PreparedStatement ps  = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        boolean nonedata = false;  //該当データなしフラグ
        setHeader(db2,svf);
        for(int i=0 ; i<5 ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps  = db2.prepareStatement(preStat());   //入試区分preparestatement
            ps1 = db2.prepareStatement(preStat1());  //速報データ（人数）preparestatement
            ps2 = db2.prepareStatement(preStat2());  //名称preparestatement
            ps3 = db2.prepareStatement(preStat3());  //名称preparestatement
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if (setSvfMain(db2,svf,ps,ps1,ps2,ps3)) nonedata = true;    //帳票出力のメソッド

    //該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //終了処理
        ret = svf.VrQuit();
        preStatClose(ps,ps1,ps2,ps3); //preparestatementを閉じる
        db2.commit();
        db2.close();      //DBを閉じる
        outstrm.close();  //ストリームを閉じる 

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        String sNENDO = convZenkakuToHankaku(param[0]);
        param[3] = sNENDO + "年度";

    //作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            
            param[4] = fomatSakuseiDate(returnval.val3, "yyyy-MM-dd");
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;
    }

    /**
     *  svf print 印刷処理
     *    入試区分が指定されていれば( => param[2] !== "0" )１回の処理
     *    入試区分が複数の場合は全ての入試区分を舐める
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3
    ) {
        boolean nonedata = false;

        if( ! param[2].equals("0") ){
            setTestNameDate(db2,svf,ps2,ps3,param[2]);                       //名称メソッド
            for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
            if (setSvfout1(db2,svf,ps1,param[2])) nonedata = true;          //速報データ（人数）出力のメソッド
            if (setSvfout2(db2,svf,param[2],1)) nonedata = true;            //速報データ（得点）出力のメソッド：合格者
            if (setSvfout2(db2,svf,param[2],2)) nonedata = true;            //速報データ（得点）出力のメソッド：受験者
            return nonedata;
        }

        try {
            ResultSet rs = ps.executeQuery();

            while( rs.next() ){
                setTestNameDate(db2,svf,ps2,ps3,rs.getString("TESTDIV"));                 //名称メソッド
                for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
                if (setSvfout1(db2,svf,ps1, rs.getString("TESTDIV"))) nonedata = true;   //速報データ（人数）のメソッド
                if (setSvfout2(db2,svf,rs.getString("TESTDIV"),1)) nonedata = true;      //速報データ（得点）のメソッド：合格者
                if (setSvfout2(db2,svf,rs.getString("TESTDIV"),2)) nonedata = true;      //速報データ（得点）のメソッド：受験者
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!");
        }
        return nonedata;
    }



    /**名称をセット**/
    private void setTestNameDate(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps2,
        PreparedStatement ps3,
        String test_div
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        //入試区分名称を設定
        try {
            int p = 0;
            if(param[1].equals("1")){
                ps2.setString( ++p, test_div );
            } else {
                ps2.setString( ++p, param[1] );
            }
            ResultSet rs = ps2.executeQuery();

            while( rs.next() ){
                param[5] = rs.getString("TEST_NAME");                                    //入試区分
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setTestName set error!");
        }

        //入試日付を設定
        try {
            int p = 0;
            if(param[1].equals("1")){
                ps3.setString( ++p, test_div );
            } else {
                ps3.setString( ++p, "1" );
            }
            ResultSet rs = ps3.executeQuery();

            while( rs.next() ){
                param[6] = fomatSakuseiDate(rs.getString("TEST_DATE"), "yyyy/MM/dd");    //入試日付
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setTestDate set error!");
        }
    }



    /**帳票出力（速報データ（人数）をセット）**/
    private boolean setSvfout1(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        String test_div
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL325H_1.frm", 1);    //セットフォーム
        try {
            setTitle(svf);        //見出しメソッド

            int p = 0;
            ps1.setString( ++p, test_div );
            ps1.setString( ++p, test_div );
            ps1.setString( ++p, test_div );
            if( !param[7].equals("1") ){
                ps1.setString( ++p, test_div ); //前回までの合格者を対象に含めない場合
            }
            ResultSet rs = ps1.executeQuery();

            int gyo = 0;    //行

            while( rs.next() ){
                //明細
                if (rs.getString("TYPE_CNT").equals("A")) gyo = 1;  //志願者開始行
                if (rs.getString("TYPE_CNT").equals("B")) gyo = 1;  //受験者開始行
                if (rs.getString("TYPE_CNT").equals("C")) gyo = 1;  //合格者開始行
                for (int i=1; i<4; i++) {
                    //志願者
                    if (rs.getString("TYPE_CNT").equals("A")){
                        ret = svf.VrsOutn("APPLICANT" ,gyo ,rs.getString("TOTAL" +String.valueOf(i)));    //計
                    }
                    //受験者
                    if (rs.getString("TYPE_CNT").equals("B")){
                        ret = svf.VrsOutn("EXAM"      ,gyo ,rs.getString("TOTAL" +String.valueOf(i)));    //計
                    }
                    //合格者
                    if (rs.getString("TYPE_CNT").equals("C")){
                        ret = svf.VrsOutn("PASS"      ,gyo ,rs.getString("TOTAL" +String.valueOf(i)));    //計
                    }
                    gyo++;
                }
                nonedata = true;
            }
            //出力
            if (nonedata) ret = svf.VrEndPage();
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout1 set error!");
        }
        return nonedata;
    }



    /**帳票出力（速報データ（得点）をセット）**/
    private boolean setSvfout2(
        DB2UDB db2,
        Vrw32alp svf,
        String test_div,
        int juken_flg
    ) {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        boolean nonedata = false;
        int ret = 0;
        int icol_max = 1;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL325H_2.frm", 1);    //セットフォーム
        try {
            ret = svf.VrsOut("ITEM"    ,(juken_flg == 1) ? "合格者" : "受験者");     //合格者・受験者
            setTitle(svf);        //見出しメソッド
            //指示画面より指定された試験科目の取得
            String retsql = getSubClass();
            //指示画面より指定された試験科目の設定
            setSubClass(db2, svf, retsql);

            //*----------------------------*
            //*  各科目計欄の編集・出力    *
            //*----------------------------*
            for(int j=0;j<hkamoku.size();j++){
                //『最高点、最低点、平均点』を設定
                //平均点(男子)取得
                ps1 = db2.prepareStatement( getKekkahyo(test_div, (String)hkamoku.get(String.valueOf(j+1)), "1", juken_flg) );
                //平均点(女子)取得
                ps2 = db2.prepareStatement( getKekkahyo(test_div, (String)hkamoku.get(String.valueOf(j+1)), "2", juken_flg) );
                //平均点(合計)取得
                ps3 = db2.prepareStatement( getKekkahyo(test_div, (String)hkamoku.get(String.valueOf(j+1)), "", juken_flg) );
                nonedata = setkekkahyo(db2, svf, ps1, ps2, ps3, j+1 );
                ++icol_max;
            }
            //入試制度が高校一般の場合、加算点エリアの出力を行う
            if(param[1].equals("2")){
                ps1 = db2.prepareStatement( getKekkahyo_kasan(test_div, "1", juken_flg) );
                ps2 = db2.prepareStatement( getKekkahyo_kasan(test_div, "2", juken_flg) );
                ps3 = db2.prepareStatement( getKekkahyo_kasan(test_div, "", juken_flg) );
                setkekkahyo(db2, svf, ps1, ps2, ps3, icol_max );
            }
            //*----------------------------*
            //*  各合計欄の編集・出力      *
            //*----------------------------*
            //『最高点、最低点、平均点』を設定
            ps1 = db2.prepareStatement( getKekkahyo_Total(test_div, "1", juken_flg) );
            ps2 = db2.prepareStatement( getKekkahyo_Total(test_div, "2", juken_flg) );
            ps3 = db2.prepareStatement( getKekkahyo_Total(test_div, "", juken_flg) );
            icol_max=5;
            setkekkahyo(db2, svf, ps1, ps2, ps3, icol_max );

            //出力
            if (nonedata) ret = svf.VrEndPage();
        } catch( Exception ex ) {
            log.error("setSvfout2 set error!"+juken_flg);
        }
        return nonedata;
    }

    /**見出し項目をセット**/
    private void setTitle(
        Vrw32alp svf
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
                ret = svf.VrsOut("NENDO"    ,param[3]);        //年度
                ret = svf.VrsOut("DATE"     ,param[4]);        //作成日
                ret = svf.VrsOut("TESTDIV"  ,param[5]);        //入試区分
                ret = svf.VrsOut("EXAMDATE" ,param[6]);        //入試日付
        } catch( Exception ex ) {
            log.error("setTitle set error!");
        }

    }

    /**
     *  svf print 明細エリアデータ出力
     */
    private boolean setkekkahyo(
        DB2UDB db2, 
        Vrw32alp svf,
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        int icol)
    {
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
        boolean nonedata = false;
        
        try{
            rs1 = ps1.executeQuery();
            //各教科：平均点(男子)、最高点、最低点
            while( rs1.next() ){
                //表出力
                String boyaverage = ( rs1.getString("AVERAGE") == null ) ? "" : String.valueOf(df.format(rs1.getFloat("AVERAGE")));
                svf.VrsOutn( "AVERAGE1", icol, boyaverage );
                nonedata = true;
            }
            rs2 = ps2.executeQuery();
            //各教科：平均点(女子)
            while( rs2.next() ){
                //表出力
                String grlaverage = ( rs2.getString("AVERAGE") == null ) ? "" : String.valueOf(df.format(rs2.getFloat("AVERAGE")));
                svf.VrsOutn( "AVERAGE2", icol, grlaverage );
                nonedata = true;
            }
            rs3 = ps3.executeQuery();
            //各教科：平均点(合計)
            while( rs3.next() ){
                //表出力
                String totalaverage = ( rs3.getString("AVERAGE") == null ) ? "" : String.valueOf(df.format(rs3.getFloat("AVERAGE")));
                svf.VrsOutn( "AVERAGE3", icol, totalaverage );
                svf.VrsOutn( "HIGH", icol, nvlT(rs3.getString("MAXSCORE")) );    //最高点
                if(!param[8].equals("")){
                    if(rs3.getString("MINSCORE") != null){
                        svf.VrsOutn( "LOW", icol, "-" );    //最低点
                    }
                } else {
                    svf.VrsOutn( "LOW", icol, nvlT(rs3.getString("MINSCORE")) ); //最低点
                }
                nonedata = true;
            }
        } catch( Exception ex ){
            log.error("setkekkahyo error!",ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps1, rs1);
            DbUtils.closeQuietly(null, ps2, rs2);
            DbUtils.closeQuietly(null, ps3, rs3);
        }
        return nonedata;
    }
    

    /**
     *  合計欄取得
     */
    private String getKekkahyo_Total(String test_div, String sSEX, int juken_flg)
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append("SELECT ");
            stb.append("MAX(W1.TOTAL4) AS MAXSCORE, ");
            stb.append("MIN(W1.TOTAL4) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(W1.TOTAL4))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_RECEPT_DAT W1 ");
            stb.append(      "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON ");
            stb.append(          "W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append(          "W1.EXAMNO = W2.EXAMNO ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");    //入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");    //入試制度
            if( param[7].equals("1") ){
                //前回までの合格者を対象に含める場合
                stb.append("W1.TESTDIV <= '" + test_div + "' AND ");    //入試区分
            }else {
                //前回までの合格者を対象に含めない場合
                stb.append("W1.TESTDIV = '" + test_div + "' AND ");     //入試区分
            }
            if(!sSEX.equals("")){
                stb.append(" W2.SEX = '" + sSEX + "' AND");             //性別 
            }
            stb.append(" W1.TOTAL4 IS NOT NULL ");                      //全科目合計
            if (juken_flg == 1) {
                stb.append("AND W1.JUDGEDIV = '1'");                    //合否区分
            }

        } catch( Exception ex ){
            log.error("sql statement error!"+ex );
        }
        return stb.toString();
    }
    
    /**
     *  加算点欄取得
     */
    private String getKekkahyo_kasan(String test_div, String sSEX, int juken_flg)
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append("SELECT ");
            stb.append("    MAX(INT(T1.TOTAL2)) AS MAXSCORE, ");
            stb.append("    MIN(INT(T1.TOTAL2)) AS MINSCORE, ");
            stb.append("    AVG(FLOAT(T1.TOTAL2)) AS AVERAGE ");
            stb.append("FROM ENTEXAM_RECEPT_DAT T1 ");
            stb.append("    INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T1.EXAMNO = T2.EXAMNO ");
            stb.append("WHERE TOTAL2 IS NOT NULL ");
            stb.append("    AND T1.ENTEXAMYEAR = '" + param[0] + "' ");    //入試年度
            stb.append("    AND T1.APPLICANTDIV = '" + param[1] + "' ");   //入試制度
            if( param[7].equals("1") ){
                //前回までの合格者を対象に含める場合
                stb.append("    AND T1.TESTDIV <= '" + test_div + "' ");   //入試区分
            } else {
                //前回までの合格者を対象に含めない場合
                stb.append("    AND T1.TESTDIV  = '" + test_div + "' ");   //入試区分
            }
            stb.append("    AND T1.TOTAL4 IS NOT NULL ");                  //4科目合計
            if(!sSEX.equals("")){
                stb.append("    AND T2.SEX = '" + sSEX + "' ");            //性別 
            }
            if (juken_flg == 1) {
                stb.append("    AND T1.JUDGEDIV = '1' ");                  //合否区分
            }
        } catch( Exception ex ){
            log.error("sql statement error!"+ex );
        }
        log.debug("+++++" + stb.toString());
        return stb.toString();
    }

    /**
     *   各科目欄取得
     */
    private String getKekkahyo(String test_div, String sTestSubClassCd, String sSEX, int juken_flg)
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append("SELECT ");
            stb.append("MAX(W1.SCORE) AS MAXSCORE, ");
            stb.append("MIN(W1.SCORE) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(W1.SCORE))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_SCORE_DAT W1 ");
            stb.append(      "INNER JOIN ENTEXAM_RECEPT_DAT W2 ON ");
            stb.append(          "W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(          "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(          "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(          "W2.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(          "W2.RECEPTNO = W1.RECEPTNO ");
            stb.append(      "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append(          "W2.ENTEXAMYEAR = W3.ENTEXAMYEAR AND ");
            stb.append(          "W2.EXAMNO = W3.EXAMNO ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("W2.ENTEXAMYEAR = '" + param[0] + "'  ");             //入試年度
            stb.append(" AND W2.APPLICANTDIV = '" + param[1] + "' ");        //入試制度
            if( param[7].equals("1") ){
                //前回までの合格者を対象に含める場合
                stb.append(" AND W2.TESTDIV <= '" + test_div + "' ");        //入試区分
            }else {
                //前回までの合格者を対象に含めない場合
                stb.append(" AND W2.TESTDIV = '" + test_div + "' ");         //入試区分
            }
            stb.append(" AND W1.TESTSUBCLASSCD = '" + sTestSubClassCd + "'");//試験科目コード 
            if(!sSEX.equals("")){
                stb.append(" AND W3.SEX = '" + sSEX + "' ");                 //性別 
            }
            if (juken_flg == 1) {
                stb.append(" AND W2.JUDGEDIV = '1'");                        //合否区分
            }

        } catch( Exception ex ){
            log.error("sql statement error!"+ex );
        }
        return stb.toString();
    }


    /**入試区分を取得**/
    private String preStat()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT DISTINCT TESTDIV ");
            stb.append("FROM   ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("       APPLICANTDIV='"+param[1]+"' ");
            stb.append("ORDER BY TESTDIV ");
        } catch( Exception e ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り



    /**速報データ（人数）を取得**/
    private String preStat1()
    {
        StringBuffer stb = new StringBuffer();
    //パラメータ（入試区分２つ）
        try {
            //志願者
            stb.append("SELECT  'A' TYPE_CNT, ");
            stb.append("        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ");
            stb.append("        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ");
            stb.append("        COUNT(*) AS TOTAL3  ");
            stb.append("FROM    ENTEXAM_DESIRE_DAT W2  ");
            stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W2.ENTEXAMYEAR AND  ");
            stb.append("                                                   W3.EXAMNO = W2.EXAMNO  ");
            stb.append("WHERE   W2.ENTEXAMYEAR = '"+param[0]+"' AND  ");
            stb.append("        W2.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者データ：入試制度
            stb.append("        W2.TESTDIV = ? AND  ");                     //志願者データ：入試区分
            stb.append("        W2.APPLICANT_DIV = '1'  ");                 //志願者データ：志願者区分
            stb.append("GROUP BY W2.TESTDIV  ");
            //受験者
            stb.append("UNION ALL  ");
            stb.append("SELECT  'B' TYPE_CNT, ");
            stb.append("        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ");
            stb.append("        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ");
            stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TOTAL3  ");
            stb.append("FROM    ENTEXAM_RECEPT_DAT W1  ");
            stb.append("        INNER JOIN ENTEXAM_DESIRE_DAT W2 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
            stb.append("                                            W2.APPLICANTDIV = W1.APPLICANTDIV AND  ");
            stb.append("                                            W2.TESTDIV = W1.TESTDIV AND  ");
            stb.append("                                            W2.EXAMNO = W1.EXAMNO  ");
            stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
            stb.append("                                                   W3.EXAMNO = W1.EXAMNO  ");
            stb.append("WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
            stb.append("        W1.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者受付データ：入試制度
            stb.append("        W1.TESTDIV = ? AND  ");                     //志願者受付データ：入試区分
            stb.append("        W2.EXAMINEE_DIV = '1' AND  ");              //志願者データ：受験者区分
            stb.append("        W2.APPLICANT_DIV = '1' ");                  //志願者データ：志願者区分
            stb.append("GROUP BY W1.TESTDIV  ");

            //合格者
            stb.append("UNION ALL  ");
            stb.append("SELECT  'C' TYPE_CNT, ");
            stb.append("        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ");
            stb.append("        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ");
            stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TOTAL3  ");
            stb.append("FROM    ENTEXAM_RECEPT_DAT W1  ");
            stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
            stb.append("                                                   W3.EXAMNO = W1.EXAMNO  ");
            stb.append("WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
            stb.append("        W1.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者受付データ：入試制度
            if( !param[7].equals("1") ){    
                stb.append("        W1.TESTDIV = ? AND  ");                 //志願者受付データ：入試区分
            }else {
                stb.append("        W1.TESTDIV <= ? AND  ");                //志願者受付データ：入試区分
            }
            stb.append("        W1.JUDGEDIV = '1'  ");                      //志願者受付データ：合否区分

            //前回までの合格者を対象に含めない場合
            if( !param[7].equals("1")){
                stb.append(    "AND W1.EXAMNO NOT IN(");
                stb.append(             "SELECT  DISTINCT EXAMNO ");
                stb.append(             "FROM    ENTEXAM_RECEPT_DAT W1  ");
                stb.append(             "WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
                stb.append(                     "W1.APPLICANTDIV = '"+param[1]+"' AND  ");  //志願者受付データ：入試制度
                stb.append(                     "W1.TESTDIV < ? AND  ");                    //志願者受付データ：入試区分
                stb.append(                     "W1.JUDGEDIV = '1' ");                      //志願者受付データ：合否区分
                stb.append(             ")");
            }

            if( !param[7].equals("1")){
                stb.append("GROUP BY W1.TESTDIV  ");
            }
            stb.append("ORDER BY TYPE_CNT ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り



    /**名称を取得**/
    private String preStat2()
    {
        StringBuffer stb = new StringBuffer();
    //パラメータ（入試区分）
        try {
            if (param[1].equals("1")){
                stb.append("SELECT NAME1 AS TEST_NAME ");
            } else {
                stb.append("SELECT ABBV1 AS TEST_NAME ");
            }
            stb.append("FROM   NAME_MST T4 ");
            if (param[1].equals("1")){
                stb.append("WHERE  NAMECD1='L004' AND NAMECD2=? ");
            } else {
                stb.append("WHERE  NAMECD1='L003' AND NAMECD2=? ");
            }
        } catch( Exception e ){
            log.error("preStat2 error!");
        }
        return stb.toString();

    }//preStat2()の括り

    /**入試日を取得**/
    private String preStat3()
    {
        StringBuffer stb = new StringBuffer();
    //パラメータ（入試区分）
        try {
            stb.append("SELECT NAMESPARE1 AS TEST_DATE ");
            stb.append("FROM   NAME_MST T4 ");
            stb.append("WHERE  NAMECD1='L004' AND NAMECD2=? ");
        } catch( Exception e ){
            log.error("preStat3 error!");
        }
        return stb.toString();

    }//preStat3()の括り


    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3
    ) {
        try {
            ps.close();
            ps1.close();
            ps2.close();
            ps3.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate, String sfmt) {

        String retDate = "";
        try {
            DateFormat foramt = new SimpleDateFormat(sfmt); 
            //文字列よりDate型へ変換
            Date date1 = foramt.parse(cnvDate); 
            //年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
            //Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        return retDate;
    }

    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     * @return    stb
     */
    private String getSubClass()
    {
        StringBuffer stb = new StringBuffer();
        
        try{
            stb.append("SELECT  T1.NAMECD2, T1.NAME1 ");
            stb.append(      "FROM    ENTEXAM_TESTSUBCLASSCD_DAT W1 ");
            stb.append(      "    LEFT JOIN NAME_MST T1 ON T1.NAMECD1='L009' ");
            stb.append(      "         AND T1.NAMECD2 = W1.TESTSUBCLASSCD  ");
            stb.append(      "WHERE   W1.ENTEXAMYEAR  = '" + param[0] + "' AND ");
            stb.append(      "        W1.APPLICANTDIV = '" + param[1] + "'");
            stb.append(" ORDER BY  ");
            stb.append("    W1.SHOWORDER ");


        } catch( Exception ex ){
            log.error("sql statement error!"+ex );
        }
        return stb.toString();
    }

    
    /**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     * @param rs    実行結果オブジェクト
     * @param svf    帳票オブジェクト
     */
    private void setSubClass(DB2UDB db2, Vrw32alp svf, String stb)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            int idx_kamoku = 1;
            //SQL発行
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while( rs.next() ){
                svf.VrsOutn("SUBCLASSNAME", idx_kamoku ,rs.getString("NAME1"));
                hkamoku.put(String.valueOf(idx_kamoku) ,rs.getString("NAMECD2"));
                ++idx_kamoku;
            }
            //試験制度が高校(一般)の場合、帳票の試験科目名称の最後に
            //『加算』を設定する
            if(param[1].equals("2")){
                svf.VrsOutn("SUBCLASSNAME2", idx_kamoku, "3科合計");
            }

        } catch( Exception ex ){
            log.error("getSUBCLASS error!",ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    /**
     * NULL値を""として返す。
     * @return 変換後文字列
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val.trim();
        }
    }

    
}//クラスの括り
