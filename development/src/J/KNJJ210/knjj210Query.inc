<?php

require_once('for_php7.php');


class knjj210Query extends Query {

    function getGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR='".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER='".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '" .$staffcd ."' ";
            $query .= "      OR TR_CD2 = '" .$staffcd ."' ";
            $query .= "      OR TR_CD3 = '" .$staffcd ."' ";
            $query .= "      OR SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= "      OR SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= "      OR SUBTR_CD3 = '" .$staffcd ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


}
?>
