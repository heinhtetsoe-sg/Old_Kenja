<?php

require_once('for_php7.php');

class knjl051fQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getExamcourse($year, $applicantdiv) {
        if ("2" == $applicantdiv) {
            $query  = " SELECT DISTINCT ";
            $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS VALUE, ";
            $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_COURSE_MST ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '{$year}' AND ";
            $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
            $query .= "     TESTDIV         = '1' ";
            if ($examcourse) {
                $query .= "     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$examcourse}' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS VALUE, ";
            $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_COURSE_MST ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR  = '{$year}' AND ";
            $query .= "     APPLICANTDIV = '{$applicantdiv}' AND ";
            $query .= "     TESTDIV      = '1' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試回数取得
    function getTestdiv0($year, $testdiv) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分(中学用)
    function getTestdivL024($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L024' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             TESTDIV ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND EXAM_TYPE IN ('6','7','8') "; //受験型　「6:グローバル」「7:スポーツ」「8:サイエンス」
        $query .= "         GROUP BY ";
        $query .= "             TESTDIV ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     smallint(NAMECD2) "; //入試区分2桁対応
        return $query;
    }

    //受験型(中学用)
    function getExamType($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 IN ('6','7','8') "; //受験型　「6:グローバル」「7:スポーツ」「8:サイエンス」
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
