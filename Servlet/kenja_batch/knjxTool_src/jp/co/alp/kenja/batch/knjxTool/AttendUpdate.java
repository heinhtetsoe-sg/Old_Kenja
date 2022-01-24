// kanji=漢字
/*
 * $Id: AttendUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
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
 * クラス情報テーブル。
 *
 * @author m-yamashiro
 * @version $Id: AttendUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class AttendUpdate {
    /* pkg */static final Log log = LogFactory.getLog(AttendUpdate.class);

    protected final Param _param;

    public AttendUpdate(final Param param, final Database knj, final String title) throws SQLException {
        _param = param;

        for (Iterator iterator = _param._a023List.iterator(); iterator.hasNext();) {
            final NameMst nameMst = (NameMst) iterator.next();
            log.info("★" + title + "　" + nameMst._abbv1);
            saveData(knj, nameMst);
        }
    }

    private void saveData(final Database knj, final NameMst nameMst) throws SQLException {
        int count = 0;
        String whereSql1 = "";
        String whereSql2 = "";
        String whereSql3 = "";
        if ("P".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '1' AND NENDO = '" + _param._year + "' ";
            whereSql2 = " WHERE T2.GRADE BETWEEN  '01' AND '06' ";
            whereSql3 = " WHERE YEAR = '" + _param._year + "' ";
        }
        if ("J".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '2' AND NENDO = '" + _param._year + "' ";
            whereSql2 = " WHERE T2.GRADE BETWEEN  '07' AND '09' ";
            whereSql3 = " WHERE YEAR = '" + _param._year + "' ";
        }
        if ("H".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '3' OR BUMONCD = '4' AND NENDO = '" + _param._year + "' ";
            whereSql2 = " WHERE T2.GRADE BETWEEN  '10' AND '12' ";
            whereSql3 = " WHERE YEAR = '" + _param._year + "' ";
        }
        final String deleteSql = getDeleteSql(whereSql1);
        knj.executeUpdate(deleteSql);
        final String insertSql = getInsertSql(whereSql2, whereSql3);
        int cnt = knj.executeUpdate(insertSql);
        count += cnt;
        log.debug("データ数=" + count);
    }

    private String getDeleteSql(final String whereSql1) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM ");
        stb.append("     TOOL_ATTEND_DAT ");
        stb.append(whereSql1);

        return stb.toString();
    }

    private String getInsertSql(final String whereSql2, final String whereSql3) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO TOOL_ATTEND_DAT  ");
        stb.append(" SELECT  ");
        stb.append("     t1.YEAR, ");
        stb.append("     CASE ");
        stb.append("     WHEN HR_CLASS = '011' ");
        stb.append("     THEN '4' ");
        stb.append("     WHEN GRADE BETWEEN  '01' AND '06' ");
        stb.append("     THEN '1' ");
        stb.append("     WHEN GRADE BETWEEN  '07' AND '09' ");
        stb.append("     THEN '2' ");
        stb.append("     WHEN GRADE BETWEEN  '10' AND '12' ");
        stb.append("     THEN '3' ");
        stb.append("     ELSE '3' ");
        stb.append("     END as BUMONCD, ");
        stb.append("     t1.SEMESTER, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     ltrim(t2.GRADE,'0') as GRADE, ");
        stb.append("     CASE ");
        stb.append("     WHEN W3.COUNT > 0 ");
        stb.append("     THEN '2' ");
        stb.append("     WHEN (t3.GRD_DIV IS NULL OR t3.GRD_DIV = '4') or (t3.GRD_DIV IS NOT NULL and t4.EDATE < t3.GRD_DATE) ");
        stb.append("     THEN '1' ");
        stb.append("     ELSE '0' ");
        stb.append("     END as ENT_DIV, ");
        stb.append("     t1.LESSON, ");
        stb.append("     t1.MOURNING, ");
        stb.append("     t1.SUSPEND, ");
        stb.append("     t1.ABSENT, ");
        stb.append("     t1.SICK + t1.NOTICE + t1.NONOTICE as SICK, ");
        stb.append("     t1.LATE, ");
        stb.append("     t1.EARLY, ");
        stb.append("     CASE ");
        stb.append("     WHEN t1.UPDATED > t2.UPDATED and t1.UPDATED > t3.UPDATED and (t1.UPDATED > W3.UPDATED or W3.UPDATED IS NULL) ");
        stb.append("     THEN t1.UPDATED ");
        stb.append("     WHEN t2.UPDATED > t1.UPDATED and t2.UPDATED > t3.UPDATED and (t2.UPDATED > W3.UPDATED or W3.UPDATED IS NULL) ");
        stb.append("     THEN t2.UPDATED ");
        stb.append("     WHEN t3.UPDATED > t1.UPDATED and t3.UPDATED > t2.UPDATED and (t3.UPDATED > W3.UPDATED or W3.UPDATED IS NULL) ");
        stb.append("     THEN t3.UPDATED ");
        stb.append("     WHEN W3.UPDATED > t1.UPDATED and W3.UPDATED > t2.UPDATED and W3.UPDATED > t3.UPDATED and W3.UPDATED IS NOT NULL ");
        stb.append("     THEN W3.UPDATED ");
        stb.append("     ELSE t1.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     t1.REGISTERCD, ");
        stb.append("     sysdate() as COMPDATETIME ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         SCHREGNO, ");
        stb.append("         SUM(LESSON) as LESSON, ");
        stb.append("         SUM(MOURNING) as MOURNING, ");
        stb.append("         SUM(SUSPEND) as SUSPEND, ");
        stb.append("         SUM(ABSENT) as ABSENT, ");
        stb.append("         SUM(SICK) as SICK, ");
        stb.append("         SUM(NOTICE) as NOTICE, ");
        stb.append("         SUM(NONOTICE) as NONOTICE, ");
        stb.append("         SUM(LATE) AS LATE, ");
        stb.append("         SUM(EARLY) AS EARLY, ");
        stb.append("         MAX(REGISTERCD) as REGISTERCD, ");
        stb.append("         MAX(UPDATED) as UPDATED ");
        stb.append("     FROM ");
        stb.append("         ( ");
        stb.append("         SELECT ");
        stb.append("             YEAR, ");
        stb.append("             SEMESTER, ");
        stb.append("             SCHREGNO, ");
        stb.append("             CASE ");
        stb.append("             WHEN LESSON IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE LESSON ");
        stb.append("             END as LESSON, ");
        stb.append("             CASE ");
        stb.append("             WHEN MOURNING IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE MOURNING ");
        stb.append("             END as MOURNING, ");
        stb.append("             CASE ");
        stb.append("             WHEN SUSPEND IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE SUSPEND ");
        stb.append("             END as SUSPEND, ");
        stb.append("             CASE ");
        stb.append("             WHEN ABSENT IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE ABSENT ");
        stb.append("             END as ABSENT, ");
        stb.append("             CASE ");
        stb.append("             WHEN SICK IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE SICK ");
        stb.append("             END as SICK, ");
        stb.append("             CASE ");
        stb.append("             WHEN NOTICE IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE NOTICE ");
        stb.append("             END  as NOTICE, ");
        stb.append("             CASE ");
        stb.append("             WHEN NONOTICE IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE NONOTICE ");
        stb.append("             END as NONOTICE, ");
        stb.append("             CASE ");
        stb.append("             WHEN LATE IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE LATE ");
        stb.append("             END AS LATE, ");
        stb.append("             CASE ");
        stb.append("             WHEN EARLY IS NULL ");
        stb.append("             THEN 0 ");
        stb.append("             ELSE EARLY ");
        stb.append("             END AS EARLY, ");
        stb.append("             REGISTERCD, ");
        stb.append("             UPDATED ");
        stb.append("         FROM ");
        stb.append("             ATTEND_SEMES_DAT ");
        stb.append(whereSql3);
        stb.append("         ) ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         SCHREGNO ");
        stb.append("     ) t1  ");
        stb.append("     left join SCHREG_REGD_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER ");
        stb.append("     left join SCHREG_BASE_MST t3 on t1.SCHREGNO = t3.SCHREGNO ");
        stb.append("     LEFT JOIN SEMESTER_MST T4 on t1.year = t4.year and t1.semester = t4.semester ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO,YEAR,SEMESTER,MAX(UPDATED) AS UPDATED,COUNT(*) AS COUNT FROM ");
        stb.append("                     ( ");
        stb.append("                     SELECT ");
        stb.append("                         SCHREGNO,YEAR,SEMESTER,T1.UPDATED ");
        stb.append("                     FROM ");
        stb.append("                         SCHREG_TRANSFER_DAT T1 ");
        stb.append("                         LEFT JOIN SEMESTER_MST T2 on (T1.TRANSFER_SDATE BETWEEN T2.SDATE AND T2.EDATE or T1.TRANSFER_EDATE BETWEEN T2.SDATE AND T2.EDATE) and T2.SEMESTER != '9' ");
        stb.append("                     ) ");
        stb.append("                GROUP BY SCHREGNO,YEAR,SEMESTER ");
        stb.append("               ) as W3 on T1.SCHREGNO = W3.SCHREGNO and T1.YEAR = W3.YEAR and T1.SEMESTER = W3.SEMESTER ");
        stb.append(whereSql2);
        stb.append(" ORDER BY YEAR ");
        return stb.toString();
    }
} // AttendUpdate

// eof
