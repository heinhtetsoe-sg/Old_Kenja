<?php

require_once('for_php7.php');
class knjx_d132aQuery extends Query {

    //学年名取得
    function getGradeName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($nameCd1, $nameCd2) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= "   , NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$nameCd1}' ";
        if ($nameCd2 != "") {
            $query .= " AND NAMECD2 = '{$nameCd2}' ";
        }
        return $query;
    }

    //定型分マスター一覧取得
    function getHtrainRemarkTempDat($model) {
        if ($model->Properties["KNJD133J_semesCombo"] == "1") {
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".$model->exp_year."' ";
            $query .= "     AND SEMESTER = '9' ";
            $query .= "     AND GRADE    = '".$model->grade."' ";
            $query .= "     AND DATA_DIV = '".$model->dataDiv."' ";
            $query .= " ORDER BY ";
            $query .= "     PATTERN_CD ";
        } else {
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     HTRAINREMARK_TEMP_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$model->exp_year."' AND ";
            $query .= "     GRADE       = '".$model->grade."' AND ";
            $query .= "     DATA_DIV    = '".$model->dataDiv."' ";
            $query .= " ORDER BY ";
            $query .= "     PATTERN_CD ";
        }
        return $query;
    }


}
?>
