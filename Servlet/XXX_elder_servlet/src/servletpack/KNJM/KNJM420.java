// kanji=漢字
/*
 * $Id: 7b6e4e4f8a5806209fd61f578362cb91e183e4fe $
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
 *                  ＜ＫＮＪＭ４２０＞  ＳＨＲ出席チェックリスト
 *
 *  2005/04/06 m-yama 作成日
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM420 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM420.class);
    int len = 0;            //列数カウント用
    int ccnt    = 0;
    String classnm[];
    String staffnm[];
    String gradecd[];
    String hrclass[];

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[7];
//      String classnm[] = new String[7];
//      String staffnm[] = new String[7];
//      String gradecd[] = new String[7];
//      String hrclass[] = new String[7];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                                //年度
            param[1] = request.getParameter("SEMESTER");                            //学期
            param[2] = request.getParameter("CLASS");                               //クラス
            param[3] = request.getParameter("DATE");                                //日付
log.debug("class"+param[2]);
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
        PreparedStatement ps5 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJM420]param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));       //担当コードpreparestatement
            ps3 = db2.prepareStatement(Pre_Stat3(param));       //担当コードpreparestatement
            ps4 = db2.prepareStatement(Pre_Stat4(param));       //担当コードpreparestatement
            ps5 = db2.prepareStatement(Pre_Stat5(param));       //校時コードpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }
        //カウンタ
        //SVF出力
        //固定項目GET
        if (param[2].equals("0")) {
            Allclassdata(db2,svf,param,ps3);
        }else {
            Classdata(db2,svf,param,ps2);
        }

        for( int ia=0 ; ia<classnm.length ; ia++ ){
            try {
                ResultSet rs4 = ps4.executeQuery();
                rs4.next();
                param[4] = rs4.getString("CHAIRCD");
                rs4.close();
log.debug("chair"+param[4]);
log.debug("data"+classnm[ia]);
            } catch( Exception ex ) {
                log.error("SQL read error!");
            }
            if( Set_Detail_1(db2,svf,param,classnm[ia],staffnm[ia],gradecd[ia],hrclass[ia],ps1,ps5) )nonedata = true;
            if( nonedata )  svf.VrEndPage();              //種別の最終データフィールド出力
        }
    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1,ps2,ps3,ps4,ps5);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        try {
            svf.VrSetForm("KNJM420.frm", 1);
        } catch( Exception ex ){
            log.error("file open error!" + ex);
        }
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

        classnm = new String[Allcnt];
        staffnm = new String[Allcnt];
        gradecd = new String[Allcnt];
        hrclass = new String[Allcnt];

        try {
            ResultSet rs3 = ps3.executeQuery();
            while( rs3.next() ){
                classnm[ccnt] = rs3.getString("HR_NAME");
                staffnm[ccnt] = rs3.getString("STAFFNAME");
                gradecd[ccnt] = rs3.getString("GRADE");
                hrclass[ccnt] = rs3.getString("HR_CLASS");
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

        classnm = new String[Allcnt];
        staffnm = new String[Allcnt];
        gradecd = new String[Allcnt];
        hrclass = new String[Allcnt];
log.debug("classdata");

        try {
            ResultSet rs2 = ps2.executeQuery();
            while( rs2.next() ){
                classnm[ccnt] = rs2.getString("HR_NAME");
                staffnm[ccnt] = rs2.getString("STAFFNAME");
                gradecd[ccnt] = rs2.getString("GRADE");
                hrclass[ccnt] = rs2.getString("HR_CLASS");
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
    String classnm,
    String staffnm,
    String gradecd,
    String hrclass,
    PreparedStatement ps1,
    PreparedStatement ps5)
    {
        boolean nonedata = false;
        int pcnt    = 0;
        String pecd[] = new String[2];
        String penm[] = new String[2];
        try {
            ResultSet rs1 = ps5.executeQuery();
            while( rs1.next() ){
                if (pcnt < 2){
                    pecd[pcnt] = rs1.getString("PCD");
                    penm[pcnt] = rs1.getString("ABBV1");
                    pcnt++;
                }
            }
            rs1.close();
            if (pecd[0] == null) pecd[0] = "*";
            if (pecd[1] == null) pecd[1] = "*";
            ps1.setString(1,param[4]);  //講座
            ps1.setString(2,String.valueOf(pecd[0]));   //校時
            ps1.setString(3,param[4]);  //講座
            ps1.setString(4,String.valueOf(pecd[1]));   //校時
            ps1.setString(5,gradecd);   //学年
            ps1.setString(6,hrclass);   //組
            ResultSet rs = ps1.executeQuery();
log.debug("detail");
log.debug("grade"+gradecd);
log.debug("class"+hrclass);
            int gyo   = 1;          //行数カウント用
            int nom   = 0;          //出席者カウント用
            len       = 1;          //左右切り分け用
            while( rs.next() ){
                if ( gyo > 50 ){
                    gyo = 1;
                    len++;
                }
                if ( len > 2 ){
                    gyo = 1;
                    len = 1;
                    nom = 1;
                    svf.VrEndPage();                  //SVFフィールド出力
                }

                nom = nom + Integer.parseInt(rs.getString("ATTENDFLG"));
            //  組略称・担任名出力
                svf.VrsOut("DATE"                         , param[3].replace('-','/') );
                svf.VrsOut("CLASS"                        , String.valueOf(classnm) );
                svf.VrsOut("TECHNAME"                     , String.valueOf(staffnm) );
                svf.VrsOut("MAKEDAY"                      , param[6].replace('-','/') );
                if (penm[0] != null) svf.VrsOut("KOUJI1_" + String.valueOf(len), String.valueOf(penm[0]) );
                if (penm[1] != null) svf.VrsOut("KOUJI2_" + String.valueOf(len), String.valueOf(penm[1]) );
                svf.VrsOut("GOUKEI"                       , String.valueOf(nom) );
            //  出席番号・かな出力  04/11/30Modify
                svf.VrsOutn("NUMBER"  + String.valueOf(len)   ,gyo, rs.getString("ATTENDNO") );
                svf.VrsOutn("SCHNO"   + String.valueOf(len)   ,gyo, rs.getString("SCHREGNO") );
                svf.VrsOutn("NAME"    + String.valueOf(len)   ,gyo, rs.getString("NAME_SHOW") );
                svf.VrsOutn("ATEND1_" + String.valueOf(len)   ,gyo, rs.getString("AMSH") );
                svf.VrsOutn("ATEND2_" + String.valueOf(len)   ,gyo, rs.getString("PMSH") );

                //svf.VrEndRecord();
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
log.debug("chairchairchair"+param[4]);
        try {
            stb.append("WITH ATABLE(SCHNO,ATEND,PERIODCD) AS ");
            stb.append("(SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    '○' AS ATTEND, ");
            stb.append("    PERIODCD ");
            stb.append("FROM ");
            stb.append("    HR_ATTEND_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND EXECUTEDATE = '"+param[3].replace('/','-')+"' ");
            stb.append("    AND CHAIRCD = ? ");
            stb.append("    AND PERIODCD = ? ");
            stb.append("ORDER BY PERIODCD ");
            stb.append("), ");
            stb.append("BTABLE(SCHNO,ATEND,PERIODCD) AS ");
            stb.append("(SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    '○' AS ATTEND, ");
            stb.append("    PERIODCD ");
            stb.append("FROM ");
            stb.append("    HR_ATTEND_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND EXECUTEDATE = '"+param[3].replace('/','-')+"' ");
            stb.append("    AND CHAIRCD = ? ");
            stb.append("    AND PERIODCD = ? ");
            stb.append("ORDER BY PERIODCD ");
            stb.append(") ");
            stb.append("SELECT w1.ATTENDNO,w1.SCHREGNO,w2.NAME_SHOW,w3.ATEND AS AMSH,w4.ATEND AS PMSH, ");
            stb.append("    w3.PERIODCD AS ONE,w4.PERIODCD AS TWO, ");
            stb.append("    case when w3.ATEND = '○' then '1' when w4.ATEND = '○' then '1' else '0' end ATTENDFLG ");
            stb.append("FROM SCHREG_REGD_DAT w1 LEFT JOIN SCHREG_BASE_MST w2 ON w1.SCHREGNO = w2.SCHREGNO LEFT JOIN atable w3 ON w1.SCHREGNO = w3.SCHNO LEFT JOIN btable w4 ON w1.SCHREGNO = w4.SCHNO ");
            stb.append("WHERE ");
            stb.append("    w1.YEAR = '"+param[0]+"' ");
            stb.append("    AND w1.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND w1.GRADE = ? ");
            stb.append("    AND w1.HR_CLASS = ? ");
            stb.append("ORDER BY w1.ATTENDNO ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り


    /**組み指定時抽出**/
    private String Pre_Stat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    HR_NAME,TR_CD1,STAFFNAME,GRADE,HR_CLASS ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT LEFT JOIN STAFF_MST ON TR_CD1 = STAFFCD ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SEMESTER  = '"+param[1]+"' ");
            stb.append("    AND GRADE = '"+param[2].substring(0,2)+"' ");
            stb.append("    AND HR_CLASS = '"+param[2].substring(2)+"' ");

        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**全クラス抽出**/
    private String Pre_Stat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    HR_NAME,TR_CD1,STAFFNAME,GRADE,HR_CLASS ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT LEFT JOIN STAFF_MST ON TR_CD1 = STAFFCD ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SEMESTER  = '"+param[1]+"' ");
            stb.append("ORDER BY GRADE,HR_CLASS ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat3 error!");
        }
        return stb.toString();

    }//Pre_Stat3()の括り

    /**講座取得**/
    private String Pre_Stat4(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    CHAIRCD ");
            stb.append("FROM ");
            stb.append("    CHAIR_CLS_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SEMESTER  = '"+param[1]+"' ");
            stb.append("    AND CHAIRCD like '92%' ");
            stb.append("    AND TRGTGRADE = '"+param[2].substring(0,2)+"' ");
            stb.append("    AND TRGTCLASS = '"+param[2].substring(2)+"' ");
            stb.append("GROUP BY CHAIRCD ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat4 error!");
        }
        return stb.toString();

    }//Pre_Stat4()の括り


    /**校時取得**/
    private String Pre_Stat5(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    NAMECD2 AS PCD,ABBV1 ");
            stb.append("FROM ");
            stb.append("    V_NAME_MST ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND NAMECD1 = 'B001' ");
            stb.append("    AND NAMESPARE1 IS NOT NULL ");
            stb.append("ORDER BY NAMECD2 ");
log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat5 error!");
        }
        return stb.toString();

    }//Pre_Stat5()の括り


    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4,
        PreparedStatement ps5
    ) {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();
            ps5.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
