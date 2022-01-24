<?php

require_once('for_php7.php');

class knja143tQuery extends Query {
    //学期マスタ
    function getSemeMst($year, $semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";

        return $query;
    }

    //学年取得
    function getGrade($model, $year, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.GRADE VALUE,";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON HDAT.YEAR  = GDAT.YEAR ";
        $query .= "                                    AND HDAT.GRADE = GDAT.GRADE ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '{$year}' ";
        $query .= "     AND HDAT.SEMESTER = '{$semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND (HDAT.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "       OR HDAT.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "       OR HDAT.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $year, $semester) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE,";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "       OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "       OR T1.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getSchno($model, $year, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCHREGNO || '　' || T2.NAME AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //GEADECD取得
    function getGradeCd($grade) {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }
}
?>

