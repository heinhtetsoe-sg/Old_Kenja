<?php

require_once('for_php7.php');


class knja223bQuery extends Query {
    //学校名取得の取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            if ($model->field["HR_CLASS_TYPE"] == "2") {
                $query .= "FROM SCHREG_REGD_FI_HDAT T1 ";
            } else {
                $query .= "FROM SCHREG_REGD_HDAT T1 ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            if ($model->field["HR_CLASS_TYPE"] == "2") {
                $query .= "FROM SCHREG_REGD_FI_HDAT T1 ";
            } else {
                $query .= "FROM SCHREG_REGD_HDAT T1 ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."' ";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }
    //駒澤大判定用
    function checkKoma() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     UPPER(NAME1) IN ('KOMA') ";

        return $query;
    }
}
?>