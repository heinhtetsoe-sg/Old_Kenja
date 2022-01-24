// kanji=漢字
/*
 * $Id: 0f64502b58b0c6d37a723a1162fd696329bb2fd2 $
 *
 * 作成日: 2005/07/26 11:25:40 - JST
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２１＞  机上タックシール（近大）
 *
 *  2005/07/26 nakamoto 作成日
 *  2005/11/07 m-yama   NO002 会場データがリストToリストに変更
 *  2005/12/17 m-yama   NO003 試験区分を名称マスタから、取得する。
 * @author nakamoto
 * @version $Id: 0f64502b58b0c6d37a723a1162fd696329bb2fd2 $
 */
public class KNJL321K {


    private static final Log log = LogFactory.getLog(KNJL321K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[5];

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }

        String classcd[] = request.getParameterValues("HALL_SELECTED");     //会場 NO002
    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 99:全て
            param[4] = request.getParameter("SPECIAL_REASON_DIV");          //特別理由

            //NO003
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMECD2 = '1'";
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                rs.next();
                param[3] = rs.getString("NAME1");
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        //明細データ
        for( int ia=0 ; ia<classcd.length ; ia++ ){
            if( printMeisai(db2,svf,param,classcd[ia]) ) nonedata = true;
        }

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


    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[4] + "' ");
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
    
    /**明細データをセット*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],String classcd)
    {
        boolean nonedata = false;
        svf.VrSetForm("KNJL321.frm", 1);

        try {
log.debug("Meisai start!");
            db2.query(statementMeisai(param,classcd));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int gyo = 1;
            int len = 1;
            String testdiv = "d";
            String hallno = "d";
            while( rs.next() ){
                //１ページ印刷
                if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                    (!hallno.equals("d") && !hallno.equals(rs.getString("EXAMHALLNO"))) ) {
                    setInfluenceName(db2, svf, param);
                    svf.VrEndPage();
                    gyo = 1;len = 1;
                }
                //最終列
                if (len > 4) {
                    len = 1;gyo++;
                    //最終行
                    if (gyo > 5) {
                        setInfluenceName(db2, svf, param);
                        svf.VrEndPage();
                        gyo = 1;
                    }
                }
                //試験区分・受験番号
                if (param[3] != null){
                    svf.VrsOutn("TESTDIV"+String.valueOf(len)     ,gyo ,param[3]);
                }
                svf.VrsOutn("RECEPTNO"+String.valueOf(len)    ,gyo ,String.valueOf(rs.getInt("EXAMNO")));

                testdiv = rs.getString("TESTDIV");
                hallno = rs.getString("EXAMHALLNO");
                len++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                setInfluenceName(db2, svf, param);
                svf.VrEndPage();
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

        return nonedata;

    }//printMeisai()の括り


    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[],String classcd)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,EXAMHALLNO ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[4])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[4] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            stb.append("       AND EXAMHALLNO = '"+classcd+"' ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV,NAME1 AS TEST_NAME, ");
            stb.append("       EXAMHALLNO,EXAMNO ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       INNER JOIN ENTEXAM_HALL_DAT T2 ON T2.TESTDIV=T1.TESTDIV AND T2.EXAMHALLCD=T1.EXAMHALLNO ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("ORDER BY T1.TESTDIV,EXAMHALLNO,EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り



}//クラスの括り
