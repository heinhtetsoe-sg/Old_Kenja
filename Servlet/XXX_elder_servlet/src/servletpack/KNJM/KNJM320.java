// kanji=漢字
/*
 * $Id: 963ab0c90b4dccff0cf1bea321d33c651e90a034 $
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

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ３２０＞  教科書レポート引換票（東京都）
 *
 * 2005/02/01 m-yama  作成
 * 2006/04/18 m-yama  NO001 科目名フィールドの長さを20→30に変更。
 *                          備考欄の冊数出力なし。
 *                          科目コード頭２桁が90より大きい科目は、対象外とする。
 */

public class KNJM320 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM320.class);

    Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                     // Databaseクラスを継承したクラス
    String dbname;
    boolean nonedata;               // 該当データなしフラグ
    int ret;                        // ＳＶＦ応答値


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {


    // パラメータの取得
        String param[] = new String[5];
        String schno[] = request.getParameterValues("category_selected");   //生徒データ

        try {
            dbname   = request.getParameter("DBNAME");          // データベース名
            param[0] = request.getParameter("YEAR");            // 年度
            param[1] = request.getParameter("GAKKI");           // 学期
            param[2] = request.getParameter("GRADE_HR_CLASS");  // 年組
            param[4] = request.getParameter("useCurriculumcd");                     //教育課程

        } catch( Exception ex ) {
            log.error("DB2 parameter set error!"+ex);
        }


    // print設定
        new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定
        svf.VrSetForm("KNJM320.frm", 4);      //SuperVisualFormadeで設計したレイアウト定義態の設定

    // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 connect error!"+ex);
        }

    //  ＳＶＦ作成処理
        PreparedStatement ps  = null;
        Set_Head(db2,svf,param);    //見出し出力のメソッド
        nonedata = false;           // 該当データなしフラグ(MES001.frm出力用)
for(int i=0 ; i<4 ; i++) log.debug("param["+i+"]="+param[i]);
        log.fatal("$Revision: 56595 $");
        //SQL作成
        try {
            ps  = db2.prepareStatement(preStat(param));
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        int i = 0;
        while(i < schno.length){
            set_detail(db2,svf,param,schno[i],ps);
            i++;
        }

    //  該当データ無し
        if(nonedata == false){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndRecord();
            svf.VrEndPage();
        }

    // 終了処理
        svf.VrQuit();
        preStatClose(ps);   //preparestatementを閉じる
        db2.commit();
        db2.close();        // DBを閉じる
        outstrm.close();    // ストリームを閉じる 


    }//svf_outの括り

    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[3] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

    }//Set_Head()の括り

    /*----------------------------*
     * ＳＶＦ出力
     *----------------------------*/
    public void set_detail(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        String schno,
        PreparedStatement ps
    )
    {
        try {
           /** 照会結果の取得とsvf_formへ出力 **/

            //年度
            svf.VrsOut("NENDO1", param[0] + "年度");

            int totalcredit = 0;        //合計単位
            int totalreprt  = 0;        //合計レポート数
            int totaltext   = 0;        //合計教科書数
            int fieldcnt   = 1;         //連番
            int andflg     = 1;         //同一科目フラグ
            int gyo        = 0;         //小計出力判定
            int taiku_kcnt = 0;             // 体育フラグ
            int eng2_kcnt  = 0;             // 英語2フラグ
            int taiku_gcnt = 0;             // 体育フラグ
            int eng2_gcnt  = 0;             // 英語2フラグ
            int kamokucnt  = 0;             // 
            String bfdata  ;            //前回コード
            String afdata  ;            //現在コード
            boolean bookdata;           // 該当データなしフラグ
            boolean creditflg;          // 単位データ加算フラグ
            boolean taiku;              // 体育フラグ
            boolean eng2 ;              // 英語2フラグ

            bfdata  = String.valueOf("0");
            afdata  = String.valueOf("0");

            ps.setString( 1, schno);
            ps.setString( 2, schno);
log.debug(schno);
            ResultSet rs = ps.executeQuery();
            bookdata  = false;
            taiku  = false;
            eng2   = false;
            while( rs.next() ){
                
log.debug("rsstart");
                creditflg = false;
                bfdata = String.valueOf(afdata);
                afdata = rs.getString("SUBCLASSCD");
                if ("1".equals(param[4])) {
                    afdata = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");
                }
                //教科書区分が変われば、次の行
                if (!bfdata.equalsIgnoreCase(afdata) && gyo > 0) {
                    fieldcnt++;
                    andflg = 0;
                }else {
                    andflg = 1;
                }

                if (gyo == 40){
                    gyo = 0;
                }else if(gyo == 0){
                    andflg = 0;
                }

                //体育は、全教科で、同一教科書
                if (rs.getString("CHAIRCD").equals("1610010") || rs.getString("CHAIRCD").equals("1610020") ||
                    rs.getString("CHAIRCD").equals("1610030") || rs.getString("CHAIRCD").equals("1610040") ||
                    rs.getString("CHAIRCD").equals("1610110") || rs.getString("CHAIRCD").equals("1610120")){
                    taiku = true;
                    if (rs.getString("TEXTBOOKDIV") != null){
                        if (rs.getString("TEXTBOOKDIV").equals("1")){
                            taiku_kcnt++;
                        }else {
                            taiku_gcnt++;
                        }
                    }
                }

                //英語2は、全教科で、同一教科書
                if (rs.getString("CHAIRCD").equals("1810460") || rs.getString("CHAIRCD").equals("1810370")){
                    eng2 = true;
                    if (rs.getString("TEXTBOOKDIV") != null){
                        if (rs.getString("TEXTBOOKDIV").equals("1")){
                            eng2_kcnt++;
                        }else {
                            eng2_gcnt++;
                        }
                    }
                }


                //ヘッダ
                svf.VrsOut("NENDO"    , String.valueOf(param[0]));                        //組名称
                svf.VrsOut("DATE"     , String.valueOf(param[3]));                        //組名称
                svf.VrsOut("SCHREGNO" , String.valueOf(schno));                       //組名称
                svf.VrsOut("HR_NAME"  , rs.getString("HR_NAME"));                     //組名称
                svf.VrsOut("ATTENDNO" , String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))));  //出席番号
                svf.VrsOut("NAME"     , rs.getString("NAME"));                   //生徒氏名
                svf.VrsOut("NO"       , String.valueOf(fieldcnt));                    //連番
                //科目の先頭データ
                if (andflg == 0){
log.debug("set1!!!!!"+rs.getString("SUBCLASSNAME"));
                    byte check_len[] = new byte[40];
                    check_len = (rs.getString("SUBCLASSNAME")).getBytes("MS932");
                    //NO001
                    if (check_len.length <= 30){
                        svf.VrsOut("SUBCLASS1_1"      , rs.getString("SUBCLASSNAME"));    //科目名
                    }else {
                        svf.VrsOut("SUBCLASS1_2"      , rs.getString("SUBCLASSNAME"));    //科目名
                    }

                    if (rs.getString("NAMEFLG").equals("2")){

                        if ((taiku == true && taiku_kcnt > 1) || (taiku == true && taiku_gcnt > 1)){
                            svf.VrAttribute("TEXTNAME","Meido=100");
                            svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                        }else if((eng2 == true && eng2_kcnt > 1) || (eng2 == true && eng2_gcnt > 1)){
                            svf.VrAttribute("TEXTNAME","Meido=100");
                            svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                        }else {
                            if (rs.getString("TEXTBOOKDIV").equals("1")){
                                svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME"));            //教科書名
                            }else {
                                if (rs.getString("NAME1") != null){
                                    svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                                }else {
                                    svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"(　)");         //教科書名
                                }
                            }
                            totalreprt++;
                            totaltext++;
                        }
                    }else {
                        svf.VrAttribute("TEXTNAME","Meido=100");
                        svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                    }
log.debug("2222222222222222");
                    if (rs.getString("CREDITS") != null) {
                        svf.VrsOut("CREDIT"       , rs.getString("CREDITS"));                 //単位
                        creditflg = true;
                    }else {
                        svf.VrAttribute("CREDIT","Meido=100");
                        svf.VrsOut("CREDIT"       , String.valueOf("空"));                    //単位
                    }

                    svf.VrEndRecord();
                    gyo++;
                    if (gyo == 40){
                        gyo = 0;
                    }
                    kamokucnt++;
                }else {
                    if (gyo == 0 && andflg == 0) {
log.debug("set2!!!!!"+rs.getString("SUBCLASSNAME"));
                        byte check_len[] = new byte[40];
                        check_len = (rs.getString("SUBCLASSNAME")).getBytes("MS932");
                        //NO001
                        if (check_len.length <= 30){
                            svf.VrsOut("SUBCLASS1_1"      , rs.getString("SUBCLASSNAME"));    //科目名
                        }else {
                            svf.VrsOut("SUBCLASS1_2"      , rs.getString("SUBCLASSNAME"));    //科目名
                        }
                        if (rs.getString("NAMEFLG").equals("2")){
                            if ((taiku == true && taiku_kcnt > 1) || (taiku == true && taiku_gcnt > 1)){
                                svf.VrAttribute("TEXTNAME","Meido=100");
                                svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                            }else if((eng2 == true && eng2_kcnt > 1) || (eng2 == true && eng2_gcnt > 1)){
                                svf.VrAttribute("TEXTNAME","Meido=100");
                                svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                            }else {
                                if (rs.getString("TEXTBOOKDIV").equals("1")){
                                    svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME"));            //教科書名
                                }else {
                                    if (rs.getString("NAME1") != null){
                                        svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                                    }else {
                                        svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"(　)");         //教科書名
                                    }
                                }
                                totalreprt++;
                                totaltext++;
                            }
                        }else {
                            svf.VrAttribute("TEXTNAME","Meido=100");
                            svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                        }
                        if (rs.getString("CREDITSFLG").equals("1")) {
                            if (!rs.getString("SETFLG").equals("1")) {
                                svf.VrsOut("CREDIT"       , rs.getString("CREDITS"));                 //単位
                            }else {
                                svf.VrAttribute("CREDIT","Meido=100");
                                svf.VrsOut("CREDIT"       , String.valueOf("空"));                    //単位
                            }
                        }else {
                            svf.VrAttribute("CREDIT","Meido=100");
                            svf.VrsOut("CREDIT"       , String.valueOf("空"));                    //単位
                        }
                        svf.VrEndRecord();
                        gyo++;
                        if (gyo == 40){
                            gyo = 0;
                        }
                    }else {
log.debug("set3!!!!!"+rs.getString("SUBCLASSNAME"));
                        //詳細
                        if (rs.getString("NAMEFLG").equals("2")){
                            if ((taiku == true && taiku_kcnt > 1) || (taiku == true && taiku_gcnt > 1)){
                                svf.VrAttribute("TEXTNAME","Meido=100");
                                svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                            }else if((eng2 == true && eng2_kcnt > 1) || (eng2 == true && eng2_gcnt > 1)){
                                svf.VrAttribute("TEXTNAME","Meido=100");
                                svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                            }else {
                                if (rs.getString("TEXTBOOKDIV").equals("1")){
                                    svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME"));            //教科書名
                                }else {
                                    if (rs.getString("NAME1") != null){
                                        svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                                    }else {
                                        svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"(　)");         //教科書名
                                    }
                                }
                                totaltext++;
                            }
                        }else {
                            svf.VrAttribute("TEXTNAME","Meido=100");
                            svf.VrsOut("TEXTNAME"     , rs.getString("TEXTBOOKNAME")+"("+rs.getString("NAME1")+")");          //教科書名
                        }
                        svf.VrEndRecord();
                        gyo++;
                        if (gyo == 40){
                            gyo = 0;
                        }
                    }
                }
                //合計単位を計算
                if (rs.getString("CREDITSFLG").equals("1") && rs.getString("CREDITS") != null) {
                    totalcredit = totalcredit + Integer.parseInt(rs.getString("CREDITS"));
                }else if (creditflg == true && rs.getString("CREDITS") != null){
                    totalcredit = totalcredit + Integer.parseInt(rs.getString("CREDITS"));
                }
                nonedata = true; //該当データなしフラグ
                bookdata = true;
                taiku    = false;
                eng2     = false;
log.debug("endgyou="+String.valueOf(gyo));
            }
log.debug("whilesyuuryou!!!!!!!!!!!");
            if(bookdata == true){
                svf.VrsOut("TOTALCREDIT"      , String.valueOf(totalcredit));     //小計
                svf.VrsOut("TOTALREPORT"  , String.valueOf(kamokucnt));           //小計金額
//              svf.VrsOut("TOTALREPORT"  , String.valueOf(totalreprt));          //小計金額
//              svf.VrsOut("TOTALTEXT"    , String.valueOf(totaltext));           //小計金額 NO001
                svf.VrEndRecord();
                gyo++;
log.debug("saisyuugyou="+String.valueOf(gyo));
                if (gyo == 40){
                    gyo = 0;
                }
                if (gyo > 0){
                    for (int lastcnt = gyo ; lastcnt < 40 ; lastcnt++){
                        svf.VrsOut("SPACE1"   , "空");
                        svf.VrEndRecord();
                gyo++;
                    }
                }
log.debug("lastcnt="+String.valueOf(gyo));
            }
            db2.commit();
            log.debug("set_detail read ok!");
        } catch( Exception ex ){
            log.error("set_detail read error!"+ex);
        }

    }//set_detailの括り

    /**データ　取得**/
    private String preStat(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH basetable as ( ");
            stb.append("SELECT ");
            stb.append("    w1.SCHREGNO,w1.CHAIRCD,w2.TEXTBOOKCD, ");
            if ("1".equals(param[4])) {
                stb.append("       w3.CLASSCD, ");
                stb.append("       w3.SCHOOL_KIND, ");
                stb.append("       w3.CURRICULUM_CD, ");
            }
            stb.append("    w3.SUBCLASSCD ");
            stb.append("FROM ");
            stb.append("    CHAIR_STD_DAT w1 ");
            stb.append("    LEFT JOIN SCHREG_TEXTBOOK_DAT w2 ON w1.SCHREGNO = w2.SCHREGNO ");
            stb.append("    AND w1.CHAIRCD = w2.CHAIRCD ");
            stb.append("    AND w2.YEAR = w1.YEAR ");
            stb.append("    AND w2.SEMESTER = '"+param[1]+"' ");
            stb.append("    LEFT JOIN CHAIR_DAT w3 ON w1.CHAIRCD = w3.CHAIRCD ");
            stb.append("    AND w3.YEAR = '"+param[0]+"' ");
            stb.append("    AND w3.SEMESTER = '"+param[1]+"' ");
            stb.append("WHERE ");
            stb.append("    w1.YEAR = '"+param[0]+"' ");
            stb.append("    AND w1.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND w1.SCHREGNO = ? ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    t1.CHAIRCD, ");
            stb.append("    t7.HR_NAME, ");
            stb.append("    t6.ATTENDNO, ");
            stb.append("    t5.NAME, ");
            if ("1".equals(param[4])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD, ");
            stb.append("    t2.SUBCLASSNAME, ");
            stb.append("    t1.TEXTBOOKCD, ");
            stb.append("    t3.TEXTBOOKDIV, ");
            stb.append("    t3.TEXTBOOKNAME,");
            stb.append("    case when t8.CREDITS is null then 1 else 2 end SETFLG, ");
            stb.append("    case when t3.TEXTBOOKNAME is null then 1 else 2 end NAMEFLG, ");
            stb.append("    case when t3.TEXTBOOKDIV = '1' then 1 else 2 end CREDITSFLG, ");
            stb.append("    t8.CREDITS, ");
            stb.append("    t4.NAME1 ");
            stb.append("FROM ");
            stb.append("    basetable t1 LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ");
            if ("1".equals(param[4])) {
                stb.append("       AND t1.CLASSCD = t2.CLASSCD ");
                stb.append("       AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
            }
            stb.append("    LEFT JOIN V_TEXTBOOK_MST t3 ON t1.TEXTBOOKCD = t3.TEXTBOOKCD ");
            stb.append("    AND t3.YEAR = '"+param[0]+"' ");
            stb.append("    LEFT JOIN V_NAME_MST t4 ON t3.TEXTBOOKDIV = t4.NAMECD2 ");
            stb.append("    AND t4.YEAR = '"+param[0]+"' ");
            stb.append("    AND t4.NAMECD1 = 'M004' ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT t6 ON t1.SCHREGNO = t6.SCHREGNO ");
            stb.append("    AND t6.YEAR = '"+param[0]+"' ");
            stb.append("    AND t6.SEMESTER = '"+param[1]+"' ");
            stb.append("    LEFT JOIN CREDIT_MST t8 ON t6.COURSECD = t8.COURSECD ");
            stb.append("    AND t8.YEAR = '"+param[0]+"' ");
            stb.append("    AND t6.MAJORCD = t8.MAJORCD ");
            stb.append("    AND t6.GRADE = t8.GRADE ");
            stb.append("    AND t6.COURSECODE = t8.COURSECODE ");
            stb.append("    AND substr(t1.SUBCLASSCD,1,2) = t8.CLASSCD ");
            if ("1".equals(param[4])) {
                stb.append("       AND t1.SCHOOL_KIND = t8.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t8.CURRICULUM_CD ");
            }
            stb.append("    AND t1.SUBCLASSCD = t8.SUBCLASSCD, ");
            stb.append("    SCHREG_BASE_MST t5,SCHREG_REGD_HDAT t7 ");
            stb.append("WHERE ");
            stb.append("    t5.schregno = ? ");
            stb.append("    AND t7.YEAR = '"+param[0]+"' ");
            stb.append("    AND t7.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND t7.GRADE || t7.HR_CLASS = '"+param[2]+"' ");
            stb.append("    AND t1.SUBCLASSCD < '91' ");                        //NO001
            stb.append("ORDER BY ");
            if ("1".equals(param[4])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD,TEXTBOOKDIV ");
log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**PrepareStatement close**/
    private void preStatClose(PreparedStatement ps)
    {
        try {
            ps.close();
        } catch( Exception e ){
            log.warn("preStatClose error!");
        }
    }//preStatClose()の括り

}//クラスの括り
