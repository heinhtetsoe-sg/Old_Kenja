<?php

require_once('for_php7.php');


class knje150aQuery extends Query {

    //学習の記録
    function getStudyrecCsv($model) {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE BETWEEN '04' AND '06' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
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
        $query .= "     CASE WHEN L3.ELECTDIV = '1' ";
        $query .= "          THEN D001.NAME1 ";
        $query .= "          ELSE rtrim(char(T3.VALUATION)) ";
        $query .= "     END AS VALUATION, ";
        $query .= "     T3.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_STUDYREC_DAT T3 ON T3.YEAR < '".CTRL_YEAR."' ";
        $query .= "           AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND substr(T3.SUBCLASSCD, 1, 2) NOT IN ('31','33','39') ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND L1.CLASSCD = T3.CLASSCD ";
            $query .= "                              AND L1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                              AND L1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = substr(T3.SUBCLASSCD, 1, 2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND L3.SCHOOL_KIND = T3.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST D001 ON D001.NAMECD1 = 'D001' ";
        $query .= "           AND smallint(D001.NAMECD2) = T3.VALUATION ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.MAX_SEM ";
        $query .= "     ) ";
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

    //観点の記録
    function getJviewCsv($model) {
        $query  = "";
        $query .= " WITH REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN JVIEWSTAT_DAT T3 ON T3.YEAR < '".CTRL_YEAR."' ";
        $query .= "           AND T3.SEMESTER = '9' ";
        $query .= "           AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWNAME_MST L1 ON L1.VIEWCD = T3.VIEWCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN BEHAVIOR_DAT T3 ON T3.YEAR < '".CTRL_YEAR."' ";
        $query .= "           AND T3.DIV IN ('1', '2') ";
        $query .= "           AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     L3.CLASSNAME, ";
        $query .= "     T3.VIEWCD, ";
        $query .= "     CASE WHEN substr(T3.VIEWCD, 3, 2) = '99' ";
        $query .= "          THEN '評定' ";
        $query .= "          ELSE L1.VIEWNAME ";
        $query .= "     END AS VIEWNAME, ";
        $query .= "     T3.STATUS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWSTAT_DAT T3 ON T3.YEAR < '".CTRL_YEAR."' ";
        $query .= "           AND T3.YEAR = T1.YEAR ";
        $query .= "           AND T3.SEMESTER = '9' ";
        $query .= "           AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWNAME_MST L1 ON L1.VIEWCD = T3.VIEWCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "             AND L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND L2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = substr(T3.VIEWCD, 1, 2) ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.MAX_SEM ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T3.VIEWCD ";
        return $query;
    }

    //行動の記録
    function getBehaviorCsv($model) {
        $query  = "";
        $query .= " WITH REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN BEHAVIOR_DAT T3 ON T3.YEAR < '".CTRL_YEAR."' ";
        $query .= "           AND T3.DIV IN ('1', '2') ";
        $query .= "           AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO, ";
        $query .= "     T3.DIV || T3.CODE AS CODE, ";
        $query .= "     T3.RECORD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN BEHAVIOR_DAT T3 ON T3.YEAR < '".CTRL_YEAR."' ";
        $query .= "           AND T3.YEAR = T1.YEAR ";
        $query .= "           AND T3.DIV IN ('1', '2') ";
        $query .= "           AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "             AND L2.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND L2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.MAX_SEM ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     L2.GRADE, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T3.CODE ";
        return $query;
    }

    //出欠の記録
    function getAttendrecCsv($model) {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE BETWEEN '04' AND '06' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
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
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.MAX_SEM ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.ANNUAL ";
        return $query;
    }

}
?>
