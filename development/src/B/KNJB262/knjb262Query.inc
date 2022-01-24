<?php

require_once('for_php7.php');


class knjb262Query extends Query {

    //学期取得
    function getSemester()
    {
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

    //学年取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('H','J') ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND GRADE IN ( ";
            $query .= "         SELECT DISTINCT  ";
            $query .= "             GRADE ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_HDAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR='".CTRL_YEAR."' AND ";
            $query .= "             SEMESTER='".$model->field["SEMESTER"]."' AND ";
            $query .= "            (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "             TR_CD2 = '".STAFFCD."' OR ";
            $query .= "             TR_CD3 = '".STAFFCD."')) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期日付範囲取得
    function getSemeDate($model)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER='".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //履修履歴
    function getRirekiCode($model)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
}
?>
