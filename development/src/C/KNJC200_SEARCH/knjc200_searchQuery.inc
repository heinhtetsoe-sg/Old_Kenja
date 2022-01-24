<?php

require_once('for_php7.php');

class knjc200_searchQuery extends Query {
    //年組
    function getHrName($model) {
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "       T1.HR_NAME AS LABEL ";
        $query .= "FROM   SCHREG_REGD_HDAT T1 ";
        $query .= "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
        $query .= "WHERE  T1.YEAR='".CTRL_YEAR."' AND T1.SEMESTER='".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "ORDER BY T1.GRADE, T1.HR_CLASS ";
        return $query;
    }
    //検索結果
    function getList($model) {
        $query  = "SELECT T2.GRADE || T2.HR_CLASS AS GRADE_HR_CLASS";
        $query .= "      ,T2.GRADE";
        $query .= "      ,T2.HR_CLASS";
        $query .= "      ,T2.ATTENDNO";
        $query .= "      ,T1.NAME";
        $query .= "      ,T1.NAME_KANA";
        $query .= "      ,T1.SEX";
        $query .= "      ,T4.NAME2 AS SEX_NAME";
        $query .= "      ,T1.SCHREGNO";
        $query .= "      ,T3.HR_NAME";
        $query .= "  FROM SCHREG_REGD_DAT T2";
        $query .= "       INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "           AND T2.SEMESTER   = T3.SEMESTER ";
        $query .= "           AND T2.GRADE      = T3.GRADE";
        $query .= "           AND T2.HR_CLASS   = T3.HR_CLASS";
        $query .= "       INNER JOIN SCHREG_BASE_MST T1 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "       LEFT JOIN NAME_MST T4 ON NAMECD1 = 'Z002' AND T4.NAMECD2 = T1.SEX";
        $query .= "       LEFT JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T2.YEAR AND T5.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "       T1.SCHREGNO   = T2.SCHREGNO";
        $query .= "   AND T2.YEAR       = '".CTRL_YEAR."'";
        $query .= "   AND T2.SEMESTER   = '".CTRL_SEMESTER."'";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        if (strlen($model->field["GRADE_HR_CLASS"])) {
            $query .= "   AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."'";
        }
        if (strlen($model->field["NAME"])) {
            $query .= "   AND T1.NAME       LIKE '%".$model->field["NAME"]."%'";
        }
        if (strlen($model->field["NAME_KANA"])) {
            $query .= "   AND T1.NAME_KANA  LIKE '%".$model->field["NAME_KANA"]."%'";
        }
        $query .= " ORDER BY T2.GRADE,T2.HR_CLASS,T2.ATTENDNO";

        return $query;
    }
}
?>
