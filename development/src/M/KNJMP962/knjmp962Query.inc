<?php

require_once('for_php7.php');

class knjmp962Query extends Query {

    //学期リスト取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";

        return $query;
    }

    //学年
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
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
            $query .= "       YEAR = '" .CTRL_YEAR ."'";
            $query .= "   AND SEMESTER = '{$model->field["SEMESTER"] }'";
            $query .= "   AND GRADE = '{$model->field["GRADE"] }'";
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
            $query .= "   AND SEMESTER  = '".$model->field["SEMESTER"]."' ";
            $query .= "   AND GRADE     = '{$model->field["GRADE"] }'";
            $query .= "   AND (TR_CD1   = '".STAFFCD."' ";
            $query .= "   OR  TR_CD2    = '".STAFFCD."' ";
            $query .= "   OR  TR_CD3    = '".STAFFCD."' ";
            $query .= "   OR  SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "   OR  SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "   OR  SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //月
    function getMonth($model)
    {
        $query  = " WITH MONTH_T(LABEL, VALUE, SORT) AS ( ";
        $setUnion = "";
        $sort = 0;
        foreach ($model->month as $value => $label) {
            $query .= $setUnion."     VALUES('{$label}', '{$value}', {$sort}) ";
            $setUnion = " UNION ";
            $sort++;
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL, ";
        $query .= "     SORT ";
        $query .= " FROM ";
        $query .= "     MONTH_T ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }
}
?>
