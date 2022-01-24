// kanji=漢字
/*
 * $Id: 11f994f66a6ed3a47feae5f24bef801b23f4e8c8 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 中学校用（レイアウトは近大付属中学、データは千代田区立九段版を基に作成） 2006/04/10
 * Build yamashiro 2006/04/25 yamashiro・一括出力(学年指定かつHR組単位)処理を追加 --NO001
 */

public class KNJA131K {
    private static final Log log = LogFactory.getLog(KNJA131K.class);

    private static final KNJObjectAbs knjobj = new KNJEditString();         //編集用クラス

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定


    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        boolean nonedata = false;


        // print svf設定
        sd.setSvfInit(request, response, svf);

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        // パラメータの取得
        Param param = getParam(request, db2);

        // 印刷処理
        nonedata = printSvf(request, db2, svf, param);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private Param getParam(HttpServletRequest request, DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    /**
     *  文字数を取得
     */
    private static int retStringByteValue(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }
    
    private static Calendar getCalendarOfDate(final String date) {
        final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(sqlDate);
        return cal;
    }

    private static int getNendo(final Calendar cal) {
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
            return year - 1;
        }
        return year;
    }

    /**
     * 印刷処理 NO001 Modify
     */
    private boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;

        String selected[] = null; // 出力対象HR組を格納

        try {
            
            if ("2".equals(request.getParameter("OUTPUT"))) {
                selected = request.getParameterValues("category_selected"); // HR組を格納
            } else {
                selected = request.getParameterValues("GRADE_HR_CLASS"); // HR組を格納
            }

            final List knjobj = setKnj131List(db2, request); // 帳票作成JAVAクラスをＬＩＳＴへ格納

            for (int i = 0; i < selected.length; i++) { // HR組の繰り返し
                final List schregnotList = param.getSchregnoList(db2, request, selected[i]); // 出力対象学籍番号を格納

                for (final Iterator t = schregnotList.iterator(); t.hasNext();) { // --学籍番号の繰り返し
                    final String schregno = (String) t.next();
                    final Student student = new Student(schregno, db2, param);
                    log.fatal(" schregno = " + student._schregno);

                    for (final Iterator r = knjobj.iterator(); r.hasNext();) { // --帳票作成JAVAクラスの繰り返し
                        final BASE knj131 = (BASE) (r.next()); // 帳票作成クラス
                        if (knj131.printSvf(db2, svf, param, student)) {
                            nonedata = true; // 印刷処理
                        }
                    }

                    t.remove(); // 処理済み学籍番号をLISTから削除
                }
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return nonedata;
    }

    /**
     * 帳票作成JAVAクラスをＬＩＳＴへ格納 NO001 Build
     */
    private List setKnj131List(final DB2UDB db2, final HttpServletRequest request) {
        List rtnList = new ArrayList();
        if (request.getParameter("seito") != null) {
            rtnList.add(new KNJA131KFORM1()); // 様式１（学籍に関する記録）
        }
        if (request.getParameter("gakushu1") != null) {
            rtnList.add(new KNJA131KFORM3()); // 様式２（指導に関する記録）前期課程
        }
        if (request.getParameter("katsudo") != null) {
            rtnList.add(new KNJA131KFORM5()); // 様式３
        }
        return rtnList;
    }
    
    /**pkg*/abstract static class BASE
    {
        
        protected Map hmap;

        protected void setMapForHrclassName(final DB2UDB db2) {
            if (hmap == null) {
                hmap = KNJ_Get_Info.getMapForHrclassName(db2);  // 表示用組
            }
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public abstract boolean printSvf(DB2UDB db2, Vrw32alp svf, Param param, Student student);
    }

    // --- 内部クラス -------------------------------------------------------
    private static class Student {
        final String _schregno;
        final String _schname;
        final SchregEntGrdHistDat _entGrdHist;
        final List _schregEntGrdHistComebackDatComebackDateList;
        final List _schregEntGrdHistComebackDatList;
        final Map _yearLimitCache = new HashMap();
        Student(final String schregno, final DB2UDB db2, final Param param) {
            _schregno = schregno;
            _schname = getSchname(db2, param);
            _schregEntGrdHistComebackDatComebackDateList = getSchregEntGrdHistComebackDatComebackDateList(db2, _schregno, param);
            _schregEntGrdHistComebackDatList = new ArrayList();
            for (final Iterator it = _schregEntGrdHistComebackDatComebackDateList.iterator(); it.hasNext();) {
                final String comebackDate = (String) it.next();
                final SchregEntGrdHistDat entGrdHistComebackDat = SchregEntGrdHistDat.load(db2, this, comebackDate);
                entGrdHistComebackDat._comebackDate = comebackDate;
                _schregEntGrdHistComebackDatList.add(entGrdHistComebackDat);    
            }
            _entGrdHist = SchregEntGrdHistDat.load(db2, this, null);
        }
        

        /**
         * 印刷する生徒情報
         */
        private List getPrintSchregEntGrdHistList(final Param param) {
            final List rtn = new ArrayList();
            if (_schregEntGrdHistComebackDatList.size() == 0) {
                return Collections.singletonList(_entGrdHist);
            }
            // 復学が同一年度の場合、復学前、復学後を表示
            // 復学が同一年度ではない場合、復学後のみ表示
            final List personalInfoList = new ArrayList();
            personalInfoList.addAll(_schregEntGrdHistComebackDatList);
            personalInfoList.add(_entGrdHist);
            for (final Iterator it = personalInfoList.iterator(); it.hasNext();) {
                final SchregEntGrdHistDat personalInfo = (SchregEntGrdHistDat) it.next();
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd();
                if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
                    rtn.add(personalInfo);
                }
            }
            return rtn;
        }

        protected String getSchname(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                //個人学籍データ
                ps = setPersonalInfoStatement(db2, param);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("NAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        protected PreparedStatement setPersonalInfoStatement(final DB2UDB db2, final Param param) throws SQLException {
            int p = 0;
            PreparedStatement ps;
            ps = db2.prepareStatement(new KNJ_PersonalinfoSql().sql_info_reg("1111111000"));
            ps.setString( ++p, _schregno );  //学籍番号
            ps.setString(++p, param._year); // 年度
            ps.setString(++p, param._gakki); // 学期
            ps.setString( ++p, _schregno );  //学籍番号
            ps.setString(++p, param._year); // 年度
            return ps;
        }
        
        private static String getKNJ_GradeRecSqlsql_state(final Param param, final Student student) {

            String sql = null;
            sql = "SELECT "
                    + "T1.YEAR,"
                    + "T1.GRADE,"
                    + "T1.HR_CLASS,"
                    + "T1.ATTENDNO,"
                //  + "T7.ANNUAL,"
                    + "T1.ANNUAL,"
                    + "T3.HR_NAME,";
                    if ("1".equals(param._useSchregRegdHdat)) {
                       sql += "T3.HR_CLASS_NAME1,";
                    }
            sql +=    "T2.SCHOOLDIV,"
                    + "T4.STAFFNAME,"
                    + "S2.STAFFNAME as STAFFNAME2,"
                    + "CASE WHEN T6.PRINCIPAL_NAME IS NOT NULL "
                    + "     THEN T6.PRINCIPAL_NAME "
                    + "     ELSE T7.STAFFNAME "
                    + "END AS PRINCIPALNAME "
                    + ",T4.STAFFCD "
                    + ",T5.STAFFCD AS PRINCIPALSTAFFCD "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "'0' AS SCHOOLDIV,"
                            + "SCHREGNO,"
                            + "YEAR,"
                            + "SEMESTER,"
                            + "GRADE,"
                            + "HR_CLASS,"
                            + "ANNUAL,"
                            + "ATTENDNO "
                        + "FROM "
                            + "V_REGDYEAR_GRADE_DAT "
                        + "WHERE "
                            + "SCHREGNO = '" + student._schregno +"' "
                        + "UNION SELECT "
                            + "'1' AS SCHOOLDIV,"
                            + "SCHREGNO,"
                            + "YEAR,"
                            + "SEMESTER,"
                            + "GRADE,"
                            + "HR_CLASS,"
                            + "ANNUAL,"
                            + "ATTENDNO "
                        + "FROM "
                            + "V_REGDYEAR_UNIT_DAT "
                        + "WHERE "
                            + "SCHREGNO = '" + student._schregno +"' "
                    + ")T1 "
                //  + "INNER JOIN SCHREG_REGD_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO "
                //                                  + "AND T7.YEAR = T1.YEAR "
                //                                  + "AND T7.SEMESTER = T1.SEMESTER "
                    + "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR AND T2.SCHOOLDIV=T1.SCHOOLDIV "
                    + "INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR "
                                                        + "AND T3.SEMESTER=T1.SEMESTER "
                                                        + "AND T3.GRADE=T1.GRADE "
                                                        + "AND T3.HR_CLASS=T1.HR_CLASS "
                    + "LEFT JOIN STAFF_MST T4 ON T4.STAFFCD=T3.TR_CD1 "
                    + "LEFT JOIN STAFF_MST S2 ON S2.STAFFCD=T3.TR_CD2 "
                    + "LEFT JOIN("
                        + "SELECT "
                            + "YEAR,"
                            + "MAX(STAFFCD) AS STAFFCD "
                        + "FROM "
                            + "V_STAFF_MST "
                        + "WHERE "
                                + "YEAR <= '" + param._year + "' "
                            + "AND JOBCD='0001' "
                        + "GROUP BY "
                            + "YEAR "
                    + ")T5 ON T5.YEAR=T2.YEAR "
                    + "LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T5.STAFFCD "
                    + "LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR "
                    + "     AND T6.CERTIF_KINDCD = '108' "
                + "WHERE "
                    + "T1.YEAR <= '" + param._year + "' "
                + "ORDER BY "
                    + "T1.GRADE,T1.YEAR";

                return sql;
        }//public String sql_stateの括り
        
        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getSchregEntGrdHistDatYearEnd(final SchregEntGrdHistDat target, final Param param) {
            final TreeSet yearSetAll = new TreeSet();
            final List personalInfoList = getPrintSchregEntGrdHistList(param);
            for (final ListIterator it = personalInfoList.listIterator(personalInfoList.size()); it.hasPrevious();) { // 新しい生徒情報順
                final SchregEntGrdHistDat entGrdHist = (SchregEntGrdHistDat) it.previous();
                final int begin = entGrdHist.getYearBegin();
                final int end = entGrdHist.getYearEnd();
                final TreeSet yearSet = new TreeSet();
                for (int y = begin; y <= end; y++) {
                    final Integer year = new Integer(y);
                    if (yearSetAll.contains(year)) {
                        // 新しい生徒情報で表示されるものは含まない
                    } else {
                        yearSetAll.add(year);
                        yearSet.add(year);
                    }
                }
                if (target == entGrdHist) {
                    if (yearSet.isEmpty()) {
                        return -1; // 対象の生徒情報は成績等は表示しない
                    }
                    return ((Integer) yearSet.last()).intValue();
                }
            }
            return -1; // 対象の生徒情報は成績等は表示しない
        }
        
        private boolean yearIsOutLimit(final String regdYear, final Param param) {
            final String year = "1".equals(param._seitoSidoYorokuCyugakuKirikaeNendoForRegdYear) ? regdYear : _entGrdHist._curriculumYear;
            if (null == _yearLimitCache.get(year)) {
                Boolean rtn;
                if (!NumberUtils.isDigits(year)) {
                    rtn = Boolean.FALSE;
                } else {
                    rtn = new Boolean(Integer.parseInt(year) >= param._seitoSidoYorokuCyugakuKirikaeNendo);
                }
                log.info(" kirikaeRegd = " + param._seitoSidoYorokuCyugakuKirikaeNendoForRegdYear + ", check year = " + year + ", kirikaeNendo = " + param._seitoSidoYorokuCyugakuKirikaeNendo + ", notprint? = " + rtn);
                _yearLimitCache.put(year, rtn);
            }
            return ((Boolean) _yearLimitCache.get(year)).booleanValue();
        }
        
        private static List getSchregEntGrdHistComebackDatComebackDateList(final DB2UDB db2, final String schregno, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List rtn = Collections.EMPTY_LIST;
            if (!param._hasSchregEntGrdHistComebackDat) {
                return rtn;
            }
            try {
                final String sql = 
                        " SELECT T1.COMEBACK_DATE "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = '" + schregno + "' AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' AND FISCALYEAR(T1.COMEBACK_DATE) <= '" + param._year + "' "
                        + " ORDER BY COMEBACK_DATE ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rtn == Collections.EMPTY_LIST) {
                        rtn = new ArrayList();
                    }
                    rtn.add(rs.getString("COMEBACK_DATE"));
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        protected static String sqlSchGradeRec(
                final Param param,
                final Student student) {
            final StringBuffer stb = new StringBuffer();
            // 印鑑関連 1
            stb.append(" WITH T_TEACHER AS ( ");
            stb.append("     SELECT ");
            stb.append("         STAFFCD, ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         FROM_DATE, ");
            stb.append("         MIN(TO_DATE) AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT ");
            stb.append("     WHERE ");
            stb.append("         TR_DIV = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         STAFFCD, YEAR, GRADE, HR_CLASS, FROM_DATE ");
            stb.append(" ), T_MINMAX_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         MAX(FROM_DATE) AS MAX_FROM_DATE, ");
            stb.append("         MIN(FROM_DATE) AS MIN_FROM_DATE ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER ");
            stb.append("     GROUP BY ");
            stb.append("         YEAR, GRADE, HR_CLASS ");
            stb.append(" ), T_TEACHER_MIN_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MIN_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MIN_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), T_TEACHER_MAX_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MAX_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MAX_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), REGD AS ( ");
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("      WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER IN (SELECT  MAX(T2.SEMESTER)AS SEMESTER");
            stb.append("                             FROM    SCHREG_REGD_DAT T2");
            stb.append("                             WHERE   T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR");
            stb.append("                             GROUP BY T2.YEAR)");
            stb.append(" ), PRINCIPAL_HIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR) AS ORDER ");
            stb.append("     FROM ");
            stb.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,REGD T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
            stb.append("     ORDER BY ");
            stb.append("         T2.YEAR, T1.FROM_DATE ");
            stb.append(" ), YEAR_PRINCIPAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR ");
            stb.append("         ,T2.STAFFCD AS PRINCIPALSTAFFCD1, T2.FROM_DATE AS PRINCIPAL1_FROM_DATE, T2.TO_DATE AS PRINCIPAL1_TO_DATE ");
            stb.append("         ,T3.STAFFCD AS PRINCIPALSTAFFCD2, T3.FROM_DATE AS PRINCIPAL2_FROM_DATE, T3.TO_DATE AS PRINCIPAL2_TO_DATE ");
            stb.append("     FROM ( ");
            stb.append("       SELECT YEAR, MIN(ORDER) AS FIRST, MAX(ORDER) AS LAST FROM PRINCIPAL_HIST GROUP BY YEAR ");
            stb.append("      ) T1 ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T2 ON T2.YEAR = T1.YEAR AND T2.ORDER = T1.LAST ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T3 ON T3.YEAR = T1.YEAR AND T3.ORDER = T1.FIRST ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,T3.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("         ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,T4.STAFFCD AS STAFFCD1 ");
            stb.append("   ,T9.STAFFCD AS STAFFCD2 ");
            stb.append("   ,T10.FROM_DATE AS STAFF1_FROM_DATE, T10.TO_DATE AS STAFF1_TO_DATE ");
            stb.append("   ,T11.FROM_DATE AS STAFF2_FROM_DATE, T11.TO_DATE AS STAFF2_TO_DATE ");
            stb.append("   ,T5.STAFFCD AS PRINCIPALSTAFFCD ");
            stb.append("   ,CASE WHEN T6.PRINCIPAL_NAME IS NOT NULL ");
            stb.append("         THEN T6.PRINCIPAL_NAME ");
            stb.append("         ELSE T7.STAFFNAME ");
            stb.append("   END AS PRINCIPALNAME ");
            // stb.append("        ,T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME ");
            stb.append("   ,T13.STAFFCD AS PRINCIPALSTAFFCD1 ");
            stb.append("   ,T14.STAFFCD AS PRINCIPALSTAFFCD2 ");
            stb.append("   ,T12.PRINCIPAL1_FROM_DATE, T12.PRINCIPAL1_TO_DATE ");
            stb.append("   ,T12.PRINCIPAL2_FROM_DATE, T12.PRINCIPAL2_TO_DATE ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("                              AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN ( ");
            stb.append("      SELECT ");
            stb.append("         YEAR ");
            stb.append("        ,MAX(STAFFCD) AS STAFFCD ");
            stb.append("      FROM   V_STAFF_MST ");
            stb.append("      WHERE  YEAR <= '" + param._year + "' AND JOBCD = '0001' ");
            stb.append("      GROUP BY YEAR ");
            stb.append("     ) T5 ON T5.YEAR = T1.YEAR ");
            stb.append(" LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T5.STAFFCD ");
             
            stb.append(" LEFT JOIN T_TEACHER_MAX_FROM_DATE T10A ON T10A.YEAR = T1.YEAR ");
            stb.append("          AND T10A.GRADE = T1.GRADE AND T10A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T10 ON T10.STAFFCD = T10A.STAFFCD ");
            stb.append("          AND T10.FROM_DATE = T10A.MAX_FROM_DATE AND T10.YEAR = T1.YEAR AND T10.GRADE = T1.GRADE AND T10.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MIN_FROM_DATE T11A ON T11A.YEAR = T1.YEAR ");
            stb.append("          AND T11A.GRADE = T1.GRADE AND T11A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T11 ON T11.STAFFCD = T11A.STAFFCD ");
            stb.append("          AND T11.FROM_DATE = T11A.MIN_FROM_DATE AND T11.YEAR = T1.YEAR AND T11.GRADE = T1.GRADE AND T11.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T10.STAFFCD ");
            stb.append(" LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T11.STAFFCD ");
            
            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '108'");
            stb.append(" LEFT JOIN YEAR_PRINCIPAL T12 ON T12.YEAR = T1.YEAR ");
            stb.append(" LEFT JOIN STAFF_MST T13 ON T13.STAFFCD = T12.PRINCIPALSTAFFCD1 ");
            stb.append(" LEFT JOIN STAFF_MST T14 ON T14.STAFFCD = T12.PRINCIPALSTAFFCD2 ");
            stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS");
            return stb.toString();
        }
    }
    
    // --- 内部クラス -------------------------------------------------------
    private static class SchregEntGrdHistDat {
        private static SchregEntGrdHistDat NULL = new SchregEntGrdHistDat(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        final String _schoolKind;
        final String _finschoolcd;
        final String _finschoolName;
        final String _installationDiv;
        final String _finishDate;
        final String _curriculumYear;
        final String _entDate;
        final String _entDiv;
        final String _entReason;
        final String _entSchool;
        final String _entAddr;
        final String _grdDate;
        final String _grdDiv;
        final String _grdReason;
        final String _grdSchool;
        final String _grdAddr;
        final String _grdNo;
        final String _grdTerm;
        final String _entDivName;
        final String _grdDivName;
        String _comebackDate;

        SchregEntGrdHistDat(
                final String schoolKind,
                final String finschoolcd,
                final String finschoolName,
                final String installationDiv,
                final String finishDate,
                final String curriculumYear,
                final String entDate,
                final String entDiv,
                final String entReason,
                final String entSchool,
                final String entAddr,
                final String grdDate,
                final String grdDiv,
                final String grdReason,
                final String grdSchool,
                final String grdAddr,
                final String grdNo,
                final String grdTerm,
                final String entDivName,
                final String grdDivName
         ) { 
            _schoolKind = schoolKind;
            _finschoolcd = finschoolcd;
            _finschoolName = finschoolName;
            _installationDiv = installationDiv;
            _finishDate = finishDate;
            _curriculumYear = curriculumYear;
            _entDate = entDate;
            _entDiv = entDiv;
            _entReason = entReason;
            _entSchool = entSchool;
            _entAddr = entAddr;
            _grdDate = grdDate;
            _grdDiv = grdDiv;
            _grdReason = grdReason;
            _grdSchool = grdSchool;
            _grdAddr = grdAddr;
            _grdNo = grdNo;
            _grdTerm = grdTerm;
            _entDivName = entDivName;
            _grdDivName = grdDivName;
        }
        
        public int getYearBegin() {
            return null == _entDate ? 0 : getNendo(getCalendarOfDate(_entDate));
        }

        public int getYearEnd() {
            return null == _grdDate ? 9999 : getNendo(getCalendarOfDate(_grdDate));
        }

        /**
         * 学校種別をキーとするデータのマップを得る
         * @param db2
         * @param schregno
         * @return
         */
        private static SchregEntGrdHistDat load(final DB2UDB db2, final Student student, final String comebackDate) {
            SchregEntGrdHistDat rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(student, comebackDate);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String finschoolcd = rs.getString("FINSCHOOLCD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String installationDiv = rs.getString("INSTALLATION_DIV");
                    final String finishDate = rs.getString("FINISH_DATE");
                    final String curriculumYear = rs.getString("CURRICULUM_YEAR");
                    final String entDate = rs.getString("ENT_DATE");
                    final String entDiv = rs.getString("ENT_DIV");
                    final String entReason = rs.getString("ENT_REASON");
                    final String entSchool = rs.getString("ENT_SCHOOL");
                    final String entAddr = rs.getString("ENT_ADDR");
                    final String grdDate = rs.getString("GRD_DATE");
                    final String grdDiv = rs.getString("GRD_DIV");
                    final String grdReason = rs.getString("GRD_REASON");
                    final String grdSchool = rs.getString("GRD_SCHOOL");
                    final String grdAddr = rs.getString("GRD_ADDR");
                    final String grdNo = rs.getString("GRD_NO");
                    final String grdTerm = rs.getString("GRD_TERM");
                    final String entDivName = rs.getString("ENT_DIV_NAME");
                    final String grdDivName = rs.getString("GRD_DIV_NAME");
                    final SchregEntGrdHistDat schregentgrdhistdat = new SchregEntGrdHistDat(schoolKind, finschoolcd, finschoolName, installationDiv, finishDate, curriculumYear,
                            entDate, entDiv, entReason, entSchool, entAddr, grdDate, grdDiv, grdReason, grdSchool, grdAddr, grdNo, grdTerm,
                            entDivName, grdDivName);
                    
                    rtn = schregentgrdhistdat;
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn == null ? NULL : rtn;
        }
        
        private static String sql(final Student student, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.* ");
            sql.append("     , L1.FINSCHOOL_NAME ");
            sql.append("     , L2.NAME1 AS INSTALLATION_DIV ");
            sql.append("     , L3.NAME1 AS ENT_DIV_NAME ");
            sql.append("     , L4.NAME1 AS GRD_DIV_NAME ");
            sql.append(" FROM ");
            if (null != comebackDate) {
                sql.append("     SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            } else {
                sql.append("     SCHREG_ENT_GRD_HIST_DAT T1 ");
            }
            sql.append(" LEFT JOIN FINSCHOOL_MST L1 ON L1.FINSCHOOLCD = T1.FINSCHOOLCD ");
            sql.append(" LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L001' ");
            sql.append("     AND L2.NAMECD2 = L1.FINSCHOOL_DISTCD ");
            sql.append(" LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'A002' ");
            sql.append("     AND L3.NAMECD2 = T1.ENT_DIV ");
            sql.append(" LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'A003' ");
            sql.append("     AND L4.NAMECD2 = T1.GRD_DIV ");
            sql.append(" WHERE T1.SCHREGNO = '" + student._schregno + "' ");
            sql.append("   AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (null != comebackDate) {
                sql.append("   AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
            }
            return sql.toString();
        }
    }
    
    // --- 内部クラス -------------------------------------------------------
    /**
     * <<ローダー>>。
     */
    /**pkg*/static class Loader {
        public static List load(final DB2UDB db2, final String sql) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 0; i < meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i + 1);
                        final String val = rs.getString(columnName);
                        m.put(columnName, val);
                    }
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception! sql=" + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフマスタ>>。
     */
    /**pkg*/ static class StaffMst {
        /**pkg*/ static StaffMst Null = new StaffMst(null, null, null, null, null);
        final String _staffcd;
        final String _name;
        final String _kana;
        final String _nameReal;
        final String _kanaReal;
        private final Map _yearStaffNameSetUp;
        public StaffMst(final String staffcd, final String name, final String kana, final String nameReal, final String kanaReal) {
            _staffcd = staffcd;
            _name = name;
            _kana = kana;
            _nameReal = nameReal;
            _kanaReal = kanaReal;
            _yearStaffNameSetUp = new HashMap();
        }
        public boolean isPrintNameBoth(final String year) {
            final Map nameSetup = (Map) _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year) {
            final Map nameSetup = (Map) _yearStaffNameSetUp.get(year);
            return null != nameSetup;
        }
        
        public String getName(final String year) {
            final String name;
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = _name;
                } else {
                    name = _nameReal + (StringUtils.isBlank(_name) ? "" : ("（" + _name + "）"));
                }
            } else if (isPrintNameReal(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = _name;
                } else {
                    name = _nameReal;
                }
            } else {
                name = _name;
            }
            return name;
        }

        public List getNameLine(final String year, final int size) {
            final String[] name;
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    if (StringUtils.isBlank(_name)) {
                        name = new String[]{_nameReal};
                    } else {
                        final String n = "（" + _name + "）";
                        if ((null == _nameReal ? "" : _nameReal).equals(_name)) {
                            name =  new String[]{_nameReal};
                        } else if (retStringByteValue(_nameReal + n) > size) {
                            name =  new String[]{_nameReal, n};
                        } else {
                            name =  new String[]{_nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    name = new String[]{_nameReal};
                }
            } else {
                name = new String[]{_name};
            }
            return Arrays.asList(name);
        }

        public static StaffMst get(final Map staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return (StaffMst) staffMstMap.get(staffcd);
        }

        public static Map load(final DB2UDB db2, final String year) {
            final Map rtn = new HashMap();

            final String sql1 = "SELECT * FROM STAFF_MST ";
            for (final Iterator it = Loader.load(db2, sql1).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String staffcd = (String) m.get("STAFFCD");
                final String name = (String) m.get("STAFFNAME");
                final String kana = (String) m.get("STAFFNAME_KANA");
                final String nameReal = (String) m.get("STAFFNAME_REAL");
                final String kanaReal = (String) m.get("STAFFNAME_KANA_REAL");
                
                final StaffMst s = new StaffMst(staffcd, name, kana, nameReal, kanaReal);
                
                rtn.put(s._staffcd, s);
            }
            
            final String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT WHERE YEAR <= '" + year + "' AND DIV = '02' ";
            for (final Iterator it = Loader.load(db2, sql2).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                if (null == rtn.get(m.get("STAFFCD"))) {
                    continue;
                }
                final StaffMst s = (StaffMst) rtn.get(m.get("STAFFCD"));
                
                final Map nameSetupDat = new HashMap();
                nameSetupDat.put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
                s._yearStaffNameSetUp.put(m.get("YEAR"), nameSetupDat);
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + ", nameSetupDat=" + _yearStaffNameSetUp + ")";
        }
    }
    

    /**
     * 学校教育システム 賢者 [学籍管理] 生徒指導要録 中学校用（レイアウトは近大付属中学、データは千代田区立九段版を基に作成）
     * 様式1（学生に関する記録）
     * 2006/04/10 Build yamashiro
     * 2006/04/24 yamashiro・名称マスターに表示用組( 'A021'+HR_CLASSで検索 )の処理を追加 --NO003 => 無い場合は従来通りHR_CLASSを出力
     * ・生徒名および保護者名の出力仕様変更 --NO006
     * ・学籍異動履歴出力仕様変更 --NO007
     * 2006/05/02 yamashiro・入学前学歴の学校名出力仕様変更 --NO008
     */
    private static class KNJA131KFORM1 extends KNJA131K.BASE {

        /**
         * SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) {
            final String form = "KNJA131_9.frm";
            final List entGrdHistList = student.getPrintSchregEntGrdHistList(param);
            for (final Iterator it = entGrdHistList.iterator(); it.hasNext();) {
                final SchregEntGrdHistDat entGrdHist = (SchregEntGrdHistDat) it.next();
                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", "中学校指導要録"); // 項目名
                printSvfDefault(svf, param); // デフォルト印刷
                printSchool(db2, svf, param); // 学校情報
                printEntGrdHist(db2, svf, param, student, entGrdHist); // 個人情報
                printAddress(db2, svf, param, student, entGrdHist); // 住所履歴情報
                printTransfer(db2, svf, param, student, entGrdHist); // 異動履歴情報
                printRegd(db2, svf, param, student, entGrdHist); // 年次・ホームルーム・整理番号
                printAftGradCourse(db2, svf, param, student); // 異動履歴情報
                svf.VrEndPage();
            }
            return true;
        }

        /**
         * SVF-FORM 印刷処理 明細 学校情報
         */
        private void printSchool(final DB2UDB db2, final Vrw32alp svf, final Param param) {
            // 学校データ
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(new KNJ_SchoolinfoSql("10000").pre_sql());
                ps.setString(++p, param._year);
                ps.setString(++p, param._year);
                rs = ps.executeQuery();

                if (rs.next()) {
                    final String schoolName1 = StringUtils.defaultString(param._schoolName, rs.getString("SCHOOLNAME1"));
                    final int n = retStringByteValue(schoolName1);
                    if (50 < n) {
                        svf.VrsOut("SCHOOLNAME3", schoolName1);
                    } else if (40 < n) {
                        svf.VrsOut("SCHOOLNAME2", schoolName1);
                    } else if (0 < n) {
                        svf.VrsOut("SCHOOLNAME1", schoolName1);
                    }

                    final int n1 = retStringByteValue(rs.getString("SCHOOLADDR1"));
                    final int n2 = retStringByteValue(rs.getString("SCHOOLADDR2"));
                    if (40 < n1) {
                        svf.VrsOut("SCHOOLADDRESS1_2", rs.getString("SCHOOLADDR1"));
                    } else if (0 < n1) {
                        svf.VrsOut("SCHOOLADDRESS1_1", rs.getString("SCHOOLADDR1"));
                    }

                    if (40 < n2 || 40 < n1) {
                        svf.VrsOut("SCHOOLADDRESS2_2", rs.getString("SCHOOLADDR2"));
                    } else if (0 < n2) {
                        svf.VrsOut("SCHOOLADDRESS2_1", rs.getString("SCHOOLADDR2"));
                    }
                }
            } catch (final Exception e) {
                log.debug("printSvfDetail_1 error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * SVF-FORM 印刷処理 明細 個人情報
         */
        private void printEntGrdHist(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist) {
            ResultSet rs = null;
            // 個人学籍データ
            PreparedStatement ps = null;
            try {
                final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                final PreparedStatement psSchool = db2.prepareStatement(sql);
                final ResultSet rsSchool = psSchool.executeQuery();
                final String schoolDiv = (rsSchool.next()) ? (null == rsSchool.getString("NAME1") ? "" : rsSchool.getString("NAME1")) : "";
                DbUtils.closeQuietly(null, psSchool, rsSchool);
                db2.commit();
                ps = student.setPersonalInfoStatement(db2, param);
                rs = ps.executeQuery();

                if (rs.next()) {
                    if (rs.getString("COURSENAME") != null) {
                        svf.VrsOut("COURSE", rs.getString("COURSENAME"));
                    }
                    if (rs.getString("MAJORNAME") != null) {
                        svf.VrsOut("MAJOR", rs.getString("MAJORNAME"));
                    }
                    if (rs.getString("NAME_KANA") != null) {
                        svf.VrsOut("KANA", rs.getString("NAME_KANA"));
                    }
                    if (rs.getString("GUARD_KANA") != null) {
                        svf.VrsOut("GUARDIANKANA", rs.getString("GUARD_KANA"));
                    }
                    if (param.KANJI_OUT != null) {
                        int n = retStringByteValue(rs.getString("NAME"));
                        if (0 < n && n <= 24) {
                            svf.VrsOut("NAME1", rs.getString("NAME"));
                        } else if (0 < n) {
                            svf.VrsOut("NAME2", rs.getString("NAME"));
                        }
                        n = retStringByteValue(rs.getString("GUARD_NAME"));
                        if (0 < n && n <= 24) {
                            svf.VrsOut("GUARDIANNAME1", rs.getString("GUARD_NAME"));
                        } else if (0 < n) {
                            svf.VrsOut("GUARDIANNAME2", rs.getString("GUARD_NAME"));
                        }
                    }

                    svf.VrsOut("BIRTHDAY", setDateFormat2(formatDate(rs.getString("BIRTHDAY"), param)) + "生");
                    if (rs.getString("SEX") != null) {
                        svf.VrsOut("SEX", rs.getString("SEX"));
                    }
                    final String suf = param._isChiben ? "卒業" : "小学校卒業";  

                    
                    printSvfFinSchool(svf, entGrdHist._finschoolName, suf); // 入学前学歴の学校名編集
                    printSvfInstallationDiv(svf, student, entGrdHist._finschoolName, schoolDiv, entGrdHist);

                    svf.VrsOut("FINISHDATE", setDateFormat(formatDateM(entGrdHist._finishDate, param), param._year, param));

                    if (StringUtils.defaultString(rs.getString("GUARD_ADDR1")).equals(StringUtils.defaultString(rs.getString("ADDR1")))
                            && StringUtils.defaultString(rs.getString("GUARD_ADDR2")).equals(StringUtils.defaultString(rs.getString("ADDR2")))) { // 06/03/23
                        svf.VrsOut("GUARDIANADD1_1_1", "生徒の欄に同じ");
                    } else {
                        svf.VrsOut("GUARDZIP1", rs.getString("GUARD_ZIPCD"));
                        final int n1 = retStringByteValue(rs.getString("GUARD_ADDR1"));
                        final int n2 = retStringByteValue(rs.getString("GUARD_ADDR2"));
                        if (40 < n1) {
                            svf.VrsOut("GUARDIANADD1_1_2", rs.getString("GUARD_ADDR1"));
                        } else if (0 < n1) {
                            svf.VrsOut("GUARDIANADD1_1_1", rs.getString("GUARD_ADDR1"));
                        }
                        if (40 < n2 || 40 < n1) {
                            svf.VrsOut("GUARDIANADD1_2_2", rs.getString("GUARD_ADDR2"));
                        } else if (0 < n2) {
                            svf.VrsOut("GUARDIANADD1_2_1", rs.getString("GUARD_ADDR2"));
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_2 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void printSvfInstallationDiv(final Vrw32alp svf, final Student student, final String jName, final String schoolDiv, final SchregEntGrdHistDat entGrdHist) {
            final String fieldInstallationDiv = "INSTALLATION_DIV";
            if (null != jName) {
                final int i = jName.indexOf('　');  // 全角スペース
                if (-1 != i && 5 >= i) {
                    final String ritu = jName.substring(0, i);
                    if (null != ritu) {
                        svf.VrsOut(fieldInstallationDiv, ritu + "立");
                    }
                }
            }
            final String installationDiv = entGrdHist._installationDiv;
            if ("KINJUNIOR".equals(schoolDiv) || "KINDAI".equals(schoolDiv) || "kyoai".equals(schoolDiv)) { // ここでは表示処理無し
//                    } else if ("omiya".equals(schoolDiv)) {
//                        svf.VrsOut("INSTALLATION_DIV", "　　　　　立");
            } else if (installationDiv != null) {
                final String putRitu = "HOUSEI".equals(schoolDiv) ? "立" : "" ;
                svf.VrsOut(fieldInstallationDiv, installationDiv + putRitu);
            }
        }
        
        /**
         *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
         *  高校指導要録・中学指導要録・中等学校指導要録の様式１で使用しています。
         *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
         *  全角スペースより前半の文字を○○○○○立と見なします。
         *  @param str1 例えば"千代田区　アルプ"
         *  @param str2 例えば"小学校卒業"
         */
        public void printSvfFinSchool(
                final Vrw32alp svf,
                final String str1,
                final String str2
        ) {
            final String schoolName;
            if (null == str1) {
                schoolName = "";
            } else {
                final int i = str1.indexOf('　');  // 全角スペース
                if (-1 < i && 5 >= i) {
                    schoolName = str1.substring(i + 1);
                } else {
                    schoolName = str1;
                }
            }
            final int schoolNameLen = retStringByteValue(schoolName);

            final String kotei = StringUtils.defaultString(str2);
            final int koteiLen = retStringByteValue(kotei);

            if (schoolNameLen == 0) {
                svf.VrsOut("FINSCHOOL1", kotei);
            } else if (schoolNameLen + koteiLen <= 40) {
                svf.VrsOut("FINSCHOOL1", schoolName + kotei);
            } else if(schoolNameLen + koteiLen <= 50) {
                svf.VrsOut("FINSCHOOL2", schoolName + kotei);
            } else {
                svf.VrsOut("FINSCHOOL2", schoolName);
                svf.VrsOut("FINSCHOOL3", kotei);
            }
        }

        /**
         * SVF-FORM 印刷処理 明細 個人情報 住所履歴情報 履歴を降順に読み込み、最大３件まで出力
         */
        private void printAddress(DB2UDB db2, Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT "
                        + "T1.ISSUEDATE,"
                        + "T1.ADDR1,"
                        + "T1.ADDR2,"
                        + "T1.ADDR_FLG,"
                        + "T1.ZIPCD,"
                        + "T2.COUNT,"
                        + "T1.SCHREGNO "
                    + "FROM "
                        + "SCHREG_ADDRESS_DAT T1 "
                        + "LEFT JOIN("
                            + "SELECT "
                                + "SCHREGNO,"
                                + "COUNT(SCHREGNO) AS COUNT "
                            + "FROM "
                                + "SCHREG_ADDRESS_DAT T1 "
                            + "WHERE "
                                    + "SCHREGNO = '" + student._schregno + "' "
                                + "AND FISCALYEAR(ISSUEDATE) <= '" + param._year + "' "
                            + "GROUP BY "
                                + "SCHREGNO "
                        + ")T2 ON T2.SCHREGNO = T1.SCHREGNO "
                    
                    + "WHERE "
                            + "T1.SCHREGNO = '" + student._schregno + "' "
                        + "AND FISCALYEAR(ISSUEDATE) <= '" + param._year + "' "
                    + "ORDER BY "
                        + "ISSUEDATE DESC";
                ps = db2.prepareStatement(sql);
                // 生徒住所履歴
                rs = ps.executeQuery();
                int i = 0; // 出力件数
                int num = 0;
                while (rs.next()) {
                    if (i == 0) {
                        num = rs.getInt("COUNT");
                        i = rs.getInt("COUNT");
                        if (3 < i) {
                            i = 3;
                        }
                    }
                    final String address1 = rs.getString("ADDR1");
                    final String address2 = rs.getString("ADDR2");

                    svf.VrsOut("ZIPCODE" + i, rs.getString("ZIPCD"));
                    printAddressLine(svf, rs.getString("ZIPCD"), i, num, "ZIPCODELINE" + i);
                    final int n1 = retStringByteValue(address1);
                    final int n2 = retStringByteValue(address2);
                    if (40 < n1) {
                        svf.VrsOut("ADDRESS" + i + "_1_2", address1);
                        printAddressLine(svf, address1, i, num, "ADDRESSLINE" + i + "_1_2");
                    } else if (0 < n1) {
                        svf.VrsOut("ADDRESS" + i + "_1_1", address1);
                        printAddressLine(svf, address1, i, num, "ADDRESSLINE" + i + "_1_1");
                    }
                    if (40 < n2 || 40 < n1) {
                        svf.VrsOut("ADDRESS" + i + "_2_2", address2);
                        printAddressLine(svf, address2, i, num, "ADDRESSLINE" + i + "_2_2");
                    } else if (0 < n2) {
                        svf.VrsOut("ADDRESS" + i + "_2_1", address2);
                        printAddressLine(svf, address2, i, num, "ADDRESSLINE" + i + "_2_1");
                    }

                    if (i == 1) {
                        break;
                    }
                    i--;
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_3 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 住所の取り消し線印刷
         * 
         * @param svf
         * @param i
         */
        private void printAddressLine(final Vrw32alp svf, final String str, final int i, final int num, final String field) {
            if (null == str || i == num || i == 3) {
                return;
            }
            printCancelLine(svf, retStringByteValue(str), field);
        }
        /**
         * 住所の取り消し線印刷
         * 
         * @param svf
         * @param i
         */
        private void printCancelLine(final Vrw32alp svf, final int keta, final String field) {
//            final StringBuffer stb = new StringBuffer();
//            for (int j = 0; j < keta / 2; j++) {
//                stb.append("＝");
//            }
//            svf.VrsOut(field, stb.toString());
            svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
        }

        /**
         * SVF-FORM 印刷処理 明細 個人情報 異動履歴情報
         */
        private void printTransfer(DB2UDB db2, Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist) {
            final String tengakusakiZenjitu = getTengausakiZenjitu(db2, param, student);
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            // 生徒異動履歴
            final StringBuffer sql = new StringBuffer();
            sql.append(      "SELECT ");
            sql.append(      "T1.YEAR,");
            sql.append(      "T1.SDATE,");
            sql.append(      "T1.EDATE,");
            sql.append(      "T1.REASON,");
            sql.append(      "T1.PLACE,");
            sql.append(      "T1.ADDR,");
            sql.append(      "T1.CERTIFNO,");
            sql.append(      "T1.NAMECD2,");
            sql.append(      "T1.NAMECD1,");
            sql.append(      "T3.NAME1,");
            sql.append(      "CASE T2.SCHOOLDIV WHEN '0' THEN T4.GRADE ELSE T5.GRADE END AS GRADE, ");
            sql.append(      "CASE T2.SCHOOLDIV WHEN '0' THEN T4_2.GRADE_CD ELSE T5_2.GRADE_CD END AS GRADE_CD ");
            sql.append(  "FROM ");
            sql.append(      "(");
            sql.append(          "SELECT ");
            sql.append(              "FISCALYEAR(ENT_DATE) AS YEAR,");
            sql.append(              "ENT_DATE AS SDATE,");
            sql.append(              "ENT_DATE AS EDATE,");
            sql.append(              "ENT_REASON AS REASON,");
            sql.append(              "ENT_SCHOOL AS PLACE,");
            sql.append(              "ENT_ADDR AS ADDR,");
            sql.append(              "'' AS CERTIFNO,");
            sql.append(              "ENT_DIV AS NAMECD2,");
            sql.append(              "'A002' AS NAMECD1 ");
            sql.append(          "FROM ");
            if (null != entGrdHist._comebackDate) {
                sql.append(              "SCHREG_ENT_GRD_HIST_COMEBACK_DAT ");
            } else {
                sql.append(              "SCHREG_ENT_GRD_HIST_DAT ");
            }
            sql.append(          "WHERE ");
            sql.append(              "SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' AND SCHREGNO= '" + student._schregno + "' ");
            if (null != entGrdHist._comebackDate) {
                sql.append(              "AND COMEBACK_DATE = '" + entGrdHist._comebackDate + "' ");
            }
            sql.append(          "UNION SELECT ");
            sql.append(              "FISCALYEAR(GRD_DATE) AS YEAR,");
            sql.append(              "GRD_DATE AS SDATE,");
            sql.append(              "GRD_DATE AS EDATE,");
            sql.append(              "GRD_REASON AS REASON,");
            sql.append(              "GRD_SCHOOL AS PLACE,");
            sql.append(              "GRD_ADDR AS ADDR,");
            sql.append(              "GRD_NO AS CERTIFNO,");
            sql.append(              "GRD_DIV AS NAMECD2,");
            sql.append(              "'A003' AS NAMECD1 ");
            sql.append(          "FROM ");
            if (null != entGrdHist._comebackDate) {
                sql.append(              "SCHREG_ENT_GRD_HIST_COMEBACK_DAT ");
            } else {
                sql.append(              "SCHREG_ENT_GRD_HIST_DAT ");
            }
            sql.append(          "WHERE ");
            sql.append(              "SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' AND SCHREGNO= '" + student._schregno + "' ");
            if (null != entGrdHist._comebackDate) {
                sql.append(              "AND COMEBACK_DATE = '" + entGrdHist._comebackDate + "' ");
            }
            sql.append(          "UNION SELECT ");
            sql.append(              "FISCALYEAR(TRANSFER_SDATE) AS YEAR,");
            sql.append(              "TRANSFER_SDATE AS SDATE,");
            sql.append(              "TRANSFER_EDATE AS EDATE,");
            sql.append(              "TRANSFERREASON AS REASON,");
            sql.append(              "TRANSFERPLACE AS PLACE,");
            sql.append(              "TRANSFERADDR AS ADDR,");
            sql.append(              "'' AS CERTIFNO,");
            sql.append(              "TRANSFERCD AS NAMECD2,");
            sql.append(              "'A004' AS NAMECD1 ");
            sql.append(          "FROM ");
            sql.append(              "SCHREG_TRANSFER_DAT ");
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO= '" + student._schregno + "' ");
            sql.append(      ")T1 ");
            sql.append(      "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR ");
            sql.append(      "INNER JOIN NAME_MST T3 ON T3.NAMECD1=T1.NAMECD1 AND T3.NAMECD2=T1.NAMECD2 ");
            sql.append(      "LEFT JOIN(");
            sql.append(          "SELECT ");
            sql.append(              "'0' AS SCHOOLDIV,");
            sql.append(              "YEAR,");
            sql.append(              "GRADE ");
            sql.append(          "FROM ");
            sql.append(              "V_REGDYEAR_GRADE_DAT ");     //学年制
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO = '" + student._schregno + "' ");
            sql.append(      ")T4 ON T4.YEAR=T2.YEAR AND T4.SCHOOLDIV=T2.SCHOOLDIV ");
            sql.append(      "LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            sql.append(      "LEFT JOIN(");
            sql.append(          "SELECT ");
            sql.append(              "'1' AS SCHOOLDIV,");
            sql.append(              "YEAR,");
            sql.append(              "GRADE ");
            sql.append(          "FROM ");
            sql.append(              "V_REGDYEAR_UNIT_DAT ");      //単位制
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO = '" + student._schregno + "' ");
            sql.append(      ")T5 ON T5.YEAR=T2.YEAR AND T5.SCHOOLDIV=T2.SCHOOLDIV ");
            sql.append(      "LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ");
            sql.append(  "WHERE ");
            sql.append(      "T1.YEAR <= '" + param._year + "' ");
            sql.append(  "ORDER BY ");
            sql.append(      "NAMECD1,NAMECD2,SDATE");

            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();

                printCancelLine(svf, retStringByteValue("＝＝＝＝＝＝＝＝"), "LINE1");
                printCancelLine(svf, retStringByteValue("＝＝＝＝＝＝＝＝"), "LINE2");
                int i = 0;
                while (rs.next()) {
                    final String cd1 = rs.getString("NAMECD1");
                    final int cd2 = Integer.parseInt(rs.getString("NAMECD2"));
                    if ("A002".equals(cd1)) {
                        if (cd2 == 4) { // 転入学
                            final String sdate = entGrdHist._entDate;
                            if (sdate != null) {
                                svf.VrsOut("MOVEDATE", setDateFormat(formatDate(sdate, param), param._year, param));
                            }
                            if (rs.getString("GRADE") != null) {
                                svf.VrsOut("MOVEGRADE", String.valueOf(Integer.parseInt(rs.getString("GRADE"))));
                            }
                            i = 0;
                            if (rs.getString("REASON") != null) {
                                svf.VrsOut("MOVENOTE" + (++i), rs.getString("REASON"));
                            }
                            if (rs.getString("PLACE") != null) {
                                svf.VrsOut("MOVENOTE" + (++i), rs.getString("PLACE"));
                            }
                            if (rs.getString("ADDR") != null) {
                                svf.VrsOut("MOVENOTE" + (++i), rs.getString("ADDR"));
                            }
                        } else {
                            final String sdate = entGrdHist._entDate;
                            if (sdate != null) {
                                svf.VrsOut("ENTERDATE", setDateFormat(formatDate(sdate, param), param._year, param));
                            }
                            if (rs.getString("REASON") != null)
                                svf.VrsOut("ENTERNOTE1", rs.getString("REASON"));
                            if (cd2 != 5) {
                                if (rs.getString("GRADE") != null)
                                    svf.VrsOut("ENTERGRADE1", String.valueOf(Integer.parseInt(rs.getString("GRADE"))));
                                svf.VrAttribute("LINE1", "X=" + 10000); // 打ち消し線消去
                            } else {
                                if (rs.getString("GRADE") != null)
                                    svf.VrsOut("ENTERGRADE2", String.valueOf(Integer.parseInt(rs.getString("GRADE"))));
                                svf.VrAttribute("LINE2", "X=" + 10000); // 打ち消し線消去
                            }
                        }
                    }
                    if ("A003".equals(cd1)) {
                        if (cd2 == 2) { // 2:退学
                            final String sdate = entGrdHist._grdDate;
                            if (sdate != null) {
                                svf.VrsOut("EXPULDATE2", setDateFormat(formatDate(sdate, param), param._year, param));
                            }
                            
                            if (rs.getString("REASON") != null)
                                svf.VrsOut("EXPULNOTE1", rs.getString("REASON"));
                            if (rs.getString("PLACE") != null)
                                svf.VrsOut("EXPULNOTE2", rs.getString("PLACE"));
                            if (rs.getString("ADDR") != null)
                                svf.VrsOut("EXPULNOTE3", rs.getString("ADDR"));
                        }
                        if (cd2 == 3) { // 3:転学
                            final String sdate = entGrdHist._grdDate;
                            if (tengakusakiZenjitu != null) {
                                svf.VrsOut("EXPULDATE1", setDateFormat(formatDate(tengakusakiZenjitu, param), param._year, param));
                            } else if (sdate != null){
                                svf.VrsOut("EXPULDATE1", setDateFormat(formatDate(sdate, param), param._year, param));
                            }
                            if (sdate != null) {
                                svf.VrsOut("EXPULDATE2", setDateFormat(formatDate(sdate, param), param._year, param));
                            }
                            
                            if (rs.getString("REASON") != null)
                                svf.VrsOut("EXPULNOTE1", rs.getString("REASON"));
                            if (rs.getString("PLACE") != null)
                                svf.VrsOut("EXPULNOTE2", rs.getString("PLACE"));
                            if (rs.getString("ADDR") != null)
                                svf.VrsOut("EXPULNOTE3", rs.getString("ADDR"));
                        }
                        if (cd2 == 1) { // 卒業
                            final String sdate = entGrdHist._grdDate;
                            if (sdate != null) {
                                svf.VrsOut("GRADDATE", setDateFormat(formatDate(sdate, param), param._year, param));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_4 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String seirekiFormat(final String date) {
            final String year = year(date);
            final String month = month(date);
            final String dayOfMonth = dayOfMonth(date);
            if (null == year || null == month || null == dayOfMonth) {
                return "";
            }
            return year + "年" + month + "月" + dayOfMonth + "日";
        }

        private String seirekiFormatM(final String date) {
            final String year = year(date);
            final String month = month(date);
            if (null == year || null == month) {
                return "";
            }
            return year + "年" + month + "月";
        }

        private String formatDate(final String sdate, final Param param) {
            if ("2".equals(param._seirekiFlg)) {
                return "　" + seirekiFormat(sdate);
            }
            return KNJ_EditDate.h_format_JP(sdate);
        }

        private String formatDateM(final String sdate, final Param param) {
            if ("2".equals(param._seirekiFlg)) {
                return "　" + seirekiFormatM(sdate);
            }
            return KNJ_EditDate.h_format_JP_M(sdate);
        }
        
        private String month(final String date) {
            try {
                final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(sqlDate);
                return String.valueOf(cal.get(Calendar.MONTH) + 1);
            } catch (Exception e) {
            }
            return null;
        }

        private String dayOfMonth(final String date) {
            try {
                final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(sqlDate);
                return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            } catch (Exception e) {
            }
            return null;
        }

        private String year(final String date) {
            try {
                final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(sqlDate);
                return String.valueOf(cal.get(Calendar.YEAR));
            } catch (Exception e) {
            }
            return null;
        }

        /**
         *  年度の編集（ブランク挿入）
         *  ○引数について >> １番目は編集対象年度「平成3年度」、２番目は元号取得用年度
         *  ○戻り値について >> 「平成3年度」-> 「平成 3年度」
         */
        public String setNendoFormat(
                final String hdate,
                final String nendo,
                final Param param
        ) {
            final StringBuffer stb = new StringBuffer();
            try {
                if (hdate == null && !StringUtils.isNumeric(nendo)) {
                    throw new NumberFormatException("nendo = " + nendo);
                } else if (hdate != null) {
                    //「平成18年度」の様式とする => 数値は２桁
                    stb.append(hdate);
                    setFormatInsertBlank(stb);
                } else {
                    if ("2".equals(param._seirekiFlg)) {
                        stb.append("　    年度");
                    } else {
                        //日付が無い場合は「平成　年度」の様式とする
                        final String gengou0 = nao_package.KenjaProperties.gengou(Integer.parseInt(nendo));
                        final String gengou = 2 < gengou0.length() ? gengou0.substring(0, 2) : gengou0;
                        stb.append(gengou);
                        stb.append("    年度");
                    }
                }
            } catch (NumberFormatException e) {
                 log.error("NumberFormatException", e);
            }
            return stb.toString();
        }
        
        /**
         *  日付の編集（ブランク挿入）
         *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
         *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
         */
        public String setDateFormat(
                final String hdate,
                final String nendo,
                final Param param
        ) {
            final StringBuffer stb = new StringBuffer();
            try {
                if (hdate == null && !StringUtils.isNumeric(nendo)) {
                    throw new NumberFormatException("nendo = " + nendo);
                } else if (hdate != null) {
                    //「平成18年 1月 1日」の様式とする => 数値は２桁
                    stb.append(hdate);
                    setFormatInsertBlank(stb);
                } else {
                    if ("2".equals(param._seirekiFlg)) {
                        stb.append("　    年    月    日");
                    } else {
                        //日付が無い場合は「平成　年  月  日」の様式とする
                        final String hformat = nao_package.KenjaProperties.gengou(Integer.parseInt(nendo), 4, 1);
                        final String gengou = 2 < hformat.length() ? hformat.substring(0, 2) : hformat;
                        stb.append(gengou);
                        stb.append("    年    月    日");
                    }
                }
            } catch (NumberFormatException e) {
                 log.error("NumberFormatException", e);
            }
            return stb.toString();
        }
        
        /**
         *  文字編集（ブランク挿入）
         */
        private static StringBuffer setFormatInsertBlank(final StringBuffer stb) {
            int n = 0;
            for (int i = 0; i < stb.length(); i++) {
                final char ch = stb.charAt(i);
                if (Character.isDigit (ch)) {
                    n++;
                } else {
                    if (0 < n) {
                        if (1 == n) {
                            stb.insert(i - n, "　");
                            i++;
                        } else if (2 == n) {
                            stb.insert(i - n, " ");
                            i++;
                        }
                        stb.insert(i, " ");
                        i++;
                        n = 0;
                    } else if (ch == '元') {
                        stb.insert(i, "　");
                        i++;
                    }
                }
            }
            return stb;
        }

        /**
         * 日付の編集（XXXX年XX月XX日の様式に編集）
         * @param hdate
         * @return
         */
        public static String setDateFormat2(final String hdate) {
            if (hdate == null || 0 == hdate.length()) {
                return "    年  月  日";
            }
            final StringBuffer stb = new StringBuffer(hdate);
            setFormatInsertBlank2(stb);
            return stb.toString();
        }
        

        /**
         * 文字編集（日付の数字が１桁の場合、ブランクを挿入）
         * @param stb
         * @return
         */
        private static StringBuffer setFormatInsertBlank2(final StringBuffer stb) {
            int n = 0;
            for (int i = 0; i < stb.length(); i++) {
                final char ch = stb.charAt(i);
                if (Character.isDigit(ch)) {
                    n++;
                } else if (0 < n) {
                    if (1 == n) {
                        stb.insert(i - n, " ");
                        i++;
                        n = 0;
                    }
                }
            }
            return stb;
        }

        private String getTengausakiZenjitu(DB2UDB db2, final Param param, final Student student) {
            String tengakusakiZenjitu = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM V_SCHREG_BASE_MST WHERE SCHREGNO = '" + student._schregno + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    tengakusakiZenjitu = rs.getString("TENGAKU_SAKI_ZENJITU");
                }
            } catch (Exception ex) {
                log.debug("tengakusakiZenjitu error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return tengakusakiZenjitu;
        }

        /**
         * SVF-FORM 印刷処理 明細 個人情報 学籍等履歴情報
         */
        private void printRegd(DB2UDB db2, Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                setMapForHrclassName(db2);

                final String sql = Student.sqlSchGradeRec(param, student); // 学籍等履歴
                // log.debug(" schgrade rec sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (student.yearIsOutLimit(year, param)) {
                        continue;
                    }
                    if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= entGrdHist.getYearEnd())) {
                        // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                        continue;
                    }
                    final int i = Integer.parseInt(rs.getString("GRADE"));
                    String hrname = null;
                    if ("1".equals(param._useSchregRegdHdat)) {
                        hrname = rs.getString("HR_CLASS_NAME1");
                    } else if ("0".equals(param._useSchregRegdHdat)) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), hmap);
                    }
                    if (hrname == null) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"));
                    }
                    svf.VrsOutn("HR_CLASS", i, hrname); // 組

                    svf.VrsOutn("ATTENDNO", i, String.valueOf(Integer.parseInt(rs.getString("ATTENDNO")))); // 出席番号

                    final String nendo0;
                    if ("2".equals(param._seirekiFlg)) {
                        nendo0 = "　" + year;
                    } else {
                        nendo0 = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
                    }
                    final String nendo = setNendoFormat(nendo0 + "年度", param._year, param);
                    if ("KINJUNIOR".equals(param._definecode.schoolmark) || "KIN".equals(param._definecode.schoolmark)) {
                        if (i == 1) {
                            svf.VrsOutn("NENDO", i, nendo);
                        }
                    } else {
                        svf.VrsOutn("NENDO", i, nendo);
                    }
                    if (null != year && !(Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                        continue;
                    }
                    // 校長印
                    final String pcd = rs.getString("PRINCIPALSTAFFCD");
                    final String pname = rs.getString("PRINCIPALNAME");
                    final String pcd1 = rs.getString("PRINCIPALSTAFFCD1");
                    final String pcd2 = rs.getString("PRINCIPALSTAFFCD2");
                    
                    final Staff pstaff = new Staff(year, new StaffMst(pcd, pname, null, null, null), null, null, pcd);
                    final Staff pstaff1 = new Staff(year, StaffMst.get(param.staffMstMap, pcd1), rs.getString("PRINCIPAL1_FROM_DATE"), rs.getString("PRINCIPAL1_TO_DATE"), pcd1);
                    final Staff pstaff2 = new Staff(year, StaffMst.get(param.staffMstMap, pcd2), rs.getString("PRINCIPAL2_FROM_DATE"), rs.getString("PRINCIPAL2_TO_DATE"), pcd2);
                    printSvfStaff(svf, param, i, "STAFFNAME1", "STAMP1", true, pstaff, pstaff1, pstaff2);

                    // 担任印
                    final String cd1 = rs.getString("STAFFCD1");
                    final String cd2 = rs.getString("STAFFCD2");

                    final Staff staff1 = new Staff(year, StaffMst.get(param.staffMstMap, cd1), rs.getString("STAFF1_FROM_DATE"), rs.getString("STAFF1_TO_DATE"), cd1);
                    final Staff staff2 = new Staff(year, StaffMst.get(param.staffMstMap, cd2), rs.getString("STAFF2_FROM_DATE"), rs.getString("STAFF2_TO_DATE"), cd2);

                    printSvfStaff(svf, param, i, "STAFFNAME2", "STAMP2", false, staff1, staff1, staff2);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_5 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /**
         * SVF-FORM 印刷処理 明細 個人情報 学籍等履歴情報
         */
        private TreeSet gakusekiYearSet(DB2UDB db2, final Param param, final Student student) {
            final TreeSet set = new TreeSet();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = Student.sqlSchGradeRec(param, student); // 学籍等履歴
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (student.yearIsOutLimit(year, param)) {
                        continue;
                    }
                    set.add(year);
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_5 error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return set;
        }

        /**
         * SVF-FORM 印刷処理 明細 個人情報 進路情報
         */
        private void printAftGradCourse(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) {
            if (param._isChiben) {
                final List afterGraduatedCourseTextList = AfterGraduatedCourse.loadTextList(db2, param, student, gakusekiYearSet(db2, param, student));
                for (int i = 0; i < afterGraduatedCourseTextList.size(); i++) {
                    svf.VrsOut("AFTER_GRADUATION" + String.valueOf(i + 1), (String) afterGraduatedCourseTextList.get(i));
                }
            }
        }

        private void printSvfStaff(final Vrw32alp svf, final Param param, final int i, final String field, final String stamp,
                final boolean isCheckStaff0, final Staff staff, final Staff staff1, final Staff staff2
        ) {
            final int csize = 16;
            final int size = param._isChiben ? 16 : 20;
            final String[] wide = new String[]{"", "_4", "_6", "_8"};
            final String[] shal = new String[]{"_2", "_3", "_5", "_7"};
            final String stampstaffcd;
            for (int j = 0; j < 4; j++) {
                svf.VrsOutn(field + wide[j], i, "");
                svf.VrsOutn(field + shal[j], i, "");
            }
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff.getNameString();
                final String sfx = param._isChiben ? shal[0] : (retStringByteValue(name) > csize ? wide[0] : shal[0]);
                svf.VrsOutn(field + sfx, i, name);
                stampstaffcd = staff._staffMst._staffcd;
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List line = new ArrayList();
                line.addAll(staff1._staffMst.getNameLine(staff1._year, size));
                if (line.size() == 2) {
                    final String[] sfx = param._isChiben ? new String[]{shal[0], shal[1]} : new String[]{wide[0], wide[1]};
                    svf.VrsOutn(field + sfx[0], i, (String) line.get(0));
                    svf.VrsOutn(field + sfx[1], i, (String) line.get(1));
                } else {
                    final String name = staff1.getNameString();
                    final String sfx = param._isChiben ? shal[0] : (retStringByteValue(name) > csize ? wide[0] : shal[0]);
                    svf.VrsOutn(field + sfx, i, name);
                }
                stampstaffcd = staff1._staffMst._staffcd;
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List line = new ArrayList();
                line.addAll(staff2.getNameBetweenLine(size));
                line.addAll(staff1.getNameBetweenLine(size));
                if (line.size() == 2) {
                    final String[] sfx = param._isChiben ? new String[]{shal[0], shal[1]} : new String[]{wide[0], wide[1]};
                    svf.VrsOutn(field + sfx[0], i, (String) line.get(0));
                    svf.VrsOutn(field + sfx[1], i, (String) line.get(1));
                } else {
                    final String[] sfx = param._isChiben ? shal : wide;
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        svf.VrsOutn(field + sfx[k], i, (String) line.get(k));
                    }
                }
                stampstaffcd = staff1._staffMst._staffcd;
            }
            final String img = getImageFile(param, stampstaffcd);
            if (img != null) {
                svf.VrsOutn(stamp, i, img); // 印
            }
        }

        /**
         * SVF-FORM 印刷処理 初期印刷
         */
        private void printSvfDefault(Vrw32alp svf, Param param) {
            try {
                svf.VrsOut("BIRTHDAY", setDateFormat2(null) + "生");
                svf.VrsOut("ENTERDATE", setDateFormat(null, param._year, param));
                svf.VrsOut("MOVEDATE", setDateFormat(null, param._year, param));
                svf.VrsOut("EXPULDATE1", setDateFormat(null, param._year, param));
                svf.VrsOut("EXPULDATE2", setDateFormat(null, param._year, param));
                svf.VrsOut("GRADDATE", setDateFormat(null, param._year, param));
                for (int i = 0; i < 3; i++) {
                    svf.VrsOutn("NENDO", i + 1, setNendoFormat(null, param._year, param));
                }
            } catch (Exception ex) {
                log.debug("printSvfDetail_5 error!", ex);
            }
        }

        /**
         * 写真データファイルの取得 --NO001
         */
        private String getImageFile(Param param, String filename) {
            String ret = null;
            if (param.DOCUMENTROOT != null && param.IMAGE1 != null && param.IMAGE2 != null) {
                String str = param.DOCUMENTROOT + "/" + param.IMAGE1 + "/" + filename + "." + param.IMAGE2;
                final File file1 = new File(str); // 写真データ存在チェック用
                if (file1.exists()) {
                    ret = str;
                }
            }
            return ret;
        }

        // --- 内部クラス -------------------------------------------------------
        /**
         * <<スタッフクラス>>。
         */
        private static class Staff {
            /**pkg*/ static Staff Null = new Staff(null, StaffMst.Null, null, null, null);
            final String _year;
            final StaffMst _staffMst;
            final String _dateFrom;
            final String _dateTo;
            final String _stampNo;
            public Staff(final String year, final StaffMst staffMst, final String dateFrom, final String dateTo, final String stampNo) {
                _year = year;
                _staffMst = staffMst;
                _dateFrom = dateFrom;
                _dateTo = dateTo;
                _stampNo = stampNo;
            }

            public String getStaffNameString() {
                final String fromDate = toYearDate(_dateFrom, _year);
                final String toDate = toYearDate(_dateTo, _year);
                final String name = null == _staffMst.getName(_year) ? "" : _staffMst.getName(_year);
                final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";
                return name + between;
            }

            public List getNameBetweenLine(final int size) {
                final String fromDate = toYearDate(_dateFrom, _year);
                final String toDate = toYearDate(_dateTo, _year);
                final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";
                
                final List rtn;
                if (retStringByteValue(getNameString() + between) > size) {
                    rtn = Arrays.asList(new String[]{getNameString(), between});
                } else {
                    rtn = Arrays.asList(new String[]{getNameString() + between});
                }
                return rtn;
            }

            public String getNameString() {
                final StringBuffer stb = new StringBuffer();
                final List name = _staffMst.getNameLine(_year, Integer.MAX_VALUE);
                for (int i = 0; i < name.size(); i++) {
                    if (null == name.get(i)) continue;
                    stb.append(name.get(i));
                }
                return stb.toString();
            }
            
            private String toYearDate(final String date, final String year) {
                if (null == date) {
                    return null;
                }
                final String sdate = year + "-04-01";
                final String edate = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
                if (date.compareTo(sdate) <= 0) {
                    return sdate;
                } else if (date.compareTo(edate) >= 0) {
                    return edate;
                }
                return date;
            }

            private String jpMonthName(final String date) {
                if (StringUtils.isBlank(date)) {
                    return "";
                }
                return new java.text.SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
            }

            public String toString() {
                return "Staff(year=" + _year + ", staffMst=" + _staffMst + ", dateFrom=" + _dateFrom + ", dateTo=" + _dateTo + ", stampNo="+ _stampNo + ")";
            }
        }

        private static class AfterGraduatedCourse {
            
            public static List loadTextList(final DB2UDB db2, final Param param, final Student student, final TreeSet yearSet) {
                String preSql = 
                    "select count(*) as COUNT from SYSIBM.SYSCOLUMNS "
                    + " where TBNAME = 'AFT_GRAD_COURSE_DAT' ";
                
                final String minYear = (String) yearSet.first();
                final String maxYear = (String) yearSet.last();
                
                // 進路用・就職用両方の最新の年度を取得
                final StringBuffer stb = new StringBuffer();
                stb.append("with TA as( select ");
                stb.append("         SCHREGNO, ");
                stb.append("         '0' as SCH_SENKOU_KIND, ");
                stb.append("         MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, ");
                stb.append("         '1' as COMP_SENKOU_KIND, ");
                stb.append("         MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR ");
                stb.append(" from ");
                stb.append("         AFT_GRAD_COURSE_DAT ");
                stb.append(" where ");
                stb.append("         SCHREGNO = '" + student._schregno + "' and PLANSTAT = '1' ");
                stb.append("         AND YEAR <='" + param._year + "' ");
                stb.append("         AND YEAR BETWEEN '" + minYear + "' AND '" + maxYear + "' ");
                stb.append(" group by ");
                stb.append("         SCHREGNO ");
                // 進路用・就職用どちらか(進路が優先)の最新の受験先種別コードを取得);
                stb.append("), TA2 as( select ");
                stb.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SENKOU_KIND, ");
                stb.append("     MAX(T1.SEQ) AS SEQ ");
                stb.append(" from ");
                stb.append("     AFT_GRAD_COURSE_DAT T1 ");
                stb.append(" inner join TA on ");
                stb.append("     T1.SCHREGNO = TA.SCHREGNO ");
                stb.append("     and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) ");
                stb.append("     and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) ");
                stb.append(" where ");
                stb.append("     T1.PLANSTAT = '1'");
                stb.append(" group by ");
                stb.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SENKOU_KIND ");
                stb.append(") ");
                // 最新の年度と受験先種別コードの感想を取得);
                stb.append("select  ");
                stb.append("      T1.SENKOU_KIND ");
                stb.append("     ,T1.STAT_CD ");
                stb.append("     ,T1.THINKEXAM ");
                stb.append("     ,T1.JOB_THINK ");
                stb.append("     ,L1.NAME1 as E017NAME1 ");
                stb.append("     ,L2.NAME1 as E018NAME1 ");
                stb.append("     ,L3.SCHOOL_NAME ");
                stb.append("     ,L5.FACULTYNAME ");
                stb.append("     ,L6.DEPARTMENTNAME ");
                stb.append("     ,L7.ADDR1 AS CAMPUSADDR1 ");
                stb.append("     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ");
                stb.append("     ,L4.COMPANY_NAME ");
                stb.append("     ,L4.ADDR1 AS COMPANYADDR1 ");
                stb.append("     ,L4.ADDR2 AS COMPANYADDR2 ");
                stb.append("from ");
                stb.append("     AFT_GRAD_COURSE_DAT T1 ");
                stb.append("inner join TA2 on ");
                stb.append("     T1.YEAR = TA2.YEAR ");
                stb.append("     and T1.SCHREGNO = TA2.SCHREGNO ");
                stb.append("     and T1.SENKOU_KIND = TA2.SENKOU_KIND ");
                stb.append("     and T1.SEQ = TA2.SEQ ");
                stb.append("left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD ");
                stb.append("left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD ");
                stb.append("left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD ");
                stb.append("left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD ");
                stb.append("     and L5.FACULTYCD = T1.FACULTYCD ");
                stb.append("left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD ");
                stb.append("     and L6.FACULTYCD = T1.FACULTYCD ");
                stb.append("     and L6.DEPARTMENTCD = T1.DEPARTMENTCD ");
                stb.append("left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD ");
                stb.append("     and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD ");
                stb.append("left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD ");
                stb.append("     and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD ");
                stb.append("left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD ");
                stb.append("where ");
                stb.append("     T1.PLANSTAT = '1' ");
                stb.append("order by ");
                stb.append("     T1.YEAR, T1.SCHREGNO ");
                final String sql = stb.toString();

                PreparedStatement ps = null, ps0 = null;
                ResultSet rs = null, rs0 = null;
                final List textList = new ArrayList();
                try {
                    // テーブルがあるか確認する。テーブルが無いなら後の処理を行わない。
                    ps0 = db2.prepareStatement(preSql);
                    rs0 = ps0.executeQuery();
                    int fieldCount = 0;
                    if (rs0.next()) {
                        fieldCount = rs0.getInt("COUNT");
                    }
                    if (fieldCount == 0) {
                        return textList;
                    }
                    
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if ("0".equals(rs.getString("SENKOU_KIND"))) { // 進学
                            if (null == rs.getString("STAT_CD") || null != rs.getString("E017NAME1")) {
                                final String[] token = KNJ_EditEdit.get_token(rs.getString("THINKEXAM"), 50, 10);
                                if (null != token) {
                                    textList.addAll(Arrays.asList(token));
                                }
                             } else {
                                 textList.add(StringUtils.defaultString(rs.getString("SCHOOL_NAME")));
                                 textList.add(StringUtils.defaultString(rs.getString("FACULTYNAME")) + StringUtils.defaultString(rs.getString("DEPARTMENTNAME")));
                                 textList.add(StringUtils.defaultString(rs.getString("CAMPUSFACULTYADDR1"), rs.getString("CAMPUSADDR1")));
                             }
                         } else if ("1".equals(rs.getString("SENKOU_KIND"))) { // 就職
                            if (null == rs.getString("STAT_CD") || null != rs.getString("E018NAME1")) {
                                final String[] token = KNJ_EditEdit.get_token(rs.getString("JOB_THINK"), 50, 10);
                                if (null != token) {
                                    textList.addAll(Arrays.asList(token));
                                }
                             } else {
                                textList.add(StringUtils.defaultString(rs.getString("COMPANY_NAME")));
                                textList.add(StringUtils.defaultString(rs.getString("COMPANYADDR1")));
                                textList.add(StringUtils.defaultString(rs.getString("COMPANYADDR2")));
                             }
                         }

                    }
                } catch (final Exception e) {
                    log.error("Exception", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
                return textList;
            }
        }
    }
    


    /**
     *
     *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中学校用（レイアウトは尚学中学版、データは千代田区立九段版を基に作成）
     *                                                    各教科・科目の学習の記録
     *
     *  2006/04/10 Build yamashiro
     *  2006/04/24 yamashiro・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加  --NO003
     *                            => 無い場合は従来通りHR_CLASSを出力
     */
    private static class KNJA131KFORM3 extends KNJA131K.BASE
    {
        private static final String KNJA131A = "KNJA131A";
        
        /**
         *  SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student)
        {
            final String form = KNJA131A.equals(param._prgid) ? "KNJA131_10_KYOAI.frm" : "KNJA131_10.frm";   
            boolean nonedata = false;
            final List entGrdHistList = student.getPrintSchregEntGrdHistList(param);
            try {
                for (final Iterator it = entGrdHistList.iterator(); it.hasNext();) {
                    svf.VrSetForm(form, 4);
                    final SchregEntGrdHistDat entGrdHist = (SchregEntGrdHistDat) it.next();
                    printRegd(db2, svf, param, student, entGrdHist);  //年次・ホームルーム・整理番号
                    printRemark(db2, svf, param, student, entGrdHist);  //総合的な学習の時間の記録・総合所見
                    if (KNJA131A.equals(param._prgid)) {
                        final Form3 form3 = new Form3();
                        form3.printHyotei(db2, svf, param, student, entGrdHist);
                        if (form3.printKanten(db2, svf, param, student, entGrdHist)) {
                            nonedata = true;
                        }
                    } else {
                        printHyotei(db2, svf, param, student, entGrdHist);  //評定
                        if (printKanten(db2, svf, param, student, entGrdHist)) {
                            nonedata = true;  //観点 ＊ココでsvf.VrEndRecord()
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug( "printSvf error!", ex );
            }
            return nonedata;
        }
        
        private static class Form3 {
            
            private static final int VIEW_LINE1_MAX = 49;
            private static final int VIEW_LINE2_MAX = 49;
            
            /**
             *  観点出力処理
             *
             */
            public boolean printKanten(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist) {

                if (param._isChiben) {
                    svf.VrsOut("ITEM_SELECT", "必修教科");
                }

                boolean nonedata = false;
                final List classViewList = ClassViewSub.load(db2, param, student);
                try {
                    final List leftList = new ArrayList();
                    final List rightList = new ArrayList();
                    
                    int olde = 0;
                    int line1 = 0;
                    int line2 = 0;
                    
                    for (final Iterator it = classViewList.iterator(); it.hasNext();) {
                        final ClassViewSub classview = (ClassViewSub) it.next();
                        final List inlist;
                        final int classnamelen = null == classview._classname ? 0 : classview._classname.length();;
                        final int viewnum = Math.max(classnamelen, classview._views.size());
                        final int maxlen = Math.max(classnamelen, classview._views.size() + 1);
                        if (param._isChiben) {
                            final int e;
                            if (classview._e == 2) {
                                continue; // 智辯は選択教科を表示しない
                            } else if (VIEW_LINE1_MAX < line1 + viewnum || olde == 2) {
                                log.debug(" line1 = " + line1 + " , " + viewnum);
                                e = 2;
                                inlist = VIEW_LINE2_MAX < line2 + viewnum ? null : rightList;
                                line2 += maxlen;
                            } else {
                                e = 1;
                                inlist = leftList;
                                line1 += maxlen;
                            }
                            olde = e;
                        } else {
                            if (classview._e == 2) {
                                inlist = VIEW_LINE2_MAX < line2 + viewnum ? null : rightList;
                                line2 += maxlen;
                            } else {
                                inlist = VIEW_LINE1_MAX < line1 + viewnum ? null : leftList;
                                line1 += maxlen;
                            }
                        }
                        if (null != inlist) {
                            inlist.add(classview);
                        }
                    }
                    if (printKantenList(svf, param, student, entGrdHist, leftList, 1)) nonedata = true;
                    if (printKantenList(svf, param, student, entGrdHist, rightList, 2)) nonedata = true;
                    if (!nonedata) {  // --> データが１件も無い場合の処理
                        svf.VrsOut("CLASSCD1", "A");  //教科コード
                        svf.VrEndRecord();
                        nonedata = true;
                    }
                } catch (Exception ex) {
                    log.error( "printSvfDetail_1 error!", ex);
                }
                return nonedata;
            }

            private boolean printKantenList(final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist, final List classViewList, final int e) {
                int line = 0;
                boolean nonedata = false;
                String oldclasscd = "";
                for (final Iterator it = classViewList.iterator(); it.hasNext();) {
                    final ClassViewSub classview = (ClassViewSub) it.next();
                    final String classname = classview.setClassname(classview._classname);  // 教科名のセット
                    int i = 0;  //教科名称カウント用変数を初期化
                    String classcd = classview._classcd;
                    if (oldclasscd.equals(classview._classcd)) {
                        if (StringUtils.isNumeric(classview._classcd)) {
                            classcd = new DecimalFormat("00").format(Integer.parseInt(classview._classcd) + 1);
                        } else {
                            classcd = "00";
                        }
                    }
                    
                    for (final Iterator it2 = classview._views.iterator(); it2.hasNext();) {
                        final ViewSub view = (ViewSub) it2.next();
                        
                        if (i < classname.length()) {
                            svf.VrsOut("CLASS" + e, String.valueOf(classname.charAt(i)));  //教科名称
                            i++;
                        }
                        for (final Iterator itv = view._views.iterator(); itv.hasNext();) {
                            final View v = (View) itv.next();
                            final String year = v._year;
                            if (student.yearIsOutLimit(year, param)) {
                                continue;
                            }
                            if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                                // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                                continue;
                            }
                            svf.VrsOut("ASSESS" + e + "_" + v._g, v._view);  //観点
                        }
                        svf.VrsOut("CLASSCD" + e, classcd);  //教科コード
                        svf.VrsOut("VIEW" + e, view._viewname);  //観点名称
                        svf.VrEndRecord();
                        line++;
                    }
                    //観点別学習状況の教科名出力処理（教科の変わり目）
                    if (i < classname.length()) {
                        for (int k = i; k < classname.length(); k+= 1) {
                            line++;
                            svf.VrsOut("CLASSCD" + e, classcd);  //教科コード
                            svf.VrsOut("CLASS" + e, classname.substring(k));  // 教科名称
                            svf.VrEndRecord();
                            nonedata = true;
                        }
                    } else {
                        if (1 == ((line % VIEW_LINE1_MAX == 0) ? -1 : 1)) { // 行数がオーバーしない場合、レコードを印刷
                            line++;
                            svf.VrsOut("CLASSCD" + e, classcd);  // 教科コード
                            svf.VrEndRecord();
                            nonedata = true;
                        }
                    }
                    oldclasscd = classcd;
                }
                return nonedata;
            }
            
            /**
             *  評定出力処理
             */
            public void printHyotei(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist) {
                int line1 = 0;  //欄の出力行数
                int line2 = 0;
                final int MAX_LINE = 10;
                final List valueRecordList = ValueRecord.load(db2, param, student);
                for (final Iterator it = valueRecordList.iterator(); it.hasNext();) {
                    final ValueRecord valueRecord = (ValueRecord) it.next();
                    if (param._isChiben && "1".equals(valueRecord._electDiv)) {
                        continue; // 智辯は選択教科を表示しない。
                    }
                    final boolean isPrintRight = param._isChiben && line1 >= MAX_LINE || "1".equals(valueRecord._electDiv);
                    final int line;
                    if (isPrintRight) {
                        line2 += 1;
                        line = line2;
                    } else {
                        line1 += 1;
                        line = line1;
                    }
                    if (null != valueRecord._className) { //教科名出力
                        printSvfSubjectName(svf, valueRecord._className, line, isPrintRight);  //教科名出力
                    }
                    for (final Iterator itv = valueRecord._values.iterator(); itv.hasNext();) {
                        final Value v = (Value) itv.next();
                        final String year = v._year;
                        if (student.yearIsOutLimit(year, param)) {
                            continue;
                        }
                        if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                            // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                            continue;
                        }
                        if (v._value != null) {
                            final String value;
                            if ("1".equals(valueRecord._electDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                                if ("11".equals(v._value)) {
                                    value = "A";
                                } else if ("22".equals(v._value)) {
                                    value = "B";
                                } else if ("33".equals(v._value)) {
                                    value = "C";
                                } else {
                                    value = v._value;
                                }
                            } else {
                                value = v._value;
                            }
                            svf.VrsOutn( "ASSESS" + (isPrintRight ? 4 : 3) + "_" + v._g, line, value);  //評定
                        }
                    }
                }
            }
            
            /**
             * 評定データ
             */
            private static class ValueRecord {
                final String _classCd;
                final String _electDiv;
                final String _className;
                final List _values;
                public ValueRecord(
                        final String classCd, 
                        final String electDiv, 
                        final String className) {
                    _classCd = classCd;
                    _electDiv = electDiv;
                    _className = className;
                    _values = new ArrayList();
                }
                
                public static List load(final DB2UDB db2, final Param param, final Student student) {
                    final List valueRecordList = new ArrayList();
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        /**
                         *  priparedstatement作成  成績データ（評定）
                         */
                        final StringBuffer stb = new StringBuffer();
                        stb.append("WITH ");
                        //評定の表
                        stb.append(" VALUE_DATA AS( ");
                        stb.append("   SELECT ");
                        stb.append("        ANNUAL ");
                        stb.append("       ,CLASSCD ");
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("       ,SCHOOL_KIND ");
                            stb.append("       ,CURRICULUM_CD ");
                        }
                        stb.append("       ,SUBCLASSCD ");
                        stb.append("       ,YEAR ");
                        stb.append("       ,VALUATION AS VALUE ");
                        stb.append("   FROM ");
                        stb.append("       SCHREG_STUDYREC_DAT T1 ");
                        stb.append("   WHERE ");
                        stb.append("       T1.SCHREGNO = '" + student._schregno + "' ");
                        stb.append("       AND T1.YEAR <= '" + param._year + "' ");
                        stb.append(" ) ");
                        
                        //学籍の表
                        stb.append(",SCHREG_DATA AS( ");
                        stb.append("  SELECT ");
                        stb.append("      YEAR ");
                        stb.append("     ,ANNUAL  ");
                        stb.append("     ,GRADE  ");
                        stb.append("  FROM ");
                        stb.append("     SCHREG_REGD_DAT ");
                        stb.append("  WHERE ");
                        stb.append("      SCHREGNO = '" + student._schregno + "' ");
                        stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
                        stb.append("                 FROM    SCHREG_REGD_DAT  ");
                        stb.append("                 WHERE   SCHREGNO = '" + student._schregno + "' ");
                        stb.append("                     AND YEAR <= '" + param._year + "' ");
                        stb.append("                 GROUP BY GRADE) ");
                        stb.append("  GROUP BY ");
                        stb.append("      YEAR ");
                        stb.append("      ,ANNUAL ");
                        stb.append("      ,GRADE ");
                        stb.append(") ");
                        
                        //メイン表
                        stb.append("SELECT ");
                        stb.append("     T2.YEAR ");
                        stb.append("    ,T2.GRADE ");
                        stb.append("    ,T3.ELECTDIV ");
                        stb.append("    ,T3.CLASSCD ");
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("       ,T5.SCHOOL_KIND ");
                            stb.append("       ,T5.CURRICULUM_CD ");
                        }
                        stb.append("    ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
                        stb.append("    ,CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
                        stb.append("    ,T5.VALUE ");
                        stb.append("FROM  SCHREG_DATA T2 ");
                        stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
                        stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
                        stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("       AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
                        }
                        stb.append("ORDER BY ");
                        stb.append("    SHOWORDERCLASS, ");
                        stb.append("    T5.CLASSCD, ");
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("       T5.SCHOOL_KIND, ");
                            stb.append("       T5.CURRICULUM_CD, ");
                        }
                        stb.append("    T3.ELECTDIV, ");
                        stb.append("    T2.GRADE ");
                        
                        log.debug(" valrec sql = " + stb.toString());

                        final String sql = stb.toString();
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        
                        while (rs.next()) {
                            //教科コードの変わり目
                            final String year = rs.getString("YEAR");
                            if (null == year) {
                                continue;
                            }
                            final int g = param.getGradeCd(year, rs.getString("GRADE")); // 学年
                            final String electDiv = rs.getString("ELECTDIV");
                            final String classCd;
                            if ("1".equals(param._useCurriculumcd)) {
                                classCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                            } else {
                                classCd = rs.getString("CLASSCD");
                            }
                            final String className = rs.getString("CLASSNAME");
                            //評定出力
                            final String value = rs.getString("VALUE");
                            
                            ValueRecord valueRecord = null;
                            
                            for (final Iterator it = valueRecordList.iterator(); it.hasNext();) {
                                ValueRecord vr = (ValueRecord) it.next();
                                if (vr._classCd != null && vr._classCd.equals(classCd)) {
                                    valueRecord = vr;
                                    break;
                                }
                            }
                            
                            if (null == valueRecord) {
                                valueRecord = new ValueRecord(classCd, electDiv, className);
                                valueRecordList.add(valueRecord);
                            }
                            valueRecord._values.add(new Value(year, g, value));
                        }
                    } catch (Exception ex) {
                        log.error("printSvfDetail_1 error!", ex);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                    return valueRecordList;
                }
            }
            
            private static class Value {
                final String _year;
                final int _g;
                final String _value; //評定
                Value(
                        final String year,
                        final int g,
                        final String value) {
                    _year = year;
                    _g = g;
                    _value = value;
                }
            }
            
            /**
             * 観点の教科（JVIEWNAME_SUB_MST、JVIEWSTAT_SUB_DAT）
             */
            private static class ClassViewSub {
                final String _classcd;  //教科コード
                final String _classname;  //教科名称
                final int _e;
                final List _views;
                
                public ClassViewSub(
                        final String classcd, 
                        final String classname, 
                        final int e
                ) {
                    _classcd = classcd;
                    _classname = classname;
                    _e = e;
                    _views = new ArrayList();
                }
                
                public void addView(final ViewSub view) {
                    _views.add(view);
                }
                
                public int getViewNum() {
                    int c = 0;
                    String viewcdOld = "";
                    for (Iterator it = _views.iterator(); it.hasNext();) {
                        final ViewSub view = (ViewSub) it.next();
                        if (view._viewcd != null && !viewcdOld.equals(view._viewcd)) {
                            c += 1;
                            viewcdOld = view._viewcd;
                        }
                    }
                    return c;
                }
                
                // 教科名のセット
                private String setClassname(final String classname) {
                    if (classname == null) {
                        return "";
                    }
                    final int viewnum = getViewNum();
                    if (viewnum == 0) {
                        return classname;
                    }
                    final int newviewnum = (classname.length() <= viewnum) ? viewnum + 1 : viewnum;  // 教科間の観点行に１行ブランクを挿入
                    final String newclassname;
                    
                    if (classname.length() < newviewnum) {
                        final int i = (newviewnum - classname.length()) / 2 - ((newviewnum - classname.length()) % 2 == 0 ? 1 : 0);
                        String space = "";
                        for (int j = 0; j < i; j++) {
                            space = " " + space;
                        }  // 教科名のセンタリングのため、空白を挿入
                        newclassname = space + classname;
                    } else {
                        newclassname = classname;
                    }
                    return newclassname;
                }
                
                public String toString() {
                    return "[" + _classcd + ":" + _classname + " e = " + _e + "]";
                }
                
                private static ClassViewSub getClassViewSub(final List classViewList, final String classcd, final String classname, final int e) {
                    ClassViewSub classView = null;
                    for (final Iterator it = classViewList.iterator(); it.hasNext();) {
                        final ClassViewSub classView0 = (ClassViewSub) it.next();
                        if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._e == e) {
                            classView = classView0;
                            break;
                        }
                    }
                    return classView;
                }

                private static ViewSub getViewSub(final List viewSubList, final String subclasscd, final String viewcd) {
                    ViewSub viewSub = null;
                    for (final Iterator it = viewSubList.iterator(); it.hasNext();) {
                        final ViewSub viewSub0 = (ViewSub) it.next();
                        if (viewSub0._subclasscd.equals(subclasscd) && viewSub0._viewcd.equals(viewcd)) {
                            viewSub = viewSub0;
                            break;
                        }
                    }
                    return viewSub;
                }

                /**
                 * 観点のリストを得る
                 * @param db2
                 * @param param
                 * @param schregno
                 * @return
                 */
                public static List load(final DB2UDB db2, final Param param, final Student student) {
                    final List classViewList = new ArrayList();
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        final String sql = getViewRecordSql(param, student);
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        
                        while (rs.next()) {
                            //教科コードの変わり目
                            final String year = rs.getString("YEAR");
                            if (null == year) {
                                continue;
                            }
                            final String classcd;
                            if ("1".equals(param._useCurriculumcd)) {
                                classcd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                            } else {
                                classcd = rs.getString("CLASSCD");
                            }
                            final String classname = rs.getString("CLASSNAME");
                            final String subclasscd;
                            if ("1".equals(param._useCurriculumcd)) {
                                subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                            } else {
                                subclasscd = rs.getString("SUBCLASSCD");
                            }
                            final String viewcd = rs.getString("VIEWCD");
                            final String viewname = rs.getString("VIEWNAME");
                            final String status = rs.getString("STATUS");
                            final int e = "1".equals(rs.getString("ELECTDIV")) ? 2 : 1;  //必修:1 選択:2
                            final int g = param.getGradeCd(year, rs.getString("GRADE")); // 学年
                            
                            ClassViewSub classViewSub = getClassViewSub(classViewList, classcd, classname, e);
                            if (null == classViewSub) {
                                classViewSub = new ClassViewSub(classcd, classname, e);
                                classViewList.add(classViewSub);
                            }
                            ViewSub viewSub = getViewSub(classViewSub._views, subclasscd, viewcd);
                            if (null == viewSub) {
                                viewSub = new ViewSub(subclasscd, viewcd, viewname);
                                classViewSub.addView(viewSub);
                            }
                            View view = new View(year, g, status);
                            viewSub._views.add(view);
                        }
                    } catch (Exception ex) {
                        log.error("printSvfDetail_1 error!", ex);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                    return classViewList;
                }
                
                /**
                 *  priparedstatement作成  成績データ（観点）
                 */
                private static String getViewRecordSql(final Param param, final Student student) {
                    
                    final StringBuffer stb = new StringBuffer();
                    stb.append("WITH ");
                    //観点の表
                    stb.append("VIEW_DATA AS( ");
                    stb.append("  SELECT ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("      CLASSCD, ");
                        stb.append("      SCHOOL_KIND, ");
                        stb.append("      CURRICULUM_CD, ");
                    }
                    stb.append("      SUBCLASSCD ");
                    stb.append("     ,VIEWCD ");
                    stb.append("     ,YEAR ");
                    stb.append("     ,STATUS ");
                    stb.append("  FROM ");
                    stb.append("     JVIEWSTAT_SUB_DAT T1 ");
                    stb.append("  WHERE ");
                    stb.append("     T1.SCHREGNO = '" + student._schregno + "' ");
                    stb.append("    AND T1.YEAR <= '" + param._year + "' ");
                    stb.append("    AND T1.SEMESTER = '9' ");
                    stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
                    stb.append(") ");
                    
                    //学籍の表
                    stb.append(",SCHREG_DATA AS( ");
                    stb.append("  SELECT  YEAR ");
                    stb.append(         ",GRADE  ");
                    stb.append("  FROM    SCHREG_REGD_DAT  ");
                    stb.append("  WHERE   SCHREGNO = '" + student._schregno + "'  ");
                    stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
                    stb.append("                 FROM    SCHREG_REGD_DAT  ");
                    stb.append("                 WHERE   SCHREGNO = '" + student._schregno + "' ");
                    stb.append("                     AND YEAR <= '" + param._year + "' ");
                    stb.append("                 GROUP BY GRADE)  ");
                    stb.append("  GROUP BY YEAR,GRADE  ");
                    stb.append(") ");
                    
                    //メイン表
                    stb.append("SELECT ");
                    stb.append("    T2.YEAR ");
                    stb.append("   ,T2.GRADE ");
                    stb.append("   ,T3.ELECTDIV ");
                    stb.append("   ,T3.CLASSCD");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("  ,T2.CLASSCD ");
                        stb.append("  ,T2.SCHOOL_KIND ");
                        stb.append("  ,T2.CURRICULUM_CD ");
                    }
                    stb.append("   ,T2.SUBCLASSCD");
                    stb.append("   ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
                    stb.append("   ,CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
                    stb.append("   ,T2.VIEWCD ");
                    stb.append("   ,T2.VIEWNAME ");
                    stb.append("   ,T1.STATUS ");
                    stb.append("FROM  ( SELECT ");
                    stb.append("            W2.YEAR ");
                    stb.append("          , W2.GRADE ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("          , W1.CLASSCD ");
                        stb.append("          , W1.SCHOOL_KIND ");
                        stb.append("          , W1.CURRICULUM_CD ");
                    }
                    stb.append("          , W1.SUBCLASSCD ");
                    stb.append("          , W1.VIEWCD ");
                    stb.append("          , VIEWNAME ");
                    stb.append("          , CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
                    stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
                    stb.append("               ,SCHREG_DATA W2 ");
                    stb.append("        WHERE W1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
                    stb.append("      ) T2 ");
                    stb.append("INNER JOIN CLASS_MST T3 ON ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("    T3.CLASSCD = T2.CLASSCD AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
                    } else {
                        stb.append("    T3.CLASSCD = SUBSTR(T2.VIEWCD,1,2)  ");
                    }
                    stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("    AND T1.CLASSCD = T2.CLASSCD  ");
                        stb.append("    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND  ");
                        stb.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD  ");
                    }
                    stb.append("    AND T1.SUBCLASSCD = T2.SUBCLASSCD  ");
                    stb.append("    AND T1.VIEWCD = T2.VIEWCD  ");
                    stb.append("ORDER BY ");
                    stb.append("    VALUE(SHOWORDERCLASS, -1), ");
                    stb.append("    T3.CLASSCD, ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("  T2.SCHOOL_KIND, ");
                        stb.append("  T2.CURRICULUM_CD, ");
                    }
                    stb.append("    VALUE(T3.ELECTDIV, '0'), ");
                    stb.append("    VALUE(T2.SHOWORDERVIEW, -1), ");
                    stb.append("    T2.VIEWCD, ");
                    stb.append("    T2.GRADE ");
                    return stb.toString();
                }
            }
            
            /**
             * 観点データ
             */
            private static class ViewSub {
                final String _subclasscd; // 科目コード
                final String _viewcd;  //観点コード
                final String _viewname;  //観点コード
                final List _views;

                public ViewSub(
                        final String subclasscd,
                        final String viewcd, 
                        final String viewname
                ) {
                    _subclasscd = subclasscd;
                    _viewcd = viewcd;
                    _viewname = viewname;
                    _views = new ArrayList();
                }
            }
            
            private static class View {
                final String _year;
                final int _g;
                final String _view; // 観点
                public View(
                        final String year,
                        final int g,
                        final String view) {
                    _year = year;
                    _g = g;
                    _view = view;
                }
            }
        }

        /**
         *  SVF-FORM 印刷処理 明細 観点出力処理
         *
         */
        private boolean printKanten(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
        {
            final int VIEW_LINE1_MAX = 46;
            final int VIEW_LINE2_MAX = 46;
            
            boolean nonedata = false;
            List classViews = getClassViews(db2, param, student);

            if (param._isChiben) {
                svf.VrsOut("ITEM_SELECT", "必修教科");
            }
            
            try {
                final List leftList = new ArrayList();
                final List rightList = new ArrayList();
                int olde = 0;
                int line1 = 0;
                int line2 = 0;
                
                for (final Iterator it = classViews.iterator(); it.hasNext();) {
                    final ClassView classview = (ClassView) it.next();
                    final List inlist;
                    final int classnamelen = null == classview._classname ? 0 : classview._classname.length();
                    final int viewnum = Math.max(classnamelen, classview.getViewNum());
                    final int maxlen = Math.max(classnamelen, viewnum + 1);
                    if (param._isChiben) {
                        final int e;
                        if (classview._e == 2) {
                            continue; // 智辯は選択教科を表示しない
                        } else if (VIEW_LINE1_MAX < line1 + viewnum || olde == 2) {
                            e = 2;
                            inlist = VIEW_LINE2_MAX < line2 + viewnum ? null : rightList;
                            line2 += maxlen;
                        } else {
                            e = 1;
                            inlist = leftList;
                            line1 += maxlen;
                        }
                        olde = e;
                    } else {
                        if (classview._e == 2) {
                            inlist = VIEW_LINE2_MAX < line2 + viewnum ? null : rightList;
                            line2 += maxlen;
                        } else {
                            inlist = VIEW_LINE1_MAX < line1 + viewnum ? null : leftList;
                            line1 += maxlen;
                        }
                    }
                    if (null != inlist) {
                        inlist.add(classview);
                    }
                }
                if (printList(svf, param, student, entGrdHist, nonedata, leftList, 1, VIEW_LINE1_MAX)) nonedata = true;
                if (printList(svf, param, student, entGrdHist, nonedata, rightList, 2, VIEW_LINE2_MAX)) nonedata = true;
                if (!nonedata) {  // --> データが１件も無い場合の処理
                    svf.VrsOut("CLASSCD1", "A");  //教科コード
                    svf.VrEndRecord();
                    nonedata = true;
                }
            } catch (Exception ex) {
                log.debug( "printSvfDetail_1 error!", ex);
            }
            return nonedata;
        }


        private boolean printList(final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist, boolean nonedata, final List inList, final int e, final int VIEW_LINE_MAX) {
            int line = 0;
            String oldclasscd = "";
            for (final Iterator it = inList.iterator(); it.hasNext();) {
                final ClassView classview = (ClassView) it.next();
                final String classname = classview.setClassname(classview._classname, classview._viewnum);  // 教科名のセット
                int classnamecount = 0;  //教科名称カウント用変数を初期化
                String viewcd = null;  //保管用観点コードを初期化
                String classcd = classview._classcd;
                if (oldclasscd.equals(classview._classcd)) {
                    if (StringUtils.isNumeric(classview._classcd)) {
                        classcd = new DecimalFormat("00").format(Integer.parseInt(classview._classcd) + 1);
                    } else {
                        classcd = "00";
                    }
                }
                
                for (final Iterator it2 = classview._views.iterator(); it2.hasNext();) {
                    final View view = (View) it2.next();
                    
                    //観点コードの変わり目
                    if (viewcd == null || !viewcd.equals(view._viewcd)) {
                        if (viewcd != null) {
                            if (line < VIEW_LINE_MAX) {  // --> 行数チェックがＯＫならレコードを出力
                                line++;
                                svf.VrEndRecord();
                                nonedata = true;
                            }
                        }
                        viewcd = view._viewcd;  //観点コードの保管
                        if (classnamecount < classname.length()) {
                            svf.VrsOut( "CLASS" + e, classname.substring(classnamecount, classnamecount + 1));  //教科名称
                            classnamecount++;
                        }
                        svf.VrsOut("CLASSCD" + e, classcd);  //教科コード
                        svf.VrsOut("VIEW" + e, view._viewname);  //観点名称
                    }
                    final int g = view._g; // 学年
                    final String year = view._year;
                    if (student.yearIsOutLimit(year, param)) {
                        continue;
                    }
                    if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                        // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                        continue;
                    }
                    svf.VrsOut("ASSESS" + e + "_" + g, view._view);  //観点
                }
                
                try {
                    if( viewcd != null) {
                        if(line < VIEW_LINE_MAX){  //行数がオーバーしない場合、レコードを印刷
                            line++;
                            svf.VrEndRecord();
                            nonedata = true;
                        }
                    }
                    if( classnamecount < classname.length()) {
                        for(int ci = classnamecount; ci < classname.length(); ci++) {
                            if(line < VIEW_LINE_MAX){  //行数がオーバーしない場合、レコードを印刷
                                line++;
                                svf.VrsOut( "CLASSCD" + e, classcd );  //教科コード
                                svf.VrsOut( "CLASS" + e, classname.substring(ci, ci + 1));  //教科名称
                                svf.VrEndRecord();
                                nonedata = true;
                            }
                        }
                    } else {
                        if(line < VIEW_LINE_MAX){  //行数がオーバーしない場合、レコードを印刷
                            line++;
                            svf.VrsOut( "CLASSCD" + e, classcd );  //教科コード
                            svf.VrEndRecord();
                            svf.VrEndRecord();
                            nonedata = true;
                        }
                    }
                } catch (Exception ex) {
                    log.debug( "printSvfDetail_1Sub1 error!", ex );
                }
                oldclasscd = classcd;
            }
            return nonedata;
        }
        

        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private String prestatViewRecord(final Param param, final Student student)
        {
            StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //観点の表
            stb.append("VIEW_DATA AS( ");
            stb.append("SELECT  VIEWCD, YEAR, STATUS AS VIEW ");
            stb.append("FROM    JVIEWSTAT_DAT T1 ");
            stb.append("WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append(    "AND T1.YEAR <= '" + param._year + "' ");
            stb.append(    "AND T1.SEMESTER = '9' ");
            stb.append(    "AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(") ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("SELECT  YEAR,GRADE  ");
            stb.append("FROM    SCHREG_REGD_DAT  ");
            stb.append("WHERE   SCHREGNO = '" + student._schregno + "' ");
            stb.append(    "AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append(                 "FROM    SCHREG_REGD_DAT  ");
            stb.append(                 "WHERE   SCHREGNO = '" + student._schregno + "' ");
            stb.append(                     "AND YEAR <= '" + param._year + "' ");
            stb.append(                 "GROUP BY GRADE)  ");
            stb.append("GROUP BY YEAR,GRADE  ");
            stb.append(") ");

            //教科別観点の数の表
            stb.append(",VIEWNUM_DATA AS(");
            stb.append("SELECT  SUBSTR(W1.VIEWCD,1,2) AS CLASSCD, COUNT(*) AS VIEWNUM ");
            stb.append("FROM    JVIEWNAME_MST W1 ");
            stb.append("GROUP BY SUBSTR(W1.VIEWCD,1,2) ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  T2.YEAR, T2.GRADE, T3.ELECTDIV, T3.CLASSCD");
            stb.append(       ",CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append(       ",CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
            stb.append(       ",T2.VIEWCD,T2.VIEWNAME ");
            stb.append(       ",T1.VIEW ");
            stb.append(       ",T6.VIEWNUM ");
            stb.append("FROM  ( SELECT  W2.YEAR,W2.GRADE,W1.VIEWCD,VIEWNAME ");
            stb.append(               ",CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
            stb.append(        "FROM    JVIEWNAME_MST W1 ");
            stb.append(               ",SCHREG_DATA W2 ");
            stb.append(      ")T2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("INNER JOIN (SELECT T1.CLASSCD, MIN(T1.CLASSCD || T1.SCHOOL_KIND) AS CL_SCHK_CURCD FROM CLASS_MST T1 ");
                stb.append("    GROUP BY T1.CLASSCD ) TT3 ON TT3.CLASSCD = SUBSTR(T2.VIEWCD,1,2)");
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD || T3.SCHOOL_KIND = TT3.CL_SCHK_CURCD ");

            } else {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.VIEWCD,1,2)  ");
            }
            stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR AND T1.VIEWCD = T2.VIEWCD  ");
            stb.append("LEFT JOIN VIEWNUM_DATA T6 ON T6.CLASSCD = SUBSTR(T2.VIEWCD,1,2) ");
            stb.append("ORDER BY SHOWORDERCLASS, SHOWORDERVIEW, T2.VIEWCD, T2.GRADE ");
            return stb.toString();
        }
        
        private List getClassViews(final DB2UDB db2, final Param param, final Student student) {
            List classViews = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //成績データ（観点）
                ps = db2.prepareStatement(prestatViewRecord(param, student));
                rs = ps.executeQuery();

                while( rs.next()) {
                    //教科コードの変わり目
                    final String year = rs.getString("YEAR");
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    final int viewnum = rs.getInt("VIEWNUM");
                    final String rsView = rs.getString("VIEW");
                    final int e = "1".equals(rs.getString("ELECTDIV")) ? 2: 1;  //必修:1 選択:2
                    final int g = Integer.parseInt( rs.getString("GRADE") ); // 学年
                    
                    ClassView classView = null;
                    for (Iterator it = classViews.iterator(); it.hasNext();) {
                        ClassView classView0 = (ClassView) it.next();
                        if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._e == e) {
                            classView = classView0;
                            break;
                        }
                    }
                    if (null == classView) {
                        classView = new ClassView(classcd, classname, viewnum, e);
                        classViews.add(classView);
                    }
                    final View view = new View(year, viewcd, viewname, rsView, g);
                    classView.addView(view);
                }
            } catch (Exception ex) {
                log.debug( "printSvfDetail_1 error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return classViews;
        }

        /**
         * 評定
         */
        private class ClassValuation {
            final String _electdiv;
            final String _classcd;
            final String _classname;
            final List _valuationList;
            ClassValuation(
                    final String electdiv,
                    final String classcd,
                    final String classname) {
                _electdiv = electdiv;
                _classcd = classcd;
                _classname = classname;
                _valuationList = new ArrayList();
            }
        }

        /**
         * 評定
         */
        private class Valuation {
            final String _year;
            final String _grade;
            final String _value;
            Valuation(
                    final String year,
                    final String grade,
                    final String value) {
                _year = year;
                _grade = grade;
                _value = value;
            }
        }

        /**
         * 観点の教科
         */
        private class ClassView {
            final String _classcd;  //教科コード
            final String _classname;  //教科名称
            final int _viewnum;  //教科名称
            final int _e;
            final List _views;

            public ClassView(
                    final String classcd, 
                    final String classname, 
                    final int viewnum,
                    final int e
            ) {
                _classcd = classcd;
                _classname = classname;
                _viewnum = viewnum;
                _e = e;
                _views = new ArrayList();
            }
            
            public int getViewNum() {
                final Set viewCd = new HashSet();
                for (final Iterator it = _views.iterator(); it.hasNext();) {
                    final View view = (View) it.next();
                    viewCd.add(view._viewcd);
                }
                return viewCd.size();
            }

            public void addView(View view) {
                _views.add(view);
            }
            
            // 教科名のセット
            private String setClassname(String classname, int viewnum) {
                if (classname == null) { return ""; }
                if (viewnum == 0) { return classname; }
                
                int classnamelen = classname.length();
                if (classnamelen <= viewnum) viewnum++;  // 教科間の観点行に１行ブランクを挿入
                if (classnamelen < viewnum) {
                    int i = (viewnum - classnamelen) / 2;
                    for (int j = 0; j < i; j++) { classname = " " + classname; }  // 教科名のセンタリングのため、空白を挿入
                }
                
                return classname;
            }
            
            public String toString() {
                return "[" + _classcd + ":" + _classname + " e = " + _e + "]";
            }
        }
        
        /**
         * 観点データ
         */
        private class View {
            final String _year;
            final String _viewcd;  //観点コード
            final String _viewname;  //観点コード
            final String _view; //観点
            final int _g; // 学年

            public View(
                    final String year,
                    final String viewcd, 
                    final String viewname,
                    final String view,
                    final int g
            ) {
                _year = year;
                _viewcd = viewcd;
                _viewname = viewname;
                _view = view;
                _g = g;
            }
        }

        /**
         *  SVF-FORM 印刷処理 評定出力処理
         *
         */
        private void printHyotei(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
        {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //成績データ（評定）
                /**
                 *  priparedstatement作成  成績データ（評定）
                 */
                StringBuffer stb = new StringBuffer();
                stb.append("WITH ");

                //評定の表
                stb.append(" VALUE_DATA AS( ");
                stb.append(" SELECT  CLASSCD, YEAR, MAX(VALUATION) AS VALUE ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" ,SCHOOL_KIND ");
                }
                stb.append(" FROM    SCHREG_STUDYREC_DAT T1 ");
                stb.append(" WHERE   T1.SCHREGNO = '" + student._schregno + "' AND T1.YEAR <= '" + param._year + "' ");
                stb.append(" GROUP BY YEAR, CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" ,SCHOOL_KIND ");
                }
                stb.append(" ) ");
                stb.append(",VALUE_DATA2 AS( ");
                stb.append(" SELECT T1.CLASSCD,T1.YEAR,CASE WHEN VALUE(T2.ELECTDIV,'0') = '1' THEN T3.NAME1 ELSE CHAR(T1.VALUE) END AS VALUE");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" ,T1.SCHOOL_KIND ");
                }
                stb.append(" FROM VALUE_DATA T1");
                stb.append(" INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'D001' AND NAMECD2 = CHAR(VALUE)");
                stb.append(" ) ");

                //学籍の表
                stb.append(",SCHREG_DATA AS( ");
                stb.append("SELECT  T1.YEAR, T1.GRADE, T2.SCHOOL_KIND  ");
                stb.append("FROM    SCHREG_REGD_DAT T1  ");
                stb.append("INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append("WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
                stb.append(    "AND T1.YEAR IN (SELECT  MAX(YEAR)  ");
                stb.append(                 "FROM    SCHREG_REGD_DAT  ");
                stb.append(                 "WHERE   SCHREGNO = '" + student._schregno + "' ");
                stb.append(                     "AND YEAR <= '" + param._year + "' ");
                stb.append(                 "GROUP BY GRADE)  ");
                stb.append("GROUP BY T1.YEAR, T1.GRADE, T2.SCHOOL_KIND  ");
                stb.append(") ");

                //メイン表
                stb.append("SELECT  T2.YEAR, T2.GRADE, T3.ELECTDIV, T3.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" ,T3.SCHOOL_KIND ");
                }
                stb.append(       ",CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
                stb.append(       ",CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
                stb.append(       ",T5.VALUE ");
                stb.append("FROM  ( SELECT  W2.YEAR, W2.GRADE, W2.SCHOOL_KIND, W1.CLASSCD ");
                stb.append(        "FROM   (SELECT  SUBSTR(VIEWCD,1,2) AS CLASSCD ");
                stb.append(                "FROM    JVIEWNAME_MST ");
                stb.append(                "GROUP BY SUBSTR(VIEWCD,1,2) ");
                stb.append(                ") W1 ");
                stb.append(               ",SCHREG_DATA W2 ");
                stb.append(      ")T2 ");
                stb.append("LEFT JOIN VALUE_DATA2 T5 ON T5.YEAR = T2.YEAR AND T5.CLASSCD = T2.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
                    stb.append("  AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
                } else {
                    stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
                }
                stb.append("ORDER BY SHOWORDERCLASS, T2.CLASSCD, T2.GRADE ");

                // log.debug(" val sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                final int MAX_LINE = 9;
                
                final List leftClassList = new ArrayList();
                final List rightClassList = new ArrayList();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (null == year) {
                        continue;
                    }
                    if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                        // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                        continue;
                    }
                    final String electdiv = rs.getString("ELECTDIV");
                    final String classcd;
                    if ("1".equals(param._useCurriculumcd)) {
                        classcd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                    } else {
                        classcd = rs.getString("CLASSCD");
                    }
                    final String classname = StringUtils.defaultString(rs.getString("CLASSNAME"));
                    final String grade = rs.getString("GRADE");
                    final String value = rs.getString("VALUE");
                    
                    final Valuation valuation = new Valuation(year, grade, value);
                    final int e = "1".equals(electdiv) ? 1 : 0;  //必修:0 選択:1
                    final List inList;
                    if (param._isChiben) {
                        if (e == 1) {
                            inList = null;  // 智辯は選択教科を表示しない
                        } else {
                            ClassValuation classValuation = null;
                            for (final Iterator it = leftClassList.iterator(); it.hasNext();) {
                                final ClassValuation cv = (ClassValuation) it.next();
                                if (cv._classcd != null && cv._classcd.equals(classcd)) {
                                    classValuation = cv;
                                }
                            }
                            if (null == classValuation && leftClassList.size() >= MAX_LINE) {
                                inList = rightClassList;
                            } else {
                                inList = leftClassList;
                            }
                        }
                    } else {
                        if (e == 1) {
                            inList = rightClassList;
                        } else {
                            inList = leftClassList;
                        }
                    }
                    if (null == inList) {
                        continue;
                    }
                    
                    ClassValuation classValuation = null;
                    for (final Iterator it = inList.iterator(); it.hasNext();) {
                        final ClassValuation cv = (ClassValuation) it.next();
                        if (cv._classcd != null && cv._classcd.equals(classcd)) {
                            classValuation = cv;
                        }
                    }
                    if (null == classValuation) {
                        classValuation = new ClassValuation(electdiv, classcd, classname);
                        inList.add(classValuation);
                    }
                    classValuation._valuationList.add(valuation);
                }
                
                printValue(svf, param, student, entGrdHist, MAX_LINE, leftClassList, false);
                printValue(svf, param, student, entGrdHist, MAX_LINE, rightClassList, true);
                
            } catch (Exception ex) {
                log.debug( "printSvfDetail_1 error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }


        private void printValue(final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist, final int MAX_LINE, final List inClassList, final boolean isPrintRight) {
            for (int line = 0; line < inClassList.size() && line < MAX_LINE; line++) {
                final ClassValuation cv = (ClassValuation) inClassList.get(line);
                printSvfSubjectName( svf, cv._classname, line + 1, isPrintRight);  //教科名出力
                for (final Iterator it = cv._valuationList.iterator(); it.hasNext();) {
                    final Valuation valuation = (Valuation) it.next();
                    //評定出力
                    final String year = valuation._year;
                    if (student.yearIsOutLimit(year, param)) {
                        continue;
                    }
                    if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                        // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + student.getSchregEntGrdHistDatYearEnd(entGrdHist));
                        continue;
                    }
                    final int g = Integer.parseInt(valuation._grade);  //学年
                    if (valuation._value != null) {
                        svf.VrsOutn( "ASSESS" + (isPrintRight ? 4 : 3) + "_" + g, line + 1, valuation._value);  //評定
                    }
                }
            }
        }


        /**
         *  SVF-FORM 印刷処理 評定 教科名出力
         *
         */
        private static void printSvfSubjectName( Vrw32alp svf, final String classname, int n, boolean isPrintRight )
        {
            int num = classname.length();
            if( num <= 3) {
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_1",  n,  classname );  //評定教科名
            } else if( num <= 4) {
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_2",  n,  classname.substring( 0, 2 ) );  //評定教科名
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_3",  n,  classname.substring( 2 )    );  //評定教科名
            } else if( num <= 6) {
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_2",  n,  classname.substring( 0, 3 ) );  //評定教科名
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_3",  n,  classname.substring( 3 )    );  //評定教科名
            } else if( num <= 8) {
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_2",  n,  classname.substring( 0, 4 ) );  //評定教科名
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_3",  n,  classname.substring( 4 )    );  //評定教科名
            } else{
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_2",  n,  classname.substring( 0, 5 ) );  //評定教科名
                svf.VrsOutn( "CLASS" + ( isPrintRight ? 4: 3 ) + "_3",  n,  classname.substring( 5 )    );  //評定教科名
            }
        }


        /**
         *  SVF-FORM 印刷処理 明細
         *  個人情報  学籍等履歴情報
         */
        private void printRegd(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
        {
            svf.VrsOut("NAME" + (retStringByteValue(student._schname) <= 24 ? "1" : "2"), student._schname);
            svf.VrsOut("NAME",  student._schname);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //学校データ
                int p = 0;
                ps = db2.prepareStatement( new KNJ_SchoolinfoSql("10000").pre_sql() );
                ps.setString(++p, param._year);
                ps.setString(++p, param._year);
                rs = ps.executeQuery();

                if (rs.next()) {
                    final String schoolName1 = StringUtils.defaultString(param._schoolName, rs.getString("SCHOOLNAME1"));
                    final int n1 = retStringByteValue(schoolName1);
                    if (40 < n1) {
                        svf.VrsOut("SCHOOLNAME2", schoolName1);
                    } else if (0 < n1) {
                        svf.VrsOut("SCHOOLNAME1", schoolName1);
                    }

                    if (0 < n1 && null == param._schoolName) {
                        param._schoolName = schoolName1;
                    }
                }
            } catch (final Exception e) {
                log.debug("printSvfDetail_1 error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                setMapForHrclassName(db2);

                //学籍履歴の取得および印刷
                ps = db2.prepareStatement(Student.getKNJ_GradeRecSqlsql_state(param, student));
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (student.yearIsOutLimit(year, param)) {
                        continue;
                    }
                    if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= entGrdHist.getYearEnd())) {
                        // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                        continue;
                    }
                    
                    final int i = Integer.parseInt( rs.getString("GRADE") );
                    String hrname = null;
                    if ("1".equals(param._useSchregRegdHdat)) {
                        hrname = rs.getString("HR_CLASS_NAME1");
                    } else if ("0".equals(param._useSchregRegdHdat)) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), hmap);
                    }
                    hrname = StringUtils.defaultString(hrname, KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS")));
                    svf.VrsOutn("HR_NAME", i, hrname); // 組
                    svf.VrsOutn( "ATTENDNO",  i,  String.valueOf( Integer.parseInt( rs.getString("ATTENDNO") ) ) );    //出席番号
                }
            } catch (Exception ex) {
                log.debug( "printSvfDetail_5 _REGD_RECORD error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps,rs);
                db2.commit();
            }
        }

        /**
         *  SVF-FORM 印刷処理 明細
         *  総合的な学習の時間の記録
         */
        private void printRemark(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
        {
            final int pactw = Param.getParamSizeNum(param.HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD, 0);
            final int pacth = Param.getParamSizeNum(param.HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD, 1);
            final int pvieww = Param.getParamSizeNum(param.HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD, 0);
            final int pviewh = Param.getParamSizeNum(param.HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD, 1);
            final int pvalw = Param.getParamSizeNum(param.HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD, 0);
            final int pvalh = Param.getParamSizeNum(param.HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD, 1);
            
            final int totalstudyactw = (-1 == pactw || -1 == pacth) ? 20 : pactw;
            final int totalstudyacth = (-1 == pactw || -1 == pacth) ? 4 : pacth;
            final int viewremarkw = (-1 == pvieww || -1 == pviewh) ? 10 : pvieww;
            final int viewremarkh = (-1 == pvieww || -1 == pviewh) ? 4 : pviewh;
            final int totalstudyvalw = (-1 == pvalw || -1 == pvalh) ? 20 : pvalw;
            final int totalstudyvalh = (-1 == pvalw || -1 == pvalh) ? 4 : pvalh;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //総合的な学習の時間の記録
                StringBuffer stb = new StringBuffer();
                stb.append("SELECT  YEAR, ANNUAL, TOTALSTUDYACT, VIEWREMARK, TOTALSTUDYVAL,TOTALREMARK ");
                stb.append(       ",BEHAVEREC_REMARK, SPECIALACTREMARK, ATTENDREC_REMARK ");
                stb.append("FROM    HTRAINREMARK_DAT T1 ");
                stb.append("WHERE   YEAR <= '" + param._year + "' ");
                stb.append(    "AND SCHREGNO = '" + student._schregno + "' ");
                // log.debug(" det6 sql = " + stb.toString());

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    if (student.yearIsOutLimit(year, param)) {
                        continue;
                    }
                    if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                        // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                        continue;
                    }

                    final int g = Integer.parseInt(rs.getString("ANNUAL"));

                    final List studyact = knjobj.retDividString(rs.getString("TOTALSTUDYACT"), totalstudyactw * 2, totalstudyacth);
                    if (studyact != null) {
                        for (int i = 0; i < studyact.size(); i++) {
                            svf.VrsOutn("TOTAL_ACT" + g, i + 1, (String) studyact.get(i)); //学習活動
                        }
                    }

                    final List viewremark = knjobj.retDividString(rs.getString("VIEWREMARK"), viewremarkw * 2, viewremarkh);
                    if (viewremark != null) {
                        for (int i = 0; i < viewremark.size(); i++) {
                            svf.VrsOutn("TOTAL_VIEW" + g, i + 1, (String) viewremark.get(i)); //観点
                        }
                    }

                    final List studyval = knjobj.retDividString(rs.getString("TOTALSTUDYVAL"), totalstudyvalw * 2, totalstudyvalh);
                    if (studyval != null) {
                        for (int i = 0; i < studyval.size(); i++) {
                            svf.VrsOutn("TOTAL_VALUE" + g, i + 1, (String) studyval.get(i)); //評価
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug( "printSvfDetail_1 error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
    
    /**
    *
    *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中学校用（レイアウトは尚学中学版、データは千代田区立九段版を基に作成）
    *                                                    行動の記録
    *                                                    特別活動の記録
    *                                                    総合所見及び指導上参考となる事項
    *                                                    出欠の記録
    *
    *  2006/04/10 Build yamashiro
    */

   private static class KNJA131KFORM5 extends KNJA131K.BASE
   {
       /**
        *  SVF-FORM 印刷処理
        */
       public boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student)
       {
           boolean nonedata = false;
           final String form = (param._isKyoai) ? "KNJA131_11_KYOAI.frm" : (param._isChiben) ? "KNJA131_11A.frm" : "KNJA131_11.frm";   
           final List entGrdHistList = student.getPrintSchregEntGrdHistList(param);
           try {
               for (final Iterator it = entGrdHistList.iterator(); it.hasNext();) {
                   final SchregEntGrdHistDat entGrdHist = (SchregEntGrdHistDat) it.next();
                   svf.VrSetForm(form, 1);
                   printRegd(db2, svf, param, student, entGrdHist);  //学籍データ
                   printRemark(db2, svf, param, student, entGrdHist);  //総合的な学習の時間の記録・総合所見
                   printBehavior(db2, svf, param, student, entGrdHist);  //行動の記録・特別活動の記録
                   printAttendance(db2, svf, param, student, entGrdHist);  //出欠の記録
                   svf.VrEndPage();
                   nonedata = true;
               }
           } catch (Exception ex) {
               log.debug("printSvf error!", ex);
           }
           return nonedata;
       }


       /**
        *  SVF-FORM 印刷処理 明細
        *  行動の記録・特別活動の記録
        */
       private void printBehavior(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
       {
           //行動の記録・特別活動の記録
           PreparedStatement ps = null;
           ResultSet rs = null;
           try {
               StringBuffer stb = new StringBuffer();
               stb.append("SELECT  YEAR, DIV, CODE, ANNUAL, RECORD ");
               stb.append("FROM    BEHAVIOR_DAT T1 ");
               stb.append("WHERE   YEAR <= '" + param._year + "' ");
               stb.append(    "AND SCHREGNO = '" + student._schregno + "' ");

               // log.debug(" det3 sql = " + stb.toString());
               ps = db2.prepareStatement(stb.toString());
               rs = ps.executeQuery();

               while (rs.next()) {
                   if ("1".equals(rs.getString("RECORD"))) {
                       final String year = rs.getString("YEAR");
                       if (student.yearIsOutLimit(year, param)) {
                           continue;
                       }
                       if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                           // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                           continue;
                       }
                       final int g = Integer.parseInt( rs.getString("ANNUAL"));
                       if("1".equals(rs.getString("DIV"))) {
                           svf.VrsOutn("ACTION" + g, Integer.parseInt(rs.getString("CODE")), "○"); //行動の記録
                       } else {
                           svf.VrsOutn("SPECIALACT" + g, Integer.parseInt(rs.getString("CODE")), "○"); //行動の記録
                       }
                   }
               }
           } catch (Exception ex) {
               log.debug( "printSvfDetail_3 error!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
       }

       /**
        *  SVF-FORM 印刷処理 明細
        *  出欠の記録
        */
       private void printAttendance(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
       {
           PreparedStatement ps = null;
           ResultSet rs = null;
           try {
               //出欠の記録
               StringBuffer stb = new StringBuffer();
               stb.append("SELECT  T1.YEAR, ANNUAL, ");
               stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
               stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
               if (param._definecode.schoolmark.substring(0, 1).equals("K")) {
                   stb.append("              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                   stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
               } else {
                   stb.append("              THEN VALUE(CLASSDAYS,0) ");
                   stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
               }
               stb.append(             "END AS LESSON, ");
               stb.append(        "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
               stb.append(        "VALUE(SUSPEND,0) AS SUSPEND, ");
               stb.append(        "VALUE(MOURNING,0) AS MOURNING, ");
               stb.append(        "VALUE(ABROAD,0) AS ABROAD, ");
               stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
               stb.append(             "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
               stb.append(             "ELSE VALUE(REQUIREPRESENT,0) ");
               stb.append(             "END AS REQUIREPRESENT, ");
               stb.append(        "VALUE(PRESENT,0) AS PRESENT, ");
               stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
               stb.append(             "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
               stb.append(             "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
               stb.append(             "END AS ABSENT ");
               stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
               stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
               stb.append("WHERE   T1.YEAR <= '" + param._year + "' ");
               stb.append(    "AND SCHREGNO = '" + student._schregno + "' ");

               // log.debug(" det4 sql = " + stb.toString());
               ps = db2.prepareStatement(stb.toString());
               rs = ps.executeQuery();

               while (rs.next()) {
                   final String year = rs.getString("YEAR");
                   if (student.yearIsOutLimit(year, param)) {
                       continue;
                   }
                   if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                       // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                       continue;
                   }
                   final int g = Integer.parseInt( rs.getString("ANNUAL"));
                   svf.VrsOutn("LESSON",   g,  rs.getString("LESSON")); //授業日数
                   svf.VrsOutn("SUSPEND",  g,  rs.getString("SUSPEND_MOURNING")); //出停・忌引
                   svf.VrsOutn("ABROAD",   g,  rs.getString("ABROAD")); //留学
                   svf.VrsOutn("PRESENT",  g,  rs.getString("REQUIREPRESENT")); //要出席
                   svf.VrsOutn("ATTEND",   g,  rs.getString("PRESENT")); //出席
                   svf.VrsOutn("ABSENCE",  g,  rs.getString("ABSENT")); //欠席
                   //svf.VrsOutn("LATE",     g,  rs.getString("LATE")); //遅刻
                   //svf.VrsOutn("LEAVE",    g,  rs.getString("EARLY")); //早退
               }
           } catch (Exception ex) {
               log.debug( "printSvfDetail_4 error!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
       }

       /**
        *  SVF-FORM 印刷処理 明細
        *  総合的な学習の時間の記録・総合所見
        */
       private void printRemark(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
       {
           final int ptotalw = Param.getParamSizeNum(param.HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD, 0);
           final int ptotalh = Param.getParamSizeNum(param.HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD, 1);
           final int pattendw = Param.getParamSizeNum(param.HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD, 0);
           final int pattendh = Param.getParamSizeNum(param.HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD, 1);
           final int totalw =  (-1 == ptotalw || -1 == ptotalh) ?  15 : ptotalw;
           final int totalh =  (-1 == ptotalw || -1 == ptotalh) ?  29 : ptotalh;
           final int attendw = (-1 == pattendw || -1 == pattendh) ?  20 : pattendw;
           final int attendh = (-1 == pattendw || -1 == pattendh) ?  1 : pattendh;
           
           //総合的な学習の時間の記録・総合所見
           PreparedStatement ps = null;
           ResultSet rs = null;
           try {
               StringBuffer stb = new StringBuffer();
               stb.append("SELECT  YEAR, ANNUAL, TOTALSTUDYACT, VIEWREMARK, TOTALSTUDYVAL,TOTALREMARK ");
               stb.append(       ",BEHAVEREC_REMARK, SPECIALACTREMARK, ATTENDREC_REMARK ");
               stb.append("FROM    HTRAINREMARK_DAT T1 ");
               stb.append("WHERE   YEAR <= '" + param._year + "' ");
               stb.append(    "AND SCHREGNO = '" + student._schregno + "' ");
               
               // log.debug(" det1 sql = " + stb.toString());

               ps = db2.prepareStatement(stb.toString());
               rs = ps.executeQuery();
               while (rs.next()) {
                   final String year = rs.getString("YEAR");
                   if (student.yearIsOutLimit(year, param)) {
                       continue;
                   }
                   if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= student.getSchregEntGrdHistDatYearEnd(entGrdHist, param))) {
                       // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                       continue;
                   }
                   final int g = Integer.parseInt(rs.getString("ANNUAL"));
                   for (int i = 0; i < 29; i++) {
                       svf.VrsOutn("TOTALREMARK" + g, i + 1, ""); //総合所見クリア
                   }
                   for (int i = 0; i < 1; i++) {
                       svf.VrsOutn("ATTEND_REMARK" + (i + 1), g, ""); //出欠の記録備考クリア
                   }
                   final List list = retDividString(rs.getString("TOTALREMARK"), totalw * 2, (param._isKyoai ? 44 : totalh));
                   final String field1 = (param._isKyoai && list.size() > 29) ? ("TOTALREMARK" + g + "_2") : ("TOTALREMARK" + g);
                   for (int i = 0; i < list.size(); i++) {
                       svf.VrsOutn(field1, i + 1, (String) list.get(i)); //総合所見
                   }
                   final List list2 = retDividString(rs.getString("ATTENDREC_REMARK"), attendw * 2, attendh);
                   final String field2 = attendw > 26 ? "3_" : "";
                   for (int i = 0; i < list2.size(); i++) {
                       svf.VrsOutn("ATTEND_REMARK" + field2 + (i + 1), g, (String) list2.get(i)); //出欠の記録備考
                   }
               }
           } catch (Exception ex) {
               log.debug("printSvfDetail_1 error!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
       }


       /**
        *  SVF-FORM 印刷処理 明細
        *  個人情報  学籍等履歴情報
        */
       private void printRegd(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final SchregEntGrdHistDat entGrdHist)
       {
           svf.VrsOut("NAME" + (24 < retStringByteValue(student._schname) ? "2" : "1"), student._schname);

           if (param._isChiben) {
               PreparedStatement ps = null;
               ResultSet rs = null;
               //学籍等履歴
               try {
                   ps = db2.prepareStatement(Student.getKNJ_GradeRecSqlsql_state(param, student));
                   rs = ps.executeQuery();
                   svf.VrsOut("GRADENAME", "学年" );
                   while( rs.next()) {
                       final String year = rs.getString("YEAR");
                       if (student.yearIsOutLimit(year, param)) {
                           continue;
                       }
                       if (null != year && !(entGrdHist.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= entGrdHist.getYearEnd())) {
                           // log.debug(" skip print year = " + year + ", begin = " + entGrdHist.getYearBegin() + ", end = " + entGrdHist.getYearEnd());
                           continue;
                       }
                       final int i = Integer.parseInt( rs.getString("GRADE"));
                       String hrClass = null;
                       if ("1".equals(param._useSchregRegdHdat)) {
                           hrClass = rs.getString("HR_CLASS_NAME1");
                       } else if ("0".equals(param._useSchregRegdHdat)) {
                           hrClass = KNJ_EditEdit.Ret_Num_Str(rs.getString("HR_CLASS"), hmap);
                       }
                       if (hrClass == null) {
                           hrClass = KNJ_EditEdit.Ret_Num_Str( rs.getString("HR_CLASS")); 
                       }
                       svf.VrsOut( "GRADE1_" + i , String.valueOf(i));
                       svf.VrsOut( "HR_CLASS_" + i,     hrClass);
                       svf.VrsOut( "ATTENDNO_" + i,   String.valueOf( Integer.parseInt(rs.getString("ATTENDNO"))));    //出席番号
                   }
               } catch (Exception ex) {
                   log.debug( "printSvfDetail_5 _REGD_RECORD error!", ex );
               } finally {
                   DbUtils.closeQuietly(null, ps, rs);
                   db2.commit();
               }
           }
       }

       private static class CharMS932 {
           final String _char;
           final String _b;
           final int _len;
           public CharMS932(final String v, final byte[] b) {
               _char = v;
               _b = btos(b);
               _len = b.length;
           }
           public String toString() {
               return "[" + _char + " : " + _b + " : " + _len + "]";
           }
           private static String btos(final byte[] b) {
               final StringBuffer stb = new StringBuffer("[");
               final String[] ns = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
               String spc = "";
               for (int i = 0; i < b.length; i++) {
                   final int n = b[i] + (b[i] < 0 ? 256 : 0);
                   stb.append(spc).append(ns[n / 16]).append(ns[n % 16]);
                   spc = " ";
               }
               return stb.append("]").toString();
           }
           private static List toCharMs932List(final String src) throws Exception {
               final List rtn = new ArrayList();
               for (int j = 0; j < src.length(); j++) {
                   final String z = src.substring(j, j + 1);             //1文字を取り出す
                   final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
                   rtn.add(c);
               }
               return rtn;
           }
       }
       
       private static List retDividString(String targetsrc, final int dividlen, final int dividnum) {
           if (targetsrc == null) {
               return Collections.EMPTY_LIST;
           }
           List lines = new ArrayList();         //編集後文字列を格納する配列
           int len = 0;
           StringBuffer stb = new StringBuffer();
           
           try {
               if (!StringUtils.replace(targetsrc, "\r\n", "\n").equals(targetsrc)) {
//                   log.fatal("改行コードが\\r\\n!");
//                   log.fatal(targetsrc);
//                   log.fatal(btos(targetsrc.getBytes("MS932")));
                   targetsrc = StringUtils.replace(targetsrc, "\r\n", "\n");
               }

               final List charMs932List = CharMS932.toCharMs932List(targetsrc);

               for (final Iterator it = charMs932List.iterator(); it.hasNext();) {
                   final CharMS932 c = (CharMS932) it.next();
                   //log.debug(" c = " + c);
                   
                   if (("\n".equals(c._char) || "\r".equals(c._char))) {
                       if (len <= dividlen) {
                           lines.add(stb.toString());
                           len = 0;
                           stb.delete(0, stb.length());
                       }
                   } else {
                       if (len + c._len > dividlen) {
                           lines.add(stb.toString());
                           len = 0;
                           stb.delete(0, stb.length());
                       }
                       stb.append(c._char);
                       len += c._len;
                   }
               }
               if (0 < len) {
                   lines.add(stb.toString());
               }
           } catch (Exception ex) {
               log.error("retDividString error! ", ex);
           }
           if (dividnum != -1 && lines.size() > dividnum) {
               lines = lines.subList(0, dividnum);
           }
           return lines;
       }

   }
   
   public static class Param {
       static final String SCHOOL_KIND = "J";

       final String _year;
       final String _gakki;
       final String _output;
       final String GRADE_HR_CLASS;
       private String KANJI_OUT;
       private String INNEI_OUT;

       private String _useSchregRegdHdat;
       private String _useCurriculumcd;
       private String _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear;
       private int _seitoSidoYorokuCyugakuKirikaeNendo;

       private String _z010;
       boolean _isChiben;
       boolean _isKyoai;
       private String DOCUMENTROOT;
       private String IMAGE1;
       private String IMAGE2;
       
       final KNJDefineCode _definecode; // 各学校における定数等設定
       
       final String _prgid;
       final String HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD;
       final String HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD;
       final String HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD;
       final String HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD;
       final String HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD;
       private Map staffMstMap = Collections.EMPTY_MAP;

       /** 高校か? */
       private boolean _isHigh;
       private String _seirekiFlg;
       final Map _gradeCdMap;
       final boolean _isJuniorHiSchool;
       private String _schoolName;

       final boolean _hasSchregEntGrdHistComebackDat;

       Param(final HttpServletRequest request, final DB2UDB db2) {
           try {
               PreparedStatement ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2 = '00'");
               ResultSet rs = ps.executeQuery();
               if (rs.next()) {
                   _seirekiFlg = rs.getString("NAME1");
               }
               db2.commit();
               DbUtils.closeQuietly(null, ps, rs);
           } catch (SQLException e) {
               log.error("SQLException", e);
           }

           _year = request.getParameter("YEAR"); // 年度
           _gakki = request.getParameter("GAKKI"); // 学期
           _output = request.getParameter("OUTPUT");
           GRADE_HR_CLASS = request.getParameter("GRADE_HR_CLASS"); // 学年・組

           if (request.getParameter("simei") != null) {
               KANJI_OUT = request.getParameter("simei"); // 漢字名出力
           }
           if (request.getParameter("inei") != null) {
               INNEI_OUT = request.getParameter("inei"); // 陰影出力
           }
           if (request.getParameter("DOCUMENTROOT") != null) {
               DOCUMENTROOT = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
           }
           _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
           _useCurriculumcd = request.getParameter("useCurriculumcd");
           _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear = request.getParameter("seitoSidoYorokuCyugakuKirikaeNendoForRegdYear");
           _seitoSidoYorokuCyugakuKirikaeNendo = NumberUtils.isDigits(request.getParameter("seitoSidoYorokuCyugakuKirikaeNendo")) ? Integer.parseInt(request.getParameter("seitoSidoYorokuCyugakuKirikaeNendo")) : 9999;

           _definecode = new KNJDefineCode();
           _definecode.setSchoolCode(db2, _year);
           _z010 = getZ010(db2);
           _isChiben = "CHIBEN".equals(_z010);
           _isKyoai = "kyoai".equals(_z010);
           // TAKAESU: ダサい
           final String hoge = request.getParameterValues("GRADE_HR_CLASS")[0];
           if (null != hoge && hoge.length() >= 2) {
               final int grade = Integer.parseInt(hoge.substring(0, 2));
               _isHigh = (grade >= 4);
           }
           
           _prgid = request.getParameter("PRGID");
           HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD =    StringUtils.replace(request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD"), "+", " ");
           HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD =       StringUtils.replace(request.getParameter("HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD"), "+", " ");
           HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD =    StringUtils.replace(request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD"), "+", " ");
           HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD = StringUtils.replace(request.getParameter("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD"), "+", " ");
           HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD =      StringUtils.replace(request.getParameter("HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD"), "+", " ");
           
           if (null != request.getParameter("seito")) {
               staffMstMap = StaffMst.load(db2, request.getParameter("YEAR"));
           }
           _gradeCdMap = getGradeCdMap(db2);
           _isJuniorHiSchool = isJuniorHiSchool(db2);
           if (_isJuniorHiSchool) {
               _schoolName = loadSchoolName(db2);
           } else {
               _schoolName = null;
           }
           
           getDocumentroot(db2);

           _hasSchregEntGrdHistComebackDat = setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
       }
       
       public List getSchregnoList(final DB2UDB db2, final HttpServletRequest request, final String selected) {
           final List schregnotList;
           if ("2".equals(_output)) {
               schregnotList = setSchnoList(db2, selected);
           } else {
               schregnotList = new ArrayList();
               final String s[] = request.getParameterValues("category_selected"); // 学籍番号
               for (int j = 0; j < s.length; j++) {
                   schregnotList.add(s[j]);
               }
           }
           return schregnotList;
       }

       /**
        * 組単位の学籍番号をＬＩＳＴへ格納 NO001 Build
        */
       private List setSchnoList(final DB2UDB db2, String selected) {
           PreparedStatement ps = null;
           ResultSet rs = null;
           List rtnList = new ArrayList();
           try {
               StringBuffer stb = new StringBuffer();
               stb.append("SELECT SCHREGNO FROM SCHREG_REGD_DAT ");
               stb.append("WHERE YEAR = '" + _year + "' ");
               stb.append("AND SEMESTER = '" + _gakki + "' ");
               stb.append("AND GRADE || HR_CLASS = '" + selected + "' ");
               stb.append("ORDER BY ATTENDNO ");
               ps = db2.prepareStatement(stb.toString()); // 任意のHR組の学籍番号取得用
               rs = ps.executeQuery();
               while (rs.next()) {
                   rtnList.add(rs.getString("SCHREGNO"));
               }
           } catch (Exception ex) {
               log.error("error! ", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           return rtnList;
       }
       
       private String getZ010(DB2UDB db2) {
           PreparedStatement ps = null;
           ResultSet rs = null;
           try {
               ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
               rs = ps.executeQuery();
               if (rs.next()) {
                   return rs.getString("NAME1");
               }
           } catch (SQLException ex) {
               log.debug("exception! ", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           return null;
       }
       
       public int getGradeCd(final String year, final String grade) {
           final String gradeCd = (String) _gradeCdMap.get(year + grade);
           int n = -1;
           try {
               n = Integer.parseInt(gradeCd);
           } catch (Exception e) {
               log.error("SCHREG_REGD_GDAT.GRADE_CD IS NOT NUMBER. value = '" + gradeCd + "'");
           }
           return n;
       }
       
       private Map getGradeCdMap(final DB2UDB db2) {
           final Map gdatMap = new HashMap();
           PreparedStatement ps = null;
           ResultSet rs = null;
           try {
               final StringBuffer stb = new StringBuffer();
               stb.append(" SELECT ");
               stb.append("     * ");
               stb.append(" FROM ");
               stb.append("     SCHREG_REGD_GDAT T1 ");
               stb.append(" WHERE ");
               stb.append("     T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
               ps = db2.prepareStatement(stb.toString());
               rs = ps.executeQuery();
               while (rs.next()) {
                   final String year = rs.getString("YEAR");
                   final String grade = rs.getString("GRADE");
                   gdatMap.put(year + grade, rs.getString("GRADE_CD"));
               }
           } catch (SQLException e) {
               log.error("exception!", e);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           return gdatMap;
       }
       
       /**
        * 中高一貫か?
        * @param db2 DB2UDB
        * @return 中高一貫ならtrue
        */
       private boolean isJuniorHiSchool(final DB2UDB db2) {
           boolean isJuniorHighSchool = false;
           PreparedStatement ps = null;
           ResultSet rs = null;
           try {
               ps = db2.prepareStatement("SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
               rs = ps.executeQuery();
               if (rs.next()) {
                   final String str = rs.getString("NAMESPARE2");
                   if (str != null) {
                       isJuniorHighSchool = true;
                   }
               }
           } catch (SQLException e) {
               log.error("SQLException", e);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           return isJuniorHighSchool;
       }

       /**
        * 写真データ格納フォルダの取得 --NO001
        */
       private void getDocumentroot(final DB2UDB db2) {
           try {
               KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
               KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
               if (returnval.val4 != null) {
                   IMAGE1 = returnval.val4; // 写真データ格納フォルダ
               }
               if (returnval.val5 != null) {
                   IMAGE2 = returnval.val5; // 写真データの拡張子
               }
           } catch (Exception ex) {
               log.debug("getDocumentroot error!", ex);
           }
       }

       private String loadSchoolName(final DB2UDB db2) {
           String rtn = null;
           PreparedStatement ps = null;
           ResultSet rs = null;

           final String certifKindCd = _isHigh ? "107" : "108";
           final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT"
               + " WHERE YEAR='" + _year + "'"
               + " AND CERTIF_KINDCD='" + certifKindCd + "'";

           try {
               ps = db2.prepareStatement(sql);
               rs = ps.executeQuery();
               if (rs.next()) {
                   rtn = rs.getString("SCHOOL_NAME");
               }
           } catch (final SQLException e) {
               log.error("学校名称取得エラー:" + sql, e);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + rtn + "]");
           return rtn;
       }

       /**
        * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
        * @param param サイズタイプのパラメータ文字列
        * @param pos split後のインデクス (0:w, 1:h)
        * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
        */
       protected static int getParamSizeNum(final String param, final int pos) {
           int num = -1;
           String[] nums = StringUtils.split(param, " * ");
           if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
               num = -1;
           } else {
               try {
                   num = Integer.valueOf(nums[pos]).intValue();
               } catch (Exception e) {
                   log.error("Exception!", e);
               }
           }
           return num;
       }

       private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
           final StringBuffer stb = new StringBuffer();
           stb.append(" SELECT 1 FROM ");
           if (StringUtils.isBlank(colname)) {
               stb.append("SYSCAT.TABLES");
           } else {
               stb.append("SYSCAT.COLUMNS");
           }
           stb.append(" WHERE TABNAME = '" + tabname + "' ");
           if (!StringUtils.isBlank(colname)) {
               stb.append(" AND COLNAME = '" + colname + "' ");
           }
           
           PreparedStatement ps = null;
           ResultSet rs = null;
           boolean hasTableColumn = false;
           try {
               ps = db2.prepareStatement(stb.toString());
               rs = ps.executeQuery();
               while (rs.next()) {
                   hasTableColumn = true;
               }
           } catch (Exception ex) {
               log.error("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
           return hasTableColumn;
       }
   }

}
