<?php

require_once('for_php7.php');

class knjz031Query extends Query
{
    public function getYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    public function getSemeName($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year. "' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    public function getDeleteQuery($year)
    {
        $query  = " DELETE FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $year .  "' ";

        return $query;
    }

    public function getInsertQuery($insert_array, $year)
    {
        $updated = date("Y-m-d H:i:s", time());

        $query  = " INSERT INTO SEMESTER_DETAIL_MST  ";
        $query .= "     VALUES ('" . $year . "', ";
        $query .= "             '" . $insert_array["SEM_CD"] . "', ";
        $query .= "             '" . $insert_array["SEM_DITAIL_CD"] . "', ";
        $query .= "             '" . $insert_array["SEMESTER_NAME"] . "', ";
        $query .= "             '" . $insert_array["SDATE"] . "', ";
        $query .= "             '" . $insert_array["EDATE"] . "', ";
        $query .= "             '" . STAFFCD . "', ";
        $query .= "             '" . $updated . "') ";

        return $query;
    }

    public function countSemesterDetail($code, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER = '". $code . "' AND ";
        $query .= "     YEAR = '". $year . "' ";

        return $query;
    }

    public function getSdate($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS SEM_CD, ";
        $query .= "     T2.SEMESTERNAME AS SEM_NAME, ";
        $query .= "     T1.SEMESTER_DETAIL AS SEM_DITAIL_CD, ";
        $query .= "     T1.SEMESTERNAME AS SEMESTER_NAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST T1  ";
        $query .= " LEFT JOIN SEMESTER_MST T2  ";
        $query .= "     ON T1.YEAR = T2.YEAR  ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . $year . "' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTER_DETAIL ";

        return $query;
    }
}
