// kanji=漢字
/*
 * $Id: 42b791ac24c380401b47d52337074e08138921fa $
 *
 * 作成日: 2005/07/29 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３４２Ｋ＞  入学試験合否通知書・入学許可書発行台帳
 *
 *  2005/08/04 nakamoto 作成日
 *  2005/08/15 m-yama   出力種別選択追加
 *  2005/08/16 nakamoto 発行番号は、判定結果をみて印字
 *  2005/08/24 nakamoto 合格コース＋受験番号順に出力。コース毎、及び合否で改ページ。
 *  2005/12/20 m-yama   NO001 欠席と不合格の改頁はなし
 *  2005/12/23 m-yama   NO002 追加/繰上をひとつにまとめる
 *  2006/01/05 o-naka   NO003 出力対象全員 ラジオを追加に対応。---1:合格コース+受験番号順,2:受験番号順
 *  2006/01/06 o-naka   NO004 2:受験番号順のソートおよび改頁を修正
 *  2006/01/11 m-yama   NO005 判定結果の合欄にデータがある場合、否欄は出力しない。
 *  2006/01/12 o-naka   NO006 「附属出身者」の場合、判定結果の否欄は、'否'、'欠'の表記を以下の条件の通りとする
 *                      　　　○附属出身者で１人でも得点データがある場合---テストありと判断
 *                      　　　　　○個人得点（有）Base判定（不合格）--->否欄（否）
 *                      　　　　　○個人得点（無）Base判定（不合格）--->否欄（欠）
 *                      　　　○附属出身者で１人も得点データがない場合---テストなしと判断
 *                      　　　　　○Base判定（不合格）--->否欄（否）
 *  2006/01/20 o-naka   NO007 出力対象追加/繰上合格者 ラジオを追加に対応。---1:合格コース+受験番号順,2:受験番号順
 *  2006/10/25 m-yama   NO008 SORTが１の場合に一般と附属推薦者の間は、改ページする。
 *  2010/01/13 o-naka   NO017 「判定区分が8：未受験」の場合に”欠”表示する
 * @author nakamoto
 * @version $Id: 42b791ac24c380401b47d52337074e08138921fa $
 */

public class KNJL342K {


    private static final Log log = LogFactory.getLog(KNJL342K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[13];//NO003

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //次年度
            param[1] = request.getParameter("TESTDIV");                     //試験区分 1:前期,2:後期

            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[9] = request.getParameter("OUTPUT");                      //出力対象 1:全員,5:追加/繰上合格者
            param[10] = request.getParameter("PASSDIV");                    //追加／繰上合格グループNo

            param[11] = request.getParameter("SORT");           //1:合格コース+受験番号順,2:受験番号順//NO003
            param[12] = request.getParameter("SPECIAL_REASON_DIV");          //特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[12] + "' ");
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

        svf.VrSetForm("KNJL342.frm", 1);

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  学校
        if (param[5].equals("1")) param[6] = "中学校";
        if (param[5].equals("2")) param[6] = "高等学校";

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
            String testdiv = "d";
            String kaipage = "d";   //2005.08.24Add
            String kaipage_tmp = "";//2005.08.24Add
            String examdiv = "d";   //NO008
            boolean fugouflg = false;   //NO001
            boolean passflg = false;    //NO001
            while( rs.next() ){
                if ((rs.getString("JUDGE_SORT")).equals("1")) {
                    if (param[11] != null && param[11].equals("2")) 
                        kaipage_tmp = rs.getString("JUDGE_SORT");
                    else 
                        kaipage_tmp = rs.getString("SUC_COURSECODE"); //2005.08.24Add
                    fugouflg = false;   //NO001
                    passflg = true;     //NO001
                }else {
                    kaipage_tmp = rs.getString("JUDGE_SORT");     //2005.08.24Add
                    if (!passflg){
                        fugouflg = true;
                    }
                    passflg = false;
                }
                //１ページ印刷
                if (14 < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(rs.getString("TESTDIV"))) || 
                    (!kaipage.equals("d") && !kaipage.equals(kaipage_tmp) && !fugouflg) ||
                    (!examdiv.equals("d") && !examdiv.equals(rs.getString("EXAMDIV")))) {   //NO008
                    svf.VrEndPage();
                    gyo = 0;
                }
                //見出し
                printHeader(db2, svf,param,rs);
                //明細データ
                printScore(svf,param,rs,gyo);

                testdiv = rs.getString("TESTDIV");
                kaipage = kaipage_tmp;//2005.08.24Add
                examdiv = rs.getString("EXAMDIV");  //NO008
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
    private void printHeader(DB2UDB db2, Vrw32alp svf,String param[],ResultSet rs)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (rs.getString("TEST_NAME") != null) svf.VrsOut("TESTDIV"      , rs.getString("TEST_NAME") );
            svf.VrsOut("DATE"         , param[8] );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        String len2 = "0";
        try {
            gyo = gyo+1;
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"     ,gyo      , rs.getString("EXAMNO") );
            svf.VrsOutn("KANA"       ,gyo      , rs.getString("NAME_KANA") );
            svf.VrsOutn("NAME"+len2  ,gyo      , rs.getString("NAME") );
            svf.VrsOutn("SEX"        ,gyo      , rs.getString("SEX_NAME") );

            svf.VrsOutn("JUDGEMENT1"   ,gyo      , rs.getString("JUDGE1") );
            //NO005
            if (null == rs.getString("JUDGE1") || rs.getString("JUDGE1").equals("")){
                svf.VrsOutn("JUDGEMENT2"   ,gyo      , rs.getString("JUDGE2") );
            }
            svf.VrsOutn("NOTICENO1"   ,gyo      , rs.getString("JUDGE1NO") );
            svf.VrsOutn("NOTICENO2"   ,gyo      , rs.getString("JUDGE2NO") );
        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


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
            stb.append("    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,SUCCESS_NOTICENO,FAILURE_NOTICENO, ");
            stb.append("           JUDGEMENT,SUC_COURSECD,SUC_MAJORCD,SUC_COURSECODE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[12])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[12] + "' ");
            }
            if (!param[1].equals("99")) //試験区分
                stb.append("       AND TESTDIV = '"+param[1]+"' ");
            if (!param[9].equals("1")){ //出力対象
                stb.append("       AND JUDGEMENT IN ('5','6') ");   //NO002
                if (!param[10].equals("99")) //追加／繰上合格グループNo
                    stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[10]+"' ");
            }
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",SCORE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO, ");
            stb.append("           SUM(A_SCORE) AS SCORE_SUM ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       TESTDIV = '"+param[1]+"' AND ");
            stb.append("           A_SCORE IS NOT NULL ");
            stb.append("    GROUP BY TESTDIV,EXAMNO ");
            stb.append("    ) ");
//NO006↓
            //附属で１人でも得点データがある場合（テストあり：試験区分毎）
            stb.append(",FUZOKU1 AS ( ");
            stb.append("    SELECT TESTDIV,COUNT(*) AS CNT_F ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!param[1].equals("99")) //試験区分
                stb.append("       TESTDIV = '"+param[1]+"' AND ");
            if (param[5].equals("1")) //中学
                stb.append("       EXAMNO BETWEEN '3000' AND '3999' AND ");
            if (param[5].equals("2")) //高校
                stb.append("       EXAMNO BETWEEN '5000' AND '5999' AND ");
            stb.append("           A_SCORE IS NOT NULL ");
            stb.append("    GROUP BY TESTDIV ");
            stb.append("    ) ");
            //附属でテストありの者
            stb.append(",FUZOKU2 AS ( ");
            stb.append("    SELECT W1.TESTDIV,W1.EXAMNO,W2.CNT_F ");
            stb.append("    FROM   EXAM_BASE W1, FUZOKU1 W2 ");
            stb.append("    WHERE  W1.TESTDIV = W2.TESTDIV AND ");
            if (param[5].equals("1")) //中学
                stb.append("       W1.EXAMNO BETWEEN '3000' AND '3999' ");
            if (param[5].equals("2")) //高校
                stb.append("       W1.EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append("    ) ");
//NO006↑

            //メイン
            stb.append("SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       T1.EXAMNO,T1.NAME,T1.NAME_KANA, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T1.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ");
            stb.append("       T1.SUC_COURSECODE, ");//2005.08.24Add
//NO006↓
                            //判定結果：合
            stb.append("       T4.EXAMCOURSE_ABBV AS JUDGE1, ");
            //NO008 合格コース+受験番号順で、中学の場合
            if (param[11] != null && param[11].equals("1") && param[5].equals("1")) {
                stb.append("       CASE WHEN T1.EXAMNO BETWEEN '3000' AND '3999' THEN '1' ELSE '0' END AS EXAMDIV, ");
            } else {
                stb.append("       '0' AS EXAMDIV, ");
            }
                            //判定結果：否 NO006
            stb.append("       CASE WHEN F2.CNT_F IS NOT NULL ");
                                    //?附属でテストありの者の場合
            stb.append("            THEN ");
            stb.append("            CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN NULL ");
            stb.append("                 WHEN T1.JUDGEMENT = '7' AND T2.SCORE_SUM IS NOT NULL THEN '否' ");
            stb.append("                 WHEN T1.JUDGEMENT = '7' AND T2.SCORE_SUM IS NULL THEN '欠' ");
            stb.append("                 WHEN T1.JUDGEMENT = '8' THEN '欠' ELSE NULL END "); //NO017
                                    //?以外の者の場合
            stb.append("            ELSE ");
            stb.append("            CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN NULL ");
            stb.append("                 WHEN T1.JUDGEMENT = '7' THEN '否' ");
            stb.append("                 WHEN T2.SCORE_SUM IS NULL THEN '欠' ");
            stb.append("                 WHEN T1.JUDGEMENT = '8' THEN '欠' ELSE NULL END "); //NO017
            stb.append("            END AS JUDGE2, ");
                            //ソート用 NO006
            stb.append("       CASE WHEN F2.CNT_F IS NOT NULL ");
                                    //?附属でテストありの者の場合
            stb.append("            THEN ");
            stb.append("            CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN '1' ");
            stb.append("                 WHEN T1.JUDGEMENT = '7' AND T2.SCORE_SUM IS NOT NULL THEN '2' ");
            stb.append("                 WHEN T1.JUDGEMENT = '7' AND T2.SCORE_SUM IS NULL THEN '3' ");
            stb.append("                 WHEN T1.JUDGEMENT = '8' THEN '3' ELSE '4' END "); //NO017
                                    //?以外の者の場合
            stb.append("            ELSE ");
            stb.append("            CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN '1' ");
            stb.append("                 WHEN T1.JUDGEMENT = '7' THEN '2' ");
            stb.append("                 WHEN T2.SCORE_SUM IS NULL THEN '3' ");
            stb.append("                 WHEN T1.JUDGEMENT = '8' THEN '3' ELSE '4' END "); //NO017
            stb.append("            END AS JUDGE_SORT, ");
                            //発行番号：合
            stb.append("       CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN T1.SUCCESS_NOTICENO ");
            stb.append("            ELSE NULL END AS JUDGE1NO, ");
                            //発行番号：否
            stb.append("       CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN NULL ");
            stb.append("            WHEN T1.JUDGEMENT = '7' THEN T1.FAILURE_NOTICENO ");
            stb.append("            ELSE NULL END AS JUDGE2NO ");
//NO006↑
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN SCORE T2 ON T2.TESTDIV=T1.TESTDIV AND T2.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                          T4.COURSECD=T1.SUC_COURSECD AND  ");
            stb.append("                                          T4.MAJORCD=T1.SUC_MAJORCD AND  ");
            stb.append("                                          T4.EXAMCOURSECD=T1.SUC_COURSECODE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("       LEFT JOIN FUZOKU2 F2 ON F2.TESTDIV=T1.TESTDIV AND F2.EXAMNO=T1.EXAMNO ");//NO006
            if (param[11] != null && param[11].equals("2")) 
                stb.append("ORDER BY T1.TESTDIV,JUDGE_SORT,EXAMDIV,T1.EXAMNO ");//NO004 NO008
            else 
                stb.append("ORDER BY T1.TESTDIV,JUDGE_SORT,T1.SUC_COURSECODE,EXAMDIV,T1.EXAMNO ");   //NO008
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
//log.debug(stb);
        return stb.toString();

    }//statementMeisai()の括り



}//クラスの括り
