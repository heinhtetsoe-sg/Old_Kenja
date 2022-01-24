// kanji=漢字
/*
 * $Id: d280966899313ccf729f8df07c5341ab56b131d1 $
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
 *                  ＜ＫＮＪＭ３１１＞  レポート受付件数リスト
 *
 *  2005/07/02 m-yama 作成日
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM311 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM311.class);
    int len = 0;            //列数カウント用
    int ccnt    = 0;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[8];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                                //年度
            param[1] = request.getParameter("DATEF");                               //日付FROM
            param[2] = request.getParameter("DATET");                               //日付TO
            param[7] = request.getParameter("useCurriculumcd");                     //教育課程
log.debug("class"+param[0]);
log.debug("date"+param[1]);
        log.fatal("$Revision: 56595 $");
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
        boolean nonedata = false;                               //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJM311]param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //設定データpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }
        //SVF出力

        if( Set_Detail_1(db2,svf,param,ps1) )nonedata = true;
        if( nonedata )  svf.VrEndPage();              //最終データフィールド出力
    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1);          //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJM311.frm", 1);
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

    }//Set_Head()の括り

    /**SVF-FORM**/
    private boolean Set_Detail_1(
    DB2UDB db2,
    Vrw32alp svf,
    String param[],
    PreparedStatement ps1)
    {
        boolean nonedata = false;
        int fcnt    = 0;
        int rcnt    = 0;
        int tcnt    = 0;
        try {
            ResultSet rs = ps1.executeQuery();
            int gyo   = 1;          //行数カウント用
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                //ヘッダ出力
                svf.VrsOut("SDATE"        , String.valueOf(param[1]) );
                svf.VrsOut("EDATE"        , String.valueOf(param[2]) );
                svf.VrsOut("DATE"         , String.valueOf(param[6]) );
                //科目コード・学籍・生徒・科目名・再提出・回数・受付月日・返信日付・評価
                if ("1".equals(param[7])) {
                    svf.VrsOutn("SUBCLASSCD"      ,gyo, null != rs.getString("SUBCD") && rs.getString("SUBCD").length() >= 3 ? rs.getString("SUBCD").substring(3) : rs.getString("SUBCD"));
                } else {
                    svf.VrsOutn("SUBCLASSCD"      ,gyo, rs.getString("SUBCD"));
                }
                svf.VrsOutn("SUBCLASSNAME"    ,gyo, rs.getString("SUBCLASSNAME"));
                svf.VrsOutn("FIRSTREPCNT"     ,gyo, rs.getString("STANCNT"));
                if (rs.getString("STANCNT") != null){
                    fcnt = fcnt + Integer.parseInt(rs.getString("STANCNT"));
                }
                svf.VrsOutn("REPRESENTCNT"    ,gyo, rs.getString("REPCNT"));
                if (rs.getString("REPCNT") != null){
                    rcnt = rcnt + Integer.parseInt(rs.getString("REPCNT"));
                }
                svf.VrsOutn("TOTALCNT"        ,gyo, rs.getString("TOTALCNT"));
                if (rs.getString("TOTALCNT") != null){
                    tcnt = tcnt + Integer.parseInt(rs.getString("TOTALCNT"));
                }
                nonedata = true;
                gyo++;          //行数カウント用
            }
            rs.close();
            if (nonedata){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }
                if (gyo < 51){
                    for (;gyo < 50;gyo++){
                        //白抜き
                        svf.VrAttributen("FIRSTREPCNT"    ,gyo    ,"Meido=100");
                        svf.VrAttributen("REPRESENTCNT"   ,gyo    ,"Meido=100");
                        svf.VrAttributen("TOTALCNT"       ,gyo    ,"Meido=100");
                    }
                    //ヘッダ出力
                    svf.VrsOut("SDATE"        , String.valueOf(param[1]) );
                    svf.VrsOut("EDATE"        , String.valueOf(param[2]) );
                    svf.VrsOut("DATE"         , String.valueOf(param[6]) );
                    //中央寄せ
                    svf.VrAttributen("SUBCLASSNAME",gyo   ,"Hensyu=3");
                    //合計
                    svf.VrsOutn("SUBCLASSNAME"    ,gyo    ,"合計");
                    svf.VrsOutn("FIRSTREPCNT"     ,gyo    ,String.valueOf(fcnt));
                    svf.VrsOutn("REPRESENTCNT"    ,gyo    ,String.valueOf(rcnt));
                    svf.VrsOutn("TOTALCNT"        ,gyo    ,String.valueOf(tcnt));
                }
            }
        } catch( Exception ex ) {
            log.error("Set_Detail_1 read error!");
        }
        return nonedata;

    }//Set_Detail_1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SUBTABLE AS ( ");
            stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");
            } else {
                stb.append("     SUBCLASSCD ");
            }
            stb.append("    AS SUBCD,COUNT(SUBCLASSCD) AS TOTALCNT ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND RECEIPT_DATE BETWEEN '"+param[1].replace('/','-')+"' AND '"+param[2].replace('/','-')+"' ");
            stb.append("    AND GRAD_VALUE NOT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1='M011') ");
            stb.append("GROUP BY ");
            if ("1".equals(param[7])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
            stb.append("), ");
            stb.append("STANDARD_TABLE AS ( ");
            stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");
            } else {
                stb.append("     SUBCLASSCD ");
            }
            stb.append("    AS SSUBCD,COUNT(SUBCLASSCD) AS STANCNT ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND REPRESENT_SEQ = 0 ");
            stb.append("    AND RECEIPT_DATE BETWEEN '"+param[1].replace('/','-')+"' AND '"+param[2].replace('/','-')+"' ");
            stb.append("    AND GRAD_VALUE NOT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1='M011') ");
            stb.append("GROUP BY ");
            if ("1".equals(param[7])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
            stb.append("), ");
            stb.append("REPRESENT_TABLE AS ( ");
            stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");
            } else {
                stb.append("     SUBCLASSCD ");
            }
            stb.append("    AS RSUBCD,COUNT(SUBCLASSCD) AS REPCNT ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND (REPRESENT_SEQ > 0 OR REPRESENT_SEQ < 0)");
            stb.append("    AND RECEIPT_DATE BETWEEN '"+param[1].replace('/','-')+"' AND '"+param[2].replace('/','-')+"' ");
            stb.append("    AND GRAD_VALUE NOT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1='M011') ");
            stb.append("GROUP BY ");
            if ("1".equals(param[7])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    SUBCD,SUBCLASSNAME,STANCNT,REPCNT,TOTALCNT ");
            stb.append("FROM ");
            stb.append("    SUBTABLE ");
            stb.append("    LEFT JOIN STANDARD_TABLE ON SUBCD = SSUBCD ");
            stb.append("    LEFT JOIN REPRESENT_TABLE ON SUBCD = RSUBCD ");
            stb.append("    LEFT JOIN SUBCLASS_MST ON SUBCD = CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ");
            stb.append("ORDER BY ");
            stb.append("    SUBCD ");

log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1
    ) {
        try {
            ps1.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
