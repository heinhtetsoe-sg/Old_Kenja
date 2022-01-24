// kanji=漢字
/*
 * $Id: b253ba5d77209fc3d46e334f5e79326a01b732de $
 *
 * 作成日: 2005/07/28 11:25:40 - JST
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

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２０＞  入学試験出欠表
 *
 *  2005/07/28 nakamoto 作成日
 *  2005/08/23 nakamoto 「附属」「性別」欄を追加し、「*」で印字。氏名欄にふりがなを追加。
 *                      中学は「学科試験時間表」の「１・２・３・４・５」を「点呼・１・２・３・４」に変更。
 *  2005/08/31 m-yama   ふりがなを追加 NO001
 *  2005/11/07 m-yama   NO002 会場データがリストToリストに変更
 *  2005/12/29 nakamoto NO003 印刷日付は指示画面で指定できるように変更
 * @author nakamoto
 * @version $Id: b253ba5d77209fc3d46e334f5e79326a01b732de $
 */
public class KNJL320K {


    private static final Log log = LogFactory.getLog(KNJL320K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[10];

        String classcd[] = request.getParameterValues("HALL_SELECTED");     //会場 NO002
    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 99:全て

            param[3] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[4] = request.getParameter("DATE");                        //印刷日付 NO003
            param[9] = request.getParameter("SPECIAL_REASON_DIV");          //特別理由
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

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力 NO002
        for( int ia=0 ; ia<classcd.length ; ia++ ){
            getHeaderData(db2,svf,param);                           //見出し出力のメソッド
            if( printMain(db2,svf,param,classcd[ia]) ) nonedata = true;
        }

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

    //  フォーム
        svf.VrSetForm("KNJL320.frm", 1);
        setInfluenceName(db2, svf, param);

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        if (param[3].equals("1")) param[6] = "中学校";
        if (param[3].equals("2")) param[6] = "高等学校";

    //  作成日
        try {
            param[8] = KNJ_EditDate.h_format_JP(param[4]);//NO003
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String classcd)
    {
        boolean nonedata = false;

        try {
            //明細データ
            if( printMeisai(db2,svf,param,classcd) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],String classcd)
    {
        boolean nonedata = false;
        try {
            db2.query(statementMeisai(param,classcd));
            ResultSet rs = db2.getResultSet();

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            String testdiv = "d";
            String hallno = "d";
            while( rs.next() ){
                //１ページ印刷
                if (49 < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                    (!hallno.equals("d") && !hallno.equals(rs.getString("EXAMHALLNO"))) ) {
                    //合計印刷
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                        (!hallno.equals("d") && !hallno.equals(rs.getString("EXAMHALLNO"))) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                        (!hallno.equals("d") && !hallno.equals(rs.getString("EXAMHALLNO"))) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;
                    }
                }
                //見出し
                printHeader(svf,param,rs);
                //明細データをセット
                printExam(svf,param,rs,gyo);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = rs.getString("TESTDIV");
                hallno = rs.getString("EXAMHALLNO");
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
                printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
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
    private void printHeader(Vrw32alp svf,String param[],ResultSet rs)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("EXAM_PLACE"   , rs.getString("EXAMHALL_NAME") );
            svf.VrsOut("SE_EXAMNO"    , "受験番号：" + rs.getString("S_EXAMNO") + " \uFF5E " + rs.getString("E_EXAMNO") );
            //NO002
            if (param[3].equals("1")){
                svf.VrsOut("ITEM1"    , "附属" );
                svf.VrsOut("ITEM2"    , "附属" );
            }
            //学科試験時間表の項目名をセット---2005.08.23
            for (int ia = 1; ia < 3; ia++) {
                //中学
                if (param[3].equals("1")) {
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_1" , "点呼" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_2" , "１" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_3" , "２" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_4" , "３" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_5" , "４" );
                //高校
                } else {
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_1" , "１" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_2" , "２" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_3" , "３" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_4" , "４" );
                    svf.VrsOut("PERIOD"+String.valueOf(ia)+"_5" , "５" );
                }
            }
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
            len  = (gyo < 25) ? "1" : "2" ;
            gyo  = (gyo < 25) ? gyo+1 : gyo+1-25 ;
            len2 = (10 < (rs.getString("NAME")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("EXAMNO"+len      ,gyo    , rs.getString("EXAMNO") );
            svf.VrsOutn("NAME"+len+len2   ,gyo    , rs.getString("NAME") );

            //NO002
            if (param[3].equals("1")){
                svf.VrsOutn("ATTACH"+len      ,gyo    , rs.getString("NATPUB_NAME") );//2005.08.23
            }
            svf.VrsOutn("SEX"+len         ,gyo    , rs.getString("SEX_NAME") );   //2005.08.23
            svf.VrsOutn("KANA"+len        ,gyo    , rs.getString("NAME_KANA") );  //NO001
        } catch( Exception ex ) {
            log.warn("printExam read error!",ex);
        }

    }//printExam()の括り


    /**合計をセット*/
    private void printTotal(Vrw32alp svf,String param[],int sex1_cnt,int sex2_cnt,int sex_cnt)
    {
        try {
            svf.VrsOut("TOTAL_MEMBER" , "男" + String.valueOf(sex1_cnt) + "名、" + 
                                              "女" + String.valueOf(sex2_cnt) + "名、" + 
                                              "合計" + String.valueOf(sex_cnt) + "名" );
        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }//printTotal()の括り


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
            stb.append("    SELECT TESTDIV,EXAMNO,EXAMHALLNO,NAME,SEX,NATPUBPRIDIV,NAME_KANA ");//2005.08.23
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            stb.append("       AND EXAMHALLNO = '"+classcd+"' ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.TESTDIV,N3.NAME1 AS TEST_NAME, ");
            stb.append("       T2.EXAMHALLNO,T1.EXAMHALL_NAME, ");
            stb.append("       VALUE(T1.S_EXAMNO,'') AS S_EXAMNO,VALUE(T1.E_EXAMNO,'') AS E_EXAMNO, ");
            stb.append("       T2.EXAMNO,T2.NAME, ");
            stb.append("       VALUE(T2.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T2.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ");//2005.08.23
            stb.append("       T2.NAME_KANA,T2.NATPUBPRIDIV, ");//2005.08.23
            stb.append("       CASE WHEN T2.NATPUBPRIDIV = '9' THEN '*' ELSE NULL END AS NATPUB_NAME ");//2005.08.23
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       INNER JOIN ENTEXAM_HALL_DAT T1 ON T1.TESTDIV=T2.TESTDIV AND T1.EXAMHALLCD=T2.EXAMHALLNO ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
            stb.append("ORDER BY T2.TESTDIV,T2.EXAMHALLNO,T2.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り



}//クラスの括り
