<?php

require_once('for_php7.php');

class knjd123xQuery extends Query {

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
        $query .= "     PROGRAMID = 'KNJD123X' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '". CTRL_YEAR ."'";
            $query .= "     AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "     AND GRADE    = '".$model->field["GRADE"] ."'";
        }
        //参照・更新可（制限付き）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR='". CTRL_YEAR ."' ";
            $query .= "     AND SEMESTER='". CTRL_SEMESTER ."' ";
            $query .= "     AND GRADE='".$model->field["GRADE"] ."'";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "       OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "       OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "    GRADE || '学年' AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER ."' ";
        //参照・更新可（制限付き）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //SCHOOL_MST
    function getSchoolMst() {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($grade_hr_class, $model) {
        $year = CTRL_YEAR;

        $query  = " WITH SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' AND ";
        $query .= "         GRADE || HR_CLASS = '{$grade_hr_class}' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";

        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         TT1.YEAR, ";
        $query .= "         TT1.SEMESTER, ";
        $query .= "         TT1.SCHREGNO, ";
        $query .= "         TT1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         TT1.SCHOOL_KIND, ";
            $query .= "         TT1.CURRICULUM_CD, ";
        }
        $query .= "         TT1.SUBCLASSCD, ";
        $query .= "         SUM(TT1.LESSON) AS LESSON, ";
        $query .= "         SUM(TT1.OFFDAYS) AS OFFDAYS, ";
        $query .= "         SUM(TT1.ABSENT) AS ABSENT, ";
        $query .= "         SUM(TT1.SUSPEND) AS SUSPEND, ";
        $query .= "         SUM(TT1.MOURNING) AS MOURNING, ";
        if ($model->virus) {
            $query .= "         SUM(TT1.VIRUS) AS VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "         SUM(TT1.KOUDOME) AS KOUDOME, ";
        }
        $query .= "         SUM(TT1.LATE) AS LATE, ";
        $query .= "         SUM(TT1.EARLY) AS EARLY, ";
        $query .= "         SUM(TT1.SICK) AS SICK, ";
        $query .= "         SUM(TT1.NOTICE) AS NOTICE, ";
        $query .= "         SUM(TT1.NONOTICE) AS NONOTICE, ";
        $query .= "         SUM(TT1.NURSEOFF) AS NURSEOFF ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT TT1 ";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG LL1 ON  LL1.YEAR = TT1.YEAR ";
        $query .= "                    AND LL1.SCHREGNO = TT1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         TT1.COPYCD = '0' ";
        $query .= "     GROUP BY ";
        $query .= "         TT1.YEAR, ";
        $query .= "         TT1.SEMESTER, ";
        $query .= "         TT1.SCHREGNO, ";
        $query .= "         TT1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         TT1.SCHOOL_KIND, ";
            $query .= "         TT1.CURRICULUM_CD, ";
        }
        $query .= "         TT1.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L8.NAME_SHOW, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L5.SUBCLASSNAME, ";
        $query .= "     L1.CHAIRCD, ";
        $query .= "     L6.CHAIRNAME, ";
        $query .= "     L1.SEM1_INTR_VALUE, ";
        $query .= "     L1.SEM1_INTR_VALUE_DI, ";
        $query .= "     L1.SEM1_VALUE, ";
        $query .= "     L1.SEM1_VALUE_DI, ";
        $query .= "     L1.SEM2_INTR_VALUE, ";
        $query .= "     L1.SEM2_INTR_VALUE_DI, ";
        $query .= "     L1.SEM2_VALUE, ";
        $query .= "     L1.SEM2_VALUE_DI, ";
        $query .= "     L1.GRAD_VALUE, ";
        $query .= "     L1.GRAD_VALUE_DI, ";
        $query .= "     L1.COMP_CREDIT, ";
        $query .= "     L1.GET_CREDIT, ";
        $query .= "     L7.CREDITS, ";

        $query .= "     L2.LESSON   AS LESSON_1, ";
        $query .= "     L2.OFFDAYS  AS OFFDAYS_1, ";
        $query .= "     L2.ABSENT   AS ABSENT_1, ";
        $query .= "     L2.SUSPEND  AS SUSPEND_1, ";
        $query .= "     L2.MOURNING AS MOURNING_1, ";
        if ($model->virus) {
            $query .= "     L2.VIRUS  AS VIRUS_1, ";
        }
        if ($model->koudome) {
            $query .= "     L2.KOUDOME  AS KOUDOME_1, ";
        }
        $query .= "     L2.LATE     AS LATE_1, ";
        $query .= "     L2.EARLY    AS EARLY_1, ";
        $query .= "     L2.SICK     AS SICK_1, ";
        $query .= "     L2.NOTICE   AS NOTICE_1, ";
        $query .= "     L2.NONOTICE AS NONOTICE_1, ";
        $query .= "     L2.NURSEOFF AS NURSEOFF_1, ";

        $query .= "     L3.LESSON   AS LESSON_2, ";
        $query .= "     L3.OFFDAYS  AS OFFDAYS_2, ";
        $query .= "     L3.ABSENT   AS ABSENT_2, ";
        $query .= "     L3.SUSPEND  AS SUSPEND_2, ";
        $query .= "     L3.MOURNING AS MOURNING_2, ";
        if ($model->virus) {
            $query .= "     L3.VIRUS  AS VIRUS_2, ";
        }
        if ($model->koudome) {
            $query .= "     L3.KOUDOME  AS KOUDOME_2, ";
        }
        $query .= "     L3.LATE     AS LATE_2, ";
        $query .= "     L3.EARLY    AS EARLY_2, ";
        $query .= "     L3.SICK     AS SICK_2, ";
        $query .= "     L3.NOTICE   AS NOTICE_2, ";
        $query .= "     L3.NONOTICE AS NONOTICE_2, ";
        $query .= "     L3.NURSEOFF AS NURSEOFF_2, ";

        $query .= "     L4.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     RECORD_DAT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                   AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND L2 ON  L2.YEAR       = T1.YEAR ";
        $query .= "               AND L2.SEMESTER   = '1' ";
        $query .= "               AND L2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "               AND L2.SUBCLASSCD = L1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND L2.CLASSCD = L1.CLASSCD ";
            $query .= "               AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "               AND L2.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND L3 ON  L3.YEAR       = T1.YEAR ";
        $query .= "               AND L3.SEMESTER   = '2' ";
        $query .= "               AND L3.SCHREGNO   = T1.SCHREGNO ";
        $query .= "               AND L3.SUBCLASSCD = L1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND L3.CLASSCD = L1.CLASSCD ";
            $query .= "               AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "               AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     RECORD_REMARK_DAT L4 ON  L4.YEAR       = T1.YEAR ";
        $query .= "                          AND L4.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "                          AND L4.SCHREGNO   = T1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND L4.CLASSCD = L1.CLASSCD ";
            $query .= "                      AND L4.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "                      AND L4.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L5 ON L5.SUBCLASSCD = L1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND L5.CLASSCD = L1.CLASSCD ";
            $query .= "                      AND L5.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "                      AND L5.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     CHAIR_DAT L6 ON  L6.YEAR     = T1.YEAR ";
        $query .= "                  AND L6.SEMESTER = '1' ";
        $query .= "                  AND L6.CHAIRCD  = L1.CHAIRCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CREDIT_MST L7 ON  L7.YEAR       = T1.YEAR ";
        $query .= "                   AND L7.COURSECD   = T1.COURSECD ";
        $query .= "                   AND L7.MAJORCD    = T1.MAJORCD ";
        $query .= "                   AND L7.GRADE      = T1.GRADE ";
        $query .= "                   AND L7.COURSECODE = T1.COURSECODE ";
        $query .= "                   AND L7.SUBCLASSCD = L1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND L7.CLASSCD = L1.CLASSCD ";
            $query .= "                      AND L7.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "                      AND L7.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L8 ON L8.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L1.CHAIRCD ";

        return $query;
    }
}
?>
