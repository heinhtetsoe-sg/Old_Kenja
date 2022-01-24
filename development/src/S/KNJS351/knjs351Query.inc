<?php

require_once('for_php7.php');

class knjs351Query extends Query {
    //学期取得
    function getSemester($model) {
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

    //年組取得
    function getGradeHrClass($model) {
        $query = "";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                     MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR         = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER     = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS     = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND  = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD      = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } else if ($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     GHR_CD AS VALUE, ";
                $query .= "     GHR_NAME AS LABEL ";
            } else {
                $query .= "     GRADE || HR_CLASS AS VALUE, ";
                $query .= "     HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT ";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT ";
            }
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";
            if ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     REGD_G.GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                 WHERE ";
                    $query .= "                     REGD_G.YEAR         = '".CTRL_YEAR."' AND ";
                    $query .= "                     REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     REGD_G.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT REGD_G ";
                $query .= "                 WHERE ";
                $query .= "                     REGD_G.YEAR         = '".CTRL_YEAR."' AND ";
                $query .= "                     REGD_G.SCHOOL_KIND  = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
                $query .= "          TR_CD2     = '".STAFFCD."' OR ";
                $query .= "          TR_CD3     = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR           = '".CTRL_YEAR."' ";
            $query .= "     AND REGD_H.SEMESTER   = '".$model->field["SEMESTER"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                              REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                              REGD_G.YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                              REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                              REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
                $query .= "          TR_CD2     = '".STAFFCD."' OR ";
                $query .= "          TR_CD3     = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }
}
?>
