<?php

require_once('for_php7.php');

class knjc162Query extends Query
{
    //年組取得（権限チェック）
    public function getAuth($model)
    {
        $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $semester = $semester == "9" ? CTRL_SEMESTER : $semester;
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$semester."' ";
            $query .= "     AND GRADE    = '".$model->field["GRADE"]."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR       = '".$model->control["年度"]."' ";
            $query .= "     AND SEMESTER   = '".$semester."' ";
            $query .= "     AND GRADE      = '".$model->field["GRADE"]."' ";
            $query .= "     AND (  TR_CD1  = '".STAFFCD."' ";
            $query .= "         OR TR_CD2  = '".STAFFCD."' ";
            $query .= "         OR TR_CD3  = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD1  = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD2  = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD3  = '".STAFFCD."') ";
        }

        return $query;
    }

    //学年取得
    public function getSelectGrade($model)
    {
        $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $semester = $semester == "9" ? CTRL_SEMESTER : $semester;
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
            $query .= "     T1.SEMESTER = '{$semester}' AND ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' OR ";
            $query .= "     L1.SCHOOL_KIND = 'J' ";
            $query .= "     ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
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
            $query .= "     T1.SEMESTER = '{$semester}' AND ";
            $query .= "     ( ";
            $query .= "     TR_CD1    = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= "     OR SUBTR_CD1  = '".STAFFCD."' ";
            $query .= "     OR SUBTR_CD2  = '".STAFFCD."' ";
            $query .= "     OR SUBTR_CD3  = '".STAFFCD."') AND  ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' OR ";
            $query .= "     L1.SCHOOL_KIND = 'J' ";
            $query .= "     ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["knjc162NenkanAttendance"] == "1") {
            $query .= "     CASE WHEN SEMESTER = '9' ";
            $query .= "          THEN '年間' ";
            $query .= "          ELSE SEMESTERNAME ";
            $query .= "     END AS LABEL, ";
        } else {
            $query .= "     SEMESTERNAME AS LABEL, ";
        }
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["knjc162NenkanAttendance"] != "1") {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
}