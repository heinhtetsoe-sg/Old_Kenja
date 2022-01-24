<?php

require_once('for_php7.php');


class knje371dQuery extends Query
{

    //学校取得 ※'00003602','00003603' 固定
    public function getSchool($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_CD AS VALUE, ";
        //$query .= "     T1.SCHOOL_CD || ' ' || SCHOOL_NAME AS LABEL ";
        $query .= "     T1.SCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD IN ('00003602','00003603')";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学部取得
    public function getFaculty($model, $school_cd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FACULTYCD AS VALUE, ";
        $query .= "     T1.FACULTYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '".$school_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 学科取得
    public function getDepartment($model, $school_cd, $facultycd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.DEPARTMENTCD AS VALUE, ";
        $query .= "     T1.DEPARTMENTNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '".$school_cd."' AND";
        $query .= "     T1.FACULTYCD = '".$facultycd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
