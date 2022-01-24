<?php

require_once('for_php7.php');


class knjp974Query extends Query {

    //年度
    function getYear()
    {
        $query .= " WITH YEAR_T(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."年度', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."年度', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     YEAR_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSemesterMst($model) {
        $query   = " SELECT ";
        $query  .= "   SDATE, ";
        $query  .= "   EDATE ";
        $query  .= " FROM ";
        $query  .= "   SEMESTER_MST ";
        $query  .= " WHERE ";
        $query  .= "   YEAR = '".$model->field["YEAR"]."' ";
        $query  .= "   AND SEMESTER = '9' ";

        return $query;
    }





}
?>
