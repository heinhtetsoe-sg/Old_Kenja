// kanji=漢字
/*
 * $Id: 09d18250bb51554166cff1da3c9ade7d38e8b070 $
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
 *                  ＜ＫＮＪＬ３１０Ｋ＞  入学試験志願者名簿
 *
 *  2005/07/28 nakamoto 作成日
 *  2005/08/28 nakamoto 高校用を修正
 *
 *  2005/10/27 nakamoto ・'H','S' --> '専','併'に変更
 *                      ・性別'*' --> '女'に変更
 *                      ・所在地欄は、所在地コードを表記する。
 *  2005/12/29 nakamoto NO001 所在地欄は、名称を表記する。
 *  2005/12/30 nakamoto NO002 (高校)コースコード記号の変更による修正 Q→T R→P
 *  2006/01/14 nakamoto NO003 氏名またはかな氏名がnullの場合に帳票エラーになる不具合を対応
 *  2006/01/14 nakamoto NO004 クラブ推薦を追加
 *                          　○クラブ推薦は、志願者基礎データの出願区分が'3'且つクラブコードがnot nullの者
 *  2006/01/24 nakamoto NO005 総ページ数の不具合を修正
 *                      NO006 ○クラブ推薦は、志願者基礎データの出願区分が'3'の者
 *                      NO007 ○クラブ推薦に、全体を追加
 *  2006/01/25 nakamoto NO008 ○志願者基礎データの出願区分が'2'の者を除く
 *  2007/02/06 nakamoto NO009 ○クラブ推薦の全体にて、クラブ名行が出力されない不具合を修正
 * @author nakamoto
 * @version $Id: 09d18250bb51554166cff1da3c9ade7d38e8b070 $
 */
public class KNJL310K {
    private static final Log log = LogFactory.getLog(KNJL310K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[13];//NO004-NO007

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 99:全て
            param[4] = request.getParameter("EXAMNOF");                     //受験番号From
            param[5] = request.getParameter("EXAMNOT");                     //受験番号To
            param[9] = request.getParameter("OUTPUT");                      //1:受験番号,2:クラブ推薦---NO004
            param[11] = request.getParameter("OUTPUT2");                    //1:クラブ別,2:全体---NO007
            param[12] = request.getParameter("SPECIAL_REASON_DIV");         //特別理由
            param[3] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
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
        //NO007
        if (param[11] != null && param[11].equals("2")) {
            if( printMain2(db2,svf,param) ) nonedata = true;
        } else {
            if( printMain(db2,svf,param) ) nonedata = true;
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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2= '" + param[12] + "' ");
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

    //  フォーム
        if (param[3].equals("1")) svf.VrSetForm("KNJL310_1.frm", 1);//中学用
        if (param[3].equals("2")) svf.VrSetForm("KNJL310_2.frm", 1);//高校用
        if (param[11] != null && param[11].equals("2")) svf.VrSetForm("KNJL310_3.frm", 4);//高校用-NO007

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校TITLE
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

    //  タイトル---NO004
        if (param[3].equals("2") && param[9] != null && param[9].equals("2")) param[10] = "クラブ推薦者名簿";
        else param[10] = "志願者名簿";


    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            int total_page[] = new int[getTotalPageCount(db2,param)];//NO005
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**総ページ数NO005*/
    private int getTotalPageCount(DB2UDB db2,String param[])
    {
        int cnt = 0;
        try {
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                cnt++;
            }
            rs.close();
            db2.commit();
log.debug("TotalPageCount = "+cnt);
        } catch( Exception ex ) {
            log.warn("getTotalPageCount read error!",ex);
        }
        return (0 < cnt) ? cnt : 1;

    }//getTotalPageCount()の括り


    /**試験区分毎の総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
    {
        try {
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();

            int cnt = 0;
            while( rs.next() ){
                total_page[cnt] = rs.getInt("COUNT");
                cnt++;
            }
            rs.close();
            db2.commit();
log.debug("TotalPage = "+cnt);
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

    }//getTotalPage()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
    {
        boolean nonedata = false;
        try {
log.debug("Meisai start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            int page_arr = 0;   //総ページ数配列No
            String testdiv = "d";
            while( rs.next() ){
                //１ページ印刷
                if (49 < gyo || (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ) {
                    //合計印刷
                    if ( !testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV")) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ( !testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV")) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                        //クラブ推薦(高校のみ)---NO004
                        if (param[3].equals("2") && param[9] != null && param[9].equals("2")) page_arr--;
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,total_page,page_arr);
                //明細データをセット
                printExam(svf,param,rs,gyo);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = rs.getString("TESTDIV");
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
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int page_cnt,int total_page[],int page_arr)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("DATE"         , param[8] );
            //クラブ推薦(高校のみ)---NO004
            if (param[3].equals("2") && param[9] != null && param[9].equals("2")) 
                svf.VrsOut("CLUBNAME"      , "クラブ名：" + rs.getString("CLUBNAME") );
            svf.VrsOut("TITLE"        , param[10] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , String.valueOf(total_page[page_arr]) );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printExam(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len1 = "0";
        String len2 = "0";
        String len3 = "0";
        String len4 = "0";
        try {
            //2005.08.28
            if (param[3].equals("1")) {
                len1 = ((rs.getString("MARK1")).equals("I")) ? "1" : 
                       ((rs.getString("MARK1")).equals("T")) ? "2" : 
                       ((rs.getString("MARK1")).equals("H")) ? "3" : "0" ;
            }
            if (param[3].equals("2")) {
                len1 = ((rs.getString("MARK1")).equals("S")) ? "1" : 
                       ((rs.getString("MARK1")).equals("K")) ? "2" : 
                       ((rs.getString("MARK1")).equals("T")) ? "3" : "4" ;//NO002
            }

            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            len3 = (10 < (rs.getString("NAME_KANA")).length()) ? "2" : "1" ;
            len4 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"          ,gyo+1    , rs.getString("EXAMNO") );
            svf.VrsOutn("SEX"             ,gyo+1    , rs.getString("SEX_NAME") );
            svf.VrsOutn("LOCATION"        ,gyo+1    , (param[3].equals("2")) ? rs.getString("LOCATION_NAME") : rs.getString("LOCATIONCD") );//NO001
            svf.VrsOutn("SENHEI"          ,gyo+1    , rs.getString("SHDIV_NAME") );//2005.08.28

            svf.VrsOutn("COURSE"+len1     ,gyo+1    , rs.getString("WISHNO1") );
            svf.VrsOutn("MAJOR2"          ,gyo+1    , rs.getString("WISHNO2") );
            svf.VrsOutn("MAJOR3"          ,gyo+1    , rs.getString("WISHNO3") );

            svf.VrsOutn("NAME"+len2       ,gyo+1    , rs.getString("NAME") );
            svf.VrsOutn("KANA"+len3       ,gyo+1    , rs.getString("NAME_KANA") );
            svf.VrsOutn("FINSCHOOL"+len4  ,gyo+1    , rs.getString("FINSCHOOL_NAME") );
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


    /**印刷処理メイン（クラブ推薦：全体-NO007）*/
    private boolean printMain2(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //明細データ
            if( printMeisai2(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain2 read error!",ex);
        }

        return nonedata;

    }//printMain2()の括り


    /**明細データ印刷処理（クラブ推薦：全体-NO007）*/
    private boolean printMeisai2(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
log.debug("Meisai2 start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai2 end!");

            int gyo = 1;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            String testdiv = "d";
            while( rs.next() ){
                //２レコード以降：明細行
                if (!testdiv.equals("d")) {
                    svf.VrEndRecord();
                    gyo++;
                }
                //クラブのブレイク時
                if (!testdiv.equals(rs.getString("TESTDIV"))) {
                    //空行---1行目以外なら出力
                    if (!testdiv.equals("d") && (gyo % 50 != 1)) {
                        clearExam2(svf);
                        svf.VrEndRecord();
                        gyo++;
                    }
                    //クラブ名行---50行目以外なら出力
                    if (gyo % 50 > 0) {
                        printHeader2(db2, svf,param,rs);
                        svf.VrEndRecord();
                        gyo++;
                    } else { //NO009
                        clearExam2(svf);
                        svf.VrEndRecord();
                        gyo++;
                        printHeader2(db2, svf,param,rs);
                        svf.VrEndRecord();
                        gyo++;
                    }
                }
                //明細データをセット
                printExam2(svf,param,rs);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = rs.getString("TESTDIV");

                nonedata = true;
            }
            //最終レコード印刷
            if (nonedata) {
                printTotal2(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                svf.VrEndRecord();
            }
            rs.close();
            db2.commit();
log.debug("Meisai2 gyo = "+gyo);
        } catch( Exception ex ) {
            log.warn("printMeisai2 read error!",ex);
        }
        return nonedata;

    }//printMeisai2()の括り


    /**ヘッダーデータをセット（クラブ推薦：全体-NO007）*/
    private void printHeader2(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            svf.VrsOut("DATE"         , param[8] );
            svf.VrsOut("TITLE"        , param[10] );

            svf.VrsOut("CLUBNAME"     , rs.getString("CLUBNAME") );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader2 read error!",ex);
        }

    }//printHeader2()の括り


    /**明細データをセット（クラブ推薦：全体-NO007）*/
    private void printExam2(Vrw32alp svf,String param[],ResultSet rs)
    {
        String len1 = "0";
        String len2 = "0";
        String len3 = "0";
        String len4 = "0";
        try {
            if (param[3].equals("2")) {
                len1 = ((rs.getString("MARK1")).equals("S")) ? "1" : 
                       ((rs.getString("MARK1")).equals("K")) ? "2" : 
                       ((rs.getString("MARK1")).equals("T")) ? "3" : "4" ;
            }
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            len3 = (10 < (rs.getString("NAME_KANA")).length()) ? "2" : "1" ;
            len4 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;

            svf.VrsOut("EXAMNO"          , rs.getString("EXAMNO") );
            svf.VrsOut("SEX"             , rs.getString("SEX_NAME") );
            svf.VrsOut("LOCATION"        , rs.getString("LOCATION_NAME") );
            svf.VrsOut("SENHEI"          , rs.getString("SHDIV_NAME") );
            svf.VrsOut("COURSE"+len1     , rs.getString("WISHNO1") );
            svf.VrsOut("MAJOR2"          , rs.getString("WISHNO2") );
            svf.VrsOut("MAJOR3"          , rs.getString("WISHNO3") );
            svf.VrsOut("NAME"+len2       , rs.getString("NAME") );
            svf.VrsOut("KANA"+len3       , rs.getString("NAME_KANA") );
            svf.VrsOut("FINSCHOOL"+len4  , rs.getString("FINSCHOOL_NAME") );
        } catch( Exception ex ) {
            log.warn("printExam2 read error!",ex);
        }

    }//printExam2()の括り


    /**合計をセット（クラブ推薦：全体-NO007）*/
    private void printTotal2(Vrw32alp svf,String param[],int sex1_cnt,int sex2_cnt,int sex_cnt)
    {
        try {
            svf.VrsOut("TOTAL_MEMBER" , "男" + String.valueOf(sex1_cnt) + "名、" + 
                                              "女" + String.valueOf(sex2_cnt) + "名、" + 
                                              "合計" + String.valueOf(sex_cnt) + "名" );
        } catch( Exception ex ) {
            log.warn("printTotal2 read error!",ex);
        }

    }//printTotal2()の括り


    /**明細データにブランクをセット（クラブ推薦：全体-NO007）*/
    private void clearExam2(Vrw32alp svf)
    {
        try {
            svf.VrsOut("MASK"            , "1" );
            svf.VrsOut("EXAMNO"          , "　" );
            svf.VrsOut("SEX"             , "　" );
            svf.VrsOut("LOCATION"        , "　" );
            svf.VrsOut("SENHEI"          , "　" );
            svf.VrsOut("COURSE1"         , "　" );
            svf.VrsOut("COURSE2"         , "　" );
            svf.VrsOut("COURSE3"         , "　" );
            svf.VrsOut("COURSE4"         , "　" );
            svf.VrsOut("MAJOR2"          , "　" );
            svf.VrsOut("MAJOR3"          , "　" );
            svf.VrsOut("NAME1"           , "　" );
            svf.VrsOut("KANA1"           , "　" );
            svf.VrsOut("FINSCHOOL1"      , "　" );
            svf.VrsOut("NAME2"           , "　" );
            svf.VrsOut("KANA2"           , "　" );
            svf.VrsOut("FINSCHOOL2"      , "　" );
        } catch( Exception ex ) {
            log.warn("clearExam2 read error!",ex);
        }

    }//clearExam2()の括り


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
            stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV,SHDIV,CLUBCD, ");//NO004
            stb.append("           NAME_KANA,NAME,SEX, ");
            stb.append("           FS_CD,LOCATIONCD,NATPUBPRIDIV ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[12])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[12] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
//NO004↓
            //クラブ推薦(高校のみ)
            if (param[3].equals("2") && param[9] != null && param[9].equals("2")) {
                    stb.append("       AND APPLICANTDIV = '3' ");
            //受験番号
            } else {
                if (param[4] != null && !param[4].equals("")) //受験番号From
                    stb.append("       AND '"+param[4]+"' <= EXAMNO ");
                if (param[5] != null && !param[5].equals("")) //受験番号To
                    stb.append("       AND EXAMNO <= '"+param[5]+"' ");
                    stb.append("       AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO008
            }
//NO004↑
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.TESTDIV,W1.DESIREDIV,W1.WISHNO,W2.EXAMCOURSE_ABBV,W2.EXAMCOURSE_MARK ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT N3.NAME1 AS TEST_NAME, ");
//NO004↓
            //クラブ推薦(高校のみ)
            if (param[3].equals("2") && param[9] != null && param[9].equals("2")) {
                stb.append("       T2.TESTDIV || VALUE(T2.CLUBCD,'') AS TESTDIV, ");//NO006
                stb.append("       VALUE(C1.CLUBNAME,'無し') AS CLUBNAME, ");
            //受験番号
            } else {
                stb.append("       T2.TESTDIV, ");
            }
//NO004↑
            stb.append("       T2.EXAMNO,VALUE(T2.NAME_KANA,'') AS NAME_KANA,VALUE(T2.NAME,'') AS NAME, ");//NO003
            stb.append("       VALUE(T2.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN VALUE(T2.SEX,'0') = '2' THEN N1.ABBV1 ELSE '' END AS SEX_NAME, ");//2005.10.27
//          stb.append("       CASE WHEN VALUE(T2.SEX,'0') = '2' THEN '＊' ELSE '' END AS SEX_NAME, ");
            stb.append("       SHDIV, N5.NAME1 AS SHDIV_NAME, ");//2005.10.27
//          stb.append("       SHDIV, N5.ABBV1 AS SHDIV_NAME, ");//2005.08.28
            stb.append("       T2.DESIREDIV, ");
            stb.append("       VALUE(W1.EXAMCOURSE_MARK,'') AS MARK1, ");
            stb.append("       VALUE(W1.EXAMCOURSE_ABBV,'') AS WISHNO1, ");
            stb.append("       W2.EXAMCOURSE_ABBV AS WISHNO2, ");
            stb.append("       W3.EXAMCOURSE_ABBV AS WISHNO3, ");
            stb.append("       T2.FS_CD,VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME, ");
            stb.append("       T2.NATPUBPRIDIV,T2.LOCATIONCD, ");
            stb.append("       N4.NAME1 AS LOCATION_NAME ");//NO001
//          stb.append("       CASE WHEN T2.NATPUBPRIDIV = '2' THEN N4.NAME1 ELSE N2.NAME1 END AS LOCATION_NAME ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T2.SEX ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T2.NATPUBPRIDIV ");
            stb.append("       LEFT JOIN NAME_MST N4 ON N4.NAMECD1='L007' AND N4.NAMECD2=T2.LOCATIONCD ");
            stb.append("       LEFT JOIN NAME_MST N5 ON N5.NAMECD1='L006' AND N5.NAMECD2=T2.SHDIV ");
            stb.append("       LEFT JOIN EXAM_WISH W1 ON W1.TESTDIV=T2.TESTDIV AND W1.DESIREDIV=T2.DESIREDIV AND W1.WISHNO='1' ");
            stb.append("       LEFT JOIN EXAM_WISH W2 ON W2.TESTDIV=T2.TESTDIV AND W2.DESIREDIV=T2.DESIREDIV AND W2.WISHNO='2' ");
            stb.append("       LEFT JOIN EXAM_WISH W3 ON W3.TESTDIV=T2.TESTDIV AND W3.DESIREDIV=T2.DESIREDIV AND W3.WISHNO='3' ");
            stb.append("       LEFT JOIN CLUB_MST C1 ON C1.CLUBCD=T2.CLUBCD ");//NO004
//NO004↓
        //クラブ推薦(高校のみ)
        if (param[3].equals("2") && param[9] != null && param[9].equals("2")) {
            stb.append("ORDER BY T2.TESTDIV,T2.CLUBCD,T2.EXAMNO ");
        //受験番号
        } else {
            stb.append("ORDER BY T2.TESTDIV,T2.EXAMNO ");
        }
//NO004↑
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
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV,CLUBCD, ");//NO004
            stb.append("           NAME_KANA,NAME,SEX, ");
            stb.append("           FS_CD,LOCATIONCD,NATPUBPRIDIV ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[12])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[12] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
//NO004↓
            //クラブ推薦(高校のみ)
            if (param[3].equals("2") && param[9] != null && param[9].equals("2")) {
                    stb.append("       AND APPLICANTDIV = '3' ");
            //受験番号
            } else {
                if (param[4] != null && !param[4].equals("")) //受験番号From
                    stb.append("       AND '"+param[4]+"' <= EXAMNO ");
                if (param[5] != null && !param[5].equals("")) //受験番号To
                    stb.append("       AND EXAMNO <= '"+param[5]+"' ");
                    stb.append("       AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO008
            }
//NO004↑
            stb.append("    ) ");

            //メイン
//NO004↓
            //クラブ推薦(高校のみ)
            if (param[3].equals("2") && param[9] != null && param[9].equals("2")) {
                stb.append("    SELECT TESTDIV,CLUBCD, ");
                stb.append("           CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
                stb.append("    FROM   EXAM_BASE ");
                stb.append("    GROUP BY TESTDIV,CLUBCD ");
                stb.append("    ORDER BY TESTDIV,CLUBCD ");//NO005
            //受験番号
            } else {
                stb.append("SELECT TESTDIV, ");
                stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
                stb.append("FROM   EXAM_BASE ");
                stb.append("GROUP BY TESTDIV ");
                stb.append("ORDER BY TESTDIV ");
            }
//NO004↑
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
