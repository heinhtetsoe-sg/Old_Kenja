<?php

require_once('for_php7.php');

class knjc039Query extends Query {
    //年組取得（権限チェック）
    function getGrade($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T1.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT T1 ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     INT(GRADE) < 11 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T1.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT T1 ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     INT(GRADE) < 11 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    //ATTEND_DI_CD_DATから表示する文字を取得する
    function getDiName() {
        $query  = " SELECT ";
        $query .= "     DI_CD, ";
        $query .= "     DI_NAME1 ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND REP_DI_CD IN ('1', '2', '3', '14') ";
        $query .= " ORDER BY ";
        $query .= "     smallint(DI_CD) ";

        return $query;
    }

    function getCheckDate() {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '9' ";
        $query .= "  ";

        return $query;
    }
}
?>
