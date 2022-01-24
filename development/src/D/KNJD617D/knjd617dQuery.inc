<?php

require_once('for_php7.php');


class knjd617dQuery extends Query
{

    //学期取得
    public function getSemester($model, $getSemes)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($getSemes != 0) {
            $query .= "     AND T1.SEMESTER = '".$getSemes."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学年取得（権限チェック）
    public function getGrade($semester, $model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('J','H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     S1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_HDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR     = T1.YEAR AND ";
            $query .= "                     S1.GRADE    = T1.GRADE AND ";
            $query .= "                     S1.SEMESTER = '".$semester."' AND ";
            $query .= "                     '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    public function getSdate($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SDATE ";
        $query .= " FROM ";
        $query .= "     V_SEMESTER_GRADE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE        = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //テスト種別
    public function getTest($model, $grade, $flg = 1, $testcd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.TESTITEMNAME, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL, ";
        $query .= "  L3.EDATE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        if ($model->field["SEMESTER"]=='9') {
            $query .= "     INNER JOIN SEMESTER_MST L3 ON L3.YEAR = L1.YEAR ";
            $query .= "                 AND L3.SEMESTER = L1.SEMESTER ";
        } else {
            $query .= "     INNER JOIN SEMESTER_DETAIL_MST L3 ON L3.YEAR = L1.YEAR ";
            $query .= "                AND L3.SEMESTER = L1.SEMESTER ";
            $query .= "                AND L3.SEMESTER_DETAIL = L1.SEMESTER_DETAIL ";
        }
        if ($flg == 1) {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ";
            $query .= "                                   AND L2.GRADE = '".$grade."' ";
            $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.CLASSCD      = '00' ";
        if ($flg != 1) {
            $query .= "     AND T1.SCHOOL_KIND = '00' ";
        }
        $query .= "     AND T1.CURRICULUM_CD  = '00' ";
        $query .= "     AND T1.SUBCLASSCD  = '000000' ";
        if ($testcd != "") {
            $query .= "  AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$testcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getHrClass($semester, $grade, $grHrCls = "")
    {
        $semester = $semester == "9" ? CTRL_SEMESTER : $semester;
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.HR_NAMEABBV, ";
        $query .= "     T1.GRADE_NAME, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        if ($grHrCls != "") {
            $query .= "     ,T2.STAFFNAME ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($grHrCls != "") {
            $query .= "    LEFT JOIN STAFF_MST T2 ";
            $query .= "      ON T2.STAFFCD = T1.TR_CD1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($grHrCls != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$grHrCls."' ";
        } else {
            $query .= "     AND T1.GRADE = '".$grade."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1      = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2      = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3      = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1   = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2   = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getCsvSubclsAttendInfoSql($model, $hrClass)
    {
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $sDate = str_replace("/", "-", $model->field["SDATE"]);
        $eDate = str_replace("/", "-", $model->field["DATE"]);
        $query  = " WITH ASDAT AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   SUM(LESSON) AS LESSON, ";
        $query .= "   SUM(OFFDAYS) AS OFFDAYS, ";
        $query .= "   SUM(ABSENT) AS ABSENT, ";
        $query .= "   SUM(SUSPEND) AS SUSPEND, ";
        $query .= "   SUM(MOURNING) AS MOURNING, ";
        $query .= "   SUM(ABROAD) AS ABROAD, ";
        $query .= "   SUM(SICK) AS SICK, ";
        $query .= "   SUM(NOTICE) AS NOTICE, ";
        $query .= "   SUM(NONOTICE) AS NONOTICE, ";
        $query .= "   SUM(NURSEOFF) NURSEOFF, ";
        $query .= "   SUM(LATE) AS LATE, ";
        $query .= "   SUM(EARLY) AS EARLY, ";
        $query .= "   SUM(VIRUS) AS VIRUS, ";
        $query .= "   SUM(KOUDOME) AS KOUDOME ";
        $query .= " FROM ";
        $query .= "   ATTEND_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "   COPYCD = '0' ";
        $query .= "   AND YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <= '".$model->field["SEMESTER"]."' ";
        $query .= "   AND YEAR || '-' || MONTH || '-' || APPOINTED_DAY BETWEEN '".$sDate."' AND '".$eDate."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD ";
        $query .= " ), BDAT AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T3.NAME, ";
        $query .= "   T2.CLASSCD, ";
        $query .= "   T2.SCHOOL_KIND, ";
        $query .= "   T2.CURRICULUM_CD, ";
        $query .= "   T2.SUBCLASSCD, ";
        $query .= "   T2.LESSON, ";
        $query .= "   T2.OFFDAYS, ";
        $query .= "   T2.ABSENT, ";
        $query .= "   T2.SUSPEND, ";
        $query .= "   T2.MOURNING, ";
        $query .= "   T2.ABROAD, ";
        $query .= "   T2.SICK, ";
        $query .= "   T2.NOTICE, ";
        $query .= "   T2.NONOTICE, ";
        $query .= "   T2.NURSEOFF, ";
        $query .= "   T2.LATE, ";
        $query .= "   T2.EARLY, ";
        $query .= "   T2.VIRUS, ";
        $query .= "   T2.KOUDOME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN ASDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".$semester."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '".$hrClass."' ";
        $query .= "   AND T2.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ";
        $query .= "   AND T2.CLASSCD < '90' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.* ";
        $query .= " FROM ";
        $query .= "   BDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
        $query .= "     SELECT ";
        $query .= "       T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       SUBCLASS_REPLACE_COMBINED_DAT T2W ";
        $query .= "     WHERE ";
        $query .= "       T2W.REPLACECD = '1' AND T2W.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ";
        $query .= "           <> T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD ";
        $query .= "       AND T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD IN (";
        $query .= "         SELECT T2WW.CLASSCD || '-' || T2WW.SCHOOL_KIND || '-' || T2WW.CURRICULUM_CD || '-' || T2WW.SUBCLASSCD FROM BDAT T2WW ";
        $query .= "       ) ";
        $query .= "   ) ";
        $query .= " ORDER BY ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO ";
        return $query;
    }

    public function getCsvBaseStudentInfoSql($model, $hrClass, $clsCd = "")
    {
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $sDate = str_replace("/", "-", $model->field["SDATE"]);
        $eDate = str_replace("/", "-", $model->field["DATE"]);
        $query  = " WITH BDAT AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T3.NAME, ";
        $query .= "   T4.CLASSCD, ";
        $query .= "   T4.SCHOOL_KIND, ";
        $query .= "   T4.CURRICULUM_CD, ";
        $query .= "   T4.SUBCLASSCD, ";
        $query .= "   T4.SCORE, ";
        $query .= "   ROUND(T4.AVG, 2) AS AVG, ";
        $query .= "   T4.GRADE_RANK, ";
        $query .= "   T4.CLASS_RANK ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN RECORD_RANK_SDIV_DAT T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "    AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".$semester."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '".$hrClass."' ";
        $query .= "   AND T4.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ";
        if ($clsCd != "") {
            $query .= "   AND T4.CLASSCD = '".$clsCd."' ";
        } else {
            $query .= "   AND (T4.CLASSCD < '90' OR T4.CLASSCD = '99') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.* ";
        $query .= " FROM ";
        $query .= "   BDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
        $query .= "     SELECT ";
        $query .= "       T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       SUBCLASS_REPLACE_COMBINED_DAT T2W ";
        $query .= "     WHERE ";
        $query .= "       T2W.REPLACECD = '1' AND T2W.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ";
        $query .= "           <> T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD ";
        $query .= "       AND T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD IN (";
        $query .= "         SELECT T2WW.CLASSCD || '-' || T2WW.SCHOOL_KIND || '-' || T2WW.CURRICULUM_CD || '-' || T2WW.SUBCLASSCD FROM BDAT T2WW ";
        $query .= "       ) ";
        $query .= "   ) ";
        $query .= " ORDER BY ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO ";
        return $query;
    }
    
    public function getCsvSubclsInfoSql($model, $hrClass)
    {
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = " WITH BDAT AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        $query .= "   T3.CLASSCD, ";
        $query .= "   T3.SCHOOL_KIND, ";
        $query .= "   T3.CURRICULUM_CD, ";
        $query .= "   T3.SUBCLASSCD, ";
        $query .= "   T4.SUBCLASSNAME, ";
        $query .= "   T4.SUBCLASSABBV, ";
        $query .= "   T5.CREDITS ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN CHAIR_DAT T3 ";
        $query .= "     ON T3.YEAR = T2.YEAR ";
        $query .= "    AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "    AND T3.CHAIRCD = T2.CHAIRCD ";
        $query .= "   LEFT JOIN SUBCLASS_MST T4 ";
        $query .= "     ON T4.CLASSCD = T3.CLASSCD ";
        $query .= "    AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "    AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "   LEFT JOIN CREDIT_MST T5 ";
        $query .= "     ON T5.YEAR = T1.YEAR ";
        $query .= "    AND T5.COURSECD = T1.COURSECD ";
        $query .= "    AND T5.MAJORCD = T1.MAJORCD ";
        $query .= "    AND T5.GRADE = T1.GRADE ";
        $query .= "    AND T5.COURSECODE = T1.COURSECODE ";
        $query .= "    AND T5.CLASSCD = T3.CLASSCD ";
        $query .= "    AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "    AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "    AND T5.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".$semester."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '".$hrClass."' ";
        $query .= "   AND T3.CLASSCD < '90' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T3.* ";
        $query .= " FROM ";
        $query .= "   BDAT T3 ";
        $query .= " WHERE ";
        $query .= "   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD NOT IN ( ";
        $query .= "     SELECT ";
        $query .= "       T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       SUBCLASS_REPLACE_COMBINED_DAT T2W ";
        $query .= "     WHERE ";
        $query .= "       T2W.REPLACECD = '1' AND T2W.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND T2W.ATTEND_CLASSCD || '-' || T2W.ATTEND_SCHOOL_KIND || '-' || T2W.ATTEND_CURRICULUM_CD || '-' || T2W.ATTEND_SUBCLASSCD ";
        $query .= "           <> T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD ";
        $query .= "       AND T2W.COMBINED_CLASSCD || '-' || T2W.COMBINED_SCHOOL_KIND || '-' || T2W.COMBINED_CURRICULUM_CD || '-' || T2W.COMBINED_SUBCLASSCD IN (";
        $query .= "         SELECT T2WW.CLASSCD || '-' || T2WW.SCHOOL_KIND || '-' || T2WW.CURRICULUM_CD || '-' || T2WW.SUBCLASSCD FROM BDAT T2WW ";
        $query .= "       ) ";
        $query .= "   ) ";
        $query .= " ORDER BY ";
        $query .= "   T3.CLASSCD, ";
        $query .= "   T3.SCHOOL_KIND, ";
        $query .= "   T3.CURRICULUM_CD, ";
        $query .= "   T3.SUBCLASSCD ";
        $query .= "  ";
        return $query;
    }

    public function getCsvTotalAttendInfoSql($model, $hrClass)
    {
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $sDate = str_replace("/", "-", $model->field["SDATE"]);
        $eDate = str_replace("/", "-", $model->field["DATE"]);
        $query  = " SELECT ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   SUM(T2.LESSON) AS LESSON, ";
        $query .= "   SUM(T2.OFFDAYS) AS OFFDAYS, ";
        $query .= "   SUM(T2.ABSENT) AS ABSENT, ";
        $query .= "   SUM(T2.SUSPEND) AS SUSPEND, ";
        $query .= "   SUM(T2.MOURNING) AS MOURNING, ";
        $query .= "   SUM(T2.ABROAD) AS ABROAD, ";
        $query .= "   SUM(T2.SICK) AS SICK, ";
        $query .= "   SUM(T2.NOTICE) AS NOTICE, ";
        $query .= "   SUM(T2.NONOTICE) AS NONOTICE, ";
        $query .= "   SUM(T2.LATE) AS LATE, ";
        $query .= "   SUM(T2.EARLY) AS EARLY, ";
        $query .= "   SUM(T2.KEKKA_JISU) AS KEKKA_JISU, ";
        $query .= "   SUM(T2.KEKKA) AS KEKKA, ";
        $query .= "   SUM(T2.LATEDETAIL) AS LATEDETAIL, ";
        $query .= "   SUM(T2.VIRUS) AS VIRUS, ";
        $query .= "   SUM(T2.KOUDOME) ";
        $query .= " FROM ";
        $query .= " SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ATTEND_SEMES_DAT T2 ";
        $query .= "   ON T2.COPYCD = '0' ";
        $query .= "  AND T2.YEAR = T1.YEAR ";
        $query .= "  AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "  AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "  AND T2.YEAR ||'-'|| T2.MONTH || '-' || T2.APPOINTED_DAY BETWEEN '".$sDate."' AND '".$eDate."' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".$semester."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '".$hrClass."' ";
        $query .= " GROUP BY ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO ";

        return $query;
    }
}
