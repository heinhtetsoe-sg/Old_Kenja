<?php

require_once('for_php7.php');

class knjd627cQuery extends Query
{
    //前期後期リスト取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE  ";
        $query .= "     NAMECD1 = 'D039' "; // 関学専用を指す

        return $query;
    }

    //年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_NAME1 AS NAME, ";
        $query .= "     GRADE_CD,";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND VALUE(GRADE_CD, 0) > 1 "; // ２年次以降のみ
        if ($model->isIndicateSchoolKind()) {
            $query .= " AND SCHOOL_KIND ='{$model->selectSchoolKind}' ";
        }
        $query .= " ORDER BY SCHOOL_KIND DESC, GRADE_CD ASC";

        return $query;
    }

    //下級年次取得
    public function getLowerGrade($model, $school_kind, $gradecd)
    {
        $query  = " SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        //指定校種における指定学年に対して下級年次を指定（年度と年次）
        $year = (int)$model->year - 1;
        $grade_lower = $gradecd - 1;
        $join_condition = "";
        for (; 0 < $year; $year--) {
            $query .= $join_condition." ( ";
            $query .= "     YEAR = '{$year}' ";
            $query .= " AND SCHOOL_KIND = '{$school_kind}' ";
            $query .= " AND VALUE(GRADE_CD, 0) = {$grade_lower} ";
            $query .= ")";
            $join_condition = " OR ";
            $grade_lower--;
            if ($grade_lower < 1) {
                break;
            }
        }

        $query .= " ORDER BY GRADE ASC";

        return $query;
    }

    //再試験対象年度時に受講していた科目一覧を取得
    public function getTakenSubClassList($model, $school_kind)
    {
        $query  = " WITH RETRY_TEST_CLS AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CLASSCD , ";
        $query .= "     CURRICULUM_CD , ";
        $query .= "     SUBCLASSCD , ";
        $query .= "     SCHOOL_KIND  ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->retry_test_year}' ";
        $query .= "     AND SEMESTER    = '{$model->retry_test_semester}' ";
        $query .= "     AND TESTKINDCD  = '{$model->retry_test_kind}' ";
        $query .= "     AND TESTITEMCD  = '{$model->retry_test_itemcd}' ";
        $query .= "     AND SCORE_DIV   = '{$model->retry_test_score_div}' ";
        $query .= "     AND SCHOOL_KIND = '{$school_kind}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUBCLS.CLASSCD       AS CLASSCD,";
        $query .= "     SUBCLS.CURRICULUM_CD AS CURRICULUM_CD ,";
        $query .= "     SUBCLS.SUBCLASSCD    AS SUBCLASSCD ,";
        $query .= "     SUBCLS.SUBCLASSNAME  AS LABEL , ";
        $query .= "     SUBCLS.SCHOOL_KIND   AS SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST SUBCLS, RETRY_TEST_CLS ";
        $query .= " WHERE ";
        $query .= "     SUBCLS.CLASSCD       = RETRY_TEST_CLS.CLASSCD ";
        $query .= " AND SUBCLS.CURRICULUM_CD = RETRY_TEST_CLS.CURRICULUM_CD ";
        $query .= " AND SUBCLS.SUBCLASSCD    = RETRY_TEST_CLS.SUBCLASSCD ";
        $query .= " AND SUBCLS.SCHOOL_KIND   = RETRY_TEST_CLS.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLS.CLASSCD, SUBCLS.CURRICULUM_CD, SUBCLS.SUBCLASSCD ASC ";

        return $query;
    }

    //年組取得
    public function getHrClass($model, $semester = "", $grade = "")
    {
        $query  = " SELECT ";
        $query .= "    HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME  AS LABEL ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '{$model->year}' ";
        if ($semester != "") {
            $query .= " AND SEMESTER='{$semester}' ";
        }
        if ($grade != "") {
            $query .= " AND GRADE='{$grade}' ";
        }

        return $query;
    }
}
