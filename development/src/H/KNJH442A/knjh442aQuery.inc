<?php

class knjh442aQuery extends Query
{
    //学年取得
    public function getSelectGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //SCHREG_REGD_DAT 取得
    public function getList($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->field["OUTPUT"] == "1") {
            //学級別
            $query .= "     REGD.GRADE || REGD.HR_CLASS AS VALUE, ";
            $query .= "     VALUE(HDAT.HR_NAME, '') AS LABEL ";
        } else {
            //コース別
            $query .= "     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS VALUE, ";
            $query .= "     VALUE(L1.COURSENAME, '') || VALUE(L2.MAJORNAME, '') || VALUE(L3.COURSECODENAME, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "            ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "           AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "           AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "            AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     LEFT JOIN COURSE_MST L1 ";
        $query .= "            ON L1.COURSECD = REGD.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ";
        $query .= "            ON L2.COURSECD = REGD.COURSECD ";
        $query .= "           AND L2.MAJORCD  = REGD.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST L3 ";
        $query .= "            ON L3.COURSECODE = REGD.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."'";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校情報取得
    public function getCertifSchoolDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND CERTIF_KINDCD = '104' ";
        return $query;
    }

    //SCHREG_REGD_GDATの取得
    public function getSchregRegdGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    //評定の科目を取得
    public function getSubclass($model, $value)
    {
        $query  = " WITH SCHNO AS( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     YEAR, SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "         YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["OUTPUT"] == "1") {
            $query .= " AND GRADE || HR_CLASS = '".$value."' ";
        } else {
            $query .= " AND COURSECD || MAJORCD || COURSECODE = '".$value."' ";
        }
        $query .= " ), RECORD AS( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     INNER JOIN RECORD_RANK_SDIV_DAT T2 ";
        $query .= "             ON T2.YEAR = T1.YEAR ";
        $query .= "            AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990009' ";
        $query .= "            AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SUBCLASS_YDAT SUBY ";
        $query .= "             ON SUBY.YEAR          = T2.YEAR ";
        $query .= "            AND SUBY.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "            AND SUBY.CLASSCD       = T2.CLASSCD ";
        $query .= "            AND SUBY.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "            AND SUBY.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   CASE WHEN ATT1.COMBINED_CLASSCD IS NOT NULL ";
        $query .= "        THEN ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD ";
        $query .= "        ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "   END AS SUBCLASSCD, ";
        $query .= "   VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
        $query .= "   VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
        $query .= "   VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
        $query .= " FROM SUBCLASS_MST T1 ";
        $query .= "      LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "      LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '".$model->field["YEAR"]."' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "      INNER JOIN RECORD R1 ";
        $query .= "              ON R1.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "             AND R1.CLASSCD       = T1.CLASSCD ";
        $query .= "             AND R1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "             AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= " WHERE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D026') ";
        $query .= " ORDER BY CLASS_SHOWORDER3, SUBCLASS_SHOWORDER3 ";
        return $query;
    }

    //SCHREG_REGD_HDAT の取得
    public function getSchregRegdHdat($model, $value)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " AND GRADE || HR_CLASS = '".$value."' ";
        return $query;
    }

    //コース名証の取得
    public function getCourseName($model, $value)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(L1.COURSENAME, '') || VALUE(L2.MAJORNAME, '') || VALUE(L3.COURSECODENAME, '') AS COURSENAME ";
        $query .= " FROM ";
        $query .= "     ( SELECT DISTINCT COURSECD, MAJORCD, COURSECODE ";
        $query .= "       FROM SCHREG_REGD_DAT ";
        $query .= "       WHERE YEAR = '".$model->field["YEAR"]."' AND COURSECD || MAJORCD || COURSECODE = '".$value."' ";
        $query .= "     ) REGD ";
        $query .= "     INNER JOIN COURSE_MST L1 ";
        $query .= "             ON L1.COURSECD = REGD.COURSECD ";
        $query .= "     INNER JOIN MAJOR_MST L2 ";
        $query .= "             ON L2.COURSECD = REGD.COURSECD ";
        $query .= "            AND L2.MAJORCD  = REGD.MAJORCD ";
        $query .= "     INNER JOIN COURSECODE_MST L3 ";
        $query .= "             ON L3.COURSECODE = REGD.COURSECODE ";
        return $query;
    }

    //生徒情報
    public function getSchregInfo($model, $value)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "             ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE";
        $query .= "     REGD.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["OUTPUT"] == "1") {
            $query .= " AND REGD.GRADE || REGD.HR_CLASS = '".$value."' ";
        } else {
            $query .= " AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE = '".$value."' ";
        }
        $query .= " ORDER BY";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     REGD.COURSECD, ";
            $query .= "     REGD.MAJORCD, ";
            $query .= "     REGD.COURSECODE, ";
        }
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";
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

    
    //AFT_RECOMMENDATION_RANK_HEAD_DAT の取得
    public function getAftRecommendationRankHead($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_RANK_HEAD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
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

    //平均の取得
    public function getAvgRecordRank($model, $kbn, $value)
    {
        $query .= " WITH SCHNO AS(  ";
        $query .= "   SELECT DISTINCT  ";
        $query .= "     YEAR, SCHREGNO  ";
        $query .= "   FROM  ";
        $query .= "     SCHREG_REGD_DAT  ";
        $query .= "   WHERE  ";
        $query .= "       YEAR = '".$model->field["YEAR"]."'  ";
        if ($kbn == "GRADE") {
            $query .= "   AND GRADE = '".$value."' ";
        } elseif ($kbn == "CLASS") {
            $query .= "   AND GRADE || HR_CLASS = '".$value."' ";
        } elseif ($kbn == "COURSE") {
            $query .= "   AND COURSECD || MAJORCD || COURSECODE = '".$value."' ";
        }
        $query .= " )  ";
        $query .= " SELECT  ";
        $query .= "   T2.CLASSCD ||'-'|| T2.SCHOOL_KIND ||'-'|| T2.CURRICULUM_CD ||'-'|| T2.SUBCLASSCD AS SUBCLASSCD,  ";
        $query .= "   SUM(T2.SCORE) AS SCORE,  ";
        $query .= "   AVG(T2.SCORE) AS SCORE_AVG  ";
        $query .= " FROM  ";
        $query .= "   SCHNO T1  ";
        $query .= "   INNER JOIN RECORD_RANK_SDIV_DAT T2  ";
        $query .= "           ON T2.YEAR = T1.YEAR  ";
        $query .= "          AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990009'  ";
        $query .= "          AND T2.SCHREGNO = T1.SCHREGNO  ";
        $query .= "   INNER JOIN SUBCLASS_YDAT SUBY  ";
        $query .= "           ON SUBY.YEAR          = T2.YEAR  ";
        $query .= "          AND SUBY.SUBCLASSCD    = T2.SUBCLASSCD  ";
        $query .= "          AND SUBY.CLASSCD       = T2.CLASSCD  ";
        $query .= "          AND SUBY.SCHOOL_KIND   = T2.SCHOOL_KIND  ";
        $query .= "          AND SUBY.CURRICULUM_CD = T2.CURRICULUM_CD  ";
        $query .= " GROUP BY  ";
        $query .= "   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD  ";
        return $query;
    }

    //総合成績の平均取得
    public function getAvgAftSchregRecommendationRank($model, $kbn, $value)
    {
        $query .= " WITH SCHNO AS( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       YEAR, ";
        $query .= "       SCHREGNO ";
        $query .= "   FROM ";
        $query .= "       SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "         YEAR = '".$model->field["YEAR"]."'  ";
        if ($kbn == "GRADE") {
            $query .= "     AND GRADE = '".$value."' ";
        } elseif ($kbn == "CLASS") {
            $query .= "     AND GRADE || HR_CLASS = '".$value."' ";
        } elseif ($kbn == "COURSE") {
            $query .= "     AND COURSECD || MAJORCD || COURSECODE = '".$value."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     AVG(T2.MOCK_NATIONAL_LANGUAGE_AVG) AS MOCK_NATIONAL_LANGUAGE_AVG, ";
        $query .= "     AVG(T2.MOCK_MATH_AVG) AS MOCK_MATH_AVG, ";
        $query .= "     AVG(T2.MOCK_ENGLISH_AVG) AS MOCK_ENGLISH_AVG, ";
        $query .= "     AVG(T2.TEST_VALUATION_AVG) AS TEST_VALUATION_AVG, ";
        $query .= "     AVG(T2.TEST_VALUATION_PERCENT_SCORE) AS TEST_VALUATION_PERCENT_SCORE, ";
        $query .= "     AVG(T2.MOCK_TOTAL_AVG) AS MOCK_TOTAL_AVG, ";
        $query .= "     AVG(T2.MOCK_TOTAL_PERCENT_SCORE) AS MOCK_TOTAL_PERCENT_SCORE, ";
        $query .= "     AVG(T2.TEST_VALUATION_AVG + T2.TEST_VALUATION_AVG_PERCENT + T2.TEST_VALUATION_PERCENT_SCORE) AS TOTAL_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     INNER JOIN AFT_SCHREG_RECOMMENDATION_RANK_DAT T2 ";
        $query .= "            ON T2.YEAR = T1.YEAR ";
        $query .= "           AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " GROUP BY T2.YEAR ";
        return $query;
    }
}
