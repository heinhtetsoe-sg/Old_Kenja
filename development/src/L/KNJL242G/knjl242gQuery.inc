<?php

require_once('for_php7.php');


class knjl242gQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //出身学校
    function getGradeHrclass($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     BASE.FS_CD AS VALUE, ";
        $query .= "     BASE.FS_CD || ':' || FSCHOOL.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.JUDGEMENT = '1' ";
        $query .= "     AND BASE.ENTDIV = '1' ";
        $query .= "     AND BASE.PROCEDUREDIV = '1' ";
        $query .= "     AND BASE.EXAMNO NOT IN ( ";
        $query .= "                 SELECT ";
        $query .= "                     EXAMNO ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PRINT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "                     AND GET_YOUROKU = '1' ";
        $query .= "                     AND GET_MEDEXAM = '1' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
