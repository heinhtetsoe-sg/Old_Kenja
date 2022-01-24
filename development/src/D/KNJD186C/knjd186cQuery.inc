<?php

require_once('for_php7.php');

class knjd186cQuery extends Query {
    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || ' ' || T1.ATTENDNO || '番 ' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->field["GRADE_HRCLASS"]}' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //年組取得（権限チェック）
    function getSelectHrclass($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '" .CTRL_YEAR. "' ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            $query .= "     AND EXISTS (SELECT 'X' ";
            $query .= "          FROM SCHREG_REGD_DAT T3 WHERE T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "                                  AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ";
            $query .= "                                  AND T3.COURSECD || '-' || T3.MAJORCD = '".$model->field["MAJOR"]."' ";
            $query .= "         ) ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '" .CTRL_YEAR. "' ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            $query .= "     AND EXISTS (SELECT 'X' ";
            $query .= "          FROM SCHREG_REGD_DAT T3 WHERE T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "                                  AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ";
            $query .= "                                  AND T3.COURSECD || '-' || T3.MAJORCD = '".$model->field["MAJOR"]."' ";
            $query .= "         ) ";
            $query .= " AND ( TR_CD1 = '" .STAFFCD. "' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD. "' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD. "' ";
            $query .= "    OR SUBTR_CD1 = '" .STAFFCD. "' ";
            $query .= "    OR SUBTR_CD2 = '" .STAFFCD. "' ";
            $query .= "    OR SUBTR_CD3 = '" .STAFFCD. "') ";
        }

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        return $query;
    }

}
?>
