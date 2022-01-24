<?php

require_once('for_php7.php');

class knjd230tQuery extends Query
{
    //学年取得
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND GRADE IN(SELECT  ";
            $query .= "                     GRADE ";
            $query .= "                  FROM ";
            $query .= "                     SCHREG_REGD_HDAT ";
            $query .= "                  WHERE ";
            $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                     (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "                      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "                      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "                      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "                      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "                      SUBTR_CD3 = '".STAFFCD."')) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "          TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "          TR_CD3 = '" .STAFFCD ."' OR ";
            $query .= "          SUBTR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "          SUBTR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "          SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
