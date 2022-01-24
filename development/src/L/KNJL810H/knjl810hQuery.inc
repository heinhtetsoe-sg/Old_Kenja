<?php
class knjl810hQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
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

    //入試区分取得
    function getTestDiv($year, $applicantDiv, $testDiv="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($testDiv) {
            $query .= " AND TESTDIV = '{$testDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    function getKindDiv($year, $applicantDiv, $testDiv="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     KINDDIV_NAME, ";
        $query .= "     KINDDIV || ':' || KINDDIV_NAME AS LABEL, ";
        $query .= "     KINDDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND KINDDIV = '{$testDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    function getHallList($year, $applicantDiv, $testDiv, $examType="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMHALLCD || ':' || T2.EXAMHALL_NAME AS LABEL, ";
        $query .= "     T1.EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_LIST_YDAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT T2 ";
        $query .= "            ON T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "           AND T1.TESTDIV      = T2.TESTDIV ";
        $query .= "           AND T1.EXAM_TYPE    = T2.EXAM_TYPE ";
        $query .= "           AND T1.EXAMHALLCD   = T2.EXAMHALLCD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testDiv}' ";
        if($examType != "ALL"){
          $query .= "   AND T1.EXAM_TYPE    = '{$examType}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
