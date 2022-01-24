<?php

require_once('for_php7.php');


class knjm373eQuery extends Query {

    //学期の取得
    function getSemesterMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year, $semester, $grade)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
            $query .= "AND GRADE='".$grade ."' ";
            $query .= " ORDER BY HR_CLASS ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= "AND GRADE='".$grade ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY HR_CLASS ";
        }

        return $query;
    }

    //生徒取得
    function getGrade($model)
    {
        $query  = " SELECT DISTINCT GRADE ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '" .$model->control["年度"] ."' AND ";
        $query .= "       SEMESTER = '" .$model->control["学期"]. "' ";
        $query .= " ORDER BY GRADE ";

        return $query;
    }
}
?>
