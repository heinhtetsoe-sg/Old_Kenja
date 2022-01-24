<?php

require_once('for_php7.php');


class knjm391mQuery extends Query {

    //CSV出力
    function getCsvQueryDay($model, $baseYear) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T5.INOUTCD, ";
        $query .= "         T5.ENT_DIV, ";
        $query .= "         FISCALYEAR(T5.ENT_DATE) AS ENT_YEAR, ";
        $query .= "         T4.CLASSCD, ";
        $query .= "         T9.BASE_REMARK1 AS GRD_YOTEI ";
        $query .= "     FROM ";
        $query .= "         SCH_ATTEND_DAT T1 ";
        $query .= "         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "             AND T2.SEMESTER <> '9' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "             AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T3.CHAIRCD = T1.CHAIRCD ";
        $query .= "         INNER JOIN SUBCLASS_MST T4 ON T4.CLASSCD = T3.CLASSCD ";
        $query .= "             AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "             AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "             AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T9 ON T9.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T9.YEAR = '".$baseYear."' ";
        $query .= "             AND T9.BASE_SEQ = '001' ";
        $query .= "     WHERE ";
        $query .= "         T1.EXECUTEDATE = '".(str_replace("/", "-", $model->field["DATE"]))."' AND T1.SCHOOLINGKINDCD <> '2' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.ATTENDDATE AS EXECUTEDATE, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T5.INOUTCD, ";
        $query .= "         T5.ENT_DIV, ";
        $query .= "         FISCALYEAR(T5.ENT_DATE) AS ENT_YEAR, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T9.BASE_REMARK1 AS GRD_YOTEI ";
        $query .= "     FROM ";
        $query .= "         SPECIALACT_ATTEND_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T9 ON T9.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T9.YEAR = '".$baseYear."' ";
        $query .= "             AND T9.BASE_SEQ = '001' ";
        $query .= "     WHERE ";
        $query .= "         T1.ATTENDDATE = '".(str_replace("/", "-", $model->field["DATE"]))."' ";
        $query .= " ), MASTER(ID, DIV, TITLE) AS ( ";
        $query .= "               VALUES(1, 'LOGIN_YEAR', '".$baseYear."' || '年度') ";
        $query .= "     UNION ALL VALUES(2, 'LOGIN_YEAR-1', '".($baseYear - 1)."' || '年度') ";
        $query .= "     UNION ALL VALUES(3, 'LOGIN_YEAR-2', '".($baseYear - 2)."' || '年度') ";
        $query .= "     UNION ALL VALUES(4, 'LOGIN_YEAR_OUT', '以前') ";
        $query .= "     UNION ALL VALUES(5, 'INOUTCD9', '併修生') ";
        $query .= "     UNION ALL VALUES(6, 'INOUTCD8', '聴講生') ";
        $query .= " ), STAT AS ( ";
        $query .= "     SELECT 'LOGIN_YEAR' AS DIV, T1.* ";
        $query .= "     FROM MAIN T1 WHERE T1.INOUTCD NOT IN ('8', '9') AND T1.ENT_YEAR = '".$baseYear."' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT 'LOGIN_YEAR-1' AS DIV, T1.* ";
        $query .= "     FROM MAIN T1 WHERE T1.INOUTCD NOT IN ('8', '9') AND T1.ENT_YEAR = '".($baseYear - 1)."' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT 'LOGIN_YEAR-2' AS DIV, T1.* ";
        $query .= "     FROM MAIN T1 WHERE T1.INOUTCD NOT IN ('8', '9') AND T1.ENT_YEAR = '".($baseYear - 2)."' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT 'LOGIN_YEAR_OUT' AS DIV, T1.* ";
        $query .= "     FROM MAIN T1 WHERE T1.INOUTCD NOT IN ('8', '9') AND T1.ENT_YEAR < '".($baseYear - 2)."' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT 'INOUTCD8' AS DIV, T1.* ";
        $query .= "     FROM MAIN T1 WHERE T1.INOUTCD = '8' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT 'INOUTCD9' AS DIV, T1.* ";
        $query .= "     FROM MAIN T1 WHERE T1.INOUTCD = '9' ";
        $query .= " ), STAT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.SCHREGNO AS TOTAL, ";
        $query .= "         CASE WHEN T1.ENT_DIV IN ('1', '2', '3') THEN T1.SCHREGNO END AS IPPAN, ";
        $query .= "         CASE WHEN T1.ENT_DIV IN ('4', '5') THEN T1.SCHREGNO END AS HENNYU, ";
        $query .= "         CASE WHEN T1.CLASSCD IN ('93', '94') THEN T1.SCHREGNO END AS HR_GYOUJI, ";
        $query .= "         CASE WHEN T1.CLASSCD IN ('93') THEN T1.SCHREGNO END AS HR, ";
        $query .= "         CASE WHEN T1.CLASSCD IN ('94') THEN T1.SCHREGNO END AS GYOUJI, ";
        $query .= "         CASE WHEN T1.GRD_YOTEI IS NOT NULL AND T1.CLASSCD IN ('93', '94') THEN T1.SCHREGNO END AS GRD_HR_GYOUJI, ";
        $query .= "         CASE WHEN T1.GRD_YOTEI IS NOT NULL AND T1.CLASSCD IN ('93') THEN T1.SCHREGNO END AS GRD_HR, ";
        $query .= "         CASE WHEN T1.GRD_YOTEI IS NOT NULL AND T1.CLASSCD IN ('94') THEN T1.SCHREGNO END AS GRD_GYOUJI ";
        $query .= "     FROM ";
        $query .= "         STAT T1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.ID, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.TITLE, ";
        $query .= "     COUNT(DISTINCT TOTAL) AS TOTAL, ";
        $query .= "     COUNT(DISTINCT IPPAN) AS IPPAN, ";
        $query .= "     COUNT(DISTINCT HENNYU) AS HENNYU, ";
        $query .= "     COUNT(DISTINCT HR_GYOUJI) AS HR_GYOUJI, ";
        $query .= "     COUNT(DISTINCT HR) AS HR, ";
        $query .= "     COUNT(DISTINCT GYOUJI) AS GYOUJI, ";
        $query .= "     COUNT(DISTINCT GRD_HR_GYOUJI) AS GRD_HR_GYOUJI, ";
        $query .= "     COUNT(DISTINCT GRD_HR) AS GRD_HR, ";
        $query .= "     COUNT(DISTINCT GRD_GYOUJI) AS GRD_GYOUJI ";
        $query .= " FROM MASTER T1 ";
        $query .= "     LEFT JOIN STAT2 T2 ON T2.DIV = T1.DIV ";
        $query .= " GROUP BY ";
        $query .= "     T1.ID, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.TITLE ";
        $query .= " ORDER BY ";
        $query .= "     T1.ID ";
        return $query;
    }

    //校時
    function getPeriod($model, $baseYear) {
        $query  = " SELECT T1.NAMECD2, T1.NAME1 ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN NAME_YDAT T2 ON T2.YEAR = '".$baseYear."' ";
        $query .= "     AND T2.NAMECD1 = T1.NAMECD1 ";
        $query .= "     AND T2.NAMECD2 = T1.NAMECD2 ";
        $query .= " WHERE T1.NAMECD1 = 'B001' ";
        $query .= " ORDER BY T1.NAMECD2 ";
        return $query;
    }

    //学期
    function getSemester($model, $baseYear) {
        $query  = " SELECT T1.* ";
        $query .= " FROM SEMESTER_MST T1 ";
        $query .= " WHERE T1.YEAR = '".$baseYear."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        return $query;
    }

    //CSV出力
    function getCsvQuerySubclass($model) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT  ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN  ";
        $query .= "             T5.COMBINED_CLASSCD || '-' || T5.COMBINED_SCHOOL_KIND || '-' || T5.COMBINED_CURRICULUM_CD || '-' || T5.COMBINED_SUBCLASSCD ";
        $query .= "         ELSE ";
        $query .= "             T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
        $query .= "         END AS SUBCLASSCD, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCH_ATTEND_DAT T1 ";
        $query .= "         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "             AND T2.SEMESTER <> '9' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "             AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "             AND T3.CHAIRCD = T1.CHAIRCD ";
        $query .= "         LEFT JOIN SUBCLASS_SCHOOLING_COMBINED_DAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "             AND T5.ATTEND_CLASSCD = T3.CLASSCD ";
        $query .= "             AND T5.ATTEND_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "             AND T5.ATTEND_CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "             AND T5.ATTEND_SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLINGKINDCD <> '2' ";
        $query .= "         AND T3.CLASSCD <= '90' AND T1.EXECUTEDATE = '".(str_replace("/", "-", $model->field["DATE"]))."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     VALUE(T1.PERIODCD, '999999') AS PERIODCD, ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM MAIN T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST T4 ON T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     GROUPING SETS ((T1.SUBCLASSCD, T4.SUBCLASSNAME, T1.PERIODCD), (T1.SUBCLASSCD, T4.SUBCLASSNAME)) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= "      ";
        return $query;
    }
}
?>
