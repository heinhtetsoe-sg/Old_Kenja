// kanji=漢字
/*
 * $Id: d36818fb58d11811563c7d1ad38c07c3ddaee74a $
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
 *                  ＜ＫＮＪＭ２９０＞  レポート担当者別提出リスト
 *
 *  2005/04/19 m-yama 作成日
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM300 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM300.class);
    int len = 0;            //列数カウント用
    int ccnt    = 0;
    String schno[];
    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[8];
        String schno[] = request.getParameterValues("category_selected");       //生徒

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                                //年度
            param[1] = request.getParameter("STAFF");                               //学期
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
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJM300]param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //設定データpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }
        //カウンタ
        //SVF出力
        int i = 0;
        while(i < schno.length){
            Set_Detail_1(db2,svf,param,schno[i],ps1);
            i++;
        }
    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJM300.frm", 1);
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
    String schno,
    PreparedStatement ps1)
    {
log.debug("main");
        boolean dataflg  = false;   //データフラグ
        String seqflg  ;            //回数設定
        try {
            ps1.setString(1,schno); //講座コード
log.debug("start");
            ResultSet rs = ps1.executeQuery();
log.debug("end");
            int gyo   = 1;          //行数カウント用
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                //ヘッダ出力
                svf.VrsOut("NENDO1"       , String.valueOf(param[0]) );
                svf.VrsOut("SCHREGNO"     , String.valueOf(schno));
                svf.VrsOut("SCHREGNAME"   , rs.getString("NAME"));
                svf.VrsOut("DATE"         , String.valueOf(param[6]) );
                //科目コード・学籍・生徒・科目名・再提出・回数・受付月日・返信日付・評価
                svf.VrsOutn("SUBCLASSCD"      ,gyo, rs.getString("SUBCLASSCD"));
                svf.VrsOutn("SUBCLASSNAME"    ,gyo, rs.getString("SUBCLASSABBV"));
                svf.VrsOutn("RECEIPT_DATE"    ,gyo, KNJ_EditDate.h_format_JP_MD(rs.getString("RECEIPT_DATE")));
                svf.VrsOutn("GRAD_DATE"       ,gyo, KNJ_EditDate.h_format_JP_MD(rs.getString("GRAD_DATE")));
                svf.VrsOutn("GRAD_VALUE"      ,gyo, rs.getString("NAME1"));
                svf.VrsOutn("ATTESTOR"        ,gyo, rs.getString("STAFFNAME"));

                if (rs.getString("REPRESENT_SEQ") != null){
                    if (!rs.getString("REPRESENT_SEQ").equals("0")){
                        seqflg = rs.getString("REPRESENT_SEQ");
                        seqflg = seqflg + String.valueOf("回");
                    }else {
                        seqflg = String.valueOf("");
                    }
                }else {
                    seqflg = String.valueOf("");
                }
                svf.VrsOutn("SAI"     ,gyo, String.valueOf(seqflg));

                if (rs.getString("STANDARD_SEQ") != null){
                    seqflg = String.valueOf("第");
                    seqflg = seqflg + rs.getString("STANDARD_SEQ");
                    seqflg = seqflg + String.valueOf("回");
                }else {
                    seqflg = String.valueOf("");
                }
                svf.VrsOutn("SEQ"     ,gyo, String.valueOf(seqflg));

                nonedata = true;
                dataflg  = true;
                gyo++;          //行数カウント用
            }
            if (dataflg){
                svf.VrEndPage();
log.debug("syuturyoku");
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
            if ("1".equals(param[7])) {
                stb.append("     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     t1.SUBCLASSCD, ");
            }
            stb.append("    t1.SCHREGNO, ");
            stb.append("    t2.NAME, ");
            stb.append("    t3.SUBCLASSABBV, ");
            stb.append("    t1.REPRESENT_SEQ, ");
            stb.append("    t1.STANDARD_SEQ, ");
            stb.append("    t1.RECEIPT_DATE, ");
            stb.append("    t1.GRAD_DATE, ");
            stb.append("    t4.NAME1, ");
            stb.append("    t5.STAFFNAME ");
            stb.append("FROM ");
            stb.append("    REP_PRESENT_DAT t1 LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
            stb.append("    LEFT JOIN SUBCLASS_MST t3 ON t1.SUBCLASSCD = t3.SUBCLASSCD ");
            if ("1".equals(param[7])) {
                stb.append("       AND t1.CLASSCD = t3.CLASSCD ");
                stb.append("       AND t1.SCHOOL_KIND = t3.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t3.CURRICULUM_CD ");
            }
            stb.append("    LEFT JOIN STAFF_MST t5 ON t1.STAFFCD = t5.STAFFCD ");
            stb.append("    LEFT JOIN V_NAME_MST t4 ON t1.GRAD_VALUE = t4.NAMECD2 AND  t4.YEAR = '"+param[0]+"' AND t4.NAMECD1 = 'M003' ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' AND ");
            stb.append("    t1.SCHREGNO = ? ");
            stb.append("ORDER BY ");
            if ("1".equals(param[7])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD, ");
            stb.append("    t1.RECEIPT_DATE, ");
            stb.append("    t1.REPRESENT_SEQ, ");
            stb.append("    t1.GRAD_DATE ");
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
