<?php

class knjh442cQuery extends Query
{

    //学期取得
    public function getSemester($date = "")
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($date) {
            $query .= "     AND '".$date."' BETWEEN SDATE AND EDATE ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    public function getSelectGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS AS VALUE, ";
        $query .= "     VALUE(HDAT.HR_NAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "            ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR        = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE       = REGD.GRADE ";
        $query .= "            AND GDAT.SCHOOL_KIND = 'H' ";
        $query .= "            AND GDAT.GRADE_CD    = '03' ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."'";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //SCHREG_REGD_DAT 取得
    public function getList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.SCHREGNO AS VALUE, ";
        $query .= "     VALUE(BASE.NAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN MOCK_TRAINREMARK_DAT MCOK ";
        $query .= "             ON MCOK.SCHREGNO = REGD.SCHREGNO ";
        if ($model->field["TITLEDIV"] == "1") {
            $query .= "        AND MCOK.HOPE_COLLEGE_NAME1 IS NOT NULL ";
            $query .= "        AND MCOK.HOPE_COURSE_NAME1 IS NOT NULL ";
        } else {
            $query .= "        AND MCOK.HOPE_COLLEGE_NAME2 IS NOT NULL ";
            $query .= "        AND MCOK.HOPE_COURSE_NAME2 IS NOT NULL ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "            ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "            ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."'";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース名証の取得
    public function getCourseName($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(L1.COURSENAME, '') || VALUE(L2.MAJORNAME, '') || VALUE(L3.COURSECODENAME, '') AS COURSENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN COURSE_MST L1 ";
        $query .= "             ON L1.COURSECD = REGD.COURSECD ";
        $query .= "     INNER JOIN MAJOR_MST L2 ";
        $query .= "             ON L2.COURSECD = REGD.COURSECD ";
        $query .= "            AND L2.MAJORCD  = REGD.MAJORCD ";
        $query .= "     INNER JOIN COURSECODE_MST L3 ";
        $query .= "             ON L3.COURSECODE = REGD.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= " AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND REGD.SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //生徒情報
    public function getSchregInfo($model, $schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     GDAT.GRADE_NAME2, ";
        $query .= "     HDAT.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "            ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "            ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE";
        $query .= "     REGD.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= " AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND REGD.SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //評定
    public function getReocrdRankSdiv($model, $schregno)
    {
        $query  = " WITH SCHNO_A AS( ";
        //過年度含めた学籍の取得
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     GDAT.GRADE_CD, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     <= '".$model->field["YEAR"]."'  ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."'  ";
        $query .= " ), SCHNO AS( ";
        //学籍の表
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHNO_A T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ";
        //講座の表
        $query .= " ) , CHAIR_A AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     S1.SCHREGNO, ";
        $query .= "     S2.CLASSCD, ";
        $query .= "     S2.SCHOOL_KIND, ";
        $query .= "     S2.CURRICULUM_CD, ";
        $query .= "     S2.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT S1 ";
        $query .= "     INNER JOIN CHAIR_DAT S2 ";
        $query .= "             ON S2.YEAR     = S1.YEAR ";
        $query .= "            AND S2.SEMESTER = S1.SEMESTER ";
        $query .= "            AND S2.CHAIRCD  = S1.CHAIRCD ";
        $query .= "            AND S2.SUBCLASSCD <= '89' ";
        $query .= "            AND S2.SUBCLASSCD NOT LIKE '50%' ";
        $query .= "     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) S3 ";
        $query .= "             ON S3.YEAR     = S1.YEAR ";
        $query .= "            AND S3.SCHREGNO = S1.SCHREGNO ";
        //成績明細データの表
        $query .= " ) ,RECORD00 AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ";
        $query .= "     FROM RECORD_RANK_SDIV_DAT T1 ";
        $query .= "     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) T2 ";
        $query .= "             ON T2.YEAR     = T1.YEAR ";
        $query .= "            AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ";
        $query .= "         AND T1.SUBCLASSCD NOT LIKE '50%' ";
        $query .= "         AND T1.SUBCLASSCD NOT IN ('333333', '555555') ";
        $query .= " ) ,RECORD0 AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ";
        $query .= "            , L2.SCORE ";
        $query .= "     FROM RECORD00 T1 ";
        $query .= "     INNER JOIN SCHNO T2 ";
        $query .= "             ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ";
        $query .= "            ON L2.YEAR          = T1.YEAR ";
        $query .= "           AND L2.SEMESTER      = T1.SEMESTER ";
        $query .= "           AND L2.TESTKINDCD    = T1.TESTKINDCD ";
        $query .= "           AND L2.TESTITEMCD    = T1.TESTITEMCD ";
        $query .= "           AND L2.SCORE_DIV     = T1.SCORE_DIV ";
        $query .= "           AND L2.CLASSCD       = T1.CLASSCD ";
        $query .= "           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "           AND L2.SCHREGNO      = T1.SCHREGNO ";
        $query .= "     INNER JOIN SUBCLASS_YDAT SUBY ";
        $query .= "            ON SUBY.YEAR          = T1.YEAR ";
        $query .= "           AND SUBY.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "           AND SUBY.CLASSCD       = T1.CLASSCD ";
        $query .= "           AND SUBY.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "           AND SUBY.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= " ) ,RECORD_M AS( ";
        $query .= "     SELECT ";
        $query .= "              T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ";
        $query .= "            , SUM(T1.SCORE) AS SCORE ";
        $query .= "            , AVG(T1.SCORE) AS AVG ";
        $query .= "     FROM RECORD0 T1 ";
        $query .= "     GROUP BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ";
        $query .= " ) ,RECORD AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "       T3.CLASSNAME, ";
        $query .= "       T4.SUBCLASSNAME, ";
        $query .= "       T1.* ";
        $query .= "     FROM RECORD_M T1 ";
        $query .= "     INNER JOIN CHAIR_A T5 ";
        $query .= "        ON T5.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "       AND T5.SCHREGNO      = T1.SCHREGNO ";
        $query .= "       AND T5.CLASSCD       = T1.CLASSCD ";
        $query .= "       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     INNER JOIN CLASS_MST T3 ";
        $query .= "        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
        $query .= "       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "     INNER JOIN SUBCLASS_MST T4 ";
        $query .= "        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "       AND T4.CLASSCD       = T1.CLASSCD ";
        $query .= "       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "   UNION ALL ";
        $query .= "     SELECT ";
        $query .= "       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ";
        $query .= "       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ";
        $query .= "       T1.* ";
        $query .= "      FROM RECORD_M T1 ";
        $query .= "      WHERE T1.SUBCLASSCD = '999999' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "       T1.* ";
        $query .= " FROM RECORD T1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD,  ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV  ";
        return $query;
    }

    //過年度含む履修科目を取得
    public function getSubclassName($model, $schregno, $classcd)
    {
        $query .= " WITH SCHNO_A AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     GDAT.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     <= '".$model->field["YEAR"]."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " ), SCHNO AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHNO_A T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ";
        $query .= " ) , CHAIR_A AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     S1.SCHREGNO, ";
        $query .= "     S2.CLASSCD, ";
        $query .= "     S2.SCHOOL_KIND, ";
        $query .= "     S2.CURRICULUM_CD, ";
        $query .= "     S2.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT S1 ";
        $query .= "     INNER JOIN CHAIR_DAT S2 ";
        $query .= "             ON S2.YEAR     = S1.YEAR ";
        $query .= "            AND S2.SEMESTER = S1.SEMESTER ";
        $query .= "            AND S2.CHAIRCD  = S1.CHAIRCD ";
        $query .= "     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) S3 ";
        $query .= "             ON S3.YEAR     = S1.YEAR ";
        $query .= "            AND S3.SCHREGNO = S1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T4.CLASSCD, ";
        $query .= "   T4.SCHOOL_KIND, ";
        $query .= "   T4.CURRICULUM_CD, ";
        $query .= "   T4.SUBCLASSCD, ";
        $query .= "   T4.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "   CHAIR_A T1 ";
        $query .= "   INNER JOIN CLASS_MST T3 ";
        $query .= "      ON T3.CLASSCD     = T1.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "   INNER JOIN SUBCLASS_MST T4 ";
        $query .= "      ON T4.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "     AND T4.CLASSCD       = T1.CLASSCD ";
        $query .= "     AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "     AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "   T1.CLASSCD = '".$classcd."' ";
        $query .= " ORDER BY SUBCLASSCD ";
        return $query;
    }

    //指定校推薦総合成績を取得
    public function getAftSchregRecommendationRank($model, $schregno)
    {
        $query .= " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   T1.TEST_VALUATION_AVG + T1.TEST_VALUATION_AVG_PERCENT + T1.TEST_VALUATION_PERCENT_SCORE AS TOTAL_SCORE, ";
        $query .= "   T2.CNT ";
        $query .= " FROM ";
        $query .= "   AFT_SCHREG_RECOMMENDATION_RANK_DAT T1 ";
        $query .= "   INNER JOIN (SELECT YEAR, COUNT(*) AS CNT ";
        $query .= "               FROM AFT_SCHREG_RECOMMENDATION_RANK_DAT  ";
        $query .= "               GROUP BY YEAR) T2 ";
        $query .= "           ON T2.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= " AND T1.SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //所見
    public function getMockTrainremark($model, $schregno)
    {
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   MOCK_TRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //過年度含む履修科目の評定平均
    public function getRecordRankAvg($model, $schregno, $classcd)
    {
        $query .= " WITH SCHNO_A AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     GDAT.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     <= '".$model->field["YEAR"]."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " ), SCHNO AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHNO_A T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ";
        $query .= " ) , CHAIR_A AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     S1.SCHREGNO, ";
        $query .= "     S2.CLASSCD, ";
        $query .= "     S2.SCHOOL_KIND, ";
        $query .= "     S2.CURRICULUM_CD, ";
        $query .= "     S2.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT S1 ";
        $query .= "     INNER JOIN CHAIR_DAT S2 ";
        $query .= "             ON S2.YEAR     = S1.YEAR ";
        $query .= "            AND S2.SEMESTER = S1.SEMESTER ";
        $query .= "            AND S2.CHAIRCD  = S1.CHAIRCD ";
        $query .= "     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) S3 ";
        $query .= "             ON S3.YEAR     = S1.YEAR ";
        $query .= "            AND S3.SCHREGNO = S1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   R1.CLASSCD, ";
        $query .= "   AVG(R1.SCORE) AS SCORE_AVG ";
        $query .= " FROM ";
        $query .= "   SCHNO T1 ";
        $query .= "   INNER JOIN RECORD_RANK_SDIV_DAT R1 ";
        $query .= "      ON R1.YEAR = T1.YEAR ";
        $query .= "     AND R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '9990009' ";
        $query .= "     AND R1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND R1.CLASSCD = '".$classcd."' ";
        $query .= "   INNER JOIN CHAIR_A T2 ";
        $query .= "      ON T2.SUBCLASSCD    = R1.SUBCLASSCD ";
        $query .= "     AND T2.SUBCLASSCD    = R1.SUBCLASSCD ";
        $query .= "     AND T2.CLASSCD       = R1.CLASSCD ";
        $query .= "     AND T2.SCHOOL_KIND   = R1.SCHOOL_KIND ";
        $query .= "     AND T2.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "   INNER JOIN CLASS_MST T3 ";
        $query .= "      ON T3.CLASSCD     = R1.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND = R1.SCHOOL_KIND ";
        $query .= "   INNER JOIN SUBCLASS_MST T4 ";
        $query .= "      ON T4.SUBCLASSCD    = R1.SUBCLASSCD ";
        $query .= "     AND T4.CLASSCD       = R1.CLASSCD ";
        $query .= "     AND T4.SCHOOL_KIND   = R1.SCHOOL_KIND ";
        $query .= "     AND T4.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= " GROUP BY ";
        $query .= "   R1.CLASSCD ";
        return $query;
    }


    //出欠状況の取得
    public function getAttend($model, $schregno)
    {
        list($year, $month) = explode("-", $model->fiedl["E_MONTH"]);
        $query .= " WITH SCHNO_A AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     GDAT.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     <= '".$model->field["YEAR"]."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " ), SCHNO AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHNO_A T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   SUM(VALUE(T2.SICK, 0)) AS SICK, ";
        $query .= "   SUM(VALUE(T2.LATE, 0)) AS LATE, ";
        $query .= "   SUM(VALUE(T2.EARLY, 0)) AS EARLY ";
        $query .= " FROM ";
        $query .= "   SCHNO T1 ";
        $query .= "   LEFT JOIN V_ATTEND_SEMES_DAT T2 ";
        $query .= "          ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR  <= '".$year."' ";
        $query .= "         AND T2.MONTH <= '".$month."' ";
        $query .= " GROUP BY T1.YEAR ";
        return $query;
    }
}
