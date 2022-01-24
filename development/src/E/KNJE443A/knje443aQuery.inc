<?php

require_once('for_php7.php');

class knje443aQuery extends Query {
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
        $query .= "     T1.REQUEST_ANSWER_PRG = 'KNJE443'  ";
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
        $query .= "                                     N1.REQUEST_ANSWER_PRG = 'KNJE443' ";
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

    //CSV出力データ取得
    function getCsvQuery($model, $schoolCd) {

        $query  = " WITH MAX_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EDBOARD_SCHOOLCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.FIXED_DATE ";
        $query .= "     FROM ";
        $query .= "         REPORT_AFT_DISEASE_ADDITION443_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(EXECUTE_DATE) AS EXECUTE_DATE ";
        $query .= "         FROM ";
        $query .= "             REPORT_AFT_DISEASE_ADDITION443_DAT ";
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
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.SCHOOL_GROUP, ";
        $query .= "         T1.SCHOOL_CD, ";
        $query .= "         SUM(CASE WHEN T1.SEQ = '01' THEN T1.INT_VAL1 ELSE 0 END) AS GOUKAKU_G, ";
        $query .= "         SUM(CASE WHEN T1.SEQ = '02' THEN T1.INT_VAL1 ELSE 0 END) AS GOUKAKU_K, ";
        $query .= "         SUM(T1.INT_VAL1) AS GOUKAKU_T, ";
        $query .= "         SUM(CASE WHEN T1.SEQ = '01' THEN T1.INT_VAL2 ELSE 0 END) AS NAITEI_G, ";
        $query .= "         SUM(CASE WHEN T1.SEQ = '02' THEN T1.INT_VAL2 ELSE 0 END) AS NAITEI_K, ";
        $query .= "         SUM(T1.INT_VAL2) AS NAITEI_T ";
        $query .= "     FROM ";
        $query .= "         AFT_DISEASE_ADDITION443_FIXED_DAT T1 ";
        $query .= "         INNER JOIN MAX_DATE T2 ";
        $query .= "              ON T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD ";
        $query .= "             AND T1.YEAR         = T2.YEAR ";
        $query .= "             AND T1.FIXED_DATE   = T2.FIXED_DATE ";
        $query .= "     GROUP BY ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.SCHOOL_GROUP, ";
        $query .= "         T1.SCHOOL_CD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.COURSENAME, ";
        $query .= "     L2.MAJORNAME, ";
        $query .= "     L3.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "     L4.SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON T1.COURSECD = L1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ON T1.COURSECD = L2.COURSECD AND T1.MAJORCD = L2.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'E012' AND T1.SCHOOL_GROUP = L3.NAMECD2 ";
        $query .= "     LEFT JOIN COLLEGE_MST L4 ON T1.SCHOOL_CD = L4.SCHOOL_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T1.SCHOOL_CD ";

        return $query;
    }
}
?>
