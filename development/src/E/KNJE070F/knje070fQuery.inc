<?php

require_once('for_php7.php');

class knje070fQuery extends Query {

    //大学取得
    function getUniversity($year) {
        $query  = " SELECT ";
        $query .= "     STAT.SCHOOL_CD AS VALUE, ";
        $query .= "     STAT.SCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN NAME_MST E002  ON E002.NAMECD1 = 'E002' ";
        $query .= "                             AND E002.NAMECD2 = AFT.HOWTOEXAM ";
        $query .= "     LEFT JOIN COLLEGE_MST STAT ON STAT.SCHOOL_CD = AFT.STAT_CD ";
        $query .= " WHERE ";
        $query .= "         AFT.YEAR        = '".$year."' ";
        $query .= "     AND E002.NAMESPARE3 = '1' ";
        $query .= " GROUP BY ";
        $query .= "     STAT.SCHOOL_CD, ";
        $query .= "     STAT.SCHOOL_NAME ";
        $query .= " ORDER BY ";
        $query .= "     STAT.SCHOOL_CD ";

        return $query;
    }

    //学部取得
    function getFacultyDepartment($year, $statCd) {
        $query  = " SELECT ";
        $query .= "     value(FACU.FACULTYCD, '') || '-' || value(DEPA.DEPARTMENTCD, '') AS VALUE, ";
        $query .= "     case ";
        $query .= "         when value(FACU.FACULTYNAME, '') = '' then value(DEPA.DEPARTMENTNAME, '') ";
        $query .= "         when value(DEPA.DEPARTMENTNAME, '') = '' then value(FACU.FACULTYNAME, '') ";
        $query .= "         else value(FACU.FACULTYNAME, '') || '・' || value(DEPA.DEPARTMENTNAME, '') ";
        $query .= "     end AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN NAME_MST E002  ON E002.NAMECD1 = 'E002' ";
        $query .= "                             AND E002.NAMECD2 = AFT.HOWTOEXAM ";
        $query .= "     LEFT JOIN COLLEGE_MST STAT ON STAT.SCHOOL_CD = AFT.STAT_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST FACU ON FACU.SCHOOL_CD = AFT.STAT_CD ";
        $query .= "                                       AND FACU.FACULTYCD = AFT.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST DEPA ON DEPA.SCHOOL_CD    = AFT.STAT_CD ";
        $query .= "                                          AND DEPA.FACULTYCD    = AFT.FACULTYCD ";
        $query .= "                                          AND DEPA.DEPARTMENTCD = AFT.DEPARTMENTCD ";
        $query .= " WHERE ";
        $query .= "         AFT.YEAR    = '".$year."' ";
        $query .= "     AND AFT.STAT_CD = '".$statCd."' ";
        $query .= "     AND E002.NAMESPARE3 = '1' ";
        $query .= " GROUP BY ";
        $query .= "     FACU.FACULTYCD, ";
        $query .= "     DEPA.DEPARTMENTCD, ";
        $query .= "     FACU.FACULTYNAME, ";
        $query .= "     DEPA.DEPARTMENTNAME ";
        $query .= " ORDER BY ";
        $query .= "     value(FACU.FACULTYCD, '000'), ";
        $query .= "     value(DEPA.DEPARTMENTCD, '000') ";

        return $query;
    }

    //生徒一覧
    function getList($model, $schregno="") {
        $query  = " WITH AFT_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         AFT.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT AFT ";
        $query .= "         LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' ";
        $query .= "                                AND E002.NAMECD2 = AFT.HOWTOEXAM ";
        $query .= "     WHERE ";
        $query .= "             AFT.YEAR    = '".CTRL_YEAR."' ";
        $query .= "         AND AFT.STAT_CD = '".$model->field["STAT_CD"]."' ";
        $query .= "         AND value(AFT.FACULTYCD, '') || '-' || value(AFT.DEPARTMENTCD, '') = '".$model->field["FACULTY_DEPARTMENT"]."' ";
        $query .= "         AND E002.NAMESPARE3 = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         AFT.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     REGD.GRADE || '-' || REGD.HR_CLASS || '-' || REGD.ATTENDNO || '-' || REGD.SCHREGNO AS VALUE, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        if ($schregno == "") {
            $query .= "     INNER JOIN AFT_DAT AFTD ON BASE.SCHREGNO = AFTD.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($schregno) {
            $query .= "     AND BASE.SCHREGNO IN ('".implode("','", $schregno)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    function getSchoolDiv($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //近大チェック
    function checkKindai() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     UPPER(NAME1) IN ('KINDAI', 'KINJUNIOR') ";

        return $query;
    }

    //職員一覧取得
    function getStaffList() {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || T1.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST AS T1, ";
        $query .= "     STAFF_YDAT AS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = T2.STAFFCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
