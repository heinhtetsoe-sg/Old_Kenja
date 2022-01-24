// kanji=漢字
/*
 * $Id: 08aa36b2a548540001cc677776bd88a9ca1582da $
 *
 * 作成日: 2005/08/09 11:25:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
 *  学校教育システム 賢者 [入試]  事前相談結果名簿
 *
 *  2005/08/09  作成  m-yama
 *  2005/10/26  NO001 m-yama 判定を学校・塾両方出力
 *  2006/02/02  NO002 m-yama 学校塾名称を学校・塾両方出力
 *              NO003 m-yama ソート順を追加
 *  2006/02/10  NO004 m-yama 塾/学校受付番号でリンクしていた箇所を受付番号でリンクするよう修正
 * @author m-yama
 * @version $Id: 08aa36b2a548540001cc677776bd88a9ca1582da $
 */
public class KNJL303K {

    private static final Log log = LogFactory.getLog(KNJL303K.class);
    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];
        String sqlsort = "";
        String subtitle = "";

    //  パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");       //年度
            param[1]  = request.getParameter("TESTDIV");    //試験区分
            param[2]  = request.getParameter("JHFLG");      //中高判定フラグ1:中学、2:高校
            param[10] = request.getParameter("OUTPUT");     //ソート順1:受験番号順、2:氏名＋氏名かな順、3:学校コード順、4:塾コード順 NO003
            if (param[10].equals("1")){
                sqlsort = "t1.TESTDIV,t1.ACCEPTNO";
                subtitle = "（受付番号順）";
            }else if (param[10].equals("2")){
                sqlsort = "t1.TESTDIV,t1.NAME_KANA";
                subtitle = "（かな氏名順）";
            }else if (param[10].equals("3")){
                sqlsort = "t1.TESTDIV,t1.FS_CD,t1.NAME_KANA";
                subtitle = "（出身学校順）";
            }else {
                sqlsort = "t1.TESTDIV,t1.PS_CD,t1.NAME_KANA";
                subtitle = "（塾順）";
            }
        } catch( Exception ex ) {
            log.error("Param read error!");
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
            log.error("DB2 open error!");
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJL303]param["+ia+"]="+param[ia]);

        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param,sqlsort));       //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));               //設定データpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        //SVF出力
        Set_Detail_1(db2,svf,param,ps1,ps2,subtitle);

        //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        preStatClose(ps1,ps2);      //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

    //タイトル年度
    try {
           param[8] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
    } catch( Exception e ){
        log.warn("jinendo get error!",e);
    }

    //  学校
    if (param[2].equals("1")) param[9] = "中学校";
    if (param[2].equals("2")) param[9] = "高等学校";

    //  作成日(現在処理日)の取得
    try {
        String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
        db2.query(sql);
        ResultSet rs = db2.getResultSet();
        String arr_ctrl_date[] = new String[3];
        int number = 0;
        while( rs.next() ){
            arr_ctrl_date[number] = rs.getString(1);
            number++;
        }
        rs.close();
        db2.commit();
        param[3] = KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分"+" 現在";
    } catch( Exception e ){
        log.warn("ctrl_date get error!",e);
    }
    //  試験区分の取得
    try {
        String testdiv[] = new String[2];
        String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' ORDER BY NAMECD2";
        db2.query(sql);
        ResultSet rs = db2.getResultSet();
        int i = 0;
        while( rs.next() ){
            if (i > 1) break;
            testdiv[i] = rs.getString("NAME1");
            i++;
        }
        param[6] = testdiv[0];
        param[7] = testdiv[1];
        rs.close();
        db2.commit();
    } catch( Exception e ){
        log.warn("ctrl_date get error!",e);
    }

    }//Set_Head()の括り

    /**SVF-FORM**/
    private boolean Set_Detail_1(
    DB2UDB db2,
    Vrw32alp svf,
    String param[],
    PreparedStatement ps1,
    PreparedStatement ps2,
    String subtitle)
    {
        int girlcnt = 0;            //女子カウンタ
        int mancnt  = 0;            //男子カウンタ
        int pagecnt = 1;            //現在ページ
        String beftdiv = "*" ;      //前回試験区分
        String allpage = "" ;       //最終ページ
        String pagezen = "" ;       //前期最終ページ
        String pagekou = "" ;       //後期最終ページ
        String settest = "";        //試験区分
        boolean firstflg = true;    //先頭データフラグ
        boolean endflg   = false;   //終了フラグ
        try {
            //トータルページ取得
            ResultSet rs1 = ps2.executeQuery();
            while( rs1.next() ){
                pagezen = rs1.getString("ZENKI_PAGE");
                pagekou = rs1.getString("KOUKI_PAGE");
            }
            svf.VrSetForm("KNJL303.frm", 4);
            rs1.close();

            //明細データ
            ResultSet rs = ps1.executeQuery();
            int gyo  = 1;           //行数カウント用
            while( rs.next() ){
                if (!rs.getString("TESTDIV").equalsIgnoreCase(beftdiv) && !firstflg){
                    for (;gyo < 52 ;gyo++){
                        svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                        svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                        svf.VrEndRecord();
                    }
                    gyo = 1;
                    pagecnt = 1;
                    mancnt  = 0;
                    girlcnt = 0;
                }
                //ヘッダ出力
                if (rs.getString("TESTDIV").equals("1")){
                    settest = param[6];
                    allpage = pagezen;
                }else {
                    settest = param[7];
                    allpage = pagekou;
                }
                printHeader(svf,param,pagecnt,allpage,settest,subtitle);
                if ( gyo > 50 ){
                    gyo = 1;
                    pagecnt++;
                    svf.VrEndPage();
                    endflg = true;
                }
                if (endflg){
                    svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                    svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                    svf.VrEndRecord();
                    endflg = false;
                }
                //明細出力
                printData(svf,param,rs,gyo);
                //合計用カウンタ更新
                if (rs.getString("SEX").equals("*")){
                    girlcnt++;
                }else {
                    mancnt++;
                }
                beftdiv = rs.getString("TESTDIV");
                gyo++;
                nonedata = true;
                firstflg = false;
            }
            rs.close();
            if (nonedata){
                svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                svf.VrEndRecord();
            }

        } catch( Exception ex ) {
            log.error("Set_Detail_1 read error!");
        }
        return nonedata;

    }//Set_Detail_1()の括り

    /**ヘッダーデータをセット*/
    private void printHeader(Vrw32alp svf,String param[],int pagecnt,String allpage,String settest,String subtitle)
    {
        try {
            svf.VrsOut("NENDO"        , String.valueOf(param[8]) );
            svf.VrsOut("SCHOOLDIV"    , String.valueOf(param[9]) );
            if (settest != null) svf.VrsOut("TESTDIV"     , String.valueOf(settest) );
            svf.VrsOut("SUBTITLE"     , String.valueOf(subtitle) );
            svf.VrsOut("DATE"         , String.valueOf(param[3]) );
            svf.VrsOut("TOTAL_PAGE"   , allpage );
            svf.VrsOut("PAGE"         , String.valueOf(pagecnt) );
            if (param[2].equals("2")){
                svf.VrsOut("FIN_PRI_DIV"  , "出身学校又は塾名" );
            }else {
                svf.VrsOut("FIN_PRI_DIV"  , "出身塾又は学校名" );
            }
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り

    /**明細データをセット*/
    private void printData(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len1 = "0";
        try {
            svf.VrsOut("ACCEPTNO" ,rs.getString("ACCEPTNO"));
            svf.VrsOut("FINSCHOOLNAME"    ,rs.getString("FINSCHOOL_NAME"));   //NO002
            svf.VrsOut("PRISCHOOLNAME"    ,rs.getString("PRISCHOOL_NAME"));   //NO002
            svf.VrsOut("UPDATE"   ,rs.getString("CREATE_DATE"));
            if (rs.getString("NAME") != null){
                len1 = (10 < (rs.getString("NAME").length())) ? "2" : "1" ;
                svf.VrsOut("NAME"+len1    ,rs.getString("NAME"));
            }
            if (rs.getString("NAME_KANA") != null){
                len1 = (10 < (rs.getString("NAME_KANA").length())) ? "2" : "1" ;
                svf.VrsOut("KANA"+len1    ,rs.getString("NAME_KANA"));
            }
            svf.VrsOut("SEX"          ,rs.getString("SEX"));
            svf.VrsOut("ORG_MAJOR1"   ,rs.getString("FSJUDG"));   //NO001
            svf.VrsOut("ORG_MAJOR2"   ,rs.getString("PSJUDG"));   //NO001
            svf.VrEndRecord();
        } catch( Exception ex ) {
            log.warn("printData read error!",ex);
        }

    }//printData()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[],String sqlsort)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH FSDATA AS ( ");
            stb.append("SELECT ");
            stb.append("    ACCEPTNO,DATADIV,SHDIV,WISHNO,TESTDIV, ");
            stb.append("    n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
            stb.append("    LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ");
            stb.append("    AND n1.NAMECD1 = 'L002' ");
            stb.append("    LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ");
            stb.append("    AND n2.NAMECD1 = 'L006' ");
            stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
            stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND t1.DATADIV = '1' ");
            //塾データ
            stb.append("),PSDATA AS ( ");
            stb.append("SELECT ");
            stb.append("    ACCEPTNO,DATADIV,SHDIV,WISHNO,TESTDIV, ");
            stb.append("    n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
            stb.append("    LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ");
            stb.append("    AND n1.NAMECD1 = 'L002' ");
            stb.append("    LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ");
            stb.append("    AND n2.NAMECD1 = 'L006' ");
            stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
            stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND t1.DATADIV = '2' ");
            stb.append(") ");
            //メイン
            stb.append("SELECT ");
            stb.append("    t1.ACCEPTNO,t1.FS_CD,t1.TESTDIV, ");
            stb.append("    t2.FINSCHOOL_NAME,t5.PRISCHOOL_NAME, ");    //NO002
            stb.append("    t1.CREATE_DATE,t1.NAME,t1.NAME_KANA,CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("    CASE WHEN f1.MAJOR IS NULL THEN ' ' ELSE f1.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f1.SHNAME IS NULL THEN ' ' ELSE f1.SHNAME END || ");
            }
            stb.append("    CASE WHEN f1.JUDG IS NULL THEN ' ・' ELSE f1.JUDG || '・' END || ");
            stb.append("    CASE WHEN f2.MAJOR IS NULL THEN ' ' ELSE f2.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f2.SHNAME IS NULL THEN ' ' ELSE f2.SHNAME END || ");
            }
            stb.append("    CASE WHEN f2.JUDG IS NULL THEN ' ・' ELSE f2.JUDG || '・' END || ");
            stb.append("    CASE WHEN f3.MAJOR IS NULL THEN ' ' ELSE f3.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f3.SHNAME IS NULL THEN ' ' ELSE f3.SHNAME END || ");
            }
            stb.append("    CASE WHEN f3.JUDG IS NULL THEN ' ・' ELSE f3.JUDG || '・' END || ");
            stb.append("    CASE WHEN f4.MAJOR IS NULL THEN ' ' ELSE f4.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f4.SHNAME IS NULL THEN ' ' ELSE f4.SHNAME END || ");
            }
            stb.append("    CASE WHEN f4.JUDG IS NULL THEN ' ' ELSE f4.JUDG ");
            stb.append("    END FSJUDG, "); //NO001
            stb.append("    CASE WHEN p1.MAJOR IS NULL THEN ' ' ELSE p1.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p1.SHNAME IS NULL THEN ' ' ELSE p1.SHNAME END || ");
            }
            stb.append("    CASE WHEN p1.JUDG IS NULL THEN ' ・' ELSE p1.JUDG || '・' END || ");
            stb.append("    CASE WHEN p2.MAJOR IS NULL THEN ' ' ELSE p2.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p2.SHNAME IS NULL THEN ' ' ELSE p2.SHNAME END || ");
            }
            stb.append("    CASE WHEN p2.JUDG IS NULL THEN ' ・' ELSE p2.JUDG || '・' END || ");
            stb.append("    CASE WHEN p3.MAJOR IS NULL THEN ' ' ELSE p3.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p3.SHNAME IS NULL THEN ' ' ELSE p3.SHNAME END || ");
            }
            stb.append("    CASE WHEN p3.JUDG IS NULL THEN ' ・' ELSE p3.JUDG || '・' END || ");
            stb.append("    CASE WHEN p4.MAJOR IS NULL THEN ' ' ELSE p4.MAJOR END || ");
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p4.SHNAME IS NULL THEN ' ' ELSE p4.SHNAME END || ");
            }
            stb.append("    CASE WHEN p4.JUDG IS NULL THEN ' ' ELSE p4.JUDG ");
            stb.append("    END PSJUDG ");  //NO001
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT t1 ");
            stb.append("    LEFT JOIN FINSCHOOL_MST t2 ON t2.FINSCHOOLCD = t1.FS_CD ");
            stb.append("    LEFT JOIN PRISCHOOL_MST t5 ON t5.PRISCHOOLCD = t1.PS_CD ");
            stb.append("    LEFT JOIN FSDATA f1 ON f1.ACCEPTNO = t1.ACCEPTNO AND f1.WISHNO = '1' AND f1.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN FSDATA f2 ON f2.ACCEPTNO = t1.ACCEPTNO AND f2.WISHNO = '2' AND f2.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN FSDATA f3 ON f3.ACCEPTNO = t1.ACCEPTNO AND f3.WISHNO = '3' AND f3.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN FSDATA f4 ON f4.ACCEPTNO = t1.ACCEPTNO AND f4.WISHNO = '4' AND f4.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN PSDATA p1 ON p1.ACCEPTNO = t1.ACCEPTNO AND p1.WISHNO = '1' AND p1.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN PSDATA p2 ON p2.ACCEPTNO = t1.ACCEPTNO AND p2.WISHNO = '2' AND p2.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN PSDATA p3 ON p3.ACCEPTNO = t1.ACCEPTNO AND p3.WISHNO = '3' AND p3.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("    LEFT JOIN PSDATA p4 ON p4.ACCEPTNO = t1.ACCEPTNO AND p4.WISHNO = '4' AND p4.TESTDIV = t1.TESTDIV ");    //NO007
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("ORDER BY "+sqlsort+" ");

log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH ZENKIPAGE AS ( ");
            stb.append("SELECT ");
            stb.append("    CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END ZENKI_PAGE ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '1' ");
            stb.append("),KOUKIPAGE AS ( ");
            stb.append("SELECT ");
            stb.append("    CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END KOUKI_PAGE ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '2' ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    ZENKIPAGE, ");
            stb.append("    KOUKIPAGE ");

//log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            ps1.close();
            ps2.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
