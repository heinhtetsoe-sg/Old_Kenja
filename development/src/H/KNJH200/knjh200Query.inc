<?php

require_once('for_php7.php');

class knjh200Query extends Query {
    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REG_G ";
        $query .= "          ON T1.YEAR     = REG_G.YEAR ";
        $query .= "         AND T1.GRADE    = REG_G.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T1.YEAR     = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.GRADE    = T3.GRADE ";
        $query .= "         AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //CSV出力データ取得
    function getSelectQuery($model) {
        if ($model->field["SELECT_DIV"] == "2") {
            $array = array();
            foreach (explode(',', $model->selectdata) as $val) {
                list ($gha, $schregno) = explode("-", $val);
                $array[] = $schregno;
            }
        }

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.SEX ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        if ($model->field["SELECT_DIV"] == "2") {
            $query .= "     T1.SCHREGNO IN ('".implode("','", $array)."') ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode("','", explode(",", $model->selectdata))."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
?>
