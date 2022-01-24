// kanji=漢字
/*
 * $Id: Score1update.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * 学年情報テーブル。
 *
 * @author m-yamashiro
 * @version $Id: Score1update.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Score1update {
    /* pkg */static final Log log = LogFactory.getLog(Score1update.class);

    protected final Param _param;

    public Score1update(final Param param, final Database knj, final String title) throws SQLException {
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
            whereSql1 = " WHERE BUMONCD = '1' AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '1' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("J".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '2' AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '1' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("H".equals(nameMst._name1)) {
            whereSql1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '1' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
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
        stb.append("     TOOL_SCORE_DAT ");
        stb.append(whereSql1);

        return stb.toString();
    }

    private String getInsertSql(final String whereSql2) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO TOOL_SCORE_DAT  ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     CASE  ");
        stb.append("     WHEN T2.HR_CLASS = '011' ");
        stb.append("     THEN '4' ");
        stb.append("     WHEN T2.GRADE BETWEEN '01' and '06' ");
        stb.append("     THEN '1' ");
        stb.append("     WHEN T2.GRADE BETWEEN '07' and '09' ");
        stb.append("     THEN '2' ");
        stb.append("     WHEN T2.GRADE BETWEEN '10' and '12' ");
        stb.append("     THEN '3' ");
        stb.append("     END AS BUMONCD, ");
        stb.append("     CASE  ");
        stb.append("     WHEN T2.HR_CLASS = '011' ");
        stb.append("     THEN '美デ' ");
        stb.append("     WHEN T2.GRADE BETWEEN '01' and '06' ");
        stb.append("     THEN '駿小' ");
        stb.append("     WHEN T2.GRADE BETWEEN '07' and '09' ");
        stb.append("     THEN '駿中' ");
        stb.append("     WHEN T2.GRADE BETWEEN '10' and '12' ");
        stb.append("     THEN '駿高' ");
        stb.append("     END AS BUMONNAME, ");
        stb.append("     W3.PRESENT_EST, ");
        stb.append("     ltrim(T2.GRADE,'0') AS GRADE, ");
        stb.append("     '1' as TEST_CATEGORYCD, ");
        stb.append("     '定期考査' as TESTCATEGORYNAME, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.YEAR || T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV as TESTID, ");
        stb.append("     T3.TESTITEMNAME, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T4.CLASSNAME, ");
        stb.append("     T4.SHOWORDER, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T5.SUBCLASSNAME, ");
        stb.append("     T5.SHOWORDER, ");
        stb.append("     T72.CHAIR_GROUP_CD, ");
        stb.append("     T72.CHAIR_GROUP_NAME, ");
        stb.append("     T7.CHAIRCD, ");
        stb.append("     T7.CHAIRNAME, ");
        stb.append("     W1.LESSON AS SCH_CHR_COUNT, ");
        stb.append("     CASE ");
        stb.append("     WHEN T8.PASS_SCORE IS NULL ");
        stb.append("     THEN 30 ");
        stb.append("     ELSE T8.PASS_SCORE ");
        stb.append("     END AS PASS_SCORE, ");
        stb.append("     T9.AVG AS GROUP_AVG, ");
        stb.append("     T9.STDDEV AS GROUP_STDDEV, ");
        stb.append("     T11.COURSE_RANK AS GROUP_RANK, ");
        stb.append("     T10.AVG AS AVG, ");
        stb.append("     T10.STDDEV AS STDDEV, ");
        stb.append("     T11.CLASS_RANK AS RANK, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     CASE  ");
        stb.append("     WHEN T1.VALUE_DI IS NOT NULL ");
        stb.append("     THEN '0' ");
        stb.append("     ELSE '1' ");
        stb.append("     END AS ATTEND, ");
        stb.append("     CASE  ");
        stb.append("     WHEN W2.ABSENCE IS NULL ");
        stb.append("     THEN 0 ");
        stb.append("     ELSE W2.ABSENCE ");
        stb.append("     END AS ABSENCE, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.SCORE IS NULL ");
        stb.append("     THEN 0 ");
        stb.append("     ELSE T1.SCORE ");
        stb.append("     END AS SCORE, ");
        stb.append("     CASE  ");
        stb.append("     WHEN T12.SCORE IS NOT NULL ");
        stb.append("     THEN T12.SCORE ");
        stb.append("     ELSE 0 ");
        stb.append("     END AS TOTAL_SCORE, ");
        stb.append("     CASE  ");
        stb.append("     WHEN T12.AVG IS NOT NULL ");
        stb.append("     THEN T12.AVG ");
        stb.append("     ELSE 0 ");
        stb.append("     END AS TOTAL_AVG, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T9.UPDATED AND T1.UPDATED > T10.UPDATED AND T1.UPDATED > T11.UPDATED ");
        stb.append("     THEN T1.UPDATED ");
        stb.append("     WHEN T9.UPDATED > T1.UPDATED AND T9.UPDATED > T10.UPDATED AND T9.UPDATED > T11.UPDATED ");
        stb.append("     THEN T9.UPDATED ");
        stb.append("     WHEN T10.UPDATED > T1.UPDATED AND T10.UPDATED > T9.UPDATED AND T10.UPDATED > T11.UPDATED ");
        stb.append("     THEN T10.UPDATED ");
        stb.append("     WHEN T11.UPDATED > T1.UPDATED AND T11.UPDATED > T9.UPDATED AND T11.UPDATED > T10.UPDATED ");
        stb.append("     THEN T11.UPDATED ");
        stb.append("     ELSE T1.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T9.UPDATED AND T1.UPDATED > T10.UPDATED AND T1.UPDATED > T11.UPDATED ");
        stb.append("     THEN T1.REGISTERCD ");
        stb.append("     WHEN T9.UPDATED > T1.UPDATED AND T9.UPDATED > T10.UPDATED AND T9.UPDATED > T11.UPDATED ");
        stb.append("     THEN T9.REGISTERCD ");
        stb.append("     WHEN T10.UPDATED > T1.UPDATED AND T10.UPDATED > T9.UPDATED AND T10.UPDATED > T11.UPDATED ");
        stb.append("     THEN T10.REGISTERCD ");
        stb.append("     WHEN T11.UPDATED > T1.UPDATED AND T11.UPDATED > T9.UPDATED AND T11.UPDATED > T10.UPDATED ");
        stb.append("     THEN T11.REGISTERCD ");
        stb.append("     ELSE T1.REGISTERCD ");
        stb.append("     END AS REGISTERCD, ");
        stb.append("     SYSDATE() AS COMPDATETIME ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T3 on T1.YEAR = T3.YEAR and T1.SEMESTER = T3.SEMESTER and T1.TESTKINDCD = T3.TESTKINDCD and T1.TESTITEMCD = T3.TESTITEMCD and T1.SCORE_DIV = T3.SCORE_DIV and T1.SCHOOL_KIND = T3.SCHOOL_KIND and T2.MAJORCD = T3.MAJORCD ");
        stb.append("     LEFT JOIN CLASS_MST T4 on T1.CLASSCD = T4.CLASSCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SUBCLASS_MST T5 on T1.SCHOOL_KIND = T5.SCHOOL_KIND and T1.SUBCLASSCD = T5.SUBCLASSCD and T1.CURRICULUM_CD = T5.CURRICULUM_CD ");
        stb.append("     LEFT JOIN CHAIR_STD_DAT T6 on T1.YEAR = T6.YEAR and T1.SEMESTER = T6.SEMESTER and T1.SCHREGNO = T6.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT T7 on T1.YEAR = T7.YEAR and T1.SEMESTER = T7.SEMESTER and T1.SCHOOL_KIND = T7.SCHOOL_KIND and T1.CURRICULUM_CD = T7.CURRICULUM_CD and T1.SUBCLASSCD = T7.SUBCLASSCD ");
        stb.append("     LEFT JOIN CHAIR_GROUP_DAT T71 on T7.YEAR = T71.YEAR and T7.SEMESTER = T71.SEMESTER and T7.CHAIRCD = T71.CHAIRCD ");
        stb.append("     LEFT JOIN CHAIR_GROUP_MST T72 on T71.YEAR = T72.YEAR and T71.SEMESTER = T72.SEMESTER and T71.CHAIR_GROUP_CD = T72.CHAIR_GROUP_CD and T1.SCHOOL_KIND = T72.SCHOOL_KIND and T1.CURRICULUM_CD = T72.CURRICULUM_CD ");
        stb.append("     LEFT JOIN ");
        stb.append("     (SELECT YEAR,SEMESTER,'02' as TESTKINDCD,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,sum(LESSON) as LESSON FROM ( ");
        stb.append("     SELECT YEAR,SEMESTER,CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,case LESSON when null then 0 else LESSON end as LESSON FROM ATTEND_SUBCLASS_DAT) group by year,semester,schregno,classcd,school_kind,curriculum_cd,subclasscd ");
        stb.append("     union ");
        stb.append("     SELECT YEAR,SEMESTER,CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,case LESSON when null then 0 else LESSON end as LESSON FROM ATTEND_SUBCLASS_DAT where month in('05','10') ");
        stb.append("     ) W1 ON T1.YEAR = W1.YEAR and T1.SEMESTER = W1.SEMESTER and T1.TESTKINDCD = W1.TESTKINDCD and T1.SCHREGNO = W1.SCHREGNO and T1.SCHOOL_KIND = W1.SCHOOL_KIND and T1.CURRICULUM_CD = W1.CURRICULUM_CD and T1.SUBCLASSCD = W1.SUBCLASSCD ");
        stb.append("     LEFT JOIN PERFECT_RECORD_DAT T8 on T1.YEAR = T8.YEAR and T1.SEMESTER = T8.SEMESTER and T1.TESTKINDCD = T8.TESTKINDCD and T1.TESTITEMCD = T8.TESTITEMCD and T1.SCHOOL_KIND = T8.SCHOOL_KIND and T1.CURRICULUM_CD = T8.CURRICULUM_CD and T1.SUBCLASSCD = T8.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT T9 on T1.YEAR = T9.YEAR and T1.SEMESTER = T9.SEMESTER and T1.TESTKINDCD = T9.TESTKINDCD and T1.TESTITEMCD = T9.TESTITEMCD and T1.SCHOOL_KIND = T9.SCHOOL_KIND and T1.CURRICULUM_CD = T9.CURRICULUM_CD and T1.SUBCLASSCD = T9.SUBCLASSCD and T2.COURSECODE = T9.COURSECODE and T9.AVG_DIV = '3' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT T10 on T1.YEAR = T10.YEAR and T1.SEMESTER = T10.SEMESTER and T1.TESTKINDCD = T10.TESTKINDCD and T1.TESTITEMCD = T10.TESTITEMCD and T1.SCHOOL_KIND = T10.SCHOOL_KIND and T1.CURRICULUM_CD = T10.CURRICULUM_CD and T1.SUBCLASSCD = T10.SUBCLASSCD and T2.GRADE = T10.GRADE and T2.HR_CLASS = T10.HR_CLASS and T10.AVG_DIV = '2' ");
        stb.append("     LEFT JOIN  ");
        stb.append("     (SELECT YEAR,SEMESTER,TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CASE WHEN SEMESTER = '3' THEN ABSENCE WHEN TESTKINDCD = '02' THEN ABSENCE + ABSENCE2 ELSE ABSENCE END AS ABSENCE FROM  ");
        stb.append("     ( ");
        stb.append("     SELECT T1.YEAR,T1.SEMESTER,T1.TESTKINDCD,T1.SCHREGNO,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,CASE WHEN T1.ABSENCE IS NULL THEN 0 ELSE T1.ABSENCE END as ABSENCE,CASE WHEN T2.ABSENCE IS NULL THEN 0 ELSE T2.ABSENCE END as ABSENCE2 FROM  ");
        stb.append("     ( ");
        stb.append("     SELECT YEAR,SEMESTER,CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SICK AS ABSENCE FROM ATTEND_SUBCLASS_DAT) as T1 ");
        stb.append("     LEFT JOIN  ");
        stb.append("     ( ");
        stb.append("     SELECT YEAR,SEMESTER,    CASE MONTH WHEN '05' THEN '01' WHEN '07' THEN '02' WHEN '10' THEN '01' WHEN '12' THEN '02' WHEN '03' THEN '02' ELSE NULL END AS TESTKINDCD,SCHREGNO,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SICK AS ABSENCE FROM ATTEND_SUBCLASS_DAT) as T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO and T1.SCHOOL_KIND = T2.SCHOOL_KIND and T1.CURRICULUM_CD = T2.CURRICULUM_CD and T1.SUBCLASSCD = T2.SUBCLASSCD and T2.TESTKINDCD = '01' ");
        stb.append("     ) ");
        stb.append("     ) W2 ");
        stb.append("     on T1.YEAR = W2.YEAR and T1.SEMESTER = W2.SEMESTER and T1.TESTKINDCD = W2.TESTKINDCD and T1.SCHREGNO = W2.SCHREGNO and T1.SCHOOL_KIND = W2.SCHOOL_KIND and T1.CURRICULUM_CD = W2.CURRICULUM_CD and T1.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T11 on T1.YEAR = T11.YEAR and T1.SEMESTER = T11.SEMESTER and T1.TESTKINDCD = T11.TESTKINDCD and T1.TESTITEMCD = T11.TESTITEMCD and T1.SCORE_DIV = T11.SCORE_DIV and T1.SCHOOL_KIND = T11.SCHOOL_KIND and T1.CURRICULUM_CD = T11.CURRICULUM_CD and T1.SUBCLASSCD = T11.SUBCLASSCD and T1.SCHREGNO = T11.SCHREGNO  ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T12 on T1.YEAR = T12.YEAR and T1.SEMESTER = T12.SEMESTER and T1.TESTKINDCD = T12.TESTKINDCD and T1.TESTITEMCD = T12.TESTITEMCD and T1.SCORE_DIV = T12.SCORE_DIV and T1.SCHOOL_KIND = T12.SCHOOL_KIND and T12.CURRICULUM_CD = '99' and T1.SCHREGNO = T12.SCHREGNO and T12.SUBCLASSCD = '999999' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT W4 on T2.YEAR = W4.YEAR and T2.GRADE = W4.GRADE ");
        stb.append("     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and T1.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.SEMESTER != '9' AND ");
        stb.append("     T6.CHAIRCD = T7.CHAIRCD AND ");
        stb.append("     T6.CHAIRCD = T9.CHAIRCD AND ");
        stb.append("     T6.CHAIRCD = T10.CHAIRCD ");
        stb.append(whereSql2);

        return stb.toString();
    }
} // Score1update

// eof
