<?php

require_once('for_php7.php');

class knjxattend_htrainremarkQuery extends Query {
    //�N�x�R���{
    function getYear($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR AS LABEL, ";
        $query .= "     T1.YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "   AND T1.ANNUAL = T2.GRADE ";
            $query .= "   AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "       T1.SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //���O
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //�o�����l
    function getRemark($schregno, $year, $output_field) {
        $query  = " SELECT ";
        $query .= "     {$output_field} ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$schregno}' ";

        return $query;
    }
}
?>
