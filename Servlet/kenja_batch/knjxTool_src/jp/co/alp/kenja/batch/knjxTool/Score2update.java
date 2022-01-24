// kanji=漢字
/*
 * $Id: Score2update.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * @version $Id: Score2update.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Score2update {
    /* pkg */static final Log log = LogFactory.getLog(Score2update.class);

    protected final Param _param;

    public Score2update(final Param param, final Database knj, final String title) throws SQLException {
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
            whereSql1 = " WHERE BUMONCD = '1' AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '2' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("J".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '2' AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '2' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("H".equals(nameMst._name1)) {
            whereSql1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '2' ";
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
        stb.append("     T1.YEAR as NENDO, ");
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
        stb.append("     '2' as TEST_CATEGORYCD, ");
        stb.append("     '実力テスト' as TESTCATEGORYNAME, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.YEAR || T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD as TESTID, ");
        stb.append("     T3.PROFICIENCYNAME1, ");
        stb.append("     T4.CLASSCD, ");
        stb.append("     T5.CLASSNAME, ");
        stb.append("     NULL as CLASSSORT, ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD, ");
        stb.append("     T4.SUBCLASS_NAME, ");
        stb.append("     NULL as SUBCLASSSORT, ");
        stb.append("     NULL as GROUP_CHAIRCD, ");
        stb.append("     NULL as GROUP_CHAIRNAME, ");
        stb.append("     NULL as CHAIRCD, ");
        stb.append("     NULL as CHAIRNAME, ");
        stb.append("     0 as SCH_CHR_COUNT, ");
        stb.append("     30 as PASS_SCORE, ");
        stb.append("     T6.AVG as GROUP_AVG, ");
        stb.append("     T6.STDDEV as GROUP_STDDEV, ");
        stb.append("     T7.RANK as GROUP_RANK, ");
        stb.append("     T8.AVG, ");
        stb.append("     T8.STDDEV, ");
        stb.append("     T9.RANK, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '1' as ATTEND, ");
        stb.append("     0 as ABSENCE, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T10.SCORE as TOTAL_SCORE, ");
        stb.append("     T10.AVG as TOTAL_AVG, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T6.UPDATED AND T1.UPDATED > T7.UPDATED ");
        stb.append("     THEN T1.UPDATED ");
        stb.append("     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T6.UPDATED AND T3.UPDATED > T7.UPDATED ");
        stb.append("     THEN T3.UPDATED ");
        stb.append("     WHEN T6.UPDATED > T1.UPDATED AND T6.UPDATED > T3.UPDATED AND T6.UPDATED > T7.UPDATED ");
        stb.append("     THEN T6.UPDATED ");
        stb.append("     WHEN T7.UPDATED > T1.UPDATED AND T7.UPDATED > T3.UPDATED AND T7.UPDATED > T6.UPDATED ");
        stb.append("     THEN T7.UPDATED ");
        stb.append("     ELSE T1.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T6.UPDATED AND T1.UPDATED > T7.UPDATED ");
        stb.append("     THEN T1.REGISTERCD ");
        stb.append("     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T6.UPDATED AND T3.UPDATED > T7.UPDATED ");
        stb.append("     THEN T3.REGISTERCD ");
        stb.append("     WHEN T6.UPDATED > T1.UPDATED AND T6.UPDATED > T3.UPDATED AND T6.UPDATED > T7.UPDATED ");
        stb.append("     THEN T6.REGISTERCD ");
        stb.append("     WHEN T7.UPDATED > T1.UPDATED AND T7.UPDATED > T3.UPDATED AND T7.UPDATED > T6.UPDATED ");
        stb.append("     THEN T7.REGISTERCD ");
        stb.append("     ELSE T1.REGISTERCD ");
        stb.append("     END AS REGISTERCD, ");
        stb.append("     SYSDATE() AS COMPDATETIME ");
        stb.append("      ");
        stb.append(" FROM ");
        stb.append("     PROFICIENCY_DAT T1  ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 on T1.YEAR = T2.YEAR and T1.SEMESTER = T2.SEMESTER and T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN PROFICIENCY_MST T3 on T1.PROFICIENCYDIV = T3.PROFICIENCYDIV and T1.PROFICIENCYCD = T3.PROFICIENCYCD ");
        stb.append("     LEFT JOIN PROFICIENCY_SUBCLASS_MST T4 on T1.PROFICIENCY_SUBCLASS_CD = T4.PROFICIENCY_SUBCLASS_CD ");
        stb.append("     LEFT JOIN CLASS_MST T5 on T4.SCHOOL_KIND = T5.SCHOOL_KIND and T4.CLASSCD = T5.CLASSCD ");
        stb.append("     LEFT JOIN PROFICIENCY_AVERAGE_DAT T6 on T1.YEAR = T6.YEAR and T1.SEMESTER = T6.SEMESTER and T1.PROFICIENCYDIV = T6.PROFICIENCYDIV and T1.PROFICIENCYCD = T6.PROFICIENCYCD and T1.PROFICIENCY_SUBCLASS_CD = T6.PROFICIENCY_SUBCLASS_CD and T6.DATA_DIV = '1' and T6.AVG_DIV = '01' and T2.GRADE = T6.GRADE ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT T7 on T1.YEAR = T7.YEAR and T1.SEMESTER = T7.SEMESTER and T1.PROFICIENCYDIV = T7.PROFICIENCYDIV and T1.PROFICIENCYCD = T7.PROFICIENCYCD and T1.SCHREGNO = T7.SCHREGNO and T1.PROFICIENCY_SUBCLASS_CD = T7.PROFICIENCY_SUBCLASS_CD and T7.RANK_DATA_DIV = '01' and T7.RANK_DIV = '01' ");
        stb.append("     LEFT JOIN PROFICIENCY_AVERAGE_DAT T8 on T1.YEAR = T8.YEAR and T1.SEMESTER = T8.SEMESTER and T1.PROFICIENCYDIV = T8.PROFICIENCYDIV and T1.PROFICIENCYCD = T8.PROFICIENCYCD and T1.PROFICIENCY_SUBCLASS_CD = T8.PROFICIENCY_SUBCLASS_CD and T8.DATA_DIV = '1' and T8.AVG_DIV = '03' and T2.GRADE = T8.GRADE and T2.COURSECD = T8.COURSECD and T2.MAJORCD = T8.MAJORCD and T2.COURSECODE = T8.COURSECODE ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT T9 on T1.YEAR = T9.YEAR and T1.SEMESTER = T9.SEMESTER and T1.PROFICIENCYDIV = T9.PROFICIENCYDIV and T1.PROFICIENCYCD = T9.PROFICIENCYCD and T1.SCHREGNO = T9.SCHREGNO and T1.PROFICIENCY_SUBCLASS_CD = T9.PROFICIENCY_SUBCLASS_CD and T9.RANK_DATA_DIV = '01' and T9.RANK_DIV = '03' ");
        stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT T10 on T1.YEAR = T10.YEAR and T1.SEMESTER = T10.SEMESTER and T1.PROFICIENCYDIV = T10.PROFICIENCYDIV and T1.PROFICIENCYCD = T10.PROFICIENCYCD and T1.SCHREGNO = T10.SCHREGNO and T10.PROFICIENCY_SUBCLASS_CD = '999999' and T10.RANK_DATA_DIV = '01' and T10.RANK_DIV = '01' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT W4 on T1.YEAR = W4.YEAR and T1.GRADE = W4.GRADE ");
        stb.append("     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and W4.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     T1.SCORE IS NOT NULL ");
        stb.append(whereSql2);

        return stb.toString();
    }
} // Score2update

// eof
