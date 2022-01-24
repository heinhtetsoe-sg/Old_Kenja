<?php

require_once('for_php7.php');

class knje373cQuery extends Query
{
    //卒業学年の取得
    public function getGraduateGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " AND NAME1   = '".$model->schoolKind."' ";
        
        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1='Z010' AND ";
        $query .= "     NAMECD2='00' ";
        return $query;
    }

    //CSV出力
    public function selectCsvQuery($model)
    {
        $query  = " WITH COLLEGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOL_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         FACULTYNAME ";
        $query .= "     FROM ";
        $query .= "         COLLEGE_FACULTY_MST ";
        $query .= " ), ";
        $query .= " DEPARTMENT AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLLEGE_FACULTY_MST.SCHOOL_CD, ";
        $query .= "         COLLEGE_FACULTY_MST.FACULTYCD, ";
        $query .= "         COLLEGE_DEPARTMENT_MST.DEPARTMENTCD, ";
        $query .= "         COLLEGE_DEPARTMENT_MST.DEPARTMENTNAME ";
        $query .= "     FROM ";
        $query .= "         COLLEGE_DEPARTMENT_MST ";
        $query .= "         INNER JOIN COLLEGE_FACULTY_MST ON ";
        $query .= "                    COLLEGE_FACULTY_MST.SCHOOL_CD = COLLEGE_DEPARTMENT_MST.SCHOOL_CD ";
        $query .= "                AND COLLEGE_FACULTY_MST.FACULTYCD = COLLEGE_DEPARTMENT_MST.FACULTYCD ";
        $query .= " ), ";
        $query .= " AFT_SCH_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(SEQ) AS SEQ ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT ";
        $query .= "     WHERE ";
        $query .= "         SENKOU_KIND = '0' ";
        $query .= "     AND STAT_CD     = '".$model->schoolCd."' ";
        $query .= "     AND PLANSTAT    = '1' "; // 1:決定
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO ";
        $query .= " ), ";
        $query .= " AFT_SCH AS ( ";
        $query .= "     SELECT ";
        $query .= "         AFT_DAT.YEAR, ";
        $query .= "         AFT_DAT.SCHREGNO, ";
        $query .= "         AFT_DAT.STAT_CD, ";
        $query .= "         AFT_DAT.FACULTYCD, ";
        $query .= "         AFT_DAT.DEPARTMENTCD ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT AFT_DAT ";
        $query .= "         INNER JOIN AFT_SCH_MAX ON ";
        $query .= "                    AFT_SCH_MAX.YEAR     = AFT_DAT.YEAR ";
        $query .= "                AND AFT_SCH_MAX.SEQ      = AFT_DAT.SEQ ";
        $query .= "                AND AFT_SCH_MAX.SCHREGNO = AFT_DAT.SCHREGNO ";
        $query .= " ), ";
        $query .= " MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         VALUE(AFT_SCH.FACULTYCD, 'ZZZZ') AS FACULTYCD, ";
        $query .= "         CASE WHEN AFT_SCH.SCHREGNO IS NOT NULL THEN COLLEGE.FACULTYNAME ELSE '非推薦' END AS FACULTYNAME, ";
        $query .= "         DAT.COURSECD, ";
        $query .= "         DAT.MAJORCD, ";
        $query .= "         DAT.SCHREGNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         VALUE(AFT_SCH.DEPARTMENTCD, 'ZZZZ') AS DEPARTMENTCD, ";
        $query .= "         CASE WHEN AFT_SCH.SCHREGNO IS NOT NULL THEN DEPARTMENT.DEPARTMENTNAME ELSE '' END AS DEPARTMENTNAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT DAT ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT HDAT ON ";
        $query .= "                   HDAT.YEAR     = DAT.YEAR ";
        $query .= "               AND HDAT.SEMESTER = DAT.SEMESTER ";
        $query .= "               AND HDAT.GRADE    = DAT.GRADE ";
        $query .= "               AND HDAT.HR_CLASS = DAT.HR_CLASS ";
        $query .= "         LEFT JOIN AFT_SCH ON ";
        $query .= "                   AFT_SCH.YEAR     = DAT.YEAR ";
        $query .= "               AND AFT_SCH.SCHREGNO = DAT.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ON ";
        $query .= "                   BASE.SCHREGNO = DAT.SCHREGNO ";
        $query .= "         LEFT JOIN COLLEGE ON ";
        $query .= "                   COLLEGE.SCHOOL_CD = AFT_SCH.STAT_CD ";
        $query .= "               AND COLLEGE.FACULTYCD = AFT_SCH.FACULTYCD ";
        $query .= "         LEFT JOIN DEPARTMENT ON ";
        $query .= "                   DEPARTMENT.SCHOOL_CD    = AFT_SCH.STAT_CD ";
        $query .= "               AND DEPARTMENT.FACULTYCD    = AFT_SCH.FACULTYCD ";
        $query .= "               AND DEPARTMENT.DEPARTMENTCD = AFT_SCH.DEPARTMENTCD ";
        $query .= "     WHERE ";
        $query .= "         DAT.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         DAT.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         DAT.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ), ";
        $query .= " FACULTY_MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         FACULTYCD, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD ";
        $query .= "     FROM ";
        $query .= "         MAIN ";
        $query .= "     GROUP BY ";
        $query .= "         FACULTYCD, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD ";
        $query .= " ), ";
        $query .= " MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         FACULTY_MAJOR.FACULTYCD, ";
        $query .= "         LISTAGG(MAJOR.MAJORNAME, '、') WITHIN GROUP (ORDER BY MAJOR.MAJORCD) AS MAJORNAME ";
        $query .= "     FROM ";
        $query .= "         FACULTY_MAJOR ";
        $query .= "         LEFT JOIN MAJOR_MST MAJOR ON ";
        $query .= "                   MAJOR.COURSECD = FACULTY_MAJOR.COURSECD ";
        $query .= "               AND MAJOR.MAJORCD  = FACULTY_MAJOR.MAJORCD ";
        $query .= "     GROUP BY ";
        $query .= "         FACULTY_MAJOR.FACULTYCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAJOR.MAJORNAME, ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.NAME, ";
        $query .= "     MAIN.NAME_KANA, ";
        $query .= "     MAIN.FACULTYNAME, ";
        $query .= "     MAIN.DEPARTMENTNAME, ";
        $query .= "     '' AS SENSYU ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= "     LEFT JOIN MAJOR ON MAJOR.FACULTYCD = MAIN.FACULTYCD ";
        $query .= " ORDER BY ";
        $query .= "     MAIN.FACULTYCD, ";
        $query .= "     MAIN.DEPARTMENTCD, ";
        $query .= "     MAIN.SCHREGNO ";

        return $query;
    }
}
