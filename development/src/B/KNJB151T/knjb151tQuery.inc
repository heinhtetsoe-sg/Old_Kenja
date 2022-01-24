<?php

require_once('for_php7.php');


class knjb151tQuery extends Query {

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJB151T' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年組取得
    function getHrClass($year, $semester)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR='" .$year ."'";
        $query .= "AND SEMESTER='".$semester ."'";

        return $query;
    }

    //生徒取得
    function getSchregno($year, $semester, $hr_class)
    {
        $query  = "SELECT ";
        $query .= "     T2.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS NAME ";
        $query .= "FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "WHERE ";
        $query .= "         T2.YEAR     = '" .$year ."' ";
        $query .= "     AND T2.SEMESTER = '" .$semester ."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '" .$hr_class ."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "ORDER BY ";
        $query .= "     T2.ATTENDNO";

        return $query;
    }

    //CSVデータ取得
    function getCsvData($inSentence)
    {
        $query  = "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.SEMESTER, ";
        $query .= "    L7.HR_NAME || REPLACE(CAST(SMALLINT(L6.ATTENDNO) AS CHAR(3)), ' ', '') || '番' AS HR_ATTEND, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    L5.NAME_SHOW, ";
        $query .= "    L1.TEXTBOOKCD, ";
        $query .= "    L3.TEXTBOOKNAME, ";
        $query .= "    L4.ISSUECOMPANYNAME, ";
        $query .= "    L3.TEXTBOOKPRICE, ";
        $query .= "    T1.CHAIRCD, ";
        $query .= "    L2.CHAIRNAME, ";
        $query .= "    '' AS YOUFUYOU ";
        $query .= "FROM ";
        $query .= "    (SELECT DISTINCT ";
        $query .= "        TT1.YEAR, ";
        $query .= "        TT1.SEMESTER, ";
        $query .= "        TT1.CHAIRCD, ";
        $query .= "        TT1.SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        CHAIR_STD_DAT TT1 ";
        $query .= "    WHERE ";
        $query .= "        TT1.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND TT1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "        AND TT1.SCHREGNO IN (".$inSentence.") ";
        $query .= "    ) T1 ";
        $query .= "    LEFT JOIN CHAIR_TEXTBOOK_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "    LEFT JOIN CHAIR_DAT L2 ON L2.CHAIRCD = T1.CHAIRCD ";
        $query .= "         AND L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "    LEFT JOIN TEXTBOOK_MST L3 ON L3.TEXTBOOKCD = L1.TEXTBOOKCD ";
        $query .= "    LEFT JOIN ISSUECOMPANY_MST L4 ON L4.ISSUECOMPANYCD = L3.ISSUECOMPANYCD ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L6 ON L6.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L6.YEAR = T1.YEAR ";
        $query .= "         AND L6.SEMESTER = T1.SEMESTER ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L7 ON L7.YEAR = T1.YEAR ";
        $query .= "         AND L7.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L7.GRADE = L6.GRADE ";
        $query .= "         AND L7.HR_CLASS = L6.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.SCHREGNO IN (".$inSentence.") ";
        $query .= "    AND L1.YEAR = T1.YEAR ";
        $query .= "    AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "    AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "ORDER BY ";
        $query .= "    L6.GRADE, ";
        $query .= "    L6.HR_CLASS, ";
        $query .= "    L6.ATTENDNO, ";
        $query .= "    L3.TEXTBOOKDIV, ";
        $query .= "    L1.TEXTBOOKCD ";

        return $query;
    }

}
?>
