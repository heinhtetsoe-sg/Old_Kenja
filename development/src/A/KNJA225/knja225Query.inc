<?php

require_once('for_php7.php');

class knja225Query extends Query {
    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "       YEAR='" .CTRL_YEAR ."'";
            $query .= "   AND SEMESTER='{$model->field["GAKKI"] }'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/07
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "       YEAR      = '".CTRL_YEAR."' ";
            $query .= "   AND SEMESTER  = '".$model->field["GAKKI"]."' ";
            $query .= "   AND (TR_CD1   = '".STAFFCD."' ";
            $query .= "   OR  TR_CD2    = '".STAFFCD."' ";
            $query .= "   OR  TR_CD3    = '".STAFFCD."' ";
            $query .= "   OR  SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "   OR  SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "   OR  SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //学期リスト取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     semester, ";
        $query .= "     semestername ";
        $query .= " FROM ";
        $query .= "     semester_mst ";
        $query .= " WHERE ";
        $query .= "       year='".CTRL_YEAR."' ";
        $query .= "   and semester<>'9' ";

        return $query;
    }
}
?>
