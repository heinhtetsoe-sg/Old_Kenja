<?php

require_once('for_php7.php');

class knje351Query extends Query {

    //年度取得
    function getYear()
    {
        $query  = "SELECT ";
        $query .= "    YEAR AS LABEL, ";
        $query .= "    YEAR AS VALUE ";
        $query .= "FROM ";
        $query .= "    AFT_GRAD_COURSE_DAT ";
        $query .= "GROUP BY ";
        $query .= "    YEAR ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["YEAR"]."' ";
        $query .= "    AND SEMESTER < '9' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getClass($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$model->field["YEAR"]."'";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$model->field["YEAR"]."'";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
