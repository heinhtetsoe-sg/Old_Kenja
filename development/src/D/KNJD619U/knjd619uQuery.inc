<?php

require_once('for_php7.php');

class knjd619uQuery extends Query {

    //学年・コース取得
    function getGradeCourseList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '_' || T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.COURSECODE AS VALUE, ";
        $query .= "     S2.GRADE_NAME1, ";
        $query .= "     S3.MAJORNAME, ";
        $query .= "     S4.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.SEMESTER     = S1.SEMESTER ";
        $query .= "         AND T1.GRADE        = S1.GRADE ";
        $query .= "         AND T1.HR_CLASS     = S1.HR_CLASS ";
        //権限（制限付）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "         AND '".STAFFCD."' IN (S1.TR_CD1, S1.TR_CD2, S1.TR_CD3, S1.SUBTR_CD1, S1.SUBTR_CD2, S1.SUBTR_CD3) ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
        $query .= "          ON T1.YEAR         = S2.YEAR ";
        $query .= "         AND T1.GRADE        = S2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND S2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S3 ";
        $query .= "          ON T1.YEAR         = S3.YEAR ";
        $query .= "         AND T1.COURSECD     = S3.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S3.MAJORCD ";
        $query .= "     INNER JOIN V_COURSECODE_MST S4 ";
        $query .= "          ON T1.YEAR         = S4.YEAR ";
        $query .= "         AND T1.COURSECODE   = S4.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
