// kanji=漢字
/*
 * $Id: f3f2eb93f18b5561d1d73db45f9767199f5c030d $
 *
 * 作成日: 2006/01/17 11:25:40 - JST
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 *    学校教育システム 賢者 [入試管理]
 *
 *                    ＜ＫＮＪＬ３６１Ｋ＞  スカラシップ認定者名簿(高校)
 *
 *    2006/01/17 m-yama 作成日
 *    2006/02/04 m-yama NO001 ソート順処理を追加
 *    2006/02/10 m-yama NO002：順位の取得を全体に変更
 *    2006/02/11 m-yama NO003：対象の条件に合格者を追加する
 */
public class KNJL361K {


    private static final Log log = LogFactory.getLog(KNJL361K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();             //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        String param[] = new String[10];

    //    パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                 //次年度
            param[1] = request.getParameter("TESTDIV");                //試験区分
            param[2] = request.getParameter("SCALASHIPDIV");        //スカラシップ区分
            param[3] = request.getParameter("SCORE");                //出力対象最低点数
            param[4] = request.getParameter("JHFLG");                //中高フラグ
            param[7] = request.getParameter("SORT");                //ソート順 NO001
            param[9] = request.getParameter("SPECIAL_REASON_DIV");  //特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //    print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //    svf設定
        svf.VrInit();                               //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //    ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


    //    ＳＶＦ作成処理
        boolean nonedata = false;                                 //該当データなしフラグ

        getHeaderData(db2,svf,param);                            //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

           if( printMain(db2,svf,param) ) nonedata = true;

log.debug("nonedata="+nonedata);

    //    該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //     終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[9] + "' ");
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

    //    次年度
        try {
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //    作成日
        try {
            param[6] = KNJ_EditDate.h_format_JP(param[1]);
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        String total_page = "";
        try {
            //総ページ数
            List total_code = getTotalCode(db2,svf,param);//スカラシップ種別数
            for (final Iterator it = total_code.iterator(); it.hasNext();) {
                final String scaladiv = (String) it.next();
                total_page = getTotalPage(db2,svf,param,scaladiv);
                   if( printMeisai(db2,svf,param,total_page,scaladiv) ) nonedata = true;
            }
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**スカラシップ種別数*/
    private List getTotalCode(DB2UDB db2,Vrw32alp svf,String param[])
    {
        List ret_val = new ArrayList();
        try {

            db2.query(statementTotalCode(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                ret_val.add(rs.getString("SCALASHIPDIV"));
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalCode read error!",ex);
        }
        return ret_val;

    }//getTotalCode()の括り


    /**総ページ数*/
    private String getTotalPage(DB2UDB db2,Vrw32alp svf,String param[],String scaladiv)
    {
        String pagecnt = "";
        try {
            db2.query(statementTotalPage(param,scaladiv));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                pagecnt = rs.getString("COUNT");
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

        return pagecnt;

    }//getTotalPage()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],String total_page,String scaladiv)
    {
        boolean nonedata = false;
        try {
            db2.query(statementMeisai(param,scaladiv));
            ResultSet rs = db2.getResultSet();

            int girlcnt = 0;
            int mancnt  = 0;
            int gyo = 1;
            int page_cnt = 1;   //ページ数
            String scalashipdiv = "d";
            while( rs.next() ){
                //１ページ印刷
                if (50 < gyo || 
                    (!scalashipdiv.equals("d") && !scalashipdiv.equals(rs.getString("SCALASHIPDIV")))) {
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 1;
                    if ((!scalashipdiv.equals("d") && !scalashipdiv.equals(rs.getString("SCALASHIPDIV"))) ) {
                        page_cnt = 1;
                    }
                }
                //見出し
                   printHeader(db2,svf,param,rs,page_cnt,total_page);
                //明細データ
                   printScore(svf,param,rs,gyo);
                if (rs.getString("SEX").equals("*")){
                    girlcnt++;
                }else {
                    mancnt++;
                }

                scalashipdiv = rs.getString("SCALASHIPDIV");
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                svf.VrsOut("TOTAL_MEMBER", "男"+String.valueOf(mancnt)+"名,女"+String.valueOf(girlcnt)+"名,合計"+String.valueOf(girlcnt+mancnt)+"名" );
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
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int page_cnt,String total_page)
    {
        try {
            svf.VrsOut("PRGID"        , "KNJL361K" );
            svf.VrsOut("NENDO"        , param[5] );
            svf.VrsOut("TITLEDIV"     , "認定者名簿" );
            svf.VrsOut("SUBTITLE"     , "(タイプ："+rs.getString("SCALASHIPNAME")+")" );
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

            svf.VrsOutn("ORDER"            ,gyo    ,rs.getString("RANK") );            //順位
            svf.VrsOutn("EXAMNO"            ,gyo    ,rs.getString("EXAMNO") );            //受験番号
            svf.VrsOutn("NAME"+len2        ,gyo    ,rs.getString("NAME") );            //氏名
            svf.VrsOutn("SEX"                ,gyo    ,rs.getString("SEX") );                //性別
            svf.VrsOutn("SHDIV"            ,gyo    ,rs.getString("SHNAME") );            //専併
            svf.VrsOutn("COURSE"            ,gyo    ,rs.getString("EXAMCOURSE_ABBV") );    //合格コース
            svf.VrsOutn("SCORE"            ,gyo    ,rs.getString("SCORE") );            //A得点

            if (null != rs.getString("FINSCHOOL_NAME")){
                len2 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;
            }else {
                len2 = "1";
            }

            svf.VrsOutn("FINSCHOOL"+len2    ,gyo    ,rs.getString("FINSCHOOL_NAME") );    //氏名

        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り

    /**
     *    明細データを抽出
     *
     */
    private String statementMeisai(String param[],String scaladiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH MAINT AS ( ");    //NO002
            stb.append(" SELECT ");
            stb.append("     RANK() OVER(ORDER BY (value(t1.A_TOTAL,0)) desc) as RANK, ");
            stb.append("     t1.SCALASHIPDIV, ");
            stb.append("     n2.NAME1 AS SCALASHIPNAME, ");
            stb.append("     t1.EXAMNO, ");
            stb.append("     t1.NAME, ");
            stb.append("     t1.NAME_KANA, ");    //NO002
            stb.append("     CASE WHEN t1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("     t1.SHDIV, ");
            stb.append("     n1.NAME1 AS SHNAME, ");
            stb.append("     VALUE(t1.SUC_COURSECD,'') || VALUE(t1.SUC_MAJORCD,'') || VALUE(t1.SUC_COURSECODE,'') AS PASSCOURSE, ");
            stb.append("     t2.EXAMCOURSE_ABBV, ");
            stb.append("     t1.JUDGEMENT, ");    //NO003
            stb.append("     t1.FS_CD, ");
            stb.append("     t3.FINSCHOOL_NAME, ");
            stb.append("     t1.A_TOTAL AS SCORE, ");
            stb.append("     '' AS REMARK ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("     LEFT JOIN NAME_MST n1 ON n1.NAMECD1 = 'L006' ");
            stb.append("     AND n1.NAMECD2 = t1.SHDIV ");
            stb.append("     LEFT JOIN NAME_MST n2 ON n2.NAMECD1 = 'Z006' ");
            stb.append("     AND n2.NAMECD2 = t1.SCALASHIPDIV ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("     AND VALUE(t2.COURSECD,'') || VALUE(t2.MAJORCD,'') || VALUE(t2.EXAMCOURSECD,'') = VALUE(t1.SUC_COURSECD,'') || VALUE(t1.SUC_MAJORCD,'') || VALUE(t1.SUC_COURSECODE,'') ");
            stb.append("     LEFT JOIN FINSCHOOL_MST t3 ON t3.FINSCHOOLCD = t1.FS_CD ");
            stb.append(" WHERE ");
            stb.append("     t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("     AND t1.SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("     AND t1.TESTDIV = '"+param[1]+"' ");
            stb.append(" ) ");    //NO002
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     MAINT ");
            stb.append(" WHERE ");
            stb.append("     SCALASHIPDIV = '"+scaladiv+"' ");
            stb.append("     AND ((VALUE(JUDGEMENT,'88') > '0' AND VALUE(JUDGEMENT,'88') <= '6') OR VALUE(JUDGEMENT,'88') = '9') ");    //NO003
            stb.append(" ORDER BY ");
            stb.append("     SCALASHIPDIV ");    //NO001
            if ("1".equals(param[7])) {
                stb.append("     ,EXAMNO ");
            }else {
                stb.append("     ,NAME_KANA ");
            }

        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        //log.debug(stb);
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *    総ページ数を取得
     *
     */
    private String statementTotalPage(String param[],String scaladiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH EXAM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCALASHIPDIV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("     AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("     AND TESTDIV = '"+param[1]+"' ");
            stb.append("     AND SCALASHIPDIV = '"+scaladiv+"' ");
            stb.append("     AND ((VALUE(JUDGEMENT,'88') > '0' AND VALUE(JUDGEMENT,'88') <= '6') OR VALUE(JUDGEMENT,'88') = '9') ");    //NO003
            stb.append("    ) ");

            //メイン
            stb.append("SELECT SCALASHIPDIV, ");
            stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   EXAM_BASE ");
            stb.append("GROUP BY SCALASHIPDIV ");
            stb.append("ORDER BY SCALASHIPDIV ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り


    /**
     *    スカラシップ種別数を取得
     *
     */
    private String statementTotalCode(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     SCALASHIPDIV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("     AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("     AND TESTDIV = '"+param[1]+"' ");
            if (null != param[2] && param[2].equals("99")){
                stb.append("     AND SCALASHIPDIV IS NOT NULL ");
                stb.append("     AND SCALASHIPDIV != '' ");
                stb.append("     AND ((VALUE(JUDGEMENT,'88') > '0' AND VALUE(JUDGEMENT,'88') <= '6') OR VALUE(JUDGEMENT,'88') = '9') ");    //NO003
            }else {
                stb.append("     AND SCALASHIPDIV = '"+param[2]+"' ");
                stb.append("     AND ((VALUE(JUDGEMENT,'88') > '0' AND VALUE(JUDGEMENT,'88') <= '6') OR VALUE(JUDGEMENT,'88') = '9') ");    //NO003
            }
            stb.append(" GROUP BY ");
            stb.append("     SCALASHIPDIV ");
        } catch( Exception e ){
            log.warn("statementTotalCode error!",e);
        }
        //log.debug(stb);
        return stb.toString();

    }//statementTotalCode()の括り



}//クラスの括り
