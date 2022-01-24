<?php

require_once('for_php7.php');

class knjd616iQuery extends Query {

    //学期
    function getSemester() {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER ";
        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model, $div = "") {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($div == "GRADE") {
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if ($model->field["PRINT_DIV"] == "1") {
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND ((TR_CD1     = '".STAFFCD."' OR ";
                $query .= "          TR_CD2     = '".STAFFCD."' OR ";
                $query .= "          TR_CD3     = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
                $query .= "         OR (T1.GRADE IN ";
                $query .= "             (SELECT ";
                $query .= "                  FIELD2 ";
                $query .= "              FROM ";
                $query .= "                  STAFF_DETAIL_MST ST ";
                $query .= "              WHERE ";
                $query .= "                  T1.YEAR = ST.YEAR ";
                $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
                $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
                $query .= "                  AND ST.FIELD1 = '0200')) ";
                $query .= "         ) ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        if ($model->field["PRINT_DIV"] == "2") {
            $query .= " AND EXISTS ( ";
            $query .= " SELECT 'X' ";
            $query .= " FROM ";
            $query .= "     V_SUBCLASS_MST SUBM ";
            $query .= "     LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' ";
            $query .= "         AND NMA023.NAME1 = SUBM.SCHOOL_KIND ";
            $query .= "     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T_REPL ON T_REPL.YEAR = SUBM.YEAR ";
            $query .= "         AND T_REPL.COMBINED_CLASSCD = SUBM.CLASSCD ";
            $query .= "         AND T_REPL.COMBINED_SCHOOL_KIND = SUBM.SCHOOL_KIND ";
            $query .= "         AND T_REPL.COMBINED_CURRICULUM_CD = SUBM.CURRICULUM_CD ";
            $query .= "         AND T_REPL.COMBINED_SUBCLASSCD = SUBM.SUBCLASSCD ";
            $query .= "     INNER JOIN CHAIR_DAT T_CHAIR ON T_CHAIR.YEAR = SUBM.YEAR ";
            $query .= "         AND T_CHAIR.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "         AND T_CHAIR.CLASSCD = SUBM.CLASSCD ";
            $query .= "         AND T_CHAIR.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
            $query .= "         AND T_CHAIR.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
            $query .= "         AND T_CHAIR.SUBCLASSCD = SUBM.SUBCLASSCD ";
            $query .= "     INNER JOIN CHAIR_STD_DAT T_CHRSTD ON T_CHRSTD.YEAR = SUBM.YEAR ";
            $query .= "         AND T_CHRSTD.SEMESTER = T_CHAIR.SEMESTER ";
            $query .= "         AND T_CHRSTD.CHAIRCD = T_CHAIR.CHAIRCD ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT IREGD ON IREGD.YEAR = T_CHRSTD.YEAR ";
            $query .= "         AND IREGD.SEMESTER = T_CHRSTD.SEMESTER ";
            $query .= "         AND IREGD.SCHREGNO = T_CHRSTD.SCHREGNO ";
            $query .= "         AND IREGD.GRADE = T1.GRADE ";
            $query .= "         AND IREGD.GRADE || '-' || IREGD.HR_CLASS = T1.GRADE || '-' || T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     SUBM.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND NMA023.NAMECD2 || ':' || SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
            $query .= "     AND T_REPL.YEAR IS NULL ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     NMA023.NAMECD2 || ':' || SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD AS VALUE, ";
        $query .= "     SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD || '：' || SUBM.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST SUBM ";
        $query .= "     LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' ";
        $query .= "         AND NMA023.NAME1 = SUBM.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T_REPL ON T_REPL.YEAR = SUBM.YEAR ";
        $query .= "         AND T_REPL.COMBINED_CLASSCD = SUBM.CLASSCD ";
        $query .= "         AND T_REPL.COMBINED_SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "         AND T_REPL.COMBINED_CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "         AND T_REPL.COMBINED_SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_DAT T_CHAIR ON T_CHAIR.YEAR = SUBM.YEAR ";
        $query .= "         AND T_CHAIR.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T_CHAIR.CLASSCD = SUBM.CLASSCD ";
        $query .= "         AND T_CHAIR.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "         AND T_CHAIR.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "         AND T_CHAIR.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T_CHRSTD ON T_CHRSTD.YEAR = SUBM.YEAR ";
        $query .= "         AND T_CHRSTD.SEMESTER = T_CHAIR.SEMESTER ";
        $query .= "         AND T_CHRSTD.CHAIRCD = T_CHAIR.CHAIRCD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T_CHRSTD.YEAR ";
        $query .= "         AND REGD.SEMESTER = T_CHRSTD.SEMESTER ";
        $query .= "         AND REGD.SCHREGNO = T_CHRSTD.SCHREGNO ";
        if ($model->field["PRINT_DIV"] == "1") {
            $query .= "         AND REGD.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "         AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        }
        if ($model->field["PRINT_DIV"] == "2") {
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     INNER JOIN CHAIR_STF_DAT T_CHRSTF ON T_CHRSTF.YEAR = SUBM.YEAR ";
                $query .= "         AND T_CHRSTF.SEMESTER = T_CHAIR.SEMESTER ";
                $query .= "         AND T_CHRSTF.CHAIRCD = T_CHAIR.CHAIRCD ";
                $query .= "         AND T_CHRSTF.STAFFCD = '".STAFFCD."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     SUBM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T_CHAIR.CLASSCD <= '90' ";
        $query .= "     AND T_REPL.YEAR IS NULL ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->field["PRINT_DIV"] == "1") {
                $query .= "     AND SUBM.SCHOOL_KIND IN (SELECT ";
                $query .= "                                  SCHOOL_KIND ";
                $query .= "                              FROM ";
                $query .= "                                  SCHREG_REGD_GDAT ";
                $query .= "                              WHERE ";
                $query .= "                                      YEAR  = '".CTRL_YEAR."' ";
                $query .= "                                  AND GRADE = '".$model->field["GRADE"]."' ";
                $query .= "                              ) ";
            }
            if ($model->field["PRINT_DIV"] == "2") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SUBM.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SUBM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
