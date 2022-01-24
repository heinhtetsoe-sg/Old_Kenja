<?php

require_once('for_php7.php');

class knja143vQuery extends Query {

    //学期マスタ
    function getSemeMst($year,$semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $year, $semester) {
        $query  = " SELECT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE,";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
                $query .= "                                     AND GDAT.GRADE = HDAT.GRADE ";
                $query .= "                                     AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
            $query .= "                                     AND GDAT.GRADE = HDAT.GRADE ";
            $query .= "                                     AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '" .$year ."' ";
        $query .= "     AND HDAT.SEMESTER = '".$semester ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND (HDAT.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "       OR HDAT.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "       OR HDAT.TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS ";

        return $query;
    }

    //生徒取得
    function getSchno($model, $year, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD.SCHREGNO || '　' || BASE.NAME AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".$year."' ";
        $query .= "     AND REGD.SEMESTER = '".$semester."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO || '-' || REGD.SCHREGNO ";

        return $query;
    }

}
?>
