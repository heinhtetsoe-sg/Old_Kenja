<?php

require_once('for_php7.php');

class knjb225Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学籍在籍データの件数取得
    function checkRegdDat() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学年・コース取得
   function getGradeCouse($model, $flg) {
        if ($flg) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.GRADE_NAME1, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSENAME, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T2.COURSECODE, ";
            $query .= "     T2.COURSECODENAME ";
            $query .= " FROM ";
            $query .= "     V_COURSE_MAJOR_MST T1, ";
            $query .= "     V_COURSECODE_MST T2, ";
            $query .= "     SCHREG_REGD_GDAT T3 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.YEAR = T3.YEAR AND ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= " ORDER BY ";
            $query .= "     T3.GRADE, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T2.COURSECODE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE, ";
            $query .= "     T4.GRADE_NAME1, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T2.COURSENAME, ";
            $query .= "     T2.MAJORNAME, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     T3.COURSECODENAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= "     V_COURSE_MAJOR_MST T2, ";
            $query .= "     V_COURSECODE_MST T3, ";
            $query .= "     SCHREG_REGD_GDAT T4 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = T2.YEAR AND ";
            $query .= "     T1.YEAR         = T3.YEAR AND ";
            $query .= "     T1.YEAR         = T4.YEAR AND ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     T1.COURSECD     = T2.COURSECD AND ";
            $query .= "     T1.MAJORCD      = T2.MAJORCD AND ";
            $query .= "     T1.COURSECODE   = T3.COURSECODE AND ";
            $query .= "     T1.GRADE        = T4.GRADE  ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE ";
        }

        return $query;
    }
}
?>
