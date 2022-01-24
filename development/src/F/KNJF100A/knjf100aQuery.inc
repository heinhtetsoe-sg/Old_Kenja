<?php

require_once('for_php7.php');

class knjf100aQuery extends Query {
    //学期取得
    function getSelectSeme() {
        $query  = " SELECT DISTINCT SEMESTER,SEMESTERNAME FROM SEMESTER_MST ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY SEMESTER";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }
}
?>
