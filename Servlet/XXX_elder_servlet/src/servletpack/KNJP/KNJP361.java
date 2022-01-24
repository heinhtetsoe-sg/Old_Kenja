// kanji=漢字
/*
 * $Id: 3cd37459070f9cb64684c90f5befe29e33e76b66 $
 *
 * 作成日: 2005/11/27 11:40:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.ServletException;
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
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ３６０＞  授業料軽減補助金
 *
 *  2005/11/27 m-yama 作成
 *  2006/01/21 m-yama NO001 軽減特殊/補助金パラメータの追加
 *                          合計欄を追加
 *  2006/02/16 m-yama NO002 備考欄を出力
 *                    NO003 授業料軽減補助金一覧を追加
 *  2006/02/21 m-yama NO004 合計欄出力変更
 */

public class KNJP361 {


    private static final Log log = LogFactory.getLog(KNJP361.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 57113 $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];

        //パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");               //年度
            param[1]  = request.getParameter("SEMESTER");           //学期
            param[2]  = request.getParameter("REDUC_RARE_CASE_CD"); //軽減特殊コード NO001
            param[3]  = request.getParameter("REDUCTIONMONEY");     //補助金 NO001
            param[6]  = request.getParameter("OUTPUT");             //帳票種別 1:国 2:府県
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

//NO003-->
        String selected[] = request.getParameterValues("CLASS_SELECTED");   //印字対象

        //SQL作成時のIN文の作成
        StringBuffer sql = new StringBuffer();

        if (param[6].equals("1") || param[6].equals("2")){

            sql.append(" ( ");

            String comma = "";
            for (int i = 0; i < selected.length; i++) {
                sql.append(comma);
                sql.append("'").append(selected[i]).append("'");
                comma = ",";
            }
            sql.append(")");
        }
//NO003<--

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力
        //NO003
        if (param[6].equals("1")) {
            if( printKuni(db2,svf,param,sql.toString()) ) nonedata = true;
        } else if (param[6].equals("2")) {
            if( printMain(db2,svf,param,sql.toString()) ) nonedata = true;
        } else {
            if( printItiran(db2,svf,param) ) nonedata = true;
        }

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        //NO003
        if (param[6].equals("1")) {
            svf.VrSetForm("KNJP361_5.frm", 1);
        } else if (param[6].equals("2")) {
            svf.VrSetForm("KNJP361_3.frm", 1);
        } else {
            svf.VrSetForm("KNJP361_4.frm", 4);
        }
    //  作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[4] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
log.debug("heda OK!");
        //年度
        try {
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
            param[10] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])-1) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り

    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String gradeHrclass)
    {
        boolean nonedata = false;
        int gyo = 1;
        int totalcnt  = 0;  //NO001
        int totalmony = 0;  //NO001
        String bifchangepage = "";
        try {
log.debug("printMain start!");

            db2.query(meisaiSql(param,gradeHrclass));
            ResultSet rs = db2.getResultSet();

log.debug("printMain end!");

            while( rs.next() ){

                //１ページ印刷
                if (25 < gyo) {
                    svf.VrEndPage();
                    gyo = 1;
                } else if (gyo > 1 && !bifchangepage.equalsIgnoreCase(rs.getString("CHANGEPAGE"))) {
                    svf.VrsOut("TOTALCNT"           , String.valueOf(totalcnt) );   //NO001
                    svf.VrsOut("TOTALMONEY"     , String.valueOf(totalmony) );  //NO001
                    svf.VrEndPage();
                    gyo = 1;
                    totalcnt  = 0;  //NO001
                    totalmony = 0;  //NO001
                }
                //明細データをセット
                printMeisai(svf,param,rs,gyo);
                nonedata = true;

                gyo++;
                totalcnt++;                                             //NO001
                totalmony = totalmony + rs.getInt("REDUCTIONMONEY_1") + rs.getInt("REDUCTIONMONEY_2");    //NO001
                bifchangepage = rs.getString("CHANGEPAGE");

            }

            //最終ページ印刷
            if (nonedata) {
                svf.VrsOut("TOTALCNT"           , String.valueOf(totalcnt) );   //NO001
                svf.VrsOut("TOTALMONEY"     , String.valueOf(totalmony) );  //NO001
                svf.VrEndPage();
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    /**明細データをセット*/
    private void printMeisai(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        try {

            //ヘッダ
            svf.VrsOut("NENDO"      , param[5] );
            svf.VrsOut("DATE"           , param[4] );
            svf.VrsOut("HR_NAME"        , rs.getString("HR_NAME") );
            svf.VrsOut("TUITION_NENDO1" , param[10] );
            svf.VrsOut("TUITION_NENDO2" , param[5] );
            //明細
            svf.VrsOutn("ATTENDNO"  ,gyo    , rs.getString("ATTENDNO") );
            svf.VrsOutn("PASSNO"    ,gyo    , rs.getString("PASSNO") );
            svf.VrsOutn("NAME"      ,gyo    , rs.getString("NAME") );
            svf.VrsOutn("GUARDIANNAME",gyo  , rs.getString("GUARANTOR_NAME") );
            if (rs.getString("REDUC_DEC_FLG_1") != null && rs.getString("REDUC_DEC_FLG_1").equals("1")){
                svf.VrsOutn("MARK1",gyo  , "*" );
            }
            svf.VrsOutn("RANK1",gyo  , rs.getString("REDUC_RANK_1_NAME") );
            if (rs.getString("REDUC_DEC_FLG_2") != null && rs.getString("REDUC_DEC_FLG_2").equals("1")){
                svf.VrsOutn("MARK2",gyo  , "*" );
            }
            svf.VrsOutn("RANK2",gyo  , rs.getString("REDUC_RANK_2_NAME") );
            svf.VrsOutn("TUITION1"       ,gyo    , rs.getString("REDUC_INCOME_1") );
            svf.VrsOutn("TUITION2"       ,gyo    , rs.getString("REDUC_INCOME_2") );
            if (rs.getString("REDUC_RARE_CASE_CD") != null)
                svf.VrsOutn("SPECIAL"   ,gyo    , rs.getString("REDUC_RARE_CASE_CD") );
            svf.VrsOutn("AID1"       ,gyo    , rs.getString("REDUCTIONMONEY_1") );
            svf.VrsOutn("AID2"       ,gyo    , rs.getString("REDUCTIONMONEY_2") );
            int totalReducMoney = 0;
            if (rs.getString("REDUCTIONMONEY_1") != null || rs.getString("REDUCTIONMONEY_2") != null) {
                totalReducMoney = rs.getInt("REDUCTIONMONEY_1") + rs.getInt("REDUCTIONMONEY_2");
                svf.VrsOutn("TOTAL_AID"  ,gyo    , String.valueOf(totalReducMoney) );
            } else {
                svf.VrsOutn("TOTAL_AID"  ,gyo    , "" );
            }

            if (rs.getString("ADD_PLAN_MONEY1") != null || rs.getString("ADD_PLAN_MONEY2") != null || rs.getString("PLAN_MONEY") != null) {
                totalReducMoney = totalReducMoney + rs.getInt("PLAN_MONEY_TOTAL");
                svf.VrsOutn("TOTAL" ,gyo    , String.valueOf(totalReducMoney) );
            } else {
                svf.VrsOutn("TOTAL" ,gyo    , String.valueOf(totalReducMoney) );
            }

            svf.VrsOutn("PREFECTURE"    ,gyo    , rs.getString("PREF") );
            svf.VrsOutn("REMARK"        ,gyo    , rs.getString("REDUC_REMARK") );

        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り

    /**国メイン*/
    private boolean printKuni(DB2UDB db2,Vrw32alp svf,String param[],String gradeHrclass) {
        boolean nonedata = false;
        int gyo = 1;
        String bifchangepage = "";
        try {
log.debug("printKuni start!");
            db2.query(getKuniSql(param,gradeHrclass));
            ResultSet rs = db2.getResultSet();
log.debug("printKuni end!");

            while (rs.next()) {
                //１ページ印刷
                if (25 < gyo) {
                    svf.VrEndPage();
                    gyo = 1;
                } else if (gyo > 1 && !bifchangepage.equals(rs.getString("CHANGEPAGE"))) {
                    svf.VrEndPage();
                    gyo = 1;
                }
                //明細データをセット
                printKuniDetail(svf,param,rs,gyo);
                nonedata = true;

                gyo++;
                bifchangepage = rs.getString("CHANGEPAGE");
            }

            //最終ページ印刷
            if (nonedata) {
                svf.VrEndPage();
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printKuni read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    /**国データをセット*/
    private void printKuniDetail(Vrw32alp svf,String param[],ResultSet rs,int gyo) {
        try {
            //ヘッダ
            svf.VrsOut("NENDO"          , param[5] );
            svf.VrsOut("DATE"           , param[4] );
            svf.VrsOut("HR_NAME"        , rs.getString("HR_NAME") );
            svf.VrsOut("INCOME_NENDO1"  , param[10] );
            svf.VrsOut("INCOME_NENDO2"  , param[5] );
            //明細
            svf.VrsOutn("ATTENDNO"      ,gyo    , rs.getString("ATTENDNO") );
            svf.VrsOutn("PASSNO"        ,gyo    , rs.getString("PASSNO") );
            svf.VrsOutn("NAME"          ,gyo    , rs.getString("NAME") );
            svf.VrsOutn("GUARDIANNAME"  ,gyo    , rs.getString("GUARANTOR_NAME") );
//            svf.VrsOutn("DEC_MARK1"     ,gyo    , rs.getString("REDUC_DEC_FLG_1") );
//            svf.VrsOutn("ADD_MARK1"     ,gyo    , rs.getString("REDUC_ADD_FLG_1") );
//            svf.VrsOutn("ADD_MARK2"     ,gyo    , rs.getString("REDUC_ADD_FLG_2") );
            svf.VrsOutn("FLG1"          ,gyo    , rs.getString("REDUC_RARE_CASE_CD_1") );
            svf.VrsOutn("FLG2"          ,gyo    , rs.getString("REDUC_RARE_CASE_CD_2") );
            svf.VrsOutn("INCOME1"       ,gyo    , rs.getString("REDUC_INCOME_1") );
            svf.VrsOutn("INCOME2"       ,gyo    , rs.getString("REDUC_INCOME_2") );
            svf.VrsOutn("DEC_MONEY1"    ,gyo    , rs.getString("PLAN_MONEY1") );
            svf.VrsOutn("DEC_MONEY2"    ,gyo    , rs.getString("PLAN_MONEY2") );
            svf.VrsOutn("ADD_MONEY1"    ,gyo    , rs.getString("ADD_PLAN_MONEY1") );
            svf.VrsOutn("ADD_MONEY2"    ,gyo    , rs.getString("ADD_PLAN_MONEY2") );
            if (rs.getString("ADD_PLAN_MONEY1") != null || rs.getString("ADD_PLAN_MONEY2") != null) {
                svf.VrsOutn("TOTAL_ADD" ,gyo    , rs.getString("ADD_PLAN_MONEY_TOTAL") );
            } else {
                svf.VrsOutn("TOTAL_ADD" ,gyo    , "" );
            }
            if (rs.getString("ADD_PLAN_MONEY1") != null || rs.getString("ADD_PLAN_MONEY2") != null || rs.getString("PLAN_MONEY") != null) {
                svf.VrsOutn("TOTAL_DEC" ,gyo    , rs.getString("PLAN_MONEY_TOTAL") );
            } else {
                svf.VrsOutn("TOTAL_DEC" ,gyo    , "" );
            }
            svf.VrsOutn("PREFECTURE"    ,gyo    , rs.getString("PREF") );
            svf.VrsOutn("REMARK"        ,gyo    , rs.getString("REDUC_REMARK") );
        } catch( Exception ex ) {
            log.warn("printKuniDetail read error!",ex);
        }
    }


    /**印刷処理一覧 NO003 */
    private boolean printItiran(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        int classall = 0;
        int number1  = 0;
        int number2  = 0;
        int number3  = 0;
        int ninkei1  = 0;
        int ninkei2  = 0;
        int ninkei3  = 0;
        int kinkei1  = 0;
        int kinkei2  = 0;
        int kinkei3  = 0;
        try {
            String sql = "SELECT COUNT(*) AS GCNT FROM SCHREG_REGD_HDAT WHERE YEAR = '"+param[0]+"' AND SEMESTER = '"+param[1]+"' GROUP BY GRADE ORDER BY GCNT ";
            db2.query(sql);
            ResultSet rs1 = db2.getResultSet();
            while( rs1.next() ){
                classall = Integer.parseInt(rs1.getString("GCNT"));
            }
log.debug("マックスクラス="+classall);
            String kindata1[] = new String[classall];
            String nindata1[] = new String[classall];
            String clsdata1[] = new String[classall];
            String kindata2[] = new String[classall];
            String nindata2[] = new String[classall];
            String clsdata2[] = new String[classall];
            String kindata3[] = new String[classall];
            String nindata3[] = new String[classall];
            String clsdata3[] = new String[classall];
            String nizukei = "0";
            String kingaku = "0";
            rs1.close();
            db2.commit();

            db2.query(itiranSql(param));
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                if (rs.getString("GRADE").equals("01")){
                    kindata1[number1] = rs.getString("KINGAKU");
                    nindata1[number1] = rs.getString("NINZU");
                    clsdata1[number1] = rs.getString("HR_CLASS");
                    number1++;
                }else if (rs.getString("GRADE").equals("02")){
                    kindata2[number2] = rs.getString("KINGAKU");
                    nindata2[number2] = rs.getString("NINZU");
                    clsdata2[number2] = rs.getString("HR_CLASS");
                    number2++;
                }else {
                    kindata3[number3] = rs.getString("KINGAKU");
                    nindata3[number3] = rs.getString("NINZU");
                    clsdata3[number3] = rs.getString("HR_CLASS");
                    number3++;
                }
            }
            rs.close();
            db2.commit();
            for(int i=0;i<classall;i++){
                //ヘッダ
                svf.VrsOut("DATE"               ,param[4]);         //作成日
                svf.VrsOut("NENDO"          ,param[5]);  //年度
                //明細
                if (nindata1[i] == null) nindata1[i] = "0";
                if (kindata1[i] == null) kindata1[i] = "0";
                if (nindata2[i] == null) nindata2[i] = "0";
                if (kindata2[i] == null) kindata2[i] = "0";
                if (nindata3[i] == null) nindata3[i] = "0";
                if (kindata3[i] == null) kindata3[i] = "0";
                if (clsdata1[i] == null) {
                    svf.VrAttribute("CLASS_1",  "Meido=100");       //1年クラス
                    svf.VrAttribute("NINZU_1",  "Meido=100");       //1年人数
                    svf.VrAttribute("KINGAKU_1",    "Meido=100");       //1年金額
                }
                if (clsdata2[i] == null){
                    svf.VrAttribute("CLASS_2",  "Meido=100");       //2年クラス
                    svf.VrAttribute("NINZU_2",  "Meido=100");       //2年人数
                    svf.VrAttribute("KINGAKU_2",    "Meido=100");       //2年金額
                }
                if (clsdata3[i] == null){
                    svf.VrAttribute("CLASS_3",  "Meido=100");       //3年クラス
                    svf.VrAttribute("NINZU_3",  "Meido=100");       //3年人数
                    svf.VrAttribute("KINGAKU_3",    "Meido=100");       //3年金額
                }

                svf.VrsOut("CLASS_1"        ,String.valueOf(clsdata1[i]));      //1年クラス
                svf.VrsOut("NINZU_1"        ,String.valueOf(nindata1[i]));      //1年人数
                svf.VrsOut("KINGAKU_1"  ,String.valueOf(kindata1[i]));      //1年金額
                svf.VrsOut("CLASS_2"        ,String.valueOf(clsdata2[i]));      //2年クラス
                svf.VrsOut("NINZU_2"        ,String.valueOf(nindata2[i]));      //2年人数
                svf.VrsOut("KINGAKU_2"  ,String.valueOf(kindata2[i]));      //2年金額
                svf.VrsOut("CLASS_3"        ,String.valueOf(clsdata3[i]));      //3年クラス
                svf.VrsOut("NINZU_3"        ,String.valueOf(nindata3[i]));      //3年人数
                svf.VrsOut("KINGAKU_3"  ,String.valueOf(kindata3[i]));      //3年金額
                svf.VrEndRecord();
                //小計
                ninkei1 = ninkei1+Integer.parseInt(String.valueOf(nindata1[i]));
                ninkei2 = ninkei2+Integer.parseInt(String.valueOf(nindata2[i]));
                ninkei3 = ninkei3+Integer.parseInt(String.valueOf(nindata3[i]));
                kinkei1 = kinkei1+Integer.parseInt(String.valueOf(kindata1[i]));
                kinkei2 = kinkei2+Integer.parseInt(String.valueOf(kindata2[i]));
                kinkei3 = kinkei3+Integer.parseInt(String.valueOf(kindata3[i]));
                nonedata = true;
            }
            if (nonedata){
                svf.VrsOut("SYOUKEI_1"      ,"小計");       //小計
                svf.VrsOut("SYOUKEININZU_1" ,String.valueOf(ninkei1));      //人数
                svf.VrsOut("SYOUKEIGAKU_1"  ,String.valueOf(kinkei1));      //金額
                svf.VrsOut("SYOUKEI_2"      ,"小計");       //小計
                svf.VrsOut("SYOUKEININZU_2" ,String.valueOf(ninkei2));      //人数
                svf.VrsOut("SYOUKEIGAKU_2"  ,String.valueOf(kinkei2));      //金額
                svf.VrsOut("SYOUKEI_3"      ,"小計");       //小計
                svf.VrsOut("SYOUKEININZU_3" ,String.valueOf(ninkei3));      //人数
                svf.VrsOut("SYOUKEIGAKU_3"  ,String.valueOf(kinkei3));      //金額
                svf.VrEndRecord();
                nizukei = String.valueOf(ninkei1+ninkei2+ninkei3);
                kingaku = String.valueOf(kinkei1+kinkei2+kinkei3);

                //NO004-->
                svf.VrsOut("TOTALITEM"  ,"総人数");     //総人数
                svf.VrsOut("TOTAL"      ,nizukei );     //総人数計
                svf.VrsOut("UNIT"           ,"名" );        //総人数単位
                svf.VrEndRecord();
                svf.VrsOut("TOTALITEM"  ,"総金額");     //総金額
                svf.VrsOut("TOTAL"      ,kingaku );     //総金額計
                svf.VrsOut("UNIT"           ,"円" );        //総金額単位
                svf.VrEndRecord();
                //NO004<--

            }
        } catch( Exception ex ) {
            log.error("printItiran set error!");
        }
        return nonedata;
    }

    /**
     *  クラス別軽減データを抽出（府県）
     *
     */
    private String meisaiSql(String param[],String gradeHrclass)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("  WITH COUNTRY_PLAN AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN PLAN_MONEY ");
            stb.append("                  ELSE 0 END) AS PLAN_MONEY1, ");
            stb.append("         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN 0 ");
            stb.append("                  ELSE PLAN_MONEY END) AS PLAN_MONEY2, ");
            stb.append("         SUM(CASE WHEN ADD_PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN ADD_PLAN_MONEY ");
            stb.append("                  ELSE 0 END) AS ADD_PLAN_MONEY1, ");
            stb.append("         SUM(CASE WHEN ADD_PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN 0 ");
            stb.append("                  ELSE ADD_PLAN_MONEY END) AS ADD_PLAN_MONEY2 ");
            stb.append("     FROM ");
            stb.append("         REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR='"+param[0]+"' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append(" ), COUNTRY_PLAN_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t4.PLAN_MONEY1, ");
            stb.append("     t4.PLAN_MONEY2, ");
            stb.append("     VALUE(t4.ADD_PLAN_MONEY1, 0) as ADD_PLAN_MONEY1, ");
            stb.append("     VALUE(t4.ADD_PLAN_MONEY2, 0) as ADD_PLAN_MONEY2, ");
            stb.append("     (VALUE(t4.ADD_PLAN_MONEY1, 0) + VALUE(t4.ADD_PLAN_MONEY2, 0)) AS ADD_PLAN_MONEY_TOTAL, ");
            stb.append("     (VALUE(t4.ADD_PLAN_MONEY1, 0) + VALUE(t4.ADD_PLAN_MONEY2, 0) + VALUE(t4.PLAN_MONEY1, 0) + VALUE(t4.PLAN_MONEY2, 0)) AS PLAN_MONEY_TOTAL ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT t1 ");
            stb.append("     LEFT JOIN COUNTRY_PLAN t4 ON t4.SCHREGNO = t1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '"+param[0]+"' ");
            stb.append("     AND t1.SEMESTER = '"+param[1]+"' ");
            stb.append("     AND t1.GRADE || t1.HR_CLASS IN "+gradeHrclass+" ");
            stb.append(" ) ");

            stb.append("SELECT ");
            stb.append("    t1.SCHREGNO, ");
            stb.append("    t3.REDUC_INCOME_1, ");
            stb.append("    t3.REDUC_DEC_FLG_1, ");
            stb.append("    t3.REDUC_RANK_1, ");
            stb.append("    t10.NAME1 AS REDUC_RANK_1_NAME, ");
            stb.append("    t3.REDUC_INCOME_2, ");
            stb.append("    t3.REDUC_DEC_FLG_2, ");
            stb.append("    t3.REDUC_RANK_2, ");
            stb.append("    t11.NAME1 AS REDUC_RANK_2_NAME, ");
            stb.append("    t2.HR_NAME, ");
            stb.append("    t1.ATTENDNO, ");
            stb.append("    t9.PASSNO, ");
            stb.append("    t1.GRADE || t1.HR_CLASS || t1.ATTENDNO AS GRD_CLASS, ");
            stb.append("    t1.GRADE || t1.HR_CLASS AS CHANGEPAGE, ");
            stb.append("    t5.NAME, ");
            stb.append("    t8.PREF, ");
            stb.append("    t7.GUARANTOR_NAME, ");
            stb.append("    t4.MONEY_DUE, ");
            stb.append("    t3.REDUCTIONMONEY_1, ");
            stb.append("    t3.REDUCTIONMONEY_2, ");
            stb.append("    t3.REDUC_RARE_CASE_CD, ");
            stb.append("    t3.REDUC_REMARK, ");
            stb.append("    COUNTRY_PLAN_T.PLAN_MONEY1, ");
            stb.append("    COUNTRY_PLAN_T.PLAN_MONEY2, ");
            stb.append("    COUNTRY_PLAN_T.ADD_PLAN_MONEY1, ");
            stb.append("    COUNTRY_PLAN_T.ADD_PLAN_MONEY2, ");
            stb.append("    COUNTRY_PLAN_T.ADD_PLAN_MONEY_TOTAL, ");
            stb.append("    COUNTRY_PLAN_T.PLAN_MONEY_TOTAL ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t1 ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.GRADE || t2.HR_CLASS = t1.GRADE || t1.HR_CLASS ");
            stb.append("    AND t2.YEAR = t1.YEAR ");
            stb.append("    AND t2.SEMESTER = t1.SEMESTER ");
            stb.append("    LEFT JOIN REDUCTION_DAT t3 ON t3.SCHREGNO = t1.SCHREGNO ");
            stb.append("    AND t3.YEAR = t1.YEAR ");
            stb.append("    LEFT JOIN MONEY_DUE_M_DAT t4 ON t4.YEAR = t1.YEAR ");
            stb.append("    AND t4.SCHREGNO = t1.SCHREGNO ");
            stb.append("    AND t4.EXPENSE_M_CD = '13' ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST t5 ON t5.SCHREGNO = t1.SCHREGNO ");
            stb.append("    LEFT JOIN GUARDIAN_DAT t7 ON t7.SCHREGNO = t1.SCHREGNO ");
            stb.append("    LEFT JOIN ( ");
            stb.append("        SELECT ");
            stb.append("            ZIP.NEW_ZIPCD, ");
            stb.append("            MAX(ZIP.PREF) AS PREF ");
            stb.append("        FROM ");
            stb.append("            ZIPCD_MST ZIP ");
            stb.append("        GROUP BY ZIP.NEW_ZIPCD ");
            stb.append("    ) t8 ON t7.GUARANTOR_ZIPCD = t8.NEW_ZIPCD ");
            stb.append("    LEFT JOIN REDUCTION_AUTHORIZE_DAT t9 ON t9.SCHREGNO = t1.SCHREGNO ");
            stb.append("         AND t9.DATA_DIV = '1' ");
            stb.append("         AND t9.DATA_DIV_SUB = '1' ");
            stb.append("    LEFT JOIN NAME_MST t10 ON t10.NAMECD1 = CASE WHEN t1.YEAR < '2016' OR (t1.YEAR = '2016' AND t1.GRADE > '01') OR (t1.YEAR = '2017' AND t1.GRADE = '03') THEN 'G213' ELSE 'G218' END ");
            stb.append("         AND t10.NAMECD2 = T3.REDUC_RANK_1 ");
            stb.append("    LEFT JOIN NAME_MST t11 ON t11.NAMECD1 = CASE WHEN t1.YEAR < '2016' OR (t1.YEAR = '2016' AND t1.GRADE > '01') OR (t1.YEAR = '2017' AND t1.GRADE = '03') THEN 'G213' ELSE 'G218' END ");
            stb.append("         AND t11.NAMECD2 = T3.REDUC_RANK_2 ");
            stb.append("    LEFT JOIN COUNTRY_PLAN_T COUNTRY_PLAN_T ON COUNTRY_PLAN_T.SCHREGNO = t1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND t1.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND t1.GRADE || t1.HR_CLASS IN "+gradeHrclass+" ");
            //NO001
            if (!param[2].equals("99") || (null != param[3] && !param[3].equals(""))){
                stb.append("    AND t1.SCHREGNO IN (SELECT SCHREGNO ");
                stb.append("                        FROM REDUCTION_DAT ");
                stb.append("                        WHERE YEAR = '"+param[0]+"' ");
                if (!param[2].equals("99")){
                    stb.append("                              AND REDUC_RARE_CASE_CD = '"+param[2]+"' ");
                }
                if (null != param[3] && !param[3].equals("")){
                    stb.append("                              AND (value(REDUCTIONMONEY_1,0) + value(REDUCTIONMONEY_2,0)) = "+param[3]+" ");
                }
                stb.append("                        ) ");
            }
            stb.append("ORDER BY ");
            stb.append("    GRD_CLASS ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("meisaiSql error!",e);
        }
        return stb.toString();

    }//meisaiSql()の括り

    /**
     *  クラス別軽減データを抽出（国）
     *
     */
    private String getKuniSql(String param[],String gradeHrclass) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("  WITH COUNTRY_PLAN AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN PLAN_MONEY ");
            stb.append("                  ELSE 0 END) AS PLAN_MONEY1, ");
            stb.append("         SUM(CASE WHEN PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN 0 ");
            stb.append("                  ELSE PLAN_MONEY END) AS PLAN_MONEY2, ");
            stb.append("         SUM(CASE WHEN ADD_PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN ADD_PLAN_MONEY ");
            stb.append("                  ELSE 0 END) AS ADD_PLAN_MONEY1, ");
            stb.append("         SUM(CASE WHEN ADD_PLAN_CANCEL_FLG = '1' THEN 0 ");
            stb.append("                  WHEN PLAN_MONTH IN('04','05','06') THEN 0 ");
            stb.append("                  ELSE ADD_PLAN_MONEY END) AS ADD_PLAN_MONEY2 ");
            stb.append("     FROM ");
            stb.append("         REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR='"+param[0]+"' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.GRADE, ");
            stb.append("     t1.HR_CLASS, ");
            stb.append("     t1.GRADE || t1.HR_CLASS AS CHANGEPAGE, ");
            stb.append("     t2.HR_NAME, ");
            stb.append("     t1.ATTENDNO, ");
            stb.append("     t9.PASSNO, ");
            stb.append("     t5.NAME, ");
            stb.append("     t7.GUARANTOR_NAME, ");
            stb.append("     t3.REDUC_INCOME_1, ");
            stb.append("     t3.REDUC_INCOME_2, ");
            stb.append("     t3.REDUC_RARE_CASE_CD_1, ");
            stb.append("     t3.REDUC_RARE_CASE_CD_2, ");
            stb.append("     t4.PLAN_MONEY1, ");
            stb.append("     t4.PLAN_MONEY2, ");
            stb.append("     VALUE(t4.ADD_PLAN_MONEY1, 0) as ADD_PLAN_MONEY1, ");
            stb.append("     VALUE(t4.ADD_PLAN_MONEY2, 0) as ADD_PLAN_MONEY2, ");
            stb.append("     (VALUE(t4.ADD_PLAN_MONEY1, 0) + VALUE(t4.ADD_PLAN_MONEY2, 0)) AS ADD_PLAN_MONEY_TOTAL, ");
            stb.append("     (VALUE(t4.ADD_PLAN_MONEY1, 0) + VALUE(t4.ADD_PLAN_MONEY2, 0) + VALUE(t4.PLAN_MONEY1, 0) + VALUE(t4.PLAN_MONEY2, 0)) AS PLAN_MONEY_TOTAL, ");
            stb.append("     t8.PREF, ");
            stb.append("     t3.REDUC_REMARK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT t1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.GRADE || t2.HR_CLASS = t1.GRADE || t1.HR_CLASS ");
            stb.append("         AND t2.YEAR = t1.YEAR ");
            stb.append("         AND t2.SEMESTER = t1.SEMESTER ");
            stb.append("     LEFT JOIN REDUCTION_COUNTRY_DAT t3 ON t3.YEAR = t1.YEAR AND t3.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN COUNTRY_PLAN t4 ON t4.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST t5 ON t5.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN GUARDIAN_DAT t7 ON t7.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             ZIP.NEW_ZIPCD, ");
            stb.append("             MAX(ZIP.PREF) AS PREF ");
            stb.append("         FROM ");
            stb.append("             ZIPCD_MST ZIP ");
            stb.append("         GROUP BY ZIP.NEW_ZIPCD ");
            stb.append("     ) t8 ON t7.GUARANTOR_ZIPCD = t8.NEW_ZIPCD ");
            stb.append("     LEFT JOIN REDUCTION_AUTHORIZE_DAT t9 ON t9.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND t9.DATA_DIV = '1' ");
            stb.append("          AND t9.DATA_DIV_SUB = '1' ");
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '"+param[0]+"' ");
            stb.append("     AND t1.SEMESTER = '"+param[1]+"' ");
            stb.append("     AND t1.GRADE || t1.HR_CLASS IN "+gradeHrclass+" ");
            stb.append(" ORDER BY ");
            stb.append("     t1.GRADE, ");
            stb.append("     t1.HR_CLASS, ");
            stb.append("     t1.ATTENDNO ");
        } catch( Exception e ){
            log.warn("getKuniSql error!",e);
        }
        return stb.toString();
    }

    /**
     *  一覧データを抽出 NO003
     *
     */
    private String itiranSql(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    t1.GRADE, ");
            stb.append("    t1.HR_CLASS, ");
            stb.append("    COUNT(*) AS NINZU, ");
            stb.append("    SUM(value(t2.REDUCTIONMONEY_1,0) + value(t2.REDUCTIONMONEY_2,0)) AS KINGAKU ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t1 ");
            stb.append("    LEFT JOIN REDUCTION_DAT t2 ON t2.SCHREGNO = t1.SCHREGNO ");
            stb.append("    AND t2.YEAR = t1.YEAR ");
            stb.append("WHERE ");
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND t1.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND t1.SCHREGNO IN (SELECT SCHREGNO ");
            stb.append("                        FROM REDUCTION_DAT ");
            stb.append("                        WHERE YEAR = '"+param[0]+"' ");
            stb.append("                              AND (REDUCTIONMONEY_1 is not null OR REDUCTIONMONEY_2 is not null) ");
            stb.append("                        ) ");
            stb.append("GROUP BY ");
            stb.append("    t1.GRADE,t1.HR_CLASS ");
            stb.append("ORDER BY ");
            stb.append("    t1.GRADE,t1.HR_CLASS ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("itiranSql error!",e);
        }
        return stb.toString();

    }//itiranSql()の括り

}//クラスの括り
