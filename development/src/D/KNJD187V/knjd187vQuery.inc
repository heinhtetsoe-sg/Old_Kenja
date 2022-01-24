<?php

require_once('for_php7.php');

class knjd187vQuery extends Query
{
    //学期
    public function getSemester($model)
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

    //学年取得
    public function getGrade($model, $semester, $grade = "")
    {
        $query  = " SELECT DISTINCT ";
        if (!$grade) {
            $query .= "     GDAT.GRADE AS VALUE, ";
            $query .= "     GDAT.GRADE_NAME1 AS LABEL, ";
        }
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        $query .= "           AND GDAT.SCHOOL_KIND = 'J' ";
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        if ($grade) {
            $query .= "     AND REGDH.GRADE = '{$grade}' ";
        }
        if (!$grade) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //年組取得（権限チェック）
    public function getGradeHrClass($model, $semester, $grade = "")
    {
        $query  = " SELECT ";
        $query .= "     REGDH.HR_CLASS AS VALUE, ";
        $query .= "     REGDH.HR_NAME AS LABEL, ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGDH.YEAR = GDAT.YEAR ";
        $query .= "           AND REGDH.GRADE = GDAT.GRADE ";
        $query .= "           AND GDAT.SCHOOL_KIND = 'J' ";
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3) ";
        }
        $query .= "     AND REGDH.GRADE = '{$grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象生徒
    public function getStudent($model, $semester)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".$semester."' AND ";
        $query .= "     T2.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "     T2.HR_CLASS = '".$model->field["HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.SCHREGNO ";

        return $query;
    }
}
