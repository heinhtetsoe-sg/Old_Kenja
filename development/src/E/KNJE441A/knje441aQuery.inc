<?php

require_once('for_php7.php');

class knje441aQuery extends Query {
    //教育委員会チェック
    function getIinkaiFlg() {
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
        $query .= "     T1.REQUEST_ANSWER_PRG = 'KNJE441'  ";
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
        $query .= "                                     N1.REQUEST_ANSWER_PRG = 'KNJE441' ";
        $query .= "                                 ) ";
        $query .= " ORDER BY ";
        $query .= "     EDBOARD_SCHOOLCD ";

        return $query;
    }

    //課程学科性別取得
    function getCsvMajor($model, $table, $schoolCd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     MAJOR.MAJORNAME, ";
        $query .= "     Z002.NAMECD2 AS SEX, ";
        $query .= "     Z002.ABBV1 ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= "     LEFT JOIN MAJOR_MST MAJOR ON T1.COURSECD = MAJOR.COURSECD ";
        $query .= "          AND T1.MAJORCD = MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= " WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "     T1.EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') ";
        } else {
            $query .= "     T1.EDBOARD_SCHOOLCD = '{$schoolCd}' ";
        }
        $query .= "     AND T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     Z002.NAMECD2 ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

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

    //FIXED_DATEのMAX取得
    function getMaxDateQuery($model, $table, $schoolCd) {
        $query  = " WITH MAX_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EDBOARD_SCHOOLCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.FIXED_DATE ";
        $query .= "     FROM ";
        $query .= "         {$table} T1, ";
        $query .= "         (SELECT ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(EXECUTE_DATE) AS EXECUTE_DATE ";
        $query .= "         FROM ";
        $query .= "             {$table} ";
        $query .= "         WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "             EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') AND ";
        } else {
            $query .= "             EDBOARD_SCHOOLCD = '{$schoolCd}' AND ";
        }
        $query .= "             YEAR = '".$model->field["YEAR"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD AND ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.EXECUTE_DATE = T2.EXECUTE_DATE ";
        $query .= " ) ";

        return $query;
    }

    //CSV
    function getCollegeCnt($model, $msVal, $dataVal, $schoolCd) {

        $query  = knje441aQuery::getMaxDateQuery($model, "REPORT_AFT_DISEASE_ADDITION441_COLLEGE_DAT", $schoolCd);

        $query .= " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT T1 ";
        $query .= "     INNER JOIN MAX_DATE T2 ";
        $query .= "          ON T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD ";
        $query .= "         AND T1.YEAR         = T2.YEAR ";
        $query .= "         AND T1.FIXED_DATE   = T2.FIXED_DATE ";
        $query .= " WHERE ";
        $query .= "         T1.COURSECD     = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD      = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX          = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.SCHOOL_SORT  = '{$dataVal["SCHOOL_SORT"]}' ";
        $query .= "     AND T1.SCHOOL_GROUP = '{$dataVal["SCHOOL_GROUP"]}' ";

        return $query;
    }

    //CSV
    function getCollege2Cnt($model, $msVal, $dataVal, $e050Val, $schoolCd) {

        $query  = knje441aQuery::getMaxDateQuery($model, "REPORT_AFT_DISEASE_ADDITION441_COLLEGE2_DAT", $schoolCd);

        $query .= " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT T1 ";
        $query .= "     INNER JOIN MAX_DATE T2 ";
        $query .= "          ON T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD ";
        $query .= "         AND T1.YEAR         = T2.YEAR ";
        $query .= "         AND T1.FIXED_DATE   = T2.FIXED_DATE ";
        $query .= " WHERE ";
        $query .= "         T1.COURSECD     = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD      = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX          = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.SCHOOL_SORT  = '{$dataVal["SCHOOL_SORT"]}' ";
        $query .= "     AND T1.BUNYA        = '{$e050Val["NAMECD2"]}' ";

        return $query;
    }

    //CSV
    function getSenmonCnt($model, $msVal, $dataVal, $schoolCd) {

        $query  = knje441aQuery::getMaxDateQuery($model, "REPORT_AFT_DISEASE_ADDITION441_SENMON_DAT", $schoolCd);

        $query .= " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT T1 ";
        $query .= "     INNER JOIN MAX_DATE T2 ";
        $query .= "          ON T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD ";
        $query .= "         AND T1.YEAR         = T2.YEAR ";
        $query .= "         AND T1.FIXED_DATE   = T2.FIXED_DATE ";
        $query .= " WHERE ";
        $query .= "         T1.COURSECD     = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD      = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX          = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.SCHOOL_SORT  = '{$dataVal["SCHOOL_SORT"]}' ";

        return $query;
    }

    //CSV
    function getSenmon2Cnt($model, $msVal, $e051Val, $schoolCd) {

        $query  = knje441aQuery::getMaxDateQuery($model, "REPORT_AFT_DISEASE_ADDITION441_SENMON2_DAT", $schoolCd);

        $query .= " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT T1 ";
        $query .= "     INNER JOIN MAX_DATE T2 ";
        $query .= "          ON T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD ";
        $query .= "         AND T1.YEAR         = T2.YEAR ";
        $query .= "         AND T1.FIXED_DATE   = T2.FIXED_DATE ";
        $query .= " WHERE ";
        $query .= "         T1.COURSECD = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD  = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX      = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.BUNYA    = '{$e051Val["NAMECD2"]}' ";

        return $query;
    }
}
?>
