// kanji=漢字
/*
 * $Id: cc5d4c75bcad78c94c94d68bfc5896b7298f31b2 $
 *
 * 作成日: 2005/11/10 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３４７＞  合否判定基準表（近大）
 *
 *  2005/11/10 nakamoto 作成日
 *  2005.11.14 nakamoto NO001 事前判定は、上段に専願、下段に併願を表記し、表示順は、最終判定と同じように表記するよう変更
 *                      NO002 １ページ２５行に変更
 *                      NO003 最終判定は、上段に専願、下段に併願を表記するように変更
 *  2005/12/30 nakamoto NO004 (高校)コースコード記号の変更による修正 Q→T R→P
 *  2006/01/19 nakamoto NO005 性別コードがnullの場合、NullPointerExceptionエラーに対応
 *  2006/01/25 nakamoto NO006 ○志願者基礎データの出願区分が'2'の者を除く
 * @author nakamoto
 * @version $Id: cc5d4c75bcad78c94c94d68bfc5896b7298f31b2 $
 */
public class KNJL347K {


    private static final Log log = LogFactory.getLog(KNJL347K.class);

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

            param[3] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[9] = request.getParameter("SPECIAL_REASON_DIV");         // 特別理由
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
        svf.VrSetForm("KNJL347.frm", 1);

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
            getTotalPage(db2,svf,param);

            //明細データ
            if( printMeisai(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
log.debug("TotalPage start!");
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();
log.debug("TotalPage end!");

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
log.debug("Meisai start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int gyo = 0;
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            int page_cnt = 1;   //ページ数
            while( rs.next() ){
                //１ページ印刷
                if (24 < gyo) {
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt);
                //明細データをセット
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
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs,int page_cnt)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            svf.VrsOut("DATE"         , param[8] );
            //中学のみ表記
            if (param[3].equals("1")) 
                svf.VrsOut("TESTDIV"  , rs.getString("TEST_NAME") );
            //高校のみ表記
            if (param[3].equals("2")) 
                svf.VrsOut("ITEM"     , "専併" );

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
        String len2 = "0";
        String len3 = "0";
        String len4 = "0";
        try {
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            len3 = (10 < (rs.getString("NAME_KANA")).length()) ? "2" : "1" ;
            len4 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"          ,gyo+1    , rs.getString("EXAMNO") );
            svf.VrsOutn("NAME"+len2       ,gyo+1    , rs.getString("NAME") );
            svf.VrsOutn("KANA"+len3       ,gyo+1    , rs.getString("NAME_KANA") );
            svf.VrsOutn("SEX"             ,gyo+1    , rs.getString("SEX_NAME") );

            //高校のみ表記
            if (param[3].equals("2")) 
                svf.VrsOutn("SHDIV"       ,gyo+1    , rs.getString("SHDIV_NAME") );

            svf.VrsOutn("BIRTHDAY"        ,gyo+1    , setBirthday(rs.getString("BIRTHDAY")) );//2005.10.27

            svf.VrsOutn("ADDRESSCD"       ,gyo+1    , rs.getString("ADDRESSCD") );
            svf.VrsOutn("NATPUBPRIDIV"    ,gyo+1    , rs.getString("NATPUBPRIDIV") );
            svf.VrsOutn("LOCATIONCD"      ,gyo+1    , rs.getString("LOCATIONCD") );
            svf.VrsOutn("FINSCHOOLCD"     ,gyo+1    , rs.getString("FS_CD") );
            svf.VrsOutn("FINSCHOOL"+len4  ,gyo+1    , rs.getString("FINSCHOOL_NAME") );
            svf.VrsOutn("DESIREDIV"       ,gyo+1    , rs.getString("DESIREDIV") );

            svf.VrsOutn("JUDGEMENT"       ,gyo+1    , rs.getString("JUDGE_NAME") );   // 合否
            //NO003
            svf.VrsOutn("FINAL_JUDGE1"    ,gyo+1    , rs.getString("JUDGE1") );       // 最終判定(専願)
            svf.VrsOutn("FINAL_JUDGE2"    ,gyo+1    , rs.getString("JUDGE2") );       // 最終判定(併願)
            //NO001
            svf.VrsOutn("ORG_JUDGE1_1"    ,gyo+1    , rs.getString("FS_JUDGE1") );    // 事前判定：(専願)出身学校
            svf.VrsOutn("ORG_JUDGE2_1"    ,gyo+1    , rs.getString("PS_JUDGE1") );    // 事前判定：(専願)塾
            svf.VrsOutn("ORG_JUDGE1_2"    ,gyo+1    , rs.getString("FS_JUDGE2") );    // 事前判定：(併願)出身学校
            svf.VrsOutn("ORG_JUDGE2_2"    ,gyo+1    , rs.getString("PS_JUDGE2") );    // 事前判定：(併願)塾

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
     *  生年月日をセット
     *
     * ※「H02.01.08」表記
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
                if (bunkatu_date[0].equals("平成元年")) bunkatu_date[1] = "1";

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
     *　最終判定：１．表示順は、左から{I・T・H・NULL}(中学)、{S・K・T・P}(高校)と表記 NO004
     *　        　２．ENTEXAM_APPLICANTCONS_DATの判定(JUDGEMENT)がNULLでないデータを表記
     *　事前判定：１．表示順は、左からENTEXAM_CONSULTATION_DATの志望連番(WISHNO)の若い順に表記
     *　        　２．ENTEXAM_CONSULTATION_DATの判定(JUDGEMENT)がNULLでないデータ、かつ、
     *　        　　　ENTEXAM_CONSULTATION_HDATの受験番号(EXAMNO)がNULLでないデータを表記
     *
     * 2005.11.14 NO001 事前判定は、上段に専願、下段に併願を表記し、表示順は、最終判定と同じように表記するよう変更
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO, NAME, NAME_KANA, SEX, SHDIV, BIRTHDAY, TESTDIV, ");
            stb.append("           JUDGEMENT, SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE, ");
            stb.append("           ADDRESSCD, NATPUBPRIDIV, LOCATIONCD, FS_CD, DESIREDIV ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("           AND TESTDIV = '"+param[1]+"' ");
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO006
            stb.append("    ) ");

            //志願者事前相談データ
            stb.append(",EXAM_CONS AS ( ");
            stb.append("    SELECT EXAMNO, SHDIV, JUDGEMENT, ");
            stb.append("           COURSECD||MAJORCD||EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTCONS_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           TESTDIV = '"+param[1]+"' AND ");
            stb.append("           JUDGEMENT IS NOT NULL ");
            stb.append("    ) ");

            //事前相談ヘッダデータ・事前相談データ
            stb.append(",EXAM_CONSUL AS ( ");
            stb.append("    SELECT W1.EXAMNO, SHDIV, JUDGEMENT, DATADIV, WISHNO, ");
            stb.append("           COURSECD||MAJORCD||EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_CONSULTATION_HDAT W1, ");
            stb.append("           ENTEXAM_CONSULTATION_DAT W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("           W2.TESTDIV = W1.TESTDIV AND ");
            stb.append("           W2.ACCEPTNO = W1.ACCEPTNO AND ");
            stb.append("           JUDGEMENT IS NOT NULL AND ");
            stb.append("           W1.EXAMNO IS NOT NULL ");
            stb.append("    ) ");

            //受験コースマスタ
            stb.append(",EXAM_COURSE AS ( ");
            stb.append("    SELECT EXAMCOURSE_MARK AS MARK, EXAMCOURSE_ABBV AS SUC_ABBV, ");
            stb.append("           COURSECD||MAJORCD||EXAMCOURSECD AS COURSE ");
            stb.append("    FROM   ENTEXAM_COURSE_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    ) ");

            //名称マスタ：事前判定
            stb.append(",NAME_L002 AS ( ");
            stb.append("    SELECT NAMECD2 AS JUDGEMENT, NAME1 AS JUDGE_NAME ");
            stb.append("    FROM   NAME_MST ");
            stb.append("    WHERE  NAMECD1 = 'L002' ");
            stb.append("    ) ");

            //志願者事前相談データ：最終判定
            stb.append(",RESULT_CONS AS ( ");
            stb.append("    SELECT W1.EXAMNO, ");
            stb.append("           W1.SHDIV, ");
            if (param[3].equals("1")) {//中学
                stb.append("       MAX(CASE WHEN W2.MARK='I' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE1, ");
                stb.append("       MAX(CASE WHEN W2.MARK='T' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE2, ");
                stb.append("       MAX(CASE WHEN W2.MARK='H' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE3, ");
            }
            if (param[3].equals("2")) {//高校
                stb.append("       MAX(CASE WHEN W2.MARK='S' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE1, ");
                stb.append("       MAX(CASE WHEN W2.MARK='K' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE2, ");
                stb.append("       MAX(CASE WHEN W2.MARK='T' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE3, ");//NO004
            }
            stb.append("           MAX(CASE WHEN W2.MARK='P' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE4 ");//高校用。中学はNULL //NO004
            stb.append("    FROM   EXAM_CONS W1 ");
            stb.append("           LEFT JOIN EXAM_COURSE W2 ON W2.COURSE=W1.COURSE ");
            stb.append("           LEFT JOIN NAME_L002 W3 ON W3.JUDGEMENT=W1.JUDGEMENT ");
            stb.append("    GROUP BY W1.EXAMNO,W1.SHDIV ");
            stb.append("    ) ");

            //事前相談ヘッダデータ・事前相談データ：事前判定---NO001
            stb.append(",RESULT_CONSUL AS ( ");
            stb.append("    SELECT W1.EXAMNO, ");
            stb.append("           W1.SHDIV, ");
            if (param[3].equals("1")) {//中学
                stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='I' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE11, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='T' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE12, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='H' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE13, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='I' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE21, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='T' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE22, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='H' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE23, ");
            }
            if (param[3].equals("2")) {//高校
                stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='S' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE11, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='K' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE12, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='T' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE13, ");//NO004
                stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='S' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE21, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='K' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE22, ");
                stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='T' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE23, ");//NO004
            }
            stb.append("       MIN(CASE WHEN W1.DATADIV='1' AND W2.MARK='P' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE14, ");//NO004
            stb.append("       MIN(CASE WHEN W1.DATADIV='2' AND W2.MARK='P' THEN W2.MARK||W3.JUDGE_NAME END) AS JUDGE24 ");//NO004
            stb.append("    FROM   EXAM_CONSUL W1 ");
            stb.append("           LEFT JOIN EXAM_COURSE W2 ON W2.COURSE=W1.COURSE ");
            stb.append("           LEFT JOIN NAME_L002 W3 ON W3.JUDGEMENT=W1.JUDGEMENT ");
            stb.append("    GROUP BY W1.EXAMNO,W1.SHDIV ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.EXAMNO, ");
            stb.append("       VALUE(T1.NAME,'') AS NAME, ");
            stb.append("       VALUE(T1.NAME_KANA,'') AS NAME_KANA, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX, N1.ABBV1 AS SEX_NAME, ");//NO005
            stb.append("       T1.TESTDIV, N3.NAME1 AS TEST_NAME, ");
            stb.append("       T1.SHDIV, N2.NAME1 AS SHDIV_NAME, ");
            stb.append("       T1.BIRTHDAY, ");
            stb.append("       T1.ADDRESSCD, ");
            stb.append("       T1.NATPUBPRIDIV, ");
            stb.append("       T1.LOCATIONCD, ");
            stb.append("       T1.FS_CD, VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME, ");
            stb.append("       T1.DESIREDIV, ");
                            // 合否
            stb.append("       CASE WHEN ((T1.JUDGEMENT > '0' AND T1.JUDGEMENT <= '6') OR T1.JUDGEMENT = '9') THEN E1.SUC_ABBV ");   //合格
            stb.append("            WHEN T1.JUDGEMENT  = '7' THEN '否' ");          //不合格
            stb.append("            WHEN T1.JUDGEMENT  = '8' THEN '欠' ");          //未受験
            stb.append("            ELSE '' END AS JUDGE_NAME, ");                  //その他
                            // 最終判定(専願)
            stb.append("       VALUE(T2.JUDGE1,'  ')||'・'||VALUE(T2.JUDGE2,'  ')||'・'||VALUE(T2.JUDGE3,'  ')||'・'||VALUE(T2.JUDGE4,'  ') AS JUDGE1, ");
                            // 最終判定(併願)---NO003
            stb.append("       VALUE(T5.JUDGE1,'  ')||'・'||VALUE(T5.JUDGE2,'  ')||'・'||VALUE(T5.JUDGE3,'  ')||'・'||VALUE(T5.JUDGE4,'  ') AS JUDGE2, ");
                            // 事前判定：(専願)出身学校
            stb.append("       VALUE(T3.JUDGE11,'  ')||'・'||VALUE(T3.JUDGE12,'  ')||'・'||VALUE(T3.JUDGE13,'  ')||'・'||VALUE(T3.JUDGE14,'  ') AS FS_JUDGE1, ");
                            // 事前判定：(専願)塾
            stb.append("       VALUE(T3.JUDGE21,'  ')||'・'||VALUE(T3.JUDGE22,'  ')||'・'||VALUE(T3.JUDGE23,'  ')||'・'||VALUE(T3.JUDGE24,'  ') AS PS_JUDGE1, ");
                            // 事前判定：(併願)出身学校---NO001
            stb.append("       VALUE(T4.JUDGE11,'  ')||'・'||VALUE(T4.JUDGE12,'  ')||'・'||VALUE(T4.JUDGE13,'  ')||'・'||VALUE(T4.JUDGE14,'  ') AS FS_JUDGE2, ");
                            // 事前判定：(併願)塾---NO001
            stb.append("       VALUE(T4.JUDGE21,'  ')||'・'||VALUE(T4.JUDGE22,'  ')||'・'||VALUE(T4.JUDGE23,'  ')||'・'||VALUE(T4.JUDGE24,'  ') AS PS_JUDGE2 ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T1.SEX ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L006' AND N2.NAMECD2=T1.SHDIV ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T1.TESTDIV ");
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T1.FS_CD ");
            stb.append("       LEFT JOIN RESULT_CONS T2 ON T2.EXAMNO=T1.EXAMNO AND T2.SHDIV='1' ");//NO003
            stb.append("       LEFT JOIN RESULT_CONS T5 ON T5.EXAMNO=T1.EXAMNO AND T5.SHDIV='2' ");//NO003
            stb.append("       LEFT JOIN RESULT_CONSUL T3 ON T3.EXAMNO=T1.EXAMNO AND T3.SHDIV='1' ");//NO001
            stb.append("       LEFT JOIN RESULT_CONSUL T4 ON T4.EXAMNO=T1.EXAMNO AND T4.SHDIV='2' ");//NO001
            stb.append("       LEFT JOIN EXAM_COURSE E1 ON E1.COURSE=T1.SUC_COURSE ");
            stb.append("ORDER BY T1.EXAMNO ");
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  総ページ数を取得
     *
     * 2005.11.14 NO002 １ページ２５行に変更
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[9])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[9] + "' ");
            }
            stb.append("           AND TESTDIV = '"+param[1]+"' ");
            stb.append("           AND VALUE(APPLICANTDIV,'0') NOT IN('2') ");//NO006
            stb.append("    ) ");

            //メイン
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(*),25) THEN COUNT(*)/25 + 1 ELSE COUNT(*)/25 END AS COUNT ");
            stb.append("FROM   EXAM_BASE ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
