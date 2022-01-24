// kanji=漢字
/*
 * $Id: 20d5dca4b0d889a6aa01675fddd0e38a1a5746ea $
 *
 * 作成日: 2005/07/27 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３１２Ｋ＞  入学試験志願者データ一覧（近大）
 *
 *  2005/07/27 nakamoto 作成日
 *  2005/08/28 nakamoto 高校用を修正
 *  2005/09/13 nakamoto 郵便番号マスタの複数データに対応
 *
 *  2005/10/27 nakamoto ・１ページ２５名表記にする
 *                      ・'H','S' --> '専'のみ表記に変更
 *                      ・性別'*' --> '女'に変更
 *                      ・生年月日 --> 'H02.01.08'表記に変更
 *                      ・住所欄（都道府県に、「L007」「名称２」を表記。市区町村に所在地コードを表記）
 *  2005/11/08 nakamoto NO001 住所の都道府県は、出身学校コードの３・４桁目を見て「L007」「名称２」を表記。
 *  2005/11/10 nakamoto NO002 生年月日の不具合を修正。平成元年の場合、'H00'となっていたので、'H01'と修正。
 *  2005/11/14 nakamoto NO003 住所の都道府県は、所在地コードを見て判断し固定値を表記。--->後日、変更の可能性有り。
 *  2005/11/28 nakamoto NO004 １ページ（中学：５０名、高校：２５名）表記にするよう修正
 *  2005/12/29 nakamoto NO005 住所の市区町村は、所在地コードを見て「L007」「名称１」を表記。
 *  2006/01/25 nakamoto NO006 ○志願者基礎データの出願区分が'2'の者を除く
 * @author nakamoto
 * @version $Id: 20d5dca4b0d889a6aa01675fddd0e38a1a5746ea $
 */
public class KNJL312K {


    private static final Log log = LogFactory.getLog(KNJL312K.class);

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
            param[1] = request.getParameter("TESTDIV");                     //試験区分 99:全て
            param[4] = request.getParameter("EXAMNOF");                     //受験番号From
            param[5] = request.getParameter("EXAMNOT");                     //受験番号To

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

        if( printMain(db2,svf,param) ) nonedata = true;

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

    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

    //  フォーム
        if (param[3].equals("1")) svf.VrSetForm("KNJL312_1.frm", 1);//中学用
        if (param[3].equals("2")) svf.VrSetForm("KNJL312_2.frm", 1);//高校用

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


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //総ページ数
            int total_page[] = new int[3];
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


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
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

    }//getTotalPage()の括り


    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[],int total_page[])
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
            int page_arr = 0;   //総ページ数配列No
            String testdiv = "d";

            //１ページの表記人数-1 ---NO004
            int page_ninzuu = 0;
            if (param[3].equals("1")) page_ninzuu = 49;//中学用
            if (param[3].equals("2")) page_ninzuu = 24;//高校用
            while( rs.next() ){
                //１ページ印刷 ---NO004
                if (page_ninzuu < gyo || (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ) {
                    //合計印刷
                    if ( !testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV")) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ( !testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV")) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
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
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs,int page_cnt,int total_page[],int page_arr)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("DATE"         , param[8] );

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
        String len2 = "0";
        String len3 = "0";
        String len4 = "0";
        try {
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            len3 = (10 < (rs.getString("NAME_KANA")).length()) ? "2" : "1" ;
            len4 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"          ,gyo+1    , rs.getString("EXAMNO") );
            svf.VrsOutn("DESIREDIV"       ,gyo+1    , rs.getString("DESIREDIV") );
            svf.VrsOutn("SEX"             ,gyo+1    , rs.getString("SEX_NAME") );
            svf.VrsOutn("BIRTHDAY"        ,gyo+1    , setBirthday(rs.getString("BIRTHDAY")) );//2005.10.27
            svf.VrsOutn("ADDRESSCD"       ,gyo+1    , rs.getString("ADDRESSCD") );
            svf.VrsOutn("LOCATIONCD"      ,gyo+1    , rs.getString("LOCATIONCD") );
            svf.VrsOutn("NATPUBPRIDIV"    ,gyo+1    , rs.getString("NATPUB_NAME") );

            svf.VrsOutn("KANA"+len3       ,gyo+1    , rs.getString("NAME_KANA") );
            svf.VrsOutn("NAME"+len2       ,gyo+1    , rs.getString("NAME") );

            //2005.08.28
            if (param[3].equals("2")) {
                svf.VrsOutn("SHDIV"           ,gyo+1    , rs.getString("SHDIV_NAME") );
                svf.VrsOutn("APPLICANTDIV"    ,gyo+1    , rs.getString("DESIREDIV") );
                svf.VrsOutn("SCHOOLCD"        ,gyo+1    , rs.getString("FS_CD") );
                svf.VrsOutn("SCHOOLNAME"+len4 ,gyo+1    , rs.getString("FINSCHOOL_NAME") );
                svf.VrsOutn("PREFECTURE"      ,gyo+1    , rs.getString("LOC_NAME") );//2005.10.27
                svf.VrsOutn("LOCATION"        ,gyo+1    , rs.getString("LOC_NAME2") );//NO005
            }
        } catch( Exception ex ) {
            log.warn("printExam read error!",ex);
        }

    }//printExam()の括り


    /**
     *  市町村をセット
     *
     * ※最後の文字で判断---2005.08.28
     * 市：大阪市             ⇒ 大阪市
     * 区：大阪市中央区       ⇒ 大阪市
     * 町：泉南郡岬町         ⇒ 泉南郡
     * 村：南河内郡千早赤阪村 ⇒ 南河内郡
     * その他：XXX            ⇒ XXX
     */
    private String setCityName(ResultSet rs)
    {
        String ret_val = "";
        try {
            if (rs.getString("ZIP_CITY") != null) {
                String city_nam = rs.getString("ZIP_CITY");
                String city_flg = rs.getString("ZIP_CITY_FLG");

                if (city_flg.equals("市")) {
                    ret_val = city_nam;

                } else if (city_flg.equals("区")) {
                    ret_val = (-1 < city_nam.indexOf("市")) ? city_nam.substring(0,city_nam.indexOf("市"))+"市" : city_nam;

                } else if (city_flg.equals("町") || city_flg.equals("村")) {
                    ret_val = (-1 < city_nam.indexOf("郡")) ? city_nam.substring(0,city_nam.indexOf("郡"))+"郡" : city_nam;

                } else {
                    ret_val = city_nam;
                }
            }
        } catch( Exception ex ) {
            log.warn("setCityName read error!",ex);
        }
        return ret_val;

    }//setCityName()の括り


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
     *  生年月日をセット
     *
     * ※「H02.01.08」表記に変更---2005.10.27
     * NO002:平成元年の場合、'H00'となっていたので、'H01'と修正
     */
    private String setBirthday(String birthday)
    {
        String ret_val = "";
        try {
            if (birthday != null) {
                String wareki_date = KNJ_EditDate.h_format_JP(birthday);
                String bunkatu_date[] = KNJ_EditDate.tate_format(wareki_date);

                String gengou = "";
                if (bunkatu_date[0].equals("平成")) gengou = "H";
                if (bunkatu_date[0].equals("昭和")) gengou = "S";
                if (bunkatu_date[0].equals("平成元年")) gengou = "H";
                if (bunkatu_date[0].equals("平成元年")) bunkatu_date[1] = "1";//NO002

                for (int i = 1; i < 4; i++) {
                    if (Integer.parseInt(bunkatu_date[i]) < 10) bunkatu_date[i] = "0" + bunkatu_date[i];
                }

                ret_val = gengou + bunkatu_date[1] + "." + bunkatu_date[2] + "." + bunkatu_date[3];
            }
        } catch( Exception ex ) {
            log.warn("setBirthday read error!",ex);
        }
        return ret_val;

    }//setBirthday()の括り


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
            stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV,SHDIV,FS_CD,APPLICANTDIV, ");//2005.08.28
            stb.append("           CASE WHEN FS_CD IS NOT NULL THEN SUBSTR(FS_CD,3,2) ELSE FS_CD END AS FS_CD34, ");//NO001
            stb.append("           NAME_KANA,NAME,SEX,BIRTHDAY, ");
            stb.append("           ADDRESSCD,LOCATIONCD,NATPUBPRIDIV ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (param[4] != null && !param[4].equals("")) //受験番号From
                stb.append("       AND '"+param[4]+"' <= EXAMNO ");
            if (param[5] != null && !param[5].equals("")) //受験番号To
                stb.append("       AND EXAMNO <= '"+param[5]+"' ");
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO006
            stb.append("    ) ");
//2005.10.27Del
            //メイン
            stb.append("SELECT T2.TESTDIV,N3.NAME1 AS TEST_NAME,T2.EXAMNO, ");
            stb.append("       T2.APPLICANTDIV,T2.SHDIV, ");//2005.10.27
            stb.append("       CASE WHEN VALUE(T2.SHDIV,'0') = '1' THEN N5.NAME1 ELSE '' END AS SHDIV_NAME, ");//2005.10.27
            stb.append("       T2.DESIREDIV,T2.ADDRESSCD,T2.LOCATIONCD, ");
            stb.append("       CASE WHEN T2.LOCATIONCD >= '01' AND T2.LOCATIONCD <= '35' THEN '大阪府' ");
            stb.append("            WHEN T2.LOCATIONCD  = '36' THEN '和歌山県' ");
            stb.append("            WHEN T2.LOCATIONCD  = '37' THEN '兵庫県' ");
            stb.append("            WHEN T2.LOCATIONCD  = '38' THEN '京都府' ");
            stb.append("            WHEN T2.LOCATIONCD  = '39' THEN '滋賀県' ");
            stb.append("            WHEN T2.LOCATIONCD  = '40' THEN 'その他' ");
            stb.append("            WHEN T2.LOCATIONCD >= '41' AND T2.LOCATIONCD <= '49' THEN '奈良県' ");
            stb.append("            WHEN T2.LOCATIONCD  = '51' THEN '三重県' ");
            stb.append("            ELSE NULL END AS LOC_NAME, ");//NO003
            stb.append("       N4.NAME1 AS LOC_NAME2, ");//NO005
            stb.append("       T2.NAME_KANA,T2.NAME,T2.BIRTHDAY, ");
            stb.append("       CASE WHEN VALUE(T2.SEX,'0') = '2' THEN N1.ABBV1 ELSE '' END AS SEX_NAME, ");//2005.10.27
            stb.append("       VALUE(T2.SEX,'0') AS SEX, NATPUBPRIDIV, ");
            stb.append("       T2.FS_CD,VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME,F1.FINSCHOOL_ZIPCD, ");//2005.08.28
            stb.append("       N2.ABBV1 AS NATPUB_NAME ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T2.SEX ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T2.NATPUBPRIDIV ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N4 ON N4.NAMECD1='L007' AND N4.NAMECD2=T2.LOCATIONCD ");//NO005
            stb.append("       LEFT JOIN NAME_MST N5 ON N5.NAMECD1='L006' AND N5.NAMECD2=T2.SHDIV ");//2005.08.28
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");//2005.08.28
            stb.append("ORDER BY T2.TESTDIV,T2.EXAMNO ");
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
            stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV, ");
            stb.append("           NAME_KANA,NAME,SEX,BIRTHDAY, ");
            stb.append("           ADDRESSCD,LOCATIONCD,NATPUBPRIDIV ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (param[4] != null && !param[4].equals("")) //受験番号From
                stb.append("       AND '"+param[4]+"' <= EXAMNO ");
            if (param[5] != null && !param[5].equals("")) //受験番号To
                stb.append("       AND EXAMNO <= '"+param[5]+"' ");
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO006
            stb.append("    ) ");

            //メイン
            stb.append("SELECT TESTDIV, ");
                //中学用---NO004
            if (param[3].equals("1")) 
                stb.append("   CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
                //高校用---NO004
            if (param[3].equals("2")) 
                stb.append("   CASE WHEN 0 < MOD(COUNT(*),25) THEN COUNT(*)/25 + 1 ELSE COUNT(*)/25 END AS COUNT ");//2005.10.27
            stb.append("FROM   EXAM_BASE ");
            stb.append("GROUP BY TESTDIV ");
            stb.append("ORDER BY TESTDIV ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
