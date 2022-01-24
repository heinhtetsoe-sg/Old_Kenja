<?php

require_once('for_php7.php');

class knjxSearch_StudentQuery extends Query {

    function getRegdHdat()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '" .CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCourseMajor()
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCourseCode()
    {
        $query  = " SELECT ";
        $query .= "     COURSECODE AS VALUE, ";
        $query .= "     COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function SearchStudent($fields) {
        $query  = " SELECT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW AS NAME, ";
        $query .= "     substr(char(T1.ENT_DATE), 1, 4) AS GRADUATEYEAR, ";
        $query .= "     T1.NAME_KANA AS KANA, ";
        $query .= "     T1.SEX AS SEXNUM, ";
        $query .= "     CASE T1.SEX WHEN T4.NAMECD2 THEN T4.NAME2 ELSE T1.SEX END AS SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.HR_NAME ";
        $query .= "  FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT OUTER JOIN (SELECT NAME2, NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'Z002') T4 ON T1.SEX = T4.NAMECD2, ";
        $query .= "     SCHREG_REGD_DAT T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "       T1.SCHREGNO = T2.SCHREGNO";
        $query .= "   AND T2.YEAR = '".CTRL_YEAR."'";
        $query .= "   AND T2.SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "   AND T3.YEAR = '".CTRL_YEAR."'";
        $query .= "   AND T3.SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "   AND T2.GRADE = T3.GRADE";
        $query .= "   AND T2.HR_CLASS = T3.HR_CLASS";
        if (strlen($fields["SCHREGNO"])) {
            $query .= "   AND T1.SCHREGNO = '".$fields["SCHREGNO"]."'";
        }
        if (strlen($fields["GRADE"])) {
            $query .= "   AND T2.GRADE = '".$fields["GRADE"]."'";
        }
        if (strlen($fields["HRCLASS"])) {
            $query .= "   AND T2.HR_CLASS = '".$fields["HRCLASS"]."'";
        }
        if (strlen($fields["ATTENDNO"])) {
            $query .= "   AND T2.ATTENDNO LIKE '%".$fields["ATTENDNO"]."%'";
        }
        if (strlen($fields["COURSEMAJOR"])) {
            $query .= "   AND T2.COURSECD || T2.MAJORCD = '".$fields["COURSEMAJOR"]."'";
        }
        if (strlen($fields["COURSECODE"])) {
            $query .= "   AND T2.COURSECODE = '".$fields["COURSECODE"]."'";
        }
        if (strlen($fields["NAME"])) {
            $query .= "   AND T1.NAME LIKE '%".$fields["NAME"]."%'";
        }
        if (strlen($fields["NAMESHOW"])) {
            $query .= "   AND T1.NAME_SHOW LIKE '%".$fields["NAMESHOW"]."%'";
        }
        if (strlen($fields["KANA"])) {
            $query .= "   AND T1.NAME_KANA LIKE '%".$fields["KANA"]."%'";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
