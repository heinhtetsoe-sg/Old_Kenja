<?php

require_once('for_php7.php');


class knje150Query extends Query {

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
        $query .= "     PROGRAMID = 'KNJE150' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学習の記録
    function getStudyrecCsv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     T3.VALUATION, ";
        $query .= "     T3.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND L1.CLASSCD = T3.CLASSCD ";
            $query .= "                              AND L1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                              AND L1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";
        return $query;
    }

    //出欠の記録
    function getAttendrecCsv($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     VALUE(T3.CLASSDAYS,0) - VALUE(T3.OFFDAYS,0) - VALUE(T3.ABROAD,0) AS LESSON, ";
        $query .= "     VALUE(T3.SUSPEND,0) + VALUE(T3.MOURNING,0) AS SUSPEND_MOURNING, ";
        $query .= "     VALUE(T3.ABROAD,0) AS ABROAD, ";
        $query .= "     VALUE(T3.REQUIREPRESENT,0) AS REQUIREPRESENT, ";
        $query .= "     VALUE(T3.PRESENT,0) AS PRESENT, ";
        $query .= "     VALUE(T3.SICK,0) + VALUE(T3.ACCIDENTNOTICE,0) + VALUE(T3.NOACCIDENTNOTICE,0) AS ABSENT, ";
        $query .= "     '' AS LATE, ";
        $query .= "     '' AS EARLY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_ATTENDREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.ANNUAL ";
        return $query;
    }

}
?>
