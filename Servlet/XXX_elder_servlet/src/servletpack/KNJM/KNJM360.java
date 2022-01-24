// kanji=漢字
/*
 * $Id: 54f96dfc651236f70ea0991d568ee104b4bb7bee $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ３６０＞  未返送レポート一覧
 *
 *  2005/05/18 m-yama 作成
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM360 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM360.class);
    int len = 0;            //列数カウント用
    int ccnt    = 0;
    String subclnm[];
    String subclcd[];
    String subclct[];
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    boolean paramset  = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[9];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                //年度
            param[1] = request.getParameter("CHAIR");               //学期
            param[2] = request.getParameter("LASTDAY");             //経過日数
            if (request.getParameter("OUTPUT1") != null) param[3] = request.getParameter("OUTPUT1");
            if (request.getParameter("OUTPUT2") != null) param[4] = request.getParameter("OUTPUT2");
            param[5] = request.getParameter("SUBCLASS");                            //科目
            param[8] = request.getParameter("useCurriculumcd");                     //教育課程
        } catch( Exception ex ) {
            log.error("Param read error!");
        }
    //  print設定
        new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);        //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        boolean nonedata  = false;                              //該当データなしフラグ
        boolean nonedata1 = false;                              //該当データなしフラグ
        boolean nonedata2 = false;                              //該当データなしフラグ
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //レポート詳細データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));       //科目データ指定preparestatement
            ps3 = db2.prepareStatement(Pre_Stat3(param));       //科目データ全てpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }
        //経過日付設定
        try {
            Set_Head(db2,svf,param);                                //現在日付取得
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(sdf.parse(param[6].replace('/','-')));     //Calendar cal1に現在日付をセット
            cal1.add(Calendar.DATE,-(Integer.parseInt(param[2])));  //経過日付算出
            param[7] = sdf.format(cal1.getTime());                  //経過日付設定
        } catch( Exception ex ) {
            log.error("SQL Set_Head read error!");
        }

for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJM360]param["+ia+"]="+param[ia]);
        log.fatal("$Revision: 56595 $");

        //カウンタ
        //SVF出力
        if (param[3] != null) {
            Set_Frm(db2,svf,"1",param);                             //FRM設定のメソッド
            Allclassdata(db2,svf,param,ps3);
            if( nonedata2 = Set_Detail_2(db2,svf,param) )nonedata = true;
            if( nonedata2 ) svf.VrEndPage();              //最終データフィールド出力
        }
        if (param[4] != null){
            if (param[5].equals("0") && !paramset) {
                Allclassdata(db2,svf,param,ps3);
            }else {
                Classdata(db2,svf,param,ps2);
            }
        }
        nonedata1 = false;
        nonedata2 = false;
        if (param[4] != null) {
            if (param[5].equals("0")){
                for( int ia=0 ; ia<subclnm.length ; ia++ ){
                    Set_Frm(db2,svf,"2",param);                         //FRM設定のメソッド
                    if( nonedata2 = Set_Detail_1(db2,svf,param,subclnm[ia],subclcd[ia],ps1) )nonedata1 = true;
                    if( nonedata2 ) svf.VrEndPage();              //最終データフィールド出力
                }
            }else {
                if(subclnm.length > 0){                                 //出力データ無し
                    Set_Frm(db2,svf,"2",param);                         //FRM設定のメソッド
                    if( nonedata2 = Set_Detail_1(db2,svf,param,subclnm[0],subclcd[0],ps1) )nonedata1 = true;
                    if( nonedata2 ) svf.VrEndPage();              //最終データフィールド出力
                }
            }
        }
log.debug("nonedata"+nonedata+" "+nonedata1);
    //  該当データ無し
        if( !nonedata1 && !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1,ps2,ps3);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    /** ヘッダ **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
log.debug(String.valueOf(param[6]));
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

    }//Set_Head()の括り


    /** FRM設定 **/
    private void Set_Frm(DB2UDB db2,Vrw32alp svf,String selectfrm,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        if (selectfrm.equals("1")) {
            svf.VrSetForm("KNJM360_1.frm", 1);
        }else {
            svf.VrSetForm("KNJM360_2.frm", 1);
        }
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
log.debug(String.valueOf(param[6]));
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

    }//Set_Frm()の括り

    /** 全科目 **/
    private void Allclassdata(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps3){

        int Allcnt = 0;
        try {
log.debug("Allclass");
            ps3.setString(1,param[7]);  //日付
            ResultSet rs3 = ps3.executeQuery();
            while( rs3.next() ){
                Allcnt++;
            }
            rs3.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        subclnm = new String[Allcnt];
        subclcd = new String[Allcnt];
        subclct = new String[Allcnt];
        ccnt = 0;
        try {
            ResultSet rs3 = ps3.executeQuery();
            while( rs3.next() ){
                subclnm[ccnt] = rs3.getString("SUBCLASSNAME");
                subclcd[ccnt] = rs3.getString("SUBCLASSCD");
                subclct[ccnt] = rs3.getString("SUBCNT");
                ccnt++;
            }
            rs3.close();
            paramset = true;
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

    }//Allclassdata()の括り

    /** 指定科目 **/
    private void Classdata(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps2){

        int Allcnt = 0;
        try {
            ps2.setString(1,param[7]);  //日付
            ResultSet rs2 = ps2.executeQuery();
            while( rs2.next() ){
                Allcnt++;
            }
            rs2.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        if (!paramset){
            subclnm = new String[Allcnt];
            subclcd = new String[Allcnt];
            subclct = new String[Allcnt];
        }
log.debug("classdata");

        ccnt = 0;
        try {
            ResultSet rs2 = ps2.executeQuery();
            while( rs2.next() ){
                subclnm[ccnt] = rs2.getString("SUBCLASSNAME");
                subclcd[ccnt] = rs2.getString("SUBCLASSCD");
                subclct[ccnt] = rs2.getString("SUBCNT");
                ccnt++;
            }
            rs2.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

    }//Classdata()の括り


    /**SVF-FORM**/
    private boolean Set_Detail_1(
    DB2UDB db2,
    Vrw32alp svf,
    String param[],
    String subclnm,
    String subclcd,
    PreparedStatement ps1)
    {
        boolean nonedata = false;
        int stcnt   = 0;                //件数カウンタ
        try {
            ps1.setString(1,subclcd);   //科目コード
            ps1.setString(2,param[7]);  //日付
            ResultSet rs = ps1.executeQuery();
            int gyo   = 1;          //行数カウント用
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }
                //ヘッダ出力
                svf.VrsOut("DATE"         , String.valueOf(param[6]) );
                svf.VrsOut("DAY"          , String.valueOf(param[2]) );
                svf.VrsOut("SUBCLASSNAME" , String.valueOf(subclnm));

                //学籍番号・生徒名・回数・再提出・受付日・受付時間
                svf.VrsOutn("SCHREGNO"    ,gyo, rs.getString("SCHREGNO") );
                svf.VrsOutn("SCHREGNAME"  ,gyo, rs.getString("NAME") );
                svf.VrsOutn("SEQ"         ,gyo, "第"+rs.getString("STANDARD_SEQ")+"回" );
                if (!rs.getString("REPRESENT_SEQ").equals("0")){
                    svf.VrsOutn("SAI"         ,gyo, "再提出"+rs.getString("REPRESENT_SEQ")+"回目" );
                }
                svf.VrsOutn("RECEIPT_DATE",gyo, rs.getString("RECEIPT_DATE").replace('-','/') );
                svf.VrsOutn("RECEIPT_TIME",gyo, rs.getString("RECEIPT_TIME") );
                
                nonedata = true;
                gyo++;          //行数カウント用
                stcnt++;        //件数カウント用
            }
            svf.VrsOut("TOTAL"            , String.valueOf(stcnt) );
            rs.close();

        } catch( Exception ex ) {
            log.error("Set_Detail_1 read error!");
        }
        return nonedata;

    }//Set_Detail_1()の括り

    /* 科目別件数一覧 */
    private boolean Set_Detail_2(
    DB2UDB db2,
    Vrw32alp svf,
    String param[])
    {
        boolean nonedata = false;
        int gyo  = 1;           //行数カウント用
        int ken  = 0;           //件数カウント用
        try {
            for( int ia=0 ; ia<subclnm.length ; ia++ ){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                //ヘッダ出力
                svf.VrsOut("DATE"     , String.valueOf(param[6]) );
                svf.VrsOut("DAY"      , String.valueOf(param[2]) );
                //科目名・件数
                svf.VrsOutn("SUBCLASSNAME"    ,gyo, String.valueOf(subclnm[ia]));
                svf.VrsOutn("KENSU"           ,gyo, String.valueOf(subclct[ia])+"件" );
                
                ken = ken + Integer.parseInt(subclct[ia]);          //TOTAL用
                nonedata = true;
                gyo++;          //行数カウント用
            }
            svf.VrsOut("TOTAL"            , String.valueOf(ken) );
        } catch( Exception ex ) {
            log.error("Set_Detail_2 read error!");
        }
        return nonedata;

    }//Set_Detail_2()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    t1.SCHREGNO, ");
            stb.append("    t2.NAME, ");
            stb.append("    t1.STANDARD_SEQ, ");
            stb.append("    t1.REPRESENT_SEQ, ");
            stb.append("    t1.RECEIPT_DATE, ");
            stb.append("    t1.RECEIPT_TIME ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT t1 left join SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            if ("1".equals(param[8])) {
                stb.append("    AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = ? ");
            } else {
                stb.append("    AND t1.SUBCLASSCD = ? ");
            }
            stb.append("    AND t1.GRAD_DATE is null ");
            stb.append("    AND t1.RECEIPT_DATE <= ? ");
            stb.append("ORDER BY ");
            stb.append("    t1.SCHREGNO ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り


    /**科目指定時抽出**/
    private String Pre_Stat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            if ("1".equals(param[8])) {
                stb.append("    t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("    t1.SUBCLASSCD, ");
            }
            stb.append("    t2.SUBCLASSNAME, ");
            stb.append("    COUNT(t1.SUBCLASSCD) AS SUBCNT ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT t1 left join SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ");
            if ("1".equals(param[8])) {
                stb.append("       AND t1.CLASSCD = t2.CLASSCD ");
                stb.append("       AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
            }
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            if ("1".equals(param[8])) {
                stb.append("    AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '"+param[5]+"' ");
            } else {
                stb.append("    AND t1.SUBCLASSCD = '"+param[5]+"' ");
            }
            stb.append("    AND t1.GRAD_DATE is null ");
            stb.append("    AND t1.RECEIPT_DATE <= ? ");
            stb.append("GROUP BY ");
            if ("1".equals(param[8])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD, ");
            stb.append("    t2.SUBCLASSNAME ");
            stb.append("ORDER BY ");
            if ("1".equals(param[8])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD ");

        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**全科目抽出**/
    private String Pre_Stat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            if ("1".equals(param[8])) {
                stb.append("    t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("    t1.SUBCLASSCD, ");
            }
            stb.append("    t2.SUBCLASSNAME, ");
            stb.append("    COUNT(t1.SUBCLASSCD) AS SUBCNT ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT t1 left join SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ");
            if ("1".equals(param[8])) {
                stb.append("       AND t1.CLASSCD = t2.CLASSCD ");
                stb.append("       AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
            }
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND t1.GRAD_DATE is null ");
            stb.append("    AND t1.RECEIPT_DATE <= ? ");
            stb.append("GROUP BY ");
            if ("1".equals(param[8])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD, ");
            stb.append("    t2.SUBCLASSNAME ");
            stb.append("ORDER BY ");
            if ("1".equals(param[8])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat3 error!");
        }
        return stb.toString();

    }//Pre_Stat3()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3
    ) {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
