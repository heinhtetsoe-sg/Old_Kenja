<?php

require_once('for_php7.php');

class knjl301rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCsvQuery($year, $model)
    {
        $query  = " SELECT ";
        $query .= "     NML004.NAME1 AS TESTDIV_NAME, ";
        $query .= "     T1.EXAMHALL_NAME, ";
        $query .= "     T2.EXAMNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = T2.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST NML004 ON NML004.NAMECD1 = 'L004' ";
        $query .= "         AND NML004.NAMECD2 = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD, ";
        $query .= "     T2.RECEPTNO ";

        return $query;
    }


}
?>
