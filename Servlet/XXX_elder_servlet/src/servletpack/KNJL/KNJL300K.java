// kanji=漢字
/*
 * $Id: 8b93c6afd45b541929384269f15f90d7dbf76fb0 $
 *
 * 作成日: 2005/07/25 11:25:40 - JST
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
 *  学校教育システム 賢者 [入試]  入試事前相談データ重複チェックリスト
 *
 *  2005/07/25  作成  m-yama
 *  2005/09/03  NO001 総ページ出力なし m-yama
 *  2005/10/26  NO002 判定を学校・塾両方出力 m-yama
 *  2005/10/26  NO003 空白行出力時のカウンタクリア処理 m-yama
 *  2005/11/07  NO004 チェック項目塾追加 m-yama
 *  2006/01/10  NO005 SQLを修正 m-yama
 *  2006/01/29  NO006 塾受付番号を学校受付番号でリンクしていたバグを修正 m-yama
 *                    学校名、塾名を両方出力に変更。
 *  2006/02/10  NO007 塾/学校受付番号でリンクしていた箇所を受付番号でリンクするよう修正 m-yama
 * @author m-yama
 * @version $Id: 8b93c6afd45b541929384269f15f90d7dbf76fb0 $
 */
public class KNJL300K {

    private static final Log log = LogFactory.getLog(KNJL300K.class);
    int len = 0;            //列数カウント用
    int ccnt    = 0;
    boolean nonedata = false;
    private String setacce[];           //セットデータ配列
    private String setschl[];           //セットデータ配列
    private String setpri[];            //セットデータ配列
    private String setdate[];           //セットデータ配列
    private String setname[];           //セットデータ配列
    private String setkana[];           //セットデータ配列
    private String setsex[];            //セットデータ配列
    private String setfsmj[];           //セットデータ配列
    private String setfsjd[];           //セットデータ配列
    private String setpsmj[];           //セットデータ配列
    private String setpsjd[];           //セットデータ配列
    private String setvalu[];           //セットデータ配列

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = new String[11];       //NO004

    //  パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");           //年度
            param[1]  = request.getParameter("TESTDIV");        //試験区分
            param[2]  = request.getParameter("JHFLG");          //中高判定フラグ1:中学、2:高校
            param[4]  = request.getParameter("OUTNAME");        //重複チェック氏名
            param[5]  = request.getParameter("OUTKANA");        //重複チェック氏名かな
            param[6]  = request.getParameter("OUTSCHL");        //重複チェック出身学校
            param[10] = request.getParameter("OUTPRI");         //重複チェック出身塾
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
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJL300]param["+ia+"]="+param[ia]);

        //SQL作成前処理
        String simei     = "−";
        String kanasimei = "−";
        String school    = "−";
        String prischl   = "−";
        String sqlparam  = " ";
        String sqlparam2 = " ";
        String sqlparam3 = " ";
        String sqlparam4 = " ";
        String sqlparam5 = " ";
        String sqlparam6 = " ";
        boolean orderflg = false;
        try {
            if (param[4] != null){
                sqlparam  = sqlparam  + "NAME";
                sqlparam2 = sqlparam2 + "t1.NAME";
                sqlparam3 = sqlparam3 + "value(t1.NAME,'')";    //NO005
                sqlparam4 = sqlparam4 + "value(t2.NAME,'')";    //NO005
                sqlparam5 = sqlparam5 + "t1.NAME";
                sqlparam6 = sqlparam6 + "CASE WHEN t1.NAME IS NULL THEN '' ELSE value(t1.NAME,'') END"; //NO005
                orderflg = true;
                simei = "○";
            }
            if (param[5] != null){
                if (orderflg){
                    sqlparam  = sqlparam  + ",NAME_KANA";
                    sqlparam2 = sqlparam2 + ",t1.NAME_KANA";
                    sqlparam3 = sqlparam3 + "|| value(t1.NAME_KANA,'')";    //NO005
                    sqlparam4 = sqlparam4 + "|| value(t2.NAME_KANA,'')";    //NO005
                    sqlparam5 = sqlparam5 + ",t1.NAME_KANA";
                    sqlparam6 = sqlparam6 + "|| CASE WHEN t1.NAME_KANA IS NULL THEN '' ELSE value(t1.NAME_KANA,'') END";    //NO005
                }else {
                    sqlparam  = sqlparam  + "NAME_KANA";
                    sqlparam2 = sqlparam2 + "t1.NAME_KANA";
                    sqlparam3 = sqlparam3 + "value(t1.NAME_KANA,'')";   //NO005
                    sqlparam4 = sqlparam4 + "value(t2.NAME_KANA,'')";   //NO005
                    sqlparam5 = sqlparam5 + "t1.NAME_KANA";
                    sqlparam6 = sqlparam6 + "CASE WHEN t1.NAME_KANA IS NULL THEN '' ELSE value(t1.NAME_KANA,'') END";   //NO005
                }
                orderflg = true;
                kanasimei = "○";
            }
            if (param[6] != null && param[2].equals("2")){
                if (orderflg){
                    sqlparam  = sqlparam  + ",FS_CD";
                    sqlparam2 = sqlparam2 + ",t1.FS_CD";
                    sqlparam3 = sqlparam3 + "|| value(t1.FS_CD,'')";    //NO005
                    sqlparam4 = sqlparam4 + "|| value(t2.FS_CD,'')";    //NO005
                    sqlparam6 = sqlparam6 + "|| CASE WHEN t1.FS_CD IS NULL THEN '' ELSE value(t1.FS_CD,'') END";    //NO005
                }else {
                    sqlparam  = sqlparam  + "FS_CD";
                    sqlparam2 = sqlparam2 + "t1.FS_CD";
                    sqlparam3 = sqlparam3 + "value(t1.FS_CD,'')";   //NO005
                    sqlparam4 = sqlparam4 + "value(t2.FS_CD,'')";   //NO005
                    sqlparam5 = "t1.FS_CD";
                    sqlparam6 = sqlparam6 + "CASE WHEN t1.FS_CD IS NULL THEN '' ELSE value(t1.FS_CD,'') END";   //NO005
                }
                school = "○";
            }
            if (param[10] != null && param[2].equals("2")){
                if (orderflg){
                    sqlparam  = sqlparam  + ",PS_CD";
                    sqlparam2 = sqlparam2 + ",t1.PS_CD";
                    sqlparam3 = sqlparam3 + "|| value(t1.PS_CD,'')";    //NO005
                    sqlparam4 = sqlparam4 + "|| value(t2.PS_CD,'')";    //NO005
                    sqlparam6 = sqlparam6 + "|| CASE WHEN t1.PS_CD IS NULL THEN '' ELSE value(t1.PS_CD,'') END";    //NO005
                }else {
                    sqlparam  = sqlparam  + "PS_CD";
                    sqlparam2 = sqlparam2 + "t1.PS_CD";
                    sqlparam3 = sqlparam3 + "value(t1.PS_CD,'')";   //NO005
                    sqlparam4 = sqlparam4 + "value(t2.PS_CD,'')";   //NO005
                    sqlparam5 = "t1.PS_CD";
                    sqlparam6 = sqlparam6 + "CASE WHEN t1.PS_CD IS NULL THEN '' ELSE value(t1.PS_CD,'') END";   //NO005
                }
                prischl = "○";
            }
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param,sqlparam,sqlparam2,sqlparam6));              //設定データpreparestatement
            ps2 = db2.prepareStatement(Pre_Stat2(param,sqlparam2,sqlparam3,sqlparam4,sqlparam5));   //設定データpreparestatement
            ps3 = db2.prepareStatement(Pre_Stat3(param,sqlparam2,sqlparam3,sqlparam4,sqlparam5));   //設定データpreparestatement
        } catch( Exception ex ) {
            log.error("SQL read error!");
        }

        //SVF出力
        Set_Detail_1(db2,svf,param,ps1,ps2,ps3,simei,kanasimei,school,prischl);

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
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '"+param[1]+"'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
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
    PreparedStatement ps3,
    String simei,
    String kanasimei,
    String school,
    String prischl)
    {
        boolean firstflg = true;    //先頭データフラグ
        boolean setdtflg = false;   //データセットフラグ
        boolean endflg   = false;   //終了フラグ
        int allcnt  = 0;            //配列用カウンタ
        int girlcnt = 0;            //女子カウンタ
        int mancnt  = 0;            //男子カウンタ
        int rennum  = 1;            //連番
        int pagecnt = 1;            //現在ページ
        String allpage = "" ;       //最終ページ
        String bfrdata = "*" ;      //最終データ
        try {
            ResultSet rs1 = ps2.executeQuery();
            while( rs1.next() ){
                allcnt  = Integer.parseInt(rs1.getString("ALCNT"));
            }
            ResultSet rs3 = ps3.executeQuery();
            while( rs3.next() ){
                allpage = rs3.getString("TOTAL_PAGE");
            }
            rs3.close();
            setacce = new String[allcnt];
            setschl = new String[allcnt];
            setpri  = new String[allcnt];
            setdate = new String[allcnt];
            setname = new String[allcnt];
            setkana = new String[allcnt];
            setsex  = new String[allcnt];
            setfsmj = new String[allcnt];
            setfsjd = new String[allcnt];
            setpsmj = new String[allcnt];
            setpsjd = new String[allcnt];
            setvalu = new String[allcnt];
            svf.VrSetForm("KNJL300K.frm", 4);
            rs1.close();
            if (Integer.parseInt(allpage) > 0){
                ResultSet rs = ps1.executeQuery();
                int gyo  = 1;           //行数カウント用
                int seti = 0;
                while( rs.next() ){
                    setacce[seti] = rs.getString("ACCEPTNO");
                    setschl[seti] = rs.getString("FINSCHOOL_NAME");
                    setdate[seti] = rs.getString("CREATE_DATE");
                    setname[seti] = rs.getString("NAME");
                    setkana[seti] = rs.getString("NAME_KANA");
                    setfsmj[seti] = rs.getString("FSMAJOR");
                    setfsjd[seti] = rs.getString("FSJUDG");
                    setpsmj[seti] = rs.getString("PSMAJOR");
                    setpsjd[seti] = rs.getString("PSJUDG");
                    setvalu[seti] = rs.getString("VALUE");
                    setpri[seti]  = rs.getString("PRISCHOOL_NAME");
                    setsex[seti]  = rs.getString("SEX");
                    seti++;
                }
                rs.close();
                for (int i=0;i < allcnt;i++){
                    //ヘッダ出力
                    printHeader(svf,param,pagecnt,allpage,simei,kanasimei,school,prischl);
                    if ( gyo > 50 ){
                        endflg = true;
                    }
                    //先頭データ判定
                    if (firstflg){
                        //最終データ判定
                        if (allcnt - i > 1){
                            //次データ重複
                            if (setvalu[i].equalsIgnoreCase(setvalu[i+1])){
                                if (endflg){
                                    svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                                    svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                                    svf.VrEndRecord();
                                    endflg = false;
                                    gyo = 1;            //行数カウント用 NO003
                                    pagecnt++;          //NO003
                                }
                                printdata(svf,param,i,rennum);
                                bfrdata = setvalu[i];
                                if (setsex[i].equals("*")){
                                    girlcnt++;
                                }else {
                                    mancnt++;
                                }
                                nonedata = true;
                                setdtflg = true;
                                gyo++;          //行数カウント用
                            }
                        }
                    }else {
                        //最終データ判定
                        if (allcnt - i > 1){
                            //次データ・前データ重複
                            if (setvalu[i].equalsIgnoreCase(setvalu[i+1]) ||
                                setvalu[i].equalsIgnoreCase(setvalu[i-1])){
                                if (!setvalu[i].equalsIgnoreCase(bfrdata) && setdtflg){
                                    rennum++;
                                }
                                if (endflg){
                                    svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                                    svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                                    svf.VrEndRecord();
                                    endflg = false;
                                    gyo = 1;            //行数カウント用 NO003
                                    pagecnt++;          //NO003
                                }
                                printdata(svf,param,i,rennum);
                                bfrdata = setvalu[i];
                                if (setsex[i].equals("*")){
                                    girlcnt++;
                                }else {
                                    mancnt++;
                                }
                                nonedata = true;
                                setdtflg = true;
                                gyo++;          //行数カウント用
                            }
                        }else {
                            //次データ重複
                            if (setvalu[i].equalsIgnoreCase(setvalu[i-1])){
                                if (!setvalu[i].equalsIgnoreCase(bfrdata) && setdtflg){
                                    rennum++;
                                }
                                if (endflg){
                                    svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                                    svf.VrsOut("TOTAL_MEMBER"     , String.valueOf("空"));    //空行
                                    svf.VrEndRecord();
                                    endflg = false;
                                    gyo = 1;            //行数カウント用 NO003
                                    pagecnt++;          //NO003
                                }
                                printdata(svf,param,i,rennum);
                                bfrdata = setvalu[i];
                                if (setsex[i].equals("*")){
                                    girlcnt++;
                                }else {
                                    mancnt++;
                                }
                                nonedata = true;
                                setdtflg = true;
                                gyo++;          //行数カウント用
                            }
                        }
                    }

                    firstflg = false;
                }//for end
            }
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
    private void printHeader(Vrw32alp svf,String param[],int pagecnt,String allpage,String simei,String kanasimei,String school,String prischl)
    {
        try {
            svf.VrsOut("NENDO"        , String.valueOf(param[8]) );
            svf.VrsOut("SCHOOLDIV"    , String.valueOf(param[9]) );
            if (param[7] != null) svf.VrsOut("TESTDIV"        , String.valueOf(param[7]) );
            svf.VrsOut("SIMEI"        , String.valueOf(simei) );
            svf.VrsOut("KANASIMEI"    , String.valueOf(kanasimei) );
            svf.VrsOut("SCHOOL"       , String.valueOf(school) );
            svf.VrsOut("PRISCHOOL"    , String.valueOf(prischl) );
            svf.VrsOut("DATE"         , String.valueOf(param[3]) );
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
    private void printdata(Vrw32alp svf,String param[],int i,int rennum)
    {
        String len1 = "0";
        String len2 = "0";
        try {
            //明細出力
            svf.VrsOut("NUMBER"   , String.valueOf(rennum));
            svf.VrsOut("ACCEPTNO" , setacce[i]);

            svf.VrsOut("FINSCHOOLNAME"    , setschl[i]);  //NO006
            svf.VrsOut("PRISCHOOLNAME"    , setpri[i]);   //NO006

            svf.VrsOut("ORG_MAJOR1"   , setfsjd[i]);  //NO002
            svf.VrsOut("ORG_MAJOR2"   , setpsjd[i]);  //NO002
            svf.VrsOut("UPDATE"       , setdate[i]);

            if (null != setname[i]){
                len1 = (10 < (setname[i]).length()) ? "2" : "1" ;
            }else {
                len1 = "1" ;
            }
            if (null != setkana[i]){
                len2 = (10 < (setkana[i]).length()) ? "2" : "1" ;
            }else {
                len2 = "1" ;
            }
            svf.VrsOut("NAME"+len1    , setname[i]);
            svf.VrsOut("KANA"+len2    , setkana[i]);
            svf.VrsOut("SEX"          , setsex[i]);
            svf.VrEndRecord();
        } catch( Exception ex ) {
            log.warn("printdata read error!",ex);
        }

    }//printdata()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(String param[],String sqlparam,String sqlparam2,String sqlparam6)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //学校データ
            stb.append("WITH FSDATA AS ( ");
            stb.append("SELECT ");
            stb.append("    ACCEPTNO,DATADIV,SHDIV,WISHNO, ");
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
            if (!param[1].equals("99")){
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("    AND t1.DATADIV = '1' ");
            //塾データ
            stb.append("),PSDATA AS ( ");
            stb.append("SELECT ");
            stb.append("    ACCEPTNO,DATADIV,SHDIV,WISHNO, ");
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
            if (!param[1].equals("99")){
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("    AND t1.DATADIV = '2' ");
            stb.append(") ");
            //メイン
            stb.append("SELECT ");
            stb.append("    t1.ACCEPTNO,t1.FS_CD,t2.FINSCHOOL_NAME,t5.PRISCHOOL_NAME,t1.CREATE_DATE, ");
            stb.append("    t1.NAME,t1.NAME_KANA,CASE WHEN SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("    CASE WHEN f1.MAJOR IS NULL THEN '' ELSE value(f1.MAJOR,'') END || ");   //NO005
            stb.append("    CASE WHEN f2.MAJOR IS NULL THEN '' ELSE value(f2.MAJOR,'') END || ");   //NO005
            stb.append("    CASE WHEN f3.MAJOR IS NULL THEN '' ELSE value(f3.MAJOR,'') END || ");   //NO005
            stb.append("    CASE WHEN f4.MAJOR IS NULL THEN '' ELSE value(f4.MAJOR,'') END FSMAJOR, "); //NO005
            stb.append("    CASE WHEN f1.MAJOR IS NULL THEN ' ' ELSE value(f1.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f1.SHNAME IS NULL THEN ' ' ELSE value(f1.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN f1.JUDG IS NULL THEN ' ・' ELSE value(f1.JUDG,'') || '・' END || ");    //NO005
            stb.append("    CASE WHEN f2.MAJOR IS NULL THEN ' ' ELSE value(f2.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f2.SHNAME IS NULL THEN ' ' ELSE value(f2.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN f2.JUDG IS NULL THEN ' ・' ELSE value(f2.JUDG,'') || '・' END || ");    //NO005
            stb.append("    CASE WHEN f3.MAJOR IS NULL THEN ' ' ELSE value(f3.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f3.SHNAME IS NULL THEN ' ' ELSE value(f3.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN f3.JUDG IS NULL THEN ' ・' ELSE value(f3.JUDG,'') || '・' END || ");    //NO005
            stb.append("    CASE WHEN f4.MAJOR IS NULL THEN ' ' ELSE value(f4.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN f4.SHNAME IS NULL THEN ' ' ELSE value(f4.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN f4.JUDG IS NULL THEN ' ' ELSE value(f4.JUDG,'') END FSJUDG, ");   //NO005
            stb.append("    CASE WHEN p1.MAJOR IS NULL THEN '' ELSE value(p1.MAJOR,'') END || ");   //NO005
            stb.append("    CASE WHEN p2.MAJOR IS NULL THEN '' ELSE value(p2.MAJOR,'') END || ");   //NO005
            stb.append("    CASE WHEN p3.MAJOR IS NULL THEN '' ELSE value(p3.MAJOR,'') END || ");   //NO005
            stb.append("    CASE WHEN p4.MAJOR IS NULL THEN '' ELSE value(p4.MAJOR,'') END PSMAJOR, "); //NO005
            stb.append("    CASE WHEN p1.MAJOR IS NULL THEN ' ' ELSE value(p1.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p1.SHNAME IS NULL THEN ' ' ELSE value(p1.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN p1.JUDG IS NULL THEN ' ・' ELSE value(p1.JUDG,'') || '・' END || ");    //NO005
            stb.append("    CASE WHEN p2.MAJOR IS NULL THEN ' ' ELSE value(p2.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p2.SHNAME IS NULL THEN ' ' ELSE value(p2.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN p2.JUDG IS NULL THEN ' ・' ELSE value(p2.JUDG,'') || '・' END || ");    //NO005
            stb.append("    CASE WHEN p3.MAJOR IS NULL THEN ' ' ELSE value(p3.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p3.SHNAME IS NULL THEN ' ' ELSE value(p3.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN p3.JUDG IS NULL THEN ' ・' ELSE value(p3.JUDG,'') || '・' END || ");    //NO005
            stb.append("    CASE WHEN p4.MAJOR IS NULL THEN ' ' ELSE value(p4.MAJOR,'') END || ");  //NO005
            if (param[2].equals("2")){
                stb.append("    CASE WHEN p4.SHNAME IS NULL THEN ' ' ELSE value(p4.SHNAME,'') END || ");    //NO005
            }
            stb.append("    CASE WHEN p4.JUDG IS NULL THEN ' ' ELSE value(p4.JUDG,'') END PSJUDG, ");   //NO005
            stb.append("    "+sqlparam6+" AS VALUE ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT t1 ");
            stb.append("    LEFT JOIN FINSCHOOL_MST t2 ON t2.FINSCHOOLCD = t1.FS_CD ");
            stb.append("    LEFT JOIN PRISCHOOL_MST t5 ON t5.PRISCHOOLCD = t1.PS_CD ");
            stb.append("    LEFT JOIN FSDATA f1 ON f1.ACCEPTNO = t1.ACCEPTNO AND f1.WISHNO = '1'"); //NO007
            stb.append("    LEFT JOIN FSDATA f2 ON f2.ACCEPTNO = t1.ACCEPTNO AND f2.WISHNO = '2'"); //NO007
            stb.append("    LEFT JOIN FSDATA f3 ON f3.ACCEPTNO = t1.ACCEPTNO AND f3.WISHNO = '3'"); //NO007
            stb.append("    LEFT JOIN FSDATA f4 ON f4.ACCEPTNO = t1.ACCEPTNO AND f4.WISHNO = '4'"); //NO007
            stb.append("    LEFT JOIN PSDATA p1 ON p1.ACCEPTNO = t1.ACCEPTNO AND p1.WISHNO = '1'"); //NO006 //NO007
            stb.append("    LEFT JOIN PSDATA p2 ON p2.ACCEPTNO = t1.ACCEPTNO AND p2.WISHNO = '2'"); //NO006 //NO007
            stb.append("    LEFT JOIN PSDATA p3 ON p3.ACCEPTNO = t1.ACCEPTNO AND p3.WISHNO = '3'"); //NO006 //NO007
            stb.append("    LEFT JOIN PSDATA p4 ON p4.ACCEPTNO = t1.ACCEPTNO AND p4.WISHNO = '4'"); //NO006 //NO007
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("ORDER BY ");
            stb.append(" "+sqlparam2+",t1.ACCEPTNO ");

//log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat2(String param[],String sqlparam2,String sqlparam3,String sqlparam4,String sqlparam5)
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("WITH ALLCNT AS (SELECT ");
            stb.append("    COUNT(*) AS ALCNT ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+param[1]+"' ");
            stb.append("),GROUPCNTB AS( ");
            stb.append("SELECT ");
            stb.append("    FS_CD,COUNT(*) AS CNT ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+param[1]+"' ");
            stb.append("GROUP BY ");
            stb.append("    FS_CD ");
            stb.append("HAVING ");
            stb.append("    COUNT(*) > 1 ");
            stb.append("ORDER BY ");
            stb.append("    FS_CD ");
            stb.append("),GROUPCNTA AS( ");
            stb.append("SELECT ");
            stb.append("    SUM(CNT) AS GPCNT ");
            stb.append("FROM ");
            stb.append("    GROUPCNTB ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    ALCNT, ");
            stb.append("    CASE WHEN 0 < MOD((GPCNT),50) THEN (GPCNT)/50 + 1 ELSE (GPCNT)/50 END AS TOTAL_PAGE ");
            stb.append("FROM ");
            stb.append("    ALLCNT, ");
            stb.append("    GROUPCNTA ");

//log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat3(String param[],String sqlparam2,String sqlparam3,String sqlparam4,String sqlparam5)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH CNTSUB AS (SELECT ");
            if (sqlparam5.equals("t1.FS_CD")){
                stb.append("    "+sqlparam5+" AS FSCD,COUNT(*) AS ALLCNT, ");
            }else if(sqlparam5.equals("t1.PS_CD")){
                stb.append("    "+sqlparam5+" AS PSCD,COUNT(*) AS ALLCNT, ");
            }else {
                stb.append("    "+sqlparam5+",COUNT(*) AS ALLCNT, ");
            }
            stb.append("    MIN(t1.ACCEPTNO) AS ACCEPTNO, ");
            stb.append("    MIN(t1.FS_CD) AS FS_CD, ");
            stb.append("    MIN(t1.PS_CD) AS PS_CD ");  //NO004
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT t1, ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT t2 ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("    AND t2.TESTDIV = '"+param[1]+"' ");
            }
            stb.append("    AND "+sqlparam3+" = "+sqlparam4+" ");
            stb.append("    AND t1.ACCEPTNO <> t2.ACCEPTNO ");
            stb.append("GROUP BY ");
            stb.append("    "+sqlparam5+" ");
            stb.append("), ALLCNT AS ( ");
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS ALCNT ");
            stb.append("FROM ");
            if (sqlparam5.equals("t1.FS_CD")){
                stb.append("    CNTSUB t1 LEFT JOIN ENTEXAM_CONSULTATION_HDAT t2 ON t1.FS_CD = t2.FS_CD ");
            }else {
                stb.append("    CNTSUB t1 LEFT JOIN ENTEXAM_CONSULTATION_HDAT t2 ON "+sqlparam3+" = "+sqlparam4+" ");
            }
            stb.append("    AND t2.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!param[1].equals("99")){
                stb.append("    AND t2.TESTDIV = '"+param[1]+"' ");
            }
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    ALCNT, ");
            stb.append("    CASE WHEN 0 < MOD((ALCNT),50) THEN (ALCNT)/50 + 1 ELSE (ALCNT)/50 END AS TOTAL_PAGE ");
            stb.append("FROM ");
            stb.append("    ALLCNT ");

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
