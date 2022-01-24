// kanji=漢字
/*
 * $Id: 5601043060e25d7cc40910cfe46542879a35222d $
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [進路管理]
 *
 *                  ＜ＫＮＪＥ１３０＞  評定平均値一覧表（近大）
 *
 *  2005.07.11 nakamoto 作成日
 *  2005.07.12 nakamoto 指示画面にて年組指定を追加に対応
 *                      備考は、1科目でも評定にnullがあれば「＊」を印字---仕様変更2005.07.13
 *  2005.07.13 nakamoto 備考は、今年度在籍年次に1科目でも評定にnullがあれば「＊」を印字
 *
 *  2005.09.16 nakamoto 学籍基礎マスタの卒業区分が'2,3'は、備考に「＊」を印字---仕様追加
 *                      学籍基礎マスタの卒業区分が'1,2,3'以外で、評定にnullがあれば「＊」を印字---仕様追加
 */

public class KNJE130 {


    private static final Log log = LogFactory.getLog(KNJE130.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/


    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException, SQLException
    {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        final PrintWriter outstrm = new PrintWriter (response.getOutputStream());
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
        final Param param = createParam(request, db2);

    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        printHeader(db2, svf, param);                             //見出し出力のメソッド

        printGradeAvg(db2, svf, param);                           //学年平均出力のメソッド

        //SVF出力

        if( printMain(db2, svf, param) ) nonedata = true;

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


    /** 見出し出力 **/
    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJE130.frm", 4);

    //  ＳＶＦ属性変更--->学年・組毎に改ページ
        svf.VrAttribute("HR_NAME","FF=1");

    //  年度
        try {
            svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");
        } catch( Exception e ){
            log.warn("nendo get error!",e);
        }

    //  作成日
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }

        getinfo = null;
        returnval = null;

    }//printHeader()の括り


    /** 学年平均出力 **/
    private void printGradeAvg(final DB2UDB db2, final Vrw32alp svf, final Param param){

        try {
            PreparedStatement ps = db2.prepareStatement(psGradeAvg(param));
            ResultSet rs = ps.executeQuery();

            while( rs.next() ){
                //学年平均
                svf.VrsOut("GRAD_AVERAGE",rs.getString("VAL"));
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printGradeAvg read error!",ex);
        }

    }//printGradeAvg()の括り


    /**メイン*/
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        try {
            PreparedStatement ps = db2.prepareStatement(psHrName(param));
            ResultSet rs = ps.executeQuery();

            Map hm1 = new HashMap();                                        //教科コードと列番号の保管
            while( rs.next() ){
log.debug(rs.getString("GR_CL")+" start!");
                //組名称
                svf.VrsOut("HR_NAME"  , rs.getString("HR_NAME") );

                printClassAbbv(db2,svf,param,rs.getString("GR_CL"),hm1);    //教科略称の出力メソッド

                if( printSchnoAssess(db2,svf,param,rs.getString("GR_CL"),hm1) ) nonedata = true;//評定平均値の出力メソッド

                printClear(svf);                                            //教科略称のクリアメソッド
                hm1.clear();                                                //列番号情報を削除
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り


    /**教科略称の出力*/
    private void printClassAbbv(final DB2UDB db2, final Vrw32alp svf, final Param param, final String gr_cl, final Map hm1)
    {
        try {
            PreparedStatement ps = db2.prepareStatement(psClassAbbv(param));
            int pp = 0;
            ps.setString(++pp,gr_cl);   //学年・組
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
    private boolean printSchnoAssess(final DB2UDB db2, final Vrw32alp svf, final Param param, final String gr_cl, final Map hm1)
    {
        boolean nonedata = false;
        try {
            PreparedStatement ps = db2.prepareStatement(psSchnoAssess(param));
            int pp = 0;
            ps.setString(++pp,gr_cl);   //学年・組
            ResultSet rs = ps.executeQuery();

            String schno = "0";
            while( rs.next() ){
                //学籍番号のブレイク
                if (!schno.equals("0") && !schno.equals(rs.getString("SCHREGNO"))) svf.VrEndRecord();

                svf.VrsOut("ATTENDNO"     , String.valueOf(rs.getInt("ATTENDNO")) );      //出席番号
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
            log.warn("printSchnoAssess read error!",ex);
        }
        return nonedata;

    }//printSchnoAssess()の括り


    /**教科略称のクリア*/
    private void printClear(final Vrw32alp svf)
    {
        for (int i = 1; i < 21; i++)
            svf.VrsOutn("SUBCLASS"    ,i  , "" );

    }//printClear()の括り


    /**
     *  学年平均を抽出
     *
     */
    private String psGradeAvg(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._gakki+"' AND ");
        stb.append("           GRADE = '"+param._grade+"' ");
        stb.append("    ) ");
        stb.append(",SCHREG_STUDYREC AS ( ");
        stb.append("    SELECT T1.YEAR,SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
        stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
        stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= '"+param._year+"' AND ");
        stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
        stb.append("           T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM NAME_MST IS1 WHERE NAMECD1 = 'D065') AND ");
        stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
        stb.append(        "NOT EXISTS(SELECT  'X' ");
        stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append(                   "WHERE   T2.YEAR = T1.YEAR ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("       AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(                           " AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
        stb.append("    ) ");
        //留年生は、MAX年度を取得（つまり、留年した年度は対象外）
        stb.append(", T_SCHNO AS ( ");
        stb.append("    SELECT YEAR,SCHREGNO,GRADE ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR <= '"+param._year+"' AND ");
        stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        stb.append("    GROUP BY YEAR,SCHREGNO,GRADE ");
        stb.append("    ) ");
        stb.append(", RYUNEN_SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO,GRADE ");
        stb.append("    FROM   T_SCHNO T1 ");
        stb.append("    GROUP BY SCHREGNO,GRADE ");
        stb.append("    HAVING 1 < COUNT(*) ");
        stb.append("    ) ");
        stb.append(", RYUNEN_MAX_YEAR AS ( ");
        stb.append("    SELECT MAX(YEAR) AS YEAR,SCHREGNO,GRADE ");
        stb.append("    FROM   T_SCHNO T1 ");
        stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM RYUNEN_SCHNO) ");
        stb.append("    GROUP BY SCHREGNO,GRADE ");
        stb.append("    ) ");
        stb.append(",STUDYREC AS ( ");
        stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
        stb.append("    FROM   SCHREG_STUDYREC T1 ");
        stb.append("    WHERE  SCHREGNO NOT IN (SELECT SCHREGNO FROM RYUNEN_SCHNO) ");
        stb.append("    UNION ALL ");
        stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
        stb.append("    FROM   SCHREG_STUDYREC T1 ");
        stb.append("    WHERE  EXISTS(SELECT  'X' ");
        stb.append("                  FROM    RYUNEN_MAX_YEAR T3 ");
        stb.append("                  WHERE   T3.YEAR = T1.YEAR AND ");
        stb.append("                          T3.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("                          T3.GRADE = T1.ANNUAL) ");
        stb.append("    ) ");

        //メイン
        stb.append("SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");//学年平均
        stb.append("FROM   STUDYREC T1 ");
        return stb.toString();

    }//psGradeAvg()の括り


    /**
     *  組名称をを抽出
     *
     */
    private String psHrName(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT HR_NAME,GRADE||HR_CLASS AS GR_CL ");
        stb.append("FROM   SCHREG_REGD_HDAT ");
        stb.append("WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._gakki+"' AND ");
        stb.append("       GRADE||HR_CLASS IN "+param._classSelected+" ");//---2005.07.12
//      stb.append("       GRADE = '"+param._2+"' ");
        stb.append("ORDER BY GRADE,HR_CLASS");
        return stb.toString();

    }//psHrName()の括り


    /**
     *  教科略称を抽出
     *
     */
    private String psClassAbbv(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._gakki+"' AND ");
        stb.append("           GRADE||HR_CLASS = ? ");
        stb.append("    ) ");

        //メイン
        stb.append("SELECT DISTINCT ");
        stb.append("       T1.CLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            if ("KINDAI".equals(param._schoolName)) {
                stb.append("       || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("       || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD ");
            }
        }
        stb.append("       AS CLASSCD ");
        stb.append("       ,T2.CLASSABBV ");
        stb.append("FROM   SCHREG_STUDYREC_DAT T1 ");
        stb.append("       LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        }
        stb.append("WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= '"+param._year+"' AND ");
        stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM NAME_MST IS1 WHERE NAMECD1 = 'D065') AND ");
        stb.append("       SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
        stb.append(        "NOT EXISTS(SELECT  'X' ");
        stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append(                   "WHERE   T2.YEAR = T1.YEAR ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("       AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(                           " AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
        stb.append("ORDER BY T1.CLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            if ("KINDAI".equals(param._schoolName)) {
                stb.append("       || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("       || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD ");
            }
        }
        return stb.toString();

    }//psClassAbbv()の括り


    /**
     *  生徒毎の評定平均値を抽出
     *
     */
    private String psSchnoAssess(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR='"+param._year+"' AND SEMESTER='"+param._gakki+"' AND ");
        stb.append("           GRADE||HR_CLASS = ? ");
        stb.append("    ) ");
        stb.append(",SCHREG_STUDYREC AS ( ");
        stb.append("    SELECT T1.YEAR,SCHREGNO,ANNUAL,");
        stb.append("       T1.CLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            if ("KINDAI".equals(param._schoolName)) {
                stb.append("       || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("       || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD ");
            }
        }
        stb.append("           AS CLASSCD,VALUATION ");
        stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
        stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= '"+param._year+"' AND ");
        stb.append("           T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM NAME_MST IS1 WHERE NAMECD1 = 'D065') AND ");
        stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
        stb.append(        "NOT EXISTS(SELECT  'X' ");
        stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append(                   "WHERE   T2.YEAR = T1.YEAR ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("       AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(                           " AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
        stb.append("    ) ");
        //留年生は、MAX年度を取得（つまり、留年した年度は対象外）
        stb.append(", T_SCHNO AS ( ");
        stb.append("    SELECT YEAR,SCHREGNO,GRADE ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR <= '"+param._year+"' AND ");
        stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        stb.append("    GROUP BY YEAR,SCHREGNO,GRADE ");
        stb.append("    ) ");
        stb.append(", RYUNEN_SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO,GRADE ");
        stb.append("    FROM   T_SCHNO T1 ");
        stb.append("    GROUP BY SCHREGNO,GRADE ");
        stb.append("    HAVING 1 < COUNT(*) ");
        stb.append("    ) ");
        stb.append(", RYUNEN_MAX_YEAR AS ( ");
        stb.append("    SELECT MAX(YEAR) AS YEAR,SCHREGNO,GRADE ");
        stb.append("    FROM   T_SCHNO T1 ");
        stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM RYUNEN_SCHNO) ");
        stb.append("    GROUP BY SCHREGNO,GRADE ");
        stb.append("    ) ");
        stb.append(",STUDYREC AS ( ");
        stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
        stb.append("    FROM   SCHREG_STUDYREC T1 ");
        stb.append("    WHERE  SCHREGNO NOT IN (SELECT SCHREGNO FROM RYUNEN_SCHNO) ");
        stb.append("    UNION ALL ");
        stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
        stb.append("    FROM   SCHREG_STUDYREC T1 ");
        stb.append("    WHERE  EXISTS(SELECT  'X' ");
        stb.append("                  FROM    RYUNEN_MAX_YEAR T3 ");
        stb.append("                  WHERE   T3.YEAR = T1.YEAR AND ");
        stb.append("                          T3.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("                          T3.GRADE = T1.ANNUAL) ");
        stb.append("    ) ");
        //教科毎評定平均値
        stb.append(",ASSESS_CLASS AS ( ");
        stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
        stb.append("           ,SCHREGNO,CLASSCD ");
        stb.append("    FROM   STUDYREC ");
        stb.append("    GROUP BY SCHREGNO,CLASSCD ");
        stb.append("    ) ");
        //全体の評定平均値
        stb.append(",ASSESS_ALL AS ( ");
        stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
        stb.append("           ,SCHREGNO ");
        stb.append("           ,MAX(ANNUAL) AS GRADE_MAX ");
        stb.append("    FROM   STUDYREC ");
        stb.append("    GROUP BY SCHREGNO ");
        stb.append("    ) ");
        //全体の評定平均値・段階
        stb.append(",ASSESS_ALL2 AS ( ");
        stb.append("    SELECT SCHREGNO,VAL,ASSESSMARK, ");
        stb.append("           CASE WHEN GRADE_MAX < '"+param._grade+"' THEN '＊' ELSE '' END AS BIKOU ");//備考は、使用していない。---2005.07.12
        stb.append("    FROM   ASSESS_ALL W1 ");
        stb.append("           LEFT JOIN ASSESS_MST W2 ON W2.ASSESSCD='4' AND ");
        stb.append("                                      VAL BETWEEN ASSESSLOW AND ASSESSHIGH ");
        stb.append("    ) ");
        //備考---2005.07.12Add
        stb.append(",STUDYREC_BIKOU AS ( ");
        stb.append("    SELECT SCHREGNO,'＊' AS BIKOU ");
        stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
//      stb.append("    WHERE  CLASSCD < '90' AND YEAR <= '"+param._0+"' AND VALUE(VALUATION,0) < 1 AND ");
        stb.append("    WHERE  CLASSCD < '90' AND YEAR  = '"+param._year+"' AND VALUE(VALUATION,0) < 1 AND ");
        stb.append("           T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM NAME_MST IS1 WHERE NAMECD1 = 'D065') AND ");
        stb.append("           ANNUAL = '"+param._grade+"' AND  ");//---2005.07.13
        stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND  ");
        stb.append("            NOT EXISTS(SELECT  'X' ");
        stb.append("                       FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append("                       WHERE   T2.YEAR = T1.YEAR ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("       AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("                               AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
        stb.append("    GROUP BY SCHREGNO ");
        stb.append("    ) ");

        //メイン
        stb.append("SELECT K1.SCHREGNO,K1.ATTENDNO,K4.NAME, ");
        stb.append("       K2.CLASSCD,K2.VAL, ");
        stb.append("       K3.VAL AS ALL_VAL,K3.ASSESSMARK, ");
        //---2005.09.16Modify
        stb.append("       CASE WHEN K4.GRD_DIV = '2' OR K4.GRD_DIV = '3' THEN '＊' ");
        stb.append("            WHEN '3' < VALUE(K4.GRD_DIV,'4') THEN K5.BIKOU ");
        stb.append("            ELSE NULL END AS BIKOU ");
        //stb.append("       K5.BIKOU ");//備考(K3.BIKOU⇒K5.BIKOU)---2005.07.12Modify
        stb.append("FROM   SCHNO K1 ");
        stb.append("       LEFT JOIN ASSESS_CLASS K2 ON K2.SCHREGNO=K1.SCHREGNO ");
        stb.append("       LEFT JOIN ASSESS_ALL2 K3 ON K3.SCHREGNO=K1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_BASE_MST K4 ON K4.SCHREGNO=K1.SCHREGNO ");
        stb.append("       LEFT JOIN STUDYREC_BIKOU K5 ON K5.SCHREGNO=K1.SCHREGNO ");
        stb.append("ORDER BY K1.ATTENDNO ");
        return stb.toString();

    }//psSchnoAssess()の括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final String _grade;
        final String _classSelected;
        final String _useCurriculumcd;
        final String _schoolName;

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期 1,2,3
            _grade = request.getParameter("GRADE");                       //学年
            _schoolName = getZ010(db2);
            final String classcd[] = request.getParameterValues("CLASS_SELECTED");//年組---2005.07.12
            String stb = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) stb = stb + ",";
                stb = stb + "'" + classcd[ia] + "'";
            }
            stb = stb + ")";
            _classSelected = stb;
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getZ010(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs  = ps.executeQuery();
                rs.next();
                retStr = rs.getString("NAME1");
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                log.debug("exception!", e);
            }
            return retStr;
        }
    }

}//クラスの括り
