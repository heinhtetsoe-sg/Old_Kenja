<?php

require_once('for_php7.php');


class knjj140Query extends Query {

    function getHr_Class_alp($model, $year,$semester,$auth_user,$staffcd,$auth)
    {
        $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='" .$year ."' ";
        $query .= "AND SEMESTER='".$semester ."' ";
        if( $auth == "1" ){
        //参照・更新可（制限付き）
            if( $auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT ){
                $query .= " AND (TR_CD1 = '" .$staffcd ."' ";
                $query .= "    OR TR_CD2 = '" .$staffcd ."' ";
                $query .= "    OR TR_CD3 = '" .$staffcd ."' ";
                $query .= " OR SUBTR_CD1 = '" .$staffcd ."' ";//2005.10.13
                $query .= "    OR SUBTR_CD2 = '" .$staffcd ."' ";//2005.10.13
                $query .= "    OR SUBTR_CD3 = '" .$staffcd ."') ";//2005.10.13
            }
        }

        return $query;
    }

}
?>
