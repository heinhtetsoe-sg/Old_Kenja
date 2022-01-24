// kanji=漢字
/*
 * $Id: a5045c9ce790a9c5b361455be1fe12380291a32d $
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
 *                  ＜ＫＮＪＭ１３０＞  時間割チェックリスト
 *
 *  2005/04/06 m-yama 作成日
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM130 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM130.class);
    int len = 0;            //列数カウント用
    int ccnt    = 0;
    String chairnm[];
    String chaircd[];

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[7];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                                //年度
            param[1] = request.getParameter("CHAIR");                               //学期
log.debug("class"+param[1]);
log.debug("date"+param[3]);
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
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJM130]param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));       //講座担当コードpreparestatement
            ps3 = db2.prepareStatement(Pre_Stat3(param));       //講座担当コードpreparestatement
            ps4 = db2.prepareStatement(Pre_Stat4(param));       //担当名preparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }
        //カウンタ
        //SVF出力
        //固定項目GET
        if (param[1].equals("0")) {
            Allclassdata(db2,svf,param,ps3);
        }else {
            Classdata(db2,svf,param,ps2);
        }

        for( int ia=0 ; ia<chairnm.length ; ia++ ){
            try {
log.debug("chair"+chaircd[ia]);
log.debug("data"+chairnm[ia]);
            } catch( Exception ex ) {
                log.error("SQL read error!");
            }
            if( Set_Detail_1(db2,svf,param,chairnm[ia],chaircd[ia],ps1,ps4) )nonedata = true;
            if( nonedata )  svf.VrEndPage();              //最終データフィールド出力
        }
    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1,ps2,ps3,ps4);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJM130.frm", 1);
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

    }//Set_Head()の括り

    /** SVF-FORM **/
    private void Allclassdata(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps3){

        int Allcnt = 0;
        try {
log.debug("Allclass");
            ResultSet rs3 = ps3.executeQuery();
            while( rs3.next() ){
                Allcnt++;
            }
            rs3.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        chairnm = new String[Allcnt];
        chaircd = new String[Allcnt];

        try {
            ResultSet rs3 = ps3.executeQuery();
            while( rs3.next() ){
                chairnm[ccnt] = rs3.getString("CHAIRNAME");
                chaircd[ccnt] = rs3.getString("CHAIRCD");
                ccnt++;
            }
            rs3.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

    }//Set_Head()の括り

    /** SVF-FORM **/
    private void Classdata(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps2){

        int Allcnt = 0;
        try {
            ResultSet rs2 = ps2.executeQuery();
            while( rs2.next() ){
                Allcnt++;
            }
            rs2.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        chairnm = new String[Allcnt];
        chaircd = new String[Allcnt];
log.debug("classdata");

        try {
            ResultSet rs2 = ps2.executeQuery();
            while( rs2.next() ){
                chairnm[ccnt] = rs2.getString("CHAIRNAME");
                chaircd[ccnt] = rs2.getString("CHAIRCD");
                ccnt++;
            }
            rs2.close();
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

    }//Set_Head()の括り


    /**SVF-FORM**/
    private boolean Set_Detail_1(
    DB2UDB db2,
    Vrw32alp svf,
    String param[],
    String chairnm,
    String chaircd,
    PreparedStatement ps1,
    PreparedStatement ps4)
    {
        boolean nonedata = false;
        int stcnt   = 0;            //担当者カウンタ
        String stafnm  ;            //担当者名
        String seqflg  ;            //回数設定
        try {
            //担当者名セット
            ps4.setString(1,chaircd);   //講座コード
            ResultSet rs4 = ps4.executeQuery();
            stafnm = String.valueOf("");
            while( rs4.next() ){
                if (stcnt > 0) stafnm = stafnm + String.valueOf("、");
                stafnm = stafnm + rs4.getString("STAFFNAME");
                stcnt++;
            }

            ps1.setString(1,chaircd);   //講座コード
            ResultSet rs = ps1.executeQuery();
            int gyo   = 1;          //行数カウント用
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                //ヘッダ出力
                svf.VrsOut("YEAR"                         , String.valueOf(param[0]) );
                svf.VrsOut("CHAIRNAME"                    , String.valueOf(chairnm) );
                svf.VrsOut("ATTESTOR"                     , String.valueOf(stafnm) );
                //日付・校時・回数・備考
                svf.VrsOutn("EXECUTEDATE"     ,gyo, KNJ_EditDate.h_format_JP_MD(rs.getString("EXECUTEDATE")) + "(" + KNJ_EditDate.h_format_W(rs.getString("EXECUTEDATE")) + ")" );
                svf.VrsOutn("PERIODCD"        ,gyo, rs.getString("NAME1") );
                
                if (rs.getString("SEQFLG").equals("A")){
                    seqflg = String.valueOf("第");
                    seqflg = seqflg + rs.getString("SCHOOLING_SEQ");
                    seqflg = seqflg + String.valueOf("回");
                }else {
                    seqflg = String.valueOf("");
                }
                svf.VrsOutn("SCHOOLING_SEQ"   ,gyo, String.valueOf(seqflg) );
                svf.VrsOutn("REMARK"          ,gyo, rs.getString("REMARK") );

                nonedata = true;
                gyo++;          //行数カウント用
            }
            rs.close();

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
            stb.append("SELECT ");
            stb.append("    EXECUTEDATE, ");
            stb.append("    PERIODCD, ");
            stb.append("    NAME1, ");
            stb.append("    SCHOOLING_SEQ, ");
            stb.append("    CASE WHEN SCHOOLING_SEQ IS NULL THEN 'N' ELSE 'A' END AS SEQFLG, ");
            stb.append("    REMARK ");
            stb.append("FROM ");
            stb.append("    SCH_CHR_T_DAT T1 LEFT JOIN V_NAME_MST T2 ON PERIODCD = NAMECD2 AND T2.YEAR = '"+param[0]+"' AND NAMECD1 = 'B001' ");
            stb.append("WHERE ");
            stb.append("    CHAIRCD = ? AND ");
            stb.append("    T1.YEAR = '"+param[0]+"' ");
            stb.append("ORDER BY ");
            stb.append("    EXECUTEDATE, ");
            stb.append("    PERIODCD, ");
            stb.append("    SCHOOLING_SEQ ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り


    /**講座指定時抽出**/
    private String Pre_Stat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    CHAIRCD,MAX(CHAIRNAME) AS CHAIRNAME ");
            stb.append("FROM ");
            stb.append("    CHAIR_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' AND ");
            stb.append("    CHAIRCD = '"+param[1]+"' ");
            stb.append("GROUP BY ");
            stb.append("    CHAIRCD ");

        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**全講座抽出**/
    private String Pre_Stat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("with chnm AS (SELECT ");
            stb.append("    CHAIRCD,MAX(CHAIRNAME) AS CHAIRNAME ");
            stb.append("FROM ");
            stb.append("    CHAIR_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' AND ");
            stb.append("    CHAIRCD NOT LIKE '92%' ");
            stb.append("GROUP BY ");
            stb.append("    CHAIRCD ) ");
            stb.append("SELECT ");
            stb.append("    t1.CHAIRCD,t2.CHAIRNAME ");
            stb.append("FROM ");
            stb.append("    SCH_CHR_T_DAT t1 left join chnm t2 ON t1.CHAIRCD = t2.CHAIRCD ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' AND ");
            stb.append("    t1.CHAIRCD NOT LIKE '92%' ");
            stb.append("GROUP BY ");
            stb.append("    t1.CHAIRCD,t2.CHAIRNAME ");
            stb.append("ORDER BY ");
            stb.append("    t1.CHAIRCD ");

log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat3 error!");
        }
        return stb.toString();

    }//Pre_Stat3()の括り

    /**担当者取得**/
    private String Pre_Stat4(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    t1.STAFFCD,t2.STAFFNAME ");
            stb.append("FROM ");
            stb.append("    CHAIR_STF_DAT t1 left join STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND t1.CHAIRCD = ? ");
            stb.append("GROUP BY t1.STAFFCD,t2.STAFFNAME ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat4 error!");
        }
        return stb.toString();

    }//Pre_Stat4()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4
    ) {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
