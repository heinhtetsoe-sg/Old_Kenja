// kanji=漢字
/*
 * $Id: 8d04b510b6672f943c074127766b3697b0217b5e $
 *
 * 作成日: 2008/05/07 15:36:56 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD.detail;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 通知表データ取得。
 * @author m-yama
 * @version $Id: 8d04b510b6672f943c074127766b3697b0217b5e $
 */
public class getReportCardInfoTottori {
    public static String getRecordRankSql(
            final String year,
            final String sSeme,
            final String eSeme,
            final String schregno,
            final String grade,
            final String sObjClass,
            final String eObjClass
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.SEQ ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        stb.append("     LEFT JOIN RECORD_MOCK_ORDER_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND L1.GRADE = '" + grade + "' ");
        stb.append("          AND L1.TEST_DIV = '1' ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.TESTKINDCD || T1.TESTITEMCD = L1.TESTKINDCD || L1.TESTITEMCD ");
        stb.append("          AND T1.SUBCLASSCD = '999999' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER BETWEEN '" + sSeme + "' AND '" + eSeme + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    public static String getRecordMockSql(
            final String year,
            final String sSeme,
            final String eSeme,
            final String schregno,
            final String grade,
            final String sObjClass,
            final String eObjClass
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.TESTKINDCD || T1.TESTITEMCD AS TEST_CD, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.GRADE_RANK, ");
        stb.append("     T1.GRADE_DEVIATION, ");
        stb.append("     T1.CLASS_RANK, ");
        stb.append("     T1.CLASS_DEVIATION, ");
        stb.append("     T1.COURSE_RANK, ");
        stb.append("     T1.COURSE_DEVIATION, ");
        stb.append("     L1.SEQ ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        stb.append("     LEFT JOIN RECORD_MOCK_ORDER_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND L1.GRADE = '" + grade + "' ");
        stb.append("          AND L1.TEST_DIV = '1' ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.TESTKINDCD || T1.TESTITEMCD = L1.TESTKINDCD || L1.TESTITEMCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER BETWEEN '" + sSeme + "' AND '" + eSeme + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND L1.SEQ IS NOT NULL ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.MOCKCD AS TEST_CD, ");
        stb.append("     '1' AS SEMESTER, ");
        stb.append("     T1.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.RANK AS GRADE_RANK, ");
        stb.append("     T1.DEVIATION AS GRADE_DEVIATION, ");
        stb.append("     T1.RANK AS CLASS_RANK, ");
        stb.append("     T1.DEVIATION AS CLASS_DEVIATION, ");
        stb.append("     T1.RANK AS COURSE_RANK, ");
        stb.append("     T1.DEVIATION AS COURSE_DEVIATION, ");
        stb.append("     L1.SEQ ");
        stb.append(" FROM ");
        stb.append("     MOCK_DAT T1 ");
        stb.append("     LEFT JOIN RECORD_MOCK_ORDER_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND L1.GRADE = '" + grade + "' ");
        stb.append("          AND L1.TEST_DIV = '2' ");
        stb.append("          AND T1.MOCKCD = L1.MOCKCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND L1.SEQ IS NOT NULL ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.MOCK_SUBCLASS_CD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    public static String getRecordMockRankSql(
            final String year,
            final String ctrlSeme,
            final String schregno,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String course
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH MOCK_RANK AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.MOCKCD AS TEST_CD, ");
        stb.append("     '1' AS SEMESTER, ");
        stb.append("     T1.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
        stb.append("     T1.MOCKDIV, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.AVG, ");
        stb.append("     T1.GRADE_RANK, ");
        stb.append("     T1.GRADE_DEVIATION, ");
        stb.append("     T1.CLASS_RANK, ");
        stb.append("     T1.CLASS_DEVIATION, ");
        stb.append("     T1.COURSE_RANK, ");
        stb.append("     T1.COURSE_DEVIATION, ");
        stb.append("     L1.SEQ ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK_DAT T1 ");
        stb.append("     LEFT JOIN RECORD_MOCK_ORDER_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND L1.GRADE = '" + grade + "' ");
        stb.append("          AND L1.TEST_DIV = '2' ");
        stb.append("          AND T1.MOCKCD = L1.MOCKCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.MOCK_SUBCLASS_CD = '999999' ");
        stb.append("     AND T1.MOCKDIV = '2' ");
        stb.append("     AND L1.SEQ IS NOT NULL ");
        stb.append(" ) , MOCK_RANK_CNT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.TEST_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.MOCKDIV, ");
        stb.append("     COUNT(T1.SCHREGNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("           AND T2.YEAR='" + year + "' ");
        stb.append("           AND T2.SEMESTER='" + ctrlSeme + "' ");
        if (avgDiv.equals("1")) {
            stb.append("           AND T2.GRADE='" + grade + "' ");
        } else if (avgDiv.equals("2")) {
            stb.append("     AND T2.GRADE = '" + grade + "' ");
            stb.append("     AND T2.HR_CLASS = '" + hrClass + "' ");
        } else {
            stb.append("     AND T2.GRADE = '" + grade + "' ");
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + course + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.TEST_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.MOCKDIV ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.TEST_CD, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.MOCKDIV, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.AVG, ");
        stb.append("     T1.GRADE_RANK, ");
        stb.append("     T1.GRADE_DEVIATION, ");
        stb.append("     T1.CLASS_RANK, ");
        stb.append("     T1.CLASS_DEVIATION, ");
        stb.append("     T1.COURSE_RANK, ");
        stb.append("     T1.COURSE_DEVIATION, ");
        stb.append("     T1.SEQ, ");
        stb.append("     L1.CNT ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK T1 ");
        stb.append("     LEFT JOIN MOCK_RANK_CNT L1 ON T1.TEST_CD = L1.TEST_CD ");
        stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append("          AND T1.MOCKDIV = L1.MOCKDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    /**
     * 席次データの取得
     * @param tableDiv 1:DAT, 2:V_DAT 
     * @return sql
     */
    public static String getRecordRankTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String testKindItem,
            final String tableDiv
    ) {
        final StringBuffer stb = new StringBuffer();
        final String tableName = "2".equals(tableDiv) ? "V_DAT" : "DAT";

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_" + tableName + " T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testKindItem + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    /**
     * 講座席次データの取得
     * @param tableDiv 1:DAT, 2:V_DAT 
     * @return sql
     */
    public static String getRecordRankChairTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String testKindItem,
            final String tableDiv
    ) {
        final StringBuffer stb = new StringBuffer();
        final String tableName = "2".equals(tableDiv) ? "V_DAT" : "DAT";

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_CHAIR_" + tableName + " T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testKindItem + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    public static String getRecordRankTotalSql(
            final String year,
            final String sSeme,
            final String eSeme,
            final String schregno
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER BETWEEN '" + sSeme + "' AND '" + eSeme + "' ");
        stb.append("     AND T1.SUBCLASSCD = '999999' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    public static String getRecordAverageSql(
            final String year,
            final String sSeme,
            final String eSeme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String course
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER BETWEEN '" + sSeme + "' AND '" + eSeme + "' ");
        stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
        if (avgDiv.equals("1")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
        } else if (avgDiv.equals("2")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        } else {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + course + "' ");
        }
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    /**
     * 平均データの取得
     * @param tableDiv 1:DAT, 2:V_DAT 
     * @return sql
     */
    public static String getRecordAverageTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String course,
            final String testKindItem,
            final String tableDiv
    ) {
        final StringBuffer stb = new StringBuffer();
        final String tableName = "2".equals(tableDiv) ? "V_DAT" : "DAT";

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_" + tableName + " T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testKindItem + "' ");
        stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
        if (avgDiv.equals("1")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
        } else if (avgDiv.equals("2")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        } else if (avgDiv.equals("4")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD = '" + course + "' ");
        } else {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + course + "' ");
        }
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    /**
     * 平均データの取得
     * @param tableDiv 1:DAT, 2:V_DAT 
     * @return sql
     */
    public static String getRecordAverageChairTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String course,
            final String testKindItem,
            final String tableDiv
    ) {
        final StringBuffer stb = new StringBuffer();
        final String tableName = "2".equals(tableDiv) ? "V_DAT" : "DAT";

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_CHAIR_" + tableName + " T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testKindItem + "' ");
        stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
        if (avgDiv.equals("1")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
        } else if (avgDiv.equals("2")) {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        } else {
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + course + "' ");
        }
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    public static String getRecordScoreSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String scoreDiv
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + seme + "' ");
        stb.append("     AND T1.SCORE_DIV <= '" + scoreDiv + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
        return stb.toString();
    }

    public static String getRecordScoreTestAppointSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String scoreDiv,
            final String testKindItem
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     L1.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + seme + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + testKindItem + "' ");
        stb.append("     AND T1.SCORE_DIV = '" + scoreDiv + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        return stb.toString();
    }

    public static String getGetCreditSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String yearDiv
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" SELECT ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0)) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append(" WHERE ");
        if (yearDiv.equals("TOTAL")) {
            stb.append("     T1.YEAR <= '" + year + "' ");
        } else {
            stb.append("     T1.YEAR = '" + year + "' ");
        }
        stb.append("     AND T1.SEMESTER = '" + seme + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
    
        return stb.toString();
    }

}
 // getReportCardInfoTottori

// eof
