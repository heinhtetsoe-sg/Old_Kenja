<?php

require_once('for_php7.php');

class knjd234uQuery extends Query {

    //名称マスタ
    function getNameMst($model, $namecd1) {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年コード取得
    function getGradeCdQuery($grade) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    //学校校種取得
    function getSchoolkindQuery($grade) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース名
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "  T2.COURSECODENAME AS LABEL, ";
        $query .= "  T2.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "  COURSECODE_YDAT T1 ";
        $query .= "  LEFT JOIN COURSECODE_MST T2 ";
        $query .= "    ON T2.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND EXISTS(SELECT 'X' ";
        $query .= "             FROM ";
        $query .= "               SCHREG_REGD_DAT T3 ";
        $query .= "             WHERE ";
        $query .= "               T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "               AND T3.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "               AND T3.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "               AND T3.COURSECODE = T1.COURSECODE) ";
        $query .= " ORDER BY ";
        $query .= "  T2.COURSECODE ";

        return $query;
    }

    //皆勤の生徒
    function getKaikinStudentRegdDat($model) {
        $query  = " SELECT ";
        $query .= "     VALUE(REGDH.HR_NAMEABBV, ' ') || ' ' || REGD.ATTENDNO || '番 ' || BASE.NAME AS LABEL, ";
        $query .= "     REGD.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ";
        $query .= "         AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "         AND REGDH.GRADE = REGD.GRADE ";
        $query .= "         AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND REGD.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND REGD.GRADE = '".$model->field["GRADE"]."' ";
        if ($model->field["COURSE"] != "" && $model->field["COURSE"] != "ALL") {
            $query .= " AND REGD.COURSECODE = '".$model->field["COURSE"]."' ";
        }
        $query .= " AND EXISTS ( ";
        $query .= "  SELECT 'X' ";
        $query .= "  FROM ATTEND_SEMES_DAT T1 ";
        $query .= "  WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "  GROUP BY T1.SCHREGNO ";
        $query .= "  HAVING SUM(VALUE(SICK, 0)) + SUM(VALUE(NOTICE, 0)) + SUM(VALUE(NONOTICE, 0)) + SUM(VALUE(LATE, 0)) + SUM(VALUE(EARLY, 0)) = 0 ";
        $query .= " ) ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ";

        return $query;
    }

}
?>
