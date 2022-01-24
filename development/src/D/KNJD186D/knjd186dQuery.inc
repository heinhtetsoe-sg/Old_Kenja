<?php

require_once('for_php7.php');

class knjd186dQuery extends Query {
    //学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得
    function gethrName($model) {
            $query .= " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL, ";
            $query .= "     REGD_H.TR_CD1 ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "         REGD_H.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND REGD_H.SEMESTER = '{$model->field["SEMESTER"]}' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                             REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }

        return $query;
    }

    //対象者リスト
    function getSchno($year, $semester, $grade_hr_class) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2  ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR                 = '{$year}' ";
        $query .= "     AND T2.SEMESTER             = '{$semester}' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '{$grade_hr_class}' ";
        $query .= "     AND T2.SCHREGNO             = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //SDATE取得
    function getSdate() {
        $query  = " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '1' ";

        return $query;
    }

    //学期数カウント
    function getSemestercnt() {
        $query  = " SELECT" ;
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }
}
?>
