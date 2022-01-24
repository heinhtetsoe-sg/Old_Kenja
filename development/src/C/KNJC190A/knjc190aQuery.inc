<?php

require_once('for_php7.php');

class knjc190aQuery extends Query {
    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND G1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得（権限チェック）
    function getSubclasscd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '　' || VALUE(T3.SUBCLASSNAME, '') AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || '　' || VALUE(T3.SUBCLASSNAME, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STD_DAT T4, ";
        $query .= "     SCHREG_REGD_DAT T5 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "     T5.SCHREGNO     = T4.SCHREGNO AND ";
        $query .= "     T5.YEAR         = T4.YEAR AND ";
        $query .= "     T5.SEMESTER     = T4.SEMESTER AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     ((T2.STAFFCD = '".STAFFCD."') ";
            $query .= "         OR (T5.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                  ST.STAFFCD = '".STAFFCD."' AND ";
            $query .= "                  ST.STAFF_SEQ IN ('005', '006', '007') AND ";
            $query .= "                  ST.FIELD1  = '0200')) ";
            $query .= "         OR (T1.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                  ST.STAFFCD = '".STAFFCD."' AND ";
            $query .= "                  ST.STAFF_SEQ IN ('005', '006', '007') AND ";
            $query .= "                  ST.FIELD1  = '1050')) ";
            $query .= "         ) AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD          = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD   = T3.SUBCLASSCD AND ";
        $query .= "     T1.YEAR         = T4.YEAR AND ";
        $query .= "     T1.SEMESTER     = T4.SEMESTER AND ";
        $query .= "     T1.CHAIRCD      = T4.CHAIRCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T5.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "                 ) ";
        }
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";

        return $query;
    }
}
?>
