<?php

require_once('for_php7.php');

class knjf307aQuery extends Query {
    //教育委員会チェック
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //年度
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS VALUE, ";
        $query .= "     '".CTRL_YEAR."' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象学校
    function getSchoolData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN L1.ANSWER_FLG = '1' ";
        $query .= "          THEN '【回済】' ";
        $query .= "          ELSE '　　　　' ";
        $query .= "     END AS FLG_NAME, ";
        $query .= "     '0' AS FLG, ";
        $query .= "     L1.EDBOARD_SCHOOLCD, ";
        $query .= "     L2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT L1 ";
        $query .= "              ON L1.YEAR         = T1.YEAR ";
        $query .= "             AND L1.DOC_NUMBER   = T1.DOC_NUMBER ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L2 ";
        $query .= "              ON L2.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     T1.REQUEST_ANSWER_PRG = 'KNJF307'  ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '【未発】' AS FLG_NAME, ";
        $query .= "     '1' AS FLG, ";
        $query .= "     L1.EDBOARD_SCHOOLCD, ";
        $query .= "     L2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT L1 ";
        $query .= "              ON L1.YEAR         = T1.YEAR  ";
        $query .= "             AND L1.DOC_NUMBER   = T1.DOC_NUMBER  ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L2 ";
        $query .= "              ON L2.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     L1.EDBOARD_SCHOOLCD NOT IN ( ";
        $query .= "                                 SELECT DISTINCT ";
        $query .= "                                     N2.EDBOARD_SCHOOLCD ";
        $query .= "                                 FROM ";
        $query .= "                                     AFT_SEARCH_REPORT_DAT N1 ";
        $query .= "                                     INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT N2 ";
        $query .= "                                          ON N1.YEAR         = N2.YEAR ";
        $query .= "                                         AND N1.DOC_NUMBER   = N2.DOC_NUMBER ";
        $query .= "                                 WHERE ";
        $query .= "                                     N1.YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "                                     N1.REQUEST_ANSWER_PRG = 'KNJF307' ";
        $query .= "                                 ) ";
        $query .= " ORDER BY ";
        $query .= "     EDBOARD_SCHOOLCD ";

        return $query;
    }

    //学校名取得
    function getEdboardSchoolName($schoolCd) {
        $query  = " SELECT ";
        $query .= "     EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$schoolCd}' ";

        return $query;
    }

    //年齢取得
    function getAge($model, $schoolCd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     AGE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION307_FIXED_DAT ";
        $query .= " WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "     EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') ";
        } else {
            $query .= "     EDBOARD_SCHOOLCD = '{$schoolCd}' ";
        }
        $query .= "     AND YEAR = '{$model->field["YEAR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     AGE ";

        return $query;
    }

    //コースコード取得
    function getCoursecd() {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //CSV出力データ取得
    function selectCsvQuery($model, $dataDiv, $coursecd, $schoolCd) {

        $query  = " WITH MAX_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EDBOARD_SCHOOLCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.FIXED_DATE ";
        $query .= "     FROM ";
        $query .= "         REPORT_MEDEXAM_DISEASE_ADDITION307_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(EXECUTE_DATE) AS EXECUTE_DATE ";
        $query .= "         FROM ";
        $query .= "             REPORT_MEDEXAM_DISEASE_ADDITION307_DAT ";
        $query .= "         WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "             EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') AND ";
        } else {
            $query .= "             EDBOARD_SCHOOLCD = '{$schoolCd}' AND ";
        }
        $query .= "             YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD AND ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.EXECUTE_DATE = T2.EXECUTE_DATE ";
        $query .= " ) ";
        foreach ($model->age as $ageVal) {
            $query .= " , INTVAL_{$ageVal} AS ( ";
            $query .= " SELECT ";
            $query .= "     FIXED.COURSECD, ";
            $query .= "     FIXED.MAJORCD, ";
            $query .= "     FIXED.AGE, ";
            $query .= "     FIXED.SEQ, ";
            $query .= "     FIXED.SEX, ";
            $query .= "     SUM(INT_VAL1) AS INT_VAL1, ";
            $query .= "     SUM(INT_VAL2) AS INT_VAL2, ";
            $query .= "     SUM(INT_VAL2) / SUM(INT_VAL1) AS INT_AVG ";
            $query .= " FROM ";
            $query .= "     MEDEXAM_DISEASE_ADDITION307_FIXED_DAT FIXED ";
            $query .= "     INNER JOIN MAX_DATE ON FIXED.EDBOARD_SCHOOLCD = MAX_DATE.EDBOARD_SCHOOLCD ";
            $query .= "           AND FIXED.YEAR = MAX_DATE.YEAR ";
            $query .= "           AND FIXED.FIXED_DATE = MAX_DATE.FIXED_DATE ";
            $query .= " WHERE ";
            $query .= "     FIXED.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND AGE = {$ageVal} ";
            $query .= " GROUP BY ";
            $query .= "     FIXED.COURSECD, ";
            $query .= "     FIXED.MAJORCD, ";
            $query .= "     FIXED.AGE, ";
            $query .= "     FIXED.SEQ, ";
            $query .= "     FIXED.SEX ";
            $query .= " ) ";
        }
        $query .= " , MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     FIXED.COURSECD, ";
        $query .= "     FIXED.MAJORCD, ";
        $query .= "     FIXED.SEQ, ";
        $query .= "     FIXED.SEX ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION307_FIXED_DAT FIXED ";
        $query .= "     INNER JOIN MAX_DATE ON FIXED.EDBOARD_SCHOOLCD = MAX_DATE.EDBOARD_SCHOOLCD ";
        $query .= "           AND FIXED.YEAR = MAX_DATE.YEAR ";
        $query .= "           AND FIXED.FIXED_DATE = MAX_DATE.FIXED_DATE ";
        $query .= " WHERE ";
        $query .= "     FIXED.YEAR = '{$model->field["YEAR"]}' ";
        $query .= " GROUP BY ";
        $query .= "     FIXED.COURSECD, ";
        $query .= "     FIXED.MAJORCD, ";
        $query .= "     FIXED.SEQ, ";
        $query .= "     FIXED.SEX ";
        $query .= " ), SELECT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL1 AS VAL1_{$ageVal} ";
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL2 AS VAL2_{$ageVal} ";
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL2 / INTVAL_{$ageVal}.INT_VAL1 AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        foreach ($model->age as $ageVal) {
            $query .= "     LEFT JOIN INTVAL_{$ageVal} ON MAIN_T.COURSECD = INTVAL_{$ageVal}.COURSECD ";
            $query .= "          AND MAIN_T.MAJORCD = INTVAL_{$ageVal}.MAJORCD ";
            $query .= "          AND MAIN_T.SEQ = INTVAL_{$ageVal}.SEQ ";
            $query .= "          AND MAIN_T.SEX = INTVAL_{$ageVal}.SEX ";
        }
        $query .= " WHERE ";
        $query .= "     MAIN_T.SEQ = '01' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     '999' AS MAJORCD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL1) AS VAL1_{$ageVal} ";
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL2) AS VAL2_{$ageVal} ";
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL2) / SUM(INTVAL_{$ageVal}.INT_VAL1) AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        foreach ($model->age as $ageVal) {
            $query .= "     LEFT JOIN INTVAL_{$ageVal} ON MAIN_T.COURSECD = INTVAL_{$ageVal}.COURSECD ";
            $query .= "          AND MAIN_T.MAJORCD = INTVAL_{$ageVal}.MAJORCD ";
            $query .= "          AND MAIN_T.SEQ = INTVAL_{$ageVal}.SEQ ";
            $query .= "          AND MAIN_T.SEX = INTVAL_{$ageVal}.SEX ";
        }
        $query .= " WHERE ";
        $query .= "     MAIN_T.SEQ = '01' ";
        $query .= " GROUP BY ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL1 AS VAL1_{$ageVal} ";
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL2 AS VAL2_{$ageVal} ";
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL2 / INTVAL_{$ageVal}.INT_VAL1 AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        foreach ($model->age as $ageVal) {
            $query .= "     LEFT JOIN INTVAL_{$ageVal} ON MAIN_T.COURSECD = INTVAL_{$ageVal}.COURSECD ";
            $query .= "          AND MAIN_T.MAJORCD = INTVAL_{$ageVal}.MAJORCD ";
            $query .= "          AND MAIN_T.SEQ = INTVAL_{$ageVal}.SEQ ";
            $query .= "          AND MAIN_T.SEX = INTVAL_{$ageVal}.SEX ";
        }
        $query .= " WHERE ";
        $query .= "     MAIN_T.SEQ = '02' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     '999' AS MAJORCD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL1) AS VAL1_{$ageVal} ";
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL2) AS VAL2_{$ageVal} ";
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL2) / SUM(INTVAL_{$ageVal}.INT_VAL1) AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        foreach ($model->age as $ageVal) {
            $query .= "     LEFT JOIN INTVAL_{$ageVal} ON MAIN_T.COURSECD = INTVAL_{$ageVal}.COURSECD ";
            $query .= "          AND MAIN_T.MAJORCD = INTVAL_{$ageVal}.MAJORCD ";
            $query .= "          AND MAIN_T.SEQ = INTVAL_{$ageVal}.SEQ ";
            $query .= "          AND MAIN_T.SEX = INTVAL_{$ageVal}.SEX ";
        }
        $query .= " WHERE ";
        $query .= "     MAIN_T.SEQ = '02' ";
        $query .= " GROUP BY ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.MAJORCD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL1 AS VAL1_{$ageVal} ";
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL2 AS VAL2_{$ageVal} ";
            $query .= "     ,INTVAL_{$ageVal}.INT_VAL2 / INTVAL_{$ageVal}.INT_VAL1 AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        foreach ($model->age as $ageVal) {
            $query .= "     LEFT JOIN INTVAL_{$ageVal} ON MAIN_T.COURSECD = INTVAL_{$ageVal}.COURSECD ";
            $query .= "          AND MAIN_T.MAJORCD = INTVAL_{$ageVal}.MAJORCD ";
            $query .= "          AND MAIN_T.SEQ = INTVAL_{$ageVal}.SEQ ";
            $query .= "          AND MAIN_T.SEX = INTVAL_{$ageVal}.SEX ";
        }
        $query .= " WHERE ";
        $query .= "     MAIN_T.SEQ = '03' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     '999' AS MAJORCD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL1) AS VAL1_{$ageVal} ";
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL2) AS VAL2_{$ageVal} ";
            $query .= "     ,SUM(INTVAL_{$ageVal}.INT_VAL2) / SUM(INTVAL_{$ageVal}.INT_VAL1) AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        foreach ($model->age as $ageVal) {
            $query .= "     LEFT JOIN INTVAL_{$ageVal} ON MAIN_T.COURSECD = INTVAL_{$ageVal}.COURSECD ";
            $query .= "          AND MAIN_T.MAJORCD = INTVAL_{$ageVal}.MAJORCD ";
            $query .= "          AND MAIN_T.SEQ = INTVAL_{$ageVal}.SEQ ";
            $query .= "          AND MAIN_T.SEX = INTVAL_{$ageVal}.SEX ";
        }
        $query .= " WHERE ";
        $query .= "     MAIN_T.SEQ = '03' ";
        $query .= " GROUP BY ";
        $query .= "     MAIN_T.COURSECD, ";
        $query .= "     MAIN_T.SEQ, ";
        $query .= "     MAIN_T.SEX ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SE_T.COURSECD, ";
        $query .= "     SE_T.MAJORCD, ";
        $query .= "     CASE WHEN SE_T.MAJORCD = '999' ";
        $query .= "          THEN CM.COURSENAME ";
        $query .= "          ELSE CM.COURSENAME || MM.MAJORNAME ";
        $query .= "     END AS COURSENAME, ";
        $query .= "     SE_T.SEQ, ";
        $query .= "     SE_T.SEX, ";
        $query .= "     VN1.NAME1 AS SEXNAME ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,CASE WHEN VAL1_{$ageVal} IS NULL THEN 0 ELSE VAL1_{$ageVal} END AS VAL1_{$ageVal} ";
            $query .= "     ,CASE WHEN VAL2_{$ageVal} IS NULL THEN 0 ELSE VAL2_{$ageVal} END AS VAL2_{$ageVal} ";
            $query .= "     ,CASE WHEN AVG_{$ageVal} IS NULL THEN 0 ELSE AVG_{$ageVal} END AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     SELECT_T SE_T ";
        $query .= " LEFT JOIN COURSE_MST CM ON SE_T.COURSECD = CM.COURSECD  ";
        $query .= " LEFT JOIN MAJOR_MST MM ON SE_T.COURSECD = MM.COURSECD  ";
        $query .= "                                AND SE_T.MAJORCD  = MM.MAJORCD  ";
        $query .= " LEFT JOIN NAME_MST VN1 ON VN1.NAMECD1 = 'Z002'  ";
        $query .= "                       AND SE_T.SEX    = VN1.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     SE_T.SEQ, ";
        $query .= "     SE_T.COURSECD, ";
        $query .= "     SE_T.MAJORCD, ";
        $query .= "     SE_T.SEX ";

        return $query;
    }
}
?>
