<?php

require_once('for_php7.php');


class knjl132fQuery extends Query {

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
        $query .= " WITH RECEPT_MAX AS ( ";
        $query .= " SELECT ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     MAX(RECEPT.RECEPTNO) AS RECEPTNO ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.JUDGEDIV = '1' ";
        $query .= " GROUP BY ";
        $query .= "     RECEPT.EXAMNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     BASE.FS_CD AS VALUE, ";
        $query .= "     BASE.FS_CD || ':' || FSCHOOL.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN RECEPT_MAX ON BASE.EXAMNO = RECEPT_MAX.EXAMNO ";
        $query .= "     INNER JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ";
        $query .= "     INNER JOIN ENTEXAM_MONEY_DAT MONEY_D ON BASE.ENTEXAMYEAR = MONEY_D.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = MONEY_D.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = MONEY_D.EXAMNO ";
        $query .= "           AND MONEY_D.ENT_PAY_DATE IS NOT NULL ";
        $query .= "           AND MONEY_D.ENT_PAY_MONEY IS NOT NULL ";
        $query .= "           AND MONEY_D.EXP_PAY_DATE IS NOT NULL ";
        $query .= "           AND MONEY_D.EXP_PAY_MONEY IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.JUDGEMENT = '1' ";
        $query .= "     AND BASE.ENTDIV = '1' ";
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
