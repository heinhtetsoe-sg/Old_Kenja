<?php

require_once('for_php7.php');

class knjd183eQuery extends Query
{

    //学期
    public function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (SEMESTER = '1' ";
        $query .= "      OR  SEMESTER = '2' ";
        $query .= "      OR  SEMESTER = '9') ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年
    public function getRegdGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス
    public function getRegdHdat($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$seme."' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒データ
    public function getSchno($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.SCHREGNO  AS VALUE";
        $query .= "   , T3.HR_NAME || ' ' || T2.ATTENDNO || '番 ' || T1.NAME_SHOW AS LABEL";
        $query .= "   FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "   , SCHREG_REGD_DAT T2 ";
        $query .= "   , SCHREG_REGD_HDAT T3 ";
        $query .= "  WHERE ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND T2.YEAR = T3.YEAR ";
        $query .= "     AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T2.GRADE = T3.GRADE ";
        $query .= "     AND T2.HR_CLASS = T3.HR_CLASS ";
        $query .= " ORDER BY T2.ATTENDNO ";

        return $query;
    }

    public function getHrClassList($seme)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER    = '".$seme."' ";

        return $query;
    }
}
