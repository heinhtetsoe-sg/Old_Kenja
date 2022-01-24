<?php

require_once('for_php7.php');

class knjl304rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //課程学科
    function getCourseMajorcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L1.COURSENAME || L1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.ENTEXAMYEAR = L1.YEAR ";
        $query .= "                                    AND T1.COURSECD = L1.COURSECD ";
        $query .= "                                    AND T1.MAJORCD  = L1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //受験コースマスタより取得
    function getEntexamCourseMst($year, $applicantdiv, $testdiv, $course_majorcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                AND L1.COURSECD = T1.COURSECD ";
        $query .= "                                AND L1.MAJORCD = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."' ";
        $query .= " AND T1.APPLICANTDIV = '".$applicantdiv."' ";
        $query .= " AND T1.TESTDIV = '".$testdiv."' ";
        if ($course_majorcd !== '9999') {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$course_majorcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
