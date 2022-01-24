<?php

require_once('for_php7.php');

class knjd187pQuery extends Query
{

    //学期区分
    public function getSemesterdiv()
    {
        $query  = " SELECT";
        $query .= "     SEMESTERDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学期
    public function getSemester()
    {
        $query  = " SELECT";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getGradeHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "     T1.GRADE = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象者リスト
    public function getSchno($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "     T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
