<?php
class knjl412iQuery extends Query {
    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分
    function getTestDiv($model) {
        $query  = "     SELECT ";
        $query .= "       TESTDIV AS VALUE, ";
        $query .= "       TESTDIV || ':' || TESTDIV_ABBV AS LABEL";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_TESTDIV_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

}
?>
