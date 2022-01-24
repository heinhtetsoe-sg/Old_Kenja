<?php

require_once('for_php7.php');

class knje152Query extends Query
{

    //学年取得
    public function getGrade($model, $grade = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.GRADE    = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        if ($grade) {
            $query .= " AND T1.GRADE    = '".$grade."' ";
        }
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   T3.TR_CD1       = '".STAFFCD."' ";
            $query .= "      OR T3.TR_CD2       = '".STAFFCD."' ";
            $query .= "      OR T3.TR_CD3       = '".STAFFCD."' ";
            $query .= "      OR T3.SUBTR_CD1    = '".STAFFCD."' ";
            $query .= "      OR T3.SUBTR_CD2    = '".STAFFCD."' ";
            $query .= "      OR T3.SUBTR_CD3    = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校コード取得
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //高セキュリティー
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE152' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //教科マスタ取得
    public function getClassMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     CASE WHEN T2.CLASSORDERNAME1 IS NOT NULL THEN T2.CLASSORDERNAME1 ";
        $query .= "          WHEN T2.CLASSNAME IS NOT NULL THEN T2.CLASSNAME ";
        $query .= "          WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ";
        $query .= "          ELSE T3.CLASSNAME END AS CLASSNAME, ";
        $query .= "     T2.SHOWORDER2 ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,SCHOOL_KIND ";
        }
        $query .= "     FROM ";
        $query .= "         CLASS_MST ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,SCHOOL_KIND ";
        }
        $query .= "     FROM ";
        $query .= "         ANOTHER_CLASS_MST) T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN ANOTHER_CLASS_MST T3 ON T1.CLASSCD = T3.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                   AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.SHOWORDER2, ";
        $query .= "     T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     ,T1.SCHOOL_KIND ";
        }

        return $query;
    }

    //評定平均
    public function getStudyrecCsv($model)
    {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW, ";
        $query .= "         T3.SCHOOLCD, ";
        $query .= "         T3.YEAR, ";
        $query .= "         T3.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         CASE WHEN L1.SUBCLASSCD2 IS NULL THEN T3.SUBCLASSCD ";
        $query .= "              ELSE L1.SUBCLASSCD2 END AS SUBCLASSCD, ";
        $query .= "         T3.VALUATION, ";
        $query .= "         CASE WHEN T3.GET_CREDIT = 0 THEN (CASE WHEN T3.COMP_CREDIT <> 0 THEN T3.COMP_CREDIT ";
        $query .= "                                                ELSE L3.CREDITS END) ";
        $query .= "              ELSE T3.GET_CREDIT END AS GET_CREDIT, ";
        $query .= "         T3.ADD_CREDIT, ";
        $query .= "         L2.GVAL_CALC ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD      = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND L1.CLASSCD         = T3.CLASSCD ";
            $query .= "                                  AND L1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                                  AND L1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN SCHOOL_MST L2 ON L2.YEAR = T3.YEAR ";
        $query .= "         LEFT JOIN CREDIT_MST L3 ON L3.YEAR          = T3.YEAR ";
        $query .= "                                AND L3.COURSECD      = T1.COURSECD ";
        $query .= "                                AND L3.MAJORCD       = T1.MAJORCD ";
        $query .= "                                AND L3.GRADE         = T1.GRADE ";
        $query .= "                                AND L3.COURSECODE    = T1.COURSECODE ";
        $query .= "                                AND L3.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query .= "                                AND L3.CLASSCD       = T3.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                AND L3.SCHOOL_KIND   = T3.SCHOOL_KIND ";
            $query .= "                                AND L3.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ), GVAL_CALC0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,1),0),5,0) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '0' AND ";
        $query .= "         GVAL_CALC   = '0' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= " ), GVAL_CALC1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         INTEGER(ROUND(DECIMAL(FLOAT(SUM(VALUATION * (CASE WHEN ADD_CREDIT IS NOT NULL THEN GET_CREDIT + ADD_CREDIT ELSE GET_CREDIT END))) / SUM((CASE WHEN ADD_CREDIT IS NOT NULL THEN GET_CREDIT + ADD_CREDIT ELSE GET_CREDIT END)),5,1),0)) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '0' AND ";
        $query .= "         GVAL_CALC   = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= " ), GVAL_CALC2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         MAX(VALUATION) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '0' AND ";
        $query .= "         GVAL_CALC   = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        //前籍校をGVAL_CALCごとにカウント
        $query .= " ), ZENSEKI_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             SCHREGNO, ";
        $query .= "             CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             GVAL_CALC ";
        $query .= "         FROM ";
        $query .= "             BASE ";
        $query .= "         WHERE ";
        $query .= "             SCHOOLCD = '1' ";
        $query .= "         ) T1 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        //前籍校成績
        $query .= " ), ZENSEKI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(T1.VALUATION)),5,1),0),5,0) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT = 1 AND ";
        $query .= "         T1.GVAL_CALC = '0' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         INTEGER(ROUND(DECIMAL(FLOAT(SUM(T1.VALUATION * (CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.GET_CREDIT + T1.ADD_CREDIT ELSE T1.GET_CREDIT END))) / SUM((CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.GET_CREDIT + T1.ADD_CREDIT ELSE T1.GET_CREDIT END)),5,1),0)) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT = 1 AND ";
        $query .= "         T1.GVAL_CALC = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         MAX(T1.VALUATION) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT = 1 AND ";
        $query .= "         T1.GVAL_CALC = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.VALUATION ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT > 1 ";
        $query .= " ), HYOTEI AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GVAL_CALC0 ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GVAL_CALC1 ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GVAL_CALC2 ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         '1' AS SCHOOLCD, ";
        $query .= "         '入学前' AS YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         VALUATION ";
        $query .= "     FROM ";
        $query .= "         ZENSEKI ";
        $query .= " ), HYOTEI_AVG AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
        }
        $query .= "         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,2),1),5,1) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         HYOTEI ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,SCHOOL_KIND ";
        }
        $query .= " ), HYOTEI_TOTAL_AVG AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,2),1),5,1) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         HYOTEI ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW ";
        $query .= " ), SCH_CLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW, ";
        $query .= "         T3.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,T3.SCHOOL_KIND ";
        }
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "        (SELECT ";
        $query .= "             CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            ,SCHOOL_KIND ";
        }
        $query .= "         FROM ";
        $query .= "             CLASS_MST ";
        $query .= "         UNION   ";
        $query .= "         SELECT ";
        $query .= "             CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            ,SCHOOL_KIND ";
        }
        $query .= "         FROM ";
        $query .= "             ANOTHER_CLASS_MST) T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.VALUATION ";
        $query .= "     FROM ";
        $query .= "         SCH_CLASS T1 ";
        $query .= "         LEFT JOIN HYOTEI_AVG T2 ON T1.SCHREGNO      = T2.SCHREGNO ";
        $query .= "                                AND T1.CLASSCD       = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        }
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         '999' AS CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         'Z' AS SCHOOL_KIND, ";
        }
        $query .= "         VALUATION ";
        $query .= "     FROM ";
        $query .= "         HYOTEI_TOTAL_AVG ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
        } else {
            $query .= "     T1.CLASSCD AS CLASSCD, ";
        }
        $query .= "     T1.VALUATION, ";
        $query .= "     CASE WHEN L1.SHOWORDER2 IS NOT NULL THEN L1.SHOWORDER2 ";
        $query .= "          ELSE 100 END AS CLASS_ORDER ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD        = L1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T1.SCHOOL_KIND    = L1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.CLASSCD != '999' ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME_SHOW, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
        } else {
            $query .= "     CLASSCD AS CLASSCD, ";
        }
        $query .= "     VALUATION, ";
        $query .= "     999 AS CLASS_ORDER ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     CLASSCD = '999' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     CLASS_ORDER, ";
        $query .= "     CLASSCD ";

        return $query;
    }

    //科目別成績詳細情報
    public function getStudyrecCsv2($model)
    {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW, ";
        $query .= "         T3.SCHOOLCD, ";
        $query .= "         T3.YEAR, ";
        $query .= "         T3.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         CASE WHEN L1.SUBCLASSCD2 IS NULL THEN T3.SUBCLASSCD ";
        $query .= "              ELSE L1.SUBCLASSCD2 END AS SUBCLASSCD, ";
        $query .= "         T3.SUBCLASSNAME, ";
        $query .= "         T3.VALUATION, ";
        $query .= "         CASE WHEN T3.GET_CREDIT = 0 THEN (CASE WHEN T3.COMP_CREDIT <> 0 THEN T3.COMP_CREDIT ";
        $query .= "                                                ELSE L3.CREDITS END) ";
        $query .= "              ELSE T3.GET_CREDIT END AS CREDIT, ";
        $query .= "         T3.GET_CREDIT, ";
        $query .= "         T3.ADD_CREDIT, ";
        $query .= "         T3.COMP_CREDIT, ";
        $query .= "         L2.GVAL_CALC ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD      = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND L1.CLASSCD         = T3.CLASSCD ";
            $query .= "                                  AND L1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                                  AND L1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN SCHOOL_MST L2 ON L2.YEAR = T3.YEAR ";
        $query .= "         LEFT JOIN CREDIT_MST L3 ON L3.YEAR          = T3.YEAR ";
        $query .= "                                AND L3.COURSECD      = T1.COURSECD ";
        $query .= "                                AND L3.MAJORCD       = T1.MAJORCD ";
        $query .= "                                AND L3.GRADE         = T1.GRADE ";
        $query .= "                                AND L3.COURSECODE    = T1.COURSECODE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ), GVAL_CALC0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME, ";
        $query .= "         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(VALUATION)),5,1),0),5,0) AS VALUATION, ";
        $query .= "         SUM(GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "         SUM(COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '0' AND ";
        $query .= "         GVAL_CALC   = '0' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME ";
        $query .= " ), GVAL_CALC1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME, ";
        $query .= "         INTEGER(ROUND(DECIMAL(FLOAT(SUM(VALUATION * (CASE WHEN ADD_CREDIT IS NOT NULL THEN CREDIT + ADD_CREDIT ELSE CREDIT END))) / SUM((CASE WHEN ADD_CREDIT IS NOT NULL THEN CREDIT + ADD_CREDIT ELSE CREDIT END)),5,1),0)) AS VALUATION, ";
        $query .= "         SUM(GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "         SUM(COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '0' AND ";
        $query .= "         GVAL_CALC   = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME ";
        $query .= " ), GVAL_CALC2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME, ";
        $query .= "         MAX(VALUATION) AS VALUATION, ";
        $query .= "         SUM(GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "         SUM(COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '0' AND ";
        $query .= "         GVAL_CALC   = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME ";
        //前籍校をGVAL_CALCごとにカウント
        $query .= " ), ZENSEKI_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        $query .= "             SCHREGNO, ";
        $query .= "             CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             GVAL_CALC ";
        $query .= "         FROM ";
        $query .= "             BASE ";
        $query .= "         WHERE ";
        $query .= "             SCHOOLCD = '1' ";
        $query .= "         ) T1 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        //前籍校成績
        $query .= " ), ZENSEKI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME, ";
        $query .= "         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(T1.VALUATION)),5,1),0),5,0) AS VALUATION, ";
        $query .= "         SUM(T1.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "         SUM(T1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT = 1 AND ";
        $query .= "         T1.GVAL_CALC = '0' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME, ";
        $query .= "         INTEGER(ROUND(DECIMAL(FLOAT(SUM(T1.VALUATION * (CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.CREDIT + T1.ADD_CREDIT ELSE T1.CREDIT END))) / SUM((CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN T1.CREDIT + T1.ADD_CREDIT ELSE T1.CREDIT END)),5,1),0)) AS VALUATION, ";
        $query .= "         SUM(T1.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "         SUM(T1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT = 1 AND ";
        $query .= "         T1.GVAL_CALC = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME, ";
        $query .= "         MAX(T1.VALUATION) AS VALUATION, ";
        $query .= "         SUM(T1.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "         SUM(T1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                 AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                 AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                 AND T1.SCHOOLCD     = '1' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T2.CNT = 1 AND ";
        $query .= "         T1.GVAL_CALC = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME, ";
        $query .= "         MAX(T1.VALUATION) AS VALUATION, ";
        $query .= "         T2.GET_CREDIT, ";
        $query .= "         T2.ADD_CREDIT, ";
        $query .= "         T2.COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE T1, ";
        $query .= "         (SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
        }
        $query .= "             T1.SUBCLASSCD, ";
        $query .= "             MAX(T1.GET_CREDIT) AS MAX_CREDIT, ";
        $query .= "             SUM(T1.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "             SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "             SUM(T1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "         FROM ";
        $query .= "             BASE T1 ";
        $query .= "             LEFT JOIN ZENSEKI_CNT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "                                     AND T1.CLASSCD      = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                                     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                     AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                                     AND T1.SCHOOLCD     = '1' ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHOOLCD = '1' AND ";
        $query .= "             T2.CNT > 1 ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
        }
        $query .= "             T1.SUBCLASSCD ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO         = T2.SCHREGNO AND ";
        $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.SUBCLASSCD       = T2.SUBCLASSCD AND ";
        $query .= "         T1.GET_CREDIT       = T2.MAX_CREDIT ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.SUBCLASSNAME, ";
        $query .= "         T2.GET_CREDIT, ";
        $query .= "         T2.ADD_CREDIT, ";
        $query .= "         T2.COMP_CREDIT ";
        //留学単位数
        $query .= " ), TRANSFER AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         '' AS SCHOOLCD, ";
        $query .= "         '9999' AS YEAR, ";
        $query .= "         '' AS CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         '' AS SCHOOL_KIND, ";
            $query .= "         '' AS CURRICULUM_CD, ";
        }
        $query .= "         '' AS SUBCLASSCD, ";
        $query .= "         '留学単位数' AS SUBCLASSNAME, ";
        $query .= "         0 AS VALUATION, ";
        $query .= "         T2.ABROAD_CREDITS AS GET_CREDIT, ";
        $query .= "         0 AS ADD_CREDIT, ";
        $query .= "         T2.ABROAD_CREDITS AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         BASE T1, ";
        $query .= "         (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             SUM(ABROAD_CREDITS) AS ABROAD_CREDITS ";
        $query .= "         FROM ";
        $query .= "             SCHREG_TRANSFER_DAT ";
        $query .= "         WHERE ";
        $query .= "             TRANSFERCD = '1' ";     //留学
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GVAL_CALC0 ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GVAL_CALC1 ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         GVAL_CALC2 ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME_SHOW, ";
        $query .= "         '1' AS SCHOOLCD, ";
        $query .= "         '入学前' AS YEAR, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUBCLASSNAME, ";
        $query .= "         VALUATION, ";
        $query .= "         GET_CREDIT, ";
        $query .= "         ADD_CREDIT, ";
        $query .= "         COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         ZENSEKI ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         TRANSFER ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     CASE WHEN T1.SUBCLASSNAME IS NOT NULL THEN T1.SUBCLASSNAME ";
        $query .= "          WHEN T1.SCHOOLCD='0' AND L2.SUBCLASSORDERNAME1 IS NOT NULL THEN L2.SUBCLASSORDERNAME1 ";
        $query .= "          WHEN T1.SCHOOLCD='1' AND L3.SUBCLASSORDERNAME1 IS NOT NULL THEN L3.SUBCLASSORDERNAME1 ";
        $query .= "          WHEN T1.SCHOOLCD='1' THEN L3.SUBCLASSNAME ";
        $query .= "          ELSE L2.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.ADD_CREDIT, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     L1.SHOWORDER2 AS CLASS_ORDER, ";
        $query .= "     L2.SHOWORDER2 AS SUBCLASS_ORDER ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD        = L1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T1.SCHOOL_KIND    = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON T1.SUBCLASSCD      = L2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND T1.CLASSCD         = L2.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = L2.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = L2.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN ANOTHER_SUBCLASS_MST L3 ON T1.SUBCLASSCD  = L3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND T1.CLASSCD         = L3.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = L3.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = L3.CURRICULUM_CD ";
            $query .= "                              AND T1.SCHOOLCD        = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     CLASS_ORDER, ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASS_ORDER, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     YEAR ";

        return $query;
    }
}
