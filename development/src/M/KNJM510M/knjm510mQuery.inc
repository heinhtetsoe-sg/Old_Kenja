<?php

require_once('for_php7.php');

class knjm510mQuery extends Query {

    //年組取得（権限チェック）
    function getClass()
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR='" .CTRL_YEAR."'";
        $query .= "AND SEMESTER='".CTRL_SEMESTER."'";

        return $query;
    }

    //クラスデータ取得
    function getSch($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO,t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND t1.SEMESTER = '" .CTRL_SEMESTER."' ";
        $query .= "     AND t1.GRADE || t1.HR_CLASS = '" .$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY t1.ATTENDNO ";

        return $query;
    }

}
?>
