// kanji=漢字
/*
 * $Id: 552057d3e0961130525ff5eef96b0bcb5aa602ae $
 *
 * 作成日: 2005/08/13 11:25:40 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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
 *                  ＜ＫＮＪＬ３０５Ｋ＞  事前相談コピー済（削除対象）リスト
 *
 * @author nakamoto
 * @version $Id: 552057d3e0961130525ff5eef96b0bcb5aa602ae $
 */

public class KNJL305K {


    private static final Log log = LogFactory.getLog(KNJL305K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[10];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 1:前期,2:後期

            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
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

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        if( printMain(db2,svf,param) ) nonedata = true;

log.debug("nonedata="+nonedata);

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


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        svf.VrSetForm("KNJL305.frm", 1);

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        if (param[5].equals("1")) param[6] = "中学校";
        if (param[5].equals("2")) param[6] = "高等学校";

    //  試験区分
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='L003' AND NAMECD2='"+param[1]+"' ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                param[4] = rs.getString("NAME1");
            }
            db2.commit();
        } catch( Exception e ){
            log.warn("testname get error!",e);
        }

    //  作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[8] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            getTotalPage(db2,svf,param);

            //明細データ
            if( printMeisai(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**試験区分毎の総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
log.debug("TotalPage start!");
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();
log.debug("TotalPage end!");

            param[9] = "1";
            while( rs.next() ){
                param[9] = rs.getString("COUNT");      //総ページ数
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

    }//getTotalPage()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
log.debug("Meisai start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int gyo = 0;
            int page_cnt = 1;   //ページ数
            while( rs.next() ){
                //１ページ印刷
                if (49 < gyo) {
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                }
                //見出し
                printHeader(svf,param,rs,page_cnt);
                //明細データ
                printScore(svf,param,rs,gyo);

                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) svf.VrEndPage();
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(Vrw32alp svf,String param[],ResultSet rs,int page_cnt)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (param[4] != null) svf.VrsOut("TESTDIV"      , param[4] );
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , param[9] );
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len1 = "0";
        String len2 = "0";
        try {
            len1 = (10 < (rs.getString("CPM_NAME")).length()) ? "_2" : "_1" ;
            len2 = (10 < (rs.getString("CPM_KANA")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("ACCEPTNO1"   ,gyo+1  , rs.getString("CPM_NO") );
            svf.VrsOutn("NAME1"+len1  ,gyo+1  , rs.getString("CPM_NAME") );
            svf.VrsOutn("KANA1"+len2  ,gyo+1  , rs.getString("CPM_KANA") );

            svf.VrsOutn("ACCEPTNO2"   ,gyo+1  , rs.getString("CPS_NO") );
            svf.VrsOutn("NAME2"+len1  ,gyo+1  , rs.getString("CPS_NAME") );
            svf.VrsOutn("KANA2"+len2  ,gyo+1  , rs.getString("CPS_KANA") );

            svf.VrsOutn("FINSCHOOL"   ,gyo+1  , rs.getString("FS_MARK") );
            svf.VrsOutn("PRISCHOOL"   ,gyo+1  , rs.getString("PS_MARK") );
        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


    /**
     *  明細データを抽出（前期）
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //事前相談ヘッダ
            stb.append("WITH EXAM_CONSUL AS ( ");
            stb.append("    SELECT ACCEPTNO,PS_ACCEPTNO,FS_ACCEPTNO,NAME,NAME_KANA ");
            stb.append("    FROM   ENTEXAM_CONSULTATION_HDAT ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           TESTDIV='"+param[1]+"' ");
            stb.append("    ) ");
            //「受付Noと塾：受付No」または「受付Noと出身学校：受付No」が異なるデータ
            stb.append(",EXAM_CONSUL1 AS ( ");
            stb.append("    SELECT ACCEPTNO AS CPS_NO, PS_ACCEPTNO AS CPM_NO, ");
            stb.append("           VALUE(NAME,'') AS CPS_NAME, VALUE(NAME_KANA,'') AS CPS_KANA, 'PS' AS MARK ");
            stb.append("    FROM   EXAM_CONSUL ");
            stb.append("    WHERE  ACCEPTNO<>PS_ACCEPTNO ");
            stb.append("    UNION ");
            stb.append("    SELECT ACCEPTNO AS CPS_NO, FS_ACCEPTNO AS CPM_NO, ");
            stb.append("           VALUE(NAME,'') AS CPS_NAME, VALUE(NAME_KANA,'') AS CPS_KANA, 'FS' AS MARK ");
            stb.append("    FROM   EXAM_CONSUL ");
            stb.append("    WHERE  ACCEPTNO<>FS_ACCEPTNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.CPM_NO, VALUE(T2.NAME,'') AS CPM_NAME, VALUE(T2.NAME_KANA,'') AS CPM_KANA, ");
            stb.append("       T1.CPS_NO, T1.CPS_NAME, T1.CPS_KANA, ");
            stb.append("       MAX(CASE WHEN T1.MARK = 'FS' THEN '○' ELSE NULL END) AS FS_MARK, ");
            stb.append("       MAX(CASE WHEN T1.MARK = 'PS' THEN '○' ELSE NULL END) AS PS_MARK ");
            stb.append("FROM   EXAM_CONSUL1 T1 ");
            stb.append("       LEFT JOIN EXAM_CONSUL T2 ON T2.ACCEPTNO=T1.CPM_NO ");
            stb.append("GROUP BY T1.CPM_NO, T2.NAME, T2.NAME_KANA, T1.CPS_NO, T1.CPS_NAME, T1.CPS_KANA ");
            stb.append("ORDER BY T1.CPM_NO, T1.CPS_NO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  試験区分毎の総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //事前相談ヘッダ
            stb.append("WITH EXAM_CONSUL AS ( ");
            stb.append("    SELECT ACCEPTNO,PS_ACCEPTNO,FS_ACCEPTNO,NAME,NAME_KANA ");
            stb.append("    FROM   ENTEXAM_CONSULTATION_HDAT ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("           TESTDIV='"+param[1]+"' ");
            stb.append("    ) ");
            //「受付Noと塾：受付No」または「受付Noと出身学校：受付No」が異なるデータ
            stb.append(",EXAM_CONSUL1 AS ( ");
            stb.append("    SELECT ACCEPTNO AS CPS_NO, PS_ACCEPTNO AS CPM_NO, ");
            stb.append("           NAME AS CPS_NAME, NAME_KANA AS CPS_KANA, 'PS' AS MARK ");
            stb.append("    FROM   EXAM_CONSUL ");
            stb.append("    WHERE  ACCEPTNO<>PS_ACCEPTNO ");
            stb.append("    UNION ");
            stb.append("    SELECT ACCEPTNO AS CPS_NO, FS_ACCEPTNO AS CPM_NO, ");
            stb.append("           NAME AS CPS_NAME, NAME_KANA AS CPS_KANA, 'FS' AS MARK ");
            stb.append("    FROM   EXAM_CONSUL ");
            stb.append("    WHERE  ACCEPTNO<>FS_ACCEPTNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   ( ");
            stb.append("SELECT T1.CPM_NO, T2.NAME AS CPM_NAME, T2.NAME_KANA AS CPM_KANA, ");
            stb.append("       T1.CPS_NO, T1.CPS_NAME, T1.CPS_KANA, ");
            stb.append("       MAX(CASE WHEN T1.MARK = 'FS' THEN '○' ELSE NULL END) AS FS_MARK, ");
            stb.append("       MAX(CASE WHEN T1.MARK = 'PS' THEN '○' ELSE NULL END) AS PS_MARK ");
            stb.append("FROM   EXAM_CONSUL1 T1 ");
            stb.append("       LEFT JOIN EXAM_CONSUL T2 ON T2.ACCEPTNO=T1.CPM_NO ");
            stb.append("GROUP BY T1.CPM_NO, T2.NAME, T2.NAME_KANA, T1.CPS_NO, T1.CPS_NAME, T1.CPS_KANA ");
            stb.append("       ) T1 ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
