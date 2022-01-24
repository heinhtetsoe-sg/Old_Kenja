<?php

require_once('for_php7.php');


class knja180Query extends Query {

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
        $query .= "     PROGRAMID = 'KNJA180' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学期取得
    function getSelectSeme($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst WHERE year='" .$year ."' AND not semester='9'";

        return $query;
    }

    //クラス一覧取得
    function getSql_GradeHrClass($model)
    {

        $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."' ";
        $query .= "AND SEMESTER='".$model->field["GAKKI"] ."'";

        return $query;

    }


    //課程学科取得
    function GetSql_Course_Major($year)
    {

        $query  = "SELECT ";
        $query .= "COURSECD || MAJORCD AS VALUE,";
        $query .= "COURSECD || MAJORCD || ' ' || VALUE(COURSENAME,'') || VALUE(MAJORNAME,'') AS LABEL ";
        $query .= "FROM V_COURSE_MAJOR_MST ";
        $query .= "WHERE YEAR='" .$year ."' ";

        return $query;

    }


    //課程学科取得CSV
    function GetSql_Course_Major_Csv($year,$cmcd)
    {

        $query  = "SELECT ";
        $query .= "VALUE(COURSENAME,'') || VALUE(MAJORNAME,'') AS COURSE ";
        $query .= "FROM V_COURSE_MAJOR_MST ";
        $query .= "WHERE YEAR='" .$year ."' ";
        $query .= "AND COURSECD || MAJORCD ='" .$cmcd ."' ";

        return $query;

    }

    //コース取得
    function GetSql_CourseCode($year)
    {

        $query  = "SELECT ";
        $query .= "COURSECODE AS VALUE,";
        $query .= "COURSECODE || ' ' || VALUE(COURSECODENAME,'') AS LABEL ";
        $query .= "FROM V_COURSECODE_MST ";
        $query .= "WHERE YEAR='" .$year ."' ";

        return $query;

    }

    //コース取得CSV
    function GetSql_CourseCode_Csv($year,$ccd)
    {

        $query  = "SELECT ";
        $query .= "VALUE(COURSECODENAME,'') AS NAME ";
        $query .= "FROM V_COURSECODE_MST ";
        $query .= "WHERE YEAR='" .$year ."' ";
        $query .= "AND COURSECODE ='" .$ccd ."' ";

        return $query;

    }

    //CSV取得
    function getcsvsch($model,$schregno,$select)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.SEMESTER = '" .$model->field["GAKKI"]. "' AND ";
        $query .= "     T2.YEAR     = '" .$model->control["年度"]. "' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS IN ".$select." AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     2, ";
        $query .= "     3, ";
        $query .= "     4  ";

        return $query;

    }
    //CSV取得
    function getcsvdata($model,$schregno,$sday,$eday)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     DB1.YEAR, ";
        $query .= "     DB1.GRADE, ";
        $query .= "     DB1.HR_CLASS, ";
        $query .= "     DB1.SEMESTER, ";
        $query .= "     DB1.ATTENDNO, ";
        $query .= "     DB1.SCHREGNO, ";
        $query .= "     DB7.HR_NAMEABBV, ";
        $query .= "     DB2.NAME AS SEITO_KANJI, ";
        $query .= "     VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ";
        $query .= "     VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY, ";
        $query .= "     VALUE(DB3.ZIPCD,'') AS ZIPCD1, ";
        $query .= "     VALUE(DB3.ADDR1,'') AS ADDRESS1, ";
        $query .= "     VALUE(DB3.ADDR2,'') AS ADDRESS2, ";
        $query .= "     VALUE(DB3.TELNO,'') AS TELNO1, ";
        $query .= "     VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME, ";
        $query .= "     VALUE(DB3.EMERGENCYTELNO,'') AS TELNO2, ";
        $query .= "     VALUE(DB6.FINSCHOOL_NAME,'')    AS J_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT                 DB1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST      DB2 ON DB1.SCHREGNO = DB2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT     DB7 ON DB1.YEAR = DB7.YEAR ";
        $query .= "                                            AND DB1.SEMESTER = DB7.SEMESTER ";
        $query .= "                                            AND DB1.GRADE = DB7.GRADE ";
        $query .= "                                            AND DB1.HR_CLASS = DB7.HR_CLASS ";
        $query .= "     LEFT  JOIN GUARDIAN_DAT         DB4 ON DB2.SCHREGNO = DB4.SCHREGNO ";
        $query .= "     LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     W1.SCHREGNO, ";
        $query .= "                     ZIPCD, ";
        $query .= "                     TELNO, ";
        $query .= "                     EMERGENCYTELNO, ";
        $query .= "                 ADDR1, ";
        $query .= "                 ADDR2 ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ADDRESS_DAT W1 ";
        $query .= "                     LEFT JOIN SCHREG_BASE_MST L1 ON W1.SCHREGNO = L1.SCHREGNO ";    //NO001
        $query .= "                 WHERE ";
        $query .= "                     (W1.SCHREGNO,W1.ISSUEDATE) IN (SELECT ";
        $query .= "                                                         SCHREGNO, ";
        $query .= "                                                         MAX(ISSUEDATE) ";
        $query .= "                                                     FROM ";
        $query .= "                                                         SCHREG_ADDRESS_DAT W2 ";
        $query .= "                                                     WHERE ";
        //---2005.05.25Modify
        //$query .= "                                                           W2.ISSUEDATE <= '" .$sday. "' ";
        //$query .= "                                                           AND (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '" .$eday. "') AND ";
        $query .= "                                                         W2.ISSUEDATE <= '" .$eday. "' ";
        $query .= "                                                         AND (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '" .$sday. "') AND ";
        $query .= "                                                         W2.SCHREGNO IN ".$schregno. " ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         SCHREGNO ";
        $query .= "                                                     ) ";
        $query .= "                 )DB3 ON DB3.SCHREGNO = DB1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     DB1.YEAR     = '" .$model->control["年度"]. "' ";
        $query .= "      AND DB1.SEMESTER = '" .$model->field["GAKKI"]. "' ";
        $query .= "      AND DB1.SCHREGNO IN " .$schregno. " ";
        $query .= "      AND DB1.COURSECD || DB1.MAJORCD = '" .$model->field["COURSE_MAJOR_NAME"]. "' ";
        $query .= "      AND DB1.COURSECODE = '" .$model->field["COURSECODE"]. "' ";
        $query .= " ORDER BY DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ";

        return $query;
    }

}
?>
