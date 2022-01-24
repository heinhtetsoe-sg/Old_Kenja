<?php

require_once('for_php7.php');

class knjd626jQuery extends Query
{

    //学期取得
    public function getSemester($semester = '')
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester) {
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年末は、最新の学期取得
    public function getMaxSemester()
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校種別取得
    public function getSchregRegdGdat()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学年取得（権限チェック）
    public function getGrade($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getCourse($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT ";
        $query .= "   T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ";
        $query .= "  ,T4.COURSECODENAME AS NAME ";
        $query .= "FROM SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ";
        $query .= " INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD ";
        $query .= " INNER JOIN COURSECODE_MST T4 ON T4.COURSECODE = T1.COURSECODE ";
        $query .= "WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    public function getPrintGroup($model, $course)
    {
        $query = "";
        $query .= " SELECT DISTINCT T1.HR_CLASS || '00000000' AS CODE, ";
        $query .= " T2.HR_NAME AS NAME, T2.HR_NAMEABBV AS ABBV, ";
        $query .= " T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ";
        $query .= " FROM SCHREG_REGD_DAT T1";
        $query .= " INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ($course) {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$course."' ";
        }
        $query .= " ORDER BY CODE ";
        return $query;
    }

    public function getSubclassList($model, $course)
    {
        $query = "";
        $query .= " WITH RECORD AS (";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, T1.SEMESTER, ";
        $query .= "     T2.GRADE, T2.HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE, ";
        $query .= "     T1.SCHREGNO, ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= " CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN SUBCLASSCD ELSE ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD END AS SUBCLASSCD, T1.SCORE, T1.AVG ";
        $query .= " FROM RECORD_RANK_SDIV_DAT T1";
        $query .= " INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "    AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "    AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= "    AND T2.GRADE= '".$model->field["GRADE"]."' ";
        if ($course) {
            $query .= "    AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '".$course."' ";
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT VALUE(T4.SHOWORDER4, 999), ";
        $query .= "      T1.SUBCLASSCD, T2.SUBCLASSNAME, T2.SUBCLASSABBV ";
        $query .= " FROM RECORD T1 ";
        $query .= " INNER JOIN SUBCLASS_MST T2 ON ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " LEFT JOIN CLASS_MST T4 ON ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "     T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } else {
            $query .= "     T4.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ";
        }
        $query .= " ORDER BY VALUE(T4.SHOWORDER4, 999), T1.SUBCLASSCD ";
        return $query;
    }

    public function getAverageList($model)
    {
        $query = "";
        $query .= " SELECT ";
        $query .= " CASE WHEN SUBCLASSCD IN ('333333', '555555', '999999') THEN SUBCLASSCD ELSE ";
        if ("1"== $model->Properties["useCurriculumcd"]) {
            $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
        }
        $query .= "   SUBCLASSCD END AS SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS || COURSECD || MAJORCD || COURSECODE AS CODE, SCORE, AVG ";
        $query .= " FROM ";
        $query .= "   RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= "   AND (AVG_DIV IN ('1', '2', '3')) ";
        return $query;
    }

    public function getLastSemester($model)
    {
        $query = "SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <> '".$model->SEMEALL."' ";
        return $query;
    }

    public function getGradeInfo($model)
    {
        return "SELECT GRADE_CD, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$model->field["GRADE"]."' ";
    }

    public function getPeEst()
    {
        return "SELECT PRESENT_EST FROM SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."' AND SCHOOLCD = '".SCHOOLCD."' ";
    }
    public function getGrdCnt($model)
    {
        $query .= " SELECT ";
        $query .= "   COUNT(T1.SCHREGNO) AS CNT, ";
        $query .= "   SUM(CASE WHEN T2.SEX = '1' THEN 1 ELSE 0 END) AS MAN_CNT, ";
        $query .= "   SUM(CASE WHEN T2.SEX = '2' THEN 1 ELSE 0 END) AS WOMAN_CNT ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".$model->useSemester."' ";
        $query .= "   AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "   AND T2.GRD_DIV IS NULL ";
        return $query;
    }

    public function getSchInfo($model, $dataInfoMap)
    {
        $query  = " WITH RYUUNEN_BASE_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    SCHREGNO, ";
        $query .= "    YEAR, ";
        $query .= "    GRADE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '".CTRL_YEAR."' ";
        $query .= " ), RYUUNEN_SUM_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    SCHREGNO, ";
        $query .= "    COUNT(YEAR) AS CNT, ";
        $query .= "    GRADE ";
        $query .= " FROM ";
        $query .= "    RYUUNEN_BASE_T ";
        $query .= " GROUP BY ";
        $query .= "    SCHREGNO, ";
        $query .= "    GRADE ";
        $query .= " ), RYUUNEN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T2.CNT, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T3.GRADE_CD ";
        $query .= " FROM ";
        $query .= "    RYUUNEN_BASE_T T1 ";
        $query .= "    LEFT JOIN RYUUNEN_SUM_T T2 ";
        $query .= "      ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "      ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "    T3.SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$model->field["GRADE"]."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T2.SCHREGNO, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T4.HR_NAME, ";
        $query .= "   T2.ATTENDNO, ";
        $query .= "   T3.NAME, ";
        $query .= "   T3.SEX, ";
        $query .= "   L1.NAME1 AS SYUSSIN, ";
        $query .= "   CASE WHEN VALUE(T7.GRADE_CD, 0) >= 1 THEN CASE WHEN T6_1.CNT > 1 THEN '".$model->RYUKYU_MARK."' ELSE '―' END ELSE '　' END AS RYUNEN1, ";
        $query .= "   CASE WHEN VALUE(T7.GRADE_CD, 0) >= 2 THEN CASE WHEN T6_2.CNT > 1 THEN '".$model->RYUKYU_MARK."' ELSE '―' END ELSE '　' END AS RYUNEN2, ";
        $query .= "   CASE WHEN VALUE(T7.GRADE_CD, 0) >= 3 THEN CASE WHEN T6_3.CNT > 1 THEN '".$model->RYUKYU_MARK."' ELSE '―' END ELSE '　' END AS RYUNEN3, ";
        $query .= "   '' AS SUBCLS_CNT, ";
        $query .= "   '' AS CREDIT_CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "       ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "       ON T4.YEAR = T2.YEAR ";
        $query .= "      AND T4.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T4.GRADE = T2.GRADE ";
        $query .= "      AND T4.HR_CLASS = T2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T5 ";
        $query .= "       ON T5.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND T5.BASE_SEQ = '016' ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "       ON L1.NAMECD1 = 'A053' ";
        $query .= "      AND L1.NAMECD2 = T5.BASE_REMARK1 ";
        $query .= "     LEFT JOIN RYUUNEN_T T6_1 ";
        $query .= "       ON T6_1.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND T6_1.GRADE_CD = '01' ";
        $query .= "     LEFT JOIN RYUUNEN_T T6_2 ";
        $query .= "       ON T6_2.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND T6_2.GRADE_CD = '02' ";
        $query .= "     LEFT JOIN RYUUNEN_T T6_3 ";
        $query .= "       ON T6_3.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND T6_3.GRADE_CD = '03' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T7 ";
        $query .= "       ON T7.YEAR = T2.YEAR ";
        $query .= "      AND T7.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "      T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "      AND T2.GRADE = '".$model->field["GRADE"]."' ";

        $schParamStr = "";
        $delim = "";
        $akeys = array_unique(array_keys($dataInfoMap));
        foreach ($akeys as $key) {
            $schParamStr .= $delim . "'" . $key . "'";
            $delim = ",";
        }

        $query .= "      AND T2.SCHREGNO IN (".$schParamStr.") ";
        $query .= " ORDER BY ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";

        return $query;
    }

    public function getDataInfo1Sql()
    {
        //3/31と4/1を連結させる。
        //そのために、4/1開始、3/31終了データを持ってくる。最後にそれ以外のデータをUNIONする。
        //4/1開始データ
        $query  = " WITH HASIHASI4_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     TRANSFER_SDATE, ";
        $query .= "     TRANSFER_EDATE, ";
        $query .= "     YEAR(TRANSFER_SDATE) AS CONNYEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     TRANSFERCD = '1' ";
        $query .= "     AND ";
        $query .= "     (MONTH(TRANSFER_SDATE) = '4' AND DAY(TRANSFER_SDATE) = '1') ";
        //3/31終了データ
        $query .= " ), HASIHASI3_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     TRANSFER_SDATE, ";
        $query .= "     TRANSFER_EDATE, ";
        $query .= "     YEAR(TRANSFER_EDATE) AS CONNYEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     TRANSFERCD = '1' ";
        $query .= "     AND ";
        $query .= "     (MONTH(TRANSFER_EDATE) = '3' AND DAY(TRANSFER_EDATE) = '31') ";
        $query .= " ) ";
        //OUTER JOINで紐づくものは紐づけて、"紐づかない物はそのまま"持ってくる。
        //イメージとしては、下記
        //  T1開始、T1終了、結合条件、T2開始、T2終了
        //  あり    あり       ○      あり    あり  ->T1開始、T2終了 <-連続データ
        //  あり    あり       ×       -       -    ->T1開始、T1終了 <-3/31終了データ
        //   -       -         ×      あり    あり  ->T2開始、T2終了 <-4/1開始データ
        $query .= " SELECT ";
        $query .= "   CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCHREGNO ELSE T2.SCHREGNO END AS SCHREGNO, ";
        $query .= "   CASE WHEN T1.TRANSFER_SDATE IS NOT NULL THEN T1.TRANSFER_SDATE ELSE T2.TRANSFER_SDATE END AS SDATE, ";
        $query .= "   CASE WHEN T2.TRANSFER_EDATE IS NOT NULL THEN T2.TRANSFER_EDATE ELSE T1.TRANSFER_EDATE END AS EDATE ";
        $query .= " FROM ";
        $query .= "   HASIHASI3_T T1 ";
        $query .= "   FULL OUTER JOIN HASIHASI4_T T2 ";
        $query .= "     ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND T1.CONNYEAR = T2.CONNYEAR ";
        //3/31終了，4/1開始"以外"のデータ
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     TRANSFER_SDATE AS SDATE, ";
        $query .= "     TRANSFER_EDATE AS EDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     TRANSFERCD = '1' ";
        $query .= "     AND ";
        $query .= "     (MONTH(TRANSFER_SDATE) <> '4' OR DAY(TRANSFER_SDATE) <>'1') ";
        $query .= "     AND ";
        $query .= "     (MONTH(TRANSFER_EDATE) <> '3' OR DAY(TRANSFER_EDATE) <> '31') ";
        $query .= " ORDER BY ";
        $query .= "   SCHREGNO, ";
        $query .= "   SDATE ";
        return $query;
    }

    public function getDataInfo2Sql($model)
    {
        $query  = " WITH ATTSUM_T AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ";
        $query .= "   SUM(VALUE(LESSON, 0)) AS LESSON, ";
        $query .= "   SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ";
        $query .= "   SUM(VALUE(ABSENT, 0)) AS ABSENT, ";
        $query .= "   SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ";
        $query .= "   SUM(VALUE(MOURNING, 0)) AS MOURNING, ";
        $query .= "   SUM(VALUE(ABROAD, 0)) AS ABROAD, ";
        $query .= "   SUM(VALUE(SICK, 0)) AS SICK, ";
        $query .= "   SUM(VALUE(NOTICE, 0)) AS NOTICE, ";
        $query .= "   SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ";
        $query .= "   SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ";
        $query .= "   SUM(VALUE(LATE, 0)) AS LATE, ";
        $query .= "   SUM(VALUE(EARLY, 0)) AS EARLY, ";
        $query .= "   SUM(VALUE(VIRUS, 0)) AS VIRUS, ";
        $query .= "   SUM(VALUE(KOUDOME, 0)) AS KOUDOME ";
        $query .= " FROM ";
        $query .= "   ATTEND_SUBCLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   COPYCD = '0' ";
        $query .= "   AND YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.APPOINTED_DAY, ";
        $query .= "   T1.LESSON, ";
        $query .= "   T1.OFFDAYS, ";
        $query .= "   T1.ABSENT, ";
        $query .= "   T1.SUSPEND, ";
        $query .= "   T1.MOURNING, ";
        $query .= "   T1.ABROAD, ";
        $query .= "   T1.SICK, ";
        $query .= "   T1.NOTICE, ";
        $query .= "   T1.NONOTICE, ";
        $query .= "   T1.NURSEOFF, ";
        $query .= "   T1.LATE, ";
        $query .= "   T1.EARLY, ";
        $query .= "   T1.VIRUS, ";
        $query .= "   T1.KOUDOME ";
        $query .= " FROM ";
        $query .= "   ATTSUM_T T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";

        return $query;
    }

    public function getDataInfo3Sql($model)
    {
        $query  = " WITH ATTSUM_T AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ";
        $query .= "     SUM(VALUE(LESSON, 0)) AS LESSON, ";
        $query .= "     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ";
        $query .= "     SUM(VALUE(ABSENT, 0)) AS ABSENT, ";
        $query .= "     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ";
        $query .= "     SUM(VALUE(MOURNING, 0)) AS MOURNING, ";
        $query .= "     SUM(VALUE(ABROAD, 0)) AS ABROAD, ";
        $query .= "     SUM(VALUE(SICK, 0)) AS SICK, ";
        $query .= "     SUM(VALUE(NOTICE, 0)) AS NOTICE, ";
        $query .= "     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ";
        $query .= "     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ";
        $query .= "     SUM(VALUE(LATE, 0)) AS LATE, ";
        $query .= "     SUM(VALUE(EARLY, 0)) AS EARLY, ";
        $query .= "     SUM(VALUE(VIRUS, 0)) AS VIRUS, ";
        $query .= "     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     COPYCD = '0' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " ), MERGESCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   TR1.YEAR, ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ";
        $query .= "   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.SCORE END AS SCORE, ";
        $query .= "   T8.CREDITS ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT TR1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = TR1.YEAR ";
        $query .= "      AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "      AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "     LEFT JOIN CREDIT_MST T8 ";
        $query .= "       ON T8.YEAR = TR1.YEAR ";
        $query .= "      AND T8.GRADE = T2.GRADE ";
        $query .= "      AND T8.COURSECD = T2.COURSECD ";
        $query .= "      AND T8.MAJORCD = T2.MAJORCD ";
        $query .= "      AND T8.COURSECODE = T2.COURSECODE ";
        $query .= "      AND T8.CLASSCD = TR1.CLASSCD ";
        $query .= "      AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "      AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "      AND T8.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "     ON R1.YEAR = TR1.YEAR ";
        $query .= "    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ";
        $query .= "    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "    AND R1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "     TR1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '".$model->SEARCHTESTCD1."' ";
        $query .= "     AND VALUE(TR1.SCORE,0) <= 3 ";
        $query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND TR1.CLASSCD <= '90' ";
        $query .= "     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ";
        $query .= "                                SELECT DISTINCT ";
        $query .= "                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                                FROM ";
        $query .= "                                    SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                WHERE ";
        $query .= "                                    REPLACECD = '1' ";
        $query .= "                                    AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                                    AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                               ) ";
        $query .= " ), SUMMSCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   CASE WHEN COUNT(SCORE) = 0 THEN NULL ELSE SUM(VALUE(SCORE, 0)) END AS SCORE, ";
        $query .= "   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ";
        $query .= " FROM ";
        $query .= "   MERGESCORE_T ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   SCORE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   CASE WHEN 3 * (T1.SICK + T1.NOTICE + T1.NONOTICE) > (T1.LESSON - T1.OFFDAYS - T1.ABROAD) THEN '*' ELSE '' END AS AST_MK, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD, ";
        $query .= "   T7.SUBCLASSNAME, ";
        $query .= "   TR1.CREDITS, ";
        $query .= "   TR1.SCORE ";
        $query .= " FROM ";
        $query .= "   SUMMSCORE_T TR1 ";
        $query .= "   LEFT JOIN ATTSUM_T T1 ";
        $query .= "     ON T1.YEAR = TR1.YEAR ";
        $query .= "    AND T1.SCHREGNO = TR1.SCHREGNO ";
        $query .= "    AND T1.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T1.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T1.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T1.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = TR1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "   LEFT JOIN SUBCLASS_MST T7 ";
        $query .= "     ON T7.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";
        return $query;
    }

    public function getDataInfo4Sql($model)
    {
        $query  = " WITH ATTSUM_T AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ";
        $query .= "     SUM(VALUE(LESSON, 0)) AS LESSON, ";
        $query .= "     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ";
        $query .= "     SUM(VALUE(ABSENT, 0)) AS ABSENT, ";
        $query .= "     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ";
        $query .= "     SUM(VALUE(MOURNING, 0)) AS MOURNING, ";
        $query .= "     SUM(VALUE(ABROAD, 0)) AS ABROAD, ";
        $query .= "     SUM(VALUE(SICK, 0)) AS SICK, ";
        $query .= "     SUM(VALUE(NOTICE, 0)) AS NOTICE, ";
        $query .= "     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ";
        $query .= "     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ";
        $query .= "     SUM(VALUE(LATE, 0)) AS LATE, ";
        $query .= "     SUM(VALUE(EARLY, 0)) AS EARLY, ";
        $query .= "     SUM(VALUE(VIRUS, 0)) AS VIRUS, ";
        $query .= "     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     COPYCD = '0' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " ), MERGESCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   TR1.YEAR, ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ";
        $query .= "   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.SCORE END AS SCORE, ";
        $query .= "   T8.CREDITS ";
        $query .= " FROM ";
        $query .= "   RECORD_SCORE_DAT TR1 ";
        $query .= "   LEFT JOIN ATTSUM_T T1 ";
        $query .= "     ON T1.YEAR = TR1.YEAR ";
        $query .= "    AND T1.SCHREGNO = TR1.SCHREGNO ";
        $query .= "    AND T1.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T1.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T1.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T1.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = TR1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "   LEFT JOIN CREDIT_MST T8 ";
        $query .= "     ON T8.YEAR = TR1.YEAR ";
        $query .= "    AND T8.GRADE = T2.GRADE ";
        $query .= "    AND T8.COURSECD = T2.COURSECD ";
        $query .= "    AND T8.MAJORCD = T2.MAJORCD ";
        $query .= "    AND T8.COURSECODE = T2.COURSECODE ";
        $query .= "    AND T8.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "     ON R1.YEAR = TR1.YEAR ";
        $query .= "    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ";
        $query .= "    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "    AND R1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "     TR1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '".$model->SEARCHTESTCD1."' ";
        $query .= "     AND VALUE(TR1.SCORE,0) <= 3 ";
        $query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND TR1.CLASSCD <= '90' ";
        $query .= "     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ";
        $query .= "                                SELECT DISTINCT ";
        $query .= "                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                                FROM ";
        $query .= "                                    SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                WHERE ";
        $query .= "                                    REPLACECD = '1' ";
        $query .= "                                    AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                                    AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                               ) ";
        $query .= " ), SUMMSCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   CASE WHEN COUNT(SCORE) = 0 THEN NULL ELSE SUM(VALUE(SCORE, 0)) END AS SCORE, ";
        $query .= "   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ";
        $query .= " FROM ";
        $query .= "   MERGESCORE_T ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   SCORE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   CASE WHEN 3 * (T1.SICK + T1.NOTICE + T1.NONOTICE) > (T1.LESSON - T1.OFFDAYS - T1.ABROAD) THEN '*' ELSE '' END AS AST_MK, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD, ";
        $query .= "   T7.SUBCLASSNAME, ";
        $query .= "   TR1.CREDITS, ";
        $query .= "   TR1.SCORE ";
        $query .= " FROM ";
        $query .= "   SUMMSCORE_T TR1 ";
        $query .= "   LEFT JOIN ATTSUM_T T1 ";
        $query .= "     ON T1.YEAR = TR1.YEAR ";
        $query .= "    AND T1.SCHREGNO = TR1.SCHREGNO ";
        $query .= "    AND T1.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T1.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T1.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T1.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = TR1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "   LEFT JOIN SUBCLASS_MST T7 ";
        $query .= "     ON T7.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";
        return $query;
    }
    public function getDataInfo5Sql($model)
    {
        $query  = " WITH ATTSUM_T AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ";
        $query .= "     SUM(VALUE(LESSON, 0)) AS LESSON, ";
        $query .= "     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ";
        $query .= "     SUM(VALUE(ABSENT, 0)) AS ABSENT, ";
        $query .= "     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ";
        $query .= "     SUM(VALUE(MOURNING, 0)) AS MOURNING, ";
        $query .= "     SUM(VALUE(ABROAD, 0)) AS ABROAD, ";
        $query .= "     SUM(VALUE(SICK, 0)) AS SICK, ";
        $query .= "     SUM(VALUE(NOTICE, 0)) AS NOTICE, ";
        $query .= "     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ";
        $query .= "     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ";
        $query .= "     SUM(VALUE(LATE, 0)) AS LATE, ";
        $query .= "     SUM(VALUE(EARLY, 0)) AS EARLY, ";
        $query .= "     SUM(VALUE(VIRUS, 0)) AS VIRUS, ";
        $query .= "     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     COPYCD = '0' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " ), MERGECREDIT_T AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE T1.CLASSCD END AS CLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE T1.SCHOOL_KIND END AS SCHOOL_KIND, ";
        $query .= "   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE T1.CURRICULUM_CD END AS CURRICULUM_CD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE T1.SUBCLASSCD END AS SUBCLASSCD, ";
        $query .= "   T3.CREDITS ";
        $query .= " FROM ";
        $query .= "   ATTSUM_T T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN CREDIT_MST T3 ";
        $query .= "     ON T3.YEAR = T2.YEAR ";
        $query .= "    AND T3.GRADE = T2.GRADE ";
        $query .= "    AND T3.COURSECD = T2.COURSECD ";
        $query .= "    AND T3.MAJORCD = T2.MAJORCD ";
        $query .= "    AND T3.COURSECODE = T2.COURSECODE ";
        $query .= "    AND T3.CLASSCD = T1.CLASSCD ";
        $query .= "    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "    AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "     ON R1.YEAR = T1.YEAR ";
        $query .= "    AND R1.ATTEND_CLASSCD = T1.CLASSCD ";
        $query .= "    AND R1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "    AND R1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "    AND R1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "    AND R1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "   AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN ( ";
        $query .= "                              SELECT DISTINCT ";
        $query .= "                                  ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                              FROM ";
        $query .= "                                  SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                              WHERE ";
        $query .= "                                  REPLACECD = '1' ";
        $query .= "                                  AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                                  AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                             ) ";
        $query .= " ), SUMMCREDIT_T AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ";
        $query .= " FROM ";
        $query .= "   MERGECREDIT_T ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.APPOINTED_DAY, ";
        $query .= "   T1.LESSON, ";
        $query .= "   T1.OFFDAYS, ";
        $query .= "   T1.ABSENT, ";
        $query .= "   T1.SUSPEND, ";
        $query .= "   T1.MOURNING, ";
        $query .= "   T1.ABROAD, ";
        $query .= "   T1.SICK, ";
        $query .= "   T1.NOTICE, ";
        $query .= "   T1.NONOTICE, ";
        $query .= "   T1.NURSEOFF, ";
        $query .= "   T1.LATE, ";
        $query .= "   T1.EARLY, ";
        $query .= "   T1.VIRUS, ";
        $query .= "   T1.KOUDOME, ";
        $query .= "   T4.SUBCLASSNAME, ";
        $query .= "   T3.CREDITS ";
        $query .= " FROM ";
        $query .= "     ATTSUM_T T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SUMMCREDIT_T T3 ";
        $query .= "       ON T3.YEAR = T1.YEAR ";
        $query .= "      AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T3.CLASSCD = T1.CLASSCD ";
        $query .= "      AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "      AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "      AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST T4 ";
        $query .= "       ON T4.CLASSCD = T1.CLASSCD ";
        $query .= "      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "      AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN ( ";
        $query .= "                                SELECT DISTINCT ";
        $query .= "                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                                FROM ";
        $query .= "                                    SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                WHERE ";
        $query .= "                                    REPLACECD = '1' ";
        $query .= "                                    AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                                    AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                               ) ";
        $query .= "     AND (T1.LESSON - T1.OFFDAYS - T1.ABROAD) > 0 ";
        $query .= " ORDER BY ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";
        return $query;
    }
    public function getDataInfo6Sql($model)
    {
        $query  = " WITH MERGESCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   TR1.YEAR, ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ";
        $query .= "   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.SCORE END AS SCORE, ";
        $query .= "   T8.CREDITS ";
        $query .= " FROM ";
        $query .= "   RECORD_SCORE_DAT TR1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = TR1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "   LEFT JOIN CREDIT_MST T8 ";
        $query .= "     ON T8.YEAR = TR1.YEAR ";
        $query .= "    AND T8.GRADE = T2.GRADE ";
        $query .= "    AND T8.COURSECD = T2.COURSECD ";
        $query .= "    AND T8.MAJORCD = T2.MAJORCD ";
        $query .= "    AND T8.COURSECODE = T2.COURSECODE ";
        $query .= "    AND T8.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "     ON R1.YEAR = TR1.YEAR ";
        $query .= "    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ";
        $query .= "    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "    AND R1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "     TR1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '".$model->SEARCHTESTCD1."' ";
        $query .= "     AND VALUE(TR1.SCORE,0) <= 2 ";
        $query .= "     AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND TR1.CLASSCD <= '90' ";
        $query .= "     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ";
        $query .= "                                SELECT DISTINCT ";
        $query .= "                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                                FROM ";
        $query .= "                                    SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                WHERE ";
        $query .= "                                    REPLACECD = '1' ";
        $query .= "                                    AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                                    AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                               ) ";
        $query .= " ), SUMMSCORE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   CASE WHEN COUNT(SCORE) = 0 THEN NULL ELSE SUM(VALUE(SCORE, 0)) END AS SCORE, ";
        $query .= "   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ";
        $query .= " FROM ";
        $query .= "   MERGESCORE_T ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   SCORE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD, ";
        $query .= "   T7.SUBCLASSNAME, ";
        $query .= "   TR1.CREDITS, ";
        $query .= "   TR1.SCORE ";
        $query .= " FROM ";
        $query .= "     SUMMSCORE_T TR1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = TR1.YEAR ";
        $query .= "      AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "      AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST T7 ";
        $query .= "       ON T7.CLASSCD = TR1.CLASSCD ";
        $query .= "      AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "      AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "      AND T7.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";
        return $query;
    }
    public function getDataInfo7Sql($model)
    {
        $query .= " SELECT ";
        $query .= "   T2.COURSECODE, ";
        $query .= "   CASE WHEN T8.REQUIRE_FLG = '1' THEN '1' ELSE '0' END AS REQUIRE_FLG, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD, ";
        $query .= "   T9.CLASSNAME, ";
        $query .= "   T7.SUBCLASSNAME, ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   RECORD_SCORE_DAT TR1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = TR1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "   LEFT JOIN SUBCLASS_MST T7 ";
        $query .= "     ON T7.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN CREDIT_MST T8 ";
        $query .= "     ON T8.YEAR = TR1.YEAR ";
        $query .= "    AND T8.GRADE = T2.GRADE ";
        $query .= "    AND T8.COURSECD = T2.COURSECD ";
        $query .= "    AND T8.MAJORCD = T2.MAJORCD ";
        $query .= "    AND T8.COURSECODE = T2.COURSECODE ";
        $query .= "    AND T8.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN CLASS_MST T9 ";
        $query .= "     ON T9.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T9.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "   TR1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '".$model->SEARCHTESTCD1."' ";
        $query .= "   AND VALUE(TR1.SCORE,0) <= 3 ";
        $query .= "   AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "   AND TR1.CLASSCD <= '90' ";
        $query .= "   AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ";
        $query .= "                              SELECT DISTINCT ";
        $query .= "                                  ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                              FROM ";
        $query .= "                                  SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                              WHERE ";
        $query .= "                                  REPLACECD = '1' ";
        $query .= "                                  AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                                  AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                             ) ";
        $query .= " GROUP BY ";
        $query .= "   T2.COURSECODE, ";
        $query .= "   T8.REQUIRE_FLG, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD, ";
        $query .= "   T9.CLASSNAME, ";
        $query .= "   T7.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "   T2.COURSECODE, ";
        $query .= "   T8.REQUIRE_FLG, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD ";
        return $query;
    }
    public function getDataInfo8Sql($model)
    {
        //当年度と過年度分を分けて取得して、並べる。
        //最初に単位の集計を行う(先科目に対してCALCULATE_CREDIT_FLGに応じて除外、集計を行う)
        $query  = " WITH MERGECREDIT_T AS ( ";
        $query .= " SELECT ";
        $query .= "   TR1.YEAR, ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ";
        $query .= "   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ";
        $query .= "   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.GET_CREDIT END AS GET_CREDIT, ";
        $query .= "   T8.CREDITS ";
        $query .= " FROM ";
        $query .= "   RECORD_SCORE_DAT TR1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON ((T2.YEAR < TR1.YEAR ";
        $query .= "            AND T2.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR < T2.YEAR AND SEMESTER <> '9') ";
        $query .= "         ) OR (T2.YEAR = TR1.YEAR ";
        $query .= "            AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "         )) ";
        $query .= "    AND T2.SCHREGNO = TR1.SCHREGNO ";
        $query .= "   LEFT JOIN CREDIT_MST T8 ";
        $query .= "     ON T8.YEAR = TR1.YEAR ";
        $query .= "    AND T8.GRADE = T2.GRADE ";
        $query .= "    AND T8.COURSECD = T2.COURSECD ";
        $query .= "    AND T8.MAJORCD = T2.MAJORCD ";
        $query .= "    AND T8.COURSECODE = T2.COURSECODE ";
        $query .= "    AND T8.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "     ON R1.YEAR = TR1.YEAR ";
        $query .= "    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ";
        $query .= "    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= "    AND R1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " WHERE ";
        $query .= "     TR1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '".$model->SEARCHTESTCD1."' ";
        $query .= "     AND TR1.CLASSCD <= '90' ";
        $query .= "     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ";
        $query .= "                                SELECT DISTINCT ";
        $query .= "                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ";
        $query .= "                                FROM ";
        $query .= "                                    SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                WHERE ";
        $query .= "                                    REPLACECD = '1' ";
        $query .= "                                    AND YEAR = TR1.YEAR ";
        $query .= "                                    AND CALCULATE_CREDIT_FLG = '1' ";
        $query .= "                               ) ";
        //単位の集計
        $query .= " ), SUMMCREDIT_T AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   GET_CREDIT, ";
        $query .= "   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ";
        $query .= " FROM ";
        $query .= "   MERGECREDIT_T ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   CLASSCD, ";
        $query .= "   SCHOOL_KIND, ";
        $query .= "   CURRICULUM_CD, ";
        $query .= "   SUBCLASSCD, ";
        $query .= "   GET_CREDIT ";
        //ベースとなるデータ
        $query .= " ), STATBASE_T AS ( ";
        $query .= " SELECT ";
        $query .= "   TR1.YEAR, ";
        $query .= "   TR1.SCHREGNO, ";
        $query .= "   TR1.CLASSCD, ";
        $query .= "   TR1.SCHOOL_KIND, ";
        $query .= "   TR1.CURRICULUM_CD, ";
        $query .= "   TR1.SUBCLASSCD, ";
        $query .= "   TR1.GET_CREDIT, ";
        $query .= "   TR1.CREDITS, ";
        $query .= "   T7.SUBCLASSNAME, ";
        $query .= "   CASE WHEN TR1.GET_CREDIT IS NULL THEN '未入力' ELSE '未履修' END AS STAT ";
        $query .= " FROM ";
        $query .= "   SUMMCREDIT_T TR1 ";
        $query .= "   LEFT JOIN SUBCLASS_MST T7 ";
        $query .= "     ON T7.CLASSCD = TR1.CLASSCD ";
        $query .= "    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ";
        $query .= "    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ";
        $query .= "    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ";
        $query .= " ), STAT_Y_T AS ( ";
        //当年度分
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SUBCLASSNAME, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T2.GRADE AS YGRADE, ";
        $query .= "   T1.STAT, ";
        $query .= "   T1.CREDITS ";
        $query .= " FROM ";
        $query .= "   STATBASE_T T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".$model->useSemester."' ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SUBCLASSCD = '".$model->SEARCHSUBCLSCD1."' ";
        $query .= "   AND (CASE WHEN T1.GET_CREDIT IS NULL THEN '' ELSE TRIM(T1.GET_CREDIT) END) = '' ";
        //過年度分
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SUBCLASSNAME, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T3.GRADE AS YGRADE, ";
        $query .= "   T1.STAT, ";
        $query .= "   T1.CREDITS ";
        $query .= " FROM ";
        $query .= "   STATBASE_T T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.SEMESTER = (SELECT MAX(TX.SEMESTER) FROM SEMESTER_MST TX WHERE TX.YEAR = T3.YEAR AND TX.SEMESTER <> '9') ";
        $query .= "    AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR < '".CTRL_YEAR."' ";
        $query .= "   AND T1.SUBCLASSCD = '".$model->SEARCHSUBCLSCD1."' ";
        $query .= "   AND (CASE WHEN T1.GET_CREDIT IS NULL THEN '' ELSE TRIM(T1.GET_CREDIT) END) = '' ";
        $query .= "   AND T1.SCHREGNO IN (SELECT T2.SCHREGNO FROM SCHREG_REGD_DAT T2 WHERE T2.YEAR = '".CTRL_YEAR."' AND T2.SEMESTER = '".$model->useSemester."' AND T2.GRADE = '".$model->field["GRADE"]."') ";
        $query .= " ) ";
        //並び順を指定
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SUBCLASSNAME, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.YGRADE, ";
        $query .= "   T1.STAT, ";
        $query .= "   T1.CREDITS ";
        $query .= " FROM ";
        $query .= "   STAT_Y_T T1 ";
        $query .= " ORDER BY ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.YGRADE ";
        return $query;
    }
}
