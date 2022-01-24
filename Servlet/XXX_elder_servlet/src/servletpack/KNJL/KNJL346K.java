// kanji=漢字
/*
 * $Id: ea9b08285eb43c814b1af2fe7da1d9929050631c $
 *
 * 作成日: 2005/11/08 11:25:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
 *                  ＜ＫＮＪＬ３４６＞  各種通知書
 *
 *  2005/11/08 m-yama 作成日
 *  2006/01/05  NO001 o-naka 結果通知書 基準点(３つのコース)を出力するよう修正
 *  2006/01/05  NO002 o-naka 合格通知書 ラジオを追加に対応。---1:全員(合格コース+受験番号順、受験番号順),2:追加／繰上グループNo
 *  2006/01/14  NO003 m-yama スカラシップ文言を追加
 *  2006/01/20  NO004 m-yama ソート順指定を合格通知書の全員→合格通知書に変更
 *  2006/01/24  NO005 o-naka (高校)入学許可書は、中高一貫者のみ出力するよう修正
 *  2006/01/30  NO006 m-yama 入学許可書の組・番号は、頭０サプレスする。
 *  2006/01/31  NO007 m-yama 入学許可書の組はそのまま印字。
 *  2006/02/10  NO008 o-naka (高校)合格通知書「合格コース(昇順)+受験番号順」を「専願併願+合格コース(降順)+受験番号順」にするよう修正
 *  2007/01/30  NO009 m-yama 346_2,_4,_6は、氏名が２０より大きい場合に２行で表示。
 */

public class KNJL346K {


    private static final Log log = LogFactory.getLog(KNJL346K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];//NO002 //NO009

    //  パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");           //次年度
            param[1]  = request.getParameter("TESTDIV");        //試験区分 99:全て
            param[2]  = request.getParameter("OUTPUT");         //出力対象
            param[3]  = request.getParameter("JHFLG");          //中学/高校フラグ 1:中学,2:高校
            param[4]  = request.getParameter("DATE");           //作成日付

            param[6]  = request.getParameter("OUT");            //1:全員,2:追加／繰上グループNo//NO002
            param[7]  = request.getParameter("SORT");           //1:合格コース+受験番号順,2:受験番号順//NO002
            param[8]  = request.getParameter("PASSDIV");        //追加繰上グループNo//NO002
            param[9]  = "OFF";                                  //NO009
            param[10] = request.getParameter("SPECIAL_REASON_DIV"); // 特別理由
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
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD1 = '" + param[10] + "' ");
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

        //作成日
        try {
            if (null != param[4] && !param[4].equals("")){
                param[4] = KNJ_EditDate.h_format_JP(param[4]);
            }else {
                param[4] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年  月  日";
            }
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        //年度
        try {
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            //フォーム
            if (param[3].equals("1")){
                switch( Integer.parseInt(param[2]) ){
                    case 1:
                        svf.VrSetForm("KNJL346_1.frm", 1);    //合格通知書
                        if( printMeisai(db2,svf,param) ) nonedata = true;
                        break;
                    case 2:
                        svf.VrSetForm("KNJL346_3.frm", 1);    //結果通知書
                        if( printMeisai2(db2,svf,param) ) nonedata = true;
                        break;
                    case 3:
                        svf.VrSetForm("KNJL346_5.frm", 1);    //入学許可書
                        if( printMeisai3(db2,svf,param) ) nonedata = true;
                        break;
                    default:
                }
            }else {
                param[9] = "ON";                                   //NO009
                switch( Integer.parseInt(param[2]) ){
                    case 1:
                        svf.VrSetForm("KNJL346_2.frm", 1);    //合格通知書
                        if( printMeisai(db2,svf,param) ) nonedata = true;
                        break;
                    case 2:
                        svf.VrSetForm("KNJL346_4.frm", 1);    //通知書
                        if( printMeisai2(db2,svf,param) ) nonedata = true;
                        break;
                    case 3:
                        svf.VrSetForm("KNJL346_6.frm", 1);    //入学許可書
                        if( printMeisai3(db2,svf,param) ) nonedata = true;
                        break;
                    default:
                }
            }

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
            db2.query(passMeisai(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){
                setInfluenceName(db2, svf, param);

                //明細データをセット
                printPass(svf,param,rs);
                svf.VrEndPage();

                nonedata = true;
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り

    /**明細データ印刷処理*/
    private boolean printMeisai2(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        Map borderMap = new TreeMap();  //基準点
        try {

            if (param[3].equals("1")){
                int len = 0;
                db2.query(borderGet(param));
                ResultSet rs1 = db2.getResultSet();
                while( rs1.next() ){
                    setInfluenceName(db2, svf, param);
                    borderMap.put(rs1.getString("COURSE"),rs1.getString("BORDER_SCORE"));
                    len++;
                }
                rs1.close();
                db2.commit();
            }

            db2.query(failureMeisai(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){

                //明細データをセット
                printFailure(svf,param,rs,borderMap);
                svf.VrEndPage();

                nonedata = true;
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai2 read error!",ex);
        }
        return nonedata;

    }//printMeisai2()の括り

    /**明細データ印刷処理*/
    private boolean printMeisai3(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            db2.query(procedureMeisai(param));
            ResultSet rs = db2.getResultSet();

            while( rs.next() ){

                //明細データをセット
                setInfluenceName(db2, svf, param);
                printProcedure(svf,param,rs);
                svf.VrEndPage();

                nonedata = true;
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai3 read error!",ex);
        }
        return nonedata;

    }//printMeisai3()の括り

    /**明細データをセット*/
    private void printPass(Vrw32alp svf,String param[],ResultSet rs)
    {
        try {

            svf.VrsOut("NOTICENO"         , rs.getString("SUCCESS_NOTICENO") );
            svf.VrsOut("EXAMNO"           , rs.getString("EXAMNO") );
            //NO009
            final String nameField = getNameField(rs.getString("NAME"), param);
            svf.VrsOut(nameField          , rs.getString("NAME") );

            svf.VrsOut("EXAM_COURSE"      , rs.getString("EXAMCOURSE_NAME") );
            svf.VrsOut("DATE"             , param[4] );

            //NO003
            if (param[3].equals("2") && rs.getString("SCALASHIPDIV").equals("01")){
                svf.VrsOut("SCHOLARSHIP"      ,"入学試験の成績が優秀でしたのでK-1スカラシップの資格を認定致します。");
            }else {
                svf.VrsOut("SCHOLARSHIP"      ,"入学試験の結果、K-1スカラシップには該当しませんでした。");
            }
        } catch( Exception ex ) {
            log.warn("printExam read error!",ex);
        }

    }//printExam()の括り

    /**明細データをセット*/
    private void printFailure(Vrw32alp svf,String param[],ResultSet rs,Map borderMap)
    {
        try {
            if (param[3].equals("1")){
                String mapKey = null;
                int keynum = 1;
                for (Iterator it = borderMap.keySet().iterator(); it.hasNext();){
                    final String key = (String) it.next();
                    mapKey = "BORDER"+String.valueOf(keynum);
                    svf.VrsOut(mapKey             , (String) borderMap.get(key) );//NO001
                    keynum++;
                }
                svf.VrsOut("POINT1"           , rs.getString("SCORE1") );
                svf.VrsOut("POINT2"           , rs.getString("SCORE2") );
                svf.VrsOut("POINT3"           , rs.getString("SCORE3") );
                svf.VrsOut("POINT4"           , rs.getString("SCORE4") );
                svf.VrsOut("TOTAL"            , rs.getString("SCORE_SUM") );
                if (!rs.getString("EXAMCOURSE_MARK").equals("I")) {
                    svf.VrsOut("NOTE1"            , "※合計は、国語、算数に社会または理科" );
                    svf.VrsOut("NOTE2"            , "　を加算した点数です。(320点満点)" );
                }
            }

            svf.VrsOut("NOTICENO"         , rs.getString("FAILURE_NOTICENO") );
            svf.VrsOut("EXAMNO"           , rs.getString("EXAMNO") );

            //NO009
            final String nameField = getNameField(rs.getString("NAME"), param);
            svf.VrsOut(nameField          , rs.getString("NAME") );

            svf.VrsOut("DATE"             , param[4] );
        } catch( Exception ex ) {
            log.warn("printExam read error!",ex);
        }

    }//printExam2()の括り

    /**明細データをセット*/
    private void printProcedure(Vrw32alp svf,String param[],ResultSet rs)
    {
        try {
            if (param[3].equals("2")){
                svf.VrsOut("HR_CLASS"         , rs.getString("FS_HRCLASS") );                 //NO007
                svf.VrsOut("ATTENDNO"         , String.valueOf(rs.getInt("FS_ATTENDNO")) );   //NO006
                svf.VrsOut("NOTICENO"         , rs.getString("EXAMNO") );
            }
            svf.VrsOut("EXAMNO"           , rs.getString("EXAMNO") );

            //NO009
            final String nameField = getNameField(rs.getString("NAME"), param);
            svf.VrsOut(nameField          , rs.getString("NAME") );

            svf.VrsOut("BIRTHDAY"         , KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")) );
            svf.VrsOut("NENDO"            , param[5] );
            svf.VrsOut("DATE"             , param[4] );
        } catch( Exception ex ) {
            log.warn("printExam read error!",ex);
        }

    }//printExam3()の括り

    /**
     * フィールド名取得 NO009
     */
    private String getNameField(final String name, String param[])
    {
        final String fieldName;
        int namelen = 0;
        if (name != null && param[9].equals("ON")) {
            final byte namebyte[] = name.getBytes();
            namelen = namebyte.length;
        }
        if (namelen > 20) {
            fieldName = "NAME1";
        } else {
            fieldName = "NAME";
        }
        return fieldName;
    }

    /**
     *  合格通知明細データを抽出
     *
     */
    private String passMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    t1.EXAMNO, ");
            stb.append("    t1.NAME, ");
            stb.append("    t1.NAME_KANA, ");
            stb.append("    t1.SUC_COURSECD, ");
            stb.append("    t1.SUC_MAJORCD, ");
            stb.append("    t1.SUC_COURSECODE, ");
            stb.append("    t1.SUCCESS_NOTICENO, ");
            stb.append("    VALUE(t1.SCALASHIPDIV,'00') AS SCALASHIPDIV, ");    //NO003
            stb.append("    t2.EXAMCOURSE_NAME ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("    AND t2.COURSECD = t1.SUC_COURSECD ");
            stb.append("    AND t2.MAJORCD = t1.SUC_MAJORCD ");
            stb.append("    AND t2.EXAMCOURSECD = t1.SUC_COURSECODE ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[10])) {
                stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
            }
            stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            if (param[3].equals("2")){
                stb.append("    AND t1.EXAMNO NOT BETWEEN '5000' AND '5999' ");
            }
            stb.append("    AND ((t1.JUDGEMENT > '0' AND t1.JUDGEMENT < '7') OR t1.JUDGEMENT = '9') ");
            //NO002
            //合格通知書
            if (param[2].equals("1")) {
                //NO004↓
                //2:追加／繰上グループNo
                if (param[6].equals("2")) {
                    stb.append("    AND t1.JUDGEMENT IN ('5','6') ");
                    if (!param[8].equals("99")) {
                        stb.append("    AND t1.JUDGEMENT_GROUP_NO = '"+param[8]+"' ");
                    }
                }
                if (param[7].equals("1")) {
                    //NO008-->
                    if (param[3].equals("2")) {
                        stb.append("ORDER BY t1.SHDIV,t1.SUC_COURSECODE DESC,t1.EXAMNO ");
                    } else {
                        stb.append("ORDER BY t1.SHDIV,t1.SUC_COURSECODE,t1.EXAMNO ");
                    }
                    //NO008<--
                } else {
                    stb.append("ORDER BY t1.EXAMNO ");
                }
            }

//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り

    /**
     *  通知書明細データを抽出
     *
     */
    private String failureMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            if (param[3].equals("2")){
                stb.append("SELECT ");
                stb.append("    t1.EXAMNO, ");
                stb.append("    t1.NAME, ");
                stb.append("    t1.NAME_KANA, ");
                stb.append("    t1.FAILURE_NOTICENO ");
                stb.append("FROM ");
                stb.append("    ENTEXAM_APPLICANTBASE_DAT t1 ");
                stb.append("WHERE ");
                stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
                stb.append("    AND t1.JUDGEMENT = '7' ");
                stb.append("ORDER BY t1.SUC_COURSECODE,t1.EXAMNO ");
            }else {
                //志願者基礎データ
                stb.append("WITH EXAM_BASE AS ( ");
                stb.append("    SELECT TESTDIV,DESIREDIV,EXAMNO,NAME,NAME_KANA,FAILURE_NOTICENO, ");
                stb.append("           JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ");
                stb.append("    FROM   ENTEXAM_APPLICANTBASE_DAT ");
                stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' ");
                if (!"9".equals(param[10])) {
                    stb.append("    AND SPECIAL_REASON_DIV = '" + param[10] + "' ");
                }
                stb.append("           AND TESTDIV = '"+param[1]+"' ");
                stb.append("           AND JUDGEMENT = '7' ");
                stb.append("    ) ");
                //志願者得点データ
                stb.append(",EXAM_SCORE AS ( ");
                stb.append("    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ");
                stb.append("    FROM   ENTEXAM_SCORE_DAT ");
                stb.append("    WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
                stb.append("           TESTDIV = '"+param[1]+"' AND ");
                stb.append("           A_SCORE IS NOT NULL ");
                stb.append("    ) ");
                //志望区分マスタ：志望連番MAX値
                stb.append(",WISHDIV AS ( ");
                stb.append("    SELECT t1.DESIREDIV,t1.WISHNO,t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD AS COURSE,t2.EXAMCOURSE_MARK ");
                stb.append("    FROM   ENTEXAM_WISHDIV_MST t1 ");
                stb.append("           LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
                stb.append("           AND t2.COURSECD||t2.MAJORCD||t2.EXAMCOURSECD = t1.COURSECD||t1.MAJORCD||t1.EXAMCOURSECD ");
                stb.append("    WHERE  t1.ENTEXAMYEAR = '"+param[0]+"' AND ");
                stb.append("           t1.TESTDIV = '"+param[1]+"' AND ");
                stb.append("           (t1.DESIREDIV,t1.WISHNO) IN ( ");
                stb.append("                SELECT DESIREDIV,MAX(WISHNO) AS MAX_WISHNO ");
                stb.append("                FROM   ENTEXAM_WISHDIV_MST ");
                stb.append("                WHERE  ENTEXAMYEAR = '"+param[0]+"' AND ");
                stb.append("                       TESTDIV = '"+param[1]+"' ");
                stb.append("                GROUP BY DESIREDIV ");
                stb.append("                ) ");
                stb.append("    ) ");
                //最終判定コース
                stb.append(",LAST_COURSE AS ( ");
                stb.append("    SELECT T1.EXAMNO, ");
                stb.append("           T4.COURSE,t4.EXAMCOURSE_MARK ");//否
                stb.append("    FROM   EXAM_BASE T1 ");
                stb.append("           LEFT JOIN WISHDIV T4 ON T4.DESIREDIV=T1.DESIREDIV ");
                stb.append("    ) ");
                //満点マスタ：採用区分
                stb.append(",PERFECT AS ( ");
                stb.append("    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ");
                stb.append("           TESTSUBCLASSCD,ADOPTIONDIV,A_TOTAL_FLG,B_TOTAL_FLG ");
                stb.append("    FROM   ENTEXAM_PERFECT_MST ");
                stb.append("    WHERE  ENTEXAMYEAR='"+param[0]+"' ");
                stb.append("           AND TESTDIV='"+param[1]+"' ");
                stb.append("    ) ");
                //成績：後期（合計・平均・順位の計算用）
                stb.append(",BASE_SCORE AS ( ");
                stb.append("    SELECT T1.EXAMNO,T1.COURSE,T2.TESTSUBCLASSCD, ");
                stb.append("    CASE WHEN T1.EXAMCOURSE_MARK = 'I' THEN T2.A_SCORE ");
                stb.append("         ELSE T2.B_SCORE END AS SCORE, ");
                stb.append("    T1.EXAMCOURSE_MARK ");
                stb.append("    FROM   LAST_COURSE T1 ");
                stb.append("           LEFT JOIN EXAM_SCORE T2 ON T2.EXAMNO = T1.EXAMNO ");
                stb.append("    ) ");
                stb.append(",BASE_SCORE2 AS ( ");
                stb.append("    SELECT T1.EXAMNO,T1.COURSE,T1.TESTSUBCLASSCD,T2.ADOPTIONDIV, ");
                stb.append("    CASE WHEN T1.EXAMCOURSE_MARK = 'I' AND T2.A_TOTAL_FLG = '0' THEN 0 ");
                stb.append("         ELSE T1.SCORE END AS SCORE, ");
                stb.append("    CASE WHEN T1.EXAMCOURSE_MARK = 'I' THEN T2.A_TOTAL_FLG ");
                stb.append("         ELSE T2.B_TOTAL_FLG END AS TOTAL_FLG ");
                stb.append("    FROM   BASE_SCORE T1 ");
                stb.append("           LEFT JOIN PERFECT T2 ON T2.COURSE=T1.COURSE AND T2.TESTSUBCLASSCD=T1.TESTSUBCLASSCD ");
                stb.append("    ) ");
                stb.append(",BASE_SCORE3 AS ( ");
                stb.append("    SELECT EXAMNO,COURSE,TESTSUBCLASSCD,SCORE ");
                stb.append("    FROM   BASE_SCORE2 ");
                stb.append("    WHERE  ADOPTIONDIV='0' ");
                stb.append("    UNION ");
                stb.append("    SELECT EXAMNO,COURSE,'0' AS TESTSUBCLASSCD,MAX(SCORE) AS SCORE ");
                stb.append("    FROM   BASE_SCORE2 ");
                stb.append("    WHERE  ADOPTIONDIV='1' ");
                stb.append("           AND TOTAL_FLG='1' ");
                stb.append("    GROUP BY EXAMNO,COURSE ");
                stb.append("    ) ");
                //成績：後期（合計・平均・順位）
                stb.append(",SCORE_S AS ( ");
                stb.append("    SELECT EXAMNO,COURSE, ");
                stb.append("           SUM(SCORE) AS SCORE_SUM ");
                stb.append("    FROM   BASE_SCORE3 ");
                stb.append("    GROUP BY EXAMNO,COURSE ");
                stb.append("    ) ");
                //成績：前期（素点）
                stb.append(",SCORE AS ( ");
                stb.append("    SELECT T1.EXAMNO, ");
                stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '1' THEN T2.SCORE ELSE NULL END) AS SCORE1, ");
                stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '2' THEN T2.SCORE ELSE NULL END) AS SCORE2, ");
                stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '3' THEN T2.SCORE ELSE NULL END) AS SCORE3, ");
                stb.append("           SUM(CASE WHEN T2.TESTSUBCLASSCD = '4' THEN T2.SCORE ELSE NULL END) AS SCORE4 ");
                stb.append("    FROM   EXAM_BASE T1, BASE_SCORE T2 ");
                stb.append("    WHERE  T2.EXAMNO=T1.EXAMNO ");
                stb.append("    GROUP BY T1.EXAMNO ");
                stb.append("    ) ");

                //メイン
                stb.append("SELECT ");
                stb.append("       T1.EXAMNO,T1.NAME,T1.NAME_KANA, ");
                stb.append("       T2.SCORE1,T2.SCORE2,T2.SCORE3,T2.SCORE4, ");
                stb.append("       T5.SCORE_SUM, ");
                stb.append("       T5.COURSE, ");
                stb.append("       T1.FAILURE_NOTICENO, ");
                stb.append("       T6.EXAMCOURSE_MARK ");
                stb.append("FROM   EXAM_BASE T1 ");
                stb.append("       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ");
                stb.append("       LEFT JOIN SCORE_S T5 ON T5.EXAMNO=T1.EXAMNO ");
                stb.append("       LEFT JOIN LAST_COURSE T6 ON T6.EXAMNO=T1.EXAMNO ");
                stb.append("ORDER BY T1.TESTDIV,T1.EXAMNO ");
            }

//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り

    /**
     *  通知書明細データを抽出
     *
     */
    private String borderGet(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    BORDER_SCORE, ");
            stb.append("    COURSECD || MAJORCD || EXAMCOURSECD AS COURSE ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_PASSINGMARK_MST ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+param[1]+"' ");
            stb.append("ORDER BY SHDIV,EXAMCOURSECD DESC ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り

    /**
     *  入学許可書明細データを抽出
     *
     */
    private String procedureMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("SELECT ");
            stb.append("    t1.EXAMNO, ");
            stb.append("    t1.NAME, ");
            stb.append("    t1.NAME_KANA, ");
            stb.append("    t1.FS_HRCLASS, ");
            stb.append("    t1.FS_ATTENDNO, ");
            stb.append("    t1.BIRTHDAY ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("    AND t2.COURSECD = t1.SUC_COURSECD ");
            stb.append("    AND t2.MAJORCD = t1.SUC_MAJORCD ");
            stb.append("    AND t2.EXAMCOURSECD = t1.SUC_COURSECODE ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[10])) {
                stb.append("    AND t1.SPECIAL_REASON_DIV = '" + param[10] + "' ");
            }
            stb.append("    AND t1.TESTDIV = '"+param[1]+"' ");
            stb.append("    AND t1.PROCEDUREDIV = '2' ");
            stb.append("    AND (t1.ENTDIV = '2' OR t1.ENTDIV = '1') ");
            //NO005
            if (param[3].equals("2")){
                stb.append("    AND t1.EXAMNO BETWEEN '5000' AND '5999' ");
            }
            stb.append("ORDER BY t1.EXAMNO ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り

}//クラスの括り
