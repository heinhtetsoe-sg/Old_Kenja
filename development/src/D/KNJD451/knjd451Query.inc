<?php

require_once('for_php7.php');

class knjd451Query extends Query {
    //学期
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
        
        return $query;
    }

    //学年コンボ
    function GetGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudents($model, $flg="") {
        $query  = " SELECT  ";
        $query .= "     T1.SCHREGNO,  ";
        $query .= "     T1.GRADE,  ";
        $query .= "     T1.HR_CLASS,  ";
        $query .= "     T1.ATTENDNO,  ";
        $query .= "     T2.HR_NAME,  ";
        $query .= "     T3.NAME  ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST L1 ON L1.YEAR = T1.YEAR AND L1.SCHREGNO = T1.SCHREGNO AND L1.BASE_SEQ = '004'  ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'A025' ";
        $query .= "                           AND N1.NAMECD2 = L1.BASE_REMARK1 ";
        $query .= "                           AND N1.NAMESPARE2 = '1' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS  ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND T2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " UNION ";
        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO,  ";
        $query .= "     T1.GRADE,  ";
        $query .= "     T1.HR_CLASS,  ";
        $query .= "     T1.ATTENDNO,  ";
        $query .= "     T2.HR_NAME,  ";
        $query .= "     T3.NAME  ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS  ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'A025' ";
        $query .= "                           AND N1.NAMECD2 = T3.HANDICAP ";
        $query .= "                           AND N1.NAMESPARE2 = '1' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     L1.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_BASE_YEAR_DETAIL_MST L1 ";
        $query .= "                 WHERE ";
        $query .= "                         L1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "                     AND L1.BASE_SEQ = '004' ";
        $query .= "                 ) TAISHOUGAI ON TAISHOUGAI.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND T2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " AND TAISHOUGAI.SCHREGNO IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";
        
        return $query;
    }

    //出力対象年度取得
    function getYearList() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
}
?>
