<?php

require_once('for_php7.php');

class knje040Query extends Query {

    //左側のlistを作るためのデータを取得
    function getlist($row_year, $row_semester, $model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     D.GRADE, ";
        $query .= "     D.HR_CLASS, ";
        $query .= "     D.ATTENDNO, ";
        $query .= "     M.NAME,";
        $query .= "     M.NAME_KANA AS KANA, ";
        $query .= "     D.YEAR, ";
        $query .= "     M.SCHREGNO, ";
        $query .= "     M.ENT_DATE, ";
        $query .= "     L1.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST M, ";
        $query .= "     SCHREG_REGD_DAT D ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON D.YEAR = REG_G.YEAR ";
                $query .= "           AND D.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON D.YEAR = REG_G.YEAR ";
            $query .= "           AND D.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L1 ";
        $query .= "         ON  L1.YEAR     = D.YEAR ";
        $query .= "         AND L1.SEMESTER = D.SEMESTER ";
        $query .= "         AND L1.GRADE    = D.GRADE ";
        $query .= "         AND L1.HR_CLASS = D.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         D.SCHREGNO = M.SCHREGNO ";
        $query .= "     AND D.YEAR     = '{$row_year}'";
        $query .= "     AND D.SEMESTER = '{$row_semester}'";
        $query .= "     AND M.ENT_DIV IN ('4','5','7') ";
        $query .= " ORDER BY ";
        $query .= "     D.GRADE, ";
        $query .= "     D.HR_CLASS, ";
        $query .= "     D.ATTENDNO ";

        return $query;
    }
}
?>
