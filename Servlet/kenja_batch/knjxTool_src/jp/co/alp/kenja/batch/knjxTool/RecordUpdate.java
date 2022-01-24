// kanji=漢字
/*
 * $Id: RecordUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * @version $Id: RecordUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class RecordUpdate {
    /* pkg */static final Log log = LogFactory.getLog(RecordUpdate.class);

    protected final Param _param;

    public RecordUpdate(final Param param, final Database knj, final String title) throws SQLException {
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
        if ("P".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '1' AND NENDO = '" + _param._year + "' ";
            whereSql2 = " AND W1.GRADE BETWEEN  '01' AND '06' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("J".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '2' AND NENDO = '" + _param._year + "' ";
            whereSql2 = " AND W1.GRADE BETWEEN  '07' AND '09' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("H".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '3' OR BUMONCD = '4' AND NENDO = '" + _param._year + "' ";
            whereSql2 = " AND W1.GRADE BETWEEN  '10' AND '12' AND T1.YEAR = '" + _param._year + "' ";
        }
        final String deleteSql = getDeleteSql(whereSql1);
        knj.executeUpdate(deleteSql);
        final String insertSql = getInsertSql(whereSql2);
        int cnt = knj.executeUpdate(insertSql);
        count += cnt;
        log.debug("データ数=" + count);
    }

    private String getDeleteSql(final String whereSql1) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM ");
        stb.append("     TOOL_RECORD_DAT ");
        stb.append(whereSql1);

        return stb.toString();
    }

    private String getInsertSql(final String whereSql2) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO TOOL_RECORD_DAT  ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     CASE ");
        stb.append("     WHEN W1.HR_CLASS = '011' ");
        stb.append("     THEN '4' ");
        stb.append("     WHEN W1.GRADE BETWEEN  '01' AND '06' ");
        stb.append("     THEN '1' ");
        stb.append("     WHEN W1.GRADE BETWEEN  '07' AND '09' ");
        stb.append("     THEN '2' ");
        stb.append("     WHEN W1.GRADE BETWEEN  '10' AND '12' ");
        stb.append("     THEN '3' ");
        stb.append("     END as BUMONCD, ");
        stb.append("     ltrim(W1.GRADE,'0') AS GRADE, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T2.CLASSNAME, ");
        stb.append("     T2.SHOWORDER, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T3.SUBCLASSNAME, ");
        stb.append("     T3.SHOWORDER, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.SCORE, ");
        stb.append("     CASE ");
        stb.append("     WHEN T4.PROV_FLG IS NULL ");
        stb.append("     THEN 0 ");
        stb.append("     ELSE T4.PROV_FLG ");
        stb.append("     END AS PROV_FLG, ");
        stb.append("     W2.SICK, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T4.UPDATED AND T1.UPDATED > W2.UPDATED ");
        stb.append("     THEN T1.UPDATED ");
        stb.append("     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > W2.UPDATED ");
        stb.append("     THEN T4.UPDATED ");
        stb.append("     WHEN W2.UPDATED > T1.UPDATED AND W2.UPDATED > T4.UPDATED ");
        stb.append("     THEN W2.UPDATED ");
        stb.append("     ELSE T1.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     T1.REGISTERCD, ");
        stb.append("     SYSDATE() AS COMPDATETIME ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     LEFT JOIN (SELECT DISTINCT YEAR,SCHREGNO,GRADE,HR_CLASS,ATTENDNO FROM SCHREG_REGD_DAT) W1 on T1.YEAR = W1.YEAR and T1.SCHREGNO = W1.SCHREGNO ");
        stb.append("     LEFT JOIN CLASS_MST T2 on T1.CLASSCD = T2.CLASSCD and T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SUBCLASS_MST T3 on T1.SUBCLASSCD = T3.SUBCLASSCD and T1.SCHOOL_KIND = T3.SCHOOL_KIND and T1.CLASSCD = T3.CLASSCD and T1.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT T4 on T1.YEAR = T4.YEAR and T1.CLASSCD = T4.CLASSCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND and T1.CURRICULUM_CD = T4.CURRICULUM_CD and T1.SUBCLASSCD = T4.SUBCLASSCD and T1.SCHREGNO = T4.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SUM(SICK) AS SICK,MAX(UPDATED) AS UPDATED FROM (SELECT YEAR,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,(SICK + NOTICE + NONOTICE) AS SICK,UPDATED FROM (SELECT YEAR,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CASE WHEN SICK IS NULL THEN 0 ELSE SICK END AS SICK,CASE WHEN NOTICE IS NULL THEN 0 ELSE NOTICE END AS NOTICE,CASE WHEN NONOTICE IS NULL THEN 0 ELSE NONOTICE END AS NONOTICE,UPDATED FROM ATTEND_SUBCLASS_DAT)) GROUP BY YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD) W2 on T1.YEAR = W2.YEAR and T1.SCHREGNO = W2.SCHREGNO and T1.CLASSCD = W2.CLASSCD and T1.SCHOOL_KIND = W2.SCHOOL_KIND and T1.CURRICULUM_CD = W2.CURRICULUM_CD and T1.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append("  ");
        stb.append("                  ");
        stb.append(" WHERE ");
        stb.append("     T1.SEMESTER = '9' and T1.TESTKINDCD = '99' and T1.TESTITEMCD = '00' and SCORE_DIV = '09' ");
        stb.append(whereSql2);

        return stb.toString();
    }
} // RecordUpdate

// eof
