// kanji=äøéö
/*
 * $Id: RegdUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * çÏê¨ì˙: 2017/05/30 14:23:11 - JST
 * çÏê¨é“: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.knjxTool;

import java.sql.SQLException;
import java.util.Iterator;

import jp.co.alp.kenja.batch.knjxTool.Param.NameMst;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ê∂ìkèÓïÒÉeÅ[ÉuÉãÅB
 *
 * @author m-yamashiro
 * @version $Id: RegdUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class RegdUpdate {
    /* pkg */static final Log log = LogFactory.getLog(RegdUpdate.class);

    protected final Param _param;

    public RegdUpdate(final Param param, final Database knj, final String title) throws SQLException {
        _param = param;

        for (Iterator iterator = _param._a023List.iterator(); iterator.hasNext();) {
            final NameMst nameMst = (NameMst) iterator.next();
            log.info("Åö" + title + "Å@" + nameMst._abbv1);
            saveData(knj, nameMst);
        }
    }

    private void saveData(final Database knj, final NameMst nameMst) throws SQLException {
        int count = 0;
        String whereSql1 = "";
        String whereSql2 = "";
        if ("P".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '1' ";
            whereSql2 = " WHERE t1.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' ";
        }
        if ("J".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '2' ";
            whereSql2 = " WHERE t1.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' ";
        }
        if ("H".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '3' OR BUMONCD = '4' ";
            whereSql2 = " WHERE t1.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND t1.year > '2003' ";
        }
        final String deleteSql = getDeleteSql(whereSql1);
        knj.executeUpdate(deleteSql);
        final String insertSql = getInsertSql(whereSql2);
        int cnt = knj.executeUpdate(insertSql);
        count += cnt;
        log.debug("ÉfÅ[É^êî=" + count);
    }

    private String getDeleteSql(final String whereSql1) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM ");
        stb.append("     TOOL_SCHREG_DAT ");
        stb.append(whereSql1);

        return stb.toString();
    }

    private String getInsertSql(final String whereSql2) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO TOOL_SCHREG_DAT  ");
        stb.append(" SELECT  ");
        stb.append("     t1.YEAR, ");
        stb.append("     t2.SCHREGNO, ");
        stb.append("     CASE ");
        stb.append("     WHEN t1.HR_CLASS = '011' ");
        stb.append("     THEN '4' ");
        stb.append("     WHEN t1.GRADE BETWEEN  '01' AND '06' ");
        stb.append("     THEN '1' ");
        stb.append("     WHEN t1.GRADE BETWEEN  '07' AND '09' ");
        stb.append("     THEN '2' ");
        stb.append("     WHEN t1.GRADE BETWEEN  '10' AND '12' ");
        stb.append("     THEN '3' ");
        stb.append("     END as BUMONCD, ");
        stb.append("     CASE ");
        stb.append("     WHEN t1.HR_CLASS = '011' ");
        stb.append("     THEN 'î¸Éf' ");
        stb.append("     WHEN t1.GRADE BETWEEN  '01' AND '06' ");
        stb.append("     THEN 'èxè¨' ");
        stb.append("     WHEN t1.GRADE BETWEEN  '07' AND '09' ");
        stb.append("     THEN 'èxíÜ' ");
        stb.append("     WHEN t1.GRADE BETWEEN  '10' AND '12' ");
        stb.append("     THEN 'èxçÇ' ");
        stb.append("     END as BUMONNAME, ");
        stb.append("     t3.PRESENT_EST, ");
        stb.append("     ltrim(t1.GRADE,'0') AS GRADE, ");
        stb.append("     CASE ");
        stb.append("     WHEN t9.HR_CLASS_NAME1 IS NOT NULL ");
        stb.append("     THEN t9.HR_CLASS_NAME1 ");
        stb.append("     ELSE ");
        stb.append("     t1.HR_CLASS ");
        stb.append("     END AS HR_CLASS, ");
        stb.append("     ltrim(t1.ATTENDNO,'0') AS ATTENDNO, ");
        stb.append("     t2.NAME, ");
        stb.append("     t2.NAME_KANA, ");
        stb.append("     t2.SEX, ");
        stb.append("     SUBSTR(T1.SCHREGNO,5,1) as ENT_BUMONCD, ");
        stb.append("     CASE ");
        stb.append("     WHEN SUBSTR(T1.SCHREGNO,5,1) = '1'  ");
        stb.append("     THEN 'èxè¨' ");
        stb.append("     WHEN SUBSTR(T1.SCHREGNO,5,1) = '2'  ");
        stb.append("     THEN 'èxíÜ' ");
        stb.append("     WHEN SUBSTR(T1.SCHREGNO,5,1) = '3'  ");
        stb.append("     THEN 'èxçÇ' ");
        stb.append("     WHEN SUBSTR(T1.SCHREGNO,5,1) = '4'  ");
        stb.append("     THEN 'î¸Éf' ");
        stb.append("     END as ENT_BUMONNAME, ");
        stb.append("     t2.ENT_DATE, ");
        stb.append("     CASE WHEN ENT_DIV IS NULL ");
        stb.append("     THEN '0' ");
        stb.append("     ELSE ");
        stb.append("     t2.ENT_DIV ");
        stb.append("     END AS ENT_DIV, ");
        stb.append("     t4.NAME1 as ENT_NAME, ");
        stb.append("     CASE ");
        stb.append("     WHEN GRD_DATE between w1.SDATE and w1.EDATE or GRD_DATE < w1.SDATE ");
        stb.append("     THEN GRD_DATE ");
        stb.append("     ELSE '9999-01-01' ");
        stb.append("     END AS GRD_DATE, ");
        stb.append("     CASE ");
        stb.append("     WHEN GRD_DATE between w1.SDATE and w1.EDATE or GRD_DATE < w1.SDATE ");
        stb.append("     THEN t2.GRD_DIV ");
        stb.append("     ELSE '0' ");
        stb.append("     END AS GRD_DIV, ");
        stb.append("     CASE ");
        stb.append("     WHEN GRD_DATE between w1.SDATE and w1.EDATE or GRD_DATE < w1.SDATE ");
        stb.append("     THEN t7.NAME1 ");
        stb.append("     ELSE NULL ");
        stb.append("     END as GRD_NAME, ");
        stb.append("     CASE T1.COURSECODE ");
        stb.append("     WHEN '0000' ");
        stb.append("     THEN '000' ");
        stb.append("     WHEN '1000' ");
        stb.append("     THEN '201' ");
        stb.append("     WHEN '2000' ");
        stb.append("     THEN '202' ");
        stb.append("     WHEN '3000' ");
        stb.append("     THEN '100' ");
        stb.append("     WHEN '4000' ");
        stb.append("     THEN '100' ");
        stb.append("     WHEN '5000' ");
        stb.append("     THEN '000' ");
        stb.append("     WHEN '6000' ");
        stb.append("     THEN '000' ");
        stb.append("     WHEN '7000' ");
        stb.append("     THEN '000' ");
        stb.append("     WHEN '8000' ");
        stb.append("     THEN '000' ");
        stb.append("     WHEN '9000' ");
        stb.append("     THEN '000' ");
        stb.append("     ELSE NULL ");
        stb.append("     END AS COURSECODE, ");
        stb.append("     t5.COURSECODENAME, ");
        stb.append("     t2.FINSCHOOLCD, ");
        stb.append("     t6.FINSCHOOL_NAME, ");
        stb.append("     CASE ");
        stb.append("     WHEN t6.FINSCHOOL_DIV IS NULL ");
        stb.append("     THEN '9' ");
        stb.append("     ELSE t6.FINSCHOOL_DIV ");
        stb.append("     END AS FINSCHOOL_DIV, ");
        stb.append("     t6.DISTRICTCD, ");
        stb.append("     CASE ");
        stb.append("     WHEN t6.DISTRICTCD IS NULL ");
        stb.append("     THEN 'åßäO' ");
        stb.append("     ELSE t8.NAME1 ");
        stb.append("     END as DISTRICTNAME, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T2.UPDATED ");
        stb.append("     THEN T1.UPDATED ");
        stb.append("     ELSE T2.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     t2.REGISTERCD, ");
        stb.append("     sysdate() as COMPDATETIME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST t2 ");
        stb.append("     left join (select a2.* from ");
        stb.append("                                 ( ");
        stb.append("                                 SELECT ");
        stb.append("                                     year, ");
        stb.append("                                     schregno, ");
        stb.append("                                     max(semester) as semester ");
        stb.append("                                 FROM ");
        stb.append("                                     schreg_regd_dat ");
        stb.append("                                 GROUP BY ");
        stb.append("                                     year, ");
        stb.append("                                     schregno ");
        stb.append("                                 ) as a1 ");
        stb.append("                                 left join schreg_regd_dat a2 on a1.year = a2.year and a1.schregno = a2.schregno and a1.semester = a2.semester  ");
        stb.append("                 ) t1 on t2.SCHREGNO = t1.SCHREGNO ");
        stb.append("     left join SCHREG_REGD_GDAT t10 on t1.YEAR = t10.YEAR and t1.GRADE = t10.GRADE ");
        stb.append("     left join SCHOOL_MST t3 on int(t1.YEAR) - int(t10.grade_cd) + 1 = t3.YEAR and t3.SCHOOL_KIND = t10.SCHOOL_KIND ");
        stb.append("     left join NAME_MST t4 on t2.ENT_DIV = t4.NAMECD2 and t4.NAMECD1 = 'A002' ");
        stb.append("     left join COURSECODE_MST t5 on t1.COURSECODE = t5.COURSECODE ");
        stb.append("     left join FINSCHOOL_MST t6 on t2.FINSCHOOLCD = t6.FINSCHOOLCD ");
        stb.append("     left join NAME_MST t7 on t2.GRD_DIV = t7.NAMECD2 and t7.NAMECD1 = 'A003' ");
        stb.append("     left join NAME_MST t8 on t6.DISTRICTCD = t8.NAMECD2 and t8.NAMECD1 = 'Z003' ");
        stb.append("     left join SCHREG_REGD_HDAT t9 on t1.YEAR = t9.YEAR and t1.SEMESTER = t9.SEMESTER and t1.GRADE = t9.GRADE and t1.HR_CLASS = t9.HR_CLASS ");
        stb.append("     left join (SELECT YEAR,MIN(SDATE) AS SDATE, MAX(EDATE) AS EDATE FROM SEMESTER_MST GROUP BY YEAR) w1 on t1.year = w1.year ");
        stb.append(whereSql2);

        return stb.toString();
    }
} // RegdUpdate

// eof
