<?php

require_once('for_php7.php');

class knjc170aQuery extends Query {
    //学年取得
    function getGrade() {
        $query  = "    SELECT DISTINCT ";
        $query .= "      GRADE_NAME1 AS LABEL, ";
        $query .= "      GRADE AS VALUE";
        $query .= "    FROM ";
        $query .= "      SCHREG_REGD_GDAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        //校種対応
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND IN ('".SCHOOLKIND."') ";
        }

        return $query;
    }

    //クラス一覧取得
    function getHrClass($model) {
        $query  ="    SELECT ";
        $query .="      HR_NAME AS LABEL, ";
        $query .="      GRADE || '-' || HR_CLASS AS VALUE ";
        $query .="    FROM ";
        $query .="      SCHREG_REGD_HDAT ";
        $query .="    WHERE ";
        $query .="      YEAR = '".CTRL_YEAR."' ";
        $query .="      AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="      AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

}
?>
