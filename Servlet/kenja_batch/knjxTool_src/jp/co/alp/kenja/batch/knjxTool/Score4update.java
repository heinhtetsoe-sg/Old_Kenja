// kanji=漢字
/*
 * $Id: Score4update.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * @version $Id: Score4update.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Score4update {
    /* pkg */static final Log log = LogFactory.getLog(Score4update.class);

    protected final Param _param;

    public Score4update(final Param param, final Database knj, final String title) throws SQLException {
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
            whereSql1 = " WHERE BUMONCD = '1' AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '4' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("J".equals(nameMst._name1)) {
            whereSql1 = " WHERE BUMONCD = '2' AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '4' ";
            whereSql2 = " AND T2.GRADE BETWEEN  '" + nameMst._name2 + "' AND '" + nameMst._name3 + "' AND T1.YEAR = '" + _param._year + "' ";
        }
        if ("H".equals(nameMst._name1)) {
            whereSql1 = " WHERE (BUMONCD = '3' OR BUMONCD = '4') AND NENDO = '" + _param._year + "' AND TEST_CATEGORYCD = '4' ";
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
        stb.append("     '4' as TEST_CATEGORYCD, ");
        stb.append("     '外部模擬テスト' as TESTCATEGORYNAME, ");
        stb.append("     NULL as SEMESTER, ");
        stb.append("     substr(T1.MOCKCD,2,4) || '000' || substr(T1.MOCKCD,6,4), ");
        stb.append("     T3.MOCKNAME1, ");
        stb.append("     NULL as CLASSCD, ");
        stb.append("     NULL as CLASSNAME, ");
        stb.append("     NULL as CLASSSORT, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T4.SUBCLASS_NAME, ");
        stb.append("     NULL as SUBCLASSSORT, ");
        stb.append("     NULL as GROUP_CHAIRCD, ");
        stb.append("     NULL as GROUP_CHAIRNAME, ");
        stb.append("     NULL as CHAIRCD, ");
        stb.append("     NULL as CHAIRNAME, ");
        stb.append("     0 as SCH_CHR_COUNT, ");
        stb.append("     NULL as PASS_SCORE, ");
        stb.append("     NULL as GROUP_AVG, ");
        stb.append("     NULL as GROUP_STDDEV, ");
        stb.append("     NULL as GROUP_RANK, ");
        stb.append("     NULL as AVG, ");
        stb.append("     T1.DEVIATION, ");
        stb.append("     T1.RANK, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '1' as ATTEND, ");
        stb.append("     0 as ABSENCE, ");
        stb.append("     T1.SCORE, ");
        stb.append("     NULL as TOTAL_SCORE, ");
        stb.append("     NULL as TOTAL_AVG, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T4.UPDATED ");
        stb.append("     THEN T1.UPDATED ");
        stb.append("     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T4.UPDATED ");
        stb.append("     THEN T3.UPDATED ");
        stb.append("     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > T3.UPDATED ");
        stb.append("     THEN T4.UPDATED ");
        stb.append("     ELSE T1.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     CASE ");
        stb.append("     WHEN T1.UPDATED > T3.UPDATED AND T1.UPDATED > T4.UPDATED ");
        stb.append("     THEN T1.REGISTERCD ");
        stb.append("     WHEN T3.UPDATED > T1.UPDATED AND T3.UPDATED > T4.UPDATED ");
        stb.append("     THEN T3.REGISTERCD ");
        stb.append("     WHEN T4.UPDATED > T1.UPDATED AND T4.UPDATED > T3.UPDATED ");
        stb.append("     THEN T4.REGISTERCD ");
        stb.append("     ELSE T1.REGISTERCD ");
        stb.append("     END AS REGISTERCD, ");
        stb.append("     SYSDATE() AS COMPDATETIME ");
        stb.append("      ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK_RANGE_DAT T1  ");
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
        stb.append("                 ) t2 on t1.SCHREGNO = t2.SCHREGNO and t1.year = t2.year ");
        stb.append("     LEFT JOIN MOCK_MST T3 on T1.MOCKCD = T3.MOCKCD ");
        stb.append("     LEFT JOIN MOCK_SUBCLASS_MST T4 on T1.MOCK_SUBCLASS_CD = T4.MOCK_SUBCLASS_CD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT W4 on T1.YEAR = W4.YEAR and T2.GRADE = W4.GRADE ");
        stb.append("     LEFT JOIN SCHOOL_MST W3 on int(T1.YEAR) - int(W4.grade_cd) + 1 = int(W3.YEAR) and W4.SCHOOL_KIND = W3.SCHOOL_KIND ");
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     T1.SCORE IS NOT NULL ");
        stb.append(whereSql2);

        return stb.toString();
    }
} // Score4update

// eof
