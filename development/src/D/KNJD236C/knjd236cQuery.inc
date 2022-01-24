<?php

require_once('for_php7.php');


class knjd236cQuery extends Query {

    //中高一貫取得
    function getJHFlg()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='Z010' AND ";
        $query .= "     NAMECD2='00' AND ";
        $query .= "     NAMESPARE2 is not null ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年末は、最新の学期取得
    function getMaxSemester()
    {
        $query  = " SELECT ";
        $query .= "     max(SEMESTER) as SEMESTE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学年取得（権限チェック）
    function getGrade($semester)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND GRADE < '11' "; // 11学年以上は表示しない。
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //類型グループ取得
    function getHrClass($semester, $grade)
    {
        $query  = " SELECT ";
        $query .= "     TYPE_GROUP_CD AS VALUE, ";
        $query .= "     TYPE_GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' AND ";
        $query .= "     GRADE='".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
