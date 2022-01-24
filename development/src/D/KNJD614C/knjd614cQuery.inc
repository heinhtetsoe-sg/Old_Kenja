<?php

require_once('for_php7.php');

class knjd614cQuery extends Query {
    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT DISTINCT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            //$query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND GRADE    = '".$model->field["GRADE"]."'";
            $query .= "     AND GRADE IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND SCHOOL_KIND = 'H')";
            $query .= " ORDER BY GRADE || HR_CLASS ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT DISTINCT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR       = '".$model->control["年度"]."' ";
            //$query .= "     AND SEMESTER   = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND GRADE      = '".$model->field["GRADE"]."' ";
            $query .= "     AND (  TR_CD1  = '".STAFFCD."' ";
            $query .= "         OR TR_CD2  = '".STAFFCD."' ";
            $query .= "         OR TR_CD3  = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD1  = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD2  = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD3  = '".STAFFCD."') ";
            $query .= "     AND GRADE IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND SCHOOL_KIND = 'H')";
            $query .= " ORDER BY GRADE || HR_CLASS ";
        }

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        //$semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
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
            //$query .= "     T1.SEMESTER = '{$semester}' AND ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' ";
            $query .= "     ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
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
            //$query .= "     T1.SEMESTER = '{$semester}' AND ";
            $query .= "     ( ";
            $query .= "     TR_CD1    = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= "     OR SUBTR_CD1  = '".STAFFCD."' ";
            $query .= "     OR SUBTR_CD2  = '".STAFFCD."' ";
            $query .= "     OR SUBTR_CD3  = '".STAFFCD."') AND  ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' ";
            $query .= "     ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }

        return $query;
    }

}
?>