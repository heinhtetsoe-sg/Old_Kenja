// kanji=漢字
/*
 * $Id: f859816dd4c18a5355d683eafb6941f6c8652ccf $
 *
 * 作成日: 2005/08/06 11:25:40 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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
*   学校教育システム 賢者 [入試管理]
*
*                   ＜ＫＮＪＬ３４５Ｋ＞  2:追加合格者名簿,3:繰上合格者名簿,5:入学辞退者名簿,6:手続辞退者名簿
*
*   2005/08/06 nakamoto 作成日
*   2005/08/24 nakamoto 6:手続辞退者名簿を追加
*   2005/09/01 nakamoto 高校にも対応
*   2005/09/04 nakamoto コースを降順で出力
*   2005/09/05 nakamoto 手続辞退者の条件を変更
*   2005/09/06 nakamoto 高校用のフォームＩＤを変更
*
*   2005/10/31 nakamoto NO001：追加合格者名簿にて、受験生が別のコースに印字されている不具合修正
*                       例：合格コースが特進コースの受験生が、理数科コースに印字されている
*   2005/11/09 nakamoto NO002：仕様変更
*   2005/12/29 nakamoto NO003：手続辞退者名簿（高校）は、フォーム変更(50X4)。コース別男女計を出力。
*   2006/01/05 nakamoto NO004：追加合格者名簿（高校）繰上合格者名簿（高校）入学辞退者名簿（高校）は、フォーム変更(50X4)。コース別男女計を出力。
*   2006/01/14 nakamoto NO005：追加合格者名簿（高校）繰上合格者名簿（高校）は、名前の下に電話番号を表記
*   2006/01/14 nakamoto NO006：追加合格者名簿（高校）繰上合格者名簿（高校）は、２５名表記に変更。よってフォームＩＤを変更(KNJL345_7)。
*   2006/01/14 nakamoto NO007：合格コースがnullでない条件をＳＱＬに追加
*                           　 ○合格コースがnullの場合に帳票エラーになるための対応
*   2006/01/19 nakamoto NO008：(高校)コースは、コースコードを降順にソート。つまり、理・国・特・進の順。
*   2006/02/07 nakamoto NO009：(高校)中高一貫者は除外するよう修正---各種名簿共通条件
*   2006/10/27 m-yama   NO010：繰上合格の繰上前データは、合格コース履歴テーブルより取得するよう修正。
*   2006/10/30 m-yama   NO011：ENTEXAM_COURSE_HIST_DAT追加に伴う修正をコメント化(保留：メール2006/10/28)
*  2006/10/30 m-yama   NO012：ENTEXAM_COURSE_HIST_DAT追加に伴う修正をコメント化解除
*/
public class KNJL345K {


    private static final Log log = LogFactory.getLog(KNJL345K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();             //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        Map cflgmap = new HashMap();            //データをセットする列番号保管用 NO001
        String param[] = new String[16];

    //    パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                         //次年度
            param[1] = request.getParameter("TESTDIV");                       //試験区分 1:前期,2:後期

            param[3] = request.getParameter("PASSDIV");                     //追加繰上合格グループNo 99:ブランク
            param[4] = request.getParameter("OUTPUT");                      //帳票フラグ 2:追加合格者名簿,3:繰上合格者名簿,5:入学辞退者名簿,6:手続辞退者名簿
            param[5] = request.getParameter("JHFLG");                       //中学/高校フラグ 1:中学,2:高校
            param[14] = request.getParameter("SPECIAL_REASON_DIV");         //特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //    print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //    svf設定
        svf.VrInit();                               //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //    ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


    //    ＳＶＦ作成処理
        boolean nonedata = false;                                 //該当データなしフラグ

        getHeaderData(db2,svf,param,cflgmap);                    //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

           if( printMain(db2,svf,param,cflgmap) ) nonedata = true;

log.debug("nonedata="+nonedata);

    //    該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //     終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[],Map cflgmap){

        //2005.09.06
        if (param[5].equals("1")) {//中学
            if (param[4].equals("5") || param[4].equals("6")) {
                svf.VrSetForm("KNJL345_2.frm", 4);//2005.08.24
            } else {
                svf.VrSetForm("KNJL345_1.frm", 4);
            }
        } else {//高校
            if (param[4].equals("5") || param[4].equals("6")) {
                svf.VrSetForm("KNJL345_5.frm", 4);//NO004
            } else {
                svf.VrSetForm("KNJL345_7.frm", 4);//NO006
            }
        }

        param[13] = (param[5].equals("2") && (param[4].equals("5") || param[4].equals("6"))) ? "50" : "25" ;//NO006

    //    次年度
        try {
            param[7] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    //    学校
        if (param[5].equals("1")) param[6] = "中学校";
        if (param[5].equals("2")) param[6] = "高等学校";

    //    タイトル---2005.08.24
        param[2] = (param[4].equals("2")) ? "追加合格者名簿" : 
                   (param[4].equals("3")) ? "繰上合格者名簿" : 
                   (param[4].equals("5")) ? "入学辞退者名簿" : 
                   (param[4].equals("6")) ? "手続辞退者名簿" : "" ;

    //    作成日
        try {
            String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            String arr_ctrl_date[] = new String[3];
            int number = 0;
            while( rs.next() ){
                arr_ctrl_date[number] = rs.getString(1);
                number++;
            }
            db2.commit();
            param[8] = KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分"+"　現在";
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

    //    コース名称
        try {
            db2.query(statementCourseName(param));
            ResultSet rs = db2.getResultSet();
            int len = 1;//2005.09.01
            while( rs.next() ){
                   svf.VrsOut("EXAMCOURSE"+String.valueOf(len) , rs.getString("EXAMCOURSE_NAME") );

                cflgmap.put( rs.getString("COURSE"), String.valueOf(len-1) );//列番号保管 NO001
                len++;//2005.09.01
            }
            db2.commit();
        } catch( Exception e ){
            log.warn("coursename get error!",e);
        }

    //    コース別男女計---NO003---NO004
        if (param[5].equals("2")) {

            try {
                db2.query(statementTotalCount(param));
                ResultSet rs = db2.getResultSet();
                int gyo = 1;
                while( rs.next() ){
                    svf.VrsOutn("EXAMCOURSE"   , gyo ,   rs.getString("EXAMCOURSE_NAME") );
                    svf.VrsOutn("MEMBER1"      , gyo ,   rs.getString("SEX1") );
                    svf.VrsOutn("MEMBER2"      , gyo ,   rs.getString("SEX2") );
                    svf.VrsOutn("TOTAL_MEMBER" , gyo ,   rs.getString("KEI") );
                    gyo++;
                }
                db2.commit();
            } catch( Exception ex ) {
                log.warn("TotalCount read error!",ex);
            }

        }

        try {
            setInfluenceName(db2, svf, param);
        } catch( Exception e ){
            log.warn("setInfluenceName get error!",e);
        }
    }//getHeaderData()の括り

    private void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[14] + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                param[15] = name2;
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
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],Map cflgmap)
    {
        boolean nonedata = false;
        try {
            //総ページ数
            getTotalPage(db2,svf,param);

            //明細データ
               if( setMeisai(db2,svf,param,cflgmap) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り


    /**試験区分毎の総ページ数*/
    private void getTotalPage(DB2UDB db2,Vrw32alp svf,String param[])
    {
        try {
log.debug("TotalPage start!");
            db2.query(statementTotalPage(param));
            ResultSet rs = db2.getResultSet();
log.debug("TotalPage end!");

               param[11] = "25";
               param[12] = "1";
            while( rs.next() ){
                param[11] = rs.getString("DATA_CNT");   //コース別データ数のMAX値
                param[12] = rs.getString("COUNT");      //総ページ数
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("getTotalPage read error!",ex);
        }

    }//getTotalPage()の括り


    /**明細データを配列にセット*/
    private boolean setMeisai(DB2UDB db2,Vrw32alp svf,String param[],Map cflgmap)
    {
        boolean nonedata = false;
        try {
log.debug("Meisai start!");
            db2.query(statementMeisai(param));
            ResultSet rs = db2.getResultSet();
log.debug("Meisai end!");
log.debug("param[11] = "+param[11]);
log.debug("param[12] = "+param[12]);

            int data_cnt = Integer.parseInt(param[11]);
            String meisai[][][] = new String[4][8][data_cnt];//コース数・項目数・コース別データ数のMAX値 NO005
            int cflg = 0;
            int data = 0;
            String course = "d";
            int sex_cnt = 0;    //合計
            int sex1_cnt = 0;   //男
            int sex2_cnt = 0;   //女
            while( rs.next() ){
                if( !course.equals(rs.getString("SUC_COURSE")) ){
                    if( !course.equals("d") ) data = 0;
                    //列番号をセット NO001
                    String cflg_tmp = (String)cflgmap.get( rs.getString("SUC_COURSE") );
                    cflg = Integer.parseInt(cflg_tmp);
                }
                param[9] = rs.getString("TEST_NAME");
                //cflg = rs.getInt("COURSE_NO");//2005.09.01
                setMeisaiArray(meisai,cflg,data,rs);

                course = rs.getString("SUC_COURSE");
                data++;
                nonedata = true;

                //性別
                if( (rs.getString("SEX")).equals("1") ) sex1_cnt++;
                if( (rs.getString("SEX")).equals("2") ) sex2_cnt++;
                sex_cnt = sex1_cnt + sex2_cnt;
            }
            //ページ印刷
            if (nonedata) {
                param[10] = "男" + String.valueOf(sex1_cnt) + "名、" + 
                            "女" + String.valueOf(sex2_cnt) + "名、" + 
                            "合計" + String.valueOf(sex_cnt) + "名";
                printMeisai(svf,param,meisai);
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("setMeisai read error!",ex);
        }
        return nonedata;

    }//setMeisai()の括り


    /**明細データを配列にセット*/
    private void setMeisaiArray(String meisai[][][],int cflg,int data,ResultSet rs)
    {
        try {
            meisai[cflg][0][data] = rs.getString("EXAMNO");
            meisai[cflg][1][data] = rs.getString("ATTACH");
            meisai[cflg][2][data] = rs.getString("SEX_NAME");
            meisai[cflg][3][data] = rs.getString("NAME");
            meisai[cflg][4][data] = rs.getString("MARK");
            meisai[cflg][5][data] = rs.getString("SUC_MARK");
            meisai[cflg][6][data] = rs.getString("GROUP_NO");
            meisai[cflg][7][data] = rs.getString("TELNO");//NO005
        } catch( Exception ex ) {
            log.warn("setMeisaiArray read error!",ex);
        }

    }//setMeisaiArray()の括り


    /**明細データ印刷処理*/
    private void printMeisai(Vrw32alp svf,String param[],String meisai[][][])
    {
        int flg = 0;
        int page_cnt = 0;
        int data_cnt = Integer.parseInt(param[11]);
        int gyo_max = Integer.parseInt(param[13]);//NO003
        try {

            for (int id = 0; id < data_cnt; id++) {
                if (id % gyo_max == 0) page_cnt++;//NO003
                flg = 0;
                for (int ic = 0; ic < 4; ic++) {
                    if( meisai[ic][0][id] == null ){
                        flg++;
                        continue;
                    }
                    //見出し
                    printHeader(svf,param,page_cnt);
                    //各コースの明細データ
                    printScore(svf,ic,id,meisai);
                }
                if (id == data_cnt-1 && param[5].equals("1")) svf.VrsOut("TOTAL_MEMBER" , param[10] );//NO006
                if (flg < 4) svf.VrEndRecord();
            }

        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り


    /**ヘッダーデータをセット*/
    private void printHeader(Vrw32alp svf,String param[],int page_cnt)
    {
        try {
            svf.VrsOut("NENDO"        , param[7] );
            svf.VrsOut("SCHOOLDIV"    , param[6] );
            if (param[9] != null) svf.VrsOut("TESTDIV"      , param[9] );
            svf.VrsOut("TITLEDIV"     , param[2] );//2005.08.24Modify
            svf.VrsOut("DATE"         , param[8] );

            svf.VrsOut("PAGE"         , String.valueOf(page_cnt) );
            svf.VrsOut("TOTAL_PAGE"   , param[12] );
            svf.VrsOut("VIRUS", param[15]);
        } catch( Exception ex ) {
            log.warn("printHeader read error!",ex);
        }

    }//printHeader()の括り


    /**明細データをセット*/
    private void printScore(Vrw32alp svf,int ic,int id,String meisai[][][])
    {
        try {
               svf.VrsOut("EXAMNO"+String.valueOf(ic+1)          , meisai[ic][0][id] );
               svf.VrsOut("ATTACH"+String.valueOf(ic+1)          , meisai[ic][1][id] );
               svf.VrsOut("SEX"+String.valueOf(ic+1)             , meisai[ic][2][id] );
               svf.VrsOut("DESIREDIV"+String.valueOf(ic+1)       , meisai[ic][4][id] );
               svf.VrsOut("EXAMCOURSE_MARK"+String.valueOf(ic+1) , meisai[ic][5][id] );
               svf.VrsOut("ADDITIONAL"+String.valueOf(ic+1)      , meisai[ic][6][id] );
               svf.VrsOut("PHONE"+String.valueOf(ic+1)           , meisai[ic][7][id] );//NO005

               String len2 = (10 < (meisai[ic][4][id]).length()) ? "_2" : "_1" ;
               svf.VrsOut("NAME"+String.valueOf(ic+1)+len2       , meisai[ic][3][id] );
        } catch( Exception ex ) {
            log.warn("printScore read error!",ex);
        }

    }//printScore()の括り


    /**
     *    明細データを抽出
     *
     */
    private String statementMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NATPUBPRIDIV,SEX,NAME,DESIREDIV,JUDGEMENT_GROUP_NO AS GROUP_NO, ");
            stb.append("           TELNO, ");
            stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[14])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[14] + "' ");
            }
            stb.append("           AND TESTDIV = '"+param[1]+"' ");
            stb.append("           AND APPLICANTDIV NOT IN('2') ");//NO009
            stb.append("           AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            //追加合格者
            if (param[4].equals("2")) 
                stb.append("       AND JUDGEMENT = '5' ");
            //繰上合格者
            if (param[4].equals("3")) 
                stb.append("       AND JUDGEMENT = '6' ");
            //追加繰上合格グループNo
            if (param[3] != null && !param[3].equals("99")) 
                stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[3]+"' ");
            //入学辞退者
            if (param[4].equals("5")) 
                stb.append("       AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");
            //手続辞退者
            if (param[4].equals("6")) 
                stb.append("       AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.TESTDIV,W1.DESIREDIV, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    GROUP BY W1.TESTDIV,W1.DESIREDIV ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.TESTDIV,N1.NAME1 AS TEST_NAME, ");
            stb.append("       T1.EXAMNO,T1.NATPUBPRIDIV, T1.GROUP_NO, ");
            stb.append("       CASE WHEN T1.NATPUBPRIDIV = '9' THEN 'F' ELSE NULL END AS ATTACH, ");
            stb.append("       VALUE(T1.SEX,'0') AS SEX, ");
            stb.append("       CASE WHEN T1.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ");
            stb.append("       T1.NAME,T1.DESIREDIV, ");
            stb.append("       T1.TELNO, ");//NO005
            stb.append("       VALUE(T3.MARK1,'')||VALUE(T3.MARK2,'')||VALUE(T3.MARK3,'') AS MARK, ");//2005.09.01Add
            stb.append("       VALUE(T1.SUC_COURSE,'') AS SUC_COURSE, ");
            if (param[4].equals("3")) {
                //NO010
                stb.append("   VALUE(L3.EXAMCOURSE_MARK,' ') || '-' || T4.EXAMCOURSE_MARK AS SUC_MARK ");

            } else {
                stb.append("   T4.EXAMCOURSE_MARK AS SUC_MARK ");
            }
            stb.append("FROM   EXAM_BASE T1 ");
            stb.append("       LEFT JOIN EXAM_WISH T3 ON T3.TESTDIV=T1.TESTDIV AND T3.DESIREDIV=T1.DESIREDIV ");
            stb.append("       LEFT JOIN (SELECT ");
            stb.append("                      L1.*, ");
            stb.append("                      L2.EXAMCOURSE_MARK ");
            stb.append("                  FROM ");
            stb.append("                      ENTEXAM_COURSE_HIST_DAT L1 ");
            stb.append("                      LEFT JOIN ENTEXAM_COURSE_MST L2 ON L2.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
            stb.append("                           AND L2.COURSECD || L2.MAJORCD || L2.EXAMCOURSECD = L1.SUC_COURSECD || L1.SUC_MAJORCD || L1.SUC_COURSECODE ");
            stb.append("                  WHERE ");
            stb.append("                      L1.ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("                      AND L1.TESTDIV = '"+param[1]+"' ");
            stb.append("                      AND L1.SEQ = 1) L3 ");
            stb.append("            ON L3.EXAMNO = T1.EXAMNO ");
            stb.append("       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND  ");
            stb.append("                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD=T1.SUC_COURSE ");
            stb.append("       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ");
            stb.append("WHERE  T1.SUC_COURSE IS NOT NULL ");//NO007
            stb.append("ORDER BY T1.TESTDIV,T1.SUC_COURSE DESC,T1.EXAMNO ");//2005.09.04Modify
            //log.debug(stb.toString());
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り


    /**
     *    試験区分毎の総ページ数を取得
     *
     */
    private String statementTotalPage(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT TESTDIV,EXAMNO,NATPUBPRIDIV,SEX,NAME,DESIREDIV, ");
            stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[14])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[14] + "' ");
            }
            stb.append("           AND TESTDIV = '"+param[1]+"' ");
            stb.append("           AND APPLICANTDIV NOT IN('2') ");//NO009
               stb.append("           AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            //追加合格者
            if (param[4].equals("2")) 
                stb.append("       AND JUDGEMENT = '5' ");
            //繰上合格者
            if (param[4].equals("3")) 
                stb.append("       AND JUDGEMENT = '6' ");
            //追加繰上合格グループNo
            if (param[3] != null && !param[3].equals("99")) 
                stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[3]+"' ");
            //入学辞退者
            if (param[4].equals("5")) 
                stb.append("       AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");
            //手続辞退者
            if (param[4].equals("6")) 
                stb.append("       AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");
            stb.append("    ) ");
            //志望区分マスタ・受験コースマスタ
            stb.append(",EXAM_WISH AS ( ");
            stb.append("    SELECT W1.TESTDIV,W1.DESIREDIV, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '1' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK1, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '2' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK2, ");
            stb.append("           MAX(CASE WHEN W1.WISHNO = '3' THEN W2.EXAMCOURSE_MARK ELSE NULL END) AS MARK3 ");
            stb.append("    FROM   ENTEXAM_WISHDIV_MST W1, ENTEXAM_COURSE_MST W2 ");
            stb.append("    WHERE  W1.ENTEXAMYEAR = '"+param[0]+"' AND ");
            stb.append("           W1.TESTDIV = '"+param[1]+"' AND ");
            stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append("           W1.COURSECD = W2.COURSECD AND ");
            stb.append("           W1.MAJORCD = W2.MAJORCD AND ");
            stb.append("           W1.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("    GROUP BY W1.TESTDIV,W1.DESIREDIV ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T2.DATA_CNT,T2.COUNT ");
            stb.append("FROM   ( ");
            stb.append("    SELECT MAX(T1.DATA_CNT) AS DATA_CNT,MAX(T1.COUNT) AS COUNT ");
            stb.append("    FROM   ( ");
            stb.append("        SELECT SUC_COURSE,COUNT(*) AS DATA_CNT, ");
            stb.append("               CASE WHEN 0 < MOD(COUNT(*),"+param[13]+") THEN COUNT(*)/"+param[13]+" + 1 ELSE COUNT(*)/"+param[13]+" END AS COUNT ");
            stb.append("        FROM   EXAM_BASE ");
            stb.append("        WHERE  SUC_COURSE IS NOT NULL ");//NO007
            stb.append("        GROUP BY SUC_COURSE ");
            stb.append("            ) T1 ");
            stb.append("        ) T2 ");
            stb.append("WHERE T2.COUNT IS NOT NULL ");
        } catch( Exception e ){
            log.warn("statementTotalPage error!",e);
        }
        return stb.toString();

    }//statementTotalPage()の括り


    /**
     *    コース名称を取得
     *
     */
    private String statementCourseName(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("       EXAMCOURSE_NAME,EXAMCOURSE_MARK ");
            stb.append("FROM   ENTEXAM_COURSE_MST ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            if (param[5].equals("2")) 
                stb.append("   AND EXAMCOURSE_MARK IN('S','K','T','P') ");
            stb.append("ORDER BY COURSE DESC ");//2005.09.01Add---2005.09.04Modify
        } catch( Exception e ){
            log.warn("statementCourseName error!",e);
        }
        return stb.toString();

    }//statementCourseName()の括り


    /**
     *    コース別男女計を取得---NO003---NO004
     *
     */
    private String statementTotalCount(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //志願者基礎データ
            stb.append("WITH EXAM_BASE AS ( ");
            stb.append("    SELECT EXAMNO,SEX, ");
            stb.append("           SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
            stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[14])) {
                stb.append("           AND SPECIAL_REASON_DIV = '" + param[14] + "' ");
            }
            stb.append("           AND TESTDIV = '"+param[1]+"' ");
            stb.append("           AND APPLICANTDIV NOT IN('2') ");//NO009
               stb.append("           AND ((JUDGEMENT > '0' AND JUDGEMENT <= '6') OR JUDGEMENT = '9') ");
            //追加合格者
            if (param[4].equals("2")) 
                stb.append("       AND JUDGEMENT = '5' ");
            //繰上合格者
            if (param[4].equals("3")) 
                stb.append("       AND JUDGEMENT = '6' ");
            //追加繰上合格グループNo
            if (param[3] != null && !param[3].equals("99")) 
                stb.append("       AND JUDGEMENT_GROUP_NO = '"+param[3]+"' ");
            //入学辞退者
            if (param[4].equals("5")) 
                stb.append("       AND ENTDIV = '1' AND PROCEDUREDIV = '2' ");
            //手続辞退者
            if (param[4].equals("6")) 
                stb.append("       AND ((VALUE(PROCEDUREDIV,'0') < '2') OR (PROCEDUREDIV = '2' AND ENTDIV IS NULL)) ");
            stb.append("    ) ");
            //受験コースマスタ
            stb.append(",EXAM_COURSE AS ( ");
            stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
            stb.append("           EXAMCOURSE_NAME,EXAMCOURSE_MARK ");
            stb.append("    FROM   ENTEXAM_COURSE_MST ");
            stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' ");
            if (param[5].equals("2")) 
                stb.append("   AND EXAMCOURSE_MARK IN('S','K','T','P') ");
            stb.append("    ) ");
            //コース別男女計
            stb.append(",GOUKEI AS ( ");
            stb.append("    SELECT SUC_COURSE, ");
            stb.append("           SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SEX1, ");
            stb.append("           SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SEX2, ");
            stb.append("           SUM(CASE WHEN SEX = '1' OR SEX = '2' THEN 1 ELSE 0 END) AS KEI ");
            stb.append("    FROM   EXAM_BASE ");
            stb.append("    WHERE  SUC_COURSE IS NOT NULL ");
            stb.append("    GROUP BY SUC_COURSE ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT T1.COURSE,T1.EXAMCOURSE_NAME, ");
            stb.append("       VALUE(T2.SEX1,0) AS SEX1,VALUE(T2.SEX2,0) AS SEX2,VALUE(T2.KEI,0) AS KEI ");
            stb.append("FROM   EXAM_COURSE T1 ");
            stb.append("       LEFT JOIN GOUKEI T2 ON T2.SUC_COURSE=T1.COURSE ");
            //中学
            if (param[5].equals("1")) {
                stb.append("UNION ");
                stb.append("SELECT '99999999' AS COURSE,'合計' AS EXAMCOURSE_NAME, ");
                stb.append("       SUM(T2.SEX1) AS SEX1,SUM(T2.SEX2) AS SEX2,SUM(T2.KEI) AS KEI ");
                stb.append("FROM   GOUKEI T2 ");
                stb.append("ORDER BY COURSE ");
            //高校
            } else {
                stb.append("UNION ");
                stb.append("SELECT '00000000' AS COURSE,'合計' AS EXAMCOURSE_NAME, ");
                stb.append("       SUM(T2.SEX1) AS SEX1,SUM(T2.SEX2) AS SEX2,SUM(T2.KEI) AS KEI ");
                stb.append("FROM   GOUKEI T2 ");
                stb.append("ORDER BY COURSE DESC ");
            }
        } catch( Exception e ){
            log.warn("statementTotalCount error!",e);
        }
        return stb.toString();

    }//statementTotalCount()の括り



}//クラスの括り
