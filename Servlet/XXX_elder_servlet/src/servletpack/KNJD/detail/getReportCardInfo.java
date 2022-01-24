// kanji=漢字
/*
 * $Id: 309616296b5e1adbb5ebee58fe51e88425fa80f5 $
 *
 * 作成日: 2008/05/07 15:36:56 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD.detail;

import jp.co.alp.kenja.common.dao.SQLUtils;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * 通知表データ取得。
 * @author m-yama
 * @version $Id: 309616296b5e1adbb5ebee58fe51e88425fa80f5 $
 */
public class getReportCardInfo {
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

    public static String getRecordRankVSql(
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
        stb.append("     RECORD_RANK_V_DAT T1 ");
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
            final String eObjClass,
            final boolean hasMock
    ) {
        return getRecordMockSql(year, sSeme, eSeme, schregno, grade, sObjClass, eObjClass, hasMock, null);
    }

    public static String getRecordMockSql(
            final String year,
            final String sSeme,
            final String eSeme,
            final String schregno,
            final String grade,
            final String sObjClass,
            final String eObjClass,
            final boolean hasMock,
            final String useCurriculumcd
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.TESTKINDCD || T1.TESTITEMCD AS TEST_CD, ");
        stb.append("     T1.SEMESTER, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
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
        if (hasMock) {
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.MOCKCD AS TEST_CD, ");
            stb.append("     '1' AS SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     L2.CLASSCD, ");
                stb.append("     L2.SCHOOL_KIND, ");
                stb.append("     L2.CURRICULUM_CD, ");
            }
            stb.append("     L2.SUBCLASSCD, ");
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
            stb.append("     LEFT JOIN MOCK_SUBCLASS_MST L2 ON L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND L1.SEQ IS NOT NULL ");
            if (null != sObjClass && null != eObjClass) {
                stb.append("     AND (SUBSTR(T1.MOCK_SUBCLASS_CD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
            }
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
            final String course,
            final String subClasscd,
            final String useCurriculumcd
    ) {
        final String setSubClassCd = null != subClasscd ? subClasscd : "999999";
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
        stb.append("     AND T1.MOCK_SUBCLASS_CD = '" + setSubClassCd + "' ");
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
        if ("1".equals(useCurriculumcd)) {
            stb.append("     TT1.CLASSCD, ");
            stb.append("     TT1.SCHOOL_KIND, ");
            stb.append("     TT1.CURRICULUM_CD, ");
        }
        stb.append("     TT1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSCD AS MOCK_SUBCLASS_CD, ");
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
        stb.append("     L1.CNT, ");
        stb.append("     L2.COUNT AS CLASS_COUNT ");
        stb.append(" FROM ");
        stb.append("     MOCK_RANK T1 ");
        stb.append("     LEFT JOIN MOCK_SUBCLASS_MST TT1 ON TT1.MOCK_SUBCLASS_CD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN MOCK_RANK_CNT L1 ON T1.TEST_CD = L1.TEST_CD ");
        stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append("          AND T1.MOCKDIV = L1.MOCKDIV ");
        stb.append("     LEFT JOIN MOCK_AVERAGE_DAT L2 ON L2.YEAR = '" + year + "' ");
        stb.append("          AND L2.MOCKCD = T1.TEST_CD ");
        stb.append("          AND L2.MOCK_SUBCLASS_CD = T1.SUBCLASSCD ");
        stb.append("          AND L2.AVG_DIV = '2' AND L2.GRADE = '" + grade + "' AND L2.HR_CLASS = '" + hrClass + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    public static String getRecordMockAverageSql(
            final String year,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String course,
            final String useCurriculumcd
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     '1' AS SEMESTER, ");
        stb.append("     T1.MOCKCD AS TEST_CD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
        }
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T1.AVG_KANSAN ");
        stb.append(" FROM ");
        stb.append("     MOCK_AVERAGE_DAT T1 ");
        stb.append("     LEFT JOIN MOCK_SUBCLASS_MST T2 ON T2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
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
            stb.append("     AND (SUBSTR(T1.MOCK_SUBCLASS_CD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    public static String getRecordRankTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String testKindItem
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T1 ");
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

    public static String getRecordRankVTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String testKindItem
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_V_DAT T1 ");
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
    
    public static String getRecordRankVTotalSql(
            final String year,
            final String sSeme,
            final String eSeme,
            final String schregno
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_V_DAT T1 ");
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
            final String testKindItem
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_DAT T1 ");
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

    public static String getRecordAverageVTestAppointSql(
            final String year,
            final String semester,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String course,
            final String testKindItem
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_AVERAGE_V_DAT T1 ");
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
        return getRecordScoreSql(year, seme, schregno, sObjClass, eObjClass, scoreDiv, null);
    }
    
    public static String getRecordScoreSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String scoreDiv,
            final String useCurriculumcd
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

    public static String getPerfectRecordSql(
            final String year,
            final String ctrlSemester,
            final String schregno
    ) {
        return getPerfectRecordSql(year, ctrlSemester, schregno, null);
    }
    public static String getPerfectRecordSql(
            final String year,
            final String ctrlSemester,
            final String schregno,
            final String useCurriculumcd
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" WITH PERFECT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
        stb.append("     CLASSCD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD, ");
        stb.append("     MIN(DIV) AS DIV ");
        stb.append(" FROM ");
        stb.append("     PERFECT_RECORD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     TESTKINDCD || TESTITEMCD, ");
        stb.append("     CLASSCD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
        }
        stb.append("     SUBCLASSCD ");
        stb.append(" ), PERFECT_MAIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     PERFECT_RECORD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             PERFECT_T E1 ");
        stb.append("         WHERE ");
        stb.append("             E1.YEAR = T1.YEAR ");
        stb.append("             AND E1.SEMESTER = T1.SEMESTER ");
        stb.append("             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ");
        stb.append("             AND E1.CLASSCD = T1.CLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("             AND E1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND E1.DIV = T1.DIV ");
        stb.append(" )) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.TESTKINDCD, ");
        stb.append("     T1.TESTITEMCD, ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.PASS_SCORE ");
        stb.append(" FROM PERFECT_MAIN T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("       AND L1.SEMESTER = CASE WHEN T1.SEMESTER = '9' THEN '" + ctrlSemester + "' ELSE T1.SEMESTER END");
        stb.append("       AND L1.SCHREGNO = '" + schregno + "' ");
        stb.append(" WHERE T1.YEAR = '" + year + "' ");
        stb.append("       AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE L1.GRADE END ");
        stb.append("       AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') THEN '00000000' ELSE L1.COURSECD || L1.MAJORCD || L1.COURSECODE END ");
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
        return getRecordScoreTestAppointSql(year, seme, schregno, sObjClass, eObjClass, scoreDiv, testKindItem, null);
    }

    public static String getRecordScoreTestAppointSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String scoreDiv,
            final String testKindItem,
            final String useCurriculumcd
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     L1.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
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
            final boolean isNoPrintMoto,
            final String[] notSelectSubclass,
            final String yearDiv
    ) {
        return getGetCreditSql(year, seme, schregno, sObjClass, eObjClass, isNoPrintMoto, notSelectSubclass, yearDiv, null, null);
    }

    public static String getGetCreditSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final boolean isNoPrintMoto,
            final String[] notSelectSubclass,
            final String yearDiv,
            final String useCurriculumcd,
            final String useClassDetailDat
    ) {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" WITH SUBCLASS_CREDIT AS ( ");
        stb.append("   SELECT ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("       T1.SUBCLASSCD, VALUE(T1.GET_CREDIT, 0) AS GET_CREDIT, MAX(COMBINED_SUBCLASSCD) AS MAX_COMBINED_SUBCLASSCD ");
        stb.append("   FROM ");
        stb.append("       RECORD_SCORE_DAT T1 ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = '" + year + "' ");
        stb.append("       AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("     AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("     AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("   WHERE ");
        if (yearDiv.equals("TOTAL")) {
            stb.append("       T1.YEAR <= '" + year + "' ");
        } else {
            stb.append("       T1.YEAR = '" + year + "' ");
        }
        stb.append("       AND T1.SEMESTER = '" + seme + "' ");
        stb.append("       AND T1.SCHREGNO = '" + schregno + "' ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("       AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
        if (isNoPrintMoto) {
            stb.append("       AND NOT EXISTS (SELECT ");
            stb.append("                     'X' ");
            stb.append("                 FROM ");
            stb.append("                     SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                 WHERE ");
            stb.append("                     T2.YEAR = '" + year + "' AND ");
            stb.append("                     T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("                 AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("                 AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                 AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("                 ) ");
        }
        if (null != notSelectSubclass && 0 != notSelectSubclass.length) {
            if ("1".equals(useClassDetailDat)) {
                stb.append("       AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN " + SQLUtils.whereIn(true, notSelectSubclass));
            } else {
                stb.append("       AND T1.SUBCLASSCD NOT IN " + SQLUtils.whereIn(true, notSelectSubclass));
            }
        }
        stb.append("   GROUP BY ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("       T1.SUBCLASSCD, VALUE(T1.GET_CREDIT, 0) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_CREDIT T1 ");
        stb.append(" WHERE ");
        stb.append("     (MAX_COMBINED_SUBCLASSCD IS NULL OR ");
        stb.append("      MAX_COMBINED_SUBCLASSCD NOT IN (SELECT SUBCLASSCD FROM SUBCLASS_CREDIT)) ");
    
        return stb.toString();
    }

    public static String getGetCreditNotContainAttendSubclassSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String[] notSelectSubclass,
            final String yearDiv
    ) {
        return getGetCreditNotContainAttendSubclassSql(year, seme, schregno, sObjClass, eObjClass, notSelectSubclass, yearDiv, null, null);
    }

    public static String getGetCreditNotContainAttendSubclassSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String[] notSelectSubclass,
            final String yearDiv,
            final String useCurriculumcd,
            final String useClassDetailDat
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
        stb.append("     AND NOT EXISTS (SELECT 'X' ");
        stb.append("                 FROM SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                 WHERE YEAR = T1.YEAR ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("         AND ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("         AND ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("                   AND ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T1.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
        if (null != notSelectSubclass && 0 != notSelectSubclass.length) {
            if ("1".equals(useClassDetailDat)) {
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || SUBCLASSCD NOT IN " + SQLUtils.whereIn(true, notSelectSubclass));
            } else {
                stb.append("     AND T1.SUBCLASSCD NOT IN " + SQLUtils.whereIn(true, notSelectSubclass));
            }
        }
    
        return stb.toString();
    }

    public static String getAssessLevelMstSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String assessDiv,
            final String testKindItem
    ) {
        return getAssessLevelMstSql(year, seme, schregno, sObjClass, eObjClass, assessDiv, testKindItem, null);
    }

    public static String getAssessLevelMstSql(
            final String year,
            final String seme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String assessDiv,
            final String testKindItem,
            final String useCurriculumcd
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" SELECT ");
        stb.append("     T0.SCHREGNO, ");
        stb.append("     L2.* ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_DAT T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T0.YEAR ");
        stb.append("         AND T1.SEMESTER = T0.SEMESTER ");
        stb.append("         AND T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("     INNER JOIN ASSESS_LEVEL_MST L2 ON L2.YEAR = T0.YEAR ");
        stb.append("         AND L2.SEMESTER = T0.SEMESTER ");
        stb.append("         AND L2.TESTKINDCD = T0.TESTKINDCD ");
        stb.append("         AND L2.TESTITEMCD = T0.TESTITEMCD ");
        if ("1".equals(useCurriculumcd)) {
            stb.append("         AND L2.CLASSCD = T0.CLASSCD ");
            stb.append("         AND L2.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("         AND L2.CURRICULUM_CD = T0.CURRICULUM_CD ");
        }
        stb.append("         AND L2.SUBCLASSCD = T0.SUBCLASSCD ");
        stb.append("         AND L2.DIV = '" + assessDiv + "' ");
        stb.append("         AND L2.GRADE = T1.GRADE ");
        if ("2".equals(assessDiv)) {
            stb.append("         AND L2.HR_CLASS = T1.HR_CLASS ");
            stb.append("         AND L2.COURSECD = '0' ");
            stb.append("         AND L2.MAJORCD = '000' ");
            stb.append("         AND L2.COURSECODE = '0000' ");
        } else if ("3".equals(assessDiv)) {
            stb.append("         AND L2.HR_CLASS = '000' ");
            stb.append("         AND L2.COURSECD = T1.COURSECD ");
            stb.append("         AND L2.MAJORCD = T1.MAJORCD ");
            stb.append("         AND L2.COURSECODE = T1.COURSECODE ");
        } else {
            stb.append("         AND L2.HR_CLASS = '000' ");
            stb.append("         AND L2.COURSECD = '0' ");
            stb.append("         AND L2.MAJORCD = '000' ");
            stb.append("         AND L2.COURSECODE = '0000' ");
        }
        stb.append("         AND T0.SCORE BETWEEN L2.ASSESSLOW AND L2.ASSESSHIGH ");
        stb.append(" WHERE ");
        stb.append("     T0.YEAR = '" + year + "' ");
        stb.append("     AND T0.SEMESTER <= '" + seme + "' ");
        if (null != testKindItem) {
            stb.append("     AND T0.TESTKINDCD || T0.TESTITEMCD = '" + testKindItem + "' ");
        }
        stb.append("     AND T0.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T0.SCORE IS NOT NULL ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T0.SUBCLASSCD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T0.TESTKINDCD, ");
        stb.append("     T0.TESTITEMCD ");
    
        return stb.toString();
    }

    public static String getMockAssessLevelMstSql(
            final String year,
            final String ctrlSeme,
            final String schregno,
            final String sObjClass,
            final String eObjClass,
            final String assessDiv,
            final String mockcd
    ) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T0.SCHREGNO, ");
        stb.append("     '1' AS SEMESTER, ");
        stb.append("     L2.MOCKCD AS TEST_CD, ");
        stb.append("     L2.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
        stb.append("     L2.ASSESSLEVEL ");
        stb.append(" FROM ");
        stb.append("     MOCK_DAT T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = T0.YEAR ");
        stb.append("         AND T1.SEMESTER = '" + ctrlSeme + "' ");
        stb.append("         AND T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("     INNER JOIN MOCK_ASSESS_LEVEL_MST L2 ON L2.YEAR = T0.YEAR ");
        stb.append("         AND L2.MOCKCD = T0.MOCKCD ");
        stb.append("         AND L2.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD ");
        stb.append("         AND L2.DIV = '" + assessDiv + "' ");
        stb.append("         AND L2.GRADE = T1.GRADE ");
        if ("2".equals(assessDiv)) {
            stb.append("         AND L2.HR_CLASS = T1.HR_CLASS ");
            stb.append("         AND L2.COURSECD = '0' ");
            stb.append("         AND L2.MAJORCD = '000' ");
            stb.append("         AND L2.COURSECODE = '0000' ");
        } else if ("3".equals(assessDiv)) {
            stb.append("         AND L2.HR_CLASS = '000' ");
            stb.append("         AND L2.COURSECD = T1.COURSECD ");
            stb.append("         AND L2.MAJORCD = T1.MAJORCD ");
            stb.append("         AND L2.COURSECODE = T1.COURSECODE ");
        } else {
            stb.append("         AND L2.HR_CLASS = '000' ");
            stb.append("         AND L2.COURSECD = '0' ");
            stb.append("         AND L2.MAJORCD = '000' ");
            stb.append("         AND L2.COURSECODE = '0000' ");
        }
        stb.append("         AND T0.SCORE BETWEEN L2.ASSESSLOW AND L2.ASSESSHIGH ");
        stb.append(" WHERE ");
        stb.append("     T0.YEAR = '" + year + "' ");
        if (null != mockcd) {
            stb.append("     AND T0.MOCKCD = '" + mockcd + "' ");
        }
        stb.append("     AND T0.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T0.SCORE IS NOT NULL ");
        if (null != sObjClass && null != eObjClass) {
            stb.append("     AND (SUBSTR(T0.MOCK_SUBCLASS_CD,1,2) BETWEEN '" + sObjClass + "' AND '" + eObjClass + "') ");
        }

        return stb.toString();
    }

    public static String getAssessSubclassMstSql(
            final String year,
            final String seme,
            final String schregno
    ) {
        final StringBuffer stb = new StringBuffer();
    
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L2.* ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN ASSESS_SUBCLASS_MST L2 ON L2.YEAR = T1.YEAR ");
        stb.append("         AND L2.GRADE = T1.GRADE ");
        stb.append("         AND L2.COURSECD = T1.COURSECD ");
        stb.append("         AND L2.MAJORCD = T1.MAJORCD ");
        stb.append("         AND L2.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + seme + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
    
        return stb.toString();
    }

}
 // getReportCardInfo

// eof
