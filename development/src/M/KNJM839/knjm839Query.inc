<?php

require_once('for_php7.php');


class knjm839Query extends Query {


    //年度取得
    function getYear()
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      YEAR || '年度' AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_HDAT ";
        $query .= "  ORDER BY  ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期マスタ
    function getSemeMst($year)
    {
        $query  = "  SELECT DISTINCT";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '{$year}' ";
        $query .= "  AND SEMESTER <> '9' ";

        return $query;
    }

    function getSubclass($year, $semester) {
        $query  = " WITH ZENKI_SUB AS ( ";
        $query .= "     SELECT DISTINCT T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        $query .= "     FROM SUBCLASS_MST T1";
        $query .= "     INNER JOIN NAME_MST T2 ON NAMECD1 = 'M015' ";
        $query .= "      AND NAME1 = T1.CLASSCD ";
        $query .= "      AND NAME2 = T1.CURRICULUM_CD ";
        $query .= "      AND NAME3 = T1.SUBCLASSCD ";
        $query .= "      AND VALUE(ABBV1, 'H') = T1.SCHOOL_KIND ";
        $query .= " ), CHAIR_STD_MAX AS ( ";
        $query .= "     SELECT DISTINCT T1.YEAR, T1.SEMESTER, T1.CHAIRCD, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD, T1.SCHREGNO, ";
        $query .= "      CASE WHEN T3.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS ZENKI ";
        $query .= "     FROM CHAIR_STD_DAT T1";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     LEFT JOIN ZENKI_SUB T3 ON T3.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     WHERE T1.YEAR = '{$year}' ";
        if ('1' == $semester) { // 前期科目のみ
            $query .= "            AND T3.SUBCLASSCD IS NOT NULL ";
        }
        if ('' != $semester) {
            $query .= "            AND T1.SEMESTER <= '{$semester}' ";
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T5.CLASSNAME, ";
        $query .= "     T6.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_MAX T2 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T7 ON T7.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND VALUE(T7.INOUTCD, '') NOT IN ('8', '9') ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T8 ON T8.YEAR = T2.YEAR ";
        $query .= "         AND T8.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T8.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T11 ON T11.YEAR = T2.YEAR ";
        $query .= "         AND T11.GRADE = T8.GRADE ";
        $query .= "     INNER JOIN SCHREG_ENT_GRD_HIST_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T12.SCHOOL_KIND = T11.SCHOOL_KIND ";
        $query .= "         AND T12.ENT_DATE <= '".CTRL_DATE."' ";
        $query .= "         AND (T12.GRD_DATE IS NULL OR '".CTRL_DATE."' < T12.GRD_DATE) ";
        $query .= "     LEFT JOIN RECORD_CHKFIN_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "         AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T3.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "         AND T3.CHAIRCD = T2.CHAIRCD ";
        $query .= "         AND T3.TESTKINDCD = '99' ";
        $query .= "         AND T3.TESTITEMCD = '00' ";
        $query .= "         AND T3.RECORD_DIV = '2' ";
        $query .= "     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T6.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     LEFT JOIN ZENKI_SUB T13 ON T13.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T13.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T13.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T13.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     (T2.CLASSCD = '90' OR T2.CLASSCD <> '90' AND T3.CHAIRCD IS NOT NULL) ";
        $query .= " ORDER BY ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD ";
        return $query;
    }

    function getSubclassFugou($year, $semester, $classcd, $schoolKind, $curriculumCd, $subclasscd) {
        $query  = " WITH ZENKI_SUB AS ( ";
        $query .= "     SELECT DISTINCT T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        $query .= "     FROM SUBCLASS_MST T1";
        $query .= "     INNER JOIN NAME_MST T2 ON NAMECD1 = 'M015' ";
        $query .= "      AND NAME1 = T1.CLASSCD ";
        $query .= "      AND NAME2 = T1.CURRICULUM_CD ";
        $query .= "      AND NAME3 = T1.SUBCLASSCD ";
        $query .= "      AND VALUE(ABBV1, 'H') = T1.SCHOOL_KIND ";
        $query .= " ), CHAIR_STD_MAX AS ( ";
        $query .= "     SELECT T1.YEAR, T1.SEMESTER, T1.CHAIRCD, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD, T1.SCHREGNO, MAX(APPDATE) AS APPDATE, ";
        $query .= "      CASE WHEN T3.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS ZENKI ";
        $query .= "     FROM CHAIR_STD_DAT T1";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     LEFT JOIN ZENKI_SUB T3 ON T3.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     WHERE T1.YEAR = '{$year}' ";
        if ('1' == $semester) { // 前期科目のみ
            $query .= "            AND T3.SUBCLASSCD IS NOT NULL ";
        }
        if ('' != $semester) {
            $query .= "            AND T1.SEMESTER <= '{$semester}' ";
        }
        if ('' != $classcd || '' != $schoolKind || '' != $curriculumCd || '' != $subclasscd) {
            $query .= "            AND T2.CLASSCD = '{$classcd}' AND T2.SCHOOL_KIND = '{$schoolKind}' AND T2.CURRICULUM_CD = '{$curriculumCd}' AND T2.SUBCLASSCD = '{$subclasscd}' ";
        }
        $query .= "     GROUP BY T1.YEAR, T1.SEMESTER, T1.CHAIRCD, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD, T1.SCHREGNO, ";
        $query .= "      CASE WHEN T3.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END ";
        $query .= " ), SUBCLASS_SCORE0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.CLASSCD, ";
        $query .= "         T2.SCHOOL_KIND, ";
        $query .= "         T2.CURRICULUM_CD, ";
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T7.NAME, ";
        $query .= "         T9.CREDITS, ";
        $query .= "         CASE WHEN T2.ZENKI = 1 THEN T10.SEM_PASS_FLG ";
        $query .= "              ELSE T13.SEM_PASS_FLG ";
        $query .= "         END AS SEM_PASS_FLG, ";
        $query .= "         CASE WHEN T2.CLASSCD = '90' AND VALUE(T4.GET_CREDIT, 0) <> 0 THEN 1 ";
        $query .= "              WHEN T2.CLASSCD <> '90' AND T4.GET_CREDIT IS NOT NULL THEN 1 ";
        $query .= "              ELSE 0 END AS GOUKAKU, ";
        $query .= "         CASE WHEN T2.CLASSCD = '90' AND VALUE(T4.GET_CREDIT, 0) = 0 THEN 1 ";
        $query .= "              WHEN T2.ZENKI  = 1 AND T4.SEM1_INTR_VALUE < 40 AND VALUE(SEM1_TERM_VALUE, 0) < 40 THEN 1 "; // 前期科目
        $query .= "              WHEN T2.ZENKI <> 1 AND T4.SEM2_INTR_VALUE < 40 AND VALUE(SEM2_TERM_VALUE, 0) < 40 THEN 1 "; // 後期・通年科目
        $query .= "              ELSE 0 END AS FUGOUKAKU ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_MAX T2 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T7 ON T7.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND VALUE(T7.INOUTCD, '') NOT IN ('8', '9') ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT T8 ON T8.YEAR = T2.YEAR ";
        $query .= "             AND T8.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T8.SCHREGNO = T2.SCHREGNO ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T11 ON T11.YEAR = T2.YEAR ";
        $query .= "             AND T11.GRADE = T8.GRADE ";
        $query .= "         INNER JOIN SCHREG_ENT_GRD_HIST_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND T12.SCHOOL_KIND = T11.SCHOOL_KIND ";
        $query .= "             AND T12.ENT_DATE <= '".CTRL_DATE."' ";
        $query .= "             AND (T12.GRD_DATE IS NULL OR '".CTRL_DATE."' < T12.GRD_DATE) ";
        $query .= "         LEFT JOIN CREDIT_MST T9 ON T9.YEAR = T8.YEAR ";
        $query .= "             AND T9.COURSECD = T8.COURSECD ";
        $query .= "             AND T9.MAJORCD = T8.MAJORCD ";
        $query .= "             AND T9.GRADE = T8.GRADE ";
        $query .= "             AND T9.CLASSCD = T2.CLASSCD ";
        $query .= "             AND T9.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T9.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "             AND T9.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "         LEFT JOIN RECORD_DAT T4 ON T4.YEAR = T2.YEAR ";
        $query .= "             AND T4.CLASSCD = T2.CLASSCD ";
        $query .= "             AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "             AND T4.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "         LEFT JOIN RECORD_CHKFIN_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "             AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T3.CLASSCD = T2.CLASSCD ";
        $query .= "             AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "             AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "             AND T3.CHAIRCD = T2.CHAIRCD ";
        $query .= "             AND T3.TESTKINDCD = '99' ";
        $query .= "             AND T3.TESTITEMCD = '00' ";
        $query .= "             AND T3.RECORD_DIV = '2' ";
        $query .= "         LEFT JOIN SUBCLASS_STD_PASS_DAT T10 ON T10.YEAR = T2.YEAR ";
        $query .= "             AND T10.SEMESTER = '1' ";
        $query .= "             AND T10.CLASSCD = T2.CLASSCD ";
        $query .= "             AND T10.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T10.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "             AND T10.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "             AND T10.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND T10.SEM_PASS_FLG = '1' ";
        $query .= "         LEFT JOIN SUBCLASS_STD_PASS_DAT T13 ON T13.YEAR = T2.YEAR ";
        if ('' == $semester) {
            $query .= "             AND T13.SEMESTER = '2' ";
        } else {
            $query .= "             AND T13.SEMESTER = '{$semester}' ";
        }
        $query .= "             AND T13.CLASSCD = T2.CLASSCD ";
        $query .= "             AND T13.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "             AND T13.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "             AND T13.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "             AND T13.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND T13.SEM_PASS_FLG = '1' ";
        $query .= " WHERE ";
        $query .= "     (T2.CLASSCD = '90' OR T2.CLASSCD <> '90' AND T3.CHAIRCD IS NOT NULL) ";
        $query .= " ), SUBCLASS_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         NAME, ";
        $query .= "         SEM_PASS_FLG, ";
        $query .= "         MAX(CREDITS) AS CREDITS, ";
        $query .= "         GOUKAKU, "; 
        $query .= "         FUGOUKAKU ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_SCORE0 ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         NAME, ";
        $query .= "         SEM_PASS_FLG, ";
        $query .= "         GOUKAKU, "; 
        $query .= "         FUGOUKAKU ";
        $query .= " ), TOUKEI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR ";
        if ('' != $classcd || '' != $schoolKind || '' != $curriculumCd || '' != $subclasscd) {
            $query .= "         ,T1.CLASSCD ";
            $query .= "         ,T1.SCHOOL_KIND ";
            $query .= "         ,T1.CURRICULUM_CD ";
            $query .= "         ,T1.SUBCLASSCD ";
            $query .= "         ,MAX(T1.CREDITS) AS CREDITS ";
        }
        $query .= "         ,COUNT(*) AS JUKOUSHA_SU ";
        $query .= "         ,SUM(CASE WHEN SEM_PASS_FLG = '1' THEN T1.GOUKAKU ELSE 0 END) AS GOUKAKUSHA_SU ";
        $query .= "         ,SUM(CASE WHEN SEM_PASS_FLG = '1' THEN T1.FUGOUKAKU ELSE 0 END) AS FUGOUKAKUSHA_SU ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_SCORE T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR ";
        if ('' != $classcd || '' != $schoolKind || '' != $curriculumCd || '' != $subclasscd) {
            $query .= "         ,T1.CLASSCD ";
            $query .= "         ,T1.SCHOOL_KIND ";
            $query .= "         ,T1.CURRICULUM_CD ";
            $query .= "         ,T1.SUBCLASSCD ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        if ('' != $classcd || '' != $schoolKind || '' != $curriculumCd || '' != $subclasscd) {
            $query .= "     ,T2.SCHREGNO ";
            $query .= "     ,T2.NAME ";
        }
        $query .= " FROM TOUKEI T1 ";
        if ('' != $classcd || '' != $schoolKind || '' != $curriculumCd || '' != $subclasscd) {
            $query .= " LEFT JOIN SUBCLASS_SCORE T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "     AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "     AND T2.FUGOUKAKU = 1 ";
            $query .= " ORDER BY T2.SCHREGNO ";
        }
        return $query;
    }

//    function getSubclassHoryu($year, $semester, $classcd, $schoolKind, $curriculumCd, $subclasscd) {
//        $query  = " WITH CHAIR_STD_MAX AS ( ";
//        $query .= "     SELECT YEAR, SEMESTER, CHAIRCD, SCHREGNO, MAX(APPDATE) AS APPEDATE ";
//        $query .= "     FROM CHAIR_STD_DAT ";
//        $query .= "     WHERE YEAR = '{$year}' ";
//        if ('' != $semester) {
//            $query .= "            AND SEMESTER = '{$semester}' ";
//        }
//        $query .= "     GROUP BY YEAR, SEMESTER, CHAIRCD, SCHREGNO ";
//        $query .= " ), SUBCLASS_SCORE AS ( ";
//        $query .= "     SELECT ";
//        $query .= "         T2.YEAR, ";
//        $query .= "         T2.SEMESTER, ";
//        $query .= "         T2.CLASSCD, ";
//        $query .= "         T2.SCHOOL_KIND, ";
//        $query .= "         T2.CURRICULUM_CD, ";
//        $query .= "         T2.SUBCLASSCD, ";
//        $query .= "         T1.SCHREGNO, ";
//        $query .= "         T4.GRAD_VALUE ";
//        $query .= "     FROM ";
//        $query .= "         CHAIR_STD_DAT T1 ";
//        $query .= "         INNER JOIN CHAIR_STD_MAX T1_2 ON T1_2.YEAR = T1.YEAR ";
//        $query .= "             AND T1_2.SEMESTER = T1.SEMESTER ";
//        $query .= "             AND T1_2.CHAIRCD = T1.CHAIRCD ";
//        $query .= "             AND T1_2.SCHREGNO = T1.SCHREGNO ";
//        $query .= "         INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
//        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
//        $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
//        $query .= "         INNER JOIN RECORD_CHKFIN_DAT T3 ON T3.YEAR = T2.YEAR ";
//        $query .= "             AND T3.SEMESTER = T2.SEMESTER ";
//        $query .= "             AND T3.CLASSCD = T2.CLASSCD ";
//        $query .= "             AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
//        $query .= "             AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
//        $query .= "             AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
//        $query .= "             AND T3.CHAIRCD = T2.CHAIRCD ";
//        $query .= "             AND T3.TESTKINDCD = '99' ";
//        $query .= "             AND T3.TESTITEMCD = '00' ";
//        $query .= "             AND T3.RECORD_DIV = '2' ";
//        $query .= "         INNER JOIN SUBCLASS_STD_PASS_DAT T10 ON T10.YEAR = T2.YEAR ";
//        $query .= "             AND T10.SEMESTER = T2.SEMESTER ";
//        $query .= "             AND T10.CLASSCD = T2.CLASSCD ";
//        $query .= "             AND T10.SCHOOL_KIND = T2.SCHOOL_KIND ";
//        $query .= "             AND T10.CURRICULUM_CD = T2.CURRICULUM_CD ";
//        $query .= "             AND T10.SUBCLASSCD = T2.SUBCLASSCD ";
//        $query .= "             AND T10.SCHREGNO = T1.SCHREGNO ";
//        $query .= "             AND T10.SEM_PASS_FLG = '1' ";
//        $query .= "         LEFT JOIN RECORD_DAT T4 ON T4.YEAR = T2.YEAR ";
//        $query .= "             AND T4.CLASSCD = T2.CLASSCD ";
//        $query .= "             AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ";
//        $query .= "             AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ";
//        $query .= "             AND T4.SUBCLASSCD = T2.SUBCLASSCD ";
//        $query .= "             AND T4.SCHREGNO = T1.SCHREGNO ";
//        $query .= "         LEFT JOIN SCHREG_BASE_MST T7 ON T7.SCHREGNO = T1.SCHREGNO ";
//        if ('' != $classcd && '' != $schoolKind && '' != $curriculumCd && '' != $subclasscd) {
//            $query .= "     WHERE ";
//            $query .= "         T2.CLASSCD = '{$classcd}' AND T2.SCHOOL_KIND = '{$schoolKind}' AND T2.CURRICULUM_CD = '{$curriculumCd}' AND T2.SUBCLASSCD = '{$subclasscd}' ";
//        }
//        $query .= " ) ";
//        $query .= "     SELECT DISTINCT ";
//        $query .= "         T1.YEAR, ";
//        $query .= "         T1.CLASSCD, ";
//        $query .= "         T1.SCHOOL_KIND, ";
//        $query .= "         T1.CURRICULUM_CD, ";
//        $query .= "         T1.SUBCLASSCD, ";
//        $query .= "         T1.SCHREGNO, ";
//        $query .= "         T3.NAME ";
//        $query .= "     FROM ";
//        $query .= "         SUBCLASS_SCORE T1 ";
//        $query .= "         LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
//        $query .= "     WHERE ";
//        $query .= "         T1.GRAD_VALUE IS NULL ";
//        return $query;
//    }


}
?>

