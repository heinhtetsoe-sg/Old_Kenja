<?php

require_once('for_php7.php');

class knjc162aQuery extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;

        $query  = " SELECT DISTINCT "; 
        $query .= "     L1.GRADE_NAME1 AS LABEL, ";
        $query .= "     L1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                         AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."'  AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    (TR_CD1      = '" .STAFFCD ."' OR ";
            $query .= "     TR_CD2      = '" .STAFFCD ."' OR ";
            $query .= "     TR_CD3      = '" .STAFFCD ."' OR ";
            $query .= "     SUBTR_CD1   = '" .STAFFCD ."' OR ";
            $query .= "     SUBTR_CD2   = '" .STAFFCD ."' OR ";
            $query .= "     SUBTR_CD3   = '" .STAFFCD ."') AND ";
        }
        $query .= "     L1.SCHOOL_KIND IN ('H', 'J') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrclass($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') AND ";
        }
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
