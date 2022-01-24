// kanji=漢字
/*
 * $Id: 5b00ed104ccd725291cc469e7375c807821c47d7 $
 *
 * 作成日: 2006/01/17 11:25:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３６０＞  スカラシップ認定対象者名簿(高校)
 *
 *  2006/01/17 m-yama 作成日
 *  2006/01/30 m-yama NO001：選択ソートの優先順を指定可にする。（1:男女別,2:専願併願別,3:コース別）
 *  2006/02/04 m-yama NO002：ソート順に成績順を追加。
 *                    NO003：選択ソート単位での改頁を追加。
 *  2006/02/06 m-yama NO004：総頁数取得処理の修正
 *  2006/02/10 m-yama NO005：順位の取得を全体に変更
 *  2006/02/11 m-yama NO006：出力対象を追加
 */
public class KNJL360K {


    private static final Log log = LogFactory.getLog(KNJL360K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[17];        //NO001 NO003 NO004 NO006

    //  パラメータの取得
        String sort[] = request.getParameterValues("L_COURSE");     //1:男女別,2:専／併別,3:コース別 NO001
        try {
            param[0] = request.getParameter("YEAR");                //次年度
            param[1] = request.getParameter("TESTDIV");             //試験区分
            param[2] = request.getParameter("SCALASHIPDIV");        //スカラシップ区分
            param[3] = request.getParameter("SCORE");               //出力対象最低点数
            param[4] = request.getParameter("JHFLG");               //中高フラグ

            param[8] = request.getParameter("SORT");                //選択ソート 1:受験番号順,2:かな氏名順 NO001
            param[9] = request.getParameter("selectdata");          //選択ソート not null:あり,null:なし   NO001
            param[15] = request.getParameter("OUTPUT");             //出力対象   not null:合格者のみ null:全員 NO006
            param[16] = request.getParameter("SPECIAL_REASON_DIV"); //特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
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
            log.error("DB2 open error!",ex);
            return;
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

        //NO001
        String subtitle = getSort(param,sort);

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        if( printMain(db2,svf,param,subtitle) ) nonedata = true;

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[16] + "' ");
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
    
    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        svf.VrSetForm("KNJL360.frm", 1);

    //  次年度
        try {
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  作成日
        try {
            param[6] = KNJ_EditDate.h_format_JP(param[1]);
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    //  スカラシップ名称
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z006' AND NAMECD2 = '"+param[2]+"'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                param[7] = rs.getString("NAME1");
            }
            rs.close();
            db2.commit();
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り

    //NO001
    /**ソート順位設定*/
    private String getSort(String param[],String sort[]){

        StringBuffer ordersql = new StringBuffer();
        StringBuffer ordersql2 = new StringBuffer();    //NO004
        StringBuffer ordersql3 = new StringBuffer();    //NO004
        String comma = "";

        StringBuffer changsql = new StringBuffer();     //NO003
        StringBuffer changsql2 = new StringBuffer();    //NO004
        String paipe = "";                              //NO003

        StringBuffer subtitle = new StringBuffer();
        String period = "";
        ordersql.append("");
        ordersql2.append("");   //NO004
        changsql.append("");    //NO003
        changsql2.append("");   //NO004
        ordersql3.append("");   //NO004
        if (null == param[9] || param[9].equals("")){
            //NO002--->
            if (param[8].equals("1")){
                ordersql.append("t1.EXAMNO");
            }else if (param[8].equals("2")){
                ordersql.append("t1.NAME_KANA");
            }else {
                ordersql.append("RANK");
            }
            ordersql2.append("CHANGE");     //NO004
            changsql.append("'' AS CHANGE");        //NO003
            changsql2.append("'' AS CHANGE");       //NO004
            ordersql3.append("CHANGE");             //NO004
            //<---NO002
        }else {
            for (int i = 0;i < sort.length;i++){
                ordersql.append(comma);
                ordersql2.append(comma);    //NO004
                changsql.append(paipe);     //NO003
                changsql2.append(comma);    //NO004
                ordersql3.append(comma);    //NO004
                subtitle.append(period);
                if (sort[i].equals("1")){
                    ordersql.append("t1.SEX");
                    ordersql2.append("SEX");                    //NO004
                    changsql.append("VALUE(t1.SEX,'')");        //NO003
                    changsql2.append("VALUE(SEX,'') AS SEX");   //NO004
                    ordersql3.append("SEX");                    //NO004
                    subtitle.append("男女");
                }else if (sort[i].equals("2")){
                    ordersql.append("t1.SHDIV");
                    ordersql2.append("SHDIV");                      //NO004
                    changsql.append("VALUE(t1.SHDIV,'')");          //NO003
                    changsql2.append("VALUE(SHDIV,'') AS SHDIV");   //NO004
                    ordersql3.append("SHDIV");                      //NO004
                    subtitle.append("専併");
                }else {
                    ordersql.append("t1.SUC_COURSECODE DESC");
                    ordersql2.append("SUC_COURSECODE DESC");                        //NO004
                    changsql.append("VALUE(t1.SUC_COURSECODE,'')");                 //NO003
                    changsql2.append("VALUE(SUC_COURSECODE,'') AS SUC_COURSECODE"); //NO004
                    ordersql3.append("SUC_COURSECODE");                             //NO004
                    subtitle.append("コース");
                }
                comma  = ",";
                paipe  = "||";  //NO003
                period = "・";
            }
            changsql.append(" AS CHANGE");  //NO003
            ordersql.append(comma);
            //NO002--->
            if (param[8].equals("1")){
                ordersql.append("t1.EXAMNO");
            }else if (param[8].equals("2")){
                ordersql.append("t1.NAME_KANA");
            }else {
                ordersql.append("RANK");
            }
            //<---NO002
        }

        param[10] = ordersql.toString();
        param[11] = changsql.toString();
        param[12] = ordersql2.toString();   //NO004
        param[13] = changsql2.toString();   //NO004
        param[14] = ordersql3.toString();   //NO004

        return subtitle.toString();

    }

    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String subtitle)
    {
        boolean nonedata = false;
        Map total_page = new TreeMap(); //NO004
        try {
            total_page = getTotalPage(db2,svf,param);
            if( printMeisai(db2,svf,param,total_page,subtitle) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り

    /**総ページ数*/
    private Map getTotalPage(DB2UDB db2,Vrw32alp svf,String param[])
    {
        Map pagecnt = new TreeMap();    //NO004
        try {
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();

            int cnt = 0;
            while( rs.next() ){
                pagecnt.put(String.valueOf(cnt),rs.getString("COUNT")); //NO004
                cnt++;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

        return pagecnt;

    }//getTotalPage()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],Map total_page,String subtitle)  //NO004
    {
        boolean nonedata = false;
        try {
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();

            int girlcnt = 0;
            int mancnt  = 0;
            int gyo = 1;
            int pagecnt = 0;    //NO004
            int page_cnt = 1;   //ページ数
            String scalashipdiv = "d";
            String changepage   = "d";  //NO003
            while( rs.next() ){
                //１ページ印刷
                //NO003
                if (50 < gyo || 
                    (!scalashipdiv.equals("d") && !scalashipdiv.equals(rs.getString("SCALASHIPDIV"))) ||
                    (!changepage.equals("d") && !changepage.equals(rs.getString("CHANGE"))) ) {
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 1;
                    //NO003
                    if ((!scalashipdiv.equals("d") && !scalashipdiv.equals(rs.getString("SCALASHIPDIV"))) ||
                        (!changepage.equals("d") && !changepage.equals(rs.getString("CHANGE"))) ) {
                        page_cnt = 1;
                        pagecnt++;  //NO004
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,(String) total_page.get(String.valueOf(pagecnt)),subtitle);   //NO004
                //明細データ
                printScore(svf,param,rs,gyo);
                if (rs.getString("SEX").equals("*")){
                    girlcnt++;
                }else {
                    mancnt++;
                }

                scalashipdiv = rs.getString("SCALASHIPDIV");
                changepage   = rs.getString("CHANGE");  //NO003
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                svf.VrsOut("TOTAL_MEMBER"         ,"男"+String.valueOf(mancnt)+"名,女"+String.valueOf(girlcnt)+"名,合計"+String.valueOf(girlcnt+mancnt)+"名" );
                svf.VrEndPage();
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int page_cnt,String total_page,String subtitle)
    {
        try {
            svf.VrsOut("PRGID"        , "KNJL360" );
            svf.VrsOut("NENDO"        , param[5] );
            svf.VrsOut("TITLEDIV"     , "認定対象者名簿" );
            if (null != subtitle && !subtitle.equals("")){
                svf.VrsOut("SUBTITLE"     , "("+subtitle+")" );
            }else {
                svf.VrsOut("SUBTITLE"     , "" );
            }
            svf.VrsOut("TYPE"         , "タイプ："+param[7] );
            svf.VrsOut("DATE"         , param[6] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , total_page );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len2 = "0";
        try {
            if (null != rs.getString("NAME")){
                len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            }else {
                len2 = "1";
            }

            svf.VrsOutn("ORDER"           ,gyo    ,rs.getString("RANK") );            //順位
            svf.VrsOutn("EXAMNO"          ,gyo    ,rs.getString("EXAMNO") );          //受験番号
            svf.VrsOutn("NAME"+len2       ,gyo    ,rs.getString("NAME") );            //氏名
            svf.VrsOutn("SEX"             ,gyo    ,rs.getString("SEX") );             //性別
            svf.VrsOutn("SHDIV"           ,gyo    ,rs.getString("SHNAME") );          //専併
            svf.VrsOutn("COURSE"          ,gyo    ,rs.getString("EXAMCOURSE_ABBV") ); //合格コース
            svf.VrsOutn("SCORE"           ,gyo    ,rs.getString("SCORE") );           //A得点

            if (null != rs.getString("FINSCHOOL_NAME")){
                len2 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;
            }else {
                len2 = "1";
            }

            svf.VrsOutn("FINSCHOOL"+len2  ,gyo    ,rs.getString("FINSCHOOL_NAME") );  //氏名

        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り

    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH MAINT AS ( ");    //NO005
            stb.append(" SELECT ");
            stb.append("     "+param[11]+", "); //NO003
            stb.append("     RANK() OVER(ORDER BY (value(t1.A_TOTAL,0)) desc) as RANK, ");
            stb.append("     '"+param[2]+"' AS SCALASHIPDIV, ");
            stb.append("     n2.NAME1 AS SCALASHIPNAME, ");
            stb.append("     t1.EXAMNO, ");
            stb.append("     t1.NAME, ");
            stb.append("     CASE WHEN t1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("     t1.SHDIV, ");
            stb.append("     n1.NAME1 AS SHNAME, ");
            stb.append("     VALUE(t1.SUC_COURSECD,'') || VALUE(t1.SUC_MAJORCD,'') || VALUE(t1.SUC_COURSECODE,'') AS PASSCOURSE, ");
            stb.append("     VALUE(t1.SUC_COURSECODE,'') AS SUC_COURSECODE, "); //NO005
            stb.append("     t2.EXAMCOURSE_ABBV, ");
            stb.append("     t1.FS_CD, ");
            stb.append("     t3.FINSCHOOL_NAME, ");
            stb.append("     t1.A_TOTAL AS SCORE, ");
            stb.append("     t1.JUDGEMENT, ");  //NO005
            stb.append("     '' AS REMARK ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("     LEFT JOIN NAME_MST n1 ON n1.NAMECD1 = 'L006' ");
            stb.append("     AND n1.NAMECD2 = t1.SHDIV ");
            stb.append("     LEFT JOIN NAME_MST n2 ON n2.NAMECD1 = 'Z006' ");
            stb.append("     AND n2.NAMECD2 = '"+param[2]+"' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("     AND VALUE(t2.COURSECD,'') || VALUE(t2.MAJORCD,'') || VALUE(t2.EXAMCOURSECD,'') = VALUE(t1.SUC_COURSECD,'') || VALUE(t1.SUC_MAJORCD,'') || VALUE(t1.SUC_COURSECODE,'') ");
            stb.append("     LEFT JOIN FINSCHOOL_MST t3 ON t3.FINSCHOOLCD = t1.FS_CD ");
            stb.append(" WHERE ");
            stb.append("     t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[16])) {
                stb.append("     AND t1.SPECIAL_REASON_DIV = '" + param[16] + "' ");
            }
            stb.append("     AND t1.TESTDIV = '"+param[1]+"' ");
            stb.append(" ) ");  //NO005
//NO005-->
            stb.append(" SELECT * ");
            stb.append(" FROM MAINT t1 ");
            stb.append(" WHERE ");
            stb.append("     VALUE(t1.SCORE,0) >= "+param[3]+" ");
            if (null != param[15]){ //NO006
                stb.append("     AND ((VALUE(t1.JUDGEMENT,'88') > '0' AND VALUE(t1.JUDGEMENT,'88') <= '6') OR VALUE(t1.JUDGEMENT,'88') = '9') ");
            }
            stb.append(" ORDER BY ");
            stb.append("     "+param[10]+" ");
//NO005<--
//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH EXAM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     '"+param[2]+"' AS SCALASHIPDIV,"+param[13]+" ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[16])) {
                stb.append("     AND t1.SPECIAL_REASON_DIV = '" + param[16] + "' ");
            }
            stb.append("     AND TESTDIV = '"+param[1]+"' ");
            stb.append("     AND VALUE(A_TOTAL,0) >= "+param[3]+" ");
            if (null != param[15]){ //NO006
                stb.append("     AND ((VALUE(JUDGEMENT,'88') > '0' AND VALUE(JUDGEMENT,'88') <= '6') OR VALUE(JUDGEMENT,'88') = '9') ");
            }
            stb.append("    ) ");

            //メイン
            stb.append("SELECT SCALASHIPDIV, ");
            stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   EXAM_BASE ");
            stb.append("GROUP BY SCALASHIPDIV,"+param[14]+" ");
            stb.append("ORDER BY SCALASHIPDIV,"+param[12]+" ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
//log.debug(stb);
        return stb.toString();

    }//statementTotalPage()の括り

}//クラスの括り
