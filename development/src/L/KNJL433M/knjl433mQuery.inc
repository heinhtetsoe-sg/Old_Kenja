<?php

require_once('for_php7.php');

class knjl433mQuery extends Query
{
    //校種コンボ
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examyear."' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getExamId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || EXAM_NAME AS LABEL, ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //CSV出力
    public function getCsvList($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPTNO, ";
        $query .= "     JUDGEMENT || LEFT(RECEPTNO,1) AS STATUS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$model->examId}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";
        
        return $query;
    }
}
