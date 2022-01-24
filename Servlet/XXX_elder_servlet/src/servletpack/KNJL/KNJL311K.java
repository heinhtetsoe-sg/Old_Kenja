// kanji=漢字
/*
 * $Id: f25c03cbfaa07bb2dbbbf325a9fed004b6cbb4cb $
 *
 * 作成日: 2009/10/19 11:25:40 - JST
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
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１１Ｋ＞  入学試験志願者名索引（近大）
 *
 *  2005/07/27 nakamoto 作成日
 *
 *  2005/10/27 nakamoto ・出身学校の上段は、所在地コードを表記する。
 *  2005/12/29 nakamoto NO001 出身学校の上段は、名称を表記する。
 *                      NO002 大阪府下の公立は、左横に'*'を印字する。
 *  2006/01/25 nakamoto NO003 ○志願者基礎データの出願区分が'2'の者を除く
 * @author nakamoto
 * @version $Id: f25c03cbfaa07bb2dbbbf325a9fed004b6cbb4cb $
 */
public class KNJL311K {


    private static final Log log = LogFactory.getLog(KNJL311K.class);

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
        //五十音リスト
        String classcd[] = request.getParameterValues("DATA_SELECTED");
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 99:全て

            param[3] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
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

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        for( int ia=0 ; ia<classcd.length ; ia++ )
            if( printMain(db2,svf,param,classcd[ia]) ) nonedata = true;

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

        svf.VrSetForm("KNJL311.frm", 1);
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
    
    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String classcd)
    {
        boolean nonedata = false;

        try {
            //五十音変換
            getConvertKana(classcd,param);  //五十音変換メソッド
            //総ページ数
            getTotalPage(db2,svf,param);

            //明細データ
            if( printMeisai(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**試験区分・五十音毎の総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                param[5] = rs.getString("COUNT");
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
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            while( rs.next() ){
                //１ページ印刷
                if (49 < gyo) {
                    svf.VrEndPage();
                    page_cnt++;gyo = 0;
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt);
                //明細データ
                printExam(svf,param,rs,gyo);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

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
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int page_cnt)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            svf.VrsOut("TESTDIV"      , (!param[1].equals("99")) ? rs.getString("TEST_NAME") : "前・後期" );
            svf.VrsOut("SEARCH"       , "【" + param[4] + "】" );
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , param[5] );
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
        String len3 = "0";
        try {
            len = (gyo < 25) ? "1" : "2" ;
            gyo = (gyo < 25) ? gyo+1 : gyo+1-25 ;
            len2 = (10 < (rs.getString("NAME")).length()) ? "_2" : "_1" ;
            len3 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "_2" : "_1" ;

            svf.VrsOutn("EXAMNO"+len          ,gyo      , rs.getString("EXAMNO") );
            svf.VrsOutn("KANA"+len            ,gyo      , rs.getString("NAME_KANA") );
            svf.VrsOutn("LOCATION"+len        ,gyo      , rs.getString("LOCATION_NAME") );//NO001

            svf.VrsOutn("NAME"+len+len2       ,gyo      , rs.getString("NAME") );
            svf.VrsOutn("FINSCHOOL"+len+len3  ,gyo      , rs.getString("FINSCHOOL_NAME") );
            svf.VrsOutn("FINSCHOOL_CHECK"+len ,gyo      , rs.getString("FINSCHOOL_CHECK") );//NO002
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


    /** 五十音変換・・・指示画面からのパラメータ(0〜45)をひらがなに変換 **/
    private void getConvertKana(String classcd,String param[]){

    //  ひらがな
        String obj1[] = {"あ","い","う","え","お","か","き","く","け","こ","さ","し","す","せ","そ",
                         "た","ち","つ","て","と","な","に","ぬ","ね","の","は","ひ","ふ","へ","ほ",
                         "ま","み","む","め","も","や","ゆ","よ","ら","り","る","れ","ろ","わ","を","ん"};
        String obj2[] = {"no","no","no","no","no","が","ぎ","ぐ","げ","ご","ざ","じ","ず","ぜ","ぞ",
                         "だ","ぢ","づ","で","ど","no","no","no","no","no","ば","び","ぶ","べ","ぼ",
                         "no","no","no","no","no","no","no","no","no","no","no","no","no","no","no","no"};
        String obj3[] = {"no","no","no","no","no","no","no","no","no","no","no","no","no","no","no",
                         "no","no","no","no","no","no","no","no","no","no","ぱ","ぴ","ぷ","ぺ","ぽ",
                         "no","no","no","no","no","no","no","no","no","no","no","no","no","no","no","no"};
    //  カタカナ
        String obj4[] = {"ア","イ","ウ","エ","オ","カ","キ","ク","ケ","コ","サ","シ","ス","セ","ソ",
                         "タ","チ","ツ","テ","ト","ナ","ニ","ヌ","ネ","ノ","ハ","ヒ","フ","ヘ","ホ",
                         "マ","ミ","ム","メ","モ","ヤ","ユ","ヨ","ラ","リ","ル","レ","ロ","ワ","ヲ","ン"};
        String obj5[] = {"no","no","ヴ","no","no","ガ","ギ","グ","ゲ","ゴ","ザ","ジ","ズ","ゼ","ゾ",
                         "ダ","ヂ","ヅ","デ","ド","no","no","no","no","no","バ","ビ","ブ","ベ","ボ",
                         "no","no","no","no","no","no","no","no","no","no","no","no","no","no","no","no"};
        String obj6[] = {"no","no","no","no","no","no","no","no","no","no","no","no","no","no","no",
                         "no","no","no","no","no","no","no","no","no","no","パ","ピ","プ","ペ","ポ",
                         "no","no","no","no","no","no","no","no","no","no","no","no","no","no","no","no"};
        try {
            param[2] = "(";
        //  ひらがな
            param[2] = param[2] + "'" + obj1[Integer.parseInt(classcd)] + "'";
            if( !(obj2[Integer.parseInt(classcd)]).equals("no") )
                param[2] = param[2] + "," + "'" + obj2[Integer.parseInt(classcd)] + "'";
            if( !(obj3[Integer.parseInt(classcd)]).equals("no") )
                param[2] = param[2] + "," + "'" + obj3[Integer.parseInt(classcd)] + "'";
        //  カタカナ
            param[2] = param[2] + "," + "'" + obj4[Integer.parseInt(classcd)] + "'";
            if( !(obj5[Integer.parseInt(classcd)]).equals("no") )
                param[2] = param[2] + "," + "'" + obj5[Integer.parseInt(classcd)] + "'";
            if( !(obj6[Integer.parseInt(classcd)]).equals("no") )
                param[2] = param[2] + "," + "'" + obj6[Integer.parseInt(classcd)] + "'";
            param[2] = param[2] + ")";

            param[4] = obj1[Integer.parseInt(classcd)];
        } catch( Exception e ){
            log.warn("getConvertKana get error!",e);
        }

    }//getConvertKana()の括り


    /**
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,SEX, ");
            stb.append("           NAME_KANA,SUBSTR(NAME_KANA,1,3) AS KANA, ");
            stb.append("           LOCATIONCD,FS_CD ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            stb.append("           AND SUBSTR(NAME_KANA,1,3) IN "+param[2]+" ");//氏名かな先頭１文字
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO003
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       T2.LOCATIONCD,N2.NAME1 AS LOCATION_NAME, ");
            if (param[3].equals("1")) {
                stb.append("       CASE WHEN T2.LOCATIONCD >= '01' AND T2.LOCATIONCD <= '38' THEN '*' ");
                stb.append("            ELSE NULL END AS FINSCHOOL_CHECK, ");//NO002
            } else {
                stb.append("       CASE WHEN T2.LOCATIONCD >= '01' AND T2.LOCATIONCD <= '35' THEN '*' ");
                stb.append("            ELSE NULL END AS FINSCHOOL_CHECK, ");//NO002
            }
            stb.append("       T2.FS_CD,VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME, ");
            stb.append("       T2.EXAMNO,T2.NAME_KANA,T2.KANA,T2.NAME,VALUE(T2.SEX,'0') AS SEX ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L007' AND N2.NAMECD2=T2.LOCATIONCD ");
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");
            stb.append("ORDER BY T2.NAME_KANA ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  五十音毎の総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,SEX, ");
            stb.append("           NAME_KANA,SUBSTR(NAME_KANA,1,3) AS KANA, ");
            stb.append("           LOCATIONCD,FS_CD ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            //試験区分
            if (!param[1].equals("99")) {
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            }
            stb.append("           AND SUBSTR(NAME_KANA,1,3) IN "+param[2]+" ");//氏名かな先頭１文字
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO003
            stb.append("    ) ");

            //メイン
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   EXAM_BASE ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
