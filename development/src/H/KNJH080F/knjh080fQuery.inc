<?php

require_once('for_php7.php');

class knjh080fQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model, $semester) {
        $query .= " SELECT ";
        $query .= "     distinct ";
        $query .= "     G1.GRADE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT H1 ";
        $query .= " left join ";
        $query .= "     SCHREG_REGD_GDAT G1 ";
        $query .= "     on H1.YEAR = G1.YEAR ";
        $query .= "     and H1.GRADE = G1.GRADE ";
        $query .= " INNER JOIN ";
        $query .= " 	(SELECT  ";
        $query .= "         NAME2 ";
        $query .= "     FROM  ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'A023' ";
        $query .= "         AND NAME1 = 'H') N1 ";
        $query .= "     ON G1.GRADE = N1.NAME2 ";
        $query .= " WHERE ";
        $query .= "     H1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND H1.SEMESTER = '" . $semester . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;

        return $query;
    }

    //クラス一覧
    function getClass($model, $semester, $grade) {
        $query .= " SELECT ";
        $query .= "     HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND SEMESTER = '" . $semester . "' ";
        $query .= "     AND GRADE = '" . $grade . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
