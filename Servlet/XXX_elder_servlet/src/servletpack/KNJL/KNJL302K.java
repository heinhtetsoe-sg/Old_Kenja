// kanji=漢字
/*
 * $Id: 4da1a4b2322cc18034f70e80a2ddee4a7c823e23 $
 *
 * 作成日: 2005/08/07 11:25:40 - JST
 * 作成者: m-yama
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

//public class KNJL302 extends HttpServlet {
/**
 *
 *  学校教育システム 賢者 [入試]  志願者/事前相談データ突合せリスト(中学用)
 *
 *  2005/08/07  作成  m-yama
 *  2005/08/16  NO001 m-yama 帳票追加
 *  2005/09/03  NO002 m-yama 総ページ出力なし
 *  2006/01/05  NO003 o-naka 事前相談不参加者リスト 附属出身者(3000番台)は除くよう修正
 *  2006/01/10  NO004 m-yama SQLを修正
 *  2006/01/11  NO005 m-yama 全リスト 附属出身者(3000番台)は除くよう修正
 *  2006/01/17  NO006 m-yama 頁の表示をフォームで行う
 *  2006/02/10  NO007 m-yama 塾/学校受付番号でリンクしていた箇所を受付番号でリンクするよう修正
 * @author m-yama
 * @version $Id: 4da1a4b2322cc18034f70e80a2ddee4a7c823e23 $
 */
public class KNJL302K {

    private static final Log log = LogFactory.getLog(KNJL302K.class);
    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[11];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("TESTDIV");         //試験区分
            param[2] = request.getParameter("JHFLG");           //中高判定フラグ1:中学、2:高校
            param[4] = request.getParameter("OUTPUT");          //重複チェック氏名
            param[10] = request.getParameter("SPECIAL_REASON_DIV");
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
        PreparedStatement ps3 = null;
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJL302]param["+ia+"]="+param[ia]);

        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));   //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param));               //設定データpreparestatement
            ps3 = db2.prepareStatement(Pre_Stat3(param));               //設定データpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        //SVF出力
        if (param[4].equals("4") || param[4].equals("6")){  //NO001
            Set_Detail_2(db2,svf,param,ps1,ps2);
        }else {
            Set_Detail_1(db2,svf,param,ps1,ps2,ps3);
        }

        //  該当データ無し
        if( !nonedata ){
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

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[10] + "' ");
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
        param[7] = "";
        if (!param[1].equals("99")){
            String sql = "SELECT ABBV1,NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '"+param[1]+"'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                param[6] = rs.getString("ABBV1") + "志";
                param[7] = rs.getString("NAME1");
            }
            rs.close();
            db2.commit();
        }
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
    PreparedStatement ps3)
    {
        boolean firstflg = true;    //先頭データフラグ
        boolean endflg   = false;   //終了フラグ
        int girlcnt = 0;            //女子カウンタ
        int mancnt  = 0;            //男子カウンタ
        int rennum  = 1;            //連番
        String bfrexam = "*" ;      //最終データ
        try {
            //トータルページ取得
            svf.VrSetForm("KNJL302.frm", 4);
            setInfluenceName(db2, svf, param);

            //明細データ
            ResultSet rs = ps1.executeQuery();
            int gyo  = 1;           //行数カウント用
            while( rs.next() ){
                //ヘッダ出力
                printHeader(svf,param);
                if ( gyo > 50 ){
                    gyo = 1;
                    endflg = true;
                }

                if (!rs.getString("EXAMNO").equalsIgnoreCase(bfrexam)){
                    if (!firstflg){
                        rennum++;
                    }
                    if (endflg){
                        svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                        svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                        svf.VrEndRecord();
                        endflg = false;
                    }
                    //検索対象データ
                    printDataMain(db2,svf,param,rs,rennum,ps3);
                    gyo++;          //行数カウント用
                    if (rs.getString("SEX").equals("*")){
                        girlcnt++;
                    }else {
                        mancnt++;
                    }
                    if ( gyo > 50 ){
                        gyo = 1;
                        endflg = true;
                    }
                    if (endflg){
                        svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                        svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                        svf.VrEndRecord();
                        endflg = false;
                    }
                    //検索データ
                    printDataSub(svf,param,rs,rennum);
                    if (rs.getString("SEX").equals("*")){
                        girlcnt++;
                    }else {
                        mancnt++;
                    }
                    gyo++;          //行数カウント用
                    bfrexam = rs.getString("EXAMNO");
                }else {
                    if (endflg){
                        svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                        svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                        svf.VrEndRecord();
                        endflg = false;
                    }
                    //検索データ
                    printDataSub(svf,param,rs,rennum);
                    gyo++;          //行数カウント用
                    bfrexam = rs.getString("EXAMNO");
                    if (rs.getString("SEX").equals("*")){
                        girlcnt++;
                    }else {
                        mancnt++;
                    }
                }
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

    /**SVF-FORM**/
    private boolean Set_Detail_2(
    DB2UDB db2,
    Vrw32alp svf,
    String param[],
    PreparedStatement ps1,
    PreparedStatement ps2)
    {
        int girlcnt = 0;            //女子カウンタ
        int mancnt  = 0;            //男子カウンタ
        String beftest = "*" ;      //試験区分
        try {
            svf.VrSetForm("KNJL302_2.frm", 1);
            setInfluenceName(db2, svf, param);

            //明細データ
            ResultSet rs = ps1.executeQuery();
            int gyo  = 1;           //行数カウント用
            while( rs.next() ){

                if (beftest.equals("*")){
                    String sql3 = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '"+rs.getString("TESTDIV")+"' ORDER BY NAMECD2 ";
                    db2.query(sql3);
                    ResultSet rs3 = db2.getResultSet();
                    while( rs3.next() ){
                        param[7] = rs3.getString("NAME1");
                    }
                    rs3.close();
                    db2.commit();
                }
                if (!beftest.equals("*") && !beftest.equalsIgnoreCase(rs.getString("TESTDIV"))){
                    svf.VrsOut("TOTAL_MEMBER"     , "男"+String.valueOf(mancnt)+"名,女"+String.valueOf(girlcnt)+"名,合計"+String.valueOf(girlcnt+mancnt)+"名");
                    gyo = 1;
                    mancnt = 0;
                    girlcnt = 0;
                    svf.VrEndPage();

                    String sql2 = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '"+rs.getString("TESTDIV")+"' ORDER BY NAMECD2 ";
                    db2.query(sql2);
                    ResultSet rs2 = db2.getResultSet();
                    while( rs2.next() ){
                        param[7] = rs2.getString("NAME1");
                    }
                    rs2.close();
                    db2.commit();
                }
                if ( gyo > 50 ){
                    gyo = 1;
                    svf.VrEndPage();
                }
                //ヘッダ出力
                printHeader(svf,param);
                //明細データ
                printHdatNasi(svf,param,rs,gyo);
                gyo++;          //行数カウント用
                if (rs.getString("SEX").equals("*")){
                    girlcnt++;
                }else {
                    mancnt++;
                }
                beftest = rs.getString("TESTDIV");
                nonedata = true;
            }
            rs.close();
            if (nonedata){
                svf.VrsOut("TOTAL_MEMBER"     , "男"+String.valueOf(mancnt)+"名,女"+String.valueOf(girlcnt)+"名,合計"+String.valueOf(girlcnt+mancnt)+"名");
                svf.VrEndPage();
            }

        } catch( Exception ex ) {
            log.error("Set_Detail_2 read error!");
        }
        return nonedata;

    }//Set_Detail_2()の括り

    /**ヘッダーデータをセット*/
    private void printHeader(Vrw32alp svf,String param[])
    {
        try {
            svf.VrsOut("NENDO"        , String.valueOf(param[8]) );
            svf.VrsOut("SCHOOLDIV"    , String.valueOf(param[9]) );
            svf.VrsOut("TESTDIV"      , String.valueOf(param[7]) );
            if (param[4].equals("1")){
                svf.VrsOut("TITLE"        , "志願者/事前相談データ突合リスト" );
                svf.VrsOut("SUBTITLE"     , "（氏名○、かな氏名−、試験区分−）" );
            }else if (param[4].equals("2")){
                svf.VrsOut("TITLE"        , "志願者/事前相談データ突合リスト" );
                svf.VrsOut("SUBTITLE"     , "（氏名○、かな氏名○、試験区分○）" );
            }else if (param[4].equals("3")){
                svf.VrsOut("TITLE"        , "志願者/事前相談データ突合リスト" );
                svf.VrsOut("SUBTITLE"     , "（氏名○、かな氏名○、試験区分×）" );
            }else if (param[4].equals("4")){
                svf.VrsOut("TITLE"        , "事前相談不参加者リスト" );
                svf.VrsOut("SUBTITLE"     , "（氏名−、かな氏名−、試験区分○）" );
                svf.VrsOut("ITEM_NO"      , "受験番号" );
                svf.VrsOut("ITEM_MAJOR"   , "志望パターン" );
                svf.VrsOut("ITEM_SCHOOL"  , "学校区分" );
            }else if (param[4].equals("5")){
                svf.VrsOut("TITLE"        , "氏名又はかな氏名重複リスト" );
                svf.VrsOut("SUBTITLE"     , "（氏名○、又はかな氏名○、試験区分−）" );
            }else {
                svf.VrsOut("TITLE"        , "事前相談未出願者リスト" );
                svf.VrsOut("SUBTITLE"     , "（氏名−、かな氏名−、試験区分−）" );
                svf.VrsOut("ITEM_NO"      , "受付番号" );
                svf.VrsOut("ITEM_MAJOR"   , "判定" );
                svf.VrsOut("ITEM_SCHOOL"  , "塾又は学校名" );
            }
            svf.VrsOut("DATE"         , String.valueOf(param[3]) );
            if (param[2].equals("2") && !param[4].equals("4")){
                svf.VrsOut("FIN_PRI_DIV"  , "出身学校" );
            }else {
                svf.VrsOut("FIN_PRI_DIV"  , "出身塾" );
            }
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り

    /**検索対象データをセット*/
    private void printDataMain(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int rennum,PreparedStatement ps3)
    {
        String len1 = "0";
        String len2 = "0";
        try {
            ps3.setString(1,rs.getString("EXAMNO"));    //受験番号
            ResultSet rs2 = ps3.executeQuery();
            while( rs2.next() ){
                svf.VrsOut("NUMBER"   , String.valueOf(rennum));
                svf.VrsOut("DESIREDIV", param[6]);
                svf.VrsOut("ACCEPTNO" , rs2.getString("EXAMNO"));
                if (rs2.getString("NAME") != null){
                    len1 = (10 < (rs2.getString("NAME").length())) ? "2" : "1" ;
                    svf.VrsOut("NAME"+len1    , rs2.getString("NAME"));
                }
                if (rs2.getString("NAME_KANA") != null){
                    len2 = (10 < (rs2.getString("NAME_KANA").length())) ? "2" : "1" ;
                    svf.VrsOut("KANA"+len2    , rs2.getString("NAME_KANA"));
                }
                svf.VrsOut("SEX"          , rs2.getString("SEX"));
                svf.VrsOut("ORG_MAJOR"    , rs2.getString("JUDG"));
                svf.VrsOut("NATPUBPRIDIV" , rs2.getString("SCLNAME"));
            }
            rs2.close();
            db2.commit();

            svf.VrEndRecord();
        } catch( Exception ex ) {
            log.warn("printDataMain read error!",ex);
        }

    }//printDataMain()の括り

    /**検索一致データをセット*/
    private void printDataSub(Vrw32alp svf,String param[],ResultSet rs,int rennum)
    {
        String len1 = "0";
        String len2 = "0";
        try {
            svf.VrsOut("NUMBER"   , String.valueOf(rennum));
            svf.VrsOut("DESIREDIV", rs.getString("DESIRENAME"));
            if (rs.getString("ACCEPTNO").equals("BB")){
                svf.VrsOut("ACCEPTNO" , rs.getString("TEXAM"));
            }else {
                svf.VrsOut("ACCEPTNO" , rs.getString("ACCEPTNO"));
            }
            if (param[4].equals("5")){
                if (rs.getString("NAME2") != null){
                    len1 = (10 < (rs.getString("NAME2").length())) ? "2" : "1" ;
                    svf.VrsOut("NAME"+len1    , rs.getString("NAME2"));
                }
            }else {
                if (rs.getString("NAME") != null){
                    len1 = (10 < (rs.getString("NAME").length())) ? "2" : "1" ;
                    svf.VrsOut("NAME"+len1    , rs.getString("NAME"));
                }
            }
            if (rs.getString("KANA2") != null){
                len2 = (10 < (rs.getString("KANA2").length())) ? "2" : "1" ;
                svf.VrsOut("KANA"+len2    , rs.getString("KANA2"));
            }
            svf.VrsOut("SEX"          , rs.getString("SEX"));
            svf.VrsOut("ORG_MAJOR"    , rs.getString("JUDG"));
            svf.VrsOut("NATPUBPRIDIV" , rs.getString("SCLNAME"));
            svf.VrEndRecord();
        } catch( Exception ex ) {
            log.warn("printDataSub read error!",ex);
        }

    }//printDataSub()の括り

    /**検索一致データをセット*/
    private void printHdatNasi(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len1 = "0";
        String len2 = "0";
        try {
            if (param[4].equals("6")){
                svf.VrsOutn("EXAMNO"  ,gyo    , rs.getString("ACCEPTNO"));
            }else {
                svf.VrsOutn("EXAMNO"  ,gyo    , rs.getString("EXAMNO"));
            }
            if (rs.getString("NAME") != null){
                len1 = (10 < (rs.getString("NAME").length())) ? "2" : "1" ;
                svf.VrsOutn("NAME"+len1   ,gyo    , rs.getString("NAME"));
            }
            if (rs.getString("NAME_KANA") != null){
                len2 = (10 < (rs.getString("NAME_KANA").length())) ? "2" : "1" ;
                svf.VrsOutn("KANA"+len2   ,gyo    , rs.getString("NAME_KANA"));
            }
            svf.VrsOutn("SEX"         ,gyo    , rs.getString("SEX"));
            svf.VrsOutn("ORG_MAJOR"   ,gyo    , rs.getString("JUDG"));
            svf.VrsOutn("NATPUBPRIDIV"    ,gyo    , rs.getString("SCLNAME"));
        } catch( Exception ex ) {
            log.warn("printDataSub read error!",ex);
        }

    }//printDataSub()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH NOSERCH AS (SELECT ");
            stb.append("    t1.EXAMNO AS NOEXAM ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT t1, ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT t2 ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[10])) {
                stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
            }
            if (!param[1].equals("99")){
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("    AND t2.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("    AND (t1.EXAMNO < '3000' OR '3999' < t1.EXAMNO) ");//NO003

            stb.append("    AND value(t1.NAME,'') || value(t1.NAME_KANA,'') = value(t2.NAME,'') || value(t2.NAME_KANA,'') ");   //NO004
            stb.append("), DESIRETBL1 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV1, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK1 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '1' ");
            stb.append("),DESIRETBL2 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV2, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK2 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '2' ");
            stb.append("),DESIRETBL3 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV3, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK3 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '3' ");
            stb.append("),DESIRETBL4 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV4, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK4 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '4' ");
            stb.append("),DESIRETBL AS ( ");
            stb.append("SELECT DESIREDIV1 AS DESIREDIV, ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK1 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK1,'') END || ");   //NO004
            stb.append("    CASE WHEN EXAMCOURSE_MARK2 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK2,'') END || ");   //NO004
            stb.append("    CASE WHEN EXAMCOURSE_MARK3 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK3,'') END || ");   //NO004
            stb.append("    CASE WHEN EXAMCOURSE_MARK4 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK4,'') END AS EXAMCOURSE_MARK ");   //NO004
            stb.append("FROM   DESIRETBL1 t1 ");
            stb.append("       LEFT JOIN DESIRETBL2 ON DESIREDIV2 = DESIREDIV1 ");
            stb.append("       LEFT JOIN DESIRETBL3 ON DESIREDIV3 = DESIREDIV1 ");
            stb.append("       LEFT JOIN DESIRETBL4 ON DESIREDIV4 = DESIREDIV1 ");
            stb.append("),EXAM_BASE AS ( ");
            stb.append("SELECT EXAMNO,value(NAME,'') AS NAME,value(NAME_KANA,'') AS NAME_KANA,t1.DESIREDIV,t1.TESTDIV, ");  //NO004
            stb.append("       EXAMCOURSE_MARK AS JUDG,t3.NAME1 AS SCLNAME,t4.ABBV1 || '志' AS DESIRENAME, ");
            stb.append("       CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX ");
            stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("       LEFT JOIN DESIRETBL t2 ON t1.DESIREDIV = t2.DESIREDIV ");
            stb.append("       LEFT JOIN NAME_MST t3 ON t3.NAMECD2 = t1.NATPUBPRIDIV ");
            stb.append("       AND NAMECD1 = 'L004' ");
            stb.append("       LEFT JOIN NAME_MST t4 ON t4.NAMECD2 = t1.TESTDIV ");
            stb.append("       AND t4.NAMECD1 = 'L003' ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            if (!"9".equals(param[10])) {
                stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
            }
            if (!param[1].equals("99")){
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            }
            if (param[4].equals("3")){
                stb.append("       AND EXAMNO NOT IN (SELECT NOEXAM FROM NOSERCH) ");
            }
            stb.append("    AND (EXAMNO < '3000' OR '3999' < EXAMNO) ");//NO003
            if (param[4].equals("4")){
                stb.append("), HDATMAIN AS ( ");
                stb.append("SELECT value(NAME,'') AS NAME,value(NAME_KANA,'') AS NAME_KANA ");  //NO004
                stb.append("FROM   ENTEXAM_CONSULTATION_HDAT ");
                stb.append("WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
                if (!param[1].equals("99")){
                    stb.append("       AND TESTDIV = '"+param[1]+"' ");
                }
                stb.append("), HDATSUB AS ( ");
                stb.append("SELECT value(NAME,'') AS NAME,value(NAME_KANA,'') AS NAME_KANA ");  //NO004
                stb.append("FROM   ENTEXAM_CONSULTATION_HDAT ");
                stb.append("WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
                if (!param[1].equals("99")){
                    stb.append("       AND TESTDIV <> '"+param[1]+"' ");
                }
                stb.append(") ");
                stb.append("SELECT ");
                stb.append("    * ");
                stb.append("FROM ");
                stb.append("    EXAM_BASE ");
                stb.append("WHERE ");
                stb.append("    value(NAME,'') || value(NAME_KANA,'') NOT IN (SELECT value(NAME,'') || value(NAME_KANA,'') FROM HDATMAIN) ");       //NO004
                stb.append("    AND value(NAME,'') || value(NAME_KANA,'') NOT IN (SELECT value(NAME,'') || value(NAME_KANA,'') FROM HDATSUB) ");    //NO004
                stb.append("ORDER BY NAME_KANA ");
            }else {
                stb.append("),FSDATA1 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '1' ");
                stb.append("    AND t1.WISHNO = '1' ");
                stb.append("),FSDATA2 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '1' ");
                stb.append("    AND t1.WISHNO = '2' ");
                stb.append("),FSDATA3 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '1' ");
                stb.append("    AND t1.WISHNO = '3' ");
                stb.append("),FSDATA4 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '1' ");
                stb.append("    AND t1.WISHNO = '4' ");
                stb.append("),PSDATA1 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '2' ");
                stb.append("    AND t1.WISHNO = '1' ");
                stb.append("),PSDATA2 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '2' ");
                stb.append("    AND t1.WISHNO = '2' ");
                stb.append("),PSDATA3 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '2' ");
                stb.append("    AND t1.WISHNO = '3' ");
                stb.append("),PSDATA4 AS ( ");
                stb.append("SELECT ");
                stb.append("    ACCEPTNO,DATADIV,NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
                stb.append("    LEFT JOIN NAME_MST ON NAMECD2 = JUDGEMENT ");
                stb.append("    AND NAMECD1 = 'L002' ");
                stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
                stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t1.DATADIV = '2' ");
                stb.append("    AND t1.WISHNO = '4' ");
                stb.append("),FSDATAALL AS ( ");
                stb.append("SELECT ");
                stb.append("    t1.ACCEPTNO AS FSACC,t1.DATADIV FSDATA, ");
                stb.append("    CASE WHEN t1.MAJOR IS NULL THEN '' ELSE t1.MAJOR END || ");
                stb.append("    CASE WHEN t2.MAJOR IS NULL THEN '' ELSE t2.MAJOR END || ");
                stb.append("    CASE WHEN t3.MAJOR IS NULL THEN '' ELSE t3.MAJOR END || ");
                stb.append("    CASE WHEN t4.MAJOR IS NULL THEN '' ELSE t4.MAJOR END FSMAJOR, ");
                stb.append("    CASE WHEN t1.MAJOR IS NULL THEN ' ' ELSE t1.MAJOR END || ");
                stb.append("    CASE WHEN t1.JUDG IS NULL THEN '-・' ELSE t1.JUDG || '・' END || ");
                stb.append("    CASE WHEN t2.MAJOR IS NULL THEN ' ' ELSE t2.MAJOR END || ");
                stb.append("    CASE WHEN t2.JUDG IS NULL THEN '-・' ELSE t2.JUDG || '・' END || ");
                stb.append("    CASE WHEN t3.MAJOR IS NULL THEN ' ' ELSE t3.MAJOR END || ");
                stb.append("    CASE WHEN t3.JUDG IS NULL THEN '-・' ELSE t3.JUDG || '・' END || ");
                stb.append("    CASE WHEN t4.MAJOR IS NULL THEN '-' ELSE t4.MAJOR END || ");
                stb.append("    CASE WHEN t4.JUDG IS NULL THEN '-' ELSE t4.JUDG END FSJUDG ");
                stb.append("FROM ");
                stb.append("    FSDATA1 t1 ");
                stb.append("    LEFT JOIN FSDATA2 t2 ON t2.ACCEPTNO = t1.ACCEPTNO ");
                stb.append("    LEFT JOIN FSDATA3 t3 ON t3.ACCEPTNO = t1.ACCEPTNO ");
                stb.append("    LEFT JOIN FSDATA4 t4 ON t4.ACCEPTNO = t1.ACCEPTNO ");
                stb.append("GROUP BY ");
                stb.append("    t1.ACCEPTNO,t1.DATADIV,t1.MAJOR,t2.MAJOR,t3.MAJOR,t4.MAJOR, ");
                stb.append("    t1.JUDG,t2.JUDG,t3.JUDG,t4.JUDG ");
                stb.append("),PSDATAALL AS ( ");
                stb.append("SELECT ");
                stb.append("    t1.ACCEPTNO AS PSACC,t1.DATADIV PSDATA, ");
                stb.append("    CASE WHEN t1.MAJOR IS NULL THEN '' ELSE value(t1.MAJOR,'') END || ");           //NO004
                stb.append("    CASE WHEN t2.MAJOR IS NULL THEN '' ELSE value(t2.MAJOR,'') END || ");           //NO004
                stb.append("    CASE WHEN t3.MAJOR IS NULL THEN '' ELSE value(t3.MAJOR,'') END || ");           //NO004
                stb.append("    CASE WHEN t4.MAJOR IS NULL THEN '' ELSE value(t4.MAJOR,'') END PSMAJOR, ");     //NO004
                stb.append("    CASE WHEN t1.MAJOR IS NULL THEN ' ' ELSE value(t1.MAJOR,'') END || ");          //NO004
                stb.append("    CASE WHEN t1.JUDG IS NULL THEN '-・' ELSE value(t1.JUDG,'') || '・' END || ");    //NO004
                stb.append("    CASE WHEN t2.MAJOR IS NULL THEN ' ' ELSE value(t2.MAJOR,'') END || ");          //NO004
                stb.append("    CASE WHEN t2.JUDG IS NULL THEN '-・' ELSE value(t2.JUDG,'') || '・' END || ");    //NO004
                stb.append("    CASE WHEN t3.MAJOR IS NULL THEN ' ' ELSE value(t3.MAJOR,'') END || ");      //NO004
                stb.append("    CASE WHEN t3.JUDG IS NULL THEN '-・' ELSE value(t3.JUDG,'') || '・' END || ");    //NO004
                stb.append("    CASE WHEN t4.MAJOR IS NULL THEN '-' ELSE value(t4.MAJOR,'') END || ");      //NO004
                stb.append("    CASE WHEN t4.JUDG IS NULL THEN '-' ELSE  value(t4.JUDG,'') END PSJUDG ");       //NO004
                stb.append("FROM ");
                stb.append("    PSDATA1 t1 ");
                stb.append("    LEFT JOIN PSDATA2 t2 ON t2.ACCEPTNO = t1.ACCEPTNO ");
                stb.append("    LEFT JOIN PSDATA3 t3 ON t3.ACCEPTNO = t1.ACCEPTNO ");
                stb.append("    LEFT JOIN PSDATA4 t4 ON t4.ACCEPTNO = t1.ACCEPTNO ");
                stb.append("GROUP BY ");
                stb.append("    t1.ACCEPTNO,t1.DATADIV,t1.MAJOR,t2.MAJOR,t3.MAJOR,t4.MAJOR, ");
                stb.append("    t1.JUDG,t2.JUDG,t3.JUDG,t4.JUDG ");
                stb.append("),EXAM_CON AS ( ");
                stb.append("SELECT ");
                stb.append("    t1.ACCEPTNO,t1.FS_CD,t1.TESTDIV, ");
                stb.append("    CASE WHEN PSACC IS NULL THEN t2.FINSCHOOL_NAME ELSE t5.PRISCHOOL_NAME END SCLNAME, ");
                stb.append("    t1.CREATE_DATE,t6.ABBV1 || '事' AS DESIRENAME, ");
                stb.append("    value(t1.NAME,'') AS NAME,value(t1.NAME_KANA,'') AS NAME_KANA,CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ");  //NO004
                stb.append("    CASE WHEN PSACC IS NULL THEN FSMAJOR ELSE PSMAJOR END MAJOR, ");
                stb.append("    CASE WHEN PSACC IS NULL THEN FSJUDG ELSE PSJUDG END JUDG ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT t1 ");
                stb.append("    LEFT JOIN FINSCHOOL_MST t2 ON t2.FINSCHOOLCD = t1.FS_CD ");
                stb.append("    LEFT JOIN PRISCHOOL_MST t5 ON t5.PRISCHOOLCD = t1.PS_CD ");
                stb.append("    LEFT JOIN FSDATAALL t3 ON t3.FSACC = t1.ACCEPTNO ");    //NO007
                stb.append("    LEFT JOIN PSDATAALL t4 ON t4.PSACC = t1.ACCEPTNO ");    //NO007
                stb.append("    LEFT JOIN NAME_MST t6 ON t6.NAMECD2 = t1.TESTDIV ");
                stb.append("    AND t6.NAMECD1 = 'L003' ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (param[4].equals("2")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                if (param[4].equals("6")){
                    stb.append(") ");
                    stb.append("SELECT ");
                    stb.append("    * ");
                    stb.append("FROM ");
                    stb.append("    EXAM_CON ");
                    stb.append("WHERE ");
                    stb.append("    value(NAME,'') || value(NAME_KANA,'') NOT IN (SELECT value(NAME,'') || value(NAME_KANA,'') FROM EXAM_BASE) ");  //NO004
                    stb.append("ORDER BY TESTDIV,NAME_KANA ");
                }else {
                    stb.append("    ) ");
                    stb.append("    SELECT T2.TESTDIV,T1.EXAMNO,value(T1.NAME,'') AS NAME,value(T1.NAME_KANA,'') AS NAME_KANA,T1.SEX,T2.DESIRENAME, "); //NO004
                    stb.append("           T2.ACCEPTNO,'CC' AS TEXAM,value(T2.NAME,'') AS NAME2,value(T2.NAME_KANA,'') AS KANA2, ");    //NO004
                    stb.append("           T2.SEX,T2.SCLNAME,T2.JUDG ");
                    stb.append("    FROM   EXAM_BASE T1,EXAM_CON T2 ");
                    if (param[4].equals("5")){
                        stb.append("    WHERE  (value(T1.NAME,'') = value(T2.NAME,'') AND value(T1.NAME_KANA,'') <> value(T2.NAME_KANA,''))  ");    //NO004
                        stb.append("    OR (value(T1.NAME,'') <> value(T2.NAME,'') AND value(T1.NAME_KANA,'') = value(T2.NAME_KANA,'')) "); //NO004
                    }else {
                        stb.append("    WHERE  value(T1.NAME,'')=value(T2.NAME,'') ");  //NO004
                        if (param[4].equals("1")){
                        }else if(param[4].equals("2")){
                            stb.append("       AND value(T1.NAME_KANA,'')=value(T2.NAME_KANA,'') ");    //NO004
                            stb.append("       AND T1.TESTDIV=T2.TESTDIV ");
                        }else {
                            stb.append("       AND value(T1.NAME_KANA,'')=value(T2.NAME_KANA,'') ");    //NO004
                            stb.append("       AND T1.TESTDIV<>T2.TESTDIV ");
                        }
                    }
                    if (!param[4].equals("2") && !param[4].equals("5")){
                        stb.append("    UNION ALL ");
                        stb.append("    SELECT W1.TESTDIV,W1.EXAMNO,value(W1.NAME,'') AS NAME,value(W1.NAME_KANA,'') AS NAME_KANA,W1.SEX,W1.DESIRENAME, "); //NO004
                        stb.append("           'BB' AS ACCEPTNO,W2.EXAMNO AS TEXAM, value(W2.NAME,'') AS NAME2, value(W2.NAME_KANA,'') AS KANA2, ");    //NO004
                        stb.append("           W2.SEX,W1.SCLNAME,W1.JUDG ");
                        stb.append("    FROM   EXAM_BASE W1,EXAM_BASE W2, ");
                        stb.append("           (SELECT ");
                        stb.append("               T1.EXAMNO ");
                        stb.append("           FROM ");
                        stb.append("               EXAM_BASE T1,EXAM_CON T2 ");
                        stb.append("           WHERE ");
                        stb.append("               value(T1.NAME,'')=value(T2.NAME,'') AND ");              //NO004
                        stb.append("               value(T1.NAME_KANA,'')=value(T2.NAME_KANA,'') AND ");    //NO004
                        stb.append("               T1.TESTDIV<>T2.TESTDIV) W3 ");
                        stb.append("    WHERE  W3.EXAMNO = W1.EXAMNO ");
                        stb.append("           AND value(W1.NAME,'')=value(W2.NAME,'') ");              //NO004
                        if (param[4].equals("3")){
                            stb.append("       AND value(W1.NAME_KANA,'')=value(W2.NAME_KANA,'') ");    //NO004
                        }
                        stb.append("           AND W1.EXAMNO<>W2.EXAMNO ");
                    }
                    stb.append("ORDER BY NAME_KANA,EXAMNO,TESTDIV,ACCEPTNO ");
                }
            }
//log.debug(stb);
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
            if (param[4].equals("5")){
                stb.append("WITH HDATMAIN AS (SELECT ");
                stb.append("    T1.EXAMNO ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_APPLICANTBASE_DAT T1, ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT T2 ");
                stb.append("WHERE ");
                stb.append("    T1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                if (!param[1].equals("99")){
                    stb.append("    AND T1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND ((value(T1.NAME,'') = value(T2.NAME,'') AND value(T1.NAME_KANA,'') <> value(T2.NAME_KANA,''))  ");  //NO004
                stb.append("    OR (value(T1.NAME,'') <> value(T2.NAME,'') AND value(T1.NAME_KANA,'') = value(T2.NAME_KANA,''))) ");    //NO004
                stb.append("), HDATSUB AS (SELECT ");
                stb.append("    T1.EXAMNO ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_APPLICANTBASE_DAT T1, ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT T2 ");
                stb.append("WHERE ");
                stb.append("    T1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                if (!param[1].equals("99")){
                    stb.append("    AND T1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND ((value(T1.NAME,'') = value(T2.NAME,'') AND value(T1.NAME_KANA,'') <> value(T2.NAME_KANA,''))  ");  //NO004
                stb.append("    OR (value(T1.NAME,'') <> value(T2.NAME,'') AND value(T1.NAME_KANA,'') = value(T2.NAME_KANA,''))) ");    //NO004
                stb.append("    GROUP BY T1.EXAMNO ");
                stb.append("), HDATALL AS ( ");
                stb.append("    SELECT 'B' AS CONECT,COUNT(*) AS CNT ");
                stb.append("    FROM HDATMAIN ");
                stb.append("    UNION ALL ");
                stb.append("    SELECT 'B' AS CONECT,COUNT(*) AS CNT ");
                stb.append("    FROM HDATSUB ");
                stb.append(") ");
                stb.append("SELECT ");
                stb.append("    SUM(CNT), ");
                stb.append("    CASE WHEN 0 < MOD(SUM(CNT),50) THEN SUM(CNT)/50 + 1 ELSE SUM(CNT)/50 END AS TOTAL_PAGE ");
                stb.append("FROM ");
                stb.append("    HDATALL ");
                stb.append("GROUP BY CONECT ");
            }else if (param[4].equals("4")){
                stb.append("WITH HDATMAIN AS (SELECT ");
                stb.append("    value(NAME,'') AS NAME, ");             //NO004
                stb.append("    value(NAME_KANA,'') AS NAME_KANA ");    //NO004
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT ");
                stb.append("WHERE ");
                stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND TESTDIV = '"+param[1]+"' ");
                stb.append("), HDATSUB AS (SELECT ");
                stb.append("    value(NAME,'') AS NAME, ");             //NO004
                stb.append("    value(NAME_KANA,'') AS NAME_KANA ");    //NO004
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT ");
                stb.append("WHERE ");
                stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND TESTDIV <> '"+param[1]+"' ");
                stb.append(") ");
                stb.append("SELECT ");
                stb.append("    COUNT(*), ");
                stb.append("    CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS TOTAL_PAGE ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
                stb.append("WHERE ");
                stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                stb.append("    AND TESTDIV = '"+param[1]+"' ");
                stb.append("    AND value(NAME,'') || value(NAME_KANA,'') NOT IN (SELECT value(NAME,'') || value(NAME_KANA,'') FROM HDATMAIN) ");   //NO004
                stb.append("    AND value(NAME,'') || value(NAME_KANA,'') NOT IN (SELECT value(NAME,'') || value(NAME_KANA,'') FROM HDATSUB) ");    //NO004
                stb.append("    AND (EXAMNO < '3000' OR '3999' < EXAMNO) ");//NO003
            }else if (param[4].equals("6")){
                stb.append("WITH HDATMAIN AS (SELECT ");
                stb.append("    value(NAME,'') AS NAME, ");             //NO004
                stb.append("    value(NAME_KANA,'') AS NAME_KANA ");    //NO004
                stb.append("FROM ");
                stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
                stb.append("WHERE ");
                stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                stb.append("    AND TESTDIV = '"+param[1]+"' ");
                stb.append("    AND (EXAMNO < '3000' OR '3999' < EXAMNO) ");//NO005
                stb.append(") ");
                stb.append("SELECT ");
                stb.append("    COUNT(*), ");
                stb.append("    CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS TOTAL_PAGE ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT ");
                stb.append("WHERE ");
                stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("    AND value(NAME,'') || value(NAME_KANA,'') NOT IN (SELECT value(NAME,'') || value(NAME_KANA,'') FROM HDATMAIN) ");   //NO004
                stb.append("    GROUP BY TESTDIV ");
            }else {
                stb.append("WITH NOSERCH AS (SELECT ");
                stb.append("    t1.EXAMNO AS NOEXAM ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_APPLICANTBASE_DAT t1, ");
                stb.append("    ENTEXAM_CONSULTATION_HDAT t2 ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                if (!param[1].equals("99")){
                    stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!param[1].equals("99")){
                    stb.append("    AND t2.TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    AND value(t1.NAME,'') || value(t1.NAME_KANA,'') = value(t2.NAME,'') || value(t2.NAME_KANA,'') )");  //NO004
                stb.append(", EXAM_BASE AS ( ");
                stb.append("    SELECT EXAMNO,value(NAME,'') AS NAME,value(NAME_KANA,'') AS NAME_KANA,TESTDIV ");   //NO004
                stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
                stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                stb.append("           AND TESTDIV='"+param[1]+"' ");
                if (param[4].equals("3") ){
                    stb.append("       AND EXAMNO NOT IN (SELECT NOEXAM FROM NOSERCH) ");
                }
                stb.append("    ) ");
                stb.append(",EXAM_CON AS ( ");
                stb.append("    SELECT value(NAME,'') AS NAME,value(NAME_KANA,'') AS NAME_KANA,ACCEPTNO,TESTDIV "); //NO004
                stb.append("    FROM   ENTEXAM_CONSULTATION_HDAT ");
                stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' ");
                if (param[4].equals("2") ){
                    stb.append("       AND TESTDIV = '"+param[1]+"' ");
                }
                stb.append("    ) ");
                stb.append(",TYOUFUKUCNT AS ( ");
                stb.append("    SELECT T1.EXAMNO,value(T1.NAME,'') AS NAME,value(T1.NAME_KANA,'') AS NAME_KANA, "); //NO004
                stb.append("           T2.ACCEPTNO,value(T2.NAME_KANA,'') AS KANA2 ");  //NO004
                stb.append("    FROM   EXAM_BASE T1,EXAM_CON T2 ");
                stb.append("    WHERE  value(T1.NAME,'')=value(T2.NAME,'') ");          //NO004
                if (param[4].equals("1")){
                }else if(param[4].equals("2")){
                    stb.append("       AND value(T1.NAME_KANA,'')=value(T2.NAME_KANA,'') ");    //NO004
                }else {
                    stb.append("       AND value(T1.NAME_KANA,'')=value(T2.NAME_KANA,'') ");    //NO004
                    stb.append("       AND T1.TESTDIV<>T2.TESTDIV ");
                }
                stb.append("    UNION ALL ");
                stb.append("    SELECT W1.EXAMNO,value(W1.NAME,'') AS NAME,value(W1.NAME_KANA,'') AS NAME_KANA, ");     //NO004
                stb.append("           'BB' AS ACCEPTNO, value(W2.NAME_KANA,'') AS KANA2 ");    //NO004
                stb.append("    FROM   EXAM_BASE W1,EXAM_BASE W2 ");
                stb.append("    WHERE  value(W1.NAME,'')=value(W2.NAME,'') ");                  //NO004
                if (!param[4].equals("3")){
                    stb.append("       AND value(W1.NAME_KANA,'')=value(W2.NAME_KANA,'') ");    //NO004
                }
                stb.append("           AND W1.EXAMNO<>W2.EXAMNO ");
                stb.append("    ) ");
                stb.append(",BASECNT AS ( ");
                stb.append("    SELECT EXAMNO ");
                stb.append("    FROM TYOUFUKUCNT ");
                stb.append("    GROUP BY EXAMNO ");
                stb.append("    ) ");
                stb.append(",ALLCNTTBL AS ( ");
                stb.append("    SELECT 'B' AS CONECT,COUNT(*) AS CNT ");
                stb.append("    FROM BASECNT ");
                stb.append("    UNION ALL ");
                stb.append("    SELECT 'B' AS CONECT,COUNT(*) AS CNT ");
                stb.append("    FROM TYOUFUKUCNT ");
                stb.append(") ");
                stb.append("    SELECT CASE WHEN 0 < MOD((SUM(CNT)),50) THEN SUM(CNT)/50 + 1 ELSE SUM(CNT)/50 END AS TOTAL_PAGE ");
                stb.append("    FROM ALLCNTTBL ");
                stb.append("    GROUP BY CONECT ");
            }
//log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
//log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH DESIRETBL1 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV1, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK1 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '1' ");
            stb.append("),DESIRETBL2 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV2, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK2 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '2' ");
            stb.append("),DESIRETBL3 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV3, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK3 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD  ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD  ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '3' ");
            stb.append("),DESIRETBL4 AS ( ");
            stb.append("SELECT DESIREDIV AS DESIREDIV4, ");
            stb.append("       EXAMCOURSE_MARK AS EXAMCOURSE_MARK4 ");
            stb.append("FROM   ENTEXAM_WISHDIV_MST t1 ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST t2 ON t1.COURSECD = t2.COURSECD ");
            stb.append("       AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("       AND t2.MAJORCD = t1.MAJORCD  ");
            stb.append("       AND t2.EXAMCOURSECD = t1.EXAMCOURSECD  ");
            stb.append("WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("       AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("       AND t1.WISHNO = '4' ");
            stb.append("),DESIRETBL AS ( ");
            stb.append("SELECT DESIREDIV1 AS DESIREDIV, ");
            stb.append("    CASE WHEN EXAMCOURSE_MARK1 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK1,'') END || ");                   //NO004
            stb.append("    CASE WHEN EXAMCOURSE_MARK2 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK2,'') END || ");                   //NO004
            stb.append("    CASE WHEN EXAMCOURSE_MARK3 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK3,'') END || ");                   //NO004
            stb.append("    CASE WHEN EXAMCOURSE_MARK4 IS NULL THEN '' ELSE value(EXAMCOURSE_MARK4,'') END AS EXAMCOURSE_MARK ");   //NO004
            stb.append("FROM   DESIRETBL1 t1 ");
            stb.append("       LEFT JOIN DESIRETBL2 ON DESIREDIV2 = DESIREDIV1 ");
            stb.append("       LEFT JOIN DESIRETBL3 ON DESIREDIV3 = DESIREDIV1 ");
            stb.append("       LEFT JOIN DESIRETBL4 ON DESIREDIV4 = DESIREDIV1 ");
            stb.append(") ");
            stb.append("SELECT EXAMNO,value(NAME,'') AS NAME,value(NAME_KANA,'') AS NAME_KANA,t1.DESIREDIV, "); //NO004
            stb.append("       EXAMCOURSE_MARK AS JUDG,t3.NAME1 AS SCLNAME,t4.ABBV1 || '志' AS DESIRENAME, ");
            stb.append("       CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX ");
            stb.append("FROM   ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("       LEFT JOIN DESIRETBL t2 ON t1.DESIREDIV = t2.DESIREDIV ");
            stb.append("       LEFT JOIN NAME_MST t3 ON t3.NAMECD2 = t1.NATPUBPRIDIV ");
            stb.append("       AND NAMECD1 = 'L004' ");
            stb.append("       LEFT JOIN NAME_MST t4 ON t4.NAMECD2 = t1.TESTDIV ");
            stb.append("       AND t4.NAMECD1 = 'L003' ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            if (!"9".equals(param[10])) {
                stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
            }
            if (!param[1].equals("99")){
                stb.append("       AND TESTDIV='"+param[1]+"' ");
            }
            stb.append("       AND EXAMNO = ? ");

//log.debug(stb);
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
