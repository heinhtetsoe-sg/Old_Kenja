// kanji=漢字
/*
 * $Id: 06267a3a8fdc002f348b1cc5bd91bffe3e424fdf $
 *
 * 作成日: 2005/09/04 11:25:40 - JST
 * 作成者: nakamoto
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

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *                    ＜ＫＮＪＬ３５３Ｋ＞  出欠表（面談）
 *
 *    2005/09/04 nakamoto 作成日
 *
 *    2006/02/01 m-yama   NO001：中高一貫性は除く
 *    2006/02/08 o-naka   NO002：１ページ５０人までに変更。面接会場名はカット。
 */

public class KNJL353K {


    private static final Log log = LogFactory.getLog(KNJL353K.class);

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
            param[0] = request.getParameter("YEAR");                         //次年度
            param[1] = request.getParameter("OUTPUT2");                       //1:専願,2:併願
            param[2] = request.getParameter("OUTPUT");                         //スポーツ推薦者除く

            param[4] = request.getParameter("EXAMCNT");                     //指定人数
            param[9] = request.getParameter("SPECIAL_REASON_DIV");          //特別理由
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

        getHeaderData(db2,svf,param);                            //見出し出力のメソッド

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

    /** 見出し出力 **/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        //    フォーム
        svf.VrSetForm("KNJL353.frm", 1);

        //    次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            //明細データ
               if( printMeisai(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


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
            int page_cnt = 1;
            String examno_s = "";
            String examno_e = "";
            while( rs.next() ){
                //１ページ印刷
                if ((Integer.parseInt(param[4])-1) < gyo) {
                    //見出し
                       printHeader(db2,svf,param,page_cnt,examno_s,examno_e);
                    svf.VrEndPage();
                    gyo = 0;
                    page_cnt++;
                }
                //明細データをセット
                   printExam(svf,param,rs,gyo);

                if (gyo == 0) examno_s = rs.getString("EXAMNO");
                examno_e = rs.getString("EXAMNO");
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                //見出し
                   printHeader(db2,svf,param,page_cnt,examno_s,examno_e);
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
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],int page_cnt,String examno_s,String examno_e)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , "高等学校" );
            svf.VrsOut("EXAMNO_FROM"  , examno_s );
            svf.VrsOut("EXAMNO_TO"    , examno_e );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printExam(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len = "0";
        String len2 = "0";
        try {
            len  = (gyo < 25) ? "1" : "2" ;//NO002
            gyo  = (gyo < 25) ? gyo+1 : gyo+1-25 ;//NO002
            len2 = (10 < (rs.getString("NAME")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("EXAMNO"+len      ,gyo    , rs.getString("EXAMNO") );
            svf.VrsOutn("KANA"+len        ,gyo    , rs.getString("NAME_KANA") );
            svf.VrsOutn("SEX"+len         ,gyo    , rs.getString("SEX_NAME") );
            svf.VrsOutn("NAME"+len+len2   ,gyo    , rs.getString("NAME") );
        } catch( Exception ex ) {
            log.warn("printExam read error!",ex);
        }

    }//printExam()の括り


    /**
     *    明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,NAME,SEX,NAME_KANA ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[9])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[9] + "' AND ");
            }
            stb.append("           ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') AND ");
            stb.append("           SHDIV = '"+param[1]+"' ");
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN ('2') ");    //NO001
            if (param[1].equals("1") && param[2] != null) //スポーツ推薦者除く
                   stb.append("       AND VALUE(APPLICANTDIV,'4') NOT IN ('3') ");
            if (param[1].equals("2")) //手続済み
                   stb.append("       AND PROCEDUREDIV = '2' ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.EXAMNO,T2.NAME,T2.NAME_KANA, ");
            stb.append("       VALUE(T2.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T2.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("ORDER BY T2.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り



}//クラスの括り
