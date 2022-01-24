// kanji=漢字
/*
 * $Id: fa4ae5a8c21386f8e70cf1b31e2f352a68bdd12e $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 生徒情報
 */
public class KNJA139A {
    private static final Log log = LogFactory.getLog(KNJA139A.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        boolean nonedata = false;
        try {
            sd.setSvfInit(request, response, svf);

            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(request, db2);

            // 印刷処理
            nonedata = printSvf(db2, svf, _param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            sd.closeSvf(svf, nonedata);
            sd.closeDb(db2);
        }
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
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

    private static String getBirthday(final String date, final String birthdayFlg, final Param param) {
        final String birthday;
        if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
            birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.h_format_JP_MD(date);
        } else {
            birthday = KNJ_EditDate.h_format_JP(date);
        }
        return birthday;
    }

    private static String whereIn(final boolean skipNull, final String[] array) {
        if (null == array || 0 == array.length) {
            return null;
        }

        final StringBuffer sb = new StringBuffer();
        int n = 0;
        for (int i = 0; i < array.length; i++) {
            if (null == array[i] && skipNull) {
                continue;
            }

            if (0 == n) { sb.append("("); }
            if (0 != n) { sb.append(", "); }

            if (null == array[i]) {
                sb.append(String.valueOf(array[i])); // "null"
            } else {
                sb.append('\'');
                sb.append(StringEscapeUtils.escapeSql(array[i]));
                sb.append('\'');
            }
            //--
            n++;
        }

        if (0 == n) {
            return null;
        }

        sb.append(")");
        return sb.toString();
    }

    private static String getSql(final Param param) {
        final StringBuffer sql = new StringBuffer();
        sql.append(" WITH SCHREGNOS AS (");
        sql.append("  SELECT T1.SCHREGNO, T2.SCHOOL_KIND ");
        sql.append("  FROM SCHREG_REGD_DAT T1 ");
        sql.append("  LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
        sql.append("      AND T2.GRADE = T1.GRADE ");
        sql.append("  WHERE T1.YEAR = '" + param._year + "' ");
        sql.append("    AND T1.SEMESTER = '" + param._gakki + "' ");
        if ("2".equals(param._output)) {
            sql.append("AND T1.GRADE || T1.HR_CLASS IN " + whereIn(true, param._categorySelected) + " ");
        } else {
            sql.append("AND T1.SCHREGNO IN " + whereIn(true, param._categorySelected) + " ");
        }
        sql.append(" ), ENT_GRD_YEARS AS (");
        sql.append("      SELECT '0' AS SCHOOLDIV, T4.SCHREGNO, T4.YEAR, T4.GRADE, T4_2.GRADE_CD ");
        sql.append("      FROM SCHOOL_MST T2 ");
        sql.append("      LEFT JOIN ( ");
        sql.append("          SELECT SCHREGNO, YEAR, GRADE FROM V_REGDYEAR_GRADE_DAT ");

        sql.append("      ) T4 ON T4.YEAR = T2.YEAR ");
        sql.append("      LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
        sql.append("      WHERE T2.SCHOOLDIV = '0' ");
        sql.append("      UNION ALL ");
        sql.append("      SELECT '1' AS SCHOOLDIV, T5.SCHREGNO, T5.YEAR, T5.GRADE, T5_2.GRADE_CD ");
        sql.append("      FROM SCHOOL_MST T2 ");
        sql.append("      LEFT JOIN ( ");
        sql.append("          SELECT SCHREGNO, YEAR, GRADE FROM V_REGDYEAR_UNIT_DAT ");

        sql.append("      ) T5 ON T5.YEAR = T2.YEAR  ");
        sql.append("      LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ");
        sql.append("      WHERE T2.SCHOOLDIV = '1' ");
        sql.append(" ) ");

        sql.append(" , STATE_MAIN AS (SELECT ");
        sql.append("    T1.SCHREGNO, ");
        sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
        sql.append("    ENT_DATE, ");
        sql.append("    ENT_REASON, ");
        sql.append("    ENT_SCHOOL, ");
        sql.append("    ENT_ADDR, ");
        if ("1".equals(param._useAddrField2)) {
            sql.append("    ENT_ADDR2,");
        } else {
            sql.append("    CAST(NULL AS VARCHAR(1)) AS ENT_ADDR2,");
        }
        sql.append("    ENT_DIV, ");
        sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
        sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
        sql.append("    GRD_DATE, ");
        sql.append("    GRD_REASON, ");
        sql.append("    GRD_SCHOOL, ");
        sql.append("    GRD_ADDR, ");
        if ("1".equals(param._useAddrField2)) {
            sql.append("    GRD_ADDR2,");
        } else {
            sql.append("    CAST(NULL AS VARCHAR(1)) AS GRD_ADDR2,");
        }
        sql.append("    GRD_NO, ");
        sql.append("    GRD_DIV, ");
        sql.append("    T4.NAME1 AS GRD_DIV_NAME, ");
        sql.append("    T1.CURRICULUM_YEAR, ");
        sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
        sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
        sql.append(" FROM ");
        sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
        sql.append("    INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        sql.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ");
        sql.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ");
        sql.append(" ), STATE AS ( ");
        sql.append(" SELECT DISTINCT ");
        sql.append("    T1.SCHREGNO, ");
        sql.append("    T1.ENT_YEAR, ");
        sql.append("    T1.ENT_DATE, ");
        sql.append("    T1.ENT_REASON, ");
        sql.append("    T1.ENT_SCHOOL, ");
        sql.append("    T1.ENT_ADDR, ");
        sql.append("    T1.ENT_ADDR2,");
        sql.append("    T1.ENT_DIV, ");
        sql.append("    T1.ENT_DIV_NAME, ");
        sql.append("    T1.GRD_YEAR, ");
        sql.append("    T1.GRD_DATE, ");
        sql.append("    T1.GRD_REASON, ");
        sql.append("    T1.GRD_SCHOOL, ");
        sql.append("    T1.GRD_ADDR, ");
        sql.append("    T1.GRD_ADDR2,");
        sql.append("    T1.GRD_NO, ");
        sql.append("    T1.GRD_DIV, ");
        sql.append("    T1.GRD_DIV_NAME, ");
        sql.append("    T1.CURRICULUM_YEAR, ");
        sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
        sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ");
        sql.append("    YE.GRADE AS ENT_YEAR_GRADE, ");
        sql.append("    YE.GRADE_CD AS ENT_YEAR_GRADE_CD, ");
        sql.append("    YG.GRADE AS GRD_YEAR_GRADE, ");
        sql.append("    YG.GRADE_CD AS GRD_YEAR_GRADE_CD ");
        sql.append(" FROM STATE_MAIN T1 ");
        sql.append("    LEFT JOIN ENT_GRD_YEARS YE ON YE.YEAR = T1.ENT_YEAR AND YE.SCHREGNO = T1.SCHREGNO ");
        sql.append("    LEFT JOIN ENT_GRD_YEARS YG ON YG.YEAR = T1.GRD_YEAR AND YG.SCHREGNO = T1.SCHREGNO ");
        sql.append(" ) ");

        // 進路用・就職用両方の最新の年度を取得
        sql.append(", TA as( select ");
        sql.append("         T1.SCHREGNO, ");
        sql.append("         '0' as SCH_SENKOU_KIND, ");
        sql.append("         MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, ");
        sql.append("         '1' as COMP_SENKOU_KIND, ");
        sql.append("         MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR ");
        sql.append(" from ");
        sql.append("         AFT_GRAD_COURSE_DAT T1 ");
        sql.append("         INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        sql.append(" where ");
        sql.append("         PLANSTAT = '1'");
        sql.append(" group by ");
        sql.append("         T1.SCHREGNO ");
        // 進路用・就職用どちらか(進路が優先)の最新の受験先種別コードを取得
        sql.append("), TA2 as( select ");
        sql.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, ");
        sql.append("     T1.SCHREGNO, ");
        sql.append("     T1.SENKOU_KIND, ");
        sql.append("     MAX(T1.SEQ) AS SEQ ");
        sql.append(" from ");
        sql.append("     AFT_GRAD_COURSE_DAT T1 ");
        sql.append(" inner join TA on ");
        sql.append("     T1.SCHREGNO = TA.SCHREGNO ");
        sql.append("     and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) ");
        sql.append("     and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) ");
        sql.append(" where ");
        sql.append("     T1.PLANSTAT = '1'");
        sql.append(" group by ");
        sql.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), ");
        sql.append("     T1.SCHREGNO, ");
        sql.append("     T1.SENKOU_KIND ");
        sql.append("), AFT_MAIN AS ( ");
        // 最新の年度と受験先種別コードの感想を取得
        sql.append("select  ");
        sql.append("      T1.SCHREGNO ");
        sql.append("     ,T1.SENKOU_KIND ");
        sql.append("     ,T1.STAT_CD ");
        sql.append("     ,T1.THINKEXAM ");
        sql.append("     ,T1.JOB_THINK ");
        sql.append("     ,L1.NAME1 as E017NAME1 ");
        sql.append("     ,L2.NAME1 as E018NAME1 ");
        sql.append("     ,L3.SCHOOL_NAME ");
        sql.append("     ,T1.FACULTYCD ");
        sql.append("     ,L5.FACULTYNAME ");
        sql.append("     ,T1.DEPARTMENTCD ");
        sql.append("     ,L6.DEPARTMENTNAME ");
        sql.append("     ,L7.ADDR1 AS CAMPUSADDR1 ");
        sql.append("     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ");
        sql.append("     ,L4.COMPANY_NAME ");
        sql.append("     ,L4.ADDR1 AS COMPANYADDR1 ");
        sql.append("     ,L4.ADDR2 AS COMPANYADDR2 ");
        sql.append("     ,ROW_NUMBER() OVER(ORDER BY T1.YEAR) AS ROW_NUM ");
        sql.append("from ");
        sql.append("     AFT_GRAD_COURSE_DAT T1 ");
        sql.append("inner join TA2 on ");
        sql.append("     T1.YEAR = TA2.YEAR ");
        sql.append("     and T1.SCHREGNO = TA2.SCHREGNO ");
        sql.append("     and T1.SENKOU_KIND = TA2.SENKOU_KIND ");
        sql.append("     and T1.SEQ = TA2.SEQ ");
        sql.append("left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD ");
        sql.append("left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD ");
        sql.append("left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD ");
        sql.append("left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD ");
        sql.append("     and L5.FACULTYCD = T1.FACULTYCD ");
        sql.append("left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD ");
        sql.append("     and L6.FACULTYCD = T1.FACULTYCD ");
        sql.append("     and L6.DEPARTMENTCD = T1.DEPARTMENTCD ");
        sql.append("left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD ");
        sql.append("     and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD ");
        sql.append("left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD ");
        sql.append("     and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD ");
        sql.append("left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD ");
        sql.append("where ");
        sql.append("     T1.PLANSTAT = '1' ");
        sql.append(") ");
        sql.append("SELECT ");
        sql.append("T2.NAME,");
        sql.append("T2.REAL_NAME,");
        sql.append("ENTGRD.GRD_DATE, ");
        sql.append("T2.NAME_ENG,");
        sql.append("T2.NAME_KANA,T2.REAL_NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,");
        sql.append("T21.BIRTHDAY_FLG,");
        sql.append("T7.ABBV2 AS SEX_ENG,");
        sql.append("T1.GRADE, T1.HR_CLASS, T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,");
        sql.append("T3.COURSENAME,");
        sql.append("T4.MAJORNAME,");
        sql.append("T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,");
        sql.append("T3.COURSEENG,T4.MAJORENG,");
        sql.append("ENTGRD.FINISH_DATE,");
        sql.append("FIN_S.FINSCHOOL_ZIPCD AS J_ZIPCD,");
        sql.append("FIN_S.FINSCHOOL_NAME AS J_NAME,");
        sql.append("NM_MST.NAME1 AS INSTALLATION_DIV,");
        sql.append("VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
        sql.append("SCHADD.ADDR1, SCHADD.ADDR2, SCHADD.ZIPCD, SCHADD.TELNO, SCHADD.EMAIL, ");
        sql.append("T12.GUARD_NAME, ");
        sql.append("T12.GUARD_KANA,");
        sql.append("T12.GUARD_ADDR1, T12.GUARD_ADDR2, T12.GUARD_ZIPCD, T12.GUARD_TELNO,");
        sql.append("T1.SCHREGNO, ");
        sql.append("    STT.ENT_YEAR, ");
        sql.append("    STT.ENT_DATE, ");
        sql.append("    STT.ENT_REASON, ");
        sql.append("    STT.ENT_SCHOOL, ");
        sql.append("    STT.ENT_ADDR, ");
        sql.append("    STT.ENT_ADDR2,");
        sql.append("    STT.ENT_DIV, ");
        sql.append("    STT.ENT_DIV_NAME, ");
        sql.append("    STT.GRD_YEAR, ");
        sql.append("    STT.GRD_DATE, ");
        sql.append("    STT.GRD_REASON, ");
        sql.append("    STT.GRD_SCHOOL, ");
        sql.append("    STT.GRD_ADDR, ");
        sql.append("    STT.GRD_ADDR2,");
        sql.append("    STT.GRD_NO, ");
        sql.append("    STT.GRD_DIV, ");
        sql.append("    STT.GRD_DIV_NAME, ");
        sql.append("    STT.CURRICULUM_YEAR, ");
        sql.append("    STT.TENGAKU_SAKI_ZENJITU, ");
        sql.append("    STT.NYUGAKUMAE_SYUSSIN_JOUHOU, ");
        sql.append("    STT.ENT_YEAR_GRADE, ");
        sql.append("    STT.ENT_YEAR_GRADE_CD, ");
        sql.append("    STT.GRD_YEAR_GRADE, ");
        sql.append("    STT.GRD_YEAR_GRADE_CD ");
        sql.append("     ,AFT.SENKOU_KIND ");
        sql.append("     ,AFT.STAT_CD ");
        sql.append("     ,AFT.THINKEXAM ");
        sql.append("     ,AFT.JOB_THINK ");
        sql.append("     ,AFT.E017NAME1 ");
        sql.append("     ,AFT.E018NAME1 ");
        sql.append("     ,AFT.SCHOOL_NAME ");
        sql.append("     ,AFT.FACULTYCD ");
        sql.append("     ,AFT.FACULTYNAME ");
        sql.append("     ,AFT.DEPARTMENTCD ");
        sql.append("     ,AFT.DEPARTMENTNAME ");
        sql.append("     ,AFT.CAMPUSADDR1 ");
        sql.append("     ,AFT.CAMPUSFACULTYADDR1 ");
        sql.append("     ,AFT.COMPANY_NAME ");
        sql.append("     ,AFT.COMPANYADDR1 ");
        sql.append("     ,AFT.COMPANYADDR2 ");
        sql.append("     ,AFT.ROW_NUM ");
        sql.append("     ,DET.BASE_REMARK1 ");
        sql.append("FROM SCHREG_REGD_DAT T1 ");
        sql.append("INNER JOIN SCHREGNOS T0 ON T0.SCHREGNO = T1.SCHREGNO ");
        sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
        sql.append("    AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
        sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
        sql.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        sql.append("LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR ");
        sql.append("    AND REGDG.GRADE = T1.GRADE ");
        sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
        sql.append("    AND ENTGRD.SCHOOL_KIND= REGDG.SCHOOL_KIND ");
        sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=T2.SEX ");
        sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = ENTGRD.FINSCHOOLCD ");
        sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
        sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
        sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
        sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
        sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
        sql.append("    AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
        sql.append("LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ");
        sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");
        sql.append("LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) SCHADD0 ON SCHADD0.SCHREGNO = T1.SCHREGNO ");
        sql.append("LEFT JOIN SCHREG_ADDRESS_DAT SCHADD ON SCHADD.SCHREGNO = SCHADD0.SCHREGNO AND  SCHADD.ISSUEDATE = SCHADD0.ISSUEDATE ");
        sql.append("LEFT JOIN STATE STT ON STT.SCHREGNO = T1.SCHREGNO ");
        sql.append("LEFT JOIN AFT_MAIN AFT ON AFT.SCHREGNO = T1.SCHREGNO AND AFT.ROW_NUM = 1 ");
        sql.append("LEFT JOIN SCHREG_BASE_DETAIL_MST DET ON DET.SCHREGNO = T1.SCHREGNO AND DET.BASE_SEQ = '003' ");
        sql.append(" WHERE T1.YEAR= '" + param._year + "' ");
        sql.append("   AND T1.SEMESTER= '" + param._gakki + "' ");
        sql.append(" GROUP BY ");
        sql.append("     T2.NAME, ");
        sql.append("     T2.REAL_NAME, ");
        sql.append("     ENTGRD.GRD_DATE, ");
        sql.append("     T2.NAME_ENG, ");
        sql.append("     T2.NAME_KANA, ");
        sql.append("     T2.REAL_NAME_KANA, ");
        sql.append("     T2.BIRTHDAY, ");
        sql.append("     T7.ABBV1, ");
        sql.append("     T21.BIRTHDAY_FLG, ");
        sql.append("     T7.ABBV2, ");
        sql.append("     T1.GRADE, ");
        sql.append("     T1.HR_CLASS, ");
        sql.append("     T1.ATTENDNO, ");
        sql.append("     T1.ANNUAL, ");
        sql.append("     T6.HR_NAME, ");
        sql.append("     T3.COURSENAME, ");
        sql.append("     T4.MAJORNAME, ");
        sql.append("     T5.COURSECODENAME, ");
        sql.append("     T3.COURSEABBV, ");
        sql.append("     T4.MAJORABBV, ");
        sql.append("     T3.COURSEENG, ");
        sql.append("     T4.MAJORENG, ");
        sql.append("     ENTGRD.FINISH_DATE, ");
        sql.append("     FIN_S.FINSCHOOL_ZIPCD, ");
        sql.append("     FIN_S.FINSCHOOL_NAME, ");
        sql.append("     NM_MST.NAME1, ");
        sql.append("     VALUE(NML019.NAME1, ''), ");
        sql.append("     SCHADD.ADDR1, ");
        sql.append("     SCHADD.ADDR2, ");
        sql.append("     SCHADD.ZIPCD, ");
        sql.append("     SCHADD.TELNO, ");
        sql.append("     SCHADD.EMAIL, ");
        sql.append("     T12.GUARD_NAME, ");
        sql.append("     T12.GUARD_KANA, ");
        sql.append("     T12.GUARD_ADDR1, ");
        sql.append("     T12.GUARD_ADDR2, ");
        sql.append("     T12.GUARD_ZIPCD, ");
        sql.append("     T12.GUARD_TELNO, ");
        sql.append("     T1.SCHREGNO, ");
        sql.append("     STT.ENT_YEAR, ");
        sql.append("     STT.ENT_DATE, ");
        sql.append("     STT.ENT_REASON, ");
        sql.append("     STT.ENT_SCHOOL, ");
        sql.append("     STT.ENT_ADDR, ");
        sql.append("     STT.ENT_ADDR2, ");
        sql.append("     STT.ENT_DIV, ");
        sql.append("     STT.ENT_DIV_NAME, ");
        sql.append("     STT.GRD_YEAR, ");
        sql.append("     STT.GRD_DATE, ");
        sql.append("     STT.GRD_REASON, ");
        sql.append("     STT.GRD_SCHOOL, ");
        sql.append("     STT.GRD_ADDR, ");
        sql.append("     STT.GRD_ADDR2, ");
        sql.append("     STT.GRD_NO, ");
        sql.append("     STT.GRD_DIV, ");
        sql.append("     STT.GRD_DIV_NAME, ");
        sql.append("     STT.CURRICULUM_YEAR, ");
        sql.append("     STT.TENGAKU_SAKI_ZENJITU, ");
        sql.append("     STT.NYUGAKUMAE_SYUSSIN_JOUHOU, ");
        sql.append("     STT.ENT_YEAR_GRADE, ");
        sql.append("     STT.ENT_YEAR_GRADE_CD, ");
        sql.append("     STT.GRD_YEAR_GRADE, ");
        sql.append("     STT.GRD_YEAR_GRADE_CD      , ");
        sql.append("     AFT.SENKOU_KIND      , ");
        sql.append("     AFT.STAT_CD      , ");
        sql.append("     AFT.THINKEXAM      , ");
        sql.append("     AFT.JOB_THINK      , ");
        sql.append("     AFT.E017NAME1      , ");
        sql.append("     AFT.E018NAME1      , ");
        sql.append("     AFT.SCHOOL_NAME      , ");
        sql.append("     AFT.FACULTYCD      , ");
        sql.append("     AFT.FACULTYNAME      , ");
        sql.append("     AFT.DEPARTMENTCD      , ");
        sql.append("     AFT.DEPARTMENTNAME      , ");
        sql.append("     AFT.CAMPUSADDR1      , ");
        sql.append("     AFT.CAMPUSFACULTYADDR1      , ");
        sql.append("     AFT.COMPANY_NAME      , ");
        sql.append("     AFT.COMPANYADDR1      , ");
        sql.append("     AFT.COMPANYADDR2      , ");
        sql.append("     AFT.ROW_NUM      , ");
        sql.append("     DET.BASE_REMARK1 ");
        sql.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");

        return sql.toString();
    }

    private static String KNJ_EditDateSetDateFormat(final String data, final String year) {
        return data;
    }

    /**
     * 個人情報クラスを作成ます。
     */
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {

        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(param);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                svf.VrSetForm("KNJA139A.frm", 1);

                // デフォルト印刷
                final String setDateFormat = KNJ_EditDateSetDateFormat(null, _param._year);
                svf.VrsOut("J_GRADUATEDDATE_YE", setDateFormat);
                svf.VrsOut("ENTERDATE1", setDateFormat);
                svf.VrsOut("TRANSFER_DATE_1", setDateFormat);
                svf.VrsOut("TRANSFER_DATE_2", setDateFormat);
                svf.VrsOut("TRANSFER_DATE3_1", setDateFormat + "\uFF5E" + setDateFormat);
                svf.VrsOut("TRANSFER_DATE_4", setDateFormat);

                svf.VrsOut("SCHREG_NO", rs.getString("SCHREGNO"));

                String _entDate = rs.getString("ENT_DATE");
                final String entDivName = StringUtils.defaultString(rs.getString("ENT_DIV_NAME"));
                final int iEntDiv = null == rs.getString("ENT_DIV") ? -1 : Integer.parseInt(rs.getString("ENT_DIV"));
                final String fieldEnterDiv;
                final StringBuffer enterDiv = new StringBuffer();
                final String entDateFormat = KNJ_EditDateSetDateFormat(KNJ_EditDate.h_format_JP(_entDate), _param._year);
                if (4 == iEntDiv) {
                    // 転入学を印字します。
                    svf.VrsOut("ENTERDATE1", entDateFormat);

                    fieldEnterDiv = "ENTERDATE3";
                    enterDiv.append(entDivName);
                } else if (5 == iEntDiv) {
                    // 編入学を印字します。
                    svf.VrsOut("ENTERDATE1", entDateFormat);
                    fieldEnterDiv = "ENTERDATE3";
                    enterDiv.append(entDivName);
                } else {
                    // 入学を印字します。
                    svf.VrsOut("ENTERDATE1", entDateFormat);
                    fieldEnterDiv = "ENTERDATE3";

                    enterDiv.append(entDivName);
                }
                svf.VrsOut(fieldEnterDiv, enterDiv.toString());

                final int iGrdDiv = null == rs.getString("GRD_DIV") ? -1 : Integer.parseInt(rs.getString("GRD_DIV"));
                if (3 == iGrdDiv || 2 == iGrdDiv) {
                    svf.VrsOut("TRANSFER_DATE_2", KNJ_EditDateSetDateFormat(KNJ_EditDate.h_format_JP(rs.getString("GRD_DATE")), _param._year));
                    svf.VrsOut("tengaku_GRADE", NumberUtils.isDigits(rs.getString("GRD_YEAR_GRADE_CD")) ? String.valueOf(Integer.parseInt(rs.getString("GRD_YEAR_GRADE_CD"))) : " ");
                    svf.VrsOut("TRANSFERREASON2_1", rs.getString("GRD_REASON"));
                    svf.VrsOut("TRANSFERREASON2_2", rs.getString("GRD_SCHOOL"));
                    svf.VrsOut("TRANSFERREASON2_3", rs.getString("GRD_ADDR"));
                    if ("1".equals(_param._useAddrField2)) {
                        final boolean useField2 = getMS932ByteLength(rs.getString("GRD_ADDR")) > 50 || getMS932ByteLength(rs.getString("GRD_ADDR2")) > 50;
                        if (null != rs.getString("GRD_ADDR2")) {
                            svf.VrsOut("TRANSFERREASON2_2" + (useField2 ? "_2" : ""), rs.getString("GRD_ADDR"));
                            svf.VrsOut("TRANSFERREASON2_3" + (useField2 ? "_2" : ""), rs.getString("GRD_ADDR2"));
                        } else {
                            svf.VrsOut("TRANSFERREASON2_2" + (useField2 ? "_2" : ""), rs.getString("GRD_ADDR"));
                        }
                    } else {
                        svf.VrsOut("TRANSFERREASON2_2", rs.getString("GRD_ADDR")); // NO007
                        svf.VrsOut("TRANSFERREASON1_3", rs.getString("GRD_REASON"));
                    }
                    String kubun = "";
                    if (3 == iGrdDiv) { // 転学
                        kubun = "転学";
                    } else if (2 == iGrdDiv) { // 退学
                        kubun = "退学";
                    }
                    svf.VrsOut("KUBUN", kubun);
                } else if (1 == iGrdDiv) { // 卒業
                    svf.VrsOut("TRANSFER_DATE_4", KNJ_EditDateSetDateFormat(KNJ_EditDate.h_format_JP(rs.getString("GRD_DATE")), _param._year));
                    svf.VrsOut("FIELD1", rs.getString("GRD_NO")); // 卒業台帳番号
                }


                boolean isPringGuarantor = false;
                String studentName = null;
                /** 最も古い履歴の生徒名 */
                String studentNameHistFirst = null;

                String schKana = null;
                String guardKana = null;
                String guardName = null;
                studentName          = StringUtils.defaultString(rs.getString("NAME"));
                schKana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                guardKana = StringUtils.defaultString(rs.getString("GUARD_KANA"));
                guardName = StringUtils.defaultString(rs.getString("GUARD_NAME"));

                /** 最も古い履歴の生徒名 */

                // 個人情報印刷
                svf.VrsOut("COURSE", rs.getString("COURSENAME"));
                svf.VrsOut("DEPARTMENT", rs.getString("MAJORNAME"));

                final KNJSvfFieldInfo fkana = new KNJSvfFieldInfo(629, 1662, KNJSvfFieldModify.charSizeToPixel(9.0), 842, -1, -1, 12, 100);
                final KNJSvfFieldInfo fgKana = new KNJSvfFieldInfo(629, 1662, KNJSvfFieldModify.charSizeToPixel(9.0), 2258, -1, -1, 12, 100);
                final KNJSvfFieldInfo fname = new KNJSvfFieldInfo(629, 1662, KNJSvfFieldModify.charSizeToPixel(14.0), 945, 914, 965, 24, 48);
                final KNJSvfFieldInfo fgName = new KNJSvfFieldInfo(629, 1662, KNJSvfFieldModify.charSizeToPixel(14.0), 2376, 2351, 2401, 24, 48);

                printKana(svf, "KANA", schKana, fkana);

                isPringGuarantor = false; // getPrintGuarantor(diffYear(rs.getString("BIRTHDAY"), _entDate));
                final String guarKana = guardKana; // isPringGuarantor ? guarantorKana : guardKana;
                printKana(svf, "GUARD_KANA", guarKana, fgKana);

                final String printName;
                printName = studentName;
                printName(svf, studentNameHistFirst, printName, "NAME", fname);

                final String guarName = guardName; // isPringGuarantor ? guarantorName : guardName;
                final String guarNameHistFirst = null; // isPringGuarantor ? guarantorNameHistFirst : guardNameHistFirst;
                printName(svf, guarNameHistFirst, guarName, "GUARD_NAME", fgName);

                svf.VrsOut("BIRTHDAY", getBirthday(rs.getString("BIRTHDAY"), rs.getString("BIRTHDAY_FLG"), param) + "生");
                svf.VrsOut("SEX", rs.getString("SEX"));
                svf.VrsOut("J_GRADUATEDDATE_Y", KNJ_EditDateSetDateFormat(KNJ_EditDate.h_format_JP(rs.getString("FINISH_DATE")), param._year));
                svf.VrsOut("INSTALLATION_DIV", rs.getString("INSTALLATION_DIV"));
                // 入学前学歴の学校名編集
                if ("1".equals(param._notPrintFinschooltypeName)) {
                    printSvfFinSchool(svf, rs.getString("J_ZIPCD"), rs.getString("J_NAME"), "卒業");
                } else {
                    printSvfFinSchool(svf, rs.getString("J_ZIPCD"), rs.getString("J_NAME"), rs.getString("FINSCHOOL_TYPE_NAME") + "卒業");
                }

                final boolean isPrintAddr2 = true; // "1".equals(rs.getString("ADDR_FLG"));
                if (null != rs.getString("ZIPCD")) {
                    svf.VrsOut("ZIPCODE1", "〒" + rs.getString("ZIPCD"));
                }

                final int n1 = getMS932ByteLength(rs.getString("ADDR1"));
                printAddr1(svf, "ADDRESS1", rs.getString("ADDR1"), n1);

                if (isPrintAddr2) {
                    printAddr2(svf, "ADDRESS1", rs.getString("ADDR2"), n1);
                }

                svf.VrsOut("TELNO", rs.getString("TELNO"));
                svf.VrsOut("EMAIL_ADDR" + (getMS932ByteLength(rs.getString("EMAIL")) <= 50 ? "1" : "2"), rs.getString("EMAIL"));
                svf.VrsOut("EXAM_NO", rs.getString("BASE_REMARK1"));

                // 保護者住所履歴印刷
                svf.VrsOut("GRD_HEADER", isPringGuarantor ? "保証人" : "保護者");

                final boolean isPrintGuardAddr2 = true; // "1".equals(rs.getString("GUARD_ADDR_FLG"));

                if (null != rs.getString("GUARD_ZIPCD")) {
                    svf.VrsOut("GUARDZIP1", "〒" + rs.getString("GUARD_ZIPCD"));
                }

                final int keta1 = getMS932ByteLength(StringUtils.defaultString(rs.getString("GUARD_ADDR1")));
                printAddr1(svf, "GUARDIANADD1", StringUtils.defaultString(rs.getString("GUARD_ADDR1")), keta1);

                if (isPrintGuardAddr2) {
                    printAddr2(svf, "GUARDIANADD1", StringUtils.defaultString(rs.getString("GUARD_ADDR2")), keta1);
                }

                svf.VrsOut("GURAD_TELNO", rs.getString("GUARD_TELNO"));

                final List textList = new ArrayList();
                if ("0".equals(rs.getString("SENKOU_KIND"))) { // 進学
                    if (null == rs.getString("STAT_CD") || null != rs.getString("E017NAME1")) {
                        final String[] token = KNJ_EditEdit.get_token(rs.getString("THINKEXAM"), 50, 10);
                        if (null != token) {
                            textList.addAll(Arrays.asList(token));
                        }
                    } else {
                        textList.add(StringUtils.defaultString(rs.getString("SCHOOL_NAME")));
                        final String faculutyname = "000".equals(rs.getString("FACULTYCD")) || null == rs.getString("FACULTYNAME") ? "" : rs.getString("FACULTYNAME");
                        final String departmentname = "000".equals(rs.getString("DEPARTMENTCD")) || null == rs.getString("DEPARTMENTNAME") ? "" : rs.getString("DEPARTMENTNAME");
                        textList.add(faculutyname + departmentname);
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

                for (int i = 0; i < textList.size(); i++) {
                    final String line = (String) textList.get(i);
                    final String field = "AFTER_GRADUATION" + String.valueOf(i + 1) + (getMS932ByteLength(line) > 50 ? "_2" : "");
                    svf.VrsOut(field, line);
                }

                svf.VrEndPage();
                printEveryDayData(db2, svf, param, rs.getString("SCHREGNO"));

                nonedata = true;
            }

        } catch (final SQLException e) {
            log.error("個人情報クラス作成にてエラー", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return nonedata;
    }

    /**
     * 個人情報クラスを作成ます。
     * @throws SQLException
     */
    private void printEveryDayData(final DB2UDB db2, final Vrw32alp svf, final Param param, final String schregNo) throws SQLException {
        PreparedStatement psInfo = null;
        ResultSet rsInfo = null;
        String hrName = "";
        String attendNo = "";
        String name = "";
        try {
            final String sql = getSchoInfoSql(param, schregNo);
            log.info(" sql = " + sql);
            psInfo = db2.prepareStatement(sql);
            rsInfo = psInfo.executeQuery();
            rsInfo.next();
            hrName = rsInfo.getString("HR_NAME");
            attendNo = rsInfo.getString("ATTENDNO");
            name = rsInfo.getString("NAME");
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psInfo, rsInfo);
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getEveryDaySql(schregNo);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrSetForm("KNJA139A_2.frm", 1);
            String befYear = "";
            final int maxLine = 50;
            int lineCnt = 1;
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                if (!befYear.equals("") && !befYear.equals(year)) {
                    printOutEvery(svf, param, schregNo, hrName, attendNo, name, befYear);
                    lineCnt = 1;
                }
                final String findDate = rs.getString("FIND_DATE");
                final String findTime = rs.getString("FIND_TIME");
                final String title = rs.getString("TITLE");
                final String staffname = rs.getString("STAFFNAME");
                svf.VrsOutn("TEXT", lineCnt, "【" + KNJ_EditDate.h_format_JP(findDate) + "　" + findTime + "　" + title + "　" + staffname + "】");
                lineCnt++;
                if (lineCnt > maxLine) {
                    printOutEvery(svf, param, schregNo, hrName, attendNo, name, befYear);
                    lineCnt = 1;
                }
                final String text = rs.getString("TEXT");
                final String[] textArray = KNJ_EditEdit.get_token(text, 60, 5);
                for (int textCnt = 0; textCnt < textArray.length; textCnt++) {
                    if (null == textArray[textCnt]) {
                        continue;
                    }
                    svf.VrsOutn("TEXT", lineCnt, textArray[textCnt]);
                    lineCnt++;
                    if (lineCnt > maxLine) {
                        printOutEvery(svf, param, schregNo, hrName, attendNo, name, befYear);
                        lineCnt = 1;
                    }
                }
                befYear = year;
            }

            if (!befYear.equals("")) {
                printOutEvery(svf, param, schregNo, hrName, attendNo, name, befYear);
            }

        } catch (final SQLException e) {
            log.error("個人情報クラス作成にてエラー", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printOutEvery(final Vrw32alp svf, final Param param, final String schregNo, final String hrName, final String attendNo, final String name, final String befYear) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(befYear + "-04-01") + "度　生徒　所見・連絡事項");
        svf.VrsOut("HR_NAME", hrName);
        svf.VrsOut("NAME", name);
        svf.VrsOut("SCHREG_NO", schregNo);
        svf.VrsOut("PRINTDAY", KNJ_EditDate.h_format_JP(param._ctrlDate));
        svf.VrEndPage();
    }

    private String getSchoInfoSql(final Param param, final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO  = BASE.SCHREGNO  ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + param._year + "' ");
        stb.append("     AND REGD.SEMESTER = '" + param._gakki + "' ");
        stb.append("     AND REGD.SCHREGNO = '" + schregNo + "' ");
        return stb.toString();
    }

    private String getEveryDaySql(final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     EVERY_D.*, ");
        stb.append("     FISCALYEAR(EVERY_D.FIND_DATE) AS YEAR, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     EVERYDAY_FINDINGS_DAT EVERY_D ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON EVERY_D.REGISTERCD = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     EVERY_D.SCHREGNO = '" + schregNo + "' ");
        stb.append(" ORDER BY ");
        stb.append("     FISCALYEAR(EVERY_D.FIND_DATE), ");
        stb.append("     EVERY_D.FIND_DATE ");
        return stb.toString();
    }

    // 入学・編入学・転入学

    private void printAddr1(final Vrw32alp svf, final String field, final String addr1, final int keta1) {
        String p = null;
        if ("1".equals(_param._useAddrField2) && 50 < keta1) {
            p = "_1_3";
        } else if (40 < keta1) {
            p = "_1_2";
        } else if (0 < keta1) {
            p = "_1_1";
        }
        if (p != null) {
            svf.VrsOut(field + p, addr1);
        }
    }

    private void printAddr2(final Vrw32alp svf, final String field, final String addr2, final int keta1) {
        final int keta2 = getMS932ByteLength(addr2);
        String p = null;
        if ("1".equals(_param._useAddrField2) && (50 < keta2 || 50 < keta1)) {
            p = "_2_3";
        } else if (40 < keta2 || 40 < keta1) {
            p = "_2_2";
        } else if (0 < keta2) {
            p = "_2_1";
        }
        if (p != null) {
            svf.VrsOut(field + p, addr2);
        }
    }

    /**
     * かなを表示する
     */
    private void printKana(final Vrw32alp svf, final String fieldKana, final String schKana, final KNJSvfFieldInfo fkana) {

        final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldKana, fkana._x1, fkana._x2, fkana._height, fkana._ystart, fkana._minnum, fkana._maxnum);
        final float charSize = modify.getCharSize(schKana);
        svf.VrAttribute(fieldKana, "Size=" + charSize);
        svf.VrAttribute(fieldKana, "Y=" + (int) modify.getYjiku(0, charSize));
        svf.VrsOut(fieldKana, schKana);
    }

    /**
     * 名前を表示する。
     */
    private void printName(final Vrw32alp svf, final String nameHistFirst, String name, final String field, final KNJSvfFieldInfo fname) {

        final String fieldname = field + "1";
        final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldname, fname._x1, fname._x2, fname._height, fname._ystart, fname._minnum, fname._maxnum);
        final float charSize = modify.getCharSize(name);
        svf.VrAttribute(fieldname, "Size=" + charSize);
        svf.VrAttribute(fieldname, "Y=" + (int) modify.getYjiku(0, charSize));
        svf.VrsOut(fieldname, name);
    }

    /**
     *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
     *  高校指導要録・中学指導要録・中等学校指導要録の様式１で使用しています。
     *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
     *  全角スペースより前半の文字を○○○○○立と見なします。
     *  @param str1 例えば"千代田区　アルプ"
     *  @param koteiSrc 例えば"小学校卒業"
     */
    private void printSvfFinSchool(
            final Vrw32alp svf,
            final String jZipcd,
            final String str1,
            final String koteiSrc
    ) {
        String schoolName = StringUtils.defaultString(str1);
        final char splitchar = '　';
        final int i = StringUtils.defaultString(str1).indexOf(splitchar);
        if (-1 < i && 5 >= i) {
            schoolName = str1.substring(i + 1);
        }
        if (!StringUtils.isBlank(jZipcd)) {
            svf.VrsOut("FINSCHOOL_ZIP", "〒" + jZipcd);
        }
        final String kotei = StringUtils.defaultString(koteiSrc);
        if (getMS932ByteLength(schoolName) == 0) {
            svf.VrsOut("FINSCHOOL1", kotei);
        } else if (getMS932ByteLength(schoolName) + getMS932ByteLength(kotei) <= 40) {
            svf.VrsOut("FINSCHOOL1", schoolName + kotei);
        } else if(getMS932ByteLength(schoolName) + getMS932ByteLength(kotei) <= 50) {
            svf.VrsOut("FINSCHOOL2", schoolName + kotei);
        } else {
            svf.VrsOut("FINSCHOOL2", schoolName);
            svf.VrsOut("FINSCHOOL3", kotei);
        }
    }

    private static class KNJSvfFieldInfo {
        int _x1;   //開始位置X(ドット)
        int _x2;   //終了位置X(ドット)
        int _height;  //フィールドの高さ(ドット)
        int _ystart;  //開始位置Y(ドット)
        int _ystart1;  //開始位置Y(ドット)フィールド1
        int _ystart2;  //開始位置Y(ドット)フィールド2
        int _minnum;  //最小設定文字数
        int _maxnum;  //最大設定文字数
        public KNJSvfFieldInfo(final int x1, final int x2, final int height, final int ystart, final int ystart1, final int ystart2, final int minnum, final int maxnum) {
            _x1 = x1;
            _x2 = x2;
            _height = height;
            _ystart = ystart;
            _ystart1 = ystart1;
            _ystart2 = ystart2;
            _minnum = minnum;
            _maxnum = maxnum;
        }
        public KNJSvfFieldInfo() {
            this(-1, -1, -1, -1, -1, -1, -1, -1);
        }
    }

    public static class KNJSvfFieldModify {

        private static final Log log = LogFactory.getLog(KNJSvfFieldModify.class);

        private final String _fieldname; // フィールド名
        private final int _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        public KNJSvfFieldModify(String fieldname, int x1, int x2, int height, int ystart, int minnum, int maxnum) {
            _fieldname = fieldname;
            _width = x2 - x1;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public float getCharSize(String str) {
            return Math.min((float) pixelToCharSize(_height), retFieldPoint(_width, getStringByteSize(str)));                  //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private int getStringByteSize(String str) {
            return Math.min(Math.max(getMS932ByteLength(str), _minnum), _maxnum);
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charSize 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static int charSizeToPixel(final double charSize) {
            return (int) Math.round(charSize / 72 * 400);
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharSize(final int pixel) {
            return pixel / 400.0 * 72;
        }

        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public float getYjiku(int hnum, float charSize) {
            float jiku = 0;
            try {
                jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
            } catch (Exception ex) {
                log.error("setRetvalue error!", ex);
                log.debug(" jiku = " + jiku);
            }
            return jiku;
        }

        /**
         *  文字サイズを設定
         */
        private static float retFieldPoint(int width, int num) {
            return (float) Math.round((float) width / (num / 2) * 72 / 400 * 10) / 10;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static float retFieldY(int height, float charSize) {
            return (float) Math.round(((double) height - (charSize / 72 * 400)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    private static class Param {

        final String _year;
        final String _gakki;
        final String _ctrlDate;

        final String _output;
        final String[] _categorySelected;
        final String[] _selectedHR;

        final String _useAddrField2;
        /** FINSCHOOL_MST.FINSCHOOL_TYPE(名称マスタ「L019」、「中学校」「小学校」等)を表示しない */
        final String _notPrintFinschooltypeName;

        /** 生年月日に西暦を使用するか */
        final boolean _isSeireki;
        final boolean _hasAftGradCourseDat;
        final KNJSchoolMst _knjSchoolMst;

        Param(final HttpServletRequest request, final DB2UDB db2) throws Exception {

            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _ctrlDate = request.getParameter("CTRL_DATE");

            _output = request.getParameter("OUTPUT");    // 1=個人, 2=クラス
            _categorySelected = request.getParameterValues("category_selected"); // 複数生徒 or 複数年組

            if ("2".equals(_output)) {
                _selectedHR = _categorySelected;
            } else {
                _selectedHR = request.getParameterValues("GRADE_HR_CLASS");
            }

            _knjSchoolMst = new KNJSchoolMst(db2, _year);

            _useAddrField2 = request.getParameter("useAddrField2");
            _notPrintFinschooltypeName = request.getParameter("notPrintFinschooltypeName");

            _isSeireki = getSeireki(db2);
            _hasAftGradCourseDat = setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
        }

        private boolean getSeireki(final DB2UDB db2) {
            boolean isSeireki = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2 = '00'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    isSeireki = "2".equals(rs.getString("NAME1"));
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return isSeireki;
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