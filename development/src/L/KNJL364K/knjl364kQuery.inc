<?php

require_once('for_php7.php');
class knjl364kquery extends Query
{

    //合格コース
    public function getExamCourse($year)
    {
        return "SELECT coursecd,majorcd,examcoursecd,examcourse_name FROM entexam_course_mst WHERE entexamyear = '".$year."' ";
    }

    //合格コース名
    public function getExamCourseName($model)
    {
        $query  = " SELECT examcourse_name FROM entexam_course_mst ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND coursecd     = '".substr($model->examcourse, 0, 1)."' ";
        $query .= "    AND majorcd      = '".substr($model->examcourse, 1, 3)."' ";
        $query .= "    AND examcoursecd = '".substr($model->examcourse, 4)."' ";
        return $query;
    }

    //中高判定 NO005
    public function getJorH()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //実行
    public function selectCvsQuery($model, $bankcd, $jhflg)
    {
        $query  = "SELECT ";
        $query .= "    '' AS GRADE, ";
        $query .= "    '' AS HR_CLASS, ";
        $query .= "    '' AS ATTENDNO, ";
        $query .= "    '' AS NAME, ";
        if ($jhflg == 1) {
            $query .= "    T1.ENTEXAMYEAR || CAST(SMALLINT(T1.EXAMNO) + 5000 AS CHAR(4)) AS SCHREGNO, ";
        } else {
            $query .= "    T1.ENTEXAMYEAR || T1.EXAMNO AS SCHREGNO, ";
        }
        $query .= "    '01' AS RELATIONSHIP, ";
        $query .= "    T1.GNAME AS GUARD_NAME, ";
        $query .= "    T1.GKANA AS GUARD_KANA, ";
        $query .= "    '' AS GUARD_SEX, ";
        $query .= "    '' AS GUARD_BIRTHDAY, ";
        $query .= "    L1.GZIPCD AS GUARD_ZIPCD, ";
        $query .= "    L1.GADDRESS AS GUARD_ADDR1, ";
        $query .= "    '' AS GUARD_ADDR2, ";
        $query .= "    T1.GTELNO AS GUARD_TELNO, ";
        $query .= "    '' AS GUARD_FAXNO, ";
        $query .= "    '' AS GUARD_E_MAIL, ";
        $query .= "    '' AS GUARD_JOBCD, ";
        $query .= "    '' AS GUARD_WORK_NAME, ";
        $query .= "    '' AS GUARD_WORK_TELNO, ";
        $query .= "    '01' AS GUARANTOR_RELATIONSHIP, ";
        $query .= "    T1.GNAME AS GUARANTOR_NAME, ";
        $query .= "    T1.GKANA AS GUARANTOR_KANA, ";
        $query .= "    '' AS GUARANTOR_SEX, ";
        $query .= "    L1.GZIPCD AS GUARANTOR_ZIPCD, ";
        $query .= "    L1.GADDRESS AS GUARANTOR_ADDR1, ";
        $query .= "    '' AS GUARANTOR_ADDR2, ";
        $query .= "    T1.GTELNO AS GUARANTOR_TELNO, ";
        $query .= "    '' AS GUARANTOR_JOBCD, ";
        $query .= "    '' AS PUBLIC_OFFICE, ";
        $query .= "    '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.TESTDIV = L1.TESTDIV ";
        $query .= "         AND T1.EXAMNO = L1.EXAMNO ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        if ($this->examcourse != "99999999") {
            $query .= "    AND T1.SUC_COURSECD   = '".substr($model->examcourse, 0, 1)."' ";
            $query .= "    AND T1.SUC_MAJORCD    = '".substr($model->examcourse, 1, 3)."' ";
            $query .= "    AND T1.SUC_COURSECODE = '".substr($model->examcourse, 4)."'   ";
        } else {
            $query .= "    AND T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE IN('".implode($model->examcourseall, "','")."') ";
        }
        $query .= "    AND T1.PROCEDUREDIV = '2' ";
        $query .= "    AND T1.ENTDIV = '2' ";
        $query .= "    AND T1.JUDGEMENT IN ('1','2','3','4','5','6','9') ";
        $query .= "ORDER BY ";
        $query .= "    T1.SUC_COURSECD, ";
        $query .= "    T1.SUC_MAJORCD, ";
        $query .= "    T1.SUC_COURSECODE, ";
        $query .= "    T1.EXAMNO ";

        return $query;
    }
}
