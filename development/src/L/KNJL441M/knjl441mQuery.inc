<?php

require_once('for_php7.php');

class knjl441mQuery extends Query
{
    //校種リスト取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(ABBV1,'') AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '{$model->examyear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験名取得
    public function getApplicant($model)
    {
        $query  = " SELECT ";
        $query .= "     APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY AS VALUE, ";
        $query .= "     EXAM_NAME  AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR             = '{$model->examyear}' AND ";
        $query .= "     EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
}
