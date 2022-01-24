<?php

require_once('for_php7.php');

class knjl141kquery extends Query {

    //合格コース
    function getExamCourse($year)
    {
        return "SELECT coursecd,majorcd,examcoursecd,examcourse_name FROM entexam_course_mst WHERE entexamyear = '".$year."' ";
    }

    //合格コース名
    function getExamCourseName($model)
    {
        $query  = " SELECT examcourse_name FROM entexam_course_mst ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND coursecd     = '".substr($model->examcourse,0,1)."' ";
        $query .= "    AND majorcd      = '".substr($model->examcourse,1,3)."' ";
        $query .= "    AND examcoursecd = '".substr($model->examcourse,4)."' ";
        return $query;
    }

    //実行
    function selectCvsQuery($model)
    {
        $query  = " WITH SCORE_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     EXAMNO,TESTSUBCLASSCD,A_SCORE,B_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR,T1.ENTEXAMYEAR || T1.EXAMNO AS SCHREGNO, ";
        $query .= "     T1.NAME,T1.NAME_KANA,T1.SEX, ";
        $query .= "     T1.SUC_COURSECD,T1.SUC_MAJORCD,T1.SUC_COURSECODE, ";
        $query .= "     s1.A_SCORE AS A_KOKU,s2.A_SCORE AS A_SYA,s3.A_SCORE AS A_SU,s4.A_SCORE AS A_RIKA,s5.A_SCORE AS A_ENG,t1.A_TOTAL, ";
        $query .= "     s1.B_SCORE AS B_KOKU,s2.B_SCORE AS B_SYA,s3.B_SCORE AS B_SU,s4.B_SCORE AS B_RIKA,s5.B_SCORE AS B_ENG,t1.B_TOTAL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN SCORE_DAT  s1 ON s1.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN SCORE_DAT  s2 ON s2.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s2.TESTSUBCLASSCD = '2' ";
        $query .= "     LEFT JOIN SCORE_DAT  s3 ON s3.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s3.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN SCORE_DAT  s4 ON s4.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s4.TESTSUBCLASSCD = '4' ";
        $query .= "     LEFT JOIN SCORE_DAT  s5 ON s5.EXAMNO = t1.EXAMNO ";
        $query .= "     AND s5.TESTSUBCLASSCD = '5' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->examyear."' ";
        $query .= "     AND T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        $query .= "     AND T1.APPLICANTDIV     <> '2' ";

        if ($this->examcourse != "99999999") {
            $query .= " AND T1.SUC_COURSECD     = '".substr($model->examcourse,0,1)."' ";
            $query .= " AND T1.SUC_MAJORCD      = '".substr($model->examcourse,1,3)."' ";
            $query .= " AND T1.SUC_COURSECODE   = '".substr($model->examcourse,4)."'   ";
        } else {
            $query .= " AND T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE IN('".implode($model->examcourseall, "','")."') ";
        }

        $query .= "     AND T1.procedurediv     = '2' ";
        $query .= "     AND T1.entdiv           = '2' ";
        $query .= "     AND T1.judgement IN ('1','2','3','4','5','6','9') ";    //合格フラグ
        $query .= " ORDER BY T1.suc_coursecd,T1.suc_majorcd,T1.suc_coursecode,T1.examno ";
        return $query;
    }
}
?>
