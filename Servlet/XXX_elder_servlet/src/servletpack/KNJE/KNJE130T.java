// kanji=漢字
/*
 * $Id: 573ae520a09512c6399eb3ae8d5aeef5aa6251d2 $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [進路管理]
 *
 *                  ＜ＫＮＪＥ１３０Ｔ＞  評価・評定平均値一覧表（新賢者）
 *
 *  2009.09.20 作成日
 */

public class KNJE130T {


    private static final Log log = LogFactory.getLog(KNJE130T.class);

    private Param param;
    private boolean hasdata;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        PrintWriter outstrm = null;

        try {
            //  print設定
            outstrm = new PrintWriter (response.getOutputStream());
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrInit();                           //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

            //  ＤＢ接続
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
                db2.open();
            } catch (Exception ex) {
                log.error("DB2 open error!", ex);
                return;
            }

            final Param param = createParam(request, db2);

            //SVF出力
            printMain(db2, svf, param);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {

            log.debug("nonedata = " + hasdata);

            //  該当データ無し
            if (!hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            try {
                outstrm.close();            //ストリームを閉じる
            } catch(Exception e) {
            }
        }

    }//doGetの括り

    /**メイン*/
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        final String gradeAvg = getGradeAvg(db2, param);

        final List grClList = getGrClList(db2, param);

        for (final Iterator it = grClList.iterator(); it.hasNext();) {
            final Map grClMap = (Map) it.next();

            final String grCl = (String) grClMap.get("GR_CL");
            log.debug(grCl + " start!");

            svf.VrSetForm("KNJE130T.frm", 4);

            svf.VrAttribute("HR_NAME","FF=1"); //  ＳＶＦ属性変更--->学年・組毎に改ページ
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度"); //  年度
            svf.VrsOut("DATE", param._ctrlDate); //  作成日
            svf.VrsOut("SELECT", ("1".equals(param._outDiv)) ? "評価" : "評定"); //タイトル 1:学年評価 2:評定

            //学年平均
            svf.VrsOut("GRAD_AVERAGE", gradeAvg); //学年平均出力のメソッド

            //組名称
            svf.VrsOut("HR_NAME", (String) grClMap.get("HR_NAME"));

            final List hm1List = getClassAbbv(db2, param, grCl);    //教科略称の出力メソッド

            final List studentList = getStudentList(db2, param, grCl);

            printSchnoAssess(svf, studentList, hm1List);

            for (int i = 1; i < 21; i++) {
                svf.VrsOutn("SUBCLASS", i, "");
            }
        }
    }

    /** 学年平均出力 **/
    private String getGradeAvg(final DB2UDB db2, final Param param) {
        String val = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(psGradeAvg(param));
            rs = ps.executeQuery();

            while (rs.next()) {
                val = rs.getString("VAL");
            }
        } catch (Exception ex) {
            log.warn("printGradeAvg read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return val;

    }//printGradeAvg()の括り

    private List getGrClList(final DB2UDB db2, final Param param) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List grClList = new ArrayList();
        try {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT HR_NAME, GRADE || HR_CLASS AS GR_CL ");
            stb.append("FROM   SCHREG_REGD_HDAT ");
            stb.append("WHERE  YEAR = '" + param._year + "' AND SEMESTER = '" + param._gakki + "' AND ");
            stb.append("       GRADE || HR_CLASS IN " + param._classSelected + " ");
            stb.append("ORDER BY GRADE, HR_CLASS");

            final String sql = stb.toString();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Map grClMap = new HashMap();

                final String grCl = rs.getString("GR_CL");
                grClMap.put("GR_CL", grCl);
                grClMap.put("HR_NAME", rs.getString("HR_NAME"));
                grClList.add(grClMap);
            }
        } catch (Exception ex) {
            log.warn("printMain read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return grClList;
    }


    /**教科略称の出力*/
    private List getClassAbbv(final DB2UDB db2, final Param param, final String gr_cl) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List hm1List = new ArrayList();
        try {
            final String sql = psClassAbbv(param);
            ps = db2.prepareStatement(sql);
            int pp = 0;
            ps.setString(++pp, gr_cl);   //学年・組
            rs = ps.executeQuery();

            int classno = 0;
            while (rs.next()) {
                final Map hm1 = new HashMap();
                hm1.put("CLASSNO", new Integer(++classno));
                //列番号に教科コードを付ける
                hm1.put("CLASSCD", rs.getString("CLASSCD"));

                hm1.put("CLASSABBV", rs.getString("CLASSABBV"));

                //教科略称
                hm1List.add(hm1);

            }
        } catch (Exception ex) {
            log.warn("printClassAbbv read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return hm1List;

    }//printClassAbbv()の括り


    /**生徒毎の評定平均値の出力*/
    private void printSchnoAssess(final Vrw32alp svf, final List studentList, final List hm1List) {

        for (int i = 0; i < hm1List.size(); i++) {
            final Map hm1 = (Map) hm1List.get(i);
            final int classno = ((Integer) hm1.get("CLASSNO")).intValue();
            final String classabbv = (String) hm1.get("CLASSABBV");
            svf.VrsOutn("SUBCLASS", classno, classabbv);
        }

        for (int j = 0; j < studentList.size(); j++) {
            final Map student = (Map) studentList.get(j);

            svf.VrsOut("ATTENDNO"     , (String) student.get("ATTENDNO"));      //出席番号
            svf.VrsOut("NAME"         , (String) student.get("NAME"));                       //氏名
            svf.VrsOut("TOTAL_AVERAGE", (String) student.get("ALL_VAL"));                    //全体平均
            svf.VrsOut("RANK"         , (String) student.get("ASSESSMARK"));                 //段階
            svf.VrsOut("NOTE"         , (String) student.get("BIKOU"));                      //備考

            final Map valmap = (Map) student.get("VALMAP");

            for (int i = 0; i < hm1List.size(); i++) {
                final Map hm1 = (Map) hm1List.get(i);
                final int classno = ((Integer) hm1.get("CLASSNO")).intValue();
                final String val = (String) valmap.get(hm1.get("CLASSCD"));

                svf.VrsOutn("AVERAGE", classno, val);  //各教科の評定平均値
            }
            svf.VrEndRecord();
            hasdata = true;
        }

    }//printSchnoAssess()の括り


    private List getStudentList(final DB2UDB db2, final Param param, final String gr_cl) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = psSchnoAssess(param);
            log.debug(" student sql = " + sql);
            ps = db2.prepareStatement(sql);
            int pp = 0;
            ps.setString(++pp, gr_cl);   //学年・組
            rs = ps.executeQuery();

            String schno = "0";
            Map student = null;
            while (rs.next()) {
                //学籍番号のブレイク
                if (null == student || !schno.equals(rs.getString("SCHREGNO"))) {
                    student = new HashMap();
                    student.put("ATTENDNO", String.valueOf(rs.getInt("ATTENDNO")));
                    student.put("NAME", rs.getString("NAME"));
                    student.put("ALL_VAL", rs.getString("ALL_VAL"));
                    student.put("ASSESSMARK", rs.getString("ASSESSMARK"));
                    student.put("BIKOU", rs.getString("BIKOU"));
                    studentList.add(student);
                }

                schno = rs.getString("SCHREGNO");

                if (null == student.get("VALMAP")) {
                    student.put("VALMAP", new HashMap());
                }
                final Map valmap = (Map) student.get("VALMAP");
                valmap.put(rs.getString("CLASSCD"), rs.getString("VAL"));
            }
        } catch (Exception ex) {
            log.warn("printSchnoAssess read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }


    /**
     *  学年平均を抽出
     *
     */
    private String psGradeAvg(final Param param) {

        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR = '" + param._year + "' AND SEMESTER = '" + param._gakki + "' AND ");
        stb.append("           GRADE = '" + param._grade + "' ");
        stb.append("   ) ");

        //メイン 1:学年評価 2:評定
        if ("1".equals(param._outDiv)) {
            stb.append("SELECT ");
            if ("1".equals(param._hyoteiYomikae)) {
                if ("1".equals(param._useProvFlg)) {
                    stb.append(" DECIMAL(ROUND(AVG(FLOAT(CASE WHEN 1 = T1.SCORE AND PROV.PROV_FLG = '1' THEN 2 ELSE T1.SCORE END))*10,0)/10,4,1) AS VAL ");//学年平均
                } else {
                    stb.append(" DECIMAL(ROUND(AVG(FLOAT(CASE WHEN 1 = T1.SCORE THEN 2 ELSE T1.SCORE END))*10,0)/10,4,1) AS VAL ");//学年平均
                }
            } else {
                stb.append(" DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*10,0)/10,4,1) AS VAL ");//学年平均
            }
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("FROM   RECORD_RANK_SDIV_DAT T1 ");
            } else {
                stb.append("FROM   RECORD_RANK_DAT T1 ");
            }
            stb.append(" INNER JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" 								AND T2.SEMESTER 		= T1.SEMESTER ");
            stb.append(" 								AND T2.TESTKINDCD 		= T1.TESTKINDCD ");
            stb.append(" 								AND T2.TESTITEMCD 		= T1.TESTITEMCD ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
            	stb.append(" 								AND T2.SCORE_DIV 		= T1.SCORE_DIV ");
            }
            stb.append(" 								AND T2.CLASSCD 			= T1.CLASSCD ");
            stb.append(" 								AND T2.SCHOOL_KIND 		= T1.SCHOOL_KIND ");
            stb.append(" 								AND T2.CURRICULUM_CD 	= T1.CURRICULUM_CD ");
            stb.append(" 								AND T2.SUBCLASSCD 		= T1.SUBCLASSCD ");
            stb.append(" 								AND T2.SCHREGNO 		= T1.SCHREGNO ");
            if ("1".equals(param._hyoteiYomikae) && "1".equals(param._useProvFlg)) {
                stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
                stb.append("          AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND PROV.CLASSCD = T1.CLASSCD ");
                    stb.append("       AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("       AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("          AND PROV.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append("WHERE  T1.YEAR = '" + param._year + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ");
            } else {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ");
            }
            stb.append("  AND  substr(T1.SUBCLASSCD,1,2) < '90' ");
            stb.append("  AND  0 < T1.SCORE ");
            stb.append("  AND  T1.SCHREGNO IN (SELECT W1.SCHREGNO FROM SCHNO W1) ");
            stb.append("  AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T1.SUBCLASSCD NOT IN (SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     R1.ATTEND_CLASSCD || '-' || R1.ATTEND_SCHOOL_KIND || '-' || R1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("     R1.ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT R1 WHERE R1.REPLACECD = '1' AND R1.YEAR = '" + param._year + "') ");
            stb.append(" AND T1.SUBCLASSCD NOT IN ('333333', '555555', '777777', '888888') AND T1.SUBCLASSCD NOT LIKE '99999%' ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            if (!"1".equals(param._includeMirisyuuFlg)) {
            	stb.append("  AND  VALUE(T2.COMP_CREDIT, 0) <> 0 ");
            }
        } else {
            stb.append("SELECT ");
            if ("1".equals(param._hyoteiYomikae)) {
                if ("1".equals(param._useProvFlg)) {
                    stb.append("  DECIMAL(ROUND(AVG(FLOAT(CASE WHEN 1 = VALUATION AND PROV.PROV_FLG = '1' THEN 2 ELSE VALUATION END))*10,0)/10,4,1) AS VAL ");
                } else {
                    stb.append("  DECIMAL(ROUND(AVG(FLOAT(CASE WHEN 1 = VALUATION THEN 2 ELSE VALUATION END))*10,0)/10,4,1) AS VAL ");
                }
            } else {
                stb.append("  DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
            }
            stb.append("FROM   SCHREG_STUDYREC_DAT T1 ");
            if ("1".equals(param._hyoteiYomikae) && "1".equals(param._useProvFlg)) {
                stb.append("    LEFT JOIN STUDYREC_PROV_FLG_DAT PROV ON PROV.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("          AND PROV.YEAR = T1.YEAR ");
                stb.append("          AND PROV.SCHREGNO = T1.SCHREGNO ");
                stb.append("          AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND PROV.CLASSCD = T1.CLASSCD ");
                    stb.append("       AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("       AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
            }
            stb.append("WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND T1.YEAR <= '" + param._year + "' ");
            if (!"1".equals(param._includeMirisyuuFlg)) {
            	stb.append("    	AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
            }
            if (param._isGradeH) {
                stb.append("    AND NOT EXISTS (SELECT 'X' FROM NAME_MST N1 WHERE N1.NAMECD1 = 'A023' AND N1.NAME1 = 'J' AND T1.ANNUAL BETWEEN N1.NAME2 AND N1.NAME3) ");
            }
            if (param._isGakunensei) {
                stb.append("    AND (T1.SCHREGNO, T1.YEAR) IN ( ");
                stb.append("           SELECT T2.SCHREGNO, MAX(T2.YEAR) AS YEAR");
                stb.append("           FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("           WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL <> '00'");
                stb.append("           GROUP BY T2.SCHREGNO, ANNUAL ");
                stb.append("        UNION ");
                stb.append("           SELECT T2.SCHREGNO, T2.YEAR ");
                stb.append("           FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("           WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL = '00'");
                stb.append("       ) ");
            } else {
                stb.append("    AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            }
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
        }
        return stb.toString();

    }//psGradeAvg()の括り

    /**
     *  教科略称を抽出
     *
     */
    private String psClassAbbv(final Param param) {

        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR = '" + param._year + "' AND SEMESTER = '" + param._gakki + "' AND ");
        stb.append("           GRADE || HR_CLASS = ? ");
        stb.append("   ) ");

        //メイン 1:学年評価 2:評定
        if ("1".equals(param._outDiv)) {
            stb.append("SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(" substr(T1.SUBCLASSCD,1,2) ");
            }
            stb.append(" as CLASSCD, T2.CLASSABBV ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("FROM   RECORD_RANK_SDIV_DAT T1 ");
            } else {
                stb.append("FROM   RECORD_RANK_DAT T1 ");
            }
            stb.append("       LEFT JOIN CLASS_MST T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("     T2.CLASSCD = substr(T1.SUBCLASSCD,1,2) ");
            }
            stb.append("WHERE  T1.YEAR = '" + param._year + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ");
            } else {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T1.CLASSCD < '90' ");
            } else {
                stb.append("  AND substr(T1.SUBCLASSCD,1,2) < '90' ");
            }
            stb.append("  AND  0 < T1.SCORE ");
            stb.append("  AND  T1.SCHREGNO IN (SELECT W1.SCHREGNO FROM SCHNO W1) ");
            stb.append("  AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T1.SUBCLASSCD NOT IN (SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     R1.ATTEND_CLASSCD || '-' || R1.ATTEND_SCHOOL_KIND || '-' || R1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("     R1.ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT R1 WHERE R1.REPLACECD = '1' AND R1.YEAR = '" + param._year + "') ");
            stb.append(" AND T1.SUBCLASSCD NOT IN ('333333', '555555', '777777', '888888') AND T1.SUBCLASSCD NOT LIKE '99999%' ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append("ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("  substr(T1.SUBCLASSCD,1,2) ");
            }
        } else {
            stb.append("SELECT DISTINCT ");
            stb.append("       T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       || '-' || T1.SCHOOL_KIND ");
            }
            stb.append("       AS CLASSCD ");
            stb.append("       ,T2.CLASSABBV ");
            stb.append("FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("       LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append("WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND T1.YEAR <= '" + param._year + "' ");
            if (param._isGradeH) {
                stb.append("    AND NOT EXISTS (SELECT 'X' FROM NAME_MST N1 WHERE N1.NAMECD1 = 'A023' AND N1.NAME1 = 'J' AND T1.ANNUAL BETWEEN N1.NAME2 AND N1.NAME3) ");
            }
            if (param._isGakunensei) {
                stb.append("    AND (SCHREGNO, YEAR) IN ( ");
                stb.append("           SELECT T2.SCHREGNO, MAX(T2.YEAR) AS YEAR");
                stb.append("           FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("           WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL <> '00'");
                stb.append("           GROUP BY T2.SCHREGNO, ANNUAL ");
                stb.append("        UNION ");
                stb.append("           SELECT T2.SCHREGNO, T2.YEAR ");
                stb.append("           FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("           WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL = '00'");
                stb.append("       ) ");
            } else {
                stb.append("    AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            }
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append("ORDER BY T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       || '-' || T1.SCHOOL_KIND ");
            }
        }
        return stb.toString();

    }//psClassAbbv()の括り


    /**
     *  生徒毎の評定平均値を抽出
     *
     */
    private String psSchnoAssess(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //在籍
        stb.append("WITH SCHNO AS ( ");
        stb.append("    SELECT SCHREGNO, GRADE, HR_CLASS, ATTENDNO, ANNUAL, COURSECD, MAJORCD, COURSECODE ");
        stb.append("    FROM   SCHREG_REGD_DAT ");
        stb.append("    WHERE  YEAR = '" + param._year + "' AND SEMESTER = '" + param._gakki + "' AND ");
        stb.append("           GRADE || HR_CLASS = ? ");
        stb.append("   ) ");
        //メイン 1:学年評価 2:評定
        if ("1".equals(param._outDiv)) {
            stb.append(",STUDYREC AS ( ");
            stb.append("SELECT T1.SCHREGNO, L1.ANNUAL, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(" substr(T1.SUBCLASSCD,1,2) ");
            }
            stb.append("  as CLASSCD, ");
            if ("1".equals(param._hyoteiYomikae)) {
                if ("1".equals(param._useProvFlg)) {
                    stb.append("  CASE WHEN 1 = T1.SCORE AND PROV.PROV_FLG = '1' THEN 2 ELSE T1.SCORE END as VALUATION ");
                } else {
                    stb.append("  CASE WHEN 1 = T1.SCORE THEN 2 ELSE T1.SCORE END as VALUATION ");
                }
            } else {
                stb.append("  T1.SCORE as VALUATION ");
            }
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("FROM   RECORD_RANK_SDIV_DAT T1 ");
            } else {
                stb.append("FROM   RECORD_RANK_DAT T1 ");
            }
            stb.append(" INNER JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" 								AND T2.SEMESTER 		= T1.SEMESTER ");
            stb.append(" 								AND T2.TESTKINDCD 		= T1.TESTKINDCD ");
            stb.append(" 								AND T2.TESTITEMCD 		= T1.TESTITEMCD ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
            	stb.append(" 								AND T2.SCORE_DIV 		= T1.SCORE_DIV ");
            }
            stb.append(" 								AND T2.CLASSCD 			= T1.CLASSCD ");
            stb.append(" 								AND T2.SCHOOL_KIND 		= T1.SCHOOL_KIND ");
            stb.append(" 								AND T2.CURRICULUM_CD 	= T1.CURRICULUM_CD ");
            stb.append(" 								AND T2.SUBCLASSCD 		= T1.SUBCLASSCD ");
            stb.append(" 								AND T2.SCHREGNO 		= T1.SCHREGNO ");
            if ("1".equals(param._hyoteiYomikae) && "1".equals(param._useProvFlg)) {
                stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
                stb.append("          AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND PROV.CLASSCD = T1.CLASSCD ");
                    stb.append("       AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("       AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("          AND PROV.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append("       LEFT JOIN SCHNO L1 ON L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE  T1.YEAR = '" + param._year + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ");
            } else {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T1.CLASSCD < '90' ");
            } else {
                stb.append("  AND  substr(T1.SUBCLASSCD,1,2) < '90' ");
            }
            stb.append("  AND  0 < T1.SCORE ");
            stb.append("  AND  T1.SCHREGNO IN (SELECT W1.SCHREGNO FROM SCHNO W1) ");
            stb.append("  AND  T1.SUBCLASSCD NOT IN (SELECT R1.ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT R1 WHERE R1.REPLACECD = '1' AND R1.YEAR = '" + param._year + "') ");
            stb.append("  AND  T1.SUBCLASSCD NOT IN ('333333', '555555', '777777', '888888') AND T1.SUBCLASSCD NOT LIKE '99999%' ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            if (!"1".equals(param._includeMirisyuuFlg)) {
            	stb.append("  AND  VALUE(T2.COMP_CREDIT, 0) <> 0 ");
            }
        } else {
            stb.append(",STUDYREC0 AS ( ");
            stb.append("    SELECT T1.SCHREGNO, ");
            stb.append("           T1.YEAR,");
            stb.append("           T1.ANNUAL,");
            stb.append("           T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" || '-' || T1.SCHOOL_KIND ");
            }
            stb.append("           AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
            if ("1".equals(param._hyoteiYomikae)) {
                if ("1".equals(param._useProvFlg)) {
                    stb.append("  CASE WHEN 1 = T1.VALUATION AND PROV.PROV_FLG = '1' THEN 2 ELSE T1.VALUATION END as VALUATION, ");
                } else {
                    stb.append("  CASE WHEN 1 = T1.VALUATION THEN 2 ELSE T1.VALUATION END as VALUATION, ");
                }
            } else {
                stb.append("  T1.VALUATION as VALUATION, ");
            }
            stb.append("     CASE WHEN ADD_CREDIT IS NOT NULL OR GET_CREDIT IS NOT NULL THEN VALUE(ADD_CREDIT, 0) + VALUE(GET_CREDIT, 0) END AS CREDIT ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            if ("1".equals(param._hyoteiYomikae) && "1".equals(param._useProvFlg)) {
                stb.append("    LEFT JOIN STUDYREC_PROV_FLG_DAT PROV ON PROV.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("          AND PROV.YEAR = T1.YEAR ");
                stb.append("          AND PROV.SCHREGNO = T1.SCHREGNO ");
                stb.append("          AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       AND PROV.CLASSCD = T1.CLASSCD ");
                    stb.append("       AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("       AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
            }
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND T1.YEAR <= '" + param._year + "' ");
            if (!"1".equals(param._includeMirisyuuFlg)) {
            	stb.append("    	AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
            }
            if (param._isGradeH) {
                stb.append("    AND NOT EXISTS (SELECT 'X' FROM NAME_MST N1 WHERE N1.NAMECD1 = 'A023' AND N1.NAME1 = 'J' AND T1.ANNUAL BETWEEN N1.NAME2 AND N1.NAME3) ");
            }
            if (param._isGakunensei) {
                stb.append("    AND (T1.SCHREGNO, T1.YEAR) IN ( ");
                stb.append("           SELECT T2.SCHREGNO, MAX(T2.YEAR) AS YEAR");
                stb.append("           FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("           WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL <> '00'");
                stb.append("           GROUP BY T2.SCHREGNO, ANNUAL ");
                stb.append("        UNION ");
                stb.append("           SELECT T2.SCHREGNO, T2.YEAR ");
                stb.append("           FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("           WHERE T2.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ANNUAL = '00'");
                stb.append("       ) ");
            } else {
                stb.append("    AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            }
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append("   ) ");
            stb.append(", STUDYREC AS ( ");
            stb.append("    SELECT SCHREGNO, CASE WHEN ANNUAL = '00' THEN T1.YEAR ELSE ANNUAL END AS ANNUAL, CLASSCD, ");
            stb.append("      CASE WHEN COUNT(*) = 1 THEN MAX(VALUATION) ");
            stb.append("           WHEN GVAL_CALC = '0' THEN ROUND(AVG(FLOAT(CASE WHEN 0 < VALUATION THEN VALUATION END)),0) ");
            stb.append("           WHEN GVAL_CALC = '1' AND 0 < SUM(CASE WHEN 0 < VALUATION THEN CREDIT END) THEN ROUND(FLOAT(SUM((CASE WHEN 0 < VALUATION THEN VALUATION END)*CREDIT))/SUM(CASE WHEN 0 < VALUATION THEN CREDIT END),0) ");
            stb.append("           ELSE MAX(VALUATION) END AS VALUATION ");
            stb.append("    FROM   STUDYREC0 T1 ");
            stb.append("    LEFT JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ");
            if ("1".equals(param._useSchool_KindField)) {
                stb.append(" AND T2.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append("    GROUP BY SCHREGNO, CASE WHEN ANNUAL = '00' THEN T1.YEAR ELSE ANNUAL END, T1.CLASSCD, T1.SUBCLASSCD, GVAL_CALC ");
        }
        stb.append("   ) ");
        //教科毎評定平均値
        stb.append(",ASSESS_CLASS AS ( ");
        stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
        stb.append("           ,SCHREGNO,CLASSCD ");
        stb.append("    FROM   STUDYREC ");
        stb.append("    GROUP BY SCHREGNO,CLASSCD ");
        stb.append("   ) ");
        //全体の評定平均値
        stb.append(",ASSESS_ALL AS ( ");
        stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
        stb.append("           ,SCHREGNO ");
        stb.append("           ,MAX(ANNUAL) AS GRADE_MAX ");
        stb.append("    FROM   STUDYREC ");
        stb.append("    GROUP BY SCHREGNO ");
        stb.append("   ) ");
        //全体の評定平均値・段階
        stb.append(",ASSESS_ALL2 AS ( ");
        stb.append("    SELECT W1.SCHREGNO, W1.VAL, W2.ASSESSMARK, ");
        stb.append("           CASE WHEN GRADE_MAX < '" + param._grade + "' THEN '＊' ELSE '' END AS BIKOU ");//備考は、使用していない。
        stb.append("    FROM   ASSESS_ALL W1 ");
        stb.append("    LEFT JOIN SCHNO L1 ON L1.SCHREGNO = W1.SCHREGNO ");
        if ("1".equals(param._useAssessCourseMst)) {
            stb.append("           LEFT JOIN ASSESS_COURSE_MST W2 ON W2.ASSESSCD = '4' ");
            stb.append("                                      AND W2.COURSECD = L1.COURSECD ");
            stb.append("                                      AND W2.MAJORCD = L1.MAJORCD ");
            stb.append("                                      AND W2.COURSECODE = L1.COURSECODE ");
            stb.append("                                      AND W1.VAL BETWEEN ASSESSLOW AND ASSESSHIGH ");
        } else {
            stb.append("           LEFT JOIN ASSESS_MST W2 ON W2.ASSESSCD = '4' ");
            stb.append("                                      AND W1.VAL BETWEEN ASSESSLOW AND ASSESSHIGH ");
        }
        stb.append("   ) ");
        //備考
        stb.append(",STUDYREC_BIKOU AS ( ");
        //メイン 1:学年評価 2:評定
        if ("1".equals(param._outDiv)) {
            stb.append("SELECT T1.SCHREGNO, '＊' as BIKOU ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("FROM   RECORD_RANK_SDIV_DAT T1 ");
            } else {
                stb.append("FROM   RECORD_RANK_DAT T1 ");
            }
            stb.append("WHERE  T1.YEAR = '" + param._year + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ");
            } else {
                stb.append("  AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T1.CLASSCD < '90' ");
            } else {
                stb.append("  AND substr(T1.SUBCLASSCD,1,2) < '90' ");
            }
            stb.append("  AND  value(T1.SCORE,0) < 1 ");
            stb.append("  AND  T1.SCHREGNO IN (SELECT W1.SCHREGNO FROM SCHNO W1) ");
            stb.append("  AND  T1.SUBCLASSCD NOT IN (SELECT R1.ATTEND_SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT R1 WHERE R1.REPLACECD = '1' AND R1.YEAR = '" + param._year + "') ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append("GROUP BY T1.SCHREGNO ");
        } else {
            stb.append("    SELECT SCHREGNO,'＊' AS BIKOU ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("    WHERE  CLASSCD < '90' AND YEAR  = '" + param._year + "' AND VALUE(VALUATION,0) < 1 AND ");
            stb.append("           ANNUAL = '" + param._grade + "' AND  ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append("    GROUP BY SCHREGNO ");
        }
        stb.append("   ) ");

        //メイン
        stb.append("SELECT K1.SCHREGNO,");
        stb.append("       K1.ATTENDNO,");
        stb.append("       K4.NAME, ");
        stb.append("       K2.CLASSCD,");
        stb.append("       K2.VAL, ");
        stb.append("       K3.VAL AS ALL_VAL,");
        stb.append("       K3.ASSESSMARK, ");
        stb.append("       CASE WHEN K4.GRD_DIV = '2' OR K4.GRD_DIV = '3' THEN '＊' ");
        stb.append("            WHEN '3' < VALUE(K4.GRD_DIV,'4') THEN K5.BIKOU ");
        stb.append("            ELSE NULL END AS BIKOU ");
        stb.append("FROM   SCHNO K1 ");
        stb.append("       LEFT JOIN ASSESS_CLASS K2 ON K2.SCHREGNO=K1.SCHREGNO ");
        stb.append("       LEFT JOIN ASSESS_ALL2 K3 ON K3.SCHREGNO=K1.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_BASE_MST K4 ON K4.SCHREGNO=K1.SCHREGNO ");
        stb.append("       LEFT JOIN STUDYREC_BIKOU K5 ON K5.SCHREGNO=K1.SCHREGNO ");
        stb.append("ORDER BY K1.ATTENDNO ");
        return stb.toString();

    }//psSchnoAssess()の括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74475 $ $Date: 2020-05-22 16:21:26 +0900 (金, 22 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {

        final String _year;
        final String _gakki;
        final String _grade;
        String _classSelected;
        final String _outDiv;
        final String _hyoteiYomikae;

        boolean _isGakunensei = false;
        boolean _isGradeH = false;
        String _useCurriculumcd;
        final String _useAssessCourseMst;
        final String _useTestCountflg;
        final String _SCHOOLKIND;
        final String _useSchool_KindField;
        final String _ctrlDate;
        final String _useProvFlg;
        final String _includeMirisyuuFlg;

        public Param(final DB2UDB db2, final HttpServletRequest request) {

            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期 1,2,3
            _grade = request.getParameter("GRADE");                       //学年
            String classcd[] = request.getParameterValues("CLASS_SELECTED");//年組
            _includeMirisyuuFlg = request.getParameter("INCLUDE_MIRISYUU"); //未履修科目を評定に含むかのフラグ
            _classSelected = "(";
            for (int ia=0 ; ia<classcd.length ; ia++) {
                if (ia > 0) _classSelected = _classSelected + ",";
                _classSelected = _classSelected + "'" + classcd[ia] + "'";
            }
            _classSelected = _classSelected + ")";
            _outDiv = request.getParameter("OUT_DIV");                     //出力対象 1:学年評価 2:評定
            _hyoteiYomikae = request.getParameter("HYOTEI_YOMIKAE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _isGakunensei = "0".equals(getSchoolDiv(db2, _year));
            _isGradeH = "H".equals(getSchoolKind(db2, _grade));
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _useProvFlg = request.getParameter("useProvFlg");
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _ctrlDate = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        }

        private String getSchoolDiv(final DB2UDB db2, final String year) {
            String schoolDiv = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR = '" + year + "' ";
                if ("1".equals(_useSchool_KindField)) {
                    sql += " AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolDiv = rs.getString("SCHOOLDIV");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolDiv;
        }


        private String getSchoolKind(final DB2UDB db2, final String grade) {
            String schoolKind = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAME1 AS SCHOOL_KIND FROM NAME_MST WHERE NAMECD1 = 'A023' AND '" + grade + "' BETWEEN NAME2 AND NAME3 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
        }
    }

}//クラスの括り
