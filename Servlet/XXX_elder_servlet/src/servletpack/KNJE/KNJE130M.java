// kanji=漢字
/*
 * $Id: 781669e2b7b1cf82fc800c1133040d9884eb2638 $
 *
 * 作成日: 2005/07/11 15:46:35 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
 *  学校教育システム 賢者 [進路管理]
 *
 *                  ＜ＫＮＪＥ１３０Ｍ＞  評価・評定平均値一覧表（武蔵）
 *
 *  2009.12.14 作成日
 */

public class KNJE130M {


    private static final Log log = LogFactory.getLog(KNJE130M.class);
    
    private boolean _isGakunensei = false;
    private String _gradeName = "";
    
    private String _useCurriculumcd;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[6];

        //  パラメータの取得
        try {
            param[0] = request.getParameter("CTRL_YEAR");                   //年度
            param[1] = request.getParameter("CTRL_SEMESTER");               //学期 1,2,3
            param[2] = request.getParameter("GRADE");                       //学年
            param[3] = request.getParameter("HYOUTEI");                      //評定
            param[4] = request.getParameter("KESSEKI");                     //欠席数
            param[5] = request.getParameter("CTRL_DATE");                   //日付
            _useCurriculumcd = request.getParameter("useCurriculumcd");
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
        _isGakunensei = "0".equals(getSchoolDiv(db2, param[0]));
        _gradeName = getGradeName(db2, param[0], param[2]);

        for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        printHeader(db2,svf,param);                             //見出し出力のメソッド

        //SVF出力
        if( printMain(db2,svf,param) ) nonedata = true;

        log.debug("nonedata="+nonedata);

        //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り

    private String getGradeName(DB2UDB db2, String year, String grade) {
        String gradeName = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                gradeName = rs.getString("GRADE_NAME1") == null ? "" : rs.getString("GRADE_NAME1");
            }
        } catch (SQLException e) {
            log.error(e);
        }
        return gradeName;
    }

    private String getSchoolDiv(DB2UDB db2, String year) {
        String schoolDiv = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = " SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR = '" + year + "' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                schoolDiv = rs.getString("SCHOOLDIV");
            }
        } catch (SQLException e) {
            log.error(e);
        }
        return schoolDiv;
    }


    /** 見出し出力 **/
    private void printHeader(DB2UDB db2,Vrw32alp svf,String param[]){

        svf.VrSetForm("KNJE130M.frm", 4);
        svf.VrsOut("GRADE", _gradeName);
        svf.VrsOut("SUBTITLE", "(学年平均" + param[3] +"以上、各学年欠席" + param[4] +"日未満かつ進級基準(出欠)で不合格でない者)");

    //  ＳＶＦ属性変更--->学年・組毎に改ページ
        svf.VrAttribute("HR_NAME","FF=1");

    //  年度
        try {
            svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
        } catch( Exception e ){
            log.warn("nendo get error!",e);
        }

        //  作成日
        svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(param[5]));

        //タイトル 評定
        try {
            svf.VrsOut("SELECT", "評定");
        } catch( Exception e ){
            log.warn("title set error!",e);
        }

    }//printHeader()の括り


    /**メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        try {
            Map hm1 = new HashMap();                                        //教科コードと列番号の保管

            printClassAbbv(db2, svf, param, hm1);    //教科略称の出力メソッド

            if( printSchnoAssess(db2, svf, param, hm1) ) nonedata = true;//評定平均値の出力メソッド

            clearClass(svf);                                            //教科略称のクリアメソッド
            hm1.clear();                                                //列番号情報を削除
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り


    /**教科略称の出力*/
    private void printClassAbbv(DB2UDB db2, Vrw32alp svf, String param[], Map hm1)
    {
        try {
            final String sql = psClassAbbv(param);
            log.debug(" sql classAbbv = " + sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int classno = 0;
            while( rs.next() ){
                //列番号に教科コードを付ける
                hm1.put(rs.getString("CLASSCD"),new Integer(++classno));
                //教科略称
                svf.VrsOutn("SUBCLASS"    ,classno  , rs.getString("CLASSABBV") );

            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printClassAbbv read error!",ex);
        }

    }//printClassAbbv()の括り


    /**生徒毎の評定平均値の出力*/
    private boolean printSchnoAssess(DB2UDB db2, Vrw32alp svf, String param[], Map hm1)
    {
        boolean nonedata = false;
        try {
            String sql = psSchnoAssess(param);
            log.debug(" sql schnoAssess = " + sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            String schno = "0";
            while( rs.next() ){
                //学籍番号のブレイク
                if (!schno.equals("0") && !schno.equals(rs.getString("SCHREGNO"))) svf.VrEndRecord();

                final String hrName = (rs.getString("HR_NAME") != null) ? rs.getString("HR_NAME") : "";
                final String attendno = (rs.getString("ATTENDNO") != null) ? String.valueOf(rs.getInt("ATTENDNO")) + "番" : "";
                svf.VrsOut("ATTENDNO"     , hrName + attendno );      //クラス名 + 出席番号
                svf.VrsOut("NAME"         , rs.getString("NAME") );                       //氏名
                svf.VrsOut("TOTAL_AVERAGE", rs.getString("ALL_VAL") );                    //全体平均
                svf.VrsOut("RANK"         , rs.getString("ASSESSMARK") );                 //段階
                svf.VrsOut("NOTE"         , rs.getString("BIKOU") );                      //備考

                schno = rs.getString("SCHREGNO");
                nonedata = true;

                //教科コードに対応した列に評定平均値をセットする
                Integer int1 = (Integer)hm1.get(rs.getString("CLASSCD"));
                if( int1==null )continue;
                svf.VrsOutn("AVERAGE"    ,int1.intValue()  , rs.getString("VAL") );  //各教科の評定平均値

            }
            //最終レコード出力
            if (nonedata) svf.VrEndRecord();
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSchnoAssess read error!", ex);
        }
        return nonedata;

    }//printSchnoAssess()の括り


    /**教科略称のクリア*/
    private void clearClass(Vrw32alp svf) {
        for (int i = 1; i < 21; i++) {
            svf.VrsOutn("SUBCLASS"    ,i  , "" );
        }
    }//printClear()の括り

    /**
     *  教科略称を抽出
     *
     */
    private String psClassAbbv(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT T1.SCHREGNO, T1.HR_CLASS, T1.ATTENDNO, T2.HR_NAME ");
            stb.append("    FROM   SCHREG_REGD_DAT T1");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND ");
            stb.append("           T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("           T2.GRADE = T1.GRADE AND ");
            stb.append("           T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("    WHERE  T1.YEAR = '" + param[0] + "' AND T1.SEMESTER = '" + param[1] + "' AND ");
            stb.append("           T1.GRADE = '" + param[2] + "') ");
            stb.append(" , REGD AS ( ");
            stb.append("    SELECT DISTINCT T1.YEAR, T1.SCHREGNO, T1.GRADE ");
            stb.append("    FROM   SCHREG_REGD_DAT T1");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT T2 ON ");
            stb.append("        T2.YEAR = T1.YEAR AND T2.SCHOOL_KIND = 'H' ");
            stb.append("    WHERE  T1.YEAR <= '" + param[0] + "' AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append("           T1.SCHREGNO NOT IN (SELECT DISTINCT T1.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  SCHREG_ATTENDREC_DAT T1 ");
            stb.append("                  LEFT JOIN SCHOOL_MST T3 ON ");
            stb.append("                      T3.YEAR = T1.YEAR     ");
            stb.append("              GROUP BY ");
            stb.append("                  T1.YEAR, T1.SCHREGNO ");
            stb.append("              HAVING ");
            stb.append("                  SUM(VALUE(SICK, 0) + VALUE(ACCIDENTNOTICE, 0) + VALUE(NOACCIDENTNOTICE, 0) ");
            stb.append("                      + (CASE WHEN T3.SEM_OFFDAYS = '1' THEN VALUE(OFFDAYS, 0) ELSE 0 END) ");
            stb.append("                  ) >= " + param[4] + ") AND ");
            // 進学基準で不合格でないもの
            stb.append("           T1.SCHREGNO NOT IN (SELECT DISTINCT T1.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  ATTEND_SEMES_DAT T1 ");
            stb.append("              GROUP BY ");
            stb.append("                  T1.YEAR, T1.SCHREGNO ");
            stb.append("              HAVING ");
            stb.append("                  SUM(VALUE(KEKKA, 0)) >= 15 ");
            stb.append("                  OR SUM(VALUE(LATEDETAIL, 0)) >= 15 ");
            stb.append("                  OR SUM(VALUE(KEKKA, 0)) + SUM(VALUE(LATEDETAIL, 0)) >= 23 ");
            stb.append("                  ) ");
            stb.append("    ) ");
            //メイン 評定
            stb.append(",STUDYREC AS ( ");
            stb.append("    SELECT SCHREGNO,ANNUAL,");
            stb.append("    CLASSCD");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     || '-' || T1.SCHOOL_KIND ");
            }
            stb.append("    AS CLASSCD, VALUATION ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= '"+param[0]+"' AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
            if (_isGakunensei) {
                stb.append("       AND YEAR IN (SELECT DISTINCT MAX(YEAR) AS YEAR");
                stb.append("       FROM SCHREG_REGD_DAT  ");
                stb.append("       WHERE SCHREGNO = T1.SCHREGNO ");
                stb.append("       GROUP BY GRADE) ");
            }
            stb.append("    ) ");
            //教科毎評定平均値
            stb.append(",ASSESS_CLASS AS ( ");
            stb.append("    SELECT SCHREGNO,CLASSCD ");
            stb.append("    FROM   STUDYREC ");
            stb.append("    GROUP BY SCHREGNO,CLASSCD ");
            stb.append("    ) ");
            //全体の評定平均値
            stb.append(",ASSESS_ALL1 AS ( ");
            stb.append("    SELECT SCHREGNO ");
            stb.append("    FROM   STUDYREC ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    HAVING  DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) >= " + param[3] + " ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT DISTINCT K2.CLASSCD, K4.CLASSABBV ");
            stb.append("FROM   SCHNO K1 ");
            stb.append("       INNER JOIN ASSESS_ALL1 K31 ON K31.SCHREGNO = K1.SCHREGNO ");
            stb.append("       LEFT JOIN ASSESS_CLASS K2 ON K2.SCHREGNO = K1.SCHREGNO ");
            stb.append("       LEFT JOIN CLASS_MST K4 ON K4.CLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     || '-' || K4.SCHOOL_KIND ");
            }
            stb.append("        = K2.CLASSCD ");
            stb.append("ORDER BY K2.CLASSCD ");
        } catch( Exception e ){
            log.warn("psClassAbbv error!",e);
        }
        return stb.toString();

    }//psClassAbbv()の括り


    /**
     *  生徒毎の評定平均値を抽出
     *
     */
    private String psSchnoAssess(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT T1.SCHREGNO, T1.HR_CLASS, T1.ATTENDNO, T2.HR_NAME ");
            stb.append("    FROM   SCHREG_REGD_DAT T1");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND ");
            stb.append("           T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("           T2.GRADE = T1.GRADE AND ");
            stb.append("           T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("    WHERE  T1.YEAR = '" + param[0] + "' AND T1.SEMESTER = '" + param[1] + "' AND ");
            stb.append("           T1.GRADE = '" + param[2] + "') ");
            stb.append(" , REGD AS ( ");
            stb.append("    SELECT DISTINCT T1.YEAR, T1.SCHREGNO, T1.GRADE ");
            stb.append("    FROM   SCHREG_REGD_DAT T1");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT T2 ON ");
            stb.append("        T2.YEAR = T1.YEAR AND T2.SCHOOL_KIND = 'H' ");
            stb.append("    WHERE  T1.YEAR <= '" + param[0] + "' AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append("           T1.SCHREGNO NOT IN (SELECT DISTINCT T1.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  SCHREG_ATTENDREC_DAT T1 ");
            stb.append("                  LEFT JOIN SCHOOL_MST T3 ON ");
            stb.append("                      T3.YEAR = T1.YEAR     ");
            stb.append("              GROUP BY ");
            stb.append("                  T1.YEAR, T1.SCHREGNO ");
            stb.append("              HAVING ");
            stb.append("                  SUM(VALUE(SICK, 0) + VALUE(ACCIDENTNOTICE, 0) + VALUE(NOACCIDENTNOTICE, 0) ");
            stb.append("                      + (CASE WHEN T3.SEM_OFFDAYS = '1' THEN VALUE(OFFDAYS, 0) ELSE 0 END) ");
            stb.append("                  ) >= " + param[4] + ") AND ");
            // 進学基準で不合格でないもの
            stb.append("           T1.SCHREGNO NOT IN (SELECT DISTINCT T1.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  ATTEND_SEMES_DAT T1 ");
            stb.append("              GROUP BY ");
            stb.append("                  T1.YEAR, T1.SCHREGNO ");
            stb.append("              HAVING ");
            stb.append("                  SUM(VALUE(KEKKA, 0)) >= 15 ");
            stb.append("                  OR SUM(VALUE(LATEDETAIL, 0)) >= 15 ");
            stb.append("                  OR SUM(VALUE(KEKKA, 0)) + SUM(VALUE(LATEDETAIL, 0)) >= 23 ");
            stb.append("                  ) ");
            stb.append("    ) ");
            //メイン 評定
            stb.append(",STUDYREC AS ( ");
            stb.append("    SELECT SCHREGNO,ANNUAL,");
            stb.append("    CLASSCD");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     || '-' || T1.SCHOOL_KIND ");
            }
            stb.append("    AS CLASSCD, VALUATION ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= '"+param[0]+"' AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
            if (_isGakunensei) {
                stb.append("       AND YEAR IN (SELECT DISTINCT MAX(YEAR) AS YEAR");
                stb.append("       FROM SCHREG_REGD_DAT  ");
                stb.append("       WHERE SCHREGNO = T1.SCHREGNO ");
                stb.append("       GROUP BY GRADE) ");
            }
            stb.append("    ) ");
            //教科毎評定平均値
            stb.append(",ASSESS_CLASS AS ( ");
            stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
            stb.append("           ,SCHREGNO,CLASSCD ");
            stb.append("    FROM   STUDYREC ");
            stb.append("    GROUP BY SCHREGNO,CLASSCD ");
            stb.append("    ) ");
            //全体の評定平均値
            stb.append(",ASSESS_ALL1 AS ( ");
            stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
            stb.append("           ,SCHREGNO ");
            stb.append("           ,MAX(ANNUAL) AS GRADE_MAX ");
            stb.append("    FROM   STUDYREC ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    HAVING  DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) >= " + param[3] + " ");
            stb.append("    ) ");
            //全体の評定平均値・段階
            stb.append(",ASSESS_ALL2 AS ( ");
            stb.append("    SELECT SCHREGNO, ASSESSMARK, ");
            stb.append("           CASE WHEN GRADE_MAX < '"+param[2]+"' THEN '＊' ELSE '' END AS BIKOU ");//備考は、使用していない。
            stb.append("    FROM   ASSESS_ALL1 W1 ");
            stb.append("           LEFT JOIN ASSESS_MST W2 ON W2.ASSESSCD='4' AND ");
            stb.append("                                      VAL BETWEEN ASSESSLOW AND ASSESSHIGH ");
            stb.append("    ) ");
            //備考
            stb.append(",STUDYREC_BIKOU AS ( ");
            stb.append("    SELECT SCHREGNO,'＊' AS BIKOU ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("    WHERE  CLASSCD < '90' AND YEAR  = '"+param[0]+"' AND VALUE(VALUATION,0) < 1 AND ");
            stb.append("           ANNUAL = '"+param[2]+"' AND  ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT K1.HR_NAME, K1.SCHREGNO, K1.ATTENDNO, K4.NAME, ");
            stb.append("       K2.CLASSCD, K2.VAL, ");
            stb.append("       K31.VAL AS ALL_VAL, K32.ASSESSMARK, ");
            stb.append("       CASE WHEN K4.GRD_DIV = '2' OR K4.GRD_DIV = '3' THEN '＊' ");
            stb.append("            WHEN '3' < VALUE(K4.GRD_DIV,'4') THEN K5.BIKOU ");
            stb.append("            ELSE NULL END AS BIKOU ");
            stb.append("FROM   SCHNO K1 ");
            stb.append("       INNER JOIN ASSESS_ALL1 K31 ON K31.SCHREGNO=K1.SCHREGNO ");
            stb.append("       LEFT JOIN ASSESS_CLASS K2 ON K2.SCHREGNO=K1.SCHREGNO ");
            stb.append("       LEFT JOIN ASSESS_ALL2 K32 ON K32.SCHREGNO=K1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST K4 ON K4.SCHREGNO=K1.SCHREGNO ");
            stb.append("       LEFT JOIN STUDYREC_BIKOU K5 ON K5.SCHREGNO=K1.SCHREGNO ");
            stb.append("ORDER BY K1.HR_CLASS, K1.ATTENDNO ");
        } catch( Exception e ){
            log.warn("psSchnoAssess error!",e);
        }
        return stb.toString();

    }//psSchnoAssess()の括り



}//クラスの括り
