<?php

require_once('for_php7.php');

class knjd157cQuery extends Query
{

    //学期取得
    public function getSemester()
    {
        $query  = "   SELECT SEMESTER     AS VALUE, ";
        $query .= "          SEMESTERNAME AS LABEL ";
        $query .= "     FROM SEMESTER_MST ";
        $query .= "    WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getGradeHrClass($model, $semester, $div = "")
    {
        //参照・更新可
        $query  = "     SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "        T1.GRADE       AS VALUE, ";
            $query .= "        T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "        T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "        T1.HR_NAME                     AS LABEL ";
        }
        $query .= "       FROM SCHREG_REGD_HDAT T1";
        $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "         ON T1.YEAR        = T2.YEAR ";
        $query .= "        AND T1.GRADE       = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "      WHERE T1.YEAR        = '".CTRL_YEAR."' ";
        if ($semester !== '9') {
            $query .= "     AND T1.SEMESTER = '".$semester."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        if ($div != "GRADE") {
            $query .= "    AND T1.GRADE       = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND (   T1.TR_CD1  = '".STAFFCD."' ";
            $query .= "         OR T1.TR_CD2  = '".STAFFCD."' ";
            $query .= "         OR T1.TR_CD3  = '".STAFFCD."') ";
        }
        $query .= "   ORDER BY VALUE ";

        return $query;
    }

    //テスト取得
    public function getTest($semester)
    {
        $query  = "   SELECT TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "          TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= "     FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND SEMESTER = '".$semester."' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }
}
