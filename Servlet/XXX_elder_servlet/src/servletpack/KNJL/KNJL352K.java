// kanji=漢字
/*
 * $Id: 07795c3ebfcc15249b6bba0e9e1448284d72347a $
 *
 * 作成日: 2005/08/31 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３５２＞  合否通知書発行台帳
 *
 *  2005/08/31 m-yama 作成日
 *  2005/09/13 m-yama NO001 出力種別追加
 *  2005/09/13 m-yama NO002 ソート順追加
 *  2005/11/07 m-yama NO003 専併区分の表示を'専'、'併'に変更
 *  2005/12/20 m-yama NO004 追加/繰上をひとつにまとめる
 *  2006/01/14 m-yama NO005 パラメータ追加(合格/不合格者出力)
 *  2006/01/17 m-yama NO006 不合格者出力時は、否と欠のソートもする。
 *  2006/01/19 m-yama NO007 サブタイトル修正。
 *  2006/02/07 o-naka NO008 印刷日付は、指示画面からのパラメータをセットするように変更
 *  2006/02/09 yamashiro NO009 出力順「合格コース＆受験番号順」」は専願を先に出力する。
 * @author m-yama
 * @version $Id: 07795c3ebfcc15249b6bba0e9e1448284d72347a $
 */

public class KNJL352K {


    private static final Log log = LogFactory.getLog(KNJL352K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[14];        //NO005 NO008

    //  パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");           //次年度
            param[1]  = request.getParameter("TESTDIV");        //試験区分 99:全て
            param[3]  = request.getParameter("JHFLG");          //中学/高校フラグ 1:中学,2:高校
            param[4]  = request.getParameter("OUTPUT2");        //1:一般,2:中高一貫
            param[5]  = request.getParameter("OUTPUT");         //出力対象 NO001
            param[9]  = request.getParameter("PASSDIV");        //回数 NO001
            param[10] = request.getParameter("OUTPUT3");        //ソート順 NO002
            param[11] = request.getParameter("OUTPUT4");        //1:合格,2:不合格 NO005
            param[12] = request.getParameter("DATE");           //印刷日付 NO008
            param[13] = request.getParameter("SPECIAL_REASON_DIV"); //特別理由
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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[13] + "' ");
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
        svf.VrSetForm("KNJL352.frm", 1);//高校用

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
            param[8] = KNJ_EditDate.h_format_JP(param[12]);//NO008
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
            int total_page[] = new int[5];
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
log.debug("TotalPage start!");
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();
log.debug("TotalPage end!");

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
log.debug("Meisai start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");

            int gyo = 0;
            int page_cnt = 1;                   //ページ数
            int page_arr = 0;                   //総ページ数配列No
            String testdiv = "d";               //入試区分
            String judgement = "d";             //NO005
            String judgement_group_no = "d";    //NO005
            while( rs.next() ){
                //１ページ印刷
                if (14 < gyo || (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ||
                    (param[10].equals("1") && !judgement.equals("d") && !judgement.equals(rs.getString("JUDGEMENT")) && judgement.equals("1")) ||   //NO005
                    (null != param[5] && param[5].equals("5") && !judgement_group_no.equals("d") && !judgement_group_no.equals(rs.getString("JUDGEMENT_GROUP_NO"))) //NO005
                    ) { //NO005
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) ||
                        (!judgement.equals("d") && !judgement.equals(rs.getString("JUDGEMENT")) && !rs.getString("JUDGEMENT").equals("1")) ||   //NO005
                        (null != param[5] && param[5].equals("2") && !judgement_group_no.equals("d") && !judgement_group_no.equals(rs.getString("JUDGEMENT_GROUP_NO"))) //NO005]
                        ) {
                        page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,total_page,page_arr);
                //明細データをセット
                printExam(svf,param,rs,gyo);

                testdiv = rs.getString("TESTDIV");
                judgement = rs.getString("JUDGEMENT");
                judgement_group_no = rs.getString("JUDGEMENT_GROUP_NO");
                gyo++;
                nonedata = true;
            }
            //最終ページ印刷
            if (nonedata) {
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
            if (param[4].equals("1")) {
                svf.VrsOut("TITLE"        , "入学試験 合否通知書 発行台帳" );
                if (null != param[5] && param[5].equals("5")){
                    svf.VrsOut("SUBTITLE"     , "(追加/繰上分　" + rs.getString("JUDGEMENT_GROUP_NO") +"回目)" ); //NO007
                }
            }else {
                svf.VrsOut("TITLE"        , "入学試験 入学許可書 発行台帳" );
            }
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printExam(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len2 = "0";
        String len4 = "0";
        try {

            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;
            len4 = (10 < (rs.getString("FINSCHOOL_NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"          ,gyo+1    , rs.getString("EXAMNO") );
            svf.VrsOutn("SEX"             ,gyo+1    , rs.getString("SEX_NAME") );
            if (rs.getString("SHDIV") != null && rs.getString("SHDIV").equals("1")){
                svf.VrsOutn("SHDIV"       ,gyo+1  , "専" ); //専
            }else {
                svf.VrsOutn("SHDIV"       ,gyo+1  , "併" ); //併
            }

            svf.VrsOutn("DESIREDIV1"      ,gyo+1    , rs.getString("WISHNO1") );
            svf.VrsOutn("DESIREDIV2"      ,gyo+1    , rs.getString("WISHNO2") );
            svf.VrsOutn("DESIREDIV3"      ,gyo+1    , rs.getString("WISHNO3") );
            if (rs.getString("JUDGEMENT") != null && rs.getInt("JUDGEMENT") == 1){
                svf.VrsOutn("JUDGEMENT1"      ,gyo+1    , rs.getString("GOUHI") );
                svf.VrsOutn("NOTICENO1"       ,gyo+1    , rs.getString("HAKKOUNO") );
            }else {
                svf.VrsOutn("JUDGEMENT2"      ,gyo+1    , rs.getString("GOUHI") );
                svf.VrsOutn("NOTICENO2"       ,gyo+1    , rs.getString("HAKKOUNO") );
            }

            svf.VrsOutn("NAME"+len2       ,gyo+1    , rs.getString("NAME") );
            svf.VrsOutn("KANA"            ,gyo+1    , rs.getString("NAME_KANA") );
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
            stb.append("    SELECT TESTDIV,EXAMNO,DESIREDIV,SHDIV, ");
            stb.append("           NAME_KANA,NAME,SEX,JUDGEMENT,VALUE(JUDGEMENT_GROUP_NO,'d') AS JUDGEMENT_GROUP_NO, ");    //NO005
            stb.append("           ATTEND_ALL_FLG,SUCCESS_NOTICENO,FAILURE_NOTICENO, ");    //NO005
            stb.append("           FS_CD,SUC_COURSECD,SUC_MAJORCD,SUC_COURSECODE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[13])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[13] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (param[4].equals("1")){  //一般受験者
                stb.append("       AND EXAMNO NOT BETWEEN '5000' AND '5999' ");
                //NO001
                if (null != param[5] && param[5].equals("5")){
                    stb.append("       AND JUDGEMENT IN ('5','6') ");
                    if (null != param[9] && !param[9].equals("99")){
                        stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[9]+"' ");
                    }
//NO004
//              }else if(param[5].equals("6")){
//                  stb.append("       AND JUDGEMENT = '6' ");
//                  if (!param[9].equals("99")){
//                      stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[9]+"' ");
//                  }
                }
            }else {                     //中高一貫
                stb.append("       AND EXAMNO BETWEEN '5000' AND '5999' ");
            }
            //NO005
            if (param[10] != null && param[10].equals("2")){
                if (param[11] != null && param[11].equals("1")){
                    stb.append("       AND ((JUDGEMENT = '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
                }else if(param[11] != null && param[11].equals("2")){
                    stb.append("       AND ((JUDGEMENT >= '7' AND JUDGEMENT < '9') OR JUDGEMENT = '0') ");
                }
            }
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

            //合格者
            stb.append(",PASS_DATA AS ( ");
            stb.append("SELECT T2.TESTDIV,N3.NAME1 AS TEST_NAME, ");
            stb.append("       T2.EXAMNO,T2.NAME_KANA,T2.NAME, ");
            stb.append("       VALUE(T2.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN VALUE(T2.SEX,'0') = '2' THEN '＊' ELSE '' END AS SEX_NAME, ");
            stb.append("       SHDIV, N5.ABBV1 AS SHDIV_NAME, ");
            stb.append("       T2.DESIREDIV, ");
            stb.append("       VALUE(W1.EXAMCOURSE_MARK,'') AS MARK1, ");
            stb.append("       VALUE(W1.EXAMCOURSE_ABBV,'') AS WISHNO1, ");
            stb.append("       W2.EXAMCOURSE_ABBV AS WISHNO2, ");
            stb.append("       W3.EXAMCOURSE_ABBV AS WISHNO3, ");
            stb.append("       '1' AS JUDGEMENT,JUDGEMENT_GROUP_NO,T2.SUC_COURSECODE, ");   //NO005
            stb.append("       CASE WHEN T2.JUDGEMENT = '7' THEN '否' WHEN ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') THEN C1.EXAMCOURSE_ABBV WHEN T2.ATTEND_ALL_FLG = '0' THEN '欠' ELSE '' END AS GOUHI, ");
            stb.append("       CASE WHEN T2.JUDGEMENT = '7' THEN T2.FAILURE_NOTICENO WHEN ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') THEN T2.SUCCESS_NOTICENO ELSE '' END AS HAKKOUNO, ");
            stb.append("       T2.FS_CD,VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N5 ON N5.NAMECD1='L006' AND N5.NAMECD2=T2.SHDIV ");
            stb.append("       LEFT JOIN EXAM_WISH W1 ON W1.TESTDIV=T2.TESTDIV AND W1.DESIREDIV=T2.DESIREDIV AND W1.WISHNO='1' ");
            stb.append("       LEFT JOIN EXAM_WISH W2 ON W2.TESTDIV=T2.TESTDIV AND W2.DESIREDIV=T2.DESIREDIV AND W2.WISHNO='2' ");
            stb.append("       LEFT JOIN EXAM_WISH W3 ON W3.TESTDIV=T2.TESTDIV AND W3.DESIREDIV=T2.DESIREDIV AND W3.WISHNO='3' ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.COURSECD=T2.SUC_COURSECD AND C1.MAJORCD=T2.SUC_MAJORCD AND C1.EXAMCOURSECD=T2.SUC_COURSECODE AND C1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("WHERE  ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') ");
            stb.append("ORDER BY T2.TESTDIV,T2.SUC_COURSECODE DESC,T2.EXAMNO ");
            stb.append("    ) ");
            //不合格者
            stb.append(",UNPASS_DATA AS ( ");
            stb.append("SELECT T2.TESTDIV,N3.NAME1 AS TEST_NAME, ");
            stb.append("       T2.EXAMNO,T2.NAME_KANA,T2.NAME, ");
            stb.append("       VALUE(T2.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN VALUE(T2.SEX,'0') = '2' THEN '＊' ELSE '' END AS SEX_NAME, ");
            stb.append("       SHDIV, N5.ABBV1 AS SHDIV_NAME, ");
            stb.append("       T2.DESIREDIV, ");
            stb.append("       VALUE(W1.EXAMCOURSE_MARK,'') AS MARK1, ");
            stb.append("       VALUE(W1.EXAMCOURSE_ABBV,'') AS WISHNO1, ");
            stb.append("       W2.EXAMCOURSE_ABBV AS WISHNO2, ");
            stb.append("       W3.EXAMCOURSE_ABBV AS WISHNO3, ");
            stb.append("       '2' AS JUDGEMENT,JUDGEMENT_GROUP_NO,T2.SUC_COURSECODE, ");   //NO005
            stb.append("       CASE WHEN T2.JUDGEMENT = '7' THEN '否' WHEN ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') THEN C1.EXAMCOURSE_ABBV WHEN T2.ATTEND_ALL_FLG = '0' THEN '欠' ELSE '' END AS GOUHI, ");
            stb.append("       CASE WHEN T2.JUDGEMENT = '7' THEN T2.FAILURE_NOTICENO WHEN ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') THEN T2.SUCCESS_NOTICENO ELSE '' END AS HAKKOUNO, ");
            stb.append("       T2.FS_CD,VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N5 ON N5.NAMECD1='L006' AND N5.NAMECD2=T2.SHDIV ");
            stb.append("       LEFT JOIN EXAM_WISH W1 ON W1.TESTDIV=T2.TESTDIV AND W1.DESIREDIV=T2.DESIREDIV AND W1.WISHNO='1' ");
            stb.append("       LEFT JOIN EXAM_WISH W2 ON W2.TESTDIV=T2.TESTDIV AND W2.DESIREDIV=T2.DESIREDIV AND W2.WISHNO='2' ");
            stb.append("       LEFT JOIN EXAM_WISH W3 ON W3.TESTDIV=T2.TESTDIV AND W3.DESIREDIV=T2.DESIREDIV AND W3.WISHNO='3' ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.COURSECD=T2.SUC_COURSECD AND C1.MAJORCD=T2.SUC_MAJORCD AND C1.EXAMCOURSECD=T2.SUC_COURSECODE AND C1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("WHERE  T2.JUDGEMENT = '7' ");
            stb.append("ORDER BY T2.TESTDIV,T2.SUC_COURSECODE DESC,T2.EXAMNO ");
            stb.append("    ) ");
            //その他
            stb.append(",SONOTA_DATA AS ( ");
            stb.append("SELECT T2.TESTDIV,N3.NAME1 AS TEST_NAME, ");
            stb.append("       T2.EXAMNO,T2.NAME_KANA,T2.NAME, ");
            stb.append("       VALUE(T2.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN VALUE(T2.SEX,'0') = '2' THEN '＊' ELSE '' END AS SEX_NAME, ");
            stb.append("       SHDIV, N5.ABBV1 AS SHDIV_NAME, ");
            stb.append("       T2.DESIREDIV, ");
            stb.append("       VALUE(W1.EXAMCOURSE_MARK,'') AS MARK1, ");
            stb.append("       VALUE(W1.EXAMCOURSE_ABBV,'') AS WISHNO1, ");
            stb.append("       W2.EXAMCOURSE_ABBV AS WISHNO2, ");
            stb.append("       W3.EXAMCOURSE_ABBV AS WISHNO3, ");
            stb.append("       '3' AS JUDGEMENT,JUDGEMENT_GROUP_NO,T2.SUC_COURSECODE, ");   //NO005
            stb.append("       CASE WHEN T2.JUDGEMENT = '7' THEN '否' WHEN ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') THEN C1.EXAMCOURSE_ABBV WHEN T2.ATTEND_ALL_FLG = '0' THEN '欠' ELSE '' END AS GOUHI, ");
            stb.append("       CASE WHEN T2.JUDGEMENT = '7' THEN T2.FAILURE_NOTICENO WHEN ((T2.JUDGEMENT > '0' AND T2.JUDGEMENT <= '6') OR T2.JUDGEMENT = '9') THEN T2.SUCCESS_NOTICENO ELSE '' END AS HAKKOUNO, ");
            stb.append("       T2.FS_CD,VALUE(F1.FINSCHOOL_NAME,'') AS FINSCHOOL_NAME ");
            stb.append("FROM   EXAM_BASE T2 ");
            stb.append("       LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD=T2.FS_CD ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L003' AND N3.NAMECD2=T2.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST N5 ON N5.NAMECD1='L006' AND N5.NAMECD2=T2.SHDIV ");
            stb.append("       LEFT JOIN EXAM_WISH W1 ON W1.TESTDIV=T2.TESTDIV AND W1.DESIREDIV=T2.DESIREDIV AND W1.WISHNO='1' ");
            stb.append("       LEFT JOIN EXAM_WISH W2 ON W2.TESTDIV=T2.TESTDIV AND W2.DESIREDIV=T2.DESIREDIV AND W2.WISHNO='2' ");
            stb.append("       LEFT JOIN EXAM_WISH W3 ON W3.TESTDIV=T2.TESTDIV AND W3.DESIREDIV=T2.DESIREDIV AND W3.WISHNO='3' ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.COURSECD=T2.SUC_COURSECD AND C1.MAJORCD=T2.SUC_MAJORCD AND C1.EXAMCOURSECD=T2.SUC_COURSECODE AND C1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("WHERE  (T2.JUDGEMENT = '8' OR T2.JUDGEMENT = '0') ");
            stb.append("ORDER BY T2.TESTDIV,T2.SUC_COURSECODE DESC,T2.EXAMNO ");
            stb.append("    ) ");
            //メイン
            stb.append("SELECT * ");
            stb.append("FROM   PASS_DATA ");
            stb.append("UNION ALL ");
            stb.append("SELECT * ");
            stb.append("FROM   UNPASS_DATA ");
            stb.append("UNION ALL ");
            stb.append("SELECT * ");
            stb.append("FROM   SONOTA_DATA ");
            if (param[10] != null && param[10].equals("1")){
                //NO006
                if (null != param[5] && param[5].equals("5")){
                    stb.append("ORDER BY TESTDIV,JUDGEMENT,SHDIV,SUC_COURSECODE DESC,JUDGEMENT_GROUP_NO,EXAMNO ");
                    //NO009 stb.append("ORDER BY TESTDIV,JUDGEMENT,SUC_COURSECODE DESC,JUDGEMENT_GROUP_NO,EXAMNO ");
                }else {
                    stb.append("ORDER BY TESTDIV,JUDGEMENT,SHDIV,SUC_COURSECODE DESC,EXAMNO ");
                    //NO009 stb.append("ORDER BY TESTDIV,JUDGEMENT,SUC_COURSECODE DESC,EXAMNO ");
                }
            }else {
                //NO006
                if (null != param[5] && param[5].equals("5")){
                    stb.append("ORDER BY TESTDIV,JUDGEMENT,JUDGEMENT_GROUP_NO,EXAMNO ");    //NO005
                }else {
                    stb.append("ORDER BY TESTDIV,JUDGEMENT,EXAMNO ");                       //NO005
                }
            }
//log.debug(stb);
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
            stb.append("           NAME_KANA,NAME,SEX, ");
            stb.append("           FS_CD,LOCATIONCD,NATPUBPRIDIV ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[13])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[13] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (param[4].equals("1")){  //一般受験者
                stb.append("       AND EXAMNO NOT BETWEEN '5000' AND '5999' ");
                //NO001
                if (null != param[5] && param[5].equals("5")){
                    stb.append("       AND JUDGEMENT IN ('5','6') ");
                    if (null != param[9] && param[9].equals("99")){
                        stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[9]+"' ");
                    }
//NO004
//              }else if(param[5].equals("6")){
//                  stb.append("       AND JUDGEMENT = '6' ");
//                  if (param[9].equals("99")){
//                      stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[9]+"' ");
//                  }
                }
            }else {                     //中高一貫
                stb.append("       AND EXAMNO BETWEEN '5000' AND '5999' ");
            }
            //NO005
            if (param[10] != null && param[10].equals("2")){
                if (param[11] != null && param[11].equals("1")){
                    stb.append("       AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
                }else if(param[11] != null && param[11].equals("2")){
                    stb.append("       AND ((JUDGEMENT >= '7' AND JUDGEMENT < '9') OR JUDGEMENT = '0') ");
                }
            }
            stb.append("    ) ");

            //メイン
            stb.append("SELECT TESTDIV, ");
            stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   EXAM_BASE ");
            stb.append("GROUP BY TESTDIV ");
            stb.append("ORDER BY TESTDIV ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り



}//クラスの括り
