// kanji=漢字
/*
 * $Id: f08b60d9b200fb597c084622dd1c2f92a07d97ec $
 *
 * 作成日: 2005/09/05 11:25:40 - JST
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
 *                  ＜ＫＮＪＬ３５１＞  学校別・塾別合否一覧(高校)
 *
 *  2005/09/05 nakamoto 作成日
 *  2005/09/13 nakamoto 郵便番号マスタの複数データに対応
 *
 *  2005/10/27 nakamoto 塾別の時も、サブタイトルを表記する。---NO001
 *                      合否欄、コース欄を一緒にする。（合格したコース、不合格の時は'否'を表記する。）---NO002
 *  2005/11/07 m-yama   専併区分の表示を'専'、'併'に変更 ---NO003
 *  2005/11/18 nakamoto NO004:合否欄は、第１志望合格・推薦合格の場合「合」と表記
 *  2006/01/17 nakamoto NO005:APPROVAL_FLG='1'の場合のみ合否欄から備考欄まで印字する
 *  2006/01/24 nakamoto NO006:(学校別)学校名を中学校名に変更。コードはカット。市区町村を所在地に変更。
 */
public class KNJL351K {


    private static final Log log = LogFactory.getLog(KNJL351K.class);

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
            param[1] = request.getParameter("DATE");                        //印刷日付
            param[2] = request.getParameter("OUTPUT2");                     //出力対象 1:学校,2:塾
            //学校・塾コードリスト
            String classcd[] = request.getParameterValues("DATA_SELECTED");
            param[3] = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) param[3] = param[3] + ",";
                param[3] = param[3] + "'" + classcd[ia] + "'";
            }
            param[3] = param[3] + ")";

            param[4] = request.getParameter("OUTPUT");                      //通知承諾可のみ
            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
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

        svf.VrSetForm("KNJL351.frm", 1);

    //  次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //  作成日
        try {
            param[8] = KNJ_EditDate.h_format_JP(param[1]);
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
            int total_code = getTotalCode(db2,svf,param);//学校・塾のコード数
            int total_page[] = new int[total_code];
            getTotalPage(db2,svf,param,total_page);

            //明細データ
            if( printMeisai(db2,svf,param,total_page) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**学校・塾のコード数*/
    private int getTotalCode(DB2UDB db2,Vrw32alp svf,String param[])
    {
        int ret_val = 1;
        try {
            db2.query(statementTotalCode(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                ret_val = rs.getInt("CNT");
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalCode read error!",ex);
        }
        return ret_val;

    }//getTotalCode()の括り


    /**総ページ数*/
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
            while( rs.next() ){
                //１ページ印刷
                if (49 < gyo || 
                    (!testdiv.equals("d") && !testdiv.equals(rs.getString("SCHOOLCD"))) ) {
                    //合計印刷
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("SCHOOLCD"))) ) {
                        printTotal(svf,param,sex1_cnt,sex2_cnt,sex_cnt);     //合計出力のメソッド
                    }
                    svf.VrEndPage();
                    page_cnt++;
                    gyo = 0;
                    if ((!testdiv.equals("d") && !testdiv.equals(rs.getString("SCHOOLCD"))) ) {
                        sex_cnt = 0;sex1_cnt = 0;sex2_cnt = 0;page_cnt = 1;page_arr++;
                    }
                }
                //見出し
                printHeader(db2,svf,param,rs,page_cnt,total_page[page_arr]);
                //明細データ
                printScore(svf,param,rs,gyo);
                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;

                testdiv = rs.getString("SCHOOLCD");
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
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[],ResultSet rs,int page_cnt,int total_page)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("TITLE"        , (param[2].equals("1")) ? "学校別合否一覧表" : "塾別合否一覧表" );
            svf.VrsOut("NOTE"         , "合格者については成績を公表しておりませんのでご了承下さい" );//NO001
            svf.VrsOut("DATE"         , param[8] );
            //NO006
            if (param[2].equals("1")) svf.VrsOut("LOCATION"   , "所在地："+rs.getString("LOCATION_NAME") );
            svf.VrsOut("SCHOOLCD"     , rs.getString("SCHOOLCD") );
            svf.VrsOut("SCHOOLNAME"   , rs.getString("SCHOOL_NAME") );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , String.valueOf(total_page) );
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
            len2 = (10 < (rs.getString("NAME")).length()) ? "2" : "1" ;

            svf.VrsOutn("EXAMNO"      ,gyo+1  , rs.getString("EXAMNO") );     //受験番号
            svf.VrsOutn("NAME"+len2   ,gyo+1  , rs.getString("NAME") );       //氏名
            svf.VrsOutn("SEX"         ,gyo+1  , rs.getString("SEX_NAME") );   //性別

            svf.VrsOutn("DESIREDIV"  ,gyo+1  , rs.getString("DESIREDIV") ); //志望区分
//NO003
            if (rs.getString("SHDIV") != null && rs.getString("SHDIV").equals("1")){
                svf.VrsOutn("SHDIV"       ,gyo+1  , "専" ); //専
            }else {
                svf.VrsOutn("SHDIV"       ,gyo+1  , "併" ); //併
            }

        //NO005
        if (rs.getString("APPROVAL_FLG") != null && (rs.getString("APPROVAL_FLG")).equals("1")) {
            svf.VrsOutn("JUDGEMENT"   ,gyo+1  , ( rs.getString("JUDGE1") != null ) ? rs.getString("JUDGE1") : rs.getString("JUDGE2") );
            svf.VrsOutn("TOTAL1"      ,gyo+1  , rs.getString("TOTAL1") );     //成績Ａ(合計)
            svf.VrsOutn("POINT1_1"    ,gyo+1  , rs.getString("SCORE1_1") );   //成績Ａ(国語)
            svf.VrsOutn("POINT1_2"    ,gyo+1  , rs.getString("SCORE1_2") );   //成績Ａ(社会)
            svf.VrsOutn("POINT1_3"    ,gyo+1  , rs.getString("SCORE1_3") );   //成績Ａ(数学)
            svf.VrsOutn("POINT1_4"    ,gyo+1  , rs.getString("SCORE1_4") );   //成績Ａ(理科)
            svf.VrsOutn("POINT1_5"    ,gyo+1  , rs.getString("SCORE1_5") );   //成績Ａ(英語)
            svf.VrsOutn("TOTAL2"      ,gyo+1  , rs.getString("TOTAL2") );     //成績Ｂ(合計)
            svf.VrsOutn("POINT2_3"    ,gyo+1  , rs.getString("SCORE2_3") );   //成績Ｂ(数学)
            svf.VrsOutn("POINT2_5"    ,gyo+1  , rs.getString("SCORE2_5") );   //成績Ｂ(英語)
            svf.VrsOutn("REMARK"      ,gyo+1  , rs.getString("REMARK") );     //備考
        } else {
            svf.VrsOutn("CONSENT"     ,gyo+1  , "否" );     //通知承諾 NO005
        }

        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


    /**
     *  市町村をセット
     *
     * ※最後の文字で判断---2005.09.01
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
     *  明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,SEX,NAME,DESIREDIV,SHDIV,A_TOTAL,B_TOTAL,FS_CD,PS_CD,APPROVAL_FLG, ");//NO005
            stb.append("           LOCATIONCD, ");//NO006
            stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[9])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[9] + "' AND ");
            }
            if (param[2].equals("1")) //学校
                stb.append("       FS_CD IN "+param[3]+" ");
            if (param[2].equals("2")) //塾
                stb.append("       PS_CD IN "+param[3]+" ");
            stb.append("    ) ");
            //志願者得点データ
            stb.append(",EXAM_SCORE AS ( ");
            stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ");
            stb.append("    FROM   ENTEXAM_SCORE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    ) ");
            //成績：（素点）
            stb.append(",SCORE AS ( ");
            stb.append("    SELECT EXAMNO, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN A_SCORE ELSE NULL END) AS SCORE1_1, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN A_SCORE ELSE NULL END) AS SCORE1_2, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN A_SCORE ELSE NULL END) AS SCORE1_3, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN A_SCORE ELSE NULL END) AS SCORE1_4, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN A_SCORE ELSE NULL END) AS SCORE1_5, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN B_SCORE ELSE NULL END) AS SCORE2_1, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN B_SCORE ELSE NULL END) AS SCORE2_2, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN B_SCORE ELSE NULL END) AS SCORE2_3, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN B_SCORE ELSE NULL END) AS SCORE2_4, ");
            stb.append("           SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN B_SCORE ELSE NULL END) AS SCORE2_5 ");
            stb.append("    FROM   EXAM_SCORE ");
            stb.append("    GROUP BY EXAMNO ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.DESIREDIV,W1.WISHNO, ");
            stb.append("           W2.EXAMCOURSE_ABBV AS ABBV, ");
            stb.append("           W2.EXAMCOURSE_MARK AS MARK ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    ) ");
            //志望連番のMAX,MIN
            stb.append(",EXAM_WISHNO_HL AS ( ");
            stb.append("    SELECT DESIREDIV,MAX(WISHNO) AS WISHNO_H,MIN(WISHNO) AS WISHNO_L ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    GROUP BY DESIREDIV ");
            stb.append("    ) ");
            //コース
            stb.append(",WISHNO_HL AS ( ");
            stb.append("    SELECT W1.DESIREDIV, ");
            stb.append("           W2.ABBV AS ABBV_H, ");
            stb.append("           W2.MARK AS MARK_H, ");
            stb.append("           W3.ABBV AS ABBV_L, ");
            stb.append("           W3.MARK AS MARK_L ");
            stb.append("    FROM   EXAM_WISHNO_HL W1 ");
            stb.append("           LEFT JOIN EXAM_WISH W2 ON W2.DESIREDIV=W1.DESIREDIV AND W2.WISHNO=W1.WISHNO_H ");
            stb.append("           LEFT JOIN EXAM_WISH W3 ON W3.DESIREDIV=W1.DESIREDIV AND W3.WISHNO=W1.WISHNO_L ");
            stb.append("    ) ");
            //郵便番号マスタ---2005.09.13
            stb.append(",ZIPCD AS ( ");
            stb.append("    SELECT NEW_ZIPCD,MAX(ZIPNO) AS ZIPNO_MAX ");
            stb.append("    FROM   ZIPCD_MST ");
            stb.append("    GROUP BY NEW_ZIPCD ");
            stb.append("    ) ");
            stb.append(",ZIPCD2 AS ( ");
            stb.append("    SELECT NEW_ZIPCD,CITY ");
            stb.append("    FROM   ZIPCD_MST ");
            stb.append("    WHERE  ZIPNO IN (SELECT ZIPNO_MAX FROM ZIPCD) ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.EXAMNO,T1.NAME, ");
            stb.append("       T1.APPROVAL_FLG, ");//NO005
            stb.append("       VALUE(T1.SEX,'0') AS SEX,N1.ABBV1 AS SEX_NAME, ");//性別
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T3.ABBV_H ELSE T3.ABBV_L END AS DESIREDIV, ");//志望区分
            stb.append("       T1.SHDIV,N2.ABBV1 AS SHDIV_NAME, ");//専併
            stb.append("       CASE WHEN ((T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9') THEN '合' ");
            stb.append("            WHEN T1.JUDGEMENT = '7' THEN '否' ");
            stb.append("            WHEN T1.A_TOTAL IS NULL AND T1.B_TOTAL IS NULL THEN '欠' ELSE NULL END AS JUDGE2, ");//合否
            stb.append("       CASE WHEN T1.JUDGEMENT = '1' OR T1.JUDGEMENT = '4' THEN '合' ");//NO004
            stb.append("            WHEN ((T1.JUDGEMENT > '0' AND T1.JUDGEMENT < '7') OR T1.JUDGEMENT = '9') THEN T4.EXAMCOURSE_ABBV ELSE NULL END AS JUDGE1, ");//コース
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T1.A_TOTAL ELSE NULL END AS TOTAL1, ");//Ａ総点
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE1_1 ELSE NULL END AS SCORE1_1, ");//Ａ得点(国語)
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE1_2 ELSE NULL END AS SCORE1_2, ");//Ａ得点(社会)
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE1_3 ELSE NULL END AS SCORE1_3, ");//Ａ得点(数学)
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE1_4 ELSE NULL END AS SCORE1_4, ");//Ａ得点(理科)
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE1_5 ELSE NULL END AS SCORE1_5, ");//Ａ得点(英語)
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' AND (T3.MARK_H = 'S' OR T3.MARK_H = 'K') THEN T1.B_TOTAL ELSE NULL END AS TOTAL2, ");//Ｂ総点
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' AND T3.MARK_H = 'S' THEN T2.SCORE2_3 ELSE NULL END AS SCORE2_3, ");//Ｂ得点(数学)
            stb.append("       CASE WHEN T1.JUDGEMENT = '7' AND T3.MARK_H = 'K' THEN T2.SCORE2_5 ELSE NULL END AS SCORE2_5, ");//Ｂ得点(英語)
            stb.append("       CASE WHEN T1.JUDGEMENT BETWEEN '2' AND '6' OR T1.JUDGEMENT = '9' THEN N3.ABBV1 ELSE NULL END AS REMARK, ");//備考
            if (param[2].equals("1")) {//学校
                stb.append("       VALUE(N4.NAME1,'') AS LOCATION_NAME, ");//NO006
                stb.append("       T1.FS_CD AS SCHOOLCD, ");
                stb.append("       '中学校名：'||VALUE(T5.FINSCHOOL_NAME,'') AS SCHOOL_NAME ");//NO006
            } else {//塾
                stb.append("       T1.PS_CD AS SCHOOLCD, ");
                stb.append("       '塾名：'||VALUE(T6.PRISCHOOL_NAME,'') AS SCHOOL_NAME ");
            }
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ");
            stb.append("       LEFT JOIN WISHNO_HL T3 ON T3.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD = T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T1.SEX ");
            stb.append("       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L006' AND N2.NAMECD2=T1.SHDIV ");
            stb.append("       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L010' AND N3.NAMECD2=T1.JUDGEMENT ");
            if (param[2].equals("1")) {//学校
                stb.append("       LEFT JOIN FINSCHOOL_MST T5 ON T5.FINSCHOOLCD=T1.FS_CD ");
                stb.append("       LEFT JOIN NAME_MST N4 ON N4.NAMECD1='L007' AND N4.NAMECD2=T1.LOCATIONCD ");//NO006
                stb.append("ORDER BY T1.FS_CD,T1.EXAMNO ");
            } else {//塾
                stb.append("       LEFT JOIN PRISCHOOL_MST T6 ON T6.PRISCHOOLCD=T1.PS_CD ");
                stb.append("ORDER BY T1.PS_CD,T1.EXAMNO ");
            }
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *  学校・塾毎の総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO, ");
            if (param[2].equals("1")) //学校
                stb.append("       FS_CD AS SCHOOLCD ");
            if (param[2].equals("2")) //塾
                stb.append("       PS_CD AS SCHOOLCD ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
            if (!"9".equals(param[9])) {
                stb.append("           SPECIAL_REASON_DIV = '" + param[9] + "' AND ");
            }
            if (param[2].equals("1")) //学校
                stb.append("       FS_CD IN "+param[3]+" ");
            if (param[2].equals("2")) //塾
                stb.append("       PS_CD IN "+param[3]+" ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.SCHOOLCD, ");
            stb.append("       CASE WHEN 0 < MOD(COUNT(*),50) THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END AS COUNT ");
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("GROUP BY T1.SCHOOLCD ");
            stb.append("ORDER BY T1.SCHOOLCD ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り


    /**
     *  学校・塾のコード数を取得
     *
     */
    private String statementTotalCode(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT COUNT(*) AS CNT ");
            if (param[2].equals("1")) stb.append("FROM FINSCHOOL_MST ");//学校
            if (param[2].equals("2")) stb.append("FROM PRISCHOOL_MST ");//塾
        } catch( Exception e ){
            log.warn("statementTotalCode error!",e);
        }
        return stb.toString();

    }//statementTotalCode()の括り



}//クラスの括り
