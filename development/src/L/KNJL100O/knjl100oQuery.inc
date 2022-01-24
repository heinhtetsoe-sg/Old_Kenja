<?php

require_once('for_php7.php');
class knjl100oquery extends Query
{

    //合格コース
    public function getExamCourse($year)
    {
        return "SELECT coursecd,majorcd,examcoursecd,examcourse_name FROM entexam_course_mst WHERE entexamyear = '".$year."' ";
    }

    //名称マスタより取得
    public function getNameMst($year, $namecd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
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

    //実行
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     '1' || substr(T1.SUC_COURSECODE, 4) || substr(T1.ENTEXAMYEAR, 3) || ";
        $query .= "         cast(RIGHT(RTRIM('0000'|| cast(row_number() over(ORDER BY T1.NAME_KANA) as char(4))), 4) AS char(4)) AS SCHREGNO, ";
        $query .= "     T3.NAMESPARE1, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     T3.NAMESPARE2, ";
        $query .= "     T1.SUC_COURSECD AS COURSECD, ";
        $query .= "     T1.SUC_MAJORCD AS MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     cast(replace(cast(T1.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     CASE WHEN VALUE(T1.FS_GRDYEAR, '') != '' ";
        $query .= "          THEN FS_GRDYEAR || '/03/31' ";
        $query .= "          ELSE '' END AS FINSCHOOLGRADDATE, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     '' AS FAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     '' AS EMERGENCYCALL, ";
        $query .= "     '' AS EMERGENCYTELNO, ";
        $query .= "     '' AS SCALASHIPDIV, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT OUTER JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.EXAMNO      = T1.EXAMNO ";
        $query .= "     LEFT OUTER JOIN V_NAME_MST T3 ON T3.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "          AND T3.NAMECD1 = 'L003' ";
        $query .= "          AND T3.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->examyear."' AND ";
        $query .= "     T1.SUC_COURSECD     = '".substr($model->examcourse, 0, 1)."' AND ";
        $query .= "     T1.SUC_MAJORCD      = '".substr($model->examcourse, 1, 3)."' AND ";
        $query .= "     T1.SUC_COURSECODE   = '".substr($model->examcourse, 4)."' AND ";
        $query .= "     T1.ENTCLASS         = '".$model->entclass."' AND ";
        $query .= "     T1.PROCEDUREDIV     = '1' AND ";    //手続済み
        $query .= "     T1.ENTDIV           = '1' AND ";    //入学有り
        $query .= "     T1.JUDGEMENT        = '1'";         //合格
        $query .= " ORDER BY ";
        $query .= "     T1.NAME_KANA ";
        return $query;
    }

    //実行
    public function selectCvsGuardQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     '' AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '' AS NAME, ";
        $query .= "     '1' || substr(T1.SUC_COURSECODE, 4) || substr(T1.ENTEXAMYEAR, 3) || ";
        $query .= "         cast(RIGHT(RTRIM('0000'|| cast(row_number() over(ORDER BY T1.NAME_KANA) as char(4))), 4) AS char(4)) AS SCHREGNO, ";
        $query .= "     '01' AS RELATIONSHIP, ";
        $query .= "     L1.GNAME AS GUARD_NAME, ";
        $query .= "     L1.GKANA AS GUARD_KANA, ";
        $query .= "     '' AS GUARD_SEX, ";
        $query .= "     '' AS GUARD_BIRTHDAY, ";
        $query .= "     L1.GZIPCD AS GUARD_ZIPCD, ";
        $query .= "     L1.GADDRESS1 AS GUARD_ADDR1, ";
        $query .= "     L1.GADDRESS2 AS GUARD_ADDR2, ";
        $query .= "     L1.GTELNO AS GUARD_TELNO, ";
        $query .= "     '' AS GUARD_FAXNO, ";
        $query .= "     '' AS GUARD_E_MAIL, ";
        $query .= "     '' AS GUARD_JOBCD, ";
        $query .= "     '' AS GUARD_WORK_NAME, ";
        $query .= "     '' AS GUARD_WORK_TELNO, ";
        $query .= "     '01' AS GUARANTOR_RELATIONSHIP, ";
        $query .= "     L1.GNAME AS GUARANTOR_NAME, ";
        $query .= "     L1.GKANA AS GUARANTOR_KANA, ";
        $query .= "     '' AS GUARANTOR_SEX, ";
        $query .= "     L1.GZIPCD AS GUARANTOR_ZIPCD, ";
        $query .= "     L1.GADDRESS1 AS GUARANTOR_ADDR1, ";
        $query .= "     L1.GADDRESS2 AS GUARANTOR_ADDR2, ";
        $query .= "     L1.GTELNO AS GUARANTOR_TELNO, ";
        $query .= "     '' AS GUARANTOR_JOBCD, ";
        $query .= "     '' AS PUBLIC_OFFICE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT OUTER JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->examyear."' AND ";
        $query .= "     T1.SUC_COURSECD     = '".substr($model->examcourse, 0, 1)."' AND ";
        $query .= "     T1.SUC_MAJORCD      = '".substr($model->examcourse, 1, 3)."' AND ";
        $query .= "     T1.SUC_COURSECODE   = '".substr($model->examcourse, 4)."' AND ";
        $query .= "     T1.ENTCLASS         = '".$model->entclass."' AND ";
        $query .= "     T1.PROCEDUREDIV     = '1' AND ";    //手続済み
        $query .= "     T1.ENTDIV           = '1' AND ";    //入学有り
        $query .= "     T1.JUDGEMENT        = '1'";         //合格
        $query .= " ORDER BY ";
        $query .= "     T1.NAME_KANA ";

        return $query;
    }
}
