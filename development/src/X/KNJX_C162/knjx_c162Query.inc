<?php

require_once('for_php7.php');

class knjx_c162Query extends Query
{
    //校種取得
    public function getSchoolKind($grade)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND　";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT　";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getHrClass($model)
    {
        $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $semester = $semester == "9" ? CTRL_SEMESTER : $semester;
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "             ON T2.YEAR  = T1.YEAR ";
        $query .= "            AND T2.GRADE = T1.GRADE ";
        $query .= "            AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getSelectGrade($model)
    {
        $semester = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $semester = $semester == "9" ? CTRL_SEMESTER : $semester;
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "             ON T2.YEAR  = T1.YEAR ";
        $query .= "            AND T2.GRADE = T1.GRADE ";
        $query .= "            AND T2.SEMESTER = '".$semester."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    public function getSemester($model, $semester = "")
    {
        $query  = " SELECT ";
        if ($semester != "") {
            $query .= "     SEMESTERNAME ";
        } else {
            if ($model->Properties["knjc162NenkanAttendance"] == "1") {
                $query .= "     '".CTRL_YEAR."年度 ' || ";
                $query .= "     CASE WHEN SEMESTER = '9' ";
                $query .= "          THEN '年間' ";
                $query .= "          ELSE SEMESTERNAME ";
                $query .= "     END AS LABEL, ";
            } else {
                $query .= "     '".CTRL_YEAR."年度 ' || SEMESTERNAME AS LABEL, ";
            }
            $query .= "     SEMESTER AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        if ($semester != "") {
            $query .= " AND SEMESTER = '".$semester."' ";
        } elseif ($model->Properties["knjc162NenkanAttendance"] != "1") {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }
}
