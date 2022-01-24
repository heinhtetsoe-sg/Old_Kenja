// kanji=漢字
/*
 * $Id: 92fbc5820acee487dd0655fe4cfe08daf91de6aa $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ０１０＞  学籍番号バーコードラベル(通信制)
 *
 *  2005/03/10 m-yama 作成日
 **/

public class KNJM021 {

    private static final Log log = LogFactory.getLog(KNJM021.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[9];
        String schno[] = request.getParameterValues("category_selected");       //生徒
        param[3] = "( ";
        for( int ia=0 ; ia<schno.length ; ia++ ){
            if (ia != 0) param[3] = param[3]+", ";
            param[3] = param[3]+"'";
            param[3] = param[3]+schno[ia];
            param[3] = param[3]+"'";

        }
        param[3] = param[3]+" )";
log.debug("param[3]"+param[3]);
        log.fatal("$Revision: 56595 $");
    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[4] = request.getParameter("GAKKI");           //学期
            param[5] = request.getParameter("GRADE_HR_CLASS");  //クラス
            param[6] = request.getParameter("BUSU");            //部数
log.debug("param[6]"+param[6]);
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps  = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
for(int i=0 ; i<3 ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps  = db2.prepareStatement(preStat(param));
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if (setSvfMain(db2,svf,param,ps)) nonedata = true;  //帳票出力のメソッド

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps);           //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        param[1] = param[0]+"年度";

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[2] = KNJ_EditDate.h_format_thi(returnval.val3,0);
        } catch( Exception ex ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;
    }

    /**
     *  svf print 印刷処理
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps
    ) {
        boolean nonedata = false;
        try {
//          setTitle(svf,param);        //見出しメソッド
            svf.VrSetForm("KNJM020.frm", 4);          //セットフォーム
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                for (int bu = 0;bu < Integer.parseInt(param[6]);bu++){
                    //レコードを出力６単位
                    for (int reccnt = 0;reccnt < 6;reccnt++){
                        //ヘッダ
                        svf.VrsOut("NENDO"    ,param[1]);                         //年度
                        svf.VrsOut("DATE"     ,param[2]);                         //作成日
                        svf.VrsOut("CLASS"       ,rs.getString("HR_NAME"));       //クラス
                        svf.VrsOut("GAKU1"       ,rs.getString("SCHREGNO"));      //生徒番号
                        final String name = rs.getString("NAME");
                        svf.VrsOut("SCHNAME1", name);     //生徒名１
                        //明細
                        svf.VrsOut(KNJ_EditEdit.setformatArea("SCHNAME2", name, 10, "", "_2"), name);     //生徒名１
                        svf.VrsOut("BARCODE1"     ,rs.getString("SCHREGNO"));     //バーコード１
                        svf.VrsOut("GAKU2"       ,rs.getString("SCHREGNO"));      //生徒番号２
                        svf.VrsOut(KNJ_EditEdit.setformatArea("SCHNAME3", name, 10, "", "_2"), name);     //生徒名１
                        svf.VrsOut("BARCODE2"     ,rs.getString("SCHREGNO"));     //バーコード２
                        svf.VrsOut("GAKU3"       ,rs.getString("SCHREGNO"));      //生徒番号３
                        svf.VrEndRecord();
                    }
                }
                nonedata = true;
            }
//          if (nonedata) svf.VrEndRecord();
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!");
        }
        return nonedata;
    }

    /**見出し項目をセット**/
    private void setTitle(
        Vrw32alp svf,
        String param[]
    ) {
        try {
                svf.VrSetForm("KNJM020.frm", 4);          //セットフォーム
                svf.VrsOut("NENDO"    ,param[1]);     //年度
                svf.VrsOut("DATE"     ,param[2]);     //作成日
        } catch( Exception ex ) {
            log.error("setTitle set error!");
        }

    }
    /**データ　取得**/
    private String preStat(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("with atable(SCHREGNO,HR_CLASS,ATTENDNO,HR_NAME) as (SELECT ");
            stb.append("    w1.SCHREGNO, ");
            stb.append("    w1.HR_CLASS, ");
            stb.append("    w1.ATTENDNO, ");
            stb.append("    w2.HR_NAME ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT w1 LEFT JOIN SCHREG_REGD_HDAT w2 on w1.GRADE||w1.HR_CLASS = w2.GRADE||w2.HR_CLASS AND w2.year = '"+param[0]+"' AND w2.semester = '"+param[4]+"' ");
            stb.append("WHERE ");
            stb.append("    w1.YEAR = '"+param[0]+"' AND ");
            stb.append("    w1.SEMESTER = '"+param[4]+"' AND ");
            stb.append("    w1.GRADE||w1.HR_CLASS = '"+param[5]+"' ");
            stb.append(") ");
            stb.append("select w1.SCHREGNO,w1.NAME,w2.HR_CLASS,w2.HR_NAME ");
            stb.append("from schreg_base_mst w1,atable w2 ");
            stb.append("where w1.SCHREGNO = w2.SCHREGNO AND w1.SCHREGNO in "+param[3] );
            stb.append("order by w2.HR_CLASS,w2.ATTENDNO ");
log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps
    ) {
        try {
            ps.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



}//クラスの括り
