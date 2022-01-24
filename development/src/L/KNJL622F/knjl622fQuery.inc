<?php

require_once('for_php7.php');

class knjl622fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003"){
            $query .= "     AND NAMECD2 = '2' ";
        } else if ($namecd1 == "L004"){
            $query .= "     AND NAMECD2 <> '6' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getExamcourse($year, $applicantdiv, $testdiv, $examcoursecd = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";//固定
        if (strlen($examcoursecd)) {
            $query .= "     AND T1.EXAMCOURSECD {$examcoursecd} ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //受験科目数
    function getExamType($year, $applicantdiv, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             MEMO_KISO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_NO_DAT ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR  = '{$year}' ";
        $query .= "             AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "             AND TESTDIV      = '{$testdiv}' ";
        $query .= "         GROUP BY ";
        $query .= "             MEMO_KISO ";
        $query .= "     ) ";
        return $query;
    }

    function getFSCourse($year) {
        $query .= " SELECT ";
        $query .= "     T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECODE || ':' || T1.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSECODE_YDAT T2 ";
        $query .= "     LEFT JOIN COURSECODE_MST T1 ";
        $query .= "       ON T1.COURSECODE = T2.COURSECODE ";
        $query .= " WHERE ";
        //$query .= "     T1.COURSECODE LIKE '1%' ";
        $query .= "     T2.YEAR = '".$year."' ";
        $query .= "     AND T1.COURSECODE IN ('1002', '1003') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        $query .= "  ";
        return $query;
    }
}
?>
