<?php

require_once('for_php7.php');

class knja042query extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA042' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学年取得
    function getGrade($model, $grade="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->examyear."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     VALUE(T1.REMAINGRADE_FLG, '0') != '1' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($grade) {
            $query .= " AND T1.GRADE    = '".$grade."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //実行
    function selectCvsQuery($model)
    {
        $query  = " SELECT ";
        $query .= "    t1.YEAR, ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.SEMESTER, ";
        $query .= "    CASE WHEN t2.NAME is null OR t2.NAME = '' THEN t3.NAME ELSE t2.NAME END AS NAME, ";
        $query .= "    CASE WHEN t2.NAME_KANA is null OR t2.NAME_KANA = '' THEN t3.NAME_KANA ELSE t2.NAME_KANA END AS NAME_KANA, ";
        $query .= "    CASE WHEN t2.SEX is null OR t2.SEX = '' THEN t3.SEX ELSE t2.SEX END AS SEX, ";
        $query .= "    t1.GRADE, ";
        $query .= "    t1.HR_CLASS, ";
        $query .= "    t1.ATTENDNO, ";
        $query .= "    t1.COURSECD, ";
        $query .= "    t1.MAJORCD, ";
        $query .= "    t1.COURSECODE, ";
        $query .= "    t1.REMAINGRADE_FLG, ";
        $query .= "    t1.OLD_SCHREGNO, ";
        $query .= "    t1.OLD_GRADE, ";
        $query .= "    t1.OLD_HR_CLASS, ";
        $query .= "    t1.OLD_ATTENDNO, ";
        $query .= "    t1.SCORE ";
        $query .= " FROM ";
        $query .= "    CLASS_FORMATION_DAT t1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST t2 ON t2.SCHREGNO = t1.SCHREGNO ";
        $query .= "    LEFT JOIN FRESHMAN_DAT t3 ON t3.SCHREGNO = t1.SCHREGNO ";
        $query .= "    AND t3.ENTERYEAR = t1.YEAR ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ";
                $query .= "   AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ";
            $query .= "   AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".$model->examyear."' ";
        $query .= "    AND t1.SEMESTER = '1' ";
        if ($model->grade != "99"){
            $query .= "    AND t1.GRADE = '".$model->grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    t1.GRADE,t1.HR_CLASS,t1.ATTENDNO ";

        return $query;
    }
}
?>
